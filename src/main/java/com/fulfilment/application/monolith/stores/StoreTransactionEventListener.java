package com.fulfilment.application.monolith.stores;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.inject.Inject;

/**
 * Event listener for store transactions.
 */
@ApplicationScoped
public class StoreTransactionEventListener {

    /** Legacy store manager gateway. */
    @Inject
    private LegacyStoreManagerGateway legacyStoreManagerGateway;

    /**
     * Handles events after transaction commit.
     *
     * @param event the store transaction event
     */
    public void afterTransactionCommit(
            @Observes(during = TransactionPhase.AFTER_COMPLETION)
            final StoreTransactionEvent event) {
        if (event.getAction() == StoreTransactionEvent.Action.CREATE) {
            legacyStoreManagerGateway.createStoreOnLegacySystem(
                event.getStore());
        } else if (event.getAction() == StoreTransactionEvent.Action.UPDATE) {
            legacyStoreManagerGateway.updateStoreOnLegacySystem(
                event.getStore());
        }
    }
}
