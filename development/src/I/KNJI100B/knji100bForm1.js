function doSubmit()
{
    document.forms[0].encoding = "multipart/form-data";
    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    sep = "";
    if (document.forms[0].left_select.length==0) {
        alert('{rval MSG304}'+'（書出し項目）');
        return true;
    }
    for (var i = 0; i < document.forms[0].left_select.length; i++)
    {
        attribute3.value = attribute3.value + sep + document.forms[0].left_select.options[i].value;
        sep = ",";
    }

    sep = schregno = "";
    for (var i=0;i<parent.left_frame.document.forms[0].elements.length;i++)
    {
        var e = parent.left_frame.document.forms[0].elements[i];
        if (e.type=='checkbox' && e.name != "chk_all"){
            if (!e.checked) continue;
            schregno = schregno + sep + e.value;
            sep = ",";
        }
    }
    if (schregno.length == 0){
        alert('{rval MSG304}'+'（生徒）');
        return true;
    }

    document.forms[0].mode.value = parent.left_frame.document.forms[0].mode.value;

    document.forms[0].SCHREGNO.value = schregno;
    document.forms[0].cmd.value = 'csv';
    document.forms[0].submit();
    return false;
}

function temp_clear()
{
    ClearList(document.forms[0].left_select,document.forms[0].left_select);
    ClearList(document.forms[0].right_select,document.forms[0].right_select);
}

//セキュリティーチェック
function OnSecurityError()
{
    alert('{rval MSG300}' + '\n高セキュリティー設定がされています。');
    closeWin();
}

//印刷
function newwin(SERVLET_URL, schoolCd, fileDiv) {
    document.forms[0].encoding = "application/x-www-form-urlencoded";

    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    sep = "";
    if (document.forms[0].left_select.length==0) {
        alert('{rval MSG304}'+'（書出し項目）');
        return true;
    }
    for (var i = 0; i < document.forms[0].left_select.length; i++)
    {
        attribute3.value = attribute3.value + sep + document.forms[0].left_select.options[i].value;
        sep = ",";
    }

    sep = schregno = "";
    for (var i=0;i<parent.left_frame.document.forms[0].elements.length;i++)
    {
        var e = parent.left_frame.document.forms[0].elements[i];
        if (e.type=='checkbox' && e.name != "chk_all"){
            if (!e.checked) continue;
            schregno = schregno + sep + e.value;
            sep = ",";
        }
    }
    if (schregno.length == 0){
        alert('{rval MSG304}'+'（生徒）');
        return true;
    }

    document.forms[0].mode.value = parent.left_frame.document.forms[0].mode.value;
    document.forms[0].SCHREGNO.value = schregno;

    //テンプレート格納場所
    urlVal = document.URL;
    urlVal = urlVal.replace("http://", "");
    var resArray = urlVal.split("/");
    var fieldArray = fileDiv.split(":");
    urlVal = "/usr/local/" + resArray[1] + "/src/etc_system/XLS_TEMP_" + schoolCd + "/CSV_Template" + fieldArray[0] + "." + fieldArray[1];
    document.forms[0].TEMPLATE_PATH.value = urlVal;

    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJX";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
