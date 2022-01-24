function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//ウィンドウクローズ
function closing_window() {

    if ( document.forms[0].cntl_dt_year.value == ''){
        alert('{rval MSG305}');
        closeWin();
        return true;
    }
}

//入力チェック(週数、日数)
function count(that,temp2) {

var cl = that.id.slice(1,2);
var num = 0;

    cut_range = that.id.length ;
    id_value = that.id.slice(2,cut_range);
    //全角から半角
    that.value = toHankakuNum(that.value);
    //数値型へ変換
    that.value = toInteger(that.value);
    //セルが空の時０
    if(that.value == '' ){
        that.value = 0;
        return;
    }

    var gakki = document.forms[0].gakki.value;
    for(var i =1 ;i<=eval(gakki);i++){
        num +=eval(document.all("C" + cl + i + temp2).value);
    }
    
//合計チェック
var max = (cl == "W") ? 54 : 365;
    if(num > max){
    alert('{rval MSG915}\n学年で'+max+'以内になるように入力してください。')
        var number = eval(num) - max;
        that.value = eval(that.value) - eval(number);
        num = max;
    }
    var obj = new Object();
    obj = document.all("SC"+cl+"0"+temp2);
    obj.innerHTML = num;
    return true;
}

//クリアボタン押下時にyesno選択
function ShowConfirm(){
    if (!confirm('{rval MSG106}'))
        return false;
}

//セキュリティチェックによるウィンドウクローズ
function close_window(cd){
    if(cd){
        alert('{rval MSG305}\n学校マスタ、学期マスタ、HRクラスが作成されているか確認してください。');
    } else {
        alert('{rval MSG300}');
    }
        closeWin();
}