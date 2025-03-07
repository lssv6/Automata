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

        // Construir a tabela de todos os pares de estados possíveis enquanto marca os pares triviais.
        Set<UnorderedPair<State>> unorderedPairs = new HashSet<>();
        Set<UnorderedPair<State>> markedPairs = new HashSet<>();
        // Conjunto de estados finais. (Ser utilizado para consulta).
        Set<State> fStates = dfaMachine.getFinalStates();
        for(int i = 0; i < numberOfStates - 1; i++){
            for(int j = i + 1; j < numberOfStates; j++){
                State a = stateArray[i];
                State b = stateArray[j];

                UnorderedPair<State> pair = new UnorderedPair<State>(a, b);
                unorderedPairs.add(pair);

                // Marcar par trivial.
                if(fStates.contains(a) != fStates.contains(b)){
                    System.out.printf("Marking trivial pair = %s\n", pair);
                    markedPairs.add(pair);
                }

            }
        }


        // Marcar os pares de estados trivialmente não equivalentes; 
        TransitionFunction transitionFunction = dfaMachine.getTransitionFunction();
        boolean isOptimized = false;
        while(!isOptimized){
            isOptimized = true;
            // Para cada par de estados
            for(UnorderedPair<State> statePair : unorderedPairs){
                // Se o par de estados não está marcado
                if(!markedPairs.contains(statePair)){
                    State a = statePair.getA();
                    State b = statePair.getB();
                    // Pega os estados e siga-os
                    for(Character symbol: dfaMachine.getInputAlphabet()){
                        // Pega os estados para onde eles vão.(consultando tabela de estados.)
                        State whereAGoes = transitionFunction.queryNextState(a, symbol);
                        State whereBGoes = transitionFunction.queryNextState(b, symbol);
                        UnorderedPair<State> goingTo = new UnorderedPair<State>(whereAGoes, whereBGoes);
                        // Se os estados estão marcados.
                        if(markedPairs.contains(goingTo)){
                            // Parece que vamos precisar rodar o loop while(!isOptimized) mais uma vez.
                            isOptimized = false;
                            // Marque o par de estados.
                            markedPairs.add(statePair);
                            System.out.printf("marking pair %s\n", statePair); // LOG
                        }
                    }
                }
            }
        }

        // Esse é o conjunto de estados não marcados prontos para serem juntados.
        // Exemplo: {|S1, S2|, |S4, S5|} em que S1 e S2 são equivalentes e S4 e S5 são equivalentes também.
        // Juntar os estados equivalentes em um só, atualizando todas as referências necessárias.
        Set<UnorderedPair<State>> unmarkedPairs = SetUtils.difference(unorderedPairs, markedPairs).toSet();
        System.out.println(unmarkedPairs);

        Set<State> finalStates = dfaMachine.getFinalStates();

        Map<State,State> newStatesMap = new HashMap<>(); // Estado antigo, Estado novo
        // Para cada par não ordenado.
        for(UnorderedPair<State> unorderedPair: unmarkedPairs){
            // Pegue os estados deste par.
            State a = unorderedPair.getA();
            State b = unorderedPair.getB();

            // Faça um estado novo.
            State newState = State.fromString(a.getName() + b.getName());
            
            // Caso algum deles seja final.
            if(finalStates.contains(a) || finalStates.contains(b)){
                // Remova os estados antigos do conjunto de estados finais.
                finalStates.remove(a);finalStates.remove(b);
                // Adicione o novo.
                finalStates.add(newState);
            }
            // Adiciona os estados novos ao mapa de estados novos.
            newStatesMap.putIfAbsent(a, newState);
            newStatesMap.putIfAbsent(b, newState);
        }

        // Criar nova função de transição.
        TransitionFunction transitionFunction2 = new TransitionFunction();

        // Para cada entry da função de transição original
        for(TFEntry entry :transitionFunction.getEntries()){
            // Pegue o estado novo se tiver. pegue o antigo se não tiver. 
            State from = newStatesMap.getOrDefault(entry.getFrom(), entry.getFrom());
            State to = newStatesMap.getOrDefault(entry.getTo(), entry.getTo());
            Character symbol = entry.getSymbol();
            
            // Registre o estado na função de transição.
            transitionFunction2.boundState(from, to, symbol);
        }

        // Construa usando builder.
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

