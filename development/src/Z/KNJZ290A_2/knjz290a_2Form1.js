//サブミット
function btn_submit(cmd) {
    if (cmd == 'copy') {
        result = confirm('{rval MSG102}' + '(未設定の部分のみ)');
        if (result == false) {
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
