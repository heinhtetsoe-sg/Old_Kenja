
function btn_submit(cmd) {
    if (cmd == 'exec') {

        if (document.forms[0].CHAIR_CNT_FLG.value == '1') {
            if (!confirm('講座情報が既に存在します。講座情報の再作成を行います。よろしいでしょうか？')) {
                return false;
            }
        }
        if (!confirm('{rval MSG101}')) {
            return false;
        }
        if (document.forms[0].YEAR_SEME.value == '') {
            alert('年度学期を指定して下さい。');
            return false;
        }
        if (document.forms[0].PRESEQ.value == '') {
            alert('科目展開表SEQを指定して下さい。');
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
