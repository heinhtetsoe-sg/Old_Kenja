<?php

require_once('for_php7.php');

class knja120aQuery extends Query {

    function getSecurityHigh() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJA120A' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    //近大判定用
    function getKindaiJudgment($model)
    {
        $db = Query::dbCheckOut();
        $setyear = "";
        $setyear = $model->exp_year == "" ? CTRL_YEAR : $model->exp_year;
        $query = "SELECT COUNT(*) FROM SCHOOL_MST WHERE YEAR = '".$setyear."' AND SCHOOLNAME1 LIKE '%近畿大学%' ";
        $cnt = $db->getOne($query);
        Query::dbCheckIn($db);

        return $cnt;
    }

    //卒業可能学年か判定
    function getGraduationGrade($model) {
        //DB接続
        $db = Query::dbCheckOut();
        
        $query  = " SELECT ";
        $query .= "     L1.SCHOOL_KIND, ";
        $query .= "     CASE WHEN T1.GRADE BETWEEN L2.NAMESPARE2 AND L2.NAMESPARE3 THEN 1 ELSE 0 END AS FLG ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT L1 ";
        $query .= "          ON L1.YEAR     = T1.YEAR ";
        $query .= "         AND L1.GRADE    = T1.GRADE ";
        $query .= "     LEFT JOIN NAME_MST L2 ";
        $query .= "          ON L2.NAMECD1  = 'A023' ";
        $query .= "         AND L2.NAME1    = L1.SCHOOL_KIND ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '{$model->exp_year}' ";
        $query .= " AND T1.SCHREGNO = '{$model->schregno}' ";
        $query .= " AND T1.SEMESTER = '{$model->exp_semester}' ";

        $ret = $db->getRow($query, DB_FETCHMODE_ASSOC);
        
        Query::dbCheckIn($db);

        return $ret;
    }

    //学年取得
    function getGradeHrclass($model) {
        //DB接続
        $db = Query::dbCheckOut();
        
        $query  = " SELECT ";
        $query .= "     GRADE || '-' || HR_CLASS AS VALUE";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->exp_year}' ";
        $query .= " AND T1.SCHREGNO = '{$model->schregno}' ";
        $query .= " AND T1.SEMESTER = '{$model->exp_semester}' ";
        
        $gradehrclass = $db->getOne($query);
        
        Query::dbCheckIn($db);

