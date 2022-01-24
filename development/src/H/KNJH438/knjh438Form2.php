<?php

require_once('for_php7.php');

class knjh438Form2
{
    function main(&$model)
    {
        $objForm = new form();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjh438index.php", "", "edit");

        $model->mockyear = ($model->mockyear) ? $model->mockyear : CTRL_YEAR;

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && $model->cmd != "kakutei") {
            $query = knjh438Query::getRow($model->mockyear, $model->mockcd, $model->grade);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //年度
        $arg["data"]["YEAR"] = $model->mockyear;

        //模試データ
        $model->field["MOCKCD"] = $Row["MOCKCD"];
        $arg["data"]["MOCKCD"] = $Row["MOCKNAME1"];

        $query = knjh438Query::getCompanyCd($Row["MOCKCD"]);
        $model->companyCd = $db->getOne($query);

        //学年
        if ($Row["GRADE"] != "") {
            $model->field["GRADE"] = sprintf("%02d", $Row["GRADE"]);
            $query = knjh438Query::getGrade($model);
            $grade = $db->getOne($query);
            $arg["data"]["GRADE"] = $grade;
        }

        //科目数
        $model->field["FIELD_CNT"] = $Row["FIELD_CNT"];
        $arg["data"]["FIELD_CNT"] = $Row["FIELD_CNT"];

        //リスト作成
        makeList($objForm, $arg, $db, $model, $Row);

        //コピー用年度
        $mock = mb_substr($model->mockcd, -4);
        $cpYearQuery = knjh438Query::getCopyYear($mock);
        $extra = "";
        $arg["KCOPYYEAR"] = makeCmb2($objForm, $arg, $db, $cpYearQuery, $model->kcopyyear, "KCOPYYEAR", $extra, "BLANK");


        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "MOCKCD", $model->field["MOCKCD"]);
        knjCreateHidden($objForm, "GRADE", $model->field["GRADE"]);
        knjCreateHidden($objForm, "FIELD_CNT", $model->field["FIELD_CNT"]);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if ($model->cmd != "edit" && $model->cmd != "reset" && $model->cmd != "kakutei") {
            $arg["reload"]  = "parent.left_frame.location.href='knjh438index.php?cmd=list';";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh438Form2.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $blank = "")
{
    $opt = array();
    $result = $db->query($query);
    if ($blank == "BLANK") {
        $opt[] = array ("label" => "",
                        "value" => "");
    }

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
    }
    $result->free();

    $value = ($value) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, 1);
}

//コンボ作成2
function makeCmb2(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $blank = "")
{
    $opt = array();
    $result = $db->query($query);
    if ($blank == "BLANK") {
        $opt[] = array ("label" => "",
                        "value" => "");
    }

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
    }
    $result->free();

    $value = ($value) ? $value : $opt[0]["value"];

    return knjCreateCombo($objForm, $name, $value, $opt, $extra, 1);
}

//リスト作成
function makeList(&$objForm, &$arg, $db, $model, $Row)
{
    if (strlen($Row["FIELD_CNT"]) && $Row["FIELD_CNT"] > 0) {
        for ($field_no = 1; $field_no <= $Row["FIELD_CNT"]; $field_no++) {
            $name = "MOCK_SUBCLASS_CD".$field_no;
            //警告メッセージを表示しない場合
            if (!isset($model->warning) && $model->cmd != "kakutei" && $model->cmd != "kcopy") {
                $query = knjh438Query::getMockSubclassCd($model->mockyear, $model->mockcd, $model->grade, $field_no);
                $Row2[$name] = $db->getOne($query);
            } else {
                $Row2[$name] = $model->field2[$name];
            }
            //模試科目(業者)
            if ($model->companyCd == "00000002") {
                $query = knjh438Query::getMockSubclassTitleBene($model, $field_no);
            } elseif ($model->companyCd == "00000001") {
                $query = knjh438Query::getMockSubclassTitleSundai($model, $field_no);
            } elseif ($model->companyCd == "00000004") {
                $query = knjh438Query::getMockSubclassTitleKawai($model, $field_no);
            } else {
                $query = knjh438Query::getMockSubclassTitleZkai($model, $field_no);
            }
            $setSubclassName = $db->getOne($query);
            $setRow["MOSI_SUBCLASS_NAME"] = $setSubclassName;
            //模試科目(賢者)
            $query = knjh438Query::getMockSubclassMst();
            $extra = "onChange=\"return btnMockDisabled()\"";
            $setRow["MOCK_SUBCLASS_CD"] = makeCmb2($objForm, $arg, $db, $query, $Row2[$name], $name, $extra, "BLANK");
            //No
            $setRow["FIELD_NO"] = $field_no;

            $arg["data2"][] = $setRow;
        }
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //確定ボタン
    //$extra = "onclick=\"return btn_submit('kakutei');\"";
    //$arg["button"]["btn_kakutei"] = knjCreateBtn($objForm, "btn_kakutei", "確 定", $extra);

    //コピーボタン
    $extra = "onclick=\"return btn_submit('kcopy');\"";
    $arg["button"]["btn_kcopy"] = knjCreateBtn($objForm, "btn_kcopy", "左の年度から割り当て", $extra);

    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

    //削除ボタン
    $extra = "onclick=\"return btn_submit('delete');\"";
    //$arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);

    //クリアボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

    //実行ボタン
    $disabled = $model->cmd != "kakutei" ? "" : " disabled ";
    $extra = "onclick=\"return btn_submit('makeMock');\" style=\"width:200px\"".$disabled;
    $arg["button"]["btn_mock"] = knjCreateBtn($objForm, "btn_mock", "模試データ作成(業者→賢者)", $extra);
}
?>
