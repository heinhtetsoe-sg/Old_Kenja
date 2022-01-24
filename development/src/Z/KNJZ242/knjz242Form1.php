<?php

require_once('for_php7.php');

class knjz242Form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //年度コンボ
        $query = knjz242Query::getYear();
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "YEAR", $model->field["YEAR"], $extra, 1, "");

        //学年コンボ
        $query = knjz242Query::getGdat($model);
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1, "");

        //コピーボタン
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からコピー", $extra);

        //校種セット
        $model->school_kind = $db->getOne(knjz242Query::getGdat($model, "kind"));

        //警告メッセージを表示しない場合
        if(!isset($model->warning)){
            $Row = $db->getRow(knjz242Query::getMedexamDetNotExaminedDat($model), DB_FETCHMODE_ASSOC);
        }else{
            $Row =& $model->field;
        }

        //レイアウトの切り替え
        if ($model->Properties["printKenkouSindanIppan"] == "1") {
            $arg["new"] = 1;
        } else if ($model->Properties["printKenkouSindanIppan"] == "2" || $model->Properties["printKenkouSindanIppan"] == "3") {
            $arg["new2"] = 1;
            $arg["Ippan".$model->Properties["printKenkouSindanIppan"]] = "1";
        } else {
            $arg["base"] = 1;
        }

        //寄生虫卵表示
        if (($model->school_kind == "P" && $model->Properties["useParasite_P"] == "1") || ($model->school_kind == "J" && $model->Properties["useParasite_J"] == "1") || ($model->school_kind == "H" && $model->Properties["useParasite_H"] == "1")) {
            $arg["para"] = 1;
            $arg["rowspan3"] = 5;
        } else {
            $arg["rowspan3"] = 4;
        }

        //尿)その他の検査テキスト、指導区分コンボ
        if ($model->Properties["printKenkouSindanIppan"] == "1" || $model->Properties["printKenkouSindanIppan"] == "3") {
            $arg["rowspan3"] += 1;
        }
        //メッセージ
        if ($model->Properties["printKenkouSindanIppan"] == "2") {
            $arg["rowspan3"] += 2;
        }
        //福井（貧血）
        if ($model->z010name1 == "fukuiken") {
            $arg["is_fukui"] = "1";
            $arg["rowspan3"] += 1;
        }

        // "１"はテーブルのフィールドには存在しない。便宜上名付けた。
        $model->fieldArr = array();
        $model->fieldArr["HEIGHT"]                 = "";
        $model->fieldArr["WEIGHT"]                 = "";
        $model->fieldArr["SITHEIGHT"]              = "";
        /** 視力 **/
        $model->fieldArr["VISION"]                 = "1";
        $model->fieldArr["R_BAREVISION"]           = "";
        $model->fieldArr["R_BAREVISION_MARK"]      = "";
        $model->fieldArr["L_BAREVISION"]           = "";
        $model->fieldArr["L_BAREVISION_MARK"]      = "";
        $model->fieldArr["R_VISION"]               = "";
        $model->fieldArr["R_VISION_MARK"]          = "";
        $model->fieldArr["L_VISION"]               = "";
        $model->fieldArr["L_VISION_MARK"]          = "";
        $model->fieldArr["VISION_CANTMEASURE"]     = "";
        $model->fieldArr["R_VISION_CANTMEASURE"]   = "";
        $model->fieldArr["L_VISION_CANTMEASURE"]   = "";
        /** 聴力 **/
        $model->fieldArr["EAR"]                    = "1";
        $model->fieldArr["R_EAR"]                  = "";
        $model->fieldArr["R_EAR_DB"]               = "";
        $model->fieldArr["R_EAR_DB_4000"]          = "";
        $model->fieldArr["R_EAR_CANTMEASURE"]      = "";
        $model->fieldArr["L_EAR"]                  = "";
        $model->fieldArr["L_EAR_DB"]               = "";
        $model->fieldArr["L_EAR_DB_4000"]          = "";
        $model->fieldArr["L_EAR_CANTMEASURE"]      = "";
        /** 尿 **/
        $model->fieldArr["URI"]                    = "1";
        $model->fieldArr["ALBUMINURIA1CD"]         = "";
        $model->fieldArr["URICSUGAR1CD"]           = "";
        $model->fieldArr["URICBLEED1CD"]           = "";
        $model->fieldArr["ALBUMINURIA2CD"]         = "";
        $model->fieldArr["URICSUGAR2CD"]           = "";
        $model->fieldArr["URICBLEED2CD"]           = "";
        $model->fieldArr["URICOTHERTESTCD"]        = "";
        $model->fieldArr["URICOTHERTEST"]          = "";
        $model->fieldArr["URI_ADVISECD"]           = "";
        /** 栄養状態 **/
        $model->fieldArr["NUTRITIONCD"]            = "";
        $model->fieldArr["NUTRITIONCD_REMARK"]     = "";
        /** 脊柱・胸郭・四肢 **/
        $model->fieldArr["SPINERIBCD"]             = "";
        $model->fieldArr["SPINERIBCD_REMARK"]      = "";
        /** 目の疾病及び異常 **/
        $model->fieldArr["EYE"]                    = "1";
        $model->fieldArr["EYEDISEASECD"]           = "";
        $model->fieldArr["EYEDISEASECD2"]          = "";
        $model->fieldArr["EYEDISEASECD3"]          = "";
        $model->fieldArr["EYEDISEASECD4"]          = "";
        $model->fieldArr["EYEDISEASECD5"]          = "";
        $model->fieldArr["EYE_TEST_RESULT"]        = "";
        $model->fieldArr["EYE_TEST_RESULT2"]       = "";
        $model->fieldArr["EYE_TEST_RESULT3"]       = "";
        /** 耳鼻咽頭疾患 **/
        $model->fieldArr["NOSEDISEASECD"]          = "";
        $model->fieldArr["NOSEDISEASECD2"]         = "";
        $model->fieldArr["NOSEDISEASECD3"]         = "";
        $model->fieldArr["NOSEDISEASECD4"]         = "";
        $model->fieldArr["NOSEDISEASECD5"]         = "";
        $model->fieldArr["NOSEDISEASECD6"]         = "";
        $model->fieldArr["NOSEDISEASECD7"]         = "";
        $model->fieldArr["NOSEDISEASECD_REMARK"]   = "";
        $model->fieldArr["NOSEDISEASECD_REMARK1"]  = "";
        $model->fieldArr["NOSEDISEASECD_REMARK2"]  = "";
        $model->fieldArr["NOSEDISEASECD_REMARK3"]  = "";
        /** 皮膚疾患 **/
        $model->fieldArr["SKINDISEASECD"]          = "";
        $model->fieldArr["SKINDISEASECD_REMARK"]   = "";
        /** 心臓 **/
        $model->fieldArr["HEART"]                  = "1";
        $model->fieldArr["HEART_MEDEXAM"]          = "";
        $model->fieldArr["HEART_MEDEXAM_REMARK"]   = "";
        $model->fieldArr["HEARTDISEASECD"]         = "";
        $model->fieldArr["HEARTDISEASECD_REMARK"]  = "";
        $model->fieldArr["MANAGEMENT_DIV"]         = "";
        $model->fieldArr["MANAGEMENT_REMARK"]      = "";
        /** 結核 **/
        $model->fieldArr["TB_"]                    = "1";
        $model->fieldArr["TB_DATE"]                = "";
        $model->fieldArr["TB_REACT"]               = "";
        $model->fieldArr["TB_RESULT"]              = "";
        $model->fieldArr["TB_BCGDATE"]             = "";
        $model->fieldArr["TB_FILMDATE"]            = "";
        $model->fieldArr["TB_FILMNO"]              = "";
        $model->fieldArr["TB_REMARKCD"]            = "";
        $model->fieldArr["TB_OTHERTESTCD"]         = "";
        $model->fieldArr["TB_OTHERTEST_REMARK1"]   = "";
        $model->fieldArr["TB_NAMECD"]              = "";
        $model->fieldArr["TB_NAME_REMARK1"]        = "";
        $model->fieldArr["TB_ADVISECD"]            = "";
        $model->fieldArr["TB_ADVISE_REMARK1"]      = "";
        $model->fieldArr["TB_X_RAY"]               = "";
        /** 貧血 **/
        $model->fieldArr["ANEMIA_REMARK"]          = "";
        $model->fieldArr["HEMOGLOBIN"]             = "";
        /** 寄生虫卵 **/
        $model->fieldArr["PARASITE"]               = "";
        /** 寄生虫卵 **/
        $model->fieldArr["OTHERDISEASECD"]         = "";
        $model->fieldArr["OTHER_ADVISECD"]         = "";
        $model->fieldArr["OTHER_REMARK"]           = "";
        $model->fieldArr["OTHER_REMARK2"]          = "";
        $model->fieldArr["OTHER_REMARK3"]          = "";
        /** 学校医 **/
        $model->fieldArr["DOC_"]                   = "1";
        $model->fieldArr["DOC_CD"]                 = "";
        $model->fieldArr["DOC_REMARK"]             = "";
        $model->fieldArr["DOC_DATE"]               = "";
        /** 事後措置 **/
        $model->fieldArr["TREATCD"]                = "";
        $model->fieldArr["TREATCD2"]               = "";
        $model->fieldArr["TREAT_REMARK1"]          = "";
        $model->fieldArr["TREAT_REMARK2"]          = "";
        $model->fieldArr["TREAT_REMARK3"]          = "";
        /** 備考 **/
        $model->fieldArr["REMARK"]                 = "";
        /** 運動 **/
        $model->fieldArr["GUIDE_DIV"]              = "";
        $model->fieldArr["JOINING_SPORTS_CLUB"]    = "";
        /** 既往症 **/
        $model->fieldArr["MEDICAL_HISTORY"]        = "1";
        $model->fieldArr["MEDICAL_HISTORY1"]       = "";
        $model->fieldArr["MEDICAL_HISTORY2"]       = "";
        $model->fieldArr["MEDICAL_HISTORY3"]       = "";
        /** 診断名 **/
        $model->fieldArr["DIAGNOSIS_NAME"]         = "";
        /** メッセージ **/
        $model->fieldArr["MESSAGE"]                = "";
        /** F010Aでは未使用 **/
        $model->fieldArr["CHEST"]                  = "2";
        $model->fieldArr["NUTRITION_RESULT"]       = "2";
        $model->fieldArr["EYEDISEASE_RESULT"]      = "2";
        $model->fieldArr["SKINDISEASE_RESULT"]     = "2";
        $model->fieldArr["SPINERIB_RESULT"]        = "2";
        $model->fieldArr["NOSEDISEASE_RESULT"]     = "2";
        $model->fieldArr["OTHERDISEASE_RESULT"]    = "2";
        $model->fieldArr["HEARTDISEASE_RESULT"]    = "2";

        foreach ($model->fieldArr as $field => $fieldVal) {
            if ($fieldVal == "2") continue;

            //チェックボックス作成
            $checked = ($Row[$field] == "1") ? " checked": "";
            $extra = " id=\"{$field}\"; class=\"changeColor\" data-name=\"{$field}\"";
            $arg["data"][$field] = knjCreateCheckBox($objForm, $field, "1", $extra.$checked);
            $arg["name"][$field] = $field;
        }

        /************/
        /** ボタン **/
        /************/
        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$disBtn);
        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjz242index.php", "", "main");

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz242Form1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($name == "TEST_DATE") {
            $row["LABEL"] = str_replace('-', '/', $row["LABEL"]);
        }
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    if ($name == "YEAR") {
        $value = ($value != "" && $value_flg) ? $value : CTRL_YEAR;
    } else {
        $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    }
    $arg["TOP"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
