/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import master.DTOMaster;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import slave.DTOSlave;

/**
 *
 * @author 1150829 & 1150831 & 1190109
 * Classe criada para transformar o json dos pedidos de calculo lido em informação útil
 */
public class JsonDecoding {

    public DTOSlave jsonDecoder(String json) {

        JSONParser parser = new JSONParser();

        try {

            Object obj = parser.parse(json);

            JSONObject jsonObject = (JSONObject) obj;
            
            DTOSlave dto = new DTOSlave();
            
            dto.setOp((String) jsonObject.get("operation"));
            
            JSONArray vec = (JSONArray) jsonObject.get("vector");
            
            int [] aux = new int[vec.size()];
            
            for (int i = 0; i < vec.size(); i++) {
                aux[i] = Math.toIntExact((long) vec.get(i));
            }
            
            dto.setJson(aux);

            return dto;

        } catch (ParseException pe) {
            System.out.println("position: " + pe.getPosition());
            System.out.println(pe);
        }

        return null;
    }
    
    public DTOMaster jsonDecoderMaster(String json) {

        JSONParser parser = new JSONParser();

        try {

            Object obj = parser.parse(json);

            JSONObject jsonObject = (JSONObject) obj;
            
            DTOMaster dto = new DTOMaster();
            
            dto.setOp((String) jsonObject.get("operation"));
            
            JSONArray vec = (JSONArray) jsonObject.get("vector");
            
            int [] aux = new int[vec.size()];
            
            for (int i = 0; i < vec.size(); i++) {
                aux[i] = Math.toIntExact((long) vec.get(i));
            }
            
            dto.setJson(aux);

            return dto;

        } catch (ParseException pe) {
            System.out.println("position: " + pe.getPosition());
            System.out.println(pe);
        }

        return null;
    }
}
