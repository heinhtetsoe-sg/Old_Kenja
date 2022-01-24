<?php
class knja120eQuery extends Query
{
    //自立活動の記録表示制御用
    public function getSchregBaseMstHandicap($model)
    {
        $query .= " SELECT ";
        $query .= "     HANDICAP ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";

        return $query;
    }

    public function getSecurityHigh()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJA120E' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    //近大判定用
    public function getKindaiJudgment($db, $model)
    {
        $query = "SELECT COUNT(*) FROM SCHOOL_MST WHERE YEAR = '".$model->exp_year."' AND SCHOOLNAME1 LIKE '%近畿大学%' ";
        $cnt = $db->getOne($query);

        return $cnt;
    }

    //学校名取得の取得
    public function getSchoolName($flg = "")
    {
        $query  = " SELECT ";
        if ($flg) {
            $query .= "     NAME1 ";
        } else {
            $query .= "     NAME1, ";
            $query .= "     NAME2 ";
        }
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";

        return $query;
    }

    //卒業可能学年か判定
    public function getGraduationGrade($db, $model)
    {
        //DB接続

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

        return $ret;
    }

    //調査書より読込
    public function getHexamEntremark($db, $model)
    {
        $query  = " SELECT ";
        $query .= "     T1.ATTENDREC_REMARK, ";
        $query .= "     T1.SPECIALACTREC, ";
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

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        return $row;
    }

    //調査書より読込み(6分割)
    public function getHexamTrainRef($model)
    {
        $query  = "     SELECT ";
        $query .= "         TRAIN_SEQ, REMARK ";
        $query .= "     FROM ";
        $query .= "         HEXAM_ENTREMARK_TRAINREF_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$model->exp_year}' ";
        $query .= "         AND SCHREGNO = '{$model->schregno}' ";
        $query .= "         AND TRAIN_SEQ BETWEEN '101' AND '106' ";
        $query .= "     ORDER BY TRAIN_SEQ ";

        return $query;
    }

    //諸事項
    public function getHtrainTrainRef($model)
    {
        $query  = "     SELECT ";
        $query .= "         TRAIN_SEQ, REMARK ";
        $query .= "     FROM ";
        $query .= "         HTRAINREMARK_TRAINREF_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$model->exp_year}' ";
        $query .= "         AND SCHREGNO = '{$model->schregno}' ";
        $query .= "         AND TRAIN_SEQ BETWEEN '101' AND '106' ";
        $query .= "     ORDER BY TRAIN_SEQ ";

        return $query;
    }

