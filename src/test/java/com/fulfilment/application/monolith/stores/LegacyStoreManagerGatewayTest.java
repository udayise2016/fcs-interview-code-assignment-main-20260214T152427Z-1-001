package com.fulfilment.application.monolith.stores;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class LegacyStoreManagerGatewayTest {

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
    void testCreateStoreOnLegacySystem_ValidStore() {
        Store store = new Store("Test Store");
        store.setQuantityProductsInStock(100);

        assertDoesNotThrow(() -> gateway.createStoreOnLegacySystem(store));
        
        String output = testOut.toString();
        assertTrue(output.contains("Temporary file created at:"));
        assertTrue(output.contains("Data written to temporary file."));
        assertTrue(output.contains("Data read from temporary file:"));
        assertTrue(output.contains("Store created. [ name =Test Store ] [ items on stock =100 ]"));
        assertTrue(output.contains("Temporary file deleted."));
    }

    @Test
    void testUpdateStoreOnLegacySystem_ValidStore() {
        Store store = new Store("Update Store");
        store.setQuantityProductsInStock(200);

        assertDoesNotThrow(() -> gateway.updateStoreOnLegacySystem(store));
        
        String output = testOut.toString();
        assertTrue(output.contains("Temporary file created at:"));
        assertTrue(output.contains("Data written to temporary file."));
        assertTrue(output.contains("Data read from temporary file:"));
        assertTrue(output.contains("Store created. [ name =Update Store ] [ items on stock =200 ]"));
        assertTrue(output.contains("Temporary file deleted."));
    }

    @Test
    void testCreateStoreOnLegacySystem_NullStore() {
        assertDoesNotThrow(() -> gateway.createStoreOnLegacySystem(null));
        
        String output = testOut.toString();
        // Should handle null gracefully and print stack trace
        assertTrue(output.contains("java.lang.NullPointerException") || output.contains("Exception"));
    }

    @Test
    void testUpdateStoreOnLegacySystem_NullStore() {
        assertDoesNotThrow(() -> gateway.updateStoreOnLegacySystem(null));
        
        String output = testOut.toString();
        // Should handle null gracefully and print stack trace
        assertTrue(output.contains("java.lang.NullPointerException") || output.contains("Exception"));
    }

    @Test
    void testCreateStoreOnLegacySystem_EmptyName() {
        Store store = new Store("");
        store.setQuantityProductsInStock(50);

        assertDoesNotThrow(() -> gateway.createStoreOnLegacySystem(store));
        
        String output = testOut.toString();
        assertTrue(output.contains("Store created. [ name = ] [ items on stock =50 ]"));
    }

    @Test
    void testCreateStoreOnLegacySystem_SpecialCharacters() {
        Store store = new Store("Store!@#$%^&*()");
        store.setQuantityProductsInStock(75);

        assertDoesNotThrow(() -> gateway.createStoreOnLegacySystem(store));
        
        String output = testOut.toString();
        assertTrue(output.contains("Store created. [ name =Store!@#$%^&*() ] [ items on stock =75 ]"));
    }

    @Test
    void testCreateStoreOnLegacySystem_UnicodeCharacters() {
        Store store = new Store("商店测试🏪");
        store.setQuantityProductsInStock(150);

        assertDoesNotThrow(() -> gateway.createStoreOnLegacySystem(store));
        
        String output = testOut.toString();
        assertTrue(output.contains("Store created. [ name =商店测试🏪 ] [ items on stock =150 ]"));
    }

    @Test
    void testCreateStoreOnLegacySystem_ZeroStock() {
        Store store = new Store("Zero Stock Store");
        store.setQuantityProductsInStock(0);

        assertDoesNotThrow(() -> gateway.createStoreOnLegacySystem(store));
        
        String output = testOut.toString();
        assertTrue(output.contains("Store created. [ name =Zero Stock Store ] [ items on stock =0 ]"));
    }

    @Test
    void testCreateStoreOnLegacySystem_NegativeStock() {
        Store store = new Store("Negative Stock Store");
        store.setQuantityProductsInStock(-50);

        assertDoesNotThrow(() -> gateway.createStoreOnLegacySystem(store));
        
        String output = testOut.toString();
        assertTrue(output.contains("Store created. [ name =Negative Stock Store ] [ items on stock =-50 ]"));
    }

    @Test
    void testCreateStoreOnLegacySystem_MaximumStock() {
        Store store = new Store("Max Stock Store");
        store.setQuantityProductsInStock(Integer.MAX_VALUE);

        assertDoesNotThrow(() -> gateway.createStoreOnLegacySystem(store));
        
        String output = testOut.toString();
        assertTrue(output.contains("Store created. [ name =Max Stock Store ] [ items on stock =" + Integer.MAX_VALUE + " ]"));
    }

    @Test
    void testCreateStoreOnLegacySystem_MinimumStock() {
        Store store = new Store("Min Stock Store");
        store.setQuantityProductsInStock(Integer.MIN_VALUE);

        assertDoesNotThrow(() -> gateway.createStoreOnLegacySystem(store));
        
        String output = testOut.toString();
        assertTrue(output.contains("Store created. [ name =Min Stock Store ] [ items on stock =" + Integer.MIN_VALUE + " ]"));
    }

    @Test
    void testCreateStoreOnLegacySystem_VeryLongName() {
        String longName = "A".repeat(1000); // Very long name
        Store store = new Store(longName);
        store.setQuantityProductsInStock(100);

        assertDoesNotThrow(() -> gateway.createStoreOnLegacySystem(store));
        
        String output = testOut.toString();
        assertTrue(output.contains("Store created. [ name =" + longName + " ] [ items on stock =100 ]"));
    }

    @Test
    void testCreateStoreOnLegacySystem_NameWithSpaces() {
        Store store = new Store("   ");
        store.setQuantityProductsInStock(25);

        assertDoesNotThrow(() -> gateway.createStoreOnLegacySystem(store));
        
        String output = testOut.toString();
        assertTrue(output.contains("Store created. [ name =   ] [ items on stock =25 ]"));
    }

    @Test
    void testUpdateStoreOnLegacySystem_SameDataAsCreate() {
        Store store = new Store("Same Data Store");
        store.setQuantityProductsInStock(300);

        // Test both create and update produce similar output
        assertDoesNotThrow(() -> gateway.createStoreOnLegacySystem(store));
        String createOutput = testOut.toString();
        testOut.reset();

        assertDoesNotThrow(() -> gateway.updateStoreOnLegacySystem(store));
        String updateOutput = testOut.toString();

        // Both should contain the same store data
        assertTrue(createOutput.contains("Store created. [ name =Same Data Store ] [ items on stock =300 ]"));
        assertTrue(updateOutput.contains("Store created. [ name =Same Data Store ] [ items on stock =300 ]"));
    }

    @Test
    void testMultipleOperations() {
        Store store1 = new Store("Store 1");
        store1.setQuantityProductsInStock(100);

        Store store2 = new Store("Store 2");
        store2.setQuantityProductsInStock(200);

        Store store3 = new Store("Store 3");
        store3.setQuantityProductsInStock(300);

        // Perform multiple operations
        assertDoesNotThrow(() -> gateway.createStoreOnLegacySystem(store1));
        assertDoesNotThrow(() -> gateway.updateStoreOnLegacySystem(store2));
        assertDoesNotThrow(() -> gateway.createStoreOnLegacySystem(store3));

        String output = testOut.toString();
        
        // Should contain all three operations
        assertTrue(output.contains("Store created. [ name =Store 1 ] [ items on stock =100 ]"));
        assertTrue(output.contains("Store created. [ name =Store 2 ] [ items on stock =200 ]"));
        assertTrue(output.contains("Store created. [ name =Store 3 ] [ items on stock =300 ]"));
        
        // Should have 3 complete file operation cycles
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
    void testFileOperationsAreTemporary() throws Exception {
        Store store = new Store("Temp File Test");
        store.setQuantityProductsInStock(123);

        // Get count of temp files before operation
        String tempDir = System.getProperty("java.io.tmpdir");
        long initialTempFileCount = Files.list(Path.of(tempDir))
            .filter(path -> path.getFileName().toString().startsWith("Temp File Test"))
            .count();

        // Perform operation
        gateway.createStoreOnLegacySystem(store);

        // Check that temp files are cleaned up
        long finalTempFileCount = Files.list(Path.of(tempDir))
            .filter(path -> path.getFileName().toString().startsWith("Temp File Test"))
            .count();

        assertEquals(initialTempFileCount, finalTempFileCount);
        
        String output = testOut.toString();
        assertTrue(output.contains("Temporary file deleted."));
    }

    @Test
    void testWriteToFileContentIntegrity() {
        Store store = new Store("Content Test");
        store.setQuantityProductsInStock(456);

        gateway.createStoreOnLegacySystem(store);

        String output = testOut.toString();
        
        // Verify the content written matches what was read back
        assertTrue(output.contains("Store created. [ name =Content Test ] [ items on stock =456 ]"));
        
        // The same content should appear in both "written" and "read" messages
        String[] lines = output.split("\n");
        String writtenLine = "";
        String readLine = "";
        
        for (String line : lines) {
            if (line.contains("Data written to temporary file.")) {
                // Get the previous line which should contain the written content
                int index = output.indexOf(line);
                writtenLine = output.substring(0, index).trim().split("\n")[output.substring(0, index).trim().split("\n").length - 1];
            }
            if (line.contains("Data read from temporary file:")) {
                readLine = line.substring("Data read from temporary file: ".length()).trim();
            }
        }
        
        // The content should be consistent
        assertTrue(writtenLine.contains("Store created. [ name =Content Test ] [ items on stock =456 ]"));
        assertTrue(readLine.contains("Store created. [ name =Content Test ] [ items on stock =456 ]"));
    }
}
