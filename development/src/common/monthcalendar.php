<?php

require_once('for_php7.php');

# kanji=漢字
# $Id: monthcalendar.php 74681 2020-06-03 07:10:21Z yamashiro $

class MonthCalendar
{
    var $date;
    var $era;
    var $year;
    var $month;
    var $name;
    var $reload;
    var $frame;
    var $form;
    var $extra;

    function MonthCalendar(){
    }
    function main($rq){

        if ($rq["name"]){
            $this->name = $rq["name"];      //日付を反映させるテキストボックス
        }
        if ($rq["reload"]){
            $this->reload = $rq["reload"];  //リロード
        } else {
            $this->reload = false;  //リロード
        }
        if ($rq["frame"]){
            $this->frame = $rq["frame"];    //フレーム名
        }
        if ($rq["date"]){
            $this->date = $rq["date"];    //引数
        }
        $this->extra = "";
        if ($rq["extra"]){
            $this->extra = $rq["extra"];    //画面呼出後に実行するメソッド
        }

        // 初期
        if (preg_match("/([0-9]{2})\/([0-9]{2})/", $rq["date"], $regs)){
            //$this->era   = $regs[1];
            $this->year   = $regs[2];
            $this->month  = (int) $regs[3];
        }else{
            list($this->year, $this->month, $this->day) = explode("/", Date_Calc::dateNow("%Y/%m/%d"));
            $this->year   = (int) $this->year;
            $this->month  = (int) $this->month;
        }

        $this->form = new form();

        //hiddenを作成する
        $this->form->ae( array("type"      => "hidden",
                            "name"      => "sdate"
                            ) );
        $this->form->ae( array("type"      => "hidden",
                            "name"      => "reload",
                            "value"      => $this->reload
                            ) );
    }

    //入力パラメタ取得
    function getDate(){
        return $this->date;
    }
    //元号取得
    function getEra(){
        return $this->era;
    }
    //年取得
    function getYear(){
        return $this->year;
    }
    //月取得
    function getMonth(){
        return $this->month;
    }
    //名前取得
    function getName(){
        return $this->name;
    }
    //再更新フラグ取得
    function getReload(){
        return $this->reload;
    }
    //フォームタブ(開始)
    function getStart(){
        return $this->form->get_start("cal", "POST", "monthcalendar.php", "", "cal");
    }
    //フォームタブ(終了)
    function getFinish(){
        return $this->form->get_finish();
    }
    //フレーム取得
    function getFrame(){
        return $this->frame;
    }
    //フレーム取得
    function getExtra(){
        return $this->extra;
    }
    //カレンダー作成
    function getCalendarMonth(){
        //カレンダー作成
        return  Date_Calc::getCalendarMonth($this->month, $this->year, "%Y/%m/%d");
    }
}

if ($_REQUEST["CAL_SESSID"] == "") exit;
$sess_cal = new APP_Session($_REQUEST["CAL_SESSID"], 'MonthCalendar');
if (!$sess_cal->isCached($_REQUEST["CAL_SESSID"], 'MonthCalendar')) {
    $sess_cal->data = new MonthCalendar();
}
$sess_cal->data->main($_REQUEST);

$objCal = $sess_cal->getData();
?>
<html>
<head>
<title>月入力</title>
<meta http-equiv="Content-Type" content="text/html; charset=<?php echo CHARSET ?>">
<link rel="stylesheet" href="gk.css">
    <style>
table.calendar {
	padding: 0;
	margin: 0px;
}
table.calendar tr.headline td{
	font-size: 12px;
}
table.calendar th {
	text-align: left;
	vertical-align: bottom;
	color: #777777;
}
table.calendar th span {
	cursor: pointer;
	float: right;
	height: 20px;
	vertical-align: bottom;
	line-height: normal;
}
/* Add by PP for PC-talker 読み start 2020/06/02 */
table.calendar td a.linkClass{
	border-bottom: solid 1px #CCCCCC;
	text-align: right;
	width: 36px;
	height: 20px;
	vertical-align: bottom;
	line-height: normal;
	color: #333333;
    text-decoration: none;
}
/* Add by PP for PC-talker 読み end 2020/06/02 */
table.calendar td.click {
	cursor: pointer;
	background-color: #FFFFFF;
}
/* Add by PP for PC-talker 読み start 2020/06/02 */
table.calendar td.Today a{
	font-weight: bolder;
	color: #cc0000;
	cursor: pointer;
}
/* Add by PP for PC-talker 読み end 2020/06/02 */
table.calendar td.Weekday {
	color: #000000;
	background-color: #FFFFFF;
	cursor: pointer
}
    </style>
