function btn_submit(cmd) {
    if (document.forms[0].SCHREGNO.value == "") {
        alert("{rval MSG304}");
        return true;
    }
    if (cmd == "subform1_updatemain") {
        if (document.forms[0].RECORD_STAFFNAME.value == "") {
            alert("{rval MSG301}" + "\n(作成者)");
            return true;
        }
    }
    if (cmd == "subform1_copy") {
        if (document.forms[0].RECORD_HISTORY.value == document.forms[0].CTRL_DATE.value) {
            alert("{rval MSG203}" + "\n作成日付と元データの日付が同一の場合、処理できません。");
            return false;
        }

        var msg = "基本情報を " + document.forms[0].RECORD_HISTORY.value + "データを元に新規作成しますか？";
        msg += "\n（本日の日付が作成年月日となります。）";
        if (!confirm(msg)) {
            return false;
        }
    }

    if (cmd == "subform1_clear") {
        if (!confirm("{rval MSG106}")) {
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//更新後次の生徒のリンクをクリックする
function updateNextStudent(schregno, order) {
    if (document.forms[0].SCHREGNO.value == "") {
        alert("{rval MSG304}");
        return true;
    }
    nextURL = "";

    for (var i = 0; i < parent.left_frame.document.links.length; i++) {
        var search = parent.left_frame.document.links[i].search;
        //searchの中身を&で分割し配列にする。
        arr = search.split("&");

        //学籍番号が一致
        if (arr[1] == "SCHREGNO=" + schregno) {
            //昇順
            if (order == 0 && i == parent.left_frame.document.links.length - 1) {
                idx = 0; //更新後次の生徒へ(データが最後の生徒の時、最初の生徒へ)
            } else if (order == 0) {
                idx = i + 1; //更新後次の生徒へ
            } else if (order == 1 && i == 0) {
                idx = parent.left_frame.document.links.length - 1; //更新後前の生徒へ(データが最初の生徒の時)
            } else if (order == 1) {
                idx = i - 1; //更新後前の生徒へ
            }
            nextURL = parent.left_frame.document.links[idx].href.replace("edit", "subform1"); //上記の結果
            break;
        }
    }
    document.forms[0].cmd.value = "subform1_updatemain";
    //クッキー書き込み
    saveCookie("nextURL", nextURL);
    document.forms[0].submit();
    return false;
}

function NextStudent(cd) {
    var nextURL;
    nextURL = loadCookie("nextURL");
    if (nextURL) {
        if (cd == "0") {
            //クッキー削除
            deleteCookie("nextURL");
            document.location.replace(nextURL);
            alert("{rval MSG201}");
        } else if (cd == "1") {
            //クッキー削除
            deleteCookie("nextURL");
        }
    }
}

window.onload = function (e) {
    var keta = document.forms[0].useFinschoolcdFieldSize.value;
    if (keta == "12") {
        document.forms[0].P_SCHOOL_CD.maxlength = 12;
        document.forms[0].P_SCHOOL_CD.size = 12;
        document.forms[0].J_SCHOOL_CD.maxlength = 12;
        document.forms[0].J_SCHOOL_CD.size = 12;
    }
};

//Submitしない
function btn_keypress() {
    if (event.keyCode == 13) {
        event.keyCode = 0;
        window.returnValue = false;
    }
}

//学校検索で使用
function current_cursor(para) {
    sessionStorage.setItem("KNJE390MSubForm1_CurrentCursor", para);
}
function current_cursor_list() {
    if (sessionStorage.getItem("KNJE390MSubForm1_CurrentCursor") == "btn_kensaku") {
        document.getElementsByName("P_SCHOOL_CD")[0].focus();
        // remove item
        sessionStorage.removeItem("P_SCHOOL_CD");
    } else {
        document.getElementsByName("J_SCHOOL_CD")[0].focus();
        // remove item
        sessionStorage.removeItem("J_SCHOOL_CD");
    }
}
