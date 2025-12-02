package com.example.demo.controllers;

import com.example.demo.models.LanguageConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.io.*;

@Controller
public class AdminController {

    @RequestMapping({ "admin", "admin.html", "admin.html/", "admin/"})
    public String chat() {
        return "admin";
    }

    @GetMapping("/getConfig")
    @ResponseBody
    public LanguageConfig getLanguageServices(LanguageConfig languageObject) throws IOException {
        String language = languageObject.language;

        //Fetching the JSON configuration file
        File jsonFile = new File("C:\\Serverfiles\\languages.JSON");
        BufferedReader br = new BufferedReader(new FileReader(jsonFile));

        JsonObject jsonObj = JsonParser.parseReader(br).getAsJsonObject();

        //Creates a JSON object based on the language
        JsonObject jsonObjLanguage = (JsonObject)jsonObj.get(language);

        //Creates a return object
        LanguageConfig returnObject = new LanguageConfig(language);

        //Sets translator values
        String translator=jsonObjLanguage.get("Translator").getAsString();
        returnObject.setTranslatorservice(translator);

        //Sets text-to-speech values
        String texttospeech = jsonObjLanguage.get("Text-to-speech").getAsString();
        returnObject.setTexttospeechservice(texttospeech);

        //Setter speech-to-text values
        String speechtottext = jsonObjLanguage.get("Speech-to-text").getAsString();
        returnObject.setSpeechtotextservice(speechtottext);

        //Sets service codes

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

        return returnObject;
    }


    @GetMapping("/setConfig")
    @ResponseBody
    public void setNewLanguageServices(LanguageConfig languageConfig) throws Exception {

        //Fetching the JSON configuration file
        File jsonFile = new File("C:\\Serverfiles\\languages.JSON");
        BufferedReader br = new BufferedReader(new FileReader(jsonFile));

        JsonObject jsonObj = JsonParser.parseReader(br).getAsJsonObject();
        JsonObject obj = (JsonObject)jsonObj.get(languageConfig.getLanguage());



        //Updates JSON object with new properties
        //Checking if received input from radio menu is valid (to prevent JSON file from being corrupted)
        if ((languageConfig.getTranslatorservice().equals("Google") ||
            languageConfig.getTranslatorservice().equals("Microsoft") ||
            languageConfig.getTranslatorservice().equals("Amazon")) &&
            !obj.get(languageConfig.getTranslatorservice()).getAsJsonObject().get("TranslatorCode").getAsString().equals("Disabled")) {

                obj.addProperty("Translator", languageConfig.getTranslatorservice());
        }

        if ((languageConfig.getTexttospeechservice().equals("Google") ||
            languageConfig.getTexttospeechservice().equals("Microsoft") ||
            languageConfig.getTexttospeechservice().equals("Amazon")) &&
            !obj.get(languageConfig.getTexttospeechservice()).getAsJsonObject().get("TextToSpeechCode").getAsString().equals("Disabled")) {

                obj.addProperty("Text-to-speech", languageConfig.getTexttospeechservice());
        }

        if ((languageConfig.getSpeechtotextservice().equals("Google") ||
            languageConfig.getSpeechtotextservice().equals("Microsoft") ||
            languageConfig.getSpeechtotextservice().equals("Amazon")) &&
            !obj.get(languageConfig.getSpeechtotextservice()).getAsJsonObject().get("SpeechToTextCode").getAsString().equals("Disabled")) {

                obj.addProperty("Speech-to-text", languageConfig.getSpeechtotextservice());
        }

        //Prettifying the JSON format to make it readable
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        //Outputs to file
        OutputStream outputStream = new FileOutputStream(jsonFile);
        outputStream.write(gson.toJson(jsonObj).getBytes());
        outputStream.flush();
        outputStream.close();
    }
}
