package br.ufpa.regex;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import br.ufpa.regex.model.DFAMachine;
import br.ufpa.regex.optimization.MachineOptimizer;
import br.ufpa.regex.parser.MachineParser;

public class App {
    public static void main(String[] args) {
        System.out.println(args[0]);
        DFAMachine m = null;
        try(FileReader reader = new FileReader(args[0])){
            m = (DFAMachine) MachineParser.parseFromCSV(reader);

        }catch(IOException exception){
            exception.printStackTrace();
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

    }
}
