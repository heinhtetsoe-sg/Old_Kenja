<?php

require_once('for_php7.php');

require_once('knjh020Query.inc');

//兄弟姉妹検索
class stucd
{
    var $form;
    var $year;
    var $semester;
    var $stucd;
    var $schregno;
    var $grd_cls;
    var $name;
    var $name_kana;
    var $cd;
    var $cmd;

    function stucd(){
        $this->form = new form();
        //コントロールマスターデータを取得
        common::GetControlMaster_Fnc($control_data);
        $this->year = $control_data["年度"];
        $this->semester = $control_data["学期"];
    }
    function main($rq){
        if($rq["stucd"]){
            $this->stucd    = $rq["stucd"];	//反映元の学籍番号
        }
        if($rq["frame"]){
            $this->frame    = $rq["frame"];
        }
        if($rq["CD"]){
            $this->cd       = $rq["CD"];	//反映先の学籍番号
        }
        if($rq["SCHREGNO"]){
            $this->schregno = $rq["SCHREGNO"];	//検索画面の学籍番号
        }else{
            unset($this->schregno);
        }
        if($rq["Grd_Cls"]){
            $this->grd_cls  = $rq["Grd_Cls"];	//検索画面の年組コンボボックス
        }else{
            unset($this->grd_cls);
        }
        if($rq["NAME"]){
            $this->name = $rq["NAME"];                //検索画面の生徒氏名
        }else{
            unset($this->name);
        }
        if($rq["NAME_KANA"]){
            $this->name_kana = $rq["NAME_KANA"];//検索画面の生徒氏名かな
        }else{
            unset($this->name_kana);
        }
        $this->cmd          = $rq["cmd"];      //コマンド
    }
    //年組コンボボックス作成
    function getGrd_Cls(){
        //DBオープン
        $db = Query::dbCheckOut();

        $result = $db->query(knjh020Query::getGrdCls());

        $opt = array();
        $opt["Grd_Cls"][] = array("label" => "","value" => "00");
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
              $opt["Grd_Cls"][] = array("label" => htmlspecialchars($row["GC_J"]),"value" => $row["GC"]);
        }

        //DBクローズ
        Query::dbCheckIn($db);

