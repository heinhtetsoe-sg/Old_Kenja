function btn_submit(cmd)
{
    if (cmd == 'reference') {
        if (document.forms[0].EXAMNO.value == '') {
            alert('{rval MSG301}\n( 受験番号 )');
            return true;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//ボタンを押し不可にする
function btn_disabled() {
    document.forms[0].btn_update.disabled = true;
    document.forms[0].btn_del.disabled = true;
    document.getElementById("NAME_DISP").innerHTML = "";
}
