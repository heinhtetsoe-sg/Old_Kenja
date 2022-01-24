function btn_submit(cmd) {

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function Page_jumper(jump,no) {
    var cd;
    cd = '?NO=';

    if (document.forms[0].SEND_selectSchoolKind) {
        param = '&SEND_selectSchoolKind=' + document.forms[0].SEND_selectSchoolKind.value;
    }

    parent.location.replace(jump + cd + no + param);
}

function Cleaning() {
    var str = 'knjz300index.php?cmd=edit&csvYear=' + document.forms[0].year.value;
    btn_submit('list');
    window.open(str,'right_frame');
    return false;
}

function closing_window(cd){

    if (cd) {
        alert('{rval MSG300}');
    } else {
        alert('{rval MSG305} \r\n職員マスタを登録してください。');
    }
    closeWin();
    return true;
}
