let chosenLanguage;

//Service variables
let ttsStatus = true;
let sttStatus = true;

//Text-to-speech variables
let playingAudio;
let audio;

//Speech-to-text variables
let microphoneActive = false;
let mediaRecorder;
let dataArray;

//Boost variable, set to empty to indicate to boost.ai that it is the start of the conversation
let conversationId = "";

const BOT_MESSAGES_MESSAGE_WAIT_TIME = 1000;

//Sets start values on page load
$( async function() {
  try{
    chosenLanguage = await $.get("getLanguage").promise();
  }catch (err) {
    let errormsg = "There was a problem retrieving the language. Language has been set to English by default.";
    receiveMessage(textDivCreatorReceive(errormsg, errormsg));
    chosenLanguage = "English";
  }
  if (chosenLanguage === "English") {
    $("#onclick-underline").hide();
  }
  placeholderText();
  await getLanguageServiceStatus();
  popupText();
  popupHover();

  //Starting a dialog with Boost.ai.The empty conversation id and message string indicates to the server that this is the
  //beginning of the conversation and should return a welcome message
  await boostCommunication("");
});


//Checking whether the language has support for stt or tts
async function getLanguageServiceStatus() {
  let microphone =  $("#microphone");
  let microphoneImage = $("#microphone-image");

  try {
    let apiService = await $.get("getLanguageStatus").promise();
    ttsStatus = apiService.texttospeechservice;
    sttStatus = apiService.speechtotextservice;
  } catch (err) {

    //If get-method fails, tts and stt are by default set to disabled to avoid errors
    let apiServiceStatus = "Error retrieving api service status. Speech to text and text to speech" +
        " has been blocked";
    receiveMessage(textDivCreatorReceive(await translateTextFromEnglish(apiServiceStatus), apiServiceStatus));
    ttsStatus = "Disabled";
    sttStatus = "Disabled";
  }

    if (ttsStatus === "Disabled") {
      ttsStatus = false;
    }
    if (sttStatus === "Disabled") {
      sttStatus = false;
      microphoneImage.attr("src", "img/mic-disabled.png");
      microphoneImage.attr("alt", "Microphone disabled");
      microphone.css("cursor", "default");
      microphone.css("opacity", "0.4");
      microphone.attr("disabled", true);
      microphone.attr("value", "Microphone button disabled");
    }
}

function microphoneHover() {
  $("#microphone-image").attr("src", "img/mic-hover.png");
}

function microphoneDefault() {
  $("#microphone-image").attr("src", "img/mic-not-active.png");
}

//Handles the text input from the user.
async function inputFromUser() {
  let text = $("#message-text").val();
  let translatedText= await translateTextToEnglish(text);
  sendMessage(text, translatedText);
  boostCommunication(translatedText);
}

async function translateTextToEnglish(text) {
  const translateObject = {
    translateText: text,
    translateLanguageFrom: chosenLanguage
  };

  let data;
  try{
    data = await $.get("translatetoenglish", translateObject).promise();
  }catch (err) {
    data = "Translation failed, please try again";
  }
  return data;
}

async function translateTextFromEnglish(text) {
  const translateObject = {
    translateText: text,
    translateLanguageTo: chosenLanguage
  };

  let data;
  try{
    data = await $.get("translatefromenglish", translateObject).promise();
  }catch (err){
    data=translationFailed();
  }
  return data;
}

// Handles the boost communication from the server
async function boostCommunication(message) {
  let messageText = { message: message, conversationId: conversationId };
  let botChatMessages;
  let links;

  //Sending the users message and receiving the response from the server with a unique conversation id
  try {
    let data = await $.post("boostConversation", messageText).promise();
    botChatMessages = data.textResponse;
    links = data.linkResponse;
    conversationId = data.conversationId;
  } catch (err) {
    botChatMessages = ["There was a problem with the connection.", "Please try again."];
  }

  //Loops through all text responses and writes to the dialog box.
  for (const botChatMessage of botChatMessages) {
    let translatedText = await translateTextFromEnglish(botChatMessage);
    let textDiv = textDivCreatorReceive(translatedText, botChatMessage);
    receiveMessage(textDiv);

    //Waiting between each message to make the chatbot more "human"
    await sleep(BOT_MESSAGES_MESSAGE_WAIT_TIME);
  }

  //Loops through all link/action responses and writes to the dialog box.
  links.filter(async (linkObject) => {
    let buttonTextinEnglish = linkObject.text;
    let userLangBtnText = await translateTextFromEnglish(buttonTextinEnglish);

    let alternativeDiv = "  <div class='message-received'>" +
        "                     <div class='alternative-div-styling'>" +

        "                       <div class='original-language-text'>" +
        "                         <button class='alternative-button'>" +
        "                            <div class='radio-button-alternative'>" +
        "                                <div class='radio-buttonsmall-alternative'></div>"+
        "                            </div>  " +
        "                            <div class='alternative-text'>" +
                                     userLangBtnText +
        "                            </div> " +
        "                         </button>" +
        "                       </div>" +
        "                       <div class='english-language-text'>" +
                                  buttonTextinEnglish +
        "                      </div>" +
        "                     </div>" +
        "                   <button onclick='repeatAlternativeSpeech(this)' value='Click to listen to the text' class='speaker btn'> <i class='fas fa-volume-off'></i></button>" +
        "                 </div>";
    receiveMessage(alternativeDiv);
  });
}

