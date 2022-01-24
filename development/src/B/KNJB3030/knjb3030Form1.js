function btn_submit(cmd) {
    if (cmd == "copy") {
        var term = document.forms[0].term;
        var term2 = document.forms[0].term2;
        var i = term.selectedIndex;
        var j = term2.selectedIndex;
        var year_seme = term.options[i].value;
        var year_seme2 = term2.options[j].value;
        var insertyear = document.forms[0].CHAIR_DAT_COUNT.value;

        if (insertyear == 0) {
            if (
                !confirm(
                    "対象年度のデータを削除して、コピーします。よろしいでしょうか？"
                )
            ) {
                return false;
            }
        } else {
            if (
                !confirm(
                    "対象年度に既にデータが存在します。\n対象年度のデータを削除して、コピーします。よろしいでしょうか？"
                )
            ) {
                return false;
            }
        }
        if (year_seme < document.forms[0].ctrl_year_semester.value) {
            var year = document.forms[0].ctrl_year.value;
            var seme = document.forms[0].ctrl_semester.value;
            alert(
                "コピー不可。対象年度は、" +
                    year +
                    seme +
                    " 以降を選択して下さい。"
            );
            return false;
        }
        if (year_seme > year_seme2) {
        } else {
            alert(
                "コピー不可。参照学期は、対象年度より過去の学期を選択して下さい。"
            );
            return false;
        }
        var tmp = term.options[i].value.split("-");
        var tmp2 = term2.options[j].value.split("-");
        if (document.forms[0].check.checked) {
            if (tmp[0] != tmp2[0]) {
                alert(
                    "コピー不可。生徒もコピーは、年度をまたがってはコピーできません。"
                );
                return false;
            }
        }
    }
    if (cmd == "delete") {
        if (!confirm("{rval MSG103}")) return false;
    }
    if (cmd == "reset") {
        if (!confirm("{rval MSG106}")) return false;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//権限チェック
function OnAuthError() {
    alert("{rval MSG300}");
    closeWin();
}
//一度に表示する講座一覧の上限を超えたら、ワーニング
function DataLimitError(kensuu) {
    alert("表示件数が" + kensuu + "件を超えました。");
}
//卒業生・退学者・転学者も含むチェックボックス
function disCheckGrddiv() {
    if (document.forms[0].check.checked) {
        document.forms[0].checkGrddiv.disabled = false;
    } else {
        document.forms[0].checkGrddiv.disabled = true;
    }
}
