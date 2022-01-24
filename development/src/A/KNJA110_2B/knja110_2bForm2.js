function btn_submit(cmd) {
    var pop_up_flg = false;
    if (cmd == 'add2' || cmd == 'update2') {
        if (document.forms[0].CHECK_RELATIONSHIP.value    != document.forms[0].RELATIONSHIP.value) { pop_up_flg = true; }
        if (document.forms[0].CHECK_GUARD_NAME.value      != document.forms[0].GUARD_NAME.value)   { pop_up_flg = true; }
        if (document.forms[0].CHECK_GUARD_KANA.value      != document.forms[0].GUARD_KANA.value)   { pop_up_flg = true; }
        if (document.forms[0].CHECK_GUARD_REAL_NAME.value != document.forms[0].GUARD_REAL_NAME.value)   { pop_up_flg = true; }
        if (document.forms[0].CHECK_GUARD_REAL_KANA.value != document.forms[0].GUARD_REAL_KANA.value)   { pop_up_flg = true; }
        if (document.forms[0].CHECK_GUARD_SEX.value       != document.forms[0].GUARD_SEX.value)    { pop_up_flg = true; }
        if (document.forms[0].CHECK_GUARD_BIRTHDAY.value.replace(/-/g,"/") != document.forms[0].GUARD_BIRTHDAY.value.replace(/-/g,"/")) { pop_up_flg = true; }
    }

    if (pop_up_flg) {
        var check_relationship    = document.forms[0].CHECK_RELATIONSHIP.value    != document.forms[0].RELATIONSHIP.value    ? '1' : '0';
        var check_guard_name      = document.forms[0].CHECK_GUARD_NAME.value      != document.forms[0].GUARD_NAME.value      ? '1' : '0';
        var check_guard_kana      = document.forms[0].CHECK_GUARD_KANA.value      != document.forms[0].GUARD_KANA.value      ? '1' : '0';
        var check_guard_real_name = document.forms[0].CHECK_GUARD_REAL_NAME.value != document.forms[0].GUARD_REAL_NAME.value ? '1' : '0';
        var check_guard_real_kana = document.forms[0].CHECK_GUARD_REAL_KANA.value != document.forms[0].GUARD_REAL_KANA.value ? '1' : '0';
        var check_guard_sex       = document.forms[0].CHECK_GUARD_SEX.value       != document.forms[0].GUARD_SEX.value       ? '1' : '0';
        var check_guard_birthday  = document.forms[0].CHECK_GUARD_BIRTHDAY.value.replace(/-/g,"/") != document.forms[0].GUARD_BIRTHDAY.value.replace(/-/g,"/") ? '1' : '0';

        document.forms[0].cmd.value = cmd;
        var requestroot = document.forms[0].REQUESTROOT.value;
        var load = '';
        load  = "loadwindow('" + requestroot + "/A/KNJA110_2B/knja110_2bindex.php?cmd=subForm1";
        load += "&RELATIONSHIP_FLG="+     check_relationship    ;
        load += "&GUARD_NAME_FLG="+       check_guard_name      ;
        load += "&GUARD_KANA_FLG="+       check_guard_kana      ;
        load += "&GUARD_REAL_NAME_FLG="+  check_guard_real_name ;
        load += "&GUARD_REAL_KANA_FLG="+  check_guard_real_kana ;
        load += "&GUARD_SEX_FLG="+        check_guard_sex       ;
        load += "&GUARD_BIRTHDAY_FLG="+   check_guard_birthday  ;
        load += "',50,200,500,250)";

        eval(load);
        load = '';

        return false;
    }

    if (cmd == 'clear') {
        if (!confirm('{rval MSG106}'))
        return false;
    }
    if (cmd == 'delete'){
        if (!confirm('{rval MSG103}'))
            return false;
    }
    document.forms[0].cmd.value = cmd;

    for (var i = 0; i < document.forms[0].length; i++) {
        if (document.forms[0][i].disabled) {
            document.forms[0][i].disabled = false;
        }
    }

    document.forms[0].submit();
    return false;
}

function ShowConfirm(){
    if (!confirm('{rval MSG106}'))
        return false;
}

function copy(mode){
    with(document.forms[0]){
        if (mode == 1){
            ISSUEDATE.value  = GUARD_ISSUEDATE.value;
            EXPIREDATE.value = GUARD_EXPIREDATE.value;
            ZIPCD.value      = GUARD_ZIPCD.value;
            ADDR1.value      = GUARD_ADDR1.value;
            ADDR2.value      = GUARD_ADDR2.value;
            TELNO.value      = GUARD_TELNO.value;
            TELNO2.value     = GUARD_TELNO2.value;
            FAXNO.value      = GUARD_FAXNO.value;
        }else{
            GUARD_ISSUEDATE.value  = ISSUEDATE.value;
            GUARD_EXPIREDATE.value = EXPIREDATE.value;
            GUARD_ZIPCD.value      = ZIPCD.value;
            GUARD_ADDR1.value      = ADDR1.value;
            GUARD_ADDR2.value      = ADDR2.value;
            GUARD_TELNO.value      = TELNO.value;
            GUARD_TELNO2.value     = TELNO2.value;
            GUARD_FAXNO.value      = FAXNO.value;
        }
    }
}

function copyHidden(mode){
    with(document.forms[0]){
        if (mode == 1){
            ISSUEDATE.value  = COPY_GUARD_ISSUEDATE.value;
            EXPIREDATE.value = COPY_GUARD_EXPIREDATE.value;
            ZIPCD.value      = COPY_GUARD_ZIPCD.value;
            ADDR1.value      = COPY_GUARD_ADDR1.value;
            ADDR2.value      = COPY_GUARD_ADDR2.value;
            TELNO.value      = COPY_GUARD_TELNO.value;
            TELNO2.value     = COPY_GUARD_TELNO2.value;
            FAXNO.value      = COPY_GUARD_FAXNO.value;
        }else{
            GUARD_ISSUEDATE.value  = COPY_GUARD_ISSUEDATE.value;
            GUARD_EXPIREDATE.value = COPY_GUARD_EXPIREDATE.value;
            GUARD_ZIPCD.value      = COPY_GUARD_ZIPCD.value;
            GUARD_ADDR1.value      = COPY_GUARD_ADDR1.value;
            GUARD_ADDR2.value      = COPY_GUARD_ADDR2.value;
            GUARD_TELNO.value      = COPY_GUARD_TELNO.value;
            GUARD_TELNO2.value     = COPY_GUARD_TELNO2.value;
            GUARD_FAXNO.value      = COPY_GUARD_FAXNO.value;
        }
    }
}

function Page_jumper(link) {
    if (document.forms[0].UPDATED.value == "" && document.forms[0].GUARD_UPDATED.value == "") {
        alert('リストから生徒を選択してください。');
        return;
    }
    if (!confirm('{rval MSG108}')) {
        return;
    }
    parent.location.href=link;
}

