<?php

require_once('for_php7.php');
class knjf010Form1
{
    function main(&$model)
    {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knjf010index.php", "", "edit");

        //警告メッセージを表示しない場合
        if(!isset($model->warning) && isset($model->schregno)){
            $RowH = knjf010Query::getMedexam_hdat($model);      //生徒健康診断ヘッダデータ取得
            $RowD = knjf010Query::getMedexam_det_dat($model);   //生徒健康診断詳細データ取得
            $arg["NOT_WARNING"] = 1;
        }else{
            $RowH =& $model->field;
            $RowD =& $model->field;
        }

        $db     = Query::dbCheckOut();

        /* ヘッダ */
        if(isset($model->schregno)){
            //生徒学籍データを取得
            $result = $db->query(knjf010Query::getSchreg_Base_Mst($model));
            $RowB = $result->fetchRow(DB_FETCHMODE_ASSOC);
            $result->free();
            //生徒学籍番号
            $arg["header"]["SCHREGNO"] = $model->schregno;
            //生徒名前
            $arg["header"]["NAME_SHOW"] = $model->name;
            //生徒生年月日
            $birth_day = explode("-",$RowB["BIRTHDAY"]);
            $arg["header"]["BIRTHDAY"] = $birth_day[0]."年".$birth_day[1]."月".$birth_day[2]."日";
            //生徒学年クラスを取得
            $result = $db->query(knjf010Query::getSchreg_Regd_Dat($model));
            $RowR = $result->fetchRow(DB_FETCHMODE_ASSOC);
            $result->free();
            $model->GradeClass = $RowR["GRADE"]."-".$RowR["HR_CLASS"];
            $model->Hrname = $RowR["HR_NAME"];
        }else{
            //学籍番号
            $arg["header"]["SCHREGNO"] = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
            //生徒氏名
            $arg["header"]["NAME_SHOW"] = "&nbsp;&nbsp;&nbsp;&nbsp;";
            //生年月日
            $arg["header"]["BIRTHDAY"] = "&nbsp;&nbsp;&nbsp;&nbsp;年&nbsp;&nbsp;&nbsp;&nbsp;月&nbsp;&nbsp;&nbsp;&nbsp;日";
        }

/* 編集項目 */
        /**********************************/
        /***** 1項目目 ********************/
        /**********************************/
        //健康診断実施日付
        $RowH["DATE"] = str_replace("-","/",$RowH["DATE"]);
        $arg["data"]["DATE"] = View::popUpCalendar($objForm, "DATE" ,$RowH["DATE"]);
        //身長
        $objForm->ae( array("type"        => "text",
                            "name"        => "HEIGHT",
                            "size"        => 5,
                            "maxlength"   => 5,
                            "extrahtml"   => "onblur=\"return Num_Check(this);\"",
                            "value"       => $RowD["HEIGHT"] ));
        $arg["data"]["HEIGHT"] = $objForm->ge("HEIGHT");
        //体重
        $objForm->ae( array("type"        => "text",
                            "name"        => "WEIGHT",
                            "size"        => 5,
                            "maxlength"   => 5,
                            "extrahtml"   => "onblur=\"return Num_Check(this);\"",
                            "value"       => $RowD["WEIGHT"] ));
        $arg["data"]["WEIGHT"] = $objForm->ge("WEIGHT");
        //座高
        $objForm->ae( array("type"        => "text",
                            "name"        => "SITHEIGHT",
                            "size"        => 5,
                            "maxlength"   => 5,
                            "extrahtml"   => "onblur=\"return Num_Check(this);\"",
                            "value"       => $RowD["SITHEIGHT"] ));
        $arg["data"]["SITHEIGHT"] = $objForm->ge("SITHEIGHT");
        //視力・右裸眼（数字）
        $objForm->ae( array("type"        => "text",
                            "name"        => "R_BAREVISION",
                            "size"        => 4,
                            "maxlength"   => 4,
                            "extrahtml"   => "onblur=\"return Num_Check(this);\"",
                            "value"       => $RowD["R_BAREVISION"] ));
        $arg["data"]["R_BAREVISION"] = $objForm->ge("R_BAREVISION");
        //視力・右矯正（数字）
        $objForm->ae( array("type"        => "text",
                            "name"        => "R_VISION",
                            "size"        => 4,
                            "maxlength"   => 4,
                            "extrahtml"   => "onblur=\"return Num_Check(this);\"",
                            "value"       => $RowD["R_VISION"] ));
        $arg["data"]["R_VISION"] = $objForm->ge("R_VISION");
        //視力・左裸眼（数字）
        $objForm->ae( array("type"        => "text",
                            "name"        => "L_BAREVISION",
                            "size"        => 4,
                            "maxlength"   => 4,
                            "extrahtml"   => "onblur=\"return Num_Check(this);\"",
                            "value"       => $RowD["L_BAREVISION"] ));
        $arg["data"]["L_BAREVISION"] = $objForm->ge("L_BAREVISION");
        //視力・左矯正（数字）
        $objForm->ae( array("type"        => "text",
                            "name"        => "L_VISION",
                            "size"        => 4,
                            "maxlength"   => 4,
                            "extrahtml"   => "onblur=\"return Num_Check(this);\"",
                            "value"       => $RowD["L_VISION"] ));
        $arg["data"]["L_VISION"] = $objForm->ge("L_VISION");
        //視力・右裸眼（文字）
        makeTextBox($objForm, $arg, "R_BAREVISION_MARK", 1, 1, $RowD["R_BAREVISION_MARK"], "onblur=\"return Mark_Check(this);\"");

        //視力・右矯正（文字）
        makeTextBox($objForm, $arg, "R_VISION_MARK", 1, 1, $RowD["R_VISION_MARK"], "onblur=\"return Mark_Check(this);\"");

        //視力・左矯正（文字）
        makeTextBox($objForm, $arg, "L_BAREVISION_MARK", 1, 1, $RowD["L_BAREVISION_MARK"], "onblur=\"return Mark_Check(this);\"");

        //視力・左裸眼（文字）
        makeTextBox($objForm, $arg, "L_VISION_MARK", 1, 1, $RowD["L_VISION_MARK"], "onblur=\"return Mark_Check(this);\"");

        //聴力・右DB
        $objForm->ae( array("type"        => "text",
                            "name"        => "R_EAR_DB",
                            "size"        => 4,
                            "maxlength"   => 3,
                            "extrahtml"   => "onblur=\"return Num_Check(this);\"",
                            "value"       => $RowD["R_EAR_DB"] ));
        $arg["data"]["R_EAR_DB"] = $objForm->ge("R_EAR_DB");
        //聴力・右状態コンボ
        $optnull    = array("label" => "","value" => "");   //初期値：空白項目
        $result     = $db->query(knjf010Query::getLR_EAR($model));
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
                            "name"        => "R_EAR",
                            "size"        => "1",
                            "extrahtml"   => "style=\"width:100px;\"",
                            "value"       => $RowD["R_EAR"],
                            "options"     => $opt ));
        $arg["data"]["R_EAR"] = $objForm->ge("R_EAR");
        //聴力・左DB
        $objForm->ae( array("type"        => "text",
                            "name"        => "L_EAR_DB",
                            "size"        => 4,
                            "maxlength"   => 3,
                            "extrahtml"   => "onblur=\"return Num_Check(this);\"",
                            "value"       => $RowD["L_EAR_DB"] ));
        $arg["data"]["L_EAR_DB"] = $objForm->ge("L_EAR_DB");
        //聴力・左状態コンボ
        $objForm->ae( array("type"        => "select",
                            "name"        => "L_EAR",
                            "size"        => "1",
                            "extrahtml"   => "style=\"width:100px;\"",
                            "value"       => $RowD["L_EAR"],
                            "options"     => $opt ));
        $arg["data"]["L_EAR"] = $objForm->ge("L_EAR");
        /**********************************/
        /***** 2項目目 ********************/
        /**********************************/
        //尿・１次蛋白コンボ
        $result     = $db->query(knjf010Query::getUric($model));
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
                            "name"        => "ALBUMINURIA1CD",
                            "size"        => "1",
                            "extrahtml"   => "style=\"width:70px;\"",
                            "value"       => $RowD["ALBUMINURIA1CD"],
                            "options"     => $opt ));
        $arg["data"]["ALBUMINURIA1CD"] = $objForm->ge("ALBUMINURIA1CD");
        //尿・１次糖コンボ
        $objForm->ae( array("type"        => "select",
                            "name"        => "URICSUGAR1CD",
                            "size"        => "1",
                            "extrahtml"   => "style=\"width:70px;\"",
                            "value"       => $RowD["URICSUGAR1CD"],
                            "options"     => $opt ));
        $arg["data"]["URICSUGAR1CD"] = $objForm->ge("URICSUGAR1CD");
        //尿・１次潜血コンボ
        $objForm->ae( array("type"        => "select",
                            "name"        => "URICBLEED1CD",
                            "size"        => "1",
                            "extrahtml"   => "style=\"width:70px;\"",
                            "value"       => $RowD["URICBLEED1CD"],
                            "options"     => $opt ));
        $arg["data"]["URICBLEED1CD"] = $objForm->ge("URICBLEED1CD");
        //尿・２次蛋白コンボ
        $objForm->ae( array("type"        => "select",
                            "name"        => "ALBUMINURIA2CD",
                            "size"        => "1",
                            "extrahtml"   => "style=\"width:70px;\"",
                            "value"       => $RowD["ALBUMINURIA2CD"],
                            "options"     => $opt ));
        $arg["data"]["ALBUMINURIA2CD"] = $objForm->ge("ALBUMINURIA2CD");
        //尿・２次糖コンボ
        $objForm->ae( array("type"        => "select",
                            "name"        => "URICSUGAR2CD",
                            "size"        => "1",
                            "extrahtml"   => "style=\"width:70px;\"",
                            "value"       => $RowD["URICSUGAR2CD"],
                            "options"     => $opt ));
        $arg["data"]["URICSUGAR2CD"] = $objForm->ge("URICSUGAR2CD");
        //尿・２次潜血コンボ
        $objForm->ae( array("type"        => "select",
                            "name"        => "URICBLEED2CD",
                            "size"        => "1",
                            "extrahtml"   => "style=\"width:70px;\"",
                            "value"       => $RowD["URICBLEED2CD"],
                            "options"     => $opt ));
        $arg["data"]["URICBLEED2CD"] = $objForm->ge("URICBLEED2CD");
        //尿・その他の検査
        $objForm->ae( array("type"        => "text",
                            "name"        => "URICOTHERTEST",
                            "size"        => 40,//NO004
                            "maxlength"   => 20,//NO004
                            "extrahtml"   => "",
                            "value"       => $RowD["URICOTHERTEST"] ));
        $arg["data"]["URICOTHERTEST"] = $objForm->ge("URICOTHERTEST");
        //栄養状態コンボ
        $result     = $db->query(knjf010Query::getNutrition($model));
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
                            "name"        => "NUTRITIONCD",
                            "size"        => "1",
                            "extrahtml"   => "style=\"width:170px;\"",
                            "value"       => $RowD["NUTRITIONCD"],
                            "options"     => $opt ));
        $arg["data"]["NUTRITIONCD"] = $objForm->ge("NUTRITIONCD");
        //脊柱・胸部コンボ
        $result     = $db->query(knjf010Query::getSpinerib($model));
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
                            "name"        => "SPINERIBCD",
                            "size"        => "1",
                            "extrahtml"   => "style=\"width:170px;\"",
                            "value"       => $RowD["SPINERIBCD"],
                            "options"     => $opt ));
        $arg["data"]["SPINERIBCD"] = $objForm->ge("SPINERIBCD");
        //目の疾病及び異常コンボ
        $result     = $db->query(knjf010Query::getEyedisease($model));
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
                            "name"        => "EYEDISEASECD",
                            "size"        => "1",
                            "extrahtml"   => "style=\"width:170px;\"",
                            "value"       => $RowD["EYEDISEASECD"],
                            "options"     => $opt ));
        $arg["data"]["EYEDISEASECD"] = $objForm->ge("EYEDISEASECD");
        //耳鼻咽頭疾患コンボ
        $result     = $db->query(knjf010Query::getNosedisease($model));
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
                            "name"        => "NOSEDISEASECD",
                            "size"        => "1",
                            "extrahtml"   => "style=\"width:170px;\"",
                            "value"       => $RowD["NOSEDISEASECD"],
                            "options"     => $opt ));
        $arg["data"]["NOSEDISEASECD"] = $objForm->ge("NOSEDISEASECD");

        //眼科検診結果
        $extra = "";
        $arg["data"]["EYE_TEST_RESULT"] = knjCreateTextBox($objForm, $RowD["EYE_TEST_RESULT"], "EYE_TEST_RESULT", 40, 20, $extra);

        //皮膚疾患コンボ
        $result     = $db->query(knjf010Query::getSkindisease($model));
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
                            "name"        => "SKINDISEASECD",
                            "size"        => "1",
                            "extrahtml"   => "style=\"width:170px;\"",
                            "value"       => $RowD["SKINDISEASECD"],
                            "options"     => $opt ));
        $arg["data"]["SKINDISEASECD"] = $objForm->ge("SKINDISEASECD");
        //心臓・臨床医学的検査コンボ
        $result     = $db->query(knjf010Query::getHeart_medexam($model));
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
                            "name"        => "HEART_MEDEXAM",
                            "size"        => "1",
                            "extrahtml"   => "style=\"width:170px;\"",
                            "value"       => $RowD["HEART_MEDEXAM"],
                            "options"     => $opt ));
        $arg["data"]["HEART_MEDEXAM"] = $objForm->ge("HEART_MEDEXAM");
        //心臓・臨床医学的検査テキスト---NO005
        $objForm->ae( array("type"        => "text",
                            "name"        => "HEART_MEDEXAM_REMARK",
                            "size"        => 60,
                            "maxlength"   => 120,
                            "extrahtml"   => "",
                            "value"       => $RowD["HEART_MEDEXAM_REMARK"] ));
        $arg["data"]["HEART_MEDEXAM_REMARK"] = $objForm->ge("HEART_MEDEXAM_REMARK");
        //心臓・疾病及び異常コンボ
        $result     = $db->query(knjf010Query::getHeartdisease($model));
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
                            "name"        => "HEARTDISEASECD",
                            "size"        => "1",
                            "extrahtml"   => "style=\"width:170px;\"",
                            "value"       => $RowD["HEARTDISEASECD"],
                            "options"     => $opt ));
        $arg["data"]["HEARTDISEASECD"] = $objForm->ge("HEARTDISEASECD");
        /**********************************/
        /***** 3項目目 ********************/
        /**********************************/
        //結核・撮影日付
        $RowD["TB_FILMDATE"] = str_replace("-","/",$RowD["TB_FILMDATE"]);
        $arg["data"]["TB_FILMDATE"] = View::popUpCalendar($objForm, "TB_FILMDATE" ,$RowD["TB_FILMDATE"]);
        //結核・フィルム番号
        $objForm->ae( array("type"        => "text",
                            "name"        => "TB_FILMNO",
                            "size"        => 6,
                            "maxlength"   => 6,
                            "extrahtml"   => "",
                            "value"       => $RowD["TB_FILMNO"] ));
        $arg["data"]["TB_FILMNO"] = $objForm->ge("TB_FILMNO");
        //結核・所見コンボ
        $result     = $db->query(knjf010Query::getTb_remark($model));
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
                            "extrahtml"   => "style=\"width:100px;\"",
                            "value"       => $RowD["TB_REMARKCD"],
                            "options"     => $opt ));
        $arg["data"]["TB_REMARKCD"] = $objForm->ge("TB_REMARKCD");

        //結核検査(X線)
        $extra = "";
        $arg["data"]["TB_X_RAY"] = knjCreateTextBox($objForm, $RowD["TB_X_RAY"], "TB_X_RAY", 40, 20, $extra);

        //結核・その他検査コンボ
        $result     = $db->query(knjf010Query::getTb_othertest($model));
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
        $arg["data"]["TB_OTHERTESTCD"] = $objForm->ge("TB_OTHERTESTCD");
        //結核・病名コンボ
        $result     = $db->query(knjf010Query::getTb_Name($model));
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
        $arg["data"]["TB_NAMECD"] = $objForm->ge("TB_NAMECD");
        //結核・指導区分コンボ
        $result     = $db->query(knjf010Query::getTb_Advise($model));
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
        $arg["data"]["TB_ADVISECD"] = $objForm->ge("TB_ADVISECD");
        //貧血・所見
        $objForm->ae( array("type"        => "text",
                            "name"        => "ANEMIA_REMARK",
                            "size"        => 20,
                            "maxlength"   => 10,
                            "extrahtml"   => "",
                            "value"       => $RowD["ANEMIA_REMARK"] ));
        $arg["data"]["ANEMIA_REMARK"] = $objForm->ge("ANEMIA_REMARK");
        //貧血・ヘモグロビン値
        $objForm->ae( array("type"        => "text",
                            "name"        => "HEMOGLOBIN",
                            "size"        => 4,
                            "maxlength"   => 4,
                            "extrahtml"   => "onblur=\"return Num_Check(this);\"",
                            "value"       => $RowD["HEMOGLOBIN"] ));
        $arg["data"]["HEMOGLOBIN"] = $objForm->ge("HEMOGLOBIN");
        //その他疾病及び異常コンボ
        $result     = $db->query(knjf010Query::getOther_disease($model));
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
        $arg["data"]["OTHERDISEASECD"] = $objForm->ge("OTHERDISEASECD");
        //学校医・所見
        $objForm->ae( array("type"        => "text",
                            "name"        => "DOC_REMARK",
                            "size"        => 20,
                            "maxlength"   => 10,
                            "extrahtml"   => "",
                            "value"       => $RowD["DOC_REMARK"] ));
        $arg["data"]["DOC_REMARK"] = $objForm->ge("DOC_REMARK");
        //学校医・所見日付
        $RowD["DOC_DATE"] = str_replace("-","/",$RowD["DOC_DATE"]);
        $arg["data"]["DOC_DATE"] = View::popUpCalendar($objForm, "DOC_DATE" ,$RowD["DOC_DATE"]);
        //事後処置コンボ
        $result     = $db->query(knjf010Query::getTreat($model));
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
        $arg["data"]["TREATCD"] = $objForm->ge("TREATCD");
        //備考
        $objForm->ae( array("type"        => "text",
                            "name"        => "REMARK",
                            "size"        => 20,
                            "maxlength"   => 10,
                            "extrahtml"   => "",
                            "value"       => $RowD["REMARK"] ));
        $arg["data"]["REMARK"] = $objForm->ge("REMARK");

        /**********************************/
        /***** 4項目目 ********************/
        /**********************************/
        //運動/指導区分
        $result     = $db->query(knjf010Query::getGuideDiv($model));
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
        $arg["data"]["GUIDE_DIV"] = $objForm->ge("GUIDE_DIV");

        //運動/部活動
        $result     = $db->query(knjf010Query::getJoiningSportsClub($model));
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
        $arg["data"]["JOINING_SPORTS_CLUB"] = $objForm->ge("JOINING_SPORTS_CLUB");


        //既往症
        $result     = $db->query(knjf010Query::getMedicalHist());
        $opt        = array();
        $opt[]      = $optnull;
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
        }
        $result->free();
        $objForm->ae( array("type"        => "select",
                            "name"        => "MEDICAL_HISTORY",
                            "size"        => "1",
                            "extrahtml"   => "style=\"width:170px;\"",
                            "value"       => $RowD["MEDICAL_HISTORY"],
                            "options"     => $opt ));
        $arg["data"]["MEDICAL_HISTORY"] = $objForm->ge("MEDICAL_HISTORY");

        Query::dbCheckIn($db);

