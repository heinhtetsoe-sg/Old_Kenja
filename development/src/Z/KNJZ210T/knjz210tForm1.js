function btn_submit(cmd) {

    if (cmd == "copy") {
        if (document.forms[0].SEMESTER.value == ""){
            alert('{rval MSG304}' + ' （学期）');
            return true;
        } else if (document.forms[0].TESTCD.value == ""){
            alert('{rval MSG304}' + ' （成績種別）');
            return true;
        } else if (document.forms[0].RECORD_DIV.value == ""){
            alert('{rval MSG304}' + ' （区分）');
            return true;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
