function btn_submit(cmd)
{
    student = document.forms[0].selectStudent;
    student.value = "";
    studentLabel = document.forms[0].selectStudentLabel;
    studentLabel.value = "";
    sep = "";

    for (var i = 0; i < document.forms[0].category_selected.length; i++) {
        student.value = student.value + sep + document.forms[0].category_selected.options[i].value;
        studentLabel.value = studentLabel.value + sep + document.forms[0].category_selected.options[i].text;
        sep = ",";
    }

    //生徒、教職員切替時は初期化
    if (cmd == 'output') {
        student.value = "";
        studentLabel.value = "";
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL)
{
    if (document.forms[0].category_selected.length == 0) {
        alert('{rval MSG916}');
        return;
    }

    if (document.forms[0].TERM_SDATE.value == '') {
        alert('発行日を指定して下さい。');
        return;
    }

    if (document.forms[0].TERM_EDATE && document.forms[0].TERM_EDATE.value == '') {
        alert('有効期限を指定して下さい。');
        return;
    }
    else{
        var strtspl = document.forms[0].TERM_SDATE.value.split('/');
        var endspl  = document.forms[0].TERM_EDATE.value.split('/');
        if (strtspl.length < 2 || endspl.length < 2) {
            alert('{rval MSG902}');
            if (strtspl.length < 2) {
                document.forms[0].TERM_SDATE.focus();
            } else {
                document.forms[0].TERM_EDATE.focus();
            }
            return false;
        }
        //開始日付 > 終了日付
        var startDate = new Date(parseInt(strtspl[0]), parseInt(strtspl[1]) - 1, parseInt(strtspl[2]));
        var endDate   = new Date(parseInt(endspl [0]), parseInt(endspl [1]) - 1, parseInt(endspl [2]));
        if (startDate > endDate) {
            alert('{rval MSG913}' + "\r\n有効期限が不正です。");
            document.forms[0].TERM_EDATE.focus();
            return false;
        }
    }

    for (var i = 0; i < document.forms[0].category_name.length; i++) {
        document.forms[0].category_name.options[i].selected = 0;
    }

    for (var i = 0; i < document.forms[0].category_selected.length; i++) {
        document.forms[0].category_selected.options[i].selected = 1;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

    document.forms[0].action = SERVLET_URL +"/KNJA";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

function ClearList(OptionList, TitleName)
{
    OptionList.length = 0;
}

function move1(side)
{
    var temp1 = new Array();
    var temp2 = new Array();
    var tempa = new Array();
    var tempb = new Array();
    var tempaa = new Array();
    var current1 = 0;
    var current2 = 0;
    var y = 0;
    var attribute;

    if (side == "left") {
        attribute1 = document.forms[0].category_name;
        attribute2 = document.forms[0].category_selected;
    } else {
        attribute1 = document.forms[0].category_selected;
        attribute2 = document.forms[0].category_name;  
    }

    for (var i = 0; i < attribute2.length; i++) {
        y = current1++
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        var cutpt = String(attribute2.options[i].text).search("　");
        tempaa[y] = String(attribute2.options[i].text).substring(0, cutpt + 4) + "," + y;
    }

    for (var i = 0; i < attribute1.length; i++) {
        if (attribute1.options[i].selected) {
            y = current1++;
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text;
            var cutpt = String(attribute1.options[i].text).search("　");
            tempaa[y] = String(attribute1.options[i].text).substring(0, cutpt + 4) + "," + y;
        } else {
            y=current2++;
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

function moves(sides)
{
    var temp5 = new Array();
    var tempc = new Array();
    var tempaa = new Array();
    var current5 = 0;
    var z = 0;

    if (sides == "left") {
        attribute5 = document.forms[0].category_name;
        attribute6 = document.forms[0].category_selected;
    } else {
        attribute5 = document.forms[0].category_selected;
        attribute6 = document.forms[0].category_name;
    }

    for (var i = 0; i < attribute6.length; i++) {
        z = current5++
        temp5[z] = attribute6.options[i].value;
        tempc[z] = attribute6.options[i].text;
        var cutpt = String(attribute6.options[i].text).search("　");
        tempaa[z] = String(attribute6.options[i].text).substring(0, cutpt + 4) + "," + z;
    }

    //assign new values to arrays
    for (var i = 0; i < attribute5.length; i++) {
        z = current5++
        temp5[z] = attribute5.options[i].value;
        tempc[z] = attribute5.options[i].text; 
        var cutpt = String(attribute5.options[i].text).search("　");
        tempaa[z] = String(attribute5.options[i].text).substring(0, cutpt + 4) + "," + z;
    }

    tempaa.sort();

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
