package io.datakitchen.ide.ui;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.EventListener;
import java.util.LinkedHashSet;
import java.util.Set;

public class EventSupport
        <ListenerType extends EventListener>
        implements InvocationHandler, Serializable {

    private final Class<ListenerType> listenerType;
    private transient Set<ListenerType> listeners;

    private transient ListenerType proxy;

    public static <ListenerType extends EventListener> EventSupport<ListenerType> of(Class<ListenerType> listenerType){
        return new EventSupport<>(listenerType);
    }


    private EventSupport(Class<ListenerType> listenerType){
        this.listenerType = listenerType;
    }

    public ListenerType getProxy(){
        if (proxy == null){
            proxy = listenerType.cast(Proxy.newProxyInstance(
                    getClass().getClassLoader(),
                    new Class[]{listenerType},this));
        }
        return proxy;
    }

    public synchronized void addListener(ListenerType listener){
        if (listeners == null){
            listeners = new LinkedHashSet<>(1);
        }
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public synchronized void removeListener(ListenerType listener){
        if (listeners != null){
            listeners.remove(listener);
        }
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        switch (methodName) {
            case "equals":
                return this.equals(args[0]);
            case "hashCode":
                return this.hashCode();
            case "toString":
                return this.toString();
        }
        if (listeners != null){
            for (ListenerType listener:listeners){
                try{
                    method.invoke(listener,args);
                }catch (Throwable ex){
                    ex.printStackTrace(System.err);
                    throw ex;
                }
            }
        }
        return null;
    }



}