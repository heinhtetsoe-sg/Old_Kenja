<?php

require_once('for_php7.php');

//兄弟姉妹検索
class stucd {
    var $form;
    var $year;
    var $semester;
    var $stucd;
    var $familyNo;
    var $schregno;
    var $grd_cls;
    var $name;
    var $name_kana;
    var $guard_telno;
    var $cd;
    var $cmd;
    var $url;
    var $mode;
    var $useGuardian2;

    function stucd() {
        $this->form = new form();
        //コントロールマスターデータを取得
        //common::GetControlMaster_Fnc($control_data);
        //$this->year = $control_data["年度"];
        //$this->semester = $control_data["学期"];
    }
    function main($rq) {
        if($rq["stucd"]) {
            $this->stucd = $rq["stucd"]; //反映元の学籍番号
        }
        if($rq["frame"]) {
            $this->frame = $rq["frame"];
        }
        if($rq["CD"]) {
            $this->cd = $rq["CD"];    //反映先の学籍番号
        }
        if ($rq["cmd"] == "search") {
            $this->sendFamilyNo = $_GET["FAMILYNO"];
        }
        $this->familyNo  = $rq["FAMILYNO"];  //検索画面の家族番号
        $this->schregno  = $rq["SCHREGNO"];  //検索画面の学籍番号
        $this->grd_cls   = $rq["Grd_Cls"];   //検索画面の年組リスト
        $this->name      = $rq["NAME"];      //検索画面の氏名
        $this->name_kana = $rq["NAME_KANA"]; //検索画面の氏名かな
        $this->guard_telno  = $rq["GUARD_TELNO"];   //検索画面の保護者 電話番号

        if($rq["useGuardian2"]) {
            $this->useGuardian2 = $rq["useGuardian2"];  //プロパティ 1:保護者2を使用する
        }
        if ($rq["year"]) {
            $this->year = $rq["year"];
        }
        if ($rq["semester"]) {
            $this->semester = $rq["semester"];
        }

        if($rq["MODE"]) {
            $this->mode = $rq["MODE"];      //親族データをコピーするか、画面にセットするかのフラグ
        }
        $this->cmd = $rq["cmd"];   //コマンド
    }
    //年組コンボボックス作成
    function getGrd_Cls() {
       //DBオープン

        $query .= " ORDER BY GC";

       global $db;
        $query  = "SELECT DISTINCT ";
        $query .= "       GRADE || ',' || HR_CLASS AS GC, GRADE, HR_CLASS, ";
        $query .= "       HR_NAME AS GC_J ";
        $query .= " FROM SCHREG_REGD_HDAT ";
        $query .= " WHERE YEAR = '".$this->year."' ";

        $result = $db->query($query);

        $opt = array();
        $opt["Grd_Cls"][] = array("label" => "","value" => "00");
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
              $opt["Grd_Cls"][] = array("label" => htmlspecialchars($row["GC_J"]),"value" => $row["GC"]);
        }
        $this->form->ae( array("type"       => "select",
                               "name"       => "Grd_Cls",
                               "value"      => $this->grd_cls,
                               "options"    => (is_array($opt["Grd_Cls"]))? $opt["Grd_Cls"] : array(),
                               "extrahtml"  => "onChange=\"\""));
        return $this->form->ge("Grd_Cls");
    }
    //家族番号
    function getSendFamilyno() {
        return $this->sendFamilyNo;
    }
    //家族番号
    function getFamilyno() {
        $this->form->ae( array("type"       => "text",
                               "name"       => "FAMILYNO",
                               "size"       => 10,
                               "extrahtml"  => " STYLE=\"ime-mode: inactive\" ",
                               "value"      => $this->familyNo));
        return $this->form->ge("FAMILYNO");
    }
    //学籍番号
    function getSchregno() {
        $this->form->ae( array("type"       => "text",
                               "name"       => "SCHREGNO",
                               "size"       => 10,
                               "extrahtml"  => " STYLE=\"ime-mode: inactive\" ",
                               "value"      => $this->schregno));
        return $this->form->ge("SCHREGNO");
    }
    //氏名漢字
    function getText_Name() {
        $this->form->ae( array("type"       => "text",
                               "size"       => 40,
                               "maxlength"  => 40,
                               "extrahtml"  => " STYLE=\"ime-mode: active\" ",
                               "value"      => $this->name,
                               "name"       => "NAME"));
        return $this->form->ge("NAME");
    }
    //氏名かな
    function getText_Kana() {
        $this->form->ae( array("type"       => "text",
                               "size"       => 40,
                               "maxlength"  => 40,
                               "extrahtml"  => " STYLE=\"ime-mode: active\" ",
                               "value"      => $this->name_kana,
                               "name"       => "NAME_KANA"));
        return $this->form->ge("NAME_KANA");
    }
    //保護者 電話番号
    function getText_Guard_Telno() {
        $this->form->ae( array("type"       => "text",
                               "size"       => 16,
                               "maxlength"  => 14,
                               "extrahtml"  => " STYLE=\"ime-mode: inactive\" ",
                               "value"      => $this->guard_telno,
                               "name"       => "GUARD_TELNO"));
        return $this->form->ge("GUARD_TELNO");
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
                               "extrahtml"  => "onclick=\"apply_stucd(document.forms[0].STUCDLIST,'".$this->mode."')\"" ));
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
            if ($this->stucd || $this->familyNo || $this->schregno || $this->grd_cls || $this->name || $this->name_kana || $this->guard_telno) {
                $query  = "";
                $query .= "SELECT DISTINCT";
                $query .= "    BM.BASE_REMARK1 AS FAMILYNO, ";
                $query .= "    SM.SCHREGNO, ";
                $query .= "    SM.NAME, ";
                $query .= "    SM.NAME_KANA, ";
                $query .= "    SM.SEX, ";
                $query .= "    SM.BIRTHDAY, ";
                $query .= "    SD.GRADE, ";
                $query .= "    SD.HR_CLASS, ";
                $query .= "    HR_NAME AS GRD_CLS, ";
                if (strlen($this->guard_telno)) {
                    $query .= "    CASE WHEN G1.GUARD_TELNO  LIKE '%".$this->guard_telno."%' THEN G1.GUARD_TELNO ";
                    $query .= "         WHEN G1.GUARD_TELNO2 LIKE '%".$this->guard_telno."%' THEN G1.GUARD_TELNO2 ";
                    if ($this->useGuardian2 == '1') {
                        $query .= "     WHEN G2.GUARD_TELNO  LIKE '%".$this->guard_telno."%' THEN G2.GUARD_TELNO ";
                        $query .= "     WHEN G2.GUARD_TELNO2 LIKE '%".$this->guard_telno."%' THEN G2.GUARD_TELNO2 ";
                    }
                    $query .= "         WHEN SM.EMERGENCYTELNO  LIKE '%".$this->guard_telno."%' THEN SM.EMERGENCYTELNO ";
                    $query .= "         WHEN SM.EMERGENCYTELNO2 LIKE '%".$this->guard_telno."%' THEN SM.EMERGENCYTELNO2 ";
                    $query .= "    END AS GUARD_TELNO, ";
                } else {
                    $query .= "    G1.GUARD_TELNO AS GUARD_TELNO, ";
                }
                $query .= "    SD.ATTENDNO ";
                $query .= "FROM ";
                $query .= "    SCHREG_BASE_MST  SM ";
                $query .= "    LEFT JOIN GUARDIAN_DAT  G1 ON SM.SCHREGNO = G1.SCHREGNO ";
                if ($this->useGuardian2 == '1') {
                    $query .= "    LEFT JOIN GUARDIAN2_DAT G2 ON SM.SCHREGNO = G2.SCHREGNO ";
                }
                $query .= "    LEFT JOIN SCHREG_BASE_DETAIL_MST BM ON SM.SCHREGNO = BM.SCHREGNO ";
                $query .= "                                       AND BM.BASE_SEQ = '009', ";
                $query .= "        SCHREG_REGD_DAT  SD, ";
                $query .= "        SCHREG_REGD_HDAT SH ";
                $query .= "WHERE ";
                $query .= "        SM.SCHREGNO = SD.SCHREGNO ";
                $query .= "    AND SD.YEAR     = '".$this->year."' ";
                $query .= "    AND SD.SEMESTER = '".$this->semester."' ";
                $query .= "    AND SH.YEAR     = SD.YEAR ";
                $query .= "    AND SH.SEMESTER = SD.SEMESTER ";
                $query .= "    AND SH.GRADE    = SD.GRADE ";
                $query .= "    AND SH.HR_CLASS = SD.HR_CLASS ";
                if(strlen($this->familyNo)) {
                    $query .= " AND BM.BASE_REMARK1 LIKE '".$this->familyNo."%' ";
                }
                if(strlen($this->schregno)) {
                    $query .= " AND SM.SCHREGNO LIKE '".$this->schregno."%' ";
                }
                if(strlen($this->grd_cls) && $this->grd_cls != 00) {
                    list($grade,$hr_class) = preg_split("/,/",$this->grd_cls);
                    $query .= " AND SD.GRADE = '".$grade."' AND SD.HR_CLASS = '".$hr_class."' ";
                    $query .= " AND SH.GRADE = '".$grade."' AND SH.HR_CLASS = '".$hr_class."' ";
                }
                if(strlen($this->name)) {
                    $query .= " AND SM.NAME LIKE '%".$this->name."%' ";
                }
                if(strlen($this->name_kana)) {
                    $query .= " AND SM.NAME_KANA LIKE '%".$this->name_kana."%' ";
                }
                if(strlen($this->guard_telno)) {
                    $query .= " AND (  G1.GUARD_TELNO  LIKE '%".$this->guard_telno."%' ";
                    $query .= "     OR G1.GUARD_TELNO2 LIKE '%".$this->guard_telno."%' ";
                    if ($this->useGuardian2 == '1') {
                        $query .= " OR G2.GUARD_TELNO  LIKE '%".$this->guard_telno."%' ";
                        $query .= " OR G2.GUARD_TELNO2 LIKE '%".$this->guard_telno."%' ";
                    }
                    $query .= " OR SM.EMERGENCYTELNO  LIKE '%".$this->guard_telno."%' ";
                    $query .= " OR SM.EMERGENCYTELNO2 LIKE '%".$this->guard_telno."%' ";
                    $query .= " ) ";
                }
                $query .= "ORDER BY GRD_CLS ,SD.ATTENDNO ";
                //教科、科目、クラス取得
                $result = $db->query($query);

                while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $opt[] = array("label" => sprintf("%'-1s | %'-1s | %'-1s | %'-1s | %'-1s | %'-1s | %'-1s | %'-1s | %'-1s | %'-1s ", 
                                                      $row["FAMILYNO"] ? $row["FAMILYNO"] : "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;",
                                                      $row["SCHREGNO"],
                                                      $row["GRD_CLS"],
                                                      $row["FAMILYNO"]=="　　　　"?"データなし":"データあり",
                                                      $row["NAME"],
                                                      $row["NAME_KANA"],
                                                      $row["SEX"]==1? "男" : "女",
                                                      $row["GRADE"].",".$row["HR_CLASS"],
                                                      $row["GUARD_TELNO"],
                                                      str_replace("-","/",$row["BIRTHDAY"])),
                                    "value" => $row["SCHREGNO"]
                                    );
                }
            }
        }
        $this->form->ae( array("type"  => "select",
                           "name"      => "STUCDLIST",
                           "size"      => "10",
                           "value"     => "",
                           "extrahtml" => "multiple onclick=\"setText(this);\" STYLE=\"WIDTH:100%\" WIDTH=\"100%\"",
                           "options"   => (is_array($opt))? $opt : array()));

        return $this->form->ge("STUCDLIST");
    }
    //フォームタブ(開始)
    function getStart() {
        return $this->form->get_start("stucd","POST","knjl011pFamilySearch.php","","stucd");
    }
    //フォームタブ(終了)
    function getFinish() {
        return $this->form->get_finish();
    }
    //フレーム取得
    function getFrame() {
        return $this->frame;
    }
    function getTarget() {
        return $this->target;
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
        document.forms[0].FAMILYNO.value    = arr[0];
        document.forms[0].SCHREGNO.value    = arr[1];
        document.forms[0].Grd_Cls.value     = arr[7];
        document.forms[0].NAME.value        = arr[4];
        document.forms[0].NAME_KANA.value   = arr[5];
        document.forms[0].GUARD_TELNO.value = arr[8];
    }catch(e) {
    }
}

