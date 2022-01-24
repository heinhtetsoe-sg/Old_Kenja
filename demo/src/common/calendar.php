<?php

require_once('for_php7.php');

class Calendar
{
    var $year;
    var $month;
    var $day;
    var $name;
    var $reload;
    var $frame;
    var $form;
    var $extra;
    public $warekiList = array();

    # kanji=漢字
    # $Id: calendar.php 65491 2019-02-04 10:52:00Z kawata $
    //コンボボックス用の配列に変換
    function Calc_Wareki(&$year, $month, $day)
    {
        $border = array();

        if (empty($this->warekiList)) {
            $this->warekiList = common::getWarekiList();
        }
        
        for ($i = 0; $i < get_count($this->warekiList); $i++) {
            $warekiInfo = $this->warekiList[$i];
            $start = str_replace("/", "", $warekiInfo['Start']);
            $end = str_replace("/", "", $warekiInfo['End']);
            $border[] = array("開始日" =>  $start, "終了日" => $end, "元号" => $warekiInfo['Name']);
        }

        $target = sprintf("%04d%02d%02d", $year, $month, $day);
        for ($i = 0; $border[$i]; $i++){
            if ($border[$i]["開始日"] <= $target &&
                $target <= $border[$i]["終了日"] ){
                $year = ($year - substr($border[$i]["開始日"], 0, 4) + 1);
                return $border[$i]["元号"] .(($year == 1)? "元年" : sprintf("%2d", (int) $year)."年");
            }

        }
        return false;
    }

	function alter_option_array($label, $value)
	{
	    $label = $label ."(" . Calendar::Calc_Wareki($label, 12, 31) .")";
	    return array("label" => $label, "value" => $value);
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
        $this->extra = "";
        if ($rq["extra"]){
            $this->extra = $rq["extra"];    //画面呼出後に実行するメソッド
        }
        if (preg_match("/([0-9]{4})\/([0-9]{2})\/([0-9]{2})/", $rq["sdate"], $regs)){
            $this->year   = (int) $regs[1];
            $this->month  = (int) $regs[2];
            $this->day    = (int) $regs[3];
        }elseif(isset($rq["year"]) || isset($rq["month"])){
            $this->year   = (int) $rq["year"];
            $this->month  = (int) $rq["month"];
        }elseif (preg_match("/([0-9]{4})\/([0-9]{2})\/([0-9]{2})/", $rq["date"], $regs)){
            $this->year   = (int) $regs[1];
            $this->month  = (int) $regs[2];
            $this->day    = (int) $regs[3];
        }else{
            list($this->year, $this->month, $this->day) = explode("/", Date_Calc::dateNow("%Y/%m/%d"));
            $this->year   = (int) $this->year;
            $this->month  = (int) $this->month;
            $this->day    = (int) $this->day;
        }
        $this->form = new form();

        $item = range(1900, 2050);
        $vals = array_values($item);
        $opt = array_map("Calendar::alter_option_array", $item, $vals);

        $this->form->ae( array("type"       => "select",
                            "name"       => "year",
                            "size"       => "1",
                            "value"      => $this->year,
                            // Add by PP for PC-talker 読み start 2020/02/03
                            "extrahtml"  => "id=\"year\" onChange=\"current_cursor('year'); document.forms[0].submit();\"",
                            // Add by PP for PC-talker 読み end 2020/02/20
                            "options"    => $opt));

        $opt = array();
        for ($i = 1; $i <=12; $i++){
            $opt[] = array("label"  => $i ."月",
                           "value"  => $i
            );
        }

         $this->form->ae( array("type"       => "select",
                            "name"       => "month",
                            "size"       => "1",
                            "value"      => $this->month,
                            // Add by PP for PC-talker 読み start 2020/02/03
                            "extrahtml"  => "id=\"month\" onChange=\"current_cursor('month'); document.forms[0].submit();\"",
                            // Add by PP for PC-talker 読み end 2020/02/20
                            "options"    => $opt));
        
        //hiddenを作成する
        $this->form->ae( array("type"      => "hidden",
                            "name"      => "sdate"
                            ) );
        $this->form->ae( array("type"      => "hidden",
                            "name"      => "reload",
                            "value"      => $this->reload
                            ) );
    }
    //年コンボボックス
    function getSelYear(){
        return $this->form->ge("year");
    }
    //月コンボボックス
    function getSelMonth(){
        return $this->form->ge("month");
    }
    //年取得
    function getYear(){
        return $this->year;
    }
    //月取得
    function getMonth(){
        return $this->month;
    }
    //日取得
    function getDay(){
        return $this->day;
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
        return $this->form->get_start("cal", "POST", "calendar.php", "", "cal");
    }
    //フォームタブ(終了)
    function getFinish(){
        return $this->form->get_finish();
    }
    //フレーム取得
    function getFrame(){
        return $this->frame;
    }
    function getExtra(){
        return $this->extra;
    }
    //カレンダー作成
    function getCalendarMonth(){
        //カレンダー作成
        return  Date_Calc::getCalendarMonth($this->month, $this->year, "%Y/%m/%d");
    }
    //昨年取得
    function getPreYear(){
        return Date_Calc::daysToDate(Date_Calc::dateToDays($this->day,$this->month,$this->year-1), "%Y/%m/%d");
    }
    //先月取得
    function getPreMonth(){
        return Date_Calc::daysToDate(Date_Calc::dateToDays($this->day,$this->month-1,$this->year), "%Y/%m/%d");
    }
    //来月取得
    function getNextMonth(){
        return Date_Calc::daysToDate(Date_Calc::dateToDays($this->day,$this->month+1,$this->year), "%Y/%m/%d");
    }
    //来年取得
    function getNextYear(){
        return Date_Calc::daysToDate(Date_Calc::dateToDays($this->day,$this->month,$this->year+1), "%Y/%m/%d");
    }
    //今日取得
    function getToday(){
        return Date_Calc::dateNow("%Y/%m/%d");
    }
    function getTodayStr(){
        return sprintf("%d年%d月%d日", $this->year, $this->month, $this->day);
    }
}
$week_font_color    = array("#ff0000","#000000","#000000",
                            "#000000","#000000","#000000","#0000ff" );
