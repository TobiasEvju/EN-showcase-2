package com.example.demo.apis;


import com.example.demo.models.Credentials;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;


import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.transcribe.TranscribeAsyncClient;
import software.amazon.awssdk.services.transcribe.model.*;

import java.nio.ByteBuffer;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import java.io.IOException;

public class AmazonTranscribeBatch {
    private static final Region REGION = Region.EU_WEST_1;
    private static final String BUCKETNAME = "transcribebucketbachelorproject";
    private static final AwsBasicCredentials awsCreds = AwsBasicCredentials.create(Credentials.getAMAZONACCESSKEYTRANSCRIBE(), Credentials.getAMAZONSECRETKEYTRANSCRIBE());


    public static String sttAmazon(String langCode, String path, int random) throws Exception {
        String JOBNAME = "BatchTranslate" + random;
        String fileName = "sttFile" + random;

        //The client that controls actions performed on the s3 bucket on AWS
        S3Client s3 = S3Client.builder()
                .region(REGION)
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .build();

        //The client that handles all transcription requests
        TranscribeAsyncClient client = TranscribeAsyncClient.builder()
                .region(REGION)
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .build();

        // Necessary to create AmazonTranscribeBatch-object because synchronize method in getTranscription
        // do not work in a static context
        AmazonTranscribeBatch tb = new AmazonTranscribeBatch();

        String transcribedText = tb.getTranscription(langCode, JOBNAME, path, fileName, s3, client);


        // Deleting the transcription job after transcription is performed
        DeleteTranscriptionJobRequest deleteJob = DeleteTranscriptionJobRequest.builder()
                .transcriptionJobName(JOBNAME)
                .build();
        client.deleteTranscriptionJob(deleteJob);


        // Deleting the audio object from the bucket after transcription is performed
        s3.deleteObject(DeleteObjectRequest.builder()
                .key(fileName)
                .bucket(BUCKETNAME)
                .build());

        return transcribedText;
    }

    public String getTranscription(String langCode, String JOBNAME, String path, String fileName, S3Client s3, TranscribeAsyncClient client) throws ExecutionException, InterruptedException, IOException {

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(BUCKETNAME)
                .key(fileName)
                .build();

        //Uploading audio file to bucket
        s3.putObject(objectRequest, RequestBody.fromByteBuffer(ByteBuffer.wrap(Files.readAllBytes(Paths.get(path)))));

        String url = "s3://transcribebucketbachelorproject/"+ fileName;

        Media media = Media.builder()
                .mediaFileUri(url)
                .build();

        StartTranscriptionJobRequest startJobRequest = StartTranscriptionJobRequest.builder()
                .languageCode(langCode)
                .transcriptionJobName(JOBNAME)
                .media(media)
                .mediaFormat("wav")
                .build();

        //Starting the transcription job
        client.startTranscriptionJob(startJobRequest).get();

        GetTranscriptionJobRequest getJobRequest = GetTranscriptionJobRequest.builder()
                .transcriptionJobName(JOBNAME)
                .build();

        TranscriptionJob result;

        String transcription;

        // Will try to receive the transcripted text from AWS every second until success or failure
        while( true ){
            result = client.getTranscriptionJob(getJobRequest).get().transcriptionJob();

            if( result.transcriptionJobStatus().toString().equals(TranscriptionJobStatus.COMPLETED.name()) ){

                String uriString = result.transcript().transcriptFileUri();

                HttpGet request = new HttpGet(uriString);
                CloseableHttpClient cHttpClient = HttpClients.createDefault();
                CloseableHttpResponse response = cHttpClient.execute(request);
                HttpEntity entity = response.getEntity();
                String jsonResult = EntityUtils.toString(entity);

                JsonObject jsonObject = JsonParser.parseString(jsonResult).getAsJsonObject();

                JsonObject results = (JsonObject) jsonObject.get("results").getAsJsonObject().get("transcripts").getAsJsonArray().get(0);
                transcription = results.get("transcript").getAsString();
                break;

            }else if( result.transcriptionJobStatus().toString().equals(TranscriptionJobStatus.FAILED.name()) ){
                transcription = "Transcription failed";
                break;
            }
            // Trying every 1 second
            synchronized ( this ) {
                try {
                    wait(1000);
                } catch (InterruptedException e) { }
            }
        }
        return transcription;
    }
}
