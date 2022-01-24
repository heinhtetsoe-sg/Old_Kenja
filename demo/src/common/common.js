<!--
// CHECK STRING - ENSURE ALL CHARACTERS ARE LETTERS
function charCount(val, gyosu, itigyou_no_mojiLen, dispMsg)
{
    //改行コードで区切って配列に入れていく
    stringArray = new Array();
    stringArray = val.split("\r\n");

    row_cnt = 0;
    gyousu = 1;
    //改行コードが現れるまでに何行消費するか数える
    for (var i = 0; i < stringArray.length; i++) {
        mojisu = stringArray[i].length;
        mojiLen = 0;
        for (var j = 0; j < mojisu; j++) {
            hitoMoji = stringArray[i].charAt(j);
            moji_hantei = escape(hitoMoji).substr(0,2);
            mojiLen += moji_hantei == "%u" ? 2 : 1;
        }
        amari = mojiLen % itigyou_no_mojiLen;
        gyousu = (mojiLen - amari) / itigyou_no_mojiLen;
        if (amari > 0) {
            gyousu++;
        }
        if (gyousu) {
            row_cnt += gyousu;
        } else {
            row_cnt++;
        }
    }
    var retArray = Array();
    retArray["GYOUSU"] = row_cnt;
    if (row_cnt > gyosu) {
        if (dispMsg) {
            alert('行数を超えています。'+gyosu+'行以内で入力して下さい。');
        }
        retArray["FLG"] = false;
        return retArray;
    }
    retArray["FLG"] = true;
    return retArray;
}
function toAlpha(checkString){
    newString = "";    // 正しい文字列
    count = 0;         // ループ用カウンタ
    // 渡された文字列の長さを引数としてループ
    for (i = 0; i < checkString.length; i++) {
        ch = checkString.substring(i, i+1);

        // ENSURE CHARACTER IS AN ALPHA CHARACTER
        if ((ch >= "a" && ch <= "z") || (ch >= "A" && ch <= "Z" )) {
            newString += ch;
        }
    }
     return ShowDialog(newString,checkString,"英字");
}
function toAlphaNumber(checkString){
    var newString = "";
    var count = 0;
    for (i = 0; i < checkString.length; i++) {
        ch = checkString.substring(i, i+1);
        if ((ch >= "a" && ch <= "z") || (ch >= "A" && ch <= "Z") ||
            (ch >= "0" && ch <= "9")) {
            newString += ch;
        }
    }
       return ShowDialog(newString,checkString,"英数字");
}
function toAlphanumeric(checkString){
    var newString = "";
    var count = 0;
    for (i = 0; i < checkString.length; i++) {
        ch = checkString.substring(i, i+1);
        if (ch >= " " && ch <= "~") {
            newString += ch;
        }
    }
    return ShowDialog(newString,checkString,"英数字・記号");
}
function toFloat(checkString){
    var newString = "";
    var count = 0;
    for (i = 0; i < checkString.length; i++) {
        ch = checkString.substring(i, i+1);
        if ((ch >= "0" && ch <= "9") || (ch == ".")) {
            newString += ch;
        }
    }
    return ShowDialog(newString,checkString,"浮動小数点数");
}
function toInteger(checkString){
    var newString = "";
    var count = 0;
    for (i = 0; i < checkString.length; i++) {
        ch = checkString.substring(i, i+1);
        if (ch >= "0" && ch <= "9") {
            newString += ch;
        }
    }
    return ShowDialog(newString,checkString,"数字");
}
function toNumber(checkString){
    var newString = "";
    var count = 0;
    for (i = 0; i < checkString.length; i++) {
        ch = checkString.substring(i, i+1);
        if ((ch >= "0" && ch <= "9") || (ch == ".") || (ch == ",")) {
            newString += ch;
        }
    }
    return ShowDialog(newString,checkString,"数値");
}
function toNumberMinus(checkString){
    var newString = "";
    var count = 0;
    for (i = 0; i < checkString.length; i++) {
        ch = checkString.substring(i, i+1);
        if ((ch >= "0" && ch <= "9") || (ch == ".") || (ch == ",") || (i == 0 && ch == "-")) {
            newString += ch;
        }
    }
    return ShowDialog(newString,checkString,"数値");
}
function toMoney(checkString){
    var newString = "";
    var count = 0;
    for (i = 0; i < checkString.length; i++) {
        ch = checkString.substring(i, i+1);
        if ((ch >= "0" && ch <= "9") || (ch == ",")) {
            if (ch == ",") continue;
            newString += ch;
        }
    }
    checkString = checkString.replace(",", "");
    return ShowDialog(number_format(newString),number_format(checkString),"数値");
}
function toTelNo(checkString){
    var newString = "";
    var count = 0;
    for (i = 0; i < checkString.length; i++) {
        ch = checkString.substring(i, i+1);
        if ((ch >= "0" && ch <= "9") || (ch == "-")) {
            newString += ch;
        }
    }
    return ShowDialog(newString,checkString,"電話(FAX)番号");
}
function ShowDialog(newString, checkString,STR){
    if (checkString != newString) {
        // VERIFY WITH USER THAT IT IS OKAY TO REMOVE INVALID CHARACTERS
        alert("入力された値は不正な文字列です。\n"+STR+"を入力してください。\n入力された文字列は削除されます。");
        // 文字列を返す
        return newString;
    }
    return checkString;
}
function checkEmail(checkString){
    var newstr = "";
    var at = false;
    var dot = false;

    if (checkString.length > 0) {

        if (checkString.indexOf("@") != -1) {
            at = true;
        } else if (checkString.indexOf(".") != -1) {
            dot = true;
        }
        for (var i = 0; i < checkString.length; i++) {
            ch = checkString.substring(i, i + 1);
            if ((ch >= "A" && ch <= "Z") || (ch >= "a" && ch <= "z")
                || (ch == "@") || (ch == ".") || (ch == "_")
                || (ch == "-") || (ch >= "0" && ch <= "9")) {
                newstr += ch;
                if (ch == "@") {
                    at=true;
                }
                if (ch == ".") {
                    dot=true;
                }
            }
        }
        if ((at == true) && (dot == true)) {
            return newstr;
        } else {
            alert ("入力されたメールアドレスは\n不正なアドレスのようです。\n再度入力し直してください。");
//            return checkString;
            return newstr;
        }
    }
    return checkString;
}
//全角数値から半角数値へ変換する
function toHankakuNum(motoText)
{
    han = "0123456789.,-+";
    zen = "０１２３４５６７８９．，－＋";
    str = "";
    for (i=0; i<motoText.length; i++)
    {
        c = motoText.charAt(i);
        n = zen.indexOf(c,0);
        if (n >= 0) c = han.charAt(n);
        str += c;
    }
    return str;
}
//半角カナから全角カナへ変換する
function toZenkaku(motoText){
    txt = "渦慨偽係杭纂従神疎団兎波品北洋椀冫嘖孛忤掣桀・・・⑭・＾■渤";
    zen = "アイウエオカキクケコサシスセソタチツテトナニヌネノハヒフヘホマミムメモヤユヨラリルレロワヲンァィゥェォャュョッ、。ー「」";
    zen+= "　　　　　ガギグゲゴザジズゼゾダヂヅデド　　　　　バビブベボ　　　　　　　　　　　　　　　　　　　　　　　　　　　　　";
    zen+= "　　　　　　　　　　　　　　　　　　　　　　　　　パピプペポ　　　　　　　　　　　　　　　　　　　　　　　　　　　　　";
    str = "";
    for (i=0; i<motoText.length; i++){
        c = motoText.charAt(i);
        cnext = motoText.charAt(i+1);
        n = txt.indexOf(c,0);
        nnext = txt.indexOf(cnext,0);
        if (n >= 0){
            if (nnext == 60){
                c = zen.charAt(n+60);
                i++;
            }else if (nnext == 61){
                c = zen.charAt(n+120);
                i++;
            }else{
                c = zen.charAt(n);
            }
        }
        if ((n != 60) && (n != 61)){
            str += c;
        }
    }
    return str;
}

// 日付妥当性チェック
function isDate(obj) {
    var datestr = obj.value;

    // 正規表現による書式チェック
    if (datestr.match(/^\d{4}\/\d{2}\/\d{2}$/) == null && datestr != "") {
        alert("日付の書式が不正です。");
        obj.value = obj.defaultValue;
        return false;
    }else if(datestr == ""){
        return true;
    }

    var vYear  = parseInt(eval(datestr.substr(0, 4))) - 0;
    var vMonth = parseInt(eval(datestr.substr(5, 2))) - 1; // Javascriptは、0-11で表現
    var vDay   = parseInt(eval(datestr.substr(8, 2))) - 0;

    // 月,日の妥当性チェック
    if (vMonth >= 0 && vMonth <= 11 && vDay >= 1 && vDay <= 31) {
        var vDt = new Date(vYear, vMonth, vDay);

        if (isNaN(vDt) == true) {
            alert("日付が不正です。");
            obj.value = obj.defaultValue;
            return false;
        } else if (vDt.getFullYear() == vYear && vDt.getMonth() == vMonth && vDt.getDate() == vDay){
            return true;
        }else{
            alert("日付が不正です。");
            obj.value = obj.defaultValue;
            return false;
        }
    } else {
        alert("日付の書式が不正です。");
        obj.value = obj.defaultValue;
        return false;
    }
}

var newWin;

