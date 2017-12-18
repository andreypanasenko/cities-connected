package org.avp.impl;

import org.avp.CitiesLoader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class CitiesLoaderFromFileImpl implements CitiesLoader {

    private final File dataFile;

    private final ExecutorService executorService;

    public CitiesLoaderFromFileImpl(String fileName, ExecutorService executorService)
            throws IOException {
        this.dataFile = new File(fileName);
        if (!dataFile.exists()
                || !dataFile.isFile()
                || !dataFile.canRead()) {
            throw new FileNotFoundException("Error file " + fileName + " is unreadable");
        }

        this.executorService = executorService;
    }

    @Override
    public CompletableFuture<Map<String,Set<String>>> loadData() {
        return supplyAsync(() -> {
            ConcurrentHashMap<String, Set<String>> cities = new ConcurrentHashMap<>();
            BufferedReader buffer = null;
            try {
                buffer = Files.newBufferedReader(dataFile.toPath());
                String line = buffer.readLine();
                while (line != null) {
                    String [] pair = line.split(",");
                    if (pair.length == 2) {
                        String leftCityName = pair[0].trim().toLowerCase();
                        String rightCityName = pair[1].trim().toLowerCase();
                        // Register forward direction
                        cities.compute(leftCityName, (name, citiesSet) -> {
                            Set<String> set = citiesSet == null ? new HashSet<>() : citiesSet;
                            set.add(rightCityName);
                            return set;
                        });
                        // Register backward direction
                        cities.compute(rightCityName, (name, citiesSet) -> {
                            Set<String> set = citiesSet == null ? new HashSet<>() : citiesSet;
                            set.add(leftCityName);
                            return set;
                        });
                    }
                    line = buffer.readLine();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                if (buffer != null) {
                    try {
                        buffer.close();
                    } catch (IOException e) {
                }                }
            }
            return cities;
        }, executorService);
    }
}
