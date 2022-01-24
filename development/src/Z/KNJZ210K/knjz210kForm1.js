function btn_submit(cmd) {
    if (cmd == 'copy') {
        if (!confirm('{rval MSG101}')) {
            alert('{rval MSG203}');
            return;
        } else {
            if (document.forms[0].year_flg.value != 0) {
                alert('{rval MSG203}\n\n' + "今年度のデータが存在します");
                return;
            }
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return;
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
