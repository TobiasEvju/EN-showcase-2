package com.example.demo.models;

public class Credentials {
    private static final String MICROSOFTSUBKEYTRANSLATE = "";
    private static final String MICROSOFTSUBKEYSPEECH = "";
    private static final String MICROSOFTLOCATION = "northeurope";

    private static final String GOOGLESUBKEYAPI = "";

    private static final String AMAZONACCESSKEYTRANSLATE = "";
    private static final String AMAZONSECRETKEYTRANSLATE = "";
    private static final String AMAZONACCESSKEYPOLLY = "";
    private static final String AMAZONSECRETKEYPOLLY = "";
    private static final String AMAZONACCESSKEYTRANSCRIBE = "";
    private static final String AMAZONSECRETKEYTRANSCRIBE ="";

    public static String getMICROSOFTSUBKEYTRANSLATE() { return MICROSOFTSUBKEYTRANSLATE; }

    public static String getMICROSOFTSUBKEYSPEECH() { return MICROSOFTSUBKEYSPEECH; }

    public static String getMICROSOFTLOCATION() { return MICROSOFTLOCATION; }

    public static String getGOOGLESUBKEYAPI() { return GOOGLESUBKEYAPI; }

    public static String getAMAZONACCESSKEYTRANSLATE() { return AMAZONACCESSKEYTRANSLATE; }

    public static String getAMAZONSECRETKEYTRANSLATE() {
        return AMAZONSECRETKEYTRANSLATE;
    }

    public static String getAMAZONACCESSKEYPOLLY() {
        return AMAZONACCESSKEYPOLLY;
    }

    public static String getAMAZONSECRETKEYPOLLY() {
        return AMAZONSECRETKEYPOLLY;
    }

    public static String getAMAZONACCESSKEYTRANSCRIBE() {
        return AMAZONACCESSKEYTRANSCRIBE;
    }

    public static String getAMAZONSECRETKEYTRANSCRIBE() {
        return AMAZONSECRETKEYTRANSCRIBE;
    }
}
