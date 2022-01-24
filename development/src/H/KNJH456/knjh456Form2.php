<?php

require_once('for_php7.php');

class knjh456Form2
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjh456index.php", "", "edit");

        $model->mockyear = ($model->mockyear) ? $model->mockyear : CTRL_YEAR;

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        /*if (!isset($model->warning) && $model->cmd != "kakutei") {
            $query = knjh456Query::getRow($model->mockyear, $model->mockcd, $model->grade);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }*/

        //年度
        $arg["data"]["YEAR"] = $model->mockyear;

        //模試データ
        $query = knjh456Query::getMockcd($model);
        //$extra = "onChange=\"btnMockDisabled()\"";
        //makeCmb($objForm, $arg, $db, $query, $Row["MOCKCD"], "MOCKCD", $extra, "BLANK");
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["data"]["MOCKCD"] = $row["LABEL"];

        //$query = knjh456Query::getCompanyCd($Row["MOCKCD"]);
        //$model->companyCd = $db->getOne($query);

        //学年
        $query = knjh456Query::getGrade($model);
        //$extra = "onChange=\"btnMockDisabled()\"";
        //makeCmb($objForm, $arg, $db, $query, $Row["GRADE"], "GRADE", $extra, "BLANK");
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["data"]["GRADE"] = $row["LABEL"];

        //リスト作成
        makeList($objForm, $arg, $db, $model);
        
        //コピー用年度
        $mock = mb_substr($model->mockcd, -4);
        $cpYearQuery = knjh456Query::getCopyYear($mock);
        $extra = "";
        $arg["COPYYEAR"] = makeCmb2($objForm, $arg, $db, $cpYearQuery, $model->copyyear, "COPYYEAR", $extra, "BLANK");
        

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if ($model->cmd != "edit" && $model->cmd != "reset" && $model->cmd != "kakutei") {
            $arg["reload"]  = "parent.left_frame.location.href='knjh456index.php?cmd=list';";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh456Form2.html", $arg); 
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
/*function makeList(&$objForm, &$arg, $db, $model, $Row) {
    if (strlen($Row["FIELD_CNT"]) && $Row["FIELD_CNT"] > 0) {
        for ($field_no = 1; $field_no <= $Row["FIELD_CNT"]; $field_no++) {
            $name = "MOCK_SUBCLASS_CD".$field_no;
            //警告メッセージを表示しない場合
            if (!isset($model->warning) && $model->cmd != "kakutei") {
                $query = knjh456Query::getMockSubclassCd($model->mockyear, $model->mockcd, $model->grade, $field_no);
                $Row2[$name] = $db->getOne($query);
            } else {
                $Row2[$name] = $model->field2[$name];
            }
            //模試科目(業者)
            if ($model->companyCd == "00000002") {
                $query = knjh456Query::getMockSubclassTitleBene($model, $field_no);
            } else if ($model->companyCd == "00000001") {
                $query = knjh456Query::getMockSubclassTitleSundai($model, $field_no);
            } else {
                $query = knjh456Query::getMockSubclassTitleZkai($model, $field_no);
            }
            $setSubclassName = $db->getOne($query);
            $setRow["MOSI_SUBCLASS_NAME"] = $setSubclassName;
            //模試科目(賢者)
            $query = knjh456Query::getMockSubclassMst();
            $extra = "onChange=\"return btnMockDisabled()\"";
            $setRow["MOCK_SUBCLASS_CD"] = makeCmb2($objForm, $arg, $db, $query, $Row2[$name], $name, $extra, "BLANK");
            //No
            $setRow["FIELD_NO"] = $field_no;

            $arg["data2"][] = $setRow;
        }
    }
}*/

//リスト作成
function makeList(&$objForm, &$arg, $db, $model) {

        //対象教科取得
        $kyoukaQuery = knjh456Query::getKyouka($model->mockyear, $model->mockcd, $model->grade);
        $kyoukaResult = $db->query($kyoukaQuery);
        
        $count = 1;
        
        while($kyoukaRow = $kyoukaResult->fetchRow(DB_FETCHMODE_ASSOC)){
            $setRow["FIELD_NO"] = $count;
            $name = "MOCK_SUBCLASS_CD".$count;
            
            //MOCK_SCHOOL_COMP_DATの科目名
            $setRow["MOSI_SUBCLASS_NAME"] = $kyoukaRow["SUBCLASS_NAME"];
            
            //hiddenに入れる
            knjCreateHidden($objForm, "subclassname".$count, $kyoukaRow["SUBCLASS_NAME"]);
            
            if($model->field2[$name] == ""){
                $model->field2[$name] = $kyoukaRow["SUBCLASS_CD"];
            }
            
            //模試科目(賢者)
            $query = knjh456Query::getMockSubclassMst();
            $extra = "onChange=\"return btnMockDisabled()\"";
            $setRow["MOCK_SUBCLASS_CD"] = makeCmb2($objForm, $arg, $db, $query, $model->field2[$name], $name, $extra, "BLANK");
            $arg["data2"][] = $setRow;
            $count++;
        }
        
        //科目数を保持するためのhidden
        knjCreateHidden($objForm, "kamokucount", $count);
}
//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //確定ボタン
    //$extra = "onclick=\"return btn_submit('kakutei');\"";
    //$arg["button"]["btn_kakutei"] = knjCreateBtn($objForm, "btn_kakutei", "確 定", $extra);

    //コピーボタン
    $extra = "onclick=\"return btn_submit('copy');\"";
    $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "左の年度から割り当て", $extra);

    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

    //削除ボタン
    //$extra = "onclick=\"return btn_submit('delete');\"";
    //$arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);

    //クリアボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

    //実行ボタン
    /*$disabled = $model->cmd != "kakutei" ? "" : " disabled ";
    $extra = "onclick=\"return btn_submit('makeMock');\" style=\"width:200px\"".$disabled;
    $arg["button"]["btn_mock"] = knjCreateBtn($objForm, "btn_mock", "模試データ作成(業者→賢者)", $extra);*/
}
?>
