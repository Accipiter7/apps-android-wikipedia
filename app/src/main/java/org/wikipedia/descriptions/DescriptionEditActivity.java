package org.wikipedia.descriptions;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.wikipedia.R;
import org.wikipedia.activity.SingleFragmentActivity;
import org.wikipedia.analytics.SuggestedEditsFunnel;
import org.wikipedia.dataclient.restbase.page.RbPageSummary;
import org.wikipedia.history.HistoryEntry;
import org.wikipedia.json.GsonMarshaller;
import org.wikipedia.json.GsonUnmarshaller;
import org.wikipedia.page.ExclusiveBottomSheetPresenter;
import org.wikipedia.page.PageActivity;
import org.wikipedia.page.PageTitle;
import org.wikipedia.page.linkpreview.LinkPreviewDialog;
import org.wikipedia.readinglist.AddToReadingListDialog;
import org.wikipedia.util.ClipboardUtil;
import org.wikipedia.util.FeedbackUtil;
import org.wikipedia.util.ShareUtil;
import org.wikipedia.views.ImagePreviewDialog;

import static org.wikipedia.Constants.INTENT_EXTRA_INVOKE_SOURCE;
import static org.wikipedia.Constants.InvokeSource;
import static org.wikipedia.Constants.InvokeSource.LINK_PREVIEW_MENU;
import static org.wikipedia.Constants.InvokeSource.PAGE_ACTIVITY;
import static org.wikipedia.util.DeviceUtil.hideSoftKeyboard;

public class DescriptionEditActivity extends SingleFragmentActivity<DescriptionEditFragment>
        implements DescriptionEditFragment.Callback, LinkPreviewDialog.Callback {

    private static final String EXTRA_TITLE = "title";
    private static final String EXTRA_HIGHLIGHT_TEXT = "highlightText";
    private static final String EXTRA_INVOKE_SOURCE = "invokeSource";
    private static final String EXTRA_SOURCE_SUMMARY = "sourceSummary";
    private static final String EXTRA_TARGET_SUMMARY = "targetSummary";
    private InvokeSource invokeSource;
    private ExclusiveBottomSheetPresenter bottomSheetPresenter = new ExclusiveBottomSheetPresenter();

    public static Intent newIntent(@NonNull Context context,
                                   @NonNull PageTitle title,
                                   @Nullable String highlightText,
                                   @NonNull InvokeSource invokeSource) {
        return new Intent(context, DescriptionEditActivity.class)
                .putExtra(EXTRA_TITLE, GsonMarshaller.marshal(title))
                .putExtra(EXTRA_HIGHLIGHT_TEXT, highlightText)
                .putExtra(EXTRA_INVOKE_SOURCE, invokeSource);
    }


    public static Intent newIntent(@NonNull Context context,
                                   @NonNull PageTitle title,
                                   @Nullable RbPageSummary sourceSummary,
                                   @Nullable RbPageSummary targetSummary,
                                   @NonNull InvokeSource invokeSource) {
        return newIntent(context, title, null, invokeSource)
                .putExtra(EXTRA_SOURCE_SUMMARY, sourceSummary == null ? null : GsonMarshaller.marshal(sourceSummary))
                .putExtra(EXTRA_TARGET_SUMMARY, targetSummary == null ? null : GsonMarshaller.marshal(targetSummary));
    }

    @Override
    public void onDescriptionEditSuccess() {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onPageSummaryContainerClicked(@NonNull PageTitle pageTitle) {
        bottomSheetPresenter.show(getSupportFragmentManager(),
                ImagePreviewDialog.Companion.newInstance(new HistoryEntry(pageTitle,
                        getIntent().hasExtra(EXTRA_INVOKE_SOURCE) && getIntent().getSerializableExtra(EXTRA_INVOKE_SOURCE) == PAGE_ACTIVITY
                                ? HistoryEntry.SOURCE_EDIT_DESCRIPTION : HistoryEntry.SOURCE_SUGGESTED_EDITS),
                        null));
    }

    public void onLinkPreviewLoadPage(@NonNull PageTitle title, @NonNull HistoryEntry entry, boolean inNewTab) {
        startActivity(PageActivity.newIntentForCurrentTab(this, entry, entry.getTitle()));
    }

    @Override
    public void onLinkPreviewCopyLink(@NonNull PageTitle title) {
        copyLink(title.getCanonicalUri());
    }

    @Override
    public void onLinkPreviewAddToList(@NonNull PageTitle title) {
        bottomSheetPresenter.show(getSupportFragmentManager(),
                AddToReadingListDialog.newInstance(title, LINK_PREVIEW_MENU));
    }

    @Override
    public void onLinkPreviewShareLink(@NonNull PageTitle title) {
        ShareUtil.shareText(this, title);
    }

    private void copyLink(@NonNull String url) {
        ClipboardUtil.setPlainText(this, null, url);
        FeedbackUtil.showMessage(this, R.string.address_copied);
    }

    @Override
    public DescriptionEditFragment createFragment() {
        invokeSource = (InvokeSource) getIntent().getSerializableExtra(INTENT_EXTRA_INVOKE_SOURCE);
        PageTitle title = GsonUnmarshaller.unmarshal(PageTitle.class, getIntent().getStringExtra(EXTRA_TITLE));
        SuggestedEditsFunnel.get().click(title.getDisplayText(), invokeSource);

        return DescriptionEditFragment.newInstance(title,
                getIntent().getStringExtra(EXTRA_HIGHLIGHT_TEXT),
                getIntent().getStringExtra(EXTRA_SOURCE_SUMMARY),
                getIntent().getStringExtra(EXTRA_TARGET_SUMMARY),
                invokeSource);
    }

    @Override
    public void onBackPressed() {
        if (getFragment().editView.showingReviewContent()) {
            getFragment().editView.loadReviewContent(false);
        } else {
            hideSoftKeyboard(this);
            SuggestedEditsFunnel.get().cancel(invokeSource);
            super.onBackPressed();
        }
    }
}