function apply_stucd(obj,cmd) {

    var setData = "";
    var sep = "";
    var befFamilyNo = "";
    for (var i = 0; i < obj.length; i++) {
        if (obj.options[i].selected == 1) {
            var textVal = obj.options[i].text;
            var arr = textVal.split(" | ");
            var checkFamily = arr[0].replace(/\s+/g, "") ;
            if (checkFamily != "" && document.forms[0].SEND_FAMILYNO.value != "" && arr[0] != document.forms[0].SEND_FAMILYNO.value) {
                alert('家族番号が不一致のデータがあります。');
                return false;
            }
            if (checkFamily != "" && befFamilyNo != "" && checkFamily != befFamilyNo) {
                alert('複数の家族番号が選択されています。');
                return false;
            }
            //setData += sep + obj.options[i].text;
            setData += sep + arr[2] + arr[4]; //年組＋氏名
            sep = ",";
            if (checkFamily != "") {
                befFamilyNo = checkFamily;
            }
        }
    }
    if (setData == "") {
        alert("生徒が選択されていません。");
        return false;
    }
    f.document.forms[0].SIMAI_NAME.value = setData;
//    f.btn_submit('apply');
    f.closeit();
}

function btn_submit(cmd) {
    if (cmd == 'cancel') {
        document.forms[0].FAMILYNO.value  ="";
        document.forms[0].SCHREGNO.value  ="";
        document.forms[0].Grd_Cls.value   = "00";
        document.forms[0].NAME.value      ="";
        document.forms[0].NAME_KANA.value ="";
        document.forms[0].STUCDLIST.options.length = 0;
        document.forms[0].GUARD_TELNO.value ="";
        return true;
    }else{
        if(document.forms[0].FAMILYNO.value==""&&document.forms[0].SCHREGNO.value==""&&document.forms[0].Grd_Cls.value==00&&document.forms[0].NAME.value==""&&document.forms[0].NAME_KANA.value==""&&document.forms[0].GUARD_TELNO.value=="") {
            alert('検索条件が入力されていません');
            return false;
        }
        document.forms[0].cmd.value = cmd;
        document.forms[0].submit();
        return false;
    }
}
function btn_cancel() {
    

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
            <th width="25%">家族番号</th>
            <th bgcolor="#ffffff" align="left">&nbsp;&nbsp;<?php echo $objStucd->getFamilyno() ?></th>
        </tr>
        <tr class="no_search">
            <th width="25%">学籍番号</th>
            <th bgcolor="#ffffff" align="left">&nbsp;&nbsp;<?php echo $objStucd->getSchregno() ?></th>
        </tr>
        <tr class="no_search">
            <th width="25%">学年</th>
            <th bgcolor="#ffffff" align="left">&nbsp;&nbsp;<?php echo $objStucd->getGrd_cls() ?></th>
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
            <th width="25%">保護者 電話番号</th>
            <th bgcolor="#ffffff" align="left">&nbsp;&nbsp;<?php echo $objStucd->getText_Guard_Telno() ?></th>
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
<input type="hidden" name="SEND_FAMILYNO" value="<?php echo $objStucd->getSendFamilyno() ?>">
<input type="hidden" name="cmd">
<?php echo $objStucd->getFinish() ?>
</body>
</html>
<?php
Query::dbCheckIn($db);
?>