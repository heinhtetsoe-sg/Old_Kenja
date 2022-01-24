<?php
class knjl122iSubForm1
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]  = $objForm->get_start("sel", "POST", "knjl122iindex.php", "", "sel");

        //DB接続
        $db = Query::dbCheckOut();

        //情報
        $appRow = $db->getRow($query = knjl122iQuery::getNameMst($model->ObjYear, "L003", $model->applicantdiv), DB_FETCHMODE_ASSOC);
        $testRow = $db->getRow($query = knjl122iQuery::getTestdivMst($model, $model->testdiv), DB_FETCHMODE_ASSOC);
        $hallRow = $db->getRow($query = knjl122iQuery::getHallYdat($model, $model->examhallcd), DB_FETCHMODE_ASSOC);
        $arg["INFO"] = $model->ObjYear."年度"."　".$appRow["NAME1"]."　".$testRow["TESTDIV_NAME"]."　".$hallRow["EXAMHALL_NAME"];

        //生徒リストToリスト作成
        makeListToList($objForm, $arg, $db, $model);

        //入試区分B方式か？
        $isTestdivB = ($model->testdiv == "2") ? true : false;
        $arg["isTestdivB"] = ($isTestdivB) ? "1" : "";
        knjCreateHidden($objForm, "IS_TESTDIV_B", $isTestdivB);

        //B方式は3つの面接評価
        if ($isTestdivB) {
            $nameArray = array("INTERVIEW_A", "INTERVIEW_B", "INTERVIEW_C");
        } else {
            $nameArray = array("INTERVIEW_A", "INTERVIEW_B");
        }

        //面接評価comboboxのリストを取得
        $interviewArray = array();
        $opt_interview = array();
        $query = knjl122iQuery::getInterview($model, "LH27"); // 高校用を取得
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $interviewArray[] = array("VALUE" => $row["VALUE"], "LABEL" => $row["LABEL"]);
            $opt_interview[] = $row["VALUE"];
        }
        $result->free();

        //チェックボックスALL
        $extra  = "onClick=\"return check_all(this);\"";
        $extra .= ($model->replace_data["CHECK_ALL"] == "1") ? " checked" : "";
        $arg["data"]["CHECK_ALL"] = knjCreateCheckBox($objForm, "CHECK_ALL", "1", $extra, "");

        //項目
        $setRow = array();
        foreach($nameArray as $nameKey => $name) {
            //チェックボックス
            $extra = ($model->replace_data["check"][$name] == "1") ? " checked" : "";
            $arg["data"]["CHECK_".$name] = knjCreateCheckBox($objForm, "CHECK_".$name, "1", $extra, "");

            //面接評価ラジオボタン
            if ($model->replace_data["field"][$name] == "" && count($interviewArray) > 0) $model->replace_data["field"][$name] = $interviewArray[0]["VALUE"]; //初期値
            foreach($interviewArray as $key => $val) {
                $id = $name."-".$val["VALUE"];
                $objForm->ae( array("type"       => "radio",
                                    "name"       => $name,
                                    "value"      => $model->replace_data["field"][$name],
                                    "extrahtml"  => "id=\"{$id}\" onclick=\"changeFlg();\" ",
                                    "multiple"   => $opt_interview ) );
                $setRow[$name] .= $objForm->ge($name, $val["VALUE"]) . "<LABEL for=\"{$id}\">" . $val["LABEL"] . "</LABEL>" . " ";
            }
            $arg["data"][$name] = $setRow[$name];
        }

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl122iSubForm1.html", $arg);
    }
}

//リストtoリスト作成
function makeListToList(&$objForm, &$arg, $db, $model) {
    //志願者一覧
    $query = knjl122iQuery::selectQuery($model, "replace");
    $result = $db->query($query);
    $opt1 = array();
    $opt2 = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt1[] = array('label' => $row["EXAMNO"]."　".$row["NAME"],
                        'value' => $row["EXAMNO"]);
    }
    $result->free();

    //志願者一覧
    $extra = "multiple style=\"width:100%\" width=\"100%\" ondblclick=\"move1('left')\"";
    $arg["main_part"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "category_name", "", $opt1, $extra, 20);

    //対象者一覧
    $extra = "multiple style=\"width:100%\" width=\"100%\" ondblclick=\"move1('right')\"";
    $arg["main_part"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "category_selected", "", $opt2, $extra, 20);

    //対象選択ボタンを作成する
    $extra = "style=\"height:20px;width:35px\" onclick=\"moves('right');\"";
    $arg["main_part"]["SEL_DEL_ALL"] = knjCreateBtn($objForm, "sel_del_all", ">>", $extra);
    //対象取消ボタンを作成する
    $extra = "style=\"height:20px;width:35px\" onclick=\"moves('left');\"";
    $arg["main_part"]["SEL_ADD_ALL"] = knjCreateBtn($objForm, "sel_add_all", "<<", $extra);
    //対象選択ボタンを作成する
    $extra = "style=\"height:20px;width:35px\" onclick=\"move1('right');\"";
    $arg["main_part"]["SEL_DEL"] = knjCreateBtn($objForm, "sel_del", "＞", $extra);
    //対象取消ボタンを作成する
    $extra = "style=\"height:20px;width:35px\" onclick=\"move1('left');\"";
    $arg["main_part"]["SEL_ADD"] = knjCreateBtn($objForm, "sel_add", "＜", $extra);
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {

    //更新ボタンを作成する
    $extra = "onclick=\"return doSubmit()\"";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

    //戻るボタンを作成する
    $extra = "onclick=\"return btn_submit('back');\"";
    $arg["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "HID_APPLICANTDIV", $model->applicantdiv);
    knjCreateHidden($objForm, "HID_TESTDIV", $model->testdiv);
    knjCreateHidden($objForm, "HID_EXAMHALLCD", $model->examhallcd);
}
?>
