package com.example.demo.apis;

//Azure
import com.example.demo.models.Credentials;
import com.example.demo.models.LanguageConfig;
import com.example.demo.models.TranslateObject;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.cognitiveservices.speech.*;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;
import com.squareup.okhttp.*;
import org.apache.tomcat.util.codec.binary.Base64;

import java.io.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class MicrosoftApis {

    //Method that translates text using the objects variables (text, language from, language to)
    public static String translateMicrosoft(TranslateObject translateObject) throws IOException {

        //Creates a Http url
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host("api.cognitive.microsofttranslator.com")
                .addPathSegment("/translate")
                .addQueryParameter("api-version", "3.0")
                .addQueryParameter("from", translateObject.getTranslateFromCode())
                .addQueryParameter("to", translateObject.getTranslateToCode())
                .build();

        //Creates a HTTP Client
        OkHttpClient client = new OkHttpClient();

        //Creates a JSON object with the desired text
        JsonObject jsonBodyIn = new JsonObject();
        jsonBodyIn.addProperty("text", translateObject.getTranslateText());

        //Creates and sends a Http request
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType,
                "["+jsonBodyIn+"]");
        Request request = new Request.Builder().url(url).post(body)
                .addHeader("Ocp-Apim-Subscription-Key", Credentials.getMICROSOFTSUBKEYTRANSLATE())
                .addHeader("Ocp-Apim-Subscription-Region", Credentials.getMICROSOFTLOCATION())
                .addHeader("Content-type", "application/json")
                .build();
        Response response = client.newCall(request).execute();

        String outPut = response.body().string();                    //Creates string from response
        String outPutFix = outPut.substring( 1, outPut.length()-1);  //Removes brackets

        //Creates a JSON object and a JSON array from the response to receive the translated text
        JsonObject jsonBodyOut = JsonParser.parseString(outPutFix).getAsJsonObject();
        JsonArray jsonBodyOutArray = jsonBodyOut.getAsJsonArray("translations");

        return jsonBodyOutArray.get(0).getAsJsonObject().get("text").getAsString();
    }

    //Method that returns a Base64 string representing the recognized speech from desired text and language
    public static String ttsMicrosoft(LanguageConfig langObject) {
        String ttsText = langObject.ttstext;
        String ttsCode = langObject.ttscode;;

        //Creates a SpeechConfig object containing the microsoft credentials and the language code
        SpeechConfig speechConfig = SpeechConfig.fromSubscription(Credentials.getMICROSOFTSUBKEYSPEECH(), Credentials.getMICROSOFTLOCATION());
        speechConfig.setSpeechSynthesisLanguage(ttsCode);

        SpeechSynthesizer synthesizer = new SpeechSynthesizer(speechConfig, null);
        SpeechSynthesisResult result = synthesizer.SpeakText(ttsText);

        byte[] bArray = result.getAudioData();

        Base64 b = new Base64();
        return b.encodeToString(bArray);
    }

    //Method for speech-to-text
    public static String sttMicrosoft(String langCode, String path) throws  ExecutionException, InterruptedException {

        //Creates a SpeechConfig object containing the microsoft credentials and the language code
        SpeechConfig speechConfig = SpeechConfig.fromSubscription(Credentials.getMICROSOFTSUBKEYSPEECH(), Credentials.getMICROSOFTLOCATION());
        speechConfig.setSpeechRecognitionLanguage(langCode);

        //Sends a recognize request from the file
        AudioConfig audioConfig = AudioConfig.fromWavFileInput(path);
        SpeechRecognizer recognizer = new SpeechRecognizer(speechConfig, audioConfig);

        Future<SpeechRecognitionResult> task = recognizer.recognizeOnceAsync();
        SpeechRecognitionResult result = task.get();

        recognizer.close();

        //If no result is returned from the response
        if (result.getText().equals("")) {
            return "0";
        } else {
            return result.getText();
        }
    }
}
