function btn_submit(cmd) {
    //データ指定チェック
    if (cmd == "update") {
        var dataCnt = document.forms[0].DATA_CNT.value;
        if (dataCnt == 0) {
            alert("{rval MSG304}");
            return false;
        }
        //更新中、サブミットする項目使用不可
        document.forms[0].H_GRADE_HR_CLASS.value =
            document.forms[0].GRADE_HR_CLASS.value;
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

//チェックボックスのラベル表示（有・無）
function checkAri_Nasi(obj, id) {
    var ari_nasi = document.getElementById(id);
    if (obj.checked) {
        ari_nasi.innerHTML = "有";
    } else {
        ari_nasi.innerHTML = "無";
    }
}

//コンボの変更イベント
function OptionUse(obj, name, disableCodes) {
    //その他疾病及び異常
    target_obj = getTextId(name);
    if (target_obj != "") {
        textDisabledChange(target_obj, disableCodes, obj.value);
    }
}

function OptionUse2(obj, name, disableCodes) {
    //口腔の疾病及び異常
    target_obj = getTextId(name);
    if (target_obj != "") {
        textDisabledChange(target_obj, disableCodes, obj.value);
    }
}

function OptionUse3(disableCodes, counter) {
    var target_obj1 = document.forms[0]["DENTISTREMARKCD-" + counter];
    var target_text1 = document.forms[0]["DENTISTREMARK-" + counter];
    var target_obj2 = document.forms[0]["DENTISTREMARKCD2-" + counter];
    var target_text2 = document.forms[0]["DENTISTREMARK2-" + counter];
    var target_obj3 = document.forms[0]["DENTISTREMARKCD3-" + counter];
    var target_text3 = document.forms[0]["DENTISTREMARK3-" + counter];

    //所見1
    if (target_obj1.value != "" && target_obj1.value != "00") {
        //所見1テキストの有効無効変更
        textDisabledChange(target_text1, disableCodes, target_obj1.value);
        objDisabledChange(target_obj2, false); //所見2のコンボの有効化
        if (target_obj2.value != "" && target_obj2.value != "00") {
            objDisabledChange(target_obj3, false); //所見3のコンボの有効化
            //所見2テキストの有効無効変更
            textDisabledChange(target_text2, disableCodes, target_obj2.value);
            //所見3テキストの有効無効変更
            textDisabledChange(target_text3, disableCodes, target_obj3.value);
        } else {
            objDisabledChange(target_text2, true);
            objDisabledChange(target_obj3, true);
            objDisabledChange(target_text3, true);
        }
    } else {
        objDisabledChange(target_text1, true);
        objDisabledChange(target_obj2, true);
        objDisabledChange(target_text2, true);
        objDisabledChange(target_obj3, true);
        objDisabledChange(target_text3, true);
    }
}

function OptionUse4(disableCodes, counter) {
    var target_obj1 = document.forms[0]["DENTISTTREATCD-" + counter];
    var target_text1 = document.forms[0]["DENTISTTREAT-" + counter];
    var target_obj2 = document.forms[0]["DENTISTTREATCD2-" + counter];
    var target_text2 = document.forms[0]["DENTISTTREAT2-" + counter];
    //事後措置1
    if (target_obj1.value != "" && target_obj1.value != "01") {
        //事後措置1テキストの有効無効変更
        textDisabledChange(target_text1, disableCodes, target_obj1.value);
        objDisabledChange(target_obj2, false); //事後措置2のコンボの有効化
        textDisabledChange(target_text2, disableCodes, target_obj2.value);
    } else {
        objDisabledChange(target_text1, true);
        objDisabledChange(target_obj2, true);
        objDisabledChange(target_text2, true);
    }
}

function objDisabledChange(obj, disabled) {
    obj.disabled = disabled;
    if (disabled) {
        obj.style.backgroundColor = "darkgray";
    } else {
        obj.style.backgroundColor = "#ffffff";
    }
}

//テキストボックスのID取得
function getTextId(name) {
    for (var i = 0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name == name) {
            return document.forms[0].elements[i];
        }
    }
    return "";
}

//所見欄の有効無効変更
function textDisabledChange(target_obj, disableCodes, targetCode) {
    if (isTextBoxDisabled(disableCodes, targetCode) == true) {
        objDisabledChange(target_obj, true);
    } else {
        objDisabledChange(target_obj, false);
    }
}

//所見欄の有効無効チェック
function isTextBoxDisabled(disaleCodes, targetCode) {
    //未選択の時は無効
    if (targetCode == "") {
        return true;
    }
    var arrDisaleCodes = disaleCodes.split(",");
    for (var i = 0; i < arrDisaleCodes.length; i++) {
        if (arrDisaleCodes[i] == targetCode) {
            return true;
        }
    }
    return false;
}

//数値かどうかをチェック
function Num_Check(obj) {
    var name = obj.name;
    var checkString = obj.value;
    var newString = "";

    for (i = 0; i < checkString.length; i++) {
        ch = checkString.substring(i, i + 1);
        if ((ch >= "0" && ch <= "9") || ch == ".") {
            newString += ch;
        }
    }
    if (checkString != newString) {
        alert("{rval MSG901}\n数値を入力してください。");
        obj.value = "";
        obj.focus();
        return false;
    }
}

//スクロール
function scrollRC() {
    document.getElementById("trow").scrollLeft = document.getElementById(
        "tbody"
    ).scrollLeft;
    document.getElementById("tcol").scrollTop = document.getElementById(
        "tbody"
    ).scrollTop;
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
                while (
                    document.forms[0].elements[currentFNo].type == "button" ||
                    document.forms[0].elements[currentFNo].type == "hidden" ||
                    document.forms[0].elements[currentFNo].disabled
                ) {
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
