function btn_submit(cmd) {
    //取消確認
    if (cmd == 'reset' && !confirm('{rval MSG106}')) {
        return false;
    }
    //削除確認
    if (cmd == "delete") {
        if (!confirm('{rval MSG103}')) {
            return false;
        } else {
            //フレームロック機能（プロパティの値が1の時有効）
            if (document.forms[0].useFrameLock.value == "1") {
                //更新中の画面ロック
                updateFrameLock()
            }
        }
    }
    //更新
    if (cmd == 'update') {

        //対象コース
        var sep = "";
        var courseSelect = "";
        var options = document.forms[0].CATEGORY_COURSE_SELECTED.options;
        for (var i = 0; i < options.length; i++) {
            const option = options[i];
            courseSelect += sep + option.value;
            sep = ",";
        }
        document.forms[0].COURSE_SELECTED.value = courseSelect;

        //対象科目
        var sep = "";
        var subclassSelect = "";
        var options = document.forms[0].CATEGORY_SUBCLASS_SELECTED.options;
        for (var i = 0; i < options.length; i++) {
            const option = options[i];
            subclassSelect += sep + option.value;
            sep = ",";
        }
        document.forms[0].SUBCLASS_SELECTED.value = subclassSelect;

        //フレームロック機能（プロパティの値が1の時有効）
        if (document.forms[0].useFrameLock.value == "1") {
            //更新中の画面ロック
            updateFrameLock()
        }
    }
    //コピー確認
    if (cmd == "copy" && !confirm('{rval MSG101}')) {
        return false;
    }

    //サブミット
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//コースのオプション移動
function moveCourse(side, allFlg) {

    var attribute1 = [];    // 移動元
    var attribute2 = [];    // 移動先

    if (side == "left") {  
        attribute1 = document.forms[0].CATEGORY_COURSE_LIST;
        attribute2 = document.forms[0].CATEGORY_COURSE_SELECTED;
    } else {
        attribute1 = document.forms[0].CATEGORY_COURSE_SELECTED;
        attribute2 = document.forms[0].CATEGORY_COURSE_LIST;
    }
    //オプションの移動
    moveOption(attribute1, attribute2, allFlg);
    return;
}

//科目のオプション移動
function moveSubclass(side, allFlg) {

    var attribute1 = [];    // 移動元
    var attribute2 = [];    // 移動先

    if (side == "left") {  
        attribute1 = document.forms[0].CATEGORY_SUBCLASS_LIST;
        attribute2 = document.forms[0].CATEGORY_SUBCLASS_SELECTED;
    } else {
        attribute1 = document.forms[0].CATEGORY_SUBCLASS_SELECTED;
        attribute2 = document.forms[0].CATEGORY_SUBCLASS_LIST;
    }
    //オプションの移動
    moveOption(attribute1, attribute2, allFlg);
    return;
}

//オプション移動
// attribute1:移動元 / attribute2:移動先
function moveOption(attribute1, attribute2, allFlg) {

    var optionList = [];    // ソート用

    // 移動先のオプションを全取得
    for (var i = 0; i < attribute2.options.length; i++) {
        const option = attribute2.options[i];
        optionList.push(option);
    }
    // 選択されたオプションを移動
    for (let i = 0; i < attribute1.options.length; i++) {
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

