package com.tuacy.library.concurrent;

import java.util.concurrent.RunnableFuture;

public interface ComparableFuture<V> extends RunnableFuture<V>, Comparable<V> {

}
