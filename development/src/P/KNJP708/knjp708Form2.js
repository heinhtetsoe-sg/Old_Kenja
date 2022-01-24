function btn_submit(cmd) {
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

function changeDisp(dispDiv, rowId) {
    if (dispDiv == "1") {
        document.getElementById("wariaiDisp" + rowId).style.display = "block";
        document.getElementById("zettaiDisp" + rowId).style.display = "none";
    } else if (dispDiv == "2") {
        document.getElementById("wariaiDisp" + rowId).style.display = "none";
        document.getElementById("zettaiDisp" + rowId).style.display = "block";
    }
}
