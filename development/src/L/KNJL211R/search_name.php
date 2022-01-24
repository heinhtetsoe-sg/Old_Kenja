<?php

require_once('for_php7.php');

//氏名かな検索
class SearchNames
{
    var $form;
    var $year;
    var $names;
    var $name_kana;
    var $names2;
    var $name_kana2;
    var $frame;         //フレーム名
    var $cmd;           //コマンド
    
    function SearchNames(){
        $this->form = new form();
    }
    
    //氏名リスト作成
    function getNameList(){
        $opt = array();
        if ($this->cmd === 'search' && ( strlen($this->names) || strlen($this->name_kana) || strlen($this->applicantDiv) || strlen($this->testDiv) || strlen($this->page) || strlen($this->seq) )){
            $db = Query::dbCheckOut();
            $query = "";
            $query .= " SELECT ";
            $query .= "     T1.APPLICANTDIV, ";
            $query .= "     T1.TESTDIV, ";
            $query .= "     L1.ABBV1, ";
            $query .= "     T1.BEFORE_PAGE, ";
            $query .= "     T1.BEFORE_SEQ, ";
            $query .= "     FIN.FINSCHOOL_NAME AS FS_NAME, ";
            $query .= "     T1.NAME, ";
            $query .= "     T1.NAME_KANA, ";
            $query .= "     T1.SEX ";
            $query .= " FROM ";
            $query .= "     ENTEXAM_APPLICANT_BEFORE_DAT T1 ";
            $query .= "     LEFT JOIN FINSCHOOL_MST FIN ON T1.FS_CD = FIN.FINSCHOOLCD ";
            $query .= "     LEFT JOIN V_NAME_MST L1 ON L1.NAMECD1 = 'L004' ";
            $query .= "                            AND L1.NAMECD2 = T1.TESTDIV ";
            $query .= "                            AND L1.YEAR    = T1.ENTEXAMYEAR ";
            $query .= " WHERE ";
            $query .= "     T1.ENTEXAMYEAR   = '".$this->year."' ";
            if (strlen($this->names)){
                $query .= "     AND T1.NAME like '%" .$this->names2 ."%' ";
            }
            if (strlen($this->name_kana)){
                $query .= "     AND T1.NAME_KANA like '%" .$this->name_kana2 ."%' ";
            }
            
            if (strlen($this->applicantDiv)) {
                $query .= "     AND T1.APPLICANTDIV  = '".$this->applicantDiv."' ";
            }
            if (strlen($this->testDiv)) {
                $query .= "     AND T1.TESTDIV       = '".$this->testDiv."' ";
            }
            if (strlen($this->page)) {
                $query .= "     AND T1.BEFORE_PAGE   = '".$this->page."' ";
            }
            if (strlen($this->seq)) {
                $query .= "     AND T1.BEFORE_SEQ    = '".$this->seq."' ";
            }
            $query .= " ORDER BY ";
            $query .= "     T1.APPLICANTDIV, ";
            $query .= "     T1.TESTDIV, ";
            $query .= "     T1.BEFORE_PAGE, ";
            $query .= "     T1.BEFORE_SEQ ";

            //受験番号、氏名、氏名かな取得
            $result = $db->query($query);
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $opt[] = array("label" => $row["ABBV1"]."　".$row["BEFORE_PAGE"]."-".$row["BEFORE_SEQ"]."　".$row["NAME"]."　(".$row["NAME_KANA"].")",
                               "value" => $row["APPLICANTDIV"]."-".$row["TESTDIV"]."-".$row["BEFORE_PAGE"]."-".$row["BEFORE_SEQ"]
                               );
            }
            Query::dbCheckIn($db);    
        }
        $this->form->ae( array("type"       => "select",
                               "name"       => "NAMELIST",
                               "size"       => "11",
                               "value"      => "",
                               "extrahtml"   => " ondblclick=\"apply_name(this);\" STYLE=\"WIDTH:100%\"",
                               "options"    => (is_array($opt))? $opt : array()));

