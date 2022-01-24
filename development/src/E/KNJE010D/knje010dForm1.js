function btn_submit(cmd, arg) {
    if (document.forms[0].SCHREGNO.value == ""){
        alert('{rval MSG304}');
        return true;
    }

    if (cmd == 'reload'){               //成績参照
//        reloadIframe("knje010dindex.php?cmd=reload");
//        for (var i = 0; i < document.forms[0]["CHECK\[\]"].length; i++){
//            document.forms[0]["CHECK\[\]"][i].checked = false;
//        }
        return true;
    } else if (cmd == 'formSeiseki_first'){   //成績参照
        loadwindow('knje010dindex.php?cmd=formSeiseki_first',0,document.documentElement.scrollTop || document.body.scrollTop,600,540);
        return true;
    } else if (cmd == 'formYorokuSanshou_first'){   //指導要録参照
        loadwindow('knje010dindex.php?cmd=formYorokuSanshou_first',0,document.documentElement.scrollTop || document.body.scrollTop,710,290);
        return true;
    } else if (cmd == 'formYorokuSanshou2_first'){   //指導要録参照
        loadwindow('knje010dindex.php?cmd=formYorokuSanshou2_first',0,document.documentElement.scrollTop || document.body.scrollTop,730,430);
        return true;
    }else if (cmd == 'hrShojikouTorikomi') { // 指導上参考となる諸事項 年組一括取込
        var hrname = arg["HR_NAME"] || "";
        var message = hrname + "全生徒の指導上参考となる諸事項を一括取込みします。";
        var countYears = arg["COUNT_YEARS"];
        if (countYears && countYears.length > 0) {
            message += "\n\n" + "※以下の入力済みの指導上参考となる諸事項を削除して取込みます。";
            for (var i = 0; i < countYears.length; i++) {
                message += "\n  " + countYears[i];
            }
        }
        message += "\n\n処理を続行しますか？";
        if (!confirm(message)) {
            return false;
        }
    } else if (cmd == 'formYourokuIkkatsuTorikomi_first'){ //指導要録所見一括取込
        loadwindow('knje010dindex.php?cmd=formYourokuIkkatsuTorikomi_first',0,document.documentElement.scrollTop || document.body.scrollTop,900,500);
        return true;
    } else if (cmd == 'reset'){         //取り消し
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    } else if (cmd == 'copy_pattern') { //左のパターンのデータをコピー
        if (document.forms[0].REFER_PATTERN.value == "") {
            alert('{rval MSG310}'+'\n（ 参照パターン ）');
            return true;
        }

        if (document.forms[0].REFER_PATTERN.value == document.forms[0].SELECT_PATTERN.value) {
            alert('参照パターンと対象パターンで同一のパターンが選択されています。');
            return true;
        }

        if (!confirm('{rval MSG101}')) {
            return false;
        }
    }
    if (cmd == "yorokuYoriYomikomi") {
        if (confirm('OK　　　  ・・・　全てクリアして読込します\nキャンセル　・・・　追加読込します')) {
            cmd = "yorokuYoriYomikomi_ok";
        } else {
            cmd = "yorokuYoriYomikomi_cancel";
        }
    }
    //フレームロック機能（プロパティの値が1の時有効）
    if (document.forms[0].useFrameLock.value == "1") {
        //更新中の画面ロック(全フレーム)
        if (cmd == 'update') {
            updateFrameLocks();
        }
    }
    if (cmd == 'update') {
        //更新ボタン・・・読み込み中は、更新ボタンをグレー（押せないよう）にする。
        document.forms[0].btn_update.disabled = true;
        document.forms[0].btn_up_next.disabled = true;
        document.forms[0].btn_up_pre.disabled = true;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//function reloadIframe(url){
//    document.getElementById("cframe").src=url
//}

function cmb_submit(obj, cmd) {
    if (document.forms[0].SCHREGNO.value == "") {
        alert('{rval MSG304}');
        obj.selectedIndex = 0;
        return true;
    }

    btn_submit(cmd);
}

//モデルでチェックすると KNJXEXP が変わったら面倒なので、
//javascriptで学年を取得する
window.onload = function () {
    if (document.forms[0].LEFT_GRADE.value == '') {
        left_grade = parent.left_frame.document.forms[0].GRADE.value;
        grade_class = new Array();
        grade_class = left_grade.split('-');
        window.location += '&GRADE=' + grade_class[0];
    }
};

/**************************************************** 貼付け関係 **********************************************/
function showPaste(obj, cnt) {
    if (!confirm('OK　　　　　・・・　上書き(複数セル)\nキャンセル　・・・　追加・挿入(単一セル)')) {
        return;
    }

    //テキストボックスの名前の配列を作る
    var nameArray = ["ATTENDREC_REMARK",
                     "SPECIALACTREC"
                     ];

    var renArray = [];
    var yearArray = document.forms[0].hiddenYear.value.split(",");
    for (var i = 0; i < yearArray.length; i++) {
        renArray[i] = yearArray[i];
    }

    insertTsv({"clickedObj"      :obj,
               "harituke_type"   :"renban_hairetu",
               "objectNameArray" :nameArray,
               "hairetuCnt" :cnt,
               "renbanArray" : renArray
               });
    //これを実行しないと貼付けそのものが実行されてしまう
    return false;
}

function showKotei(obj) {
    if (!confirm('OK　　　　　・・・　上書き(複数セル)\nキャンセル　・・・　追加・挿入(単一セル)')) {
        return;
    }

    var nameArray = [];
    var re = new RegExp("^TOTALSTUDYACT|TOTALSTUDYVAL" );
    //テキストボックスの名前の配列を作る
    if (document.forms[0].tyousasyoSougouHyoukaNentani.value == "1") {
        for (var i=0; i < document.forms[0].elements.length; i++) {
            var obj_updElement = document.forms[0].elements[i];
            if (obj_updElement.name.match(re) && obj_updElement.name.length > 16) {
                nameArray.push(obj_updElement.name);
            }
        }
    } else {
        nameArray.push("TOTALSTUDYACT");
        nameArray.push("TOTALSTUDYVAL");
    }
    nameArray.push("REMARK");

    insertTsv({"clickedObj"      :obj,
               "harituke_type"   :"kotei",
               "objectNameArray" :nameArray
               });
    //これを実行しないと貼付けそのものが実行されてしまう
    return false;
}

function showPaste6Bunkatu(obj) { //6分割用
    if (!confirm('OK　　　　　・・・　上書き(複数セル)\nキャンセル　・・・　追加・挿入(単一セル)')) {
        return;
    }

    //年度
    var yearArray = document.forms[0].hiddenYear.value.split(",");

    //テキストボックスの名前の配列を作る
    var nameArray = ["TRAIN_REF1",
                     "TRAIN_REF2",
                     "TRAIN_REF3",
                     "TRAIN_REF4",
                     "TRAIN_REF5",
                     "TRAIN_REF6"
                     ];

    var nameToPositionMap   = new Object(); //オブジェクト名->(行, 列)
    var positionToNameArray = new Array();  //(行,列)->オブジェクト名
    var innerRow = 0; //年度毎の行内の行
    for (var row = 0; row < yearArray.length; row++) {
        var year = yearArray[row];
        positionToNameArray[innerRow] = new Array();
        for (var col = 0; col < 3; col++) {
            var colObjName = nameArray[col];
            nameToPositionMap[colObjName + "-" +  year] = {"ROW" : innerRow, "COL" : col};
            positionToNameArray[innerRow][col] = colObjName + "-" + year;
        }
        innerRow++;
        positionToNameArray[innerRow] = new Array();
        for (var col = 0; col < 3; col++) {
            var colObjName = nameArray[col + 3];
            nameToPositionMap[colObjName + "-" +  year] = {"ROW" : innerRow, "COL" : col};
            positionToNameArray[innerRow][col] = colObjName + "-" + year;
        }
        innerRow++;
    }                    
    
    insertTsv({"clickedObj"          : obj,
               "harituke_type"       : "6bunkatu",
               "nameToPositionMap"   : nameToPositionMap,
               "positionToNameArray" : positionToNameArray
               });
    //これを実行しないと貼付けそのものが実行されてしまう
    return false;
}

//共通関数を上書き
function paste_other(clipTextArray, harituke_jouhou) {
    var startFlg = false;
    var retuCnt;
    var objectNameArray = harituke_jouhou.objectNameArray;
    var targetName;
    var targetNumber;
    if (harituke_jouhou.harituke_type == "renban") {
        targetName   = harituke_jouhou.clickedObj.name.split("-")[0];
        targetNumber = harituke_jouhou.clickedObj.name.split("-")[1];
    } else if (harituke_jouhou.harituke_type == "kotei") {
        targetName   = harituke_jouhou.clickedObj.name;
    } else if (harituke_jouhou.harituke_type == "6bunkatu") {
        //6bunkatuの場合は別の貼り付け処理を呼び出す
        paste_6bunkatu(clipTextArray, harituke_jouhou);
        return;
    }

    for (var gyouCnt = 0; gyouCnt < clipTextArray.length; gyouCnt++) { //クリップボードの各行をループ
        retuCnt = 0;
        startFlg = false;

        for (k = 0; k < objectNameArray.length; k++) { //テキストボックス名でまわす
            if (objectNameArray[k] == targetName) { //貼付け開始対象のテキストボックスならばフラグを立てる
                startFlg = true;
            }
            if (startFlg) {
                //クリップボードのデータでタブ区切りの最後を越えるとundefinedになる
                if (clipTextArray[gyouCnt][retuCnt] != undefined) { //対象となるデータがあれば

                    if (harituke_jouhou.harituke_type == "renban") {
                        targetObject = eval("document.forms[0][\"" + objectNameArray[k] + "-"  + targetNumber + "\"]");
                    } else if (harituke_jouhou.harituke_type == "kotei") {
                        targetObject = eval("document.forms[0][\"" + objectNameArray[k] + "\"]");
                    }
                    if (targetObject) { //テキストボックスがあれば(テキストボックスはあったりなかったりする)
                        execCopy(targetObject, clipTextArray[gyouCnt][retuCnt], targetNumber);
                    }
                }
                retuCnt++;
            }
        }
        if (harituke_jouhou.harituke_type == "kotei") {
            break;
        }
        targetNumber++;
    }
}

function paste_6bunkatu(clipTextArray, harituke_jouhou) {
    var targetName = harituke_jouhou.clickedObj.name;
    var nameToPositionMap   = harituke_jouhou.nameToPositionMap;
    var positionToNameArray = harituke_jouhou.positionToNameArray;

    var startRow = nameToPositionMap[targetName]["ROW"];
    var startCol = nameToPositionMap[targetName]["COL"];

    for (var offsetRow = 0; offsetRow < clipTextArray.length; offsetRow++) {
        for (var offsetCol = 0; offsetCol < clipTextArray[offsetRow].length; offsetCol++) {
            var targetRow = startRow + offsetRow;
            var targetCol = startCol + offsetCol;                

            if (positionToNameArray.length <= targetRow) continue;
            if (positionToNameArray[targetRow].length <= targetCol) continue;

            //クリップボードのデータでタブ区切りの最後を越えるとundefinedになる
            if (clipTextArray[offsetRow][offsetCol] != undefined) { //対象となるデータがあれば
                targetObject = eval("document.forms[0][\"" + positionToNameArray[targetRow][targetCol] + "\"]");
                if (targetObject) {
                    execCopy(targetObject, clipTextArray[offsetRow][offsetCol], "");
                }
            }
        }
    } 
     
}

//すでにある値とクリップボードの値が違う場合は背景色を変える(共通関数から呼ばれる)
function execCopy(targetObject, val, targetNumber) {
    targetObject.value = val;
    return true;
}

//クリップボードの中身のチェック(だめなデータならばfalseを返す)(共通関数から呼ばれる)
function checkClip(clipTextArray, harituke_jouhou) {
    return true;
}

//備考チェックボックス
function CheckRemark(tgtid, chkid) {
    var tgt = document.getElementById(tgtid);
    var chk = document.getElementById(chkid);
    if (chk.checked == true) {
        tgt.value = document.forms[0].NO_COMMENTS_LABEL.value;
        tgt.disabled =true;
    } else {
        tgt.disabled = false;
    }
}

function newwin(SERVLET_URL){
    if (document.forms[0].SCHREGNO.value == ""){
        alert('{rval MSG304}');
        return true;
    }

    var gradehrclass = parent.left_frame.document.forms[0].GRADE.value.split('-');
    parent.right_frame.document.forms[0].GRADE_HR_CLASS.value = gradehrclass[0] + gradehrclass[1];
    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJE";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

//コメント差し込み
function insertComment(obj, target, label) {
    if (obj.checked == true) {
        document.forms[0][target].value = document.forms[0][label].value;
        document.forms[0][target].disabled = true;
    } else {
        document.forms[0][target].disabled = false;
    }
}
