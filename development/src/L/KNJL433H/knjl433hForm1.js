function btn_submit(cmd) {
    //取消確認
    if (cmd == "reset" && !confirm("{rval MSG106}")) return true;
    //CSV
    if (cmd == "exec") {
        alert("工事中");
        return false;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//点数チェック
function validateScore(txt) {
    var before = txt.value;
    txt.value = toInteger(txt.value);
    if (txt.value != before) {
        //入力整合エラー
        return false;
    }

    //満点チェック
    var perfect = Number(txt.getAttribute("max"));
    if (isNaN(perfect) == false) {
        if (perfect < Number(txt.value)) {
            var detail = "\n受験番号: {$receptNo}　入力値: {$score}　有効範囲: ( 0 ～ {$perfect})";
            detail = detail.replace("{$receptNo}", txt.name.replace("SCORE-", ""));
            detail = detail.replace("{$score}", txt.value);
            detail = detail.replace("{$perfect}", perfect);
            alert("{rval MSG914}" + detail);
            return false;
        }
    }

    return true;
}

// Enterキーが押されたときに「TABキーが押された」イベントにするメソッド
function keyChangeEntToTab(txt) {
    // Ent13 Tab9 ←37 ↑38 →39 ↓40
    var keycode = window.event.keyCode;
    switch (keycode) {
        case 13: //Enter
            break;
        default:
            return true;
    }

    var fields = document.getElementsByName("TABSFEILDS")[0].value.split(",");
    for (var idx = 0; fields != null && idx < fields.length; idx++) {
        if (idx + 1 < fields.length && fields[idx] == txt.name) {
            var next = document.getElementsByName(fields[idx + 1])[0];
            next.focus();
            next.select();
            break;
        }
    }

    return false; // invalid enter key
}
