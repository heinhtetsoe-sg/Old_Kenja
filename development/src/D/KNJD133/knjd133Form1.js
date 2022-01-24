function btn_submit(cmd) {
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) return false;
    } else if (cmd == 'update') {
        //フレームロック機能（プロパティの値が1の時有効）
        if (document.forms[0].useFrameLock.value == '1') {
            //更新中の画面ロック
            updateFrameLock();
        }
    }
    //サブミット
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function showTeikeiWindow(targetText, dataDiv) {
    var param = 'cmd=teikei';
    param += '&CHAIRCD=' + document.forms[0].CHAIRCD.value;
    param += '&DATA_DIV=' + dataDiv;
    param += '&TARGETTEXT=' + targetText;

    loadwindow(
        'knjd133index.php?' + param,
        event.clientX +
            (function () {
                var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;
                return scrollX;
            })(),
        event.clientY +
            (function () {
                var scrollY = document.documentElement.scrollTop || document.body.scrollTop;
                return scrollY;
            })(),
        650,
        450
    );

    return true;
}
function showTeikeiWindow2(targetText, dataDiv) {
    var param = 'cmd=teikei';
    param += '&CHAIRCD=' + document.forms[0].CHAIRCD.value;
    param += '&DATA_DIV=' + dataDiv;
    param += '&CALLFUNC=setTeikeiAll';

    loadwindow(
        'knjd133index.php?' + param,
        event.clientX +
            (function () {
                var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;
                return scrollX;
            })(),
        event.clientY +
            (function () {
                var scrollY = document.documentElement.scrollTop || document.body.scrollTop;
                return scrollY;
            })(),
        650,
        450
    );

    return true;
}

//画面切り替え
function Page_jumper(link) {
    if (!confirm('{rval MSG108}')) {
        return;
    }
    parent.location.href = link;
}

//ALLチェック(単位自動計算)
function check_all(obj) {
    for (var i = 0; i < document.forms[0].elements.length; i++) {
        var e = document.forms[0].elements[i];
        var nam = e.name;
        if (e.type == 'checkbox' && nam.match(/CHK_CALC_CREDIT./) && !e.disabled) {
            e.checked = obj.checked;
        }
    }
}

//定型文セット
function tmpSet(obj) {
    // 駒沢の場合処理なし
    if (document.forms[0].isKomazawa.value == '1') {
        return;
    }
    //数値チェック
    obj.value = toInteger(obj.value);
    //定型文セット
    if (obj.value != '' && document.forms[0].showTemp04.value == '1') {
        if (obj.value.match(/1|2|3|4|5/)) {
            //行番号
            var counter = obj.name.split('-');
            //学年
            var grade = document.forms[0]['GRADE-' + counter[1] + ''].value;
            //定型文
            var tmp = '';
            var tei = document.forms[0]['TMP-' + grade + '-' + obj.value + ''];
            if (tei) {
                tmp = tei.value;
            }
            //評価
            var totalstudytime = document.forms[0]['TOTALSTUDYTIME-' + counter[1] + ''];

            if (totalstudytime.value.length == 0) {
                //評価に定型文を追加でセット
                totalstudytime.value += tmp;
            }
        }
    }
    return;
}

function setTeikeiAll(dataDiv, text) {
    var elementName = '';
    if (dataDiv == '03') {
        elementName = 'TOTALSTUDYACT-';
    } else {
        elementName = 'TOTALSTUDYTIME-';
    }
    for (let i = 0; i < document.forms[0].recordCount.value; i++) {
        const element = document.forms[0][elementName + i];
        //駒沢の学習内容(DATA_DIV=03)の場合
        if (document.forms[0].isKomazawa.value == '1' && dataDiv == '03') {
            element.value = text;
        } else {
            element.value += text;
        }
    }
    return;
}

//（駒沢大学）評価テキスト入力時の処理
function setTeikeiTotalstudyTime(obj, counter) {
    if (!obj.value) {
        return false;
    }
    //学年
    var grade = document.forms[0]['GRADE-' + counter + ''].value;
    //定型文
    var teikei1 = document.forms[0]['REMARK-TIME-' + grade + '-81-' + obj.value.substr(0, 1) + ''];
    var teikei2 = document.forms[0]['REMARK-TIME-' + grade + '-82-' + obj.value.substr(1, 1) + ''];
    var teikei3 = document.forms[0]['REMARK-TIME-' + grade + '-83-' + obj.value.substr(2, 1) + ''];
    var teikei4 = document.forms[0]['REMARK-TIME-' + grade + '-84-' + obj.value.substr(3, 1) + ''];

    var text = '';
    if (teikei1) text += teikei1.value;
    if (teikei2) text += teikei2.value;
    if (teikei3) text += teikei3.value;
    if (teikei4) text += teikei4.value;
    // 評価に値を設定
    var targetText = document.forms[0]['TOTALSTUDYTIME-' + counter];
    if (targetText) targetText.value = text;

    return true;
}

