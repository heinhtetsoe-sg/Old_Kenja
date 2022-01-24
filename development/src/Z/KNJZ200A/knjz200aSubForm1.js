function btn_submit(cmd) {

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//更新
function doSubmit() {
    var ii = 0;
    var rcheckArray = new Array();
    var checkFlag = false;
    for (var iii=0; iii < document.forms[0].elements.length; iii++) {
        if (document.forms[0].elements[iii].name == "RCHECK"+ii) {
            rcheckArray.push(document.forms[0].elements[iii]);
            ii++;
        }
    }
    for (var k = 0; k < rcheckArray.length; k++) {
        if (rcheckArray[k].checked) {
            checkFlag = true;
            break;
        }
    }

    //学年
    attribute3 = document.forms[0].selectdata_course;
    attribute3.value = "";
    sep = "";
    if (document.forms[0].left_course.length==0 && document.forms[0].right_course.length==0) {
        alert('{rval MSG916}');
        return false;
    }
    for (var i = 0; i < document.forms[0].left_course.length; i++)
    {
        attribute3.value = attribute3.value + sep + document.forms[0].left_course.options[i].value;
        sep = ",";
    }
    if (document.forms[0].left_course.length==0) {
        alert('{rval MSG916}\n　　（学年）');
        return false;
    }

    //科目
    attribute5 = document.forms[0].selectdata_subclass;
    attribute5.value = "";
    sep = "";
    if (document.forms[0].left_subclass.length==0 && document.forms[0].right_subclass.length==0) {
        alert('{rval MSG916}');
        return false;
    }
    for (var i = 0; i < document.forms[0].left_subclass.length; i++)
    {
        attribute5.value = attribute5.value + sep + document.forms[0].left_subclass.options[i].value;
        sep = ",";
    }
    if (document.forms[0].left_subclass.length==0) {
        alert('{rval MSG916}\n　　（科目）');
        return false;
    }

    document.forms[0].cmd.value = 'replace_update';
    document.forms[0].submit();
    return false;
}

//全てチェックを付ける
function check_all(obj) {
    var ii = 0;
    for (var i=0; i < document.forms[0].elements.length; i++)
    {
        if (document.forms[0].elements[i].name == "RCHECK"+ii){
            document.forms[0].elements[i].checked = obj.checked;
            ii++;
        }
    }
}

function checkDecimal(obj) {
    var decimalValue = obj.value
    var check_result = false;

    if (decimalValue != '') {
        //空じゃなければチェック
        if (decimalValue.match(/^[0-9]+(\.[0-9]+)?$/)) {
            check_result = true;
        }
    } else {
        check_result = true;
    }

    if (!check_result) {
        alert('数字を入力して下さい。');
    }

    //正しい値ならtrueを返す
    return check_result;
}

function ClearList(OptionList, TitleName) 
{
    OptionList.length = 0;
}

function move(side,div)
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
        if (div == "course") {
            attribute1 = document.forms[0].right_course;
            attribute2 = document.forms[0].left_course;
        } else {
            attribute1 = document.forms[0].right_subclass;
            attribute2 = document.forms[0].left_subclass;
        }
    } else {
        if (div == "course") {
            attribute1 = document.forms[0].left_course;
            attribute2 = document.forms[0].right_course;  
        } else {
            attribute1 = document.forms[0].left_subclass;
            attribute2 = document.forms[0].right_subclass;  
        }
    }
    
    for (var i = 0; i < attribute2.length; i++) {
        y=current1++
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        tempaa[y] = String(attribute2.options[i].value)+","+y;
    }

    for (var i = 0; i < attribute1.length; i++) {
        if ( attribute1.options[i].selected ) {
            y=current1++
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text; 
            tempaa[y] = String(attribute1.options[i].value)+","+y;
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

    if (div == "course") {
        attribute3 = document.forms[0].selectdata_course;
        attribute3.value = "";
        sep = "";
        for (var i = 0; i < document.forms[0].left_course.length; i++) {
            attribute3.value = attribute3.value + sep + document.forms[0].left_course.options[i].value;
            sep = ",";
        }
    } else {
        attribute3 = document.forms[0].selectdata_subclass;
        attribute3.value = "";
        sep = "";
        for (var i = 0; i < document.forms[0].left_subclass.length; i++) {
            attribute3.value = attribute3.value + sep + document.forms[0].left_subclass.options[i].value;
            sep = ",";
        }
    }
}

function moves(side,div)
{
    var temp5 = new Array();
    var tempc = new Array();
    var tempaa = new Array();
    var current5 = 0;
    var z=0;
    
    if (side == "sel_add_all") {
        if (div == "course") {
            attribute5 = document.forms[0].right_course;
            attribute6 = document.forms[0].left_course;
        } else {
            attribute5 = document.forms[0].right_subclass;
            attribute6 = document.forms[0].left_subclass;
        }
    } else {
        if (div == "course") {
            attribute5 = document.forms[0].left_course;
            attribute6 = document.forms[0].right_course;  
        } else {
            attribute5 = document.forms[0].left_subclass;
            attribute6 = document.forms[0].right_subclass;  
        }
    }
    
    for (var i = 0; i < attribute6.length; i++) {
        z=current5++
        temp5[z] = attribute6.options[i].value;
        tempc[z] = attribute6.options[i].text;
        tempaa[z] = String(attribute6.options[i].value)+","+z;
    }

    for (var i = 0; i < attribute5.length; i++) {
        z=current5++
        temp5[z] = attribute5.options[i].value;
        tempc[z] = attribute5.options[i].text; 
        tempaa[z] = String(attribute5.options[i].value)+","+z;
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
