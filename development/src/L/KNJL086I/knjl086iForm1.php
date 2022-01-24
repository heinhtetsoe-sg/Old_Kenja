<?php
class knjl086iForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //入試制度
        $query = knjl086iQuery::getNameMst($model->ObjYear, "L003", "1"); // 中学で固定
        $extra = "onChange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1, "");

        //入試区分
        $query = knjl086iQuery::getTestdivMst($model);
        $extra = "onChange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1, "");

        //移動方向
        $opt = array(1, 2);
        $model->move_enter = ($model->move_enter == "") ? "1" : $model->move_enter;
        $extra = array("id=\"MOVE_ENTER1\"", "id=\"MOVE_ENTER2\"");
        $radioArray = knjCreateRadio($objForm, "MOVE_ENTER", $model->move_enter, $extra, $opt, count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["TOP"][$key] = $val;
        }

        //一覧表示
        $examnoArray = array();
        if ($model->applicantdiv != "" && $model->testdiv != "") {
            //データ取得
            $query = knjl086iQuery::selectQuery($model);
            $result = $db->query($query);

            //データが1件もなかったらメッセージを返す
            if ($result->numRows() == 0) {
                $model->setWarning("MSG303");
            }

            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");
                $examNo = $row["EXAMNO"];

                //HIDDENに保持する用
                $examnoArray[] = $row["EXAMNO"];

                //textbox
                for ($remarkNo = 1; $remarkNo <= 7; $remarkNo++) {
                    $extra = " id=\"OTHER_REMARK{$remarkNo}-{$examNo}\" style=\"text-align:right;\" onkeydown=\"keyChangeEntToTab2(this);\" onchange=\"changeFlg(this); checkVal(this, {$remarkNo});\" ";
                    $row["OTHER_REMARK{$remarkNo}"] = knjCreateTextBox($objForm, $row["OTHER_REMARK{$remarkNo}"], "OTHER_REMARK{$remarkNo}-{$examNo}", 3, 1, $extra);
                }

                $dataflg = true;

                $arg["data"][] = $row;
            }
        }

        //textbox
        $textArray = array();
        for ($remarkNo = 1; $remarkNo <= 7; $remarkNo++) {
            $textArray[] = "OTHER_REMARK{$remarkNo}";
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model, $dataflg, $examno);

        //hidden作成
        makeHidden($objForm, $model, $examnoArray, $textArray);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl086iindex.php", "", "main");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl086iForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="", $retDiv="")
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
function makeBtn(&$objForm, &$arg, $model, $dataflg, $examno)
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
function makeHidden(&$objForm, $model, $examnoArray, $textArray)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "HID_APPLICANTDIV");
    knjCreateHidden($objForm, "HID_TESTDIV");
    knjCreateHidden($objForm, "HID_EXAMNO", implode(",", $examnoArray));
    knjCreateHidden($objForm, "HID_TEXT", implode(",", $textArray));
    knjCreateHidden($objForm, "CHANGE_FLG");
}
