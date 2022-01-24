function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function newwin(SERVLET_URL){
    if (document.forms[0].JIKANWARI_SYUBETU[1].checked == true) {
        var sdate = document.forms[0].SDATE.value;
        var edate = document.forms[0].EDATE.value;
        var chk_sdate = document.forms[0].CHK_SDATE.value;
        var chk_edate = document.forms[0].CHK_EDATE.value;

        if (sdate == "") {
            alert("指定範囲が正しく有りません。");
            document.forms[0].SDATE.focus();
            return;
        }

        //日付の年度内チェック
        if((sdate < chk_sdate) || (sdate > chk_edate)){
            alert("日付が範囲外です。\n（" + chk_sdate + "～" + chk_edate + "） ");
            return;
        }
    }

    if (document.forms[0].JIKANWARI_SYUBETU[0].checked == true) {
        if(document.forms[0].TITLE.selectedIndex == 0) {
            alert("指定範囲が正しく有りません。");
            return;
        }
        var title = document.forms[0].TITLE.value;
        var title_array = title.split(',');

        document.forms[0].T_YEAR.value     = title_array[0];
        document.forms[0].T_BSCSEQ.value   = title_array[1];
        document.forms[0].T_SEMESTER.value = title_array[2];
    }


    document.forms[0].EDATE.disabled  = false;


    action = document.forms[0].action;
    target = document.forms[0].target;

//    document.forms[0].action = "/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJB";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;

    document.forms[0].EDATE.disabled  = true;
}

function jikanwari(obj){
    if(obj.value == 1){
        flag1 = false;
        flag2 = true;
        document.forms[0].submit();
    } else {
        flag1 = true;
        flag2 = false;
        document.forms[0].EDATE.value = "";
        document.forms[0].TITLE.value = "";
        var ctrl_date = document.forms[0].CTRL_DATE.value;
        document.forms[0].SDATE.value = ctrl_date;
        document.forms[0].submit();
    }
    document.forms[0].TITLE.disabled = flag1;
    document.forms[0].SDATE.disabled  = flag2;
    document.forms[0].btn_calen.disabled  = flag2;
}

function dis_date(flag) {
    document.forms[0].SDATE.disabled = flag;
    document.forms[0].btn_calen.disabled = flag;
}
