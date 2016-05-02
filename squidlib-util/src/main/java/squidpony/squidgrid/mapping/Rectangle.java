package squidpony.squidgrid.mapping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import squidpony.squidgrid.Direction;
import squidpony.squidgrid.iterator.SquidIterators;
import squidpony.squidmath.Coord;

/**
 * Rectangles in 2D grids. Checkout {@link Utils} for utility methods.
 * 
 * @author smelC
 * 
 * @see RectangleRoomFinder How to find rectangles in a dungeon
 */
public interface Rectangle {

	/**
	 * @return The bottom left coordinate of the room.
	 */
	public Coord getBottomLeft();

	/**
	 * @return The room's width (from {@link #getBottomLeft()). It is greater or
	 *         equal than 0.
	 */
	public int getWidth();

	/**
	 * @return The room's height (from {@link #getBottomLeft()). It is greater
	 *         or equal than 0.
	 */
	public int getHeight();

	/**
	 * Utilities pertaining to {@link Room}
	 * 
	 * @author smelC
	 */
	public static class Utils {

		/** A comparator that uses {@link #size(Rectangle)} as the measure. */
		public static final Comparator<Rectangle> SIZE_COMPARATOR = new Comparator<Rectangle>() {
			@Override
			public int compare(Rectangle o1, Rectangle o2) {
				return Integer.compare(size(o1), size(o2));
			}
		};

		/**
		 * @param r
		 * @param c
		 * @return Whether {@code r} contains {@code c}.
		 */
		public static boolean contains(Rectangle r, Coord c) {
			final Coord bottomLeft = r.getBottomLeft();
			final int width = r.getWidth();
			final int height = r.getHeight();
			if (c.x < bottomLeft.x)
				/* Too much to the left */
				return false;
			if (bottomLeft.x + width < c.x)
				/* Too much to the right */
				return false;
			if (bottomLeft.y < c.y)
				/* Too low */
				return false;
			if (c.y < bottomLeft.y - height)
				/* Too high */
				return false;
			return true;
		}

		/**
		 * @param r
		 * @param c
		 * @return {@code true} if {@code r} contains a member of {@code cs}.
		 */
		public static boolean containsAny(Rectangle r, Collection<Coord> cs) {
			for (Coord c : cs) {
				if (contains(r, c))
					return true;
			}
			return false;
		}

		/**
		 * @param rs
		 * @param c
		 * @return {@code true} if a member of {@code rs}
		 *         {@link #contains(Room, Coord) contains} {@code c}.
		 */
		public static boolean contains(Iterable<? extends Rectangle> rs, Coord c) {
			for (Rectangle r : rs) {
				if (contains(r, c))
					return true;
			}
			return false;
		}

		/**
		 * @param r
		 * @return The number of cells that {@code r} covers.
		 */
		public static int size(Rectangle r) {
			return r.getWidth() * r.getHeight();
		}

		/**
		 * @param r
		 * @return The center of {@code r}.
		 */
		public static Coord center(Rectangle r) {
			final Coord bl = r.getBottomLeft();
			/*
			 * bl.y - ... : because we're in SquidLib coordinates (0,0) is top
			 * left
			 */
			return Coord.get(bl.x + Math.round(r.getWidth() / 2), bl.y - Math.round(r.getHeight() / 2));
		}

		/**
		 * Use {@link #cellsList(Rectangle)} if you want them all.
		 * 
		 * @param r
		 * @return The cells that {@code r} contains, from bottom left to top
		 *         right; lazily computed.
		 */
		public static Iterator<Coord> cells(Rectangle r) {
			return new SquidIterators.RectangleFromBottomLeftToTopRight(r.getBottomLeft(), r.getWidth(),
					r.getHeight());
		}

		/**
		 * Use {@link #cellsList(Rectangle)} if you may stop before the end of
		 * the list, you'll save some memory.
		 * 
		 * @param r
		 * @return The cells that {@code r} contains, from bottom left to top
		 *         right.
		 */
		public static List<Coord> cellsList(Rectangle r) {
			/* Allocate it with the right size, to avoid internal resizings */
			final List<Coord> result = new ArrayList<Coord>(size(r));
			final Iterator<Coord> it = cells(r);
			while (it.hasNext())
				result.add(it.next());
			return result;
		}

