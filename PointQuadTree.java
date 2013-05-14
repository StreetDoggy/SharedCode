//Jesse Craig 2012

import java.awt.geom.*;
import java.awt.Shape;
import java.util.*;

public class PointQuadTree<T extends java.awt.geom.Point2D> {

	//Main function is only for Unit Test
	public static void main(String[] args) {
		//Exercise vulnerability of too many objects inserted at the exact same point
		Point2D spam_pt = new Point2D.Double(10.2, 5.3);
		PointQuadTree<Point2D> spam_pqt = new PointQuadTree<Point2D>(10,
				new Rectangle2D.Double(0.0, 0.0, 1024.0, 1024.0));
		for(int index = 0; index < 200; ++index){
			spam_pqt.add((Point2D) spam_pt.clone());
		}
		// This is all testing code.
		// 500,000 random points are generated
		// The test then iterates over various values for the maximum number
		// of points per leaf before the node shatters into four new nodes.
		// The time needed to get all the points lying in a rectangle is
		// gathered
		// over several iterations and the mean displayed as a performance
		// metric.
		// The testing also tries deleting all the points inside a rectangle
		// to ensure the shattering and unshattering are functioning. The code
		// checks
		// to ensure all the correct points are deleted, no more, no less.
		Random randomizer = new Random();
		Vector<Point2D> points = new Vector<Point2D>();
		Rectangle2D mtch_region = new Rectangle2D.Double(100.0, 700.0, 50.0,
				75.0);
		int exp_mtch_cnt = 0;
		for (int index = 0; index < 500000; ++index) {
			Point2D pt = new Point2D.Double(randomizer.nextInt(1024),
					randomizer.nextInt(1024));
			if (mtch_region.contains(pt))
				++exp_mtch_cnt;
			points.add(pt);
		}
		Point2D pt_fixed = new Point2D.Double(3.76, 4.93);
		for (int max_points = 8; max_points <= (int) Math
				.ceil(points.size() * 1.05); max_points = (int) Math
				.ceil(max_points * 1.05)) {
			PointQuadTree<Point2D> pqt = new PointQuadTree<Point2D>(max_points,
					new Rectangle2D.Double(0.0, 0.0, 1024.0, 1024.0));
			for (Point2D point : points) {
				pqt.add(point);
			}
			pqt.add((Point2D) pt_fixed.clone());
			TreeSet<Double> run_times = new TreeSet<Double>();
			for (int rep = 0; rep < 100; ++rep) {
				long start_time = System.nanoTime();
				pqt.get(mtch_region);
				long end_time = System.nanoTime();
				run_times.add(new Double(end_time - start_time));
			}
			int pre_fake_del_size = pqt.size();
			pqt.remove(new Point2D.Double(-1.0, -1.0));
			pqt.remove(new Point2D.Double(-1.0, 1025.0));
			pqt.remove(new Point2D.Double(1025.0, -1.0));
			pqt.remove(new Point2D.Double(1025.0, 1025.0));
			if(pre_fake_del_size != pqt.size()){
				System.out
				.println("Error: Size not constant after delete of points not contained in tree (exp: "
						+ pre_fake_del_size
						+ " act: "
						+ pqt.size()
						+ ")");
			}
			List<Point2D> mtch_pts = pqt.get(mtch_region);
			if (mtch_pts.size() != exp_mtch_cnt) {
				System.out
						.println("Error: Not finding all the points expected (exp: "
								+ exp_mtch_cnt
								+ " act: "
								+ mtch_pts.size()
								+ ")");
			}
			int pre_del_size = pqt.size();
			for (Point2D pt : mtch_pts) {
				if (!pqt.remove(pt)) {
					System.out.println("Error: Could not remove a point!!");
				}
			}
			if (pqt.size() != (pre_del_size - mtch_pts.size())) {
				System.out
						.println("Error: Size doesn't match expected after deletion (act: "
								+ pqt.size()
								+ " exp: "
								+ (pre_del_size - mtch_pts.size()) + ")");
			}
			if ((pqt.get(mtch_region)).size() > 0) {
				System.out
						.println("Error: After deleting all points in the rect. some points still exist!!");
			}

			if ((pqt.get(pt_fixed)).size() < 1) {
				System.out
						.println("Error: Not able to retrieve the point manually inserted");
			}

			// find median run time
			int median_time = 0;
			int run = 0;
			for (Double run_time : run_times) {
				if (run == run_times.size() / 2)
					median_time = (int) Math.round(run_time);
				run++;
			}
			System.out.print("" + max_points);
			System.out.print(", " + pqt.getNodeCount());
			System.out.print(", " + pqt.getTreeDepth());
			System.out.println(", " + median_time);
		}
	}

