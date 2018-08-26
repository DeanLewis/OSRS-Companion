package com.dennyy.osrscompanion.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.dennyy.osrscompanion.R;
import com.dennyy.osrscompanion.layouthandlers.TreasureTrailViewHandler;
import com.dennyy.osrscompanion.models.TreasureTrails.TreasureTrail;
import com.dennyy.osrscompanion.models.TreasureTrails.TreasureTrails;

public class TreasureTrailFragment extends BaseFragment {

    public static final String CLUE_DATA_KEY = "CLUE_DATA_KEY";

    private TreasureTrailViewHandler treasureTrailViewHandler;
    private View view;

    public TreasureTrailFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.treasure_trails_layout, container, false);

        return view;
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        toolbarTitle.setText(getResources().getString(R.string.treasure_trails));

        treasureTrailViewHandler = new TreasureTrailViewHandler(getActivity(), view, new TreasureTrailViewHandler.TreasureTrailsLoadedCallback() {
            @Override
            public void onLoaded(TreasureTrails ignored) {
                loadFragment(savedInstanceState);
            }

            @Override
            public void onLoadError() { }
        });
    }

    private void loadFragment(Bundle savedInstanceState) {
        Window window = getActivity().getWindow();
        if (window != null) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }
        if (savedInstanceState != null) {
            treasureTrailViewHandler.treasureTrail = (TreasureTrail) savedInstanceState.getSerializable(CLUE_DATA_KEY);
            treasureTrailViewHandler.reloadData();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        treasureTrailViewHandler.cancelVolleyRequests();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(CLUE_DATA_KEY, treasureTrailViewHandler.treasureTrail);
    }
}