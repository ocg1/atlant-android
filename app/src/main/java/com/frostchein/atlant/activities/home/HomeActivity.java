package com.frostchein.atlant.activities.home;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.FrameLayout;
import butterknife.BindView;
import com.frostchein.atlant.R;
import com.frostchein.atlant.activities.base.BaseActivity;
import com.frostchein.atlant.activities.camera.CameraActivity;
import com.frostchein.atlant.dagger2.component.AppComponent;
import com.frostchein.atlant.dagger2.component.DaggerHomeActivityComponent;
import com.frostchein.atlant.dagger2.component.HomeActivityComponent;
import com.frostchein.atlant.dagger2.modules.HomeActivityModule;
import com.frostchein.atlant.fragments.transactions.TransactionsFragment;
import com.frostchein.atlant.model.Balance;
import com.frostchein.atlant.views.BaseCustomView;
import com.frostchein.atlant.views.NoTransactionView;
import com.frostchein.atlant.views.ToolbarView;
import java.util.ArrayList;
import javax.inject.Inject;
import org.greenrobot.eventbus.EventBus;

public class HomeActivity extends BaseActivity implements HomeView, ToolbarView.CallBack {

  @Inject
  HomePresenter presenter;
  @Inject
  ToolbarView toolbarView;

  private TransactionsFragment transactionsFragment;

  @BindView(R.id.screen_overlay_view)
  FrameLayout screenOverlayView;
  @BindView(R.id.fragment_content_frame)
  FrameLayout fragmentContentFrame;
  @BindView(R.id.no_transactions_view)
  NoTransactionView noTransactionView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_homescreen);
    setToolbarTitle(R.string.menu_balance);
    toolbarView.deleteTitle();

    transactionsFragment = TransactionsFragment.newInstance();
    getSupportFragmentManager().beginTransaction()
        .replace(R.id.fragment_content_frame, transactionsFragment, TransactionsFragment.class.getName())
        .commit();

    presenter.onCreate(savedInstanceState);
    EventBus.getDefault().register(presenter);
  }

  @Override
  public void onPause() {
    toolbarView.setCallback(null);
    super.onPause();
  }

  @Override
  public void onResume() {
    super.onResume();
    toolbarView.setCallback(this);
  }

  @Override
  protected void onDestroy() {
    EventBus.getDefault().unregister(presenter);
    super.onDestroy();
  }

  @Override
  public void onBackPressed() {
    if (drawerHelper.isDrawerOpen()) {
      drawerHelper.closeDrawer();
    } else {
      ActivityCompat.finishAffinity(this);
    }
  }

  @Override
  public void initUI() {
    screenOverlayView.setVisibility(View.GONE);
    swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getContext(), R.color.accent));
  }

  @Override
  public void setTransactionsOnFragment(ArrayList<Object> arrayList) {
    enableScrollToolbar();
    toolbarView.updateChart(true);
    fragmentContentFrame.setVisibility(View.VISIBLE);
    noTransactionView.setVisibility(View.GONE);
    transactionsFragment.update(arrayList);
  }

  @Override
  public void setNoTransactionsOnView() {
    disableScrollToolbar();
    toolbarView.updateChart(false);
    fragmentContentFrame.setVisibility(View.GONE);
    noTransactionView.setVisibility(View.VISIBLE);
    noTransactionView.invalidate();
  }

  @Override
  public void onLoadingError() {
    showMessage(getString(R.string.system_wallet_loading_error));
  }

  @Override
  public void onTimeout() {
    showMessage(getString(R.string.system_timeout));
  }

  @Override
  protected void setupComponent(AppComponent appComponent) {
    HomeActivityComponent component = DaggerHomeActivityComponent.builder()
        .appComponent(appComponent)
        .homeActivityModule(new HomeActivityModule(this))
        .build();
    component.inject(this);
  }

  @Override
  public boolean useToolbar() {
    return true;
  }

  @Override
  public boolean useDrawer() {
    return true;
  }

  @Override
  public boolean useToolbarActionHome() {
    return false;
  }

  @Override
  public boolean useToolbarActionQRCode() {
    return true;
  }

  @Override
  public boolean useCustomToolbar() {
    return true;
  }

  @Override
  public boolean useSwipeRefresh() {
    return true;
  }

  @Override
  protected void onToolbarQR() {
    super.onToolbarQR();
    goToCameraActivity(false, CameraActivity.TAG_FROM_HOME);
  }

  @Override
  public boolean timerLogOut() {
    return true;
  }

  @Override
  protected BaseCustomView getCustomToolbar() {
    return toolbarView;
  }

  @Override
  public void setContentOnToolbar(Balance balance) {
    toolbarView.setContent(balance);
  }

  @Override
  protected void onRefreshAction() {
    presenter.refreshContent();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if ((requestCode == BaseActivity.REQUEST_CODE_SEND || requestCode == BaseActivity.REQUEST_CODE_CAMERA)
        && resultCode == Activity.RESULT_OK) {
      presenter.onUpdateLocal();
    }
  }

  @Override
  public void onItemsClick(int pos) {
    presenter.onChangeValue(pos);
  }
}
