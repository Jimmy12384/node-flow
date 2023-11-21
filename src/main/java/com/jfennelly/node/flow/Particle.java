package com.jfennelly.node.flow;

import com.jfennelly.node.flow.constants.SceneConstants;
import com.jfennelly.node.flow.model.FlowField;
import lombok.Data;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.List;


/**
 * Data class to hold data on a single particle. This includes velocity, acceleration,
 * nearby particles, and the base color of a particle in RGB format.
 */
@Data
public class Particle {
    public int[] color = {255, 255, 255}; // RGB format: {R, G, B}
    int maxVelocity = 1;
    private PVector position;
    private PVector velocity;
    private PVector acceleration;
    private ArrayList<Particle> connectedParticles = new ArrayList<>();

    public Particle(int x, int y) {
        this.position = new PVector(x, y);
        this.velocity = new PVector();
        this.acceleration = new PVector();
    }

    /**
     * calculates the distance between two particles using a rearrangement of the pythagorean theorem
     */
    public static double getDistance(Particle p1, Particle p2) {
        return (Math.sqrt(Math.pow((p1.position.x - p2.position.x), 2) + Math.pow((p1.position.y - p2.position.y), 2)));
    }

    /**
     * Iterate a particle by a single frame.
     */
    public void iterate() {
        this.position.add(this.velocity);
        this.velocity.limit(maxVelocity);
        this.velocity.add(this.acceleration);
        this.acceleration.mult(0);
    }

    /**
     * Teleport a particle to the opposite screen edge when reaching an existing
     * edge.
     */
    public void checkEdges() {
        int width = SceneConstants.SCREEN_WIDTH;
        int height = SceneConstants.SCREEN_HEIGHT;
        if (this.position.x > width) {
            this.position.x = 0;
        }
        if (this.position.x < 0) {
            this.position.x = width;
        }
        if (this.position.y > height) {
            this.position.y = 0;
        }
        if (this.position.y < 0) {
            this.position.y = height;
        }
    }

    /**
     * apply a force over a single frame
     */
    public void applyForce(PVector force) {
        this.acceleration.add(force);
    }

    /**
     * Based on a supplied flow field, apply a force to a particle based on the
     * nearest force vector in the flow field. Additionally, prevent clotting by
     * pushing away from connected particles.
     */
    public void follow(FlowField flowField) {
        int x = (int) Math.floor(this.position.x / SceneConstants.FLOW_FIELD_CELL_SCALE);
        int y = (int) Math.floor(this.position.y / SceneConstants.FLOW_FIELD_CELL_SCALE);
        int index = x + y * flowField.cols;
        if (index < 0 || index >= flowField.grid.size()) return;
        PVector force = flowField.grid.get(index);
        this.applyForce(force);
        for (Particle connectedParticle : connectedParticles) {
            if (getDistance(this, connectedParticle) < 25) {
                PVector v = PVector.fromAngle(PVector.angleBetween(this.position, connectedParticle.getPosition()));
                v.setMag((float) 10);
                this.applyForce(v);
            }
        }
    }

    /**
     * Find the farthest connected particle. draw a triangle with opacity with each combination of left over particle tuples
     */
    public void fillTriangles(List<Particle[]> triangles) {
        double farthestDist = 0;
        Particle farthestParticle = null;
        for (Particle p : connectedParticles) {
            double dist = getDistance(p, this);
            if (dist > farthestDist) {
                farthestDist = dist;
                farthestParticle = p;
            }
        }
        for (Particle p : connectedParticles) {
            triangles.add(new Particle[]{this, p, farthestParticle});
        }
    }
}
