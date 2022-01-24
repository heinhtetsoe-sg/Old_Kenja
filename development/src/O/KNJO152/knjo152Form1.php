<?php
class knjo152Form1
{
    function main(&$model) {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knjo152index.php", "", "edit");
        //DB接続
        $db = Query::dbCheckOut();
        
        
        //生徒基本情報
        $query = knjo152Query::getStudent($model->schregno);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if(!empty($row)){
            $arg["SCHREGNO"] = $row["SCHREGNO"];
            $arg["NAME"] = $row["NAME"];
            $handicap = $row["HANDICAP"];
        }
        
        //学年ラジオボタン作成
        $query = knjo152Query::getGrade($model->schregno, $handicap);
        $result = $db->query($query);
        $cnt = 0;
        $opt = array();
        $label = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt[$cnt+1] = $row["GRADE"]; //value
            $idcnt = $cnt+1;
            $idname[$cnt] = "GRADE{$idcnt}";
            $label[$cnt] = $row["CODENAME"];
            
            //原級留置回数
            $stay[$cnt+1] = $row["STAY_CNT"];
            
            $cnt++;
        }
        $option = " onclick=\" btn_submit('grade_change');\"";
        $extraCnt = count($opt);
        $extraRadio = array();
        for($i=0;$i<$extraCnt;$i++){
            $extraRadio[$i] = "id=\"{$idname[$i]}\" {$option}";
        }
        $radioArray = knjCreateRadio($objForm, "GRADE", $model->field["GRADE"], $extraRadio, $opt, count($opt));
        $arg["GRADERADIO"] = "";
        $j=0;
        foreach($radioArray as $key => $val){
            $arg["GRADERADIO"] .= $val."<LABEL for=\"".$idname[$j]."\">".$label[$j]."</LABEL>　　";
            $j++;
        }
        if($model->field["GRADE"] != ""){
            $arg["STAY_CNT"] = $stay[$model->field["GRADE"]];
        }

        //表示ラジオボタン作成
        $cnt = 0;
        $optH = array(1,2,3);
        $label = array("送信元情報", "所見一覧", "成績一覧");

        $option = " onclick=\" btn_submit('type_change');\"";
        //様式種別がK3のとき以外は2つ作る
        if($handicap != "003"){
            $extraCnt = count($optH);
        }else{
            $extraCnt = 2;
        }
        $extraRadio = array();
        for($i=0;$i<$extraCnt;$i++){
            $extraRadio[$i] = "id=\"TYPE{$i}\" {$option}";
        }
        $radioArray = knjCreateRadio($objForm, "TYPE", $model->field["TYPE"], $extraRadio, $optH, $extraCnt);
        $arg["TYPERADIO"] = "";
        $j=0;
        foreach($radioArray as $key => $val){
            $arg["TYPERADIO"] .= $val."<LABEL for=\"TYPE".$j."\">".$label[$j]."</LABEL>　　";
            $j++;
        }
        
        if($model->schregno != "" && $model->field["TYPE"] == "3"){
            //教科のコンボボックス
            $query = knjo152Query::getKyouka($model->schregno, $opt[$model->field["GRADE"]]);
            $result = $db->query($query);
            $optK = array();
            $optK[0] = array("value" => "",
                             "label" => "");
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $optK[] = array("value" => $row["CLASS_NAME"],
                                "label" => $row["KYOUKA"]);
            }
            $optK[] = array("value" => "99",
                            "label" => "その他");
            
            $extra = " onchange=\"btn_submit('kyouka_change');\"";
            
