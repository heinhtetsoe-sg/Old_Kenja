<?php

require_once('for_php7.php');


//GET,POST変数取得
$target  = VARS::request("target");
$mode       = VARS::request("RADIO_BOX");
$Grd_Cls    = VARS::request("Grd_Cls");
$Fname      = VARS::request("FNAME");
$Lname      = VARS::request("LNAME");
$clear_flg  = VARS::request("clear_flg");

//コントロールマスターデータを取得
common::GetControlMaster_Fnc(&$control_data);

//デバッグ----------
# echo "target ". $target ."<BR>";
# echo "mode ".$mode      ."<BR>";
# echo "Grd_Cls ".$Grd_Cls   ."<BR>";
# echo "Fname ".$Fname      ."<BR>";
# echo "Lname ".$Lname  ."<BR>";
# echo "clear_flg ".$clear_flg."<BR>";

//データベースオープン
$db = Query::dbCheckOut();

//年組を取得
$query  = "SELECT DISTINCT ";
$query .= "GRADE || ',' || HR_CLASS AS GC, GRADE, HR_CLASS, ";
$query .= " CHAR(GRADE) || '年' || CHAR(CHAR(INTEGER(HR_CLASS)),2) || '組' AS GC_J ";
$query .= " FROM SCHREG_REGD_DAT ";
$query .= " WHERE YEAR = '";
$query .= $control_data["年度"];
$query .= "' ";
$query .= " ORDER BY GC";

$result = $db->query($query);

/**  コンボボックスの中身を作成  **************************/
$opt = array();
while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
      $opt["Grd_Cls"][] = array("label" => htmlspecialchars($row["GC_J"]),"value" => $row["GC"]);
}

/**  list内情報作成              **************************/
//ラジオボックスがチェックされていない場合，コンボボックスを変更した場合はクリア
if($mode != "" && $clear_flg != "TRUE"){
    switch (trim($mode)) {
        case "GRADECLASS":
            if($Grd_Cls){
                $ary = array();
                $ary = explode(",",$Grd_Cls);
            }
            $where  = " AND T1.GRADE = '";
            $where .= $ary[0];
            $where .= "' ";
            $where .= " AND T1.HR_CLASS = '";
            $where .= $ary[1];
            $where .= "' ";
            break;
        case "NAME":
            $where  = " AND T2.LNAME LIKE '%";
            $where .= $Lname;
            $where .= "%' ";
            $where .= " AND T2.FNAME LIKE '%";
            $where .= $Fname;
            $where .= "%' ";
            break;
    }

    $query = " SELECT DISTINCT "; 
    $query .= " T1.YEAR, T1.GRADE, T1.ATTENDNO,T1.SCHREGNO, T2.LNAME_SHOW || ' ' || T2.FNAME_SHOW AS NAME_SHOW, ";
    $query .= " T2.LKANA || ' ' || T2.FKANA AS KANA_SHOW, T2.SEX,";
    $query .= " T1.HR_CLASS, T1.UPDATED,T3.SCHREGNO AS EXIST ";
    $query .= " FROM  SCHREG_REGD_DAT T1 ";
    $query .= " INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
    $query .= " LEFT JOIN SCHREG_RELA_DAT T3 ON T3.SCHREGNO = T1.SCHREGNO ";
    $query .= " WHERE T1.YEAR = '";
    $query .= $control_data["年度"];;
    $query .= "' "; 
    $query .= $where;
    $query .= " AND T1.SEMESTER = '";
    $query .= $control_data["学期"];
    $query .= "' ";
    $query .= " ORDER BY T1.GRADE,T1.HR_CLASS,T1.ATTENDNO ";

    $result = $db->query($query);

    //listボックスの中身を作成------------------------------
    $spacer = "　|　";
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){    
                                                           //学籍番号
        $opt["list"][] = array("label" => htmlspecialchars($row["SCHREGNO"].$spacer.
                                                           //学年，組
                                                           $row["GRADE"]."年".$row["HR_CLASS"]."組".$spacer.
                                                           //性別
                                                           (($row["SEX"] == 1 )? "男" : "女").$spacer.
                                                           //親族データの有無
                                                           "親族データ".(($row["EXIST"])? "有り" : "無し").$spacer.
                                                           //氏名，かな氏名
                                                           $row["NAME_SHOW"]." （".$row["KANA_SHOW"]."）"),

                               "value" => (($row["SCHREGNO"])? $row["SCHREGNO"] : " ")
                              );
    }
}

//データベースクローズ
$result->free();
Query::dbCheckIn($db);

