/**
    Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.

    Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at

        http://aws.amazon.com/apache2.0/

    or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package alexa;

import alexa.battleship.ResponseHelper;
import com.amazon.speech.ui.SsmlOutputSpeech;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.Speechlet;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * This sample shows how to create a simple speechlet for handling speechlet requests.
 */
public class BattleShipSpeechlet implements Speechlet {
    private static final Logger log = LoggerFactory.getLogger(BattleShipSpeechlet.class);
    private static final String SLOT_LETTER = "Letter";
    private static final String SLOT_NUMBER = "Number";
    private static final String SESSION_LETTER = "choosenLetter";
    private static final String SESSION_NUMBER = "choosenNumber";
    private ResourceBundle messages;
    private ResponseHelper rHelper;

    @Override
    public void onSessionStarted(final SessionStartedRequest request, final Session session){
        log.info("onSessionStarted requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());
        Locale deLocale = new Locale("de","DE");
        messages = ResourceBundle.getBundle("ApplicationMessages", deLocale);
        rHelper = new ResponseHelper(messages);
    }

    @Override
    public SpeechletResponse onLaunch(final LaunchRequest request, final Session session) {
        log.info("onLaunch requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
        return getWelcomeResponse();
    }

    @Override
    public SpeechletResponse onIntent(final IntentRequest request, final Session session)
            throws SpeechletException {
        log.info("onIntent requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());

        Intent intent = request.getIntent();
        String intentName = (intent != null) ? intent.getName() : null;

        if ("NewstartIntent".equals(intentName)) {
            return getNewstartResponse();
        } else if ("AMAZON.HelpIntent".equals(intentName)) {
            return getHelpResponse();
        } else if ("ShotIntent".equals(intentName)) {
            return getShotResponse(request.getIntent(), session);
        } else {
            throw new SpeechletException("Invalid Intent");
        }
    }

    @Override
    public void onSessionEnded(final SessionEndedRequest request, final Session session) {
        log.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
        // any cleanup logic goes here
    }

    private SpeechletResponse getShotResponse(Intent intent, Session session) {
        String speechText = "<speak>";
        String eventPlayerResult = "";
        String eventComputerResult = "";
        if (intent.getSlot(SLOT_LETTER).getValue() == null && intent.getSlot(SLOT_NUMBER).getValue() == null) {
            speechText = messages.getString("alexa.battleship.what");
        } else {
            String letter = intent.getSlot(SLOT_LETTER).getValue().trim();
            int number = Integer.valueOf(intent.getSlot(SLOT_NUMBER).getValue());

            try {
                String urlString = "http://localhost:8080/shot?letter=" + letter + "&number=" + number;
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Accecpt", "application/json");

                if (conn.getResponseCode() != 200) {

                    throw new RuntimeException("Failed: HTTP error code: " + conn.getResponseCode());
                }
                //how do I get json object and print it as string
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        (conn.getInputStream()))); // Getting the response from the webservice

                StringBuilder sb = new StringBuilder();

                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                JSONObject json = new JSONObject(sb.toString());
                conn.disconnect();
                log.info(String.valueOf(conn.getResponseCode())+json);
                eventPlayerResult = json.getString("playerEventResult");
                eventComputerResult = json.getString("computerEventResult");
                log.info(String.valueOf(conn.getResponseCode()+eventPlayerResult));
                if (!eventPlayerResult.isEmpty()) {
                    eventPlayerResult = rHelper.handlePlayerShotEvent(eventPlayerResult);
                }
                if(!eventComputerResult.isEmpty()) {
                    eventComputerResult = rHelper.handleComputerShotEvent(eventComputerResult);
                }
                log.info(String.valueOf(conn.getResponseCode()+eventPlayerResult));

            }  catch (IOException e) {
                speechText = messages.getString("alexa.battleship.connection.error");
                log.error("alexa.battleship.connection.error",e);
            }


            speechText += "Ich schiesse auf " + letter + " " + number;
            speechText += "<break time=\"1s\"/> "+eventPlayerResult;
            speechText += "<break time=\"1s\"/> "+eventComputerResult;
            speechText += "</speak>";

            session.setAttribute(SESSION_LETTER, letter);
            session.setAttribute(SESSION_NUMBER, number);
        }

        // Create the plain text output.
        SsmlOutputSpeech speech = new SsmlOutputSpeech();
        speech.setSsml(speechText);

        return SpeechletResponse.newAskResponse(speech, createRepromptSpeech());
    }

    /**
     * Creates and returns a {@code SpeechletResponse} with a welcome message.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getWelcomeResponse() {
        String speechText = messages.getString("alexa.battleship.welcome.text");

        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("Schiffe versenken");
        card.setContent(speechText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        // Create reprompt
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(speech);

        return SpeechletResponse.newAskResponse(speech, reprompt, card);
    }

    /**
     * Creates a {@code SpeechletResponse} for the Newstart intent.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getNewstartResponse() {
        String speechText = messages.getString("alexa.battleship.newstart.text");
        try {
            String urlString = "http://localhost:8080/newstart";
            URL url = new URL(urlString);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accecpt", "application/json");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed: HTTP error code: " + conn.getResponseCode());
            }
            conn.disconnect();
        } catch (IOException e){
            log.error("alexa.battleship.connection.error",e);
            speechText = messages.getString("alexa.battleship.connection.error");
        }finally {
            // Create the Simple card content.
            SimpleCard card = new SimpleCard();
            card.setTitle("Newstart");
            card.setContent(speechText);

            // Create the plain text output.
            PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
            speech.setText(speechText);
            Reprompt reprompt = new Reprompt();
            reprompt.setOutputSpeech(speech);

            return SpeechletResponse.newAskResponse(speech, reprompt, card);
        }
    }

    /**
     * Creates a {@code SpeechletResponse} for the help intent.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getHelpResponse() {
        String speechText = messages.getString("alexa.battleship.help");

        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("Help");
        card.setContent(speechText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        // Create reprompt
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(speech);

        return SpeechletResponse.newAskResponse(speech, reprompt, card);
    }

    private Reprompt createRepromptSpeech() {
        PlainTextOutputSpeech repromptSpeech = new PlainTextOutputSpeech();
        repromptSpeech.setText(messages.getString("alexa.battleship.what"));
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(repromptSpeech);
        return reprompt;
    }
}