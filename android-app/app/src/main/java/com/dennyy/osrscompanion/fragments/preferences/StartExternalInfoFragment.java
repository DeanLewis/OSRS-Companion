package com.dennyy.osrscompanion.fragments.preferences;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.dennyy.osrscompanion.BuildConfig;
import com.dennyy.osrscompanion.FloatingViewService;
import com.dennyy.osrscompanion.R;
import com.dennyy.osrscompanion.fragments.BaseFragment;

public class StartExternalInfoFragment extends BaseFragment implements View.OnClickListener {

    private TextView packageNameTextView;
    private TextView serviceNameTextView;

    public StartExternalInfoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.start_external_info_layout, container, false);
        packageNameTextView = view.findViewById(R.id.start_external_package_name);
        serviceNameTextView = view.findViewById(R.id.start_external_service_name);
        return view;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        toolbarTitle.setText(getString(R.string.start_service_externally));

        packageNameTextView.setText(BuildConfig.APPLICATION_ID);
        serviceNameTextView.setText(FloatingViewService.class.getCanonicalName());

        for (int resource : new int[]{ R.id.copy_package_name, R.id.copy_service_name }) {
            view.findViewById(resource).setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.copy_service_name) {
            copyText(serviceNameTextView.getText().toString());
        }
        else if (id == R.id.copy_package_name) {
            copyText(packageNameTextView.getText().toString());
        }
        showToast(getString(R.string.copied), Toast.LENGTH_SHORT);
    }

    private void copyText(String text) {
        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("", text);
        clipboard.setPrimaryClip(clip);
    }
}