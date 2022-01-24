function btn_submit(cmd) {
    if (cmd == 'chaircd' && 0 < document.forms[0].COUNTER.value) {
        if (!confirm('保存されていないデータがあれば破棄されます。処理を続行しますか？')) {
            var cnt = document.forms[0]["LIST_CHAIRCD" + document.forms[0].SELECT_CHAIRCD.value].value;
            document.forms[0].CHAIRCD[cnt].selected = true;
            return false;
        }
    }
    if (cmd == 'month' && 0 < document.forms[0].COUNTER.value) {
        if (!confirm('保存されていないデータがあれば破棄されます。処理を続行しますか？')) {
            var cnt = document.forms[0]["LIST_MONTH_SEMESTER" + document.forms[0].SELECT_MONTH_SEMESTER.value].value;
            document.forms[0].MONTH_SEMESTER[cnt].selected = true;
            return false;
        }
    }
    if (cmd == 'calc' || cmd == 'update') {
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
    } else if (cmd == 'calc') {
        clickedBtnUdpateCalc(true);
    } else if (cmd == 'update') {
        //更新権限チェック
        userAuth = document.forms[0].USER_AUTH.value;
        updateAuth = document.forms[0].UPDATE_AUTH.value;
        if (userAuth < updateAuth){
            alert('{rval MSG300}');
            return false;
        }
/***
    } else if (cmd == 'horyuu') {
        for (var i = 0; i < document.forms[0].elements.length; i++ ) {
            var e = document.forms[0].elements[i];
            if (e.type == 'text' && e.value != '') {
                //スペース削除
                var str_num = e.value;
                e.value = str_num.replace(/ |　/g,"");
                var str = e.value;
                var nam = e.name;
                //'*'欠席はスルー
                if (str == '*') {
                    continue;
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
***/
        /*****************************************************************/
        /*** 「成績入力完了チェックの入れ忘れ防止対策(素点)」 --開始-- ***/
        /*****************************************************************/
        // 更新時、下記の処理をする
        // ①：「成績が全て未入力」で「成績入力完了チェックあり」の場合、エラーメッセージを表示する
        // ②：「成績が全て入力済」で「成績入力完了チェックなし」の場合、確認メッセージを表示する
        //テキストボックスの名前の配列を作る
        var textFieldName = document.forms[0].TEXT_FIELD_NAME.value;
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

    //更新・算出ボタン・・・読み込み中は、更新・算出ボタンをグレー（押せないよう）にする。
    document.forms[0].btn_update.disabled = true;
    //1:出欠入力ボタン非表示
    if (document.forms[0].noUseBtnAttend.value !== '1') {
        document.forms[0].btnAttend.disabled = true;
    }
    document.forms[0].btn_calc.disabled = true;
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
        document.forms[0].H_MONTH_SEMESTER.value = document.forms[0].MONTH_SEMESTER.value;
    } else {
        document.forms[0].SUBCLASSCD.value = document.forms[0].H_SUBCLASSCD.value;
        document.forms[0].CHAIRCD.value = document.forms[0].H_CHAIRCD.value;
        document.forms[0].MONTH_SEMESTER.value = document.forms[0].H_MONTH_SEMESTER.value;
    }
    document.forms[0].SUBCLASSCD.disabled = disFlg;
    document.forms[0].CHAIRCD.disabled = disFlg;
    document.forms[0].MONTH_SEMESTER.disabled = disFlg;
    document.forms[0].btn_reset.disabled = disFlg;
    document.forms[0].btn_end.disabled = disFlg;
}

function calc(obj) {
    //スペース削除
    var str_num = obj.value;
    obj.value = str_num.replace(/ |　/g,"");
    //'*'欠席はスルー
    var str = obj.value;
    var nam = obj.name;
    if (str == '*') {
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

    //D065登録科目の入力値チェック
    //D065登録科目か
    if (document.forms[0].SUB_D065.value == '') {
        return;
    }
    //履修単位・修得単位はスルー
    if (nam.match(/COMP_CREDIT/) || nam.match(/GET_CREDIT/)) {
        return;
    }
    //空白はスルー
    if (obj.value == '') {
        return;
    }
    //D065登録科目の入力値チェック用
    var d001val = document.forms[0].D001_LIST.value;
    if (d001val == '') {
        return;
    }

    var errFlg = true;
    var d001List = d001val.split(",");
    for (var h = 0; h < d001List.length; h++ ) {
        var val = d001List[h];
        if (obj.value == val) {
            errFlg = false;
        }
    }
    if (errFlg) {
        alert('{rval MSG901}'+'「'+d001val+'」を入力してください。');
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
    var textFieldName = document.forms[0].TEXT_FIELD_NAME_AND_ATTEND.value;
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

    //テキストボックスのみ貼付対象
    inputName   = targetObject.name.split("-")[0];
    inputNumber = targetObject.name.split("-")[1];
    inputObject = eval("document.forms[0][\"" + inputName + "_INPUT" + "-" + inputNumber + "\"]");
    if (inputObject) {
        if (inputObject.value == 1) {
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

                    //D065登録科目の入力値チェック
                    //D065登録科目か
                    if (document.forms[0].SUB_D065.value == '') {
                        i++;
                        continue;
                    }
                    //履修単位・修得単位はスルー
                    if (objectNameArray[k].match(/COMP_CREDIT/) || objectNameArray[k].match(/GET_CREDIT/)) {
                        i++;
                        continue;
                    }
                    //空白はスルー
                    if (clipTextArray[j][i] == '') {
                        i++;
                        continue;
                    }
                    //D065登録科目の入力値チェック用
                    var d001val = document.forms[0].D001_LIST.value;
                    if (d001val == '') {
                        i++;
                        continue;
                    }

                    var errFlg = true;
                    var d001List = d001val.split(",");
                    for (var h = 0; h < d001List.length; h++ ) {
                        var val = d001List[h];
                        if (clipTextArray[j][i] == val) {
                            errFlg = false;
                        }
                    }
                    if (errFlg) {
                        alert('{rval MSG901}'+'「'+d001val+'」を入力してください。');
                        return false;
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
                targetname = textFieldArray[i] + (cnt + 1);
                targetObject = document.getElementById(targetname);
                targetObject.focus();
                return;
            }
        }
    }
}
