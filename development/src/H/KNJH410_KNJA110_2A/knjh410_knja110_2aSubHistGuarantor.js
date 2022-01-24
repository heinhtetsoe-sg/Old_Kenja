function btn_submit(cmd) {
    //削除
    if (cmd == 'histDel2' && !confirm('{rval MSG103}')) {
        return true;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//背景色変更
function chgBGColor(obj, id) {
    var bgcolor = document.getElementById(id);
    if (obj.checked) {
        bgcolor.style.background = "yellow";
    } else {
        bgcolor.style.background = "white";
    }
}
