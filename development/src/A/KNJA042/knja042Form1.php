<?php

require_once('for_php7.php');

/********************************************************************/
/* 在校生クラス割振りデータ出力                     山城 2006/03/10 */
/*                                                                  */
/* 変更履歴                                                         */
/* NO001 :                                          name yyyy/mm/dd */
/********************************************************************/

class knja042Form1
{
    function main(&$model)
    {
        $objForm = new form;
        $arg["start"] = $objForm->get_start("main", "POST", "knja042index.php", "", "main");

        //権限チェック
        if ($model->auth != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        //DB接続
        $db = Query::dbCheckOut();

        $securityCnt = $db->getOne(knja042Query::getSecurityHigh());
        //セキュリティーチェック
        if (!$model->getPrgId && $model->Properties["useXLS"] && $securityCnt > 0) {
            $arg["jscript"] = "OnSecurityError();";
        }

        //年度学期表示
        $arg["YEAR"] = CTRL_YEAR+1;

        //学年コンボ
        $query = knja042Query::getGrade($model);
        makeCmb($objForm, $arg, $db, $query, $model->grade, "GRADE", "", "ALL");

        //ボタン作成
        makeButton($objForm, $arg, $model, $db);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJA042");
        knjCreateHidden($objForm, "TEMPLATE_PATH");
        knjCreateHidden($objForm, "XLS_EXAMYEAR", $model->examyear);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knja042Form1.html", $arg);
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
        $model->schoolCd = $db->getOne(knja042Query::getSchoolCd());
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "', '" . $model->schoolCd . "', '" . $model->Properties["xlsVer"] . "');\"";
        $arg["TITLE"] = "クラス編成結果エクセル出力";
    } else {
        $extra = "onclick=\"return btn_submit('exec');\"";
        $arg["TITLE"] = "クラス編成結果ＣＳＶ出力";
    }
    $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);

    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

?>
