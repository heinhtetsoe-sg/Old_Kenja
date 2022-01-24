<?php

require_once('for_php7.php');

//実戦模試受験者検索
class stucd {
    var $form;
    var $year;
    var $semester;
    var $simaiSchoolKind;
    var $simaiName;
    var $addr1;
    var $addr2;
    var $schregno;
    var $grade;
    var $name;
    var $name_kana;
    var $frame;         //フレーム名
    var $cmd;           //コマンド

    function stucd() {
        $this->form = new form();
    }
    function main($rq) {
        if ($rq["year"]) {
            $this->year = $rq["year"];
        }

        $this->name_kana    = $rq["NAME_KANA"]; //検索画面の氏名かな

        if ($rq["frame"]) {
            $this->frame = $rq["frame"];    //フレーム名
        }
        $this->cmd = $rq["cmd"];   //コマンド
    }
    //氏名かな
    function getText_Kana() {
        $this->form->ae( array("type"       => "text",
                               "name"       => "NAME_KANA",
                               "size"       => 40,
                               "maxlength"  => 40,
                               "extrahtml"  => " STYLE=\"ime-mode: active\" ",
                               "value"      => $this->name_kana));
        return $this->form->ge("NAME_KANA");
    }
    //検索ボタン作成
    function getBtn_Search() {
        $this->form->ae( array("type"       => "button",
                               "name"       => "search",
                               "value"      => "検 索",
                               "extrahtml"  => "onclick=\"return btn_submit('btn_search')\""));
        return $this->form->ge("search");
    }
    //反映ボタン作成
    function getBtn_Apply() {
        $this->form->ae( array("type"       => "button",
                               "name"       => "apply",
                               "value"      => "反 映",
                               "extrahtml"  => "onclick=\"apply_stucd(document.forms[0].STUCDLIST)\"" ));
        return $this->form->ge("apply");
    }
    //取消ボタン作成
    function getBtn_Cancel() {
        $this->form->ae( array("type"       => "button",
                               "name"       => "btn_cancel",
                               "value"      => "取 消",
                               "extrahtml"  => "onclick=\"return btn_submit('cancel')\""));

       return $this->form->ge("btn_cancel");
    }
    //戻るボタン作成
    function getBtn_Back() {
        $this->form->ae( array("type"       => "button",
                               "name"       => "btn_back",
                               "value"      => "戻 る",
                               "extrahtml"  => "onclick=\"Btn_Back();\""));

       return $this->form->ge("btn_back");
    }
    //生徒リスト作成
    function getStucdList() {
        global $db;
        $opt = array();

        if($this->cmd == "btn_search") {
            if ($this->name_kana) {
                $query  = "";
                $query .= " SELECT ";
                $query .= "     S1.SAT_NO, ";                   //表示No.1
                $query .= "     TRANSLATE_HK_H(S1.KANA1) || '　' || TRANSLATE_HK_H(S1.KANA2) AS NAME_KANA, ";   //3
                $query .= "     S1.SEX, ";                      //4
                $query .= "     N1.ABBV1 AS SEX_NAME, ";        //4
                $query .= "     S1.NAME1 AS NAME, ";            //2
                $query .= "     S1.BIRTHDAY, ";                 //5
                $query .= "     S1.ZIPCODE AS ZIPCD, ";
                $query .= "     S1.ADDR1 AS ADDRESS1, ";
                $query .= "     S1.ADDR2 AS ADDRESS2, ";
                $query .= "     S1.TELNO1 AS TELNO, ";          //7
                $query .= "     S1.ZIPCODE AS GZIPCD, ";
                $query .= "     S1.ADDR1 AS GADDRESS1, ";
                $query .= "     S1.ADDR2 AS GADDRESS2, ";
                $query .= "     S1.TELNO1 AS GTELNO, ";
                $query .= "     S1.SCHOOLCD AS FS_CD, ";        //6
                $query .= "     F1.FINSCHOOL_NAME AS FS_NAME "; //6
                $query .= " FROM ";
                $query .= "     SAT_APP_FORM_MST S1 ";
                $query .= "     LEFT JOIN FINSCHOOL_MST F1 ON F1.FINSCHOOLCD = S1.SCHOOLCD ";
                $query .= "     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'Z002' AND N1.NAMECD2 = S1.SEX ";
                $query .= " WHERE ";
                $query .= "         S1.YEAR   = '".$this->year."' ";
                if (strlen($this->name_kana)) {
                    $query .= "     AND TRANSLATE_HK_H(S1.KANA1) || '　' || TRANSLATE_HK_H(S1.KANA2) LIKE '%".$this->name_kana."%' ";
                }
                $query .= " ORDER BY ";
                $query .= "     S1.SAT_NO ";

                //取得
                $result = $db->query($query);

                while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $row["ZIPCD"]  = strlen($row["ZIPCD"]) ? substr($row["ZIPCD"], 0, 3)."-".substr($row["ZIPCD"], 3, 4) : "";
                    $row["GZIPCD"] = strlen($row["GZIPCD"]) ? $row["ZIPCD"] : "";

                    foreach ($row as $key => $val) {
                        $row[$key] = strlen($val) ? $val : "　";
                    }

                    $opt[] = array("label" => sprintf("%s | %s | %s | %s | %s | %s | %s", 
                                                      $row["SAT_NO"],
                                                      $row["NAME"],
                                                      $row["NAME_KANA"],
                                                      $row["SEX_NAME"],
                                                      str_replace("-", "/", $row["BIRTHDAY"]),
                                                      $row["FS_NAME"],
                                                      $row["TELNO"]),
                                    "value" => sprintf("%s | %s | %s | %s | %s | %s | %s | %s | %s | %s | %s | %s | %s | %s | %s | %s", 
                                                      $row["SAT_NO"],
                                                      $row["NAME"],
                                                      $row["NAME_KANA"],
                                                      $row["SEX_NAME"],
                                                      str_replace("-", "/", $row["BIRTHDAY"]),
                                                      $row["FS_NAME"],
                                                      $row["TELNO"],
                                                      $row["SEX"],
                                                      $row["ZIPCD"],
                                                      $row["ADDRESS1"],
                                                      $row["ADDRESS2"],
                                                      $row["GZIPCD"],
                                                      $row["GADDRESS1"],
                                                      $row["GADDRESS2"],
                                                      $row["GTELNO"],
                                                      $row["FS_CD"])
                                    );
                }
            }
        }
        $this->form->ae( array("type"  => "select",
                           "name"      => "STUCDLIST",
                           "size"      => "10",
                           "value"     => "",
                           "extrahtml" => "onclick=\"setText(this);\" ondblclick=\"apply_stucd(document.forms[0].STUCDLIST)\" STYLE=\"WIDTH:100%\" WIDTH=\"100%\"",
                           "options"   => (is_array($opt))? $opt : array()));

