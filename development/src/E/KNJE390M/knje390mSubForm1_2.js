function btn_submit(cmd) {
    if (cmd == "educate1_delete" && !confirm("{rval MSG103}")) {
        return true;
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

window.onload = function (e) {
    var keta = document.forms[0].useFinschoolcdFieldSize.value;
    if (keta == "12" && document.forms[0].P_J_SCHOOL_CD) {
        document.forms[0].P_J_SCHOOL_CD.maxlength = 12;
        document.forms[0].P_J_SCHOOL_CD.size = 12;
    }
};

//学校検索で使用
function current_cursor(para) {
    sessionStorage.setItem("KNJE390MSubForm1_CurrentCursor", para);
}
function current_cursor_list() {
    if (sessionStorage.getItem("KNJE390MSubForm1_CurrentCursor") == "btn_kensaku") {
        document.getElementsByName("P_J_SCHOOL_CD")[0].focus();
        // remove item
        sessionStorage.removeItem("P_J_SCHOOL_CD");
    }
}
