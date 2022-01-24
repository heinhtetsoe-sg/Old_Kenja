function btn_submit(cmd) {

    //削除
    if (cmd == 'delete') {
        if (!confirm('{rval MSG103}'))
            return false;
    }

    //取消
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')){
            return false;
        } else {
            document.forms[0].cmd.value = cmd;
            document.forms[0].submit();
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function OnSchoolcdError(cnt)
{
    alert('{rval MSG305}' + '\n学校マスタに登録されている教育委員会統計用学校番号とグループウェアの学校コードが正しく登録されているか確認をして下さい。');
    closeWin();
}
