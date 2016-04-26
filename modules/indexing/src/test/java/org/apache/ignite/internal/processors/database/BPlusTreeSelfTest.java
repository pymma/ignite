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

package org.apache.ignite.internal.processors.database;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.apache.ignite.IgniteCheckedException;
import org.apache.ignite.internal.mem.unsafe.UnsafeMemoryProvider;
import org.apache.ignite.internal.pagemem.FullPageId;
import org.apache.ignite.internal.pagemem.PageIdAllocator;
import org.apache.ignite.internal.pagemem.PageMemory;
import org.apache.ignite.internal.pagemem.impl.PageMemoryImpl;
import org.apache.ignite.internal.processors.cache.database.tree.BPlusTree;
import org.apache.ignite.internal.processors.cache.database.tree.io.BPlusIO;
import org.apache.ignite.internal.processors.cache.database.tree.io.BPlusInnerIO;
import org.apache.ignite.internal.processors.cache.database.tree.io.BPlusLeafIO;
import org.apache.ignite.internal.processors.cache.database.tree.reuse.ReuseList;
import org.apache.ignite.internal.util.lang.GridCursor;
import org.apache.ignite.internal.util.typedef.X;
import org.apache.ignite.testframework.junits.common.GridCommonAbstractTest;

/**
 */
public class BPlusTreeSelfTest extends GridCommonAbstractTest {
    /** */
    private static final short LONG_INNER_IO = 30000;

    /** */
    private static final short LONG_LEAF_IO = 30001;

    /** */
    private static final int PAGE_SIZE = 256;

    /** */
    private static final long MB = 1024 * 1024;

    /** */
    private static final int CPUS = Runtime.getRuntime().availableProcessors();

    /** */
    private static final int CACHE_ID = 100500;

    /** */
    private static int MAX_ITEMS_COUNT = 0;

    /** */
    private static int CNT = 10;

    /** */
    private static int PUT_INC = 1;

    /** */
    private static int RMV_INC = 1;

    /** */
    private PageMemory pageMem;

    /** */
    private ReuseList reuseList;

    @Override
    protected long getTestTimeout() {
        return 25 * 60 * 1000;
    }

    /** {@inheritDoc} */
    @Override protected void beforeTest() throws Exception {
        long seed = System.nanoTime();

        X.println("Test seed: " + seed);

        TestTree.rnd = new Random(seed);

        pageMem = new PageMemoryImpl(log, new UnsafeMemoryProvider(64 * MB, 32 * MB), null, PAGE_SIZE, CPUS);

        pageMem.start();

        reuseList = null;
//            new ReuseList(CACHE_ID, pageMem, 2, new MetaStore() {
//            @Override public IgniteBiTuple<FullPageId,Boolean> getOrAllocateForIndex(int cacheId, String idxName)
//                throws IgniteCheckedException {
//                return new T2<>(allocatePage(), true);
//            }
//        });
    }

    /** {@inheritDoc} */
    @Override protected void afterTest() throws Exception {
        pageMem.stop();

        MAX_ITEMS_COUNT = 0;
        PUT_INC = 1;
        RMV_INC = -1;
        CNT = 10;
    }

    /**
     * @throws IgniteCheckedException If failed.
     */
    public void testPutRemove_1_20_mm_1() throws IgniteCheckedException {
        MAX_ITEMS_COUNT = 1;
        CNT = 20;
        PUT_INC = -1;
        RMV_INC = -1;

        doTestPutRemove(true);
    }

    /**
     * @throws IgniteCheckedException If failed.
     */
    public void testPutRemove_1_20_mm_0() throws IgniteCheckedException {
        MAX_ITEMS_COUNT = 1;
        CNT = 20;
        PUT_INC = -1;
        RMV_INC = -1;

        doTestPutRemove(false);
    }

    /**
     * @throws IgniteCheckedException If failed.
     */
    public void testPutRemove_1_20_pm_1() throws IgniteCheckedException {
        MAX_ITEMS_COUNT = 1;
        CNT = 20;
        PUT_INC = 1;
        RMV_INC = -1;

        doTestPutRemove(true);
    }

