<?php

require_once('for_php7.php');


class knjc180tForm1
{
    function main(&$model){

$objForm = new form;
//フォーム作成
$arg["start"] = $objForm->get_start("knjc180tForm1", "POST", "knjc180tindex.php", "", "knjc180tForm1");

//年度設定
$arg["data"]["YEAR"] = CTRL_YEAR;

$db = Query::dbCheckOut();

//処理月(各学期の期間の月のみをコンボにセット
$result = $db->query(knjc180tQuery::getSemesterMonth());
while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
{
    for ($i = 4; $i < 16; $i++) 
    {   
        $mon = ($i<13) ? $i : ($i-12);
        
        if ($mon < 4) {
            $year = CTRL_YEAR + 1;
        } else {
            $year = CTRL_YEAR;
        }

        //年と月を合わせて比較する    
        if ((int)($year.sprintf("%02d",$mon)) >= (int)strftime("%Y%m",strtotime($row["SDATE"])) 
        && ((int)$year.sprintf("%02d",$mon)) <= (int)strftime("%Y%m",strtotime($row["EDATE"]))) 
        {
            //月が学期の開始月または終了月かチェック
            //開始月の場合は開始日以降その月末日まで集計
            //開始月の場合は開始日以降翌月の１日まで集計
            if ($mon == (int)strftime("%m",strtotime($row["SDATE"]))) {

                $flg = "1";
            
            //終了月の場合はその月の１日から終了日まで集計
            //終了月の場合はその月の２日から終了日まで集計
            } else if ($mon == (int)strftime("%m",strtotime($row["EDATE"]))) {
            
                $flg = "2";
            
            //それ以外はその月の１日から月末日まで集計
            //それ以外はその月の２日から翌月の１日まで集計
            } else {
            
                $flg = "0";
            }
            
            //初期値(学籍処理日の月にする）
            if ($model->month == "") {
                if ($mon == strftime("%m", strtotime(CTRL_DATE))) {
                    $model->month = $row["SEMESTER"]."-".sprintf("%02d",$mon)."-".$flg;
                }
            }
                
            $opt[] = array("label"    =>$mon."月 ( ".$row["SEMESTERNAME"]." )",
                           "value"    => $row["SEMESTER"]."-".sprintf("%02d",$mon)."-".$flg);              
        }
    }
}
$result->free();
Query::dbCheckIn($db);

$objForm->ae( array("type"        => "select",
                    "name"        => "month",
                    "size"        => "1",
                    "extrahtml"   => "STYLE=\"WIDTH:120\" onChange=\"return btn_submit('knjc180t');\"",
                    "value"       => $model->month,
                    "options"     => $opt));

$arg["data"]["MONTH"] = $objForm->ge("month");

//学年コンボボックス作成
$db = Query::dbCheckOut();
$query = knjc180tQuery::getSelectGrade($model);
$result = $db->query($query);
while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
    $row1[]= array('label'  => $row["LABEL"],
                    'value' => $row["VALUE"]);
}
$result->free();
Query::dbCheckIn($db);

if($model->field["GRADE"] == "") {
    $model->field["GRADE"] = $row1[0]["value"];
}
$objForm->ae( array("type"       => "select",
                    "name"       => "GRADE",
                    "size"       => "1",
                    "value"      => $model->field["GRADE"],
                    "extrahtml"   => " onChange=\"return btn_submit('knjc180t');\"",
                    "options"    => isset($row1)?$row1:array())); 

$arg["data"]["GRADE"] = $objForm->ge("GRADE");

//クラス一覧リスト作成
$db = Query::dbCheckOut();
$opt_class_left = $opt_class_right = array();
$query = knjc180tQuery::getSelectClass($model);
$result = $db->query($query);

while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
    $opt_class_right[]= array('label' => $row["LABEL"],
							  'value' => $row["VALUE"]);
}
$result->free();
Query::dbCheckIn($db);

$objForm->ae( array("type"       => "select",
                    "name"       => "CLASS_NAME",
					"extrahtml"  => "multiple style=\"width=180px\" width=\"180px\" ondblclick=\"move1('left')\"",
                    "size"       => "20",
                    "options"    => $opt_class_right));

$arg["data"]["CLASS_NAME"] = $objForm->ge("CLASS_NAME");

//出力対象クラスリストを作成する
$objForm->ae( array("type"       => "select",
                    "name"       => "CLASS_SELECTED",
					"extrahtml"  => "multiple style=\"width=180px\" width=\"180px\" ondblclick=\"move1('right')\"",
                    "size"       => "20",
                    "options"    => $opt_class_left));

$arg["data"]["CLASS_SELECTED"] = $objForm->ge("CLASS_SELECTED");


//対象選択ボタンを作成する（全部）
$objForm->ae( array("type" => "button",
                    "name"        => "btn_rights",
                    "value"       => ">>",
                    "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('right');\"" ) );

$arg["button"]["btn_rights"] = $objForm->ge("btn_rights");


//対象取消ボタンを作成する（全部）
$objForm->ae( array("type" => "button",
                    "name"        => "btn_lefts",
                    "value"       => "<<",
                    "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('left');\"" ) );

$arg["button"]["btn_lefts"] = $objForm->ge("btn_lefts");


//対象選択ボタンを作成する（一部）
$objForm->ae( array("type" => "button",
                    "name"        => "btn_right1",
                    "value"       => "＞",
                    "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('right');\"" ) );

$arg["button"]["btn_right1"] = $objForm->ge("btn_right1");


//対象取消ボタンを作成する（一部）
$objForm->ae( array("type" => "button",
                    "name"        => "btn_left1",
                    "value"       => "＜",
                    "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('left');\"" ) );

$arg["button"]["btn_left1"] = $objForm->ge("btn_left1");

//印刷ボタンを作成する
$objForm->ae( array("type" => "button",
                    "name"        => "btn_print",
                    "value"       => "プレビュー／印刷",
                    "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) );

$arg["button"]["btn_print"] = $objForm->ge("btn_print");

//終了ボタンを作成する
$objForm->ae( array("type" => "button",
                    "name"        => "btn_end",
                    "value"       => "終 了",
                    "extrahtml"   => "onclick=\"closeWin();\"" ) );

$arg["button"]["btn_end"] = $objForm->ge("btn_end");

//hiddenを作成する
$objForm->ae( array("type"      => "hidden",
                    "name"      => "DBNAME",
                    "value"      => DB_DATABASE
                    ) );

$objForm->ae( array("type"      => "hidden",
                    "name"      => "PRGID",
                    "value"      => "KNJC180T"
                    ) );

$objForm->ae( array("type"      => "hidden",
                    "name"      => "YEAR",
                    "value"      => CTRL_YEAR
                    ) );

$objForm->ae( array("type"      => "hidden",
                    "name"      => "SEME",
                    "value"      => substr($model->month,0,1)
                    ) );

$objForm->ae( array("type"      => "hidden",
                    "name"      => "TUKI",
                    "value"      => substr($model->month,2,2)
                    ) );

$objForm->ae( array("type"      => "hidden",
                    "name"      => "FLG",
                    "value"      => substr($model->month,5,1)
                    ) );

$objForm->ae( array("type"      => "hidden",
                    "name"      => "cmd"
                    ) );

$objForm->ae( array("type"      => "hidden",
                    "name"      => "selectdata") );  


$arg["finish"]  = $objForm->get_finish();
//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
View::toHTML($model, "knjc180tForm1.html", $arg); 
}
}
?>
