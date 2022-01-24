<?php

require_once('for_php7.php');

class knjxtoke2form1
{
    function main(&$model){
        $objForm = new form;
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjxtoke2form1", "POST", "knjxtoke2index.php", "", "knjxtoke2");
        if (isset($model->attendclasscd) || isset($model->subclasscd)){ 
            $db = Query::dbCheckOut();
            $query = knjxtoke2Query::SQLGet_Main($model);
        
            $i=0;
            //教科、科目、クラス取得
            $result = $db->query($query);
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $i++;
                if($i==1) {
                $checked = 1;
                }
                else {
                $checked = (is_array($model->checked_attend) && in_array($row["ATTENDCLASSCD"], $model->checked_attend))? true:false;
                }
                if($checked) {
                    $objForm->add_element(array("type"      => "radio",
                                                "name"      => "chk",
                                                "value"     => $row["SUBCLASSCD"].",".$row["ATTENDCLASSCD"].",".$row["GROUPCD"].",".$row["STAFFCD"]));//2004-07-30 naka
                }
                else {
                $objForm->add_element(array("type"       => "radio",
                                             "name"      => "chk"));
                }
                //$row["TARGETCLASS"] = common::PubFncData2Print($row["TARGETCLASS"]);
                $row["CHECK"] = $objForm->ge("chk",$row["SUBCLASSCD"].",".$row["ATTENDCLASSCD"].",".$row["GROUPCD"].",".$row["STAFFCD"]);//2004-07-30 naka
                
                $start = str_replace("-","/",$row["STARTDAY"]);
                $end = str_replace("-","/",$row["ENDDAY"]);
                //学籍処理範囲外の場合背景色を変える
                if ((strtotime($model->control["学籍処理日"]) < strtotime($start)) ||
                    (strtotime($model->control["学籍処理日"]) > strtotime($end))){
                    $row["BGCOLOR"] = "#ccffcc";
                }else{
                    $row["BGCOLOR"] = "#ffffff";
                }
                $row["TERM"] = $start ."～" .$end;
				if($row["CHARGEDIV"] == 1) {
					$row["CHARGEDIV"] = ' ＊';
				}
				else {
					$row["CHARGEDIV"] = ' ';
				}
                $arg["data"][] = $row; 
            }
            Query::dbCheckIn($db);
        }
        $objForm->add_element(array("type"      => "checkbox",
                                    "name"      => "chk_all",
                                    "extrahtml"   => "disabled" ));
        
        $arg["CHECK_ALL"] = $objForm->ge("chk_all");
        
        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "radiochk",
                            "value"     => $j
                            ) );
        
        //ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_ok",
                            "value"       => " 選 択 ",
                            "extrahtml"   => "onClick=\"return opener_submit();\"" ));
        
        $arg["btn_ok"] = $objForm->ge("btn_ok");
        
        //ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_can",
                            "value"       => "キャンセル",
                            "extrahtml"   => "onClick=\"closeWin();\"" ));
        
        $arg["btn_can"] = $objForm->ge("btn_can");
        
        //タイトル
        $arg["TITLE"] = $model->title;
        
        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "TESTKINDCD",
                            "value"     => $model->testkindcd
                            ) );
        
        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "TESTITEMCD",
                            "value"     => $model->testitemcd
                            ) );
        
        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "DISP",
                            "value"     => $model->disp
                            ) );
        
        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );
        
        $arg["finish"]  = $objForm->get_finish();
        
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjxtoke2Form1.html", $arg); 
    }
}
?>
