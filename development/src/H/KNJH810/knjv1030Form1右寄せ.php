<?php
class knjv1030Form1
{
    function main(&$model) {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knjv1030index.php", "", "edit");
        //DB接続
        $db = Query::dbCheckOut();
        
        //年度と学期
        if (!isset($model->exp_year)) $model->exp_year = CTRL_YEAR ."-" .CTRL_SEMESTER;
        $arg["EXP_YEAR"] = CTRL_YEAR;

        //科目コンボ
        $extra = "onChange=\"btn_submit('subclasscd')\";";
        $query = knjv1030Query::getSubclassMst($model);
        makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $model->field["SUBCLASSCD"], $extra, 1, "blank");

        //講座コンボ
        $extra = "onChange=\"btn_submit('chaircd')\";";
        $query = knjv1030Query::selectChairQuery($model);
        makeCmb($objForm, $arg, $db, $query, "CHAIRCD", $model->field["CHAIRCD"], $extra, 1, "blank");

        
        
        //表示内容
        //教科
        $extra = "";
        $kyoka[0] = "すべて";
        $kyoka[1] = "英語";
        $kyoka[2] = "数学";
        $kyoka[3] = "国語";
        
        $opt = array();
        foreach($kyoka as $key => $val){
            $opt[] = array("label"  => $val,
                           "value"  => $key);
        }
        $arg["data"]["KYOKA"] = knjCreateCombo($objForm, "KYOKA", $model->field["KYOKA"], $opt, $extra, "1");
        
        //科目
        if($model->field["KYOKA"] != ""){
            //Queryで中身取得？
        }else{
            $kamoku[0] = "すべて";
            $kamoku[1] = "数学ⅠＡ";
            $kamoku[2] = "数学ⅡＢ";
            $kamoku[3] = "数学ⅢＣ";
        }
        $extra = "";
        $opt2 = array();
        foreach($kamoku as $key => $val){
            $opt2[] = array("label"  => $val,
                            "value"  => $key);
        }
        $arg["data"]["KAMOKU"] = knjCreateCombo($objForm, "KAMOKU", $model->field["KAMOKU"], $opt2, $extra, "1");
        
        //テスト種類
        $extra = "";
        $kind[0] = "すべて";
        $kind[1] = "易";
        $kind[2] = "やや易";
        $kind[3] = "標準レベル";
        $kind[4] = "やや難";
        $kind[5] = "難";
        
        $opt3 = array();
        foreach($kind as $key => $val){
            $opt3[] = array("label"  => $val,
                            "value"  => $key);
        }
        $arg["data"]["TEST_KIND"] = knjCreateCombo($objForm, "TEST_KIND", $model->field["TEST_KIND"], $opt3, $extra, "1", "blank");
                
        //区分
        $extra = " onchange=\"checkCng();\"";
        $change[0] = "回数別";
        $change[1] = "大分野別";
        $change[2] = "小分野別";
        
        $opt4 = array();
        foreach($change as $key => $val){
            $opt4[] = array("label"  => $val,
                            "value"  => $key);
        }
        //$model->field["KUBUN"] = 1;     ///////////////////////////
        $arg["data"]["KUBUN"] = knjCreateCombo($objForm, "KUBUN", $model->field["KUBUN"], $opt4, $extra, "1");
        
        //回数選択
        $extra = "";
        $kaisu = range(1, 27);
        $opt5 = array();
        $opt5[] = array("label"  => "",
                        "value"  => "");
        foreach($kaisu as $key => $val){
            $opt5[] = array("label"  => $val,
                            "value"  => "{$key}");
        }
        $model->field["F_KAISU"] = "";
        $arg["data"]["F_KAISU"] = knjCreateCombo($objForm, "F_KAISU", $model->field["F_KAISU"], $opt5, $extra, "1");
        $model->field["T_KAISU"] = "";
        $arg["data"]["T_KAISU"] = knjCreateCombo($objForm, "T_KAISU", $model->field["T_KAISU"], $opt5, $extra, "1");

        //取込日
        $arg["IMPORTDATE"] = "2016年6月8日";
        
