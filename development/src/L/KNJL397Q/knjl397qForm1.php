<?php

require_once('for_php7.php');

class knjl397qForm1
{
    function main(&$model)
    {
        //権限チェック
        if ($model->auth != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjl397qindex.php", "", "main");
        
        $extraInt   = "onblur=\"this.value=toInteger(this.value)\" ";   //数字に
        //DB接続
        $db = Query::dbCheckOut();

        //試験日取得
        $arg["CTRL_YEAR"] = CTRL_YEAR;
        $query = knjl397qQuery::getExamDate();
        $examDate = $db->getOne($query);
        if($examDate != ""){
            $exam = explode("-", $examDate);
            $week = array("日","月","火","水","木","金","土");
            $weekNo = date('w', mktime(0, 0, 0, $exam[1], $exam[2], $exam[0]));
            
            $arg["EXAM_DATE"] = $exam[0]."年".$exam[1]."月".$exam[2]."日 (".$week[$weekNo].")";
        }
        
        //表示対象コンボ
        $extra = " onChange=\"btn_submit('change');\"";
        $query = knjl397qQuery::getKubun();
        $result = $db->query($query);
        $opt = array();
        $opt[0] = array("value" => "",
                        "label" => "すべて");
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt[] = array("value" => $row["NAMECD2"],
                           "label" => $row["NAME1"]);
        }
        $arg["KUBUN"] = knjCreateCombo($objForm, "KUBUN", $model->field["KUBUN"], $opt, $extra, 1);

        
        //対象生徒取得
        //一応カウント
        $query = knjl397qQuery::getListData($model->field["KUBUN"], "1");
        $cnt = $db->getOne($query);
        if($cnt > 0){
            $query = knjl397qQuery::getListData($model->field["KUBUN"]);
            $result = $db->query($query);
            $i = 0;
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                //チェックボックス
                $extra = " id=\"CHECK".$i."\"";
                if(!empty($model->field["CHECK"]) && in_array($row["SAT_NO"], $model->field["CHECK"])){
                    $extra .= " checked ";
                }else if(empty($model->field["CHECK"])){
                    $extra .= " checked ";
                }
                $row["CHECK"] = knjCreateCheckBox($objForm, "CHECK", $row["SAT_NO"], $extra, "multi");
                
                $arg["data"][] = $row;
                
                $i++;
            }
        }else{
            $model->setMessage("対象のデータはありません。");
        }
        
        //全チェック
        $arg["ALL_CHECK"] = knjCreateCheckBox($objForm, "ALL_CHECK", "ON", " onClick=\"allCheck('change', this)\"; checked", "1");
        //全チェック用hidden
        knjCreateHidden($objForm, "CHECK_CNT", $cnt);
        
        
        //ボタン作成
        makeButton($objForm, $arg, $model);


        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJL397Q");
        knjCreateHidden($objForm, "TEMPLATE_PATH");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl397qForm1.html", $arg);
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
