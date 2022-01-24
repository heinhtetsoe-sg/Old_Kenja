function btn_submit(cmd) {

    if (cmd == 'clear') {
        if (!confirm('{rval MSG106}'))
        return false;
    }
    if (cmd == 'delete'){
        if (!confirm('{rval MSG103}'))
            return false;
    }
    document.forms[0].cmd.value = cmd;

    for (var i = 0; i < document.forms[0].length; i++) {
        if (document.forms[0][i].disabled) {
            document.forms[0][i].disabled = false;
        }
    }

    document.forms[0].submit();
    return false;
}

function ShowConfirm(){
    if (!confirm('{rval MSG106}'))
        return false;
}

function Page_jumper(link) {
    if (document.forms[0].UPDATED.value == "" && document.forms[0].GUARANTOR_UPDATED.value == "") {
        alert('リストから生徒を選択してください。');
        return;
    }
    if (!confirm('{rval MSG108}')) {
        return;
    }
    parent.location.href=link;
}

function modoru(link) {
    if (!confirm('{rval MSG108}')) {
        return;
    }
    location.href=link;
}
