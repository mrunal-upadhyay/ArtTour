package com.mrunal.arttour.ui;

import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import com.mrunal.arttour.utils.NetworkUtils;

public class BaseFragment extends Fragment {

  public boolean isInternetAvailable() {
    return NetworkUtils.isNetworkConnected(getActivity());
  }

  public void showSnackBar(String value) {
    ((ArtActivity) getActivity()).showSnackBar(value);
  }

  public void showSnackBar(int value) {
    ((ArtActivity) getActivity()).showSnackBar(value);
  }

  public CoordinatorLayout getCoordinatorLayout() {
    return ((ArtActivity) getActivity()).getCoordinatorLayout();
  }

}
