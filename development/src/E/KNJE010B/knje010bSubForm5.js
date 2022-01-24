// kanji=漢字
function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();

    return false;
}
function changiDateDisp(changeMode) {
    for (var i = 1; i <= document.forms[0].ID_CNT.value; i++) {
        var selObj = document.getElementById('LIST_' + i);
        var changeDate = selObj.innerHTML.split("／");
        var nokoriStr = selObj.innerHTML.substr(selObj.innerHTML.indexOf("／"));
        if (changeMode == 'wareki') {
            if (changeDate[0].indexOf("/") > 0) {
                var retval = Change_Wareki(changeDate[0], 0);
                selObj.innerHTML = retval + nokoriStr;
            }
        } else {
            var retval = document.forms[0]["DEF_REGDDATE_" + i].value;
            selObj.innerHTML = retval + nokoriStr;
        }
    }
}
