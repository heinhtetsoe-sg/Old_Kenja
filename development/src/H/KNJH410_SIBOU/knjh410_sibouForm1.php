<?php

require_once('for_php7.php');

class knjh410_sibouForm1
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }


        //フォーム作成
        $objForm = new form;

        $arg["start"]   = $objForm->get_start("main", "POST", "knjh410_sibouindex.php", "", "main");

        //データベース接続
        $db = Query::dbCheckOut();


        $ctrlyear = $model->year;

        //データ
        //$model->GAKUSEKI = "13100020";
        //$ctrlyear = CTRL_YEAR;
        //$ctrlyear = '2016';

        //js部分用
        $jsfunc = "";
        $jsdata = "";

        for($gaku=$model->gakunen;$gaku>0;$gaku--){
            //CTRL_YEARと学籍番号で模試データ取得したい
            $mock["NENDO"] = $ctrlyear."年度　".$gaku."年次模試";
            $cntQuery = knjh410_sibouQuery::getMockCnt($ctrlyear, $model->GAKUSEKI, $model->field["GYOUSYA"], "1");  //カウント
            $cnt = $db->getOne($cntQuery);
            //$cnt = 1;
            if($cnt > 0){
                //header_x部分
                $table["header_x"]  = "<table>";
                $table["header_x"] .= "<tr><th class=\"no_search\">志望順位</th></tr></table>";
                
                //模試志望校数の最大値と模試回数取得
                $cntQuery = knjh410_sibouQuery::getMockCnt($ctrlyear, $model->GAKUSEKI, $model->field["GYOUSYA"], "2");
                $cntRow = $db->getRow($cntQuery, DB_FETCHMODE_ASSOC);
                
                $hopeMax = $cntRow["CNT_MAX"];      //志望校数最大値
                $mosiCnt = $cntRow["MOCK_CNT"];     //模試回数
                
                //左側の志望順位のループ(header_v部分)
                $table["header_v"]  = "<table>\n";
                for($i=1;$i<$hopeMax+1;$i++){
                    $table["header_v"] .= "<tr>\n<td class=\"no_search\">".$i."</td>\n</tr>\n";
                }
                $table["header_v"] .= "</table>\n";
                
                
                //模試データ(CDとか取得)
                $mosiQuery = knjh410_sibouQuery::getMockCnt($ctrlyear, $model->GAKUSEKI, $model->field["GYOUSYA"], "0");
                $mosiResult = $db->query($mosiQuery);
                
                
                $width = 165*$mosiCnt;     //回数分かける
                
                $table["header_h"]  = "<table style=\"width:{$width}px;\">\n<tr>\n";
                
                //模試回数分カウントする変数
                $roop = 1;
                //以下ループ
                while($mosiRow = $mosiResult->fetchRow(DB_FETCHMODE_ASSOC)){
                    //右側header部分
                    $table["header_h"] .= "<th class=\"no_search\">".$mosiRow["SINROSIDOU_MOSI_NAME"]."</th>\n";
                    
                    //ベネッセ・駿台判定
                    $moshi_gyousya = substr($mosiRow["MOCKCD"],5,1);

                    //模試ごとに志望校名と評価取得して右側データ部分を配列に入れる
                    $hopeQuery = knjh410_sibouQuery::getHopeGaku($mosiRow["MOCKCD"], $model->GAKUSEKI, $ctrlyear);
                    $hopeResult = $db->query($hopeQuery);
                    $i = 0;
                    while($hopeRow = $hopeResult->fetchRow(DB_FETCHMODE_ASSOC)){
                        //配列に入れる
                        $name = "data".$roop;
                        if($i != $hopeRow["SEQ"]-1){    //志望順が飛んでるときにちゃんと飛ばせるように
                            for($t = $i; $t < $hopeRow["SEQ"]-1; $t++){
                                ${$name}[$t]["GAKKO"] = "";
                                ${$name}[$t]["NITTEI"] = "";
                                ${$name}[$t]["JUDGE"] = "";
                                ${$name}[$t]["JUDGE_SS"] = "";
                            }
                            $i = $hopeRow["SEQ"]-1;
                        }
                        if($moshi_gyousya == "2"){
                            //ベネッセはそのまま使う
                            ${$name}[$i] = $hopeRow;
                        }else{
                            ${$name}[$i]["GAKKO"] = $hopeRow["GAKKO"];
                            ${$name}[$i]["NITTEI"] = $hopeRow["NITTEI"];
                            ${$name}[$i]["JUDGE"] = mb_convert_kana($hopeRow["JUDGE"], "r", "UTF-8");  //全角を半角に
                            if($hopeRow["JUDGE_SS"] != ""){
                                ${$name}[$i]["JUDGE_SS"] = number_format($hopeRow["JUDGE_SS"],1);   //小数1桁までにする
                            }else{
                                ${$name}[$i]["JUDGE_SS"] = "";
                            }
                        }

                        $i++;
                    }
                    if($i < $hopeMax){
                        for($j=$i;$j<$hopeMax;$j++){
                            ${$name}[$j]["GAKKO"] = "";
                            ${$name}[$j]["NITTEI"] = "";
                            ${$name}[$j]["JUDGE"] = "";
                            ${$name}[$j]["JUDGE_SS"] = "";
                        }
                    }
                    
                    $roop++;
                    

                }

                $table["header_h"] .= "</tr>\n</table>\n";

                //data部分作成
                $table["data"]  = "<table style=\"width:{$width}px;\">\n";
                for($k=0;$k<$hopeMax;$k++){     //志望校数繰り返す
                    $table["data"] .= "<tr>";
                    for($m=1;$m<$roop;$m++){
                        $name2 = "data".$m;
                        //学校情報分割する
                        $gakko = array();
                        $gakko = explode(" ", ${$name2}[$k]["GAKKO"]);
                        $gakkoname = "";
                        $br = "";
                        
                        
                        /*if(${$name2}[$k]["NITTEI"] != ""){
                            for($aa = 0;$aa<4;$aa++){
                                if($gakko[$aa] != ""){
                                    $gakkoname .= $br.mb_substr($gakko[$aa], 0, 7);
                                    $br = "<BR>";
                                }
                            }
                            $table["data"] .= "<td class=\"gakko\">".$gakkoname."<br>".${$name2}[$k]["NITTEI"]."</td>\n<td class=\"hyoka\">".${$name2}[$k]["JUDGE"]."<br>".$judge."</td>\n";
                        }else{*/
                            for($aa = 0;$aa<5;$aa++){
                                if($gakko[$aa] != ""){
                                    $gakkoname .= $br.mb_substr($gakko[$aa], 0, 7);
                                    $br = "<BR>";
                                }
                            }
                            $table["data"] .= "<td class=\"gakko\">".$gakkoname."</td>\n<td class=\"hyoka\">".${$name2}[$k]["JUDGE"]."<br>".${$name2}[$k]["JUDGE_SS"]."</td>\n";
                        //}
                    }
                    $table["data"] .= "</tr>";
                }
                $table["data"] .= "</table>";


                $mock["TABLE"]  = "<div style=\"height:370px;\">\n<div id=\"header_x{$gaku}\" class=\"sunko_header_x\">\n".$table["header_x"]."</div>\n";
                $mock["TABLE"] .= "<div id=\"header_h{$gaku}\" class=\"sunko_header_h\">\n".$table["header_h"]."</div>\n";
                $mock["TABLE"] .= "<div id=\"header_v{$gaku}\" class=\"sunko_header_v\">\n".$table["header_v"]."</div>\n";
                $mock["TABLE"] .= "<div id=\"data{$gaku}\" class=\"sunko_data\">".$table["data"]."</div>\n</div>\n";
                
                
                //スクロールのjavascript部分
                $jsdata .= "\$E(\"data{$gaku}\").onscroll=scroll;\n";
                $jsfunc .= "    \$E(\"header_h{$gaku}\").scrollLeft= \$E(\"data{$gaku}\").scrollLeft;\n";    // 左右連動させる
                $jsfunc .= "    \$E(\"header_v{$gaku}\").scrollTop = \$E(\"data{$gaku}\").scrollTop;\n";    // 上下連動させる*/

                
            }else{
                $mock["TABLE"] = "該当するデータがありません";
            }
            $arg["data"][] = $mock;
            
            $ctrlyear--;
        }
        
        //javascript
        $arg["java"] = "<script type=\"text/javascript\">\n";
        $arg["java"] .= "function \$E(id){ return document.getElementById(id); }\n";
        $arg["java"] .= "function scroll(){\n";
        $arg["java"] .= $jsfunc;
        $arg["java"] .= "}\n";
        $arg["java"] .= $jsdata;
        $arg["java"] .= "</script>\n";


        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjh410_sibouForm1.html", $arg); 
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
    $extra .= " onclick=\"btn_submit('reappear');\"";
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
