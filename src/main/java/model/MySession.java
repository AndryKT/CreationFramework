package model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Objet de session expose aux Controllers de l'application.
 * Une instance est conservee dans une HttpSession et est donc partagee
 * par les requetes appartenant a la meme session HTTP.
 */
public class MySession implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Map<String, Object> data = new HashMap<>();

    public synchronized void add(String key, Object value) {
        data.put(key, value);
    }

    public synchronized Object get(String key) {
        return data.get(key);
    }

    public synchronized void remove(String key) {
        data.remove(key);
    }

    public synchronized boolean contains(String key) {
        return data.containsKey(key);
    }

    public synchronized Map<String, Object> getData() {
        return new HashMap<>(data);
    }
}
