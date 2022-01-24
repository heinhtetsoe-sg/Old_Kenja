<?php

require_once('for_php7.php');

class knjh410_medicalForm1
{
    function main(&$model)
    {

        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjh410_medicalindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();
        
        //選択した生徒情報
        $arg["YEAR"] = CTRL_YEAR;
        $arg["SCHREGNO"] = $model->schregno;
        $query = knjh410_medicalQuery::getSchregData($model->schregno);
        $name = $db->getOne($query);
        $arg["NAME"] = $name;
        
        
        //ラジオボタン
        if($model->btnRadio != ""){
            $arg["btn"] = 1;
        }else{
            $arg["btn"] = "";
        }
        $opt = array(1, 2);
        $extra = array("id=\"btnRadio1\" onclick=\"btn_submit('general');\"", "id=\"btnRadio2\" onclick=\"btn_submit('dental');\"");
        $label = array("btnRadio1" => "一般", "btnRadio2" => "歯・口腔");
        $radioArray = knjCreateRadio($objForm, "btnRadio", $model->btnRadio, $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg[$key] = $val."<LABEL for=\"".$key."\">".$label[$key]."</LABEL>";
        
        if($model->btnRadio != "2"){
            //一般情報表示
            $thArray = array("1" => array("健康診断実施日","身長(cm)","体重(kg)","座高(cm)","視力","聴力"),
                             "2" => array("栄養状態","脊柱･胸郭","眼の疾病及び異常","耳鼻咽頭疾患","皮膚疾患","結核" => array("間接撮影","所見","その他検査"),"心臓" => array("臨床医学的検査<BR>(心電図)","疾病及び異常","管理区分")),
                             "3" => array("尿" => array("1次","2次"),"寄生虫卵","その他の疾病及び異常","内科検診","学校医" => array("日付"),"事後措置","備考"),
                             "4" => array("既往症","診断名","運動" => array("指導区分","部活動")));
            
            //そのまま値として入れられるもの
            $tdArray = array("健康診断実施日" => "DATE","身長(cm)" => "HEIGHT","体重(kg)" => "WEIGHT","座高(cm)" => "SITHEIGHT",
                             "日付" => "DOC_DATE","備考" => "DOC_REMARK","診断名" => "DIAGNOSIS_NAME");
            
            //別にするもの
            $anotherArray = array("視力","聴力","結核");
            
            //結核
            $tbArray = array("間接撮影" => array("TB_FILMDATE", "TB_FILMNO"), "所見" => array("TB_REMARKCD","TB_X_RAY"),"その他検査" => array("TB_OTHERTESTCD","TB_NAMECD","TB_ADVISECD"));
            $tbCode = array("TB_REMARKCD" => "F100", "TB_OTHERTESTCD" => "F110", "TB_NAMECD" => "F120", "TB_ADVISECD" => "F130");
            $tbCodeNamecd = "F100','F110','F120','F130";
            
            //コード変換のみ
            $codeArray = array("栄養状態" => "NUTRITIONCD","皮膚疾患" => "SKINDISEASECD",
                               "尿" => array("1次" => array("ALBUMINURIA1CD","URICSUGAR1CD","URICBLEED1CD"),"2次" => array("ALBUMINURIA2CD","URICSUGAR2CD","URICBLEED2CD")),
                               "寄生虫卵" => "PARASITE","その他の疾病及び異常" => "OTHERDISEASECD","事後措置" => "TREATCD",
                               "既往症"=> array("MEDICAL_HISTORY1","MEDICAL_HISTORY2","MEDICAL_HISTORY3"),"運動" => array("指導区分" => "GUIDE_DIV","部活動" => "JOINING_SPORTS_CLUB"));
            
            //コード<BR>備考で入れるもの
            $brArray = array("脊柱･胸郭" => array("SPINERIBCD","SPINERIBCD_REMARK"), "眼の疾病及び異常" => array("EYEDISEASECD","EYE_TEST_RESULT"),"耳鼻咽頭疾患" => array("NOSEDISEASECD","NOSEDISEASECD_REMARK"),
                             "心臓" => array("臨床医学的検査<BR>(心電図)" => array("HEART_MEDEXAM","HEART_MEDEXAM_REMARK"),"疾病及び異常" => array("HEARTDISEASECD","HEARTDISEASECD_REMARK"),"管理区分" => array("MANAGEMENT_DIV","MANAGEMENT_REMARK")),
                             "指導区分" => array("OTHER_ADVISECD","OTHER_REMARK"),"内科検診" => array("DOC_CD","DOC_REMARK"));
            
            //コード取得するやつ
            $getCode = array("栄養状態" => "F030","脊柱･胸郭" => "F040", "眼の疾病及び異常" => "F050", "耳鼻咽頭疾患" => "F060", "皮膚疾患" => "F070",
                             "臨床医学的検査<BR>(心電図)" => "F080", "疾病及び異常" => "F090", "尿" => "F020", "その他の疾病及び異常" => "F140", "寄生虫卵" => "F023",
                             "指導区分" => "F145", "内科検診" => "F144", "事後措置" => "F150", "既往症" => "F143", "運動" => "F141','F142");

            //一番上のth部分
            //対象生徒のデータがある年度を取得
            $query = knjh410_medicalQuery::getmedYear($model->schregno, "MEDEXAM_DET_DAT");
            $result = $db->query($query);
            
            //いったんデータのある年度を保持しておく
            $yCnt = 0;
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $keepYear[$row["YEAR"]] = $row;
                $yCnt++;
            }
            
            $nextYear = $model->year + 1;       //次の年
            $beforeYear = $model->year - 3;     //3年前の年
            
            //次の年度データがあるか
            if(!empty($keepYear) && array_key_exists($nextYear, $keepYear)){
                $nextFlg = 1;
            }else{
                $nextFlg = 0;
            }
            //前の年度データがあるか
            if(!empty($keepYear) && array_key_exists($beforeYear, $keepYear)){
                $beforeFlg = 1;
            }else{
                $beforeFlg = 0;
            }

            if($nextFlg != 0){
                //次年度ボタン
                $extra = "onclick=\"btn_submit('next_year');\"";
                $nextButton = knjCreateBtn($objForm, "nextButton", " ＜ ", $extra);
            }else{
                $nextButton = "";
            }
            if($beforeFlg != 0){
                //前年度ボタン
                $extra = "onclick=\"btn_submit('before_year');\"";
                $beforeButton = knjCreateBtn($objForm, "beforeButton", " ＞ ", $extra);
            }else{
                $beforeButton = "";
            }
            
            $yCnt = 0;
            //$med["th"] = "<th width=\"13%\" colspan=\"2\">".$nextButton."　".$beforeButton."</th>";
            $med["th"] = "<th width=\"6.5%\">".$nextButton."</th><th width=\"6.5%\">".$beforeButton."</th>";
            if(!empty($keepYear)){
                foreach($keepYear as $yearKey => $val){
                    if($yearKey < $nextYear && $yearKey > $beforeYear){
                        $med["th"] .= "<th width=\"29%\" height=\"20\">".$yearKey." ( ".$val["AGE"]."歳 )</th>";
                        
                        $year[$yCnt] = $yearKey;
                        $yCnt++;
                    }
                }
            }
            if($yCnt < 2){
                for($i = $yCnt; $i<3; $i++){
                    $med["th"] .= "<th width=\"29%\" height=\"20\"></th>";
                    
                    $year[$i] = " ";
                }
            }
            $arg["th"] = $med["th"];
            
            //データ取得
            $query = knjh410_medicalQuery::getMedexamData($model->schregno);
            $result = $db->query($query);
            
            while($medRow = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                
                $medData[$medRow["YEAR"]] = $medRow;
                
            }
            //htmlの文字列を作成する
            foreach($thArray as $key => $val){
                foreach($val as $secKey => $secVal){
                    //html作成
                    //th部分
                    if(!is_array($secVal)){
                        $html["row"] = "<th width=\"13%\" class=\"no_search\" colspan=\"2\" align=\"right\">".$secVal."</th>";
                        
                        if(array_key_exists($secVal, $tdArray)){
                            //そのまま入れるデータ
                            foreach($year as $yearVal){
                                $html["row"] .= "<td bgcolor=\"#FFFFFF\" width=\"29%\">".$medData[$yearVal][$tdArray[$secVal]]."</td>";
                            }
                            
                            $arg["data".$key][] = $html;
                            $html["row"] = "";
                        }else{
                            if(in_array($secVal, $anotherArray)){
                                //視力か聴力
                                if($secVal == "聴力"){
                                    //コード取得
                                    $query = knjh410_medicalQuery::getCode("F010");
                                    $cResult = $db->query($query);
                                    while($cRow = $cResult->fetchRow(DB_FETCHMODE_ASSOC)){
                                        $ear[$cRow["NAMECD2"]] = $cRow["NAME1"];
                                    }
                                }
                                foreach($year as $yearVal){
                                    if($secVal == "視力"){
                                        $html["row"] .= "<td bgcolor=\"#FFFFFF\" width=\"29%\">右　　".$medData[$yearVal]["R_BAREVISION"].$medData[$yearVal]["R_BAREVISION_MARK"];
                                        $html["row"] .= "　( ".$medData[$yearVal]["R_VISION"].$medData[$yearVal]["R_VISION_MARK"]." )";
                                        $html["row"] .= "　　　左　　".$medData[$yearVal]["L_BAREVISION"].$medData[$yearVal]["L_BAREVISION_MARK"];
                                        $html["row"] .= "　( ".$medData[$yearVal]["L_VISION"].$medData[$yearVal]["L_VISION_MARK"]." )</td>";
                                    }else{
                                        $html["row"] .= "<td bgcolor=\"#FFFFFF\" width=\"29%\">右　　".$medData[$yearVal]["R_EAR_DB"]."db ".$ear[$medData[$yearVal]["R_EAR"]];
                                        $html["row"] .= "　　左　　".$medData[$yearVal]["L_EAR_DB"]."db ".$ear[$medData[$yearVal]["L_EAR"]];
                                    }
                                }
                            }else if(array_key_exists($secVal, $codeArray)){
                                //コード変換のみのもの
                                //コード取得
                                $query = knjh410_medicalQuery::getCode($getCode[$secVal]);
                                $cResult = $db->query($query);
                                while($cRow = $cResult->fetchRow(DB_FETCHMODE_ASSOC)){
                                    $code[$cRow["NAMECD2"]] = $cRow["NAME1"];
                                }
                                
                                foreach($year as $yearVal){
                                    if(!is_array($codeArray[$secVal])){
                                        $html["row"] .= "<td bgcolor=\"#FFFFFF\" width=\"29%\">".$code[$medData[$yearVal][$codeArray[$secVal]]]."</td>";
                                    }else{
                                        //既往症
                                        $html["row"] .= "<td bgcolor=\"#FFFFFF\" width=\"29%\">";
                                        $cnm = "";
                                        foreach($codeArray[$secVal] as $codeKey => $codeVal){
                                            if($medData[$yearVal][$codeVal] != ""){
                                                $html["row"] .= $cnm.$code[$medData[$yearVal][$codeVal]];
                                                $cnm = " / ";
                                            }
                                        }
                                        $html["row"] .= "</td>";
                                    }
                                }
                            }else{
                                //コード変換とREMARKのもの
                                //コード取得
                                $query = knjh410_medicalQuery::getCode($getCode[$secVal]);
                                $cResult = $db->query($query);
                                while($cRow = $cResult->fetchRow(DB_FETCHMODE_ASSOC)){
                                    $code[$cRow["NAMECD2"]] = $cRow["NAME1"];
                                }
                                
                                foreach($year as $yearVal){
                                    $html["row"] .= "<td bgcolor=\"#FFFFFF\" width=\"29%\">";
                                    $cnm = "";
                                    foreach($brArray[$secVal] as $brKey => $brVal){
                                        //脊柱･胸郭、眼、耳鼻、内科は$brValが配列にはならない
                                        $html["row"] .= $cnm.$code[$medData[$yearVal][$brVal]];
                                        $cnm = "<BR>";
                                    }
                                    $html["row"] .= "</td>";
                                }
                            }
                            
                            $arg["data".$key][] = $html;
                            $html["row"] = "";
                        }
                    }else{
                        $secRow = get_count($secVal);
                        $html["row"] = "<th class=\"no_search\" rowspan=\"".$secRow."\" align=\"right\">".$secKey."</th>";
                        
                        if($secKey != "結核"){
                            foreach($secVal as $thrKey => $thrVal){
                                $html["row"] .= "<th class=\"no_search\" align=\"right\">".$thrVal."</th>";
                                
                                if(array_key_exists($thrVal, $tdArray)){
                                    //そのまま入れるデータ
                                    foreach($year as $yearVal){
                                        $html["row"] .= "<td bgcolor=\"#FFFFFF\" width=\"29%\">".$medData[$yearVal][$tdArray[$thrVal]]."</td>";
                                    }
                                    
                                    $arg["data".$key][] = $html;
                                    $html["row"] = "";
                                }else if(array_key_exists($secKey, $codeArray)){
                                    //コード変換のみ  尿、運動
                                    //コード取得
                                    $query = knjh410_medicalQuery::getCode($getCode[$secKey]);
                                    $cResult = $db->query($query);
                                    while($cRow = $cResult->fetchRow(DB_FETCHMODE_ASSOC)){
                                        if($secKey == "尿"){
                                            $code[$cRow["NAMECD2"]] = $cRow["NAME1"];
                                        }else{
                                            $code[$cRow["NAMECD1"]][$cRow["NAMECD2"]] = $cRow["NAME1"];
                                        }
                                    }
                                    
                                    $uric = array("蛋白：","糖：","潜血：");    //尿
                                    $play = array("GUIDE_DIV" => "F141","JOINING_SPORTS_CLUB" => "F142");     //運動
                                    foreach($year as $yearVal){
                                        $html["row"] .= "<td bgcolor=\"#FFFFFF\" width=\"29%\">";
                                        
                                        $cnm = "";
                                        if($secKey == "尿"){
                                            foreach($codeArray[$secKey][$thrVal] as $fourKey => $fourVal){
                                                $html["row"] .= $cnm.$uric[$fourKey].$code[$medData[$yearVal][$fourVal]];
                                                $cnm = "　";
                                            }
                                        }else{
                                            $html["row"] .= $code[$play[$codeArray[$secKey][$thrVal]]][$medData[$yearVal][$codeArray[$secKey][$thrVal]]];
                                        }
                                        $html["row"] .= "</td>";
                                    }
                                    
                                    $arg["data".$key][] = $html;
                                    $html["row"] = "";
                                }else{
                                    //コード変換<BR>備考　心臓
                                    //コード取得
                                    $query = knjh410_medicalQuery::getCode($getCode[$thrVal]);
                                    $cResult = $db->query($query);
                                    while($cRow = $cResult->fetchRow(DB_FETCHMODE_ASSOC)){
                                        $code[$cRow["NAMECD2"]] = $cRow["NAME1"];
                                    }
                                    
                                    foreach($year as $yearVal){
                                        $html["row"] .= "<td bgcolor=\"#FFFFFF\" width=\"29%\">";
                                        $cnm = "";
                                        foreach($brArray[$secKey][$thrVal] as $brKey => $brVal){
                                            if($code[$medData[$yearVal][$brVal]] != ""){
                                                $html["row"] .= $cnm.$code[$medData[$yearVal][$brVal]];
                                            }else{
                                                $html["row"] .= $cnm.$medData[$yearVal][$brVal];
                                            }
                                            $cnm = "<BR>";
                                        }
                                        $html["row"] .= "</td>";
                                    }
                                    $arg["data".$key][] = $html;
                                    $html["row"] = "";
                                }
                                
                            }
                        }else{
                            foreach($secVal as $thrKey => $thrVal){
                                $html["row"] .= "<th class=\"no_search\" align=\"right\">".$thrVal."</th>";
                                
                                //結核のコードを一度に取得
                                $query = knjh410_medicalQuery::getCode($tbCodeNamecd);
                                $cResult = $db->query($query);
                                while($cRow = $cResult->fetchRow(DB_FETCHMODE_ASSOC)){
                                    $code[$cRow["NAMECD1"]][$cRow["NAMECD2"]] = $cRow["NAME1"];
                                }
                                foreach($year as $yearVal){
                                    $html["row"] .= "<td bgcolor=\"#FFFFFF\" width=\"29%\">";
                                    
                                    if($thrVal == "間接撮影"){
                                        $html["row"] .= $medData[$yearVal]["TB_FILMDATE"]." ( ".$medData[$yearVal]["TB_FILMNO"]." )";
                                    }else{
                                        $cnm = "";
                                        foreach($tbArray[$thrVal] as $fourKey => $fourVal){
                                            if(array_key_exists($fourVal, $tbCode)){
                                                $html["row"] .= $cnm.$code[$tbCode[$fourVal]][$medData[$yearVal][$fourVal]];
                                                $cnm = "<BR>";
                                            }else{
                                                $html["row"] .= $cnm.$medData[$yearVal][$fourVal];
                                                $cnm = "<BR>";
                                            }
                                        }
                                    }
                                    
                                    $html["row"] .= "</td>";
                                }
                                
                                $arg["data".$key][] = $html;
                                $html["row"] = "";
                            }
                        }
                    }
                }
                
                
            }
            
        }else{
            //歯･口腔情報表示
            $thArray = array("健康診断実施日","歯列・咬合","顎関節","歯垢の状態","歯肉の状態","歯石沈着","矯正","乳歯" => array("現在数","未処置数","処置数","要注意乳歯数"),
                             "永久歯" => array("現在数","未処置数","処置数","喪失数","要観察歯数","要精検歯数"),"その他の疾病及び異常","学校歯科医" => array("所見","所見日付","事後措置"));
            //そのまま値として入れられるもの
            $tdArray = array("健康診断実施日" => "TOOTH_DATE",
                             "乳歯" => array("現在数" => "BABYTOOTH","未処置数" => "REMAINBABYTOOTH","処置数" => "TREATEDBABYTOOTH","要注意乳歯数" => "BRACK_BABYTOOTH"),
                             "永久歯" => array("現在数" => "ADULTTOOTH","未処置数" => "REMAINADULTTOOTH","処置数" => "TREATEDADULTTOOTH",
                                               "喪失数" => "LOSTADULTTOOTH","要観察歯数" => "BRACK_ADULTTOOTH","要精検歯数" => "CHECKADULTTOOTH"));
            
            //コード変換のみ
            $codeArray = array("歯列・咬合" => "JAWS_JOINTCD","顎関節" => "JAWS_JOINTCD2",
                               "歯垢の状態" => "PLAQUECD","歯肉の状態" => "GUMCD","歯石沈着" => "CALCULUS");
            
            //コード<BR>備考で入れるもの
            $brArray = array("その他の疾病及び異常" => array("OTHERDISEASECD","OTHERDISEASE"));
            //学校医
            $dentistArray = array("所見" => array("DENTISTREMARKCD","DENTISTREMARK"),"所見日付" => "DENTISTREMARKDATE","事後措置" => array("DENTISTTREATCD","DENTISTTREAT"));
            
            //コード取得するやつ
            $getCode = array("歯列・咬合" => "F510","顎関節" => "F511", "歯垢の状態" => "F520", "歯肉の状態" => "F513", "歯石沈着" => "F521",
                             "その他の疾病及び異常" => "F530", "所見" => "F540", "事後措置" => "F541");
            
            //一番上th
            //対象生徒のデータがある年度を取得
            $query = knjh410_medicalQuery::getmedYear($model->schregno, "MEDEXAM_TOOTH_DAT");
            $result = $db->query($query);
            
            //いったんデータのある年度を保持しておく
            $yCnt = 0;
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $keepYear[$row["YEAR"]] = $row;
                $yCnt++;
            }
            
            $nextYear = $model->year + 1;       //次の年
            $beforeYear = $model->year - 3;     //3年前の年
            
            //次の年度データがあるか
            if(!empty($keepYear) && array_key_exists($nextYear, $keepYear)){
                $nextFlg = 1;
            }else{
                $nextFlg = 0;
            }
            //前の年度データがあるか
            if(!empty($keepYear) && array_key_exists($beforeYear, $keepYear)){
                $beforeFlg = 1;
            }else{
                $beforeFlg = 0;
            }

            if($nextFlg != 0){
                //次年度ボタン
                $extra = "onclick=\"btn_submit('next_year');\"";
                $nextButton = knjCreateBtn($objForm, "nextButton", " ＜ ", $extra);
            }else{
                $nextButton = "";
            }
            if($beforeFlg != 0){
                //前年度ボタン
                $extra = "onclick=\"btn_submit('before_year');\"";
                $beforeButton = knjCreateBtn($objForm, "beforeButton", " ＞ ", $extra);
            }else{
                $beforeButton = "";
            }
            
            $yCnt = 0;
            //$med["th"] = "<th width=\"13%\" colspan=\"2\">".$nextButton."　".$beforeButton."</th>";
            $med["th"] = "<th width=\"6.5%\">".$nextButton."</th><th width=\"6.5%\">".$beforeButton."</th>";
            if(!empty($keepYear)){
                foreach($keepYear as $yearKey => $val){
                    if($yearKey < $nextYear && $yearKey > $beforeYear){
                        $med["th"] .= "<th width=\"29%\" height=\"20\">".$yearKey." ( ".$val["AGE"]."歳 )</th>";
                        
                        $year[$yCnt] = $yearKey;
                        $yCnt++;
                    }
                }
            }
            if($yCnt < 2){
                for($i = $yCnt; $i<3; $i++){
                    $med["th"] .= "<th width=\"29%\" height=\"20\"></th>";
                    
                    $year[$i] = " ";
                }
            }
            $arg["th"] = $med["th"];
            
            //データ取得
            $query = knjh410_medicalQuery::getToothData($model->schregno);
            $result = $db->query($query);
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $toothData[$row["YEAR"]] = $row;
            }
            
