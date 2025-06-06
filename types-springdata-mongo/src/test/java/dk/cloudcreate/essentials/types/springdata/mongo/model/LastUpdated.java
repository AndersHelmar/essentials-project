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

package dk.cloudcreate.essentials.types.springdata.mongo.model;

import dk.cloudcreate.essentials.types.InstantType;

import java.time.*;
import java.time.temporal.ChronoField;

public class LastUpdated extends InstantType<LastUpdated> {
    public LastUpdated(Instant value) {
        super(value);
    }

    public static LastUpdated of(Instant value) {
        return new LastUpdated(value);
    }

    public static LastUpdated now() {
        // Using with(ChronoField.NANO_OF_SECOND, 0) since the MongoDB ISODate only has milliseconds resolution
        return new LastUpdated(Instant.now(Clock.systemUTC()).with(ChronoField.NANO_OF_SECOND, 0));
    }
}