function wopen(URL,winName,x,y,w,h){

    var para =""
             +" left="        +x
             +",screenX="     +x
             +",top="         +y
             +",screenY="     +y
             +",toolbar="     +0
             +",location="    +0
             +",directories=" +0
             +",status="      +1
             +",menubar="     +0
             +",scrollbars="  +1
             +",resizable="   +1
             +",innerWidth="  +w
             +",innerHeight=" +h
             +",width="       +w
             +",height="      +h;

    if(sbwin_closed(newWin)){
        newWin = window.open(URL,winName,para);
    }else{
        newWin.location.href=URL;
    }
    newWin.focus();
}
//子ウインドウ有無確認関数
function sbwin_closed(winVar) {

    var ua = navigator.userAgent;
    if( !!winVar )
        if (ua.indexOf('Trident') === -1)
             return winVar.closed;
        else return typeof winVar.document  != 'object';
    else return true;

}
function is_opener() {

    var ua = navigator.userAgent;
    if(!!window.opener)
        if (ua.indexOf('Trident') === -1)
             return !window.opener.closed;
        else return typeof window.opener.document  == 'object';
    else return false;

}
//カレンダー
function subWinOpen_cal(name,param){

    var x=100;
    var y=20;
    var w=270;
    var h=200;

    if(document.all){

        x=window.event.screenX;
        y=window.event.screenY;

    } else if (document.layers || document.getElementById){

        x+=window.screenX;
        y+=window.screenY;
    }
    if (x+w > screen.availWidth){
        x-=w;
    }
    if (y+w > screen.availHeight){
        y-=h;
    }
    URL = '../../common/cal.php?date=' + document.forms[0][name].value + '&' + param;
    winName = 'calendar';

    var para =""
             +" left="        +x
             +",screenX="     +x
             +",top="         +y
             +",screenY="     +y
             +",toolbar="     +0
             +",location="    +0
             +",directories=" +0
             +",status="      +0
             +",menubar="     +0
             +",scrollbars="  +0
             +",resizable="   +0
             +",innerWidth="  +w
             +",innerHeight=" +h
             +",width="       +w
             +",height="      +h;



    if(sbwin_closed(newWin)){
         newWin = window.open(URL,winName,para);
    }else{
        newWin.location.href=URL;

    }
    newWin.focus();
}

//郵便番号入力支援
function subWinOpen_zip(name,name2){

    var x=100;
    var y=20;
    var w=550;
    var h=240;

    if(document.all){
        x=window.event.screenX;
        y=window.event.screenY;

    } else if (document.layers || document.getElementById){

        x+=window.screenX;
        y+=window.screenY;
    }
    if (x+w > screen.availWidth){
        x-=w;
    }
    if (y+w > screen.availHeight){
        y-=h;
    }

    URL = '../../common/zipcd.php?addrname='+name+'&zipname='+name2;
    winName = 'zipcd';
    var para =""
             +" left="        +x
             +",screenX="     +x
             +",top="         +y
             +",screenY="     +y
             +",toolbar="     +0
             +",location="    +0
             +",directories=" +0
             +",status="      +0
             +",menubar="     +0
             +",scrollbars="  +0
             +",resizable="   +0
             +",innerWidth="  +w
             +",innerHeight=" +h
             +",width="       +w
             +",height="      +h;

    if(sbwin_closed(newWin)){
        newWin = window.open(URL,winName,para);
    }else{
        newWin.location.href=URL;
    }
    newWin.focus();
}

//生徒検索入力支援
function subWinOpen_stu(target){

    var x=200;
    var y=200;
    var w=550;
    var h=500;

    if(document.all){
        x=window.event.screenX;
        y=window.event.screenY;

    } else if (document.layers || document.getElementById){

        x+=window.screenX;
        y+=window.screenY;
    }
    if (x+w > screen.availWidth){
        x-=w;
    }
    if (y+w > screen.availHeight){
        y-=h;
    }

    URL = '../../common/stucd.php?target=' + target;
    winName = 'stucd';
    var para =""
             +" left="        +x
             +",screenX="     +x
             +",top="         +y
             +",screenY="     +y
             +",toolbar="     +0
             +",location="    +0
             +",directories=" +0
             +",status="      +0
             +",menubar="     +0
             +",scrollbars="  +0
             +",resizable="   +0
             +",innerWidth="  +w
             +",innerHeight=" +h
             +",width="       +w
             +",height="      +h;

    if(sbwin_closed(newWin)){
        newWin = window.open(URL,winName,para);
    }else{
        newWin.location.href=URL;
    }
    newWin.focus();
}

//メニュー画面が存在するかチェック
function observeDisp(){
    if (!is_opener()){
        parent.window.close();
    }
}
//ウインドウを閉じる
function closeWin(){
    top.window.close();
}
//確認メッセージを出力してウインドウを閉じる
function closeMsg(){
    if (document.forms[0].changeVal !== undefined && document.forms[0].changeVal.value == '1') {
        if(!confirm('保存されていないデータは破棄されます。処理を続行しますか？')){
            return;
        } else {
            top.window.close();
        }
    } else {
        top.window.close();
    }
}

//ウィンドウを画面いっぱいのサイズにします。
function fullsize(){
    window.resizeTo(window.screen.width, window.screen.height);
    window.moveTo(0,0);
    window.focus();
}
//レイヤー内のHTMLをタグごと書き換えるためのサンプルファンクション
function outputLAYER(layName,html){
    if(document.getElementById){        //N6,Moz,IE5,IE6用
        document.getElementById(layName).innerHTML=html;

    } else if(document.all){                       //IE4用
        document.all(layName).innerHTML=html;

    } else if(document.layers) {                   //NN4用
        with(document.layers[layName].document){
            open();
                write(html);
            close();
        }
    }

}
function getByte(s)
{
    var r = 0;
    for (var i = 0; i < s.length; i++) {
        var c = s.charCodeAt(i);
        // Shift_JIS: 0x0 ～ 0x80, 0xa0  , 0xa1   ～ 0xdf  , 0xfd   ～ 0xff
        // Unicode  : 0x0 ～ 0x80, 0xf8f0, 0xff61 ～ 0xff9f, 0xf8f1 ～ 0xf8f3
        if ( (c >= 0x0 && c < 0x81) || (c == 0xf8f0) || (c >= 0xff61 && c < 0xffa0) || (c >= 0xf8f1 && c < 0xf8f4)) {
            r += 1;
        } else {
            r += 2;
        }
    }
    return r;
}
function getLength(str)
{
    return str.length;
}
//ウインドウサイズ取得
function GetWindowSize(type){
    switch(type){
        case "width":
            if(document.all){
                return(document.body.clientWidth);
            }else if(document.layers){
                return(innerWidth);
            }else{
                return(-1);
            }
        break;
        case "height":
            if(document.all){
                return(document.body.clientHeight);
            }else if(document.layers){
                return(innerHeight);
            }else{
                return(-1);
            }
        break;
        default:
            return(-1);
        break;
    }
}
function saveCookie(arg1,arg2,arg3,arg4){ //arg1=dataname arg2=data arg3=expiration days
    if(arg1&&arg2){
        if(arg3){
            xDay = new Date;
            xDay.setDate(xDay.getDate() + eval(arg3));
            xDay = xDay.toGMTString();
            _exp = ";expires=" + xDay;
        }
        else _exp ="";
        if(arg4){
            _path = ";path=" + arg4;
        }
        else _path= "";
        document.cookie = escape(arg1) + "=" + escape(arg2) + _exp + _path +";";
    }
}

