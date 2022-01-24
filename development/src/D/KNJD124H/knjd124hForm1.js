function btn_submit(cmd) {
    if (cmd == "subclasscd" || cmd == "chaircd") {
        if (document.forms[0].changeVal !== undefined && document.forms[0].changeVal.value == "1") {
            alert("保存されていないデータは破棄されます。");
        }
    }
    document.forms[0].CHK_FLG.value = "reset";
    if (cmd == "reset") {
        if (!confirm("{rval MSG106}")) return false;
    } else if (cmd == "update") {
        //更新権限チェック
        userAuth = document.forms[0].USER_AUTH.value;
        updateAuth = document.forms[0].UPDATE_AUTH.value;
        if (userAuth < updateAuth) {
            alert("{rval MSG300}");
            return false;
        }
        for (var i = 0; i < document.forms[0].elements.length; i++) {
            var e = document.forms[0].elements[i];
            if (e.type == "text" && e.value != "") {
                //スペース削除
                var str_num = e.value;
                e.value = str_num.replace(/ |　/g, "");
                var str = e.value;
                var nam = e.name;
                //欠課時数情報（-、=)
                if (nam.match(/.INTR./) || nam.match(/.TERM./)) {
                    if ((str == "-") | (str == "*")) {
                        continue;
                    }
                }
                //数字チェック
                if (isNaN(e.value)) {
                    alert("{rval MSG907}");
                    return false;
                }
                //満点チェック
                var perfectName = nam.split("-")[0] + "_PERFECT";
                var perfectNumber = nam.split("-")[1];
                perfectObject = eval('document.forms[0]["' + perfectName + "-" + perfectNumber + '"]');
                var perfect = parseInt(perfectObject.value);
                if (!isNaN(e.value) && (e.value > perfect || e.value < 0)) {
                    alert("{rval MSG901}" + "\n0～" + perfect + "まで入力可能です");
                    return false;
                }
            }
        }
        /*****************************************************************/
        /*** 「成績入力完了チェックの入れ忘れ防止対策(素点)」 --開始-- ***/
        /*****************************************************************/
        // 更新時、下記の処理をする
        // ①：「成績が全て未入力」で「成績入力完了チェックあり」の場合、エラーメッセージを表示する
        // ②：「成績が全て入力済」で「成績入力完了チェックなし」の場合、確認メッセージを表示する
        var score_txt = new Array(); // テキスト入力フラグ
        var score_cnt = new Array(); // 素点入力フラグ
        var score_not = new Array(); // 素点未入力フラグ
        var score_chk = new Array(); // 成績入力完了フラグ
        // 初期化
        for (var i = 0; i < 5; i++) {
            score_txt[i] = false;
            score_cnt[i] = true;
            score_not[i] = true;
            score_chk[i] = true;
        }
        // チェック
        for (var i = 0; i < document.forms[0].elements.length; i++) {
            var e = document.forms[0].elements[i];
            var nam = e.name;
            // 素点入力チェック
            if (e.type == "text" && nam.match(/.SCORE./)) {
                if (nam.match(/SEM1_INTR_SCORE./)) {
                    score_txt[0] = true;
                    if (e.value == "") score_cnt[0] = false;
                    else score_not[0] = false;
                }
                if (nam.match(/SEM1_TERM_SCORE./)) {
                    score_txt[1] = true;
                    if (e.value == "") score_cnt[1] = false;
                    else score_not[1] = false;
                }
                if (nam.match(/SEM2_INTR_SCORE./)) {
                    score_txt[2] = true;
                    if (e.value == "") score_cnt[2] = false;
                    else score_not[2] = false;
                }
                if (nam.match(/SEM2_TERM_SCORE./)) {
                    score_txt[3] = true;
                    if (e.value == "") score_cnt[3] = false;
                    else score_not[3] = false;
                }
                if (nam.match(/SEM3_TERM_SCORE./)) {
                    score_txt[4] = true;
                    if (e.value == "") score_cnt[4] = false;
                    else score_not[4] = false;
                }
            }
            // 成績入力完了チェック
            if (e.type == "checkbox" && nam.match(/CHK_COMP./)) {
                if (nam.match(/CHK_COMP1/)) score_chk[0] = e.checked;
                if (nam.match(/CHK_COMP2/)) score_chk[1] = e.checked;
                if (nam.match(/CHK_COMP3/)) score_chk[2] = e.checked;
                if (nam.match(/CHK_COMP4/)) score_chk[3] = e.checked;
                if (nam.match(/CHK_COMP5/)) score_chk[4] = e.checked;
            }
        }
        // 成績入力完了チェックの入れ忘れメッセージ
        var score_msg = new Array();
        var semename1 = document.forms[0].SEMENAME1.value;
        var semename2 = document.forms[0].SEMENAME2.value;
        var semename3 = document.forms[0].SEMENAME3.value;
        var testname1 = document.forms[0].TESTNAME10101.value;
        var testname2 = document.forms[0].TESTNAME10201.value;
        var testname3 = document.forms[0].TESTNAME20101.value;
        var testname4 = document.forms[0].TESTNAME20201.value;
        var testname5 = document.forms[0].TESTNAME30201.value;
        score_msg[0] = "（" + semename1 + testname1 + "）";
        score_msg[1] = "（" + semename1 + testname2 + "）";
        score_msg[2] = "（" + semename2 + testname3 + "）";
        score_msg[3] = "（" + semename2 + testname4 + "）";
        score_msg[4] = "（" + semename3 + testname5 + "）";
        var info_msg = "";
        var info_msg2 = "";
        var info_msg3 = "";
        for (var i = 0; i < 5; i++) {
            // ①の場合
            if (score_txt[i] && score_cnt[i] && !score_chk[i]) info_msg = info_msg + score_msg[i];
            // ②の場合
            if (score_txt[i] && score_not[i] && score_chk[i]) info_msg2 = info_msg2 + score_msg[i];
            // ③の場合
            if (score_txt[i] && !score_cnt[i] && !score_not[i] && score_chk[i]) info_msg3 = info_msg3 + score_msg[i];
        }
        /*****************************************************************/
        /*** 「成績入力完了チェックの入れ忘れ防止対策(素点)」 --終了-- ***/
        /*****************************************************************/

        /*****************************************************************/
        /*** 「成績入力完了チェックの入れ忘れ防止対策(評価)」 --開始-- ***/
        /******************************************************************
        // 更新時、下記の処理をする
        // ①：「成績が全て未入力」で「成績入力完了チェックあり」の場合、エラーメッセージを表示する
        // ②：「成績が全て入力済」で「成績入力完了チェックなし」の場合、確認メッセージを表示する
        var value_txt = new Array(); // テキスト入力フラグ
        var value_cnt = new Array(); // 素点入力フラグ
        var value_not = new Array(); // 素点未入力フラグ
        var value_chk = new Array(); // 成績入力完了フラグ
        var value_dis = new Array(); // 成績入力完了フラグ
        // 初期化
        for (var i = 0; i < 3; i++ ) {
            value_txt[i] = false;
            value_cnt[i] = true;
            value_not[i] = true;
            value_chk[i] = true;
            value_dis[i] = false;
        }
        // チェック
        for (var i = 0; i < document.forms[0].elements.length; i++ ) {
            var e = document.forms[0].elements[i];
            var nam = e.name;
            // 素点入力チェック
            if (e.type == 'text' && (nam.match(/.VALUE./) || nam.match(/GRAD_SCORE./))) {
                if (nam.match(/SEM1_VALUE./)) {
                    value_txt[0] = true;
                    if (e.value == '') value_cnt[0] = false;
                    else               value_not[0] = false;
                }
                if (nam.match(/SEM2_VALUE./)) {
                    value_txt[1] = true;
                    if (e.value == '') value_cnt[1] = false;
                    else               value_not[1] = false;
                }
                if (nam.match(/GRAD_VALUE./)) {
                    value_txt[2] = true;
                    if (e.value == '') value_cnt[2] = false;
                    else               value_not[2] = false;
                }
            }
            // 成績入力完了チェック
            if (e.type == 'checkbox' && nam.match(/CHK_COMP_VALUE./)) {
                if (nam.match(/CHK_COMP_VALUE1/)) {value_chk[0] = e.checked; value_dis[0] = e.disabled;}
                if (nam.match(/CHK_COMP_VALUE2/)) {value_chk[1] = e.checked; value_dis[1] = e.disabled;}
                if (nam.match(/CHK_COMP_VALUE3/)) {value_chk[2] = e.checked; value_dis[2] = e.disabled;}
            }
        }
        // 成績入力完了チェックの入れ忘れメッセージ
        var value_msg = new Array();
        var semename1 = document.forms[0].SEMENAME1.value;
        var semename2 = document.forms[0].SEMENAME2.value;

        value_msg[0] = "（"+semename1+"評価）";
        value_msg[1] = "（"+semename2+"評価）";
        value_msg[2] = "（学年評定）";
        for (var i = 0; i < 3; i++ ) {
            // ①の場合
            if (value_txt[i] && value_cnt[i] && !value_chk[i]) info_msg = info_msg + value_msg[i];
            // ②の場合
            if (value_txt[i] && value_not[i] && value_chk[i]) info_msg2 = info_msg2 + value_msg[i];
            // ③の場合
            if (value_txt[i] && !value_cnt[i] && !value_not[i] && value_chk[i]) info_msg3 = info_msg3 + value_msg[i];
        }
        ******************************************************************/
        /*** 「成績入力完了チェックの入れ忘れ防止対策(評価)」 --終了-- ***/
        /*****************************************************************/
        if (info_msg2 != "") {
            alert(info_msg2 + "\n\n成績入力完了にチェックが入っています。\n成績が全て未入力の場合、成績入力完了にチェックはできません。");
            return false;
        }
        if (info_msg3 != "") {
            if (!confirm(info_msg3 + "\n\n成績入力完了にチェックが入っています。\n成績に未入力がありますが、このまま更新してもよろしいですか？")) return false;
        }
        if (info_msg != "") {
            if (!confirm(info_msg + "\n\n成績入力完了にチェックが入っていません。\n成績が全て入力済みですが、このまま更新してもよろしいですか？")) return false;
        }

        clickedBtnUdpate(true);
    }

    //更新ボタン・・・読み込み中は、更新ボタンをグレー（押せないよう）にする。
    document.forms[0].btn_update.disabled = true;
    document.forms[0].btn_calc.disabled = true;
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
function clickedBtnUdpate(disFlg) {
    if (disFlg) {
        document.forms[0].H_SUBCLASSCD.value = document.forms[0].SUBCLASSCD.value;
        document.forms[0].H_CHAIRCD.value = document.forms[0].CHAIRCD.value;
    } else {
        document.forms[0].SUBCLASSCD.value = document.forms[0].H_SUBCLASSCD.value;
        document.forms[0].CHAIRCD.value = document.forms[0].H_CHAIRCD.value;
    }
    document.forms[0].SUBCLASSCD.disabled = disFlg;
    document.forms[0].CHAIRCD.disabled = disFlg;
    document.forms[0].btn_reset.disabled = disFlg;
    document.forms[0].btn_end.disabled = disFlg;
    document.forms[0].btn_print.disabled = disFlg;
    //CSV
    document.forms[0].csv.disabled = disFlg;
    document.forms[0].userfile.disabled = disFlg;
    document.forms[0].btn_refer.disabled = disFlg;
    document.forms[0].btn_exec.disabled = disFlg;
}

function calc(obj) {
    //スペース削除
    var str_num = obj.value;
    obj.value = str_num.replace(/ |　/g, "");
    var str = obj.value;
    var nam = obj.name;

    var markFlg = false;
    if (nam.match(/.INTR./) || nam.match(/.TERM./)) {
        if ((str == "-") | (str == "*")) {
            markFlg = true;
            //            return;
        }
    }
    //記号以外
    if (!markFlg) {
        //数字チェック
        if (isNaN(obj.value)) {
            alert("{rval MSG907}");
            obj.value = obj.defaultValue;
            return;
        }
        //満点チェック
        var perfectName = nam.split("-")[0] + "_PERFECT";
        var perfectNumber = nam.split("-")[1];
        perfectObject = eval('document.forms[0]["' + perfectName + "-" + perfectNumber + '"]');
        var perfect = parseInt(perfectObject.value);

        var score = parseInt(obj.value);
        if (score > perfect) {
            alert("{rval MSG914}" + "0点～" + perfect + "点以内で入力してください。");
            obj.value = obj.defaultValue;
            return;
        }

        var score = parseInt(obj.value);
        if (score < 0) {
            alert("{rval MSG914}" + "0点～" + perfect + "点以内で入力してください。");
            obj.value = obj.defaultValue;
            return;
        }

        if (document.forms[0].gen_ed.value != "" && nam.match(/GRAD_VALUE./)) {
            var n = nam.split("-");
            if (a_mark[obj.value] == undefined) {
                outputLAYER("mark" + n[1], "");
            } else {
                outputLAYER("mark" + n[1], a_mark[obj.value]);
            }
        }
    }

    //合計を計算する
    if (nam.match(/SEM1_TERM_SCORE./) || nam.match(/SEM2_TERM_SCORE./)) {
        //calcTotal(obj);
    }
}

function newwin(SERVLET_URL) {
    //alert('工事中です！');
    //return;
    if (document.forms[0].SUBCLASSCD.value == "") {
        alert("科目を指定してください。");
        return;
    }
    if (document.forms[0].CHAIRCD.value == "") {
        alert("学級・講座を指定してください。");
        return;
    }
    action = document.forms[0].action;
    target = document.forms[0].target;
    //    document.forms[0].action = "/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL + "/KNJD";
    document.forms[0].target = "_blank";
    document.forms[0].submit();
    document.forms[0].action = action;
    document.forms[0].target = target;
}

/************************************************* 貼付け関係 ***********************************************/
function showPaste(obj) {
    if (!confirm("内容を貼付けますか？")) {
        return false;
    }

    //テキストボックスの名前の配列を作る
    var nameArray = new Array(
        "SEM1_INTR_SCORE",
        "SEM1_TERM_SCORE",
        "SEM1_VALUE",
        "SEM1_VALUE_KARI",
        "SEM2_INTR_SCORE",
        "SEM2_TERM_SCORE",
        "SEM2_VALUE",
        "SEM2_VALUE_KARI",
        "SEM3_TERM_SCORE",
        "GRAD_SCORE_HEIJOU",
        "GRAD_SCORE",
        "GRAD_VALUE",
        "COMP_CREDIT",
        "GET_CREDIT"
    );

    insertTsv({
        clickedObj: obj,
        harituke_type: "renban",
        objectNameArray: nameArray,
    });
    //これを実行しないと貼付けそのものが実行されてしまう
    return false;
}

/****************************************/
/* 実際に貼付けを実行する関数           */
/* 貼付け時に必要な処理(自動計算とか)は */
/* ここに書きます。                     */
/****************************************/
function execCopy(targetObject, val, targetNumber) {
    if (targetObject.value != val) {
        targetObject.style.background = "#ccffcc";
        document.forms[0].changeVal.value = "1";
    }
    if (document.forms[0].gen_ed.value != "" && targetObject.name.match(/GRAD_VALUE/)) {
        if (a_mark[targetObject.value] == undefined) {
            outputLAYER("mark" + targetNumber, "");
        } else {
            outputLAYER("mark" + targetNumber, a_mark[targetObject.value]);
        }
    }
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

                    if (objectNameArray[k].match(/INTR/) || objectNameArray[k].match(/TERM/)) {
                        if (clipTextArray[j][i] == "-" || clipTextArray[j][i] == "*") {
                            i++;
                            continue;
                        }
                    }

                    //数字であるのかチェック
                    if (isNaN(clipTextArray[j][i])) {
                        alert("{rval MSG907}");
                        return false;
                    }

                    //満点チェック
                    perfectNumber = parseInt(targetNumber) + j;
                    perfectObject = eval('document.forms[0]["' + objectNameArray[k] + "_PERFECT" + "-" + perfectNumber + '"]');
                    if (perfectObject) {
                        perfect = parseInt(perfectObject.value);
                        valScore = parseInt(clipTextArray[j][i]);
                        if (clipTextArray[j][i] < 0 || clipTextArray[j][i] > perfect) {
                            alert("{rval MSG914}" + "0点～" + perfect + "点以内で入力してください。");
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
//子画面へ
function openKogamen(URL) {
    if (document.forms[0].SUBCLASSCD.value == "") {
        alert("科目を指定してください。");
        return;
    }
    if (document.forms[0].CHAIRCD.value == "") {
        alert("学級・講座を指定してください。");
        return;
    }

    wopen(URL, "SUBWIN2", 0, 0, screen.availWidth, screen.availHeight);
}

// Enterキーが押されたときに「TABキーが押された」イベントにするメソッド
function keyChangeEntToTab2(obj, setTextField, cnt) {
    //移動可能なオブジェクト
    var textFieldArray = setTextField.split(",");
    //行数
    var lineCnt = document.forms[0].COUNT.value;
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
    var moveEnt = document.forms[0].MOVE_ENTER[0].checked ? 40 : 39;
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
//総合点算出
//中間・期末・平常点を入力する時、総合点算出ボタン有効
//①年５回考査実施・・・考査得点合計÷５×０．８＋平常点
//②年３回考査実施・・・考査得点合計÷３×０．８＋平常点
//1,2学期仮評定算出
//1,2学期期末を入力する時、算出
//③年５回考査実施・・・1学期仮評定=考査得点合計÷２、2学期仮評定=考査得点合計÷４
//④年３回考査実施・・・1学期仮評定=考査得点合計÷１、2学期仮評定=考査得点合計÷２
function btnCalc(setDiv, isControlFlg1, isControlFlg2) {
    //注意メッセージ
    if (!confirm("総合点を算出します。よろしいですか？\n\n【注意：入力されていた総合点は上書きされます。】")) {
        return false;
    }
    //行数
    var lineCnt = document.forms[0].COUNT.value;
    if (lineCnt == 0) {
        alert("生徒が0件です。");
        return;
    }
    //考査の入力状況
    var inputArray = new Array("SEM1_INTR_SCORE", "SEM1_TERM_SCORE", "SEM2_INTR_SCORE", "SEM2_TERM_SCORE", "SEM3_TERM_SCORE");
    var inputFlg = new Array(false, false, false, false, false);
    for (var k = 0; k < inputArray.length; k++) {
        for (var i = 0; i < lineCnt; i++) {
            scoreObject = eval('document.forms[0]["' + inputArray[k] + "-" + i + '"]');
            if ((!isNaN(scoreObject.value) && scoreObject.value != "") || scoreObject.value == "*" || scoreObject.value == "-") {
                inputFlg[k] = true;
            }
        }
    }
    //1,2学期中間が入力されているか・・・年５回考査実施を決める
    var isInputIntr = inputFlg[0] || inputFlg[2];
    //1学期期末が入力されているか・・・1学期仮評定算出を決める
    var isInputTerm1 = inputFlg[1];
    //2学期期末が入力されているか・・・2学期仮評定算出を決める
    var isInputTerm2 = inputFlg[3];
    //3学期期末が入力されているか
    var isInputTerm3 = inputFlg[4];

    var setCalcFlg = false;
    if (setDiv == "kari") {
        if (isControlFlg1 && isInputTerm1) {
            setCalc("SEM1_VALUE", lineCnt, isInputIntr);
            setCalcFlg = true;
        }
        if ((isControlFlg1 || isControlFlg2) && isInputTerm2) {
            setCalc("SEM2_VALUE", lineCnt, isInputIntr);
            setCalcFlg = true;
        }
    } else if (setDiv == "sogo") {
        if (isInputTerm1 || isInputTerm2 || isInputTerm3) {
            setCalc("GRAD_SCORE", lineCnt, isInputIntr);
            setCalcFlg = true;
        }
    }
    if (setCalcFlg) {
        alert("算出しました。\n更新ボタンを押下しないとデータは保存されません。");
    } else {
        alert("期末が入力されていません。");
    }
}

//仮評定・総合点算出
function setCalc(setName, lineCnt, isInputIntr) {
    //ログイン年度
    var year = document.forms[0].YEAR.value;
    //考査
    var nameArray;
    if (isInputIntr) {
        //        alert('年５回考査実施');
        // 2020年度は1学期中間テストが無かった為、参照しない
        if (year == "2020") {
            if (setName == "SEM1_VALUE") {
                nameArray = new Array("SEM1_TERM_SCORE");
            } else if (setName == "SEM2_VALUE") {
                nameArray = new Array("SEM1_TERM_SCORE", "SEM2_INTR_SCORE", "SEM2_TERM_SCORE");
            } else if (setName == "GRAD_SCORE") {
                nameArray = new Array("SEM1_TERM_SCORE", "SEM2_INTR_SCORE", "SEM2_TERM_SCORE", "SEM3_TERM_SCORE");
            }
        } else {
            if (setName == "SEM1_VALUE") {
                nameArray = new Array("SEM1_INTR_SCORE", "SEM1_TERM_SCORE");
            } else if (setName == "SEM2_VALUE") {
                nameArray = new Array("SEM1_INTR_SCORE", "SEM1_TERM_SCORE", "SEM2_INTR_SCORE", "SEM2_TERM_SCORE");
            } else if (setName == "GRAD_SCORE") {
                nameArray = new Array("SEM1_INTR_SCORE", "SEM1_TERM_SCORE", "SEM2_INTR_SCORE", "SEM2_TERM_SCORE", "SEM3_TERM_SCORE");
            }
        }
    } else {
        //        alert('年３回考査実施');
        if (setName == "SEM1_VALUE") {
            nameArray = new Array("SEM1_TERM_SCORE");
        } else if (setName == "SEM2_VALUE") {
            nameArray = new Array("SEM1_TERM_SCORE", "SEM2_TERM_SCORE");
        } else if (setName == "GRAD_SCORE") {
            nameArray = new Array("SEM1_TERM_SCORE", "SEM2_TERM_SCORE", "SEM3_TERM_SCORE");
        }
    }
    //分母
    var bunbo = 0;
    for (var k = 0; k < nameArray.length; k++) {
        for (var i = 0; i < lineCnt; i++) {
            scoreObject = eval('document.forms[0]["' + nameArray[k] + "-" + i + '"]');
            //中間・期末・平常点はどこまで入力されているかで分母を決める
            if ((!isNaN(scoreObject.value) && scoreObject.value != "") || scoreObject.value == "*" || scoreObject.value == "-") {
                bunbo = k + 1;
            }
        }
    }
    if (bunbo == 0) {
        //        alert('考査が未入力です。');
        return;
    }
    /***
        var msg = '';
        if (setName == 'SEM1_VALUE') msg = "1学期仮評定";
        if (setName == 'SEM2_VALUE') msg = "2学期仮評定";
        if (setName == 'GRAD_SCORE') msg = "総合点";
        if (!confirm(msg + 'を算出します。\nよろしいですか？')) {
            return false;
        }
    ***/
    //評価の算出方法(1:四捨五入 2:切り上げ 3:切り捨て)
    var CalcMethod = document.forms[0].CalcMethod.value;
    //総合点算出
    for (var i = 0; i < lineCnt; i++) {
        var total = 0;
        var scoreCnt = 0;
        var kessiCnt = 0;
        var horyuCnt = 0;
        var horyuCntIntr = 0;
        var horyuCntTerm = 0;
        for (var k = 0; k < nameArray.length; k++) {
            scoreObject = eval('document.forms[0]["' + nameArray[k] + "-" + i + '"]');
            if (!isNaN(scoreObject.value) && scoreObject.value != "") {
                total += parseInt(scoreObject.value);
                scoreCnt++;
            } else if (scoreObject.value == "*") {
                kessiCnt++;
            } else if (scoreObject.value == "-") {
                if (nameArray[k].match(/.INTR./)) horyuCntIntr++;
                if (nameArray[k].match(/.TERM./)) horyuCntTerm++;
                horyuCnt++;
            }
        }
        //平常点
        var heijou = 0;
        scoreObject = eval('document.forms[0]["' + "GRAD_SCORE_HEIJOU" + "-" + i + '"]');
        if (!isNaN(scoreObject.value) && scoreObject.value != "") {
            heijou = parseInt(scoreObject.value);
        }
        //総合点
        var setValue;
        //var setName = "GRAD_SCORE";
        //ブランクがある
        if (bunbo != scoreCnt + kessiCnt + horyuCnt) {
            setValue = "";
            //欠試がある
        } else if (0 < kessiCnt) {
            setValue = "";
            //分母が0・・・javascriptエラー回避
        } else if (bunbo == horyuCnt) {
            setValue = setName == "GRAD_SCORE" ? heijou : "";
        } else if (setName == "GRAD_SCORE") {
            //保留超過・・・中間・期末はそれぞれ１回まで。超過した考査は０点。つまり、超過した考査は分母から引かない。
            var horyuCntMax = 1;
            if (horyuCntMax < horyuCntIntr) horyuCntIntr = horyuCntMax;
            if (horyuCntMax < horyuCntTerm) horyuCntTerm = horyuCntMax;
            //算出
            var avg = parseFloat(total / (bunbo - horyuCntIntr - horyuCntTerm)) * 0.8;
            //評価の算出方法(1:四捨五入 2:切り上げ 3:切り捨て)
            if (CalcMethod == "1") {
                setValue = Math.round(avg) + heijou;
            } else if (CalcMethod == "2") {
                setValue = Math.ceil(avg) + heijou;
            } else if (CalcMethod == "3") {
                setValue = Math.floor(avg) + heijou;
            }
        } else if (setName == "SEM1_VALUE" || setName == "SEM2_VALUE") {
            //保留超過・・・中間・期末はそれぞれ１回まで。超過した考査は０点。つまり、超過した考査は分母から引かない。
            var horyuCntMax = 1;
            if (horyuCntMax < horyuCntIntr) horyuCntIntr = horyuCntMax;
            if (horyuCntMax < horyuCntTerm) horyuCntTerm = horyuCntMax;
            //算出
            var avg = parseFloat(total / (bunbo - horyuCntIntr - horyuCntTerm));
            //評価の算出方法(1:四捨五入 2:切り上げ 3:切り捨て)
            if (CalcMethod == "1") {
                setValue = Math.round(avg);
            } else if (CalcMethod == "2") {
                setValue = Math.ceil(avg);
            } else if (CalcMethod == "3") {
                setValue = Math.floor(avg);
            }
        }
        //ラベル
        targetname = setName + "_SPAN_ID" + "-" + i;
        targetObject = document.getElementById(targetname);
        if (targetObject) {
            targetObject.innerHTML = setValue;
        }
        //テキストまたはhidden
        totalObject = eval('document.forms[0]["' + setName + "-" + i + '"]');
        totalObject.value = setValue;
    }
    //    alert('算出しました。\n更新ボタンを押下しないとデータは保存されません。');
}