package com.example.demo.models;

public class Translate {
    public String translateText, translateFromCode, translateToCode, translateLanguageFrom, translateLanguageTo,
                    translatorService;

    public Translate(String translateText, String translateFromCode, String translateToCode) {
        this.translateText = translateText;
        this.translateFromCode = translateFromCode;
        this.translateToCode = translateToCode;
    }

    public String getTranslateText() {
        return translateText;
    }

    public void setTranslateText(String translateText) {
        this.translateText = translateText;
    }

    public String getTranslateFromCode() {
        return translateFromCode;
    }

    public void setTranslateFromCode(String translateFromCode) {
        this.translateFromCode = translateFromCode;
    }

    public String getTranslateToCode() {
        return translateToCode;
    }

    public void setTranslateToCode(String translateToCode) {
        this.translateToCode = translateToCode;
    }

    public String getTranslatorService() {
        return translatorService;
    }

    public void setTranslatorService(String translatorService) {
        this.translatorService = translatorService;
    }

    public String getTranslateLanguageFrom() { return translateLanguageFrom; }

    public void setTranslateLanguageFrom(String translateLanguage) { this.translateLanguageFrom = translateLanguage; }

    public String getTranslateLanguageTo() { return translateLanguageTo; }

    public void setTranslateLanguageTo(String translateLanguageTo) { this.translateLanguageTo = translateLanguageTo; }
}
