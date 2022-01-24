function btn_submit(cmd) {

    if (cmd == 'delete' && !confirm('{rval MSG103}')){
        return true;
    }
    if (cmd == 'houkoku' || cmd == 'update') {
        if (document.forms[0].SCHOOLCD.value == "") {
            alert('教育委員会統計用学校番号が、未登録です。');
            return false;
        }
    }
    if (cmd == 'houkoku') {
        if (document.forms[0].EXECUTE_DATE.value == "") {
            alert('{rval MSG304}'+'(作成日)');
            return false;
        }
        if (!confirm('{rval MSG108}')) return false;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function Btn_reset(cmd) {
    result = confirm('{rval MSG107}');
    if (result == false) {
        return false;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function Page_jumper(link) {
    if (document.forms[0].SCHREGNO.value == "") {
        alert('{rval MSG308}');
        return;
    }
    if (!confirm('{rval MSG108}')) {
        return;
    }
    parent.location.href=link;
}
