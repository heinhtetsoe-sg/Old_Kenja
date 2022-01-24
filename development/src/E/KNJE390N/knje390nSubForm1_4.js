function btn_submit(cmd) {
    //update/deleteは、選択していなければ、NG
    if ((document.forms[0].RECORD_DIV.value == "" || document.forms[0].RECORD_SEQ.value == "")
        && (
            (cmd == 'healthcare1_update' || cmd=='healthcare1_update_care' || cmd=='healthcare1_update_spasm')
            || (cmd=='healthcare1_delete' || cmd=='healthcare1_delete_care' || cmd=='healthcare1_delete_spasm')
           )
       ) {
       alert('{rval MSG304}');
       return true;
    }

    if ((cmd == 'healthcare1_delete' || cmd == 'healthcare1_delete_care' || cmd=='healthcare1_delete_spasm') && !confirm('{rval MSG103}')){
        return true;
    }
    if (cmd=='healthcare1_insert_spasm') {
        document.forms[0].RECORD_DIV.value = "4";
    } else if (cmd=='healthcare1_insert_care') {
        document.forms[0].RECORD_DIV.value = "2";
    } else if (cmd=='healthcare1_insert') {
        document.forms[0].RECORD_DIV.value = "1";
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function ShowConfirm(){
    if (!confirm('{rval MSG106}')){
        return false;
    }
}
