function btn_submit(cmd, electdiv) {

    if (cmd == 'reset2'){
        if (!confirm('{rval MSG106}'))
            return false;
    } else if (cmd == 'update'){
        var kantenHyouji_5 = document.forms[0].kantenHyouji_5.value;
        var kantenHyouji_6 = document.forms[0].kantenHyouji_6.value;
        for (var i = 0; i < document.forms[0].elements.length; i++ ) {
            var e = document.forms[0].elements[i];
            if (e.type == 'text' && e.value != '') {
                var str = e.value;
                var nam = e.name;
                if (document.forms[0].HENKAN_TYPE.value == "1") {
                    //英小文字から大文字へ自動変換
                    e.value = str.toUpperCase();
                    str = str.toUpperCase();
                } else if (document.forms[0].HENKAN_TYPE.value == "2") {
                    //英大文字から小文字へ自動変換
                    e.value = str.toLowerCase();
                    str = str.toLowerCase();
                }

                //評定
                if (nam.match(/STATUS9./)) {
                    //選択科目
                    if (electdiv != '0' && !str.match(/A|B|C/)) { 
                        alert('{rval MSG901}'+'「AまたはBまたはC」を入力して下さい。\n（評定）');
                        //background_color(e);
                        return;
                    } else if (electdiv == '0' && !str.match(/1|2|3|4|5/)) {
                        alert('{rval MSG901}'+'「1～5」を入力して下さい。\n（評定）');
                        //background_color(e);
                        return;
                    }

                //観点1～5または6
                } else {
                    var checkStr = document.forms[0].SETSHOW.value.replace(/,/g, '|');
                    var errStr = document.forms[0].SETSHOW.value.replace(/,/g, '、');
                    re = new RegExp(checkStr);
                    if (!String(str).match(re)) {
                        if (kantenHyouji_5 == 1) {
                            alert('{rval MSG901}'+'「' + errStr + '」を入力して下さい。\n（観点①～⑤）');
                        } else {
                            alert('{rval MSG901}'+'「' + errStr + '」を入力して下さい。\n（観点①～⑥）');
                        }
                        return;
                    }
                }
            }
        }
        clickedBtnUdpate(true);
    } else if (cmd == 'updateRecordSeme'){
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
    var sk_flg = false;
    if (document.forms[0].use_prg_schoolkind.value == "1" && document.forms[0].SCHOOL_KIND.type != "hidden") {
        sk_flg = true;
    }

    if (disFlg) {
        if (sk_flg) document.forms[0].H_SCHOOL_KIND.value = document.forms[0].SCHOOL_KIND.value;
        document.forms[0].H_SEMESTER.value = document.forms[0].SEMESTER.value;
        document.forms[0].H_CLASSCD.value = document.forms[0].CLASSCD.value;
        document.forms[0].H_CHAIRCD.value = document.forms[0].CHAIRCD.value;
    } else {
        if (sk_flg) document.forms[0].SCHOOL_KIND.value = document.forms[0].H_SCHOOL_KIND.value;
        document.forms[0].SEMESTER.value = document.forms[0].H_SEMESTER.value;
        document.forms[0].CLASSCD.value = document.forms[0].H_CLASSCD.value;
        document.forms[0].CHAIRCD.value = document.forms[0].H_CHAIRCD.value;
    }
    if (sk_flg) document.forms[0].SCHOOL_KIND.disabled = disFlg;
    document.forms[0].SEMESTER.disabled = disFlg;
    document.forms[0].CLASSCD.disabled = disFlg;
    document.forms[0].CHAIRCD.disabled = disFlg;
    document.forms[0].SELECT[0].disabled = disFlg;
    document.forms[0].SELECT[1].disabled = disFlg;
    document.forms[0].btn_reset.disabled = disFlg;
    document.forms[0].btn_end.disabled = disFlg;
    document.forms[0].btn_print.disabled = disFlg;
}

function background_color(obj){
//    obj.style.background='#ffff99';
    obj.style.background='#ffffff';
}
//入力チェック
function calc(obj, electdiv){

    var str = obj.value;
    var nam = obj.name;
    
    //空欄
    if (str == '') { 
        return;
    }
    if (document.forms[0].HENKAN_TYPE.value == "1") {
        //英小文字から大文字へ自動変換
        obj.value = str.toUpperCase();
        str = str.toUpperCase();
    } else if (document.forms[0].HENKAN_TYPE.value == "2") {
        //英大文字から小文字へ自動変換
        obj.value = str.toLowerCase();
        str = str.toLowerCase();
    }

    //評定
    if (nam.match(/STATUS9./)) {
        //選択科目
        if (electdiv != '0' && !str.match(/A|B|C/)) { 
            alert('{rval MSG901}'+'「AまたはBまたはC」を入力して下さい。');
            obj.value = "";
            obj.focus();
            background_color(obj);
            return;
        } else if (electdiv == '0' && !str.match(/1|2|3|4|5/)) {
            alert('{rval MSG901}'+'「1～5」を入力して下さい。');
            obj.value = "";
            obj.focus();
            background_color(obj);
            return;
        }

    //観点1～5または6
    } else {
        var checkStr = document.forms[0].SETSHOW.value.replace(/,/g, '|');
        var errStr = document.forms[0].SETSHOW.value.replace(/,/g, '、');
        re = new RegExp(checkStr);
        if (!String(str).match(re)) {
            alert('{rval MSG901}'+'「' + errStr + '」を入力して下さい。');
            obj.value = "";
            obj.focus();
            background_color(obj);
            return;
        }
    }
}
//印刷
function newwin(SERVLET_URL){

    if (document.forms[0].CHAIRCD.value == '') {
        alert('クラス・講座を指定してください。');
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

//観点①～⑤へマウスを乗せた場合、観点名称をチップヘルプで表示
function ViewcdMousein(e, msg_no){
    var msg = "";
    if (msg_no==1) msg = document.forms[0].VIEWCD1.value;
    if (msg_no==2) msg = document.forms[0].VIEWCD2.value;
    if (msg_no==3) msg = document.forms[0].VIEWCD3.value;
    if (msg_no==4) msg = document.forms[0].VIEWCD4.value;
    if (msg_no==5) msg = document.forms[0].VIEWCD5.value;
    if (msg_no==6) msg = document.forms[0].VIEWCD6.value;
    
    x = event.clientX+document.body.scrollLeft;
    y = event.clientY+document.body.scrollTop;
    document.all("lay").innerHTML = msg;
    document.all["lay"].style.position = "absolute";
    document.all["lay"].style.left = x+5;
    document.all["lay"].style.top = y+10;
    document.all["lay"].style.padding = "4px 3px 3px 8px";
    document.all["lay"].style.border = "1px solid";
    document.all["lay"].style.visibility = "visible";
    document.all["lay"].style.background = "#ccffff";
    //document.all["lay"].style.cursor = "hand";
}

function ViewcdMouseout(){
    document.all["lay"].style.visibility = "hidden";
}

/************************************************* 貼付け関係 ***********************************************/
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

/****************************************/
/* 実際に貼付けを実行する関数           */
/* 貼付け時に必要な処理(自動計算とか)は */
/* ここに書きます。                     */
/****************************************/
function execCopy(targetObject, val, targetNumber) {
    if (targetObject.value != val) {
        targetObject.style.background = '#ccffcc';
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
    var electdiv = document.forms[0].ELECTDIV.value;
    var kantenHyouji_5 = document.forms[0].kantenHyouji_5.value;
    var kantenHyouji_6 = document.forms[0].kantenHyouji_6.value;
        
    for (j = 0; j < clipTextArray.length; j++) { //クリップボードの各行をループ
        i = 0;
        startFlg = false;
        for (k = 0; k < objectNameArray.length; k++) { //テキストボックス名でまわす
            if (objectNameArray[k] == targetName) { //貼付け開始対象のテキストボックスならばフラグを立てる
                startFlg = true;
            }
            if (startFlg) {
                if (clipTextArray[j][i] != undefined && clipTextArray[j][i] != '') {
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
                        //選択科目
                        if (electdiv != '0' && str != "A" && str != "B" && str != "C") { 
                            alert('{rval MSG901}'+'「AまたはBまたはC」を入力して下さい。\n（評定）');
                            return false;
                        } else if (electdiv == '0' && str != "1" && str != "2" && str != "3" && str != "4" && str != "5") {
                            alert('{rval MSG901}'+'「1～5」を入力して下さい。\n（評定）');
                            return false;
                        }
                    //観点1～5または6
                    } else {
                        var checkStr = document.forms[0].SETSHOW.value.replace(/,/g, '|');
                        var errStr = document.forms[0].SETSHOW.value.replace(/,/g, '、');
                        re = new RegExp(checkStr);
                        if (!String(str).match(re)) {
                            if (kantenHyouji_5 == 1) {
                                alert('{rval MSG901}'+'「' + errStr + '」を入力して下さい。\n（観点①～⑤）');
                            } else {
                                alert('{rval MSG901}'+'「' + errStr + '」を入力して下さい。\n（観点①～⑥）');
                            }
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
	for(var i=1;i<document.forms[0].elements.length+1;i++){
		if(idx+i>=document.forms[0].elements.length){
			var idx2=idx+i-(document.forms[0].elements.length);
		} else {
			var idx2=idx+i;
		}
		if(document.forms[0].elements[idx2].type=='text'){
			return document.forms[0].elements[idx2];
		}
	}
	return false;
}

function nextElement2(idx){
	if(document.forms[0].elements[idx].type!='text'){
		return false;
	}
	var id = document.forms[0].elements[idx].id;
	if(id.indexOf('STATUS')===-1){
		return false;
	}
	idArray = id.replace(/STATUS/,'').split('-');
	if(idArray.length!=2){
		return false;
	}
	var StatusArray = new Array();
	var key=null;
	for(var i=0;i<document.forms[0].elements.length+1;i++){
		if(document.forms[0].elements[i].id.indexOf('STATUS') !== -1){
			var idArray2 = document.forms[0].elements[i].id.replace(/STATUS/,'').split('-');
			if(key==null){
				key=idArray2[1];
			} else if(key != idArray2[1]){
				break;
			}
			var flag = false;
			for(var j=0;j<StatusArray.length;j++){
				if(StatusArray[j]==idArray2[0]){
					flag = true;
					break;
				}
			}
			if(!flag){
				StatusArray.push(idArray2[0]);
			}
		}
	}
	if(document.forms[0].elements['STATUS'+idArray[0]+'-'+(parseInt(idArray[1])+1)]){
		return document.forms[0].elements['STATUS'+idArray[0]+'-'+(parseInt(idArray[1])+1)];
	} else {
		for(var i=0;i<StatusArray.length;i++){
			if(StatusArray[i]==idArray[0]){
				if(StatusArray[i+1]){
					if(document.forms[0].elements['STATUS'+StatusArray[i+1]+'-0']){
						return document.forms[0].elements['STATUS'+StatusArray[i+1]+'-0'];
					} else {
						return false;
					}
				} else {
					if(document.forms[0].elements['STATUS1-0']){
						return document.forms[0].elements['STATUS1-0'];
					} else {
						return false;
					}
				}
			}
		}
	}
	return false;
}
window.onkeydown = doKeyDown;