<?php

require_once('for_php7.php');

//氏名かな検索（塾）
class SearchNames
{
    public $form;
    public $year;
    public $ctrl_year;
    public $ctrl_semester;
    public $gtelno;
    public $names;
    public $name_kana;
    public $names2;
    public $name_kana2;
    public $examno;
    public $zip;
    public $gzip;
    public $zadd;
    public $gadd;
    public $frame;         //フレーム名
    public $cmd;           //コマンド
    
    public function SearchNames()
    {
        $this->form = new form();
    }
    
    //氏名リスト作成
    public function getNameList()
    {
        $opt = array();
        if ($this->cmd == "search2" && (strlen($this->gtelno) || strlen($this->names) || strlen($this->name_kana))) {
            $db = Query::dbCheckOut();
            $query  = "";
            $query .= " SELECT ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     T1.GUARD_NAME, ";
            $query .= "     T1.GUARD_KANA, ";
            $query .= "     T1.GUARD_TELNO, ";
            $query .= "     T3.HR_NAMEABBV, ";
            $query .= "     T4.NAME_SHOW ";
            $query .= " FROM ";
            $query .= "     GUARDIAN_DAT T1 ";
            $query .= "     INNER JOIN SCHREG_BASE_MST T4 ";
            $query .= "             ON T4.SCHREGNO = T1.SCHREGNO, ";
            $query .= "     SCHREG_REGD_DAT T2 ";
            $query .= "     INNER JOIN SCHREG_REGD_HDAT T3 ";
            $query .= "             ON T3.YEAR = T2.YEAR ";
            $query .= "            AND T3.SEMESTER = T2.SEMESTER ";
            $query .= "            AND T3.GRADE = T2.GRADE ";
            $query .= "            AND T3.HR_CLASS = T2.HR_CLASS ";
            $query .= " WHERE ";
            $query .= "         T2.YEAR = '" .$this->ctrl_year ."' ";
            $query .= "     AND T2.SEMESTER = '" .$this->ctrl_semester ."' ";
            $query .= "     AND T2.SCHREGNO = T1.SCHREGNO ";
            if (strlen($this->gtelno)) {
                $query .= " AND T1.GUARD_TELNO like '%" .$this->gtelno ."%' ";
            }
            if (strlen($this->names)) {
                $query .= " AND T1.GUARD_NAME like '%" .$this->names2 ."%' ";
            }
            if (strlen($this->name_kana)) {
                $query .= " AND T1.GUARD_KANA like '%" .$this->name_kana2 ."%' ";
            }
            $query .= " ORDER BY ";
            $query .= "     T2.GRADE, ";
            $query .= "     T2.HR_CLASS ";
            //受験番号、氏名、氏名かな取得
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt[] = array("label" => $row["SCHREGNO"]."　|　".$row["HR_NAMEABBV"]."　|　".$row["NAME_SHOW"],
                               "value" => $row["SCHREGNO"]
                               );
            }
            Query::dbCheckIn($db);
        }
        $this->form->ae(array("type"       => "select",
                               "name"       => "NAMELIST",
                               "size"       => "13",
                               "value"      => "",
                               "extrahtml"   => "multiple ondblclick=\"apply_name(this,'".$this->examno."','".$this->zip."','".$this->gzip."','".$this->zadd."','".$this->gadd."');\" STYLE=\"WIDTH:100%\"",
                               "options"    => (is_array($opt))? $opt : array()));

