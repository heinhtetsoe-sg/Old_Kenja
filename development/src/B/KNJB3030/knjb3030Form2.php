<?php

require_once('for_php7.php');

class knjb3030Form2
{
    public function main(&$model)
    {
        $objForm = new form();
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjb3030index.php", "", "edit");

        //１レコード取得（講座データ）
        if ($model->cmd != "group" && !isset($model->warning)) {
            $Row = knjb3030Query::getRowTest($model, $model->term, $model->chaircd);
            if ($model->Properties["useCurriculumcd"] == '1' || $model->Properties["useSpecial_Support_School"] == '1') {
                $Row["SUBCLASSCD"] = $Row["CLASSCD"].'-'.$Row["SCHOOL_KIND"].'-'.$Row["CURRICULUM_CD"].'-'.$Row["SUBCLASSCD"];
            }
        } else {
            $Row =& $model->fields;
        }

        if ($model->Properties["useTestFacility"] == "1") {
            $arg['useTestFacilityFlg'] = '1';
        }
        if ($model->Properties["chairRetsuMeisho_Hyouji"] == "1") {
            $arg['chairRetsuMeisho_Hyouji'] = '1';
        }
        //レコード取得（その他）
        if ($model->cmd != "group" && !isset($model->warning)) {
            $db = Query::dbCheckOut();

            if ($model->keikokutenFlag) {
                $arg["KEIKOKUTEN_FLAG"] = '1';
            }

            //使用施設
            $result = $db->query(knjb3030Query::getFac($model->term, $model->chaircd));
            $opt_lab = $opt_val = array();
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt_lab[] = $row["FACILITYABBV"];
                $opt_val[] = $row["FACCD"];
            }
            $Row_Fac["FACILITYABBV"]    = implode(",", $opt_lab);
            $Row_Fac["FACCD"]           = implode(",", $opt_val);
            if ($model->Properties["useTestFacility"] == "1") {
                //試験会場
                $result = $db->query(knjb3030Query::getSikenKaizyou($model->term, $model->chaircd));
                $opt_lab = $opt_val = array();
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $opt_lab[] = $row["FACILITYABBV"];
                    $opt_val[] = $row["FACCD"];
                }
                $Row_Fac["SIKENKAIZYOUFACILITYABBV"]    = implode(",", $opt_lab);
                $Row_Fac["SIKENKAIZYOUFACCD"]           = implode(",", $opt_val);
            }
            //教科書
            $result = $db->query(knjb3030Query::getTextbook($model->term, $model->chaircd));
            $opt_lab = $opt_val = array();
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt_lab[] = $row["TEXTBOOKABBV"];
                $opt_val[] = $row["TEXTBOOKCD"];
            }
            $Row_Textbook["TEXTBOOKABBV"]   = implode(",", $opt_lab);
            $Row_Textbook["TEXTBOOKCD"]     = implode(",", $opt_val);
            //科目担任
            $result = $db->query(knjb3030Query::getStaff($model, $model->term, $model->chaircd));
            $opt_lab = $opt_lab1 = $opt_val = $opt_val2 = $opt_val3 = $opt_order = $opt_stfOrder = array();
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($row["CHARGEDIV"]=="1") {
                    $opt_lab1[]  = $row["STAFFNAME_SHOW"];  //正
                } else {
                    $opt_lab[]  = $row["STAFFNAME_SHOW"];   //副
                }
                $opt_val[]      = $row["STAFFCD"];
                $opt_val2[]     = $row["CHARGEDIV"];
                $opt_val3[]     = $row["STAFFCD"]."-".$row["CHARGEDIV"];    //ダイアログで正副を判断するために使用
                $opt_order[]    = $row["STF_ORDER"];
                $opt_stfOrder[] = $row["STAFFCD"]."-".$row["STF_ORDER"];
            }
            $Row_Staff["STAFFNAME_SHOW1"]   = implode(",", $opt_lab1);
            $Row_Staff["STAFFNAME_SHOW"]    = implode(",", $opt_lab);
            $Row_Staff["STAFFCD"]           = implode(",", $opt_val);
            $Row_Staff["CHARGEDIV"]         = implode(",", $opt_val2);
            $Row_Staff["STF_CHARGE"]        = implode(",", $opt_val3);
            $Row_Staff["ORDER"]             = implode(",", $opt_order);
            $Row_Staff["STF_ORDER"]         = implode(",", $opt_stfOrder);
            //HRクラス
            $result = $db->query(knjb3030Query::getGradeClass($model, $model->term, $model->chaircd, $Row["GROUPCD"]));
            $opt_lab = $opt_val = array();
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt_lab[] = $row["HR_NAMEABBV"];
                $opt_val[] = $row["GRADE_CLASS"];
            }
            $Row_GradeClass["HR_NAMEABBV"]  = implode(",", $opt_lab);
            $Row_GradeClass["GRADE_CLASS"]  = implode(",", $opt_val);

            //授業クラス
            if ($model->Properties["useLc_Hrclass"] == "1") {
                $result = $db->query(knjb3030Query::getGradeLcClass($model, $model->term, $model->chaircd, $Row["GROUPCD"]));
                $opt_lab = $opt_val = array();
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $opt_lab[] = $row["LC_NAMEABBV"];
                    $opt_val[] = $row["GRADE_LC_CLASS"];
                }
                $Row_GradeClass["LC_NAMEABBV"]  = implode(",", $opt_lab);
                $Row_GradeClass["GRADE_LC_CLASS"]  = implode(",", $opt_val);
            } else {
                $Row_GradeClass["LC_NAMEABBV"]  = '';
                $Row_GradeClass["GRADE_LC_CLASS"]  = '';
            }

            Query::dbCheckIn($db);
        } else {
            $Row_Fac["FACILITYABBV"]             = $model->fields["FACILITYABBV"];
            $Row_Fac["FACCD"]                    = $model->fields["FACCD"];
            $Row_Fac["SIKENKAIZYOUFACILITYABBV"] = $model->fields["SIKENKAIZYOUFACILITYABBV"];
            $Row_Fac["SIKENKAIZYOUFACCD"]        = $model->fields["SIKENKAIZYOUFACCD"];
            $Row_Textbook["TEXTBOOKABBV"]        = $model->fields["TEXTBOOKABBV"];
            $Row_Textbook["TEXTBOOKCD"]          = $model->fields["TEXTBOOKCD"];
            $Row_Staff["STAFFNAME_SHOW1"]        = $model->fields["STAFFNAME_SHOW1"];
            $Row_Staff["STAFFNAME_SHOW"]         = $model->fields["STAFFNAME_SHOW"];
            $Row_Staff["STAFFCD"]                = $model->fields["STAFFCD"];
            $Row_Staff["CHARGEDIV"]              = $model->fields["CHARGEDIV"];
            $Row_Staff["STF_CHARGE"]             = $model->fields["STF_CHARGE"];
            $Row_Staff["ORDER"]                  = $model->fields["ORDER"];
            $Row_Staff["STF_ORDER"]              = $model->fields["STF_ORDER"];
            $Row_GradeClass["HR_NAMEABBV"]       = $model->fields["HR_NAMEABBV"];
            $Row_GradeClass["GRADE_CLASS"]       = $model->fields["GRADE_CLASS"];
            $Row_GradeClass["LC_NAMEABBV"]       = $model->fields["LC_NAMEABBV"];
            $Row_GradeClass["GRADE_LC_CLASS"]    = $model->fields["GRADE_LC_CLASS"];
        }
        //授業回数と受講クラス選択ボタンの使用可・不可
        if ($Row["GROUPCD"] > "0000") {
            $read_lesson = "STYLE=\"background-color:darkgray\" readonly";
            $dis_search = "disabled";
        } else {
            $read_lesson = "";
            $dis_search = "";
        }
        //群コードが変更されたときの処理
        if ($model->cmd == "group") {
            if ($Row["GROUPCD"] > "0000") {
                //授業回数
                $Row_lesson = knjb3030Query::getRowLesson($model->term, $Row["GROUPCD"]);
                $Row["LESSONCNT"] = $Row_lesson["LESSONCNT"];
                $Row["FRAMECNT"] = $Row_lesson["FRAMECNT"];
                //受講クラス
                $db  = Query::dbCheckOut();
                $result = $db->query(knjb3030Query::getGradeClass($model, $model->term, $model->chaircd, $Row["GROUPCD"]));
                $opt_lab = $opt_val = array();
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $opt_lab[] = $row["HR_NAMEABBV"];
                    $opt_val[] = $row["GRADE_CLASS"];
                }
                $Row_GradeClass["HR_NAMEABBV"]  = implode(",", $opt_lab);
                $Row_GradeClass["GRADE_CLASS"]  = implode(",", $opt_val);

                if ($model->Properties["useLc_Hrclass"] == "1") {
                    //受講クラス
                    $result = $db->query(knjb3030Query::getGradeLcClass($model, $model->term, $model->chaircd, $Row["GROUPCD"]));
                    $opt_lab = $opt_val = array();
                    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                        $opt_lab[] = $row["LC_NAMEABBV"];
                        $opt_val[] = $row["GRADE_LC_CLASS"];
                    }
                    $Row_GradeClass["LC_NAMEABBV"]  = implode(",", $opt_lab);
                    $Row_GradeClass["GRADE_LC_CLASS"]  = implode(",", $opt_val);
                } else {
                    $Row_GradeClass["LC_NAMEABBV"]  = '';
                    $Row_GradeClass["GRADE_LC_CLASS"]  = '';
                }

                Query::dbCheckIn($db);
            } else {
                $Row["LESSONCNT"] = "";
                $Row["FRAMECNT"] = "";
                $Row_GradeClass["HR_NAMEABBV"]  = "";
                $Row_GradeClass["GRADE_CLASS"]  = "";
                $Row_GradeClass["LC_NAMEABBV"]  = "";
                $Row_GradeClass["GRADE_LC_CLASS"]  = "";
            }
        }

        //講座コード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["CHAIRCD"] = knjCreateTextBox($objForm, $Row["CHAIRCD"], "CHAIRCD", 8, 7, $extra);

        //講座名称
        $extra = "";
        $arg["data"]["CHAIRNAME"] = getTextOrArea($objForm, "CHAIRNAME", 10, 1, $Row["CHAIRNAME"]);
        setInputChkHidden($objForm, "CHAIRNAME", 10, 1, $arg);

        //講座略称
        $extra = "";
        $arg["data"]["CHAIRABBV"] = getTextOrArea($objForm, "CHAIRABBV", 5, 1, $Row["CHAIRABBV"]);
        setInputChkHidden($objForm, "CHAIRABBV", 5, 1, $arg);

        $db  = Query::dbCheckOut();

        //列名称
        $extra = "";
        $query = knjb3030Query::getRetumei($model);
        makeCmb($objForm, $arg, $db, $query, $Row["SEQ004_REMARK1"], "SEQ004_REMARK1", $extra, 1, "BLANK");

        Query::dbCheckIn($db);

        //スモールクラス名称
        $extra = "";
        $arg["data"]["SEQ004_REMARK2"] = knjCreateTextBox($objForm, $Row["SEQ004_REMARK2"], "SEQ004_REMARK2", 16, 15, $extra);

        //習熟度クラス名称
        $extra = "";
        $arg["data"]["SEQ004_REMARK3"] = knjCreateTextBox($objForm, $Row["SEQ004_REMARK3"], "SEQ004_REMARK3", 16, 15, $extra);

        if ($model->Properties["useSpecial_Support_School"] == "1") {
            $db  = Query::dbCheckOut();

            //科目コード
            $extra = "";
            $query = knjb3030Query::getSubclassCondition($model, substr($model->term, 0, 4));
            makeCmb($objForm, $arg, $db, $query, $Row["SUBCLASSCD"], "SUBCLASSCD", $extra, 1, "BLANK");

            Query::dbCheckIn($db);
        } else {
            //科目コード
            $extra = "";
            $arg["data"]["SUBCLASSCD"] = knjCreateCombo($objForm, "SUBCLASSCD", $Row["SUBCLASSCD"], knjb3030Query::getSubclass($model, substr($model->term, 0, 4)), $extra, 1);
        }

        //履修期間区分
        $extra = "";
        $arg["data"]["TAKESEMES"] = knjCreateCombo($objForm, "TAKESEMES", $Row["TAKESEMES"], knjb3030Query::getTakesemes(substr($model->term, 0, 4)), $extra, 1);

        //履修コース
        $objForm->ae(array("type"        => "select",
                            "name"        => "RISYUU_COURSE",
                            "size"        => 1,
                            "value"       => $Row["RISYUU_COURSE"],
                            "options"      => knjb3030Query::getRisyuuCourse(substr($model->term, 0, 4)) ));
        $arg["data"]["RISYUU_COURSE"] = $objForm->ge("RISYUU_COURSE");
        $arg["useChairDatRisyuuCourse"] = ($model->Properties["useChairDatRisyuuCourse"] == '1') ? 1 : "";

        //週授業回数
        $extra = "onblur=\"this.value=toInteger(this.value)\"".$read_lesson;
        $arg["data"]["LESSONCNT"] = knjCreateTextBox($objForm, $Row["LESSONCNT"], "LESSONCNT", 3, 2, $extra);

        //連続枠数
        $extra = "onblur=\"this.value=toInteger(this.value)\"".$read_lesson;
        $arg["data"]["FRAMECNT"] = knjCreateTextBox($objForm, $Row["FRAMECNT"], "FRAMECNT", 3, 2, $extra);

        //使用施設
        $extra = "STYLE=\"width:100%;background-color:darkgray\" readonly";
        $arg["data"]["FACILITYABBV"] = knjCreateTextBox($objForm, $Row_Fac["FACILITYABBV"], "FACILITYABBV", 31, 30, $extra);

        //試験会場
        $extra = "STYLE=\"width:100%;background-color:darkgray\" readonly";
        $arg["data"]["SIKEN_KAIZYOU"] = knjCreateTextBox($objForm, $Row_Fac["SIKENKAIZYOUFACILITYABBV"], "SIKENKAIZYOUFACILITYABBV", 31, 30, $extra);

        //教科書
        $extra = "style=\"width:100%; background-color:darkgray\" readonly";
        $arg["data"]["TEXTBOOKABBV"] = knjCreateTextBox($objForm, $Row_Textbook["TEXTBOOKABBV"], "TEXTBOOKABBV", 31, 30, $extra);

        //科目担任（正）
        $extra = "style=\"width:100%; background-color:darkgray\" readonly";
        $arg["data"]["STAFFNAME_SHOW1"] = knjCreateTextBox($objForm, $Row_Staff["STAFFNAME_SHOW1"], "STAFFNAME_SHOW1", 31, 30, $extra);

        //科目担任（副）
        $extra = "style=\"width:100%; background-color:darkgray\" readonly";
        $arg["data"]["STAFFNAME_SHOW"] = knjCreateTextBox($objForm, $Row_Staff["STAFFNAME_SHOW"], "STAFFNAME_SHOW", 31, 30, $extra);

        //群コード
        $extra = "onChange=\"btn_submit('group')\"";
        $arg["data"]["GROUPCD"] = knjCreateCombo($objForm, "GROUPCD", $Row["GROUPCD"], knjb3030Query::getGroup(substr($model->term, 0, 4)), $extra, 1);

        //集計フラグ 1:集計する 0:集計しない
        $checked_flg = ($Row["COUNTFLG"] == "1") ? "checked" : "";
        $extra = " class=\"changeColor\" data-name=\"COUNTFLG\" id=\"COUNTFLG\" {$checked_flg}";
        $arg["data"]["COUNTFLG"] = knjCreateCheckBox($objForm, "COUNTFLG", "1", $extra);

        //受講人数テキストボックス
        $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
        $value = $Row["STD_CNT"];
        $arg["data"]["STD_CNT"] = knjCreateTextBox($objForm, $value, "STD_CNT", 3, 3, $extra);

        //１日出欠対象外checkbox
        $checkFlg = ($Row["NOT_DAY_ATTEND"] == "1") ? " checked": "";
        $extra = " class=\"changeColor\" data-name=\"NOT_DAY_ATTEND\" id=\"NOT_DAY_ATTEND\"";
        $arg["data"]["NOT_DAY_ATTEND"] = knjCreateCheckBox($objForm, "NOT_DAY_ATTEND", "1", $extra.$checkFlg);

        //警告点(素点)
        $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
        $value = $Row["KEIKOKUTEN_SOTEN"];
        $arg["data"]["KEIKOKUTEN_SOTEN"] = knjCreateTextBox($objForm, $value, "KEIKOKUTEN_SOTEN", 5, 5, $extra);

        //警告点(評価)
        $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
        $value = $Row["KEIKOKUTEN_HYOUKA"];
        $arg["data"]["KEIKOKUTEN_HYOUKA"] = knjCreateTextBox($objForm, $value, "KEIKOKUTEN_HYOUKA", 5, 5, $extra);

        //警告点(評定)
        $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
        $value = $Row["KEIKOKUTEN_HYOUTEI"];
        $arg["data"]["KEIKOKUTEN_HYOUTEI"] = knjCreateTextBox($objForm, $value, "KEIKOKUTEN_HYOUTEI", 5, 5, $extra);

        //HRクラス選択ボタンを作成する
        $extra = "onclick=\"return btn_submit('hrSearch');\" style=\"width:110px\"".$dis_search;
        $arg["button"]["btn_hrSearch"] = knjCreateBtn($objForm, "btn_hrSearch", "HRクラス選択", $extra);

        //HRクラス
        $extra = "style=\"width:100%; background-color:darkgray\" readonly";
        $arg["data"]["HR_NAMEABBV"] = knjCreateTextBox($objForm, $Row_GradeClass["HR_NAMEABBV"], "HR_NAMEABBV", 31, 30, $extra);

        //授業クラス選択ボタンを作成する
        $extra = "onclick=\"return btn_submit('lcSearch');\" style=\"width:110px\"".$dis_search;
        $arg["button"]["btn_lcSearch"] = knjCreateBtn($objForm, "btn_lcSearch", "授業クラス選択", $extra);

        //授業クラス
        $extra = "style=\"width:100%; background-color:darkgray\" readonly";
        $arg["data"]["LC_NAMEABBV"] = knjCreateTextBox($objForm, $Row_GradeClass["LC_NAMEABBV"], "LC_NAMEABBV", 31, 30, $extra);

        //科目担任選択ボタンを作成する
        $extra = "onclick=\"return btn_submit('subformStaff');\" style=\"width:110px\"";
        $arg["button"]["btn_subformStaff"] = knjCreateBtn($objForm, "btn_subformStaff", "科目担任選択", $extra);

        //使用施設選択ボタンを作成する
        $extra = "onclick=\"return btn_submit('subformFacility');\" style=\"width:110px\"";
        $arg["button"]["btn_subformFacility"] = knjCreateBtn($objForm, "btn_subformFacility", "使用施設選択", $extra);

        //試験会場選択ボタンを作成する
        $extra = "onclick=\"return btn_submit('subformSikenKaizyou');\" style=\"width:110px\"";
        $arg["button"]["btn_SikenKaizyou"] = knjCreateBtn($objForm, "btn_SikenKaizyou", "試験会場選択", $extra);

        //教科書選択ボタンを作成する
        $extra = "onclick=\"return btn_submit('subformTextBook');\" style=\"width:110px\"";
        $arg["button"]["btn_subformTextBook"] = knjCreateBtn($objForm, "btn_subformTextBook", "教科書選択", $extra);

        //名簿入力画面へ選択ボタンの使用可・不可
        //次年度の場合も有効とする
        if (CTRL_YEAR < substr($model->term, 0, 4)) {
            $dis_jump = "";
        } elseif ($model->isTsushin && CTRL_YEAR == substr($model->term, 0, 4) && CTRL_SEMESTER < substr($model->term, 5)) {
            $dis_jump = "";
        } elseif (substr($model->term, 0, 4) != CTRL_YEAR || substr($model->term, 5) != CTRL_SEMESTER) {
            $dis_jump = "disabled";
        } else {
            $dis_jump = "";
        }
        //リンク先作成
        $jumping = REQUESTROOT."/B/KNJB3050/knjb3050index.php";
        //リンクボタンを作成する
        $extra = "style=\"width:120px\" onclick=\" Page_jumper('".$jumping."',
                                                                      '".substr($model->term, 0, 4)."',
                                                                      '".substr($model->term, 5)."',
                                                                      '".$model->chaircd."',
                                                                      '".$Row["GROUPCD"]."',
                                                                      '".STAFFCD."',
                                                                      '".AUTHORITY."');\"".$dis_jump;
        $arg["btn_jump"] = knjCreateBtn($objForm, "btn_jump", "名簿入力画面へ", $extra);

        //追加、修正、削除のdisabled設定
        $disabled = "";
        if (AUTHORITY < DEF_UPDATABLE) {
            $disabled = "disabled";
        }
        //追加ボタン
        $extra = "onclick=\"return btn_submit('insert');\" ".$disabled;
        $arg["button"]["btn_insert"] = knjCreateBtn($objForm, "btn_insert", "追 加", $extra);

        //修正ボタン
        $extra = "onclick=\"return btn_submit('update');\" ".$disabled;
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_udpate", "更 新", $extra);

        //削除ボタン
        $extra = "onclick=\"return btn_submit('delete');\" ".$disabled;
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);

        //クリアボタン
        $extra = "onclick=\"return btn_submit('reset')\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "UPDATED", $Row["UPDATED"]);
        knjCreateHidden($objForm, "FACCD", $Row_Fac["FACCD"]);
        knjCreateHidden($objForm, "SIKENKAIZYOUFACCD", $Row_Fac["SIKENKAIZYOUFACCD"]);
        knjCreateHidden($objForm, "TEXTBOOKCD", $Row_Textbook["TEXTBOOKCD"]);
        knjCreateHidden($objForm, "STAFFCD", $Row_Staff["STAFFCD"]);
        knjCreateHidden($objForm, "CHARGEDIV", $Row_Staff["CHARGEDIV"]);
        knjCreateHidden($objForm, "STF_CHARGE", $Row_Staff["STF_CHARGE"]);
        knjCreateHidden($objForm, "ORDER", $Row_Staff["ORDER"]);
        knjCreateHidden($objForm, "STF_ORDER", $Row_Staff["STF_ORDER"]);
        knjCreateHidden($objForm, "GRADE_CLASS", $Row_GradeClass["GRADE_CLASS"]);
        knjCreateHidden($objForm, "GRADE_LC_CLASS", $Row_GradeClass["GRADE_LC_CLASS"]);
        knjCreateHidden($objForm, "useChairStaffOrder", $model->Properties["useChairStaffOrder"]);


        $arg["finish"]  = $objForm->get_finish();

        if ($model->cmd != "group" && VARS::get("cmd") != "edit" && !isset($model->warning)) {
            $arg["reload"]  = "window.open('knjb3030index.php?cmd=list&ed=1','left_frame');";
        }
        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        if ($model->Properties["useLc_Hrclass"] == "1") {
            $arg["useLc_Hrclass"] = 1;
        }
        View::toHTML5($model, "knjb3030Form2.html", $arg);
    }
}

