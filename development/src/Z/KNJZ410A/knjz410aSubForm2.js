function btn_submit(cmd) {
    if (cmd == "chenge_cd") {
        school_cd = document.forms[0].SCHOOL_CD;
        if (school_cd.value.match(/\D/)) {
            alert('{rval MSG909}');
            return false;
        }
    }

    if (cmd == "delete") {
        if (!confirm('{rval MSG103}')) {
            return false;
        }
    }
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
        else {
            document.forms[0].cmd.value = cmd;
            document.forms[0].submit();
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}

function Page_jumper(link) {
    if (!confirm('{rval MSG108}')) {
        return;
    }
    parent.location.href=link;
}
