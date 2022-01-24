<?php

require_once('for_php7.php');


class knjz220Form1{

    function main(&$model){

        $objForm      = new form;
        $arg["start"] = $objForm->get_start("list", "POST", "knjz220index.php", "", "edit");
        $db = Query::dbCheckOut();

        //学年コンボ
        $result = $db->query(knjz220Query::combo_grdQuery($model));
        $opt = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $Gopt[] = array("label" => htmlspecialchars($row["SHOWGRADE"]), "value" => $row["GRADE"]);
        }

        //学年コンボボックス
        $objForm->ae( array("type"      => "select",
                            "name"      => "GRADE",
                            "size"      => "1",
                            "value"     => $model->grade,
                            "extrahtml" => "onChange=\"return btn_submit('list'), Cleaning_window();\"",
                            "options"   => $Gopt));

        $arg["grade_cmb"] = $objForm->ge("GRADE");

        //教科コンボ
        $result = $db->query(knjz220Query::combo_clsQuery($model));
        $opt = array();
        $kindFlg = "";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt[] = array("label" => htmlspecialchars($row["CLASSCD"].'-'.$row["SCHOOL_KIND"]."：".$row["CLASSNAME"]),
                               "value" => $row["CLASSCD"].'-'.$row["SCHOOL_KIND"]);
                $kindFlg = $row["SCHOOL_KIND"];
            }
        } else {
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt[] = array("label" => htmlspecialchars($row["CLASSCD"]."：".$row["CLASSNAME"]),
                               "value" => $row["CLASSCD"]);
            }
        }
        if ($model->classcd == "") $model->classcd = $opt[0]["value"];
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $kindChk = array();
            $kindChk = explode("-", $model->classcd);
            $model->classcd = ($kindChk[1] == $kindFlg) ? $model->classcd : $opt[0]["value"];
        }

        //教科名コンボボックス
        $objForm->ae( array("type"      => "select",
                            "name"      => "CLASSCD",
                            "size"      => "1",
                            "value"     => $model->classcd,
                            "extrahtml" => "onChange=\"return btn_submit('list'), Cleaning_window();\"",
                            "options"   => $opt));

        $arg["class_cmb"] = $objForm->ge("CLASSCD");

        //科目リスト
        $result = $db->query(knjz220Query::readQuery($model->classcd, $model));
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
             array_walk($row, "htmlspecialchars_array");

             $row["SUBCLASSCD"] = View::alink("knjz220index.php", $row["VALUE"], "target=right_frame", 
                                        array("cmd"            => "edit",
                                              "SUBCLASSCD"     => $row["VALUE"], 
                                              "SUBCLASSNAME"   => $row["SUBCLASSNAME"]));
             $arg["data"][] = $row;
        }
        $result->free();
        Query::dbCheckIn($db);

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "Cleaning") );

        if($model->sec_competence != DEF_UPDATABLE){
            $arg["Closing"] = " closing_window(); " ;
        }

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjz220Form1.html", $arg); 
    }
}
?>
