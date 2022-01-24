<?php

require_once('for_php7.php');
class knje020Query extends Query
{
    public function getSecurityHigh()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJE020' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    //PROGRAMPATH取得
    public function getProgrampath()
    {
        $query  = " WITH MENU AS ( ";
        $query .= "     SELECT ";
        $query .= "         MIN(MENUID) AS MENUID ";
        $query .= "     FROM ";
        $query .= "         MENU_HIGH_SECURITY_MST ";
        $query .= "     WHERE ";
        $query .= "         PROGRAMID ='KNJE020' ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     PROGRAMPATH ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST T1, ";
        $query .= "     MENU T2 ";
        $query .= " WHERE ";
        $query .= "     T1.MENUID = T2.MENUID AND ";
        $query .= "     T1.PROGRAMID ='KNJE020' ";

        return $query;
    }

    public function getRemarkValue()
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'E013' AND ";
        $query .= "     NAMECD2 = '01' ";

        return $query;
    }

    //学校名取得の取得
    public function getSchoolName($JH = "")
    {
        $query  = " SELECT ";
        $query .= "     NAME1, ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";
        if ($JH) {
            $query .= "     AND NAMESPARE2 IS NOT NULL ";
        }

        return $query;
    }

    //SCHOOLDIV を取得する(0:学年制、1:単位制)
    public function getSchoolDiv()
    {
        $query  = " SELECT ";
        $query .= "     T1.SCHOOLDIV, ";
        $query .= "     VALUE(T2.NAMESPARE2,'0') AS NEN, ";
        $query .= "     VALUE(T3.NAMESPARE3,'0') AS IS_TUSIN ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST T1 ";
        $query .= "     LEFT JOIN V_NAME_MST T2 ON T2.YEAR = T1.YEAR AND T2.NAMECD1 = 'Z001' AND ";
        $query .= "             T2.NAMECD2 = T1.SCHOOLDIV AND T2.NAMESPARE2 IN ('3','4','6') ";
        $query .= "     LEFT JOIN V_NAME_MST T3 ON T3.YEAR = T1.YEAR AND T3.NAMECD1 = 'Z001' AND ";
        $query .= "             T3.NAMECD2 = T1.SCHOOLDIV ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '". CTRL_YEAR ."' ";

        return $query;
    }

    //参照パターンコンボボックス用
    public function getPatternSeq($model)
    {
        if ($model->mode == "grd") {     //卒業
            if ($model->Properties["tyousasyo_shokenTable_Seq"] == "1") {
                $tableName = "GRD_HEXAM_EMPREMARK_SEQ_DAT";
            } else {
                $tableName = "GRD_HEXAM_EMPREMARK_DAT";
            }
        } else {
            if ($model->Properties["tyousasyo_shokenTable_Seq"] == "1") {
                $tableName = "HEXAM_EMPREMARK_SEQ_DAT";
            } else {
                $tableName = "HEXAM_EMPREMARK_DAT";
            }
        }

        $query  = " WITH PATTERN_DAT AS ( ";
        $query .= " SELECT ";
        $query .= "     PATTERN_SEQ ";
        $query .= " FROM ";
        $query .= "     {$tableName} ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     'パターン' || PATTERN_SEQ AS LABEL, ";
        $query .= "     PATTERN_SEQ AS VALUE ";
        $query .= " FROM ";
        $query .= "     PATTERN_DAT ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //調査所見データ表示
    public function getReportRemarkDat($model)
    {
        if ($model->mode == "grd") {     //卒業
            $table = "GRD_HEXAM_EMPREMARK_DAT";
        } else {
            if ($model->Properties["tyousasyo_shokenTable_Seq"] == "1") {
                $table = "HEXAM_EMPREMARK_SEQ_DAT";
            } else {
                $table = "HEXAM_EMPREMARK_DAT";
            }
        }

        $query  = " SELECT ";
        $query .= "     JOBHUNT_REC, ";
        $query .= "     JOBHUNT_ABSENCE, ";
        $query .= "     JOBHUNT_HEALTHREMARK, ";
        $query .= "     JOBHUNT_RECOMMEND, ";
        $query .= "     UPDATED ";
        $query .= " FROM ";
        $query .=       $table;
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$model->schregno."' ";
        if ($model->mode != "grd" && $model->Properties["tyousasyo_shokenTable_Seq"] == "1") {
            $query .= "     AND PATTERN_SEQ = '".$model->selectPattern."' ";
        }

        return $query;
    }

    //指導要録データ取得
    public function getHtrainremarkDat($model)
    {
        $query  = " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     TOTALREMARK ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    public function &getUpdateQuery($model)
    {
        if ($model->mode == "grd") {     //卒業
            $t = "GRD_HEXAM_EMPREMARK_DAT";
        } else {
            if ($model->Properties["tyousasyo_shokenTable_Seq"] == "1") {
                $t = "HEXAM_EMPREMARK_SEQ_DAT";
            } else {
                $t = "HEXAM_EMPREMARK_DAT";
            }
        }

        $db = Query::dbCheckOut();
        $query  = "select COUNT(*) from ".$t ." where schregno = '".$model->schregno."'";
        if ($model->mode != 'grd' && $model->Properties["tyousasyo_shokenTable_Seq"] == "1") {
            $query .= " AND PATTERN_SEQ = '".$model->selectPattern."' ";
        }
        $num = $db->getOne($query);

        if ($num > "0") {//データあり
            $model->field["JOBHUNT_HEALTHREMARK"] = (isset($model->check) ? $model->remarkValue : $model->field["JOBHUNT_HEALTHREMARK"]);

            if ($model->mode != 'grd' && $model->Properties["tyousasyo_shokenTable_Seq"] == "1") {
                $data["PATTERN_SEQ"][TEXT]            =    $model->selectPattern;
            }
            $data["JOBHUNT_REC"][TEXT]            =    $model->field["JOBHUNT_REC"];
            $data["JOBHUNT_ABSENCE"][TEXT]        =    $model->field["JOBHUNT_ABSENCE"];
            $data["JOBHUNT_HEALTHREMARK"][TEXT]   =    $model->field["JOBHUNT_HEALTHREMARK"];
            $data["JOBHUNT_RECOMMEND"][TEXT]      =    $model->field["JOBHUNT_RECOMMEND"];
            $data["UPDATED"][FUNC]                =    "sysdate()";

            $where  = "WHERE SCHREGNO ='".$model->schregno."'";
            if ($model->mode != "grd" && $model->Properties["tyousasyo_shokenTable_Seq"] == "1") {
                $where .= " AND PATTERN_SEQ ='".$model->selectPattern."'";
            }
            $query = Query::updateSQL($data, $t, $where);
        } else {//データ無し
            $model->field["JOBHUNT_HEALTHREMARK"] = (isset($model->check) ? $model->remarkValue : $model->field["JOBHUNT_HEALTHREMARK"]);

            $data["SCHREGNO"][TEXT]               =    $model->schregno;
            if ($model->mode != "grd" && $model->Properties["tyousasyo_shokenTable_Seq"] == "1") {
                $data["PATTERN_SEQ"][TEXT]            =    $model->selectPattern;
            }
            $data["JOBHUNT_REC"][TEXT]            =    $model->field["JOBHUNT_REC"];
            $data["JOBHUNT_ABSENCE"][TEXT]        =    $model->field["JOBHUNT_ABSENCE"];
            $data["JOBHUNT_HEALTHREMARK"][TEXT]   =    $model->field["JOBHUNT_HEALTHREMARK"];
            $data["JOBHUNT_RECOMMEND"][TEXT]      =    $model->field["JOBHUNT_RECOMMEND"];
            $data["UPDATED"][FUNC]                =    "sysdate()";

            $query = Query::insertSQL($data, $t);
        }
        $db->query($query);
        Query::dbCheckIn($db);
    }

    //参照パターンを対象パターンにコピー
    public function getCopyPatternQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        if ($model->mode == "grd") {     //卒業
            $tableName  = "GRD_HEXAM_EMPREMARK_DAT";
        } else {
            if ($model->Properties["tyousasyo_shokenTable_Seq"] == "1") {
                $tableName  = "HEXAM_EMPREMARK_SEQ_DAT";
            } else {
                $tableName  = "HEXAM_EMPREMARK_DAT";
            }
        }

        $query  = " DELETE FROM ";
        $query .= "     {$tableName} ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";
        if ($model->cmd != 'grd' && $model->Properties["tyousasyo_shokenTable_Seq"] == "1") {
            $query .= "     AND PATTERN_SEQ = '{$model->selectPattern}' ";
        }

        $db->query($query);

        $query  = " INSERT INTO {$tableName} ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO, ";
        if ($model->cmd != 'grd' && $model->Properties["tyousasyo_shokenTable_Seq"] == "1") {
            $query .= "     '{$model->selectPattern}', ";
        }
        $query .= "     JOBHUNT_REC, ";
        $query .= "     JOBHUNT_RECOMMEND, ";
        $query .= "     JOBHUNT_ABSENCE, ";
        $query .= "     JOBHUNT_HEALTHREMARK, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     SYSDATE() ";
        $query .= " FROM ";
        $query .= "     {$tableName} ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND PATTERN_SEQ = '{$model->referPattern}' ";

        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);

        return;
    }

    //健康診断詳細データ取得
    public function getMedexamDetDat($model)
    {
        $year = ($model->mode == "grd") ? $model->grd_year : CTRL_YEAR;

        $query  = " SELECT ";
        $query .= "     T1.HEIGHT, ";
        $query .= "     T1.WEIGHT, ";
        $query .= "     M1.NAME1 R_BAREVISION_MARK_NAME, ";
        $query .= "     M2.NAME1 L_BAREVISION_MARK_NAME, ";
        $query .= "     M3.NAME1 R_VISION_MARK_NAME, ";
        $query .= "     M4.NAME1 L_VISION_MARK_NAME, ";
        $query .= "     T1.R_EAR, ";
        $query .= "     T1.L_EAR, ";
        $query .= "     T2.DATE, ";
        $query .= "     MEISYOU_GET(T1.R_EAR,'F010',1) AS R_EAR_NAME, ";
        $query .= "     MEISYOU_GET(T1.L_EAR,'F010',1) AS L_EAR_NAME ";
        $query .= " FROM ";
        if ($model->mode == "grd") {    //卒業
            $query .= "     GRD_MEDEXAM_HDAT T2, ";
            $query .= "     GRD_MEDEXAM_DET_DAT T1 ";
        } else {
            $query .= "     MEDEXAM_HDAT T2, ";
            $query .= "     MEDEXAM_DET_DAT T1 ";
        }
        $query .= " LEFT JOIN NAME_MST M1 ON M1.NAMECD1 = 'F017' AND M1.NAMECD2 = T1.R_BAREVISION_MARK ";
        $query .= " LEFT JOIN NAME_MST M2 ON M2.NAMECD1 = 'F017' AND M2.NAMECD2 = T1.L_BAREVISION_MARK ";
        $query .= " LEFT JOIN NAME_MST M3 ON M3.NAMECD1 = 'F017' AND M3.NAMECD2 = T1.R_VISION_MARK ";
        $query .= " LEFT JOIN NAME_MST M4 ON M4.NAMECD1 = 'F017' AND M4.NAMECD2 = T1.L_VISION_MARK ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.SCHREGNO = '" .$model->schregno. "' AND ";
        $query .= "     T1.YEAR     = '".$year."' ";

        return $query;
    }

    //欠席日数表示
    public function getAttendrec($schregno)
    {
        if ($model->mode == "grd") {     //卒業
            $year = $model->grd_year;
        } else {
            $year = CTRL_YEAR;
        }

        $query  = "select coalesce(sick,0)+coalesce(accidentnotice,0)+coalesce(noaccidentnotice,0) as days,year,schoolcd ";
        $query .= " from schreg_attendrec_dat ";
        $query .= " where schregno = '" .$schregno. "'";

        return $query;
    }


    //学年（年次）コンボボックス用(Subform1)
    public function selectQueryAnnual($model, $JH = "")
    {
        if ($model->mode == "grd") {     //卒業
            $year = $model->grd_year;
            $semester = $model->grd_semester;
            if ($model->control["学校区分"] == "0") {     //学年制
                $t = "V_GRD_REGDYEAR_GRADE_DAT";
            } elseif ($model->control["学校区分"] == "1") {     //単位制
                $t = "V_GRD_REGDYEAR_UNIT_DAT";
            }
        } else {
            $year = CTRL_YEAR;
            $semester = CTRL_SEMESTER;
            if ($model->control["学校区分"] == "0") {     //学年制
                $t = "V_REGDYEAR_GRADE_DAT";
            } elseif ($model->control["学校区分"] == "1") {     //単位制
                $t = "V_REGDYEAR_UNIT_DAT";
            }
        }
        $query = " SELECT DISTINCT ";
        $query .= "   T1.YEAR, ";
        $query .= "   T1.ANNUAL ";
        $query .= " FROM ";
        $query .= "   ".$t." T1 ";
        $query .= " WHERE ";
        $query .= "   T1.SCHREGNO = '" .$model->schregno ."'";
        if ($JH) {
            $query .= "     AND T1.ANNUAL >= '04' ";
        }

        return $query;
    }

    //成績参照画面(Subform1)
    public function selectQuerySubForm1($model, $JH = "")
    {
        $query = " SELECT ";
        $query .= "   T1.ANNUAL, ";
#        $query .= "   COALESCE(T1.CLASSNAME,T3.CLASSNAME)       CLASSNAME, ";
#        $query .= "   COALESCE(T1.SUBCLASSNAME,T4.SUBCLASSNAME) SUBCLASSNAME, ";
        //2004-06-03 y.arakaki 略称表示へ変更
        //$query .= "   CASE WHEN T1.CLASSNAME IS NULL OR T1.CLASSNAME = '' THEN T3.CLASSNAME ELSE T1.CLASSNAME END AS CLASSNAME, ";
        $query .= "   CASE WHEN T1.CLASSABBV IS NULL OR T1.CLASSABBV = '' THEN T3.CLASSABBV ELSE T1.CLASSABBV END AS CLASSNAME, ";
        $query .= "   CASE WHEN T1.SUBCLASSNAME IS NULL OR T1.SUBCLASSNAME = '' THEN T4.SUBCLASSNAME ELSE T1.SUBCLASSNAME END AS SUBCLASSNAME, ";
        $query .= "   T1.VALUATION, ";
        $query .= "   T1.GET_CREDIT, ";
        $query .= "   T1.ADD_CREDIT, ";
        $query .= "   T1.CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "   T1.SUBCLASSCD, ";
        $query .= "   T2.REMARK ";
        $query .= " FROM ";
        if ($model->mode == "grd") {     //卒業
            $query .= "   GRD_STUDYREC_DAT T1 ";
        } else {
            $query .= "   SCHREG_STUDYREC_DAT T1 ";
        }
        $query .= "   LEFT OUTER JOIN STUDYRECREMARK_DAT T2 ON  ";
        $query .= "                   T1.YEAR       = T2.YEAR AND  ";
        $query .= "                   T1.SCHREGNO   = T2.SCHREGNO AND  ";
        $query .= "                   T1.CLASSCD    = T2.CLASSCD AND  ";
        $query .= "                   T1.SUBCLASSCD = T2.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "               AND T1.SCHOOL_KIND    = T2.SCHOOL_KIND ";
            $query .= "               AND T1.CURRICULUM_CD  = T2.CURRICULUM_CD";
        }
        $query .= "   LEFT OUTER JOIN CLASS_MST    T3 ON  ";
        $query .= "                   T1.CLASSCD    = T3.CLASSCD  ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "               AND T1.SCHOOL_KIND    = T3.SCHOOL_KIND ";
        }
        $query .= "   LEFT OUTER JOIN SUBCLASS_MST T4 ON  ";
        $query .= "                   T1.SUBCLASSCD = T4.SUBCLASSCD  ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "               AND T1.CLASSCD        = T4.CLASSCD ";
            $query .= "               AND T1.SCHOOL_KIND    = T4.SCHOOL_KIND ";
            $query .= "               AND T1.CURRICULUM_CD  = T4.CURRICULUM_CD";
        }
        $query .= " WHERE ";
        $query .= "   T1.SCHOOLCD = '0' AND ";
        if ($model->annual["YEAR"] != "0" || $model->annual["ANNUAL"] != "0") {
            $query .= "   T1.YEAR = '" .$model->annual["YEAR"] ."' AND ";
            $query .= "   T1.ANNUAL = '" .$model->annual["ANNUAL"]."' AND";
        }
        if ($JH) {
            $query .= "   T1.ANNUAL >= '04' AND ";
        }
        $query .= "   T1.SCHREGNO = '" .$model->schregno ."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.ANNUAL ";

        return $query;

