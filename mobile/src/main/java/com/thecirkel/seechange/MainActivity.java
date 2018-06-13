package com.thecirkel.seechange;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private ArrayList<ChatMessage> listItems;
    private Button connectBtn, disconnectBtn, sendMessageBtn;
    private EditText messageText;
    private TextView statusText;
    private Socket mSocket;
    private ListView messageListView;
    private BaseAdapter adapter;

    private Boolean isConnected = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listItems = new ArrayList<>();

        adapter = new ChatMessageAdapter(getApplicationContext(),
                getLayoutInflater(), listItems);

        messageListView = findViewById(R.id.messageListView);
        messageListView.setAdapter(adapter);

        ChatApplication app = new ChatApplication();
        mSocket = app.getSocket();
        mSocket.on(Socket.EVENT_CONNECT, onConnect);
        mSocket.on(Socket.EVENT_DISCONNECT,onDisconnect);
        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.on("chat_message", onNewMessage);

        statusText = findViewById(R.id.statusText);
        messageText = findViewById(R.id.messageText);

        connectBtn = findViewById(R.id.connectBtn);
        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSocket.connect();
            }
        });

        disconnectBtn = findViewById(R.id.disconnectBtn);
        disconnectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSocket.disconnect();
            }
        });

        sendMessageBtn = findViewById(R.id.sendMessageBtn);
        sendMessageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptSend();
            }
        });
    }

    private void attemptSend() {
        if (!mSocket.connected()) return;

        String message = messageText.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
            messageText.requestFocus();
            return;
        }

        Log.i(TAG, message);

        messageText.setText("");
        // perform the sending message attempt.

        JSONObject data = new JSONObject();
        try {
            data.put("message", message);
            data.put("username", "kayvon");
            data.put("room", "room 1");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mSocket.emit("chat_message", data);
    }

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(!isConnected) {
                        Log.i(TAG, "connected");
                        statusText.setText("Connected");

                        JSONObject data = new JSONObject();
                        try {
                            data.put("room", "room 1");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        mSocket.emit("join_room", data);
                        isConnected = true;
                    }
                }
            });
        }
    };

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e(TAG, "Error connecting");
                    statusText.setText("Connection error");
                }
            });
        }
    };

    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "disconnected");
                    statusText.setText("Disconnected");
                    isConnected = false;
                }
            });
        }
    };

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String message;
                    try {
                        message = data.getString("message");
                    } catch (JSONException e) {
                        Log.e(TAG, e.getMessage());
                        return;
                    }

                    addMessage(message);
                }
            });
        }
    };

    private void addMessage(String message) {
        ChatMessage messagetext = new ChatMessage(message);

        listItems.add(messagetext);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mSocket.disconnect();
        mSocket.off(Socket.EVENT_CONNECT, onConnect);
        mSocket.off(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
    }
}