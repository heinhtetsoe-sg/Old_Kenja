<?php

require_once('for_php7.php');


class knja131aForm1
{
    function main(&$model){

//オブジェクト作成
$objForm = new form;

//フォーム作成////////////////////////////////////////////////////////////////////////
$arg["start"]   = $objForm->get_start("knja131aForm1", "POST", "knja131aindex.php", "", "knja131aForm1");

//年度テキストボックスを作成する///////////////////////////////////////////////////////////////////////////////////

$arg["data"]["YEAR"] = $model->control["年度"];

$objForm->ae( array("type"      => "hidden",
                    "name"      => "YEAR",
                    "value"      => $model->control["年度"],
                    ) );

//学期テキストボックスを作成する///////////////////////////////////////////////////////////////////////////////////

$arg["data"]["GAKKI"] = $model->control["学期名"][$model->control["学期"]];

$objForm->ae( array("type"      => "hidden",
                    "name"      => "GAKKI",
                    "value"      => $model->control["学期"],
                    ) );

//ポップアップカレンダーを作成する/////////////////////////////////////////////////////////////////////////////////

//2004/03/22 nakamoto del   $arg["el"]["DATE"]=View::popUpCalendar($objForm,"DATE",isset($model->field["DATE"])?$model->field["DATE"]:$model->control["学籍処理日"]);

//宮城県の場合、画面切り替え

$db = Query::dbCheckOut();
        $z010name1 = "";
        $query = knja131aQuery::getNameMst("Z010", "00");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $z010name1 = $row["NAME1"];
        }
        $result->free();
        if ($z010name1 == 'miyagiken') {
            $arg["not_miyagiken"] = "";
        } else {
            $arg["not_miyagiken"] = "1";
        }
Query::dbCheckIn($db);


//出力順ラジオボタンを作成  //NO001
$opt[0]=1;
$opt[1]=2;
$disable = 0;
if (!$model->field["OUTPUT"]) $model->field["OUTPUT"] = 1;
if ($model->field["OUTPUT"] == 1) $disable = 1;
$objForm->ae( array("type"       => "radio",
                    "name"       => "OUTPUT",
                    "value"      => isset($model->field["OUTPUT"])?$model->field["OUTPUT"]:"1",
                    "extrahtml"  => " onclick =\" return btn_submit('clickchange');\"",
                    "multiple"   => $opt));

$arg["data"]["OUTPUT1"] = $objForm->ge("OUTPUT",1);
$arg["data"]["OUTPUT2"] = $objForm->ge("OUTPUT",2);

if ($disable == 1){
    $arg["student"] = 1;
}else {
    $arg["hr_class"] = 2;
}

//クラス選択コンボボックスを作成する///////////////////////////////////////////////////////////////////////////////
//NO001
$db = Query::dbCheckOut();
if ($disable == 1){
    $query = common::getHrClassAuth(CTRL_YEAR,CTRL_SEMESTER,AUTHORITY,STAFFCD);
}else {
    $query = knja131aQuery::getAuth2($model);
}
$result = $db->query($query);
while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
    $row1[]= array('label' => $row["LABEL"],
                   'value' => $row["VALUE"]);
}
$result->free();
Query::dbCheckIn($db);

if(!isset($model->field["GRADE_HR_CLASS"])) {
    $model->field["GRADE_HR_CLASS"] = $row1[0]["value"];
}

if($model->cmd == 'clickchange' ) {
    $model->field["GRADE_HR_CLASS"] = $row1[0]["value"];
    $model->cmd = 'knja131a';
}

$objForm->ae( array("type"       => "select",
                    "name"       => "GRADE_HR_CLASS",
                    "size"       => "1",
                    "value"      => $model->field["GRADE_HR_CLASS"],
                    "extrahtml"  => "onchange=\"return btn_submit('knja131a'),AllClearList();\"",
                    "options"    => isset($row1)?$row1:array()));