	// indices for quadrants in the array of children
	// also used as constants to refer to the quadrants
	private static final int NE = 0;

	private static final int SE = 1;

	private static final int SW = 2;

	private static final int NW = 3;

	// The rectangle that this node stores
	// points for.
	private Rectangle2D bounds, inflated_bounds;

	// Maximum number of points allowed before
	// this node shatters into 4 more nodes.
	// Minimum number of points allowed before
	// this node unshatters into a leaf again.
	private int max_points, min_points;

	// Points owned by this node and it's children
	// This can be found using a recursive function,
	// but using this variable really helps
	// performance.
	private int point_count;

	// Are we a leaf node or do we have children.
	// This could be found by seeing if any
	// children are not null, but the variable
	// is faster.
	private boolean is_leaf;

	// Points owned by this node. This is only
	// valid if the node is a leaf node.
	// Shattering the node sets this to null.
	// Unshattering sets it to a new Vector
	private List<PointContainer<T>> points;

	// Array of child nodes. This is only
	// valid if the node is not a leaf node.
	// Note that shattering does not create
	// Children. Children are created on demand
	// when the first point that that child would
	// store is added.
	private ArrayList<PointQuadTree<T>> children;

	public PointQuadTree(Rectangle2D bounds) {
		// Why 32? -- empirical evidence suggests this is best for performance.
		// Running the main function of this class executes an experiment
		// using different max_point values and measuring performance.
		this(32, bounds);
	}

	public PointQuadTree(int max_points, Rectangle2D bounds) {
		// min_points set to 60% of the max_points.
		// if min_points is to close to max_points, nodes
		// get shattered and unshattered too much, which
		// is a big performance kill.
		this((int) Math.max(1, max_points * 0.6), max_points, bounds);
	}

	public PointQuadTree(int min_points, int max_points, Rectangle2D bounds) {
		super();
		this.min_points = min_points;
		this.max_points = max_points;
		this.point_count = 0;
		is_leaf = true;
		this.bounds = bounds;
		// inflated_bounds is used because the intersect and contains
		// operator of Rectangle2D doesn't match for points/rects exactly on
		// the boundary of the rectangle.
		inflated_bounds = new Rectangle2D.Double(bounds.getMinX() - 0.01,
				bounds.getMinY() - 0.01, bounds.getWidth() + 0.02, bounds
						.getHeight() + 0.02);
		points = new Vector<PointContainer<T>>(this.max_points);
		children = new ArrayList<PointQuadTree<T>>(4);
		children.add(NE, null);
		children.add(SE, null);
		children.add(SW, null);
		children.add(NW, null);
	}

	private void shatter() {
		// This shatters a leaf node into a node
		// pointing to four child nodes, placing
		// the points owned by this node in the jurisdiction
		// of the proper children
		assert (is_leaf == true);
		is_leaf = false;
		for(PointContainer<T> container : points){
			for(T point : container.getPoints()){
				--point_count;
				this.add(point);
			}
		}
		points.clear();
		points = null;
	}

