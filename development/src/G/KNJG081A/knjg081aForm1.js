function btn_submit(cmd) {

    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) return false;
    }

    //サブミット
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

/************************************************* 貼付け関係 ***********************************************/
function showPaste(obj) {
    if (!confirm('OK　　　　　・・・　上書き(複数セル)\nキャンセル　・・・　追加・挿入(単一セル)')) {
        return;
    }

    //テキストボックスの名前の配列を作る
    var nameArray = new Array("PRINT_CNT");

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
        for (k = 0; k < objectNameArray.length; k++) {  //テキストボックス名でまわす
            if (objectNameArray[k] == targetName) {     //貼付け開始対象のテキストボックスならばフラグを立てる
                startFlg = true;
            }
            if (startFlg) {
                if (clipTextArray[j][i] != undefined) {
                    //数字チェック
                    intCheck = true;
                    for (l = 0; l < String(clipTextArray[j][i]).length; l++) {
                        ch = String(clipTextArray[j][i]).substring(l, l+1);
                        if (ch >= "0" && ch <= "9") {
                        } else {
                            intCheck = false;
                        }
                    }
                    if(!intCheck) {
                        alert('数字を入力してください。');
                        return false;
                    }
                    //桁数チェック
                    if (String(clipTextArray[j][i]).length > 2) {
                        alert('2桁までです');
                        return false;
                    }
                }
                i++;
            }
        }
    }
    return true;
}

function newwin(SERVLET_URL, cmd) {
    var i;
    var sel;
    var cmdbk;
    var selbk = [];
    var cnt;
    //何年用のフォームを使うのか決める
    if (document.forms[0].FORM6.checked) {
        document.forms[0].NENYOFORM.value = document.forms[0].NENYOFORM_CHECK.value
    } else {
        document.forms[0].NENYOFORM.value = document.forms[0].NENYOFORM_SYOKITI.value
    }

    if (document.forms[0].GRADE_HR_CLASS.value == '') {
        alert('年組を指定してください。');
        return;
    }
    var total = 0;
    for (i = 0; i < 1000; i++) {
        cnt = document.getElementById("PRINT_CNT-" + i);
        if (!cnt) {
            break;
        }
        if (cnt.value) {
            total += parseInt(cnt.value);
        }
    }
    if (total == 0) {
        alert('調査書発行枚数を指定してください。');
        return;
    }
    if (document.forms[0].tyousasyoCheckCertifDate.value == '1' && document.forms[0].DATE.value == '') {
        alert('記載（証明）日付を指定してください。');
        return;
    }

    cmdbk = document.forms[0].cmd.value;
    document.forms[0].cmd.value = cmd;

    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJG";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
    document.forms[0].cmd.value = cmdbk;
}

