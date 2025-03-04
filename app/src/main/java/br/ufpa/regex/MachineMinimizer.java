package br.ufpa.regex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.SetUtils;

import br.ufpa.regex.model.State;
import br.ufpa.regex.model.DFAMachine;
import br.ufpa.regex.model.TransitionFunction;

// Class utilized in the minimation phase.
class Partition{
    List<Set<State>> sets;
    public Partition(){
        this.sets = new ArrayList<>();
    }
    public Partition(List<Set<State>> sets){
        this.sets = new ArrayList<>();
        for(Set<State> set: sets){
            this.sets.add(set);
        }
    }

    public void add(Set<State> states){
        sets.add(states);
    }
}

public class MachineMinimizer{
    private static State getNonMarkedState(Set<State> states, Set<State> marked){
        for(State s: states)
            if(!marked.contains(s))
                return s;
        return null;
    }
    private static DFAMachine removeUnreachableStates(DFAMachine machine){
        // Instantiate a set with only the initial state.
        Set<State> states = new HashSet<>();
        states.add(machine.getInitialState());

        // Set for the marked states.
        Set<State> marked = new HashSet<>();
        State nonMarkedState = getNonMarkedState(states, marked);
        
        TransitionFunction transitionFunction = machine.getTransitionFunction();
        TransitionFunction newTransitionFunction = new TransitionFunction();
    
        while(nonMarkedState != null){
            // At this line, nonMarkedState is actually marked. Only reusing the variable.
            marked.add(nonMarkedState);
            for(Character symbol: machine.getInputAlphabet()){
                State s = transitionFunction.queryNextState(nonMarkedState, symbol);
                if(!states.contains(s)){
                    states.add(s);
                    newTransitionFunction.boundState(nonMarkedState,s, symbol); 
                }
            }
        }
        // This last lines only create a copy of the machine with the Unreachable states optimized.
        DFAMachine.Builder builder = DFAMachine.builder(machine.getInitialState());
        builder.fromTransitionFunction(newTransitionFunction);
        builder.addFinalStates(machine.getFinalStates());
        return builder.build();
    }

    private static Set<State> disjunctionBetweenStateSets(Set<State> a, Set<State> b){
        a.removeAll(b);
        return a;
    }

    private static Partition refinePartition(DFAMachine machine, Partition partition){
        Partition partition2 = new Partition();

        for(State state: machine.getInternalStates()){
            Set<State> observationStates;
            for(Character symbol: machine.getInputAlphabet()){
                observationStates.add();
            }
            observationStates
        }
        return refinePartition(machine, partition2);
    }

    public static DFAMachine minimize(DFAMachine machine){
        DFAMachine unreachableStatesRemovedMachine = removeUnreachableStates(machine);
        Set<State> nonFinalStatess = SetUtils.difference(unreachableStatesRemovedMachine.getInternalStates(), unreachableStatesRemovedMachine.getFinalStates()).toSet();
        Partition p = refinePartition(unreachableStatesRemovedMachine, new Partitions());
        return machine;
    }
}
