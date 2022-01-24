function btn_submit(cmd) {
    if (cmd == 'exec') {
        if (!confirm('処理を開始します。よろしいでしょうか？')) {
            return false;
        }
        if (document.forms[0].SEMESTER.value == '') {
            alert('学期を指定して下さい。');
            return false;
        }
        if (document.forms[0].SEQ.value == '') {
            alert('科目時間割SEQを指定して下さい。');
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return true;
}
//権限チェック
function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}
