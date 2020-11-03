/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.internal.schema;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.Random;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.apache.ignite.internal.schema.NativeType.BYTE;
import static org.apache.ignite.internal.schema.NativeType.BYTES;
import static org.apache.ignite.internal.schema.NativeType.DOUBLE;
import static org.apache.ignite.internal.schema.NativeType.FLOAT;
import static org.apache.ignite.internal.schema.NativeType.INTEGER;
import static org.apache.ignite.internal.schema.NativeType.LONG;
import static org.apache.ignite.internal.schema.NativeType.SHORT;
import static org.apache.ignite.internal.schema.NativeType.STRING;
import static org.apache.ignite.internal.schema.NativeType.UUID;
import static org.junit.Assert.assertEquals;

/**
 * Tests tuple assembling and reading.
 */
public class TupleTest {
    /** */
    private Random rnd;

    /**
     */
    @Before
    public void initRandom() {
        long seed = System.currentTimeMillis();

        System.out.println("Using seed: " + seed + "L; //");

        rnd = new Random(seed);
    }

    /**
     */
    @Test
    public void testFixedSizes() {
        Column[] keyCols = new Column[] {
            new Column("keyByteCol", BYTE, true),
            new Column("keyShortCol", SHORT, true),
            new Column("keyIntCol", INTEGER, true),
            new Column("keyLongCol", LONG, true),
            new Column("keyFloatCol", FLOAT, true),
            new Column("keyDoubleCol", DOUBLE, true),
            new Column("keyUuidCol", UUID, true),
            new Column("keyBitmask1Col", Bitmask.of(4), true),
            new Column("keyBitmask2Col", Bitmask.of(22), true)
        };

        Column[] valCols = new Column[] {
            new Column("valByteCol", BYTE, true),
            new Column("valShortCol", SHORT, true),
            new Column("valIntCol", INTEGER, true),
            new Column("valLongCol", LONG, true),
            new Column("valFloatCol", FLOAT, true),
            new Column("valDoubleCol", DOUBLE, true),
            new Column("valUuidCol", UUID, true),
            new Column("valBitmask1Col", Bitmask.of(4), true),
            new Column("valBitmask2Col", Bitmask.of(22), true)
        };

        checkSchema(keyCols, valCols);
    }

    /**
     */
    @Test
    public void testVariableSizes() {
        Column[] keyCols = new Column[] {
            new Column("keyByteCol", BYTE, true),
            new Column("keyShortCol", SHORT, true),
            new Column("keyIntCol", INTEGER, true),
            new Column("keyLongCol", LONG, true),
            new Column("keyBytesCol", BYTES, true),
            new Column("keyStringCol", STRING, true),
        };

        Column[] valCols = new Column[] {
            new Column("keyByteCol", BYTE, true),
            new Column("keyShortCol", SHORT, true),
            new Column("keyIntCol", INTEGER, true),
            new Column("keyLongCol", LONG, true),
            new Column("valBytesCol", BYTES, true),
            new Column("valStringCol", STRING, true),
        };

        checkSchema(keyCols, valCols);
    }

    /**
     */
    @Test
    public void testMixedSizes() {
        Column[] keyCols = new Column[] {
            new Column("keyBytesCol", BYTES, true),
            new Column("keyStringCol", STRING, true),
        };

        Column[] valCols = new Column[] {
            new Column("valBytesCol", BYTES, true),
            new Column("valStringCol", STRING, true),
        };

        checkSchema(keyCols, valCols);
    }

    /**
     */
    private void checkSchema(Column[] keyCols, Column[] valCols) {
        checkSchemaShuffled(keyCols, valCols);

        shuffle(keyCols);
        shuffle(valCols);

        checkSchemaShuffled(keyCols, valCols);
    }

    /**
     */
    private void checkSchemaShuffled(Column[] keyCols, Column[] valCols) {
        SchemaDescriptor sch = new SchemaDescriptor(1, new Columns(keyCols), new Columns(valCols));

        Object[] checkArr = sequence(sch);

        checkValues(sch, checkArr);

        while (checkArr[0] != null) {
            int idx = 0;

            Object prev = checkArr[idx];
            checkArr[idx] = null;

            checkValues(sch, checkArr);

            while (idx < checkArr.length - 1 && checkArr[idx + 1] != null) {
                checkArr[idx] = prev;
                prev = checkArr[idx + 1];
                checkArr[idx + 1] = null;
                idx++;

                checkValues(sch, checkArr);
            }
        }
    }

    /**
     */
    private Object[] sequence(SchemaDescriptor schema) {
        Object[] res = new Object[schema.length()];

        for (int i = 0; i < res.length; i++) {
            NativeType type = schema.column(i).type();

            res[i] = generateRandomValue(type);
        }

        return res;
    }