//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    if ($blank == "ALL") {
        $opt[] = array("label" => "--全て--", "value" => "999");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

//テキストボックス or テキストエリア作成
function getTextOrArea(&$objForm, $name, $moji, $gyou, $val)
{
    $retArg = "";
    if ($gyou > 1) {
        //textArea
        $extra = "style=\"overflow-y:scroll\" id=\"".$name."\"";
        $retArg = knjCreateTextArea($objForm, $name, $gyou, ($moji * 2), "soft", $extra, $val);
    } else {
        //textbox
        $extra = "onkeypress=\"btn_keypress();\" id=\"".$name."\"";
        // $retArg = knjCreateTextBox($objForm, $val, $name, ($moji * 2), $moji, $extra);
        $retArg = knjCreateTextBox($objForm, $val, $name, ($moji * 2), ($moji * 2), $extra);
    }
    return $retArg;
}
function setInputChkHidden(&$objForm, $setHiddenStr, $keta, $gyo, &$arg)
{
    $arg["data"][$setHiddenStr."_COMMENT"] = getTextAreaComment($keta, $gyo);
    KnjCreateHidden($objForm, $setHiddenStr."_KETA", $keta*2);
    KnjCreateHidden($objForm, $setHiddenStr."_GYO", $gyo);
    KnjCreateHidden($objForm, $setHiddenStr."_STAT", "statusarea_".$setHiddenStr);
}
function getTextAreaComment($moji, $gyo)
{
    $comment = "";
    if ($gyo > 1) {
        $comment .= "(全角{$moji}文字X{$gyo}行まで)";
    } else {
        $comment .= "(全角{$moji}文字まで)";
    }
    return $comment;
}
