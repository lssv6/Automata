package br.ufpa.regex.model;

public class State{
    private String name;

    public static final State ERROR = new State("E");
    public static final String ERROR_STRING= "E";

    private State(String name){this.name = name;}

    public static State fromString(String name){
        return new State(name);
    }
    
    public static State fromInteger(Integer number){
        return new State("q" + number);
    }

    public String getName(){return name;}
    public void setName(String name){this.name = name;}
    
    @Override
    public boolean equals(Object other){
        State otherState = (State)other;
        return name.equals(otherState.name);
    }
    @Override
    public int hashCode(){
        return name.hashCode();
    }

    @Override
    public String toString(){
        return "{%s}".formatted(name);
    }
}

