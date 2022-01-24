<?php

require_once('for_php7.php');

class knjd120oquery extends Query
{

    //RECORD_DATに存在しない生徒をinsert(履修学期が複数あれば複数レコード作成）
    public function insertExStdRecQuery($model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd          = substr($model->field["SUBCLASSCD"], 0, 2);
            $substr_school_kind      = substr($model->field["SUBCLASSCD"], 3, 1);
            $substr_curriculum_cd    = substr($model->field["SUBCLASSCD"], 5, 1);
            $substr_subclasscd       = substr($model->field["SUBCLASSCD"], 7, 6);
        }
        $query  = " INSERT INTO RECORD_DAT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " (YEAR,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD,TAKESEMES,SCHREGNO,CHAIRCD,REGISTERCD,UPDATED) ";
        } else {
            $query .= " (YEAR,SUBCLASSCD,TAKESEMES,SCHREGNO,CHAIRCD,REGISTERCD,UPDATED) ";
        }
        $query .= " SELECT DISTINCT ";
        $query .= "    t1.YEAR, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     t2.CLASSCD, ";
            $query .= "     t2.SCHOOL_KIND, ";
            $query .= "     t2.CURRICULUM_CD, ";
        }
        $query .= "    t2.SUBCLASSCD, ";
        $query .= "    T2.TAKESEMES, ";
        $query .= "    t1.SCHREGNO, ";
        $query .= "    t1.CHAIRCD, ";
        $query .= "    '".STAFFCD."', ";
        $query .= "    sysdate() ";
        $query .= " FROM ";
        $query .= "    CHAIR_STD_DAT t1, ";
        $query .= "    CHAIR_DAT t2 ";
        $query .= " WHERE ";
        $query .= "     t1.YEAR       = t2.YEAR AND ";
        $query .= "     t1.CHAIRCD    = t2.CHAIRCD AND ";
        $query .= "     T1.semester   = T2.semester AND ";
        $query .= "     t1.YEAR       = '".CTRL_YEAR."' AND ";
        $query .= "     t1.CHAIRCD    = '".$model->field["CHAIRCD"]."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     t2.CLASSCD          = '".$substr_classcd."' AND ";
            $query .= "     t2.SCHOOL_KIND      = '".$substr_school_kind."' AND ";
            $query .= "     t2.CURRICULUM_CD    = '".$substr_curriculum_cd."' AND ";
            $query .= "     t2.SUBCLASSCD       = '".$substr_subclasscd."' AND ";
        } else {
            $query .= "     t2.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' AND ";
        }
        $query .= "     t1.SCHREGNO NOT IN (SELECT ";
        $query .= "                             SCHREGNO ";
        $query .= "                         FROM ";
        $query .= "                             RECORD_DAT ";
        $query .= "                         WHERE ";
        $query .= "                             YEAR='".CTRL_YEAR."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                               AND CLASSCD        = '".$substr_classcd."' ";
            $query .= "                               AND SCHOOL_KIND    = '".$substr_school_kind."' ";
            $query .= "                               AND CURRICULUM_CD  = '".$substr_curriculum_cd."' ";
            $query .= "                               AND SUBCLASSCD     = '".$substr_subclasscd."') ";
        } else {
            $query .= "                               AND SUBCLASSCD = '".$model->field["SUBCLASSCD"]."') ";
        }

        return $query;
    }

    //編集可能学期の判別
    public function selectContolCodeQuery($model)
    {
        $query  = " SELECT ";
        $query .= "    CONTROL_CODE ";
        $query .= " FROM ";
        $query .= "     ADMIN_CONTROL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "                     SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        }
        $query .= "     CONTROL_FLG = '1' ";

        return $query;
    }

    //科目データの取得
    public function selectSubclassQuery($gen_ed, $model)
    {
        $query  = " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T3.CLASSCD, ";
            $query .= "     T3.SCHOOL_KIND, ";
            $query .= "     T3.CURRICULUM_CD, ";
        }
        $query .= "     T3.subclasscd, ";
        $query .= "     T3.subclassname ";
        $query .= " FROM ";
        $query .= "     chair_dat T1, ";
        $query .= "     chair_std_dat T2, ";
        $query .= "     subclass_mst T3, ";
        $query .= "     chair_stf_dat T4 ";
        $query .= " WHERE ";
        $query .= "     T1.year       = '".CTRL_YEAR."' AND ";
        $query .= "     T1.semester   = '".CTRL_SEMESTER."' AND ";
        $query .= "     T2.year       = T1.year AND ";
        $query .= "     T1.year       = T4.year AND ";
        $query .= "     T1.semester   = T4.semester AND ";
        $query .= "     T1.chaircd    = T4.chaircd AND ";
        $query .= "     T2.semester   = T1.semester AND ";
        $query .= "     T2.chaircd    = T1.chaircd AND ";
        $query .= "    (T3.subclasscd NOT LIKE '9%' OR T3.subclasscd = '".$gen_ed."' ) AND ";
        //更新可能(制限付)
        if (AUTHORITY != DEF_UPDATABLE) {
            $query .= "     t4.STAFFCD = '".STAFFCD."' AND ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T3.CLASSCD = T1.CLASSCD  AND ";
            $query .= "     T3.SCHOOL_KIND = T1.SCHOOL_KIND  AND ";
            $query .= "     T3.CURRICULUM_CD = T1.CURRICULUM_CD  AND ";
        }
        $query .= "     T3.subclasscd = T1.subclasscd ";
        $query .= " GROUP BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T3.CLASSCD, ";
            $query .= "     T3.SCHOOL_KIND, ";
            $query .= "     T3.CURRICULUM_CD, ";
        }
        $query .= "     T3.subclasscd, ";
        $query .= "     T3.subclassname ";
        $query .= " ORDER BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T3.CLASSCD, ";
            $query .= "     T3.SCHOOL_KIND, ";
            $query .= "     T3.CURRICULUM_CD, ";
        }
        $query .= "     T3.subclasscd ";
        return $query;
    }
    
    //講座データの取得
    public function selectChairQuery($model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd          = substr($model->field["SUBCLASSCD"], 0, 2);
            $substr_school_kind      = substr($model->field["SUBCLASSCD"], 3, 1);
            $substr_curriculum_cd    = substr($model->field["SUBCLASSCD"], 5, 1);
            $substr_subclasscd       = substr($model->field["SUBCLASSCD"], 7, 6);
        }
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.chaircd, ";
        $query .= "     T1.chairname ";
        $query .= " FROM ";
        $query .= "     chair_dat T1, ";
        $query .= "     chair_std_dat T2, ";
        $query .= "     chair_stf_dat T3 ";
        $query .= " WHERE ";
        $query .= "     T1.year       = '".CTRL_YEAR."' AND ";
        $query .= "     T1.semester   = '".CTRL_SEMESTER."' AND ";
        $query .= "     T2.year       = T1.year AND ";
        $query .= "     T2.semester   = T1.semester AND ";
        $query .= "     T2.chaircd    = T1.chaircd AND ";
        $query .= "     T1.year       = T3.year AND ";
        $query .= "     T1.semester   = T3.semester AND ";
        $query .= "     T1.chaircd    = T3.chaircd AND ";
        //更新可能(制限付)
        if (AUTHORITY != DEF_UPDATABLE) {
            $query .= "       t3.STAFFCD = '".STAFFCD."' AND ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD          = '".$substr_classcd."' AND ";
            $query .= "     T1.SCHOOL_KIND      = '".$substr_school_kind."' AND ";
            $query .= "     T1.CURRICULUM_CD    = '".$substr_curriculum_cd."' AND ";
            $query .= "     T1.SUBCLASSCD       = '".$substr_subclasscd."' ";
        } else {
            $query .= "     T1.subclasscd = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.chaircd ";
        return $query;
    }

    //全体のデータの取得
    public function selectQuery($model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd          = substr($model->field["SUBCLASSCD"], 0, 2);
            $substr_school_kind      = substr($model->field["SUBCLASSCD"], 3, 1);
            $substr_curriculum_cd    = substr($model->field["SUBCLASSCD"], 5, 1);
            $substr_subclasscd       = substr($model->field["SUBCLASSCD"], 7, 6);
        }
        //出欠情報
        $query = " WITH TABLE1 AS( ";
        $query .= "     SELECT ";
        $query .= "         MIN(CASE WHEN ST1.SEMESTER = '1' AND ST1.TESTKINDCD = '01' THEN CASE WHEN ADD.ATSUB_REPL_DI_CD IS NOT NULL THEN ADD.ATSUB_REPL_DI_CD ELSE ADD.REP_DI_CD END END) AS SEM1_INTR_SCORE_ATTEND, ";
        $query .= "         MIN(CASE WHEN ST1.SEMESTER = '2' AND ST1.TESTKINDCD = '01' THEN CASE WHEN ADD.ATSUB_REPL_DI_CD IS NOT NULL THEN ADD.ATSUB_REPL_DI_CD ELSE ADD.REP_DI_CD END END) AS SEM2_INTR_SCORE_ATTEND, ";
        $query .= "         MIN(CASE WHEN ST1.SEMESTER = '1' AND ST1.TESTKINDCD = '02' THEN CASE WHEN ADD.ATSUB_REPL_DI_CD IS NOT NULL THEN ADD.ATSUB_REPL_DI_CD ELSE ADD.REP_DI_CD END END) AS SEM1_TERM_SCORE_ATTEND, ";
        $query .= "         MIN(CASE WHEN ST1.SEMESTER = '2' AND ST1.TESTKINDCD = '02' THEN CASE WHEN ADD.ATSUB_REPL_DI_CD IS NOT NULL THEN ADD.ATSUB_REPL_DI_CD ELSE ADD.REP_DI_CD END END) AS SEM2_TERM_SCORE_ATTEND, ";
        $query .= "         MIN(CASE WHEN ST1.SEMESTER = '1' THEN ST3.TRANSFER_SDATE END) AS TRANSFER_SDATE1, ";
        $query .= "         MIN(CASE WHEN ST1.SEMESTER = '2' THEN ST3.TRANSFER_SDATE END) AS TRANSFER_SDATE2, ";
        $query .= "         MIN(CASE WHEN ST1.SEMESTER = '1' THEN ST4.GRD_DATE END) AS GRD_DATE1, ";
        $query .= "         MIN(CASE WHEN ST1.SEMESTER = '2' THEN ST4.GRD_DATE END) AS GRD_DATE2, ";
        $query .= "         ST2.SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         SCH_CHR_TEST ST1, ";
        $query .= "         ATTEND_DAT ST2 LEFT OUTER JOIN SCHREG_TRANSFER_DAT ST3  ";
        $query .= "             ON ST3.SCHREGNO = ST2.SCHREGNO AND ";
        $query .= "             ST2.ATTENDDATE BETWEEN ST3.TRANSFER_SDATE AND ";
        $query .= "             COALESCE(ST3.TRANSFER_EDATE,'9999-12-31') ";
        $query .= "         LEFT OUTER JOIN SCHREG_BASE_MST ST4 ";
        $query .= "             ON ST2.SCHREGNO = ST4.SCHREGNO AND ";
        $query .= "             ST2.ATTENDDATE >= ST4.GRD_DATE ";
        $query .= "         LEFT JOIN ATTEND_DI_CD_DAT ADD ON ST2.YEAR  = ADD.YEAR ";
        $query .= "                                       AND ST2.DI_CD = ADD.DI_CD ";
        $query .= "  ";
        $query .= "     WHERE ";
        $query .= "         ST1.YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "         ST1.CHAIRCD = '".$model->field["CHAIRCD"]."' AND ";
        $query .= "         ST2.YEAR    = ST1.YEAR AND     ";
        $query .= "         ST2.CHAIRCD = ST1.CHAIRCD AND ";
        $query .= "         ST1.TESTITEMCD  = '01' AND ";
        $query .= "         ST2.ATTENDDATE  = ST1.EXECUTEDATE AND ";
        $query .= "         ST1.PERIODCD    = ST2.PERIODCD AND ";
        $query .= "         CASE WHEN ADD.ATSUB_REPL_DI_CD IS NOT NULL THEN ADD.ATSUB_REPL_DI_CD ELSE ADD.REP_DI_CD END IN('1','2','3','4','5','6','8','9','10','11','12','13','14') ";
        $query .= "     GROUP BY ";
        $query .= "         ST2.SCHREGNO ";
        $query .= " ), ";
        //在籍基礎データ
        $query .= " TABLE2 AS( ";
        $query .= "     SELECT ";
        $query .= "         T1.YEAR, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T0.HR_NAME, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         T2.INOUTCD, ";
        $query .= "         T2.NAME_SHOW, ";
        $query .= "         T2.SCHREGNO, ";
        $query .= "         T2.GRD_DATE, ";
        $query .= "         T1.COURSECD || T1.MAJORCD || T1.COURSECODE as COURSE ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_HDAT T0, ";
        $query .= "         SCHREG_REGD_DAT T1, ";
        $query .= "         SCHREG_BASE_MST T2 ";
        $query .= "     WHERE ";
        $query .= "         T0.YEAR     = T1.YEAR AND ";
        $query .= "         T0.SEMESTER = T1.SEMESTER AND ";
        $query .= "         T0.GRADE    = T1.GRADE AND ";
        $query .= "         T0.HR_CLASS = T1.HR_CLASS AND ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "         T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "         T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " ), ";
        //在籍情報
        $query .= " TABLE3 AS( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     SCHREGNO, ";
        $query .= "     MAX(CASE WHEN SEMESTER = '1' THEN SCHREGNO END) AS CHAIR_STD1, ";
        $query .= "     MAX(CASE WHEN SEMESTER = '2' THEN SCHREGNO END) AS CHAIR_STD2 ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     CHAIRCD = '".$model->field["CHAIRCD"]."' ";
        $query .= " GROUP BY ";
        $query .= "     SCHREGNO ";
        $query .= " ) ";
        //仮評定フラグ対応
        if ($model->Properties["useProvFlg"] == '1') {
            $query .= " ,RECORD_PROV_FLG AS ( ";
            $query .= "     SELECT  SCHREGNO, PROV_FLG ";
            $query .= "     FROM   RECORD_PROV_FLG_DAT ";
            $query .= "     WHERE  YEAR = '".CTRL_YEAR."' ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "       AND  CLASSCD          = '".$substr_classcd."' ";
                $query .= "       AND  SCHOOL_KIND      = '".$substr_school_kind."' ";
                $query .= "       AND  CURRICULUM_CD    = '".$substr_curriculum_cd."' ";
                $query .= "       AND  SUBCLASSCD       = '".$substr_subclasscd."' ";
            } else {
                $query .= "       AND  SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
            }
            $query .= "     ) ";
        }
        //成績データ
        $query .= " SELECT ";
        //仮評定フラグ対応
        if ($model->Properties["useProvFlg"] == '1') {
            $query .= "     P0.PROV_FLG, ";
        }
        $query .= "     T1.TAKESEMES, ";
        $query .= "     T1.SEM1_INTR_SCORE, ";
        $query .= "     T1.SEM1_TERM_SCORE, ";
        $query .= "     T1.SEM2_INTR_SCORE, ";
        $query .= "     T1.SEM2_TERM_SCORE, ";
        $query .= "     T1.SEM1_VALUE,SEM2_VALUE, ";
        $query .= "     T1.GRAD_VALUE, ";
        $query .= "     T2.*, ";
        $query .= "     T3.*, ";
        $query .= "     T4.* ";
        $query .= " FROM ";
        $query .= "     RECORD_DAT T1 LEFT OUTER JOIN TABLE1 T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "                   LEFT OUTER JOIN TABLE2 T3 ON T1.SCHREGNO = T3.SCHREGNO ";
        $query .= "                   LEFT OUTER JOIN TABLE3 T4 ON T1.SCHREGNO = T4.SCHREGNO ";
        /* 仮評定情報 */
        //仮評定フラグ対応
        if ($model->Properties["useProvFlg"] == '1') {
            $query .= "                   LEFT OUTER JOIN RECORD_PROV_FLG P0 ON P0.SCHREGNO = T1.SCHREGNO ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "            T1.CLASSCD          = '".$substr_classcd."' AND ";
            $query .= "            T1.SCHOOL_KIND      = '".$substr_school_kind."' AND ";
            $query .= "            T1.CURRICULUM_CD    = '".$substr_curriculum_cd."' AND ";
            $query .= "            T1.SUBCLASSCD       = '".$substr_subclasscd."' AND ";
        } else {
            $query .= "            T1.SUBCLASSCD    ='".$model->field["SUBCLASSCD"]."' AND ";
        }
