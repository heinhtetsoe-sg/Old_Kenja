function btn_submit(cmd) {
    if (cmd == 'knjl223r') {
        var henkan;
        henkan = toInteger(document.forms[0].E_KESSEKI.value);
        document.forms[0].E_KESSEKI.value = henkan;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//印刷
function newwin(SERVLET_URL){
    if (document.forms[0].S_KESSEKI.value == ""){
        alert("欠席日数の開始を指定して下さい");
        return;
    }
    if (document.forms[0].E_KESSEKI.value == ""){
        alert("欠席日数の終了を指定して下さい");
        return;
    }

    var sno  = document.forms[0].S_KESSEKI.value;               //印刷範囲(日付開始)
    var eno  = document.forms[0].E_KESSEKI.value;               //印刷範囲(日付終了)
    //開始終了印刷範囲のチェック
    if(sno > eno){
        alert("日数の大小が不正です");
        return;
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