    //通知票取込(1, 2年次)
    public function getRecordTotalstudytimeDat($model)
    {
        $setyear = "";
        $setyear = $model->exp_year == "" ? CTRL_YEAR : $model->exp_year;

        $query  = " SELECT ";
        $query .= "     TOTALSTUDYTIME, ";
        $query .= "     TOTALSTUDYACT ";
        $query .= " FROM ";
        $query .= "     RECORD_TOTALSTUDYTIME_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";
        $query .= " AND YEAR =  '{$setyear}' ";
        //通知表取込ボタンでのデータ取得（9学期のみのプロパティ)
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

    //１レコード取得
    public function getTrainRow($db, $schregno, $exp_year)
    {
        $query  = " SELECT ";
        $query .= "   T1.* ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$exp_year."'";
        $query .= "     AND T1.SCHREGNO = '".$schregno."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        return $row;
    }

    //自立活動の記録取得
    public function getHtrainremarkDetail2Dat($db, $schregno, $exp_year)
    {
        $query .= " SELECT ";
        $query .= "     REMARK1 ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_DETAIL2_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$exp_year}' ";
        $query .= "     AND SCHREGNO = '{$schregno}' ";
        $query .= "     AND HTRAIN_SEQ = '001' ";

        return $db->getOne($query);
    }

    //学年名取得
    public function getGradeName($model)
    {
        $query  = " SELECT ";
        $query .= "     GRADE_NAME1 ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     GRADE       = '".$model->grade."' ";

        return $query;
    }

    //定型分マスター一覧取得
    public function getHtrainRemarkTempDat($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_TEMP_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     GRADE       = '".$model->grade."' AND ";
        if ($model->cmd === 'teikei_act') {
            $query .= "     DATA_DIV    = '01' ";
        } else {
            $query .= "     DATA_DIV    = '02' ";
        }
        $query .= " ORDER BY ";
        $query .= "     PATTERN_CD ";

        return $query;
    }

    //INSERT
    public function &getInsertQuery($model, $fields, $schregno, $exp_year, $exp_semester)
    {
        $db = Query::dbCheckOut();

        $query = knja120eQuery::getDataCnt($schregno, $exp_year);
        $dataCnt = $db->getOne($query);

        //年次の取得
        $query  = "SELECT annual FROM schreg_regd_dat ";
        $query .= " WHERE schregno = '".$schregno."'";
        $query .= "   AND year = '".$exp_year."'";
        $query .= "   AND semester = '".$exp_semester."'";
        $setAnnual = $db->getOne($query);
        $data = array();
        if ($dataCnt > 0) {
            $data["ANNUAL"][TEXT]               = $setAnnual;
            $data["TOTALSTUDYACT"][TEXT]        = $fields["TOTALSTUDYACT"];
            $data["TOTALSTUDYVAL"][TEXT]        = $fields["TOTALSTUDYVAL"];
            $data["SPECIALACTREMARK"][TEXT]     = $fields["SPECIALACTREMARK"];
            $data["ATTENDREC_REMARK"][TEXT]     = $fields["ATTENDREC_REMARK"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]            = "sysdate()";

            $where  = " WHERE ";
            $where .= "   YEAR = '".$exp_year."' ";
            $where .= "   AND SCHREGNO = '".$schregno."' ";

            $db->query(Query::updateSQL($data, "HTRAINREMARK_DAT", $where));
        } else {
            $data["YEAR"][TEXT]                 = $exp_year;
            $data["SCHREGNO"][TEXT]             = $schregno;
            $data["ANNUAL"][TEXT]               = $setAnnual;
            $data["TOTALSTUDYACT"][TEXT]        = $fields["TOTALSTUDYACT"];
            $data["TOTALSTUDYVAL"][TEXT]        = $fields["TOTALSTUDYVAL"];
            $data["SPECIALACTREMARK"][TEXT]     = $fields["SPECIALACTREMARK"];
            $data["ATTENDREC_REMARK"][TEXT]     = $fields["ATTENDREC_REMARK"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]            = "sysdate()";

            $db->query(Query::insertSQL($data, "HTRAINREMARK_DAT"));
        }

        //詳細
        $arr = array("1" => "101", "2" => "102", "3" => "103", "4" => "104", "5" => "105", "6" => "106");
        foreach ($arr as $n => $seq) {
            $query = knja120eQuery::getTrainRefCount($schregno, $exp_year, $seq);
            $dataCnt = $db->getOne($query);

            if (isset($fields["TRAIN_REF".$n."_NO_COMM"])) {
                $fields["TRAIN_REF".$n] = $fields["NO_COMMENTS_LABEL"];
            }
            $data = array();
            $data["REMARK"][TEXT]             = $fields["TRAIN_REF".$n];
            $data["REGISTERCD"][TEXT]         = STAFFCD;
            $data["UPDATED"][FUNC]            = "sysdate()";
            if ($dataCnt > 0) {
                $where  = " WHERE ";
                $where .= "   YEAR = '".$exp_year."' ";
                $where .= "   AND SCHREGNO = '".$schregno."' ";
                $where .= "   AND TRAIN_SEQ = '".$seq."' ";

                $query = Query::updateSQL($data, "HTRAINREMARK_TRAINREF_DAT", $where);
            } else {
                $data["YEAR"][TEXT]                 = $exp_year;
                $data["SCHREGNO"][TEXT]             = $schregno;
                $data["TRAIN_SEQ"][TEXT]          = $seq;

                $query = Query::insertSQL($data, "HTRAINREMARK_TRAINREF_DAT");
            }
            $db->query($query);
        }
        if ($db->getOne(knja120eQuery::getSchregBaseMstHandicap($model)) == '002') {
            //詳細2
            $where  = " WHERE ";
            $where .= "   YEAR = '".$exp_year."' ";
            $where .= "   AND SCHREGNO = '".$schregno."' ";
            $where .= "   AND HTRAIN_SEQ = '001' ";

            $data = array();
            $data["REMARK1"][TEXT]            = $fields["ZIRITUKATUDOU"];
            $data["REGISTERCD"][TEXT]         = STAFFCD;
            $data["UPDATED"][FUNC]            = "sysdate()";
            if ($db->getOne("SELECT COUNT(*) FROM HTRAINREMARK_DETAIL2_DAT " . $where) > 0) {
                $query = Query::updateSQL($data, "HTRAINREMARK_DETAIL2_DAT", $where);
            } else {
                $data["YEAR"][TEXT]                 = $exp_year;
                $data["SCHREGNO"][TEXT]             = $schregno;
                $data["HTRAIN_SEQ"][TEXT]           = '001';

                $query = Query::insertSQL($data, "HTRAINREMARK_DETAIL2_DAT");
            }
            $db->query($query);
        }
        Query::dbCheckIn($db);
        return ;
    }

    //DATA有無
    public function getDataCnt($schregno, $exp_year)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$exp_year."'";
        $query .= "     AND SCHREGNO = '".$schregno."'";

        return $query;
    }