        //header_x部分
        if($model->field["KUBUN"] == "0"){
            $arg["header_x"] = "<th class=\"sum abnormal\"> </th><th class=\"no_search title\">実施回</th></tr>";
            $arg["header_x"] .= "<tr><td class=\"sum abnormal\"> </td><th class=\"no_search title\">配点</th></tr>";
            $arg["header_x"] .= "<tr><td class=\"sum abnormal\"> </td><th class=\"no_search title\">学年平均点</th></tr>";
            $arg["header_x"] .= "<tr><td class=\"sum abnormal\"> </td><th class=\"no_search title\">学年最高点</th></tr>";
            $arg["header_x"] .= "<tr><td class=\"sum abnormal\"> </td><th class=\"no_search title\">クラス平均点</th></tr>";
            $arg["header_x"] .= "<tr><td class=\"sum abnormal\"> </td><th class=\"no_search title\">クラス最高点</th>";
        }else{
            $arg["header_x"] = "<th class=\"no_search title\">実施回</th><th class=\"no_search sum normal\">計</th></tr>";
            $arg["header_x"] .= "<tr><th class=\"no_search title\">配点</th><td class=\"sum normal\">-</td></tr>";
            $arg["header_x"] .= "<tr><th class=\"no_search title\">学年平均点</th><td class=\"sum normal grade\">131.5</td></tr>";
            $arg["header_x"] .= "<tr><th class=\"no_search title\">学年最高点</th><td class=\"sum normal grade\">200</td></tr>";
            $arg["header_x"] .= "<tr><th class=\"no_search title\">クラス平均点</th><td class=\"sum normal\">141</td></tr>";
            $arg["header_x"] .= "<tr><th class=\"no_search title\">クラス最高点</th><td class=\"sum normal\">200</td>";
        }
        
