<?php
class knjl630hForm1
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->objYear;

        //入試日程
        $query = knjl630hQuery::getTestdivMst($model);
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1, "");

        //表示順
        $opt = array(1, 2);
        $model->sort = ($model->sort == "") ? "2" : $model->sort;
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"SORT{$val}\" onchange=\"return btn_submit('main');\"");
        }
        $radioArray = knjCreateRadio($objForm, "SORT", $model->sort, $extra, $opt, count($opt));
        foreach ($radioArray as $key => $val) {
            $arg[$key] = $val;
        }

        //先頭受験番号
        $extra = "onblur=\"this.value=toInteger(this.value);\" ";
        $arg["TOP"]["START_HUBAN"] = knjCreateTextBox($objForm, $model->startHuban, "START_HUBAN", 4, 4, $extra);

        //一覧表示
        $model->examnoArray = array();
        $query = knjl630hQuery::selectQuery($model);
        $result = $db->query($query);
        $count = 0;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //受験番号附番
            if ($model->cmd == "huban") {
                $receptno = sprintf("%04d", $model->startHuban + $count);
            } else {
                if ($model->receptnoArray && isset($model->warning)) {
                    $receptno = $model->receptnoArray[$row["EXAMNO"]];
                } else {
                    $receptno = $row["RECEPTNO"];
                }
            }
            $extra = "onblur=\"this.value=toInteger(this.value);\" onchange=\"changeValue();\" ";
            $row["RECEPTNO"]  = knjCreateTextBox($objForm, $receptno, "RECEPTNO_{$row["EXAMNO"]}", 4, 4, $extra);

            $arg["data"][] = $row;

            $model->examnoArray[] = $row["EXAMNO"];
            $count++;
        }

        //受験番号附番
        if ($model->cmd == "huban") {
            $model->setMessage("", "画面に表示されている受験番号はまだ更新されていません。\\n更新する場合は更新ボタンを押してください。");
        }

        //ボタン作成
        makeBtn($objForm, $arg, $count);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl630hindex.php", "", "main");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl630hForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "", $retDiv = "")
{
    $opt = array();
    $retOpt = array();
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
        $retOpt[$row["VALUE"]] = $row["LABEL"];

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

    if ($retDiv == "") {
        $arg["TOP"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
    } else {
        return $retOpt;
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $count)
{
    $disable  = ($count > 0) ? "" : " disabled";

    //附番ボタン
    $extra = "onclick=\"return btn_submit('huban');\"".$disable;
    $arg["button"]["btn_huban"] = knjCreateBtn($objForm, "btn_huban", "附番", $extra);
    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"".$disable;
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"".$disable;
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"return btn_submit('end');\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model)
{
    $changeFlg = "0";
    if ($model->cmd == "huban" || isset($model->warning)) {
        $changeFlg = "1";
    }

    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "CHANGE_FLG", $changeFlg);
}
?>
