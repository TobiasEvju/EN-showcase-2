let languageChosen="NotSelected";

disableStartChatButton();

function changeStartChatText() {
    let startChatText;

    switch(languageChosen) {
        case "English":
            startChatText = "Start dialog";
            break;
        case "Arabic":
            startChatText = "بدء الحوار";
            break;
        case "French":
            startChatText = "Commencer le dialogue";
            break;
        case "German":
            startChatText = "Dialog starten";
            break;
        case "Greek":
            startChatText = "Έναρξη διαλόγου";
            break;
        case "Norwegian":
            startChatText = "Start dialog";
            break;
        case "Farsi":
            startChatText = "گفتگوی شروع";
            break;
        case "Polish":
            startChatText = "Dialogowe Uruchom";
            break;
        case "Somali":
            startChatText = "Bilow wadahadal";
            break;
        case "Spanish":
            startChatText = "Diálogo de inicio";
            break;
        case "Urdu":
            startChatText = "مکالمہ شروع کریں";
            break;
        default:
            startChatText = "Start dialog";
    }
    $("#start-chat-span").html(startChatText);
    enableStartChatButton();
}

$(".dropdown-menu a").click(function () {
    $("#span-value").html($(this).text());
    languageChosen = $(this).attr('data-value');
    changeStartChatText();

});

function disableStartChatButton() {
    $("#start-chat-button").attr('disabled', true);
    $("#start-chat-button").css('cursor', 'default');
    $("#start-chat-button").css('opacity', '0.5');
}

function enableStartChatButton() {
    $("#start-chat-button").attr('disabled', false);
    $("#start-chat-button").css('cursor', 'pointer');
    $("#start-chat-button").css('opacity', '1');
}

function openChat() {
    if (languageChosen === "NotSelected") {
        disableStartChatButton();
    } else {
        window.location.href = url('/chat?lang=' + languageChosen);
    }
}

//Changing baseURL based on working environment

function url(url) {
    const baseUrl = 'https://speechdialog.netnordic.tech/HelseChat'
    //const baseUrl = 'http://localhost:8080'

    return baseUrl + url;
}