        return $gradehrclass;
    }

    //総合所見の使用不可(2020年度以降は使用しない)
    function getTotalRemarkDisable($model) {
        $query  = " WITH GRADE_2018 AS ( ";
        $query .= "     SELECT ";
        $query .= "         * ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_GDAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '2018' ";
        $query .= "         AND SCHOOL_KIND = 'H' ";
        $query .= "         FETCH FIRST 1 ROWS ONLY ";
        $query .= " ) ";
        $query .= " , GRADE_2019 AS ( ";
        $query .= "     SELECT ";
        $query .= "         * ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_GDAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '2019' ";
        $query .= "         AND SCHOOL_KIND = 'H' ";
        $query .= "         FETCH FIRST 2 ROWS ONLY ";
        $query .= " ) ";
        $query .= " SELECT * FROM GRADE_2018 ";
        $query .= " UNION ";
        $query .= " SELECT * FROM GRADE_2019 ";
        $query .= " ORDER BY ";
        $query .= "     YEAR ";
        $query .= "     , GRADE ";

        return $query;
    }

    //調査書より読込み
    function getHexamEntremark($model)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     T1.ATTENDREC_REMARK, ";
        $query .= "     T1.SPECIALACTREC, ";
        $query .= "     T1.TRAIN_REF, ";
        $query .= "     T1.TRAIN_REF1, ";
        $query .= "     T1.TRAIN_REF2, ";
        $query .= "     T1.TRAIN_REF3, ";
        //年度用
        $query .= "     T1.TOTALSTUDYACT AS TOTALSTUDYACT_YEAR, ";
        $query .= "     T1.TOTALSTUDYVAL AS TOTALSTUDYVAL_YEAR, ";
        //通年用
        $query .= "     L1.TOTALSTUDYACT, ";
        $query .= "     L1.TOTALSTUDYVAL ";
        $query .= " FROM ";
        $query .= "     HEXAM_ENTREMARK_DAT T1 ";
        $query .= "     LEFT JOIN HEXAM_ENTREMARK_HDAT L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "       T1.YEAR      = '{$model->exp_year}' ";
        $query .= "   AND T1.SCHREGNO  = '{$model->schregno}' ";

        $row = $db->getRow($query,DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //１レコード取得
    function getTrainRow($schregno, $exp_year, $model)
    {
        $db = Query::dbCheckOut();

        if ($model->Properties["seitoSidoYoroku_Totalstudyact2_val2_UseTextFlg"]) {
            if ($model->Properties["seitoSidoYorokuHoushiNentani"] == '2') {
                $query  = " WITH SCHREGNO AS ( ";
                $query .= "     SELECT SCHREGNO FROM HTRAINREMARK_DAT T1 ";
                $query .= "     WHERE ";
                $query .= "         T1.YEAR     = '".$exp_year."' AND ";
                $query .= "         T1.SCHREGNO = '".$schregno."' ";
                $query .= "     UNION ";
                $query .= "     SELECT SCHREGNO FROM HTRAINREMARK_HDAT T1 ";
                $query .= "     WHERE ";
                $query .= "         T1.SCHREGNO = '".$schregno."' ";
                $query .= " ) ";
                $query .= " SELECT ";
                $query .= "     L1.*, ";
                $query .= "     L2.TOTALSTUDYACT2 AS REMARK1_003, ";
                $query .= "     L2.TOTALSTUDYVAL2 AS REMARK2_003 ";
                $query .= " FROM ";
                $query .= "     SCHREGNO T0 ";
                $query .= "     LEFT JOIN HTRAINREMARK_DAT L1 ON L1.SCHREGNO = T0.SCHREGNO AND ";
                $query .= "         L1.YEAR     = '".$exp_year."' ";
                $query .= "     LEFT JOIN HTRAINREMARK_HDAT L2 ON L2.SCHREGNO = T0.SCHREGNO ";
            } else {
                $query  = " SELECT ";
                $query .= "     T1.*, ";
                $query .= "     L1.REMARK1 AS REMARK1_003, ";
                $query .= "     L1.REMARK2 AS REMARK2_003, ";
                $query .= "     L2.REMARK1 AS REMARK1_005 ";
                $query .= " FROM ";
                $query .= "     HTRAINREMARK_DAT T1 ";
                $query .= "     LEFT JOIN HTRAINREMARK_DETAIL2_DAT L1 ";
                $query .= "              ON T1.YEAR         = L1.YEAR ";
                $query .= "             AND T1.SCHREGNO     = L1.SCHREGNO ";
                $query .= "             AND L1.HTRAIN_SEQ   = '003' ";
                $query .= "     LEFT JOIN HTRAINREMARK_DETAIL2_DAT L2 ";
                $query .= "              ON T1.YEAR         = L2.YEAR ";
                $query .= "             AND T1.SCHREGNO     = L2.SCHREGNO ";
                $query .= "             AND L2.HTRAIN_SEQ   = '005' ";
                $query .= " WHERE ";
                $query .= "     T1.YEAR     = '".$exp_year."' AND ";
                $query .= "     T1.SCHREGNO = '".$schregno."' ";
            }
        } else {
            $query  = " SELECT T1.*, ";
            $query .= "        L2.REMARK1 AS REMARK1_005 ";
            $query .= "   FROM htrainremark_dat T1 ";
            $query .= "     LEFT JOIN HTRAINREMARK_DETAIL2_DAT L2 ";
            $query .= "              ON T1.YEAR         = L2.YEAR ";
            $query .= "             AND T1.SCHREGNO     = L2.SCHREGNO ";
            $query .= "             AND L2.HTRAIN_SEQ   = '005' ";
            $query .= "  WHERE T1.year     = '".$exp_year."'";
            $query .= "    AND T1.schregno = '".$schregno."'";
        }

        $row = $db->getRow($query,DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //通知票より読込
    function get_record_totalstudytime_dat($model) {
    
        $setyear = "";
        $setyear = $model->exp_year == "" ? CTRL_YEAR : $model->exp_year;
        $result = array();
        $targetSemester = "";
        if (preg_match("/1-([0-9]+)/", $model->Properties["tutisyoSougoushokengakki"], $result)) {
            $targetSemester = " (";
            $comma = "";
            for ($i = 0; $i < strlen($result[1]); $i++) {
                $targetSemester .= $comma."'".substr($result[1], $i, 1)."'";
                $comma = ", ";
            }
            $targetSemester .= ") ";
        } else if ($model->Properties["tutisyoSougoushokengakki"] == '1') {
            $targetSemester = "('9')";
        }
        
        $query  = " SELECT ";
        $query .= "     TOTALSTUDYTIME, ";
        $query .= "     TOTALSTUDYACT ";
        $query .= " FROM ";
        $query .= "     RECORD_TOTALSTUDYTIME_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";
        $query .= " AND YEAR =  '{$setyear}' ";
        if ($targetSemester) {
            $query .= " AND SEMESTER IN ". $targetSemester;
        }
        $query .= " ORDER BY ";
        $query .= "     YEAR, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD, ";
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CURRICULUM_CD, ";
        }
        $query .= "     SUBCLASSCD, ";
        $query .= "     SEMESTER ";

        return $query;
    }

    //INSERT
    function &getInsertQuery($fields,$schregno, $exp_year, $exp_semester)
    {
        $db = Query::dbCheckOut();

        //年次の取得
        $query  = "SELECT annual FROM schreg_regd_dat ";
        $query .= " WHERE schregno = '".$schregno."'";
        $query .= "   AND year = '".$exp_year."'";
        $query .= "   AND semester = '".$exp_semester."'";

        $data["YEAR"][TEXT]                 = $exp_year;
        $data["SCHREGNO"][TEXT]             = $schregno;
        $data["ANNUAL"][TEXT]               = $db->getOne($query);
        $data["TOTALSTUDYACT"][TEXT]        = $fields["TOTALSTUDYACT"];
        $data["TOTALSTUDYVAL"][TEXT]        = $fields["TOTALSTUDYVAL"];
        $data["SPECIALACTREMARK"][TEXT]     = $fields["SPECIALACTREMARK"];
        $data["TOTALREMARK"][TEXT]          = $fields["TOTALREMARK"];
        $data["ATTENDREC_REMARK"][TEXT]     = $fields["ATTENDREC_REMARK"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]            = "sysdate()";

        $db->query(Query::insertSQL($data, "htrainremark_dat"));

        Query::dbCheckIn($db);
        return ;
    }

    //DELETE
    function &getDeleteQuery($schregno, $exp_year)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM htrainremark_dat ";
        $query .= " WHERE YEAR = '".$exp_year."'";
        $query .= "   AND SCHREGNO = '".$schregno."'";

        $db->query($query);

        Query::dbCheckIn($db);
        return $result;
    }

    //INSERT
    function &getInsertQueryDetail($fields, $schregno, $exp_year, $model) {
        $db = Query::dbCheckOut();

        $db->autoCommit(false);

        if ($model->Properties["seitoSidoYorokuHoushiNentani"] == '2') {
            //削除
            $query  = " SELECT COUNT(*) FROM ";
            $query .= "     HTRAINREMARK_HDAT ";
            $query .= " WHERE ";
            $query .= "     SCHREGNO    = '".$schregno."' ";

            $count = $db->getOne($query);
            if ($count == 0) {
                //追加
                $data = array();
                $data["SCHREGNO"][TEXT]         = $schregno;
                $data["TOTALSTUDYACT2"][TEXT]   = $fields["REMARK1_003"];
                $data["TOTALSTUDYVAL2"][TEXT]   = $fields["REMARK2_003"];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "sysdate()";

                $db->query(Query::insertSQL($data, "HTRAINREMARK_HDAT"));
                $db->commit();
            } else {
                $query  = " UPDATE HTRAINREMARK_HDAT ";
                $query .= " SET (TOTALSTUDYACT2, TOTALSTUDYVAL2) = ('".$fields["REMARK1_003"]."', '".$fields["REMARK2_003"]."') ";
                $query .= " WHERE SCHREGNO = '".$schregno."' ";

                $db->query($query);
                $db->commit();
            }
        } else {
            //削除
            $query  = " DELETE FROM ";
            $query .= "     HTRAINREMARK_DETAIL2_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR        = '".$exp_year."' AND ";
            $query .= "     SCHREGNO    = '".$schregno."' AND ";
            $query .= "     HTRAIN_SEQ  = '003' ";

            $db->query($query);

            //追加
            $data = array();
            $data["YEAR"][TEXT]             = $exp_year;
            $data["SCHREGNO"][TEXT]         = $schregno;
            $data["HTRAIN_SEQ"][TEXT]       = '003';
            $data["REMARK1"][TEXT]          = $fields["REMARK1_003"];
            $data["REMARK2"][TEXT]          = $fields["REMARK2_003"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $db->query(Query::insertSQL($data, "HTRAINREMARK_DETAIL2_DAT"));
            $db->commit();
        }

        Query::dbCheckIn($db);

        return ;
    }

    //INSERT
    function &getInsertQueryDetail2($fields, $schregno, $exp_year, $model) {
        $db = Query::dbCheckOut();

        //削除
        $query  = " DELETE FROM ";
        $query .= "     HTRAINREMARK_DETAIL2_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$exp_year."' AND ";
        $query .= "     SCHREGNO    = '".$schregno."' AND ";
        $query .= "     HTRAIN_SEQ  = '005' ";

        $db->query($query);

        //追加
        $data = array();
        $data["YEAR"][TEXT]             = $exp_year;
        $data["SCHREGNO"][TEXT]         = $schregno;
        $data["HTRAIN_SEQ"][TEXT]       = '005';
        $data["REMARK1"][TEXT]          = $fields["REMARK1_005"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $db->query(Query::insertSQL($data, "HTRAINREMARK_DETAIL2_DAT"));
        $db->commit();
        Query::dbCheckIn($db);

        return ;
    }

    //通知表所見より年度を取得
    function selectQueryYear($model)
    {
        $query  = "SELECT distinct t1.year ";
        $query .= "  FROM hreportremark_dat t1  ";
        $query .= " WHERE t1.schregno = '" .$model->schregno ."'  ";
        $query .= " ORDER BY t1.year desc ";

        return $query;
    }

    //通知表所見より再読込
    function selectQueryGuide($model)
    {
        $query  = " SELECT t1.totalstudytime,t1.specialactremark,t1.communication, t1.remark1, t1.remark2, t1.semester ";
        $query .= "   FROM hreportremark_dat t1 ";
        $query .= "  WHERE t1.schregno = '" .$model->schregno ."' ";
        $query .= "    and t1.year     = '" .$model->year_cmb ."' ";
        $query .= "  ORDER BY t1.semester";

        return $query;
    }

    //セット日々出欠備考
    function getSemesRemark($model) {
    
        //DB接続
        $db = Query::dbCheckOut();

        $query  = "  SELECT ";
        $query .= "      REMARK1, ";
        $query .= "      CASE WHEN MONTH = '01' THEN '13' WHEN MONTH = '02' THEN '14' WHEN MONTH = '03' THEN '15' ELSE MONTH END AS SORT_MONTH ";
        $query .= "  FROM ";
        $query .= "      ATTEND_SEMES_REMARK_DAT ";
        $query .= "  WHERE ";
        $query .= "      COPYCD = '0' ";
        $query .= "  AND YEAR = '".CTRL_YEAR."' ";
        $query .= "  AND SCHREGNO = '{$model->schregno}' ";
        $query .= "  ORDER BY ";
        $query .= "      YEAR, ";
        $query .= "      SEMESTER, ";
        $query .= "      SORT_MONTH ";
        
        $result = $db->query($query);
        $set_remark = "";
        $count = 0;
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($count == 0) {
                $set_remark .= $row["REMARK1"];
            } else {
                if ($row["REMARK1"] != "") {
                    $set_remark .= "／".$row["REMARK1"];
                }
            }
            $count++;
        }        
        Query::dbCheckIn($db);
        
        return $set_remark;
    }

    //出欠備考 -- 通知票取込
    function getHreportremarkDetailDat($model) {
        //DB接続
        $db = Query::dbCheckOut();

        $year = ($model->exp_year == "") ? CTRL_YEAR : $model->exp_year;

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$year."' AND ";
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
        Query::dbCheckIn($db);

        return $set_remark;
    }

    //学年（年次）コンボボックス用(Subform4)
    function selectQueryAnnual($model, $JH="")
    {
        if ($model->control["学校区分"] == "0"){     //学年制
            $table = "V_REGDYEAR_GRADE_DAT";
        }else if ($model->control["学校区分"] == "1"){     //単位制
            $table = "V_REGDYEAR_UNIT_DAT";
        }
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.ANNUAL ";
        $query .= " FROM ";
        $query .= "     ".$table." T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '" .$model->schregno ."'";
        if($JH){
            $query .= "     AND T1.ANNUAL >= '04' ";
        }

        return $query;
    }

    //成績参照画面(Subform4)
    function selectQuerySubForm4($model, $JH="") {
        $query = " SELECT ";
        $query .= "   T1.ANNUAL, ";
        $query .= "   CASE WHEN T1.CLASSABBV IS NULL OR T1.CLASSABBV = '' THEN T3.CLASSABBV ELSE T1.CLASSABBV END AS CLASSNAME, ";
        $query .= "   CASE WHEN T1.SUBCLASSNAME IS NULL OR T1.SUBCLASSNAME = '' THEN T4.SUBCLASSNAME ELSE T1.SUBCLASSNAME END AS SUBCLASSNAME, ";
        $query .= "   T1.VALUATION, ";
        $query .= "   T1.GET_CREDIT, ";
        $query .= "   T1.ADD_CREDIT, ";
        $query .= "   T1.CLASSCD, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "   T1.SCHOOL_KIND, ";
            $query .= "   T1.CURRICULUM_CD, ";
        }
        $query .= "   T1.SUBCLASSCD, ";
        $query .= "   T2.REMARK ";
        $query .= " FROM ";
        $query .= "   SCHREG_STUDYREC_DAT T1 ";
        $query .= "   LEFT OUTER JOIN STUDYRECREMARK_DAT T2 ON  ";
        $query .= "                   T1.YEAR       = T2.YEAR AND  ";
        $query .= "                   T1.SCHREGNO   = T2.SCHREGNO AND  ";
        $query .= "                   T1.CLASSCD    = T2.CLASSCD AND  ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                   T1.SCHOOL_KIND    = T2.SCHOOL_KIND AND  ";
            $query .= "                   T1.CURRICULUM_CD  = T2.CURRICULUM_CD AND  ";
        }
        $query .= "                   T1.SUBCLASSCD = T2.SUBCLASSCD ";
        $query .= "   LEFT OUTER JOIN CLASS_MST    T3 ON  ";
        $query .= "                   T1.CLASSCD    = T3.CLASSCD  ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "               AND T1.SCHOOL_KIND    = T3.SCHOOL_KIND  ";
        }
        $query .= "   LEFT OUTER JOIN SUBCLASS_MST T4 ON  ";
        $query .= "                   T1.SUBCLASSCD = T4.SUBCLASSCD  ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "               AND T1.CLASSCD        = T4.CLASSCD  ";
            $query .= "               AND T1.SCHOOL_KIND    = T4.SCHOOL_KIND  ";
            $query .= "               AND T1.CURRICULUM_CD  = T4.CURRICULUM_CD  ";
        }
        $query .= " WHERE ";
        $query .= "   T1.SCHOOLCD = '0' AND ";
        if ($model->annual["YEAR"] != "0" || $model->annual["ANNUAL"] != "0"){
            $query .= "   T1.YEAR = '" .$model->annual["YEAR"] ."' AND ";
            $query .= "   T1.ANNUAL = '" .$model->annual["ANNUAL"]."' AND";
        }
        if($JH){
            $query .= "   T1.ANNUAL >= '04' AND ";
        }
        $query .= "   T1.SCHREGNO = '" .$model->schregno ."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.CLASSCD, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.ANNUAL ";

        return $query;
    }

    //中高一貫校
    function getJH() {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' AND ";
        $query .= "     NAMESPARE2 IS NOT NULL ";

        return $query;
    }

    //明治判定
    function getMeijiHantei() {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' AND ";
        $query .= "     NAME1   = 'meiji' ";

        $meiji = $db->getOne($query);
        Query::dbCheckIn($db);
        return $meiji;
    }

    //札幌開成判定
    function getSapporoHantei() {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' AND ";
        $query .= "     NAME1   = 'sapporo' ";

        $sapporo = $db->getOne($query);
        Query::dbCheckIn($db);
        return $sapporo;
    }

    //京都西山判定
    function getSchoolHantei() {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";

        $schoolName = $db->getOne($query);
        Query::dbCheckIn($db);
        return $schoolName;
    }

    //奈良判定
    function getNaraHantei() {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' AND ";
        $query .= "     NAME1   = 'naraken' ";

        $nara = $db->getOne($query);
        Query::dbCheckIn($db);
        return $nara;
    }

    //項目名取得
    function getItemName($model) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'A041' AND ";
        $query .= "     NAMECD2 = '01' ";

        $itemName = $db->getOne($query);
        Query::dbCheckIn($db);

        return $itemName;
    }

    //学校マスタの校種有無チェック
    function checkSchoolMst() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSCOLUMNS ";
        $query .= " WHERE ";
        $query .= "         TBNAME = 'SCHOOL_MST' ";
        $query .= "     AND NAME   = 'SCHOOL_KIND' ";

        return $query;
    }

    //校種取得
    function getSchoolKind($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T2.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_REGD_GDAT T2 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = T2.YEAR ";
        $query .= "     AND T1.YEAR     = '".$model->exp_year."' ";
        $query .= "     AND T1.GRADE    = T2.GRADE ";
        $query .= "     AND T1.SCHREGNO = '".$model->schregno."' ";
        $query .= "     AND T1.SEMESTER IN (SELECT ";
        $query .= "                         MAX(SEMESTER) ";
        $query .= "                     FROM ";
        $query .= "                         SCHREG_REGD_DAT S1 ";
        $query .= "                     WHERE ";
        $query .= "                             T1.YEAR     = S1.YEAR ";
        $query .= "                         AND T1.SCHREGNO = S1.SCHREGNO ";
        $query .= "                     ) ";

        return $query;
    }

    //出欠の記録参照
    function getAttendSemesDat($model, $knjSchoolMst) {
        $query  = " WITH ATTEND_SEM AS ( ";
        $query .= "     SELECT ";
        $query .= "         SEMESTER, ";
        $query .= "         SCHREGNO, ";
        if ($knjSchoolMst["SEM_OFFDAYS"] == "1") {
            $query .= "         SUM(VALUE(LESSON,0) - VALUE(ABROAD,0))  AS LESSON, ";
            $query .= "         SUM(VALUE(SICK,0) + VALUE(NOTICE,0) + VALUE(NONOTICE,0) + VALUE(OFFDAYS,0))  AS SICK, ";
        } else {
            $query .= "         SUM(VALUE(LESSON,0) - VALUE(ABROAD,0) - VALUE(OFFDAYS,0))  AS LESSON, ";
            $query .= "         SUM(VALUE(SICK,0) + VALUE(NOTICE,0) + VALUE(NONOTICE,0))  AS SICK, ";
        }
        $query .= "         SUM(VALUE(SUSPEND,0) ";
        if ($model->Properties["useVirus"] == "true") {
            $query .= "             + VALUE(VIRUS,0) ";
        }
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "             + VALUE(KOUDOME,0) ";
        }
        $query .= "             ) AS SUSPEND, ";
        $query .= "         SUM(VALUE(MOURNING,0)) AS MOURNING, ";
        $query .= "         SUM(VALUE(LATE,0)) AS LATE, ";
        $query .= "         SUM(VALUE(EARLY,0)) AS EARLY ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SEMES_DAT ";
        $query .= "     WHERE ";
        $query .= "             YEAR     = '".$model->exp_year."' ";
        $query .= "         AND SCHREGNO = '".$model->schregno."' ";
        $query .= "     GROUP BY ";
        $query .= "         SEMESTER, ";
        $query .= "         SCHREGNO ";
        $query .= " ), MAIN AS ( ";
        $query .= "     SELECT ";
        $query .= "         SEMESTER, ";
        $query .= "         SCHREGNO, ";
        $query .= "         LESSON, ";
        $query .= "         SUSPEND, ";
        $query .= "         MOURNING, ";
        $query .= "         VALUE(LESSON,0) - VALUE(SUSPEND,0) - VALUE(MOURNING,0) AS ATTEND1, ";
        $query .= "         SICK, ";
        $query .= "         VALUE(LESSON,0) - VALUE(SUSPEND,0) - VALUE(MOURNING,0) - VALUE(SICK,0) AS ATTEND2, ";
        $query .= "         LATE, ";
        $query .= "         EARLY ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SEM ";
        $query .= " ) ";

        $query .= "     SELECT ";
        $query .= "         T1.SEMESTER, ";
        $query .= "         T1.SEMESTERNAME, ";
        $query .= "         T2.SCHREGNO, ";
        $query .= "         T2.LESSON, ";
        $query .= "         T2.SUSPEND, ";
        $query .= "         T2.MOURNING, ";
        $query .= "         T2.ATTEND1, ";
        $query .= "         T2.SICK, ";
        $query .= "         T2.ATTEND2, ";
        $query .= "         T2.LATE, ";
        $query .= "         T2.EARLY ";
        $query .= "     FROM ";
        $query .= "         SEMESTER_MST T1 ";
        $query .= "         LEFT JOIN MAIN T2 ON T1.SEMESTER = T2.SEMESTER ";
        $query .= "     WHERE ";
        $query .= "             T1.YEAR      = '".$model->exp_year."' ";
        $query .= "         AND T1.SEMESTER <> '9' ";
        $query .= "     ORDER BY ";
        $query .= "         T1.SEMESTER ";

        return $query;
    }
}
?>