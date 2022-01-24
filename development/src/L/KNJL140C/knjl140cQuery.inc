<?php

require_once('for_php7.php');

class knjl140cQuery extends Query
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

    //課程学科
    public function getTotalcd($year, $flg = "")
    {
        $query  = " SELECT ";
        $query .= "     COURSECD || MAJORCD AS VALUE, ";
        $query .= "     COURSECD || MAJORCD || ':' || COURSENAME || ' ' ||  MAJORNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_COURSE_MAJOR_MST  ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

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
        } else {
            $query .= " LEFT JOIN ENTEXAM_SETTING_MST L1 ";
            $query .= "      ON  L1.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
            $query .= "     AND L1.APPLICANTDIV = T1.APPLICANTDIV ";
            $query .= "     AND L1.SETTING_CD = 'L013' ";
            $query .= "     AND L1.SEQ = T1.JUDGEMENT "; //合格
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
            $query .= " AND L1.NAMESPARE1 IS NOT NULL ";
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
        } elseif ($model->schoolName == "hagoromo") {
            $query .= "     B030.REMARK2 AS SCHREGNO, "; //合格受験番号(入学コース決定)
        } else {
            $query .= "     T1.EXAMNO AS SCHREGNO, ";
        }
        if ($model->schoolName == "seijyo" || $model->schoolName == "kwansei" || $model->schoolName == "jyoto") {
            $query .= "     '1' AS ENTDIV, ";
        } elseif ($model->schoolName == "nichi-ni") {
            $query .= "     CASE  ";
            $query .= "         WHEN T1.APPLICANTDIV = '2' AND T1.TESTDIV = '01' THEN '2' "; //学校種別が高校、入試区分が01:推薦の場合
            $query .= "         ELSE '1' ";                                                 //中学または高校一般入試の場合
            $query .= "     END AS ENTDIV, ";
        } else {
            $query .= "     '' AS ENTDIV, ";
        }
        if ($model->schoolName == "nichi-ni") {
            $query .= "     CASE WHEN T1.APPLICANTDIV = '1' THEN '01' ELSE '04' END AS GRADE, ";
        } elseif ($model->schoolName == "jyoto") {
            $query .= "     '01' AS GRADE, ";
        } else {
            $query .= "     '' AS GRADE, ";
        }
        $query .= "     '' AS HR_CLASS, ";
        $query .= "     '' AS ATTENDNO, ";
        if ($model->Properties["use_inout_external_KNJL140C"] == "1") {
            $query .= "     '1' AS INOUTCD, ";
        } else {
            $query .= "     '0' AS INOUTCD, ";
        }
        if ($model->schoolName == "nichi-ni") {
            $query .= "     CASE WHEN T1.APPLICANTDIV = '1' THEN '0' ELSE '1' END AS COURSECD, ";
            $query .= "     '100' AS MAJORCD, ";
            $query .= "     ''    AS COURSECODE, ";
        } elseif ($model->schoolName == "jyoto") {
            $query .= "     T1.SUC_COURSECD AS COURSECD, ";
            $query .= "     T1.SUC_MAJORCD AS MAJORCD, ";
            $query .= "     '' AS COURSECODE, ";
        } else {
            $query .= "     L0.REMARK1 AS COURSECD, ";
            $query .= "     L0.REMARK2 AS MAJORCD, ";
            $query .= "     L0.REMARK3 AS COURSECODE, ";
        }
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
        if ($model->schoolName == "jyoto") {
            $query .= "     (CASE WHEN T1.DORMITORY_FLG IS NOT NULL THEN T1.SEX ELSE NULL END) AS DORMITORY_CD, "; //入寮コード
            $query .= "     GEN04.REMARK4 AS REDUCTION_DIV_CD, "; //校納金コード
            $query .= "     T1.EXAMNO AS EXAMNO, ";
        } elseif ($model->schoolName == "hagoromo") {
            $query .= "     B030.REMARK2 AS EXAMNO, "; //合格受験番号(入学コース決定)
        } else {
            $query .= "     T1.EXAMNO AS EXAMNO, ";
        }
        $query .= "     T1.TESTDIV, ";
        $query .= "     T1.PRISCHOOLCD, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        if ($model->field["OUTPUTSCORE"] == 1) {
            if ($model->Properties["useScoreMongonDeviation"] == "1") {
                if ($model->schoolName == "tokiwagi") {
                    $query .= "    , SEQ036.REMARK1 AS DEVIATION ";
                } else {
                    $query .= "    , LR.LINK_JUDGE_DEVIATION AS DEVIATION ";
                }
            } else {
                if ($model->schoolName == "nichi-ni") {
                    $query .= "    , RD009.REMARK6 AS SCORE ";
                } else {
                    $query .= "    , LR.TOTAL4 AS SCORE ";
                }
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
        if ($model->schoolName == "jyoto") {
            //特待コード取得のための結合
            $query .= " LEFT JOIN ENTEXAM_RECEPT_DAT RECEPT ";
            $query .= "      ON RECEPT.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
            $query .= "     AND RECEPT.APPLICANTDIV = T1.APPLICANTDIV ";
            $query .= "     AND RECEPT.TESTDIV      = T1.TESTDIV "; //BASEとRECEPTが一対一という前提においてTESTDIVを結合条件に指定
            $query .= "     AND RECEPT.EXAM_TYPE    = '1' ";
            $query .= "     AND RECEPT.EXAMNO       = T1.EXAMNO ";
            $query .= " LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RD015 ";
            $query .= "      ON RD015.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ";
            $query .= "     AND RD015.APPLICANTDIV = RECEPT.APPLICANTDIV ";
            $query .= "     AND RD015.TESTDIV      = RECEPT.TESTDIV ";
            $query .= "     AND RD015.EXAM_TYPE    = RECEPT.EXAM_TYPE ";
            $query .= "     AND RD015.RECEPTNO     = RECEPT.RECEPTNO ";
            $query .= "     AND RD015.SEQ          = '015' ";
            $query .= " LEFT JOIN ENTEXAM_GENERAL_MST GEN04 ";
            $query .= "      ON GEN04.ENTEXAMYEAR  = RD015.ENTEXAMYEAR ";
            $query .= "     AND GEN04.APPLICANTDIV = RD015.APPLICANTDIV ";
            $query .= "     AND GEN04.TESTDIV      = '0' ";
            $query .= "     AND GEN04.GENERAL_DIV  = '04' ";
            $query .= "     AND GEN04.GENERAL_CD   = RD015.REMARK4 ";
        } elseif ($model->schoolName == "hagoromo") {
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
        } else {
            $query .= " LEFT JOIN ENTEXAM_SETTING_MST L2 ";
            $query .= "      ON  L2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
            $query .= "     AND L2.APPLICANTDIV = T1.APPLICANTDIV ";
            $query .= "     AND L2.SETTING_CD = 'L013' ";
            $query .= "     AND L2.SEQ = T1.JUDGEMENT "; //合格
        }
        if ($model->Properties["use_BD026_REMARK1_AS_SCHREGNO"] == "1") {
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
            if ($model->schoolName == "nichi-ni") {
                $query .= "     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RD009 ";
                $query .= "        ON RD009.ENTEXAMYEAR  = LR.ENTEXAMYEAR ";
                $query .= "       AND RD009.APPLICANTDIV = LR.APPLICANTDIV ";
                $query .= "       AND RD009.TESTDIV      = LR.TESTDIV ";
                $query .= "       AND RD009.EXAM_TYPE    = LR.EXAM_TYPE ";
                $query .= "       AND RD009.RECEPTNO     = LR.RECEPTNO ";
                $query .= "       AND RD009.SEQ          = '009' ";
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
        if ($model->schoolName == "jyoto") {
            //入学手続終了者
            $query .= "   AND T1.PROCEDUREDIV = '1' ";
            $query .= "   AND T1.ENTDIV       = '1' ";
            if ($model->field["OUTPUTDIV"] == "1" || $model->field["OUTPUTDIV"] == "2") { //入学者・保護者
                $query .= "   AND T1.SUC_COURSECD || T1.SUC_MAJORCD = '{$model->field["ENTER_TOTALCD"]}' ";
            } elseif ($model->field["OUTPUTDIV"] == "3") { //入寮希望者
                $query .= "   AND T1.DORMITORY_FLG IS NOT NULL ";
            } elseif ($model->field["OUTPUTDIV"] == "4") { //特待合格者
                $query .= "   AND RD015.REMARK4 IS NOT NULL ";
                $query .= "   AND RD015.REMARK5 IS NOT NULL ";
            }
        } elseif ($model->schoolName == "hagoromo") {
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
            $query .= "   AND L2.NAMESPARE1 IS NOT NULL ";
        }
        if ($model->schoolName == "jyoto") {
            //条件なし
        } elseif ($model->goukaku == 'ALL_J') {
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
        } elseif ($model->schoolName == "hagoromo") {
            $query .= "     B030.REMARK2 AS SCHREGNO, "; //合格受験番号(入学コース決定)
        } else {
            $query .= "     T1.EXAMNO AS SCHREGNO, ";
        }
        if ($model->schoolName == "kwansei") {
            $query .= "     '1' AS ENTDIV, ";
        }
        $query .= "     value(L1.GRELATIONSHIP, '01') AS GRELATIONSHIP, ";
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
        $query .= "     value(L1.GRELATIONSHIP, '01') AS GUARANTOR_GRELATIONSHIP, ";
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
        } else {
            $query .= " LEFT JOIN ENTEXAM_SETTING_MST L2 ";
            $query .= "      ON  L2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
            $query .= "     AND L2.APPLICANTDIV = T1.APPLICANTDIV ";
            $query .= "     AND L2.SETTING_CD = 'L013' ";
            $query .= "     AND L2.SEQ = T1.JUDGEMENT "; //合格
        }
        if ($model->Properties["use_BD026_REMARK1_AS_SCHREGNO"] == "1") {
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
        if ($model->schoolName == "jyoto") {
            //入学手続終了者
            $query .= "   AND T1.PROCEDUREDIV = '1' ";
            $query .= "   AND T1.ENTDIV       = '1' ";
            if ($model->field["OUTPUTDIV"] == "1" || $model->field["OUTPUTDIV"] == "2") {
                $query .= "   AND T1.SUC_COURSECD || T1.SUC_MAJORCD = '{$model->field["ENTER_TOTALCD"]}' ";
            }
        } elseif ($model->schoolName == "hagoromo") {
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
            $query .= "   AND L2.NAMESPARE1 IS NOT NULL ";
        }
        if ($model->schoolName == "jyoto") {
            //条件なし
        } elseif ($model->goukaku == 'ALL_J') {
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
        $query .= " LEFT JOIN ENTEXAM_SETTING_MST L2 ";
        $query .= "      ON  L2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "     AND L2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "     AND L2.SETTING_CD = 'L013' ";
        $query .= "     AND L2.SEQ = T1.JUDGEMENT "; //合格
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
        $query .= "   AND L2.NAMESPARE1 IS NOT NULL ";

        $query .= " ORDER BY ";
        $query .= $this->selected_data;

        return $query;
    }
}
