<?php

require_once('for_php7.php');

class knjd121kForm1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjd121kindex.php", "", "main");

        //処理年度
        $arg["YEAR"]=CTRL_YEAR;

        $db = Query::dbCheckOut();

        //仮評定フラグ対応
        if ($model->Properties["useProvFlg"] == '1') {
            $arg["useProvFlg"] = "1";
        } else {
            $arg["not_useProvFlg"] = "1";
        }

        //学期名
        if (is_numeric($model->control["学期数"])){
            $arg["SEMESTER"] = array();
            for ($i = 0; $i < $model->control["学期数"]; $i++){
                $arg["SEMESTER"][$i] = $model->control["学期名"][$i+1];
            }
        }

        $opt_sbuclass = array();
        $opt_sbuclass[] = array("label" => "", "value" => "");
        //SQL文発行
        $query = knjd121kQuery::selectSubclassQuery($model);
        $result = $db->query($query);
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_sbuclass[] = array("label" => $row["CLASSCD"].'-'.$row["SCHOOL_KIND"].'-'.$row["CURRICULUM_CD"].'-'.$row["SUBCLASSCD"]." ".$row["SUBCLASSABBV"],
                                    "value" => $row["CLASSCD"].'-'.$row["SCHOOL_KIND"].'-'.$row["CURRICULUM_CD"].'-'.$row["SUBCLASSCD"]
                                   );
            }
        } else {
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_sbuclass[] = array("label" => $row["SUBCLASSCD"]." ".$row["SUBCLASSABBV"],
                                    "value" => $row["SUBCLASSCD"]
                                   );
            }
        }
        //科目コンボ
        $objForm->ae( array("type"        => "select",
                            "name"        => "SUBCLASSCD",
                            "size"        => "1",
                            "value"       => $model->field["SUBCLASSCD"],
                            "options"     => $opt_sbuclass,
                            "extrahtml"   => "onChange=\"btn_submit('subclasscd')\";"
                           ));

        $arg["SUBCLASSCD"] = $objForm->ge("SUBCLASSCD");

        //エンター押下時の移動方向
        $opt = array(1, 2);
        $model->field["MOVE_ENTER"] = ($model->field["MOVE_ENTER"] == "") ? "1" : $model->field["MOVE_ENTER"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"MOVE_ENTER{$val}\"");
        }
        $radioArray = knjCreateRadio($objForm, "MOVE_ENTER", $model->field["MOVE_ENTER"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg[$key] = $val;

        $opt_chair = array();
        //先頭に空リストをセット
        $opt_chair[] = array("label" => "", "value" => "");
        
        //SQL文発行
        $query = knjd121kQuery::selectChairQuery($model);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_chair[] = array("label" => $row["CHAIRCD"]." ".$row["CHAIRNAME"],
                                 "value" => $row["CHAIRCD"]);
        }
        //講座コンボ
        $objForm->ae( array("type"        => "select",
                            "name"        => "CHAIRCD",
                            "size"        => "1",
                            "value"       => $model->field["CHAIRCD"],
                            "options"     => $opt_chair,
                            "extrahtml"   => "onChange=\"btn_submit('chaircd')\";"
                           ));
        $arg["CHAIRCD"] = $objForm->ge("CHAIRCD");

      //----------------------以下、擬似フレーム内リスト表示----------------------
            //病欠・公欠のバックカラーを配列にする
            $backcolor = array( "KK" => "#3399ff",  //公欠
                                "KS" => "#ff0099"   //病欠
                              );

            //試験名称
            $control_name[1][1] = "SEM1_INTER_REC";
            $control_name[1][2] = "SEM1_TERM_REC";
            $control_name[1][3] = "SEM1_REC";
            $control_name[2][1] = "SEM2_INTER_REC";
            $control_name[2][2] = "SEM2_TERM_REC";
            $control_name[2][3] = "SEM2_REC";
            $control_name[3][2] = "SEM3_TERM_REC";
            $control_name[3][3] = "SEM3_REC";
            $control_name[4][1] = "GRADE_RECORD";
            $control_name[4][2] = "GRADE_ASSESS";       ##2004/11/22 arakaki
            
            //試験成績フラグ
            $flg[1][1] = "SEM1_INTER_REC_FLG";
            $flg[1][2] = "SEM1_TERM_REC_FLG";
            $flg[1][3] = "SEM1_REC_FLG";
            $flg[2][1] = "SEM2_INTER_REC_FLG";
            $flg[2][2] = "SEM2_TERM_REC_FLG";
            $flg[2][3] = "SEM2_REC_FLG";
            $flg[3][2] = "SEM3_TERM_REC_FLG";
            $flg[3][3] = "SEM3_REC_FLG";

            $seme_test = "0";
            $kind_test = "0";

            //選択
            $electdiv = $db->getOne(knjd121kQuery::getElectDiv(substr($model->field["SUBCLASSCD"],0,2), $model));

            //管理者コントロール
            $admin_key = array();
            $query = knjd121kQuery::selectContolCodeQuery($model);
            $result = $db->query($query);
            while( $row_ct = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                //選択科目の時は、管理者コントロールの文字評定=8014以外は、入力可のコードがあっても無効とする
                //必須科目の時は、管理者コントロールの文字評定=8014のみは、入力可のコードがあっても無効とする
                if (0 < $electdiv && $row_ct["CONTROL_CODE"] != "8014") continue;
                if (1 > $electdiv && $row_ct["CONTROL_CODE"] == "8014") continue;

                $sem = sprintf("%1d",substr($row_ct["CONTROL_CODE"],"0","2"));
                $t_kind = substr($row_ct["CONTROL_CODE"],"3","2");
                $admin_key[$sem][$t_kind]=$row_ct["CONTROL_CODE"];
                $name = $control_name[$row_ct["CONTROL_CODE"]];
                //学年平均
                if($row_ct["CONTROL_CODE"]=="8003"){
                    $admin_key[4][1]=$row_ct["CONTROL_CODE"];
                }
                //学年評定  2004/11/22 arakaki
                if($row_ct["CONTROL_CODE"]=="8004" || $row_ct["CONTROL_CODE"]=="8014"){
                    $admin_key[4][2]=$row_ct["CONTROL_CODE"];
                }

                if ($row_ct["CONTROL_CODE"] > "0303") continue;
                $seme_test =  substr($row_ct["CONTROL_CODE"], 1, 1);
                $kind_test = (substr($row_ct["CONTROL_CODE"], 2, 2) == "01") ? "01" : "02" ;
            }

            //時間割講座テストより試験日を抽出
            $execute_date = CTRL_DATE;//初期値
            if ($seme_test != "0") {
                $result = $db->query(knjd121kQuery::selectExecuteDateQuery($model->field["CHAIRCD"], $seme_test, $kind_test));
                while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
                {
                    $execute_date = $row["EXECUTEDATE"];
                }
            }

            $arg["ASSESS"] = "学年";
            //文字評定
            if (0 < $electdiv) {
                $arg["ASSESS"] = "文字";
                $assess = array();
                $result = $db->query(knjd121kQuery::getAssessMark());
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
                {
                    $assess[$row["ASSESSLEVEL"]] = $row["ASSESSMARK"];
                }
                //チェック用
                $objForm->ae( array("type"      => "hidden",
                                    "name"      => "assess",
                                    "value"     => implode($assess, ",")
                                    ) );
            }

            //初期化
            $model->data = array();
            $model->attend_data = array(); //出欠情報
            $model->step = array();

            //ボタンの押下不可
            $disabled = "disabled";
            $counter=0;
            //SQL文発行
            $query = knjd121kQuery::selectQuery($model);
            $result = $db->query($query);
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                //文字評定を格納
                if (0 < $electdiv) $row["GRADE_ASSESS"] = $assess[$row["GRADE_ASSESS"]];

                //３学期成績のデータを格納
                $v_sum3_rec = $row["SEM3_REC"];

                //クラス-出席番(表示)
                if($row["HR_NAMEABBV"]!=""&$row["ATTENDNO"]!=""){
                    $row["ATTENDNO"] = sprintf("%s-%02d", $row["HR_NAMEABBV"], $row["ATTENDNO"]);
                }

                //学籍番号を配列で取得
                $model->data["SCHREGNO"][] = $row["SCHREGNO"];
                //学年を配列で取得
                $model->data["GRADE"][] = $row["GRADE"];

                //入力可能なテキストの名前を取得する
                $setTextField = "";
                $textSep = "";
                //各項目を作成
                for($i=1;$i<=get_count($control_name);$i++){
                    for($ii=1;$ii<=$model->control["学期数"];$ii++){
                        //各データを取得
                        $model->data[$control_name[$i][$ii]."-".$counter]=$row[$control_name[$i][$ii]];
                        $model->attend_data[$control_name[$i][$ii]."-".$counter] = $row[$control_name[$i][$ii]."_DI"];            //出欠情報も追加
                        //各データフラグを取得
                        $model->flg_data[$flg[$i][$ii]."-".$counter]=$row[$flg[$i][$ii]];
                        //学期成績集計項目
                        if(is_numeric($row[$control_name[$i][$ii]])){
                            //３学期編集中で、学年成績が非表示になるデータは追加しない
                            //遡及入力されたデータは追加しない（１学期成績フラグまたは２学期成績フラグが１）
                            if (!(($control_name[$i][$ii] == "SEM1_REC" | $control_name[$i][$ii] == "SEM2_REC") && $row[$control_name[$i][$ii]."_FLG"] == "1")) {
                                $term_data[$control_name[$i][$ii]][]=$row[$control_name[$i][$ii]];
                            }
                        }                
                        $eflg = "";
                        $cflg = "";
                        //異動者情報の取得
                        $result2 = $db->query(knjd121kQuery::getTransferData($row["YEAR"], $row["SCHREGNO"]));
                        while( $row2 = $result2->fetchRow(DB_FETCHMODE_ASSOC)){

                            //異動終了日の値がnullの場合は学期マスタのEDATEの値を使う。 
                            $edate = "";
                            if (isset($row2["TRANSFER_SDATE"]) && !isset($row2["TRANSFER_EDATE"])) {
                                $edate = $row2["EDATE"];
                            } elseif (isset($row2["TRANSFER_SDATE"]) && isset($row2["TRANSFER_EDATE"])) {
                                $edate = $row2["TRANSFER_EDATE"];
                            }

                            //異動情報があればバックカラーを黄色にする
                            if ($edate != "" && $row2["TRANSFER_SDATE"] <= $execute_date && $execute_date <= $edate) {
                                $row[$control_name[$i][$ii]."_COLOR"]="#ffff00";
                                $eflg = true;
                                break 1;
                            }
                        }
                        $result2->free();
                        if ($eflg == "") {       
                            ////2004/12/09 arakaki 近大-作業依頼書20041022-01｢卒業･退学･転学チェック｣
                            if ($row["GRD_DATE"] != "" && $row["GRD_DATE"] <= $execute_date) {
                                $eflg = true;
                            //該当講座が未受講ならバックカラーを黄色にする
                            } elseif ($i != 4 && !isset($row["CHAIR_SEM".$i])) {
                                $cflg = true;
                            }
                        }
                        $keys=$i.$ii;
                        //ラベルのみ(補点,補充,異動情報があり制限付き更新可の場合,該当講座を受講していない学期で制限付き更新可の場合)
                        if(($eflg != "" && $model->sec_competence != DEF_UPDATABLE) |
                           ($cflg != "" && $model->sec_competence != DEF_UPDATABLE) |
                           ($row[$control_name[$i][$ii]."_FLG"] == "1" | $row[$control_name[$i][$ii]."_FLG"] == "2"))
                        {
                            $setTextField .= "";
                        } else {
                            //選択科目の時は、管理者コントロールの文字評定=8014以外は、入力可のコードがあっても無効とする
                            if (0 < $electdiv) {
                                if ($control_name[$i][$ii] == "GRADE_ASSESS" && $admin_key[$i][$ii] == $admin_key[4][2] && isset($admin_key[$i][$ii])) {
                                    $setTextField .= $textSep.$control_name[$i][$ii]."-";
                                    $textSep = ",";
                                }
                            //管理者学期コントロール(編集可能な項目のテキストボックスを作成)
                            } elseif (isset($admin_key[$i][$ii])) {
                                //管理者コントロールで学年成績と学年評定が編集可能になっていても、３学期成績編集中ならテキストボックスは作成しない
                                //if(!(($admin_key[$i][$ii] == $admin_key[4][1]) && (isset($admin_key[3][2]) || isset($admin_key[3][3])) && $row["SEM3_REC"] == "") &&
                                //   !(($admin_key[$i][$ii] == $admin_key[4][2]) && (isset($admin_key[3][2]) || isset($admin_key[3][3])) && $row["SEM3_REC"] == "") ) {
                                    $setTextField .= $textSep.$control_name[$i][$ii]."-";
                                    $textSep = ",";
                                //}
                            }
                        }
                    }
                }
                //３年時のみ相対評価テキストボックスを表示
                if($row["GRADE"]=="03"){
                    if(isset($admin_key[90][1])) {    //2004/12/14 arakaki 管理者コントロール制御追加
                        $setTextField .= $textSep."GRADE3_RELAASSESS_5STEP"."-";
                        $textSep = ",";
                    }
                    if(isset($admin_key[90][2])) {    //2004/12/14 arakaki 管理者コントロール制御追加
                        $setTextField .= $textSep."GRADE3_RELAASSESS_10STEP"."-";
                        $textSep = ",";
                    }
                } else {
                    $setTextField .= "";
                }
                
                //各項目を作成
                for($i=1;$i<=get_count($control_name);$i++){
                    
                    for($ii=1;$ii<=$model->control["学期数"];$ii++){
                        //各データを取得
                        $model->data[$control_name[$i][$ii]."-".$counter]=$row[$control_name[$i][$ii]];
                        $model->attend_data[$control_name[$i][$ii]."-".$counter] = $row[$control_name[$i][$ii]."_DI"];            //出欠情報も追加
                        
                        //各データフラグを取得
                        $model->flg_data[$flg[$i][$ii]."-".$counter]=$row[$flg[$i][$ii]];

                        $eflg = "";
                        $cflg = "";

                        //異動者情報の取得
                        $result2 = $db->query(knjd121kQuery::getTransferData($row["YEAR"], $row["SCHREGNO"]));
                        while( $row2 = $result2->fetchRow(DB_FETCHMODE_ASSOC)){

                            //異動終了日の値がnullの場合は学期マスタのEDATEの値を使う。 
                            $edate = "";
                            if (isset($row2["TRANSFER_SDATE"]) && !isset($row2["TRANSFER_EDATE"])) {
                                $edate = $row2["EDATE"];
                            } elseif (isset($row2["TRANSFER_SDATE"]) && isset($row2["TRANSFER_EDATE"])) {
                                $edate = $row2["TRANSFER_EDATE"];
                            }

                            //異動情報があればバックカラーを黄色にする
                            if ($edate != "" && $row2["TRANSFER_SDATE"] <= $execute_date && $execute_date <= $edate) {
                                $row[$control_name[$i][$ii]."_COLOR"]="#ffff00";
                                $eflg = true;
                                break 1;
                            }
                        }
                        $result2->free();
                        
                        if ($eflg == "") {       
                            ////2004/12/09 arakaki 近大-作業依頼書20041022-01｢卒業･退学･転学チェック｣
                            if ($row["GRD_DATE"] != "" && $row["GRD_DATE"] <= $execute_date) {
                                $row[$control_name[$i][$ii]."_COLOR"]="#ffff00";
                                $eflg = true;
                            //該当講座が未受講ならバックカラーを黄色にする
                            } elseif ($i != 4 && !isset($row["CHAIR_SEM".$i])) {
                                $row[$control_name[$i][$ii]."_COLOR"] = ($i <= CTRL_SEMESTER) ? "#ffff00" : "#ffffff";
                                $cflg = true;
                            //バックカラー(出欠情報があれば色つける)
                            } elseif (strlen($row[$control_name[$i][$ii]."_DI"])) {
                                $row[$control_name[$i][$ii]."_COLOR"] = $backcolor[$row[$control_name[$i][$ii]."_DI"]];
                            } else { 
                                $row[$control_name[$i][$ii]."_COLOR"]="#ffffff";
                            }
                        }

                        $keys=$i.$ii;
                        $controlFlg = '';
                        //ラベルのみ(補点,補充,異動情報があり制限付き更新可の場合,該当講座を受講していない学期で制限付き更新可の場合)
                        if(($eflg != "" && $model->sec_competence != DEF_UPDATABLE) |
                           ($cflg != "" && $model->sec_competence != DEF_UPDATABLE) |
                           ($row[$control_name[$i][$ii]."_FLG"] == "1" | $row[$control_name[$i][$ii]."_FLG"] == "2"))
                        {
                            //遡及入力された値は赤字表示する（１学期成績フラグまたは２学期成績フラグが１）
                            if (($control_name[$i][$ii] == "SEM1_REC" | $control_name[$i][$ii] == "SEM2_REC") && $row[$control_name[$i][$ii]."_FLG"] == "1")
                            {
                                $row[$control_name[$i][$ii]] = "<font color=\"#00cc00\">".$row[$control_name[$i][$ii]]."</font>";
                            } else {
                                $row[$control_name[$i][$ii]] = "<font color=\"#000000\">".$row[$control_name[$i][$ii]]."</font>";
                            }

                        } else {
                            //選択科目の時は、管理者コントロールの文字評定=8014以外は、入力可のコードがあっても無効とする
                            if (0 < $electdiv) {
                                if ($control_name[$i][$ii] == "GRADE_ASSESS" && $admin_key[$i][$ii] == $admin_key[4][2] && isset($admin_key[$i][$ii])) {

                                    //出欠情報がある場合はそれを表示
                                    $value = (strlen($row[$control_name[$i][$ii]."_DI"])) ? $row[$control_name[$i][$ii]."_DI"] : $row[$control_name[$i][$ii]];

                                    //テキストボックスを作成
                                    $objForm->ae( array("type"      => "text",
                                                        "name"      => $control_name[$i][$ii]."-".$counter,
                                                        "size"      => "3",
                                                        "maxlength" => "3",
                                                        "value"     => $value,
                                                        "extrahtml" => " onKeyDown=\"keyChangeEntToTab2(this, '{$setTextField}', {$counter})\"; STYLE=\"text-align: right\" onChange=\" selcontrol(this,$counter,$keys); this.style.background='#ccffcc'\" onblur=\"calc(this);\" onKeyDown=\"keyChangeEntToTab(this)\" onPaste=\"return showPaste(this);\" id=\"{$control_name[$i][$ii]}-{$counter}\""));
                                    $row[$control_name[$i][$ii]] = $objForm->ge($control_name[$i][$ii]."-".$counter);
                                    //貼付対象フラグ
                                    knjCreateHidden($objForm, $control_name[$i][$ii]."_INPUT"."-".$counter, 1);
                                }

                            //管理者学期コントロール(編集可能な項目のテキストボックスを作成)
                            } elseif (isset($admin_key[$i][$ii])) {
                                //管理者コントロールで学年成績と学年評定が編集可能になっていても、３学期成績編集中ならテキストボックスは作成しない
                                if(!(($admin_key[$i][$ii] == $admin_key[4][1]) && (isset($admin_key[3][2]) || isset($admin_key[3][3])) && $row["SEM3_REC"] == "") &&
                                   !(($admin_key[$i][$ii] == $admin_key[4][2]) && (isset($admin_key[3][2]) || isset($admin_key[3][3])) && $row["SEM3_REC"] == "") ) {
                                    $controlFlg = '1';
                                    //出欠情報がある場合はそれを表示
                                    $value = (strlen($row[$control_name[$i][$ii]."_DI"])) ? $row[$control_name[$i][$ii]."_DI"] : $row[$control_name[$i][$ii]];

                                    //テキストボックスを作成
                                    $objForm->ae( array("type"      => "text",
                                                        "name"      => $control_name[$i][$ii]."-".$counter,
                                                        "size"      => "3",
                                                        "maxlength" => "3",
                                                        "value"     => $value,
                                                        "extrahtml" => " onKeyDown=\"keyChangeEntToTab2(this, '{$setTextField}', {$counter})\"; STYLE=\"text-align: right\" onChange=\" selcontrol(this,$counter,$keys); this.style.background='#ccffcc'\" onblur=\"calc(this);\" onKeyDown=\"keyChangeEntToTab(this)\" onPaste=\"return showPaste(this);\" id=\"{$control_name[$i][$ii]}-{$counter}\""));
                                    $row[$control_name[$i][$ii]] = $objForm->ge($control_name[$i][$ii]."-".$counter);
                                    //貼付対象フラグ
                                    knjCreateHidden($objForm, $control_name[$i][$ii]."_INPUT"."-".$counter, 1);
                                }
                            }
                        }
                        //仮評定フラグ対応
                        if ($model->Properties["useProvFlg"] == '1') {
                            if ($control_name[$i][$ii] == "GRADE_ASSESS") {
                                $chk = $row["PROV_FLG"] == '1' ? ' checked="checked" ' : '';
                                $dis = ' disabled="disabled" ';
                                $row["PROV_FLG"] = knjCreateCheckBox($objForm, "PROV_FLG"."-".$counter, "1", $chk.$dis);
                            }
                        }
                    }
                }

                //３年時のみ相対評価テキストボックスを表示
                if($row["GRADE"]=="03"){

                    //３年時のみ相対評価集計を表示
                    if($row["GRADE3_RELAASSESS_5STEP"]!=""){
                        $grade3_relaassess_5step[]=$row["GRADE3_RELAASSESS_5STEP"];
                    }
                    if($row["GRADE3_RELAASSESS_10STEP"]!=""){
                        $grade3_relaassess_10step[]=$row["GRADE3_RELAASSESS_10STEP"];
                    }

                    $model->step["GRADE3_RELAASSESS_5STEP"."-".$counter] = $row["GRADE3_RELAASSESS_5STEP"];
                    $model->step["GRADE3_RELAASSESS_10STEP"."-".$counter] = $row["GRADE3_RELAASSESS_10STEP"];

                    if(isset($admin_key[90][1])) {    //2004/12/14 arakaki 管理者コントロール制御追加
                        $controlFlg = '1';
                        //5段階評価テキストボックスを作成
                        $objForm->ae( array("type"      => "text",
                                            "name"      => "GRADE3_RELAASSESS_5STEP"."-".$counter,
                                            "size"      => "3",
                                            "maxlength" => "3",
                                            "value"     => $row["GRADE3_RELAASSESS_5STEP"],
                                            "extrahtml" => " onKeyDown=\"keyChangeEntToTab2(this, '{$setTextField}', {$counter})\"; STYLE=\"text-align: right\" onChange=\" selcontrol(this,$counter,$keys);  this.style.background='#ccffcc'\" onblur=\"calc(this);\" onKeyDown=\"keyChangeEntToTab(this)\" onPaste=\"return showPaste(this);\" "));
                        $row["GRADE3_RELAASSESS_5STEP"] = $objForm->ge("GRADE3_RELAASSESS_5STEP"."-".$counter);
                        //貼付対象フラグ
                        knjCreateHidden($objForm, "GRADE3_RELAASSESS_5STEP"."_INPUT"."-".$counter, 1);
                    }

                    if(isset($admin_key[90][2])) {    //2004/12/14 arakaki 管理者コントロール制御追加
                        $controlFlg = '1';
                        //10段階評価テキストボックスを作成
                        $objForm->ae( array("type"      => "text",
                                            "name"      => "GRADE3_RELAASSESS_10STEP"."-".$counter,
                                            "size"      => "3",
                                            "maxlength" => "3",
                                            "value"     => $row["GRADE3_RELAASSESS_10STEP"],
                                            "extrahtml" => " onKeyDown=\"keyChangeEntToTab2(this, '{$setTextField}', {$counter})\"; STYLE=\"text-align: right\" onChange=\" selcontrol(this,$counter,$keys); this.style.background='#ccffcc'\" onblur=\"calc(this);\" onKeyDown=\"keyChangeEntToTab(this)\" onPaste=\"return showPaste(this);\" "));
                        $row["GRADE3_RELAASSESS_10STEP"] = $objForm->ge("GRADE3_RELAASSESS_10STEP"."-".$counter);
                        //貼付対象フラグ
                        knjCreateHidden($objForm, "GRADE3_RELAASSESS_10STEP"."_INPUT"."-".$counter, 1);
                    }
                } else {
                    $row["GRADE3_RELAASSESS_5STEP"]  = Null;
                    $row["GRADE3_RELAASSESS_10STEP"] = Null;
                }

                $counter++;
                $Alldata[] = $row;
                //$disabled = "disabled";
            }
            $result->free();
            $arg["data"] = $Alldata;

            knjCreateHidden($objForm, "COUNT", $counter);

