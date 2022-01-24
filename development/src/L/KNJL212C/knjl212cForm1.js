function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//印刷
function newwin(SERVLET_URL){
    if (document.forms[0].Pretest_bus_Not_Hyouji.value != "1") {
        //範囲チェック
        hour1 = document.forms[0].RINKAN_HOUR.value;
        hour2 = document.forms[0].FUKUJIN_HOUR.value;
        hour3 = document.forms[0].GOJOU_HOUR.value;
        hour4 = document.forms[0].GAKUEN_HOUR.value;
        if (toHour(hour1) || toHour(hour2) || toHour(hour3) || toHour(hour4)) {
            alert('時は【01～24】を入力して下さい。');
            return false;
        }
        minute1 = document.forms[0].RINKAN_MINUTE.value;
        minute2 = document.forms[0].FUKUJIN_MINUTE.value;
        minute3 = document.forms[0].GOJOU_MINUTE.value;
        minute4 = document.forms[0].GAKUEN_MINUTE.value;
        if (toMinute(minute1) || toMinute(minute2) || toMinute(minute3) || toMinute(minute4)) {
            alert('分は【00～59】を入力して下さい。');
            return false;
        }
        if (toHourMinute(hour1, minute1) || toHourMinute(hour2, minute2) || toHourMinute(hour3, minute3) || toHourMinute(hour4, minute4)) {
            alert('時・分は両方とも入力して下さい。');
            return false;
        }
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJL";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
//時
function toHour(val) {
    if (val == '') return false;
    var hour = parseInt(val);
    if (hour < 1 || 24 < hour) {
        return true;
    }
}
//分
function toMinute(val) {
    if (val == '') return false;
    var minute = parseInt(val);
    if (minute < 0 || 59 < minute) {
        return true;
    }
}
//時分のどれか片方のみ入力された状態か？
function toHourMinute(hour, minute) {
    if ((hour != '' && minute == '') || (hour == '' && minute != '')) {
        return true;
    }
    return false;
}
