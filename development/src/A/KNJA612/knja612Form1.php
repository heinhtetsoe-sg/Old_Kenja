<?php

require_once('for_php7.php');

class knja612Form1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("knja612Form1", "POST", "knja612index.php", "", "knja612Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学年コンボ作成
        $query = knja612Query::getGradeHrClass($model);
        $extra = "onchange=\"return btn_submit('knja612');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1, "BLANK");

        //文言種別コンボ作成
        $opt = array();
        $opt[] = array("value" => "10", "label" => "10:道徳");
        $opt[] = array("value" => "21", "label" => "21:総合的な学習の時間　－　学習活動");
        $opt[] = array("value" => "22", "label" => "22:総合的な学習の時間　－　観点");
        $opt[] = array("value" => "23", "label" => "23:総合的な学習の時間　－　評価");
        $extra = "onchange=\"return btn_submit('knja612')\"";
        $arg["data"]["DATA_DIV"] = knjCreateCombo($objForm, "DATA_DIV", $model->field["DATA_DIV"], $opt, $extra, 1);

        $query = knja612Query::getScoreList($model);
        $result = $db->query($query);
        $sep = "";
        $schregnos = "";
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $schregnos .= $sep.$row["SCHREGNO"];
            $row["SCORE"]    = knjCreateTextBox($objForm, $row["SCORE"], "SCHREGNO-".$row["SCHREGNO"], 3, 3, "onchange=\"this.value=toInteger(this.value); checkVal(this);\"");
            $arg["scorelist"][] = $row;
            $sep = ",";
        }
        $model->schregnos = $schregnos;
        $result->free();

        if ($model->field["GRADE_HR_CLASS"] != "") {
            $query = knja612Query::getMaxScore($model);
            $model->field["PERFECT"] = $db->getOne($query);

            if ($model->field["PERFECT"] == "") {
                $model->field["PERFECT"] = "0";
            }
        }

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja612Form1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank != "") {
        $opt[] = array('label' => '', 'value' => '');
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

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

function makeBtn(&$objForm, &$arg)
{
    $btnStyle = "style=\"width:60px\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", " onclick=\"btn_submit('update');\" ".$btnStyle);
    $arg["button"]["btn_cancel"] = knjCreateBtn($objForm, "btn_cancel", "取 消", " onclick=\"btn_submit('cancel');\" ".$btnStyle);
    $arg["button"]["btn_end"]    = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"".$btnStyle);
}

function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJA612");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "SCHREGNOS", $model->schregnos);
    knjCreateHidden($objForm, "PERFECT", $model->field["PERFECT"]);
    knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
}
