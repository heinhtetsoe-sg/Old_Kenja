<?php
class knjx_h111aQuery extends Query
{
    //高セキュリティー
    public function getSecurityHigh($model)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID   = '{$model->getPrgId}' AND ";
        $query .= "     INVALID_FLG = '0' ";

        return $query;
    }

    public function getSchoolCd()
    {
        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";

        return $query;
    }

    //年度・学期一覧
    public function getYearSemester()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.YEAR || T1.SEMESTER AS VALUE, ";
        $query .= "     T1.YEAR || '年度 ' || T2.SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= "     INNER JOIN SEMESTER_MST T2 ";
        $query .= "          ON T1.YEAR     = T2.YEAR ";
        $query .= "         AND T1.SEMESTER = T2.SEMESTER ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組一覧
    public function getGradeHrClass($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     GRADE || '-' || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR || SEMESTER = '".$model->field["YEAR"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //生徒一覧
    public function getStudent($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.SCHREGNO AS VALUE, ";
        $query .= "     T1.ATTENDNO || '番 ' || T2.NAME_SHOW AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ";
        $query .= "          ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' AND ";
        $query .= "     T1.GRADE || '-' || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //存在チェック -- SCHREG_REGD_DAT
    public function checkSchregRegdDat($year, $schregno)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$year."' AND ";
        $query .= "     SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     SCHREGNO = '".$schregno."' ";

        return $query;
    }

    //存在チェック -- SCHREG_QUALIFIED_HOBBY_DAT
    public function checkSEQ($year, $schregno, $seq)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_QUALIFIED_HOBBY_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$year."' AND ";
        $query .= "     SCHREGNO = '".$schregno."' AND ";
        $query .= "     SEQ      =  ".$seq." ";

        return $query;
    }

    //存在チェック -- QUALIFIED_MST
    public function checkQualifiedMst($qualified_cd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     QUALIFIED_MST ";
        $query .= " WHERE ";
        $query .= "     QUALIFIED_CD = '".$qualified_cd."' ";

        return $query;
    }

    //存在チェック -- NAME_MST
    public function checkNameMst($namecd1, $namecd2)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '".$namecd1."' AND ";
        $query .= "     NAMECD2 = '".$namecd2."' ";

        return $query;
    }

    //存在チェック -- QUALIFIED_RESULT_MST
    public function checkQualifiedResultMst($year, $qualifiedCd, $resultCd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     QUALIFIED_RESULT_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     QUALIFIED_CD = '".$qualifiedCd."' AND ";
        $query .= "     RESULT_CD = '".$resultCd."' ";

        return $query;
    }

    //存在チェック -- SCHREG_QUALIFIED_TEST_DAT
    public function checkSchregQualifiedTestDatMst($year, $schregno, $testDate, $qualifiedCd, $testCd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_QUALIFIED_TEST_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND SCHREGNO = '".$schregno."' ";
        $query .= "     AND TEST_DATE = '".$testDate."' ";
        $query .= "     AND QUALIFIED_CD = '".$qualifiedCd."' ";
        $query .= "     AND TEST_CD = '".$testCd."' ";

        return $query;
    }

    //資格期限取得
    public function getLimitDate($testDate, $sikakuCd, $resultCd)
    {
        $query  = " SELECT ";
        $query .= "     DATE('".str_replace('/', '-', $testDate)."') + LIMITED_PERIOD MONTHS ";
        $query .= " FROM ";
        $query .= "     QUALIFIED_RESULT_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR         = '".CTRL_YEAR."' ";
        $query .= "     AND QUALIFIED_CD = '".$sikakuCd."' ";
        $query .= "     AND RESULT_CD    = '".$resultCd."' ";

        return $query;
    }

    //MAX値取得(SEQ)
    public function getMaxSeq($year, $schregno)
    {
        $query  = " SELECT ";
        $query .= "     MAX(SEQ) ";
        $query .= " FROM ";
        $query .= "     SCHREG_QUALIFIED_HOBBY_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$year."' AND ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        return $query;
    }

    //資格コード取得
    public function getQualifiedCd($qualified_cd)
    {
        $query  = " SELECT ";
        $query .= "     CONDITION_DIV ";
        $query .= " FROM ";
        $query .= "     QUALIFIED_MST ";
        $query .= " WHERE ";
        $query .= "     QUALIFIED_CD = '".$qualified_cd."'  ";

        return $query;
    }

    //更新（ＣＳＶ取込）
    public function updateQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        if ($model->Properties["useQualifiedManagementFlg"] == '1') {
            for ($i = 0; $i < count($data_arr); $i++) {
                $data = array();
                $limitDate = $db->getOne(knjx_h111aQuery::getLimitDate($data_arr[$i]["REGDDATE"], $data_arr[$i]["QUALIFIED_CD"], $data_arr[$i]["RANK"]));
                if ($model->field["SHORI_MEI"] == "1") {
                    $data["YEAR"][TEXT]             = $data_arr[$i]["YEAR"];
                    $data["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
                    $data["TEST_DATE"][TEXT]        = str_replace("/", "-", $data_arr[$i]["REGDDATE"]);
                    $data["QUALIFIED_CD"][TEXT]     = $data_arr[$i]["QUALIFIED_CD"];
                    $data["TEST_CD"][TEXT]          = $data_arr[$i]["RANK"];
                }
                $data["LIMITED_DATE"][TIMESTAMP]    = $limitDate;
                $data["RESULT_CD"][TEXT]            = $data_arr[$i]["RANK"];
                $data["REGISTERCD"][TEXT]           = STAFFCD;
                $data["UPDATED"][NUMBER]            = "SYSDATE()";

                if ($model->field["SHORI_MEI"] == "1") {
                    //追加
                    $query = Query::insertSQL($data, "SCHREG_QUALIFIED_TEST_DAT");
                } else {
                    $where  = " WHERE ";
                    $where .= "     YEAR         = '".$data_arr[$i]["YEAR"]."' AND ";
                    $where .= "     SCHREGNO     = '".$data_arr[$i]["SCHREGNO"]."' AND ";
                    $where .= "     TEST_DATE    = '".str_replace("/", "-", $data_arr[$i]["REGDDATE"])."' AND ";
                    $where .= "     QUALIFIED_CD = '".$data_arr[$i]["QUALIFIED_CD"]."' AND ";
                    $where .= "     TEST_CD      = '".$data_arr[$i]["RANK"]."' ";
                    //更新
                    $query = Query::updateSQL($data, "SCHREG_QUALIFIED_TEST_DAT", $where);
                }
                $db->query($query);
                $cnt++;
            }
        } else {
            for ($i = 0; $i < count($data_arr); $i++) {
                $data = array();
                if ($model->field["SHORI_MEI"] == "1") {
                    //MAX値取得(SEQ)
                    $maxSeq = $db->getOne(knjx_h111aQuery::getMaxSeq($data_arr[$i]["YEAR"], $data_arr[$i]["SCHREGNO"]));

                    $data["YEAR"][TEXT]             = $data_arr[$i]["YEAR"];
                    $data["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
                    $data["SEQ"][NUMBER]            = strlen($maxSeq) ? $maxSeq+1 : 1;
                }
                $data["REGDDATE"][TEXT]         = str_replace("/", "-", $data_arr[$i]["REGDDATE"]);
                $data["CONDITION_DIV"][TEXT]    = $db->getOne(knjx_h111aQuery::getQualifiedCd($data_arr[$i]["QUALIFIED_CD"]));
                $data["QUALIFIED_CD"][TEXT]     = $data_arr[$i]["QUALIFIED_CD"];
                $data["RANK"][TEXT]             = $data_arr[$i]["RANK"];
                $data["CERTIFICATE"][TEXT]      = $data_arr[$i]["CERTIFICATE"];
                $data["REMARK"][TEXT]           = $data_arr[$i]["REMARK"];
                $data["SCORE"][TEXT]            = $data_arr[$i]["HOBBY_SCORE"];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "SYSDATE()";

                if ($model->field["SHORI_MEI"] == "1") {
                    //追加
                    $query = Query::insertSQL($data, "SCHREG_QUALIFIED_HOBBY_DAT");
                } else {
                    $where  = " WHERE ";
                    $where .= "     YEAR        = '".$data_arr[$i]["YEAR"]."' AND ";
                    $where .= "     SEQ         =  ".$data_arr[$i]["SEQ"]." AND ";
                    $where .= "     SCHREGNO    = '".$data_arr[$i]["SCHREGNO"]."' ";
                    //更新
                    $query = Query::updateSQL($data, "SCHREG_QUALIFIED_HOBBY_DAT", $where);
                }
                $db->query($query);
                $cnt++;
            }
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //削除（ＣＳＶ取込）
    public function deleteQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0; //処理件数
        if ($model->Properties["useQualifiedManagementFlg"] == '1') {
            for ($i = 0; $i < count($data_arr); $i++) {
                $query  = " DELETE FROM ";
                $query .= "     SCHREG_QUALIFIED_TEST_DAT ";
                $query .= "	WHERE ";
                $query .= "	    YEAR         = '".$data_arr[$i]["YEAR"]."' AND ";
                $query .= "	    SCHREGNO     = '".$data_arr[$i]["SCHREGNO"]."' AND ";
                $query .= "	    TEST_DATE    = '".str_replace("/", "-", $data_arr[$i]["REGDDATE"])."' AND ";
                $query .= "	    QUALIFIED_CD = '".$data_arr[$i]["QUALIFIED_CD"]."' AND ";
                $query .= "     TEST_CD      = '".$data_arr[$i]["RANK"]."' ";

                $db->query($query);
                $cnt++;
            }
        } else {
            for ($i = 0; $i < count($data_arr); $i++) {
                $query  = " DELETE FROM ";
                $query .= "     SCHREG_QUALIFIED_HOBBY_DAT ";
                $query .= "	WHERE ";
                $query .= "	    YEAR     = '".$data_arr[$i]["YEAR"]."' AND ";
                $query .= "	    SEQ      =  ".$data_arr[$i]["SEQ"]." AND ";
                $query .= "     SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";

                $db->query($query);
                $cnt++;
            }
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1 = array();
        $data1["PROGRAMID"][TEXT]   = PROGRAMID;
        $data1["MSGROW"][NUMBER]    = $record_no;
        $data1["MSGREMARK"][TEXT]   = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $result = $db->query($query);
    }

    //CSVデータ出力
    public function selectMainQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     L1.GRADE, ";
        $query .= "     L1.HR_CLASS, ";
        $query .= "     L1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     L2.NAME, ";
        $query .= "     L2.NAME_KANA, ";
        $query .= "     T1.REGDDATE, ";
        $query .= "     T1.SEQ, ";
        $query .= "     M1.CONDITION_DIV, ";
        $query .= "     N2.NAME1 AS CONDITION_DIV_NAME,  ";
        $query .= "     T1.QUALIFIED_CD, ";
        $query .= "     M1.QUALIFIED_NAME, ";
        $query .= "     T1.RANK, ";
        $query .= "     N1.NAME1 AS RANK_NAME, ";
        if ($this->showScore == 1) {
            if ($model->cntNotPrintRank > 0) {
                $query .= "     L4.SCORE, ";
            } else {
                $query .= "     NULL AS SCORE, ";
            }
        }
        $query .= "     T1.SCORE AS HOBBY_SCORE, ";
        $query .= "     M1.PROMOTER, ";
        $query .= "     T1.CERTIFICATE, ";
        $query .= "     T1.REMARK, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     SCHREG_QUALIFIED_HOBBY_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT L1 ";
        $query .= "          ON L1.YEAR     = T1.YEAR ";
        $query .= "         AND L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "         AND L1.YEAR || L1.SEMESTER = '".$model->field["YEAR"]."' ";
        if ($model->field["GRADE_HR_CLASS"] != "ALL") {
            $query .= "         AND L1.GRADE || '-' || L1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        }
        $query .= "     LEFT JOIN SCHREG_BASE_MST L2 ";
        $query .= "          ON L2.SCHREGNO = L1.SCHREGNO ";
        $query .= "     LEFT JOIN QUALIFIED_MST M1 ";
        $query .= "          ON M1.QUALIFIED_CD     = T1.QUALIFIED_CD ";
        $query .= "     LEFT JOIN NAME_MST N1 ";
        $query .= "          ON N1.NAMECD1 = 'H312' ";
        $query .= "         AND N1.NAMECD2 = T1.RANK ";
        $query .= "     LEFT JOIN NAME_MST N2 ";
        $query .= "          ON N2.NAMECD1 = 'H311' ";
        $query .= "         AND N2.NAMECD2 = M1.CONDITION_DIV ";
        if ($model->cntNotPrintRank > 0) {
            $query .= " LEFT JOIN ";
            $query .= "     QUALIFIED_RANK_DAT L4 ON L4.QUALIFIED_CD = T1.QUALIFIED_CD ";
            $query .= "                          AND L4.RANK = T1.RANK ";
        }
        if ($model->field["STUDENT"] != "ALL") {
            $query .= " WHERE ";
            $query .= "     T1.SCHREGNO = '".$model->field["STUDENT"]."' ";
        }
        if ($model->Properties["useQualifiedManagementFlg"] == '1') {
            $query .= " UNION ";
            $query .= " SELECT ";
            $query .= "     T1.YEAR, ";
            $query .= "     L1.GRADE, ";
            $query .= "     L1.HR_CLASS, ";
            $query .= "     L1.ATTENDNO, ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     L2.NAME, ";
            $query .= "     L2.NAME_KANA, ";
            $query .= "     T1.TEST_DATE AS REGDDATE, ";
            $query .= "     0 AS SEQ, ";
            $query .= "     M1.CONDITION_DIV, ";
            $query .= "     N1.NAME1 AS CONDITION_DIV_NAME, ";
            $query .= "     T1.QUALIFIED_CD, ";
            $query .= "     M1.QUALIFIED_NAME, ";
            $query .= "     T1.RESULT_CD AS RANK, ";
            $query .= "     M2.RESULT_NAME AS RANK_NAME, ";
            if ($this->showScore == 1) {
                if ($model->cntNotPrintResult > 0) {
                    $query .= "     M2.SCORE, ";
                } else {
                    $query .= "     NULL AS SCORE, ";
                }
            }
            $query .= "     NULL AS HOBBY_SCORE, ";
            $query .= "     M1.PROMOTER, ";
            $query .= "     '' AS CERTIFICATE, ";
            $query .= "     '' AS REMARK, ";
            $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
            $query .= " FROM ";
            $query .= "     SCHREG_QUALIFIED_TEST_DAT T1 ";
            $query .= "     INNER JOIN SCHREG_REGD_DAT L1 ";
            $query .= "          ON L1.YEAR     = T1.YEAR ";
            $query .= "         AND L1.SCHREGNO = T1.SCHREGNO ";
            $query .= "         AND L1.YEAR || L1.SEMESTER = '".$model->field["YEAR"]."' ";
            if ($model->field["GRADE_HR_CLASS"] != "ALL") {
                $query .= "         AND L1.GRADE || '-' || L1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
            }
            $query .= "     LEFT JOIN SCHREG_BASE_MST L2 ";
            $query .= "          ON L2.SCHREGNO = L1.SCHREGNO ";
            $query .= "     LEFT JOIN QUALIFIED_MST M1 ";
            $query .= "          ON M1.QUALIFIED_CD     = T1.QUALIFIED_CD ";
            $query .= "     LEFT JOIN NAME_MST N1 ";
            $query .= "          ON N1.NAMECD2 = M1.CONDITION_DIV ";
            $query .= "         AND N1.NAMECD1 = 'H311' ";
            $query .= "     LEFT JOIN QUALIFIED_RESULT_MST M2 ";
            $query .= "          ON M2.YEAR         = T1.YEAR ";
            $query .= "         AND M2.QUALIFIED_CD = T1.QUALIFIED_CD ";
            $query .= "         AND M2.RESULT_CD    = T1.RESULT_CD ";
            $query .= " WHERE ";
            $query .= "     M2.CERT_FLG     = 'T' ";
            if ($model->field["STUDENT"] != "ALL") {
                $query .= "     AND T1.SCHREGNO = '".$model->field["STUDENT"]."' ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     GRADE, ";
        $query .= "     HR_CLASS, ";
        $query .= "     ATTENDNO, ";
        $query .= "     SCHREGNO, ";
        $query .= "     REGDDATE, ";
        $query .= "     SEQ ";

        return $query;
    }
    
    //対象テーブルにNOT_PRINTがあるかどうか取得
    public function getSyscatColumns($tableName)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(COLNAME) ";
        $query .= " FROM ";
        $query .= "     SYSCAT.COLUMNS ";
        $query .= " WHERE ";
        $query .= "     TABNAME = '".$tableName."' ";
        $query .= "     AND COLNAME = 'NOT_PRINT' ";

        return $query;
    }
    
    //QUALIFIED_MSTの取得(指定項目)
    public function getQualifiedMstField($qualifiedCd, $field)
    {
        $query  = " SELECT ";
        $query .= "     {$field} ";
        $query .= " FROM ";
        $query .= "     QUALIFIED_MST ";
        $query .= " WHERE ";
        $query .= "     QUALIFIED_CD = '{$qualifiedCd}' ";
        return $query;
    }
}
