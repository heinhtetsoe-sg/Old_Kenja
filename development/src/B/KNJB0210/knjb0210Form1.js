
function btn_submit(cmd) {

    if (cmd == "update" || cmd == "read") {
        attribute3 = document.forms[0].selectleft;
        attribute3.value = "";
        sep = "";
        for (var i = 0; i < document.forms[0].category_name.length; i++) {
            document.forms[0].category_name.options[i].selected = 0;
        }

        for (var i = 0; i < document.forms[0].category_selected.length; i++) {
            document.forms[0].category_selected.options[i].selected = 1;
            attribute3.value = attribute3.value + sep + document.forms[0].category_selected.options[i].value;
            sep = ",";
        }
    }
    if (cmd == "update") {
        var errMsg = "";
        var errSep = "";
        var dataNasi = true;
        for (var i = 1; i <= document.forms[0].joukenCnt.value; i++) {

            var obj_updName = "UPD" + i + "_HIDEEN";
            if (document.forms[0][obj_updName].length == 0) {
                continue;
            }
            var rowCnt = charCount(document.forms[0][obj_updName].value, 1, 200, false);
            if (!rowCnt["FLG"]) {
                errMsg += errSep + "条件" + i;
                errSep = "/";
            }
            if (document.forms[0][obj_updName].value != "") {
                dataNasi = false;
            }
        }
        if (dataNasi) {
            alert('条件設定をして下さい。')
            return false;
        }
        if (document.forms[0].category_selected.length == 0) {
            alert('科目をして下さい。')
            return false;
        }
        if (errMsg != "") {
            alert(errMsg + '文字数オーバーです。')
            return false;
        }
        if (document.forms[0].SET_GRADE.value == "") {
            alert('設定学年を指定して下さい。')
            return false;
        }
        if (document.forms[0].SET_GROUP_CD.value == "") {
            alert('必履修グループを指定して下さい。')
            return false;
        }
    }
    if (cmd == 'setAnd') {
        dataSet(' AND ');
        return true;
    } else if (cmd == 'setOr') {
        dataSet(' OR ');
        return true;
    } else if (cmd == 'setKakko') {
        dataSet(' () ');
        return true;
    }

    cmd = cmd == "search" ? "knjb0210" : cmd;

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function dataSet(setText) {
    //旧IE
    if (typeof(document.selection) != "undefined") {
        textRange = null;
        dataPosition();
        dataPositionSet(setText);
    } else {
        dataPositionSet2(setText);
    }
}

function dataPositionSet(setText) {
    textRange.text = setText;

    // キャレット位置を挿入した文字列の最後尾に移動
    textRange.select();
}

function dataPositionSet2(setText) {
    positionTarget = eval("document.forms[0]." + document.forms[0].TEXTNAME.value);
    var posCursole = document.getElementById(document.forms[0].TEXTNAME.value).selectionStart;
    //カーソル位置より左の文字列
    var leftPart = positionTarget.value.substr(0, posCursole);
    //カーソル位置より右の文字列
    var rightPart = positionTarget.value.substr(posCursole, positionTarget.value.length);

    //文字列を結合して、テキストエリアに出力
    positionTarget.value = leftPart + setText + rightPart;

    //キャレット移動用
    var leftPartPlus = leftPart + setText;
    positionTarget.setSelectionRange(leftPartPlus.length, leftPartPlus.length);
}

function subclassHanei() {
    for (var i = 1; i <= document.forms[0].joukenCnt.value; i++) {
        //条件テキスト
        var obj_name = "JOUKEN" + i;
        var setObj = document.forms[0][obj_name];
        //条件取消用(取消時に戻せるよう退避しておく)
        var obj_hiddenName = "JOUKEN" + i + "_HIDEEN";
        //条件更新用(テキスト表示は日本語なので更新用に科目コードを持っておく)
        var obj_updName = "UPD" + i + "_HIDEEN";
        var setUpdObj = document.forms[0][obj_updName];
        setUpdObj.value = setObj.value;

        //数値があれば取消用に退避
        if (String(setObj.value).search(/[0-9]/) >= 0) {
            document.forms[0][obj_hiddenName].value = setObj.value;
        }

        //数値を【】付きで保持する
        var setUpdArray = Array();

        for (var subI = document.forms[0].category_selected.length; subI > 0; subI--) {
            //置換科目名
            setValue = document.forms[0].category_selected.options[subI - 1].text;
            //置換科目コード
            setUpdValue = document.forms[0].category_selected.options[subI - 1].value;
            //】の位置
            plusVal = String(setValue).search("】");
            //数値部分の取得
            subIplus = String(setValue).substring(1, plusVal);
            //【】付き数値の取得
            subUpdIplus = String(setValue).substring(0, plusVal + 1);
            //：の位置
            var setSearchCnt = String(setValue).search(":") + 1;
            //：より後を取得(科目名)
            setValue = String(setValue).slice(setSearchCnt);
            //条件テキストの数値を科目名に置換
            setObj.value = String(setObj.value).split(subIplus).join(setValue);
            //連想配列 例：【1】=> 01-H-123456
            //             【2】=> 01-H-654321
            setUpdArray[subUpdIplus] = setUpdValue;
            //更新用の数値を【】付き数値に置換
            setUpdObj.value = String(setUpdObj.value).split(subIplus).join("'" + subUpdIplus + "'");
        }
        //更新用の【】付き数値を科目コードに置換
        for (var keyString in setUpdArray) {
            setUpdObj.value = String(setUpdObj.value).split(keyString).join(setUpdArray[keyString]);
        }
        setObj.readOnly = true;
        document.forms[0][obj_name].style.backgroundColor = '#CCCCCC';
    }
    document.forms[0].btn_set.disabled = true;
    document.forms[0].btn_update.disabled = false;
}

function retry() {
    for (var i = 1; i <= document.forms[0].joukenCnt.value; i++) {
        var obj_name = "JOUKEN" + i;
        var obj_hiddenName = "JOUKEN" + i + "_HIDEEN";
        document.forms[0][obj_name].value = document.forms[0][obj_hiddenName].value;
        document.forms[0][obj_name].readOnly = false;
        document.forms[0][obj_name].style.backgroundColor = '#FFFFFF';
    }
    document.forms[0].btn_set.disabled = false;
    document.forms[0].btn_update.disabled = true;
}

function ClearList(OptionList, TitleName) 
{
    OptionList.length = 0;
}
    
function AllClearList(OptionList, TitleName) 
{
    attribute = document.forms[0].category_name;
    ClearList(attribute,attribute);
    attribute = document.forms[0].category_selected;
    ClearList(attribute,attribute);
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
    var y=0;
    var attribute;
    
    if (side == "left") {
        attribute1 = document.forms[0].category_name;
        attribute2 = document.forms[0].category_selected;
    } else {
        attribute1 = document.forms[0].category_selected;
        attribute2 = document.forms[0].category_name;  
    }

    var setTempCnt = 1;
    for (var i = 0; i < attribute2.length; i++) {
        y=current1++
        temp1[y] = attribute2.options[i].value;
        var setTempa = attribute2.options[i].text;
        if (side == "left") {
            if (String(setTempa).search("】") >= 0) {
                var setSearchCnt = String(setTempa).search("】") + 1;
                setTempa = String(setTempa).slice(setSearchCnt);
            }
            if (String(setTempa).search("【") < 0) {
                setTempa = "【" + setTempCnt + "】" + setTempa;
            }
        } else {
            if (String(setTempa).search("】") >= 0) {
                var setSearchCnt = String(setTempa).search("】") + 1;
                setTempa = String(setTempa).slice(setSearchCnt);
            }
        }
        tempa[y] = setTempa;
        tempaa[y] = String(attribute2.options[i].value)+","+y;
        setTempCnt++;
    }

    for (var i = 0; i < attribute1.length; i++) {
        if (attribute1.options[i].selected) {
            y=current1++
            temp1[y] = attribute1.options[i].value;
            var setTempa = attribute1.options[i].text;
            if (side == "left") {
                if (String(setTempa).search("【") < 0) {
                    setTempa = "【" + setTempCnt + "】" + setTempa;
                }
            } else {
                if (String(setTempa).search("】") >= 0) {
                    var setSearchCnt = String(setTempa).search("】") + 1;
                    setTempa = String(setTempa).slice(setSearchCnt);
                }
            }
            tempa[y] = setTempa;
            tempaa[y] = String(attribute1.options[i].value)+","+y;
            setTempCnt++;
        } else {
            y=current2++
            temp2[y] = attribute1.options[i].value; 
            tempb[y] = attribute1.options[i].text;
        }
    }

    if (side == "right") {
        tempaa.sort();
    }

    for (var i = 0; i < temp1.length; i++) {
        var val = tempaa[i];
        var tmp = val.split(',');

        attribute2.options[i] = new Option();
        attribute2.options[i].value = temp1[tmp[1]];
        attribute2.options[i].text =  tempa[tmp[1]];
    }

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
function moves(sides)
{
    var temp5 = new Array();
    var tempc = new Array();
    var tempaa = new Array();
    var current5 = 0;
    var z=0;
    if (sides == "left") {
        attribute5 = document.forms[0].category_name;
        attribute6 = document.forms[0].category_selected;
    } else {
        attribute5 = document.forms[0].category_selected;
        attribute6 = document.forms[0].category_name;  
    }

    for (var i = 0; i < attribute6.length; i++) {
        z=current5++
        temp5[z] = attribute6.options[i].value;
        tempc[z] = attribute6.options[i].text;
        tempaa[z] = String(attribute6.options[i].value)+","+z;
    }

    var setTempCnt = 1;
    for (var i = 0; i < attribute5.length; i++) {
        z=current5++
        temp5[z] = attribute5.options[i].value;
        var setTempa = attribute5.options[i].text;
        if (sides == "left") {
            if (String(setTempa).search("【") < 0) {
                setTempa = "【" + setTempCnt + "】" + setTempa;
            }
        } else {
            if (String(setTempa).search("】") >= 0) {
                var setSearchCnt = String(setTempa).search("】") + 1;
                setTempa = String(setTempa).slice(setSearchCnt);
            }
        }
        tempc[z] = setTempa;
        tempaa[z] = String(attribute5.options[i].value)+","+z;
        setTempCnt++;
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
function check(obj){
    
    if (!obj.value || obj.value == 0){
        alert('出力部数を指定して下さい。');
        obj.value = '1';
        obj.focus();
    }
}

$(function(){
    $('.subMenu').hide();
    //
    $('#menu .archive').click(function(e){     
        $('+ul.subMenu',this).slideToggle();
    });
});