function loadCookie(arg){ //arg=dataname
    if(arg){
        cookieData = document.cookie + ";" ;
        arg = escape(arg);
        startPoint1 = cookieData.indexOf(arg);
        startPoint2 = cookieData.indexOf("=",startPoint1) +1;
        endPoint = cookieData.indexOf(";",startPoint1);
        if(startPoint2 < endPoint && startPoint1 > -1 &&startPoint2-startPoint1 == arg.length+1){
            cookieData = cookieData.substring(startPoint2,endPoint);
            cookieData = unescape(cookieData);
            return cookieData;
        }
    }
    return false;
}
function deleteCookie(arg){ //arg=dataname
    if(arg){
        arg = escape(arg);
        yDay = new Date;
        yDay.setHours(yDay.getHours() - 1);
        yDay = yDay.toGMTString();
        document.cookie = arg + "=xxx" + ";expires=" + yDay;
    }
}
function chkCookie(arg){ //arg="name" or "value"
    if(document.cookie!=""){
        _cookie = " " + document.cookie ;
        cookieData = _cookie.split(";");
        cookieDataName = new Array(cookieData.length);
        cookieDataValue = new Array(cookieData.length);
    for(i=0;i<cookieData.length;i++){
        if(cookieData[i].indexOf("=") > -1){
            cookieDataName[i] = cookieData[i].substring(1,cookieData[i].indexOf("="));
            cookieDataValue[i] = cookieData[i].substring(cookieData[i].indexOf("=")+1,cookieData[i].length);
        }
    }
    if(arg=="name" ||arg=="NAME"||arg=="Name")return cookieDataName;
    if(arg=="value" ||arg=="VALUE"||arg=="Value")return cookieDataValue;
    else return cookieData.length;
    }
    else if(!arg) return 0;
    return false;
}
// 郵便番号チェック
function isZipcd(obj) {
    txt = obj.value;
    if (txt != ""){
        data = txt.match(/^\d{3}-\d{4}$|^\d{3}-\d{2}$|^\d{3}$/);
        if(!data){
            alert("郵便番号が不正です");
            obj.value = obj.defaultValue;
            return false;
        }
    }else{
        return false;
    }
    return true;
}
//権限チェック
function checkAuth(authority, programid){
    if (authority == '' || programid == '') return;
    if (authority == '0' && programid.match(/^KNJ[A-Z][0-9]{3}$/i)){
        alert("この処理は許可されていません。");
        top.window.close();
    }else if (authority < '4' && programid.match(/^KNJZ[0-9]{3}$/i)){
        alert("この処理は許可されていません。");
        top.window.close();
    }else if (authority < '3' && programid.match(/^KNJ[A-Z][0-9]{3}$/i)){
        for (i = 0; i < document.forms[0].elements.length; i++){
            var el = document.forms[0].elements[i];
            if (el.type == "button" && (el.value.match(/更|新/) || el.value.match(/追|加/) || el.value.match(/削|除/))){
                el.disabled = true;
            }
        }
    }
    return;
}
//リスト移動
function ClearList(OptionList)
{
    OptionList.length = 0;
}
function move(side, left, right, sort)
{
    var temp1 = new Array();
    var temp2 = new Array();
    var tempa = new Array();
    var tempb = new Array();
    var a = new Array();
    var current1 = 0;
    var current2 = 0;
    var y=0;
    var attribute1;
    var attribute2;

    //assign what select attribute treat as attribute1 and attribute2
    if (side == "right" || side == "sel_del_all")
    {
        attribute1 = document.forms[0][left];
        attribute2 = document.forms[0][right];
    }
    else
    {
        attribute1 = document.forms[0][right];
        attribute2 = document.forms[0][left];
    }
    //fill an array with old values
    for (var i = 0; i < attribute2.length; i++)
    {
        y=current1++;
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        a[tempa[y]] = temp1[y];
    }
    //assign new values to arrays
    for (var i = 0; i < attribute1.length; i++)
    {
        if (side == "right" || side == "left")
        {
            if ( attribute1.options[i].selected )
            {
                y=current1++;
                temp1[y] = attribute1.options[i].value;
                tempa[y] = attribute1.options[i].text;
                a[tempa[y]] = temp1[y];
            }
            else
            {
                y=current2++;
                temp2[y] = attribute1.options[i].value;
                tempb[y] = attribute1.options[i].text;
            }
        } else {

            y=current1++;
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text;
            a[tempa[y]] = temp1[y];
        }
    }
    if (sort){
        //sort
        tempa = tempa.sort();
        //generating new options
        for (var i = 0; i < tempa.length; i++)
        {
//            alert(a[tempa[i]]);
            temp1[i] = a[tempa[i]];
        }
    }

    //generating new options
    for (var i = 0; i < temp1.length; i++)
    {
        attribute2.options[i] = new Option();
        attribute2.options[i].value = temp1[i];
        attribute2.options[i].text =  tempa[i];
    }

    //generating new options
    ClearList(attribute1);
    if (temp2.length>0)
    {
        for (var i = 0; i < temp2.length; i++)
        {
            attribute1.options[i] = new Option();
            attribute1.options[i].value = temp2[i];
            attribute1.options[i].text =  tempb[i];
        }
    }

    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    sep = "";
    for (var i = 0; i < document.forms[0][left].length; i++)
    {
        attribute3.value = attribute3.value + sep + document.forms[0][left].options[i].value;
        sep = ",";
    }
}

//上下移動 moveMotion up:上移動 down:下移動
function moveUpDown(idName, moveMotion) {

    var selectbox = document.getElementById(idName);
    var firstSelected = document.forms[0][idName].selectedIndex;
    var option_list = selectbox.getElementsByTagName('option');
    selectbox.selectedIndex = -1;
    for (var i = 0; i < option_list.length; i++) {
        if (i == firstSelected) {
            option_list[i].selected = true;
        }
    }

    for (var i = 0; i < option_list.length; i++) {
        if (option_list[i].selected) {
            var moveIndex = moveMotion == 'up' ? (i - 1) : (i + 1);
            if (moveIndex <= option_list.length && moveIndex >= 0) {
                selectbox.insertBefore(selectbox.removeChild(option_list[i]), option_list[moveIndex]);
            }
            break;
        }
    }
}
//フレームオブジェクトを探す
function getFrameName(obj){
    for (var i = 0; i < top.length; i++){
        var  f1 = top.frames[i];
        if (f1 == obj){
            return "top." + f1.name;
        }
        for (var j = 0; j < f1.length; j++){
            f2 = f1[j];
            if (f1[j] == obj){
                return "top." + f1.name + '.' + f1[j].name;
            }
            for (var k = 0; k < f2.length; k++){
                if (f2[k] == obj){
                    return "top." + f1.name + '.' + f1[j].name + '.' + f2[k].name;
                }
            }
        }
    }
    return "top";
}

//メッセージなしの日付妥当チェック
function isDate2(obj) {
    var datestr = obj;

    // 正規表現による書式チェック
    if (datestr.match(/^\d{4}\/\d{2}\/\d{2}$/) == null && datestr != "") {
        return false;
    }else if(datestr == ""){
        return true;
    }

    var vYear  = parseInt(eval(datestr.substr(0, 4))) - 0;
    var vMonth = parseInt(eval(datestr.substr(5, 2))) - 1; // Javascriptは、0-11で表現
    var vDay   = parseInt(eval(datestr.substr(8, 2))) - 0;

    // 月,日の妥当性チェック
    if (vMonth >= 0 && vMonth <= 11 && vDay >= 1 && vDay <= 31) {
        var vDt = new Date(vYear, vMonth, vDay);

        if (isNaN(vDt) == true) {
            return false;
        } else if (vDt.getFullYear() == vYear && vDt.getMonth() == vMonth && vDt.getDate() == vDay){
            return true;
        }else{
            return false;
        }
    } else {
        return false;
    }
}


//Scriptのロードを行う
function loadScript(url) {
    var script = document.createElement('script');
    var dateObj = new Date();

    script.setAttribute('src', url+'?'+dateObj.getTime());
    script.setAttribute('charset', 'utf-8');
    if (document.body){
        document.body.appendChild(script);
    } else if (document.head) {
        document.head.appendChild(script);
    } else {
        document.write("<script type='text/javascript' src='" + url + "'></script>");
    }
}

//common配下のパスを取得
function getDocumentCommon() {

    var root;
    var scripts = document.getElementsByTagName("script");

    for (var i = 0; i < scripts.length; i++) {
        var script = scripts[i];
        // common以下のURL取得
        var match = script.src.match(/(^|.*\/)common\.js$/);
        if (match) {
            var port = "";
            var origin = "";
            if (location.origin){
                origin = location.origin;
                // ポート番号が指定されている場合排除
                portmatch = location.origin.match(/:[0-9]+$/);
                if (portmatch) {
                    port = portmatch[0];
                }
            }
            root = match[1].substring(origin.length - port.length);
            break;
        }
    }
    return root;
}

// 和暦情報保持用
var WarekiList = new Array();

function getWarekiListSync()
{
    if (WarekiList.length <= 0) {
        var DOCCOMMON = getDocumentCommon();
        var INCFILE = DOCCOMMON + 'js/jquery-1.11.0.min.js';
        loadScript(INCFILE);

        if ($.ajax) {
            $.ajax({
                url:DOCCOMMON + 'calendarajax.php',
                type:'POST',
                data:{
                    cmd :'getWarekiList'
                },
                async:false

            }).done(function(data,textStatus,jqXHR) {
                var warekilist = $.parseJSON(data);

                for (var i = 0; i < warekilist.length; i++) {
                    var warekiinfo = warekilist[i];
                    WarekiList.push({
                        'CD':warekiinfo['CD'],
                        'Name':warekiinfo['Name'],
                        'SName':warekiinfo['SName'],
                        'Start':warekiinfo['Start'],
                        'End':warekiinfo['End']
                    });
                }
            });
        }
    }
    return WarekiList;
}

