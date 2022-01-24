function btn_submit(cmd) {
    if (cmd == "csv") {
        if (document.forms[0].APPLICANTDIV.value == '') {
            alert('{rval MSG301}' + '( 入試制度 )');
            return false;
        } 
        if (document.forms[0].TESTDIV.value == '') {
            alert('{rval MSG301}' + '( 試験回数 )');
            return false;
        } 
        if (document.forms[0].EXAM_TYPE.value == '') {
            alert('{rval MSG301}' + '( 受験型 )');
            return false;
        } 
        if (document.forms[0].EXAMCOURSECD.value == '') {
            alert('{rval MSG301}' + '( 受験コース )');
            return false;
        } 
        if (document.forms[0].SHDIV.value == '') {
            alert('{rval MSG301}' + '( 出願区分 )');
            return false;
        } 
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
