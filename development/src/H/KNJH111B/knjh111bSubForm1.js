function btn_submit(cmd) {
    if (cmd == 'delete' && !confirm('{rval MSG103}')){
        return true;
    }

    selectdata = document.forms[0].selectdata;
    selectdata.value = "";
    sep = "";
    for (var i = 0; i < document.forms[0].CATEGORY_SELECTED.length; i++) {
        selectdata.value = selectdata.value + sep + document.forms[0].CATEGORY_SELECTED.options[i].value;
        sep = ",";
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function ShowConfirm(){
    if (!confirm('{rval MSG106}')){
        return false;
    }
}

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

function doSubmit() {
    if (!document.forms[0].RCHECK0.checked) {
        alert('{rval MSG301}');
        return false;
    }
    if (!document.forms[0].RCHECK1.checked) {
        alert('{rval MSG301}');
        return false;
    }
    if (!document.forms[0].RCHECK2.checked) {
        alert('{rval MSG301}');
        return false;
    }
    if (!document.forms[0].REGDDATE.value) {
        alert('{rval MSG301}');
        return false;
    }
    if (!document.forms[0].QUALIFIED_CD.value) {
        alert('{rval MSG301}');
        return false;
    }
    if (document.forms[0].managementFlg.value == "1") {
        if (!document.forms[0].RANK.value) {
            alert('{rval MSG301}\n\n'+'級・段位');
            return false;
        }
    }

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
    if (!checkFlag) {
        alert("最低ひとつチェックを入れてください。");
        return false;
    }

    if (!confirm('{rval MSG102}')) {
        return false;
    }
    selectdata = document.forms[0].selectdata;
    selectdata.value = "";
    sep = "";
    if (document.forms[0].CATEGORY_SELECTED.length==0 && document.forms[0].CATEGORY_NAME.length==0) {
        alert('{rval MSG916}');
        return false;
    }
    for (var i = 0; i < document.forms[0].CATEGORY_SELECTED.length; i++) {
        selectdata.value = selectdata.value + sep + document.forms[0].CATEGORY_SELECTED.options[i].value;
        sep = ",";
    }
    document.forms[0].cmd.value = 'replace_update1';
    document.forms[0].submit();
    return false;
}

function temp_clear() {
    ClearList(document.forms[0].CATEGORY_SELECTED,document.forms[0].CATEGORY_SELECTED);
    ClearList(document.forms[0].CATEGORY_NAME,document.forms[0].CATEGORY_NAME);
}
//数値かどうかをチェック
function Num_Check(obj){
    var name = obj.name;
    var checkString = obj.value;
    var newString ="";
    var count = 0;

    for (i = 0; i < checkString.length; i++) {
        ch = checkString.substring(i, i+1);
        if ((ch >= "0" && ch <= "9") || (ch == ".")) {
            newString += ch;
        }
    }
    if (checkString != newString) {
        alert('{rval MSG901}\n数値を入力してください。');
        obj.value="";
        obj.focus();
        return false;
    }
}


function ClearList(OptionList, TitleName) {
    OptionList.length = 0;
}

function move1(side) {
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
        attribute1 = document.forms[0].CATEGORY_NAME;
        attribute2 = document.forms[0].CATEGORY_SELECTED;
    } else {
        attribute1 = document.forms[0].CATEGORY_SELECTED;
        attribute2 = document.forms[0].CATEGORY_NAME;  
    }

    for (var i = 0; i < attribute2.length; i++) {
        y=current1++
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        tempaa[y] = attribute2.options[i].text+","+y;
    }

    for (var i = 0; i < attribute1.length; i++) {
        if ( attribute1.options[i].selected ) {
            y=current1++
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text; 
            tempaa[y] = attribute1.options[i].text+","+y;
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
}

function moves(sides) {
    var temp5 = new Array();
    var tempc = new Array();
    var tempaa = new Array();
    var current5 = 0;
    var z=0;
    
    if (sides == "left") {
        attribute5 = document.forms[0].CATEGORY_NAME;
        attribute6 = document.forms[0].CATEGORY_SELECTED;
    } else {
        attribute5 = document.forms[0].CATEGORY_SELECTED;
        attribute6 = document.forms[0].CATEGORY_NAME;  
    }

    for (var i = 0; i < attribute6.length; i++) {
        z=current5++
        temp5[z] = attribute6.options[i].value;
        tempc[z] = attribute6.options[i].text;
        tempaa[z] = attribute6.options[i].text+","+z;
    }

    for (var i = 0; i < attribute5.length; i++) {
        z=current5++
        temp5[z] = attribute5.options[i].value;
        tempc[z] = attribute5.options[i].text; 
        tempaa[z] = attribute5.options[i].text+","+z;
    }

    tempaa.sort();

    for (var i = 0; i < temp5.length; i++) {
        var val = tempaa[i];
        var tmp = val.split(',');

        attribute6.options[i] = new Option();
        attribute6.options[i].value = temp5[tmp[1]];
        attribute6.options[i].text =  tempc[tmp[1]];
    }

    ClearList(attribute5,attribute5);

}

