function btn_submit(cmd)
{
    var str = new Object();
    if(cmd == 'reset' && !confirm('{rval MSG106}'))  return true;
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function setName(obj, rowid, flg)
{
    Setflg(obj);    //バックカラーを黄色に変更
    var idx = obj.value;
    if (obj.value == '') {
        if (flg == '0') {
            outputLAYER('STATE_NAME' + rowid, '');
        } else {
            outputLAYER('PERIOD_NAME' + rowid, '');
        }
        return;
    }

    if (flg == '0') {
        if (state_name[idx] != null) {
            outputLAYER('STATE_NAME' + rowid, state_name[idx]);
            if (obj.value == 0){
                document.forms[0].elements[obj.id*2+1].value = '*';
                document.forms[0].elements[obj.id*2+1].readOnly = true ;
                document.forms[0].elements[obj.id*2+2].focus();
            }else {
                if (document.forms[0].elements[obj.id*2+1].value == '*') document.forms[0].elements[obj.id*2+1].value = '';
                document.forms[0].elements[obj.id*2+1].focus();
                document.forms[0].elements[obj.id*2+1].readOnly = false ;
            }
        } else {
            alert('状態区分を指定して下さい。');
            outputLAYER('STATE_NAME' + rowid, '');
            obj.value = '';
            if (obj.id == 0) {
                document.forms[0].elements[obj.id+1].focus();   //OnChangeでは、フォーカスの移動が直接できないので
            } else {
                document.forms[0].elements[obj.id-1].focus();   //OnChangeでは、フォーカスの移動が直接できないので
            }
            obj.focus();                                        //一旦他のテキストへ移動後、再度フォーカスを設定する。
        }
    } else {
        if (pcnt_name[idx] != null) {
            outputLAYER('PERIOD_NAME' + rowid, pcnt_name[idx]);
        } else {
            //document.forms[0].elements[obj.id*2].value：現在行の状態コード
            if(document.forms[0].elements[obj.id*2].value == 0 && obj.value == '*'){
                outputLAYER('PERIOD_NAME' + rowid, '');
            }else if(document.forms[0].elements[obj.id*2].value == 0 && obj.value != '*'){
                alert('校時ＣＤが不正です。\n\n状態【0】は、【*】を指定して下さい。');
                outputLAYER('PERIOD_NAME' + rowid, '');
                obj.value = '';
                if (obj.id == 0) {
                    document.forms[0].elements[0].focus();          //OnChangeでは、フォーカスの移動が直接できないので
                } else {
                    document.forms[0].elements[obj.id-1].focus();   //OnChangeでは、フォーカスの移動が直接できないので
                }
                obj.focus();                                        //一旦他のテキストへ移動後、再度フォーカスを設定する。
            }else {
                alert('校時ＣＤが不正です。');
                outputLAYER('PERIOD_NAME' + rowid, '');
                obj.value = '';
                if (obj.id == 0) {
                    document.forms[0].elements[0].focus();          //OnChangeでは、フォーカスの移動が直接できないので
                } else {
                    document.forms[0].elements[obj.id-1].focus();   //OnChangeでは、フォーカスの移動が直接できないので
                }
                obj.focus();                                        //一旦他のテキストへ移動後、再度フォーカスを設定する。
            }
        }
    }
    return;
}

function Setflg(obj)
{
    change_flg = true;
    if (obj.id){
        obj.style.background="yellow";
        document.getElementById('ROWID' + obj.id).style.background="yellow";
    }
}

function isAsteriskOrNaN(val, astFlg) {
    if (val == "*") {
        if (astFlg) {
            return false;
        } else {
            return true;
        }
    }
    //数字チェック
    return isNaN(val);
}

function check(obj, astFlg){

    //数字チェック
    if (isAsteriskOrNaN(obj.value, astFlg)){
        alert('{rval MSG907}');
        obj.value = obj.defaultValue;
        return;
    }

    //評定範囲
    assessMax = document.forms[0].ASSESSMAX.value;
    assessMin = document.forms[0].ASSESSMIN.value;

    if (obj.name.substring(0,10) == "GRAD_VALUE" && document.forms[0].CTRL_YEAR.value >= 2013) {
        var score = parseInt(obj.value);
        if (score < assessMin || score > assessMax) {
            alert('{rval MSG914}'+ assessMin + '～' + assessMax + '以内で入力してください。');
            obj.value = obj.defaultValue;
            return;
        }
    } else {
        var score = parseInt(obj.value);
        if(score>100){
            alert('{rval MSG914}'+'0点～100点以内で入力してください。');
            obj.value = obj.defaultValue;
            return;
        }

        var score = parseInt(obj.value);
        if(score<0){
        alert('{rval MSG914}'+'0点～100点以内で入力してください。');
            obj.value = obj.defaultValue;
            return;
        }
    }
}

/************************************************* 貼付け関係 ***********************************************/
function showPaste(obj) {
    if (!confirm('内容を貼付けますか？')) {
        return false;
    }

    //テキストボックスの名前の配列を作る
    var nameArray = new Array("SEM1_TERM_SCORE",
                              "SEM1_VALUE",
                              "SEM2_TERM_SCORE",
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
//        targetObject.style.background = '#ccffcc';
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
    //評定範囲
    assessMax = document.forms[0].ASSESSMAX.value;
    assessMin = document.forms[0].ASSESSMIN.value;

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
                    if (isAsteriskOrNaN(clipTextArray[j][i])){
                        alert('{rval MSG907}');
                        return false;
                    }

                    //満点チェック
                    if (objectNameArray[k].match(/GRAD_VALUE/) && document.forms[0].CTRL_YEAR.value >= 2013) {
                        score = parseInt(clipTextArray[j][i]);
                        if (score < assessMin || score > assessMax) {
                            alert('{rval MSG914}'+ assessMin + '～' + assessMax + '以内で入力してください。');
                            obj.value = obj.defaultValue;
                            return;
                        }
                    } else {
                        perfect = 100;
                        score = parseInt(clipTextArray[j][i]);
                        if (score < 0 || score > perfect) {
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
