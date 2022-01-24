<?php

require_once('for_php7.php');

class knjc166bForm1
{
    function main(&$model)
    {
        /* フォーム作成 */
        $objForm = new form;

        $arg["start"] = $objForm->get_start("main", "POST", "knjc166bindex.php", "", "main");

        /* データベース接続 */
        $db = Query::dbCheckOut();

        /* 処理年度 */
        $arg["YEAR"] = CTRL_YEAR;

        /* 学年 */
        $query = knjc166bQuery::getGrade($model);
        $extra = "onchange=\"return btn_submit('changeGrade');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);

        /* 年組 */
        $query = knjc166bQuery::getHrClass($model);
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "HR_CLASS", $model->field["HR_CLASS"], $extra, 1);

        /* 皆勤区分 */
        $query = knjc166bQuery::getKaikinCd($model);
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "KAIKIN_CD", $model->field["KAIKIN_CD"], $extra, 1);

        //更新用の配列
        $model->data = array();

        /* リスト取得 */
        $query = knjc166bQuery::selectQuery($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            $model->data["SCHREGNO"][] = $row["SCHREGNO"];

            $disabled = $row["KAIKIN_FLG"] == "1" ? "" : "disabled";
            $extra  = $row["KAIKIN_FLG"] == "1" ? "checked" : "";
            $extra .= " id=\"KAIKIN_FLG_{$row["EXAMNO"]}\" onclick=\"changeKikanFlg('{$row["SCHREGNO"]}');\" ";
            $row["KAIKIN_FLG"] = knjCreateCheckBox($objForm, "KAIKIN_FLG_".$row["SCHREGNO"], "1", $extra, "");

            $extra = $row["INVALID_FLG"] == "1" ? "checked" : "";
            $extra .= " id=\"INVALID_FLG_{$row["EXAMNO"]}\" ".$disabled;
            $row["INVALID_FLG"] = knjCreateCheckBox($objForm, "INVALID_FLG_".$row["SCHREGNO"], "1", $extra, "");

            $arg["data"][] = $row;
        }

        /* ボタン作成 */
        makeButton($objForm, $arg, $db, $model);

        /* データベース接続切断 */
        Query::dbCheckIn($db);

        //hidden
        knjCreateHidden($objForm, "cmd");

        $arg["finish"]  = $objForm->get_finish();

        /* テンプレート呼び出し */
        View::toHTML($model, "knjc166bForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
            'value' => $row["VALUE"]);
    }
    $result->free();
    $value = ($value == "") ? $opt[0]["value"] : $value;

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeButton(&$objForm, &$arg, $db, $model)
{
    //更新ボタン
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", " onclick=\"return btn_submit('update');\"");
    //取消ボタン
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", "onclick=\"btn_submit('reset');\"");
    //終了ボタン
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

?>
