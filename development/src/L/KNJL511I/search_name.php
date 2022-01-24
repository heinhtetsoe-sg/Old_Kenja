<?php
//氏名カナ検索
class searchNames
{
    public $form;
    public $year;
    public $applicantdiv;
    public $testdiv0;
    public $testdiv;
    public $names;
    public $name_kana;
    public $names2;
    public $name_kana2;
    public $examno;
    public $examno2;
    public $zip;
    public $gzip;
    public $zadd;
    public $gadd;
    public $frame;         //フレーム名
    public $cmd;           //コマンド

    public function searchNames()
    {
        $this->form = new form();
    }

    //氏名リスト作成
    public function getNameList()
    {
        $opt = array();
        if (strlen($this->examno) || strlen($this->names) || strlen($this->name_kana)) {
            $db = Query::dbCheckOut();
            $query  = "";
            $query .= "SELECT ";
            $query .= "    EXAMNO, ";
            $query .= "    NAME, ";
            $query .= "    NAME_KANA ";
            $query .= "FROM ";
            $query .= "    ENTEXAM_APPLICANTBASE_DAT ";
            $query .= "WHERE ENTEXAMYEAR = '".$this->year."'";
            $query .= "  AND APPLICANTDIV = '".$this->applicantdiv."'";
            $query .= "  AND TESTDIV0 = '".$this->testdiv0."'";
            $query .= "  AND TESTDIV = '".$this->testdiv."'";
            if (strlen($this->examno)) {
                $query .= " AND EXAMNO like '%" .$this->examno2 ."%' ";
            }
            if (strlen($this->names)) {
                $query .= " AND NAME like '%" .$this->names2 ."%' ";
            }
            if (strlen($this->name_kana)) {
                $query .= " AND NAME_KANA like '%" .$this->name_kana2 ."%' ";
            }
            $query .= "ORDER BY EXAMNO ";

            //受験番号、氏名、氏名カナ取得
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt[] = array("label" => $row["EXAMNO"]."　".$row["NAME"]."　(".$row["NAME_KANA"].")",
                               "value" => $row["EXAMNO"]
                               );
            }
            Query::dbCheckIn($db);
        }

