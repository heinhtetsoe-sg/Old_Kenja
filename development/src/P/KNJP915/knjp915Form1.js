//サブミット
function btn_submit(cmd) {
    if (cmd == "update" && !confirm("{rval MSG101}")) {
        return false;
    }

    if (cmd == "update") {
        //更新
        if (document.forms[0].UPDATE_DIV.value == "1") {
            if (document.forms[0].COLLECT_LM_CD.value == "") {
                alert("名称マスタ(P016)に入金項目を設定してください。");
                return;
            } else if (document.forms[0].COLLECT_CD_EXISTS_FLG.value != "1") {
                alert("名称マスタ(P016)に設定された入金項目はマスタに存在しません。");
                return;
            }

            if (document.forms[0].CARRY_LM_CD.value == "") {
                alert("繰越項目を指定してください。");
                return;
            }

            if (document.forms[0].INCOME_LM_CD_SAKI.value == "") {
                alert("預り金項目マスタ(次年度)を設定してください。");
                return;
            }

            if (document.forms[0].INCOME_DATE.value == "") {
                alert("収入日を指定してください。");
                return;
            }
            //生徒未選択エラー
            if (document.forms[0].warihuriDiv.value == "1") {
                if (document.forms[0].category_selected.length == 0) {
                    alert("生徒を選択して下さい。");
                    return;
                }
                student = document.forms[0].selectStudent;
                student.value = "";
                sep = "";
                for (var i = 0; i < document.forms[0].category_selected.length; i++) {
                    student.value = student.value + sep + document.forms[0].category_selected.options[i].value.split("_")[1];
                    sep = ",";
                }
            }
        } else {
            // 削除
            if (document.forms[0].INCOME_LM_CD_CANCEL.value == "") {
                alert("預り金項目を指定してください。");
                return;
            }
        }
    }

    //読み込み中は、実行ボタンはグレーアウト
    document.forms[0].btn_upd.disabled = true;

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();

    return false;
}
// 前年度、年度締めチェック
function closeCheck() {
    alert("{rval MSG300}" + "\n前年度の年度締め処理が行われていません。");
    closeWin();
}
function ClearList(OptionList, TitleName) {
    OptionList.length = 0;
}
function AllClearList(OptionList, TitleName) {
    attribute = document.forms[0].category_name;
    ClearList(attribute, attribute);
    attribute = document.forms[0].category_selected;
    ClearList(attribute, attribute);
}
function move1(side) {
    var tempVal1 = new Array();
    var tempVal2 = new Array();
    var tempText1 = new Array();
    var tempText2 = new Array();
    var tempSort = new Array();
    var current1 = 0;
    var current2 = 0;
    var y = 0;
    var attribute;

    //assign what select attribute treat as attribute1 and attribute2
    if (side == "left") {
        attribute1 = document.forms[0].category_name;
        attribute2 = document.forms[0].category_selected; //右リスト
    } else {
        attribute1 = document.forms[0].category_selected;
        attribute2 = document.forms[0].category_name;
    }

    //fill an array with old values
    for (var i = 0; i < attribute2.length; i++) {
        if (side == "left") {
            //今年度HR_CLASS無い人は左寄せ不可
            if (attribute2.options[i].value.substring(0, 1) != "_") {
                y = current1++;
                tempVal1[y] = attribute2.options[i].value;
                tempText1[y] = attribute2.options[i].text;
                tempSort[y] = String(attribute2.options[i].value) + "," + y;
            }
        } else {
            y = current1++;
            tempVal1[y] = attribute2.options[i].value;
            tempText1[y] = attribute2.options[i].text;
            tempSort[y] = String(attribute2.options[i].value) + "," + y;
        }
    }

    //assign new values to arrays
    for (var i = 0; i < attribute1.length; i++) {
        if (side == "left") {
            //今年度HR_CLASS無い人は左寄せ不可
            if (attribute1.options[i].selected && attribute1.options[i].value.substring(0, 1) != "_") {
                y = current1++;
                tempVal1[y] = attribute1.options[i].value;
                tempText1[y] = attribute1.options[i].text;
                tempSort[y] = String(attribute1.options[i].value) + "," + y;
            } else {
                y = current2++;
                tempVal2[y] = attribute1.options[i].value;
                tempText2[y] = attribute1.options[i].text;
            }
        } else {
            if (attribute1.options[i].selected) {
                y = current1++;
                tempVal1[y] = attribute1.options[i].value;
                tempText1[y] = attribute1.options[i].text;
                tempSort[y] = String(attribute1.options[i].value) + "," + y;
            } else {
                y = current2++;
                tempVal2[y] = attribute1.options[i].value;
                tempText2[y] = attribute1.options[i].text;
            }
        }
    }

    tempSort.sort(); // 2004/01/23

    //generating new options // 2004/01/23
    for (var i = 0; i < tempVal1.length; i++) {
        var val = tempSort[i];
        var tmp = val.split(",");

        attribute2.options[i] = new Option();
        attribute2.options[i].value = tempVal1[tmp[1]];
        attribute2.options[i].text = tempText1[tmp[1]];
    }

    //generating new options
    ClearList(attribute1, attribute1);
    if (tempVal2.length > 0) {
        for (var i = 0; i < tempVal2.length; i++) {
            attribute1.options[i] = new Option();
            attribute1.options[i].value = tempVal2[i];
            attribute1.options[i].text = tempText2[i];
        }
    }

    document.getElementById("LEFT_PART_NUM").innerHTML = document.forms[0].category_selected.options.length;
    document.getElementById("RIGHT_PART_NUM").innerHTML = document.forms[0].category_name.options.length;
}
function moves(sides) {
    var temp5 = new Array();
    var temp6 = new Array();
    var tempc = new Array();
    var tempd = new Array();
    var tempaa = new Array();
    var tempbb = new Array();
    var current5 = 0;
    var current6 = 0;
    var z = 0;

    //assign what select attribute treat as attribute5 and attribute6
    if (sides == "left") {
        attribute5 = document.forms[0].category_name;
        attribute6 = document.forms[0].category_selected; //右リスト
    } else {
        attribute5 = document.forms[0].category_selected;
        attribute6 = document.forms[0].category_name;
    }

    //fill an array with old values
    for (var i = 0; i < attribute6.length; i++) {
        if (sides == "left") {
            if (attribute6.options[i].value.substring(0, 1) != "_") {
                z = current5++;
                temp5[z] = attribute6.options[i].value;
                tempc[z] = attribute6.options[i].text;
                tempaa[z] = String(attribute6.options[i].value) + "," + z;
            }
        } else {
            z = current5++;
            temp5[z] = attribute6.options[i].value;
            tempc[z] = attribute6.options[i].text;
            tempaa[z] = String(attribute6.options[i].value) + "," + z;
        }
    }

    //assign new values to arrays
    for (var i = 0; i < attribute5.length; i++) {
        if (sides == "left") {
            if (attribute5.options[i].value.substring(0, 1) != "_") {
                z = current5++;
                temp5[z] = attribute5.options[i].value;
                tempc[z] = attribute5.options[i].text;
                tempaa[z] = String(attribute5.options[i].value) + "," + z;
            } else {
                z = current6++;
                temp6[z] = attribute5.options[i].value;
                tempd[z] = attribute5.options[i].text;
                tempbb[z] = String(attribute5.options[i].value) + "," + z;
            }
        } else {
            z = current5++;
            temp5[z] = attribute5.options[i].value;
            tempc[z] = attribute5.options[i].text;
            tempaa[z] = String(attribute5.options[i].value) + "," + z;
        }
    }

    tempaa.sort();
    tempbb.sort();

    //generating new options
    for (var i = 0; i < temp5.length; i++) {
        var val = tempaa[i];
        var tmp = val.split(",");

        attribute6.options[i] = new Option();
        attribute6.options[i].value = temp5[tmp[1]];
        attribute6.options[i].text = tempc[tmp[1]];
    }

    //generating new options
    ClearList(attribute5, attribute5);
    if (sides == "left") {
        if (temp6.length > 0) {
            for (var i = 0; i < temp6.length; i++) {
                var val = tempbb[i];
                var tmp = val.split(",");

                attribute5.options[i] = new Option();
                attribute5.options[i].value = temp6[tmp[1]];
                attribute5.options[i].text = tempd[tmp[1]];
            }
        }
    }

    document.getElementById("LEFT_PART_NUM").innerHTML = document.forms[0].category_selected.options.length;
    document.getElementById("RIGHT_PART_NUM").innerHTML = document.forms[0].category_name.options.length;
}
