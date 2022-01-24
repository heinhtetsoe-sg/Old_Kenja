<?php

require_once('for_php7.php');

class knje360SubForm2
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform2", "POST", "knje360index.php", "", "subform2");

        //DB接続
        $db = Query::dbCheckOut();

        // 入試カレンダーの使用フラグ
        $arg["useCollegeExamCalendar"] = "";
        if ($model->Properties["useCollegeExamCalendar"] === '1') {
            $arg["useCollegeExamCalendar"] = "1";
        }
        knjCreateHidden($objForm, "useCollegeExamCalendar", $arg["useCollegeExamCalendar"]);

        //生徒情報
        $info = $db->getRow(knje360Query::getSchInfo($model), DB_FETCHMODE_ASSOC);
        $ban = ($info["ATTENDNO"]) ? '番　' : '　';
        $arg["SCHINFO"] = $info["HR_NAME"].' '.$info["ATTENDNO"].$ban.$info["NAME_SHOW"];

        unset($model->replace);
        //警告メッセージを表示しない場合
        if (($model->cmd == "subform2A" || $model->cmd == "subform2_clear") && $model->cmd != "subform2_college") {
            if (isset($model->schregno) && isset($model->seq) && !isset($model->warning)) {
                $Row = $db->getRow(knje360Query::getSubQuery2($model), DB_FETCHMODE_ASSOC);
                if ($model->Properties["useAutoSetCollegeNameToThinkExam"] == "1") {
                    //読込み時のデータを保持するため、このタイミングでhidden設定する。
                    knjCreateHidden($objForm, "HID_FSTREAD_SCHOOL_CD", $Row["SCHOOL_CD"]);
                    $college = $db->getRow(knje360Query::getCollegeInfo($Row["SCHOOL_CD"], $Row["FACULTYCD"], $Row["DEPARTMENTCD"], $Row["CAMPUS_ADDR_CD"], $model), DB_FETCHMODE_ASSOC);
                    knjCreateHidden($objForm, "HID_FSTREAD_SCHOOL_NAME", $college["SCHOOL_NAME"]);
                    knjCreateHidden($objForm, "HID_FSTREAD_PLANSTAT", $Row["PLANSTAT"]);
                }
                $arg["NOT_WARNING"] = 1;
            } else {
                $Row =& $model->field;
            }
        } else {
            if ($model->cmd == "subform2B") {
                $arg["NOT_WARNING"] = 1;
                $model->cmd = "subform2";
                unset($model->field);
                unset($model->seq);
            }
            $Row =& $model->field;
            knjCreateHidden($objForm, "HID_FSTREAD_SCHOOL_CD", $model->fstRead["SCHOOL_CD"]);
            knjCreateHidden($objForm, "HID_FSTREAD_SCHOOL_NAME", $model->fstRead["SCHOOL_NAME"]);
            knjCreateHidden($objForm, "HID_FSTREAD_PLANSTAT", $model->fstRead["PLANSTAT"]);
        }

        //登録日
        $Row["TOROKU_DATE"] = ($Row["TOROKU_DATE"] == "") ? str_replace("-", "/", CTRL_DATE) : str_replace("-", "/", $Row["TOROKU_DATE"]);
        $arg["data"]["TOROKU_DATE"] = View::popUpCalendar($objForm, "TOROKU_DATE", $Row["TOROKU_DATE"]);

        //指導要録に表記する進路先（直接入力）
        $extra = "style=\"height:95px;\"";
        $arg["data"]["THINKEXAM"] = knjCreateTextArea($objForm, "THINKEXAM", 6, 51, "soft", $extra, $Row["THINKEXAM"]);

        if ($model->Properties["useCollegeSearch10Keta"] == "1") {
            //学校コード10桁
            $extra = "onblur=\"this.value=toInteger(this.value);\" onKeyDown=\"keyChangeEntToTab(this, 'SCHOOL_CD')\"";
            $arg["data"]["SEARCH10"] = knjCreateTextBox($objForm, $Row["SEARCH10"], "SEARCH10", 10, 10, $extra);
            //確定ボタン
            $extra = " onclick=\"collegeSelectEvent10();\"";
            $arg["button"]["btn_kakutei10"] = knjCreateBtn($objForm, "btn_kakutei10", "確 定", $extra);
        }

        //学校コードテキストボックス
        $extra = "onblur=\"this.value=toInteger(this.value);\" onKeyDown=\"keyChangeEntToTab(this, 'FACULTYCD')\"";
        $arg["data"]["SCHOOL_CD"] = knjCreateTextBox($objForm, $Row["SCHOOL_CD"], "SCHOOL_CD", 8, 8, $extra);

        //学部コードテキストボックス
        $extra = "onblur=\"this.value=toInteger(this.value);\" onKeyDown=\"keyChangeEntToTab(this, 'DEPARTMENTCD')\"";
        $arg["data"]["FACULTYCD"] = knjCreateTextBox($objForm, $Row["FACULTYCD"], "FACULTYCD", 3, 3, $extra);

        //学科コードテキストボックス
        $extra = "onblur=\"this.value=toInteger(this.value);\" onKeyDown=\"keyChangeEntToTab(this, 'btn_kakutei')\"";
        $arg["data"]["DEPARTMENTCD"] = knjCreateTextBox($objForm, $Row["DEPARTMENTCD"], "DEPARTMENTCD", 3, 3, $extra);

        //学校情報
        $college = $db->getRow(knje360Query::getCollegeInfo($Row["SCHOOL_CD"], $Row["FACULTYCD"], $Row["DEPARTMENTCD"], $Row["CAMPUS_ADDR_CD"], $model), DB_FETCHMODE_ASSOC);

        //学校名
        $arg["data"]["SCHOOL_NAME"] = $college["SCHOOL_NAME"];

        //学部
        $arg["data"]["FACULTYNAME"] = $college["FACULTYNAME"];

        //学科
        $arg["data"]["DEPARTMENTNAME"] = $college["DEPARTMENTNAME"];

        //郵便番号
        $arg["data"]["ZIPCD"] = $college["ZIPCD"];

        //住所
        $arg["data"]["ADDR1"] = $college["ADDR1"];
        $arg["data"]["ADDR2"] = $college["ADDR2"];

        //電話番号
        $arg["data"]["TELNO"] = $college["TELNO"];

        //設置区分
        $arg["data"]["SCHOOL_GROUP_NAME"] = $college["SCHOOL_GROUP_NAME"];

        //本都道府県
        $mainpref = $db->getOne(knje360Query::getMainPref());

        //所在地
        $Row["PREF_CD"] = ($Row["PREF_CD"]) ? $Row["PREF_CD"] : '-';
        $query = knje360Query::getPrefList($mainpref);
        makeCmb($objForm, $arg, $db, $query, "PREF_CD", $Row["PREF_CD"], "", 1, 1);

        //東京都集計用
        knjCreateHidden($objForm, "clicBtn");

        //学校検索後の処理
        if ($model->cmd == "subform2_college") {
            if ($model->Properties["useCollegeExamCalendar"] === '1') {
                //募集区分
                $query = knje360Query::getNameMst('E044');
                $extra = "onchange=\"collegeSelectEvent2();\"";
                $response  = makeCmb2($objForm, $arg, $db, $query, "ADVERTISE_DIV", $model->field["ADVERTISE_DIV"], $extra, 1, 1);

                //日程
                $query = knje360Query::getProgramCd($model->field);
                $extra = "onchange=\"collegeSelectEvent2();\"";
                $response .= "::".makeCmb2($objForm, $arg, $db, $query, "PROGRAM_CD", $model->field["PROGRAM_CD"], $extra, 1, 1);

                //方式
                $query = knje360Query::getFormCd($model->field);
                $extra = "onchange=\"collegeSelectEvent2();\"";
                $response .= "::".makeCmb2($objForm, $arg, $db, $query, "FORM_CD", $model->field["FORM_CD"], $extra, 1, 1);

                //大分類
                $query = knje360Query::getLCd($model->field);
                $extra = "onchange=\"collegeSelectEvent2();\"";
                $response .= "::".makeCmb2($objForm, $arg, $db, $query, "L_CD", $model->field["L_CD"], $extra, 1, 1);

                //小分類
                $query = knje360Query::getSCd($model->field);
                $extra = "onchange=\"collegeSelectEvent2();\"";
                $response .= "::".makeCmb2($objForm, $arg, $db, $query, "S_CD", $model->field["S_CD"], $extra, 1, 1);
            } else {
                $response = "::"."::"."::"."::";
            }

            $query = knje360Query::getLimit($model->field);
            $setLimit = $db->getRow($query, DB_FETCHMODE_ASSOC);
            //締切日（窓口）
            $model->field["LIMIT_DATE_WINDOW"] = makeDate($setLimit["LIMIT_DATE_WINDOW"]);
            $response .= "::".View::popUpCalendar($objForm, "LIMIT_DATE_WINDOW", $model->field["LIMIT_DATE_WINDOW"]);

            //締切日（郵送）
            $model->field["LIMIT_DATE_MAIL"] = makeDate($setLimit["LIMIT_DATE_MAIL"]);
            $response .= "::".View::popUpCalendar($objForm, "LIMIT_DATE_MAIL", $model->field["LIMIT_DATE_MAIL"]);

            //郵送区分
            $opt = array();
            $opt[] = array('label' => "", 'value' => "");
            $opt[] = array('label' => "0：未定・その他", 'value' => "0");
            $opt[] = array('label' => "1：消印有効", 'value' => "1");
            $opt[] = array('label' => "2：必着", 'value' => "2");
            $model->field["LIMIT_MAIL_DIV"] = ($model->field["LIMIT_MAIL_DIV"] == "") ? ($setLimit["LIMIT_MAIL_DIV"] ? $setLimit["LIMIT_MAIL_DIV"] : $opt[0]["value"]) : $model->field["LIMIT_MAIL_DIV"];
            $extra = "";
            $response .= "::".knjCreateCombo($objForm, "LIMIT_MAIL_DIV", $model->field["LIMIT_MAIL_DIV"], $opt, $extra, 1);

            //入試日
            $model->field["STAT_DATE1"] = makeDate($setLimit["EXAM_DATE"]);
            $response .= "::".View::popUpCalendar($objForm, "STAT_DATE1", $model->field["STAT_DATE1"]);

            //合格発表日
            $model->field["STAT_DATE3"] = makeDate($setLimit["EXAM_PASS_DATE"]);
            $response .= "::".View::popUpCalendar($objForm, "STAT_DATE3", $model->field["STAT_DATE3"]);

            //学校名
            $response .= "::".$college["SCHOOL_NAME"];
            //学部
            $response .= "::".$college["FACULTYNAME"];
            //学科
            $response .= "::".$college["DEPARTMENTNAME"];
            //郵便番号
            $response .= "::".$college["ZIPCD"];
            //住所
            $response .= "::".$college["ADDR1"];
            $response .= "::".$college["ADDR2"];
            //電話番号
            $response .= "::".$college["TELNO"];
            //設置区分
            $response .= "::".$college["SCHOOL_GROUP_NAME"];

            //東京都集計用
            if ($model->Properties["useTokyotoShinroTyousasyo"] == "1") {
                //学校区分
                $model->field["SCHOOL_CATEGORY_CD"] = ($model->clicBtn == '1') ? $college["SCHOOL_CATEGORY_CD"]: $model->field["SCHOOL_CATEGORY_CD"];
                if ($college["PROTECTION_FLG"] == '1') {
                    $model->field["SCHOOL_CATEGORY_CD"] = $college["SCHOOL_CATEGORY_CD"];
                }
                $query = knje360Query::getSchoolCategoryCd();
                $extra = "onchange=\"collegeSelectEvent2();\"";
                $response .= "::".makeCmb2($objForm, $arg, $db, $query, "SCHOOL_CATEGORY_CD", $model->field["SCHOOL_CATEGORY_CD"], $extra, 1, 1);

                //大分類
                $model->field["TOKYO_L_CD"] = ($model->clicBtn == '1') ? $college["TOKYO_L_CD"]: $model->field["TOKYO_L_CD"];
                if ($college["PROTECTION_FLG"] == '1') {
                    $model->field["TOKYO_L_CD"] = $college["TOKYO_L_CD"];
                }
                $query = knje360Query::getTokyoLcd($model->field["SCHOOL_CATEGORY_CD"]);
                $extra = "onchange=\"collegeSelectEvent2();\"";
                $response .= "::".makeCmb2($objForm, $arg, $db, $query, "TOKYO_L_CD", $model->field["TOKYO_L_CD"], $extra, 1, 1);

                //中分類
                $model->field["TOKYO_M_CD"] = ($model->clicBtn == '1') ? $college["TOKYO_M_CD"]: $model->field["TOKYO_M_CD"];
                if ($college["PROTECTION_FLG"] == '1') {
                    $model->field["TOKYO_M_CD"] = $college["TOKYO_M_CD"];
                }
                $query = knje360Query::getTokyoMcd($model->field["SCHOOL_CATEGORY_CD"], $model->field["TOKYO_L_CD"]);
                $extra = "onchange=\"collegeSelectEvent2();\"";
                $response .= "::".makeCmb2($objForm, $arg, $db, $query, "TOKYO_M_CD", $model->field["TOKYO_M_CD"], $extra, 1, 1);

                $model->clicBtn = 0;
            }

            echo $response;
            die();
        }

        //東京都集計用
        if ($model->Properties["useTokyotoShinroTyousasyo"] == "1") {
            $arg["useTokyotoShinroTyousasyo"] = "1";

            //学校区分
            $query = knje360Query::getSchoolCategoryCd();
            $extra = "onchange=\"collegeSelectEvent2();\"";
            makeCmb($objForm, $arg, $db, $query, "SCHOOL_CATEGORY_CD", $Row["SCHOOL_CATEGORY_CD"], $extra, 1, "BLANK");

            //大分類
            $query = knje360Query::getTokyoLcd($Row["SCHOOL_CATEGORY_CD"]);
            $extra = "onchange=\"collegeSelectEvent2();\"";
            makeCmb($objForm, $arg, $db, $query, "TOKYO_L_CD", $Row["TOKYO_L_CD"], $extra, 1, "BLANK");

            //中分類
            $query = knje360Query::getTokyoMcd($Row["SCHOOL_CATEGORY_CD"], $Row["TOKYO_L_CD"]);
            $extra = "onchange=\"collegeSelectEvent2();\"";
            makeCmb($objForm, $arg, $db, $query, "TOKYO_M_CD", $Row["TOKYO_M_CD"], $extra, 1, "BLANK");
        }
        knjCreateHidden($objForm, "useTokyotoShinroTyousasyo", $model->Properties["useTokyotoShinroTyousasyo"]);

        //募集区分
        $query = knje360Query::getNameMst('E044');
        $extra = "onchange=\"collegeSelectEvent2();\"";
        makeCmb($objForm, $arg, $db, $query, "ADVERTISE_DIV", $Row["ADVERTISE_DIV"], $extra, 1, 1);

        //日程
        $query = knje360Query::getProgramCd($Row);
        $extra = "onchange=\"collegeSelectEvent2();\"";
        makeCmb($objForm, $arg, $db, $query, "PROGRAM_CD", $Row["PROGRAM_CD"], $extra, 1, 1);

        //方式
        $query = knje360Query::getFormCd($Row);
        $extra = "onchange=\"collegeSelectEvent2();\"";
        makeCmb($objForm, $arg, $db, $query, "FORM_CD", $Row["FORM_CD"], $extra, 1, 1);

        //大分類
        $query = knje360Query::getLCd($Row);
        $extra = "onchange=\"collegeSelectEvent2();\"";
        makeCmb($objForm, $arg, $db, $query, "L_CD", $Row["L_CD"], $extra, 1, 1);

        //小分類
        $query = knje360Query::getSCd($Row);
        $extra = "onchange=\"collegeSelectEvent2();\"";
        makeCmb($objForm, $arg, $db, $query, "S_CD", $Row["S_CD"], $extra, 1, 1);

        //締切日（窓口）
        $Row["LIMIT_DATE_WINDOW"] = ($Row["LIMIT_DATE_WINDOW"] == "") ? "" : str_replace("-", "/", $Row["LIMIT_DATE_WINDOW"]);
        $arg["data"]["LIMIT_DATE_WINDOW"] = View::popUpCalendar($objForm, "LIMIT_DATE_WINDOW", $Row["LIMIT_DATE_WINDOW"]);

        //締切日（郵送）
        $Row["LIMIT_DATE_MAIL"] = ($Row["LIMIT_DATE_MAIL"] == "") ? "" : str_replace("-", "/", $Row["LIMIT_DATE_MAIL"]);
        $arg["data"]["LIMIT_DATE_MAIL"] = View::popUpCalendar($objForm, "LIMIT_DATE_MAIL", $Row["LIMIT_DATE_MAIL"]);

        //郵送区分
        $opt = array();
        $opt[] = array('label' => "", 'value' => "");
        $opt[] = array('label' => "0：未定・その他", 'value' => "0");
        $opt[] = array('label' => "1：消印有効", 'value' => "1");
        $opt[] = array('label' => "2：必着", 'value' => "2");
        $Row["LIMIT_MAIL_DIV"] = ($Row["LIMIT_MAIL_DIV"] == "") ? $opt[0]["value"] : $Row["LIMIT_MAIL_DIV"];
        $extra = "";
        $arg["data"]["LIMIT_MAIL_DIV"] = knjCreateCombo($objForm, "LIMIT_MAIL_DIV", $Row["LIMIT_MAIL_DIV"], $opt, $extra, 1);

        //入試日
        $Row["STAT_DATE1"] = ($Row["STAT_DATE1"] == "") ? "" : str_replace("-", "/", $Row["STAT_DATE1"]);
        $arg["data"]["STAT_DATE1"] = View::popUpCalendar($objForm, "STAT_DATE1", $Row["STAT_DATE1"]);

        //合格発表日
        $Row["STAT_DATE3"] = ($Row["STAT_DATE3"] == "") ? "" : str_replace("-", "/", $Row["STAT_DATE3"]);
        $arg["data"]["STAT_DATE3"] = View::popUpCalendar($objForm, "STAT_DATE3", $Row["STAT_DATE3"]);

        //受験番号
        $arg["data"]["EXAMNO"] = knjCreateTextBox($objForm, $Row["EXAMNO"], "EXAMNO", 10, 10, "");

        //備考1
        $arg["data"]["CONTENTEXAM"] = knjCreateTextBox($objForm, $Row["CONTENTEXAM"], "CONTENTEXAM", 80, 40, "");

        //備考2
        $extra = "style=\"height:35px;\"";
        $arg["data"]["REASONEXAM"] = knjCreateTextArea($objForm, "REASONEXAM", 2, 75, "soft", $extra, $Row["REASONEXAM"]);

        //受験方式
        $query = knje360Query::getNameMst('E002');
        $extra = "onChange=\"changeDispSh(this);\"";
        makeCmb($objForm, $arg, $db, $query, "HOWTOEXAM", $Row["HOWTOEXAM"], $extra, 1, 1);

        if ($model->Properties["KNJE360_SENKOUKOUMOKU"] == "1") {
            $arg["KNJE360_SENKOUKOUMOKU"] = "1";

            //志望順位
            $arg["data"]["DESIRED_RANK"] = knjCreateTextBox($objForm, $Row["DESIRED_RANK"], "DESIRED_RANK", 3, 3, "");

            //選考分類
            $query = knje360Query::getNameMst('E054');
            $extra = "onChange=\"\"";
            makeCmb($objForm, $arg, $db, $query, "SELECT_CATEGORY", $Row["SELECT_CATEGORY"], $extra, 1, 1);

            //選考結果
            $query = knje360Query::getNameMst('E055');
            $extra = "onChange=\"\"";
            makeCmb($objForm, $arg, $db, $query, "SELECT_RESULT", $Row["SELECT_RESULT"], $extra, 1, 1);
        }

        $setShDivArr = array();
        $query = knje360Query::getNameMstNamecd2('E002');
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $setShDivArr[] = $row["NAMECD2"];
        }
        knjCreateHidden($objForm, "SH_ARR", implode(",", $setShDivArr));
        if (in_array($Row["HOWTOEXAM"], $setShDivArr)) {
            $arg["data"]["SH_DISP"] = " style=\"display:inline;\" ";
        } else {
            $arg["data"]["SH_DISP"] = " style=\"display:none;\" ";
        }

        //専併区分コンボ
        $query = knje360Query::getNameMst('L006');
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "SHDIV", $Row["SHDIV"], $extra, 1, "BLANK");

        //受験結果
        $setDecision = $model->seq ? $Row["DECISION"] : $model->Properties["KNJE360_DEF_DECISION"];
        $query = knje360Query::getNameMst('E005');
        makeCmb($objForm, $arg, $db, $query, "DECISION", $setDecision, "", 1, 1);

        //合格短冊匿名希望チェックボックス
        if ($model->Properties["knje360ShowTokumeiCheck"] == "1") {
            $arg["knje360ShowTokumeiCheck"] = "1";
            $extra  = "id=\"TOKUMEI\"";
            $extra .= $Row["TOKUMEI"] == "1" ? " checked " : "";
            $arg["data"]["TOKUMEI"] = knjCreateCheckBox($objForm, "TOKUMEI", "1", $extra);
        }

        //証明書番号取得
        $certif_no = "";
        if (isset($model->seq)) {
            $query = knje360Query::getCertifNo($model, $model->seq);
            $certif_no = $db->getOne($query);
        }
        //証明書学校データ
        $query = knje360Query::getCertifSchoolDat($model);
        $certifSchool = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if ($certifSchool["CERTIF_NO"] == "0") {
            $arg["PRINT_CERTIF_NO"] = "1";
        }
        $arg["data"]["CERTIF_NO"] = $certif_no;

        //調査書発行チェックボックス
        $setIssue = $model->seq ? $Row["ISSUE"] : $model->Properties["KNJE360_DEF_ISSUE"];
        $extra = " id=\"ISSUE\" onClick=\"issueControl(this);\"";
        $checked = $setIssue == "1" ? " checked " : "";
        $arg["data"]["ISSUE"] = knjCreateCheckBox($objForm, "ISSUE", "1", $extra.$checked);
        if ($model->Properties["KNJE360_DEF_ISSUE"] == 1) {
            $arg["useISSUE"] = 1;
        }

        //進路状況
        $query = knje360Query::getNameMst('E006');
        makeCmb($objForm, $arg, $db, $query, "PLANSTAT", $Row["PLANSTAT"], "", 1, 1);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $db, $model, $Row);

        //DB切断
        Query::dbCheckIn($db);

        if (get_count($model->warning)== 0 && $model->cmd !="subform1_clear") {
            $arg["next"] = "NextStudent(0);";
        } elseif ($model->cmd =="subform1_clear") {
            $arg["next"] = "NextStudent(1);";
        }

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje360SubForm2.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //確定ボタン
    $extra = " onclick=\"collegeSelectEvent3();\"";
    $arg["button"]["btn_kakutei"] = knjCreateBtn($objForm, "btn_kakutei", "確 定", $extra);

    //学校検索ボタンを作成する
    $requestroot = REQUESTROOT;
    $extra = " onclick=\"Page_jumper('{$requestroot}');\"";
    $arg["button"]["btn_search"] = knjCreateBtn($objForm, "btn_search", "学校検索", $extra);

    $disabled = ($model->mode == "grd") ? " disabled" : "";
    //追加ボタンを作成する
    $arg["button"]["btn_insert"] = knjCreateBtn($objForm, "btn_insert", "追 加", "onclick=\"return btn_submit('subform2_insert');\"");
    //追加後前の生徒へを作成する
    $extra = " onclick=\"return updateNextStudent('".$model->schregno."', 1);\" style=\"width:130px\"";
    $arg["button"]["btn_up_pre"] = knjCreateBtn($objForm, "btn_up_pre", "追加後前の{$model->sch_label}へ", $extra.$disabled);
    //追加後次の生徒へを作成する
    $extra = " onclick=\"return updateNextStudent('".$model->schregno."', 0);\" style=\"width:130px\"";
    $arg["button"]["btn_up_next"] = knjCreateBtn($objForm, "btn_up_next", "追加後次の{$model->sch_label}へ", $extra.$disabled);
    //更新ボタンを作成する
    $extra = ($model->seq == "") ? "disabled" : "onclick=\"return btn_submit('subform2_update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //クリアボタンを作成する
    $extra = ($model->seq == "") ? "disabled" : "onclick=\"return btn_submit('subform2_clear');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //戻るボタン
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", "onclick=\"return btn_submit('edit');\"");

    //進路相談ボタン
    $extra = "style=\"height:30px;background:#FFE4E1;color:#FF0000;font:bold\" onclick=\"return btn_submit('subform4');\"";
    $arg["button"]["btn_subform4"] = KnjCreateBtn($objForm, "btn_subform4", "進路相談", $extra.$disabled);

    //一括更新ボタン
    $link = REQUESTROOT."/E/KNJE360/knje360index.php?cmd=replace2&SCHREGNO=".$model->schregno."&SEQ=".$model->seq;
    $extra = "onclick=\"window.open('$link','_self');\"";
    $arg["button"]["btn_replace"] = KnjCreateBtn($objForm, "btn_replace", "一括更新", $extra.$disabled);
}