$arg["data"]["GRADE_HR_CLASS"] = $objForm->ge("GRADE_HR_CLASS");

/*
$db = Query::dbCheckOut();
$query = knja131aQuery::getAuth($model);
$result = $db->query($query);
while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
    $row1[]= array('label' => $row["LABEL"],
                    'value' => $row["VALUE"]);
}
$result->free();
Query::dbCheckIn($db);

if(!isset($model->field["GRADE_HR_CLASS"])) {
    $model->field["GRADE_HR_CLASS"] = $row1[0]["value"];
}

$objForm->ae( array("type"       => "select",
                    "name"       => "GRADE_HR_CLASS",
                    "size"       => "1",
                    "value"      => $model->field["GRADE_HR_CLASS"],
                    "extrahtml"  => "onchange=\"return btn_submit('knja131a');\"",
                    "options"    => isset($row1)?$row1:array()));

$arg["data"]["GRADE_HR_CLASS"] = $objForm->ge("GRADE_HR_CLASS");
*/
//対象者リストを作成する/////////////////////////////////////////////////////////////////////////////////////////////
$db = Query::dbCheckOut();

if ($disable == 1){     //NO001
    $query = "SELECT SCHREG_REGD_DAT.SCHREGNO AS SCHREGNO,SCHREG_REGD_DAT.SCHREGNO || '　' || ATTENDNO || '番' || '　' || NAME_SHOW AS NAME ".
             "FROM SCHREG_BASE_MST INNER JOIN SCHREG_REGD_DAT ON SCHREG_BASE_MST.SCHREGNO = SCHREG_REGD_DAT.SCHREGNO ".
             "WHERE (((SCHREG_REGD_DAT.YEAR)='" .$model->control["年度"] ."') AND ".
             "((SCHREG_REGD_DAT.SEMESTER)='" .$model->control["学期"] ."') AND ".
//           "((SCHREG_REGD_DAT.GRADE)='" .substr($model->field["GRADE_HR_CLASS"],0,2) ."') AND ".
//           "((SCHREG_REGD_DAT.HR_CLASS)='" .substr($model->field["GRADE_HR_CLASS"],2,2) ."'))".
             "((SCHREG_REGD_DAT.GRADE || SCHREG_REGD_DAT.HR_CLASS)='" .$model->field["GRADE_HR_CLASS"] ."'))".
             "ORDER BY ATTENDNO";
    $result = $db->query($query);
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt1[]= array('label' =>  $row["NAME"],
                       'value' => $row["SCHREGNO"]);
    }
}else {  //NO001
    $query = common::getHrClassAuth(CTRL_YEAR,CTRL_SEMESTER,AUTHORITY,STAFFCD);
    $result = $db->query($query);
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        if (substr($row["VALUE"],0,2) != $model->field["GRADE_HR_CLASS"]) continue;
        $opt1[]= array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
    }
}

$result->free();
Query::dbCheckIn($db);

$objForm->ae( array("type"       => "select",
                    "name"       => "category_name",
//                  "extrahtml"  => "multiple style=\"width=230px\" width=\"230px\" ondblclick=\"move1('left')\"",
                    "extrahtml"  => "multiple style=\"width:230px\" width=\"230px\" ondblclick=\"move1('left',$disable)\"",  //NO001
                    "size"       => "20",
                    "options"    => isset($opt1)?$opt1:array()));

$arg["data"]["CATEGORY_NAME"] = $objForm->ge("category_name");

//生徒一覧リストを作成する/////////////////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type"       => "select",
                    "name"       => "category_selected",
//                  "extrahtml"  => "multiple style=\"width=230px\" width=\"230px\" ondblclick=\"move1('right')\"",
                    "extrahtml"  => "multiple style=\"width:230px\" width=\"230px\" ondblclick=\"move1('right',$disable)\"",  //NO001
                    "size"       => "20",
                    "options"    => array()));