        return $this->form->ge("NAMELIST");
    }
    public function main($rq)
    {
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

        $imp = "";
        $exist = strstr($rq["gtelno"], "'");
        if ($exist == false) {
            $this->gtelno = $rq["gtelno"];
        } else {
            $plode = explode("'", $rq["gtelno"]);
            for ($i=0; $i<get_count($plode); $i++) {
                if ($i != 0) {
                    $imp = $imp . "''";
                }
                $imp = $imp . $plode[$i];
            }
            $this->gtelno = $imp;
        }

        $this->names        = $rq["names"];
        $this->name_kana    = $rq["name_kana"];
        
        if ($rq["year"]) {
            $this->year = $rq["year"];
            $this->ctrl_year = $rq["ctrl_year"];
            $this->ctrl_semester = $rq["ctrl_semester"];
        }
        if ($rq["examno"]) {
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
        if ($rq["frame"]) {
            $this->frame = $rq["frame"];      //フレーム名
        }
        $this->cmd = $rq["cmd"];              //コマンド
        
        Query::dbCheckIn($db);
    }

    //電話番号テキストボックス
    public function getTextGTelno()
    {
        //電話番号
        $this->form->ae(array("type"        => "text",
                               "name"        => "gtelno",
                               "size"        => 14,
                               "maxlength"   => 14,
                               "extrahtml"   => "",
                               "value"       => $this->gtelno ));

        return $this->form->ge("gtelno");
    }
    //氏名テキストボックス
    public function getTextName()
    {
        //氏名
        $this->form->ae(array("type"        => "text",
                               "name"        => "names",
                               "size"        => 31,
                               "maxlength"   => 16,
                               "extrahtml"   => "",
                               "value"       => $this->names ));

        return $this->form->ge("names");
    }
    //氏名かなテキストボックス
    public function getTextNameKana()
    {
        //氏名かな
        $this->form->ae(array("type"        => "text",
                               "name"        => "name_kana",
                               "size"        => 31,
                               "maxlength"   => 40,
                               "extrahtml"   => "",
                               "value"       => $this->name_kana ));

        return $this->form->ge("name_kana");
    }

    //検索ボタン作成
    public function getBtnSearch()
    {
        //検索ボタンを作成する
        $this->form->ae(array("type"        => "button",
                               "name"        => "search",
                               "value"       => "検 索",
                               "extrahtml"   => "onclick=\"return btn_submit('search2')\"" ));
                               
        //終了ボタン
        $this->form->ae(array("type"      => "button",
                               "name"      => "close",
                               "value"     => "戻 る",
                               "extrahtml" => "onclick=\"f.closeit();\"" ));

        //コピーボタンを作成する
        $this->form->ae(array("type"        => "button",
                               "name"        => "copy",
                               "value"       => "コピー",
                               "extrahtml"   => "onclick=\"apply_name_copy()\"" ));

        return $this->form->ge("search") . $this->form->ge("close") . "　　" . $this->form->ge("copy");
    }
 
 
    //フォームタブ(開始)
    public function getStart()
    {
        return $this->form->get_start("name", "POST", "search_guard_name.php", "", "name");
    }
    //フォームタブ(終了)
    public function getFinish()
    {
        return $this->form->get_finish();
    }
    //フレーム取得
    public function getFrame()
    {
        return $this->frame;
    }
}

if ($_REQUEST["NAME_SESSID"] == "") {
    exit;
}
$sess = new APP_Session($_REQUEST["NAME_SESSID"], 'NAME');
if (!$sess->isCached($_REQUEST["NAME_SESSID"], 'NAME')) {
    $sess->data = new SearchNames();
}
$sess->data->main($_REQUEST);

$objName = $sess->getData();

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
        var arr = val.split("　|　");
        f.document.forms[0]['REMARK1'].value = arr[1] + "：" + arr[2];
        f.closeit();
    }else{
        alert("名前が選択されていません");
    }
 
}
function apply_name_copy() {
    var namelist = document.forms[0].NAMELIST;
    var copylist = "";
    var seq = "";
    for (var i = 0; i < namelist.length; i++) {
        if (namelist.options[i].selected) {
            var val = namelist.options[i].text;
            var arr = val.split("　|　");
            var templist = arr[1] + "：" + arr[2];
            copylist = copylist + seq + templist;
            seq = "、";
        }
    }
    if (copylist != "") {
        f.document.forms[0]['REMARK1'].value = copylist;
        f.closeit();
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
                <th rowspan="3" nowrap width="5%">保<BR>護<BR>者</th>
                <th nowrap width="20%" align="right">&nbsp;電話番号&nbsp;</th>
                <td width="*" bgcolor="#ffffff"><?php echo $objName->getTextGTelno() ?></td>
              </tr>
              <tr class="no_search">
                <th nowrap align="right">&nbsp;氏名&nbsp;</th>
                <td bgcolor="#ffffff"><?php echo $objName->getTextName() ?></td>
              </tr>
              <tr class="no_search">
                <th nowrap align="right">&nbsp;氏名かな&nbsp;</th>
                <td bgcolor="#ffffff"><?php echo $objName->getTextNameKana() ?></td>
              </tr>
              <tr class="no_search">
                <td colspan="3"  bgcolor="#ffffff"><?php echo $objName->getBtnSearch() ?></td>
              </tr>
              <tr class="no_search">
                <td colspan="3" bgcolor="#ffffff"><?php echo $objName->getNameList() ?></td>
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
