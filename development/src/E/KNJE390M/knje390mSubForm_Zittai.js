//選択ボタン押し下げ時の処理
function btn_submit(cmd) {
    if (cmd == "subformZittai_copy") {
        if (document.forms[0].RECORD_HISTORY.value == "") {
            alert("{rval MSG203}" + "\nコピーする日付を指定してください。");
            return false;
        }

        var msg = "実態表を " + document.forms[0].RECORD_HISTORY.value + "データを元に新規作成しますか？";
        msg += "\n（本日の日付が作成年月日となります。）";

        if (!confirm(msg)) {
            return false;
        }

        if (document.forms[0].RECORD_HISTORY.value == document.forms[0].CTRL_DATE.value) {
            alert("{rval MSG203}" + "\n作成日付と元データの日付が同一の場合、処理できません。");
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
