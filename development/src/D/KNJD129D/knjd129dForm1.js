function btn_submit(cmd) {
    if (cmd == 'chaircd' && 0 < document.forms[0].COUNTER.value) {
        if (!confirm('保存されていないデータがあれば破棄されます。処理を続行しますか？')) {
            var cnt = document.forms[0]["LIST_CHAIRCD" + document.forms[0].SELECT_CHAIRCD.value].value;
            document.forms[0].CHAIRCD[cnt].selected = true;
            return false;
        }
    }
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
        clickedBtnUdpateCalc(true);
    } else if (cmd == 'horyuu') {
        for (var i = 0; i < document.forms[0].elements.length; i++ ) {
            var e = document.forms[0].elements[i];
            if (e.type == 'text' && e.value != '') {
                //スペース削除
                var str_num = e.value;
                e.value = str_num.replace(/ |　/g,"");
                var str = e.value;
                var nam = e.name;
                //欠課時数情報（-、=)
                if (nam.match(/.VALUE./)) {
                    if (str == '-' | str == '=') {
                        continue;
                    }
                }
                //数字チェック
                if (isNaN(e.value)) {
                    alert('{rval MSG907}');
                    return false;
                }
                //満点チェック
                var perfectName   = nam.split("-")[0] + "_PERFECT";
                var perfectNumber = nam.split("-")[1];
                perfectObject = eval("document.forms[0][\"" + perfectName + "-" + perfectNumber + "\"]");
                var perfect = parseInt(perfectObject.value);
                if (!isNaN(e.value) && (e.value > perfect || e.value < 0)) {
                    alert('{rval MSG901}' + '\n0～'+perfect+'まで入力可能です');
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
        for (var i = 0; i < 5; i++ ) {
            score_txt[i] = false;
            score_cnt[i] = true;
            score_not[i] = true;
            score_chk[i] = true;
        }
        // チェック
        for (var i = 0; i < document.forms[0].elements.length; i++ ) {
            var e = document.forms[0].elements[i];
            var nam = e.name;
            // 素点入力チェック
            if (e.type == 'text' && nam.match(/.SCORE./)) {
                if (nam.match(/SEM1_INTR_SCORE./)) {
                    score_txt[0] = true;
                    if (e.value == '') score_cnt[0] = false;
                    else               score_not[0] = false;
                }
                if (nam.match(/SEM1_TERM_SCORE./)) {
                    score_txt[1] = true;
                    if (e.value == '') score_cnt[1] = false;
                    else               score_not[1] = false;
                }
                if (nam.match(/SEM2_INTR_SCORE./)) {
                    score_txt[2] = true;
                    if (e.value == '') score_cnt[2] = false;
                    else               score_not[2] = false;
                }
                if (nam.match(/SEM2_TERM_SCORE./)) {
                    score_txt[3] = true;
                    if (e.value == '') score_cnt[3] = false;
                    else               score_not[3] = false;
                }
                if (nam.match(/SEM3_TERM_SCORE./)) {
                    score_txt[4] = true;
                    if (e.value == '') score_cnt[4] = false;
                    else               score_not[4] = false;
                }
            }
            // 成績入力完了チェック
            if (e.type == 'checkbox' && nam.match(/CHK_COMP./)) {
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
        score_msg[0] = "（"+semename1+testname1+"）";
        score_msg[1] = "（"+semename1+testname2+"）";
        score_msg[2] = "（"+semename2+testname3+"）";
        score_msg[3] = "（"+semename2+testname4+"）";
        score_msg[4] = "（"+semename3+testname5+"）";
        var info_msg = "";
        var info_msg2 = "";
        var info_msg3 = "";
        for (var i = 0; i < 5; i++ ) {
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
        /*****************************************************************/
        // 更新時、下記の処理をする
        // ①：「成績が全て未入力」で「成績入力完了チェックあり」の場合、エラーメッセージを表示する
        // ②：「成績が全て入力済」で「成績入力完了チェックなし」の場合、確認メッセージを表示する
        var value_txt = new Array(); // テキスト入力フラグ
        var value_cnt = new Array(); // 素点入力フラグ
        var value_not = new Array(); // 素点未入力フラグ
        var value_chk = new Array(); // 成績入力完了フラグ
        var value_dis = new Array(); // 成績入力完了フラグ
        // 初期化
        for (var i = 0; i < 4; i++ ) {
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
                if (nam.match(/GRAD_SCORE./)) {
                    value_txt[2] = true;
                    if (e.value == '') value_cnt[2] = false;
                    else               value_not[2] = false;
                }
                if (nam.match(/GRAD_VALUE./)) {
                    value_txt[3] = true;
                    if (e.value == '') value_cnt[3] = false;
                    else               value_not[3] = false;
                }
            }
            // 成績入力完了チェック
            if (e.type == 'checkbox' && nam.match(/CHK_COMP_VALUE./)) {
                if (nam.match(/CHK_COMP_VALUE1/)) {value_chk[0] = e.checked; value_dis[0] = e.disabled;}
                if (nam.match(/CHK_COMP_VALUE2/)) {value_chk[1] = e.checked; value_dis[1] = e.disabled;}
                if (nam.match(/CHK_COMP_VALUE3/)) {value_chk[2] = e.checked; value_dis[2] = e.disabled;}
                if (nam.match(/CHK_COMP_VALUE4/)) {value_chk[3] = e.checked; value_dis[3] = e.disabled;}
            }
        }
        // 成績入力完了チェックの入れ忘れメッセージ
        var value_msg = new Array();
        var semename1 = document.forms[0].SEMENAME1.value;
        var semename2 = document.forms[0].SEMENAME2.value;

        value_msg[0] = "（"+semename1+"評価）";
        value_msg[1] = "（"+semename2+"評価）";
        value_msg[2] = "（学年評価）";
        value_msg[3] = "（学年評定）";
        for (var i = 0; i < 4; i++ ) {
            // ①の場合
            if (value_txt[i] && value_cnt[i] && !value_chk[i]) info_msg = info_msg + value_msg[i];
            // ②の場合
            if (value_txt[i] && value_not[i] && value_chk[i]) info_msg2 = info_msg2 + value_msg[i];
            // ③の場合
            if (value_txt[i] && !value_cnt[i] && !value_not[i] && value_chk[i]) info_msg3 = info_msg3 + value_msg[i];
        }
        if (info_msg2 != "") {
            alert(info_msg2+'\n\n成績入力完了にチェックが入っています。\n成績が全て未入力の場合、成績入力完了にチェックはできません。');
            return false;
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
        /*** 「成績入力完了チェックの入れ忘れ防止対策(評価)」 --終了-- ***/
        /*****************************************************************/
    }

    //更新・算出ボタン・・・読み込み中は、更新・算出ボタンをグレー（押せないよう）にする。
    document.forms[0].btn_update.disabled = true;
    //見込点/参考点/備考ボタン
    if (document.forms[0].disBtnName.value !== '') {
        var disBtnList = document.forms[0].disBtnName.value.split(",");
        for (var k = 0; k < disBtnList.length; k++ ) { //テキストボックス名でまわす
            disBtnObject = eval("document.forms[0][\"" + disBtnList[k] + "\"]");
            disBtnObject.disabled = true;
        }
    }
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
    //'*'欠席はスルー
    var kessekiMarkFlg = document.forms[0].z010name1.value == "nichi-ni";
    var str = obj.value;
    var nam = obj.name;
    if (str == '*') {
        return;
    }
    if (kessekiMarkFlg && str == '**') {
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
                    if (clipTextArray[j][i] != '*' && isNaN(clipTextArray[j][i])){
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
    if (!confirm('{rval MSG108}')){
        return;
    }

    wopen(URL, 'SUBWIN2', 0, 0, screen.availWidth, screen.availHeight);
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
//◆年５回考査実施・・・前期（中間、期末、平常点）後期（中間１、中間２、期末、平常点）
//②前期得点100点＝(中間＋期末)/2×割合(70%)＋平常点(30点)
//②後期得点100点＝(中間１＋中間２＋期末)/3×割合(70%)＋平常点(30点)
//③学年成績200点＝前期得点＋後期得点
//◆技術・家庭は、割合が50%
//②前期得点100点＝(中間＋期末)/2×割合(50%)＋平常点(50点)
//②後期得点100点＝(中間１＋中間２＋期末)/3×割合(50%)＋平常点(50点)
//③学年成績200点＝前期得点＋後期得点
function btnCalc(testcdSaki, testcdMoto) {
    //行数
    var lineCnt = document.forms[0].COUNTER.value;
    if (lineCnt == 0) {
        alert('生徒が0件です。');
        return;
    }
    //算出元のテストコードの配列
    var testcdMotoArray = testcdMoto.split(",");
    //分母
    var bunbo = 0;
    for (var k = 0; k < testcdMotoArray.length; k++ ) {
        for (var i = 0; i < lineCnt; i++ ) {
            scoreName = "SCORE" + testcdMotoArray[k];
            scoreObject = eval("document.forms[0][\"" + scoreName + "-" + i + "\"]");
            //算出元はどこまで入力されているかで分母を決める
            if (!isNaN(scoreObject.value) && scoreObject.value != '' || scoreObject.value == "*") {
                //前期平常点または後期平常点
                if (testcdMotoArray[k] == '1990002' || testcdMotoArray[k] == '2990002') {
                } else {
                    bunbo = k + 1;
                }
            }
        }
    }
    //分母0・・・javascriptエラー回避
    if (bunbo == 0) {
        return;
    }
    //実行前メッセージ
    if (!confirm('得点をすべて再計算します。\nよろしいですか？')) {
        return false;
    }
    //算出
    var setCalcFlg = false;
    //評価の算出方法(1:四捨五入 2:切り上げ 3:切り捨て)
    var CalcMethod = document.forms[0].CalcMethod.value;
    for (var i = 0; i < lineCnt; i++ ) {
        var total = 0;
        var cnt = 0;
        var heijou = 0; //平常点
        for (var k = 0; k < testcdMotoArray.length; k++ ) {
            scoreName = "SCORE" + testcdMotoArray[k];
            scoreObject = eval("document.forms[0][\"" + scoreName + "-" + i + "\"]");
            if (!isNaN(scoreObject.value) && scoreObject.value != '') {
                //前期平常点または後期平常点
                if (testcdMotoArray[k] == '1990002' || testcdMotoArray[k] == '2990002') {
                    heijou += parseInt(scoreObject.value);
                } else {
                    total += parseInt(scoreObject.value);
                    cnt++;
                }
            } else if (document.forms[0].useMikomiFlg.value == '1' && scoreObject.value == "*") {
                //欠席者で見込点があれば算出対象に見込点を含める
                scorePassObject = eval("document.forms[0][\"" + scoreName + "_PASS" + "-" + i + "\"]");
                if (scorePassObject) {
                    if (!isNaN(scorePassObject.value) && scorePassObject.value != '') {
                        //前期平常点または後期平常点
                        if (testcdMotoArray[k] == '1990002' || testcdMotoArray[k] == '2990002') {
                            heijou += parseInt(scorePassObject.value);
                        } else {
                            total += parseInt(scorePassObject.value);
                            cnt++;
                        }
                    }
                }
            }
        }
        //算出先へセット
        var setValue;
        var setName = "SCORE" + testcdSaki;
        //欠試またはブランクがある
        if (cnt != bunbo) {
            setValue = "";
        //前期得点または後期得点
        } else if (testcdSaki == '1990008' || testcdSaki == '2990008') {
            //割合 初期値:70%
            percentObject = eval("document.forms[0][\"" + "PERCENT" + "-" + i + "\"]");
            percent = parseInt(percentObject.value);
            //算出
            var avg = parseFloat(total / bunbo) * percent / 100;
            //評価の算出方法(1:四捨五入 2:切り上げ 3:切り捨て)
            if (CalcMethod == '1') { //初期値
                setValue = Math.round(avg) + heijou;
            } else if (CalcMethod == '2') {
                setValue = Math.ceil(avg) + heijou;
            } else if (CalcMethod == '3') {
                setValue = Math.floor(avg) + heijou;
            }
        //学年成績＝前期得点＋後期得点
        } else {
            setValue = total;
        }
        //テキストまたはhidden
        totalObject = eval("document.forms[0][\"" + setName + "-" + i + "\"]");
        totalObject.value = setValue;
        setCalcFlg = true;
    }
    //実行後メッセージ
    if (setCalcFlg) {
        alert('算出しました。\n更新ボタンを押下しないとデータは保存されません。');
    }
}

//◆算出ボタン（茗溪学園中学校から要望）
//平均を算出する
//算出先＝算出元合計/算出元数
function btnCalcAvg(testcdSaki, testcdMoto) {
    //行数
    var lineCnt = document.forms[0].COUNTER.value;
    if (lineCnt == 0) {
        alert('生徒が0件です。');
        return;
    }
    //算出元のテストコードの配列
    var testcdMotoArray = testcdMoto.split(",");
    //分母
    var bunbo = 0;
    for (var k = 0; k < testcdMotoArray.length; k++ ) {
        for (var i = 0; i < lineCnt; i++ ) {
            scoreName = "SCORE" + testcdMotoArray[k];
            scoreObject = eval("document.forms[0][\"" + scoreName + "-" + i + "\"]");
            //算出元はどこまで入力されているかで分母を決める
            if (!isNaN(scoreObject.value) && scoreObject.value != '' || scoreObject.value == "*") {
                bunbo = k + 1;
            }
        }
    }
    //分母0・・・javascriptエラー回避
    if (bunbo == 0) {
        return;
    }
    //実行前メッセージ
    if (!confirm('得点をすべて再計算します。\nよろしいですか？')) {
        return false;
    }
    //算出
    var setCalcFlg = false;
    //評価の算出方法(1:四捨五入 2:切り上げ 3:切り捨て)
    var CalcMethod = document.forms[0].CalcMethod.value;
    for (var i = 0; i < lineCnt; i++ ) {
        var total = 0;
        var cnt = 0;
        for (var k = 0; k < testcdMotoArray.length; k++ ) {
            scoreName = "SCORE" + testcdMotoArray[k];
            scoreObject = eval("document.forms[0][\"" + scoreName + "-" + i + "\"]");
            if (!isNaN(scoreObject.value) && scoreObject.value != '') {
                total += parseInt(scoreObject.value);
                cnt++;
            } else if (document.forms[0].useMikomiFlg.value == '1' && scoreObject.value == "*") {
                //欠席者で見込点があれば算出対象に見込点を含める
                scorePassObject = eval("document.forms[0][\"" + scoreName + "_PASS" + "-" + i + "\"]");
                if (scorePassObject) {
                    if (!isNaN(scorePassObject.value) && scorePassObject.value != '') {
                        total += parseInt(scorePassObject.value);
                        cnt++;
                    }
                }
            }
        }
        if (cnt == 0) {
            continue;
        }
        //算出先へセット
        var setValue;
        var setName = "SCORE" + testcdSaki;
        //欠試またはブランクがある
        if (cnt != bunbo && "1" != document.forms[0].ignoreBlankKesshi.value) {
            setValue = "";
        //前期得点または後期得点
        } else {
            //算出
            var avg = parseFloat(total / cnt);
            //評価の算出方法(1:四捨五入 2:切り上げ 3:切り捨て)
            if (CalcMethod == '1') { //初期値
                setValue = Math.round(avg);
            } else if (CalcMethod == '2') {
                setValue = Math.ceil(avg);
            } else if (CalcMethod == '3') {
                setValue = Math.floor(avg);
            }
        }
        //テキストまたはhidden
        totalObject = eval("document.forms[0][\"" + setName + "-" + i + "\"]");
        totalObject.value = setValue;
        setCalcFlg = true;
        //ラベル
        targetName = setName + "_TESTCDSAKI_ID" + "-" + i;
        targetObject = document.getElementById(targetName);
        if (targetObject) {
            targetObject.innerHTML = setValue;
        }
    }
    //実行後メッセージ
    if (setCalcFlg) {
        alert('算出しました。\n更新ボタンを押下しないとデータは保存されません。');
    }
}

//◆算出ボタン（専修大松戸用）
//学年評価の算出を追加・・・各学期評価の平均
//100段階の学期評価を自動算出する
//計算式内の掛け率は、単位マスタメンテで設定した値を使用する
//◆管理者コントロールの算出設定画面で下記の通り設定されている前提
//算出先・・・学期評価
//算出元・・・中間素点、期末素点、平常点
//◆各値・・・RECORD_SCORE_DAT（見込点ではなく追指導を参照する）
//(testkind=01,scorediv=01)中間素点
//(testkind=01,scorediv=01)中間追指導
//(testkind=02,scorediv=01)期末素点
//(testkind=02,scorediv=01)期末追指導
//(testkind=99,scorediv=02)平常点
//(testkind=99,scorediv=08)学期評価
function btnCalcMatsudo(testcdSaki, testcdMoto) {
    //行数
    var lineCnt = document.forms[0].COUNTER.value;
    if (lineCnt == 0) {
        alert('生徒が0件です。');
        return;
    }
    //算出元のテストコードの配列
    var testcdMotoArray = testcdMoto.split(",");
    //実行前メッセージ
    calcMsg = (testcdSaki == '9990008') ? "学年評価" : "学期評価";
    if (!confirm(calcMsg + 'を算出します。\nよろしいですか？')) {
        return false;
    }
    //算出
    var setCalcFlg = false;
    for (var i = 0; i < lineCnt; i++) {
        var total = 0;
        var totalCnt = 0;
        var heijou = 0;
        var heijouCnt = 0;
        for (var k = 0; k < testcdMotoArray.length; k++) {
            semester = testcdMotoArray[k].substring(0, 1);
            testkind = testcdMotoArray[k].substring(1, 3);
            testitem = testcdMotoArray[k].substring(3, 5);
            scorediv = testcdMotoArray[k].substring(5, 7);
            scoreName = "SCORE" + testcdMotoArray[k];
            scoreObject = eval("document.forms[0][\"" + scoreName + "-" + i + "\"]");
            sidouObject = eval("document.forms[0][\"" + scoreName + "_SIDOU_SCORE" + "-" + i + "\"]");
            score = (scoreObject) ? scoreObject.value : '';
            sidou = (sidouObject) ? sidouObject.value : '';
            if (testcdSaki != '9990008' && (testkind == '01' && scorediv == '01' || testkind == '02' && scorediv == '01')) {//中間or期末
                //追指導を優先する
                if (!isNaN(sidou) && sidou != '') {//追指導
                    total += parseInt(sidou);
                    totalCnt++;
                } else if (!isNaN(score) && score != '') {//素点
                    total += parseInt(score);
                    totalCnt++;
                }
            } else if (testcdSaki != '9990008' && testkind == '99' && scorediv == '02') {//平常点
                if (!isNaN(score) && score != '') {
                    heijou = parseInt(score);
                    heijouCnt++;
                }
            } else if (testcdSaki == '9990008' && testkind == '99' && scorediv == '08') {//学期評価
                if (!isNaN(score) && score != '') {
                    total += parseInt(score);
                    totalCnt++;
                }
            }
        }
        //掛け率
        rateObject = eval("document.forms[0][\"" + "RATE" + "-" + i + "\"]");
        rate = (rateObject) ? rateObject.value : '';
        //学期評価算出
        if (testcdSaki != '9990008' && totalCnt > 0 && heijouCnt > 0 && rate != '') {
            var avg = parseFloat(total / totalCnt) * parseFloat(rate);
            var setValue = Math.round(avg) + heijou;
            //算出先へセット
            var setName = "SCORE" + testcdSaki;
            //テキストまたはhidden
            setObject = eval("document.forms[0][\"" + setName + "-" + i + "\"]");
            if (setObject) {
                setObject.value = setValue;
                setCalcFlg = true;
            }
            //ラベル
            targetName = setName + "_TESTCDSAKI_ID" + "-" + i;
            targetObject = document.getElementById(targetName);
            if (targetObject) {
                targetObject.innerHTML = setValue;
            }
        //学年評価算出
        } else if (testcdSaki == '9990008' && totalCnt > 0) {
            var avg = parseFloat(total / totalCnt);
            var setValue = Math.round(avg);
            //算出先へセット
            var setName = "SCORE" + testcdSaki;
            //テキストまたはhidden
            setObject = eval("document.forms[0][\"" + setName + "-" + i + "\"]");
            if (setObject) {
                setObject.value = setValue;
                setCalcFlg = true;
            }
            //ラベル
            targetName = setName + "_TESTCDSAKI_ID" + "-" + i;
            targetObject = document.getElementById(targetName);
            if (targetObject) {
                targetObject.innerHTML = setValue;
            }
        }
    }
    //実行後メッセージ
    if (setCalcFlg) {
        alert('算出しました。\n更新ボタンを押下しないとデータは保存されません。');
    } else {
        alert('算出データはありません。');
    }
}
