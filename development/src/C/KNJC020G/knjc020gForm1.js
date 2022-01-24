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
window.onload = function () {
    document.getElementById("box1").style.width = parseInt(window.innerWidth * 0.35);
    document.getElementById("box2").style.width = parseInt(window.innerWidth * 0.65);
};
function syukketu(idx) {
    var schVal = document.forms[0].schregnos.value;
    var schValList = schVal == "" ? [] : schVal.split(",");
    for (var i = 0; i < schValList.length; i++) {
        document.getElementsByName("syukketu_" + idx + "_" + i)[0].value = "SHUSSEKI";
    }
    document.getElementById("UPDATE_CHECK_" + idx).checked = true;
}
function syukketu2(idx) {
    document.getElementById("UPDATE_CHECK_" + idx).checked = true;
}
function ViewcdMousein(e, idx) {
    var msg = document.getElementsByName("TIP_" + idx)[0].value;
    if (msg == "") {
        return;
    }

    x = event.clientX + document.body.scrollLeft;
    y = event.clientY + document.body.scrollTop;
    document.getElementById("lay").innerHTML = msg;
    document.getElementById("lay").style.position = "absolute";
    document.getElementById("lay").style.left = x + 5;
    document.getElementById("lay").style.top = y + 10;
    document.getElementById("lay").style.padding = "4px 3px 3px 8px";
    document.getElementById("lay").style.border = "1px solid";
    document.getElementById("lay").style.visibility = "visible";
    document.getElementById("lay").style.background = "#ccffff";
}

function ViewcdMouseout() {
    document.getElementById("lay").style.visibility = "hidden";
}
