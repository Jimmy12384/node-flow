package com.jfennelly.node.flow;

import com.jfennelly.node.flow.constants.SceneConstants;
import com.jfennelly.node.flow.model.SquareBoundary;
import com.jfennelly.node.flow.model.FlowField;
import com.jfennelly.node.flow.model.Node;
import com.jfennelly.node.flow.model.QuadTree;
import processing.core.PApplet;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class NodeFlowRunner extends PApplet {

    private static NodeFlowRunner instance;
    private final List<Particle[]> triangles = new ArrayList<>();
    // Set up video variables
    int videoFramesCaptured = 0;
    List<Particle> particles = new ArrayList<>();
    int timeDifferential = 0;
    boolean debounced = false;
    FlowField flowField = new FlowField();

    public static NodeFlowRunner getInstance() {
        if (instance == null) {
            instance = new NodeFlowRunner();
        }
        return instance;
    }

    public static void main(String[] args) {
        PApplet.main("com.jfennelly.node.flow.NodeFlowRunner", args);
    }

    public double perlin_noise(float x, float y, float z) {
        return noise(x, y, z);
    }

    public void settings() {
        size(SceneConstants.SCREEN_WIDTH, SceneConstants.SCREEN_HEIGHT, P2D);
    }

    public void setup() {
        for (int i = 0; i < SceneConstants.TOTAL_POINTS; i++) {
            Particle p = new Particle((int) random(0, width), (int) random(0, height));
            particles.add(p);
        }
        flowField.generateField();
        background(32);
    }

    public void draw() {
        background(32, 1);
        frameRate(SceneConstants.VIDEO_FRAME_RATE);

        SquareBoundary bound = new SquareBoundary(0, 0, width, height);
        QuadTree qtree = new QuadTree(bound, 4);
        if (timeDifferential % 500 == 0) {
            flowField.generateField();
        }
        for (Particle p : particles) {
            p.getConnectedParticles().clear();
            p.follow(flowField);
            p.iterate();
            p.checkEdges();
            p.fillTriangles(triangles);
            SquareBoundary boundCheck = new SquareBoundary(p.getPosition().x, p.getPosition().y, SceneConstants.MAX_RADIUS * 2, SceneConstants.MAX_RADIUS * 2);
            ArrayList<Node> foundNodes = qtree.getAllInBoundary(boundCheck);
            for (int i = 0; i < foundNodes.size(); i++) {
                drawEdges(p, foundNodes);
                drawTriangles(p, foundNodes);
            }
            drawParticle(p);
            qtree.insert(new Node(p.getPosition().x, p.getPosition().y, p));
        }
        int maxParticles = 2000;
        if (mousePressed && particles.size() < maxParticles) {
            if (!debounced) {
                debounced = true;
                CompletableFuture.delayedExecutor(250, TimeUnit.MILLISECONDS).execute(() -> {
                    debounced = false;
                });
                float nodeX = mouseX + random(-25, 25);
                float nodeY = mouseY + random(-25, 25);
                Particle tmpParticle = new Particle((int) nodeX, (int) nodeY);
                particles.add(tmpParticle);
                Node tmpNode = new Node(nodeX, nodeY, tmpParticle);
                qtree.insert(tmpNode);
            }
        }

        if (SceneConstants.SHOW_QUAD_TREE) {
            showQuadTree(qtree);
        }

        if (SceneConstants.SHOW_FLOW_FIELD) {
            showFlowField(flowField);
        }

        timeDifferential++;
        if (SceneConstants.RECORD_VIDEO) {
            saveFrame("../export/####-export.tga");
            if (videoFramesCaptured > SceneConstants.VIDEO_FRAME_RATE * SceneConstants.SECONDS_TO_CAPTURE) {
                SceneConstants.RECORD_VIDEO = false;
                videoFramesCaptured = 0;
            } else {
                videoFramesCaptured++;
            }

            pushStyle();
            noFill();
            strokeWeight(2);
            stroke(255, 0, 0);
            rect(0, 0, width, height);
            popStyle();
        }
    }

    private void drawEdges(Particle p, ArrayList<Node> nearbyNodes) {
        int count = 0;
        strokeWeight(SceneConstants.EDGE_WEIGHT);
        for (Node near : nearbyNodes) {
            Particle nearPart = near.getParticle();
            if (count > SceneConstants.MAX_CONNECTIONS || nearPart.getConnectedParticles().contains(p))
                return;
            float alpha = map((float) Particle.getDistance(p, nearPart), 0, (float) (SceneConstants.MAX_RADIUS * 2.5), (float) 0, (float) 1.0);
            alpha = 1 - alpha;
            alpha *= 100;
            stroke(255, alpha);
            line(p.getPosition().x, p.getPosition().y, nearPart.getPosition().x, nearPart.getPosition().y);
            p.getConnectedParticles().add(nearPart);
            count++;
        }
    }

    public void drawTriangles(Particle p1, ArrayList<Node> nearbyNodes) {
        if (nearbyNodes.size() >= 2) {
            double farthestDist = 0;
            Node farthestNode = nearbyNodes.get(0);
            Particle p2;
            for (Node p : nearbyNodes) {
                double dist = Particle.getDistance(p.getParticle(), p1);
                if (dist > farthestDist) {
                    farthestDist = dist;
                    farthestNode = p;
                }
            }
            nearbyNodes.remove(farthestNode);
            p2 = farthestNode.getParticle();
            for (Node p : nearbyNodes) {
                Particle p3 = p.getParticle();
                float area = Math.abs((p1.getPosition().x * (p2.getPosition().y - p3.getPosition().y) + p2.getPosition().x * (p3.getPosition().y - p1.getPosition().y) + p3.getPosition().x * (p1.getPosition().y - p2.getPosition().y)) / 2);
                float normalizedArea = map(area, 0, 900, 100, 0);
                pushMatrix();
                beginShape();
                fill(165, 64, 45, normalizedArea);
                vertex(p1.getPosition().x, p1.getPosition().y);
                vertex(p2.getPosition().x, p2.getPosition().y);
                vertex(p3.getPosition().x, p3.getPosition().y);
                endShape();
                popMatrix();
            }
        }
    }

    private void showFlowField(FlowField flowField) {
        for (int y = 0; y < flowField.rows; y++) {
            for (int x = 0; x < flowField.cols; x++) {
                int index = x + y * flowField.cols;
                strokeWeight(1);
                stroke(255, 50);
                pushMatrix();
                translate(x * SceneConstants.FLOW_FIELD_CELL_SCALE, y * SceneConstants.FLOW_FIELD_CELL_SCALE);
                rotate(flowField.grid.get(index).heading());
                line(0, 0, SceneConstants.FLOW_FIELD_CELL_SCALE, 0);
                popMatrix();
            }
        }
    }

    private void drawParticle(Particle p) {
        stroke(p.color[0], p.color[1], p.color[2]);
        strokeWeight(SceneConstants.NODE_WEIGHT);
        point(p.getPosition().x, p.getPosition().y);
    }

    private void showQuadTree(QuadTree quadTree) {
        stroke(0, 255, 0, 25);
        strokeWeight(1);
        noFill();
        rectMode(CENTER);
        rect(quadTree.getBoundary().getX(), quadTree.getBoundary().getY(), quadTree.getBoundary().getWidth() * 2, quadTree.getBoundary().getHeight() * 2);
        if (quadTree.isDivided()) {
            showQuadTree(quadTree.getNorthEast());
            showQuadTree(quadTree.getNorthWest());
            showQuadTree(quadTree.getSouthEast());
            showQuadTree(quadTree.getSouthWest());
        }
    }
}
