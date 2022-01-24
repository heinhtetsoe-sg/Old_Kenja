<?php

require_once('for_php7.php');

class knjh562bForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjh562bForm1", "POST", "knjh562bindex.php", "", "knjh562bForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ作成
        $query = knjh562bQuery::getSemester();
        $extra = "onchange=\"return btn_submit('knjh562b');\"";
        if ($model->field["SEMESTER"] == "") {
            $model->field["SEMESTER"] = CTRL_SEMESTER;
        }
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //卒業学年の取得
        if ($model->field["GRADE"] == "") {
            $query = knjh562bQuery::getGraduateGrade($model);
            $model->field["GRADE"] = $db->getOne($query);
        }

        //学年コンボ作成
        $query = knjh562bQuery::getGrade($model);
        $extra = "onchange=\"return btn_submit('knjh562b');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);

        //データ種別コンボ作成
        $query = knjh562bQuery::getDataDiv();
        $extra = "onchange=\"return btn_submit('knjh562b')\"";
        makeCmb($objForm, $arg, $db, $query, "PROFICIENCYDIV", $model->field["PROFICIENCYDIV"], $extra, 1);

        //テスト名称コンボ作成
        $query = knjh562bQuery::getProName($model);
        $extra = "onchange=\"return btn_submit('knjh562b')\"";
        makeCmb($objForm, $arg, $db, $query, "PROFICIENCYCD", $model->field["PROFICIENCYCD"], $extra, 1);

        /* 実施科目コンボ */
        $query = knjh562bQuery::getTestSubclass($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "PROFICIENCY_SUBCLASS_CD", $model->field["PROFICIENCY_SUBCLASS_CD"], $extra, 1);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh562bForm1.html", $arg);
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

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

function makeBtn(&$objForm, &$arg)
{
    //印刷ボタンを作成する
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJH562B");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "KNJH562BPrintDistributionScore", $model->Properties["KNJH562BPrintDistributionScore"]);
    knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
    knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
}
