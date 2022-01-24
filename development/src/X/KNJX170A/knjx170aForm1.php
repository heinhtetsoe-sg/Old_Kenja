<?php

require_once('for_php7.php');

class knjx170aForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjx170aindex.php", "", "main");

        //権限チェック
        if ($model->auth != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        //ＤＢ接続
        $db = Query::dbCheckOut();

        $securityCnt = $db->getOne(knjx170aQuery::getSecurityHigh());
        //セキュリティーチェック
        if (!$model->getPrgId && $model->Properties["useXLS"] && $securityCnt > 0) {
            $arg["jscript"] = "OnSecurityError();";
        }

        //処理名コンボボックス
        $opt_shori      = array();
        $opt_shori[]    = array("label" => "追加","value" => "1");
        $opt_shori[]    = array("label" => "削除","value" => "2");
        $extra = "style=\"width:60px;\"";
        $arg["data"]["SHORI_MEI"] = knjCreateCombo($objForm, "SHORI_MEI", $model->field["SHORI_MEI"], $opt_shori, $extra, 1);

        //年度コンボ
        $result = $db->query(knjx170aquery::getSelectYear());
        $opt_year = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_year[] = array("label" => $row["YEAR"]."年度 ",
                                "value" => $row["YEAR"]);
        }
        $result->free();
        $model->field["YEAR"] = ($model->field["YEAR"] == "") ? CTRL_YEAR : $model->field["YEAR"];
        $arg["data"]["YEAR"] = knjCreateCombo($objForm, "YEAR", $model->field["YEAR"], $opt_year, "onchange=\"btn_submit('');\"", 1);

        //ヘッダ有チェックボックス
        if ($model->field["HEADER"] == "on") {
            $check_header = "checked";
        } else {
            $check_header = ($model->cmd == "") ? "checked" : "";
        }
        $extra = "id=\"HEADER\"".$check_header;
        $arg["data"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $extra);

        //出力取込種別ラジオボタン(1:ヘッダ出力 2:データ取込 3:エラー出力 4:データ出力)
        $opt = array(1, 2, 3);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT2\"", "id=\"OUTPUT3\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //ファイルからの取り込み
        $arg["FILE"] = knjCreateFile($objForm, "FILE", "", 1024000);

        //テンプレート書出し
        $arg["btn_output"] = knjCreateBtn($objForm, "btn_output", "テンプレート書出し", "onclick=\"return btn_submit('output');\"");

        //実行ボタン
        if ($model->Properties["useXLS"]) {
            $model->schoolCd = $db->getOne(knjx170aQuery::getSchoolCd());
            $extra = "onclick=\"return newwin('" . SERVLET_URL . "', '" . $model->schoolCd . "', '" . $model->Properties["xlsVer"] . "');\"";
            $setBtnName = "エクセル";
        } else {
            $extra = "onclick=\"return btn_submit('execute');\"";
            $setBtnName = "ＣＳＶ";
        }
        $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);

        //今年度・今学期名及びタイトルの表示
        $arg["data"]["YEAR_SEMESTER"] = CTRL_YEAR ."年度" ."　選択科目マスタ　".$setBtnName."出力／ＣＳＶ取込";

        //終了ボタンを作成する
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJX170A");
        knjCreateHidden($objForm, "TEMPLATE_PATH");

        //ＤＢ切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjx170aForm1.html", $arg);
    }
}
