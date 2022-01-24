function btn_submit(cmd) {

    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}'))
            return false;
    } else if (cmd == 'update') {
        clickedBtnUdpate(true);
    }

    //更新ボタン・・・読み込み中は、更新ボタンをグレー（押せないよう）にする。
    document.forms[0].btn_update.disabled = true;

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

//更新時、サブミットする項目使用不可
function clickedBtnUdpate(disFlg) {
    if (disFlg) {
        document.forms[0].H_SEMESTER.value = document.forms[0].SEMESTER.value;
        document.forms[0].H_GRADE_HR_CLASS.value = document.forms[0].GRADE_HR_CLASS.value;
        document.forms[0].H_SUBCLASSCD.value = document.forms[0].SUBCLASSCD.value;
    } else {
        document.forms[0].SEMESTER.value = document.forms[0].H_SEMESTER.value;
        document.forms[0].GRADE_HR_CLASS.value = document.forms[0].H_GRADE_HR_CLASS.value;
        document.forms[0].SUBCLASSCD.value = document.forms[0].H_SUBCLASSCD.value;
    }
    document.forms[0].SEMESTER.disabled = disFlg;
    document.forms[0].GRADE_HR_CLASS.disabled = disFlg;
    document.forms[0].SUBCLASSCD.disabled = disFlg;
    document.forms[0].btn_reset.disabled = disFlg;
    document.forms[0].btn_end.disabled = disFlg;
    document.forms[0].btn_print.disabled = disFlg;
}

//背景色の変更
function background_color(obj) {
    obj.style.background='#ffffff';
}

