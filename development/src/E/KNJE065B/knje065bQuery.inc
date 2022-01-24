<?php

require_once('for_php7.php');
class knje065bQuery extends Query
{

    //Z010
    public function getZ010()
    {
        $query  = "SELECT NAME1, NAMESPARE1, NAMESPARE2 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' ";
        return $query;
    }

    //学籍在籍データの存在確認
    public function getSchSchregno()
    {
        $query  = "SELECT COUNT(schregno) FROM schreg_regd_dat ";
        $query .= " WHERE year     = '".CTRL_YEAR."'";
        $query .= "   AND semester = '".CTRL_SEMESTER."'";
        return $query;
    }
    //評定マスタの存在確認
    public function getAssesscd($model)
    {
        $table = ($model->Properties["useAssessCourseMst"] == "1") ? "ASSESS_COURSE_MST" : "assess_mst";

        $query  = " SELECT COUNT(assesscd) FROM ".$table;
        $query .= " WHERE assesscd = '4' ";
        return $query;
    }

    //コンボボックスの中
    public function getGradeQuery($model)
    {
        $query  = " SELECT DISTINCT REGD.grade, REG_G.GRADE_NAME1  ";
        $query .= " FROM schreg_regd_hdat REGD ";
        $query .= " LEFT JOIN SCHREG_REGD_GDAT REG_G ON REGD.YEAR = REG_G.YEAR ";
        $query .= "           AND REGD.GRADE = REG_G.GRADE ";
        $query .= " WHERE REGD.year     = '".CTRL_YEAR."'";
        $query .= "   AND REGD.semester = '".CTRL_SEMESTER."'";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "           AND REG_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "           AND REG_G.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY REGD.GRADE";

        return $query;
    }

    //表示用
    public function readQuery($model)
    {
        if ($model->Properties["gaihyouGakkaBetu"] === '1' || $model->Properties["gaihyouGakkaBetu"] === '2') {
            if ($model->Properties["gaihyouGakkaBetu"] == '1') {
                $query  = " SELECT  ";
                $query .= "     T1.coursecd, ";
                $query .= "     T1.majorcd, ";
                $query .= "     (W2.coursename || W2.majorname) AS name, ";
                $query .= "     T1.coursecode, W3.coursecodename, ";
                $query .= "       W1.a_member, W1.b_member, W1.c_member, W1.d_member, W1.e_member, ";
                $query .= "       W1.course_member, W1.grade_member ";
                $query .= "  FROM (SELECT distinct coursecd, majorcd, coursecode ";
                $query .= "          FROM schreg_regd_dat ";
                $query .= "         WHERE year = '".CTRL_YEAR."'";
                $query .= "           AND semester = '".CTRL_SEMESTER."'";
                $query .= "           AND grade = '".$model->gc_select."') T1 ";
                if ($model->fields["KINDCD"] != "") {
                    $query .= "       LEFT JOIN geneviewmbr_kind_dat W1 ";
                    $query .= "         ON W1.year = '".CTRL_YEAR."'";
                    $query .= "        AND W1.KINDCD = '".$model->fields["KINDCD"]."' ";
                } else {
                    $query .= "       LEFT JOIN geneviewmbr_dat W1 ";
                    $query .= "         ON W1.year = '".CTRL_YEAR."'";
                }
                $query .= "        AND T1.coursecd = W1.coursecd ";
                $query .= "        AND T1.majorcd = W1.majorcd ";
                $query .= "        AND T1.coursecode = W1.coursecode ";
                $query .= "        AND W1.grade = '".$model->gc_select."'";
                $query .= "       LEFT JOIN v_course_major_mst W2 ";
                $query .= "         ON T1.coursecd = W2.coursecd ";
                $query .= "        AND T1.majorcd = W2.majorcd ";
                $query .= "        AND W2.year = '".CTRL_YEAR."'";
                $query .= "       LEFT JOIN v_coursecode_mst W3 ";
                $query .= "         ON T1.coursecode = W3.coursecode ";
                $query .= "        AND W3.year = '".CTRL_YEAR."'";
                $query .= " ORDER BY T1.coursecd, T1.majorcd, T1.coursecode ";
            } else {
                $query  = " SELECT DISTINCT ";
                $query .= "     '0' AS COURSECD, ";
                $query .= "     '0000' AS COURSECODE, ";
                $query .= "     H1.GROUP_CD, ";
                $query .= "     H1.GROUP_NAME AS NAME, ";
                $query .= "     T1.a_member, T1.b_member, T1.c_member, T1.d_member, T1.e_member, ";
                $query .= "     T1.course_member, T1.grade_member ";
                $query .= " FROM COURSE_GROUP_CD_HDAT H1";
                if ($model->fields["KINDCD"] != "") {
                    $query .= " LEFT JOIN  GENEVIEWMBR_KIND_DAT T1 ";
                    $query .= "         ON T1.YEAR = H1.YEAR ";
                    $query .= "        AND T1.KINDCD = '".$model->fields["KINDCD"]."' ";
                } else {
                    $query .= " LEFT JOIN  GENEVIEWMBR_DAT T1 ";
                    $query .= "         ON T1.YEAR = H1.YEAR ";
                }
                $query .= "        AND T1.GRADE = H1.GRADE ";
                $query .= "        AND T1.MAJORCD = H1.GROUP_CD ";
                $query .= "        AND T1.COURSECD = '0' ";
                $query .= "        AND T1.COURSECODE = '0000' ";
                $query .= "     WHERE  ";
                $query .= "            H1.YEAR = '".CTRL_YEAR."'";
                $query .= "        AND H1.GRADE = '".$model->gc_select."'";
                $query .= " ORDER BY H1.GROUP_CD ";
            }
        } else {
            $query  = "SELECT T1.coursecd, T1.majorcd, ";
            $query .= "       (W2.coursename || W2.majorname) AS name, ";
            $query .= "       '0000' as coursecode, ";
            $query .= "       W1.a_member, W1.b_member, W1.c_member, W1.d_member, W1.e_member, ";
            $query .= "       W1.course_member, W1.grade_member ";
            $query .= "  FROM (SELECT distinct coursecd, majorcd ";
            $query .= "          FROM schreg_regd_dat ";
            $query .= "         WHERE year = '".CTRL_YEAR."'";
            $query .= "           AND semester = '".CTRL_SEMESTER."'";
            $query .= "           AND grade = '".$model->gc_select."') T1 ";
            $query .= "       LEFT JOIN ";
            $query .= "                    (SELECT ";
            $query .= "                         YEAR, ";
            $query .= "                         COURSECD, ";
            $query .= "                         MAJORCD, ";
            $query .= "                         GRADE, ";
            $query .= "                         SUM(A_MEMBER) AS A_MEMBER, ";
            $query .= "                         SUM(B_MEMBER) AS B_MEMBER, ";
            $query .= "                         SUM(C_MEMBER) AS C_MEMBER, ";
            $query .= "                         SUM(D_MEMBER) AS D_MEMBER, ";
            $query .= "                         SUM(E_MEMBER) AS E_MEMBER, ";
            $query .= "                         SUM(COURSE_MEMBER) AS COURSE_MEMBER, ";
            $query .= "                         SUM(GRADE_MEMBER) AS GRADE_MEMBER ";
            $query .= "                     FROM ";
            if ($model->fields["KINDCD"] != "") {
                $query .= "                         GENEVIEWMBR_KIND_DAT ";
                $query .= "                 WHERE ";
                $query .= "                         KINDCD = '".$model->fields["KINDCD"]."' ";
            } else {
                $query .= "                         GENEVIEWMBR_DAT ";
            }
            $query .= "                     GROUP BY ";
            $query .= "                         YEAR, ";
            $query .= "                         COURSECD, ";
            $query .= "                         MAJORCD, ";
            $query .= "                         GRADE) W1 ";
            $query .= "         ON T1.coursecd = W1.coursecd ";
            $query .= "        AND T1.majorcd = W1.majorcd ";
            $query .= "        AND W1.year = '".CTRL_YEAR."'";
            $query .= "        AND W1.grade = '".$model->gc_select."'";
            $query .= "       LEFT JOIN v_course_major_mst W2 ";
            $query .= "         ON T1.coursecd = W2.coursecd ";
            $query .= "        AND T1.majorcd = W2.majorcd ";
            $query .= "        AND W2.year = '".CTRL_YEAR."'";
            $query .= " ORDER BY T1.coursecd, T1.majorcd ";
        }

        return $query;
    }

