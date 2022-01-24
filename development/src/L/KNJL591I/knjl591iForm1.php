<?php
class knjl591iForm1
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->objYear;

        //学科
        $extra = " onchange=\"return btn_submit('main');\" ";
        $opt = array();
        $opt[] = array("label" => "1:普通科", "value" => "1");
        $opt[] = array("label" => "2:工業科", "value" => "2");
        $model->majorcd = ($model->majorcd == "") ? $opt[0]["value"] : $model->majorcd;
        $arg["TOP"]["MAJORCD"] = knjCreateCombo($objForm, "MAJORCD", $model->majorcd, $opt, $extra, "1");

        //開始学籍番号
        $extra = "onblur=\"this.value=toInteger(this.value);\" ";
        $arg["TOP"]["START_HUBAN"] = knjCreateTextBox($objForm, $model->startHuban, "START_HUBAN", 3, 3, $extra);

        //固定学籍番号上5桁
        $koteiNo = $model->objYear.$model->majorcd;

        //一覧表示
        $model->examnoArray = array();
        $query = knjl591iQuery::selectQuery($model);
        $result = $db->query($query);
        $count = 0;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //学籍番号附番
            if ($model->cmd == "huban") {
                $studentno = $koteiNo.sprintf("%03d", $model->startHuban + $count);
            } else {
                if ($model->studentnoArray && isset($model->warning)) {
                    $studentno = $model->studentnoArray[$row["EXAMNO"]];
                } else {
                    $studentno = $row["STUDENTNO"];
                }
            }
            $extra = "onblur=\"this.value=toInteger(this.value);\" onchange=\"changeValue();\" ";
            $row["STUDENTNO"]  = knjCreateTextBox($objForm, $studentno, "STUDENTNO_{$row["EXAMNO"]}", 8, 8, $extra);

            $arg["data"][] = $row;

            $model->examnoArray[] = $row["EXAMNO"];
            $count++;
        }

        //学籍番号附番
        if ($model->cmd == "huban") {
            $model->setMessage("", "画面に表示されている学籍番号はまだ更新されていません。\\n更新する場合は更新ボタンを押してください。");
        }

        //ボタン作成
        makeBtn($objForm, $arg, $count);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl591iindex.php", "", "main");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl591iForm1.html", $arg);
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
    knjCreateHidden($objForm, "KOTEINO", $model->objYear.$model->majorcd);
}
?>
