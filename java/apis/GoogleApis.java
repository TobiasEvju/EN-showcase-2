package com.example.demo.apis;

import com.example.demo.models.Credentials;
import com.example.demo.models.LanguageConfig;
import com.example.demo.models.TranslateObject;
import com.google.cloud.speech.v1.*;
import com.google.cloud.storage.*;
import com.google.cloud.texttospeech.v1.*;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.google.protobuf.ByteString;
import org.apache.tomcat.util.codec.binary.Base64;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class GoogleApis {

    //Metode som oversetter text, tar inn et objekt med (tekst, originalspråk og språk det skal til)
    public static String translateGoogle(TranslateObject translateObject) {

        //Setter API nøkkel så vi får tilgang til tjenesten
        Translate translate = TranslateOptions.newBuilder().setApiKey(Credentials.getGOOGLESUBKEYAPI()).build().getService();

        //Bygger det som skal oversettes med getmetoder fra objektet som ble sendt inn
        Translation translation =
                translate.translate(
                        translateObject.getTranslateText(),
                        com.google.cloud.translate.Translate.TranslateOption.sourceLanguage(translateObject.getTranslateFromCode()),
                        com.google.cloud.translate.Translate.TranslateOption.targetLanguage(translateObject.getTranslateToCode()),
                        com.google.cloud.translate.Translate.TranslateOption.model("base"));

        return translation.getTranslatedText();
    }


    //Metode for text-to-speech (Google)
    public static String ttsGoogle(LanguageConfig langObject) throws Exception {
        String ttsText = langObject.ttstext;
        String ttsCode = langObject.ttscode;

        try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create()) {

            SynthesisInput input = SynthesisInput.newBuilder().setText(ttsText).build();

            // Bygger en stemme forespørsel
            VoiceSelectionParams voice =
                    VoiceSelectionParams.newBuilder()
                            .setLanguageCode(ttsCode)
                            .setSsmlGender(SsmlVoiceGender.FEMALE)
                            .build();

            // Velger hvilken type lydfil som skal returneres
            AudioConfig audioConfig =
                    AudioConfig.newBuilder()
                            .setAudioEncoding(AudioEncoding.MP3) // MP3 fil.
                            .build();

            // Utfører text-to-speech forespørselen
            SynthesizeSpeechResponse response =
                    textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);

            // Henter lyden fra svaret
            ByteString audioContents = response.getAudioContent();

            // Skriver lyden til en lydfil som lagres


            //Gjør bytearrayen til lydfilen om til en base64 string som kan sendes til klienten
            Base64 b = new Base64();
            return b.encodeToString(audioContents.toByteArray());

        }
    }

    //Metode for speechToText, blir kjørt fra handleFileUpload
    public static String sttGoogle(String langCode, int random) throws IOException {

        try (SpeechClient speechClient = SpeechClient.create()) {


            // Path'en til lydfilen som skal brukes
            String gcsUri = "gs://bachleorwebapp2/sttFile" + random + ".wav";



            // Bygger forespørselen, her må sampleRateHertz (standard er 16000) og språkkode endres
            RecognitionConfig config =
                    RecognitionConfig.newBuilder()
                            .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                            .setSampleRateHertz(16000)
                            .setLanguageCode(langCode)
                            .build();
            RecognitionAudio audio = RecognitionAudio.newBuilder().setUri(gcsUri).build();

            // Utfører språkgjennkjenning på lydfilen
            RecognizeResponse response = speechClient.recognize(config, audio);
            List<SpeechRecognitionResult> results = response.getResultsList();

            deleteObject(random);

            //Lar dette stå som det er for ellers blir det en rar feil
            for (SpeechRecognitionResult result : results) {
                // Det kan være flere svar fra API'en
                // Velger den første siden det er den mest sannsynlige.
                SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
                System.out.printf("Transcription: %s%n", alternative.getTranscript());
                return alternative.getTranscript();
            }
            return "0";


        } catch (IOException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    //Laster opp et objekt til google storage
    public static void uploadObjectToGoogleCloud(int random, String path) throws IOException, InterruptedException {
        // ID til google cloud storage project
        String projectId = "savvy-hybrid-313710";

        // ID til google cloud storage bucket
        String bucketName = "bachleorwebapp2";

        // ID til google cloud storage objekt
        String objectName = "sttFile" + random + ".wav";

        // path'en til filen som skal lastes opp
        String filePath = path;

        //Kode som laster filen opp
        Storage storage = StorageOptions.newBuilder().setProjectId(projectId).build().getService();
        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
        storage.create(blobInfo, Files.readAllBytes(Paths.get(filePath)));

        boolean compleate = false;
        while (!compleate) {
            Blob blob = storage.get(BlobId.of(bucketName, objectName));
            if (blob.exists()) {
                compleate = true;
            } else {
                TimeUnit.MILLISECONDS.sleep(50);
            }
        }
    }


    public static void deleteObject(int random) {
        // ID til google cloud storage project
        String projectId = "savvy-hybrid-313710";

        // ID til google cloud storage bucket
        String bucketName = "bachleorwebapp2";

        // ID til google cloud storage objekt
        String objectName = "sttFile" + random + ".wav";

        Storage storage = StorageOptions.newBuilder().setProjectId(projectId).build().getService();
        storage.delete(bucketName, objectName);
    }

}
