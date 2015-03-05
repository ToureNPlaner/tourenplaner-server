/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tourenplaner.graphrep;

/**
 *
 * @author storandt
 */
class TypeLine {
    int p1ID, p2ID;
    int type;
    boolean bi;

    public TypeLine(int p1ID, int p2ID, int type) {
        this.p1ID = p1ID;
        this.p2ID = p2ID;
        this.type = type;
        bi = true;
    }

    public void setParameters(int p1ID, int p2ID, int type) {
        this.p1ID = p1ID;
        this.p2ID = p2ID;
        this.type = type;
        bi = true;
    }
    
    public void display() {
        System.out.println(p1ID + " " + p2ID + " " + type);
    }
    
    
}
