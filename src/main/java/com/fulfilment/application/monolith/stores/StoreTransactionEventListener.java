package com.fulfilment.application.monolith.stores;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.inject.Inject;

@ApplicationScoped
public class StoreTransactionEventListener {

    @Inject
    LegacyStoreManagerGateway legacyStoreManagerGateway;

    public void afterTransactionCommit(@Observes(during = TransactionPhase.AFTER_COMPLETION) StoreTransactionEvent event) {
        if (event.getAction() == StoreTransactionEvent.Action.CREATE) {
            legacyStoreManagerGateway.createStoreOnLegacySystem(event.getStore());
        } else if (event.getAction() == StoreTransactionEvent.Action.UPDATE) {
            legacyStoreManagerGateway.updateStoreOnLegacySystem(event.getStore());
        }
    }
}
