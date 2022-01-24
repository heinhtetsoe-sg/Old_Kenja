function btn_submit(cmd) {
    //update/deleteは、選択していなければ、NG
    if (document.forms[0].RECORD_DIV.value == "" || document.forms[0].RECORD_SEQ.value == "") {
        if (
            cmd == "healthcare1_update" ||
            cmd == "healthcare1_update_care" ||
            cmd == "healthcare1_update_spasm" ||
            cmd == "healthcare1_delete" ||
            cmd == "healthcare1_delete_care" ||
            cmd == "healthcare1_delete_spasm"
        ) {
            alert("{rval MSG304}");
            return true;
        }
    }

    //更新チェック
    if (cmd == "healthcare1_update" || cmd == "healthcare1_delete") {
        if (document.forms[0].RECORD_DIV.value != "1") {
            alert("{rval MSG308}");
            return true;
        }
    }
    if (cmd == "healthcare1_update_care" || cmd == "healthcare1_delete_care") {
        if (document.forms[0].RECORD_DIV.value != "2") {
            alert("{rval MSG308}");
            return true;
        }
    }
    if (cmd == "healthcare1_update_spasm" || cmd == "healthcare1_delete_spasm") {
        if (document.forms[0].RECORD_DIV.value != "4") {
            alert("{rval MSG308}");
            return true;
        }
    }
    //削除確認
    if (cmd == "healthcare1_delete" || cmd == "healthcare1_delete_care" || cmd == "healthcare1_delete_spasm") {
        if (!confirm("{rval MSG103}")) {
            return true;
        }
    }

    if (cmd == "healthcare1_insert" || cmd == "healthcare1_update") {
        document.forms[0].RECORD_DIV.value = "1";
    } else if (cmd == "healthcare1_insert_care" || cmd == "healthcare1_update_care") {
        document.forms[0].RECORD_DIV.value = "2";
    } else if (cmd == "healthcare1_update_allergen") {
        document.forms[0].RECORD_DIV.value = "3";
    } else if (cmd == "healthcare1_insert_spasm" || cmd == "healthcare1_update_spasm") {
        document.forms[0].RECORD_DIV.value = "4";
    } else if (cmd == "healthcare1_update_consid") {
        document.forms[0].RECORD_DIV.value = "5";
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function ShowConfirm() {
    if (!confirm("{rval MSG106}")) {
        return false;
    }
}

//Submitしない
function btn_keypress() {
    if (event.keyCode == 13) {
        event.keyCode = 0;
        window.returnValue = false;
    }
}
