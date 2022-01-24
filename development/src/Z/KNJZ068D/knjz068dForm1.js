function btn_submit(cmd)
{
    if (cmd == 'reset' && !confirm('{rval MSG106}')){
        return;
    }
    //CSV
    if (cmd == 'exec') {
        if (document.forms[0].IBYEAR.value == '') {
            alert('年度を指定してください');
            return false;
        }
        cmd = 'downloadCsv';
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function closeMethod() {
    closeWin();
}

function scrollRC(){
    document.getElementById('trow').scrollLeft = document.getElementById('tbody').scrollLeft;
    document.getElementById('tcol').scrollTop = document.getElementById('tbody').scrollTop;
}

