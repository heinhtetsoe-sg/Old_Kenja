function btn_submit(cmd) {

    document.forms[0].CHK_FLG.value = 'reset';

    if (cmd == 'reset'){
        if (!confirm('{rval MSG106}'))
            return false;
    } else if (cmd == 'update'){
        //更新権限チェック
        userAuth = document.forms[0].USER_AUTH.value;
        updateAuth = document.forms[0].UPDATE_AUTH.value;
        if (userAuth < updateAuth){
            alert('{rval MSG300}');
            return false;
        }

        var maxScore2 = document.forms[0].MAXSCORE2.value;

        for (var i = 0; i < document.forms[0].elements.length; i++ ) {
            var e = document.forms[0].elements[i];
            if (e.type == 'text' && e.value != '') {
                //スペース削除
                var str_num = e.value;
                e.value = str_num.replace(/ |　/g,"");
                //数字チェック
                if (isNaN(e.value)) {
                    alert('{rval MSG901}' + '\n値：' + e.value + 'は 数値ではありません');
                    return false;
                }                
                //値(範囲)チェック
                var nam = e.name;
                var score = parseInt(e.value);
                if (nam.match(/.SCORE2./)) {
                    if (score < 0 || score > maxScore2) {
                        alert('{rval MSG914}'+'0点～'+maxScore2+'点以内で入力してください。値：' + e.value);
                        return false;
                    }
                } else if (nam.match(/.SCORE3./)) {
                    if (score < 0 || score > 10) {
                        alert('{rval MSG914}'+'0点～10点以内で入力してください。値：' + e.value);
                        return false;
                    }
                } else {
                    //満点チェック
                    var perfectName   = nam.split("-")[0] + "_PERFECT";
                    var perfectNumber = nam.split("-")[1];
                    perfectObject = eval("document.forms[0][\"" + perfectName + "-" + perfectNumber + "\"]");
                    var perfect = parseInt(perfectObject.value);
                    if (!isNaN(e.value) && (e.value > perfect || e.value < 0)) {
                        alert('{rval MSG914}'+'0点～'+perfect+'点以内で入力してください。');
                        return false;
                    }
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
        for (var i = 0; i < 4; i++ ) {
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
            if (e.type == 'text' && nam.match(/.SCORE1./)) {
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
            }
            // 成績入力完了チェック
            if (e.type == 'checkbox' && nam.match(/CHK_COMP./)) {
                if (nam.match(/CHK_COMP1/)) score_chk[0] = e.checked;
                if (nam.match(/CHK_COMP2/)) score_chk[1] = e.checked;
                if (nam.match(/CHK_COMP3/)) score_chk[2] = e.checked;
                if (nam.match(/CHK_COMP4/)) score_chk[3] = e.checked;
            }
        }
        // 成績入力完了チェックの入れ忘れメッセージ
        var score_msg = new Array();
        score_msg[0] = "（前期１学期期末素点）";
        score_msg[1] = "（前期２学期期末素点）";
        score_msg[2] = "（後期３学期期末素点）";
        score_msg[3] = "（後期４学期期末素点）";
        var info_msg = "";
        var info_msg2 = "";
        var info_msg3 = "";
        for (var i = 0; i < 4; i++ ) {
            // ①の場合
            if (score_txt[i] && score_cnt[i] && !score_chk[i]) info_msg = info_msg + score_msg[i];
            // ②の場合
            if (score_txt[i] && score_not[i] && score_chk[i]) info_msg2 = info_msg2 + score_msg[i];
            // ③の場合
            if (score_txt[i] && !score_cnt[i] && !score_not[i] && score_chk[i]) info_msg3 = info_msg3 + score_msg[i];
        }
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
        /*****************************************************************/
        /*** 「成績入力完了チェックの入れ忘れ防止対策(共通)」 --終了-- ***/
        /*****************************************************************/
    }

    //更新ボタン・・・読み込み中は、更新ボタンをグレー（押せないよう）にする。
    document.forms[0].btn_update.disabled = true;

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function calc(obj){
    //スペース削除
    var str_num = obj.value;
    obj.value = str_num.replace(/ |　/g,"");

    var str = obj.value;
    var nam = obj.name;
    
    //数字チェック
    if (isNaN(obj.value)) {
        alert('{rval MSG907}');
        obj.value = obj.defaultValue;
        return;
    }

    var score = parseInt(obj.value);
    if (nam.match(/.SCORE2./)) {
        var maxScore2 = document.forms[0].MAXSCORE2.value;
        if (score < 0 || score > maxScore2) {
            alert('{rval MSG914}'+'0点～'+maxScore2+'点以内で入力してください。');
            obj.value = obj.defaultValue;
            return;
        }
    } else if (nam.match(/.SCORE3./)) {
        if (score < 0 || score > 10) {
            alert('{rval MSG914}'+'0点～10点以内で入力してください。');
            obj.value = obj.defaultValue;
            return;
        }
    } else {
        //満点チェック
        var perfectName   = nam.split("-")[0] + "_PERFECT";
        var perfectNumber = nam.split("-")[1];
        perfectObject = eval("document.forms[0][\"" + perfectName + "-" + perfectNumber + "\"]");
        var perfect = parseInt(perfectObject.value);
        var score = parseInt(obj.value);
        if (score > perfect || score < 0) {
            alert('{rval MSG914}'+'0点～'+perfect+'点以内で入力してください。');
            obj.value = obj.defaultValue;
            return;
        }
    }
    for (i = 0; i < str.length; i++) {
        ch = str.substring(i, i+1);
        if (ch == ".") {
            alert('{rval MSG901}'+'「整数」を入力してください。');
            obj.value = obj.defaultValue;
            return;
        }
    }
}

function newwin(SERVLET_URL){

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
function show(obj) {
    if (!confirm('内容を貼付けますか？')) {
        return false;
    }

    //テキストボックスの名前の配列を作る
    var nameArray = new Array("SEM1_INTR_SCORE1",
                              "SEM1_INTR_SCORE2",
                              "SEM1_INTR_SCORE3",
                              "SEM1_TERM_SCORE1",
                              "SEM1_TERM_SCORE2",
                              "SEM1_TERM_SCORE3",
                              "SEM1_SCORE",
                              "SEM1_VALUE",
                              "SEM2_INTR_SCORE1",
                              "SEM2_INTR_SCORE2",
                              "SEM2_INTR_SCORE3",
                              "SEM2_TERM_SCORE1",
                              "SEM2_TERM_SCORE2",
                              "SEM2_TERM_SCORE3",
                              "SEM2_SCORE",
                              "SEM2_VALUE",
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
    if (document.forms[0].gen_ed.value != "" && targetObject.name.match(/GRAD_VALUE/)) {
        if (a_mark[targetObject.value] == undefined){
            outputLAYER('mark' + targetNumber, '');
        } else {
            outputLAYER('mark' + targetNumber, a_mark[targetObject.value]);
        }
    }
    targetObject.value = val;
};

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

                    //数字であるのかチェック
                    if (isNaN(clipTextArray[j][i])){
                        alert('{rval MSG907}');
                        return false;
                    }

                    var valScore = parseInt(clipTextArray[j][i]);
                    if (String(objectNameArray[k]).match(/SCORE2/)) {
                        var maxScore2 = document.forms[0].MAXSCORE2.value;
                        if (valScore < 0 || valScore > maxScore2) {
                            alert('{rval MSG914}'+'0点～'+maxScore2+'点以内で入力してください。');
                            return false;
                        }
                    } else if (String(objectNameArray[k]).match(/SCORE3/)) {
                        if (valScore < 0 || valScore > 10) {
                            alert('{rval MSG914}'+'0点～10点以内で入力してください。');
                            return false;
                        }
                    } else {
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
                    if (String(clipTextArray[j][i]).match(/\./)) {
                        alert('{rval MSG901}'+'「整数」を入力してください。');
                        return false;
                    }
                }
                i++;
            }
        }
    }
    return true;
}

