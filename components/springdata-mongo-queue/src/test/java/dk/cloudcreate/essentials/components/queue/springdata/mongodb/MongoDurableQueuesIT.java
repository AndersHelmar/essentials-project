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

package dk.cloudcreate.essentials.components.queue.springdata.mongodb;

import dk.cloudcreate.essentials.components.foundation.json.*;
import dk.cloudcreate.essentials.components.foundation.test.messaging.queue.DurableQueuesIT;
import dk.cloudcreate.essentials.components.foundation.transaction.spring.mongo.SpringMongoTransactionAwareUnitOfWorkFactory;
import dk.cloudcreate.essentials.components.foundation.transaction.spring.mongo.SpringMongoTransactionAwareUnitOfWorkFactory.SpringMongoTransactionAwareUnitOfWork;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.*;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.*;

import static dk.cloudcreate.essentials.components.queue.springdata.mongodb.MongoDurableQueues.createDefaultObjectMapper;

@Testcontainers
@DataMongoTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class MongoDurableQueuesIT extends DurableQueuesIT<MongoDurableQueues, SpringMongoTransactionAwareUnitOfWork, SpringMongoTransactionAwareUnitOfWorkFactory> {
    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:latest");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private MongoDatabaseFactory databaseFactory;

    @Autowired
    private MongoTransactionManager transactionManager;

    @Override
    protected JSONSerializer createJSONSerializer() {
        return new JacksonJSONSerializer(createDefaultObjectMapper());
    }

    @Override
    protected MongoDurableQueues createDurableQueues(SpringMongoTransactionAwareUnitOfWorkFactory unitOfWorkFactory,
                                                     JSONSerializer jsonSerializer) {
        return new MongoDurableQueues(mongoTemplate,
                                      unitOfWorkFactory,
                                      jsonSerializer);
    }

    @Override
    protected SpringMongoTransactionAwareUnitOfWorkFactory createUnitOfWorkFactory() {
        return new SpringMongoTransactionAwareUnitOfWorkFactory(transactionManager,
                                                                databaseFactory);
    }

    @Override
    protected void resetQueueStorage(SpringMongoTransactionAwareUnitOfWorkFactory unitOfWorkFactory) {
        mongoTemplate.dropCollection(MongoDurableQueues.DEFAULT_DURABLE_QUEUES_COLLECTION_NAME);
    }
}