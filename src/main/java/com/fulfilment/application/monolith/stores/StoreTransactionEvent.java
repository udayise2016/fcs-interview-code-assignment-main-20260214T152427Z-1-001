package com.fulfilment.application.monolith.stores;

/**
 * Event representing a store transaction.
 */
public class StoreTransactionEvent {
    
    /** Transaction action types. */
    public enum Action {
        /** Create action. */
        CREATE,
        /** Update action. */
        UPDATE
    }
    
    /** The store involved in the transaction. */
    private final Store store;
    /** The action performed. */
    private final Action action;
    
    /**
     * Constructs a new StoreTransactionEvent.
     *
     * @param store the store involved in the transaction
     * @param action the action performed
     */
    public StoreTransactionEvent(final Store store, final Action action) {
        this.store = store;
        this.action = action;
    }
    
    /**
     * Gets the store involved in the transaction.
     *
     * @return the store
     */
    public Store getStore() {
        return store;
    }
    
    /**
     * Gets the action performed.
     *
     * @return the action
     */
    public Action getAction() {
        return action;
    }
}
