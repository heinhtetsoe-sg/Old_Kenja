function btn_submit(cmd) {

    if (document.forms[0].SCHREGNO.value == ""){
        alert('{rval MSG304}');
        return true;
    } else if (cmd == 'clear') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    } else if (cmd == 'delete') {
        if (!confirm('{rval MSG103}')) {
            return false;
        }
    } else if (cmd == 'allcopy') {
        if (document.forms[0].SEMESTERCOPY.value == "1") {
            if (!confirm('{rval MSG104}')) {
                return false;
            }
        }
    } else if (cmd == 'copy') {
        if (document.forms[0].SUBCLASSCOPY.value == "1") {
            if (!confirm('{rval MSG104}')) {
                return false;
            }
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL) {
    var i;
    var radio2;
    //必須チェック
    if (document.forms[0].SCHREGNO.value == "") {
        alert('{rval MSG304}');
        return true;
    }
    radio2 = parent.left_frame.document.getElementById("HUKUSIKI_RADIO2");
    if (radio2 && radio2.checked) {
        document.forms[0].SELECT_GHR.value = "1";
    } else {
        document.forms[0].SELECT_GHR.value = "";
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJD";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
//Submitしない
function btn_keypress() {
    if (event.keyCode == 13) {
        event.keyCode = 0;
        window.returnValue  = false;
    }
}
