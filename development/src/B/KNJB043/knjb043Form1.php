<?php

require_once('for_php7.php');

class knjb043Form1
{
    public function main(&$model)
    {
        $objForm = new form();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjb043Form1", "POST", "knjb043index.php", "", "knjb043Form1");

        //年度テキストボックスを作成する
        $arg["data"]["YEAR"] = $model->control["年度"];

        //クラス一覧リスト作成する NO008↓
        $row1 = array();
        $row2 = array();
        $db = Query::dbCheckOut();
        $query = knjb043Query::getSection($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row1[]= array('label' => $row["LABEL"],
                            'value' => $row["VALUE"]);
        }
        $result->free();
        Query::dbCheckIn($db);

        $extra = "multiple style=\"width:100%\" ondblclick=\"move1('left')\"";
        $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $row1, $extra, 20);

        //出力対象クラスリストを作成する
        $extra = "multiple style=\"width:100%\" ondblclick=\"move1('right')\"";
        $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", $row2, $extra, 20);

        //対象選択ボタンを作成する（全部）
        $extra_rights = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
        $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra_rights);

        //対象取消ボタンを作成する（全部）
        $extra_lefts = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
        $arg["button"]["btn_lefts"]  = knjCreateBtn($objForm, "btn_lefts", "<<", $extra_lefts);

        //対象選択ボタンを作成する（一部）
        $extra_right1 = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
        $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra_right1);

        //対象取消ボタンを作成する（一部）
        $extra_left1 = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
        $arg["button"]["btn_left1"]  = knjCreateBtn($objForm, "btn_left1", "＜", $extra_left1);

        $value = isset($model->field["SDATE"]) ? $model->field["SDATE"] : $model->control["学籍処理日"];
        //カレンダーコントロール
        $arg["el"]["SDATE"] = View::popUpCalendar($objForm, "SDATE", $value);

        $weekNo = date('w', strtotime(date($model->control["学籍処理日"])));
        $moveWeekWk = 6 - $weekNo;
        $eDVal = date('Y/m/d', strtotime("+{$moveWeekWk} day", strtotime(date($model->control["学籍処理日"]))));
        $value = isset($model->field["EDATE"]) ? $model->field["EDATE"] : $eDVal;
        //カレンダーコントロール
        $arg["el"]["EDATE"] = View::popUpCalendar($objForm, "EDATE", $value);

        //印刷ボタンを作成する
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する
        knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJB043");
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CALCD_START_DATE");
        knjCreateHidden($objForm, "CALCD_END_DATE");

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjb043Form1.html", $arg);
    }
}
