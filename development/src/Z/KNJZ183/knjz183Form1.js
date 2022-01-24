function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//事前・権限チェック
function closing_window(div) {
    if (div == '1') {
        alert('{rval MSG305}\n学年が設定されていません。');
    } else {
        alert('{rval MSG300}');
    }
    closeWin();
}
