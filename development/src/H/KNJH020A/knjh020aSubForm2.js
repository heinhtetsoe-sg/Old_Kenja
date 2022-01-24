function btn_submit(cmd) {
    if (cmd == "subUpdate") {
        if (document.forms[0].E_APPDATE.value == "") {
            alert("変更開始日を入力して下さい");
            return true;
        }

        //今日の日付
        TODAY = document.forms[0].TODAY.value;

        //変更開始日付
        E_APPDATE = document.forms[0].E_APPDATE.value;
        E_APPDATE2 = document.forms[0].E_APPDATE2.value;

        LAST_DAY = document.forms[0].LAST_DAY.value;

        if (
            document.forms[0].RELATIONSHIP_FLG.checked ||
            document.forms[0].GUARD_NAME_FLG.checked ||
            document.forms[0].GUARD_KANA_FLG.checked ||
            document.forms[0].GUARD_REAL_NAME_FLG.checked ||
            document.forms[0].GUARD_REAL_KANA_FLG.checked ||
            document.forms[0].GUARD_SEX_FLG.checked ||
            document.forms[0].GUARD_BIRTHDAY_FLG.checked
        ) {
            //日付のチェックはチェックボックスにチェックが入っているときのみ行う
            if (E_APPDATE < LAST_DAY) {
                alert(LAST_DAY + "以降の日付を入力して下さい(保護者)");
                return false;
            }
        }
        LAST_DAY2 = document.forms[0].LAST_DAY2.value;

        if (
            document.forms[0].GUARANTOR_RELATIONSHIP_FLG.checked ||
            document.forms[0].GUARANTOR_NAME_FLG.checked ||
            document.forms[0].GUARANTOR_KANA_FLG.checked ||
            document.forms[0].GUARANTOR_REAL_NAME_FLG.checked ||
            document.forms[0].GUARANTOR_REAL_KANA_FLG.checked ||
            document.forms[0].GUARANTOR_SEX_FLG.checked
        ) {
            //日付のチェックはチェックボックスにチェックが入っているときのみ行う
            if (E_APPDATE2 < LAST_DAY2) {
                alert(LAST_DAY2 + "以降の日付を入力して下さい(保証人)");
                return false;
            }
        }

        parent.document.forms[0].E_APPDATE.value = document.forms[0].E_APPDATE.value;
        parent.document.forms[0].RELATIONSHIP_FLG.value = document.forms[0].RELATIONSHIP_FLG.checked ? "1" : "0";
        parent.document.forms[0].GUARD_NAME_FLG.value = document.forms[0].GUARD_NAME_FLG.checked ? "1" : "0";
        parent.document.forms[0].GUARD_KANA_FLG.value = document.forms[0].GUARD_KANA_FLG.checked ? "1" : "0";
        parent.document.forms[0].GUARD_REAL_NAME_FLG.value = document.forms[0].GUARD_REAL_NAME_FLG.checked ? "1" : "0";
        parent.document.forms[0].GUARD_REAL_KANA_FLG.value = document.forms[0].GUARD_REAL_KANA_FLG.checked ? "1" : "0";
        parent.document.forms[0].GUARD_SEX_FLG.value = document.forms[0].GUARD_SEX_FLG.checked ? "1" : "0";
        parent.document.forms[0].GUARD_BIRTHDAY_FLG.value = document.forms[0].GUARD_BIRTHDAY_FLG.checked ? "1" : "0";

        parent.document.forms[0].E_APPDATE2.value = document.forms[0].E_APPDATE2.value;
        parent.document.forms[0].GUARANTOR_RELATIONSHIP_FLG.value = document.forms[0].GUARANTOR_RELATIONSHIP_FLG.checked ? "1" : "0";
        parent.document.forms[0].GUARANTOR_NAME_FLG.value = document.forms[0].GUARANTOR_NAME_FLG.checked ? "1" : "0";
        parent.document.forms[0].GUARANTOR_KANA_FLG.value = document.forms[0].GUARANTOR_KANA_FLG.checked ? "1" : "0";
        parent.document.forms[0].GUARANTOR_REAL_NAME_FLG.value = document.forms[0].GUARANTOR_REAL_NAME_FLG.checked ? "1" : "0";
        parent.document.forms[0].GUARANTOR_REAL_KANA_FLG.value = document.forms[0].GUARANTOR_REAL_KANA_FLG.checked ? "1" : "0";
        parent.document.forms[0].GUARANTOR_SEX_FLG.value = document.forms[0].GUARANTOR_SEX_FLG.checked ? "1" : "0";

        if (parent.document.forms[0].cmd.value.match(/add/)) {
            parent.document.forms[0].cmd.value = "subAdd";
        } else {
            parent.document.forms[0].cmd.value = "subUpdate";
        }

        for (var i = 0; i < parent.document.forms[0].length; i++) {
            if (parent.document.forms[0][i].disabled) {
                parent.document.forms[0][i].disabled = false;
            }
        }

        parent.document.forms[0].submit();
    }
    parent.closeit();
}
