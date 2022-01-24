//サブミット
function btn_submit(cmd) {

    if (cmd === 'csv') {
        //日付のチェック
        var date_from = document.forms[0].DATE_FROM;
        var date_to   = document.forms[0].DATE_TO;
        var irekae    = "";
        var checksdate = document.forms[0].CHECK_SDATE.value;
        var checkedate = document.forms[0].CHECK_EDATE.value;

        //入力チェック
        if (date_from.value == "") {
            alert("開始日付が未入力です。");
            date_from.focus();
            return false;
        }
        if (date_to.value == "") {
            alert("終了日付が未入力です。");
            date_to.focus();
            return false;
        }

        //大小チェック
        if(date_from.value > date_to.value){
            alert("開始日付と終了日付の範囲指定が不正です。");
            date_from.focus();
            return false;
        }
        
        //年度内日付チェック
        if(date_from.value < checksdate || date_to.value < checksdate) {
            alert("開始日付と終了日付の範囲指定がログイン年度内ではありません。");
            date_from.focus();
            return false;
        }
        if(date_from.value > checkedate || date_to.value > checkedate ) {
            alert("開始日付と終了日付の範囲指定がログイン年度内ではありません。");
            date_from.focus();
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
