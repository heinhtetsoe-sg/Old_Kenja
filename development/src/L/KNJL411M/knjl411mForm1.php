<?php
require_once('for_php7.php');
class knjl411mForm1
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR + 1;

        /********************/
        /* チェックボックス */
        /********************/
        //ヘッダ有
        //$extra = ($model->field["HEADER"] == "on") ? "checked" : "";
        $extra .= "checked id=\"HEADER\"";
        $arg["data"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $extra, "");
                
        /****************/
        /* ラジオボタン */
        /****************/
        //出力取込種別(1:ヘッダ出力 2:データ取込/3:エラー出力)
        $opt = array(1, 2, 3);
        $model->field['OUTPUT'] = $model->field['OUTPUT'] ? $model->field['OUTPUT'] : '1';
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"OUTPUT{$val}\" ");
        }
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field['OUTPUT'], $extra, $opt, count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //CSV種別(1:帰国生入試/2:一般入試)
        $opt = array(1, 2);
        $model->field['EXAM'] = $model->field['EXAM'] ? $model->field['EXAM'] : '1';
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"EXAM{$val}\" ");
        }
        $radioArray = knjCreateRadio($objForm, "EXAM", $model->field['EXAM'], $extra, $opt, count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        /********/
        /* FILE */
        /********/
        $extra = "";
        $arg["FILE"] = knjCreateFile($objForm, "FILE", $extra, 1024000);

        /**********/
        /* ボタン */
        /**********/
        //実行
        $extra = "onclick=\"return btn_submit('exec');\"";
        $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);

        $arg["IFRAME"] = VIEW::setIframeJs();
        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"]  = $objForm->get_start("csv", "POST", "knjl411mindex.php", "", "csv");
        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl411mForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
