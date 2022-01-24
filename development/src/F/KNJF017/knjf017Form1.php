<?php

require_once('for_php7.php');

class knjf017Form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjf017index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //初期化
        if ($model->cmd == "changeForm") {
            $model->field = array();
            if ($model->input_form == "1" && $model->field["DATE"] == "") {
                $model->field["DATE"] = CTRL_DATE;
            }
        }

        /**************/
        /*  画面右側  */
        /**************/

        if ($model->isHirokoudai) { // 広工大は数字入力
            $arg["is_hirokoudai"] = "1";
            $arg["no_hirokoudai"] = "";
            $arg["EAR_TITLE"]     = "1000Hz";
        } else {
            $arg["is_hirokoudai"] = "";
            $arg["no_hirokoudai"] = "1";
            $arg["EAR_TITLE"]     = "平均dB";
        }

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //学期
        $arg["SEMESTERNAME"] = CTRL_SEMESTERNAME;

        //学年コンボ
        $extra = "onChange=\"btn_submit('changeGrade');\"";
        $query = knjf017Query::getGradeHrClass($model, "grade");
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->grade, $extra, 1);

        //校種取得
        $query = knjf017Query::getSchoolKind($model->grade);
        $model->schoolkind = $db->getOne($query);

        //年組コンボ
        $extra = "onChange=\"btn_submit('edit');\"";
        $query = knjf017Query::getGradeHrClass($model, "hr_class");
        makeCmb($objForm, $arg, $db, $query, "HR_CLASS", $model->hr_class, $extra, 1);

        //生徒項目名切替
        $schName = "";
        //テーブルの有無チェック
        $query = knjf017Query::checkTableExist();
        $table_cnt = $db->getOne($query);
        if ($table_cnt > 0 && $model->grade) {
            //生徒項目名取得
            $schName = $db->getOne(knjf017Query::getSchName($model, $model->grade));
        }
        $arg["schName"] = (strlen($schName)) ? $schName : '生徒';

        //生徒リストToリスト作成
        makeListToList($objForm, $arg, $db, $model);


        /**************/
        /*  画面左側  */
        /**************/

        //種類コンボ
        $extra = "onChange=\"btn_submit('changeForm');\"";
        $query = knjf017Query::getInputForm();
        makeCmb($objForm, $arg, $db, $query, "INPUT_FORM", $model->input_form,  $extra, 1, "");
        $arg["inputForm".$model->input_form] = 1;

        //レイアウトの切り替え
        if ($model->Properties["printKenkouSindanIppan"] == "1") {
            $arg["new"] = 1;
        } else if ($model->Properties["printKenkouSindanIppan"] == "2" || $model->Properties["printKenkouSindanIppan"] == "3") {
            $arg["new2"] = 1;
            $arg["Ippan".$model->Properties["printKenkouSindanIppan"]] = "1";
        } else {
            $arg["base"] = 1;
        }

        //更新項目件数
        if ($model->input_form == "2") {
            $check_cnt = 21;
        } else if ($model->input_form == "3") {
            $check_cnt = 20;
        } else if ($model->input_form == "4") {
            $check_cnt = 5;
        } else {
            $check_cnt = 10;
        }
        knjCreateHidden($objForm, "CHECK_CNT", $check_cnt);

        //ALLチェック
        $extra = " id=\"CHECKALL\" onClick=\"check_all(this);\"";
        $arg["data"]["CHECKALL"] = knjCreateCheckBox($objForm, "CHECKALL", "", $extra, "");

        //選択チェックボックス
        for ($i=0; $i <= $check_cnt; $i++) {
            $extra = ($model->field["RCHECK".$i] == 1) ? "checked" : "";

            $arg["data"]["RCHECK".$i] = knjCreateCheckBox($objForm, "RCHECK".$i, 1, $extra, "");
        }

        //名称マスタより取得するコード一覧
        $nameM["F010"] = "";
        $nameM["F018"] = "CD_NASHI";
        $nameM["F019"] = "CD_NASHI";
        $nameM["F020"] = "CD_NASHI";
        $nameM["F021"] = "";
        $nameM["F022"] = "";
        $nameM["F023"] = "";
        $nameM["F030"] = "";
        $nameM["F040"] = "";
        $nameM["F050"] = "";
        $nameM["F051"] = "";
        $nameM["F060"] = "";
        $nameM["F061"] = "";
        $nameM["F062"] = "";
        $nameM["F063"] = "";
        $nameM["F070"] = "";
        $nameM["F080"] = "";
        $nameM["F090"] = "";
        $nameM["F091"] = "";
        $nameM["F100"] = "";
        $nameM["F110"] = "";
        $nameM["F120"] = "";
        $nameM["F130"] = "";
        $nameM["F140"] = "";
        $nameM["F141"] = "3";
        $nameM["F142"] = "";
        $nameM["F143"] = "";
        $nameM["F144"] = "";
        $nameM["F145"] = "";
        $nameM["F150"] = "";

        foreach ($nameM as $namecd1 => $flg) {
            //名称マスタよりデータ取得・格納
            $query = knjf017Query::getNameMst($model, $namecd1, $flg);
            $optname = "opt".$namecd1;
            $$optname = makeArrayReturn($db, $query);
        }

        //健康診断実施日付
        $model->field["DATE"] = str_replace("-", "/", $model->field["DATE"]);
        $arg["data"]["DATE"] = View::popUpCalendar($objForm, "DATE", $model->field["DATE"]);

        //視力・裸眼(数字)
        $extra = "";
        $arg["data"]["R_BAREVISION"] = knjCreateTextBox($objForm, $model->field["R_BAREVISION"], "R_BAREVISION", 5, 5, $extra);
        $arg["data"]["L_BAREVISION"] = knjCreateTextBox($objForm, $model->field["L_BAREVISION"], "L_BAREVISION", 5, 5, $extra);

        //視力・矯正(数字)
        $extra = "";
        $arg["data"]["R_VISION"] = knjCreateTextBox($objForm, $model->field["R_VISION"], "R_VISION", 5, 5, $extra);
        $arg["data"]["L_VISION"] = knjCreateTextBox($objForm, $model->field["L_VISION"], "L_VISION", 5, 5, $extra);

        //視力・裸眼(文字)
        $extra = "onblur=\"return Mark_Check(this);\"";
        $arg["data"]["R_BAREVISION_MARK"] = knjCreateTextBox($objForm, $model->field["R_BAREVISION_MARK"], "R_BAREVISION_MARK", 1, 1, $extra);
        $arg["data"]["L_BAREVISION_MARK"] = knjCreateTextBox($objForm, $model->field["L_BAREVISION_MARK"], "L_BAREVISION_MARK", 1, 1, $extra);

        //視力・矯正(文字)
        $extra = "onblur=\"return Mark_Check(this);\"";
        $arg["data"]["R_VISION_MARK"] = knjCreateTextBox($objForm, $model->field["R_VISION_MARK"], "R_VISION_MARK", 1, 1, $extra);
        $arg["data"]["L_VISION_MARK"] = knjCreateTextBox($objForm, $model->field["L_VISION_MARK"], "L_VISION_MARK", 1, 1, $extra);

        //聴力・右DB
        //聴力・左DB
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["R_EAR_DB"] = knjCreateTextBox($objForm, $model->field["R_EAR_DB"], "R_EAR_DB", 4, 3, $extra);
        $arg["data"]["L_EAR_DB"] = knjCreateTextBox($objForm, $model->field["L_EAR_DB"], "L_EAR_DB", 4, 3, $extra);

        //聴力・右状態
        //聴力・左状態
        $extra = "";
        $arg["data"]["R_EAR"] = makeCmbReturn($objForm, $optF010, $model->field["R_EAR"], "R_EAR", $extra, 1, "BLANK");
        $arg["data"]["L_EAR"] = makeCmbReturn($objForm, $optF010, $model->field["L_EAR"], "L_EAR", $extra, 1, "BLANK");

        //聴力・右4000Hz
        //聴力・左4000Hz
        if ($model->Properties["useEar4000Hz"] == "1") {
            $arg["useEar4000Hz"] = 1;
            $extra = "onblur=\"this.value=toInteger(this.value)\"";
            $arg["data"]["R_EAR_DB_4000"] = knjCreateTextBox($objForm, $model->field["R_EAR_DB_4000"], "R_EAR_DB_4000", 4, 3, $extra);
            $arg["data"]["L_EAR_DB_4000"] = knjCreateTextBox($objForm, $model->field["L_EAR_DB_4000"], "L_EAR_DB_4000", 4, 3, $extra);
        } else {
            $arg["unuseEar4000Hz"] = 1;
        }

        //尿・蛋白
        $extra = "";
        $arg["data"]["ALBUMINURIA1CD"] = makeCmbReturn($objForm, $optF020, $model->field["ALBUMINURIA1CD"], "ALBUMINURIA1CD", $extra, 1, "BLANK");
        $arg["data"]["ALBUMINURIA2CD"] = makeCmbReturn($objForm, $optF020, $model->field["ALBUMINURIA2CD"], "ALBUMINURIA2CD", $extra, 1, "BLANK");

        //尿・糖
        $arg["data"]["URICSUGAR1CD"] = makeCmbReturn($objForm, $optF019, $model->field["URICSUGAR1CD"], "URICSUGAR1CD", $extra, 1, "BLANK");
        $arg["data"]["URICSUGAR2CD"] = makeCmbReturn($objForm, $optF019, $model->field["URICSUGAR2CD"], "URICSUGAR2CD", $extra, 1, "BLANK");

        //尿・潜血
        $arg["data"]["URICBLEED1CD"] = makeCmbReturn($objForm, $optF018, $model->field["URICBLEED1CD"], "URICBLEED1CD", $extra, 1, "BLANK");
        $arg["data"]["URICBLEED2CD"] = makeCmbReturn($objForm, $optF018, $model->field["URICBLEED2CD"], "URICBLEED2CD", $extra, 1, "BLANK");

        //尿・その他の検査
        $extra  = "";
        $arg["data"]["URICOTHERTEST"] = knjCreateTextBox($objForm, $model->field["URICOTHERTEST"], "URICOTHERTEST", 40, 20, $extra);
        $extra = "";
        $arg["data"]["URICOTHERTESTCD"] = makeCmbReturn($objForm, $optF022, $model->field["URICOTHERTESTCD"], "URICOTHERTESTCD", $extra, 1, "BLANK");

        //尿・指導区分
        $extra = "";
        $arg["data"]["URI_ADVISECD"] = makeCmbReturn($objForm, $optF021, $model->field["URI_ADVISECD"], "URI_ADVISECD", $extra, 1, "BLANK");

        //栄養状態
        $extra = "onChange=\"syokenNyuryoku(this, 'NUTRITIONCD_REMARK')\"";
        $arg["data"]["NUTRITIONCD"] = makeCmbReturn($objForm, $optF030, $model->field["NUTRITIONCD"], "NUTRITIONCD", $extra, 1, "BLANK");
        $extra = ((int)$model->field["NUTRITIONCD"] < 2) ? " disabled" : "";
        $arg["data"]["NUTRITIONCD_REMARK"] = knjCreateTextBox($objForm, $model->field["NUTRITIONCD_REMARK"], "NUTRITIONCD_REMARK", 40, 20, $extra);

        //目の疾病及び異常コンボ
        if ($model->Properties["printKenkouSindanIppan"] == "2") {
            $extra = "";
        } else {
            $extra = "onChange=\"syokenNyuryoku(this, 'EYE_TEST_RESULT')\"";
        }
        $arg["data"]["EYEDISEASECD"]  = makeCmbReturn($objForm, $optF050, $model->field["EYEDISEASECD"], "EYEDISEASECD", $extra, 1, "BLANK");
        $arg["data"]["EYEDISEASECD2"] = makeCmbReturn($objForm, $optF050, $model->field["EYEDISEASECD2"], "EYEDISEASECD2", $extra, 1, "BLANK");
        $arg["data"]["EYEDISEASECD3"] = makeCmbReturn($objForm, $optF050, $model->field["EYEDISEASECD3"], "EYEDISEASECD3", $extra, 1, "BLANK");
        $arg["data"]["EYEDISEASECD4"] = makeCmbReturn($objForm, $optF050, $model->field["EYEDISEASECD4"], "EYEDISEASECD4", $extra, 1, "BLANK");
        //目の疾病及び異常テキスト
        $extra = ((int)$model->field["EYEDISEASECD"] < 2 && $model->Properties["printKenkouSindanIppan"] != "2") ? " disabled" : "";
        $arg["data"]["EYE_TEST_RESULT"]  = knjCreateTextBox($objForm, $model->field["EYE_TEST_RESULT"], "EYE_TEST_RESULT", 40, 20, $extra);
        $arg["data"]["EYE_TEST_RESULT2"] = knjCreateTextBox($objForm, $model->field["EYE_TEST_RESULT2"], "EYE_TEST_RESULT2", 40, 20, $extra);
        $arg["data"]["EYE_TEST_RESULT3"] = knjCreateTextBox($objForm, $model->field["EYE_TEST_RESULT3"], "EYE_TEST_RESULT3", 40, 20, $extra);

        //脊柱・胸郭・四肢
        if ($model->Properties["printKenkouSindanIppan"] == "2") {
            $extra = "";
        } else {
            $extra = "onChange=\"syokenNyuryoku(this, 'SPINERIBCD_REMARK')\"";
        }
        $arg["data"]["SPINERIBCD"] = makeCmbReturn($objForm, $optF040, $model->field["SPINERIBCD"], "SPINERIBCD", $extra, 1, "BLANK");
        $extra = ((int)$model->field["SPINERIBCD"] < 2 && $model->Properties["printKenkouSindanIppan"] != "2") ? " disabled" : "";
        $arg["data"]["SPINERIBCD_REMARK"] = knjCreateTextBox($objForm, $model->field["SPINERIBCD_REMARK"], "SPINERIBCD_REMARK", 40, 20, $extra);

        //耳鼻咽頭疾患
        if ($model->Properties["printKenkouSindanIppan"] == "2") {
            $extra = "";
        } else {
            $extra = "onChange=\"syokenNyuryoku(this, 'NOSEDISEASECD_REMARK')\"";
        }
        $arg["data"]["NOSEDISEASECD"]  = makeCmbReturn($objForm, $optF060, $model->field["NOSEDISEASECD"], "NOSEDISEASECD", $extra, 1, "BLANK");
        $arg["data"]["NOSEDISEASECD2"] = makeCmbReturn($objForm, $optF060, $model->field["NOSEDISEASECD2"], "NOSEDISEASECD2", $extra, 1, "BLANK");
        $arg["data"]["NOSEDISEASECD3"] = makeCmbReturn($objForm, $optF060, $model->field["NOSEDISEASECD3"], "NOSEDISEASECD3", $extra, 1, "BLANK");
        $arg["data"]["NOSEDISEASECD4"] = makeCmbReturn($objForm, $optF060, $model->field["NOSEDISEASECD4"], "NOSEDISEASECD4", $extra, 1, "BLANK");
        $extra = ((int)$model->field["NOSEDISEASECD"] < 2 && $model->Properties["printKenkouSindanIppan"] != "2") ? " disabled" : "";
        $arg["data"]["NOSEDISEASECD_REMARK"]  = knjCreateTextBox($objForm, $model->field["NOSEDISEASECD_REMARK"], "NOSEDISEASECD_REMARK", 40, 20, $extra);
        $arg["data"]["NOSEDISEASECD_REMARK2"] = knjCreateTextBox($objForm, $model->field["NOSEDISEASECD_REMARK2"], "NOSEDISEASECD_REMARK2", 40, 20, $extra);
        $arg["data"]["NOSEDISEASECD_REMARK3"] = knjCreateTextBox($objForm, $model->field["NOSEDISEASECD_REMARK3"], "NOSEDISEASECD_REMARK3", 40, 20, $extra);

        //皮膚疾患
        if ($model->Properties["printKenkouSindanIppan"] == "2") {
            $extra = "";
        } else {
            $extra = "onChange=\"syokenNyuryoku(this, 'SKINDISEASECD_REMARK')\"";
        }
        $arg["data"]["SKINDISEASECD"] = makeCmbReturn($objForm, $optF070, $model->field["SKINDISEASECD"], "SKINDISEASECD", $extra, 1, "BLANK");
        $extra = ((int)$model->field["SKINDISEASECD"] < 2 && $model->Properties["printKenkouSindanIppan"] != "2") ? " disabled" : "";
        $arg["data"]["SKINDISEASECD_REMARK"] = knjCreateTextBox($objForm, $model->field["SKINDISEASECD_REMARK"], "SKINDISEASECD_REMARK", 40, 20, $extra);

        //結核（間接撮影、所見、その他検査）表示
        if ($model->schoolkind == "H") {
            $arg["tbFilmShow"] = 1;
        } else {
            $arg["tbFilmUnShow"] = 1;
        }

        //結核・撮影日付
        $model->field["TB_FILMDATE"] = str_replace("-", "/", $model->field["TB_FILMDATE"]);
        $arg["data"]["TB_FILMDATE"] = View::popUpCalendar($objForm, "TB_FILMDATE", $model->field["TB_FILMDATE"]);

        //結核・所見
        $extra = "";
        $arg["data"]["TB_REMARKCD"] = makeCmbReturn($objForm, $optF100, $model->field["TB_REMARKCD"], "TB_REMARKCD", $extra, 1, "BLANK");

        //結核検査(X線)
        $extra = "";
        $arg["data"]["TB_X_RAY"] = knjCreateTextBox($objForm, $model->field["TB_X_RAY"], "TB_X_RAY", 40, 20, $extra);

        //結核・その他検査
        $extra = "";
        $arg["data"]["TB_OTHERTESTCD"] = makeCmbReturn($objForm, $optF110, $model->field["TB_OTHERTESTCD"], "TB_OTHERTESTCD", $extra, 1, "BLANK");
        $extra = "";
        $arg["data"]["TB_OTHERTEST_REMARK1"] = knjCreateTextBox($objForm, $model->field["TB_OTHERTEST_REMARK1"], "TB_OTHERTEST_REMARK1", 40, 20, $extra);

        //結核・病名
        $extra = "";
        $arg["data"]["TB_NAMECD"] = makeCmbReturn($objForm, $optF120, $model->field["TB_NAMECD"], "TB_NAMECD", $extra, 1, "BLANK");
        $extra = "";
        $arg["data"]["TB_NAME_REMARK1"] = knjCreateTextBox($objForm, $model->field["TB_NAME_REMARK1"], "TB_NAME_REMARK1", 40, 20, $extra);
        if ($model->Properties["printKenkouSindanIppan"] == "1") {
            $arg["data"]["TB_NAMECD_LABEL"] = ($model->z010name1 == "miyagiken") ? "病名" : "疾病及び異常";
        } else {
            $arg["data"]["TB_NAMECD_LABEL"] = "病名";
        }

        //結核・指導区分
        $extra = "";
        $arg["data"]["TB_ADVISECD"] = makeCmbReturn($objForm, $optF130, $model->field["TB_ADVISECD"], "TB_ADVISECD", $extra, 1, "BLANK");
        $extra = "";
        $arg["data"]["TB_ADVISE_REMARK1"] = knjCreateTextBox($objForm, $model->field["TB_ADVISE_REMARK1"], "TB_ADVISE_REMARK1", 40, 20, $extra);

        //心臓・臨床医学的検査
        if ($model->Properties["printKenkouSindanIppan"] == "2") {
            $extra = "";
        } else {
            $extra = "onChange=\"syokenNyuryoku(this, 'HEART_MEDEXAM_REMARK')\"";
        }
        $arg["data"]["HEART_MEDEXAM"] = makeCmbReturn($objForm, $optF080, $model->field["HEART_MEDEXAM"], "HEART_MEDEXAM", $extra, 1, "BLANK");
        $extra = ((int)$model->field["HEART_MEDEXAM"] < 2 && $model->Properties["printKenkouSindanIppan"] != "2") ? " disabled" : "";
        $arg["data"]["HEART_MEDEXAM_REMARK"] = knjCreateTextBox($objForm, $model->field["HEART_MEDEXAM_REMARK"], "HEART_MEDEXAM_REMARK", 40, 40, $extra);

        //心臓・疾病及び異常
        if ($model->Properties["printKenkouSindanIppan"] == "2") {
        $extra = "";
        } else {
        $extra = "onChange=\"syokenNyuryoku(this, 'HEARTDISEASECD_REMARK')\"";
        }
        $arg["data"]["HEARTDISEASECD"] = makeCmbReturn($objForm, $optF090, $model->field["HEARTDISEASECD"], "HEARTDISEASECD", $extra, 1, "BLANK");
        $extra = ((int)$model->field["HEARTDISEASECD"] < 2 && $model->Properties["printKenkouSindanIppan"] != "2") ? " disabled" : "";
        $arg["data"]["HEARTDISEASECD_REMARK"] = knjCreateTextBox($objForm, $model->field["HEARTDISEASECD_REMARK"], "HEARTDISEASECD_REMARK", 40, 20, $extra);

        //心臓・管理区分
        $extra = "";
        $arg["data"]["MANAGEMENT_DIV"] = makeCmbReturn($objForm, $optF091, $model->field["MANAGEMENT_DIV"], "MANAGEMENT_DIV", $extra, 1, "BLANK");
        $extra  = "";
        $arg["data"]["MANAGEMENT_REMARK"] = knjCreateTextBox($objForm, $model->field["MANAGEMENT_REMARK"], "MANAGEMENT_REMARK", 40, 20, $extra);

        //寄生虫卵
        $extra = "";
        $arg["data"]["PARASITE"] = makeCmbReturn($objForm, $optF023, $model->field["PARASITE"], "PARASITE", $extra, 1, "BLANK");
        //寄生虫卵表示
        if ($model->Properties["useParasite_".$model->schoolkind] == "1") {
            $arg["para"] = 1;
        }

        //その他疾病及び異常
        $extra = "";
        $arg["data"]["OTHERDISEASECD"] = makeCmbReturn($objForm, $optF140, $model->field["OTHERDISEASECD"], "OTHERDISEASECD", $extra, 1, "BLANK");
        $extra = "";
        $arg["data"]["OTHER_ADVISECD"] = makeCmbReturn($objForm, $optF145, $model->field["OTHER_ADVISECD"], "OTHER_ADVISECD", $extra, 1, "BLANK");
        $extra = "";
        $arg["data"]["OTHER_REMARK"] = knjCreateTextBox($objForm, $model->field["OTHER_REMARK"], "OTHER_REMARK", 40, 20, $extra);
        $arg["data"]["OTHER_REMARK2"] = knjCreateTextBox($objForm, $model->field["OTHER_REMARK2"], "OTHER_REMARK2", 40, 20, $extra);
        $arg["data"]["OTHER_REMARK3"] = knjCreateTextBox($objForm, $model->field["OTHER_REMARK3"], "OTHER_REMARK3", 40, 20, $extra);

        //学校医・所見
        $extra = "";
        $arg["data"]["DOC_CD"] = makeCmbReturn($objForm, $optF144, $model->field["DOC_CD"], "DOC_CD", $extra, 1, "BLANK");
        $extra = "";
        if ($model->Properties["printKenkouSindanIppan"] == "1") {
            $arg["data"]["DOC_REMARK"] = knjCreateTextBox($objForm, $model->field["DOC_REMARK"], "DOC_REMARK", 40, 30, $extra);
        } else {
            $arg["data"]["DOC_REMARK"] = knjCreateTextBox($objForm, $model->field["DOC_REMARK"], "DOC_REMARK", 40, 20, $extra);
        }
        //学校医・所見日付
        $model->field["DOC_DATE"] = str_replace("-", "/", $model->field["DOC_DATE"]);
        $arg["data"]["DOC_DATE"] = View::popUpCalendar($objForm, "DOC_DATE", $model->field["DOC_DATE"]);
        //備考
        $extra  = "";
        $arg["data"]["REMARK"] = knjCreateTextBox($objForm, $model->field["REMARK"], "REMARK", 60, 200, $extra);

        //既往症
        $extra = "";
        $arg["data"]["MEDICAL_HISTORY1"] = makeCmbReturn($objForm, $optF143, $model->field["MEDICAL_HISTORY1"], "MEDICAL_HISTORY1", $extra, 1, "BLANK");
        $arg["data"]["MEDICAL_HISTORY2"] = makeCmbReturn($objForm, $optF143, $model->field["MEDICAL_HISTORY2"], "MEDICAL_HISTORY2", $extra, 1, "BLANK");
        $arg["data"]["MEDICAL_HISTORY3"] = makeCmbReturn($objForm, $optF143, $model->field["MEDICAL_HISTORY3"], "MEDICAL_HISTORY3", $extra, 1, "BLANK");

        //診断名
        $extra = "";
        $arg["data"]["DIAGNOSIS_NAME"] = knjCreateTextBox($objForm, $model->field["DIAGNOSIS_NAME"], "DIAGNOSIS_NAME", 40, 50, $extra);

        //運動・指導区分
        $extra = "";
        $arg["data"]["GUIDE_DIV"] = makeCmbReturn($objForm, $optF141, $model->field["GUIDE_DIV"], "GUIDE_DIV", $extra, 1, "BLANK");
        //運動・部活動
        $extra = "";
        $arg["data"]["JOINING_SPORTS_CLUB"] = makeCmbReturn($objForm, $optF142, $model->field["JOINING_SPORTS_CLUB"], "JOINING_SPORTS_CLUB", $extra, 1, "BLANK");

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        View::toHTML($model, "knjf017Form1.html", $arg); 
    }
}

