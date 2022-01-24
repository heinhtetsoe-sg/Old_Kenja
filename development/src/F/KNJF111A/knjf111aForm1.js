//ＣＳＶ出力
function btn_submit(cmd) {
    if (cmd != 'year') {
        var sdate = document.forms[0].SDATE;
        var edate = document.forms[0].EDATE;
        var chk_sdate = document.forms[0].YEAR.value+'/04/01';
        var chk_edate = parseInt(document.forms[0].YEAR.value)+1+'/03/31';
        var irekae = "";

        //日付入力チェック
        if (sdate.value == "") {
            alert("日付が不正です。");
            sdate.focus();
            return false;
        }
        if (edate.value == "") {
            alert("日付が不正です。");
            edate.focus();
            return false;
        }

        //日付の大小比較
        if(sdate.value > edate.value){
            irekae      = sdate.value;
            sdate.value = edate.value;
            edate.value = irekae;
        }

        //日付範囲チェック
        if ((sdate.value < chk_sdate) || (sdate.value > chk_edate)) {
            sdate.focus();
            alert('{rval MSG916}\n'+chk_sdate+'～'+chk_edate);
            return false;
        }
        if ((edate.value < chk_sdate) || (edate.value > chk_edate)) {
            edate.focus();
            alert('{rval MSG916}\n'+chk_sdate+'～'+chk_edate);
            return false;
        }

        if (cmd == "csv") {
            if (document.forms[0].CATEGORY_SELECTED.length == 0) {
                alert('{rval MSG916}');
                return;
            }
        }

        attribute3 = document.forms[0].selectdata;
        attribute3.value = "";
        sep = "";
        for (var i = 0; i < document.forms[0].CATEGORY_NAME.length; i++) {
            document.forms[0].CATEGORY_NAME.options[i].selected = 0;
        }

        for (var i = 0; i < document.forms[0].CATEGORY_SELECTED.length; i++) {
            document.forms[0].CATEGORY_SELECTED.options[i].selected = 1;
            attribute3.value = attribute3.value + sep + document.forms[0].CATEGORY_SELECTED.options[i].value;
            sep = ",";
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}


//印刷
function newwin(SERVLET_URL){

    var sdate = document.forms[0].SDATE;
    var edate = document.forms[0].EDATE;
    var chk_sdate = document.forms[0].YEAR.value+'/04/01';
    var chk_edate = parseInt(document.forms[0].YEAR.value)+1+'/03/31';
    var irekae = "";

    //日付入力チェック
    if (sdate.value == "") {
        alert("日付が不正です。");
        sdate.focus();
        return false;
    }
    if (edate.value == "") {
        alert("日付が不正です。");
        edate.focus();
        return false;
    }

    //日付の大小比較
    if(sdate.value > edate.value){
        irekae      = sdate.value;
        sdate.value = edate.value;
        edate.value = irekae;
    }

    //日付範囲チェック
    if ((sdate.value < chk_sdate) || (sdate.value > chk_edate)) {
        sdate.focus();
        alert('{rval MSG916}\n'+chk_sdate+'～'+chk_edate);
        return false;
    }
    if ((edate.value < chk_sdate) || (edate.value > chk_edate)) {
        edate.focus();
        alert('{rval MSG916}\n'+chk_sdate+'～'+chk_edate);
        return false;
    }

    if (document.forms[0].CATEGORY_SELECTED.length == 0) {
        alert('{rval MSG916}');
        return;
    }

    for (var i = 0; i < document.forms[0].CATEGORY_NAME.length; i++) {
        document.forms[0].CATEGORY_NAME.options[i].selected = 0;
    }

    for (var i = 0; i < document.forms[0].CATEGORY_SELECTED.length; i++) {
        document.forms[0].CATEGORY_SELECTED.options[i].selected = 1;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJF";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

function ClearList(OptionList, TitleName) 
{
    OptionList.length = 0;
}
    
function AllClearList(OptionList, TitleName) 
{
        attribute = document.forms[0].CATEGORY_NAME;
        ClearList(attribute,attribute);
        attribute = document.forms[0].CATEGORY_SELECTED;
        ClearList(attribute,attribute);
}

//クラス選択／取消（一部）
function move1(side)
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
    
    if (side == "left") {
        attribute1 = document.forms[0].CATEGORY_NAME;
        attribute2 = document.forms[0].CATEGORY_SELECTED;
    } else {
        attribute1 = document.forms[0].CATEGORY_SELECTED;
        attribute2 = document.forms[0].CATEGORY_NAME;  
    }

    for (var i = 0; i < attribute2.length; i++) {
        y=current1++
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        tempaa[y] = attribute2.options[i].text+","+y;
    }

    for (var i = 0; i < attribute1.length; i++) {
        if ( attribute1.options[i].selected ) {
            y=current1++
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text; 
            tempaa[y] = attribute1.options[i].text+","+y;
        } else {
            y=current2++
            temp2[y] = attribute1.options[i].value; 
            tempb[y] = attribute1.options[i].text;
        }
    }

    tempaa.sort();

    for (var i = 0; i < temp1.length; i++) {
        var val = tempaa[i];
        var tmp = val.split(',');

        attribute2.options[i] = new Option();
        attribute2.options[i].value = temp1[tmp[1]];
        attribute2.options[i].text =  tempa[tmp[1]];
    }

    ClearList(attribute1,attribute1);
    if (temp2.length>0) {
        for (var i = 0; i < temp2.length; i++) {
            attribute1.options[i] = new Option();
            attribute1.options[i].value = temp2[i];
            attribute1.options[i].text =  tempb[i];
        }
    }

}

//クラス選択／取消（全部）
function moves(sides)
{   
    var temp5 = new Array();
    var tempc = new Array();
    var tempaa = new Array();
    var current5 = 0;
    var z=0;
    
    if (sides == "left") {
        attribute5 = document.forms[0].CATEGORY_NAME;
        attribute6 = document.forms[0].CATEGORY_SELECTED;
    } else {
        attribute5 = document.forms[0].CATEGORY_SELECTED;
        attribute6 = document.forms[0].CATEGORY_NAME;  
    }

    for (var i = 0; i < attribute6.length; i++) {
        z=current5++
        temp5[z] = attribute6.options[i].value;
        tempc[z] = attribute6.options[i].text;
        tempaa[z] = attribute6.options[i].text+","+z;
    }

    for (var i = 0; i < attribute5.length; i++) {
        z=current5++
        temp5[z] = attribute5.options[i].value;
        tempc[z] = attribute5.options[i].text; 
        tempaa[z] = attribute5.options[i].text+","+z;
    }

    tempaa.sort();

    for (var i = 0; i < temp5.length; i++) {
        var val = tempaa[i];
        var tmp = val.split(',');

        attribute6.options[i] = new Option();
        attribute6.options[i].value = temp5[tmp[1]];
        attribute6.options[i].text =  tempc[tmp[1]];
    }

    ClearList(attribute5,attribute5);

}
