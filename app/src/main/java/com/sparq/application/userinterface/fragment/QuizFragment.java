package com.sparq.application.userinterface.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.sparq.R;
import com.sparq.application.SPARQApplication;
import com.sparq.application.userinterface.EventActivity;
import com.sparq.application.userinterface.NotifyPollHandler;
import com.sparq.application.userinterface.NotifyQuizHandler;
import com.sparq.application.userinterface.adapter.EventListAdapter;
import com.sparq.application.userinterface.adapter.PollListAdapter;
import com.sparq.application.userinterface.adapter.QuizListAdapter;
import com.sparq.application.userinterface.adapter.RecyclerItemClickListener;
import com.sparq.application.userinterface.model.EventItem;
import com.sparq.application.userinterface.model.QuizItem;
import com.sparq.application.userinterface.model.UserItem;

import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;

public class QuizFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private ArrayList<QuizItem> quizzesArrayList = new ArrayList<>();
    private RecyclerView recyclerView;
    private TextView emptyView;

    private QuizListAdapter mAdapter;

    public QuizFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment QuizFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static QuizFragment newInstance(String param1, String param2) {
        QuizFragment fragment = new QuizFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_quiz, container, false);

        initializeView(view);

        quizzesArrayList = SPARQApplication.getQuizzes();

        if(quizzesArrayList.size() == 0){
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        }
        else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }

        initializeQuizAdapater();

        return view;
    }

    public void initializeView(View view){

        recyclerView = (RecyclerView) view.findViewById(R.id.quiz_recycler_view);
        emptyView = (TextView) view.findViewById(R.id.empty_view);
    }

    public void initializeQuizAdapater(){
        mAdapter = new QuizListAdapter(getActivity(),quizzesArrayList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onResume(){
        super.onResume();

        NotifyQuizHandler quizHandler = new NotifyQuizHandler() {
            @Override
            public void handleQuizQuestions() {

                if(quizzesArrayList.size() == 0){
                    recyclerView.setVisibility(View.GONE);
                    emptyView.setVisibility(View.VISIBLE);
                }
                else {
                    recyclerView.setVisibility(View.VISIBLE);
                    emptyView.setVisibility(View.GONE);
                }

                initializeQuizAdapater();
            }

            @Override
            public void handleQuizAnswers() {
                // if an answer has arrived prevent the user from answering again
                initializeQuizAdapater();
            }
        };

        SPARQApplication.setQuizNotifier(quizHandler);
    }

    public void showStartDialog(QuizItem quiz){

        MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .title(quiz.getName())
                .customView(R.layout.dialog_quiz_details, true)
                .positiveText("START")
                .negativeText("CANCEL")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        // start quiz activity
                    }
                }).onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                }).build();

        View view = dialog.getCustomView();

        TextView dialogDate = (TextView) view.findViewById(R.id.dialog_quiz_date);
        dialogDate.setText(quiz.getDate().toString());
        TextView dialogDescription = (TextView) view.findViewById(R.id.dialog_quiz_description);
        dialogDescription.setText(quiz.getDescription());
        TextView dialogNoQuestions = (TextView) view.findViewById(R.id.dialog_quiz_no_questions);
        dialogNoQuestions.setText("20");
        TextView dialogDuration = (TextView) view.findViewById(R.id.dialog_quiz_duration);
        dialogDuration.setText(String.valueOf(quiz.getDuration()));
        TextView dialogMaxMarks = (TextView) view.findViewById(R.id.dialog_quiz_max_marks);
        dialogMaxMarks.setText(String.valueOf(quiz.getTotalMarks()));


        dialog.show();
    }

    public void showFinishDialog(QuizItem quiz){

        MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .title(quiz.getName())
                .customView(R.layout.dialog_after_quiz_details, true)
                .positiveText("OKAY")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                }).build();

        View view = dialog.getCustomView();

        TextView dialogDate = (TextView) view.findViewById(R.id.dialog_quiz_date);
        dialogDate.setText(quiz.getDate().toString());
        TextView dialogNoQuestions = (TextView) view.findViewById(R.id.dialog_quiz_no_questions);
        dialogNoQuestions.setText("20");
        TextView dialogDuration = (TextView) view.findViewById(R.id.dialog_quiz_no_attempted);
        dialogDuration.setText(String.valueOf(quiz.getDuration()));
        TextView dialogMaxMarks = (TextView) view.findViewById(R.id.dialog_quiz_no_correct);
        dialogMaxMarks.setText(String.valueOf(quiz.getTotalMarks()));


        dialog.show();
    }

}
