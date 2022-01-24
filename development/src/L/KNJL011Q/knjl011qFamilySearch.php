<?php

require_once('for_php7.php');

//兄弟姉妹検索
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
        if ($rq["semester"]) {
            $this->semester = $rq["semester"];
        }
        //テキストボックスの名前
        if ($rq["simaiSchoolKind"]) {
            $this->simaiSchoolKind = $rq["simaiSchoolKind"];
        }
        if ($rq["simaiName"]) {
            $this->simaiName = $rq["simaiName"];
        }

        $this->addr1        = $rq["ADDR1"];     //検索画面の住所
        $this->addr2        = $rq["ADDR2"];     //検索画面の方書
        $this->schregno     = $rq["SCHREGNO"];  //検索画面の学籍番号
        $this->grade        = $rq["GRADE"];     //検索画面の学年
        $this->name         = $rq["NAME"];      //検索画面の氏名
        $this->name_kana    = $rq["NAME_KANA"]; //検索画面の氏名かな

        if ($rq["frame"]) {
            $this->frame = $rq["frame"];    //フレーム名
        }
        $this->cmd = $rq["cmd"];   //コマンド
    }
    //住所
    function getText_Addr1() {
        $this->form->ae( array("type"       => "text",
                               "name"       => "ADDR1",
                               "size"       => 40,
                               "maxlength"  => 40,
                               "extrahtml"  => " STYLE=\"ime-mode: active\" ",
                               "value"      => $this->addr1));
        return $this->form->ge("ADDR1");
    }
    //方書
    function getText_Addr2() {
        $this->form->ae( array("type"       => "text",
                               "name"       => "ADDR2",
                               "size"       => 40,
                               "maxlength"  => 40,
                               "extrahtml"  => " STYLE=\"ime-mode: active\" ",
                               "value"      => $this->addr2));
        return $this->form->ge("ADDR2");
    }
    //学籍番号
    function getSchregno() {
        $this->form->ae( array("type"       => "text",
                               "name"       => "SCHREGNO",
                               "size"       => 10,
                               "maxlength"  => 8,
                               "extrahtml"  => " STYLE=\"ime-mode: inactive\" ",
                               "value"      => $this->schregno));
        return $this->form->ge("SCHREGNO");
    }
    //学年コンボ
    function getGrade() {
        global $db;

        $query  = "SELECT GRADE_NAME2 AS LABEL, GRADE AS VALUE FROM SCHREG_REGD_GDAT WHERE YEAR='".$this->year."' ORDER BY GRADE";
        $result = $db->query($query);

        $opt = array();
        $opt[] = array("label" => "","value" => "");
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
              $opt[] = array("label" => htmlspecialchars($row["LABEL"]),"value" => $row["VALUE"]);
        }

        $this->form->ae( array("type"       => "select",
                               "name"       => "GRADE",
                               "value"      => $this->grade,
                               "options"    => $opt,
                               "extrahtml"  => "onChange=\"\""));
        return $this->form->ge("GRADE");
    }
    //氏名漢字
    function getText_Name() {
        $this->form->ae( array("type"       => "text",
                               "name"       => "NAME",
                               "size"       => 40,
                               "maxlength"  => 40,
                               "extrahtml"  => " STYLE=\"ime-mode: active\" ",
                               "value"      => $this->name));
        return $this->form->ge("NAME");
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
                               "extrahtml"  => "onclick=\"apply_stucd(document.forms[0].STUCDLIST, '".$this->simaiSchoolKind."', '".$this->simaiName."')\"" ));
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
            if ($this->addr1 || $this->addr2 || $this->schregno || $this->grade || $this->name || $this->name_kana) {
                $query  = "";
                $query .= " WITH MAX_ADDR AS ( ";
                $query .= "     SELECT ";
                $query .= "         SCHREGNO, ";
                $query .= "         MAX(ISSUEDATE) AS ISSUEDATE ";
                $query .= "     FROM ";
                $query .= "         SCHREG_ADDRESS_DAT ";
                $query .= "     GROUP BY ";
                $query .= "         SCHREGNO ";
                $query .= " ), SCHREG_ADDRESS AS ( ";
                $query .= "     SELECT ";
                $query .= "         A1.* ";
                $query .= "     FROM ";
                $query .= "         SCHREG_ADDRESS_DAT A1 ";
                $query .= "         INNER JOIN MAX_ADDR A2 ON A2.SCHREGNO = A1.SCHREGNO AND A2.ISSUEDATE = A1.ISSUEDATE ";
                $query .= " ) ";
                $query .= " SELECT ";
                $query .= "     RD.SCHREGNO, ";
                $query .= "     RD.GRADE, ";
                $query .= "     RD.HR_CLASS, ";
                $query .= "     RD.ATTENDNO, ";
                $query .= "     RD.COURSECD, ";
                $query .= "     RD.MAJORCD, ";
                $query .= "     BM.NAME, ";
                $query .= "     BM.NAME_KANA, ";
                $query .= "     RG.SCHOOL_KIND, ";
                $query .= "     N1.ABBV1 AS SCHOOL_KIND_NAME, ";
                $query .= "     RH.HR_NAME, ";
                $query .= "     VALUE(AD.ADDR1, '　') AS ADDR1, ";
                $query .= "     VALUE(AD.ADDR2, '　') AS ADDR2 ";
                $query .= " FROM ";
                $query .= "     SCHREG_REGD_DAT RD ";
                $query .= "     INNER JOIN SCHREG_BASE_MST BM ON BM.SCHREGNO = RD.SCHREGNO ";
                $query .= "     INNER JOIN SCHREG_REGD_GDAT RG ON RG.YEAR = RD.YEAR AND RG.GRADE = RD.GRADE ";
                $query .= "     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'A023' AND N1.NAME1 = RG.SCHOOL_KIND ";
                $query .= "     INNER JOIN SCHREG_REGD_HDAT RH ON RH.YEAR = RD.YEAR AND RH.SEMESTER = RD.SEMESTER AND RH.GRADE = RD.GRADE AND RH.HR_CLASS = RD.HR_CLASS ";
                $query .= "     LEFT JOIN SCHREG_ADDRESS AD ON AD.SCHREGNO = RD.SCHREGNO ";
                $query .= " WHERE ";
                $query .= "     RD.YEAR = '".$this->year."' ";
                $query .= "     AND RD.SEMESTER = '".$this->semester."' ";
                if (strlen($this->addr1)) {
                    $query .= "     AND AD.ADDR1 LIKE '%".$this->addr1."%' ";
                }
                if (strlen($this->addr2)) {
                    $query .= "     AND AD.ADDR2 LIKE '%".$this->addr2."%' ";
                }
                if (strlen($this->schregno)) {
                    $query .= "     AND RD.SCHREGNO = '".$this->schregno."' ";
                }
                if (strlen($this->grade)) {
                    $query .= "     AND RD.GRADE = '".$this->grade."' ";
                }
                if (strlen($this->name)) {
                    $query .= "     AND BM.NAME LIKE '%".$this->name."%' ";
                }
                if (strlen($this->name_kana)) {
                    $query .= "     AND BM.NAME_KANA LIKE '%".$this->name_kana."%' ";
                }
                $query .= " ORDER BY ";
                $query .= "     RD.GRADE, ";
                $query .= "     RD.HR_CLASS, ";
                $query .= "     RD.ATTENDNO ";

                //取得
                $result = $db->query($query);

                while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $opt[] = array("label" => sprintf("%s | %s | %s | %s | %s | %s | %s | %s", 
                                                      $row["SCHREGNO"],
                                                      $row["HR_NAME"],
                                                      $row["NAME"],
                                                      $row["NAME_KANA"],
                                                      $row["ADDR1"],
                                                      $row["ADDR2"],
                                                      $row["GRADE"],
                                                      $row["SCHOOL_KIND"]),
                                    "value" => $row["SCHREGNO"]
                                    );
                }
            }
        }
        $this->form->ae( array("type"  => "select",
                           "name"      => "STUCDLIST",
                           "size"      => "10",
                           "value"     => "",
                           "extrahtml" => "onclick=\"setText(this);\" ondblclick=\"apply_stucd(document.forms[0].STUCDLIST, '".$this->simaiSchoolKind."', '".$this->simaiName."')\" STYLE=\"WIDTH:100%\" WIDTH=\"100%\"",
                           "options"   => (is_array($opt))? $opt : array()));

        return $this->form->ge("STUCDLIST");
    }
    //フォームタブ(開始)
    function getStart() {
        return $this->form->get_start("stucd","POST","knjl011qFamilySearch.php","","stucd");
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
<title>兄弟姉妹検索</title>
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
        var val = obj.options[obj.selectedIndex].text;
        var arr = val.split(" | ");
        for (var i = 0; i <= arr.length; i++) {
            if (arr[i] == "-") {
                arr[i] = "";
            }
        }
        document.forms[0].ADDR1.value       = arr[4];
        document.forms[0].ADDR2.value       = arr[5];
        document.forms[0].SCHREGNO.value    = arr[0];
        document.forms[0].GRADE.value       = arr[6];
        document.forms[0].NAME.value        = arr[2];
        document.forms[0].NAME_KANA.value   = arr[3];
    }catch(e) {
    }
}

