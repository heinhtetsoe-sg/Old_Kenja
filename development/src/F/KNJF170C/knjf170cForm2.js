function btn_submit(cmd)
{
    //日付範囲チェック
    if (cmd == "add" || cmd == "update" || cmd == "delete") {
        if (document.forms[0].DATE.value == "") {
            alert('{rval MSG301}');
            return false;
        }

        chk_date = document.forms[0].DATE.value.replace("/","-");
        chk_date = chk_date.replace("/","-");
        sdate = document.forms[0].YEAR.value+'-04-01';
        edate = parseInt(document.forms[0].YEAR.value)+1+'-03-31';

        if (chk_date < sdate || edate < chk_date) {

            chk_date = document.forms[0].DATE.value.replace("-","/");
            chk_date = chk_date.replace("-","/");
            sdate = document.forms[0].YEAR.value+'/04/01';
            edate = parseInt(document.forms[0].YEAR.value)+1+'/03/31';

            alert('{rval MSG916}\n'+sdate+'～'+edate);
            return false;
        }
    }

    temp = parseFloat(document.forms[0].TEMPERATURE.value);

    if ((temp < "-50.0") || ("50.0" < temp)) {
            alert('{rval MSG916}\n      (気温)');
            return false;
    }

    if (cmd == "delete") {
        result = confirm('{rval MSG103}');
        if (result == false) {
            return false;
        }
    }

    if (cmd == 'clear'){
        if (!confirm('{rval MSG106}')){
            return false;
        }else{
            document.forms[0].cmd.value = cmd;
            document.forms[0].submit();
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function OnAuthError()
{
   alert('{rval MSG300}');
   closeWin();
}

//天気コンボ[5]の時だけテキスト
function textDisabled()
{
    if (document.forms[0].WEATHER.value != "5") {
        document.forms[0].WEATHER_TEXT.disabled = "true";
    } else {
        document.forms[0].WEATHER_TEXT.disabled = "";
    }
}

//欠席状況の計算
function calc(obj, di_cd)
{
    //数値チェック
    obj.value = toInteger(obj.value);

    //加算
    var total_cnt = 0;
    re = new RegExp("^CNT_"+di_cd );
    for (var i=0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name.match(re)) {
            var val = parseInt(document.forms[0].elements[i].value);
            if (val > 0) {
                total_cnt += val;
            }
        }
    }

    //加算した値を貼り付け
    id = 'total_'+di_cd;
    var total = document.getElementById(id);
    total.innerHTML = total_cnt;
}

//残留塩素の入力チェック
function toFloat_ResidualChlorine(checkString)
{
    if(checkString.value != ""){
        var newString = "";
        var count = 0;
        for (i = 0; i < checkString.value.length; i++) {
            ch = checkString.value.substring(i, i+1);
            if ((ch >= "0" && ch <= "9") || (ch == ".")) {
                newString += ch;
            } else {
                alert('0.00～9.99までの数値を入力してください。');
                checkString.focus();
                return checkString.value;
            }
        }
        //入力チェック
        var arrCheckString = checkString.value.split(".");
        if(arrCheckString.length == 1 || arrCheckString.length == 2){
            if(arrCheckString.length == 2){
                if(arrCheckString[0].length == '' || arrCheckString[1].length == '' ){
                    alert('0.00～9.99までの数値を入力してください。');
                    checkString.focus();
                    return checkString.value;
                }
            }
        } else {
            alert('0.00～9.99までの数値を入力してください。');
            checkString.focus();
            return checkString.value;
        }
        //文字数チェック
        if (checkString.value.length > 4) {
            alert('0.00～9.99までの数値を入力してください。');
            checkString.focus();
            return checkString.value;
        }
        //数値チェック
        var num = (isNaN(1 * checkString.value) === true)? 0 : (1 * checkString.value );
        if (num < 0.00 || num > 9.99) {
            alert('0.00～9.99までの数値を入力してください。');
            checkString.focus();
            return checkString.value;
        }
    }
    return checkString.value;
}

//湿度の入力チェック
function toInteger_Humidity(checkString){
    if (checkString.value != "") {
        var newString = "";
        var count = 0;
        for (i = 0; i < checkString.value.length; i++) {
            ch = checkString.value.substring(i, i+1);
            if (ch >= "0" && ch <= "9") {
                newString += ch;
            } else {
                alert('0～100までの数値で入力してください。');
                checkString.focus();
                return checkString.value;
            }
        }

        //文字数チェック
        if (checkString.value.length > 3) {
            alert('0～100までの数値で入力してください。');
            checkString.focus();
            return checkString.value;
        }

        //数値チェック
        var num = (isNaN(1 * checkString.value) === true)? 0 : (1 * checkString.value );
        if (num < 0 || num > 100) {
            alert('0～100までの数値で入力してください。');
            checkString.focus();
            return checkString.value;
        }
    }
    return checkString.value;
}

//時間の入力チェック
function toInteger_CheckTime(checkString, cmd)
{
    if (checkString.value != "") {
        var newString = "";
        var count = 0;
        for (i = 0; i < checkString.value.length; i++) {
            ch = checkString.value.substring(i, i+1);
            if (ch >= "0" && ch <= "9") {
                newString += ch;
            } else {
                if(cmd == "hour"){
                    //時の場合
                    alert('00～23までの数値で入力してください。');
                } else{
                    //分の場合
                    alert('00～59までの数値で入力してください。');
                }
                checkString.focus();
                return checkString.value;
            }
        }
        //文字数チェック
        if(checkString.value.length > 2){
            if(cmd == "hour"){
                //時の場合
                alert('00～23までの数値で入力してください。');
            } else {
                //分の場合
                alert('00～59までの数値で入力してください。');
            }
            checkString.focus();
            return checkString.value;
        }

        //数値チェック
        var num = (isNaN(1 * checkString.value) === true)? 0 : (1 * checkString.value );

        if ((num < 0 || num > 23) && cmd == "hour") {
            //時の場合
            alert('00～23までの数値で入力してください。');
            checkString.focus();
            return checkString.value;
        } else if((num < 0 || num > 59) && cmd == "minute") {
            //分の場合
            alert('00～59までの数値で入力してください。');
            checkString.focus();
            return checkString.value;
        }
        checkString.value = ( '00' + checkString.value ).slice( -2 );
    }
    return checkString.value;
}
