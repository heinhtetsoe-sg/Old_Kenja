<?php

require_once('for_php7.php');

class knjx120Form1
{
    function main(&$model)
    {

//権限チェック
if (AUTHORITY != DEF_UPDATABLE){
	$arg["jscript"] = "OnAuthError();";
}

$objForm = new form;


//ラジオボタンを作成する
$opt_shubetsu[0]=1;
$opt_shubetsu[1]=2;
$opt_shubetsu[2]=3;
$opt_shubetsu[3]=4;
$opt_shubetsu[4]=5;
$opt_shubetsu[5]=6;

if($model->field["OUTPUT"]=="") $model->field["OUTPUT"] = "1";

$objForm->ae( array("type"       => "radio",
                    "name"       => "OUTPUT",
					"value"      => $model->field["OUTPUT"],
                    "extrahtml"   => "",
					"multiple"   => $opt_shubetsu));

$arg["data"]["OUTPUT1"] = $objForm->ge("OUTPUT",1);
$arg["data"]["OUTPUT2"] = $objForm->ge("OUTPUT",2);
$arg["data"]["OUTPUT3"] = $objForm->ge("OUTPUT",3);
$arg["data"]["OUTPUT4"] = $objForm->ge("OUTPUT",4);
$arg["data"]["OUTPUT5"] = $objForm->ge("OUTPUT",5);
$arg["data"]["OUTPUT6"] = $objForm->ge("OUTPUT",6);


//ボタンを作成する（ＣＳＶへエラー出力）
$objForm->ae( array("type" 		=> "button",
                    "name"        => "btn_error",
                    "value"       => "CSV出力",
                    "extrahtml"   => "onclick=\"return btn_submit('csv_error');\"" ));

$arg["btn_error"] = $objForm->ge("btn_error");


//終了ボタンを作成する
$objForm->ae( array("type" => "button",
                    "name"        => "btn_end",
                    "value"       => "終了",
                    "extrahtml"   => "onclick=\"closeWin();\"" ) );

$arg["btn_end"] = $objForm->ge("btn_end");


//hiddenを作成する
$objForm->ae( array("type"      => "hidden",
                    "name"      => "cmd"
                    ) );


//フォーム作成
$arg["start"]   = $objForm->get_start("main", "POST", "knjx120index.php", "", "main");
$arg["finish"]  = $objForm->get_finish();


//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
View::toHTML($model, "knjx120Form1.html", $arg);

    }
}
?>
