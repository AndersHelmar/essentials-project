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

import dk.cloudcreate.essentials.components.foundation.messaging.queue.*;
import dk.cloudcreate.essentials.shared.interceptor.InterceptorChain;

import static dk.cloudcreate.essentials.shared.FailFast.requireNonNull;

/**
 * Get a queued message that's marked as a {@link QueuedMessage#isDeadLetterMessage}<br>
 * Operation also matches {@link DurableQueuesInterceptor#intercept(GetDeadLetterMessage, InterceptorChain)}
 */
public final class GetDeadLetterMessage {
    /**
     * The messages unique queue entry id
     */
    public final QueueEntryId queueEntryId;

    /**
     * Create a new builder that produces a new {@link GetDeadLetterMessage} instance
     *
     * @return a new {@link GetDeadLetterMessageBuilder} instance
     */
    public static GetDeadLetterMessageBuilder builder() {
        return new GetDeadLetterMessageBuilder();
    }

    /**
     * Get a queued message that's marked as a {@link QueuedMessage#isDeadLetterMessage}<br>
     *
     * @param queueEntryId the messages unique queue entry id
     */
    public GetDeadLetterMessage(QueueEntryId queueEntryId) {
        this.queueEntryId = requireNonNull(queueEntryId, "No queueEntryId provided");
    }

    /**
     * @return the messages unique queue entry id
     */
    public QueueEntryId getQueueEntryId() {
        return queueEntryId;
    }

    @Override
    public String toString() {
        return "GetDeadLetterMessage{" +
                "queueEntryId=" + queueEntryId +
                '}';
    }
}
