package com.example.coderescue.navar.ar;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

/**
 * Stub replacement for the BeyondAR-based ArFragmentSupport.
 * The BeyondAR library has been removed from the build.
 * This provides no-op implementations so the rest of the app compiles.
 */
public class ArFragmentSupport extends Fragment {

    private static final String TAG = "ArFragmentSupport";
    private FrameLayout mMainLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mMainLayout = new FrameLayout(getActivity());
        TextView placeholder = new TextView(getActivity());
        placeholder.setText("AR View not available (library removed)");
        mMainLayout.addView(placeholder);
        Log.w(TAG, "BeyondAR library removed - AR rendering disabled");
        return mMainLayout;
    }

    /**
     * Stub - previously set the BeyondAR World for rendering.
     */
    public void setWorld(Object world) {
        Log.w(TAG, "setWorld: no-op (BeyondAR removed)");
    }

    /**
     * Stub - previously returned the GL surface view.
     */
    public ArBeyondarGLSurfaceView getGLSurfaceView() {
        return null;
    }

    /**
     * Stub - previously set click listener for AR objects.
     */
    public void setOnClickBeyondarObjectListener(Object listener) {
        Log.w(TAG, "setOnClickBeyondarObjectListener: no-op (BeyondAR removed)");
    }

    /**
     * Stub - previously set touch listener for AR view.
     */
    public void setOnTouchBeyondarViewListener(Object listener) {
        Log.w(TAG, "setOnTouchBeyondarViewListener: no-op (BeyondAR removed)");
    }
}