    /**
     */
    private Object generateRandomValue(NativeType type) {
        switch (type.spec()) {
            case BYTE:
                return (byte)rnd.nextInt(255);

            case SHORT:
                return (short)rnd.nextInt(65535);

            case INTEGER:
                return rnd.nextInt();

            case LONG:
                return rnd.nextLong();

            case FLOAT:
                return rnd.nextFloat();

            case DOUBLE:
                return rnd.nextDouble();

            case UUID:
                return new java.util.UUID(rnd.nextLong(), rnd.nextLong());

            case STRING: {
                int size = rnd.nextInt(255);

                StringBuilder sb = new StringBuilder();

                while (sb.length() < size) {
                    char pt = (char)rnd.nextInt(Character.MAX_VALUE + 1);

                    if (Character.isDefined(pt) &&
                        Character.getType(pt) != Character.PRIVATE_USE &&
                        !Character.isSurrogate(pt))
                        sb.append(pt);
                }

                return sb.toString();
            }

            case BYTES: {
                int size = rnd.nextInt(255);
                byte[] data = new byte[size];
                rnd.nextBytes(data);

                return data;
            }

            case BITMASK: {
                Bitmask maskType = (Bitmask)type;

                BitSet set = new BitSet();

                for (int i = 0; i < maskType.bits(); i++) {
                    if (rnd.nextBoolean())
                        set.set(i);
                }

                return set;
            }

            default:
                throw new IllegalStateException("Unsupported type: " + type);
        }
    }

    /**
     */
    private void checkValues(SchemaDescriptor schema, Object... vals) {
        assertEquals(schema.keyColumns().length() + schema.valueColumns().length(), vals.length);

        int nonNullVarsizeKeyCols = 0;
        int nonNullVarsizeValCols = 0;
        int nonNullVarsizeKeySize = 0;
        int nonNullVarsizeValSize = 0;

        for (int i = 0; i < vals.length; i++) {
            NativeTypeSpec type = schema.column(i).type().spec();

            if (vals[i] != null && !type.fixedLength()) {
                if (type == NativeTypeSpec.BYTES) {
                    byte[] val = (byte[])vals[i];
                    if (schema.keyColumn(i)) {
                        nonNullVarsizeKeyCols++;
                        nonNullVarsizeKeySize += val.length;
                    }
                    else {
                        nonNullVarsizeValCols++;
                        nonNullVarsizeValSize += val.length;
                    }
                }
                else if (type == NativeTypeSpec.STRING) {
                    if (schema.keyColumn(i)) {
                        nonNullVarsizeKeyCols++;
                        nonNullVarsizeKeySize += TupleAssembler.utf8EncodedLength((CharSequence)vals[i]);
                    }
                    else {
                        nonNullVarsizeValCols++;
                        nonNullVarsizeValSize += TupleAssembler.utf8EncodedLength((CharSequence)vals[i]);
                    }
                }
                else
                    throw new IllegalStateException("Unsupported test varsize type: " + type);
            }
        }

        int size = TupleAssembler.tupleSize(
            schema.keyColumns(), nonNullVarsizeKeyCols, nonNullVarsizeKeySize,
            schema.valueColumns(), nonNullVarsizeValCols, nonNullVarsizeValSize);

        TupleAssembler asm = new TupleAssembler(schema, size, nonNullVarsizeKeyCols, nonNullVarsizeValCols);

        for (int i = 0; i < vals.length; i++) {
            if (vals[i] == null)
                asm.appendNull();
            else {
                NativeType type = schema.column(i).type();

                switch (type.spec()) {
                    case BYTE:
                        asm.appendByte((Byte)vals[i]);
                        break;

                    case SHORT:
                        asm.appendShort((Short)vals[i]);
                        break;

                    case INTEGER:
                        asm.appendInt((Integer)vals[i]);
                        break;

                    case LONG:
                        asm.appendLong((Long)vals[i]);
                        break;

                    case FLOAT:
                        asm.appendFloat((Float)vals[i]);
                        break;

                    case DOUBLE:
                        asm.appendDouble((Double)vals[i]);
                        break;

                    case UUID:
                        asm.appendUuid((java.util.UUID)vals[i]);
                        break;

                    case STRING:
                        asm.appendString((String)vals[i]);
                        break;

                    case BYTES:
                        asm.appendBytes((byte[])vals[i]);
                        break;

                    case BITMASK:
                        asm.appendBitmask((BitSet)vals[i]);
                        break;

                    default:
                        throw new IllegalStateException("Unsupported test type: " + type);
                }
            }
        }

        byte[] data = asm.build();

        ByteBufferTuple tup = new ByteBufferTuple(schema, data);

        for (int i = 0; i < vals.length; i++) {
            NativeTypeSpec type = schema.column(i).type().spec();

            if (type == NativeTypeSpec.BYTES)
                Assert.assertArrayEquals((byte[])vals[i], (byte[])NativeTypeSpec.BYTES.objectValue(tup, i));
            else
                Assert.assertEquals(vals[i], type.objectValue(tup, i));
        }
    }

    /**
     */
    private void shuffle(Column[] cols) {
        Collections.shuffle(Arrays.asList(cols));
    }
}
