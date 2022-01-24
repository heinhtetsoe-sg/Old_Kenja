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

    //受付番号の大小チェック
    if (!checkedReceptNo()) {
        return;
    }

    //受付番号のチェック
    var no_from  = document.forms[0].RECEPTNO_FROM.value;               //印刷範囲(入力開始)
    var no_to  = document.forms[0].RECEPTNO_TO.value;                   //印刷範囲(入力終了)
    var sns ;
    var ens ;

    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJL";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;

    document.forms[0].RECEPTNO_FROM.value = "";
    document.forms[0].RECEPTNO_TO.value = "";
}

//受付番号の大小チェック
function checkedReceptNo() {

    var no_from = document.forms[0].RECEPTNO_FROM.value;    //受付番号(開始)
    var no_to   = document.forms[0].RECEPTNO_TO.value;      //受付番号(終了)
    var sns ;
    var ens ;
    var irekae = '';

    //ALL'0'の場合に'0'変換(.testで真偽を返す)
    if (/^0+$/.test(no_from)) {
        sns = 0;
    } else if (/^0+$/.test(no_to)){
        ens = 0;
    } else {
        //ZERO･サプレス
        var sn2 = no_from.replace(/^0+/, "");
        var en2 = no_to.replace(/^0+/, "");
        //整数変換
        sns = parseInt(sn2);
        ens = parseInt(en2);
    }

    if(sns > ens) {
        irekae      = no_to;
        no_to       = no_from;
        no_from     = irekae;
        document.forms[0].RECEPTNO_FROM.value = no_from;
        document.forms[0].RECEPTNO_TO.value   = no_to;
    }

    return true;
}