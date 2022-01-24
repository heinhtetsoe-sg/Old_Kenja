function btn_submit(cmd) {
    if (cmd == 'execute' && !confirm('{rval MSG101}'+'\n定義した文字数より多い場合は、自動的に削除されます。')){
        return true;
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