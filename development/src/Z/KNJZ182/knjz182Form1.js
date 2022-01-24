function btn_submit(cmd) {
    if (cmd == "clear") {
        result = confirm('{rval MSG106}');
        if (result == false) {
            return false;
        }
    }
    if (cmd == "update") {
        if (document.forms[0].SUBCLASSCD.value == "" || document.forms[0].ASSESSLEVELCNT.value == "") {
            alert('{rval MSG301}');
            return false;
        }
        for (var i = 0; i < document.forms[0].length; i++) {
            if (document.forms[0][i].name.match(/ASSESS(LOW|HIGH)/)) {
                if (document.forms[0][i].value == "") {
                    alert('{rval MSG301}');
                    return false;
                }
            }
        }
    }
    if (cmd == "copy") {
        var copyMotoCnt = document.forms[0].COPY_MOTO_CNT.value;
        var copySakiCnt = document.forms[0].COPY_SAKI_CNT.value;
        var copyMotoCd = document.forms[0].PRE_SEMESTER.value + document.forms[0].PRE_TESTKINDCD.value;
        var copySakiCd = document.forms[0].SEMESTER.value + document.forms[0].TESTKINDCD.value;

        //①確認メッセージを表示する。
        if (copySakiCnt == 0) {
            if (!confirm('コピーします。よろしいでしょうか？')) {
                return false;
            }
        } else {
            if (!confirm('既に対象データが存在します。\n対象データを削除して、コピーします。よろしいでしょうか？')) {
                return false;
            }
        }
        //②エラーメッセージを表示する。
        if (copyMotoCd == copySakiCd) {
            alert('コピーできません。\n\n選択した参照データが対象データと同じです。\n別の参照データを選択して下さい。');
            return false;
        }
        //③エラーメッセージを表示する。
        if (copyMotoCnt == 0) {
            alert('コピーできません。\n\n選択した参照データが存在しません。\n別の参照データを選択して下さい。');
            return false;
        }
    }
    //読込中はコピーボタン使用不可
    document.forms[0].btn_copy.disabled = true;

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function level(cnt) {
    var level;
    level = document.forms[0].ASSESSLEVELCNT.value;
    if (level == cnt) {
        return false;
    }
    if (level == '') {
        document.forms[0].ASSESSLEVELCNT.value = 0;
    }

    document.forms[0].cmd.value = 'level';
    document.forms[0].submit();
    return false;
}

function isNumb(that,level,mode){
    var num;
    var anser;

    that.value = toInteger(that.value);

    if (that.value <= 0) {
        return;
    } else {
        if (mode == 'SYOUSUU') {
            anser = that.value;
            anser = ((anser * 10) - 1) / 10;
            anser = "" + anser;
            if(anser.length == 1)
            {
                anser = anser + '.0';
            }
        } else {
            anser = (that.value - 1);
        }
        document.all['strID' + (level)].innerHTML = anser;
    }
    return;
}

function keisan(level) {
    //必須
    /************************************************/
    /* var cnt = 20; //段階数(3以上)                */
    /* var avg = 50; //平均点(序列)                 */
    /* var perfect = 100; //cntの上限値(満点)       */
    /* var maxAssessLow = 80; //cntの下限値(手入力) */
    /************************************************/
    var cnt = document.forms[0].ASSESSLEVELCNT.value;
    var avg = document.forms[0].AVG.value;
    var perfect = document.forms[0].PERFECT.value;
    var maxAssessLow;

    var avgLevel;    //平均点の段階値

    /****************/
    /* 必須チェック */
    /****************/
    if (level == '' || cnt == '') {
        alert('{rval MSG301}' + '( 段階数 )');
        return false;
    }
    if (level != cnt) {
        alert('{rval MSG901}' + '( 段階数 )');
        return false;
    }
    if (cnt < 3) {
        alert('段階数(3以上)を指定して下さい。');
        return false;
    }
    if (avg == '') {
        alert('{rval MSG305}' + '( 序列確定処理 )');
        return false;
    }

    /***********************/
    /* cntの下限値(手入力) */
    /***********************/
    maxAssessLowObject  = eval("document.forms[0].ASSESSLOW"  + cnt);
    maxAssessLow = maxAssessLowObject.value;
    if (maxAssessLow == '') {
        alert('{rval MSG301}' + '( ' + cnt + '段階の下限 )');
        return false;
    }

    /*****************************/
    /* cntの上限値(満点)をセット */
    /*****************************/
    maxAssessHighObject  = eval("document.forms[0].ASSESSHIGH"  + cnt);
    maxAssessHighObject.value  = perfect;

    /****************/
    /* 整数値に変換 */
    /****************/
    cnt = parseInt(cnt);
    //avg = parseInt(avg);
    maxAssessLow = parseInt(maxAssessLow);

    /******************/
    /* 平均点の段階値 */
    /******************/
    //算出式・・・(段階数/2)+(段階数%2)
    //例・・・(20/2)+(20%2)=(10)+(0)=10
    avgLevel = parseInt(cnt / 2) + (cnt % 2);

    /**********/
    /* セット */
    /**********/
    var preAssessLevel = cnt;
    var AssessLevel; //各段階値
    var AssessLow;   //各下限値
    var AssessHigh;  //各上限値
    for (var score = (maxAssessLow - 1); score >= 0; score--) {
        //素点（評定段階２０の最低点より１点ずつ減らしたもの）
        //各段階値の算出式・・・(平均点の段階値)/(cntの下限値(手入力)-平均点)*(素点-平均点)+(平均点の段階値+0.00001)
        //例・・・10/(80-50)*(77-50)+10.00001=19.00001=19(切り捨て)
        AssessLevel = avgLevel / (maxAssessLow - avg) * (score - avg) + (avgLevel + 0.00001);
        AssessLevel = Math.floor(AssessLevel);

        if (AssessLevel < 1) continue; //段階値１まで繰り返す

//alert('score='+score+', AssessLevel='+AssessLevel);

        //下限値は常に素点で上書き
        AssessLow   = score;
        //上限値は段階値が変わった時の素点で上書き
        if (AssessLevel < preAssessLevel) {
            AssessHigh  = score;
            preAssessLevel = AssessLevel;
        }

        //各下限値
        AssessLowObject  = eval("document.forms[0].ASSESSLOW"  + AssessLevel);
        AssessLowObject.value  = AssessLow;
        //各上限値
        document.all['strID' + (AssessLevel)].innerHTML = AssessHigh;
    }

    return;
}

function closing_window(){
        alert('{rval MSG300}');
        closeWin();
        return true;
}
