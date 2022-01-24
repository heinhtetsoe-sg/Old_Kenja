function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function datecheck(dval)
{
    var chflg = 0;

    //Nullチェック
    if (dval == '') {
        return '';
    }
    //日付正規チェック
    if (!isDate2(dval)) {
       return '';
    }
    //日付の一致チェック
    if (dval == document.forms[0].DEFOULTDATE.value){
        return '';
    }
    if (sem == 0){
        sem = 1;
        document.forms[0].cmd.value = 'dsub';
        document.forms[0].submit();
        return false;
    }
}
function kin(obj) {

    if (obj[1].selected || obj[2].selected || obj[3].selected){
        document.forms[0].SCHLTIME.disabled = true;
    }else {
        document.forms[0].SCHLTIME.disabled = false;
    }

}

function checkr(obj) {

    if (obj.value == 1){
        document.forms[0].STAFF.disabled = true;
    }else {
        document.forms[0].STAFF.disabled = false;
    }

}

function newwin(SERVLET_URL){

    if (document.forms[0].DATE.value == ""){
        alert("日付を入力して下さい。");
        return;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

//  url = location.hostname;
//  document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJM";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