$arg["data"]["CATEGORY_SELECTED"] = $objForm->ge("category_selected");


//対象選択ボタンを作成する（全部）/////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type" => "button",
                    "name"        => "btn_rights",
                    "value"       => ">>",
//                    "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('right');\"" ) );
                    "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('right',$disable);\"" ) );  //NO001

$arg["button"]["btn_rights"] = $objForm->ge("btn_rights");


//対象取消ボタンを作成する（全部）//////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type" => "button",
                    "name"        => "btn_lefts",
                    "value"       => "<<",
//                    "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('left');\"" ) );
                    "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('left',$disable);\"" ) );  //NO001

$arg["button"]["btn_lefts"] = $objForm->ge("btn_lefts");


//対象選択ボタンを作成する（一部）//////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type" => "button",
                    "name"        => "btn_right1",
                    "value"       => "＞",
//                    "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('right');\"" ) );
                    "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('right',$disable);\"" ) );  //NO001

$arg["button"]["btn_right1"] = $objForm->ge("btn_right1");


//対象取消ボタンを作成する（一部）//////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type" => "button",
                    "name"        => "btn_left1",
                    "value"       => "＜",
//                    "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('left');\"" ) );
                    "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('left',$disable);\"" ) );  //NO001

$arg["button"]["btn_left1"] = $objForm->ge("btn_left1");


//印刷ボタンを作成する///////////////////////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type" => "button",
                    "name"        => "btn_print",
                    "value"       => "プレビュー／印刷",
                    "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) );

$arg["button"]["btn_print"] = $objForm->ge("btn_print");


//csvボタンを作成する/////////////////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type" => "button",
                    "name"        => "btn_csv",
                    "value"       => "ＣＳＶ出力",
                    "extrahtml"   => "onclick=\"return btn_submit('csv');\" disabled" ) );

$arg["button"]["btn_csv"] = $objForm->ge("btn_csv");


//終了ボタンを作成する//////////////////////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type" => "button",
                    "name"        => "btn_end",
                    "value"       => "終 了",
                    "extrahtml"   => "onclick=\"closeWin();\"" ) );

$arg["button"]["btn_end"] = $objForm->ge("btn_end");

//帳票種別チェックボックスを作成する////////////////////////////////////////////////////////////////////////////////

$objForm->ae( array("type"       => "checkbox",
                    "name"       => "seito",
                    "checked"    => true,
                    "extrahtml"  => "onclick=\"kubun();\"",
                    "value"      => isset($model->field["seito"])?$model->field["seito"]:"1"));

$objForm->ae( array("type"       => "checkbox",
                    "name"       => "simei",
                    "checked"    => true,
                    "extrahtml"  => "onclick=\"kubun();\"",
                    "value"      => isset($model->field["simei"])?$model->field["simei"]:"1"));

$objForm->ae( array("type"       => "checkbox",
                    "name"       => "katsudo",
                    "checked"    => true,
                    "extrahtml"  => "onclick=\"kubun();\"",
                    "value"      => isset($model->field["katsudo"])?$model->field["katsudo"]:"1"));

$objForm->ae( array("type"       => "checkbox",
                    "name"       => "gakushu1",
                    "checked"    => true,
                    "extrahtml"  => "onclick=\"kubun();\"",
                    "value"      => isset($model->field["gakushu1"])?$model->field["gakushu1"]:"1"));

/*  2006/03/23 yamauchi del
$objForm->ae( array("type"       => "checkbox",
                    "name"       => "gakushu2",
                    "checked"    => true,
                    "extrahtml"  => "onclick=\"kubun();\"",
                    "value"      => isset($model->field["gakushu2"])?$model->field["gakushu2"]:"1"));

$objForm->ae( array("type"       => "checkbox",
                    "name"       => "tani",
                    "checked"    => true,
                    "extrahtml"  => "onclick=\"kubun();\"",
                    "value"      => isset($model->field["tani"])?$model->field["tani"]:"1"));
*/