/* ボタン作成 */
        //一括更新ボタン1
        $link = REQUESTROOT."/F/KNJF010/knjf010index.php?cmd=replace1&SCHREGNO=".$model->schregno;
        $objForm->ae( array("type"      => "button",
                           "name"       => "btn_replace",
                           "value"      => "一括更新1",
                           "extrahtml"  => "style=\"width:80px\" onclick=\"Page_jumper('{$link}');\"" ) );
        $arg["button"]["btn_replace1"] = $objForm->ge("btn_replace");
        //一括更新ボタン2
        $link = REQUESTROOT."/F/KNJF010/knjf010index.php?cmd=replace2&SCHREGNO=".$model->schregno;
        $objForm->ae( array("type"      => "button",
                           "name"       => "btn_replace",
                           "value"      => "一括更新2",
                           "extrahtml"  => "style=\"width:80px\" onclick=\"Page_jumper('{$link}');\"" ) );
        $arg["button"]["btn_replace2"] = $objForm->ge("btn_replace");
        //一括更新ボタン3
        $link = REQUESTROOT."/F/KNJF010/knjf010index.php?cmd=replace3&SCHREGNO=".$model->schregno;
        $objForm->ae( array("type"      => "button",
                           "name"       => "btn_replace",
                           "value"      => "一括更新3",
                           "extrahtml"  => "style=\"width:80px\" onclick=\"Page_jumper('{$link}');\"" ) );
        $arg["button"]["btn_replace3"] = $objForm->ge("btn_replace");
        //一括更新ボタン4
        $link = REQUESTROOT."/F/KNJF010/knjf010index.php?cmd=replace4&SCHREGNO=".$model->schregno;
        $objForm->ae( array("type"      => "button",
                           "name"       => "btn_replace",
                           "value"      => "一括更新4",
                           "extrahtml"  => "style=\"width:80px\" onclick=\"Page_jumper('{$link}');\"" ) );
        $arg["button"]["btn_replace4"] = $objForm->ge("btn_replace");
        //更新ボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_udpate",
                            "value"     => "更 新",
                            "extrahtml" => "onclick=\"return btn_submit('update');\"" ) );
        $arg["button"]["btn_update"] = $objForm->ge("btn_udpate");

        //更新後前の生徒へボタン
        $arg["button"]["btn_up_next"]    = View::updateNext($model, $objForm, 'btn_udpate');

        //削除ボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_del",
                            "value"     => "削 除",
                            "extrahtml" => "onclick=\"return btn_submit('delete');\"" ) );
        $arg["button"]["btn_del"] = $objForm->ge("btn_del");
        //取消ボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_reset",
                            "value"     => "取 消",
                            "extrahtml" => "onclick=\"return btn_submit('reset');\""  ) );
        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");
        //終了ボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_end",
                            "value"     => "終 了",
                            "extrahtml" => "onclick=\"closeWin();\"" ) );
        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SCHREGNO",
                            "value"     => $model->schregno) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "HIDDENDATE",
                            "value"     => $RowH["DATE"]) );

        if(get_count($model->warning)== 0 && $model->cmd !="reset"){
            $arg["next"] = "NextStudent(0);";
        }elseif($model->cmd =="reset"){
            $arg["next"] = "NextStudent(1);";
        }

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjf010Form1.html", $arg);
    }
}

function makeTextBox($objForm, $arg, $name, $size, $maxlength, $value, $extra="onblur=\"return Num_Check(this);\"") {
        $objForm->ae( array("type"        => "text",
                            "name"        => $name,
                            "size"        => $size,
                            "maxlength"   => $maxlength,
                            "extrahtml"   => $extra,
                            "value"       => $value ));
        $arg["data"][$name] = $objForm->ge($name);
}

?>