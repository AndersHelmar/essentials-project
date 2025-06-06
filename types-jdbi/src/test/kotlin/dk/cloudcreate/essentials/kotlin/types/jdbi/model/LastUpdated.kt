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

package dk.cloudcreate.essentials.kotlin.types.jdbi.model

import dk.cloudcreate.essentials.kotlin.types.InstantValueType
import dk.cloudcreate.essentials.kotlin.types.jdbi.InstantValueTypeArgumentFactory
import dk.cloudcreate.essentials.kotlin.types.jdbi.InstantValueTypeColumnMapper
import java.time.Instant

@JvmInline
value class LastUpdated(override val value: Instant) : InstantValueType<LastUpdated> {
    companion object {
        fun of(value: Instant): LastUpdated {
            return LastUpdated(value)
        }

        fun now(): LastUpdated {
            return LastUpdated(Instant.now())
        }
    }
}

class LastUpdatedArgumentFactory : InstantValueTypeArgumentFactory<LastUpdated>() {
}

class LastUpdatedColumnMapper : InstantValueTypeColumnMapper<LastUpdated>() {
}
