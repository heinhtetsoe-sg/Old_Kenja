<?php
require_once('for_php7.php');

class knjh725Form1
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjh725index.php", "", "main");

        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学力テスト区分コンボボックス
        $this->makeTestDivCombobox($objForm, $arg, $db, $model);

        //合計チェックボックス
        $extra  = " id=\"total\" onchange=\"OnSumCheckChanged(this);\" ";
        $extra .= $model->field["TESTDIV"] === "9" ? "checked" : "";
        $arg["data"]["TOTAL"] = knjCreateCheckBox($objForm, "TOTAL", "1", $extra);

        //ボタン作成
        $this->makeBtn($objForm, $arg);

        //実行履歴
        $this->makeListRireki($arg, $db);

        //hidden作成
        $this->makeHidden($objForm);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh725Form1.html", $arg);
    }

    //学力テスト区分コンボボックス
    public function makeTestDivCombobox(&$objForm, &$arg, $db, $model)
    {
        $opt = array();
        $query = knjh725Query::getNameMst("H320");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
        }
        $result->free();

        $extra  = "";
        $extra .= "id=\"testdiv\" ";
        $extra .= $model->field["TESTDIV"] === "9" ? "disabled" : "";

        $arg["data"]["TESTDIV"] = knjCreateCombo($objForm, "TESTDIV", $model->field["TESTDIV"], $opt, $extra, 1);
    }

    //実行履歴
    public function makeListRireki(&$arg, $db)
    {
        $query  = knjh725Query::getListRireki();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row["CALC_DATE"] = str_replace("-", "/", $row["CALC_DATE"]);
            $arg["rireki"][] = $row;
        }
        $result->free();
    }

    //ボタン作成
    public function makeBtn($objForm, &$arg)
    {
        //実行ボタン
        $extra = "onclick=\"return btn_submit('exec');\"";
        $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
    }

    //hidden作成
    public function makeHidden($objForm)
    {
        knjCreateHidden($objForm, "cmd");
    }
}
