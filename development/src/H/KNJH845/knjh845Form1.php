<?php
class knjh845Form1
{
    function main(&$model) {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knjh845index.php", "", "edit");
        //DB接続
        $db = Query::dbCheckOut();
        
        //年度と学期
        if (!isset($model->exp_year)) $model->exp_year = CTRL_YEAR ."-" .CTRL_SEMESTER;

        //年組番氏名
        if($model->knjid != ""){
            $query = knjh845Query::getName($model->knjid, $model->year);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            
            $arg["GRADE"] = $row["GRADE"]."-".$row["HR_CLASS"]."-".$row["ATTENDNO"];
            $arg["NAME"] = $row["NAME"];

            $semester = $row["SEMESTER"];
        }
        
        
        //表示内容
        //データの区分
        $extra = " onchange=\"btn_submit('change');\"";
        $query = knjh845Query::getSite();
        $sresult = $db->query($query);
        $opt = array();
        while($row = $sresult->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt[] = array("label"  => $row["NAME1"],
                           "value"  => $row["NAME2"]);
        }
        $arg["data"]["SITE"] = knjCreateCombo($objForm, "SITE", $model->field["SITE"], $opt, $extra, "1");

        //教科
        $extra = " onchange=\"btn_submit('change');\"";
        $query = knjh845Query::getKyouka($model->year, $model->knjid, $model->field["SITE"]);
        $kresult = $db->query($query);
        $opt = array();
        //「すべて」はなし　エラーチェック有
        $opt[0] = array("label" => "",
                        "value" => "");
        while($row = $kresult->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt[] = array("label"  => $row["LABEL"],
                           "value"  => $row["VALUE"]);
        }
        $arg["data"]["KYOKA"] = knjCreateCombo($objForm, "KYOKA", $model->field["KYOKA"], $opt, $extra, "1");
        
        //科目
        $extra = " onchange=\"btn_submit('change');\"";
        $opt2 = array();
        $opt2[0] = array("label" => "",
                         "value" => "");
        if($model->field["KYOKA"] != ""){
            //Queryで中身取得
            $query = knjh845Query::getKamoku($model->year, $model->field["SITE"], $model->field["KYOKA"]);
            $Result = $db->query($query);
            while($row = $Result->fetchRow(DB_FETCHMODE_ASSOC)){
                $opt2[] = array("label"  => $row["LABEL"],
                                "value"  => $row["VALUE"]);
            }
        }
        $arg["data"]["KAMOKU"] = knjCreateCombo($objForm, "KAMOKU", $model->field["KAMOKU"], $opt2, $extra, "1");

        //表示対象(初回受験か最新の受験回数か)
        $extra = "";
        $opt6 = array(1, 2);
        $extra = array("id=\"TESTCNT1\"", "id=\"TESTCNT2\"");
        $label = array("TESTCNT1" => "初回受験", "TESTCNT2" => "最新受験");
        $radioArray = knjCreateRadio($objForm, "TESTCNT", $model->field["TESTCNT"], $extra, $opt6, count($opt6));
        $arg["data"]["TEST_CNT"] = "";
        $sp = "";
        foreach($radioArray as $key => $val){
            $arg["data"]["TEST_CNT"] .= $sp.$val."<LABEL for=\"".$key."\">".$label[$key]."</LABEL>";
            $sp = "　";
        }
                
        
                
        //区分
        $extra = " onchange=\"checkCng();\"";
        //$change[0] = "正答率";
        $change[1] = "大分野別";
        $change[2] = "小分野別";
        
        $opt4 = array();
        foreach($change as $key => $val){
            $opt4[] = array("label"  => $val,
                            "value"  => $key);
        }
        //$model->field["KUBUN"] = 1;     ///////////////////////////
        $arg["data"]["KUBUN"] = knjCreateCombo($objForm, "KUBUN", $model->field["KUBUN"], $opt4, $extra, "1");
        
        //受験日時
        $extra = "";
        $arg["data"]["F_DATE"] = View::popUpCalendar2($objForm, "F_DATE", str_replace("-", "/", $model->field["F_DATE"]), "", "", "");
        $arg["data"]["T_DATE"] = View::popUpCalendar2($objForm, "T_DATE", str_replace("-", "/", $model->field["T_DATE"]), "", "", "");
        
        if($model->mode != 0){
            //件数チェック
            //件数0ならmode=0にする
            $query = knjh845Query::getIndData($model->year, $model->knjid, $model->field);
            $eCnt = $db->getOne($query);
            if($eCnt < 1){
                $model->mode = 0;
                $model->setMessage("対象のデータがありません。");
            }
        }
        
        if($model->knjid != ""){
            //前年度と来年度のデータがあるか確認して移動するボタンを作る。
            $arg["NOW_YEAR"] = $model->year."年度".$semester."学期";
            $backyear = $model->year - 1;
            $nextyear = $model->year + 1;
            $query = knjh845Query::getBackNextData($model->knjid, $backyear);
            $backCnt = $db->getOne($query);
            if($backCnt > 0){
                $extra = " onclick=\"yearChange('{$backyear}');\"";
                $arg["BACK_YEAR"] = createBtn($objForm, "btn_back", "前年度", $extra);
            }
            $query = knjh845Query::getBackNextData($model->knjid, $nextyear);
            $nextCnt = $db->getOne($query);
            if($nextCnt > 0){
                $extra = " onclick=\"yearChange('{$nextyear}');\"";
                $arg["NEXT_YEAR"] = createBtn($objForm, "btn_next", "次年度", $extra);
            }
        }
        
        if($model->mode != 0){
            $arg["HYOUZI"] = "1";
        
            //取込日
            $query = knjh845Query::getImportDate();
            $import = $db->getOne($query);
            $importDate = explode("-", $import);
            $arg["IMPORTDATE"] = "取込日：".$importDate[0]."年".$importDate[1]."月".$importDate[2]."日";
            
            //変換用配列    共通で使いたい
            $partNo = array("POINT", "SCHREG_SCORE", "GRADE_MAX", "AVG", "GRADE_AVG");
            $partChange = array("POINT"          => "配点",
                                "SCHREG_SCORE"   => "個人得点",
                                "GRADE_MAX"      => "学年最高点",
                                "AVG"            => "クラス平均点",
                                "GRADE_AVG"      => "学年平均点");
            
            
            //right_header部分　全部共通////////////////////////////////////////////////////////////
            $arg["right_header"] = "";
            //個人データ取得
            $query = knjh845Query::getTestScregData($model->year, $model->knjid, $model->field);
            $scResult = $db->query($query);
            $dataArray = array();   //実施回をキーにしてデータを保持
            $takeArray = array();   //実施回のみの配列
            $nameArray = array();   //実施回のみの配列
            $sql = "";  //学年平均・学年最高点取得用where条件
            $cnm = "";
            $takeCnt = 0;
            $allPoint = 0;  //左上の得点率計算のためにすべてのテストの配点を合計する
            while($scRow = $scResult->fetchRow(DB_FETCHMODE_ASSOC)){
                $dataArray[$scRow["TEACHING_CD"]] = $scRow;
                $takeArray[$takeCnt] = $scRow["TEACHING_CD"];
                $nameArray[$takeCnt] = $scRow["TEACHING_NAME"];
                
                $allPoint = $allPoint + $scRow["POINT"];
                
                $sql .= $cnm."'".$scRow["TEACHING_CD"]."'";
                $cnm = ",";
                $takeCnt++;
            }
            //実施回と配点クラス平均・クラス最高点取得
            $query = knjh845Query::getTestClass($model->year, $semester, $model->knjid, $model->field, $sql);
            $cResult = $db->query($query);
            while($cRow = $cResult->fetchRow(DB_FETCHMODE_ASSOC)){
                $dataArray[$cRow["TEACHING_CD"]] += $cRow;
            }
            //学年平均・学年最高点取得
            $query = knjh845Query::getTestGrade($model->year, $semester, $model->knjid, $model->field, $sql);
            $gResult = $db->query($query);
            while($gRow = $gResult->fetchRow(DB_FETCHMODE_ASSOC)){
                $dataArray[$gRow["TEACHING_CD"]] += $gRow;
            }
            
            //得点率
            $rhperArray = array();
            foreach($takeArray as $testid){
                foreach($partNo as $val){
                    if($val != "POINT"){
                        $percent = $dataArray[$testid][$val] / $dataArray[$testid]["POINT"] * 100;
                        $percent = round($percent, 1);
                        $rhperArray[$testid][$val] = "(".$percent."%)";
                    }else{
                        $rhperArray[$testid][$val] = "";
                    }
                }
            }

            for($i=0;$i<$takeCnt;$i++){
                if(mb_strlen($nameArray[$i]) > 22){
                    $arg["right_header"] .= "<th class=\"no_search right_td\" onmouseover=\"tooltip.Schedule( this, event );\" tooltip=\"{$nameArray[$i]}\">".mb_substr($nameArray[$i], 0, 23)."…</th>";
                }else{
                    $arg["right_header"] .= "<th class=\"no_search right_td\">".$nameArray[$i]."</th>";
                }
            }
            
            foreach($partNo as $partKey => $partVal){
                $arg["right_header"] .= "</tr><tr>";
                
                foreach($takeArray as $val){
                    if($partKey == 0){
                        $class = " field_color";
                    }else if($partKey == 1 || $partKey == 2){
                        $class = " grade";
                    }else{
                        $class = "";
                    }
                    
                    if(preg_match("/AVG/", $partVal)){
                        $dataArray[$val][$partVal] = sprintf("%0.2f", $dataArray[$val][$partVal]);
                    }
                    
                    if($dataArray[$val][$partVal] != ""){
                        $arg["right_header"] .= "<td class=\"right_td".$class."\">".$dataArray[$val][$partVal].$rhperArray[$val][$partVal]."</td>";
                    }else{
                        $arg["right_header"] .= "<td class=\"right_td".$class."\">-</td>";
                    }
                }
            }
            
            
            //#holizonの代わりに直接値決めて入れる
            $width = 180*$takeCnt;
            $arg["width"] = "style=\"width:".$width."px;\"";
            /*right_header部分　終わり*************************************************************/

            //header_x部分
            //テスト関係なく人毎に計算してデータ取得
            //クラス平均とクラス最高点
            $query = knjh845Query::getAllClass($model->year, $semester, $model->knjid, $model->field, $sql);
            $aRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $allArray = array();
            $allArray = $aRow;
            
            //学年平均と学年最高点
            $query = knjh845Query::getAllGrade($model->year, $semester, $model->knjid, $model->field, $sql);
            $agRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $allArray += $agRow;
            
            //個人得点
            $query = knjh845Query::getAllSchreg($model->year, $model->knjid, $model->field, $sql);
            $asRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $allArray += $asRow;
            
            //得点率計算
            $allArray["POINT"] = $allPoint;     //右上で計算してきたやつ
            $hxperArray = array();
            foreach($partNo as $val){
                if($val != "POINT"){
                    $percent = $allArray[$val] / $allArray["POINT"] * 100;
                    $percent = round($percent, 1);
                    $hxperArray[$val] = "(".$percent."%)";
                }else{
                    $hxperArray[$val] = "";
                }
            }

            $arg["header_x"] = "<th class=\"no_search title\">実施回</th><th class=\"no_search sum normal\">計</th>";
            foreach($partNo as $partKey => $partVal){
                if($partKey == 0){
                    $class = " field_color";
                }else if($partKey == 1 || $partKey == 2){
                    $class = " grade";
                }else{
                    $class = "";
                }
                
                if(preg_match("/AVG/", $partVal)){
                    $allArray[$partVal] = sprintf("%0.2f", $allArray[$partVal]);
                }else{
                    $allArray[$partVal] = floor($allArray[$partVal]);
                }
                
                $arg["header_x"] .= "</tr><tr>";
                $arg["header_x"] .= "<th class=\"no_search title\">".$partChange[$partVal]."</th>";
                $arg["header_x"] .= "<td class=\"sum normal".$class."\">".$allArray[$partVal].$hxperArray[$partVal]."</td>";
            }
            
            
            //left_body部分　大分野・小分野時はデータのみ取得
            if($model->field["KUBUN"] == "1"){
                //大分野ごとの名称と全体のテストの配点、クラス平均、クラス最高点を取得
                $query = knjh845Query::getPartClass($model->year, $semester, $model->knjid, $model->field, $sql);
                $pcResult = $db->query($query);
                $partArray = array();   //名称保持
                $partNoArray = array();
                $partCnt = 0;
                while($pcRow = $pcResult->fetchRow(DB_FETCHMODE_ASSOC)){
                    $partArray[$pcRow["FIELDNO"]] = $pcRow;
                    if($pcRow["FIELDNO"] == "0000"){
                        $partArray[$pcRow["FIELDNO"]]["THIRD_NAME"] = "分野未設定";
                    }
                    $partNoArray[$partCnt] = $pcRow["FIELDNO"];
                    $partCnt++;
                }
                
                //大分野ごとに全体のテストの学年平均・最高点を取得
                $query = knjh845Query::getPartGrade($model->year, $semester, $model->knjid, $model->field, $sql);
                $pgResult = $db->query($query);
                while($pgRow = $pgResult->fetchRow(DB_FETCHMODE_ASSOC)){
                    $partArray[$pgRow["FIELDNO"]] += $pgRow;
                }
                
                //大分野ごとの全体の個人得点取得
                $query = knjh845Query::getPartSchreg($model->year, $model->knjid, $model->field, $sql);
                $psResult = $db->query($query);
                while($psRow = $psResult->fetchRow(DB_FETCHMODE_ASSOC)){
                    $partArray[$psRow["FIELDNO"]] += $psRow;
                }
            }else{
                //大分野名称と大分野に付く小分野の個数を取得
                $query = knjh845Query::getPartCnt($model->year, $semester, $model->knjid, $model->field, $sql);
                $pResult = $db->query($query);
                $pthArray = array();
                $partNoArray = array();
                $partCnt = 0;
                
                while($pRow = $pResult->fetchRow(DB_FETCHMODE_ASSOC)){
                    $pthArray[$pRow["PART_FIELDNO"]] = $pRow;
                    if($pRow["PART_FIELDNO"] == "0000"){
                        $pthArray[$pRow["PART_FIELDNO"]]["THIRD_NAME"] = "分野未設定";
                    }
                    
                    $partNoArray[$partCnt] = $pRow["PART_FIELDNO"];
                    $partCnt++;
                }
                //小分野の名称とFIELDNOを取得
                $query = knjh845Query::getQuestNo($model->year, $semester, $model->knjid, $model->field, $sql);
                $qResult = $db->query($query);
                $qthArray = array();
                $questNoArray = array();
                $questCnt = 0;
                $bfrPart = "";
                
                while($qRow = $qResult->fetchRow(DB_FETCHMODE_ASSOC)){
                    if($bfrPart != $qRow["PART_FIELDNO"]){
                        $questCnt = 0;
                    }
                    $qthArray[$qRow["PART_FIELDNO"]][$qRow["QUESTION_FIELDNO"]] = $qRow;
                    if($qRow["PART_FIELDNO"] == "0000"){
                        $qthArray[$qRow["PART_FIELDNO"]][$qRow["QUESTION_FIELDNO"]]["FOURTH_NAME"] = "分野未設定";
                    }
                    
                    $questNoArray[$qRow["PART_FIELDNO"]][$questCnt] = $qRow["QUESTION_FIELDNO"];
                    $questCnt++;
                    
                    $bfrPart = $qRow["PART_FIELDNO"];
                }
                
                //小分野ごとのクラス平均・クラス最高点を取得
                $query = knjh845Query::getQuestClass($model->year, $semester, $model->knjid, $model->field, $sql);
                $qcResult = $db->query($query);
                
                while($qcRow = $qcResult->fetchRow(DB_FETCHMODE_ASSOC)){
                    $qthArray[$qcRow["PART_FIELDNO"]][$qcRow["QUESTION_FIELDNO"]] += $qcRow;
                }
                
                //小分野ごとの学年平均・学年最高点を取得
                $query = knjh845Query::getQuestGrade($model->year, $semester, $model->knjid, $model->field, $sql);
                $qgResult = $db->query($query);
                while($qgRow = $qgResult->fetchRow(DB_FETCHMODE_ASSOC)){
                    $qthArray[$qgRow["PART_FIELDNO"]][$qgRow["QUESTION_FIELDNO"]] += $qgRow;
                }
                
                //小分野ごとの個人得点を取得
                $query = knjh845Query::getQuestSchreg($model->year, $model->knjid, $model->field, $sql);
                $qsResult = $db->query($query);
                while($qsRow = $qsResult->fetchRow(DB_FETCHMODE_ASSOC)){
                    $qthArray[$qsRow["PART_FIELDNO"]][$qsRow["QUESTION_FIELDNO"]] += $qsRow;
                }
                
            }
            
            //right_body部分　ここから////////////////////////////////////////////////////////////
            $arg["right_body"] = "";
            if($model->field["KUBUN"] == "1"){
                //大分野ごと、実施回数ごとにクラス平均とクラス最高点取得
                $query = knjh845Query::getPartClassPoint($model->year, $semester, $model->knjid, $model->field, $sql);
                $pcResult = $db->query($query);
                $ppArray = array();
                
                while($pcRow = $pcResult->fetchRow(DB_FETCHMODE_ASSOC)){
                    $ppArray[$pcRow["FIELDNO"]][$pcRow["TEACHING_CD"]] = $pcRow;
                }
                //大分野ごと、実施回数ごとに学年平均と学年最高点取得
                $query = knjh845Query::getPartGradePoint($model->year, $semester, $model->knjid, $model->field, $sql);
                $pgResult = $db->query($query);
                while($pgRow = $pgResult->fetchRow(DB_FETCHMODE_ASSOC)){
                    $ppArray[$pgRow["FIELDNO"]][$pgRow["TEACHING_CD"]] += $pgRow;
                }
                //大分野ごと、TEST_IDごとに個人成績取得
                $query = knjh845Query::getPartSchregPoint($model->year, $model->knjid, $model->field, $sql);
                $psResult = $db->query($query);
                while($psRow = $psResult->fetchRow(DB_FETCHMODE_ASSOC)){
                    $ppArray[$psRow["FIELDNO"]][$psRow["TEACHING_CD"]] += $psRow;
                }
                
                
                //左下用に大分野ごとの配点の合計点を計算したい
                $partAllPoint = array();
                foreach($partNoArray as $val){
                    foreach($takeArray as $test){
                        if($partAllPoint[$val] == ""){
                            $partAllPoint[$val] = 0;
                        }
                        $partAllPoint[$val] = $partAllPoint[$val] + $ppArray[$val][$test]["POINT"];
                    }
                }

                //得点率計算
                $ppPerArray = array();
                foreach($partNoArray as $val){
                    foreach($takeArray as $test){
                        foreach($partNo as $part){
                            if($part != "POINT"){
                                if($ppArray[$val][$test]["POINT"] != ""){
                                    $percent = $ppArray[$val][$test][$part] / $ppArray[$val][$test]["POINT"] * 100;
                                    $percent = round($percent, 1);
                                    $ppPerArray[$val][$test][$part] = "(".$percent."%)";
                                }else{
                                    $ppPerArray[$val][$test][$part] = "";
                                }
                            }else{
                                $ppPerArray[$val][$test][$part] = "";
                            }
                        }
                    }
                }
                
                $arg["right_body"] = "";
                foreach($partNoArray as $val){  //分野ごとにまわす
                    foreach($partNo as $partKey => $partVal){        //配点とかをまわす
                        if($arg["right_body"] != ""){
                            $arg["right_body"] .= "</tr><tr>";
                            $border = 1;
                        }else{
                            $border = 0;
                        }
                        foreach($takeArray as $take){   //実施回数まわす
                            if($partKey == 0){
                                if($border == 0){
                                    $class = " field_color";
                                }else{
                                    $class = " field_color field_border";
                                }
                            }else if($partKey < 3){
                                $class = " grade";
                            }else{
                                $class = "";
                            }
                            if($ppArray[$val][$take][$partVal] != ""){
                                if(preg_match("/AVG/", $partVal)){
                                    $ppArray[$val][$take][$partVal] = sprintf("%0.2f", $ppArray[$val][$take][$partVal]);
                                }else{
                                    $ppArray[$val][$take][$partVal] = floor($ppArray[$val][$take][$partVal]);
                                }
                                $arg["right_body"] .= "<td class=\"right_td".$class."\">".$ppArray[$val][$take][$partVal].$ppPerArray[$val][$take][$partVal]."</td>";
                            }else{
                                $arg["right_body"] .= "<td class=\"right_td".$class."\">-</td>";
                            }
                        }
                    }
                }
                
            }else{
                //小分野ごと、実施回数ごとにクラス平均・クラス最高点取得
                $query = knjh845Query::getQuestClassPoint($model->year, $semester, $model->knjid, $model->field, $sql);
                $qcResult = $db->query($query);
                $qpArray = array();
                
                while($qcRow = $qcResult->fetchRow(DB_FETCHMODE_ASSOC)){
                    $qpArray[$qcRow["PARTCD"]][$qcRow["QUESTCD"]][$qcRow["TEACHING_CD"]] = $qcRow;
                }
                //小分野ごと、実施回数ごとに学年平均・学年最高点取得
                $query = knjh845Query::getQuestGradePoint($model->year, $semester, $model->knjid, $model->field, $sql);
                $qgResult = $db->query($query);
                while($qgRow = $qgResult->fetchRow(DB_FETCHMODE_ASSOC)){
                    $qpArray[$qgRow["PARTCD"]][$qgRow["QUESTCD"]][$qgRow["TEACHING_CD"]] += $qgRow;
                }
                //小分野ごと、TEST_IDごとの個人得点取得
                $query = knjh845Query::getQuestSchregPoint($model->year, $model->knjid, $model->field, $sql);
                $qsResult = $db->query($query);
                while($qsRow = $qsResult->fetchRow(DB_FETCHMODE_ASSOC)){
                    $qpArray[$qsRow["PART_FIELDNO"]][$qsRow["QUESTION_FIELDNO"]][$qsRow["TEACHING_CD"]] += $qsRow;
                }
                
                //左下計算用配点合計
                $questAllPoint = array();
                foreach($partNoArray as $partNoVal){
                    foreach($questNoArray[$partNoVal] as $questNo){
                        foreach($takeArray as $test){
                            if($questAllPoint[$partNoVal][$questNo] == ""){
                                $questAllPoint[$partNoVal][$questNo] = 0;
                            }
                            $questAllPoint[$partNoVal][$questNo] = $questAllPoint[$partNoVal][$questNo] + $qpArray[$partNoVal][$questNo][$test]["POINT"];
                        }
                    }
                }
                
                //右下用得点率計算
                $qpPerArray = array();
                foreach($partNoArray as $partNoVal){
                    foreach($questNoArray[$partNoVal] as $questNo){
                        foreach($takeArray as $test){
                            foreach($partNo as $part){
                                if($part != "POINT"){
                                    if($qpArray[$partNoVal][$questNo][$test]["POINT"] != ""){
                                        $percent = $qpArray[$partNoVal][$questNo][$test][$part] / $qpArray[$partNoVal][$questNo][$test]["POINT"] * 100;
                                        $percent = round($percent, 1);
                                        $qpPerArray[$partNoVal][$questNo][$test][$part] = "(".$percent."%)";
                                    }else{
                                        $qpPerArray[$partNoVal][$questNo][$test][$part] = "";
                                    }
                                }else{
                                    $qpPerArray[$partNoVal][$questNo][$test][$part] = "";
                                }
                            }
                        }
                    }
                }

                $arg["right_body"] = "";
                foreach($partNoArray as $pKey => $pVal){
                    
                    foreach($questNoArray[$pVal] as $qVal){
                        if($arg["right_body"] != ""){
                            $border = 1;
                        }else{
                            $border = 0;
                        }
                        foreach($partNo as $partKey => $partVal){
                            if($arg["right_body"] != ""){
                                $arg["right_body"] .= "</tr><tr>";
                            }
                            foreach($takeArray as $tVal){
                                if($partKey == 0){
                                    if($border == 0){
                                        $class = " field_color";
                                    }else{
                                        $class = " field_color field_border";
                                    }
                                }else if($partKey < 3){
                                    $class = " grade";
                                }else{
                                    $class = "";
                                }
                                
                                if($qpArray[$pVal][$qVal][$tVal][$partVal] != ""){
                                    if(preg_match("/AVG/", $partVal)){
                                        $qpArray[$pVal][$qVal][$tVal][$partVal] = sprintf("%0.2f", $qpArray[$pVal][$qVal][$tVal][$partVal]);
                                    }else{
                                        $qpArray[$pVal][$qVal][$tVal][$partVal] = floor($qpArray[$pVal][$qVal][$tVal][$partVal]);
                                    }
                                    
                                    $arg["right_body"] .= "<td class=\"right_td".$class."\">".$qpArray[$pVal][$qVal][$tVal][$partVal].$qpPerArray[$pVal][$qVal][$tVal][$partVal]."</td>";
                                    
                                }else{
                                    $arg["right_body"] .= "<td class=\"right_td".$class."\">-</td>";
                                }
                            }
                        }
                    }
                }
                
            }
            
            //left_bodyちゃんと作成
            if($model->field["KUBUN"] == "1"){
                //得点率計算
                $lbperArray = array();
                foreach($partNoArray as $val){
                    foreach($partNo as $part){
                        if($part != "POINT"){
                            $percent = $partArray[$val][$part] / $partAllPoint[$val] * 100;
                            $percent = round($percent, 1);
                            $lbperArray[$val][$part] = "(".$percent."%)";
                        }else{
                            $partArray[$val]["POINT"] = $partAllPoint[$val];    //配点合計を入れる
                            $lbperArray[$val][$part] = "";
                        }
                    }
                }
                
                $arg["left_body"] = "";
                foreach($partArray as $key => $val){
                    //大分野名称
                    if($val["THIRD_NAME"] == ""){
                        $val["THIRD_NAME"] = "分野なし";
                    }
                    if($arg["left_body"] == ""){
                        $arg["left_body"] .= "<th class=\"changecolor large_field\" rowspan=\"5\">".$val["THIRD_NAME"]."</th>";
                        $border = 0;
                    }else{
                        $arg["left_body"] .= "<th class=\"changecolor large_field field_border\" rowspan=\"5\">".$val["THIRD_NAME"]."</th>";
                        $border = 1;
                    }
                    //右側
                    foreach($partNo as $partKey => $partVal){
                        if(preg_match("/AVG/", $partVal)){
                            $val[$partVal] = sprintf("%0.2f", $val[$partVal]);
                        }else{
                            $val[$partVal] = floor($val[$partVal]);
                        }
                        if($partKey == 0){
                            if($border == 0){
                                $class = " field_color";
                                $class2 = "";
                            }else{
                                $class = " field_color field_border";
                                $class2 = " field_border";
                            }
                        }else if($partKey < 3){
                            $class = " grade";
                            $class2 = "";
                        }else{
                            $class = "";
                            $class2 = "";
                        }
                        if($partKey != 0){
                            $arg["left_body"] .= "<tr>";
                        }
                        $arg["left_body"] .= "<th class=\"changecolor title".$class2."\">".$partChange[$partVal]."</th><td class=\"sum normal".$class."\">".$val[$partVal].$lbperArray[$key][$partVal]."</td></tr>";
                    }
                }
                
            }else if($model->field["KUBUN"] == "2"){
                //得点率計算
                $lbPerArray = array();
                foreach($partNoArray as $partNoVal){
                    foreach($questNoArray[$partNoVal] as $questNo){
                        foreach($partNo as $part){
                            if($part != "POINT"){
                                $percent = $qthArray[$partNoVal][$questNo][$part] / $qthArray[$partNoVal][$questNo]["POINT"] * 100;
                                $percent = round($percent, 1);
                                $lbPerArray[$partNoVal][$questNo][$part] = "(".$percent."%)";
                            }else{
                                $qthArray[$partNoVal][$questNo][$part] = $questAllPoint[$partNoVal][$questNo];
                                $lbPerArray[$partNoVal][$questNo][$part] = "";
                            }
                        }
                    }
                }
                
                $arg["left_body"] = "";
                foreach($partNoArray as $val){
                    if($arg["left_body"] != ""){
                        $arg["left_body"] .= "</tr><tr>";
                    }
                    if($pthArray[$val]["THIRD_NAME"] == ""){
                        $pthArray[$val]["THIRD_NAME"] = "分野なし";
                    }
                    
                    $rowspan = 5*$pthArray[$val]["QUESTION_CNT"];
                    if($arg["left_body"] == ""){
                        $arg["left_body"] .= "<th class=\"changecolor large_field\" rowspan=\"".$rowspan."\">".$pthArray[$val]["THIRD_NAME"]."</th>";
                        $border = 0;
                    }else{
                        $arg["left_body"] .= "<th class=\"changecolor large_field field_border\" rowspan=\"".$rowspan."\">".$pthArray[$val]["THIRD_NAME"]."</th>";
                        $border = 1;
                    }
                    foreach($qthArray[$val] as $qKey => $qVal){
                        foreach($partNo as $partKey => $partVal){
                            if($partKey == 0){
                                if($border == 0){
                                    $class = " field_color";
                                    $class2 = "";
                                }else{
                                    $class = " field_color field_border";
                                    $class2 = " field_border";
                                }
                            }else if($partKey < 3){
                                $class = " grade";
                                $class2 = "";
                            }else{
                                $class = "";
                                $class2 = "";
                            }
                            
                            if($partKey == 0){
                                if(mb_strlen($qVal["FOURTH_NAME"]) > 6){
                                    $fourthName = "<th class=\"changecolor small_field".$class2."\" onmouseover=\"tooltip.Schedule( this, event );\" tooltip=\"{$qVal["FOURTH_NAME"]}\">".mb_substr($qVal["FOURTH_NAME"], 0, 5)."…</th>";
                                }else{
                                    if($qVal["FOURTH_NAME"] == ""){
                                        $qVal["FOURTH_NAME"] = "分野なし";
                                    }
                                    $fourthName = "<th class=\"changecolor small_field".$class2."\">".$qVal["FOURTH_NAME"]."</th>";
                                }
                                $arg["left_body"] .= $fourthName."<td class=\"sum normal".$class."\">".$qVal[$partVal]."</td></tr>";
                                $border = 1;
                            }else{
                                if(preg_match("/AVG/", $partVal)){
                                    $qVal[$partVal] = sprintf("%0.2f", $qVal[$partVal]);
                                }else{
                                    $qVal[$partVal] = floor($qVal[$partVal]);
                                }

                                $arg["left_body"] .= "<th class=\"changecolor title\">".$partChange[$partVal]."</th><td class=\"sum normal".$class."\">".$qVal[$partVal].$lbPerArray[$val][$qKey][$partVal]."</td></tr>";
                            }
                        }
                    }
                }
            }
            
            //スクロールのjavascript部分
            if($model->field["KUBUN"] == "0"){
                $jsdata .= "\$E(\"right_body2\").onscroll=scroll;\n";
                $jsfunc .= "    \$E(\"right_header2\").scrollLeft= \$E(\"right_body2\").scrollLeft;\n";    // 左右連動させる
                $jsfunc .= "    \$E(\"left_body2\").scrollTop = \$E(\"right_body2\").scrollTop;\n";    // 上下連動させる*/
                
                $arg["right_h_id"] = "right_header2";
                $arg["right_b_id"] = "right_body2";
                $arg["left_h_id"] = "header_x2";
                $arg["left_b_id"] = "left_body2";
            }else{
                $jsdata .= "\$E(\"right_body\").onscroll=scroll;\n";
                $jsfunc .= "    \$E(\"right_header\").scrollLeft = \$E(\"right_body\").scrollLeft;\n";    // 左右連動させる
                $jsfunc .= "    \$E(\"left_body\").scrollTop = \$E(\"right_body\").scrollTop;\n";    // 上下連動させる*/
                
                $arg["right_h_id"] = "right_header";
                $arg["right_b_id"] = "right_body";
                $arg["left_h_id"] = "header_x";
                $arg["left_b_id"] = "left_body";
            }
            
            //javascript
            $arg["java"] = "<script type=\"text/javascript\">\n";
            $arg["java"] .= "function \$E(id){ return document.getElementById(id); }\n";
            $arg["java"] .= "function scroll(){\n";
            $arg["java"] .= $jsfunc;
            $arg["java"] .= "}\n";
            $arg["java"] .= $jsdata;
            $arg["java"] .= "</script>\n";
        }
        
        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        $arg["IFRAME"] = View::setIframeJs();
        View::toHTML($model, "knjh845Form1.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //表示
    $extra = " onclick=\"btn_submit('hyouzi');\"";
    $arg["button"]["btn_search"] = createBtn($objForm, "btn_search", "表 示", $extra);
    if($model->mode != 0){
        //CSV
        $extra = " onclick=\"btn_submit('csv');\"";
        $arg["button"]["btn_csv"] = createBtn($objForm, "btn_csv", "CSV出力", $extra);
    }
    //終了
    $arg["button"]["btn_end"] = createBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//Hidden作成
function makeHidden(&$objForm, $model)
{
    $objForm->ae(createHiddenAe("cmd"));
    $objForm->ae(createHiddenAe("SCHREGNO", $model->schregno));
    $objForm->ae(createHiddenAe("YEAR"));                           //年度移動ボタン押して年度を変える箱。jsで制御。
    //$objForm->ae(createHiddenAe("BEFORE_YEAR", $model->year));      //年度移動ボタン押したときに表示不備エラーなら年度を戻すためのもの。現在表示している年度が入る。
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