        //left_body部分
        if($model->field["KUBUN"] == "0"){
            $arg["left_body"] = "<th class=\"sum abnormal\"> </th><th class=\"tokuten changecolor\">得点率</th><th class=\"percent changecolor\">&nbsp;100%</th></tr>";
            $arg["left_body"] .= "<tr><th class=\"sum abnormal\"> </th><th class=\"tokuten changecolor\"> </th><th class=\"percent changecolor\">&nbsp;80%～</th></tr>";
            $arg["left_body"] .= "<tr><th class=\"sum abnormal\"> </th><th class=\"tokuten changecolor\"> </th><th class=\"percent changecolor\">&nbsp;60%～</th></tr>";
            $arg["left_body"] .= "<tr><th class=\"sum abnormal\"> </th><th class=\"tokuten changecolor\"> </th><th class=\"percent changecolor\">&nbsp;40%～</th></tr>";
            $arg["left_body"] .= "<tr><th class=\"sum abnormal\"> </th><th class=\"tokuten changecolor\"> </th><th class=\"percent changecolor\">&nbsp;20%～</th></tr>";
            $arg["left_body"] .= "<tr><th class=\"sum abnormal\"> </th><th class=\"tokuten changecolor\"> </th><th class=\"percent changecolor\">&nbsp;0%～</th></tr>";
            $arg["left_body"] .= "<tr><th class=\"sum abnormal\"> </th><th class=\"title changecolor\" colspan=\"2\">人数計</th>";
        }else if($model->field["KUBUN"] == "1"){
            $arg["left_body"] = "<th class=\"changecolor large_field\" rowspan=\"5\">集<br>合<br>と<br>論<br>証</th>";
            $arg["left_body"] .= "<th class=\"changecolor title\">配点</th><td class=\"sum normal\">-</td></tr>";
            $arg["left_body"] .= "<tr><th class=\"changecolor title\">学年平均点</th><td class=\"sum normal grade\">68.6</td></tr>";
            $arg["left_body"] .= "<tr><th class=\"changecolor title\">学年最高点</th><td class=\"sum normal grade\">102</td></tr>";
            $arg["left_body"] .= "<tr><th class=\"changecolor title\">クラス平均点</th><td class=\"sum normal\">73.3</td></tr>";
            $arg["left_body"] .= "<tr><th class=\"changecolor title\">クラス最高点</th><td class=\"sum normal\">102</td></tr>";
            
            $arg["left_body"] .= "<tr><th class=\"changecolor large_field\" rowspan=\"5\">数<br>と<br>式</th>";
            $arg["left_body"] .= "<th class=\"changecolor title\">配点</th><td class=\"sum normal\">-</td></tr>";
            $arg["left_body"] .= "<tr><th class=\"changecolor title\">学年平均点</th><td class=\"sum normal grade\">62.9</td></tr>";
            $arg["left_body"] .= "<tr><th class=\"changecolor title\">学年最高点</th><td class=\"sum normal grade\">98</td></tr>";
            $arg["left_body"] .= "<tr><th class=\"changecolor title\">クラス平均点</th><td class=\"sum normal\">67.8</td></tr>";
            $arg["left_body"] .= "<tr><th class=\"changecolor title\">クラス最高点</th><td class=\"sum normal\">98</td></tr>";
            
            $arg["left_body"] .= "<tr><th class=\"changecolor large_field\" rowspan=\"5\">図<br>形<br>と<br>計<br>量</th>";
            $arg["left_body"] .= "<th class=\"changecolor title\">配点</th><td class=\"sum normal\">-</td></tr>";
            $arg["left_body"] .= "<tr><th class=\"changecolor title\">学年平均点</th><td class=\"sum normal grade\">-</td></tr>";
            $arg["left_body"] .= "<tr><th class=\"changecolor title\">学年最高点</th><td class=\"sum normal grade\">-</td></tr>";
            $arg["left_body"] .= "<tr><th class=\"changecolor title\">クラス平均点</th><td class=\"sum normal\">-</td></tr>";
            $arg["left_body"] .= "<tr><th class=\"changecolor title\">クラス最高点</th><td class=\"sum normal\">-</td></tr>";
            
            $arg["left_body"] .= "<tr><th class=\"changecolor large_field\" rowspan=\"5\">集<br>合<br>と<br>論<br>証</th>";
            $arg["left_body"] .= "<th class=\"changecolor title\">配点</th><td class=\"sum normal\">-</td></tr>";
            $arg["left_body"] .= "<tr><th class=\"changecolor title\">学年平均点</th><td class=\"sum normal grade\">68.6</td></tr>";
            $arg["left_body"] .= "<tr><th class=\"changecolor title\">学年最高点</th><td class=\"sum normal grade\">102</td></tr>";
            $arg["left_body"] .= "<tr><th class=\"changecolor title\">クラス平均点</th><td class=\"sum normal\">73.3</td></tr>";
            $arg["left_body"] .= "<tr><th class=\"changecolor title\">クラス最高点</th><td class=\"sum normal\">102</td></tr>";
            
            $arg["left_body"] .= "<tr><th class=\"changecolor large_field\" rowspan=\"5\">数<br>と<br>式</th>";
            $arg["left_body"] .= "<th class=\"changecolor title\">配点</th><td class=\"sum normal\">-</td></tr>";
            $arg["left_body"] .= "<tr><th class=\"changecolor title\">学年平均点</th><td class=\"sum normal grade\">62.9</td></tr>";
            $arg["left_body"] .= "<tr><th class=\"changecolor title\">学年最高点</th><td class=\"sum normal grade\">98</td></tr>";
            $arg["left_body"] .= "<tr><th class=\"changecolor title\">クラス平均点</th><td class=\"sum normal\">67.8</td></tr>";
            $arg["left_body"] .= "<tr><th class=\"changecolor title\">クラス最高点</th><td class=\"sum normal\">98</td></tr>";
            
            $arg["left_body"] .= "<tr><th class=\"changecolor large_field\" rowspan=\"5\">図<br>形<br>と<br>計<br>量</th>";
            $arg["left_body"] .= "<th class=\"changecolor title\">配点</th><td class=\"sum normal\">-</td></tr>";
            $arg["left_body"] .= "<tr><th class=\"changecolor title\">学年平均点</th><td class=\"sum normal grade\">-</td></tr>";
            $arg["left_body"] .= "<tr><th class=\"changecolor title\">学年最高点</th><td class=\"sum normal grade\">-</td></tr>";
            $arg["left_body"] .= "<tr><th class=\"changecolor title\">クラス平均点</th><td class=\"sum normal\">-</td></tr>";
            $arg["left_body"] .= "<tr><th class=\"changecolor title\">クラス最高点</th><td class=\"sum normal\">-</td></tr>";
        }else{
            $arg["left_body"] = "<th class=\"changecolor large_field\" rowspan=\"15\">集<br>合<br>と<br>論<br>証</th>";
            $arg["left_body"] .= "<th class=\"changecolor small_field\">集合</th><td class=\"sum normal\">-</td></tr>";
            $arg["left_body"] .= "<tr><th class=\"changecolor small_field\">学年平均点</th><td class=\"sum normal grade\">33.3</td></tr>";
            $arg["left_body"] .= "<tr><th class=\"changecolor small_field\">学年最高点</th><td class=\"sum normal grade\">46</td></tr>";
            $arg["left_body"] .= "<tr><th class=\"changecolor small_field\">クラス平均点</th><td class=\"sum normal\">36</td></tr>";
            $arg["left_body"] .= "<tr><th class=\"changecolor small_field\">クラス最高点</th><td class=\"sum normal\">46</td></tr>";
            $arg["left_body"] .= "<tr><th class=\"changecolor small_field\">命題と論証</th><td class=\"sum normal\">-</td></tr>";
            $arg["left_body"] .= "<tr><th class=\"changecolor small_field\">学年平均点</th><td class=\"sum normal grade\">35.3</td></tr>";
            $arg["left_body"] .= "<tr><th class=\"changecolor small_field\">学年最高点</th><td class=\"sum normal grade\">56</td></tr>";
            $arg["left_body"] .= "<tr><th class=\"changecolor small_field\">クラス平均点</th><td class=\"sum normal\">37.4</td></tr>";
            $arg["left_body"] .= "<tr><th class=\"changecolor small_field\">クラス最高点</th><td class=\"sum normal\">56</td></tr>";
            $arg["left_body"] .= "<tr><th class=\"changecolor small_field\">その他</th><td class=\"sum normal\">-</td></tr>";
            $arg["left_body"] .= "<tr><th class=\"changecolor small_field\">学年平均点</th><td class=\"sum normal grade\">-</td></tr>";
            $arg["left_body"] .= "<tr><th class=\"changecolor small_field\">学年最高点</th><td class=\"sum normal grade\">-</td></tr>";
            $arg["left_body"] .= "<tr><th class=\"changecolor small_field\">クラス平均点</th><td class=\"sum normal\">-</td></tr>";
            $arg["left_body"] .= "<tr><th class=\"changecolor small_field\">クラス最高点</th><td class=\"sum normal\">-</td></tr>";
        }
        
