function btn_submit(cmd) {

    if (cmd == "update" && document.forms[0].GRADE.value == "") {
        alert('学年を指定して下さい。');
        return false;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
