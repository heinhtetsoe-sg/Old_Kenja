<?php

require_once('for_php7.php');

class knjl378qForm1
{
    function main(&$model)
    {
        //権限チェック
        if ($model->auth != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjl378qindex.php", "", "main");
        
        $extraInt   = "onblur=\"this.value=toInteger(this.value)\" ";   //数字に

        //DB接続
        $db = Query::dbCheckOut();
        
        //年度
        $arg["CTRL_YEAR"] = CTRL_YEAR;
        
        //試験日
        $examQuery = knjl378qQuery::getExam();
        $examRow = $db->getRow($examQuery, DB_FETCHMODE_ASSOC);
        if($examRow["EXAM_DATE"] != ""){
            $examDate = explode("-", $examRow["EXAM_DATE"]);
            $week = array("日","月","火","水","木","金","土");
            $weekNo = date('w', mktime(0, 0, 0, $examDate[1], $examDate[2], $examDate[0]));
            
            $arg["EXAM_DATE"] = $examDate[0]."年".$examDate[1]."月".$examDate[2]."日 (".$week[$weekNo].")";
        }
        
        //平均点表
        $cntQuery = knjl378qQuery::getCntData();
        $cnt = $db->getOne($cntQuery);
        
        if($cnt > 0){
            $arg["HYOU"] = 1;
            $avgQuery = knjl378qQuery::getAverage();
            $avgResult = $db->query($avgQuery);
            
            while($avgRow = $avgResult->fetchRow(DB_FETCHMODE_ASSOC)){
                $data = $avgRow;
                
                if($data["AREA_NAME"] == ""){
                    $data["AREA_NAME"] = "海外";
                }
                
                $arg["data"][] = $data;
            }
        }
        
        
        //ボタン作成
        makeButton($objForm, $arg, $db, $model);


        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJl378q");
        knjCreateHidden($objForm, "TEMPLATE_PATH");
        knjCreateHidden($objForm, "mockRankRangeCnt", $mockRankRangeCnt);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl378qForm1.html", $arg);
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
    //実行ボタン
    $extra = "onclick=\"btn_submit('exec');\"";
    $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);

    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
?>