function apply_stucd(obj, simaiSchoolKind, simaiName) {
    if(obj.selectedIndex >= 0){
        var val = obj.options[obj.selectedIndex].text;
        var arr = val.split(" | ");
        f.document.forms[0][simaiSchoolKind].value = arr[7]; //校種
        f.document.forms[0][simaiName].value = arr[1] + arr[2]; //年組＋氏名
        f.document.forms[0][simaiName].focus();
        f.closeit();
    }else{
        alert("生徒が選択されていません");
    }
 
}

function apply_stucd2(obj, simaiSchoolKind, simaiName) {

    var setDataSchoolKind = "";
    var setDataName = "";
    var dataCnt = 0;
    for (var i = 0; i < obj.length; i++) {
        if (obj.options[i].selected == 1) {
            var textVal = obj.options[i].text;
            var arr = textVal.split(" | ");

            setDataSchoolKind = arr[7]; //校種
            setDataName = arr[1] + arr[2]; //年組＋氏名
            dataCnt++;
        }
    }
    if (dataCnt == 0) {
        alert("生徒が選択されていません。");
        return false;
    }
    if (dataCnt > 1) {
        alert("生徒が複数選択されています。");
        return false;
    }
    f.document.forms[0][simaiSchoolKind].value = setDataSchoolKind;
    f.document.forms[0][simaiName].value = setDataName;
//    f.btn_submit('apply');
    f.closeit();
}

