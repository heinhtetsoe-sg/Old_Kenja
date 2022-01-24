function btn_submit(cmd) {

    if (document.forms[0].SCHREGNO.value == ""){
        alert('{rval MSG304}');
        return true;
    }

    if (cmd == 'subform3_clear'){
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }

    if ((cmd == 'subform3_update') || (cmd == 'subform3_delete')){
        if (document.forms[0].RELANO.value == "") {
            alert('{rval MSG308}');
            return true;
        }
    }

    if ((cmd == 'subform3_insert') || (cmd == 'subform3_update')){
        if (document.forms[0].RELANAME.value == "") {
            alert("氏名を入力してください");
            return true;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
