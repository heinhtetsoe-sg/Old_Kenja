<?php

require_once('for_php7.php');


class knjz220dForm1{

    function main(&$model){

        $objForm      = new form;
        $arg["start"] = $objForm->get_start("list", "POST", "knjz220dindex.php", "", "edit");
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["execjs"] = " close_window(); " ;
        }

        $arg["YEAR"] = CTRL_YEAR;

        $db = Query::dbCheckOut();

        $query = knjz220dQuery::semesterQuery();
        $extra = " onChange=\"return btn_submit('semester'), iniright();\"";
        $arg["seme_cmb"] = knjCreateCombo($objForm, "SEMESTER", $model->semester, knjz220dModel::createOpts($query, $db), $extra, 1);

        //学年コンボ
        $query =knjz220dQuery::combo_grdQuery($model);
        $extra =  "onChange=\"return btn_submit('list'), iniright();\"";
        $arg["grade_cmb"] = knjCreateCombo($objForm, "GRADE", $model->grade, knjz220dModel::createOpts($query, $db), $extra, 1);

        //校種
        $query = knjz220dQuery::getSchoolKind($model);
        $model->schoolKind = $db->getOne($query);

        //教科コンボ
        $query = knjz220dQuery::combo_clsQuery($model);
        $result = $db->query($query);
        $opt = array();
        $kindFlg = "";
        //教育課程対応
        $setClassDef = "00-".$model->schoolKind;
        $opt[] = array("label" => $setClassDef."：基本", "value" => $setClassDef);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => htmlspecialchars($row["LABEL"]), "value" => $row["VALUE"]);
            if ($model->Properties["useCurriculumcd"] == '1') {
                $kindFlg = $row["SCHOOL_KIND"];
            }
        }
        if ($model->classcd == "") {
            $model->classcd = $opt[0]["value"];
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $kindChk = array();
            $kindChk = explode("-", $model->classcd);
            $model->classcd = ($kindChk[1] == $kindFlg) ? $model->classcd : $opt[0]["value"];
        }
        $extra = "onChange=\"return btn_submit('chgCmb'), iniright();\"";
        $arg["class_cmb"] = knjCreateCombo($objForm, "CLASSCD", $model->classcd, $opt, $extra, 1);

        //科目リスト
        if($model->classcd == $setClassDef){
            $setClassDefcd   = $setClassDef."-00-000000";
            $setClassDefname = "基本科目";
            $row = array("VALUE" => $setClassDefcd, "SUBCLASSNAME" => $setClassDefname);

            $row["SUBCLASSCD"] = View::alink("knjz220dindex.php", $row["VALUE"], "target=right_frame", 
                                       array("cmd"            => "edit"
                                             , "SEMESTER"       => $model->semester
                                             , "GRADE"          => $model->grade
                                             , "SUBCLASSCD"     => $row["VALUE"]
                                             , "inir"           => "1" 
                                         )
                                     );
            $arg["data"][] = $row;
        }
        $query = knjz220dQuery::readQuery($model->classcd, $model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
             array_walk($row, "htmlspecialchars_array");

             $row["SUBCLASSCD"] = View::alink("knjz220dindex.php", $row["VALUE"], "target=right_frame", 
                                        array("cmd"            => "edit"
                                              , "SEMESTER"       => $model->semester
                                              , "GRADE"          => $model->grade
                                              , "SUBCLASSCD"     => $row["VALUE"]
                                              , "inir"           => "1" 
                                          )
                                      );
             $arg["data"][] = $row;
        }
        $result->free();
        Query::dbCheckIn($db);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "Cleaning");

        $arg["finish"]  = $objForm->get_finish();
        if ($model->cmd == "chgCmb") {
            $arg["reload"] = "window.open('knjz220dindex.php?cmd=edit2&SEMESTER={$model->semester}&GRADE={$model->grade}&CLASSCD={$model->classcd}','right_frame');";
        }
        View::toHTML($model, "knjz220dForm1.html", $arg); 
    }
}
?>
