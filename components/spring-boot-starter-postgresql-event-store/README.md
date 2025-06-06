# Essentials Components - Essentials Postgresql: Spring Boot starter

This library provides Spring Boot auto-configuration for all Postgresql focused Essentials components as well as the Postgresql `EventStore`.

> **NOTE:**  
> **The library is WORK-IN-PROGRESS**

All `@Beans` auto-configured by this library use `@ConditionalOnMissingBean` to allow for easy overriding.

> Please see the **Security** notices below, as well as **Security** notices for the individual components, to familiarize yourself with the security risks  
> Such as:
> - [foundation-types](../foundation-types/README.md)
> - [postgresql-distributed-fenced-lock](../postgresql-distributed-fenced-lock/README.md)
> - [postgresql-queue](../postgresql-queue/README.md)
> - [postgresql-event-store](../postgresql-event-store/README.md)
> - [eventsourced-aggregates](../eventsourced-aggregates/README.md)

To use `spring-boot-starter-postgresql-event-store` to add the following dependency:
```
<dependency>
    <groupId>dk.cloudcreate.essentials.components</groupId>
    <artifactId>spring-boot-starter-postgresql-event-store</artifactId>
    <version>0.40.24</version>
</dependency>
```

This will ensure to include the `spring-boot-starter-postgresql` starter and its `EssentialsComponentsConfiguration` auto-configuration, so you don't need to include it as well.

## `EventStoreConfiguration` auto-configuration:

>If you in your own Spring Boot application choose to override the Beans defined by this starter,
 then you need to check the component document to learn about the Security implications of each configuration.
> - dk.cloudcreate.essentials.components.eventsourced.eventstore.postgresql.subscription.PostgresqlDurableSubscriptionRepository
> - dk.cloudcreate.essentials.components.eventsourced.eventstore.postgresql.persistence.table_per_aggregate_type.SeparateTablePerAggregateTypePersistenceStrategy
> - dk.cloudcreate.essentials.components.eventsourced.eventstore.postgresql.persistence.table_per_aggregate_type.SeparateTablePerAggregateTypeEventStreamConfigurationFactory
> - dk.cloudcreate.essentials.components.eventsourced.eventstore.postgresql.persistence.table_per_aggregate_type.SeparateTablePerAggregateEventStreamConfiguration
> - dk.cloudcreate.essentials.components.eventsourced.eventstore.postgresql.eventstream.EventStreamTableColumnNames

### Beans provided by the `EventStoreConfiguration` auto-configuration:
- `PostgresqlEventStore` using `PostgresqlEventStreamGapHandler` (using default configuration)
  - You can configure `NoEventStreamGapHandler` using Spring properties:
  - `essentials.event-store.use-event-stream-gap-handler=false`
- `SeparateTablePerAggregateTypePersistenceStrategy` using `IdentifierColumnType.TEXT` for persisting `AggregateId`'s and `JSONColumnType.JSONB` for persisting Event and EventMetadata JSON payloads
  - ColumnTypes can be overridden by using Spring properties:
  - ```
       essentials.event-store.identifier-column-type=uuid
       essentials.event-store.json-column-type=jsonb
      ```
- `EventStoreUnitOfWorkFactory` in the form of `SpringTransactionAwareEventStoreUnitOfWorkFactory`
- `EventStoreEventBus` with an internal `LocalEventBus` with bus-name `EventStoreLocalBus`
- `PersistableEventMapper` with basic setup. Override this bean if you need additional meta-data, such as event-id, event-type, event-order, event-timestamp, event-meta-data, correlation-id, tenant-id included
- `AggregateEventStreamPersistenceStrategy` in the form of `SeparateTablePerAggregateTypePersistenceStrategy` using `SeparateTablePerAggregateTypeEventStreamConfigurationFactory.standardSingleTenantConfiguration`
- `EventStoreSubscriptionManager` with default `EventStoreSubscriptionManagerProperties` values using `PostgresqlDurableSubscriptionRepository`
  - The default `EventStoreSubscriptionManager` values can be overridden using Spring properties:
  - ```
      essentials.event-store.subscription-manager.event-store-polling-batch-size=5
      essentials.event-store.subscription-manager.snapshot-resume-points-every=2s
      essentials.event-store.subscription-manager.event-store-polling-interval=200
     ```
