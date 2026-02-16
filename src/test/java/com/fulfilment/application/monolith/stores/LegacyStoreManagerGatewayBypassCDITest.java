package com.fulfilment.application.monolith.stores;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class LegacyStoreManagerGatewayBypassCDITest {

    private LegacyStoreManagerGateway gateway;
    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream testOut;

    @BeforeEach
    void setUp() {
        gateway = new LegacyStoreManagerGateway();
        testOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(testOut));
    }

    @Test
    void testCreateStoreOnLegacySystemSuccess() {
        Store store = new Store("Test Store");
        store.setQuantityProductsInStock(100);

        gateway.createStoreOnLegacySystem(store);

        String output = testOut.toString();
        assertTrue(output.contains("Temporary file created at:"));
        assertTrue(output.contains("Data written to temporary file."));
        assertTrue(output.contains("Data read from temporary file:"));
        assertTrue(output.contains("Store created. [ name =Test Store ] [ items on stock =100 ]"));
        assertTrue(output.contains("Temporary file deleted."));
    }

    @Test
    void testCreateStoreOnLegacySystemNullStore() {
        gateway.createStoreOnLegacySystem(null);

        String output = testOut.toString();
        assertTrue(output.contains("java.lang.NullPointerException") ||
                   output.contains("Exception") ||
                   output.isEmpty()); // Exception might be caught and printed differently
    }

    @Test
    void testCreateStoreOnLegacySystemEmptyName() {
        Store store = new Store("");
        store.setQuantityProductsInStock(50);

        gateway.createStoreOnLegacySystem(store);

        String output = testOut.toString();
        assertTrue(output.contains("Store created. [ name = ] [ items on stock =50 ]"));
    }

    @Test
    void testCreateStoreOnLegacySystemSpecialCharacters() {
        Store store = new Store("Special!@#$%^&*()Store");
        store.setQuantityProductsInStock(75);

        gateway.createStoreOnLegacySystem(store);

        String output = testOut.toString();
        assertTrue(output.contains("Store created. [ name =Special!@#$%^&*()Store ] [ items on stock =75 ]"));
    }

    @Test
    void testCreateStoreOnLegacySystemUnicodeCharacters() {
        Store store = new Store("商店测试🏪");
        store.setQuantityProductsInStock(150);

        gateway.createStoreOnLegacySystem(store);

        String output = testOut.toString();
        assertTrue(output.contains("Store created. [ name =商店测试🏪 ] [ items on stock =150 ]"));
    }

    @Test
    void testCreateStoreOnLegacySystemZeroStock() {
        Store store = new Store("Zero Stock Store");
        store.setQuantityProductsInStock(0);

        gateway.createStoreOnLegacySystem(store);

        String output = testOut.toString();
        assertTrue(output.contains("Store created. [ name =Zero Stock Store ] [ items on stock =0 ]"));
    }

    @Test
    void testCreateStoreOnLegacySystemNegativeStock() {
        Store store = new Store("Negative Stock Store");
        store.setQuantityProductsInStock(-50);

        gateway.createStoreOnLegacySystem(store);

        String output = testOut.toString();
        assertTrue(output.contains("Store created. [ name =Negative Stock Store ] [ items on stock =-50 ]"));
    }

    @Test
    void testCreateStoreOnLegacySystemMaxIntStock() {
        Store store = new Store("Max Stock Store");
        store.setQuantityProductsInStock(Integer.MAX_VALUE);

        gateway.createStoreOnLegacySystem(store);

        String output = testOut.toString();
        assertTrue(output.contains("Store created. [ name =Max Stock Store ] [ items on stock =" + Integer.MAX_VALUE + " ]"));
    }

    @Test
    void testCreateStoreOnLegacySystemMinIntStock() {
        Store store = new Store("Min Stock Store");
        store.setQuantityProductsInStock(Integer.MIN_VALUE);

        gateway.createStoreOnLegacySystem(store);

        String output = testOut.toString();
        assertTrue(output.contains("Store created. [ name =Min Stock Store ] [ items on stock =" + Integer.MIN_VALUE + " ]"));
    }

    @Test
    void testCreateStoreOnLegacySystemSpacesName() {
        Store store = new Store("   ");
        store.setQuantityProductsInStock(25);

        gateway.createStoreOnLegacySystem(store);

        String output = testOut.toString();
        assertTrue(output.contains("Store created. [ name =   ] [ items on stock =25 ]"));
    }

    @Test
    void testUpdateStoreOnLegacySystemSuccess() {
        Store store = new Store("Update Store");
        store.setQuantityProductsInStock(200);

        gateway.updateStoreOnLegacySystem(store);

        String output = testOut.toString();
        assertTrue(output.contains("Temporary file created at:"));
        assertTrue(output.contains("Data written to temporary file."));
        assertTrue(output.contains("Data read from temporary file:"));
        assertTrue(output.contains("Store created. [ name =Update Store ] [ items on stock =200 ]"));
        assertTrue(output.contains("Temporary file deleted."));
    }

    @Test
    void testUpdateStoreOnLegacySystemNullStore() {
        gateway.updateStoreOnLegacySystem(null);

        String output = testOut.toString();
        assertTrue(output.contains("java.lang.NullPointerException") ||
                   output.contains("Exception") ||
                   output.isEmpty());
    }

    @Test
    void testUpdateStoreOnLegacySystemEmptyName() {
        Store store = new Store("");
        store.setQuantityProductsInStock(75);

        gateway.updateStoreOnLegacySystem(store);

        String output = testOut.toString();
        assertTrue(output.contains("Store created. [ name = ] [ items on stock =75 ]"));
    }

    @Test
    void testUpdateStoreOnLegacySystemNegativeStock() {
        Store store = new Store("Negative Update Store");
        store.setQuantityProductsInStock(-100);

        gateway.updateStoreOnLegacySystem(store);

        String output = testOut.toString();
        assertTrue(output.contains("Store created. [ name =Negative Update Store ] [ items on stock =-100 ]"));
    }

    @Test
    void testUpdateStoreOnLegacySystemZeroStock() {
        Store store = new Store("Zero Update Store");
        store.setQuantityProductsInStock(0);

        gateway.updateStoreOnLegacySystem(store);

        String output = testOut.toString();
        assertTrue(output.contains("Store created. [ name =Zero Update Store ] [ items on stock =0 ]"));
    }

    @Test
    void testMultipleOperations() {
        Store store1 = new Store("Store 1");
        store1.setQuantityProductsInStock(100);

        Store store2 = new Store("Store 2");
        store2.setQuantityProductsInStock(200);

        Store store3 = new Store("Store 3");
        store3.setQuantityProductsInStock(300);

        gateway.createStoreOnLegacySystem(store1);
        gateway.updateStoreOnLegacySystem(store2);
        gateway.createStoreOnLegacySystem(store3);

        String output = testOut.toString();

        assertTrue(output.contains("Store created. [ name =Store 1 ] [ items on stock =100 ]"));
        assertTrue(output.contains("Store created. [ name =Store 2 ] [ items on stock =200 ]"));
        assertTrue(output.contains("Store created. [ name =Store 3 ] [ items on stock =300 ]"));

        int fileCreatedCount = output.split("Temporary file created at:").length - 1;
        int dataWrittenCount = output.split("Data written to temporary file.").length - 1;
        int dataReadCount = output.split("Data read from temporary file:").length - 1;
        int fileDeletedCount = output.split("Temporary file deleted.").length - 1;

        assertEquals(3, fileCreatedCount);
        assertEquals(3, dataWrittenCount);
        assertEquals(3, dataReadCount);
        assertEquals(3, fileDeletedCount);
    }

    @Test
    void testWriteToFileContentIntegrity() {
        Store store = new Store("Content Test");
        store.setQuantityProductsInStock(456);

        gateway.createStoreOnLegacySystem(store);

        String output = testOut.toString();

        String[] lines = output.split("\n");
        String writtenLine = "";
        String readLine = "";

        for (int i = 0; i < lines.length; i++) {
            if (lines[i].contains("Data written to temporary file.")) {
                writtenLine = lines[i - 1].trim();
            }
            if (lines[i].contains("Data read from temporary file:")) {
                readLine = lines[i].substring("Data read from temporary file: ".length()).trim();
            }
        }

        assertTrue(writtenLine.contains("Store created. [ name =Content Test ] [ items on stock =456 ]"));
        assertTrue(readLine.contains("Store created. [ name =Content Test ] [ items on stock =456 ]"));
    }

    @Test
    void testCreateAndUpdateSameStore() {
        Store store = new Store("Same Store");
        store.setQuantityProductsInStock(100);

        gateway.createStoreOnLegacySystem(store);
        String createOutput = testOut.toString();
        testOut.reset();

        gateway.updateStoreOnLegacySystem(store);
        String updateOutput = testOut.toString();

        assertTrue(createOutput.contains("Store created. [ name =Same Store ] [ items on stock =100 ]"));
        assertTrue(updateOutput.contains("Store created. [ name =Same Store ] [ items on stock =100 ]"));
    }

    @Test
    void testStoreWithVeryLongName() {
        String longName = "A".repeat(1000);
        Store store = new Store(longName);
        store.setQuantityProductsInStock(100);

        gateway.createStoreOnLegacySystem(store);

        String output = testOut.toString();
        assertTrue(output.contains("Store created. [ name =" + longName + " ] [ items on stock =100 ]"));
    }

    @Test
    void testWriteToFileDirectly() throws Exception {
        Store store = new Store("Direct Test");
        store.setQuantityProductsInStock(999);

        Method writeToFileMethod = LegacyStoreManagerGateway.class.getDeclaredMethod("writeToFile", Store.class);
        writeToFileMethod.setAccessible(true);

        assertDoesNotThrow(() -> {
            try {
                writeToFileMethod.invoke(gateway, store);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        String output = testOut.toString();
        assertTrue(output.contains("Temporary file created at:"));
        assertTrue(output.contains("Data written to temporary file."));
        assertTrue(output.contains("Data read from temporary file:"));
        assertTrue(output.contains("Store created. [ name =Direct Test ] [ items on stock =999 ]"));
        assertTrue(output.contains("Temporary file deleted."));
    }

    @Test
    void testLegacyStoreManagerGatewayConstructor() {
        LegacyStoreManagerGateway gateway = new LegacyStoreManagerGateway();
        assertNotNull(gateway);
    }

    @Test
    void testLegacyStoreManagerGatewayMethods() throws Exception {
        // Test that all expected methods exist
        Method[] methods = LegacyStoreManagerGateway.class.getDeclaredMethods();

        boolean hasCreateMethod = false;
        boolean hasUpdateMethod = false;
        boolean hasWriteToFileMethod = false;

        for (Method method : methods) {
            switch (method.getName()) {
                case "createStoreOnLegacySystem":
                    hasCreateMethod = true;
                    break;
                case "updateStoreOnLegacySystem":
                    hasUpdateMethod = true;
                    break;
                case "writeToFile":
                    hasWriteToFileMethod = true;
                    break;
            }
        }

        assertTrue(hasCreateMethod);
        assertTrue(hasUpdateMethod);
        assertTrue(hasWriteToFileMethod);
    }

    @Test
    void testLegacyStoreManagerGatewayPrivateMethodAccess() throws Exception {
        // Test that we can access private methods
        Method writeToFileMethod = LegacyStoreManagerGateway.class.getDeclaredMethod("writeToFile", Store.class);
        assertNotNull(writeToFileMethod);
        assertEquals("writeToFile", writeToFileMethod.getName());
        assertEquals(1, writeToFileMethod.getParameterCount());
        assertEquals(Store.class, writeToFileMethod.getParameterTypes()[0]);
    }

    private static void assertDoesNotThrow(Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            fail("Expected no exception, but got: " + e.getMessage());
        }
    }
}