        return $this->form->ge("NAMELIST");
    }
    function main($rq){
        $db = Query::dbCheckOut();
        //テキストボックスの名前
        //シングルクォートが入力されたらSQL用にエスケープする
        $exist = strstr($rq["names"], "'");
        if ($exist == false) {
            $this->names2 = $rq["names"];
        } else {
            $plode = explode("'", $rq["names"]);
            for ($i=0; $i<get_count($plode); $i++) {
                if ($i != 0) {
                    $imp = $imp . "''";
                }
                $imp = $imp . $plode[$i];
            }
            $this->names2 = $imp;
        }

        $imp = "";
        $exist = strstr($rq["name_kana"], "'");
        if ($exist == false) {
            $this->name_kana2 = $rq["name_kana"];
        } else {
            $plode = explode("'", $rq["name_kana"]);
            for ($i=0; $i<get_count($plode); $i++) {
                if ($i != 0) {
                    $imp = $imp . "''";
                }
                $imp = $imp . $plode[$i];
            }
            $this->name_kana2 = $imp;
        }

        $this->names        = $rq["names"];
        $this->name_kana    = $rq["name_kana"];
        $this->page = $rq["page"];
        $this->seq = $rq["seq"];
        $this->applicantDiv = $rq["applicantDiv"];
        $this->testDiv = $rq["testDiv"];

        if ($rq["year"]){
            $this->year = $rq["year"];
        }

        if ($rq["frame"]){
            $this->frame = $rq["frame"];      //フレーム名
        }
        $this->cmd = $rq["cmd"];              //コマンド
        
        Query::dbCheckIn($db);
    }

    //入試制度
    function getApplicantDiv(){
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$this->year."' ";
        $query .= "     AND NAMECD1 = 'L003' ";
        $query .= "     AND NAMECD2 = '".$this->applicantDiv."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        //入試制度コンボ
        $extra = "";
        $retVal = $this->makeCmb($db, $query, $this->applicantDiv, "applicantDiv", $extra, 1, "");
        return $retVal;

    }

    //入試区分
    function getTestDiv(){
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || ABBV1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$this->year."' ";
        $query .= "     AND NAMECD1 = 'L004' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        //入試区分
        $extra = "";
        $retVal = $this->makeCmb($db, $query, $this->testDiv, "testDiv", $extra, 1, "BLANK");
        Query::dbCheckIn($db);
        return $retVal;
    }

    //事前番号
    function getBefText(){
        //頁
        $this->form->ae( array("type"        => "text",
                               "name"        => "page",
                               "size"        => 3,
                               "maxlength"   => 3,
                               "extrahtml"   => "",
                               "value"       => $this->page ));

        //連番
        $this->form->ae( array("type"        => "text",
                               "name"        => "seq",
                               "size"        => 3,
                               "maxlength"   => 3,
                               "extrahtml"   => "",
                               "value"       => $this->seq ));
        return $this->form->ge("page")." - ".$this->form->ge("seq");
    }

    //氏名テキストボックス
    function getTextName(){
        //氏名
        $this->form->ae( array("type"        => "text",
                               "name"        => "names",
                               "size"        => 31,
                               "maxlength"   => 16,
                               "extrahtml"   => "",
                               "value"       => $this->names ));

        return $this->form->ge("names");
    }
    
    //氏名かなテキストボックス
    function getTextName_Kana(){
        //氏名かな
        $this->form->ae( array("type"        => "text",
                               "name"        => "name_kana",
                               "size"        => 31,
                               "maxlength"   => 40,
                               "extrahtml"   => "",
                               "value"       => $this->name_kana ));

        return $this->form->ge("name_kana");
    }

    //検索ボタン作成
    function getBtnSearch(){
        //検索ボタンを作成する
        $this->form->ae( array("type"        => "button",
                               "name"        => "search",
                               "value"       => "検 索",
                               "extrahtml"   => "onclick=\"return btn_submit('search')\"" ) );
                               
        //終了ボタン
        $this->form->ae( array("type"      => "button",
                               "name"      => "close",
                               "value"     => "戻 る",
                               "extrahtml" => "onclick=\"f.closeit();\"" ) );

        return $this->form->ge("search") . $this->form->ge("close");
    }    
 
    //makeCmb
    function makeCmb($db, $query, &$value, $name, $extra, $size, $blank = "")
    {
        $opt = array();
        if ($blank == "BLANK") {
            $opt[] = array("label" => "", "value" => "");
        }
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($value == $row["VALUE"]) $value_flg = true;
        }
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
        $this->form->ae( array("type"       => "select",
                               "name"       => $name,
                               "size"       => $size,
                               "value"      => $value,
                               "extrahtml"  => $extra,
                               "options"    => $opt));
        return $this->form->ge($name);

    }

     //フォームタブ(開始)
    function getStart(){
        return $this->form->get_start("name", "POST", "search_name.php", "", "name");
    }
    //フォームタブ(終了)
    function getFinish(){
        return $this->form->get_finish();
    }
    //フレーム取得
    function getFrame(){
        return $this->frame;
    }
}

if ($_REQUEST["NAME_SESSID"] == "") exit;
$sess = new APP_Session($_REQUEST["NAME_SESSID"], 'NAME');
if (!$sess->isCached($_REQUEST["NAME_SESSID"], 'NAME')) {
    $sess->data = new SearchNames();
}
$sess->data->main($_REQUEST);

$objName = $sess->getData();