#        $query = " SELECT ";
#        $query .= "   T1.ANNUAL, ";
#        $query .= "   T1.CLASSNAME, ";
#        $query .= "   T1.SUBCLASSNAME, ";
#        $query .= "   T1.VALUATION, ";
#        $query .= "   T1.GET_CREDIT, ";
#        $query .= "   T1.ADD_CREDIT ";
#        $query .= " FROM ";
#        $query .= "   SCHREG_STUDYREC_DAT T1 LEFT OUTER JOIN STUDYRECREMARK_DAT T2 ON  ";
#        $query .= "   T1.YEAR = T2.YEAR AND  ";
#        $query .= "   T1.SCHREGNO = T2.SCHREGNO AND  ";
#        $query .= "   T1.CLASSCD = T2.CLASSCD AND  ";
#        $query .= "   T1.SUBCLASSCD = T2.SUBCLASSCD ";
#        $query .= " WHERE ";
#        $query .= "   T1.SCHOOLCD = '0' AND ";
#        if ($model->annual["YEAR"] != "0" || $model->annual["ANNUAL"] != "0"){
#            $query .= "   T1.YEAR = '" .$model->annual["YEAR"] ."' AND ";
#            $query .= "   T1.ANNUAL = '" .$model->annual["ANNUAL"]."' AND";
#        }
#        $query .= "   T1.SCHREGNO = '" .$model->schregno ."' ";
#
#        return $query;
    }

    //出欠備考 -- 通知票取込
    public function getHreportremarkDetailDat($db, $model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER    = '9' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "     DIV         = '06' AND ";
        $query .= "     CODE IN ('01','02','03','04','05') AND ";
        $query .= "     REMARK2     = '1' ";
        $query .= " ORDER BY ";
        $query .= "     CODE ";

        $result = $db->query($query);
        $set_remark = "";
        $sep = "";
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if (strlen($row["REMARK1"]) > 0) {
                $set_remark .= $sep.$row["REMARK1"];
                $sep = "、";
            }
        }

        return $set_remark;
    }

    //臘八摂心皆勤
    public function getRouhatsuKaikin($model)
    {
        $query  = " WITH SCHREG AS ( ";
        $query .= "   SELECT DISTINCT ";
        $query .= "     T1.YEAR ";
        $query .= "     , T2.SCHOOL_KIND ";
        $query .= "   FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT T2 ";
        $query .= "       ON T1.YEAR = T2.YEAR ";
        $query .= "       AND T1.GRADE = T2.GRADE ";
        $query .= "   WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
        $query .= " ) ";
        $query .= " , SCHREGYEAR AS ( ";
        $query .= "   SELECT YEAR ";
        $query .= "   FROM SCHREG ";
        $query .= "   WHERE SCHOOL_KIND IN ( ";
        $query .= "       SELECT SCHOOL_KIND ";
        $query .= "       FROM SCHREG ";
        $query .= "       WHERE YEAR = '".CTRL_YEAR."' ";
        $query .= "     ) ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "   COUNT(*) REC_CNT ";
        $query .= "   , SUM(CASE WHEN KAIKIN_FLG = '1' THEN 1 ELSE 0 END) KAIKIN_CNT ";
        $query .= " FROM SCHREGYEAR ";
        $query .= " LEFT JOIN ROU_HATSU_SESSIN_KAI_DAT ROUHATSU ";
        $query .= "    ON SCHREGYEAR.YEAR = ROUHATSU.YEAR ";
        $query .= "   AND ROUHATSU.SCHREGNO = '".$model->schregno."' ";
        $query .= " WHERE SCHREGYEAR.YEAR <= '".CTRL_YEAR."' ";

        return $query;
    }
}
