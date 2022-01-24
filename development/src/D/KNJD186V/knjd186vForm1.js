function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function retfalse() {
    return false;
}

function setGroupDiv() {
    var pata = false;
    var patb = false;
    var patc = false;
    var patd = false;
    var pate = false;
    var val;
    if (document.forms[0].PATARN_DIV[0].checked) {
        pata = true;
    } else if (document.forms[0].PATARN_DIV[1].checked) {
        patb = true;
    } else if (document.forms[0].PATARN_DIV[2].checked) {
        patc = true;
    } else if (document.forms[0].PATARN_DIV[3].checked) {
        patd = true;
    } else if (document.forms[0].PATARN_DIV[4].checked) {
        pate = true;
    }
    document.forms[0].MAX_OR_SIDOU1.disabled = !pata;
    document.forms[0].MAX_OR_SIDOU2.disabled = !pata;
//    document.forms[0].HOGOSHA1.disabled = !pata;
//    document.forms[0].HOGOSHA2.disabled = !pata;
    document.forms[0].NOT_PRINT_SUBEKI.onclick = patc ? null : retfalse;
    document.forms[0].PRINT_CAREERPLAN.onclick = pate ? null : retfalse;
    document.forms[0].NO_ATTEND_SUBCLASS_SP.onclick = pate ? null : retfalse;
    if (document.forms[0].editGroupDivCFlg.value == '' && patc) {
        if (document.forms[0].knjd186vPatCPrintTitleSemestername.value == '1') {
            document.forms[0].NO_PRINT_SEMENAME_IN_TESTNAME.checked = false;
        } else {
            document.forms[0].NO_PRINT_SEMENAME_IN_TESTNAME.checked = true;
        }
        document.forms[0].editGroupDivCFlg.value = "1";
    }
    document.forms[0].PRINT_TUISHIDOU.onclick = pata ? retfalse : null;
    document.forms[0].ADD_PAST_CREDIT.onclick = (pata || patc || patd || pate) ? null : retfalse;
    if (document.forms[0].NO_PRINT_GAKUNENHYOKA_HYOTEI_SUM_AVG) {
        document.forms[0].NO_PRINT_GAKUNENHYOKA_HYOTEI_SUM_AVG.onclick = patc ? null : retfalse;
    }
    document.forms[0].NO_PRINT_ATTENDREMARK.onclick = (patb || patd) ? null : retfalse;
}

function chkNotPrintLastExam(chk) {
    if (chk.checked) {
        if (chk == document.forms[0].NOT_PRINT_LASTEXAM) {
            document.forms[0].NOT_PRINT_LASTEXAM_SCORE.checked = false;
        } else if (chk == document.forms[0].NOT_PRINT_LASTEXAM_SCORE) {
            document.forms[0].NOT_PRINT_LASTEXAM.checked = false;
        }
    }
}

//異動対象日付変更
function tmp_list(cmd, submit) {
    var i;
    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    sep = "";
    for (i = 0; i < document.forms[0].category_selected.length; i++) {
        attribute3.value = attribute3.value + sep + document.forms[0].category_selected.options[i].value;
        sep = ",";
    }

    document.forms[0].cmd.value = cmd;
    if (submit == 'on') {
        document.forms[0].submit();
        return false;
    }
}

function newwin(SERVLET_URL){
    var i;
    if (document.forms[0].category_selected.length == 0) {
        alert('{rval MSG916}');
        return;
    }
    if (document.forms[0].DATE.value == '') {
        alert("異動対象日付が未入力です。");
        return;
    }
    for (i = 0; i < document.forms[0].category_name.length; i++) {
        document.forms[0].category_name.options[i].selected = 0;
    }

    for (i = 0; i < document.forms[0].category_selected.length; i++) {
        document.forms[0].category_selected.options[i].selected = 1;
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

function ClearList(OptionList) {
    OptionList.length = 0;
}

function move1(side) {
    var temp1 = [];
    var temp2 = [];
    var tempa = [];
    var tempb = [];
    var tempaa = [];
    var y=0;
    var attribute;
    var i;
    var val, tmp;

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
        y = temp1.length;
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        tempaa[y] = attribute2.options[i].text.substring(11,14) + "," + y;
    }

    //assign new values to arrays
    for (i = 0; i < attribute1.length; i++) {
        if ( attribute1.options[i].selected ) {
            y = temp1.length;
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text;
            tempaa[y] = attribute1.options[i].text.substring(11,14) + "," + y;
        } else {
            y = temp2.length;
            temp2[y] = attribute1.options[i].value;
            tempb[y] = attribute1.options[i].text;
        }
    }

    tempaa.sort();

    //generating new options
    for (i = 0; i < temp1.length; i++) {
        val = tempaa[i];
        tmp = val.split(',');

        attribute2.options[i] = new Option();
        attribute2.options[i].value = temp1[tmp[1]];
        attribute2.options[i].text =  tempa[tmp[1]];
    }

    //generating new options
    ClearList(attribute1);
    for (i = 0; i < temp2.length; i++) {
        attribute1.options[i] = new Option();
        attribute1.options[i].value = temp2[i];
        attribute1.options[i].text =  tempb[i];
    }
}

function moves(sides) {
    var i;
    var temp5 = [];
    var tempc = [];
    var tempaa = [];
    var z = 0;
    var val, tmp;

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
        z = temp5.length;
        temp5[z] = attribute6.options[i].value;
        tempc[z] = attribute6.options[i].text;
        tempaa[z] = attribute6.options[i].text.substring(11,14) + "," + z;
    }

    //assign new values to arrays
    for (i = 0; i < attribute5.length; i++) {
        z = temp5.length;
        temp5[z] = attribute5.options[i].value;
        tempc[z] = attribute5.options[i].text;
        tempaa[z] = attribute5.options[i].text.substring(11,14) + "," + z;
    }

    tempaa.sort();

    //generating new options 
    for (i = 0; i < temp5.length; i++) {
        val = tempaa[i];
        tmp = val.split(',');

        attribute6.options[i] = new Option();
        attribute6.options[i].value = temp5[tmp[1]];
        attribute6.options[i].text =  tempc[tmp[1]];
    }

    ClearList(attribute5);
}

