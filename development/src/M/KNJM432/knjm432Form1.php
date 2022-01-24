<?php

require_once('for_php7.php');


class knjm432Form1
{
    function main(&$model){

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjm432Form1", "POST", "knjm432index.php", "", "knjm432Form1");

        //年度テキストボックスを作成する
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //年度
        knjCreateHidden($objForm, "YEAR", CTRL_YEAR);

        //学期テキストボックスを作成する
        $opt_seme = array();
        for ($i = 0; $i < $model->control["学期数"]; $i++) {
            $opt_seme[]= array("label" => $model->control["学期名"][$i+1],
                               "value" => sprintf("%d", $i+1));

        }
        $model->field["SEMESTER"] = !$model->field["SEMESTER"] ? CTRL_SEMESTER : $model->field["SEMESTER"];
        $extra = "onChange=\"return btn_submit('knjm432');\"";
        $arg["data"]["SEMESTER"] = knjCreateCombo($objForm, "SEMESTER", $model->field["SEMESTER"], $opt_seme, $extra, 1);

        //クラス選択コンボボックスを作成する
        $db = Query::dbCheckOut();
        $query = knjm432Query::getClass($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt1[]= array('label' => $row["HR_NAME"],
                           'value' => $row["VALUE"]);
        }
        $result->free();
        Query::dbCheckIn($db);

        //対象者リストを作成する
        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right')\"";
        $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", array(), $extra, 20);

        //生徒一覧リストを作成する
        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left')\"";
        $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", isset($opt1) ? $opt1 : array(), $extra, 20);

        //対象取り消しボタンを作成する(個別)
        $extra = " onclick=\"move('right');\"";
        $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "　＞　", $extra);

        //対象取り消しボタンを作成する(全て)
        $extra = " onclick=\"move('rightall');\"";
        $arg["button"]["btn_right2"] = knjCreateBtn($objForm, "btn_right2", "　≫　", $extra);

        //対象選択ボタンを作成する(個別)
        $extra = " onclick=\"move('left');\"";
        $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "　＜　", $extra);

        //対象選択ボタンを作成する(全て)
        $extra = " onclick=\"move('leftall');\"";
        $arg["button"]["btn_left2"] = knjCreateBtn($objForm, "btn_left2", "　≪　", $extra);

        //印刷ボタンを作成する
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJM432");
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjm432Form1.html", $arg); 
    }
}
?>
