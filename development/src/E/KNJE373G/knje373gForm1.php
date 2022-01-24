<?php

require_once('for_php7.php');

class knje373gForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("knje373gForm1", "POST", "knje373gindex.php", "", "knje373gForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        if (!isset($model->field["YEAR"])) {
            $model->field["YEAR"] = CTRL_YEAR;
        }
        $extra  = "style=\"text-align:right;\" ";
        $extra .= "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["YEAR"] = knjCreateTextBox($objForm, $model->field["YEAR"], "YEAR", 4, 4, $extra);

        //出力タイプ選択ラジオボタン 1:進学実績 2:専門・各種学校別進学実績 3:内定企業一覧
        $model->field["OUTDIV"] = $model->field["OUTDIV"] ? $model->field["OUTDIV"] : '1';
        $opt_brdiv = array(1, 2, 3);
        $extra = array("id=\"OUTDIV1\"", "id=\"OUTDIV2\"", "id=\"OUTDIV3\"");
        $radioArray = knjCreateRadio($objForm, "OUTDIV", $model->field["OUTDIV"], $extra, $opt_brdiv, get_count($opt_brdiv));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //年間
        if (!isset($model->field["YEARS"])) {
            $model->field["YEARS"] = 1;
        }
        $extra  = "style=\"text-align:right;\" ";
        $extra .= "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["YEARS"] = knjCreateTextBox($objForm, $model->field["YEARS"], "YEARS", 2, 2, $extra);

        //ＣＳＶ出力ボタン
        $extra = "onclick=\"return btn_submit('csv');\"";
        $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje373gForm1.html", $arg);
    }
}
