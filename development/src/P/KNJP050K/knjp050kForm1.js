function btn_submit(cmd) {
    if (cmd == 'copy') {
        if (!confirm('{rval MSG101}')) {
            alert('{rval MSG203}');
            return;
        } 
        if (document.forms[0].money_flg.value != 0) {
            alert('{rval MSG203}\n\n' + "入金済データが存在します。");
            return;
        } else if(document.forms[0].year_flg.value != 0) {
            if (!confirm('{rval MSG104}')) {
                alert('{rval MSG203}');
                return;
            }
        }
        if (document.forms[0].mst_flg.value == true) {
            var msg = '前年度と今年度のマスタの内容が違うため\n' + 'コピーされないデータが存在します。\n\n';
                msg = msg +  '処理を続行しますか？';
            if (!confirm(msg)) {
                alert('{rval MSG203}');
                return;
            }
        }            
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

function OnPreError(cd)
{
    alert('{rval MSG305}' + '\n('+cd+')');
    closeWin();
}