$week_bgc_color     = array("#ffcccc","#ccffcc","#ccffcc","#ccffcc",
                            "#ccffcc","#ccffcc","#ccccff" );

if ($_REQUEST["CAL_SESSID"] == "") exit;
$sess_cal = new APP_Session($_REQUEST["CAL_SESSID"], 'Calendar');
if (!$sess_cal->isCached($_REQUEST["CAL_SESSID"], 'Calendar')) {
    $sess_cal->data = new Calendar();
}
$sess_cal->data->main($_REQUEST);

$objCal = $sess_cal->getData();
?>
<html>
<head>
<title>カレンダー</title>
<meta http-equiv="Content-Type" content="text/html; charset=<?php echo CHARSET ?>">
<link rel="stylesheet" href="gk.css">
<script language="JavaScript">
//  Add by PP for PC-talker 読み start 2020/02/03
 window.onload = function () {
     if (sessionStorage.getItem("calendar_CurrentCursor") != null) {
            document.title = "";
            if(sessionStorage.getItem("calendar_CurrentCursor") == 'pyear' || sessionStorage.getItem("calendar_CurrentCursor") == 'nyear'){
                document.getElementById('year').focus();
            } else if(sessionStorage.getItem("calendar_CurrentCursor") == 'pmonth' || sessionStorage.getItem("calendar_CurrentCursor") == 'nmonth'){
                document.getElementById('month').focus();
            } else {
                document.getElementById(sessionStorage.getItem("calendar_CurrentCursor")).focus();
            }
            // remove item
            sessionStorage.removeItem('calendar_CurrentCursor');
        } else {
            document.getElementById('calendar').focus();
        }
}
function current_cursor(para){
    sessionStorage.setItem("calendar_CurrentCursor", para);
}
// text box focus
function current_cursor_focus(para){
    f.document.forms[0][para].focus();
}
//  Add by PP for PC-talker 読み end 2020/02/20

