function btn_submit(cmd) {
    if (cmd == 'delete'){
        if (!confirm('{rval MSG103}'))
            return false;
    }
    if (cmd == 'reset'){
        if (!confirm('{rval MSG106}'))
            return false;
    }

    if (cmd == 'update' || cmd == 'add') {
        if (document.forms[0].QUALIFIED_JUDGE_CD.value == '') {
            alert('{rval MSG301}' + '\n(入試資格判定ＣＤ)');
            return false;
        }
        if (document.forms[0].QUALIFIED_NAME.value == '') {
            alert('{rval MSG301}' + '\n(入試資格名称)');
            return false;
        }
    }

    //読込中は、追加・更新・削除ボタンをグレーアウト
    document.forms[0].btn_add.disabled      = true;
    document.forms[0].btn_update.disabled   = true;
    document.forms[0].btn_del.disabled      = true;
    document.forms[0].btn_reset.disabled    = true;

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();
}
