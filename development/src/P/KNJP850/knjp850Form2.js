function btn_submit(cmd) {
    if (cmd == "delete" && !confirm("{rval MSG103}")) {
        return true;
    }
    if (cmd == "csv") {
        var schregno = document.forms[0].SCHREGNO.value;
        if (schregno == "") {
            alert("{rval MSG304}\n(左より生徒を選択してから行ってください)");
            return false;
        }
    } else if (cmd == "csv2") {
        var nodeList = parent.left_frame.document.querySelectorAll("td.koumoku2_schreg");
        var tmp = "";
        var sep = "";
        for (var i = 0; i < nodeList.length; i++) {
            tmp += sep + nodeList[i].textContent;
            sep = ",";
        }
        document.forms[0].IKKATU_LIST.value = tmp;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function Btn_reset(cmd) {
    result = confirm("{rval MSG107}");
    if (result == false) {
        return false;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function Page_jumper(link) {
    if (document.forms[0].SCHREGNO.value == "") {
        alert("{rval MSG308}");
        return;
    }
    if (!confirm("{rval MSG108}")) {
        return;
    }
    parent.location.href = link;
}

//Submitしない
function btn_keypress() {
    if (event.keyCode == 13) {
        event.keyCode = 0;
        window.returnValue = false;
    }
}
