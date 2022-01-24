function Btn_reset(cmd) {
    result = confirm('{rval MSG107}');
    if (result == false) {
        return false;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function btn_submit(cmd)
{
    if(cmd == 'adoreset'){
        if(!confirm('{rval MSG106}')){
            return false;
        }
    }
    if(cmd == 'end'){
        top.main_frame.left_frame.location.href='knjz020kindex.php?cmd=end';
        top.main_frame.left_frame.closeit();
        return false;
    }
    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    sep = "";
//    if (document.forms[0].L_SUBCLASS.length==0 && document.forms[0].R_SUBCLASS.length==0) {
//        alert("データは存在していません。");
//        return false;
//    }
    for (var i = 0; i < document.forms[0].L_SUBCLASS.length; i++)
    {
        attribute3.value = attribute3.value + sep + document.forms[0].L_SUBCLASS.options[i].value;
        sep = ",";
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}
