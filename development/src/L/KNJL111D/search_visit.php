<?php

require_once('for_php7.php');

//氏名かな検索
class SearchVist {
    var $form;
    var $year;
    var $visitNo;
    var $names;
    var $name_kana;
    var $names2;
    var $name_kana2;
    var $examno;
    var $chkName;
    var $chkNameKana;
    var $chkBirthDay;
    var $nameList;
    var $zip;
    var $gzip;
    var $zadd;
    var $gadd;
    var $frame;         //フレーム名
    var $cmd;           //コマンド

    function SearchVist() {
        $this->form = new form();
    }

    //氏名リスト作成
    function getNameList() {
        $opt = array();
        if ($this->cmd == "searchOn") {
            $db = Query::dbCheckOut();
            $query  = "";
            $query .= " SELECT ";
            $query .= "     VSIT.VISIT_NO, ";
            $query .= "     VSIT.NAME, ";
            $query .= "     VSIT.NAME_KANA, ";
            $query .= "     VSIT.SEX, ";
            $query .= "     Z002.NAME1 AS SEX_NAME, ";
            $query .= "     VSIT.ERACD, ";
            $query .= "     VSIT.BIRTH_Y, ";
            $query .= "     VSIT.BIRTH_M, ";
            $query .= "     VSIT.BIRTH_D, ";
            $query .= "     value(LL007.ABBV1, '') || VSIT.BIRTH_Y || '/' || VSIT.BIRTH_M || '/' || VSIT.BIRTH_D AS BIRTHDAY, ";
            $query .= "     VSIT.FS_CD, ";
            $query .= "     FSCH.FINSCHOOL_NAME, ";
            $query .= "     value(L007.ABBV1, '') || VSIT.FS_Y || '/' || VSIT.FS_M AS GRD_YEAR_MONTH, ";
            $query .= "     VSIT.FS_ERACD, ";
            $query .= "     VSIT.FS_Y, ";
            $query .= "     VSIT.FS_M, ";
            $query .= "     '〒' || VSIT.ZIPCD || VSIT.ADDRESS1 || VSIT.ADDRESS2 AS ADDRESS_ALL, ";
            $query .= "     VSIT.ZIPCD, ";
            $query .= "     VSIT.ADDRESS1, ";
            $query .= "     VSIT.ADDRESS2, ";
            $query .= "     VSIT.TELNO ";
            $query .= " FROM ";
            $query .= "     ENTEXAM_VISIT_DAT VSIT ";
            $query .= "     LEFT JOIN FINSCHOOL_MST FSCH ON VSIT.FS_CD = FSCH.FINSCHOOLCD ";
            $query .= "     LEFT JOIN NAME_MST L007 ON L007.NAMECD1 = 'L007' ";
            $query .= "                            AND L007.NAMECD2 = VSIT.FS_ERACD ";
            $query .= "     LEFT JOIN NAME_MST LL007 ON LL007.NAMECD1 = 'L007' ";
            $query .= "                             AND LL007.NAMECD2 = VSIT.ERACD ";
            $query .= "     LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' ";
            $query .= "                            AND Z002.NAMECD2 = VSIT.SEX ";
            $query .= " WHERE ";
            $query .= "     ENTEXAMYEAR  = '".$this->year."' ";
            if (strlen($this->visitNo)) {
                $query .= " AND VISIT_NO like '%" .$this->visitNo ."%' ";
            }
            if (strlen($this->names)) {
                $query .= " AND NAME like '%" .$this->names2 ."%' ";
            }
            if (strlen($this->name_kana)) {
                 $query .= " AND NAME_KANA like '%" .$this->name_kana2 ."%' ";
            }
            $query .= " ORDER BY ";
            $query .= "     VISIT_NO ";
            //相談番号、氏名、氏名かな取得
            $result = $db->query($query);
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $opt[] = array("label" => sprintf("%'-1s | %'-1s | %'-1s | %'-1s | %'-1s | %'-1s | %'-1s | %'-1s | %'-1s | %'-1s ", 
                                                  $row["VISIT_NO"],
                                                  $row["NAME"],
                                                  $row["NAME_KANA"],
                                                  $row["SEX_NAME"],
                                                  $row["BIRTHDAY"],
                                                  $row["ADDRESS_ALL"],
                                                  $row["TELNO"],
                                                  $row["FS_CD"],
                                                  $row["FINSCHOOL_NAME"],
                                                  $row["GRD_YEAR_MONTH"]
                                                  ),
                               "value" => sprintf("%'+1s | %'+1s | %'+1s | %'+1s | %'+1s | %'+1s | %'+1s | %'+1s | %'+1s | %'+1s | %'+1s | %'+1s | %'+1s | %'+1s | %'+1s | %'+1s | %'+1s ", 
                                                  $row["VISIT_NO"],
                                                  $row["NAME"],
                                                  $row["NAME_KANA"],
                                                  $row["SEX"],
                                                  $row["ERACD"],
                                                  $row["BIRTH_Y"],
                                                  $row["BIRTH_M"],
                                                  $row["BIRTH_D"],
                                                  $row["FS_CD"],
                                                  $row["FINSCHOOL_NAME"],
                                                  $row["FS_ERACD"],
                                                  $row["FS_Y"],
                                                  $row["FS_M"],
                                                  $row["ZIPCD"],
                                                  $row["ADDRESS1"],
                                                  $row["ADDRESS2"],
                                                  $row["TELNO"]
                                                  )
                               );
            }
            Query::dbCheckIn($db);
        }
        $defVal = ($this->nameList) ? $this->nameList: "";
        $this->form->ae( array("type"       => "select",
                               "name"       => "NAMELIST",
                               "size"       => "11",
                               "value"      => $defVal,
                               "extrahtml"  => " onclick=\"btn_submit('searchOn');\" STYLE=\"WIDTH:500%\"",
                               "options"    => (is_array($opt))? $opt : array()));

