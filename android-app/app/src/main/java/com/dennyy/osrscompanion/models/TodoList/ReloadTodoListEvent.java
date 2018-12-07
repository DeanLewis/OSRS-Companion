package com.dennyy.osrscompanion.models.TodoList;

public class ReloadTodoListEvent {
    public final boolean isFloatingView;
    public final boolean forceReload;


    public ReloadTodoListEvent(boolean isFloatingView) {
        this.isFloatingView = isFloatingView;
        this.forceReload = false;
    }

    public ReloadTodoListEvent(boolean isFloatingView, boolean forceReload) {
        this.isFloatingView = isFloatingView;
        this.forceReload = forceReload;
    }
}