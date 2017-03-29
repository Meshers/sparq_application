package com.sparq.application;

import android.app.Activity;
import android.app.Application;
import android.util.Log;

import com.sparq.application.layer.ApplicationLayerManager;
import com.sparq.application.layer.ApplicationPacketDiscoveryHandler;
import com.sparq.application.layer.almessage.AlAnswer;
import com.sparq.application.layer.almessage.AlMessage;
import com.sparq.application.layer.almessage.AlQuestion;
import com.sparq.application.layer.almessage.AlVote;
import com.sparq.application.layer.pdu.ApplicationLayerPdu;
import com.sparq.application.userinterface.NotifyUIHandler;
import com.sparq.application.userinterface.model.AnswerItem;
import com.sparq.application.userinterface.model.ConversationThread;
import com.sparq.application.userinterface.model.UserItem;
import com.sparq.util.Constants;

import java.util.ArrayList;
import java.util.Date;

import test.com.blootoothtester.bluetooth.MyBluetoothAdapter;

/**
 * Created by sarahcs on 3/26/2017.
 */

public class SPARQApplication extends Application {

    private static final String TAG = "SPARQApplication";
    static MyBluetoothAdapter mBluetoothAdapter;
    static ApplicationPacketDiscoveryHandler mHandler;
    static ApplicationLayerManager mManager;

    private static byte mSessionId = (byte) 1;
    private static byte mOwnAddr = (byte) 1;
    private static final byte mBroadcastId = test.com.blootoothtester.util.Constants.PDU_BROADCAST_ADDR;

    private static int currentQuestionId = 1;
    private static int currentAnswerId = 1;

    //handlers
    static NotifyUIHandler uihandler;


    private static ArrayList<ConversationThread> conversationThreads = new ArrayList<>();

    @Override
    public void onCreate(){
        super.onCreate();
    }

    public static byte getOwnAddress(){
        return mOwnAddr;
    }

    public static void setOwnAddr(byte ownAddr){
        mOwnAddr = ownAddr;
    }

    public static byte getSessionId(){
        return mSessionId;
    }

    public static void setSessionId(byte sessionId){
        mSessionId = sessionId;
    }

    public static byte getBdcastAddress(){
        return mBroadcastId;
    }

    public static int getCurrentAnswerId() {
        return currentAnswerId;
    }

    public static int getCurrentQuestionId() {
        return currentQuestionId;
    }

    public static void initializeObjects(Activity activity){

    }

    public static void setUINotifier(NotifyUIHandler handler){
        uihandler = handler;
    }

    public static ConversationThread getConversationThread(int questionId, int creatorId){

        for(ConversationThread thread: conversationThreads){
            if(thread.getQuestionareId() == questionId
                    && thread.getCreator().getUserId() == creatorId){
                return thread;
            }
        }

        return null;
    }

    public static void notifyConversationThread(){
        Log.i("ConverstaionThread", "check null");
        if(uihandler != null){
            uihandler.handleConversationThreadQuestions();
            uihandler.handleConversationThreadAnswers();
            uihandler.handleConversationThreadAnswerVotes();
        }
    }

    public static void handlePackets(ApplicationLayerPdu.TYPE type, AlMessage alMessage){
        switch(type){
            case QUESTION:

                AlQuestion alQuestion = (AlQuestion) alMessage;
                Log.i(TAG,"RECEIVED MESSAGE: "
                        + alQuestion.getCreatorId() + ":" +alQuestion.getQuestionId() + ":" + alQuestion.getDataAsString());

                // create a conversation thread
                ConversationThread newThread = new ConversationThread(
                        alQuestion.getQuestionId(),
                        getSessionId(),
                        new Date(),
                        new UserItem(alQuestion.getCreatorId()),
                        alQuestion.getDataAsString()
                );

                conversationThreads.add(newThread);

                notifyConversationThread();

                break;
            case ANSWER:

                AlAnswer alAnswer = (AlAnswer) alMessage;
                Log.i(TAG, "RECEIVED MESSAGE: "
                        + alAnswer.getCreatorId() + ":"
                        + alAnswer.getQuestionId()+ ":"
                        + alAnswer.getAnswerCreatorId()+ ":"
                        + alAnswer.getAnswerId()+ ":"
                        + alAnswer.getAnswerDataAsString()
                );

                AnswerItem newAnswer = new AnswerItem(
                        alAnswer.getAnswerId(),
                        new UserItem(alAnswer.getAnswerCreatorId()),
                        alAnswer.getQuestionId(),
                        new UserItem(alAnswer.getCreatorId()),
                        alAnswer.getAnswerDataAsString(),
                        Constants.INITIAL_VOTE_COUNT
                );

                getConversationThread(
                        alAnswer.getQuestionId(), alAnswer.getCreatorId()
                ).addAnswerToList(newAnswer);

                notifyConversationThread();

                break;
            case QUESTION_VOTE:

                AlVote alQuestionVote = (AlVote) alMessage;
                Log.i(TAG, "RECEIVED MESSAGE: "
                        + alQuestionVote.getCreatorId() + ":"
                        + alQuestionVote.getQuestionId() +":"
                        + alQuestionVote.getVoteValue()
                );

                ConversationThread thread = getConversationThread(alQuestionVote.getQuestionId(), alQuestionVote.getCreatorId());

                if(thread != null){
                    switch(alQuestionVote.getVoteValue()){
                        case UPVOTE:
                            thread.getQuestionItem().addUpVote();
                            break;
                        case DOWNVOTE:
                            thread.getQuestionItem().addDownVote();
                            break;
                    }
                }

                notifyConversationThread();

                break;
            case ANSWER_VOTE:

                AlVote alAnswerVote = (AlVote) alMessage;
                Log.i(TAG, "RECEIVED MESSAGE: "
                        + alAnswerVote.getCreatorId() + ":"
                        + alAnswerVote.getQuestionId()+ ":"
                        + alAnswerVote.getAnswerCreatorId()+ ":"
                        + alAnswerVote.getAnswerId()+ ":"
                        + alAnswerVote.getVoteValue()
                );

                AnswerItem answer = getConversationThread(
                        alAnswerVote.getQuestionId(), + alAnswerVote.getCreatorId()
                ).getAnswer(alAnswerVote.getAnswerId(), alAnswerVote.getAnswerCreatorId());

                if(answer != null){
                    switch(alAnswerVote.getVoteValue()){
                        case UPVOTE:
                            answer.addUpVote();
                            break;
                        case DOWNVOTE:
                            answer.addDownVote();
                            break;
                    }
                }

                notifyConversationThread();

                break;
            default:
                throw new IllegalArgumentException("Illegal message type.");
        }
    }

