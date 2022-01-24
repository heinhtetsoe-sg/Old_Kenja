<?php

require_once('for_php7.php');

class knjh410_seisekiForm2
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }


        //フォーム作成
        $objForm = new form;

        $arg["start"]   = $objForm->get_start("main", "POST", "knjh410_seisekiindex.php", "", "main");

        //データベース接続
        $db = Query::dbCheckOut();

        //グラフ作成用
        $model->data = array();
        $data = array();
        $dcnm = "";
        $hcnm = "";

        //年度
        $arg["CTRLYEAR"] = $model->year."年度";
        $ctrlyear = $model->year;
        //学年
        //SCHREG_REGD_BASE_MSTから取得
        $schregQuery = knjh410_seisekiQuery::getSchreg($model->year, $model->GAKUSEKI);
        $schregRow = $db->getRow($schregQuery, DB_FETCHMODE_ASSOC);
        
        $grade = mb_convert_kana(mb_substr($schregRow["GRADE_NAME"], 0, 1), "n");
        
        $model->gakunen = number_format($grade);

        $arg["GAKUNEN"] = $model->gakunen."年生";
        //名前
        $arg["NAME"] = $schregRow["HR_CLASS_NAME1"]."組 ".number_format($schregRow["ATTENDNO"])."番 ".$schregRow["NAME_SHOW"];
        
        //期を取得したい
        /*$ent_year = $model->year - $model->gakunen + 1;
        $periodQuery = knjh410_seisekiQuery::getPeriod($ent_year);
        $period = $db->getOne($periodQuery);
        $arg["PERIOD"] = "(".$period."期)";*/
        
        //データ
        //$model->GAKUSEKI = "13100020";
        //$ctrlyear = CTRL_YEAR;
        //$ctrlyear = '2016';

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        //View::toHTML($model, "knjh410_seisekiForm1.html", $arg); 
        
        $jsplugin = "chart.js|Chart.min.js|graph.js";

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML6($model, "knjh410_seisekiForm2.html", $arg, $jsplugin, $cssplugin);
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $result = $db->query($query);
    $opt = array();
    $serch = array();

    if ($blank == "BLANK") {
        $opt[] = array ("label" => "",
                        "value" => "");
    }
    if ($blank == "GYOUSYA") {
        $opt[] = array ("label" => "全業者",
                        "value" => "0");
    }else if($blank == "SYUBETU"){
        $opt[] = array ("label" => "全模試",
                        "value" => "0");
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
    $extra .= " onclick=\"btn_submit('reappear');\"";
    $arg["button"]["btn_reappear"] = knjCreateBtn($objForm, "btn_reappear", "再表示", $extra);
    //英数国
    $extra = " style=\"font-size:110%;\"";
    $extra .= " onclick=\"btn_submit('sanka');\"";
    $arg["button"]["btn_sanka"] = knjCreateBtn($objForm, "btn_sanka", "英数国", $extra);
    //全選択
    $extra = " style=\"font-size:110%;\"";
    $extra .= " onclick=\"btn_submit('all');\"";
    $arg["button"]["btn_all"] = knjCreateBtn($objForm, "btn_all", "全選択", $extra);
    //終了
    $extra = "onclick=\"closecheck();return closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "cmd");
    
    knjCreateHidden($objForm, "type", "line");
    knjCreateHidden($objForm, "maxTicksLimit", $model->field["maxTicks"]);
}
?>
