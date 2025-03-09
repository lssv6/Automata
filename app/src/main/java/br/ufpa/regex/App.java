package br.ufpa.regex;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.Spliterator;

import br.ufpa.regex.model.DFAMachine;
import br.ufpa.regex.model.State;
import br.ufpa.regex.optimization.MachineOptimizer;
import br.ufpa.regex.parser.MachineParser;

public class App {
    private static final String MANUAL_MESSAGE = (
        "USAGEM : <CAMINHO DO ARQUIVO> [<TESTE>,[<TESTE>, ...]]"
    );
    public static void main(String[] args) {
        long length = args.length;
        if(length == 0){
            System.out.println(MANUAL_MESSAGE);
            System.exit(1);
        }
        // System.out.println(args[0]);
        DFAMachine m = null;
        try(FileReader reader = new FileReader(args[0])){
            m = (DFAMachine) MachineParser.parseFromCSV(reader);

        }catch(IOException exception){
            exception.printStackTrace();
            System.exit(1);
        }

        if(m == null){
            return;
        }

        DFAMachine m2 = MachineOptimizer.optimize(m);
        try(FileWriter fileWriter = new FileWriter(new File("better.csv"));){
            MachineParser.writeToCSV(m2, fileWriter);
        }catch(IOException ioException){
            ioException.printStackTrace();
        }

        try(FileWriter fileWriter = new FileWriter(new File("mermaidBetter.txt"));){
            MachineParser.writeToMermaid(m2, fileWriter);
        }catch(IOException ioException){
            ioException.printStackTrace();
        }
        
        try(FileWriter fileWriter = new FileWriter(new File("mermaidStd.txt"));){
            MachineParser.writeToMermaid(m, fileWriter);
        }catch(IOException ioException){
            ioException.printStackTrace();
        }

        if(length > 1){
            DFAMachine machine = m;
            Spliterator<String> tests = Arrays.spliterator(args);
            // skip first arg.
            tests.tryAdvance((test) -> {});
            tests.forEachRemaining(test -> {
                List<State> path =  machine.run(test);
                String pathString = path.stream().reduce(
                    "",
                    (str, p) -> str + (str.isEmpty()? str : "-->") + p.toString(),
                    (s1,s2) -> s1 + s2
                );
                System.out.println(test + " ::: " + pathString + " " +(machine.getFinalStates().contains(path.getLast())?"VALID":"INVALID"));
            });
        }
    }
}
