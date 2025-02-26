package br.ufpa.regex.model;

import java.util.Set;

public abstract class Machine{
    protected Set<State> internalStates;
    protected Set<Character> inputAlphabet;
    protected TransitionFunction transitionFunction;
    protected State initialState;
    protected Set<State> finalStates;

    public Set<State> getInternalStates(){
        return internalStates;
    }

    public Set<Character> getInputAlphabet(){
        return inputAlphabet;
    }

    public TransitionFunction getTransitionFunction(){
        return transitionFunction;
    }

    public State getInitialState(){
        return initialState;
    }

    public Set<State> getFinalStates(){
        return finalStates;
    }
    public abstract boolean validate(CharSequence word);
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