//Creates a received element containing response in user language and in english
function textDivCreatorReceive(textUserLang, textEnglish) {
  return "  <div class='message-received'>" +
      "                <p class='avatar'><i class='fas fa-user-md'></i></p>" +
      "                <div class='text'>" +
      "                   <div class='original-language-text'>" +
      textUserLang +
      "                   </div>" +
      "                   <div class='english-language-text'>" +
      textEnglish +
      "                   </div>" +
      "                     </div>" +
      "                <button onclick='repeatMessageSpeech(this)' value='Click to listen to the text' class='speaker btn'> <i class='fas fa-volume-off'></i></button>" +
      "            </div>";
}

//Changes appearance on send message button based on validation requirements from user text input
function disableSendMessage() {
  let $button = $("#send-message-button");
  $button.attr("disabled", true);
  $button.css("color", "grey");
  $button.css("opacity", "0.4");
  $button.attr("value" , "Send message button disabled");
}

function enableSendMessage() {
  let $button = $("#send-message-button");
  $button.attr("disabled", false);
  $button.css("color", "#E78B00");
  $button.css("opacity", "1");
  $button.attr("value" , "Send message button enabled");
}

//Function that listens for change in the user input field, disables button, checks validity, updates wordcounter
$("#message-text").on("input change paste keydown", async function (event) {
  disableSendMessage();
  $("#word-counter").css("color", "black");
  let currentLength = $(this).val().trim().length;
  const maxlength = 100;
  let isEmpty;
  let $wordcounter = $("#word-counter");

  if (currentLength > maxlength) {
    $wordcounter.css("color", "red");
  }

  if ($(this).val().trim().length !== 0) {
    isEmpty = false;
  } else {
    currentLength = 0;
    isEmpty = true;
  }

  //Performs inputFromUser() action when enter is clicked, except when validation fails
  if (
    event.keyCode === 13 &&
    !event.shiftKey &&
    currentLength < maxlength &&
    !isEmpty
  ) {
    event.preventDefault();
    inputFromUser();
  }

  if (currentLength < maxlength && !isEmpty) {
    $wordcounter.css("color", "black");
    enableSendMessage();
  }
  $wordcounter.text(currentLength + "/" + maxlength);
});

//Textarea expands when new line is being added
$("textarea")
  .each(function () {
    this.setAttribute(
      "style",
      "height:" + this.scrollHeight + "px;overflow-y:hidden;"
    );
  })
  .on("input change", function () {
    this.style.height = "auto";
    this.style.height = this.scrollHeight + "px";
  });

//Shows popup hover text
function popupHover() {
  popupText();

  $(".speaker").hover(
    function (e) {
      $("div#pop-up-tts").show();
    },
    function () {
      $("div#pop-up-tts").hide();
    }
  );

  if (!sttStatus) {
    $(".button-wrap").hover(
        function (e) {
          $("div#pop-up-stt").show();
        },
        function () {
          $("div#pop-up-stt").hide();
        }
    );
  }

  $(".speaker").mousemove(function (e) {
    let offset = $(this).offset();
    let width = $(this).width();

    let centerSpeaker = offset.left + width / 2;
    let centerScreenX = $(window).width() / 2;

    //Adjusting placement of popup depending on which side of the screen the mouse is on
    if (centerSpeaker > centerScreenX) {
      $("div#pop-up-tts")
        .css("top", e.pageY + 25)
        .css("left", e.pageX - 120);
    } else {
      $("div#pop-up-tts")
        .css("top", e.pageY + 25)
        .css("left", e.pageX + 12);
    }
  });
}

