/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package inequalizer;

import inequalizer.petriNet.*;
import java.io.File;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Florian Furbach
 */
public class Inequalizer {
    
    static final public Logger log = Logger.getLogger(Inequality.class.getName());
    
    static Marking m0, mF;
    static Formula phi;
    private static boolean cover = false;
    private static boolean quant=false;
    private static boolean isBounded=true;
    private static int bound=1000;

    /**
     * If there are no program arguments the program is run on our example.
     *
     * @param args the command line arguments
     * @throws java.lang.Exception
     */
    public static void main(String[] args) throws Exception {
        Options options = new Options();
        
        Option fileOpt = new Option("f", "file", true, "target spec file");
        fileOpt.setRequired(false);
        options.addOption(fileOpt);
        
        Option coverOpt = new Option("c", "cover", false, "Set if the input is a coverability check.");
        coverOpt.setRequired(false);
        options.addOption(coverOpt);
        
        Option boundOpt = new Option("b", "bound", true, "Set a bound on the absolute values of entries in k");
        boundOpt.setRequired(false);
        options.addOption(boundOpt);
        
         Option minOpt = new Option("m", "min", false, "Set if minization should be used.");
        minOpt.setRequired(false);
        options.addOption(minOpt);       
        Option verbOpt = new Option("v", "verbose", false, "Set if more output is required.");
        verbOpt.setRequired(false);
        options.addOption(verbOpt);
        
        CommandLineParser parserCmd = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;
        
        try {
            cmd = parserCmd.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("Inequalizer", options);
            System.exit(1);
            return;
        }
        if (cmd.hasOption("cover")) {
            cover = true;
        }
        if (cmd.hasOption("min")) {
            Formula.isOpt = true;
        }
        if (cmd.hasOption("verbose")) {
            log.setLevel(Level.ALL);
        } else {
            log.setLevel(Level.OFF);
        }
        if (cmd.hasOption("bound")) {
            isBounded = true;
            bound = Integer.parseInt(cmd.getOptionValue("bound"));
        }
        if (cmd.hasOption("file")) {
            String filePath = cmd.getOptionValue("file");
            if (!filePath.endsWith("spec")) {
                System.out.println("Unrecognized Petri Net format");
                System.exit(0);
                return;
            }
            File modelfile = new File(filePath);
            
            String mcmtext = FileUtils.readFileToString(modelfile, "UTF-8");
            ANTLRInputStream mcminput = new ANTLRInputStream(mcmtext);
            PNLexer lexer = new PNLexer(mcminput);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            PNParser parser = new PNParser(tokens);
            //System.out.println((parser.mcm()).getText());
            //System.out.println(parser.mcm());
            PetriNet.setNet(parser.pn().netz);
        } else {
            if(cmd.hasOption("verbose"))
            {
//                log.setLevel(Level.OFF);
//                for (int i = 1; i < 100; i++) {
                    setExample(7);
//                }
//            System.exit(0);
            }
            else formatter.printHelp("Inequalizer", options);
        }
        
        log.info(PetriNet.getNet().toString());
        log.info("M0: " + m0.toString() + "\n");
        log.info("MF: " + mF.toString() + "\n");
//        System.out.println("#P: "+PetriNet.getNet().getSize()+" #T: "+PetriNet.getNet().getTransitions().size());
//        int[] k={3,2};
//        Inequality in=new Inequality(k);
//        in.findC();
//        in.print();
        
if (isBounded) phi = new Formula(bound, cover);
else phi =new Formula(cover);
        
        
        Inequality temp = phi.simplesolve();
        if (temp != null) {
            //System.out.println("1");
            //System.exit(0);
            System.out.println("The formula for trivial invariants produced a result.");
            temp.setCnotrel();
            for (PNTransition transition : PetriNet.getNet().getTransitions()) {
                if (temp.isRelevant(transition) && !temp.isTrivialStable(transition)) {
                    System.err.println("The inequalite is not stable wrt: " + transition.getName());
                }
            }
        } else {
            log.info("There is no trivial solution.");
            if(quant) temp=phi.quantifiedsolve();
            else temp = Cegar();
        }
        if (temp != null) {
            System.out.println(cmd.getOptionValue("file"));
//            System.out.println("The marking is not reachable, it is separated from M0 by the following inductive inequality:");
            temp.print();
        } else {
            System.out.println("0");
//            System.out.println("There is no inductive inequality.");
        }
        
    }

    /**
     * Runs the CEGAR loop.
     *
     * @return an inductive inequality if it terminates.
     * @throws java.lang.Exception
     */
    public static Inequality Cegar() throws Exception {
        log.info(phi.toString());
        int i = 0;
        while (true) {
            i++;
//            if(i==10) {
//                System.out.print("TO ");
//                return null;
//            }
System.out.print("\r"+i);
            log.info("Solving...");
            Inequality ineq = phi.solve();
            //ineq=new Inequality(new int[] {2,1});
            if (ineq == null) {
                return ineq;
            } else {
                log.info(ineq.toString());
            }
            log.info("Looking for cs...");
            if (ineq.findC()) {
                //System.out.println("Required " + i + " Iterations");
                return ineq;
            } else {
                phi.add(ineq);
            }
        }
    }

    /**
     * Constructs the example from the paper.
     */
    static void setExample() throws Exception {
        PetriNet net = new PetriNet(2);
        PetriNet.setNet(net);
        
        int[] tmin = {2, 1};
        int[] tdelta = {-1, 1};
        int[] umin = {1, 2};
        int[] udelta = {-1, 2};
        int[] vmin = {1, 0};
        int[] vdelta = {1, 1};
        m0 = new Marking(new int[]{3, 1});
        mF = new Marking(new int[]{0, 4});
        
        PNTransition t = new PNTransition(tmin, tdelta);
        PNTransition u = new PNTransition(umin, udelta);
        PNTransition v = new PNTransition(vmin, vdelta);
        net.addTransition(t);
        net.addTransition(u);
       // net.addTransition(v);
    }
        /**
     * Constructs the example from the paper.
     */
    static void setExample(int n) throws Exception {
        int j=n-1;
        PetriNet net = new PetriNet(n);
        PetriNet.setNet(net);
        
        int[] eins=new int[n]; 
        Arrays.fill(eins, 1);
        m0 = new Marking(eins);
        
        int[] zwei=new int[n]; 
        Arrays.fill(zwei, 2); 
        mF = new Marking(zwei);
        
        for (int i = 0; i < n; i++) {
            int[] tmin = new int[n]; 
            int[] tdelta = new int[n]; 
            Arrays.fill(tmin, 1);
            Arrays.fill(tdelta, -1);    
            tdelta[i]=n-1;
            if(i==j) tdelta[i]=n;
            PNTransition t = new PNTransition(tmin, tdelta);
            net.addTransition(t);
        }
        int[] temp=new int[n]; 
        Arrays.fill(temp, -n-1); 
        temp[j]=-n;
        Inequality ineq=new Inequality(temp);
        boolean findC = ineq.findC();
        System.out.println("check-k for: "+n+": "+findC);
        System.out.println(ineq.toString());

    }
    
}
