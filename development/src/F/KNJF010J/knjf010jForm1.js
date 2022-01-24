function btn_submit(cmd) {
    if (document.forms[0].SCHREGNO.value == "") {
        alert("{rval MSG304}");
        return true;
    }
    if (cmd == "delete") {
        if (!confirm("{rval MSG103}")) return false;
    }
    if (cmd == "reset") {
        if (!confirm("{rval MSG106}")) {
            return false;
        } else {
            document.forms[0].cmd.value = cmd;
            document.forms[0].submit();
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//未検査項目設定
function setNotExamined(notFieldSet) {
    var fieldArr = new Array();
    fieldArr = notFieldSet.split(":");
    for (var i = 0; i < fieldArr.length; i++) {
        fieldName = document.forms[0][fieldArr[i]];
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

    return false;
}

// 視力・測定困難チェック時
function disVision(obj, div) {
    if (div == "right") {
        if (document.getElementById("R_BAREVISION")) {
            document.getElementById("R_BAREVISION").disabled = obj.checked;
        }
        if (document.getElementById("R_VISION")) {
            document.getElementById("R_VISION").disabled = obj.checked;
        }
        if (document.forms[0].R_BAREVISION_MARK) {
            document.forms[0].R_BAREVISION_MARK.disabled = obj.checked;
        }
        if (document.forms[0].R_VISION_MARK) {
            document.forms[0].R_VISION_MARK.disabled = obj.checked;
        }
        if (obj.checked == true) {
            document.forms[0].R_VISION_MARK_CANTMEASURE.checked = true;
            document.forms[0].R_VISION_CANTMEASURE.checked = true;
        } else {
            document.forms[0].R_VISION_MARK_CANTMEASURE.checked = false;
            document.forms[0].R_VISION_CANTMEASURE.checked = false;
        }
    } else {
        if (document.getElementById("L_BAREVISION")) {
            document.getElementById("L_BAREVISION").disabled = obj.checked;
        }
        if (document.getElementById("L_VISION")) {
            document.getElementById("L_VISION").disabled = obj.checked;
        }
        if (document.forms[0].L_BAREVISION_MARK) {
            document.forms[0].L_BAREVISION_MARK.disabled = obj.checked;
        }
        if (document.forms[0].L_VISION_MARK) {
            document.forms[0].L_VISION_MARK.disabled = obj.checked;
        }
        if (obj.checked == true) {
            document.forms[0].L_VISION_MARK_CANTMEASURE.checked = true;
            document.forms[0].L_VISION_CANTMEASURE.checked = true;
        } else {
            document.forms[0].L_VISION_MARK_CANTMEASURE.checked = false;
            document.forms[0].L_VISION_CANTMEASURE.checked = false;
        }
    }
}

// 聴力・測定困難チェック時
function disEar(obj, div) {
    if (div == "right") {
        if (document.forms[0].R_EAR_DB) {
            document.forms[0].R_EAR_DB.disabled = obj.checked;
        }
        if (document.forms[0].R_EAR) {
            document.forms[0].R_EAR.disabled = obj.checked;
        }
        if (document.forms[0].R_EAR_DB_1000) {
            document.forms[0].R_EAR_DB_1000.disabled = obj.checked;
        }
        if (document.forms[0].R_EAR_DB_4000) {
            document.forms[0].R_EAR_DB_4000.disabled = obj.checked;
        }
        if (document.forms[0].R_EAR_DB_IN) {
            document.forms[0].R_EAR_DB_IN.disabled = obj.checked;
        }
        if (document.forms[0].R_EAR_IN) {
            document.forms[0].R_EAR_IN.disabled = obj.checked;
        }
        if (document.forms[0].R_EAR_DB_4000_IN) {
            document.forms[0].R_EAR_DB_4000_IN.disabled = obj.checked;
        }
    } else {
        if (document.forms[0].L_EAR_DB) {
            document.forms[0].L_EAR_DB.disabled = obj.checked;
        }
        if (document.forms[0].L_EAR) {
            document.forms[0].L_EAR.disabled = obj.checked;
        }
        if (document.forms[0].L_EAR_DB_1000) {
            document.forms[0].L_EAR_DB_1000.disabled = obj.checked;
        }
        if (document.forms[0].L_EAR_DB_4000) {
            document.forms[0].L_EAR_DB_4000.disabled = obj.checked;
        }
        if (document.forms[0].L_EAR_DB_IN) {
            document.forms[0].L_EAR_DB_IN.disabled = obj.checked;
        }
        if (document.forms[0].L_EAR_IN) {
            document.forms[0].L_EAR_IN.disabled = obj.checked;
        }
        if (document.forms[0].L_EAR_DB_4000_IN) {
            document.forms[0].L_EAR_DB_4000_IN.disabled = obj.checked;
        }
    }
}

//コンボボックスの値でテキストボックスをdisabledにする
function syokenNyuryoku(obj, target_obj, disaleCode) {
    if (isTextBoxDisabled(disaleCode, obj.value) == true) {
        target_obj.disabled = "true";
    } else {
        target_obj.disabled = "";
    }
}

//コンボボックスの値で複数のテキストボックスをdisabledにする
function syokenNyuryoku2(
    obj,
    target_obj,
    disaleCode,
    text_obj,
    disaleCode2,
    combo_obj
) {
    var arrTarget_objs = target_obj.split(",");
    var arrText_objs = text_obj.split(",");
    var arrCombo_objs = combo_obj.split(",");
    var targetLen = arrTarget_objs.length;
    var textLen = arrText_objs.length;
    if (isTextBoxDisabled(disaleCode, obj.value) == true) {
        for (var i = 0; i < targetLen; i++) {
            document.forms[0][arrTarget_objs[i]].disabled = "true";
        }
    } else {
        for (var i = 0; i < targetLen; i++) {
            document.forms[0][arrTarget_objs[i]].disabled = "";
        }
        for (var i = 0; i < textLen; i++) {
            if (
                isTextBoxDisabled(
                    disaleCode2,
                    document.forms[0][arrCombo_objs[i]].value
                ) == true
            ) {
                document.forms[0][arrText_objs[i]].disabled = "true";
            } else {
                document.forms[0][arrText_objs[i]].disabled = "";
            }
        }
    }
}

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

function disableChange(obj, flgObjName, target_obj) {
    var flg_obj = document.forms[0][flgObjName + obj.value];
    if (flg_obj === undefined) {
        target_obj.disabled = true;
        return;
    }

    if (flg_obj.value == "") {
        target_obj.disabled = true;
        if (target_obj.value) {
            alert("テキストデータは更新時に削除されます");
        }
    } else {
        target_obj.disabled = false;
    }
}

function Page_jumper(link) {
    if (document.forms[0].SCHREGNO.value == "") {
        alert("{rval MSG304}");
        return;
    }
    if (!confirm("{rval MSG108}")) {
        return;
    }
    parent.location.href = link;
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

    if (name == "HEIGHT" || name == "WEIGHT") {
        //標準体重
        var keisu_a = document.forms[0].STD_WEIGHT_KEISU_A.value;
        var keisu_b = document.forms[0].STD_WEIGHT_KEISU_B.value;

        if (name == "HEIGHT") {
            var data = document.forms[0].HEIGHT_LIST.value.split(",");
            var std_w = "";
            if (obj.value) {
                std_w =
                    (keisu_a * 10000 * parseFloat(obj.value) -
                        keisu_b * 10000) /
                    10000;
            }
            var elem1 = document.getElementById("std_weight");
            elem1.innerHTML = "標準体重：" + std_w.toFixed(1) + "kg";
        } else {
            var data = document.forms[0].WEIGHT_LIST.value.split(",");
        }
        if (obj.value) data.push(obj.value);

        //身長、体重がセットされている時、肥満度計算する
        if (
            document.forms[0].HEIGHT.value != "" &&
            document.forms[0].WEIGHT.value != ""
        ) {
            //標準体重
            var stdWeght = keisu_a * document.forms[0].HEIGHT.value - keisu_b;
            if (stdWeght > 0 && document.getElementById("OBESITY_INDEX")) {
                //肥満度 = (実測体重 ― 身長別標準体重) ／ 身長別標準体重 × 100（％）
                var oIdx =
                    ((Math.round(document.forms[0].WEIGHT.value * 10) / 10 -
                        stdWeght) /
                        stdWeght) *
                    100;
                var oIdx2 = oIdx.toFixed(1);

                document.getElementById("OBESITY_INDEX").innerHTML =
                    oIdx2 + "%";
            }

            //栄養状態セット
            if (oIdx2) {
                var nutArr = document.forms[0].nutrInfo.value.split(",");
                for (var i = 0; i < nutArr.length; i++) {
                    var nutNameCd2 = nutArr[i].split(":")[0];
                    var nutFrom = nutArr[i].split(":")[1];
                    var nutTo = nutArr[i].split(":")[2];
                    if (
                        (nutFrom == "" && parseInt(oIdx2) <= parseInt(nutTo)) ||
                        (parseInt(nutFrom) <= parseInt(oIdx2) &&
                            parseInt(oIdx2) < parseInt(nutTo)) ||
                        (parseInt(nutFrom) <= parseInt(oIdx2) && nutTo == "")
                    ) {
                        document.forms[0].NUTRITIONCD.value = nutNameCd2;
                    }
                }
            }
        }
    } else if (name == "R_EAR_DB" || name == "L_EAR_DB") {
        //.で分割　分割した個数が１のとき何もしない
        var arrEarDb = obj.value.split(".");
        if (arrEarDb.length == 1 || arrEarDb.length == 2) {
            if (arrEarDb.length == 2) {
                //2個の時　一個目の桁数が空
                //分割された2個目の桁数が1以外のときエラー
                if (arrEarDb[0].length == 0 || arrEarDb[1].length != 1) {
                    alert("{rval MSG901}\n数値を入力してください。");
                    obj.value = "";
                    obj.focus();
                    return false;
                }
            }
        } else {
            alert("{rval MSG901}\n数値を入力してください。");
            obj.value = "";
            obj.focus();
            return false;
        }
    }
}

//更新後次の生徒のリンクをクリックする
function updateNextStudent(schregno, order) {
    if (document.forms[0].SCHREGNO.value == "") {
        alert("{rval MSG304}");
        return true;
    }
    nextURL = "";
    for (var i = 0; i < parent.left_frame.document.links.length; i++) {
        var search = parent.left_frame.document.links[i].search;
        //searchの中身を&で分割し配列にする。
        arr = search.split("&");

        //学籍番号が一致
        if (arr[1] == "SCHREGNO=" + schregno) {
            //昇順
            if (
                order == 0 &&
                i == parent.left_frame.document.links.length - 1
            ) {
                idx = 0; //更新後次の生徒へ(データが最後の生徒の時、最初の生徒へ)
            } else if (order == 0) {
                idx = i + 1; //更新後次の生徒へ
            } else if (order == 1 && i == 0) {
                idx = parent.left_frame.document.links.length - 1; //更新後前の生徒へ(データが最初の生徒の時)
            } else if (order == 1) {
                idx = i - 1; //更新後前の生徒へ
            }
            nextURL = parent.left_frame.document.links[idx].href; //上記の結果
            break;
        }
    }
    document.forms[0].cmd.value = "update";
    //クッキー書き込み
    saveCookie("nextURL", nextURL);
    document.forms[0].submit();
    return false;
}

function NextStudent(cd) {
    var nextURL;
    nextURL = loadCookie("nextURL");
    if (nextURL) {
        if (cd == "0") {
            //クッキー削除
            deleteCookie("nextURL");
            document.location.replace(nextURL);
            alert("{rval MSG201}");
        } else if (cd == "1") {
            //クッキー削除
            deleteCookie("nextURL");
        }
    }
}

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

// Enterキーが押されたときに「TABキーが押された」イベントにするメソッド
function keyChangeEntToTab(obj) {
    if (window.event.keyCode == "13") {
        var setArr = [];
        var z = 0;
        for (var i = 0; i < document.forms[0].elements.length; i++) {
            var e = document.forms[0].elements[i];
            //コンボボックスとテキストボックスが対象
            if (
                (e.type.match(/text|select/) && e.type != "textarea") ||
                (e.type == "button" && e.name.match(/^btn_up/))
            ) {
                setArr[z] = e.name;
                z++;
            }
        }

        var index = setArr.indexOf(obj.name);
        if (window.event.shiftKey) {
            if (index > 0) {
                index--;
            }
            var setFormName = setArr[index];
            if (document.forms[0][setFormName].disabled == true) {
                for (var i = index; i > 0; i--) {
                    setFormName = setArr[i];
                    if (document.forms[0][setFormName].disabled == false) break;
                }
            }
        } else {
            if (index < setArr.length - 1) {
                index++;
            }
            var setFormName = setArr[index];
            if (document.forms[0][setFormName].disabled == true) {
                for (var i = index; i < setArr.length - 1; i++) {
                    setFormName = setArr[i];
                    if (document.forms[0][setFormName].disabled == false) break;
                }
            }
        }

        document.forms[0][setFormName].focus();
        return;
    } else if (obj.type == "button" && window.event.keyCode == "32") {
        var schno = document.forms[0].SCHREGNO.value;
        if (obj.name == "btn_update") {
            //更新
            btn_submit("update");
        } else if (obj.name == "btn_up_pre") {
            //更新後前の生徒へ
            updateNextStudent(schno, 1);
        } else if (obj.name == "btn_up_next") {
            //更新後次の生徒へ
            updateNextStudent(schno, 0);
        }
    }
}

function changeReadOnly() {
    var length = document.forms[0].R_BAREVISION_MARK.length;

    //数字をreadOnlyへ
    if (document.forms[0].NYURYOKU_HOUHO1.checked) {
        document.forms[0].R_BAREVISION.readOnly = true;
        document.forms[0].L_BAREVISION.readOnly = true;
        document.forms[0].R_VISION.readOnly = true;
        document.forms[0].L_VISION.readOnly = true;

        //視力数字の背景をグレーへ
        document.forms[0].R_BAREVISION.style.backgroundColor = "darkgray";
        document.forms[0].L_BAREVISION.style.backgroundColor = "darkgray";
        document.forms[0].R_VISION.style.backgroundColor = "darkgray";
        document.forms[0].L_VISION.style.backgroundColor = "darkgray";

        //視力記号の背景を白へ
        document.forms[0].R_BAREVISION_MARK.style.backgroundColor = "#ffffff";
        document.forms[0].L_BAREVISION_MARK.style.backgroundColor = "#ffffff";
        document.forms[0].R_VISION_MARK.style.backgroundColor = "#ffffff";
        document.forms[0].L_VISION_MARK.style.backgroundColor = "#ffffff";
        for (var i = 0; i < length; i++) {
            document.forms[0].R_BAREVISION_MARK[i].disabled = false;
            document.forms[0].L_BAREVISION_MARK[i].disabled = false;
            document.forms[0].R_VISION_MARK[i].disabled = false;
            document.forms[0].L_VISION_MARK[i].disabled = false;
            document.forms[0].R_BAREVISION_MARK[i].style.backgroundColor =
                "#ffffff";
            document.forms[0].L_BAREVISION_MARK[i].style.backgroundColor =
                "#ffffff";
            document.forms[0].R_VISION_MARK[i].style.backgroundColor =
                "#ffffff";
            document.forms[0].L_VISION_MARK[i].style.backgroundColor =
                "#ffffff";
        }
    } else {
        //readOnlyを削除
        document.forms[0].R_BAREVISION.readOnly = false;
        document.forms[0].L_BAREVISION.readOnly = false;
        document.forms[0].R_VISION.readOnly = false;
        document.forms[0].L_VISION.readOnly = false;

        //視力数字の背景を白へ
        document.forms[0].R_BAREVISION.style.backgroundColor = "#ffffff";
        document.forms[0].L_BAREVISION.style.backgroundColor = "#ffffff";
        document.forms[0].R_VISION.style.backgroundColor = "#ffffff";
        document.forms[0].L_VISION.style.backgroundColor = "#ffffff";

        //視力記号の背景をグレーへ
        document.forms[0].R_BAREVISION_MARK.style.backgroundColor = "darkgray";
        document.forms[0].L_BAREVISION_MARK.style.backgroundColor = "darkgray";
        document.forms[0].R_VISION_MARK.style.backgroundColor = "darkgray";
        document.forms[0].L_VISION_MARK.style.backgroundColor = "darkgray";

        //文字の選択されているもの以外をdisabledへ
        for (var i = 0; i < length; i++) {
            if (document.forms[0].R_BAREVISION_MARK[i].selected != true) {
                document.forms[0].R_BAREVISION_MARK[i].disabled = true;
                document.forms[0].R_BAREVISION_MARK[i].style.backgroundColor =
                    "darkgray";
            }
            if (document.forms[0].L_BAREVISION_MARK[i].selected != true) {
                document.forms[0].L_BAREVISION_MARK[i].disabled = true;
                document.forms[0].L_BAREVISION_MARK[i].style.backgroundColor =
                    "darkgray";
            }
            if (document.forms[0].R_VISION_MARK[i].selected != true) {
                document.forms[0].R_VISION_MARK[i].disabled = true;
                document.forms[0].R_VISION_MARK[i].style.backgroundColor =
                    "darkgray";
            }
            if (document.forms[0].L_VISION_MARK[i].selected != true) {
                document.forms[0].L_VISION_MARK[i].disabled = true;
                document.forms[0].L_VISION_MARK[i].style.backgroundColor =
                    "darkgray";
            }
        }
    }
}