//Text-to-speech function
function textToSpeech(text, speakerbutton) {
  let iVolumeImage = speakerbutton.querySelector("i");

  //Clicking the button while audio is playing will stop it
  if (playingAudio) {
    audio.src = "";
    playingAudio = false;
    iVolumeImage.classList.remove("fa-volume-up");
    iVolumeImage.classList.add("fa-volume-off");
  } else {
    const ttsObject = {
      ttstext: text,
      ttslanguage: chosenLanguage,
    };

    $.get("tts", ttsObject, function (data) {

      //Receives a base64 string which must be converted to a bytearray
      const byteCharacters = atob(data);
      const byteNumbers = new Array(byteCharacters.length);
      for (let i = 0; i < byteCharacters.length; i++) {
        byteNumbers[i] = byteCharacters.charCodeAt(i);
      }
      const byteArray = new Uint8Array(byteNumbers);

      //Uses the bytearray to create a blob that can be played as audio
      const blob = new Blob([byteArray], { type: "audio/mp3" });
      let audioSrc = window.URL.createObjectURL(blob);
      audio = new Audio();
      audio.src = audioSrc;
      audio.play().then(function () {
        playingAudio = true;
        iVolumeImage.classList.remove("fa-volume-off");
        iVolumeImage.classList.add("fa-volume-up");
      });
      audio.onended = function () {
        playingAudio = false;
        iVolumeImage.classList.remove("fa-volume-up");
        iVolumeImage.classList.add("fa-volume-off");
      };
    });
  }
}


let tStart;
let tStop;
let timeSTT;

//Speech-to-text function
function speechToText() {
  let audioIN = { audio: true };
  let activeMicrophone =   $("#microphone-image-active");
  let microphoneImage =  $("#microphone-image");

  //Gets permission to use the clients microphone
  navigator.mediaDevices
    .getUserMedia(audioIN)
    .then(function (mediaStreamObj) {
      let interval = setInterval(checkLength, 1000);

      function checkLength() {
        tStop = performance.now();
        timeSTT = tStop - tStart;
        if (timeSTT > 20000 && microphoneActive) {
          mediaRecorder.stop();
          microphoneActive = false;
          activeMicrophone.attr("src", "img/mic-not-active.png");
          activeMicrophone.attr("id", "microphone-image");
          clearInterval(interval);
        }
      }

      //Starts recording
      if (!microphoneActive) {
        tStart = performance.now();
        mediaRecorder = new MediaRecorder(mediaStreamObj);
        mediaRecorder.start();
        dataArray = [];
        microphoneActive = true;

        microphoneImage.attr("src", "img/mic-active.png");
        microphoneImage.attr("id", "microphone-image-active");
        microphoneImage.attr("alt", "Microphone listening for input");

      //Stops the recording
      } else {
        mediaRecorder.stop();
        microphoneActive = false;
        activeMicrophone.attr("src", "img/mic-not-active.png");
        activeMicrophone.attr("id", "microphone-image");
        microphoneImage.attr("alt", "Microphone image");
        $(".stt-loader").css("display", "block");
        clearInterval(interval);
      }

      mediaRecorder.ondataavailable = function (ev) {
        dataArray.push(ev.data);
      };

      mediaRecorder.onstop = function (ev) {
        let audioData = new Blob(dataArray, { type: "audio/mp3;" });
        dataArray = [];

        //Creates a form containing the audio file and the chosen language
        form = new FormData();
        form.append("file", audioData);
        form.append("language", chosenLanguage);

        //Post method that sends the file as a multipart/form-data
        $.ajax({
          url: "stt",
          type: "POST",
          data: form,
          enctype: "multipart/form-data",
          processData: false,
          contentType: false,
          cache: false,

          success: function (result) {
            if (result === "0") {
              speechToTextUnknownInput();
              $(".stt-loader").css("display", "none");
            } else {

              //Writes recognized text from audio file into textinput field
              $("#message-text").val(result).change();
              $(".stt-loader").css("display", "none");
            }
          },
          error: function (result) {
          },
        });
      };
    })

    //Prints out error message
    .catch(function (err) {
      console.log(err.name, err.message);
    });
}

let ttsSpamprevention = true;

