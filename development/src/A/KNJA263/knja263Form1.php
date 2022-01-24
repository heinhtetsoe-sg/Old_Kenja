<?php

require_once('for_php7.php');

class knja263Form1
{
    function main(&$model)
    {
        $objForm = new form;
        $arg["start"] = $objForm->get_start("main", "POST", "knja263index.php", "", "main");
        //DB接続
        $db = Query::dbCheckOut();

        $securityCnt = $db->getOne(knja263Query::getSecurityHigh());
        //セキュリティーチェック
        if (!$model->getPrgId && $model->Properties["useXLS"] && $securityCnt > 0) {
            $arg["jscript"] = "OnSecurityError();";
        }

        //年度学期表示
        $arg["YEAR"] = CTRL_YEAR+1;

        $model->maxSemester = $db->getOne(knja263Query::getMaxSemester($model));
        //学年コンボ
        $query = knja263Query::getGrade($model);
        makeCmb($objForm, $arg, $db, $query, $model->grade, "GRADE", "");

        //ボタン作成
        makeButton($objForm, $arg, $model, $db);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJA263");
        knjCreateHidden($objForm, "TEMPLATE_PATH");
        knjCreateHidden($objForm, "XLS_EXAMYEAR", $model->examyear);
        knjCreateHidden($objForm, "XLS_RECORD_TABLE_DIV", $model->recordTableDiv);
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]    = $objForm->get_finish();

        View::toHTML($model, "knja263Form1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $blank = "")
{
    $result = $db->query($query);
    $opt = array();

    if ($blank == "BLANK") {
        $opt[] = array("label" => "",
                       "value" => "");
    }

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
    }

    $value = ($value) ? $value : $opt[0]["value"];

    if ($blank == "ALL") {
        $opt[] = array("label" => "全て",
                       "value" => "99");
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, 1);
}

//ボタン作成
function makeButton(&$objForm, &$arg, $model, $db)
{
    //実行ボタン
    if ($model->Properties["useXLS"]) {
        $model->schoolCd = $db->getOne(knja263Query::getSchoolCd());
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "', '" . $model->schoolCd . "', '" . $model->Properties["xlsVer"] . "');\"";
        $arg["TITLE"] = "進級者クラス編成用データエクセル出力";
    } else {
        $extra = "onclick=\"return btn_submit('exec');\"";
        $arg["TITLE"] = "進級者クラス編成用データＣＳＶ出力";
    }
    $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);

    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

?>
