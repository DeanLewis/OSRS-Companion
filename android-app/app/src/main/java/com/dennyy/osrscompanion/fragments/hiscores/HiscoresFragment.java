package com.dennyy.osrscompanion.fragments.hiscores;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.dennyy.osrscompanion.R;
import com.dennyy.osrscompanion.adapters.TileAdapter;
import com.dennyy.osrscompanion.fragments.BaseTileFragment;
import com.dennyy.osrscompanion.models.General.TileData;

public class HiscoresFragment extends BaseTileFragment implements AdapterView.OnItemClickListener {
    private View view;
    public HiscoresFragment() {
        super(2, 4);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.hiscores_layout, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        toolbarTitle.setText(getResources().getString(R.string.hiscores));
        initializeTiles();
    }
    @Override
    protected void initializeTiles() {
        if (tiles.isEmpty()) {
            tiles.add(new TileData(getString(R.string.hiscore_lookup), getDrawable(R.drawable.hiscores)));
            tiles.add(new TileData(getString(R.string.hiscore_compare), getDrawable(R.drawable.hiscores_compare)));
        }

        GridView gridView = view.findViewById(R.id.hiscores_grid_layout);
        TileAdapter tileAdapter = new TileAdapter(getActivity(), tiles);
        gridView.setNumColumns(currentColumns);
        gridView.setAdapter(tileAdapter);
        gridView.setOnItemClickListener(this);
    }
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        TileData tileData = tiles.get(i);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        Fragment fragment = null;
        String tag = "";
        if (tileData.text.equals(getString(R.string.hiscore_lookup))) {
            fragment = new HiscoresLookupFragment();
        }
        else if (tileData.text.equals(getString(R.string.hiscore_compare))) {
            fragment = new HiscoresCompareFragment();
        }

        transaction.replace(R.id.fragment_container, fragment, tag);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
