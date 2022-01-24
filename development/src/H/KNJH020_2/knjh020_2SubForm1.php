<?php

require_once('for_php7.php');

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
    var $url;
    var $mode;

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
        $this->schregno = $rq["SCHREGNO"];	//検索画面の学籍番号
        $this->grd_cls  = $rq["Grd_Cls"];	//検索画面の年組リスト
        $this->name     = $rq["NAME"];		//検索画面の氏名
        $this->name_kana = $rq["NAME_KANA"];//検索画面の氏名かな

        if($rq["MODE"]){
            $this->mode = $rq["MODE"];		//親族データをコピーするか、画面にセットするかのフラグ
        }
        $this->cmd          = $rq["cmd"];      //コマンド
    }
    //年組コンボボックス作成
    function getGrd_Cls(){
       //DBオープン

        $query .= " ORDER BY GC";

       global $db;
        $query  = "SELECT DISTINCT ";
        $query .= "       GRADE || ',' || HR_CLASS AS GC, GRADE, HR_CLASS, ";
        $query .= "       HR_NAME AS GC_J ";
        $query .= " FROM SCHREG_REGD_HDAT ";
        $query .= " WHERE YEAR = '".$this->year."' ";

#       $query  = "SELECT DISTINCT ";
#       $query .= "       GRADE || ',' || HR_CLASS AS GC, GRADE, HR_CLASS, ";
#       $query .= "       CHAR(CHAR(INTEGER(GRADE)),2)  || '年' || CHAR(CHAR(INTEGER(HR_CLASS)),2) || '組' AS GC_J ";
#       $query .= " FROM SCHREG_REGD_DAT ";
#       $query .= " WHERE YEAR = '".$this->year."' ";
#       $query .= " ORDER BY GC";
       $result = $db->query($query);

        $opt = array();
        $opt["Grd_Cls"][] = array("label" => "","value" => "00");
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
              $opt["Grd_Cls"][] = array("label" => htmlspecialchars($row["GC_J"]),"value" => $row["GC"]);
        }
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
                               "extrahtml"     => "onclick=\"apply_stucd(document.forms[0].STUCDLIST,'".$this->mode."')\"" ));
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
        global $db;
        $opt = array();

        if($this->cmd == "btn_search"){
            if ($this->stucd || $this->schregno || $this->grd_cls || $this->name || $this->name_kana){
                $query  = "SELECT DISTINCT";
                $query .= "    SM.SCHREGNO, ";
                $query .= "    SM.NAME, ";
                $query .= "    SM.NAME_KANA, ";
                $query .= "    SM.SEX, ";
                $query .= "    SD.GRADE, ";
                $query .= "    SD.HR_CLASS, ";

                $query .= "       HR_NAME AS GRD_CLS, ";
#                $query .= "    CHAR(CHAR(INTEGER(SD.GRADE)),2)  || '年' || CHAR(CHAR(INTEGER(SD.HR_CLASS)),2) || '組' AS GRD_CLS, ";

                $query .= "    GD.SCHREGNO AS GUARDNO, ";
                $query .= "    SD.ATTENDNO ";
                $query .= "FROM ";
                $query .= "    SCHREG_BASE_MST  SM LEFT OUTER JOIN SCHREG_RELA_DAT GD ON SM.SCHREGNO = GD.SCHREGNO,";
                $query .= "    SCHREG_REGD_DAT  SD, ";
                $query .= "    SCHREG_REGD_HDAT SH ";
                $query .= "WHERE ";
                $query .= "    SM.SCHREGNO = SD.SCHREGNO AND ";
                $query .= "    SD.YEAR = '".$this->year."' AND ";
                $query .= "    SH.YEAR = '".$this->year."' AND ";
                $query .= "    SD.SEMESTER = '".$this->semester."' ";
                if(isset($this->schregno)){
                    $query .= " AND SM.SCHREGNO LIKE '".$this->schregno."%' ";
                }
                if(isset($this->grd_cls) && $this->grd_cls != 00){
                    list($grade,$hr_class) = preg_split("/,/",$this->grd_cls);
                    $query .= " AND SD.GRADE = '".$grade."' AND SD.HR_CLASS = '".$hr_class."' ";
                    $query .= " AND SH.GRADE = '".$grade."' AND SH.HR_CLASS = '".$hr_class."' ";
                }
                if(isset($this->name)){
                    $query .= " AND SM.NAME LIKE '%".$this->name."%' ";
                }
                if(isset($this->name_kana)){
                    $query .= " AND SM.NAME_KANA LIKE '%".$this->name_kana."%' ";
                }
                $query .= "ORDER BY GRD_CLS ,SD.ATTENDNO ";
                //教科、科目、クラス取得
                $result = $db->query($query);

                while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                    $opt[] = array("label" => sprintf("%s | %s | %s | %s | %s | %s | %s ", 
                                                      $row["SCHREGNO"],
                                                      $row["GRD_CLS"],
                                                      $row["GUARDNO"]==""?"データなし":"データあり",
                                                      $row["NAME"],
                                                      $row["NAME_KANA"],
                                                      $row["SEX"]==1? "男" : "女",
                                                      $row["GRADE"].",".$row["HR_CLASS"]),
                                    "value" => $row["SCHREGNO"]
                                    );
                }
            }
        }
        $this->form->ae( array("type"      => "select",
                           "name"          => "STUCDLIST",
                           "size"          => "10",
                           "value"         => "",
                           "extrahtml"     => "onclick=\"setText(this);\" ondblclick=\"apply_stucd(document.forms[0].STUCDLIST,'".$this->mode."')\" STYLE=\"WIDTH:100%\" WIDTH=\"100%\"",
                           "options"       => (is_array($opt))? $opt : array()));

        return $this->form->ge("STUCDLIST");
    }
    //フォームタブ(開始)
    function getStart(){
        return $this->form->get_start("stucd","POST","knjh020_2SubForm1.php","","stucd");
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

function Btn_Back(){
    f.closeit();
}

function setText(obj){
    try{
        var val = obj.options[obj.selectedIndex].text;
        var arr = val.split(" | ");
        document.forms[0].SCHREGNO.value = arr[0];
        document.forms[0].Grd_Cls.value = arr[6];
        document.forms[0].NAME.value = arr[3];
        document.forms[0].NAME_KANA.value = arr[4];
    }catch(e){
    }
}

function apply_stucd(obj,cmd){
    if(obj.selectedIndex >= 0){
        var val = obj.options[obj.selectedIndex].text;
        var arr = val.split(" | ");
        var data = arr[2]=="データあり"?"1":"2";
        if(cmd == 'reflect'){
            if(data == "2"){
                alert("データがありません。");
                return false;
            }
            f.document.forms[0].STUCD.value = arr[0];
            f.btn_submit('apply');
        }else if(cmd == 'set'){
            f.document.forms[0].RELA_SCHREGNO.value = arr[0];
        }
        f.closeit();
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
function btn_cancel(){
    

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