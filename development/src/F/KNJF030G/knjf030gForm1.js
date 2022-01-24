function btn_submit(cmd) {

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

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL) {

    //出力対象チェック
    if (document.forms[0].CLASS_SELECTED.length == 0) {
        alert('{rval MSG916}');
        return;
    }
    //印刷対象チェック
    if ( document.forms[0].CHECK1.checked==false &&
         document.forms[0].CHECK2.checked==false &&
         document.forms[0].CHECK3.checked==false &&
         document.forms[0].CHECK4.checked==false &&
         document.forms[0].CHECK5.checked==false &&
         document.forms[0].CHECK6.checked==false &&
         document.forms[0].CHECK7.checked==false &&
         document.forms[0].CHECK8.checked==false &&
         document.forms[0].CHECK9.checked==false &&
         document.forms[0].CHECK10.checked==false &&
         document.forms[0].CHECK11.checked==false &&
         document.forms[0].CHECK12.checked==false &&
         document.forms[0].CHECK13.checked==false &&
         document.forms[0].CHECK14.checked==false &&
         document.forms[0].CHECK15.checked==false &&
         document.forms[0].CHECK16.checked==false &&
         document.forms[0].CHECK17.checked==false &&
         document.forms[0].CHECK18.checked==false &&
         document.forms[0].CHECK19.checked==false &&
         document.forms[0].CHECK20.checked==false &&
         document.forms[0].CHECK21.checked==false &&
         document.forms[0].CHECK22.checked==false &&
         document.forms[0].CHECK23.checked==false &&
         document.forms[0].CHECK24.checked==false &&
         document.forms[0].CHECK25.checked==false &&
         document.forms[0].CHECK26.checked==false &&
         document.forms[0].CHECK27.checked==false &&
         document.forms[0].CHECK28.checked==false &&
         document.forms[0].CHECK29.checked==false
       )
    {
        alert('{rval MSG916}');
        return;
    }

    //作成日付チェック
    if (document.forms[0].DATE.value == "") {
        alert('作成日付を指定して下さい。');
        return;
    }
    //提出日チェック
    if (document.forms[0].CHECK13.checked || document.forms[0].CHECK26.checked) {
        if (document.forms[0].PRESENT_DATE.value == "") {
            alert('提出日を指定して下さい。');
            return;
        }
    }
    //検査日時, 検査機関チェック
    if (document.forms[0].CHECK14.checked || document.forms[0].CHECK27.checked) {
        if (document.forms[0].INSPECTION_FROM.value == "" || document.forms[0].INSPECTION_TO.value == "") {
            alert('検査日時を指定して下さい。');
            return;
        }
        if (document.forms[0].INSPECTION_BODY.value == "") {
            alert('検査機関を入力して下さい。');
            return;
        }
    }

    for (var i = 0; i < document.forms[0].CLASS_NAME.length; i++) {
        document.forms[0].CLASS_NAME.options[i].selected = 0;
    }

    for (var i = 0; i < document.forms[0].CLASS_SELECTED.length; i++) {
        document.forms[0].CLASS_SELECTED.options[i].selected = 1;
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

function ClearList(OptionList, TitleName) {
    OptionList.length = 0;
}
    
function AllClearList(OptionList, TitleName) {
    attribute = document.forms[0].CLASS_NAME;
    ClearList(attribute,attribute);
    attribute = document.forms[0].CLASS_SELECTED;
    ClearList(attribute,attribute);
}

function move1(side,chdt) {
    var temp1 = new Array();
    var temp2 = new Array();
    var tempa = new Array();
    var tempb = new Array();
    var tempaa = new Array();   // 2004/01/26
    var current1 = 0;
    var current2 = 0;
    var y=0;
    var attribute;

    //assign what select attribute treat as attribute1 and attribute2
    if (side == "left") {  
        attribute1 = document.forms[0].CLASS_NAME;
        attribute2 = document.forms[0].CLASS_SELECTED;
    } else {  
        attribute1 = document.forms[0].CLASS_SELECTED;
        attribute2 = document.forms[0].CLASS_NAME;  
    }

    
    //fill an array with old values
    for (var i = 0; i < attribute2.length; i++) {
        y=current1++
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        if (chdt == 2) {
            tempaa[y] = String(attribute2.options[i].value).substr(String(attribute2.options[i].value).indexOf("-"))+","+y; 
        } else {
            tempaa[y] = String(attribute2.options[i].value)+","+y; // NO001
        }
    }

    //assign new values to arrays
    for (var i = 0; i < attribute1.length; i++) {
        if ( attribute1.options[i].selected ) {
            y=current1++
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text; 
            if (chdt == 2) {
                tempaa[y] = String(attribute1.options[i].value).substr(String(attribute1.options[i].value).indexOf("-"))+","+y; 
            } else {
                tempaa[y] = String(attribute1.options[i].value)+","+y; // NO001
            }
        } else {
            y=current2++
            temp2[y] = attribute1.options[i].value; 
            tempb[y] = attribute1.options[i].text;
        }
    }

    tempaa.sort();  // 2004/01/26

    //generating new options // 2004/01/26
    for (var i = 0; i < temp1.length; i++) {
        var val = tempaa[i];
        var tmp = val.split(',');

        attribute2.options[i] = new Option();
        attribute2.options[i].value = temp1[tmp[1]];
        attribute2.options[i].text =  tempa[tmp[1]];
    }

    //generating new options
    ClearList(attribute1,attribute1);
    if (temp2.length>0) {
        for (var i = 0; i < temp2.length; i++) {
            attribute1.options[i] = new Option();
            attribute1.options[i].value = temp2[i];
            attribute1.options[i].text =  tempb[i];
        }
    }
}

function moves(sides, chdt) {
    var temp5 = new Array();
    var tempc = new Array();
    var tempaa = new Array();   // 2004/01/26
    var current5 = 0;
    var z=0;

    //assign what select attribute treat as attribute5 and attribute6
    if (sides == "left") {
        attribute5 = document.forms[0].CLASS_NAME;
        attribute6 = document.forms[0].CLASS_SELECTED;
    } else {
        attribute5 = document.forms[0].CLASS_SELECTED;
        attribute6 = document.forms[0].CLASS_NAME;  
    }

    //fill an array with old values
    for (var i = 0; i < attribute6.length; i++) {
        z=current5++
        temp5[z] = attribute6.options[i].value;
        tempc[z] = attribute6.options[i].text;
        if (chdt == 2) {
            tempaa[z] = String(attribute6.options[i].value).substr(String(attribute6.options[i].value).indexOf("-"))+","+z; 
        } else {
            tempaa[z] = String(attribute6.options[i].value)+","+z; // NO001
        }
    }

    //assign new values to arrays
    for (var i = 0; i < attribute5.length; i++) {
        z=current5++
        temp5[z] = attribute5.options[i].value;
        tempc[z] = attribute5.options[i].text; 
        if (chdt == 2) {
            tempaa[z] = String(attribute5.options[i].value).substr(String(attribute5.options[i].value).indexOf("-"))+","+z; 
        } else {
            tempaa[z] = String(attribute5.options[i].value)+","+z; // NO001
        }
    }

    tempaa.sort();  // 2004/01/26

    //generating new options // 2004/01/26
    for (var i = 0; i < temp5.length; i++) {
        var val = tempaa[i];
        var tmp = val.split(',');

        attribute6.options[i] = new Option();
        attribute6.options[i].value = temp5[tmp[1]];
        attribute6.options[i].text =  tempc[tmp[1]];
    }

    //generating new options
    ClearList(attribute5,attribute5);

}