#        $query .= "     T1.CHAIRCD      = '".$model->field["CHAIRCD"]."' ";
        $query .= "     T1.SCHREGNO IN(SELECT SCHREGNO  ";
        $query .= "                      FROM CHAIR_STD_DAT  ";
        $query .= "                     WHERE YEAR = '".CTRL_YEAR."' ";
        $query .= "                       AND CHAIRCD = '".$model->field["CHAIRCD"]."') ";

        $query .= " ORDER BY ";
        $query .= "     T3.GRADE, ";
        $query .= "     T3.HR_CLASS, ";
        $query .= "     T3.ATTENDNO ";

#        echo $query;
        return $query;
    }

    //考査満点マスタ
    public function getPerfect($year, $subclasscd, $testcd, $grade, $course, $model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd          = substr($model->field["SUBCLASSCD"], 0, 2);
            $substr_school_kind      = substr($model->field["SUBCLASSCD"], 3, 1);
            $substr_curriculum_cd    = substr($model->field["SUBCLASSCD"], 5, 1);
            $substr_subclasscd       = substr($model->field["SUBCLASSCD"], 7, 6);
        }
        $query  = "";
        $query .= "SELECT ";
        $query .= "    case when DIV IS NULL then 100 else PERFECT end as PERFECT ";
        $query .= "FROM ";
        $query .= "    PERFECT_RECORD_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '{$year}' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    CLASSCD          = '".$substr_classcd."' AND ";
            $query .= "    SCHOOL_KIND      = '".$substr_school_kind."' AND ";
            $query .= "    CURRICULUM_CD    = '".$substr_curriculum_cd."' AND ";
            $query .= "    SUBCLASSCD       = '".$substr_subclasscd."' AND ";
        } else {
            $query .= "    SUBCLASSCD = '{$subclasscd}' AND ";
        }
        $query .= "    SEMESTER || TESTKINDCD || TESTITEMCD = '{$testcd}' AND ";
        $query .= "    GRADE = CASE WHEN DIV = '01' THEN '00' ELSE '{$grade}' END AND ";
        $query .= "    COURSECD || MAJORCD || COURSECODE = CASE WHEN DIV IN ('01','02') THEN '00000000' ELSE '{$course}' END ";
        return $query;
    }

    /* データ更新処理 */
    public function update($model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd          = substr($model->field["SUBCLASSCD"], 0, 2);
            $substr_school_kind      = substr($model->field["SUBCLASSCD"], 3, 1);
            $substr_curriculum_cd    = substr($model->field["SUBCLASSCD"], 5, 1);
            $substr_subclasscd       = substr($model->field["SUBCLASSCD"], 7, 6);
        }
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        
        for ($i=0; $i<$model->field["COUNT"]; $i++) {
            $data = array();
            $val = explode("-", $model->fields["SCHREGNO"][$i]);

            //前期
            $data["SEM1_INTR_SCORE"][NUMBER]  = $model->fields["SEM1_INTR_SCORE"][$i];
            $data["SEM1_TERM_SCORE"][NUMBER]  = $model->fields["SEM1_TERM_SCORE"][$i];
            $data["SEM1_VALUE"][NUMBER]       = $model->fields["SEM1_VALUE"][$i];

            //後期
            $data["SEM2_INTR_SCORE"][NUMBER]  = $model->fields["SEM2_INTR_SCORE"][$i];
            $data["SEM2_TERM_SCORE"][NUMBER]  = $model->fields["SEM2_TERM_SCORE"][$i];
            $data["SEM2_VALUE"][NUMBER]       = $model->fields["SEM2_VALUE"][$i];

            $data["GRAD_VALUE"][NUMBER]       = $model->fields["GRAD_VALUE"][$i];

            $data["REGISTERCD"][TEXT]        = STAFFCD ;
            $data["UPDATED"][FUNC]           = "sysdate()";

            $where  = " WHERE YEAR           = '".CTRL_YEAR."' AND ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $where .= "     CLASSCD          = '".$substr_classcd."' AND ";
                $where .= "     SCHOOL_KIND      = '".$substr_school_kind."' AND ";
                $where .= "     CURRICULUM_CD    = '".$substr_curriculum_cd."' AND ";
                $where .= "     SUBCLASSCD       = '".$substr_subclasscd."' AND ";
            } else {
                $where .= "     SUBCLASSCD     = '".$model->field["SUBCLASSCD"]."' AND ";
            }
            $where .= "       SCHREGNO       = '".$val[0]."' AND ";
            $where .= "       TAKESEMES      = '".$val[1]."' ";
