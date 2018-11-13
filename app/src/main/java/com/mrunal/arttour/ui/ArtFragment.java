package com.mrunal.arttour.ui;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.android.volley.VolleyError;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.analytics.FirebaseAnalytics.Param;
import com.mrunal.arttour.R;
import com.mrunal.arttour.adapter.ArtsAdapter;
import com.mrunal.arttour.database.ArtsContract;
import com.mrunal.arttour.database.ArtsOpenHelper;
import com.mrunal.arttour.dto.ArtworkResponse;
import com.mrunal.arttour.event.ArtSelectedEvent;
import com.mrunal.arttour.event.FavoriteChangeEvent;
import com.mrunal.arttour.model.Artwork;
import com.mrunal.arttour.network.ArtService;
import com.mrunal.arttour.network.ResponseListener;
import com.mrunal.arttour.utils.LocalStoreUtil;
import com.mrunal.arttour.utils.Utils;
import com.mrunal.arttour.utils.ViewUtils;
import java.util.ArrayList;
import java.util.List;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * A placeholder fragment containing a simple view.
 */
public class ArtFragment extends BaseFragment implements ResponseListener<ArtworkResponse>,
    ArtsAdapter.Callbacks {

  @BindView(R.id.recyclerView)
  RecyclerView recyclerView;
  @BindView(R.id.layout_loading_view)
  View loadingView;
  @BindView(R.id.layout_no_connection)
  View noConnectionView;
  @BindView(R.id.checkFavorite)
  TextView mCheckFavoriteText;
  List<Artwork> artworks;
  GridLayoutManager gridLayoutManager;
  private ArtsAdapter artsAdapter;
  //private List<Artwork> mArts = new ArrayList<>();
  private List<Object> mArts = new ArrayList<>();
  private int currentPage, totalPages;
  private FirebaseAnalytics mFirebaseAnalytics;
  private Bundle mBundleRecyclerViewState;
  private String KEY_INSTANCE_STATE_RV_POSITION = "position";

  public static ArtFragment newInstance() {
    return new ArtFragment();
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putParcelable(KEY_INSTANCE_STATE_RV_POSITION, gridLayoutManager.onSaveInstanceState());
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    if (!EventBus.getDefault().isRegistered(this)) {
      EventBus.getDefault().register(this);
    }
  }

  @Override
  public void onDetach() {

    if (EventBus.getDefault().isRegistered(this)) {
      EventBus.getDefault().unregister(this);
    }

    super.onDetach();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_home, container, false);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());
    ButterKnife.bind(this, view);

    //if(savedInstanceState!=null){
    if(false){
      Parcelable mLayoutManagerSavedState = savedInstanceState.getParcelable(KEY_INSTANCE_STATE_RV_POSITION);
      artsAdapter = new ArtsAdapter(mArts);
      artsAdapter.setCallbacks(this);
      recyclerView.setAdapter(artsAdapter);
      artsAdapter.add(mArts);
      int columnCount = getResources().getInteger(R.integer.arts_columns);
      int spacing = Utils.dpToPx(5, getActivity()); // 50px
      gridLayoutManager = new GridLayoutManager(getActivity(), columnCount);
      boolean includeEdge = false;
      recyclerView
          .addItemDecoration(new GridSpacingItemDecoration(columnCount, spacing, includeEdge));
      recyclerView.setLayoutManager(gridLayoutManager);
      if(mLayoutManagerSavedState!=null){
        gridLayoutManager.onRestoreInstanceState(mLayoutManagerSavedState);
      }
    } else {

      if (!isInternetAvailable()) {
        showNoInternetView(true);
      }

      mCheckFavoriteText.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          ((ArtActivity) getActivity()).selectFavoriteTab();
        }
      });

      int columnCount = getResources().getInteger(R.integer.arts_columns);
      int spacing = Utils.dpToPx(5, getActivity()); // 50px

      gridLayoutManager = new GridLayoutManager(getActivity(), columnCount);
      boolean includeEdge = false;
      recyclerView
          .addItemDecoration(new GridSpacingItemDecoration(columnCount, spacing, includeEdge));
      recyclerView.setLayoutManager(gridLayoutManager);

        /*StaggeredGridLayoutManager staggeredGridLayoutManager =
                new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);
        staggeredGridLayoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        recyclerView.addItemDecoration(new SpacesItemDecoration(spacing));
        recyclerView.setLayoutManager(staggeredGridLayoutManager);*/

      recyclerView.addOnScrollListener(new EndlessRecyclerView(gridLayoutManager) {
        @Override
        public void onLoadMore(int current_page) {

          Log.e("current_page", "->" + current_page);
          Log.e("currentPage", "->" + currentPage);
          Log.e("totalPages", "->" + totalPages);

          if (currentPage < totalPages) {
            getMoviesData(current_page);
          }

        }
      });

    artsAdapter = new ArtsAdapter(mArts);
    artsAdapter.setCallbacks(this);
    recyclerView.setAdapter(artsAdapter);
    getMoviesData(1);
  }
  }

  public void getMoviesData(final int currentPage) {
    if (isInternetAvailable()) {

      new ArtService().getArtworks(Integer.toString(currentPage), this);

      if (currentPage == 1) {
        showLoadingView(true);
      }

    } else {

      if (currentPage == 1) {
        showNoInternetView(true);
      }

      Snackbar snackbar = Snackbar
          .make(getCoordinatorLayout(), R.string.no_internet, Snackbar.LENGTH_INDEFINITE)
          .setAction("RETRY", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              getMoviesData(currentPage);
            }
          });

      View sbView = snackbar.getView();
      TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
      textView.setTextColor(Color.YELLOW);
      snackbar.show();
    }
  }

  @Override
  public void onErrorResponse(VolleyError error) {
    Log.e("error", "->" + error);
  }

  @Override
  public void onResponse(ArtworkResponse response) {
    //Log.e("response", "->" + response.getPage());

    showDataView(true);

    if (response == null || response.getArtObjects().isEmpty()) {
      return;
    }

    currentPage++;
    totalPages = response.getCount() / 10; //10 objects per page
    artworks = response.getArtObjects();

    for (int i = 0; i < artworks.size(); ++i) {
      if (artworks.get(i).getWebImage() != null) {
        mArts.add(artworks.get(i));
      }
    }
    artsAdapter.notifyDataSetChanged();
  }

  @Override
  public void onArtClick(Artwork artwork) {
    Bundle bundle = new Bundle();
    bundle.putString(Param.ITEM_ID, artwork.getId());
    bundle.putString(Param.ITEM_NAME, artwork.getTitle());
    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "image");
    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    EventBus.getDefault().post(new ArtSelectedEvent(artwork));
  }

  @Override
  public void onResume() {
    super.onResume();
    if (mBundleRecyclerViewState != null) {
      new Handler().postDelayed(new Runnable() {

        @Override
        public void run() {
          Parcelable mListState = mBundleRecyclerViewState.getParcelable("KEY_RECYCLER_STATE");
          recyclerView.getLayoutManager().onRestoreInstanceState(mListState);

        }
      }, 50);
    }
    recyclerView.setLayoutManager(gridLayoutManager);

  }

  @Override
  public void onPause() {
    super.onPause();
    mBundleRecyclerViewState = new Bundle();
    Parcelable mListState = recyclerView.getLayoutManager().onSaveInstanceState();
    mBundleRecyclerViewState.putParcelable("KEY_RECYCLER_STATE", mListState);
  }

  @Override
  public void onFavoriteClick(Artwork artwork) {
    if (artwork.isFavorite()) { // Already added is removed
      LocalStoreUtil.removeFromFavorites(getActivity(), artwork.getLongId());
      ViewUtils.showToast(getResources().getString(R.string.removed_favorite), getActivity());

      getActivity().getContentResolver().delete(ArtsContract.ArtsEntry.CONTENT_URI.buildUpon()
          .appendPath(String.valueOf(artwork.getLongId())).build(), null, null);

    } else {
      LocalStoreUtil.addToFavorites(getActivity(), artwork.getLongId());
      ViewUtils.showToast(getResources().getString(R.string.added_favorite), getActivity());

      ContentValues values = ArtsOpenHelper.getArtContentValues(artwork);
      getActivity().getContentResolver().insert(ArtsContract.ArtsEntry.CONTENT_URI, values);
    }

    artsAdapter.notifyDataSetChanged();
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onEvent(final FavoriteChangeEvent event) {
    Log.e("onEvent", "->" + event.isFavoriteChanged());

    artsAdapter.notifyDataSetChanged();

  }

  private void showNoInternetView(boolean value) {
    int noInternetVisibility = value ? View.VISIBLE : View.GONE;
    noConnectionView.setVisibility(noInternetVisibility);

    int hiddenViewVisibility = value ? View.GONE : View.VISIBLE;
    loadingView.setVisibility(hiddenViewVisibility);
    recyclerView.setVisibility(hiddenViewVisibility);
  }

  private void showLoadingView(boolean value) {
    int loadingVisibility = value ? View.VISIBLE : View.GONE;
    loadingView.setVisibility(loadingVisibility);

    int hiddenViewVisibility = value ? View.GONE : View.VISIBLE;
    noConnectionView.setVisibility(hiddenViewVisibility);
    recyclerView.setVisibility(hiddenViewVisibility);
  }

  private void showDataView(boolean value) {
    int dataVisibility = value ? View.VISIBLE : View.GONE;
    recyclerView.setVisibility(dataVisibility);

    int hiddenViewVisibility = value ? View.GONE : View.VISIBLE;
    noConnectionView.setVisibility(hiddenViewVisibility);
    loadingView.setVisibility(hiddenViewVisibility);
  }

}
