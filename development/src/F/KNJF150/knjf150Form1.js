function btn_submit(cmd) {
    if (document.forms[0].SCHREGNO.value == "") {
        alert("{rval MSG304}");
        return true;
    } else if (cmd == "clear") {
        if (!confirm("{rval MSG106}")) {
            return false;
        }
    }

    if (cmd == "delete" && !confirm("{rval MSG103}")) {
        return true;
    } else if (cmd == "delete") {
        for (var i = 0; i < document.forms[0].elements.length; i++) {
            if (document.forms[0].elements[i].name == "CHECKED[]" && document.forms[0].elements[i].checked) {
                break;
            }
        }
        if (i == document.forms[0].elements.length) {
            alert("チェックボックスを選択してください");
            return true;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//全チェック操作
function check_all(obj) {
    for (var i = 0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name == "CHECKED[]") {
            document.forms[0].elements[i].checked = obj.checked;
        }
    }
}

//体調１～７へマウスを乗せた場合、質問内容をチップヘルプで表示
function ViewcdMousein(e, msg_no) {
    var msg = "";
    if (msg_no == 1) msg = "昨日はよく眠れたか？";
    if (msg_no == 2) msg = "今朝、便はでたか？";
    if (msg_no == 3) msg = "朝食は食べたか？";
    if (msg_no == 4) msg = "疲れている感じはあるか？";
    if (msg_no == 5) msg = "最近心配なこと、気にかかっていること。";

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

function ViewcdMouseout() {
    document.all["lay"].style.visibility = "hidden";
}
