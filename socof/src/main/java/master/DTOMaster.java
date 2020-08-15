/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package master;

/**
 *
 * @author 1150829 & 1150831 & 1190109
 */
public class DTOMaster {

    private String op;
    private int [] json;

    
    public DTOMaster(String op, int[] json) {
        this.op = op;
        this.json = json;
    }

    public DTOMaster() {
    }

    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }

    public int[] getJson() {
        return json;
    }

    public void setJson(int[] json) {
        this.json = json;
    }
    
}
