package com.example.test.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.test.Message;
import com.example.test.Post;
import com.example.test.R;
import com.parse.DeleteCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {

    private final List<Message> mMessages;
    private final Context mContext;
    private final String mUserId;
    private static final int MESSAGE_OUTGOING = 123;
    private static final int MESSAGE_INCOMING = 321;
    private static final String DELETE = "Delete?";
    private static final String POSITIVE_BTN = "Yes";
    private static final String NEGATIVE_BTN = "Cancel";

    private static final String PROFILE_PIC = "profilePicture";




    @Override
    public int getItemViewType(int position) {
        if (isMe(position)) {
            return MESSAGE_OUTGOING;
        } else {
            return MESSAGE_INCOMING;
        }
    }

    public ChatAdapter(@NonNull Context context, @NonNull String userId, @NonNull List<Message> messages) {
        mMessages = messages;
        this.mUserId = userId;
        mContext = context;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        if (viewType == MESSAGE_INCOMING) {
            View contactView = inflater.inflate(R.layout.message_incoming, parent, false);
            return new IncomingMessageViewHolder(contactView);
        } else if (viewType == MESSAGE_OUTGOING) {
            View contactView = inflater.inflate(R.layout.message_outgoing, parent, false);
            return new OutgoingMessageViewHolder(contactView);
        } else {
            throw new IllegalArgumentException("Unknown view type");
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = mMessages.get(position);
        holder.bindMessage(message);

    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }
    public abstract class MessageViewHolder extends RecyclerView.ViewHolder {

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        abstract void bindMessage(Message message);
    }

    public class IncomingMessageViewHolder extends MessageViewHolder {
        private final ImageView imageOther;
        private final TextView body;
        private final TextView name;

        public IncomingMessageViewHolder(View itemView) {
            super(itemView);
            imageOther = (ImageView)itemView.findViewById(R.id.ivProfileOther);
            body = (TextView)itemView.findViewById(R.id.tvBody);
            name = (TextView)itemView.findViewById(R.id.tvName);
        }

        @Override
        public void bindMessage(Message message) {

            body.setText(message.getBody());
            name.setText(message.getUser().getUsername());
            ParseFile profileImg = (ParseFile) message.getUser().get(PROFILE_PIC);
            if (profileImg != null) {
                Glide.with(mContext).load(profileImg.getUrl()).circleCrop().into(imageOther);
            }
        }
    }

    public class OutgoingMessageViewHolder extends MessageViewHolder {
        private final ImageView imageMe;
        private final TextView body;
        private RelativeLayout chatContainer;

        public OutgoingMessageViewHolder(View itemView) {
            super(itemView);
            imageMe = (ImageView)itemView.findViewById(R.id.ivProfileMe);
            body = (TextView)itemView.findViewById(R.id.tvBody);
            chatContainer = itemView.findViewById(R.id.chatContainer);
        }

        @Override
        public void bindMessage(Message message) {
            if(ParseUser.getCurrentUser() != null)
            {
                ParseUser user = ParseUser.getCurrentUser();
                if(user.getParseFile(PROFILE_PIC) != null){
                    Glide.with(mContext)
                            .load(user.getParseFile(PROFILE_PIC).getUrl())
                            .circleCrop() // create an effect of a round profile picture
                            .into(imageMe);
                }

            }
            body.setText(message.getBody());

            chatContainer.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog.Builder saveDialog = new AlertDialog.Builder(mContext);
                    saveDialog.setTitle(DELETE);
                    saveDialog.setPositiveButton(POSITIVE_BTN, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            int position = getAdapterPosition();
                            Message message = mMessages.get(position);
                            message.deleteInBackground(new DeleteCallback() {
                                @Override
                                public void done(ParseException e2) {
                                    if(e2==null){
                                        Toast.makeText(mContext, "Delete Successful", Toast.LENGTH_SHORT).show();
                                    }else{
                                        Toast.makeText(mContext, "Error: "+e2.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                            mMessages.remove(position);
                            notifyDataSetChanged();
                        }
                    });
                    saveDialog.setNegativeButton(NEGATIVE_BTN, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    saveDialog.show();
                    return true;
                }
            });
        }
    }

    private boolean isMe(int position) {
        Message message = mMessages.get(position);
        Log.i("user", "user is " + message.getUserId() +" "+ "mUserId is " + mUserId);
        if (mUserId.equals(message.getUserId())){
            return true;
        }
        else {
            return false;
        }
    }

}
