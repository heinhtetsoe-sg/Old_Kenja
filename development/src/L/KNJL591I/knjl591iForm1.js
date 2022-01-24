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
            alert('{rval MSG301}\n( 開始学籍番号 )');
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
        var koteino = document.forms[0].KOTEINO.value;
        var checkedDatas = new Array();
        var dataCnt = 0;
        var tyouhukuFlag = false;
        for (var i = 0; i < document.forms[0].elements.length; i++) {
            if (document.forms[0].elements[i].name.indexOf('STUDENTNO_') !== -1) {
                checkedDatas[dataCnt] = document.forms[0].elements[i];
                dataCnt++;
            }
        }

        for (var i = 0; i < checkedDatas.length; i++) {
            var checkedData = checkedDatas[i].name.split("_");
            var examno1 = checkedData[1];
            var studentno1 = "";
            if (checkedDatas[i].value != "") {
                studentno1 = checkedDatas[i].value;
            }
            if (checkedDatas[i].value != "") {
                console.log(checkedDatas[i].value.length != 8);
                if (checkedDatas[i].value.length != 8) {
                    alert("{rval MSG901}\n受験番号:" + examno1 + 'の学籍番号が8桁以外です。');
                    checkedDatas[i].focus();
                    return false;
                } else if (checkedDatas[i].value.substring(0, 5) != koteino) {
                    console.log("haitta2");
                    alert("{rval MSG901}\n受験番号:" + examno1 + 'の学籍番号の形式が間違っています。');
                    checkedDatas[i].focus();
                    return false;
                }
            }
            for (var j = 0; j < checkedDatas.length; j++) {
                var studentno2 = "";
                if (checkedDatas[j].value != "") {
                    studentno2 = checkedDatas[j].value;
                }

                if (studentno1 != "" && studentno2 != "" && checkedDatas[j] != checkedDatas[i]
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