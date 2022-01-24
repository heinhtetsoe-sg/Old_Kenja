function btn_submit(cmd) {
    if (cmd == 'delete'){
        if (!confirm('{rval MSG103}'))
            return false;
    }
    if (cmd == 'reset'){
        if (!confirm('{rval MSG106}'))
            return false;
    }
    //確定
    if (cmd == 'kakutei') {
        if (document.forms[0].PATTERNCD.value == "" ||
            document.forms[0].PATTERNCDNAME.value == "" ||
            document.forms[0].BASEDATE.value == ""
        ) {
            alert('{rval MSG301}');
            return false;
        }
        if (!(document.forms[0].ASSESSLEVELCNT.value > 0)) {
            alert('{rval MSG916}\n( 段階値に1以上を指定してください。 )');
            return false;
        }
        if ((document.forms[0].SDATE.value >= document.forms[0].BASEDATE.value) ||
            (document.forms[0].EDATE.value <= document.forms[0].BASEDATE.value)) {
            alert('日付が年度範囲外です。');
            return false;
        }
    }
    if (cmd == 'add' || cmd == 'update') {
        for (var i=0; i < document.forms[0].elements.length; i++) {
            var obj_updElement = document.forms[0].elements[i];
            re = new RegExp("^ASSESSMARK|RATE" );
            if (obj_updElement.name.match(re) && obj_updElement.value == "") {
                alert('{rval MSG301}');
                return false;
            }
        }
        if ((document.forms[0].SDATE.value >= document.forms[0].BASEDATE.value) ||
            (document.forms[0].EDATE.value <= document.forms[0].BASEDATE.value)) {
            alert('日付が年度範囲外です。');
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}
