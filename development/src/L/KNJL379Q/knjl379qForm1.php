<?php

require_once('for_php7.php');

class knjl379qForm1
{
    function main(&$model)
    {
        //権限チェック
        if ($model->auth != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjl379qindex.php", "", "main");

        $extraInt   = "onblur=\"this.value=toInteger(this.value)\" ";   //数字に


        //DB接続
        $db = Query::dbCheckOut();
        

        //コピー用年度コンボ
        $yearQuery = knjl379qQuery::getCopyYear();
        $yearResult = $db->query($yearQuery);
        $opt = array();
        $exist = array();
        $opt[0] = array("value"    => "",
                        "label"    => "");
        while($yearRow = $yearResult->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt[] = array("value"  =>  $yearRow["YEAR"],
                           "label"  =>  $yearRow["YEAR"]);
        }
        $extra = " ";
        
        $arg["COPY_YEAR"] = knjCreateCombo($objForm, "COPY_YEAR", $model->left_field["COPY_YEAR"], $opt, $extra, 1);

        //試験情報取得
        $arg["CTRL_YEAR"] = CTRL_YEAR;
        $examQuery = knjl379qQuery::getExam();
        $examRow = $db->getRow($examQuery, DB_FETCHMODE_ASSOC);
        if($examRow["EXAM_DATE"] != ""){
            $examdate = explode("-", $examRow["EXAM_DATE"]);
            $weekday = array("日","月","火","水","木","金","土");
            $weekno = date('w', mktime(0, 0, 0, $examdate[2], $examdate[1], $examdate[0]));
            
            $arg["EXAM_DATE"] = $examdate[0]."年".$examdate[1]."月".$examdate[2]."日  (".$weekday[$weekno].") ";
        }

        
        //データ取得
        $dataQuery = knjl379qQuery::getData();
        $dataResult = $db->query($dataQuery);
        $dataArray = array();
        while($dataRow = $dataResult->fetchRow(DB_FETCHMODE_ASSOC)){
            $dataArray[$dataRow["JUDGE"]] = $dataRow;
        }
        //データをmodelにいれる
        foreach($dataArray as $key => $val){
            for($j=0;$j<4;$j++){
                $model->field["FROM_{$j}{$key}"] = $val["FROM_{$j}"];
                $model->field["TO_{$j}{$key}"] = $val["TO_{$j}"];
            }
        }
        
        //成績評価取得
        $judgeQuery = knjl379qQuery::getJudge();
        $judgeResult = $db->query($judgeQuery);
        $judgeCnt = 0;
        
        $argname = array("KENNAI","PREKENNAI","KOKUNAI","KAIGAI");
        
        $extra = $extraInt;
        
        $judgeUse = "";     //jsでのエラーチェックに使用したい
        $jCnm = "";
        
        while($judgeRow = $judgeResult->fetchRow(DB_FETCHMODE_ASSOC)){
            $RowData["SEISEKI"] = $judgeRow["NAME1"];
            
            $judgeUse .= $jCnm.$judgeRow["NAMECD2"];
            $jCnm = ",";
            
            for($i=0;$i<4;$i++){
                $name = $i.$judgeRow["NAMECD2"];

                $RowData["{$argname[$i]}_FROM"] = knjCreateTextBox($objForm, $model->field["FROM_{$name}"], "FROM_".$name, 5, 5, $extra);
                $RowData["{$argname[$i]}_TO"] = knjCreateTextBox($objForm, $model->field["TO_{$name}"], "TO_".$name, 5, 5, $extra);
            }
            
            $arg["data"][] = $RowData;
            $judgeCnt++;
        }
        
        
        //ボタン作成
        makeButton($objForm, $arg, $db, $model);


        //hiddenを作成する
        knjCreateHidden($objForm, "cmd", $model->cmd);
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJl379q");
        knjCreateHidden($objForm, "TEMPLATE_PATH");
        knjCreateHidden($objForm, "judgeUse", $judgeUse);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl379qForm1.html", $arg);
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
    //年度コピーボタン
    $extra = " onclick=\"btn_submit('copy');\"";
    $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "左の年度からコピー", $extra);


    //更新ボタン
    $extra = "onclick=\"btn_submit('update');\"";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

    //総合評定の決定ボタン
    //$extra = "onclick=\"btn_submit('exam_update');\"";
    //$arg["btn_hyotei"] = knjCreateBtn($objForm, "btn_hyotei", "総合評定の決定", $extra);

    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
?>
