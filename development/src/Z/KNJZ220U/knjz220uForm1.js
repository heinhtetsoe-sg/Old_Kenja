function btn_submit(cmd) {
    if (cmd=="clear") {
        result = confirm('{rval MSG106}');
        if (result == false) {
            return false;
        } 
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function level(cnt) {
    var level;
    level = document.forms[0].ASSESSLEVELCNT.value;
    if(level == cnt){
        return false;
    }

    document.forms[0].cmd.value = 'level';
    document.forms[0].submit();
    return false;
}
