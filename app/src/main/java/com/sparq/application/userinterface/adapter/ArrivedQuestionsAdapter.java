package com.sparq.application.userinterface.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.sparq.R;
import com.sparq.application.userinterface.PollResultsPerQuestionActivity;
import com.sparq.application.userinterface.model.EventItem;
import com.sparq.application.userinterface.model.PollItem;
import com.sparq.application.userinterface.model.QuestionItem;

import java.util.ArrayList;
import java.util.List;

import static com.sparq.R.string.events;
import static com.sparq.application.userinterface.model.Questionare.QUESTIONARE_TYPE.POLL;

public class ArrivedQuestionsAdapter extends RecyclerView.Adapter<ArrivedQuestionsAdapter.MyViewHolder> {

    private PollItem poll;
    private ArrayList<QuestionItem> questions;
    private Context mContext;

    private static int colors[] = {
            R.color.orange,
            R.color.orangeDark,
    };

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView questionName;
        public TextView questionFormat;
        public TextView answerNumber;
        public LinearLayout questionImage;
        public CardView cardView;

        public MyViewHolder(View view) {
            super(view);
            questionName = (TextView) view.findViewById(R.id.question_name);
            questionFormat = (TextView) view.findViewById(R.id.question_format);
            answerNumber = (TextView) view.findViewById(R.id.no_answers);
            questionImage = (LinearLayout) view.findViewById(R.id.question_image);
            cardView = (CardView) view.findViewById(R.id.card_view);
        }
    }


    public ArrivedQuestionsAdapter(Context context, PollItem poll) {
        this.mContext = context;
        this.poll = poll;
        this.questions = new ArrayList<>(poll.getQuestions().values());
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_arrived_question, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final QuestionItem question = questions.get(position);
        holder.questionName.setText(question.getQuestion());
        holder.questionFormat.setText(QuestionItem.getFormatAsString(question.getFormat()));

        if(poll.getAnswersForQuestion(question.getQuestionId()) != null){
            holder.answerNumber.setText(String.valueOf(poll.getAnswersForQuestion(question.getQuestionId()).size()));

            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // create intent to go to the next activity
                    Intent intent = new Intent(mContext, PollResultsPerQuestionActivity.class);
                    intent.putExtra(PollResultsPerQuestionActivity.QUESTIONARE_TYPE, POLL);
                    intent.putExtra(PollResultsPerQuestionActivity.QUESTIONARE_ID, question.getQuestionareId());
                    intent.putExtra(PollResultsPerQuestionActivity.QUESTION_FORMAT, question.getFormat());
                    intent.putExtra(PollResultsPerQuestionActivity.QUESTION_ID, question.getQuestionId());
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                }
            });

        }
        else{
            holder.answerNumber.setText(String.valueOf(0));

            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Toast.makeText(mContext, mContext.getResources().getString(R.string.no_answer), Toast.LENGTH_SHORT).show();
                }
            });
        }



        if(position % 2 == 0){
            holder.questionImage.setBackgroundColor(mContext.getResources().getColor(colors[0]));
        }
        else{
            holder.questionImage.setBackgroundColor(mContext.getResources().getColor(colors[1]));
        }

    }

    @Override
    public int getItemCount() {
        return questions.size();
    }
}