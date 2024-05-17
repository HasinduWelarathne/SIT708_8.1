package com.example.chatbot;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.mlkit.nl.smartreply.SmartReply;
import com.google.mlkit.nl.smartreply.SmartReplyGenerator;
import com.google.mlkit.nl.smartreply.TextMessage;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Adapter adapter;
    private RecyclerView messagesView;
    private EditText editText;
    private Button sendButton;
    private List<Message> messages = new ArrayList<>();
    private List<TextMessage> conversation = new ArrayList<>();
    private SmartReplyGenerator smartReplyGenerator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String username = getIntent().getStringExtra("username");
        if (username == null) username = "User";

        editText = findViewById(R.id.editTextMessage);
        sendButton = findViewById(R.id.sendButton);
        messagesView = findViewById(R.id.messages_view);

        adapter = new Adapter(messages);
        messagesView.setLayoutManager(new LinearLayoutManager(this));
        messagesView.setAdapter(adapter);

        smartReplyGenerator = SmartReply.getClient();

        WelcomeMessage(username);

        sendButton.setOnClickListener(v -> {
            String userText = editText.getText().toString();
            if (!userText.isEmpty()) {
                messages.add(new Message(userText, true));
                adapter.notifyItemInserted(messages.size() - 1);
                addToConversation(userText, true);
                editText.setText("");

                generateSmartReply();
            }
        });
    }

    private void WelcomeMessage(String username) {
        String welcomeMessage = "Welcome to AI ChatBot, " + username + "!";
        messages.add(new Message(welcomeMessage, false));
        adapter.notifyItemInserted(messages.size() - 1);
        addToConversation(welcomeMessage, false);
    }

    private void generateSmartReply() {
        if (conversation.size() > 0) {
            smartReplyGenerator.suggestReplies(conversation)
                    .addOnSuccessListener(suggestions -> {
                        if (!suggestions.getSuggestions().isEmpty()) {
                            String reply = suggestions.getSuggestions().get(0).getText();
                            messages.add(new Message(reply, true));
                            adapter.notifyItemInserted(messages.size() - 1);
                            addToConversation(reply, true);
                        } else {
                            String fallbackReply = "Could you please provide more details?";
                            messages.add(new Message(fallbackReply, false));
                            adapter.notifyItemInserted(messages.size() - 1);
                            addToConversation(fallbackReply, false);
                        }
                    })
                    .addOnFailureListener(e -> {
                        String errorMessage = "Apologies, I'm having trouble understanding that.";
                        messages.add(new Message(errorMessage, false));
                        adapter.notifyItemInserted(messages.size() - 1);
                        addToConversation(errorMessage, false);
                    });
        }
    }

    private void addToConversation(String text, boolean isLocalUser) {
        TextMessage textMessage = isLocalUser ?
                TextMessage.createForLocalUser(text, System.currentTimeMillis()) :
                TextMessage.createForRemoteUser(text, System.currentTimeMillis(), "system_user_id");

        conversation.add(textMessage);

        if (conversation.size() > 1) {
            conversation.remove(0);
        }
    }
}