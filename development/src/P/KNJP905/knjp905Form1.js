function btn_submit(cmd) {
    if (cmd == "clear") {
        if (!confirm("{rval MSG106}")) return false;
    }
    if (cmd == "search") {
        document.getElementById("marq_msg").innerHTML = "検索しています...しばらくおまちください";
        document.getElementById("marq_msg").style.color = "#FF0000";
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//同クラスのチェックボックスの排他制御
function keepExclusiveChkGroup(obj) {
    if (obj.checked) {
        var className = obj.className;
        var elemList = document.querySelectorAll("input[type='checkbox']." + className + "");
        for (var i = 0; i < elemList.length; i++) {
            elemList[i].checked = false;
        }
        obj.checked = true;
    }
}
