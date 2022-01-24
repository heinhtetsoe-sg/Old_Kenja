<?php

require_once('for_php7.php');

class knjl392qForm1
{
    function main(&$model)
    {
        //権限チェック
        if ($model->auth != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjl392qindex.php", "", "main");
        
        $extraInt   = "onblur=\"this.value=toInteger(this.value)\" ";   //数字に
        //DB接続
        $db = Query::dbCheckOut();

        //試験日取得
        $arg["CTRL_YEAR"] = CTRL_YEAR;
        $query = knjl392qQuery::getExamDate();
        $examDate = $db->getOne($query);
        if($examDate != ""){
            $exam = explode("-", $examDate);
            $week = array("日","月","火","水","木","金","土");
            $weekNo = date('w', mktime(0, 0, 0, $exam[1], $exam[2], $exam[0]));
            
            $arg["EXAM_DATE"] = $exam[0]."年".$exam[1]."月".$exam[2]."日 (".$week[$weekNo].")";
        }
        
        //対象生徒
        $opt = array(1, 2);
        $option = " onclick=\"btn_submit('change');\"";
        $extra = array("id=\"STUDENT1\"".$option, "id=\"STUDENT2\"".$option);
        $label = array("STUDENT1" => "駿中生", "STUDENT2" => "その他");
        $radioArray = knjCreateRadio($objForm, "STUDENT", $model->field["STUDENT"], $extra, $opt, get_count($opt));
        $arg["STUDENT"] = "";
        $sp = "";
        foreach($radioArray as $key => $val){
            $arg["STUDENT"] .= $sp.$val."<LABEL for=\"".$key."\">".$label[$key]."</LABEL>";
            $sp = "　";
        }
        
        //印刷対象
        $opt = array(1, 2, 3);
        $option = " onclick=\"btn_submit('change');\"";
        $extra = array("id=\"CHOICE1\"".$option, "id=\"CHOICE2\"".$option, "id=\"CHOICE3\"".$option);
        $label = array("CHOICE1" => "すべて", "CHOICE2" => "試験会場", "CHOICE3" => "受験番号");
        $radioArray = knjCreateRadio($objForm, "CHOICE", $model->field["CHOICE"], $extra, $opt, get_count($opt));
        $argName = array("CHOICE1" => "ALL", "CHOICE2" => "PLACE", "CHOICE3" => "EXAM");
        $sp = "";
        foreach($radioArray as $key => $val){
            $arg[$argName[$key]] = $val."<LABEL for=\"".$key."\">".$label[$key]."</LABEL>";
        }
        if($model->field["CHOICE"] == "2"){
            //試験会場のコンボボックス
            $query = knjl392qQuery::getExamPlace();
            makeCmb($objForm, $arg, $db, $query, $model->field["PLACE_COMB"], "PLACE_COMB", $extra, "1", "BLANK");
        }else if($model->field["CHOICE"] == "3"){
            //受験番号のテキストボックス
            $extra = $extraInt;
            $from = knjCreateTextBox($objForm, $model->field["EXAM_FROM"], "EXAM_FROM", 10, 5, $extra);
            $to = knjCreateTextBox($objForm, $model->field["EXAM_TO"], "EXAM_TO", 10, 5, $extra);
            
            $arg["EXAMNO"] = $from." ～ ".$to;
            
            //hidden作成
            $query = knjl392qQuery::getExamArea();
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            
            knjCreateHidden($objForm, "AREA_FROM", $row["JUKEN_NO_FROM"]);
            knjCreateHidden($objForm, "AREA_TO", $row["JUKEN_NO_TO"]);
        }
        
        //ボタン作成
        makeButton($objForm, $arg, $model);


        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJL392Q");
        knjCreateHidden($objForm, "TEMPLATE_PATH");
        knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);

        //チェックボックスの数をhiddenに
        knjCreateHidden($objForm, "CHECK_CNT", $count);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl392qForm1.html", $arg);
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
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

//ボタン作成
function makeButton(&$objForm, &$arg, $model)
{
    //実行ボタン
    $extra = "onclick=\"newwin('" . SERVLET_URL . "');\"";
    $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);

    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
?>
