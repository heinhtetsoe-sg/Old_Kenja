<?php

require_once('for_php7.php');

class knjf152Form1
{
    function main(&$model)
    {
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("main", "POST", "knjf152index.php", "", "main");
        $db = Query::dbCheckOut();

        $row = array();

        //年度コンボ
        $query = knjf152Query::getYear();
        $extra = "onchange=\"return btn_submit('');\"";
        makeCmb($objForm, $arg, $db, $query, $model->year, "YEAR", $extra, 1);

        //ボタン作成
        makeButton($objForm, $arg, $schoolkind);

        //リスト表示
        makeList($objForm, $arg, $db, $model);

        //hidden
        makeHidden($objForm);

        $arg["finish"] = $objForm->get_finish();
        Query::dbCheckIn($db);
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjf152Form1.html", $arg); 
    }
}

/********************************************** 以下関数 **********************************************/

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "") {
    $opt = array();

    if ($blank == "BLANK") {
        $opt[] = array("label" => "",
                       "value" => "");
    }

    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "YEAR") {
        $value = ($value && $value_flg) ? $value : CTRL_YEAR;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }
    
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//リスト表示
function makeList(&$objForm, &$arg, $db, $model) {

    //初期化
    $model->data = array();
    $before_target_month = "";
    $month_list = "";
    if ($model->cmd == "reset") unset($model->fields);

    //学期リスト
    $query = knjf152Query::getList($model->year);
    $result = $db->query($query);
    $monthData = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        for ($i = $row['SMONTH']; $i <= $row['EMONTH']; $i++) {
            if ($i > 12) {
                $target_month = $i - 12;
            } else {
                $target_month = $i;
            }

            $monthData[$target_month] = $monthData[$target_month] + 1;

            //授業日数取得
            $query = knjf152Query::getLesson($model->year, sprintf('%02d',$target_month));
            $lesson = $db->getOne($query);

            $month = sprintf('%02d', $target_month);
            $month_list .= ($month_list) ? ",".$month : $month;
            $setStyle = "";

            //授業日数テキストボックス
            $model->fields["LESSON"][$month] = $lesson;
            $extra = "onblur=\"this.value=toInteger(this.value);\" STYLE=\"text-align: right; {$setStyle}\"";
            $row["LESSON"] = knjCreateTextBox($objForm, $model->fields["LESSON"][$month], "LESSON".$month, 2, 2, $extra);

            //月の表示文字をDBから取得
            $query = knjf152Query::getMonthName($model->year, sprintf('%02d',$target_month));
            $monthName = $db->getOne($query);
            $row["MONTHNAME"] = $monthName;

            $arg["data"][] = $row;
            $before_target_month = $target_month;
        }
    }
    $result->free();
    knjCreateHidden($objForm, "month_list", $month_list);
}

//ボタン作成
function makeButton(&$objForm, &$arg, $schoolkind) {

    //保存ボタン
    $arg["button"]["btn_updte"] = knjCreateBtn($objForm, "btn_updte", "保 存", "onclick=\"return btn_submit('update');\"");

    //取消ボタンを作成する
    $arg["button"]["btn_clear"] = knjCreateBtn($objForm, "btn_clear", "取 消", "onclick=\"return btn_submit('reset');\"");

    //終了ボタン
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"return closeWin();\"");
}

//hidden作成
function makeHidden(&$objForm) {
        knjCreateHidden($objForm, "cmd");
}
?>
