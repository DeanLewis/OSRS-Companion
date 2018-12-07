package com.dennyy.osrscompanion.interfaces;

public interface TodoEditorListener {
    void onTodoEditorSave(String todoText, int sortOrder, boolean done);

    void onTodoEditorCancel();
}
