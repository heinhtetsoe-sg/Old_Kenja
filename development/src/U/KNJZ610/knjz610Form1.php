<?php

require_once('for_php7.php');

class knjz610Form1
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }


        //フォーム作成
        $objForm = new form;

        $arg["start"]   = $objForm->get_start("main", "POST", "knjz610index.php", "", "main");

        //データベース接続
        $db = Query::dbCheckOut();


        //年度
        $arg["CTRLYEAR"] = $model->year."年度";
        $ctrlyear = $model->year;
        //学年
        //SCHREG_REGD_BASE_MSTから取得
        $schregQuery = knjz610Query::getSchreg($model->year, $model->GAKUSEKI);
        $schregRow = $db->getRow($schregQuery, DB_FETCHMODE_ASSOC);
        
        $model->gakunen = number_format($schregRow["GRADE"]);

        $arg["GAKUNEN"] = $model->gakunen."年生";
        //名前
        $arg["NAME"] = $schregRow["HR_CLASS"]."組　".$schregRow["ATTENDNO"]."番　".$schregRow["NAME_SHOW"];

        //業者
        $extra = " style=\"font-size:110%;\"";
        $array = array("0" => "全表示",
                       "1" => "ベネッセ",
                       "2" => "駿台");
        $opt = array();
        foreach($array as $key => $val){
            $opt[] = array("label"  => $val,
                           "value"  => $key);
        }
        //$model->field["GYOUSYA"] = 0;
        $arg["GYOUSYA"] = knjCreateCombo($objForm, "GYOUSYA", $model->field["GYOUSYA"], $opt, $extra, 1);

        //データ
        //$model->GAKUSEKI = "13100020";
        //$ctrlyear = CTRL_YEAR;
        //$ctrlyear = '2016';
        //業者
        if($model->field["GYOUSYA"] == "0"){
            $gyousya = "";
        }else{
            //業者コード良く分からないのでとりあえず
            $gyousya = sprintf("%08d", $model->field["GYOUSYA"]);
        }
        //js部分用
        $jsfunc = "";
        $jsdata = "";

        for($gaku=$model->gakunen;$gaku>0;$gaku--){
            //SCHOOL_CDの配列用
            $j = 0;
            $school = array();
            //CTRL_YEARと学籍番号で模試データ取得したい
            $mock["NENDO"] = $ctrlyear."年度　".$gaku."年次模試";
            $cntQuery = knjz610Query::getMockCnt($ctrlyear, $model->GAKUSEKI, $gyousya, "1");  //カウント
            $cnt = $db->getOne($cntQuery);
            //$cnt = 1;
            if($cnt > 0){
                //header_x部分
                $table["header_x"]  = "<table>";
                $table["header_x"] .= "<tr><th class=\"no_search\">大学･学部･学科･日程･方式</th></tr></table>";
                
                //模試志望校数の最大値と模試回数取得
                $cntQuery = knjz610Query::getMockCnt($ctrlyear, $model->GAKUSEKI, $gyousya, "2");
                $cntRow = $db->getRow($cntQuery, DB_FETCHMODE_ASSOC);
                
                //$hopeMax = $cntRow["CNT_MAX"];      //志望校数最大値
                $mosiCnt = $cntRow["MOCK_CNT"];     //模試回数
                
                
                //模試データ(CDとか取得)
                $mosiQuery = knjz610Query::getMockCnt($ctrlyear, $model->GAKUSEKI, $gyousya, "0");
                $mosiResult = $db->query($mosiQuery);
                
                
                $width = 150*$mosiCnt;     //回数分かける
                
                $table["header_h"]  = "<table style=\"width:{$width}px;\">\n<tr>\n";
                
                //模試回数分カウントする変数
                $roop = 1;
                //以下ループ
                while($mosiRow = $mosiResult->fetchRow(DB_FETCHMODE_ASSOC)){
                    //右側header部分
                    $table["header_h"] .= "<th class=\"no_search\">".$gaku."年　".$mosiRow["MOCKNAME1"]."</th>\n";
                    
                    //模試ごとに志望校CDと評価取得して右側データ部分を配列に入れる
                    $hopeQuery = knjz610Query::getHopeGaku($mosiRow["MOCKCD"], $model->GAKUSEKI, $ctrlyear);
                    $hopeResult = $db->query($hopeQuery);
                    $name = "data".$roop;
                    ${$name} = array();

                    while($hopeRow = $hopeResult->fetchRow(DB_FETCHMODE_ASSOC)){
                        //判定数値を小数点以下1桁にする
                        if($hopeRow["JUDGE_SS"] != ""){
                            $judge = number_format($hopeRow["JUDGE_SS"],1);
                        }else{
                            $judge = "";
                        }
                        //配列に入れる
                        $name = "data".$roop;
                        ${$name}[$hopeRow["GYOUSYA"].$hopeRow["SCHOOL_CD"]] = $hopeRow["SEQ"]." : ".$hopeRow["JUDGE"]." : ".$judge;
                        
                        //とりあえず全部入れちゃう
                        $school[$j]["SCHOOL_CD"] = $hopeRow["GYOUSYA"].$hopeRow["SCHOOL_CD"];
                        $school[$j]["GAKKO"] = $hopeRow["GAKKO"];
                        $school[$j]["NITTEI"] = $hopeRow["NITTEI"];
                        $j++;
                    }
                    
                    $roop++;
                    
                }

                $table["header_h"] .= "</tr>\n</table>\n";

                //左側の学部のループ(header_v部分)
                $table["header_v"]  = "<table>\n";
                //data部分作成
                $table["data"]  = "<table style=\"width:{$width}px;\">\n";

                //なぜかSCHOOL_CD重複してしまうので無理やり重複削除
                $tmp = [];
                $uniqueSchool = [];
                foreach ($school as $school){
                   if (!in_array($school['SCHOOL_CD'], $tmp)) {
                      $tmp[] = $school['SCHOOL_CD'];
                      $uniqueSchool[] = $school;
                   }
                }
                $school = $uniqueSchool;    //重複削除した配列($school)

                $schCnt = get_count($school);
                
                for($k=0;$k<$schCnt;$k++){     //$school数繰り返す
                    //学校情報分割する
                    $gakko = array();
                    $gakko = explode(" ", $school[$k]["GAKKO"]);
                    $gakkoname = "";
                    $br = "";
                    if($school[$k]["NITTEI"] != ""){
                        for($aa = 0;$aa<4;$aa++){
                            if($gakko[$aa] != ""){
                                $gakkoname .= $br.mb_substr($gakko[$aa], 0, 7);
                                $br = "<BR>";
                            }
                        }
                        $gakkoname .= "<BR>".$school[$k]["NITTEI"];
                    }else{
                        for($aa = 0;$aa<5;$aa++){
                            if($gakko[$aa] != ""){
                                $gakkoname .= $br.mb_substr($gakko[$aa], 0, 7);
                                $br = "<BR>";
                            }
                        }
                    }
                   //header_v部分
                    $table["header_v"] .= "<tr>\n<td class=\"no_search\">".$gakkoname."</td>\n</tr>\n";
                    //data部分
                    $table["data"] .= "<tr>";
                    for($m=1;$m<$roop;$m++){
                        $name2 = "data".$m;
                        if(array_key_exists($school[$k]["SCHOOL_CD"], ${$name2})){
                            $table["data"] .= "<td class=\"gakko\">".${$name2}[$school[$k]["SCHOOL_CD"]]."</td>\n";
                        }else{
                            $table["data"] .= "<td class=\"gakko\">　　</td>\n";
                        }
                    }
                    $table["data"] .= "</tr>";
                }
                $table["data"] .= "</table>";
                $table["header_v"] .= "</table>\n";

                $mock["TABLE"]  = "<div style=\"height:678px;\">\n<div id=\"header_x{$gaku}\" class=\"sunko_header_x\">\n".$table["header_x"]."</div>\n";
                $mock["TABLE"] .= "<div id=\"header_h{$gaku}\" class=\"sunko_header_h\"\">\n".$table["header_h"]."</div>\n";
                $mock["TABLE"] .= "<div id=\"header_v{$gaku}\" class=\"sunko_header_v\">\n".$table["header_v"]."</div>\n";
                $mock["TABLE"] .= "<div id=\"data{$gaku}\" class=\"sunko_data\">".$table["data"]."</div>\n</div>\n";
                
                
                //スクロールのjavascript部分
                $jsdata .= "\$E(\"data{$gaku}\").onscroll=scroll;\n";
                $jsfunc .= "    \$E(\"header_h{$gaku}\").scrollLeft= \$E(\"data{$gaku}\").scrollLeft;\n";    // 左右連動させる
                $jsfunc .= "    \$E(\"header_v{$gaku}\").scrollTop = \$E(\"data{$gaku}\").scrollTop;\n";    // 上下連動させる*/

                $mock["ALERT"]  = "<BR>　志望順 : 判定評価 : 判定数値<BR><BR>";
                $mock["ALERT"] .= "　※センター試験を1段階選抜でのみ利用する募集単位には｢*｣を出力し、<BR>";
                $mock["ALERT"] .= "　　進研模試では｢1次単純｣集計の偏差値と判定を表示しています。<BR>";
                $mock["ALERT"] .= "　　判定の意味　　　　A:合格可能性80%以上　B:60%以上　C:40%以上　D:20%以上<BR>";
                $mock["ALERT"] .= "　　　　　　　　　　　E:20%未満　　　M:学科試験なし　　　N:集計科目不足<BR><BR>";
                
            }else{
                $mock["TABLE"] = "該当するデータがありません";
                $mock["ALERT"] = "";
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
        View::toHTML($model, "knjz610Form1.html", $arg); 
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
