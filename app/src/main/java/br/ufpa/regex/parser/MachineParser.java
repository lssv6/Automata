package br.ufpa.regex.parser;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import br.ufpa.regex.model.Machine;
import br.ufpa.regex.model.State;

public class MachineParser{
    public static Machine parseFromCSV(Reader reader) throws IOException{
        Iterable<CSVRecord> records = CSVFormat.RFC4180.parse(reader);
        List<List<String>> transitionFunctionEntries = new ArrayList<>();

        State initialState = State.ERROR;
        Set<State> finalStates = null;
        List<String> alphabet = null;
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
            }else if(strings.getFirst().equalsIgnoreCase("INICIAL")){
                initialState = State.fromString(strings.get(1));
            }else if(strings.getFirst().equalsIgnoreCase("FINAIS")){
                finalStates = new HashSet<>();
                finalStates.addAll(
                    strings.subList(1,1 + alphabet.size())
                        .stream()
                        .filter(str -> !str.isBlank())
                        .map(State::fromString)
                        .toList()
                );
            }else{
                transitionFunctionEntries.add(strings);
            }
        }

        if(alphabet == null){
            throw new IOException("Error while parsing machine");
        }

        if(finalStates == null){
            throw new IOException("Error while parsing machine");
        }

        Machine.Builder machineBuilder = Machine.builder(initialState);
        // Now this for is more readable.
        for(List<String> strings: transitionFunctionEntries){
            System.out.println(strings);
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

