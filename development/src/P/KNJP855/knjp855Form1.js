// kanji=漢字

function btn_submit(cmd) {

    //出力項目取得
    var tmp = "";
    var categorySelected = document.forms[0].CATEGORY_SELECTED;
    var sep = "";
    for (var i = 0; i < categorySelected.options.length; i++) {
        tmp += sep + categorySelected.options[i].value;
        sep = ",";
    }

    if (cmd == "csv") {
        if (document.forms[0].OUTPUT[0].checked == true) {
            cmd = 'data';
        //エラー出力
        } else if (document.forms[0].OUTPUT[1].checked == true) {
            cmd = 'exec';
        } else {
            cmd = 'err'
        }
    }

    if (cmd == "exec") {
        //割当グループコードが無い場合
        var grpCdFrom = document.forms[0].GRPCD_FROM.value;
        var grpCdTo = document.forms[0].GRPCD_TO.value;
        if (grpCdFrom == "" || grpCdTo == "") {
            alert("{rval MSG917}" + "\nグループコードの割振り範囲が設定されていません。");
            return;
        }
        var nextGrpCd = document.forms[0].NEXT_GRPCD.value;
        if (nextGrpCd == "") {
            alert("{rval MSG917}" + "\nグループコードが割振り範囲を超えています。");
            return;
        }
        
        //徴収月が指定した入金パターンに含まれるかチェック
        if (!chkMonthInPattern()) {
            alert("{rval MSG203}" + "\n徴収月には入金パターンに設定された月を指定してください。");
            return;
        }
    } else if (cmd == "data") {
        if (categorySelected.options.length == 0) {
            alert("{rval MSG916}" + "\n出力項目を1つ以上選択してください。");
            return;
        }
    }

    document.forms[0].encoding = "multipart/form-data";
    document.forms[0].selectdata.value = tmp;
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function chkMonthInPattern() {
    var response = {"EXIST_FLG" : false};
    var month = document.forms[0].COLLECT_MONTH.value;
    var patternCd = document.forms[0].COLLECT_PATTERN_CD.value;
    $.ajax({
        url: "knjp855index.php",
        type: "POST",
        data: {
            COLLECT_MONTH: month,
            COLLECT_PATTERN_CD: patternCd,
            cmd: "chk_month",
        },
        async: false,
    }).done(function (data, textStatus, jqXHR) {
        response = $.parseJSON(data);
    });

    return response["EXIST_FLG"];
}

function doMove(side) {
    var motoSel;
    var sakiSel;
    if (side == "left") {
        motoSel = document.forms[0].CATEGORY_NAME;
        sakiSel = document.forms[0].CATEGORY_SELECTED;
    } else {
        motoSel = document.forms[0].CATEGORY_SELECTED;
        sakiSel = document.forms[0].CATEGORY_NAME;
    }

    var selectedMotoList = motoSel.querySelectorAll("option:checked");
    var addFragment = document.createDocumentFragment();
    var movedToSakiArray = new Array();

    //移動前処理
    if (!beforeMoveFunc(side)) {
        return;
    }

    //移動元リストの選択項目を移動先用配列に追加
    for (var i = 0; i < selectedMotoList.length; i++) {
        var selectedMotoElem = selectedMotoList[i];
        movedToSakiArray.push(selectedMotoElem);
        selectedMotoElem.selected = false;
    }

    //一時配列に移動先項目を追加(移動元項目とのソートのため)
    for (var i = 0; i < sakiSel.length; i++) {
        var sakiElem = sakiSel[i];
        movedToSakiArray.push(sakiElem);
    }

    //一時配列をソート
    movedToSakiArray.sort(moveSortFunc);

    //一時配列内の項目をdocumentFragmentに追加
    for (var i = 0; i < movedToSakiArray.length; i++) {
        addFragment.appendChild(movedToSakiArray[i]);
    }

    //移動先リストに追加
    sakiSel.appendChild(addFragment);

    //移動後処理
    afterMoveFunc();
}

//リストのソート順番
function moveSortFunc(firstElem, secondElem) {
    return Number(firstElem.value) - Number(secondElem.value);
}

//リスト移動前処理
function beforeMoveFunc(side) {
    var motoSel = document.forms[0].CATEGORY_NAME;
    var sakiSel = document.forms[0].CATEGORY_SELECTED;

    var selectedMotoList = motoSel.querySelectorAll("option:checked");
    var sakiList = sakiSel.querySelectorAll("option");

    var outputMaxCnt = document.forms[0].OUTPUT_MAX_CNT.value;

    var leftCnt = Number(sakiList.length);
    var moveCnt = Number(selectedMotoList.length);

    //出力最大項目数を超えたらエラー
    if (side == "left" && leftCnt + moveCnt > outputMaxCnt) {
        alert("{rval MSG915}" + "\n出力項目は最大" + outputMaxCnt + "項目までです。");
        return false;
    }
    return true;
}

//リスト移動後処理
function afterMoveFunc() {
    //移動後処理
    updateListCnt();
}

function moves(sides) {
    if (sides == "left") {
        motoSel = document.forms[0].CATEGORY_NAME;
    } else {
        motoSel = document.forms[0].CATEGORY_SELECTED;
    }

    //移動元リストの全選択
    var motoList = motoSel.querySelectorAll("option");
    for (var i = 0; i < motoList.length; i++) {
        motoList[i].selected = true;
    }

    //選択項目の移動処理
    doMove(sides);
}

//リスト移動後の件数更新
function updateListCnt() {
    var leftCnt = document.getElementById("left_cnt");
    var rightCnt = document.getElementById("right_cnt");

    leftCnt.textContent = document.forms[0].CATEGORY_SELECTED.options.length; //左
    rightCnt.textContent = document.forms[0].CATEGORY_NAME.options.length; //右
}
