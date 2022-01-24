function btn_submit(cmd) {

    document.forms[0].CHK_FLG.value = 'reset';

    if (cmd == 'cancel' && !confirm('{rval MSG106}')){
        return true;
    }else if (cmd == 'update'){

        /*** 「成績入力完了チェックの入れ忘れ防止対策」 --開始-- ***/
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
            if (e.type == 'text' && (nam.match(/.INTER./) || nam.match(/.TERM./))) {
                if (nam.match(/SEM1_INTER_REC./)) {
                    score_txt[0] = true;
                    if (e.value == '') score_cnt[0] = false;
                    else               score_not[0] = false;
                }
                if (nam.match(/SEM1_TERM_REC./)) {
                    score_txt[1] = true;
                    if (e.value == '') score_cnt[1] = false;
                    else               score_not[1] = false;
                }
                if (nam.match(/SEM2_INTER_REC./)) {
                    score_txt[2] = true;
                    if (e.value == '') score_cnt[2] = false;
                    else               score_not[2] = false;
                }
                if (nam.match(/SEM2_TERM_REC./)) {
                    score_txt[3] = true;
                    if (e.value == '') score_cnt[3] = false;
                    else               score_not[3] = false;
                }
                if (nam.match(/SEM3_TERM_REC./)) {
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
        score_msg[0] = "（１学期中間）";
        score_msg[1] = "（１学期期末）";
        score_msg[2] = "（２学期中間）";
        score_msg[3] = "（２学期期末）";
        score_msg[4] = "（３学期学年末）";
        var info_msg = "";
        var info_msg2 = "";
        for (var i = 0; i < 5; i++ ) {
            // ①の場合
            if (score_txt[i] && score_cnt[i] && !score_chk[i]) info_msg = info_msg + score_msg[i];
            // ②の場合
            if (score_txt[i] && score_not[i] && score_chk[i]) info_msg2 = info_msg2 + score_msg[i];
        }
        if (info_msg2 != "") {
            alert(info_msg2+'\n\n成績入力完了にチェックが入っています。\n成績が全て未入力の場合、成績入力完了にチェックはできません。');
            return false;
        }
        if (info_msg != "") {
            if (!confirm(info_msg+'\n\n成績入力完了にチェックが入っていません。\n成績が全て入力済みですが、このまま更新してもよろしいですか？')) {
                return false;
            }
        }
        /*** 「成績入力完了チェックの入れ忘れ防止対策」 --終了-- ***/

        clickedBtnUdpate(true);
    }
    document.forms[0].btn_udpate.disabled = true;
    document.forms[0].btn_can.disabled = true;
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
    document.forms[0].btn_udpate.disabled = disFlg;
    document.forms[0].btn_can.disabled = disFlg;
    document.forms[0].btn_end.disabled = disFlg;
    document.forms[0].btn_print.disabled = disFlg;
}

function clickedChkBox(){
    if(document.forms[0].CHAIRCD.value == '') return false;
    //データを入力するとＹＥＳとＮＯボタンが有効になる
    document.forms[0].btn_udpate.disabled = false;
    document.forms[0].btn_can.disabled = false;
    //データを入力すると終了と印刷ボタンが無効になる
    document.forms[0].btn_end.disabled = true;
    document.forms[0].btn_print.disabled = true;
}

function calc(obj){

    if (obj.value == '') return false;
    var electdiv = document.forms[0].ELECTDIV.value;
    var str = obj.value;
    var nam = obj.name;
    if (nam.match(/.INTER.|.TERM./)) {
    
        if (str.toUpperCase() == 'KK' | str.toUpperCase() == 'KS') { 
            obj.value = str.toUpperCase();
            return;
        }
    }

    //文字評定チェック
    if (nam.match(/GRADE_ASSESS/) && 0 < electdiv) {
        var assess = document.forms[0].assess.value;
        if (assess == '') {
            alert('{rval MSG305}' + '評定マスタを設定してください。');
            obj.value = obj.defaultValue;
            return;
        }
        var ass = assess.split(',');
        var ass_flg = true;
        for (var i = 0; i < ass.length; i++) {
            if (str.toUpperCase() == ass[i]) {
                obj.value = str.toUpperCase();
                ass_flg = false;
            }
        }
        if (ass_flg) {
            alert('{rval MSG901}' + '「' + assess + '」を入力してください。');
            obj.value = obj.defaultValue;
        }
        return;
    }

    //数字チェック
    obj.value = toInteger(obj.value);

    if (isNaN(obj.value)){
        alert('{rval MSG907}');
        obj.value = obj.defaultValue;
        return;
    }else{
        if (parseInt(obj.value,10)){
            obj.value = parseInt(obj.value,10);
        }
    }

if(obj.name.match(/GRADE_ASSESS/)){

    var relaassess5 = parseInt(obj.value,10);
    if(relaassess5>5){
    alert('{rval MSG914}'+'5段階以内で入力してください。');
        obj.value = obj.defaultValue;
        return;
    }

}else if(obj.name.match(/GRADE3_RELAASSESS_5STEP/)){

    var relaassess5 = parseInt(obj.value,10);
    if(relaassess5>5){
    alert('{rval MSG914}'+'5段階以内で入力してください。');
        obj.value = obj.defaultValue;
        return;
    }

}else if(obj.name.match(/GRADE3_RELAASSESS_10STEP/)){

    var relaassess10 = parseInt(obj.value,10);
    if(relaassess10>10){
    alert('{rval MSG914}'+'10段階以内で入力してください。');
        obj.value = obj.defaultValue;
        return;
    }

}else{
    var score = parseInt(obj.value,10);
    if(score>100){
    alert('{rval MSG914}'+'100点以内で入力してください。');
        obj.value = obj.defaultValue;
        return;
    }
}

}


function selcontrol(that,ct,keys)
{
//データを入力するとＹＥＳとＮＯボタンが有効になる
document.forms[0].btn_udpate.disabled = '';
document.forms[0].btn_can.disabled = '';
//データを入力すると終了と印刷ボタンが無効になる
document.forms[0].btn_end.disabled = 'disabled';
document.forms[0].btn_print.disabled = 'disabled';

}

function checkTestDateError()
{
    alert('{rval MSG302}'+'\n テスト実施日');
    closeWin();
}

//add  04/12/07  yamauchi
function newwin(SERVLET_URL){

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
    var nameArray = new Array("SEM1_INTER_REC",
                              "SEM1_TERM_REC",
                              "SEM1_REC",
                              "SEM2_INTER_REC",
                              "SEM2_TERM_REC",
                              "SEM2_REC",
                              "SEM3_TERM_REC",
                              "SEM3_REC",
                              "GRADE_RECORD",
                              "GRADE_ASSESS",
                              "GRADE3_RELAASSESS_5STEP",
                              "GRADE3_RELAASSESS_10STEP");

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
    //テキストボックスのみ貼付対象
    inputName   = targetObject.name.split("-")[0];
    inputNumber = targetObject.name.split("-")[1];
    inputObject = eval("document.forms[0][\"" + inputName + "_INPUT" + "-" + inputNumber + "\"]");
    if (inputObject) {
        if (inputObject.value == 1) {
            if (targetObject.value != val) {
                targetObject.style.background = '#ccffcc';
                //データを入力するとＹＥＳとＮＯボタンが有効になる
                document.forms[0].btn_udpate.disabled = false;
                document.forms[0].btn_can.disabled = false;
                //データを入力すると終了と印刷ボタンが無効になる
                document.forms[0].btn_end.disabled = true;
                document.forms[0].btn_print.disabled = true;
            }
            targetObject.value = val;
        }
    }
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

                    //空白はスルー
                    if (clipTextArray[j][i] == '') {
                        i++;
                        continue;
                    }

                    if (objectNameArray[k].match(/INTER/) || objectNameArray[k].match(/TERM/)) {
                        if (clipTextArray[j][i] == 'KK' || clipTextArray[j][i] == 'KS') {
                            i++;
                            continue;
                        }
                    }

                    //文字評定チェック
                    var electdiv = document.forms[0].ELECTDIV.value;
                    if (objectNameArray[k].match(/GRADE_ASSESS/) && 0 < electdiv) {
                        var assess = document.forms[0].assess.value;
                        if (assess == '') {
                            alert('{rval MSG305}' + '評定マスタを設定してください。');
                            return false;
                        }
                        var ass = assess.split(",");
                        var ass_flg = true;
                        for (var h = 0; h < ass.length; h++) {
                            if (clipTextArray[j][i] == ass[h]) {
                                ass_flg = false;
                            }
                        }
                        if (ass_flg) {
                            alert('{rval MSG901}' + '「' + assess + '」を入力してください。');
                            return false;
                        }

                        i++;
                        continue;
                    }

                    //数字であるのかチェック
                    if (isNaN(clipTextArray[j][i])) {
                        alert('{rval MSG907}');
                        return false;
                    }

                    //満点チェック
                    perfect = 100;
                    perfectMsg = '100点';
                    if (objectNameArray[k].match(/GRADE_ASSESS/)) {
                        perfect = 5;
                        perfectMsg = '5段階';
                    } else if (objectNameArray[k].match(/GRADE3_RELAASSESS_5STEP/)) {
                        perfect = 5;
                        perfectMsg = '5段階';
                    } else if (objectNameArray[k].match(/GRADE3_RELAASSESS_10STEP/)) {
                        perfect = 10;
                        perfectMsg = '10段階';
                    }
                    if(clipTextArray[j][i] > perfect) {
                        alert('{rval MSG914}' + perfectMsg + '以内で入力してください。');
                        return false;
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
                targetname = textFieldArray[i] + (cnt + 1);
                targetObject = document.getElementById(targetname);
                targetObject.focus();
                return;
            }
        }
    }
}

