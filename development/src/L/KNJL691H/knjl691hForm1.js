function btn_submit(cmd) {
    //取消
    if (cmd == 'reset' && !confirm('{rval MSG106}')) return true;

    //終了
    if (cmd == 'end') {
        if (document.forms[0].CHANGE_FLG.value == '1') {
            if (confirm('{rval MSG108}')) {
                closeWin();
            }
            return false;
        }
        closeWin();
    }

    if (cmd == 'huban') {
        if (document.forms[0].START_HUBAN.value == '') {
            alert('{rval MSG301}\n( 先頭受験番号 )');
            document.forms[0].START_HUBAN.focus();
            return false;
        }

        if (document.forms[0].CHANGE_FLG.value == "1") {
            if (!confirm('{rval MSG108}')) {
                return false;
            }
        }
    }

    if (cmd == 'update') {
        var checkedDatas = new Array();
        var dataCnt = 0;
        var tyouhukuFlag = false;
        for (var i = 0; i < document.forms[0].elements.length; i++) {
            if (document.forms[0].elements[i].name.indexOf('STUDENTNO_') !== -1) {
                if (document.forms[0].elements[i].value == "") {
                    var checkedData = document.forms[0].elements[i].name.split("_");
                    var examno1 = checkedData[1];
        
                    alert('{rval MSG301}\n( 学籍番号 ( 受験番号:' + examno1 + ' ) )');
                    document.forms[0].elements[i].focus();
                    return false;
                }

                checkedDatas[dataCnt] = document.forms[0].elements[i];
                dataCnt++;
            }
        }

        for (var i = 0; i < checkedDatas.length; i++) {
            var checkedData = checkedDatas[i].name.split("_");
            var examno1 = checkedData[1];
            var studentno1 = ("00000000" + checkedDatas[i].value).slice(-4) ; // 8桁ゼロ埋め

            for (var j = 0; j < checkedDatas.length; j++) {
                var studentno2 = ("00000000" + checkedDatas[j].value).slice(-4) ; // 8桁ゼロ埋め

                if (checkedDatas[j] != checkedDatas[i]
                    && studentno2 == studentno1) {
                    tyouhukuFlag = true;
                    break;
                } else {
                    tyouhukuFlag = false;
                    continue;
                }
            }

            if (tyouhukuFlag) {
                var checkedData2 = checkedDatas[j].name.split("_");
                var examno2 = checkedData2[1];
                alert("{rval MSG302}\n受験番号:" + examno1 + 'と受験番号:' + examno2 + 'が重複しています。');
                checkedDatas[j].focus();
                return false;
            }
        }

        if (!confirm('{rval MSG102}')) {
            return;
        }
    }

    document.forms[0].btn_huban.disabled = true;
    document.forms[0].btn_update.disabled = true;
    document.forms[0].btn_reset.disabled = true;
    document.forms[0].btn_end.disabled = true;

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function changeValue() {
    document.forms[0].CHANGE_FLG.value = "1";
}