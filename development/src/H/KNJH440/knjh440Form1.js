function btn_submit(cmd) {
    if (cmd == 'copy' && !confirm('{rval MSG101}')){
        return false;
    }
    if (cmd == 'clear'){
        if (!confirm('{rval MSG106}'))
            return false;
        else
            cmd = "";
    }
    //模試
    if (cmd == 'update'){
        var leftList = document.forms[0].LEFT_MOCK;
        var rightList = document.forms[0].RIGHT_MOCK;
        var attribute = document.forms[0].selectdataMock;
        attribute.value = "";
        if (leftList.length == 0 && rightList.length == 0) {
            alert('{rval MSG916}');
            return false;
        }
        sep = "";
        for (var i = 0; i < leftList.length; i++) {
            attribute.value = attribute.value + sep + leftList.options[i].value;
            sep = ",";
        }
    }
    //科目
    if (cmd == 'update2'){
        var leftList = document.forms[0].LEFT_SUB;
        var rightList = document.forms[0].RIGHT_SUB;
        var attribute = document.forms[0].selectdataSub;
        attribute.value = "";
        if (leftList.length == 0 && rightList.length == 0) {
            alert('{rval MSG916}');
            return false;
        }
        sep = "";
        for (var i = 0; i < leftList.length; i++) {
            attribute.value = attribute.value + sep + leftList.options[i].value;
            sep = ",";
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();
}
