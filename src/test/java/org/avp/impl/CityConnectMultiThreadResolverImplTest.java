package org.avp.impl;

import org.avp.CityConnectResolver;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CityConnectMultiThreadResolverImplTest {

    private CityConnectResolver connectResolver;

    private Map<String, Set<String>> cities;

    private ExecutorService executorService;

    @Before
    public void setUp() throws Exception {
        executorService = Executors.newFixedThreadPool(2);
        cities = new ConcurrentHashMap<>();
        connectResolver = new CityConnectMultiThreadResolverImpl(cities, executorService);

        initData(cities);

    }

    @Test
    public void isConnectedTwoDirectConnected() throws Exception {

        assertTrue(connectResolver.isConnected("new york", "boston").get());
        assertTrue(connectResolver.isConnected("boston", "new york").get());

    }

    @Test
    public void isConnectedTwoOverOneConnected() throws Exception {

        assertTrue(connectResolver.isConnected("hartford", "boston").get());
        assertTrue(connectResolver.isConnected("boston", "hartford").get());

    }

    @Test
    public void isConnectedTwoOverTwoConnected() throws Exception {

        assertTrue(connectResolver.isConnected("hartford", "danbury").get());
        assertTrue(connectResolver.isConnected("danbury", "hartford").get());

    }

    @Test
    public void isConnectedCyclicConnected() throws Exception {

        assertTrue(connectResolver.isConnected("livittown", "trenton").get());
        assertTrue(connectResolver.isConnected("trenton", "livittown").get());

    }

    @Test
    public void isConnectedNotDifferentNetworksConnected() throws Exception {

        assertFalse(connectResolver.isConnected("hartford", "livittown").get());
        assertFalse(connectResolver.isConnected("livittown", "hartford").get());

    }

    @Test
    public void isConnectedSelfConnected() throws Exception {

        assertTrue(connectResolver.isConnected("hartford", "hartford").get());

    }

    @Test
    public void isConnectedNotConnectedUnknown() throws Exception {

        assertFalse(connectResolver.isConnected("hartford", "unknown").get());

    }

    @Test
    public void isConnectedNotConnectedUnknownBoth() throws Exception {

        assertFalse(connectResolver.isConnected("london", "unknown").get());

    }


    private void initData(Map<String,Set<String>> cities) {

        // Define two not connected networks:
        // 1. New York -> Boston, Croton-Harmon, Hartford, Danbury(thought Croton-Harmon)
        // 2. Trenton -> Livittown, Princeton (Cyclic linked)

        //New York connections
        Set<String> ny = new HashSet<>();
        ny.add("boston");
        ny.add("hartford");
        ny.add("croton-harmon");
        cities.put("new york", ny);

        //Boston
        Set<String> boston = new HashSet<>();
        boston.add("new york");
        cities.put("boston", boston);

        //Hartford
        Set<String> hartford = new HashSet<>();
        hartford.add("new york");
        cities.put("hartford", hartford);

        //Croton-Harmon
        Set<String> crotonHarmon = new HashSet<>();
        crotonHarmon.add("new york");
        crotonHarmon.add("danbury");
        cities.put("croton-harmon", crotonHarmon);

        //Danbury
        Set<String> danbury = new HashSet<>();
        danbury.add("croton-harmon");
        cities.put("danbury", danbury);

        //Trenton
        Set<String> trenton = new HashSet<>();
        trenton.add("livittown");
        trenton.add("princeton");
        cities.put("trenton", trenton);

        //Livittown
        Set<String> livittown = new HashSet<>();
        livittown.add("trenton");
        livittown.add("princeton");
        cities.put("livittown", livittown);

        //Princeton
        Set<String> princeton = new HashSet<>();
        princeton.add("trenton");
        princeton.add("livittown");
        cities.put("princeton", princeton);


    }
}