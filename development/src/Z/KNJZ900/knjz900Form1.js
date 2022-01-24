function btn_submit(cmd){

    if (cmd == "delete") {
        result = confirm('{rval MSG103}');
        if (result == false) {
            return false;
        }
    } else if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')){
            return false;
        }else{
            document.forms[0].cmd.value = cmd;
            document.forms[0].submit();
            return false;
        }
    } else if (cmd == 'insert' || cmd == 'update') {
        if (document.forms[0].MENUID.value == "" && document.forms[0].PROGRAMID.value == ""){
            alert('{rval MSG304}\n　　（プログラム）');
            return true;
        }
        if (document.forms[0].NAME.value == ""){
            alert('{rval MSG304}\n　　（パラメータ）');
            document.forms[0].NAME.focus();
            return true;
        }
        document.forms[0].PROGRAMID.value = document.forms[0].PROGRAMID.value.toUpperCase();
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//権限チェック
function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}

//disabled
function OptionUse(obj) {
    if(document.forms[0].INPUT[0].checked == true) {
        document.forms[0].MENUID.disabled = false;
        document.forms[0].PROGRAMID.disabled = true;
		document.forms[0].PROGRAMID.style.backgroundColor = "#D3D3D3";
    } else {
        document.forms[0].MENUID.disabled = true;
        document.forms[0].PROGRAMID.disabled = false;
		document.forms[0].PROGRAMID.style.backgroundColor = "#ffffff";
    }
}
