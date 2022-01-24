function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function newwin(SERVLET_URL) {
    var bunri = document.getElementById("BUNRI").checked;
    var tireki = document.getElementById("TIREKI").checked;
    var rika = document.getElementById("RIKA").checked;

    if (bunri == false && tireki == false && rika == false) {
        alert("出力対象帳票を選択して下さい。");
        return false;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

    document.forms[0].action = SERVLET_URL + "/KNJA";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
