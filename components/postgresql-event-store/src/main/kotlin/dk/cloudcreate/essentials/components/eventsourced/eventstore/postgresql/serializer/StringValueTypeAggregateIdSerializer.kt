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

package dk.cloudcreate.essentials.components.eventsourced.eventstore.postgresql.serializer

import dk.cloudcreate.essentials.kotlin.types.StringValueType
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

/**
 * [AggregateIdSerializer] for semantic [StringValueType]'s
 */
data class StringValueTypeAggregateIdSerializer<T : StringValueType<T>>(private val concreteType: KClass<T>) : AggregateIdSerializer {
    override fun aggregateIdType(): Class<*> {
        return concreteType.java
    }

    override fun serialize(aggregateId: Any): String {
        return (aggregateId as T).value
    }

    override fun deserialize(aggregateId: String): Any {
        return concreteType.primaryConstructor!!.call(aggregateId)
    }
}