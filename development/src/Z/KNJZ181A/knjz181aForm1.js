function btn_submit(cmd) {
    if (cmd == "clear") {
        result = confirm('{rval MSG106}');
        if (result == false) {
            return false;
        }
    }
    if (cmd == "update") {
        if (document.forms[0].MOCK_SUBCLASS_CD.value == "" || document.forms[0].ASSESSLEVELCNT.value == "") {
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
        var copyMotoCd = document.forms[0].PRE_MOCKCD.value;
        var copySakiCd = document.forms[0].MOCKCD.value;

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
    /*********************************/
    /* var cnt = 20; //段階数(3以上) */
    /* var avg = 45; //平均点(序列)  */
    /* var min = 10; //最低点(序列)  */
    /* var max = 87; //最高点(序列)  */
    /*********************************/
    var cnt = document.forms[0].ASSESSLEVELCNT.value;
    var avg = document.forms[0].AVG.value;
    var min = document.forms[0].LOWSCORE.value;
    var max = document.forms[0].HIGHSCORE.value;

    var avgLevel;    //平均点の段階値
    var uppLevelCnt; //上段の段階数
    var dwnLevelCnt; //下段の段階数
    var uppWidth;    //上段幅
    var dwnWidth;    //下段幅

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
    if (avg == '' || min == '' || max == '') {
        alert('{rval MSG305}' + '( 序列確定処理 )');
        return false;
    }

    /****************/
    /* 整数値に変換 */
    /****************/
    cnt = parseInt(cnt);
    avg = parseInt(avg);
    min = parseInt(min);
    max = parseInt(max);

    /******************/
    /* 平均点の段階値 */
    /******************/
    //算出式・・・(段階数/2)+(段階数%2)
    //例・・・(20/2)+(20%2)=(10)+(0)=10
    avgLevel = parseInt(cnt / 2) + (cnt % 2);

    /****************/
    /* 上段の段階数 */
    /****************/
    //算出式・・・(平均点の段階値)-(1)
    //例・・・(10)-(1)=9
    uppLevelCnt = avgLevel - 1;

    /****************/
    /* 下段の段階数 */
    /****************/
    //算出式・・・(段階数)-(平均点の段階値)
    //例・・・(20)-(10)=10
    dwnLevelCnt = cnt - avgLevel;

    /**********/
    /* 上段幅 */
    /**********/
    //算出式・・・(平均点-最低点)/(上段の段階数)
    //例・・・(45-10)/(9)=(35)/(9)≒3.8=4(切り上げ)
    var upp = (avg - min) / uppLevelCnt;
    uppWidth = Math.ceil(upp);

    /**********/
    /* 下段幅 */
    /**********/
    //算出式・・・(最高点-平均点)/(下段の段階数)
    //例・・・(87-45)/(10)=(42)/(10)≒4.2=5(切り上げ)
    var dwn = (max - avg) / dwnLevelCnt;
    dwnWidth = Math.ceil(dwn);

    /****************/
    /* 上段にセット */
    /****************/
    //(上段の段階数)分を繰り返す
    var uppAssessLevel; //各段階値・・・(平均点の段階値)-(i)
    var uppAssessLow;   //各下限値・・・(平均点)-{(上段幅)*(i)}
    var uppAssessHigh;  //各上限値・・・(平均点)-{(上段幅)*(i-1)}-(1)
    for (var i = 1; i <= uppLevelCnt; i++) {
        uppAssessLevel = avgLevel - i;
        uppAssessLow   = avg - (uppWidth * i);
        uppAssessHigh  = avg - (uppWidth * (i - 1)) - 1;

        uppAssessLowObject  = eval("document.forms[0].ASSESSLOW"  + uppAssessLevel);
        uppAssessLowObject.value  = uppAssessLow;
        document.all['strID' + (uppAssessLevel)].innerHTML = uppAssessHigh;
    }

    /****************/
    /* 下段にセット */
    /****************/
    //(下段の段階数)分を繰り返す
    /******************************/
    /* 平均点の段にも一緒にセット */
    /******************************/
    //(1回)分を繰り返す・・・初期値:0
    var dwnAssessLevel; //各段階値・・・(平均点の段階値)+(i)
    var dwnAssessLow;   //各下限値・・・(平均点)+{(下段幅)*(i)}
    var dwnAssessHigh;  //各上限値・・・(平均点)+{(下段幅)*(i+1)}-(1)
    for (var i = 0; i <= dwnLevelCnt; i++) {
        dwnAssessLevel = avgLevel + i;
        dwnAssessLow   = avg + (dwnWidth * i);
        dwnAssessHigh  = avg + (dwnWidth * (i + 1)) - 1;

        dwnAssessLowObject  = eval("document.forms[0].ASSESSLOW"  + dwnAssessLevel);
        dwnAssessLowObject.value  = dwnAssessLow;
        if (i == dwnLevelCnt) {
            dwnAssessHighObject  = eval("document.forms[0].ASSESSHIGH"  + dwnAssessLevel);
            dwnAssessHighObject.value  = dwnAssessHigh;
        } else {
            document.all['strID' + (dwnAssessLevel)].innerHTML = dwnAssessHigh;
        }
    }

    return;
}

function closing_window(){
        alert('{rval MSG300}');
        closeWin();
        return true;
}
