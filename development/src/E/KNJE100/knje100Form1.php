<?php

require_once('for_php7.php');

class knje100Form1
{
    function main(&$model)
    {

//権限チェック
if (AUTHORITY != DEF_UPDATABLE){
	$arg["jscript"] = "OnAuthError();";
}

$objForm = new form;

//年度学期表示
$arg["HEADER"] = $model->control["年度"] ."年度　" .$model->control["学期名"][(integer) $model->control["学期"]];
$db = Query::dbCheckOut();

//学年取得
$query = knje100Query::selectGradeQuery($model);
$result = $db->query($query);
$opt = array();
while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
    $opt[] = array("label" => $row["NAMECD2"] ." ". $row["NAME1"],
                   "value" => $row["NAMECD2"]
                   );
    if (!isset($model->field["GRADE"])) $model->field["GRADE"] = $row["NAMECD2"];
}


Query::dbCheckIn($db);


$objForm->ae( array("type"       => "select",
                    "name"       => "GRADE",
                    "size"       => "1",
                    "value"      => $model->field["GRADE"],
                    "extrahtml"  => "onChange=\"return btn_submit('main');\"",
                    "options"    => $opt));

$arg["GRADE"] = $objForm->ge("GRADE");


//ファイルからの取り込み
$objForm->add_element(array("type"      => "file",
                            "name"      => "FILE",
                            "size"      => 1024000,
                            "extrahtml"   => "" ));

$arg["FILE"] = $objForm->ge("FILE");


//CSV取込みボタンを作成する
$objForm->ae( array("type" => "button",
                    "name"        => "btn_exec",
                    "value"       => " 実行 ",
                    "extrahtml"   => "onclick=\"return btn_submit('exec');\"" ));

$arg["btn_exec"] = $objForm->ge("btn_exec");


//終了ボタンを作成する
$objForm->ae( array("type" => "button",
                    "name"        => "btn_end",
                    "value"       => "終 了",
                    "extrahtml"   => "onclick=\"closeWin();\"" ) );

$arg["btn_end"] = $objForm->ge("btn_end");


//hiddenを作成する
$objForm->ae( array("type"      => "hidden",
                    "name"      => "cmd"
                    ) );


//フォーム作成
$arg["start"]   = $objForm->get_start("main", "POST", "knje100index.php", "", "main");
$arg["finish"]  = $objForm->get_finish();


//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
View::toHTML($model, "knje100Form1.html", $arg);

    }
}
?>
