package org.schabi.newpipe.info_list.holder;

import android.content.SharedPreferences;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

import org.schabi.newpipe.R;
import org.schabi.newpipe.error.ErrorActivity;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.comments.CommentReplyExtractor;
import org.schabi.newpipe.extractor.comments.CommentsInfoItem;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.utils.Utils;
import org.schabi.newpipe.fragments.list.comments.CommentsReplyFragment;
import org.schabi.newpipe.info_list.InfoItemBuilder;
import org.schabi.newpipe.local.history.HistoryRecordManager;
import org.schabi.newpipe.util.CommentTextOnTouchListener;
import org.schabi.newpipe.util.DeviceUtils;
import org.schabi.newpipe.util.ImageDisplayConstants;
import org.schabi.newpipe.util.Localization;
import org.schabi.newpipe.util.NavigationHelper;
<<<<<<< HEAD
import org.schabi.newpipe.util.external_communication.ShareUtils;
=======
import org.schabi.newpipe.util.ShareUtils;
import org.schabi.newpipe.views.MaxHeightScrollView;
>>>>>>> Extracts and shows replies inside a new view called "MaxHeightScrollView"

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsMiniInfoItemHolder extends InfoItemHolder {
    private static final int COMMENT_DEFAULT_LINES = 2;
    private static final int COMMENT_EXPANDED_LINES = 1000;
    private static final Pattern PATTERN = Pattern.compile("(\\d+:)?(\\d+)?:(\\d+)");
    private final String downloadThumbnailKey;
    private final int commentHorizontalPadding;
    private final int commentVerticalPadding;

    private SharedPreferences preferences = null;
    private final RelativeLayout itemRoot;
    public final CircleImageView itemThumbnailView;
    private final TextView itemContentView;
    private final TextView itemLikesCountView;
    private final TextView itemDislikesCountView;
    private final TextView itemPublishedTime;
    private final TextView showReplies;
    private final TextView scrollDown;
    private final MaxHeightScrollView repliesHolder;
    private final RelativeLayout repliesLayout;

    private String commentText;
    private String streamUrl;
    private int repliesLayoutId;
    private boolean downloadedReplies = false;
    private boolean isReply = false;

    private final Linkify.TransformFilter timestampLink = new Linkify.TransformFilter() {
        @Override
        public String transformUrl(final Matcher match, final String url) {
            int timestamp = 0;
            final String hours = match.group(1);
            final String minutes = match.group(2);
            final String seconds = match.group(3);
            if (hours != null) {
                timestamp += (Integer.parseInt(hours.replace(":", "")) * 3600);
            }
            if (minutes != null) {
                timestamp += (Integer.parseInt(minutes.replace(":", "")) * 60);
            }
            if (seconds != null) {
                timestamp += (Integer.parseInt(seconds));
            }
            return streamUrl + url.replace(match.group(0), "#timestamp=" + timestamp);
        }
    };

    CommentsMiniInfoItemHolder(final InfoItemBuilder infoItemBuilder, final int layoutId,
                               final ViewGroup parent) {
        super(infoItemBuilder, layoutId, parent);

        itemRoot = itemView.findViewById(R.id.itemRoot);
        itemThumbnailView = itemView.findViewById(R.id.itemThumbnailView);
        itemLikesCountView = itemView.findViewById(R.id.detail_thumbs_up_count_view);
        itemDislikesCountView = itemView.findViewById(R.id.detail_thumbs_down_count_view);
        itemPublishedTime = itemView.findViewById(R.id.itemPublishedTime);
        itemContentView = itemView.findViewById(R.id.itemCommentContentView);
        showReplies = itemView.findViewById(R.id.showReplies);
        scrollDown = itemView.findViewById(R.id.scrollDown);
        repliesHolder = itemView.findViewById(R.id.RepliesHolder);
        repliesLayout = itemView.findViewById(R.id.RepliesLayout);


        downloadThumbnailKey = infoItemBuilder.getContext().
                getString(R.string.download_thumbnail_key);

        commentHorizontalPadding = (int) infoItemBuilder.getContext()
                .getResources().getDimension(R.dimen.comments_horizontal_padding);
        commentVerticalPadding = (int) infoItemBuilder.getContext()
                .getResources().getDimension(R.dimen.comments_vertical_padding);
    }

    public CommentsMiniInfoItemHolder(final InfoItemBuilder infoItemBuilder,
                                      final ViewGroup parent) {
        this(infoItemBuilder, R.layout.list_comments_mini_item, parent);
    }

    @Override
    public void updateFromItem(final InfoItem infoItem,
                               final HistoryRecordManager historyRecordManager) {
        if (!(infoItem instanceof CommentsInfoItem)) {
            return;
        }
        final CommentsInfoItem item = (CommentsInfoItem) infoItem;

        preferences = PreferenceManager.getDefaultSharedPreferences(itemBuilder.getContext());

        itemBuilder.getImageLoader()
                .displayImage(item.getUploaderAvatarUrl(),
                        itemThumbnailView,
                        ImageDisplayConstants.DISPLAY_THUMBNAIL_OPTIONS);

        if (preferences.getBoolean(downloadThumbnailKey, true)) {
            itemThumbnailView.setVisibility(View.VISIBLE);
            itemRoot.setPadding(commentVerticalPadding, commentVerticalPadding,
                    commentVerticalPadding, commentVerticalPadding);
        } else {
            itemThumbnailView.setVisibility(View.GONE);
            itemRoot.setPadding(commentHorizontalPadding, commentVerticalPadding,
                    commentHorizontalPadding, commentVerticalPadding);
        }


        itemThumbnailView.setOnClickListener(view -> openCommentAuthor(item));

        streamUrl = item.getUrl();

        itemContentView.setLines(COMMENT_DEFAULT_LINES);
        commentText = item.getCommentText();
        itemContentView.setText(commentText);
        itemContentView.setOnTouchListener(CommentTextOnTouchListener.INSTANCE);

        if (itemContentView.getLineCount() == 0) {
            itemContentView.post(this::ellipsize);
        } else {
            ellipsize();
        }

        if (item.getLikeCount() >= 0) {
            itemLikesCountView.setText(
                    Localization.shortCount(
                            itemBuilder.getContext(),
                            item.getLikeCount()));
        } else {
            itemLikesCountView.setText("-");
        }

        if (item.getUploadDate() != null) {
            itemPublishedTime.setText(Localization.relativeTime(item.getUploadDate()
                    .offsetDateTime()));
        } else {
            itemPublishedTime.setText(item.getTextualUploadDate());
        }

        itemView.setOnClickListener(view -> {
            toggleEllipsize();
            if (itemBuilder.getOnCommentsSelectedListener() != null) {
                itemBuilder.getOnCommentsSelectedListener().selected(item);
            }
        });


        itemView.setOnLongClickListener(view -> {
            if (DeviceUtils.isTv(itemBuilder.getContext())) {
                openCommentAuthor(item);
            } else {
                ShareUtils.copyToClipboard(itemBuilder.getContext(), commentText);
            }
            return true;
        });

        final CommentReplyExtractor itemReplies = item.getReplies();
        try {
            if ((itemReplies != null) && !Utils.isNullOrEmpty(itemReplies.getUrl())) {
                showReplies.setVisibility(View.VISIBLE);
                showReplies.setText("Show Replies                                      ");
                repliesLayoutId = View.generateViewId();
                repliesLayout.setId(repliesLayoutId);
                showReplies.setOnClickListener(view -> {
                    try {
                        createAndAddReplyFragment(itemReplies);
                    } catch (final ParsingException e) {

                    }
                });
                scrollDown.setOnClickListener(view -> {
                    repliesHolder.scrollBy(0,700);
                });
            } else {
                showReplies.setText("");
                showReplies.setVisibility(View.GONE);
            }
        } catch (final Exception e) {
            //Pass; we don't care if this fails
        }
    }

    private void createAndAddReplyFragment(final CommentReplyExtractor replyExtractor)
            throws ParsingException {
        if (!downloadedReplies) {
            final CommentsReplyFragment replyFragment
                    = CommentsReplyFragment.getInstance(replyExtractor);

            final AppCompatActivity repliesAct = (AppCompatActivity) repliesHolder.getContext();
            final FragmentManager repMan = repliesAct.getSupportFragmentManager();
            final FragmentTransaction addRep = repMan.beginTransaction();
            addRep.replace(repliesLayoutId, replyFragment).commitNowAllowingStateLoss();
            final int newViewId = View.generateViewId();
            itemView.findViewById(replyFragment.getId()).setId(newViewId);
            repliesLayoutId = newViewId;
            downloadedReplies = true;
            showReplies.setText("Scroll Up");
            scrollDown.setText("Scroll Down");
            downloadedReplies = true;
        } else{
            repliesHolder.scrollBy(0,-700);
        }
    }

    private void openCommentAuthor(final CommentsInfoItem item) {
        if (TextUtils.isEmpty(item.getUploaderUrl())) {
            return;
        }
        final AppCompatActivity activity = (AppCompatActivity) itemBuilder.getContext();
        try {
            NavigationHelper.openChannelFragment(
                    activity.getSupportFragmentManager(),
                    item.getServiceId(),
                    item.getUploaderUrl(),
                    item.getUploaderName());
        } catch (final Exception e) {
            ErrorActivity.reportUiErrorInSnackbar(activity, "Opening channel fragment", e);
        }
    }

    private void allowLinkFocus() {
        itemContentView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void denyLinkFocus() {
        itemContentView.setMovementMethod(null);
    }

    private boolean shouldFocusLinks() {
        if (itemView.isInTouchMode()) {
            return false;
        }

        final URLSpan[] urls = itemContentView.getUrls();

        return urls != null && urls.length != 0;
    }

    private void determineLinkFocus() {
        if (shouldFocusLinks()) {
            allowLinkFocus();
        } else {
            denyLinkFocus();
        }
    }

    private void ellipsize() {
        boolean hasEllipsis = false;

        if (itemContentView.getLineCount() > COMMENT_DEFAULT_LINES) {
            final int endOfLastLine
                    = itemContentView.getLayout().getLineEnd(COMMENT_DEFAULT_LINES - 1);
            int end = itemContentView.getText().toString().lastIndexOf(' ', endOfLastLine - 2);
            if (end == -1) {
                end = Math.max(endOfLastLine - 2, 0);
            }
            final String newVal = itemContentView.getText().subSequence(0, end) + " …";
            itemContentView.setText(newVal);
            hasEllipsis = true;
        }

        linkify();

        if (hasEllipsis) {
            denyLinkFocus();
        } else {
            determineLinkFocus();
        }
    }

    private void toggleEllipsize() {
        if (itemContentView.getText().toString().equals(commentText)) {
            if (itemContentView.getLineCount() > COMMENT_DEFAULT_LINES) {
                ellipsize();
            }
        } else {
            expand();
        }
    }

    private void expand() {
        itemContentView.setMaxLines(COMMENT_EXPANDED_LINES);
        itemContentView.setText(commentText);
        linkify();
        determineLinkFocus();
    }

    private void linkify() {
        Linkify.addLinks(itemContentView, Linkify.WEB_URLS);
        Linkify.addLinks(itemContentView, PATTERN, null, null, timestampLink);
    }
}
