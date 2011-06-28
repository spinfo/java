/** Copyright 2011 Fabian Steeg, University of Cologne, http://github.com/spinfo */
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

/** Trees: a versatile data structure. */
public class Trees {

	/* Basic implementation strategy: nodes and links/pointers/references */

	/* Simple binary tree, two pointers in each node. */
	static class SimpleTree {
		SimpleNode root;

		static class SimpleNode {
			Object value;
			SimpleNode left;
			SimpleNode right;
		}
	}

	/* Simple multi-children tree, with a list of children in each node. */
	static class MultiTree {
		MultiNode root;

		static class MultiNode {
			Object value;
			List<MultiNode> children; // n references per node for n children
		}
	}

	/* Multi-children tree implemented like a linked list: less references. */
	static class LinkedTree {
		LinkedNode root;

		static class LinkedNode {
			Object value;
			LinkedNode head;
			LinkedNode tail; // 2 references per node for n children
		}
	}

	@Test
	public void binarySearchTree() {
		BinaryTree tree = new BinaryTree();
		tree.addRecursive(5);
		tree.addRecursive(3);
		tree.addRecursive(8);
		tree.addRecursive(2);
		tree.addRecursive(4);
		tree.addRecursive(7);
		tree.addRecursive(6);
		assertEquals(Arrays.asList(5, 3, 2, 4, 8, 7, 6), tree.preorder());
	}

	@Test
	public void dotVisualization() {
		BinaryTree tree = new BinaryTree();
		tree.addIterative(5);
		tree.addIterative(3);
		tree.addIterative(8);
		tree.addIterative(2);
		tree.addIterative(4);
		tree.addIterative(7);
		tree.addIterative(6);
		assertEquals("digraph{5->3;3->2;3->4;5->8;8->7;7->6;}",
				tree.visualize());
	}

	static class BinaryTree {
		BinaryNode root;

		static class BinaryNode {
			public BinaryNode(int value) {
				this.value = value;
			}

			int value;
			BinaryNode left;
			BinaryNode right;

			public String toString() {
				return String.valueOf(value);
			}
		}

		/* Corresponding to the rekursive structure, we add recursively */
		public void addRecursive(int value) {
			if (root == null)
				root = new BinaryNode(value); // done
			else
				addRecursive(root, value); // go on
		}

		private void addRecursive(BinaryNode node, int value) {
			if (value < node.value) { // new val should go left
				if (node.left == null)
					node.left = new BinaryNode(value); // done
				else
					addRecursive(node.left, value); // go on left
			} else { // new val should go right
				if (node.right == null)
					node.right = new BinaryNode(value); // done
				else
					addRecursive(node.right, value); // go on right
			}
		}

		/* But a recursive concept can also be implemented iteratively */
		public void addIterative(int value) {
			if (root == null)
				root = new BinaryNode(value); // done
			else
				addIterative(root, value); // go on
		}

		private void addIterative(BinaryNode root, int value) {
			BinaryNode current = root;
			while (current != null) {
				if (value < current.value) {
					if (current.left == null) {
						current.left = new BinaryNode(value);
						return; // done
					}
					current = current.left; // go on
				} else {
					if (current.right == null) {
						current.right = new BinaryNode(value);
						return; // done
					}
					current = current.right; // go on
				}
			}
		}

		/* Two preorder traversals: collect values, visualize with Graphviz DOT */

		public List<Integer> preorder() {
			return preorder(root, new ArrayList<Integer>());
		}

		public List<Integer> preorder(BinaryNode node, List<Integer> result) {
			if (node == null)
				return result;
			result.add(node.value); // Pre-order: 1. root
			preorder(node.left, result); // 2. left
			preorder(node.right, result); // 3. right
			return result;
		}

		public String visualize() { // another Pre-order traversal
			StringBuilder builder = new StringBuilder();
			return String.format("digraph{%s}", visualize(root, builder));
		}

		private String visualize(BinaryNode node, StringBuilder builder) {
			if (node != null) { // Pre-order: 1. root
				if (node.left != null)
					builder.append(String.format("%s->%s;", node, node.left));
				visualize(node.left, builder); // 2. left
				if (node.right != null)
					builder.append(String.format("%s->%s;", node, node.right));
				visualize(node.right, builder); // 3. right
			}
			return builder.toString();
		}

	}
}
