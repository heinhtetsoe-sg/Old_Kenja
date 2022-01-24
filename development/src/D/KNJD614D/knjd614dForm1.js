// kanji=漢字

function btn_submit(cmd) {
    if (cmd == "save_setting" || cmd == "load_setting") {
        if (document.forms[0].SEMESTER.value == "") {
            alert('{rval MSG301}'+"学期を設定してください。");
            return false;
        }
        if (document.forms[0].GRADE.value == "") {
            alert('{rval MSG301}'+"学年を設定してください。");
            return false;
        }
        if (document.forms[0].TESTCD.value == "") {
            alert('{rval MSG301}'+"テストコードを設定してください。");
            return false;
        }
        if (document.forms[0].DATE_FROM.value == "") {
            alert('{rval MSG301}'+"出欠集計開始日付を設定してください。");
            return false;
        }
        if (document.forms[0].DATE_TO.value == "") {
            alert('{rval MSG301}'+"出欠集計終了日付を設定してください。");
            return false;
        }
    }

    if (cmd == "save_setting") {
        if (document.forms[0].SETTING_NAME.value == "") {
            alert('{rval MSG301}'+"登録する設定名称を入力してください。");
            return false;
        }
        var chkStrs = document.forms[0].CHK_SETTINGNAME.value.split(',');
        for (var cCnt = 0;cCnt < chkStrs.length;cCnt++) {
            if (chkStrs[cCnt] == document.forms[0].SETTING_NAME.value) {
                if (!confirm('{rval MSG104}')) {
                    return false;
                } else {
                    break;
                }
            }
        }
    }

    if (cmd == "del_setting" || cmd == "load_setting") {
        if (document.forms[0].SETTING_SEQ.value == "") {
            alert('{rval MSG301}'+"登録済みの設定名を選択してください。");
            return false;
        }
    }

    fixSelectList();

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function fixSelectList() {
    var setSelPrm;
    var sep = "";
    for (var sIdx = 1; sIdx <= 6;sIdx++) {
        switch (sIdx) {
            case 1:
                setSelPrm = document.forms[0].selHR;
                break;
            case 2:
                if (!document.forms[0].CATEGORY_IS_OUTER_COLLEGE.checked) {
                    continue;
                }
                setSelPrm = document.forms[0].selOutCol;
                break;
            case 3:
                if (!document.forms[0].CATEGORY_IS_PROF_TEST.checked && !document.forms[0].CATEGORY_IS_BENESSE_TEST.checked) {
                    continue;
                }
                setSelPrm = document.forms[0].selBene;
                break;
            case 4:
                if (!document.forms[0].CATEGORY_IS_STUDY_SUP.checked) {
                    continue;
                }
                setSelPrm = document.forms[0].selSSup;
                break;
            case 5:
                if (!document.forms[0].CATEGORY_IS_SUNDAI.checked) {
                    continue;
                }
                setSelPrm = document.forms[0].selSund;
                break;
            case 6:
                if (!document.forms[0].CATEGORY_IS_KAWAI.checked) {
                    continue;
                }
                setSelPrm = document.forms[0].selKawi;
                break;
            default:
                break;
        }
        for (var i = 0; i < document.forms[0]["CATEGORY_NAME"+sIdx].length; i++) {
            document.forms[0]["CATEGORY_NAME"+sIdx].options[i].selected = 0;
        }
        sep = "";
        setSelPrm.value = "";
        for (var i = 0; i < document.forms[0]["CATEGORY_SELECTED"+sIdx].length; i++) {
            document.forms[0]["CATEGORY_SELECTED"+sIdx].options[i].selected = 1;
            setSelPrm.value = setSelPrm.value + sep + document.forms[0]["CATEGORY_SELECTED"+sIdx].options[i].value;
            sep = ",";
        }
    }
}

function newwin(SERVLET_URL, paramCmd){

    if (document.forms[0].CATEGORY_SELECTED1.length == 0) {
        alert('{rval MSG916}');
        return;
    }
    fixSelectList();

    if (document.forms[0].DATE_FROM.value == ""){
        alert('出欠集計日付(FROM)を指定してください。');
        return false;
    }

    if (document.forms[0].DATE_TO.value == ""){
        alert('出欠集計日付(TO)を指定してください。');
        return false;
    }

    document.forms[0].DATE_FROM.value = document.forms[0].DATE_FROM.value.replace(/\//g, "-");
    document.forms[0].DATE_TO.value = document.forms[0].DATE_TO.value.replace(/\//g, "-");

    var action = document.forms[0].action;
    var target = document.forms[0].target;
    var cmd = document.forms[0].cmd.value;
    if (paramCmd) {
      document.forms[0].cmd.value = paramCmd;
    }

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJD";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].DATE_FROM.value = document.forms[0].DATE_FROM.value.replace(/\-/g,"/");
    document.forms[0].DATE_TO.value = document.forms[0].DATE_TO.value.replace(/\-/g,"/");
    document.forms[0].action = action;
    document.forms[0].target = target;
    document.forms[0].cmd.value = cmd;
}

function ClearList(OptionList, TitleName) 
{
    OptionList.length = 0;
}
    
function AllClearList()
{
    attribute = document.forms[0].CATEGORY_NAME1;
    ClearList(attribute,attribute);
    attribute = document.forms[0].CATEGORY_SELECTED1;
    ClearList(attribute,attribute);
}

function disEnaCtl(obj, partId, idx)
{
    switch (partId) {
    case 1:
        switch (idx) {
        case 0:
        case 1:
            if (document.forms[0].CATEGORY_IS_INNER.checked) {
                document.forms[0].CATEGORY_IS_INNER_G1.disabled = false;
                if (document.forms[0].CATEGORY_IS_INNER_G1.checked) {
                    document.forms[0].CATEGORY_IS_INNER_G1S1.disabled = false;
                    document.forms[0].CATEGORY_IS_INNER_G1SH.disabled = false;
                    document.forms[0].CATEGORY_IS_INNER_G1S9.disabled = false;
                } else {
                    document.forms[0].CATEGORY_IS_INNER_G1S1.disabled = true;
                    document.forms[0].CATEGORY_IS_INNER_G1SH.disabled = true;
                    document.forms[0].CATEGORY_IS_INNER_G1S9.disabled = true;
                }
            } else {
                document.forms[0].CATEGORY_IS_INNER_G1.disabled = true;
                document.forms[0].CATEGORY_IS_INNER_G1S1.disabled = true;
                document.forms[0].CATEGORY_IS_INNER_G1SH.disabled = true;
                document.forms[0].CATEGORY_IS_INNER_G1S9.disabled = true;
            }
            if (idx != 0) {
                break;
            }
        case 2:
            if (document.forms[0].CATEGORY_IS_INNER.checked) {
                document.forms[0].CATEGORY_IS_INNER_G2.disabled = false;
                if (document.forms[0].CATEGORY_IS_INNER_G2.checked) {
                    document.forms[0].CATEGORY_IS_INNER_G2S1.disabled = false;
                    document.forms[0].CATEGORY_IS_INNER_G2SH.disabled = false;
                    document.forms[0].CATEGORY_IS_INNER_G2S9.disabled = false;
                } else {
                    document.forms[0].CATEGORY_IS_INNER_G2S1.disabled = true;
                    document.forms[0].CATEGORY_IS_INNER_G2SH.disabled = true;
                    document.forms[0].CATEGORY_IS_INNER_G2S9.disabled = true;
                }
            } else {
                document.forms[0].CATEGORY_IS_INNER_G2.disabled = true;
                document.forms[0].CATEGORY_IS_INNER_G2S1.disabled = true;
                document.forms[0].CATEGORY_IS_INNER_G2SH.disabled = true;
                document.forms[0].CATEGORY_IS_INNER_G2S9.disabled = true;
            }
            if (idx != 0) {
                break;
            }
        case 3:
            if (document.forms[0].CATEGORY_IS_INNER.checked) {
                document.forms[0].CATEGORY_IS_INNER_G3.disabled = false;
                if (document.forms[0].CATEGORY_IS_INNER_G3.checked) {
                    document.forms[0].CATEGORY_IS_INNER_G3S1.disabled = false;
                    document.forms[0].CATEGORY_IS_INNER_G3SH.disabled = false;
                    document.forms[0].CATEGORY_IS_INNER_G3S9.disabled = false;
                } else {
                    document.forms[0].CATEGORY_IS_INNER_G3S1.disabled = true;
                    document.forms[0].CATEGORY_IS_INNER_G3SH.disabled = true;
                    document.forms[0].CATEGORY_IS_INNER_G3S9.disabled = true;
                }
            } else {
                document.forms[0].CATEGORY_IS_INNER_G3.disabled = true;
                document.forms[0].CATEGORY_IS_INNER_G3S1.disabled = true;
                document.forms[0].CATEGORY_IS_INNER_G3SH.disabled = true;
                document.forms[0].CATEGORY_IS_INNER_G3S9.disabled = true;
            }
            if (idx != 0) {
                break;
            }
        case 4:
            if (document.forms[0].CATEGORY_IS_INNER.checked) {
                document.forms[0].CATEGORY_IS_INNER_9_ALL.disabled = false;
                if (document.forms[0].CATEGORY_IS_INNER_9_ALL.checked) {
                    document.forms[0].CATEGORY_IS_INNER_9_ALL12.disabled = false;
                    document.forms[0].CATEGORY_IS_INNER_9_ALL123.disabled = false;
                } else {
                    document.forms[0].CATEGORY_IS_INNER_9_ALL12.disabled = true;
                    document.forms[0].CATEGORY_IS_INNER_9_ALL123.disabled = true;
                }
            } else {
                document.forms[0].CATEGORY_IS_INNER_9_ALL.disabled = true;
                document.forms[0].CATEGORY_IS_INNER_9_ALL12.disabled = true;
                document.forms[0].CATEGORY_IS_INNER_9_ALL123.disabled = true;
            }
            break;
        }
        break;
    default:
        break;
    }
}

function disEnaMockCtl(obj, partId) {
    switch (partId) {
    case 1:
        document.forms[0].CATEGORY_IS_OUTER_COLLEGE.disabled = !obj.checked;
        document.forms[0].CATEGORY_IS_BENESSE_TEST.disabled = !obj.checked;
        document.forms[0].CATEGORY_IS_STUDY_SUP.disabled = !obj.checked;
        document.forms[0].CATEGORY_IS_SUNDAI.disabled = !obj.checked;
        document.forms[0].CATEGORY_IS_KAWAI.disabled = !obj.checked;
        document.forms[0].CATEGORY_IS_OUTER_COLLEGE.checked = obj.checked;
        document.forms[0].CATEGORY_IS_BENESSE_TEST.checked = obj.checked;
        document.forms[0].CATEGORY_IS_STUDY_SUP.checked = obj.checked;
        document.forms[0].CATEGORY_IS_SUNDAI.checked = obj.checked;
        document.forms[0].CATEGORY_IS_KAWAI.checked = obj.checked;
    case 2:
        document.forms[0].CATEGORY_SELECTED2.disabled = !obj.checked;
        document.forms[0].CATEGORY_NAME2.disabled = !obj.checked;
        document.forms[0].btn_lefts2.disabled = !obj.checked;
        document.forms[0].btn_left2.disabled = !obj.checked;
        document.forms[0].btn_right2.disabled = !obj.checked;
        document.forms[0].btn_rights2.disabled = !obj.checked;
        if (partId != "1") {
            break;
        }
    case 3:
        document.forms[0].CATEGORY_SELECTED3.disabled = !obj.checked;
        document.forms[0].CATEGORY_NAME3.disabled = !obj.checked;
        document.forms[0].btn_lefts3.disabled = !obj.checked;
        document.forms[0].btn_left3.disabled = !obj.checked;
        document.forms[0].btn_right3.disabled = !obj.checked;
        document.forms[0].btn_rights3.disabled = !obj.checked;
        if (partId != "1") {
            break;
        }
    case 4:
        document.forms[0].CATEGORY_SELECTED4.disabled = !obj.checked;
        document.forms[0].CATEGORY_NAME4.disabled = !obj.checked;
        document.forms[0].btn_lefts4.disabled = !obj.checked;
        document.forms[0].btn_left4.disabled = !obj.checked;
        document.forms[0].btn_right4.disabled = !obj.checked;
        document.forms[0].btn_rights4.disabled = !obj.checked;
        if (partId != "1") {
            break;
        }
    case 5:
        document.forms[0].CATEGORY_SELECTED5.disabled = !obj.checked;
        document.forms[0].CATEGORY_NAME5.disabled = !obj.checked;
        document.forms[0].btn_lefts5.disabled = !obj.checked;
        document.forms[0].btn_left5.disabled = !obj.checked;
        document.forms[0].btn_right5.disabled = !obj.checked;
        document.forms[0].btn_rights5.disabled = !obj.checked;
        if (partId != "1") {
            break;
        }
    case 6:
        document.forms[0].CATEGORY_SELECTED6.disabled = !obj.checked;
        document.forms[0].CATEGORY_NAME6.disabled = !obj.checked;
        document.forms[0].btn_lefts6.disabled = !obj.checked;
        document.forms[0].btn_left6.disabled = !obj.checked;
        document.forms[0].btn_right6.disabled = !obj.checked;
        document.forms[0].btn_rights6.disabled = !obj.checked;
        if (partId != "1") {
            break;
        }
    default:
        break;
    }
    console.log("end");
    return;
}

///////////////////
function moven(side, student, ids)
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
    
    //assign what select attribute treat as attribute1 and attribute2
    if (side == "left") {
        attribute1 = document.forms[0]["CATEGORY_NAME"+ids];
        attribute2 = document.forms[0]["CATEGORY_SELECTED"+ids];
    } else {
        attribute1 = document.forms[0]["CATEGORY_SELECTED"+ids];
        attribute2 = document.forms[0]["CATEGORY_NAME"+ids];
    }

    
    //fill an array with old values
    for (var i = 0; i < attribute2.length; i++) {
        y=current1++
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        if(student){
            tempaa[y] = String(attribute2.options[i].text).substring(9,12)+","+y;
        } else {
            tempaa[y] = attribute2.options[i].value+","+y;
        }
    }

    //assign new values to arrays
    for (var i = 0; i < attribute1.length; i++) {
        if ( attribute1.options[i].selected ) {
            y=current1++
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text; 
            if(student){
                tempaa[y] = String(attribute1.options[i].text).substring(9,12)+","+y;
            } else {
                tempaa[y] = attribute1.options[i].value+","+y;
            }
        } else {
            y=current2++
            temp2[y] = attribute1.options[i].value; 
            tempb[y] = attribute1.options[i].text;
        }
    }

    tempaa.sort();

    //generating new options
    for (var i = 0; i < temp1.length; i++)
    {  
        var val = tempaa[i];
        var tmp = val.split(',');

        attribute2.options[i] = new Option();
        attribute2.options[i].value = temp1[tmp[1]];
        attribute2.options[i].text =  tempa[tmp[1]];
    }

    //generating new options
    ClearList(attribute1,attribute1);
    if (temp2.length>0)
    {    
        for (var i = 0; i < temp2.length; i++)
        {   
            attribute1.options[i] = new Option();
            attribute1.options[i].value = temp2[i];
            attribute1.options[i].text =  tempb[i];
        }
    }

}
function movesn(sides, student, ids)
{   
    var temp5 = new Array();
    var tempc = new Array();
    var tempaa = new Array();
    var current5 = 0;
    var z=0;
    
    //assign what select attribute treat as attribute5 and attribute6
    if (sides == "left")
    {  
        attribute5 = document.forms[0]["CATEGORY_NAME"+ids];
        attribute6 = document.forms[0]["CATEGORY_SELECTED"+ids];
    }
    else
    {  
        attribute5 = document.forms[0]["CATEGORY_SELECTED"+ids];
        attribute6 = document.forms[0]["CATEGORY_NAME"+ids];
    }

    
    //fill an array with old values
    for (var i = 0; i < attribute6.length; i++)
    {  
        z=current5++
        temp5[z] = attribute6.options[i].value;
        tempc[z] = attribute6.options[i].text;
        if(student){
            tempaa[z] = String(attribute6.options[i].text).substring(9,12)+","+z;
        } else {
            tempaa[z] = attribute6.options[i].value+","+z;
        }
    }

    //assign new values to arrays
    for (var i = 0; i < attribute5.length; i++)
    {   
        z=current5++
        temp5[z] = attribute5.options[i].value;
        tempc[z] = attribute5.options[i].text; 
        if(student){
            tempaa[z] = String(attribute5.options[i].text).substring(9,12)+","+z;
        } else {
            tempaa[z] = attribute5.options[i].value+","+z;
        }
    }

    tempaa.sort();

    //generating new options
    for (var i = 0; i < temp5.length; i++)
    {  
        var val = tempaa[i];
        var tmp = val.split(',');

        attribute6.options[i] = new Option();
        attribute6.options[i].value = temp5[tmp[1]];
        attribute6.options[i].text =  tempc[tmp[1]];
    }

    //generating new options
    ClearList(attribute5,attribute5);

}
