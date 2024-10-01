/*
 * Copyright 2021-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dk.cloudcreate.essentials.components.boot.autoconfigure.postgresql;

import dk.cloudcreate.essentials.components.distributed.fencedlock.postgresql.*;
import dk.cloudcreate.essentials.components.foundation.IOExceptionUtil;
import dk.cloudcreate.essentials.components.foundation.fencedlock.*;
import dk.cloudcreate.essentials.components.foundation.messaging.queue.*;
import dk.cloudcreate.essentials.components.foundation.messaging.queue.operations.ConsumeFromQueue;
import dk.cloudcreate.essentials.components.foundation.postgresql.*;
import dk.cloudcreate.essentials.components.foundation.transaction.UnitOfWork;
import dk.cloudcreate.essentials.components.queue.postgresql.PostgresqlDurableQueues;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.*;

/**
 * Properties for the Postgresql focused Essentials Components auto-configuration<br>
 * <br>
 * <u><b>Security:</b></u><br>
 * If you in your own Spring Boot application choose to override the Beans defined by this starter,
 * then you need to check the component document to learn about the Security implications of each configuration.
 * <br>
 * <u>{@link PostgresqlFencedLockManager}</u><br>
 * To support customization of {@link PostgresqlFencedLockManager} storage table name, the {@link EssentialsComponentsProperties#getFencedLockManager()}'s {@link FencedLockManagerProperties#setFencedLocksTableName(String)}
 * will be directly used in constructing SQL statements through string concatenation, which exposes the component to SQL injection attacks.<br>
 * <br>
 * It is the responsibility of the user of this component to sanitize the {@code fencedLocksTableName}
 * to ensure the security of all the SQL statements generated by this component. The {@link PostgresqlFencedLockStorage} component will
 * call the {@link PostgresqlUtil#checkIsValidTableOrColumnName(String)} method to validate the table name as a first line of defense.<br>
 * However, Essentials components as well as {@link PostgresqlUtil#checkIsValidTableOrColumnName(String)} does not offer exhaustive protection, nor does it assure the complete security of the resulting SQL against SQL injection threats.<br>
 * <b>The responsibility for implementing protective measures against SQL Injection lies exclusively with the users/developers using the Essentials components and its supporting classes.</b><br>
 * Users must ensure thorough sanitization and validation of API input parameters,  column, table, and index names.<br>
 * Insufficient attention to these practices may leave the application vulnerable to SQL injection, potentially endangering the security and integrity of the database.<br>
 * <br>
 * It is highly recommended that the {@code fencedLocksTableName} value is only derived from a controlled and trusted source.<br>
 * To mitigate the risk of SQL injection attacks, external or untrusted inputs should never directly provide the {@code fencedLocksTableName} value.<br>
 * <b>Failure to adequately sanitize and validate this value could expose the application to SQL injection
 * vulnerabilities, compromising the security and integrity of the database.</b><br>
 * <br>
 * <u>{@link PostgresqlDurableQueues}</u><br>
 * To support customization of {@link PostgresqlDurableQueues} storage table name, the {@link EssentialsComponentsProperties#getDurableQueues()}'s {@link DurableQueuesProperties#setSharedQueueTableName(String)}
 * will be directly used in constructing SQL statements through string concatenation, which exposes the component to SQL injection attacks.<br>
 * It is the responsibility of the user of this component to sanitize the {@code sharedQueueTableName}
 * to ensure the security of all the SQL statements generated by this component. The {@link PostgresqlDurableQueues} component will
 * call the {@link PostgresqlUtil#checkIsValidTableOrColumnName(String)} method to validate the table name as a first line of defense.<br>
 * The {@link PostgresqlUtil#checkIsValidTableOrColumnName(String)} provides an initial layer of defense against SQL injection by applying naming conventions intended to reduce the risk of malicious input.<br>
 * However, Essentials components as well as {@link PostgresqlUtil#checkIsValidTableOrColumnName(String)} does not offer exhaustive protection, nor does it assure the complete security of the resulting SQL against SQL injection threats.<br>
 * <b>The responsibility for implementing protective measures against SQL Injection lies exclusively with the users/developers using the Essentials components and its supporting classes.</b><br>
 * Users must ensure thorough sanitization and validation of API input parameters,  column, table, and index names.<br>
 * Insufficient attention to these practices may leave the application vulnerable to SQL injection, potentially endangering the security and integrity of the database.<br>
 * <br>
 * It is highly recommended that the {@code sharedQueueTableName} value is only derived from a controlled and trusted source.<br>
 * To mitigate the risk of SQL injection attacks, external or untrusted inputs should never directly provide the {@code sharedQueueTableName} value.<br>
 * <b>Failure to adequately sanitize and validate this value could expose the application to SQL injection
 * vulnerabilities, compromising the security and integrity of the database.</b>
 * @see dk.cloudcreate.essentials.components.queue.postgresql.PostgresqlDurableQueues
 * @see dk.cloudcreate.essentials.components.distributed.fencedlock.postgresql.PostgresqlFencedLockManager
 * @see dk.cloudcreate.essentials.components.distributed.fencedlock.postgresql.PostgresqlFencedLockStorage
 * @see MultiTableChangeListener
 */
