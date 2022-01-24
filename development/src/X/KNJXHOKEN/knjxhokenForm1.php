<?php

require_once('for_php7.php');

class knjxhokenForm1 {
    function main(&$model) {
        $objForm = new form;
        $arg["start"] = $objForm->get_start("detail", "POST", "knjxhokenindex.php", "", "detail");

        //DB OPEN
        $db = Query::dbCheckOut();
        
        //年度
        $arg["YEAR"] = $model->year;

        //学籍番号
        $arg["SCHREGNO"] = $model->schregno;

        //氏名
        $query = knjxhokenQuery::getName($model->schregno);
        $schName = $db->getOne($query);
        $arg["NAME"] = $schName;

        //保健室利用記録
        $extra = "style=\"height:30px;background:#00FFFF;color:#000080;font:bold\" onclick=\"return btn_submit('hoken1');\"";
        $arg["button"]["btn_subform1"] = knjCreateBtn($objForm, "btn_subform1", "保健室利用記録", $extra);
        //一般
        $extra = "style=\"height:30px;background:#ADFF2F;color:#006400;font:bold\" onclick=\"return btn_submit('hoken2');\"";
        $arg["button"]["btn_subform2"] = knjCreateBtn($objForm, "btn_subform2", "一般", $extra);
        //歯・口腔
        $extra = "style=\"height:30px;background:#C0FFFF;color:#1E90FF;font:bold\" onclick=\"return btn_submit('hoken3');\"";
        $arg["button"]["btn_subform3"] = knjCreateBtn($objForm, "btn_subform3", "歯・口腔", $extra);
        //相談記録
        $extra = "style=\"height:30px;background:#FFE4E1;color:#FF0000;font:bold\" onclick=\"return btn_submit('hoken4');\"";
        $arg["button"]["btn_subform4"] = knjCreateBtn($objForm, "btn_subform4", "相談記録", $extra);
        
        Query::dbCheckIn($db);

//1:保健室利用記録
        if ($model->cmd === 'hoken1') {
            $db = Query::dbCheckOut();
            $arg["HOKEN1"] = "1";
            //質問内容表示（体調１～５）
            $view_html = "";
            $view_html_no = array("1" => "体調１", "2" => "体調２", "3" => "体調３", "4" => "体調４", "5" => "体調５");

            for ($i=1; $i<= get_count($view_html_no); $i++){
                $view_html .= "<th align=\"center\" nowrap width=\"150\" height=\"25\" rowspan=\"2\" onMouseOver=\"ViewcdMousein(event, ".$i.")\" onMouseOut=\"ViewcdMouseout()\">".$view_html_no[$i]."</th>";
            }
            $arg["view_html"] = $view_html;

            //データを取得
            $setval = array();
            $firstflg = true;   //初回フラグ
            if($model->schregno)
            {
                $result = $db->query(knjxhokenQuery::sub1Query($model));
                while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
                {
                    $row["VISIT_TIME"] = str_replace("-", "/", $row["VISIT_DATE"]).' '.$row["VISIT_HOUR"].':'.$row["VISIT_MINUTE"];

                    if ($firstflg) {
                        $setval = $row;
                        $firstflg = false;
                    } else {
                        $visit = $setval["VISIT_DATE"].':'.$setval["VISIT_HOUR"].':'.$setval["VISIT_MINUTE"].':'.$setval["TYPE"];
                        $arg["data"][] = $setval;
                        $setval = $row;
                    }

                }
                $result->free();
                $arg["data"][] = $setval;
            }
            Query::dbCheckIn($db);
        }

//2:一般
        if ($model->cmd === 'hoken2') {
            $arg["HOKEN2"] = "1";
            $RowH = knjxhokenQuery::getMedexam_hdat($model);      //生徒健康診断ヘッダデータ取得
            $RowD = knjxhokenQuery::getMedexam_det_dat($model);   //生徒健康診断詳細データ取得
            
            $db = Query::dbCheckOut();

            if(isset($model->schregno)){
                //生徒学年クラスを取得
                $result = $db->query(knjxhokenQuery::getSchreg_Regd_Dat($model));
                $RowR = $result->fetchRow(DB_FETCHMODE_ASSOC);
                $result->free();
                $model->GradeClass = $RowR["GRADE"]."-".$RowR["HR_CLASS"];
                $model->Hrname = $RowR["HR_NAME"];
                $model->school_kind = $RowR["SCHOOL_KIND"];
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

            /* 編集項目 */
            /**********************************/
            /***** 1項目目 ********************/
            /**********************************/
            //健康診断実施日付
            $RowH["DATE"] = str_replace("-","/",$RowH["DATE"]);
            $arg["data2"]["DATE"] = View::popUpCalendar($objForm, "DATE" ,$RowH["DATE"]);
            //身長
            $extra = "onblur=\"return Num_Check(this);\"";
            $arg["data2"]["HEIGHT"] = knjCreateTextBox($objForm, $RowD["HEIGHT"], "HEIGHT", 5, 5, $extra);
            //体重
            $extra = "onblur=\"return Num_Check(this);\"";
            $arg["data2"]["WEIGHT"] = knjCreateTextBox($objForm, $RowD["WEIGHT"], "WEIGHT", 5, 5, $extra);
            //座高
            $extra = "onblur=\"return Num_Check(this);\"";
            $arg["data2"]["SITHEIGHT"] = knjCreateTextBox($objForm, $RowD["SITHEIGHT"], "SITHEIGHT", 5, 5, $extra);
            //視力・右裸眼(数字)
            $extra = "onblur=\"return Num_Check(this);\"";
            $arg["data2"]["R_BAREVISION"] = knjCreateTextBox($objForm, $RowD["R_BAREVISION"], "R_BAREVISION", 4, 4, $extra);
            //視力・右矯正(数字)
            $extra = "onblur=\"return Num_Check(this);\"";
            $arg["data2"]["R_VISION"] = knjCreateTextBox($objForm, $RowD["R_VISION"], "R_VISION", 4, 4, $extra);
            //視力・左裸眼(数字)
            $extra = "onblur=\"return Num_Check(this);\"";
            $arg["data2"]["L_BAREVISION"] = knjCreateTextBox($objForm, $RowD["L_BAREVISION"], "L_BAREVISION", 4, 4, $extra);
            //視力・左矯正(数字)
            $extra = "onblur=\"return Num_Check(this);\"";
            $arg["data2"]["L_VISION"] = knjCreateTextBox($objForm, $RowD["L_VISION"], "L_VISION", 4, 4, $extra);
            //視力・右裸眼(文字)
            $extra = "onblur=\"return Mark_Check(this);\"";
            $arg["data2"]["R_BAREVISION_MARK"] = knjCreateTextBox($objForm, $RowD["R_BAREVISION_MARK"], "R_BAREVISION_MARK", 1, 1, $extra);
            //視力・右矯正(文字)
            $extra = "onblur=\"return Mark_Check(this);\"";
            $arg["data2"]["R_VISION_MARK"] = knjCreateTextBox($objForm, $RowD["R_VISION_MARK"], "R_VISION_MARK", 1, 1, $extra);
            //視力・左矯正(文字)
            $extra = "onblur=\"return Mark_Check(this);\"";
            $arg["data2"]["L_BAREVISION_MARK"] = knjCreateTextBox($objForm, $RowD["L_BAREVISION_MARK"], "L_BAREVISION_MARK", 1, 1, $extra);
            //視力・左裸眼(文字)
            $extra = "onblur=\"return Mark_Check(this);\"";
            $arg["data2"]["L_VISION_MARK"] = knjCreateTextBox($objForm, $RowD["L_VISION_MARK"], "L_VISION_MARK", 1, 1, $extra);
            //聴力・右DB
            $extra = "onblur=\"return Num_Check(this);\"";
            $arg["data2"]["R_EAR_DB"] = knjCreateTextBox($objForm, $RowD["R_EAR_DB"], "R_EAR_DB", 4, 3, $extra);
            //聴力・右状態コンボ
            $optnull = array("label" => "","value" => "");   //初期値：空白項目
            $result  = $db->query(knjxhokenQuery::getLR_EAR($model, ""));
            $opt     = array();
            $opt[]   = $optnull;
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $namecd = substr($row["NAMECD2"],0,2);
                $opt[] = array("label" => $namecd."  ".$row["NAME1"],
                               "value" => $row["NAMECD2"]);
            }
            $result->free();
            $extra = "";
            $arg["data2"]["R_EAR"] = knjCreateCombo($objForm, "R_EAR", $RowD["R_EAR"], $opt, $extra, 1);
            //聴力・左DB
            $extra = "onblur=\"return Num_Check(this);\"";
            $arg["data2"]["L_EAR_DB"] = knjCreateTextBox($objForm, $RowD["L_EAR_DB"], "L_EAR_DB", 4, 3, $extra);
            //聴力・左状態コンボ
            $extra = "";
            $arg["data2"]["L_EAR"] = knjCreateCombo($objForm, "L_EAR", $RowD["L_EAR"], $opt, $extra, 1);

            /**********************************/
            /***** 2項目目 ********************/
            /**********************************/
            //尿・１次蛋白コンボ
            //尿・２次蛋白コンボ
            $result = $db->query(knjxhokenQuery::getUric($model, "F020", ""));
            $opt    = array();
            $opt[]  = $optnull;
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $namecd = substr($row["NAMECD2"],0,2);
                $opt[] = array("label" => $namecd."  ".$row["NAME1"],
                               "value" => $row["NAMECD2"]);
            }
            $result->free();
            $extra = "";
            $arg["data2"]["ALBUMINURIA1CD"] = knjCreateCombo($objForm, "ALBUMINURIA1CD", $RowD["ALBUMINURIA1CD"], $opt, $extra, 1);
            $arg["data2"]["ALBUMINURIA2CD"] = knjCreateCombo($objForm, "ALBUMINURIA2CD", $RowD["ALBUMINURIA2CD"], $opt, $extra, 1);
            //尿・１次糖コンボ
            //尿・２次糖コンボ
            $result = $db->query(knjxhokenQuery::getUric($model, "F019", ""));
            $opt    = array();
            $opt[]  = $optnull;
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $namecd = substr($row["NAMECD2"],0,2);
                $opt[] = array("label" => $namecd."  ".$row["NAME1"],
                               "value" => $row["NAMECD2"]);
            }
            $result->free();
            $extra = "";
            $arg["data2"]["URICSUGAR1CD"] = knjCreateCombo($objForm, "URICSUGAR1CD", $RowD["URICSUGAR1CD"], $opt, $extra, 1);
            $arg["data2"]["URICSUGAR2CD"] = knjCreateCombo($objForm, "URICSUGAR2CD", $RowD["URICSUGAR2CD"], $opt, $extra, 1);
            //尿・１次潜血コンボ
            //尿・２次潜血コンボ
            $result = $db->query(knjxhokenQuery::getUric($model, "F018", ""));
            $opt    = array();
            $opt[]  = $optnull;
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $namecd = substr($row["NAMECD2"],0,2);
                $opt[] = array("label" => $namecd."  ".$row["NAME1"],
                               "value" => $row["NAMECD2"]);
            }
            $result->free();
            $extra = "";
            $arg["data2"]["URICBLEED1CD"] = knjCreateCombo($objForm, "URICBLEED1CD", $RowD["URICBLEED1CD"], $opt, $extra, 1);
            $arg["data2"]["URICBLEED2CD"] = knjCreateCombo($objForm, "URICBLEED2CD", $RowD["URICBLEED2CD"], $opt, $extra, 1);
            //尿・その他の検査
            if ($model->Properties["printKenkouSindanIppan"] != "2") {
                $extra = "";
                $arg["data2"]["URICOTHERTEST"] = knjCreateTextBox($objForm, $RowD["URICOTHERTEST"], "URICOTHERTEST", 40, 20, $extra);
            } else {
                $result     = $db->query(knjxhokenQuery::getUriCothertest());
                $opt        = array();
                $opt[]      = $optnull;
                while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $opt[] = array("label" => $row["LABEL"],
                                   "value" => $row["VALUE"]);
                }
                $result->free();
                $extra = "style=\"width:190px;\"";
                $arg["data2"]["URICOTHERTESTCD"] = knjCreateCombo($objForm, "URICOTHERTESTCD", $RowD["URICOTHERTESTCD"], $opt, $extra, 1);
            }
            //尿:指導区分コンボ
            $result     = $db->query(knjxhokenQuery::getUriAdvisecd());
            $opt        = array();
            $opt[]      = $optnull;
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt[] = array("label" => $row["LABEL"],
                               "value" => $row["VALUE"]);
            }
            $result->free();
            $extra = "style=\"width:170px;\"";
            $arg["data2"]["URI_ADVISECD"] = knjCreateCombo($objForm, "URI_ADVISECD", $RowD["URI_ADVISECD"], $opt, $extra, 1);
            //栄養状態コンボ
            $result     = $db->query(knjxhokenQuery::getNutrition($model, ""));
            $opt        = array();
            $opt[]      = $optnull;
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $namecd = substr($row["NAMECD2"],0,2);
                $opt[] = array("label" => $namecd."  ".$row["NAME1"],
                               "value" => $row["NAMECD2"]);
            }
            $result->free();
            $extra = "style=\"width:170px;\"";
            $arg["data2"]["NUTRITIONCD"] = knjCreateCombo($objForm, "NUTRITIONCD", $RowD["NUTRITIONCD"], $opt, $extra, 1);

            //目の疾病及び異常コンボ
            $result     = $db->query(knjxhokenQuery::getEyedisease($model, ""));
            $opt        = array();
            $opt[]      = $optnull;
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $namecd = substr($row["NAMECD2"],0,2);
                $opt[] = array("label" => $namecd."  ".$row["NAME1"],
                               "value" => $row["NAMECD2"]);
            }
            $result->free();
            if ($model->Properties["printKenkouSindanIppan"] == "2") {
                $extra = "style=\"width:170px;\"";
            } else {
                $extra = "style=\"width:170px;\" onChange=\"syokenNyuryoku(this, document.forms[0].EYE_TEST_RESULT)\"";
            }
            $arg["data2"]["EYEDISEASECD"] = knjCreateCombo($objForm, "EYEDISEASECD", $RowD["EYEDISEASECD"], $opt, $extra, 1);
            $arg["data2"]["EYEDISEASECD2"] = knjCreateCombo($objForm, "EYEDISEASECD2", $RowD["EYEDISEASECD2"], $opt, $extra, 1);
            $arg["data2"]["EYEDISEASECD3"] = knjCreateCombo($objForm, "EYEDISEASECD3", $RowD["EYEDISEASECD3"], $opt, $extra, 1);
            $arg["data2"]["EYEDISEASECD4"] = knjCreateCombo($objForm, "EYEDISEASECD4", $RowD["EYEDISEASECD4"], $opt, $extra, 1);
    /*****************************/
            if ((int)$RowD["EYEDISEASECD"] < 2 && $model->Properties["printKenkouSindanIppan"] != "2") {
                $extra = "disabled";
            } else {
                $extra = "";
            }
            $arg["data2"]["EYE_TEST_RESULT"] = knjCreateTextBox($objForm, $RowD["EYE_TEST_RESULT"], "EYE_TEST_RESULT", 40, 20, $extra);
            $arg["data2"]["EYE_TEST_RESULT2"] = knjCreateTextBox($objForm, $RowD["EYE_TEST_RESULT2"], "EYE_TEST_RESULT2", 40, 20, $extra);
            $arg["data2"]["EYE_TEST_RESULT3"] = knjCreateTextBox($objForm, $RowD["EYE_TEST_RESULT3"], "EYE_TEST_RESULT3", 40, 20, $extra);
    /*****************************/

            //脊柱・胸部コンボ
            $result     = $db->query(knjxhokenQuery::getSpinerib($model, ""));
            $opt        = array();
            $opt[]      = $optnull;
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $namecd = substr($row["NAMECD2"],0,2);
                $opt[] = array("label" => $namecd."  ".$row["NAME1"],
                               "value" => $row["NAMECD2"]);
            }
            $result->free();
            if ($model->Properties["printKenkouSindanIppan"] == "2") {
                $extra = "style=\"width:170px;\"";
            } else {
                $extra = "style=\"width:170px;\" onChange=\"syokenNyuryoku(this, document.forms[0].SPINERIBCD_REMARK)\"";
            }
            $arg["data2"]["SPINERIBCD"] = knjCreateCombo($objForm, "SPINERIBCD", $RowD["SPINERIBCD"], $opt, $extra, 1);

    /*****************************/
            //脊柱・胸部
            if ((int)$RowD["SPINERIBCD"] < 2 && $model->Properties["printKenkouSindanIppan"] != "2") {
                $extra = "disabled";
            } else {
                $extra = "";
            }
            $arg["data2"]["SPINERIBCD_REMARK"] = knjCreateTextBox($objForm, $RowD["SPINERIBCD_REMARK"], "SPINERIBCD_REMARK", 40, 20, $extra);
    /*****************************/

            //耳鼻咽頭疾患コンボ
            $result     = $db->query(knjxhokenQuery::getNosedisease($model, ""));
            $opt        = array();
            $opt[]      = $optnull;
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $namecd = substr($row["NAMECD2"],0,2);
                $opt[] = array("label" => $namecd."  ".$row["NAME1"],
                               "value" => $row["NAMECD2"]);
            }
            $result->free();
            if ($model->Properties["printKenkouSindanIppan"] == "2") {
                $extra = "style=\"width:170px;\"";
            } else {
                $extra = "style=\"width:170px;\" onChange=\"syokenNyuryoku(this, document.forms[0].NOSEDISEASECD_REMARK)\"";
            }
            $arg["data2"]["NOSEDISEASECD"] = knjCreateCombo($objForm, "NOSEDISEASECD", $RowD["NOSEDISEASECD"], $opt, $extra, 1);
            $arg["data2"]["NOSEDISEASECD2"] = knjCreateCombo($objForm, "NOSEDISEASECD2", $RowD["NOSEDISEASECD2"], $opt, $extra, 1);
            $arg["data2"]["NOSEDISEASECD3"] = knjCreateCombo($objForm, "NOSEDISEASECD3", $RowD["NOSEDISEASECD3"], $opt, $extra, 1);
            $arg["data2"]["NOSEDISEASECD4"] = knjCreateCombo($objForm, "NOSEDISEASECD4", $RowD["NOSEDISEASECD4"], $opt, $extra, 1);

    /*****************************/
            //耳鼻咽頭疾患
            if ((int)$RowD["NOSEDISEASECD"] < 2 && $model->Properties["printKenkouSindanIppan"] != "2") {
                $extra = "disabled";
            } else {
                $extra = "";
            }
            $arg["data2"]["NOSEDISEASECD_REMARK"] = knjCreateTextBox($objForm, $RowD["NOSEDISEASECD_REMARK"], "NOSEDISEASECD_REMARK", 40, 20, $extra);
            $arg["data2"]["NOSEDISEASECD_REMARK2"] = knjCreateTextBox($objForm, $RowD["NOSEDISEASECD_REMARK2"], "NOSEDISEASECD_REMARK2", 40, 20, $extra);
            $arg["data2"]["NOSEDISEASECD_REMARK3"] = knjCreateTextBox($objForm, $RowD["NOSEDISEASECD_REMARK3"], "NOSEDISEASECD_REMARK3", 40, 20, $extra);
    /*****************************/

            //皮膚疾患コンボ
            $result     = $db->query(knjxhokenQuery::getSkindisease($model, ""));
            $opt        = array();
            $opt[]      = $optnull;
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $namecd = substr($row["NAMECD2"],0,2);
                $opt[] = array("label" => $namecd."  ".$row["NAME1"],
                               "value" => $row["NAMECD2"]);
            }
            $result->free();
            $extra = "style=\"width:170px;\" onChange=\"syokenNyuryoku(this, document.forms[0].SKINDISEASECD_REMARK)\"";
            $arg["data2"]["SKINDISEASECD"] = knjCreateCombo($objForm, "SKINDISEASECD", $RowD["SKINDISEASECD"], $opt, $extra, 1);
            
            //皮膚疾患テキスト
            if ((int)$RowD["SKINDISEASECD"] < 2 && $model->Properties["printKenkouSindanIppan"] != "2") {
                $extra = "disabled";
            } else {
                $extra = "";
            }
            $arg["data2"]["SKINDISEASECD_REMARK"] = knjCreateTextBox($objForm, $RowD["SKINDISEASECD_REMARK"], "SKINDISEASECD_REMARK", 40, 20, $extra);
            
            //心臓・臨床医学的検査コンボ
            $result     = $db->query(knjxhokenQuery::getHeart_medexam($model, ""));
            $opt        = array();
            $opt[]      = $optnull;
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $namecd = substr($row["NAMECD2"],0,2);
                $opt[] = array("label" => $namecd."  ".$row["NAME1"],
                               "value" => $row["NAMECD2"]);
            }
            $result->free();
            if ($model->Properties["printKenkouSindanIppan"] == "2") {
                $extra = "style=\"width:170px;\"";
            } else {
                $extra = "style=\"width:170px;\" onChange=\"syokenNyuryoku(this, document.forms[0].HEART_MEDEXAM_REMARK)\"";
            }
            $arg["data2"]["HEART_MEDEXAM"] = knjCreateCombo($objForm, "HEART_MEDEXAM", $RowD["HEART_MEDEXAM"], $opt, $extra, 1);

    /*****************************/
            //心臓・臨床医学的検査テキスト
            if ((int)$RowD["HEART_MEDEXAM"] < 2 && $model->Properties["printKenkouSindanIppan"] != "2") {
                $extra = "disabled";
            } else {
                $extra = "";
            }
            $arg["data2"]["HEART_MEDEXAM_REMARK"] = knjCreateTextBox($objForm, $RowD["HEART_MEDEXAM_REMARK"], "HEART_MEDEXAM_REMARK", 80, 40, $extra);
    /*****************************/

            //心臓・疾病及び異常コンボ
            $result     = $db->query(knjxhokenQuery::getHeartdisease($model, ""));
            $opt        = array();
            $opt[]      = $optnull;
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $namecd = substr($row["NAMECD2"],0,2);
                $opt[] = array("label" => $namecd."  ".$row["NAME1"],
                               "value" => $row["NAMECD2"]);
            }
            $result->free();
            if ($model->Properties["printKenkouSindanIppan"] == "2") {
                $extra = "style=\"width:170px;\"";
            } else {
                $extra = "style=\"width:170px;\" onChange=\"syokenNyuryoku(this, document.forms[0].HEARTDISEASECD_REMARK)\"";
            }
            $arg["data2"]["HEARTDISEASECD"] = knjCreateCombo($objForm, "HEARTDISEASECD", $RowD["HEARTDISEASECD"], $opt, $extra, 1);

    /*****************************/
            //心臓・疾病及び異常テキスト
            if ((int)$RowD["HEARTDISEASECD"] < 2 && $model->Properties["printKenkouSindanIppan"] != "2") {
                $extra = "disabled";
            } else {
                $extra = "";
            }
            $arg["data2"]["HEARTDISEASECD_REMARK"] = knjCreateTextBox($objForm, $RowD["HEARTDISEASECD_REMARK"], "HEARTDISEASECD_REMARK", 40, 20, $extra);
    /*****************************/

            //心臓・管理区分
            $optnull = array("label" => "","value" => "");   //初期値：空白項目
            $result  = $db->query(knjxhokenQuery::getManagementDiv($model));
            $opt     = array();
            $opt[]   = $optnull;
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $namecd = substr($row["NAMECD2"],0,2);
                $opt[] = array("label" => $namecd."  ".$row["NAME1"],
                               "value" => $row["NAMECD2"]);
            }
            $result->free();
            $extra = "";
            $arg["data2"]["MANAGEMENT_DIV"] = knjCreateCombo($objForm, "MANAGEMENT_DIV", $RowD["MANAGEMENT_DIV"], $opt, $extra, 1);

            //心臓・管理区分テキスト
            $arg["data2"]["MANAGEMENT_REMARK"] = knjCreateTextBox($objForm, $RowD["MANAGEMENT_REMARK"], "MANAGEMENT_REMARK", 40, 20, "");

            /**********************************/
            /***** 3項目目 ********************/
            /**********************************/
            //結核・撮影日付
            $RowD["TB_FILMDATE"] = str_replace("-","/",$RowD["TB_FILMDATE"]);
            $arg["data2"]["TB_FILMDATE"] = View::popUpCalendar($objForm, "TB_FILMDATE" ,$RowD["TB_FILMDATE"]);
            //結核・フィルム番号
            $objForm->ae( array("type"        => "text",
                                "name"        => "TB_FILMNO",
                                "size"        => 6,
                                "maxlength"   => 6,
                                "extrahtml"   => "",
                                "value"       => $RowD["TB_FILMNO"] ));
            $arg["data2"]["TB_FILMNO"] = $objForm->ge("TB_FILMNO");
            //結核・所見コンボ
            $result     = $db->query(knjxhokenQuery::getTb_remark($model, ""));
            $opt        = array();
            $opt[]      = $optnull;
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $namecd = substr($row["NAMECD2"],0,2);
                $opt[] = array("label" => $namecd."  ".$row["NAME1"],
                               "value" => $row["NAMECD2"]);
            }
            $result->free();
            $objForm->ae( array("type"        => "select",
                                "name"        => "TB_REMARKCD",
                                "size"        => "1",
                                "extrahtml"   => "style=\"width:170px;\"",
                                "value"       => $RowD["TB_REMARKCD"],
                                "options"     => $opt ));
            $arg["data2"]["TB_REMARKCD"] = $objForm->ge("TB_REMARKCD");

            //結核検査(X線)
            $extra = "";
            $arg["data2"]["TB_X_RAY"] = knjCreateTextBox($objForm, $RowD["TB_X_RAY"], "TB_X_RAY", 40, 20, $extra);

            //結核・その他検査コンボ
            $result     = $db->query(knjxhokenQuery::getTb_othertest($model, ""));
            $opt        = array();
            $opt[]      = $optnull;
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $namecd = substr($row["NAMECD2"],0,2);
                $opt[] = array("label" => $namecd."  ".$row["NAME1"],
                               "value" => $row["NAMECD2"]);
            }
            $result->free();
            $objForm->ae( array("type"        => "select",
                                "name"        => "TB_OTHERTESTCD",
                                "size"        => "1",
                                "extrahtml"   => "style=\"width:100px;\"",
                                "value"       => $RowD["TB_OTHERTESTCD"],
                                "options"     => $opt ));
            $arg["data2"]["TB_OTHERTESTCD"] = $objForm->ge("TB_OTHERTESTCD");
            $arg["data2"]["TB_OTHERTEST_REMARK1"] = knjCreateTextBox($objForm, $RowD["TB_OTHERTEST_REMARK1"], "TB_OTHERTEST_REMARK1", 40, 20, $extra);

            //結核・病名コンボ
            $result     = $db->query(knjxhokenQuery::getTb_Name($model, ""));
            $opt        = array();
            $opt[]      = $optnull;
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $namecd = substr($row["NAMECD2"],0,2);
                $opt[] = array("label" => $namecd."  ".$row["NAME1"],
                               "value" => $row["NAMECD2"]);
            }
            $result->free();
            $objForm->ae( array("type"        => "select",
                                "name"        => "TB_NAMECD",
                                "size"        => "1",
                                "extrahtml"   => "style=\"width:100px;\"",
                                "value"       => $RowD["TB_NAMECD"],
                                "options"     => $opt ));
            $arg["data2"]["TB_NAMECD"] = $objForm->ge("TB_NAMECD");
            $arg["data2"]["TB_NAME_REMARK1"] = knjCreateTextBox($objForm, $RowD["TB_NAME_REMARK1"], "TB_NAME_REMARK1", 40, 20, $extra);

            //結核・指導区分コンボ
            $result     = $db->query(knjxhokenQuery::getTb_Advise($model));
            $opt        = array();
            $opt[]      = $optnull;
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $namecd = substr($row["NAMECD2"],0,2);
                $opt[] = array("label" => $namecd."  ".$row["NAME1"],
                               "value" => $row["NAMECD2"]);
            }
            $result->free();
            $objForm->ae( array("type"        => "select",
                                "name"        => "TB_ADVISECD",
                                "size"        => "1",
                                "extrahtml"   => "style=\"width:150px;\"",
                                "value"       => $RowD["TB_ADVISECD"],
                                "options"     => $opt ));
            $arg["data2"]["TB_ADVISECD"] = $objForm->ge("TB_ADVISECD");
            $arg["data2"]["TB_ADVISE_REMARK1"] = knjCreateTextBox($objForm, $RowD["TB_ADVISE_REMARK1"], "TB_ADVISE_REMARK1", 40, 20, $extra);

            //寄生虫卵コンボ
            $query = knjxhokenQuery::getNameMst("F023");
            $extra = "";
            makeCmb($objForm, $arg, $db, $query, "PARASITE", $RowD["PARASITE"], $extra, 1, "blank");
            //寄生虫卵表示
            if ($model->school_kind == "P" || ($model->school_kind == "J" && $model->Properties["useParasite_J"] == "1") || ($model->school_kind == "H" && $model->Properties["useParasite_H"] == "1")) {
                $arg["para"] = 1;
                $arg["rowspan3"] = 8;
            } else {
                $arg["rowspan3"] = 7;
            }
            if ($model->Properties["printKenkouSindanIppan"] == "2") {
                $arg["rowspan3"] += 1;
            }

            //その他疾病及び異常コンボ
            $result     = $db->query(knjxhokenQuery::getOther_disease($model, ""));
            $opt        = array();
            $opt[]      = $optnull;
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $namecd = substr($row["NAMECD2"],0,2);
                $opt[] = array("label" => $namecd."  ".$row["NAME1"],
                               "value" => $row["NAMECD2"]);
            }
            $result->free();
            $objForm->ae( array("type"        => "select",
                                "name"        => "OTHERDISEASECD",
                                "size"        => "1",
                                "extrahtml"   => "style=\"width:170px;\"",
                                "value"       => $RowD["OTHERDISEASECD"],
                                "options"     => $opt ));
            $arg["data2"]["OTHERDISEASECD"] = $objForm->ge("OTHERDISEASECD");
            //その他疾病及び異常:指導区分コンボ
            $result     = $db->query(knjxhokenQuery::getOtherAdvisecd());
            $opt        = array();
            $opt[]      = $optnull;
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt[] = array("label" => $row["LABEL"],
                               "value" => $row["VALUE"]);
            }
            $result->free();
            $extra = "style=\"width:170px;\"";
            $arg["data2"]["OTHER_ADVISECD"] = knjCreateCombo($objForm, "OTHER_ADVISECD", $RowD["OTHER_ADVISECD"], $opt, $extra, 1);

            //その他疾病及び異常:所見1
            $arg["data2"]["OTHER_REMARK"] = knjCreateTextBox($objForm, $RowD["OTHER_REMARK"], "OTHER_REMARK", 40, 20, "");
            //その他疾病及び異常:所見2
            $arg["data2"]["OTHER_REMARK2"] = knjCreateTextBox($objForm, $RowD["OTHER_REMARK2"], "OTHER_REMARK2", 40, 20, "");
            //その他疾病及び異常:所見3
            $arg["data2"]["OTHER_REMARK3"] = knjCreateTextBox($objForm, $RowD["OTHER_REMARK3"], "OTHER_REMARK3", 40, 20, "");

            //学校医（内科検診）
            $result     = $db->query(knjxhokenQuery::getDoc_Cd($model));
            $opt        = array();
            $opt[]      = $optnull;
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $namecd = substr($row["NAMECD2"],0,2);
                $opt[] = array("label" => $namecd."  ".$row["NAME1"],
                               "value" => $row["NAMECD2"]);
            }
            $result->free();
            $objForm->ae( array("type"        => "select",
                                "name"        => "DOC_CD",
                                "size"        => "1",
                                "extrahtml"   => "style=\"width:170px;\"",
                                "value"       => $RowD["DOC_CD"],
                                "options"     => $opt ));
            $arg["data2"]["DOC_CD"] = $objForm->ge("DOC_CD");

            //学校医・所見（内科検診）
            if ($model->Properties["printKenkouSindanIppan"] == "1") {
                $arg["data2"]["DOC_REMARK"] = knjCreateTextBox($objForm, $RowD["DOC_REMARK"], "DOC_REMARK", 60, 30, "");
            } else {
                $arg["data2"]["DOC_REMARK"] = knjCreateTextBox($objForm, $RowD["DOC_REMARK"], "DOC_REMARK", 40, 20, "");
            }

            //学校医・所見日付
            $RowD["DOC_DATE"] = str_replace("-","/",$RowD["DOC_DATE"]);
            $arg["data2"]["DOC_DATE"] = View::popUpCalendar($objForm, "DOC_DATE" ,$RowD["DOC_DATE"]);

            //事後措置コンボ
            $result     = $db->query(knjxhokenQuery::getTreat($model, ""));
            $opt        = array();
            $opt[]      = $optnull;
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $namecd = substr($row["NAMECD2"],0,2);
                $opt[] = array("label" => $namecd."  ".$row["NAME1"],
                               "value" => $row["NAMECD2"]);
            }
            $result->free();
            $objForm->ae( array("type"        => "select",
                                "name"        => "TREATCD",
                                "size"        => "1",
                                "extrahtml"   => "style=\"width:170px;\"",
                                "value"       => $RowD["TREATCD"],
                                "options"     => $opt ));
            $arg["data2"]["TREATCD"] = $objForm->ge("TREATCD");

            //事後措置:所見1
            $arg["data2"]["TREAT_REMARK1"] = knjCreateTextBox($objForm, $RowD["TREAT_REMARK1"], "TREAT_REMARK1", 40, 20, "");
            //事後措置:所見2
            $arg["data2"]["TREAT_REMARK2"] = knjCreateTextBox($objForm, $RowD["TREAT_REMARK2"], "TREAT_REMARK2", 40, 20, "");
            //事後措置:所見3
            $arg["data2"]["TREAT_REMARK3"] = knjCreateTextBox($objForm, $RowD["TREAT_REMARK3"], "TREAT_REMARK3", 40, 20, "");

            //備考
            $arg["data2"]["REMARK"] = knjCreateTextBox($objForm, $RowD["REMARK"], "REMARK", 120, 200, "");

            //メッセージ
            $gyo = 4;
            $moji = 21;
            $arg["data2"]["MESSAGE"] = KnjCreateTextArea($objForm, "MESSAGE", $gyo, ($moji * 2 + 1), "soft", "", $RowD["MESSAGE"]);

            /**********************************/
            /***** 4項目目 ********************/
            /**********************************/
            //既往症
            $result     = $db->query(knjxhokenQuery::getMedicalHist());
            $opt        = array();
            $opt[]      = $optnull;
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt[] = array("label" => $row["LABEL"],
                               "value" => $row["VALUE"]);
            }
            $result->free();
            $extra = "";
            $arg["data2"]["MEDICAL_HISTORY1"] = knjCreateCombo($objForm, "MEDICAL_HISTORY1", $RowD["MEDICAL_HISTORY1"], $opt, $extra, 1);
            $arg["data2"]["MEDICAL_HISTORY2"] = knjCreateCombo($objForm, "MEDICAL_HISTORY2", $RowD["MEDICAL_HISTORY2"], $opt, $extra, 1);
            $arg["data2"]["MEDICAL_HISTORY3"] = knjCreateCombo($objForm, "MEDICAL_HISTORY3", $RowD["MEDICAL_HISTORY3"], $opt, $extra, 1);

            //診断名
            $extra = "";
            $arg["data2"]["DIAGNOSIS_NAME"] = knjCreateTextBox($objForm, $RowD["DIAGNOSIS_NAME"], "DIAGNOSIS_NAME", 100, 50, $extra);


            //運動/指導区分
            $result     = $db->query(knjxhokenQuery::getGuideDiv($model));
            $opt        = array();
            $opt[]      = $optnull;
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $opt[] = array("label" => $row["LABEL"],
                               "value" => $row["VALUE"]);
            }
            $result->free();
            $objForm->ae( array("type"        => "select",
                                "name"        => "GUIDE_DIV",
                                "size"        => "1",
                                "extrahtml"   => "",
                                "value"       => $RowD["GUIDE_DIV"],
                                "options"     => $opt ));
            $arg["data2"]["GUIDE_DIV"] = $objForm->ge("GUIDE_DIV");

            //運動/部活動
            $result     = $db->query(knjxhokenQuery::getJoiningSportsClub($model));
            $opt        = array();
            $opt[]      = $optnull;
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $opt[] = array("label" => $row["LABEL"],
                               "value" => $row["VALUE"]);
            }
            $result->free();
            $objForm->ae( array("type"        => "select",
                                "name"        => "JOINING_SPORTS_CLUB",
                                "size"        => "1",
                                "extrahtml"   => "style=\"width:170px;\"",
                                "value"       => $RowD["JOINING_SPORTS_CLUB"],
                                "options"     => $opt ));
            $arg["data2"]["JOINING_SPORTS_CLUB"] = $objForm->ge("JOINING_SPORTS_CLUB");

            Query::dbCheckIn($db);
        }

