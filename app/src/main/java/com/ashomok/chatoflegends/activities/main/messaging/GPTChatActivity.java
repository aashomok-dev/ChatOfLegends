package com.ashomok.chatoflegends.activities.main.messaging;

import static com.ashomok.chatoflegends.activities.main.messaging.settings.TokenCalculatorUtil.DEFAULT_GPT_TOKENS_ALLOWED;
import static com.ashomok.chatoflegends.activities.main.messaging.settings.TokenCalculatorUtil.DEFAULT_HISTORY_BUFFER_SIZE;
import static com.ashomok.chatoflegends.activities.main.messaging.settings.TokenCalculatorUtil.DEFAULT_OUTGOING_MESSAGE_LENGTH;
import static com.ashomok.chatoflegends.activities.main.messaging.settings.TokenCalculatorUtil.DEFAULT_TEMPERATURE_INT;
import static com.ashomok.chatoflegends.activities.main.messaging.settings.TokenCalculatorUtil.EXTRA_CURRENT_USER;
import static com.ashomok.chatoflegends.activities.main.messaging.settings.TokenCalculatorUtil.TAG_GPT_TOKENS_ALLOWED;
import static com.ashomok.chatoflegends.activities.main.messaging.settings.TokenCalculatorUtil.TAG_HISTORY_BUFFER_SIZE;
import static com.ashomok.chatoflegends.activities.main.messaging.settings.TokenCalculatorUtil.TAG_MESSAGE_MAX_LENGTH;
import static com.ashomok.chatoflegends.activities.main.messaging.settings.TokenCalculatorUtil.TAG_TEMPERATURE;
import static com.ashomok.chatoflegends.activities.main.messaging.settings.TokenCalculatorUtil.calculateMessageCost;
import static com.ashomok.chatoflegends.activities.main.messaging.settings.TokenCalculatorUtil.convertToFloat;
import static com.ashomok.chatoflegends.activities.settings.Settings.isPremium;
import static com.ashomok.chatoflegends.utils.IntentUtils.EXTRA_CURRENT_ALBUM_POSITION;
import static com.ashomok.chatoflegends.utils.IntentUtils.EXTRA_CURRENT_MESSAGE_ID;
import static com.ashomok.chatoflegends.utils.IntentUtils.EXTRA_STARTING_POSITION;
import static com.ashomok.lullabies.billing_kotlin.AppSku.REQUESTS_INFINITE;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.Spanned;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.SharedElementCallback;
import androidx.core.view.ViewCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aghajari.emojiview.AXEmojiManager;
import com.aghajari.emojiview.listener.PopupListener;
import com.aghajari.emojiview.search.AXEmojiSearchView;
import com.aghajari.emojiview.view.AXEmojiPager;
import com.aghajari.emojiview.view.AXEmojiPopup;
import com.aghajari.emojiview.view.AXEmojiView;
import com.ashomok.chatoflegends.R;
import com.ashomok.chatoflegends.activities.BaseActivity;
import com.ashomok.chatoflegends.activities.main.messaging.billing.BillingViewModelImpl;
import com.ashomok.chatoflegends.activities.main.messaging.settings.SettingsActivity;
import com.ashomok.chatoflegends.activities.main.messaging.update_to_premium.ChatRequestsStateModel;
import com.ashomok.chatoflegends.activities.main.rate_app.RateAppAsker;
import com.ashomok.chatoflegends.activities.settings.Settings;
import com.ashomok.chatoflegends.adapters.messaging.Interaction;
import com.ashomok.chatoflegends.adapters.messaging.MessagingAdapter;
import com.ashomok.chatoflegends.chatgpt.ChatGPTViewModel;
import com.ashomok.chatoflegends.chatgpt.ChatGPTViewModelFactory;
import com.ashomok.chatoflegends.chatgpt.model.completions.request.RequestModel;
import com.ashomok.chatoflegends.model.constants.MessageStat;
import com.ashomok.chatoflegends.model.constants.MessageType;
import com.ashomok.chatoflegends.model.constants.TypingStat;
import com.ashomok.chatoflegends.model.realms.Message;
import com.ashomok.chatoflegends.model.realms.Model;
import com.ashomok.chatoflegends.utils.AdapterHelper;
import com.ashomok.chatoflegends.utils.ClipboardUtil;
import com.ashomok.chatoflegends.utils.InfoSnackbarUtil;
import com.ashomok.chatoflegends.utils.LogHelper;
import com.ashomok.chatoflegends.utils.MessageCreator;
import com.ashomok.chatoflegends.utils.MyApp;
import com.ashomok.chatoflegends.utils.NetworkHelper;
import com.ashomok.chatoflegends.utils.RealmHelper;
import com.ashomok.chatoflegends.utils.ServiceHelper;
import com.ashomok.chatoflegends.utils.SharedPreferencesManager;
import com.ashomok.chatoflegends.utils.Util;
import com.ashomok.chatoflegends.utils.heroes.HeroType;
import com.ashomok.chatoflegends.utils.heroes.HeroesSystemSelector;
import com.ashomok.chatoflegends.utils.keyboard.KeyboardHelper;
import com.ashomok.chatoflegends.views.ChatEditText;
import com.ashomok.chatoflegends.views.dialogs.DeleteDialog;
import com.google.android.gms.ads.AdView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import ca.barrenechea.widget.recyclerview.decoration.StickyHeaderDecoration;
import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.RealmResults;


