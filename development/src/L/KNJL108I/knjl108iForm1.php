<?php
class knjl108iForm1
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
        $query = knjl108iQuery::getNameMst($model->ObjYear, "L003");
        $extra = "onChange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1, "", "");

        //一括チェック(一次手続き)
        $extra = "id=\"ALLCHECK_BD022_REMARK1\" onClick=\"return check_all(this);\"";
        $arg["TOP"]["ALLCHECK_BD022_REMARK1"] = knjCreateCheckBox($objForm, "ALLCHECK_BD022_REMARK1", "1", $extra);

        //一括チェック(二次手続き)
        $extra = "id=\"ALLCHECK_PROCEDUREDIV\" onClick=\"return check_all(this);\"";
        $arg["TOP"]["ALLCHECK_PROCEDUREDIV"] = knjCreateCheckBox($objForm, "ALLCHECK_PROCEDUREDIV", "1", $extra);

        //一括チェック(招集日)
        $extra = "id=\"ALLCHECK_BD022_REMARK3\" onClick=\"return check_all(this);\"";
        $arg["TOP"]["ALLCHECK_BD022_REMARK3"] = knjCreateCheckBox($objForm, "ALLCHECK_BD022_REMARK3", "1", $extra);

        //入試区分
        $query = knjl108iQuery::getTestdivMst($model);
        $extra = "onChange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1, "", "");

        //入辞区分コンボのリストを取得
        $query = knjl108iQuery::getEntdiv($model->ObjYear, $model->applicantdiv, "L012");
        $extra = "";
        $entdivOpt = makeCmb($objForm, $arg, $db, $query, "ENTDIV", $model->entdiv, $extra, 1, "BLANK", "1");

        //一覧表示
        $examnoArray = array();
        if ($model->applicantdiv != "" && $model->testdiv != "") {
            //データ取得
            $query = knjl108iQuery::selectQuery($model);
            $result = $db->query($query);

            //データが1件もなかったらメッセージを返す
            if ($result->numRows() == 0) {
                $model->setWarning("MSG303");
            }

            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");
                $examNo = $row["EXAMNO"];

                //HIDDENに保持する用
                $examnoArray[] = $examNo;

                //一次手続きチェック
                $checked = ($row["BD022_REMARK1"] == "1") ? " checked": "";
                $extra = "id=\"BD022_REMARK1-{$examNo}\" onclick=\"changeFlg(this); clearDate(this);\" ".$checked;
                $row["BD022_REMARK1"] = knjCreateCheckBox($objForm, "BD022_REMARK1-{$examNo}", "1", $extra);

                //一次手続き日
                $row["BD022_REMARK2"] = str_replace("-", "/", $row["BD022_REMARK2"]);
                $row["BD022_REMARK2"] = View::popUpCalendar($objForm, "BD022_REMARK2-{$examNo}", $row["BD022_REMARK2"]);

                //二次手続きチェック
                $checked = ($row["PROCEDUREDIV"] == "1") ? " checked": "";
                $extra = "id=\"PROCEDUREDIV-{$examNo}\" onclick=\"changeFlg(this); clearDate(this);\" ".$checked;
                $row["PROCEDUREDIV"] = knjCreateCheckBox($objForm, "PROCEDUREDIV-{$examNo}", "1", $extra);

                //二次手続き日
                $row["PROCEDUREDATE"] = str_replace("-", "/", $row["PROCEDUREDATE"]);
                $row["PROCEDUREDATE"] = View::popUpCalendar($objForm, "PROCEDUREDATE-{$examNo}", $row["PROCEDUREDATE"]);

                //招集日チェック
                $checked = ($row["BD022_REMARK3"] == "1") ? " checked": "";
                $extra = "id=\"BD022_REMARK3-{$examNo}\" onclick=\"changeFlg(this);\" ".$checked;
                $row["BD022_REMARK3"] = knjCreateCheckBox($objForm, "BD022_REMARK3-{$examNo}", "1", $extra);

                //入辞区分コンボ
                $extra = "id=\"ENTDIV-{$examNo}\" onchange=\"changeFlg(this);\" ";
                $row["ENTDIV"] = knjCreateCombo($objForm, "ENTDIV-{$examNo}", $row["ENTDIV"], $entdivOpt, $extra, 1);

                $dataflg = true;

                $arg["data"][] = $row;
            }
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model, $dataflg);

        //hidden作成
        makeHidden($objForm, $model, $examnoArray);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl108iindex.php", "", "main");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl108iForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "", $retOpt = "")
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

    if ($retOpt) {
        return $opt;
    } else {
        $arg["TOP"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
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
function makeHidden(&$objForm, $model, $examnoArray)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "HID_APPLICANTDIV");
    knjCreateHidden($objForm, "HID_TESTDIV");
    knjCreateHidden($objForm, "HID_EXAMHALLCD");
    knjCreateHidden($objForm, "HID_EXAMNO", implode(",", $examnoArray));
    knjCreateHidden($objForm, "CHANGE_FLG");
}
