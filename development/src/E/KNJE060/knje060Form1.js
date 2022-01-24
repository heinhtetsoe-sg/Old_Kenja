function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}
function btn_disabled(obj){
    if (obj.value == 1){
        clr = "#cccccc";
        ret = true;
    }else{
        clr = "#000000";
        ret = false;
    }
    document.getElementById("label1").style.color = clr;
    document.getElementById("label2").style.color = clr;
    document.getElementById("label3").style.color = clr;
    document.forms[0].METHOD[0].disabled = ret;
    document.forms[0].METHOD[1].disabled = ret;
    document.forms[0].REPLACE.disabled = ret;
    document.forms[0].FILE.disabled = ret;
}