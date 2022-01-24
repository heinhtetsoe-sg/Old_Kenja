<?php

require_once('for_php7.php');

class knjj191Form1 {

    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjj191Form1", "POST", "knjj191index.php", "", "knjj191Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->year;

        //校種radio
        $setSchoolKind = SCHOOLKIND == "H" ? "2" : "1";
        $opt = array(1, 2);
        $model->field["SCHOOL_KIND"] = ($model->field["SCHOOL_KIND"] == "") ? $setSchoolKind : $model->field["SCHOOL_KIND"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"SCHOOL_KIND{$val}\" ");
        }
        $radioArray = knjCreateRadio($objForm, "SCHOOL_KIND", $model->field["SCHOOL_KIND"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //印刷
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJJ191");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        knjCreateHidden($objForm, "selectdata");
        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        knjCreateHidden($objForm, "SCHOOLCD", sprintf("%012d", SCHOOLCD));
        knjCreateHidden($objForm, "SCHOOL_KIND", SCHOOLKIND);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjj191Form1.html", $arg); 
    }
}

?>