	private void unshatter() {
		// This takes all the points owned by
		// the children and moves them into this
		// node. The children are then deleted.
		assert (is_leaf == false);
		points = this.getAllPointContiners();
		assert (points.size() == point_count);
		// delete children
		children.set(NE, null);
		children.set(SE, null);
		children.set(SW, null);
		children.set(NW, null);
		is_leaf = true;
	}

	public synchronized void add(T point) {
		++point_count;
		if (is_leaf) {
			if (points.size() >= max_points) {
				this.shatter();
				--point_count; // this is done so we don't get double counting
				this.add(point);
			} else {
				this.addToLeaf(point);
			}
		} else {
			int quadrant = this.getOwningQuadrant(point);
			if (children.get(quadrant) == null) {
				Rectangle2D rect = this.getQuadrantBounds(quadrant);
				children.set(quadrant, new PointQuadTree<T>(min_points,
						max_points, rect));
			}
			children.get(quadrant).add(point);
		}
	}
	
	private void addToLeaf(T point){
		for(PointContainer<T> container : points){
			if(container.equals(point)){
				container.addPoint(point);
				return;
			}
		}
		PointContainer<T> container = new PointContainer<T>(point.getX(), point.getY());
		container.addPoint(point);
		points.add(container);
	}
	
	private boolean removeFromLeaf(T point){
		for(PointContainer<T> container : points){
			if(container.equals(point)){
				container.removePoint(point);
				if(container.size() < 1) points.remove(container);
				return true;
			}
		}
		return false;
	}

	public synchronized boolean remove(T point) {
		--point_count;
		if (is_leaf) {
			return removeFromLeaf(point);
		} else {
			if (min_points > 0 && this.size() <= min_points) {
				this.unshatter(); //adjust the structure of this section of the tree
				++point_count; // this is done so we don't get double counting
				return this.remove(point); //call remove again to do actual removal
			} else {
				int quadrant = this.getOwningQuadrant(point);
				if (children.get(quadrant) != null) {
					if(children.get(quadrant).remove(point)){
						return true;
					}//otherwise just fall through to return false after undo-ing the change
					//in the point_count;
				}
			}
		}
		++point_count; //we never found the point to delete
		return false;
	}

	protected int getOwningQuadrant(T point) {
		// Checking point with asserts so development code
		// runs faster. Not generally good practice since
		// points MUST be inside the bounds or the QuadTree
		// can enter an infinite loop of shattering.
		assert (this.contains(point));

		if (point.getX() <= bounds.getCenterX()) {
			// West
			if (point.getY() <= bounds.getCenterY()) {
				// North
				return NW;
			} else {
				// South
				return SW;
			}
		} else {
			// East
			if (point.getY() <= bounds.getCenterY()) {
				// North
				return NE;
			} else {
				// South
				return SE;
			}
		}
	}

	protected Rectangle2D getQuadrantBounds(int quad) {
		assert ((quad == NE) || (quad == NW) || (quad == SW) || (quad == SE));
		double width = (bounds.getWidth() / 2.0);
		double height = (bounds.getHeight() / 2.0);
		if (quad == NW) {
			return new Rectangle2D.Double(bounds.getMinX(), bounds.getMinY(),
					width, height);
		} else if (quad == NE) {
			return new Rectangle2D.Double(bounds.getCenterX(),
					bounds.getMinY(), width, height);
		} else if (quad == SW) {
			return new Rectangle2D.Double(bounds.getMinX(),
					bounds.getCenterY(), width, height);
		} else if (quad == SE) {
			return new Rectangle2D.Double(bounds.getCenterX(), bounds
					.getCenterY(), width, height);
		}
		return null;
	}

	private boolean intersects(Rectangle2D rect) {
		return this.inflated_bounds.intersects(rect);
	}

	private boolean contains(Point2D point) {
		return inflated_bounds.contains(point);
	}

	public synchronized int size() {
		return point_count;
	}

