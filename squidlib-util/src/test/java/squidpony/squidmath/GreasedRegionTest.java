package squidpony.squidmath;

import org.junit.Test;
import squidpony.squidgrid.Radius;
import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidgrid.mapping.DungeonUtility;

import static org.junit.Assert.assertTrue;
import static squidpony.squidmath.CoordPacker.*;


/**
 * Created by Tommy Ettinger on 10/1/2015.
 */
public class GreasedRegionTest {

    public static GreasedRegion dataCross = new GreasedRegion(unpack(unionPacked(rectangle(25, 2, 14, 60), rectangle(2, 25, 60, 14)), 64, 64));
    public static GreasedRegion dataCross2 = new GreasedRegion(unpack(unionPacked(rectangle(24 + 32, 2 + 32, 16, 60), rectangle(2 + 32, 24 + 32, 60, 16)), 128, 128));
    public static GreasedRegion box = new GreasedRegion(unpack(intersectPacked(rectangle(24, 2, 16, 60), rectangle(2, 24, 60, 16)), 64, 64));
    public static GreasedRegion box2 = new GreasedRegion(unpack(intersectPacked(rectangle(24 + 32, 2 + 32, 16, 60), rectangle(2 + 32, 24 + 32, 60, 16)), 120, 120));
    public static GreasedRegion box3 = new GreasedRegion(unpack(rectangle(30, 30, 180, 180), 240, 240));
    public static StatefulRNG srng = new StatefulRNG(0x1337BEEF);
    public static DungeonGenerator dungeonGen = new DungeonGenerator(64, 64, srng);
    public static char[][] dungeon = dungeonGen.generate();
    public static GreasedRegion dataDungeon = new GreasedRegion(dungeon, '.');
    static {
        //printRegion(dataCross);
        //printRegion(dataCross2);
    }
    public static final boolean PRINTING = true;
    @Test
    public void testBasics() {
        //printPacked(dataCross, 64, 64);
        GreasedRegion singleNegative = new GreasedRegion(dataCross).not(),
                doubleNegative = new GreasedRegion(dataCross).not().not();
        assertTrue(dataCross.equals(doubleNegative));
        GreasedRegion gr = new GreasedRegion(box2);
        printRegion(gr);
        GreasedRegion gr2 = new GreasedRegion(120, 120);
        gr2.insertRectangle(24 + 32, 24 + 32, 16, 16);
        assertTrue(gr.equals(gr2));
        if(PRINTING) {
            srng.setState(0x123456789ABCDEFL);
            DungeonUtility.debugPrint(CoordPacker.unpackChar(CoordPacker.packSeveral(gr.singleRandom(srng),
                    gr.singleRandom(srng), gr.singleRandom(srng),
                    gr.singleRandom(srng), gr.singleRandom(srng),
                    gr.singleRandom(srng), gr.singleRandom(srng),
                    gr.singleRandom(srng), gr.singleRandom(srng),
                    gr.singleRandom(srng), gr.singleRandom(srng),
                    gr.singleRandom(srng), gr.singleRandom(srng),
                    gr.singleRandom(srng), gr.singleRandom(srng),
                    gr.singleRandom(srng), gr.singleRandom(srng),
                    gr.singleRandom(srng), gr.singleRandom(srng),
                    gr.singleRandom(srng), gr.singleRandom(srng)),
                    120, 120, '@', '.'));
            System.out.println();
            printRegion(dataCross.copy().not().insertSeveral(dataCross.separatedPortion(0.05)));
            System.out.println();
            printRegion(dataDungeon);
            printRegion(dataDungeon.copy().clear().insertSeveral(dataDungeon.separatedPortion(0.06)));
        }
        GreasedRegion g = new GreasedRegion(box);
        GreasedRegion g2 = new GreasedRegion(64, 64);
        g2.insertRectangle(24, 24, 16, 16);
        assertTrue(g.equals(g2));
        GreasedRegion grr = new GreasedRegion(box3);
        GreasedRegion grr2 = new GreasedRegion(240, 240);
        grr2.insertRectangle(30, 30, 180, 180);
        assertTrue(grr.equals(grr2));
    }

    public static int FOV_RANGE = 12;
    public static Radius RADIUS = Radius.SQUARE;

    public static void printRegion(GreasedRegion r) {
        if (PRINTING) {
            DungeonUtility.debugPrint(r.toChars());
            System.out.println();
        }
    }

    public void printBits16(int n) {
        for (int i = 0x8000; i > 0; i >>= 1)
            System.out.print((n & i) > 0 ? 1 : 0);
    }

