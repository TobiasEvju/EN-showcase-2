package com.example.demo.apis;

import com.example.demo.models.*;
import com.google.gson.*;
import com.squareup.okhttp.*;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class BoostApi {

    public BoostApi() throws FileNotFoundException {
    }

    @PostMapping()
    public static BoostResponse boostConversation(BoostSendDTO boostSendDTO) throws IOException {
        JsonObject jsonBodyIn = new JsonObject();

        // Necessary boost.ai conversation properties are added to the JSON object.
        //Conversation ID is empty if it is the start of the conversation
        if (boostSendDTO.getConversationId().equals("")) {
            jsonBodyIn.addProperty("command", "START");
            jsonBodyIn.addProperty("language", "en-US");
            jsonBodyIn.addProperty("clean", true);
        } else {
            jsonBodyIn.addProperty("command", "POST");
            jsonBodyIn.addProperty("conversation_id", boostSendDTO.getConversationId());
            jsonBodyIn.addProperty("clean", true);
            jsonBodyIn.addProperty("type", "text");
            jsonBodyIn.addProperty("value", boostSendDTO.getMessage());
        }

        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host("partner18.boost.ai")
                .addPathSegment("api")
                .addPathSegment("chat")
                .addPathSegment("v2")
                .build();

        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/json");

        RequestBody body = RequestBody.create(mediaType,
                String.valueOf(jsonBodyIn));

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Content-type", "application/json")
                .build();

        // Making a call to boost.ai with the requests
        Response response = client.newCall(request).execute();

        // Storing result as a string
        String jsonStringOut = response.body().string();

        // Making JSON object based on string
        JsonObject jsonBodyOut = JsonParser.parseString(jsonStringOut).getAsJsonObject();

        // Creating object where the different types of responses will be stored
        BoostResponse boostResponse = new BoostResponse();

        // Creating new JSON objects and strings based on the different elements/substrings inside the JSON
        JsonObject convJson = jsonBodyOut.getAsJsonObject("conversation");

        boostResponse.setConversationId(convJson.get("id").getAsString());
        boostResponse.setReference(convJson.get("reference").getAsString());

        JsonObject respJson = jsonBodyOut.getAsJsonObject("response");
        boostResponse.setAvatarUrl(respJson.get("avatar_url").getAsString());
        boostResponse.setDateCreated(respJson.get("date_created").getAsString());
        boostResponse.setId(respJson.get("id").getAsString());
        boostResponse.setId(respJson.get("language").getAsString());
        boostResponse.setId(respJson.get("source").getAsString());

        JsonArray elementsJson = respJson.getAsJsonArray("elements");

        ArrayList<String> textArrayList = new ArrayList<>();
        ArrayList<LinkResponse> linkArrayList = new ArrayList<>();

        // A boost.ai response can contain several types of elements. Loops through all and stores them in arrays based
        //on type. Only handling text- and link-elements in this application
        for (JsonElement payloadElement : elementsJson) {
            JsonObject elementJson = payloadElement.getAsJsonObject();
            if (elementJson.get("type").getAsString().equals("text")) {
                textArrayList.add(elementJson.get("payload").getAsJsonObject().get("text").getAsString());
            }
            if (elementJson.get("type").getAsString().equals("links")) {

                JsonArray linkArray = elementJson.get("payload").getAsJsonObject().getAsJsonArray("links");
                for (JsonElement jsonLinkElement : linkArray) {
                    LinkResponse linkResponse  = new Gson().fromJson(jsonLinkElement.getAsJsonObject(), LinkResponse.class);
                    linkArrayList.add(linkResponse);
                }
            }
        }

        boostResponse.setTextResponse(textArrayList);
        boostResponse.setLinkResponse(linkArrayList);

        return boostResponse;
    }
}
