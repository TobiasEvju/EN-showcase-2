//Setting global variables for chosen language
let languageChosen="English";

$(async function(){
    await setRadioValuesFromConfig(languageChosen);
});

//Dropdown menu
$(".dropdown-menu a").click(async function () {
    $("#span-value").html($(this).text());
    languageChosen = $(this).attr('data-value');
    await setRadioValuesFromConfig(languageChosen);

    if (languageChosen === "Tigrinya") {
        $('#onclick-underline').hide();
    } else {
        $('#onclick-underline').show();
    }
});

//Writing changes to languages.JSON file when a radio button is being changed
async function setNewRadioValuesInConfig() {

    //Finds the active radiobuttons
    let translatorChosen =  $("input[name='Translator']:checked").val();
    let texttospeechChosen = $("input[name='TextToSpeech']:checked").val();
    let speechtotextChosen = $("input[name='SpeechToText']:checked").val();

    //Checks if a service is disabled
    if (typeof translatorChosen == 'undefined') {translatorChosen = "Disabled"}
    if (typeof texttospeechChosen == 'undefined') {texttospeechChosen = "Disabled"}
    if (typeof speechtotextChosen == 'undefined') {speechtotextChosen = "Disabled"}

    const languageConfig={
        language:languageChosen,
        translatorservice:translatorChosen,
        texttospeechservice:texttospeechChosen,
        speechtotextservice:speechtotextChosen
    };

    //Sends an Ajax call to update the languages.JSON file
    try{
        await $.get("setConfig", languageConfig).promise();
    }
    catch (err){
        await setRadioValuesFromConfig(languageChosen);
        alert("Could not update services. Please try again");
    }
};

//Sets checked radio button values from languages.JSON on the chosen language
async function setRadioValuesFromConfig(language) {

    const languageConfigObject = {
        language: language
    };
    let data;

    //Sends an Ajax call to get the appropriate settings for the chosen language
    try{
        data = await $.get("getConfig", languageConfigObject).promise();
    }catch (err){
        alert("Failed to load services. All options have been disabled by default. Please try again.")
        $('input[name="Translator"]:radio').prop({'checked':false, 'disabled':true});
        $('input[name="TextToSpeech"]:radio').prop({'checked':false, 'disabled':true});
        $('input[name="SpeechToText"]:radio').prop({'checked':false, 'disabled':true});
        return;
    }

    //Ticks off and deactivates disabled buttons
    $('input[name="Translator"]:radio').prop({'checked':false, 'disabled':false});
    $('input[name="TextToSpeech"]:radio').prop({'checked':false, 'disabled':false});
    $('input[name="SpeechToText"]:radio').prop({'checked':false, 'disabled':false});

        //Services
        let translatorService = data.translatorservice;
        let texttospeechService = data.texttospeechservice;
        let speechtotextService = data.speechtotextservice;

        //Language codes
        let google_Translator = data.googletranslatorcode;
        let google_TextToSpeech = data.googletexttospeechcode;
        let google_SpeechToText = data.googlespeechtotextcode;
        let microsoft_Translator = data.microsofttranslatorcode;
        let microsoft_TextToSpeech = data.microsofttexttospeechcode;
        let microsoft_SpeechToText = data.microsoftspeechtotextcode;
        let amazon_Translator = data.amazontranslatorcode;
        let amazon_TextToSpeech = data.amazontexttospeechcode;
        let amazon_SpeechToText = data.amazonspeechtotextcode;

        //Creates an object which stores keys and values
        let serviceCodes = {
            google_Translator: google_Translator, google_TextToSpeech: google_TextToSpeech, google_SpeechToText: google_SpeechToText,
            microsoft_Translator: microsoft_Translator,  microsoft_TextToSpeech: microsoft_TextToSpeech, microsoft_SpeechToText: microsoft_SpeechToText,
            amazon_Translator: amazon_Translator, amazon_TextToSpeech: amazon_TextToSpeech, amazon_SpeechToText: amazon_SpeechToText}

        //Iteration through each keypair
        for (let key in serviceCodes) {
            let languageCodes = serviceCodes[key];
            let services = key.split("_");
            let provider = services[0].charAt(0).toUpperCase() + services[0].slice(1);
            let service = services[1];

        if (languageCodes === "Disabled") {
            $('input[name="'+service+'"]:radio[value="'+provider+'"]').prop({'disabled':true, 'checked':false});
        } else {
            $('input[name="'+service+'"]:radio[value="'+provider+'"]').prop({'disabled':false});
        }
    }
    //Ticks off the active settings
    $('input[name="Translator"]:radio[value="'+translatorService+'"]').prop({'checked':true, 'disabled':false});
    $('input[name="TextToSpeech"]:radio[value="'+texttospeechService+'"]').prop({'checked':true, 'disabled':false});
    $('input[name="SpeechToText"]:radio[value="'+speechtotextService+'"]').prop({'checked':true, 'disabled':false});
};

function openChat() {
    window.location.href=url('/chat?lang='+languageChosen);
}

const baseUrl = 'https://speechdialog.netnordic.tech/HelseChat'
//const baseUrl = 'http://localhost:8080/'

function url(url) {
    return baseUrl + url;
}