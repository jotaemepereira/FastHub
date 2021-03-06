package com.fastaccess.ui.modules.notification;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.fastaccess.R;
import com.fastaccess.provider.rest.loadmore.OnLoadMore;
import com.fastaccess.ui.adapter.NotificationsAdapter;
import com.fastaccess.ui.base.BaseFragment;
import com.fastaccess.ui.widgets.AppbarRefreshLayout;
import com.fastaccess.ui.widgets.StateLayout;
import com.fastaccess.ui.widgets.recyclerview.DynamicRecyclerView;

import butterknife.BindView;

/**
 * Created by Kosh on 20 Feb 2017, 8:50 PM
 */

public class NotificationsView extends BaseFragment<NotificationsMvp.View, NotificationsPresenter>
        implements NotificationsMvp.View {

    @BindView(R.id.recycler) DynamicRecyclerView recycler;
    @BindView(R.id.refresh) AppbarRefreshLayout refresh;
    @BindView(R.id.stateLayout) StateLayout stateLayout;

    private OnLoadMore onLoadMore;
    private NotificationsAdapter adapter;

    public static NotificationsView newInstance() {
        return new NotificationsView();
    }

    @Override public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override public void onRefresh() {
        getPresenter().onCallApi(1, null);
    }

    @SuppressWarnings("unchecked") @NonNull @Override public OnLoadMore getLoadMore() {
        if (onLoadMore == null) {
            onLoadMore = new OnLoadMore<>(getPresenter());
        }
        return onLoadMore;
    }

    @Override public void onNotifyAdapter() {
        hideProgress();
        adapter.notifyDataSetChanged();
    }

    @Override public void onTypeChanged(boolean unread) {
        getPresenter().showAllNotifications(!unread);
        onRefresh();
    }

    @Override protected int fragmentLayout() {
        return R.layout.small_grid_refresh_list;
    }

    @Override protected void onFragmentCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        adapter = new NotificationsAdapter(getPresenter().getNotifications());
        adapter.setListener(getPresenter());
        refresh.setOnRefreshListener(this);
        stateLayout.setOnReloadListener(v -> onRefresh());
        getLoadMore().setCurrent_page(getPresenter().getCurrentPage(), getPresenter().getPreviousTotal());
        recycler.setEmptyView(stateLayout, refresh);
        recycler.setAdapter(adapter);
        recycler.addOnScrollListener(getLoadMore());
        if (savedInstanceState == null || !getPresenter().isApiCalled()) {
            onRefresh();
        }
    }

    @NonNull @Override public NotificationsPresenter providePresenter() {
        return new NotificationsPresenter();
    }

    @Override public void showProgress(@StringRes int resId) {

        stateLayout.showProgress();
    }

    @Override public void hideProgress() {
        refresh.setRefreshing(false);
        stateLayout.hideProgress();
    }

    @Override public void showErrorMessage(@NonNull String msgRes) {
        hideProgress();
        stateLayout.showReload(adapter.getItemCount());
        super.showErrorMessage(msgRes);
    }

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.notification_menu, menu);
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.readAll) {
            getPresenter().onReadAll();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override public void onDestroyView() {
        recycler.removeOnScrollListener(getLoadMore());
        super.onDestroyView();
    }
}
