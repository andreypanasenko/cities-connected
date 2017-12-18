package org.avp;

import java.util.concurrent.CompletableFuture;

public interface CityConnectResolver {

    CompletableFuture<Boolean> isConnected(String cityOne, String cityTwo);

}
