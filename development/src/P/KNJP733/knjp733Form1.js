function btn_submit(cmd) {
    if (cmd == 'exec') {
        if (document.forms[0].REDUCTION_KIND.value == "") {
            alert('{rval MSG310}' + '\n（種別）');
            return false;
        }

        var setMonthFrom = Number(document.forms[0].MONTH_FROM.value);
        var setMonthTo   = Number(document.forms[0].MONTH_TO.value);
        setMonthFrom = (setMonthFrom < 4) ? setMonthFrom + 12: setMonthFrom;
        setMonthTo   = (setMonthTo < 4)   ? setMonthTo   + 12: setMonthTo;

        if (setMonthFrom > setMonthTo) {
            alert('{rval MSG916}' + '\n（期間）');
            return false;
        }

        //フレームロック機能（プロパティの値が1の時有効）
        if (document.forms[0].useFrameLock.value == "1") {
            updateFrameLocks();
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();
}
