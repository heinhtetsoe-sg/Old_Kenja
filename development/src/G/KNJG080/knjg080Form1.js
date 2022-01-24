function btn_submit(cmd) {

    if (cmd == "clear") {
        result = confirm('{rval MSG106}');
        if (result == false) {
            return false;
        }
    }

    //チェック
    rightList = document.forms[0].CATEGORY_NAME;
    leftList  = document.forms[0].CATEGORY_SELECTED;
    if (cmd == "update") {
        if (rightList.length==0 && leftList.length==0) {
            alert('{rval MSG303}');
            return false;
        }
    }

    //生徒、職員の切り替え
    var studentsvalue = document.forms[0].STUDENTSVALUE.value;
    var staffvalue    = document.forms[0].STAFFVALUE.value;
    var guardianvalue = document.forms[0].GUARDIANVALUE.value;

    if (studentsvalue == 1) {
        //生徒一覧
        attribute3 = document.forms[0].selectdata2;
        attribute3.value = "";
        sep = "";
        if (rightList.length > 0){
            for (var i = 0; i < rightList.length; i++)
            {
                attribute3.value = attribute3.value + sep + rightList.options[i].value;
                sep = ",";
            }
        }
        //対象者一覧
        attribute4 = document.forms[0].selectdata;
        attribute4.value = "";
        sep = "";
        if (leftList.length > 0){
            for (var i = 0; i < leftList.length; i++)
            {
                attribute4.value = attribute4.value + sep + leftList.options[i].value;
                sep = ",";
            }
        }
    } else if (staffvalue == 1) {
        //職員一覧
        staffattribute3 = document.forms[0].selectstaffdata2;
        staffattribute3.value = "";
        sep = "";
        if (rightList.length > 0){
            for (var i = 0; i < rightList.length; i++)
            {
                staffattribute3.value = staffattribute3.value + sep + rightList.options[i].value;
                sep = ",";
            }
        }
        //職員対象者一覧
        staffattribute4 = document.forms[0].selectstaffdata;
        staffattribute4.value = "";
        sep = "";
        if (leftList.length > 0){
            for (var i = 0; i < leftList.length; i++)
            {
                staffattribute4.value = staffattribute4.value + sep + leftList.options[i].value;
                sep = ",";
            }
        }
    } else if (guardianvalue == 1) {
        //生徒一覧(保護者)
        guardianattribute3 = document.forms[0].selectguardiandata2;
        guardianattribute3.value = "";
        sep = "";
        if (rightList.length > 0){
            for (var i = 0; i < rightList.length; i++)
            {
                guardianattribute3.value = guardianattribute3.value + sep + rightList.options[i].value;
                sep = ",";
            }
        }
        //対象者一覧(保護者)
        guardianattribute4 = document.forms[0].selectguardiandata;
        guardianattribute4.value = "";
        sep = "";
        if (leftList.length > 0){
            for (var i = 0; i < leftList.length; i++)
            {
                guardianattribute4.value = guardianattribute4.value + sep + leftList.options[i].value;
                sep = ",";
            }
        }
    }
    
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
/***************************************************************************/
/**************************** List to List 関係 ****************************/
/***************************************************************************/
function ClearList(OptionList, TitleName) {
    OptionList.length = 0;
}
    
function AllClearList(OptionList, TitleName) {
    attribute = document.forms[0].CATEGORY_NAME;
    ClearList(attribute,attribute);
    attribute = document.forms[0].CATEGORY_SELECTED;
    ClearList(attribute,attribute);
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
    
    //assign what select attribute treat as attribute1 and attribute2
    if (side == "left") {
        attribute1 = document.forms[0].CATEGORY_NAME;
        attribute2 = document.forms[0].CATEGORY_SELECTED;
    } else {
        attribute1 = document.forms[0].CATEGORY_SELECTED;
        attribute2 = document.forms[0].CATEGORY_NAME;  
    }

    
    //fill an array with old values
    for (var i = 0; i < attribute2.length; i++) {
        y=current1++
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        tempaa[y] = String(attribute2.options[i].text).substring(0,12)+","+y;
    }

    //assign new values to arrays
    for (var i = 0; i < attribute1.length; i++) {
        if ( attribute1.options[i].selected )
        {  
            y=current1++
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text; 
            tempaa[y] = String(attribute1.options[i].text).substring(0,12)+","+y;
        } else {
            y=current2++
            temp2[y] = attribute1.options[i].value; 
            tempb[y] = attribute1.options[i].text;
        }
    }

    tempaa.sort();

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

function moves(sides) {   
    var temp5 = new Array();
    var tempc = new Array();
    var tempaa = new Array();
    var current5 = 0;
    var z=0;
    
    //assign what select attribute treat as attribute5 and attribute6
    if (sides == "left") {
        attribute5 = document.forms[0].CATEGORY_NAME;
        attribute6 = document.forms[0].CATEGORY_SELECTED;
    } else {
        attribute5 = document.forms[0].CATEGORY_SELECTED;
        attribute6 = document.forms[0].CATEGORY_NAME;  
    }


    //fill an array with old values
    for (var i = 0; i < attribute6.length; i++) {
        z=current5++
        temp5[z] = attribute6.options[i].value;
        tempc[z] = attribute6.options[i].text;
        tempaa[z] = String(attribute6.options[i].text).substring(0,12)+","+z;
    }

    //assign new values to arrays
    for (var i = 0; i < attribute5.length; i++) {
        z=current5++
        temp5[z] = attribute5.options[i].value;
        tempc[z] = attribute5.options[i].text; 
        tempaa[z] = String(attribute5.options[i].text).substring(0,12)+","+z;
    }

    tempaa.sort();

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