//（駒沢大学）学年評定を設定する
function setHyouteiRank(obj, counter) {
    if (!obj.value) {
        return false;
    }
    //定型文
    var rankA = document.forms[0]['RANK_A'];
    var rankB = document.forms[0]['RANK_B'];
    var rankC = document.forms[0]['RANK_C'];

    var rankAFromTo = ['', 0, 0];
    if (rankA) rankAFromTo = rankA.value.split('_');
    var rankBFromTo = ['', 0, 0];
    if (rankB) rankBFromTo = rankB.value.split('_');
    var rankCFromTo = ['', 0, 0];
    if (rankC) rankCFromTo = rankC.value.split('_');

    var score = 0;
    if (obj.value.substr(0, 1) && Number(obj.value.substr(0, 1))) {
        if (Number(obj.value.substr(0, 1)) <= 5) score += Number(obj.value.substr(0, 1));
    }
    if (obj.value.substr(1, 1) && Number(obj.value.substr(1, 1))) {
        if (Number(obj.value.substr(1, 1)) <= 5) score += Number(obj.value.substr(1, 1));
    }
    if (obj.value.substr(2, 1) && Number(obj.value.substr(2, 1))) {
        if (Number(obj.value.substr(2, 1)) <= 5) score += Number(obj.value.substr(2, 1));
    }
    if (obj.value.substr(3, 1) && Number(obj.value.substr(3, 1))) {
        if (Number(obj.value.substr(3, 1)) <= 5) score += Number(obj.value.substr(3, 1));
    }

    var text = '';
    if (rankAFromTo[1] <= score && score <= rankAFromTo[2]) {
        text += rankAFromTo[0];
    } else if (rankBFromTo[1] <= score && score <= rankBFromTo[2]) {
        text += rankBFromTo[0];
    } else if (rankCFromTo[1] <= score && score <= rankCFromTo[2]) {
        text += rankCFromTo[0];
    }
    // 学年評定に値を設定
    var targetText = document.forms[0]['GRAD_VALUE-' + counter];
    if (targetText) targetText.value = text;

    return true;
}

//（駒沢大学）評価テキスト入力時の処理
function setTeikeiTotalstudyTimeAll(obj) {
    for (let i = 0; i < document.forms[0].recordCount.value; i++) {
        const element = document.forms[0]['TOTALSTUDYTIME-' + i];
        if (element) {
            setTeikeiTotalstudyTime(obj, i);
        }
    }
    return true;
}

//（駒沢大学）学年評定を設定する
function setHyouteiRankALL(obj) {
    for (let i = 0; i < document.forms[0].recordCount.value; i++) {
        const element = document.forms[0]['GRAD_VALUE-' + i];
        if (element) {
            setHyouteiRank(obj, i);
        }
    }
    return true;
}

/************************************************* 貼付け関係 ***********************************************/
function showPaste(obj) {
    if (!confirm('OK　　　　　・・・　上書き(複数セル)\nキャンセル　・・・　追加・挿入(単一セル)')) {
        return;
    }

    //テキストボックスの名前の配列を作る
    var nameArray = new Array('TOTALSTUDYACT', 'TOTALSTUDYTIME', 'GRAD_VALUE', 'COMP_CREDIT', 'GET_CREDIT');

    insertTsv({ clickedObj: obj, harituke_type: 'renban', objectNameArray: nameArray });
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
    var targetName = harituke_jouhou.clickedObj.name.split('-')[0];
    var targetNumber = harituke_jouhou.clickedObj.name.split('-')[1];
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
                    if (objectNameArray[k] == 'TOTALSTUDYACT') {
                        if (validate_row_cnt(String(clipTextArray[j][i]), 25 * 2) > 2) {
                            alert('2行までです');
                            return false;
                        }
                    }
                    if (objectNameArray[k] == 'TOTALSTUDYTIME') {
                        if (validate_row_cnt(String(clipTextArray[j][i]), 25 * 2) > 3) {
                            alert('3行までです');
                            return false;
                        }
                    }
                    if (objectNameArray[k] == 'GRAD_VALUE' || objectNameArray[k] == 'COMP_CREDIT' || objectNameArray[k] == 'GET_CREDIT') {
                        if (isNaN(clipTextArray[j][i])) {
                            alert('{rval MSG907}');
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
