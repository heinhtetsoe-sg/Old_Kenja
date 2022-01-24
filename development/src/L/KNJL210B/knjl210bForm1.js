function btn_submit(cmd) {
    document.forms[0].encoding = "multipart/form-data";
    if (cmd == 'execute') {
        if (document.forms[0].OUTPUT[1].checked == true) {
            if (confirm('{rval MSG102}')) {
                if (document.forms[0].SHORI_MEI.value == '3' && !confirm('（再確認）削除を開始します。よろしいでしょうか？')){
                    return true;
                }
            } else {
                return;
            }
        }
        if (!document.forms[0].OUTPUT[1].checked == true) {
            cmd = "exec";
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

//セキュリティーチェック
function OnSecurityError()
{
    alert('{rval MSG300}' + '\n高セキュリティー設定がされています。');
    closeWin();
}

function OutputFile(filename)
{
    parent.top_frame.location.href=filename;
}