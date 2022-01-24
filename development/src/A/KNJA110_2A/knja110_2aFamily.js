function btn_submit(cmd) {
    if (cmd == 'clear') {
        if (!confirm('{rval MSG106}'))
        return false;
    }
    if (cmd == 'kakutei'){
        if (document.forms[0].RELA_SCHREGNO.value == "") {
            alert('兄弟姉妹学籍番号が未入力です。');
            return false;
        }
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
        } else if (mode == 4) {
            GUARANTOR_ISSUEDATE.value   = ISSUEDATE.value;
            GUARANTOR_EXPIREDATE.value  = EXPIREDATE.value;
            GUARANTOR_ZIPCD.value       = ZIPCD.value;
            GUARANTOR_ADDR1.value       = ADDR1.value;
            GUARANTOR_ADDR2.value       = ADDR2.value;
            GUARANTOR_TELNO.value       = TELNO.value;
        } else if (mode == 5) {
            SEND_ZIPCD.value    = ZIPCD.value;
            SEND_AREACD.value   = AREACD.value;
            SEND_ADDR1.value    = ADDR1.value;
            SEND_ADDR2.value    = ADDR2.value;
            SEND_TELNO.value    = TELNO.value;
            SEND_TELNO2.value   = TELNO2.value;
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
        if (mode == 1) {
            //保護者２よりコピー
            ISSUEDATE.value  = COPY2_GUARD_ISSUEDATE.value;
            EXPIREDATE.value = COPY2_GUARD_EXPIREDATE.value;
            ZIPCD.value      = COPY2_GUARD_ZIPCD.value;
            ADDR1.value      = COPY2_GUARD_ADDR1.value;
            ADDR2.value      = COPY2_GUARD_ADDR2.value;
            TELNO.value      = COPY2_GUARD_TELNO.value;
            TELNO2.value     = COPY2_GUARD_TELNO2.value;
            FAXNO.value      = COPY2_GUARD_FAXNO.value;
        } else if (mode == 4) {
            GUARANTOR_ISSUEDATE.value   = COPY_GUARD_ISSUEDATE.value;
            GUARANTOR_EXPIREDATE.value  = COPY_GUARD_EXPIREDATE.value;
            GUARANTOR_ZIPCD.value       = COPY_GUARD_ZIPCD.value;
            GUARANTOR_ADDR1.value       = COPY_GUARD_ADDR1.value;
            GUARANTOR_ADDR2.value       = COPY_GUARD_ADDR2.value;
            GUARANTOR_TELNO.value       = COPY_GUARD_TELNO.value;
        } else if (mode == 5) {
            SEND_ZIPCD.value        = COPY_GUARD_ZIPCD.value;
            SEND_ADDR1.value        = COPY_GUARD_ADDR1.value;
            SEND_ADDR2.value        = COPY_GUARD_ADDR2.value;
            SEND_TELNO.value        = COPY_GUARD_TELNO.value;
            SEND_TELNO2.value       = COPY_GUARD_TELNO2.value;
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

function Page_jumper(link, div) {
    if (div == 2) {
        if (document.forms[0].UPDATED.value == "" && document.forms[0].GUARANTOR_UPDATED.value == "") {
            alert('リストから生徒を選択してください。');
            return;
        }
    } else {
        if (document.forms[0].UPDATED.value == "" && document.forms[0].GUARD_UPDATED.value == "") {
            alert('リストから生徒を選択してください。');
            return;
        }
    }
    if (!confirm('{rval MSG108}')) {
        return;
    }
    parent.location.href=link;
}

