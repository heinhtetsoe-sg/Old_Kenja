function btn_submit(cmd) {
    if (cmd == 'update') {
        if (!confirm('{rval MSG101}')) {
            return false;
        }
    }
    var btns = document.getElementsByTagName("input");
    for (var i in btns) {
        if (btns[i].type == "button") {
            btns[i].disabled = true;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function close_window()
{
    alert('{rval MSG300}');
    closeWin();
}
