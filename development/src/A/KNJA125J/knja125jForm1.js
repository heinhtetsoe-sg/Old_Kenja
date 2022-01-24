function btn_submit(cmd) {
    if (document.forms[0].SCHREGNO.value == "") {
        alert("{rval MSG304}");
        return true;
    } else if (cmd == "clear") {
        if (!confirm("{rval MSG106}")) {
            return false;
        }
    } else if (cmd == "form2") {
        loadwindow("knja125jindex.php?cmd=form2", 0, 0, 750, 650);
        return true;
    } else if (cmd == "subform1") {
        if (document.forms[0].SEMES_CNT.value == 3) {
            loadwindow("knja125jindex.php?cmd=subform1", 0, 100, 750, 400);
        } else {
            loadwindow("knja125jindex.php?cmd=subform1", 0, 150, 750, 320);
        }
        return true;
    } else if (cmd == "subform2") {
        loadwindow("knja125jindex.php?cmd=subform2", 0, 150, 750, 280);
        return true;
    } else if (cmd == "subform3") {
        loadwindow("knja125jindex.php?cmd=subform3", 0, 150, 750, 280);
        return true;
    } else if (cmd == "subform4") {
        loadwindow("knja125jindex.php?cmd=subform4", 0, 150, 750, 500);
        return true;
    } else if (cmd == "syukketsu") {
        //出欠の記録参照
        loadwindow("knja125jindex.php?cmd=syukketsu", 0, 0, 600, 280);
        return true;
    } else if (cmd == "shokenlist1") {
        //既入力内容を参照（総合的な学習時間）
        loadwindow("knja125jindex.php?cmd=shokenlist1", 0, 0, 700, 350);
        return true;
    } else if (cmd == "shokenlist2") {
        //既入力内容を参照（総合所見）
        loadwindow("knja125jindex.php?cmd=shokenlist2", 0, 0, 700, 350);
        return true;
    } else if (cmd == "shokenlist3") {
        //既入力内容を参照（出欠の記録備考）
        loadwindow("knja125jindex.php?cmd=shokenlist3", 0, 0, 700, 350);
        return true;
    } else if (cmd == "shokenlist4") {
        //既入力内容を参照（道徳）
        loadwindow("knja125jindex.php?cmd=shokenlist4", 0, 0, 700, 350);
        return true;
    } else if (cmd == "act_doc") {
        //行動の記録参照
        if (!document.forms[0].SCHREGNO.value) {
            alert("{rval MSG304}");
            return;
        } else {
            loadwindow("knja125jindex.php?cmd=act_doc", 0, 0, 750, 450);
            return true;
        }
    } else if (cmd == "teikei") {
        loadwindow(
            "knja125jindex.php?cmd=teikei",
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
    } else if (cmd == "teikei_act") {
        loadwindow(
            "knja125jindex.php?cmd=teikei_act",
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
            "knja125jindex.php?cmd=teikei_val",
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

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//Submitしない
function btn_keypress() {
    //Enterキー
    if (event.keyCode == 13) {
        return false;
    }
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

//数値チェック
function CheckInteger(obj) {
    if (obj.value != "*") {
        to_Integer(obj);
    }
}
//数値チェック
function to_Integer(obj) {
    var checkString = obj.value;
    var newString = "";
    var count = 0;
    for (i = 0; i < checkString.length; i++) {
        ch = checkString.substring(i, i + 1);
        if (ch == ".") {
            newString += ch;
        } else if (ch >= "0" && ch <= "9") {
            newString += ch;
        }
    }
    if (checkString != newString) {
        alert("入力された値は不正な文字列です。\n数値を入力してください。\n入力された文字列は削除されます。");
        obj.value = "";
        return false;
    }
    return true;
}
