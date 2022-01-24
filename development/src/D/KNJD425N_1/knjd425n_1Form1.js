function btn_submit(cmd, seq) {

    if (document.forms[0].SCHREGNO.value == ""){
        alert('{rval MSG304}');
        return true;
    } else if (cmd == 'delete') {
        if (!confirm('{rval MSG103}')) {
            return false;
        }
        document.forms[0].DELETE_SEQ.value = seq;
    } else if (cmd == 'update') {
        document.forms[0].UPDATE_SEQ.value = seq;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function resetRemark(seq) {
    if (!confirm('{rval MSG106}')) {
        return false;
    } else {
        var seqList = seq.split(':');

        seqList.forEach(function (value) {
            document.forms[0]['REMARK-' + value].value = document.forms[0]['INIT_REMARK-' + value].value;
        });
    }
}
