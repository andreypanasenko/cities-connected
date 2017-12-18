import org.avp.CityConnectResolver;
import org.avp.impl.CitiesLoaderFromFileImpl;
import org.avp.impl.CityConnectMultiThreadResolverImpl;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

import static java.util.concurrent.Executors.newFixedThreadPool;

public class Connected {

    private static int DEFAULT_NUMBER_OF_THREADS = 4;

    private static ExecutorService executor = newFixedThreadPool(DEFAULT_NUMBER_OF_THREADS);

    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("usage: java Connected <cities file name> <cityOne> <cityTwo>");
            return;
        }
        final String dataFileName = args.length > 0 ? args[0] : "undefined";
        final String cityFrom = args.length > 1 ? args[1].trim().toLowerCase() : "undefined";
        final String cityTo = args.length > 2 ? args[2].trim().toLowerCase() : "undefined";

        if (Objects.isNull(dataFileName)
                || Objects.isNull(cityFrom)
                || Objects.isNull(cityTo)) {
            System.err.println("Error mandatory parameters must not be empty");
            System.err.println("usage: java Connected <cities file name> <cityOne> <cityTwo>");
            return;
        }

        try {

            CitiesLoaderFromFileImpl loader = new CitiesLoaderFromFileImpl(dataFileName, executor);

            loader.loadData()
                    .thenCompose(cities -> {
                        CityConnectResolver resolver = new CityConnectMultiThreadResolverImpl(cities, executor);
                        return resolver.isConnected(cityFrom, cityTo);
                    })
                    .thenApply(result -> {
                        if (result) {
                            System.out.println("yes");
                        } else {
                            System.out.println("no");
                        }
                        return null;
                    })
                    .join();

        } catch (IOException e) {
            System.err.println("Error loadin data from file " + dataFileName + " error: " + e.getMessage());
        } finally {
            executor.shutdown();
        }
    }
}