//フォーム作成
$objForm = new form;
$start = $objForm->get_start("c1", "POST", "stucd.php", "", "c1");

//ヘッダー表
$header = array( "CTRL_CHAR1" => $control_data["年度"],
                 "CTRL_CHAR2" => $control_data["学期"]
                );

//ラジオボタン{ (1)年組 (2)漢字名前 }
if(!$mode) $mode = "GRADECLASS"; //ラジオボタン初期値
$objForm->ae( array("type"        => "radio",
                    "name"        => "RADIO_BOX",
                    "value"       => $mode,
                    "extrahtml"   => ""
                     ));

$forms["RADIO_BOX1"] = $objForm->ge("RADIO_BOX","GRADECLASS");
$forms["RADIO_BOX2"] = $objForm->ge("RADIO_BOX","NAME");

//氏名(姓)
$objForm->ae( array("type"        => "text",
                    "name"        => "LNAME",
                    "size"        => 40,
                    "maxlength"   => 20,
                    "extrahtml"   => "onChange=\"\"",
                    "value"       => $Lname));

$forms["LNAME"] = $objForm->ge("LNAME");

//氏名(名)
$objForm->ae( array("type"        => "text",
                    "name"        => "FNAME",
                    "size"        => 40,
                    "maxlength"   => 20,
                    "extrahtml"   => "onChange=\"\"",
                    "value"       => $Fname ));

$forms["FNAME"] = $objForm->ge("FNAME");


//年組コンボボックス作成
$objForm->ae( array("type"        => "select",
                    "name"        => "Grd_Cls",
                    "value"       => $Grd_Cls,
                    "options"     => (is_array($opt["Grd_Cls"]))? $opt["Grd_Cls"] : array(),
                    "extrahtml"   => " onChange=\"return btn_submit('clean');\" "
                    ));

$forms["Grd_Cls"] = $objForm->ge("Grd_Cls");

//リンク先
$link = REQUESTROOT."/LF/LFA120_2/index.php";

//リストボックス作成
$objForm->ae( array("type"        => "select",
                    "name"        => "LISTUP",
                    "size"        => 19,
                    "options"     => (is_array($opt["list"]))? $opt["list"] : array(),
                    "extrahtml"   => "ondblclick=\"apply_student('".$target."');\" STYLE=\"WIDTH:100%\" WIDTH=\"100%\""));

$forms["LISTUP"] = $objForm->ge("LISTUP");

//検 索ボタンを作成する
$objForm->ae( array("type"        => "button",
                    "name"        => "btn_seek",
                    "value"       => "検 索",
                    "extrahtml"   => "onclick=\"return btn_submit('main');\"" ) );

$forms["btn_seek"] = $objForm->ge("btn_seek");

//反 映ボタンを作成する
$objForm->ae( array("type"          => "button",
                    "name"          => "btn_rflct",
                    "value"         => "反 映",
                    "extrahtml"   => "onclick=\"apply_student('".$target."');\""  ) );

$forms["btn_rflct"] = $objForm->ge("btn_rflct");

//終了ボタンを作成する
$objForm->ae( array("type"          => "button",
                    "name"          => "btn_back",
                    "value"         => "終 了",
                    "extrahtml"   => "onclick=\" self.close();\""  ) );

$froms["btn_back"] = $objForm->ge("btn_back");

//hiddenを作成する
$objForm->ae( array("type"      => "hidden",
                    "name"      => "cmd"
                    ) );

//hiddenを作成する
$objForm->ae( array("type"      => "hidden",
                    "name"      => "target",
                    "value"     => $target
                    ) );

//hiddenを作成する
$objForm->ae( array("type"      => "hidden",
                    "name"      => "clear_flg"
                    ) );


$finish  = $objForm->get_finish();
?>
<html>
<head>
<title>生徒情報検索</title>
<meta http-equiv="Content-Type" content="text/html; charset=<?php echo CHARSET ?>">
<link rel="stylesheet" href="gk.css">
<script language="JavaScript">
<!--
var a;    
a = setInterval('window.focus()',15000);

