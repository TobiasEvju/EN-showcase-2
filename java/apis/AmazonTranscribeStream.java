package com.example.demo.apis;

import com.example.demo.controllers.ChatController;
import com.example.demo.models.Credentials;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.transcribestreaming.TranscribeStreamingAsyncClient;
import software.amazon.awssdk.services.transcribestreaming.model.*;
import javax.sound.sampled.*;
import java.io.*;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import static javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED;
import static javax.sound.sampled.AudioFormat.Encoding.PCM_UNSIGNED;

public class AmazonTranscribeStream {
    private static final Region REGION = Region.EU_WEST_1;
    private static TranscribeStreamingAsyncClient client;
    private static String transcribedText;
    private static String LangCode;

    public static String sttAmazon(String langCode, String path, int random) throws Exception {
        String fileName = "sttFile" + random;

        LangCode = langCode;

        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(Credentials.getAMAZONACCESSKEYTRANSCRIBE(), Credentials.getAMAZONSECRETKEYTRANSCRIBE());

        File theFile = new File(path);

        client = TranscribeStreamingAsyncClient.builder()
                .region(REGION)
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .build();


        //Using completableFuture to perform async task
        CompletableFuture<Void> result = client.startStreamTranscription(getRequest(theFile),
                new AudioStreamPublisher(getStreamFromFile(path)),
                getResponseHandler());

        result.get();
        client.close();

        return transcribedText;
    }

    //Using audio file from user to create audiostream
    private static InputStream getStreamFromFile(String file) {
        try {
            File inputFile = new File(file);
            InputStream audioStream = new FileInputStream(inputFile);
            return audioStream;

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    private static StartStreamTranscriptionRequest getRequest(File inputFile) throws IOException, UnsupportedAudioFileException {
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(inputFile);
        AudioFormat audioFormat = audioInputStream.getFormat();

        return StartStreamTranscriptionRequest.builder()
                .languageCode(LangCode)
                .mediaEncoding(getAwsMediaEncoding(audioFormat))
                .mediaSampleRateHertz(getAwsSampleRate(audioFormat))
                .build();
    }

    // Handling the response from AWS. AWS returns a stream, but that is not necessary for this application
    // Only the final, fully transcribed result, stored in "transcribedText" is therefore returned to the
    // controller and client
    private static StartStreamTranscriptionResponseHandler getResponseHandler() {

        return StartStreamTranscriptionResponseHandler.builder()
                .onResponse(r -> {
                    System.out.println("Received Initial response");
                })
                .onError(e -> {
                    System.out.println(e.getMessage());
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));
                    System.out.println("Error Occurred: " + sw.toString());
                })
                .onComplete(() -> {
                    System.out.println("=== All records stream successfully ===");
                })
                .subscriber(event -> {
                    List<Result> results = ((TranscriptEvent) event).transcript().results();
                    if (results.size() > 0) {
                        if (!results.get(0).alternatives().get(0).transcript().isEmpty()) {
                            System.out.println(results.get(0).alternatives().get(0).transcript());
                          transcribedText = results.get(0).alternatives().get(0).transcript();
                        }
                    }
                })
                .build();
    }

    private static class AudioStreamPublisher implements Publisher<AudioStream> {
        private final InputStream inputStream;
        private static Subscription currentSubscription;

        private AudioStreamPublisher(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public void subscribe(Subscriber<? super AudioStream> s) {

            if (this.currentSubscription == null) {
                this.currentSubscription = new SubscriptionImpl(s, inputStream);
            } else {
                this.currentSubscription.cancel();
                this.currentSubscription = new SubscriptionImpl(s, inputStream);
            }
            s.onSubscribe(currentSubscription);
        }
    }

    public static class SubscriptionImpl implements Subscription {
        private static final int CHUNK_SIZE_IN_BYTES = 1024 * 1;
        private final Subscriber<? super AudioStream> subscriber;
        private final InputStream inputStream;
        private ExecutorService executor = Executors.newFixedThreadPool(1);
        private AtomicLong demand = new AtomicLong(0);

        SubscriptionImpl(Subscriber<? super AudioStream> s, InputStream inputStream) {
            this.subscriber = s;
            this.inputStream = inputStream;
        }

        @Override
        public void request(long n) {
            if (n <= 0) {
                subscriber.onError(new IllegalArgumentException("Demand must be positive"));
            }

            demand.getAndAdd(n);

            executor.submit(() -> {
                try {
                    do {
                        ByteBuffer audioBuffer = getNextEvent();
                        if (audioBuffer.remaining() > 0) {
                            AudioEvent audioEvent = audioEventFromBuffer(audioBuffer);
                            subscriber.onNext(audioEvent);
                        } else {
                            subscriber.onComplete();
                            break;
                        }
                    } while (demand.decrementAndGet() > 0);
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            });
        }

        @Override
        public void cancel() {
            executor.shutdown();
        }

        private ByteBuffer getNextEvent() {
            ByteBuffer audioBuffer = null;
            byte[] audioBytes = new byte[CHUNK_SIZE_IN_BYTES];

            int len = 0;
            try {
                len = inputStream.read(audioBytes);

                if (len <= 0) {
                    audioBuffer = ByteBuffer.allocate(0);
                } else {
                    audioBuffer = ByteBuffer.wrap(audioBytes, 0, len);
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }

            return audioBuffer;
        }

        private AudioEvent audioEventFromBuffer(ByteBuffer bb) {
            return AudioEvent.builder()
                    .audioChunk(SdkBytes.fromByteBuffer(bb))
                    .build();
        }
    }

    private static MediaEncoding getAwsMediaEncoding(AudioFormat audioFormat) {
        final String javaMediaEncoding = audioFormat.getEncoding().toString();

        if (PCM_SIGNED.toString().equals(javaMediaEncoding)) {
            return MediaEncoding.PCM;
        } else if (PCM_UNSIGNED.toString().equals(javaMediaEncoding)){
            return MediaEncoding.PCM;
        }
        throw new IllegalArgumentException("Not a recognized media encoding:" + javaMediaEncoding);
    }

    private static Integer getAwsSampleRate(AudioFormat audioFormat) {
        return Math.round(audioFormat.getSampleRate());
    }

}