    //再計算
    public function &getRecalculateQuery($model, $dataFlg = "")
    {
        $valuation = "T1.valuation";
        //評定1の場合は2で処理する
        if ($model->hyoteiYomikae == "1") {
            //仮評定フラグ
            if ($model->Properties["useProvFlg"] == '1') {
                $valuation = "case when T1.valuation = 1 AND T1.PROV_FLG = '1' then 2 else T1.valuation end";
            } else {
                $valuation = "case when T1.valuation = 1 then 2 else T1.valuation end";
            }
        }
        //同一グループ科目設定の処理にて使用
        $valuationCase  = "case when 0 < T1.valuation then T1.valuation end";
        $creditCase     = "case when 0 < T1.valuation then T1.CREDIT end";

        $query  = "";
        if ($model->Properties["useKNJE065B_grdCreditSansyo"] == '1') {
            $query .= knje065bQuery::getCreditWithText($model);
            $query .= ", ";
        } else {
            $query .= " WITH ";
        }
        $query .= " REGD_T AS ( ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."'";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER."'";
        $query .= "     AND GRADE = '".$model->gc_select."' ";
        $query .= " ), REGD_YEAR AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.ANNUAL, ";
        $query .= "     MAX(T1.YEAR) AS YEAR ";
        $query .= " FROM ";
        $query .= "     SCHREG_STUDYREC_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     EXISTS( ";
        $query .= "         SELECT ";
        $query .= "             'x' ";
        $query .= "         FROM ";
        $query .= "             REGD_T ";
        $query .= "         WHERE ";
        $query .= "             T1.SCHREGNO = REGD_T.SCHREGNO ";
        $query .= "     ) ";
        $query .= "     AND T1.SCHOOL_KIND = ( SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '".CTRL_YEAR."' AND GRADE = '".$model->gc_select."' ) ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.ANNUAL ";
        $query .= " ), SUBCLASS_GROUP AS ( ";
        $query .= "     SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.CLASSCD, ";
            $query .= "     T2.SCHOOL_KIND, ";
            $query .= "     T2.CURRICULUM_CD, ";
        }
        $query .= "         T2.SUBCLASSCD, ";
        $query .= "         T2.SUBCLASSCD2 ";
        $query .= "     FROM ";
        $query .= "         SUBCLASS_MST T2, ";
        $query .= "         SUBCLASS_MST T3 ";
        $query .= "     WHERE ";
        $query .= "         T2.SUBCLASSCD2 IS NOT NULL AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.CLASSCD = T3.CLASSCD AND ";
            $query .= "     T2.SCHOOL_KIND = T3.SCHOOL_KIND AND ";
            $query .= "     T2.CURRICULUM_CD = T3.CURRICULUM_CD AND ";
        }
        $query .= "         T2.SUBCLASSCD2 = T3.SUBCLASSCD ";
        $query .= " ), T_SCHREG_STUDYREC AS ( ";
        $query .= "     SELECT T1.YEAR,T1.SCHREGNO,T1.CLASSCD,T1.VALUATION ";
        if ($model->Properties["useProvFlg"] == '1') {
            $query .= "           ,L3.PROV_FLG ";
        } else {
            $query .= "           ,CAST(NULL AS VARCHAR(1)) AS PROV_FLG ";
        }
        $query .= "           ,CASE WHEN T1.ADD_CREDIT IS NOT NULL ";
        $query .= "                 THEN VALUE(T1.GET_CREDIT,0) + VALUE(T1.ADD_CREDIT,0) ";
        $query .= "                 ELSE T1.GET_CREDIT END AS CREDIT ";
        $query .= "           ,L1.GVAL_CALC ";
        $query .= "           ,CASE WHEN L2.SUBCLASSCD2 IS NOT NULL THEN L2.SUBCLASSCD2 ";
        $query .= "                 ELSE T1.SUBCLASSCD END AS SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "       ,T1.SCHOOL_KIND ";
            $query .= "       ,T1.CURRICULUM_CD ";
        }
        $query .= "     FROM SCHREG_STUDYREC_DAT T1 ";
        $query .= "          LEFT JOIN SCHOOL_MST L1 ON L1.YEAR = T1.YEAR ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND L1.SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= "     AND L1.SCHOOL_KIND in (SELECT ";
            $query .= "                             SCHOOL_KIND ";
            $query .= "                         FROM ";
            $query .= "                             SCHREG_REGD_GDAT ";
            $query .= "                         WHERE ";
            $query .= "                                 YEAR  = '".CTRL_YEAR."' ";
            $query .= "                             AND GRADE = '{$model->gc_select}') ";
        }
        $query .= "          LEFT JOIN SUBCLASS_GROUP L2 ON L2.SUBCLASSCD = T1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                 AND L2.CLASSCD = T1.CLASSCD ";
            $query .= "                                 AND L2.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "                                 AND L2.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        //仮評定フラグ
        if ($model->Properties["useProvFlg"] == '1') {
            $query .= "     LEFT JOIN STUDYREC_PROV_FLG_DAT L3 ";
            $query .= "            ON  L3.SCHOOLCD = T1.SCHOOLCD ";
            $query .= "            AND L3.YEAR = T1.YEAR ";
            $query .= "            AND L3.SCHREGNO = T1.SCHREGNO ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "        AND L3.CLASSCD = T1.CLASSCD ";
                $query .= "        AND L3.SCHOOL_KIND = T1.SCHOOL_KIND ";
                $query .= "        AND L3.CURRICULUM_CD = T1.CURRICULUM_CD ";
            }
            $query .= "            AND L3.SUBCLASSCD = T1.SUBCLASSCD ";
        }
        $query .= "     WHERE ";
        if ($model->z010["NAME1"] == "jisyukan") {
            $query .= "            (T1.CLASSCD BETWEEN '01' AND '89' OR T1.SUBCLASSCD = '941001')";
        } elseif ($model->z010["NAME1"] == "HOUSEI") {
            $query .= "            T1.CLASSCD BETWEEN '01' AND '85' ";
        } else {
            $query .= "            T1.CLASSCD BETWEEN '01' AND '89' ";
        }
        $query .= "            AND T1.VALUATION <> 0 ";
        if ($model->Properties["knje065bNotIncludeShutoku"] == "1") {
            $query .= "            AND VALUE(T1.GET_CREDIT, 0) + VALUE(T1.ADD_CREDIT, 0) <> 0 ";
        }
        $query .= "            AND EXISTS ( ";
        $query .= "                        SELECT 'X' ";
        $query .= "                        FROM REGD_YEAR E1 ";
        $query .= "                        WHERE T1.SCHREGNO = E1.SCHREGNO ";
        $query .= "                          AND T1.YEAR = E1.YEAR ";
        $query .= "                       ) ";
        if ($model->electdiv == "on") { // 選択科目を除く
            $query .= "            AND NOT EXISTS(SELECT 'X' FROM CLASS_MST T2 ";
            $query .= "                            WHERE T2.ELECTDIV='1' ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                          AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
            }
            $query .= "                              AND T2.CLASSCD=T1.CLASSCD) ";
            $query .= "            AND NOT EXISTS(SELECT 'X' FROM SUBCLASS_MST T3 ";
            $query .= "                            WHERE T3.ELECTDIV='1' ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                          AND T3.CLASSCD = T1.CLASSCD ";
                $query .= "                          AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ";
                $query .= "                          AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ";
            }
            $query .= "                              AND T3.SUBCLASSCD=T1.SUBCLASSCD) ";
        }
        $query .= " ), STUDYREC AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.YEAR,T1.SCHREGNO,T1.CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD,T1.GVAL_CALC,T1.VALUATION,T1.PROV_FLG,T1.CREDIT ";
        $query .= "     FROM ";
        $query .= "         T_SCHREG_STUDYREC T1 ";
        $query .= "     WHERE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD || '-' ||  ";
            $query .= "     T1.SCHOOL_KIND || '-' ||  ";
            $query .= "     T1.CURRICULUM_CD || '-' ||  ";
        }
        $query .= "         T1.SUBCLASSCD NOT IN (SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         W2.CLASSCD || '-' ||  ";
            $query .= "         W2.SCHOOL_KIND || '-' ||  ";
            $query .= "         W2.CURRICULUM_CD || '-' ||  ";
        }
        $query .= "             W2.SUBCLASSCD2 FROM SUBCLASS_GROUP W2) ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT ";
        $query .= "         T1.YEAR,T1.SCHREGNO,T1.CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD,T1.GVAL_CALC ";
        $query .= "        ,case when COUNT(*) = 1 then MAX(T1.VALUATION) ";
        $query .= "              when T1.GVAL_CALC = '0' then ROUND(AVG(FLOAT(".$valuationCase.")),0) ";
        $query .= "              when T1.GVAL_CALC = '1' and 0 < SUM(".$creditCase.") then ROUND(FLOAT(SUM((".$valuationCase.")*T1.CREDIT))/SUM(".$creditCase."),0) ";
        $query .= "              else MAX(T1.VALUATION) end AS VALUATION ";
        $query .= "        ,MIN(VALUE(T1.PROV_FLG, '0')) AS PROV_FLG ";
        $query .= "        ,SUM(VALUE(T1.CREDIT, '0')) AS CREDIT ";
        $query .= "     FROM ";
        $query .= "         T_SCHREG_STUDYREC T1 ";
        $query .= "     WHERE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD || '-' ||  ";
            $query .= "     T1.SCHOOL_KIND || '-' ||  ";
            $query .= "     T1.CURRICULUM_CD || '-' ||  ";
        }
        $query .= "         T1.SUBCLASSCD IN (SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         W2.CLASSCD || '-' ||  ";
            $query .= "         W2.SCHOOL_KIND || '-' ||  ";
            $query .= "         W2.CURRICULUM_CD || '-' ||  ";
        }
        $query .= "             W2.SUBCLASSCD2 FROM SUBCLASS_GROUP W2) ";
        $query .= "     GROUP BY ";
        $query .= "         T1.YEAR,T1.SCHREGNO,T1.CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD,T1.GVAL_CALC ";
        $query .= " ), TARGET_REGD AS ( ";
        $query .= "  SELECT distinct year, schregno, grade, coursecd, majorcd ";
        if ($model->Properties["gaihyouGakkaBetu"] === '1' || $model->Properties["gaihyouGakkaBetu"] === '2') {
            $query .= "  , coursecode ";
        }
        $query .= "          FROM schreg_regd_dat w1";
        $query .= "         WHERE year     = '".CTRL_YEAR."'";
        $query .= "           AND semester = '".CTRL_SEMESTER."'";
        $query .= "           AND grade    = '".$model->gc_select."' ";
        $query .= "           AND NOT EXISTS ( ";
        $query .= "                           SELECT 'X' ";
        $query .= "                           FROM schreg_base_mst w2 ";
        $query .= "                           WHERE w2.schregno = w1.schregno ";
        $query .= "                             AND w2.grd_div in ('2','3','6','7') "; //2:退学 3:転学 6:除籍 7:転籍 者は除く
        $query .= "                             AND w2.grd_date < '".str_replace("/", "-", $model->base_date)."' ";
        $query .= "                          ) ";
        //習得単位集計が卒業履修単位数以上の人  KNJD233Vと同じ条件
        if ($model->Properties["useKNJE065B_grdCreditSansyo"] == '1') {
            $query .= "       AND EXISTS (SELECT ";
            $query .= "                       'X' ";
            $query .= "                   FROM ";
            $query .= "                       CREDIT_DATA CRDT ";
            $query .= "                   WHERE ";
            $query .= "                           CRDT.schregno = w1.schregno ";
            $query .= "                       AND CRDT.TOTAL_CREDITS >= {$model->gradCompCredits} ";
            $query .= "                   ) ";
        }
        $query .= " ), HYOTEI_HEIKIN AS ( ";
        $query .= "       SELECT W1.schregno, W1.avg, W2.assesslevel, w1.count, w1.sum, w1.credit_sum ";
        $query .= "       FROM ";
        $query .= "              (SELECT ";
        $query .= "                T1.schregno, ";
        if ($model->fields["KINDCD"] == "01") {
            // 加重平均
            $query .= "            CASE WHEN SUM(T1.CREDIT) > 0 THEN  ";
            $query .= "                floor(decimal(SUM(".$valuation." * T1.CREDIT)) / SUM(T1.CREDIT) * 10) / 10 "; // 小数点第2位切捨
            $query .= "            END AS avg, ";
            $query .= "            COUNT(".$valuation." * T1.CREDIT) AS count, ";
            $query .= "            SUM(".$valuation." * T1.CREDIT) AS sum, ";
            $query .= "            SUM(T1.CREDIT) AS credit_sum ";
        } else {
            // 単純平均
            $query .= "            round(AVG(decimal(".$valuation.")),1)  AS avg, ";
            $query .= "            COUNT(".$valuation.") AS count, ";
            $query .= "            SUM(".$valuation.") AS sum, ";
            $query .= "            SUM(T1.CREDIT) AS credit_sum ";
        }
        $query .= "               FROM STUDYREC T1 ";
        $query .= "                GROUP BY T1.schregno) W1,";
        if ($model->Properties["useAssessCourseMst"] == "1") {
            $query .= "              (  SELECT T1.SCHREGNO, T2.* ";
            $query .= "                 FROM REGD_T T1, ASSESS_COURSE_MST T2 ";
            $query .= "                 WHERE T2.assesscd   = '4' AND ";
            $query .= "                       T1.COURSECD   = T2.COURSECD AND ";
            $query .= "                       T1.MAJORCD    = T2.MAJORCD AND ";
            $query .= "                       T1.COURSECODE = T2.COURSECODE ) W2 ";
            $query .= "        where W1.avg between W2.assesslow AND W2.assesshigh AND ";
            $query .= "              W1.SCHREGNO = W2.SCHREGNO ";
        } else {
            $query .= "              (SELECT * ";
            $query .= "                 FROM assess_mst ";
            $query .= "                WHERE assesscd = '4' ) W2 ";
            $query .= "        where W1.avg between W2.assesslow AND W2.assesshigh ";
        }
        $query .= " ) ";

        if ($dataFlg == "RANK") {
            $query .= "SELECT  ";
            $query .= "     HH.SCHREGNO, ";
            if ($model->fields["KINDCD"] == "01") {
                // 多重平均
                $query .= "     DECIMAL(HH.AVG, 5, 1) AS AVG, ";
            } else {
                // 単純平均
                $query .= "     DECIMAL(ROUND(HH.AVG, 0), 1) AS AVG, ";
            }
            $query .= "     HH.COUNT, ";
            $query .= "     INT(HH.SUM) AS SUM, ";
            $query .= "     HH.CREDIT_SUM, ";
            if ($model->fields["KINDCD"] == "01") {
                $query .= "     RANK() OVER(ORDER BY DECIMAL(HH.AVG, 5, 1) DESC) AS RANK ";
            } else {
                $query .= "     RANK() OVER(ORDER BY DECIMAL(ROUND(HH.AVG, 0), 1) DESC) AS RANK ";
            }
            $query .= " FROM ";
            $query .= "     TARGET_REGD T1 ";
            $query .= "     INNER JOIN HYOTEI_HEIKIN HH ON HH.SCHREGNO = T1.SCHREGNO ";
            if ($model->Properties["gaihyouGakkaBetu"] == '1') {
                $query .= " INNER JOIN V_COURSE_MAJOR_MST CMM ";
                $query .= "            ON CMM.COURSECD = T1.COURSECD ";
                $query .= "           AND CMM.MAJORCD  = T1.MAJORCD ";
                $query .= "           AND CMM.YEAR     = T1.YEAR ";
                $query .= " INNER JOIN V_COURSECODE_MST CCM ";
                $query .= "            ON CCM.COURSECODE = T1.COURSECODE ";
                $query .= "           AND CCM.YEAR       = T1.YEAR ";
            } elseif ($model->Properties["gaihyouGakkaBetu"] == '2') {
                $query .= " INNER JOIN COURSE_GROUP_CD_DAT CG ";
                $query .= "         ON CG.YEAR = T1.YEAR ";
                $query .= "        AND CG.GRADE = T1.GRADE ";
                $query .= "        AND CG.COURSECD = T1.COURSECD ";
                $query .= "        AND CG.MAJORCD = T1.MAJORCD ";
                $query .= "        AND CG.COURSECODE = T1.COURSECODE ";
                $query .= " INNER JOIN COURSE_GROUP_CD_HDAT CGH ";
                $query .= "         ON CG.YEAR = CGH.YEAR ";
                $query .= "        AND CG.GRADE = CGH.GRADE ";
                $query .= "        AND CG.GROUP_CD = CGH.GROUP_CD ";
            } else {
                $query .= " INNER JOIN V_COURSE_MAJOR_MST CMM ";
                $query .= "         ON CMM.COURSECD = T1.COURSECD ";
                $query .= "        AND CMM.MAJORCD  = T1.MAJORCD ";
                $query .= "        AND CMM.YEAR     = T1.YEAR ";
            }
            $query .= " WHERE ";
            $query .= "     HH.AVG IS NOT NULL";
        } else {
            $query .= "SELECT  ";
            if ($model->Properties["gaihyouGakkaBetu"] === '1') {
                $query .= "        T1.coursecd ";
                $query .= "       ,T1.majorcd ";
                $query .= "       ,(W3.coursename || W3.majorname) AS name ";
                $query .= "       ,T1.coursecode, W4.coursecodename ";
            } elseif ($model->Properties["gaihyouGakkaBetu"] === '2') {
                $query .= "        '0' as COURSECD ";
                $query .= "       ,'0000' as COURSECODE ";
                $query .= "       ,H1.GROUP_CD ";
                $query .= "       ,H1.GROUP_NAME AS NAME ";
            } else {
                $query .= "        T1.coursecd , T1.majorcd ";
                $query .= "       ,(W3.coursename || W3.majorname) AS name ";
                $query .= "       ,'0000' as coursecode ";
            }
            $query .= "       ,SUM(CASE WHEN t2.assesslevel=5 THEN 1 ELSE 0 END) AS A_MEMBER ";
            $query .= "       ,SUM(CASE WHEN t2.assesslevel=4 THEN 1 ELSE 0 END) AS B_MEMBER ";
            $query .= "       ,SUM(CASE WHEN t2.assesslevel=3 THEN 1 ELSE 0 END) AS C_MEMBER ";
            $query .= "       ,SUM(CASE WHEN t2.assesslevel=2 THEN 1 ELSE 0 END) AS D_MEMBER ";
            $query .= "       ,SUM(CASE WHEN t2.assesslevel=1 THEN 1 ELSE 0 END) AS E_MEMBER ";
            $query .= "  FROM TARGET_REGD T1 ";
            $query .= "          LEFT JOIN HYOTEI_HEIKIN T2 ON T1.schregno = T2.schregno ";
            if ($model->Properties["gaihyouGakkaBetu"] == '1') {
                $query .= "          LEFT JOIN v_course_major_mst W3 ";
                $query .= "            ON T1.coursecd = W3.coursecd ";
                $query .= "           AND T1.majorcd  = W3.majorcd ";
                $query .= "           AND T1.year     = W3.year ";
                $query .= "          LEFT JOIN v_coursecode_mst W4 ";
                $query .= "            ON T1.coursecode = W4.coursecode ";
                $query .= "           AND T1.year       = W4.year ";
                $query .= " GROUP BY T1.coursecd, T1.majorcd, ";
                $query .= "          W3.coursename, W3.majorname, T1.coursecode, W4.coursecodename";
                $query .= " ORDER BY T1.coursecd, T1.majorcd, T1.coursecode ";
            } elseif ($model->Properties["gaihyouGakkaBetu"] == '2') {
                $query .= "       LEFT JOIN COURSE_GROUP_CD_DAT H2 ";
                $query .= "         ON H2.YEAR = T1.YEAR ";
                $query .= "        AND H2.GRADE = T1.GRADE ";
                $query .= "        AND H2.COURSECD = T1.COURSECD ";
                $query .= "        AND H2.MAJORCD = T1.MAJORCD ";
                $query .= "        AND H2.COURSECODE = T1.COURSECODE ";
                $query .= "       INNER JOIN COURSE_GROUP_CD_HDAT H1 ";
                $query .= "         ON H1.YEAR = H2.YEAR ";
                $query .= "        AND H1.GRADE = H2.GRADE ";
                $query .= "        AND H1.GROUP_CD = H2.GROUP_CD ";
                $query .= " GROUP BY H1.GROUP_CD, H1.GROUP_NAME";
                $query .= " ORDER BY H1.GROUP_CD ";
            } else {
                $query .= "          LEFT JOIN v_course_major_mst W3 ";
                $query .= "            ON T1.coursecd = W3.coursecd ";
                $query .= "           AND T1.majorcd  = W3.majorcd ";
                $query .= "           AND T1.year     = W3.year ";
                $query .= " GROUP BY T1.coursecd, T1.majorcd, ";
                $query .= "          W3.coursename, W3.majorname ";
                $query .= " ORDER BY T1.coursecd, T1.majorcd ";
            }
        }

        return $query;
    }

    //学年人数合計
    public function &getAllCntQuery($model, $Row)
    {
        $query  = "";
        if ($model->Properties["useKNJE065B_grdCreditSansyo"] == '1') {
            $query .= knje065bQuery::getCreditWithText($model);
        }
        $query .= "SELECT COUNT(schregno) AS GRADE_MEMBER ";
        $query .= "  FROM schreg_regd_dat w1 ";
        $query .= " WHERE year     = '".CTRL_YEAR."'";
        $query .= "   AND semester = '".CTRL_SEMESTER."'";
        $query .= "   AND grade    = '".$model->gc_select."'";
        $query .= "   AND NOT EXISTS ( ";
        $query .= "                    SELECT 'X' ";
        $query .= "                    FROM schreg_base_mst w2 ";
        $query .= "                    WHERE w2.schregno = w1.schregno ";
        $query .= "                      AND w2.grd_div in ('2','3','6','7') "; //2:退学 3:転学 6:除籍 7:転籍 者は除く
        $query .= "                      AND w2.grd_date < '".str_replace("/", "-", $model->base_date)."' ";
        $query .= "                   ) ";
        if ($model->Properties["gaihyouGakkaBetu"] == '1' && $model->z010["NAME1"] == "sundaikoufu") {
            // 駿台甲府はコース別の場合、学年人数に学科人数をセット
            $query .= "   AND COURSECD = '".$Row["COURSECD"]."'";
            $query .= "   AND MAJORCD = '".$Row["MAJORCD"]."'";
        }
        //習得単位集計が卒業履修単位数以上の人  KNJD233Vと同じ条件
        if ($model->Properties["useKNJE065B_grdCreditSansyo"] == '1') {
            $query .= "   AND EXISTS (SELECT ";
            $query .= "                   'X' ";
            $query .= "               FROM ";
            $query .= "                   CREDIT_DATA CRDT ";
            $query .= "               WHERE ";
            $query .= "                       CRDT.schregno = w1.schregno ";
            $query .= "                   AND CRDT.TOTAL_CREDITS >= {$model->gradCompCredits} ";
            $query .= "               ) ";
        }

        return $query;
    }

    //With文セットSQL
    public function getCreditWithText($model)
    {
        $query  = " WITH SCH_INFO AS ( ";
        $query .= "         SELECT ";
        $query .= "             T1.SCHREGNO, ";
        $query .= "             L2.GRD_DATE, ";
        $query .= "             T1.GRADE, ";
        $query .= "             T1.COURSECD, ";
        $query .= "             T1.MAJORCD, ";
        $query .= "             T1.COURSECODE ";
        $query .= "         FROM ";
        $query .= "             SCHREG_REGD_DAT T1 ";
        $query .= "             LEFT JOIN SCHREG_REGD_HDAT L1 ON L1.YEAR     = T1.YEAR ";
        $query .= "                                          AND L1.SEMESTER = T1.SEMESTER ";
        $query .= "                                          AND L1.GRADE || L1.HR_CLASS = T1.GRADE || T1.HR_CLASS ";
        $query .= "             INNER JOIN SCHREG_BASE_MST L2 ON L2.SCHREGNO = T1.SCHREGNO ";
        $query .= "         WHERE ";
        $query .= "                 T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "             AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "             AND T1.GRADE    = '{$model->gc_select}' ";
        $query .= " ), SCH_INF_LAST_YEAR AS ( ";
        $query .= "         SELECT ";
        $query .= "             T1.SCHREGNO, ";
        $query .= "             T1.COURSECD, ";
        $query .= "             T1.MAJORCD, ";
        $query .= "             T1.COURSECODE, ";
        $query .= "             T1.GRADE, ";
        if ($model->schoolDiv == "0") {//学年制
            $query .= "             MAX(T1.YEAR) AS YEAR ";
        } else {
            $query .= "             T1.YEAR ";
        }
        $query .= "         FROM ";
        $query .= "             SCHREG_REGD_DAT T1 ";
        $query .= "         WHERE ";
        $query .= "                 T1.YEAR < '".CTRL_YEAR."' ";
        $query .= "             AND T1.SCHREGNO IN (SELECT ";
        $query .= "                                     SCHREGNO ";
        $query .= "                                 FROM ";
        $query .= "                                     SCH_INFO ";
        $query .= "                                 ) ";
        if ($model->schoolDiv == "0") {//学年制
            $query .= "             AND T1.GRADE IN (SELECT ";
            $query .= "                                  GRADE ";
            $query .= "                              FROM ";
            $query .= "                                  SCHREG_REGD_GDAT ";
            $query .= "                              WHERE ";
            $query .= "                                      YEAR  = '".CTRL_YEAR."' ";
            $query .= "                                  AND GRADE < '{$model->gc_select}' ";
            $query .= "                                  AND SCHOOL_KIND IN (SELECT ";
            $query .= "                                                          SCHOOL_KIND ";
            $query .= "                                                      FROM ";
            $query .= "                                                          SCHREG_REGD_GDAT ";
            $query .= "                                                      WHERE ";
            $query .= "                                                              YEAR  = '".CTRL_YEAR."' ";
            $query .= "                                                          AND GRADE = '{$model->gc_select}' ";
            $query .= "                                                      ) ";
            $query .= "                              ) ";
            $query .= "             AND EXISTS (SELECT ";
            $query .= "                             'x' ";
            $query .= "                         FROM ";
            $query .= "                             (SELECT ";
            $query .= "                                 SCHREGNO, ";
            $query .= "                                 GRADE, ";
            $query .= "                                 MAX(YEAR || SEMESTER) AS YEAR_SEM ";
            $query .= "                             FROM ";
            $query .= "                                 SCHREG_REGD_DAT ";
            $query .= "                             WHERE ";
            $query .= "                                     YEAR < '".CTRL_YEAR."' ";
            $query .= "                                 AND SCHREGNO IN (SELECT ";
            $query .= "                                                      SCHREGNO ";
            $query .= "                                                  FROM ";
            $query .= "                                                      SCH_INFO ";
            $query .= "                                                  ) ";
            $query .= "                                 AND GRADE IN (SELECT ";
            $query .= "                                                   GRADE ";
            $query .= "                                               FROM ";
            $query .= "                                                   SCHREG_REGD_GDAT ";
            $query .= "                                               WHERE ";
            $query .= "                                                       YEAR  = '".CTRL_YEAR."' ";
            $query .= "                                                   AND GRADE < '{$model->gc_select}' ";
            $query .= "                                                   AND SCHOOL_KIND IN (SELECT ";
            $query .= "                                                                           SCHOOL_KIND ";
            $query .= "                                                                       FROM ";
            $query .= "                                                                           SCHREG_REGD_GDAT ";
            $query .= "                                                                       WHERE ";
            $query .= "                                                                               YEAR  = '".CTRL_YEAR."' ";
            $query .= "                                                                           AND GRADE = '{$model->gc_select}' ";
            $query .= "                                                                       ) ";
            $query .= "                                               ) ";
            $query .= "                             GROUP BY ";
            $query .= "                                 SCHREGNO, ";
            $query .= "                                 GRADE ";
            $query .= "                             ) E1 ";
            $query .= "                         WHERE ";
            $query .= "                                 T1.SCHREGNO = E1.SCHREGNO ";
            $query .= "                             AND T1.GRADE    = E1.GRADE ";
            $query .= "                             AND T1.YEAR || T1.SEMESTER = E1.YEAR_SEM ";
            $query .= "                         ) ";
        }
        $query .= "         GROUP BY ";
        $query .= "             T1.SCHREGNO, ";
        $query .= "             T1.COURSECD, ";
        $query .= "             T1.MAJORCD, ";
        $query .= "             T1.COURSECODE, ";
        if ($model->schoolDiv == "1") {//単位制
            $query .= "             T1.YEAR, ";
        }
        $query .= "             T1.GRADE ";
        //前籍校の習得単位
        $query .= " ), BSD AS ( ";
        $query .= "         SELECT ";
        $query .= "             T1.SCHREGNO, ";
        $query .= "             CASE WHEN SUM(T1.GET_CREDIT) IS NULL AND SUM(T1.ADD_CREDIT) IS NULL THEN NULL ";
        $query .= "                  ELSE VALUE(SUM(T1.GET_CREDIT), 0) + VALUE(SUM(T1.ADD_CREDIT), 0) END AS STUDYREC_CREDIT_ZENSEKI ";
        $query .= "         FROM ";
        $query .= "             SCHREG_STUDYREC_DAT T1 ";
        $query .= "         WHERE ";
        $query .= "             T1.SCHOOLCD = '1' ";
        $query .= "             AND EXISTS (SELECT ";
        $query .= "                             'X' ";
        $query .= "                         FROM ";
        $query .= "                             SCH_INFO T3 ";
        $query .= "                         WHERE ";
        $query .= "                             T3.SCHREGNO = T1.SCHREGNO ";
        $query .= "                         ) ";
        $query .= "             AND substr(T1.SUBCLASSCD, 1, 2) <= '90' ";
        $query .= "         GROUP BY ";
        $query .= "             T1.SCHREGNO ";
        //前年度までの習得単位
        $query .= " ), SSD AS ( ";
        $query .= "         SELECT ";
        $query .= "             T1.SCHREGNO, ";
        $query .= "             SUM(VALUE(T1.GET_CREDIT, 0) + VALUE(T1.ADD_CREDIT, 0)) AS STUDYREC_CREDIT_UNTIL_LAST ";
        $query .= "         FROM ";
        $query .= "             SCHREG_STUDYREC_DAT T1 ";
        $query .= "             INNER JOIN (SELECT DISTINCT ";
        $query .= "                             YEAR, ";
        $query .= "                             SCHREGNO ";
        $query .= "                         FROM ";
        $query .= "                             SCH_INF_LAST_YEAR ";
        $query .= "                         ) T2 ON T1.YEAR = T2.YEAR AND T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "         WHERE ";
        $query .= "                 T1.SCHOOLCD = '0' ";
        $query .= "             AND T1.YEAR     < '".CTRL_YEAR."' ";
        $query .= "             AND substr(T1.SUBCLASSCD, 1, 2) <= '90' ";
        $query .= "         GROUP BY ";
        $query .= "             T1.SCHREGNO ";
        //留学習得単位（今年度）
        $query .= " ), ABT AS ( ";
        $query .= "         SELECT ";
        $query .= "             SCHREGNO, ";
        $query .= "             SUM(VALUE(ABROAD_CREDITS, 0)) AS ABROAD_CREDITS ";
        $query .= "         FROM ";
        $query .= "             SCHREG_TRANSFER_DAT ";
        $query .= "         WHERE ";
        $query .= "                 TRANSFERCD = '1' ";
        $query .= "             AND FISCALYEAR(TRANSFER_SDATE) = '".CTRL_YEAR."' ";
        $query .= "         GROUP BY ";
        $query .= "             SCHREGNO ";
        //留学習得単位（前年度以前）
        $query .= " ), ABL AS ( ";
        $query .= "         SELECT ";
        $query .= "             SCHREGNO, ";
        $query .= "             SUM(VALUE(ABROAD_CREDITS, 0)) AS ABROAD_CREDITS ";
        $query .= "         FROM ";
        $query .= "             SCHREG_TRANSFER_DAT ";
        $query .= "         WHERE ";
        $query .= "                 TRANSFERCD = '1' ";
        $query .= "             AND FISCALYEAR(TRANSFER_SDATE) < '".CTRL_YEAR."' ";
        $query .= "         GROUP BY ";
        $query .= "             SCHREGNO ";
        //標準修得単位
        $query .= " ), REC AS ( ";
        $query .= "         SELECT ";
        $query .= "             T1.SCHREGNO, ";
        $query .= "             SUM(VALUE(T1.GET_CREDIT, 0) + VALUE(T1.ADD_CREDIT, 0)) AS RECORD_DAT_CREDIT ";
        $query .= "         FROM ";
        if ($model->Properties["useTestCountflg"] == "TESTITEM_MST_COUNTFLG_NEW_SDIV" || $model->Properties["useTestCountflg"] == "TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV") {
            $query .= "             RECORD_SCORE_DAT T1 ";
            if ($model->isRecordProvFlgDat) {
                $query .= "             LEFT JOIN RECORD_PROV_FLG_DAT L1 ON L1.YEAR          = T1.YEAR ";
                $query .= "                                             AND L1.CLASSCD       = T1.CLASSCD ";
                $query .= "                                             AND L1.SCHOOL_KIND   = T1.SCHOOL_KIND ";
                $query .= "                                             AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ";
                $query .= "                                             AND L1.SUBCLASSCD    = T1.SUBCLASSCD ";
                $query .= "                                             AND L1.SCHREGNO      = T1.SCHREGNO ";
            }
            $query .= "         WHERE ";
            $query .= "                 T1.YEAR       = '".CTRL_YEAR."' ";
            $query .= "             AND T1.SEMESTER   = '9' ";
            $query .= "             AND T1.TESTKINDCD = '99' ";
            $query .= "             AND T1.TESTITEMCD = '00' ";
            $query .= "             AND T1.SCORE_DIV  = '09' ";
            if ($model->isRecordProvFlgDat) {
                $query .= "             AND L1.PROV_FLG IS NULL ";
            }
        } elseif ($model->useRecordScoreDat) {
            $query .= "             RECORD_SCORE_DAT T1 ";
            $query .= "         WHERE ";
            $query .= "                 T1.YEAR       = '".CTRL_YEAR."' ";
            $query .= "             AND T1.SEMESTER   = '9' ";
            $query .= "             AND T1.TESTKINDCD = '99' ";
            $query .= "             AND T1.TESTITEMCD = '00' ";
            $query .= "             AND T1.SCORE_DIV  = '00' ";
        } else {
            $query .= "             RECORD_DAT T1 ";
            $query .= "         WHERE ";
            $query .= "                 T1.YEAR       = '".CTRL_YEAR."' ";
        }
        $query .= "             AND T1.SCHREGNO IN (SELECT ";
        $query .= "                                     SCHREGNO ";
        $query .= "                                 FROM ";
        $query .= "                                     SCH_INFO ";
        $query .= "                                 ) ";
        $query .= "             AND substr(T1.SUBCLASSCD, 1, 2) <= '90' ";
        $query .= "             AND NOT EXISTS (SELECT ";
        $query .= "                                 'X' ";
        $query .= "                             FROM ";
        $query .= "                                 SUBCLASS_REPLACE_COMBINED_DAT E1 ";
        $query .= "                             WHERE ";
        $query .= "                                     E1.YEAR                 = '".CTRL_YEAR."' ";
        $query .= "                                 AND E1.CALCULATE_CREDIT_FLG = '1' ";// 1:固定 => 先科目の単位数を加算するので元科目を除く
        $query .= "                                 AND E1.ATTEND_CLASSCD       = T1.CLASSCD ";
        $query .= "                                 AND E1.ATTEND_SCHOOL_KIND   = T1.SCHOOL_KIND ";
        $query .= "                                 AND E1.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "                                 AND E1.ATTEND_SUBCLASSCD    = T1.SUBCLASSCD ";
        $query .= "                             UNION ALL ";
        $query .= "                             SELECT ";
        $query .= "                                 'X' ";
        $query .= "                             FROM ";
        $query .= "                                 SUBCLASS_REPLACE_COMBINED_DAT E1 ";
        $query .= "                             WHERE ";
        $query .= "                                     E1.YEAR                   = '".CTRL_YEAR."' ";
        $query .= "                                 AND E1.CALCULATE_CREDIT_FLG   = '2' ";// 2:加算 => 元科目の単位数を加算するので先科目を除く
        $query .= "                                 AND E1.COMBINED_CLASSCD       = T1.CLASSCD ";
        $query .= "                                 AND E1.COMBINED_SCHOOL_KIND   = T1.SCHOOL_KIND ";
        $query .= "                                 AND E1.COMBINED_CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "                                 AND E1.COMBINED_SUBCLASSCD    = T1.SUBCLASSCD ";
        $query .= "                             ) ";
        $query .= "         GROUP BY ";
        $query .= "             T1.SCHREGNO ";

        if ($model->Properties["useTestCountflg"] == "TESTITEM_MST_COUNTFLG_NEW_SDIV" || $model->Properties["useTestCountflg"] == "TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV") {
            $table = "RECORD_SCORE_DAT";
        } elseif ($model->useRecordScoreDat) {
            $table = "RECORD_SCORE_DAT";
        } else {
            $table = "RECORD_DAT";
        }

        $query .= " ),  CHAIR  AS ( ";
        $query .= "         SELECT ";
        $query .= "             T1.SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             T2.CLASSCD || T2.SCHOOL_KIND || T2.CURRICULUM_CD || ";
        }
        $query .= "             T2.SUBCLASSCD AS SUBCLASSCD, ";
        $query .= "             MAX(CRE.CREDITS) AS CREDITS ";
        $query .= "         FROM ";
        $query .= "             CHAIR_STD_DAT T1 ";
        $query .= "             INNER JOIN CHAIR_DAT T2 ON T2.YEAR     = T1.YEAR ";
        $query .= "                                    AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "                                    AND T2.CHAIRCD  = T1.CHAIRCD ";
        $query .= "             INNER JOIN SCH_INFO T3 ON T3.SCHREGNO = T1.SCHREGNO ";
        $query .= "             LEFT JOIN CREDIT_MST CRE   ON CRE.YEAR          = T1.YEAR ";
        $query .= "                                       AND CRE.COURSECD      = T3.COURSECD ";
        $query .= "                                       AND CRE.MAJORCD       = T3.MAJORCD ";
        $query .= "                                       AND CRE.GRADE         = T3.GRADE ";
        $query .= "                                       AND CRE.COURSECODE    = T3.COURSECODE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                       AND CRE.CLASSCD       = T2.CLASSCD ";
            $query .= "                                       AND CRE.SCHOOL_KIND   = T2.SCHOOL_KIND ";
            $query .= "                                       AND CRE.CURRICULUM_CD = T2.CURRICULUM_CD ";
        }
        $query .= "                                       AND CRE.SUBCLASSCD    = T2.SUBCLASSCD ";
        $query .= "         WHERE ";
        $query .= "                 T1.YEAR      = '".CTRL_YEAR."' ";
        $query .= "             AND T1.SEMESTER <= '".CTRL_SEMESTER."' ";
        $query .= "             AND substr(T2.SUBCLASSCD, 1, 2) <= '90' ";
        $query .= "         GROUP BY ";
        $query .= "             T1.SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             T2.CLASSCD || T2.SCHOOL_KIND || T2.CURRICULUM_CD || ";
        }
        $query .= "             T2.SUBCLASSCD ";
        $query .= " ),  REC_SUBCLASS  AS ( ";
        $query .= "         SELECT ";
        $query .= "             T1.SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || ";
        }
        $query .= "             T1.SUBCLASSCD AS SUBCLASSCD, ";
        $query .= "             CASE WHEN T1.GET_CREDIT IS NOT NULL OR T1.ADD_CREDIT IS NOT NULL THEN VALUE(T1.GET_CREDIT, 0) + VALUE(T1.ADD_CREDIT, 0) ";
        $query .= "             END AS RECORD_DAT_CREDIT ";
        $query .= "         FROM ";
        $query .= "             {$table} T1 ";
        if ($model->isRecordProvFlgDat) {
            $query .= "             LEFT JOIN RECORD_PROV_FLG_DAT L1 ON L1.YEAR          = T1.YEAR ";
            $query .= "                                             AND L1.CLASSCD       = T1.CLASSCD ";
            $query .= "                                             AND L1.SCHOOL_KIND   = T1.SCHOOL_KIND ";
            $query .= "                                             AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ";
            $query .= "                                             AND L1.SUBCLASSCD    = T1.SUBCLASSCD ";
            $query .= "                                             AND L1.SCHREGNO      = T1.SCHREGNO ";
        }
        $query .= "         WHERE ";
        $query .= "                 T1.YEAR       = '".CTRL_YEAR."' ";
        if ($model->Properties["useTestCountflg"] == "TESTITEM_MST_COUNTFLG_NEW_SDIV" || $model->Properties["useTestCountflg"] == "TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV") {
            $query .= "             AND T1.SEMESTER   = '9' ";
            $query .= "             AND T1.TESTKINDCD = '99' ";
            $query .= "             AND T1.TESTITEMCD = '00' ";
            $query .= "             AND T1.SCORE_DIV  = '09' ";
            if ($model->isRecordProvFlgDat) {
                $query .= "             AND L1.PROV_FLG IS NULL ";
            }
        } elseif ($model->useRecordScoreDat) {
            $query .= "             AND T1.SEMESTER   = '9' ";
            $query .= "             AND T1.TESTKINDCD = '99' ";
            $query .= "             AND T1.TESTITEMCD = '00' ";
            $query .= "             AND T1.SCORE_DIV  = '00' ";
        }
        $query .= "             AND substr(T1.SUBCLASSCD, 1, 2) <= '90' ";
        $query .= "             AND NOT EXISTS (SELECT ";
        $query .= "                                 'X' ";
        $query .= "                             FROM ";
        $query .= "                                 SUBCLASS_REPLACE_COMBINED_DAT E1 ";
        $query .= "                             WHERE ";
        $query .= "                                     E1.YEAR = '".CTRL_YEAR."' ";
        $query .= "                                 AND E1.CALCULATE_CREDIT_FLG = '1' ";// 1:固定 => 先科目の単位数を加算するので元科目を除く
        $query .= "                                 AND E1.ATTEND_CLASSCD       = T1.CLASSCD ";
        $query .= "                                 AND E1.ATTEND_SCHOOL_KIND   = T1.SCHOOL_KIND ";
        $query .= "                                 AND E1.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "                                 AND E1.ATTEND_SUBCLASSCD    = T1.SUBCLASSCD ";
        $query .= "                             UNION ALL                       ";
        $query .= "                             SELECT ";
        $query .= "                                 'X' ";
        $query .= "                             FROM ";
        $query .= "                                 SUBCLASS_REPLACE_COMBINED_DAT E1 ";
        $query .= "                             WHERE ";
        $query .= "                                     E1.YEAR                   = '".CTRL_YEAR."' ";
        $query .= "                                 AND E1.CALCULATE_CREDIT_FLG   = '2' ";// 2:加算 => 元科目の単位数を加算するので先科目を除く
        $query .= "                                 AND E1.COMBINED_CLASSCD       = T1.CLASSCD ";
        $query .= "                                 AND E1.COMBINED_SCHOOL_KIND   = T1.SCHOOL_KIND ";
        $query .= "                                 AND E1.COMBINED_CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "                                 AND E1.COMBINED_SUBCLASSCD    = T1.SUBCLASSCD ";
        $query .= "                             ) ";
        //習得見込単位
        $query .= " ),  MKM  AS ( ";
        $query .= "         SELECT ";
        $query .= "             T1.SCHREGNO, ";
        $query .= "             SUM(T1.CREDITS) AS MIKOMI_CREDIT ";
        $query .= "         FROM ";
        $query .= "             CHAIR  T1 ";
        $query .= "             LEFT JOIN  REC_SUBCLASS  T2 ON T2.SCHREGNO   = T1.SCHREGNO ";
        $query .= "                                        AND T2.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "             INNER JOIN SCH_INFO T3 ON T3.SCHREGNO = T1.SCHREGNO ";
        $query .= "         WHERE ";
        $query .= "             (T3.GRD_DATE IS NULL ";
        $query .= "                OR NOT T3.GRD_DATE >= (SELECT EDATE FROM SEMESTER_MST WHERE YEAR = '".CTRL_YEAR."' AND SEMESTER = '".CTRL_SEMESTER."') ";
        $query .= "               ) ";
        $query .= "             AND T2.RECORD_DAT_CREDIT IS NULL ";
        $query .= "         GROUP BY ";
        $query .= "             T1.SCHREGNO ";
        $query .= " ), SQD AS ( ";
        $query .= "         SELECT ";
        $query .= "             SCHREGNO, ";
        $query .= "             SUM(VALUE(CREDITS, 0)) AS QUALIFIED_CREDIT ";
        $query .= "         FROM ";
        $query .= "             SCHREG_QUALIFIED_DAT ";
        $query .= "         WHERE ";
        $query .= "             YEAR = '".CTRL_YEAR."' ";
        $query .= "             AND SCHREGNO IN (SELECT ";
        $query .= "                                  SCHREGNO ";
        $query .= "                              FROM ";
        $query .= "                                  SCH_INFO ";
        $query .= "                              ) ";
        $query .= "         GROUP BY ";
        $query .= "             SCHREGNO ";
        $query .= " ), CREDIT_DATA AS (   ";
        $query .= "         SELECT ";
        $query .= "             T1.SCHREGNO, ";
        $query .= "             value(BSD.STUDYREC_CREDIT_ZENSEKI, 0) + ";
        $query .= "             value(SSD.STUDYREC_CREDIT_UNTIL_LAST, 0) + ";
        $query .= "             value(ABL.ABROAD_CREDITS, 0) + ";
        $query .= "             value(REC.RECORD_DAT_CREDIT, 0) + ";
        $query .= "             value(ABT.ABROAD_CREDITS, 0)  + ";
        $query .= "             value(SQD.QUALIFIED_CREDIT, 0) + ";
        $query .= "             value(MKM.MIKOMI_CREDIT, 0) as TOTAL_CREDITS ";
        $query .= "         FROM ";
        $query .= "             SCH_INFO T1 ";
        $query .= "             LEFT JOIN SSD ON SSD.SCHREGNO = T1.SCHREGNO ";
        $query .= "             LEFT JOIN REC ON REC.SCHREGNO = T1.SCHREGNO ";
        $query .= "             LEFT JOIN SQD ON SQD.SCHREGNO = T1.SCHREGNO ";
        $query .= "             LEFT JOIN ABT ON ABT.SCHREGNO = T1.SCHREGNO ";
        $query .= "             LEFT JOIN ABL ON ABL.SCHREGNO = T1.SCHREGNO ";
        $query .= "             LEFT JOIN BSD ON BSD.SCHREGNO = T1.SCHREGNO ";
        $query .= "             LEFT JOIN MKM ON MKM.SCHREGNO = T1.SCHREGNO ";
        $query .= " ) ";

        return $query;
    }

    //学校マスタ
    public function getSchoolMst($model, $fieldName)
    {
        $query  = " SELECT ";
        $query .= "     {$fieldName} ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR        = '".CTRL_YEAR."' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= "     AND SCHOOL_KIND in (SELECT ";
            $query .= "                             SCHOOL_KIND ";
            $query .= "                         FROM ";
            $query .= "                             SCHREG_REGD_GDAT ";
            $query .= "                         WHERE ";
            $query .= "                                 YEAR  = '".CTRL_YEAR."' ";
            $query .= "                             AND GRADE = '{$model->gc_select}') ";
        }

        return $query;
    }

    //テーブル有無チェック
    public function checkTable($tableName)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SYSCAT.TABLES ";
        $query .= " WHERE ";
        $query .= "     TABNAME  = '{$tableName}' ";

        return $query;
    }

    //DELETE
    public function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();

        if ($model->fields["KINDCD"] != "") {
            $query  = "DELETE FROM geneviewmbr_kind_dat ";
            $query .= " WHERE year  = '".CTRL_YEAR."'";
            $query .= "   AND kindcd = '".$model->fields["KINDCD"]."'";
            $query .= "   AND grade = '".$model->gc_select."'";
        } else {
            $query  = "DELETE FROM geneviewmbr_dat ";
            $query .= " WHERE year  = '".CTRL_YEAR."'";
            $query .= "   AND grade = '".$model->gc_select."'";
        }

        $db->query($query);

        Query::dbCheckIn($db);
        return true;
    }

    //UPDATE
    public function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();

        $db->autoCommit(false);
        for ($i=0; $i<get_count($model->fields["CODE"]); $i++) {
            $code_arr = explode(",", $model->fields["CODE"][$i]);

            $course_member = $model->fields["A_MEMBER"][$i];
            $course_member += $model->fields["B_MEMBER"][$i];
            $course_member += $model->fields["C_MEMBER"][$i];
            $course_member += $model->fields["D_MEMBER"][$i];
            $course_member += $model->fields["E_MEMBER"][$i];

            $data["YEAR"][TEXT]             = CTRL_YEAR;
            if ($model->fields["KINDCD"] != "") {
                $data["KINDCD"][TEXT]         = $model->fields["KINDCD"];
            }
            $data["COURSECD"][TEXT]         = $code_arr[0];
            $data["MAJORCD"][TEXT]          = $code_arr[1];
            $data["GRADE"][TEXT]            = $model->gc_select;
            $data["COURSECODE"][TEXT]       = $code_arr[2];
            $data["A_MEMBER"][NUMBER]       = $model->fields["A_MEMBER"][$i];
            $data["B_MEMBER"][NUMBER]       = $model->fields["B_MEMBER"][$i];
            $data["C_MEMBER"][NUMBER]       = $model->fields["C_MEMBER"][$i];
            $data["D_MEMBER"][NUMBER]       = $model->fields["D_MEMBER"][$i];
            $data["E_MEMBER"][NUMBER]       = $model->fields["E_MEMBER"][$i];
            $data["COURSE_MEMBER"][NUMBER]  = $course_member;
            $data["GRADE_MEMBER"][NUMBER]   = $model->fields["GRADE_MEMBER"][$i];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        =" sysdate()";

            if ($model->fields["KINDCD"] != "") {
                $query = Query::insertSQL($data, "geneviewmbr_kind_dat");
            } else {
                $query = Query::insertSQL($data, "geneviewmbr_dat");
            }
            $db->query($query);
        }
        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    // 評定平均、順位を更新
    public function updateAvgRank($model)
    {
        $db = Query::dbCheckOut();
        $baseSeq = "";
        if ($model->fields["KINDCD"] == "01") {
            $baseSeq = "017";
        } elseif ($model->fields["KINDCD"] == "") {
            $baseSeq = "018";
        }
        if ($baseSeq == "") {
            return;
        }
        $query = knje065bQuery::deleteAvgRankQuery($model, $baseSeq);
        $db->query($query);

        $query = knje065bQuery::getRecalculateQuery($model, "RANK");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = array();
            $data["SCHREGNO"][TEXT]     = $row["SCHREGNO"];
            $data["BASE_SEQ"][TEXT]     = $baseSeq;
            $data["BASE_REMARK1"][TEXT] = $row["RANK"];
            $data["BASE_REMARK2"][TEXT] = $row["AVG"];
            $data["BASE_REMARK3"][TEXT] = $row["SUM"];
            $data["BASE_REMARK4"][TEXT] = $row["COUNT"];
            $data["BASE_REMARK5"][TEXT] = $row["CREDIT_SUM"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][NUMBER]    = "sysdate()";
            $query = Query::insertSQL($data, "SCHREG_BASE_DETAIL_MST");
            $db->query($query);
        }
        $result->free();
        Query::dbCheckIn($db);
    }

    public function deleteAvgRankQuery($model, $baseSeq)
    {
        $query = " DELETE FROM ";
        $query .= "    SCHREG_BASE_DETAIL_MST T1 ";
        $query .= " WHERE ";
        $query .= "    T1.SCHREGNO IN (SELECT SCHREGNO ";
        $query .= "                 FROM SCHREG_REGD_DAT ";
        $query .= "                 WHERE YEAR = '".CTRL_YEAR."' ";
        $query .= "                   AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "                   AND GRADE = '".$model->gc_select."' ";
        $query .= "                 ) ";
        $query .= "    AND T1.BASE_SEQ = '{$baseSeq}' ";
        return $query;
    }

    //UPDATE
    public function &setHExamEntRemarkHDatQuery($model)
    {
        $db = Query::dbCheckOut();

        $valuation = "T1.valuation";
        //評定1の場合は2で処理する
        if ($model->hyoteiYomikae == "1") {
            //仮評定フラグ
            if ($model->Properties["useProvFlg"] == '1') {
                $valuation = "case when T1.valuation = 1 AND T1.PROV_FLG = '1' then 2 else T1.valuation end";
            } else {
                $valuation = "case when T1.valuation = 1 then 2 else T1.valuation end";
            }
        }
        $valuationCase  = "case when 0 < T1.valuation then T1.valuation end";
        $creditCase     = "case when 0 < T1.valuation then T1.CREDIT end";

        $query  = "";
        if ($model->Properties["useKNJE065B_grdCreditSansyo"] == '1') {
            $query .= knje065bQuery::getCreditWithText($model);
            $query .= ", ";
        } else {
            $query .= " WITH ";
        }

        $query .= " REGD_T AS ( ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."'";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER."'";
        $query .= "     AND GRADE = '".$model->gc_select."' ";
        $query .= " ), REGD_YEAR AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.ANNUAL, ";
        $query .= "     MAX(T1.YEAR) AS YEAR ";
        $query .= " FROM ";
        $query .= "     SCHREG_STUDYREC_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     EXISTS( ";
        $query .= "         SELECT ";
        $query .= "             'x' ";
        $query .= "         FROM ";
        $query .= "             REGD_T ";
        $query .= "         WHERE ";
        $query .= "             T1.SCHREGNO = REGD_T.SCHREGNO ";
        $query .= "     ) ";
        $query .= "     AND T1.SCHOOL_KIND = ( SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '".CTRL_YEAR."' AND GRADE = '".$model->gc_select."' ) ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.ANNUAL ";
        $query .= " ), SUBCLASS_GROUP AS ( ";
        $query .= "     SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.CLASSCD, ";
            $query .= "     T2.SCHOOL_KIND, ";
            $query .= "     T2.CURRICULUM_CD, ";
        }
        $query .= "         T2.SUBCLASSCD, ";
        $query .= "         T2.SUBCLASSCD2 ";
        $query .= "     FROM ";
        $query .= "         SUBCLASS_MST T2, ";
        $query .= "         SUBCLASS_MST T3 ";
        $query .= "     WHERE ";
        $query .= "         T2.SUBCLASSCD2 IS NOT NULL AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.CLASSCD = T3.CLASSCD AND ";
            $query .= "     T2.SCHOOL_KIND = T3.SCHOOL_KIND AND ";
            $query .= "     T2.CURRICULUM_CD = T3.CURRICULUM_CD AND ";
        }
        $query .= "         T2.SUBCLASSCD2 = T3.SUBCLASSCD ";
        $query .= " ), T_SCHREG_STUDYREC AS ( ";
        $query .= "     SELECT T1.YEAR,T1.SCHREGNO,T1.CLASSCD,T1.VALUATION ";
        if ($model->Properties["useProvFlg"] == '1') {
            $query .= "           ,L3.PROV_FLG ";
        } else {
            $query .= "           ,CAST(NULL AS VARCHAR(1)) AS PROV_FLG ";
        }
        $query .= "           ,CASE WHEN T1.ADD_CREDIT IS NOT NULL ";
        $query .= "                 THEN VALUE(T1.GET_CREDIT,0) + VALUE(T1.ADD_CREDIT,0) ";
        $query .= "                 ELSE T1.GET_CREDIT END AS CREDIT ";
        $query .= "           ,L1.GVAL_CALC ";
        $query .= "           ,CASE WHEN L2.SUBCLASSCD2 IS NOT NULL THEN L2.SUBCLASSCD2 ";
        $query .= "                 ELSE T1.SUBCLASSCD END AS SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "       ,T1.SCHOOL_KIND ";
            $query .= "       ,T1.CURRICULUM_CD ";
        }
        $query .= "     FROM SCHREG_STUDYREC_DAT T1 ";
        $query .= "          LEFT JOIN SCHOOL_MST L1 ON L1.YEAR = T1.YEAR ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND L1.SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= "     AND L1.SCHOOL_KIND in (SELECT ";
            $query .= "                             SCHOOL_KIND ";
            $query .= "                         FROM ";
            $query .= "                             SCHREG_REGD_GDAT ";
            $query .= "                         WHERE ";
            $query .= "                                 YEAR  = '".CTRL_YEAR."' ";
            $query .= "                             AND GRADE = '{$model->gc_select}') ";
        }
        $query .= "          LEFT JOIN SUBCLASS_GROUP L2 ON L2.SUBCLASSCD = T1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                 AND L2.CLASSCD = T1.CLASSCD ";
            $query .= "                                 AND L2.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "                                 AND L2.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        //仮評定フラグ
        if ($model->Properties["useProvFlg"] == '1') {
            $query .= "     LEFT JOIN STUDYREC_PROV_FLG_DAT L3 ";
            $query .= "            ON  L3.SCHOOLCD = T1.SCHOOLCD ";
            $query .= "            AND L3.YEAR = T1.YEAR ";
            $query .= "            AND L3.SCHREGNO = T1.SCHREGNO ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "        AND L3.CLASSCD = T1.CLASSCD ";
                $query .= "        AND L3.SCHOOL_KIND = T1.SCHOOL_KIND ";
                $query .= "        AND L3.CURRICULUM_CD = T1.CURRICULUM_CD ";
            }
            $query .= "            AND L3.SUBCLASSCD = T1.SUBCLASSCD ";
        }
        $query .= "     WHERE ";
        if ($model->z010["NAME1"] == "jisyukan") {
            $query .= "            (T1.CLASSCD BETWEEN '01' AND '89' OR T1.SUBCLASSCD = '941001')";
        } elseif ($model->z010["NAME1"] == "HOUSEI") {
            $query .= "            T1.CLASSCD BETWEEN '01' AND '85' ";
        } else {
            $query .= "            T1.CLASSCD BETWEEN '01' AND '89' ";
        }
        $query .= "            AND EXISTS ( ";
        $query .= "                        SELECT 'X' ";
        $query .= "                        FROM REGD_YEAR E1 ";
        $query .= "                        WHERE T1.SCHREGNO = E1.SCHREGNO ";
        $query .= "                          AND T1.YEAR = E1.YEAR ";
        $query .= "                       ) ";
        if ($model->electdiv == "on") { // 選択科目を除く
            $query .= "            AND NOT EXISTS(SELECT 'X' FROM CLASS_MST T2 ";
            $query .= "                            WHERE T2.ELECTDIV='1' ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                          AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
            }
            $query .= "                              AND T2.CLASSCD=T1.CLASSCD) ";
            $query .= "            AND NOT EXISTS(SELECT 'X' FROM SUBCLASS_MST T3 ";
            $query .= "                            WHERE T3.ELECTDIV='1' ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                          AND T3.CLASSCD = T1.CLASSCD ";
                $query .= "                          AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ";
                $query .= "                          AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ";
            }
            $query .= "                              AND T3.SUBCLASSCD=T1.SUBCLASSCD) ";
        }
        $query .= " ), STUDYREC AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.YEAR,T1.SCHREGNO,T1.CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD,T1.GVAL_CALC,T1.VALUATION,T1.PROV_FLG ";
        $query .= "     FROM ";
        $query .= "         T_SCHREG_STUDYREC T1 ";
        $query .= "     WHERE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD || '-' ||  ";
            $query .= "     T1.SCHOOL_KIND || '-' ||  ";
            $query .= "     T1.CURRICULUM_CD || '-' ||  ";
        }
        $query .= "         T1.SUBCLASSCD NOT IN (SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         W2.CLASSCD || '-' ||  ";
            $query .= "         W2.SCHOOL_KIND || '-' ||  ";
            $query .= "         W2.CURRICULUM_CD || '-' ||  ";
        }
        $query .= "             W2.SUBCLASSCD2 FROM SUBCLASS_GROUP W2) ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT ";
        $query .= "         T1.YEAR,T1.SCHREGNO,T1.CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD,T1.GVAL_CALC ";
        $query .= "        ,case when COUNT(*) = 1 then MAX(T1.VALUATION) ";
        $query .= "              when T1.GVAL_CALC = '0' then ROUND(AVG(FLOAT(".$valuationCase.")),0) ";
        $query .= "              when T1.GVAL_CALC = '1' and 0 < SUM(".$creditCase.") then ROUND(FLOAT(SUM((".$valuationCase.")*T1.CREDIT))/SUM(".$creditCase."),0) ";
        $query .= "              else MAX(T1.VALUATION) end AS VALUATION ";
        $query .= "        ,MIN(VALUE(T1.PROV_FLG, '0')) AS PROV_FLG ";
        $query .= "     FROM ";
        $query .= "         T_SCHREG_STUDYREC T1 ";
        $query .= "     WHERE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD || '-' ||  ";
            $query .= "     T1.SCHOOL_KIND || '-' ||  ";
            $query .= "     T1.CURRICULUM_CD || '-' ||  ";
        }
        $query .= "         T1.SUBCLASSCD IN (SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         W2.CLASSCD || '-' ||  ";
            $query .= "         W2.SCHOOL_KIND || '-' ||  ";
            $query .= "         W2.CURRICULUM_CD || '-' ||  ";
        }
        $query .= "             W2.SUBCLASSCD2 FROM SUBCLASS_GROUP W2) ";
        $query .= "     GROUP BY ";
        $query .= "         T1.YEAR,T1.SCHREGNO,T1.CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD,T1.GVAL_CALC ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "   T1.SCHREGNO, ";
        $query .= "   T2.avg, ";
        $query .= "   CASE WHEN VALUE(T3.NAME1, 1000) <= T2.avg THEN '1' ELSE NULL END AS EX_A_CD ";  //T3.NAME1がNULLなら、あり得ない値にしておく。ここを低い値にすればデバッグ可能。

        $query .= "  FROM (SELECT distinct year, schregno, grade, coursecd, majorcd ";
        $query .= "          FROM schreg_regd_dat w1";
        $query .= "         WHERE year     = '".CTRL_YEAR."'";
        $query .= "           AND semester = '".CTRL_SEMESTER."'";
        $query .= "           AND grade    = '".$model->gc_select."' ";
        $query .= "           AND NOT EXISTS ( ";
        $query .= "                           SELECT 'X' ";
        $query .= "                           FROM schreg_base_mst w2 ";
        $query .= "                           WHERE w2.schregno = w1.schregno ";
        $query .= "                             AND w2.grd_div in ('2','3','6','7') "; //2:退学 3:転学 6:除籍 7:転籍 者は除く
        $query .= "                             AND w2.grd_date < '".str_replace("/", "-", $model->base_date)."' ";
        $query .= "                          ) ";
        //習得単位集計が卒業履修単位数以上の人  KNJD233Vと同じ条件
        if ($model->Properties["useKNJE065B_grdCreditSansyo"] == '1') {
            $query .= "       AND EXISTS (SELECT ";
            $query .= "                       'X' ";
            $query .= "                   FROM ";
            $query .= "                       CREDIT_DATA CRDT ";
            $query .= "                   WHERE ";
            $query .= "                           CRDT.schregno = w1.schregno ";
            $query .= "                       AND CRDT.TOTAL_CREDITS >= {$model->gradCompCredits} ";
            $query .= "                   ) ";
        }
        $query .= "       ) T1 ";
        $query .= "          LEFT JOIN (";
        $query .= "                           SELECT W1.schregno, W1.avg, W2.assesslevel ";
        $query .= "                           FROM ";
        $query .= "                                  (SELECT T1.schregno, round(AVG(decimal(".$valuation.")),1)  AS avg ";
        $query .= "                                   FROM STUDYREC T1 ";
        $query .= "                                    GROUP BY T1.schregno) W1,";
        if ($model->Properties["useAssessCourseMst"] == "1") {
            $query .= "                                  (  SELECT T1.SCHREGNO, T2.* ";
            $query .= "                                     FROM REGD_T T1, ASSESS_COURSE_MST T2 ";
            $query .= "                                     WHERE T2.assesscd   = '4' AND ";
            $query .= "                                           T1.COURSECD   = T2.COURSECD AND ";
            $query .= "                                           T1.MAJORCD    = T2.MAJORCD AND ";
            $query .= "                                           T1.COURSECODE = T2.COURSECODE ) W2 ";
            $query .= "                            where W1.avg between W2.assesslow AND W2.assesshigh AND ";
            $query .= "                                  W1.SCHREGNO = W2.SCHREGNO ";
        } else {
            $query .= "                                  (SELECT * ";
            $query .= "                                     FROM assess_mst ";
            $query .= "                                    WHERE assesscd = '4' ) W2 ";
            $query .= "                            where W1.avg between W2.assesslow AND W2.assesshigh ";
        }
        $query .= "       ) T2 ON T1.schregno = T2.schregno ";
        $query .= " LEFT JOIN NAME_MST T3 ON T3.NAMECD1 = 'E073' AND NAMECD2 = '01' ";
        $query .= " WHERE T1.SCHREGNO IS NOT NULL ";

        $result = $db->query($query);

        $db->autoCommit(false);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = array();
            $data["COMMENTEX_A_CD"][TEXT] = $row["EX_A_CD"];
            $data["REGISTERCD"][TEXT] = STAFFCD;
            $data["UPDATED"][NUMBER]  =" sysdate()";
            $count = $db->getOne(" SELECT COUNT(*) FROM HEXAM_ENTREMARK_HDAT WHERE SCHREGNO = '".$row["SCHREGNO"]."' ");
            if ($count > 0) {
                $where = " WHERE SCHREGNO = '".$row["SCHREGNO"]."' ";
                $query = Query::updateSQL($data, "HEXAM_ENTREMARK_HDAT", $where);
            } else {
                $data["SCHREGNO"][TEXT] = $row["SCHREGNO"];
                $query = Query::insertSQL($data, "HEXAM_ENTREMARK_HDAT");
            }
            $db->query($query);
        }
        $db->commit();
        Query::dbCheckIn($db);


        return;
    }
}