- `MicrometerTracingEventStoreInterceptor` if property `management.tracing.enabled` has value `true`
  - The default `MicrometerTracingEventStoreInterceptor` values can be overridden using Spring properties:
  - ```
    essentials.event-store.verbose-tracing=true
    ```
- `EventProcessorDependencies` to simplify configuring `EventProcessor`'s
- `JacksonJSONEventSerializer` which uses an internally configured `ObjectMapper`, which provides good defaults for JSON serialization, and includes all Jackson `Module`'s defined in the `ApplicationContext`

## `EssentialsComponentsConfiguration` auto-configuration:

>If you in your own Spring Boot application choose to override the Beans defined by this starter,
then you need to check the component document to learn about the Security implications of each configuration, such as:
> - dk.cloudcreate.essentials.components.queue.postgresql.PostgresqlDurableQueues
> - dk.cloudcreate.essentials.components.distributed.fencedlock.postgresql.PostgresqlFencedLockManager
> - dk.cloudcreate.essentials.components.distributed.fencedlock.postgresql.PostgresqlFencedLockStorage
> - dk.cloudcreate.essentials.components.foundation.postgresql.MultiTableChangeListener

### Beans provided by the `EssentialsComponentsConfiguration` auto-configuration:
- Jackson/FasterXML JSON modules:
  - `EssentialTypesJacksonModule`
  - `EssentialsImmutableJacksonModule` (if `Objenesis` is on the classpath)
- `JacksonJSONSerializer` which uses an internally configured `ObjectMapper`, which provides good defaults for JSON serialization, and includes all Jackson `Module`'s defined in the `ApplicationContext`
  - This `JSONSerializer` will only be auto-registered if the `JSONEventSerializer` is not on the classpath (see `EventStoreConfiguration`)
- `Jdbi` to use the provided Spring `DataSource`
- `SpringTransactionAwareJdbiUnitOfWorkFactory` configured to use the Spring provided `PlatformTransactionManager`
  - This `UnitOfWorkFactory` will only be auto-registered if the `SpringTransactionAwareEventStoreUnitOfWorkFactory` is not on the classpath (see `EventStoreConfiguration`)
  - `PostgresqlFencedLockManager` using the `JSONSerializer` as JSON serializer
    - It Supports additional properties:
    - ```
      essentials.fenced-lock-manager.fenced-locks-table-name=fenced_locks
      essentials.fenced-lock-manager.lock-confirmation-interval=5s
      essentials.fenced-lock-manager.lock-time-out=12s
      essentials.fenced-lock-manager.release-acquired-locks-in-case-of-i-o-exceptions-during-lock-confirmation=false
      ```
    - **Security Notice regarding `essentials.fenced-lock-manager.fenced-locks-table-name`:**
      - This property, no matter if it's set using properties, System properties, env variables or yaml configuration, will be provided to the `PostgresqlFencedLockManager`'s `PostgresqlFencedLockStorage` as the `fencedLocksTableName` parameter.
      - To support customization of storage table name, the `essentials.fenced-lock-manager.fenced-locks-table-name` provided through the Spring configuration to the `PostgresqlFencedLockManager`'s `PostgresqlFencedLockStorage`,
        will be directly used in constructing SQL statements through string concatenation, which exposes the component to **SQL injection attacks**.
      - It is the responsibility of the user of this starter component to sanitize the `essentials.fenced-lock-manager.fenced-locks-table-name` to ensure the security of all the SQL statements generated by the `PostgresqlFencedLockManager`'s `PostgresqlFencedLockStorage`.
        - The `PostgresqlFencedLockStorage` component will call the `PostgresqlUtil#checkIsValidTableOrColumnName(String)` method to validate the table name as a first line of defense.
        - The `PostgresqlUtil#checkIsValidTableOrColumnName(String)` provides an initial layer of defense against SQL injection by applying naming conventions intended to reduce the risk of malicious input.
          - **However, Essentials components as well as `PostgresqlUtil#checkIsValidTableOrColumnName(String)` does not offer exhaustive protection, nor does it assure the complete security of the resulting SQL against SQL injection threats.**
        - The responsibility for implementing protective measures against SQL Injection lies exclusively with the users/developers using the Essentials components and its supporting classes.
        - Users must ensure thorough sanitization and validation of API input parameters,  column, table, and index names
        - **Insufficient attention to these practices may leave the application vulnerable to SQL injection, potentially endangering the security and integrity of the database.**
      - It is highly recommended that the `essentials.fenced-lock-manager.fenced-locks-table-name` value is only derived from a controlled and trusted source.
        - To mitigate the risk of SQL injection attacks, external or untrusted inputs should never directly provide the `essentials.fenced-lock-manager.fenced-locks-table-name` value.
        - **Failure to adequately sanitize and validate this value could expose the application to SQL injection  vulnerabilities, compromising the security and integrity of the database.**
