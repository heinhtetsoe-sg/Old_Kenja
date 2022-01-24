function btn_submit(cmd) {
    if (cmd == "clear") {
        if (!confirm("{rval MSG106}")) {
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function boxClick(obj, i, j) {
    var notuseVal = document.forms[0].notuse.value;
    var notuseValList = notuseVal == "" ? [] : notuseVal.split(",");
    if (notuseValList.indexOf(i + "*" + j) == -1) {
        obj.style.backgroundColor = "#CCCCCC";
        notuseValList.push(i + "*" + j);
        document.forms[0].notuse.value = notuseValList.join(",");
    } else {
        obj.style.backgroundColor = "#FFFFFF";
        notuseValList.splice(notuseValList.indexOf(i + "*" + j), 1);
        document.forms[0].notuse.value = notuseValList.join(",");
    }
}