//Function that sends the text to the tts method
async function repeatMessageSpeech(speakerbutton) {

  if (ttsStatus && ttsSpamprevention) {
    let text = speakerbutton.parentNode
        .querySelector(".text")
        .querySelector(".original-language-text").innerHTML;
    textToSpeech(text, speakerbutton);

    //To avoid spamclick
    ttsSpamprevention = false;
    await sleep(500);
    ttsSpamprevention = true;
  }
}

//Function that sends the actionlink-text to the tts method
async function repeatAlternativeSpeech(speakerbutton) {
  if (ttsStatus && ttsSpamprevention) {
    let text = speakerbutton.parentNode
        .querySelector(".alternative-div-styling")
        .querySelector(".original-language-text")
        .querySelector(".alternative-text").innerHTML;
    textToSpeech(text, speakerbutton);

    //To avoid spamclick
    ttsSpamprevention = false;
    await sleep(500);
    ttsSpamprevention = true;
  }
}

function sendMessage(messageTextUser, messageTextEnglish) {
  let messagebox = $("#message-box");
  const sentMessage =
    "       <li class='message-container'>" +
    "            <div class='message-sent'>" +
    "                <button onclick='repeatMessageSpeech(this)' value='Click to listen to the text' class='speaker btn'> <i class='fas fa-volume-off'></i></button>" +
    "                <div class='text'>" +
    "                   <div class='original-language-text'>" +
    messageTextUser +
    "                   </div>" +
    "                   <div class='english-language-text'>" +
    messageTextEnglish +
    "                   </div>" +
    "                 </div>" +
    "                <p class='avatar'><i class='fas fa-user'></i></p>" +
    "            </div>" +
    "       </li>";

  messagebox.append(sentMessage);

  //Emptying input field and resetting counter
  $("#message-text").val("").change();

  //Scrolls down when new message is added
  messagebox.scrollTop(messagebox[0].scrollHeight);
  popupHover();
}

//Handles messages that are being received from boost.ai
function receiveMessage(textDiv) {
  let messagebox = $("#message-box");
  const receivedMessage =
    "<li class='message-container'>" +
     textDiv +
    "</li>";

  messagebox.append(receivedMessage);
  messagebox.scrollTop(messagebox[0].scrollHeight);
  popupHover();
}

let englishTranslationActive = false;

//Adds english translation to each message
function addEnglishTranslation() {
  if (!englishTranslationActive) {
    $(".english-language-text").show();
    englishTranslationActive = true;
  } else {
    $(".english-language-text").hide();
    englishTranslationActive = false;
  }
}

// Helpers
async function sleep(waitTime) {
  await new Promise((resolve) => setTimeout(resolve, waitTime));
}

////// Switch-cases for hardcoded translations //////


//Setting placeholder text and direction based on language
function placeholderText() {
  let placeholdertext;
  let textInput =  $("#message-text");
  let orgText = $(".original-language-text");
  switch (chosenLanguage) {
    case "English":
      placeholdertext = "Type here";
      break;
    case "Arabic":
      placeholdertext = "أكتب هنا";
      textInput.attr("dir", "rtl");
      orgText.attr("dir", "rtl");
      break;
    case "French":
      placeholdertext = "Écrivez ici";
      break;
    case "German":
      placeholdertext = "Geben Sie hier";
      break;
    case "Greek":
      placeholdertext = "Πληκτρολόγησε εδώ";
      break;
    case "Norwegian":
      placeholdertext = "Skriv her";
      break;
    case "Farsi":
      placeholdertext = "اینجا را تایپ کنید";
      textInput.attr("dir", "rtl");
      break;
    case "Polish":
      placeholdertext = "Pisz tutaj";
      break;
    case "Somali":
      placeholdertext = "Ku qor halkan";
      break;
    case "Spanish":
      placeholdertext = "Escriba aquí";
      break;
    case "Urdu":
      placeholdertext = "یہاں ٹائپ کریں";
      textInput.attr("dir", "rtl");
      break;
    default:
      placeholdertext = "Type here";
  }
  textInput.attr("placeholder", placeholdertext);
}

