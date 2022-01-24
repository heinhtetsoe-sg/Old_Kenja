function btn_submit(cmd) {
    if (document.forms[0].SCHREGNO.value == "") {
        alert("{rval MSG304}");
        return true;
    }

    if (cmd == "syuusyoku_clear") {
        if (!confirm("{rval MSG106}")) {
            return false;
        }
    }

    if (cmd == "pdf") {
        if (document.forms[0].SENKOU_NO.value == "") {
            alert("{rval MSG304}\n　　（求人番号）");
            return true;
        }
    }

    //進路相談ボタン押し下げ時
    if (cmd == "shinroSoudan") {
        loadwindow("knje360jindex.php?cmd=shinroSoudan&TYPE=btn", 0, 0, 760, 680);
        return true;
    }

    if (cmd == "syuusyoku_update" && document.forms[0].SEQ.value == "") {
        alert("{rval MSG308}");
        return true;
    }

    if (cmd == "syuusyoku_insert" || cmd == "syuusyoku_update") {
        if (document.forms[0].TOROKU_DATE.value == "") {
            alert("データを入力してください。\n　　（登録日）");
            return true;
        }

        var date = document.forms[0].TOROKU_DATE.value.split("/");
        var sdate = document.forms[0].SDATE.value.split("/");
        var edate = document.forms[0].EDATE.value.split("/");
        sdate_show = document.forms[0].SDATE.value;
        edate_show = document.forms[0].EDATE.value;

        if (
            new Date(eval(sdate[0]), eval(sdate[1]) - 1, eval(sdate[2])) > new Date(eval(date[0]), eval(date[1]) - 1, eval(date[2])) ||
            new Date(eval(edate[0]), eval(edate[1]) - 1, eval(edate[2])) < new Date(eval(date[0]), eval(date[1]) - 1, eval(date[2]))
        ) {
            alert("登録日が入力範囲外です。\n（" + sdate_show + "～" + edate_show + "）");
            return true;
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
            nextURL = parent.left_frame.document.links[idx].href.replace("edit", "syuusyoku"); //上記の結果
            break;
        }
    }
    document.forms[0].cmd.value = "syuusyoku_insert";
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
