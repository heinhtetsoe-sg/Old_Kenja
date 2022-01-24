function btn_submit(cmd) {
    //入力チェック
    if (document.forms[0].DIARY_DATE.value == "") {
        alert("日付が未入力です。");
        document.forms[0].DIARY_DATE.focus();
        return false;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