//リストtoリスト作成
function makeListToList(&$objForm, &$arg, $db, $model) {
    //リスト（右）取得
    $opt_right = array();
    $result = $db->query(knjf017Query::getStudent($model, "right"));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt_right[] = array('label' => $row["HR_NAME"]."　".$row["ATTENDNO"].'番'."　".$row["NAME_SHOW"],
                             'value' => $row["VALUE"]);
    }
    $result->free();

    //リスト（左）取得
    $opt_left = array();
    if ($model->selectdata && $model->cmd != "change") {
        $result = $db->query(knjf017Query::getStudent($model, "left"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_left[] = array('label' => $row["HR_NAME"]."　".$row["ATTENDNO"].'番'."　".$row["NAME_SHOW"],
                                'value' => $row["VALUE"]);
        }
        $result->free();
    }

    //対象者リスト（左）
    $extra = "multiple style=\"width:100%\" ondblclick=\"move1('right')\"";
    $arg["data"]["LEFT_SELECT"] = knjCreateCombo($objForm, "LEFT_SELECT", "", $opt_left, $extra, 20);

    //一覧リスト（右）
    $extra = "multiple style=\"width:100%\" ondblclick=\"move1('left')\"";
    $arg["data"]["RIGHT_SELECT"] = knjCreateCombo($objForm, "RIGHT_SELECT", "", $opt_right, $extra, 20);

    //対象選択ボタン（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    //対象選択ボタン（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
    //対象取消ボタン（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    //対象取消ボタン（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") $opt[] = array ("label" => "", "value" => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//配列作成
function makeArrayReturn($db, $query) {
    $opt = array();
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
    }
    $result->free();

    return $opt;
}

//コンボ作成
function makeCmbReturn(&$objForm, &$getopt, &$value, $name, $extra, $size, $blank="") {
    $opt = $getopt;
    if ($blank) array_unshift($opt, array("label" => "", "value" => ""));
    $value_flg = false;
    foreach ($opt as $key => $val) {
        if ($value == $val["value"]) $value_flg = true;
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    //更新
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "printKenkouSindanIppan", $model->Properties["printKenkouSindanIppan"]);
    knjCreateHidden($objForm, "selectdata");
}
?>
