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
        loadwindow("knja120eindex.php?cmd=subform1", 0, 0, 750, 450);
        return true;
    } else if (cmd == "subformSeisekiSansho") {
        loadwindow("knja120eindex.php?cmd=subformSeisekiSansho", 0, 0, 700, 550);
        return true;
    } else if (cmd == "act_doc") {
        //行動の記録参照
        loadwindow("knja120eindex.php?cmd=act_doc", 0, 0, 750, 450);
        return true;
    } else if (cmd == "teikei_act") {
        loadwindow(
            "knja120eindex.php?cmd=teikei_act",
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
    } else if (cmd == "teikei_val") {
        loadwindow(
            "knja120eindex.php?cmd=teikei_val",
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
    } else if (cmd == "execute") {
        document.forms[0].encoding = "multipart/form-data";
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
            nextURL = parent.left_frame.document.links[idx].href; //上記の結果
            break;
        }
    }
    document.forms[0].cmd.value = "update";
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

//備考チェックボックス
function CheckRemark(tgtid, chkid) {
    var tgt = document.getElementById(tgtid);
    var chk = document.getElementById(chkid);
    if (chk.checked == true) {
        tgt.value = document.forms[0].NO_COMMENTS_LABEL.value;
        tgt.disabled = true;
    } else {
        tgt.disabled = false;
    }
}

function setTextFieldName(fieldName) {
    document.forms[0].TEXTNAME.value = fieldName;
    document.forms[0].TRAINREF_TARGET.value = fieldName;
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
