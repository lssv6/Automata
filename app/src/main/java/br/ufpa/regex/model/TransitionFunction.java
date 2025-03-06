package br.ufpa.regex.model;

import java.util.HashSet;
import java.util.Set;

public class TransitionFunction implements Cloneable{
    private Set<TFEntry> entries;
    /**
     * This class shouldn't be instanciated outside ThransitionFunction
     * */
    public class TFEntry implements Cloneable{
        private State from;
        private State to;
        private Character symbol;
        public TFEntry(State from, State to, Character symbol){
            this.from = from;
            this.to= to;
            this.symbol = symbol;
        }

        public State getFrom(){return from;}
        public State getTo(){return to;}
        public Character getSymbol(){return symbol;}

        @Override
        public String toString(){
            return "[%s --%c--> %s]".formatted(from, symbol, to);
        }
        
        @Override
        public boolean equals(Object other){
            TFEntry tfe = (TFEntry) other;
            return (
                from.equals(tfe.from) &&
                to.equals(tfe.to) &&
                symbol.equals(tfe.symbol)
            );
        }
    }

    public TransitionFunction(){
        entries = new HashSet<>(0);
    }

    public Set<TFEntry> getEntries(){return entries;}

    public void boundState(State from, State to, Character symbol){
        entries.add(new TFEntry(from, to, symbol));
    }

    public State queryNextState(State from, Character symbol){
        State state = State.ERROR;
        for(TFEntry entry: entries)
            if(entry.getFrom().equals(from) && entry.getSymbol().equals(symbol)){
                state = entry.getTo();
                break;
            }
        return state;
    }

    @Override
    public String toString(){
        return "TransitionFunction[entries=%s]".formatted(entries);
    }
}