#            $where .= "       CHAIRCD        = '".$model->field["CHAIRCD"]."'";

            $query = Query::updateSQL($data, "RECORD_DAT", $where);
            $db->query($query);
            
            //評定フラグ・・・仮評定(1)、本評定(NULL)
            //仮評定フラグ対応
            if ($model->Properties["useProvFlg"] == '1') {
                $query = knjd120oQuery::delRecordProvFlgDat(CTRL_YEAR, $model->field["SUBCLASSCD"], $val[0], $model);
                $db->query($query);
                if ($model->fields["PROV_FLG"][$i] == '1') {
                    $query = knjd120oQuery::insRecordProvFlgDat(CTRL_YEAR, $model->field["SUBCLASSCD"], $val[0], $model);
                    $db->query($query);
                }
            }
        }
        //評価評定を更新
        $query = " UPDATE RECORD_DAT TT1 SET (SEM1_VALUE,SEM2_VALUE,GRAD_VALUE)= ";
        $query .= " (SELECT   ";
        $query .= "     TT2.SEM1_VALUE, ";
        $query .= "     TT2.SEM2_VALUE, ";
        $query .= "     TT2.GRAD_VALUE ";
        $query .= " FROM ( ";
        $query .= "         SELECT ";
        $query .= "             T1.SCHREGNO, ";
        $query .= "             T1.TAKESEMES, ";
        $query .= "             CASE WHEN T1.SEM1_VALUE IS NULL THEN T2.ASSESSLEVEL ELSE T1.SEM1_VALUE END AS SEM1_VALUE, ";
        $query .= "             CASE WHEN T1.SEM2_VALUE IS NULL THEN T3.ASSESSLEVEL ELSE T1.SEM2_VALUE END AS SEM2_VALUE, ";
        $query .= "             CASE WHEN T1.GRAD_VALUE IS NULL THEN T4.ASSESSLEVEL ELSE T1.GRAD_VALUE END AS GRAD_VALUE ";
        $query .= "         FROM ";
        $query .= "             (SELECT ";
        $query .= "                 T1.SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                 T1.CLASSCD, ";
            $query .= "                 T1.SCHOOL_KIND, ";
            $query .= "                 T1.CURRICULUM_CD, ";
        }
        $query .= "                 T1.SUBCLASSCD, ";
        $query .= "                 T1.TAKESEMES, ";
        $query .= "                 CASE WHEN T1.SEM1_TERM_SCORE IS NOT NULL AND COUNT1 > 0 THEN FLOAT((COALESCE(T1.SEM1_INTR_SCORE,0)+COALESCE(T1.SEM1_TERM_SCORE,0))/COUNT1) END AS SCORE_AVG1, ";
        $query .= "                 CASE WHEN T1.SEM2_TERM_SCORE IS NOT NULL AND COUNT2 > 0 THEN FLOAT((COALESCE(T1.SEM2_INTR_SCORE,0)+COALESCE(T1.SEM2_TERM_SCORE,0))/COUNT2) END AS SCORE_AVG2, ";
        $query .= "                 CASE WHEN T1.SEM2_TERM_SCORE IS NOT NULL AND COUNT1+COUNT2>0 THEN FLOAT((COALESCE(T1.SEM1_INTR_SCORE,0)+COALESCE(T1.SEM1_TERM_SCORE,0)+COALESCE(T1.SEM2_INTR_SCORE,0)+COALESCE(T1.SEM2_TERM_SCORE,0))/(COUNT1+COUNT2)) END AS SCORE_AVG3, ";
        $query .= "                 T1.SEM1_VALUE, ";
        $query .= "                 T1.SEM2_VALUE, ";
        $query .= "                 T1.GRAD_VALUE, ";
        $query .= "                 T2.GRADE ";
        $query .= "             FROM ";
        $query .= "                 record_dat T1, ";
        $query .= "                 SCHREG_REGD_DAT T2, ";
        $query .= "                 (SELECT ";
        $query .= "                     ST1.SCHREGNO, ";
        $query .= "                     ST1.TAKESEMES, ";
        $query .= "                     (CASE WHEN ST1.SEM1_INTR_SCORE IS NOT NULL THEN 1 ELSE 0 END) + (CASE WHEN ST1.SEM1_TERM_SCORE IS NOT NULL THEN 1 ELSE 0 END) AS COUNT1, ";
        $query .= "                     (CASE WHEN ST1.SEM2_INTR_SCORE IS NOT NULL THEN 1 ELSE 0 END) + (CASE WHEN ST1.SEM2_TERM_SCORE IS NOT NULL THEN 1 ELSE 0 END) AS COUNT2 ";
        $query .= "                 FROM ";
        $query .= "                     record_dat ST1 ";
        $query .= "                 WHERE ";
        $query .= "                     ST1.year = '".CTRL_YEAR."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                     ST1.CLASSCD          = '".$substr_classcd."' AND ";
            $query .= "                     ST1.SCHOOL_KIND      = '".$substr_school_kind."' AND ";
            $query .= "                     ST1.CURRICULUM_CD    = '".$substr_curriculum_cd."' AND ";
            $query .= "                     ST1.SUBCLASSCD       = '".$substr_subclasscd."' ";
        } else {
            $query .= "                     ST1.subclasscd = '".$model->field["SUBCLASSCD"]."' ";
        }
