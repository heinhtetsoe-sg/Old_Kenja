function btn_submit(cmd)
{   
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function closing_window(no)
{
    var msg;

    if(no == 'year'){
        alert('{rval MSG306}');
    }else if(no == 'cm'){
        alert('{rval MB6101}');     //権限がありません
    }else if(no == 'se'){
        alert('{rval MSG306}');
    }

    closeWin();
    return true;
}

function showConfirm()
{
    if(confirm('{rval MSG106}')) return true;
    return false;
}
