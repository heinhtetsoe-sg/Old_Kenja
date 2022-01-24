//サブミット
function btn_submit(cmd) {
    parent.document.forms[0].NATIONALITY2.value =
        document.forms[0].NATIONALITY2.value;
    parent.document.forms[0].NATIONALITY_NAME.value =
        document.forms[0].NATIONALITY_NAME.value;
    parent.document.forms[0].NATIONALITY_NAME_KANA.value =
        document.forms[0].NATIONALITY_NAME_KANA.value;
    parent.document.forms[0].NATIONALITY_NAME_ENG.value =
        document.forms[0].NATIONALITY_NAME_ENG.value;
    parent.document.forms[0].NATIONALITY_REAL_NAME.value =
        document.forms[0].NATIONALITY_REAL_NAME.value;
    parent.document.forms[0].NATIONALITY_REAL_NAME_KANA.value =
        document.forms[0].NATIONALITY_REAL_NAME_KANA.value;
    parent.document.forms[0].cmd.value = cmd;
    parent.document.forms[0].submit();
    return false;
}
