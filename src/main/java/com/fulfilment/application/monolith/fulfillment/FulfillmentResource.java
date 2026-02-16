package com.fulfilment.application.monolith.fulfillment;

import com.fulfilment.application.monolith.fulfillment.FulfillmentRepository;
import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/fulfillment")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FulfillmentResource {

    @Inject
    FulfillmentService fulfillmentService;

    @Inject
    FulfillmentRepository fulfillmentRepository;

    @POST
    public Response createFulfillment(FulfillmentRequest request) {
        try {
            // Convert request to domain objects (simplified for this example)
            Product product = new Product();
            product.id = request.productId;
            
            // Find the actual DbWarehouse entity
            DbWarehouse dbWarehouse = new DbWarehouse();
            dbWarehouse.businessUnitCode = request.warehouseBusinessUnitCode;
            
            Store store = new Store();
            store.id = request.storeId;

            FulfillmentAssociation association = fulfillmentService.createFulfillmentAssociation(
                product, dbWarehouse, store, request.allocatedStock, request.maxCapacity);

            return Response.status(Response.Status.CREATED).entity(association).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse(e.getMessage()))
                .build();
        }
    }

    @GET
    @Path("/product/{productId}")
    public List<FulfillmentAssociation> getFulfillmentsByProduct(@PathParam("productId") Long productId) {
        return fulfillmentService.getFulfillmentsByProduct(productId);
    }

    @GET
    @Path("/warehouse/{warehouseBusinessUnitCode}")
    public List<FulfillmentAssociation> getFulfillmentsByWarehouse(@PathParam("warehouseBusinessUnitCode") String warehouseBusinessUnitCode) {
        return fulfillmentService.getFulfillmentsByWarehouse(warehouseBusinessUnitCode);
    }

    @GET
    @Path("/store/{storeId}")
    public List<FulfillmentAssociation> getFulfillmentsByStore(@PathParam("storeId") Long storeId) {
        return fulfillmentService.getFulfillmentsByStore(storeId);
    }

    @DELETE
    @Path("/{associationId}")
    public Response deleteFulfillment(@PathParam("associationId") Long associationId) {
        fulfillmentService.deleteFulfillmentAssociation(associationId);
        return Response.noContent().build();
    }

    @DELETE
    @Path("/cleanup")
    @Transactional
    public Response cleanupTestData() {
        fulfillmentRepository.deleteAll();
        return Response.noContent().build();
    }

    // DTOs
    public static class FulfillmentRequest {
        public Long productId;
        public String warehouseBusinessUnitCode;
        public Long storeId;
        public Integer allocatedStock;
        public Integer maxCapacity;
    }

    public static class ErrorResponse {
        public String message;
        public String type = "IllegalArgumentException";

        public ErrorResponse(String message) {
            this.message = message;
        }
    }
}