    //HTRAINREMARK_TRAINREF_DAT有無
    public function getTrainRefCount($schregno, $exp_year, $seq)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_TRAINREF_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$exp_year."'";
        $query .= "     AND SCHREGNO = '".$schregno."'";
        $query .= "     AND TRAIN_SEQ = '".$seq."'";

        return $query;
    }

    //通知表所見より年度を取得
    public function selectQueryYear($model)
    {
        $query  = "SELECT distinct t1.year ";
        $query .= "  FROM hreportremark_dat t1  ";
        $query .= " WHERE t1.schregno = '" .$model->schregno ."'  ";
        $query .= " ORDER BY t1.year desc ";

        return $query;
    }

    //通知表所見より再読込
    public function selectQueryGuide($model)
    {
        $query  = " SELECT t1.totalstudytime,t1.specialactremark,t1.communication, t1.remark1, t1.remark2, t1.semester ";
        $query .= "   FROM hreportremark_dat t1 ";
        $query .= "  WHERE t1.schregno = '" .$model->schregno ."' ";
        $query .= "    and t1.year     = '" .$model->year_cmb ."' ";
        $query .= "  ORDER BY t1.semester";

        return $query;
    }

    //セット日々出欠備考
    public function getSemesRemark($db, $model)
    {
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
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($count == 0) {
                $set_remark .= $row["REMARK1"];
            } else {
                if ($row["REMARK1"] != "") {
                    $set_remark .= "／".$row["REMARK1"];
                }
            }
            $count++;
        }

        return $set_remark;
    }

    //学年（年次）コンボボックス用(subformSeisekiSansho)
    public function selectQueryAnnual($model)
    {
        if ($model->control["学校区分"] == "0") {     //学年制
            $table = "V_REGDYEAR_GRADE_DAT";
        } elseif ($model->control["学校区分"] == "1") {     //単位制
            $table = "V_REGDYEAR_UNIT_DAT";
        }
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.ANNUAL ";
        $query .= " FROM ";
        $query .= "     ".$table." T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '" .$model->schregno ."'";
        $query .= "     AND T1.ANNUAL ";
        $query .= "         IN (SELECT GRADE FROM SCHREG_REGD_GDAT ";
        $query .= "             WHERE YEAR = '".CTRL_YEAR."' ";
        $query .= "               AND SCHOOL_KIND NOT IN ('P', 'J', 'K') ";
        $query .= "             ) ";

        return $query;
    }

    //成績参照画面(subformSeisekiSansho)
    public function selectQuerySubForm4($model)
    {
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
        $query .= "   LEFT JOIN STUDYRECREMARK_DAT T2 ON  ";
        $query .= "                   T1.YEAR       = T2.YEAR AND  ";
        $query .= "                   T1.SCHREGNO   = T2.SCHREGNO AND  ";
        $query .= "                   T1.CLASSCD    = T2.CLASSCD AND  ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                   T1.SCHOOL_KIND    = T2.SCHOOL_KIND AND  ";
            $query .= "                   T1.CURRICULUM_CD  = T2.CURRICULUM_CD AND  ";
        }
        $query .= "                   T1.SUBCLASSCD = T2.SUBCLASSCD ";
        $query .= "   LEFT JOIN CLASS_MST    T3 ON  ";
        $query .= "                   T1.CLASSCD    = T3.CLASSCD  ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "               AND T1.SCHOOL_KIND    = T3.SCHOOL_KIND  ";
        }
        $query .= "   LEFT JOIN SUBCLASS_MST T4 ON  ";
        $query .= "                   T1.SUBCLASSCD = T4.SUBCLASSCD  ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "               AND T1.CLASSCD        = T4.CLASSCD  ";
            $query .= "               AND T1.SCHOOL_KIND    = T4.SCHOOL_KIND  ";
            $query .= "               AND T1.CURRICULUM_CD  = T4.CURRICULUM_CD  ";
        }
        $query .= " WHERE ";
        $query .= "   T1.SCHOOLCD = '0' ";
        if ($model->annual["YEAR"] != "0" || $model->annual["ANNUAL"] != "0") {
            $query .= "   AND T1.YEAR = '" .$model->annual["YEAR"] ."' ";
            $query .= "   AND T1.ANNUAL = '" .$model->annual["ANNUAL"]."' ";
        }
        $query .= "     AND T1.ANNUAL ";
        $query .= "         IN (SELECT GRADE FROM SCHREG_REGD_GDAT ";
        $query .= "             WHERE YEAR = '".CTRL_YEAR."' ";
        $query .= "               AND SCHOOL_KIND NOT IN ('P', 'J', 'K') ";
        $query .= "             ) ";
        $query .= "     AND T1.SCHREGNO = '" .$model->schregno ."' ";
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

    //明治判定
    public function getMeijiHantei($db)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' AND ";
        $query .= "     NAME1   = 'meiji' ";

        $meiji = $db->getOne($query);
        return $meiji;
    }

    //行動の記録取得
    public function getActionDocumentDat($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.ACTIONDATE, ";
        $query .= "     T1.SEQ, ";
        $query .= "     T1.ACTIONTIME, ";
        $query .= "     L1.NAME1 AS DIVIDECD, ";
        $query .= "     T1.TITLE, ";
        $query .= "     T1.TEXT ";
        $query .= " FROM ";
        $query .= "     ACTION_DOCUMENT_DAT T1 ";
        $query .= "     LEFT JOIN V_NAME_MST L1 ";
        $query .= "          ON L1.YEAR     = '".$model->exp_year."' ";
        $query .= "         AND L1.NAMECD1  = 'H307' ";
        $query .= "         AND L1.NAMECD2  = T1.DIVIDECD ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.ACTIONDATE DESC, ";
        $query .= "     T1.ACTIONTIME DESC, ";
        $query .= "     T1.SEQ ";

        return $query;
    }
    public function getStudents($model)
    {
        $query .= " select ";
        $query .= "     T1.*, ";
        $query .= "     T1.SCHREGNO as VALUE, ";
        $query .= "     T1.SCHREGNO || ' ' || T3.HR_NAME || ' ' || T1.ATTENDNO || '番' || ' ' || T4.NAME_SHOW as LABEL ";
        $query .= " from ";
        $query .= "     DB2INST1.SCHREG_REGD_DAT T1 ";
        $query .= " inner join ";
        $query .= "     (select ";
        $query .= "         GRADE, ";
        $query .= "         HR_CLASS, ";
        $query .= "         SEMESTER, ";
        $query .= "         YEAR ";
        $query .= "     from ";
        $query .= "         DB2INST1.SCHREG_REGD_DAT ";
        $query .= "     where ";
        $query .= "         SCHREGNO='{$model->schregno}' ";
        $query .= "         and SEMESTER='{$model->exp_semester}' ";
        $query .= "         and YEAR='{$model->exp_year}') T2 ";
        $query .= " on ";
        $query .= "     T1.GRADE = T2.GRADE ";
        $query .= "     and T1.HR_CLASS = T2.HR_CLASS ";
        $query .= "     and T1.SEMESTER = T2.SEMESTER ";
        $query .= "     and T1.YEAR = T2.YEAR ";
        $query .= " left join ";
        $query .= "     SCHREG_REGD_HDAT T3 ";
        $query .= " on ";
        $query .= "     T1.YEAR = T3.YEAR ";
        $query .= "     and T1.SEMESTER = T3.SEMESTER ";
        $query .= "     and T1.GRADE = T3.GRADE ";
        $query .= "     and T1.HR_CLASS = T3.HR_CLASS ";
        $query .= " left join ";
        $query .= "     SCHREG_BASE_MST T4 ";
        $query .= " on ";
        $query .= "     T1.SCHREGNO = T4.SCHREGNO ";
        $query .= " order by ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }
    public function getDeleteTyousasyoQuery($model)
    {
        $query .= " DELETE FROM ";
        $query .= "     HTRAINREMARK_TRAINREF_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->exp_year}' ";
        $query .= "     AND SCHREGNO IN('".join("','", $model->field['CATEGORY_SELECTED'])."'); ";

        return $query;
    }
    public function getInsertTyousasyoQuery($model)
    {
        $query .= " INSERT INTO HTRAINREMARK_TRAINREF_DAT (YEAR,SCHREGNO,TRAIN_SEQ,REMARK) ";
        $query .= " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO, ";
        $query .= "     TRAIN_SEQ, ";
        $query .= "     REMARK ";
        $query .= " FROM ";
        $query .= "     HEXAM_ENTREMARK_TRAINREF_SEQ_DAT ";
        $query .= " WHERE ";
        $query .= "     PATTERN_SEQ = '{$model->field['PATTERN']}' ";
        $query .= "     AND YEAR = '{$model->exp_year}' ";
        $query .= "     AND SCHREGNO IN('".join("','", $model->field['CATEGORY_SELECTED'])."'); ";

        return $query;
    }
}
