window.onload = function () {
    if (sessionStorage.getItem("KNJA128PForm2_CurrentCursor915") != null) {
        document.getElementsByName(sessionStorage.getItem("KNJA128PForm2_CurrentCursor915"))[0].focus();
        sessionStorage.removeItem("KNJA128PForm2_CurrentCursor915");
    } else {
        sessionStorage.removeItem("KNJA128PForm2_CurrentCursor915");
        if (sessionStorage.getItem("KNJA128PForm2_CurrentCursor") != null) {
            document.title = "";
            setTimeout(function () {
                document.getElementById(sessionStorage.getItem("KNJA128PForm2_CurrentCursor")).focus();
                sessionStorage.removeItem("KNJA128PForm2_CurrentCursor");
            }, 500);
        } else {
            setTimeout(function () {
                document.getElementById("screen_id").focus();
            }, 500);
        }
    }
};
function current_cursor(para) {
    sessionStorage.setItem("KNJA128PForm2_CurrentCursor", para);
}
//サブミット
function btn_submit(cmd) {
    document.getElementById(sessionStorage.getItem("KNJA128PForm2_CurrentCursor")).blur();
    document.title = "";
    if (cmd == "clear2") {
        if (!confirm("{rval MSG106}")) {
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//Submitしない
function btn_keypress() {
    if (event.keyCode == 13) {
        event.keyCode = 0;
        window.returnValue = false;
    }
}
