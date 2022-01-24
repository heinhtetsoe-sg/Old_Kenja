function btn_submit(cmd) {

    if (cmd == 'copy') {
        document.forms[0].Cleaning.value = 'off';
    }

    if (cmd == 'update' || cmd == 'setdef') {
        if (document.forms[0].SUBCLASSCD.value == '') {
            alert('{rval MSG308}');
            return true;
        }
    }

    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function level(cnt) {
    
    var level = document.forms[0].ASSESSLEVELCNT.value;
    if(level == cnt){
        return false;
    }
    if (level > 100) {
        alert('{rval MSG913}'+'•]‰¿’iŠK”‚Í100‚ğ’´‚¦‚Ä‚Í‚¢‚¯‚Ü‚¹‚ñB');
        return false;
    } 
    
    document.forms[0].cmd.value = 'new';
    document.forms[0].submit();
    return false;
}

function ShowConfirm() {
    if (!confirm('{rval MSG106}')) {
        return false;
    }
}

function isNumb(ival) {
    return toInteger(ival)
}

function cleaning_val(str) {
    if (str == 'off') {
        document.forms[0].Cleaning.value = 'off';
    }
}

