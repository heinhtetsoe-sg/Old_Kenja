<?php

require_once('for_php7.php');

class knjh45psearchForm1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjh45psearchindex.php", "", "edit");

        $db = Query::dbCheckOut();
        
        //全体分析か上位分析か選択
        $value = array(2, 1);
        $model->top_field["ANALYSIS"] = $model->top_field["ANALYSIS"] ? $model->top_field["ANALYSIS"] : "1";
        $extraRadio = array("id=\"ANALYSIS1\" onclick=\"btn_submit('top_change')\";", "id=\"ANALYSIS2\" onclick=\"btn_submit('top_change')\";");
        $radioArray = knjCreateRadio($objForm, "ANALYSIS", $model->top_field["ANALYSIS"], $extraRadio, $value, get_count($value));
        foreach($radioArray as $key => $val) $arg[$key] = $val;
        
        //スポーツ含むかどうかか選択
        $jogaiQuery = knjh45psearchQuery::getJogai();
        $jogaiResult = $db->query($jogaiQuery);
        $i = 1;
        $value = array();
        $label = array();
        $extraRadio = array();
        $id = array();
        while($jogaiRow = $jogaiResult->fetchRow(DB_FETCHMODE_ASSOC)){
            $value[$i-1] = $jogaiRow["NAMECD2"];
            $label[$i-1] = $jogaiRow["NAME1"];
            $extraRadio[$i-1] = "id=\"SPORTS".$i."\" onclick=\"btn_submit('top_change')\";";
            $id[$i-1] = "SPORTS".$i;
            $i++;
        }
        //$value = array(2, 1);
        $model->top_field["SPORTS"] = $model->top_field["SPORTS"] ? $model->top_field["SPORTS"] : "1";
        //$extraRadio = array("id=\"SPORTS1\" onclick=\"btn_submit('top_change')\";", "id=\"SPORTS2\" onclick=\"btn_submit('top_change')\";");
        $radioArray = knjCreateRadio($objForm, "SPORTS", $model->top_field["SPORTS"], $extraRadio, $value, get_count($value));
        $cnt = 0;
        foreach($radioArray as $key => $val) {
            $arg[$key] = $val."<LABEL for=\"".$id[$cnt]."\"> ".$label[$cnt]."</LABEL>";
            $cnt++;
        }
        
        //期
        $extra = " style=\"font-size:110%;\" onchange=\"btn_submit('top_change')\"";
        //SCHOOL_MSTから取得
        $toyear = CTRL_YEAR;
        $fromyear = $toyear - 2;
        $nendoQuery = knjh45psearchQuery::getNendo($fromyear, $toyear);
        $nendoResult = $db->query($nendoQuery);
        $nendo = array();
        
        while($nendoRow = $nendoResult->fetchRow(DB_FETCHMODE_ASSOC)){
            $nendo[$nendoRow["YEAR"]] = $nendoRow["PRESENT_EST"];
        }
        $opt1[] = array();
        foreach($nendo as $key => $val){
            $opt1[] = array("label"  => $val,
                            "value"  => $key);
        }
        $arg["NENDO"] = knjCreateCombo($objForm, "NENDO", $model->top_field["NENDO"], $opt1, $extra, 1);
        
        //業者
        $extra = " style=\"font-size:110%;\" onchange=\"btn_submit('top_change')\"";
        $query = knjh45psearchQuery::getCompanycd();
        $result = $db->query($query);
        
        $opt = array();
        $opt[] = array("label"  => "",
                       "value"  => "");
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt[] = array("label"  => $row["LABEL"],
                           "value"  => $row["VALUE"]);
        }
        $arg["GYOUSYA"] = knjCreateCombo($objForm, "GYOUSYA", $model->top_field["GYOUSYA"], $opt, $extra, 1);
        

        
        //型選択
        $value = array(2, 1);
        $model->top_field["KATA"] = $model->top_field["KATA"] ? $model->top_field["KATA"] : "1";
        $extraRadio = array("id=\"KATA1\" onclick=\"btn_submit('top_change')\";", "id=\"KATA2\" onclick=\"btn_submit('top_change')\";");
        $radioArray = knjCreateRadio($objForm, "KATA", $model->top_field["KATA"], $extraRadio, $value, get_count($value));
        foreach($radioArray as $key => $val) $arg[$key] = $val;

        
        //$arg["link"] = REQUESTROOT."/Z/KNJh45psearch_2/knjh45psearch_2index.php";
        Query::dbCheckIn($db);
        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd",
                            "value"     => $model->cmd
                            ) );
        
        //hidden作成
        makeHidden($objForm, $model);
        
        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjh45psearchForm1.html", $arg);
    }
} 
//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    //表示
    $para = "?cmd=open&ANALYSIS={$model->top_field["ANALYSIS"]}&SPORTS={$model->top_field["SPORTS"]}&KATA={$model->top_field["KATA"]}&YEAR={$model->top_field["NENDO"]}&GYOUSYA={$model->top_field["GYOUSYA"]}";
    
    $extra = " style=\"font-size:110%;\"";
    $extra .= " onclick=\"openGamen('{$model->prgid}','{$para}');\"";
    $arg["button"]["btn_appear"] = knjCreateBtn($objForm, "btn_appear", "表示", $extra);
    //終了
    $extra = " style=\"font-size:110%;\"";
    $extra .= "onclick=\"closecheck();return closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終了", $extra);
}
//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "requestroot", REQUESTROOT);
}
?>
