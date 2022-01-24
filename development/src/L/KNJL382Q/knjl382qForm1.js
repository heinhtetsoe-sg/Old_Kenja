function btn_submit(cmd) {
    
    if (cmd == 'copy'){
        if(document.forms[0].COPY_YEAR.value == ''){
            alert('コピーする年度を選択してください。');
            return false;
        }else{
            if(!confirm('現年度の作成済みデータはすべて削除されますが、コピーしてよろしいですか？')){
                return false;
            }
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
