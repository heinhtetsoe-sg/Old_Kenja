function btn_submit(cmd) {

    if (cmd == 'replace_sub') {
        if (!confirm('事前準備：科目読替先の類型グループ設定されていますか？')) {
            return;
        }
    }
    if (cmd == 'replace_sub' || cmd == 'update' || cmd == 'update2' || cmd == 'updateKariNomi') {
        document.all('marq_msg').style.color = '#FF0000';
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function checkTestDateError()
{
    alert('{rval MSG302}'+'\n テスト実施日');
    closeWin();
}
function btn_openerSubmit(){
    window.opener.btn_submit('main');
    closeWin();
}
