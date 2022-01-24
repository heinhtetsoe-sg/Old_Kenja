function btn_submit(cmd) {
   
    if (cmd == "clear") {
        if (!confirm('{rval MSG106}'))
            return false;
    }        
        
    if (cmd == "delete") {
        result = confirm('{rval MSG103}');
        if (result == false) {
            alert('{rval MSG203}');
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function Btn_reset() {
   result = confirm('{rval MSG106}');
   if (result == false) {
       return false;
   }
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
var allBank1 = null;
var allBank2 = null;
window.onload = function() {
    selectObj1 = document.forms[0].BANKCD1;
    selectObj2 = document.forms[0].BANKCD2;
    allBank1 = selectObj1.cloneNode(true);
    allBank2 = selectObj2.cloneNode(true);
};

function SetBank(obj, seq){
    /* レスポンスデータ処理 */
    var handleHttpEvent = function (){
            debug(xmlhttp.readyState);
        if( xmlhttp.readyState == 4 ){
            var data = [];
            if( xmlhttp.status == 200 ){
                var resdata = xmlhttp.responseText;
                debug(resdata);
                if (resdata.length != ""){
                  //デコードとevalしてJavaScript化
                  eval('data='+ decodeURIComponent(resdata));
                }
                setList(document.forms[0]["BANKCD" + seq], data);
                SetBranch(document.forms[0]["BANKCD" + seq], seq);
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
    if (obj.value == "") {
        var selectObj = document.forms[0]["BANKCD" + seq];
        var selectParentNode = selectObj.parentNode;
        if (seq == '1') {
            allBank1Clone = allBank1.cloneNode(true);
            selectParentNode.replaceChild(allBank1Clone, selectObj);
            SetBranch(allBank1Clone, seq);
        } else {
            allBank2Clone = allBank2.cloneNode(true);
            selectParentNode.replaceChild(allBank2Clone, selectObj);
            SetBranch(allBank2Clone, seq);
        }
    } else {
        /* 入力フォームデータの処理 */
        var postdata = new String();
        postdata = "cmd=sendBank";
        postdata += "&BANKCD_SEARCH="+obj.value;
        debug(postdata);
        /* レスポンスデータ処理方法の設定 */
        xmlhttp.onreadystatechange = handleHttpEvent;
        /* HTTPリクエスト実行 */
        xmlhttp.open("POST", "knjp717index.php" , true);
        xmlhttp.setRequestHeader("Content-Type" , "application/x-www-form-urlencoded");
        xmlhttp.send(postdata);
    }

}
function SetBranch(obj, seq){

    if (obj.value == '9900') {
        document.forms[0]["BRANCHCD_T" + seq].style.display = "block";
        document.forms[0]["BRANCHCD_C" + seq].style.display = "none";
        return;
    } else {
        document.forms[0]["BRANCHCD_T" + seq].style.display = "none";
        document.forms[0]["BRANCHCD_C" + seq].style.display = "block";
    }

    /* レスポンスデータ処理 */
    var handleHttpEvent = function (){
            debug(xmlhttp.readyState);
        if( xmlhttp.readyState == 4 ){
            var data = [];
            if( xmlhttp.status == 200 ){
                var resdata = xmlhttp.responseText;
                debug(resdata);
                if (resdata.length != ""){
                  //デコードとevalしてJavaScript化
                  eval('data='+ decodeURIComponent(resdata));
                }
                setList(document.forms[0]["BRANCHCD_C" + seq], data);
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
    /* 入力フォームデータの処理 */
    var postdata = new String();
    postdata = "cmd=send";
    postdata += "&BANKCD="+obj.value;
    debug(postdata);
    /* レスポンスデータ処理方法の設定 */
    xmlhttp.onreadystatechange = handleHttpEvent;
    /* HTTPリクエスト実行 */
    xmlhttp.open("POST", "knjp717index.php" , true);
    xmlhttp.setRequestHeader("Content-Type" , "application/x-www-form-urlencoded");
    xmlhttp.send(postdata);

}

function setList(opt, data)
{
    var selectObj = opt;
    var selectParentNode = selectObj.parentNode;
    var newSelectObj = selectObj.cloneNode(false); // Make a shallow copy
    selectParentNode.replaceChild(newSelectObj, selectObj);
    opt = newSelectObj;
    var setData = [];
    for (var i in data){
        var dataArray = data[i].split(':');
        setData[i * 1] = data[i];
    }
    var j = 0;
    for (var i in setData){
        var dataArray = setData[i].split(':');
        opt.options[j] = new Option(setData[i], dataArray[0]);
        j++;
    }
}
function debug(str){
}
