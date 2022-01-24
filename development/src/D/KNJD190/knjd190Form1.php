<?php

require_once('for_php7.php');


class knjd190Form1
{
    function main(&$model){

$objForm = new form;

$arg = array();

//フォーム作成
$arg["start"]   = $objForm->get_start("main", "POST", "knjd190index.php", "", "main");


//読込ボタンを作成する
$objForm->ae( array("type" => "button",
                    "name"        => "btn_toukei",
                    "value"       => "･･･",
                    "extrahtml"   => "onclick=\"wopen('../../X/KNJXTOKE3/knjxtoke3index.php?DISP=CLASS&PROGRAMID=$model->programid','KNJXTOKE3',0,0,900,550);\"") );
    
$arg["explore"] = $objForm->ge("btn_toukei");

//学習記録エクスプローラー
if($model->cmd != "toukei") {
    $arg["ONLOAD"] = "wopen('../../X/KNJXTOKE3/knjxtoke3index.php?DISP=CLASS&PROGRAMID=$model->programid','KNJXTOKE3',0,0,900,550);";
}

//$cd =& $model->attendclasscd;
//$cd_name = "ATTENDCLASSCD";

$cd =& $model->subclasscd;
if (isset($cd)){ 
    $db = Query::dbCheckOut();
    $query = knjd190Query::SQLGet_Main($model);

    $i=0;
    //教科、科目、クラス取得
    $result = $db->query($query);
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $title = "[" . $row["CLASSCD"]."　".htmlspecialchars($row["CLASSNAME"]) ."]";

        $subclasscd = $row["SUBCLASSCD"];

        $checked = (is_array($model->checked_attend) && in_array($row["ATTENDCLASSCD"], $model->checked_attend))? true:false;
        if($checked==0) {
            $objForm->add_element(array("type"      => "checkbox",
    	                                "name"     => "chk",
    	                                "checked"  => $checked,
    	                                "value"    => $row["SUBCLASSCD"].",".$row["ATTENDCLASSCD"].",".$row["GROUPCD"],
                                        "extrahtml"   => "multiple" ));

            $row["CHECK"] = $objForm->ge("chk");

            $start = str_replace("-","/",$row["STARTDAY"]);
            $end = str_replace("-","/",$row["ENDDAY"]);
        //学籍処理範囲外の場合背景色を変える
            if ((strtotime($model->control["学籍処理日"]) < strtotime($start)) ||
                (strtotime($model->control["学籍処理日"]) > strtotime($end))) {
                $row["BGCOLOR"] = "#ccffcc";
            } else {
                $row["BGCOLOR"] = "#ffffff";
            }
            $row["TERM"] = $start ."～" .$end;
//2004/06/30 nakamoto-------------------------------------
			if($row["CHARGEDIV"] == 1) {
				$row["CHARGEDIV"] = ' ＊';
			}
			else {
				$row["CHARGEDIV"] = ' ';
			}
            $arg["data"][] = $row; 
        }
        $i++;
        if($i==1) {
            $arg["data1"][] = $row; 
        }
    }
    Query::dbCheckIn($db);
}
$objForm->add_element(array("type"      => "checkbox",
                            "name"      => "chk_all",
                            "extrahtml"   => "onClick=\"return check_all();\"" ));  

$arg["CHECK_ALL"] = $objForm->ge("chk_all");


//ラジオボタンを作成する
//$objForm->add_element(array("type"       => "radio",
//                            "value"      => 1,
//                            "name"      => "OUTPUT"));
//$arg["OUTPUT1"] = $objForm->ge("OUTPUT",1);
//$arg["OUTPUT2"] = $objForm->ge("OUTPUT",2);


//プレビューボタンを作成する
$objForm->ae( array("type" => "button",
                    "name"        => "btn_ok",
                    "value"       => "プレビュー／印刷",
                    "extrahtml"   => "onclick=\"return opener_submit('" . SERVLET_URL . "');\"" ) );

$arg["btn_ok"] = $objForm->ge("btn_ok");


//終了ボタンを作成する
$objForm->ae( array("type" => "button",
                    "name"        => "btn_can",
                    "value"       => " 終 了 ",
                    "extrahtml"   => "onClick=\"closeWin();\"" ));

$arg["btn_can"] = $objForm->ge("btn_can");


//タイトル
$arg["TITLE"] = $title;


//年度・学期（表示）
if (($model->year != "") && ($model->semester != "")) {
    $arg["YEAR_SEMESTER"] = $model->year."年度&nbsp;" .$model->control["学期名"][$model->semester]."&nbsp;";
}


//hiddenを作成する
$objForm->ae( array("type"      => "hidden",
                    "name"      => "DBNAME",
                    "value"      => DB_DATABASE
                    ) );

$objForm->ae( array("type"      => "hidden",
                    "name"      => "PRGID",
                    "value"     => "KNJD190"
                    ) );

$objForm->ae( array("type"      => "hidden",
                    "name"      => "YEAR",
                    "value"     => $model->year,
                    ) );

$objForm->ae( array("type"      => "hidden",
                    "name"      => "SEMESTER",
                    "value"     => $model->semester,
                    ) );

$objForm->ae( array("type"      => "hidden",
                    "name"      => "CLASSCD",
                    "value"     => $model->classcd,
                    ) );

$objForm->ae( array("type"      => "hidden",
                    "name"      => "SUBCLASSCD",
                    "value"     => $subclasscd,
                    ) );

$objForm->ae( array("type"      => "hidden",
                    "name"      => "TESTKINDCD",
                    "value"     => $model->testkindcd,
                    ) );

$objForm->ae( array("type"      => "hidden",
                    "name"      => "TESTITEMCD",
                    "value"     => $model->testitemcd,
                    ) );

$objForm->ae( array("type"      => "hidden",
                    "name"      => "ATTENDCLASSCD"
                    ) );

$objForm->ae( array("type"      => "hidden",
                    "name"      => "GROUPCD"
                    ) );


$objForm->ae( array("type"      => "hidden",
                    "name"      => "DISP"
                    ) );

$objForm->ae( array("type"      => "hidden",
                    "name"      => "cmd"
                    ) );


$arg["finish"]  = $objForm->get_finish();


//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
View::toHTML($model, "knjd190Form1.html", $arg); 
}
}
?>
