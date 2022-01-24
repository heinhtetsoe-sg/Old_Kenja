function btn_submit(cmd)
{
    if (cmd == 'update') {
    
        var i = document.forms[0].SCHREGNO.selectedIndex;
        if (document.forms[0].SCHREGNO.options[i].value == '') {
            alert('{rval MSG304}');
            return false;
        }
    }
    if (cmd == 'reset' && !confirm('{rval MSG106}')){
        return;
    }
    //フレームロック機能（プロパティの値が1の時有効）
    if (document.forms[0].useFrameLock.value == "1") {
        if (cmd == 'update') {
            updateFrameLock();
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}