<?php

require_once('for_php7.php');

// kanji=漢字
// $Id: knjb0056Form1.php,v 1.8 2014/04/14 06:21:51 maesiro Exp $
class knjb0056Form1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjb0056Form1", "POST", "knjb0056index.php", "", "knjb0056Form1");

        $arg["subclasscd"]    = $model->subclasscd;     // 科目コード

        $arg["dbname"]     = DB_DATABASE;
        $arg["staffcd"]    = STAFFCD;
        $arg["useCurriculumcd"] = $model->Properties["useCurriculumcd"] == "1" ? "true" : "false";
        $arg["isShowStaffcd"] = $model->Properties["notShowStaffcd"] == "1" ? "false" : "true";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $arg["SCHOOLCD"] = sprintf("%012d", SCHOOLCD);
                $arg["SCHOOLKIND"] = SCHOOLKIND;
        }

        $arg["ctrl_m_ctrl_year"]        = $model->year;
        $arg["ctrl_m_ctrl_semester"]    = $model->semester;
        $arg["ctrl_m_ctrl_date"]        = CTRL_DATE;
        $arg["ctrl_m_attend_ctrl_date"] = ATTEND_CTRL_DATE;

        $objForm->ae( array("type" => "hidden",
                            "name" => "cmd"
                            ));

        //フォーム作成
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjb0056Form1.html", $arg); 
    }
}
?>
