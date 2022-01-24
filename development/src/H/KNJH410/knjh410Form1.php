<?php

require_once('for_php7.php');
class knjh410Form1
{
    function main(&$model) {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knjh410index.php", "", "edit");
        //DB接続
        $db = Query::dbCheckOut();
        
        //ボタン表示用ラジオ
        if($model->btnRadio != ""){
            $arg["btn"] = 1;
        }else{
            $arg["btn"] = "";
        }
        $opt = array(1, 2, 3, 4);
        $extra = array("id=\"btnRadio1\" onclick=\"btn_submit('radio');\"", "id=\"btnRadio2\" onclick=\"btn_submit('radio');\"",
                       "id=\"btnRadio3\" onclick=\"btn_submit('radio');\"", "id=\"btnRadio4\" onclick=\"btn_submit('radio');\"");
        $label = array("btnRadio1" => "基本情報", "btnRadio2" => "住所情報", "btnRadio3" => "テスト･出欠情報", "btnRadio4" => "外部模試情報");
        $radioArray = knjCreateRadio($objForm, "btnRadio", $model->btnRadio, $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data3"][$key] = $val."<LABEL for=\"".$key."\">".$label[$key]."</LABEL>";

        //生徒資料取得
        if($model->schregno != ""){
            $dir = DOCUMENTROOT."/studentFiles";
            if(file_exists($dir)){
                $dirPath = DOCUMENTROOT."/studentFiles/".$model->schregno."/";
                if(is_dir($dirPath)){
                    $i = 3;
                    $x = 0;
                    $y = 0;
                    $arrayflg = 0;
                    $breakArray = array(".", "..");
                    
                    $aa = opendir($dirPath);
                    while (false !== ($filename = readdir($aa))) {
                        $hyouziname = mb_convert_encoding($filename,"UTF-8", "EUC-JP");       //表示用にエンコード
                        
                        if(in_array($hyouziname, $breakArray)){
                            continue;
                        }
                        
                        $bb = str_replace("src", "", DOCUMENTROOT);
                        $bb = str_replace("/usr/local", "", $bb);
                        $filedir = $bb."studentFiles/{$model->schregno}/".$filename;     //$filename は エンコードしてないファイル名
                        $info = pathinfo($filedir);

                        $setFiles = array();

                        $setFiles["DOWNLOAD_FILE_NAME"] = $hyouziname;
                        $setFiles["DOWNLOAD_FILE_TYPE2"] = $info["extension"];
                        $subwin = "SUBWIN".$i;
                        $root   = $bb."studentFiles/{$model->schregno}/".urlencode($filename);
                        $param  = "";
                        $setFiles["NEWWINDOW"] = "onClick=\"openGamen('{$root}', '{$param}', '{$subwin}', $x, $y);\"";

                        $setFiles["DOWNLOAD_FILE_NAME"] = $hyouziname;

                        //プレビュー表示
                        //$usefile = DOCUMENTROOT."/studentFiles/{$model->schregno}/".$filename;
                        //ファイルの更新時間取得
                        //$updatetime = date("Y/m/d H:i", filemtime($usefile));

                        //$setFiles["TIME"] = " (".$updatetime.") ";
            
                        $filelist[] = $setFiles;
                        $arrayflg = 1;

                        $i++;
                        $x = $x + 50;
                        $y = $y + 50;
                    }
                }
                $filePath = "";
                $sp = "";
                if(!empty($filelist)){
                    foreach($filelist as $key =>$val){
                        $filePath .= $sp."<a align=\"center\" href=\"#\" {$val["NEWWINDOW"]}>{$val["DOWNLOAD_FILE_NAME"]}{$val["TIME"]}</a>";
                        $sp = "　";
                    }
                    if($filePath != ""){
                        $arg["FILE_LINK"] = $filePath;
                    }
                }
            }
            
        }
        
        //生徒データ表示
        makeStudentInfo($arg, $db, $model);

        if($model->cmd != "edit" && $model->cmd != "radio"){
            $createData = $model->cmd;
        }else{
            $createData = $model->backData;
        }
        //radioボタンの番号が前回と違った場合、初期画面として年度情報を表示
        if ($model->btnRadio != $model->preBtnRadio) {
            $createData = "init";
        }
        if($createData == "schreg"){
            //学籍基礎情報
            $schreg = array();
            
            //データ取得
            $Row = $db->getRow(knjh410Query::getStudent_data($model->schregno, $model), DB_FETCHMODE_ASSOC);

            //誕生日
            $schreg["BIRTHDAY"] = str_replace("-","/",$Row["BIRTHDAY"]);

            //性別
            $schreg["SEX"] = $Row["SEX"]."：".$Row["SEX_NAME"];

            //血液型(型)
            $schreg["BLOOD"] = "型：".$Row["BLOODTYPE"];

            //血液型(RH型)
            $schreg["BLOOD"] .= "　RH：".$Row["BLOOD_RH"];

            //その他
            if($Row["HANDICAP"] != ""){
                $schreg["OTHER"] = $Row["HANDICAP"]."：".$row["OTHER"];
            }

            //国籍
            if($Row["NATIONALITY"] != ""){
                $schreg["NATION"] = $Row["NATIONALITY"]."：".$row["NATION_NAME"];
            }

            //出身中学校
            $extra = "";
            $schreg["FINSCHOOLCD"] = $Row["FINSCHOOLCD"];
            $finschoolname = $db->getOne(knjh410Query::getFinschoolName($Row["FINSCHOOLCD"]));
            $Row["FINSCHOOLNAME"] = $Row["FINSCHOOLNAME"] ? $Row["FINSCHOOLNAME"] : $finschoolname;
            
            $schreg["FINSCHOOL"] = $Row["FINSCHOOLCD"]."：".$Row["FINSCHOOLNAME"];
            
            //出身中学校 卒業年月日
            $schreg["FINISH_DATE"] = "卒業年月日：".str_replace("-","/",$Row["FINISH_DATE"]);
            
            //入学
            $query = knjh410Query::getComeBackT();
            $isComeBack = $db->getOne($query) > 0 ? true : false;
            if ($isComeBack) {
                $query = knjh410Query::getCB_entDate($model);
                $comeBackEntDate = $db->getOne($query);
                if ($comeBackEntDate) {
                    $schreg["CB_ENT_DATE"] = str_replace("-", "/", $comeBackEntDate);
                }
            }

            $schreg["ENT_DATE"] = str_replace("-","/",$Row["ENT_DATE"]);
            //課程入学年度
            $extra = "onblur=\"this.value=toInteger(this.value)\";";
            $schreg["CURRICULUM_YEAR"] = $Row["CURRICULUM_YEAR"];
            //入学区分
            $schreg["ENT_DIV"] = $Row["ENT_DIV"].".".$Row["ENT_DIV_NAME"];
            //受験番号
            $extra = "onblur=\"this.value=toInteger(this.value)\";";
            $examLen = $model->Properties["examnoLen"] ? $model->Properties["examnoLen"] : "5";
            $schreg["EXAMNO"] = $Row["EXAMNO"];
            
            $schreg["ENTER"] = "日付：".$schreg["ENT_DATE"]."<BR>課程入学年度：".$schreg["CURRICULUM_YEAR"];
            $schreg["ENTER"] .= "<BR>区分：".$schreg["ENT_DIV"]."<BR>受験番号：".$schreg["EXAMNO"];
            
            //事由
            $extra = "";
            $schreg["ENT_REASON"] = $Row["ENT_REASON"];

            //学校名
            $extra = "";
            $schreg["ENT_SCHOOLNAME"] = $Row["ENT_SCHOOL"];

            //学校住所1
            $extra = "";
            $schreg["ENT_SCHOOL_ADDR1"] = $Row["ENT_ADDR"];

            //住所２使用(小学校、中学校)
            if ($model->Properties["useAddrField2"] == "1" && $model->schoolKind != "H") {
                $schreg["ADDR2_NAME"] = "住所2";
                $schreg["ROWSPAN"] = "4";
                $arg["ADDR2"] = "1";
            //高校(住所2と同一フィールド名だが、項目名が異なる)
            } else if ($model->schoolKind == "H") {
                $schreg["ADDR2_NAME"] = "課程･学科等";
                $schreg["ROWSPAN"] = "4";
                $arg["ADDR2"] = "1";
            } else {
                $schreg["ENTADDR2_NAME"] = "住所2";
                $schreg["ROWSPAN"] = "3";
            }

            //学校住所2
            $extra = "";
            $schreg["ENT_SCHOOL_ADDR2"] = $Row["ENT_ADDR2"];

            //卒業
            $schreg["GRD_DATE"] = str_replace("-","/",$Row["GRD_DATE"]);
            $schreg["GRD_DIV"] = $Row["GRD_DIV"].".".$Row["GRD_DIV_NAME"];

            if ($model->schoolKind != "H") {
                $schreg["TENGAKU_SAKI_ZENJITU"] = str_replace("-","/",$Row["TENGAKU_SAKI_ZENJITU"]);
            }
            $schreg["TENGAKU_SAKI_GRADE"] = $Row["TENGAKU_SAKI_GRADE"];

            $schreg["OUT"] = "日付：".$schreg["GRD_DATE"]."<BR>区分：".$schreg["GRD_DIV"];
            $schreg["OUT"] .= "<BR>転学先前日：".$schreg["TENGAKU_SAKI_ZENJITU"]."<BR>転学先学年：".$schreg["TENGAKU_SAKI_GRADE"];

            //事由
            $extra = "";
            $schreg["OUT_REASON"] = $Row["GRD_REASON"];

            //学校名
            $extra = "";
            $schreg["OUT_SCHOOLNAME"] = $Row["GRD_SCHOOL"];

            //学校住所1
            $extra = "";
            $schreg["OUT_SCHOOL_ADDR1"] = $Row["GRD_ADDR"];

            //学校住所2
            $extra = "";
            $schreg["OUT_SCHOOL_ADDR2"] = $Row["GRD_ADDR2"];

            //出身塾
            if($Row["PRISCHOOLCD"] != ""){
                $prischool = $db->getRow(knjh410Query::getPrischoolName($model, $Row["PRISCHOOLCD"]), DB_FETCHMODE_ASSOC);
                $schreg["PRISCHOOL"] = $Row["PRISCHOOLCD"]."：".$prischool["PRISCHOOL_NAME"];
            }

            //備考1
            $extra = "";
            $schreg["REMARK1"] = $Row["REMARK1"];

            //備考2
            $extra = "";
            $schreg["REMARK2"] = $Row["REMARK2"];

            //備考3
            $extra = "";
            $schreg["REMARK3"] = $Row["REMARK3"];
            
                
            //行の色
            $schreg["color"] = "#ffffff";

            $arg["schreg"] = $schreg;
            

        }else if($createData == "club"){
            //部活情報
            $club = array();
            
            $club["TITLE"] = "部活動情報";
            $club["thRow"] = "<th width=\"200\">部クラブ</th><th width=\"100\">日付</th><th width=\"270\">記録</th><th width=\"*\">備考</th>";
            $arg["choice"] = $club;

            
            $query = knjh410Query::getClub($model->schregno);
            $result = $db->query($query);
            
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $club["tdRow"] = "<td width=\"200\">".$row["CLUBNAME"]."</td><td width=\"100\">".str_replace("-", "/", $row["DETAIL_DATE"])."</td><td width=\"270\">".$row["DOCUMENT"]."</td><td width=\"*\">".$row["DETAIL_REMARK"]."</td>";
                
                //行の色
                $club["color"] = "#ffffff";
                
                $arg["choice1"][] = $club;
            }
            
        }else if($createData == "committee"){
            //委員会情報
            $committee = array();
            
            $committee["TITLE"] = "委員会情報";
            $committee["thRow"] = "<th width=\"200\">委員会名／係り名</th><th width=\"100\">日付</th><th width=\"*\">記録備考</th>";
            $arg["choice"] = $committee;
            
            $query = knjh410Query::getCommittee($model);
            $result = $db->query($query);

            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $committee["tdRow"]  = "<td width=\"200\">".$row["SEQ"] . ":" . $row["COMMITTEENAME"] . " ／ " . $row["CHARGENAME"]."</td>";
                $committee["tdRow"] .= "<td width=\"100\">".str_replace("-", "/", $row["DETAIL_DATE"])."</td><td width=\"*\">".$row["DETAIL_REMARK"]."</td>";
                
                //行の色
                $committee["color"] = "#ffffff";
                
                $arg["choice1"][] = $committee;
            }

        }else if($createData == "shikaku"){
            //資格情報
            $shikaku = array();
            
            if ($model->Properties["useQualifiedMst"] == '1') {
                $thName = "名称 ／ 級・段位";
            } else {
                $thName = "内容";
            }
            
            $shikaku["TITLE"] = "資格情報";
            $shikaku["thRow"] = "<th width=\"300\">".$thName."</th><th width=\"100\">取得日</th><th width=\"*\">備考</th>";
            $arg["choice"] = $shikaku;
            
            $query = knjh410Query::getAward($model, $model->schregno);
            $result = $db->query($query);
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $shikaku["tdRow"] = "<td width=\"300\">".$row["HYOUJI_CONTENTS"]."</td><td width=\"100\">".$row["REGDDATE"]."</td><td width=\"*\">".$row["REMARK"]."</td>";
                
                //行の色
                $shikaku["color"] = "#ffffff";
                
                $arg["choice1"][] = $shikaku;
            }

        }else if($createData == "train"){
            //指導情報
            $train = array();
            
            $train["TITLE"] = "指導情報";
            $train["thRow"] = "<th width=\"90\">指導日</th><th width=\"130\">相談者</th><th width=\"150\">対応者</th><th width=\"130\">指導方法</th><th width=\"*\">指導内容</th>";
            $arg["choice"] = $train;
            
            $query = knjh410Query::selectQuery($model);
            $result = $db->query($query);
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $train["tdRow"]  = "<td width=\"90\">".$row["TRAINDATE"]."</td><td width=\"130\">".$row["PATIENT"]."</td><td width=\"150\">".$row["STAFFNAME_SHOW"]."</td>";
                $train["tdRow"] .= "<td width=\"130\">".$row["HOWTOTRAIN"]."</td><td width=\"*\">".$row["CONTENT"]."</td>";
                
                //行の色
                $train["color"] = "#ffffff";
                
                $arg["choice1"][] = $train;
            }

        }else if($createData == "detail"){
            //賞罰情報
            $detail = array();
            
            $detail["TITLE"] = "賞罰情報";
            $detail["thRow"] = "<th width=\"90\">登録日</th><th width=\"130\">詳細区分</th><th width=\"130\">詳細種類</th><th width=\"300\">詳細内容</th><th width=\"*\">備考</th>";
            $arg["choice"] = $detail;
            
            $query = knjh410Query::getDetail($model);
            $result = $db->query($query);
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $detail["tdRow"]  = "<td width=\"90\">".$row["DETAIL_SDATE"]."</td><td width=\"130\">".$row["DETAIL_DATA"]."</td><td width=\"130\">".$row["DETAILCD"]."</td>";
                $detail["tdRow"] .= "<td width=\"300\">".$row["CONTENT"]."</td><td width=\"*\">".$row["REMARK"]."</td>";
                
                //行の色
                $detail["color"] = "#ffffff";
                
                $arg["choice1"][] = $detail;
                
            }
        }else if($createData == "attend"){
            //出欠情報
            $attend = array();
            
            //欠席の名称取得
            $attendName = array();
            $query = knjh410Query::getAttendName();
            $result =$db->query($query);
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $attendName[$row["NAMECD2"]] = $row["NAME1"];
            }
            
            $attend["TITLE"] = "出欠情報";
            $attend["thRow"]  = "<th width=\"6%\" rowspan=\"2\">年度</th><th width=\"6%\" rowspan=\"2\">学年</th><th width=\"6%\" rowspan=\"2\">学期</th>";
            $attend["thRow"] .= "<th width=\"8%\" rowspan=\"2\">授業日数</th><th width=\"8%\" rowspan=\"2\">休学日数</th><th width=\"8%\" rowspan=\"2\">出席停止日数</th>";
            $attend["thRow"] .= "<th width=\"8%\" rowspan=\"2\">忌引日数</th><th width=\"8%\" rowspan=\"2\">留学中日数</th><th width=\"8%\" rowspan=\"2\">出席しなければならない日数</th>";
            $attend["thRow"] .= "<th colspan=\"3\">欠席日数</th><th width=\"*\" rowspan=\"2\">出席日数</th>";
            $attend["thRow"] .= "</tr><tr class=\"no_search\" nowrap align=\"center\">";
            $attend["thRow"] .= "<th width=\"50\">".$attendName[4]."</th><th width=\"50\">".$attendName[5]."</th><th width=\"50\">".$attendName[6]."</th>";
            
            $arg["choice"] = $attend;
            
            $query = knjh410Query::getAttend($model->Properties, $model->schregno);
            $result = $db->query($query);
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $attend["tdRow"]  = "<td width=\"6%\">".$row["YEAR"]."</td>";
                if($row["GRADE_NAME"] != ""){
                    $attend["tdRow"] .= "<td width=\"6%\">".$row["GRADE_NAME"]."</td>";
                }else{
                    $attend["tdRow"] .= "<td width=\"6%\">".$grade_name."</td>";
                }
                $attend["tdRow"] .= "<td width=\"6%\">".$row["SEMESTERNAME"]."</td>";

                $attend["tdRow"] .= "<td width=\"8%\" align=\"right\">".number_format($row["LESSON"])."</td>";
                $attend["tdRow"] .= "<td width=\"8%\" align=\"right\">".number_format($row["OFFDAYS"])."</td>";
                
                //出席停止
                if($model->Properties["useVirus"] == 'true'){
                    $row["SUSPEND"] = number_format($row["SUSPEND"]) + number_format($row["VIRUS"]);
                }
                if($model->Properties["useKoudome"] == 'true'){
                    $row["SUSPEND"] = number_format($row["SUSPEND"]) + number_format($row["KOUDOME"]);
                }
                $attend["tdRow"] .= "<td width=\"8%\" align=\"right\">".number_format($row["SUSPEND"])."</td>";
                
                $attend["tdRow"] .= "<td width=\"8%\" align=\"right\">".number_format($row["MOURNING"])."</td>";
                $attend["tdRow"] .= "<td width=\"8%\" align=\"right\">".number_format($row["ABROAD"])."</td>";
                
                //出席しなければならない日数
                $must = $row["LESSON"] - ($row["OFFDAYS"] + $row["ABROAD"] + $row["SUSPEND"] + $row["MOURNING"]);
                if($row["SEM_OFFDAYS"] == "1"){
                    $must = $must + $row["OFFDAYS"];
                }
                $attend["tdRow"] .= "<td width=\"8%\" align=\"right\">".number_format($must)."</td>";
                
                //NAME_MST　C001の4の部分
                if($row["SICK_FLG"] != ""){
                    $attend["tdRow"] .= "<td width=\"50\" align=\"right\">".number_format($row["SICK"])."</td>";
                }else{
                    $attend["tdRow"] .= "<td width=\"50\" align=\"right\">-</td>";
                }
                //NAME_MST　C001の5の部分
                if($row["NOTICE_FLG"] != ""){
                    $attend["tdRow"] .= "<td width=\"50\" align=\"right\">".number_format($row["NOTICE"])."</td>";
                }else{
                    $attend["tdRow"] .= "<td width=\"50\" align=\"right\">-</td>";
                }
                //NAME_MST　C001の6の部分
                $attend["tdRow"] .= "<td width=\"50\" align=\"right\">".number_format($row["NONOTICE"])."</td>";
                
                //出席日数
                $attendDays = $row["LESSON"] - ($row["SUSPEND"] + $row["MOURNING"] + $row["OFFDAYS"] + $row["ABROAD"]) - ($row["SICK"] + $row["NOTICE"] + $row["NONOTICE"]);
                $attend["tdRow"] .= "<td width=\"*\" align=\"right\">".number_format($attendDays)."</td>";
                
                //学年末の行だけ色付けたい
                if($row["SEMESTER"] != '9'){
                    $attend["color"] = "#ffffff";
                }else{
                    $attend["color"] = "#fffd93";
                }
                
                $arg["choice1"][] = $attend;
                $grade_name = $row["GRADE_NAME"];
            }
        }else if($createData == "tuugaku"){
            //通学情報
            $tuugaku = array();
            
            $tuugaku["TITLE"] = "通学情報";
            $tuugaku["thRow"] = "";
            $arg["choice"] = $tuugaku;
            
            $query = knjh410Query::getTuugakuData($model->schregno);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            
            $thNameArray  = array("災害時帰宅グループ番号", "責任者", "通学所要時間", "通学方法");
            $thFieldArray = array(array("GO_HOME_GROUP_NO","GO_HOME_GROUP_NAME"), array("RESPONSIBILITY","RESPONSIBLE"),
                                  array("COMMUTE_HOURS", "COMMUTE_MINUTES"), array("HOWTOCOMMUTECD", "COMMUTE"));
            
            foreach($thNameArray as $key => $val){
                $tuugaku["color"] = "#ffffff";
                $tuugaku["tdRow"] = "";
                
                $tuugaku["tdRow"] .= "<th width=\"20%\" class=\"no_search\" align=\"right\" colspan=\"2\">".$val."</th>";
                if($key != 2){
                    $tuugaku["tdRow"] .= "<td>".$row[$thFieldArray[$key][0]]."：".$row[$thFieldArray[$key][1]]."</td>";
                }else{
                    //通学所要時間だけ
                    $tuugaku["tdRow"] .= "<td>".$row[$thFieldArray[$key][0]]."時間 ".$row[$thFieldArray[$key][1]]."分</td>";
                }
                
                $arg["choice1"][] = $tuugaku;
            }
            
            //経路作成
            $rootArray = array("JOSYA_", "ROSEN_", "GESYA_");
            $rootNameArray = array("乗車駅", "路線", "下車駅");
            
            $tuugaku["tdRow"] = "";
            
            for($i=1; $i<8; $i++){
                if($row["JOSYA_".$i] != ""){
                    $rosen_name = "";
                    
                    if($i == 1){
                        $tuugaku["tdRow"] .= "<th width=\"10%\" class=\"no_search\" align=\"right\" rowspan=\"3\">自宅</th>";
                    }else if($i == 7){
                        $tuugaku["tdRow"] .= "<th width=\"10%\" class=\"no_search\" align=\"right\" rowspan=\"3\">学校</th>";
                    }else{
                        $keiro = $i+1;
                        $tuugaku["tdRow"] .= "<th width=\"10%\" class=\"no_search\" align=\"right\" rowspan=\"3\">経路".$keiro."</th>";
                    }
                    
                    foreach($rootArray as $key => $val){
                        
                        if($i == 1 || $i == 7){
                            if($key == 0){
                                $tuugaku["tdRow"] .= "<th width=\"10%\" class=\"no_search\" align=\"right\">最寄り駅</th>";
                            }else{
                                $tuugaku["tdRow"] .= "<th width=\"10%\" class=\"no_search\" align=\"right\">".$rootNameArray[$key]."</th>";
                            }
                        }else{
                            $tuugaku["tdRow"] .= "<th width=\"10%\" class=\"no_search\" align=\"right\">".$rootNameArray[$key]."</th>";
                        }
                        
                        if($val != "ROSEN_"){
                            if($row["FLG_".$i] == "1"){
                                //コードから駅名と路線名取得
                                $query = knjh410Query::getStationName($row["ROSEN_".$i], $row[$val.$i]);
                                $station = $db->getRow($query, DB_FETCHMODE_ASSOC);
                                
                                $tuugaku["tdRow"] .= "<td>".$station["STATION_NAME"]."</td>";
                                
                                $rosen_name = $station["LINE_NAME"];
                                
                            }else{
                                $tuugaku["tdRow"] .= "<td>".$row[$val.$i]."</td>";
                            }
                        }else{
                            if($row["FLG_".$i] == "1"){
                                //路線名は乗車駅名称取得したときに一緒にとってきたもの
                                $tuugaku["tdRow"] .= "<td>".$rosen_name."</td>";
                            }else{
                                $tuugaku["tdRow"] .= "<td>".$row[$val.$i]."</td>";
                            }
                        }
                        
                        
                        $arg["choice1"][] = $tuugaku;
                        $tuugaku["tdRow"] = "";
                    }
                    
                    $rosen_name = "";
                }
            }
        }else if($createData == "seito"){
            //生徒情報
            $seito = array();
            
            $seito["TITLE"] = "生徒情報";
            $seito["thRow"]  = "<th width=\"87\">住所<br>有効期間開始</th><th width=\"87\">住所<br>有効期間終了</th><th width=\"6%\">郵便番号</th><th width=\"18%\">住所</th>";
            $seito["thRow"] .= "<th width=\"18%\">方書</th><th width=\"9%\">電話番号</th><th width=\"9%\">急用連絡先</th><th width=\"9%\">急用連絡氏名</th><th width=\"*\">急用電話番号</th>";
            
            $arg["choice"] = $seito;
            
            $query = knjh410Query::getStudentAddr($model->schregno);
            $result = $db->query($query);
            
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $seito["color"] = "#ffffff";
                $seito["tdRow"] = "";
                
                $seito["tdRow"]  = "<td width=\"87\">".$row["ISSUEDATE"]."</td><td width=\"87\">".$row["EXPIREDATE"]."</td><td width=\"6%\">".$row["ZIPCD"]."</td>";
                $seito["tdRow"] .= "<td width=\"18%\">".$row["ADDR1"]."</td><td width=\"18%\">".$row["ADDR2"]."</td><td width=\"9%\">".$row["TELNO"]."</td>";
                $seito["tdRow"] .= "<td width=\"9%\">".$row["EMERGENCYCALL"]."</td><td width=\"9%\">".$row["EMERGENCYNAME"]."</td><td width=\"*\">".$row["EMERGENCYTELNO"]."</td>";
                
                $arg["choice1"][] = $seito;
            }
            
        }else if($createData == "hogo" || $createData == "hogo2" || $createData == "hosyou"){
            //保護者情報・保護者情報２・保証人情報
            $name = array("hogo" => "保護者情報", "hogo2" => "保護者情報2", "hosyou" => "保証人情報");
            $table = array("hogo" => "GUARDIAN", "hogo2" => "GUARDIAN2", "hosyou" => "GUARANTOR");
            $fieldName = array("hogo" => "GUARD", "hogo2" => "GUARD", "hosyou" => "GUARANTOR");
            $parent = array();
            
            $parent["TITLE"] = $name[$createData];
            $parent["thRow"]  = "<th width=\"87\">住所<br>有効期間開始</th><th width=\"87\">住所<br>有効期間終了</th><th width=\"10%\">氏名</th><th width=\"10%\">郵便番号</th>";
            $parent["thRow"] .= "<th width=\"20%\">住所</th><th width=\"20%\">方書</th><th width=\"*\">電話番号</th>";
            
            $arg["choice"] = $parent;
            
            $query = knjh410Query::getParentAddr($model->schregno, $table[$createData], $fieldName[$createData]);
            $result = $db->query($query);
            
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $parent["color"] = "#ffffff";
                
                $parent["tdRow"]  = "<th width=\"87\">".$row["ISSUEDATE"]."</th><th width=\"87\">".$row["EXPIREDATE"]."</th><th width=\"10%\">".$row[$fieldName[$createData]."_NAME"]."</th><th width=\"10%\">".$row[$fieldName[$createData]."_ZIPCD"]."</th>";
                $parent["tdRow"] .= "<th width=\"20%\">".$row[$fieldName[$createData]."_ADDR1"]."</th><th width=\"20%\">".$row[$fieldName[$createData]."_ADDR2"]."</th><th width=\"*\">".$row[$fieldName[$createData]."_TELNO"]."</th>";
                
                $arg["choice1"][] = $parent;
            }
            
            
        }else if($createData == "other"){
            //その他情報
            $other = array();
            
            $other["TITLE"] = "その他情報";
            $other["thRow"]  = "<th width=\"5%\">区分</th><th width=\"10%\">氏名</th><th width=\"10%\">郵便番号</th><th width=\"25%\">住所</th>";
            $other["thRow"] .= "<th width=\"25%\">方書</th><th width=\"15%\">電話番号1</th><th width=\"*%\">電話番号2</th>";
            
            $arg["choice"] = $other;
            
            $query = knjh410Query::getOtherAddr($model->schregno);
            $result = $db->query($query);
            
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $other["color"] = "#ffffff";
                
                $div = ($row["DIV"] == "1") ? "その他" : "その他".mb_convert_kana($row["DIV"], "N");

                $other["tdRow"]  = "<th width=\"5%\">".$div."</th><th width=\"10%\">".$row["SEND_NAME"]."</th><th width=\"10%\">".$row["SEND_ZIPCD"]."</th><th width=\"25%\">".$row["SEND_ADDR1"]."</th>";
                $other["tdRow"] .= "<th width=\"25%\">".$row["SEND_ADDR2"]."</th><th width=\"15%\">".$row["SEND_TELNO"]."</th><th width=\"*%\">".$row["SEND_TELNO2"]."</th>";
                
                $arg["choice1"][] = $other;
            }
            
        }else if($createData == "family"){
            //家族情報
            $family = array();
            
            $family["TITLE"] = "家族情報";
            $family["thRow"]  = "<th width=\"9%\">連番</th><th width=\"9%\">氏名</th><th width=\"10%\">氏名(かな)</th><th width=\"15%\">性別</th>";
            $family["thRow"] .= "<th width=\"15%\">生年月日</th><th width=\"10%\">続柄</th>";
            
            $arg["choice"] = $family;
            
            $query = knjh410Query::getFamilyData($model->schregno);
            $result = $db->query($query);
            
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $family["color"] = "#ffffff";
                
                $birthday = str_replace("-", "/", $row["RELABIRTHDAY"]);
                
                $family["tdRow"]  = "<th width=\"9%\">".$row["RELANO"]."</th><th width=\"9%\">".$row["RELANAME"]."</th><th width=\"10%\">".$row["RELAKANA"]."</th><th width=\"15%\">".$row["SEX"]."</th>";
                $family["tdRow"] .= "<th width=\"15%\">".$birthday."</th><th width=\"10%\">".$row["RELATION"]."</th>";
                
                $arg["choice1"][] = $family;
            }
            
        }else if($createData == "action_document" || $createData == "STAFFSORTCLICK" || $createData == "DATESORTCLICK" || $createData == "TITLESORTCLICK" || $createData == "subEnd" || $createData == "delete"){
            //行動の記録
            $action_document = array();

            $action_document["TITLE"] = "行動の記録";

            //ALLチェック
            $checkAll = createCheckBox($objForm, "CHECKALL", "", "onClick=\"return check_all(this);\"", "");
            //分類絞り込みコンボ
            $query = knjh410Query::getNameMst($model, 'H307');
            $extra = "onChange=\"return btn_submit('radio');\"";
            $narrowingCmb = makeCmb($objForm, $arg, $db, $query, "NARROWING", $model->narrowing, $extra, 1, "BLANK");
            //並び替え設定
            $sortLinkArr = array();
            $sortQuery = makeSortLink($sortLinkArr, $model);

            $action_document["thRow"]  = "<th width=\"20\">削<br>$checkAll</th><th width=\"200\">{$sortLinkArr["STAFFSORT"]}</th><th width=\"80\">{$sortLinkArr["DATESORT"]}</th>";
            $action_document["thRow"] .= "<th width=\"50\">時間</th><th width=\"80\">分類<br>$narrowingCmb</th><th width=\"*\">{$sortLinkArr["TITLESORT"]}</th>";

            $arg["choice"] = $action_document;

            //追加
            $extra = "onclick=\"return btn_submit('insert');\"";
            $arg["button"]["btn_insert"] = createBtn($objForm, "btn_insert", "追 加", $extra);
            //削除
            $extra = "onclick=\"return btn_submit('delete');\"";
            $arg["button"]["btn_del"] = createBtn($objForm, "btn_del", "削 除", $extra);

            //行動の記録
            makeActionData($objForm, $arg, $db, $model, $sortQuery);

        }else{
            //初期画面で生徒情報を表示する
            $nendo = array();
            
            $nendo["TITLE"] = "年度情報";
            $nendo["thRow"] = "<th width=\"5%\">年度</th><th width=\"8%\">学年</th><th width=\"8%\">学期</th><th width=\"8%\">組</th><th width=\"8%\">番</th><th width=\"20%\">担任</th><th width=\"22%\">部活</th><th width=\"*\">委員会</th>";
            
            $arg["choice"] = $nendo;
            
            $query = knjh410Query::getSchregAllData($model->schregno);
            $result = $db->query($query);
            
            $grade = "";
            $semester = "";
            $cnm1 = "";
            $cnm2 = "";
            $tdData = array();
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                if($grade != "" && $semester != ""){
                    if($grade != $row["GRADE"] || $semester != $row["SEMESTER"]){
                        $nendo["tdRow"]  = "<td width=\"5%\">".$tdData["YEAR"]."</td><td width=\"8%\">".$tdData["GRADENAME"]."</td><td width=\"8%\">".$tdData["SEMESTERNAME"]."</td>";
                        $nendo["tdRow"] .= "<td width=\"8%\">".$tdData["HRCLASSNAME"]."</td><td width=\"8%\">".$tdData["ATTENDNO"]."</td><td width=\"20%\">".$tdData["STAFFNAME"]."</td>";
                        $nendo["tdRow"] .= "<td width=\"22%\">".$tdData["CLUB"]."</td><td width=\"*\">".$tdData["COMMITTEE"]."</td>";
                        
                        //行の色
                        $nendo["color"] = "#ffffff";
                        
                        $arg["choice1"][] = $nendo;
                        $tdData = array();
                        $cnm1 = "";
                        $cnm2 = "";
                    }
                }
                    
                    $tdData["YEAR"] = $row["YEAR"];
                    $tdData["GRADENAME"] = $row["GRADE_NAME"];
                    $tdData["SEMESTERNAME"] = $row["SEMESTERNAME"];
                    $tdData["HRCLASSNAME"] = $row["HR_CLASS_NAME1"];
                    $tdData["ATTENDNO"] = number_format($row["ATTENDNO"]);
                    
                    if($row["STAFFNAME2"] != ""){
                        $tdData["STAFFNAME"] = $row["STAFFNAME_SHOW"]." / ".$row["STAFFNAME2"];
                    }else{
                        $tdData["STAFFNAME"] = $row["STAFFNAME_SHOW"];
                    }

                    if ($row["CHKFLG"] == '1') {
                        $tdData["CLUB"]      .= $cnm1.$row["NAME"];
                                            $cnm1 = " / ";

                    }else if ($row["CHKFLG"] == '2') {
                        $tdData["COMMITTEE"] .= $cnm2.$row["NAME"];
                                            $cnm2 = " / ";

                    }

                    
                    $grade = $row["GRADE"];
                    $semester = $row["SEMESTER"];
                
            }
            if(!empty($tdData)){
                $nendo["tdRow"]  = "<td width=\"5%\">".$tdData["YEAR"]."</td><td width=\"8%\">".$tdData["GRADENAME"]."</td><td width=\"8%\">".$tdData["SEMESTERNAME"]."</td>";
                $nendo["tdRow"] .= "<td width=\"8%\">".$tdData["HRCLASSNAME"]."</td><td width=\"8%\">".$tdData["ATTENDNO"]."</td><td width=\"20%\">".$tdData["STAFFNAME"]."</td>";
                $nendo["tdRow"] .= "<td width=\"22%\">".$tdData["CLUB"]."</td><td width=\"*\">".$tdData["COMMITTEE"]."</td>";
                
                //行の色
                $nendo["color"] = "#ffffff";
                
                $arg["choice1"][] = $nendo;
            }

        }
        //コマンド変わっても表示するため
        knjCreateHidden($objForm, "BACK_DATA", $createData);
        knjCreateHidden($objForm, "preBtnRadio", $model->btnRadio);
        knjCreateHidden($objForm, "auth2", $model->auth2);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        $arg["IFRAME"] = View::setIframeJs();
        View::toHTML($model, "knjh410Form1.html", $arg);
    }
}

