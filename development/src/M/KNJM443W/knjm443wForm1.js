function btn_submit(cmd) {

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function newwin(SERVLET_URL){

    //必須チェック
    if (document.forms[0].SEMESTER.value == "") {
        alert('{rval MSG304}'+'\n'+'　　　( 学期 )');
        return false;
    }
    if (document.forms[0].TESTCD.value == "") {
        alert('{rval MSG304}'+'\n'+'　　　( テスト種別 )');
        return false;
    }
    if (document.forms[0].TEST_COUNT.value == "") {
        alert('{rval MSG304}'+'\n'+'　　　( 回数 )');
        return false;
    }
    if (document.forms[0].SUBCLASS.value == "") {
        alert('{rval MSG304}'+'\n'+'　　　( 科目 )');
        return false;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJM";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