/***** 
checkbox(成績入力完了)の要不要の判断基準が無い為
1学期の中間期末、2学期の中間期末、3学期の期末を固定で表示する
******/

        //初期化
        $j = 1;
        $chkDbValue = array();
        $chngChk = array( "0" => "",
                          "1" => "checked",
                          "" => "" );

        if($model->field["CHK_FLG"] == "reset")
        {
            #echo "Do Reset !<BR>";

            $chkDbValue = array( "10101" => null,
                                 "10201" => null,
                                 "20101" => null,
                                 "20201" => null,
                                 "30201" => null  );

            $ableflg["10101"] = (isset($admin_key[1][1]))? "" : "disabled" ;
            $ableflg["10201"] = (isset($admin_key[1][2]))? "" : "disabled" ;
            $ableflg["20101"] = (isset($admin_key[2][1]))? "" : "disabled" ;
            $ableflg["20201"] = (isset($admin_key[2][2]))? "" : "disabled" ;
            $ableflg["30201"] = (isset($admin_key[3][2]))? "" : "disabled" ;

            $model->field["CHK_FLG"] = "";


            //データ取得
            $query = knjd121kQuery::getSchChrTestData($model->field["CHAIRCD"]);
            #var_dump($query);
            $result3 = $db->query($query);
            while( $row = $result3->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $ExecutedValue = ($row["EXECUTED"] == "1")? 1 : 0;
                $StartchkDbValue = (is_null($chkDbValue[$row["TESTKIND"]]))? 1 : $chkDbValue[$row["TESTKIND"]];
                $chkDbValue[$row["TESTKIND"]] = $StartchkDbValue * $ExecutedValue;
            }
            $result3->free();

        }else{
            #echo "Default <BR>";

            for($k=1;$k<=5;$k++)
            {
                $chkDbValue[$k] = ($model->field["CHK_COMP".$i] == "on" )? "1" : "0";
            }
        }
        //チェックボックス作成
        foreach ($chkDbValue as $key => $value) 
        {
            $objForm->ae( array("type"     => "checkbox",
                                "name"     => "CHK_COMP".$j,
                                "value"    => "on",
                                "extrahtml"=> $chngChk["$value"]." onClick=\"clickedChkBox();\" ".$ableflg[$key]." " )
                         );

            $arg["CHK_COMP".$j] = $objForm->ge("CHK_COMP".$j);
            $j++;
        }

        //変更保存用hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "CHK_FLG",
                            "value"     => $model->field["CHK_FLG"]
                            ) );

        Query::dbCheckIn($db);

        //学期ごとの集計
        for($i=1;$i<=get_count($control_name);$i++){
          for($ii=1;$ii<=3;$ii++){
            if(isset($term_data[$control_name[$i][$ii]])){
              //合計
              $arg[$control_name[$i][$ii]."_SUM"]=array_sum($term_data[$control_name[$i][$ii]]);
              //平均
              ##$arg[$control_name[$i][$ii]."_AVG"]=round((array_sum($term_data[$control_name[$i][$ii]])/get_count($term_data[$control_name[$i][$ii]])));   2004/11/22 arakaki 近大-作業依頼書20041122-01.doc
              $arg[$control_name[$i][$ii]."_AVG"]=round((array_sum($term_data[$control_name[$i][$ii]])/get_count($term_data[$control_name[$i][$ii]])),1);
              //最高点と最低点を求める
              array_multisort ($term_data[$control_name[$i][$ii]], SORT_NUMERIC);
              $max = get_count($term_data[$control_name[$i][$ii]])-1;
              //最高点
              $arg[$control_name[$i][$ii]."_MAX"]=$term_data[$control_name[$i][$ii]][$max];
              //最低点
              $arg[$control_name[$i][$ii]."_MIN"]=$term_data[$control_name[$i][$ii]][0];
            }
          }
        }
        for($i=0;$i<get_count($grade3_relaassess_5step);$i++){
            if($grade3_relaassess_5step[$i]){
              //合計値
              $arg["GRADE3_RELAASSESS_5STEP_SUM"]=array_sum($grade3_relaassess_5step);
              //平均値
              $arg["GRADE3_RELAASSESS_5STEP_AVG"]=round(array_sum($grade3_relaassess_5step)/get_count($grade3_relaassess_5step),1);
              //最高値と最低値を求める
              array_multisort ($grade3_relaassess_5step, SORT_NUMERIC);
              $max = get_count($grade3_relaassess_5step)-1;
              //最高値
              $arg["GRADE3_RELAASSESS_5STEP_MAX"]=$grade3_relaassess_5step[$max];
              //最低値
              $arg["GRADE3_RELAASSESS_5STEP_MIN"]=$grade3_relaassess_5step[0];
            }
        }
        for($i=0;$i<get_count($grade3_relaassess_10step);$i++){
            if($grade3_relaassess_10step[$i]){
              //合計値
              $arg["GRADE3_RELAASSESS_10STEP_SUM"]=array_sum($grade3_relaassess_10step);
              //平均値
              $arg["GRADE3_RELAASSESS_10STEP_AVG"]=round(array_sum($grade3_relaassess_10step)/get_count($grade3_relaassess_10step),1);
              //最高値と最低値を求める
              array_multisort ($grade3_relaassess_10step, SORT_NUMERIC);
              $max = get_count($grade3_relaassess_10step)-1;
              //最高値
              $arg["GRADE3_RELAASSESS_10STEP_MAX"]=$grade3_relaassess_10step[$max];
              //最低値
              $arg["GRADE3_RELAASSESS_10STEP_MIN"]=$grade3_relaassess_10step[0];
            }
        }

        //ＹＥＳボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_udpate",
                            "value"       => "ＹＥＳ",
                            "extrahtml"   => $disabled ." onclick=\"return btn_submit('update');\"" ) );

        $arg["btn_update"] = $objForm->ge("btn_udpate");

        //ＮＯボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_can",
                            "value"       => "ＮＯ",
                            "extrahtml"   => $disabled ." onclick=\"return btn_submit('cancel');\"" ) );

        $arg["btn_can"] = $objForm->ge("btn_can");

        //終了ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $arg["btn_end"] = $objForm->ge("btn_end");

        //印刷ボタンを作成する      //add  04/12/07  yamauchi 
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_print",
                            "value"       => "印 刷",
                            "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) );

        $arg["btn_print"] = $objForm->ge("btn_print");


        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );


        //現在年度
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "YEAR",
                            "value"     => CTRL_YEAR
                            ) );

        //現在学期
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SEMESTER",
                            "value"     => CTRL_SEMESTER
                            ) );

        //プログラムＩＤ
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PRGID",
                            "value"     => "KNJD121"
                            ) );

        //ＤＢ名
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "DBNAME",
                            "value"      => DB_DATABASE
                            ) );

        //選択
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "ELECTDIV",
                            "value"     => $electdiv
                            ) );
                            
        //教育課程コード
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        //hidden
        knjCreateHidden($objForm, "H_SUBCLASSCD");
        //hidden
        knjCreateHidden($objForm, "H_CHAIRCD");        
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);

        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $setNameCd = "Z".SCHOOLKIND."09";
        }
        knjCreateHidden($objForm, "Z009", $setNameCd);

        $arg["finish"]  = $objForm->get_finish();

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();        
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。

        View::toHTML($model, "knjd121kForm1.html", $arg);
    }
}
?>
