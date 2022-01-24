function btn_submit(cmd) {

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//更新
function doSubmit() {

    //課程学科
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
        alert('{rval MSG916}\n　　（課程学科）');
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

    //法定欠課数上限値の入力チェック
    var jugyou_jisu_flg  = document.forms[0].JUGYOU_JISU_FLG;
    if (jugyou_jisu_flg.value != '2' && document.forms[0].RCHECK_ABSENCE_HIGH.checked) {
        var absence_high     = document.forms[0].ABSENCE_HIGH;
        var get_absence_high = document.forms[0].GET_ABSENCE_HIGH;
        if (parseInt(absence_high.value) < parseInt(get_absence_high.value)) {
            alert('履修より修得の欠課数上限値が大きくなっています。');
            return false;
        }
        if (!checkDecimal(absence_high)) {
            return false;
        }
        if (!checkDecimal(get_absence_high)) {
            return false;
        }
    }

    document.forms[0].cmd.value = 'replace_update';
    document.forms[0].submit();
    return false;
}

//全てチェックを付ける
function check_all() {
    var checkVal = $('input[name^=RCHECK_ALL]').prop('checked');
    $('input[name^=RCHECK_]').each(function () {
        $(this).prop('checked', checkVal);
    });
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

//半期認定フラグの文言表示
function Check_a(Name){

    flg = document.forms[0].AUTHORIZE_FLG.checked ;

    if(flg){
        document.all('check_a').innerHTML=document.all('check1_a').innerHTML;
    }else{
        document.all('check_a').innerHTML=document.all('check2_a').innerHTML;
    }
}

//無条件履修取得フラグの文言表示
function Check_c(Name){

    flg = document.forms[0].COMP_UNCONDITION_FLG.checked ;

    if(flg){
        document.all('check_c').innerHTML=document.all('check1_c').innerHTML;
    }else{
        document.all('check_c').innerHTML=document.all('check2_c').innerHTML;
    }
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