#        $query .= "                     ST1.chaircd = '".$model->field["CHAIRCD"]."' ";
        $query .= "     ) T3 ";
        $query .= "             WHERE ";
        $query .= "                 T1.year = '".CTRL_YEAR."' AND ";
        $query .= "                 T1.year = T2.YEAR AND ";
        $query .= "                 T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "                 T1.SCHREGNO = T3.SCHREGNO AND ";
        $query .= "                 T1.TAKESEMES = T3.TAKESEMES AND ";
        $query .= "                 T2.SEMESTER = '".CTRL_SEMESTER."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                 T1.CLASSCD          = '".$substr_classcd."' AND ";
            $query .= "                 T1.SCHOOL_KIND      = '".$substr_school_kind."' AND ";
            $query .= "                 T1.CURRICULUM_CD    = '".$substr_curriculum_cd."' AND ";
            $query .= "                 T1.SUBCLASSCD       = '".$substr_subclasscd."' ";
        } else {
            $query .= "                 T1.subclasscd = '".$model->field["SUBCLASSCD"]."' ";
        }
#        $query .= "                 T1.chaircd = '".$model->field["CHAIRCD"]."' ";
        $query .= "             ) T1 LEFT OUTER JOIN RELATIVEASSESS_MST T2  ";
        $query .= "                 ON T1.GRADE = T2.GRADE AND  ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                 T1.CLASSCD       = T2.CLASSCD  AND ";
            $query .= "                 T1.SCHOOL_KIND   = T2.SCHOOL_KIND  AND ";
            $query .= "                 T1.CURRICULUM_CD = T2.CURRICULUM_CD  AND ";
        }
        $query .= "                 T1.SUBCLASSCD = T2.SUBCLASSCD AND  ";
        $query .= "                 T2.ASSESSCD = '3' AND  ";
        $query .= "                 T1.SCORE_AVG1 BETWEEN T2.ASSESSLOW AND T2.ASSESSHIGH ";
        $query .= "                 LEFT OUTER JOIN RELATIVEASSESS_MST T3  ";
        $query .= "                         ON T1.GRADE = T3.GRADE AND  ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                         T1.CLASSCD       = T3.CLASSCD  AND ";
            $query .= "                         T1.SCHOOL_KIND   = T3.SCHOOL_KIND  AND ";
            $query .= "                         T1.CURRICULUM_CD = T3.CURRICULUM_CD  AND ";
        }
        $query .= "                         T1.SUBCLASSCD = T3.SUBCLASSCD AND  ";
        $query .= "                         T3.ASSESSCD = '3' AND  ";
        $query .= "                         T1.SCORE_AVG2 BETWEEN T3.ASSESSLOW AND T3.ASSESSHIGH ";
        $query .= "                 LEFT OUTER JOIN RELATIVEASSESS_MST T4  ";
        $query .= "                         ON T1.GRADE = T4.GRADE AND  ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                         T1.CLASSCD       = T4.CLASSCD  AND ";
            $query .= "                         T1.SCHOOL_KIND   = T4.SCHOOL_KIND  AND ";
            $query .= "                         T1.CURRICULUM_CD = T4.CURRICULUM_CD  AND ";
        }
        $query .= "                         T1.SUBCLASSCD = T4.SUBCLASSCD AND  ";
        $query .= "                         T4.ASSESSCD = '3' AND  ";
        $query .= "                         T1.SCORE_AVG3 BETWEEN T4.ASSESSLOW AND T4.ASSESSHIGH ";
        $query .= "         ) TT2 ";
        $query .= "     WHERE ";
        $query .= "         TT1.SCHREGNO = TT2.SCHREGNO AND ";
        $query .= "         TT1.TAKESEMES = TT2.TAKESEMES  ";
        $query .= " ) ";
        $query .= " WHERE ";
        $query .= "     TT1.year = '".CTRL_YEAR."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     TT1.CLASSCD          = '".$substr_classcd."' AND ";
            $query .= "     TT1.SCHOOL_KIND      = '".$substr_school_kind."' AND ";
            $query .= "     TT1.CURRICULUM_CD    = '".$substr_curriculum_cd."' AND ";
            $query .= "     TT1.SUBCLASSCD       = '".$substr_subclasscd."' ";
        } else {
            $query .= "     TT1.subclasscd = '".$model->field["SUBCLASSCD"]."' ";
        }
