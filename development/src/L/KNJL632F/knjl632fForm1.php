<?php
class knjl632fForm1
{
    public function main(&$model)
    {
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        /******************/
        /* コンボボックス */
        /******************/

        //入試年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        //入試制度
        $query = knjl632fQuery::getNameMst($model->ObjYear, "L003", "2"); //2:高等学校 (固定)
        $extra = " onChange=\"btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->field["APPLICANTDIV"], $extra, 1, "");

        //処理名
        $opt   = array();
        $opt[] = array("label" => "更新","value" => "1"); //更新 (固定)
        $extra = "style=\"width:60px;\"";
        $arg["data"]["SHORI_MEI"] = knjCreateCombo($objForm, "SHORI_MEI", $model->field["SHORI_MEI"], $opt, $extra, 1);

        /********************/
        /* チェックボックス */
        /********************/
        //ヘッダ有
        $extra = ($model->field["HEADER"] == "on" || $model->cmd == "") ? "checked" : "";
        $extra .= " id=\"HEADER\"";
        $arg["data"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $extra, "");

        /****************/
        /* ラジオボタン */
        /****************/
        //出力取込種別 (1:ヘッダ出力 2:データ取込 3:エラー出力)
        $opt = array(1, 2, 3);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"OUTPUT{$val}\"");
        }
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        /************/
        /* ファイル */
        /************/
        //ファイルからの取り込み
        $extra = "";
        $arg["FILE"] = knjCreateFile($objForm, "FILE", 1024000, $extra);

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

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjl632findex.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl632fForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank) {
        $opt[] = array("label" => "", "value" => "");
    }
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }

        if ($row["NAMESPARE2"] && $default_flg) {
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[$default]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
