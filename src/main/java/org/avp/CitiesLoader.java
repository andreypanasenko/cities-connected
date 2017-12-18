package org.avp;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface CitiesLoader {

    CompletableFuture<Map<String,Set<String>>> loadData();
}
