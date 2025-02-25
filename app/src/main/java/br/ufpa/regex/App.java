package br.ufpa.regex;

import java.io.FileReader;
import java.io.IOException;

import br.ufpa.regex.model.Machine;
import br.ufpa.regex.parser.MachineParser;

public class App {
    public static void main(String[] args) {
        System.out.println(args[0]);
        try(FileReader reader = new FileReader(args[0])){
            Machine m = MachineParser.parseFromCSV(reader);
            System.out.println(m);
            System.out.println(m.isValid("ababbaabb"));
        }catch(IOException exception){
            exception.printStackTrace();
        }

        // If you wish to build a machine programmatically:
        //Machine.Builder builder = Machine.builder(State.fromString("q0"));

        //builder.boundState("q0", "q1", 'a');
        //builder.boundState("q0", State.ERROR_STRING, 'b');
        //builder.boundState("q1", State.ERROR_STRING, 'a');
        //builder.boundState("q1", "q2", 'b');
        //builder.boundState("q2", "q3", 'a');
        //builder.boundState("q2", State.ERROR_STRING, 'b');
        //builder.boundState("q3", "q3", 'a');
        //builder.boundState("q3", "q4", 'b');
        //builder.boundState("q4", "q3", 'a');
        //builder.boundState("q4", "q5", 'b');
        //builder.boundState("q5", "q3", 'a');
        //builder.boundState("q5", "q6", 'b');
        //builder.boundState("q6", "q6", 'a');
        //builder.boundState("q6", "q6", 'b');
        //builder.boundState(State.ERROR_STRING, State.ERROR_STRING, 'a');
        //builder.boundState(State.ERROR_STRING, State.ERROR_STRING, 'b');
        //
        //
        //Machine machine = builder.build();
        //System.out.println(machine.run(args[0]));
    }
}
