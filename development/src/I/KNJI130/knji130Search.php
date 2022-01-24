<?php

require_once('for_php7.php');

class knji130Search
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        $arg = array();

        //フォーム作成
        $arg["start"] = $objForm->get_start("search", "POST", "knji130index.php", "right_frame");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $query = knji130Query::getGrdDateYear();
        $extra = "onchange=\"return btn_submit('right')\"";
        makeCmb($objForm, $arg, $db, $query, "GRD_YEAR", $model->search["GRD_YEAR"], $extra, 1);

        //年組
        $query = knji130Query::searchGradeHrClass($model, $model->search["GRD_YEAR"]);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->search["GRADE_HR_CLASS"], $extra, 1);

        //コースコンボボックス
        $query = knji130Query::searchCourseCodeMst();
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "COURSECODE", $model->search["COURSECODE"], $extra, 1);

        //学籍番号
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["S_SCHREGNO"] = knjCreateTextBox($objForm, $model->search["S_SCHREGNO"], "S_SCHREGNO", 8, 8, $extra);

        //氏名
        $extra = "";
        $arg["NAME"] = knjCreateTextBox($objForm, $model->search["NAME"], "NAME", 40, 40, $extra);

        //氏名表示用
        $extra = "";
        $arg["NAME_SHOW"] = knjCreateTextBox($objForm, $model->search["NAME_SHOW"], "NAME_SHOW", 40, 40, $extra);

        //氏名かな
        $extra = "";
        $arg["NAME_KANA"] = knjCreateTextBox($objForm, $model->search["NAME_KANA"], "NAME_KANA", 40, 40, $extra);

        //性別
        $query = knji130Query::getNameMst("Z002", "", 3);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "SEX", $model->search["SEX"], $extra, 1);

        //検索ボタン
        $extra = "onclick=\"return search_submit();\"";
        $arg["btn_search"] = knjCreateBtn($objForm, "btn_search", "検 索", $extra);

        //終了ボタン
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"return btn_back();\"");

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        View::toHTML($model, "knji130Search.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $opt[] = array('label' => "", 'value' => "");
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
