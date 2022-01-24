function btn_submit(cmd, zip, gzip, zadd, gadd) 
{
    if (cmd == 'delete'){
        if (!confirm('{rval MSG103}'))
            return false;
    }
    if (cmd == 'reset' && !confirm('{rval MSG106}')) {
        return true;
    }
    //検索時のチェック
    if (cmd == 'reference' || cmd == 'back1' || cmd == 'next1') {
        if (document.forms[0].EXAMNO.value == '') {
            alert('{rval MSG301}\n( 受験番号 )');
            return true;
        }
        if (zip != document.forms[0].ZIPCD.value ||
            gzip != document.forms[0].GZIPCD.value ||zadd != document.forms[0].ADDRESS1.value || 
            gadd != document.forms[0].GADDRESS1.value) {
            if (!confirm('{rval MSG108}')) {
                return true;
            }
        }
    }
    
    if (cmd == 'disp_clear') {
        //outputLAYER('label_birthday', '');
        outputLAYER('label_birthday', '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;');
        for (i = 0; i < document.forms[0].elements.length; i++) {
            if (document.forms[0].elements[i].type == 'select-one' || document.forms[0].elements[i].type == 'text') {
                if (document.forms[0].elements[i].type == 'select-one') {
                    document.forms[0].elements[i].value = document.forms[0].elements[i].options[0].value;
                } else {
                    document.forms[0].elements[i].value = "";
                }
            }
        }
        document.forms[0].btn_udpate.disabled = true;
        document.forms[0].btn_up_pre.disabled = true;
        document.forms[0].btn_up_next.disabled = true;
        document.forms[0].btn_del.disabled = true;
        return false;
    }
    
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
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

    if (checkString != newString) {
        alert("入力された値は不正な文字列です。\n電話番号を入力してください。\n入力された文字列は削除されます。");
        // 文字列を返す
        return newString;
    }
    return checkString;
}

function setWareki(obj, ymd)
{

    var d = ymd;
    var tmp = d.split('/');
    var ret = Calc_Wareki(tmp[0],tmp[1],tmp[2]);

}

function setWarekiName(obj)
{
    if (obj.value == '') {
        //outputLAYER('label_birthday', '');
        outputLAYER('label_birthday', '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;');
        //outputLAYER('label_fs_day', '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;');
    }
    return;
}

//ボタンを押し不可にする
function btn_disabled() {
    document.forms[0].btn_udpate.disabled = true;
    document.forms[0].btn_up_pre.disabled = true;
    document.forms[0].btn_up_next.disabled = true;
    document.forms[0].btn_del.disabled = true;
}

//フォームの値が変更されたか判断する
function change_flg() {
    vflg = true;
}


/* XMLHttpRequest生成 */
function createXmlHttp(){
    if( document.all ){
        return new ActiveXObject("Microsoft.XMLHTTP");
    }
    else if( document.implementation ){
        return new XMLHttpRequest();
    }
    else{
        return null;
    }
}
var xmlhttp = null;

function SetFsCd(obj){
    var fs_label = document.getElementById("label_name");
    fs_label.innerHTML = '';
    if (obj.value.length == 4) {
        /* レスポンスデータ処理 */
        var handleHttpEvent = function (){
            if( xmlhttp.readyState == 4 ){
                var data = "";
                if( xmlhttp.status == 200 ){
                    var resdata = xmlhttp.responseText;
                    if (resdata.length != ""){
                        fs_label.innerHTML = '    '+resdata;
                    }

                    //フォームに反映

                }
            }
        }
        /* XMLHttpRequestオブジェクト作成 */
        if( xmlhttp == null ){
            xmlhttp = createXmlHttp();
        }
        else{
            /* 既に作成されている場合、通信をキャンセル */
            xmlhttp.abort();
        }
        if (obj.value != "") {
            /* 入力フォームデータの処理 */
            var postdata = new String();
            postdata = "cmd=sendFsCd";
            postdata += "&FS_CD_SEARCH="+obj.value;
            /* レスポンスデータ処理方法の設定 */
            xmlhttp.onreadystatechange = handleHttpEvent;
            /* HTTPリクエスト実行 */
            xmlhttp.open("POST", "knjl510aindex.php" , true);
            xmlhttp.setRequestHeader("Content-Type" , "application/x-www-form-urlencoded");
            xmlhttp.send(postdata);

        }
    }
}

