/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tourenplaner.graphrep;

import com.carrotsearch.hppc.IntArrayList;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 *
 * @author storandt
 */
public final class PriorityGrid {
    
    private final GraphRep graph;

    
    public IntArrayList getNodeSelection(Rectangle2D.Double range, int priority) {
        System.out.println("view " + range);
        IntArrayList selectedNodeIDs = new IntArrayList();
        for (int i = 0; i < graph.getNodeCount(); i++) {
//            n.display();
            if (graph.getRank(i) < priority) {
//                System.out.println(n.getPriority() + " <  " + priority);
                continue;
            }
            if (graph.getXPos(i) < range.x || graph.getXPos(i) > range.x +range.width) {
//                System.out.println(n.getxPos() + " < " + x1 + " || " + n.getxPos() + " > " + x2);
                continue;
            }
            if (graph.getYPos(i) < range.y || graph.getYPos(i) > range.y+range.height) {
//                System.out.println(n.getyPos() + " < " + y1 + " || " + n.getyPos() + " > " + y2);
                continue;   
            }
//            System.out.println("add ID");
            selectedNodeIDs.add(i);
        }
        return selectedNodeIDs;
    }
    
    public IntArrayList getNodeSelection(Rectangle range, int minPriority, int maxPriority) {
        System.out.println("view " + range);
        IntArrayList selectedNodeIDs = new IntArrayList();
        for (int i = 0; i < graph.getNodeCount(); i++) {
//            n.display();
            if (graph.getRank(i) < minPriority || graph.getRank(i) >= maxPriority) {
//                System.out.println(n.getPriority() + " <  " + priority);
                continue;
            }
            if (graph.getXPos(i) < range.x || graph.getXPos(i) > range.x +range.width) {
//                System.out.println(n.getxPos() + " < " + x1 + " || " + n.getxPos() + " > " + x2);
                continue;
            }
            if (graph.getYPos(i) < range.y ||graph.getYPos(i) > range.y+range.height) {
//                System.out.println(n.getyPos() + " < " + y1 + " || " + n.getyPos() + " > " + y2);
                continue;   
            }
//            System.out.println("add ID");
            selectedNodeIDs.add(i);
        }
        return selectedNodeIDs;
    }
    
    public PriorityGrid(GraphRep graph) {
        this.graph = graph;
    }

    public double getDiff(int x1, int y1, int x2, int y2) {
        return Math.hypot(x2-x1, y2-y1);
    }
    
    int getNextNode(int x, int y, int P) {
        double minDiff = Double.MAX_VALUE;
        int id = -1;
        for (int i = 0; i < graph.getNodeCount(); i++) {
            if (graph.getRank(i) < P)
                continue;
            double diff = getDiff(x, y, graph.getXPos(i), graph.getYPos(i));
            if (diff < minDiff) {
                minDiff = diff;
                id = i;
            }
        }
        System.out.println("nearest node " + id + "  diff = " + minDiff);
        return id;
    }
    
   
}