        //right_header部分　全部共通
        $arg["right_header"] = "";
        for($i=1;$i<28;$i++){
            $arg["right_header"] .= "<th class=\"no_search right_td\">第".$i."回</th>";
        }
        $arg["right_header"] .= "</tr>";
        
        //配点行
        $arg["right_header"] .= "<tr><td class=\"right_td\">50</td><td class=\"right_td\">50</td><td class=\"right_td\">50</td><td class=\"right_td\">50</td>";
        for($k=5;$k<28;$k++){
            $arg["right_header"] .= "<td class=\"right_td\">-</td>";
        }
        $arg["right_header"] .= "</tr>";
        
        //学年平均点
        $arg["right_header"] .= "<tr><td class=\"right_td grade\">30.2</td><td class=\"right_td grade\">39.9</td><td class=\"right_td grade\">38.6</td><td class=\"right_td grade\">22.8</td>";
        for($k=5;$k<28;$k++){
            $arg["right_header"] .= "<td class=\"right_td grade\">-</td>";
        }
        $arg["right_header"] .= "</tr>";
        
        //学年最高点
        $arg["right_header"] .= "<tr><td class=\"right_td grade\">50</td><td class=\"right_td grade\">50</td><td class=\"right_td grade\">50</td><td class=\"right_td grade\">50</td>";
        for($k=5;$k<28;$k++){
            $arg["right_header"] .= "<td class=\"right_td grade\">-</td>";
        }
        $arg["right_header"] .= "</tr>";

        //クラス平均点
        $arg["right_header"] .= "<tr><td class=\"right_td\">32.4</td><td class=\"right_td\">41.3</td><td class=\"right_td\">41.6</td><td class=\"right_td\">25.7</td>";
        for($k=5;$k<28;$k++){
            $arg["right_header"] .= "<td class=\"right_td\">-</td>";
        }
        $arg["right_header"] .= "</tr>";
        
        //クラス最高点
        $arg["right_header"] .= "<tr><td class=\"right_td\">50</td><td class=\"right_td\">50</td><td class=\"right_td\">50</td><td class=\"right_td\">50</td>";
        for($k=5;$k<28;$k++){
            $arg["right_header"] .= "<td class=\"right_td\">-</td>";
        }
        
        //#holizonの代わりに直接値決めて入れる
        $width = 80*27;
        $arg["width"] = "style=\"width:".$width."px;\"";

