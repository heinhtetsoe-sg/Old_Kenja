function btn_submit(cmd) {
    if (document.forms[0].SCHREGNO.value == "") {
        alert("{rval MSG304}");
        return true;
    }

    if (cmd == "subform2_new" || cmd == "subform5_new" || cmd == "subform6_new" || cmd == "subform7_new" || cmd == "subform8_new") {
        //作成年月日チェック
        var getWiringDate = document.forms[0].WRITING_DATE_MAIN.value;
        var getSDate = document.forms[0].SDATE.value;
        var getEDate = document.forms[0].EDATE.value;
        if (cmd == "subform2_new") {
            var copyDate = document.forms[0].RECORD_DATE_A.value;
            var title = "A アセスメント表";
        } else if (cmd == "subform5_new") {
            var copyDate = document.forms[0].RECORD_DATE_E.value;
            var title = "サポートブック";
        } else if (cmd == "subform6_new") {
            var copyDate = document.forms[0].RECORD_DATE_F.value;
            var title = "引継資料(担任)";
        } else if (cmd == "subform7_new") {
            var copyDate = document.forms[0].RECORD_DATE_G.value;
            var title = "引継資料(事業者)";
        } else {
            var copyDate = document.forms[0].RECORD_DATE_H.value;
            var title = "関係者間資料";
        }
        copyDate = copyDate.replace(/-/g, "/");
        if (getWiringDate == "") {
            alert("{rval MSG301}" + "\n(作成日付)");
            return true;
        }
        if (getSDate > getWiringDate) {
            alert("{rval MSG203}" + "\n作成日付はログイン年度内の日付を指定して下さい。");
            return true;
        }
        if (getEDate < getWiringDate) {
            alert("{rval MSG203}" + "\n作成日付はログイン年度内の日付を指定して下さい。");
            return true;
        }
        //同一日チェック
        if (getWiringDate == copyDate) {
            alert("{rval MSG203}" + "\n作成日付と元データの日付が同一の場合、処理できません。");
            return true;
        }
        if (copyDate === "NEW") {
            copyDate = "最新";
        }
        if (
            !confirm(
                document.forms[0].CTRL_YEAR.value +
                    "年度の" +
                    title +
                    "を" +
                    copyDate +
                    "データを元に新規作成しますか？" +
                    "\n\n(作成日付が作成年月日となります。\n指定の作成年月日のデータがある場合は、上書き更新します。)"
            )
        ) {
            return false;
        }
    }
    if (cmd == "subform3_new") {
        //作成年月日チェック
        var getWiringDate = document.forms[0].WRITING_DATE_MAIN.value;
        var getSDate = document.forms[0].SDATE.value;
        var getEDate = document.forms[0].EDATE.value;
        var copyDate = document.forms[0].RECORD_DATE_C.value;
        copyDate = copyDate.replace(/-/g, "/");
        if (getWiringDate == "") {
            alert("{rval MSG301}" + "\n(作成日付)");
            return true;
        }
        if (getSDate > getWiringDate) {
            alert("{rval MSG203}" + "\n作成日付はログイン年度内の日付を指定して下さい。");
            return true;
        }
        if (getEDate < getWiringDate) {
            alert("{rval MSG203}" + "\n作成日付はログイン年度内の日付を指定して下さい。");
            return true;
        }
        //履歴チェック
        if (document.forms[0].CTRL_DATE.value == document.forms[0].RECORD_DATE_C.value) {
            alert("{rval MSG203}" + "\nログイン日付と履歴の日付が同一の場合、処理できません。");
            return true;
        }
        if (document.forms[0].CTRL_YEAR.value == document.forms[0].YEAR_C.value && document.forms[0].RECORD_DATE_C.value == "NEW") {
            alert("{rval MSG203}" + "\nコピー元がログイン年度で最新データの場合、処理できません。");
            return true;
        }
        if (copyDate === "NEW") {
            copyDate = "最新";
        }
        if (
            !confirm(
                document.forms[0].CTRL_YEAR.value +
                    "年度の支援計画を" +
                    copyDate +
                    "データを元に最新データを新規作成しますか？" +
                    "\n\n(作成日付が作成年月日となります。\n既に最新データがある場合は、ログイン日で履歴に残した後に上書き更新します。)"
            )
        ) {
            return false;
        }
    }

    if (cmd == "delete" && !confirm("{rval MSG103}")) {
        return true;
    } else if (cmd == "delete") {
        for (var i = 0; i < document.forms[0].elements.length; i++) {
            if (document.forms[0].elements[i].name == "CHECKED[]" && document.forms[0].elements[i].checked) {
                break;
            }
        }
        if (i == document.forms[0].elements.length) {
            alert("チェックボックスを選択してください");
            return true;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//全チェック操作
function check_all(obj) {
    for (var i = 0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name == "CHECKED[]") {
            document.forms[0].elements[i].checked = obj.checked;
        }
    }
}
