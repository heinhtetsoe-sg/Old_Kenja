function btn_submit(cmd) {
    if (cmd == 'delete') {
        if (!confirm('{rval MSG103}')) return false;
    }
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) return false;
    }

    if (cmd == 'update' || cmd == 'add') {
        if (document.forms[0].APPLICANT_DIV.value == '') {
            alert('{rval MSG301}' + '\n(入試コード)');
            return false;
        }
        if (document.forms[0].COURSE_DIV.value == '') {
            alert('{rval MSG301}' + '\n(志望コース)');
            return false;
        }
        if (document.forms[0].FREQUENCY.value == '') {
            alert('{rval MSG301}' + '\n(回数)');
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();
}

function Page_jumper(link) {
    if (document.forms[0].UPDATED1.value == '') {
        alert('リストから試験を選択してください。');
        return;
    }
    if (!confirm('{rval MSG108}')) {
        return;
    }
    parent.location.href = link;
}
