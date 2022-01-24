function btn_submit(cmd) {
    document.forms[0].CHK_FLG.value = 'reset';
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
        for (var i = 0; i < document.forms[0].elements.length; i++ ) {
            var e = document.forms[0].elements[i];
            if (e.type == 'text' && e.value != '') {
                //スペース削除
                var str_num = e.value;
                e.value = str_num.replace(/ |　/g,"");
                var str = e.value;
                var nam = e.name;
                //欠課時数情報（-、=、*)
                if (nam.match(/.VALUE./) || nam.match(/.SCORE./)) {
                    if (str == '*') {
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
        for (var i = 0; i < 5; i++ ) {
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
                if (nam.match(/SEM3_VALUE./)) {
                    value_txt[2] = true;
                    if (e.value == '') value_cnt[2] = false;
                    else               value_not[2] = false;
                }
                if (nam.match(/GRAD_SCORE./)) {
                    value_txt[3] = true;
                    if (e.value == '') value_cnt[3] = false;
                    else               value_not[3] = false;
                }
                if (nam.match(/GRAD_VALUE./)) {
                    value_txt[4] = true;
                    if (e.value == '') value_cnt[4] = false;
                    else               value_not[4] = false;
                }
            }
            // 成績入力完了チェック
            if (e.type == 'checkbox' && nam.match(/CHK_COMP_VALUE./)) {
                if (nam.match(/CHK_COMP_VALUE1/))  {value_chk[0] = e.checked; value_dis[0] = e.disabled;}
                if (nam.match(/CHK_COMP_VALUE2/))  {value_chk[1] = e.checked; value_dis[1] = e.disabled;}
                if (nam.match(/CHK_COMP_VALUE3/))  {value_chk[2] = e.checked; value_dis[2] = e.disabled;}
                if (nam.match(/CHK_COMP_VALUE4/))  {value_chk[3] = e.checked; value_dis[3] = e.disabled;}
                if (nam.match(/CHK_COMP_VALUE5/))  {value_chk[4] = e.checked; value_dis[4] = e.disabled;}
            }
        }
        // 成績入力完了チェックの入れ忘れメッセージ
        var value_msg = new Array();
        var semename1 = document.forms[0].SEMENAME1.value;
        var semename2 = document.forms[0].SEMENAME2.value;
        var semename3 = document.forms[0].SEMENAME3.value;

        value_msg[0] = "（"+semename1+"評価）";
        value_msg[1] = "（"+semename2+"評価）";
        value_msg[2] = "（"+semename3+"評価）";
        value_msg[3] = "（学年評価）";
        value_msg[4] = "（学年評定）";
        for (var i = 0; i < 5; i++ ) {
            // ①の場合
            if (value_txt[i] && value_cnt[i] && !value_chk[i]) info_msg = info_msg + value_msg[i];
            // ②の場合
            if (value_txt[i] && value_not[i] && value_chk[i]) info_msg2 = info_msg2 + value_msg[i];
            // ③の場合
            if (value_txt[i] && !value_cnt[i] && !value_not[i] && value_chk[i]) info_msg3 = info_msg3 + value_msg[i];
        }
        /*****************************************************************/
        /*** 「成績入力完了チェックの入れ忘れ防止対策(評価)」 --終了-- ***/
        /*****************************************************************/
        if (info_msg2 != "") {
            alert(info_msg2+'\n\n成績入力完了にチェックが入っています。\n成績が全て未入力の場合、成績入力完了にチェックはできません。');
            return false;
        }
        if (info_msg3 != "") {
            if (!confirm(info_msg3+'\n\n成績入力完了にチェックが入っています。\n成績に未入力がありますが、このまま更新してもよろしいですか？'))
                return false;
        }
        if (info_msg != "") {
            if (!confirm(info_msg+'\n\n成績入力完了にチェックが入っていません。\n成績が全て入力済みですが、このまま更新してもよろしいですか？'))
                return false;
        }
        clickedBtnUdpate(true);
    }

    //更新ボタン・・・読み込み中は、更新ボタンをグレー（押せないよう）にする。
    document.forms[0].btn_update.disabled = true;
    document.forms[0].btn_calc.disabled = true;
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
    obj.value = str_num.replace(/ |　/g,"");
    var str = obj.value;
    var nam = obj.name;
    if (nam.match(/.VALUE./) || nam.match(/.SCORE./)) {
        if (str == '*') {
            return;
        }
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
    var nameArray = new Array("SEM1_INTR_SCORE",
                              "SEM1_TERM_SCORE",
                              "SEM1_VALUE",
                              "SEM2_INTR_SCORE",
                              "SEM2_TERM_SCORE",
                              "SEM2_VALUE",
                              "SEM3_TERM_SCORE",
                              "SEM3_VALUE",
                              "GRAD_SCORE",
                              "GRAD_VALUE",
                              "COMP_CREDIT",
                              "GET_CREDIT");

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

                    if (objectNameArray[k].match(/VALUE/) || objectNameArray[k].match(/SCORE/)) {
                        if (clipTextArray[j][i] == "*") {
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
//学年評価算出
function btnCalc() {
    //行数
    var lineCnt = document.forms[0].COUNT.value;
    //各学期評価
    var nameArray = new Array("SEM1_VALUE",
                              "SEM2_VALUE",
                              "SEM3_VALUE");
    if (lineCnt == 0) {
        alert('生徒が0件です。');
        return;
    }
    //分母
    var bunbo = 0;
    for (var k = 0; k < nameArray.length; k++ ) {
        for (var i = 0; i < lineCnt; i++ ) {
            scoreObject = eval("document.forms[0][\"" + nameArray[k] + "-" + i + "\"]");
            //各学期評価はどこまで入力されているかで分母を決める
            if (!isNaN(scoreObject.value) && scoreObject.value != '' || scoreObject.value == "*") {
                bunbo = k + 1;
            }
        }
    }
    if (bunbo == 0) {
        alert('各学期評価が未入力です。');
        return;
    }
    var msg = "";
    var seq = "";
    for (var i = 0; i < bunbo; i++ ) {
        msg += seq + (i+1) + "学期評価";
        seq = "、";
    }
    if (1 < bunbo) {
        msg += "の平均点";
    }
    if (!confirm('学年評価を算出します。\n（' + msg + '）\nよろしいですか？')) {
        return false;
    }
    //学年評価算出
    for (var i = 0; i < lineCnt; i++ ) {
        var total = 0;
        var cnt = 0;
        var blankFlg = false;
        for (var k = 0; k < nameArray.length; k++ ) {
            scoreObject = eval("document.forms[0][\"" + nameArray[k] + "-" + i + "\"]");
            if (!isNaN(scoreObject.value) && scoreObject.value != '') {
                total += parseInt(scoreObject.value);
                cnt++;
            }
        }
        totalObject = eval("document.forms[0][\"" + "GRAD_SCORE" + "-" + i + "\"]");
        if (cnt != bunbo) {
            totalObject.value = "";
        } else {
            var avg = parseFloat(total / bunbo);
            totalObject.value = Math.round(avg);
        }
        totalObjectKeep = eval("document.forms[0][\"" + "GRAD_SCORE_KEEP" + "-" + i + "\"]");
        totalObjectKeep.value = totalObject.value;
    }
    alert('算出しました。\n更新ボタンを押下しないとデータは保存されません。');
}
// テキストボックスをEnterキーで移動するメソッド（↓縦、→横）
function keyChangeEntToTab2(obj) {
    //テキストボックスの名前の配列
    var textFieldName = document.forms[0].TEXT_FIELD_NAME.value;
    var textFieldArray = textFieldName.split(",");
    //現在カーソルがあるテキストボックスの名前と行No
    var nam = obj.name;
    var startItem = nam.split("-")[0];
    var startNumberStudent = nam.split("-")[1];
    var cnt = parseInt(startNumberStudent);
    //行数
    var lineCnt = document.forms[0].COUNT.value;
    //最終行の生徒
    var isLastStudent = cnt == lineCnt - 1 ? true : false;
    // Ent13 Tab9 ←37 ↑38 →39 ↓40
    var e = window.event;
    //方向キー
    if (e.keyCode != 13) {
        return;
    }
    var moveEnt = document.forms[0].MOVE_ENTER[0].checked ? 40 : 39;
    for (var i = 0; i < textFieldArray.length; i++) {
        if (textFieldArray[i] == startItem) {
            var isLastItem = i == textFieldArray.length - 1 ? true : false;
            //→横
            if (moveEnt == 39) {
                if (isLastItem && isLastStudent) {
                    obj.focus();
                    return;
                }
                var nextNumberStudent = isLastItem ? cnt + 1 : cnt;
                for (var st = nextNumberStudent; st < lineCnt; st++) {
                    var nextNumberItem = isLastItem || st > nextNumberStudent ? 0 : i + 1;
                    for (var it = nextNumberItem; it < textFieldArray.length; it++) {
                        targetname = textFieldArray[it] + '_ID' + '-' + st;
                        targetObject = document.getElementById(targetname);
                        if (targetObject) {
                            targetObject.focus();
                            return;
                        }
                    }
                }
                obj.focus();
                return;
            }
            //↓縦
            if (moveEnt == 40) {
                if (isLastItem && isLastStudent) {
                    obj.focus();
                    return;
                }
                var nextNumberItem = isLastStudent ? i + 1 : i;
                for (var it = nextNumberItem; it < textFieldArray.length; it++) {
                    var nextNumberStudent = isLastStudent || it > nextNumberItem ? 0 : cnt + 1;
                    for (var st = nextNumberStudent; st < lineCnt; st++) {
                        targetname = textFieldArray[it] + '_ID' + '-' + st;
                        targetObject = document.getElementById(targetname);
                        if (targetObject) {
                            targetObject.focus();
                            return;
                        }
                    }
                }
                obj.focus();
                return;
            }
        }
    }
}