$arg["data"]["SEITO"] = $objForm->ge("seito");
$arg["data"]["SIMEI"] = $objForm->ge("simei");
$arg["data"]["KATSUDO"] = $objForm->ge("katsudo");
$arg["data"]["GAKUSHU1"] = $objForm->ge("gakushu1");
//$arg["data"]["GAKUSHU2"] = $objForm->ge("gakushu2");
//$arg["data"]["TANI"] = $objForm->ge("tani");

//hiddenを作成する(必須)/////////////////////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type"      => "hidden",
                    "name"      => "DBNAME",
                    "value"      => DB_DATABASE
                    ) );


$objForm->ae( array("type"      => "hidden",
                    "name"      => "PRGID",
                    "value"     => "KNJA131A"
                    ) );


$objForm->ae( array("type"      => "hidden",
                    "name"      => "cmd"
                    ) );

$objForm->ae( array("type"      => "hidden",
                    "name"      => "HTRAINREMARK_DAT_TOTALSTUDYACT_SIZE_J_OLD",
                    "value"      => $model->Properties["HTRAINREMARK_DAT_TOTALSTUDYACT_SIZE_J_OLD"]
                    ) );

$objForm->ae( array("type"      => "hidden",
                    "name"      => "HTRAINREMARK_DAT_VIEWREMARK_SIZE_J_OLD",
                    "value"      => $model->Properties["HTRAINREMARK_DAT_VIEWREMARK_SIZE_J_OLD"]
                    ) );

$objForm->ae( array("type"      => "hidden",
                    "name"      => "HTRAINREMARK_DAT_TOTALSTUDYVAL_SIZE_J_OLD",
                    "value"      => $model->Properties["HTRAINREMARK_DAT_TOTALSTUDYVAL_SIZE_J_OLD"]
                    ) );

$objForm->ae( array("type"      => "hidden",
                    "name"      => "HTRAINREMARK_DAT_ATTENDREC_REMARK_SIZE_J_OLD",
                    "value"      => $model->Properties["HTRAINREMARK_DAT_ATTENDREC_REMARK_SIZE_J_OLD"]
                    ) );

$objForm->ae( array("type"      => "hidden",
                    "name"      => "HTRAINREMARK_DAT_TOTALREMARK_SIZE_J_OLD",
                    "value"      => $model->Properties["HTRAINREMARK_DAT_TOTALREMARK_SIZE_J_OLD"]
                    ) );

$objForm->ae( array("type"      => "hidden",
                    "name"      => "useSchregRegdHdat",
                    "value"      => $model->Properties["useSchregRegdHdat"]
                    ) );

//NO001
$objForm->ae( array("type"      => "hidden",
                    "name"      => "DOCUMENTROOT",
                    "value"     => DOCUMENTROOT ) );

//CSV用
$objForm->ae( array("type"      => "hidden",
                    "name"      => "selectdata") );
                    
knjCreateHidden($objForm, "useCurriculumcd" , $model->Properties["useCurriculumcd"]);
knjCreateHidden($objForm, "useAddrField2" , $model->Properties["useAddrField2"]);

$objForm->ae( array("type"      => "hidden",
                    "name"      => "seitoSidoYorokuCyugakuKirikaeNendo",
                    "value"      => $model->Properties["seitoSidoYorokuCyugakuKirikaeNendo"]
                    ) );

$objForm->ae( array("type"      => "hidden",
                    "name"      => "seitoSidoYorokuCyugakuKirikaeNendoForRegdYear",
                    "value"      => $model->Properties["seitoSidoYorokuCyugakuKirikaeNendoForRegdYear"]
                    ) );

//フォーム終わり
$arg["finish"]  = $objForm->get_finish();


//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
View::toHTML($model, "knja131aForm1.html", $arg); 
}
}
?>