- `PostgresqlDurableQueues` using the `JSONSerializer` as JSON serializer
  - Supports additional properties:
  - ```
    essentials.durable-queues.shared-queue-table-name=durable_queues 
    essentials.durable-queues.use-centralized-message-fetcher=true (default)
    essentials.durable-queues.centralized-message-fetcher-polling-interval=20ms (default)
    essentials.durable-queues.transactional-mode=fullytransactional or singleoperationtransaction (default)
    essentials.durable-queues.polling-delay-interval-increment-factor=0.5
    essentials.durable-queues.max-polling-interval=2s
    essentials.durable-queues.verbose-tracing=false
    # Only relevant if transactional-mode=singleoperationtransaction
    essentials.durable-queues.message-handling-timeout=5s
    ```
  - **Security Notice regarding `essentials.durable-queues.shared-queue-table-name`:**
    - This property, no matter if it's set using properties, System properties, env variables or yaml configuration, will be provided to the `PostgresqlDurableQueues` as the `sharedQueueTableName` parameter.
    - To support customization of storage table name, the `essentials.durable-queues.shared-queue-table-name` provided through the Spring configuration to the `PostgresqlDurableQueues`,
      will be directly used in constructing SQL statements through string concatenation, which exposes the component to **SQL injection attacks**.
    - It is the responsibility of the user of this starter component to sanitize the `essentials.durable-queues.shared-queue-table-name` to ensure the security of all the SQL statements generated by the `PostgresqlDurableQueues`.
      - The `PostgresqlDurableQueues` component will call the `PostgresqlUtil#checkIsValidTableOrColumnName(String)` method to validate the table name as a first line of defense.
      - The `PostgresqlUtil#checkIsValidTableOrColumnName(String)` provides an initial layer of defense against SQL injection by applying naming conventions intended to reduce the risk of malicious input.
        - **However, Essentials components as well as `PostgresqlUtil#checkIsValidTableOrColumnName(String)` does not offer exhaustive protection, nor does it assure the complete security of the resulting SQL against SQL injection threats.**
      - The responsibility for implementing protective measures against SQL Injection lies exclusively with the users/developers using the Essentials components and its supporting classes.
      - Users must ensure thorough sanitization and validation of API input parameters,  column, table, and index names
      - **Insufficient attention to these practices may leave the application vulnerable to SQL injection, potentially endangering the security and integrity of the database.**
    - It is highly recommended that the `essentials.durable-queues.shared-queue-table-name` value is only derived from a controlled and trusted source.
      - To mitigate the risk of SQL injection attacks, external or untrusted inputs should never directly provide the `essentials.durable-queues.shared-queue-table-name` value.
      - **Failure to adequately sanitize and validate this value could expose the application to SQL injection vulnerabilities, compromising the security and integrity of the database.**
- `Inboxes`, `Outboxes` and `DurableLocalCommandBus` configured to use `PostgresqlDurableQueues`
- `EventStoreEventBus` with bus-name `EventStoreLocalBus` and Bean name `eventBus`
  - Supports additional configuration properties:
  - ```
    essentials.reactive.event-bus-backpressure-buffer-size=1024
    essentials.reactive.overflow-max-retries=20
    essentials.reactive.queued-task-cap-factor=1.5
    #essentials.reactive.event-bus-parallel-threads=4
    #essentials.reactive.command-bus-parallel-send-and-dont-wait-consumers=4
    ```