?>
<html>
<head>
<title>カナ検索</title>
<meta http-equiv="Content-Type" content="text/html; charset=<?php echo CHARSET ?>">
<style type="text/css">
<!--
a:hover { color: #330066; font-weight:bold;}
table { font-family: "ＭＳ ゴシック", "Osaka"; font-size: 3.8mm; }
td { font-family: "ＭＳ ゴシック", "Osaka"; font-size: 3.8mm; }
th { font-family: "ＭＳ ゴシック", "Osaka"; font-size: 3.8mm; font-weight:normal;}
td.cln { font-family: "ＭＳ ゴシック", "Osaka"; font-size: 4.0mm; }
body { 
  font-family: "ＭＳ ゴシック", "Osaka"; 
  font-size: 3.8mm;
}
/*
body { 
  font-family: "ＭＳ ゴシック", "Osaka"; 
  font-size: 3.8mm;
  background-image:url(../image/menu/bg.gif); 
  background-attachment:fixed; 
}*/

.err { font-size:4.5mm;color:red;font-weight:bold; }

a.dontdel:link { color: #999999; text-decoration:none; }
a.dontdel:visited{ color: #999999; text-decoration:none; }

.no_search { background-color: #316F9B;color: #ffffff; } 
.search { background-color: #3399cc;color: #ffffff; } 
tr.no_search_line { background-color: #255476; } 
tr.search_line { background-color: #000066; }

INPUT{
  font-family: "ＭＳ ゴシック";
}
SELECT{
  font-family : "ＭＳ ゴシック";
  font-weight : normal;
}
-->
</style>
<script language="JavaScript">
<!--
var  f = <?php echo $objName->getFrame() ?>;

function setAddrText(obj){
    try{
        var val = obj.options[obj.selectedIndex].text;    
        var arr = val.split("　");
        document.forms[0].names.value = arr[0];
        document.forms[0].name_kana.value = arr[1];
    }catch(e){
    }
}

function apply_name(obj) {
    if(obj.selectedIndex >= 0){
        var val = obj.options[obj.selectedIndex].value;    
        var key = val.split("-");
        f.document.forms[0]['APPLICANTDIV'].value = key[0];
        f.document.forms[0]['TESTDIV'].value = key[1];
        f.document.forms[0]['BEFORE_PAGE'].value = key[2];
        f.document.forms[0]['BEFORE_SEQ'].value = key[3];
        if(f.btn_submit('reference') != true) {
            f.closeit();
        }
    } else {
        alert("名前が選択されていません");
    }
 
}
function btn_submit(cmd){
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
// -->
</script>
</head>
<body bgcolor="#ffffff" text="#000000" leftmargin="0" topmargin="0" marginwidth="5" marginheight="5"
link="#006633" vlink="#006633" alink="#006633">
<?php echo $objName->getStart() ?>
<table width="100%" border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td >
      <table width="100%" border="0" cellspacing="0" cellpadding="0">
        <tr class="no_search_line">
          <td>
            <table width="100%" border="0" cellspacing="1" cellpadding="2">
              <tr class="no_search">
                <th nowrap width="30%" align="right">入試制度</th>
                <td width="*" bgcolor="#ffffff"><?php echo $objName->getApplicantDiv() ?></td>
              </tr>
              <tr class="no_search">
                <th nowrap width="30%" align="right">入試区分</th>
                <td width="*" bgcolor="#ffffff"><?php echo $objName->getTestDiv() ?></td>
              </tr>
              <tr class="no_search">
                <th nowrap width="30%" align="right">事前番号</th>
                <td width="*" bgcolor="#ffffff" style="color:black;"><?php echo $objName->getBefText() ?></td>
              </tr>
              <tr class="no_search">
                <th nowrap width="30%" align="right">氏名</th>
                <td width="*" bgcolor="#ffffff"><?php echo $objName->getTextName() ?></td>
              </tr>
              <tr class="no_search">
                <th nowrap width="30%" align="right">氏名カナ</th>
                <td bgcolor="#ffffff"><?php echo $objName->getTextName_Kana() ?></td>
              </tr>
              <tr class="no_search">
                <td colspan="2"  bgcolor="#ffffff"><?php echo $objName->getBtnSearch() ?></td>
              </tr>
              <tr class="no_search">
                <td colspan="2" bgcolor="#ffffff"><?php echo $objName->getNameList() ?></td>
              </tr>
            </table>
          </td>
        </tr>
      </table>
    </td>
  </tr>
</table>
<input type="hidden" name="NAME_SESSID" value="<?php echo $_REQUEST["NAME_SESSID"] ?>">
<input type="hidden" name="cmd">
<?php echo $objName->getFinish() ?>
</body>
</html>
