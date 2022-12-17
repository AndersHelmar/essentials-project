package dk.cloudcreate.essentials.components.foundation.messaging.queue.operations;

import dk.cloudcreate.essentials.components.foundation.messaging.queue.QueueEntryId;

/**
 * Builder for {@link DeleteMessage}
 */
public class DeleteMessageBuilder {
    private QueueEntryId queueEntryId;

    /**
     *
     * @param queueEntryId the unique id of the Message to delete
     * @return this builder instance
     */
    public DeleteMessageBuilder setQueueEntryId(QueueEntryId queueEntryId) {
        this.queueEntryId = queueEntryId;
        return this;
    }

    public DeleteMessage build() {
        return new DeleteMessage(queueEntryId);
    }
}