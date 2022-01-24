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

function syokenNyuryoku(obj, target_obj) {
    if (obj.value == "") {
        var select_no = 0;
    } else {
        var select_no = parseInt(obj.value);
    }

    if (select_no < 2) {
        if (target_obj.value) {
            alert("テキストデータは更新時に削除されます");
        }
        target_obj.disabled = true;
    } else {
        target_obj.disabled = false;
    }
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

    if (name == "URICPH1" || name == "URICPH2") {
        if (checkString == ".") {
            alert("{rval MSG901}\n数値を入力してください。");
            obj.value = "";
            obj.focus();
            return false;
        }
    }

    if (name == "HEIGHT" || name == "WEIGHT") {
        //標準体重
        var keisu_a = document.forms[0].STD_WEIGHT_KEISU_A.value;
        var keisu_b = document.forms[0].STD_WEIGHT_KEISU_B.value;

        if (name == "HEIGHT") {
            var data = document.forms[0].HEIGHT_LIST.value.split(",");
            var std_w = "";
            if (obj.value) {
                std_w = (keisu_a * 10000 * parseFloat(obj.value) - keisu_b * 10000) / 10000;
            }
            var elem1 = document.getElementById("std_weight");
            elem1.innerHTML = std_w.toFixed(3);
        } else {
            var data = document.forms[0].WEIGHT_LIST.value.split(",");
        }
        if (obj.value) data.push(obj.value);

        //身長、体重がセットされている時、肥満度計算する
        if (document.forms[0].HEIGHT.value != "" && document.forms[0].WEIGHT.value != "") {
            //肥満度 = (実測体重 ― 身長別標準体重) ／ 身長別標準体重 × 100（％）
            var stdWeght = keisu_a * document.forms[0].HEIGHT.value - keisu_b;
            if (stdWeght > 0 && document.getElementById("OBESITY_INDEX")) {
                var oIdx = ((document.forms[0].WEIGHT.value - stdWeght) / stdWeght) * 100;
                var oIdx2 = oIdx.toFixed(1);
                document.getElementById("OBESITY_INDEX").innerHTML = oIdx2 + "%";
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
                        (parseInt(nutFrom) <= parseInt(oIdx2) && parseInt(oIdx2) < parseInt(nutTo)) ||
                        (parseInt(nutFrom) <= parseInt(oIdx2) && nutTo == "")
                    ) {
                        document.forms[0].NUTRITIONCD.value = nutNameCd2;
                    }
                }
            }
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
            if (order == 0 && i == parent.left_frame.document.links.length - 1) {
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
            if ((e.type.match(/text|select/) && e.type != "textarea") || (e.type == "button" && e.name.match(/^btn_up/))) {
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
window.onload = function () {
    for (var i = 0; i < document.forms[0].elements.length; i++) {
        var tagName = document.forms[0].elements[i].tagName;
        var tagType = document.forms[0].elements[i].type;
        var name = document.forms[0].elements[i].name;
        if ((tagName == "INPUT" && name != "btn_back") || tagName == "SELECT" || tagName == "TEXTAREA") {
            document.forms[0].elements[i].disabled = true;
            document.forms[0].elements[i].style.backgroundColor = "#FFFFFF";
        }
    }
};