//和暦に変換して表示
function Calc_Wareki(obj, ymd, fmt)
{
    if (ymd == '') {
        return '';
    }

    if (!isDate2(ymd)) {
       return '';
    }
    var t = ymd.split('/');
    var str_year = t[0].toString();
    var str_month = t[1].toString();
    var str_day = t[2].toString();

    var target;
    var ret_year = eval(str_year);
    var ret_val = "";

    // 和暦情報取得
    var warekiList = getWarekiListSync();

    target = str_year + str_month + str_day;
    for (var i = 0; i < warekiList.length; i++) {
        var info = warekiList[i];

        var startDate = info['Start'].replace(/\//g, "");
        var endDate = info['End'].replace(/\//g, "");

        if (startDate <= eval(target) && eval(target) <= endDate) {
            var startDate = new Date(info['Start']);
            var startYear = startDate.getFullYear();

            ret_year = (eval(str_year) - eval(startYear) + 1);

            if (fmt == 0) {
                ret_val = info['Name'] + ((ret_year == 1)? "元年" : ret_year + "年") + str_month + '月' + str_day + '日';
            } else if (fmt == 1) {
                ret_val = info['SName'] + ret_year + '.' + str_month + '.' + str_day;
            } else if (fmt == 2) {
                ret_val = info['SName'] + ret_year;
            }
            break;
        }
    }

    outputLAYER(obj,ret_val);
    return false;
}


// -------------------以下ALP作成------------------------

//数値を３桁カンマ区切りにする
function number_format(val) {
    var s = "" + val; // 確実に文字列型に変換する。例では "95839285734.3245"
    var p = s.indexOf("."); // 小数点の位置を0オリジンで求める。例では 11
    if (p < 0) { // 小数点が見つからなかった時
        p = s.length; // 仮想的な小数点の位置とする
    }
    var r = s.substring(p, s.length); // 小数点の桁と小数点より右側の文字列。例では ".3245"
    for (var i = 0; i < p; i++) { // (10 ^ i) の位について
        var c = s.substring(p - 1 - i, p - 1 - i + 1); // (10 ^ i) の位のひとつの桁の数字。例では "4", "3", "7", "5", "8", "2", "9", "3", "8", "5", "9" の順になる。
        if (c < "0" || c > "9") { // 数字以外のもの(符合など)が見つかった
            r = s.substring(0, p - i) + r; // 残りを全部付加する
            break;
        }
        if (i > 0 && i % 3 == 0) { // 3 桁ごと、ただし初回は除く
            r = "," + r; // カンマを付加する
        }
        r = c + r; // 数字を一桁追加する。
    }
    return r; // 例では "95,839,285,734.3245"
}

// Enterキーが押されたときに「TABキーが押された」イベントにするメソッド
function keyChangeEntToTab(obj) {
    var e = window.event;
    if (e.keyCode == 13) {
        e.keyCode = 9;
    }
}

var befKey = 0;
var breakFlg = false;
function setKeyVal(keyVal) {
    breakFlg = false;
    //keyVal 8:BackSpace 13:Enter 16:Shift 27:Esc 28:変換 29:無変換 32:Space 37:← 38:↑ 39:→ 40:↓ 46:Del
    //       48～57:数字キー(0～9)
    //       96～105:テンキー(0～9)
    //       118:F7 119:F8 120:F9 121:F10
    if (keyVal == 13) {
        befKey = keyVal;
    } else if (befKey == 13 && (keyVal == 32 || keyVal == 28 || keyVal == 29 || keyVal == 118 || keyVal == 119)) {
        befKey = 0;
    } else if (befKey == 13 && (keyVal == 8 || keyVal == 46)) {
        breakFlg = true;
    } else if (keyVal == 8 || keyVal == 16 || keyVal == 32 || keyVal == 37 || keyVal == 38 ||
               keyVal == 39 || keyVal == 40 || keyVal == 27 || keyVal == 28 || keyVal == 29 ||
               keyVal == 46 || keyVal == 118 || keyVal == 119 || keyVal == 120 || keyVal == 121 || 
               (keyVal >= 48 && keyVal <= 57) || (keyVal >= 96 && keyVal <= 105)
    ) {
        breakFlg = true;
    } else {
        befKey = 0;
    }
}

var befName1 = "";

// 文字変換
// henkanMoji = "H" : ひらがな
// henkanMoji = "K" : カタカナ
function keySet(field1, field2, henkanMoji) {

    setKeyVal(event.keyCode);

    var aftName1 = document.getElementById(field1).value;
    if (aftName1 == "") {
        document.getElementById(field2).value = "";
        befName1 = "";
        return;
    }

    if (befName1 == aftName1) {
        return;
    }

    var nowName1 = aftName1;
    for (var i = befName1.length; i >= 0; i--) {
        if (aftName1.substr(0, i) == befName1.substr(0, i)) {
            nowName1 = aftName1.substr(i);
            break;
        }
    }
    befName1 = aftName1;

    if (breakFlg || befKey == 13) {
        return;
    }

    var changeName = nowName1.replace( /[^ 　ぁあ-んァー]/g, "" );
    if (changeName == "") {
        return;
    }
    if (henkanMoji == "K") {
        changeName = covKana(changeName);
    }
    document.getElementById(field2).value += changeName;
}

// カタカナ変換
function covKana(val) {

    var changeVal, retArray = [];
    for (var i = val.length - 1; 0 <= i; i--) {
        changeVal = val.charCodeAt(i);
        retArray[i] = (0x3041 <= changeVal && changeVal <= 0x3096) ? changeVal + 0x0060 : changeVal;
    }
    return String.fromCharCode.apply(null, retArray);
}


/******************************************** コピペ関係 ************************************************/
/* harituke_jouhou.harituke_type   ⇒ テキストボックスの名前の規則                                      */
/*                                    "renban"、"kotei"、"hairetu"、"renban_hairetu"が現在用意されている*/
/* harituke_jouhou.objectNameArray ⇒ テキストボックスの名前の配列                                      */
/* harituke_jouhou.clickedObj      ⇒ 右クリックされたオブジェクト(テキストボックス)                    */
/* harituke_jouhou.hairetuCnt      ⇒ 右クリックされたオブジェクトの配列番号(hairetu指定で使用)         */
/* harituke_jouhou.renbanArray     ⇒ 連番の代わりに使用する配列(renban_hairetu指定で使用)              */
/* checkClip と execCopy のメソッドを個別にそれぞれ実装してください。                                   */
/* afterPasteClipは、関数があれば後処理として実行します。画面更新処理としての利用を想定。               */
/********************************************************************************************************/
//クリップボードの値をテキストボックスに入れる
function insertTsv(harituke_jouhou) {
    if (typeof checkClip != "function") {
        alert("関数checkClipが定義されていません。");
        return false;
    }
    if (typeof execCopy != "function") {
        alert("関数execCopyが定義されていません。");
        return false;
    }

    if (window.clipboardData && window.clipboardData.getData) { // IE
        var clipText = window.clipboardData.getData('Text');
    } else if (window.event.clipboardData && window.event.clipboardData.getData) {   //non-IE
        var clipText = window.event.clipboardData.getData('text/plain');
    }

    var clipTextArray = tsv_to_array(clipText);

    // まずはクリップボードの中身のチェック
    // falseが帰ってきたら正しくないデータが混ざっている
    if (!checkClip(clipTextArray, harituke_jouhou)) {
        return false;
    }

    if (harituke_jouhou.harituke_type == "hairetu") {
        paste_hairetu(clipTextArray, harituke_jouhou);
    } else if(harituke_jouhou.harituke_type == "renban_hairetu") {
        paste_renban_hairetu(clipTextArray, harituke_jouhou);
    } else {
        paste_other(clipTextArray, harituke_jouhou);
    }
    if (typeof afterPasteClip == 'function') {
        afterPasteClip(harituke_jouhou);
    }
}

/***************************/
/* renban kotei のペースト */
/***************************/
function paste_other(clipTextArray, harituke_jouhou) {
    var startFlg = false;
    var retuCnt;
    var objectNameArray = harituke_jouhou.objectNameArray;
    var targetName;
    var targetNumber;
    if (harituke_jouhou.harituke_type == "renban") {
        targetName   = harituke_jouhou.clickedObj.name.split("-")[0];
        targetNumber = harituke_jouhou.clickedObj.name.split("-")[1];
    } else if (harituke_jouhou.harituke_type == "kotei") {
        targetName   = harituke_jouhou.clickedObj.name;
    }

    for (var gyouCnt = 0; gyouCnt < clipTextArray.length; gyouCnt++) { //クリップボードの各行をループ
        retuCnt = 0;
        startFlg = false;

        for (k = 0; k < objectNameArray.length; k++) { //テキストボックス名でまわす
            if (objectNameArray[k] == targetName) { //貼付け開始対象のテキストボックスならばフラグを立てる
                startFlg = true;
            }
            if (startFlg) {
                //クリップボードのデータでタブ区切りの最後を越えるとundefinedになる
                if (clipTextArray[gyouCnt][retuCnt] != undefined) { //対象となるデータがあれば

                    if (harituke_jouhou.harituke_type == "renban") {
                        targetObject = eval("document.forms[0][\"" + objectNameArray[k] + "-"  + targetNumber + "\"]");
                    } else if (harituke_jouhou.harituke_type == "kotei") {
                        targetObject = eval("document.forms[0][\"" + objectNameArray[k] + "\"]");
                    }
                    if (targetObject) { //テキストボックスがあれば(テキストボックスはあったりなかったりする)
                        execCopy(targetObject, clipTextArray[gyouCnt][retuCnt], targetNumber);
                    }
                }
                retuCnt++;
            }
        }
        if (harituke_jouhou.harituke_type == "kotei") {
            break;
        }
        targetNumber++;
    }
}

/**********************/
/* hairetu のペースト */
/**********************/
function paste_hairetu(clipTextArray, harituke_jouhou) {
    var startFlg = false;
    var retuCnt;
    var objCnt          = harituke_jouhou.hairetuCnt;
    var objectNameArray = harituke_jouhou.objectNameArray;
    var targetName      = harituke_jouhou.clickedObj.name;

    for (var gyouCnt = 0; gyouCnt < clipTextArray.length; gyouCnt++) { //クリップボードの各行をループ
        retuCnt = 0;
        startFlg = false;

        for (k = 0; k < objectNameArray.length; k++) { //テキストボックス名でまわす
            if (objectNameArray[k] == targetName) { //貼付け開始対象のテキストボックスならばフラグを立てる
                startFlg = true;
            }
            if (startFlg) {
                //クリップボードのデータでタブ区切りの最後を越えるとundefinedになる
                if (clipTextArray[gyouCnt][retuCnt] != undefined) { //対象となるデータがあれば

                    targetObject = eval("document.forms[0][\"" + objectNameArray[k] + "\"][" + objCnt + "]");
                    if (targetObject) { //テキストボックスがあれば(テキストボックスはあったりなかったりする)
                        execCopy(targetObject, clipTextArray[gyouCnt][retuCnt], objCnt);
                    }
                }
                retuCnt++;
            }
        }
        objCnt++;
    }
}

/***********************************/
/* paste_renban_hairetu のペースト */
/***********************************/
function paste_renban_hairetu(clipTextArray, harituke_jouhou) {
    var startFlg = false;
    var retuCnt;
    var renbanArray     = harituke_jouhou.renbanArray;
    var objectNameArray = harituke_jouhou.objectNameArray;
    var targetName   = harituke_jouhou.clickedObj.name.split("-")[0];
    var targetNumber = harituke_jouhou.clickedObj.name.split("-")[1];

    for (var objCnt = 0; objCnt < renbanArray.length; objCnt++) {
        if (renbanArray[objCnt] == targetNumber) {
            break;
        }
    }
    for (var gyouCnt = 0; gyouCnt < clipTextArray.length; gyouCnt++) { //クリップボードの各行をループ
        retuCnt = 0;
        startFlg = false;

        for (k = 0; k < objectNameArray.length; k++) { //テキストボックス名でまわす
            if (objectNameArray[k] == targetName) { //貼付け開始対象のテキストボックスならばフラグを立てる
                startFlg = true;
            }
            if (startFlg) {
                //クリップボードのデータでタブ区切りの最後を越えるとundefinedになる
                if (clipTextArray[gyouCnt][retuCnt] != undefined) { //対象となるデータがあれば
                    targetObject = eval("document.forms[0][\"" + objectNameArray[k] + "-"  + renbanArray[objCnt] + "\"]");
                    if (targetObject) { //テキストボックスがあれば(テキストボックスはあったりなかったりする)
                        execCopy(targetObject, clipTextArray[gyouCnt][retuCnt], objCnt);
                    }
                }
                retuCnt++;
            }
        }
        objCnt++;
    }
}

/******************************************/
/* Tab区切りのデータ(TSV)を配列にして返す */
/******************************************/
function tsv_to_array(str) {
    var tsv = new Array();
    var i = 0;
    var j = 0;
    var tsv_pattern = '[^\t\r\n]*(?=[\t\r\n]|$)';
    var qtsv_pattern = '"(?:(?:[^\t\r\n"]*"")*[^\t\r\n"]*[\r\n]+)+(?:[^\t\r\n"]*"")*[^\t\r\n"]*"(?=[\t\r\n]|$)';

    var tsv_n = new RegExp('^' + tsv_pattern);
    var tsv_t = new RegExp('^\t' + tsv_pattern);
    var tsv_q = new RegExp('^' + qtsv_pattern);
    var tsv_qt = new RegExp('^\t' + qtsv_pattern);

    var match_tab = new RegExp('^\t');
    str = str.toString();

    while(str != '') {
        tsv[i] = new Array();
        j = 0;

        //行はじめのセル
        if(str.match(tsv_q)) { //セルの中に改行がある場合
            tsv[i][j] = tsv_double_quote(str.match(tsv_q));
            str = str.replace(tsv_q, '');
            j++;
        } else if(str.match(tsv_n)) { //普通のセルの場合
            tsv[i][j] = str.match(tsv_n);
            str = str.replace(tsv_n, '');
            j++;
        }

        //行の二番目のセル以降
        while(str.match(tsv_t) || str.match(tsv_qt)) { //セルがある間ループでまわす
            str = str.replace(match_tab, '');

            if(str.match(tsv_q)) { //セルの中に改行がある場合
                tsv[i][j] = tsv_double_quote(str.match(tsv_q));
                str = str.replace(tsv_q, '');
                j++;
            } else if(str.match(tsv_n)) { //普通のセルの場合
                tsv[i][j] = str.match(tsv_n);
                str = str.replace(tsv_n, '');
                j++;
            }
        }

        //行のはじめに改行があった場合その改行はカット(最後の行は改行だけになっている)
        if(str.match(/^[\r\n]/)){
            str = str.replace(/^(?:\r\n|[\r\n])/, '');
        }
        i++;
    }
    return tsv;
}

// TSVのダブルクォートを調整(改行のあるセルの時)
function tsv_double_quote(str) {
    str = str.toString();
    var trim_quote1 = new RegExp('^"');
    var trim_quote2 = new RegExp('"$');
    str = str.replace(trim_quote1, '');
    str = str.replace(trim_quote2, '');
    str = str.replace(/""/g, '"');

    return str;
}

/******************************/
/* 入力文字数のチェック       */
/* 第一引数 ⇒ 対象文字列     */
/* 第二引数 ⇒ 一行で何文字か */
/* 返り値 ⇒ 何行か           */
/******************************/
function validate_row_cnt(string, itigyou_no_mojiLen) {
    //改行コードで区切って配列に入れていく
    string = string.replace(/\r\n/,"\n");
    string = string.replace(/\r/,"\n");

    stringArray = new Array();
    stringArray = string.split("\n");

    row_cnt = 0;
    //改行コードが現れるまでに何行消費するか数える
    for (var i = 0; i < stringArray.length; i++) {
        mojisu = stringArray[i].length;
        mojiLen = 0;
        for (var j = 0; j < mojisu; j++) {
            hitoMoji = stringArray[i].charAt(j);
            moji_hantei = escape(hitoMoji).substr(0,2);
            mojiLen += moji_hantei == "%u" ? 2 : 1;
        }
        amari = mojiLen % itigyou_no_mojiLen;
        gyousu = (mojiLen - amari) / itigyou_no_mojiLen;
        if (amari > 0) {
            gyousu++;
        }
        if (gyousu) {
            row_cnt += gyousu;
        } else {
            row_cnt++;
        }
    }

    return row_cnt;
}

function updDefault(cmd, prgId)
{
    if (!confirm('初期値登録しますか？\n(チェックボックス・ラジオボタン)')) {
        return false;
    }
    var setDefVal = "";
    var sep = "";
    for (elCnt = 0; elCnt < document.forms[0].elements.length; elCnt++) {
        var obj = document.forms[0].elements[elCnt];
        if (obj.type == "checkbox") {
            if (obj.checked) {
                setDefVal += sep + obj.name + ":" + obj.value;
            } else {
                setDefVal += sep + obj.name + ":0";
            }
            sep = "|";
        }
        if (obj.type == "radio") {
            if (obj.checked) {
                setDefVal += sep + obj.name + ":" + obj.value;
                sep = "|";
            }
        }
    }
    document.forms[0].UPD_DEFAULT_VALUE.value = setDefVal;

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

// -->


//カレンダー子ウインドゥの呼出し
function dayWin(URL, IN, x, y){
    var cmdstr="document.forms[0]."+IN+".value";
    var reqday=eval(cmdstr);
    var strObj=reqday;
    var para="";
            para="dialogLeft:"+x+"px; dialogTop:"+y+"px; dialogWidth:200px; dialogHeight:210px; location:0; directories:0; status:0;";
    
    //日付を入れるテキストボックスにフォーカスをあてる
    cmdstr="document.forms[0]."+IN+".focus()";
    reqday=eval(cmdstr);
    
    var yourDate=showModalDialog(URL,strObj,para);
    if (yourDate!=""){
        cmdstr="document.forms[0]."+IN+".value=yourDate";
        reqday=eval(cmdstr);
    }
}
function monthWin(URL, IN, x, y, reload){
    var cmdstr="document.forms[0]."+IN+".value";
    var reqday=eval(cmdstr);
    var strObj=reqday;
    var para="";
            para="dialogLeft:"+x+"px; dialogTop:"+y+"px; dialogWidth:180px; dialogHeight:180px; location:0; directories:0; status:0;";
    var para="dialogLeft:"+x+"px; dialogTop:"+y+"px; dialogWidth:220px; dialogHeight:200px; location:0; directories:0; status:0;";
    var yourMonth=showModalDialog(URL,strObj,para);
    if (yourMonth!=""){
        cmdstr="document.forms[0]."+IN+".value=yourMonth";
        reqday=eval(cmdstr);
        if (reload == "1") {
            eval("document.forms[0].submit();");
        }
    }
    //return FALSE;//20090824 kurata(mycalendarに手を加える際必要なので)
}
//20100517 mycalendar3用-<daywinと同じなので消す予定
//カレンダー子ウインドゥの呼出し2
function dayWin2(URL, IN, x, y, reload){
    var cmdstr="document.forms[0]."+IN+".value";
    //alert(cmdstr);
    var reqday=eval(cmdstr);
    //alert(reqday);
    var strObj=reqday;
    //alert(strObj);
    var para="";
            para="dialogLeft:"+x+"px; dialogTop:"+y+"px; dialogWidth:200px; dialogHeight:210px; location:0; directories:0; status:0;";
    
    //日付を入れるテキストボックスにフォーカスをあてる
    cmdstr="document.forms[0]."+IN+".focus()";
    //alert(cmdstr);
    reqday=eval(cmdstr);
    //alert(reqday)
    
    var yourDate=showModalDialog(URL,strObj,para);
    
      //alert(yourDate);
    if (yourDate!=""){
        cmdstr="document.forms[0]."+IN+".value=yourDate";
        reqday=eval(cmdstr);
        if (reload == "1") {
            eval("document.forms[0].submit();");
        }
      //  alert(reqday);
    }
}
//20100517 mycalendar3用-<daymonthと同じなので消す予定
function monthWin2(URL, IN, x, y){
    var cmdstr="document.forms[0]."+IN+".value";
    var reqday=eval(cmdstr);
    var strObj=reqday;
    var para="";
            para="dialogLeft:"+x+"px; dialogTop:"+y+"px; dialogWidth:180px; dialogHeight:180px; location:0; directories:0; status:0;";
    var para="dialogLeft:"+x+"px; dialogTop:"+y+"px; dialogWidth:220px; dialogHeight:200px; location:0; directories:0; status:0;";
    var yourMonth=showModalDialog(URL,strObj,para);
    if (yourMonth!=""){
        cmdstr="document.forms[0]."+IN+".value=yourMonth";
        reqday=eval(cmdstr);
    }
    //return FALSE;//20090824 kurata(mycalendarに手を加える際必要なので)
}
//**20100518*** mycalendar3用**********************************************************
//日付チェック用①
function key7AndCheck(myVal)
{
    var judgement   ="";
    var aaa = myVal.value.length;
    if(aaa > 0 && aaa < 7){
        alert("数字で7桁入力してください\n\n入力値：" + myVal.value + "\n\nデータはクリアされます。");
        myVal.value = "";
        myVal.focus();
        return false;
    }
    if(aaa==7 && event.keyCode != 37 && event.keyCode != 39 ){//横カーソルはonkeyupしたことにしない
        //alert(1);
        judgement = CheckFieldSeihoDate(myVal);
        if (!judgement) {
            myVal.focus();
        }
    }
    return judgement;
}
//日付チェック用②
function CheckFieldSeihoDate(myVal)
{
//    //*日付の型
//    var def_nengo    = ["明治","大正","昭和","平成"];
//    var nengoYear    = [ 1867,  1912,  1926,  1989 ];
//    var nengoMonth   = [   9,     7,    12,     1  ];
//    var nengoDay     = [   8,    30,    25,     8  ];
//    //年号識別記号
//    var nengoID      = [  "M",   "T",   "S",   "H" ];
//    var nengoID2     = [  "1",   "2",   "3",   "4" ];
    var def_nengo = new Array();
    var nengoYear = new Array();
    var nengoMonth= new Array();
    var nengoDay  = new Array();
    var nengoID   = new Array();
    var nengoID2  = new Array();
    // 和暦情報取得
    var warekiList = getWarekiListSync();
    for (var i = 0; i < warekiList.length; i++) {
        var info = warekiList[i];
        var startDate = new Date(info['Start']);

        def_nengo.push(info['Name']);
        nengoYear.push(startDate.getFullYear());
        nengoMonth.push(startDate.getMonth());
        nengoDay.push(startDate.getDate());
        nengoID.push(info['SName']);
        nengoID2.push(info['CD']);
    }

    //*空白チェック&&7桁チェック(念のため)
    if(myVal.value.length < 7 || myVal.value ==""){
        alert("数字で7桁入力してください");
        return false;
    }
  // alert(2); 
  //正規表現チェック
  // alert(myVal.value);
        if(myVal.value.match(/[^0-9]/g)){
          alert("数字以外の文字が入っています");
         return false;
        }
  // alert(3);     
  //*文字列の分割
    //var nnn,yyy,mmm,ddd,wyyy;
    nnn = myVal.value.substr(0,1);
    yyy = myVal.value.substr(1,2);
    mmm = myVal.value.substr(3,2);
    ddd = myVal.value.substr(5,2);
    //年号の特定
    l = nengoID2.length;
    for(i=0; i <= l ; i++)
     {
       if(nnn == nengoID2[i]){
        break;
       }
       if(i==l){
        alert("年号が存在しません。");
        return false;
        }
     }
  //年の特定（現行mycalendarの年の限定が緩いので厳密化するなら必要？？)(作成中)   
     
     
  //日付の合体
    YYY= eval(nengoYear[i]) + (eval(yyy)-1);//初年度は1からはじまるので 
    MMM= eval(mmm);
    DDD= eval(ddd);
  //妥当性チェック
    var judgement = "";
    judgement = isDate3(YYY,MMM,DDD);
    return judgement;
    
}
// 日付妥当性チェック
function isDate3(YYY,MMM,DDD) {
    var vYear  = parseInt(eval(YYY)) - 0;
    var vMonth = parseInt(eval(MMM)) - 1; // Javascriptは、0-11で表現
    var vDay   = parseInt(eval(DDD)) - 0;

    // 月,日の妥当性チェック
    if (vMonth >= 0 && vMonth <= 11 && vDay >= 1 && vDay <= 31) {
        var vDt = new Date(vYear, vMonth, vDay);

        if (isNaN(vDt) == true) {
            alert("日付が不正です。");
            //obj.value = obj.defaultValue;
            return false;
        } else if (vDt.getFullYear() == vYear && vDt.getMonth() == vMonth && vDt.getDate() == vDay){
            return true;
        }else{
            alert("日付が不正です。");
            //obj.value = obj.defaultValue;
            return false;
        }
    } else {
        alert("日付の書式が不正です。");
        //obj.value = obj.defaultValue;
        return false;
    }
}
//*************************************************************************************************************
//**20100518*** myMonthWin3用*(無駄な反復が多いのでそのうちmyCalendarWin3と共通化する予定)******************************************
function key5AndCheck(myVal){
    var judgement   ="";
    var aaa = myVal.value.length;
    if(aaa > 0 && aaa < 5){
        alert("数字で5桁入力してください\n\n入力値：" + myVal.value + "\n\nデータはクリアされます。");
        myVal.value = "";
        myVal.focus();
        return false;
    }
    if(aaa==5 && event.keyCode != 37 && event.keyCode != 39 ){//横カーソルはonkeyupしたことにしない
        //  alert(1);
        judgement = CheckFieldSeihoMonth(myVal);
        if (!judgement) {
            myVal.focus();
        }
    }
    return  judgement ;
}
//日付チェック用②
function CheckFieldSeihoMonth(myVal)
{
    //*日付の型
//    var def_nengo    = ["明治","大正","昭和","平成"];
//    var nengoYear    = [ 1867,  1912,  1926,  1989 ];
//    var nengoMonth   = [   9,     7,    12,     1  ];
//    var nengoDay     = [   8,    30,    25,     8  ];
//    //年号識別記号
//    var nengoID      = [  "M",   "T",   "S",   "H" ];
//    var nengoID2     = [  "1",   "2",   "3",   "4" ];
    var def_nengo = new Array();
    var nengoYear = new Array();
    var nengoMonth= new Array();
    var nengoDay  = new Array();
    var nengoID   = new Array();
    var nengoID2  = new Array();
    // 和暦情報取得
    var warekiList = getWarekiListSync();
    for (var i = 0; i < warekiList.length; i++) {
        var info = warekiList[i];
        var startDate = new Date(info['Start']);

        def_nengo.push(info['Name']);
        nengoYear.push(startDate.getFullYear());
        nengoMonth.push(startDate.getMonth());
        nengoDay.push(startDate.getDate());
        nengoID.push(info['SName']);
        nengoID2.push(info['CD']);
    }

  //*空白チェック&&5桁チェック(念のため)
        if(myVal.value.length < 5 || myVal.value ==""){
        alert("数字で5桁入力してください");
        return false;
      }
  // alert(2); 
  //正規表現チェック
  // alert(myVal.value);
        if(myVal.value.match(/[^0-9]/g)){
          alert("数字以外の文字が入っています");
         return false;
        }
  // alert(3);     
  //*文字列の分割
    //var nnn,yyy,mmm,ddd,wyyy;
    nnn = myVal.value.substr(0,1);
    yyy = myVal.value.substr(1,2);
    mmm = myVal.value.substr(3,2);
    //年号の特定
    l = nengoID2.length; 
    for(i=0; i <= l ; i++)
     { 
       if(nnn == nengoID2[i]){
        break;
       } 
       if(i==l){
        alert("年号が存在しません。");
        return false;
        }
     } 
  //年の特定（現行mycalendarの年の限定が緩いので厳密化するなら必要？？)(作成中)   
     
  //日付の合体
    YYY= eval(nengoYear[i]) + (eval(yyy)-1);//初年度は1からはじまるので 
    MMM= eval(mmm);
  //妥当性チェック
    var judgement ="";
    judgement = isDate4(YYY,MMM);  
    return judgement;
    
}
// 日付妥当性チェック
function isDate4(YYY,MMM) {
    var vYear  = parseInt(eval(YYY)) - 0;
    var vMonth = parseInt(eval(MMM)) - 1; // Javascriptは、0-11で表現
    var vDay = 1;
    // 月,日の妥当性チェック
    if (vMonth >= 0 && vMonth <= 11 && vDay >= 1 && vDay <= 31) {
        var vDt = new Date(vYear, vMonth, vDay);

        if (isNaN(vDt) == true) {
            alert("年月の表記が不正です。");
            //obj.value = obj.defaultValue;
            return false;
        } else if (vDt.getFullYear() == vYear && vDt.getMonth() == vMonth && vDt.getDate() == vDay){
            return true;
        }else{
            alert("年月の表記が不正です。");
            //obj.value = obj.defaultValue;
            return false;
        }
    } else {
        alert("年月の書式が不正です。");
        //obj.value = obj.defaultValue;
        return false;
    }
}

// 日付妥当性チェック
function isAllHankaku(String) {
    
    if (String.length > 0){
        if (!String.match(/[ｱ-ﾝｧｨｩｪｫｬｭｮﾞﾟ]/)) {
            alert("半角意外の文字がまじっています。");
            return false;
        }
    }
}

//マウスを乗せた場合チップヘルプで表示
function AcceptnoMousein(e, msg){

    x = event.clientX+document.body.scrollLeft;
    y = event.clientY+document.body.scrollTop;
    document.all("lay").innerHTML = msg;
    document.all["lay"].style.position = "absolute";
    document.all["lay"].style.pixelLeft = x+5;
    document.all["lay"].style.pixelTop = y+10;
    document.all["lay"].style.padding = "4px 3px 3px 8px";
    document.all["lay"].style.border = "1px solid";
    document.all["lay"].style.visibility = "visible";
    document.all["lay"].style.background = "#fffff0";
}

function AcceptnoMouseout(){
    document.all["lay"].style.visibility = "hidden";
}

//マウスUPイベント発生ならクッキー定義 2012/11/01
document.onmousedown = function(e) {
    //alert("onmousedown");
    keychar = 'M';
    if ((top.opener) && (!top.opener.closed)) {
        //キークッキー定義
        key_cookie(keychar);
    }
}
//2012/12/19
var KeyThrough=0;	//初期値は、スルーしない。
function keyThroughSet() {
    KeyThrough=1;
}
function keyThroughReSet() {
    KeyThrough=0;
}

//キーイベント発生ならクッキー定義 2012/05/16
document.onkeydown = function(e) {
    if (KeyThrough==1)
        return;
    if (e != null) {
        keycode = e.which;
        ctrl    = typeof e.modifiers == 'undefined' ? e.ctrlKey : e.modifiers & Event.CONTROL_MASK;
        shift   = typeof e.modifiers == 'undefined' ? e.shiftKey : e.modifiers & Event.SHIFT_MASK;
        // イベントの上位伝播を防止
        //e.preventDefault();
        //e.stopPropagation();
    }
    else {
        keycode = event.keyCode;
        ctrl    = event.ctrlKey;
        shift   = event.shiftKey;
        // イベントの上位伝播を防止
        //event.returnValue = false;
        //event.cancelBubble = true;
    }
    keychar = String.fromCharCode(keycode).toUpperCase();
    if ((top.opener) && (!top.opener.closed)) {
        //キークッキー定義
        key_cookie(keychar);
    }
}
function key_cookie(keychar) {
    if (document.cookie) {
        var cookies = document.cookie.split("; ");
        for (var i = 0; i < cookies.length; i++) { 
            var str = cookies[i].split("=");
            if (str[0] == "keycode") {
                return false;    //キークッキー定義済み
            }
        }
        document.cookie = "keycode="+escape(keychar) + ";path=/";
    }
    return true;
}


//和暦に変換
/* ymd = 2017/07/28
 * fmt = 0:平成29年07月28日 1:平成29.07.28 2:平成29
 */
function Change_Wareki(ymd, fmt)
{
    if (ymd == '') {
        return '';
    }
    
    if (!isDate2(ymd)) {
       return '';
    }
    var t = ymd.split('/');
    var str_year = t[0].toString();
    var str_month = t[1].toString();
    var str_day = t[2].toString();
    var border = new Array();
    var target;
    var ret_year;
    var ret_val;
    var gengo = new Array();
    gengo[0] = new Array("明治","大正","昭和","平成");
    gengo[1] = new Array("M","T","S","H");
    
    var tmp_arr = new Array();
    tmp_arr["開始日"] = 18680908;
    tmp_arr["終了日"] = 19120730;
    border[0] = tmp_arr;

    var tmp_arr = new Array();
    tmp_arr["開始日"] = 19120730;
    tmp_arr["終了日"] = 19261225;
    border[1] = tmp_arr;

    var tmp_arr = new Array();
    tmp_arr["開始日"] = 19261225;
    tmp_arr["終了日"] = 19890107;
    border[2] = tmp_arr;

    var tmp_arr = new Array();
    tmp_arr["開始日"] = 19890107;
    tmp_arr["終了日"] = 21001231;
    border[3] = tmp_arr;
    
    target = str_year + str_month + str_day;

    for (var i = 0; border[i]; i++){
    
        if (border[i]["開始日"] <= eval(target) && eval(target) <= border[i]["終了日"] )
        {
            var str_y = border[i]["開始日"].toString();
            ret_year = (eval(str_year) - eval(str_y.substr(0, 4)) + 1);
            
            if (fmt == 0) {
                ret_val = gengo[fmt][i] + ((ret_year == 1)? "元年" : ret_year + "年") + str_month + '月' + str_day + '日';
            } else if (fmt == 1) {
                ret_val = gengo[fmt][i] + ret_year + '.' + str_month + '.' + str_day;
            } else if (fmt == 2) {
                ret_val = gengo[1][i] + ret_year;
            }
            break;
        }
    }

    return ret_val;
}

/////////////////////////////////以下tablesort//////////////////////////
/*
Table Sorter v2.4
Adds bi-directional sorting to table columns.
Copyright 2005 Digital Routes, Scotland
Copyright 2007 Neil Fraser, California
Copyright 2011 Google Inc.
http://neil.fraser.name/software/tablesort/

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

Include on your page:
  <SCRIPT LANGUAGE='JavaScript1.2' SRC='tablesort.js'></SCRIPT>
*/

// Namespace object.
var TableSort = {};

// Switch to enable or disable the TableSort.
TableSort.enabled = true;

//昇順▲、降順▼、対象外半角SP
TableSort.arrowNone = ' &nbsp;';
TableSort.arrowUp   = ' ▲';
TableSort.arrowDown = ' ▼';

//リンクの説明
TableSort.titleText = 'クリックすると、並び替えます';

/**
 * List of all the tables.
 * @private
 */
TableSort.tables = [];

/**
 * Upon which column was the table sorted last time.  -=up, +=down
 * @private
 */
TableSort.lastSort = [];


/**
 * Make all tables sortable.
 */
TableSort.initAll = function() {
  if (!TableSort.enabled) {
    return;
  }
  var tableNodeList = document.getElementsByTagName('TABLE');
  for (var x = 0, table; table = tableNodeList[x]; x++) {
    TableSort.initTable_(table);
  }
};


/**
 * Make one or more tables sortable.
 * Call this function with the ID(s) of any tables which are created
 * with DTHML after the page has loaded.
 * @param {...string} var_args ID(s) of tables.
 */
TableSort.init = function(var_args) {
  if (!TableSort.enabled) {
    return;
  }
  for (var x = 0; x < arguments.length; x++) {
    var table = document.getElementById(arguments[x]);
    if (table) {
      TableSort.initTable_(table);
    }
  }
};


/**
 * Turn all the header/footer cells of one table into sorting links.
 * @param {Element} table The table to be converted.
 * @private
 */
TableSort.initTable_ = function(table) {
  TableSort.tables.push(table);
  var t = TableSort.tables.length - 1;
  if (table.tHead) {
    for (var y = 0, row; row = table.tHead.rows[y]; y++) {
      for (var x = 0, cell; cell = row.cells[x]; x++) {
        TableSort.linkCell_(cell, t, x);
      }
    }
  }
  if (table.tFoot) {
    for (var y = 0, row; row = table.tFoot.rows[y]; y++) {
      for (var x = 0, cell; cell = row.cells[x]; x++) {
        TableSort.linkCell_(cell, t, x);
      }
    }
  }
  TableSort.lastSort[t] = 0;
};


/**
 * Turn one header/footer cell into a sorting link.
 * @param {!Element} cell The TH or TD to be made a link.
 * @param {number} t Index of table in TableSort array.
 * @param {number} x Column index.
 * @private
 */
TableSort.linkCell_ = function(cell, t, x) {
  if (TableSort.getClass_(cell)) {
    var link = document.createElement('A');
    link.href = 'javascript:TableSort.click(' + t + ', ' + x + ', "' +
        escape(TableSort.getClass_(cell)) + '");';
    if (TableSort.titleText) {
      link.title = TableSort.titleText;
    }
    while(cell.hasChildNodes()) {
      link.appendChild(cell.firstChild);
    }
    cell.appendChild(link);
    // Add an element where the sorting arrows will go.
    var arrow = document.createElement('SPAN');
    arrow.innerHTML = TableSort.arrowNone;
    arrow.className = 'TableSort_' + t + '_' + x;
    cell.appendChild(arrow);
  }
};


/**
 * Return the class name for a cell.  The name must match a sorting function.
 * @param {!Element} cell The cell element.
 * @returns {string} Class name matching a sorting function.
 * @private
 */
TableSort.getClass_ = function(cell) {
  var className = (cell.className || '').toLowerCase();
  var classList = className.split(/\s+/g);
  for (var x = 0; x < classList.length; x++) {
    if (('compare_' + classList[x]) in TableSort) {
      return classList[x];
    }
  }
  return '';
};


/**
 * Sort the rows in this table by the specified column.
 * @param {number} t Index of table in TableSort array.
 * @param {number} column Index of the column to sort by.
 * @param {string} mode Sorting mode (e.g. 'nocase').
 */
TableSort.click = function(t, column, mode) {
  var table = TableSort.tables[t];
  if (!mode.match(/^[_a-z0-9]+$/)) {
    alert('Illegal sorting mode type.');
    return;
  }
  var compareFunction = TableSort['compare_' + mode];
  if (typeof compareFunction != 'function') {
    alert('Unknown sorting mode: ' + mode);
    return;
  }
  // Determine and record the direction.
  var reverse = false;
  if (Math.abs(TableSort.lastSort[t]) == column + 1) {
    reverse = TableSort.lastSort[t] > 0;
    TableSort.lastSort[t] *= -1;
  } else {
    TableSort.lastSort[t] = column + 1;
  }
  // Display the correct arrows on every header/footer cell.
  var spanMatchAll = new RegExp('\\bTableSort_' + t + '_\\d+\\b');
  var spanMatchExact = new RegExp('\\bTableSort_' + t + '_' + column + '\\b');
  var spans = table.getElementsByTagName('SPAN');
  for (var s = 0, span; span = spans[s]; s++) {
    if (span.className && spanMatchAll.test(span.className)) {
      if (spanMatchExact.test(span.className)) {
        if (reverse) {
          span.innerHTML = TableSort.arrowDown;
          if (document.forms[0].sortData !== undefined) {
              document.forms[0].sortData.value = column + "," + 1;
          }
        } else {
          span.innerHTML = TableSort.arrowUp;
          if (document.forms[0].sortData !== undefined) {
              document.forms[0].sortData.value = column + "," + 0;
          }
        }
      } else {
        span.innerHTML = TableSort.arrowNone;
      }
    }
  }
  // Fetch the table's data and store it in a dictionary (assoc array).
  if (!table.tBodies.length) {
    return; // No data in table.
  }
  var tablebody = table.tBodies[0];
  var cellDictionary = [];
  for (var y = 0, row; row = tablebody.rows[y]; y++) {
    var cell;
    if (row.cells.length) {
      cell = row.cells[column];
    } else { // Dodge Safari 1.0.3 bug
      cell = row.childNodes[column];
    }
    cellDictionary[y] = [TableSort.dom2txt_(cell), row];
  }
  // Sort the dictionary.
  cellDictionary.sort(compareFunction);
  // Rebuild the table with the new order.
  for (y = 0; y < cellDictionary.length; y++) {
    var i = reverse ? (cellDictionary.length - 1 - y) : y;
    tablebody.appendChild(cellDictionary[i][1]);
  }
};


/**
 * Recursively build a plain-text version of a DOM structure.
 * Bug: whitespace isn't always correct, but shouldn't matter for tablesort.
 * @param {Element} obj Element to flatten into text.
 * @returns {string} Plain-text contents of element.
 * @private
 */
TableSort.dom2txt_ = function(obj) {
  if (!obj) {
    return '';
  }
  if (obj.nodeType == 3) {
    return obj.data;
  }
  var textList = [];
  for (var x = 0, child; child = obj.childNodes[x]; x++) {
    textList[x] = TableSort.dom2txt_(child);
  }
  return textList.join('');
};


/**
 * Case-sensitive sorting.
 * Compare two dictionary structures and indicate which is larger.
 * @param {Array} a First tuple.
 * @param {Array} b Second tuple.
 */
TableSort.compare_case = function(a, b) {
  if (a[0] == b[0]) {
    return 0;
  }
  return (a[0] > b[0]) ? 1 : -1;
};

/**
 * Case-insensitive sorting.
 * Compare two dictionary structures and indicate which is larger.
 * @param {Array} a First tuple.
 * @param {Array} b Second tuple.
 */
TableSort.compare_nocase = function(a, b) {
  var aLower = a[0].toLowerCase();
  var bLower = b[0].toLowerCase();
  if (aLower == bLower) {
    return 0;
  }
  return (aLower > bLower) ? 1 : -1;
};

/**
 * Numeric sorting.
 * Compare two dictionary structures and indicate which is larger.
 * @param {Array} a First tuple.
 * @param {Array} b Second tuple.
 */
TableSort.compare_num = function(a, b) {
  var aNum = parseFloat(a[0]);
  if (isNaN(aNum)) {
    aNum = -Number.MAX_VALUE;
  }
  var bNum = parseFloat(b[0]);
  if (isNaN(bNum)) {
    bNum = -Number.MAX_VALUE;
  }
  if (aNum == bNum) {
    return 0;
  }
  return (aNum > bNum) ? 1 : -1;
};


if (window.addEventListener) {
  window.addEventListener('load', TableSort.initAll, false);
} else if (window.attachEvent) {
  window.attachEvent('onload', TableSort.initAll);
}

if (navigator.appName == 'Microsoft Internet Explorer' &&
    navigator.platform.indexOf('Mac') == 0) {
  // The Mac version of MSIE is way too buggy to deal with.
  TableSort.enabled = false;
}

//更新中の画面ロック(自身のフレームのみ)
function updateFrameLock()
{
    updateFrameLocks();
}

//更新中の画面ロック(自身のフレームのみ)
function updateFrameLockMessage(exeFrame)
{
    //グレーのDiv
    exeFrame.lockScreen.style.width   = exeFrame.innerWidth + "px";
    exeFrame.lockScreen.style.height  = exeFrame.innerHeight + "px";
    exeFrame.lockScreen.style.opacity = "0.5";
    exeFrame.lockScreen.style.display = "table";

    //透明の文言Div
    exeFrame.lockScreenPopUp.style.width   = exeFrame.innerWidth + "px";
    exeFrame.lockScreenPopUp.style.height  = exeFrame.innerHeight + "px";
    exeFrame.lockScreenPopInner.innerHTML = "処理中です。しばらくお待ちください。";
    exeFrame.lockScreenPopUp.style.display = "table";
//    javascript:sdlog.show('<span style="font-size:20px; font-weight:bold;">更新中です。</span><br><progress max="100"></progress>',{duration:'0',backgroundColor:'#000000',width:'100',height:'50',opacity:'0.3',noClickHide:'1'},{border:'none',backgroundColor:'#b0b0b0',noHideButton:'1'});
}

//更新中の画面ロック：文言なし
function updateFrameLockNotMessage(exeFrame)
{
    exeFrame.lockScreen.style.width   = exeFrame.innerWidth + "px";
    exeFrame.lockScreen.style.height  = exeFrame.innerHeight + "px";
    exeFrame.lockScreen.style.opacity = "0.5";
    exeFrame.lockScreen.style.display = "table";

//    javascript:sdlog.show(null,{duration:'0',backgroundColor:'#000000',opacity:'0.3',noClickHide:'1'},{noHideButton:'1',border:'none',width:'0',height:'0',padding:'0'});
}
//処理中の画面アンロック(自身のフレームのみ)
function updateFrameUnLock(exeFrame)
{
    //グレーのDiv
    exeFrame.lockScreen.style.display = "none";
    //透明の文言Div
    exeFrame.lockScreenPopUp.style.display = "none";
}

//更新中の画面ロック(全フレーム)
//使い方
//var thisFrame = getFrameName();
//updateFrameLocks(thisFrame);
function updateFrameLocks()
{
    thisFrame = self.frames.name;
    updateFrameLockMessage(self.frames);
    if (thisFrame != 'left_frame' && undefined !== parent.left_frame) {
        parent.left_frame.updateFrameLockNotMessage(parent.left_frame);
    }
    if (thisFrame != 'right_frame' && undefined !== parent.right_frame) {
        parent.right_frame.updateFrameLockNotMessage(parent.right_frame);
    }
    if (thisFrame != 'edit_frame' && undefined !== parent.edit_frame) {
        parent.edit_frame.updateFrameLockNotMessage(parent.edit_frame);
    }
    if (thisFrame != 'top_frame' && undefined !== parent.top_frame) {
        parent.top_frame.updateFrameLockNotMessage(parent.top_frame);
    }
    if (thisFrame != 'bottom_frame' && undefined !== parent.bottom_frame) {
        parent.bottom_frame.updateFrameLockNotMessage(parent.bottom_frame);
    }
}

//更新後次の生徒のリンクをクリックする
function updateNextStudent2(schregno, order, schregnoField, repCmd, setUpdCmd) {
//alert('updateNextStudent2');
   if (document.forms[0][schregnoField].value == "") {
       //alert('{rval MSG304}');
       alert('データを指定してください。');
       return true;
   }
    nextURL = "";

    for (var i = 0; i < parent.left_frame.document.links.length; i++) {
          var search = parent.left_frame.document.links[i].search;
          //searchの中身を&で分割し配列にする。
          arr = search.split("&");

          //学籍番号が一致
          if (arr[1] == "SCHREGNO="+schregno) {
            //昇順
            if (order == 0 && i == parent.left_frame.document.links.length-1) {
                idx = 0;                                         //更新後次の生徒へ(データが最後の生徒の時、最初の生徒へ)
            }else if (order == 0) {
                idx = i+1;                                       //更新後次の生徒へ
            }else if (order == 1 && i == 0) {
                idx = parent.left_frame.document.links.length-1; //更新後前の生徒へ(データが最初の生徒の時)
            }else if (order == 1) {
                idx = i-1;                                       //更新後前の生徒へ
            }
            nextURL = parent.left_frame.document.links[idx].href.replace("edit", repCmd);    //上記の結果
            break;
        }
    }
//    document.forms[0].cmd.value = setUpdCmd;
    //クッキー書き込み
    saveCookie("nextURL", nextURL);
    btn_submit(setUpdCmd);
    return false;
}

function NextStudent2(cd) {
//alert(NextStudent2);

    var nextURL;
    nextURL = loadCookie("nextURL");
    if (nextURL) {
        if(cd == '0') {
                //クッキー削除
                deleteCookie("nextURL");
            //alert('{rval MSG201}');
            alert('データを更新しました。');
                document.location.replace(nextURL);
        }else if(cd == '1') {
                //クッキー削除
                deleteCookie("nextURL");

        }
    }
}

// -->