<script language="JavaScript">
//  Add by PP for PC-talker 読み start 2020/06/02 
window.onload = function () {
     if (sessionStorage.getItem("monthcalendar_CurrentCursor") != null) {
         document.title = "";
         document.getElementById(sessionStorage.getItem("monthcalendar_CurrentCursor")).focus();
         // remove item
         sessionStorage.removeItem('monthcalendar_CurrentCursor');
     } else {
         document.getElementById('cal_1').focus();
     }
}

function current_cursor(para){
    sessionStorage.setItem("monthcalendar_CurrentCursor", para);
}
//  Add by PP for PC-talker 読み end 2020/06/02 
var  f = <?php echo $objCal->getFrame(); ?>;
</script>
</head>
<!--  Add by PP for PC-talker 読み start 2020/06/02  -->
<body bgcolor="#ffffff" text="#000000" leftmargin="5" topmargin="20" marginwidth="5" marginheight="5" id="cal_1" >
<!-- Add by PP for PC-talker 読み end 2020/06/02  -->
<?php echo $objCal->getStart() ?>
    <script langage="JavaScript" type="text/javascript" src="js/jquery-1.11.0.min.js"></script>
    <script langage="JavaScript" type="text/javascript" src="common.js"></script>
    <script langage="JavaScript" type="text/javascript" src="defcalendar.js"></script>
    <script langage="JavaScript" type="text/javascript">
    getWarekiListSync();

    returnValue="";
    function send(sdate)
    {
        f.document.forms[0]['<?php echo $objCal->getName() ?>'].value=sdate;
        // Add by PP for PC-talker 読み start 2020/06/02  
        f.document.forms[0]['<?php echo $objCal->getName() ?>'].focus();
        // Add by PP for PC-talker 読み end 2020/06/02 
        f.closeit();
        <?php 
        if (mb_strlen($objCal->getExtra()) > 0) {
            echo "return f.".$objCal->getExtra();
        } else {
            echo "return ;";
        }
        ?>
    }
    var today = new Date();
    var cal_year = today.getYear();
    var cal_month = today.getMonth() + 1;
    var cal_day = today.getDate();
    var cal_era = toGengo(cal_year, cal_month, cal_day);
    if (cal_year < 1900) cal_year += 1900;
    var today_match = cal_year + "/" + cal_month + "/" + cal_day;
