package com.uninaswap.client.service;

import javafx.application.Platform;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple event bus to decouple components in the application.
 * Components can subscribe to events and be notified when those events occur.
 */
public class EventBusService {
    private static EventBusService instance;
    
    private final Map<String, CopyOnWriteArrayList<Consumer<Object>>> subscribers = new ConcurrentHashMap<>();
    
    private EventBusService() {
    }
    
    public static synchronized EventBusService getInstance() {
        if (instance == null) {
            instance = new EventBusService();
        }
        return instance;
    }
    
    /**
     * Subscribe to an event.
     * 
     * @param eventType The type of event to subscribe to
     * @param handler The handler to call when the event occurs
     * @return A subscription ID that can be used to unsubscribe
     */
    public void subscribe(String eventType, Consumer<Object> handler) {
        subscribers.computeIfAbsent(eventType, _ -> new CopyOnWriteArrayList<>())
                  .add(handler);
    }
    
    /**
     * Unsubscribe from an event.
     * 
     * @param eventType The type of event to unsubscribe from
     * @param handler The handler to unsubscribe
     */
    public void unsubscribe(String eventType, Consumer<Object> handler) {
        if (subscribers.containsKey(eventType)) {
            subscribers.get(eventType).remove(handler);
        }
    }
    
    /**
     * Publish an event to all subscribers.
     * Will run on the JavaFX thread if not already on it.
     * 
     * @param eventType The type of event to publish
     * @param data The data to send with the event
     */
    public void publishEvent(String eventType, Object data) {
        if (subscribers.containsKey(eventType)) {
            for (Consumer<Object> handler : subscribers.get(eventType)) {
                // Make sure we run on the JavaFX application thread if handlers update UI
                if (Platform.isFxApplicationThread()) {
                    handler.accept(data);
                } else {
                    Platform.runLater(() -> handler.accept(data));
                }
            }
        }
    }
    
    /**
     * Clear all subscriptions for a specific event type
     */
    public void clearSubscriptions(String eventType) {
        subscribers.remove(eventType);
    }
    
    /**
     * Clear all subscriptions
     */
    public void clearAllSubscriptions() {
        subscribers.clear();
    }
    
    /**
     * Clears all subscribers for a specific controller class
     * Used when controllers are destroyed (e.g., during logout)
     * 
     * @param controllerClass The class of the controller being destroyed
     */
    public void clearSubscriptionsForController(Class<?> controllerClass) {
        for (String eventType : new ArrayList<>(subscribers.keySet())) {
            List<Consumer<Object>> handlers = subscribers.get(eventType);
            if (handlers != null) {
                List<Consumer<Object>> copy = new ArrayList<>(handlers);
                for (Consumer<Object> handler : copy) {
                    if (handler.toString().contains(controllerClass.getName())) {
                        unsubscribe(eventType, handler);
                    }
                }
            }
        }
    }
}