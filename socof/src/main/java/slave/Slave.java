/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package slave;

import utils.JsonDecoding;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import master.SlaveOp;

/**
 *
 * @author 1150829 & 1150831 & 1190109
 */
@Path("slave")
public class Slave {

    private SlaveOp appSettings;
    private ExecutorService executor;
    
    public Slave(ExecutorService executor, SlaveOp app) {
        this.appSettings = app;
        this.executor = executor;
    }

    @GET
    @Produces("text/plain")
    @Path("/pong")
    //Devolver o indice de performance
    public int getPerformanceIndex(@Context Request request) throws InterruptedException, ExecutionException {
        CompletableFuture<Integer> cf = CompletableFuture.supplyAsync(() -> {
            return this.appSettings.getPerformance_index();
        }, executor);
        return cf.get();
    }

    @GET
    @Produces("text/plain")
    @Path("/ping")
    //Devolver o numero de threads disponiveis
    public int getAvailableThreads(@Context Request request) throws InterruptedException, ExecutionException {
        CompletableFuture<Integer> cf = CompletableFuture.supplyAsync(() -> {
            if (executor instanceof ThreadPoolExecutor) {
                return ((
                        this.appSettings.getPerformance_index()
                        - ((ThreadPoolExecutor) executor).getActiveCount()));
            }
            return 0;
        }, executor);
        return cf.get();
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("text/plain")

    public Long calculator(@Context Request request, String json) {
        
        JsonDecoding jd = new JsonDecoding();
        DTOSlave dto = jd.jsonDecoder(json);;

        int k = 0;
        int partition_size = 1;
        int npartitions = this.appSettings.getN_partitions();
        long res = 0;
        
        if (dto.getOp().equals("*")) {
            res = 1;
        }

        ArrayList<CompletableFuture<Long>> list = new ArrayList<CompletableFuture<Long>>();

        //Se o tamanho do vetor for inferior ao numero de partições definido,
        //o número de partições vai ser igual ao tamanho do vetor e cada partição terá 
        //1 elementos. Caso contrário, o número de partições é o numero definido no 
        //ficheiro appSettings.json e o tamanho de cada partição será o tamanho do 
        //vetor a dividir pelo número de partições
        
        if (dto.getJson().length < this.appSettings.getN_partitions()) {
            npartitions = dto.getJson().length;
        } else {
            partition_size = dto.getJson().length / this.appSettings.getN_partitions();
        }

        final int part_size = partition_size;
        //Resto da divisão, valor este que será correspondente ao número de partições
        //que terão mais 1 elemento
        
        int resto = (dto.getJson().length % this.appSettings.getN_partitions());
        
        for (int i = 0; i < npartitions; i++) {
            final int cfk = k;
            
            int end = cfk + part_size;
            
            //Se o resto for diferente de 0, é feito o incremento de um elemento
            //à partição e vamos decrementando o resto
            if(resto != 0) {
                end++;
                resto--;
            }
            
            final int aux = end;
            
            //A completablefuture permite completar automaticamente a task
            //Passa-se um executor para se definir uma threadpool com um numero de threads
            CompletableFuture<Long> cf = CompletableFuture.supplyAsync(() -> {
                if (dto.getOp().equals("+")) {
                    try {
                        return sumPart(cfk, aux, dto);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Slave.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else if (dto.getOp().equals("*")) {
                    try {
                        return multipPart(cfk, aux, dto);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Slave.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                return (long) 0;
            }, executor);
            list.add(cf);
            k = aux;
        }

        for (CompletableFuture<Long> future : list) {
            try {
                if (dto.getOp().equals("+")) {
                    res += future.get();
                } else if (dto.getOp().equals("*")) {
                    res *= future.get();
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(Slave.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ExecutionException ex) {
                Logger.getLogger(Slave.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        return res;
    }

    public Long sumPart(int beg, int end, DTOSlave dto) throws InterruptedException {
        long res = 0;
        int stop = end;
        //Método para prevenir que sejam lidas posições no vetor inexistentes
        if (dto.getJson().length < end) {
            stop = dto.getJson().length;
        }
        for (int i = beg; i < stop; i++) {
            res += dto.getJson()[i];
        }
        //O thread sleep está aqui com o propósito de teste, de forma a acompanhar o numero de threads disponiveis
        //Thread.sleep(8000);
        return res;
    }

    public Long multipPart(int beg, int end, DTOSlave dto) throws InterruptedException {

        long res = 1;
        int stop = end;
        //Método para prevenir que sejam lidas posições no vetor inexistentes
        if (dto.getJson().length < end) {
            stop = dto.getJson().length;
        }
        for (int i = beg; i < stop; i++) {
            res *= dto.getJson()[i];
        }
        //O thread sleep está aqui com o propósito de teste, de forma a acompanhar o numero de threads disponiveis
        //Thread.sleep(8000);
        return res;
    }
}
