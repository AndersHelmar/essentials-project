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

package dk.cloudcreate.essentials.types.avro;

import dk.cloudcreate.essentials.types.DoubleType;
import org.apache.avro.*;

/**
 * Logical-Type for {@link dk.cloudcreate.essentials.types.DoubleType}<br>
 * <b>NOTICE:</b> This Conversion requires that AVRO field/property must be use the AVRO primitive type: <b><code>double</code></b><br>
 * <br>
 * Each concrete {@link DoubleType} that must be support Avro serialization and deserialization must have a dedicated
 * {@link Conversion} and {@link LogicalTypes.LogicalTypeFactory} pair registered with the <b><code>avro-maven-plugin</code></b>.<br>
 * <br>
 * <b>Important:</b> The AVRO field/property must be use the AVRO primitive type: <b><code>double</code></b><br>
 * <pre>{@code
 * @namespace("dk.cloudcreate.essentials.types.avro.test")
 * protocol Test {
 *   record Order {
 *       @logicalType("MyLogicalType")
 *       double  tax;
 *   }
 * }
 * }</pre>
 * <br>
 * <b>Example:Support AVRO serialization and Deserialization for a given Tax type:</b><br>
 * <b><u>1. Create the <code>TaxLogicalType</code> and <code>TaxLogicalTypeFactory</code></u></b>:<br>
 * <pre>{@code
 * package com.myproject.types.avro;
 *
 * public class TaxLogicalTypeFactory implements LogicalTypes.LogicalTypeFactory {
 *     public static final LogicalType TAX = new DoubleTypeLogicalType("Tax");
 *
 *     @Override
 *     public LogicalType fromSchema(Schema schema) {
 *         return TAX;
 *     }
 *
 *     @Override
 *     public String getTypeName() {
 *         return TAX.getName();
 *     }
 * }
 * }</pre>
 * <b><u>2. Create the <code>TaxConversion</code></u></b>:<br>
 * <pre>{@code
 * package com.myproject.types.avro;
 *
 * public class TaxConversion extends BaseDoubleTypeConversion<Tax> {
 *     @Override
 *     public Class<Tax> getConvertedType() {
 *         return Tax.class;
 *     }
 *
 *     @Override
 *     protected LogicalType getLogicalType() {
 *         return TaxLogicalTypeFactory.TAX;
 *     }
 * }
 * }</pre>
 * <b><u>3. Register the <code>TaxConversion</code> and <code>TaxLogicalTypeFactory</code> with the <code>avro-maven-plugin</code></u></b>:<br>
 * <pre>{@code
 * <plugin>
 *     <groupId>org.apache.avro</groupId>
 *     <artifactId>avro-maven-plugin</artifactId>
 *     <version>${avro.version}</version>
 *     <executions>
 *         <execution>
 *             <phase>generate-sources</phase>
 *             <goals>
 *                 <goal>idl-protocol</goal>
 *             </goals>
 *             <configuration>
 *                 <stringType>String</stringType>
 *                 <enableDecimalLogicalType>false</enableDecimalLogicalType>
 *                 <customLogicalTypeFactories>
 *                     <logicalTypeFactory>com.myproject.types.avro.TaxLogicalTypeFactory</logicalTypeFactory>
 *                 </customLogicalTypeFactories>
 *                 <customConversions>
 *                     <conversion>com.myproject.types.avro.TaxConversion</conversion>
 *                 </customConversions>
 *             </configuration>
 *         </execution>
 *     </executions>
 * </plugin>
 * }</pre>
 *
 * @see BaseDoubleTypeConversion
 */
public class DoubleTypeLogicalType extends LogicalType {
    public DoubleTypeLogicalType(String logicalTypeName) {
        super(logicalTypeName);
    }

    @Override
    public void validate(Schema schema) {
        super.validate(schema);
        if (schema.getType() != Schema.Type.DOUBLE) {
            throw new IllegalArgumentException("'" + getName() + "' can only be used with type '" + Schema.Type.DOUBLE.getName() + "'. Invalid schema: " + schema.toString(true));
        }
    }
}