	public synchronized int getNodeCount() {
		int count = 1;
		if (!is_leaf) {
			for (PointQuadTree<T> pqt : children) {
				if (pqt != null) {
					count += pqt.getNodeCount();
				}
			}
		}
		return count;
	}

	public synchronized int getTreeDepth() {
		int depth = 0;
		if (!is_leaf) {
			for (PointQuadTree<T> pqt : children) {
				if (pqt != null) {
					depth = Math.max(depth, pqt.getTreeDepth());
				}
			}
		}
		return depth + 1;
	}

	public synchronized java.util.List<T> get(Shape shape) {
		// Returns all the points for which
		// shape.contains(the_point) is true.
		Vector<T> match_points = new Vector<T>();
		this.appendPoints(shape, shape.getBounds(), match_points);
		return match_points;
	}

	private void appendPoints(Shape shp, Rectangle2D shp_bounds,
			Vector<T> match_points) {
		// Recursively has the proper children add their points to
		// the Vector
		if (is_leaf) {
			// Return all the points in this leaf that are in
			// the desired rectangle
			for(PointContainer<T> container : points){
				if(shp.contains(container)){
					match_points.addAll(container.getPoints());	
				}
			}
		} else {
			// Run recursively on the child nodes
			// that own space overlapping the rectangle
			for (PointQuadTree<T> pqt : children) {
				if (pqt != null && pqt.intersects(shp_bounds)) {
					pqt.appendPoints(shp, shp_bounds, match_points);
				}
			}
		}
	}

	public synchronized java.util.List<T> get(Point2D point) {
		// Returns all the points for which
		// points.equals(the_stored_point) is true.
		Vector<T> match_points = new Vector<T>();
		this.appendPoints(point, match_points);
		return match_points;
	}

	private void appendPoints(Point2D criteria, Vector<T> match_points) {
		// Recursively has the proper children add their points to
		// the Vector
		if (is_leaf) {
			// Return all the points in this leaf that are in
			// the desired rectangle
			for(PointContainer<T> container : points){
				if(container.equals(criteria)){
					match_points.addAll(container.getPoints());	
				}
			}
		} else {
			// Run recursively on the child nodes
			// that own space overlapping the rectangle
			for (PointQuadTree<T> pqt : children) {
				if (pqt != null && pqt.contains(criteria)) {
					pqt.appendPoints(criteria, match_points);
				}
			}
		}
	}
	
	public synchronized java.util.List<T> getContents(){
		List<T> contents = new Vector<T>();
		List<PointContainer<T>> containers = getAllPointContiners();
		for(PointContainer<T> container : containers){
			contents.addAll(container.getPoints());
		}
		return contents;
	}

	private synchronized java.util.List<PointContainer<T>> getAllPointContiners() {
		Vector<PointContainer<T>> all_points = new Vector<PointContainer<T>>();
		this.appendAllPointContainers(all_points);
		return all_points;
	}

	private void appendAllPointContainers(Vector<PointContainer<T>> all_points) {
		// Recursively has all the children add their points to
		// the Vector
		if (is_leaf) {
			// Return all the points in this leaf
			for (PointContainer<T> point : points) {
				all_points.add(point);
			}
		} else {
			// Run recursively on the child nodes
			for (PointQuadTree<T> pqt : children) {
				if (pqt != null) {
					pqt.appendAllPointContainers(all_points);
				}
			}
		}
	}

	public synchronized Rectangle2D getBounds() {
		return bounds;
	}
	
	protected class PointContainer<G extends T> extends java.awt.geom.Point2D.Double{
		private static final long serialVersionUID = 1L;
		
		private List<G> points;
		
		public PointContainer(double x, double y){
			super(x, y);
			
			points = new ArrayList<G>();
		}
		
		public List<G> getPoints(){
			return points;
		}
		
		public void addPoint(G point){
			points.add(point);
		}
		
		public void removePoint(G point){
			points.remove(point);
		}
		
		public int size(){
			return points.size();
		}
		
	}
}
