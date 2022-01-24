function btn_submit(cmd) {

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//体調１～７へマウスを乗せた場合、質問内容をチップヘルプで表示
function ViewcdMousein(e, msg_no){
    var msg = "";
    if (msg_no==1) msg = "昨日はよく眠れたか？";
    if (msg_no==2) msg = "今朝、便はでたか？";
    if (msg_no==3) msg = "朝食は食べたか？";
    if (msg_no==4) msg = "疲れている感じはあるか？";
    if (msg_no==5) msg = "最近心配なこと、気にかかっていること。";

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

//2:一般より--//
function syokenNyuryoku(obj, target_obj) {
    if (obj.value == '') {
        var select_no = 0;
    } else {
        var select_no = parseInt(obj.value.replace(/^0+/, ""));
    }

    if (select_no < 2) {
        if (target_obj.value) {
            alert("テキストデータは更新時に削除されます");
        }
        target_obj.disabled = true;
    } else {
        target_obj.disabled = false;
    }
}

//数値かどうかをチェック
function Num_Check(obj) {
    var name = obj.name;
    var checkString = obj.value;
    var newString ="";
    var count = 0;

    for (i = 0; i < checkString.length; i++) {
        ch = checkString.substring(i, i+1);
        if ((ch >= "0" && ch <= "9") || (ch == ".")) {
            newString += ch;
        }
    }
    if (checkString != newString) {
        alert('{rval MSG901}\n数値を入力してください。');
        obj.value="";
        obj.focus();
        return false;
    }
}

function Mark_Check(obj) {
    var mark = obj.value;
    var printKenkouSindanIppan = document.forms[0].printKenkouSindanIppan.value;
    var msg = "A～Dを入力して下さい。";
    if (printKenkouSindanIppan == '2') {
        msg = "A～D,未を入力して下さい。";
    }
    switch(mark) {
        case "a":
        case "A":
        case "ａ":
        case "Ａ":
            obj.value = "A";
            break;
        case "b":
        case "B":
        case "ｂ":
        case "Ｂ":
            obj.value = "B";
            break;
        case "c":
        case "C":
        case "ｃ":
        case "Ｃ":
            obj.value = "C";
            break;
        case "d":
        case "D":
        case "ｄ":
        case "Ｄ":
            obj.value = "D";
            break;
        case "":
            obj.value = "";
            break;
        case "未":
            if (printKenkouSindanIppan != '2') {
                alert(msg);
                obj.value = "";
            }
            break;
        default:
            alert(msg);
            obj.value = "";
            break;
    }
}

//3:歯・口腔より-----//
//チェックボックスのラベル表示（有・無）
function checkAri_Nasi(obj, id) {
    var ari_nasi = document.getElementById(id);
    if (obj.checked) {
        ari_nasi.innerHTML = '有';
    } else {
        ari_nasi.innerHTML = '無';
    }
}

//その他疾病及び異常
function OptionUse(obj) {
    if(obj.value == '99') {
        document.forms[0].OTHERDISEASE.disabled = false;
        document.forms[0].OTHERDISEASE.style.backgroundColor = "#ffffff";
    } else {
        document.forms[0].OTHERDISEASE.disabled = true;
        document.forms[0].OTHERDISEASE.style.backgroundColor = "#D3D3D3";
    }
}
var xmlhttp = null;

/* XMLHttpRequest生成 */
function createXmlHttp() {
    if( document.all ) {
        return new ActiveXObject("Microsoft.XMLHTTP");
    } else if( document.implementation ) {
        return new XMLHttpRequest();
    } else{
        return null;
    }
}

/* POSTによるデータ送信 */
function chgDataSisikiUp(schregno) {

    /* XMLHttpRequestオブジェクト作成 */
    if( xmlhttp == null ) {
        xmlhttp = createXmlHttp();
    } else {
        /* 既に作成されている場合、通信をキャンセル */
        xmlhttp.abort();
    }

    /* 入力フォームデータの処理 */
    var postdata = new String();
    postdata = "cmd=send";
    postdata += "&SCHREGNO="+schregno;
    /* レスポンスデータ処理方法の設定 */
    xmlhttp.onreadystatechange = function(){handleHttpEvent(schregno)};//こうすると引数が渡せる
    /* HTTPリクエスト実行 */
    xmlhttp.open("POST", "knjf020index.php" , true);
    xmlhttp.setRequestHeader("Content-Type" , "application/x-www-form-urlencoded");
    xmlhttp.send(postdata);
}

/* レスポンスデータ処理 */
function handleHttpEvent(schregno) {
    if( xmlhttp.readyState == 4 ) {
        if( xmlhttp.status == 200 ) {
            var brackBabytooth  = document.forms[0].BRACK_BABYTOOTH;
            var brackAdulttooth = document.forms[0].BRACK_ADULTTOOTH;
            var checkAdultTooth = document.forms[0].CHECKADULTTOOTH;
            var dentistRemarkCo = document.forms[0].DENTISTREMARK_CO;

            var json = xmlhttp.responseText;
            var response;
//var debug = document.getElementById('debug');
//debug.innerHTML = json;
            eval('response = ' + json); //JSON形式のデータ(オブジェクトとして扱える)

            //戻り値
            if (response.result) {
                brackBabytooth.value  = response.BRACK_BABYTOOTH;
                brackAdulttooth.value = response.BRACK_ADULTTOOTH;
                checkAdultTooth.value = response.CHECKADULTTOOTH;
                dentistRemarkCo.value = response.DENTISTREMARK_CO;
            } else {
                brackBabytooth.value  = "";
                brackAdulttooth.value = "";
                checkAdultTooth.value = "";
                dentistRemarkCo.value = "";
            }
        } else {
            window.alert("通信エラーが発生しました。");
        }
    }
}
