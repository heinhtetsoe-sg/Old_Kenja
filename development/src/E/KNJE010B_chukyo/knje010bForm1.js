function btn_submit(cmd) {
    if (document.forms[0].SCHREGNO.value == ""){
        alert('{rval MSG304}');
        return true;
    }

    if (cmd == 'reload'){ //成績参照
        reloadIframe("knje010bindex.php?cmd=reload");
        for (var i = 0; i < document.forms[0]["CHECK\[\]"].length; i++){
            document.forms[0]["CHECK\[\]"][i].checked = false;
        }
        return true;
    } else if (cmd == 'form2_first'){ //特別活動の記録～
        if (document.forms[0].useSyojikou3.value == "1") {
            loadwindow('knje010bindex.php?cmd=form2_first',0,document.documentElement.scrollTop || document.body.scrollTop,730,520);
        } else {
            loadwindow('knje010bindex.php?cmd=form2_first',0,document.documentElement.scrollTop || document.body.scrollTop,670,440);
        }
        return true;
    } else if (cmd == 'form3_first'){ //成績参照
        loadwindow('knje010bindex.php?cmd=form3_first',0,document.documentElement.scrollTop || document.body.scrollTop,600,540);
        return true;
    } else if (cmd == 'form4_first'){ //指導要録参照
        loadwindow('knje010bindex.php?cmd=form4_first',0,document.documentElement.scrollTop || document.body.scrollTop,710,290);
        return true;
    } else if (cmd == 'subform3'){  //部活動参照
        loadwindow('knje010bindex.php?cmd=subform3',0,document.documentElement.scrollTop || document.body.scrollTop,650,350);
        return true;
    } else if (cmd == 'subform4'){  //委員会参照
        loadwindow('knje010bindex.php?cmd=subform4',0,document.documentElement.scrollTop || document.body.scrollTop,700,350);
        return true;
    } else if (cmd == 'subform5'){  //資格参照
        loadwindow('knje010bindex.php?cmd=subform5',0,document.documentElement.scrollTop || document.body.scrollTop,700,350);
        return true;
    } else if (cmd == 'subform6'){  //指導上参考となる諸事項
        loadwindow('knje010bindex.php?cmd=subform6',0,document.documentElement.scrollTop || document.body.scrollTop,700,350);
        return true;
    } else if (cmd == 'form7_first'){ //指導要録参照
        loadwindow('knje010bindex.php?cmd=form7_first',0,document.documentElement.scrollTop || document.body.scrollTop,730,430);
        return true;
    } else if (cmd == 'subform8'){  //記録備考参照
        loadwindow('knje010bindex.php?cmd=subform8',0,document.documentElement.scrollTop || document.body.scrollTop,750,350);
        return true;
    } else if (cmd == 'reset'){      //取り消し
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
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

//更新後次の生徒のリンクをクリックする
function updateNextStudent(schregno, order){
   if (document.forms[0].SCHREGNO.value == ""){
       alert('{rval MSG304}');
       return true;
   }
    nextURL = "";
    for (var i = 0; i < parent.left_frame.document.links.length; i++){
          var search = parent.left_frame.document.links[i].search;
          //searchの中身を&で分割し配列にする。
          arr = search.split("&");

          //学籍番号が一致
          if (arr[1] == "SCHREGNO="+schregno){
            //昇順
            if (order == 0 && i == parent.left_frame.document.links.length-1){
                idx = 0;                                         //更新後次の生徒へ(データが最後の生徒の時、最初の生徒へ)
            }else if (order == 0){
                idx = i+1;                                       //更新後次の生徒へ
            }else if (order == 1 && i == 0){
                idx = parent.left_frame.document.links.length-1; //更新後前の生徒へ(データが最初の生徒の時)
            }else if (order == 1){
                idx = i-1;                                       //更新後前の生徒へ
            }
            nextURL = parent.left_frame.document.links[idx].href;//上記の結果
            break;
        }
    }
    document.forms[0].cmd.value = 'update';
    //クッキー書き込み
    saveCookie("nextURL", nextURL);
    document.forms[0].submit();
    return false;
}

function NextStudent(cd){
    var nextURL;
    nextURL = loadCookie("nextURL");
    if (nextURL){
        if(cd == '0'){
                //クッキー削除
                deleteCookie("nextURL");

                document.location.replace(nextURL);
            alert('{rval MSG201}');
        }else if(cd == '1'){
                //クッキー削除
                deleteCookie("nextURL");

        }
    }
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
function show(obj, cnt) {
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
    var nameArray = new Array("TOTALSTUDYACT",
                              "TOTALSTUDYVAL",
                              "REMARK");

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
