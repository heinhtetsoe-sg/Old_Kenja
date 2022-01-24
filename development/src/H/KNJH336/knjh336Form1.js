function btn_submit(cmd) {
    if (cmd == 'execute') {
        if (document.forms[0].OUTPUT[1].checked == true) {
            if (confirm('{rval MSG102}')) {
                if (document.forms[0].SHORI_MEI.value == '2' && !confirm('（再確認）削除を開始します。よろしいでしょうか？')){
                       return true;
                   }
            } else {
                return;
            }
        }
        if (!document.forms[0].OUTPUT[1].checked == true) {
            cmd = "exec";
        }
        if (document.forms[0].OUTPUT[0].checked == true) {
            cmd = "output";
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

function OutputFile(filename)
{
    parent.top_frame.location.href=filename;
}
