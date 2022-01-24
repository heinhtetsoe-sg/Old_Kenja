function btn_submit(cmd) {
    document.forms[0].encoding = "multipart/form-data";
    if (cmd != "changeKind") {
        if (document.forms[0].year.value == "") {
            alert("{rval MSG304}" + " （対象年度）");
            return false;
        }
    }
    if (cmd == "update") {
        if (document.forms[0].GVAL_CALC.length == 1) {
            alert("{rval MSG305}" + "\n『評定計算方法』。\n名称マスタメンテにて設定して下さい。");
            return false;
        }
        if (document.forms[0].ABSENT_COV.value == 5) {
            var absent_cov_late = document.forms[0].ABSENT_COV_LATE.value;
            var amari_kuriage = document.forms[0].AMARI_KURIAGE.value;
            if (absent_cov_late == "" && amari_kuriage != "") {
                alert("{rval MSG301}\n欠課数換算");
                return false;
            }
        }
    }
    //CSV
    if (cmd == "exec") {
        if (document.forms[0].OUTPUT[1].checked && document.forms[0].FILE.value == "") {
            alert("ファイルを指定してください");
            return false;
        }

        if (document.forms[0].OUTPUT[0].checked) {
            cmd = "downloadHead";
        } else if (document.forms[0].OUTPUT[1].checked) {
            cmd = "uploadCsv";
        } else if (document.forms[0].OUTPUT[2].checked) {
            cmd = "downloadCsv";
        } else if (document.forms[0].OUTPUT[3].checked) {
            cmd = "downloadError";
        } else {
            alert("ラジオボタンを選択してください。");
            return false;
        }
    }

    if (cmd == "update" && document.forms[0].dataBaseinfo.value == "2") {
        if (document.forms[0].KYOUIKU_IINKAI_SCHOOLCD.value == "") {
            alert("{rval MSG301}" + "\n（教育委員会統計用学校番号）");
            return false;
        }
    }

    //更新時エラーチェック(特活欠課数換算より下の項目)
    if (cmd == "update") {
        var labelMap = new Object(); //エラー時に出力する項目名
        labelMap["JITU_JIFUN"] = "授業時間(分)";
        labelMap["JITU_JIFUN_SPECIAL"] = "特活授業時間(分)";
        labelMap["JITU_SYUSU"] = "授業週数";
        labelMap["RISYU_BUNSI"] = "履修上限値";
        labelMap["RISYU_BUNBO"] = "履修上限値";
        labelMap["SYUTOKU_BUNSI"] = "修得上限値";
        labelMap["SYUTOKU_BUNBO"] = "修得上限値";
        labelMap["RISYU_BUNSI_SPECIAL"] = "特活上限値";
        labelMap["RISYU_BUNBO_SPECIAL"] = "特活上限値";
        labelMap["PREF_CD"] = "都道府県";
        labelMap["KESSEKI_OUT_BUNSI"] = "欠席日数超過";
        labelMap["KESSEKI_OUT_BUNBO"] = "欠席日数超過";

        var nyuryoku_flg = false;
        if (document.forms[0].JUGYOU_JISU_FLG && document.forms[0].JUGYOU_JISU_FLG.value == "") {
            for (var i = 0; i < document.forms[0].length; i++) {
                targetObj = document.forms[0][i];
                if (targetObj.name.match(/(RISYU|SYUTOKU)_(BUNSI|BUNBO)/)) {
                    if (targetObj.value.length > 0) {
                        nyuryoku_flg = true;
                    }
                }
                if (targetObj.name.match(/JITU_(JIFUN|SYUSU)/)) {
                    if (!targetObj.value) {
                        alert("{rval MSG301}" + "\n（" + labelMap[targetObj.name] + "）");
                        return false;
                    }
                }
            }
        } else {
            for (var i = 0; i < document.forms[0].length; i++) {
                targetObj = document.forms[0][i];
                if (targetObj.name.match(/(RISYU_BUNSI|RISYU_BUNBO|SYUTOKU_BUNSI|SYUTOKU_BUNBO|RISYU_BUNSI_SPECIAL|RISYU_BUNBO_SPECIAL|JITU_JIFUN|JITU_JIFUN_SPECIAL|JITU_SYUSU|PREF_CD)/)) {
                    if (!targetObj.value) {
                        alert("{rval MSG301}" + "\n（" + labelMap[targetObj.name] + "）");
                        return false;
                    }
                }
            }
        }

        for (var i = 0; i < document.forms[0].length; i++) {
            targetObj = document.forms[0][i];
            if (
                targetObj.name.match(
                    /(RISYU_BUNSI|RISYU_BUNBO|SYUTOKU_BUNSI|SYUTOKU_BUNBO|RISYU_BUNSI_SPECIAL|RISYU_BUNBO_SPECIAL|JITU_JIFUN|JITU_JIFUN_SPECIAL|JITU_SYUSU|PREF_CD|KESSEKI_OUT_BUNSI|KESSEKI_OUT_BUNBO)/
                )
            ) {
                if (targetObj.value == "0") {
                    alert("{rval MSG901}\n0は入力できません。" + " （" + labelMap[targetObj.name] + "）");
                    return false;
                }
            }
        }

        var risyu_bunsi = document.forms[0].RISYU_BUNSI ? document.forms[0].RISYU_BUNSI : "";
        var risyu_bunbo = document.forms[0].RISYU_BUNBO ? document.forms[0].RISYU_BUNBO : "";
        var syutoku_bunsi = document.forms[0].SYUTOKU_BUNSI ? document.forms[0].SYUTOKU_BUNSI : "";
        var syutoku_bunbo = document.forms[0].SYUTOKU_BUNBO ? document.forms[0].SYUTOKU_BUNBO : "";

        var risyu_bunsi_special = document.forms[0].RISYU_BUNSI_SPECIAL ? document.forms[0].RISYU_BUNSI_SPECIAL : "";
        var risyu_bunbo_special = document.forms[0].RISYU_BUNBO_SPECIAL ? document.forms[0].RISYU_BUNBO_SPECIAL : "";

        var kesseki_warn_bunsi = document.forms[0].KESSEKI_WARN_BUNSI ? document.forms[0].KESSEKI_WARN_BUNSI : "";
        var kesseki_warn_bunbo = document.forms[0].KESSEKI_WARN_BUNBO ? document.forms[0].KESSEKI_WARN_BUNBO : "";
        var kesseki_out_bunsi = document.forms[0].KESSEKI_OUT_BUNSI ? document.forms[0].KESSEKI_OUT_BUNSI : "";
        var kesseki_out_bunbo = document.forms[0].KESSEKI_OUT_BUNBO ? document.forms[0].KESSEKI_OUT_BUNBO : "";
        var ruikeiheikin_bunsi = document.forms[0].RUIKEIHEIKIN_BUNSI ? document.forms[0].RUIKEIHEIKIN_BUNSI : "";
        var ruikeiheikin_bunbo = document.forms[0].RUIKEIHEIKIN_BUNBO ? document.forms[0].RUIKEIHEIKIN_BUNBO : "";

        if (nyuryoku_flg) {
            if (!confirm("上限値が入力されています。\n授業時数管理区分を設定していない時は空になります。")) {
                return false;
            } else {
                risyu_bunsi.value = "";
                risyu_bunbo.value = "";
                syutoku_bunsi.value = "";
                syutoku_bunbo.value = "";
                risyu_bunsi_special.value = "";
                risyu_bunbo_special.value = "";
            }
        }

        if (parseInt(risyu_bunsi.value) > parseInt(risyu_bunbo.value)) {
            alert("分母より大きい分子があります。(履修上限値)");
            return false;
        }

        if (parseInt(syutoku_bunsi.value) > parseInt(syutoku_bunbo.value)) {
            alert("分母より大きい分子があります。(修得上限値)");
            return false;
        }

        if (parseInt(risyu_bunsi_special.value) > parseInt(risyu_bunbo_special.value)) {
            alert("分母より大きい分子があります。()");
            return false;
        }

        if ((kesseki_warn_bunsi.value || kesseki_warn_bunbo.value) && (!kesseki_warn_bunsi.value || !kesseki_warn_bunbo.value)) {
            alert("分母または分子が入力されていません。");
            return false;
        }

        if ((kesseki_out_bunsi.value || kesseki_out_bunbo.value) && (!kesseki_out_bunsi.value || !kesseki_out_bunbo.value)) {
            alert("分母または分子が入力されていません。");
            return false;
        }

        if (kesseki_warn_bunsi.value && kesseki_warn_bunbo.value) {
            if (parseInt(kesseki_warn_bunsi.value) > parseInt(kesseki_warn_bunbo.value)) {
                alert("分母より大きい分子があります。");
                return false;
            }
        }

        if (kesseki_out_bunsi.value && kesseki_out_bunbo.value) {
            if (parseInt(kesseki_out_bunsi.value) > parseInt(kesseki_out_bunbo.value)) {
                alert("分母より大きい分子があります。");
                return false;
            }
        }

        if (ruikeiheikin_bunsi.value && ruikeiheikin_bunbo.value) {
            if (parseInt(ruikeiheikin_bunsi.value) > parseInt(ruikeiheikin_bunbo.value)) {
                alert("分母より大きい分子があります。");
                return false;
            }
        }

        if (kesseki_warn_bunsi.value && kesseki_warn_bunbo.value && kesseki_out_bunsi.value && kesseki_out_bunbo.value) {
            kesseki_warn = parseInt(kesseki_warn_bunsi.value) / parseInt(kesseki_warn_bunbo.value);
            kesseki_out = parseInt(kesseki_out_bunsi.value) / parseInt(kesseki_out_bunbo.value);
            if (kesseki_warn > kesseki_out) {
                alert("欠席日数超過より欠席日数注意が大きくなっています。");
                return false;
            }
        }

        risyu_jougen = parseInt(risyu_bunsi.value) / parseInt(risyu_bunbo.value);
        syutoku_jougen = parseInt(syutoku_bunsi.value) / parseInt(syutoku_bunbo.value);

        if (syutoku_jougen > risyu_jougen) {
            alert("履修上限値より修得上限値が大きくなっています。");
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function btn_jisuchange(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function showConfirm() {
    if (confirm("{rval MSG107}")) {
        document.forms[0].cmd.value = "main";
        document.forms[0].submit();
    }
    return false;
}

function closing_window(flg) {
    if (flg == 1) {
        alert("{rval MSG300}");
    }
    if (flg == 2) {
        alert("{rval MSG305}" + "\n(コントロールマスタまたは評定マスタ)");
    }
    closeWin();
    return true;
}

function add() {
    var temp1 = new Array();
    var tempa = new Array();
    var v = document.forms[0].year.length;
    var w = document.forms[0].year_add.value;

    if (w == "") return false;

    for (var i = 0; i < v; i++) {
        if (w == document.forms[0].year.options[i].value) {
            alert("追加した年度は既に存在しています。");
            return false;
        }
    }
    document.forms[0].year.options[v] = new Option();
    document.forms[0].year.options[v].value = w;
    document.forms[0].year.options[v].text = w;

    for (var i = 0; i < document.forms[0].year.length; i++) {
        temp1[i] = document.forms[0].year.options[i].value;
        tempa[i] = document.forms[0].year.options[i].text;
    }
    //sort
    temp1 = temp1.sort();
    tempa = tempa.sort();
    temp1 = temp1.reverse();
    tempa = tempa.reverse();

    //generating new options
    ClearList(document.forms[0].year, document.forms[0].year);
    if (temp1.length > 0) {
        for (var i = 0; i < temp1.length; i++) {
            document.forms[0].year.options[i] = new Option();
            document.forms[0].year.options[i].value = temp1[i];
            document.forms[0].year.options[i].text = tempa[i];
            if (w == temp1[i]) {
                document.forms[0].year.options[i].selected = true;
            }
        }
    }
    //temp_clear();
}

function change_absent(absent_cov_late, amari_kuriage) {
    var absent_cov = document.forms[0].ABSENT_COV.value;
    if (absent_cov == 0) {
        document.forms[0].ABSENT_COV_LATE.value = "";
        document.forms[0].ABSENT_COV_LATE.disabled = true;
    } else {
        document.forms[0].ABSENT_COV_LATE.value = absent_cov_late;
        document.forms[0].ABSENT_COV_LATE.disabled = false;
    }

    if (absent_cov != 5) {
        document.forms[0].AMARI_KURIAGE.value = "";
        document.forms[0].AMARI_KURIAGE.disabled = true;
    } else {
        document.forms[0].AMARI_KURIAGE.value = amari_kuriage;
        document.forms[0].AMARI_KURIAGE.disabled = false;
    }
}

function changeRadio(obj) {
    var type_file;
    if (obj.value == "1") {
        //1は取り込み
        document.forms[0].FILE.disabled = false;
    } else {
        document.forms[0].FILE.disabled = true;
        type_file = document.getElementById("type_file"); //ファイルアップローダーの値を消す
        var innertString = type_file.innerHTML;
        type_file.innerHTML = innertString;
    }
}
function changeKeikokutenKubun() {
    if (document.forms[0].KEIKOKUTEN_KUBUN) {
        if (document.forms[0].KEIKOKUTEN_KUBUN.value == "2") {
            document.forms[0].RUIKEIHEIKIN_BUNSI.disabled = false;
            document.forms[0].RUIKEIHEIKIN_BUNBO.disabled = false;
        } else {
            document.forms[0].RUIKEIHEIKIN_BUNSI.disabled = true;
            document.forms[0].RUIKEIHEIKIN_BUNBO.disabled = true;
        }
    }
}

//グループウェア画面へ
function openScreen(URL) {
    //画面コール前チェックは、ここに記述する
    if (document.forms[0].dataBaseinfo.value == "2") {
        if (document.forms[0].KYOUIKU_IINKAI_SCHOOLCD.value == "") {
            alert("{rval MSG301}" + "\n（教育委員会統計用学校番号）");
            return false;
        }
    }

    wopen(URL, "SUBWIN3", 0, 0, screen.availWidth, screen.availHeight);
}
window.onload = function () {
    changeKeikokutenKubun();
};
