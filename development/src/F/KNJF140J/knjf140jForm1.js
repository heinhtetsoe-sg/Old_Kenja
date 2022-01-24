function btn_submit(cmd)
{
    document.forms[0].encoding = "multipart/form-data";
    if (cmd == 'exec' && !confirm('処理を開始します。よろしいでしょうか？')) {
           return true;
    }
    if (cmd == 'exec') {
        if (document.forms[0].OUTPUT[2].checked == true)
        {
            if (document.forms[0].SHORI_MEI.value == '2' && !confirm('（再確認）削除を開始します。よろしいでしょうか？')) {
                   return true;
            }
            document.all('marq_msg').style.color = '#FF0000';
        } else {
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

//セキュリティーチェック
function OnSecurityError()
{
    alert('{rval MSG300}' + '\n高セキュリティー設定がされています。');
    closeWin();
}
