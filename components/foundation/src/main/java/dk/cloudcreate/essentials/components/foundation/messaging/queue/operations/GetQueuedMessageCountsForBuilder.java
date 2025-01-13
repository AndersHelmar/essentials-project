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

package dk.cloudcreate.essentials.components.foundation.messaging.queue.operations;

import dk.cloudcreate.essentials.components.foundation.messaging.queue.QueueName;

/**
 * Builder for {@link GetQueuedMessageCountsFor}
 */
public final class GetQueuedMessageCountsForBuilder {
    private QueueName queueName;

    /**
     *
     * @param queueName the name of the Queue where we will query for the number of queued messages
     * @return this builder instance
     */
    public GetQueuedMessageCountsForBuilder setQueueName(QueueName queueName) {
        this.queueName = queueName;
        return this;
    }

    /**
     * Builder an {@link GetQueuedMessageCountsFor} instance from the builder properties
     * @return the {@link GetQueuedMessageCountsFor} instance
     */
    public GetQueuedMessageCountsFor build() {
        return new GetQueuedMessageCountsFor(queueName);
    }
}