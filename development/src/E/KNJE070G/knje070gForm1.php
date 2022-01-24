<?php
class knje070gForm1
{

    function main(&$model)
    {

        //オブジェクト作成
        $objForm =& new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("knje070gForm1", "POST", "knje070gindex.php", "", "knje070gForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期
        $arg["data"]["GAKKI"] = CTRL_SEMESTERNAME;

        //クラス選択コンボボックス
        $query = knje070gQuery::getAuth(CTRL_YEAR, CTRL_SEMESTER, $model);
        $extra = "onchange=\"return btn_submit('knje070g');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1, $model);

        //校種を取得
        $query = knje070gQuery::getSchoolKind(substr($model->field["GRADE_HR_CLASS"], 0, 2));
        $model->schoolKind = $db->getOne($query);

        //年組のMAX文字数取得
        $max_len = 0;
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $zenkaku = (strlen($row["LABEL"]) - mb_strlen($row["LABEL"])) / 2;
            $hankaku = ($zenkaku > 0) ? mb_strlen($row["LABEL"]) - $zenkaku : mb_strlen($row["LABEL"]);
            $max_len = ($zenkaku * 2 + $hankaku > $max_len) ? $zenkaku * 2 + $hankaku : $max_len;
        }

        //卒業可能学年チェック
        $query = knje070gQuery::checkGrdGrade(substr($model->field["GRADE_HR_CLASS"], 0, 2));
        $grdGrade = $db->getOne($query);

        if ($model->Properties["useProvFlg"] == 1 && $grdGrade > 0) {
            if ('' != $db->getOne(knje070gQuery::getTableColumn("STUDYREC_PROV_FLG_DAT", "PROV_TESTKINDCD"))) {
                //仮評定データ取得
                $prov = $db->getRow(knje070gQuery::getProvData($model), DB_FETCHMODE_ASSOC);
                //SCHREG_STUDYREC_DATの評定有チェック
                $valCnt = $db->getOne(knje070gQuery::getValuationCnt($model));

                if (strlen($prov["PROV_SEMESTER"]) > 0) {
                    $arg["data"]["PROV_INFO"] = '（ 仮評定：'.$prov["SEMESTERNAME"].'　'.$prov["TESTITEMNAME"].' ）';
                } elseif ($valCnt > 0) {
                    $arg["data"]["PROV_INFO"] = '（ 本評定 ）';
                }
            }
        }
        if ($model->Properties["useProvFlg"] == 1) {
            $arg["data"]["HYOTEI_KARI"] = '仮';
        }

        //リストToリスト作成
        makeListToList($objForm, $arg, $db, $model, $max_len);

        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        if ($model->Properties["tyousasyoHankiNintei"] == '1') {
            //調査書種類ラジオボタン 1:進学用（通年用） 1h:進学用（半期認定）2:就職用（通年用） 2h:就職用（半期認定）
            $arg["hanki"] = "1";
            $opt_output = array("1", "1h", "2", "2h");
            $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT1h\"", "id=\"OUTPUT2\"", "id=\"OUTPUT2h\"");
        } else {
            //調査書種類ラジオボタン 1:進学用 2:就職用
            $arg["notHanki"] = "1";
            $opt_output = array(1, 2);
            $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT2\"");
        }
        for ($i = 0; $i < count($opt_output); $i++) {
            $objForm->ae(array("type" => "radio",
                               "name" => "OUTPUT",
                               "value" => $model->field["OUTPUT"],
                               "extrahtml" => $extra[$i],
                               "multiple" => $opt_output));

            $arg["data"]["OUTPUT".$opt_output[$i]] = $objForm->ge("OUTPUT", $opt_output[$i]);
        }

        //漢字氏名印刷ラジオボタン 1:する 2:しない
        $opt_kanji = array(1, 2);
        $model->field["KANJI"] = ($model->field["KANJI"] == "") ? "1" : $model->field["KANJI"];
        $extra = array("id=\"KANJI1\"", "id=\"KANJI2\"");
        $radioArray = knjCreateRadio($objForm, "KANJI", $model->field["KANJI"], $extra, $opt_kanji, count($opt_kanji));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //その他住所を優先して印字チェックボックス
        $query = knje070gQuery::getSchoolDiv($model);
        $schooldiv = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $extra  = ($model->field["SONOTAJUUSYO"] == "on" || !$model->cmd && $schooldiv["IS_TUSIN"] == '1') ? "checked" : "";
        $extra .= " id=\"SONOTAJUUSYO\"";
        $arg["data"]["SONOTAJUUSYO"] = knjCreateCheckBox($objForm, "SONOTAJUUSYO", "on", $extra, "");

        //評定読替の項目を表示するかしないかのフラグ
        if (in_array($model->Properties["hyoteiYomikae"], array("1", "1_off"))) {
            $arg["hyoteiYomikae"] = '1';

            //評定読替チェックボックス
            if (!$model->cmd && $model->Properties["hyoteiYomikae"] == "1") {
                $model->field["HYOTEI"] = "on";
            }
            $extra  = $model->field["HYOTEI"] == "on" ? "checked" : "";
            $extra .= " id=\"HYOTEI\"";
            $arg["data"]["HYOTEI"] = knjCreateCheckBox($objForm, "HYOTEI", "on", $extra, "");
        }

        //学習成績概評チェックボックス
        if ($model->cmd == '') {
            if ($model->Properties["tyousasyoNotCheckGaihyoComment"] == '1') {
                $model->field["COMMENT"] = "";
            } else {
                $model->field["COMMENT"] = "on";
            }
        }
        $extra  = ($model->field["COMMENT"] == "on") ? "checked" : "";
        $extra .= " id=\"COMMENT\"";
        $arg["data"]["COMMENT"] = knjCreateCheckBox($objForm, "COMMENT", "on", $extra, "");

        //出欠日数チェックボックス
        $extra  = ($model->field["tyousasyoNotPrintAnotherAttendrec"] == "on" || !$model->cmd && "1" == $model->Properties["tyousasyoSetNotPrintAnotherAttendrec"]) ? "checked" : "";
        $extra .= " id=\"tyousasyoNotPrintAnotherAttendrec\"";
        $arg["data"]["tyousasyoNotPrintAnotherAttendrec"] = knjCreateCheckBox($objForm, "tyousasyoNotPrintAnotherAttendrec", "on", $extra, "");

        //近大チェック
        $kindai_flg = $db->getOne(knje070gQuery::checkKindai());
        if (!$kindai_flg) {
            $arg["notKindai"] = '1';
        }

        //未履修科目出力ラジオボタン 1:する 2:しない
        $opt_mirisyu = array(1, 2);
        $model->field["MIRISYU"] = ($model->field["MIRISYU"] == "") ? "2" : $model->field["MIRISYU"];
        $extra = array("id=\"MIRISYU1\"", "id=\"MIRISYU2\"");
        $radioArray = knjCreateRadio($objForm, "MIRISYU", $model->field["MIRISYU"], $extra, $opt_mirisyu, count($opt_mirisyu));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //履修のみ科目出力ラジオボタン 1:する 2:しない
        $opt_risyu = array(1, 2);
        $model->field["RISYU"] = ($model->field["RISYU"] == "") ? "1" : $model->field["RISYU"];
        $extra = array("id=\"RISYU1\"", "id=\"RISYU2\"");
        $radioArray = knjCreateRadio($objForm, "RISYU", $model->field["RISYU"], $extra, $opt_risyu, count($opt_risyu));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //総合的な学習の時間の単位を０表示ラジオボタン 1:する 2:しない
        $opt_sougou = array(1, 2);
        $model->field["TANIPRINT_SOUGOU"] = ($model->field["TANIPRINT_SOUGOU"] == "") ? $model->Properties["tyousasyoTaniPrint"] : $model->field["TANIPRINT_SOUGOU"];
        $extra = array("id=\"TANIPRINT_SOUGOU1\"", "id=\"TANIPRINT_SOUGOU2\"");
        $radioArray = knjCreateRadio($objForm, "TANIPRINT_SOUGOU", $model->field["TANIPRINT_SOUGOU"], $extra, $opt_sougou, count($opt_sougou));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //留学の単位を０表示ラジオボタン 1:する 2:しない
        $opt_ryugaku = array(1, 2);
        $model->field["TANIPRINT_RYUGAKU"] = ($model->field["TANIPRINT_RYUGAKU"] == "") ? $model->Properties["tyousasyoTaniPrint"] : $model->field["TANIPRINT_RYUGAKU"];
        $extra = array("id=\"TANIPRINT_RYUGAKU1\"", "id=\"TANIPRINT_RYUGAKU2\"");
        $radioArray = knjCreateRadio($objForm, "TANIPRINT_RYUGAKU", $model->field["TANIPRINT_RYUGAKU"], $extra, $opt_ryugaku, count($opt_ryugaku));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

