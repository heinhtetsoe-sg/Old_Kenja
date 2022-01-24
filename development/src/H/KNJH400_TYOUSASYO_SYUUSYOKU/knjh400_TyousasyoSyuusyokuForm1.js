function btn_submit(cmd) {
    if (document.forms[0].SCHREGNO.value == "") {
        alert("{rval MSG304}");
        return true;
    }
    if (cmd == "modify") {
        //出欠の記録修正
        var url = "../../X/KNJXATTEND/index.php?SCHREGNO=" + document.forms[0].SCHREGNO.value;
        url += "&mode=" + document.forms[0].mode.value;
        url += "&GRD_YEAR=" + document.forms[0].GRD_YEAR.value;
        url += "&PROGRAMID=" + document.forms[0].PROGRAMID.value;

        loadwindow(url, 0, 0, 470, 480);
        return true;
    } else if (cmd == "subform1") {
        //成績参照
        loadwindow("knje020index.php?cmd=subform1", 0, 0, 700, 550);
        return true;
    } else if (cmd == "reset") {
        //取り消し
        if (!confirm("{rval MSG106}")) {
            return false;
        }
    } else if (cmd == "copy_pattern") {
        //左のパターンのデータをコピー
        if (document.forms[0].REFER_PATTERN.value == "") {
            alert("{rval MSG310}" + "\n（ 参照パターン ）");
            return true;
        }

        if (document.forms[0].REFER_PATTERN.value == document.forms[0].SELECT_PATTERN.value) {
            alert("参照パターンと対象パターンで同一のパターンが選択されています。");
            return true;
        }

        if (!confirm("{rval MSG101}")) {
            return false;
        }
    }
    //更新中の画面ロック(全フレーム)
    //フレームロック機能（プロパティの値が1の時有効）
    if (document.forms[0].useFrameLock.value == "1") {
        if (cmd == "update") {
            updateFrameLocks();
        }
    }
    if (cmd == "update") {
        //更新ボタン・・・読み込み中は、更新ボタンをグレー（押せないよう）にする。
        document.forms[0].btn_update.disabled = true;
        document.forms[0].btn_up_next.disabled = true;
        document.forms[0].btn_up_pre.disabled = true;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function cmb_submit(obj, cmd) {
    if (document.forms[0].SCHREGNO.value == "") {
        alert("{rval MSG304}");
        obj.selectedIndex = 0;
        return true;
    }

    btn_submit(cmd);
}

//権限チェック
function OnAuthError() {
    alert("{rval MSG300}"); //この処理は許可されていません。
    closeWin();
}

//チェックボックス
function CheckHealthRemark() {
    if (document.forms[0].CHECK.checked == true) {
        document.forms[0].jobhunt_healthremark.value = document.forms[0].REMARK_VALUE.value;
        document.forms[0].jobhunt_healthremark.disabled = true;
    } else {
        document.forms[0].jobhunt_healthremark.disabled = false;
    }
}

function wopen2(URL, winName, x, y, w, h) {
    var newWin;
    var para =
        "" +
        " left=" +
        x +
        ",screenX=" +
        x +
        ",top=" +
        y +
        ",screenY=" +
        y +
        ",toolbar=" +
        0 +
        ",location=" +
        0 +
        ",directories=" +
        0 +
        ",status=" +
        0 +
        ",menubar=" +
        0 +
        ",scrollbars=" +
        1 + //------------------------後でゼロに修正
        ",resizable=" +
        1 + //-------------------後でゼロに修正
        ",innerWidth=" +
        w +
        ",innerHeight=" +
        h +
        ",width=" +
        w +
        ",height=" +
        h;

    if (sbwin_closed(newWin)) {
        newWin = window.open(URL, winName, para);
    } else {
        newWin.location.href = URL;
    }
    newWin.focus();
}

function newwin(SERVLET_URL) {
    if (document.forms[0].SCHREGNO.value == "") {
        alert("{rval MSG304}");
        return true;
    }
    var gradehrclass = parent.left_frame.document.forms[0].GRADE.value.split("-");
    parent.right_frame.document.forms[0].GRADE_HR_CLASS.value = gradehrclass[0] + gradehrclass[1];
    action = document.forms[0].action;
    target = document.forms[0].target;

    //    url = location.hostname;
    //    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL + "/KNJE";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
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
