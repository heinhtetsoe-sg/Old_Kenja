function btn_submit(cmd) {
    if (cmd == "delete") {
        if (!confirm("{rval MSG103}")) return false;
    }
    if (cmd == "reset") {
        if (!confirm("{rval MSG106}")) return false;
    }
    if (cmd == "update") {
        if (document.forms[0].JOBTYPE_SCD.value == "") {
            alert("{rval MSG301}");
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function OnAuthError() {
    alert("{rval MSG300}");
    closeWin();
}

function check_cd(obj) {
    //小分類の入力チェック
    if (obj.name == "JOBTYPE_SCD") {
        if (obj.value.match(/[^0-9]/)) {
            alert("半角数字3文字で入力してください。");
            obj.value = "";
            return false;
        }
        if (obj.value.length > 3) {
            alert("半角数字3文字で入力してください。");
            obj.value = "";
            return false;
        }
    }
    //細分類の入力チェック
    if (obj.name == "JOBTYPE_SSCD") {
        if (obj.value.match(/[^0-9]/)) {
            alert("半角数字2文字で入力してください。");
            obj.value = "";
            return false;
        }
        if (obj.value.length > 2) {
            alert("半角数字2文字で入力してください。");
            obj.value = "";
            return false;
        }
    }
}
