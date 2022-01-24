function btn_submit(cmd) {
    if (cmd == 'exec' && !confirm('処理を開始します。よろしいでしょうか？')){
        return true;
    }
    if (cmd == 'exec'){
        if (document.forms[0].OUTPUT[1].checked == true){
            //処理なし
        }else {
            cmd = "csv";
        }
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