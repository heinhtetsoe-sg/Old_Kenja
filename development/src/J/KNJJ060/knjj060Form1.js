function btn_submit(cmd) {

    if (cmd == "csv") {
        if(document.forms[0].CLUB_SELECTED.length == 0){
            alert('{rval MSG916}');
            return false;
        }

        //出力対象クラブ
        attribute3 = document.forms[0].selectdata;
        attribute3.value = "";
        sep = "";
        for (var i = 0; i < document.forms[0].CLUB_NAME.length; i++) {
            document.forms[0].CLUB_NAME.options[i].selected = 0;
        }
        for (var i = 0; i < document.forms[0].CLUB_SELECTED.length; i++) {
            document.forms[0].CLUB_SELECTED.options[i].selected = 1;
            attribute3.value = attribute3.value + sep + document.forms[0].CLUB_SELECTED.options[i].value;
            sep = ",";
        }

        //選択ソート
        attribute5 = document.forms[0].selectsort;
        attribute5.value = "";
        sep = "";
        for (var i = 0; i < document.forms[0].SORT_NAME.length; i++) {
            document.forms[0].SORT_NAME.options[i].selected = 0;
        }
        for (var i = 0; i < document.forms[0].SORT_SELECTED.length; i++) {
            document.forms[0].SORT_SELECTED.options[i].selected = 1;
            attribute5.value = attribute5.value + sep + document.forms[0].SORT_SELECTED.options[i].value;
            sep = ",";
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function newwin(SERVLET_URL){

    if (document.forms[0].CLUB_SELECTED.length == 0) {
        alert('{rval MSG916}');
        return false;
    }
    if(document.forms[0].FROM_DATE.value == "" || document.forms[0].TO_DATE.value == "") {
        alert('対象期間を指定して下さい。');
        return false;
    }

    for (var i = 0; i < document.forms[0].CLUB_NAME.length; i++) {
        document.forms[0].CLUB_NAME.options[i].selected = 0;
    }

    for (var i = 0; i < document.forms[0].CLUB_SELECTED.length; i++) {
        document.forms[0].CLUB_SELECTED.options[i].selected = 1;
    }

    for (var i = 0; i < document.forms[0].SORT_NAME.length; i++) {
        document.forms[0].SORT_NAME.options[i].selected = 0;
    }

    for (var i = 0; i < document.forms[0].SORT_SELECTED.length; i++) {
        document.forms[0].SORT_SELECTED.options[i].selected = 1;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

    document.forms[0].action = SERVLET_URL +"/KNJJ";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}


/**********************/
/* リストtoリスト関係 */
/**********************/
function ClearList(OptionList, TitleName)
{
    OptionList.length = 0;
}

function AllClearList(OptionList, TitleName)
{
        attribute = document.forms[0].CLUB_NAME;
        ClearList(attribute,attribute);
        attribute = document.forms[0].CLUB_SELECTED;
        ClearList(attribute,attribute);
}
function move1(side, kind) {
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
    if (side == "left") {
        if (kind == 'club') {
            attribute1 = document.forms[0].CLUB_NAME;
            attribute2 = document.forms[0].CLUB_SELECTED;
        } else {
            attribute1 = document.forms[0].SORT_NAME;
            attribute2 = document.forms[0].SORT_SELECTED;
        }
    } else {
        if (kind == 'club') {
            attribute1 = document.forms[0].CLUB_SELECTED;
            attribute2 = document.forms[0].CLUB_NAME;
        } else {
            attribute1 = document.forms[0].SORT_SELECTED;
            attribute2 = document.forms[0].SORT_NAME;
        }
    }


    //fill an array with old values
    for (var i = 0; i < attribute2.length; i++) {
        y=current1++
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        tempaa[y] = attribute2.options[i].value+","+y;
    }
    
    //assign new values to arrays
    for (var i = 0; i < attribute1.length; i++) {
        if ( attribute1.options[i].selected ) {
            y=current1++
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text;
            tempaa[y] = attribute1.options[i].value+","+y;
        } else {
            y=current2++
            temp2[y] = attribute1.options[i].value;
            tempb[y] = attribute1.options[i].text;
        }
    }

    if (side == 'left' && kind == 'sort') {
    	var sortflag = false;
        for(var i=0; i < temp1.length; i++){
            if(temp1[i] == 'EXECUTIVECD' || temp1[i]=='EXECUTIVECD2'){
                if (!sortflag) {
                    sortflag = true;
                } else {
                    alert('役職は二つ選択することはできません。');
                    return;
                }
            }
        }
    }
    
    if (kind == 'club') {
        tempaa.sort();
    }

    //generating new options
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
function moves(sides, kind) {
    var temp5 = new Array();
    var tempc = new Array();
    var tempaa = new Array();
    var current5 = 0;
    var z=0;

    //assign what select attribute treat as attribute5 and attribute6
    if (sides == "left") {
        if (kind == 'club') {
            attribute5 = document.forms[0].CLUB_NAME;
            attribute6 = document.forms[0].CLUB_SELECTED;
        } else {
            attribute5 = document.forms[0].SORT_NAME;
            attribute6 = document.forms[0].SORT_SELECTED;
        }
    } else {
        if (kind == 'club') {
            attribute5 = document.forms[0].CLUB_SELECTED;
            attribute6 = document.forms[0].CLUB_NAME;
        } else {
            attribute5 = document.forms[0].SORT_SELECTED;
            attribute6 = document.forms[0].SORT_NAME;
        }
    }

    //fill an array with old values
    for (var i = 0; i < attribute6.length; i++) {
        z=current5++
        temp5[z] = attribute6.options[i].value;
        tempc[z] = attribute6.options[i].text;
        tempaa[z] = attribute6.options[i].value+","+z;
    }

    //assign new values to arrays
    for (var i = 0; i < attribute5.length; i++) {
        z=current5++
        temp5[z] = attribute5.options[i].value;
        tempc[z] = attribute5.options[i].text;
        tempaa[z] = attribute5.options[i].value+","+z;
    }

    if (kind == 'club') {
        tempaa.sort();
    }

    //generating new options
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


//セキュリティチェック
function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}
