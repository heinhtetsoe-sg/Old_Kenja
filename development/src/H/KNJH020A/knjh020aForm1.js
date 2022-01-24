function btn_submit(cmd) {
    var pop_up_flg = false;
    if (cmd == "update") {
        if (document.forms[0].CHECK_RELATIONSHIP.value != document.forms[0].RELATIONSHIP.value) {
            pop_up_flg = true;
        }
        if (document.forms[0].CHECK_GUARD_NAME.value != document.forms[0].GUARD_NAME.value) {
            pop_up_flg = true;
        }
        if (document.forms[0].CHECK_GUARD_KANA.value != document.forms[0].GUARD_KANA.value) {
            pop_up_flg = true;
        }
        if (document.forms[0].CHECK_GUARD_REAL_NAME.value != document.forms[0].GUARD_REAL_NAME.value) {
            pop_up_flg = true;
        }
        if (document.forms[0].CHECK_GUARD_REAL_KANA.value != document.forms[0].GUARD_REAL_KANA.value) {
            pop_up_flg = true;
        }
        if (document.forms[0].CHECK_GUARD_SEX.value != document.forms[0].GUARD_SEX.value) {
            pop_up_flg = true;
        }
        if (document.forms[0].CHECK_GUARD_BIRTHDAY.value.replace(/-/g, "/") != document.forms[0].GUARD_BIRTHDAY.value.replace(/-/g, "/")) {
            pop_up_flg = true;
        }

        if (document.forms[0].CHECK_GUARANTOR_RELATIONSHIP.value != document.forms[0].GUARANTOR_RELATIONSHIP.value) {
            pop_up_flg = true;
        }
        if (document.forms[0].CHECK_GUARANTOR_NAME.value != document.forms[0].GUARANTOR_NAME.value) {
            pop_up_flg = true;
        }
        if (document.forms[0].CHECK_GUARANTOR_KANA.value != document.forms[0].GUARANTOR_KANA.value) {
            pop_up_flg = true;
        }
        if (document.forms[0].CHECK_GUARANTOR_REAL_NAME.value != document.forms[0].GUARANTOR_REAL_NAME.value) {
            pop_up_flg = true;
        }
        if (document.forms[0].CHECK_GUARANTOR_REAL_KANA.value != document.forms[0].GUARANTOR_REAL_KANA.value) {
            pop_up_flg = true;
        }
        if (document.forms[0].CHECK_GUARANTOR_SEX.value != document.forms[0].GUARANTOR_SEX.value) {
            pop_up_flg = true;
        }
    }

    if (pop_up_flg) {
        var check_relationship = document.forms[0].CHECK_RELATIONSHIP.value != document.forms[0].RELATIONSHIP.value ? "1" : "0";
        var check_guard_name = document.forms[0].CHECK_GUARD_NAME.value != document.forms[0].GUARD_NAME.value ? "1" : "0";
        var check_guard_kana = document.forms[0].CHECK_GUARD_KANA.value != document.forms[0].GUARD_KANA.value ? "1" : "0";
        var check_guard_real_name = document.forms[0].CHECK_GUARD_REAL_NAME.value != document.forms[0].GUARD_REAL_NAME.value ? "1" : "0";
        var check_guard_real_kana = document.forms[0].CHECK_GUARD_REAL_KANA.value != document.forms[0].GUARD_REAL_KANA.value ? "1" : "0";
        var check_guard_sex = document.forms[0].CHECK_GUARD_SEX.value != document.forms[0].GUARD_SEX.value ? "1" : "0";
        var check_guard_birthday = document.forms[0].CHECK_GUARD_BIRTHDAY.value.replace(/-/g, "/") != document.forms[0].GUARD_BIRTHDAY.value.replace(/-/g, "/") ? "1" : "0";

        var check_guarantor_relationship = document.forms[0].CHECK_GUARANTOR_RELATIONSHIP.value != document.forms[0].GUARANTOR_RELATIONSHIP.value ? "1" : "0";
        var check_guarantor_name = document.forms[0].CHECK_GUARANTOR_NAME.value != document.forms[0].GUARANTOR_NAME.value ? "1" : "0";
        var check_guarantor_kana = document.forms[0].CHECK_GUARANTOR_KANA.value != document.forms[0].GUARANTOR_KANA.value ? "1" : "0";
        var check_guarantor_real_name = document.forms[0].CHECK_GUARANTOR_REAL_NAME.value != document.forms[0].GUARANTOR_REAL_NAME.value ? "1" : "0";
        var check_guarantor_real_kana = document.forms[0].CHECK_GUARANTOR_REAL_KANA.value != document.forms[0].GUARANTOR_REAL_KANA.value ? "1" : "0";
        var check_guarantor_sex = document.forms[0].CHECK_GUARANTOR_SEX.value != document.forms[0].GUARANTOR_SEX.value ? "1" : "0";

        document.forms[0].cmd.value = cmd;
        var setpopup = document.getElementById("sidebar").scrollTop;
        var requestroot = document.forms[0].REQUESTROOT.value;
        var load = "";
        load = "loadwindow('" + requestroot + "/H/KNJH020A/knjh020aindex.php?cmd=subForm2";
        load += "&RELATIONSHIP_FLG=" + check_relationship;
        load += "&GUARD_NAME_FLG=" + check_guard_name;
        load += "&GUARD_KANA_FLG=" + check_guard_kana;
        load += "&GUARD_REAL_NAME_FLG=" + check_guard_real_name;
        load += "&GUARD_REAL_KANA_FLG=" + check_guard_real_kana;
        load += "&GUARD_SEX_FLG=" + check_guard_sex;
        load += "&GUARD_BIRTHDAY_FLG=" + check_guard_birthday;
        load += "&GUARANTOR_RELATIONSHIP_FLG=" + check_guarantor_relationship;
        load += "&GUARANTOR_NAME_FLG=" + check_guarantor_name;
        load += "&GUARANTOR_KANA_FLG=" + check_guarantor_kana;
        load += "&GUARANTOR_REAL_NAME_FLG=" + check_guarantor_real_name;
        load += "&GUARANTOR_REAL_KANA_FLG=" + check_guarantor_real_kana;
        load += "&GUARANTOR_SEX_FLG=" + check_guarantor_sex;
        load += "',130," + setpopup + "+15,450,450)";

        eval(load);
        load = "";

        return false;
    }

    if (cmd == "reset") {
        if (!confirm("{rval MSG106}")) {
            return false;
        } else {
            document.forms[0].cmd.value = cmd;
            document.forms[0].submit();
            return false;
        }
    }
    if (cmd == "delete" && !confirm("{rval MSG103}")) {
        return true;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function Page_jumper(jump, sno, nd) {
    var cd;
    var cd2;
    cd = "?SCHREGNO=";
    cd2 = "&NEWAD=";
    if (sno == "") {
        alert("{rval MSG304}");
        return;
    }
    if (!confirm("{rval MSG108}")) {
        return;
    }
    parent.right_frame.location.replace(jump + cd + sno + cd2 + nd);
}

function Rireki_jumper(link) {
    if (!confirm("{rval MSG108}")) {
        return;
    }
    location.href = link;
}

function toTelNo(checkString) {
    var newString = "";
    var count = 0;
    for (i = 0; i < checkString.length; i++) {
        ch = checkString.substring(i, i + 1);
        if ((ch >= "0" && ch <= "9") || ch == "-") {
            newString += ch;
        }
    }

    if (checkString != newString) {
        alert("入力された値は不正な文字列です。\n電話(FAX)番号を入力してください。\n入力された文字列は削除されます。");
        // 文字列を返す
        return newString;
    }
    return checkString;
}
