function btn_submit(cmd) {

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL){

     if (document.forms[0].APPLICANTDIV.value == ""){
          alert("入試制度を指定して下さい");
          return;
     }

     if (document.forms[0].TESTDIV.value == ""){
          alert("入試区分を指定して下さい");
          return;
     }

    //点数チェック
     var max = document.forms[0].MAX_SCORE;   //最高点
     var min = document.forms[0].MIN_SCORE;   //最低点

    if(max.value == ""){
        alert("最高点を入力して下さい。");
        max.focus();
        return;
    }
    if(min.value == ""){
        alert("最低点を入力して下さい。");
        min.focus();
        return;
    }
    if(isNaN(max.value) < isNaN(min.value)){
        alert("点数の大小が不正です。");
        max.focus();
        return;
    }
    if(max.value > 500){
        alert("最高点が範囲外です。（500点以下）");
        max.focus();
        return;
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
