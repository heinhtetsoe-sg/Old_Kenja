<?php
class knjv1010Form1
{
    function main(&$model) {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knjv1010index.php", "", "edit");
        //DB接続
        $db = Query::dbCheckOut();
        
        //生徒情報取得
        //$model->gakusekiを使って取得する
        $arg["GRADE"] = "1-1-001";
        $arg["NAME"] = "アルプ太郎";
        
        
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
        $arg["data"]["TEST_KIND"] = knjCreateCombo($objForm, "TEST_KIND", $model->field["TEST_KIND"], $opt3, $extra, "1");
        
        //切り替え
        $extra = " onchange=\"checkCng();\"";
        $change[0] = "受験回数別";
        $change[1] = "大分野別";
        $change[2] = "小分野別";
        
        $opt4 = array();
        foreach($change as $key => $val){
            $opt4[] = array("label"  => $val,
                            "value"  => $key);
        }
        //$model->field["KIRIKAE"] = 1;
        $arg["data"]["KIRIKAE"] = knjCreateCombo($objForm, "KIRIKAE", $model->field["KIRIKAE"], $opt4, $extra, "1");
        
        
    for($k=0;$k<28;$k++){
        if($model->field["KIRIKAE"] != "1"){
            if($k == 0){
                $k++;
            }
        }
        
        $result["KYOKA"] = "数学";
        $result["KAMOKU"] = "数学ⅠＡ";
        $kai = $k ;
        $result["KAISU"] = "第".$kai."回";
        $result["TEST_KIND"] = "標準レベル";
        $result["NAIYO"] = "数学基礎";
        $result["HAITEN"] = "50";
        
        if($model->field["KIRIKAE"] == "1"){
            if($k==0){
                $resu["left_b"] = "<td nowrap>計</td><td nowrap colspan=\"2\">得点率</td>";
                $arg["left_f"] = $resu["left_b"];
            }else{
                $resu["left_b"] = "<td nowrap>".$result["KAISU"]."</td><td nowrap>".$result["NAIYO"]."</td><td nowrap>".$result["HAITEN"]."</td>";
            }
        }else{
            $resu["left_b"] = "<td nowrap>".$result["KAISU"]."</td><td nowrap>".$result["NAIYO"]."</td><td nowrap>".$result["HAITEN"]."</td>";
        }
        
        //適当なデータ
        if($model->field["KIRIKAE"] == "0"){
            $arg["example"] = "得点(日付)";
            $resu["right_h"] = "";
            for($i=1;$i<11;$i++){
                $resu["right_h"] .= "<th class=\"no_search\">".$i."回目</th>";
            }
            $right = $resu["right_h"];

            $a = rand(0, 50);
            $b = rand(0, 50);
            $c = rand(0, 50);
            $result["FIRST"] = $a."(6/1)";
            $result["SECOND"] = $b."(7/1)";
            $result["THIRD"] = $c."(8/1)";
            $result["FOURTH"] = "-";
            $result["FIFTH"] = "-";
            
            $resu["right_b"] = "<td nowrap>".$result["FIRST"]."</td><td nowrap>".$result["SECOND"]."</td><td nowrap>".$result["THIRD"]."</td><td nowrap>".$result["FOURTH"]."</td><td nowrap>".$result["FIFTH"]."</td>";
            $resu["right_b"] .= "<td nowrap>".$result["FIRST"]."</td><td nowrap>".$result["SECOND"]."</td><td nowrap>".$result["THIRD"]."</td><td nowrap>".$result["FOURTH"]."</td><td nowrap>".$result["FIFTH"]."</td>";


        }else if($model->field["KIRIKAE"] == "2"){
            $arg["example"] = "得点/配点";
            $resu["right_h"] = "";
            $resu["right_h"] = "<th colspan=\"3\" class=\"no_search\" style=\"width:342px;\">集合と論証</th><th colspan=\"3\" class=\"no_search\" style=\"width:342px;\">数と式</th>";
            $resu["right_h"] .= "<th colspan=\"3\" class=\"no_search\" style=\"width:342px;\">集合と論証</th><th colspan=\"3\"  style=\"width:342px;\" class=\"no_search\">数と式</th></tr>";
            $resu["right_h"] .= "<tr><th class=\"no_search\">集合</th><th class=\"no_search\">命題と論証</th><th class=\"no_search\">その他</th><th class=\"no_search\">実数</th><th class=\"no_search\">2次の式の計算</th><th class=\"no_search\">その他</th>";
            $resu["right_h"] .= "<th class=\"no_search\">集合</th><th class=\"no_search\">命題と論証</th><th class=\"no_search\">その他</th><th class=\"no_search\">実数</th><th class=\"no_search\">2次の式の計算</th><th class=\"no_search\">その他</th>";
            
            $right = $resu["right_h"];
            
            $a = rand(1, 10);
            $b = rand(1, 15);
            $c = rand(1, 5);
            $d = rand(1, 10);
            $e = rand(1, 15);
            $result["FIRST"] = rand(0, $a)."/".$a;
            $result["SECOND"] = rand(0, $b)."/".$b;
            $result["THIRD"] = rand(0, $c)."/".$c;
            $result["FOURTH"] = rand(0, $d)."/".$d;
            $result["FIFTH"] = rand(0, $e)."/".$e;
            $result["SIXTH"] = "-";
            
            $resu["right_b"] = "<td nowrap>".$result["FIRST"]."</td><td nowrap>".$result["SECOND"]."</td><td nowrap>".$result["THIRD"]."</td><td nowrap>".$result["FOURTH"]."</td><td nowrap>".$result["FIFTH"]."</td>";
            $resu["right_b"] .= "<td nowrap>".$result["SIXTH"]."</td>";
            $resu["right_b"] .= "<td nowrap>".$result["FIRST"]."</td><td nowrap>".$result["SECOND"]."</td><td nowrap>".$result["THIRD"]."</td><td nowrap>".$result["FOURTH"]."</td><td nowrap>".$result["FIFTH"]."</td>";
            $resu["right_b"] .= "<td nowrap>".$result["SIXTH"]."</td>";

        }else{
            $arg["example"] = "得点/配点";
            $resu["right_h"] = "";
            $resu["right_h"] .= "<th class=\"no_search\">集合と論証</th><th class=\"no_search\">数と式</th><th class=\"no_search\">図形と計量</th><th class=\"no_search\">方程式･不等式</th><th class=\"no_search\">整数</th><th class=\"no_search\">行列とその応用</th>";
            $resu["right_h"] .= "<th class=\"no_search\">集合と論証</th><th class=\"no_search\">数と式</th><th class=\"no_search\">図形と計量</th><th class=\"no_search\">方程式･不等式</th><th class=\"no_search\">-</th><th class=\"no_search\">-</th>";
            
            $right = $resu["right_h"];
            
            $a = rand(5, 20);
            $b = rand(5, 20);
            $c = rand(5, 20);
            $d = rand(5, 20);
            $result["FIRST"] = rand(0, $a)."/".$a;
            $result["SECOND"] = rand(0, $b)."/".$b;
            $result["THIRD"] = rand(0, $c)."/".$c;
            $result["FOURTH"] = rand(0, $d)."/".$d;
            $result["FIFTH"] = "-";
            $result["SIXTH"] = "-";
            
            
            if($k != 0){
                $resu["right_b"] = "<td nowrap>".$result["FIRST"]."</td><td nowrap>".$result["SECOND"]."</td><td nowrap>".$result["THIRD"]."</td><td nowrap>".$result["FOURTH"]."</td><td nowrap>".$result["FIFTH"]."</td>";
                $resu["right_b"] .= "<td nowrap>".$result["SIXTH"]."</td>";
                $resu["right_b"] .= "<td nowrap>".$result["FIRST"]."</td><td nowrap>".$result["SECOND"]."</td><td nowrap>".$result["THIRD"]."</td><td nowrap>".$result["FOURTH"]."</td><td nowrap>".$result["FIFTH"]."</td>";
                $resu["right_b"] .= "<td nowrap>".$result["SIXTH"]."</td>";
            }else{
                $resu["right_b"] = "<td nowrap>50</td><td nowrap>40</td><td nowrap>70</td><td nowrap>80</td><td nowrap>-</td>";
                $resu["right_b"] .= "<td nowrap>-</td>";
                $resu["right_b"] .= "<td nowrap>20</td><td nowrap>50</td><td nowrap>70</td><td nowrap>80</td><td nowrap>-</td>";
                $resu["right_b"] .= "<td nowrap>-</td>";
                
                $arg["right_f"] = $resu["right_b"];
            }
        }
        
        $arg["data2"][] = $resu;
    }
            //#holizonの代わり
            $width = 114*12;
            $arg["width"] = "style=\"width:".$width."px;\"";
            $arg["right_h"] = $right;
        
        //スクロールのjavascript部分
        if($model->field["KIRIKAE"] != "1"){
            $jsdata .= "\$E(\"right_body2\").onscroll=scroll;\n";
            $jsfunc .= "    \$E(\"right_header\").scrollLeft= \$E(\"right_body2\").scrollLeft;\n";    // 左右連動させる
            $jsfunc .= "    \$E(\"left_body2\").scrollTop = \$E(\"right_body2\").scrollTop;\n";    // 上下連動させる*/
            
            $arg["left_b_id"] = "left_body2";
            $arg["right_b_id"] = "right_body2";
        }else{
            $jsdata .= "\$E(\"right_body\").onscroll=scroll;\n";
            $jsfunc .= "    \$E(\"right_header\").scrollLeft = \$E(\"right_f\").scrollLeft = \$E(\"right_body\").scrollLeft;\n";    // 左右連動させる
            $jsfunc .= "    \$E(\"left_body\").scrollTop = \$E(\"right_body\").scrollTop;\n";    // 上下連動させる*/
            
            $arg["left_b_id"] = "left_body";
            $arg["right_b_id"] = "right_body";
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

        //hidden
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        $arg["IFRAME"] = View::setIframeJs();
        View::toHTML($model, "knjv1010Form1.html", $arg);
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
