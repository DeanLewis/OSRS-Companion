package com.dennyy.osrscompanion.interfaces;

import com.dennyy.osrscompanion.models.TodoList.TodoList;

public interface TodoListLoadedListener {
    void onTodoListLoaded(TodoList todoList);

    void onTodoListLoadFailed();
}
