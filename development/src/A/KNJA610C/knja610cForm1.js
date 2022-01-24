function btn_submit(cmd) {
    if (cmd == "init"){
        AllClearList();
        document.forms[0].SELECT_HR_CLASS.value = "";
    }
    if (cmd == "csv") {
        //出力対象クラスの選択チェック
        if (document.forms[0].CLASS_SELECTED.length == 0) {
            alert('{rval MSG916}');
            return;
        }
        //適用日付のチェック
        if (document.forms[0].APPDATE.value == "") {
            alert('{rval MSG902}');
            return;
        }
        if (!isDate(document.forms[0].APPDATE)) {
            return;
        }

        var attribute = document.forms[0].SELECT_HR_CLASS;
        attribute.value = "";
        sep = "";
        // クラス一覧の選択解除
        for (var i = 0; i < document.forms[0].CLASS_NAME.length; i++) {  
            document.forms[0].CLASS_NAME.options[i].selected = 0;
        }
        // 出力対象クラスを全設定
        for (var i = 0; i < document.forms[0].CLASS_SELECTED.length; i++) {
            document.forms[0].CLASS_SELECTED.options[i].selected = 1;
            attribute.value = attribute.value + sep + document.forms[0].CLASS_SELECTED.options[i].value;
            sep = ",";
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}


function ClearList(OptionList, TitleName) {
    OptionList.length = 0;
}

function AllClearList(OptionList, TitleName) {
        var attribute = document.forms[0].CLASS_NAME;
        ClearList(attribute,attribute);
        var attribute = document.forms[0].CLASS_SELECTED;
        ClearList(attribute,attribute);
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
