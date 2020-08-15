/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package master;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import slave.Slave;
import utils.SlaveImport;

/**
 *
 * @author 1150829 & 1150831 & 1190109
 */
public class MainMaster {

    /**
     * @param args the command line arguments É criada uma threadpool com um
     * número de threads estabelecido no ficheiro Foi criada um server para
     * depois receber pedidos
     */
    public static void main(String[] args) throws MalformedURLException, IOException {

        SlaveImport slaveImport;
        Scanner read = new Scanner(System.in);
        System.out.println("\nBem-vindo: \n Deseja preencher dados manualmente? Responda s para sim e n para nao");
        String aux = read.next();
        if (aux.equalsIgnoreCase("s") || aux.equalsIgnoreCase("sim") || aux.equalsIgnoreCase("y")) {
            //Texto para manulamente editar
            slaveImport = manuallyInsert(read);
        } else {
            //Lê json
            slaveImport = new SlaveImport("src/main/java/master/slave_list.json");
        }

        ArrayList<HttpServer> al = new ArrayList<>();

        for (SlaveOp arg : slaveImport.slaveList) {

            ExecutorService executor = Executors.newFixedThreadPool(arg.getPerformance_index());
            ResourceConfig config = new ResourceConfig();
            config.register(new Slave(executor, arg));
            try {
                HttpServer hs = JdkHttpServerFactory.createHttpServer(URI.create(arg.getUrl()), config);
                al.add(hs);
                System.out.println("COMECOU: " + arg.getUrl());
            } catch (Exception e) {
                throw e;
            }
        }

        ExecutorService executor = Executors.newFixedThreadPool(slaveImport.numberOfThreads);
        ResourceConfig config = new ResourceConfig();
        config.register(new Master(executor, slaveImport));

        try {
            JdkHttpServerFactory.createHttpServer(URI.create(slaveImport.url), config);
        } catch (Exception e) {
            throw e;
        }

        stopSlave(read, al);
    }

    private static SlaveImport manuallyInsert(Scanner read) {

        System.out.println("\nQuantas threads deseja disponibilizar para o master?");
        int n_threads_master = Integer.parseInt(read.next());
        System.out.println("\nQual devera ser o port deste master?");
        String master_url = "http://localhost:" + read.next() + "/";

        SlaveImport si = new SlaveImport(n_threads_master, master_url);

        System.out.println("\nQuantos slaves deseja adicionar?");
        int number_slaves = Integer.parseInt(read.next());

        slaveConfiguration(read, number_slaves, si);

        return si;

    }

    private static void slaveConfiguration(Scanner read, int n_slaves, SlaveImport slaveImport) {

        for (int i = 0; i < n_slaves; i++) {
            System.out.println("\nQual devera ser o port para o slave" + (i + 1) + "?");
            String slave_url = "http://localhost:" + read.next() + "/";
            System.out.println("\nQuantas particoes devera ter o slave" + (i + 1) + "?");
            int n_partitions = Integer.parseInt(read.next());
            System.out.println("\nQual devera ser o indice de performance do slave" + (i + 1) + "?");
            int performance_index = Integer.parseInt(read.next());

            slaveImport.addSlave(slave_url, n_partitions, performance_index);
        }
    }

    private static void stopSlave(Scanner read, ArrayList<HttpServer> al) {

        System.out.println("\nLista de slaves a parar: (Se quiser abortar este processo escreva stop)");

        for (HttpServer hs : al) {
            System.out.println("\n- Slave com port " + hs.getAddress().getPort());
        }

        System.out.println("\nQual o port do slave a parar?");

        String aux = read.next();

        while (!aux.equalsIgnoreCase("stop")) {
            int port = Integer.parseInt(aux);

            HttpServer toRemove = null;
            for (HttpServer hs : al) {
                if (hs.getAddress().getPort() == port) {
                    hs.stop(0);
                    toRemove = hs;
                    System.out.println("Parou o slave com o port: " + hs.getAddress().getPort());
                }
            }
            al.remove(toRemove);
            aux = read.next();
        }

        do {
            System.out.println("\nPara parar o programa digite sair.");
            aux = read.next();
        } while (!aux.equalsIgnoreCase("sair"));
            System.exit(0);        
    }
}
