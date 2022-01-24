<?php

require_once('for_php7.php');


class knjl255cForm1
{
    function main(&$model){

//オブジェクト作成
$objForm = new form;

//フォーム作成//////////////////////////////////////////////////////////////////////////////////////////////
$arg["start"]   = $objForm->get_start("knjl255cForm1", "POST", "knjl255cindex.php", "", "knjz100Form1");

//中学校・塾ラジオボタン 1:中学校選択 2:塾選択
$opt_div = array(1, 2);
$model->field["FINSCHOOLDIV"] = ($model->field["FINSCHOOLDIV"] == "") ? "1" : $model->field["FINSCHOOLDIV"];
$extra = array("id=\"FINSCHOOLDIV1\" onClick=\"return btn_submit('knjl255c')\"", "id=\"FINSCHOOLDIV2\" onClick=\"return btn_submit('knjl255c')\"");
$radioArray = knjCreateRadio($objForm, "FINSCHOOLDIV", $model->field["FINSCHOOLDIV"], $extra, $opt_div, get_count($opt_div));
foreach($radioArray as $key => $val) $arg["data"][$key] = $val;


//小学校一覧リスト作成する///////////////////////////////////////////////////////////////////////////////
$db = Query::dbCheckOut();
//$query = "SELECT FINSCHOOLCD AS VALUE,FINSCHOOL_NAME AS LABEL FROM finschool_mst";
//$result = $db->query($query);
//while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
//    $row1[]= array('label' => $row["VALUE"]."  ".$row["LABEL"],
//                    'value' => $row["VALUE"]);
//}
if ($model->field["FINSCHOOLDIV"] == "1") {
    $result      = $db->query(knjl255cQuery::selectFinSchoolQuery($model->control["年度"]));   
    $row1 = array();
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
    {
        $row1[]    = array("label" => $row["FINSCHOOLCD"]."  ".$row["FINSCHOOL_NAME"], 
                           "value" => $row["FINSCHOOLCD"]);
    }
    $arg["data"]["NAME_LIST"] = '出身校';
    $result->free();
} else {
    $result      = $db->query(knjl255cQuery::selectPriSchoolQuery($model->control["年度"]));   
    $row1 = array();
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
    {
        $row1[]    = array("label" => $row["PRISCHOOLCD"]."  ".$row["PRISCHOOL_NAME"], 
                           "value" => $row["PRISCHOOLCD"]);
    }
    $arg["data"]["NAME_LIST"] = '塾';
    $result->free();
}
Query::dbCheckIn($db);

$objForm->ae( array("type"       => "select",
                    "name"       => "SCHOOL_NAME",
//					"extrahtml"  => "multiple style=\"width=220px\" width=\"220px\" ondblclick=\"move1('left')\"",
					"extrahtml"  => "multiple style=\"width:220px\" width=\"220px\" ondblclick=\"move('left','SCHOOL_SELECTED','SCHOOL_NAME',1)\"",
                    "size"       => "20",
                    "options"    => isset($row1)?$row1:array()));

$arg["data"]["SCHOOL_NAME"] = $objForm->ge("SCHOOL_NAME");


//出力対象クラスリストを作成する///////////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type"       => "select",
                    "name"       => "SCHOOL_SELECTED",
//					"extrahtml"  => "multiple style=\"width=220px\" width=\"220px\" ondblclick=\"move1('right')\"",
					"extrahtml"  => "multiple style=\"width:220px\" width=\"220px\" ondblclick=\"move('right','SCHOOL_SELECTED','SCHOOL_NAME',1)\"",
                    "size"       => "20",
                    "options"    => array()));

$arg["data"]["SCHOOL_SELECTED"] = $objForm->ge("SCHOOL_SELECTED");


//対象選択ボタンを作成する（全部）/////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type" => "button",
                    "name"        => "btn_rights",
                    "value"       => ">>",
//                    "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('right');\"" ) );
                    "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move('sel_del_all','SCHOOL_SELECTED','SCHOOL_NAME',1);\"" ) );

