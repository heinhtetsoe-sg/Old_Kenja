<?php

require_once('for_php7.php');

class knjf010jSubForm2
{
    public function main(&$model)
    {
        $objForm = new form();
        $arg["start"] = $objForm->get_start("sel", "POST", "knjf010jindex.php", "", "sel");

        //生徒一覧
        $opt_left = $opt_right = array();
        //置換処理選択時の生徒の情報
        $array = explode(",", $model->replace_data["selectdata"]);
        if ($array[0] == "") {
            $array[0] = $model->schregno;
        }
        //生徒情報
        if (!isset($model->warning) && isset($model->schregno)) {
            $RowH = knjf010jQuery::getMedexamHdat($model);      //生徒健康診断ヘッダデータ取得
            $RowD = knjf010jQuery::getMedexamDetDat($model);   //生徒健康診断詳細データ取得
        } else {
            $RowD =& $model->replace_data["det_field"];
            $RowH =& $model->replace_data["det_field"];
        }

        $db = Query::dbCheckOut();

        //結核の間接撮影、所見、その他検査 非表示
        if ($model->school_kind == "H") {
            $arg["tbFilmShow"] = 1;
        } else {
            $arg["tbFilmUnShow"] = 1;
        }

        if ($model->isMie) {
            $arg["is_mie"] = "1";
            $arg["is_not_mie"] = "";
        } else {
            $arg["is_mie"] = "";
            $arg["is_not_mie"] = "1";
        }

        //未検査項目設定
        $notFieldSet = $sep = "";
        $query = knjf010jQuery::getMedexamDetNotExaminedDat($model);
        $result = $db->query($query);
        while ($notExamined = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            foreach ($notExamined as $field => $val) {
                if ($field == "YEAR" || $field == "GRADE") {
                    continue;
                }

                if ($val == "1") {
                    $notFieldSet .= $sep.$field;
                    $sep = ":";
                }
            }
        }
        if ($notFieldSet != "") {
            $arg["setNotExamined"] = "setNotExamined('{$notFieldSet}')";
        }

        //チェックボックス
        for ($i = 0; $i < 23; $i++) {
            if ($i == 22) {
                $extra = "onClick=\"return check_all(this);\"";
                if ($model->replace_data["check"][$i] == "1") {
                    $extra .= "checked = 'checked'";
                } else {
                    $extra .= "";
                }
                $arg["data"]["RCHECK".$i] = knjCreateCheckBox($objForm, "RCHECK".$i, "1", $extra);

                knjCreateHidden($objForm, "CHECK_ALL", "RCHECK".$i);
            } else {
                $extra = "";
                if ($model->replace_data["check"][$i] == "1") {
                    $extra .= "checked = 'checked'";
                } else {
                    $extra .= "";
                }
                $arg["data"]["RCHECK".$i] = knjCreateCheckBox($objForm, "RCHECK".$i, "1", $extra);
            }
        }
        /* 編集項目 */
        //栄養状態コンボ
        $nutrInfo = $sep = '';
        $result     = $db->query(knjf010jQuery::getNutrition($model, ""));
        $opt        = array();
        $opt[]      = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $namecd = substr($row["NAMECD2"], 0, 2);
            $opt[] = array("label" => $namecd."  ".$row["NAME1"],
                           "value" => $row["NAMECD2"]);

            if ($row["ABBV1"] == '' && $row["ABBV2"] == '') {
            } else {
                $nutrInfo .= $sep.$row["NAMECD2"].':'.$row["ABBV1"].':'.$row["ABBV2"];
                $sep = ',';
            }
        }
        knjCreateHidden($objForm, "nutrInfo", $nutrInfo);
        $result->free();
        $disableCodes = getDisabledCodes($db, "F030");

        $extra = "style=\"width:200px;\" onChange=\"syokenNyuryoku(this, document.forms[0].NUTRITIONCD_REMARK, '".implode(",", $disableCodes)."')\"";
        $arg["data"]["NUTRITIONCD"] = knjCreateCombo($objForm, "NUTRITIONCD", $RowD["NUTRITIONCD"], $opt, $extra, 1);

