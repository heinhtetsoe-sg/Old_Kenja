function btn_submit(cmd) {
    if (cmd == 'reset'){
        if (!confirm('{rval MSG106}')){
            return false;
        }else{
            document.forms[0].cmd.value = cmd;
            document.forms[0].submit();
            return false;
        }
    }
    if (cmd == 'delete'){
        if(!confirm('{rval MSG103}')){
            return true;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function Cleaning(){
    document.forms[0].C_switch.value = 'on';
    btn_submit('main');
    return false;
}

function Page_jumper(jump,sno)
{
    var cd;
    if(sno == ''){
        alert('{rval MSG304}');
        return;
    }
    if (!confirm('{rval MSG108}')) {
        return;
    }
    parent.location.replace(jump);
}