    public static void sendMessage(ApplicationLayerPdu.TYPE type, byte toAddr, String msg,
                                   int creatorId, int questionId, int answerCreatotId, int answerId,
                                   AlVote.VOTE_TYPE voteType){

        boolean isSent = false;

        switch(type){
            case QUESTION:

                // create a conversation thread

                isSent = mManager.sendData(
                        ApplicationLayerPdu.TYPE.QUESTION,
                        msg,
                        toAddr,
                        (byte) creatorId, (byte) questionId, (byte) 0, (byte) 0);

                if(isSent){
                    ConversationThread newThread = new ConversationThread(
                            questionId,
                            getSessionId(),
                            new Date(),
                            new UserItem(creatorId),
                            msg
                    );
                    conversationThreads.add(newThread);

                    currentQuestionId ++;

                    notifyConversationThread();
                }

                break;
            case ANSWER:

                isSent = mManager.sendData(
                        ApplicationLayerPdu.TYPE.ANSWER,
                        msg,
                        toAddr,
                        (byte) creatorId, (byte) questionId, (byte) answerCreatotId, (byte) answerId);

                if(isSent){
                    AnswerItem newAnswer = new AnswerItem(
                            answerId,
                            new UserItem(answerCreatotId),
                            questionId,
                            new UserItem(creatorId),
                            msg,
                            Constants.INITIAL_VOTE_COUNT
                    );

                    getConversationThread(questionId, creatorId).addAnswerToList(newAnswer);

                    currentAnswerId++;

                    notifyConversationThread();
                }

                break;
            case QUESTION_VOTE:

                isSent = mManager.sendData(
                        ApplicationLayerPdu.TYPE.QUESTION_VOTE,
                        new String(
                                new byte[]{AlVote.getVoteEncoded(voteType)},
                                Constants.CHARSET
                        ),
                        toAddr,
                        (byte) creatorId, (byte) questionId, (byte) 0, (byte) 0);

                if(isSent){
                    ConversationThread thread = getConversationThread(questionId, creatorId);

                    if(thread != null){
                        switch(voteType){
                            case UPVOTE:
                                thread.getQuestionItem().addUpVote();
                                break;
                            case DOWNVOTE:
                                thread.getQuestionItem().addDownVote();
                                break;
                        }
                    }

                    notifyConversationThread();
                }

                break;
            case ANSWER_VOTE:

                isSent = mManager.sendData(
                        ApplicationLayerPdu.TYPE.ANSWER_VOTE,
                        new String(
                                new byte[]{AlVote.getVoteEncoded(voteType)},
                                Constants.CHARSET
                        ),
                        toAddr,
                        (byte) creatorId, (byte) questionId, (byte) answerCreatotId, (byte) answerId);

                if(isSent){
                    AnswerItem answer = getConversationThread(questionId, creatorId)
                            .getAnswer(answerId, answerCreatotId);

                    if(answer != null){
                        switch(voteType){
                            case UPVOTE:
                                answer.addUpVote();
                                break;
                            case DOWNVOTE:
                                answer.addDownVote();
                                break;
                        }
                    }

                    notifyConversationThread();
                }

                break;
        }

    }


//    public static MyBluetoothAdapter getBluetoothAdapter(){
//        return mBluetoothAdapter;
//    }
//
//    public static ApplicationPacketDiscoveryHandler getHandler(){
//        return mHandler;
//    }
//
    public static void setApplicationLayerManager(ApplicationLayerManager manager){
        mManager = manager;
    }

    public static ArrayList<ConversationThread> getConversationThreads(){
        return conversationThreads;
    }

}