            //html作成
            foreach($thArray as $key => $val){
                if(!is_array($val)){
                    $html["row"] = "<th width=\"13%\" class=\"no_search\" colspan=\"2\" align=\"right\">".$val."</th>";
                    
                    if(array_key_exists($val, $tdArray)){
                        //そのまま入れる
                        foreach($year as $yearVal){
                            $html["row"] .= "<td bgcolor=\"#FFFFFF\" width=\"29%\">".$toothData[$yearVal][$tdArray[$val]]."</td>";
                        }
                        
                    }else if($val == "矯正"){
                        foreach($year as $yearVal){
                            if(!empty($toothData[$yearVal])){
                                if($toothData[$yearVal]["ORTHODONTICS"] != "1"){
                                    $html["row"] .= "<td bgcolor=\"#FFFFFF\" width=\"29%\">無</td>";
                                }else{
                                    $html["row"] .= "<td bgcolor=\"#FFFFFF\" width=\"29%\">有</td>";
                                }
                            }else{
                                $html["row"] .= "<td bgcolor=\"#FFFFFF\" width=\"29%\"></td>";
                            }
                        }
                        
                        
                    }else if(array_key_exists($val, $codeArray)){
                        //コード変換のみ
                        //コード取得
                        $query = knjh410_medicalQuery::getCode($getCode[$val]);
                        $cResult = $db->query($query);
                        while($cRow = $cResult->fetchRow(DB_FETCHMODE_ASSOC)){
                            $code[$cRow["NAMECD2"]] = $cRow["NAME1"];
                        }
                        
                        foreach($year as $yearVal){
                            $html["row"] .= "<td bgcolor=\"#FFFFFF\" width=\"29%\">".$code[$toothData[$yearVal][$codeArray[$val]]]."</td>";
                        }
                        
                    }else if(array_key_exists($val, $brArray)){
                        //コードと備考  その他の疾病及び異常
                        //コード取得
                        $query = knjh410_medicalQuery::getCode($getCode[$val]);
                        $cResult = $db->query($query);
                        while($cRow = $cResult->fetchRow(DB_FETCHMODE_ASSOC)){
                            $code[$cRow["NAMECD2"]] = $cRow["NAME1"];
                        }
                        
                        foreach($year as $yearVal){
                            $html["row"] .= "<td bgcolor=\"#FFFFFF\" width=\"29%\">";
                            $cnm = "";
                            foreach($brArray[$val] as $brKey => $brVal){
                                if($code[$toothData[$yearVal][$brVal]] != ""){
                                    $html["row"] .= $cnm.$code[$toothData[$yearVal][$brVal]];
                                }else{
                                    $html["row"] .= $cnm.$toothData[$yearVal][$brVal];
                                }
                                $cnm = "<BR>";
                            }
                            $html["row"] .= "</td>";
                        }
                    }
                    $arg["data1"][] = $html;
                    $html["row"] = "";
                }else{
                    $rows = get_count($val);
                    $html["row"] = "<th class=\"no_search\" align=\"right\" rowspan=\"".$rows."\">".$key."</th>";
                    
                    foreach($val as $secKey => $secVal){
                        $html["row"] .= "<th class=\"no_search\" align=\"right\">".$secVal."</th>";
                        
                        if(array_key_exists($key, $tdArray)){
                            //そのまま　乳歯と永久歯
                            foreach($year as $yearVal){
                                $html["row"] .= "<td bgcolor=\"#FFFFFF\" width=\"29%\">".$toothData[$yearVal][$tdArray[$key][$secVal]]."</td>";
                            }
                            
                        }else{
                            //学校歯科医しかないはず
                            if($getCode[$secVal] != ""){
                                //コード取得
                                $query = knjh410_medicalQuery::getCode($getCode[$secVal]);
                                $cResult = $db->query($query);
                                while($cRow = $cResult->fetchRow(DB_FETCHMODE_ASSOC)){
                                    $code[$cRow["NAMECD2"]] = $cRow["NAME1"];
                                }
                                //所見と事後措置
                                foreach($year as $yearVal){
                                    $html["row"] .= "<td bgcolor=\"#FFFFFF\" width=\"29%\">";
                                    foreach($dentistArray[$secVal] as $dentVal){
                                        if($code[$toothData[$dentVal]] != ""){
                                            $html["row"] .= $code[$toothData[$dentVal]];
                                        }else{
                                            $html["row"] .= $toothData[$dentVal];
                                        }
                                    }
                                    $html["row"] .= "</td>";
                                }
                                
                            }else{
                                //所見日付だけ
                                foreach($year as $yearVal){
                                    $html["row"] .= "<td bgcolor=\"#FFFFFF\" width=\"29%\">".$toothData[$yearVal][$dentistArray[$secVal]]."</td>";
                                }
                            }
                            
                        }
                        
                        $arg["data1"][] = $html;
                        $html["row"] = "";
                    }
                }
            }
        }
        
        
        //ボタン作成
        makeButton($objForm, $arg, $db, $model);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh410_medicalForm1.html", $arg);
    }

}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    if ($name == "YEAR") {
        $value = ($value != "" && $value_flg) ? $value : CTRL_YEAR;
    } else {
        $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    }
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

//ボタン作成
function makeButton(&$objForm, &$arg, $db, $model)
{

    //一般ボタン
    $extra = "onclick=\"btn_submit('general');\"";
    $arg["button"]["btn_general"] = knjCreateBtn($objForm, "btn_general", "一般", $extra);

    //歯・口腔ボタン
    $extra = "onclick=\"btn_submit('dental');\"";
    $arg["button"]["btn_dental"] = knjCreateBtn($objForm, "btn_dental", "歯・口腔", $extra);

    //終了ボタン
    $extra = "onclick=\"btn_reset();\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);
}
?>
