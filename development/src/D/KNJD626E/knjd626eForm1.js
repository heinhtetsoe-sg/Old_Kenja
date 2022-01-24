function btn_submit(cmd) {

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function chkPattern(e) {
    document.forms[0].btn_csv.disabled = document.forms[0].OUTPUT_PATERN2.checked;
    document.forms[0].OUTPUT_STUDENT_JISU.disabled = !document.forms[0].OUTPUT_PATERN1.checked;
    var usehr = false;
    if (document.forms[0].GROUP_DIV) {
        usehr = document.forms[0].GROUP_DIV1.checked;
        document.getElementById("CHECK_HR_COURSE_NEW_PAGE").style = usehr ? " display : visible; " : " display : none; ";
    }
}

function newwin(SERVLET_URL, cmd){
    document.forms[0].cmd.value = cmd;
    if (document.forms[0].CLASS_SELECTED) {
        if (document.forms[0].CLASS_SELECTED.length == 0) {x
            alert('{rval MSG916}');
            return;
        }
    }
    if (document.forms[0].DATE.value == '') {
        alert("出欠集計日付が未入力です。");
        return;
    }

    //日付範囲チェック
    var day   = document.forms[0].DATE.value.split('/');        //出欠集計日付
    var sdate = document.forms[0].SEME_SDATE.value.split('/');  //学期開始日付
    var edate = document.forms[0].SEME_EDATE.value.split('/');  //学期終了日付

    if((new Date(eval(sdate[0]),eval(sdate[1])-1,eval(sdate[2])) > new Date(eval(day[0]),eval(day[1])-1,eval(day[2])))
       || ((new Date(eval(day[0]),eval(day[1])-1,eval(day[2])) > new Date(eval(edate[0]),eval(edate[1])-1,eval(edate[2])))))
    {
        alert("日付が学期の範囲外です");
        return;
    }

    if (document.forms[0].TESTKINDCD.value == '') {
        alert("試験が選択されていません。");
        return;
    }
    if (document.forms[0].CLASS_NAME) {
        for (var i = 0; i < document.forms[0].CLASS_NAME.length; i++) {
            document.forms[0].CLASS_NAME.options[i].selected = 0;
        }

        for (var i = 0; i < document.forms[0].CLASS_SELECTED.length; i++) {
            document.forms[0].CLASS_SELECTED.options[i].selected = 1;
        }
    }
    if (checkRankVal() == false) {
        return true;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJD";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

function checkRank(e) {
    e.value = toInteger(e.value);
    if (e.value) {
        return checkRankVal();
    }
}
function checkRankVal() {
    if (document.forms[0].OUTPUT_PATERN.value == "5") {
        return true;
    }
    var start = document.forms[0].RANK_START;
    var end = document.forms[0].RANK_END;
    var error;
    if (start && end) {
        if (!error && start.value) {
            var ist = parseInt(start.value);
            if (ist <= 0) {
                error = '{rval MSG916} : 出力順位';
            }
        }
        if (!error && end.value) {
            var ied = parseInt(end.value);
            if (ied <= 0) {
                error = '{rval MSG916} : 出力順位';
            }
        }
        if (!error && start.value && end.value) {
            var ist = parseInt(start.value);
            var ied = parseInt(end.value);
            if (end.value && ist > ied) {
                error = '{rval MSG916} : 出力順位';
            }
        }
    }
    if (error) {
        alert(error);
        return false;
    }
    return true;
}

function ClearList(OptionList) 
{
    OptionList.length = 0;
}
    
function AllClearList(OptionList, TitleName) 
{
    ClearList(document.forms[0].CLASS_NAME);
    ClearList(document.forms[0].CLASS_SELECTED);
}

function cmpVal(a, b) {
    if (a.value < b.value) {
        return -1;
    } else if (a.value > b.value) {
        return 1;
    }
    return 0;
}

function move1(side) {
    var attribute1, attribute2;
    var temp1 = [];
    var temp2 = [];
    var o, i;
    
    //assign what select attribute treat as attribute1 and attribute2
    if (side == "left") {
        attribute1 = document.forms[0].CLASS_NAME;
        attribute2 = document.forms[0].CLASS_SELECTED;
    } else {
        attribute1 = document.forms[0].CLASS_SELECTED;
        attribute2 = document.forms[0].CLASS_NAME;  
    }
    
    //fill an array with old values
    for (i = 0; i < attribute2.length; i++) {
        o = attribute2.options[i];
        temp1.push({ "value" : o.value, "text" : o.text});
    }

    //assign new values to arrays
    for (i = 0; i < attribute1.length; i++) {
        o = attribute1.options[i];
        if (o.selected) {
            temp1.push({ "value" : o.value, "text" : o.text});
        } else {
            temp2.push({ "value" : o.value, "text" : o.text});
        }
    }

    temp1.sort(cmpVal);

    //generating new options
    for (i = 0; i < temp1.length; i++) {  
        o = temp1[i];
        attribute2.options[i] = new Option();
        attribute2.options[i].value = o.value;
        attribute2.options[i].text =  o.text;
    }

    //generating new options
    ClearList(attribute1);
    for (i = 0; i < temp2.length; i++) {   
        o = temp2[i];
        attribute1.options[i] = new Option();
        attribute1.options[i].value = o.value;
        attribute1.options[i].text =  o.text;
    }

}

function moves(sides) {
    var attribute5, attribute6;
    var temp = [];
    var i;
    
    //assign what select attribute treat as attribute5 and attribute6
    if (sides == "left") {  
        attribute5 = document.forms[0].CLASS_NAME;
        attribute6 = document.forms[0].CLASS_SELECTED;
    } else {  
        attribute5 = document.forms[0].CLASS_SELECTED;
        attribute6 = document.forms[0].CLASS_NAME;  
    }

    
    //fill an array with old values
    for (i = 0; i < attribute6.length; i++) {  
        o = attribute6.options[i];
        temp.push({"value" : o.value, "text" : o.text});
    }

    //assign new values to arrays
    for (i = 0; i < attribute5.length; i++) {   
        o = attribute5.options[i];
        temp.push({"value" : o.value, "text" : o.text});
    }

    temp.sort(cmpVal);

    //generating new options
    for (i = 0; i < temp.length; i++) {  
        o = temp[i];

        attribute6.options[i] = new Option();
        attribute6.options[i].value = o.value;
        attribute6.options[i].text =  o.text;
    }

    //generating new options
    ClearList(attribute5);
}

window.addEventListener("load", function(e) {
    chkPattern(e);
});

