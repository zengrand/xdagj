package io.xdag.new_libp2p.utils;

import java.util.concurrent.CompletionStage;

public interface ExceptionThrowingFutureSupplier<O> {
    CompletionStage<O> get() throws Throwable;
}

