function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

/************************************** 帳票 *******************************************/
function newwin(SERVLET_URL) {
    document.forms[0].PRGID.value = "KNJA233E";

    var appdate;
    var chargediv;
    var subclass;
    var attend;
    var groupcd;
    var staffcd;
    var sep;
    var j;
    
    appdate = "";
    chargediv = "";
    subclass = "";
    attend = "";
    groupcd = "";
    staffcd = "";
    sep = "";

    if (document.forms[0].CLASS_SELECTED.length == 0) {
        alert('{rval MSG916}');
        return false;
    }

    var obj1 = document.forms[0].DATE;
    if (obj1.value == '') {
        alert("日付が不正です。");
        obj1.focus();
        return false;
    }


    for (var i = 0; i < document.forms[0].CLASS_NAME.length; i++) {
        document.forms[0].CLASS_NAME.options[i].selected = 0;
    }

    for (var i = 0; i < document.forms[0].CLASS_SELECTED.length; i++) {
        var val = document.forms[0].CLASS_SELECTED[i].value;
        if (val != ''){
            var tmp = val.split(',');

            appdate   += sep+tmp[7];
            chargediv += sep+tmp[6];
            subclass  += sep+tmp[0];
            attend    += sep+tmp[4];
            groupcd   += sep+tmp[5];
            staffcd   += sep+tmp[2];
            sep = ",";
        }
    }

    if (attend != '' && groupcd != ''){
        document.forms[0].ATTENDCLASSCD.value = attend;
        document.forms[0].GROUPCD.value = groupcd;
        document.forms[0].NAME_SHOW.value = staffcd;
        document.forms[0].CHARGEDIV.value = chargediv;
        document.forms[0].APPDATE.value = appdate;


        action = document.forms[0].action;
        target = document.forms[0].target;

//        url = location.hostname;
//        document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
        document.forms[0].action = SERVLET_URL +"/KNJA";
        document.forms[0].target = "_blank";
        document.forms[0].submit();

        document.forms[0].action = action;
        document.forms[0].target = target;

        return false;
    }else{
        alert("チェックボックスが選択されておりません。");
        return true;
    }
}



function ClearList(OptionList, TitleName) {
    OptionList.length = 0;
}

function move1(side) {
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
        attribute1 = document.forms[0].CLASS_NAME;
        attribute2 = document.forms[0].CLASS_SELECTED;
    } else {
        attribute1 = document.forms[0].CLASS_SELECTED;
        attribute2 = document.forms[0].CLASS_NAME;  
    }

    for (var i = 0; i < attribute2.length; i++) {
        y=current1++
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;

        var valS = attribute2.options[i].value.split(',');
        tmpS = valS[4]+"_"+valS[6]+"_"+valS[2];
        tempaa[y] = tmpS+"__"+y;
    }

    for (var i = 0; i < attribute1.length; i++) {
        if ( attribute1.options[i].selected ) {
            y=current1++
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text; 

            var valS = attribute1.options[i].value.split(',');
            tmpS = valS[4]+"_"+valS[6]+"_"+valS[2];
            tempaa[y] = tmpS+"__"+y;
        } else {
            y=current2++
            temp2[y] = attribute1.options[i].value; 
            tempb[y] = attribute1.options[i].text;
        }
    }

    tempaa.sort();

    for (var i = 0; i < temp1.length; i++) {
        var val = tempaa[i];
        var tmp = val.split('__');

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

function moves(sides) {
    var temp5 = new Array();
    var tempc = new Array();
    var tempaa = new Array();
    var current5 = 0;
    var z=0;
    
    if (sides == "left") {
        attribute5 = document.forms[0].CLASS_NAME;
        attribute6 = document.forms[0].CLASS_SELECTED;
    } else {
        attribute5 = document.forms[0].CLASS_SELECTED;
        attribute6 = document.forms[0].CLASS_NAME;  
    }

    for (var i = 0; i < attribute6.length; i++) {
        z=current5++
        temp5[z] = attribute6.options[i].value;
        tempc[z] = attribute6.options[i].text;

        var valS = attribute6.options[i].value.split(',');
        tmpS = valS[4]+"_"+valS[6]+"_"+valS[2];
        tempaa[z] = tmpS+"__"+z;
    }

    for (var i = 0; i < attribute5.length; i++) {
        z=current5++
        temp5[z] = attribute5.options[i].value;
        tempc[z] = attribute5.options[i].text; 

        var valS = attribute5.options[i].value.split(',');
        tmpS = valS[4]+"_"+valS[6]+"_"+valS[2];
        tempaa[z] = tmpS+"__"+z;
    }

    tempaa.sort();

    for (var i = 0; i < temp5.length; i++) {
        var val = tempaa[i];
        var tmp = val.split('__');

        attribute6.options[i] = new Option();
        attribute6.options[i].value = temp5[tmp[1]];
        attribute6.options[i].text =  tempc[tmp[1]];
    }

    ClearList(attribute5,attribute5);

}

window.onload = function () {
    document.forms[0].DATE.attachEvent('onchange', function(){document.forms[0].submit();});
    document.forms[0].DATE.attachEvent('onkeydown', function(e){if (e.keyCode == 13) {document.forms[0].submit();}});
};

