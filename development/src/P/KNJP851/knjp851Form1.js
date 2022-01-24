function btn_submit(cmd) {
    //月チェック
    var selectedMonth = document.forms[0].SELECTED_MONTH;
    var chkList = document.querySelectorAll(".monthchk");
    var sep = "";
    var monthChkFlg = false;
    selectedMonth.value = "";
    for (var i = 0; i < chkList.length; i++) {
        if (chkList[i].checked) {
            selectedMonth.value += sep + chkList[i].value;
            sep = ",";
            monthChkFlg = true;
        }
    }

    if (cmd == "csv") {
        var patternDiv = document.forms[0].PATTERN_DIV.value;
        //入金計画・実績情報の出力対象が未選択
        if (patternDiv == "1" && !monthChkFlg) {
            alert("{rval MSG310}" + "\n出力対象を1つ以上選択してください。");
            return;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//指定したクラスに属するチェックボックスを全てチェック
function checkAll(targetClass) {
    var checkState = document.forms[0].MONTH_CHK_ALL.checked;

    var chkList = document.querySelectorAll("." + targetClass);
    for (var i = 0; i < chkList.length; i++) {
        chkList[i].checked = checkState;
    }
}
