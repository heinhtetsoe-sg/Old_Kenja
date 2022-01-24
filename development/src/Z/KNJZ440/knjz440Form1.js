function btn_submit(cmd) {
    if (cmd == 'update') {
        var checkedDatas = new Array();
        var dataCnt = 0;
        var flag = false;
        var oldSeq = null;
        var nowSeq = null;
        var countLength = 0;
        var subclasscd;
        var tyouhukuFlag = false;
        for (var i = 0; i < document.forms[0].elements.length; i++) {
            if (document.forms[0].elements[i].name == "CHECKSUBCLASS[]") {
                var checkedData = document.forms[0].elements[i];
                if (checkedData.checked) {
                    var isyundake = checkedData.value.split(":");
                    nowSeq     = isyundake[0];
                    subclasscd = isyundake[1];

                    if (oldSeq == null || oldSeq != nowSeq) {
                        checkedDatas[nowSeq] = new Array();
                    }
                    countLength = checkedDatas[nowSeq].length;
                    checkedDatas[nowSeq][countLength] = subclasscd;
                    oldSeq = nowSeq;

                    flag = true;
                }
            }
        }
        if (flag) {
            for (var i = 0; i < checkedDatas.length; i++) {
                if (checkedDatas[i]) {     //配列だったら⇒SEQの番号が存在していたら
                    //i行目のk行目を比べて重複チェック
                    for (var k = i+1; k < checkedDatas.length; k++) {
                        if (checkedDatas[k]) {
                            if (checkedDatas[i].length == checkedDatas[k].length) {
                                for (var l = 0; l < checkedDatas[k].length; l++) {
                                    if (checkedDatas[i][l] == checkedDatas[k][l]) {
                                        tyouhukuFlag = true;
                                        continue;
                                    } else {
                                        tyouhukuFlag = false;
                                        break;
                                    }
                                }
                                if (tyouhukuFlag) {
                                    alert(i + '行目と' + k + '行目が重複しています。');
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
        }
        if (!confirm('処理を開始します。よろしいでしょうか？')) {
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();

    return false;
}

function btn_delete(seq) {
    targetHidden = document.getElementById("DELETESEQ" + seq);
//    alert(targetHidden.name);
    var targetSepa = '';
    var checkedDataValueForTarget = '';
    var flag = false;
    for (var i = 0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name == "CHECKSUBCLASS[]") {
            var checkedData = document.forms[0].elements[i];
            var checkedDataValue = checkedData.value;
            var ptn = '^' + seq + ':\\d*';
            if (checkedDataValue.search(ptn) != -1 && checkedData.checked) {
                flag = true;
                checkedDataValueForTarget += targetSepa + checkedDataValue;
                checkedData.checked = false;
                targetSepa = ',';
            }
        }
    }
    if (flag == true) {
        targetHidden.value = checkedDataValueForTarget;
    } else {
        var targetHiddenEachData = new Array();
        targetHiddenEachData = targetHidden.value.split(",");
        for (var i = 0; i < document.forms[0].elements.length; i++) {
            if (document.forms[0].elements[i].name == "CHECKSUBCLASS[]") {
                var checkedData = document.forms[0].elements[i];
                var checkedDataValue = checkedData.value;
                for (var j = 0; j < targetHiddenEachData.length; j++) {
                    if (checkedDataValue == targetHiddenEachData[j]) {
                        checkedData.checked = true;
                    }
                }
            }
        }
    }
}



