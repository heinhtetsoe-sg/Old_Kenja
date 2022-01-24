//  漢字
function btn_submit(cmd){

    if (cmd == "update") {
        if (document.forms[0].SUBCLASSCD.value == "") {
            alert('科目を指定して下さい。');
            return false;
        }
        var siteiBi = document.forms[0].DEADLINE_DATE.value.replace(/\//g, "");
        var kaisiBi = document.forms[0].CHECK_FIRST_DAY.value.replace(/-/g, "");
        var saisyuBi = document.forms[0].CHECK_LAST_DAY.value.replace(/-/g, "");
        if (siteiBi < kaisiBi || siteiBi > saisyuBi) {
            alert('日付の有効範囲は、' + document.forms[0].CHECK_FIRST_DAY.value.replace(/-/g, "/") + "～" + document.forms[0].CHECK_LAST_DAY.value.replace(/-/g, "/") + "です。");
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

