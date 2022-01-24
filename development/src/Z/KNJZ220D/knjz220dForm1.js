function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function iniright(){
    var g = document.forms[0].GRADE.value;
    var s = document.forms[0].SEMESTER.value;
    var a = 'knjz220dindex.php?cmd=edit&inir=1&GRADE=' + g + '&SEMESTER=' + s;
    window.open(a, 'right_frame');
    return false;
}

function close_window(){
    alert('{rval MSG300}');
    closeWin();
    return true;
}
