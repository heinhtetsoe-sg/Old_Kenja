function btn_submit(cmd) {
    if (cmd == 'exec') {
        if (document.forms[0].OUTPUT[1].checked == true) {
            //確認
            if (!confirm('{rval MSG101}')) {
                return true;
            }

            //必須チェック
            if (document.forms[0].EXE_YEAR.value == "") {
                alert('処理年度を選択して下さい。');
                return false;
            }
            if (document.forms[0].GUARD_ISSUEDATE.value == "" || document.forms[0].GUARD_EXPIREDATE.value == "") {
                alert('住所開始日/終了日を入力して下さい。');
                return false;
            }
        } else {
            cmd = "output";
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();
}
