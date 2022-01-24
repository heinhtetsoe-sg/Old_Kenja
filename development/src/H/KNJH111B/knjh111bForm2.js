function btn_submit(cmd) {

    if (cmd == 'delete' && !confirm('{rval MSG103}')){
        return true;
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
