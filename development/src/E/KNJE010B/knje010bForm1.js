function btn_submit(cmd) {
    if (document.forms[0].SCHREGNO.value == ""){
        alert('{rval MSG304}');
        return true;
    }

    if (cmd == 'reload'){               //成績参照
        reloadIframe("knje010bindex.php?cmd=reload");
        for (var i = 0; i < document.forms[0]["CHECK\[\]"].length; i++){
            document.forms[0]["CHECK\[\]"][i].checked = false;
        }
        return true;
    } else if (cmd == 'form2_first'){   //特別活動の記録～
        if (document.forms[0].useSyojikou3.value == "1") {
            loadwindow('knje010bindex.php?cmd=form2_first',0,document.documentElement.scrollTop || document.body.scrollTop,730,520);
        } else {
            loadwindow('knje010bindex.php?cmd=form2_first',0,document.documentElement.scrollTop || document.body.scrollTop,670,440);
        }
        return true;
    } else if (cmd == 'form3_first'){   //成績参照
        loadwindow('knje010bindex.php?cmd=form3_first',0,document.documentElement.scrollTop || document.body.scrollTop,600,540);
        return true;
    } else if (cmd == 'form4_first'){   //指導要録参照
        loadwindow('knje010bindex.php?cmd=form4_first',0,document.documentElement.scrollTop || document.body.scrollTop,710,290);
        return true;
    } else if (cmd == 'subform6'){      //指導上参考となる諸事項
        loadwindow('knje010bindex.php?cmd=subform6',0,document.documentElement.scrollTop || document.body.scrollTop,700,350);
        return true;
    } else if (cmd == 'form7_first'){   //指導要録参照
        loadwindow('knje010bindex.php?cmd=form7_first',0,document.documentElement.scrollTop || document.body.scrollTop,730,430);
        return true;
    } else if (cmd == 'reset'){         //取り消し
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }
    if (cmd == "reload2") {
        if (confirm('OK　　　  ・・・　全てクリアして読込します\nキャンセル　・・・　追加読込します')) {
            cmd = "reload2_ok";
        } else {
            cmd = "reload2_cancel";
        }
    }
    var defaultPrintMessage;
    if (cmd == 'update') {
        defaultPrintMessage = checkDefaultPrint("特別活動の記録", document.forms[0].tyousasyoSpecialactrecFieldSizeDefaultPrint, document.getElementsByClassName("specialactrec_"));
        if (defaultPrintMessage && !confirm(defaultPrintMessage)) {
            return false;
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
function checkDefaultPrint(title, prop, inputs) {
    if (!prop || !prop.value || !inputs) {
        return "";
    }
    var spl = prop.value.split(/\*/);
    var mojisu = parseInt(spl[0]);
    var gyosu = parseInt(spl[1]);
    if (!mojisu && !gyosu) {
        return "";
    }
    var i, jitsu;
    for (i = 0; i < inputs.length; i++) {
        jitsu = validate_row_cnt(inputs[i].value, mojisu * 2);
        if (jitsu > gyosu) {
            return title + "は" + mojisu + "文字" + gyosu + "行の文字数を超えているため\n" +
                   "印刷時のレイアウトが入力レイアウトと一致しない可能性があります。\n" +
                   "このまま更新しますか？";
        }
    }
    return "";
}
function CheckHealth(obj)
{
    var el = obj.value;
    if (obj.checked == true) {
        var val = "異常なし";
        if (el == "TR_REMARK") val = "特記事項なし";
        document.forms[0][el].value = val;
        document.forms[0][el].onfocus = new Function("this.blur()");
    } else {
        document.forms[0][el].value = document.forms[0][el].defaultValue;
        document.forms[0][el].onfocus = new Function("");
    }

}
function reloadIframe(url){
    document.getElementById("cframe").src=url
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
    var nameArray = new Array("ATTENDREC_REMARK",
                              "SPECIALACTREC",
                              "TRAIN_REF1",
                              "TRAIN_REF2",
                              "TRAIN_REF3");

    var renArray = new Array();
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

    //テキストボックスの名前の配列を作る
    if (document.forms[0].tyousasyoSougouHyoukaNentani.value == "1") {
        var nameArray = new Array();
        for (var i=0; i < document.forms[0].elements.length; i++) {
            var obj_updElement = document.forms[0].elements[i];
            re = new RegExp("^TOTALSTUDYACT|TOTALSTUDYVAL" );
            if (obj_updElement.name.match(re) && obj_updElement.name.length > 16) {
                nameArray.push(obj_updElement.name);
            }
        }
        nameArray.push("REMARK");
    } else {
        var nameArray = new Array("TOTALSTUDYACT",
                                  "TOTALSTUDYVAL",
                                  "REMARK");
    }

    insertTsv({"clickedObj"      :obj,
               "harituke_type"   :"kotei",
               "objectNameArray" :nameArray
               });
    //これを実行しないと貼付けそのものが実行されてしまう
    return false;
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
function CheckRemark() {
    if (document.forms[0].NO_COMMENTS.checked == true) {
        document.forms[0].REMARK.value = document.forms[0].NO_COMMENTS_LABEL.value;
        document.forms[0].REMARK.disabled =true;
    } else {
        document.forms[0].REMARK.disabled = false;
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
