package com.mrunal.arttour.ui;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import butterknife.BindView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.mrunal.arttour.R;
import com.mrunal.arttour.event.ArtSelectedEvent;
import com.mrunal.arttour.model.Artwork;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;
import java.util.Random;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class ArtActivity extends BaseActivity {

  @BindView(R.id.bottomBar)
  BottomBar mBottomBar;
  private boolean mTwoPane;
  private FirebaseAnalytics mFirebaseAnalytics;

  @Override
  protected void onStart() {
    super.onStart();
    if (!EventBus.getDefault().isRegistered(this)) {
      EventBus.getDefault().register(this);
    }
  }

  @Override
  protected void onDestroy() {
    if (EventBus.getDefault().isRegistered(this)) {
      EventBus.getDefault().unregister(this);
    }
    super.onDestroy();
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_home);

    mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

    logAnalytics(randomIndex());

        /*if (savedInstanceState != null) {
            //Restore the fragment's instance
            mContent = getSupportFragmentManager().getFragment(savedInstanceState, "myFragmentName");
        ...
        }*/

    showArtsFragment();

    mBottomBar.setOnTabSelectListener(new OnTabSelectListener() {
      @Override
      public void onTabSelected(@IdRes int tabId) {
        switch (tabId) {
          case R.id.tab_home:

            if (mBottomBar.getCurrentTabId() != R.id.tab_home) {
              showArtsFragment();
            }
            break;
          case R.id.tab_favorite:

            if (mBottomBar.getCurrentTabId() != R.id.tab_favorite) {
              showArtsFavoriteFragment();
            }
            break;
        }
      }
    });

    if (findViewById(R.id.homeDetailsFragment) != null) {
      // The detail container view will be present only in the
      // large-screen layouts (res/values-w900dp).
      // If this view is present, then the
      // activity should be in two-pane mode.
      mTwoPane = true;
    }

  }

  private String getCurrentFragment() {
    FragmentManager fm = getSupportFragmentManager();
    Fragment fragment_byID = fm.findFragmentById(R.id.homeFragment);

    if (fragment_byID instanceof ArtFragment) {
      return ArtFragment.class.getName();
    } else if (fragment_byID instanceof FavouriteArtsFragment) {
      return FavouriteArtsFragment.class.getName();
    } else {
      return ArtFragment.class.getName();
    }
  }

  private void showArtsFragment() {
    replaceFragment(ArtFragment.newInstance());
  }

  private void showArtsFavoriteFragment() {
    replaceFragment(FavouriteArtsFragment.newInstance());
  }

  private void replaceFragment(Fragment fragment) {
    getSupportFragmentManager().beginTransaction()
        .replace(R.id.homeFragment, fragment)
        .commit();
  }

  private void replaceDetailsFragment(Fragment fragment) {
    getSupportFragmentManager().beginTransaction()
        .replace(R.id.homeDetailsFragment, fragment)
        .commit();
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onEvent(final ArtSelectedEvent event) {
    Log.e("onEvent", "->" + event.getSelectedArt());

    Artwork artwork = event.getSelectedArt();

    if (artwork != null) {

      if (mTwoPane) {
        ArtDetailFragment fragment = ArtDetailFragment.newInstance(artwork);
        replaceDetailsFragment(fragment);
      } else {
        ArtDetailActivity.launchActivity(this, artwork);
      }
    }

  }

  public void selectFavoriteTab() {
    mBottomBar.selectTabWithId(R.id.tab_favorite);
  }

  private void logAnalytics(int randomIndex) {
    Bundle bundle = new Bundle();
    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, String.valueOf(randomIndex));
    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    //Sets whether analytics collection is enabled for this app on this device.
    mFirebaseAnalytics.setAnalyticsCollectionEnabled(true);

    //Sets the minimum engagement time required before starting a session. The default value is 10000 (10 seconds). Let's make it 20 seconds just for the fun
    mFirebaseAnalytics.setMinimumSessionDuration(20000);

    //Sets the duration of inactivity that terminates the current session. The default value is 1800000 (30 minutes).
    mFirebaseAnalytics.setSessionTimeoutDuration(500);

    //Sets the user ID property.
    mFirebaseAnalytics.setUserId(String.valueOf(randomIndex));
  }

  private int randomIndex() {
    int min = 0;
    int max = 100;
    Random rand = new Random();
    return min + rand.nextInt((max - min) + 1);
  }
}
