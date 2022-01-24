function btn_submit(cmd) {

    if (cmd=="update") {
//        alert('工事中です！');
//        return false;
        if (document.forms[0].seme.options.length == 0) {
            return false;
        }
        if (document.forms[0].grad.options.length == 0) {
            return false;
        }
        if (!confirm('{rval MSG101}')) {
            return false;
        } 
        document.all('marq_msg').style.color = '#FF0000';
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function ShowConfirm(){
    result = confirm('{rval MSG107}');
    if (result == false) {
        return false;
    }
}

function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}

function closing_window(){
    alert('{rval MSG305}' + '\n(評定率設定マスタ)');
    closeWin();
    return true;
}
