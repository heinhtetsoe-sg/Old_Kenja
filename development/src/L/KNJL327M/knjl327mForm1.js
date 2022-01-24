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
    //日付入力チェック
    var date = document.forms[0].DATE.value;

    if (date == "") {
        alert("日付が不正です。");
        document.forms[0].DATE.focus();
        return false;
    }

    var sub_examno_from = document.forms[0].SUB_EXAMNO_FROM;
    var sub_examno_to = document.forms[0].SUB_EXAMNO_TO;

    if (document.forms[0].PRINT_TYPE[0].checked) {
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
    } else {
        //時間入力チェック
        var yuko_time = document.forms[0].YUKO_TIME.value;

        if (yuko_time == "") {
            alert("時間が不正です。");
            document.forms[0].YUKO_TIME.focus();
            return false;
        }

        //時間の範囲内チェック
        if((parseInt(yuko_time) < 1) || (parseInt(yuko_time) > 24)){
            alert("時間が範囲外です。\n（1～24） ");
            return;
        }
        document.forms[0].YUKO_TIME.value = parseInt(yuko_time);
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
