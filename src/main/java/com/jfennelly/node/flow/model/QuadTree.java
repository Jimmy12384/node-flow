package com.jfennelly.node.flow.model;

import lombok.Data;

import java.util.ArrayList;

/**
 * Data class to hold and store many points where locality search is often performed.
 * each node subdivide into further nodes to precisely query for points inside a
 * boundary.
 */
@Data
public class QuadTree {

    private final int capacity;
    private final ArrayList<Node> points;
    private QuadTree northEast;
    private QuadTree northWest;
    private QuadTree southEast;
    private QuadTree southWest;
    private SquareBoundary boundary;
    private boolean divided = false;

    public QuadTree(SquareBoundary bound, int cap) {
        this.boundary = bound;
        this.capacity = cap;
        this.points = new ArrayList<>();
    }

    public void insert(Node point) {
        if (!this.boundary.contains(point))
            return;
        if (this.points.size() < this.capacity) {
            this.points.add(point);
        } else {
            if (!this.divided) {
                this.subdivide();
            }
            this.northEast.insert(point);
            this.northWest.insert(point);
            this.southEast.insert(point);
            this.southWest.insert(point);
        }
    }

    /**
     * Recursively searches the Quad Tree to determine all points within a given
     * square boundary.
     */
    public ArrayList<Node> getAllInBoundary(SquareBoundary range) {
        ArrayList<Node> found = new ArrayList<>();

        if (!this.boundary.intersects(range))
            return found;
        else {
            for (Node p : this.points) {
                if (range.contains(p)) {
                    found.add(p);
                }
            }
        }

        if (this.divided) {
            found.addAll(this.northEast.getAllInBoundary(range));
            found.addAll(this.northWest.getAllInBoundary(range));
            found.addAll(this.southEast.getAllInBoundary(range));
            found.addAll(this.southWest.getAllInBoundary(range));
        }
        return found;
    }

    /**
     * subdivides the current boundary into four equal quad tree nodes
     */
    public void subdivide() {
        SquareBoundary NE = new SquareBoundary(boundary.getX() + boundary.getWidth() / 2, boundary.getY() - boundary.getHeight() / 2, boundary.getWidth() / 2, boundary.getHeight() / 2);
        SquareBoundary NW = new SquareBoundary(boundary.getX() - boundary.getWidth() / 2, boundary.getY() - boundary.getHeight() / 2, boundary.getWidth() / 2, boundary.getHeight() / 2);
        SquareBoundary SE = new SquareBoundary(boundary.getX() + boundary.getWidth() / 2, boundary.getY() + boundary.getHeight() / 2, boundary.getWidth() / 2, boundary.getHeight() / 2);
        SquareBoundary SW = new SquareBoundary(boundary.getX() - boundary.getWidth() / 2, boundary.getY() + boundary.getHeight() / 2, boundary.getWidth() / 2, boundary.getHeight() / 2);

        this.northEast = new QuadTree(NE, this.capacity);
        this.northWest = new QuadTree(NW, this.capacity);
        this.southEast = new QuadTree(SE, this.capacity);
        this.southWest = new QuadTree(SW, this.capacity);

        this.divided = true;
    }
}

