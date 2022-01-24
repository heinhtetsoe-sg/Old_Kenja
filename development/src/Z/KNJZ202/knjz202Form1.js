function btn_submit(cmd) {
    if (cmd == 'execute') {
        var absence_warn_exists_flg = document.forms[0].ABSENCE_WARN_EXISTS_FLG;

        if (document.forms[0].dis_absence.value == 'off') {
            alert('{rval MSG305}' + '\n\n授業時数管理区分が設定されていません。\n学校マスタの他条件設定画面にて設定して下さい。');
            return;
        }
        //既に欠課数オーバーが登録されているかチェックする
        if (
            (document.forms[0].ABSENCE_WARN_CHECK.checked  && document.forms[0].ABSENCE_WARN.value != '') ||
            (document.forms[0].ABSENCE_WARN_CHECK2.checked && document.forms[0].ABSENCE_WARN2.value != '') ||
            (document.forms[0].ABSENCE_WARN_CHECK3 && document.forms[0].ABSENCE_WARN_CHECK3.checked && document.forms[0].ABSENCE_WARN3 && document.forms[0].ABSENCE_WARN_CHECK3.checked && document.forms[0].ABSENCE_WARN3.value != '')
        ) {
            if (absence_warn_exists_flg.value = 'aru') {
                if (!confirm('{rval MSG104}\n(欠課数オーバー)')) {
                    return false;
                }
            }
        }
        if (confirm("実授業数の上限値の算定を行います。\nよろしいですか？")) {
            document.all('marq_msg').style.color = '#FF0000';
        } else {
            return;
        }
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

function closing_window(flg){
    if (flg == 1) {
        alert('{rval MSG300}');
    }
    if (flg == 2) {
        alert('{rval MSG305}' + '\n(学期マスタ)');
    }
    if (flg == 3) {
        alert('{rval MSG300}' + '\n学校マスタの設定を確認して下さい。');
    }
    closeWin();
    return true;
}