        $this->form->ae(array("type"       => "select",
                               "name"       => "NAMELIST",
                               "size"       => "11",
                               "value"      => "",
                               "extrahtml"   => " ondblclick=\"apply_name(this,'".$this->examno."','".$this->zip."','".$this->zadd."');\" STYLE=\"WIDTH:100%\"",
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
            for ($i=0; $i<count($plode); $i++) {
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
            for ($i=0; $i<count($plode); $i++) {
                if ($i != 0) {
                    $imp = $imp . "''";
                }
                $imp = $imp . $plode[$i];
            }
            $this->name_kana2 = $imp;
        }

        $exist = strstr($rq["examno"], "'");
        if ($exist == false) {
            $this->examno2 = $rq["examno"];
        } else {
            $plode = explode("'", $rq["examno"]);
            for ($i=0; $i<count($plode); $i++) {
                if ($i != 0) {
                    $imp = $imp . "''";
                }
                $imp = $imp . $plode[$i];
            }
            $this->examno2 = $imp;
        }

        $this->names        = $rq["names"];
        $this->name_kana    = $rq["name_kana"];
        $this->examno       = $rq["examno"];

        if ($rq["year"]) {
            $this->year = $rq["year"];
        }
        if ($rq["applicantdiv"]) {
            $this->applicantdiv = $rq["applicantdiv"];
        }
        if ($rq["testdiv0"]) {
            $this->testdiv0 = $rq["testdiv0"];
        }
        if ($rq["testdiv"]) {
            $this->testdiv = $rq["testdiv"];
        }
        if ($rq["examno"]) {
            $this->examno = $rq["examno"];

            $query  = " SELECT zipcd, address1, gzipcd, gaddress1 ";
            $query .= "   FROM entexam_applicantaddr_dat ";
            $query .= "  WHERE entexamyear = '".$this->year."' AND ";
            $query .= "        applicantdiv = '".$this->applicantdiv."' AND ";
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

    //学科
    public function getTestdiv0()
    {
        $name = "testdiv0";
        $size = 1;
        $value = $this->testdiv0;
        $extra = "";
        $opt = array();
        $opt[] = array("label" => "普通科", "value" => "1");
        $opt[] = array("label" => "工業科", "value" => "2");

        $this->form->ae(array("type"       => "select",
                               "name"       => $name,
                               "size"       => $size,
                               "value"      => $value,
                               "extrahtml"  => $extra,
                               "options"    => $opt));
        return $this->form->ge($name);
    }

    //入試区分
    public function getTestdiv()
    {
        $db = Query::dbCheckOut();
        $query  = " SELECT ";
        $query .= "     TESTDIV AS VALUE, ";
        $query .= "     TESTDIV || ':' || VALUE(TESTDIV_NAME, '') AS LABEL, ";
        $query .= "     TESTDIV_NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '".$this->year."' ";
        $query .= " AND APPLICANTDIV = '".$this->applicantdiv."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        //入試区分コンボ
        $extra = "";
        $retVal = $this->makeCmb($db, $query, $this->testdiv, "testdiv", $extra, 1);
        Query::dbCheckIn($db);
        return $retVal;
    }

    //受験番号
    public function getExamno()
    {
        //氏名
        $this->form->ae(array("type"        => "text",
                               "name"        => "examno",
                               "size"        => 31,
                               "maxlength"   => 16,
                               "extrahtml"   => "",
                               "value"       => $this->examno ));

        return $this->form->ge("examno");
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

    //氏名カナテキストボックス
    public function getTextNameKana()
    {
        //氏名カナ
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
                               "extrahtml"   => "onclick=\"return btn_submit('search')\"" ));

        //終了ボタン
        $this->form->ae(array("type"      => "button",
                               "name"      => "close",
                               "value"     => "戻 る",
                               "extrahtml" => "onclick=\"f.closeit();\"" ));

        return $this->form->ge("search") . $this->form->ge("close");
    }

    //makeCmb
    public function makeCmb($db, $query, &$value, $name, $extra, $size, $blank = "")
    {
        $opt = array();
        if ($blank == "BLANK") {
            $opt[] = array("label" => "", "value" => "");
        }
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($value == $row["VALUE"]) {
                $value_flg = true;
            }
        }
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
        $this->form->ae(array("type"       => "select",
                               "name"       => $name,
                               "size"       => $size,
                               "value"      => $value,
                               "extrahtml"  => $extra,
                               "options"    => $opt));
        return $this->form->ge($name);
    }

    //フォームタブ(開始)
    public function getStart()
    {
        return $this->form->get_start("name", "POST", "search_name.php", "", "name");
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
    $sess->data = new searchNames();
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

function apply_name(obj,examno,zip,zadd) {
    if(obj.selectedIndex >= 0){
        var val = obj.options[obj.selectedIndex].text;    
        var arr = val.split("　");
        f.document.forms[0]['EXAMNO'].value = arr[0];
        if(f.btn_submit('reference',zip,zadd) != true) {
            f.closeit();
        } else {
            f.document.forms[0]['EXAMNO'].value = examno;
        }
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
                <th nowrap width="40%" align="right">&nbsp;※ 学科&nbsp;</th>
                <td width="*" bgcolor="#ffffff"><?php echo $objName->getTestdiv0() ?></td>
              </tr>
              <tr class="no_search">
                <th nowrap width="40%" align="right">&nbsp;※ 入試区分&nbsp;</th>
                <td width="*" bgcolor="#ffffff"><?php echo $objName->getTestdiv() ?></td>
              </tr>
              <tr class="no_search">
                <th nowrap width="40%" align="right">&nbsp;受験番号&nbsp;</th>
                <td width="*" bgcolor="#ffffff"><?php echo $objName->getExamno() ?></td>
              </tr>
              <tr class="no_search">
                <th nowrap width="40%" align="right">&nbsp;氏名&nbsp;</th>
                <td width="*" bgcolor="#ffffff"><?php echo $objName->getTextName() ?></td>
              </tr>
              <tr class="no_search">
                <th nowrap width="40%">&nbsp;氏名カナ&nbsp;</th>
                <td bgcolor="#ffffff"><?php echo $objName->getTextNameKana() ?></td>
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
