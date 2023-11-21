package com.jfennelly.node.flow.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Boundary class used alongside QuadTrees to associate points contained within
 * the defined square boundary.
 */
@Data
@AllArgsConstructor
public class SquareBoundary {
    private float x;
    private float y;
    private float width;
    private float height;

    /**
     * determines and returns whether a node resides within the current box boundary
     */
    public boolean contains(Node p) {
        return (p.getX() >= this.x - this.width &&
                p.getX() <= this.x + this.width &&
                p.getY() >= this.y - this.height &&
                p.getY() <= this.y + this.height);
    }

    /**
     * determines and returns whether two box boundaries are intersecting.
     */
    public boolean intersects(SquareBoundary range) {
        return !(range.x - range.width > this.x + this.width ||
                range.x + this.width < this.x - this.width ||
                range.y - range.height > this.y + this.height ||
                range.y + this.height < this.y - this.height);
    }
}
