function btn_submit(cmd) {
    if (cmd == "clear") {
        result = confirm("{rval MSG106}");
        if (result == false) {
            return false;
        }
    }
    if (cmd == "update") {
        var small_date = 0;
        var big_date = 0;
        for (var i = 0; i < document.forms[0]["COUNT_SDATE"].value; i++) {
            if (document.forms[0]["SEMESTER_NAME[]"][i].value == "") {
                alert("空欄があります。");
                return false;
            }
            if (document.forms[0]["SDATE" + (i + 1)].value == "") {
                alert("空欄があります。");
                return false;
            }
            if (document.forms[0]["EDATE" + (i + 1)].value == "") {
                alert("空欄があります。");
                return false;
            }
        }
        var checker = 0;
        for (var i = 0; i < document.forms[0]["COUNT_SDATE"].value; i++) {
            keyArray = document.forms[0]["sem_key[]"][i].value.split(",");
            next_date = document.forms[0]["SDATE" + (i + 2)];
            input_date = document.forms[0]["SDATE" + (i + 1)];
            input_edate = document.forms[0]["EDATE" + (i + 1)];
            varSdate = document.forms[0]["SEM_MST_SDATE" + keyArray[0]];
            varEdate = document.forms[0]["SEM_MST_EDATE" + keyArray[0]];

            if (input_date.value < keyArray[2] || input_date.value > keyArray[3] || input_edate.value < keyArray[2] || input_edate.value > keyArray[3]) {
                alert("日付確認してください。\n(学期開始・終了日との関係)");
                return false;
            }
            if (i + 1 < document.forms[0]["COUNT_SDATE"].value) {
                if (input_date.value > input_edate.value || input_edate.value >= next_date.value) {
                    alert("日付確認してください。\n(開始日と終了日の関係)");
                    return false;
                }
            }
            if (input_date.value < varSdate.value) {
                alert("日付確認してください。\n(開始日が学期開始日より前です。)");
                return false;
            }
            if (input_date.value > varEdate.value) {
                alert("日付確認してください。\n(開始日が学期終了日より後です。)");
                return false;
            }

            if (next_date != undefined) {
                if (input_date.value > next_date.value) {
                    alert("日付確認してください。\n(開始日の前後関係)");
                    return false;
                }
            }
            if (checker != keyArray[0]) {
                if (input_date.value != varSdate.value) {
                    alert("日付確認してください。\n(学期開始日との関係)");
                    return false;
                }
            }

            checker = keyArray[0];
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
