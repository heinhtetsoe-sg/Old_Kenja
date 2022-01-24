function btn_submit(cmd) {
    if (cmd == 'teikei') {
        loadwindow(
            'knjd133index.php?cmd=teikei&CHAIRCD=' + document.forms[0].CHAIRCD.value + '&DATA_DIV=' + dataDiv + '&TARGETTEXT=' + targetText + '',
            event.clientX +
                (function () {
                    var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;
                    return scrollX;
                })(),
            event.clientY +
                (function () {
                    var scrollY = document.documentElement.scrollTop || document.body.scrollTop;
                    return scrollY;
                })(),
            650,
            450
        );
        return true;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function showTeikeiWindow(targetText, dataDiv) {
    // 定型文選択画面表示
    loadwindow(
        'knjd133index.php?cmd=teikei&CHAIRCD=' + document.forms[0].CHAIRCD.value + '&DATA_DIV=' + dataDiv + '&TARGETTEXT=' + targetText + '',
        event.clientX +
            (function () {
                var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;
                return scrollX;
            })(),
        event.clientY +
            (function () {
                var scrollY = document.documentElement.scrollTop || document.body.scrollTop;
                return scrollY;
            })(),
        650,
        450
    );
    return true;
}

//（駒沢大学）評価テキスト入力時の処理
function setTeikeiTotalstudyTime(obj) {
    if (!obj.value) {
        return false;
    }
    //学年
    var grade = document.forms[0]['GRADE'].value;
    // 学生が選択されている場合は一人目の生徒の学年を使用する
    var selectStd = document.forms[0]['category_selected'];
    if (selectStd.length > 0) {
        grade = selectStd.options[0].value.substr(0, 2);
    }

    //定型文
    var teikei1 = document.forms[0]['REMARK-TIME-' + grade + '-81-' + obj.value.substr(0, 1) + ''];
    var teikei2 = document.forms[0]['REMARK-TIME-' + grade + '-82-' + obj.value.substr(1, 1) + ''];
    var teikei3 = document.forms[0]['REMARK-TIME-' + grade + '-83-' + obj.value.substr(2, 1) + ''];
    var teikei4 = document.forms[0]['REMARK-TIME-' + grade + '-84-' + obj.value.substr(3, 1) + ''];

    var text = '';
    if (teikei1) text += teikei1.value;
    if (teikei2) text += teikei2.value;
    if (teikei3) text += teikei3.value;
    if (teikei4) text += teikei4.value;
    // 評価に値を設定
    var targetText = document.forms[0]['TOTALSTUDYTIME'];
    if (targetText) targetText.value = text;

    return true;
}

//（駒沢大学）学年評定を設定する
function setHyouteiRank(obj) {
    if (!obj.value) {
        return false;
    }
    //定型文
    var rankA = document.forms[0]['RANK_A'];
    var rankB = document.forms[0]['RANK_B'];
    var rankC = document.forms[0]['RANK_C'];

    var rankAFromTo = ['', 0, 0];
    if (rankA) rankAFromTo = rankA.value.split('_');
    var rankBFromTo = ['', 0, 0];
    if (rankB) rankBFromTo = rankB.value.split('_');
    var rankCFromTo = ['', 0, 0];
    if (rankC) rankCFromTo = rankC.value.split('_');

    var score = 0;
    if (obj.value.substr(0, 1) && Number(obj.value.substr(0, 1))) {
        if (Number(obj.value.substr(0, 1)) <= 5) score += Number(obj.value.substr(0, 1));
    }
    if (obj.value.substr(1, 1) && Number(obj.value.substr(1, 1))) {
        if (Number(obj.value.substr(1, 1)) <= 5) score += Number(obj.value.substr(1, 1));
    }
    if (obj.value.substr(2, 1) && Number(obj.value.substr(2, 1))) {
        if (Number(obj.value.substr(2, 1)) <= 5) score += Number(obj.value.substr(2, 1));
    }
    if (obj.value.substr(3, 1) && Number(obj.value.substr(3, 1))) {
        if (Number(obj.value.substr(3, 1)) <= 5) score += Number(obj.value.substr(3, 1));
    }

    var text = '';
    if (rankAFromTo[1] <= score && score <= rankAFromTo[2]) {
        text += rankAFromTo[0];
    } else if (rankBFromTo[1] <= score && score <= rankBFromTo[2]) {
        text += rankBFromTo[0];
    } else if (rankCFromTo[1] <= score && score <= rankCFromTo[2]) {
        text += rankCFromTo[0];
    }
    // 学年評定に値を設定
    var targetText = document.forms[0]['GRAD_VALUE'];
    if (targetText) targetText.value = text;

    return true;
}

function check_all(obj, s_no) {
    var ii = 0;
    for (var i = 0; i < document.forms[0].elements.length; i++) {
        if (ii == 0 && s_no != 0) {
            ii = s_no;
        }
        if (document.forms[0].elements[i].name == 'RCHECK' + ii) {
            document.forms[0].elements[i].checked = obj.checked;
            ii++;
        }
    }
}

//更新
function doSubmit() {
    attribute3 = document.forms[0].selectdata;
    attribute3.value = '';
    sep = '';

    if (document.forms[0].category_selected.length == 0 && document.forms[0].category_name.length == 0) {
        alert('{rval MSG916}');
        return false;
    }
    for (var i = 0; i < document.forms[0].category_selected.length; i++) {
        attribute3.value += sep + document.forms[0].category_selected.options[i].value.substring(9, 17);
        sep = ',';
    }
    //更新中の画面ロック
    //フレームロック機能（プロパティの値が1の時有効）
    if (document.forms[0].useFrameLock.value == '1') {
        updateFrameLock();
    }

    document.forms[0].cmd.value = 'replace_update';
    document.forms[0].submit();
    return false;
}

function ClearList(OptionList, TitleName) {
    OptionList.length = 0;
}

function AllClearList(OptionList, TitleName) {
    attribute = document.forms[0].category_name;
    ClearList(attribute, attribute);
    attribute = document.forms[0].category_selected;
    ClearList(attribute, attribute);
}

function move1(side) {
    var temp1 = new Array();
    var temp2 = new Array();
    var tempa = new Array();
    var tempb = new Array();
    var tempaa = new Array();
    var current1 = 0;
    var current2 = 0;
    var y = 0;
    var attribute;

    //assign what select attribute treat as attribute1 and attribute2
    if (side == 'left') {
        attribute1 = document.forms[0].category_name;
        attribute2 = document.forms[0].category_selected;
    } else {
        attribute1 = document.forms[0].category_selected;
        attribute2 = document.forms[0].category_name;
    }

    //fill an array with old values
    for (var i = 0; i < attribute2.length; i++) {
        y = current1++;
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        tempaa[y] = attribute2.options[i].value + ',' + y;
    }

    //assign new values to arrays
    for (var i = 0; i < attribute1.length; i++) {
        if (attribute1.options[i].selected) {
            y = current1++;
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text;
            tempaa[y] = attribute1.options[i].value + ',' + y;
        } else {
            y = current2++;
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
        attribute2.options[i].text = tempa[tmp[1]];
    }

    //generating new options
    ClearList(attribute1, attribute1);
    if (temp2.length > 0) {
        for (var i = 0; i < temp2.length; i++) {
            attribute1.options[i] = new Option();
            attribute1.options[i].value = temp2[i];
            attribute1.options[i].text = tempb[i];
        }
    }
}

function moves(sides) {
    var temp5 = new Array();
    var tempc = new Array();
    var tempaa = new Array();
    var current5 = 0;
    var z = 0;

    //assign what select attribute treat as attribute5 and attribute6
    if (sides == 'left') {
        attribute5 = document.forms[0].category_name;
        attribute6 = document.forms[0].category_selected;
    } else {
        attribute5 = document.forms[0].category_selected;
        attribute6 = document.forms[0].category_name;
    }

    //fill an array with old values
    for (var i = 0; i < attribute6.length; i++) {
        z = current5++;
        temp5[z] = attribute6.options[i].value;
        tempc[z] = attribute6.options[i].text;
        tempaa[z] = attribute6.options[i].value + ',' + z;
    }

    //assign new values to arrays
    for (var i = 0; i < attribute5.length; i++) {
        z = current5++;
        temp5[z] = attribute5.options[i].value;
        tempc[z] = attribute5.options[i].text;
        tempaa[z] = attribute5.options[i].value + ',' + z;
    }

    tempaa.sort();

    //generating new options
    for (var i = 0; i < temp5.length; i++) {
        var val = tempaa[i];
        var tmp = val.split(',');

        attribute6.options[i] = new Option();
        attribute6.options[i].value = temp5[tmp[1]];
        attribute6.options[i].text = tempc[tmp[1]];
    }

    //generating new options
    ClearList(attribute5, attribute5);
}
