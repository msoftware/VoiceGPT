package com.jentsch.voicegpt;

import static com.theokanning.openai.completion.chat.ChatMessageRole.*;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.jentsch.voicegpt.adapters.ChatViewListAdapter;
import com.jentsch.voicegpt.listener.ChatClickListener;
import com.jentsch.voicegpt.db.entity.ChatMessage;

public class ChatView extends RelativeLayout implements ChatClickListener {

    private CardView inputFrame;
    public RecyclerView chatListView;
    private EditText inputEditText;
    private MicrophoneClickListener microphoneClickListener;
    private SendClickListener sendClickListener;

    private FloatingActionButton sendButton;
    private FloatingActionButton micButton;
    private boolean previousFocusState = false, useEditorAction, isTyping;
    private TypingListener typingListener;
    private ChatClickListener chatClickListener = null;
    private ProgressBar progressBar;

    private Runnable typingTimerRunnable = new Runnable() {
        @Override
        public void run() {
            if (isTyping) {
                isTyping = false;
                if (typingListener != null) typingListener.userStoppedTyping();
            }
        }
    };

    public ChatViewListAdapter chatViewListAdapter;
    private TypedArray attributes, textAppearanceAttributes;
    private Context context;
    private boolean sendButtonClickable = true;

    ChatView(Context context) {
        this(context, null);
    }

    public ChatView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChatView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        LayoutInflater.from(getContext()).inflate(R.layout.chat_view, this, true);
        this.context = context;
        initializeViews();
        getXMLAttributes(attrs, defStyleAttr);
        setViewAttributes();
        setListAdapter();
        setButtonClickListeners();
        setUserTypingListener();
        setUserStoppedTypingListener();
    }

    public void setChatClickListener(ChatClickListener chatClickListener) {
        this.chatClickListener = chatClickListener;
    }

    private void initializeViews() {
        chatListView = findViewById(R.id.chat_list);
        inputFrame = findViewById(R.id.input_frame);
        inputEditText = findViewById(R.id.input_edit_text);
        sendButton = findViewById(R.id.sendButton);
        micButton = findViewById(R.id.micButton);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(GONE);
    }

    private void getXMLAttributes(AttributeSet attrs, int defStyleAttr) {
        attributes = context.obtainStyledAttributes(attrs, R.styleable.ChatView, defStyleAttr, R.style.ChatViewDefault);
        getAttributesForInputText();
        getUseEditorAction();
        attributes.recycle();
    }

    private void setListAdapter() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setStackFromEnd(true);
        chatViewListAdapter = new ChatViewListAdapter(context, this);
        chatListView.setAdapter(chatViewListAdapter);
        chatListView.setLayoutManager(layoutManager);
    }

    private void setViewAttributes() {
        setUseEditorAction();
    }

    public void setMicrophoneClickListener(MicrophoneClickListener microphoneClickListener) {
        this.microphoneClickListener = microphoneClickListener;
    }
    public void setSendClickListener(SendClickListener sendClickListener) {
        this.sendClickListener = sendClickListener;
    }

    private void getAttributesForInputText() {
        if (hasStyleResourceSet()) {
            setTextAppearanceAttributes();
            textAppearanceAttributes.recycle();
        }
    }


    private void setTextAppearanceAttributes() {
        final int textAppearanceId = attributes.getResourceId(R.styleable.ChatView_inputTextAppearance, 0);
        textAppearanceAttributes = getContext().obtainStyledAttributes(textAppearanceId, R.styleable.ChatViewInputTextAppearance);
    }

    private void getUseEditorAction() {
        useEditorAction = attributes.getBoolean(R.styleable.ChatView_inputUseEditorAction, false);
    }

    private void setUseEditorAction() {
        if (useEditorAction) {
            setupEditorAction();
        } else {
            inputEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        }
    }

    private boolean hasStyleResourceSet() {
        return attributes.hasValue(R.styleable.ChatView_inputTextAppearance);
    }

    private void setupEditorAction() {
        inputEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        inputEditText.setImeOptions(EditorInfo.IME_ACTION_SEND);
        inputEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    long stamp = System.currentTimeMillis();
                    String message = inputEditText.getText().toString();

                    if (!TextUtils.isEmpty(message)) {
                        sendMessage(message, stamp);
                    }
                    return true;
                }
                return false;
            }
        });
    }

    private void setButtonClickListeners() {
        sendButton.setOnClickListener(v -> onSendClick());
        micButton.setOnClickListener(v -> onMicrophoneClick());
    }

    private void onSendClick() {
        if (sendButtonClickable) {
            if (sendClickListener != null) {
                sendClickListener.onSendClick(inputEditText.getText().toString());
            }
        }
    }

    private void onMicrophoneClick() {
        if (microphoneClickListener != null) {
            microphoneClickListener.onMicrophoneClick();
        }
    }

    private void setUserTypingListener() {
        inputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {

                    if (!isTyping) {
                        isTyping = true;
                        if (typingListener != null) typingListener.userStartedTyping();
                    }

                    removeCallbacks(typingTimerRunnable);
                    postDelayed(typingTimerRunnable, 1500);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void setUserStoppedTypingListener() {
        inputEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (previousFocusState && !hasFocus && typingListener != null) {
                    typingListener.userStoppedTyping();
                }
                previousFocusState = hasFocus;
            }
        });
    }

    @Override
    protected boolean addViewInLayout(View child, int index, ViewGroup.LayoutParams params) {
        return super.addViewInLayout(child, index, params);
    }

    public String getTypedMessage() {
        return inputEditText.getText().toString();
    }

    public void setTypingListener(TypingListener typingListener) {
        this.typingListener = typingListener;
    }

    public void sendMessage(String message, long stamp) {
        ChatMessage chatMessage = new ChatMessage(USER.value(), message, System.currentTimeMillis());
        chatViewListAdapter.addMessage(chatMessage);
        chatListView.scrollToPosition(chatViewListAdapter.getItemCount() - 1);
        inputEditText.setText("");
    }

    public void clearMessages() {
        chatViewListAdapter.clearMessages();
        chatViewListAdapter.notifyDataSetChanged();
    }


    public void addMessage(ChatMessage chatMessage) {
        chatViewListAdapter.addMessage(chatMessage);
    }

    public void disableMicButton() {
        micButton.setVisibility(GONE);
    }

    public void setMessage(String content) {
        inputEditText.setText(content);
    }

    @Override
    public void onChatItemClick(ChatMessage chatMessage, LinearProgressIndicator progressBar) {
        if (chatClickListener != null) {
            chatClickListener.onChatItemClick(chatMessage, progressBar);
        }
    }

    public void showProgress(boolean show) {
        if (show) {
            progressBar.setVisibility(VISIBLE);
            sendButton.setVisibility(INVISIBLE);
            sendButtonClickable = false;
        } else {
            progressBar.setVisibility(INVISIBLE);
            sendButton.setVisibility(VISIBLE);
            sendButtonClickable = true;
        }
    }

    public interface TypingListener {
        void userStartedTyping();
        void userStoppedTyping();
    }

    interface MicrophoneClickListener {
        void onMicrophoneClick();
    }

    interface SendClickListener {
        void onSendClick(String message);
    }
}
