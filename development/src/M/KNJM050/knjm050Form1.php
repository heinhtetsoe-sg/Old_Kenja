<?php

require_once('for_php7.php');

/********************************************************************/
/* レポート提出状況                                 山城 2005/07/01 */
/*                                                                  */
/* 変更履歴                                                         */
/* ･NO001：                                         name yyyy/mm/dd */
/********************************************************************/

class knjm050Form1
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjm050Form1", "POST", "knjm050index.php", "", "knjm050Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //親画面なし
        $securityCnt = $db->getOne(knjm050Query::getSecurityHigh());
        //セキュリティーチェック
        if (!$model->getPrgId && $model->Properties["useXLS"] && $securityCnt > 0) {
            $arg["jscript"] = "OnSecurityError();";
        }

        //年度コンボを作成する
        $query = knjm050Query::GetYear($model);
        $extra = "onchange = \"return btn_submit('knjm050')\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["YEAR"], "YEAR", $extra, 1, "");

        //科目コンボを作成する
        $query = knjm050Query::GetChr($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $model->field["SUBCLASSNAME"], "SUBCLASSNAME", $extra, 1, "ALL");

        //ヘッダ有チェックボックス
        if ($model->field["HEADER"] == "on") {
            $check_header = "checked";
        } else {
            $check_header = ($model->cmd == "") ? "checked" : "";
        }
        $extra = "id=\"HEADER\"".$check_header;
        $arg["data"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $extra);

        //実行ボタン
        if ($model->Properties["useXLS"]) {
            $model->schoolCd = $db->getOne(knjm050Query::getSchoolCd());
            $extra = "onclick=\"return newwin('" . SERVLET_URL . "', '" . $model->schoolCd . "', '" . $model->Properties["xlsVer"] . "');\"";
            //タイトルの表示
            $arg["data"]["TITLE"] = "エクセル出力";
        } else {
            $extra = "onclick=\"return btn_submit('csv');\"";
            //タイトルの表示
            $arg["data"]["TITLE"] = "ＣＳＶ出力";
        }
        $arg["button"]["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する(必須)
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJM050");
        knjCreateHidden($objForm, "TEMPLATE_PATH");
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjm050Form1.html", $arg); 

    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    if ($blank == "ALL") {
        $opt[] = array("label" => "全て出力", "value" => "0");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
