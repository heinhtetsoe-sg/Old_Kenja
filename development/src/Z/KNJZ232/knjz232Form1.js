function btn_submit(cmd) {
    if (cmd == 'clear'){
        if (!confirm('{rval MZ0003}'))
            return false;
    }

//    if (cmd == 'list') {
//        window.open('knjz232index.php?cmd=sel&init=1','right_frame');
//    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function ClearList(OptionList, TitleName) 
{
    OptionList.length = 0;
}

function OnAuthError()
{
    alert('{rval MZ0026}');
    closeWin();
}
