<?php

require_once('for_php7.php');

/********************************************************************/
/* 変更履歴                                                         */
/* ･NO001：ラジオボタン文言を変更                   山城 2005/02/05 */
/* ･NO002：3学期指定でも、全学年表示                山城 2005/02/05 */
/* ･NO003：チェックボックス追加                     山城 2005/03/07 */
/********************************************************************/

class knjc140Form1
{
    function main(&$model){

//オブジェクト作成
$objForm = new form;

//フォーム作成///////////////////////////////////////////////////////////////////////////////////////////
$arg["start"]   = $objForm->get_start("knjc140Form1", "POST", "knjc140index.php", "", "knjc140Form1");


//年度
$arg["data"]["YEAR"] = CTRL_YEAR;

$objForm->ae( array("type"      => "hidden",
                    "name"      => "YEAR",
                    "value"      => CTRL_YEAR,
                    ) );


//学期リスト
$db = Query::dbCheckOut();
$query = knjc140Query::getSemester($model);
$result = $db->query($query);
$opt_seme = array();
while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
    $opt_seme[]= array('label'     => $row["SEMESTERNAME"],
                        'value' => $row["SEMESTER"]);
}
$result->free();
Query::dbCheckIn($db);

if ($model->field["GAKKI"]=="") $model->field["GAKKI"] = CTRL_SEMESTER;

$objForm->ae( array("type"       => "select",
                    "name"       => "GAKKI",
                    "size"       => "1",
                    "value"      => $model->field["GAKKI"],
                    "extrahtml"  => "onChange=\"return btn_submit('knjc140');\"",
                    "options"    => $opt_seme));

$arg["data"]["GAKKI"] = $objForm->ge("GAKKI");


//学年リストボックスを作成する//////////////////////////////////////////////////////////////////////////////////////
$opt_schooldiv = "学年";

$db = Query::dbCheckOut();
$opt_grade=array();
$query = knjc140Query::getSelectGrade($model);
$result = $db->query($query);
//$i=0;            cut 04/12/16  yamauchi
$grade_flg = true;
while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
    $grade_show= sprintf("%d",$row["GRADE"]);
    $opt_grade[] = array('label' => $grade_show.$opt_schooldiv,
                         'value' => $row["GRADE"]);
//    $i++;    cut 04/12/16  yamauchi
    if( $model->field["GRADE"]==$row["GRADE"] ) $grade_flg = false;
}
if( $grade_flg ) $model->field["GRADE"] = $opt_grade[0]["value"];
$result->free();
Query::dbCheckIn($db);

$objForm->ae( array("type"       => "select",
                    "name"       => "GRADE",
                    "size"       => "1",
                    "value"      => $model->field["GRADE"],
                    "options"    => $opt_grade ) );

$arg["data"]["GRADE"] = $objForm->ge("GRADE");

/* cut 04/12/16 yamauchi
$objForm->ae( array("type"       => "select",
                    "name"       => "GRADE",
                    "size"       => $i,
                    "value"      => $model->field["GRADE"],
                    "options"    => $opt_grade,
                    "extrahtml"     =>"multiple" ) );

*/

//帳票選択ラジオボタンを作成////////////////////////////////////////////////////////////////////////
$opt = array(1, 2);
$disable = 0;
if (!$model->field["OUTPUT"]) $model->field["OUTPUT"] = 1;
$onclick = "onclick =\" return kubun(this);\"";
$extra = array("id=\"OUTPUT1\" ".$onclick
             , "id=\"OUTPUT2\" ".$onclick
              );
$radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, get_count($opt));
foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

//add 04/08/31  yamauchi
if (isset($model->field["OUTPUT"]))
{
    switch ($model->field["OUTPUT"])
    {
        case 1:
            $ratio = "";
            break;
        case 2:
            $ratio = "disabled";
            break;
    }
}
else
{
    $ratio = "";
}


//対象割合コンボボックスを作成する//////  //add  04/08/31  yamauchi  ///////////////////////////////////////////////////////////////////////
$row2 = array(array('label' => "1",'value' => "1"),
              array('label' => "2",'value' => "2"),
              array('label' => "3",'value' => "3"),
              array('label' => "4",'value' => "4"),
              array('label' => "5",'value' => "5"),
              array('label' => "6",'value' => "6"),
              array('label' => "7",'value' => "7"),
              array('label' => "8",'value' => "8"),
              array('label' => "9",'value' => "9"),
              array('label' => "10",'value' => "10") );

$objForm->ae( array("type"       => "select",
                    "name"       => "RATIO",
                    "size"       => "1",
                    "value"      => isset($model->field["RATIO"])?$model->field["RATIO"]:3,
                    "extrahtml"     => $ratio,
                    "options"    => $row2));

$arg["data"]["RATIO"] = $objForm->ge("RATIO");

//欠課処理チェックボックスを作成するNO003
$check = " id=\"OUTPUT3\" ";
if($model->field["OUTPUT3"] == "on" || $model->cmd == ""){
    $check .= "checked";
}else {
    $check .= "";
}
$objForm->ae( array("type"       => "checkbox",
                    "name"       => "OUTPUT3",
                    "value"      => "on",
                    "extrahtml"  => $check));

$arg["data"]["OUTPUT3"] = $objForm->ge("OUTPUT3");

//印刷ボタンを作成する///////////////////////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type"           => "button",
                    "name"        => "btn_print",
                    "value"       => "プレビュー／印刷",
                    "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) );

$arg["button"]["btn_print"] = $objForm->ge("btn_print");


//終了ボタンを作成する//////////////////////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type"           => "button",
                    "name"        => "btn_end",
                    "value"       => "終 了",
                    "extrahtml"   => "onclick=\"closeWin();\"" ) );

$arg["button"]["btn_end"] = $objForm->ge("btn_end");


//hiddenを作成する(必須)/////////////////////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type"      => "hidden",
                    "name"      => "DBNAME",
                    "value"     => DB_DATABASE
                    ) );

$objForm->ae( array("type"      => "hidden",
                    "name"      => "PRGID",
                    "value"     => "KNJC140"
                    ) );


$objForm->ae( array("type"      => "hidden",
                    "name"      => "useCurriculumcd",
                    "value"     => $model->Properties["useCurriculumcd"]
                    ) );

$objForm->ae( array("type"      => "hidden",
                    "name"      => "cmd"
                    ) );

        knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
        knjCreateHidden($objForm, "useKekkaJisu", $model->Properties["useKekkaJisu"]);
        knjCreateHidden($objForm, "useKekka", $model->Properties["useKekka"]);
        knjCreateHidden($objForm, "useLatedetail", $model->Properties["useLatedetail"]);
        knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);


//フォーム終わり
$arg["finish"]  = $objForm->get_finish();


//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
View::toHTML($model, "knjc140Form1.html", $arg); 
}
}
?>