    public void printBits32(int n) {
        for (int i = 1 << 31; i != 0; i >>>= 1)
            System.out.print((n & i) != 0 ? 1 : 0);
    }

    public long arrayMemoryUsage(int length, long bytesPerItem)
    {
        return (((bytesPerItem * length + 12 - 1) / 8) + 1) * 8L;
    }
    public long arrayMemoryUsage2D(int xSize, int ySize, long bytesPerItem)
    {
        return arrayMemoryUsage(xSize, (((bytesPerItem * ySize + 12 - 1) / 8) + 1) * 8L);
    }
    public int arrayMemoryUsageJagged(short[][] arr)
    {
        int ctr = 0;
        for (int i = 0; i < arr.length; i++) {
            ctr += arrayMemoryUsage(arr[i].length, 2);
        }
        return (((ctr + 12 - 1) / 8) + 1) * 8;
    }


    @Test
    public void testTranslate() {
        GreasedRegion crossZeroTranslated = new GreasedRegion(dataCross).translate(0, 0);
        GreasedRegion crossTranslated = new GreasedRegion(dataCross).translate(1, 1);
        GreasedRegion crossUnTranslated = new GreasedRegion(crossTranslated).translate(-1, -1);

        assertTrue(dataCross.equals(crossZeroTranslated));
        assertTrue(dataCross.equals(crossUnTranslated));

        GreasedRegion crossBox = new GreasedRegion(dataCross).translate(25, 25).translate(-50, -50);
        //printPacked(crossBox, 64, 64);
        assertTrue(crossBox.equals(new GreasedRegion(unpack(rectangle(14, 14), 64, 64))));
    }

    @Test
    public void testUnion() {
        GreasedRegion box = new GreasedRegion(dataCross);
        //printRegion(box);
        box.translate(25, 25);
        //printRegion(box);
        box.translate(-50, -50);
        //printRegion(box);
        box.translate(25, 25);
        GreasedRegion alter = new GreasedRegion(unpack(rectangle(25, 2, 14, 60), 64, 64))
                .and(new GreasedRegion(unpack(rectangle(2, 25, 60, 14), 64, 64)));
        //printRegion(box);
        //printRegion(alter);
        assertTrue(box.equals(alter));
        GreasedRegion minus = new GreasedRegion(dataCross).andNot(box);
        GreasedRegion xr = new GreasedRegion(unpack(rectangle(25, 2, 14, 60), 64, 64))
                .xor(new GreasedRegion(unpack(rectangle(2, 25, 60, 14), 64, 64)));
        printRegion(minus);
        printRegion(xr);
        assertTrue(minus.equals(xr));

    }
    @Test
    public void testExpanding() {

        GreasedRegion edge = new GreasedRegion(dataCross).fringe();
        printRegion(edge);
        GreasedRegion bonus = new GreasedRegion(dataCross).expand();
        printRegion(bonus);
        assertTrue(new GreasedRegion(bonus).andNot(edge).equals(dataCross));

        GreasedRegion edge2 = new GreasedRegion(dataCross2).fringe8way();
        printRegion(edge2);
        GreasedRegion bonus2 = new GreasedRegion(dataCross2).expand8way();
        printRegion(bonus2);
        assertTrue(new GreasedRegion(bonus2).andNot(edge2).equals(dataCross2));


        GreasedRegion flooded = new GreasedRegion(Coord.get(26, 2), 64, 64).flood(dataCross, 2);
        GreasedRegion manual = new GreasedRegion(64, 64, Coord.get(25, 2), Coord.get(26, 2), Coord.get(27, 2), Coord.get(28, 2),
                Coord.get(25, 3), Coord.get(26, 3), Coord.get(27, 3),
                Coord.get(26, 4));
        printRegion(flooded);
        printRegion(manual);
        assertTrue(flooded.equals(manual));

    }
    @Test
    public void testRetracting() {
        GreasedRegion surf = new GreasedRegion(dataCross).surface();
        printRegion(surf);
        GreasedRegion shrunk = new GreasedRegion(dataCross).retract();
        printRegion(shrunk);
        assertTrue(new GreasedRegion(shrunk).or(surf).equals(dataCross));

        GreasedRegion surf2 = new GreasedRegion(dataCross2).surface8way();
        printRegion(surf2);
        GreasedRegion shrunk2 = new GreasedRegion(dataCross2).retract8way();
        printRegion(shrunk2);
        assertTrue(new GreasedRegion(shrunk2).or(surf2).equals(dataCross2));
    }
}
