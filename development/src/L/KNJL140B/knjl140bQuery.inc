<?php

require_once('for_php7.php');
class knjl140bQuery extends Query
{
    //学校名取得
    public function getSchoolName()
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";

        return $query;
    }

    //学校名取得
    public function getNameMst($namecd1)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1, ";
        $query .= "     ABBV1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //校種取得(入学コース取得用)
    public function getVNameMst($model, $namecd1)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1, ";
        $query .= "     ABBV1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->examyear}' ";
        $query .= "     AND NAMECD1 = '{$namecd1}' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //入学コースコンボ
    public function getGoukaku($model)
    {
        $query  = " SELECT ";
        $query .= "     L0.REMARK1    || ";
        $query .= "     L0.REMARK2    || ";
        $query .= "     L0.REMARK3    || ";
        $query .= "     ':'               || ";
        $query .= "     L3.COURSENAME     || ";
        $query .= "     L4.MAJORNAME      || ";
        $query .= "     ' '               || ";
        $query .= "     L5.COURSECODENAME AS LABEL, ";
        $query .= "     L0.REMARK1 || ':' || ";
        $query .= "     L0.REMARK2 || ':' || ";
        $query .= "     L0.REMARK3 AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT L0 ON L0.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        if ($model->schoolName == "hagoromo") {
            $query .= "                                    AND L0.APPLICANTDIV = T1.APPLICANTDIV ";
        }
        $query .= "                                        AND L0.EXAMNO = T1.EXAMNO ";
        $query .= "                                        AND L0.SEQ = '007' ";
        if ($model->schoolName == "hagoromo") {
            $query .= " LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT B022 ";
            $query .= "      ON B022.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
            $query .= "     AND B022.APPLICANTDIV = T1.APPLICANTDIV ";
            $query .= "     AND B022.EXAMNO = T1.EXAMNO ";
            $query .= "     AND B022.SEQ = '022' ";
            $query .= " LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT B030 ";
            $query .= "      ON B030.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
            $query .= "     AND B030.APPLICANTDIV = T1.APPLICANTDIV ";
            $query .= "     AND B030.EXAMNO = T1.EXAMNO ";
            $query .= "     AND B030.SEQ = '030' ";
        }
        if ($model->schoolName == "hirokoudai") {
            $query .= " LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ022 ";
            $query .= "      ON SEQ022.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
            $query .= "     AND SEQ022.EXAMNO       = T1.EXAMNO ";
            $query .= "     AND SEQ022.SEQ          = '022' ";
        }
        $query .= " LEFT JOIN ";
        $query .= "     COURSE_MST L3 ON L3.COURSECD =  L0.REMARK1 ";
        $query .= " LEFT JOIN ";
        $query .= "     MAJOR_MST L4 ON  L4.COURSECD =  L0.REMARK1 ";
        $query .= "                  AND L4.MAJORCD  =  L0.REMARK2 ";
        $query .= " LEFT JOIN ";
        $query .= "     COURSECODE_MST L5 ON L5.COURSECODE =  L0.REMARK3 ";
        $query .= " INNER JOIN ";
        $query .= "     V_NAME_MST L003 ON T1.ENTEXAMYEAR  = L003.YEAR ";
        $query .= "                    AND T1.APPLICANTDIV = L003.NAMECD2 ";
        $query .= "                    AND L003.NAMECD1    = 'L003' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "                    AND L003.NAMESPARE3 IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "                    AND L003.NAMESPARE3 = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$model->examyear}' ";
        if ($model->schoolName == "hagoromo") {
            $query .= " AND T1.ENTDIV IS NOT NULL ";            //入学コース(入学コース決定)
            $query .= " AND B030.REMARK1 IS NOT NULL ";         //合格試験(入学コース決定)
            $query .= " AND B030.REMARK2 IS NOT NULL ";         //合格受験番号(入学コース決定)
            $query .= " AND T1.PROCEDUREDIV = '1' ";            //払込フラグ(チェックON)
            $query .= " AND VALUE(B022.REMARK2,'0') != '1' ";   //辞退フラグ(チェックOFF)
            $query .= " AND L0.REMARK1 IS NOT NULL ";           //課程
            $query .= " AND L0.REMARK2 IS NOT NULL ";           //学科
            $query .= " AND L0.REMARK3 IS NOT NULL ";           //コース
        } else {
            if ($model->schoolName == "bunkyo") {
                $query .= " AND T1.PROCEDUREDIV = '1' ";
                //文京(辞退者は対象外)
                $query .= " AND VALUE(T1.ENTDIV, '0') <> '2' ";
            } elseif ($model->schoolName == "hirokoudai") {
                //入学区分
                $query .= " AND T1.PROCEDUREDIV = '2' ";
                //広工大(辞退者は対象外)
                $query .= " AND VALUE(SEQ022.REMARK2, '0') <> '1' ";
            } else {
                $query .= " AND T1.PROCEDUREDIV = '1' ";
                //入学有り
                $query .= " AND T1.ENTDIV       = '1' ";
            }
        }
        $query .= " GROUP BY ";
        $query .= "     L0.REMARK1, ";
        $query .= "     L0.REMARK2, ";
        $query .= "     L0.REMARK3, ";
        $query .= "     L3.COURSENAME, ";
        $query .= "     L4.MAJORNAME, ";
        $query .= "     L5.COURSECODENAME ";
        $query .= " ORDER BY ";
        $query .= "     L0.REMARK1, ";
        $query .= "     L0.REMARK2, ";
        $query .= "     L0.REMARK3 ";

        return $query;
    }

    //入学者のCSV
    public function selectCvsStudentQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.ENTEXAMYEAR, ";
        if ($model->Properties["use_BD026_REMARK1_AS_SCHREGNO"] == "1") {
            $query .= "     SEQ026.REMARK1 AS SCHREGNO, ";
        } elseif ($model->schoolName == "seijyo") {
            $query .= "     SEQ026.REMARK1 AS SCHREGNO, ";
            $query .= "     '1' AS ENTDIV, ";
        } elseif ($model->schoolName == "hagoromo") {
            $query .= "     B030.REMARK2 AS SCHREGNO, "; //合格受験番号(入学コース決定)
            $query .= "     '' AS ENTDIV, ";
        } elseif ($model->schoolName == "risshisha") {
            $query .= "     SEQ026.REMARK1 AS SCHREGNO, ";
            $query .= "     '' AS ENTDIV, ";
        } elseif ($model->schoolName == "kwansei") {
            $query .= "     SEQ026.REMARK1 AS SCHREGNO, ";
            $query .= "     '1' AS ENTDIV, ";
        } else {
            $query .= "     T1.EXAMNO AS SCHREGNO, ";
            $query .= "     '' AS ENTDIV, ";
        }
        if ($model->Properties["use_no_Grade_FRESHMAN_DAT"] != "1") {
            $query .= "     '' AS GRADE, ";
        }
        $query .= "     '' AS HR_CLASS, ";
        $query .= "     '' AS ATTENDNO, ";
        if ($model->schoolName == "sundaikoufu") {
            $query .= "   CASE WHEN L0.REMARK3 IN ('4000', '9000') THEN '1' ELSE '0' END AS INOUTCD, ";
        } elseif ($model->schoolName == "hagoromo") {
            $query .= "   CASE WHEN T1.FS_CD = '0009951' THEN '0' ELSE '1' END AS INOUTCD, ";
        } else {
            $query .= "  '0' AS INOUTCD, ";
        }
        $query .= "     L0.REMARK1 AS COURSECD, ";
        $query .= "     L0.REMARK2 AS MAJORCD, ";
        $query .= "     L0.REMARK3 AS COURSECODE, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     CAST(REPLACE(CAST(T1.BIRTHDAY AS CHAR(10)), '-', '/') AS CHAR(10)) AS BIRTHDAY, ";
        $query .= "     T1.SEX, "; //コードで出力する
        if ($model->schoolName == "ohka") {
            $query .= "     S1.FINSCHOOLCD AS FS_CD, ";
            $query .= "     FIN_M.FINSCHOOL_NAME, ";
        } else {
            $query .= "     T1.FS_CD, ";
            $query .= "     FIN_M.FINSCHOOL_NAME, ";
        }
        if ($model->schoolName == "seijyo" || $model->schoolName == "hagoromo") {
            $query .= "     T1.ENTEXAMYEAR || '/3/31' AS FINSCHOOLGRADDATE, ";
        } else {
            $query .= "     CASE WHEN VALUE(T1.FS_GRDYEAR, '') != '' ";
            $query .= "          THEN FS_GRDYEAR || '/3/31' ";
            $query .= "          ELSE '' ";
            $query .= "     END AS FINSCHOOLGRADDATE, ";
        }
        $query .= "     L1.ZIPCD, ";
        $query .= "     L1.ADDRESS1, ";
        $query .= "     L1.ADDRESS2, ";
        $query .= "     L1.TELNO, ";
        $query .= "     '' AS FAXNO, ";
        $query .= "     '' AS EMAIL, ";
        $query .= "     L1.EMERGENCYCALL, ";
        $query .= "     L1.EMERGENCYTELNO, ";
        $query .= "     '' AS SCALASHIPDIV, ";
        $query .= "     T1.ENTEXAMYEAR AS KATEI_ENTEXAMYEAR, ";
        $query .= "     '' AS TIKUCD, ";
        if ($model->schoolName == "hagoromo") {
            $query .= "     B030.REMARK2 AS EXAMNO, "; //合格受験番号(入学コース決定)
        } else {
            $query .= "     T1.EXAMNO AS EXAMNO, ";
        }
        $query .= "     T1.TESTDIV, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        if ($model->field["OUTPUTSCORE"] == 1) {
            if ($model->Properties["useScoreMongonDeviation"] == "1") {
                if ($model->schoolName == "tokiwagi") {
                    $query .= "    , SEQ036.REMARK1 AS DEVIATION ";
                } else {
                    $query .= "    , LR.LINK_JUDGE_DEVIATION AS DEVIATION ";
                }
            } else {
                $query .= "    , LR.TOTAL4 AS SCORE ";
            }
        }
        if ($model->schoolName == "tokiwagi" && $model->field["OUTPUTSCORE"] == 1) {
            $totalSql = "";
            $totalSep = "";
            foreach ($model->l009 as $kamoku => $kamokuVal) {
                $query .= "    , SCORE{$kamoku}.SCORE AS SCORE{$kamoku} ";
                $totalSql .= $totalSep." VALUE(SCORE{$kamoku}.SCORE, 0) ";
                $totalSep = " + ";
            }
            $query .= " , {$totalSql} AS TOTAL_SCORE ";
        }
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT L0 ON L0.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        if ($model->schoolName == "hagoromo") {
            $query .= "                                    AND L0.APPLICANTDIV = T1.APPLICANTDIV ";
        }
        $query .= "                                        AND L0.EXAMNO       = T1.EXAMNO ";
        $query .= "                                        AND L0.SEQ          = '007' ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_APPLICANTADDR_DAT L1 ON  L1.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        if ($model->schoolName == "hagoromo") {
            $query .= "                              AND L1.APPLICANTDIV = T1.APPLICANTDIV ";
        }
        $query .= "                                  AND L1.EXAMNO       = T1.EXAMNO ";
        if ($model->schoolName == "hagoromo") {
            $query .= " LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT B022 ";
            $query .= "      ON B022.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
            $query .= "     AND B022.APPLICANTDIV = T1.APPLICANTDIV ";
            $query .= "     AND B022.EXAMNO = T1.EXAMNO ";
            $query .= "     AND B022.SEQ = '022' ";
            $query .= " LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT B030 ";
            $query .= "      ON B030.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
            $query .= "     AND B030.APPLICANTDIV = T1.APPLICANTDIV ";
            $query .= "     AND B030.EXAMNO = T1.EXAMNO ";
            $query .= "     AND B030.SEQ = '030' ";
        }
        if ($model->Properties["use_BD026_REMARK1_AS_SCHREGNO"] == "1" || $model->schoolName == "seijyo" || $model->schoolName == "risshisha" || $model->schoolName == "kwansei") {
            $query .= " LEFT JOIN  ";
            $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ026 ON SEQ026.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
            $query .= "                                            AND SEQ026.EXAMNO       = T1.EXAMNO ";
            $query .= "                                            AND SEQ026.SEQ          = '026' ";
        }
        if ($model->schoolName == "tokiwagi") {
            $query .= " LEFT JOIN  ";
            $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ036 ON SEQ036.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
            $query .= "                                            AND SEQ036.EXAMNO       = T1.EXAMNO ";
            $query .= "                                            AND SEQ036.SEQ          = '036' ";
        }
        if ($model->schoolName == "hirokoudai") {
            $query .= " LEFT JOIN  ";
            $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ022 ON SEQ022.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
            $query .= "                                            AND SEQ022.EXAMNO       = T1.EXAMNO ";
            $query .= "                                            AND SEQ022.SEQ          = '022' ";
        }
        if ($model->field["OUTPUTSCORE"] == 1) {
            if ($model->schoolName == "hagoromo") {
                $query .= "     LEFT JOIN ENTEXAM_RECEPT_DAT LR ";
                $query .= "        ON LR.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
                $query .= "       AND LR.APPLICANTDIV = T1.APPLICANTDIV ";
                $query .= "       AND LR.TESTDIV      = B030.REMARK1 ";
                $query .= "       AND LR.RECEPTNO     = B030.REMARK2 ";
                $query .= "       AND LR.EXAMNO       = T1.EXAMNO ";
            } else {
                $query .= "     LEFT JOIN ENTEXAM_RECEPT_DAT LR ";
                $query .= "        ON LR.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
                $query .= "       AND LR.APPLICANTDIV = T1.APPLICANTDIV ";
                $query .= "       AND LR.TESTDIV      = T1.TESTDIV ";
                $query .= "       AND LR.RECEPTNO     = T1.EXAMNO ";
            }
            if ($model->schoolName == "tokiwagi" && $model->field["OUTPUTSCORE"] == 1) {
                foreach ($model->l009 as $kamoku => $kamokuVal) {
                    $query .= "     LEFT JOIN ENTEXAM_SCORE_DAT SCORE{$kamoku} ";
                    $query .= "        ON LR.ENTEXAMYEAR  = SCORE{$kamoku}.ENTEXAMYEAR ";
                    $query .= "       AND LR.APPLICANTDIV = SCORE{$kamoku}.APPLICANTDIV ";
                    $query .= "       AND LR.TESTDIV      = SCORE{$kamoku}.TESTDIV ";
                    $query .= "       AND LR.RECEPTNO     = SCORE{$kamoku}.RECEPTNO ";
                    $query .= "       AND SCORE{$kamoku}.EXAM_TYPE      = '1' ";
                    $query .= "       AND SCORE{$kamoku}.TESTSUBCLASSCD = '{$kamoku}' ";
                }
            }
        }
        if ($model->schoolName == "ohka") {
            $query .= " LEFT JOIN ENTEXAM_SCHOOL_MST S1 ";
            $query .= "     ON  S1.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
            $query .= "     AND S1.ENTEXAM_SCHOOLCD = T1.FS_CD ";
            $query .= " LEFT JOIN FINSCHOOL_MST FIN_M ";
            $query .= "     ON FIN_M.FINSCHOOLCD = S1.FINSCHOOLCD ";
        } else {
            $query .= " LEFT JOIN FINSCHOOL_MST FIN_M";
            $query .= "       ON FIN_M.FINSCHOOLCD = T1.FS_CD ";
        }
        if ($model->schoolName == "sundaikoufu") {
            $query .= " INNER JOIN ";
            $query .= "     V_NAME_MST L003 ON T1.ENTEXAMYEAR  = L003.YEAR ";
            $query .= "                    AND T1.APPLICANTDIV = L003.NAMECD2 ";
            $query .= "                    AND L003.NAMECD1    = 'L003' ";
            $query .= "                    AND L003.NAMESPARE3 = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "       T1.ENTEXAMYEAR  = '{$model->examyear}' ";
        if ($model->schoolName == "hagoromo") {
            $query .= " AND T1.ENTDIV IS NOT NULL ";            //入学コース(入学コース決定)
            $query .= " AND B030.REMARK1 IS NOT NULL ";         //合格試験(入学コース決定)
            $query .= " AND B030.REMARK2 IS NOT NULL ";         //合格受験番号(入学コース決定)
            $query .= " AND T1.PROCEDUREDIV = '1' ";            //払込フラグ(チェックON)
            $query .= " AND VALUE(B022.REMARK2,'0') != '1' ";   //辞退フラグ(チェックOFF)
        } else {
            if ($model->schoolName == "bunkyo") {
                $query .= "   AND T1.PROCEDUREDIV = '1' "; //手続済み
                //文京(辞退者は対象外)
                $query .= "   AND VALUE(T1.ENTDIV, '0') <> '2' ";
            } elseif ($model->schoolName == "hirokoudai") {
                //入学区分
                $query .= "   AND T1.PROCEDUREDIV = '2' ";
                //広工大(辞退者は対象外)
                $query .= "   AND VALUE(SEQ022.REMARK2, '0') <> '1' ";
            } else {
                $query .= "   AND T1.PROCEDUREDIV = '1' "; //手続済み
                //入学有り
                $query .= "   AND T1.ENTDIV       = '1' ";
            }
        }
        if ($model->goukaku == 'ALL_J') {
            $query .= "   AND T1.APPLICANTDIV = '1' ";
        } elseif ($model->goukaku == 'ALL_H') {
            $query .= "   AND T1.APPLICANTDIV = '2' ";
        } elseif ($model->goukaku == 'ALL_1') {
            $query .= "   AND T1.APPLICANTDIV = '1' ";
        } elseif ($model->goukaku == 'ALL_2') {
            $query .= "   AND T1.APPLICANTDIV = '2' ";
        } elseif ($model->goukaku == 'ALL_3') {
            $query .= "   AND T1.APPLICANTDIV = '3' ";
        } else {
            $query .= "   AND L0.REMARK1  = '{$model->suc_coursecd}' ";
            $query .= "   AND L0.REMARK2  = '{$model->suc_majorcd}' ";
            $query .= "   AND L0.REMARK3  = '{$model->suc_coursecode}' ";
        }
        $query .= " ORDER BY ";
        $query .= $this->selected_data;

        return $query;
    }

    //保護者のCSV
    public function selectCvsGuardQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     '' AS GRADE, ";
        $query .= "     '' AS HR_CLASS, ";
        $query .= "     '' AS ATTENDNO, ";
        $query .= "     T1.NAME, ";
        if ($model->Properties["use_BD026_REMARK1_AS_SCHREGNO"] == "1") {
            $query .= "     SEQ026.REMARK1 AS SCHREGNO, ";
        } elseif ($model->schoolName == "seijyo") {
            $query .= "     SEQ026.REMARK1 AS SCHREGNO, ";
        } elseif ($model->schoolName == "hagoromo") {
            $query .= "     B030.REMARK2 AS SCHREGNO, "; //合格受験番号(入学コース決定)
        } elseif ($model->schoolName == "risshisha") {
            $query .= "     SEQ026.REMARK1 AS SCHREGNO, ";
        } elseif ($model->schoolName == "kwansei") {
            $query .= "     SEQ026.REMARK1 AS SCHREGNO, ";
            $query .= "     '1' AS ENTDIV, ";
        } else {
            $query .= "     T1.EXAMNO AS SCHREGNO, ";
        }
        $query .= "     value(L1.RELATIONSHIP, '01') AS RELATIONSHIP, ";
        $query .= "     L1.GNAME, ";
        $query .= "     L1.GKANA, ";
        $query .= "     '' AS SEITO_SEX, ";
        $query .= "     '' AS BIRTHDAY, ";
        $query .= "     L1.GZIPCD, ";
        $query .= "     L1.GADDRESS1, ";
        $query .= "     L1.GADDRESS2, ";
        $query .= "     L1.GTELNO, ";
        $query .= "     L1.GFAXNO, ";
        $query .= "     '' AS EMAIL, ";
        $query .= "     '' AS GUARD_JOBCD, ";
        $query .= "     '' AS GUARD_WORK_NAME, ";
        $query .= "     '' AS GUARD_WORK_TELNO, ";
        $query .= "     value(L1.RELATIONSHIP, '01') AS GUARANTOR_RELATIONSHIP, ";
        $query .= "     L1.GNAME AS GUARANTOR_NAME, ";
        $query .= "     L1.GKANA AS GUARANTOR_KANA, ";
        $query .= "     '' AS GUARANTOR_SEX, ";
        $query .= "     L1.GZIPCD AS GUARANTOR_ZIPCD, ";
        $query .= "     L1.GADDRESS1 AS GUARANTOR_ADDR1, ";
        $query .= "     L1.GADDRESS2 AS GUARANTOR_ADDR2, ";
        $query .= "     L1.GTELNO AS GUARANTOR_TELNO, ";
        $query .= "     '' AS GUARANTOR_JOBCD, ";
        $query .= "     '' AS PUBLIC_OFFICE, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT L0 ON L0.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        if ($model->schoolName == "hagoromo") {
            $query .= "                                    AND L0.APPLICANTDIV = T1.APPLICANTDIV ";
        }
        $query .= "                                        AND L0.EXAMNO       = T1.EXAMNO ";
        $query .= "                                        AND L0.SEQ          = '007' ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_APPLICANTADDR_DAT L1 ON  L1.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        if ($model->schoolName == "hagoromo") {
            $query .= "                              AND L1.APPLICANTDIV = T1.APPLICANTDIV ";
        }
        $query .= "                                  AND L1.EXAMNO       = T1.EXAMNO ";
        if ($model->schoolName == "hagoromo") {
            $query .= " LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT B022 ";
            $query .= "      ON B022.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
            $query .= "     AND B022.APPLICANTDIV = T1.APPLICANTDIV ";
            $query .= "     AND B022.EXAMNO = T1.EXAMNO ";
            $query .= "     AND B022.SEQ = '022' ";
            $query .= " LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT B030 ";
            $query .= "      ON B030.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
            $query .= "     AND B030.APPLICANTDIV = T1.APPLICANTDIV ";
            $query .= "     AND B030.EXAMNO = T1.EXAMNO ";
            $query .= "     AND B030.SEQ = '030' ";
        }
        if ($model->Properties["use_BD026_REMARK1_AS_SCHREGNO"] == "1" || $model->schoolName == "seijyo" || $model->schoolName == "risshisha" || $model->schoolName == "kwansei") {
            $query .= " LEFT JOIN  ";
            $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ026 ON SEQ026.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
            $query .= "                                            AND SEQ026.EXAMNO       = T1.EXAMNO ";
            $query .= "                                            AND SEQ026.SEQ          = '026' ";
        }
        if ($model->schoolName == "hirokoudai") {
            $query .= " LEFT JOIN  ";
            $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ022 ON SEQ022.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
            $query .= "                                            AND SEQ022.EXAMNO       = T1.EXAMNO ";
            $query .= "                                            AND SEQ022.SEQ          = '022' ";
        }
        if ($model->schoolName == "sundaikoufu") {
            $query .= " INNER JOIN ";
            $query .= "     V_NAME_MST L003 ON T1.ENTEXAMYEAR  = L003.YEAR ";
            $query .= "                    AND T1.APPLICANTDIV = L003.NAMECD2 ";
            $query .= "                    AND L003.NAMECD1    = 'L003' ";
            $query .= "                    AND L003.NAMESPARE3 = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "       T1.ENTEXAMYEAR  = '{$model->examyear}' ";
        if ($model->schoolName == "hagoromo") {
            $query .= " AND T1.ENTDIV IS NOT NULL ";            //入学コース(入学コース決定)
            $query .= " AND B030.REMARK1 IS NOT NULL ";         //合格試験(入学コース決定)
            $query .= " AND B030.REMARK2 IS NOT NULL ";         //合格受験番号(入学コース決定)
            $query .= " AND T1.PROCEDUREDIV = '1' ";            //払込フラグ(チェックON)
            $query .= " AND VALUE(B022.REMARK2,'0') != '1' ";   //辞退フラグ(チェックOFF)
            $query .= " AND L0.REMARK1 IS NOT NULL ";           //課程
            $query .= " AND L0.REMARK2 IS NOT NULL ";           //学科
            $query .= " AND L0.REMARK3 IS NOT NULL ";           //コース
        } else {
            if ($model->schoolName == "bunkyo") {
                $query .= "   AND T1.PROCEDUREDIV = '1' "; //手続済み
                //文京(辞退者は対象外)
                $query .= "   AND VALUE(T1.ENTDIV, '0') <> '2' ";
            } elseif ($model->schoolName == "hirokoudai") {
                //入学区分
                $query .= "   AND T1.PROCEDUREDIV = '2' ";
                //広工大(辞退者は対象外)
                $query .= "   AND VALUE(SEQ022.REMARK2, '0') <> '1' ";
            } else {
                $query .= "   AND T1.PROCEDUREDIV = '1' "; //手続済み
                //入学有り
                $query .= "   AND T1.ENTDIV       = '1' ";
            }
        }
        if ($model->goukaku == 'ALL_J') {
            $query .= "   AND T1.APPLICANTDIV = '1' ";
        } elseif ($model->goukaku == 'ALL_H') {
            $query .= "   AND T1.APPLICANTDIV = '2' ";
        } elseif ($model->goukaku == 'ALL_1') {
            $query .= "   AND T1.APPLICANTDIV = '1' ";
        } elseif ($model->goukaku == 'ALL_2') {
            $query .= "   AND T1.APPLICANTDIV = '2' ";
        } else {
            $query .= "   AND L0.REMARK1  = '{$model->suc_coursecd}' ";
            $query .= "   AND L0.REMARK2  = '{$model->suc_majorcd}' ";
            $query .= "   AND L0.REMARK3  = '{$model->suc_coursecode}' ";
        }

        $query .= " ORDER BY ";
        $query .= $this->selected_data;

        return $query;
    }

    //通学手段情報のCSV
    public function selectCsvTugakuQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     '3' AS C1, ";
        $query .= "     '' AS C2, ";
        $query .= "     '' AS C3, ";
        $query .= "     '' AS C4, ";
        $query .= "     '' AS C5, ";
        $query .= "     T1.EXAMNO AS SCHREGNO, ";
        $query .= "     T1.NAME AS NAME, ";
        $query .= "     '' AS C8, ";
        $query .= "     '' AS C9, ";
        $query .= "     '' AS C10, ";
        $query .= "     '' AS C11, ";
        $query .= "     '' AS C12, ";
        $query .= "     '' AS C13, ";
        $query .= "     '' AS C14, ";
        $query .= "     '' AS C15, ";
        $query .= "     '' AS C16, ";
        $query .= "     '' AS C17, ";
        $query .= "     '' AS C18, ";
        $query .= "     '' AS C19, ";
        $query .= "     '' AS C20, ";
        $query .= "     '' AS C21, ";
        $query .= "     '' AS C22, ";
        $query .= "     '' AS C23, ";
        $query .= "     '' AS C24, ";
        $query .= "     '' AS C25, ";
        $query .= "     '' AS C26, ";
        $query .= "     '' AS C27, ";
        $query .= "     '' AS C28, ";
        $query .= "     '' AS C29, ";
        $query .= "     '' AS C30, ";
        $query .= "     '' AS C31, ";
        $query .= "     '' AS C32, ";
        $query .= "     '' AS C33, ";
        $query .= "     '' AS C34, ";
        $query .= "     '' AS C35, ";
        $query .= "     '' AS C36, ";
        $query .= "     '' AS C37, ";
        $query .= "     '' AS C38, ";
        $query .= "     '' AS C39, ";
        $query .= "     '' AS C30, ";
        $query .= "     '' AS C41, ";
        $query .= "     '' AS C42, ";
        $query .= "     '' AS C43, ";
        $query .= "     '' AS C44, ";
        $query .= "     '' AS C45, ";
        $query .= "     '' AS C46, ";
        $query .= "     '' AS C47, ";
        $query .= "     '' AS C48, ";
        $query .= "     '' AS C49, ";
        $query .= "     '' AS C50, ";
        $query .= "     '' AS C51, ";
        $query .= "     '' AS C52, ";
        $query .= "     '' AS C53, ";
        $query .= "     '' AS C54, ";
        $query .= "     '' AS C55, ";
        $query .= "     '' AS C56, ";
        $query .= "     '' AS C57, ";
        $query .= "     '' AS C58, ";
        $query .= "     '' AS C59, ";
        $query .= "     '' AS C60, ";
        $query .= "     '' AS C61, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT L0 ON L0.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "                                        AND L0.EXAMNO       = T1.EXAMNO ";
        $query .= "                                        AND L0.SEQ          = '007' ";
        if ($model->schoolName == "hirokoudai") {
            $query .= " LEFT JOIN  ";
            $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ022 ON SEQ022.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
            $query .= "                                            AND SEQ022.EXAMNO       = T1.EXAMNO ";
            $query .= "                                            AND SEQ022.SEQ          = '022' ";
        }
        if ($model->schoolName == "sundaikoufu") {
            $query .= " INNER JOIN ";
            $query .= "     V_NAME_MST L003 ON T1.ENTEXAMYEAR  = L003.YEAR ";
            $query .= "                    AND T1.APPLICANTDIV = L003.NAMECD2 ";
            $query .= "                    AND L003.NAMECD1    = 'L003' ";
            $query .= "                    AND L003.NAMESPARE3 = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "       T1.ENTEXAMYEAR  = '{$model->examyear}' ";
        if ($model->schoolName == "bunkyo") {
            $query .= "   AND T1.PROCEDUREDIV = '1' "; //手続済み
            //文京(辞退者は対象外)
            $query .= "   AND VALUE(T1.ENTDIV, '0') <> '2' ";
        } elseif ($model->schoolName == "hirokoudai") {
            //入学区分
            $query .= "   AND T1.PROCEDUREDIV = '2' ";
            //広工大(辞退者は対象外)
            $query .= "   AND VALUE(SEQ022.REMARK2, '0') <> '1' ";
        } else {
            $query .= "   AND T1.PROCEDUREDIV = '1' "; //手続済み
            //入学有り
            $query .= "   AND T1.ENTDIV       = '1' ";
        }
        if ($model->goukaku == 'ALL_J') {
            $query .= "   AND T1.APPLICANTDIV = '1' ";
        } elseif ($model->goukaku == 'ALL_H') {
            $query .= "   AND T1.APPLICANTDIV = '2' ";
        } else {
            $query .= "   AND L0.REMARK1  = '{$model->suc_coursecd}' ";
            $query .= "   AND L0.REMARK2  = '{$model->suc_majorcd}' ";
            $query .= "   AND L0.REMARK3  = '{$model->suc_coursecode}' ";
        }

        $query .= " ORDER BY ";
        $query .= $this->selected_data;

        return $query;
    }
}
