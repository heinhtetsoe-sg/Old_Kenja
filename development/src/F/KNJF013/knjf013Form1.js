function btn_submit(cmd) {
    bodyWidth = window.innerWidth || document.body.clientWidth || 0;
    bodyHeight = window.innerHeight || document.body.clientHeight || 0;

    document.forms[0].windowWidth.value = bodyWidth;
    document.forms[0].windowHeight.value = bodyHeight;

    //データ指定チェック
    if (cmd == "update") {
        var dataCnt = document.forms[0].DATA_CNT.value;
        if (dataCnt == 0) {
            alert("{rval MSG304}");
            return false;
        }
        //更新中、サブミットする項目使用不可
        document.forms[0].H_HR_CLASS.value = document.forms[0].HR_CLASS.value;
        document.forms[0].H_INPUT_FORM.value = document.forms[0].INPUT_FORM.value;
        document.forms[0].HR_CLASS.disabled = true;
        document.forms[0].INPUT_FORM.disabled = true;
        document.forms[0].btn_reset.disabled = true;
    }
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
//画面リサイズ
function submit_reSize() {
    bodyWidth = window.innerWidth || document.body.clientWidth || 0;
    bodyHeight = window.innerHeight || document.body.clientHeight || 0;

    document.getElementById("table1").style.width = bodyWidth - 36;
    document.getElementById("trow").style.width = bodyWidth - 237;
    document.getElementById("tbody").style.width = bodyWidth - 220;
    document.getElementById("tbody").style.height = bodyHeight - 210;
    document.getElementById("tcol").style.height = bodyHeight - 227;
}
//未検査項目設定
function setNotExamined(notFieldSet, counter) {
    var fieldArr = new Array();
    fieldArr = notFieldSet.split(":");
    var numb = parseInt(counter, 10);

    for (var i = 0; i < fieldArr.length; i++) {
        for (var c = 0; c <= numb; c++) {
            fieldName = document.forms[0][fieldArr[i] + "-" + c];
            if (fieldName) {
                fieldName.style.backgroundColor = "#cccccc";
                if (fieldName.type == "select-one") {
                    for (var j = 0; j < fieldName.length; j++) {
                        if (fieldName.options[j].selected) {
                        } else {
                            fieldName.options[j].disabled = true;
                        }
                    }
                } else if (fieldName.type == "checkbox") {
                    fieldName.checked = false;
                    fieldName.disabled = true;
                } else {
                    fieldName.readOnly = true;
                }
            }
        }
    }

    return false;
}
// 視力・測定困難チェック時
function disVision(obj, div, counter) {
    if (div == "right") {
        if (document.getElementById("R_BAREVISION-" + counter)) {
            document.getElementById("R_BAREVISION-" + counter).disabled = obj.checked;
        }
        if (document.getElementById("R_VISION-" + counter)) {
            document.getElementById("R_VISION-" + counter).disabled = obj.checked;
        }
        if (document.forms[0]["R_BAREVISION_MARK-" + counter]) {
            document.forms[0]["R_BAREVISION_MARK-" + counter].disabled = obj.checked;
        }
        if (document.forms[0]["R_VISION_MARK-" + counter]) {
            document.forms[0]["R_VISION_MARK-" + counter].disabled = obj.checked;
        }
    } else {
        if (document.getElementById("L_BAREVISION-" + counter)) {
            document.getElementById("L_BAREVISION-" + counter).disabled = obj.checked;
        }
        if (document.getElementById("L_VISION-" + counter)) {
            document.getElementById("L_VISION-" + counter).disabled = obj.checked;
        }
        if (document.forms[0]["L_BAREVISION_MARK-" + counter]) {
            document.forms[0]["L_BAREVISION_MARK-" + counter].disabled = obj.checked;
        }
        if (document.forms[0]["L_VISION_MARK-" + counter]) {
            document.forms[0]["L_VISION_MARK-" + counter].disabled = obj.checked;
        }
    }
}
// 聴力・測定困難チェック時
function disEar(obj, div, counter) {
    if (div == "right") {
        if (document.forms[0]["R_EAR_DB-" + counter]) {
            document.forms[0]["R_EAR_DB-" + counter].disabled = obj.checked;
        }
        if (document.forms[0]["R_EAR-" + counter]) {
            document.forms[0]["R_EAR-" + counter].disabled = obj.checked;
        }
        if (document.forms[0]["R_EAR_DB_1000-" + counter]) {
            document.forms[0]["R_EAR_DB_1000-" + counter].disabled = obj.checked;
        }
        if (document.forms[0]["R_EAR_DB_4000-" + counter]) {
            document.forms[0]["R_EAR_DB_4000-" + counter].disabled = obj.checked;
        }
        if (document.forms[0]["R_EAR_DB_IN-" + counter]) {
            document.forms[0]["R_EAR_DB_IN-" + counter].disabled = obj.checked;
        }
        if (document.forms[0]["R_EAR_IN-" + counter]) {
            document.forms[0]["R_EAR_IN-" + counter].disabled = obj.checked;
        }
        if (document.forms[0]["R_EAR_DB_4000_IN-" + counter]) {
            document.forms[0]["R_EAR_DB_4000_IN-" + counter].disabled = obj.checked;
        }
    } else {
        if (document.forms[0]["L_EAR_DB-" + counter]) {
            document.forms[0]["L_EAR_DB-" + counter].disabled = obj.checked;
        }
        if (document.forms[0]["L_EAR-" + counter]) {
            document.forms[0]["L_EAR-" + counter].disabled = obj.checked;
        }
        if (document.forms[0]["L_EAR_DB_1000-" + counter]) {
            document.forms[0]["L_EAR_DB_1000-" + counter].disabled = obj.checked;
        }
        if (document.forms[0]["L_EAR_DB_4000-" + counter]) {
            document.forms[0]["L_EAR_DB_4000-" + counter].disabled = obj.checked;
        }
        if (document.forms[0]["L_EAR_DB_IN-" + counter]) {
            document.forms[0]["L_EAR_DB_IN-" + counter].disabled = obj.checked;
        }
        if (document.forms[0]["L_EAR_IN-" + counter]) {
            document.forms[0]["L_EAR_IN-" + counter].disabled = obj.checked;
        }
        if (document.forms[0]["L_EAR_DB_4000_IN-" + counter]) {
            document.forms[0]["L_EAR_DB_4000_IN-" + counter].disabled = obj.checked;
        }
    }
}
//数値かどうかをチェック
function Num_Check(obj) {
    var name = obj.name;
    var checkString = obj.value;
    var newString = "";
    var count = 0;

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

    if (name == "URICPH1" || name == "URICPH2") {
        if (checkString == ".") {
            alert("{rval MSG901}\n数値を入力してください。");
            obj.value = "";
            obj.focus();
            return false;
        }
    }

    if (name.match(/^HEIGHT/) || name.match(/^WEIGHT/)) {
        var nm = name.toLowerCase().split("-");
        var data_cnt = document.forms[0].DATA_CNT.value;

        var counter = name.split("-");
        var sex = document.forms[0]["SEX-" + counter[1]].value;
        var age = document.forms[0]["AGE-" + counter[1]].value;

        var keisu_a = document.forms[0]["KEISU_A_" + sex + "_" + age].value;
        var keisu_b = document.forms[0]["KEISU_B_" + sex + "_" + age].value;

        //標準体重
        if (name.match(/^HEIGHT/)) {
            var std_w = "";
            if (obj.value) {
                std_w = (keisu_a * 10000 * parseFloat(obj.value) - keisu_b * 10000) / 10000;
            }
            var elem = document.getElementById("std_weight-" + counter[1]);
            elem.innerHTML = std_w;
        }

        //身長、体重がセットされている時、肥満度計算する
        var height = document.forms[0]["HEIGHT-" + counter[1]];
        var weight = document.forms[0]["WEIGHT-" + counter[1]];
        if (height.value != "" && weight.value != "") {
            //肥満度 = (実測体重 ― 身長別標準体重) ／ 身長別標準体重 × 100（％）
            var stdWeght = keisu_a * height.value - keisu_b;
            if (stdWeght > 0 && document.getElementById("OBESITY_INDEX-" + counter[1])) {
                var oIdx = ((weight.value - stdWeght) / stdWeght) * 100;
                var oIdx1 = oIdx * 10;
                var oIdx2 = Math.round(oIdx1) / 10;
                document.getElementById("OBESITY_INDEX-" + counter[1]).innerHTML = oIdx2 + "%";
            }
        }
    }
}
//視力(文字)
function Mark_Check(obj) {
    var mark = obj.value;
    var printKenkouSindanIppan = document.forms[0].printKenkouSindanIppan.value;
    var msg = "A～Dを入力して下さい。";
    if (printKenkouSindanIppan == "2") {
        msg = "A～D,未を入力して下さい。";
    }
    switch (mark) {
        case "a":
        case "A":
        case "ａ":
        case "Ａ":
            obj.value = "A";
            break;
        case "b":
        case "B":
        case "ｂ":
        case "Ｂ":
            obj.value = "B";
            break;
        case "c":
        case "C":
        case "ｃ":
        case "Ｃ":
            obj.value = "C";
            break;
        case "d":
        case "D":
        case "ｄ":
        case "Ｄ":
            obj.value = "D";
            break;
        case "":
            obj.value = "";
            break;
        case "未":
            if (printKenkouSindanIppan != "2") {
                alert(msg);
                obj.value = "";
            }
            break;
        default:
            alert(msg);
            obj.value = "";
            break;
    }
}
//所見
function syokenNyuryoku(obj, target_name, counter) {
    if (obj.value == "") {
        var select_no = 0;
    } else {
        var select_no = parseInt(obj.value);
    }

    target_obj = document.forms[0][target_name + "-" + counter];

    if (select_no < 2) {
        if (target_obj.value) {
            alert("テキストデータは更新時に削除されます");
        }
        target_obj.disabled = true;
    } else {
        target_obj.disabled = false;
    }
}
//権限チェック
function OnAuthError() {
    alert("{rval MSG300}");
    closeWin();
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
