package br.ufpa.regex.optimization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.collections4.SetUtils;

import br.ufpa.regex.model.DFAMachine;
import br.ufpa.regex.model.State;
import br.ufpa.regex.model.TransitionFunction;
import br.ufpa.regex.model.TransitionFunction.TFEntry;

public class MachineOptimizer{
    public static State createMergedState(State a, State b){
        return State.fromString(a.getName() + b.getName());
    }

    public static DFAMachine optimize(DFAMachine dfaMachine){
        dfaMachine = dfaMachine.clone();
        Set<State> states = dfaMachine.getInternalStates();
        int numberOfStates = states.size();
        State[] stateArray = states.toArray(new State[numberOfStates]);
        
        Set<UnorderedPair<State>> unorderedPairs = new HashSet<>();
        Set<UnorderedPair<State>> markedPairs = new HashSet<>();
        Set<State> fStates = dfaMachine.getFinalStates();
        for(int i = 0; i < numberOfStates - 1; i++){
            for(int j = i + 1; j < numberOfStates; j++){
                State a = stateArray[i];
                State b = stateArray[j];

                UnorderedPair<State> pair = new UnorderedPair<State>(a, b);
                unorderedPairs.add(pair);

                // Mark trivial(obviously non optimizable) states soon.
                if(fStates.contains(a) != fStates.contains(b)){
                    System.out.printf("Marking trivial pair = %s\n", pair);
                    markedPairs.add(pair);
                }

            }
        }
        TransitionFunction transitionFunction = dfaMachine.getTransitionFunction();
        boolean isOptimized = false;
        while(!isOptimized){
            isOptimized = true;
            for(UnorderedPair<State> statePair : unorderedPairs){
                // If the statePair is not marked then marks if suitable.
                if(!markedPairs.contains(statePair)){
                    State a = statePair.getA();
                    State b = statePair.getB();
                    
                    for(Character symbol: dfaMachine.getInputAlphabet()){
                        State whereAGoes = transitionFunction.queryNextState(a, symbol);
                        State whereBGoes = transitionFunction.queryNextState(b, symbol);
                        UnorderedPair<State> goingTo = new UnorderedPair<State>(whereAGoes, whereBGoes);
                        if(markedPairs.contains(goingTo)){
                            isOptimized = false;
                            markedPairs.add(statePair);
                            System.out.printf("marking pair %s\n", statePair);
                        }
                    }
                }
            }
        }

        // CODE VERIFIED TIL THIS LINE TODO: Continue fixing from this line to the bottom.
        // This is the set of equivalent states represented as pairs.
        // Example: {|S1, S2|, |S4, S5|} in which S1 and S2 are equivalent.
        Set<UnorderedPair<State>> unmarkedPairs = SetUtils.difference(unorderedPairs, markedPairs).toSet();
        System.out.println(unmarkedPairs);

        Set<State> finalStates = dfaMachine.getFinalStates();

        Map<State,State> newStatesMap = new HashMap<>();
        for(UnorderedPair<State> unorderedPair: unmarkedPairs){
            State a = unorderedPair.getA();
            State b = unorderedPair.getB();
            State newState = State.fromString(a.getName() + b.getName());
            if(finalStates.contains(a) || finalStates.contains(b)){
                finalStates.remove(a);finalStates.remove(b);
                finalStates.add(newState);
            }
            newStatesMap.putIfAbsent(a, newState);
            newStatesMap.putIfAbsent(b, newState);
        }

        TransitionFunction transitionFunction2 = new TransitionFunction();
        for(TFEntry entry :transitionFunction.getEntries()){
            State from = newStatesMap.getOrDefault(entry.getFrom(), entry.getFrom());
            State to = newStatesMap.getOrDefault(entry.getTo(), entry.getTo());
            Character symbol = entry.getSymbol();

            transitionFunction2.boundState(from, to, symbol);
        }

        DFAMachine.Builder builder = DFAMachine.builder(dfaMachine.getInitialState());
        builder.addFinalStates(finalStates);
        return builder.fromTransitionFunction(transitionFunction2).build();
    }
}

class UnorderedPair<T>{
    private T a;
    private T b;
    public UnorderedPair(T a, T b){
        this.a = a;
        this.b = b;
    }

    public T getA(){
        return a;
    }

    public T getB(){
        return b;
    }

    public boolean contains(T value){
        return a.equals(value) || b.equals(value);
    }

    @Override
    public int hashCode(){
        return Objects.hash(a, b) + Objects.hash(b, a);
    }
    @Override
    public String toString(){
        return "|" + a + ", " + b+ "|";
    }
    @Override
    public boolean equals(Object other){
        boolean response;
        if(!other.getClass().equals(UnorderedPair.class)){
            response = false;
        }
        if(!(other instanceof UnorderedPair)){
            response = false;
        }
        try{
            @SuppressWarnings("unchecked")// Looks that this annotation is enough
            UnorderedPair<T> otherPair = (UnorderedPair<T>) other;

            response = (Objects.equals(a, otherPair.a) && Objects.equals(b, otherPair.b))
                || (Objects.equals(a, otherPair.b) && Objects.equals(b, otherPair.a));
        }catch(ClassCastException castException){
            response = false;
        }
        // System.out.printf("comparing %s, %s = %s\n", this, other, response);
        return response;
        
    }
}