@Configuration
@ConfigurationProperties(prefix = "essentials")
public class EssentialsComponentsProperties {
    private final FencedLockManagerProperties fencedLockManager = new FencedLockManagerProperties();
    private final DurableQueuesProperties     durableQueues     = new DurableQueuesProperties();

    private final MultiTableChangeListenerProperties multiTableChangeListener = new MultiTableChangeListenerProperties();

    private final LifeCycleProperties lifeCycles = new LifeCycleProperties();
    private final TracingProperties tracingProperties = new TracingProperties();

    private boolean             immutableJacksonModuleEnabled = true;

    /**
     * Should the EssentialsImmutableJacksonModule be included in the ObjectMapper configuration - default is true<br>
     * Setting this value to false will not include the EssentialsImmutableJacksonModule, in the ObjectMapper configuration, even if Objenesis is on the classpath
     * @return Should the EssentialsImmutableJacksonModule be included in the ObjectMapper configuration
     */
    public boolean isImmutableJacksonModuleEnabled() {
        return immutableJacksonModuleEnabled;
    }

    /**
     /**
     * Should the EssentialsImmutableJacksonModule be included in the ObjectMapper configuration - default is true<br>
     * Setting this value to false will not include the EssentialsImmutableJacksonModule, in the ObjectMapper configuration, even if Objenesis is on the classpath
     * @param immutableJacksonModuleEnabled Should the EssentialsImmutableJacksonModule be included in the ObjectMapper configuration
     */
    public void setImmutableJacksonModuleEnabled(boolean immutableJacksonModuleEnabled) {
        this.immutableJacksonModuleEnabled = immutableJacksonModuleEnabled;
    }

    public FencedLockManagerProperties getFencedLockManager() {
        return fencedLockManager;
    }

    public DurableQueuesProperties getDurableQueues() {
        return durableQueues;
    }

    public MultiTableChangeListenerProperties getMultiTableChangeListener() {
        return multiTableChangeListener;
    }

    public LifeCycleProperties getLifeCycles() {
        return this.lifeCycles;
    }

    public TracingProperties getTracingProperties() {
        return this.tracingProperties;
    }

    public static class MultiTableChangeListenerProperties {
        private Duration pollingInterval = Duration.ofMillis(50);

        /**
         * Get the interval with which the {@link MultiTableChangeListener} is polling Postgresql for notification
         *
         * @return the interval with which the {@link MultiTableChangeListener} is polling Postgresql for notification
         */
        public Duration getPollingInterval() {
            return pollingInterval;
        }

        /**
         * Set the interval with which the {@link MultiTableChangeListener} is polling Postgresql for notification
         *
         * @param pollingInterval the interval with which the {@link MultiTableChangeListener} is polling Postgresql for notification
         */
        public void setPollingInterval(Duration pollingInterval) {
            this.pollingInterval = pollingInterval;
        }
    }

    public static class DurableQueuesProperties {
        private String sharedQueueTableName = PostgresqlDurableQueues.DEFAULT_DURABLE_QUEUES_TABLE_NAME;

        private Double pollingDelayIntervalIncrementFactor = 0.5d;

        private Duration          maxPollingInterval     = Duration.ofMillis(2000);
        private TransactionalMode transactionalMode      = TransactionalMode.SingleOperationTransaction;
        private Duration          messageHandlingTimeout = Duration.ofSeconds(30);

