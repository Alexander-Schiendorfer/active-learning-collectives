package de.uniaugsburg.isse.abstraction.merging;

import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

import de.uniaugsburg.isse.abstraction.types.Interval;

/**
 * Prototypical solution for MergeLists hole detection algorithm to find feasible regions and supply holes
 * 
 * @author Alexander Schiendorfer
 * 
 */
public class MergeLists {

	public static class List {
		private Interval<Double> interval;
		private List next;

		public List(Interval<Double> intInterval, List nextObject) {
			this.setInterval(intInterval);
			this.setNext(nextObject);
		}

		/**
		 * Takes a Java collection and converts it into the singly linked list used by the hole detetion algorithm
		 * 
		 * @param collection
		 * @return
		 */
		public static List fromCollection(Collection<Interval<Double>> collection) {
			List head = null;
			if (collection == null || collection.isEmpty()) {
				throw new RuntimeException("Trying to initialize List head with empty collection -- impossible!");
			} else {
				boolean first = true;

				for (Interval<Double> otherInterval : collection) {
					if (first) {
						head = new List(otherInterval, null);
						first = false;
					} else {
						head = List.mergeIn(head, otherInterval);
					}
				}
			}
			return head;
		}

		public Interval<Double> getInterval() {
			return interval;
		}

		public void setInterval(Interval<Double> interval) {
			this.interval = interval;
		}

		public List getNext() {
			return next;
		}

		public void setNext(List next) {
			this.next = next;
		}

		/**
		 * Takes a singly linked list by its head and merges in a new interval appropriately such that no overlaps are
		 * found and all ranges covered by head and nextInterval are included
		 * 
		 * @param head
		 * @param nextInterval
		 * @return
		 */
		public static List mergeIn(List head, Interval<Double> nextInterval) {
			List s = null;
			List iteratePointer = head;
			List predecessor = null;
			List newNode = new List(nextInterval, null);

			while (iteratePointer != null && iteratePointer.getInterval().max < nextInterval.min) {
				predecessor = iteratePointer;
				iteratePointer = iteratePointer.getNext();
			}

			if (iteratePointer == null) { // append
				predecessor.setNext(newNode);
				return head;
			} else if (iteratePointer.getInterval().min > nextInterval.max) {
				if (predecessor != null) {
					newNode.setNext(iteratePointer);
					predecessor.setNext(newNode);
					return head;
				} else {
					newNode.setNext(iteratePointer);
					return newNode;
				}
			} else { // starting to expand s = l
				s = iteratePointer;
				s.getInterval().min = Math.min(s.getInterval().min, nextInterval.min);

				while (iteratePointer != null && nextInterval.max >= iteratePointer.getInterval().min) {
					s.getInterval().max = Math.max(s.getInterval().max, iteratePointer.getInterval().max);
					iteratePointer = iteratePointer.getNext();
				}
				s.getInterval().max = Math.max(s.getInterval().max, nextInterval.max);
				s.setNext(iteratePointer);
				// actually needs to delete all visited intervals -> left as an exercise for gc
				return head;
			}
		}

		public static List deepCopy(List head) {
			List newHead = head;
			List l = head, pred = null;
			while (l != null) {
				List newNode = new List(l.getInterval(), null);
				if (pred == null)
					newHead = newNode;
				else
					pred.setNext(newNode);
				pred = newNode;
				l = l.getNext();
			}
			return newHead;
		}

		public int size() {
			if (getNext() == null)
				return 1;
			else
				return 1 + getNext().size();
		}
	}

	public static List zero() {
		return new List(new Interval<Double>(0.0, 0.0), null);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		List l = new List(new Interval<Double>(0.0, 0.0), null);
		List head = l;
		head = List.mergeIn(head, new Interval<Double>(4.0, 6.0));
		head = List.mergeIn(head, new Interval<Double>(9., 12.));
		head = List.mergeIn(head, new Interval<Double>(15., 17.));

		// list [4 6] [9 12] [15 17]
		printList(head);
		System.out.println("Inserting 10, 15");
		head = List.mergeIn(head, new Interval<Double>(18., 23.));
		printList(head);
		System.out.println("Inserting 2 3");
		head = List.mergeIn(head, new Interval<Double>(2., 3.));
		printList(head);

		System.out.println("Inserting 1 10");
		head = List.mergeIn(head, new Interval<Double>(1., 10.));
		printList(head);

		System.out.println("Inserting 4 14");
		head = List.mergeIn(head, new Interval<Double>(4., 14.));
		printList(head);

		System.out.println("Inserting 5 16");
		head = List.mergeIn(head, new Interval<Double>(5., 16.));
		printList(head);

		System.out.println("Inserting 15 25");
		head = List.mergeIn(head, new Interval<Double>(15., 25.));
		printList(head);

	}

	public static void printList(List head) {
		List l = head;
		while (l != null) {
			System.out.print("[" + l.getInterval().min + " " + l.getInterval().max + "] ");
			l = l.getNext();
		}
		System.out.println();

	}

	public static SortedSet<Interval<Double>> toJavaSet(List l) {
		TreeSet<Interval<Double>> al = new TreeSet<Interval<Double>>();
		while (l != null) {
			al.add(l.getInterval());
			l = l.next;
		}
		return al;
	}

}