//印刷
function newwin(SERVLET_URL) {
    if (document.forms[0].GRADE_HR_CLASS.value == '' || document.forms[0].SUBCLASSCD.value == '') {
        alert('年組・科目を指定してください。');
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

function checkPerfect(obj, viewCd) {
    var perfect = document.forms[0]["PERFECT_" + viewCd];
    obj.value = toInteger(obj.value);
    if (perfect !== undefined && document.forms[0]["PERFECT_" + viewCd].value < parseInt(obj.value)) {
        obj.focus();
        alert('入力範囲が不正です。\n' + document.forms[0]["PERFECT_" + viewCd].value + '点以内で入力して下さい。');
    }
}

//観点①～⑤へマウスを乗せた場合、観点名称をチップヘルプで表示
function ViewcdMousein(e, msg_no){
    var msg = "";
    if (msg_no == 1) msg = document.forms[0].VIEWCD1.value;
    if (msg_no == 2) msg = document.forms[0].VIEWCD2.value;
    if (msg_no == 3) msg = document.forms[0].VIEWCD3.value;
    if (msg_no == 4) msg = document.forms[0].VIEWCD4.value;
    if (msg_no == 5) msg = document.forms[0].VIEWCD5.value;
    if (msg_no == 6) msg = document.forms[0].VIEWCD6.value;
    x = e.clientX + document.body.scrollLeft;
    y = e.clientY + document.body.scrollTop;

    document.all("lay").innerHTML = msg;
    document.all["lay"].style.position = "absolute";
    document.all["lay"].style.left = x;
    document.all["lay"].style.top = y;
    document.all["lay"].style.padding = "4px 3px 3px 8px";
    document.all["lay"].style.border = "1px solid";
    document.all["lay"].style.visibility = "visible";
    document.all["lay"].style.background = "#ccffff";
}

function ViewcdMouseout(){
    document.all["lay"].style.visibility = "hidden";
}

//貼り付け機能
function showPaste(obj) {
    if (!confirm('内容を貼付けますか？')) {
        return false;
    }
    var kantenHyouji_5 = document.forms[0].kantenHyouji_5.value;
    var kantenHyouji_6 = document.forms[0].kantenHyouji_6.value;

    //テキストボックスの名前の配列を作る
    if (kantenHyouji_5 == 1) {
        var nameArray = new Array("STATUS1",
                                  "STATUS2",
                                  "STATUS3",
                                  "STATUS4",
                                  "STATUS5",
                                  "STATUS9");
    } else {
        var nameArray = new Array("STATUS1",
                                  "STATUS2",
                                  "STATUS3",
                                  "STATUS4",
                                  "STATUS5",
                                  "STATUS6",
                                  "STATUS9");
    }

    insertTsv({"clickedObj"      :obj,
               "harituke_type"   :"renban",
               "objectNameArray" :nameArray
               });
    //これを実行しないと貼付けそのものが実行されてしまう
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

//クリップボードの中身のチェック(だめなデータならばfalseを返す)(共通関数から呼ばれる)
function checkClip(clipTextArray, harituke_jouhou) {
    var startFlg = false;
    var i;
    var targetName   = harituke_jouhou.clickedObj.name.split("-")[0];
    var targetNumber = harituke_jouhou.clickedObj.name.split("-")[1];
    var objectNameArray = harituke_jouhou.objectNameArray;
    var electdiv = document.forms[0].ELECTDIV.value;
    var kantenHyouji_5 = document.forms[0].kantenHyouji_5.value;
    var kantenHyouji_6 = document.forms[0].kantenHyouji_6.value;
    var IntErrFlg = false;

    for (j = 0; j < clipTextArray.length; j++) { //クリップボードの各行をループ
        i = 0;
        startFlg = false;
        for (k = 0; k < objectNameArray.length; k++) { //テキストボックス名でまわす
            if (objectNameArray[k] == targetName) { //貼付け開始対象のテキストボックスならばフラグを立てる
                startFlg = true;
            }
            if (startFlg) {
                if (clipTextArray[j][i] != undefined && clipTextArray[j][i] != '') {
                    if (electdiv == 0) {
                        var str = clipTextArray[j][i];
                        var newString = "";
                        for (s = 0; s < String(str).length; s++) {
                            ch = String(str).substring(s, s+1);
                            if (ch >= "0" && ch <= "9") {
                                newString += ch;
                            }
                        }

                        if (str != newString) {
                            clipTextArray[j][i] = newString;
                            IntErrFlg = true;
                        }
                    } else {
                        var clipStr = clipTextArray[j][i];
                        if (document.forms[0].HENKAN_TYPE.value == "1") {
                            //英小文字から大文字へ自動変換
                            clipTextArray[j][i] = String(clipStr).toUpperCase();
                        } else if (document.forms[0].HENKAN_TYPE.value == "2") {
                            //英大文字から小文字へ自動変換
                            clipTextArray[j][i] = String(clipStr).toLowerCase();
                        }
                        var str = clipTextArray[j][i];
                        //評定
                        if (objectNameArray[k].match(/STATUS9/)) {
                            if (str != "A" && str != "B" && str != "C") { 
                                alert('{rval MSG901}'+'「AまたはBまたはC」を入力して下さい。\n（評定）');
                                return false;
                            }
                        //観点1～5または6
                        } else {
                            //数値チェック
                            var newString = "";
                            for (s = 0; s < String(str).length; s++) {
                                ch = String(str).substring(s, s+1);
                                if (ch >= "0" && ch <= "9") {
                                    newString += ch;
                                }
                            }

                            if (str != newString) {
                                clipTextArray[j][i] = newString;
                                IntErrFlg = true;
                            }
                        }
                    }
                }
                i++;
            }
        }
    }

    var message = "";
    if (electdiv != 0) {
        message = (kantenHyouji_5 == 1) ? "\n（観点①～⑤）" : "\n（観点①～⑥）";
    }
    if (IntErrFlg) {
        alert("入力された値は不正な文字列です。\n数字を入力してください。\n入力された文字列は削除されます。"+message);
    }

    return true;
}

function kirikae2(obj, viewCd) {
    if (event.preventDefault) {
        event.preventDefault();
    }
    event.cancelBubble = true
    event.returnValue = false;
    clickList(obj, viewCd);
}

function clickList(obj, viewCd) {

    setObj = obj;
    if (event.preventDefault) {
        myObj = document.getElementById('myID_Menu').style;
    } else {
        myObj = document.forms[0].all["myID_Menu"].style;
    }
    myObj.left = window.event.clientX + document.body.scrollLeft + "px";
    myObj.top  = window.event.clientY + document.body.scrollTop + "px";
    var allViewArray = document.forms[0].ALL_VIEWCD.value.split(",");
    for (var viewCnt = 0; viewCnt < allViewArray.length; viewCnt++) {
        document.getElementById(allViewArray[viewCnt]).style.display = "none";
    }
    document.getElementById(viewCd).style.display = "block";
    myObj.visibility = "visible";
}

function myHidden() {
    var allViewArray = document.forms[0].ALL_VIEWCD.value.split(",");
    for (var viewCnt = 0; viewCnt < allViewArray.length; viewCnt++) {
        document.getElementById(allViewArray[viewCnt]).style.display = "none";
    }
    document.getElementById('myID_Menu').style.visibility = "hidden";
    switchDisabled();
}

function setClickValue(val, mark) {
    if (val != '999') {
        setObj.value = mark;
    }
    myHidden();
    setObj.focus();
}

function switchDisabled() {
    obj = document.getElementById("NYURYOKU1");
    for (var i=0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name.match(/TYPE_DIV/)) {
            document.forms[0].elements[i].disabled = !obj.checked;
        }
    }
}
