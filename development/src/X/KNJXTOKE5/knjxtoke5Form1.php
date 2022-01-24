<?php

require_once('for_php7.php');

class knjxtoke5form1
{
    function main(&$model){

        $objForm = new form;
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjxtoke5form1", "POST", "knjaxtoke5index.php", "", "knjaxtoke5");
        if (isset($model->attendclasscd) || isset($model->subclasscd)){ 
            $db = Query::dbCheckOut();
            $query = knjxtoke5Query::SQLGet_Main($model);

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
                                                "value"     => $row["SUBCLASSCD"].",".$row["ATTENDCLASSCD"].",".$row["GROUPCD"].",".$row["STAFFCD"].",".$row["APPDATE"]));	//2004-08-11 naka
                }
                else {
                $objForm->add_element(array("type"       => "radio",
                                             "name"      => "chk"));
                }

//                $row["TARGETCLASS"] = common::PubFncData2Print($row["TARGETCLASS"]);
                $row["CHECK"] = $objForm->ge("chk",$row["SUBCLASSCD"].",".$row["ATTENDCLASSCD"].",".$row["GROUPCD"].",".$row["STAFFCD"].",".$row["APPDATE"]);	//2004-08-11 naka
        
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
	            $row["APPDATE"] = str_replace("-","/",$row["APPDATE"]);			//2004-08-11 naka
                $arg["data"][] = $row; 
            }
            Query::dbCheckIn($db);
        }
        $objForm->add_element(array("type"      => "checkbox",
                                    "name"      => "chk_all",
                                    "extrahtml"   => "disabled" ));

        $arg["CHECK_ALL"] = $objForm->ge("chk_all");

        //ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_ok",
                            "value"       => " 選 択 ",
                            "extrahtml"   => "onClick=\"return opener_submit($model->semester);\"" ));

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
                            "name"      => "cmd"
                            ) );

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PRGID",
                            "value"     => $model->programid
                            ) );

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjxtoke5Form1.html", $arg); 
    }

}
?>
