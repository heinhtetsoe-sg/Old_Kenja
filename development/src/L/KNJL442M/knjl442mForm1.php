<?php

require_once('for_php7.php');

class knjl442mForm1
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"]  = $objForm->get_start("knjl442mForm1", "POST", "knjl442mindex.php", "", "knjl442mForm1");
        $extra = "onChange=\"return btn_submit('knjl442m')\"";

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->test_year;

        // 校種
        $query = knjl442mQuery::getSchoolKindName($model->test_year);
        makeCmb($objForm, $arg, $db, $query, "EXAM_SCHOOL_KIND", $model->field["EXAM_SCHOOL_KIND"], $extra, 1);

        // 入試区分（＝志望区分）
        $query = knjl442mQuery::getApplicant($model);
        makeCmb($objForm, $arg, $db, $query, "APPLICANT_DIV", $model->field["APPLICANT_DIV"], $extra, 1, " ");

        // 志望コース
        $query = knjl442mQuery::getCoursecode($model);
        makeCmb($objForm, $arg, $db, $query, "COURSECODE", $model->field["COURSECODE"], $extra, 1, " ");

        // 回数
        $query = knjl442mQuery::getFrequency($model);
        makeCmb($objForm, $arg, $db, $query, "FREQUENCY", $model->field["FREQUENCY"], "", 1, " ");


        /**************/
        /* ボタン作成 */
        /**************/
        //CSV出力
        $extra = "onclick=\"return btn_submit('exec');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "CSV出力", $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);


        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "YEAR", $model->test_year);
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "PRGID", "KNJL442M");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl442mForm1.html", $arg);
    }
}
//コンボ作成

function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $space = "")
{
    $opt = array();
    if ($space) {
        $opt[] = array('label' => "", 'value' => "");
    }
    $value_flg = false;
    $result1 = $db->query($query);
    while ($row1 = $result1->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row1["LABEL"], 'value' => $row1["VALUE"]);
        if ($value == $row1["VALUE"]) {
            $value_flg = true;
        }
    }

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
