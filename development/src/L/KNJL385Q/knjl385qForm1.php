<?php

require_once('for_php7.php');

class knjl385qForm1
{
    function main(&$model)
    {
        //権限チェック
        if ($model->auth != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjl385qindex.php", "", "main");
        
        $extraInt   = "onblur=\"this.value=toInteger(this.value)\" ";   //数字に

        //DB接続
        $db = Query::dbCheckOut();
        
        //年度
        $nendoQuery = knjl385qQuery::getNendo();
        $nendoResult = $db->query($nendoQuery);
        $opt = array();
        $opt[0] = array("value"  =>  " ",
                        "label"  =>  " ");
        while($nendoRow = $nendoResult->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt[] = array("value"  =>  $nendoRow["YEAR"],
                           "label"  =>  $nendoRow["YEAR"]);
        }
        $extra = " onchange=\"btn_submit('nendoChange');\"";
        $arg["data"]["NENDO"] = knjCreateCombo($objForm, "NENDO", $model->field["NENDO"], $opt, $extra, "1");


        //年度データチェックに行く
        if($model->cmd == "nendoChange" || $this->cmd == ""){
            
            $query = knjl385qQuery::getSatInfo($model->field["NENDO"], "1");
            $cntRow = $db->getOne($query);
            if($cntRow > 0){
                //年度データ存在するとき
                $model->updateflg = 1;
                $query = knjl385qQuery::getSatInfo($model->field["NENDO"]);
                $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                
                $model->field = array("NENDO"        =>      $row["YEAR"],
                                      "EXAM_DATE"    =>      $row["EXAM_DATE"],
                                      "AMOUNT1"      =>      $row["EXAM_AMOUNT1"],
                                      "AMOUNT2"      =>      $row["EXAM_AMOUNT2"],
                                      "AMOUNT3"      =>      $row["EXAM_AMOUNT3"],
                                     );
            }else{
                $model->updateflg = 0;
                $model->field = array("NENDO"        =>      VARS::post("NENDO") != "" ? VARS::post("NENDO") : CTRL_YEAR,
                                      "EXAM_DATE"    =>      "",
                                      "AMOUNT1"      =>      "",
                                      "AMOUNT2"      =>      "",
                                      "AMOUNT3"      =>      "",
                                     );
            }
            
        }


        //試験日
        $arg["data"]["EXAM_DATE"] = View::popUpCalendar($objForm, "EXAM_DATE", str_replace("-","/",$model->field["EXAM_DATE"]),"");
        
        //受験料（団体）
        $extra = $extraInt;
        $arg["data"]["AMOUNT1"] = knjCreateTextBox($objForm, $model->field["AMOUNT1"], "AMOUNT1", 10, 10, $extra);

        //受験料（個人）
        $arg["data"]["AMOUNT2"] = knjCreateTextBox($objForm, $model->field["AMOUNT2"], "AMOUNT2", 10, 10, $extra);

        //受験料（校内生）
        $arg["data"]["AMOUNT3"] = knjCreateTextBox($objForm, $model->field["AMOUNT3"], "AMOUNT3", 10, 10, $extra);
        
        //ボタン作成
        makeButton($objForm, $arg, $db, $model);


        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJl385q");
        knjCreateHidden($objForm, "TEMPLATE_PATH");
        knjCreateHidden($objForm, "mockRankRangeCnt", $mockRankRangeCnt);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl385qForm1.html", $arg);
    }

}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    if ($name == "YEAR") {
        $value = ($value != "" && $value_flg) ? $value : CTRL_YEAR;
    } else {
        $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    }
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

//ボタン作成
function makeButton(&$objForm, &$arg, $db, $model)
{
    if($model->updateflg != 1){
        //追加ボタン
        $extra = "onclick=\"btn_submit('insert');\"";
        $arg["btn_insert"] = knjCreateBtn($objForm, "btn_insert", "追 加", $extra);
    }else{
        //更新ボタン
        $extra = "onclick=\"btn_submit('update');\"";
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //削除ボタン
        $extra = "onclick=\"btn_submit('delete');\"";
        $arg["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra);
    }

    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
?>
