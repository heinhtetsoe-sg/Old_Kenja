function btn_submit(cmd) {
    
    if(cmd == "delete"){
        if(!confirm('削除してよろしいですか?')){
            return false;
        }
    }else if(cmd == "exec"){
        if(!confirm('すでに実行し作成したデータは削除されますがよろしいですか？')){
            return false;
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