        $this->form->ae( array("type"          => "select",
                               "name"          => "Grd_Cls",
                               "value"         => $this->grd_cls,
                               "options"       => (is_array($opt["Grd_Cls"]))? $opt["Grd_Cls"] : array(),
                               "extrahtml"     => "onChange=\"\""));
        return $this->form->ge("Grd_Cls");
    }
    //学籍番号
    function getSchregno(){
        $this->form->ae( array("type"          => "text",
                               "name"          => "SCHREGNO",
                               "size"          => 10,
                               "extrahtml"     => "",
                               "value"         => $this->schregno));
        return $this->form->ge("SCHREGNO");
    }
    //氏名漢字
    function getText_Name(){
        $this->form->ae( array("type"          => "text",
                               "size"          => 40,
                               "maxlength"     => 40,
                               "extrahtml"     => "",
                               "value"         => $this->name,
                               "name"          => "NAME"));
        return $this->form->ge("NAME");
    }
    //氏名かな
    function getText_Kana(){
        $this->form->ae( array("type"          => "text",
                               "size"          => 40,
                               "maxlength"     => 40,
                               "extrahtml"     => "",
                               "value"         => $this->name_kana,
                               "name"          => "NAME_KANA"));
        return $this->form->ge("NAME_KANA");
    }
    //検索ボタン作成
    function getBtn_Search(){
        $this->form->ae( array("type"          => "button",
                               "name"          => "search",
                               "value"         => "検 索",
                               "extrahtml"     => "onclick=\"return btn_submit('btn_search')\""));
        return $this->form->ge("search");
    }
    //反映ボタン作成
    function getBtn_Apply(){
        $this->form->ae( array("type"          => "button",
                               "name"          => "apply",
                               "value"         => "反 映",
                               "extrahtml"     => "onclick=\"apply_stucd(document.forms[0].STUCDLIST)\"" ));
        return $this->form->ge("apply");
    }
    //取消ボタン作成
    function getBtn_Cancel(){
        $this->form->ae( array("type"          => "button",
                               "name"          => "btn_cancel",
                               "value"         => "取 消",
                               "extrahtml"     => "onclick=\"return btn_submit('cancel')\""));

       return $this->form->ge("btn_cancel");
    }
    //戻るボタン作成
    function getBtn_Back(){
        $this->form->ae( array("type"          => "button",
                               "name"          => "btn_back",
                               "value"         => "戻 る",
                               "extrahtml"     => "onclick=\"Btn_Back();\""));

       return $this->form->ge("btn_back");
    }
    //生徒リスト作成
    function getStucdList(){
        $opt = array();

        $db = Query::dbCheckOut();
        if($this->cmd == "btn_search" || $this->cmd == "apply"){
            if ($this->stucd || $this->schregno || $this->grd_cls || $this->name || $this->name_kana){
                $query  = "SELECT DISTINCT";
                $query .= "    SM.SCHREGNO, ";
                $query .= "    SM.NAME, ";
                $query .= "    SM.NAME_KANA, ";
                $query .= "    SM.SEX, ";
                $query .= "    SD.GRADE, ";
                $query .= "    SD.HR_CLASS, ";
                //2004/08/19 arakaki $query .= "    CHAR(CHAR(INTEGER(SD.GRADE)),2)  || '年' || CHAR(CHAR(INTEGER(SD.HR_CLASS)),2) || '組' AS GRD_CLS, ";
                $query .= "    CHAR(CHAR(INTEGER(SD.GRADE)),2)  || '年' || SD.HR_CLASS || '組' AS GRD_CLS, ";
                $query .= "    GD.SCHREGNO AS GUARDNO, ";
                $query .= "    SD.ATTENDNO ";
                $query .= ",";
                $query .= "    GD.RELATIONSHIP, ";
                $query .= "    GD.GUARD_NAME, ";
                $query .= "    GD.GUARD_KANA, ";
                $query .= "    GD.GUARD_SEX, ";
                $query .= "    GD.GUARD_BIRTHDAY, ";
                $query .= "    GD.GUARD_ZIPCD, ";
                $query .= "    GD.GUARD_ADDR1, ";
                $query .= "    GD.GUARD_ADDR2, ";
                $query .= "    GD.GUARD_TELNO, ";
                $query .= "    GD.GUARD_FAXNO, ";
                $query .= "    GD.GUARD_E_MAIL, ";
                $query .= "    GD.GUARD_JOBCD, ";
                $query .= "    GD.GUARD_WORK_NAME, ";
                $query .= "    GD.GUARD_WORK_TELNO, ";
                $query .= "    GD.GUARANTOR_RELATIONSHIP, ";
                $query .= "    GD.GUARANTOR_NAME, ";
                $query .= "    GD.GUARANTOR_KANA, ";
                $query .= "    GD.GUARANTOR_SEX, ";
                $query .= "    GD.GUARANTOR_ZIPCD, ";
                $query .= "    GD.GUARANTOR_ADDR1, ";
                $query .= "    GD.GUARANTOR_ADDR2, ";
                $query .= "    GD.GUARANTOR_TELNO, ";
                $query .= "    GD.GUARANTOR_JOBCD, ";
                $query .= "    GD.PUBLIC_OFFICE ";
                $query .= "FROM ";
                $query .= "    SCHREG_BASE_MST SM LEFT OUTER JOIN GUARDIAN_DAT GD ON SM.SCHREGNO = GD.SCHREGNO,";
                $query .= "    SCHREG_REGD_DAT SD ";
                $query .= "WHERE ";
                $query .= "    SM.SCHREGNO = SD.SCHREGNO AND ";
                $query .= "    SD.YEAR = '".$this->year."' AND ";
                $query .= "    SD.SEMESTER = '".$this->semester."' ";
                if($this->cmd == "apply"){
                    $query .= " AND SM.SCHREGNO = '".$this->stucd."' ";
                }
                if($this->cmd == "btn_search"){
                    if(isset($this->schregno)){
                        $query .= " AND SM.SCHREGNO LIKE '".$this->schregno."%' ";
                    }
                    if(isset($this->grd_cls) && $this->grd_cls != 00){
                        list($grade,$hr_class) = preg_split("/,/",$this->grd_cls);
                        $query .= " AND SD.GRADE = '".$grade."' AND SD.HR_CLASS = '".$hr_class."' ";
                    }
                    if(isset($this->name)){
                        $query .= " AND SM.NAME LIKE '%".$this->name."%' ";
                    }
                    if(isset($this->name_kana)){
                        $query .= " AND SM.NAME_KANA LIKE '%".$this->name_kana."%' ";
                    }
                }
                //print $query;
                $query .= "ORDER BY GRD_CLS ,SD.ATTENDNO ";
                //教科、科目、クラス取得
                $result = $db->query($query);

                while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                    $opt[] = array("label" => sprintf("%s | %s | %s | %s | %s | %s | %s | %s | %s | %s | %s | %s | %s | %s | %s | %s | %s | %s | %s | %s | %s | %s | %s | %s | %s | %s | %s | %s | %s | %s | %s ", 
                                                      $row["SCHREGNO"],
                                                      $row["GRD_CLS"],
                                                      $row["GUARDNO"]==""?"データなし":"データあり",
                                                      $row["NAME"],
                                                      $row["NAME_KANA"],
                                                      $row["SEX"]==1? "男" : "女",
                                                      $row["RELATIONSHIP"]==""?"&nbsp;":$row["RELATIONSHIP"],
                                                      $row["GUARD_NAME"]==""?"&nbsp;":$row["GUARD_NAME"],
                                                      $row["GUARD_KANA"]==""?"&nbsp;":$row["GUARD_KANA"],
                                                      $row["GUARD_SEX"]==""?"&nbsp;":$row["GUARD_SEX"],
                                                      $row["GUARD_BIRTHDAY"]==""?"&nbsp;":str_replace("-", "/", $row["GUARD_BIRTHDAY"]),
                                                      $row["GUARD_ZIPCD"]==""?"&nbsp;":$row["GUARD_ZIPCD"],
                                                      $row["GUARD_ADDR1"]==""?"&nbsp;":$row["GUARD_ADDR1"],
                                                      $row["GUARD_ADDR2"]==""?"&nbsp;":$row["GUARD_ADDR2"],
                                                      $row["GUARD_TELNO"]==""?"&nbsp;":$row["GUARD_TELNO"],
                                                      $row["GUARD_FAXNO"]==""?"&nbsp;":$row["GUARD_FAXNO"],
                                                      $row["GUARD_E_MAIL"]==""?"&nbsp;":$row["GUARD_E_MAIL"],
                                                      $row["GUARD_JOBCD"]==""?"&nbsp;":$row["GUARD_JOBCD"],
                                                      $row["GUARD_WORK_NAME"]==""?"&nbsp;":$row["GUARD_WORK_NAME"],
                                                      $row["GUARD_WORK_TELNO"]==""?"&nbsp;":$row["GUARD_WORK_TELNO"],
                                                      $row["GUARANTOR_RELATIONSHIP"]==""?"&nbsp;":$row["GUARANTOR_RELATIONSHIP"],
                                                      $row["GUARANTOR_NAME"]==""?"&nbsp;":$row["GUARANTOR_NAME"],
                                                      $row["GUARANTOR_KANA"]==""?"&nbsp;":$row["GUARANTOR_KANA"],
                                                      $row["GUARANTOR_SEX"]==""?"&nbsp;":$row["GUARANTOR_SEX"],
                                                      $row["GUARANTOR_ZIPCD"]==""?"&nbsp;":$row["GUARANTOR_ZIPCD"],
                                                      $row["GUARANTOR_ADDR1"]==""?"&nbsp;":$row["GUARANTOR_ADDR1"],
                                                      $row["GUARANTOR_ADDR2"]==""?"&nbsp;":$row["GUARANTOR_ADDR2"],
                                                      $row["GUARANTOR_TELNO"]==""?"&nbsp;":$row["GUARANTOR_TELNO"],
                                                      $row["GUARANTOR_JOBCD"]==""?"&nbsp;":$row["GUARANTOR_JOBCD"],
                                                      $row["PUBLIC_OFFICE"]==""?"&nbsp;":$row["PUBLIC_OFFICE"],
                                                      $row["GRADE"].",".$row["HR_CLASS"]),
                                    "value" => $row["SCHREGNO"]
                                    );
                }
            }
        }
        Query::dbCheckIn($db);
	$this->form->ae( array("type"          => "select",
	                   "name"          => "STUCDLIST",
	                   "size"          => "10",
	                   "value"         => "",
	                   "extrahtml"     => "onclick=\"setText(this);\" ondblclick=\"apply_stucd(document.forms[0].STUCDLIST)\" STYLE=\"WIDTH:100%\" WIDTH=\"100%\"",
	                   "options"       => (is_array($opt))? $opt : array()));
        return $this->form->ge("STUCDLIST");
    }
    //フォームタブ(開始)
    function getStart(){
        return $this->form->get_start("stucd","POST","knjh020SubForm1.php","","stucd");
    }
    //フォームタブ(終了)
    function getFinish(){
        return $this->form->get_finish();
    }
    //フレーム取得
    function getFrame(){
        return $this->frame;
    }
    function getTarget(){
        return $this->target;
    }
}

