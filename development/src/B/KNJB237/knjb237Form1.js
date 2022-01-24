function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL) {
    //必須チェック（基本時間割）
    if (document.forms[0].PTRN_TITLE.value == "") {
        alert('{rval MSG916}'+'\n( 基本時間割：未選択 )');
        return;
    }

    //日付チェック
    sd = document.forms[0].CHK_SEM_SDATE.value;
    ed = document.forms[0].CHK_SEM_EDATE.value;
    d = document.forms[0].DATE.value;
    m = "";
    err = "";
    //必須チェック
    if (d == "") {
        m = '未入力';
        err = 1;
    }
    //範囲チェック（学期内）
    if (!err && (d < sd || ed < d)) {
        m = sd + '　～　' + ed;
        err = 1;
    }
    //エラー出力
    if (err) {
        alert('{rval MSG916}'+'\n( 講座日付：' + m + ' )');
        document.forms[0].DATE.focus();
        return;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJB";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
