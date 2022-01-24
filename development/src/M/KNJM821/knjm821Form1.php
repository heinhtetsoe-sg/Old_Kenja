<?php

require_once('for_php7.php');


class knjm821Form1
{
    function main(&$model){

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjm821Form1", "POST", "knjm821index.php", "", "knjm821Form1");

        //年度テキストボックスを作成する
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //印刷範囲を作成する
        //開始
        $model->field["OUTPUTSDATE"] = ($model->field["OUTPUTSDATE"]) ? $model->field["OUTPUTSDATE"] : str_replace("-","/",$model->control['学期開始日付']["1"]);
        $arg["data"]["OUTPUTSDATE"] = View::popUpCalendar($objForm, "OUTPUTSDATE", $model->field["OUTPUTSDATE"]);
        //終了
        $model->field["OUTPUTEDATE"] = ($model->field["OUTPUTEDATE"]) ? $model->field["OUTPUTEDATE"] : str_replace("-","/",CTRL_DATE);
        $arg["data"]["OUTPUTEDATE"] = View::popUpCalendar($objForm, "OUTPUTEDATE", $model->field["OUTPUTEDATE"]);
        
        //集計票
        if ($model->field["SHUUKEI"] == "1") {
            $extra = "checked='checked' ";
        } else {
            $extra = "";
        }
        $arg["data"]["SHUUKEI"] = knjCreateCheckBox($objForm, "SHUUKEI", "1", $extra);
        
        //明細票
        if ($model->field["MEISAI"] == "1") {
            $extra = "checked='checked' ";
        } else {
            $extra = "";
        }
        $arg["data"]["MEISAI"] = knjCreateCheckBox($objForm, "MEISAI", "1", $extra);

        //クラス選択コンボボックスを作成する
        $db = Query::dbCheckOut();
        $query = knjm821Query::getClass($model);
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
        knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJM821");
        knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjm821Form1.html", $arg); 
    }
}
?>
