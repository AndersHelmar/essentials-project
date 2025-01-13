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

package dk.cloudcreate.essentials.types.jdbi;

import dk.cloudcreate.essentials.shared.types.GenericType;
import dk.cloudcreate.essentials.types.*;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.*;

import static dk.cloudcreate.essentials.shared.FailFast.requireNonNull;

/**
 * Generic {@link ColumnMapper} for {@link BooleanType}'s<br>
 * <br>
 * Example of a concrete mapper:
 * <pre>{@code
 * public final class FeatureFlagEnabledArgumentFactory extends BooleanTypeArgumentFactory<FeatureFlagEnabled> {
 * }}</pre>
 *
 * @param <T> the concrete {@link BooleanType} this instance is mapping
 */
public abstract class BooleanTypeColumnMapper<T extends BooleanType<T>> implements ColumnMapper<T> {
    private final Class<T> concreteType;

    @SuppressWarnings("unchecked")
    public BooleanTypeColumnMapper() {
        concreteType = (Class<T>) GenericType.resolveGenericTypeOnSuperClass(this.getClass(),
                                                                             0);
    }

    public BooleanTypeColumnMapper(Class<T> concreteType) {
        this.concreteType = requireNonNull(concreteType, "No concreteType provided");
    }

    @Override
    public T map(ResultSet r, int columnNumber, StatementContext ctx) throws SQLException {
        var value = r.getBoolean(columnNumber);
        return SingleValueType.from(value,
                                    concreteType);
    }
}
