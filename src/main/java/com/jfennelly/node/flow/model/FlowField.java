package com.jfennelly.node.flow.model;

import com.jfennelly.node.flow.NodeFlowRunner;
import com.jfennelly.node.flow.constants.SceneConstants;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.List;

import static processing.core.PConstants.TWO_PI;

/**
 * Utility class to encapsulate functionality surrounding the generation of the
 * particle vector field. The particle vector field defines the direction and
 * intensity a particle will be pushed based on the local position of the particle.
 * Currently, the field is generated using perlin noise.
 */
public class FlowField {
    public float perlinZOffset = 0;
    public int cols, rows;
    public List<PVector> grid;

    public FlowField() {
        this.cols = (int) Math.floor((double) SceneConstants.SCREEN_WIDTH / SceneConstants.FLOW_FIELD_CELL_SCALE);
        this.rows = (int) Math.floor((double) SceneConstants.SCREEN_HEIGHT / SceneConstants.FLOW_FIELD_CELL_SCALE);
        grid = new ArrayList<>();
    }

    /**
     * Generates a 2D Grid of vectors to create a flow field. Utilizes perlin noise
     * to ease randomness throughout the field.
     */
    public void generateField() {
        this.grid.clear();
        NodeFlowRunner pc = NodeFlowRunner.getInstance();
        float perlinIncrement = 0.1F;
        for (int y = 0; y < this.rows; y++) {
            for (int x = 0; x < this.cols; x++) {
                this.grid.add(new PVector());
                int index = (x + y * this.cols);
                float angle = (float) pc.perlin_noise(x * perlinIncrement, y * perlinIncrement, perlinZOffset) * TWO_PI;
                PVector v = PVector.fromAngle(angle);
                v.setMag(SceneConstants.FLOW_FIELD_VECTOR_MAGNITUDE);
                this.grid.set(index, v);
            }
        }
        perlinZOffset += perlinIncrement / 50;
    }
}
