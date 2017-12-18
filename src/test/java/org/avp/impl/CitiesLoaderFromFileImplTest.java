package org.avp.impl;

import org.avp.CitiesLoader;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CitiesLoaderFromFileImplTest {

    private ExecutorService executorService;

    private CitiesLoader citiesLoader;

    @Before
    public void setUp() throws Exception {

        executorService = Executors.newFixedThreadPool(1);
        citiesLoader = new CitiesLoaderFromFileImpl("src/test/java/resources/cities.txt", executorService);

    }

    @Test
    public void loadData() throws Exception {

        Map<String, Set<String>> cities = citiesLoader.loadData().get();

        assertEquals(8, cities.size());
        assertTrue(cities.containsKey("new york"));
        assertTrue(cities.containsKey("boston"));
        assertTrue(cities.containsKey("hartford"));
        assertTrue(cities.containsKey("criton-harmon"));
        assertTrue(cities.containsKey("danbury"));
        assertTrue(cities.containsKey("trenton"));
        assertTrue(cities.containsKey("princeton"));
        assertTrue(cities.containsKey("livittown"));

        //Check Boston
        assertEquals(1, cities.get("boston").size());
        assertTrue(cities.get("boston").contains("new york"));

        //Check Hartford
        assertEquals(1, cities.get("hartford").size());
        assertTrue(cities.get("hartford").contains("new york"));

        //Check Danbury
        assertEquals(1, cities.get("danbury").size());
        assertTrue(cities.get("danbury").contains("criton-harmon"));

        //Check Criton-Harmon
        assertEquals(2, cities.get("criton-harmon").size());
        assertTrue(cities.get("criton-harmon").contains("danbury"));
        assertTrue(cities.get("criton-harmon").contains("new york"));

        //Check New York
        assertEquals(3, cities.get("new york").size());
        assertTrue(cities.get("new york").contains("criton-harmon"));
        assertTrue(cities.get("new york").contains("boston"));
        assertTrue(cities.get("new york").contains("hartford"));

        //Check Trenton
        assertEquals(2, cities.get("trenton").size());
        assertTrue(cities.get("trenton").contains("livittown"));
        assertTrue(cities.get("trenton").contains("princeton"));

        //Check Livittown
        assertEquals(2, cities.get("livittown").size());
        assertTrue(cities.get("livittown").contains("trenton"));
        assertTrue(cities.get("livittown").contains("princeton"));

        //Check Princeton
        assertEquals(2, cities.get("princeton").size());
        assertTrue(cities.get("princeton").contains("trenton"));
        assertTrue(cities.get("princeton").contains("livittown"));

    }

}