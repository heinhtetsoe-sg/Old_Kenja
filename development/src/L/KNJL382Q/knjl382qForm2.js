window.onload = function(){

}

function btn_submit(cmd) {

    if (cmd == 'delete'){
        if (!confirm('{rval MSG103}'))
            return false;
    }
    if (cmd == 'reset'){
        if (!confirm('{rval MSG106}'))
            return false;
    }
    if (cmd == 'add' || cmd == 'update'){
        if (document.forms[0].GROUPPREF.value == ''){
            alert('県名を選択してください。')
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//権限
function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}
