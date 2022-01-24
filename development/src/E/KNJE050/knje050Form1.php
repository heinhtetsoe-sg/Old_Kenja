<?php

require_once('for_php7.php');


class knje050Form1
{
    function main(&$model)
    {

        $arg = array();
        //権限チェック
        if (common::SecurityCheck(STAFFCD,PROGRAMID) != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("left", "POST", "knje050index.php", "", "left");

        //年度学期表示
        $db = Query::dbCheckOut();
        $result = $db->query(knje050Query::getThisSemester(CTRL_YEAR, CTRL_SEMESTER));
        $row = $result->fetchRow(DB_FETCHMODE_ASSOC);
        $arg["HEADER"] = CTRL_YEAR ."年度" .$row["SEMESTERNAME"];
        $result->free();
        $db = Query::dbCheckOut();

        //学年取得
        $query = knje050Query::selectGradeQuery($model);
        $result = $db->query($query);
        $opt = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt[] = array("label" => $row["GRADE"] ."学年",
                           "value" => $row["GRADE"]
                           );
            if (!isset($model->field["GRADE"])){
                $model->field["GRADE"] = $row["GRADE"];
            }
        }
        $objForm->ae( array("type"       => "select",
                            "name"       => "GRADE",
                            "size"       => "1",
                            "value"      => $model->field["GRADE"],
                            "extrahtml"   => "onChange=\"return btn_submit('left')\"",
                            "options"    => $opt));

        $arg["GRADE"] = $objForm->ge("GRADE");

        $query = knje050Query::selectLeftQuery($model);    
        $result = $db->query($query);
        $arg["ROWSPAN"] = $result->numRows();
        $arg["GRADE2"] = $model->field["GRADE"];
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){

            $row["NAME1"] = View::alink("knje050index.php",$row["COURSECODE"]."　".$row["NAME"], "target=\"right_frame\"", 
                                      array("cmd"         =>"main",
                                            "GRADE"       =>$row["GRADE"],
                                            "COURSECODE"  =>$row["COURSECODE"],
                                            "COURSENAME"  =>$row["NAME"]
                                      ));
            if (!isset($model->field["COURSECODE"]) && !isset($model->field["COURSENAME"])){
                $model->field["COURSECODE"] = $row["COURSECODE"];
                $model->field["COURSENAME"] = $row["NAME"];
            }
            $arg["data"][] = $row;
        }
        Query::dbCheckIn($db);

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd",
                            "value"      => $model->cmd
                            ) );

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knje050Form1.html", $arg); 

    }
}
?>