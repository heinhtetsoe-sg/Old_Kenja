<?php

require_once('for_php7.php');


class knjg081aForm1
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjg081aindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //処理年度
        $arg["YEAR"] = CTRL_YEAR;

        //年組コンボ作成
        $query = knjg081aQuery::getGradeHrclass();
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1, 1);

        $model->schoolKind = $db->getOne(knjg081aQuery::getSchoolKind(substr($model->field["GRADE_HR_CLASS"],0,2)));

        //卒業可能学年チェック
        $query = knjg081aQuery::checkGrdGrade(substr($model->field["GRADE_HR_CLASS"],0,2));
        $grdGrade = $db->getOne($query);

        if ($model->Properties["useProvFlg"] == 1 && $grdGrade > 0) {
            if ('' != $db->getOne(knjg081aQuery::getTableColumn("STUDYREC_PROV_FLG_DAT", "PROV_TESTKINDCD"))) {
                //仮評定データ取得
                $prov = $db->getRow(knjg081aQuery::getProvData($model), DB_FETCHMODE_ASSOC);
                //SCHREG_STUDYREC_DATの評定有チェック
                $valCnt = $db->getOne(knjg081aQuery::getValuationCnt($model));

                if (strlen($prov["PROV_SEMESTER"]) > 0) {
                    $arg["el"]["PROV_INFO"] = '（ 仮評定：'.$prov["SEMESTERNAME"].'　'.$prov["TESTITEMNAME"].' ）';
                } else if ($valCnt > 0) {
                    $arg["el"]["PROV_INFO"] = '（ 本評定 ）';
                }
            }
        }
        if ($model->Properties["useProvFlg"] == 1) {
            $arg["el"]["HYOTEI_KARI"] = '仮';
        }

        //初期化
        $model->data = array();
        $counter = 0;

        //一覧表示
        $colorFlg = false;
        $result = $db->query(knjg081aQuery::selectQuery($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //学籍番号を配列で取得
            $model->data["SCHREGNO"][] = $row["SCHREGNO"];
            knjCreateHidden($objForm, "SCHREGNO"."-".$counter, $row["SCHREGNO"]);

            //出席番号
            if($row["ATTENDNO"] != ""){
                $row["ATTENDNO"] = sprintf("%01d", $row["ATTENDNO"]).'番';
            }

            if ($counter % 5 == 0) {
                $colorFlg = !$colorFlg;
            }

            //発行枚数テキストボックス
            $value = (!isset($model->warning)) ? $row["PRINT_CNT"] : $model->fields["PRINT_CNT"][$counter];
            $extra = " id=\"PRINT_CNT"."-".$counter."\"";
            $extra .= "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\" onPaste=\"return showPaste(this);\"";
            $row["PRINT_CNT"] = knjCreateTextBox($objForm, $value, "PRINT_CNT"."-".$counter, 2, 2, $extra);

            $row["COLOR"] = $colorFlg ? "#ffffff" : "#cccccc";

            $counter++;
            $arg["data"][] = $row;
        }
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        if ($model->Properties["tyousasyoHankiNintei"] == '1') {
            //調査書種類ラジオボタン 1:進学用（通年用） 1h:進学用（半期認定）2:就職用（通年用） 2h:就職用（半期認定）
            $arg["hanki"] = "1";
            $opt_output = array("1", "1h", "futo");
            $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT1h\"", "id=\"OUTPUT_FUTO\"");
        } else {
            //調査書種類ラジオボタン 1:進学用 2:就職用
            $arg["notHanki"] = "1";
            $opt_output = array("1", "futo");
            $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT_FUTO\"");
        }
        for ($i = 0; $i < get_count($opt_output); $i++) {
            $objForm->ae(array("type" => "radio",
                               "name" => "OUTPUT",
                               "value" => $model->field["OUTPUT"],
                               "extrahtml" => $extra[$i],
                               "multiple" => $opt_output));
            
            $arg["el"]["OUTPUT".$opt_output[$i]] = $objForm->ge("OUTPUT", $opt_output[$i]);
        }

        //漢字氏名印刷ラジオボタン 1:する 2:しない
        $opt_kanji = array(1, 2);
        $model->field["KANJI"] = ($model->field["KANJI"] == "") ? "1" : $model->field["KANJI"];
        $extra = array("id=\"KANJI1\"", "id=\"KANJI2\"");
        $radioArray = knjCreateRadio($objForm, "KANJI", $model->field["KANJI"], $extra, $opt_kanji, get_count($opt_kanji));
        foreach($radioArray as $key => $val) $arg["el"][$key] = $val;

        //その他住所を優先して印字チェックボックス
        $query = knjg081aQuery::getSchoolDiv($model);
        $schooldiv = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $extra  = ($model->field["SONOTAJUUSYO"] == "on" || $model->cmd == "init" && $schooldiv["IS_TUSIN"] == '1') ? "checked" : "";
        $extra .= " id=\"SONOTAJUUSYO\"";
        $arg["el"]["SONOTAJUUSYO"] = knjCreateCheckBox($objForm, "SONOTAJUUSYO", "on", $extra, "");

        //評定読替の項目を表示するかしないかのフラグ
        if ($model->Properties["hyoteiYomikae"] == '1') {
            $arg["hyoteiYomikae"] = '1';
        }

        //評定読替チェックボックス
        $extra  = ($model->field["HYOTEI"] == "on" || $model->cmd == "init") ? "checked" : "";
        $extra .= " id=\"HYOTEI\"";
        $arg["el"]["HYOTEI"] = knjCreateCheckBox($objForm, "HYOTEI", "on", $extra, "");

        //学習成績概評チェックボックス
        if ($model->cmd == 'init') {
            if ($model->Properties["tyousasyoNotCheckGaihyoComment"] == '1') {
                $model->field["COMMENT"] = "";
            } else {
                $model->field["COMMENT"] = "on";
            }
        }
        $extra  = ($model->field["COMMENT"] == "on") ? "checked" : "";
        $extra .= " id=\"COMMENT\"";
        $arg["el"]["COMMENT"] = knjCreateCheckBox($objForm, "COMMENT", "on", $extra, "");

        //出欠日数チェックボックス
        $extra  = ($model->field["tyousasyoNotPrintAnotherAttendrec"] == "on" || $model->cmd == "init" && "1" == $model->Properties["tyousasyoSetNotPrintAnotherAttendrec"]) ? "checked" : "";
        $extra .= " id=\"tyousasyoNotPrintAnotherAttendrec\"";
        $arg["el"]["tyousasyoNotPrintAnotherAttendrec"] = knjCreateCheckBox($objForm, "tyousasyoNotPrintAnotherAttendrec", "on", $extra, "");

        //近大チェック
        $kindai_flg = $db->getOne(knjg081aQuery::checkKindai());
        if (!$kindai_flg) {
            $arg["notKindai"] = '1';
        }

        //未履修科目出力ラジオボタン 1:する 2:しない
        $opt_mirisyu = array(1, 2);
        $model->field["MIRISYU"] = ($model->field["MIRISYU"] == "") ? "2" : $model->field["MIRISYU"];
        $extra = array("id=\"MIRISYU1\"", "id=\"MIRISYU2\"");
        $radioArray = knjCreateRadio($objForm, "MIRISYU", $model->field["MIRISYU"], $extra, $opt_mirisyu, get_count($opt_mirisyu));
        foreach($radioArray as $key => $val) $arg["el"][$key] = $val;

        //履修のみ科目出力ラジオボタン 1:する 2:しない
        $opt_risyu = array(1, 2);
        $model->field["RISYU"] = ($model->field["RISYU"] == "") ? "1" : $model->field["RISYU"];
        $extra = array("id=\"RISYU1\"", "id=\"RISYU2\"");
        $radioArray = knjCreateRadio($objForm, "RISYU", $model->field["RISYU"], $extra, $opt_risyu, get_count($opt_risyu));
        foreach($radioArray as $key => $val) $arg["el"][$key] = $val;

        //総合的な学習の時間の単位を０表示ラジオボタン 1:する 2:しない
        $opt_sougou = array(1, 2);
        $model->field["TANIPRINT_SOUGOU"] = ($model->field["TANIPRINT_SOUGOU"] == "") ? $model->Properties["tyousasyoTaniPrint"] : $model->field["TANIPRINT_SOUGOU"];
        $extra = array("id=\"TANIPRINT_SOUGOU1\"", "id=\"TANIPRINT_SOUGOU2\"");
        $radioArray = knjCreateRadio($objForm, "TANIPRINT_SOUGOU", $model->field["TANIPRINT_SOUGOU"], $extra, $opt_sougou, get_count($opt_sougou));
        foreach($radioArray as $key => $val) $arg["el"][$key] = $val;

        //留学の単位を０表示ラジオボタン 1:する 2:しない
        $opt_ryugaku = array(1, 2);
        $model->field["TANIPRINT_RYUGAKU"] = ($model->field["TANIPRINT_RYUGAKU"] == "") ? $model->Properties["tyousasyoTaniPrint"] : $model->field["TANIPRINT_RYUGAKU"];
        $extra = array("id=\"TANIPRINT_RYUGAKU1\"", "id=\"TANIPRINT_RYUGAKU2\"");
        $radioArray = knjCreateRadio($objForm, "TANIPRINT_RYUGAKU", $model->field["TANIPRINT_RYUGAKU"], $extra, $opt_ryugaku, get_count($opt_ryugaku));
        foreach($radioArray as $key => $val) $arg["el"][$key] = $val;

        //フォーム選択チェックボックス（6年用）
        $extra  = ($model->field["FORM6"] == "on") ? "checked" : "";
        $extra .= " id=\"FORM6\"";
        $arg["el"]["FORM6"] = knjCreateCheckBox($objForm, "FORM6", "on", $extra, "");

        // ○年用フォーム
        $arg["el"]["FORM6NAME"] = "{$model->Properties["nenYoForm"]}年用フォーム";

        //フォーム選択チェックボックス（指導上参考となる諸事欄、３分割フォーム）
        $extra  = ($model->field["useSyojikou3"] == "1" || ($model->cmd == "init" && $model->Properties["useSyojikou3"] == '1')) ? "checked" : "";
        $extra .= " id=\"useSyojikou3\"";
        $arg["el"]["useSyojikou3"] = knjCreateCheckBox($objForm, "useSyojikou3", "1", $extra, "");

        //校長印刷ラジオボタン 1:する 2:しない
        $opt_kotyo = array(1, 2);
        $model->field["KOTYO"] = ($model->field["KOTYO"] == "") ? "1" : $model->field["KOTYO"];
        $extra = array("id=\"KOTYO1\"", "id=\"KOTYO2\"");
        $radioArray = knjCreateRadio($objForm, "KOTYO", $model->field["KOTYO"], $extra, $opt_kotyo, get_count($opt_kotyo));
        foreach($radioArray as $key => $val) $arg["el"][$key] = $val;

        //記載（証明）日付
        if (!isset($model->field["DATE"]) && $model->Properties["tyousasyoCheckCertifDate"] != '1') {
            $model->field["DATE"] = $model->control["学籍処理日"];
        }
        $arg["el"]["DATE"]=View::popUpCalendar($objForm, "DATE", $model->field["DATE"]);

        //記載責任者コンボボックス
        $query = knjg081aQuery::getStaffList();
        makeCmbE070($objForm, $arg, $db, $query, "SEKI", $model->field["SEKI"], "", 1, $model);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $db, $model);

        //DB切断
        Query::dbCheckIn($db);

        //インラインフレーム
        $arg["IFRAME"] = VIEW::setIframeJs();

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjg081aForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="")
{
    $opt = array();
    $value_flg = false;
    if($blank != "") $opt[] = array('label' => "", 'value' => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {

    //更新ボタンを作成する
    $disabled = (AUTHORITY > DEF_REFER_RESTRICT) ? "" : " disabled";
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$disabled);
    //取消ボタンを作成する
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
    //データCSVボタン
    $extra = "onClick=\" wopen('".REQUESTROOT."/X/KNJX_G081/knjx_g081index.php?','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
    $arg["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ処理", $extra);

    //印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end2"] = knjCreateBtn($objForm, "btn_end2", "終 了", $extra);
}

//コンボ作成
function makeCmbE070(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $model) {
    $opt = array();
    if ($name == "SEKI") {
        $opt[0] = array();
    }
    list($simo, $fuseji) = explode(" | ", $model->Properties["showMaskStaffCd"]);
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        if ($name == "SEKI") {
            $ume = "" ;
            for ($umecnt = 1; $umecnt <= strlen($row["VALUE"]) - (int)$simo; $umecnt++) {
                $ume .= $fuseji;
            }
            if ($fuseji) {
                $SET_VALUE = $ume.substr($row["VALUE"], (strlen($row["VALUE"]) - (int)$simo), (int)$simo);
            } else {
                $SET_VALUE = $row["VALUE"];
            }
            $row["LABEL"] = str_replace($row["VALUE"], $SET_VALUE, $row["LABEL"]);
        }

        if ($name == "SEKI" && $model->Properties["tyousasyoPrintHomeRoomStaff"] == "1") {
            // ブランク
        } else {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "SEKI") {
        if ($model->Properties["tyousasyoPrintHomeRoomStaff"] == "1") {
            $value = "";
        } else {
            $value = ($value && $value_flg) ? $value : STAFFCD;
        }
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["el"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}


//hidden作成
function makeHidden(&$objForm, $db, $model) {

    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJG081A");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "OS", "1");
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "GAKKI", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
    knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
    knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
    knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
    knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);

    knjCreateHidden($objForm, "useCertifSchPrintCnt",              $model->Properties["useCertifSchPrintCnt"]);
    knjCreateHidden($objForm, "tyousasyoAttendrecRemarkFieldSize", $model->Properties["tyousasyoAttendrecRemarkFieldSize"]);
    knjCreateHidden($objForm, "gaihyouGakkaBetu",                  $model->Properties["gaihyouGakkaBetu"]);
    knjCreateHidden($objForm, "nenYoForm",                         $model->Properties["nenYoForm"]);
    knjCreateHidden($objForm, "train_ref_1_2_3_field_size",        $model->Properties["train_ref_1_2_3_field_size"]);
    knjCreateHidden($objForm, "train_ref_1_2_3_gyo_size",          $model->Properties["train_ref_1_2_3_gyo_size"]);
    knjCreateHidden($objForm, "tyousasyoSougouHyoukaNentani",      $model->Properties["tyousasyoSougouHyoukaNentani"]);
    knjCreateHidden($objForm, "tyousasyoTotalstudyactFieldSize",   $model->Properties["tyousasyoTotalstudyactFieldSize"]);
    knjCreateHidden($objForm, "tyousasyoTotalstudyvalFieldSize",   $model->Properties["tyousasyoTotalstudyvalFieldSize"]);
    knjCreateHidden($objForm, "tyousasyoSpecialactrecFieldSize",   $model->Properties["tyousasyoSpecialactrecFieldSize"]);
    knjCreateHidden($objForm, "tyousasyoTokuBetuFieldSize",        $model->Properties["tyousasyoTokuBetuFieldSize"]);
    knjCreateHidden($objForm, "tyousasyoEMPTokuBetuFieldSize",     $model->Properties["tyousasyoEMPTokuBetuFieldSize"]);
    knjCreateHidden($objForm, "tyousasyoKinsokuForm",              $model->Properties["tyousasyoKinsokuForm"]);
    knjCreateHidden($objForm, "tyousasyoNotPrintAnotherStudyrec",  $model->Properties["tyousasyoNotPrintAnotherStudyrec"]);
    knjCreateHidden($objForm, "tyousasyoNotPrintEnterGrade",       $model->Properties["tyousasyoNotPrintEnterGrade"]);
    knjCreateHidden($objForm, "tyousasyoSyusyokuPrintGappeiTougou",       $model->Properties["tyousasyoSyusyokuPrintGappeiTougou"]);
    knjCreateHidden($objForm, "tyousasyoHankiNintei",              $model->Properties["tyousasyoHankiNintei"]);
    knjCreateHidden($objForm, "useCurriculumcd",                   $model->Properties["useCurriculumcd"]);
    knjCreateHidden($objForm, "useClassDetailDat",                 $model->Properties["useClassDetailDat"]);
    knjCreateHidden($objForm, "useAddrField2" ,                    $model->Properties["useAddrField2"]);
    knjCreateHidden($objForm, "useProvFlg" ,                       $model->Properties["useProvFlg"]);
    knjCreateHidden($objForm, "useGakkaSchoolDiv" ,                $model->Properties["useGakkaSchoolDiv"]);
    knjCreateHidden($objForm, "useAssessCourseMst",                $model->Properties["useAssessCourseMst"]);
    knjCreateHidden($objForm, "useMaruA_avg",                      $model->Properties["useMaruA_avg"]);
    knjCreateHidden($objForm, "tyousasyoRemarkFieldSize",          $model->Properties["tyousasyoRemarkFieldSize"]);
    knjCreateHidden($objForm, "tyousasyoUseEditKinsoku",           $model->Properties["tyousasyoUseEditKinsoku"]);
    knjCreateHidden($objForm, "certifPrintRealName",               $model->Properties["certifPrintRealName"]);
    knjCreateHidden($objForm, "useTotalstudySlashFlg",             $model->Properties["useTotalstudySlashFlg"]);
    knjCreateHidden($objForm, "useAttendrecRemarkSlashFlg",        $model->Properties["useAttendrecRemarkSlashFlg"]);
    knjCreateHidden($objForm, "tyousasyoCheckCertifDate",          $model->Properties["tyousasyoCheckCertifDate"]);
    knjCreateHidden($objForm, "tyousasyoPrintHomeRoomStaff",       $model->Properties["tyousasyoPrintHomeRoomStaff"]);
    knjCreateHidden($objForm, "tyousasyoPrintCoursecodename",      $model->Properties["tyousasyoPrintCoursecodename"]);
    knjCreateHidden($objForm, "tyousasyoPrintChairSubclassSemester2", $model->Properties["tyousasyoPrintChairSubclassSemester2"]);
    knjCreateHidden($objForm, "tyousasyoHanasuClasscd",            $model->Properties["tyousasyoHanasuClasscd"]);

    //何年用のフォームを使うのかの初期値を判断する
    $query = knjg081aQuery::getSchoolDiv($model);
    $schooldiv = $db->getRow($query, DB_FETCHMODE_ASSOC);
    if ($schooldiv["SCHOOLDIV"] == '0') {
        if ($schooldiv["NEN"] == '0') {
            knjCreateHidden($objForm, "NENYOFORM_SYOKITI", '3');
        } else {
            knjCreateHidden($objForm, "NENYOFORM_SYOKITI", $schooldiv["NEN"]);
        }
    } else {
        if ($schooldiv["NEN"] == '0') {
            knjCreateHidden($objForm, "NENYOFORM_SYOKITI", '4');
        } else {
            knjCreateHidden($objForm, "NENYOFORM_SYOKITI", $schooldiv["NEN"]);
        }
    }
    knjCreateHidden($objForm, "NENYOFORM_CHECK", $model->Properties["nenYoForm"]);
    knjCreateHidden($objForm, "NENYOFORM");
}

?>
