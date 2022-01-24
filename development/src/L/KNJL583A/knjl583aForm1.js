function btn_submit(cmd) {

    //終了
    if (cmd == 'end') {
        closeWin();
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
