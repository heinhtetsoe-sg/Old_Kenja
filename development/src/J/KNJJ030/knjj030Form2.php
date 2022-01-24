<?php

require_once('for_php7.php');

/********************************************************************/
/* 変更履歴                                                         */
/* ･NO001：開始日の判定を追加                       山城 2004/11/17 */
/* ･NO002：登録更新後のデータ初期化処理修正         山城 2004/11/17 */
/********************************************************************/

class knjj030Form2
{
    function main(&$model){

$objForm = new form;
//フォーム作成
$arg["start"]   = $objForm->get_start("edit", "POST", "knjj030index.php", "", "edit");

//警告メッセージを表示しない場合
if (isset($model->schregno) && isset($model->clubcd) && isset($model->enterdate) && !isset($model->warning)){
    $Row = knjj030Query::getRowSdate($model, $model->schregno,$model->clubcd,$model->enterdate);   /* NO001 */
    $temp_cd = $Row["SCHREGNO"];
}else{
    $Row =& $model->field;
}

//入部日付////////////////////////////////////////////////////////////////////////////////
$arg["data"]["SDATE"]=View::popUpCalendar($objForm, "SDATE", str_replace("-","/",$Row["SDATE"]),"");


//退部日付////////////////////////////////////////////////////////////////////////////////
$arg["data"]["EDATE"]=View::popUpCalendar($objForm, "EDATE", str_replace("-","/",$Row["EDATE"]),"");


//役職区分コンボボックスの中身を作成///////////////////////////////////////////////////////////////////////////////
$db     = Query::dbCheckOut();      //dbCheckOut

$query  = knjj030Query::getName_Data($model->control_data["年度"]);
$result = $db->query($query);
$opt_rolecd = array();
$opt_rolecd[0] = array("label" => "",       "value" => "");
while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
    $opt_rolecd[] = array("label" => htmlspecialchars($row["NAME1"]),
                          "value" => $row["NAMECD2"]);
}

$objForm->ae( array("type"        => "select",
                    "name"        => "EXECUTIVECD",
                    "size"        => 1,
                    "value"       => $Row["EXECUTIVECD"],
                    "options"     => $opt_rolecd
                    ));

$arg["data"]["EXECUTIVECD"] = $objForm->ge("EXECUTIVECD");


//部クラブコードコンボボックスの中身を作成///////////////////////////////////////////////////////////////////////////////
$query  = knjj030Query::getClub_Data($model, $model->control_data["年度"]);
$result = $db->query($query);
$opt_clubcd = array();
$opt_clubcd[0] = array("label" => "",       "value" => "");
while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
    $opt_clubcd[] = array("label" => htmlspecialchars($row["CLUBNAME"]),
                          "value" => $row["CLUBCD"]);
}

$result->free();
Query::dbCheckIn($db);      //dbCheckIn

$objForm->ae( array("type"        => "select",
                    "name"        => "CLUBCD",
                    "size"        => 1,
                    "value"       => $Row["CLUBCD"],
                    "options"     => $opt_clubcd
                    ));

$arg["data"]["CLUBCD"] = $objForm->ge("CLUBCD");


//備考/////////////////////////////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type"        => "text",
                    "name"        => "REMARK",
                    "size"        => 40,
                    "maxlength"   => 40,
                    "value"       => $Row["REMARK"] ));

$arg["data"]["REMARK"] = $objForm->ge("REMARK");


//ボタン/////////////////////////////////////////////////////////////////////////////////////////////////////
//追加ボタンを作成する
$objForm->ae( array("type"        => "button",
                    "name"        => "btn_add",
                    "value"       => "登 録",
                    "extrahtml"   => "onclick=\"return btn_submit('add');\"" ) );

$arg["button"]["btn_add"] = $objForm->ge("btn_add");


//修正ボタンを作成する
$objForm->ae( array("type"        => "button",
                    "name"        => "btn_update",
                    "value"       => "更 新",
                    "extrahtml"   => "onclick=\"return btn_submit('update');\"" ) );

$arg["button"]["btn_update"] = $objForm->ge("btn_update");


//削除ボタンを作成する
$objForm->ae( array("type"        => "button",
                    "name"        => "btn_del",
                    "value"       => "削 除",
                    "extrahtml"   => "onclick=\"return btn_submit('delete');\"" ) );

$arg["button"]["btn_del"] = $objForm->ge("btn_del");


//クリアボタンを作成する
$objForm->ae( array("type"        => "reset",
                    "name"        => "btn_reset",
                    "value"       => "取 消",
                    "extrahtml"   => "onclick=\"return Btn_reset('edit');\"" ) );

$arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

//終了ボタンを作成する
$objForm->ae( array("type"        => "button",
                    "name"        => "btn_end",
                    "value"       => "終 了",
                    "extrahtml"   => "onclick=\"closeWin();\"" ) );
                    
$arg["button"]["btn_end"] = $objForm->ge("btn_end");

//記録詳細入力ボタン
$extra = ($model->schregno) ? "onclick=\" wopen('".REQUESTROOT."/X/KNJXCLUB_DETAIL/knjxclub_detailindex.php?PROGRAMID=".PROGRAMID."&SCHREGNO=".$model->schregno."&SEND_schKind={$model->schKind}','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"" : "disabled";
$arg["button"]["btn_detail"] = KnjCreateBtn($objForm, "btn_detail", "記録備考入力", $extra);

//ＣＳＶ処理ボタン
$extra = "onClick=\" wopen('".REQUESTROOT."/X/KNJX_J030/knjx_j030index.php?PROGRAMID=".PROGRAMID."&SEND_PRGID=KNJJ030&SEND_AUTH=".AUTHORITY."&SEND_selectSchoolKind={$model->selectSchoolKind}','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
$arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ処理", $extra);

//hiddenを作成する
$objForm->ae( array("type"      => "hidden",
                    "name"      => "cmd"
                    ) );

//hiddenを作成する
$objForm->ae( array("type"      => "hidden",
                    "name"      => "UPDATED",
                    "value"     => $Row["UPDATED"]
                    ) );
                   
$objForm->ae( array("type"      => "hidden",
                    "name"      => "SCHREGNO",
                    "value"     => $model->schregno
                    ) );      
if ($temp_cd=="") $temp_cd = $model->field["temp_cd"];

$objForm->ae( array("type"      => "hidden",
                    "name"      => "temp_cd",
                    "value"     => $temp_cd
                    ) ); 
                                      
$cd_change = false;                                                                               
if ($temp_cd==$Row["SCHREGNO"] ) $cd_change = true;

$arg["finish"]  = $objForm->get_finish();
if (VARS::get("cmd") != "edit" && VARS::get("cmd") != "clear" && ($cd_change==true || $model->isload != 1) && !isset($model->warning)){
    $arg["reload"]  = "window.open('knjj030index.php?cmd=list&SCHREGNO=$model->schregno','right_frame');";/* NO002 */
}

//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
View::toHTML($model, "knjj030Form2.html", $arg); 
}
}
?>