        private boolean verboseTracing = false;

        /**
         * Should the Tracing produces only include all operations or only top level operations (default false)
         *
         * @return Should the Tracing produces only include all operations or only top level operations
         */
        public boolean isVerboseTracing() {
            return verboseTracing;
        }

        /**
         * Should the Tracing produces only include all operations or only top level operations (default false)
         *
         * @param verboseTracing Should the Tracing produces only include all operations or only top level operations
         */
        public void setVerboseTracing(boolean verboseTracing) {
            this.verboseTracing = verboseTracing;
        }

        /**
         * Get the transactional behaviour mode of the {@link PostgresqlDurableQueues}<br>
         * Default: {@link TransactionalMode#SingleOperationTransaction}
         *
         * @return the transactional behaviour mode of the {@link PostgresqlDurableQueues}
         */
        public TransactionalMode getTransactionalMode() {
            return transactionalMode;
        }

        /**
         * Set the transactional behaviour mode of the {@link PostgresqlDurableQueues}
         * Default: {@link TransactionalMode#SingleOperationTransaction}
         *
         * @param transactionalMode the transactional behaviour mode of the {@link PostgresqlDurableQueues}
         */
        public void setTransactionalMode(TransactionalMode transactionalMode) {
            this.transactionalMode = transactionalMode;
        }

        /**
         * Get the Message Handling timeout - Only relevant for {@link TransactionalMode#SingleOperationTransaction}<br>
         * The Message Handling timeout defines the timeout for messages being delivered, but haven't yet been acknowledged.
         * After this timeout the message delivery will be reset and the message will again be a candidate for delivery<br>
         * Default is 30 seconds
         *
         * @return the Message Handling timeout
         */
        public Duration getMessageHandlingTimeout() {
            return messageHandlingTimeout;
        }

        /**
         * Get the Message Handling timeout - Only relevant for {@link TransactionalMode#SingleOperationTransaction}<br>
         * The Message Handling timeout defines the timeout for messages being delivered, but haven't yet been acknowledged.
         * After this timeout the message delivery will be reset and the message will again be a candidate for delivery<br>
         * Default is 30 seconds
         *
         * @param messageHandlingTimeout the Message Handling timeout
         */
        public void setMessageHandlingTimeout(Duration messageHandlingTimeout) {
            this.messageHandlingTimeout = messageHandlingTimeout;
        }

        /**
         * Get the name of the table that will contain all messages (across all {@link QueueName}'s)<br>
         * Default is {@value PostgresqlDurableQueues#DEFAULT_DURABLE_QUEUES_TABLE_NAME}<br>
         * <br>
         * <strong>Note:</strong><br>
         * To support customization of storage table name, the {@link #getSharedQueueTableName()} will be directly used in constructing SQL statements
         * through string concatenation, which exposes the component to SQL injection attacks.<br>
         * <br>
         * <strong>Security Note:</strong><br>
         * It is the responsibility of the user of this component to sanitize the {@code sharedQueueTableName}
         * to ensure the security of all the SQL statements generated by this component. The {@link PostgresqlDurableQueues} component will
         * call the {@link PostgresqlUtil#checkIsValidTableOrColumnName(String)} method to validate the table name as a first line of defense.<br>
         * The {@link PostgresqlUtil#checkIsValidTableOrColumnName(String)} provides an initial layer of defense against SQL injection by applying naming conventions intended to reduce the risk of malicious input.<br>
         * However, Essentials components as well as {@link PostgresqlUtil#checkIsValidTableOrColumnName(String)} does not offer exhaustive protection, nor does it assure the complete security of the resulting SQL against SQL injection threats.<br>
         * <b>The responsibility for implementing protective measures against SQL Injection lies exclusively with the users/developers using the Essentials components and its supporting classes.</b><br>
         * Users must ensure thorough sanitization and validation of API input parameters,  column, table, and index names.<br>
         * Insufficient attention to these practices may leave the application vulnerable to SQL injection, potentially endangering the security and integrity of the database.<br>
         * <br>
         * It is highly recommended that the {@code sharedQueueTableName} value is only derived from a controlled and trusted source.<br>
         * To mitigate the risk of SQL injection attacks, external or untrusted inputs should never directly provide the {@code sharedQueueTableName} value.<br>
         * <b>Failure to adequately sanitize and validate this value could expose the application to SQL injection
         * vulnerabilities, compromising the security and integrity of the database.</b>
         *
         * @return the name of the table that will contain all messages (across all {@link QueueName}'s)
         */
        public String getSharedQueueTableName() {
            return sharedQueueTableName;
        }

