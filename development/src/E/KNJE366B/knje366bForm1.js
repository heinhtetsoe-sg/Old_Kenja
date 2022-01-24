
function btn_submit(cmd) {
    if (cmd == "csv") {
        //出力対象一覧が選択されてない場合はメッセージ表示
        if (document.forms[0].CLASS_SELECTED.length == 0 || document.forms[0].CATEGORY_SELECTED_TYPE.length == 0) {
            alert('{rval MSG916}');
            return;
        }

        var attribute = document.forms[0].SELECTDATA;
        attribute.value = "";
        sep = "";
        //クラス一覧を未選択状態に変更
        for (var i = 0; i < document.forms[0].CLASS_NAME.length; i++) {
            document.forms[0].CLASS_NAME.options[i].selected = 0;
        }
        //出力対象一覧（クラス一覧）を選択状態に変更
        for (var i = 0; i < document.forms[0].CLASS_SELECTED.length; i++) {
            document.forms[0].CLASS_SELECTED.options[i].selected = 1;
            attribute.value += sep + document.forms[0].CLASS_SELECTED.options[i].value;
            sep = ",";
        }
        var attribute_Type = document.forms[0].SELECTTYPEDATA;
        attribute_Type.value = "";
        sep = "";
        for (var i = 0; i < document.forms[0].CATEGORY_SELECTED_TYPE.length; i++) {
            attribute_Type.value += sep + document.forms[0].CATEGORY_SELECTED_TYPE.options[i].value;
            sep = ",";
        }
    }
    if (cmd == "changeYear") {
        var attribute = document.forms[0].SELECTDATA;
        attribute.value = "";
        var attribute_Type = document.forms[0].SELECTTYPEDATA;
        attribute_Type.value = "";
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//「進路分析資料」選択時、「模試コード」を使用可にする
function onChangeOutDiv() {
    document.forms[0].COMPANYCD.disabled = true;
    document.forms[0].MOCKCD.disabled = true;
    if (document.forms[0].OUT_DIV2.checked) {
        document.forms[0].COMPANYCD.disabled = false;
        document.forms[0].MOCKCD.disabled = false;
    }
}

//印刷
function newwin(SERVLET_URL){
    if (document.forms[0].CLASS_SELECTED.length == 0 || document.forms[0].CATEGORY_SELECTED_TYPE.length == 0) {
        alert('{rval MSG916}');
        return;
    }

    var attribute = document.forms[0].SELECTDATA;
    attribute.value = "";
    sep = "";

    //クラス一覧を未選択状態に変更
    for (var i = 0; i < document.forms[0].CLASS_NAME.length; i++) {
        document.forms[0].CLASS_NAME.options[i].selected = 0;
    }
    //出力対象一覧（クラス一覧）を選択状態に変更
    for (var i = 0; i < document.forms[0].CLASS_SELECTED.length; i++) {
        document.forms[0].CLASS_SELECTED.options[i].selected = 1;
        attribute.value += sep + document.forms[0].CLASS_SELECTED.options[i].value;
        sep = ",";
    }
    var attribute_Type = document.forms[0].SELECTTYPEDATA;
    attribute_Type.value = "";
    sep = "";
    for (var i = 0; i < document.forms[0].CATEGORY_SELECTED_TYPE.length; i++) {
        attribute_Type.value += sep + document.forms[0].CATEGORY_SELECTED_TYPE.options[i].value;
        sep = ",";
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

    document.forms[0].action = SERVLET_URL +"/KNJE";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

function ClearList(OptionList, TitleName) {
    OptionList.length = 0;
}

function AllClearList(OptionList, TitleName) {
        var attribute = document.forms[0].CLASS_NAME;
        ClearList(attribute, attribute);
        var attribute = document.forms[0].CLASS_SELECTED;
        ClearList(attribute, attribute);
}

//クラス選択／取消（一部）
function move(side, allFlg) {

    var attribute1 = [];    // 移動元
    var attribute2 = [];    // 移動先
    var optionList = [];    // ソート用

    if (side == "left") {  
        attribute1 = document.forms[0].CLASS_NAME;
        attribute2 = document.forms[0].CLASS_SELECTED;
    } else {
        attribute1 = document.forms[0].CLASS_SELECTED;
        attribute2 = document.forms[0].CLASS_NAME;  
    }

    // 移動先のオプションを全取得
    for (let index = 0; index < attribute2.length; index++) {
        const option = attribute2.options[index];
        optionList.push(option);
    }
    // 選択されたオプションを移動
    for (let i = 0; i < attribute1.length; i++) {
        const option = attribute1.options[i];
        if (allFlg == "ALL") {
            option.selected = false;
            optionList.push(option);
        } else {
            if (option.selected) {
                option.selected = false;
                optionList.push(option);
            }
        }
    }

    // オプションのソート
    optionList.sort(function(option1, option2) {
        if (option1.value < option2.value) return -1;
        if (option1.value > option2.value) return 1;
        return 0;
    });

    // 移動先のオプションを全削除後、ソートされたオプションを追加
    attribute2.length = 0;
    for (let i = 0; i < optionList.length; i++) {
        const option = optionList[i];
        attribute2.appendChild(option);
    }
}

//クラス選択／取消（一部）
function moveType(side, allFlg) {

    var attribute1 = [];    // 移動元
    var attribute2 = [];    // 移動先
    var optionList = [];    // ソート用

    if (side == "left") {  
        attribute1 = document.forms[0].CATEGORY_NAME_TYPE;
        attribute2 = document.forms[0].CATEGORY_SELECTED_TYPE;
    } else {
        attribute1 = document.forms[0].CATEGORY_SELECTED_TYPE;
        attribute2 = document.forms[0].CATEGORY_NAME_TYPE;  
    }

    // 移動先のオプションを全取得
    for (let index = 0; index < attribute2.length; index++) {
        const option = attribute2.options[index];
        optionList.push(option);
    }
    // 選択されたオプションを移動
    for (let i = 0; i < attribute1.length; i++) {
        const option = attribute1.options[i];
        if (allFlg == "ALL") {
            option.selected = false;
            optionList.push(option);
        } else {
            if (option.selected) {
                option.selected = false;
                optionList.push(option);
            }
        }
    }

    // オプションのソート
    optionList.sort(function(option1, option2) {
        if (option1.value < option2.value) return -1;
        if (option1.value > option2.value) return 1;
        return 0;
    });

    // 移動先のオプションを全削除後、ソートされたオプションを追加
    attribute2.length = 0;
    for (let i = 0; i < optionList.length; i++) {
        const option = optionList[i];
        attribute2.appendChild(option);
    }
}