        return $this->form->ge("NAMELIST");
    }

    //不合格者リスト作成
    function getUnPassList() {
        $opt = array();

        if ($this->cmd == "searchOn" && $this->chkName != "") {
            $db = Query::dbCheckOut();

            $query  = "";
            $query .= " SELECT ";
            $query .= "     BASE.ENTEXAMYEAR, ";
            $query .= "     BASE.NAME, ";
            $query .= "     BASE.NAME_KANA, ";
            $query .= "     Z002.NAME1 AS SEX_NAME, ";
            $query .= "     value(LL007.ABBV1, '') || BASE.BIRTH_Y || '/' || BASE.BIRTH_M || '/' || BASE.BIRTH_D AS BIRTHDAY, ";
            $query .= "     BASE.FS_CD, ";
            $query .= "     FSCH.FINSCHOOL_NAME, ";
            $query .= "     value(L007.ABBV1, '') || BASE.FS_Y || '/' || BASE.FS_M AS GRD_YEAR_MONTH, ";
            $query .= "     '〒' || ADDR.ZIPCD || ADDR.ADDRESS1 || ADDR.ADDRESS2 AS ADDRESS_ALL, ";
            $query .= "     ADDR.TELNO ";
            $query .= " FROM ";
            $query .= "     ENTEXAM_APPLICANTBASE_DAT BASE ";
            $query .= "     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT ADDR ON ADDR.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
            $query .= "                                             AND ADDR.APPLICANTDIV = BASE.APPLICANTDIV ";
            $query .= "                                             AND ADDR.EXAMNO       = BASE.EXAMNO ";
            $query .= "     LEFT JOIN FINSCHOOL_MST FSCH ON BASE.FS_CD = FSCH.FINSCHOOLCD ";
            $query .= "     LEFT JOIN NAME_MST L007 ON L007.NAMECD1 = 'L007' ";
            $query .= "                            AND L007.NAMECD2 = BASE.FS_ERACD ";
            $query .= "     LEFT JOIN NAME_MST LL007 ON LL007.NAMECD1 = 'L007' ";
            $query .= "                             AND LL007.NAMECD2 = BASE.ERACD ";
            $query .= "     LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' ";
            $query .= "                            AND Z002.NAMECD2 = BASE.SEX ";
            $query .= " WHERE ";
            $query .= "         BASE.APPLICANTDIV = '1' ";
            $query .= "     AND BASE.JUDGEMENT    = '4' "; // 不合格者
            $query .= "     AND BASE.NAME         = '".$this->chkName."' ";
            $query .= "     AND BASE.NAME_KANA    = '".$this->chkNameKana."' ";
            $query .= "     AND BASE.ERACD || BASE.BIRTH_Y || BASE.BIRTH_M || BASE.BIRTH_D = '".$this->chkBirthDay."' ";
            $query .= " ORDER BY ";
            $query .= "     ENTEXAMYEAR ";
            //相談番号、氏名、氏名かな取得
            $result = $db->query($query);
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $opt[] = array("label" => sprintf("%'-1s | %'-1s | %'-1s | %'-1s | %'-1s | %'-1s | %'-1s | %'-1s | %'-1s | %'-1s ", 
                                                  $row["ENTEXAMYEAR"],
                                                  $row["NAME"],
                                                  $row["NAME_KANA"],
                                                  $row["SEX_NAME"],
                                                  $row["BIRTHDAY"],
                                                  $row["ADDRESS_ALL"],
                                                  $row["TELNO"],
                                                  $row["FS_CD"],
                                                  $row["FINSCHOOL_NAME"],
                                                  $row["GRD_YEAR_MONTH"]
                                                  ),
                               "value" => ""
                               );
            }
            Query::dbCheckIn($db);
        }

        $this->form->ae( array("type"       => "select",
                               "name"       => "UNPASSLIST",
                               "size"       => "11",
                               "value"      => "",
                               "extrahtml"  => "readonly STYLE=\"WIDTH:500%\"",
                               "options"    => (is_array($opt))? $opt : array()));

        return $this->form->ge("UNPASSLIST");
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

        $this->visitNo      = $rq["visitNo"];
        $this->names        = $rq["names"];
        $this->name_kana    = $rq["name_kana"];
        $this->nameList     = $rq["NAMELIST"];

        if ($rq["year"]){
            $this->year = $rq["year"];
        }

        if ($this->cmd == "search") {
            $this->chkName     = "";
            $this->chkNameKana = "";
            $this->chkBirthDay = "";
        }

        if ($this->cmd == "searchOn") {
            if ($rq["NAMELIST"]){
                $arrData = explode(' | ', $rq["NAMELIST"]);
                $this->chkName     = $arrData[1];
                $this->chkNameKana = $arrData[2];
                $this->chkBirthDay = $arrData[4].$arrData[5].$arrData[6].$arrData[7];
            }
        }

        if ($rq["visitNo"]){
            $this->visitNo = $rq["visitNo"];
        }
        if ($rq["frame"]){
            $this->frame = $rq["frame"];      //フレーム名
        }
        $this->cmd = $rq["cmd"];              //コマンド

        Query::dbCheckIn($db);
    }

    //相談番号テキストボックス
    function getTextVisitNo(){
        //氏名
        $this->form->ae( array("type"        => "text",
                               "name"        => "visitNo",
                               "size"        => 3,
                               "maxlength"   => 3,
                               "extrahtml"   => "",
                               "value"       => $this->visitNo ));

        return $this->form->ge("visitNo");
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
                               "extrahtml"   => "onclick=\"return btn_submit('searchOn')\"" ) );
                               
        //終了ボタン
        $this->form->ae( array("type"      => "button",
                               "name"      => "close",
                               "value"     => "戻 る",
                               "extrahtml" => "onclick=\"f.closeit();\"" ) );

        return $this->form->ge("search") . $this->form->ge("close");
    }

    //反映ボタン作成
    function getBtnReflect(){
        //検索ボタンを作成する
        $this->form->ae( array("type"        => "button",
                               "name"        => "reflect",
                               "value"       => "反 映",
                               "extrahtml"   => "disabled onclick=\"setText()\"" ) );
                               
        //終了ボタン
        $this->form->ae( array("type"      => "button",
                               "name"      => "close2",
                               "value"     => "戻 る",
                               "extrahtml" => "onclick=\"f.closeit();\"" ) );

        return $this->form->ge("reflect") . $this->form->ge("close2");
    }
 
    //不合格履歴無しチェックボックス
    function getCheckNoRireki(){
        //氏名
        $this->form->ae( array("type"        => "checkbox",
                               "name"        => "NoRireki",
                               "extrahtml"   => "id=\"NoRireki\" onclick=\"disFalse(this)\"",
                               "value"       => 1 ));

        return $this->form->ge("NoRireki");
    }

     //フォームタブ(開始)
    function getStart(){
        return $this->form->get_start("visit", "POST", "search_visit.php", "", "visit");
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
if ($_REQUEST["VISIT_SESSID"] == "") exit;
$sess = new APP_Session($_REQUEST["VISIT_SESSID"], 'VISIT');
if (!$sess->isCached($_REQUEST["VISIT_SESSID"], 'VISIT')) {
    $sess->data = new SearchVist();
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

function setText() {
    try{
        var pt = parent.document.forms[0];
        var val = document.forms[0].NAMELIST.options[document.forms[0].NAMELIST.selectedIndex].value;
        if (val == "") {
            alert('生徒が選択されていません');
            return false;
        }
        var arr = val.split(" | ");

        for (var i = 0; i <= arr.length; i++) {
            if (arr[i] == "+") {
                arr[i] = "";
            }
        }

        if (pt.VISIT_NO)    pt.VISIT_NO.value    = arr[0];
        if (pt.NAME)        pt.NAME.value        = arr[1];
        if (pt.NAME_KANA)   pt.NAME_KANA.value   = arr[2];
        if (pt.SEX)         pt.SEX.value         = arr[3];
        if (pt.ERACD)       pt.ERACD.value       = arr[4];
        if (pt.BIRTH_Y)     pt.BIRTH_Y.value     = arr[5];
        if (pt.BIRTH_M)     pt.BIRTH_M.value     = arr[6];
        if (pt.BIRTH_D)     pt.BIRTH_D.value     = arr[7];
        if (pt.FINSCHOOLCD) pt.FINSCHOOLCD.value = arr[8];
        parent.document.getElementById('FINSCHOOLNAME_ID').innerHTML = arr[9];
        if (pt.FS_ERACD)    pt.FS_ERACD.value    = arr[10];
        if (pt.FS_Y)        pt.FS_Y.value        = arr[11];
        if (pt.FS_M)        pt.FS_M.value        = arr[12];
        if (pt.ZIPCD)       pt.ZIPCD.value       = arr[13];
        if (pt.ADDRESS1)    pt.ADDRESS1.value    = arr[14];
        if (pt.ADDRESS2)    pt.ADDRESS2.value    = arr[15];
        if (pt.TELNO)       pt.TELNO.value       = arr[16];

        pt.btn_ok.disabled      = false;
        pt.btn_pdf.disabled     = false;
        pt.btn_add.disabled     = false;
        pt.btn_udpate.disabled  = false;
        pt.btn_up_pre.disabled  = false;
        pt.btn_up_next.disabled = false;
        pt.btn_del.disabled     = false;

        f.closeit();
    } catch(e) {
    }
}

function apply_name(obj,examno,zip,gzip,zadd,gadd) {
    if(obj.selectedIndex >= 0){
        var val = obj.options[obj.selectedIndex].text;    
        var arr = val.split("　");
        f.document.forms[0]['EXAMNO'].value = arr[0];
        if(f.btn_submit('reference',zip,gzip,zadd,gadd) != true) {
            f.closeit();
        } else {
            f.document.forms[0]['EXAMNO'].value = examno;
        }
    }else{
        alert("名前が選択されていません");
    }
 
}
//チェックボックス
function disFalse(obj) {
    if (obj.checked == true) {
        document.forms[0].reflect.disabled = false;
    } else {
        document.forms[0].reflect.disabled = true;
    }
    return false;
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
<table width="1000" border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td >
      <table width="100%" border="0" cellspacing="0" cellpadding="0">
        <tr class="no_search_line">
          <td>
            <table width="100%" border="0" cellspacing="1" cellpadding="2">
              <tr class="no_search">
                <th nowrap width="200" align="right">&nbsp;相談番号&nbsp;</th>
                <td width="*" bgcolor="#ffffff"><?php echo $objName->getTextVisitNo() ?></td>
              </tr>
              <tr class="no_search">
                <th nowrap width="200" align="right">&nbsp;氏名&nbsp;</th>
                <td width="*" bgcolor="#ffffff"><?php echo $objName->getTextName() ?></td>
              </tr>
              <tr class="no_search">
                <th nowrap width="200" align="right">&nbsp;氏名かな&nbsp;</th>
                <td bgcolor="#ffffff"><?php echo $objName->getTextName_Kana() ?></td>
              </tr>
              <tr class="no_search">
                <td colspan="2"  bgcolor="#ffffff"><?php echo $objName->getBtnSearch() ?></td>
              </tr>
              <tr class="no_search">
                <td colspan="2" bgcolor="#ffffff"><?php echo $objName->getNameList() ?></td>
              </tr>
              <tr class="no_search">
                <td colspan="2" bgcolor="#ffffff" style="color:black;"><?php echo $objName->getCheckNoRireki() ?><LABEL for="NoRireki">不合格履歴なし</LABEL></td>
              </tr>
              <tr class="no_search">
                <td colspan="2" bgcolor="#ffffff"><?php echo $objName->getUnPassList() ?></td>
              </tr>
              <tr class="no_search">
                <td colspan="2"  bgcolor="#ffffff"><?php echo $objName->getBtnReflect() ?></td>
              </tr>
            </table>
          </td>
        </tr>
      </table>
    </td>
  </tr>
</table>
<input type="hidden" name="VISIT_SESSID" value="<?php echo $_REQUEST["VISIT_SESSID"] ?>">
<input type="hidden" name="cmd">
<?php echo $objName->getFinish() ?>
</body>
</html>
