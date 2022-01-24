function btn_submit(cmd) {
    if (cmd == "delete") {
        if (!confirm('{rval MSG103}')) {
            return false;
        }
    }
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
        else {
            document.forms[0].cmd.value = cmd;
            document.forms[0].submit();
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function checkaut()
{
    var flg = document.forms[0].AUTOCALC.checked;
    if(flg){
        document.forms[0].AUTOCALC.value = '1';
        document.all('check').innerHTML=document.all('check1').innerHTML;
        document.forms[0].INC_MAGNIFICATION.disabled = false;
    }else{
        document.all('check').innerHTML=document.all('check2').innerHTML;
        document.forms[0].INC_MAGNIFICATION.disabled = true;
    }
}

function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}
