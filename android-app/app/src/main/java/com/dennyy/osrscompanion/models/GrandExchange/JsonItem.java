package com.dennyy.osrscompanion.models.GrandExchange;


import java.io.Serializable;

public class JsonItem implements Serializable{
    public String id;
    public String name;
    public String store;
    public boolean isMembers;
    public int limit;

    @Override
    public String toString() {
        return name;
    }
}
