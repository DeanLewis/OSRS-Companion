package com.dennyy.osrscompanion.viewhandlers;

import android.content.Context;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.dennyy.osrscompanion.R;
import com.dennyy.osrscompanion.asynctasks.ReadFromFileTask;
import com.dennyy.osrscompanion.asynctasks.WriteToFileTask;
import com.dennyy.osrscompanion.helpers.Constants;
import com.dennyy.osrscompanion.interfaces.ContentLoadedListener;
import com.dennyy.osrscompanion.models.Notes.NoteChangeEvent;

import org.greenrobot.eventbus.EventBus;

public class NotesViewHandler extends BaseViewHandler implements TextWatcher {
    private String note;
    private EditText notesEditText;
    private final Handler handler = new Handler();
    private Runnable runnable;

    public NotesViewHandler(Context context, View view, boolean isFloatingView) {
        super(context, view, isFloatingView);
        notesEditText = view.findViewById(R.id.notes_edittext);
        new ReadFromFileTask(context, Constants.NOTES_FILE_NAME, new ContentLoadedListener() {
            @Override
            public void onContentLoaded(String content) {
                notesEditText.setText(content);
            }
        }).execute();
        notesEditText.addTextChangedListener(this);
    }

    public String getNote() {
        return notesEditText.getText().toString();
    }

    public void setNote(String note) {
        notesEditText.setTag("");
        notesEditText.setText(note);
        notesEditText.setTag(null);
    }

    @Override
    public void afterTextChanged(final Editable s) {
        note = s.toString();
        handler.removeCallbacks(runnable);
        if (notesEditText.getTag() != null) {
            return;
        }
        runnable = new Runnable() {
            @Override
            public void run() {
                saveAndPublishNotes();
            }
        };
        handler.postDelayed(runnable, 500);
    }

    private void saveAndPublishNotes() {
        new WriteToFileTask(context, Constants.NOTES_FILE_NAME, note).execute();
        EventBus.getDefault().post(new NoteChangeEvent(note, isFloatingView));
    }

    @Override
    public boolean wasRequesting() {
        return false;
    }

    @Override
    public void cancelRunningTasks() {

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }
}