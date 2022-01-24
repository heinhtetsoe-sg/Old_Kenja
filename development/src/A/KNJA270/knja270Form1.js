function btn_submit(cmd) {
    document.forms[0].encoding = "multipart/form-data";
    if (cmd == "init"){
        AllClearList();
        document.forms[0].selectdata.value = "";
    }
    if (cmd == "csv"){
        if (document.forms[0].Radio_No.value=="4" && document.forms[0].RUIKEI[1].checked){
            //ブランクチェック
            if (document.forms[0].ATTENDDATE.value == ""){
                alert('出欠集計日付を入力して下さい。');
                return false;
            }
        }

        if (document.forms[0].Radio_No.value=="5") {
            //ブランクチェック
            if (document.forms[0].PERIODCD.value == ""){
                alert('校時を選択して下さい。');
                return false;
            }
            var abbDate = document.forms[0].ABBDATE.value;    //日付（欠席者）
            var tmp1 = abbDate.split('/');
            var tmp_s = tmp1[0] + tmp1[1] + tmp1[2];     //日付（'/'は除く）

            var today = document.forms[0].TODAY.value.split('/');
            var todaychk = today[0] + today[1] + today[2];     //今日（'/'は除く）
            if (abbDate == ""){
                alert('日付を入力して下さい。');
                return false;
            }
            //範囲チェック（今日以前のみ）
            if (parseInt(tmp_s) > parseInt(todaychk)){
                alert('今日以前の日付を入力して下さい。');
                return false;
            }
        }

        if (document.forms[0].CLASS_SELECTED.length == 0){
            alert('{rval MSG916}');
            return;
        }

        attribute3 = document.forms[0].selectdata;
        attribute3.value = "";
        sep = "";
        for (var i = 0; i < document.forms[0].CLASS_NAME.length; i++)
        {  
            document.forms[0].CLASS_NAME.options[i].selected = 0;
        }

        for (var i = 0; i < document.forms[0].CLASS_SELECTED.length; i++)
        {  
            document.forms[0].CLASS_SELECTED.options[i].selected = 1;
            attribute3.value = attribute3.value + sep + document.forms[0].CLASS_SELECTED.options[i].value;
            sep = ",";
        }

        if (document.forms[0].Radio_No.value=="1"){
            var tmp_seme = document.forms[0].DATE_SEME.value;
            var tmp_seme2 = document.forms[0].DATE_SEME2.value;

            var date1 = document.forms[0].DATE.value;    //開始日付
            var date2 = document.forms[0].DATE2.value;    //終了日付
            var tmp1 = date1.split('/');
            var tmp2 = date2.split('/');
            var tmp_s = tmp1[0] + tmp1[1] + tmp1[2];     //開始日付（'/'は除く）
            var tmp_e = tmp2[0] + tmp2[1] + tmp2[2];     //終了日付（'/'は除く）

            var seme_s = document.forms[0].SEME_S.value.split(',');    //学期開始日付（'/'は除く）
            var seme_e = document.forms[0].SEME_E.value.split(',');    //学期終了日付（'/'は除く）
            var gakki_su = document.forms[0].GAKKI_SUU.value;        //学期数

            //ブランクチェック
            if (date1 == "" || date2 == ""){
                alert('日付を入力して下さい。');
                return false;
            }
            //大小チェック
            if (parseInt(tmp_s) > parseInt(tmp_e)){
                alert('日付範囲が不正です。');
                return false;
            }
            //今年度の範囲内チェック・・・（日付＜１学期開始日付　日付＞最終学期終了日付）
            if (parseInt(tmp_s) < parseInt(seme_s[0]) || parseInt(tmp_s) > parseInt(seme_e[parseInt(gakki_su)-1]) || 
                parseInt(tmp_e) < parseInt(seme_s[0]) || parseInt(tmp_e) > parseInt(seme_e[parseInt(gakki_su)-1]) ){
                alert('今年度の範囲内の日付を入力して下さい。');
                return false;
            }
            //学期またがりチェック・・・（日付＞＝学期開始日付　学期終了日付＞＝日付）
            for (var i = 0; i < parseInt(gakki_su); i++){
                if (parseInt(tmp_s) >= parseInt(seme_s[i]) && parseInt(seme_e[i]) >= parseInt(tmp_s)) {
                    //学期の取得
                    document.forms[0].DATE_SEME.value = i+1;
                }
                if (parseInt(tmp_e) >= parseInt(seme_s[i]) && parseInt(seme_e[i]) >= parseInt(tmp_e)) {
                    //学期の取得
                    document.forms[0].DATE_SEME2.value = i+1;
                }
            }

            if (tmp_seme != document.forms[0].DATE_SEME.value || tmp_seme2 != document.forms[0].DATE_SEME2.value){
                AllClearList();
                document.forms[0].selectdata.value = "";

                document.forms[0].cmd.value = "init";
                document.forms[0].submit();
                return false;
            }
        }
        
    }
    if (cmd == "cmbchange"){
        AllClearList();
        document.forms[0].selectdata.value = "";
    }
    if (cmd == "datechange"){
        AllClearList();
        document.forms[0].selectdata.value = "";
        var tmp_seme = document.forms[0].DATE_SEME.value;
        var tmp_seme2 = document.forms[0].DATE_SEME2.value;

        var date1 = document.forms[0].DATE.value;    //開始日付
        var date2 = document.forms[0].DATE2.value;    //終了日付
        var tmp1 = date1.split('/');
        var tmp2 = date2.split('/');
        var tmp_s = tmp1[0] + tmp1[1] + tmp1[2];     //開始日付（'/'は除く）
        var tmp_e = tmp2[0] + tmp2[1] + tmp2[2];     //終了日付（'/'は除く）

        var seme_s = document.forms[0].SEME_S.value.split(',');    //学期開始日付（'/'は除く）
        var seme_e = document.forms[0].SEME_E.value.split(',');    //学期終了日付（'/'は除く）
        var gakki_su = document.forms[0].GAKKI_SUU.value;        //学期数
        for (var i = 0; i < parseInt(gakki_su); i++){
            if (parseInt(tmp_s) >= parseInt(seme_s[i])) {
                //学期の取得
                document.forms[0].DATE_SEME.value = i+1;
            }
            if (parseInt(seme_e[i]) >= parseInt(tmp_s)) {
                //学期の取得
                document.forms[0].DATE_SEME2.value = i+1;
            }
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//NO001
function dis_ruikei(obj) {
    if (obj.value==1) document.forms[0].ATTENDDATE.disabled = true;
    if (obj.value==2) document.forms[0].ATTENDDATE.disabled = false;
}

//function date_check(cmd) {
//
//    AllClearList();
//    document.forms[0].selectdata.value = "";
//
//    var date1 = document.forms[0].DATE.value;    //開始日付
//    var date2 = document.forms[0].DATE2.value;    //終了日付
//    var tmp1 = date1.split('/');
//    var tmp2 = date2.split('/');
//    var tmp_s = tmp1[0] + tmp1[1] + tmp1[2];     //開始日付（'/'は除く）
//    var tmp_e = tmp2[0] + tmp2[1] + tmp2[2];     //終了日付（'/'は除く）
//
//    var seme_s = document.forms[0].SEME_S.value.split(',');    //学期開始日付（'/'は除く）
//    var seme_e = document.forms[0].SEME_E.value.split(',');    //学期終了日付（'/'は除く）
//    var gakki_su = document.forms[0].GAKKI_SUU.value;        //学期数
//
//    //ブランクチェック
//    if (date1 == "" || date2 == ""){
//        alert('日付を入力して下さい。');
//        return false;
//    }
//    //大小チェック
//    if (parseInt(tmp_s) > parseInt(tmp_e))
//    {
//        alert('日付範囲が不正です。');
//        return false;
//    }
//    //今年度の範囲内チェック・・・（日付＜１学期開始日付　日付＞最終学期終了日付）
//    if (parseInt(tmp_s) < parseInt(seme_s[0]) || parseInt(tmp_s) > parseInt(seme_e[parseInt(gakki_su)-1]) || 
//        parseInt(tmp_e) < parseInt(seme_s[0]) || parseInt(tmp_e) > parseInt(seme_e[parseInt(gakki_su)-1]) )
//    {
//        alert('今年度の範囲内の日付を入力して下さい。');
//        return false;
//    }
//    //学期またがりチェック・・・（日付＞＝学期開始日付　学期終了日付＞＝日付）
//    var flg = true;
//    for (var i = 0; i < parseInt(gakki_su); i++)
//    {  
//        if (parseInt(tmp_s) >= parseInt(seme_s[i]) && parseInt(seme_e[i]) >= parseInt(tmp_s) && 
//            parseInt(tmp_e) >= parseInt(seme_s[i]) && parseInt(seme_e[i]) >= parseInt(tmp_e) )
//        {
//            //学期の取得
//            document.forms[0].DATE_SEME.value = i+1;
//            flg = false;
//        }
//    }
//    if (flg)
//    {
//        alert('日付範囲が不正です。学期をまたがっています。');
//        return false;
//    }
//    document.forms[0].cmd.value = cmd;
//    document.forms[0].submit();
//    return false;
//}

function change(obj) {
    if (obj.value==1 || obj.value==2){
        document.forms[0].DATE.disabled = false;
        document.forms[0].DATE2.disabled = false;
        document.forms[0].ABBDATE.disabled = true;
        document.forms[0].PERIODCD.disabled = true;
        document.forms[0].GAKKI.disabled = true;
        document.forms[0].YEAR.disabled = true;
        document.forms[0].OUTDIV.disabled = true;
        for (var i=0;i<document.forms[0].elements.length;i++){
            var e = document.forms[0].elements[i];
            if (e.type=='button' && e.name=='btn_calen'){
                e.disabled = false;
            }
        }
        document.forms[0].Radio_No.value = obj.value;
        AllClearList();
        btn_submit('datechange')
    }
    if (obj.value==3 || obj.value==4){
        document.forms[0].GAKKI.disabled = false;
        document.forms[0].YEAR.disabled = false;
        document.forms[0].OUTDIV.disabled = false;
        document.forms[0].ABBDATE.disabled = true;
        document.forms[0].PERIODCD.disabled = true;
        document.forms[0].DATE.disabled = true;
        document.forms[0].DATE2.disabled = true;
        for (var i=0;i<document.forms[0].elements.length;i++)
        {
            var e = document.forms[0].elements[i];
            if (e.type=='button' && e.name=='btn_calen'){
                e.disabled = true;
            }
        }
        if (document.forms[0].Radio_No.value != 3 && document.forms[0].Radio_No.value != 4){
            document.forms[0].Radio_No.value = obj.value;
            AllClearList();
            btn_submit('cmbchange');
        }else {
            document.forms[0].Radio_No.value = obj.value;
            AllClearList();//NO001
            btn_submit('cmbchange');//NO001
        }
    }
    if (obj.value==5) {
        document.forms[0].ABBDATE.disabled = false;
        document.forms[0].PERIODCD.disabled = false;
        document.forms[0].GAKKI.disabled = true;
        document.forms[0].YEAR.disabled = true;
        document.forms[0].OUTDIV.disabled = true;
        document.forms[0].DATE.disabled = true;
        document.forms[0].DATE2.disabled = true;
        for (var i=0;i<document.forms[0].elements.length;i++){
            var e = document.forms[0].elements[i];
            if (e.type=='button' && e.name=='btn_calen'){
                e.disabled = false;
            }
        }
        document.forms[0].Radio_No.value = obj.value;
        AllClearList();
        btn_submit('knja270')
    }
}

function ClearList(OptionList, TitleName) 
{
    OptionList.length = 0;
}
    
function AllClearList(OptionList, TitleName) 
{
        attribute = document.forms[0].CLASS_NAME;
        ClearList(attribute,attribute);
        attribute = document.forms[0].CLASS_SELECTED;
        ClearList(attribute,attribute);
}

//クラス選択／取消（一部）
function move1(side)
{   
    var temp1 = new Array();
    var temp2 = new Array();
    var tempa = new Array();
    var tempb = new Array();
    var tempaa = new Array();    // 2004/01/26
    var current1 = 0;
    var current2 = 0;
    var y=0;
    var attribute;
    
    //assign what select attribute treat as attribute1 and attribute2
    if (side == "left")
    {  
        attribute1 = document.forms[0].CLASS_NAME;
        attribute2 = document.forms[0].CLASS_SELECTED;
    }
    else
    {  
        attribute1 = document.forms[0].CLASS_SELECTED;
        attribute2 = document.forms[0].CLASS_NAME;  
    }

    
    //fill an array with old values
    for (var i = 0; i < attribute2.length; i++)
    {  
        y=current1++
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        tempaa[y] = attribute2.options[i].value+","+y; // 2004/01/26
    }

    //assign new values to arrays
    for (var i = 0; i < attribute1.length; i++)
    {   
        if ( attribute1.options[i].selected )
        {  
            y=current1++
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text; 
            tempaa[y] = attribute1.options[i].value+","+y; // 2004/01/26
        }
        else
        {  
            y=current2++
            temp2[y] = attribute1.options[i].value; 
            tempb[y] = attribute1.options[i].text;
        }
    }

    tempaa.sort();    // 2004/01/26

    //generating new options // 2004/01/26
    for (var i = 0; i < temp1.length; i++)
    {  
        var val = tempaa[i];
        var tmp = val.split(',');

        attribute2.options[i] = new Option();
        attribute2.options[i].value = temp1[tmp[1]];
        attribute2.options[i].text =  tempa[tmp[1]];
    }

    //generating new options
    ClearList(attribute1,attribute1);
    if (temp2.length>0)
    {    
        for (var i = 0; i < temp2.length; i++)
        {   
            attribute1.options[i] = new Option();
            attribute1.options[i].value = temp2[i];
            attribute1.options[i].text =  tempb[i];
        }
    }

}

//クラス選択／取消（全部）
function moves(sides)
{   
    var temp5 = new Array();
    var tempc = new Array();
    var tempaa = new Array();    // 2004/01/26
    var current5 = 0;
    var z=0;
    
    //assign what select attribute treat as attribute5 and attribute6
    if (sides == "left")
    {  
        attribute5 = document.forms[0].CLASS_NAME;
        attribute6 = document.forms[0].CLASS_SELECTED;
    }
    else
    {  
        attribute5 = document.forms[0].CLASS_SELECTED;
        attribute6 = document.forms[0].CLASS_NAME;  
    }

    
    //fill an array with old values
    for (var i = 0; i < attribute6.length; i++)
    {  
        z=current5++
        temp5[z] = attribute6.options[i].value;
        tempc[z] = attribute6.options[i].text;
        tempaa[z] = attribute6.options[i].value+","+z; // 2004/01/26
    }

    //assign new values to arrays
    for (var i = 0; i < attribute5.length; i++)
    {   
        z=current5++
        temp5[z] = attribute5.options[i].value;
        tempc[z] = attribute5.options[i].text; 
        tempaa[z] = attribute5.options[i].value+","+z; // 2004/01/26
    }

    tempaa.sort();    // 2004/01/26

    //generating new options // 2004/01/26
    for (var i = 0; i < temp5.length; i++)
    {  
        var val = tempaa[i];
        var tmp = val.split(',');

        attribute6.options[i] = new Option();
        attribute6.options[i].value = temp5[tmp[1]];
        attribute6.options[i].text =  tempc[tmp[1]];
    }

    //generating new options
    ClearList(attribute5,attribute5);

}
function dis_date(flag)
{
    document.forms[0].DATE.disabled = flag;
    document.forms[0].DATE2.disabled = flag;
        for (var i=0;i<document.forms[0].elements.length;i++)
        {
            var e = document.forms[0].elements[i];
            if (e.type=='button' && e.name=='btn_calen'){
                e.disabled = flag;
            }
        }
}

//印刷
function newwin(SERVLET_URL, schoolCd, fileDiv) {
    if (document.forms[0].Radio_No.value=="4" && document.forms[0].RUIKEI[1].checked) {
        //ブランクチェック
        if (document.forms[0].ATTENDDATE.value == "") {
            alert('出欠集計日付を入力して下さい。');
            return false;
        }
        //対象学期範囲内チェック
        if (document.forms[0].GAKKI_SDATE.value <= document.forms[0].ATTENDDATE.value && 
            document.forms[0].ATTENDDATE.value <= document.forms[0].GAKKI_FDATE.value)
        {
        } else {
            alert('出欠集計日付が学期範囲外です。\n学期の範囲内の日付を入力して下さい。');
            return false;
        }
    }
    if (document.forms[0].CLASS_SELECTED.length == 0) {
        alert('{rval MSG916}');
        return;
    }

    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    sep = "";
    for (var i = 0; i < document.forms[0].CLASS_NAME.length; i++)
    {  
        document.forms[0].CLASS_NAME.options[i].selected = 0;
    }

    for (var i = 0; i < document.forms[0].CLASS_SELECTED.length; i++)
    {  
        document.forms[0].CLASS_SELECTED.options[i].selected = 1;
        attribute3.value = attribute3.value + sep + document.forms[0].CLASS_SELECTED.options[i].value;
        sep = ",";
    }

    if (document.forms[0].Radio_No.value == "1") {
        var tmp_seme = document.forms[0].DATE_SEME.value;
        var tmp_seme2 = document.forms[0].DATE_SEME2.value;

        var date1 = document.forms[0].DATE.value;    //開始日付
        var date2 = document.forms[0].DATE2.value;    //終了日付
        var tmp1 = date1.split('/');
        var tmp2 = date2.split('/');
        var tmp_s = tmp1[0] + tmp1[1] + tmp1[2];     //開始日付（'/'は除く）
        var tmp_e = tmp2[0] + tmp2[1] + tmp2[2];     //終了日付（'/'は除く）

        var seme_s = document.forms[0].SEME_S.value.split(',');    //学期開始日付（'/'は除く）
        var seme_e = document.forms[0].SEME_E.value.split(',');    //学期終了日付（'/'は除く）
        var gakki_su = document.forms[0].GAKKI_SUU.value;        //学期数

        //ブランクチェック
        if (date1 == "" || date2 == "") {
            alert('日付を入力して下さい。');
            return false;
        }
        //大小チェック
        if (parseInt(tmp_s) > parseInt(tmp_e)) {
            alert('日付範囲が不正です。');
            return false;
        }
        //今年度の範囲内チェック・・・（日付＜１学期開始日付　日付＞最終学期終了日付）
        if (parseInt(tmp_s) < parseInt(seme_s[0]) || parseInt(tmp_s) > parseInt(seme_e[parseInt(gakki_su)-1]) || 
            parseInt(tmp_e) < parseInt(seme_s[0]) || parseInt(tmp_e) > parseInt(seme_e[parseInt(gakki_su)-1]) ){
            alert('今年度の範囲内の日付を入力して下さい。');
            return false;
        }
        //学期またがりチェック・・・（日付＞＝学期開始日付　学期終了日付＞＝日付）
        for (var i = 0; i < parseInt(gakki_su); i++){
            if (parseInt(tmp_s) >= parseInt(seme_s[i]) && parseInt(seme_e[i]) >= parseInt(tmp_s)) {
                //学期の取得
                document.forms[0].DATE_SEME.value = i+1;
            }
            if (parseInt(tmp_e) >= parseInt(seme_s[i]) && parseInt(seme_e[i]) >= parseInt(tmp_e)) {
                //学期の取得
                document.forms[0].DATE_SEME2.value = i+1;
            }
        }

        if (tmp_seme != document.forms[0].DATE_SEME.value || tmp_seme2 != document.forms[0].DATE_SEME2.value) {
            AllClearList();
            document.forms[0].selectdata.value = "";

            document.forms[0].cmd.value = "init";
            document.forms[0].submit();
            return false;
        }
    }

    document.forms[0].encoding = "application/x-www-form-urlencoded";
    //テンプレート格納場所
    urlVal = document.URL;
    urlVal = urlVal.replace("http://", "");
    var resArray = urlVal.split("/");
    var fieldArray = fileDiv.split(":");
    urlVal = "/usr/local/" + resArray[1] + "/src/etc_system/XLS_TEMP_" + schoolCd + "/CSV_Template" + fieldArray[0] + "." + fieldArray[1];
    document.forms[0].TEMPLATE_PATH.value = urlVal;

    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJX";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
//権限チェック
function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}

//セキュリティーチェック
function OnSecurityError()
{
    alert('{rval MSG300}' + '\n高セキュリティー設定がされています。');
    closeWin();
}
