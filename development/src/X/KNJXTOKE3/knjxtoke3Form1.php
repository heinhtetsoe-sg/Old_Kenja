<?php

require_once('for_php7.php');

class knjxtoke3form1
{
    function main(&$model){
        $objForm = new form;
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjxtoke3form1", "POST", "knjxtoke3index.php", "", "knjxtoke3");
        if (isset($model->classcd)){ 
            $db = Query::dbCheckOut();
            $query = knjxtoke3Query::SQLGet_Main($model);
        
            //教科、科目、クラス取得
            $i=0;
            $result = $db->query($query);
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $i++;
                if($i==1) {
                $checked = 1;
                }
                else {
                $checked = (is_array($model->subclasscd) && in_array($row["SUBCLASSCD"], $model->subclasscd))? true:false;
                }
                if($checked) {
                    $objForm->add_element(array("type"      => "radio",
                                                "name"      => "chk",
                                                "value"     => $row["SUBCLASSCD"]));
                }
                else {
                $objForm->add_element(array("type"       => "radio",
                                             "name"      => "chk"));
                }
                $row["TARGETCLASS"] = common::PubFncData2Print($row["TARGETCLASS"]);
                $row["CHECK"] = $objForm->ge("chk",$row["SUBCLASSCD"]);
                
                
                $row["BGCOLOR"] = "#ffffff";
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
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "TESTKINDCD",
                            "value"     => $model->testkindcd,
                            ) );
        
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "TESTITEMCD",
                            "value"     => $model->testitemcd,
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
        View::toHTML($model, "knjxtoke3Form1.html", $arg); 
    }
}
?>
