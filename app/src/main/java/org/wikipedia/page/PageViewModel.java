package org.wikipedia.page;

import androidx.annotation.Nullable;

import org.wikipedia.dataclient.okhttp.OkHttpConnectionFactory;
import org.wikipedia.history.HistoryEntry;
import org.wikipedia.readinglist.database.ReadingListPage;

import okhttp3.CacheControl;

/**
 * Shared data between PageFragment and PageFragmentLoadState
 */
public class PageViewModel {
    @Nullable private Page page;
    @Nullable private PageTitle title;
    @Nullable private HistoryEntry curEntry;
    @Nullable private ReadingListPage readingListPage;

    private boolean forceNetwork;

    @Nullable public Page getPage() {
        return page;
    }

    public void setPage(@Nullable Page page) {
        this.page = page;
    }

    @Nullable public PageTitle getTitle() {
        return title;
    }

    public void setTitle(@Nullable PageTitle title) {
        this.title = title;
    }

    @Nullable public HistoryEntry getCurEntry() {
        return curEntry;
    }

    public void setCurEntry(@Nullable HistoryEntry curEntry) {
        this.curEntry = curEntry;
    }

    public boolean isInReadingList() {
        return readingListPage != null;
    }

    public boolean shouldSaveOffline() {
        return readingListPage != null;
    }

    @Nullable public ReadingListPage getReadingListPage() {
        return readingListPage;
    }

    public void setReadingListPage(@Nullable ReadingListPage page) {
        readingListPage = page;
    }

    public void setForceNetwork(boolean forceNetwork) {
        this.forceNetwork = forceNetwork;
    }

    public boolean shouldForceNetwork() {
        return forceNetwork;
    }

    public boolean shouldLoadAsMobileWeb() {
        return title != null && title.isMainPage();
    }

    public CacheControl getCacheControl() {
        return shouldForceNetwork() ? OkHttpConnectionFactory.CACHE_CONTROL_FORCE_NETWORK : OkHttpConnectionFactory.CACHE_CONTROL_NONE;
    }
}
