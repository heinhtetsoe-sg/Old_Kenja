<?php

require_once('for_php7.php');

class knjd626jForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjd626jForm1", "POST", "knjd626jindex.php", "", "knjd626jForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ作成
        $query = knjd626jQuery::getSemester();
        $extra = "onchange=\"return btn_submit('knjd626j')\"";
        $model->field["SEMESTER"] = $model->field["SEMESTER"] == "" ? "9" : $model->field["SEMESTER"];
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1, $model);

        //学籍系データ検索用の学期を割り出す
        $query = knjd626jQuery::getMaxSemester();
        $seme = $model->field["SEMESTER"] == "9" ? $db->getOne($query) : $model->field["SEMESTER"];

        //学年コンボ作成
        $query = knjd626jQuery::getGrade($model);
        $extra = "onchange=\"return btn_submit('knjd626j')\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1, $model);

        //全選択
        $extra  = "onchange=\"chkAll();\"";
        $extra .= $model->field["CHKALL"] == "ALL" ? " checked" : "";
        $arg["data"]["CHKALL"] = knjCreateCheckBox($objForm, "CHKALL", "ALL", $extra);

        for ($cnt = 1; $cnt <= 9; $cnt++) {
            $extra = $model->field["CHKBOX".$cnt] == $cnt ? " checked" : "";
            $arg["data"]["CHKBOX".$cnt] = knjCreateCheckBox($objForm, "CHKBOX".$cnt, $cnt, $extra);
        }

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd626jForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $model)
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

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

function makeBtn(&$objForm, &$arg)
{
    //印刷ボタンを作成する
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //ＣＳＶボタンを作成する
    $extra = "onclick=\"return btn_submit('csv');\"";
    $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJD626J");
    knjCreateHidden($objForm, "SCHOOLCD", SCHOOLCD);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
}
