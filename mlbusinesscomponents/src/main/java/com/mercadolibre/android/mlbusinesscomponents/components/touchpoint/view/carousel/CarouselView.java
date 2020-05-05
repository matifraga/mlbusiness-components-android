package com.mercadolibre.android.mlbusinesscomponents.components.touchpoint.view.carousel;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import com.mercadolibre.android.mlbusinesscomponents.R;
import com.mercadolibre.android.mlbusinesscomponents.components.touchpoint.domain.model.carousel.CarouselCard;
import com.mercadolibre.android.mlbusinesscomponents.components.touchpoint.domain.model.carousel.Carousel;
import com.mercadolibre.android.mlbusinesscomponents.components.touchpoint.tracking.TouchpointTrackeable;
import com.mercadolibre.android.mlbusinesscomponents.components.touchpoint.view.AbstractTouchpointChildView;
import com.mercadolibre.android.mlbusinesscomponents.components.utils.TrackingUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;
import static com.mercadolibre.android.mlbusinesscomponents.components.utils.TrackingUtils.PRINT;
import static com.mercadolibre.android.mlbusinesscomponents.components.utils.TrackingUtils.SHOW;
import static com.mercadolibre.android.mlbusinesscomponents.components.utils.TrackingUtils.mergeData;

public class CarouselView extends AbstractTouchpointChildView<Carousel> {

    private final CarouselAdapter adapter;
    private final RecyclerView recyclerView;
    private final Rect rect;

    /**
     * Constructor
     *
     * @param context The execution context.
     */
    public CarouselView(final Context context) {
        this(context, null);
    }

    /**
     * Constructor
     *
     * @param context The execution context.
     * @param attrs The style attributes.
     */
    public CarouselView(final Context context, @Nullable final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Constructor
     *
     * @param context The execution context.
     * @param attrs The style attributes.
     * @param defStyleAttr Default style attributes.
     */
    public CarouselView(final Context context, @Nullable final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(getContext(), R.layout.touchpoint_carousel_view, this);
        recyclerView = findViewById(R.id.touchpoint_carousel_recycler_view);
        adapter = new CarouselAdapter();
        rect = new Rect();
        initList(context);
    }

    private void initList(final Context context) {
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.addItemDecoration(new CarouselDecorator(getResources().getDimensionPixelOffset(R.dimen.ui_1_25m)));
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull final RecyclerView recyclerView, final int state) {
                super.onScrollStateChanged(recyclerView, state);
                if (state == SCROLL_STATE_IDLE) {
                    print();
                }
            }
        });
    }

    @Override
    public void bind(@Nullable final Carousel model) {
        if (model != null && model.isValid()) {
            adapter.setOnClickCallback(onClickCallback);
            adapter.setTracker(tracker);
            adapter.setExtraData(tracking);
            showCards(model.getItems());
            trackShowEvent(new ArrayList<>(model.getItems()));
        }
    }

    private void trackShowEvent(final List<TouchpointTrackeable> trackeables) {
        if (tracker != null && !trackeables.isEmpty()) {
            tracker.track(SHOW, TrackingUtils.retrieveDataToTrack(trackeables, tracking));
        }
    }

    private void showCards(final List<CarouselCard> cards) {
        adapter.setItems(cards);
    }

    @Override
    public void print() {
        if (tracker != null) {
            recyclerView.getHitRect(rect);
            findData(recyclerView);
            final Map<String, Object> data = printProvider.getData();
            if (!data.isEmpty()) {
                tracker.track(PRINT, mergeData(data, tracking));
            }
            printProvider.cleanData();
        }
    }

    private void findData(final ViewGroup viewGroup) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            final View child = viewGroup.getChildAt(i);
            if (child instanceof ViewGroup && !(child instanceof TouchpointTrackeable)) {
                findData((ViewGroup) child);
            }
            if (shouldTrackPrint(child)) {
                printProvider.accumulateData(((TouchpointTrackeable) child).getTracking());
            }
        }
    }

    private boolean shouldTrackPrint(final View child) {
        return child instanceof TouchpointTrackeable && child.getLocalVisibleRect(rect);
    }
}