        /**
         * Set the name of the table that will contain all messages (across all {@link QueueName}'s)<br>
         * Default is {@value PostgresqlDurableQueues#DEFAULT_DURABLE_QUEUES_TABLE_NAME}<br>
         * <br>
         * <strong>Note:</strong><br>
         * To support customization of storage table name, the {@code sharedQueueTableName} will be directly used in constructing SQL statements
         * through string concatenation, which exposes the component to SQL injection attacks.<br>
         * <br>
         * <strong>Security Note:</strong><br>
         * It is the responsibility of the user of this component to sanitize the {@code sharedQueueTableName}
         * to ensure the security of all the SQL statements generated by this component. The {@link PostgresqlDurableQueues} component will
         * call the {@link PostgresqlUtil#checkIsValidTableOrColumnName(String)} method to validate the table name as a first line of defense.<br>
         * The {@link PostgresqlUtil#checkIsValidTableOrColumnName(String)} provides an initial layer of defense against SQL injection by applying naming conventions intended to reduce the risk of malicious input.<br>
         * However, Essentials components as well as {@link PostgresqlUtil#checkIsValidTableOrColumnName(String)} does not offer exhaustive protection, nor does it assure the complete security of the resulting SQL against SQL injection threats.<br>
         * <b>The responsibility for implementing protective measures against SQL Injection lies exclusively with the users/developers using the Essentials components and its supporting classes.</b><br>
         * Users must ensure thorough sanitization and validation of API input parameters,  column, table, and index names.<br>
         * Insufficient attention to these practices may leave the application vulnerable to SQL injection, potentially endangering the security and integrity of the database.<br>
         * <br>
         * It is highly recommended that the {@code sharedQueueTableName} value is only derived from a controlled and trusted source.<br>
         * To mitigate the risk of SQL injection attacks, external or untrusted inputs should never directly provide the {@code sharedQueueTableName} value.<br>
         * <b>Failure to adequately sanitize and validate this value could expose the application to SQL injection
         * vulnerabilities, compromising the security and integrity of the database.</b>
         *
         * @param sharedQueueTableName the name of the table that will contain all messages (across all {@link QueueName}'s)
         */
        public void setSharedQueueTableName(String sharedQueueTableName) {
            this.sharedQueueTableName = sharedQueueTableName;
        }

        /**
         * When the {@link PostgresqlDurableQueues} polling returns 0 messages, what should the increase in the {@link ConsumeFromQueue#getPollingInterval()}
         * be? (logic: new_polling_interval = current_polling_interval + base_polling_interval * polling_delay_interval_increment_factor)<br>
         * Default is 0.5d<br>
         * This is used to avoid polling a the {@link DurableQueues} for a queue that isn't experiencing a lot of messages
         *
         * @return the increase in the {@link ConsumeFromQueue#getPollingInterval()} when the {@link DurableQueues} polling returns 0 messages
         */
        public Double getPollingDelayIntervalIncrementFactor() {
            return pollingDelayIntervalIncrementFactor;
        }

        /**
         * When the {@link PostgresqlDurableQueues} polling returns 0 messages, what should the increase in the {@link ConsumeFromQueue#getPollingInterval()}
         * be? (logic: new_polling_interval = current_polling_interval + base_polling_interval * polling_delay_interval_increment_factor)<br>
         * Default is 0.5d<br>
         * This is used to avoid polling a the {@link DurableQueues} for a queue that isn't experiencing a lot of messages
         *
         * @param pollingDelayIntervalIncrementFactor the increase in the {@link ConsumeFromQueue#getPollingInterval()} when the {@link DurableQueues} polling returns 0 messages
         */
        public void setPollingDelayIntervalIncrementFactor(Double pollingDelayIntervalIncrementFactor) {
            this.pollingDelayIntervalIncrementFactor = pollingDelayIntervalIncrementFactor;
        }