		public static List<Coord> column(Rectangle r, boolean leftOrRight) {
			final Coord bl = r.getBottomLeft();
			final int height = r.getHeight();
			final int width = r.getWidth();
			final List<Coord> result = new ArrayList<Coord>(height);
			Coord current = leftOrRight ? bl : bl.translate(width, 0);
			for (int i = 0; i < height; i++) {
				result.add(current);
				current = current.translate(0, 1);
			}
			return result;
		}

		public static List<Coord> line(Rectangle r, boolean botOrTop) {
			final Coord bl = r.getBottomLeft();
			final int height = r.getHeight();
			final int width = r.getWidth();
			final List<Coord> result = new ArrayList<Coord>(width);
			/* In SquidLib, (0,0) is top left so bl has a big 'y' */
			Coord current = botOrTop ? bl : bl.translate(0, -height);
			for (int i = 0; i < width; i++) {
				result.add(current);
				current = current.translate(1, 0);
			}
			return result;
		}

		public static List<Coord> getBorder(Rectangle r) {
			final List<Coord> result = new ArrayList<Coord>((r.getHeight() + r.getWidth()) * 2);
			/* bottom line */
			result.addAll(line(r, true));
			/* right colum */
			result.addAll(column(r, false));
			/* top line */
			result.addAll(line(r, false));
			/* left colum */
			result.addAll(column(r, true));
			return result;
		}

		public static List<Coord> getOutsideBorder(Rectangle r) {
			Rectangle extension = extend(r, Direction.DOWN);
			extension = extend(extension, Direction.RIGHT);
			extension = extend(extension, Direction.UP);
			extension = extend(extension, Direction.LEFT);
			return getBorder(extension);
		}

		/**
		 * @param d
		 *            A direction.
		 * @return {@code r} extended to {@code d} by one row and/or column.
		 */
		public static Rectangle extend(Rectangle r, Direction d) {
			final Coord bl = r.getBottomLeft();
			final int width = r.getWidth();
			final int height = r.getHeight();

			switch (d) {
			case DOWN_LEFT:
				return new Rectangle.Impl(bl.translate(Direction.DOWN_LEFT), width + 1, height + 1);
			case DOWN_RIGHT:
				return new Rectangle.Impl(bl.translate(Direction.DOWN), width + 1, height + 1);
			case NONE:
				return r;
			case UP_LEFT:
				return new Rectangle.Impl(bl.translate(Direction.LEFT), width + 1, height + 1);
			case UP_RIGHT:
				return new Rectangle.Impl(bl, width + 1, height + 1);
			case DOWN:
				return new Rectangle.Impl(bl.translate(Direction.DOWN), width, height + 1);
			case LEFT:
				return new Rectangle.Impl(bl.translate(Direction.LEFT), width + 1, height);
			case RIGHT:
				return new Rectangle.Impl(bl, width + 1, height);
			case UP:
				return new Rectangle.Impl(bl, width, height + 1);
			}
			throw new IllegalStateException("Unmatched direction in Rectangle.Utils::extend: " + d);
		}

	}

	/**
	 * @author smelC
	 */
	public static class Impl implements Rectangle {

		protected final Coord bottomLeft;
		protected final int width;
		protected final int height;

		public Impl(Coord bottomLeft, int width, int height) {
			this.bottomLeft = bottomLeft;
			this.width = width;
			this.height = height;
		}

		@Override
		public Coord getBottomLeft() {
			return bottomLeft;
		}

		@Override
		public int getWidth() {
			return width;
		}

		@Override
		public int getHeight() {
			return height;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((bottomLeft == null) ? 0 : bottomLeft.hashCode());
			result = prime * result + height;
			result = prime * result + width;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Impl other = (Impl) obj;
			if (bottomLeft == null) {
				if (other.bottomLeft != null)
					return false;
			} else if (!bottomLeft.equals(other.bottomLeft))
				return false;
			if (height != other.height)
				return false;
			if (width != other.width)
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "Room at " + bottomLeft + ", width:" + width + ", height:" + height;
		}
	}

}