- **Metrics:**
  - **Overview:**  
    This configuration controls the collection of performance metrics and determines the log level at which operations are reported.  
    When metrics collection is enabled for a component (such as durable queues, command bus, or message handlers), the duration of each operation is measured.
    If the duration exceeds certain thresholds, the operation is logged at the corresponding level:
    - **errorThreshold:** If the duration exceeds this value, the operation is logged at **ERROR** level.
    - **warnThreshold:** If the duration exceeds this value (but is less than the error threshold), it is logged at **WARN** level.
    - **infoThreshold:** If the duration exceeds this value (but is less than the warn threshold), it is logged at **INFO** level.
    - **debugThreshold:** If the duration exceeds this value (but is less than the info threshold), it is logged at **DEBUG** level.
    - If none of the thresholds are met and metrics collection is enabled, the operation is logged at **TRACE** level.

  - **How to Configure:**  
    Each component can be configured individually. For each component, you can:
    - Enable or disable metrics collection.
    - Set the minimum duration (using a time unit such as `ms`) for each logging level.  
      These settings allow you to fine-tune how sensitive the logging should be based on the performance characteristics you expect.

  - **YAML Example:**
    ```yaml
    # Event Store metrics configuration
    essentials:
      event-store:
        metrics:
          enabled: true
          thresholds:
            debug: 25ms    # Log at DEBUG if duration ≥ 25ms (and below the INFO threshold)
            info: 200ms    # Log at INFO if duration ≥ 200ms (and below the WARN threshold)
            warn: 500ms    # Log at WARN if duration ≥ 500ms (and below the ERROR threshold)
            error: 5000ms  # Log at ERROR if duration ≥ 5000ms
        subscription-manager:
          metrics:
            enabled: true
            thresholds:
              debug: 25ms
              info: 200ms
              warn: 500ms
              error: 5000ms

      # Other components metrics configuration
      metrics:
        durable-queues:
          enabled: true
          thresholds:
            debug: 25ms
            info: 200ms
            warn: 500ms
            error: 5000ms
        command-bus:
          enabled: true
          thresholds:
            debug: 25ms
            info: 200ms
            warn: 500ms
            error: 5000ms
        message-handler:
          enabled: true
          thresholds:
            debug: 25ms
            info: 200ms
            warn: 500ms
            error: 5000ms
    ```

  - **Properties Example:**
    ```properties
    # Event Store metrics configuration
    essentials.event-store.metrics.enabled=true
    essentials.event-store.metrics.thresholds.debug=25ms
    essentials.event-store.metrics.thresholds.info=200ms
    essentials.event-store.metrics.thresholds.warn=500ms
    essentials.event-store.metrics.thresholds.error=5000ms
    
    essentials.event-store.subscription-manager.metrics.enabled=true
    essentials.event-store.subscription-manager.metrics.thresholds.debug=25ms
    essentials.event-store.subscription-manager.metrics.thresholds.info=200ms
    essentials.event-store.subscription-manager.metrics.thresholds.warn=500ms
    essentials.event-store.subscription-manager.metrics.thresholds.error=5000ms

    # Durable Queues metrics configuration
    essentials.metrics.durable-queues.enabled=true
    essentials.metrics.durable-queues.thresholds.debug=25ms
    essentials.metrics.durable-queues.thresholds.info=200ms
    essentials.metrics.durable-queues.thresholds.warn=500ms
    essentials.metrics.durable-queues.thresholds.error=5000ms

    # Command Bus metrics configuration
    essentials.metrics.command-bus.enabled=true
    essentials.metrics.command-bus.thresholds.debug=25ms
    essentials.metrics.command-bus.thresholds.info=200ms
    essentials.metrics.command-bus.thresholds.warn=500ms
    essentials.metrics.command-bus.thresholds.error=5000ms

    # Message Handler metrics configuration
    essentials.metrics.message-handler.enabled=true
    essentials.metrics.message-handler.thresholds.debug=25ms
    essentials.metrics.message-handler.thresholds.info=200ms
    essentials.metrics.message-handler.thresholds.warn=500ms
    essentials.metrics.message-handler.thresholds.error=5000ms
    ```

  - **Adjusting Log Levels:**  
    In addition to these properties, you can control which metrics are actually written to your log files by configuring the log levels for the corresponding logger classes in your logging framework (e.g. Logback or Log4j). For example:
    - For **Event Store metrics**, adjust the log level for:
      - `dk.cloudcreate.essentials.components.eventsourced.eventstore.postgresql.interceptor.micrometer.RecordExecutionTimeEventStoreInterceptor`
      - `dk.cloudcreate.essentials.components.eventsourced.eventstore.postgresql.observability.micrometer.MeasurementEventStoreSubscriptionObserver`
    - For **Durable Queues metrics**, adjust the log level for:
      - `dk.cloudcreate.essentials.components.foundation.interceptor.micrometer.RecordExecutionTimeDurableQueueInterceptor`
    - For **Command Bus metrics**, adjust the log level for:
      - `dk.cloudcreate.essentials.components.foundation.interceptor.micrometer.RecordExecutionTimeCommandBusInterceptor`
    - For **Message Handler metrics**, adjust the log level for:
      - `dk.cloudcreate.essentials.components.foundation.interceptor.micrometer.RecordExecutionTimeMessageHandlerInterceptor`
    ```
- `ReactiveHandlersBeanPostProcessor` (for auto-registering `EventHandler` and `CommandHandler` Beans with the `EventBus`'s and `CommandBus` beans found in the `ApplicationContext`)
  - You can disable post-processing by setting: `essentials.reactive-bean-post-processor-enabled=false`
- `MultiTableChangeListener` which is used for optimizing `PostgresqlDurableQueues` message polling
  - Supports additional configuration properties:
  - ```
    essentials.multi-table-change-listener.filter-duplicate-notifications=true
    essentials.multi-table-change-listener.polling-interval=100ms
    ```
- Automatically calling `Lifecycle.start()`/`Lifecycle.stop`, on any Beans implementing the `Lifecycle` interface, when the `ApplicationContext` is started/stopped through the `DefaultLifecycleManager`
  - In addition, during `ContextRefreshedEvent`, it will call `JdbiConfigurationCallback#configure(Jdbi)` on all `ApplicationContext` Beans that implement the `JdbiConfigurationCallback` interface,
    thereby providing them with an instance of the `Jdbi` instance.
  - Notice: The `JdbiConfigurationCallback#configure(Jdbi)` will be called BEFORE any `Lifecycle` beans have been started.
  - You can disable starting `Lifecycle` Beans by using setting this property to false:
    - `essentials.life-cycles.start-life-cycles=false`
