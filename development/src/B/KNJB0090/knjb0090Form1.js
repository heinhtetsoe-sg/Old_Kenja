function btn_submit(cmd) {
    if (cmd == "exec") {
        if (confirm("{rval MSG101}")) {
            document.forms[0].btn_exec.disabled = true;
        } else {
            return;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//権限チェック
function OnAuthError() {
    alert("{rval MSG300}");
    closeWin();
}

//REFLECTDIV='3'の場合、適用期間をチップヘルプで表示
function daysMousein(msg_no) {
    msgObject = eval('document.forms[0]["' + "DAYS" + msg_no + '"]');
    var msg = msgObject.value;

    x = event.clientX + document.body.scrollLeft;
    y = event.clientY + document.body.scrollTop;
    document.all("lay").innerHTML = msg;
    document.all["lay"].style.position = "absolute";
    document.all["lay"].style.left = x + 5;
    document.all["lay"].style.top = y + 10;
    document.all["lay"].style.padding = "4px 3px 3px 8px";
    document.all["lay"].style.border = "1px solid";
    document.all["lay"].style.visibility = "visible";
    document.all["lay"].style.background = "#ccffff";
}

function daysMouseout() {
    document.all["lay"].style.visibility = "hidden";
}
