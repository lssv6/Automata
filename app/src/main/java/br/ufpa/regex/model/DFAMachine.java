package br.ufpa.regex.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import br.ufpa.regex.model.TransitionFunction.TFEntry;


public class DFAMachine extends Machine implements Cloneable{
    public static class Builder{
        private DFAMachine machine;
        private Builder(State initialState){
            machine = new DFAMachine(); 
            machine.initialState = initialState;
            machine.internalStates.add(initialState);
        }

        private Builder(){
            machine = new DFAMachine(); 
        }
        
        public Builder fromTransitionFunction(TransitionFunction transitionFunction){
            transitionFunction.getEntries().forEach(
                tfe -> boundState(tfe.getFrom(), tfe.getTo(), tfe.getSymbol())
            );
            return this;
        }
        public Builder boundState(String from, String to, Character symbol){
            return boundState(State.fromString(from), State.fromString(to), symbol);
        }

        public Builder boundState(State from, State to, Character symbol){
            machine.internalStates.add(from);
            machine.internalStates.add(to);
            machine.transitionFunction.boundState(from, to, symbol);
            machine.inputAlphabet.add(symbol);
            return this;
        }

        public Builder addFinalStates(Collection<State> c){
            machine.finalStates.addAll(c);
            return this;
        }

        public DFAMachine build(){
            return machine;
        }
    }


    private DFAMachine(){
        internalStates = new HashSet<>(0);
        inputAlphabet = new HashSet<>(0);
        finalStates = new HashSet<>(0);
        transitionFunction = new TransitionFunction();
    }

    public static Builder builder(State initialState){
        return new Builder(initialState);
    }
    
    public static Builder builder(){
        return new Builder();
    }

    public List<State> run(CharSequence word){
        List<State> states = new ArrayList<State>();
        State current = initialState;
        states.add(current);
        for(Character symbol :word.toString().toCharArray()){
            current = transitionFunction.queryNextState(current, symbol);
            states.add(current);
        }
        return states;
    }

    @Override
    public DFAMachine clone(){
        DFAMachine.Builder builder = DFAMachine.builder(initialState.clone());
        Set<State> finalStatesClone = new HashSet<>();
        for(TFEntry entry: transitionFunction.getEntries()){
            State from = entry.getFrom();
            State to = entry.getTo();
            Character symbol = entry.getSymbol();
            
            State fromClone = from.clone();
            State toClone = to.clone();
            builder.boundState(fromClone, toClone, symbol);

            if(finalStates.contains(fromClone)){
                finalStatesClone.add(fromClone);
            }

            if(finalStates.contains(toClone)){
                finalStatesClone.add(toClone);
            }
        }
        builder.addFinalStates(finalStatesClone);
        return builder.build();
    }
    @Override
    public boolean validate(CharSequence word){
        State current = initialState;
        for(Character symbol :word.toString().toCharArray())
            current = transitionFunction.queryNextState(current, symbol);
        return finalStates.contains(current);
    }   
}