- `DurableQueuesMicrometerTracingInterceptor` and `DurableQueuesMicrometerInterceptor` if property `management.tracing.enabled` has value `true`
  - The default `DurableQueuesMicrometerTracingInterceptor` values can be overridden using Spring properties:
   -  ```
       essentials.durable-queues.verbose-tracing=true
       ```
- `DurableLocalCommandBus`
  - The `DurableLocalCommandBus` supports two different error handling concepts for true **fire-and-forget asynchronous command processing** (i.e., when `CommandBus.sendAndDontWait` is used):
    - `SendAndDontWaitErrorHandler` -  The `SendAndDontWaitErrorHandler` exception handler will handle errors that occur while processing Commands sent using `CommandBus.sendAndDontWait`.
      - If this handler doesn't rethrow the exception, then the message will not be retried by the underlying `DurableQueues`,  nor will the message be marked as a dead-letter/poison message.
      - Default it uses `SendAndDontWaitErrorHandler.RethrowingSendAndDontWaitErrorHandler`.
      - To override this configuration, you need to register a Spring bean of type `SendAndDontWaitErrorHandler`
    - `RedeliveryPolicy` which sets the `RedeliveryPolicy` used when handling queued commands sent using `CommandBus.sendAndDontWait`.
      - Default it's using `DurableLocalCommandBus.DEFAULT_REDELIVERY_POLICY`.
      - To override this, you need to register a Spring bean of type `RedeliveryPolicy`
  - Example of custom Spring configuration:
     ```
     /**
      * Custom {@link RedeliveryPolicy} used by the {@link DurableLocalCommandBus} that is autoconfigured by the springboot starter
      * @return The {@link RedeliveryPolicy} used for {@link DurableLocalCommandBusBuilder#setCommandQueueRedeliveryPolicy(RedeliveryPolicy)}
      */
     @Bean
     RedeliveryPolicy durableLocalCommandBusRedeliveryPolicy() {
         return RedeliveryPolicy.exponentialBackoff()
                                .setInitialRedeliveryDelay(Duration.ofMillis(200))
                                .setFollowupRedeliveryDelay(Duration.ofMillis(200))
                                .setFollowupRedeliveryDelayMultiplier(1.1d)
                                .setMaximumFollowupRedeliveryDelayThreshold(Duration.ofSeconds(3))
                                .setMaximumNumberOfRedeliveries(20)
                                .setDeliveryErrorHandler(
                                        MessageDeliveryErrorHandler.stopRedeliveryOn(
                                                ConstraintViolationException.class,
                                                HttpClientErrorException.BadRequest.class))
                                .build();
     }
     
     
     /**
      * Custom {@link SendAndDontWaitErrorHandler} used by the {@link DurableLocalCommandBus} that is autoconfigured by the springboot starter
      * @return The {@link SendAndDontWaitErrorHandler} used for {@link DurableLocalCommandBusBuilder#setSendAndDontWaitErrorHandler(SendAndDontWaitErrorHandler)}
      */
     @Bean
     SendAndDontWaitErrorHandler sendAndDontWaitErrorHandler() {
         return (exception, commandMessage, commandHandler) -> {
             // Example of not retrying HttpClientErrorException.Unauthorized at all -
             // if this exception is encountered then the failure is logged, but the command is never retried
             // nor marked as a dead-letter/poison message
             if (exception instanceof HttpClientErrorException.Unauthorized) {
                 log.error("Unauthorized exception", exception);
             } else {
                 Exceptions.sneakyThrow(exception);
             }
         };
     }
     ```

