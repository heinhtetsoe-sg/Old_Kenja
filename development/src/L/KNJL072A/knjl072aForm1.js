function btn_submit(cmd) {
    if (cmd == 'sim' || cmd == 'decision') {
        if (document.forms[0].APPLICANTDIV.value == '') {
            alert('{rval MSG301}' + '\n ( 受験校種 )');
            return false;
        }
        if (document.forms[0].TESTDIV.value == '') {
            alert('{rval MSG301}' + '\n ( 試験区分 )');
            return false;
        }
        if (document.forms[0].WISH_COURSE.value == '') {
            alert('{rval MSG301}' + '\n ( 受験コース )');
            return false;
        }
        if (document.forms[0].SHDIV.value == '') {
            alert('{rval MSG301}' + '\n ( 専併区分 )');
            return false;
        }
    }
    if ((cmd == 'sim' || cmd == 'decision') && !confirm('{rval MSG101}')) {
        return true;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