public class GPTChatActivity extends BaseActivity implements Interaction {

    public static final int DAILY_LIMIT = 25;
    //value to indicate whether it's in action mode or not
    public boolean isInActionMode;
    public ChatEditText etMessage;
    View rootView;
    AXEmojiPopup emojiPopup;
    RealmResults<Message> messageList;
    RealmResults<Message> observableList;
    OrderedRealmCollectionChangeListener<RealmResults<Message>> changeListener;
    MessagingAdapter adapter;
    LinearLayoutManager linearLayoutManager;
    String receiverUid;
    boolean isBroadcast;
    int searchIndex;
    Model model;

    private boolean isInSearchMode;
    private RecyclerView recyclerView;
    private ImageView emojiBtn;
    private ImageButton btnToolbarBack, upArrowSearchToolbar, downArrowSearchToolbar;

    private FloatingActionButton sendButton;
    private LinearLayout searchGroup;
    private SearchView searchViewToolbar;
    private Toolbar toolbar;
    private ImageView userImgToolbarChatAct;
    private TextView userNameToolbarChatActivity, tvCounterAction, tvTypingStatToolbar;
    private LinearLayout imgAndBackContainer;
    //quoted message layout when replying
    private View requestCounterLayout;
    private TextView textCounter;
    private ChatGPTViewModel chatGPTViewModel;
    private BillingViewModelImpl billingViewModel;
    private int requestsCount;
    private AdView adView;
    private int messageCost;

    private static final String TAG = LogHelper.makeLogTag(GPTChatActivity.class);

    private Bundle mTmpReenterState;