    /**
     * @throws IgniteCheckedException If failed.
     */
    public void testPutRemove_1_20_pm_0() throws IgniteCheckedException {
        MAX_ITEMS_COUNT = 1;
        CNT = 20;
        PUT_INC = 1;
        RMV_INC = -1;

        doTestPutRemove(false);
    }

    /**
     * @throws IgniteCheckedException If failed.
     */
    public void testPutRemove_1_20_pp_1() throws IgniteCheckedException {
        MAX_ITEMS_COUNT = 1;
        CNT = 20;
        PUT_INC = 1;
        RMV_INC = 1;

        doTestPutRemove(true);
    }

    /**
     * @throws IgniteCheckedException If failed.
     */
    public void testPutRemove_1_20_pp_0() throws IgniteCheckedException {
        MAX_ITEMS_COUNT = 1;
        CNT = 20;
        PUT_INC = 1;
        RMV_INC = 1;

        doTestPutRemove(false);
    }

    /**
     * @throws IgniteCheckedException If failed.
     */
    public void testPutRemove_1_20_mp_1() throws IgniteCheckedException {
        MAX_ITEMS_COUNT = 1;
        CNT = 20;
        PUT_INC = -1;
        RMV_INC = 1;

        doTestPutRemove(true);
    }

    /**
     * @throws IgniteCheckedException If failed.
     */
    public void testPutRemove_1_20_mp_0() throws IgniteCheckedException {
        MAX_ITEMS_COUNT = 1;
        CNT = 20;
        PUT_INC = -1;
        RMV_INC = 1;

        doTestPutRemove(false);
    }

    // ------- 2 - 40
    /**
     * @throws IgniteCheckedException If failed.
     */
    public void testPutRemove_2_40_mm_1() throws IgniteCheckedException {
        MAX_ITEMS_COUNT = 2;
        CNT = 40;
        PUT_INC = -1;
        RMV_INC = -1;

        doTestPutRemove(true);
    }

    /**
     * @throws IgniteCheckedException If failed.
     */
    public void testPutRemove_2_40_mm_0() throws IgniteCheckedException {
        MAX_ITEMS_COUNT = 2;
        CNT = 40;
        PUT_INC = -1;
        RMV_INC = -1;

        doTestPutRemove(false);
    }

    /**
     * @throws IgniteCheckedException If failed.
     */
    public void testPutRemove_2_40_pm_1() throws IgniteCheckedException {
        MAX_ITEMS_COUNT = 2;
        CNT = 40;
        PUT_INC = 1;
        RMV_INC = -1;

        doTestPutRemove(true);
    }

    /**
     * @throws IgniteCheckedException If failed.
     */
    public void testPutRemove_2_40_pm_0() throws IgniteCheckedException {
        MAX_ITEMS_COUNT = 2;
        CNT = 40;
        PUT_INC = 1;
        RMV_INC = -1;

        doTestPutRemove(false);
    }

    /**
     * @throws IgniteCheckedException If failed.
     */
    public void testPutRemove_2_40_pp_1() throws IgniteCheckedException {
        MAX_ITEMS_COUNT = 2;
        CNT = 40;
        PUT_INC = 1;
        RMV_INC = 1;

        doTestPutRemove(true);
    }

    /**
     * @throws IgniteCheckedException If failed.
     */
    public void testPutRemove_2_40_pp_0() throws IgniteCheckedException {
        MAX_ITEMS_COUNT = 2;
        CNT = 40;
        PUT_INC = 1;
        RMV_INC = 1;

        doTestPutRemove(false);
    }

    /**
     * @throws IgniteCheckedException If failed.
     */
    public void testPutRemove_2_40_mp_1() throws IgniteCheckedException {
        MAX_ITEMS_COUNT = 2;
        CNT = 40;
        PUT_INC = -1;
        RMV_INC = 1;

        doTestPutRemove(true);
    }

    /**
     * @throws IgniteCheckedException If failed.
     */
    public void testPutRemove_2_40_mp_0() throws IgniteCheckedException {
        MAX_ITEMS_COUNT = 2;
        CNT = 40;
        PUT_INC = -1;
        RMV_INC = 1;

        doTestPutRemove(false);
    }

