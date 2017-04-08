package com.sparq.application.userinterface.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.sparq.R;
import com.sparq.application.SPARQApplication;
import com.sparq.application.userinterface.ConverstaionThreadActivity;
import com.sparq.application.userinterface.PollResultsActivity;
import com.sparq.application.userinterface.QuestionareActivity;
import com.sparq.application.userinterface.model.ConversationThread;
import com.sparq.application.userinterface.model.PollItem;
import com.sparq.application.userinterface.model.Questionare;

import java.text.SimpleDateFormat;
import java.util.List;

import static com.sparq.application.SPARQApplication.USER_TYPE.TEACHER;
import static com.sparq.application.userinterface.model.Questionare.QUESTIONARE_TYPE.POLL;

/**
 * Created by sarahcs on 2/25/2017.
 */

public class PollListAdapter extends RecyclerView.Adapter<PollListAdapter.MyViewHolder>{

    private Context mContext;
    private List<PollItem> polls;

    private static int colors[] = {
            R.color.warning,
            R.color.warningDark,
    };

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView pollName;
        public TextView pollDate;
        public TextView pollQuestions;
        public LinearLayout pollImage;
        public ImageView pollStatusImage;
        public CardView cardView;

        public MyViewHolder(View view) {
            super(view);
            pollName = (TextView) view.findViewById(R.id.poll_name);
            pollDate = (TextView) view.findViewById(R.id.poll_date);
            pollQuestions = (TextView) view.findViewById(R.id.poll_questions);
            pollImage = (LinearLayout) view.findViewById(R.id.poll_image);
            pollStatusImage = (ImageView) view.findViewById(R.id.poll_status);
            cardView = (CardView) view.findViewById(R.id.card_view);
        }
    }


    public PollListAdapter(Context mContext, List<PollItem> polls) {
        this.mContext = mContext;
        this.polls = polls;
    }

    @Override
    public PollListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_poll_list, parent, false);

        return new PollListAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(PollListAdapter.MyViewHolder holder, int position) {
        final PollItem poll = polls.get(position);
        holder.pollName.setText(poll.getName());
        holder.pollDate.setText(new SimpleDateFormat("yyyy-MM-dd").format(poll.getDate()));
        holder.pollQuestions.setText(String.valueOf(poll.getNumberOfQuestions()));

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(SPARQApplication.getUserType() == TEACHER){
                    Intent intent = new Intent(mContext, PollResultsActivity.class);
                    intent.putExtra(PollResultsActivity.QUESTIONARE_TYPE, POLL);
                    intent.putExtra(PollResultsActivity.QUESTIONARE_ID, poll.getQuestionareId());
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                }else{
                    Intent intent = new Intent(mContext, QuestionareActivity.class);
                    intent.putExtra(QuestionareActivity.QUESTIONARE_TYPE, POLL);
                    intent.putExtra(QuestionareActivity.QUESTIONARE_ID, poll.getQuestionareId());
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                }
            }
        });

//        ColorGenerator generator = ColorGenerator.MATERIAL; // or use DEFAULT
//        // generate color based on a key (same key returns the same color), useful for list/grid views
//        int color = generator.getColor(poll.getName());
//
//        TextDrawable drawable = TextDrawable.builder()
//                .buildRound(String.valueOf(poll.getName().charAt(0)), color);
//        holder.pollImage.setImageDrawable(drawable);

        switch(poll.getState()){
            case PLAY:
                holder.pollStatusImage.setBackgroundResource(R.drawable.ic_bookmark_play);
                break;
            case PAUSE:
                holder.pollStatusImage.setBackgroundResource(R.drawable.ic_bookmark_pause);
                break;
            case STOP:
                holder.pollStatusImage.setBackgroundResource(R.drawable.ic_bookmark_stop);
                break;
        }

        if(position % 2 == 0){
            holder.pollImage.setBackgroundColor(mContext.getResources().getColor(colors[0]));
        }
        else{
            holder.pollImage.setBackgroundColor(mContext.getResources().getColor(colors[1]));
        }

    }

    @Override
    public int getItemCount() {
        return polls.size();
    }
}
