package com.dennyy.osrscompanion.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dennyy.osrscompanion.R;
import com.dennyy.osrscompanion.helpers.Utils;
import com.dennyy.osrscompanion.viewhandlers.BestiaryViewHandler;

public class BestiaryFragment extends BaseFragment {
    private static final String NPC_NAME_KEY = "npc_name_key";

    private BestiaryViewHandler bestiaryViewHandler;

    public BestiaryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.bestiary_layout, container, false);
        return view;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        toolbarTitle.setText(getResources().getString(R.string.bestiary));

        bestiaryViewHandler = new BestiaryViewHandler(getActivity(), view, false);
        if (savedInstanceState != null) {
            String npcName = savedInstanceState.getString(NPC_NAME_KEY);
            if (!Utils.isNullOrEmpty(npcName)) {
                bestiaryViewHandler.loadNpc(npcName);
            }
        }
    }

    @Override
    public boolean onBackClick() {
        if (bestiaryViewHandler != null && !bestiaryViewHandler.isNpcDetailsVisible()) {
            bestiaryViewHandler.toggleNpcDetails(true);
            return true;
        }
        return super.onBackClick();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(NPC_NAME_KEY, bestiaryViewHandler.getNpcName());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        bestiaryViewHandler.cancelRunningTasks();
    }
}