        return $this->form->ge("STUCDLIST");
    }
    //フォームタブ(開始)
    function getStart() {
        return $this->form->get_start("stucd","POST","knjl011qSatNameSearch.php","","stucd");
    }
    //フォームタブ(終了)
    function getFinish() {
        return $this->form->get_finish();
    }
    //フレーム取得
    function getFrame() {
        return $this->frame;
    }
}
if ($_REQUEST["STUCD_SESSID"] == "") exit;
$sess = new APP_Session($_REQUEST["STUCD_SESSID"], 'stucd');
if (!$sess->isCached($_REQUEST["STUCD_SESSID"], 'stucd')) {
    $sess->data = new stucd();
}
$sess->data->main($_REQUEST);

$objStucd = $sess->getData();
$db = Query::dbCheckOut();
?>
<html>
<head>
<title>実戦模試受験者検索</title>
<meta http-equiv="Content-Type" content="text/html; charset=<?php echo CHARSET ?>">
<link rel="stylesheet" href="<?php echo REQUESTROOT ?>/common/gk.css">
<script language="JavaScript" src="<?php echo REQUESTROOT ?>/common/common.js"></script>
<script language="JavaScript">
<!--
var  f = <?php echo $objStucd->getFrame() ?>;

function Btn_Back() {
    f.closeit();
}

