package com.br.vtspp.adapter;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

final public class ReplaceValue<K, V> {
    private final K targetKey;
    private Map<K, V> map;
    private boolean wasReplaced;

    private ReplaceValue(K targetKey, Map<K, V> map) {
        this.targetKey = targetKey;
        this.map = map;
    }

    public static <K, V> ReplaceValue<K, V> createNewInstance(K targetKey, Map<K, V> map) {
        return new ReplaceValue<>(targetKey, map);
    }

    public ReplaceValue<K, V> replaceByFunction(Function<V, V> function) {
        replaceTargetKey(m -> m.replace(targetKey, function.apply(m.get(targetKey))));
        return this;
    }

    public boolean wasReplaced() {
        return this.wasReplaced;
    }

    private void replaceTargetKey(Consumer<Map<K, V>> consumer) {
        this.map.forEach((k, v) -> {
            if (k.equals(targetKey)) {
                consumer.accept(this.map);
                this.wasReplaced = true;
            } else if (v instanceof Map<?, ?>) {
                this.map = (Map<K, V>) v;
                replaceTargetKey(consumer);
            }
        });
    }
}
