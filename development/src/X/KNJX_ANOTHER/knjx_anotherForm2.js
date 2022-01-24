function btn_submit(cmd, zip, gzip, zadd, gadd) {
    if (cmd == "delete") {
        if (!confirm("{rval MSG103}")) return false;
    }
    if (cmd == "reset" && !confirm("{rval MSG106}")) {
        return true;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function closeMethod() {
    top.opener.parent.edit_frame.btn_submit("edit_src");
    closeWin();
}

//ボタンを押し不可にする
function btn_disabled() {
    document.forms[0].btn_copy.disabled = true;
    document.forms[0].btn_udpate.disabled = true;
    document.forms[0].btn_up_pre.disabled = true;
    document.forms[0].btn_up_next.disabled = true;
    document.forms[0].btn_del.disabled = true;
}
