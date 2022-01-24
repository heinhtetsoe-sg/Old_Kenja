function btn_submit(cmd) {
    document.forms[0].encoding = 'multipart/form-data';

    if (cmd == 'exec' && !confirm('処理を開始します。よろしいでしょうか？')) {
        return true;
    }

    if (cmd == 'exec') {
        if (document.forms[0].OUTPUT[1].checked == false) {
            cmd = 'csv';
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL, schoolCd, fileDiv) {
    document.forms[0].encoding = 'application/x-www-form-urlencoded';
    if (document.forms[0].OUTPUT[3].checked != true) {
        btn_submit('exec');
    } else {
        //テンプレート格納場所
        urlVal = document.URL;
        urlVal = urlVal.replace('http://', '');
        var resArray = urlVal.split('/');
        var fieldArray = fileDiv.split(':');
        urlVal = '/usr/local/' + resArray[1] + '/src/etc_system/XLS_TEMP_' + schoolCd + '/CSV_Template' + fieldArray[0] + '.' + fieldArray[1];
        document.forms[0].TEMPLATE_PATH.value = urlVal;

        action = document.forms[0].action;
        target = document.forms[0].target;

        //    url = location.hostname;
        //    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
        document.forms[0].action = SERVLET_URL + '/KNJX';
        document.forms[0].target = '_blank';
        document.forms[0].submit();

        document.forms[0].action = action;
        document.forms[0].target = target;
    }
}
