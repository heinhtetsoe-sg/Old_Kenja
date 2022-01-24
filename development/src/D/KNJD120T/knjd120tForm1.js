function btn_submit(cmd) {

    if (cmd == 'reset'){
        if (!confirm('{rval MSG106}'))
            return false;
    } else if (cmd == 'update'){

        for (var i = 0; i < document.forms[0].elements.length; i++ ) {
            var e = document.forms[0].elements[i];
            if (e.type == 'text' && e.value != '') {
                var nam = e.name;
                //満点チェック
                var perfectName   = nam.split("-")[0] + "_PERFECT";
                var perfectNumber = nam.split("-")[1];
                perfectObject = eval("document.forms[0][\"" + perfectName + "-" + perfectNumber + "\"]");
                var perfect = parseInt(perfectObject.value);
                if (!isNaN(e.value) && (e.value > perfect || e.value < 0)) {
                    alert('{rval MSG901}' + '\n0～'+perfect+'まで入力可能です');
                    return false;
                }
                if (!nam.match(/.SCORE./) && isNaN(e.value)) {
                    alert(e.value+'{rval MSG901}' + '\n値：'+e.value+'は 数値ではありません');
                    return false;
                }                
                if (nam.match(/.SCORE./)) {
                    var v = e.value;
                    if (isNaN(e.value) && !v.match(/kk|KK|Kk|kK|ks|KS|Ks|kS/)) {
                        alert(e.value+'{rval MSG901}' + '\n出欠情報（KK、KS)ではない項目があります');
                        return false;
                    } else if (!isNaN(e.value) && (e.value > perfect || e.value < 0)) {
                        alert(e.value+'{rval MSG901}' + '\n0～'+perfect+'まで入力可能です');
                        return false;
                    }
                }
            }
        }

    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function calc(obj){

    var str = obj.value;
    var nam = obj.name;
    
    if (nam.match(/.SCORE./)) {
        if (str.toUpperCase() == 'KK' | str.toUpperCase() == 'KS') { 
            obj.value = str.toUpperCase();
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

    if (document.forms[0].gen_ed.value != "" && nam.match(/GRAD_VALUE./)) {
        var n = nam.split('-');
        if (a_mark[obj.value] == undefined){
            outputLAYER('mark'+n[1], '');
        } else {
            outputLAYER('mark'+n[1], a_mark[obj.value]);
        }
    }
}

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
function show(obj) {
    if (!confirm('内容を貼付けますか？')) {
        return false;
    }

    //テキストボックスの名前の配列を作る
    var nameArray = new Array("SEM1_INTR_SCORE",
                              "SEM1_INTR_VALUE",
                              "SEM1_TERM_SCORE",
                              "SEM1_TERM_VALUE",
                              "SEM1_VALUE",
                              "SEM2_INTR_SCORE",
                              "SEM2_INTR_VALUE",
                              "SEM2_TERM_SCORE",
                              "SEM2_TERM_VALUE",
                              "SEM2_VALUE",
                              "GRAD_VALUE");

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

                    if (objectNameArray[k].match(/SCORE/)) {
                        if (clipTextArray[j][i] == 'KK' | clipTextArray[j][i] == 'KS') { 
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

