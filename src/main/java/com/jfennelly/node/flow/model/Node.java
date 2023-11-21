package com.jfennelly.node.flow.model;

import com.jfennelly.node.flow.Particle;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Basic data class to hold data on a particle inside of a Quad Tree.
 */
@AllArgsConstructor
@Data
public class Node {
    private float x;
    private float y;
    private Particle particle;
}
