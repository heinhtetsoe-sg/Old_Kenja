<!--kanji=漢字-->
<!-- <?php # $RCSfile: knjd125eForm1.js,v $ ?> -->
<!-- <?php # $Revision: 56581 $ ?> -->
<!-- <?php # $Date: 2017-10-22 21:37:16 +0900 (日, 22 10 2017) $ ?> -->

function btn_submit(cmd) {

    if(cmd == 'show_all'){
        document.forms[0].shw_flg.value = (document.forms[0].shw_flg.value == 'on')? 'off' : 'on';
        cmd = '';
    }else if(cmd == ''){
        document.forms[0].shw_flg.value = 'off';
    }
    if (cmd == 'reset'){
        if (!confirm('{rval MSG106}'))
            return false;
    } else if (cmd == 'update'){
        //更新権限チェック
        userAuth = document.forms[0].USER_AUTH.value;
        updateAuth = document.forms[0].UPDATE_AUTH.value;
        if (userAuth < updateAuth){
            alert('{rval MSG300}');
            return false;
        }

        var checkbox_flg = true;
        var text_cnt  = 0;
        var text_cnt2 = 0;
        for (var i = 0; i < document.forms[0].elements.length; i++ ) {
            var e = document.forms[0].elements[i];
            if (e.type == 'text' && e.value != '') {
                //スペース削除
                var str_num = e.value;
                e.value = str_num.replace(/ |　/g,"");
                //数字チェック
                if (isNaN(e.value)) {
                    alert('{rval MSG901}' + '\n値：' + e.value + 'は 数値ではありません');
                    return false;
                }                
                //値(範囲)チェック
                var score = parseInt(e.value);
                if (score < 0 || score > 100) {
                    alert('{rval MSG914}'+'0点～100点以内で入力してください。値：' + e.value);
                    return false;
                }
            }
            if (e.type == 'text') {
                if (text_cnt == 2) {
                    text_cnt  = 0;
                    text_cnt2 = 0;
                }
                if (e.value != '') text_cnt2++;
                text_cnt++;
                if (text_cnt2 == 2) {
                    alert('両方（追試・見込点）の入力はできません。\nいづれか１つのみに入力して下さい。');
                    return false;
                }                
            }
            if (e.type == 'checkbox') {
                if (e.checked) checkbox_flg = false;
            }
        }
        if (checkbox_flg) {
            alert('処理指定にチェックが入っていません。\n１つ以上チェックして下さい。');
            return false;
        }                

    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function calc(obj){
    //スペース削除
    var str_num = obj.value;
    obj.value = str_num.replace(/ |　/g,"");

    var str = obj.value;
    var nam = obj.name;
    
    //数字チェック
    if (isNaN(obj.value)) {
        alert('{rval MSG907}');
        obj.value = obj.defaultValue;
        return;
    }

    var score = parseInt(obj.value);
    if (score < 0 || score > 100) {
        alert('{rval MSG914}'+'0点～100点以内で入力してください。');
        obj.value = obj.defaultValue;
        return;
    }
    for (i = 0; i < str.length; i++) {
        ch = str.substring(i, i+1);
        if (ch == ".") {
            alert('{rval MSG901}'+'「整数」を入力してください。');
            obj.value = obj.defaultValue;
            return;
        }
    }
}
