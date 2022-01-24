var textRange;
function btn_submit(cmd) {
    if (document.forms[0].SCHREGNO.value == "") {
        alert('{rval MSG304}');
        return true;
    } else if (cmd == 'clear') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    } 

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function chksetdate() {
    if (document.forms[0].USEKNJD425NDISPUPDDATE.value == "1") {
        if (document.forms[0].UPDDATE.value == "" || (document.forms[0].UPDDATE.value == "9999/99/99" && document.forms[0].SELNEWDATE.value == "")) {
            alert('{rval MSG304}');
            return false;
        }
    }
    return true;
}