//    document.write("<div id='calendar' align='center'></div>");
//    var cal = document.getElementById("calendar");
    var cal = document.createElement("div");
    cal.id = "calendar";
    cal.align = "center";
    var body = document.getElementsByTagName("body").item(0);
    body.appendChild(cal);
    var defaultBackgroundColors = new Object();
    var to_year = cal_year;
    var to_month = cal_month;
    var to_day = cal_day;
    

    //初期表示
    function writeCal0(){
        var reqday="<?php echo $objCal->getDate() ?>";
        splitday(reqday);
        writeCal(cal_year,cal_month,cal_day);
    }
    //西暦年・月日に分解
    function splitday(reqday){
        var myArray=new Array();
        if (reqday.length > 0) {
            myArray=reqday.split("/");
        }
        var i, l, y0, y, m, d, max, nen, jy;
        max=myArray.length; y0=0;
        y=cal_year;
        m=cal_month;
        d=cal_day;
        for(i=0;i<max;i++){
            if (i==0){
                nen=myArray[0].charAt(0);
                for(l=0;l<def_nengo.length;l++){
                    if (nen==nengoID[l]){
                        y0=nengoYear[l];
                        break;
                    }
                }
                //alert(y0+"年号年");
                if(y0==0)  //年号未定義は終了
                    break;
                nen=myArray[0].substr(1,3);
                jy=parseInt(nen,10);
                if (jy>0) y=y0+jy-1;
                else      y=y0+jy;
                //alert(y+"年");
                if(max==1){  //年号のみ定義
                    break;
                }
            }
            else if(i==1){
                m=parseInt(myArray[1],10);
                //alert(m+"月");
                if (jy<=1)
                    d=GetMaxDay(y, m);
                else
                    d=1;
                if(max==2)
                    break;
            }
            else{
                d=parseInt(myArray[2],10);
                //alert(d+"日");
            }
        }
        cal_year=y;
        cal_month=m;
        cal_day=d;
        //alert(cal_year+"年"+cal_month+"月"+cal_day+"日");
    }
    //コンボボックスの変更
    function tdChg(){
        var index = def_nengo.length-document.getElementsByName("nengo")[0].selectedIndex;
        var yy=0;
        var y0 = nengoYear[index-1];
        var y = document.getElementsByName("year")[0].selectedIndex;
        //年号の代り目処理
        if(y==0){
            //初年度の時
            cal_year=y0+y;
            if(cal_month>nengoMonth[index-1])
                cal_month=nengoMonth[index-1];
            cal_day=GetMaxDay(cal_year, cal_month);
        }
        else {
            //最終年度の時
            if (index==def_nengo.length)
                //yy=to_year-y0;	2008/07/30 +9を追加
                yy=to_year-y0+9;
            else
                yy=nengoYear[index]-y0;
            if(yy<y){
                cal_year=y0+yy;
                cal_month=nengoMonth[index-1];
            }
            else {
                cal_year=y0+y;
            }
            cal_day=1;
        }
        writeCal(cal_year,cal_month,cal_day);
    }
    //月をクリック
    //結果を書込みクローズ
    function tdClick(num){
        var index = def_nengo.length-document.getElementsByName("nengo")[0].selectedIndex;
        var year = document.getElementsByName("year")[0].selectedIndex+nengoYear[index-1];
        var month = "0"+num;
        var nengo = toGengo(year, num, cal_day);
        year = "0"+(year-nengoYear[nengo-1]+1);
        year = year.substr(year.length-2,2);
        month = month.substr(month.length-2,2);
        var str=nengoID[nengo-1]+year+"/"+month;
        send(str);
    }
    //月マウスオン
    function tdOver(obj){
        defaultBackgroundColors[obj] = obj.style.backgroundColor;
        obj.style.backgroundColor = '#E8EEF9';
    }
    //月マウスオフ
    function tdOut(obj){
        obj.style.backgroundColor = defaultBackgroundColors[obj];
    }
    //月移動マウスオン
    function spanOver(obj){
        defaultBackgroundColors[obj] = obj.style.backgroundColor;
        obj.style.color = '#FF9900';
    }
    //月移動マウスオフ
    function spanOut(obj){
        obj.style.color = defaultBackgroundColors[obj];
    }
    //今月の今日へ
    function currentCal(){
        cal_year = to_year;
        cal_month = to_month;
        cal_day = to_day;
        writeCal(cal_year,cal_month,cal_day);
    }
    //前月の最終日へ
    function prevCal(){
        cal_month -= 1;
        if(cal_month < 1){
            cal_month = 12;
            cal_year -= 1;
        }
        cal_day = GetMaxDay(cal_year, cal_month);
        writeCal(cal_year,cal_month,cal_day);
    }
    //翌月の1日へ
    function nextCal(){
        cal_month += 1;
        if(cal_month > 12){
            cal_month = 1;
            cal_year += 1;
        }
        cal_day = 1;
        writeCal(cal_year,cal_month,cal_day);
    }

    //HTML画面
    function writeCal(year,month,day){
        var monthName = new Array('１月','２月','３月','４月','５月','６月','７月','８月','９月','10月','11月','12月');
        var max_day = GetMaxDay(year, month);
        var cal_tags = "<form name=datetable>";

        //年号取出し
        var n, i;
        var nengo, nenlen, jyear, y, m;
        var str="";
        nengo=toGengo(year, month, day);
        jyear=year-nengoYear[nengo-1]+1;
        if(nengo==def_nengo.length){
            //nenlen=to_year-nengoYear[nengo-1]+1;	2008/07/30 +9を追加
            nenlen=to_year-nengoYear[nengo-1]+1+9;
            if (jyear>nenlen){
                jyear=1; cal_year-=(nenlen-1);
            }
        }
        else
            nenlen=nengoYear[nengo]-nengoYear[nengo-1]+1;

        if(day > max_day)
            day = max_day;

        //年号コンボボックス
        n=def_nengo.length;
        // Add by PP for PC-talker 読み start 2020/06/02
        cal_tags += ("<SELECT size=\"1\" name=\"nengo\" id=\"nengo\" onChange='current_cursor('nengo');tdChg();'>");
        // Add by PP for PC-talker 読み end 2020/06/02
        while(n>0){
            if(n==nengo)
                cal_tags += ("<option value=\""+n+"\" SELECTED>"+def_nengo[n-1]+"</option>");
            else
                cal_tags += ("<option value=\""+n+"\">"+def_nengo[n-1]+"</option>");
            n--;
        }
        cal_tags += ("</SELECT>  \n");
        //年コンボボックス
        // Add by PP for PC-talker 読み start 2020/06/02
        cal_tags += "<SELECT size=\"1\" name=\"year\" id=\"year\" onChange='current_cursor('year');tdChg();'>";
        // Add by PP for PC-talker 読み end 2020/06/02
        for(i=1;i<=nenlen;i++){
            if (i<10) str=" "+i;
            else      str=i;
            if (i==jyear)
                cal_tags += ("<option value=\""+str+"\" SELECTED>"+str+"</option>");
            else
                cal_tags += ("<option value=\""+str+"\">"+str+"</option>");
        }
        cal_tags += "</SELECT> 年 <br>";

        //月移動
        cal_tags += "<table border='0' cellspacing='1' cellpadding='2' class='calendar'>";
        // Add by PP for PC-talker 読み start 2020/06/02
        cal_tags += "<tr><th colspan='4' aria-label = \"\">";
        // Add by PP for PC-talker 読み end 2020/06/02
        cal_tags += "<span onMouseOver='spanOver(this);' onMouseOut='spanOut(this);' onClick='nextCal();'>→次月</span>";
       // cal_tags += "<span onMouseOver='spanOver(this);' onMouseOut='spanOut(this);' onClick='currentCal();'>今月</span>";
        cal_tags += "<span onMouseOver='spanOver(this);' onMouseOut='spanOut(this);' onClick='prevCal();'>前月←</span>";
        y = "0"+jyear;
        m = "0"+month;
        y = y.substr(y.length-2,2);
        m = m.substr(m.length-2,2);
        cal_tags += (nengoID[nengo-1] + y + "/" + m + "</th></tr>");
        for(var i=1;i<=monthName.length;i++){
            if (i == cal_month){
                dayClass = ' class="Today"';
            }
            else {
                dayClass = ' class="Weekday"';
            }
            // Add by PP for PC-talker 読み (<a>) start 2020/06/02 
            cal_tags += ("<td onClick='tdClick("+i+");' onMouseOver='tdOver(this);' onMouseOut='tdOut(this);'"+dayClass+"><a href='#' class='linkClass'>" + monthName[i-1] + "</a></td>");
            // Add by PP for PC-talker 読み end 2020/06/02 
            if ((i%4)==0)
                cal_tags += "</tr><tr>";
        }
        cal_tags += "</tr>";
        cal_tags += "</table></form>";
        cal.innerHTML = cal_tags;
    }
    writeCal0();
    </script>
<input type="hidden" name="CAL_SESSID" value="<?php echo $_REQUEST["CAL_SESSID"] ?>">
<?php echo $objCal->getFinish() ?>
</body>
</html>
