<?php

require_once('for_php7.php');

class knjc120Form1
{
    function main(&$model){

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjc120Form1", "POST", "knjc120index.php", "", "knjc120Form1");

        //DB接続
        $db = Query::dbCheckOut();

        $opt=array();

        //年度テキストボックスを作成する
        $arg["data"]["YEAR"] = $model->control["年度"];

        knjCreateHidden($objForm, "YEAR", $model->control["年度"]);

        //学期テキストボックスを作成する
        $arg["data"]["SEME_SHOW"] = $model->control["学期名"][$model->control["学期"]];

        //カレンダーコントロール
        $arg["el"]["DATE"] = View::popUpCalendar($objForm, "DATE", isset($model->field["DATE"])?$model->field["DATE"]:$model->control["学籍処理日"]);

        //クラス選択コンボボックスを作成する
        $query = knjc120Query::getAuth($model, $model->control["年度"],$model->control["学期"]);
        $row1 = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row1[]= array('label' => $row["LABEL"],
                            'value' => $row["VALUE"]);
        }
        $result->free();

        if (!isset($model->field["GRADE_HR_CLASS"])) {
            $model->field["GRADE_HR_CLASS"] = $row1[0]["value"];
        }

        $extra = "onchange=\"return btn_submit('knjc120');\"";
        $arg["data"]["GRADE_HR_CLASS"] = knjCreateCombo($objForm, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $row1, $extra, 1);

        //クラス選択コンボボックスを作成する
        $query = knjc120Query::getStudent($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt1[]= array('label' =>  $row["NAME"],
                            'value' => $row["SCHREGNO"]);
        }
        $result->free();

        //対象者リストを作成する
        $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move('right')\"";
        $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "category_name", $value, array(), $extra, 20);

        //生徒一覧リストを作成する
        $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move('left')\"";
        $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "category_selected", "", isset($opt1) ? $opt1 : array(), $extra, 20);

        //対象選択ボタンを作成する（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
        $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);

        //対象取消ボタンを作成する（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
        $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);

        //対象取消ボタンを作成する
        $extra = "style=\"height:20px;width:40px\" onclick=\"move('right');\"";
        $arg["button"]["btn_right"] = knjCreateBtn($objForm, "btn_right", "＞", $extra);

        //対象選択ボタンを作成する
        $extra = "style=\"height:20px;width:40px\" onclick=\"move('left');\"";
        $arg["button"]["btn_left"] = knjCreateBtn($objForm, "btn_left", "＜", $extra);

        //印刷ボタンを作成する
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJC120");
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        //学期用データ
        knjCreateHidden($objForm, "SEMESTER",  $model->control["学期"]);
        //年度データ
        knjCreateHidden($objForm, "YEAR", $model->control["年度"]);
        //年度データ
        knjCreateHidden($objForm, "CTRL_DATE", str_replace("-", "/", CTRL_DATE));
        knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
        knjCreateHidden($objForm, "useKekkaJisu", $model->Properties["useKekkaJisu"]);
        knjCreateHidden($objForm, "useKekka", $model->Properties["useKekka"]);
        knjCreateHidden($objForm, "useLatedetail", $model->Properties["useLatedetail"]);
        knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);
        knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
        knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);
        knjCreateHidden($objForm, "knjc120PrintGradeCd", $model->Properties["knjc120PrintGradeCd"]);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjc120Form1.html", $arg); 

    }

}

?>