    /**
     * @param canGetRow Can get row from inner page.
     * @throws IgniteCheckedException If failed.
     */
    private void doTestPutRemove(boolean canGetRow) throws IgniteCheckedException {
        TestTree tree = createTestTree(canGetRow);

        long cnt = CNT;

        for (long x = PUT_INC > 0 ? 0 : cnt - 1; x >= 0 && x < cnt; x += PUT_INC) {
            assertNull(tree.findOne(x));

            tree.put(x);

            assertEquals(x, tree.findOne(x).longValue());
        }

        X.println(tree.printTree());

        assertNull(tree.findOne(-1L));

        for (long x = 0; x < cnt; x++)
            assertEquals(x, tree.findOne(x).longValue());

        assertNull(tree.findOne(cnt));

        for (long x = RMV_INC > 0 ? 0 : cnt - 1; x >= 0 && x < cnt; x += RMV_INC) {
            X.println(" -- " + x);

            assertEquals(x, tree.remove(x).longValue());

            X.println(tree.printTree());

            assertNull(tree.findOne(x));
        }

        assertFalse(tree.find(null, null).next());
    }

    /**
     * @throws IgniteCheckedException If failed.
     */
    public void _testRandomRemove0() throws IgniteCheckedException {
        // seed: 1461177795261173000, 1461187841179332000
        doTestRandomRemove(false);
    }

    /**
     * @throws IgniteCheckedException If failed.
     */
    public void _testRandomRemove1() throws IgniteCheckedException {
        // seed: 1461188744119034000 1461188844311788000 1461189099834526000
        doTestRandomRemove(true);
    }

    /**
     * @param canGetRow Can get row from inner page.
     * @throws IgniteCheckedException If failed.
     */
    private void doTestRandomRemove(boolean canGetRow) throws IgniteCheckedException {
        TestTree tree = createTestTree(canGetRow);

        Map<Long,Long> map = new HashMap<>();

        int cnt = 100_000;

        int rmv = 0;

        for (long x = 0; x < cnt; x++)
            assertEquals(map.put(x,x), tree.put(x));

        for (;;) {
            for (int i = 0; i < 1000 && !map.isEmpty();) {
                Long x = (long)tree.randomInt(cnt);

                if (map.remove(x) != null) {
//                    if (rmv > 93440) {
//                        X.println("Rmv: " + rmv + " -> " + x);
//
//                        if (rmv == 93449)
//                            X.println(tree.printTree());
//                    }

                    assertEquals(x, tree.remove(x));
                    assertNull(tree.remove(x));

                    rmv++;
                    i++;
                }
            }

            GridCursor<Long> cursor = tree.find(null, null);

            int size = 0;

            while (cursor.next()) {
                size++;

                Long x = cursor.get();

                assert x != null;

                assertEquals(map.get(x), x);
            }

            assertEquals(map.size(), size);

            if (size == 0)
                break;
        }
    }

    private void doTestStagedPutRemove(boolean canGetRow) {
        // TODO
    }

    /**
     * @param canGetRow Can get row from inner page.
     * @return Test tree instance.
     * @throws IgniteCheckedException If failed.
     */
    private TestTree createTestTree(boolean canGetRow) throws IgniteCheckedException {
        return new TestTree(reuseList, canGetRow, CACHE_ID, pageMem, allocatePage());
    }

    /**
     * @return Allocated full page ID.
     * @throws IgniteCheckedException If failed.
     */
    private FullPageId allocatePage() throws IgniteCheckedException {
        return pageMem.allocatePage(CACHE_ID, -1, PageIdAllocator.FLAG_IDX);
    }

    /**
     * Test tree.
     */
    private static class TestTree extends BPlusTree<Long, Long> {
        /** */
        static Random rnd;

        /** */
        final boolean canGetRow;

        /**
         * @param canGetRow Can get row from inner page.
         * @param cacheId Cache ID.
         * @param pageMem Page memory.
         * @param metaPageId Meta page ID.
         * @throws IgniteCheckedException If failed.
         */
        public TestTree(ReuseList reuseList, boolean canGetRow, int cacheId, PageMemory pageMem, FullPageId metaPageId)
            throws IgniteCheckedException {
            super(cacheId, pageMem, metaPageId, reuseList);

            this.canGetRow = canGetRow;

            initNew();
        }

        /** {@inheritDoc} */
        @Override public int randomInt(int max) {
            // Need to have predictable reproducibility.
            return rnd.nextInt(max);
        }

