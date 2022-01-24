<?php

require_once('for_php7.php');

class knjm011Form1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjm011Form1", "POST", "knjm011index.php", "", "knjm011Form1");

        //年度テキストボックスを作成する
        $arg["data"]["YEAR"] = CTRL_YEAR;
        knjCreateHidden($objForm, "YEAR", CTRL_YEAR);

        //学期テキストボックス設定
        $arg["data"]["GAKKI"] = CTRL_SEMESTER;
        knjCreateHidden($objForm, "GAKKI", CTRL_SEMESTER);

        //クラス選択コンボボックスを作成する
        $db = Query::dbCheckOut();
        $query = knjm011Query::getAuth($model->control["年度"],$model->control["学期"]);
        
        //並び順ラジオ 1:クラス順 2:あいうえお順 3:学籍番号順
        $opt_sort = array(1, 2, 3);
        $model->field["SORT"] = $model->field["SORT"] ? $model->field["SORT"] : "1";
        $extra = array("id=\"SORT1\"", "id=\"SORT2\"", "id=\"SORT3\"");
        $sortArray = knjCreateRadio($objForm, "SORT", $model->field["SORT"], $extra, $opt_sort, get_count($opt_sort));
        foreach ($sortArray as $key => $val) $arg["data"][$key] = $val;

        $result = $db->query($query);
        $grade_hr_class_flg = false;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row1[]= array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->field["GRADE_HR_CLASS"] == $row["VALUE"]) {
                $grade_hr_class_flg = true;
            }
        }
        $result->free();
        Query::dbCheckIn($db);

        if(!isset($model->field["GRADE_HR_CLASS"]) || !$grade_hr_class_flg) {
            $model->field["GRADE_HR_CLASS"] = $row1[0]["value"];
        }
        $extra = "onchange=\"return btn_submit('gakki'),AllClearList();\"";
        $arg["data"]["GRADE_HR_CLASS"] = knjCreateCombo($objForm, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], isset($row1) ? $row1 : array(), $extra, 1);

        //対象者リストを作成する
        $db = Query::dbCheckOut();
        $query = knjm011Query::getSchreg($model);
        $result = $db->query($query);

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt1[]= array('label' =>  $row["NAME"],
                           'value' => $row["SCHREGNO"]);
        }
        $result->free();
        Query::dbCheckIn($db);

        $arg["data"][""] = $objForm->ge("category_name");
        $extra = "multiple style=\"width:200px\" width=\"200px\" ondblclick=\"move1('left')\"";
        $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "category_name", $value, isset($opt1) ? $opt1 : array(), $extra, 20);

        //生徒一覧リストを作成する
        $extra = "multiple style=\"width:200px\" width=\"200px\" ondblclick=\"move1('right')\"";
        $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "category_selected", $value, array(), $extra, 20);

        //対象選択ボタンを作成する（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
        $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);

        //対象取消ボタンを作成する（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
        $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);

        //対象選択ボタンを作成する（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
        $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);

        //対象取消ボタンを作成する（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
        $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);

        //部数
        if (!$model->field["BUSU"]) $model->field["BUSU"] = 1;
        $extra = "STYLE=\"text-align: right\" onblur=\"this.value=toInteger(this.value);check(this)\"";
        $arg["data"]["BUSU"] = knjCreateTextBox($objForm, $model->field["BUSU"], "BUSU", 2, 2, $extra);

        //印刷ボタンを作成する
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する(必須)
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJM011");
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "formTypeM010", $model->Properties["formTypeM010"]);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjm011Form1.html", $arg); 
    }
}
?>
