<!--kanji=漢字-->
function btn_submit(cmd) {

    document.forms[0].encoding = "multipart/form-data";
    if (cmd == "csv"){
        //ブランクチェック
        if (document.forms[0].DATE.value == ""){
            alert('適用日付を入力して下さい。');
            return false;
        }
        //対象学期範囲内チェック
        if (document.forms[0].GAKKI_SDATE.value <= document.forms[0].DATE.value && 
            document.forms[0].DATE.value <= document.forms[0].GAKKI_FDATE.value)
        {
        } else {
            alert('適用日付が学期範囲外です。\n学期の範囲内の日付を入力して下さい。');
            return false;
        }
        //選択チェック
        if (document.forms[0].CLASS_SELECTED.length == 0){
            alert('{rval MSG916}');
            return;
        }
    }

    attribute3 = document.forms[0].selectleft;
    attribute3.value = "";
    sep = "";
    for (var i = 0; i < document.forms[0].CLASS_NAME.length; i++)
    {
        document.forms[0].CLASS_NAME.options[i].selected = 0;
    }
    for (var i = 0; i < document.forms[0].CLASS_SELECTED.length; i++)
    {
        document.forms[0].CLASS_SELECTED.options[i].selected = 1;
        attribute3.value = attribute3.value + sep + document.forms[0].CLASS_SELECTED.options[i].value;
        sep = ",";
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//権限チェック
function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}

//セキュリティーチェック
function OnSecurityError()
{
    alert('{rval MSG300}' + '\n高セキュリティー設定がされています。');
    closeWin();
}

function ClearList(OptionList, TitleName) 
{
    OptionList.length = 0;
}
    
function AllClearList(OptionList, TitleName) 
{
        attribute = document.forms[0].CLASS_NAME;
        ClearList(attribute,attribute);
        attribute = document.forms[0].CLASS_SELECTED;
        ClearList(attribute,attribute);
}

function move1(side,chdt)
{   
    var temp1 = new Array();
    var temp2 = new Array();
    var tempa = new Array();
    var tempb = new Array();
    var tempaa = new Array();
    var current1 = 0;
    var current2 = 0;
    var y=0;
    var attribute;
    
    //assign what select attribute treat as attribute1 and attribute2
    if (side == "left")
    {  
        attribute1 = document.forms[0].CLASS_NAME;
        attribute2 = document.forms[0].CLASS_SELECTED;
    }
    else
    {  
        attribute1 = document.forms[0].CLASS_SELECTED;
        attribute2 = document.forms[0].CLASS_NAME;  
    }

    
    //fill an array with old values
    for (var i = 0; i < attribute2.length; i++)
    {  
        y=current1++
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        if (chdt == 2){
            tempaa[y] = String(attribute2.options[i].value).substr(String(attribute2.options[i].value).indexOf("-"))+","+y; 
        }else {
            tempaa[y] = String(attribute2.options[i].value)+","+y;
        }
    }

    //assign new values to arrays
    for (var i = 0; i < attribute1.length; i++)
    {   
        if ( attribute1.options[i].selected )
        {  
            y=current1++
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text; 
            if (chdt == 2){
                tempaa[y] = String(attribute1.options[i].value).substr(String(attribute1.options[i].value).indexOf("-"))+","+y; 
            }else {
                tempaa[y] = String(attribute1.options[i].value)+","+y;
            }
        }
        else
        {  
            y=current2++
            temp2[y] = attribute1.options[i].value; 
            tempb[y] = attribute1.options[i].text;
        }
    }

    tempaa.sort();

    //generating new options
    for (var i = 0; i < temp1.length; i++)
    {  
        var val = tempaa[i];
        var tmp = val.split(',');

        attribute2.options[i] = new Option();
        attribute2.options[i].value = temp1[tmp[1]];
        attribute2.options[i].text =  tempa[tmp[1]];
    }

    //generating new options
    ClearList(attribute1,attribute1);
    if (temp2.length>0)
    {    
        for (var i = 0; i < temp2.length; i++)
        {   
            attribute1.options[i] = new Option();
            attribute1.options[i].value = temp2[i];
            attribute1.options[i].text =  tempb[i];
        }
    }
}



function moves(sides,chdt)
{   
    var temp5 = new Array();
    var tempc = new Array();
    var tempaa = new Array();
    var current5 = 0;
    var z=0;
    
    //assign what select attribute treat as attribute5 and attribute6
    if (sides == "left")
    {  
        attribute5 = document.forms[0].CLASS_NAME;
        attribute6 = document.forms[0].CLASS_SELECTED;
    }
    else
    {  
        attribute5 = document.forms[0].CLASS_SELECTED;
        attribute6 = document.forms[0].CLASS_NAME;  
    }

    
    //fill an array with old values
    for (var i = 0; i < attribute6.length; i++)
    {  
        z=current5++
        temp5[z] = attribute6.options[i].value;
        tempc[z] = attribute6.options[i].text;
        if (chdt == 2){
               tempaa[z] = String(attribute6.options[i].value).substr(String(attribute6.options[i].value).indexOf("-"))+","+z; 
        }else {
            tempaa[z] = String(attribute6.options[i].value)+","+z;
        }
    }

    //assign new values to arrays
    for (var i = 0; i < attribute5.length; i++)
    {   
        z=current5++
        temp5[z] = attribute5.options[i].value;
        tempc[z] = attribute5.options[i].text; 
        if (chdt == 2){
               tempaa[z] = String(attribute5.options[i].value).substr(String(attribute5.options[i].value).indexOf("-"))+","+z; 
        }else {
            tempaa[z] = String(attribute5.options[i].value)+","+z;
        }
    }

    tempaa.sort();

    //generating new options
    for (var i = 0; i < temp5.length; i++)
    {  
        var val = tempaa[i];
        var tmp = val.split(',');

        attribute6.options[i] = new Option();
        attribute6.options[i].value = temp5[tmp[1]];
        attribute6.options[i].text =  tempc[tmp[1]];
    }

    //generating new options
    ClearList(attribute5,attribute5);

}

//印刷
function newwin(SERVLET_URL, schoolCd, fileDiv) {
    document.forms[0].encoding = "application/x-www-form-urlencoded";
    //ブランクチェック
    if (document.forms[0].DATE.value == "") {
        alert('適用日付を入力して下さい。');
        return false;
    }
    //対象学期範囲内チェック
    if (document.forms[0].GAKKI_SDATE.value <= document.forms[0].DATE.value && 
        document.forms[0].DATE.value <= document.forms[0].GAKKI_FDATE.value
        )
    {
    } else {
        alert('適用日付が学期範囲外です。\n学期の範囲内の日付を入力して下さい。');
        return false;
    }
    //選択チェック
    if (document.forms[0].CLASS_SELECTED.length == 0) {
        alert('{rval MSG916}');
        return;
    }

    attribute3 = document.forms[0].selectleft;
    attribute3.value = "";
    sep = "";
    for (var i = 0; i < document.forms[0].CLASS_NAME.length; i++) {
        document.forms[0].CLASS_NAME.options[i].selected = 0;
    }
    for (var i = 0; i < document.forms[0].CLASS_SELECTED.length; i++) {
        document.forms[0].CLASS_SELECTED.options[i].selected = 1;
        attribute3.value = attribute3.value + sep + document.forms[0].CLASS_SELECTED.options[i].value;
        sep = ",";
    }

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
