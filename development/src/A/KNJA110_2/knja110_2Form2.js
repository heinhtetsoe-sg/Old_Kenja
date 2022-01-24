function btn_submit(cmd) {

    if (cmd == 'clear') {
        if (!confirm('{rval MSG106}'))
        return false;
    }
    if (cmd == 'delete'){
        if (!confirm('{rval MSG103}'))
            return false;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function ShowConfirm(){
    if (!confirm('{rval MSG106}'))
        return false;
}

function copy(mode){
    with(document.forms[0]){
        if (mode == 1){
            ZIPCD.value = GUARD_ZIPCD.value;
            ADDR1.value = GUARD_ADDR1.value;
            ADDR2.value = GUARD_ADDR2.value;
            TELNO.value = GUARD_TELNO.value;
            FAXNO.value = GUARD_FAXNO.value;
        }else{
            GUARD_ZIPCD.value = ZIPCD.value;
            GUARD_ADDR1.value = ADDR1.value;
            GUARD_ADDR2.value = ADDR2.value;
            GUARD_TELNO.value = TELNO.value;
            GUARD_FAXNO.value = FAXNO.value;
        }
    }
}