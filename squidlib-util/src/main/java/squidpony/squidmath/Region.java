package squidpony.squidmath;

import java.util.AbstractList;
import java.util.Collection;

import static squidpony.squidmath.CoordPacker.*;
/**
 * Represents an area or series of areas as one logical unit, and allows iterating over or altering that unit.
 * This can be useful for getting an iterable data structure from a FOV map, Dijkstra map, or just a map from a dungeon
 * generator. It can also work with some shapes (circles and rectangles).
 * Regions must be no larger than 256x256 (actually, the Coords in them should fit in that), even if Coord has had its
 * expandPool() method called. This is because CoordPacker relies on that maximum size to significantly improve
 * efficiency, and this is really just a convenience class wrapping CoordPacker to avoid some of its complexity.
 * More features are likely to be ported into Region as they are added to CoordPacker.
 * Created by Tommy Ettinger on 5/12/2016.
 */
public class Region extends AbstractList<Coord>{

    protected short[] raw;
    protected Coord[] coords;
    private boolean dirty;

    /**
     * A constructor for a Region that takes a 2D double array, usually the kind produced by FOV, and stores only Coord
     * positions that correspond to values greater than 0.0 (actually, greater than epsilon, which here is 0.0001).
     * This won't behave as-expected if you give it a double[][] that DijkstraMap produces; there's a different
     * constructor for that purpose.
     * @param fovMap a 2D double array as produced by FOV
     */
    public Region(double[][] fovMap)
    {
        this(pack(fovMap));
    }

    /**
     * A constructor for a Region that takes a 2D double array, usually produced by DijkstraMap, and a maximum value,
     * and stores only Coord positions that correspond to values no greater than maximum.
     * This won't behave as-expected if you give it a double[][] that FOV produces; there's a different
     * constructor for that purpose.
     * @param dijkstraMap a 2D double array as produced by DijkstraMap
     * @param maximum the highest value that a position can have in dijkstraMap and still be given a Coord in this
     */
    public Region(double[][] dijkstraMap, double maximum)
    {
        this(pack(dijkstraMap, maximum));
    }

    /**
     * A constructor for a Region that takes a 2D char array, the kind produced by most map/dungeon generators in this
     * library, and a vararg or array of char that will have their Coord positions used where those chars appear in map.
     * This is optimized for the common case of a single char in using if you only want to, for example, store '.' to
     * make a Region of floors, or '#' for walls.
     * @param map a 2D char array that is usually the kind returned by a dungeon or map generator
     * @param using an array or vararg of char that will have their Coords used where they appear in map
     */
    public Region(char[][] map, char... using)
    {
        this(pack(map, using));
    }

    /**
     * A constructor for a Region that takes an array or vararg of Coord and encodes all of them in the Region.
     * @param points an array or vararg of Coord that will be stored in this Region, none can be null
     */
    public Region(Coord... points)
    {
        this(packSeveral(points));
    }
    /**
     * A constructor for a Region that takes a Collection of Coord, such as a List or Set, and encodes all of them in
     * the Region.
     * @param points a Collection of Coord that will be stored in this Region, none can be null
     */
    public Region(Collection<Coord> points)
    {
        this(packSeveral(points));
    }

    /**
     * A constructor for a circular Region (possibly truncated at the edges) with a Coord center, an int radius, and a
     * maximum width and height that the Coords in this Region will not exceed.
     * @param center the center of the circular Region
     * @param circleRadius the radius as an int
     * @param mapWidth one more than the maximum x-position of any Coord this will contain
     * @param mapHeight one more than the maximum y-position of any Coord this will contain
     */
    public Region(Coord center, int circleRadius, int mapWidth, int mapHeight)
    {
        this(circle(center, circleRadius, mapWidth, mapHeight));
    }

    /**
     * A constructor for a rectangular Region that stores Coords for the area from (minX,minY) at the minimum corner to
     * (width + minX - 1, height + minY - 1) at the maximum corner. All parameters should be non-negative and less than
     * 256, and height and width will be reduced if a Coord in the rectangle would extend to 256 in either dimension.
     * This doesn't take two Coords as parameters because that would be confusing with the constructor that takes a
     * vararg or array of Coord for its parameters.
     * @param minX lowest x-coordinate in the rectangle; should be between 0 and 255
     * @param minY lowest y-coordinate in the rectangle; should be between 0 and 255
     * @param width total width of the rectangle; must be non-negative
     * @param height total height of the rectangle; must be non-negative
     */
    public Region(int minX, int minY, int width, int height)
    {
        this(rectangle(minX, minY, width, height));
    }
    /**
     * A constructor for a Region that takes a specifically-formatted short array (packed data), as produced by
     * CoordPacker or sometimes other classes, like RegionMap or RoomFinder. If you don't have such data, the other
     * constructors are recommended instead.
     * <br>
     * Note: arrays of Hilbert indices are occasionally returned in CoordPacker as a different kind of short array, but
     * those methods are always noted as such and those short arrays won't make sense if passed to this constructor.
     * They may result in a technically-valid Region with random-seeming contents. In general, if a method in
     * CoordPacker returns a short array (most of them do), but the name contains Hilbert, it may return the wrong kind
     * (an array of Hilbert indices is wrong, packed data is right); check the documentation for that method.
     * @param packedData a short array as produced by CoordPacker (usually), or sometimes RoomFinder or RegionMap
     */
    public Region(short[] packedData)
    {
        raw = new short[packedData.length];
        System.arraycopy(packedData, 0, raw, 0, packedData.length);
        coords = allPacked(raw);
        dirty = false;
    }

