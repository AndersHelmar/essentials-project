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

package dk.cloudcreate.essentials.kotlin.types.jdbi

import dk.cloudcreate.essentials.kotlin.types.*
import dk.cloudcreate.essentials.shared.types.GenericType
import org.jdbi.v3.core.mapper.ColumnMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet
import java.sql.SQLException
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

/**
 * Generic [ColumnMapper] for [ZonedDateTimeValueType]'s
 *
 * @param <T> the concrete [ZonedDateTimeValueType] this instance is mapping
 */
abstract class ZonedDateTimeValueTypeColumnMapper<T : ZonedDateTimeValueType> : ColumnMapper<T?> {
    private val concreteType: KClass<T>

    constructor() {
        concreteType = (GenericType.resolveGenericTypeOnSuperClass(
            this.javaClass,
            0
        ) as Class<T>).kotlin
    }

    constructor(concreteType: KClass<T>) {
        this.concreteType = concreteType
    }

    @Throws(SQLException::class)
    override fun map(r: ResultSet, columnNumber: Int, ctx: StatementContext): T? {
        val value = r.getTimestamp(columnNumber)
        return if (value == null) null else concreteType.primaryConstructor!!.call(ZonedDateTime.ofInstant(value.toInstant(), ZoneId.systemDefault()))
    }
}
