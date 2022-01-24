function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//欠席チェックON/OFFで背景色を黄色表示
function bgcolorYellow(receptno, examno, obj) {

    //更新処理用（得点、受験番号セット）
    document.forms[0].HID_RECEPTNO.value = receptno;
    document.forms[0].HID_EXAMNO2.value  = examno;

    //次の受験番号セット
    var setArr = document.forms[0].HID_EXAMNO.value.split(obj.id);
    var nextId = setArr[1].substr(1, 5);
    document.forms[0].NEXT_ID.value = nextId;

    var y = document.getElementById("mainDiv").scrollTop;
    document.forms[0].SET_SC_VAL.value = y;

    document.forms[0].cmd.value = 'update';
    document.forms[0].submit();
    return false;

}

//カーソルセット
function setCursor(nextId, setval) {
    document.getElementById("mainDiv").scrollTop = setval;

    if (document.getElementById(nextId)) {
        document.getElementById(nextId).focus();
        document.getElementById(nextId).select();
    }
}
