function btn_submit(cmd) {

    //取消確認
    if (cmd == 'clear') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }
    //終了
    if (cmd == 'close') {
        closeWin();
        return false;
    }
    //更新
    if (cmd == 'update') {
        var examNoList = [];
        var entryList = document.forms[0].SELECT_EXAMNO;
        for (let i = 0; i < entryList.length; i++) {
            const option = entryList[i];
            examNoList.push(option.value);
        }
        document.forms[0].ENTRY_EXAMNO.value = examNoList.join();

        var examNoList = [];
        var nonEntryList = document.forms[0].EXAMNO;
        for (let i = 0; i < nonEntryList.length; i++) {
            const option = nonEntryList[i];
            examNoList.push(option.value);
        }
        document.forms[0].NONENTRY_EXAMNO.value = examNoList.join();
    }
    if (cmd == 'csv') {
        if (document.forms[0].CSV_INOUT1.checked) {
            cmd = 'csvInput';
        }
        if (document.forms[0].CSV_INOUT2.checked) {
            cmd = 'csvOutput';
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//クラス選択／取消（一部）
function move(side, allFlg) {

    var attribute1 = [];    // 移動元
    var attribute2 = [];    // 移動先
    var optionList = [];    // ソート用

    if (side == "left") {
        attribute1 = document.forms[0].EXAMNO;
        attribute2 = document.forms[0].SELECT_EXAMNO;
    } else {
        attribute1 = document.forms[0].SELECT_EXAMNO;
        attribute2 = document.forms[0].EXAMNO;
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
    optionList.sort(function (option1, option2) {
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

//権限チェック
function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();
}
