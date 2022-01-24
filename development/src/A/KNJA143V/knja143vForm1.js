function btn_submit(cmd) {
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

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL) {

    if (document.forms[0].category_selected.length == 0) {
        alert('{rval MSG916}');
        return;
    }
    if (document.forms[0].TERM_SDATE.value == '') {
        alert('発行日を指定して下さい。');
        return;
    }
    if (document.forms[0].TERM_EDATE.value == '') {
        alert('有効期限を指定して下さい。');
        return;
    }
    for (var i = 0; i < document.forms[0].category_name.length; i++) {
        document.forms[0].category_name.options[i].selected = 0;
    }

    for (var i = 0; i < document.forms[0].category_selected.length; i++) {
        document.forms[0].category_selected.options[i].selected = 1;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJA";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

function ClearList(OptionList) {
    OptionList.length = 0;
}
function AllClearList(OptionList, TitleName) {
        attribute = document.forms[0].category_name;
        ClearList(attribute);
        attribute = document.forms[0].category_selected;
        ClearList(attribute);
}
function move1(side) {
    var tempa = [];
    var tempb = [];
    var i;
    
    //assign what select attribute treat as attribute1 and attribute2
    if (side == "left") {  
        attribute1 = document.forms[0].category_name;
        attribute2 = document.forms[0].category_selected;
    } else {  
        attribute1 = document.forms[0].category_selected;
        attribute2 = document.forms[0].category_name;  
    }

    //fill an array with old values
    for (i = 0; i < attribute2.length; i++) {  
        tempa[tempa.length] = {"value": attribute2.options[i].value, "text": attribute2.options[i].text};
    }

    //assign new values to arrays
    for (i = 0; i < attribute1.length; i++) {   
        if ( attribute1.options[i].selected ) {  
            tempa[tempa.length] = {"value": attribute1.options[i].value, "text": attribute1.options[i].text};
        } else {  
            tempb[tempb.length] = {"value": attribute1.options[i].value, "text": attribute1.options[i].text};
        }
    }

    tempa.sort(compareValue);

    //generating new options
    for (i = 0; i < tempa.length; i++) {  
        attribute2.options[i] = new Option();
        attribute2.options[i].value = tempa[i].value;
        attribute2.options[i].text =  tempa[i].text;
    }

    //generating new options
    ClearList(attribute1);
    for (i = 0; i < tempb.length; i++) {   
        attribute1.options[i] = new Option();
        attribute1.options[i].value = tempb[i].value;
        attribute1.options[i].text =  tempb[i].text;
    }

}
function compareValue(a, b) {
    if (a.value < b.value) {
        return -1;
    } else if (a.value == b.value) {
        return 0;
    } else {
        return 1;
    }
}
function moves(sides) {
    var temp = [];
    var i;
    
    //assign what select attribute treat as attribute5 and attribute6
    if (sides == "left") {  
        attribute5 = document.forms[0].category_name;
        attribute6 = document.forms[0].category_selected;
    } else {  
        attribute5 = document.forms[0].category_selected;
        attribute6 = document.forms[0].category_name;  
    }

    //fill an array with old values
    for (i = 0; i < attribute6.length; i++) {  
        temp[temp.length] = {"value": attribute6.options[i].value, "text": attribute6.options[i].text};
    }

    //assign new values to arrays
    for (i = 0; i < attribute5.length; i++) {   
        temp[temp.length] = {"value": attribute5.options[i].value, "text": attribute5.options[i].text};
    }

    temp.sort(compareValue);

    //generating new options
    for (i = 0; i < temp.length; i++) {  
        attribute6.options[i] = new Option();
        attribute6.options[i].value = temp[i].value;
        attribute6.options[i].text =  temp[i].text;
    }

    //generating new options
    ClearList(attribute5);

}

