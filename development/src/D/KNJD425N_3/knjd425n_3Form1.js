function btn_submit(cmd) {

    if (document.forms[0].SCHREGNO.value == ""){
        alert('{rval MSG304}');
        return true;
    } else if (cmd == 'delete') {
        if (!confirm('{rval MSG103}')) {
            return false;
        }
    } else if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    } else if (cmd == 'listdelete') {
        var checkLine = "";
        var sep = "";
        var checkList = document.forms[0].DEL_CHECK;
        if (checkList) {
            if (checkList.length) {
                for (let i = 0; i < checkList.length; i++) {
                    var check = checkList[i];
                    if (check.checked) {
                        checkLine += sep + check.value;
                        sep = ",";
                    }
                }
            } else {
                //1行しかない場合
                if (checkList.checked) {
                    checkLine += sep + checkList.value;
                }
            }
        }
        //チェックされている行を取得
        document.forms[0].DEL_LIST.value = checkLine;

        if (checkLine == "") {
            alert('{rval MSG304}');
            return true;
        }
        if (!confirm('{rval MSG103}')) {
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
