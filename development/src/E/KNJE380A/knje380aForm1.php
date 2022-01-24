<?php
require_once('for_php7.php');

class knje380aForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knje380aindex.php", "", "main");

        //権限チェック
        if ($model->auth != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        //DB接続
        $db = Query::dbCheckOut();

        $securityCnt = $db->getOne(knje380aQuery::getSecurityHigh());
        //セキュリティーチェック
        if (!$model->getPrgId && $model->Properties["useXLS"] && $securityCnt > 0) {
            $arg["jscript"] = "OnSecurityError();";
        }

        //学科コンボボックス
        $result = $db->query(knje380aQuery::getSelectFieldSQL($model));
        $opt_majorcd = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_majorcd[] = array("label" => $row["MAJORNAME"],
                                   "value" => $row["MAJORCD"]);
        }
        $result->free();
        $extra = "";
        $arg["data"]["MAJORCD"] = knjCreateCombo($objForm, "MAJORCD", $model->field["MAJORCD"], $opt_majorcd, $extra, 1);

        //既卒含むチェックボックス
        $extra = "id=\"HEADER\"".$check_header;
        $arg["data"]["GRD"] = knjCreateCheckBox($objForm, "GRD", "on", $extra);

        //実行ボタン
        $extra = "onclick=\"return btn_submit('csv');\"";
        $arg["data"]["YEAR_SEMESTER"] = CTRL_YEAR."年度　" .CTRL_SEMESTERNAME ."　進路状況調査票データ出力";
        $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "CSV出力", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJE380A");
        knjCreateHidden($objForm, "TEMPLATE_PATH");
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje380aForm1.html", $arg);
    }
}
