<?php

require_once('for_php7.php');

class knje030Form1
{
    function main(&$model)
    {
        //権限チェック
        if(AUTHORITY != DEF_UPDATABLE){
            $arg["close"] = "closing_window();";
        }

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knje030index.php", "", "edit");

        $db = Query::dbCheckOut();

        //コンボボックスの値を作成
        $sqlstu = knje030Query::getTrans_Student(CTRL_YEAR,CTRL_SEMESTER, $model);
        $result = $db->query($sqlstu); 
        $opt = array();
        $i = 0;
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){

            $opt[] = array("label" => $row["HR_NAME"]."　：　".$row["SCHREGNO"]."　".$row["NAME"],
                           "value" => $row["SCHREGNO"]);

            if ($i==0) {
                $namecd_defult = $row["SCHREGNO"];
                $i++;
            }

        }

        //事前チェック(対象年度に転入生が存在するか)
        if(get_count($opt)<1){
            $arg["close"] = " closing_window(1); " ;
        }

        $objForm->ae( array("type"       => "select",
                            "name"       => "SCHREGNO",
                            "size"       => "1",
                            "value"      => $model->knje030schreg,
                            "extrahtml"  => "onchange=\"return btn_submit('list');\"",
                            "options"    => $opt
                            ));

        $arg["top"]["SCHREGNO"] = $objForm->ge("SCHREGNO");

        //表示選択リストの値を作成
        if ($model->Properties["useCurriculumcd"] == '1') {
            $arg["useCurriculumcd"] = "1";
        } else {
            $arg["NoCurriculumcd"] = "1";
        }        
        $sqlcla = knje030Query::getTrans_Class($model,$model->knje030schreg,CTRL_YEAR);
        $result = $db->query($sqlcla);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $arg["data"][]=$row;
        }

        $result->free();
        Query::dbCheckIn($db); 

        if($model->knje030schreg ==""){
            $model->knje030schreg = knje030Query::First_No(CTRL_YEAR,CTRL_SEMESTER, $model);
        }

        //処理年度および処理学期作成
        $arg["top"]["year"] = CTRL_YEAR;
        $arg["top"]["semester"] = CTRL_SEMESTERNAME;

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ));
                            
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "hope"
                            ));


        if ($model->hope == "edit"||$model->cmd == "reset"){
            $arg["reload_edit"]  = "window.open('knje030index.php?cmd=edit&SCHREGNO=$model->knje030schreg','right_frame');";
        } 

        $arg["finish"]  = $objForm->get_finish();


        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knje030Form1.html", $arg); 
    }
}
?>