        /**
         * What is the maximum polling interval (when adjusted using {@link #setPollingDelayIntervalIncrementFactor(Double)})<br>
         * Default is 2 seconds
         *
         * @return What is the maximum polling interval (when adjusted using {@link #setPollingDelayIntervalIncrementFactor(Double)})
         */
        public Duration getMaxPollingInterval() {
            return maxPollingInterval;
        }

        /**
         * What is the maximum polling interval (when adjusted using {@link #setPollingDelayIntervalIncrementFactor(Double)})<br>
         * Default is 2 seconds
         *
         * @param maxPollingInterval the maximum polling interval (when adjusted using {@link #setPollingDelayIntervalIncrementFactor(Double)})
         */
        public void setMaxPollingInterval(Duration maxPollingInterval) {
            this.maxPollingInterval = maxPollingInterval;
        }
    }

    public static class FencedLockManagerProperties {
        private Duration lockTimeOut              = Duration.ofSeconds(15);
        private Duration lockConfirmationInterval = Duration.ofSeconds(4);
        private String   fencedLocksTableName     = PostgresqlFencedLockStorage.DEFAULT_FENCED_LOCKS_TABLE_NAME;
        private boolean  releaseAcquiredLocksInCaseOfIOExceptionsDuringLockConfirmation = false;

        /**
         * @return Should {@link FencedLock}'s acquired by this {@link dk.cloudcreate.essentials.components.foundation.fencedlock.FencedLockManager} be released in case calls to {@link FencedLockStorage#confirmLockInDB(DBFencedLockManager, UnitOfWork, DBFencedLock, OffsetDateTime)} fails
         * with an exception where {@link IOExceptionUtil#isIOException(Throwable)} returns true -
         * If releaseAcquiredLocksInCaseOfIOExceptionsDuringLockConfirmation is true, then {@link FencedLock}'s will be released locally,
         * otherwise we will retain the {@link FencedLock}'s as locked.
         */
        public boolean isReleaseAcquiredLocksInCaseOfIOExceptionsDuringLockConfirmation() {
            return releaseAcquiredLocksInCaseOfIOExceptionsDuringLockConfirmation;
        }

        /**
         * @param releaseAcquiredLocksInCaseOfIOExceptionsDuringLockConfirmation Should {@link FencedLock}'s acquired by this {@link dk.cloudcreate.essentials.components.foundation.fencedlock.FencedLockManager} be released in case calls to {@link FencedLockStorage#confirmLockInDB(DBFencedLockManager, UnitOfWork, DBFencedLock, OffsetDateTime)} fails
         *                                                                       with an exception where {@link IOExceptionUtil#isIOException(Throwable)} returns true -
         *                                                                       If releaseAcquiredLocksInCaseOfIOExceptionsDuringLockConfirmation is true, then {@link FencedLock}'s will be released locally,
         *                                                                       otherwise we will retain the {@link FencedLock}'s as locked.
         */
        public void setReleaseAcquiredLocksInCaseOfIOExceptionsDuringLockConfirmation(boolean releaseAcquiredLocksInCaseOfIOExceptionsDuringLockConfirmation) {
            this.releaseAcquiredLocksInCaseOfIOExceptionsDuringLockConfirmation = releaseAcquiredLocksInCaseOfIOExceptionsDuringLockConfirmation;
        }

        /**
         * Get the period between {@link FencedLock#getLockLastConfirmedTimestamp()} and the current time before the lock is marked as timed out
         *
         * @return the period between {@link FencedLock#getLockLastConfirmedTimestamp()} and the current time before the lock is marked as timed out
         */
        public Duration getLockTimeOut() {
            return lockTimeOut;
        }

        /**
         * Set the period between {@link FencedLock#getLockLastConfirmedTimestamp()} and the current time before the lock is marked as timed out
         *
         * @param lockTimeOut the period between {@link FencedLock#getLockLastConfirmedTimestamp()} and the current time before the lock is marked as timed out
         */
        public void setLockTimeOut(Duration lockTimeOut) {
            this.lockTimeOut = lockTimeOut;
        }

        /**
         * Get how often should the locks be confirmed. MUST is less than the <code>lockTimeOut</code>
         *
         * @return how often should the locks be confirmed. MUST is less than the <code>lockTimeOut</code>
         */
        public Duration getLockConfirmationInterval() {
            return lockConfirmationInterval;
        }

