<?php

require_once('for_php7.php');

class knjl393qForm1
{
    function main(&$model)
    {
        //権限チェック
        if ($model->auth != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjl393qindex.php", "", "main");
        
        $extraInt   = "onblur=\"this.value=toInteger(this.value)\" ";   //数字に
        //DB接続
        $db = Query::dbCheckOut();

        //試験日取得
        $arg["CTRL_YEAR"] = CTRL_YEAR;
        $query = knjl393qQuery::getExamDate();
        $examDate = $db->getOne($query);
        if($examDate != ""){
            $exam = explode("-", $examDate);
            $week = array("日","月","火","水","木","金","土");
            $weekNo = date('w', mktime(0, 0, 0, $exam[1], $exam[2], $exam[0]));
            
            $arg["EXAM_DATE"] = $exam[0]."年".$exam[1]."月".$exam[2]."日 (".$week[$weekNo].")";
        }
        
        //印刷チェックリスト
        $label = array("受験番号順", "中学校別受験番号順", "団体別受験番号順", "会場別受験番号順",  //受験番号順
                       "高得点順", "中学校別高得点順", "団体別高得点順", "会場別高得点順",          //高得点順
                       "中学校別高得点順",                                                          //中学校訪問用
                       //"海外日本人学校別高得点順", "リンデン校内生高得点順", "団体(中学除)別高得点順", "団体参加県外中学高得点順", "甲斐ゼミ・文理　受験番号順", "ハイステップ　受験番号順"     //郵送用
                       "海外日本人学校別高得点順", "駿台中学部生高得点順", "団体(中学除)別高得点順", "団体参加県外中学高得点順", "甲斐ゼミ・文理　受験番号順"     //郵送用
                       );
        $count = get_count($label);
        for($i=0;$i<$count;$i++){
            $extra = " id=\"CHECK".$i."\"";
            if(!empty($model->field["CHECK"]) && in_array($i, $model->field["CHECK"])){
                $extra .= " checked ";
            }
            $data["CHECK"] = knjCreateCheckBox($objForm, "CHECK", $i, $extra, "multi")."<LABEL for=\"CHECK".$i."\">".$label[$i]."</LABEL>";
            if($i == 5){
                //高得点順＞中学校別高得点順のときだけチェックボックスを作り直して、ラジオボタンを後ろにくっつける
                $extra .= " onclick=\"radio_change();\"";
                $data["CHECK"] = knjCreateCheckBox($objForm, "CHECK", $i, $extra, "multi")."<LABEL for=\"CHECK".$i."\">".$label[$i]."</LABEL>";
                //ラジオボタン
                $opt = array(1, 2);
                if(!empty($model->field["CHECK"]) && in_array("5", $model->field["CHECK"])){
                    $option = "";
                }else{
                    $option = " disabled ";
                }
                $extra = array("id=\"GROUPNAME1\"".$option, "id=\"GROUPNAME2\"".$option);
                $label2 = array("GROUPNAME1" => "団体名を印刷", "GROUPNAME2" => "団体名を空欄");
                $radioArray = knjCreateRadio($objForm, "GROUPNAME", $model->field["GROUPNAME"], $extra, $opt, get_count($opt));
                $data["CHECK"] .= "<br>　　(";
                $sp = "";
                foreach($radioArray as $key => $val){
                    $data["CHECK"] .= $sp.$val."<LABEL for=\"".$key."\">".$label2[$key]."</LABEL>";
                    $sp = "　";
                }
                $data["CHECK"] .= ")";
            }
            
            if($i<4){
                $arg["data"][] = $data;
            }else if($i<8){
                $arg["data2"][] = $data;
            }else if($i<9){
                $arg["data3"][] = $data;
            }else{
                $arg["data4"][] = $data;
            }
        }
        
        //中学校のコンボボックス
        $query = knjl393qQuery::getExamFinschool();
        makeCmb($objForm, $arg, $db, $query, $model->field["FINSCHOOL_COMB"], "FINSCHOOL_COMB", $extra, "1", "BLANK");
        
        //団体のコンボボックス
        $query = knjl393qQuery::getExamGroup();
        makeCmb($objForm, $arg, $db, $query, $model->field["GROUP_COMB"], "GROUP_COMB", $extra, "1", "BLANK");
        
        //試験会場のコンボボックス
        $query = knjl393qQuery::getExamPlace();
        makeCmb($objForm, $arg, $db, $query, $model->field["PLACE_COMB"], "PLACE_COMB", $extra, "1", "BLANK");
        
        //印刷対象
        $opt = array(1, 2);
        $option = " onclick=\"btn_submit('change');\"";
        $extra = array("id=\"CHOICE1\"".$option, "id=\"CHOICE2\"".$option);
        $label = array("CHOICE1" => "すべて", "CHOICE2" => "受験番号");
        $radioArray = knjCreateRadio($objForm, "CHOICE", $model->field["CHOICE"], $extra, $opt, get_count($opt));
        $argName = array("CHOICE1" => "ALL", "CHOICE2" => "EXAM");
        $sp = "";
        foreach($radioArray as $key => $val){
            $arg[$argName[$key]] = $val."<LABEL for=\"".$key."\">".$label[$key]."</LABEL>";
        }
        if($model->field["CHOICE"] == "2"){
            //受験番号のテキストボックス
            $extra = $extraInt;
            $from = knjCreateTextBox($objForm, $model->field["EXAM_FROM"], "EXAM_FROM", 10, 5, $extra);
            $to = knjCreateTextBox($objForm, $model->field["EXAM_TO"], "EXAM_TO", 10, 5, $extra);
            
            $arg["EXAMNO"] = $from." ～ ".$to;
            
        }
        
        //ボタン作成
        makeButton($objForm, $arg, $model);


        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJL393Q");
        knjCreateHidden($objForm, "TEMPLATE_PATH");

        //チェックボックスの数をhiddenに
        knjCreateHidden($objForm, "CHECK_CNT", $count);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl393qForm1.html", $arg);
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
