function btn_submit(cmd) {
    if (cmd == "update2" || cmd == "reset2") {
        document.forms[0].cmd.value = cmd;
        document.forms[0].submit();
        return false;
    }

    top.main_frame.closeit();
    top.main_frame.document.forms[0].cmd.value = "";
    top.main_frame.document.forms[0].submit();
    return false;
}
function setcmd() {
    document.forms[0].cmd.value = "";
    document.forms[0].submit();
    return false;
}
function to_Integer(obj) {
    var checkString = obj.value;
    var newString = "";
    var count = 0;

    for (i = 0; i < checkString.length; i++) {
        ch = checkString.substring(i, i + 1);
        if (ch >= "0" && ch <= "9") {
            newString += ch;
        }
    }
    if (checkString != newString) {
        alert("入力された値は不正な文字列です。\n数値を入力してください。");
        obj.value = "";
        return false;
    }

    switch (obj.name) {
        case "BEDTIME":
        case "RISINGTIME":
            if ((1 > obj.value || obj.value > 24) && obj.value != "") {
                alert("1から24の値を入力してください。");
                obj.value = "";
                return false;
            }
    }

    return true;
}
window.onload = function () {
    for (var i = 0; i < document.forms[0].elements.length; i++) {
        var tagName = document.forms[0].elements[i].tagName;
        var tagType = document.forms[0].elements[i].type;
        if ((tagName == "INPUT" && tagType != "button") || tagName == "SELECT" || tagName == "TEXTAREA") {
            document.forms[0].elements[i].disabled = true;
            document.forms[0].elements[i].style.backgroundColor = "#FFFFFF";
        }
    }
};
