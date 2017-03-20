package com.sparq.application.layer.pdu;

import java.nio.charset.Charset;

/**
 * Created by sarahcs on 3/20/2017.
 */

public class ThreadPdu extends ApplicationLayerPdu{

    private final static int THREAD_ID_BYTES = 1;
    private final static int THREAD_CREATOR_ID_BYTES = 1;
    private final static int SUB_THREAD_ID_BYTES = 1;
    private final static int ANSWERER_ID_BYTES = 1;

    private final static int PDU_QUESTION_HEADER_BYTES = TYPE_BYTES + THREAD_ID_BYTES;
    private final static int PAYLOAD_QUESTION_MAX_BYTES = TOT_SIZE - PDU_QUESTION_HEADER_BYTES;

    private final static int PDU_ANSWER_HEADER_BYTES = TYPE_BYTES + THREAD_CREATOR_ID_BYTES + THREAD_ID_BYTES + SUB_THREAD_ID_BYTES;
    private final static int PAYLOAD_ANSWER_MAX_BYTES = TOT_SIZE - PDU_ANSWER_HEADER_BYTES;

    private final static int PDU_QUESTION_VOTE_HEADER_BYTES = TYPE_BYTES + THREAD_CREATOR_ID_BYTES + THREAD_ID_BYTES;
    private final static int PAYLOAD_QUESTION_VOTE_MAX_BYTES = TOT_SIZE - PDU_QUESTION_VOTE_HEADER_BYTES;

    private final static int PDU_ANSWER_VOTE_HEADER_BYTES = TYPE_BYTES + THREAD_CREATOR_ID_BYTES + THREAD_ID_BYTES + SUB_THREAD_ID_BYTES + ANSWERER_ID_BYTES;
    private final static int PAYLOAD_ANSWER_VOTE_MAX_BYTES = TOT_SIZE - PDU_ANSWER_VOTE_HEADER_BYTES;

    private final static int PAYLOAD_MAX_BYTES = Math.max(
            PAYLOAD_QUESTION_MAX_BYTES,
            Math.max(PAYLOAD_ANSWER_MAX_BYTES,
                    Math.max(PAYLOAD_QUESTION_VOTE_MAX_BYTES,
                            PAYLOAD_ANSWER_VOTE_MAX_BYTES))
    );

    private TYPE mType;
    private byte mCreatorId;
    private byte mThreadId;
    private byte mSubthreadId;
    private byte mAnswererId;
    private byte[] mData;

    private ThreadPdu(TYPE type, byte creatorId, byte threadId, byte subThreadId, byte answererId, byte[] data) {
        super(type);

        this.mType = type;
        this.mCreatorId = creatorId;
        this.mThreadId = threadId;
        this.mSubthreadId = subThreadId;
        this.mAnswererId = answererId;

        if (mData.length > PAYLOAD_MAX_BYTES) {
            throw new IllegalArgumentException("Payload size greater than max (received "
                    + data.length + " max " + PAYLOAD_MAX_BYTES + " bytes)");
        }

        this.mData = data;
    }

    public static ThreadPdu getQuestionPdu(byte threadId, byte[] data){
        return new ThreadPdu(TYPE.QUESTION, (byte) 0, threadId, (byte) 0, (byte) 0, data);
    }

    public static ThreadPdu getAnswerPdu(byte creatorId, byte threadId, byte subThreadId, byte[] data){
        return new ThreadPdu(TYPE.ANSWER, creatorId, threadId, subThreadId, (byte) 0, data);
    }

    public static ThreadPdu getQuestionVotePdu(byte creatorId, byte threadId, byte[] data){
        return new ThreadPdu(TYPE.QUESTION_VOTE, creatorId, threadId, (byte) 0, (byte) 0, data);
    }

    public static ThreadPdu getAnswerVotePdu(byte creatorId, byte threadId, byte subThreadId, byte answererId, byte[] data){
        return new ThreadPdu(TYPE.ANSWER_VOTE, creatorId, threadId, subThreadId, answererId, data);
    }

    public static boolean isValidPdu(String encoded) {
        return encoded != null && isValidPdu(encoded.getBytes(CHARSET));
    }

