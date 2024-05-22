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

package dk.cloudcreate.essentials.components.eventsourced.eventstore.postgresql.persistence;

import dk.cloudcreate.essentials.components.eventsourced.eventstore.postgresql.*;
import dk.cloudcreate.essentials.components.eventsourced.eventstore.postgresql.eventstream.*;
import dk.cloudcreate.essentials.components.eventsourced.eventstore.postgresql.persistence.table_per_aggregate_type.*;
import dk.cloudcreate.essentials.components.eventsourced.eventstore.postgresql.serializer.AggregateIdSerializer;
import dk.cloudcreate.essentials.components.eventsourced.eventstore.postgresql.transaction.EventStoreUnitOfWork;
import dk.cloudcreate.essentials.components.eventsourced.eventstore.postgresql.types.GlobalEventOrder;
import dk.cloudcreate.essentials.components.foundation.postgresql.PostgresqlUtil;
import dk.cloudcreate.essentials.components.foundation.types.*;
import dk.cloudcreate.essentials.types.LongRange;

import java.util.*;
import java.util.stream.Stream;

/**
 * Represents the strategy that the {@link PostgresqlEventStore} will use to persist and load events related to a named Event Stream.<br>
 * The {@link AggregateEventStreamPersistenceStrategy} is managed by the {@link PostgresqlEventStore} and is <b>shared</b>
 * between all the {@link AggregateEventStream}'s it manages.<br>
 * <br>
 * <u><b>Security related to using it with {@link SeparateTablePerAggregateEventStreamConfiguration}:</b></u>
 * The {@link SeparateTablePerAggregateEventStreamConfiguration}'s contains the configuration for the persistence of an {@link AggregateEventStream} belonging to a given {@link AggregateType}<br>
 * <br>
 * <u>SeparateTablePerAggregateEventStreamConfiguration#eventStreamTableName</u><br>
 * The {@link SeparateTablePerAggregateEventStreamConfiguration#eventStreamTableName} defines the Postgresql table name where {@link PersistedEvent}'s related to {@link SeparateTablePerAggregateEventStreamConfiguration#aggregateType} are stored<br>
 * The {@code eventStreamTableName}'s value will be directly used in constructing SQL statements (in components using the {@link SeparateTablePerAggregateEventStreamConfiguration},
 * such as {@link SeparateTablePerAggregateTypePersistenceStrategy}) through string concatenation, which exposes the component to SQL injection attacks.<br>
 * <br>
 * <blockquote>It is the responsibility of the user of this component to sanitize the {@code eventStreamTableName}'s value
 * to ensure the security of all the SQL statements generated by components using the {@link SeparateTablePerAggregateEventStreamConfiguration}, such as the {@link SeparateTablePerAggregateTypePersistenceStrategy}.
 * </blockquote>
 * The {@link SeparateTablePerAggregateEventStreamConfiguration} component will
 * call the {@link PostgresqlUtil#checkIsValidTableOrColumnName(String)} method to validate the table name as a first line of defense.<br>
 * The {@link PostgresqlUtil#checkIsValidTableOrColumnName(String)} provides an initial layer of defense against SQL injection by applying naming conventions intended to reduce the risk of malicious input.<br>
 * However, Essentials components as well as {@link PostgresqlUtil#checkIsValidTableOrColumnName(String)} does not offer exhaustive protection, nor does it assure the complete security of the resulting SQL against SQL injection threats.<br>
 * <b>The responsibility for implementing protective measures against SQL Injection lies exclusively with the users/developers using the Essentials components and its supporting classes.</b><br>
 * Users must ensure thorough sanitization and validation of API input parameters,  column, table, and index names.<br>
 * Insufficient attention to these practices may leave the application vulnerable to SQL injection, potentially endangering the security and integrity of the database.<br>
 * <br>
 * It is highly recommended that the {@code eventStreamTableName}'s value is only derived from a controlled and trusted source.<br>
 * To mitigate the risk of SQL injection attacks, external or untrusted inputs should never directly provide the {@code eventStreamTableName}'s value.<br>
 * <b>Failure to adequately sanitize and validate this value could expose the application to SQL injection
 * vulnerabilities, compromising the security and integrity of the database.</b><br>
 * <br>
 * <u>SeparateTablePerAggregateEventStreamConfiguration#eventStreamTableColumnNames</u><br>
 * The {@link SeparateTablePerAggregateEventStreamConfiguration#eventStreamTableColumnNames} defines the columns names used for persisting AggregateEventStreams related to a specific {@link AggregateType}<br>
 * <br>
 * <b>All the column names provided</b> will be directly used in constructing SQL statements
 * through string concatenation, in components such as {@link SeparateTablePerAggregateTypePersistenceStrategy}, which exposes those components to SQL injection attacks.<br>
 * <br>
 * <blockquote>It is the responsibility of the user of this component to sanitize <b>all the column names provided</b>
 * to ensure the security of all the SQL statements generated by this component.</blockquote><br>
 * The {@link SeparateTablePerAggregateEventStreamConfiguration} component will
 * call the {@link EventStreamTableColumnNames#validate()} method, which uses {@link PostgresqlUtil#checkIsValidTableOrColumnName(String)}, to validate the column names provided as a first line of defense.<br>
 * The {@link PostgresqlUtil#checkIsValidTableOrColumnName(String)} provides an initial layer of defense against SQL injection by applying naming conventions intended to reduce the risk of malicious input.<br>
 * However, Essentials components as well as {@link PostgresqlUtil#checkIsValidTableOrColumnName(String)} does not offer exhaustive protection, nor does it assure the complete security of the resulting SQL against SQL injection threats.<br>
 * <b>The responsibility for implementing protective measures against SQL Injection lies exclusively with the users/developers using the Essentials components and its supporting classes.</b><br>
 * Users must ensure thorough sanitization and validation of API input parameters,  column, table, and index names.<br>
 * Insufficient attention to these practices may leave the application vulnerable to SQL injection, potentially endangering the security and integrity of the database.<br>
 * <br>
 * It is highly recommended that <b>all the column names provided</b> are derived from a controlled and trusted source.<br>
 * To mitigate the risk of SQL injection attacks, external or untrusted inputs should never directly provide the column name values.<br>
 * <b>Failure to adequately sanitize and validate these values could expose the application to SQL injection
 * vulnerabilities, compromising the security and integrity of the database.</b><br>
 * <br>
 * <u>AggregateType</u><br>
 * The {@link AggregateEventStreamConfiguration#aggregateType}'s value will be converted to a table name, or part of a table name, in {@link SeparateTablePerAggregateTypePersistenceStrategy},
 * and thereafter directly be used in constructing SQL statements through string concatenation, which exposes that component to SQL injection attacks.<br>
 * <br>
 * It is the responsibility of the user of this component to sanitize the {@code aggregateType}'s value
 * to ensure the security of all the SQL statements generated by this component. The {@link SeparateTablePerAggregateEventStreamConfiguration} component will
 * call the {@link PostgresqlUtil#checkIsValidTableOrColumnName(String)} method to validate the table name as a first line of defense.<br>
 * The {@link PostgresqlUtil#checkIsValidTableOrColumnName(String)} provides an initial layer of defense against SQL injection by applying naming conventions intended to reduce the risk of malicious input.<br>
 * However, Essentials components as well as {@link PostgresqlUtil#checkIsValidTableOrColumnName(String)} does not offer exhaustive protection, nor does it assure the complete security of the resulting SQL against SQL injection threats.<br>
 * <b>The responsibility for implementing protective measures against SQL Injection lies exclusively with the users/developers using the Essentials components and its supporting classes.</b><br>
 * Users must ensure thorough sanitization and validation of API input parameters,  column, table, and index names.<br>
 * Insufficient attention to these practices may leave the application vulnerable to SQL injection, potentially endangering the security and integrity of the database.<br>
 *
 * <br>
 * It is highly recommended that the {@link AggregateEventStreamConfiguration#aggregateType}'s value is only derived from a controlled and trusted source.<br>
 * To mitigate the risk of SQL injection attacks, external or untrusted inputs should never directly provide the {@link AggregateEventStreamConfiguration#aggregateType}'s value.<br>
 * <b>Failure to adequately sanitize and validate this value could expose the application to SQL injection
 * @param <CONFIG> the {@link AggregateType} configuration type
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public interface AggregateEventStreamPersistenceStrategy<CONFIG extends AggregateEventStreamConfiguration> {
    /**
     * Get the {@link AggregateEventStreamConfiguration} for this specific AggregateType
     *
     * @param aggregateType the aggregate type to get the configuration for
     * @return the aggregate type configuration
     * @throws EventStoreException if the
     */
    CONFIG getAggregateEventStreamConfiguration(AggregateType aggregateType);

    /**
     * Lookup the optional {@link AggregateEventStreamConfiguration} for the given aggregate type
     *
     * @param aggregateType the aggregate type
     * @return the {@link AggregateEventStreamConfiguration} wrapped in an {@link Optional}
     */
    Optional<CONFIG> findAggregateEventStreamConfiguration(AggregateType aggregateType);

    /**
     * Add Event stream configuration related to a specific {@link AggregateType} as indicated by {@link AggregateEventStreamConfiguration#aggregateType }<br>
     * <br>
     * <u><b>Security related to using it with {@link SeparateTablePerAggregateEventStreamConfiguration}:</b></u>
     * The {@link SeparateTablePerAggregateEventStreamConfiguration}'s contains the configuration for the persistence of an {@link AggregateEventStream} belonging to a given {@link AggregateType}<br>
     * <br>
     * <u>SeparateTablePerAggregateEventStreamConfiguration#eventStreamTableName</u><br>
     * The {@link SeparateTablePerAggregateEventStreamConfiguration#eventStreamTableName} defines the Postgresql table name where {@link PersistedEvent}'s related to {@link SeparateTablePerAggregateEventStreamConfiguration#aggregateType} are stored<br>
     * The {@code eventStreamTableName}'s value will be directly used in constructing SQL statements (in components using the {@link SeparateTablePerAggregateEventStreamConfiguration},
     * such as {@link SeparateTablePerAggregateTypePersistenceStrategy}) through string concatenation, which exposes the component to SQL injection attacks.<br>
     * <br>
     * <blockquote>It is the responsibility of the user of this component to sanitize the {@code eventStreamTableName}'s value
     * to ensure the security of all the SQL statements generated by components using the {@link SeparateTablePerAggregateEventStreamConfiguration}, such as the {@link SeparateTablePerAggregateTypePersistenceStrategy}.
     * </blockquote>
     * The {@link SeparateTablePerAggregateEventStreamConfiguration} component will
     * call the {@link PostgresqlUtil#checkIsValidTableOrColumnName(String)} method to validate the table name as a first line of defense.<br>
     * The {@link PostgresqlUtil#checkIsValidTableOrColumnName(String)} provides an initial layer of defense against SQL injection by applying naming conventions intended to reduce the risk of malicious input.<br>
     * However, Essentials components as well as {@link PostgresqlUtil#checkIsValidTableOrColumnName(String)} does not offer exhaustive protection, nor does it assure the complete security of the resulting SQL against SQL injection threats.<br>
     * <b>The responsibility for implementing protective measures against SQL Injection lies exclusively with the users/developers using the Essentials components and its supporting classes.</b><br>
     * Users must ensure thorough sanitization and validation of API input parameters,  column, table, and index names.<br>
     * Insufficient attention to these practices may leave the application vulnerable to SQL injection, potentially endangering the security and integrity of the database.<br>
     * <br>
     * It is highly recommended that the {@code eventStreamTableName}'s value is only derived from a controlled and trusted source.<br>
     * To mitigate the risk of SQL injection attacks, external or untrusted inputs should never directly provide the {@code eventStreamTableName}'s value.<br>
     * <b>Failure to adequately sanitize and validate this value could expose the application to SQL injection
     * vulnerabilities, compromising the security and integrity of the database.</b><br>
     * <br>
     * <u>SeparateTablePerAggregateEventStreamConfiguration#eventStreamTableColumnNames</u><br>
     * The {@link SeparateTablePerAggregateEventStreamConfiguration#eventStreamTableColumnNames} defines the columns names used for persisting AggregateEventStreams related to a specific {@link AggregateType}<br>
     * <br>
     * <b>All the column names provided</b> will be directly used in constructing SQL statements
     * through string concatenation, in components such as {@link SeparateTablePerAggregateTypePersistenceStrategy}, which exposes those components to SQL injection attacks.<br>
     * <br>
     * <blockquote>It is the responsibility of the user of this component to sanitize <b>all the column names provided</b>
     * to ensure the security of all the SQL statements generated by this component.</blockquote><br>
     * The {@link SeparateTablePerAggregateEventStreamConfiguration} component will
     * call the {@link EventStreamTableColumnNames#validate()} method, which uses {@link PostgresqlUtil#checkIsValidTableOrColumnName(String)}, to validate the column names provided as a first line of defense.<br>
     * The {@link PostgresqlUtil#checkIsValidTableOrColumnName(String)} provides an initial layer of defense against SQL injection by applying naming conventions intended to reduce the risk of malicious input.<br>
     * However, Essentials components as well as {@link PostgresqlUtil#checkIsValidTableOrColumnName(String)} does not offer exhaustive protection, nor does it assure the complete security of the resulting SQL against SQL injection threats.<br>
     * <b>The responsibility for implementing protective measures against SQL Injection lies exclusively with the users/developers using the Essentials components and its supporting classes.</b><br>
     * Users must ensure thorough sanitization and validation of API input parameters,  column, table, and index names.<br>
     * Insufficient attention to these practices may leave the application vulnerable to SQL injection, potentially endangering the security and integrity of the database.<br>
     * <br>
     * It is highly recommended that <b>all the column names provided</b> are derived from a controlled and trusted source.<br>
     * To mitigate the risk of SQL injection attacks, external or untrusted inputs should never directly provide the column name values.<br>
     * <b>Failure to adequately sanitize and validate these values could expose the application to SQL injection
     * vulnerabilities, compromising the security and integrity of the database.</b><br>
     * <br>
     * <u>AggregateType</u><br>
     * The {@link AggregateEventStreamConfiguration#aggregateType}'s value will be converted to a table name, or part of a table name, in {@link SeparateTablePerAggregateTypePersistenceStrategy},
     * and thereafter directly be used in constructing SQL statements through string concatenation, which exposes that component to SQL injection attacks.<br>
     * <br>
     * It is the responsibility of the user of this component to sanitize the {@code aggregateType}'s value
     * to ensure the security of all the SQL statements generated by this component. The {@link SeparateTablePerAggregateEventStreamConfiguration} component will
     * call the {@link PostgresqlUtil#checkIsValidTableOrColumnName(String)} method to validate the table name as a first line of defense.<br>
     * The {@link PostgresqlUtil#checkIsValidTableOrColumnName(String)} provides an initial layer of defense against SQL injection by applying naming conventions intended to reduce the risk of malicious input.<br>
     * However, Essentials components as well as {@link PostgresqlUtil#checkIsValidTableOrColumnName(String)} does not offer exhaustive protection, nor does it assure the complete security of the resulting SQL against SQL injection threats.<br>
     * <b>The responsibility for implementing protective measures against SQL Injection lies exclusively with the users/developers using the Essentials components and its supporting classes.</b><br>
     * Users must ensure thorough sanitization and validation of API input parameters,  column, table, and index names.<br>
     * Insufficient attention to these practices may leave the application vulnerable to SQL injection, potentially endangering the security and integrity of the database.<br>
     *
     * <br>
     * It is highly recommended that the {@link AggregateEventStreamConfiguration#aggregateType}'s value is only derived from a controlled and trusted source.<br>
     * To mitigate the risk of SQL injection attacks, external or untrusted inputs should never directly provide the {@link AggregateEventStreamConfiguration#aggregateType}'s value.<br>
     * <b>Failure to adequately sanitize and validate this value could expose the application to SQL injection
     *
     * @param eventStreamConfiguration the event stream configuration
     * @return this strategy instance
     */
    AggregateEventStreamPersistenceStrategy<CONFIG> addAggregateEventStreamConfiguration(CONFIG eventStreamConfiguration);

    /**
     * Add Event stream configuration related to a specific {@link AggregateType} and {@link AggregateIdSerializer}
     * using the default configuration provided by the encapsulated {@link AggregateEventStreamConfigurationFactory}'s
     * {@link AggregateEventStreamConfigurationFactory#createEventStreamConfigurationFor(AggregateType, AggregateIdSerializer)}
     *
     * @param aggregateType         the aggregate type for which we're creating an {@link AggregateEventStreamConfiguration}
     * @param aggregateIdSerializer the {@link AggregateIdSerializer} to use for the given {@link AggregateType}
     * @return this strategy instance
     */
    AggregateEventStreamPersistenceStrategy<CONFIG> addAggregateEventStreamConfiguration(AggregateType aggregateType,
                                                                                         AggregateIdSerializer aggregateIdSerializer);

    /**
     * Add Event stream configuration related to a specific {@link AggregateType} and {@link AggregateIdSerializer}
     * using the default configuration provided by the encapsulated {@link AggregateEventStreamConfigurationFactory}'s
     * {@link AggregateEventStreamConfigurationFactory#createEventStreamConfigurationFor(AggregateType, Class)}
     *
     * @param aggregateType   the aggregate type for which we're creating an {@link AggregateEventStreamConfiguration}
     * @param aggregateIdType the Aggregate Id type used by the provided {@link AggregateType} - calls {@link AggregateIdSerializer#serializerFor(Class)}
     * @return this strategy instance
     */
    AggregateEventStreamPersistenceStrategy<CONFIG> addAggregateEventStreamConfiguration(AggregateType aggregateType,
                                                                                         Class<?> aggregateIdType);


    /**
     * Persist a List of persistable events
     *
     * @param unitOfWork                  the current unitOfWork
     * @param aggregateType               the aggregate type that the underlying {@link AggregateEventStream} is associated with
     * @param aggregateId                 The id of the aggregate identifier (aka the stream id) that the <code>persistableEvents</code> are related to
     * @param appendEventsAfterEventOrder the {@link PersistedEvent#eventOrder()} of the last event persisted related to the given <code>aggregateId</code>.
     *                                    This means that events in <code>persistableEvents</code> will receive {@link PersistedEvent#eventOrder()} starting from and including <code>appendEventsAfterEventOrder + 1</code>
     * @param persistableEvents           the list of persistable events (i.e. events that haven't yet been persisted)
     * @return the {@link PersistedEvent}'s - each one corresponds 1-1 and IN-ORDER with the <code>persistableEvents</code>
     */
    <STREAM_ID> AggregateEventStream<STREAM_ID> persist(EventStoreUnitOfWork unitOfWork, AggregateType aggregateType, STREAM_ID aggregateId, Optional<Long> appendEventsAfterEventOrder, List<?> persistableEvents);

    /**
     * Load the last {@link PersistedEvent} in relation to the specified <code>configuration</code> and <code>aggregateId</code>
     *
     * @param aggregateId   the identifier of the aggregate we want to find the last {@link PersistedEvent}
     * @param aggregateType the aggregate type that the underlying {@link AggregateEventStream} is associated with
     * @param <STREAM_ID>   the id type for the aggregate
     * @return an {@link Optional} with the last {@link PersistedEvent} related to the <code>aggregateId</code> instance or
     * {@link Optional#empty()} if no events have been persisted before
     */
    <STREAM_ID> Optional<PersistedEvent> loadLastPersistedEventRelatedTo(EventStoreUnitOfWork unitOfWork, AggregateType aggregateType, STREAM_ID aggregateId);

    /**
     * Load all events related to the given <code>configuration</code>, sharing the same <code>aggregateId</code> and having a {@link PersistedEvent#eventOrder()} within the <code>eventOrderRange</code>
     *
     * @param unitOfWork                            the current unit of work
     * @param aggregateType                         the aggregate type that the underlying {@link AggregateEventStream} is associated with
     * @param aggregateId                           The id of the aggregate identifier (aka the stream id) that the events is related to
     * @param eventOrderRange                       the range of {@link PersistedEvent#globalEventOrder()}'s we want Events for
     * @param onlyIncludeEventsIfTheyBelongToTenant Matching events will only be returned if the {@link PersistedEvent#tenant()} matches
     *                                              the given tenant specified in this parameter OR if the Event doesn't specify ANY tenant
     * @return the {@link PersistedEvent} related to the event stream's
     */
    <STREAM_ID> Optional<AggregateEventStream<STREAM_ID>> loadAggregateEvents(EventStoreUnitOfWork unitOfWork, AggregateType aggregateType, STREAM_ID aggregateId, LongRange eventOrderRange, Optional<Tenant> onlyIncludeEventsIfTheyBelongToTenant);

    /**
     * Load all events related to the given <code>configuration</code> and having a {@link PersistedEvent#globalEventOrder()} within the <code>globalOrderRange</code>
     *
     * @param unitOfWork                            the current unit of work
     * @param aggregateType                         the aggregate type that the underlying {@link AggregateEventStream} is associated with
     * @param globalOrderRange                      the range of {@link PersistedEvent#globalEventOrder()}'s we want Events for
     * @param includeAdditionalGlobalOrders         a list of additional global orders (typically outside the <code>globalOrderRange</code>) that you want to include additionally<br>
     *                                              May be null or empty if no additional events should be loaded outside the <code>globalOrderRange</code>
     * @param onlyIncludeEventsIfTheyBelongToTenant Matching events will only be returned if the {@link PersistedEvent#tenant()} matches
     *                                              the given tenant specified in this parameter OR if the Event doesn't specify ANY tenant
     * @return the {@link PersistedEvent}'s
     */
    Stream<PersistedEvent> loadEventsByGlobalOrder(EventStoreUnitOfWork unitOfWork, AggregateType aggregateType, LongRange globalOrderRange, List<GlobalEventOrder> includeAdditionalGlobalOrders, Optional<Tenant> onlyIncludeEventsIfTheyBelongToTenant);

    /**
     * Load the event belonging to the given <code>configuration</code> and having the specified <code>eventId</code>
     *
     * @param unitOfWork    the current unit of work
     * @param aggregateType the aggregate type that the underlying {@link AggregateEventStream} is associated with
     * @param eventId       the identifier of the {@link PersistedEvent}
     * @return an {@link Optional} with the {@link PersistedEvent} or {@link Optional#empty()} if the event couldn't be found
     */
    Optional<PersistedEvent> loadEvent(EventStoreUnitOfWork unitOfWork, AggregateType aggregateType, EventId eventId);

    /**
     * Find the highest {@link GlobalEventOrder} persisted in relation to the given aggregateType
     *
     * @param unitOfWork    the current unit of work
     * @param aggregateType the aggregate type that the underlying {@link AggregateEventStream} is associated with
     * @return an {@link Optional} with the {@link GlobalEventOrder} persisted or {@link Optional#empty()} if no events have been persisted
     */
    Optional<GlobalEventOrder> findHighestGlobalEventOrderPersisted(EventStoreUnitOfWork unitOfWork, AggregateType aggregateType);

    /**
     * Load all the <code>eventIds</code> related to the specified <code>aggregateType</code>
     * @param unitOfWork the current unit of work
     * @param aggregateType the aggregate type that the specified <code>eventIds</code> are associated with
     * @param eventIds the {@link EventId}'s that we want to load the corresponding {@link PersistedEvent}'s for
     * @return the matching {@link PersistedEvent}'s
     */
    List<PersistedEvent> loadEvents(EventStoreUnitOfWork unitOfWork, AggregateType aggregateType, List<EventId> eventIds);
}
