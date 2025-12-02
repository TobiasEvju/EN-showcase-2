package com.example.demo.apis;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.translate.AmazonTranslate;
import com.amazonaws.services.translate.AmazonTranslateClient;
import com.amazonaws.services.translate.model.TranslateTextRequest;
import com.amazonaws.services.translate.model.TranslateTextResult;
import com.example.demo.models.Credentials;
import com.example.demo.models.TranslateObject;

public class AmazonTranslater {

    private static final String REGION = "eu-north-1";

    public static String translateAmazon(TranslateObject translateObject) {

        String text = translateObject.getTranslateText();
        String translateFrom = translateObject.getTranslateFromCode();
        String translateTo = translateObject.getTranslateToCode();

        // Use this if credentials are stored on computer.
        //AWSCredentialsProvider awsCreds = DefaultAWSCredentialsProviderChain.getInstance();

        //Use this to add credentials directly
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(Credentials.getAMAZONACCESSKEYTRANSLATE(), Credentials.getAMAZONSECRETKEYTRANSLATE());

        AmazonTranslate translate = AmazonTranslateClient.builder()
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .withRegion(REGION)
                .build();

        TranslateTextRequest request = new TranslateTextRequest()
                .withText(text)
                .withSourceLanguageCode(translateFrom)
                .withTargetLanguageCode(translateTo);
        TranslateTextResult result  = translate.translateText(request);

        return result.getTranslatedText();
    }
}
