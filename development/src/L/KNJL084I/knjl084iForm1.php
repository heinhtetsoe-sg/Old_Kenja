<?php
class knjl084iForm1
{

    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //入試制度
        $query = knjl084iQuery::getNameMst($model->ObjYear, "L003");
        $extra = "onChange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1, "", "", "");

        //入試区分
        $query = knjl084iQuery::getTestdivMst($model);
        $extra = "onChange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1, "", "", "");

        //受験班
        $query = knjl084iQuery::getHallYdat($model);
        $extra = "onChange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "EXAMHALLCD", $model->examhallcd, $extra, 1, "", "ALL", "");

        //受験科目
        $query = knjl084iQuery::getTestsubclasscd($model);
        $extra = "onChange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "TESTSUBCLASSCD", $model->testsubclasscd, $extra, 1, "BLANK", "", "");

        //満点
        $query = knjl084iQuery::getTestsubclasscd($model, $model->testsubclasscd);
        $subclassRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        knjCreateHidden($objForm, "PERFECT", strlen($subclassRow["PERFECT"]) ? $subclassRow["PERFECT"] : 200);

        //一覧表示
        $receptnoArray = array();
        if ($model->applicantdiv != "" && $model->testdiv != "" && $model->examhallcd != "" && $model->testsubclasscd != "") {
            //データ取得
            $query = knjl084iQuery::selectQuery($model);
            $result = $db->query($query);

            //データが1件もなかったらメッセージを返す
            if ($result->numRows() == 0 ) {
                $model->setWarning("MSG303");
            }

            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");
                $receptno = $row["RECEPTNO"];

                //HIDDENに保持する用
                $receptnoArray[] = $receptno;

                //得点textbox
                $disScore = ($row["ATTEND_FLG"] == "1") ? " disabled" : "";
                $extra = " id=\"SCORE-{$receptno}\" style=\"text-align:right;\" onkeydown=\"goEnter(this);\" onchange=\"changeFlg(this);\" onblur=\"checkScore(this);\" ".$disScore;
                $row["SCORE"] = knjCreateTextBox($objForm, $row["SCORE"], "SCORE-{$receptno}", 3, 3, $extra);

                //欠席checkbox
                $checked = ($row["ATTEND_FLG"] == "1") ? " checked": "";
                $extra = "id=\"ATTEND_FLG-{$receptno}\" onclick=\"changeFlg(this); disScore(this);\" ".$checked;
                $row["ATTEND_FLG"] = knjCreateCheckBox($objForm, "ATTEND_FLG-{$receptno}", "1", $extra);

                $dataflg = true;

                $arg["data"][] = $row;
            }
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model, $dataflg);

        //hidden作成
        makeHidden($objForm, $model, $receptnoArray);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl084iindex.php", "", "main");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl084iForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "", $all = "", $retDiv = "")
{
    $opt = array();
    if ($blank) {
        $opt[] = array("label" => "", "value" => "");
    }
    if ($all) {
        $opt[] = array("label" => "全て", "value" => "ALL");
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

    if ($retDiv == "") {
        $arg["TOP"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
    } else {
        return $opt;
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $dataflg)
{
    $disable  = ($dataflg) ? "" : " disabled";

    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"".$disable;
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"".$disable;
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"return btn_submit('end');\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model, $receptnoArray)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "HID_APPLICANTDIV");
    knjCreateHidden($objForm, "HID_TESTDIV");
    knjCreateHidden($objForm, "HID_EXAMHALLCD");
    knjCreateHidden($objForm, "HID_TESTSUBCLASSCD");
    knjCreateHidden($objForm, "HID_RECEPTNO", implode(",", $receptnoArray));
    knjCreateHidden($objForm, "CHANGE_FLG");

    knjCreateHidden($objForm, "UPD_RECEPTNO"); //更新受験番号
    knjCreateHidden($objForm, "NEXT_ID");      //js内部利用変数
    knjCreateHidden($objForm, "ENTER_FLG");    //js内部利用変数
    knjCreateHidden($objForm, "SET_SC_VAL");   //js内部利用変数
    knjCreateHidden($objForm, "F_IO_CHK");     //js内部利用変数
}
