/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package master;

import org.json.simple.JSONObject;

/**
 *
 * @author 1150829 & 1150831 & 1190109
 */
public class SlaveOp {

    private String Url;
    private int n_partitions;
    private int performance_index;
    private String id;

    public SlaveOp(Object obj) {
        JSONObject jsonObject = (JSONObject) obj;
        this.Url = (String) jsonObject.get("url");
        this.id = (String) jsonObject.get("slaveId");
        this.n_partitions = Math.toIntExact((long) jsonObject.get("n_partitions"));
        this.performance_index = Math.toIntExact((long) jsonObject.get("performance_index"));
    }

    public SlaveOp(String id, String url, int n_partitions, int performance_index) {
        this.id = id;
        this.Url = url;
        this.n_partitions = n_partitions;
        this.performance_index = performance_index;
    }

    public String getUrl() {
        return Url;
    }

    public String getId() {
        return id;
    }   

    public int getN_partitions() {
        return n_partitions;
    }

    public int getPerformance_index() {
        return performance_index;
    }
    
}
