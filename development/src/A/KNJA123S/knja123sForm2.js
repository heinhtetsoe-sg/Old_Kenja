function btn_submit(cmd){
    if (document.forms[0].SCHREGNO.value == ""){
        alert('{rval MSG304}');
        return true;
    }

    var reason = document.forms[0].CANCEL_REASON.value;
    if (cmd == "sslApplet" && reason.length > 42) {
        alert('入力桁数オーバー');
        return true;
    }
    top.right_frame.document.forms[0].CANCEL_REASON.value = reason;
    top.right_frame.document.forms[0].cmd.value = cmd;
    top.right_frame.btn_submit(cmd);
    top.right_frame.closeit();
    return false;
}
