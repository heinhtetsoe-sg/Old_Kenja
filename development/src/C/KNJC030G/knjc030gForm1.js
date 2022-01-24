function btn_submit(cmd) {
    if (document.forms[0].SCHREGNO.value == "") {
        alert("{rval MSG304}");
        return true;
    } else if (cmd == "clear") {
        if (!confirm("{rval MSG106}")) {
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function rowSyukketu(dayIdx) {
    var idx = document.getElementsByName("row_syukketu_" + dayIdx)[0].selectedIndex;
    for (var i = 0; i <= document.forms[0].MAXPERI.value; i++) {
        if (document.forms[0].elements["syukketu_" + dayIdx + "_" + i]) {
            document.forms[0].elements["syukketu_" + dayIdx + "_" + i].selectedIndex = idx;
        }
    }
    document.getElementsByName("row_syukketu_" + dayIdx)[0].value = "";
}
window.onload = function () {
    document.getElementById("cframe").onload = function () {
        var list = document.getElementById("cframe").contentDocument.querySelectorAll("tt a");
        for (var i = 0; i < list.length; i++) {
            list[i].onclick = current_cursor_focus2;
        }
    };
};
function current_cursor_focus2() {
    setTimeout(function () {
        btn_submit("edit");
    }, 100);
}
function ViewcdMousein(e, idx) {
    var msg = "";
    if (document.getElementById("MENU_" + idx)) {
        msg = document.getElementById("MENU_" + idx).innerHTML;
    }
    if (msg == "") {
        return;
    }

    x = event.clientX + document.body.scrollLeft;
    y = event.clientY + document.body.scrollTop;
    document.getElementById("lay").innerHTML = msg;
    document.getElementById("lay").style.position = "absolute";
    document.getElementById("lay").style.left = x + 5;
    document.getElementById("lay").style.top = y + 10;
    document.getElementById("lay").style.visibility = "visible";
    document.getElementById("lay").style.background = "#ccffff";
}

function ViewcdMouseout() {
    document.getElementById("lay").style.visibility = "hidden";
}
