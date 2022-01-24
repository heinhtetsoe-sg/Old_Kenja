function btn_submit(cmd) {
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function check_all(){
    for (var i=0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name == "CHECKED[]") {
            document.forms[0].elements[i].checked = document.forms[0]['CHECKALL'].checked;
            if (document.forms[0].elements[i].checked == true){
                chgColor(document.forms[0].elements[i].value, "#ccffcc");
            }else{
                chgColor(document.forms[0].elements[i].value, "#ffffff");
            }
        }
    }
}

function chgColor(schregno, rgb){
    document.getElementById(schregno).style.background = rgb;
}

function chkClick(obj){
    if (obj.checked == true){
        chgColor(obj.value, "#ccffcc");
    }else{
        chgColor(obj.value, "#ffffff");
    }
}
function closing_window(MSGCD, msg)
{
    if (MSGCD == 'MSG311'){
        alert('{rval MSG311}');
    }else if (MSGCD == 'MSG305'){
        alert('{rval MSG305}'+'\n'+msg);
    }
    closeWin();
    return true;
}