        //栄養状態テキスト
        if (in_array($RowD["NUTRITIONCD"], $disableCodes) == true || $RowD["NUTRITIONCD"] == '') {
            $extra = "disabled";
        } else {
            $extra = "";
        }
        $arg["data"]["NUTRITIONCD_REMARK"] = knjCreateTextBox($objForm, $RowD["NUTRITIONCD_REMARK"], "NUTRITIONCD_REMARK", 40, 20, $extra);

        //目の疾病及び異常コンボ
        $result     = $db->query(knjf010jQuery::getNameMst($model, "F050"));
        $opt        = array();
        $opt[]      = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
        }
        $result->free();
        $disableCodes = getDisabledCodes($db, "F050");
        $extra = "style=\"width:200px;\" onChange=\"syokenNyuryoku(this, document.forms[0].EYE_TEST_RESULT, '".implode(",", $disableCodes)."')\"";
        $arg["data"]["EYEDISEASECD"] = knjCreateCombo($objForm, "EYEDISEASECD", $RowD["EYEDISEASECD"], $opt, $extra.$entMove, 1);

        //目の疾病及び異常テキスト
        if (in_array($RowD["EYEDISEASECD"], $disableCodes) == true || $RowD["EYEDISEASECD"] == '') {
            $extra = "disabled";
        } else {
            $extra = "";
        }
        $arg["data"]["EYE_TEST_RESULT"] = knjCreateTextBox($objForm, $RowD["EYE_TEST_RESULT"], "EYE_TEST_RESULT", 40, 20, $extra.$entMove);

        //色覚異常コンボ
        $opt        = array();
        $opt[]      = $optnull;
        $result     = $db->query(knjf010jQuery::getNameMst($model, "F051"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
        }
        $result->free();
        $disableCodes = getDisabledCodes($db, "F051");
        $extra = "style=\"width:200px;\" onChange=\"syokenNyuryoku(this, document.forms[0].VISION_CANTMEASURE, '".implode(",", $disableCodes)."')\"";
        $arg["data"]["EYEDISEASECD5"] = knjCreateCombo($objForm, "EYEDISEASECD5", $RowD["EYEDISEASECD5"], $opt, $extra.$entMove, 1);

        //色覚異常(所見)
        if (in_array($RowD["EYEDISEASECD5"], $disableCodes) == true || $RowD["EYEDISEASECD5"] == '') {
            $extra = "disabled";
        } else {
            $extra = "";
        }
        $arg["data"]["VISION_CANTMEASURE"] = knjCreateTextBox($objForm, $RowD["VISION_CANTMEASURE"], "VISION_CANTMEASURE", 40, 20, $extra.$entMove);

        //脊柱・胸郭・四肢(全般)コンボ
        $result     = $db->query(knjf010jQuery::getNameMst($model, "F040"));
        $opt        = array();
        $opt[]      = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
        }
        $result->free();
        $disableCodes = getDisabledCodes($db, "F040");
        $target_objs = "SPINERIBCD1,SPINERIBCD2,SPINERIBCD3,SPINERIBCD_REMARK,SPINERIBCD_REMARK1,SPINERIBCD_REMARK2,SPINERIBCD_REMARK3";
        $text_objs   = "SPINERIBCD_REMARK1,SPINERIBCD_REMARK2,SPINERIBCD_REMARK3";
        $combo_objs   = "SPINERIBCD1,SPINERIBCD2,SPINERIBCD3";

        $extra = "style=\"width:200px;\" onChange=\"syokenNyuryoku2(this, '".$target_objs."','".implode(",", $disableCodes)."', '".$text_objs."','".implode(",", getDisabledCodes($db, "F041"))."', '".$combo_objs."')\"";
        $arg["data"]["SPINERIBCD"] = knjCreateCombo($objForm, "SPINERIBCD", $RowD["SPINERIBCD"], $opt, $extra.$entMove, 1);

        //脊柱・胸郭・四肢(疾患)コンボ
        $result     = $db->query(knjf010jQuery::getNameMst($model, "F041"));
        $opt        = array();
        $opt[]      = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
        }
        $result->free();
        //脊柱・胸郭・四肢(全般コンボが未選択、未受検、異常なしのとき)
        $disableCodes = getDisabledCodes($db, "F041");
        if (in_array($RowD["SPINERIBCD"], getDisabledCodes($db, "F040")) == true || $RowD["SPINERIBCD"] == '') {
            $extra = "style=\"width:200px;\" disabled onChange=\"syokenNyuryoku(this, document.forms[0].SPINERIBCD_REMARK1, '".implode(",", $disableCodes)."')\"";
            $arg["data"]["SPINERIBCD1"] = knjCreateCombo($objForm, "SPINERIBCD1", $RowD["SPINERIBCD1"], $opt, $extra.$entMove, 1);

            $extra = "style=\"width:200px;\" disabled onChange=\"syokenNyuryoku(this, document.forms[0].SPINERIBCD_REMARK2, '".implode(",", $disableCodes)."')\"";
            $arg["data"]["SPINERIBCD2"] = knjCreateCombo($objForm, "SPINERIBCD2", $RowD["SPINERIBCD2"], $opt, $extra.$entMove, 1);

            $extra = "style=\"width:200px;\" disabled onChange=\"syokenNyuryoku(this, document.forms[0].SPINERIBCD_REMARK3, '".implode(",", $disableCodes)."')\"";
            $arg["data"]["SPINERIBCD3"] = knjCreateCombo($objForm, "SPINERIBCD3", $RowD["SPINERIBCD3"], $opt, $extra.$entMove, 1);
        } else {
            //それ以外の時
            $extra = "style=\"width:200px;\" onChange=\"syokenNyuryoku(this, document.forms[0].SPINERIBCD_REMARK1, '".implode(",", $disableCodes)."')\"";
            $arg["data"]["SPINERIBCD1"] = knjCreateCombo($objForm, "SPINERIBCD1", $RowD["SPINERIBCD1"], $opt, $extra.$entMove, 1);

            $extra = "style=\"width:200px;\" onChange=\"syokenNyuryoku(this, document.forms[0].SPINERIBCD_REMARK2, '".implode(",", $disableCodes)."')\"";
            $arg["data"]["SPINERIBCD2"] = knjCreateCombo($objForm, "SPINERIBCD2", $RowD["SPINERIBCD2"], $opt, $extra.$entMove, 1);

            $extra = "style=\"width:200px;\" onChange=\"syokenNyuryoku(this, document.forms[0].SPINERIBCD_REMARK3, '".implode(",", $disableCodes)."')\"";
            $arg["data"]["SPINERIBCD3"] = knjCreateCombo($objForm, "SPINERIBCD3", $RowD["SPINERIBCD3"], $opt, $extra.$entMove, 1);
        }

        //脊柱・胸郭・四肢テキスト
        //疾患1テキスト
        if (in_array($RowD["SPINERIBCD1"], $disableCodes) == true || $RowD["SPINERIBCD1"] == '') {
            $extra = "disabled";
        } else {
            $extra = "";
        }
        $arg["data"]["SPINERIBCD_REMARK1"] = knjCreateTextBox($objForm, $RowD["SPINERIBCD_REMARK1"], "SPINERIBCD_REMARK1", 40, 20, $extra.$entMove);

        //疾患2テキスト
        if (in_array($RowD["SPINERIBCD2"], $disableCodes) == true || $RowD["SPINERIBCD2"] == '') {
            $extra = "disabled";
        } else {
            $extra = "";
        }
        $arg["data"]["SPINERIBCD_REMARK2"] = knjCreateTextBox($objForm, $RowD["SPINERIBCD_REMARK2"], "SPINERIBCD_REMARK2", 40, 20, $extra.$entMove);

        //疾患3テキスト
        if (in_array($RowD["SPINERIBCD3"], $disableCodes) == true || $RowD["SPINERIBCD3"] == '') {
            $extra = "disabled";
        } else {
            $extra = "";
        }
        $arg["data"]["SPINERIBCD_REMARK3"] = knjCreateTextBox($objForm, $RowD["SPINERIBCD_REMARK3"], "SPINERIBCD_REMARK3", 40, 20, $extra.$entMove);

        //脊柱・胸郭・四肢の全般がdisabled設定のとき
        if (in_array($RowD["SPINERIBCD"], getDisabledCodes($db, "F040")) == true || $RowD["SPINERIBCD"] == '') {
            $extra = "disabled";
            $arg["data"]["SPINERIBCD_REMARK1"] = knjCreateTextBox($objForm, $RowD["SPINERIBCD_REMARK1"], "SPINERIBCD_REMARK1", 40, 20, $extra.$entMove);
            $arg["data"]["SPINERIBCD_REMARK2"] = knjCreateTextBox($objForm, $RowD["SPINERIBCD_REMARK2"], "SPINERIBCD_REMARK2", 40, 20, $extra.$entMove);
            $arg["data"]["SPINERIBCD_REMARK3"] = knjCreateTextBox($objForm, $RowD["SPINERIBCD_REMARK3"], "SPINERIBCD_REMARK3", 40, 20, $extra.$entMove);
        } else {
            $extra = "";
        }
        $arg["data"]["SPINERIBCD_REMARK"]  = knjCreateTextBox($objForm, $RowD["SPINERIBCD_REMARK"], "SPINERIBCD_REMARK", 40, 20, $extra.$entMove);

        //耳鼻咽頭疾患コンボ
        $result     = $db->query(knjf010jQuery::getNameMst($model, "F060"));
        $opt        = array();
        $opt[]      = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
        }
        $result->free();
        $disableCodes = getDisabledCodes($db, "F060");
        $target_objs = "NOSEDISEASECD5,NOSEDISEASECD6,NOSEDISEASECD7,NOSEDISEASECD_REMARK,NOSEDISEASECD_REMARK1,NOSEDISEASECD_REMARK2,NOSEDISEASECD_REMARK3";
        $text_objs   = "NOSEDISEASECD_REMARK1,NOSEDISEASECD_REMARK2,NOSEDISEASECD_REMARK3";
        $combo_objs  = "NOSEDISEASECD5,NOSEDISEASECD6,NOSEDISEASECD6";

        $extra = "style=\"width:200px;\" onChange=\"syokenNyuryoku2(this, '".$target_objs."','".implode(",", $disableCodes)."', '".$text_objs."','".implode(",", getDisabledCodes($db, "F061"))."', '".$combo_objs."')\"";
        $arg["data"]["NOSEDISEASECD"] = knjCreateCombo($objForm, "NOSEDISEASECD", $RowD["NOSEDISEASECD"], $opt, $extra.$entMove, 1);

        //耳鼻咽頭疾患コンボ
        $result     = $db->query(knjf010jQuery::getNameMst($model, "F061"));
        $opt        = array();
        $opt[]      = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
        }
        $result->free();

        //耳鼻咽喉疾患(全般コンボが未選択、未受検、異常なしのとき)
        $disableCodes = getDisabledCodes($db, "F061");
        if (in_array($RowD["NOSEDISEASECD"], getDisabledCodes($db, "F060")) == true || $RowD["NOSEDISEASECD"] == '') {
            $extra = "style=\"width:200px;\" disabled onChange=\"syokenNyuryoku(this, document.forms[0].NOSEDISEASECD_REMARK1, '".implode(",", $disableCodes)."')\"";
            $arg["data"]["NOSEDISEASECD5"] = knjCreateCombo($objForm, "NOSEDISEASECD5", $RowD["NOSEDISEASECD1"], $opt, $extra.$entMove, 1);

            $extra = "style=\"width:200px;\" disabled onChange=\"syokenNyuryoku(this, document.forms[0].NOSEDISEASECD_REMARK2, '".implode(",", $disableCodes)."')\"";
            $arg["data"]["NOSEDISEASECD6"] = knjCreateCombo($objForm, "NOSEDISEASECD6", $RowD["NOSEDISEASECD6"], $opt, $extra.$entMove, 1);

            $extra = "style=\"width:200px;\" disabled onChange=\"syokenNyuryoku(this, document.forms[0].NOSEDISEASECD_REMARK3, '".implode(",", $disableCodes)."')\"";
            $arg["data"]["NOSEDISEASECD7"] = knjCreateCombo($objForm, "NOSEDISEASECD7", $RowD["NOSEDISEASECD7"], $opt, $extra.$entMove, 1);
        } else {
            //それ以外の時
            $extra = "style=\"width:200px;\" onChange=\"syokenNyuryoku(this, document.forms[0].NOSEDISEASECD_REMARK1, '".implode(",", $disableCodes)."')\"";
            $arg["data"]["NOSEDISEASECD5"] = knjCreateCombo($objForm, "NOSEDISEASECD5", $RowD["NOSEDISEASECD5"], $opt, $extra.$entMove, 1);

            $extra = "style=\"width:200px;\" onChange=\"syokenNyuryoku(this, document.forms[0].NOSEDISEASECD_REMARK2, '".implode(",", $disableCodes)."')\"";
            $arg["data"]["NOSEDISEASECD6"] = knjCreateCombo($objForm, "NOSEDISEASECD6", $RowD["NOSEDISEASECD6"], $opt, $extra.$entMove, 1);

            $extra = "style=\"width:200px;\" onChange=\"syokenNyuryoku(this, document.forms[0].NOSEDISEASECD_REMARK3, '".implode(",", $disableCodes)."')\"";
            $arg["data"]["NOSEDISEASECD7"] = knjCreateCombo($objForm, "NOSEDISEASECD7", $RowD["NOSEDISEASECD7"], $opt, $extra.$entMove, 1);
        }

        //耳鼻咽喉疾患テキスト
        //疾患1テキスト
        if (in_array($RowD["NOSEDISEASECD5"], $disableCodes) == true || $RowD["NOSEDISEASECD5"] == '') {
            $extra = "disabled";
        } else {
            $extra = "";
        }
        $arg["data"]["NOSEDISEASECD_REMARK1"] = knjCreateTextBox($objForm, $RowD["NOSEDISEASECD_REMARK1"], "NOSEDISEASECD_REMARK1", 40, 20, $extra.$entMove);

        //疾患2テキスト
        if (in_array($RowD["NOSEDISEASECD6"], $disableCodes) == true || $RowD["NOSEDISEASECD6"] == '') {
            $extra = "disabled";
        } else {
            $extra = "";
        }
        $arg["data"]["NOSEDISEASECD_REMARK2"] = knjCreateTextBox($objForm, $RowD["NOSEDISEASECD_REMARK2"], "NOSEDISEASECD_REMARK2", 40, 20, $extra.$entMove);

        //疾患3テキスト
        if (in_array($RowD["NOSEDISEASECD7"], $disableCodes) == true || $RowD["NOSEDISEASECD7"] == '') {
            $extra = "disabled";
        } else {
            $extra = "";
        }
        $arg["data"]["NOSEDISEASECD_REMARK3"] = knjCreateTextBox($objForm, $RowD["NOSEDISEASECD_REMARK3"], "NOSEDISEASECD_REMARK3", 40, 20, $extra.$entMove);

        //耳鼻咽喉疾患の全般がdisabled設定のとき
        if (in_array($RowD["NOSEDISEASECD"], getDisabledCodes($db, "F060")) == true || $RowD["NOSEDISEASECD"] == '') {
            $extra = "disabled";
            $arg["data"]["NOSEDISEASECD_REMARK1"] = knjCreateTextBox($objForm, $RowD["NOSEDISEASECD_REMARK1"], "NOSEDISEASECD_REMARK1", 40, 20, $extra.$entMove);
            $arg["data"]["NOSEDISEASECD_REMARK2"] = knjCreateTextBox($objForm, $RowD["NOSEDISEASECD_REMARK2"], "NOSEDISEASECD_REMARK2", 40, 20, $extra.$entMove);
            $arg["data"]["NOSEDISEASECD_REMARK3"] = knjCreateTextBox($objForm, $RowD["NOSEDISEASECD_REMARK3"], "NOSEDISEASECD_REMARK3", 40, 20, $extra.$entMove);
        } else {
            $extra = "";
        }
        $arg["data"]["NOSEDISEASECD_REMARK"] = knjCreateTextBox($objForm, $RowD["NOSEDISEASECD_REMARK"], "NOSEDISEASECD_REMARK", 40, 20, $extra.$entMove);

        //皮膚疾患コンボ
        $result     = $db->query(knjf010jQuery::getNameMst($model, "F070"));
        $opt        = array();
        $opt[]      = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
        }
        $result->free();

        $disableCodes = getDisabledCodes($db, "F070");
        $extra = "style=\"width:200px;\" onChange=\"syokenNyuryoku(this, document.forms[0].SKINDISEASECD_REMARK, '".implode(",", $disableCodes)."')\"";
        $arg["data"]["SKINDISEASECD"] = knjCreateCombo($objForm, "SKINDISEASECD", $RowD["SKINDISEASECD"], $opt, $extra.$entMove, 1);

        //皮膚疾患テキスト
        if (in_array($RowD["SKINDISEASECD"], $disableCodes) == true || $RowD["SKINDISEASECD"] == '') {
            $extra = "disabled";
        } else {
            $extra = "";
        }
        $arg["data"]["SKINDISEASECD_REMARK"] = knjCreateTextBox($objForm, $RowD["SKINDISEASECD_REMARK"], "SKINDISEASECD_REMARK", 40, 20, $extra.$entMove);

        //結核・撮影日付
        $RowD["TB_FILMDATE"] = str_replace("-", "/", $RowD["TB_FILMDATE"]);
        $arg["data"]["TB_FILMDATE"] = View::popUpCalendar($objForm, "TB_FILMDATE", $RowD["TB_FILMDATE"]);

        //結核・所見コンボ
        $result     = $db->query(knjf010jQuery::getNameMst($model, "F100"));
        $opt        = array();
        $opt[]      = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
        }
        $result->free();

        $disableCodes = getDisabledCodes($db, "F100");
        $extra = "style=\"width:200px;\" onChange=\"syokenNyuryoku(this, document.forms[0].TB_X_RAY, '".implode(",", $disableCodes)."')\"";
        $arg["data"]["TB_REMARKCD"] = knjCreateCombo($objForm, "TB_REMARKCD", $RowD["TB_REMARKCD"], $opt, $extra.$entMove, 1);

        //結核・所見テキストボックス
        if (in_array($RowD["TB_REMARKCD"], $disableCodes) == true || $RowD["TB_REMARKCD"] == '') {
            $extra = "disabled";
        } else {
            $extra = "";
        }
        $arg["data"]["TB_X_RAY"] = knjCreateTextBox($objForm, $RowD["TB_X_RAY"], "TB_X_RAY", 40, 20, $extra.$entMove);

        //結核・再検査・撮影日付
        $RowD["TB_RE_EXAMINATION_DATE"] = str_replace("-", "/", $RowD["TB_RE_EXAMINATION_DATE"]);
        $arg["data"]["TB_RE_EXAMINATION_DATE"] = View::popUpCalendar($objForm, "TB_RE_EXAMINATION_DATE", $RowD["TB_RE_EXAMINATION_DATE"]);

        //結核・再検査・結果
        $result     = $db->query(knjf010jQuery::getNameMst($model, "F101"));
        $opt        = array();
        $opt[]      = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
        }
        $result->free();
        $extra = "";
        $arg["data"]["TB_RE_EXAMINATION_RESULT"] = knjCreateCombo($objForm, "TB_RE_EXAMINATION_RESULT", $RowD["TB_RE_EXAMINATION_RESULT"], $opt, $extra.$entMove, 1);

        //結核・その他検査コンボ
        $result     = $db->query(knjf010jQuery::getNameMst($model, "F110"));
        $opt        = array();
        $opt[]      = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
        }
        $result->free();
        $extra = "";
        $arg["data"]["TB_OTHERTESTCD"] = knjCreateCombo($objForm, "TB_OTHERTESTCD", $RowD["TB_OTHERTESTCD"], $opt, $extra.$entMove, 1);

        //結核・疾病及び異常コンボ
        $result     = $db->query(knjf010jQuery::getNameMst($model, "F120"));
        $opt        = array();
        $opt[]      = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
        }
        $result->free();

        $disableCodes = getDisabledCodes($db, "F120");
        $extra = "style=\"width:100px;\" onChange=\"syokenNyuryoku(this, document.forms[0].TB_NAME_REMARK1, '".implode(",", $disableCodes)."')\"";
        $arg["data"]["TB_NAMECD"] = knjCreateCombo($objForm, "TB_NAMECD", $RowD["TB_NAMECD"], $opt, $extra.$entMove, 1);

        //結核・疾病及び異常テキスト
        if (in_array($RowD["TB_NAMECD"], $disableCodes) == true || $RowD["TB_NAMECD"] == '') {
            $extra = "disabled";
        } else {
            $extra = "";
        }
        $arg["data"]["TB_NAME_REMARK1"] = knjCreateTextBox($objForm, $RowD["TB_NAME_REMARK1"], "TB_NAME_REMARK1", 40, 20, $extra.$entMove);

        //結核・指導区分コンボ
        $result     = $db->query(knjf010jQuery::getNameMst($model, "F130"));
        $opt        = array();
        $opt[]      = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
        }
        $result->free();
        $extra = "style=\"width:150px;\"";
        $arg["data"]["TB_ADVISECD"] = knjCreateCombo($objForm, "TB_ADVISECD", $RowD["TB_ADVISECD"], $opt, $extra.$entMove, 1);

        //心臓・臨床医学的検査コンボ
        $result     = $db->query(knjf010jQuery::getNameMst($model, "F080"));
        $opt        = array();
        $opt[]      = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
        }
        $result->free();
        $disableCodes = getDisabledCodes($db, "F080");

        $extra = "style=\"width:200px;\" onChange=\"syokenNyuryoku(this, document.forms[0].HEART_MEDEXAM_REMARK, '".implode(",", $disableCodes)."')\"";
        $arg["data"]["HEART_MEDEXAM"] = knjCreateCombo($objForm, "HEART_MEDEXAM", $RowD["HEART_MEDEXAM"], $opt, $extra.$entMove, 1);

        //心臓・臨床医学的検査テキスト
        if (in_array($RowD["HEART_MEDEXAM"], $disableCodes) == true || $RowD["HEART_MEDEXAM"] == '') {
            $extra = "disabled";
        } else {
            $extra = "";
        }
        $arg["data"]["HEART_MEDEXAM_REMARK"] = knjCreateTextBox($objForm, $RowD["HEART_MEDEXAM_REMARK"], "HEART_MEDEXAM_REMARK", 80, 40, $extra.$entMove);

        if ($model->isKoma) {
            $arg["is_koma"] = "1";
            $extra = "onblur=\"return Num_Check(this);\"";
            $arg["data"]["HEART_GRAPH_NO"] = knjCreateTextBox($objForm, $RowD["HEART_GRAPH_NO"], "HEART_GRAPH_NO", 12, 12, $extra.$entMove);
        }

        //心臓・精密検査コンボ
        $result     = $db->query(knjf010jQuery::getNameMst($model, "F091"));
        $opt        = array();
        $opt[]      = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
        }
        $result->free();

        $disableCodes = getDisabledCodes($db, "F091");
        $extra = "style=\"width:200px;\" onChange=\"syokenNyuryoku(this, document.forms[0].MANAGEMENT_REMARK, '".implode(",", $disableCodes)."')\"";
        $arg["data"]["MANAGEMENT_DIV"] = knjCreateCombo($objForm, "MANAGEMENT_DIV", $RowD["MANAGEMENT_DIV"], $opt, $extra.$entMove, 1);

        //心臓・精密検査テキスト
        if (in_array($RowD["MANAGEMENT_DIV"], $disableCodes) == true || $RowD["MANAGEMENT_DIV"] == '') {
            $extra = "disabled";
        } else {
            $extra = "";
        }
        $arg["data"]["MANAGEMENT_REMARK"] = knjCreateTextBox($objForm, $RowD["MANAGEMENT_REMARK"], "MANAGEMENT_REMARK", 80, 40, $extra.$entMove);

        //心臓・疾病及び異常コンボ
        $result     = $db->query(knjf010jQuery::getNameMst($model, "F090"));
        $opt        = array();
        $opt[]      = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
        }
        $result->free();
        $disableCodes = getDisabledCodes($db, "F090");
        $extra = "style=\"width:250px;\" onChange=\"syokenNyuryoku(this, document.forms[0].HEARTDISEASECD_REMARK, '".implode(",", $disableCodes)."')\"";
        $arg["data"]["HEARTDISEASECD"] = knjCreateCombo($objForm, "HEARTDISEASECD", $RowD["HEARTDISEASECD"], $opt, $extra.$entMove, 1);

        //心臓・疾病及び異常テキスト
        if (in_array($RowD["HEARTDISEASECD"], $disableCodes) == true || $RowD["HEARTDISEASECD"] == '') {
            $extra = "disabled";
        } else {
            $extra = "";
        }
        $arg["data"]["HEARTDISEASECD_REMARK"] = knjCreateTextBox($objForm, $RowD["HEARTDISEASECD_REMARK"], "HEARTDISEASECD_REMARK", 40, 20, $extra.$entMove);

        /******************/
        /* リストtoリスト */
        /******************/
        $db = Query::dbCheckOut();

        $result   = $db->query(knjf010jQuery::getStudent($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if (!in_array($row["SCHREGNO"], $array)) {
                $opt_right[]  = array("label" => $row["ATTENDNO"]." ".$row["SCHREGNO"]." ".$row["NAME_SHOW"],
                                      "value" => $row["SCHREGNO"]);
            } else {
                $opt_left[]   = array("label" => $row["ATTENDNO"]." ".$row["SCHREGNO"]." ".$row["NAME_SHOW"],
                                      "value" => $row["SCHREGNO"]);
            }
        }
        $result->free();

        Query::dbCheckIn($db);

        /* ボタン作成 */
        //更新ボタン
        $extra = "onclick=\"return doSubmit()\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //戻るボタン
        $link = REQUESTROOT."/F/KNJF010J/knjf010jindex.php?cmd=back&ini2=1";
        $extra = "onclick=\"window.open('$link','_self');\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //対象者一覧
        $extra = "multiple STYLE=\"WIDTH:100%\" ondblclick=\"move('right','left_select','right_select',1)\"";
        $arg["data"]["left_select"] = knjCreateCombo($objForm, "left_select", "left", $opt_left, $extra, "20");

        //生徒一覧
        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left','left_select','right_select',1)\"";
        $arg["data"]["right_select"] = knjCreateCombo($objForm, "right_select", "left", $opt_right, $extra, "20");

        //全て追加
        $extra = "onclick=\"return move('sel_add_all','left_select','right_select',1);\"";
        $arg["button"]["sel_add_all"] = knjCreateBtn($objForm, "sel_add_all", "≪", $extra);

        //追加
        $extra = "onclick=\"return move('left','left_select','right_select',1);\"";
        $arg["button"]["sel_add"] = knjCreateBtn($objForm, "sel_add", "＜", $extra);

        //削除
        $extra = "onclick=\"return move('right','left_select','right_select',1);\"";
        $arg["button"]["sel_del"] = knjCreateBtn($objForm, "sel_del", "＞", $extra);

        //全て削除
        $extra = "onclick=\"return move('sel_del_all','left_select','right_select',1);\"";
        $arg["button"]["sel_del_all"] = knjCreateBtn($objForm, "sel_del_all", "≫", $extra);

        /* ヘッダ */
        $arg["info"]    = array("TOP"        =>  $model->year."年度  "
                                                .$model->control_data["学期名"][$model->semester]
                                                ."  対象クラス  ".$model->Hrname,
                                "LEFT_LIST"  => "対象者一覧",
                                "RIGHT_LIST" => $model->sch_label."一覧");
        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");
        knjCreateHidden($objForm, "REPLACEHIDDENDATE", $RowH["DATE"]);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjf010jSubForm2.html", $arg);
    }
}

function getDisabledCodes($db, $nameCd1)
{
    $result     = $db->query(knjf010jQuery::getNameSpare2($nameCd1));
    $nameCd2 = array();
    while ($row  = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $nameCd2[] = $row["NAMECD2"];
    }
    return $nameCd2;
}
