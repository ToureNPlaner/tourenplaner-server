/**
 * $$\\ToureNPlaner\\$$
 */
package server;

import algorithms.*;
import computecore.AlgorithmManagerFactory;
import computecore.AlgorithmRegistry;
import computecore.ComputeCore;
import computecore.SharingAMFactory;
import config.ConfigManager;
import database.DatabasePool;
import graphrep.GraphRep;
import graphrep.GraphRepDumpReader;
import graphrep.GraphRepTextReader;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;

import java.beans.PropertyVetoException;
import java.io.*;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TourenPlaner {

    private static Logger log = Logger.getLogger("tourenplaner");

    /**
     * @param graphName Original file name of the graph
     * @return The filename of the dumped graph
     */
    private static String dumpName(String graphName) {
        return graphName + ".dat";
    }

    private static Map<String, Object> getServerInfo(AlgorithmRegistry reg) {
        Map<String, Object> info = new HashMap<String, Object>(4);
        info.put("version", new Float(0.1));
        info.put("servertype", ConfigManager.getInstance().getEntryBool("private", false) ? "private" : "public");

        // when serverinfosslport is available then use that in the serverinfo, else use sslport
        int sslport = ConfigManager.getInstance().getEntryInt("serverinfosslport", 1) == 1 ?
                      ConfigManager.getInstance().getEntryInt("sslport", 8081) :
                      ConfigManager.getInstance().getEntryInt("serverinfosslport", 8081);

        info.put("sslport", sslport);
        // Enumerate Algorithms
        Collection<AlgorithmFactory> algs = reg.getAlgorithms();
        Map<String, Object> algInfo;
        List<Map<String, Object>> algList = new ArrayList<Map<String, Object>>();
        for (AlgorithmFactory alg : algs) {
            algInfo = new HashMap<String, Object>(5);
            algInfo.put("version", alg.getVersion());
            algInfo.put("name", alg.getAlgName());
            algInfo.put("urlsuffix", alg.getURLSuffix());
            if (alg instanceof GraphAlgorithmFactory) {
                algInfo.put("pointconstraints", ((GraphAlgorithmFactory) alg).getPointConstraints());
                algInfo.put("constraints", ((GraphAlgorithmFactory) alg).getConstraints());
                algInfo.put("details", ((GraphAlgorithmFactory) alg).getDetails());
            }
            algList.add(algInfo);
        }
        info.put("algorithms", algList);

        return info;
    }

    /**
     * This is the main class of ToureNPlaner. It passes CLI parameters to the
     * handler and creates the httpserver
     */
    public static void main(String[] args) {
        // TODO remove when not needed as default
        Logger.getLogger("algorithms").setLevel(Level.FINEST);
        Logger.getLogger("server").setLevel(Level.FINEST);
        // Create ObjectMapper so we reuse it's data structures
        ObjectMapper mapper = new ObjectMapper();
        // TODO: check if we really want to enable comments since it's a nonstandard feature of JSON
        mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        GraphRep graph = null;
        String graphfilename;

        CLIParser cliParser = new CLIParser(args);
        if (cliParser.getConfigFilePath() != null) {
            try {
                ConfigManager.init(mapper, cliParser.getConfigFilePath());
            } catch (Exception e) {
                // ConfigManager either didn't like the path or the config file at the path
                log.severe("Error reading configuration file from file: " + cliParser.getConfigFilePath() + "\n" +
                e.getMessage() + "\n" +
                "Using builtin configuration...");
            }
        } else {
            log.severe("Usage: \n\tjava -jar tourenplaner-server.jar -c \"config file\" " +
                       "[-f dump|text] [dumpgraph]\nDefaults are: builtin configuration, -f text");
        }
        ConfigManager cm = ConfigManager.getInstance();
        graphfilename = cm.getEntryString("graphfilepath", System.getProperty("user.home") + "/germany-ch.txt");

        // now that we have a config (or not) we look if we only need to dump our graph and then exit
        if (cliParser.dumpgraph()) {
            log.info("Dumping Graph...");
            try {
                graph = new GraphRepTextReader().createGraphRep(new FileInputStream(graphfilename));
                utils.GraphSerializer.serialize(new FileOutputStream(dumpName(graphfilename)), graph);
            } catch (IOException e) {
                log.severe("IOError dumping graph to file: " + graphfilename + "\n" + e.getMessage());
            } finally {
                System.exit(0);
            }
        }

        //TODO there's an awful lot of duplicate logic and three layers of exception throwing code wtf
        try {
            if (cliParser.loadTextGraph()) {
                graph = new GraphRepTextReader().createGraphRep(new FileInputStream(graphfilename));
            } else {
                try {
                    graph = new GraphRepDumpReader().createGraphRep(new FileInputStream(dumpName(graphfilename)));
                } catch (InvalidClassException e) {
                    log.warning("Dumped Graph version does not match the required version: " + e.getMessage());
                    log.info("Falling back to text reading from file: " + graphfilename + " (path provided by config file)");
                    graph = new GraphRepTextReader().createGraphRep(new FileInputStream(graphfilename));
                    

                    if (graph != null && new File(dumpName(graphfilename)).delete()) {
                        log.info("Graph successfully read. Now replacing old dumped graph");
                        try {
                            utils.GraphSerializer.serialize(new FileOutputStream(dumpName(graphfilename)), graph);
                        }catch(IOException e1){
                            log.warning("writing dump failed (but graph loaded):\n" + e1.getMessage());
                        }
                    }
                } catch (IOException e) {
                    log.log(Level.WARNING, "loading dumped graph failed", e);
                    log.info("Falling back to text reading from file " + graphfilename + " (path provided by config file)");
                    graph = new GraphRepTextReader().createGraphRep(new FileInputStream(graphfilename));
                    log.info("Graph successfully read. Now writing new dump");
                    utils.GraphSerializer.serialize(new FileOutputStream(dumpName(graphfilename)), graph);
                }
            }
        } catch (IOException e) {
            log.log(Level.SEVERE, "loading text graph failed", e);
        }

        if (graph == null) {
            log.severe("Reading graph failed");
            System.exit(1);
        }

        // Register Algorithms
        AlgorithmRegistry reg = new AlgorithmRegistry();
        // reg.registerAlgorithm(new ShortestPathFactory(graph));
        reg.registerAlgorithm(new ShortestPathCHFactory(graph));
        reg.registerAlgorithm(new TravelingSalesmenFactory(graph));
        reg.registerAlgorithm(new NNSearchFactory(graph));
        reg.registerAlgorithm(new ConstrainedSPFactory(graph));

        // initialize DatabasePool
        if (cm.getEntryBool("private", false)) {
            try {
                DatabasePool.getDatabaseManager(
                        cm.getEntryString("dburi", "jdbc:mysql://localhost:3306/tourenplaner?autoReconnect=true"),
                        cm.getEntryString("dbuser", "tnpuser"),
                        cm.getEntryString("dbpw", "toureNPlaner"),
                        cm.getEntryString("dbdriverclass", "com.mysql.jdbc.Driver")).getNumberOfUsers();
            } catch (PropertyVetoException e) {
                log.log(Level.SEVERE, "Couldn't establish database connection", e);
                System.exit(1);
            } catch (SQLException e) {
                log.log(Level.SEVERE, "Couldn't establish database connection. DatabaseManager.getNumberOfUsers() was called " +
                        "to test and initialize Pool, but an SQLException was thrown", e);
                System.exit(1);
            }
        }


        // Create our ComputeCore that manages all ComputeThreads
        ComputeCore comCore = new ComputeCore(reg, cm.getEntryInt("threads", 16), cm.getEntryInt("queuelength", 32));
        AlgorithmManagerFactory amFac = new SharingAMFactory(graph);
        comCore.start(amFac);




        // Create ServerInfo object
        Map<String, Object> serverInfo = getServerInfo(reg);

        new HttpServer(cm, reg, serverInfo, comCore);
    }

}
