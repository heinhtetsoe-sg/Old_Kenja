function btn_submit(cmd) {
    if (cmd == "exec") {
        if (document.forms[0].TESTDIV.value == '') {
            alert('{rval MSG301}' + '( 入試区分 )');
            return false;
        } 

        if (!confirm("処理を開始します。よろしいでしょうか？")) {
            return;
        }
        document.getElementById('marq_msg').style.color = '#FF0000';
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
