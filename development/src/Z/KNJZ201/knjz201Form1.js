function btn_submit(cmd) {
    var exists_flg              = document.forms[0].EXISTS_FLG;
    var absence_warn_exists_flg = document.forms[0].ABSENCE_WARN_EXISTS_FLG;
    var jugyou_jisu_flg         = document.forms[0].JUGYOU_JISU_FLG;

    if (document.forms[0].ABSENCE_WARN_CHECK.checked) {
        if (document.forms[0].ABSENCE_WARN.value == '') {
            alert('欠課数オーバーに値が入力されていません');
            return false;
        }
    }

    if (jugyou_jisu_flg.value == 'touroku_sarete_nai') {
        alert('{rval MSG203}\n授業時数管理区分を設定して下さい。');
        return false;
    }
    if (jugyou_jisu_flg.value == 'flg_null') {
        alert('{rval MSG203}\n授業時数管理区分を設定して下さい。');
        return false;
    }
    if (jugyou_jisu_flg.value == '2') {
        if (!confirm('授業時数管理区分は実授業が選択されています。よろしいでしょうか？')) {
            return false;
        }
    }

    if (exists_flg.value = 'aru') {
        if (!confirm('{rval MSG104}\n(上限値)')) {
            return false;
        }
    }

    if (document.forms[0].EXISTS_CHECK3.value == 'aru') {
        if (
            (document.forms[0].ABSENCE_WARN_CHECK.checked  && document.forms[0].ABSENCE_WARN.value != '') ||
            (document.forms[0].ABSENCE_WARN_CHECK2.checked && document.forms[0].ABSENCE_WARN2.value != '') ||
            (document.forms[0].ABSENCE_WARN_CHECK3.checked && document.forms[0].ABSENCE_WARN3.value != '')
        ) {
            if (absence_warn_exists_flg.value = 'aru') {
                if (!confirm('{rval MSG104}\n(欠課数オーバー)')) {
                    return false;
                }
            }
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
