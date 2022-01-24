<?php

require_once('for_php7.php');


class knjd270Form1
{
    function main(&$model){

$objForm = new form;

$arg = array();

//フォーム作成
$arg["start"]   = $objForm->get_start("knjd270Form1", "POST", "knjd270index.php", "", "knjd270Form1");


//読込ボタンを作成する
$objForm->ae( array("type" => "button",
                    "name"        => "btn_toukei",
                    "value"       => "･･･",
                    "extrahtml"   => "onclick=\"wopen('../../X/KNJXTOKE2/knjxtoke2index.php?DISP=TEST&PROGRAMID=$model->programid','KNJXTOKE2',0,0,900,550);\"") );
    
$arg["explore"] = $objForm->ge("btn_toukei");

if($model->cmd != "toukei") {
    $arg["ONLOAD"] = "wopen('../../X/KNJXTOKE2/knjxtoke2index.php?DISP=TEST&PROGRAMID=$model->programid','KNJXTOKE2',0,0,900,550);";
}

$cd =& $model->attendclasscd;

if (isset($cd)){ 
    $db = Query::dbCheckOut();
    $query = knjd270Query::SQLGet_Main($model);

    //教科、科目、クラス取得
    $result = $db->query($query);
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $title = "[" . $row["CLASSCD"]."　". htmlspecialchars($row["CLASSNAME"]) ."]-[" .$row["SUBCLASSCD"]."　".htmlspecialchars($row["SUBCLASSNAME"]) ."]";

        $subclasscd = $row["SUBCLASSCD"];


        $checked = (is_array($model->checked_attend) && in_array($row["ATTENDCLASSCD"], $model->checked_attend))? true:false;
//2004-07-30 naka
        $checked2 = (is_array($model->checked_staff) && in_array($row["STAFFCD"], $model->checked_staff))? true:false;
//2004/06/30 nakamoto-------------------------------------
			if($row["CHARGEDIV"] == 1) {
				$row["CHARGEDIV"] = ' ＊';
			}
			else {
				$row["CHARGEDIV"] = ' ';
			}
        if($checked==0 || $checked2==0) {	//2004-07-30 naka
            $objForm->add_element(array("type"      => "checkbox",
    	                                "name"     => "chk",
    	                                //"checked"  => $checked,	2004-07-30 naka
    	                                "checked"  => false,
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
            $arg["data"][] = $row; 
        } else {
    	    $objForm->add_element(array("type"      => "checkbox",
    	                                "name"     => "chk1",
    	                                "checked"  => $checked,
    	                                "value"    => $row["SUBCLASSCD"].",".$row["ATTENDCLASSCD"].",".$row["GROUPCD"],
                                        "extrahtml"   => "disabled" ));
            $row["CHECK"] = $objForm->ge("chk1");
            $row["TERM"] = str_replace("-","/",$row["STARTDAY"]) ."～" .str_replace("-","/",$row["ENDDAY"]);
            $arg["data1"][] = $row; 
        }
    }
    Query::dbCheckIn($db);
}
$objForm->add_element(array("type"      => "checkbox",
                            "name"      => "chk_all",
                            "extrahtml"   => "onClick=\"return check_all();\"" )); 

$arg["CHECK_ALL"] = $objForm->ge("chk_all");

$opt1 = array();

if (isset($arg["data1"][0]["SUBCLASSCD"])){ 
    $db = Query::dbCheckOut();
    $query = "SELECT DISTINCT ".
             "    T1.SUBCLASSCD AS VALUE,".
    		 "    T1.SUBCLASSCD || '　' || T1.SUBCLASSNAME AS LABEL ".
             "FROM ".
    		 "    subclass_mst T1,".
             "    kin_record_dat T2 ".      //近大用成績データ  04/07/23  yamauchi
//             "    recordsemes_dat T2 ".      //成績期末データ
             "WHERE ".
    		 "    T2.SUBCLASSCD='". $arg["data1"][0]["SUBCLASSCD"]. "' AND ".
    		 "    T2.YEAR='". $model->year. "' AND ".
//    		 "    T2.SEMESTER='". $model->semester. "' AND ".
//    		 "    T1.SUBCLASSCD=T2.GRADINGCLASSCD ";
    		 "    T1.SUBCLASSCD=T2.SUBCLASSCD ";		//  04/07/23  yamauchi


    $result = $db->query($query);
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt1[]= array('label' =>  $row["LABEL"],
                        'value' => $row["VALUE"]);
    }
    $result->free();
    Query::dbCheckIn($db);


}

$objForm->ae( array("type"       => "select",
                    "name"       => "GRADINGCLASSCD",
                    "size"       => "1",
                    "options"    => $opt1 ) );

$arg["GRADINGCLASSCD"] = $objForm->ge("GRADINGCLASSCD");

//ラジオボタンを作成する
$objForm->add_element(array("type"       => "radio",
                            "value"      => 1,
                            "name"      => "OUTPUT"));

$arg["OUTPUT1"] = $objForm->ge("OUTPUT",1);
$arg["OUTPUT2"] = $objForm->ge("OUTPUT",2);


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
                    "value"     => "KNJD270"
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
//2004-07-30 naka
$objForm->ae( array("type"      => "hidden",
                    "name"      => "STAFF_CD"
                    ) );

$objForm->ae( array("type"      => "hidden",
                    "name"      => "ATTENDCLASSCD1"
                    ) );

$objForm->ae( array("type"      => "hidden",
                    "name"      => "ATTENDCLASSCD2"
                    ) );

$objForm->ae( array("type"      => "hidden",
                    "name"      => "GROUPCD"
                    ) );

$objForm->ae( array("type"      => "hidden",
                    "name"      => "TAI"
                    ) );

$objForm->ae( array("type"      => "hidden",
                    "name"      => "TOKE"
                    ) );

$objForm->ae( array("type"      => "hidden",
                    "name"      => "DISP"
                    ) );

$objForm->ae( array("type"      => "hidden",
                    "name"      => "cmd"
                    ) );




$arg["finish"]  = $objForm->get_finish();


//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
View::toHTML($model, "knjd270Form1.html", $arg); 
}
}
?>
