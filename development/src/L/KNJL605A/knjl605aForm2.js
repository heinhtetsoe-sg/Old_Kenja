function btn_submit(cmd) {
    if (cmd == "delete") {
        if (!confirm('{rval MSG103}')) {
            return false;
        }
    }

    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }

    if (cmd == 'add' || cmd == 'update') {
        document.forms[0].SELECT_COURSE = '';
        var course = [];
        var options = document.forms[0].SELECT_EXAMCOURSECD.options;
        for (let i = 0; i < options.length; i++) {
            const option = options[i];
            course.push(option.value);
        }
        document.forms[0].SELECT_COURSE.value = course.join(',')
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//権限チェック
function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}

//クラス選択／取消（一部）
function move(side, allFlg) {

    var attribute1 = [];    // 移動元
    var attribute2 = [];    // 移動先
    var optionList = [];    // ソート用

    if (side == "left") {  
        attribute1 = document.forms[0].EXAMCOURSECD;
        attribute2 = document.forms[0].SELECT_EXAMCOURSECD;
    } else {
        attribute1 = document.forms[0].SELECT_EXAMCOURSECD;
        attribute2 = document.forms[0].EXAMCOURSECD;  
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
