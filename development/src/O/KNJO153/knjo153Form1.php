<?php
require_once('for_php7.php');
class knjo153Form1
{
    function main(&$model) {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knjo153index.php", "", "edit");
        //DB接続
        $db = Query::dbCheckOut();
        
        
        //生徒基本情報
        $query = knjo153Query::getStudent($model->schregno);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if(!empty($row)){
            $arg["SCHREGNO"] = $row["SCHREGNO"];
            $arg["NAME"] = $row["NAME"];
            $handicap = $row["HANDICAP"];
        }
        
        //学年ラジオボタン作成
        $query = knjo153Query::getGrade($model->schregno);
        $result = $db->query($query);
        $cnt = 0;
        $opt = array();
        $label = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt[$cnt+1] = $row["GRADE"]; //value
            $idcnt = $cnt+1;
            $idname[$cnt] = "GRADE{$idcnt}";
            $label[$cnt] = $row["GRADENAME"];
            
            //原級留置回数
            if($row["SYUBETU"] != "01"){
                $syubetu[$cnt+1]["LABEL"] = $row["SYUBETUNAME"];
            }else{
                $syubetu[$cnt+1]["LABEL"] = "健常者";
            }
            $syubetu[$cnt+1]["VALUE"] = $row["SYUBETU"];
            
            $cnt++;
        }
        $option = " onclick=\" btn_submit('grade_change');\"";
        $extraCnt = get_count($opt);
        $extraRadio = array();
        for($i=0;$i<$extraCnt;$i++){
            $extraRadio[$i] = "id=\"{$idname[$i]}\" {$option}";
        }
        $radioArray = knjCreateRadio($objForm, "GRADE", $model->field["GRADE"], $extraRadio, $opt, get_count($opt));
        $arg["GRADERADIO"] = "";
        $j=0;
        foreach($radioArray as $key => $val){
            $arg["GRADERADIO"] .= $val."<LABEL for=\"".$idname[$j]."\">".$label[$j]."</LABEL>　　";
            $j++;
        }
        if($model->field["GRADE"] != ""){
            $arg["SYUBETU"] = $syubetu[$model->field["GRADE"]]["LABEL"];
            $dataSyubetu = $syubetu[$model->field["GRADE"]]["VALUE"];
        }

        //表示ラジオボタン作成
        $cnt = 0;
        $optH = array(1,2);
        $label = array("送信元情報", "学習者情報");

        $option = " onclick=\" btn_submit('type_change');\"";
        $extraRadio = array();
        $extraCnt = get_count($optH);
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

        
        //とりあえずデータ取得
        if($model->field["TYPE"] != 1){
            if($model->schregno != "" && $model->field["GRADE"] != ""){
                $query = knjo153Query::getShidou($model->schregno, $opt[$model->field["GRADE"]]);
                $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                
                if($dataSyubetu == "02" && $row["JIRITU_ACT"] != ""){
                    $arg["ZIRITU"] = 1;
                    $arg["colspan"] = "colspan=\"2\"";
                    
                    $row["ZIRITU_ACTION"] = $row["JIRITU_ACT"];
                }
                $row["SYOKEN"] = $row["GENERAL_FINDINGS"];
                $row["ATTEND_BIKOU"] = $row["BIKOU"];
                
                $arg["data"] = $row;
            }
            
            //各教科の学習の記録
            $query = knjo153Query::getPointKyouka($model->schregno, $opt[$model->field["GRADE"]]);  //教科と観点の個数を取得する
            $result = $db->query($query);
            while($krow = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $arg["STUDY"] = 1;
                //教科ごとに観点と評価・評定を取得する
                $query = knjo153Query::getPointNaiyo($model->schregno, $krow["UPPER_DATA_ROW"], $krow["SUBJECT"]);
                $pResult = $db->query($query);
                $cnt = 0;
                while($row = $pResult->fetchRow(DB_FETCHMODE_ASSOC)){
                    if($cnt == 0){
                        if($krow["KYOUKA"] != ""){
                            $row["KYOUKANAME"] = "<td nowrap bgcolor=\"#ffffff\" rowspan=\"{$krow["CNT"]}\">　".$krow["KYOUKA"]."</td>";
                        }else{
                            $row["KYOUKANAME"] = "<td nowrap bgcolor=\"#ffffff\" rowspan=\"{$krow["CNT"]}\">　".$krow["SUBJECT"]."</td>";
                        }
                        $row["HYOUTEI"] = "<td nowrap bgcolor=\"#ffffff\" rowspan=\"{$krow["CNT"]}\">　".$krow["SCORE"]."</td>";
                    }else{
                        $row["KYOUKANAME"] = "";
                        $row["HYOUTEI"] = "";
                    }
                    if($row["CODENAME"] != ""){
                        $row["POINT"] = $row["CODENAME"];
                    }else{
                        $row["POINT"] = $row["OTHER_POINT"];
                    }
                    
                    $arg["data1"][] = $row;
                    
                    $cnt++;
                }
            }
            
            //外国語活動の記録
            if(substr($opt[$model->field["GRADE"]], 0, 1) == "P" && $dataSyubetu != "03"){
                $query = knjo153Query::getForeign($model->schregno, $opt[$model->field["GRADE"]]);
                $result = $db->query($query);
                while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                    $arg["FOREIGN"] = 1;
                    
                    if($row["OTHER_POINT"] != ""){
                        $row["POINT"] = $row["OTHER_POINT"];
                    }else{
                        $row["POINT"] = $row["CODENAME"];
                    }
                    
                    $arg["data2"][] = $row;
                }
            }
            
