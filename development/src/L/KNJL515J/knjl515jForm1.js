// kanji=漢字
function btn_submit(cmd) {
    if (cmd == 'exec' || cmd == 'csv') {
        //必須チェック
        if (document.forms[0].APPLICANTDIV.value == '') {
            alert('{rval MSG310}\n( 学校種別 )');
            return;
        }
        if (document.forms[0].TESTDIV.value == '') {
            alert('{rval MSG310}\n( 入試種別 )');
            return;
        }
    }
    if (cmd == 'exec' && !confirm('{rval MSG101}')) {
        return;
    }
    if (cmd == 'exec') {
        document.getElementById('marq_msg').style.color = '#FF0000';
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}
