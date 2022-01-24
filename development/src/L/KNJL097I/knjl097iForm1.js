function btn_submit(cmd) {
    if(cmd == 'reset' && !confirm('{rval MSG106}'))  return true;

    if (cmd == 'update') {
        //どこか選択されているかをチェック
        var wkFlg = false;
        var splStr = document.forms[0].HID_RECEPTNO.value.split(",");
        for (cnt = 0; cnt < splStr.length; cnt++) {
            if (document.forms[0]["CHECKED_"+splStr[cnt]].checked) {
                wkFlg = true;
            }
        }
        if (!wkFlg) {
            alert('{rval MSG304}'+"どこにもチェックが付いていません。");
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}


//全チェック
function setSelChk(obj) {
    var setval = obj.checked;
    var splStr = document.forms[0].HID_RECEPTNO.value.split(",");
    for (cnt = 0; cnt < splStr.length; cnt++) {
        if (!document.forms[0]["CHECKED_"+splStr[cnt]].disabled) {
            document.forms[0]["CHECKED_"+splStr[cnt]].checked = setval;
        }
    }
}