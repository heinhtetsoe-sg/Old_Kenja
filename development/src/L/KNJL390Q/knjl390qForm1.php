<?php

require_once('for_php7.php');

class knjl390qForm1
{
    function main(&$model)
    {
        //権限チェック
        if ($model->auth != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjl390qindex.php", "", "main");
        
        $extraInt   = "onblur=\"this.value=toInteger(this.value)\" ";   //数字に
        //DB接続
        $db = Query::dbCheckOut();

        //試験日取得
        $arg["CTRL_YEAR"] = CTRL_YEAR;
        $query = knjl390qQuery::getExamDate();
        $examDate = $db->getOne($query);
        if($examDate != ""){
            $exam = explode("-", $examDate);
            $week = array("日","月","火","水","木","金","土");
            $weekNo = date('w', mktime(0, 0, 0, $exam[1], $exam[2], $exam[0]));
            
            $arg["EXAM_DATE"] = $exam[0]."年".$exam[1]."月".$exam[2]."日 (".$week[$weekNo].")";
        }
        //hidden
        knjCreateHidden($objForm, "examDate", $examDate);

        //印刷チェックリスト
        $label = array("会場別受験者名簿(受験番号順)", "会場別受験者名簿(氏名50音順)",
                       "会場別未徴収受付名簿(個人)", "会場別未徴収受付名簿(団体)","会場別未徴収受付名簿(校内生)");
        $count = get_count($label);
        for ($i = 0; $i < $count; $i++) {
            $extra = " id=\"CHECK".$i."\"";
            if ($model->field["CHECK".$i] == "1") {
                $extra .= " checked ";
            }
            $data["CHECK"] = knjCreateCheckBox($objForm, "CHECK".$i, "1", $extra, "")."<LABEL for=\"CHECK".$i."\">".$label[$i]."</LABEL>";

            if ($i < 2) {
                $arg["data"][] = $data;
            } else {
                $arg["data2"][] = $data;
            }
        }
        
        //試験会場
        $opt = array(1, 2);
        $option = " onclick=\"btn_submit('change');\"";
        $extra = array("id=\"PLACE1\"".$option, "id=\"PLACE2\"".$option);
        $label = array("PLACE1" => "すべて", "PLACE2" => "指定");
        $radioArray = knjCreateRadio($objForm, "PLACE", $model->field["PLACE"], $extra, $opt, get_count($opt));
        $arg["PLACE"] = "";
        $sp = "";
        foreach($radioArray as $key => $val){
            $arg["PLACE"] .= $sp.$val."<LABEL for=\"".$key."\">".$label[$key]."</LABEL>";
            $sp = "　";
        }
        if($model->field["PLACE"] == "2"){
            //試験会場のコンボボックス
            $query = knjl390qQuery::getExamPlace();
            makeCmb($objForm, $arg, $db, $query, $model->field["PLACE_COMB"], "PLACE_COMB", $extra, "1", "BLANK");
        }
        
        
        //ボタン作成
        makeButton($objForm, $arg, $model);


        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJL390Q");
        knjCreateHidden($objForm, "TEMPLATE_PATH");
        
        //チェックボックスの数をhiddenに
        knjCreateHidden($objForm, "CHECK_CNT", $count);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl390qForm1.html", $arg);
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
