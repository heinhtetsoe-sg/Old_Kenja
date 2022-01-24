<!--kanji=漢字-->
<!-- <?php

require_once('for_php7.php');
 # $RCSfile: knjd123oForm1.js,v $ ?> -->
<!-- <?php # $Revision: 1.1 $ ?> -->
<!-- <?php # $Date: 2011/04/14 09:23:43 $ ?> -->

function btn_submit(cmd) {

    document.forms[0].CHK_FLG.value = 'reset';//NO001

    if (cmd == 'reset'){
        if (!confirm('{rval MSG106}'))
            return false;
    } else if (cmd == 'update'){

        for (var i = 0; i < document.forms[0].elements.length; i++ ) {
            var e = document.forms[0].elements[i];
            if (e.type == 'text' && e.value != '') {
                var nam = e.name;
                if (!isNaN(e.value) && (e.value > 100 || e.value < 0)) {
                    alert('{rval MSG901}' + '\n0～100まで入力可能です');
                    return false;
                }
//---2005.06.08Modify---↓---
//NO002
//              if (nam.match(/.VALUE./) && nam.match(/^SEM/)) {
                if (nam.match(/.VALUE./)) {
                    var v = e.value;
                    if (isNaN(e.value) && !v.match(/-|=/)) {
                        alert(e.value+'{rval MSG901}' + '\n欠課時数情報（-、=)ではない項目があります');
                        return false;
                    } else if (!isNaN(e.value) && (e.value > 100 || e.value < 0)) {
                        alert(e.value+'{rval MSG901}' + '\n0～100まで入力可能です');
                        return false;
                    }
                } else if (isNaN(e.value)) {
                    alert(e.value+'{rval MSG901}' + '\n値：'+e.value+'は 数値ではありません');
                    return false;
                }                
//---2005.05.24Modify
//                if (!nam.match(/.SCORE./) && isNaN(e.value)) {
//                if (isNaN(e.value)) {
//                    alert(e.value+'{rval MSG901}' + '\n値：'+e.value+'は 数値ではありません');
//                    return false;
//                }                
//---2005.05.24Del
//                if (nam.match(/.SCORE./)) {
//                    var v = e.value;
//                    if (isNaN(e.value) && !v.match(/kk|KK|Kk|kK|ks|KS|Ks|kS/)) {
//                        alert(e.value+'{rval MSG901}' + '\n出欠情報（KK、KS)ではない項目があります');
//                        return false;
//                    } else if (!isNaN(e.value) && (e.value > 100 || e.value < 0)) {
//                        alert(e.value+'{rval MSG901}' + '\n0～100まで入力可能です');
//                        return false;
//                    }
//                }
//---2005.06.08Modify---↑---
            }
        }

        /*** 「成績入力完了チェックの入れ忘れ防止対策」 --開始-- ***/
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
            if (e.type == 'text' && nam.match(/.SCORE./)) {
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
        var semename1 = document.forms[0].SEMENAME1.value;
        var semename2 = document.forms[0].SEMENAME2.value;
        var testname1 = document.forms[0].TESTNAME10101.value;
        var testname2 = document.forms[0].TESTNAME10201.value;
        var testname3 = document.forms[0].TESTNAME20101.value;
        var testname4 = document.forms[0].TESTNAME20201.value;
        score_msg[0] = "（"+semename1+testname1+"素点）";
        score_msg[1] = "（"+semename1+testname2+"素点）";
        score_msg[2] = "（"+semename2+testname3+"素点）";
        score_msg[3] = "（"+semename2+testname4+"素点）";
        var info_msg = "";
        var info_msg2 = "";
        for (var i = 0; i < 4; i++ ) {
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
            if (!confirm(info_msg+'\n\n成績入力完了にチェックが入っていません。\n成績が全て入力済みですが、このまま更新してもよろしいですか？'))
                return false;
        }
        /*** 「成績入力完了チェックの入れ忘れ防止対策」 --終了-- ***/
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function calc(obj){

    var str = obj.value;
    var nam = obj.name;
    
//---2005.05.24Del
//    if (nam.match(/.SCORE./)) {
//        if (str.toUpperCase() == 'KK' | str.toUpperCase() == 'KS') { 
//            obj.value = str.toUpperCase();
//            return;
//        }
//    }
//---2005.06.08Add
//NO002
//  if (nam.match(/.VALUE./) && nam.match(/^SEM/)) {
    if (nam.match(/.VALUE./)) {
        if (str == '-' | str == '=') { 
            return;
        }
    }
    //数字チェック
    if (isNaN(obj.value)){
        alert('{rval MSG907}');
        obj.value = obj.defaultValue;
        return;
    }

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

    //2005.09.13---ALP
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

