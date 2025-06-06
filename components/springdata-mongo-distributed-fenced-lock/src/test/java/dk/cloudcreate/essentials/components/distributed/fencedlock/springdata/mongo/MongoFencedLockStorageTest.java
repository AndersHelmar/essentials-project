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

package dk.cloudcreate.essentials.components.distributed.fencedlock.springdata.mongo;

import dk.cloudcreate.essentials.components.foundation.mongo.InvalidCollectionNameException;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class MongoFencedLockStorageTest {
    @Test
    void initializeWithInvalidOverriddenCollectionName() {
        assertThatThrownBy(() ->
                                   new MongoFencedLockStorage(
                                           mock(MongoTemplate.class),
                                           "system.collection"
                                   ))
                .isInstanceOf(InvalidCollectionNameException.class);

        assertThatThrownBy(() ->
                                   new MongoFencedLockStorage(
                                           mock(MongoTemplate.class),
                                           "my$_collection"
                                   ))
                .isInstanceOf(InvalidCollectionNameException.class);
        assertThatThrownBy(() ->
                                   new MongoFencedLockStorage(
                                           mock(MongoTemplate.class),
                                           "collection\0name"
                                   ))
                .isInstanceOf(InvalidCollectionNameException.class);
        assertThatThrownBy(() ->
                                   new MongoFencedLockStorage(
                                           mock(MongoTemplate.class),
                                           "Invalid Name With Spaces"
                                   ))
                .isInstanceOf(InvalidCollectionNameException.class);
    }

}