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
        document.forms[0].H_SCREEN.value = document.forms[0].SCREEN.value;
        document.forms[0].GRADE_HR_CLASS.disabled = true;
        document.forms[0].SCREEN.disabled = true;
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
//所見
function syokenNyuryoku(obj, target_name, nameCd2List, counter) {
    target_obj = document.forms[0][target_name + "-" + counter];

    if (obj.value != "" && nameCd2List.indexOf(obj.value) >= 0) {
        target_obj.disabled = false;
    } else {
        if (target_obj.value) {
            alert("テキストデータは更新時に削除されます");
        }
        target_obj.disabled = true;
    }
}

//チェックボックスのラベル表示（有・無）
function checkAri_Nasi(obj, id) {
    var ari_nasi = document.getElementById(id);
    if (obj.checked) {
        ari_nasi.innerHTML = "有";
    } else {
        ari_nasi.innerHTML = "無";
    }
}

//その他疾病及び異常
function OptionUse(obj, name) {
    for (var i = 0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name == name) {
            if (obj.value == "99") {
                document.forms[0].elements[i].disabled = false;
                document.forms[0].elements[i].style.backgroundColor = "#ffffff";
            } else {
                document.forms[0].elements[i].disabled = true;
                document.forms[0].elements[i].style.backgroundColor = "#D3D3D3";
            }
        }
    }
}

//スクロール
function scrollRC() {
    document.getElementById("trow").scrollLeft = document.getElementById("tbody").scrollLeft;
    document.getElementById("tcol").scrollTop = document.getElementById("tbody").scrollTop;
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
        if (document.forms[0].SCREEN.value == 2) {
            return;
        }
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
