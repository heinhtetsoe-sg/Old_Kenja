function btn_submit(cmd) {
    //取消
    if (cmd == 'reset_remark') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }

    //更新
    if (cmd == 'update_remark') {
        if (!document.forms[0].SCHREGNO.value) {
            alert('{rval MSG304}');
            return;
        }
    }

    //フレームロック機能（プロパティの値が1の時有効）
    if (document.forms[0].useFrameLock.value == "1") {
        if (cmd == 'update_remark') {
            updateFrameLocks();
        }
    }

    //指導計画から取込
    if (cmd == "input") {
        if (document.forms[0].REMARK_CNT.value > 0) {
            if (!confirm('{rval MSG104}')) {
                return false;
            }
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