        /**
         * Set how often should the locks be confirmed. MUST is less than the <code>lockTimeOut</code>
         *
         * @param lockConfirmationInterval how often should the locks be confirmed. MUST is less than the <code>lockTimeOut</code>
         */
        public void setLockConfirmationInterval(Duration lockConfirmationInterval) {
            this.lockConfirmationInterval = lockConfirmationInterval;
        }

        /**
         * Get the name of the table where the fenced locks will be store - default is {@link PostgresqlFencedLockStorage#DEFAULT_FENCED_LOCKS_TABLE_NAME}<br>
         * <br>
         * <strong>Note:</strong><br>
         * To support customization of storage table name, the {@link #getFencedLocksTableName()} will be directly used in constructing SQL statements
         * through string concatenation, which exposes the component to SQL injection attacks.<br>
         * <br>
         * <strong>Security Note:</strong><br>
         * It is the responsibility of the user of this component to sanitize the {@code fencedLocksTableName}
         * to ensure the security of all the SQL statements generated by this component. The {@link PostgresqlFencedLockStorage} component will
         * call the {@link PostgresqlUtil#checkIsValidTableOrColumnName(String)} method to validate the table name as a first line of defense.<br>
         * The {@link PostgresqlUtil#checkIsValidTableOrColumnName(String)} provides an initial layer of defense against SQL injection by applying naming conventions intended to reduce the risk of malicious input.<br>
         * However, {@link PostgresqlUtil#checkIsValidTableOrColumnName(String)} does not offer exhaustive protection, nor does it assure the complete security of the resulting SQL against SQL injection threats.<br>
         * <b>The responsibility for implementing protective measures against SQL Injection lies exclusively with the users/developers using the Essentials components and its supporting classes.</b><br>
         * Users must ensure thorough sanitization and validation of API input parameters,  column, table, and index names.<br>
         * Insufficient attention to these practices may leave the application vulnerable to SQL injection, potentially endangering the security and integrity of the database.<br>
         * <br>
         * It is highly recommended that the {@code fencedLocksTableName} value is only derived from a controlled and trusted source.<br>
         * To mitigate the risk of SQL injection attacks, external or untrusted inputs should never directly provide the {@code fencedLocksTableName} value.<br>
         * <b>Failure to adequately sanitize and validate this value could expose the application to SQL injection
         * vulnerabilities, compromising the security and integrity of the database.</b><br>
         * <br>
         * If this value is set to null then {@link PostgresqlFencedLockStorage#DEFAULT_FENCED_LOCKS_TABLE_NAME}
         * will be used.
         *
         * @return the name of the table where the fenced locks will be stored - default is {@link PostgresqlFencedLockStorage#DEFAULT_FENCED_LOCKS_TABLE_NAME}
         */
        public String getFencedLocksTableName() {
            return fencedLocksTableName;
        }

