/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import master.SlaveOp;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author 1150829 & 1150831 & 1190109
 */

public class SlaveImport {

    public JSONObject[] array;
    public List<SlaveOp> slaveList = new ArrayList<>();
    public int numberOfThreads;
    public String url;
    
    public SlaveImport(String filePath) {

        JSONParser parser = new JSONParser();
        
        try {
            Object obj = parser.parse(new FileReader(filePath));
            
            JSONObject jsonObject = (JSONObject) obj;
            JSONArray jsonArray = (JSONArray) jsonObject.get("list");
            
            this.numberOfThreads = Math.toIntExact((long) jsonObject.get("NumberOfThreads"));
            this.url = (String) jsonObject.get("Url");

            if (jsonArray != null) {
                for (Object object : jsonArray) {
                    this.slaveList.add(new SlaveOp(object));
                }
            }
        } catch (Exception e) {
            System.out.println("FILE NOT FOUND");
            System.out.print(e);
        }
    }

    public SlaveImport(int n_threads_master, String master_url) {
        this.numberOfThreads = n_threads_master;
        this.url = master_url;
    }
    
    public void addSlave(String url, int n_partitions, int performance_index) {
        SlaveOp slave = new SlaveOp(""+this.slaveList.size(),url, n_partitions, performance_index);
        this.slaveList.add(slave);
    }

    public List<SlaveOp> getSlaveList() {
        return slaveList;
    } 
}

