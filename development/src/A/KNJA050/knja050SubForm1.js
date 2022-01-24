var frameName;

if (top.main_frame.right_frame) {
    frameName = top.main_frame.right_frame;
} else if (top.main_frame) {
    frameName = top.main_frame;
}

function btn_submit(cmd) {
    if (cmd == 'subExecute') {
        if (document.forms[0].E_APPDATE.value == "") {
            alert('変更開始日を入力して下さい');
            return true;
        }

        //今日の日付
        date = document.forms[0].TODAY.value;
        TODAY = date_add(date, 0, 0, 0, "/");

        //変更開始日付
        date = document.forms[0].E_APPDATE.value;
        E_APPDATE = date_add(date, 0, 0, 0, "/");

        if (TODAY < E_APPDATE) {
            alert('今日までの日付を入力して下さい');
            return true;
        }

        var ninzu = document.forms[0].COUNT.value;
        for (var i = 0; i < ninzu; i++) {
            S_APPDATE = eval("document.forms[0].S_APPDATE" + i + ".value");
            if (S_APPDATE != "") {
                //日付の設定方法
                //生徒更新履歴最終日
                date = eval("document.forms[0].S_APPDATE" + i + ".value");
                schregno = eval("document.forms[0].SCHREGNO" + i + ".value");

                S_APPDATE = date_add(date, 0, 0, 0, "/");

                //変更開始日付(から一日引いたものが履歴の変更終了日になる)
                date = document.forms[0].E_APPDATE.value;
                E_APPDATE = date_add(date, 0, 0, 0, "/");
                //S_APPDATEは前回の終了日＋１、なければ入学日
                if (S_APPDATE > E_APPDATE) {
                    alert('変更開始日を『' + S_APPDATE + '』以降の日付を入力して下さい\n学籍番号' + schregno + 'の生徒の変更終了日とかぶっています');
                    return true;
                }
            }
        }

        frameName.document.forms[0].E_APPDATE.value = document.forms[0].E_APPDATE.value;
        frameName.document.forms[0].cmd.value = cmd;

        frameName.document.forms[0].submit();
    }
    frameName.closeit();
}

//日付計算
//パラメータ(h_date:日付文字列, h_year:加算年, h_month:加算月, h_day:加算日, h_split_str:日付の区切り文字列)
//戻り値(日付文字列)
function date_add(h_date, h_year, h_month, h_day, h_split_str) {
    var ret = '';

    date_ary = h_date.split(h_split_str);

    DATEobj = new Date();

    DATEobj.setYear(eval(date_ary[0]) + h_year);
    DATEobj.setMonth((eval(date_ary[1]) + h_month) - 1);
    DATEobj.setDate(eval(date_ary[2]) + h_day);

    year = DATEobj.getFullYear();
    month = DATEobj.getMonth() + 1;
    day = DATEobj.getDate();
    if (year < 2000) year += 1900;

    ret = headZero(year, 4) + h_split_str + headZero(month, 2) + h_split_str + headZero(day, 2);

    return ret;
}

//先頭に０を付ける
function headZero(val, len) {
    var ret = '';
    var work = "" + val;

    for (i = work.length; i < len; i++) {
        ret = ret + "0";
    }

    ret = ret + work;

    return ret;
}
