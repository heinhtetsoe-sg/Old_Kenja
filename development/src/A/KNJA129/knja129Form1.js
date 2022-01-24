var textRange;
function btn_submit(cmd) {
    if (document.forms[0].SCHREGNO.value == "") {
        alert("{rval MSG304}");
        return true;
    } else if (cmd == "clear") {
        if (!confirm("{rval MSG106}")) {
            return false;
        }
    } else if (cmd == "update") {
        parent.left_frame.document.forms[0].changeFlg.value = "";
    }
    //更新中の画面ロック(全フレーム)
    //フレームロック機能（プロパティの値が1の時有効）
    if (document.forms[0].useFrameLock && document.forms[0].useFrameLock.value == "1") {
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

function toIntegerKnja129(value) {
    var hankaku = "";
    var c, idx;
    for (i = 0; i < value.length; i++) {
        c = value.charAt(i);
        idx = "０１２３４５６７８９".indexOf(c);
        if (0 <= idx) {
            c = String.fromCharCode(idx + "0".charCodeAt(0));
        }
        hankaku += c;
    }
    value = hankaku;
    return toInteger(value);
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

function newwin(SERVLET_URL) {
    if (document.forms[0].SCHREGNO.value == "") {
        alert("{rval MSG304}");
        return true;
    }
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
