<!--
    //元号が変わった場合は以下の配列へ追加して下さい。
    //年号の開始日付を定義する。
    var def_nengo = ["明治","大正","昭和","平成"];
    var nengoYear = [ 1867,  1912,  1926,  1989 ];
    var nengoMonth= [   9,     7,    12,     1  ];
    var nengoDay  = [   8,    30,    25,     8  ];
    //年号識別記号
    var nengoID   = [  "M",   "T",   "S",   "H" ];
    var nengoID2  = [  "1",   "2",   "3",   "4" ];

    //指定西暦年・月の最大日を取出す
    // パラメータは西暦年, 月
    // 戻りは最大日(データ型はNumber)
    function GetMaxDay(year, month){
        var calendars = new Array(31,28,31,30,31,30,31,31,30,31,30,31);
        //2月以外の処理
        if ((month<1)||(month>12))
            return 31;             //本当はエラー
        if (month!=2)
            return calendars[month-1];
        //2月の日数を閏年を考慮して算出
        var cal_flag = 0;
        if((year%100 == 0) || (year%4 != 0)){
            if(year%400 != 0){
                cal_flag = 0;
            }
            else{
                cal_flag = 1;
            }
        }
        else if(year%4 == 0){
            cal_flag = 1;
        }
        else{
            cal_flag = 0;
        }
        var max_day = calendars[1]+cal_flag;

        return max_day;
    }

    //元号に変換
    // パラメータは西暦年, 月, 日
    // 戻りは1～が"明治","大正","昭和","平成"・・・(データ型はNumber)
    function toGengo(year, month, day){
        var nengo, jyear;
        var str="";
        for(nengo=def_nengo.length;nengo>0;nengo--){
           if(nengoYear[nengo-1]<year)
               break;
           else if(nengoYear[nengo-1]==year){
               if(nengoMonth[nengo-1]<month)
                   break;
               else if(nengoMonth[nengo-1]==month){
                   if(nengoDay[nengo-1]<=day)
                       break;
               }
           }
        }
        return nengo;
    }
//-->