if ($_GET["cmd"] == "apply"){
    $objStucd = new stucd();
    $objStucd->main($_REQUEST);
}else{
    if ($_REQUEST["STUCD_SESSID"] == "") exit;
    $sess = new APP_Session($_REQUEST["STUCD_SESSID"], 'stucd');
    if (!$sess->isCached($_REQUEST["STUCD_SESSID"], 'stucd')) {
        $sess->data = new stucd();
    }
    $sess->data->main($_REQUEST);

    $objStucd = $sess->getData();
}
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

function Btn_Back(){
    f.closeit();
}

function setText(obj){
    try{
        var val = obj.options[obj.selectedIndex].text;
        var arr = val.split(" | ");
        document.forms[0].SCHREGNO.value = arr[0];
        document.forms[0].Grd_Cls.value = arr[30];
        document.forms[0].NAME.value = arr[3];
        document.forms[0].NAME_KANA.value = arr[4];
    }catch(e){
    }
}

function apply_stucd(obj){
    if(obj.selectedIndex >= 0){
        var val = obj.options[obj.selectedIndex].text;
        var arr = val.split(" | ");
        var data = arr[2]=="データあり"?"1":"2";
        if(data == "2"){
            alert("データがありません。");
            return false;
        }
	for(var i=6;i<=29;i++){
	    if(arr[i] == "&nbsp;"){
	        arr[i] = "";
	    }
	}
	f.document.forms[0].J_STUCD.value = arr[0];
	f.document.forms[0].RELATIONSHIP.value = arr[6];
	f.document.forms[0].GUARD_NAME.value = arr[7];
	f.document.forms[0].GUARD_KANA.value = arr[8];
	f.document.forms[0].GUARD_SEX.value = arr[9];
	f.document.forms[0].GUARD_BIRTHDAY.value = arr[10];
	f.document.forms[0].J_ZIPCD.value = arr[11];
	f.document.forms[0].GUARD_ADDR1.value = arr[12];
	f.document.forms[0].GUARD_ADDR2.value = arr[13];
	f.document.forms[0].GUARD_TELNO.value = arr[14];
	f.document.forms[0].GUARD_FAXNO.value = arr[15];
	f.document.forms[0].GUARD_E_MAIL.value = arr[16];
	f.document.forms[0].GUARD_JOBCD.value = arr[17];
	f.document.forms[0].GUARD_WORK_NAME.value = arr[18];
	f.document.forms[0].GUARD_WORK_TELNO.value = arr[19];
	f.document.forms[0].GUARANTOR_RELATIONSHIP.value = arr[20];
	f.document.forms[0].GUARANTOR_NAME.value = arr[21];
	f.document.forms[0].GUARANTOR_KANA.value = arr[22];
	f.document.forms[0].GUARANTOR_SEX.value = arr[23];
	f.document.forms[0].J_GUARANTOR_ZIPCD.value = arr[24];
	f.document.forms[0].GUARANTOR_ADDR1.value = arr[25];
	f.document.forms[0].GUARANTOR_ADDR2.value = arr[26];
	f.document.forms[0].GUARANTOR_TELNO.value = arr[27];
	f.document.forms[0].GUARANTOR_JOBCD.value = arr[28];
	f.document.forms[0].PUBLIC_OFFICE.value = arr[29];
	f.closeit();
	return true;
    }else{
        alert("生徒が選択されていません。");
        return false;
    }
}