            //総合的な学習の時間の記録
            $query = knjo153Query::getInteg($model->schregno, $opt[$model->field["GRADE"]]);
            $cResult = $db->query($query);
            while($cRow = $cResult->fetchRow(DB_FETCHMODE_ASSOC)){
                $query = knjo153Query::getIntegPoint($model->schregno, $cRow["UPPER_DATA_ROW"], $cRow["DATA_ROW"]);
                $result = $db->query($query);
                $cnt = 0;
                while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                    $arg["INTEGRATED"] = 1;
                    if($cnt == 0){
                        $row["ACTION"] = "<td bgcolor=\"#ffffff\" rowspan=\"{$cRow["CNT"]}\">　".$cRow["ACTION"]."</td>";
                        $row["SCORE"] = "<td bgcolor=\"#ffffff\" rowspan=\"{$cRow["CNT"]}\">　".$cRow["SCORE"]."</td>";
                    }else{
                        $row["ACTION"] = "";
                        $row["SCORE"] = "";
                    }
                    
                    $arg["data3"][] = $row;
                    
                    $cnt++;
                }
            }
            
            //特別活動の記録
            $query = knjo153Query::getSpeAct($model->schregno, $opt[$model->field["GRADE"]]);
            $result = $db->query($query);
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $arg["SPECIAL"] = 1;
                if($row["OTHER_ACTION"] != ""){
                    $row["NAIYO"] = $row["OTHER_ACTION"];
                }else{
                    $row["NAIYO"] = $row["CODENAME"];
                }
                
                $arg["data4"][] = $row;
            }
            
            //行動の記録
            $query = knjo153Query::getBehavior($model->schregno, $opt[$model->field["GRADE"]]);
            $result = $db->query($query);
            if($dataSyubetu != "03"){
                while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                    $arg["BEHAV1"] = 1;
                    if($row["OTHER_ITEM"] != ""){
                        $row["NAIYO"] = $row["OTHER_ITEM"];
                    }else{
                        $row["NAIYO"] = $row["CODENAME"];
                    }
                    $row["SCORE"] = $row["SCORE_CODE"];
                    
                    $arg["data5"][] = $row;
                }
            }else{
                while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                    $arg["BEHAV2"] = 1;
                    $arg["data5"][] = $row;
                }
            }
            
            //各教科_特別活動_自立活動の記録
            if($dataSyubetu == "03"){
                $query = knjo153Query::getAll($model->schregno, $opt[$model->field["GRADE"]]);
                $result = $db->query($query);
                while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                    $arg["ALL"] = 1;
                    
                    if($row["OTHER_SUBJECT"] != ""){
                        $row["KYOUKA"] = $row["OTHER_SUBJECT"];
                    }else{
                        $row["KYOUKA"] = $row["CODENAME"];
                    }
                    
                    $arg["data6"][] = $row;
                }
            }

            //参考様式コード取得//akimoto:取得場所はもっと上にするかも。
            $query = knjo153Query::getStyleVersion($model->schregno, $opt[$model->field["GRADE"]]);
            $styleVer = $db->getOne($query);

            //特別な教科_道徳
            if($styleVer != "F01"){
                $query = knjo153Query::getMoral($model->schregno, $opt[$model->field["GRADE"]]);
                $result = $db->query($query);
                while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                    $arg["MORAL"] = 1;
                    $arg["data9"] = $row;
                }
            }
        }else{
            if($model->schregno != ""){
                $query = knjo153Query::getSendFrom($model->schregno);
                $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                
                if($row["INFO_FLG"] != "2"){
                    //教育委員会
                    if($row["GNAME"] != ""){
                        $row["NAME"] = $row["NNAME"]."(".$row["GNAME"].")";
                    }else if($row["NNAME"] != ""){
                        $row["NAME"] = $row["NNAME"];
                    }
                    
                    if($row["GADDR"] != ""){
                        $row["ADDR"] = $row["NADDR"]."(".$row["GADDR"].")";
                    }else if($row["NADDR"] != ""){
                        $row["ADDR"] = $row["NADDR"];
                    }
                    
                    if($row["GKATAGAKI"] != ""){
                        $row["KATAGAKI"] = $row["NKATAGAKI"]."(".$row["GKATAGAKI"].")";
                    }else if($row["NKATAGAKI"] != ""){
                        $row["KATAGAKI"] = $row["NKATAGAKI"];
                    }
                    if($row["ZIPCD"] != ""){
                        $row["ZIPCD"] = "〒 ".$row["ZIPCD"];
                    }
                    $arg["data7"] = $row;
                }else{
                    //学校
                    if($row["GNAME"] != ""){
                        $row["NAME"] = $row["NNAME"]."(".$row["GNAME"].")";
                    }else if($row["NNAME"] != ""){
                        $row["NAME"] = $row["NNAME"];
                    }
                    
                    if($row["GADDR"] != ""){
                        $row["ADDR"] = $row["NADDR"]."(".$row["GADDR"].")";
                    }else if($row["NADDR"] != ""){
                        $row["ADDR"] = $row["NADDR"];
                    }
                    
                    if($row["GKATAGAKI"] != ""){
                        $row["KATAGAKI"] = $row["NKATAGAKI"]."(".$row["GKATAGAKI"].")";
                    }else if($row["NKATAGAKI"] != ""){
                        $row["KATAGAKI"] = $row["NKATAGAKI"];
                    }
                    if($row["BUN_GNAME"] != ""){
                        $row["BUN_NAME"] = $row["BUN_NNAME"]."(".$row["BUN_GNAME"].")";
                    }else if($row["BUN_NNAME"] != ""){
                        $row["BUN_NAME"] = $row["BUN_NNAME"];
                    }
                    if($row["ZIPCD"] != ""){
                        $row["ZIPCD"] = "〒 ".$row["ZIPCD"];
                    }
                    
                    if($row["BUN_GADDR"] != ""){
                        $row["BUN_ADDR"] = $row["BUN_NADDR"]."(".$row["BUN_GADDR"].")";
                    }else if($row["BUN_NKATAGAKI"] != ""){
                        $row["BUN_ADDR"] = $row["BUN_NADDR"];
                    }
                    
                    if($row["BUN_GKATAGAKI"] != ""){
                        $row["BUN_KATAGAKI"] = $row["BUN_NKATAGAKI"]."(".$row["BUN_GKATAGAKI"].")";
                    }else if($row["BUN_NKATAGAKI"] != ""){
                        $row["BUN_KATAGAKI"] = $row["BUN_NKATAGAKI"];
                    }
                    if($row["BUN_ZIPCD"] != ""){
                        $row["BUN_ZIPCD"] = "〒 ".$row["BUN_ZIPCD"];
                    }
                    $arg["data8"] = $row;
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
        View::toHTML($model, "knjo153Form1.html", $arg);
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
