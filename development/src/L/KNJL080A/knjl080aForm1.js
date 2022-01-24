function btn_submit(cmd) {
    //取消
    if (cmd == "reset" && !confirm("{rval MSG106}")) return true;

    //更新
    if (cmd == "update") {
        if (document.forms[0].HID_RECEPTNO.value.length == 0) {
            return false;
        }
        if (checkInputShDup()) {
            alert("複数の試験区分に入力があります。");
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//複数入力チェック
function checkInputShDup() {
    var isError = false;

    //フォーマット。(受験番号は、「_」区切りでn個)
    //志願者SEQ:受験番号_受験番号,志願者SEQ:受験番号_受験番号
    var arrlinkRecept = document.forms[0]["HID_LINK_RECEPTNO"].value.split(",");
    var arrReceptData = Array();

    for (var i = 0; i < arrlinkRecept.length; i++) {
        var arrExamNoData = arrlinkRecept[i].split(":");
        arrReceptData[arrExamNoData[0]] = { recept: arrExamNoData[1], inputCnt: 0 };
    }

    //同一EXAMNOの入力欄カウント
    var repno = document.forms[0]["HID_RECEPTNO"].value.split(",");
    for (var i = 0; i < repno.length; i++) {
        var examNo = document.forms[0]["EXAMNO-" + repno[i]];
        var inputSh = document.forms[0]["INPUT_SHDIV-" + repno[i]];
        if (inputSh.value != "") {
            arrReceptData[examNo.value].inputCnt++;
        }
    }

    for (var key in arrReceptData) {
        //同一EXAMNOで複数入力は、エラー
        if (arrReceptData[key].inputCnt > 1) {
            isError = true;
            var arrReceptNo = arrReceptData[key].recept.split("_");
            for (var i = 0; i < arrReceptNo.length; i++) {
                var targetRow = document.getElementById("ROW_" + arrReceptNo[i]);
                targetRow.style.backgroundColor = "#c2e76b";
            }
        }
    }
    return isError;
}

//値チェック
function checkNum(obj, receptno) {
    //入学コースコード
    var shdiv = document.forms[0]["SHDIV-" + receptno];
    //数値チェック
    obj.value = toInteger(obj.value);

    if (obj.value != "" && obj.value != 1 && obj.value != 2) {
        alert("{rval MSG901}\n" + "1か2を入力して下さい。");
        obj.select();
        return false;
    }

    var passCourse = document.forms[0]["HID_PASS_COURSE"].value;
    var coursecd = document.forms[0]["COURSECD" + obj.value + "-" + receptno];
    var errFlg = false;
    if (typeof coursecd === "undefined") {
        errFlg = true;
    } else if (passCourse.indexOf(coursecd.value) == -1) {
        errFlg = true;
    }

    /*
        専願・併願の合格フラグ配列
        passFlgArray[1]:専願フラグ
        passFlgArray[2]:併願合格フラグ
    */
    var passFlgArray = Array();
    for (var i = 1; i <= 2; i++) {
        var passFlg = document.forms[0]["COURSECD" + i + "-" + receptno].value > 0 ? 1 : 0; //0以上の値は合格なのでフラグを立てる
        passFlgArray[i] = passFlg;
    }

    if (obj.value && (passFlgArray[obj.value] != 1 || obj.value == "0" || errFlg)) {
        //エラーメッセージ
        mes = sep = "";
        mesArray = Array();
        for (var i = 1; i <= 2; i++) {
            if (passFlgArray[i]) {
                mesArray.push(i);
            }
        }
        if (mesArray.length !== 0) {
            mes += mesArray.join(",") + "を入力してください。";
        }
        alert("{rval MSG901}\n" + mes);
        obj.select();

        return false;
    }

    var targetRow = document.getElementById("ROW_" + receptno);

    var changeFlg = false;
    //背景色
    var defaultData = document.forms[0]["DEFAULT_SCORE-" + receptno].value;
    color = "white";
    if (obj.value != defaultData) {
        color = "pink";
        changeFlg = true;
    }

    for (var i = 1; i <= 2; i++) {
        id = "COURSE" + i + "_" + receptno;

        //選択
        selectedCourse = document.getElementById(id);

        courseColor = "white";
        if (obj.value == i) {
            courseColor = "pink";
        }
        if (selectedCourse != null) {
            selectedCourse.style.backgroundColor = courseColor;
        }
    }

    if (changeFlg) {
        targetRow.style.backgroundColor = "pink";
    } else {
        targetRow.style.backgroundColor = "white";
    }

    //コース名セット
    coursename = document.getElementById("COURSENAME-" + receptno);
    coursename.innerHTML = "";
    if (obj.value == "1") {
        coursename.innerHTML = document.forms[0]["COURSENAME1-" + receptno].value;
    } else if (obj.value == "2") {
        coursename.innerHTML = document.forms[0]["COURSENAME2-" + receptno].value;
    }

    return true;
}

// Enterキーが押されたときに「TABキーが押された」イベントにするメソッド
function keyChangeEntToTab(obj, receptno) {
    var ar = arguments.callee.caller.arguments[0];

    if (ar.keyCode == "13" && document.activeElement.name == obj.name) {
        //入学コースコード
        var shdiv = document.forms[0]["SHDIV-" + receptno];

        //数値チェック
        var newString = "";
        var count = 0;
        for (i = 0; i < obj.value.length; i++) {
            ch = obj.value.substring(i, i + 1);
            if (ch >= "0" && ch <= "9") {
                newString += ch;
            }
        }
        if (obj.value != newString) {
            //イベント削除
            var event = obj.onblur;
            obj.onblur = "";

            alert("{rval MSG907}\n入力された文字列は削除されます。");
            obj.value = newString;

            //イベント再登録
            obj.onblur = event;
            return;
        }

        var passCourse = document.forms[0]["HID_PASS_COURSE"].value;
        var coursecd = document.forms[0]["COURSECD" + obj.value + "-" + receptno];
        var errFlg = false;
        if (typeof coursecd === "undefined") {
            errFlg = true;
        } else if (passCourse.indexOf(coursecd.value) == -1) {
            errFlg = true;
        }

        var dataCheckFlg = true;
        mes = sep = "";
        if (obj.value && (obj.value > shdiv.value || obj.value == "0" || errFlg)) {
            //エラーメッセージ
            for (var i = 1; i <= shdiv.value; i++) {
                var tmp = document.forms[0]["COURSECD" + i + "-" + receptno].value;
                if (passCourse.indexOf(tmp) != -1) {
                    mes += sep + i;
                    sep = ",";
                }
            }
            mes += "を入力してください。";
            dataCheckFlg = false;
        }

        //対象取得
        var setArr = [];
        var z = 0;
        for (var i = 0; i < document.forms[0].elements.length; i++) {
            var e = document.forms[0].elements[i];
            //テキストボックスが対象
            if (e.type == "text") {
                setArr[z] = e.name;
                z++;
            }
        }

        var setFormName = null;
        var index = setArr.indexOf(obj.name);
        if (ar.shiftKey) {
            if (index > 0) {
                index--;
            }
            setFormName = setArr[index];
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
            setFormName = setArr[index];
            if (document.forms[0][setFormName].disabled == true) {
                for (var i = index; i < setArr.length - 1; i++) {
                    setFormName = setArr[i];
                    if (document.forms[0][setFormName].disabled == false) break;
                }
            }
        }

        if (dataCheckFlg == true) {
            //最終行
            if (setFormName == obj.name) {
                for (var i = 1; i <= 2; i++) {
                    id = "COURSE" + i + "_" + receptno;

                    //選択
                    selectedCourse = document.getElementById(id);

                    //背景色
                    color = "white";
                    if (obj.value == i) {
                        color = "pink";
                    }

                    if (selectedCourse != null) {
                        selectedCourse.style.backgroundColor = color;
                    }
                }

                //コース名セット
                coursename = document.getElementById("COURSENAME-" + receptno);
                coursename.innerHTML = "";
                if (obj.value == "1") {
                    coursename.innerHTML = document.forms[0]["COURSENAME1-" + receptno].value;
                } else if (obj.value == "2") {
                    coursename.innerHTML = document.forms[0]["COURSENAME2-" + receptno].value;
                }
            } else {
                document.forms[0][setFormName].focus();
                document.forms[0][setFormName].select();
            }
        } else {
            //イベント削除
            var event = obj.onblur;
            obj.onblur = "";

            alert("{rval MSG901}\n" + mes);
            obj.select();

            //イベント再登録
            obj.onblur = event;
        }

        return;
    }
}

//コース選択（列）
function changeAllCourse(id) {
    var selSH = id.substr(-1);
    var selCourse = document.forms[0]["SELECT_COURSE"];
    var repno = document.forms[0]["HID_RECEPTNO"].value.split(",");
    var passCourse = document.forms[0]["HID_PASS_COURSE"].value;

    for (var i = 0; i < repno.length; i++) {
        var shdiv = document.forms[0]["SHDIV-" + repno[i]];
        var input = document.forms[0]["INPUT_SHDIV-" + repno[i]];
        var coursecd = document.forms[0]["COURSECD" + selSH + "-" + repno[i]].value;

        if (!input.disabled && selSH <= shdiv.value && selSH != selCourse.value && passCourse.indexOf(coursecd) != -1) {
            input.value = selSH;
        } else if (input.value == selSH) {
            input.value = null;
        }

        //背景色、コース名セット
        setBackColor(shdiv, input, repno[i]);
    }

    //背景色切替
    for (var j = 1; j <= shdiv.value; j++) {
        target = "COURSE" + j;
        selectedCourse = document.getElementById(target);

        if (selSH == j && selSH != selCourse.value) {
            selectedCourse.style.color = "black";
            selectedCourse.style.backgroundColor = "pink";
        } else {
            selectedCourse.style.backgroundColor = "";
            selectedCourse.style.color = "";
        }
    }

    if (selSH != selCourse.value) {
        selCourse.value = selSH;
    } else {
        selCourse.value = null;
    }
}

//カーソルによる背景色切替
function changeColor(div, flg, id) {
    var selCourse = document.forms[0]["SELECT_COURSE"];
    var select = id.split("_");
    var selSH = select[0].substr(-1);

    if (div == "on") {
        if (flg == "1") {
            document.getElementById(id).style.color = "black";
        }
        document.getElementById(id).style.backgroundColor = "orange";
    } else {
        //明細行
        if (select.length > 1) {
            var repno = select[1];
            var input = document.forms[0]["INPUT_SHDIV-" + repno];

            if (input.value && input.value == selSH) {
                document.getElementById(id).style.backgroundColor = "pink";
            } else {
                document.getElementById(id).style.backgroundColor = "white";
            }

            //項目行
        } else {
            if (selCourse.value == selSH) {
                if (flg == "1") {
                    document.getElementById(id).style.color = "black";
                }
                document.getElementById(id).style.backgroundColor = "pink";
            } else {
                document.getElementById(id).style.backgroundColor = "";
                document.getElementById(id).style.color = "";
            }
        }
    }
}

//コース選択
function selectCourse(id) {
    var select = id.split("_");
    var shdiv = document.forms[0]["SHDIV-" + select[1]];
    var input = document.forms[0]["INPUT_SHDIV-" + select[1]];
    var selSH = select[0].substr(-1);

    //入学コーステキストに値をセット
    if (!input.disabled && selSH <= shdiv.value) {
        if (input.value == selSH) {
            input.value = null;
        } else {
            input.value = selSH;
        }
    }

    //背景色、コース名セット
    setBackColor(shdiv, input, select[1]);
}

//背景色、コース名セット
function setBackColor(shdiv, input, repno) {
    var targetRow = document.getElementById("ROW_" + repno);
    var changeFlg = false;
    for (var i = 1; i <= shdiv.value; i++) {
        target = "COURSE" + i + "_" + repno;

        //選択
        selectedCourse = document.getElementById(target);

        //背景色
        color = "white";
        if (input.value == i) {
            color = "pink";
        }

        if (selectedCourse != null) {
            selectedCourse.style.backgroundColor = color;
            changeFlg = true;
        }
    }

    if (input.value && changeFlg) {
        targetRow.style.backgroundColor = "pink";
    } else {
        targetRow.style.backgroundColor = "white";
    }

    //コース名セット
    coursename = document.getElementById("COURSENAME-" + repno);
    coursename.innerHTML = "";
    if (input.value == "1") {
        coursename.innerHTML = document.forms[0]["COURSENAME1-" + repno].value;
    } else if (input.value == "2") {
        coursename.innerHTML = document.forms[0]["COURSENAME2-" + repno].value;
    }
}