$arg["button"]["btn_rights"] = $objForm->ge("btn_rights");


//対象取消ボタンを作成する（全部）//////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type" => "button",
                    "name"        => "btn_lefts",
                    "value"       => "<<",
//                    "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('left');\"" ) );
                    "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move('sel_add_all','SCHOOL_SELECTED','SCHOOL_NAME',1);\"" ) );

$arg["button"]["btn_lefts"] = $objForm->ge("btn_lefts");


//対象選択ボタンを作成する（一部）//////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type" => "button",
                    "name"        => "btn_right1",
                    "value"       => "＞",
//                    "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('right');\"" ) );
                    "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move('right','SCHOOL_SELECTED','SCHOOL_NAME',1);\"" ) );

$arg["button"]["btn_right1"] = $objForm->ge("btn_right1");


//対象取消ボタンを作成する（一部）//////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type" => "button",
                    "name"        => "btn_left1",
                    "value"       => "＜",
//                    "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('left');\"" ) );
                    "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move('left','SCHOOL_SELECTED','SCHOOL_NAME',1);\"" ) );

$arg["button"]["btn_left1"] = $objForm->ge("btn_left1");


//開始位置（行）コンボボックスを作成する///////////////////////////////////////////////////////////////////////
$row = array(array('label' => "１行",'value' => 1),
			array('label' => "２行",'value' => 2),
			array('label' => "３行",'value' => 3),
			array('label' => "４行",'value' => 4),
			array('label' => "５行",'value' => 5),
			array('label' => "６行",'value' => 6),
//			array('label' => "７行",'value' => 7),
			);

$objForm->ae( array("type"       => "select",
                    "name"       => "POROW",
                    "size"       => "1",
                    "value"      => $model->field["POROW"],
                    "options"    => isset($row)?$row:array()));

$arg["data"]["POROW"] = $objForm->ge("POROW");


//開始位置（列）コンボボックスを作成する////////////////////////////////////////////////////////////////////////
$col = array(array('label' => "１列",'value' => 1),
			array('label' => "２列",'value' => 2),
			array('label' => "３列",'value' => 3),
			);


$objForm->ae( array("type"       => "select",
                    "name"       => "POCOL",
                    "size"       => "1",
                    "value"      => $model->field["POCOL"],
                    "options"    => isset($col)?$col:array()));

$arg["data"]["POCOL"] = $objForm->ge("POCOL");


//印刷ボタンを作成する///////////////////////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type" => "button",
                    "name"        => "btn_print",
                    "value"       => "プレビュー／印刷",
                    "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) );

$arg["button"]["btn_print"] = $objForm->ge("btn_print");


//終了ボタンを作成する//////////////////////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type" => "button",
                    "name"        => "btn_end",
                    "value"       => "終 了",
                    "extrahtml"   => "onclick=\"closeWin();\"" ) );

$arg["button"]["btn_end"] = $objForm->ge("btn_end");


//hiddenを作成する(必須)/////////////////////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type"      => "hidden",
                    "name"      => "DBNAME",
                    "value"      => DB_DATABASE
                    ) );


$objForm->ae( array("type"      => "hidden",
                    "name"      => "PRGID",
                    "value"     => "KNJL255C"
                    ) );


$objForm->ae( array("type"      => "hidden",
                    "name"      => "YEAR",
                    "value"      => $model->control["年度"] + 1,
                    ) );

$arg["data"]["YEAR"] = $model->control["年度"] + 1;

$objForm->ae( array("type"      => "hidden",
                    "name"      => "GAKKI",
                    "value"      => $model->control["学期"],
                    ) );


$objForm->ae( array("type"      => "hidden",
                    "name"      => "cmd"
                    ) );

    $objForm->ae( array("type"      => "hidden",
                        "name"      => "selectdata"
                        ) );  

knjCreateHidden($objForm, "useAddrField2" , $model->Properties["useAddrField2"]);

//フォーム終わり
$arg["finish"]  = $objForm->get_finish();


//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
View::toHTML($model, "knjl255cForm1.html", $arg); 
}
}
?>
