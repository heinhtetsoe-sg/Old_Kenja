/*
 * kanji=Š¿Žš
 * <?php # $Id: knjz420Form1.js 56591 2017-10-22 13:04:39Z maeshiro $ ?>
 */

function btn_submit(cmd) {
    if (cmd == "delete") {
        result = confirm('{rval MSG103}');
        if (result == false) {
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function Btn_reset(cmd) {
    result = confirm('{rval MSG106}');
    if (result == false) {
        return false;
    }
}
