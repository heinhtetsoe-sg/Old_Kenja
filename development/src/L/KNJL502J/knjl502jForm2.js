function btn_submit(cmd) {
    if (cmd == "delete") {
        if (!confirm('{rval MSG103}')) {
            return false;
        }
    }

    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) {
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
//権限チェック
function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();
}