package com.fulfilment.application.monolith.stores;

public class StoreTransactionEvent {
    
    public enum Action {
        CREATE,
        UPDATE
    }
    
    private final Store store;
    private final Action action;
    
    public StoreTransactionEvent(Store store, Action action) {
        this.store = store;
        this.action = action;
    }
    
    public Store getStore() {
        return store;
    }
    
    public Action getAction() {
        return action;
    }
}
