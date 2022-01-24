window.onclose = windowClose;

function windowClose() {
    //親ウィンドウをリフレッシュ
    if (window.opener) {
        window.opener.document.forms[0].btn_submit("edit");
    }
    return false;
}

function btn_submit(cmd) {
    //    document.forms[0].cmd.value = cmd;
    //    document.forms[0].submit();

    //親ウィンドウをリフレッシュ
    if (cmd == "update") {
        if (window.opener) {
            window.opener.document.forms[0].btn_submit("edit");
        }
    }
    return false;
}

function ShowConfirm() {
    if (!confirm("{rval MSG106}")) return false;
}

function OnAuthError() {
    alert("{rval MSG300}");
    closeWin();
}
