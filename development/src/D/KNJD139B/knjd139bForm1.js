function btn_submit(cmd) {

    if (cmd == 'update') {
        if (document.forms[0].SCHREGNO.value == "") {
            alert('{rval MSG304}');
            return true;
        }
        if (document.forms[0].SEMESTER.value == "") {
            alert('{rval MSG304}\n( 学期 )');
            return true;
        }
    }

    if (cmd == 'clear') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }

    //フレームロック機能（プロパティの値が1の時有効）
    if (document.forms[0].useFrameLock.value == "1") {
        if (cmd == 'update') {
            updateFrameLocks();
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
