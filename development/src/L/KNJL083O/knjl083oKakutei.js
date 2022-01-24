function btn_submit(cmd) {
    if (cmd == 'retParent') {
        top.main_frame.btn_submit('');
        top.main_frame.closeit();
    }
    //サブミット
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
