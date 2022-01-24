window.addEventListener(
    "load",
    function (event) {
        if (sessionStorage.getItem("KNJH720_CurrentCursor") != null) {
            document.title = "";
            document.getElementById(sessionStorage.getItem("KNJH720_CurrentCursor")).focus();
        }
    },
    false
);

function current_cursor(para) {
    document.title = "";
    sessionStorage.setItem("KNJH720_CurrentCursor", para);
}

function btn_submit(cmd) {
    document.forms[0].encoding = "multipart/form-data";
    if (sessionStorage.getItem("KNJH720_CurrentCursor") != null) {
        document.title = "";
        parent.top_frame.document.title = "";
        document.getElementById(sessionStorage.getItem("KNJH720_CurrentCursor")).blur();
    }
    if (cmd == "classcd" && 0 < document.forms[0].COUNTER.value) {
        if (!confirm("保存されていないデータがあれば破棄されます。処理を続行しますか？")) {
            return false;
        }
    }
    if (cmd == "update") {
        if (document.forms[0].TESTDIV.value == "") {
            alert("学力テスト区分を指定してください。");
            return;
        }
        if (document.forms[0].CLASSCD.value == "") {
            alert("教科を指定してください。");
            return;
        }
        if (document.forms[0].SUBCLASSCD.value == "") {
            alert("科目を指定してください。");
            return;
        }
        //更新権限チェック
        userAuth = document.forms[0].USER_AUTH.value;
        updateAuth = document.forms[0].UPDATE_AUTH.value;
        if (userAuth < updateAuth) {
            alert("{rval MSG300}");
            return false;
        }
        clickedBtnUdpateCalc(true);
    } else if (cmd == "reset") {
        if (!confirm("{rval MSG106}")) return false;
    } else if (cmd == "exec" && !confirm("処理を開始します。よろしいでしょうか？")) {
        return true;
    }

    //フレームロック機能（プロパティの値が1の時有効）
    if (document.forms[0].useFrameLock.value == "1") {
        if (cmd == "update") {
            updateFrameLock();
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//更新時、サブミットする項目使用不可
function clickedBtnUdpateCalc(disFlg) {
    if (disFlg) {
        document.forms[0].H_TESTDIV.value = document.forms[0].TESTDIV.value;
        document.forms[0].H_CLASSCD.value = document.forms[0].CLASSCD.value;
        document.forms[0].H_SUBCLASSCD.value = document.forms[0].SUBCLASSCD.value;
        document.forms[0].H_FACCD.value = document.forms[0].FACCD.value;
    } else {
        document.forms[0].TESTDIV.value = document.forms[0].H_TESTDIV.value;
        document.forms[0].CLASSCD.value = document.forms[0].H_CLASSCD.value;
        document.forms[0].SUBCLASSCD.value = document.forms[0].H_SUBCLASSCD.value;
        document.forms[0].FACCD.value = document.forms[0].H_FACCD.value;
    }
    document.forms[0].SUBCLASSCD.disabled = disFlg;
    document.forms[0].btn_reset.disabled = disFlg;
    document.forms[0].btn_end.disabled = disFlg;
}

function calc(obj) {
    //スペース削除
    var str_num = obj.value;
    var nam = obj.name;
    obj.value = str_num.replace(/ |　/g, "");

    //数字チェック
    if (isNaN(obj.value)) {
        alert("{rval MSG907}");
        obj.value = obj.defaultValue;
        document.getElementById(obj.id).focus();
        return;
    }
    //満点チェック
    var perfectName = nam.split("-")[0] + "_PERFECT";
    var perfectNumber = nam.split("-")[1];
    perfectObject = eval('document.forms[0]["' + perfectName + "-" + perfectNumber + '"]');
    var perfect = parseInt(perfectObject.value);
    var perfectFrom = 0;

    var score = parseInt(obj.value);
    if (score < perfectFrom || perfect < score) {
        alert("{rval MSG914}" + perfectFrom + "点～" + perfect + "点以内で入力してください。");
        obj.value = obj.defaultValue;
        document.getElementById(obj.id).focus();
        return;
    }
}

//入力回数切り替え時処理
function changeInputNum() {
    if (document.forms[0].INPUT_NUM[0].checked) {
        var disableScore = "SCORE2";
        var disableAbsence = "ABSENCE2";
        var notDisableScore = "SCORE1";
        var notDisableAbsence = "ABSENCE1";
    } else {
        var disableScore = "SCORE1";
        var disableAbsence = "ABSENCE1";
        var notDisableScore = "SCORE2";
        var notDisableAbsence = "ABSENCE2";
    }
    for (var i = 0; i < document.forms[0].elements.length; i++) {
        var e = document.forms[0].elements[i];
        var nam = e.name.split("-");
        if (e.type == "text" || e.type == "checkbox") {
            if (nam[0] == disableScore || nam[0] == disableAbsence) {
                e.disabled = true;
            } else if (nam[0] == notDisableScore || nam[0] == notDisableAbsence) {
                e.disabled = false;
            }
        }
    }
}

/************************************************* 貼付け関係 ***********************************************/
function showPaste(obj) {
    if (!confirm("内容を貼付けますか？")) {
        return false;
    }

    //テキストボックスの名前の配列を作る
    var nameArray = [];
    if (document.forms[0].INPUT_NUM[0].checked) {
        //入力1回目
        nameArray[0] = "SCORE1";
    } else {
        //入力2回目
        nameArray[0] = "SCORE2";
    }

    insertTsv({ clickedObj: obj, harituke_type: "renban", objectNameArray: nameArray });
    //これを実行しないと貼付けそのものが実行されてしまう
    return false;
}

/****************************************/
/* 実際に貼付けを実行する関数           */
/* 貼付け時に必要な処理(自動計算とか)は */
/* ここに書きます。                     */
/****************************************/
function execCopy(targetObject, val, targetNumber) {
    targetObject.value = val;
}

/***********************************/
/* クリップボードの中身のチェック  */
/* (だめなデータならばfalseを返す) */
/* (共通関数から呼ばれる)          */
/***********************************/
function checkClip(clipTextArray, harituke_jouhou) {
    var startFlg = false;
    var i;
    var targetName = harituke_jouhou.clickedObj.name.split("-")[0];
    var targetNumber = harituke_jouhou.clickedObj.name.split("-")[1];
    var objectNameArray = harituke_jouhou.objectNameArray;

    for (j = 0; j < clipTextArray.length; j++) {
        //クリップボードの各行をループ
        i = 0;
        startFlg = false;
        for (k = 0; k < objectNameArray.length; k++) {
            //テキストボックス名でまわす
            if (objectNameArray[k] == targetName) {
                //貼付け開始対象のテキストボックスならばフラグを立てる
                startFlg = true;
            }
            if (startFlg) {
                if (clipTextArray[j][i] != undefined) {
                    //スペース削除
                    var str_num = new String(clipTextArray[j][i]);
                    clipTextArray[j][i] = str_num.replace(/ |　/g, "");

                    if (objectNameArray[k].match(/VALUE/)) {
                        if (clipTextArray[j][i] == "-" || clipTextArray[j][i] == "=") {
                            i++;
                            continue;
                        }
                    }

                    //数字であるのかチェック
                    if (clipTextArray[j][i] != "*" && isNaN(clipTextArray[j][i])) {
                        alert("{rval MSG907}");
                        return false;
                    }

                    //満点チェック
                    perfectNumber = parseInt(targetNumber) + j;
                    perfectObject = eval('document.forms[0]["' + objectNameArray[k] + "_PERFECT" + "-" + perfectNumber + '"]');
                    if (perfectObject) {
                        perfect = parseInt(perfectObject.value);
                        valScore = parseInt(clipTextArray[j][i]);
                        perfectFrom = 0;
                        if (clipTextArray[j][i] < perfectFrom || clipTextArray[j][i] > perfect) {
                            alert("{rval MSG914}" + perfectFrom + "点～" + perfect + "点以内で入力してください。");
                            return false;
                        }
                    }
                }
                i++;
            }
        }
    }
    return true;
}

// Enterキーが押されたときに「TABキーが押された」イベントにするメソッド
function keyChangeEntToTab2(obj, setTextField, cnt) {
    //移動可能なオブジェクト
    var textFieldArray = setTextField.split(",");
    //行数
    var lineCnt = document.forms[0].COUNTER.value;
    //1行目の生徒
    var isFirstStudent = cnt == 0 ? true : false;
    //最終行の生徒
    var isLastStudent = cnt == lineCnt - 1 ? true : false;
    // Ent13 Tab9 ←37 ↑38 →39 ↓40
    var e = window.event;
    //方向キー
    //var moveEnt = e.keyCode;
    if (e.keyCode != 13) {
        return;
    }
    var moveEnt = 40;
    for (var i = 0; i < textFieldArray.length; i++) {
        if (textFieldArray[i] + cnt == obj.name) {
            var isFirstItem = i == 0 ? true : false;
            var isLastItem = i == textFieldArray.length - 1 ? true : false;
            if (moveEnt == 37) {
                if (isFirstItem && isFirstStudent) {
                    obj.focus();
                    return;
                }
                if (isFirstItem) {
                    targetname = textFieldArray[textFieldArray.length - 1] + (cnt - 1);
                    targetObject = document.getElementById(targetname);
                    targetObject.focus();
                    return;
                }
                targetname = textFieldArray[i - 1] + cnt;
                targetObject = document.getElementById(targetname);
                targetObject.focus();
                return;
            }
            if (moveEnt == 38) {
                if (isFirstStudent) {
                    obj.focus();
                    return;
                }
                targetname = textFieldArray[i] + (cnt - 1);
                targetObject = document.getElementById(targetname);
                targetObject.focus();
                return;
            }
            if (moveEnt == 39 || moveEnt == 13) {
                if (isLastItem && isLastStudent) {
                    obj.focus();
                    return;
                }
                if (isLastItem) {
                    targetname = textFieldArray[0] + (cnt + 1);
                    targetObject = document.getElementById(targetname);
                    targetObject.focus();
                    return;
                }
                targetname = textFieldArray[i + 1] + cnt;
                targetObject = document.getElementById(targetname);
                targetObject.focus();
                return;
            }
            if (moveEnt == 40) {
                if (isLastItem && isLastStudent) {
                    obj.focus();
                    return;
                }
                if (isLastStudent) {
                    targetname = textFieldArray[i + 1] + 0;
                    targetObject = document.getElementById(targetname);
                    targetObject.focus();
                    return;
                }
                targetname = textFieldArray[i] + (cnt + 1);
                targetObject = document.getElementById(targetname);
                targetObject.focus();
                return;
            }
        }
    }
}
