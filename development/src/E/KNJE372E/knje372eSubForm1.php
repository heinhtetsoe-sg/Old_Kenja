<?php

require_once('for_php7.php');

class knje372eSubForm1
{
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //年組リストを作成する
        $query = knje372eQuery::getHrClass($model, CTRL_YEAR, CTRL_SEMESTER);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "HR_CLASS", $model->subField["HR_CLASS"], $extra, 1, "BLANK");

        /****************/
        /* ラジオボタン */
        /****************/
        //出力取込種別(1:ヘッダ出力（見本）/2:データ取込/3:エラー出力/4:データ出力)
        $opt = array(1, 2, 3, 4);
        $model->subField['OUTPUT'] = $model->subField['OUTPUT'] ? $model->subField['OUTPUT'] : '1';
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"OUTPUT{$val}\" ");
        }
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->subField['OUTPUT'], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //処理名コンボボックス ※データの削除は行わない
        $opt      = array();
        $opt[]    = array("label" => "更新","value" => "1");
        // $opt[]    = array("label" => "削除","value" => "2");
        $extra = "style=\"width:60px;\"";
        $arg["data"]["SHORI_MEI"] = knjCreateCombo($objForm, "SHORI_MEI", $model->subField["SHORI_MEI"], $opt, $extra, 1);

        /********/
        /* FILE */
        /********/
        $extra = "";
        $arg["FILE"] = knjCreateFile($objForm, "FILE", $extra, 1024000);

        /******************/
        /* チェックボックス */
        /******************/
        //ヘッダ有チェックボックス
        if($model->subField["HEADER"] == "on") {
            $check_header = "checked";
        } else {
            $check_header = ($model->cmd == "csv") ? "checked" : "";
        }
        $extra = " id=\"HEADER\"";
        $arg["data"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $check_header.$extra, "");

        /**********/
        /* ボタン */
        /**********/
        //実行
        $extra = "onclick=\"return btn_submit('csvExec');\"";
        $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);
        //終了
        $extra = "onclick=\"parent.closeit();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");

        $arg["IFRAME"] = VIEW::setIframeJs();
        //DB切断
        Query::dbCheckIn($db);

        if ($model->cmd == "csvComp") {
            //データ更新成功時、親画面リフレッシュ
            $arg["jscript"] = " window.onload = function() { parent.btn_submit('edit'); } ";
        }

        //フォーム作成
        $arg["start"]  = $objForm->get_start("csv", "POST", "knje372eindex.php", "", "csv");
        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje372eSubForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

?>