//hidden作成
function makeHidden(&$objForm, $db, $model, $Row)
{
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    knjCreateHidden($objForm, "SEQ", $model->seq);
    knjCreateHidden($objForm, "ORIGINAL_ISSUE", $Row["ISSUE"]);
    knjCreateHidden($objForm, "cmd");

    $semes = $db->getRow(knje360Query::getSemesterMst(), DB_FETCHMODE_ASSOC);
    knjCreateHidden($objForm, "SDATE", str_replace("-", "/", $semes["SDATE"]));
    knjCreateHidden($objForm, "EDATE", str_replace("-", "/", $semes["EDATE"]));
    knjCreateHidden($objForm, "USEAUTOSETCOLLEGENAMETOTHINKEXAM", $model->Properties["useAutoSetCollegeNameToThinkExam"]);
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $space = "")
{
    $opt = array();
    if ($space) {
        $opt[] = array('label' => "", 'value' => "");
    }
    $value_flg = false;
    $result1 = $db->query($query);
    while ($row1 = $result1->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row1["LABEL"],
                       'value' => $row1["VALUE"]);
        if ($value == $row1["VALUE"]) {
            $value_flg = true;
        }
    }

    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
//コンボ作成
function makeCmb2(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $space = "")
{
    $opt = array();
    if ($space) {
        $opt[] = array('label' => "", 'value' => "");
    }
    $value_flg = false;
    $result1 = $db->query($query);
    while ($row1 = $result1->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row1["LABEL"],
                       'value' => $row1["VALUE"]);
        if ($value == $row1["VALUE"]) {
            $value_flg = true;
        }
    }

    if (get_count($opt) == 2) {
        $value = ($value != "" && $value_flg) ? $value : $opt[1]["value"];
    } else {
        $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    }

    return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//日付チェック
function makeDate($monthDay)
{
    if ($monthDay == "") {
        return "";
    }
    if (strlen($monthDay) != 4) {
        return "";
    }
    $month = substr($monthDay, 0, 2);
    $day = substr($monthDay, 2);
    $year = ((int)$month * 1) < 4 ? CTRL_YEAR + 1 : CTRL_YEAR;
    if (checkdate($month, $day, $year)) {
        return $year."/".$month."/".$day;
    } else {
        return "";
    }
}
