<?php

require_once('for_php7.php');

class knjh400_sibouForm2
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }


        //フォーム作成
        $objForm = new form;

        $arg["start"]   = $objForm->get_start("main", "POST", "knjh400_sibouindex.php", "", "main");

        //データベース接続
        $db = Query::dbCheckOut();


        //年度
        $arg["CTRLYEAR"] = $model->year."年度";
        //学年
        //SCHREG_REGD_BASE_MSTから取得
        $schregQuery = knjh400_sibouQuery::getSchreg($model->year, $model->GAKUSEKI);
        $schregRow = $db->getRow($schregQuery, DB_FETCHMODE_ASSOC);
        
        $arg["GAKUNEN"] = $model->gakunen."年生";
        $model->hrClass = $schregRow["HR_CLASS"];
        $model->attendno = $schregRow["ATTENDNO"];

        //名前
        $arg["NAME"] = $schregRow["HR_CLASS_NAME1"]."組 ".number_format($schregRow["ATTENDNO"])."番 ".$schregRow["NAME_SHOW"];
        
        //期を取得したい
        /*$ent_year = $model->year - $model->gakunen + 1;
        $periodQuery = knjh400_sibouQuery::getPeriod($ent_year);
        $period = $db->getOne($periodQuery);
        $arg["PERIOD"] = "(".$period."期)";*/

        //業者
        $extra = " style=\"font-size:110%;\" onchange=\"btn_submit('list');\"";
        $query = knjh400_sibouQuery::getCompanycd();
        $result = $db->query($query);
        
        $opt = array();
        $opt[] = array("label"  => "全表示",
                       "value"  => "");
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt[] = array("label"  => $row["LABEL"],
                           "value"  => $row["VALUE"]);
        }
        $arg["GYOUSYA"] = knjCreateCombo($objForm, "GYOUSYA", $model->top["GYOUSYA"], $opt, $extra, 1);


        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjh400_sibouForm2.html", $arg); 
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $result = $db->query($query);
    $opt = array();
    $serch = array();

    if ($blank == "SORT") {
        $opt[] = array("label" => "並び替え",
                       "value" => "");
    }

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
        $serch[] = $row["VALUE"];
    }

    $value = ($value && in_array($value, $serch)) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
//コンボ作成　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　
function makeCombo(&$objForm, &$arg, $array, &$value, $name, $extra, $size, $blank = "") {
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array ("label" => "",
                        "value" => "");
    }
    if ($blank == "SYORI") {
    }


    foreach($array as $key => $val){
        //$val = str_pad($val,2,"0",STR_PAD_LEFT);
        $opt[] = array ("label" => $val,
                        "value" => $key);
        if ($value == $key) $value_flg = true;
    }

    //$value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    //再表示
    $extra = " style=\"font-size:110%;\"";
    $extra .= " onclick=\"openGamen('{$model->field["GYOUSYA"]}');\"";
    $arg["button"]["btn_reappear"] = knjCreateBtn($objForm, "btn_reappear", "再表示", $extra);
    //終了
    $extra = "onclick=\"closecheck();return closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
//hidden作成
function makeHidden(&$objForm) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "selectdataLabel");
}
?>
