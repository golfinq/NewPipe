package org.schabi.newpipe.fragments.list.comments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.schabi.newpipe.R;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.comments.CommentReplyExtractor;
import org.schabi.newpipe.extractor.comments.CommentsInfo;
import org.schabi.newpipe.extractor.comments.CommentsReplyInfo;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.report.UserAction;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class CommentsReplyFragment extends CommentsFragment {
    private CommentReplyExtractor replyExtractor;
    private String url;
    private int serviceId;
    private String name;

    private final CompositeDisposable disposables = new CompositeDisposable();

    public static CommentsReplyFragment getInstance(final CommentReplyExtractor replyExtractor)
            throws ParsingException {
        final CommentsReplyFragment instance = new CommentsReplyFragment();
        instance.setInitialData(replyExtractor);
        return instance;
    }

    protected void setInitialData(final CommentReplyExtractor replyExtractorB)
            throws ParsingException {
        this.url = replyExtractorB.getUrl();
        this.serviceId = replyExtractorB.getServiceId();
        this.name = replyExtractorB.getName();
        this.replyExtractor = replyExtractorB;
    }

    /*//////////////////////////////////////////////////////////////////////////
    // LifeCycle
    //////////////////////////////////////////////////////////////////////////*/

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_comments, container, false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposables.clear();
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Load and handle
    //////////////////////////////////////////////////////////////////////////*/

    @Override
    protected Single<ListExtractor.InfoItemsPage> loadMoreItemsLogic() {
        return Single.fromCallable(() -> CommentsReplyInfo.getMoreItems(replyExtractor));
    }

    @Override
    protected Single<CommentsInfo> loadResult(final boolean forceLoad) {
        return Single.fromCallable(() -> CommentsReplyInfo.getInfo(replyExtractor));
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Contract
    //////////////////////////////////////////////////////////////////////////*/


    @Override
    public void handleNextItems(final ListExtractor.InfoItemsPage result) {
        super.handleNextItems(result);

        if (!result.getErrors().isEmpty()) {
            showSnackBarError(result.getErrors(), UserAction.REQUESTED_COMMENTS,
                    NewPipe.getNameOfService(serviceId), "Get next page of: " + url,
                    R.string.general_error);
        }
    }
}