function btn_submit(cmd) {
    if (cmd == 'cancel') {
        document.forms[0].ADDR1.value       = "";
        document.forms[0].ADDR2.value       = "";
        document.forms[0].SCHREGNO.value    = "";
        document.forms[0].GRADE.value       = "";
        document.forms[0].NAME.value        = "";
        document.forms[0].NAME_KANA.value   = "";
        document.forms[0].STUCDLIST.options.length = 0;
        return true;
    } else {
        if (document.forms[0].ADDR1.value=="" && document.forms[0].ADDR2.value=="" && document.forms[0].SCHREGNO.value=="" && document.forms[0].GRADE.value=="" && document.forms[0].NAME.value=="" && document.forms[0].NAME_KANA.value=="") {
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
            <th width="25%">住所</th>
            <th bgcolor="#ffffff" align="left">&nbsp;&nbsp;<?php echo $objStucd->getText_Addr1() ?></th>
        </tr>
        <tr class="no_search">
            <th width="25%">方書</th>
            <th bgcolor="#ffffff" align="left">&nbsp;&nbsp;<?php echo $objStucd->getText_Addr2() ?></th>
        </tr>
        <tr class="no_search">
            <th width="25%">学籍番号</th>
            <th bgcolor="#ffffff" align="left">&nbsp;&nbsp;<?php echo $objStucd->getSchregno() ?></th>
        </tr>
        <tr class="no_search">
            <th width="25%">学年</th>
            <th bgcolor="#ffffff" align="left">&nbsp;&nbsp;<?php echo $objStucd->getGrade() ?></th>
        </tr>
        <tr class="no_search">
            <th width="25%">生徒氏名</th>
            <th bgcolor="#ffffff" align="left">&nbsp;&nbsp;<?php echo $objStucd->getText_Name() ?></th>
        </tr>
        <tr class="no_search">
            <th width="25%">生徒氏名かな</th>
            <th bgcolor="#ffffff" align="left">&nbsp;&nbsp;<?php echo $objStucd->getText_Kana() ?></th>
        </tr>
        <tr class="no_search">
            <td colspan="2"  bgcolor="#ffffff" nowrap>
                <?php echo $objStucd->getBtn_Search() ?>
                <?php echo $objStucd->getBtn_Apply() ?>
                <?php echo $objStucd->getBtn_Cancel() ?>
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