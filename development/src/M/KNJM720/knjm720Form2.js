function btn_submit(cmd) {

    if (cmd == "update") {
        if (document.forms[0].PATTERN_CD.value == ""){
            alert('{rval MSG301}\n　　（履修パターンコード）');
            return true;
        }
        if (document.forms[0].PATTERN_NAME.value == ""){
            alert('{rval MSG301}\n　　（履修パターン名称）');
            return true;
        }
    }

    if (cmd == "delete") {
        result = confirm('{rval MSG103}');
        if (result == false) {
            return false;
        }
    }
    if (cmd == "reset") {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

