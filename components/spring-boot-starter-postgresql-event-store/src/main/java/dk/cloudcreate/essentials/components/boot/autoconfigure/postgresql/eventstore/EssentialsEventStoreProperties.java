/*
 * Copyright 2021-2023 the original author or authors.
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

package dk.cloudcreate.essentials.components.boot.autoconfigure.postgresql.eventstore;

import dk.cloudcreate.essentials.components.eventsourced.eventstore.postgresql.PostgresqlEventStore;
import dk.cloudcreate.essentials.components.eventsourced.eventstore.postgresql.eventstream.*;
import dk.cloudcreate.essentials.components.eventsourced.eventstore.postgresql.gap.*;
import dk.cloudcreate.essentials.components.eventsourced.eventstore.postgresql.subscription.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Properties for the Postgresql EventStore
 */
@Configuration
@ConfigurationProperties(prefix = "essentials.eventstore")
public class EssentialsEventStoreProperties {
    private IdentifierColumnType identifierColumnType     = IdentifierColumnType.TEXT;
    private JSONColumnType       jsonColumnType           = JSONColumnType.JSONB;
    private boolean              useEventStreamGapHandler = true;

    private final EventStoreSubscriptionManagerProperties subscriptionManager = new EventStoreSubscriptionManagerProperties();

    /**
     * The {@link IdentifierColumnType} used for all Aggregate-Ids
     *
     * @return The {@link IdentifierColumnType} used for all Aggregate-Ids
     */
    public IdentifierColumnType getIdentifierColumnType() {
        return identifierColumnType;
    }

    /**
     * The {@link IdentifierColumnType} used for all Aggregate-Ids
     *
     * @param identifierColumnType The {@link IdentifierColumnType} used for all Aggregate-Ids
     */
    public void setIdentifierColumnType(IdentifierColumnType identifierColumnType) {
        this.identifierColumnType = identifierColumnType;
    }

    /**
     * The {@link JSONColumnType} used for all JSON columns
     *
     * @return The {@link JSONColumnType} used for all JSON columns
     */
    public JSONColumnType getJsonColumnType() {
        return jsonColumnType;
    }

    /**
     * The {@link JSONColumnType} used for all JSON columns
     *
     * @param jsonColumnType The {@link JSONColumnType} used for all JSON columns
     */
    public void setJsonColumnType(JSONColumnType jsonColumnType) {
        this.jsonColumnType = jsonColumnType;
    }

    /**
     * Get the {@link EventStoreSubscriptionManager} properties
     *
     * @return the {@link EventStoreSubscriptionManager} properties
     */
    public EventStoreSubscriptionManagerProperties getSubscriptionManager() {
        return subscriptionManager;
    }

    /**
     * Should the {@link PostgresqlEventStore} use {@link PostgresqlEventStreamGapHandler} or the {@link NoEventStreamGapHandler}?
     *
     * @return Should the {@link PostgresqlEventStore} use {@link PostgresqlEventStreamGapHandler} or the {@link NoEventStreamGapHandler}?
     */
    public boolean isUseEventStreamGapHandler() {
        return useEventStreamGapHandler;
    }

    /**
     * Should the {@link PostgresqlEventStore} use {@link PostgresqlEventStreamGapHandler} or the {@link NoEventStreamGapHandler}?
     *
     * @param useEventStreamGapHandler Should the {@link PostgresqlEventStore} use {@link PostgresqlEventStreamGapHandler} or the {@link NoEventStreamGapHandler}?
     */
    public void setUseEventStreamGapHandler(boolean useEventStreamGapHandler) {
        this.useEventStreamGapHandler = useEventStreamGapHandler;
    }

    /**
     * {@link EventStoreSubscriptionManager} properties
     */
    public static class EventStoreSubscriptionManagerProperties {
        private int      eventStorePollingBatchSize = 10;
        private Duration eventStorePollingInterval  = Duration.ofMillis(100);
        private Duration snapshotResumePointsEvery  = Duration.ofSeconds(10);

        /**
         * How many events should The {@link dk.cloudcreate.essentials.components.eventsourced.eventstore.postgresql.EventStore} maximum return when polling for events
         *
         * @return how many events should The {@link dk.cloudcreate.essentials.components.eventsourced.eventstore.postgresql.EventStore} maximum return when polling for events
         */
        public int getEventStorePollingBatchSize() {
            return eventStorePollingBatchSize;
        }

        /**
         * How many events should The {@link dk.cloudcreate.essentials.components.eventsourced.eventstore.postgresql.EventStore} maximum return when polling for events
         *
         * @param eventStorePollingBatchSize how many events should The {@link dk.cloudcreate.essentials.components.eventsourced.eventstore.postgresql.EventStore} maximum return when polling for events
         */
        public void setEventStorePollingBatchSize(int eventStorePollingBatchSize) {
            this.eventStorePollingBatchSize = eventStorePollingBatchSize;
        }

        /**
         * How often should the {@link dk.cloudcreate.essentials.components.eventsourced.eventstore.postgresql.EventStore} be polled for new events
         *
         * @return how often should the {@link dk.cloudcreate.essentials.components.eventsourced.eventstore.postgresql.EventStore} be polled for new events
         */
        public Duration getEventStorePollingInterval() {
            return eventStorePollingInterval;
        }

        /**
         * How often should the {@link dk.cloudcreate.essentials.components.eventsourced.eventstore.postgresql.EventStore} be polled for new events
         *
         * @param eventStorePollingInterval how often should the {@link dk.cloudcreate.essentials.components.eventsourced.eventstore.postgresql.EventStore} be polled for new events
         */
        public void setEventStorePollingInterval(Duration eventStorePollingInterval) {
            this.eventStorePollingInterval = eventStorePollingInterval;
        }

        /**
         * How often should active (for exclusive subscribers this means subscribers that have acquired a distributed lock) subscribers have their {@link SubscriptionResumePoint} saved
         *
         * @return how often should active (for exclusive subscribers this means subscribers that have acquired a distributed lock) subscribers have their {@link SubscriptionResumePoint} saved
         */
        public Duration getSnapshotResumePointsEvery() {
            return snapshotResumePointsEvery;
        }

        /**
         * How often should active (for exclusive subscribers this means subscribers that have acquired a distributed lock) subscribers have their {@link SubscriptionResumePoint} saved
         *
         * @param snapshotResumePointsEvery How often should active (for exclusive subscribers this means subscribers that have acquired a distributed lock) subscribers have their {@link SubscriptionResumePoint} saved
         */
        public void setSnapshotResumePointsEvery(Duration snapshotResumePointsEvery) {
            this.snapshotResumePointsEvery = snapshotResumePointsEvery;
        }
    }
}
