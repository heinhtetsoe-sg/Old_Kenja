function btn_submit(cmd) {
    
    if(cmd == "import"){
        if(document.forms[0].FILE.value == ''){
            alert('取込ファイルを選択してください。');
            return false;
        }else{
            if(!confirm('すでに取込・修正したデータがある場合、削除されますがよろしいですか？')){
                return false;
            }
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

