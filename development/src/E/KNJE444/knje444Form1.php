<?php

require_once('for_php7.php');

class knje444Form1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"]  = $objForm->get_start("knje444Form1", "POST", "knje444index.php", "", "knje444Form1");

        //DB接続
        $db = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();

        //年度
        $model->field["YEAR"] = CTRL_YEAR;

        //V_SCHOOL_MSTから教育委員会用の学校コードを取得
        $query = knje444Query::getSchoolMst($model);
        $rtnRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $model->schoolcd = $rtnRow["KYOUIKU_IINKAI_SCHOOLCD"];

        //EDBOARD_SCHOOL_MSTから教育委員会用の学校名を取得
        $query = knje444Query::getEdboardSchoolMst($model);
        $rtnRow2 = $db2->getRow($query, DB_FETCHMODE_ASSOC);
        $model->schoolname = $rtnRow2["EDBOARD_SCHOOLNAME"];

        //対象学科コンボ
        $query = knje444Query::getMajorcd($model);
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "MAJORCD", $model->field["MAJORCD"], $extra, 1);

        //実行日時（学科選択時、提出済み学科であれば、最後に提出した日時情報を表示）
        $query = knje444Query::getExecDate($model);
        $execRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if (is_array($execRow)) {
            list($year, $month, $day) = explode("-", $execRow["CALC_DATE"]);
            list($hours, $minutes, $seconds) = explode(":", $execRow["CALC_TIME"]);
            $execDate = $year."年".$month."月".$day."日　".$hours."時".$minutes."分　提出済み";
        } else {
            $execDate = "";
        }
        $arg["data"]["EXEC_DATE"] = $execDate;
        knjCreateHidden($objForm, "EXEC_DATE_ERRCHECK", $execDate);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje444Form1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    if ($name == "MAJORCD") {
        $opt[] = array('label' => "全ての学科",
                       'value' => "A-ALL");
    }
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

    if ($name == "YEAR") {
        $value = ($value && $value_flg) ? $value : CTRL_YEAR;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //CSV出力
    $extra = "onclick=\"return btn_submit('csv');\"";
    $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "CSV出力（データ確認）", $extra);
    //県への報告
    $extra = "onclick=\"return btn_submit('houkoku');\"";
    $arg["button"]["btn_houkoku"] = knjCreateBtn($objForm, "btn_houkoku", "県への報告", $extra);
    //終了
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "cmd");
}