        /**
         * Set the name of the table where the fenced locks will be stored<br>
         * <br>
         * <strong>Note:</strong><br>
         * To support customization of storage table name, the {@code fencedLocksTableName} will be directly used in constructing SQL statements
         * through string concatenation, which exposes the component to SQL injection attacks.<br>
         * <br>
         * <strong>Security Note:</strong><br>
         * It is the responsibility of the user of this component to sanitize the {@code fencedLocksTableName}
         * to ensure the security of all the SQL statements generated by this component. The {@link PostgresqlFencedLockStorage} component will
         * call the {@link PostgresqlUtil#checkIsValidTableOrColumnName(String)} method to validate the table name as a first line of defense.<br>
         * The {@link PostgresqlUtil#checkIsValidTableOrColumnName(String)} provides an initial layer of defense against SQL injection by applying naming conventions intended to reduce the risk of malicious input.<br>
         * However, {@link PostgresqlUtil#checkIsValidTableOrColumnName(String)} does not offer exhaustive protection, nor does it assure the complete security of the resulting SQL against SQL injection threats.<br>
         * <b>The responsibility for implementing protective measures against SQL Injection lies exclusively with the users/developers using the Essentials components and its supporting classes.</b><br>
         * Users must ensure thorough sanitization and validation of API input parameters,  column, table, and index names.<br>
         * Insufficient attention to these practices may leave the application vulnerable to SQL injection, potentially endangering the security and integrity of the database.<br>
         * <br>
         * It is highly recommended that the {@code fencedLocksTableName} value is only derived from a controlled and trusted source.<br>
         * To mitigate the risk of SQL injection attacks, external or untrusted inputs should never directly provide the {@code fencedLocksTableName} value.<br>
         * <b>Failure to adequately sanitize and validate this value could expose the application to SQL injection
         * vulnerabilities, compromising the security and integrity of the database.</b><br>
         * <br>
         * If this value is set to null then {@link PostgresqlFencedLockStorage#DEFAULT_FENCED_LOCKS_TABLE_NAME}
         * will be used.
         *
         * @param fencedLocksTableName the name of the table where the fenced locks will be stored<br>
         *                             <strong>Note:</strong><br>
         *                             To support customization of storage table name, the {@code fencedLocksTableName} will be directly used in constructing SQL statements
         *                             through string concatenation, which exposes the component to SQL injection attacks.<br>
         *                             <br>
         *                             <strong>Security Note:</strong><br>
         *                             It is the responsibility of the user of this component to sanitize the {@code fencedLocksTableName}
         *                             to ensure the security of all the SQL statements generated by this component. The {@link PostgresqlFencedLockStorage} component will
         *                             call the {@link PostgresqlUtil#checkIsValidTableOrColumnName(String)} method to validate the table name as a first line of defense.<br>
         *                             The {@link PostgresqlUtil#checkIsValidTableOrColumnName(String)} provides an initial layer of defense against SQL injection by applying naming conventions intended to reduce the risk of malicious input.<br>
         *                             However, {@link PostgresqlUtil#checkIsValidTableOrColumnName(String)} does not offer exhaustive protection, nor does it assure the complete security of the resulting SQL against SQL injection threats.<br>
         *                             <b>The responsibility for implementing protective measures against SQL Injection lies exclusively with the users/developers using the Essentials components and its supporting classes.</b><br>
         *                             Users must ensure thorough sanitization and validation of API input parameters,  column, table, and index names.<br>
         *                             Insufficient attention to these practices may leave the application vulnerable to SQL injection, potentially endangering the security and integrity of the database.<br>
         *                             <br>
         *                             It is highly recommended that the {@code fencedLocksTableName} value is only derived from a controlled and trusted source.<br>
         *                             To mitigate the risk of SQL injection attacks, external or untrusted inputs should never directly provide the {@code fencedLocksTableName} value.<br>
         *                             <b>Failure to adequately sanitize and validate this value could expose the application to SQL injection
         *                             vulnerabilities, compromising the security and integrity of the database.</b><br>
         *                             <br>
         *                             If this value is set to null then {@link PostgresqlFencedLockStorage#DEFAULT_FENCED_LOCKS_TABLE_NAME}
         *                             will be used.
         */
        public void setFencedLocksTableName(String fencedLocksTableName) {
            this.fencedLocksTableName = fencedLocksTableName;
        }
    }

    public static class LifeCycleProperties {
        private boolean startLifeCycles = true;

        /**
         * Get property that determines if lifecycle beans should be started automatically
         *
         * @return the property that determined if lifecycle beans should be started automatically
         */
        public boolean isStartLifeCycles() {
            return startLifeCycles;
        }

        /**
         * Set property that determines if lifecycle beans should be started automatically
         *
         * @param startLifeCycles the property that determines if lifecycle beans should be started automatically
         */
        public void setStartLifeCycles(boolean startLifeCycles) {
            this.startLifeCycles = startLifeCycles;
        }
    }

    public static class TracingProperties {
        private String moduleTag;

        /**
         * Get property to set as 'module' tag value for all micrometer metrics. This to differentiate metrics across different modules.
         * @return property to set as 'module' tag value for all micrometer metrics
         */
        public String getModuleTag() {
            return moduleTag;
        }

        /**
         * Set property to use as 'module' tag value for all micrometer metrics. This to differentiate metrics across different modules.
         * @param moduleTag property to set as 'module' tag value for all micrometer metrics
         */
        public void setModuleTag(String moduleTag) {
            this.moduleTag = moduleTag;
        }
    }
}
