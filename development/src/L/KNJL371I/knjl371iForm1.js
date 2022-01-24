function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function newwin(SERVLET_URL) {
    var i;

    //受験番号Fromの0埋め
    examnoZeroPadding(document.forms[0].BANGOU1);

    //受験番号Toの0埋め
    examnoZeroPadding(document.forms[0].BANGOU2);

    if (document.forms[0].BANGOU1.value > 0 && document.forms[0].BANGOU2.value > 0) {
        select1 = document.forms[0].BANGOU1;
        select2 = document.forms[0].BANGOU2;
        wrkindex = 0;

        if (select1.value > select2.value) {
            wrkindex = select1.value;
            select1.value = select2.value;
            select2.value = wrkindex;
        }
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

    //    url = location.hostname;
    //    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL + '/KNJL';
    document.forms[0].target = '_blank';
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

function examnoZeroPadding(examno) {
    if (examno.value > 0) {
        var max = 4; //最大4桁
        max = max - String(examno.value).length;
        for (var idx = 0; idx < max; idx++) {
            //受験番号の0埋め
            examno.value = '0' + examno.value;
        }
    }
}
