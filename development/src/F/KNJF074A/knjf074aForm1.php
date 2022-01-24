<?php

require_once('for_php7.php');

class knjf074aForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjf074aForm1", "POST", "knjf074aindex.php", "", "knjf074aForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //今年度・今学期名及びタイトルの表示
        $arg["data"]["YEAR_SEMESTER"] = CTRL_YEAR."年度　" .CTRL_SEMESTERNAME ."　<b>健康診断統計表用ＣＳＶ</b>";

        //ラジオボタン(1:体格統計用CSV 2:疾病統計用CSV)
        $opt = array(1, 2);
        $model->field["OUTPUT_CSV"] = ($model->field["OUTPUT_CSV"] == "") ? "1" : $model->field["OUTPUT_CSV"];
        $extra = array("id=\"OUTPUT_CSV1\" onchange=\"return check_csv_radio();\"", "id=\"OUTPUT_CSV2\" onchange=\"return check_csv_radio();\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT_CSV", $model->field["OUTPUT_CSV"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //ラジオボタン(1:年齢別 2:性別)
        $opt = array(1, 2);
        $model->field["OUTPUT_TYPE"] = ($model->field["OUTPUT_TYPE"] == "") ? "1" : $model->field["OUTPUT_TYPE"];
        if ($model->field["OUTPUT_CSV"] == "1") {
            $extra = array("id=\"OUTPUT_TYPE1\" disabled", "id=\"OUTPUT_TYPE2\" disabled");
        } else {
            $extra = array("id=\"OUTPUT_TYPE1\"", "id=\"OUTPUT_TYPE2\"");
        }
        $radioArray = knjCreateRadio($objForm, "OUTPUT_TYPE", $model->field["OUTPUT_TYPE"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //年度テキストボックスを作成する
        $extra = "";
        $query = knjf074aQuery::getSelectYear($model);
        $result = $db->query($query);
        $chkDefaultYear = false;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[]= array('label' => $row["LABEL"]."年度",
                           'value' => $row["VALUE"]);
            if ($row["VALUE"] == CTRL_YEAR) {
                $chkDefaultYear = true;
            }
        }
        if ($model->field["YEAR"] == "" && get_count($opt) > 0) {
            if ($chkDefaultYear) {
                $model->field["YEAR"] = CTRL_YEAR;
            } else {
                $model->field["YEAR"] = $opt[0];
            }
        }

        //年度
        $arg["data"]["YEAR"] = knjCreateCombo($objForm, "YEAR", $model->field["YEAR"], $opt, $extra, 1);

        //プレビュー／印刷ボタン
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", "onclick=\"return newwin('" . SERVLET_URL . "');\"");

        //csvボタン
        $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", "onclick=\"return btn_submit('csv');\"");

        //終了ボタン
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");

        //hidden作成
        knjCreateHidden($objForm, "PRGID", "KNJF074A");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "SCHOOLCD", SCHOOLCD);
        knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
        knjCreateHidden($objForm, "useSpecial_Support_School", $model->Properties["useSpecial_Support_School"]);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjf074aForm1.html", $arg);
    }
}