## Dependencies
Typical `pom.xml` dependencies required to use this starter
```
<dependencies>
    <dependency>
        <groupId>dk.cloudcreate.essentials.components</groupId>
        <artifactId>spring-boot-starter-postgresql-event-store</artifactId>
        <version>${essentials.version}</version>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-jdbc</artifactId>
        <version>${spring-boot.version}</version>
    </dependency>
    <dependency>
        <groupId>org.jdbi</groupId>
        <artifactId>jdbi3-core</artifactId>
        <version>${jdbi.version}</version>
    </dependency>
    <dependency>
        <groupId>org.jdbi</groupId>
        <artifactId>jdbi3-postgres</artifactId>
        <version>${jdbi.version}</version>
    </dependency>
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <version>${postgresql.version}</version>
    </dependency>
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
        <version>${jackson.version}</version>
    </dependency>
    <dependency>
        <groupId>com.fasterxml.jackson.datatype</groupId>
        <artifactId>jackson-datatype-jdk8</artifactId>
        <version>${jackson.version}</version>
    </dependency>
    <dependency>
        <groupId>com.fasterxml.jackson.datatype</groupId>
        <artifactId>jackson-datatype-jsr310</artifactId>
        <version>${jackson.version}</version>
    </dependency>
    <dependency>
        <groupId>io.projectreactor</groupId>
        <artifactId>reactor-core</artifactId>
        <version>${reactor.version}</version>
    </dependency>

    <!-- Test -->
    dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <version>${junit.version}</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter-engine</artifactId>
        <version>${junit.version}</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.junit.vintage</groupId>
        <artifactId>junit-vintage-engine</artifactId>
        <version>${junit.version}</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.assertj</groupId>
        <artifactId>assertj-core</artifactId>
        <version>${assertj.version}</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.awaitility</groupId>
        <artifactId>awaitility</artifactId>
        <version>${awaitility.version}</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>ch.qos.logback</groupId>
        <artifactId>logback-classic</artifactId>
        <version>${logback.version}</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>junit-jupiter</artifactId>
        <version>${testcontainers.version}</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>postgresql</artifactId>
        <version>${testcontainers.version}</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <version>${spring-boot.version}</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```
