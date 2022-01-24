function btn_submit(cmd) {
    //データ指定チェック
    if (cmd == "update") {
        var dataCnt = document.forms[0].DATA_CNT.value;
        if (dataCnt == 0) {
            alert("{rval MSG304}");
            return false;
        }
        //更新中、サブミットする項目使用不可
        document.forms[0].H_GRADE_HR_CLASS.value = document.forms[0].GRADE_HR_CLASS.value;
        document.forms[0].GRADE_HR_CLASS.disabled = true;
        document.forms[0].btn_reset.disabled = true;
    }

    //取消確認
    if (cmd == "reset") {
        if (!confirm("{rval MSG106}")) {
            return false;
        }
    }

    //サブミット中、更新ボタン使用不可
    document.forms[0].btn_update.disabled = true;

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//文理区分変更時、選択科目の内容を変更する
function changeSubclassBunri() {
    //現在のデータを保持する
    for (var count = 0; count < document.forms[0].DATA_CNT.value; count++) {
        setHiddenData("BUNRIDIV", count, 1);
        setHiddenData("SUBCLASSCD", count, 1);
        setHiddenData("DECLINE_FLG", count, 2);
    }

    //リロード
    document.forms[0].cmd.value = "bunri";
    document.forms[0].submit();
    return false;
}

//hiddenに画面上のデータをセットする
function setHiddenData(name, count, type) {
    var element = document.getElementsByName(name + "-" + count);
    var val = "";
    if (type == 1) {
        val = element[0].value;
    } else {
        val = element[0].checked ? 1 : 0;
    }
    element = document.getElementsByName("H_" + name + "-" + count);
    element[0].value = val;
}

function Page_jumper(link) {
    if (!confirm("{rval MSG108}")) {
        return;
    }
    parent.location.href = link;
}

//エンターでのフォーカス移動
(function () {
    currentFNo = 0;
    function getFocusElementIndex() {
        for (var i = 0; i < document.forms[0].elements.length; i++) {
            if (document.forms[0].elements[i] == document.activeElement) {
                return i;
            }
        }
    }

    function nextForm() {
        if (event.keyCode == 13) {
            currentFNo = getFocusElementIndex() + 1;
            var isLoop = false;
            if (document.forms[0].elements[currentFNo]) {
                while (document.forms[0].elements[currentFNo].type == "button" || document.forms[0].elements[currentFNo].type == "hidden" || document.forms[0].elements[currentFNo].disabled) {
                    currentFNo++;
                    currentFNo %= document.forms[0].elements.length;

                    //無限ループ対策
                    if (currentFNo == 0) {
                        if (isLoop) {
                            break;
                        } else {
                            isLoop = true;
                        }
                    }
                }
                currentFNo %= document.forms[0].elements.length;
                document.forms[0].elements[currentFNo].focus();
            }
        }
    }
    window.document.onkeydown = nextForm;
})();
