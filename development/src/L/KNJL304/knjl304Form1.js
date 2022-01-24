function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//印刷
function newwin(SERVLET_URL){
    if (document.forms[0].EXAMNO_ST.value == ""){
        alert("印刷開始受験番号を指定して下さい");
        return;
    }
    if (document.forms[0].EXAMNO_ED.value == ""){
        alert("印刷終了受験番号を指定して下さい");
        return;
    }

    var sno  = document.forms[0].EXAMNO_ST.value;               //印刷範囲(入力開始)
    var eno  = document.forms[0].EXAMNO_ED.value;               //印刷範囲(入力終了)
    var sns ;
    var ens ;
    //ALL'0'の場合に'0'変換(.testで真偽を返す)
    if (/^0+$/.test(sno)) {
        sns = 0;
    } else if (/^0+$/.test(eno)){
        ens = 0;
    } else {
        //ZERO･サプレス
        var sn2 = sno.replace(/^0+/, "");
        var en2 = eno.replace(/^0+/, "");
        //整数変換
        sns = parseInt(sn2);
        ens = parseInt(en2);
    }
    //開始終了印刷範囲のチェック
    if(sns > ens){
        alert("受付番号の大小が不正です");
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

