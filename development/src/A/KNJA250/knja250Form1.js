function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function allcheck(checkval) {
    if (checkval.checked){
        document.forms[0].GRAD.checked      = true;
        document.forms[0].MOVE.checked      = true;
        document.forms[0].DROP.checked      = true;
        document.forms[0].GRAD.checked      = true;
        document.forms[0].FOREIGN.checked   = true;
        document.forms[0].HOLI.checked      = true;
        document.forms[0].SUSPEND.checked   = true;
        document.forms[0].ADMISSION.checked = true;
        document.forms[0].MOVINGIN.checked  = true;
        document.forms[0].REMOVE.checked    = true;
        if (document.forms[0].TENSEKI_O !== undefined) {
            document.forms[0].TENSEKI_O.checked = true;
        }
        if (document.forms[0].TENSEKI_I !== undefined) {
            document.forms[0].TENSEKI_I.checked = true;
        }
        document.forms[0].TENKA_O.checked   = true;
        document.forms[0].TENKA_I.checked   = true;
    }else {
        document.forms[0].GRAD.checked      = false;
        document.forms[0].MOVE.checked      = false;
        document.forms[0].DROP.checked      = false;
        document.forms[0].GRAD.checked      = false;
        document.forms[0].FOREIGN.checked   = false;
        document.forms[0].HOLI.checked      = false;
        document.forms[0].SUSPEND.checked   = false;
        document.forms[0].ADMISSION.checked = false;
        document.forms[0].MOVINGIN.checked  = false;
        document.forms[0].REMOVE.checked    = false;
        if (document.forms[0].TENSEKI_O !== undefined) {
            document.forms[0].TENSEKI_O.checked = false;
        }
        if (document.forms[0].TENSEKI_I !== undefined) {
            document.forms[0].TENSEKI_I.checked = false;
        }
        document.forms[0].TENKA_O.checked   = false;
        document.forms[0].TENKA_I.checked   = false;
    }
    return true;
}
function newwin(SERVLET_URL){
    /* NO001 NO002 */
    if((document.forms[0].GRAD.checked == false) && (document.forms[0].MOVE.checked == false) && 
        (document.forms[0].DROP.checked == false) && (document.forms[0].FOREIGN.checked == false) && 
        (document.forms[0].HOLI.checked == false) && (document.forms[0].SUSPEND.checked == false) && 
        (document.forms[0].ADMISSION.checked == false) && (document.forms[0].MOVINGIN.checked == false) && 
        (document.forms[0].REMOVE.checked == false) && 
        (document.forms[0].TENSEKI_O !== undefined && document.forms[0].TENSEKI_O.checked == false) && (document.forms[0].TENSEKI_I !== undefined && document.forms[0].TENSEKI_I.checked == false) && 
        (document.forms[0].TENKA_O.checked == false) && (document.forms[0].TENKA_I.checked == false))
    {
        alert('異動区分を選択してください。');
    }
    else
    {
        //
        action = document.forms[0].action;
        target = document.forms[0].target;

        //url = location.hostname;
        //document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
        document.forms[0].action = SERVLET_URL +"/KNJA";
        document.forms[0].target = "_blank";
        document.forms[0].submit();

        document.forms[0].action = action;
        document.forms[0].target = target;
    }
}
//NO003
function datecheck(checkval) {
    if (checkval.checked){
        document.forms[0].GRAD.disabled      = true;
        document.forms[0].MOVE.disabled      = true;
        document.forms[0].DROP.disabled      = true;
        document.forms[0].GRAD.disabled      = true;
        document.forms[0].FOREIGN.disabled   = false;
        document.forms[0].HOLI.disabled      = false;
        document.forms[0].SUSPEND.disabled   = false;
        document.forms[0].ADMISSION.disabled = true;
        document.forms[0].MOVINGIN.disabled  = true;
        document.forms[0].REMOVE.disabled    = true;
        if (document.forms[0].TENSEKI_O !== undefined) {
            document.forms[0].TENSEKI_O.disabled = true;
        }
        if (document.forms[0].TENSEKI_I !== undefined) {
            document.forms[0].TENSEKI_I.disabled = true;
        }
        document.forms[0].TENKA_O.disabled   = true;
        document.forms[0].TENKA_I.disabled   = true;
    }else {
        document.forms[0].GRAD.disabled      = false;
        document.forms[0].MOVE.disabled      = false;
        document.forms[0].DROP.disabled      = false;
        document.forms[0].GRAD.disabled      = false;
        document.forms[0].FOREIGN.disabled   = false;
        document.forms[0].HOLI.disabled      = false;
        document.forms[0].SUSPEND.disabled   = false;
        document.forms[0].ADMISSION.disabled = false;
        document.forms[0].MOVINGIN.disabled  = false;
        document.forms[0].REMOVE.disabled    = false;
        if (document.forms[0].TENSEKI_O !== undefined) {
            document.forms[0].TENSEKI_O.disabled = false;
        }
        if (document.forms[0].TENSEKI_I !== undefined) {
            document.forms[0].TENSEKI_I.disabled = false;
        }
        document.forms[0].TENKA_O.disabled   = false;
        document.forms[0].TENKA_I.disabled   = false;
    }
    return true;
}
