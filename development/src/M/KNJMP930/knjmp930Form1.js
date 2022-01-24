function btn_submit(cmd) {

    if (cmd == 'clear') {
        if (!confirm('{rval MSG106}'))
            return false;
    }
    if (cmd == 'search') {
        document.getElementById('marq_msg').innerHTML = '検索しています...しばらくおまちください';
        document.getElementById('marq_msg').style.color = '#FF0000';
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