    public static boolean isValidPdu(byte[] encoded) {

        if (encoded.length < TYPE_BYTES + THREAD_ID_BYTES) {
            return false;
        }

        return true;
    }

    public byte getThreadCreatorId(){
        return this.mCreatorId;
    }

    public byte getThreadId(){
        return this.mThreadId;
    }

    public byte getmSubthreadId(){
        return this.mSubthreadId;
    }

    private byte getAnswererId(){
        return this.mAnswererId;
    }

    public String getAsString() {
        return new String(encode(), CHARSET);
    }

    @Override
    public byte[] encode() {
        int headerSize = 0;
        switch (getType()) {
            case QUESTION:
                headerSize = PDU_QUESTION_HEADER_BYTES;
                break;
            case ANSWER:
                headerSize = PDU_ANSWER_HEADER_BYTES;
                break;
            case QUESTION_VOTE:
                headerSize = PDU_QUESTION_VOTE_HEADER_BYTES;
                break;
            case ANSWER_VOTE:
                headerSize = PDU_ANSWER_VOTE_HEADER_BYTES;
                break;
        }
        byte[] encoded = new byte[headerSize + mData.length];

        int nextFieldIndex = 0;

        // add Type
        encoded[nextFieldIndex] = getTypeEncoded(mType);
        nextFieldIndex += TYPE_BYTES;

        //add thread ID
        encoded[nextFieldIndex] = getThreadId();
        nextFieldIndex += THREAD_ID_BYTES;

        switch(getType()){
            case QUESTION:
                break;
            case ANSWER:
                encoded[nextFieldIndex] = getThreadCreatorId();
                nextFieldIndex += THREAD_CREATOR_ID_BYTES;

                break;
            case QUESTION_VOTE:
                encoded[nextFieldIndex] = getThreadCreatorId();
                nextFieldIndex += THREAD_CREATOR_ID_BYTES;

                encoded[nextFieldIndex] = getmSubthreadId();
                nextFieldIndex += SUB_THREAD_ID_BYTES;

                break;
            case ANSWER_VOTE:
                encoded[nextFieldIndex] = getThreadCreatorId();
                nextFieldIndex += THREAD_CREATOR_ID_BYTES;

                encoded[nextFieldIndex] = getmSubthreadId();
                nextFieldIndex += SUB_THREAD_ID_BYTES;

                encoded[nextFieldIndex] = getAnswererId();
                nextFieldIndex += ANSWERER_ID_BYTES;
                break;
        }

        // add the actual data to send
        System.arraycopy(mData, 0, encoded, nextFieldIndex, mData.length);
        return encoded;
    }

    @Override
    public ThreadPdu decode(byte[] encoded) {

        int nextFieldIndex = 0;
        // get type
        TYPE type = getTypeDecoded(encoded[nextFieldIndex]);
        nextFieldIndex += TYPE_BYTES;
        // get threadID
        byte threadID = encoded[nextFieldIndex];
        nextFieldIndex += THREAD_ID_BYTES;

        byte creatorID = (byte) 0;
        byte subThreadId = (byte) 0;
        byte answererId = (byte) 0;

        switch(type){
            case QUESTION:
                break;
            case ANSWER:
                creatorID = encoded[nextFieldIndex];
                nextFieldIndex += THREAD_CREATOR_ID_BYTES;

                break;
            case QUESTION_VOTE:
                creatorID = encoded[nextFieldIndex];
                nextFieldIndex += THREAD_CREATOR_ID_BYTES;

                subThreadId = encoded[nextFieldIndex];
                nextFieldIndex += SUB_THREAD_ID_BYTES;

                break;
            case ANSWER_VOTE:
                creatorID = encoded[nextFieldIndex];
                nextFieldIndex += THREAD_CREATOR_ID_BYTES;

                subThreadId = encoded[nextFieldIndex];
                nextFieldIndex += SUB_THREAD_ID_BYTES;

                answererId = encoded[nextFieldIndex];
                nextFieldIndex += ANSWERER_ID_BYTES;
                break;
        }

        // get the actual data
        byte[] data = new byte[encoded.length - nextFieldIndex];
        System.arraycopy(encoded, nextFieldIndex, data, 0, data.length);

        return new ThreadPdu(type, creatorID, threadID, subThreadId, answererId, data);
    }
}

