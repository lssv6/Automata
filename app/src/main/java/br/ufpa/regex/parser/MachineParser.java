package br.ufpa.regex.parser;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import br.ufpa.regex.model.DFAMachine;
import br.ufpa.regex.model.Machine;
import br.ufpa.regex.model.State;
import br.ufpa.regex.model.TransitionFunction;

public class MachineParser{
    public static void writeToMermaid(DFAMachine machine, FileWriter fileWriter) throws IOException{
        TransitionFunction tFunction = machine.getTransitionFunction();
        String HEADER = (
            "stateDiagram-v2\n"+
            "    direction LR\n"
        );
        String SPACING = "    ";
        fileWriter.write(HEADER);
        fileWriter.write(SPACING + "[*] " + " --> " + machine.getInitialState().getName() + "\n");
        // Define final class, making the final states to be visible and pretty.
        fileWriter.write(SPACING + "classDef final fill: pink,stroke-width:2px,stroke:yellow\n");
        for(State fState: machine.getFinalStates())
            fileWriter.write(SPACING + fState.getName() + ":::" + "final\n");
        
        TransitionFunction transitionFunction = machine.getTransitionFunction();
        for(State from: machine.getInternalStates()){
            Map<State, String> map = new HashMap<>();
            for(Character symbol: machine.getInputAlphabet()){
                State to = transitionFunction.queryNextState(from, symbol);
                String arrowLabel = map.getOrDefault(to, "");
                arrowLabel = "" + (arrowLabel.isBlank()? symbol : arrowLabel + ", " + symbol);
                map.put(to, arrowLabel);
            }

            for(State toState: map.keySet()){
                fileWriter.write(SPACING + from.getName() + " --> " + toState.getName() + " : "+ map.get(toState) + "\n");
            }

        }
        //for(State from: strangeMap.keySet()){
        //    Map<State, String> mapTo = strangeMap.get(from);
        //    for(State to: mapTo.keySet()){
        //        fileWriter.write(SPACING + from.getName() + " --> " + to.getName() + " : " + mapTo.get(to) + "\n");
        //    }

        //}
        
    }
    public static void writeToCSV(DFAMachine machine, FileWriter fileWriter) throws IOException{
        CSVPrinter printer = new CSVPrinter(fileWriter, CSVFormat.RFC4180);
        TransitionFunction transitionFunction = machine.getTransitionFunction();
        Set<State> internalStates = machine.getInternalStates();
        Set<Character> inputAlphabet = machine.getInputAlphabet();
        State initialState =  machine.getInitialState();
        Set<State> finalStates = machine.getFinalStates();
        Character[] alphabetArray = inputAlphabet.toArray(new Character[0]);

        printer.printRecord("INICIAL", initialState.getName());

        printer.print("FINAIS");
        for(State fState: finalStates){
            printer.print(fState.getName());
        }
        printer.println();


        printer.print("FUNTRANS");
        for(Character c: inputAlphabet){
            printer.print("" + c);
        }
        printer.println();


        for(State state: internalStates){
            printer.print(state.getName());
            for(Character symbol: alphabetArray){
                
                printer.print(transitionFunction.queryNextState(state, symbol).getName());
            }
            printer.println();
        }
        printer.close();
    }
    
    public static Machine parseFromCSV(Reader reader) throws IOException{
        Iterable<CSVRecord> records = CSVFormat.RFC4180.parse(reader);
        List<List<String>> transitionFunctionEntries = new ArrayList<>();

        State initialState = State.ERROR;
        Set<State> finalStates = null;
        List<String> alphabet = null;

        int alphabetSize=0;
        for(CSVRecord record: records){
            List<String> strings = record.toList();
            // Skip comment, early return!
            if(strings.getFirst().startsWith("#") || strings.getFirst().isBlank()){
                continue;
            }
            if(strings.getFirst().equalsIgnoreCase("FUNTRANS")){
                alphabet = strings.subList(1,strings.size())
                    .stream()
                    .filter(string -> !string.isBlank()).toList();
                alphabetSize = alphabet.size();
            }else if(strings.getFirst().equalsIgnoreCase("INICIAL")){
                initialState = State.fromString(strings.get(1));
            }else if(strings.getFirst().equalsIgnoreCase("FINAIS")){
                finalStates = new HashSet<>();
                finalStates.addAll(
                    strings.subList(1, strings.size())
                        .stream()
                        .filter(str -> !str.isBlank())
                        .map(State::fromString)
                        .toList()
                );
            }else{
                transitionFunctionEntries.add(strings.subList(0, alphabetSize + 1));
            }
        }

        if(alphabet == null){
            throw new IOException("Error while parsing machine");
        }

        if(finalStates == null){
            throw new IOException("Error while parsing machine");
        }

        DFAMachine.Builder machineBuilder = DFAMachine.builder(initialState);
        // Now this for is more readable.
        //System.out.println(initialState);
        //System.out.println(finalStates);
        for(List<String> strings: transitionFunctionEntries){
            //System.out.println(strings);
            State from = State.fromString(strings.getFirst());
            for(int i = 1; i < strings.size(); i++){
                String stateString = strings.get(i);
                State to;
                
                if(stateString.isBlank()){
                    to = State.ERROR;
                }else{
                    to = State.fromString(stateString);
                }

                Character symbol = alphabet.get(i-1).charAt(0);
                machineBuilder.boundState(from, to, symbol);
            }
        }

        machineBuilder.addFinalStates(finalStates);
        Machine machine = machineBuilder.build();
        return machine;
    }
}