//3:歯・口腔
        if ($model->cmd === 'hoken3') {
            $arg["HOKEN3"] = "1";
            $RowH = knjxhokenQuery::getMedexam_hdat($model);      //生徒健康診断ヘッダデータ取得
            $RowT = knjxhokenQuery::getMedexam_tooth_dat($model); //生徒健康診断歯口腔データ取得
            
            $db = Query::dbCheckOut();
            if (isset($model->schregno)) {
                //生徒学年クラスを取得
                $result = $db->query(knjxhokenQuery::getSchregRegdDat_data($model));
                $RowR = $result->fetchRow(DB_FETCHMODE_ASSOC);
                $result->free();
                $model->GradeClass = $RowR["GRADE"]."-".$RowR["HR_CLASS"];
                $model->Hrname = $RowR["HR_NAME"];
            } else {
                //学籍番号
                $arg["header"]["SCHREGNO"] = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
                //生徒氏名
                $arg["header"]["NAME_SHOW"] = "&nbsp;&nbsp;&nbsp;&nbsp;";
                //生年月日
                $arg["header"]["BIRTHDAY"] = "&nbsp;&nbsp;&nbsp;&nbsp;年&nbsp;&nbsp;&nbsp;&nbsp;月&nbsp;&nbsp;&nbsp;&nbsp;日";
            }

            //レイアウト調整用
            $arg["data3"]["SPAN1"] = ($model->Properties["printKenkouSindanIppan"] == "2" || $model->Properties["printKenkouSindanIppan"] == "3") ? 3 : 2;
            $arg["data3"]["SPAN2"] = ($model->Properties["printKenkouSindanIppan"] == "2" || $model->Properties["printKenkouSindanIppan"] == "3") ? 2 : 1;

            /* 編集項目 */
            //健康診断実施日付
            $RowH["TOOTH_DATE"] = str_replace("-","/",$RowH["TOOTH_DATE"]);
            $arg["data3"]["TOOTH_DATE"] = View::popUpCalendar($objForm, "TOOTH_DATE" ,$RowH["TOOTH_DATE"]);

            //歯列・咬合コンボボックス
            $query = knjxhokenQuery::getNameMst("F510");
            $extra = "style=\"width:250px;\"";
            makeCombo($objForm, $arg, $db, $query, $RowT["JAWS_JOINTCD"], "JAWS_JOINTCD", $extra, 1, "BLANK");

            if ($model->Properties["printKenkouSindanIppan"] == "2" || $model->Properties["printKenkouSindanIppan"] == "3") {
                $arg["data3"]["JAWS_JOINTCD_LABEL"] = '★ 歯列';
                $arg["kuma_tokiwa_miyagi"] = 1;
                $arg["Ippan".$model->Properties["printKenkouSindanIppan"]] = "1";//Ippan2:熊本、Ippan3:常磐、宮城
            } else {
                $arg["data3"]["JAWS_JOINTCD_LABEL"] = '★ 歯列・咬合';
                $arg["not_kuma_tokiwa_miyagi"] = 1;
            }

            //咬合コンボボックス
            $query = knjxhokenQuery::getNameMst("F512");
            $extra = "style=\"width:250px;\"";
            makeCombo($objForm, $arg, $db, $query, $RowT["JAWS_JOINTCD3"], "JAWS_JOINTCD3", $extra, 1, "BLANK");

            //顎関節コンボボックス
            $query = knjxhokenQuery::getNameMst("F511");
            $extra = "style=\"width:250px;\"";
            makeCombo($objForm, $arg, $db, $query, $RowT["JAWS_JOINTCD2"], "JAWS_JOINTCD2", $extra, 1, "BLANK");

            //歯垢の状態コンボ
            $query = knjxhokenQuery::getNameMst("F520");
            $extra = "style=\"width:150px;\"";
            makeCombo($objForm, $arg, $db, $query, $RowT["PLAQUECD"], "PLAQUECD", $extra, 1, "BLANK");

            //歯肉の状態コンボ
            $query = knjxhokenQuery::getNameMst("F513");
            $extra = "style=\"width:250px;\"";
            makeCombo($objForm, $arg, $db, $query, $RowT["GUMCD"], "GUMCD", $extra, 1, "BLANK");

            //歯石沈着コンボ
            $query = knjxhokenQuery::getNameMst("F521");
            $extra = "style=\"width:250px;\"";
            makeCombo($objForm, $arg, $db, $query, $RowT["CALCULUS"], "CALCULUS", $extra, 1, "BLANK");

            //矯正
            if ($RowT["ORTHODONTICS"] == 1) {
                $extra = "onclick=\"checkAri_Nasi(this, 'ari_nasi');\" checked='checked'";
                $ari_nasi = "<span id='ari_nasi' style='color:black;'>有</span>";
            } else {
                $extra = "onclick=\"checkAri_Nasi(this, 'ari_nasi');\"";
                $ari_nasi = "<span id='ari_nasi' style='color:black;'>無</span>";
            }
            $arg["data3"]["ORTHODONTICS"] = knjCreateCheckBox($objForm, "ORTHODONTICS", '1', $extra).$ari_nasi;

            //乳歯・現在数
            $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
            $arg["data3"]["BABYTOOTH"] = knjCreateTextBox($objForm, $RowT["BABYTOOTH"], "BABYTOOTH", 2, 2, $extra);

            //乳歯・未処置数
            $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
            $arg["data3"]["REMAINBABYTOOTH"] = knjCreateTextBox($objForm, $RowT["REMAINBABYTOOTH"], "REMAINBABYTOOTH", 2, 2, $extra);

            //乳歯・処置数
            $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
            $arg["data3"]["TREATEDBABYTOOTH"] = knjCreateTextBox($objForm, $RowT["TREATEDBABYTOOTH"], "TREATEDBABYTOOTH", 2, 2, $extra);

            //乳歯・要注意乳歯数
            $extra = ($model->Properties["printKenkouSindanIppan"] == "2" || $model->Properties["printKenkouSindanIppan"] == "3") ? "style=\"text-align:right;background-color:darkgray\" readonly" : "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
            $arg["data3"]["BRACK_BABYTOOTH"] = knjCreateTextBox($objForm, $RowT["BRACK_BABYTOOTH"], "BRACK_BABYTOOTH", 2, 2, $extra);

            //永久歯・現在数
            $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
            $arg["data3"]["ADULTTOOTH"] = knjCreateTextBox($objForm, $RowT["ADULTTOOTH"], "ADULTTOOTH", 2, 2, $extra);

            //永久歯・未処置数
            $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
            $arg["data3"]["REMAINADULTTOOTH"] = knjCreateTextBox($objForm, $RowT["REMAINADULTTOOTH"], "REMAINADULTTOOTH", 2, 2, $extra);

            //永久歯・処置数
            $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
            $arg["data3"]["TREATEDADULTTOOTH"] = knjCreateTextBox($objForm, $RowT["TREATEDADULTTOOTH"], "TREATEDADULTTOOTH", 2, 2, $extra);

            //永久歯・喪失数
            $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
            $arg["data3"]["LOSTADULTTOOTH"] = knjCreateTextBox($objForm, $RowT["LOSTADULTTOOTH"], "LOSTADULTTOOTH", 2, 2, $extra);

            //永久歯・要観察歯数
            $extra = ($model->Properties["printKenkouSindanIppan"] == "2" || $model->Properties["printKenkouSindanIppan"] == "3") ? "style=\"text-align:right;background-color:darkgray\" readonly" : "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
            $arg["data3"]["BRACK_ADULTTOOTH"] = knjCreateTextBox($objForm, $RowT["BRACK_ADULTTOOTH"], "BRACK_ADULTTOOTH", 2, 2, $extra);

            //永久歯・要精検歯数
            $extra = ($model->Properties["printKenkouSindanIppan"] == "2" || $model->Properties["printKenkouSindanIppan"] == "3") ? "style=\"text-align:right;background-color:darkgray\" readonly" : "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
            $arg["data3"]["CHECKADULTTOOTH"] = knjCreateTextBox($objForm, $RowT["CHECKADULTTOOTH"], "CHECKADULTTOOTH", 2, 2, $extra);

            //その他疾病及び異常コンボ
            $query = knjxhokenQuery::getNameMst("F530");
            $extra = "style=\"width:120px;\" onclick=\"OptionUse(this);\"";
            makeCombo($objForm, $arg, $db, $query, $RowT["OTHERDISEASECD"], "OTHERDISEASECD", $extra, 1, "BLANK");

            //その他疾病及び異常テキスト
            $extra = ($RowT["OTHERDISEASECD"] == '99' || $model->Properties["printKenkouSindanIppan"] == "2" || $model->Properties["printKenkouSindanIppan"] == "3") ? "" : "disabled style=\"background-color:darkgray\"";
            $arg["data3"]["OTHERDISEASE"] = knjCreateTextBox($objForm, $RowT["OTHERDISEASE"], "OTHERDISEASE", 40, 60, $extra);

            //所見コンボボックス
            $query = knjxhokenQuery::getNameMst("F540");
            $extra = "style=\"width:150px;\"";
            makeCombo($objForm, $arg, $db, $query, $RowT["DENTISTREMARKCD"], "DENTISTREMARKCD", $extra, 1, "BLANK");

            //所見(CO)本数
            $extra = ($model->Properties["printKenkouSindanIppan"] == "2") ? "style=\"text-align:right;background-color:darkgray\" readonly" : "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
            $arg["data3"]["DENTISTREMARK_CO"] = knjCreateTextBox($objForm, $RowT["DENTISTREMARK_CO"], "DENTISTREMARK_CO", 2, 2, $extra);

            //所見(GO)チェックボックス
            if ($RowT["DENTISTREMARK_GO"] == 1) {
                $extra = "onclick=\"checkAri_Nasi(this, 'ari_nasi_go');\" checked='checked'";
                $ari_nasi_go = "<span id='ari_nasi_go' style='color:black;'>有</span>";
            } else {
                $extra = "onclick=\"checkAri_Nasi(this, 'ari_nasi_go');\"";
                $ari_nasi_go = "<span id='ari_nasi_go' style='color:black;'>無</span>";
            }
            $arg["data3"]["DENTISTREMARK_GO"] = knjCreateCheckBox($objForm, "DENTISTREMARK_GO", '1', $extra).$ari_nasi_go;

            //所見(G)チェックボックス
            if ($RowT["DENTISTREMARK_G"] == 1) {
                $extra = "onclick=\"checkAri_Nasi(this, 'ari_nasi_g');\" checked='checked'";
                $ari_nasi_g = "<span id='ari_nasi_g' style='color:black;'>有</span>";
            } else {
                $extra = "onclick=\"checkAri_Nasi(this, 'ari_nasi_g');\"";
                $ari_nasi_g = "<span id='ari_nasi_g' style='color:black;'>無</span>";
            }
            $arg["data3"]["DENTISTREMARK_G"] = knjCreateCheckBox($objForm, "DENTISTREMARK_G", '1', $extra).$ari_nasi_g;

            //所見日付
            $RowT["DENTISTREMARKDATE"] = str_replace("-","/",$RowT["DENTISTREMARKDATE"]);
            $arg["data3"]["DENTISTREMARKDATE"] = View::popUpCalendar($objForm, "DENTISTREMARKDATE" ,$RowT["DENTISTREMARKDATE"]);

            //事後措置コンボ
            $query = knjxhokenQuery::getNameMst("F541");
            $extra = "style=\"width:250px;\"";
            makeCombo($objForm, $arg, $db, $query, $RowT["DENTISTTREATCD"], "DENTISTTREATCD", $extra, 1, "BLANK");

            //事後措置テキスト
            $extra =  "";
            $arg["data3"]["DENTISTTREAT"] = knjCreateTextBox($objForm, $RowT["DENTISTTREAT"], "DENTISTTREAT", 20, 30, $extra);

            //事後措置テキスト2
            $extra =  "";
            $arg["data3"]["DENTISTTREAT2"] = knjCreateTextBox($objForm, $RowT["DENTISTTREAT2"], "DENTISTTREAT2", 20, 30, $extra);

            //事後措置テキスト3
            $extra =  "";
            $arg["data3"]["DENTISTTREAT3"] = knjCreateTextBox($objForm, $RowT["DENTISTTREAT3"], "DENTISTTREAT3", 20, 30, $extra);

            Query::dbCheckIn($db);
        }

