function btn_submit(cmd) {
    if (document.forms[0].SCHREGNO.value == "") {
        alert("{rval MSG304}");
        return true;
    }
    if (cmd == "subform3_copy") {
        if (document.forms[0].RECORD_HISTORY.value == document.forms[0].CTRL_DATE.value) {
            alert("{rval MSG203}" + "\n作成日付と元データの日付が同一の場合、処理できません。");
            return false;
        }

        var msg = "基本情報を " + document.forms[0].RECORD_HISTORY.value + "データを元に新規作成しますか？";
        msg += "\n（本日の日付が作成年月日となります。）";
        if (!confirm(msg)) {
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function NextStudent(cd) {
    var nextURL;
    nextURL = loadCookie("nextURL");
    if (nextURL) {
        if (cd == "0") {
            //クッキー削除
            deleteCookie("nextURL");
            document.location.replace(nextURL);
            alert("{rval MSG201}");
        } else if (cd == "1") {
            //クッキー削除
            deleteCookie("nextURL");
        }
    }
}

//Submitしない
function btn_keypress() {
    if (event.keyCode == 13) {
        event.keyCode = 0;
        window.returnValue = false;
    }
}