//生徒データ表示
function makeStudentInfo(&$arg, $db, $model)
{
    $model->assess = 0;
    if (isset($model->schregno)) {
        $grad_Row = $db->getRow(knjh410Query::getStudentData($model), DB_FETCHMODE_ASSOC);
        $grad_Row["FACE_IMG"] = REQUESTROOT."/".$model->control_data["LargePhotoPath"]."/P".$grad_Row["SCHREGNO"].".".$model->control_data["Extension"];
        $grad_Row["IMG_PATH"] = REQUESTROOT."/".$model->control_data["LargePhotoPath"]."/P".$grad_Row["SCHREGNO"].".".$model->control_data["Extension"];
        
        if($grad_Row["NAMESPARE2"] == "1"){
            $model->assess = 1;
        }
    }
    $arg["data"] = $grad_Row;
}

//並び替え設定
function makeSortLink(&$sortLinkArr, $model)
{
    $sortQuery = $model->taitleSort[$model->sort]["ORDER".$model->taitleSort[$model->sort]["VALUE"]];
    foreach ($model->taitleSort as $key => $val) {

        $sortLinkArr[$key] = View::alink("knjh410index.php",
                                         "<font color=\"white\">".$val["NAME".$val["VALUE"]]."</font>",
                                         "",
                                         array("cmd" => $key."CLICK", $key => $val["VALUE"], "sort" => $key));
        if ($key != $model->sort) {
            $sortQuery .= $val["ORDER".$val["VALUE"]];
        }
    }
    return $sortQuery;
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    if ($blank) $opt[] = array('label' => "", 'value' => "");
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//行動の記録
function makeActionData(&$objForm, &$arg, $db, $model, $sortQuery)
{
    $action_document = array();
    $time = array();
    $result = $db->query(knjh410Query::getActionDuc($model, $sortQuery));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
    
        //管理者以外かつ非公開設定のフラグ
        $privateFlg = !IS_KANRISYA && $row["PRIVATE"] == "1";

        //チェックボックス
        $disable = (AUTHORITY != DEF_UPDATABLE && $row["STAFFCD"] != STAFFCD) || ($privateFlg) ? "disabled" : "";
        $checkVal = $row["SCHREGNO"].":".$row["ACTIONDATE"].":".$row["SEQ"];
        $row["DELCHK"] = createCheckBox($objForm, "DELCHK", $checkVal, $disable, "1");

        //リンク設定
        if (!$privateFlg) {
            $subdata = "loadwindow('../../H/KNJH410_ACTION_DOCUMENT/knjh410_action_documentindex.php?cmd=updateSub&SCHREGNO={$row["SCHREGNO"]}&ACTIONDATE={$row["ACTIONDATE"]}&SEQ={$row["SEQ"]}&SEND_PRGID=KNJH410&SEND_AUTH={$model->auth2}',0,0,600,450)";
            $row["TITLE"] = View::alink("#", htmlspecialchars($row["TITLE"]),"onclick=\"$subdata\"");
        }

        $time = preg_split("{:}", $row["ACTIONTIME"]);
        $row["ACTIONTIME"] = $time[0]."：".$time[1];
        
        $action_document["color"] = "#ffffff";
        $row["ACTIONDATE"] = str_replace("-", "/", $row["ACTIONDATE"]);
        
        $action_document["tdRow"]  = "<td width=\"20\" align=\"center\" >{$row["DELCHK"]}</td><td width=\"200\" align=\"left\" >{$row["STAFFNAME"]}</td>";
        $action_document["tdRow"] .= "<td width=\"80\" align=\"left\" >{$row["ACTIONDATE"]}</td><td width=\"50\" align=\"center\">{$row["ACTIONTIME"]}</td>";
        $action_document["tdRow"] .= "<td width=\"80\" align=\"center\">{$row["DIVIDENAME"]}</td><td width=\"*\" align=\"left\" >{$row["TITLE"]}</td>";

        $arg["choice1"][] = $action_document;
    }
    $result->free();
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    $authAll  = "OFF";
    $authSome = "OFF";
    if ($model->auth["CHAIRFLG"] == "ON" || $model->auth["HRCLASSFLG"] == "ON" || $model->auth["COURSEFLG"] == "ON") {
        $authAll = "ON";
    }
    if ($model->auth["CLUBFLG"] == "ON") {
        $authSome = "ON";
    }

    if($model->btnRadio == "1"){
        //年度情報
        $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF" && $authSome == "OFF") ? "disabled" : "";
        $extra = $disable." onclick=\"btn_submit('nendo');\"";
        $arg["data"]["BTN_NENDO"] = createBtn($objForm, "BTN_NENDO", "年度情報", $extra);

        //学籍基礎情報
        $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF" && $authSome == "OFF") ? "disabled" : "";
        $extra = $disable." onclick=\"btn_submit('schreg');\"";
        $arg["data"]["BTN_SCHREG"] = createBtn($objForm, "BTN_SCHREG", "学籍基礎情報", $extra);

        //部活情報
        $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF" && $authSome == "OFF") ? "disabled" : "";
        $extra = $disable." onclick=\"btn_submit('club');\"";
        $arg["data"]["BTN_CLUB"] = createBtn($objForm, "BTN_CLUB", "部活情報", $extra);

        //委員会情報
        $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF" && $authSome == "OFF") ? "disabled" : "";
        $extra = $disable." onclick=\"btn_submit('committee');\"";
        $arg["data"]["BTN_COMMITTEE"] = createBtn($objForm, "BTN_COMMITTEE", "委員会情報", $extra);
        
        //資格情報
        $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF" && $authSome == "OFF") ? "disabled" : "";
        $extra = $disable." onclick=\"btn_submit('shikaku');\"";
        $arg["data"]["BTN_SHIKAKU"] = createBtn($objForm, "BTN_SHIKAKU", "資格情報", $extra);

        //指導情報
        $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF" && $authSome == "OFF") ? "disabled" : "";
        $extra = $disable." onclick=\"btn_submit('train');\"";
        $arg["data"]["BTN_TRAIN"] = createBtn($objForm, "BTN_TRAIN", "指導情報", $extra);
        
        //賞罰情報
        $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF" && $authSome == "OFF") ? "disabled" : "";
        $extra = $disable." onclick=\"btn_submit('detail');\"";
        $arg["data"]["BTN_DETAIL"] = createBtn($objForm, "BTN_DETAIL", "賞罰情報", $extra);
        
        //通学情報
        $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF" && $authSome == "OFF") ? "disabled" : "";
        $extra = $disable." onclick=\"btn_submit('tuugaku');\"";
        $arg["data"]["BTN_TSUGAKU"] = createBtn($objForm, "BTN_TSUGAKU", "通学情報", $extra);

        //保健情報
        $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF" && $authSome == "OFF") ? "disabled" : "";
        $extra = $disable." onClick=\" wopen('".REQUESTROOT."/X/KNJXHOKEN/knjxhokenindex.php?cmd=&SCHREGNO=".$model->schregno."&YEAR=".$model->year."&BUTTON_FLG=1','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        $arg["data"]["BTN_HOKEN"] = createBtn($objForm, "BTN_HOKEN", "保健情報", $extra);

        //保健情報
        $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF" && $authSome == "OFF") ? "disabled" : "";
        $extra = $disable." onClick=\" wopen('".REQUESTROOT."/H/KNJH410_MEDICAL/knjh410_medicalindex.php?cmd=&SCHREGNO=".$model->schregno."','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        $arg["data"]["BTN_MEDICAL"] = createBtn($objForm, "BTN_MEDICAL", "健康診断情報", $extra);
        
        //日常所見
        $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF" && $authSome == "OFF") ? "disabled" : "";
        $extra = $disable." onClick=\" wopen('".REQUESTROOT."/H/KNJH410_EVERY/knjh410_everyindex.php?SCHREGNO=".$model->schregno."','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        $arg["data"]["BTN_EVERY"] = createBtn($objForm, "BTN_EVERY", "日常所見", $extra);
        
        if($model->assess != 0){
            //特別支援
            $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF" && $authSome == "OFF") ? "disabled" : "";
            $extra = $disable." onClick=\" wopen('".REQUESTROOT."/H/KNJH410_ASSESS/knjh410_assessindex.php?cmd=edit&SCHREGNO=".$model->schregno."&EXP_YEAR=".$model->year."&EXP_SEMESTER=".$model->semester."','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
            $arg["data"]["BTN_ASSESS"] = createBtn($objForm, "BTN_ASSESS", "特別支援", $extra);
        }

        //行動の記録
        $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF" && $authSome == "OFF") ? "disabled" : "";
        $extra = $disable." onclick=\"btn_submit('action_document');\"";
        $arg["data"]["BTN_ACTION_DOCUMENT"] = createBtn($objForm, "BTN_ACTION_DOCUMENT", "行動の記録", $extra);

    }else if($model->btnRadio == "2"){

        //住所詳細
        $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF" && $authSome == "OFF") ? "disabled" : "";
        $extra = $disable." onClick=\" wopen('".REQUESTROOT."/A/KNJA110_2A/knja110_2aindex.php?&SEND_UN_UPDATE=1&SCHREGNO=".$model->schregno."&YEAR=".$model->year."&BUTTON_FLG=1','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        //$extra = $disable;
        //$extra = $disable." onclick=\"btn_submit('seito');\"";
        $arg["data"]["BTN_ZYUSYO"] = createBtn($objForm, "BTN_ZYUSYO", "住所詳細", $extra);

        //生徒情報
        $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF" && $authSome == "OFF") ? "disabled" : "";
        //$extra = $disable." onClick=\" wopen('".REQUESTROOT."/U/KNJA110_2A/knja110_2aindex.php?&SCHREGNO=".$model->schregno."&YEAR=".CTRL_YEAR."&BUTTON_FLG=1','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        //$extra = $disable;
        $extra = $disable." onclick=\"btn_submit('seito');\"";
        $arg["data"]["BTN_SEITO"] = createBtn($objForm, "BTN_SEITO", "生徒情報", $extra);

        //保護者情報
        $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF" && $authSome == "OFF") ? "disabled" : "";
        //$extra = $disable." onClick=\" wopen('".REQUESTROOT."/U/KNJA110_2A/knja110_2aindex.php?&SCHREGNO=".$model->schregno."&YEAR=".CTRL_YEAR."&BUTTON_FLG=1','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        //$extra = $disable;
        $extra = $disable." onclick=\"btn_submit('hogo');\"";
        $arg["data"]["BTN_HOGOSYA"] = createBtn($objForm, "BTN_HOGOSYA", "保護者情報", $extra);
        
        if($model->Properties["useGuardian2"] == "1"){
            //保護者情報2
            $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF" && $authSome == "OFF") ? "disabled" : "";
            //$extra = $disable." onClick=\" wopen('".REQUESTROOT."/U/KNJA110_2A/knja110_2aindex.php?&SCHREGNO=".$model->schregno."&YEAR=".CTRL_YEAR."&BUTTON_FLG=1','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
            //$extra = $disable;
            $extra = $disable." onclick=\"btn_submit('hogo2');\"";
            $arg["data"]["BTN_HOGOSYA2"] = createBtn($objForm, "BTN_HOGOSYA2", "保護者情報2", $extra);
        }

        //保証人情報
        $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF" && $authSome == "OFF") ? "disabled" : "";
        $extra = $disable." onClick=\" wopen('".REQUESTROOT."/U/KNJA110_2A/knja110_2aindex.php?&SCHREGNO=".$model->schregno."&YEAR=".$model->year."&BUTTON_FLG=1','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        //$extra = $disable;
        $extra = $disable." onclick=\"btn_submit('hosyou');\"";
        $arg["data"]["BTN_HOSYOUNIN"] = createBtn($objForm, "BTN_HOSYOUNIN", "保証人情報", $extra);

        //その他情報
        $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF" && $authSome == "OFF") ? "disabled" : "";
        //$extra = $disable." onClick=\" wopen('".REQUESTROOT."/U/KNJA110_2A/knja110_2aindex.php?&SCHREGNO=".$model->schregno."&YEAR=".CTRL_YEAR."&BUTTON_FLG=1','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        //$extra = $disable;
        $extra = $disable." onclick=\"btn_submit('other');\"";
        $arg["data"]["BTN_SONOTA"] = createBtn($objForm, "BTN_SONOTA", "その他情報", $extra);

        //家族情報
        $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF" && $authSome == "OFF") ? "disabled" : "";
        //$extra = $disable." onClick=\" wopen('".REQUESTROOT."/U/KNJA110_2A/knja110_2aindex.php?&SCHREGNO=".$model->schregno."&YEAR=".CTRL_YEAR."&BUTTON_FLG=1','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        //$extra = $disable;
        $extra = $disable." onclick=\"btn_submit('family');\"";
        $arg["data"]["BTN_KAZOKU"] = createBtn($objForm, "BTN_KAZOKU", "家族情報", $extra);



    }else if($model->btnRadio == "3"){
        //定期考査情報
        $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF") ? "disabled" : "";
        $extra = $disable." onClick=\" wopen('".REQUESTROOT."/H/KNJH310/knjh310index.php?SCHREGNO=".$model->schregno."','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        $arg["data"]["BTN_TEST"] = createBtn($objForm, "BTN_TEST", "定期考査情報", $extra);
        //出欠情報
        $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF") ? "disabled" : "";
        //$extra = $disable." onClick=\" wopen('".REQUESTROOT."/X/KNJXATTEND2/knjxattendindex.php?SCHREGNO=".$model->schregno."','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        $extra = $disable." onclick=\"btn_submit('attend');\"";
        $arg["data"]["BTN_APPEND"] = createBtn($objForm, "BTN_APPEND", "出欠情報", $extra);
        //実力テスト情報
        $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF") ? "disabled" : "";
        $extra = $disable." onClick=\" wopen('".REQUESTROOT."/H/KNJH320/knjh320index.php?SCHREGNO=".$model->schregno."','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        $arg["data"]["BTN_SHAM"] = createBtn($objForm, "BTN_SHAM", "実力テスト情報", $extra);
    }else if($model->btnRadio == "4"){
        //志望校推移
        $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF" && $authSome == "OFF") ? "disabled" : "";
        $extra = $disable." onClick=\" wopen('".REQUESTROOT."/H/KNJH410_SIBOU/knjh410_sibouindex.php?&SCHREGNO=".$model->schregno."&YEAR=".$model->year."&BUTTON_FLG=1','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        $arg["data"]["BTN_SHIBOU"] = createBtn($objForm, "BTN_SCHREG", "志望校推移", $extra);
        //判定推移
        $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF" && $authSome == "OFF") ? "disabled" : "";
        $extra = $disable." onClick=\" wopen('".REQUESTROOT."/H/KNJH410_HANTEI/knjh410_hanteiindex.php?&SCHREGNO=".$model->schregno."&YEAR=".$model->year."&BUTTON_FLG=1','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        $arg["data"]["BTN_HANTEI"] = createBtn($objForm, "BTN_SCHREG", "判定推移", $extra);
        //成績推移
        $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF" && $authSome == "OFF") ? "disabled" : "";
        $extra = $disable." onClick=\" wopen('".REQUESTROOT."/H/KNJH410_SEISEKI/knjh410_seisekiindex.php?&SCHREGNO=".$model->schregno."&YEAR=".$model->year."&BUTTON_FLG=1','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        $arg["data"]["BTN_SEISEKI"] = createBtn($objForm, "BTN_SCHREG", "成績推移", $extra);
        //教科バランス
        $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF" && $authSome == "OFF") ? "disabled" : "";
        $extra = $disable." onClick=\" wopen('".REQUESTROOT."/H/KNJH410_KYOUKA/knjh410_kyoukaindex.php?&SCHREGNO=".$model->schregno."&YEAR=".$model->year."&BUTTON_FLG=1','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        $arg["data"]["BTN_KYOUKA"] = createBtn($objForm, "BTN_SCHREG", "教科間バランス", $extra);
    }
    
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
