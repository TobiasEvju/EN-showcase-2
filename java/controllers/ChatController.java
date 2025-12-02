package com.example.demo.controllers;

import com.example.demo.apis.*;
import com.example.demo.models.*;
import com.example.demo.storage.StorageFileNotFoundException;
import com.example.demo.storage.StorageService;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.*;

@Controller
public class ChatController {

    private final StorageService storageService;

    @RequestMapping({ "chat", "chat.html"})
    public String chat() {
        return "languageerrorpage";
    }

    @Autowired
    public ChatController(StorageService storageService) {
        this.storageService = storageService;
    }

    public static void deleteFile(String path) {
        java.io.File file = new File(path);
        file.delete();
    }

    String chosenLang;

    @RequestMapping(value = "/chat", params={"lang"})
    public String setChatLang(@RequestParam String lang) {
        chosenLang = lang;
        //Checking if "lang"-parameter matches available languages
        switch(lang) {
            case "Arabic":
            case "English":
            case "French":
            case "German":
            case "Greek":
            case "Norwegian":
            case "Farsi":
            case "Polish":
            case "Somali":
            case "Spanish":
            case "Urdu":
                return "chat";
            default:
                return "languageerrorpage";
        }
    }

    @GetMapping("/getLanguage")
    @ResponseBody
    public String returnLanguage(){

        return chosenLang;
    }

    //Tar imot lydfilen fra klienten
    @PostMapping("/stt")
    @ResponseBody
    public String speechToText(@RequestParam("file") MultipartFile file, @RequestParam("language") String sttLanguage, RedirectAttributes redirectAttributes) throws Exception {

        double r = Math.random() * 100000;
        int random = (int) r;
        String name = "blob" + random;

        String wavNamePath = "AudioFiles/SpeechToTextUpload/sttFile" + random + ".wav";
        String blobPath = "AudioFiles/SpeechToTextUpload/blob" + random;

        //Lagrer filen på riktig sted
        storageService.store(file, name);

        redirectAttributes.addFlashAttribute("message",
                "You successfully uploaded " + file.getOriginalFilename() + "!");

        //Bruker ffmpeg til å konvertere filen fra blob til wav
        Process p = Runtime.getRuntime().exec("ffmpeg\\bin\\ffmpeg -y -i " + blobPath + " -ar 16000 " + wavNamePath);

        p.waitFor();

        File fil = new File(wavNamePath);
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(fil);
        AudioFormat format = audioInputStream.getFormat();
        long audioFileLength = fil.length();
        int frameSize = format.getFrameSize();
        float frameRate = format.getFrameRate();
        float durationInSeconds = (audioFileLength / (frameSize * frameRate));
        audioInputStream.close();

        if (durationInSeconds < 20) {
            //Henter configs til språket
            LanguageConfig langConfig = new LanguageConfig(sttLanguage);
            LanguageConfig langActiveConfigs = getLanguageConfigs(langConfig);

            if (langActiveConfigs.getSpeechtotextservice().equals("Google")) {
                //Metode som laster opp filen til google storage
                GoogleApis.uploadObjectToGoogleCloud(random, wavNamePath);

                deleteFile(wavNamePath);
                deleteFile(blobPath);

                return GoogleApis.sttGoogle(langActiveConfigs.getGooglespeechtotextcode(), random);

            } else if (langActiveConfigs.getSpeechtotextservice().equals("Microsoft")) {
                String ut = MicrosoftApis.sttMicrosoft(langActiveConfigs.getMicrosoftspeechtotextcode(), wavNamePath);
                deleteFile(wavNamePath);
                deleteFile(blobPath);
                return ut;

            } else if (langActiveConfigs.getSpeechtotextservice().equals("Amazon")) {
                String ut = AmazonTranscribeBatch.sttAmazon(langActiveConfigs.getAmazonspeechtotextcode(), wavNamePath, random);
                deleteFile(wavNamePath);
                deleteFile(blobPath);
                return ut;
            }else {
                return "Disabled";
            }
        }

        return "Du snakket for lenge";


    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }


