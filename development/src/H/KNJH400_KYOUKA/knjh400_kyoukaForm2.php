<?php

require_once('for_php7.php');

class knjh400_kyoukaForm2
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjh400_kyoukaindex.php", "", "edit");

        $db = Query::dbCheckOut();
        
        //年度または業者のコンボ変えたら一応模試の中身空白に
        if($model->cmd == "right_change"){
            $model->right_field["MOCKCD"] = "";
        }
        
        //先に表示パーツ作成
        //年度　とりあえず処理年度-5年
        $extra = " style=\"font-size:110%;\" onchange=\"btn_submit('right_change')\"";
        if($model->right_field["NENDO"] == ""){
            $model->right_field["NENDO"] = $model->year;
        }
        $year = $model->year - 5;
        $opt1[] = array();
        for($i=0;$i<6;$i++){
            $opt1[] = array("label" => $year,
                            "value" => $year);
            $year++;
        }
        $arg["NENDO"] = knjCreateCombo($objForm, "NENDO", $model->right_field["NENDO"], $opt1, $extra, 1);
        
        
        //業者
        $extra = " style=\"font-size:110%;\" onchange=\"btn_submit('right_change')\"";
        $query = knjh400_kyoukaQuery::getCompanycd();
        $result = $db->query($query);
        
        $opt = array();
        $opt[] = array("label"  => "全表示",
                       "value"  => "");
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt[] = array("label"  => $row["LABEL"],
                           "value"  => $row["VALUE"]);
        }
        $arg["GYOUSYA"] = knjCreateCombo($objForm, "GYOUSYA", $model->right_field["GYOUSYA"], $opt, $extra, 1);
        
        $opt2[] = array();
        //模試名称絞込み
        $extra = " style=\"font-size:110%;\"";
        $MockNameQuery = knjh400_kyoukaQuery::getMockName($model->GAKUSEKI, $model->right_field["NENDO"], $model->right_field["GYOUSYA"]);
        $MockNameResult = $db->query($MockNameQuery);
        while($MockNameRow = $MockNameResult->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt2[] = array("label" => $MockNameRow["SINROSIDOU_MOSI_NAME"],
                            "value" => $MockNameRow["MOCKCD"]);
        }
        $arg["MOCKNAME"] = knjCreateCombo($objForm, "MOCKCD", $model->right_field["MOCKCD"], $opt2, $extra, 1);
        
        //模試名まで選択されてたらグラフ作成
        if($model->right_field["MOCKCD"] != ""){
            $arg["data"]["NENDO"] = $model->right_field["NENDO"];
            
            //模試名称取得
            $mockQuery = knjh400_kyoukaQuery::getMock($model->right_field["MOCKCD"]);
            $arg["data"]["MOCKNAME"] = $db->getOne($mockQuery);
            
            //型
            $extra = " style=\"font-size:110%;\" onchange=\"btn_submit('right_appear')\"";
            $array = array("0" => "教科",
                           "1" => "受験型");
            $opt1 = array();
            foreach($array as $key => $val){
                $opt1[] = array("label"  => $val,
                               "value"  => $key);
            }
            //$model->field["KATA"] = 0;
            $arg["KATA"] = knjCreateCombo($objForm, "KATA", $model->right_field["KATA"], $opt1, $extra, 1);
            
            //教科名取得
            $kyoukaQuery = knjh400_kyoukaQuery::getKyouka($model->GAKUSEKI, $model->right_field["NENDO"], $model->right_field["MOCKCD"], $model->right_field["KATA"]);
            $kyoukaResult = $db->query($kyoukaQuery);
            
            //設定した初期値が含まれているかのチェック
            $ex_kyouka = array();
            while($kyouka = $kyoukaResult->fetchRow(DB_FETCHMODE_ASSOC)){
                $ex_kyouka[$kyouka["MOCK_SUBCLASS_CD"]] = $kyouka["SUBCLASS_ABBV"];
            }

            $existflg = 0;
            if(!empty($model->right_field["CHECK"])){
                foreach($model->right_field["CHECK"] as $key => $value){
                    if(array_key_exists($value, $ex_kyouka)){
                        $existflg = 1;
                    }else{
                        unset($model->right_field["CHECK"][$key]);
                    }
                }
                if($existflg != 1){
                    $model->right_field["CHECK"] = "";
                }
            }
            //受験してる教科を取得したい
            $kyoukaQuery = knjh400_kyoukaQuery::getKyouka($model->GAKUSEKI, $model->right_field["NENDO"], $model->right_field["MOCKCD"], $model->right_field["KATA"]);
            $kyoukaResult = $db->query($kyoukaQuery);
            $opt3 = array();
            while($kyoukaRow = $kyoukaResult->fetchRow(DB_FETCHMODE_ASSOC)){
                $opt3[] = array("label"   => $kyoukaRow["SUBCLASS_ABBV"],
                                "value"   => $kyoukaRow["MOCK_SUBCLASS_CD"],
                                "deviation"  => $kyoukaRow["DEVIATION"]);
            }
            
            //グラフ用データ
            $model->right_graph = array();
            $lcnm = "";
            $dcnm = "";
            $model->right_graph["cnt"] = 1;
            $model->right_graph["type"] = "radar";
            
            //模試結果取得したい
            foreach($opt3 as $key => $val){
                $extra = " id=\"".$key."\"";
                if($model->right_field["CHECK"] != ""){
                    if(in_array($val["value"], $model->right_field["CHECK"])){
                        $extra .= " checked";
                        
                        //グラフ用データに追加
                        $model->right_graph["label"] .= $lcnm.$val["label"];
                        $lcnm = ",";
                        
                        $model->right_graph[0] .= $dcnm.$val["deviation"];
                        $dcnm = ",";
                    }
                }else{
                    $extra .= " checked";
                    
                    //グラフ用データに追加
                    $model->right_graph["label"] .= $lcnm.$val["label"];
                    $lcnm = ",";
                    
                    $model->right_graph[0] .= $dcnm.$val["deviation"];
                    $dcnm = ",";
                }
                $data2["CHECK"] = knjCreateCheckBox($objForm, "CHECK[]", $val["value"], $extra);
                
                $data2["KYOUKA"] = "<LABEL for=".$key." style=\"display:block;\">".$val["label"]."</LABEL>";
                $data2["HENSATI"] = "<LABEL for=".$key." style=\"display:block;\">".$val["deviation"]."</LABEL>";
                
                $arg["data2"][] = $data2;
            }
            
        }
        
        $model->right_graph["TITLE"] = "";
        $model->right_graph["YMAX"] = 80;
        $model->right_graph["YMIN"] = 0;
        
        //グラフ用hidden作成
        graph::CreateDataHidden($objForm,$model->right_graph,0);
        
        //$arg["link"] = REQUESTROOT."/Z/KNJh400_kyouka_2/knjh400_kyouka_2index.php";
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
        //View::toHTML($model, "knjh400_kyoukaForm1.html", $arg);
        
        
        $jsplugin = "chart.js|Chart.min.js|graph.js";

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML6($model, "knjh400_kyoukaForm2.html", $arg, $jsplugin, $cssplugin);
    }
} 
//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    //表示
    $extra = " style=\"font-size:110%;\"";
    $extra .= " onclick=\"btn_submit('right_appear');\"";
    $arg["button"]["btn_appear"] = knjCreateBtn($objForm, "btn_appear", "表示", $extra);
    //英数国
    $extra = " style=\"font-size:110%;\"";
    $extra .= " onclick=\"btn_submit('right_sanka');\"";
    $arg["button"]["btn_sanka"] = knjCreateBtn($objForm, "btn_sanka", "英数国", $extra);
    //再表示
    $extra = " style=\"font-size:110%;\"";
    $extra .= " onclick=\"btn_submit('right_reappear');\"";
    $arg["button"]["btn_reappear"] = knjCreateBtn($objForm, "btn_reappear", "再表示", $extra);
}
//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "type", "radar");
    knjCreateHidden($objForm, "maxTicksLimit", 6);
}
?>
