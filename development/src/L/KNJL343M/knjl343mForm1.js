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
    if (obj.name == 'SUB_EXAMNO_FROM') {
        document.forms[0].EXAMNO_FROM.value = num;
    } else {
        document.forms[0].EXAMNO_TO.value = num;
    }
}

function newwin(SERVLET_URL) {

    if (document.forms[0].DATE.value == '') {
        alert("通知日付が未入力です。");
        return;
    }

    var sub_examno_from = document.forms[0].SUB_EXAMNO_FROM;
    var sub_examno_to = document.forms[0].SUB_EXAMNO_TO;

    if (document.forms[0].PRINT_RANGE[1].checked) {
        if (sub_examno_from.value == '' ||  sub_examno_to.value == '') {
            alert('{rval MSG301}');
            return;
        }
        if (parseInt(sub_examno_from.value, 10) > parseInt(sub_examno_to.value, 10)) {
            alert('受験番号の大小が不正です');
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
