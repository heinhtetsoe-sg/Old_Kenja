function btn_submit(cmd) {
    if (cmd == 'fixedUpd') {
        if (document.forms[0].FIXED_DATE.value == "") {
            alert('確定日を入力して下さい');
            return true;
        }

        parent.document.forms[0].FIXED_DATE.value = document.forms[0].FIXED_DATE.value;
        parent.document.forms[0].cmd.value = cmd;
        parent.document.forms[0].submit();
    }
    parent.closeit();
}

//日付計算
//パラメータ(h_date:日付文字列, h_year:加算年, h_month:加算月, h_day:加算日, h_split_str:日付の区切り文字列)
//戻り値(日付文字列)
function date_add(h_date, h_year, h_month, h_day, h_split_str) {
    var ret ='';

    date_ary = h_date.split(h_split_str);

    DATEobj = new Date();

    DATEobj.setYear(eval(date_ary[0]) + h_year);
    DATEobj.setMonth((eval(date_ary[1]) + h_month) - 1);
    DATEobj.setDate(eval(date_ary[2]) + h_day);

    year  = DATEobj.getFullYear();
    month = DATEobj.getMonth()+1;
    day   = DATEobj.getDate();
                
    ret = headZero(year, 4) + h_split_str + headZero(month, 2) + h_split_str + headZero(day, 2);

    return ret;
}

//先頭に０を付ける
function headZero(val, len) {
    var ret = '';
    var work = ""+val;

    for (i=work.length;i<len;i++) {
        ret = ret + "0";
    }

    ret = ret + work;

    return ret;
}
