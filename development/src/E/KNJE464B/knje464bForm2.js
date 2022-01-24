function btn_submit(cmd){
    student = document.forms[0].selectStudent;
    student.value = "";
    studentLabel = document.forms[0].selectStudentLabel;
    studentLabel.value = "";
    if (cmd != 'output') {
        sep = "";
        for (var i = 0; i < document.forms[0].SCHREG_SELECTED.length; i++) {
            console.log(document.forms[0].SCHREG_SELECTED.options[i].value);
            student.value = student.value + sep + document.forms[0].SCHREG_SELECTED.options[i].value;
            studentLabel.value = studentLabel.value + sep + document.forms[0].SCHREG_SELECTED.options[i].text;
            sep = ",";
        }
        updSelData("1");
    }

    //必須チェック
    if (cmd == 'clear') {
        if (!confirm('{rval MSG106}')){
            return false;
        }
    }
    if (cmd == 'delete') {
        if (!confirm('{rval MSG103}')){
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function updSelData(updDiv) {
    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    sep = "";

    attribute4 = document.forms[0].selectdata2;
    attribute4.value = "";
    sep2 = "";
    if (updDiv == "1") {
        for (var i = 0; i < document.forms[0].CATEGORY_SELECTED.length; i++) {
            left_val = document.forms[0].CATEGORY_SELECTED.options[i].value;
            attribute3.value = attribute3.value + sep + left_val;
            sep = ",";
        }
        for (var i = 0; i < document.forms[0].SCHREG_SELECTED.length; i++) {
            left_val = document.forms[0].SCHREG_SELECTED.options[i].value;
            attribute4.value = attribute4.value + sep2 + left_val;
            sep2 = ",";
        }
    } else {
        for (var i = 0; i < document.forms[0].SCHREG_SELECTED.length; i++) {
            left_val = document.forms[0].SCHREG_SELECTED.options[i].value;
            attribute4.value = attribute4.value + sep2 + left_val;
            sep2 = ",";
        }
    }
}

//更新
function doSubmit(updDiv) {
    // //必須チェック
    if (document.forms[0].IPT_FACILITY_GRP.value == "") {
        alert('{rval MSG301}' + '(グループコード)');
        return true;
    }
    if (document.forms[0].IPT_GROUPNAME.value == "") {
        alert('{rval MSG301}' + '(名称)');
        return true;
    }

    if (updDiv == "1") {
        if (document.forms[0].CATEGORY_SELECTED.length == 0) {
            if (!confirm('1件も登録されていません。削除してよろしいでしょうか？\n（登録生徒も削除します）')){
                return false;
            }
        }
        updSelData(updDiv)
        document.forms[0].cmd.value = 'update';
    } else {
        updSelData(updDiv)
        document.forms[0].cmd.value = 'update2';
    }
    document.forms[0].submit();
    return false;
}

function move1(side, left, right, sort) {
    var temp1 = new Array();
    var temp2 = new Array();
    var tempa = new Array();
    var tempb = new Array();
    var a = new Array();
    var b = new Array();
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
        a[tempa[y]] = temp1[y];
        b[temp1[y]] = tempa[y];
    }

    //assign new values to arrays
    for (var i = 0; i < attribute1.length; i++) {
        if (side == "right" || side == "left") {
            //ignore move if text's top is "■"
            if ( attribute1.options[i].selected && attribute1.options[i].text.substring(0,1) != "■" ) {
                y=current1++
                temp1[y] = attribute1.options[i].value;
                tempa[y] = attribute1.options[i].text;
                a[tempa[y]] = temp1[y];
                b[temp1[y]] = tempa[y];
            } else {
                y=current2++
                temp2[y] = attribute1.options[i].value;
                tempb[y] = attribute1.options[i].text;
            }
        } else {
            //ignore full move if text's top is "■"
            if (attribute1.options[i].text.substring(0,1) != "■") {
                y=current1++
                temp1[y] = attribute1.options[i].value;
                tempa[y] = attribute1.options[i].text;
                a[tempa[y]] = temp1[y];
                b[temp1[y]] = tempa[y];
            } else {
                y=current2++
                temp2[y] = attribute1.options[i].value;
                tempb[y] = attribute1.options[i].text;
            }
        }
    }
    if (sort) {
        //sort
        temp1 = temp1.sort();
        //generating new options
        for (var i = 0; i < temp1.length; i++) {
            tempa[i] = b[temp1[i]];
        }
    }

    //generating new options
    for (var i = 0; i < temp1.length; i++) {
        attribute2.options[i] = new Option();
        attribute2.options[i].value = temp1[i];
        attribute2.options[i].text  = tempa[i];
    }

    //generating new options
    ClearList(attribute1);
    if (temp2.length > 0) {
        for (var i = 0; i < temp2.length; i++) {
            attribute1.options[i] = new Option();
            attribute1.options[i].value = temp2[i];
            attribute1.options[i].text  = tempb[i];
        }
    }

    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    sep = "";
    for (var i = 0; i < document.forms[0][left].length; i++) {
        attribute3.value = attribute3.value + sep + document.forms[0][left].options[i].value;
        sep = ",";
    }
}

function ClearList(OptionList) {
    OptionList.length = 0;
}
