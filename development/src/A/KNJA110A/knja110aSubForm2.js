function btn_submit(cmd) {
    if (cmd == "subUpdate" || cmd == "subReplace_update") {
        var isCheck = false;
        if (
            document.forms[0].GRADE_FLG.checked ||
            document.forms[0].HR_CLASS_FLG.checked ||
            document.forms[0].ATTENDNO_FLG.checked ||
            document.forms[0].ANNUAL_FLG.checked ||
            document.forms[0].COURSECD_FLG.checked ||
            document.forms[0].MAJORCD_FLG.checked ||
            document.forms[0].COURSECODE_FLG.checked ||
            document.forms[0].NAME_FLG.checked ||
            document.forms[0].NAME_SHOW_FLG.checked ||
            document.forms[0].NAME_KANA_FLG.checked ||
            document.forms[0].NAME_ENG_FLG.checked ||
            document.forms[0].REAL_NAME_FLG.checked ||
            document.forms[0].REAL_NAME_KANA_FLG.checked ||
            document.forms[0].HANDICAP_FLG.checked ||
            document.forms[0].NATIONALITY2_FLG.checked ||
            document.forms[0].NATIONALITY_NAME_FLG.checked ||
            document.forms[0].NATIONALITY_NAME_KANA_FLG.checked ||
            document.forms[0].NATIONALITY_NAME_ENG_FLG.checked ||
            document.forms[0].NATIONALITY_REAL_NAME_FLG.checked ||
            document.forms[0].NATIONALITY_REAL_NAME_KANA_FLG.checked
        ) {
            isCheck = true;
        }
        if (isCheck && document.forms[0].E_APPDATE.value == "") {
            alert("変更開始日を入力して下さい");
            return true;
        }

        //変更開始日付
        date = document.forms[0].E_APPDATE.value;
        E_APPDATE = date_add(date, 0, 0, 0, "/");

        LAST_DAY = document.forms[0].LAST_DAY.value;
        if (isCheck) {
            if (E_APPDATE < LAST_DAY) {
                alert(LAST_DAY + "以降の日付を入力して下さい");
                return false;
            }

            var ninzu = document.forms[0].COUNT.value;
            for (var i = 0; i < ninzu; i++) {
                S_APPDATE = eval("document.forms[0].S_APPDATE" + i + ".value");
                if (S_APPDATE != "") {
                    //日付の設定方法
                    //生徒更新履歴最終日
                    date = eval("document.forms[0].S_APPDATE" + i + ".value");
                    schregno = eval(
                        "document.forms[0].SCHREGNO" + i + ".value"
                    );

                    S_APPDATE = date_add(date, 0, 0, 0, "/");

                    //変更開始日付(から一日引いたものが履歴の変更終了日になる)
                    date = document.forms[0].E_APPDATE.value;
                    E_APPDATE = date_add(date, 0, 0, 0, "/");
                    //S_APPDATEは前回の終了日＋１、なければ入学日
                    if (S_APPDATE > E_APPDATE) {
                        alert(
                            "変更開始日を『" +
                                S_APPDATE +
                                "』以降の日付を入力して下さい\n学籍番号" +
                                schregno +
                                "の生徒の変更終了日とかぶっています"
                        );
                        return true;
                    }
                }
            }
        }

        parent.document.forms[0].E_APPDATE.value =
            document.forms[0].E_APPDATE.value;
        parent.document.forms[0].GRADE_FLG.value = document.forms[0].GRADE_FLG
            .checked
            ? "1"
            : "0";
        parent.document.forms[0].HR_CLASS_FLG.value = document.forms[0]
            .HR_CLASS_FLG.checked
            ? "1"
            : "0";
        parent.document.forms[0].ATTENDNO_FLG.value = document.forms[0]
            .ATTENDNO_FLG.checked
            ? "1"
            : "0";
        parent.document.forms[0].ANNUAL_FLG.value = document.forms[0].ANNUAL_FLG
            .checked
            ? "1"
            : "0";
        parent.document.forms[0].COURSECD_FLG.value = document.forms[0]
            .COURSECD_FLG.checked
            ? "1"
            : "0";
        parent.document.forms[0].MAJORCD_FLG.value = document.forms[0]
            .MAJORCD_FLG.checked
            ? "1"
            : "0";
        parent.document.forms[0].COURSECODE_FLG.value = document.forms[0]
            .COURSECODE_FLG.checked
            ? "1"
            : "0";
        parent.document.forms[0].NAME_FLG.value = document.forms[0].NAME_FLG
            .checked
            ? "1"
            : "0";
        parent.document.forms[0].NAME_SHOW_FLG.value = document.forms[0]
            .NAME_SHOW_FLG.checked
            ? "1"
            : "0";
        parent.document.forms[0].NAME_KANA_FLG.value = document.forms[0]
            .NAME_KANA_FLG.checked
            ? "1"
            : "0";
        parent.document.forms[0].NAME_ENG_FLG.value = document.forms[0]
            .NAME_ENG_FLG.checked
            ? "1"
            : "0";
        parent.document.forms[0].cmd.value = cmd;
        parent.document.forms[0].REAL_NAME_FLG.value = document.forms[0]
            .REAL_NAME_FLG.checked
            ? "1"
            : "0";
        parent.document.forms[0].REAL_NAME_KANA_FLG.value = document.forms[0]
            .REAL_NAME_KANA_FLG.checked
            ? "1"
            : "0";
        parent.document.forms[0].HANDICAP_FLG.value = document.forms[0]
            .HANDICAP_FLG.checked
            ? "1"
            : "0";
        parent.document.forms[0].NATIONALITY2_FLG.value = document.forms[0]
            .NATIONALITY2_FLG.checked
            ? "1"
            : "0";
        parent.document.forms[0].NATIONALITY_NAME_FLG.value = document.forms[0]
            .NATIONALITY_NAME_FLG.checked
            ? "1"
            : "0";
        parent.document.forms[0].NATIONALITY_NAME_KANA_FLG.value = document
            .forms[0].NATIONALITY_NAME_KANA_FLG.checked
            ? "1"
            : "0";
        parent.document.forms[0].NATIONALITY_NAME_ENG_FLG.value = document
            .forms[0].NATIONALITY_NAME_ENG_FLG.checked
            ? "1"
            : "0";
        parent.document.forms[0].NATIONALITY_REAL_NAME_FLG.value = document
            .forms[0].NATIONALITY_REAL_NAME_FLG.checked
            ? "1"
            : "0";
        parent.document.forms[0].NATIONALITY_REAL_NAME_KANA_FLG.value = document
            .forms[0].NATIONALITY_REAL_NAME_KANA_FLG.checked
            ? "1"
            : "0";

        if (cmd == "subReplace_update") {
            parent.doSubmit("subReplace_update");
        } else {
            parent.document.forms[0].submit();
        }
    }
    parent.closeit();
}

//日付計算
//パラメータ(h_date:日付文字列, h_year:加算年, h_month:加算月, h_day:加算日, h_split_str:日付の区切り文字列)
//戻り値(日付文字列)
function date_add(h_date, h_year, h_month, h_day, h_split_str) {
    var ret = "";

    date_ary = h_date.split(h_split_str);

    DATEobj = new Date();

    DATEobj.setYear(eval(date_ary[0]) + h_year);
    DATEobj.setMonth(eval(date_ary[1]) + h_month - 1);
    DATEobj.setDate(eval(date_ary[2]) + h_day);

    year = DATEobj.getFullYear();
    month = DATEobj.getMonth() + 1;
    day = DATEobj.getDate();

    ret =
        headZero(year, 4) +
        h_split_str +
        headZero(month, 2) +
        h_split_str +
        headZero(day, 2);

    return ret;
}

//先頭に０を付ける
function headZero(val, len) {
    var ret = "";
    var work = "" + val;

    for (i = work.length; i < len; i++) {
        ret = ret + "0";
    }

    ret = ret + work;

    return ret;
}