    //Translate to English
    @GetMapping("/translatetoenglish")
    @ResponseBody
    public String translateToEnglish(TranslateObject translateObject) throws IOException {

            //Gets the properties from the language
            LanguageConfig langConfig = new LanguageConfig(translateObject.translateLanguageFrom);
            LanguageConfig langActiveConfigs = getLanguageConfigs(langConfig);

            translateObject.setTranslateToCode("en");

        if (langActiveConfigs.getTranslatorservice().equals("Google")) {
            translateObject.setTranslateFromCode(langActiveConfigs.getGoogletranslatorcode());

            //Special case when its translated from and to english
            if (translateObject.getTranslateFromCode().equals("en")) {
                return translateObject.getTranslateText();
            } else {
                return GoogleApis.translateGoogle(translateObject);
            }

        } else if (langActiveConfigs.getTranslatorservice().equals("Microsoft")) {
            translateObject.setTranslateFromCode(langActiveConfigs.getMicrosofttranslatorcode());

            //Special case when its translated from and to english
            if (translateObject.getTranslateFromCode().equals("en")) {
                return translateObject.getTranslateText();
            } else {
                return MicrosoftApis.translateMicrosoft(translateObject);
            }

        } else if (langActiveConfigs.getTranslatorservice().equals("Amazon")) {
            translateObject.setTranslateFromCode(langActiveConfigs.getAmazontranslatorcode());

            //Special case when its translated from and to english
            if (translateObject.getTranslateFromCode().equals("en")) {
                return translateObject.getTranslateText();
            } else {
                return AmazonTranslater.translateAmazon(translateObject);
            }
        }
        else {
            return "Translation service not found";
        }
    }

    //Translate from English
    @GetMapping("/translatefromenglish")
    @ResponseBody
    public String translateFromEnglish(TranslateObject translateObject) throws IOException {

        //Gets the properties from the language
        LanguageConfig langConfig = new LanguageConfig(translateObject.translateLanguageTo);
        LanguageConfig langActiveConfigs = getLanguageConfigs(langConfig);

        translateObject.setTranslateFromCode("en");

        if (langActiveConfigs.getTranslatorservice().equals("Google")) {
            translateObject.setTranslateToCode(langActiveConfigs.getGoogletranslatorcode());

            //Special case when its translated from and to english
            if (translateObject.getTranslateToCode().equals("en")) {
                return translateObject.getTranslateText();
            } else {
                return GoogleApis.translateGoogle(translateObject);
            }

        } else if (langActiveConfigs.getTranslatorservice().equals("Microsoft")) {
            translateObject.setTranslateToCode(langActiveConfigs.getMicrosofttranslatorcode());

            //Special case when its translated from and to english
            if (translateObject.getTranslateToCode().equals("en")) {
                return translateObject.getTranslateText();
            } else {
                return MicrosoftApis.translateMicrosoft(translateObject);
            }

        } else if   (langActiveConfigs.getTranslatorservice().equals("Amazon")) {
            translateObject.setTranslateToCode(langActiveConfigs.getAmazontranslatorcode());

            //Special case when its translated from and to english
            if (translateObject.getTranslateToCode().equals("en")) {
                return translateObject.getTranslateText();
            } else {
                return AmazonTranslater.translateAmazon(translateObject);
            }
        } else {
            return "Translation service not found";
        }
    }

    //Method for text-to-speech
    @GetMapping("/tts")
    @ResponseBody
    public String textToSpeech(LanguageConfig ttsObject) throws Exception {

        //Gets the properties from the language
        LanguageConfig langConfig = new LanguageConfig(ttsObject.ttslanguage);
        LanguageConfig langActiveConfigs = getLanguageConfigs(langConfig);

        //Checks tts service and returns from the appropriate method
        if (langActiveConfigs.getTexttospeechservice().equals("Google")) {
            ttsObject.setTtscode(langActiveConfigs.getGoogletexttospeechcode());
            return GoogleApis.ttsGoogle(ttsObject);
        }
        else if (langActiveConfigs.getTexttospeechservice().equals("Microsoft")) {
            ttsObject.setTtscode(langActiveConfigs.getMicrosofttexttospeechcode());
            return MicrosoftApis.ttsMicrosoft(ttsObject);
        }
        else if (langActiveConfigs.getTexttospeechservice().equals("Amazon")) {
            ttsObject.setTtscode(langActiveConfigs.getAmazontexttospeechcode());
            return AmazonPolly.ttsAmazon(ttsObject);
        } else {
            return "DisabledService";
        }

    }


