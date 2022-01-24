function btn_submit(cmd) {
    if (cmd === 'update') {
        if (document.forms[0].KOUNYU_L_M_S_CD.value == "") {
            alert('{rval MSG301}' + '(品名等)');
            return false;
        }
    } else if (cmd == 'delete' && !confirm('{rval MSG103}')){
        return false;
    }
    
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//コンボにテキストの追加
function add(lmsCd, cmbCnt) {
    var temp1    = new Array();
    var tempa    = new Array();
    var cmbLen   = document.forms[0].KOUNYU_L_M_S_CD.length;
    var textVal  = document.forms[0].LEVY_S_NAME.value
    var sNameArr = document.forms[0].LEVY_S_NAMES.value.split(':');

    if (textVal == "") {
        alert("{rval MSG901}\n文字を入力してください。")
        return false;
    }

    for (var i = 0; i < sNameArr.length; i++) {
        if (textVal == sNameArr[i]) {
            alert("同じ名称はセットできません。");
            return false;
        }
    }
    document.forms[0].KOUNYU_L_M_S_CD.options[cmbCnt]          = new Option();
    document.forms[0].KOUNYU_L_M_S_CD.options[cmbCnt].value    = lmsCd + ":" + textVal;
    document.forms[0].KOUNYU_L_M_S_CD.options[cmbCnt].text     = lmsCd + ":" + textVal;
    document.forms[0].KOUNYU_L_M_S_CD.options[cmbCnt].selected = true;
}

//印刷
function newwin(SERVLET_URL){

    if (document.forms[0].category_selected.length == 0)
    {
        alert('{rval MSG916}');
        return;
    }

    for (var i = 0; i < document.forms[0].category_name.length; i++)
    {  
        document.forms[0].category_name.options[i].selected = 0;
    }

    for (var i = 0; i < document.forms[0].category_selected.length; i++)
    {  
        document.forms[0].category_selected.options[i].selected = 1;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJP";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}