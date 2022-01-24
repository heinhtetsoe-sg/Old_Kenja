<?php

require_once('for_php7.php');

class knjb1254Form1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjb1254Form1", "POST", "knjb1254index.php", "", "knjb1254Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ作成
        $query = knjb1254Query::getSemester();
        $extra = "onchange=\"return btn_submit('knjb1254');\"";
        makeCmb($objForm, $arg, $db, $query, "YEAR_SEMESTER", $model->field["YEAR_SEMESTER"], $extra, 1);

        //履修履歴コンボ
        $query = knjb1254Query::getRirekiCode($model);
        $extra = "onChange=\"return btn_submit('knjb1254');\"";
        makeCmb($objForm, $arg, $db, $query, "RIREKI_CODE", $model->field["RIREKI_CODE"], $extra, 1);
        
        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $db, $model);

        //ＣＳＶ出力対象件数
        $csv_cnt = get_count($db->getCol(knjb1254Query::selectCsvQuery($model)));

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        if (isset($model->message) && $csv_cnt > 0) {   //更新したらＣＳＶ出力
            $arg["reload"] = "btn_submit('csv')";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjb1254Form1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }

    $result->free();

    if ($value && $value_flg) {
        $value = $value;
    } else {
        $value = ($name == "YEAR_SEMESTER") ? (CTRL_YEAR + 1).":1" : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //実行ボタン
    $extra = "onclick=\"return btn_submit('create');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "履修科目の自動名簿生成", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $db, $model)
{
    knjCreateHidden($objForm, "cmd");
    //受講生徒数
    $std_cnt = $db->getOne(knjb1254Query::getChairStdDatCount($model));
    knjCreateHidden($objForm, "STD_CNT", $std_cnt);
    //出欠済み時間割数
    $exe_cnt = $db->getOne(knjb1254Query::getSchChrDatExecuted($model));
    knjCreateHidden($objForm, "EXECUTED_CNT", $exe_cnt);
}
