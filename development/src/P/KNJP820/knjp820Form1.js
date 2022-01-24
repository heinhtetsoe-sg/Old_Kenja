function btn_submit(cmd) {
    if (document.forms[0].SELDATE.value == null || document.forms[0].SELDATE.value == "") {
        alert('{rval MSG902}');
        return false;
    }
    var seldate = new Date(document.forms[0].SELDATE.value);
    var selyear = document.forms[0].SELYEAR.value;
    var leastdate = new Date(selyear , 3 , 1);
    var greatestdate = new Date((parseInt(selyear, 10) + 1), 3, 0);

    if (seldate < leastdate || greatestdate < seldate) {
        alert('{rval MSG914}' + "指定年度内の異動年月日を指定してください。");
        return false;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//権限チェック
function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();

}
