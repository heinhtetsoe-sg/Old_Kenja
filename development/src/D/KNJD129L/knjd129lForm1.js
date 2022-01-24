function btn_submit(cmd) {
    if (cmd == 'update') {
        if (document.forms[0].SUBCLASSCD.value == '') {
            alert('科目を指定してください。');
            return;
        }
        if (document.forms[0].CHAIRCD.value == '') {
            alert('学級・講座を指定してください。');
            return;
        }
    }
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) return false;
    } else if (cmd == 'update') {
        //更新権限チェック
        userAuth = document.forms[0].USER_AUTH.value;
        updateAuth = document.forms[0].UPDATE_AUTH.value;
        if (userAuth < updateAuth){
            alert('{rval MSG300}');
            return false;
        }
        /*****************************************************************/
        /*** 「成績入力完了チェックの入れ忘れ防止対策(素点)」 --開始-- ***/
        /*****************************************************************/
        // 更新時、下記の処理をする
        // ①：「成績が全て未入力」で「成績入力完了チェックあり」の場合、エラーメッセージを表示する
        // ②：「成績が全て入力済」で「成績入力完了チェックなし」の場合、確認メッセージを表示する
        //テキストボックスの名前の配列を作る
        var textFieldName = document.forms[0].TEXT_FIELD_NAME2.value;
        var chkCompName = document.forms[0].CHK_COMP_NAME.value;
        var nameArray = textFieldName.split(",");
        var chkCompNameArray = chkCompName.split(",");
        var score_txt = new Array(); // テキスト入力フラグ
        var score_cnt = new Array(); // 素点入力フラグ
        var score_not = new Array(); // 素点未入力フラグ
        var score_chk = new Array(); // 成績入力完了フラグ
        // 初期化
        for (var i = 0; i < nameArray.length; i++ ) { //テキストボックス名でまわす
            score_txt[i] = false;
            score_cnt[i] = true;
            score_not[i] = true;
            score_chk[i] = true;
        }
        // チェック
        for (var i = 0; i < document.forms[0].elements.length; i++ ) {
            var e = document.forms[0].elements[i];
            var nam = e.name;
            for (var k = 0; k < nameArray.length; k++ ) { //テキストボックス名でまわす
                // 素点入力チェック
                if (e.type == 'text' && nam.split("-")[0] == nameArray[k]) {
                    score_txt[k] = true;
                    if (e.value == '') score_cnt[k] = false;
                    else               score_not[k] = false;
                }
                // 成績入力完了チェック
                if (e.type == 'checkbox' && nam == chkCompNameArray[k]) score_chk[k] = e.checked;
            }
        }
        // 成績入力完了チェックの入れ忘れメッセージ
        var testItemName = document.forms[0].TEST_ITEM_NAME.value;
        var testItemNameArray = testItemName.split(",");
        var score_msg = new Array();
        for (var i = 0; i < nameArray.length; i++ ) { //テキストボックス名でまわす
            score_msg[i] = "（"+testItemNameArray[i]+"）";
        }
        var info_msg = "";
        var info_msg2 = "";
        var info_msg3 = "";
        for (var i = 0; i < nameArray.length; i++ ) { //テキストボックス名でまわす
            // ①の場合
            if (score_txt[i] && score_cnt[i] && !score_chk[i]) info_msg = info_msg + score_msg[i];
            // ②の場合
            if (score_txt[i] && score_not[i] && score_chk[i]) info_msg2 = info_msg2 + score_msg[i];
            // ③の場合
            if (score_txt[i] && !score_cnt[i] && !score_not[i] && score_chk[i]) info_msg3 = info_msg3 + score_msg[i];
        }
        if (info_msg2 != "") {
            //宮城県の時、メッセージを変更する
            if (document.forms[0].z010name1.value == "miyagiken") {
                if (!confirm(info_msg2+'\n\n成績入力完了にチェックが入っています。\n成績が全て未入力ですが、このまま更新してもよろしいですか？')) {
                    return false;
                }
            } else {
                alert(info_msg2+'\n\n成績入力完了にチェックが入っています。\n成績が全て未入力の場合、成績入力完了にチェックはできません。');
                return false;
            }
        }
        if (info_msg3 != "") {
            if (!confirm(info_msg3+'\n\n成績入力完了にチェックが入っています。\n成績に未入力がありますが、このまま更新してもよろしいですか？')) {
                return false;
            }
        }
        if (info_msg != "") {
            if (!confirm(info_msg+'\n\n成績入力完了にチェックが入っていません。\n成績が全て入力済みですが、このまま更新してもよろしいですか？')) {
                return false;
            }
        }
        /*****************************************************************/
        /*** 「成績入力完了チェックの入れ忘れ防止対策(素点)」 --終了-- ***/
        /*****************************************************************/
        clickedBtnUdpateCalc(true);
    }

    //更新ボタン・・・読み込み中は、更新・算出ボタンをグレー（押せないよう）にする。
    document.forms[0].btn_update.disabled = true;
    //フレームロック機能（プロパティの値が1の時有効）
    if (document.forms[0].useFrameLock.value == "1") {
        if (cmd == 'update') {
            updateFrameLock();
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//更新・算出時、サブミットする項目使用不可
function clickedBtnUdpateCalc(disFlg) {
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
}

//仮評定フラグALLチェックボックス
function check_all(obj) {
    for (var i=0; i < document.forms[0].elements.length; i++) {
        var e = document.forms[0].elements[i];
        var nam = e.name;
        if (e.type == 'checkbox' && nam.match(/PROV_FLG./) && !e.disabled) {
            e.checked = obj.checked;
        }
    }
}

function calc(obj) {
    //スペース削除
    var str_num = obj.value;
    obj.value = str_num.replace(/ |　/g,"");
    //欠席(*)・公欠(+)・忌引(-)はスルー
    var str = obj.value;
    var nam = obj.name;
    if (str == '*' || str == '+' || str == '-') {
        return;
    }
    //数字チェック
    if (isNaN(obj.value)){
        alert('{rval MSG907}');
        obj.value = obj.defaultValue;
        return;
    }
    //満点チェック
    var perfectName   = nam.split("-")[0] + "_PERFECT";
    var perfectNumber = nam.split("-")[1];
    perfectObject = eval("document.forms[0][\"" + perfectName + "-" + perfectNumber + "\"]");
    var perfect = parseInt(perfectObject.value);

    var score = parseInt(obj.value);
    if (score > perfect) {
        alert('{rval MSG914}'+'0点～'+perfect+'点以内で入力してください。');
        obj.value = obj.defaultValue;
        return;
    }

    var score = parseInt(obj.value);
    if (score < 0) {
        alert('{rval MSG914}'+'0点～'+perfect+'点以内で入力してください。');
        obj.value = obj.defaultValue;
        return;
    }

    if (document.forms[0].gen_ed.value != "" && nam.match(/GRAD_VALUE./)) {
        var n = nam.split('-');
        if (a_mark[obj.value] == undefined) {
            outputLAYER('mark'+n[1], '');
        } else {
            outputLAYER('mark'+n[1], a_mark[obj.value]);
        }
    }
}
function newwin(SERVLET_URL) {
    if (document.forms[0].SUBCLASSCD.value == '') {
        alert('科目を指定してください。');
        return;
    }
    if (document.forms[0].CHAIRCD.value == '') {
        alert('学級・講座を指定してください。');
        return;
    }
    document.forms[0].category_selected.value = document.forms[0].CHAIRCD.value;
    action = document.forms[0].action;
    target = document.forms[0].target;
//    document.forms[0].action = "/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJD";
    document.forms[0].target = "_blank";
    document.forms[0].submit();
    document.forms[0].action = action;
    document.forms[0].target = target;
}

//子画面へ
function openKogamen(URL) {
    if (document.forms[0].SUBCLASSCD.value == '') {
        alert('科目を指定してください。');
        return;
    }
    if (document.forms[0].CHAIRCD.value == '') {
        alert('学級・講座を指定してください。');
        return;
    }

    wopen(URL, 'SUBWIN2', 0, 0, screen.availWidth, screen.availHeight);
}

/************************************************* 貼付け関係 ***********************************************/
function showPaste(obj) {
    if (!confirm('内容を貼付けますか？')) {
        return false;
    }

    //テキストボックスの名前の配列を作る
    var textFieldName = document.forms[0].TEXT_FIELD_NAME.value;
    var nameArray = textFieldName.split(",");

    insertTsv({"clickedObj"      :obj,
               "harituke_type"   :"renban",
               "objectNameArray" :nameArray
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
        targetObject.style.background = '#ccffcc';
    }
    if (document.forms[0].gen_ed.value != "" && targetObject.name.match(/GRAD_VALUE/)) {
        if (a_mark[targetObject.value] == undefined){
            outputLAYER('mark' + targetNumber, '');
        } else {
            outputLAYER('mark' + targetNumber, a_mark[targetObject.value]);
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
    var targetName   = harituke_jouhou.clickedObj.name.split("-")[0];
    var targetNumber = harituke_jouhou.clickedObj.name.split("-")[1];
    var objectNameArray = harituke_jouhou.objectNameArray;

    for (j = 0; j < clipTextArray.length; j++) { //クリップボードの各行をループ
        i = 0;
        startFlg = false;
        for (k = 0; k < objectNameArray.length; k++) { //テキストボックス名でまわす
            if (objectNameArray[k] == targetName) { //貼付け開始対象のテキストボックスならばフラグを立てる
                startFlg = true;
            }
            if (startFlg) {
                if (clipTextArray[j][i] != undefined) {
                    //スペース削除
                    var str_num = new String(clipTextArray[j][i]);
                    clipTextArray[j][i] = str_num.replace(/ |　/g,"");

                    if (objectNameArray[k].match(/VALUE/)) {
                        if (clipTextArray[j][i] == "-" || clipTextArray[j][i] == "=") {
                            i++;
                            continue;
                        }
                    }

                    //数字であるのかチェック
                    if (isNaN(clipTextArray[j][i])){
                        alert('{rval MSG907}');
                        return false;
                    }

                    //満点チェック
                    perfectNumber = parseInt(targetNumber) + j;
                    perfectObject = eval("document.forms[0][\"" + objectNameArray[k] + "_PERFECT" + "-" + perfectNumber + "\"]");
                    if (perfectObject) {
                        perfect = parseInt(perfectObject.value);
                        valScore = parseInt(clipTextArray[j][i]);
                        if(clipTextArray[j][i] < 0 || clipTextArray[j][i] > perfect) {
                            alert('{rval MSG914}' + '0点～'+perfect+'点以内で入力してください。');
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
                    targetname = textFieldArray[(textFieldArray.length - 1)] + (cnt - 1);
                    targetObject = document.getElementById(targetname);
                    targetObject.focus();
                    return;
                }
                targetname = textFieldArray[(i - 1)] + cnt;
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
                targetname = textFieldArray[(i + 1)] + cnt;
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
                    targetname = textFieldArray[(i + 1)] + 0;
                    targetObject = document.getElementById(targetname);
                    targetObject.focus();
                    return;
                }
                for (var j = 1; j < lineCnt - cnt; j++) {
                    targetname = textFieldArray[i] + (cnt + j);
                    targetObject = document.getElementById(targetname);
                    if (null != targetObject && targetObject.getAttribute("type") != "hidden") {
                        break;
                    }
                }
                targetObject.focus();
                return;
            }
        }
    }
}
//◆算出ボタン
//①１・２学期評価点(100点)＝(中間＋期末)/2×割合(80%)＋出席点(10点)＋平常点(10点)
//②３学期評価点(100点)    ＝(期末)        ×割合(80%)＋出席点(10点)＋平常点(10点)
function btnCalc(testcdSaki, testcdMoto) {
    //行数
    var lineCnt = document.forms[0].COUNTER.value;
    if (lineCnt == 0) {
        alert('生徒が0件です。');
        return;
    }
    //算出元のテストコードの配列
    var testcdMotoArray = testcdMoto.split(",");
    //実行前メッセージ
    var isKeiai = document.forms[0].isKeiai.value;
    var isKasiwara = document.forms[0].isKasiwara.value;
    var preMsg = (isKasiwara == "1") ? "評価点" : "評価点および出席点";
    if (!confirm(preMsg + 'を再計算します。\n宜しいですか？')) {
        return false;
    }
    //算出
    var setCalcFlg = false;
    for (var i = 0; i < lineCnt; i++ ) {
        //転学・退学は、処理しない
        var grdDiv23Semester = "";
        grdDiv23SemesterObject = eval("document.forms[0][\"" + "GRD_DIV23_SEMESTER"  + "-" + i + "\"]");
        if (grdDiv23SemesterObject) {
            grdDiv23Semester = grdDiv23SemesterObject.value;
        }
        if (grdDiv23Semester != '') {
            continue;
        }
        var total = 0;
        var totalCnt = 0;
        var attend = 0;
        var attendFlg = "";
        var heijou = 0;
        var kanten = 0;
        var kantenFlg = "";
        var kkksCnt = 0;
        var bunbo3 = 0;
        for (var k = 0; k < testcdMotoArray.length; k++ ) {
            scoreName  = "SCORE" + testcdMotoArray[k];
            scoreObject  = eval("document.forms[0][\"" + scoreName  + "-" + i + "\"]");
            if (scoreObject) {
                if (scoreObject.value == '*' || scoreObject.value == '+' || scoreObject.value == '-') {
                    kkksCnt++;
                    scoreName2 = "SCORE_PASS" + testcdMotoArray[k];
                    scoreObject2 = eval("document.forms[0][\"" + scoreName2 + "-" + i + "\"]");
                    if (scoreObject2) {
                        if (!isNaN(scoreObject2.value) && scoreObject2.value != '') {
                            total += parseInt(scoreObject2.value);
                            totalCnt++;
                            kkksCnt--;//見込点ありの場合、欠試カウントしない
                        }
                    }
                        bunbo3++;
                } else if (testcdMotoArray[k].substr(-2) == '01') {
                        if (!isNaN(scoreObject.value) && scoreObject.value != '') {
                            total += parseInt(scoreObject.value);
                            totalCnt++;
                        }
                        bunbo3++;
                } else if (testcdMotoArray[k].substr(-2) == '02') {//平常点【敬】
                        if (!isNaN(scoreObject.value) && scoreObject.value != '') {
                            heijou = parseInt(scoreObject.value);
                        }
                } else if (testcdMotoArray[k].substr(-2) == '03') {//観点別評価点【柏】
                        if (!isNaN(scoreObject.value) && scoreObject.value != '') {
                            kanten = parseInt(scoreObject.value);
                        }
                        kantenFlg = "1";
                } else if (testcdMotoArray[k].substr(-2) == '07') {//出席点【敬】
                    //自動計算した値を取得
                    targetName = "ATTEND_SCORE" + testcdMotoArray[k].substr(0, 1);
                    targetObject = eval("document.forms[0][\"" + targetName + "-" + i + "\"]");
                    if (targetObject) {
                        if (!isNaN(targetObject.value) && targetObject.value != '') {
                            attend = parseInt(targetObject.value);
                        } else {
                        //TODO:算出前に出席点設定されているかチェックしたい・・・
                        }
                    }
                    attendFlg = "1";
                } else if (testcdMotoArray[k].substr(-2) == '08') {//中間・期末評価点【柏】
                        if (!isNaN(scoreObject.value) && scoreObject.value != '') {
                            total += parseInt(scoreObject.value);
                            totalCnt++;
                        }
                        bunbo3++;
                }
            }
        }//for(moto)

        //算出先へセット
        var setValue;
        //欠試またはブランクがある
        if (kkksCnt > 0 || totalCnt == 0 || totalCnt != bunbo3) {
            setValue = "";
        } else {
            if (attendFlg == '1') {
                var avg = parseFloat(total / totalCnt) * 80 / 100;
                setValue = Math.round(avg) + attend + heijou;
            } else if (kantenFlg == '1') {
                var avg = parseFloat(total / totalCnt) * 70 / 100;
                setValue = Math.round(avg) + kanten;
            } else {
                var avg = parseFloat(total / totalCnt);
                setValue = Math.round(avg);
            }
        }
        //テキストまたはhidden
        targetName = "SCORE" + testcdSaki;
        targetObject = eval("document.forms[0][\"" + targetName + "-" + i + "\"]");
        if (targetObject) {
            targetObject.value = setValue;
        }
        //自動計算の値を保持
        targetName = "SCORE_KEEP" + testcdSaki;
        targetObject = eval("document.forms[0][\"" + targetName + "-" + i + "\"]");
        if (targetObject) {
            targetObject.value = setValue;
        }

        if (attendFlg == '1') {
            //出席点も同時にセット
            //テキストまたはhidden
            targetName = "SCORE" + testcdSaki.substr(0, 5) + "07";
            targetObject = eval("document.forms[0][\"" + targetName + "-" + i + "\"]");
            if (targetObject) {
                targetObject.value = attend;
            }
            //ラベル
            targetName = "SCORE" + testcdSaki.substr(0, 5) + "07" + "_ATTEND_ID" + "-" + i;
            targetObject = document.getElementById(targetName);
            if (targetObject) {
                targetObject.innerHTML = attend;
            }
        }

        setCalcFlg = true;
    }//for(line)
    //実行後メッセージ
    if (setCalcFlg) {
        alert('算出しました。\n更新ボタンを押下しないとデータは保存されません。');
    }
}
//◆算出ボタン
//③学年成績(100点)        ＝(１学期評価点＋２学期評価点＋３学期評価点)/3
function btnCalc9(testcdSaki, testcdMoto) {
    //行数
    var lineCnt = document.forms[0].COUNTER.value;
    if (lineCnt == 0) {
        alert('生徒が0件です。');
        return;
    }
    //算出元のテストコードの配列
    var testcdMotoArray = testcdMoto.split(",");
    //実行前メッセージ
    var preMsg = "学年成績および学年評定";
    if (!confirm(preMsg + 'を再計算します。\n宜しいですか？')) {
        return false;
    }
    //評定マスタの配列
    var assessName = document.forms[0].ASSESS_LIST.value;
    var assessArray = assessName.split(",");
    //算出
    var setCalcFlg = false;
    for (var i = 0; i < lineCnt; i++ ) {
        //転学・退学は、処理しない
        var grdDiv23Semester = "";
        grdDiv23SemesterObject = eval("document.forms[0][\"" + "GRD_DIV23_SEMESTER"  + "-" + i + "\"]");
        if (grdDiv23SemesterObject) {
            grdDiv23Semester = grdDiv23SemesterObject.value;
        }
        if (grdDiv23Semester != '') {
            continue;
        }
        //編入生の成績は、いなかった学期の数を分母から外したい。
        //編入日から学期を取得する。（どの学期から）いつから編入したか判断したい・・・
        var entDiv5Semester = "";
        entDiv5SemesterObject = eval("document.forms[0][\"" + "ENT_DIV5_SEMESTER"  + "-" + i + "\"]");
        if (entDiv5SemesterObject) {
            entDiv5Semester = entDiv5SemesterObject.value;
        }
        var total = 0;
        var totalCnt = 0;
        var bunbo3 = 0;
        for (var k = 0; k < testcdMotoArray.length; k++ ) {
            scoreName  = "SCORE" + testcdMotoArray[k];
            scoreObject  = eval("document.forms[0][\"" + scoreName  + "-" + i + "\"]");
            if (scoreObject) {
                if (entDiv5Semester != '') {
                    if (testcdMotoArray[k].substr(0, 1) >= entDiv5Semester) {
                        if (!isNaN(scoreObject.value) && scoreObject.value != '') {
                            total += parseInt(scoreObject.value);
                            totalCnt++;
                        }
                        bunbo3++;
                    }
                } else {
                        if (!isNaN(scoreObject.value) && scoreObject.value != '') {
                            total += parseInt(scoreObject.value);
                            totalCnt++;
                        }
                        bunbo3++;
                }
            }
        }//for(moto)

        //算出先へセット
        var setValue;
        var setAssess;
        //欠試またはブランクがある
        if (totalCnt == 0 || totalCnt != bunbo3) {
            setValue = "";
            setAssess = "";
        } else {
            //学年成績
            var avg = parseFloat(total / totalCnt);
            setValue = Math.round(avg);
            setAssess = "";
            //学年評定　学年成績を５段階に換算
            for (var s = 0; s < assessArray.length; s++ ) {
                level   = assessArray[s].split("-")[0];
                low     = assessArray[s].split("-")[1];
                high    = assessArray[s].split("-")[2];
                if (low <= setValue && setValue <= high) {
                    setAssess = level;
                }
            }
        }
        //テキストまたはhidden
        targetName = "SCORE" + testcdSaki;
        targetObject = eval("document.forms[0][\"" + targetName + "-" + i + "\"]");
        if (targetObject) {
            targetObject.value = setValue;
        }
        //自動計算の値を保持
        targetName = "SCORE_KEEP" + testcdSaki;
        targetObject = eval("document.forms[0][\"" + targetName + "-" + i + "\"]");
        if (targetObject) {
            targetObject.value = setValue;
        }

        //学年評定も同時にセット　学年成績を５段階に換算
        //テキストまたはhidden
        targetName = "SCORE" + testcdSaki.substr(0, 5) + "09";
        targetObject = eval("document.forms[0][\"" + targetName + "-" + i + "\"]");
        if (targetObject) {
            targetObject.value = setAssess;
        }
        //ラベル
        targetName = "SCORE" + testcdSaki.substr(0, 5) + "09" + "_ASSESS_ID" + "-" + i;
        targetObject = document.getElementById(targetName);
        if (targetObject) {
            targetObject.innerHTML = setAssess;
        }

        setCalcFlg = true;
    }//for(line)
    //実行後メッセージ
    if (setCalcFlg) {
        alert('算出しました。\n更新ボタンを押下しないとデータは保存されません。');
    }
}
