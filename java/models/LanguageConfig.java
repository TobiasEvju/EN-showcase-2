package com.example.demo.models;

public class LanguageConfig {
    public String language,
            //Services
            translatorservice, texttospeechservice, speechtotextservice,
            //Codes
            activeTranslatorCode, activeTTSCode, activeSSTCode,
            //Google
            googletranslatorcode, googletexttospeechcode, googlespeechtotextcode,
            //Microsoft
            microsofttranslatorcode, microsofttexttospeechcode, microsoftspeechtotextcode,
            //Amazon
            amazontranslatorcode, amazontexttospeechcode, amazonspeechtotextcode,

            //TTS-innsendelser
            ttstext, ttslanguage, ttscode;


    public LanguageConfig(String language) {
        this.language=language;
    }

    public String getLanguage() { return language; }

    public void setLanguage(String language) { this.language = language; }

    public String getTranslatorservice() { return translatorservice; }

    public void setTranslatorservice(String translatorservice) { this.translatorservice = translatorservice; }

    public String getTexttospeechservice() { return texttospeechservice; }

    public void setTexttospeechservice(String texttospeechservice) { this.texttospeechservice = texttospeechservice; }

    public String getSpeechtotextservice() { return speechtotextservice; }

    public void setSpeechtotextservice(String speechtotextservice) { this.speechtotextservice = speechtotextservice; }

    public String getGoogletranslatorcode() { return googletranslatorcode; }

    public void setGoogletranslatorcode(String googletranslatorcode) { this.googletranslatorcode = googletranslatorcode; }

    public String getGoogletexttospeechcode() { return googletexttospeechcode; }

    public void setGoogletexttospeechcode(String googletexttospeechcode) { this.googletexttospeechcode = googletexttospeechcode; }

    public String getGooglespeechtotextcode() { return googlespeechtotextcode; }

    public void setGooglespeechtotextcode(String googlespeechtotextcode) { this.googlespeechtotextcode = googlespeechtotextcode; }

    public String getMicrosofttranslatorcode() { return microsofttranslatorcode; }

    public void setMicrosofttranslatorcode(String microsofttranslatorcode) { this.microsofttranslatorcode = microsofttranslatorcode; }

    public String getMicrosofttexttospeechcode() { return microsofttexttospeechcode; }

    public void setMicrosofttexttospeechcode(String microsofttexttospeechcode) { this.microsofttexttospeechcode = microsofttexttospeechcode; }

    public String getMicrosoftspeechtotextcode() { return microsoftspeechtotextcode; }

    public void setMicrosoftspeechtotextcode(String microsoftspeechtotextcode) { this.microsoftspeechtotextcode = microsoftspeechtotextcode; }

    public String getAmazontranslatorcode() { return amazontranslatorcode; }

    public void setAmazontranslatorcode(String amazontranslatorcode) { this.amazontranslatorcode = amazontranslatorcode; }

    public String getAmazontexttospeechcode() { return amazontexttospeechcode; }

    public void setAmazontexttospeechcode(String amazontexttospeechcode) { this.amazontexttospeechcode = amazontexttospeechcode; }

    public String getAmazonspeechtotextcode() { return amazonspeechtotextcode; }

    public void setAmazonspeechtotextcode(String amazonspeechtotextcode) { this.amazonspeechtotextcode = amazonspeechtotextcode; }

    public String getActiveTranslatorCode() { return activeTranslatorCode; }

    public void setActiveTranslatorCode(String activeTranslatorCode) { this.activeTranslatorCode = activeTranslatorCode; }

    public String getActiveTTSCode() { return activeTTSCode; }

    public void setActiveTTSCode(String activeTTSCode) { this.activeTTSCode = activeTTSCode; }

    public String getActiveSSTCode() { return activeSSTCode; }

    public void setActiveSSTCode(String activeSSTCode) { this.activeSSTCode = activeSSTCode; }

    public String getTtstext() { return ttstext; }

    public void setTtstext(String ttstext) { this.ttstext = ttstext; }

    public String getTtslanguage() { return ttslanguage; }

    public void setTtslanguage(String ttslanguage) { this.ttslanguage = ttslanguage; }

    public String getTtscode() { return ttscode; }

    public void setTtscode(String ttscode) { this.ttscode = ttscode; }
}