function date_submit(sdate){
    document.forms[0].sdate.value = sdate;
    document.forms[0].submit();
    return false;
}
var  f = <?php echo $objCal->getFrame(); ?>;
</script>
</head>
<body bgcolor="#ffffff" text="#000000" leftmargin="0" topmargin="0" marginwidth="5" marginheight="5"
link="#006633" vlink="#006633" alink="#006633">
<?php echo $objCal->getStart() ?>
<!-- Edit by HPA for PC-talker 読み start 2020/02/03 -->
<table width="100%" border="0" cellspacing="0" cellpadding="0" id = "calendar" aria-label = "ポップアップカレンダー画面">
<!-- Edit by HPA for PC-talker 読み end 2020/02/20 -->
  <tr>
    <td>
      <table width="100%" border="0" cellspacing="0" cellpadding="0">
        <tr>
          <td valign="top">
            <table width="100%" border="0" cellspacing="0" cellpadding="10">
              <tr>
                <td>
                  <table width="100%" border="0" cellspacing="0" cellpadding="0">
                    <tr class="no_search_line">
                      <td>
                        <table width="100%" border="0" cellspacing="1" cellpadding="3">
                          <tr >
                            <td align="center" bgcolor="#ffffff">
                            <!-- Edit by PP for PC-talker 読み start 2020/02/03 -->
                            <a href="#" title="前年に移動" id="pyear" onClick="current_cursor('pyear');return date_submit('<?php echo $objCal->getPreYear() ?>')">&lt;&lt;</a>
                            <a href="#" title="前月に移動" id="pmonth" onClick="current_cursor('pmonth');return date_submit('<?php echo $objCal->getPreMonth() ?>')">&lt;</a>
                            <?php echo $objCal->getSelYear() .'&nbsp;' .$objCal->getSelMonth() ?>
                            <a href="#" aria-label="今日" id="today" onClick="current_cursor('today');return date_submit('<?php echo $objCal->getToday() ?>')">今日</a>
                            <a href="#" title="来月に移動" id="nmonth" onClick="current_cursor('nmonth');return date_submit('<?php echo $objCal->getNextMonth() ?>')">&gt;</a>
                            <a href="#" title="来年に移動" id="nyear" onClick="current_cursor('nyear');return date_submit('<?php echo $objCal->getNextYear() ?>')">&gt;&gt;</a>
                            <!-- Edit by PP for PC-talker 読み end 2020/02/20 -->
                            </td>
                          </tr>
                          <tr bgcolor="#ffffff">
                            <td align="center"><tt><font color="#ff0000">日</font>&nbsp;月&nbsp;火&nbsp;水&nbsp;木&nbsp;金&nbsp;<font color="#0000ff">土</font></tt></td>
                          </tr>
                          <tr height="100" bgcolor="#ffffff">
                            <td align="center">
<?php
    if (is_array($objCal->getCalendarMonth())){
        foreach($objCal->getCalendarMonth() as $val ){
            echo "<tt>\n";
            $dataCnt = 1;
            for($i = 0; $i <= 6; $i++){
                list($y, $m, $d) = explode("/", $val[$i]);
                $n = $objCal->getName();
                $js = "javascript:f.document.forms[0]['$n'].value='" .$val[$i] ."';f.closeit();";
                if  ($objCal->getExtra()){
                    $js .= "f.".$objCal->getExtra().";";
                }
                //再表示
                if ($objCal->getReload() != ''){
                    $js .= "f.document.forms[0].submit();";
                }
                if ($d == $objCal->getDay() && $m == $objCal->getMonth()){
                    //  Add by PP for PC-talker start 2020/02/03
                    $date = sprintf("<a href=\"%s\" onClick=\"current_cursor_focus('$n');\"><font color=\"magenta\"><b>%d</b></font></a>",$js, $d);
                }elseif ($m == $objCal->getMonth()){
                    $date = sprintf("<a href=\"%s\" onClick=\"current_cursor_focus('$n');\"><font color=\"%s\">%d</font></a>", $js, $week_font_color[$i], $d);
                }else{
                    $date = sprintf("<a href=\"%s\" onClick=\"current_cursor_focus('$n');\"><font color=\"#cccccc\">%d</font></a>", $js, $d);
                    //  Add by PP for PC-talker 読み end 2020/02/20
                }
                $frontSp = "";
                if ($d < 10){
                    $frontSp = "&nbsp;";
                }
                $backSp = "";
                if ($dataCnt < 7) {
                    $backSp .= "&nbsp;";
                }
                echo $frontSp.$date.$backSp;
                $dataCnt++;
            }
            echo"</tt><br>\n";
        }
    }
?>                          </td>
                          </tr>
                          <tr bgcolor="#ffffff">
                            <td align="center">選択日は<?php echo $objCal->getTodayStr() ?>です</td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                </td>
              </tr>
            </table>
          </td>
        </tr>
      </table>
    </td>
  </tr>
</table>
<input type="hidden" name="CAL_SESSID" value="<?php echo $_REQUEST["CAL_SESSID"] ?>">
<input type="hidden" name="extra" value="<?php echo $objCal->getExtra() ?>">
<?php echo $objCal->getFinish() ?>
</body>
</html>