    /**
     * Gets a single random Coord from this using the given RNG (which can be seeded); returns null if this is empty.
     * @param rng the source of random numbers used to get a random Coord from this Region
     * @return a random Coord in this Region, or null if this is empty
     */
    public Coord getRandomCoord(RNG rng)
    {
        if(CoordPacker.isEmpty(raw))
            return null;
        return singleRandom(raw, rng);
    }

    /**
     * Takes this region and walks through its Coords in chunks with length equal to separation, creating a new Region
     * where one Coord in each chunk is kept and the others are discarded.
     * @param separation an int where higher numbers mean there will be more distance between Coords, and fewer total
     * @return a new Region made of the separated Coords
     */
    public Region separatedCoords(int separation)
    {
        return new Region(fractionPacked(raw, separation));
    }


    /**
     * Gets the Coord stored at the given index in this Region, or null if index is out of bounds.
     * The ordering this class uses may seem unusual, but it is predictable, using the pattern a Hilbert Curve takes to
     * wind through a 256x256 space (at its maximum). Any two given Coords will always have the same before-after
     * relationship, regardless of other Coords in the Region. A Region is a dense data structure, like a List or array,
     * so valid indices shouldn't ever return null.
     * @param index the index into this Region to get a Coord at; should be less than size() and non-negative.
     * @return the Coord this Region holds at index, if index is valid; otherwise null
     */
    @Override
    public Coord get(int index) {
        if(dirty)
        {
            coords = allPacked(raw);
            dirty = false;
        }
        if(index >= 0 && index < coords.length)
            return coords[index];
        return null;
    }

    /**
     * Gets the size of this Region as measured in Coords stored.
     * Performs "cleanup" if the Region is "dirty," even though this doesn't specifically request a Coord. As such, it
     * may take longer than a call to size() might be expected to, but only just after a "dirtying" method was called.
     * @return the number of Coords stored in this Region.
     */
    @Override
    public int size() {
        if(dirty)
        {
            coords = allPacked(raw);
            dirty = false;
        }
        return coords.length;
    }

    /**
     * Returns true if there are no Coords in this Region, or false otherwise. Can be more efficient than checking if
     * size() is greater than 0 because it doesn't depend on the "dirty or clean" state, and can quickly check.
     * @return
     */
    @Override
    public boolean isEmpty() {
        return CoordPacker.isEmpty(raw);
    }

    /**
     * Adds a Coord to this Region, returning false if the Coord is null or already included in this, or true otherwise.
     * Causes the Region to be considered "dirty", which will make anything that gets a Coord from this to perform some
     * "cleanup" on the Coords this holds when a Coord is requested. You can perform multiple "dirtying" operations in
     * succession without needing more "cleanups" than the one when a Coord is next requested.
     * @param coord a Coord to add to this region
     * @return true if the Coord was added and was not already present; false if the Coord as null or already present
     */
    @Override
    public boolean add(Coord coord) {
        if(coord == null || queryPacked(raw, coord.x, coord.y))
            return false;
        raw = insertPacked(raw, coord.x, coord.y);
        dirty = true;
        return true;
    }

    /**
     * Gets a direct reference to this Region's "raw packed data"; don't modify it unless you know what you're doing!
     * It's fine to pass this to methods in CoordPacker, since essentially all of those methods won't modify packed data
     * given as arguments.
     * @return the raw packed data this class uses; should not be modified carelessly
     */
    public short[] getRaw() {
        return raw;
    }

    /**
     * Sets the "raw packed data" to the given short array, as generated by CoordPacker or some parts of RegionMap.
     * This hopefully won't need to be consumed too much externally, but is an important bridge between this and
     * CoordPacker's API, which deals mostly with these special short arrays.
     * Causes the Region to be considered "dirty", which will make anything that gets a Coord from this to perform some
     * "cleanup" on the Coords this holds when a Coord is requested. You can perform multiple "dirtying" operations in
     * succession without needing more "cleanups" than the one when a Coord is next requested.
     * @param raw a short array of packed data; has a very specific format and should usually not be made manually
     */
    public void setRaw(short[] raw) {
        this.raw = raw;
        dirty = true;
    }

    /**
     * Gets the Coords contained in this as an array, a direct reference that does permit modifying the Coords in your
     * own code without altering "dirty/clean" status. This method does "cleanup" in itself if necessary, but once the
     * Coords are returned you can change them at-will. The Coords may not reflect changes made by this object if it
     * creates a new Coord array, as it often does.
     * @return the Coords contained in this Region as an array
     */
    public Coord[] getCoords() {
        if(dirty)
        {
            coords = allPacked(raw);
            dirty = false;
        }
        return coords;
    }

    /**
     * Changes this Region to include the given Coords and disregard its previous contents. If any elements of coords
     * are null, this Region will hold no Coords (a sort of escape hatch to avoid throwing an exception, since all other
     * methods in this class fail on null entries without potentially crashing a program).
     * @param coords an array or vararg of Coord that will be used as the entirety of this Region
     */
    public void setCoords(Coord... coords) {
        if (coords == null)
            return;
        raw = packSeveral(coords);
        if (raw == ALL_WALL) {
            this.coords = new Coord[0];
            dirty = false;
        } else {
            this.coords = new Coord[coords.length];
            System.arraycopy(coords, 0, this.coords, 0, coords.length);
            dirty = false;
        }
    }
}