function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function Cleaning_window(){
    var str = 'knjz220index.php?cmd=edit&ini=1&GRADE=';
    var numb = document.forms[0].GRADE.value;
    str = str + numb;
    window.open(str,'right_frame');
    return false;
}

function closing_window(){
        alert('{rval MSG300}');
        closeWin();
        return true;
}
