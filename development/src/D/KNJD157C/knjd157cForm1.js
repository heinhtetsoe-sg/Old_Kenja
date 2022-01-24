// kanji=漢字

//学期によってテスト種別を変更させる
function selectCombo()
{
    document.forms[0].submit();
    return false;
}

//数値かどうかをチェック
function checkText(obj)
{
    var name = obj.name;
    var checkString = obj.value;
    var newString ="";
    var count = 0;

    //優秀者
    if (name == "EXCELLENT_PERSON") {
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
        //.で分割　分割した個数が１のとき何もしない
        var arrEX = obj.value.split("."); 
        if(arrEX.length == 1 || arrEX.length == 2 ){
            if(arrEX.length == 2){
                //2個の時　一個目の桁数が空
                //分割された2個目の桁数が1以外のときエラー
                if(arrEX[0].length == 0 || arrEX[1].length != 1){
                    alert('{rval MSG901}\n数値を入力してください。');
                    obj.value="";
                    obj.focus();
                    return false;
                }
            }
        }
        else{
            alert('{rval MSG901}\n数値を入力してください。');
            obj.value="";
            obj.focus();
            return false;
        }
    }
    //欠点数多数保持者
    if (name == "UNSKILFUL_PERSON") {
        for (i = 0; i < checkString.length; i++) {
            ch = checkString.substring(i, i+1);
            if ((ch >= "0" && ch <= "9")) {
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
}

function newwin(SERVLET_URL, paramCmd)
{
    //優秀者
    if(document.forms[0].EXCELLENT_PERSON.name == "EXCELLENT_PERSON"){
        var name = document.forms[0].EXCELLENT_PERSON.name;
        var checkString = document.forms[0].EXCELLENT_PERSON.value;
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
            document.forms[0].EXCELLENT_PERSON.value="";
            document.forms[0].EXCELLENT_PERSON.focus();
            return false;
        }
        //.で分割　分割した個数が１のとき何もしない
        var arrEX = document.forms[0].EXCELLENT_PERSON.value.split("."); 
        if (arrEX.length == 1 || arrEX.length == 2) {
            if (arrEX.length == 2) {
                //2個の時　一個目の桁数が空
                //分割された2個目の桁数が1以外のときエラー
                if (arrEX[0].length == 0 || arrEX[1].length != 1) {
                    alert('{rval MSG901}\n数値を入力してください。');
                    document.forms[0].EXCELLENT_PERSON.value="";
                    document.forms[0].EXCELLENT_PERSON.focus();
                    return false;
                }
            }
        } else {
            alert('{rval MSG901}\n数値を入力してください。');
            document.forms[0].EXCELLENT_PERSON.value="";
            document.forms[0].EXCELLENT_PERSON.focus();
            return false;
        }
    }

    //欠点数多数保持者
    if(document.forms[0].UNSKILFUL_PERSON.name == "UNSKILFUL_PERSON"){
        var name = document.forms[0].UNSKILFUL_PERSON.name;
        var checkString = document.forms[0].UNSKILFUL_PERSON.value;
        var newString ="";
        var count = 0;
        for (i = 0; i < checkString.length; i++) {
            ch = checkString.substring(i, i+1);
            if ((ch >= "0" && ch <= "9")) {
                newString += ch;
            }
        }
        if (checkString != newString) {
            alert('{rval MSG901}\n数値を入力してください。');
            document.forms[0].UNSKILFUL_PERSON.value="";
            document.forms[0].UNSKILFUL_PERSON.focus();
            return false;
        }
    }

    var action = document.forms[0].action;
    var target = document.forms[0].target;
    var cmd = document.forms[0].cmd.value;
    if (paramCmd) {
        document.forms[0].cmd.value = paramCmd;
    }

    document.forms[0].action = SERVLET_URL +"/KNJD";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
    document.forms[0].cmd.value = cmd;
}
