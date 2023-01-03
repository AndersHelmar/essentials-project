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

package dk.cloudcreate.essentials.types.springdata.mongo.model;

import dk.cloudcreate.essentials.types.LocalTimeType;

import java.time.*;

public class TimeOfDay extends LocalTimeType<TimeOfDay> {
    public TimeOfDay(LocalTime value) {
        super(value);
    }

    public static TimeOfDay of(LocalTime value) {
        return new TimeOfDay(value);
    }

    public static TimeOfDay now() {
        return new TimeOfDay(LocalTime.now(Clock.systemUTC()).withNano(0));
    }
}