    private final SharedElementCallback mCallback = new SharedElementCallback() {
        @Override
        public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
            if (null != mTmpReenterState) {
                int startingPosition = mTmpReenterState.getInt(EXTRA_STARTING_POSITION);

                int currentPosition = mTmpReenterState.getInt(EXTRA_CURRENT_ALBUM_POSITION);


                if (startingPosition != currentPosition) {
                    // If startingPosition != mCurrentPosition the user must have swiped to a
                    // different page in the DetailsActivity. We must update the shared element
                    // so that the correct one falls into place.
                    String newTransitionName = mTmpReenterState.getString(EXTRA_CURRENT_MESSAGE_ID);


                    View newSharedElement = recyclerView.findViewWithTag(newTransitionName);

                    if (null != newSharedElement) {
                        names.clear();
                        names.add(newTransitionName);
                        sharedElements.clear();
                        sharedElements.put(newTransitionName, newSharedElement);
                    }
                }

                mTmpReenterState = null;
            } else {
                // If mTmpReenterState is null, then the activity is exiting.
                View navigationBar = findViewById(android.R.id.navigationBarBackground);
                View statusBar = findViewById(android.R.id.statusBarBackground);
                if (null != navigationBar) {
                    names.add(ViewCompat.getTransitionName(navigationBar));
                    sharedElements.put(ViewCompat.getTransitionName(navigationBar), navigationBar);
                }
                if (null != statusBar) {
                    names.add(ViewCompat.getTransitionName(statusBar));
                    sharedElements.put(ViewCompat.getTransitionName(statusBar), statusBar);
                }
            }
        }
    };
    private final int currentTypingState = TypingStat.NOT_TYPING;
    private ChatViewModel viewModel;

    private List<com.ashomok.chatoflegends.chatgpt.model.completions.request.Message> messagesHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        messagesHistory = new ArrayList<>();
        setContentView(R.layout.activity_chat);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            model = getIntent().getParcelableExtra("user");

        }
        receiverUid = model.getUid();
        initViews();
        setSupportActionBar(toolbar);
        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        chatGPTViewModel = new ViewModelProvider(this, new ChatGPTViewModelFactory(this))
                .get(ChatGPTViewModel.class);
        messageCost = calculateMessageCost(model);
        loadMessagesList();
        setAdapter();
        observeMessagesChanges();
        setChatNameInToolbar();
        setUpToolbar();
        //animate exit animation from FullscreenActivity to this Activity
        setExitSharedElementCallback(mCallback);
        sendButton.setOnClickListener(v -> {
            if (isPremium || messageCost < requestsCount) {
                String text = etMessage.getText().toString();
                if (!text.isEmpty()) {
                    sendMessage(text);
                }
            } else {
                showRequestsCounterDialog(requestsCount);
            }
        });

        etMessage.setOnClickListener(v -> emojiPopup.dismiss());
        emojiBtn.setOnClickListener(v -> emojiPopup.toggle());
        imgAndBackContainer.setOnClickListener(view -> onBackPressed());
        btnToolbarBack.setOnClickListener(view -> onBackPressed());
        searchViewToolbar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                RealmResults<Message> results = RealmHelper.getInstance().searchForMessage(receiverUid, query);
                if (!results.isEmpty()) {
                    //get the found last message index
                    searchIndex = results.size() - 1;
                    String foundMessageId = results.get(searchIndex).getMessageId();
                    int mIndex = getPosFromId(foundMessageId);
                    scrollAndHighlightSearch(mIndex);
                    downArrowSearchToolbar.setOnClickListener(view -> {
                        //+2 because one for index and one for previous
                        //check if there are another results
                        if (results.isEmpty() || searchIndex + 2 > results.size()) {
                            Toast.makeText(GPTChatActivity.this, R.string.not_found, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        //increment current index
                        searchIndex++;

                        String foundMessageId1 = results.get(searchIndex).getMessageId();
                        //get the index from chatList by message id from searchedList
                        int mIndex1 = getPosFromId(foundMessageId1);

                        scrollAndHighlightSearch(mIndex1);
                    });

                    upArrowSearchToolbar.setOnClickListener(view -> {

                        if (results.isEmpty() || 0 > searchIndex - 1) {
                            Toast.makeText(GPTChatActivity.this, R.string.not_found, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        //decrement search index
                        searchIndex -= 1;

                        String foundMessageId12 = results.get(searchIndex).getMessageId();
                        int mIndex12 = getPosFromId(foundMessageId12);
                        scrollAndHighlightSearch(mIndex12);
                    });
                }

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        searchViewToolbar.setOnQueryTextFocusChangeListener((view, b) -> {
            if (b)
                KeyboardHelper.openSoftKeyboard(this, view.findFocus());
        });

        searchViewToolbar.setOnCloseListener(() -> {
            isInSearchMode = false;
            return true;
        });

        adView = findViewById(R.id.ad_view);
        loadBannerAd(adView, getResources().getBoolean(R.bool.is_chat_ad_enabled), false);

        viewModel.getItemSelectedLiveData().observe(this, selectedMessages -> {
            if (selectedMessages.isEmpty())
                exitActionMode();
            else {
                updateToolbarButtons(selectedMessages);
            }
            updateActionModeItemsCount(selectedMessages.size());
        });

        chatGPTViewModel.getChatResponseLiveData().observe(this, response -> {
            messagesHistory.add(new com.ashomok.chatoflegends.chatgpt.model.completions.request.Message("assistant", response));
            new MessageCreator.Builder(model, MessageType.RECEIVED_TEXT).text(response).build();
        });

        RateAppAsker rateAppAsker = new RateAppAsker(getSharedPreferences(
                getString(R.string.preferences),
                Context.MODE_PRIVATE
        ), this);
        rateAppAsker.init(rateAppDialogFragment -> {
            // Show after 3 seconds
            Handler handler = new Handler();
            Runnable runnable = () -> {
                try {
                    rateAppDialogFragment.show(getSupportFragmentManager(), "rate_app_dialog");
                } catch (Exception e) {
                    //ignore if activity destroyed
                }
            };
            handler.postDelayed(runnable, 3000);
        });
    }

    private void setUpToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (null != actionBar) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        int iconDrawId = 0;
        ImageView userIcon = findViewById(R.id.user_img_toolbar_chat_act);
        if (model.equals(SharedPreferencesManager.getHomelessModel())) {
            iconDrawId = R.drawable.homeless;
        } else if (model.equals(SharedPreferencesManager.getSocraticModel())) {
            iconDrawId = R.drawable.socratic;
        } else if (model.equals(SharedPreferencesManager.getEinsteinModel())) {
            iconDrawId = R.drawable.einstein;
        } else if (model.equals(SharedPreferencesManager.getTeslaModel())) {
            iconDrawId = R.drawable.tesla;
        } else if (model.equals(SharedPreferencesManager.getGypsyWomanModel())) {
            iconDrawId = R.drawable.gypsy_woman;
        } else if (model.equals(SharedPreferencesManager.getElonMuskModel())) {
            iconDrawId = R.drawable.elon_musk;
        }
        userIcon.setImageResource(iconDrawId);
    }

    //hide or show toolbar button in activity depending on conditions
    private void updateToolbarButtons(List<Message> selectedMessages) {
        if (AdapterHelper.shouldHideAllItems(selectedMessages)) {
            hideCopyItem();
        } else {
            showShareItem();
            if (AdapterHelper.shouldEnableCopyItem(selectedMessages))
                showCopyItem();
            else
                hideCopyItem();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        observableList.addChangeListener(changeListener);
        MyApp.chatActivityResumed(receiverUid);
    }

    @Override
    protected void onPause() {
        observableList.removeChangeListener(changeListener);
        MyApp.chatActivityPaused();
        super.onPause();
    }

    private void scrollAndHighlightSearch(int index) {
        recyclerView.scrollToPosition(index);
        View view = getCurrentFocus();
        //hide keyboard
        if (null != view)
            KeyboardHelper.hideSoftKeyboard(this, view);
        new Handler().postDelayed(() -> {
            //get view holder of this textView
            RecyclerView.ViewHolder viewHolderForAdapterPosition = recyclerView.findViewHolderForAdapterPosition(index);
            //get textView
            TextView tv = viewHolderForAdapterPosition.itemView.findViewById(R.id.tv_message_content);
            //highlight text
            tv.setText(Util.highlightText(tv.getText().toString()));
        }, 100);

    }

    //transition effects
    @Override
    public void onActivityReenter(int requestCode, Intent data) {
        super.onActivityReenter(requestCode, data);
        supportPostponeEnterTransition();
        recyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                recyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
                recyclerView.requestLayout();
                supportStartPostponedEnterTransition();
                return true;
            }
        });
    }

    private void updateToolbarTvsVisibility(boolean hideOnlineStatToolbar) {
        if (isInActionMode || isInSearchMode) return;

        if (hideOnlineStatToolbar) {
            tvTypingStatToolbar.setVisibility(View.VISIBLE);
        } else {
            tvTypingStatToolbar.setVisibility(View.GONE);
        }
    }


    //this is called **whenever** a CHANGE occurs to the "observableList" ,insertion,change,delete,etc...
    private void observeMessagesChanges() {
        changeListener = (messages, changeSet) -> {

            OrderedCollectionChangeSet.Range[] modifications = changeSet.getChangeRanges();
            OrderedCollectionChangeSet.Range[] insertions = changeSet.getInsertionRanges();

            if (0 != insertions.length) {
                updateChat(messages.get(insertions[0].startIndex));

            } else if (0 != modifications.length) {
                updateChat(messages.get(modifications[0].startIndex));
            }
            for (OrderedCollectionChangeSet.Range range : modifications) {
                newMessageInsertedOrModified(messages, range);
            }
            for (OrderedCollectionChangeSet.Range range : insertions) {
                newMessageInsertedOrModified(messages, range);
            }
        };
    }

    private void newMessageInsertedOrModified(RealmResults<Message> messages, OrderedCollectionChangeSet.Range range) {
        //get the new Message
        Message message = messages.get(range.startIndex);

        //update date header if it's a new day
        adapter.messageInserted();

        //update incoming messages
        // if this message is from the recipient and its' not read before then update the message currentTypingState to READ
        if (MessageType.GROUP_EVENT != message.getType() && !message.getFromId().equals(receiverUid) && message.getChatId().equals(receiverUid) && MessageStat.READ != message.getMessageStat()) {
            new Handler().postDelayed(()
                    -> ServiceHelper.startUpdateMessageStatRequest(
                    this, message.getMessageId(), receiverUid,
                    message.getChatId(), MessageStat.READ), 100);

        }
    }

    //scroll to last OR update the unread count
    private void updateChat(Message message) {

        if (MessageType.GROUP_EVENT == message.getType())
            return;

        //if the message was send by the user then scroll to last
        if (message.getFromId().equals(receiverUid) && MessageStat.PENDING == message.getMessageStat()) {
            scrollToLast();
        } else {
            //if the message was sent by Receiver and its state is still pending
            if (message.getChatId().equals(receiverUid) && MessageStat.PENDING == message.getMessageStat()) {
                //get index from the message
                int i = messageList.indexOf(message);
                //if it's -1 (not exists) return
                if (-1 == i)
                    return;


                //get last visible item on screen
                int lastVisibleItemPosition = getLastVisibileItem();

                //if the last message is visible then we will scroll to last
                //the user in this case is at before the last message that inserted
                // therefore a new message was inserted and we want to scroll to it
                //"-2" because one for index and one for previous message
                if (messageList.size() - 2 == lastVisibleItemPosition) {
                    scrollToLast();
                }
            }
        }
    }

    private void scrollToLast() {
        if (null == messageList) return;
        if (0 >= messageList.size() - 1) return;


        recyclerView.scrollToPosition(messageList.size() - 1);
    }

    private void setAdapter() {
        adapter = new MessagingAdapter(messageList, true, this, this,
                model,
                viewModel.getItemSelectedLiveData());


        StickyHeaderDecoration decor = new StickyHeaderDecoration(adapter);
        linearLayoutManager = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

        //add Date Header to the Adapter
        recyclerView.addItemDecoration(decor, 0);

        //if there are messages in this chat
        if (!messageList.isEmpty()) {
            recyclerView.scrollToPosition(messageList.size() - 1);
        }
    }

    //init views
    private void initViews() {
        recyclerView = findViewById(R.id.recycler_chat);
        rootView = findViewById(R.id.drawer_layout);
        emojiBtn = findViewById(R.id.emoji_btn);
        etMessage = findViewById(R.id.et_message);
        sendButton = findViewById(R.id.send_button);

        initEmojiView();

        toolbar = findViewById(R.id.toolbar);
        userImgToolbarChatAct = findViewById(R.id.user_img_toolbar_chat_act);
        userNameToolbarChatActivity = findViewById(R.id.user_name_toolbar_chat_activity);
        tvTypingStatToolbar = findViewById(R.id.tv_typing_stat_toolbar);
        tvCounterAction = findViewById(R.id.messages_counter_action);
        btnToolbarBack = findViewById(R.id.btn_toolbar_back);
        searchViewToolbar = findViewById(R.id.search_view_toolbar);
        upArrowSearchToolbar = findViewById(R.id.up_arrow_search_toolbar);
        downArrowSearchToolbar = findViewById(R.id.down_arrow_search_toolbar);
        searchGroup = findViewById(R.id.search_layout);

        imgAndBackContainer = findViewById(R.id.img_and_back_container);
        requestCounterLayout = findViewById(R.id.requests_counter_layout);
        textCounter = findViewById(R.id.requests_counter_text);

        billingViewModel = new BillingViewModelImpl(getApplication(), ChatRequestsStateModel.Companion.getInstance(getApplication()));
        billingViewModel.getRequestsLiveData().observe(this, requests -> {
            if (null != requests) {
                updateRequestsCounter(requests);
                requestsCount = requests;
                updateViewForPremium(REQUESTS_INFINITE == requests);
            }
        });
    }

    public void consumeTokens(int tokensQuantityToConsume) {
        if (!isPremium) {
            billingViewModel.consumeTokens(tokensQuantityToConsume);
        }
    }

    private Boolean oldIsPremiumState;

    public void updateRequestsCounter(int requestCount) {
        textCounter.setText(String.valueOf(requestCount));
        requestCounterLayout.setOnClickListener(view -> showRequestsCounterDialog(requestCount));
    }

    public void showRequestsCounterDialog(int requestCount) {
        RequestsCounterDialogFragment requestsCounterDialogFragment =
                RequestsCounterDialogFragment.newInstance(R.string.you_have_tokens, messageCost, requestCount);
        requestsCounterDialogFragment.show(getSupportFragmentManager(), "dialog");
    }

    private void initEmojiView() {
        AXEmojiPager emojiPager = new AXEmojiPager(this);
        AXEmojiView emojiView = new AXEmojiView(this);
        emojiPager.addPage(emojiView, R.drawable.ic_insert_emoticon_white);
        // set target emoji edit text to emojiViewPager
        emojiPager.setEditText(etMessage);

        emojiPager.setSwipeWithFingerEnabled(true);
        emojiPager.setLeftIcon(R.drawable.ic_search_emoji);
        emojiPopup = new AXEmojiPopup(emojiPager);

        emojiPopup.setPopupListener(new PopupListener() {
            @Override
            public void onDismiss() {
                emojiBtn.setImageResource(R.drawable.ic_insert_emoticon_black);
            }

            @Override
            public void onShow() {
                emojiBtn.setImageResource(R.drawable.ic_baseline_keyboard_24);
            }

            @Override
            public void onKeyboardOpened(int height) {

            }

            @Override
            public void onKeyboardClosed() {

            }

            @Override
            public void onViewHeightChanged(int height) {

            }
        });

        // SearchView
        if (AXEmojiManager.isAXEmojiView(emojiPager.getPage(0))) {
            emojiPopup.setSearchView(new AXEmojiSearchView(this, emojiPager.getPage(0)));
            emojiPager.setOnFooterItemClicked((view, leftIcon) -> {
                if (leftIcon) emojiPopup.showSearchView();
            });
        }
    }

    public void updateViewForPremium(boolean isPremium) {
        if (null == oldIsPremiumState || oldIsPremiumState != isPremium) {
            oldIsPremiumState = isPremium;
            LogHelper.d(TAG, "Update UI. Is premium " + isPremium);
            requestCounterLayout.setVisibility(isPremium ? View.GONE : View.VISIBLE);
            Settings.isPremium = isPremium;
            adView.setVisibility(!isPremium && getResources().getBoolean(R.bool.is_chat_ad_enabled) ?
                    View.VISIBLE : View.GONE);

            loadInterstitialAd(this, !isPremium && getResources().getBoolean(R.bool.is_interstitial_ad_enabled));
        }
    }

    private void setChatNameInToolbar() {
        if (isBroadcast) {
            updateToolbarTvsVisibility(false);
        }
        userNameToolbarChatActivity.setText(model.getProperUserName());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.menu_item_copy:
                copyItemClicked();
                break;

            case R.id.menu_item_delete:
                deleteItemClicked();
                break;

            case R.id.menu_item_share:
                shareClicked();
                break;

            case R.id.search_item:
                searchItemClicked();
                break;

            case R.id.clear_chat_item:
                clearChat();
                break;

            case R.id.menu_item_settings:
                onSettingsClicked();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onSettingsClicked() {
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.putExtra(EXTRA_CURRENT_USER, model);
        startActivity(intent);
    }

    private void clearChat() {
        DeleteDialog deleteDialog = new DeleteDialog(this);
        deleteDialog.setMTitle(getResources().getString(R.string.confirmation));
        deleteDialog.setMessage(R.string.clear_chat_message);
        deleteDialog.setmListener(() -> {
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(getResources().getString(R.string.deleting));
            RealmHelper.getInstance().clearChat(receiverUid);
        });

        deleteDialog.show();
    }

    private void searchItemClicked() {
        if (isInActionMode)
            exitActionMode();

        isInSearchMode = true;
        toolbar.getMenu().clear();
        searchGroup.setVisibility(View.VISIBLE);
        hideOrShowUserInfo(true);
        if (searchViewToolbar.isIconified())
            searchViewToolbar.onActionViewExpanded();

        searchViewToolbar.requestFocus();
    }

    private void copyItemClicked() {
        List<Message> selectedItemsForActionMode = viewModel.getSelectedItems();

        //sorting messages by timestamp
        //if the user selected the messages in a Random way
        Collections.sort(selectedItemsForActionMode);

        StringBuilder builder = new StringBuilder();
        for (Message message : selectedItemsForActionMode) {
            builder.append(message.getContent());
        }
        String copiedString = builder.toString();
        ClipboardUtil.copyTextToClipboard(this, copiedString);
        Toast.makeText(this, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show();
        exitActionMode();
    }

    private void shareClicked() {
        Message message = viewModel.getSelectedItems().get(0);

        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        Resources res = getResources();
        String linkToApp = "https://play.google.com/store/apps/details?id=" + res.getString(R.string.app_package_name);
        String sharedBody =
                String.format(res.getString(R.string.share_text_message), message.getContent(), linkToApp);

        Spanned styledText;
        styledText = Html.fromHtml(sharedBody, Html.FROM_HTML_MODE_LEGACY);

        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                String.format(res.getString(R.string.that_is_what_app_says), res.getString(R.string.app_name)));
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, styledText);
        startActivity(Intent.createChooser(sharingIntent,
                res.getString(R.string.send_to)));
    }

    private void deleteItemClicked() {

        List<Message> selectedItemsForActionMode = viewModel.getSelectedItems();

        DeleteDialog deleteDialog = new DeleteDialog(this);

        deleteDialog.setmListener(() -> {
            for (Message message : selectedItemsForActionMode) {
                RealmHelper.getInstance().deleteMessageFromRealm(message.getChatId(), message.getMessageId());
            }
            exitActionMode();
        });
        deleteDialog.show();
    }

    //send text message
    private void sendMessage(String text) {
        if (!NetworkHelper.isConnected(this)) {
            Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
        }
        if (text.trim().isEmpty()) {
            return;
        }

        Map<String, Object> allValuesMap = SharedPreferencesManager.getAllValues();
        int gptTokensAllowed = (int) allValuesMap.getOrDefault(model.getUid() + "_" + TAG_GPT_TOKENS_ALLOWED, DEFAULT_GPT_TOKENS_ALLOWED);
        int historyBufferSize = (int) allValuesMap.getOrDefault(model.getUid() + "_" + TAG_HISTORY_BUFFER_SIZE, DEFAULT_HISTORY_BUFFER_SIZE);
        int messageMaxLength = (int) allValuesMap.getOrDefault(model.getUid() + "_" + TAG_MESSAGE_MAX_LENGTH, DEFAULT_OUTGOING_MESSAGE_LENGTH);
        float temperature = convertToFloat((int) allValuesMap.getOrDefault(model.getUid() + "_" + TAG_TEMPERATURE, DEFAULT_TEMPERATURE_INT));

        int length = text.getBytes(StandardCharsets.UTF_8).length;
        if (messageMaxLength < length) {
            InfoSnackbarUtil.showWarning(R.string.message_is_too_long, findViewById(android.R.id.content));
            return;
        }
        emojiPopup.dismiss();

        Message message = new MessageCreator.Builder(model, MessageType.SENT_TEXT).text(text).build();
        if (null != message) {
            ServiceHelper.startNetworkRequest(this, message.getMessageId(), message.getChatId());
            etMessage.setText("");
            messagesHistory.add(new com.ashomok.chatoflegends.chatgpt.model.completions.request.Message("system",
                    HeroesSystemSelector.getSystemMessage(HeroType.valueOf(model.getHeroType()))));
            messagesHistory.add(new com.ashomok.chatoflegends.chatgpt.model.completions.request.Message("user", text));
            if (messagesHistory.size() > historyBufferSize) {
                messagesHistory = messagesHistory.subList(messagesHistory.size() - historyBufferSize, messagesHistory.size());
            }
            int todayUsedRequestsCount = SharedPreferencesManager.getTodayUsedRequestsCount();
            if (todayUsedRequestsCount > DAILY_LIMIT) {
                InfoSnackbarUtil.showWarning(R.string.daily_limit_exited, findViewById(android.R.id.content));
            } else {
                RequestModel requestModel = new RequestModel(model.getModelName(), messagesHistory,
                        temperature, gptTokensAllowed);
                boolean success = chatGPTViewModel.askGPTChat(requestModel);
                if (success) {
                    SharedPreferencesManager.incrementTodayUsedRequestsCount();
                    consumeTokens(messageCost);
                }
                new Handler().postDelayed(() -> ServiceHelper.startUpdateMessageStatRequest(
                        this, message.getMessageId(), receiverUid,
                        message.getChatId(), MessageStat.READ), 1000);
            }

        }
    }

    private void loadMessagesList() {
        messageList = RealmHelper.getInstance().getMessagesInChat(receiverUid);
        observableList = RealmHelper.getInstance().getObservableList(receiverUid);
    }

    //get index from list using the id
    private int getPosFromId(String messageId) {
        Message message = new Message();
        message.setMessageId(messageId);
        return messageList.indexOf(message);
    }

    //when users selects a message
    public void onActionModeStarted() {
        //exit search and remove search from toolbar
        // if isInSearchMode
        if (isInSearchMode)
            exitSearchMode();

        //if it's not in action mode before
        //remove old menu items from toolbar
        //inflate action items and hide userInfo
        if (!isInActionMode) {
            toolbar.getMenu().clear();
            toolbar.inflateMenu(R.menu.menu_action_chat);
            hideOrShowUserInfo(true);
        }

        isInActionMode = true;
        //set items selected count as visible
        tvCounterAction.setVisibility(View.VISIBLE);
    }

    public void updateActionModeItemsCount(int itemsCount) {
        tvCounterAction.setText(String.valueOf(itemsCount));
    }

    //hide or show the views in toolbar, userImg,userName,typing and available
    private void hideOrShowUserInfo(boolean hide) {
        int visibility = hide ? View.GONE : View.VISIBLE;
        userImgToolbarChatAct.setVisibility(visibility);
        userNameToolbarChatActivity.setVisibility(visibility);
        tvTypingStatToolbar.setVisibility(visibility);
    }

    //when user click the back button
    @Override
    public void onBackPressed() {
        if (isInActionMode)
            exitActionMode();
        else if (isInSearchMode)
            exitSearchMode();
        else {
            super.onBackPressed();
        }
    }

    public void exitActionMode() {
        adapter.notifyDataSetChanged();
        isInActionMode = false;
        tvCounterAction.setVisibility(View.GONE);
        toolbar.getMenu().clear();
        //re inflate default menu
        toolbar.inflateMenu(R.menu.menu_chat);
        invalidateOptionsMenu();
        hideOrShowUserInfo(false);
        //update online and typing tvs visibility after exiting action mode
        updateToolbarTvsVisibility(TypingStat.NOT_TYPING != currentTypingState);
        viewModel.clearSelectedItems();
    }

    private void exitSearchMode() {
        isInSearchMode = false;
        searchViewToolbar.onActionViewCollapsed();
        searchGroup.setVisibility(View.GONE);
        toolbar.inflateMenu(R.menu.menu_chat);
        hideOrShowUserInfo(false);
        //update online and typing tvs visibility after exiting search mode
        updateToolbarTvsVisibility(TypingStat.NOT_TYPING != currentTypingState);
        adapter.notifyDataSetChanged();
    }

    public void showCopyItem() {
        MenuItem menuItem = toolbar.getMenu().findItem(R.id.menu_item_copy);
        if (null != menuItem)
            toolbar.getMenu().findItem(R.id.menu_item_copy).setVisible(true);
    }

    public void hideCopyItem() {
        MenuItem menuItem = toolbar.getMenu().findItem(R.id.menu_item_copy);
        if (null != menuItem)
            toolbar.getMenu().findItem(R.id.menu_item_copy).setVisible(false);
    }


    public void showShareItem() {
        MenuItem menuItem = toolbar.getMenu().findItem(R.id.menu_item_share);
        if (null != menuItem)
            toolbar.getMenu().findItem(R.id.menu_item_share).setVisible(true);
    }

    private int getLastVisibileItem() {
        return linearLayoutManager.findLastVisibleItemPosition();
    }


    @Override
    public void onContainerViewClick(int pos, @NotNull View view, @NotNull Message message) {
        if (isInActionMode) {
            viewModel.itemSelected(message);
        }
    }

    @Override
    public void onItemViewClick(int pos, @NotNull View itemView, @NotNull Message message) {
        if (isInActionMode)
            viewModel.itemSelected(message);
    }

    @Override
    public void onLongClick(int pos, @NotNull View itemView, @NotNull Message message) {
        if (!isInActionMode) {
            onActionModeStarted();
            viewModel.itemSelected(message);
        }
    }

    @Override
    public void onProgressButtonClick(int pos, @NotNull View itemView, @NotNull Message message) {

        if (isInActionMode) {
            viewModel.itemSelected(message);
        }

    }
}