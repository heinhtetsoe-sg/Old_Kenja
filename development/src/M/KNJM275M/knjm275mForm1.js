function doSubmit() {
    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    sep = "";
    if (document.forms[0].subclassyear.length==0 && document.forms[0].subclassmaster.length==0) {
        alert("データは存在していません。");
        return false;
    }
    for (var i = 0; i < document.forms[0].subclassyear.length; i++)
    {
        attribute3.value = attribute3.value + sep + document.forms[0].subclassyear.options[i].value;
        sep = ",";
    }
    document.forms[0].cmd.value = 'csv';
    document.forms[0].submit();
    return false;
}
function Btn_reset(cmd) {
    result = confirm('{rval MSG106}');
    if (result == false) {
        return false;
    }
}

function OnStaffError()
{
    alert('{rval MSG305}' + '\nレポート添削者登録にて添削対象の科目を登録して下さい。');
    closeWin();
}

/************************************************************ リストtoリスト ***************************************************************/
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
        attribute1 = document.forms[0].subclassmaster;
        attribute2 = document.forms[0].subclassyear;
    } else {
        attribute1 = document.forms[0].subclassyear;
        attribute2 = document.forms[0].subclassmaster;  
    }

    for (var i = 0; i < attribute2.length; i++) {
        y=current1++
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        tempaa[y] = String(attribute2.options[i].value).substr(3,4)+""+String(attribute2.options[i].value).substr(5,6)+""+String(attribute2.options[i].value).substr(0,2)+""+String(attribute2.options[i].value).substr(8,13)+","+y;
        //tempaa[y] = attribute2.options[i].value+","+y;
    }
    for (var i = 0; i < attribute1.length; i++) {
        if ( attribute1.options[i].selected ) {
            y=current1++
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text; 
            tempaa[y] = String(attribute1.options[i].value).substr(3,4)+""+String(attribute1.options[i].value).substr(5,6)+""+String(attribute1.options[i].value).substr(0,2)+""+String(attribute1.options[i].value).substr(8,13)+","+y;
            //tempaa[y] = attribute1.options[i].value+","+y;
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

function moves(sides) {
    var temp5 = new Array();
    var tempc = new Array();
    var tempaa = new Array();
    var current5 = 0;
    var z=0;
    
    if (sides == "left") {
        attribute5 = document.forms[0].subclassmaster;
        attribute6 = document.forms[0].subclassyear;
    } else {
        attribute5 = document.forms[0].subclassyear;
        attribute6 = document.forms[0].subclassmaster;  
    }

    for (var i = 0; i < attribute6.length; i++) {
        z=current5++
        temp5[z] = attribute6.options[i].value;
        tempc[z] = attribute6.options[i].text;
        tempaa[z] = String(attribute6.options[i].value).substr(3,4)+""+String(attribute6.options[i].value).substr(5,6)+""+String(attribute6.options[i].value).substr(0,2)+""+String(attribute6.options[i].value).substr(8,13)+","+z;
        //tempaa[z] = attribute6.options[i].value+","+z;        
    }

    for (var i = 0; i < attribute5.length; i++) {
        z=current5++
        temp5[z] = attribute5.options[i].value;
        tempc[z] = attribute5.options[i].text; 
        tempaa[z] = String(attribute5.options[i].value).substr(3,4)+""+String(attribute5.options[i].value).substr(5,6)+""+String(attribute5.options[i].value).substr(0,2)+""+String(attribute5.options[i].value).substr(8,13)+","+z;
        //tempaa[z] = attribute5.options[i].value+","+z;
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
