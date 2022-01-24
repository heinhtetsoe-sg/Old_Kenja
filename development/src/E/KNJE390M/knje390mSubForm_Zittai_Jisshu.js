function btn_submit(cmd) {
    if (cmd == "subformZittaiJisshu_delete" && !confirm("{rval MSG103}")) {
        return true;
    }

    if (cmd == "subformZittaiJisshu_insert" || cmd == "subformZittaiJisshu_update") {
        var start_date = document.forms[0].START_DATE.value.split("/").join("-");
        var finish_date = document.forms[0].FINISH_DATE.value.split("/").join("-");
        console.log(start_date);
        //日付チェック
        if (start_date == "") {
            alert("{rval MSG301}" + "(開始日付)");
            return false;
        }
        if (start_date > finish_date) {
            alert("{rval MSG901}" + "\n(入力した日付間の前後関係に誤りがあります。）");
            return false;
        }

        var startDateList = document.forms[0].startDateList.value.split(",");
        var existsFlg = false;
        for (var i = 0; i < startDateList.length; i++) {
            console.log(i + "番目:" + startDateList[i]);
            if (start_date == startDateList[i]) {
                existsFlg = true;
            }
        }
        if (cmd == "subformZittaiJisshu_insert" && existsFlg) {
            alert("{rval MSG917}" + "\n入力された開始日付のデータは既に存在します。");
            return false;
        } else if (cmd == "subformZittaiJisshu_update" && !existsFlg) {
            alert("{rval MSG917}" + "\n入力された開始日付のデータが存在しません。");
            return false;
        }
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
