<?php

require_once('for_php7.php');

class knjd126oForm1 {
    function main(&$model) {
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("form1", "POST", "knjd126oindex.php", "", "form1");

        /* Add by Kaung for PC-Talker 2020-01-10 start */
        $arg["TITLE"] = "学年別観点入力のマウス入力画面";
        /* Add by Kaung for PC-Talker 2020-01-17 end */

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //戻るボタン実行後 生徒未選択状態に戻す
        if($model->cmd == "back"){
            $model->field["SCHREGNO"] = '';
        }
        if($model->field["SCHREGNO"] == ''){
            $model->field["GRADE_EVALUATION"] = '';
            $model->field["SUBCLASSCD_EVALUATION"] = '';
        }

        if ($model->Properties["use_prg_schoolkind"] == "1") {
            //校種一覧取得
            $schoolkind = $db->getCol(knjd126oQuery::getSchoolKindList($model, "cnt"));
            if (get_count($schoolkind) > 1) {
                //校種コンボ
                $query = knjd126oQuery::getSchoolKindList($model, "list");
                /* Edit by Kaung for PC-Talker 2020-01-10 start */
                $extra = "aria-label=\"校種の\" id = \"SCHOOL_KIND\" onChange=\"current_cursor('SCHOOL_KIND');btn_submit('form1')\";";
                /* Edit by Kaung for PC-Talker 2020-01-17 end */
                makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $model->field["SCHOOL_KIND"], $extra, 1);
                knjCreateHidden($objForm, "H_SCHOOL_KIND");
                $arg["useSchoolKindCmb"] = 1;
            } else {
                $model->field["SCHOOL_KIND"] = $schoolkind[0];
                knjCreateHidden($objForm, "SCHOOL_KIND", $schoolkind[0]);
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $model->field["SCHOOL_KIND"] = SCHOOLKIND;
            knjCreateHidden($objForm, "SCHOOL_KIND", SCHOOLKIND);
        } else {
            knjCreateHidden($objForm, "SCHOOL_KIND");
        }

        $query = knjd126oQuery::getPrintSemester($model);
        /* Edit by Kaung for PC-Talker 2020-01-10 start */
        $extra = "aria-label=\"学期の\" id = \"SEMESTER\" onchange=\"current_cursor('SEMESTER');return btn_submit('form1')\"";
        /* Edit by Kaung for PC-Talker 2020-01-17 end */
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);
        //hidden
        knjCreateHidden($objForm, "H_SEMESTER");

        //学期(観点データ以外用)
        $model->field["SEMESTER2"] = ($model->field["SEMESTER"] == 9) ? CTRL_SEMESTER : $model->field["SEMESTER"];

