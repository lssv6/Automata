package br.ufpa.regex.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// It's a DERTEMINISTIC FINITE AUTOMATA
public class Machine{
    private Set<State> internalStates;
    private Set<Character> inputAlphabet;
    // A.K.A State table
    private TransitionFunction transitionFunction;
    private State initialState;
    private Set<State> finalStates;
    
    public static class Builder{
        private Machine machine;
        private Builder(State initialState){
            machine = new Machine(); 
            machine.initialState = initialState;
            machine.internalStates.add(initialState);
        }

        private Builder(){
            machine = new Machine(); 
        }
        
        // I'm thinking if this code is necessary
        //public Builder addState(State s){
        //    machine.addState(s);
        //    return this;
        //}

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
            machine.addFinalStates(c);
            return this;
        }

        public Machine build(){
            // machine.createTransitionFunction();
            return machine;
        }
    }


    private Machine(){
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

    public State getInitialState(){
        return initialState;
    }
    public void addFinalStates(Collection<State> c){
        finalStates.addAll(c);
    }
    public void addState(State s){
        internalStates.add(s);
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

    public boolean isValid(CharSequence word){
        State current = initialState;
        for(Character symbol :word.toString().toCharArray())
            current = transitionFunction.queryNextState(current, symbol);

        return finalStates.contains(current);
    }
    
    /*
     * Tries to minimize the machine. Reducing the quantity of states.
     * */
    public boolean minimize(){
        return false;
    }

    @Override
    public String toString(){
        return "Machine[\n\tinternalStates=%s,\n\tinputAlphabet=%s,\n\ttransitionFunction=%s,\n\tinitialState=%s,\n\tfinalStates=%s]".formatted(
                internalStates,
                inputAlphabet,
                transitionFunction,
                initialState,
                finalStates
        );
    }
}