            $arg["data2"]["KYOUKA"] = knjCreateCombo($objForm, "KYOUKA", $model->field["KYOUKA"], $optK, $extra, "1");
        }
        
        if($model->field["GRADE"] != "" && $model->field["TYPE"] == "2"){   //所見一覧
            $query = knjo152Query::getShidouData($model->schregno, $opt[$model->field["GRADE"]], $handicap);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            
            $row["SYUBETU"] = $row["CODENAME"];
            $row["ATTEND_BIKOU"] = $row["BIKOU"];
            if($handicap == "001"){
                $arg["HANDICAP1"] = 1;
            }else if($handicap == "002"){
                $arg["HANDICAP1"] = 1;
                $arg["ZIRITU"] = 1;
            }else{
                $arg["HANDICAP2"] = 1;
            }
            
            $arg["data1"] = $row;
        }else if($model->field["GRADE"] != "" && $model->field["TYPE"] == "3"){     //成績一覧
            $query = knjo152Query::getSeiseki($model->schregno,$opt[$model->field["GRADE"]], $model->field["KYOUKA"]);
            $result = $db->query($query);
            $before = "";
            
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                if($before != $row["KYOUKA"]){
                    $row["KYOUKA_NAME"]  = "<tr>";
                    $row["KYOUKA_NAME"] .= "  <th nowrap bgcolor=\"#ccffcc\" colspan=\"5\">".$row["KYOUKA"]."</th>";
                    $row["KYOUKA_NAME"] .= "</tr>";
                }else{
                    $row["KYOUKA_NAME"] = "";
                }

                $row["KAMOKU_NAME"] = $row["KAMOKU"];
                if($row["SCHOOL_SUBCLASS_NAME"] != ""){
                    $row["HYOUTEI"] = $row["SCHOOL_SUBCLASS_HYOUTEI"];
                    $row["TANNI"] = $row["SCHOOL_SUBCLASS_TANNI"];
                    $row["TANNI_TOTAL"] = $row["SCHOOL_SUBCLASS_TANNI_TOTAL"];
                    $row["BIKOU"] = $row["SCHOOL_SUBCLASS_BIKOU"];
                }else{
                    $row["HYOUTEI"] = $row["SUBCLASS_HYOUTEI"];
                    $row["TANNI"] = $row["SUBCLASS_TANNI"];
                    $row["TANNI_TOTAL"] = $row["SUBCLASS_TANNI_TOTAL"];
                    $row["BIKOU"] = $row["SUBCLASS_BIKOU"];
                }
                
                $before = $row["KYOUKA"];
            
                $arg["data3"][] = $row;
            }
        }else if($model->schregno != "" && $model->field["TYPE"] == "1"){     //送信元情報
            $query = knjo152Query::getSendFrom($model->schregno);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            
            //教育委員会
            if($row["EDU_GNAME"] != ""){
                $row["EDU_NAME"] = $row["EDU_NNAME"]."(".$row["EDU_GNAME"].")";
            }else if($row["EDU_NNAME"] != ""){
                $row["EDU_NAME"] = $row["EDU_NNAME"];
            }
            
            if($row["EDU_GADDR"] != ""){
                $row["EDU_ADDR"] = $row["EDU_NADDR"]."(".$row["EDU_GADDR"].")";
            }else if($row["EDU_NADDR"] != ""){
                $row["EDU_ADDR"] = $row["EDU_NADDR"];
            }
            
            if($row["EDU_GKATAGAKI"] != ""){
                $row["EDU_KATAGAKI"] = $row["EDU_NKATAGAKI"]."(".$row["EDU_GKATAGAKI"].")";
            }else if($row["EDU_NKATAGAKI"] != ""){
                $row["EDU_KATAGAKI"] = $row["EDU_NKATAGAKI"];
            }
            if($row["EDU_ZIPCD"] != ""){
                $row["EDU_ZIPCD"] = "〒 ".$row["EDU_ZIPCD"];
            }
            
            //学校
            if($row["SCHOOL_GNAME"] != ""){
                $row["SCHOOL_NAME"] = $row["SCHOOL_NNAME"]."(".$row["SCHOOL_GNAME"].")";
            }else if($row["SCHOOL_NNAME"] != ""){
                $row["SCHOOL_NAME"] = $row["SCHOOL_NNAME"];
            }
            
            if($row["SCHOOL_GADDR"] != ""){
                $row["SCHOOL_ADDR"] = $row["SCHOOL_NADDR"]."(".$row["SCHOOL_GADDR"].")";
            }else if($row["SCHOOL_NADDR"] != ""){
                $row["SCHOOL_ADDR"] = $row["SCHOOL_NADDR"];
            }
            
            if($row["SCHOOL_GKATAGAKI"] != ""){
                $row["SCHOOL_KATAGAKI"] = $row["SCHOOL_NKATAGAKI"]."(".$row["SCHOOL_GKATAGAKI"].")";
            }else if($row["SCHOOL_NKATAGAKI"] != ""){
                $row["SCHOOL_KATAGAKI"] = $row["SCHOOL_NKATAGAKI"];
            }
            if($row["SCHOOL_BUN_GNAME"] != ""){
                $row["SCHOOL_BUN_NAME"] = $row["SCHOOL_BUN_NNAME"]."(".$row["SCHOOL_BUN_GNAME"].")";
            }else if($row["SCHOOL_BUN_NNAME"] != ""){
                $row["SCHOOL_BUN_NAME"] = $row["SCHOOL_BUN_NNAME"];
            }
            if($row["SCHOOL_ZIPCD"] != ""){
                $row["SCHOOL_ZIPCD"] = "〒 ".$row["SCHOOL_ZIPCD"];
            }
            
            if($row["SCHOOL_BUN_GADDR"] != ""){
                $row["SCHOOL_BUN_ADDR"] = $row["SCHOOL_BUN_NADDR"]."(".$row["SCHOOL_BUN_GADDR"].")";
            }else if($row["SCHOOL_BUN_NKATAGAKI"] != ""){
                $row["SCHOOL_BUN_ADDR"] = $row["SCHOOL_BUN_NADDR"];
            }
            
            if($row["SCHOOL_BUN_GKATAGAKI"] != ""){
                $row["SCHOOL_BUN_KATAGAKI"] = $row["SCHOOL_BUN_NKATAGAKI"]."(".$row["SCHOOL_BUN_GKATAGAKI"].")";
            }else if($row["SCHOOL_BUN_NKATAGAKI"] != ""){
                $row["SCHOOL_BUN_KATAGAKI"] = $row["SCHOOL_BUN_NKATAGAKI"];
            }
            if($row["SCHOOL_BUN_ZIPCD"] != ""){
                $row["SCHOOL_BUN_ZIPCD"] = "〒 ".$row["SCHOOL_BUN_ZIPCD"];
            }


            $arg["data4"] = $row;
        }
        
        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        $arg["IFRAME"] = View::setIframeJs();
        View::toHTML($model, "knjo152Form1.html", $arg);
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
