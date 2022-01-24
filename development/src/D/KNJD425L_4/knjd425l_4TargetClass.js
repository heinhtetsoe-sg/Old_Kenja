function btn_submit(cmd) {

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

setTimeout(function () {
    window.onload = new function () {
        if (sessionStorage.getItem("KNJD420LForm1_CurrentCursor") != null) {
            document.title = "";
            document.getElementById(sessionStorage.getItem("KNJD420LForm1_CurrentCursor")).focus();
        } else if (sessionStorage.getItem("link_click") == "right_screen") {
            document.getElementById("rightscreen").focus();
            sessionStorage.removeItem('link_click');
        } else {
            document.title = "右情報画面";
        }
    }
}, 800);

function current_cursor(para) {
    sessionStorage.setItem("KNJD420LForm1_CurrentCursor", para);
}
