var textRange;
function btn_submit(cmd) {
    if (document.forms[0].SCHREGNO.value == "") {
        alert("{rval MSG304}");
        return true;
    } else if (cmd == "clear") {
        if (!confirm("{rval MSG106}")) {
            return false;
        }
    } else if (cmd == "subform1") {
        loadwindow("knja120aindex.php?cmd=subform1", 0, 0, 750, 450);
        return true;
    } else if (cmd == "subform4") {
        loadwindow("knja120aindex.php?cmd=subform4", 0, 0, 700, 550);
        return true;
    } else if (cmd == "execute") {
        document.forms[0].encoding = "multipart/form-data";
    } else if (cmd == "update") {
        parent.left_frame.document.forms[0].changeFlg.value = "";
    } else if (cmd == "tyousasyoSelect") {
        loadwindow(
            "knja120aindex.php?cmd=tyousasyoSelect",
            event.clientX +
                (function () {
                    var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;
                    return scrollX;
                })(),
            event.clientY +
                (function () {
                    var scrollY = document.documentElement.scrollTop || document.body.scrollTop;
                    return scrollY;
                })(),
            650,
            450
        );
        return true;
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

function setDataChangeFlg() {
    parent.left_frame.document[0].changeFlg.value = 1;
}

//更新後次の生徒のリンクをクリックする
function updateNextStudent(schregno, order) {
    if (document.forms[0].SCHREGNO.value == "") {
        return true;
    }
    order = parent.left_frame.document.forms[0].setOrder.value;
    if (order == "") {
        return false;
    }
    parent.left_frame.document.forms[0].changeFlg.value = "";

    var linkCnt = parent.left_frame.document.forms[0]["linkCnt" + document.forms[0].SCHREGNO.value].value;

    //昇順
    if (order == "next" && linkCnt == parent.left_frame.document.links.length - 1) {
        idx = linkCnt; //更新後次の生徒へ(データが最後の生徒の時、最初の生徒へ)
    } else if (order == "next") {
        idx = parseInt(linkCnt) + 1; //更新後次の生徒へ
    } else if (order == "pre" && linkCnt == 0) {
        idx = linkCnt; //更新後前の生徒へ(データが最初の生徒の時)
    } else if (order == "pre") {
        idx = parseInt(linkCnt) - 1; //更新後前の生徒へ
    }
    parent.left_frame.document.links[idx].click(); //上記の結果
    parent.left_frame.document.forms[0].setOrder.value = "";

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

function newwin(SERVLET_URL, GRADE) {
    if (document.forms[0].SCHREGNO.value == "") {
        alert("{rval MSG304}");
        return true;
    }
    var gradehrclass = GRADE.split("-");
    document.forms[0].GRADE_HR_CLASS.value = gradehrclass[0] + gradehrclass[1];
    action = document.forms[0].action;
    target = document.forms[0].target;

    //    url = location.hostname;
    //    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL + "/KNJA";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

function Page_jumper(link) {
    if (document.forms[0].SCHREGNO.value == "") {
        alert("{rval MSG304}");
        return;
    }
    if (!confirm("{rval MSG108}")) {
        return;
    }
    parent.location.href = link;
}
window.onload = function () {
    for (var i = 0; i < document.forms[0].elements.length; i++) {
        var tagName = document.forms[0].elements[i].tagName;
        var tagType = document.forms[0].elements[i].type;
        var name = document.forms[0].elements[i].name;
        if ((tagName == "INPUT" && tagType != "button") || (tagName == "SELECT" && name != 'YEAR') || tagName == "TEXTAREA") {
            document.forms[0].elements[i].disabled = true;
            document.forms[0].elements[i].style.backgroundColor = "#FFFFFF";
        }
    }
};
