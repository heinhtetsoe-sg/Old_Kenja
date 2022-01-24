<?php

require_once('for_php7.php');
class knja120Query extends Query {

    function getSecurityHigh() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJA120' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    //PROGRAMPATH取得
    function getProgrampath() {
        $query  = " WITH MENU AS ( ";
        $query .= "     SELECT ";
        $query .= "         MIN(MENUID) AS MENUID ";
        $query .= "     FROM ";
        $query .= "         MENU_HIGH_SECURITY_MST ";
        $query .= "     WHERE ";
        $query .= "         PROGRAMID ='KNJA120' ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     PROGRAMPATH ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST T1, ";
        $query .= "     MENU T2 ";
        $query .= " WHERE ";
        $query .= "     T1.MENUID = T2.MENUID AND ";
        $query .= "     T1.PROGRAMID ='KNJA120' ";

        return $query;
    }

    //近大判定用
    function getKindaiJudgment($model)
    {
        $db = Query::dbCheckOut();
        $query = "SELECT COUNT(*) FROM SCHOOL_MST WHERE YEAR = '".$model->exp_year."' AND SCHOOLNAME1 LIKE '%近畿大学%' ";
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

    //調査書より読込み(6分割)
    function getHexamTrainRef($model)
    {
        $query  = "     SELECT ";
        $query .= "         REMARK ";
        $query .= "     FROM ";
        $query .= "         HEXAM_ENTREMARK_TRAINREF_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$model->exp_year}' ";
        $query .= "         AND SCHREGNO = '{$model->schregno}' ";
        $query .= "         AND TRAIN_SEQ BETWEEN '101' AND '106' ";
        $query .= "     ORDER BY TRAIN_SEQ ";

        return $query;
    }

    //１レコード取得
    function getTrainRow($schregno, $model)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT * ";
        $query .= "   FROM htrainremark_dat ";
        $query .= "  WHERE year     = '".$model->exp_year."'";
        $query .= "    AND schregno = '".$schregno."'";

        $row = $db->getRow($query,DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }
    
    //総合的な学習の時間、奉仕の時間
    function getTrainHRow($schregno)
    {
        $db = Query::dbCheckOut();

        $query  = "SELECT * FROM HTRAINREMARK_HDAT ";
        $query .= "WHERE SCHREGNO ='".$schregno."' ";

        $row = $db->getRow($query,DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //名称マスタより東京仕様かどうか確認
    function getNamecd($cd, $value)
    {
        $db = Query::dbCheckOut();
        
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "         NAMECD1 = '".$cd."' ";
        $query .= "     AND NAMECD2 = '".$value."' ";

        $getname = $db->getOne($query);
        
        Query::dbCheckIn($db);
        
        return $getname;
    }

    function get_record_totalstudytime_dat($model) {
        $query  = " SELECT ";
        $query .= "     TOTALSTUDYTIME, ";
        $query .= "     TOTALSTUDYACT ";
        $query .= " FROM ";
        $query .= "     RECORD_TOTALSTUDYTIME_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";
        if ($model->Properties["tutisyoSougoushokengakki"] == '1') {
            $query .= " AND SEMESTER = '9' ";
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
    function &getInsertQuery($fields,$schregno, $model)
    {
        $db = Query::dbCheckOut();

        //年次の取得
        $query  = "SELECT annual FROM schreg_regd_dat ";
        $query .= " WHERE schregno = '".$schregno."'";
        $query .= "   AND year = '".$model->exp_year."'";
        $query .= "   AND semester = '".$model->exp_semester."'";

        $data["YEAR"][TEXT]                 = $model->exp_year;
        $data["SCHREGNO"][TEXT]             = $schregno;
        $data["ANNUAL"][TEXT]               = $db->getOne($query);
        $data["SPECIALACTREMARK"][TEXT]     = $fields["SPECIALACTREMARK"];
        $data["TOTALREMARK"][TEXT]          = $fields["TOTALREMARK"];
        $data["ATTENDREC_REMARK"][TEXT]     = $fields["ATTENDREC_REMARK"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]            = "sysdate()";

        $db->query(Query::insertSQL($data, "htrainremark_dat"));

        Query::dbCheckIn($db);
        return ;
    }
    function &getInsertHdat($fields,$schregno, $model)
    {
        $db = Query::dbCheckOut();

        $data["SCHREGNO"][TEXT]             = $schregno;
        $data["TOTALSTUDYACT"][TEXT]        = $fields["TOTALSTUDYACT"];
        $data["TOTALSTUDYVAL"][TEXT]        = $fields["TOTALSTUDYVAL"];
        if ($model->getname === 'tokyoto') {
            $data["TOTALSTUDYACT2"][TEXT]       = $fields["TOTALSTUDYACT2"];
            $data["TOTALSTUDYVAL2"][TEXT]       = $fields["TOTALSTUDYVAL2"];
            $data["CREDITREMARK"][TEXT]         = $fields["CREDITREMARK"];
        }
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $db->query(Query::insertSQL($data, "htrainremark_hdat"));

        Query::dbCheckIn($db);
        return ;
    }

    //DELETE
    function &getDeleteQuery($schregno, $model)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM htrainremark_dat ";
        $query .= " WHERE YEAR = '".$model->exp_year."'";
        $query .= "   AND SCHREGNO = '".$schregno."'";

        $db->query($query);

        Query::dbCheckIn($db);
        return $result;
    }
    function &getDeleteHdat($schregno)
    {
        $db = Query::dbCheckOut();

        $query  = "DELETE FROM HTRAINREMARK_HDAT ";
        $query .= "WHERE SCHREGNO = '".$schregno."' ";

        $db->query($query);

        Query::dbCheckIn($db);
        return $result;
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
        //HREPORTREMARK_DATのデータ取得
        $query  = " WITH DATA1 AS ( ";
        $query .= " SELECT ";
        $query .= "     t1.YEAR, ";
        $query .= "     t1.SCHREGNO, ";
        $query .= "     t1.TOTALSTUDYTIME, ";
        $query .= "     t1.SPECIALACTREMARK, ";
        $query .= "     t1.COMMUNICATION, ";
        $query .= "     t1.REMARK1, ";
        $query .= "     t1.REMARK2, ";
        $query .= "     t1.SEMESTER ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT t1 ";
        $query .= "  WHERE t1.SCHREGNO = '" .$model->schregno ."' ";
        $query .= "    AND t1.YEAR     = '" .$model->year_cmb ."' ";
        //HREPORTREMARK_DETAIL_DATのDIV=03、CODE=01のデータ取得
        $query .= " ), DATA2 AS ( ";
        $query .= " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     SEMESTER, ";
        $query .= "     SCHREGNO, ";
        $query .= "     REMARK1 ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DETAIL_DAT ";
        $query .= "  WHERE SCHREGNO = '" .$model->schregno ."' ";
        $query .= "    AND YEAR     = '" .$model->year_cmb ."' ";
        $query .= "    AND DIV      = '03' ";
        $query .= "    AND CODE     = '01' ";
        //HREPORTREMARK_DETAIL_DATのDIV=03、CODE=02のデータ取得
        $query .= " ),DATA3 AS ( ";
        $query .= " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     SEMESTER, ";
        $query .= "     SCHREGNO, ";
        $query .= "     REMARK1 ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DETAIL_DAT ";
        $query .= "  WHERE SCHREGNO = '" .$model->schregno ."' ";
        $query .= "    AND YEAR     = '" .$model->year_cmb ."' ";
        $query .= "    AND DIV      = '03' ";
        $query .= "    AND CODE     = '02' ";
        $query .= " ) ";
        //main処理
        $query .= " SELECT ";
        $query .= "     t1.TOTALSTUDYTIME, ";
        $query .= "     t1.SPECIALACTREMARK, ";
        $query .= "     t1.COMMUNICATION, ";
        $query .= "     t1.REMARK1, ";
        $query .= "     t1.REMARK2, ";
        $query .= "     t2.REMARK1 AS REMARK_0301, ";
        $query .= "     t3.REMARK1 AS REMARK_0302, ";
        $query .= "     t1.SEMESTER ";
        $query .= " FROM ";
        $query .= "    DATA1 t1 ";
        $query .= "    LEFT JOIN DATA2 t2 ON t1.YEAR = t2.YEAR ";
        $query .= "                      AND t1.SCHREGNO = t2.SCHREGNO ";
        $query .= "                      AND t1.SEMESTER = t2.SEMESTER ";
        $query .= "    LEFT JOIN DATA3 t3 ON t1.YEAR = t3.YEAR ";
        $query .= "                      AND t1.SCHREGNO = t3.SCHREGNO ";
        $query .= "                      AND t1.SEMESTER = t3.SEMESTER ";
        $query .= " ORDER BY ";
        $query .= "     t1.SEMESTER ";        

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
    
    //学年（年次）コンボボックス用(Subform4)
    function selectQueryAnnual($model, $JH="")
    {
        if ($model->control["学校区分"] == "0"){     //学年制
            $table = "V_REGDYEAR_GRADE_DAT";
        }else if ($model->control["学校区分"] == "1"){     //単位制
            $table = "V_REGDYEAR_UNIT_DAT";
        }
        $query = " SELECT DISTINCT ";
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

    //学校判定用
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
        $query .= "     ORDER BY ";
        $query .= "         GRADE ";
        $query .= "     FETCH FIRST 1 ROWS ONLY ";
        $query .= " ) ";
        $query .= " , GRADE_2019 AS ( ";
        $query .= "     SELECT ";
        $query .= "         * ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_GDAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '2019' ";
        $query .= "         AND SCHOOL_KIND = 'H' ";
        $query .= "     ORDER BY ";
        $query .= "         GRADE ";
        $query .= "     FETCH FIRST 2 ROWS ONLY ";
        $query .= " ) ";
        $query .= " SELECT * FROM GRADE_2018 ";
        $query .= " UNION ";
        $query .= " SELECT * FROM GRADE_2019 ";
        $query .= " ORDER BY ";
        $query .= "     YEAR ";
        $query .= "     , GRADE ";

        return $query;
    }

    function getGradeCd($model) {
        $query  = " SELECT ";
        $query .= "   GRADE_CD ";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "   YEAR = '".$model->exp_year."' ";
        $query .= "   AND GRADE = '".$model->grade."' ";
        return $query;
    }
}
?>