//        if ($model->Properties["tyousasyoNoSelectNenYoForm"] != '1') {
//            $arg["no_tyousasyoNoSelectNenYoForm"] = "1";
//            //フォーム選択チェックボックス（6年用）
//            $extra  = ($model->field["FORM6"] == "on") ? "checked" : "";
//            $extra .= " id=\"FORM6\"";
//            if (!in_array($model->nenyoform_syokiti, array("3", "4"))) {
//                $extra .= " disabled=\"disabled\" ";
//            }
//            $arg["data"]["FORM6"] = knjCreateCheckBox($objForm, "FORM6", "on", $extra, "");
//
//            // ○年用フォーム
//            $arg["data"]["FORM6NAME"] = ($model->nenyoform_syokiti * 2)."年用フォーム";
//        }
//
//        if ($model->Properties["tyousasyoNoSelectUseSyojikou3"] != '1') {
//            $arg["no_tyousasyoNoSelectUseSyojikou3"] = "1";
//            //フォーム選択チェックボックス（指導上参考となる諸事欄、３分割フォーム）
//            $extra  = ($model->field["useSyojikou3"] == "1" || (!$model->cmd && $model->Properties["useSyojikou3"] == '1')) ? "checked" : "";
//            $extra .= " id=\"useSyojikou3\"";
//            $arg["data"]["useSyojikou3"] = knjCreateCheckBox($objForm, "useSyojikou3", "1", $extra, "");
//        }
//        if ($arg["no_tyousasyoNoSelectNenYoForm"] || $arg["no_tyousasyoNoSelectUseSyojikou3"]) {
//            $arg["form_select"] = "1";
//        }

        //校長印刷ラジオボタン 1:する 2:しない
        $opt_kotyo = array(1, 2);
        $model->field["KOTYO"] = ($model->field["KOTYO"] == "") ? "1" : $model->field["KOTYO"];
        $extra = array("id=\"KOTYO1\"", "id=\"KOTYO2\"");
        $radioArray = knjCreateRadio($objForm, "KOTYO", $model->field["KOTYO"], $extra, $opt_kotyo, count($opt_kotyo));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //記載（証明）日付
        if (!isset($model->field["DATE"]) && $model->Properties["tyousasyoCheckCertifDate"] != '1') {
            $model->field["DATE"] = $model->control["学籍処理日"];
        }
        $arg["el"]["DATE"]=View::popUpCalendar($objForm, "DATE", $model->field["DATE"]);

        //記載責任者コンボボックス
        $query = knje070gQuery::getStaffList();
        makeCmb($objForm, $arg, $db, $query, "SEKI", $model->field["SEKI"], "", 1, $model);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $db, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje070gForm1.html", $arg);
    }
}

//リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model, $max_len)
{

    $opt1 = $opt2 = array();
    //生徒一覧リストを作成する
    $query = knje070gQuery::getList($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //クラス名称調整
        $zenkaku = (strlen($row["HR_NAME"]) - mb_strlen($row["HR_NAME"])) / 2;
        $hankaku = ($zenkaku > 0) ? mb_strlen($row["HR_NAME"]) - $zenkaku : mb_strlen($row["HR_NAME"]);
        $len = $zenkaku * 2 + $hankaku;
        $hr_name = $row["HR_NAME"];
        for ($j=0; $j < ($max_len - ($zenkaku * 2 + $hankaku)); $j++) {
            $hr_name .= "&nbsp;";
        }
        if (!in_array($row["SCHREGNO"], $model->select_data["selectdata"])) {
            $opt1[]= array('label' => $row["SCHREGNO"].' '.$hr_name.' '.$row["ATTENDNO"].'番 '.$row["NAME_SHOW"],
                           'value' => $row["VALUE"]);
        }
    }
    $result->free();

    //生徒一覧リスト
    $extra = "multiple style=\"width:280px\" width=\"280px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "category_name", "", isset($opt1)?$opt1:array(), $extra, 15);

    //出力対象者リストを作成する
    if ($model->select_data["selectdata"][0]) {
        $query = knje070gQuery::getList($model, $model->select_data["selectdata"]);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //クラス名称調整
            $zenkaku = (strlen($row["HR_NAME"]) - mb_strlen($row["HR_NAME"])) / 2;
            $hankaku = ($zenkaku > 0) ? mb_strlen($row["HR_NAME"]) - $zenkaku : mb_strlen($row["HR_NAME"]);
            $len = $zenkaku * 2 + $hankaku;
            $hr_name = $row["HR_NAME"];
            for ($j=0; $j < ($max_len - ($zenkaku * 2 + $hankaku)); $j++) {
                $hr_name .= "&nbsp;";
            }

            $opt2[]= array('label' => $row["SCHREGNO"].' '.$hr_name.' '.$row["ATTENDNO"].'番 '.$row["NAME_SHOW"],
                           'value' => $row["VALUE"]);
        }
        $result->free();
    }

    //出力対象者一覧リスト
    $extra = "multiple style=\"width:280px\" width=\"280px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "category_selected", "", isset($opt2)?$opt2:array(), $extra, 15);

    //対象選択ボタン（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
    //対象取消ボタン（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    //対象選択ボタン（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    //対象取消ボタン（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $model)
{
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
            for ($umecnt = 1; $umecnt <= strlen($row["VALUE"]) - $simo; $umecnt++) {
                $ume .= $fuseji;
            }
            if ($fuseji) {
                $SET_VALUE = $ume.substr($row["VALUE"], (strlen($row["VALUE"]) - $simo), $simo);
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

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
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

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

//    if ($model->Properties["unUseCsvBtn_YorokuTyousa"] != "1" || $model->Properties["useKNJE070GJavaCsv"] == "1") {
//        //CSVボタン（useXLS=1 => 非表示）
//        if (!$model->Properties["useXLS"]) {
//            if ($model->Properties["useKNJE070GJavaCsv"] == "1") {
//                $extra = "onclick=\"return newwin('" . SERVLET_URL . "', 'csv');\"";
//            } else {
//                $extra = "onclick=\"return btn_submit('csv');\"";
//            }
//            $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);
//        }
//    }

    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $db, $model)
{

    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJE070G");
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

    knjCreateHidden($objForm, "useCertifSchPrintCnt", $model->Properties["useCertifSchPrintCnt"]);
    knjCreateHidden($objForm, "tyousasyoAttendrecRemarkFieldSize", $model->Properties["tyousasyoAttendrecRemarkFieldSize"]);
    knjCreateHidden($objForm, "gaihyouGakkaBetu", $model->Properties["gaihyouGakkaBetu"]);
    knjCreateHidden($objForm, "nenYoForm", $model->Properties["nenYoForm"]);
    knjCreateHidden($objForm, "train_ref_1_2_3_field_size", $model->Properties["train_ref_1_2_3_field_size"]);
    knjCreateHidden($objForm, "train_ref_1_2_3_gyo_size", $model->Properties["train_ref_1_2_3_gyo_size"]);
    knjCreateHidden($objForm, "tyousasyoSougouHyoukaNentani", $model->Properties["tyousasyoSougouHyoukaNentani"]);
    knjCreateHidden($objForm, "tyousasyoTotalstudyactFieldSize", $model->Properties["tyousasyoTotalstudyactFieldSize"]);
    knjCreateHidden($objForm, "tyousasyoTotalstudyvalFieldSize", $model->Properties["tyousasyoTotalstudyvalFieldSize"]);
    knjCreateHidden($objForm, "tyousasyoSpecialactrecFieldSize", $model->Properties["tyousasyoSpecialactrecFieldSize"]);
    knjCreateHidden($objForm, "tyousasyoTokuBetuFieldSize", $model->Properties["tyousasyoTokuBetuFieldSize"]);
    knjCreateHidden($objForm, "tyousasyoEMPTokuBetuFieldSize", $model->Properties["tyousasyoEMPTokuBetuFieldSize"]);
    knjCreateHidden($objForm, "tyousasyoKinsokuForm", $model->Properties["tyousasyoKinsokuForm"]);
    knjCreateHidden($objForm, "tyousasyoNotPrintAnotherStudyrec", $model->Properties["tyousasyoNotPrintAnotherStudyrec"]);
    knjCreateHidden($objForm, "tyousasyoNotPrintEnterGrade", $model->Properties["tyousasyoNotPrintEnterGrade"]);
    knjCreateHidden($objForm, "tyousasyoSyusyokuPrintGappeiTougou", $model->Properties["tyousasyoSyusyokuPrintGappeiTougou"]);
    knjCreateHidden($objForm, "tyousasyoHankiNintei", $model->Properties["tyousasyoHankiNintei"]);
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
    knjCreateHidden($objForm, "useClassDetailDat", $model->Properties["useClassDetailDat"]);
    knjCreateHidden($objForm, "useAddrField2", $model->Properties["useAddrField2"]);
    knjCreateHidden($objForm, "useProvFlg", $model->Properties["useProvFlg"]);
    knjCreateHidden($objForm, "useGakkaSchoolDiv", $model->Properties["useGakkaSchoolDiv"]);
    knjCreateHidden($objForm, "useAssessCourseMst", $model->Properties["useAssessCourseMst"]);
    knjCreateHidden($objForm, "useMaruA_avg", $model->Properties["useMaruA_avg"]);
    knjCreateHidden($objForm, "tyousasyoRemarkFieldSize", $model->Properties["tyousasyoRemarkFieldSize"]);
    knjCreateHidden($objForm, "tyousasyoUseEditKinsoku", $model->Properties["tyousasyoUseEditKinsoku"]);
    knjCreateHidden($objForm, "certifPrintRealName", $model->Properties["certifPrintRealName"]);
    knjCreateHidden($objForm, "useTotalstudySlashFlg", $model->Properties["useTotalstudySlashFlg"]);
    knjCreateHidden($objForm, "useAttendrecRemarkSlashFlg", $model->Properties["useAttendrecRemarkSlashFlg"]);
    knjCreateHidden($objForm, "tyousasyoCheckCertifDate", $model->Properties["tyousasyoCheckCertifDate"]);
    knjCreateHidden($objForm, "tyousasyoPrintHomeRoomStaff", $model->Properties["tyousasyoPrintHomeRoomStaff"]);
    knjCreateHidden($objForm, "tyousasyoPrintCoursecodename", $model->Properties["tyousasyoPrintCoursecodename"]);
    knjCreateHidden($objForm, "tyousasyoPrintChairSubclassSemester2", $model->Properties["tyousasyoPrintChairSubclassSemester2"]);
    knjCreateHidden($objForm, "tyousasyoHanasuClasscd", $model->Properties["tyousasyoHanasuClasscd"]);
    knjCreateHidden($objForm, "tyousasyoJiritsuKatsudouRemark", $model->Properties["tyousasyoJiritsuKatsudouRemark"]);
    knjCreateHidden($objForm, "tyousasyoSougouHyoukaNentaniPrintCombined", $model->Properties["tyousasyoSougouHyoukaNentaniPrintCombined"]);
    knjCreateHidden($objForm, "tyousasyo2020", "1");
    knjCreateHidden($objForm, "tyousasyo2020shojikouExtends", $model->Properties["tyousasyo2020GshojikouExtends"]);

    knjCreateHidden($objForm, "NENYOFORM_SYOKITI", $model->nenyoform_syokiti);
    knjCreateHidden($objForm, "NENYOFORM_CHECK", $model->nenyoform_syokiti * 2);
    knjCreateHidden($objForm, "NENYOFORM");
    if ($model->Properties["tyousasyoNoSelectUseSyojikou3"] == '1') {
        knjCreateHidden($objForm, "useSyojikou3", "1");
    }
}
