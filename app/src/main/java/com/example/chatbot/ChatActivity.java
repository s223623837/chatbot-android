package com.example.chatbot;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ChatActivity extends AppCompatActivity {

    private EditText userMessage;
    private LinearLayout messageContainer;
    private TextView greeting;
    private ImageButton sendButton;
    private String username;
    private ScrollView scrollView;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        userMessage = findViewById(R.id.userMessage);
        messageContainer = findViewById(R.id.messageContainer);
        greeting = findViewById(R.id.greeting);
        sendButton = findViewById(R.id.sendButton);
        scrollView = findViewById(R.id.scrollView);

        username = getIntent().getStringExtra("USERNAME");
        greeting.setText("Welcome, " + username + "!");

        // Initialize the RequestQueue
        requestQueue = Volley.newRequestQueue(this);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = userMessage.getText().toString();
                if (!message.isEmpty()) {
                    addMessageToChat(message, "user");
                    sendMessageToServer(message);
                    userMessage.setText("");
                }
            }
        });
    }

    private void addMessageToChat(String message, String sender) {
        TextView textView = new TextView(this);
        textView.setText(message);
        if (sender.equals("user")) {
            textView.setBackgroundResource(android.R.color.white);
        } else {
            textView.setBackgroundResource(android.R.color.darker_gray);
        }
        textView.setPadding(16, 16, 16, 16);
        messageContainer.addView(textView);
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }

    private void sendMessageToServer(String message) {
        String url = "http://10.0.2.2:5000/chat";  // Updated URL for the chat endpoint

        JSONObject jsonInput = new JSONObject();
        try {
            jsonInput.put("userMessage", message);
            jsonInput.put("chatHistory", new JSONArray());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonInput,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String serverMessage = response.getString("message");
                            addMessageToChat(serverMessage, "bot");
                        } catch (JSONException e) {
                            e.printStackTrace();
                            addMessageToChat("Error parsing response", "bot");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        addMessageToChat("Error communicating with server", "bot");
                    }
                }
        );

        // Add the request to the RequestQueue.
        requestQueue.add(jsonObjectRequest);
    }

}
