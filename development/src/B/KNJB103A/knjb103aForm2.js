function btn_submit(cmd) {
    //追加と更新
    if (cmd == 'add' || cmd == 'update') {
        //時限
        if (document.forms[0].PERIODNAME2.value == '') {
            alert('{rval MSG301}');
            return false;
        }
        //開始時間
        if (document.forms[0].STARTTIME_HOUR.value == '') {
            alert('{rval MSG301}');
            return false;
        }
        //分
        if (document.forms[0].STARTTIME_MINUTE.value == '') {
            alert('{rval MSG301}');
            return false;
        }
        //終了時間
        if (document.forms[0].ENDTIME_HOUR.value == '') {
            alert('{rval MSG301}');
            return false;
        }
        //分
        if (document.forms[0].ENDTIME_MINUTE.value == '') {
            alert('{rval MSG301}');
            return false;
        }

        if (document.forms[0].STARTTIME_HOUR.value > document.forms[0].ENDTIME_HOUR.value) {
            alert('{rval MSG916}' + '\n( 開始時間、終了時間の大小 )');
            return false;
        } else if (document.forms[0].STARTTIME_HOUR.value == document.forms[0].ENDTIME_HOUR.value && document.forms[0].STARTTIME_MINUTE.value >= document.forms[0].ENDTIME_MINUTE.value) {
            alert('{rval MSG916}' + '\n( 開始時間、終了時間の大小 )');
            return false;
        }
    }

    //削除確認
    if (cmd == 'delete') {
        if (!confirm('{rval MSG103}')) {
            return false;
        }
    }
    //取消確認
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//権限チェック
function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();
}
