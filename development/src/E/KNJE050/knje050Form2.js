function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function check_all(){
    var flg;
    
    for (var i=0;i<document.forms[0].elements.length;i++)
    {
        var e = document.forms[0].elements[i];
        if (e.name == "chk_all"){
            flg = e.checked;
        }
        if (e.type=='checkbox' && e.name != "chk_all"){
            e.checked = flg;
        }
    }
}

function OnAuthError()
{
    alert('{rval MSG300}'); //B
    closeWin();
}