//4:相談記録
        if ($model->cmd === 'hoken4') {
            $db = Query::dbCheckOut();
            $arg["HOKEN4"] = "1";

            //データを取得
            if ($model->schregno) {
                $result = $db->query(knjxhokenQuery::sub4Query($model));
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $row["VISIT_DATE"] = str_replace("-", "/", $row["VISIT_DATE"]).' '.$row["VISIT_HOUR"].':'.$row["VISIT_MINUTE"];

                    $arg["data"][] = $row;
                }
                $result->free();
            }

            Query::dbCheckIn($db);
        }

        //終了ボタンを作成する
        if ($model->buttonFlg) {
            $extra = "onclick=\"closeWin()\"";
        } else {
            $extra = "onclick=\"return parent.closeit()\"";
        }
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJXHOKEN");
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "YEAR", $model->year);
        knjCreateHidden($objForm, "BUTTON_FLG", $model->buttonFlg);

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを$arg経由で渡す
        View::toHTML($model, "knjxhokenForm1.html", $arg);
    }
}

//コンボ作成(2:一般用)
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if ($blank) $opt[] = array();
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data2"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
//コンボ作成(3:歯・口腔用)
function makeCombo(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array ("label" => "",
                        "value" => "");
    }
    $result = $db->query($query);

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
    }
    $result->free();

    $value = ($value) ? $value : $opt[0]["value"];
    $arg["data3"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

?>