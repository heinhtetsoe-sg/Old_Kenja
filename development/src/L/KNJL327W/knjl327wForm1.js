function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//権限チェック
function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();
}

//印刷
function newwin(SERVLET_URL) {
    if (document.forms[0].APPLICANTDIV.value == '') {
        alert('{rval MSG310}\n( 入試制度 )');
        return;
    }
    if (document.forms[0].TESTDIV.value == '') {
        alert('{rval MSG310}\n( 入試区分 )');
        return;
    }
    if (document.forms[0].STYLE3.checked == false && document.forms[0].KISAI_DATE.value == '') {
        alert('{rval MSG301}\n( 記載日 )');
        return;
    }
    if (document.forms[0].STYLE2.checked == true) {
        if (document.forms[0].SUC_DATE.value == '' || document.forms[0].SUC_AM_PM.value == '' || document.forms[0].SUC_HOUR.value == '' || document.forms[0].SUC_MINUTE.value == '') {
            alert('{rval MSG301}\n( 合格発表日 )');
            return;
        }
        if (document.forms[0].LEFT_LIST.length == 0) {
            alert('{rval MSG916}');
            return;
        }
        for (var i = 0; i < document.forms[0].RIGHT_LIST.length; i++) {
            document.forms[0].RIGHT_LIST.options[i].selected = 0;
        }
        for (var i = 0; i < document.forms[0].LEFT_LIST.length; i++) {
            document.forms[0].LEFT_LIST.options[i].selected = 1;
        }
    }
    if (document.forms[0].STYLE3.checked == true) {
        if (document.forms[0].LEFT_LIST.length == 0) {
            alert('{rval MSG916}');
            return;
        }
        for (var i = 0; i < document.forms[0].RIGHT_LIST.length; i++) {
            document.forms[0].RIGHT_LIST.options[i].selected = 0;
        }
        for (var i = 0; i < document.forms[0].LEFT_LIST.length; i++) {
            document.forms[0].LEFT_LIST.options[i].selected = 1;
        }
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJL";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}


/*************************/
/**  以下 LIST to LIST  **/
/*************************/
function ClearList(OptionList, TitleName) {
    OptionList.length = 0;
}

function move3(direction, left_name, right_name, flg) {
    move2(direction, left_name, right_name, flg);
    return;
}

function move2(side, left, right, sort) {
    var temp1 = new Array();
    var temp2 = new Array();
    var tempa = new Array();
    var tempb = new Array();
    var a = new Array();
    var current1 = 0;
    var current2 = 0;
    var y=0;
    var attribute1;
    var attribute2;

    //assign what select attribute treat as attribute1 and attribute2
    if (side == "right" || side == "sel_del_all") {
        attribute1 = document.forms[0][left];
        attribute2 = document.forms[0][right];
    } else {
        attribute1 = document.forms[0][right];
        attribute2 = document.forms[0][left];
    }

    //fill an array with old values
    for (var i = 0; i < attribute2.length; i++) {
        y=current1++
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        a[temp1[y]] = tempa[y];
    }

    //assign new values to arrays
    for (var i = 0; i < attribute1.length; i++) {
        if (side == "right" || side == "left") {
            if ( attribute1.options[i].selected ) {
                y=current1++
                temp1[y] = attribute1.options[i].value;
                tempa[y] = attribute1.options[i].text;
                a[temp1[y]] = tempa[y];
            } else {
                y=current2++
                temp2[y] = attribute1.options[i].value;
                tempb[y] = attribute1.options[i].text;
            }
        } else {
            y=current1++
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text;
            a[temp1[y]] = tempa[y];
        }
    }
    if (sort) {
        //sort
        temp1 = temp1.sort();
        //generating new options
        for (var i = 0; i < temp1.length; i++) {
            tempa[i] = a[temp1[i]];
        }
    }

    //generating new options
    for (var i = 0; i < temp1.length; i++) {
        attribute2.options[i] = new Option();
        attribute2.options[i].value = temp1[i];
        attribute2.options[i].text =  tempa[i];
    }

    //generating new options
    ClearList(attribute1);
    if (temp2.length>0) {
        for (var i = 0; i < temp2.length; i++) {
            attribute1.options[i] = new Option();
            attribute1.options[i].value = temp2[i];
            attribute1.options[i].text =  tempb[i];
        }
    }
}
