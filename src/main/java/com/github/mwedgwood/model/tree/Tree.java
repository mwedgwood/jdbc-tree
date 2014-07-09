package com.github.mwedgwood.model.tree;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class Tree {

    public Integer getId() {
        return node.getId();
    }

    @JsonBackReference
    private Tree parent;

    @JsonManagedReference
    private List<Tree> children = new ArrayList<>();

    private Node node;

    public static <T extends Node> Tree createRoot(String name, String description, Class<T> type) {
        T root;
        try {
            root = type.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        root.setName(name);
        root.setDescription(description);
        return new Tree(root);
    }

    public static Tree fromList(List<Node> nodes) {
        Map<Integer, Tree> parents = new LinkedHashMap<>();
        Tree root = null;

        for (Node node : nodes) {
            Integer parentId = node.getParentId();
            Tree tree = new Tree(node);

            if (parentId == null || parentId == 0) {
                root = tree;
            }

            Tree parent = parents.get(parentId);
            if (parent != null) {
                parent.addChild(tree);
            }

            parents.put(node.getId(), tree);
        }
        return root == null && !parents.isEmpty() ? parents.values().iterator().next() : root;
    }

    private Tree() {
    }

    public Tree(Node node) {
        this.node = node;
    }

    public Tree getParent() {
        return parent;
    }

    public Tree setParent(Tree parent) {
        this.parent = parent;
        return this;
    }

    public List<Tree> getChildren() {
        return children;
    }

    Tree setChildren(List<Tree> children) {
        this.children = children;
        return this;
    }

    public Tree addChild(Tree tree) {
        this.children.add(tree);
        tree.setParent(this);
        return this;
    }

    public Tree addChild(Tree tree, int order) {
        // Remove the child if it already exists
        if (children.indexOf(tree) >= 0) {
            children.remove(tree);
        }

        // Update the 'order' properties of any children
        for (Tree child : children) {
            Node node = child.getNode();
            if (node.getOrder() >= order) {
                node.setOrder(node.getOrder() + 1);
            }
        }

        tree.getNode().setOrder(order);
        this.children.add(order, tree);
        return this;
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }

    public Node getNode() {
        return node;
    }

    private Tree setNode(Node node) {
        this.node = node;
        return this;
    }

    public Tree addNode(Node node) {
        children.add(new Tree().setNode(node).setParent(this));
        return this;
    }

    public Tree removeTree(Tree tree) {
        children.remove(tree);
        return this;
    }

    public String createPath() {
        List<String> parts = new ArrayList<>(Arrays.asList(node.getName()));
        for (Tree root = this.getParent(); root != null; root = root.getParent()) {
            parts.add(root.getNode().getName());
        }
        return Joiner.on(".").join(Lists.reverse(parts));
    }

    private int siblingIndex() {
        if (parent == null) {
            return 0;
        }

        return parent.getChildren().indexOf(this) + 1;
    }

    public String getLevel() {
        return parent == null ? "" : (Joiner.on(".").skipNulls().join(StringUtils.stripToNull(parent.getLevel()), siblingIndex()));
    }

    public Integer getDepth() {
        return parent == null || !parent.getClass().equals(this.getClass()) ? 0 : (parent.getDepth() + 1);
    }

    @Override
    public String toString() {
        return createPath();
    }

    public String prettyPrint() {
        return prettyPrint(this, "", true).trim();
    }

    private String prettyPrint(Tree tree, String prefix, boolean isTail) {
        StringBuilder stringBuilder = new StringBuilder(prefix).append((isTail ? "└── " : "├── ")).append(tree.node.getName()).append("\n");

        for (Iterator<Tree> iterator = tree.children.iterator(); iterator.hasNext(); ) {
            stringBuilder.append(prettyPrint(iterator.next(), prefix + (isTail ? "    " : "│   "), !iterator.hasNext()));
        }
        return stringBuilder.toString();
    }

    public List<Tree> toList() {
        return toList(this, new ArrayList<Tree>());
    }

    List<Tree> toList(Tree tree, List<Tree> allNodes) {
        allNodes.add(tree);
        for (Tree child : tree.getChildren()) {
            toList(child, allNodes);
        }
        return allNodes;
    }

    /*
     * NOTE: this will find the first leftmost element with the specified name if there are multiple elements with the same name.
     */
    public Tree findTree(String elementName) {
        return findTree(this, elementName);
    }

    Tree findTree(Tree currentNode, String elementName) {
        if (currentNode.getNode().getName().equals(elementName)) {
            return currentNode;
        }
        for (Tree child : currentNode.children) {
            Tree subtree = findTree(child, elementName);
            if (subtree != null) {
                return subtree;
            }
        }
        return null;
    }

    public Tree findById(Integer id) {
        return id != null ? findById(this, id) : null;
    }

    private Tree findById(Tree currentNode, Integer id) {
        if (currentNode.getId().equals(id)) {
            return currentNode;
        }
        for (Tree child : currentNode.children) {
            Tree node = findById(child, id);
            if (node != null) {
                return node;
            }
        }
        return null;
    }

    public void move(final Tree newParent, int order) {
        if (newParent == null) throw new AssertionError("newParent can not be null");

        this.getParent().getChildren().remove(this);
        newParent.addChild(this, order);
    }

    public Tree findRoot() {
        Tree root = this;
        for (Tree parent = this.getParent(); parent != null; parent = parent.getParent()) {
            root = parent;
        }
        return root;
    }

    public List<Tree> findLeaves() {
        return findLeaves(this, new ArrayList<Tree>());
    }

    List<Tree> findLeaves(Tree tree, List<Tree> leaves) {
        if (!tree.hasChildren()) {
            leaves.add(tree);
        }
        for (Tree child : tree.getChildren()) {
            findLeaves(child, leaves);
        }
        return leaves;
    }

    public <T extends Node> List<Tree> findNodesOfType(Class<T> type) {
        return findNodesOfType(type, null);
    }

    @SuppressWarnings("unchecked")
    public <T extends Node> List<Tree> findNodesOfType(Class<T> type, List<Tree> initialSearch) {
        Stack<Tree> stack = new Stack<>();
        List<Tree> nodes = getNode().getClass().equals(type) ? Lists.newArrayList(this) : new ArrayList<Tree>();

        if (initialSearch == null) {
            initialSearch = getChildren();
        }

        stack.addAll(initialSearch);
        while (!stack.isEmpty()) {
            Tree current = stack.pop();
            if (current.getNode().getClass().equals(type)) {
                nodes.add(current);
            }
            stack.addAll(current.getChildren());
        }
        return nodes;
    }

}
