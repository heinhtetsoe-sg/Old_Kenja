// kanji=漢字
function btn_submit(cmd) {
    if (cmd == 'exec' && !confirm('{rval MSG101}')) {
        return;
    }
    if (cmd == 'reset' && !confirm('{rval MSG106}')) {
        return true;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//印刷
function newwin(SERVLET_URL){

    action = document.forms[0].action;
    target = document.forms[0].target;

//  url = location.hostname;
//  document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJL";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
//更新
function doSubmit(cmd) {
//        alert('作成中です。');
//        return false;
    if (document.forms[0].BASE_SELECTED.length == 0 && document.forms[0].BEFORE_SELECTED.length == 0) {
        alert('{rval MSG304}');
        return false;
    }
    if (cmd == 'exec' && !confirm('{rval MSG102}')) {
        return;
    }

    var data  = "";
    sep = "";
    if (document.forms[0].BASE_SELECTED.length > 0) {
        for (var i = 0; i < document.forms[0].BASE_SELECTED.length; i++) {
            data += sep + document.forms[0].BASE_SELECTED.options[i].value;
            sep = ",";
        }
    }
    document.forms[0].upd_data_base.value = data;

    data  = "";
    sep = "";
    if (document.forms[0].BEFORE_SELECTED.length > 0) {
        for (var i = 0; i < document.forms[0].BEFORE_SELECTED.length; i++) {
            data += sep + document.forms[0].BEFORE_SELECTED.options[i].value;
            sep = ",";
        }
    }
    document.forms[0].upd_data_before.value = data;

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}

function ClearList(OptionList) {
    OptionList.length = 0;
}

function move2(side, left, right, sort)
{
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
    if (side == "right" || side == "sel_del_all")
    {
        attribute1 = document.forms[0][left];
        attribute2 = document.forms[0][right];
    }
    else
    {
        attribute1 = document.forms[0][right];
        attribute2 = document.forms[0][left];
    }
    //fill an array with old values
    for (var i = 0; i < attribute2.length; i++)
    {
        y=current1++
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        a[temp1[y]] = tempa[y];
    }
    //assign new values to arrays
    var fukusuSigan = [];
    var fukusuJizen = [];
    for (var i = 0; i < attribute1.length; i++)
    {
        if (side == "right" || side == "left")
        {
            var errcheck = attribute1.options[i].value.split("-");
            if (attribute1.options[i].selected && errcheck[3] == "OK") {
                //上段での重複
                if (side == "left" && document.forms[0].BASE_SELECTED.length > 0) {
                    for (var baseI = 0; baseI < document.forms[0].BASE_SELECTED.length; baseI++) {
                        var errcheckBase = document.forms[0].BASE_SELECTED.options[baseI].value.split("-");
                        if (errcheckBase[0] == errcheck[0]) {
                            alert('志願者重複が上段' + (baseI + 1) + '行目に存在します。');
                            attribute1.options[i].selected = false;
                            return false;
                        }
                        if (errcheckBase[1] == errcheck[1] && errcheckBase[2] == errcheck[2]) {
                            alert('事前重複が上段' + (baseI + 1) + '行目に存在します。');
                            attribute1.options[i].selected = false;
                            return false;
                        }
                    }
                }
                //下段での重複
                if (side == "left" && document.forms[0].BEFORE_SELECTED.length > 0) {
                    for (var beforeI = 0; beforeI < document.forms[0].BEFORE_SELECTED.length; beforeI++) {
                        var errcheckBefore = document.forms[0].BEFORE_SELECTED.options[beforeI].value.split("-");
                        if (errcheckBefore[0] == errcheck[0]) {
                            alert('志願者重複が下段' + (beforeI + 1) + '行目に存在します。');
                            attribute1.options[i].selected = false;
                            return false;
                        }
                        if (errcheckBefore[1] == errcheck[1] && errcheckBefore[2] == errcheck[2]) {
                            alert('事前重複が下段' + (beforeI + 1) + '行目に存在します。');
                            attribute1.options[i].selected = false;
                            return false;
                        }
                    }
                }

                //複数選択での重複
                var fukusuCnt = 1;
                for (var key in fukusuSigan) {
                    if (key == errcheck[0]) {
                        alert('志願者重複が複数選択内に存在します。');
                        attribute1.options[i].selected = false;
                        return false;
                    }
                    fukusuCnt++;
                }
                fukusuCnt = 1;
                for (var key in fukusuJizen) {
                    if (key == errcheck[1] + "" + errcheck[2]) {
                        alert('事前重複が複数選択内に存在します。');
                        attribute1.options[i].selected = false;
                        return false;
                    }
                    fukusuCnt++;
                }
                fukusuSigan[errcheck[0]] = 1;
                fukusuJizen[errcheck[1] + "" + errcheck[2]] = 1;

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
    if (sort){
        //sort
        temp1 = temp1.sort();
        //generating new options
        for (var i = 0; i < temp1.length; i++)
        {
            tempa[i] = a[temp1[i]];
        }
    }

    //generating new options
    for (var i = 0; i < temp1.length; i++)
    {
        attribute2.options[i] = new Option();
        attribute2.options[i].value = temp1[i];
        attribute2.options[i].text =  tempa[i];
    }
    
    //generating new options
    ClearList(attribute1);
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