function btn_submit(cmd) {

    if (cmd == 'main'){
        document.forms[0].clear_flg.value = '';
    }

    if (cmd == 'clean'){
        document.forms[0].clear_flg.value = 'TRUE';
        document.forms[0].RADIO_BOX.value = '1';
        document.forms[0].RADIO_BOX[0].checked = true;
        cmd = 'main';
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function apply_student(target) {
    //親画面があるかチェック
    if (is_opener()){
        obj = opener.document.forms[0][target]
        with (document.c1) {
            index = LISTUP.selectedIndex;
            if (index >= 0 && parseInt(LISTUP.options[index].value) != -1) {
                x = new Array(2);
                x = LISTUP.options[index].text.split("　|　");
                obj.focus();
                obj.value = x[0];
                self.close();
            }
        }
    }else{
        self.close();
    }
}

//親画面があるかチェック
function is_opener() {
    var ua = navigator.userAgent
    if( !!window.opener )
        if( ua.indexOf('MSIE 4')!=-1 && ua.indexOf('Win')!=-1 ) 
            return !window.opener.closed
        else return typeof window.opener.document == 'object'
    else return false
}
function observeDisp(){
    if (!is_opener()){
        self.close();
    }
}
// -->
</script>
</head>
<body bgcolor="#ffffff" text="#000000" leftmargin="0" topmargin="0" marginwidth="5" marginheight="5" 
link="#006633" vlink="#006633" alink="#006633" onLoad="setInterval('observeDisp()',5000)">
<?php echo $start ?> 
<style type="text/css">
</style>
<BR>
<table width="80%" border="0" cellspacing="0" cellpadding="0" align="center"> 
    <td valign="top"> 
      <table width="100%" border="0" cellspacing="0" cellpadding="5">
        <tr> 
          <td> 
            <table width="100%" border="0" cellspacing="0" cellpadding="0">
              <tr class="no_search_line"> 
                <td> 
                  <table width="100%" border="0" cellspacing="1" cellpadding="3">
                  <tr class="no_search">
                          <th width="80%">生徒検索</th>
                  </tr>
                  </table>
                </td>
              </tr>
            </table>
          </td>
        </tr>
      </table>
    </td>
  </tr>
</table>

<table width="92%" border="0" cellspacing="0" cellpadding="0" align="center"> 
  <tr>
    <td valign="top"> 
      <table width="100%" border="0" cellspacing="0" cellpadding="5">
        <tr> 
          <td> 
            <table width="100%" border="0" cellspacing="0" cellpadding="0">
              <tr class="no_search_line"> 
                <td> 
                  <table width="100%" border="0" cellspacing="1" cellpadding="0">
                      <tr>
                          <td colspan="2">
                          <?php echo $forms["LISTUP"] ?>
                          </td>
                      </tr>
                      <tr class="no_search">
                          <th align="center">
                            &nbsp;<?php echo $header["CTRL_CHAR1"] ?>年度&nbsp;&nbsp;
                            <?php echo $header["CTRL_CHAR2"] ?>学期&nbsp;
                          </th>
                          <th width="50%">
                            &nbsp;<?php echo $forms["btn_rflct"] ?>&nbsp;<?php echo $forms["btn_back"] ?>
                          </th>
                      </tr>
                  </table>
                </td>
              </tr>
            </table>
          </td>
        </tr>
      </table>
    </td>
  </tr>
</table>

<table width="80%" border="0" cellspacing="0" cellpadding="0" align="center"> 
     <tr>
      <td valign="top">
       <table width="100%" border="0" cellspacing="0" cellpadding="5">
        <tr> 
          <td > 
            <table width="100%" border="0" cellspacing="5" cellpadding="0">
              <tr class="no_search_line"> 
                <td> 
                  <table width="100%" border="0" cellspacing="1" cellpadding="5" >
                    <tr align="left"> 
                        <th class="no_search" width="30%">
                            &nbsp;&nbsp;<?php echo $forms["RADIO_BOX1"] ?>&nbsp;&nbsp;年組
                        </th>
                        <td bgcolor="#ffffff">&nbsp; &nbsp; &nbsp; &nbsp;<?php echo $forms["Grd_Cls"] ?></td>
                    </tr>
                    <tr align="left"> 
                        <th class="no_search">
                            &nbsp;&nbsp;<?php echo $forms["RADIO_BOX2"] ?>&nbsp;&nbsp;漢字氏名
                        </th>
                        <td bgcolor="#ffffff">
                            &nbsp;&nbsp;姓：&nbsp; <?php echo $forms["LNAME"] ?>
                            &nbsp;&nbsp;名：&nbsp; <?php echo $forms["FNAME"] ?>
                        </td>
                    </tr>
                    <tr>
                       <th class="no_search">
                       </th>
                       <td  bgcolor="#ffffff">
                            <?php echo $forms["btn_seek"] ?>
                            <?php echo $froms["btn_back"] ?>
                       </td>
                    </tr>
                  </table>
                </td>
              </tr>
            </table>
          </td>
        </tr>
      </table>
    </td>
  </tr>
 </table>

<?php echo $finish ?>
<script language="JavaScript">
</script>
</body>
</html>