function setText(obj) {
    try{
        var val = obj.options[obj.selectedIndex].value;
        var arr = val.split(" | ");
        for (var i = 0; i <= arr.length; i++) {
            if (arr[i] == "　") {
                arr[i] = "";
            }
        }
        document.forms[0].NAME_KANA.value   = arr[2];
    }catch(e) {
    }
}

function apply_stucd(obj) {
    if(obj.selectedIndex >= 0){
        var val = obj.options[obj.selectedIndex].value;
        var arr = val.split(" | ");
        for (var i = 0; i <= arr.length; i++) {
            if (arr[i] == "　") {
                arr[i] = "";
            }
        }

        f.document.forms[0].JIZEN_BANGOU.value = arr[0];
        f.document.forms[0].NAME.value = arr[1];
        f.document.forms[0].NAME_KANA.value = arr[2];
        f.document.forms[0].BIRTHDAY.value = arr[4];
        f.document.forms[0].TELNO.value = arr[6];

        f.document.forms[0].SEX.value = arr[7];
        f.document.forms[0].ZIPCD.value = arr[8];
        f.document.forms[0].ADDRESS1.value = arr[9];
        f.document.forms[0].ADDRESS2.value = arr[10];
        f.document.forms[0].GZIPCD.value = arr[11];
        f.document.forms[0].GADDRESS1.value = arr[12];
        f.document.forms[0].GADDRESS2.value = arr[13];
        f.document.forms[0].GTELNO.value = arr[14];
        f.document.forms[0].FINSCHOOLCD.value = arr[15];
        f.document.getElementById('FINSCHOOLNAME_ID').innerHTML = arr[5];

        f.document.forms[0].JIZEN_BANGOU.focus();
        f.closeit();
    }else{
        alert("生徒が選択されていません");
    }
 
}

function btn_submit(cmd) {
    if (cmd == 'cancel') {
        document.forms[0].NAME_KANA.value   = "";
        document.forms[0].STUCDLIST.options.length = 0;
        return true;
    } else {
        if (document.forms[0].NAME_KANA.value=="") {
            alert('検索条件が入力されていません');
            return false;
        }
        document.forms[0].cmd.value = cmd;
        document.forms[0].submit();
        return false;
    }
}
// -->
</script>
</head>
<body bgcolor="#ffffff" text="#000000" leftmargin="0" topmargin="0" marginwidth="5" marginheight="5"
link="#006633" vlink="#006633" alink="#006633">
<?php echo $objStucd->getStart() ?>
<table width="100%" border="0" cellspacing="0" cellpadding="0">
<tr>
    <td>
    <table width="100%" border="0" cellspacing="0" cellpadding="0">
    <tr class="no_search_line"> 
        <td> 
        <table width="100%" border="0" cellspacing="1" cellpadding="3">
        <tr class="no_search">
            <th width="25%">志願者ふりがな</th>
            <th bgcolor="#ffffff" align="left">&nbsp;&nbsp;<?php echo $objStucd->getText_Kana() ?></th>
        </tr>
        <tr class="no_search">
            <td colspan="2"  bgcolor="#ffffff" nowrap>
                <?php echo $objStucd->getBtn_Search() ?>
                <?php echo $objStucd->getBtn_Apply() ?>
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                <?php echo $objStucd->getBtn_Back() ?>
            </td>
        </tr>
        <tr class="no_search">
            <td colspan="2" bgcolor="#ffffff"><?php echo $objStucd->getStucdList() ?></td>
        </tr>
        </table>
        </td>
    </tr>
    </table>
    </td>
</tr>
</table>
<input type="hidden" name="STUCD_SESSID" value="<?php echo $_REQUEST["STUCD_SESSID"] ?>">
<input type="hidden" name="cmd">
<?php echo $objStucd->getFinish() ?>
</body>
</html>
<?php
Query::dbCheckIn($db);
?>