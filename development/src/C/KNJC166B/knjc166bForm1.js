function btn_submit(cmd)
{
    if (cmd == 'reset' && !confirm('{rval MSG106}')){
        return false;
    }

    if (cmd == 'update' && !confirm('{rval MSG102}')) {
        return false;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function changeKikanFlg(schregno) {
    if (document.forms[0]['KAIKIN_FLG_'+schregno].checked == true) {
        document.forms[0]['INVALID_FLG_'+schregno].disabled = false;
    } else {
        document.forms[0]['INVALID_FLG_'+schregno].checked = false;
        document.forms[0]['INVALID_FLG_'+schregno].disabled = true;
    }
}