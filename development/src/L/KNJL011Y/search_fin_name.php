<?php

require_once('for_php7.php');

//氏名かな検索（出身学校）
class SearchNames
{
    var $form;
    var $year;
    var $names;
    var $name_kana;
    var $names2;
    var $name_kana2;
    var $examno;
    var $zip;
    var $gzip;
    var $zadd;
    var $gadd;
    var $frame;         //フレーム名
    var $cmd;           //コマンド
    var $schoolcd;
    
    function SearchNames(){
        $this->form = new form();
    }
    
    //氏名リスト作成
    function getNameList(){
        $opt = array();
        if (strlen($this->names) || strlen($this->name_kana) || strlen($this->schoolcd)){
            $db = Query::dbCheckOut();
            $query  = "";
            $query .= " SELECT  T1.FINSCHOOLCD, T1.FINSCHOOL_NAME, T1.FINSCHOOL_KANA ";
            $query .= "        ,N1.NAME1 AS CHIKU_NAME, N2.NAME1 AS RITSU_NAME ";
            $query .= "   FROM  FINSCHOOL_YDAT T2, ";
            $query .= "         FINSCHOOL_MST T1 ";
            $query .= "         LEFT JOIN NAME_MST N1 ON N1.NAMECD1='Z003' AND N1.NAMECD2=T1.DISTRICTCD ";
            $query .= "         LEFT JOIN NAME_MST N2 ON N2.NAMECD1='L001' AND N2.NAMECD2=T1.FINSCHOOL_DISTCD ";
            $query .= "  WHERE  T2.YEAR = '".$this->year."' ";
            $query .= "    AND  T2.FINSCHOOLCD = T1.FINSCHOOLCD ";
            if ($this->cmd == "apply"){
                $query .= " AND T2.FINSCHOOLCD = '" .$this->schoolcd ."' ";
            } else {
                if (strlen($this->names)){
                    $query .= " AND T1.FINSCHOOL_NAME like '%" .$this->names2 ."%' ";
                }
                if (strlen($this->name_kana)){
                    $query .= " AND T1.FINSCHOOL_KANA like '%" .$this->name_kana2 ."%' ";
                }
            }
            $query .= " ORDER BY T1.FINSCHOOLCD ";
            //受験番号、氏名、氏名かな取得
            $result = $db->query($query);
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $opt[] = array("label" => $row["FINSCHOOLCD"]."　".$row["FINSCHOOL_NAME"]."　(".$row["FINSCHOOL_KANA"].")",
                               "value" => $row["FINSCHOOLCD"]."　".$row["CHIKU_NAME"]."　".$row["RITSU_NAME"]
                               );
            }
            Query::dbCheckIn($db);    
        }
        $this->form->ae( array("type"       => "select",
                               "name"       => "NAMELIST",
                               "size"       => "20",
                               "value"      => "",
                               "extrahtml"   => " ondblclick=\"apply_name(this,'".$this->examno."','".$this->zip."','".$this->gzip."','".$this->zadd."','".$this->gadd."');\" STYLE=\"WIDTH:100%\"",
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

        $this->schoolcd     = $rq["schoolcd"];
        $this->names        = $rq["names"];
        $this->name_kana    = $rq["name_kana"];
        
        if ($rq["year"]){
            $this->year = $rq["year"];
        }
        if ($rq["examno"]){
            $this->examno = $rq["examno"];
            
            $query  = " SELECT zipcd, address1, gzipcd, gaddress1 ";
            $query .= "   FROM entexam_applicantaddr_dat ";
            $query .= "  WHERE entexamyear = '".$this->year."' AND ";
            $query .= "        examno = '".$this->examno."'";
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            
            $this->zip  = $row["ZIPCD"];
            $this->gzip = $row["GZIPCD"];
            $this->zadd = $row["ADDRESS1"];
            $this->gadd = $row["GADDRESS1"];
        }
        if ($rq["frame"]){
            $this->frame = $rq["frame"];      //フレーム名
        }
        $this->cmd = $rq["cmd"];              //コマンド
        
        Query::dbCheckIn($db);
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
 
 
     //フォームタブ(開始)
    function getStart(){
        return $this->form->get_start("name", "POST", "search_fin_name.php", "", "name");
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

if ($_GET["cmd"] == "apply"){
    $objName = new SearchNames();
    $objName->main($_REQUEST);
}else{
    if ($_REQUEST["NAME_SESSID"] == "") exit;
    $sess = new APP_Session($_REQUEST["NAME_SESSID"], 'NAME');
    if (!$sess->isCached($_REQUEST["NAME_SESSID"], 'NAME')) {
        $sess->data = new SearchNames();
    }
    $sess->data->main($_REQUEST);

    $objName = $sess->getData();
}

?>
<html>
<head>
<title>かな検索</title>
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

function apply_name(obj,examno,zip,gzip,zadd,gadd) {
    if(obj.selectedIndex >= 0){
        var val = obj.options[obj.selectedIndex].text;    
        var arr = val.split("　");
        f.document.forms[0]['FINSCHOOLCD'].value = arr[0];
        f.document.getElementById("label_name").innerHTML = arr[1];
        var val2 = obj.options[obj.selectedIndex].value;
        var arr2 = val2.split("　");
        f.document.getElementById("RITSU_NAME_ID").innerHTML = arr2[2];
        f.closeit();
//        f.document.forms[0]['EXAMNO'].value = arr[0];
//        if(f.btn_submit('reference',zip,gzip,zadd,gadd) != true) {
//            f.closeit();
//        } else {
//            f.document.forms[0]['EXAMNO'].value = examno;
//        }
    }else{
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
                <th nowrap width="40%" align="right">&nbsp;学校名&nbsp;</th>
                <td width="*" bgcolor="#ffffff"><?php echo $objName->getTextName() ?></td>
              </tr>
              <tr class="no_search">
                <th nowrap width="40%">&nbsp;学校名かな&nbsp;</th>
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
<?php 
if ($_GET["cmd"] == "apply"){
?>
<script language="JavaScript">
    if (document.forms[0].NAMELIST.length == 1){
        var val = document.forms[0].NAMELIST.options[0].text;
        var arr = val.split("　");
        f.document.getElementById("label_name").innerHTML = arr[1];
        var val2 = document.forms[0].NAMELIST.options[0].value;
        var arr2 = val2.split("　");
        f.document.getElementById("RITSU_NAME_ID").innerHTML = arr2[2];
    } else {
<?php 
if ($_GET["schoolcd"] != ""){
?>
        alert("名称が見つかりませんでした。");
<?php
}
?>
        f.document.getElementById("label_name").innerHTML = "";
        f.document.getElementById("RITSU_NAME_ID").innerHTML = "";
    }
</script>
<?php
}
?>
