package com.springdatajpa.library.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Запуск задач после успешного коммита транзакции (например отправка почты).
 */
public final class TransactionCallbacks {

    private static final Logger log = LoggerFactory.getLogger(TransactionCallbacks.class);

    private TransactionCallbacks() {
    }

    public static void runAfterCommit(Runnable task) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    task.run();
                }
            });
        } else {
            log.warn(
                    "runAfterCommit: нет активной синхронизации транзакции — задача выполняется сразу (возможен рассинхрон с коммитом БД).");
            task.run();
        }
    }
}