        /** {@inheritDoc} */
        @Override protected BPlusIO<Long> io(int type, int ver) {
            BPlusIO<Long> io = io(type);

            assert io.getVersion() == ver: ver;

            return io;
        }

        /**
         * @param type Type.
         * @return IO.
         */
        private BPlusIO<Long> io(int type) {
            switch (type) {
                case LONG_INNER_IO:
                    return latestInnerIO();

                case LONG_LEAF_IO:
                    return latestLeafIO();

                default:
                    throw new IllegalStateException("type: " + type);
            }
        }

        /** {@inheritDoc} */
        @Override protected BPlusInnerIO<Long> latestInnerIO() {
            return canGetRow ? LongInnerIO.INSTANCE1 : LongInnerIO.INSTANCE0;
        }

        /** {@inheritDoc} */
        @Override protected BPlusLeafIO<Long> latestLeafIO() {
            return LongLeafIO.INSTANCE;
        }

        /** {@inheritDoc} */
        @Override protected int compare(BPlusIO<Long> io, ByteBuffer buf, int idx, Long n2)
            throws IgniteCheckedException {
            Long n1 = io.getLookupRow(this, buf, idx);

            return Long.compare(n1, n2);
        }

        /** {@inheritDoc} */
        @Override protected Long getRow(BPlusIO<Long> io, ByteBuffer buf, int idx) throws IgniteCheckedException {
            assert io.canGetRow() : io;

            return io.getLookupRow(this, buf, idx);
        }
    }

    /**
     * TODO refactor to use integer in inner page
     * Long inner.
     */
    private static final class LongInnerIO extends BPlusInnerIO<Long> {
        /** */
        static final LongInnerIO INSTANCE0 = new LongInnerIO(false);

        /** */
        static final LongInnerIO INSTANCE1 = new LongInnerIO(true);

        /**
         */
        protected LongInnerIO(boolean canGetRow) {
            super(LONG_INNER_IO, 302, canGetRow, 8);
        }

        /** {@inheritDoc} */
        @Override public int getMaxCount(ByteBuffer buf) {
            if (MAX_ITEMS_COUNT != 0)
                return MAX_ITEMS_COUNT;

            return super.getMaxCount(buf);
        }

        /** {@inheritDoc} */
        @Override public void store(ByteBuffer dst, int dstIdx, BPlusIO<Long> srcIo, ByteBuffer src, int srcIdx)
            throws IgniteCheckedException {
            store(dst, dstIdx, srcIo.getLookupRow(null, src, srcIdx));
        }

        /** {@inheritDoc} */
        @Override public void store(ByteBuffer buf, int idx, Long row) {
            buf.putLong(offset(idx), row);
        }

        /** {@inheritDoc} */
        @Override public Long getLookupRow(BPlusTree<Long,?> tree, ByteBuffer buf, int idx)
            throws IgniteCheckedException {
            return buf.getLong(offset(idx));
        }
    }

    /**
     * Long leaf.
     */
    private static final class LongLeafIO extends BPlusLeafIO<Long> {
        /** */
        static final LongLeafIO INSTANCE = new LongLeafIO();

        /**
         */
        protected LongLeafIO() {
            super(LONG_LEAF_IO, 603, 8);
        }

        /** {@inheritDoc} */
        @Override public int getMaxCount(ByteBuffer buf) {
            if (MAX_ITEMS_COUNT != 0)
                return MAX_ITEMS_COUNT;

            return super.getMaxCount(buf);
        }

        /** {@inheritDoc} */
        @Override public void store(ByteBuffer buf, int idx, Long row) {
            buf.putLong(offset(idx), row);
        }

        /** {@inheritDoc} */
        @Override public void store(ByteBuffer dst, int dstIdx, BPlusIO<Long> srcIo, ByteBuffer src, int srcIdx) {
            assert srcIo == this;

            dst.putLong(offset(dstIdx), src.getLong(offset(srcIdx)));
        }

        /** {@inheritDoc} */
        @Override public Long getLookupRow(BPlusTree<Long,?> tree, ByteBuffer buf, int idx)
            throws IgniteCheckedException {
            return buf.getLong(offset(idx));
        }
    }
}