#        $query .= "     TT1.chaircd = '".$model->field["CHAIRCD"]."' ";

        $db->query($query);
        $db->commit();

        Query::dbCheckIn($db);
    }

    //テーブルRECORD_PROV_FLG_DATのレコードを削除
    public function delRecordProvFlgDat($year, $subclass, $schno, $model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd         = substr($subclass, 0, 2);
            $substr_school_kind     = substr($subclass, 3, 1);
            $substr_curriculum_cd   = substr($subclass, 5, 1);
            $substr_subclasscd      = substr($subclass, 7, 6);
        }
        $query  = "";
        $query .= "DELETE ";
        $query .= "FROM   RECORD_PROV_FLG_DAT ";
        $query .= "WHERE  YEAR          = '{$year}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "  AND  CLASSCD       = '{$substr_classcd}' ";
            $query .= "  AND  SCHOOL_KIND   = '{$substr_school_kind}' ";
            $query .= "  AND  CURRICULUM_CD = '{$substr_curriculum_cd}' ";
            $query .= "  AND  SUBCLASSCD    = '{$substr_subclasscd}' ";
        } else {
            $query .= "  AND  SUBCLASSCD    = '{$subclass}' ";
        }
        $query .= "  AND  SCHREGNO      = '{$schno}' ";
        return $query;
    }

    //RECORD_PROV_FLG_DATのレコードを追加
    public function insRecordProvFlgDat($year, $subclass, $schno, $model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd         = substr($subclass, 0, 2);
            $substr_school_kind     = substr($subclass, 3, 1);
            $substr_curriculum_cd   = substr($subclass, 5, 1);
            $substr_subclasscd      = substr($subclass, 7, 6);
        }
        $data = array();
        $data["YEAR"][TEXT]         = $year;
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["CLASSCD"][TEXT]       = $substr_classcd;
            $data["SCHOOL_KIND"][TEXT]   = $substr_school_kind;
            $data["CURRICULUM_CD"][TEXT] = $substr_curriculum_cd;
            $data["SUBCLASSCD"][TEXT]    = $substr_subclasscd;
        } else {
            $data["SUBCLASSCD"][TEXT]    = $subclass;
        }
        $data["SCHREGNO"][TEXT]     = $schno;
        //評定フラグ・・・仮評定(1)、本評定(NULL)
        $data["PROV_FLG"][TEXT]     = "1";
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "SYSDATE()";

        $query = Query::insertSQL($data, "RECORD_PROV_FLG_DAT");
        return $query;
    }

    // NO001 "成績入力完了"処理 追加修正 //

    //テーブルSchChrTestのEXECUTEDデータ更新SQL
    public function updateExecuted($model)
    {
        $db = Query::dbCheckOut();

        $chkDbValue = array( "1" => array("TESTKINDCD" => "01", "TESTITEMCD" => "01","SEMETER" => "1") ,
                             "2" => array("TESTKINDCD" => "02", "TESTITEMCD" => "01","SEMETER" => "1") ,
                             "3" => array("TESTKINDCD" => "01", "TESTITEMCD" => "01","SEMETER" => "2") ,
                             "4" => array("TESTKINDCD" => "02", "TESTITEMCD" => "01","SEMETER" => "2") );

        //管理者コントロール
        $query = knjd120oquery::selectContolCodeQuery($model);
        $results = $db->query($query);
        while ($row_ct = $results->fetchRow(DB_FETCHMODE_ASSOC)) {
            $admin_key[$row_ct["CONTROL_CODE"]]=$row_ct["CONTROL_CODE"];
        }
        $results->free();

        //disable flag
        $ableflg[1] = (isset($admin_key["0111"]))? "" : "disabled" ;
        $ableflg[2] = (isset($admin_key["0121"]))? "" : "disabled" ;
        $ableflg[3] = (isset($admin_key["0211"]))? "" : "disabled" ;
        $ableflg[4] = (isset($admin_key["0221"]))? "" : "disabled" ;

        for ($i=1; $i<=4; $i++) {
            if ($ableflg[$i] == "disabled") {
                continue;
            }

            $chkfg = ($model->field["CHK_COMP".$i] == "on")? "1" : "0" ;

            $data = array();
            $data["EXECUTED"][TEXT]     = $chkfg ;       //成績入力区分
            $data["REGISTERCD"][TEXT]   = STAFFCD ;      //登録者コード
            $data["UPDATED"][FUNC]      = "sysdate()";   //更新日付

            $where  = "";
            $where  = " WHERE YEAR       = '".CTRL_YEAR."' AND ";
            $where .= "       TESTKINDCD = '".$chkDbValue[$i]["TESTKINDCD"]."' AND ";
            $where .= "       TESTITEMCD = '".$chkDbValue[$i]["TESTITEMCD"]."' AND ";
            $where .= "       CHAIRCD    = '".$model->field["CHAIRCD"]."' AND ";
            $where .= "       SEMESTER   = '".$chkDbValue[$i]["SEMETER"]."' ";

            $query = "";
            $query = Query::updateSQL($data, "SCH_CHR_TEST", $where);

            $db->query($query);
        }

        Query::dbCheckIn($db);
    }

    public function updateExecuted2($model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd          = substr($model->field["SUBCLASSCD"], 0, 2);
            $substr_school_kind      = substr($model->field["SUBCLASSCD"], 3, 1);
            $substr_curriculum_cd    = substr($model->field["SUBCLASSCD"], 5, 1);
            $substr_subclasscd       = substr($model->field["SUBCLASSCD"], 7, 6);
        }
        $db = Query::dbCheckOut();
        $testcdArray = array("0182" => "19900"
                            ,"0282" => "29900"
                            ,"0882" => "99900");

        //管理者コントロール
        $query = knjd120oQuery::selectContolCodeQuery($model);
        $results = $db->query($query);
        while ($row_ct = $results->fetchRow(DB_FETCHMODE_ASSOC)) {
            $admin_key[$row_ct["CONTROL_CODE"]] = $row_ct["CONTROL_CODE"];
        }
        $results->free();
        //初期化
        $j = 1;
        foreach ($testcdArray as $testkey => $testval) {
            $query = knjd120oQuery::getRecordChkfinDat($model, $testval);
            $resultRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if (is_array($resultRow) && isset($admin_key[$testkey])) {
                $chkfg = ($model->field["CHK_COMP_VALUE".$j] == 'on') ? '1' : '0';

                $data = array();
                $data["EXECUTED"][TEXT]   = $chkfg ;     //成績入力区分
                $data["REGISTERCD"][TEXT] = STAFFCD ;    //登録者コード
                $data["UPDATED"][FUNC]    = "sysdate()"; //更新日付

                $where  = " WHERE ";
                $where .= "         YEAR = '".CTRL_YEAR."' ";
                $where .= "     AND SEMESTER || TESTKINDCD || TESTITEMCD = '{$testval}' ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $where .= "     AND CLASSCD          = '".$substr_classcd."' ";
                    $where .= "     AND SCHOOL_KIND      = '".$substr_school_kind."' ";
                    $where .= "     AND CURRICULUM_CD    = '".$substr_curriculum_cd."' ";
                    $where .= "     AND SUBCLASSCD       = '".$substr_subclasscd."' ";
                } else {
                    $where .= "     AND SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
                }
                $where .= "     AND CHAIRCD    = '{$model->field["CHAIRCD"]}' ";
                $where .= "     AND RECORD_DIV = '2' ";

                $query = Query::updateSQL($data, "RECORD_CHKFIN_DAT", $where);

                $db->query($query);
            }
            $j++;
        }
        Query::dbCheckIn($db);
    }

    //テーブルSchChrTestのデータ抽出SQL
    public function getSchChrTestData($charcd)
    {
        $query = "
            SELECT SEMESTER || TESTKINDCD || TESTITEMCD as TESTKIND, EXECUTED FROM SCH_CHR_TEST
               WHERE CHAIRCD = '".$charcd."'
               AND YEAR = '".CTRL_YEAR."'
               AND ((TESTKINDCD = '01' AND TESTITEMCD = '01') OR (TESTKINDCD = '02' AND TESTITEMCD = '01') )
        ";

        return $query;
    }

    // NO001 "成績入力完了"処理 おわり //

    //未入力チェックのテーブルにレコードがあるのか
    public function getRecordChkfinDat($model, $testcd)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd          = substr($model->field["SUBCLASSCD"], 0, 2);
            $substr_school_kind      = substr($model->field["SUBCLASSCD"], 3, 1);
            $substr_curriculum_cd    = substr($model->field["SUBCLASSCD"], 5, 1);
            $substr_subclasscd       = substr($model->field["SUBCLASSCD"], 7, 6);
        }
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     RECORD_CHKFIN_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR       = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER || TESTKINDCD || TESTITEMCD = '{$testcd}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND CLASSCD          = '".$substr_classcd."' ";
            $query .= "     AND SCHOOL_KIND      = '".$substr_school_kind."' ";
            $query .= "     AND CURRICULUM_CD    = '".$substr_curriculum_cd."' ";
            $query .= "     AND SUBCLASSCD       = '".$substr_subclasscd."' ";
        } else {
            $query .= "     AND SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= "     AND CHAIRCD    = '{$model->field["CHAIRCD"]}' ";
        $query .= "     AND RECORD_DIV = '2' ";

        return $query;
    }
}
