package com.frostchein.atlant.activities.home;

import com.frostchein.atlant.activities.base.BaseView;
import com.frostchein.atlant.model.Balance;
import java.util.ArrayList;

public interface HomeView extends BaseView {

  void setContentOnToolbar(Balance balance);

  void setTransactionsOnFragment(ArrayList<Object> arrayList);

  void setNoTransactionsOnView();

  void onLoadingError();

  void onTimeout();

  void onNoInternetConnection();

}