//Overrides default error message when audio input is not recognized
function speechToTextUnknownInput() {
  let sttunknowntext;
  switch (chosenLanguage) {
    case "English":
      sttunknowntext = "Unknown input, please try again";
      break;
    case "Arabic":
      sttunknowntext = "إدخال غير معروف ، يرجى المحاولة مرة أخرى";
      break;
    case "French":
      sttunknowntext = "Entrée inconnue, veuillez réessayer";
      break;
    case "German":
      sttunknowntext = "Unbekannte Eingabe, bitte versuchen Sie es erneut";
      break;
    case "Greek":
      sttunknowntext = "Άγνωστη εισαγωγή, δοκιμάστε ξανά";
      break;
    case "Norwegian":
      sttunknowntext = "Ukjent inndata, vennligst prøv igjen";
      break;
    case "Farsi":
      sttunknowntext = "ورودی ناشناخته ، لطفاً دوباره امتحان کنید";
      break;
    case "Polish":
      sttunknowntext = "Nieznane wejście, spróbuj ponownie";
      break;
    case "Somali":
      sttunknowntext = "Gelin aan la aqoon, fadlan isku day mar kale";
      break;
    case "Spanish":
      sttunknowntext = "Entrada desconocida, inténtelo de nuevo";
      break;
    case "Urdu":
      sttunknowntext = "نامعلوم ان پٹ ، براہ کرم دوبارہ کوشش کریں";
      break;
    default:
      sttunknowntext = "Unknown input, please try again";
  }
  $("#message-text").val(sttunknowntext).change();
}

//Function that sets popup-text for text-to-speech and speech-to-text
function popupText() {
  let disabledPopupText;
  let ttsActivePopupText;
  switch (chosenLanguage) {
    case "Arabic":
      ttsActivePopupText = "انقر لسماع النص";
      break;
    case "English":
      ttsActivePopupText = "Click to hear text";
      break;
    case "French":
      ttsActivePopupText = "Cliquez pour entendre le texte";
      break;
    case "German":
      ttsActivePopupText = "Klicken Sie hier, um Text zu hören";
      break;
    case "Greek":
      ttsActivePopupText = "Κάντε κλικ για να ακούσετε κείμενο";
      break;
    case "Norwegian":
      ttsActivePopupText = "Klikk for å høre tekst";
      break;
    case "Farsi":
      disabledPopupText = "پشتیبانی نشده";
      break;
    case "Polish":
      ttsActivePopupText = "Kliknij, aby usłyszeć tekst";
      break;
    case "Somali":
      disabledPopupText = "Lama taageerin";
      break;
    case "Spanish":
      ttsActivePopupText = "Haga clic para escuchar el texto";
      break;
    case "Urdu":
      disabledPopupText = "سہولت مہیا نہیں";
      break;
    default:
      disabledPopupText = "Not supported";
      ttsActivePopupText = "Click to hear text";
  }

  //Disables speaker button if no supported tts service exists
  if (!ttsStatus) {
    let speaker =  $(".speaker");
    $("#pop-up-tts").text(disabledPopupText);
    speaker.css("color", "grey");
    speaker.css("opacity", "0.6");
    speaker.css("cursor", "default");
  } else {
    $("#pop-up-tts").text(ttsActivePopupText);
  }

  if (!sttStatus) {
    $("#pop-up-stt").text(disabledPopupText);
  }
}

function translationFailed() {
  let translationFailedText;
  switch (chosenLanguage) {
    case "English":
      translationFailedText = "Translation failed, please try again.";
      break;
    case "Arabic":
      translationFailedText = "فشلت الترجمة ، يرجى المحاولة مرة أخرى";
      textInput.attr("dir", "rtl");
      orgText.attr("dir", "rtl");
      break;
    case "French":
      translationFailedText = "La traduction a échoué, veuillez réessayer";
      break;
    case "German":
      translationFailedText = "Übersetzung fehlgeschlagen, bitte versuchen Sie es erneut";
      break;
    case "Greek":
      translationFailedText = "Η μετάφραση απέτυχε, δοκιμάστε ξανά.";
      break;
    case "Norwegian":
      translationFailedText = "Oversettelsen mislyktes, vennligst prøv igjen";
      break;
    case "Farsi":
      translationFailedText = "ترجمه انجام نشد ، لطفاً دوباره امتحان کنید";
      textInput.attr("dir", "rtl");
      break;
    case "Polish":
      translationFailedText = "Tłumaczenie nie powiodło się, spróbuj ponownie";
      break;
    case "Somali":
      translationFailedText = "Tarjumaadu way dhacday, fadlan iskuday markale";
      break;
    case "Spanish":
      translationFailedText = "La traducción falló, inténtelo de nuevo.";
      break;
    case "Urdu":
      translationFailedText = "ترجمہ ناکام ، براہ کرم دوبارہ کوشش کریں";
      textInput.attr("dir", "rtl");
      break;
    default:
      translationFailedText = "Translation failed, please try again.";
  }
  return translationFailedText;
}