    //Henter ut config verdiene til valgt språk
    @SuppressWarnings("DuplicatedCode")
    @ResponseBody
    public LanguageConfig getLanguageConfigs(LanguageConfig languageObject) throws IOException {
        String language = languageObject.language;


        //  The path to JSON file
        File jsonFile = new File("C:\\Serverfiles\\languages.JSON");

        BufferedReader br = new BufferedReader(new FileReader(jsonFile));
        JsonObject jsonObj = JsonParser.parseReader(br).getAsJsonObject();

        //Lager et return objekt
        LanguageConfig returnObject = new LanguageConfig(language);

        //Lager et JSON object ut fra språket
        JsonObject jsonObjLanguage = (JsonObject)jsonObj.get(language);

        ///////// Setter aktiv translator, tts og sst verdi /////////

        //Setter translator verdi
        returnObject.setTranslatorservice(jsonObjLanguage.get("Translator").getAsString());

        //Setter texttospeech verdi
        returnObject.setTexttospeechservice(jsonObjLanguage.get("Text-to-speech").getAsString());

        //Setter speechtottext verdi
        returnObject.setSpeechtotextservice(jsonObjLanguage.get("Speech-to-text").getAsString());

        ///////// Setter tjenestekoder til språket /////////

        //Setter tjenestekoder til språket
        //Google
        JsonObject jsonGoogle = jsonObjLanguage.getAsJsonObject("Google");
        returnObject.setGoogletranslatorcode(jsonGoogle.get("TranslatorCode").getAsString());
        returnObject.setGoogletexttospeechcode(jsonGoogle.get("TextToSpeechCode").getAsString());
        returnObject.setGooglespeechtotextcode(jsonGoogle.get("SpeechToTextCode").getAsString());

        //Microsoft
        JsonObject jsonMicrosoft = jsonObjLanguage.getAsJsonObject("Microsoft");
        returnObject.setMicrosofttranslatorcode(jsonMicrosoft.get("TranslatorCode").getAsString());
        returnObject.setMicrosofttexttospeechcode(jsonMicrosoft.get("TextToSpeechCode").getAsString());
        returnObject.setMicrosoftspeechtotextcode(jsonMicrosoft.get("SpeechToTextCode").getAsString());

        //Amazon
        JsonObject jsonAmazon = jsonObjLanguage.getAsJsonObject("Amazon");
        returnObject.setAmazontranslatorcode(jsonAmazon.get("TranslatorCode").getAsString());
        returnObject.setAmazontexttospeechcode(jsonAmazon.get("TextToSpeechCode").getAsString());
        returnObject.setAmazonspeechtotextcode(jsonAmazon.get("SpeechToTextCode").getAsString());


        //Sjekker om noen av tjenestene er disabled

        //Translate
        if (returnObject.getTranslatorservice().equals("Disabled")) {
            returnObject.setActiveTranslatorCode("Disabled");
        }

        //Text-to-speech
        if (returnObject.getTexttospeechservice().equals("Disabled")) {
            returnObject.setActiveTTSCode("Disabled");
        }

        //Speech-to-text
        if (returnObject.getSpeechtotextservice().equals("Disabled")) {
            returnObject.setActiveSSTCode("Disabled");
        }
        return returnObject;
    }

    @GetMapping("/getLanguageStatus")
    @ResponseBody
    public LanguageConfig getLanguageServicesStatus() throws IOException {
        String language = chosenLang;

        //  The path to JSON file
        File jsonFile = new File("C:\\Serverfiles\\languages.JSON");

        BufferedReader br = new BufferedReader(new FileReader(jsonFile));
        JsonObject jsonObj = JsonParser.parseReader(br).getAsJsonObject();

        //Lager et return objekt
        LanguageConfig returnObject = new LanguageConfig(language);

        //Lager et JSON object ut fra språket
        JsonObject jsonObjLanguage = (JsonObject)jsonObj.get(language);

        //Setter texttospeech verdi
        returnObject.setTexttospeechservice(jsonObjLanguage.get("Text-to-speech").getAsString());

        //Setter speechtottext verdi
        returnObject.setSpeechtotextservice(jsonObjLanguage.get("Speech-to-text").getAsString());

        return returnObject;
    }

    @PostMapping("/boostConversation")
    @ResponseBody
    public BoostResponseViewModel boostConversation(BoostSendDTO message) throws IOException {

        BoostResponse boostResponse = BoostApi.boostConversation(message);
        BoostResponseViewModel boostResponseViewModel = new BoostResponseViewModel();
        try {
            boostResponseViewModel.setLinkResponse(boostResponse.getLinkResponse());
        } catch (Exception e) { }

        try {
            boostResponseViewModel.setTextResponse(boostResponse.getTextResponse());
        } catch (Exception e) { }
        boostResponseViewModel.setConversationId(boostResponse.getConversationId());
        return boostResponseViewModel;
    }
}
