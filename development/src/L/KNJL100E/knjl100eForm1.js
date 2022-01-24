function btn_submit(cmd) {

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function cmbChgChkDisabled(obj)
{
    var cmb = document.forms[0].TESTDIV;
    var chk = document.getElementById('CHECK1');
    if (cmb.value == "1") {
        chk.disabled = false;
    }else{
        chk.disabled = true;
    }
    chk.checked = false;
    return;
}
