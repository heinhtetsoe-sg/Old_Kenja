function btn_submit() {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function zeroUme(keta, obj) {
    var num = new String(obj.value);
    var cnt = keta - num.length;

    if (cnt <= 0) {
        return num;
    }
    while (cnt-- > 0) {
        num = "0" + num;
    }
    obj.value = num;
}

function newwin(SERVLET_URL) {
    //日付チェック
    if (document.forms[0].DATE.value == '') {
        alert('{rval MSG301}\n( 日付 )');
        return true;
    }

    var examno_from = document.forms[0].EXAMNO_FROM;
    var examno_to = document.forms[0].EXAMNO_TO;


    if (document.forms[0].PRINT_RANGE[0].checked) {
        if (examno_from.value == '' ||  examno_to.value == '') {
            alert('{rval MSG301}');
            return;
        }
        if (parseInt(examno_from.value) > parseInt(examno_to.value)) {
            alert('受検番号の大小が不正です');
            return false;
        }
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

//  url = location.hostname;
//  document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJL";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