        //年組コンボ作成
        $query = knjd126oQuery::getHrClass($model);
        /* Edit by Kaung for PC-Talker 2020-01-10 start */
        $extra = "aria-label=\"年組の\" id = \"GRADE_HR_CLASS\" onchange=\"current_cursor('GRADE_HR_CLASS');return btn_submit('form1')\"";
        /* Edit by Kaung for PC-Talker 2020-01-17 end */
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1, "blank");
        //hidden
        knjCreateHidden($objForm, "H_GRADE_HR_CLASS");

        //科目コンボ作成
        $query = knjd126oQuery::getSubclassMst($model->field["GRADE_HR_CLASS"], $model);
        /* Edit by Kaung for PC-Talker 2020-01-10 start */
        $extra = "aria-label=\"科目の\" id = \"SUBCLASSCD\" onchange=\"current_cursor('SUBCLASSCD');return btn_submit('form1')\"";
        /* Edit by Kaung for PC-Talker 2020-01-17 end */
        makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $model->field["SUBCLASSCD"], $extra, 1, "blank");
        //hidden
        knjCreateHidden($objForm, "H_SUBCLASSCD");

        //評価する学年 0.非表示/1.表示
        $arg["useEvaluationCmb"] = 0;
        if($model->field["SCHREGNO"]){
            $schRow = $db->getRow(knjd126oQuery::getSchRow($model), DB_FETCHMODE_ASSOC);
        
            $model->field["GRADE_EVALUATION"] = ($model->cmd == "chgGrd1" || $model->cmd == "chgSub1") ? $model->field["GRADE_EVALUATION"] : $schRow["GRADE_EVALUATION"];
            $arg["useEvaluationCmb"] = 1;
            //学年コンボ作成(評価する学年)
            $query = knjd126oQuery::getHrClassEvaluation($model);
            /* Edit by Kaung for PC-Talker 2020-01-10 start */
            $extra = "aria-label=\"年組の\" id = \"GRADE_EVALUATION\" onchange=\"current_cursor('GRADE_EVALUATION');return btn_submit('chgGrd1')\"";
            /* Edit by Kaung for PC-Talker 2020-01-17 end */
            makeCmb($objForm, $arg, $db, $query, "GRADE_EVALUATION", $model->field["GRADE_EVALUATION"], $extra, 1, "blank");
            //hidden
            knjCreateHidden($objForm, "H_GRADE_EVALUATION");

            //科目コンボ作成(評価する学年)
            $query = knjd126oQuery::getSubclassMst($model->field["GRADE_EVALUATION"], $model);
            if ($model->cmd == "chgGrd1") {
                $model->field["SUBCLASSCD_EVALUATION"] = "";
            } else if ($model->cmd == "chgSub1") {
                $model->field["SUBCLASSCD_EVALUATION"] = $model->field["SUBCLASSCD_EVALUATION"];
            } else {
                $model->field["SUBCLASSCD_EVALUATION"] = $schRow["SUBCLASSCD_EVALUATION"];
            }
            /* Edit by Kaung for PC-Talker 2020-01-10 start */
            $extra = "aria-label=\"科目の\" id = \"SUBCLASSCD_EVALUATION\" onchange=\"current_cursor('SUBCLASSCD_EVALUATION');return btn_submit('chgSub1')\"";
            /* Edit by Kaung for PC-Talker 2020-01-17 end */
            makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD_EVALUATION", $model->field["SUBCLASSCD_EVALUATION"], $extra, 1, "blank");
            //hidden
            knjCreateHidden($objForm, "H_SUBCLASSCD_EVALUATION");
        }

        $model->HyoukaCount = 0;
        if($model->field["SCHOOL_KIND"] == 'P'){
            //データ入力選択ラジオボタン変更(小学部のみ)
            $model->HyoukaCount = $db->getOne(knjd126oQuery::getKantenHyoukaCount($model));
        }

        //入力選択ラジオボタン 1:マウス入力 2:手入力
        $opt_select = array(1, 2);
        $model->select = ($model->select == "") ? "1" : $model->select;
        /* Edit by Kaung for PC-Talker 2020-01-10 start */
        $extra = array("id=\"SELECT1\" onclick =\"current_cursor('SELECT1');return btn_submit('select1');\"", "id=\"SELECT2\" onclick =\"current_cursor('SELECT2');return btn_submit('select2');\"");
        /* Edit by Kaung for PC-Talker 2020-01-17 end */
        $radioArray = knjCreateRadio($objForm, "SELECT", $model->select, $extra, $opt_select, get_count($opt_select));
        foreach($radioArray as $key => $val) $arg[$key] = $val;

        //入力選択ラジオボタン 1:値選択 2:データクリア
        $opt_nyuryoku = array(1, 2);
        $model->nyuryoku = ($model->nyuryoku == "") ? "1" : $model->nyuryoku;
        $extra = array("id=\"NYURYOKU1\" onClick=\"myHidden()\"", "id=\"NYURYOKU2\" onClick=\"myHidden()\"");
        $radioArray = knjCreateRadio($objForm, "NYURYOKU", $model->nyuryoku, $extra, $opt_nyuryoku, get_count($opt_nyuryoku));
        foreach($radioArray as $key => $val) $arg[$key] = $val;

        //入力値選択ラジオボタン
        $query = knjd126oQuery::getKantenHyouka($model);
        $result = $db->query($query);
        $setumeiArr = array();
        $opt_data = array();
        $kantenCnt = 1;
        $extra = array();
        $komoji = 0;
        $oomoji = 0;
        $model->nonVisualViewCd = "";
        $useJviewStatus_NotHyoji_check = '0';
        while ($kanten = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //プロパティにセットされているコードは表示しない
            if (($model->field["SEMESTER"] == "9" && $model->Properties["useJviewStatus_NotHyoji_D028"] == $kanten["NAMECD2"]) ||
                ($model->field["SEMESTER"] != "9" && $model->Properties["useJviewStatus_NotHyoji_D029"] == $kanten["NAMECD2"])) {
                $model->nonVisualViewCd = $kanten["ABBV1"];
                $useJviewStatus_NotHyoji_check = '1';
            } else {
                $kantenArray[$kantenCnt]["VAL"] = $kanten["ABBV1"];
                if (preg_match("/^[a-z]+$/", $kanten["ABBV1"])) {
                    $komoji++;
                } else {
                    $oomoji++;
                }
                $kantenArray[$kantenCnt]["SHOW"] = $kanten["NAME1"];
                $opt_data[] = $kantenCnt;
                $radioname = $kanten["NAME1"];
                $extra[] = "aria-label=\"値選択の".$radioname."\" id=\"TYPE_DIV{$kantenCnt}\"";
                $arg["TYPE_SHOW{$kantenCnt}"] = $kanten["NAME1"];
                $kantenCnt++;
            }
            $setumeiArr[] = $kanten["NAME1"].":".$kanten["NAMESPARE1"];
        }
        $result->free();
        if ($useJviewStatus_NotHyoji_check == '1') {
            $oomoji++;
            $kantenArray[$kantenCnt]["VAL"] = "F";
            $kantenArray[$kantenCnt]["SHOW"] = "F";
            $opt_data[] = $kantenCnt;
            $extra[] = "aria-label=\"値選択のF\" id=\"TYPE_DIV{$kantenCnt}\"";
            $arg["TYPE_SHOW{$kantenCnt}"] = "F";
        }
        $model->type_div = ($model->type_div == "") ? "1" : $model->type_div;
        $radioArray = knjCreateRadio($objForm, "TYPE_DIV", $model->type_div, $extra, $opt_data, get_count($opt_data));
        foreach($radioArray as $key => $val) $arg[$key] = $val;

        if (get_count($kantenArray) == 0) {
            if ($model->field["SEMESTER"] === '9') {
                $arg["close_win"] = "close_window1();";
            } else {
                $arg["close_win"] = "close_window2();";
            }
        }

        //説明文言
        $setumeiStr = ($model->field["SCHOOL_KIND"] == "P") ? "小学部:" : "中学部・高等部:" ;
        $setumeiStr .= "（";
        $setumeiStr .= implode("　", $setumeiArr);
        $setumeiStr .= "）";
        $arg["STATUS_TAIOU"] = $setumeiStr;

        //管理者コントロール
        $admin_key = array();
        $result = $db->query(knjd126oQuery::getAdminContol($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $admin_key[] = $row["CONTROL_CODE"];
        }
        if (in_array($model->field["SEMESTER"], $admin_key) && DEF_UPDATE_RESTRICT <= AUTHORITY) {
            $adminInputFlg = true;
        } else {
            $adminInputFlg = false;
        }
        $result->free();

        //観点コード(MAX5または6)
        $view_key = array();
        $view_cnt = 0;
        $view_html = "";
        if ($model->Properties["kantenHyouji"] !== '6') {
            $arg["kantenHyouji_5"] = "1";
            $view_html_no = array("1" => "①","2" => "②","3" => "③","4" => "④","5" => "⑤");
            $result = $db->query(knjd126oQuery::selectViewcdQuery($model));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $view_cnt++;
                if ($view_cnt > 5) break;   //MAX5
                $view_key[$view_cnt] = $row["VIEWCD"];  //1,2,3,4,5
                //チップヘルプ
                $view_html .= "<th width=\"60\" onMouseOver=\"ViewcdMousein(event, ".$view_cnt.")\" onMouseOut=\"ViewcdMouseout()\">".$view_html_no[$view_cnt]."</th>";
                knjCreateHidden($objForm, "VIEWCD".$view_cnt, $row["VIEWNAME"]);
            }
            $result->free();

            for ($i=0; $i<(5-get_count($view_key)); $i++) $view_html .= "<th width=\"60\">&nbsp;</th>";
        } else {
            $arg["kantenHyouji_6"] = "1";
            $view_html_no = array("1" => "①","2" => "②","3" => "③","4" => "④","5" => "⑤","6" => "⑥");
            $result = $db->query(knjd126oQuery::selectViewcdQuery($model));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $view_cnt++;
                if ($view_cnt > 6) break;   //MAX6
                $view_key[$view_cnt] = $row["VIEWCD"];  //1,2,3,4,5,6
                //チップヘルプ
                $view_html .= "<th width=\"60\" onMouseOver=\"ViewcdMousein(event, ".$view_cnt.")\" onMouseOut=\"ViewcdMouseout()\">".$view_html_no[$view_cnt]."</th>";
                knjCreateHidden($objForm, "VIEWCD".$view_cnt, $row["VIEWNAME"]);
            }
            $result->free();

            for ($i=0; $i<(6-get_count($view_key)); $i++) $view_html .= "<th width=\"60\">&nbsp;</th>";
        }
        $arg["view_html"] = $view_html;
        //評定
        if ($view_cnt > 0) $view_key[8] = "";
        //学習内容と様子用観点コード
        if ($view_cnt > 0) $view_key[9] = "";

        //選択教科
        $electdiv = $db->getrow(knjd126oQuery::getClassMst($model->field["SUBCLASSCD"], $model->field["GRADE_HR_CLASS"], $model), DB_FETCHMODE_ASSOC);

        //選択教科と同様に英字で入力する科目コード取得
        if ($db->getOne(knjd126oQuery::getNameMstD065($model)) > 0) {
            $electdiv["ELECTDIV"] = 1;
        }

        //データ取得(JVIEWSTAT_RECORD_DAT)
        $arrJviewSchNo = array();
        $result = $db->query(knjd126oQuery::getJviewstatRecordDat($model, $view_key));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $arrJviewSchNo[] = $row["SCHREGNO"];
        }

        //項目名セット
        $model->setmojicnt = 15;
        $model->setGyou = 8;
        $arg["SUBJECT"] = "所見";
        if( in_array($model->field["SEMESTER"], $admin_key) && DEF_UPDATE_RESTRICT <= AUTHORITY) {
            if ($model->setGyou > 0 && get_count($view_key) > 0) {
                $arg["SUBJECT"] .= "<br><font size=2>(全角{$model->setmojicnt}文字X{$model->setGyou}行まで)</font>";
            }
        }
        knjCreateHidden($objForm, "SET_STATUS9_MOJICNT", $model->setmojicnt);
        knjCreateHidden($objForm, "SET_STATUS9_GYOU", $model->setGyou);

        //初期化
        $model->data = array();
        $counter = 0;
        $disable = "disabled";

        if ($model->field["SCHOOL_KIND"] != "P") {
            // 評価・評定の文言
            $arg["HyoukaHyouteiWord"] = "評定";
        }

        //生徒コンボ作成
        $query = knjd126oQuery::selectQuery($model, $view_key, true);
        /* Edit by Kaung for PC-Talker 2020-01-10 start */
        $extra = "aria-label=\"科目の\" id = \"SCHREGNO\" onchange=\"current_cursor('SCHREGNO');return btn_submit('form1')\"";
        /* Edit by Kaung for PC-Talker 2020-01-17 end */
        makeCmb($objForm, $arg, $db, $query, "SCHREGNO", $model->field["SCHREGNO"], $extra, 1, "blank");
        //hidden
        knjCreateHidden($objForm, "H_SCHREGNO");

        //一覧表示
        $code8InputFlg = "0";
        $result = $db->query(knjd126oQuery::selectQuery($model, $view_key, false));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //学籍番号を配列で取得
            $model->data["SCHREGNO"][] = $row["SCHREGNO"];

            //クラス-出席番(表示)
            $row["ATTENDNO"] = $row["HR_NAME"]."-".$row["ATTENDNO"]."番";

            //名前
            $row["NAME_SHOW"] = $row["SCHREGNO"]." ".$row["NAME_SHOW"];
            /* Add by Kaung for PC-Talker 2020-01-10 start */
            $inputname = $row["NAME_SHOW"];
            /* Add by Kaung for PC-Talker 2020-01-17 end */

            if (empty($model->field["SCHREGNO"]) && $model->field["SUBCLASSCD"] != $row["TARGET_SUBCLASSCD"]) {
                //リンク
                $hash = array("cmd"                     => "form1",
                              "SEMESTER"                => $model->field["SEMESTER"],
                              "SEMESTER2"               => $model->field["SEMESTER2"],
                              "GRADE_HR_CLASS"          => $model->field["GRADE_HR_CLASS"],
                              "SCHOOL_KIND"             => $model->field["SCHOOL_KIND"],
                              "SCHREGNO"                => $row["SCHREGNO"],
                              "SUBCLASSCD"              => $model->field["SUBCLASSCD"],
                              "GRADE_EVALUATION"        => $row["TARGET_GRADE"],
                              "SUBCLASSCD_EVALUATION"   => $row["TARGET_SUBCLASSCD"],
                              "TIPE_DIV"                => $model->type_div
                              );
                $row["LINK"] = View::alink("knjd126oindex.php", "下学年適用", "target=_self tabindex=\"-1\"", $hash);
            }

            //異動情報
            $query = knjd126oQuery::getTransfer($model, $row["SCHREGNO"]);
            $transCnt = $db->getOne($query);
            $setColor = "#ffffff";
            if ($transCnt > 0) {
                $setColor = "#ffff00";
            }
            if ($model->field["SUBCLASSCD_EVALUATION"]) {
                $targetGrade = $model->field["GRADE_EVALUATION"];
            } else if ($row["TARGET_GRADE"]) {
                $targetGrade = $row["TARGET_GRADE"];
            } else {
                $targetGrade = substr($model->field["GRADE_HR_CLASS"], 0, 2);
            }
            $model->data[$row["SCHREGNO"]]["TARGET_GRADE"]          = $targetGrade;
            $yomikaeFlg     = $row["TARGET_SUBCLASSCD"]  && $row["TARGET_SUBCLASSCD"] != $row["SUBCLASSCD"];
            if ($model->field["SUBCLASSCD_EVALUATION"]) {
                $targetSubclass = $model->field["SUBCLASSCD_EVALUATION"];
            } else if ($yomikaeFlg) {
                $targetSubclass = $row["TARGET_SUBCLASSCD"];
            } else {
                $targetSubclass = $model->field["SUBCLASSCD"];
            }
            $model->data[$row["SCHREGNO"]]["TARGET_TOTAL_SUBCLASSCD"]  = $targetSubclass;

            $rtn = $db->getOne(knjd126oQuery::getSchJviewCd($model, $targetGrade, $targetSubclass)); //対象学年・対象科目における観点
            $schJviewArray = explode(",", $rtn);
            $rtn2  = explode(",", $row["VIEW_KEY_STATUS"]); //対象科目の評価の配列
            $statusData = array();
            foreach ($rtn2 as $val) {
                list($viewcd, $status) = explode('-', $val);
                $statusData[$viewcd]["STATUS"] = $status;
            }
            $jviewData = array();
            for($code = 1; $code <= get_count($schJviewArray); $code++) {
                $viewcd = $schJviewArray[$code - 1];
                $jviewData[$code]["VIEWCD"] = $viewcd;
                $jviewData[$code]["STATUS"] = isset($statusData[$viewcd]["STATUS"]) ? $statusData[$viewcd]["STATUS"] : "";
            }

            foreach ($jviewData as $code => $viewcdAndStatus) {
                $viewcd = $viewcdAndStatus["VIEWCD"];
                $status = $viewcdAndStatus["STATUS"];

                if (($model->field["SEMESTER"] == "9" && $model->Properties["useJviewStatus_NotHyoji_D028"]) || 
                    ($model->field["SEMESTER"] != "9" && $model->Properties["useJviewStatus_NotHyoji_D029"])) {
                    if (in_array($row["SCHREGNO"], $arrJviewSchNo)) {
                        //JVIEWSTAT_RECORD_DATにデータがある人のみブランクを"F"に切り替え
                        $status = ($row["STATUS".$code] == "") ? "F": $status;
                    }
                    //プロパティにセットされているコードは表示しない
                    $status = ($status == $model->nonVisualViewCd) ? "": $status;
                }

                //管理者コントロール
                if($viewcd != "" && $adminInputFlg) {
                    //各観点コードを取得
                    $target_subclass = $db->getOne(knjd126oQuery::getTargetsubclasscd($model, $row["SCHREGNO"], $code));
                    $disabledFlg = ($model->field["SUBCLASSCD_EVALUATION"] == "" && $model->field["SUBCLASSCD"] != $targetSubclass) ? true : false;
                    $disCourse = ($disabledFlg)  ? "disabled" : "";

                    $model->data[$row["SCHREGNO"]]["STATUS"][$code] = $viewcd;
                    /* Edit by Kaung for PC-Talker 2020-01-10 start */
                    $extra = "aria-label=\"".$inputname."の観点別学習状況の".$code."\" STYLE=\"text-align: center\" readonly=\"readonly\" onClick=\"kirikae(this, 'STATUS".$code."-".$counter."')\" oncontextmenu=\"kirikae2(this, 'STATUS".$code."-".$counter."')\"; ";
                    /* Edit by Kaung for PC-Talker 2020-01-17 end */
                    $row["STATUS".$code] = knjCreateTextBox($objForm, $status, "STATUS".$code."-".$counter, 3, 1, $extra.$disCourse);
                    if ($disabledFlg) {
                        knjCreateHidden($objForm, "STATUS".$code."-".$counter, $status);
                    }
                    //更新ボタンのＯＮ／ＯＦＦ
                    $disable = "";

                //ラベルのみ
                } else {
                    $row["STATUS".$code] = "<font color=\"#000000\">".$status."</font>";
                }
            }

            //評定＆所見
            if($viewcd != "" && $adminInputFlg) {
                $arg["DISP_STATUS8"] = "1";
                if ($model->field["SCHOOL_KIND"] != "P") {
                    //評定
                    $row["STATUS8"] = isset($model->warning) ? $model->fields["STATUS8"][$counter]: $row["STATUS8"];
                    if ($model->field["SUBCLASSCD_EVALUATION"]){
                        $row["STATUS8"] = $db->getone(knjd126oQuery::getRowStatus($model, "8"));
                    }
                    if ($model->Properties["useHyoukaHyouteiFlg"] == "1"){
                        /* Edit by Kaung for PC-Talker 2020-01-10 start */
                        $extra = " aria-label=\"".$inputname."の評価\" onChange=\"tmpSet(this);\" ";
                        /* Edit by Kaung for PC-Talker 2020-01-17 end */
                    } else if ($electdiv["ELECTDIV"] != 0) {
                        /* Add by Kaung for PC-Talker 2020-01-10 start */
                        $extra = " aria-label=\"".$inputname."の評価\" onChange=\"this.style.background='#ccffcc'\"";
                        /* Add by Kaung for PC-Talker 2020-01-17 end */
                        $status8 = $row["STATUS8"];
                        if ($status == "11") $status = "A";
                        if ($status == "22") $status = "B";
                        if ($status == "33") $status = "C";
                        $row["STATUS8"] = $status8;
                    }
                    $row["STATUS8"] = knjCreateTextBox($objForm, $row["STATUS8"], "STATUS8"."-".$counter, 3, 3, $extra);
                    $code8InputFlg = "1";
                } else {
                        $arg["DISP_STATUS8"] = "";
                }

                //所見
                $row["STATUS9"] = isset($model->warning) ? $model->fields["STATUS9"][$counter] : $row["STATUS9"];
                if ($model->field["SUBCLASSCD_EVALUATION"]){
                    $row["STATUS9"] = $db->getone(knjd126oQuery::getRowStatus($model, "9"));
                }
                /* Add out by Kaung for PC-Talker 2020-01-10 start */
                $extra = " aria-label=\"".$inputname."の学習内容と様子(全角15文字X8行まで)\" onChange=\"this.style.background='#ccffcc'\"";
                /* Add out by Kaung for PC-Talker 2020-01-17 end */
                $row["STATUS9"] = knjCreateTextArea($objForm, "STATUS9"."-".$counter, $model->setGyou, ($model->setmojicnt * 2) + 1, "soft", $extra, $row["STATUS9"]);

            }

            $row["COLOR"] = $setColor;

            $counter++;
            $arg["data"][] = $row;
        }

        $result->free();

        $dataArray = array();
        foreach ($kantenArray as $key => $val) {
            $dataArray[] = array("VAL"  => "\"javascript:setClickValue('".$key."')\"",
                                 "NAME" => $val["VAL"]);
        }

        $arg["menuTitle"]["CLICK_NAME"] = knjCreateBtn($objForm, "btn_end", "×", "onclick=\"return setClickValue('999');\"");
        $arg["menuTitle"]["CLICK_VAL"] = "javascript:setClickValue('999')";
        foreach ($dataArray as $key => $val) {
            $setData["CLICK_NAME"] = $val["NAME"];
            $setData["CLICK_VAL"] = $val["VAL"];
            $arg["menu"][] = $setData;
        }

        //更新ボタン
        $disable = (AUTHORITY == DEF_UPDATABLE || AUTHORITY == DEF_UPDATE_RESTRICT) ? $disable : "disabled";
        /* Edit by Kaung for PC-Talker 2020-01-10 start */
        $extra = "aria-label = \"更新\" id = \"btn_update\" onclick=\"current_cursor('btn_update');return btn_submit('update', '".$electdiv["ELECTDIV"]."');\"".$disable;
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //取消ボタン
        $extra = "aria-label = \"取消\" id = \"btn_reset\" onclick=\"current_cursor('btn_reset');return btn_submit('reset');\"".$disable;
        $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //戻るボタン
        $extra = "aria-label = \"戻る\" id = \"btn_back\" onclick=\"current_cursor('btn_back');return btn_submit('back');\"";
        $arg["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);
        //削除ボタン
        $extra = "aria-label = \"削除\" id = \"btn_delete\" onclick=\"current_cursor('btn_delete');return btn_submit('delete');\"";
        $arg["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra);
        //終了ボタン
        $extra = "aria-label = \"終了\" id = \"btn_end\" onclick=\"current_cursor('btn_end');closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
        /* Edit by Kaung for PC-Talker 2020-01-17 end */

        //hidden作成
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "PRGID", "KNJD126O");
        knjCreateHidden($objForm, "CTRL_Y", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_S", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_D", CTRL_DATE);
        knjCreateHidden($objForm, "useTestCountflg", $model->Properties["useTestCountflg"]);
        knjCreateHidden($objForm, "CODE8INPUT", $code8InputFlg);
        knjCreateHidden($objForm, "ELECTDIV", $electdiv["ELECTDIV"]);

        $hiddenSetVal = "";
        $hiddenSetShow = "";
        $hiddenSetCheck = "";
        $sep = "";
        foreach ($kantenArray as $key => $val) {
            $hiddenSetVal .= $sep.$key;
            $hiddenSetShow .= $sep.$val["VAL"];
            $sep = ",";
        }
        knjCreateHidden($objForm, "SETVAL", $hiddenSetVal);
        knjCreateHidden($objForm, "SETSHOW", $hiddenSetShow);
        if ($komoji > 0 && $oomoji > 0) {
            $setHenkan = "3";
        } else if ($komoji > 0) {
            $setHenkan = "2";
        } else {
            $setHenkan = "1";
        }
        knjCreateHidden($objForm, "HENKAN_TYPE", $setHenkan);
        //教育課程コード
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        //観点数設定
        knjCreateHidden($objForm, "kantenHyouji", $model->Properties["kantenHyouji"]);
        knjCreateHidden($objForm, "kantenHyouji_5", $arg["kantenHyouji_5"]);
        knjCreateHidden($objForm, "kantenHyouji_6", $arg["kantenHyouji_6"]);
        //教科コード
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass = explode("-", $model->field["SUBCLASSCD"]);
            knjCreateHidden($objForm, "CLASSCD", $subclass[0].'-'.$subclass[1]);
        } else {
            knjCreateHidden($objForm, "CLASSCD", substr($model->field["SUBCLASSCD"], 0, 2));
        }
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
        knjCreateHidden($objForm, "Z009", $setNameCd);
        knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
        knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);

        if ($model->field["SEMESTER"] == "9") {
            knjCreateHidden($objForm, "useJviewStatus_NotHyoji", $model->Properties["useJviewStatus_NotHyoji_D028"]);
        } else {
            knjCreateHidden($objForm, "useJviewStatus_NotHyoji", $model->Properties["useJviewStatus_NotHyoji_D029"]);
        }
        knjCreateHidden($objForm, "displayHyoutei", $model->Properties["displayHyoutei"]);
        knjCreateHidden($objForm, "useHyoukaHyouteiFlg", $model->Properties["useHyoukaHyouteiFlg"]);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd126oForm1.html", $arg);
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if($blank) $opt[] = array('label' => "", 'value' => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    if($name == "SEMESTER"){
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
