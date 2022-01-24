<?php

require_once('for_php7.php');

class knjl396qForm1
{
    function main(&$model)
    {
        //権限チェック
        if ($model->auth != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjl396qindex.php", "", "main");
        
        $extraInt   = "onblur=\"this.value=toInteger(this.value)\" ";   //数字に
        //DB接続
        $db = Query::dbCheckOut();

        //試験日取得
        $arg["CTRL_YEAR"] = CTRL_YEAR;
        $query = knjl396qQuery::getExamDate();
        $examDate = $db->getOne($query);
        if($examDate != ""){
            $exam = explode("-", $examDate);
            $week = array("日","月","火","水","木","金","土");
            $weekNo = date('w', mktime(0, 0, 0, $exam[1], $exam[2], $exam[0]));
            
            $arg["EXAM_DATE"] = $exam[0]."年".$exam[1]."月".$exam[2]."日 (".$week[$weekNo].")";
        }
        
        //印刷チェックリスト
        $label = array("1. 県外会場受験の県内生", "2. 県外会場受験の長野県中学校生", "3. 県内会場受験の長野を除く県外生", "4. すべての長野県中学校生", 
                       "5. 県内の中2以下", "6. 県内の特奨生", "7. 長野の特奨生", "8. 長野の特奨生除くA・準A現役", "9. 長野のB～Dの現役", "10.長野除く県外の中2以下", 
                       "11.長野除く県外の特奨生", "12.長野除く県外の特奨生外A現役", "13.長野除く県外のB現役", "14.長野除く県外のC・Dまたは現役以外", 
                       "15.海外の特奨生", "16.海外の特奨生除くA・B現役", "17.県内の欠席者リスト", "18.長野県の欠席者リスト", "19.県外の欠席者リスト", "20.海外の欠席者リスト",
                       "21.浪人リスト", "22.欠科目者リスト", 
                       "国内欠席者住所シール", "海外受験者シール", "国内校内生シール"
                       );
        $count = get_count($label);
        for($i=0;$i<$count;$i++){
            $extra = " id=\"CHECK".$i."\"";
            if(!empty($model->field["CHECK"]) && in_array($i, $model->field["CHECK"])){
                $extra .= " checked ";
            }
            //データがあるかどうかをチェックする
            if($i < 22){
                if($i<16){
                    $query = knjl396qQuery::getFirstSql($i);
                }else if($i<21){
                    $query = knjl396qQuery::getSecondSql($i);
                }else if($i == 21){
                    $query = knjl396qQuery::getThirdSql($i);
                }
                $cnt = $db->getOne($query);
                if($cnt == 0){
                    $extra .= " disabled";
                    $label[$i] = "<span style=\"color:gray;\">".$label[$i]."</span>";
                }
            }
            
            $data["CHECK"] = knjCreateCheckBox($objForm, "CHECK", $i, $extra, "multi")."<LABEL for=\"CHECK".$i."\">".$label[$i]."</LABEL>";
            
            if($i < 22){
                $arg["data1"][] = $data;
            }else{
                $arg["data"][] = $data;
            }
        }
        
        
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
        knjCreateHidden($objForm, "PRGID", "KNJL396Q");
        knjCreateHidden($objForm, "TEMPLATE_PATH");

        //チェックボックスの数をhiddenに
        knjCreateHidden($objForm, "CHECK_CNT", $count);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl396qForm1.html", $arg);
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
