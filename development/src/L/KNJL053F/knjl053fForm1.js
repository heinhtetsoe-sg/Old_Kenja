function btn_submit(cmd) {
    if(cmd == 'reset' && !confirm('{rval MSG106}'))  return true;

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//欠席チェックON/OFFで背景色を黄色表示
function bgcolorYellow(obj, examno) {
    change_flg = true;

    document.forms[0].HID_APPLICANTDIV.value = document.forms[0].APPLICANTDIV.options[document.forms[0].APPLICANTDIV.selectedIndex].value;
    document.forms[0].HID_TESTDIV.value = document.forms[0].TESTDIV.options[document.forms[0].TESTDIV.selectedIndex].value;

    document.forms[0].APPLICANTDIV.disabled = true;
    document.forms[0].TESTDIV.disabled = true;

    document.getElementById('ROWID' + examno).style.background = obj.checked ? "yellow" : "white";
}

//スクロール
function scrollRC() {
    document.getElementById('trow').scrollLeft = document.getElementById('tbody').scrollLeft;
    document.getElementById('tcol').scrollTop  = document.getElementById('tbody').scrollTop;
}

//受験番号変更したら更新ボタンをグレーにする
function btn_disabled() {
    document.forms[0].btn_update.disabled = true;
}
