/*
 * Copyright 2021-2025 the original author or authors.
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

package dk.cloudcreate.essentials.components.queue.postgresql;

import dk.cloudcreate.essentials.components.foundation.messaging.queue.*;
import dk.cloudcreate.essentials.components.foundation.messaging.queue.operations.ConsumeFromQueue;
import dk.cloudcreate.essentials.components.foundation.transaction.UnitOfWorkFactory;
import dk.cloudcreate.essentials.components.foundation.transaction.jdbi.*;

import java.util.function.Consumer;

@SuppressWarnings("unchecked")
public final class PostgresqlDurableQueueConsumer extends DefaultDurableQueueConsumer<PostgresqlDurableQueues, HandleAwareUnitOfWork, UnitOfWorkFactory<HandleAwareUnitOfWork>> {

    @SuppressWarnings("rawtypes")
    public PostgresqlDurableQueueConsumer(ConsumeFromQueue consumeFromQueue,
                                          HandleAwareUnitOfWorkFactory unitOfWorkFactory,
                                          PostgresqlDurableQueues durableQueues,
                                          Consumer<DurableQueueConsumer> removeDurableQueueConsumer,
                                          long pollingIntervalMs,
                                          QueuePollingOptimizer queuePollingOptimizer) {
        super(consumeFromQueue, unitOfWorkFactory, durableQueues, removeDurableQueueConsumer, pollingIntervalMs, queuePollingOptimizer);
    }
}
