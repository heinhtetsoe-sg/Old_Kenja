function btn_submit(cmd) {
    if (cmd == 'csvOutput') {
        if (document.forms[0].EDATE.value == '') {
            alert('{rval MSG901}' + "日付が不正です。");
            document.forms[0].EDATE.focus();
            return false;
        }

        var day   = document.forms[0].EDATE.value;      //印刷範囲日付
        var sdate = document.forms[0].SEME_SDATE.value; //学期開始日付
        var edate = document.forms[0].SEME_EDATE.value; //学期終了日付
        var flag1 = document.forms[0].SEME_FLG.value;

        if (sdate > day || edate < day) {
            alert('{rval MSG914}' + "\n日付が学期の範囲外です。");
            return false;
        }

        var schoolKind = document.forms[0].SCHOOL_KIND.value;

        if (schoolKind != "J" && document.forms[0].CREDIT_LINE.value == "") {
            alert('{rval MSG901}' + "\n単位未修得基準が未入力です。");
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function inputCheck2(obj) {

    if (obj.value != null && obj.value != "") {
        if (!isFinite(obj.value)) {
            alert('{rval MSG907}');
            obj.focus();
            obj.select();
            return false;
        }
        var regex = new RegExp(/^[0-9]+$/);
        if (!regex.test(obj.value)) {
            alert('{rval MSG907}');
            obj.focus();
            obj.select();
            return false;
        }
        if (obj.value < 0 || 100 < obj.value ) {
            alert("{rval MSG914}" + "0～100の範囲で設定してください。");
            obj.focus();
            obj.select();
            return false;
        }
    }
    return true;
}

function newwin(SERVLET_URL) {
    if (document.forms[0].EDATE.value == '') {
        alert('{rval MSG901}' + "日付が不正です。");
        document.forms[0].EDATE.focus();
        return false;
    }

    var day   = document.forms[0].EDATE.value;      //印刷範囲日付
    var sdate = document.forms[0].SEME_SDATE.value; //学期開始日付
    var edate = document.forms[0].SEME_EDATE.value; //学期終了日付
    var flag1 = document.forms[0].SEME_FLG.value;

    if (sdate > day || edate < day) {
        alert('{rval MSG914}' + "\n日付が学期の範囲外です。");
        return false;
    }

    var schoolKind = document.forms[0].SCHOOL_KIND.value;

    if (schoolKind != "J" && document.forms[0].CREDIT_LINE.value == "") {
        alert('{rval MSG901}' + "\n単位未修得基準が未入力です。");
        return false;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJD";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

var check_checked = function () {

}

window.onload = check_checked;