function btn_submit(cmd){
    if (cmd == 'cancel'){
        document.forms[0].SCHREGNO.value ="";
        document.forms[0].Grd_Cls.value = "00";
        document.forms[0].NAME.value    ="";
        document.forms[0].NAME_KANA.value ="";
        document.forms[0].STUCDLIST.options.length = 0;
        return true;
    }else{
        if(document.forms[0].SCHREGNO.value==""&&document.forms[0].Grd_Cls.value==00&&document.forms[0].NAME.value==""&&document.forms[0].NAME_KANA.value==""){
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
            <td colspan="2"  bgcolor="#ffffff">
                <?php echo $objStucd->getBtn_Search() ?>
                <?php echo $objStucd->getBtn_Apply() ?>
                <?php echo $objStucd->getBtn_Cancel() ?>
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
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
if ($_GET["cmd"] == "apply"){
?>
<script language="JavaScript">
    if (document.forms[0].STUCDLIST.length == 1){
        var val = document.forms[0].STUCDLIST.options[0].text;
        var arr = val.split(" | ");
        var data = arr[2]=="データあり"?"1":"2";
        if(data == "2"){
            alert("データがありません。");
        }
        for(var i=6;i<=29;i++){
            if(arr[i] == "&nbsp;"){
                arr[i] = "";
            }
        }
        f.document.forms[0].J_STUCD.value = arr[0];
        f.document.forms[0].RELATIONSHIP.value = arr[6];
        f.document.forms[0].GUARD_NAME.value = arr[7];
        f.document.forms[0].GUARD_KANA.value = arr[8];
        f.document.forms[0].GUARD_SEX.value = arr[9];
        f.document.forms[0].GUARD_BIRTHDAY.value = arr[10];
        f.document.forms[0].J_ZIPCD.value = arr[11];
        f.document.forms[0].GUARD_ADDR1.value = arr[12];
        f.document.forms[0].GUARD_ADDR2.value = arr[13];
        f.document.forms[0].GUARD_TELNO.value = arr[14];
        f.document.forms[0].GUARD_FAXNO.value = arr[15];
        f.document.forms[0].GUARD_E_MAIL.value = arr[16];
        f.document.forms[0].GUARD_JOBCD.value = arr[17];
        f.document.forms[0].GUARD_WORK_NAME.value = arr[18];
        f.document.forms[0].GUARD_WORK_TELNO.value = arr[19];
        f.document.forms[0].GUARANTOR_RELATIONSHIP.value = arr[20];
        f.document.forms[0].GUARANTOR_NAME.value = arr[21];
        f.document.forms[0].GUARANTOR_KANA.value = arr[22];
        f.document.forms[0].GUARANTOR_SEX.value = arr[23];
        f.document.forms[0].J_GUARANTOR_ZIPCD.value = arr[24];
        f.document.forms[0].GUARANTOR_ADDR1.value = arr[25];
        f.document.forms[0].GUARANTOR_ADDR2.value = arr[26];
        f.document.forms[0].GUARANTOR_TELNO.value = arr[27];
        f.document.forms[0].GUARANTOR_JOBCD.value = arr[28];
        f.document.forms[0].PUBLIC_OFFICE.value = arr[29];
    }else{
        alert("生徒が見つかりませんでした。");
    }
</script>
<?php
}
?>