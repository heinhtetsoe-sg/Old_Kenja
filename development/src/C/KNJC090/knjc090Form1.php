<?php

require_once('for_php7.php');


class knjc090Form1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjc090Form1", "POST", "knjc090index.php", "", "knjc090Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度テキストボックスを作成する
        $arg["data"]["YEAR"] = $model->control["年度"];

        //カレンダーコントロール１
        $value = isset($model->field["DATE1"])?$model->field["DATE1"]:$model->control["学籍処理日"];
        $arg["data"]["DATE1"] = View::popUpCalendar($objForm, "DATE1", $value);

        //カレンダーコントロール２
        $value2 = isset($model->field["DATE2"])?$model->field["DATE2"]:$model->control["学籍処理日"];
        $arg["data"]["DATE2"] = View::popUpCalendar($objForm, "DATE2", $value2);

        //学期開始日・終了日を取得する
        $semester = $model->control['学期開始日付'][1] ."," .$model->control['学期終了日付'][1];
        $semester = $semester ."," .$model->control['学期開始日付'][2] ."," .$model->control['学期終了日付'][2];
        $semester = $semester ."," .$model->control['学期開始日付'][3] ."," .$model->control['学期終了日付'][3];

        //学年リストボックスを作成する
        $opt_schooldiv = "学年";

        $opt_grade=array();
        $query = knjc090Query::getSelectGrade($model->control["年度"]);
        $result = $db->query($query);
        $i=0;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $grade_show= sprintf("%d",$row["GRADE"]);
            $opt_grade[] = array('label' => $grade_show.$opt_schooldiv,
                                 'value' => $row["GRADE"]);
            $i++;
        }
        if ($model->field["GAKUNEN"] == "") $model->field["GAKUNEN"] = $opt_grade[0]["value"];
        $result->free();

        $extra = "multiple";
        $arg["data"]["GAKUNEN"] = knjCreateCombo($objForm, "GAKUNEN", $model->field["GAKUNEN"], $opt_grade, $extra, $i);
        $objForm->ge("GAKUNEN");

        //出力する情報チェックボックスを作成
        $extra = " checked onclick=\"kubun();\"";
        $arg["data"]["OUTPUT1"] = knjCreateCheckBox($objForm, "OUTPUT1", isset($model->field["OUTPUT1"]) ? $model->field["OUTPUT1"] : "1", $extra);

        $extra = " checked onclick=\"kubun();\"";
        $arg["data"]["OUTPUT2"] = knjCreateCheckBox($objForm, "OUTPUT2", isset($model->field["OUTPUT2"]) ? $model->field["OUTPUT2"] : "1", $extra);

        $extra = " checked onclick=\"kubun();\"";
        $arg["data"]["OUTPUT3"] = knjCreateCheckBox($objForm, "OUTPUT3", isset($model->field["OUTPUT3"]) ? $model->field["OUTPUT3"] : "1", $extra);

        //印刷ボタンを作成する
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJC090");
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        //年度データ
        knjCreateHidden($objForm, "YEAR", $model->control["年度"]);
        //学期（hidden）
        knjCreateHidden($objForm, "SEMESTER", $model->control["学期"]);
        //学期開始日(hidden)
        knjCreateHidden($objForm, "SEME_DATE", $semester);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjc090Form1.html", $arg); 

    }

}
?>