        //right_body部分
        if($model->field["KUBUN"] == "0"){
            //100%行
            $arg["right_body"] .= "<td class=\"right_td\">2</td><td class=\"right_td\">1</td><td class=\"right_td\">1</td><td class=\"right_td\">1</td>";
            for($k=5;$k<28;$k++){
                $arg["right_body"] .= "<td class=\"right_td\">-</td>";
            }
            $arg["right_body"] .= "</tr>";
            //80%行
            $arg["right_body"] .= "<tr><td class=\"right_td\">2</td><td class=\"right_td\">15</td><td class=\"right_td\">9</td><td class=\"right_td\">1</td>";
            for($k=5;$k<28;$k++){
                $arg["right_body"] .= "<td class=\"right_td\">-</td>";
            }
            $arg["right_body"] .= "</tr>";
            //60%行
            $arg["right_body"] .= "<tr><td class=\"right_td\">15</td><td class=\"right_td\">10</td><td class=\"right_td\">5</td><td class=\"right_td\">2</td>";
            for($k=5;$k<28;$k++){
                $arg["right_body"] .= "<td class=\"right_td\">-</td>";
            }
            $arg["right_body"] .= "</tr>";
            //40%行
            $arg["right_body"] .= "<tr><td class=\"right_td\">15</td><td class=\"right_td\">10</td><td class=\"right_td\">5</td><td class=\"right_td\">2</td>";
            for($k=5;$k<28;$k++){
                $arg["right_body"] .= "<td class=\"right_td\">-</td>";
            }
            $arg["right_body"] .= "</tr>";
            //20%行
            $arg["right_body"] .= "<tr><td class=\"right_td\">2</td><td class=\"right_td\">1</td><td class=\"right_td\">1</td><td class=\"right_td\">3</td>";
            for($k=5;$k<28;$k++){
                $arg["right_body"] .= "<td class=\"right_td\">-</td>";
            }
            $arg["right_body"] .= "</tr>";
            //0%行
            $arg["right_body"] .= "<tr><td class=\"right_td\">1</td><td class=\"right_td\">0</td><td class=\"right_td\">1</td><td class=\"right_td\">0</td>";
            for($k=5;$k<28;$k++){
                $arg["right_body"] .= "<td class=\"right_td\">-</td>";
            }
            $arg["right_body"] .= "</tr>";
            //人数行
            $arg["right_body"] .= "<tr><td class=\"right_td\">37</td><td class=\"right_td\">29</td><td class=\"right_td\">19</td><td class=\"right_td\">12</td>";
            for($k=5;$k<28;$k++){
                $arg["right_body"] .= "<td class=\"right_td\">-</td>";
            }
            $arg["right_body"] .= "</tr>";
        }else if($model->field["KUBUN"] == "1"){
            for($j=0;$j<6;$j++){
                if($j != 0){
                    $arg["right_body"] .= "</tr>";
                }
                //集合と論証行
                $arg["right_body"] .= "<td class=\"right_td\">16</td><td class=\"right_td\">26</td><td class=\"right_td\">30</td><td class=\"right_td\">30</td>";
                for($k=5;$k<28;$k++){
                    $arg["right_body"] .= "<td class=\"right_td\">-</td>";
                }
                $arg["right_body"] .= "</tr>";
                //学年平均行
                $arg["right_body"] .= "<tr><td class=\"right_td grade\">10.2</td><td class=\"right_td grade\">21.3</td><td class=\"right_td grade\">22.9</td><td class=\"right_td grade\">14.2</td>";
                for($k=5;$k<28;$k++){
                    $arg["right_body"] .= "<td class=\"right_td grade\">-</td>";
                }
                $arg["right_body"] .= "</tr>";
                //学年最高行
                $arg["right_body"] .= "<tr><td class=\"right_td grade\">16</td><td class=\"right_td grade\">26</td><td class=\"right_td grade\">30</td><td class=\"right_td grade\">30</td>";
                for($k=5;$k<28;$k++){
                    $arg["right_body"] .= "<td class=\"right_td grade\">-</td>";
                }
                $arg["right_body"] .= "</tr>";
                //クラス平均行
                $arg["right_body"] .= "<tr><td class=\"right_td\">10.6</td><td class=\"right_td\">21.7</td><td class=\"right_td\">23.7</td><td class=\"right_td\">17.3</td>";
                for($k=5;$k<28;$k++){
                    $arg["right_body"] .= "<td class=\"right_td\">-</td>";
                }
                $arg["right_body"] .= "</tr>";
                //クラス最高行
                $arg["right_body"] .= "<tr><td class=\"right_td\">16</td><td class=\"right_td\">26</td><td class=\"right_td\">26</td><td class=\"right_td\">30</td>";
                for($k=5;$k<28;$k++){
                    $arg["right_body"] .= "<td class=\"right_td\">-</td>";
                }
            }
        }else{
            for($j=0;$j<3;$j++){
                if($j != 0){
                    $arg["right_body"] .= "</tr>";
                }
                //集合と論証行
                $arg["right_body"] .= "<td class=\"right_td\">8</td><td class=\"right_td\">10</td><td class=\"right_td\">14</td><td class=\"right_td\">14</td>";
                for($k=5;$k<28;$k++){
                    $arg["right_body"] .= "<td class=\"right_td\">-</td>";
                }
                $arg["right_body"] .= "</tr>";
                //学年平均行
                $arg["right_body"] .= "<tr><td class=\"right_td grade\">6.1</td><td class=\"right_td grade\">8.9</td><td class=\"right_td grade\">10.7</td><td class=\"right_td grade\">7.6</td>";
                for($k=5;$k<28;$k++){
                    $arg["right_body"] .= "<td class=\"right_td grade\">-</td>";
                }
                $arg["right_body"] .= "</tr>";
                //学年最高行
                $arg["right_body"] .= "<tr><td class=\"right_td grade\">8</td><td class=\"right_td grade\">10</td><td class=\"right_td grade\">14</td><td class=\"right_td grade\">14</td>";
                for($k=5;$k<28;$k++){
                    $arg["right_body"] .= "<td class=\"right_td grade\">-</td>";
                }
                $arg["right_body"] .= "</tr>";
                //クラス平均行
                $arg["right_body"] .= "<tr><td class=\"right_td\">6.7</td><td class=\"right_td\">9.3</td><td class=\"right_td\">10.7</td><td class=\"right_td\">9.3</td>";
                for($k=5;$k<28;$k++){
                    $arg["right_body"] .= "<td class=\"right_td\">-</td>";
                }
                $arg["right_body"] .= "</tr>";
                //クラス最高行
                $arg["right_body"] .= "<tr><td class=\"right_td\">8</td><td class=\"right_td\">10</td><td class=\"right_td\">14</td><td class=\"right_td\">14</td>";
                for($k=5;$k<28;$k++){
                    $arg["right_body"] .= "<td class=\"right_td\">-</td>";
                }
            }
        }
        
        
        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        $arg["IFRAME"] = View::setIframeJs();
        View::toHTML($model, "knjv1030Form1.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //表示
    $arg["button"]["btn_search"] = createBtn($objForm, "btn_search", "表 示", "");
    //CSV
    $arg["button"]["btn_csv"] = createBtn($objForm, "btn_csv", "CSV出力", "");
    //終了
    $arg["button"]["btn_end"] = createBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//Hidden作成
function makeHidden(&$objForm, $model)
{
    $objForm->ae(createHiddenAe("cmd"));
    $objForm->ae(createHiddenAe("SCHREGNO", $model->schregno));
    $objForm->ae(createHiddenAe("sort", $model->sort));
}

//ボタン作成
function createBtn(&$objForm, $name, $value, $extra)
{
    $objForm->ae( array("type"      => "button",
                        "name"      => $name,
                        "value"     => $value,
                        "extrahtml" => $extra));
    return $objForm->ge($name);
}

//チェックボックス作成
function createCheckBox(&$objForm, $name, $value, $extra, $multi)
{

    $objForm->ae( array("type"      => "checkbox",
                        "name"      => $name,
                        "value"     => $value,
                        "extrahtml" => $extra,
                        "multiple"  => $multi));

    return $objForm->ge($name);
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $opt = array();
    $cnt = 0;
    if ($blank == "blank") $opt[] = array('label' => "", 'value' => "");
    if ($blank == "blank") $cnt++;
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
        if ($name == "CHAIRCD") {
            knjCreateHidden($objForm, "LIST_CHAIRCD" . $row["VALUE"], $cnt);
            $cnt++;
        }
    }
    $result->free();

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//Hidden作成ae
function createHiddenAe($name, $value = "")
{
    $opt_hidden = array();
    $opt_hidden = array("type"      => "hidden",
                        "name"      => $name,
                        "value"     => $value);
    return $opt_hidden;
}

?>
