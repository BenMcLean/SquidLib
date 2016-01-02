package squidpony.squidgrid;

import squidpony.squidmath.Coord;

import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * A data structure that seems to be re-implemented often for games, this associates Coord positions and generic I
 * identities with generic E elements. You can get an element from a SpatialMap with either an identity or a position,
 * change the position of an element without changing its value or identity, modify an element given its identity and
 * a new value, and perform analogues to most of the features of the Map interface, though this does not implement Map
 * because it essentially has two key types and one value type. You can also iterate through the values in insertion
 * order, where insertion order should be stable even when elements are moved or modified (the relevant key is the
 * identity, which is never changed in this class). Uses two LinkedHashMap fields internally.
 * Created by Tommy Ettinger on 1/2/2016.
 */
public class SpatialMap<I, E> implements Iterable<E> {
    protected LinkedHashMap<I, E> itemMapping;
    protected LinkedHashMap<Coord, E> positionMapping;

    /**
     * Constructs a SpatialMap with capacity 32.
     */
    public SpatialMap()
    {
        itemMapping = new LinkedHashMap<I, E>(32);
        positionMapping = new LinkedHashMap<Coord, E>(32);
    }

    /**
     * Constructs a SpatialMap with the given capacity
     * @param capacity the capacity for each of the internal LinkedHashMaps
     */
    public SpatialMap(int capacity)
    {
        itemMapping = new LinkedHashMap<I, E>(capacity);
        positionMapping = new LinkedHashMap<Coord, E>(capacity);
    }

    /**
     * Constructs a SpatialMap given arrays of Coord, identity, and element; all 3 arrays should have the same length,
     * since this will use only up to the minimum length of these arrays for how many it adds. Each unique id will be
     * added with the corresponding element at the corresponding Coord position if that position is not already filled.
     * @param coords a starting array of Coord positions; indices here correspond to the other parameters
     * @param ids a starting array of identities; indices here correspond to the other parameters
     * @param elements a starting array of elements; indices here correspond to the other parameters
     */
    public SpatialMap(Coord[] coords, I[] ids, E[] elements)
    {
        itemMapping = new LinkedHashMap<I, E>(Math.min(coords.length, Math.min(ids.length, elements.length)));
        positionMapping = new LinkedHashMap<Coord, E>(Math.min(coords.length, Math.min(ids.length, elements.length)));

        for (int i = 0; i < coords.length && i < ids.length && i < elements.length; i++) {
            add(coords[i], ids[i], elements[i]);
        }
    }

    /**
     * Adds a new element with the given identity and Coord position. If the position is already occupied by an element
     * in this data structure, returns null. If the identity is already used, this also returns null. If the identity
     * and position are both unused, this adds element to the data structure.
     * <br>
     * You should strongly avoid calling remove() and add() to change an element; prefer modify() and move().
     * @param coord the Coord position to place the element at; should be empty
     * @param id the identity to associate the element with; should be unused
     * @param element the element to add
     */
    public void add(Coord coord, I id, E element)
    {
        if(itemMapping.containsKey(id))
            return;
        E nextPosition = positionMapping.get(coord);
        if(nextPosition == null)
        {
            itemMapping.put(id, element);
            positionMapping.put(coord, element);
        }
    }

    /**
     * Changes the element's value associated with id. The key id should exist before calling this.
     * @param id the identity of the element to modify
     * @param newValue the element value to replace the previous element with.
     * @return the previous element value associated with id
     */
    public E modify(I id, E newValue)
    {
        if(itemMapping.containsKey(id))
            return itemMapping.put(id, newValue);
        return null;
    }

    /**
     * The preferred way to move an element from one position to another; moves whatever is at the Coord position
     * previous to the new Coord position target. The element will not be present at its original position if target is
     * unoccupied, but nothing will change if target is occupied.
     * @param previous the starting Coord position of an element to move
     * @param target the Coord position to move the element to
     * @return the moved element if movement was successful or null otherwise
     */
    public E move(Coord previous, Coord target)
    {
        if(positionMapping.containsKey(previous) && !positionMapping.containsKey(target)) {
            E elem = positionMapping.remove(previous);
            positionMapping.put(target, elem);
            return elem;
        }
        return null;
    }

    /**
     * The less-recommended way to move an element, picked by its identity, to a new Coord position. Finds the element
     * through a lower-performance way than the other overload of move(), but does not need the previous position.
     * The target position must be empty for this to move successfully, and the id must exist.
     * @param id the identity of the element to move
     * @param target the Coord position to move the element to
     * @return the moved element if movement was successful or null otherwise
     */
    public E move(I id, Coord target)
    {
        if(itemMapping.containsKey(id) && !positionMapping.containsKey(target)) {
            Iterator<Coord> coordIterator = positionMapping.keySet().iterator();
            if(!coordIterator.hasNext())
                return null;
            Coord current = coordIterator.next();
            for (I i :itemMapping.keySet()) {
                if(i.equals(id))
                {
                    E elem = positionMapping.remove(current);
                    positionMapping.put(target, elem);
                    return elem;
                }
                current = coordIterator.next();
            }
        }
        return null;
    }

