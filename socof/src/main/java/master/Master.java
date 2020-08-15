/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package master;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import utils.JsonDecoding;
import utils.SlaveImport;

/**
 *
 * @author 1150829 & 1150831 & 1190109
 */
@Path("master")
public class Master {

    private ExecutorService executor;
    private SlaveImport slaveImport;

    public Master(ExecutorService executor, SlaveImport slaveImport) {
        this.executor = executor;
        this.slaveImport = slaveImport;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("text/plain")
    public int test(@Context Request request, String json) {

        JsonDecoding jd = new JsonDecoding();
        DTOMaster dto = jd.jsonDecoderMaster(json);

        HashMap<String, Integer> listPerfIndexs = getSlavesAvailability();

        int totalIndexs = getTotalPerformance(listPerfIndexs);

        HashMap<String, Integer> numbersBySlave = getNumberOfPositionsBySlave(listPerfIndexs, totalIndexs, dto.getJson().length);

        int finalResult = callSlaves(numbersBySlave, dto.getJson(), dto.getOp());

        return finalResult;

    }

    private HashMap<String, Integer> getSlavesAvailability() {

        HashMap<String, Integer> listPerfIndexs = new HashMap<>();

        for (SlaveOp slave : slaveImport.getSlaveList()) {
            URL url = null;
            HttpURLConnection conn;
            try {
                url = new URL(slave.getUrl() + "slave/ping");
                conn = (HttpURLConnection) url.openConnection();
                conn.setFollowRedirects(false);
                conn.setConnectTimeout(1 * 1000);

                StringBuilder result = new StringBuilder();
                conn.setRequestMethod("GET");

                if (conn.getResponseCode() != 200) {
                    System.out.println("Failed : HTTP error code : " + conn.getResponseCode());
                    continue;
                }

                BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                while ((line = rd.readLine()) != null) {
                    result.append(line);
                }
                rd.close();
                listPerfIndexs.put(slave.getUrl(), Integer.parseInt(result.toString()));

            } catch (Exception e) {
            }
        }

        return listPerfIndexs;
    }

    private int getTotalPerformance(HashMap<String, Integer> listPerfIndexs) {
        int totalIndex = 0;
        for (Integer indexes : listPerfIndexs.values()) {
            totalIndex += indexes;
        }
        return totalIndex;
    }

    private HashMap<String, Integer> getNumberOfPositionsBySlave(HashMap<String, Integer> listPerfIndexs, int totalIndexs, int vecSize) {

        HashMap<String, Integer> numberBySlave = new HashMap<>();

        String higherPerf = "";
        int auxNumber = 0;
        int soma = 0;

        for (Map.Entry<String, Integer> aux : listPerfIndexs.entrySet()) {
            int increm = (aux.getValue() * vecSize) / totalIndexs;
            soma += increm;
            numberBySlave.put(aux.getKey(), increm);
            if (auxNumber < aux.getValue()) {
                auxNumber = aux.getValue();
                higherPerf = aux.getKey();
            }
        }

        int resto = vecSize - soma;
        numberBySlave.put(higherPerf, numberBySlave.get(higherPerf) + resto);

        return numberBySlave;
    }

    private int callSlaves(HashMap<String, Integer> numbersBySlave, int[] vec, String op) {

        int finalResult = 0;
        int posInicial = 0;
        int posFinal = 0;

        //Lista que guarda os resultados dos slaves
        ArrayList<CompletableFuture<Integer>> list = new ArrayList<CompletableFuture<Integer>>();

        for (Map.Entry<String, Integer> aux : numbersBySlave.entrySet()) {
            posFinal = posInicial + aux.getValue();

            final int posFinalAux = posFinal;
            final int posInicialAux = posInicial;

            //A completablefuture permite completar automaticamente a task
            //Passa-se um executor para se definir uma threadpool com um numero de threads
            CompletableFuture<Integer> cf = CompletableFuture.supplyAsync(() -> {
                try {
                    return requestResult(aux.getKey(), vec, op, posInicialAux, posFinalAux);
                } catch (Exception ex) {
                    Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                    return retrySlaveCall(vec, op, posInicialAux, posFinalAux);
                }
            }, executor);
            list.add(cf);

            posInicial = posFinal;
        }

        int[] vecRes = new int[list.size()];
        int i = 0;

        for (CompletableFuture<Integer> future : list) {
            try {
                vecRes[i] = future.get();
                i++;
            } catch (InterruptedException ex) {
                Logger.getLogger(Master.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ExecutionException ex) {
                Logger.getLogger(Master.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (vecRes.length > 1) {
            finalResult = retrySlaveCall(vecRes, op, 0, vecRes.length);
        } else {
            finalResult = vecRes[0];
        }

        return finalResult;
    }

    private int retrySlaveCall(int[] vec, String op, int posInicial, int posFinal) {

        HashMap<String, Integer> updatedSlavesAvail = getSlavesAvailability();
        String keyUrl = updatedSlavesAvail.entrySet().stream().max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1).get().getKey();

        final int posFinalAux = posFinal;
        final int posInicialAux = posInicial;

        try {
            return requestResult(keyUrl, vec, op, posInicialAux, posFinalAux);
        } catch (Exception ex) {
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            return retrySlaveCall(vec, op, posInicial, posFinal);
        }
    }

    private int requestResult(String slaveUrl, int[] vec, String op, int posInicial, int posFinal) throws MalformedURLException, IOException {

        URL url = new URL(slaveUrl + "slave");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        String input = "{\"operation\" :\"" + op + "\",\"vector\":[";

        for (int i = posInicial; i < posFinal - 1; i++) {
            input = input + vec[i] + ",";
        }

        input = input + vec[posFinal - 1] + "]}";

        OutputStream os = conn.getOutputStream();
        os.write(input.getBytes());
        os.flush();

        if (conn.getResponseCode() != 200) {
            System.out.println("Failed : HTTP error code : " + conn.getResponseCode());
        }

        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));

        StringBuilder auxRes = new StringBuilder();
        String line;

        while ((line = rd.readLine()) != null) {
            auxRes.append(line);
        }
        rd.close();

        return Integer.parseInt(auxRes.toString());
    }
}
