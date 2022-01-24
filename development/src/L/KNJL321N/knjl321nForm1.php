<?php

require_once('for_php7.php');

class knjl321nForm1
{
    public function main(&$model)
    {
        $objForm = new form();
        
        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl321nForm1", "POST", "knjl321nindex.php", "", "knjl321nForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        //入試制度
        $extra = "onchange=\"return btn_submit('knjl321n');\"";
        $query = knjl321nQuery::getNameMst($model, "L003");
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->field["APPLICANTDIV"], "", 1);

        //入試区分コンボの設定
        $extra = "onchange=\"return btn_submit('knjl321n');\"";
        $query = knjl321nQuery::getNameMst($model, "L004");
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->field["TESTDIV"], "", 1);

        //表示順 1:成績順 2:受験番号順
        $opt = array(1, 2);
        $extra  = array("id=\"ORDER1\"", "id=\"ORDER2\"");
        $model->field["ORDER"] = ('' == $model->field["ORDER"]) ? "1" : $model->field["ORDER"];
        $radioArray = knjCreateRadio($objForm, "ORDER", $model->field["ORDER"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //合計点
        $extra = "onblur=\"this.value=toNumber(this.value)\"";
        $arg["data"]["SCORE_TOTAL"] = knjCreateTextBox($objForm, $model->field["SCORE_TOTAL"], "SCORE_TOTAL", 4, 4, $extra);

        //印刷ボタン
        $extra = " onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //ＣＳＶ出力ボタン
        $extra = "onclick=\"return btn_submit('csv');\"";
        $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);
        
        //終了ボタン
        $extra = " onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "ENTEXAMYEAR", $model->ObjYear);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJL321N");
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl321nForm1.html", $arg);
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