    /**
     * Removes the element at the given position from all storage in this data structure.
     * <br>
     * You should strongly avoid calling remove() and add() to change an element; prefer modify() and move().
     * @param coord the position of the element to remove
     * @return the value of the element that was removed or null if nothing was present at the position
     */
    public E remove(Coord coord)
    {
        Iterator<I> keyIterator = itemMapping.keySet().iterator();
        if(!keyIterator.hasNext())
            return null;
        I current = keyIterator.next();
        for (Coord c:positionMapping.keySet()) {
            if(c.equals(coord))
            {
                itemMapping.remove(current);
                return positionMapping.remove(coord);
            }
            current = keyIterator.next();
        }
        return null;
    }
    /**
     * Removes the element with the given identity from all storage in this data structure.
     * <br>
     * You should strongly avoid calling remove() and add() to change an element; prefer modify() and move().
     * @param id the identity of the element to remove
     * @return the value of the element that was removed or null if nothing was present at the position
     */
    public E remove(I id)
    {
        Iterator<Coord> coordIterator = positionMapping.keySet().iterator();
        if(!coordIterator.hasNext())
            return null;
        Coord current = coordIterator.next();
        for (I i :itemMapping.keySet()) {
            if(i.equals(id))
            {
                positionMapping.remove(current);
                return itemMapping.remove(id);
            }
            current = coordIterator.next();
        }
        return null;
    }

    /**
     * Checks whether this contains the given element.
     * @param o an Object that should be an element if you expect this to possibly return true
     * @return true if o is contained as an element in this data structure
     */
    public boolean containsValue(Object o)
    {
        return itemMapping.containsValue(o);
    }
    /**
     * Checks whether this contains the given identity key.
     * @param o an Object that should be of the generic I type if you expect this to possibly return true
     * @return true if o is an identity key that can be used with this data structure
     */
    public boolean containsKey(Object o)
    {
        return itemMapping.containsKey(o);
    }
    /**
     * Checks whether this contains anything at the given position.
     * @param o an Object that should be a Coord if you expect this to possibly return true
     * @return true if o is a Coord that is associated with some element in this data structure
     */
    public boolean containsPosition(Object o)
    {
        return positionMapping.containsKey(o);
    }

    /**
     * Gets the element at the given Coord position.
     * @param c the position to get an element from
     * @return the element if it exists or null otherwise
     */
    public E get(Coord c)
    {
        return positionMapping.get(c);
    }

    /**
     * Gets the element with the given identity.
     * @param i the identity of the element to get
     * @return the element if it exists or null otherwise
     */
    public E get(I i)
    {
        return itemMapping.get(i);
    }
    public void clear()
    {
        itemMapping.clear();
        positionMapping.clear();
    }
    public boolean isEmpty()
    {
        return itemMapping.isEmpty();
    }
    public int size()
    {
        return itemMapping.size();
    }
    public Object[] toArray()
    {
        return itemMapping.values().toArray();
    }
    public E[] toArray(E[] a)
    {
        return itemMapping.values().toArray(a);
    }

    /**
     * Iterates through values in insertion order.
     * @return an Iterator of generic type E
     */
    @Override
    public Iterator<E> iterator()
    {
        return itemMapping.values().iterator();
    }
    /**
     * Iterates through positions in insertion order; has less predictable iteration order than the other iterators.
     * @return an Iterator of Coord
     */
    public Iterator<Coord> positionIterator()
    {
        return positionMapping.keySet().iterator();
    }
    /**
     * Iterates through identity keys in insertion order.
     * @return an Iterator of generic type I
     */
    public Iterator<I> identityIterator()
    {
        return itemMapping.keySet().iterator();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SpatialMap<?, ?> that = (SpatialMap<?, ?>) o;

        if (itemMapping != null ? !itemMapping.equals(that.itemMapping) : that.itemMapping != null) return false;
        return positionMapping != null ? positionMapping.equals(that.positionMapping) : that.positionMapping == null;

    }

    @Override
    public int hashCode() {
        int result = itemMapping != null ? itemMapping.hashCode() : 0;
        result = 31 * result + (positionMapping != null ? positionMapping.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SpatialMap{" +
                "itemMapping=" + itemMapping +
                ", positionMapping=" + positionMapping +
                '}';
    }

}
