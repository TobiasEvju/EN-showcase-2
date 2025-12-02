package com.example.demo.apis;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.polly.AmazonPollyClient;
import com.amazonaws.services.polly.model.*;
import com.example.demo.models.Credentials;
import com.example.demo.models.LanguageConfig;
import org.apache.tomcat.util.codec.binary.Base64;

import java.io.InputStream;

public class AmazonPolly {

    public static String ttsAmazon(LanguageConfig langObject) throws Exception {
        String ttsText = langObject.ttstext;
        String ttsCode = langObject.ttscode;
        AmazonPollyClient polly;
        Voice voice;
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(Credentials.getAMAZONACCESSKEYPOLLY(), Credentials.getAMAZONSECRETKEYPOLLY());
        polly = new AmazonPollyClient(awsCreds,
                new ClientConfiguration());
        polly.setRegion(Region.getRegion(Regions.EU_NORTH_1));

        // Finding and using the first available Amazon Polly voice
        DescribeVoicesRequest describeVoicesRequest = new DescribeVoicesRequest();
        DescribeVoicesResult describeVoicesResult = polly.describeVoices(describeVoicesRequest);
        voice = describeVoicesResult.getVoices().stream().filter(p -> p.getLanguageCode().equals(ttsCode)).findFirst().get();

        SynthesizeSpeechRequest synthReq =
                new SynthesizeSpeechRequest().withText(ttsText).withVoiceId(voice.getId())
                        .withOutputFormat(OutputFormat.Mp3);
        SynthesizeSpeechResult synthRes = polly.synthesizeSpeech(synthReq);

        InputStream speechStream = synthRes.getAudioStream();

        Base64 b = new Base64();
        return b.encodeToString(speechStream.readAllBytes());
    }
}
