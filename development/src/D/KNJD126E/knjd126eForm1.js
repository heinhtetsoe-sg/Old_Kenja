window.onload = init;
function init() {       //ウィンドウを開いたら呼ばれる関数
    switchDisabled();   //ラジオボタンを表示したり隠したり
}

function btn_submit(cmd, electdiv) {

    if (cmd == 'reset'){
        if (!confirm('{rval MSG106}'))
            return false;
    } else if (cmd == 'select1' || cmd == 'select2'){
        if (!confirm('{rval MSG108}')) {
            return;
        }
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
                    if (electdiv == '0' && !str.match(/1|2|3|4|5/)) {
                        alert('{rval MSG901}'+'「1～5」を入力して下さい。\n（評定）');
                        return;
                    } else if (electdiv != '0' && !str.match(/A|B|C/)) { 
                        alert('{rval MSG901}'+'「AまたはBまたはC」を入力して下さい。\n（評定）');
                        return;
                    }

                //観点1～5または6
                } else {
                    var checkStr = "/" + document.forms[0].SETSHOW.value.replace(/,/g, '|') + "/";
                    var errStr = document.forms[0].SETSHOW.value.replace(/,/g, '、');
                    if (!str.match(eval(checkStr))) { 
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
    document.forms[0].SELECT[0].disabled = disFlg;
    document.forms[0].SELECT[1].disabled = disFlg;
    document.forms[0].btn_reset.disabled = disFlg;
    document.forms[0].btn_back.disabled = disFlg;
    document.forms[0].btn_print.disabled = disFlg;
}

//背景色の変更
function background_color(obj){
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

    //評定（必修）
    if (electdiv == '0' && nam.match(/STATUS9./)) {
        if (!str.match(/1|2|3|4|5/)) {
            alert('{rval MSG901}'+'「1～5」を入力して下さい。');
            obj.focus();
            background_color(obj);
            return;
        }

    //観点1～5 & 評定（選択）
    } else {
        var checkStr = "/" + document.forms[0].SETSHOW.value.replace(/,/g, '|') + "/";
        var errStr = document.forms[0].SETSHOW.value.replace(/,/g, '、');
        if (!str.match(eval(checkStr))) {
            alert('{rval MSG901}'+'「' + errStr + '」を入力して下さい。');
            obj.focus();
            background_color(obj);
            return;
        }
    }
}

//印刷
function newwin(SERVLET_URL){

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

//観点①～⑤または⑥へマウスを乗せた場合、観点名称をチップヘルプで表示
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
}

function ViewcdMouseout(){
    document.all["lay"].style.visibility = "hidden";
}


function kirikae(obj, showName) {
    setValue(obj, showName, document.forms[0].NYURYOKU[1].checked);
}

function kirikae2(obj, showName) {
    event.cancelBubble = true
    event.returnValue = false;
    clickList(obj, showName);
}

//値をセット
function setValue(obj, showName, clearCheck) {
    if (clearCheck) {
        obj.value = "";
    } else {
        innerName = showName;
        typeValArray = document.forms[0].SETVAL.value.split(",");
        typeShowArray = document.forms[0].SETSHOW.value.split(",");

        for (var i = 0; i < document.forms[0].TYPE_DIV.length; i++) {
            typeDiv = document.forms[0].TYPE_DIV[i];
            if (typeDiv.checked) {
                obj.value = typeShowArray[typeDiv.value - 1];
            }
        }
    }
}

function clickList(obj, showName) {
    innerName = showName;

    setObj = obj;
    myObj = document.forms[0].all["myID_Menu"].style;
    myObj.left = window.event.clientX + document.body.scrollLeft + "px";
    myObj.top  = window.event.clientY + document.body.scrollTop + "px";
    myObj.visibility = "visible";
}

function myHidden() {
    document.all["myID_Menu"].style.visibility = "hidden";
    switchDisabled();
}

function setClickValue(val) {
    if (val != '999') {
        typeShowArray = document.forms[0].SETSHOW.value.split(",");
        setObj.value = typeShowArray[val - 1];
        typeValArray = document.forms[0].SETVAL.value.split(",");
    }
    myHidden();
    setObj.focus();
}

//disabled（入力方法の値）
function switchDisabled() {
    obj = document.getElementById("NYURYOKU1");
    for (var i=0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name.match(/TYPE_DIV/)) {
            document.forms[0].elements[i].disabled = !obj.checked;
        }
    }
}

function close_window1()
{
    alert('名称マスタD028の登録をして下さい。');
    closeWin();
}

function close_window2()
{
    alert('名称マスタD029の登録をして下さい。');
    closeWin();
}


