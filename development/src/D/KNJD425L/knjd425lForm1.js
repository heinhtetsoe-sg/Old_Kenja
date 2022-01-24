var textRange;
function btn_submit(cmd) {
    if (document.forms[0].SCHREGNO.value == "") {
        alert("{rval MSG304}");
        return true;
    } else if (cmd == "clear") {
        if (!confirm("{rval MSG106}")) {
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function chksetdate() {
    if (document.forms[0].USEKNJD425LDISPUPDDATE.value == "1") {
        if (document.forms[0].UPDDATE.value == "" || (document.forms[0].UPDDATE.value == "9999/99/99" && document.forms[0].RECORD_DATE.value == "")) {
            alert("{rval MSG304}");
            return false;
        }
    }
    return true;
}

function clickLink(param) {
    if (document.forms[0].USEKNJD425LDISPUPDDATE.value == "1") {
        if (document.forms[0].UPDDATE.value == "9999/99/99") {
            param += "&UPDDATE=" + document.forms[0].RECORD_DATE.value;
        }
    }
    document.location.href = param;
    return;
}

setTimeout(function () {
    window.onload = new (function () {
        if (sessionStorage.getItem("KNJD425LForm1_CurrentCursor") != null) {
            document.title = "";
            document.getElementById(sessionStorage.getItem("KNJD425LForm1_CurrentCursor")).focus();
        } else if (sessionStorage.getItem("link_click") == "right_screen") {
            document.getElementById("rightscreen").focus();
            sessionStorage.removeItem("link_click");
        } else {
            document.title = "右情報画面";
        }
    })();
}, 800);

function current_cursor(para) {
    sessionStorage.setItem("KNJD425LForm1_CurrentCursor", para);
}
