function btn_submit(cmd) {

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL){


   //受験番号指定で受験番号未入力の場合はエラー
   if (document.forms[0].PRINT_DIV[1].checked == true) {
       if (document.forms[0].PASS_EXAMNO.value == "") {
           alert('{rval MSG304}');
           return;
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
