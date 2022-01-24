function btn_submit(cmd) {
    //取消確認
    if (cmd == 'reset' && !confirm('{rval MSG106}')) {
        return;
    }

    //更新
    if (cmd == 'update') {
        if (document.forms[0].GRADE_HR_CLASS.value == '') {
            alert('{rval MSG916}\n　　　( 年組 )');
            return;
        }
        if (document.forms[0].SUBCLASSCD.value == '') {
            alert('{rval MSG916}\n　　　( 教科 )');
            return;
        }
        if (document.forms[0].SEQ.value == '') {
            alert('{rval MSG916}\n　　　( テスト単元 )');
            return;
        }
    }

    //フレームロック機能（プロパティの値が1の時有効）
    if (document.forms[0].useFrameLock.value == "1") {
        if (cmd == 'update') {
            updateFrameLock();
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//数字チェック
function NumCheck(obj, assesshi) {
    num = obj.value;
    num = toInteger(num);

    //範囲チェック
    if (num.length > 0 && num > assesshi) {
        alert('{rval MSG916}\n( ' + assesshi + '点までです。 )');
        num = '';
    }

    if (num != obj.value) {
        obj.value = num;
    }

    //合計値セット
    val = 0;
    for (var i=0; i < document.forms[0].elements.length; i++) {
        var element = document.forms[0].elements[i];
        if (element.name.match(/^SCORE_/) && element.id == obj.id) {
            val += (isNaN(element.value) || !element.value) ? 0 : parseInt(element.value, 10);
        }
    }
    total_id = 'total' + obj.id;
    total = document.getElementById(total_id);
    total.innerHTML = val;
}

//貼り付け機能
function showPaste(obj) {
    var StatusArray = new Array();
    for(var i=0;i<document.forms[0].elements.length;i++){
        if(document.forms[0].elements[i].name.indexOf('SCORE_') !== -1){
            var keyName = document.forms[0].elements[i].name;
            var flag = false;
            for(var j=0;j<StatusArray.length;j++){
                if(StatusArray[j]==keyName){
                    flag = true;
                    break;
                }
            }
            if(!flag){
                StatusArray.push(keyName);
            }
        }
    }
    var hairetuCnt = null;
    for(var i=0;i<document.forms[0].elements[obj.name].length;i++){
        if(document.forms[0].elements[obj.name][i]==obj){
            hairetuCnt = i;
            break;
        }
    }
    
    insertTsv({"clickedObj"      :obj,
               "harituke_type"   :"hairetu",
               "objectNameArray" :StatusArray,
               'hairetuCnt':hairetuCnt
               });
    return false;
}

//すでにある値とクリップボードの値が違う場合は背景色を変える(共通関数から呼ばれる)
function execCopy(targetObject, val, targetNumber) {
    if (targetObject.value != val) {
        targetObject.style.background = '#ccffcc';
    }
    targetObject.value = val;
    return true;
}

function checkClip(clipTextArray, harituke_jouhou) {
    var startFlg = false;
    var retuCnt;
    var objCnt          = harituke_jouhou.hairetuCnt;
    var objectNameArray = harituke_jouhou.objectNameArray;
    var targetName      = harituke_jouhou.clickedObj.name;

    for (var gyouCnt = 0; gyouCnt < clipTextArray.length; gyouCnt++) { //クリップボードの各行をループ
        retuCnt = 0;
        startFlg = false;

        for (var k = 0; k < objectNameArray.length; k++) { //テキストボックス名でまわす
            if (objectNameArray[k] == targetName) { //貼付け開始対象のテキストボックスならばフラグを立てる
                startFlg = true;
            }
            if (startFlg) {
                //クリップボードのデータでタブ区切りの最後を越えるとundefinedになる
                if (clipTextArray[gyouCnt][retuCnt] != undefined) { //対象となるデータがあれば

                    targetObject = eval("document.forms[0][\"" + objectNameArray[k] + "\"][" + objCnt + "]");
                    if (targetObject) { //テキストボックスがあれば(テキストボックスはあったりなかったりする)
                        var assesshi = parseInt(targetObject.getAttribute('data-unit-assesshigh'));
                        var num=clipTextArray[gyouCnt][retuCnt];
                        if(!(num+'').match(/^[1-9]?[0-9]+$/)){
                            alert('不正な文字が入力されました。');
                            return false;
                        }
                        num = parseInt(num);
                        if (num> 0 && num > assesshi) {
                            alert('{rval MSG916}\n( ' + assesshi + '点までです。 )');
                            return false;
                        }
                    }
                }
                retuCnt++;
            }
        }
        objCnt++;
    }
    return true;
}

function doKeyDown(e){
    if(e.keyCode !== 13){
        return;
    }
    
    var moveTate = document.forms[0].MOVE_ENTER[0].checked;
    var idx = getActiveElementIdx();
    if(idx === false){
        return;
    }
    if(moveTate){
        var obj = nextElement2(idx);
        if(obj !==false){
            obj.focus();
        }
    } else {
        var obj = nextElement(idx);
        if(obj !==false){
            obj.focus();
        }
    }
}
function getActiveElementIdx(){
    for(var i=0;i<document.forms[0].elements.length;i++){
        if(document.forms[0].elements[i]==document.activeElement){
            return i;
        }
    }
    return false;
}
function nextElement(idx){
    if(document.forms[0].elements[idx].type!='text'){
        return false;
    }
    if(document.forms[0].elements[idx].name=='UNIT_TEST_DATE'){
        return false;
    }
    for(var i=1;i<document.forms[0].elements.length+1;i++){
        if(idx+i>=document.forms[0].elements.length){
            var idx2=idx+i-(document.forms[0].elements.length);
        } else {
            var idx2=idx+i;
        }
        if(document.forms[0].elements[idx2].type=='text'){
            if(document.forms[0].elements[idx2].name!='UNIT_TEST_DATE'){
                return document.forms[0].elements[idx2];
            }
        }
    }
    return false;
}

function nextElement2(idx){
    if(document.forms[0].elements[idx].type!='text'){
        return false;
    }
    if(document.forms[0].elements[idx].name=='UNIT_TEST_DATE'){
        return false;
    }
    var StatusArray = new Array();
    for(var i=0;i<document.forms[0].elements.length;i++){
        if(document.forms[0].elements[i].name.indexOf('SCORE_') !== -1){
            var keyName = document.forms[0].elements[i].name;
            var flag = false;
            for(var j=0;j<StatusArray.length;j++){
                if(StatusArray[j]==keyName){
                    flag = true;
                    break;
                }
            }
            if(!flag){
                StatusArray.push(keyName);
            }
        }
    }
    for(var i=0;i<document.forms[0].elements[document.forms[0].elements[idx].name].length;i++){
        if(document.forms[0].elements[document.forms[0].elements[idx].name][i] == document.activeElement){
            if(document.forms[0].elements[document.forms[0].elements[idx].name][i+1]){
                return document.forms[0].elements[document.forms[0].elements[idx].name][i+1];
            }
        }
    }
    var keyName=document.forms[0].elements[idx].name;
    for(var i=0;i<StatusArray.length;i++){
        if(StatusArray[i]==keyName){
            if(StatusArray[i+1]){
                if(document.forms[0].elements[StatusArray[i+1]][0]){
                    return document.forms[0].elements[StatusArray[i+1]][0];
                } else {
                    return false;
                }
            } else {
                if(document.forms[0].elements[StatusArray[0]][0]){
                    return document.forms[0].elements[StatusArray[0]][0];
                } else {
                    return false;
                }
            }
        }
    }
    return false;
}
window.onkeydown = doKeyDown;
