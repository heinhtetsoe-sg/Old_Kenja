function btn_submit(cmd) {

    if (cmd == 'init') {
        document.forms[0].selectTestInfo.value = "";
        document.forms[0].selectSubclass.value = "";
    }
    if (cmd == 'csv') {
        var sep = "";
        var attribute = document.forms[0].TESTINFO_SELECTED;
        document.forms[0].selectTestInfo.value = "";
        for (let i = 0; i < attribute.length; i++) {
            const option = attribute[i];
            document.forms[0].selectTestInfo.value += sep + option.value;
            sep = ",";
        }
        sep = "";
        var attribute = document.forms[0].SUBCLASS_SELECTED;
        document.forms[0].selectSubclass.value = "";
        for (let i = 0; i < attribute.length; i++) {
            const option = attribute[i];
            document.forms[0].selectSubclass.value += sep + option.value;
            sep = ",";
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function ClearList(OptionList) {
    OptionList.length = 0;
}

//クラス選択／取消（一部）
function move(category, side, allFlg) {

    // ソート有無
    var isSort = false;
    var attribute1 = [];    // 移動元
    var attribute2 = [];    // 移動先
    var optionList = [];    // ソート用

    if (category == "TESTINFO") {
        if (side == "left") {  
            attribute1 = document.forms[0].TESTINFO_NAME;
            attribute2 = document.forms[0].TESTINFO_SELECTED;
        } else {
            attribute1 = document.forms[0].TESTINFO_SELECTED;
            attribute2 = document.forms[0].TESTINFO_NAME;  
        }
    } else {
        if (side == "left") {  
            attribute1 = document.forms[0].SUBCLASS_NAME;
            attribute2 = document.forms[0].SUBCLASS_SELECTED;
        } else {
            attribute1 = document.forms[0].SUBCLASS_SELECTED;
            attribute2 = document.forms[0].SUBCLASS_NAME;  
        }
    }

    //移動先が一覧の場合はソートする
    if (attribute2 == document.forms[0].TESTINFO_NAME
        || attribute2 == document.forms[0].SUBCLASS_NAME) {
        isSort = true;
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

    if (isSort) {
        // オプションのソート
        optionList.sort(function(option1, option2) {
            if (option1.value < option2.value) return -1;
            if (option1.value > option2.value) return 1;
            return 0;
        });
    }
    // 移動先のオプションを全削除後、ソートされたオプションを追加
    attribute2.length = 0;
    for (let i = 0; i < optionList.length; i++) {
        const option = optionList[i];
        attribute2.appendChild(option);
    }

}
