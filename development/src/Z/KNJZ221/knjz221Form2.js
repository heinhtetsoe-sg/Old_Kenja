function btn_submit(cmd) {
    //更新
    if (cmd == 'update') {
        if (document.forms[0].SEMESTER.value == '' || document.forms[0].CREDIT.value == '') {
            alert('{rval MSG308}');
            return true;
        }
    }

    //取消
    if (cmd == 'reset' && !confirm('{rval MSG106}')) {
        return false;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
