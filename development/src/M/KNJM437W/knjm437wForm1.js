// 漢字
function btn_submit(cmd) {
    if (cmd == 'reset' && !confirm('{rval MSG106}'))  return true;

    if (cmd == 'update') {
        if (document.forms[0].TESTCD.value == '') {
            alert('テスト種別を指定して下さい。');
            return;
        }
        if (document.forms[0].TEST_DATE.value == '') {
            alert('日付を指定して下さい。');
            return;
        }
        if (document.forms[0].SELSUB.value == '') {
            alert('科目を指定して下さい。');
            return;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
