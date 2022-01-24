function btn_submit(cmd) {
    if (cmd == 'update'){
        if(document.forms[0].ASSES_CD.value == ''){
            alert('{rval MSG308}');
            return true;
        }
    }

    if (cmd == 'reset'){
        if (!confirm('{rval MSG106}')){
            return false;
        }else{
            document.forms[0].cmd.value = cmd;
            document.forms[0].submit();
            return false;
        }
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

function cleaning_val(str){
    if(str == 'off')
        document.forms[0].Cleaning.value = 'off';
}
