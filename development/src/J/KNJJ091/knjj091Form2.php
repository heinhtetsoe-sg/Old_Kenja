<?php

require_once('for_php7.php');

class knjj091Form2 {
    function main(&$model) {

$objForm = new form;
//フォーム作成
$arg["start"]   = $objForm->get_start("edit", "POST", "knjj091index.php", "", "edit");
$db = Query::dbCheckOut();


//警告メッセージを表示しない場合
if ($model->cmd == 'edit2') {
    $Row =& $model->field2;
} elseif (isset($model->schregno) && $model->index!="" && !isset($model->warning)) {
    //対象レコードを取得
    $model->org_data = $Row = knjj091Query::getRow($model,$model->index, $db);
    $temp_cd = $Row["SCHREGNO"];
    $seqcd   = $Row["SEQ"];
} else {
    $Row =& $model->field;
}

//学期
$query = knjj091Query::getSemester();
$opt = array();
$value = $Row["SEMESTER"];
$value_flg = false;
$result = $db->query($query);
while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
    $opt[] = array('label' => $row["LABEL"],
                   'value' => $row["VALUE"]);
    if ($value == $row["VALUE"]) $value_flg = true;
}
$value = ($value && $value_flg) ? $value : $opt[0]["value"];
$extra = "";
$arg["data"]["SEMESTER"] = knjCreateCombo($objForm, "SEMESTER", $value, $opt, $extra, 1);
$Row["SEMESTER"] = $value;


//委員会区分
$opt = array();
$query = knjj091Query::getCommitteeFlg();
$value = $Row["COMMITTEE_FLG"];
$value_flg = false;
$result = $db->query($query);
while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
    $opt[] = array('label' => $row["LABEL"],
                   'value' => $row["VALUE"]);
    if ($value == $row["VALUE"]) $value_flg = true;
}
$value = ($value && $value_flg) ? $value : $opt[0]["value"];
$extra = "onChange=\"btn_submit('edit2')\"";
$arg["data"]["COMMITTEE_FLG"] = knjCreateCombo($objForm, "COMMITTEE_FLG", $value, $opt, $extra, 1);
$Row["COMMITTEE_FLG"] = $value;



//委員会
$opt_commicd    = array();
$opt_commicd[0] = array("label" => "", "value" => "");
$query  = knjj091Query::getCommiMst($model, $model->control_data["年度"], $Row["COMMITTEE_FLG"]);
$result = $db->query($query);
while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
    $opt_commicd[] = array("label" => htmlspecialchars($row["COMMITTEECD"]) . "　" . htmlspecialchars($row["COMMITTEENAME"]),
                           "value" => $row["COMMITTEECD"]);
}
$result->free();

//役職
$opt_rolecd    = array();
$opt_rolecd[0] = array("label" => "", "value" => "");
$query  = knjj091Query::getNameMst("J002",$model->control_data["年度"]);
$result = $db->query($query);
while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
    $opt_rolecd[] = array("label" => htmlspecialchars($row["NAMECD2"]) . "　" . htmlspecialchars($row["NAME1"]),
                          "value" => $row["NAMECD2"]);
}
$result->free();

//委員会/////////////////////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type"       => "select",
                    "name"       => "COMMITTEECD",
                    "size"       => "1",
                    "value"      => $Row["COMMITTEECD"],
                    "options"    => $opt_commicd));

$arg["data"]["COMMITTEECD"] = $objForm->ge("COMMITTEECD");

//係り名/////////////////////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type"       => "text",
                    "name"       => "CHARGENAME",
                    "size"       => "34",
                    "maxlength"  => "30",
                    "value"      => $Row["CHARGENAME"]) );

$arg["data"]["CHARGENAME"] = $objForm->ge("CHARGENAME");


//役職/////////////////////////////////////////////////////////////////////////////////////////////
$objForm->ae( array("type"       => "select",
                    "name"       => "EXECUTIVECD",
                    "size"       => "1",
                    "value"      => $Row["EXECUTIVECD"],
                    "options"    => $opt_rolecd));

$arg["data"]["EXECUTIVECD"] = $objForm->ge("EXECUTIVECD");


//----------------------------------------------------------------------------------------------------------------

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
                    "extrahtml"   => "onclick=\"return btn_submit('reset');\"" ) );

$arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

//終了ボタンを作成する
$objForm->ae( array("type"        => "button",
                    "name"        => "btn_end",
                    "value"       => "終 了",
                    "extrahtml"   => "onclick=\"closeWin();\"" ) );

$arg["button"]["btn_end"] = $objForm->ge("btn_end");

//記録詳細入力ボタン
$extra = ($model->schregno) ? "onclick=\" wopen('".REQUESTROOT."/X/KNJXCOMMI_DETAIL/knjxcommi_detailindex.php?PROGRAMID=".PROGRAMID."&SCHREGNO=".$model->schregno."&SCHKIND=".$model->schKind."','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"" : "disabled";
$arg["button"]["btn_detail"] = KnjCreateBtn($objForm, "btn_detail", "記録備考入力", $extra);

//一括更新ボタン
$extra = "onClick=\" wopen('".REQUESTROOT."/J/KNJJ091_2/knjj091_2index.php?SEND_selectSchoolKind=".$model->selectSchoolKind."','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
$arg["button"]["btn_batch"] = KnjCreateBtn($objForm, "btn_batch", "一括更新", $extra);

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
if ($seqcd=="") $seqcd = $model->field["SEQPOST"];

$objForm->ae( array("type"      => "hidden",
                    "name"      => "SEQPOST",
                    "value"     => $seqcd
                    ) );

$objForm->ae( array("type"      => "hidden",
                    "name"      => "PRGID",
                    "value"     => "KNJJ091"
                    ) );

if ($temp_cd=="") $temp_cd = $model->field["temp_cd"];
$objForm->ae( array("type"      => "hidden",
                    "name"      => "temp_cd",
                    "value"     => $temp_cd
                    ) );


$cd_change = false;
if ($temp_cd==$Row["SCHREGNO"] ) $cd_change = true;

$arg["finish"]  = $objForm->get_finish();
if (VARS::get("cmd") != "edit" && ($cd_change==true || $model->isload != 1)){
    $arg["reload"]  = "window.open('knjj091index.php?cmd=list&SCHREGNO=$model->schregno','right_frame');";
}

Query::dbCheckIn($db);
//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
View::toHTML($model, "knjj091Form2.html", $arg);
}
}
?>

