package com.lvqingyang.chatroom.tool;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lvqingyang.chatroom.R;
import com.lvqingyang.chatroom.bean.Message;

import java.util.List;

/**
 * @author Lv Qingyang
 * @date 2017/10/11
 * @email biloba12345@gamil.com
 * @github https://github.com/biloba123
 * @blog https://biloba123.github.io/
 */
public class MessageAdapter  extends RecyclerView.Adapter<MessageAdapter.MessageVH>{
    private Context mContext;
    private List<Message> mMessageList;
    private static final int TYPE_SEND = 836;
    public static final int TYPE_RECEIVE = 386;

    public MessageAdapter(Context context, List<Message> messageList) {
        mContext = context;
        mMessageList = messageList;
    }

    @Override
    public MessageVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view=null;
        if (viewType==TYPE_RECEIVE) {
            view=View.inflate(mContext, R.layout.item_message_receive, null);
        }else {
            view=View.inflate(mContext, R.layout.item_message_send, null);
        }
        return new MessageVH(view);
    }

    @Override
    public void onBindViewHolder(MessageVH holder, int position) {
        holder.bindMessage(mMessageList.get(position));
    }

    @Override
    public int getItemViewType(int position) {
        if (mMessageList.get(position).isReceive()) {
            return TYPE_RECEIVE;
        }else {
            return TYPE_SEND;
        }
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    class MessageVH extends RecyclerView.ViewHolder{

        private final TextView mTvContent;
        private final TextView mTvNick;

        public MessageVH(View itemView) {
            super(itemView);
            mTvContent = (TextView) itemView.findViewById(R.id.tv_content);
            mTvNick = (TextView) itemView.findViewById(R.id.tv_nick);
        }

        public void bindMessage(Message msg){
            if (msg.isReceive()) {
                mTvNick.setText(msg.getNick());
            }else {
                mTvNick.setText("我");
            }
            mTvContent.setText(msg.getContent());
        }
    }
}
