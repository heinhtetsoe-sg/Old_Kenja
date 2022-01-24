function btn_submit(cmd) {
    //科目チェック
    if (cmd == "search") {
        var subclasscd = document.forms[0].SUBCLASSCD.value;
        var kensuu = document.forms[0].KENSUU.value;
        if (subclasscd == "" || kensuu == "") {
            alert("科目・追加件数を指定してください。");
            return false;
        }
    }
    //データ指定チェック
    if (cmd == "update" || cmd == "delete") {
        var dataCnt = document.forms[0].DATA_CNT.value;
        if (dataCnt == 0) {
            alert("{rval MSG304}");
            return false;
        }
    }
    //削除対象チェック
    if (cmd == "delete") {
        var dataCnt = document.forms[0].DATA_CNT.value;
        var delCnt = 0;
        for (var i = 0; i < dataCnt; i++) {
            var DEL_FLG = "DEL_FLG-" + i;
            if (document.forms[0][DEL_FLG].checked) {
                delCnt++;
            }
        }
        if (delCnt == 0) {
            alert("削除対象が選択されていません。");
            return false;
        }
        if (!confirm("{rval MSG103}")) {
            return false;
        }
    }
    if (cmd == "reset") {
        if (!confirm("{rval MSG106}")) {
            return false;
        }
        document.forms[0].KENSUU.value = "";
    }
    if (cmd == "csv2") {
        if (!confirm("保存されたデータのみ処理します。よろしいですか？")) {
            return false;
        }
    }

    if (cmd == "change") {
        document.forms[0].KENSUU.value = "";
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//サブフォーム
function btn_submit_subform(cmd, counter) {
    //受講クラス選択ボタン押し下げ時
    if (cmd == "subform1") {
        param = document.forms[0]["GRADE_CLASS" + "-" + counter].value;
        grade_course = document.forms[0].GRADE_COURSE.value;
        loadwindow(
            "knjb0031index.php?cmd=subform1&param=" +
                param +
                "&counter=" +
                counter +
                "&grade_course=" +
                grade_course,
            0,
            0,
            350,
            400
        );
        return true;
    }
    //科目担任選択ボタン押し下げ時
    if (cmd == "subform2") {
        param = document.forms[0]["STAFFCD" + "-" + counter].value;
        param2 = document.forms[0]["STF_CHARGE" + "-" + counter].value;
        subclass = document.forms[0].SUBCLASSCD.value;
        loadwindow(
            "knjb0031index.php?cmd=subform2&param=" +
                param +
                "&param2=" +
                param2 +
                "&subclass=" +
                subclass +
                "&counter=" +
                counter,
            0,
            0,
            350,
            400
        );
        return true;
    }
    //使用施設選択ボタン押し下げ時
    if (cmd == "subform3") {
        param = document.forms[0]["FACCD" + "-" + counter].value;
        loadwindow(
            "knjb0031index.php?cmd=subform3&param=" +
                param +
                "&counter=" +
                counter,
            0,
            0,
            350,
            400
        );
        return true;
    }
    //教科書選択ボタン押し下げ時
    if (cmd == "subform4") {
        param = document.forms[0]["TEXTBOOKCD" + "-" + counter].value;
        loadwindow(
            "knjb0031index.php?cmd=subform4&param=" +
                param +
                "&counter=" +
                counter,
            0,
            0,
            350,
            400
        );
        return true;
    }
}
//権限チェック
function OnAuthError() {
    alert("{rval MSG300}");
    closeWin();
}
//スクロール
function scrollRC() {
    document.getElementById("trow").scrollLeft = document.getElementById(
        "tbody"
    ).scrollLeft;
    document.getElementById("tcol").scrollTop = document.getElementById(
        "tbody"
    ).scrollTop;
}
//子画面へ
function openKogamen(URL) {
    if (document.forms[0].SUBCLASSCD.value == "") {
        alert("科目を指定してください。");
        return;
    }
    var dataCnt = document.forms[0].DATA_CNT.value;
    if (dataCnt == 0) {
        alert("{rval MSG304}");
        return false;
    }
    if (!confirm("{rval MSG108}")) {
        return false;
    }

    document.location.href = URL;
    //    wopen(URL, 'SUBWIN', 0, 0, screen.availWidth, screen.availHeight);
}
