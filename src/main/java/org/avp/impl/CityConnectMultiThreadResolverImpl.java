package org.avp.impl;

import org.avp.CityConnectResolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.stream.Stream;

import static java.util.Collections.newSetFromMap;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.supplyAsync;

public class CityConnectMultiThreadResolverImpl implements CityConnectResolver {

    // Map cities represent all cities as key and value is Set of cities which are connected
    // Graph is unidirectional and New York, Boston record mean two cities in map with one set elements.
    private final Map<String, Set<String>> cities;

    private final ExecutorService executorService;

    public CityConnectMultiThreadResolverImpl(Map<String, Set<String>> cities, ExecutorService executorService) {
        this.cities = cities;
        this.executorService = executorService;
    }

    @Override
    public CompletableFuture<Boolean> isConnected(String cityOne, String cityTwo) {
        return supplyAsync(() -> {
            // I'm know that Sets visited and notChecked would consumes additional memory, but current implementation
            // looks more clear and give ability to server more than one request simultaneously
            Set<String> visited = newSetFromMap(new ConcurrentHashMap<>());

            Set<String> connected = cities.get(cityOne);
            Set<String> notChecked = newSetFromMap(new ConcurrentHashMap<>());
            if (connected != null) {
                if (connected.contains(cityTwo)) {
                    //Yes connected
                    return true;
                } else {
                    visited.add(cityOne);
                    connected.stream().filter(cityName -> !visited.contains(cityName)).forEach(notChecked::add);
                    while (!notChecked.isEmpty()) {
                        List<String> toCheck = new ArrayList<>(notChecked);
                        notChecked.clear();
                        if (mergeFutures(toCheck.stream()
                                .map(checkingCity -> findConnection(checkingCity, cityTwo, visited, notChecked)))
                                .thenApply(results -> results.anyMatch(c -> c))
                                .join()) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }, executorService);
    }

    private CompletableFuture<Boolean> findConnection(String checkingCity, String destinationCity, Set<String> visited, Set<String> notChecked) {
        return supplyAsync(() -> {
            Result result = new Result();
            cities.computeIfPresent(checkingCity, (name, connected) -> {
                if (connected.contains(destinationCity)) {
                    // Yes connected
                    result.setConnected(true);
                } else {
                    visited.add(checkingCity);
                    connected.stream()
                            .filter(cityName -> !visited.contains(cityName))
                            .forEach(notChecked::add);
                }
                return connected;
            });
            return result.isConnected();
        }, executorService);
    }

    /*
    Static method used for transform List of CompletableFutures to CompletableFuture of List
    and completed when all of futures completed.
     */
    private static CompletableFuture<Stream<Boolean>> mergeFutures(Stream<CompletableFuture<Boolean>> futures) {
        CompletableFuture<Stream.Builder<Boolean>> accumulator = completedFuture(Stream.builder());

        return futures.reduce(
                accumulator,
                (acc, f) -> acc.thenCompose(xs -> f.thenApply(xs::add)),
                (acc1, acc2) -> {
                    throw new UnsupportedOperationException("Combining results not supported");
                }
        ).thenApply(Stream.Builder::build);
    }

    private static class Result {

        private boolean connected;

        public boolean isConnected() {
            return connected;
        }

        public void setConnected(boolean connected) {
            this.connected = connected;
        }
    }
}
