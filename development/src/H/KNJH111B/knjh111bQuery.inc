<?php

require_once('for_php7.php');

class knjh111bQuery extends Query {
    //クラス名取得
    function getHrclass($model) {
        $query .= " SELECT ";
        $query .= "     HR_NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_REGD_HDAT L1 ON  L1.YEAR = T1.YEAR ";
        $query .= "                         AND L1.SEMESTER = T1.SEMESTER ";
        $query .= "                         AND L1.GRADE = T1.GRADE ";
        $query .= "                         AND L1.HR_CLASS = T1.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "         T1.SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND T1.YEAR     = '". CTRL_YEAR ."' ";
        $query .= "     AND T1.SEMESTER = '". CTRL_SEMESTER ."' ";

        return $query;
    }

    //一括更新用の生徒一覧を取得
    function getStudent($model) {
        $query  = " SELECT ";
        $query .= "     L1.ATTENDNO || ' ' || T1.SCHREGNO || ' ' || T1.NAME_SHOW AS LABEL, ";
        $query .= "     T1.SCHREGNO AS VALUE, ";
        $query .= "     T1.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_REGD_DAT L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "         L1.YEAR     = '". CTRL_YEAR ."' ";
        $query .= "     AND L1.SEMESTER = '". CTRL_SEMESTER ."' ";
        $query .= "     AND L1.GRADE    = '{$model->grade}' ";
        $query .= "     AND L1.HR_CLASS = '{$model->hr_class}' ";
        $query .= " ORDER BY ";
        $query .= "     L1.ATTENDNO ";

        return $query;
    }

    //資格内容取得
    function getContents() {
        $query  = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'H310' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";
        return $query;
    }

    //資格マスタ
    function getQualifiedMst($Row) {
        $query  = " SELECT ";
        $query .= "     QUALIFIED_CD || '：' || QUALIFIED_NAME || '（' || VALUE(QUALIFIED_ABBV,'　') || '）' AS LABEL, ";
        $query .= "     QUALIFIED_CD AS VALUE ";
        $query .= " FROM ";
        $query .= "     QUALIFIED_MST ";
        $query .= " WHERE ";
        $query .= "     CONDITION_DIV = '{$Row["CONDITION_DIV"]}' ";
        $query .= " ORDER BY ";
        $query .= "     INT(VALUE) ";

        return $query;
    }

    //資格マスタ(MANAGEMENT_FLG)
    function getQualifiedMst_MFlg($Row) {
        $query  = " SELECT ";
        $query .= "     MANAGEMENT_FLG ";
        $query .= " FROM ";
        $query .= "     QUALIFIED_MST ";
        $query .= " WHERE ";
        $query .= "     QUALIFIED_CD = '{$Row["QUALIFIED_CD"]}' ";
        $query .= "     AND VALUE(SAKI_FLG, '0') <> '1' ";

        return $query;
    }

    //主催
    function getPromoter($Row) {
        $query  = " SELECT ";
        $query .= "     PROMOTER ";
        $query .= " FROM ";
        $query .= "     QUALIFIED_MST ";
        $query .= " WHERE ";
        $query .= "     QUALIFIED_CD = '{$Row["QUALIFIED_CD"]}' ";

        return $query;
    }

    //級・段位
    function getRank() {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ' ' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'H312' ";

        return $query;
    }

    //級・段位(QUALIFIED_RESULT_MST)
    function getRankResultMst($qualifiedCd, $model) {

        $query  = " SELECT ";
        $query .= "     RESULT_CD || ' ' || RESULT_NAME AS LABEL, ";
        $query .= "     RESULT_CD AS VALUE ";
        if ($model->cntNotPrintResult > 0) {
            $query .= "     , SCORE ";
        }
        $query .= " FROM ";
        $query .= "     QUALIFIED_RESULT_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR         = '".CTRL_YEAR."' ";
        $query .= "     AND QUALIFIED_CD = '{$qualifiedCd}' ";
        $query .= "     AND CERT_FLG     = 'T' ";//正式フラグが立っているもの

        return $query;
    }
    
    //選択した級・段位
    function getSelectedRank($qualifiedCd, $model)
    {
        $query  = " SELECT ";
        $query .= "     NAME_MST.NAMECD2 AS VALUE, ";
        $query .= "     NAME_MST.NAMECD2 || ' ' || NAME_MST.NAME1 AS LABEL ";
        if ($model->cntNotPrintRank > 0) {
            $query .= "     , QUALIFIED_RANK_DAT.SCORE ";
        }
        $query .= " FROM ";
        $query .= "     QUALIFIED_RANK_DAT ";
        $query .= " INNER JOIN ";
        $query .= "     NAME_MST ";
        $query .= "     ON QUALIFIED_RANK_DAT.RANK = NAME_MST.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     QUALIFIED_CD='" . $qualifiedCd . "' ";
        $query .= "     AND NAME_MST.NAMECD1='H312' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //回数取得
    function getMaxSeq($sch, $finalYear = "") {
        $query  = " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO, ";
        $query .= "     MAX(VALUE(SEQ,0)) + 1 AS SEQ ";
        $query .= " FROM ";
        $query .= "     SCHREG_QUALIFIED_HOBBY_DAT ";
        $query .= " WHERE ";
        if ($finalYear) {
            $query .= "         YEAR = '".$finalYear."' ";
        } else {
            $query .= "         YEAR = '".CTRL_YEAR."' ";
        }
        $query .= "     and SCHREGNO = '".$sch."' ";
        $query .= " GROUP BY ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO ";
        return $query;
    }

    //学籍資格データよりデータを取得
    function getAward($schregno, $model) {
        $query  = " WITH SAKI_T AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     YEAR, ";
        $query .= "     QUALIFIED_CD ";
        $query .= " FROM ";
        $query .= "     QUALIFIED_SETUP_DAT ";
        $query .= " ), MOTO_T AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     YEAR, ";
        $query .= "     SETUP_QUALIFIED_CD AS QUALIFIED_CD ";
        $query .= " FROM ";
        $query .= "     QUALIFIED_SETUP_DAT ";
        $query .= " ), OYA_KO_T AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     SAKI_T.YEAR, ";
        $query .= "     SAKI_T.QUALIFIED_CD ";
        $query .= " FROM ";
        $query .= "     SAKI_T, ";
        $query .= "     MOTO_T ";
        $query .= " WHERE ";
        $query .= "     SAKI_T.YEAR = MOTO_T.YEAR ";
        $query .= "     AND SAKI_T.QUALIFIED_CD = MOTO_T.QUALIFIED_CD ";
        $query .= " ), OYA_T AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     SAKI_T.YEAR, ";
        $query .= "     SAKI_T.QUALIFIED_CD ";
        $query .= " FROM ";
        $query .= "     SAKI_T ";
        $query .= " WHERE ";
        $query .= "     NOT EXISTS ( ";
        $query .= "         SELECT ";
        $query .= "             'x' ";
        $query .= "         FROM ";
        $query .= "             OYA_KO_T ";
        $query .= "         WHERE ";
        $query .= "             SAKI_T.YEAR = OYA_KO_T.YEAR ";
        $query .= "             AND SAKI_T.QUALIFIED_CD = OYA_KO_T.QUALIFIED_CD ";
        $query .= "     ) ";
        $query .= " ), KO_T AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     MOTO_T.YEAR, ";
        $query .= "     MOTO_T.QUALIFIED_CD ";
        $query .= " FROM ";
        $query .= "     MOTO_T ";
        $query .= " WHERE ";
        $query .= "     NOT EXISTS ( ";
        $query .= "         SELECT ";
        $query .= "             'x' ";
        $query .= "         FROM ";
        $query .= "             OYA_KO_T ";
        $query .= "         WHERE ";
        $query .= "             MOTO_T.YEAR = OYA_KO_T.YEAR ";
        $query .= "             AND MOTO_T.QUALIFIED_CD = OYA_KO_T.QUALIFIED_CD ";
        $query .= "     ) ";
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.SEQ, ";
        $query .= "     T1.REGDDATE, ";
        $query .= "     L1.CONDITION_DIV, ";
        $query .= "     L1.SAKI_FLG, ";
        $query .= "     L2.NAME1 AS CONDITION_DIV_NAME, ";
        $query .= "     T1.QUALIFIED_CD, ";
        $query .= "     '' AS SHIKAKU_CD, ";
        $query .= "     L1.QUALIFIED_NAME, ";
        $query .= "     VALUE(L1.QUALIFIED_ABBV,'　') AS QUALIFIED_ABBV, ";
        $query .= "     CASE WHEN OYA_T.QUALIFIED_CD IS NOT NULL ";
        $query .= "          THEN '1' ";
        $query .= "          ELSE '2' ";
        $query .= "     END AS IS_OYA, ";
        $query .= "     CASE WHEN OYA_KO_T.QUALIFIED_CD IS NOT NULL ";
        $query .= "          THEN '1' ";
        $query .= "          ELSE '2' ";
        $query .= "     END AS IS_OYA_KO, ";
        $query .= "     CASE WHEN KO_T.QUALIFIED_CD IS NOT NULL ";
        $query .= "          THEN '1' ";
        $query .= "          ELSE '2' ";
        $query .= "     END AS IS_KO, ";
        $query .= "     L1.PROMOTER, ";
        if ($model->Properties["useQualifiedManagementFlg"] == '1') {
            $query .= "     L1.MANAGEMENT_FLG, ";
        }
        $query .= "     T1.CONTENTS, ";
        $query .= "     '' AS TEST_CD, ";
        $query .= "     T1.RANK, ";
        $query .= "     L3.NAME1 AS RANK_NAME, ";
        $query .= "     T1.REMARK ";
        if ($model->cntNotPrintRank > 0) {
            $query .= "     , L4.SCORE ";
        } else {
            $query .= "     , NULL AS SCORE ";
        }
        $query .= "     , T1.SCORE AS HOBBY_SCORE ";
        $query .= " FROM ";
        $query .= "     SCHREG_QUALIFIED_HOBBY_DAT T1 ";
        $query .= " LEFT JOIN OYA_T ON  T1.YEAR = OYA_T.YEAR ";
        $query .= "      AND T1.QUALIFIED_CD = OYA_T.QUALIFIED_CD ";
        $query .= " LEFT JOIN OYA_KO_T ON  T1.YEAR = OYA_KO_T.YEAR ";
        $query .= "      AND T1.QUALIFIED_CD = OYA_KO_T.QUALIFIED_CD ";
        $query .= " LEFT JOIN KO_T ON  T1.YEAR = KO_T.YEAR ";
        $query .= "      AND T1.QUALIFIED_CD = KO_T.QUALIFIED_CD ";
        $query .= " LEFT JOIN ";
        $query .= "     QUALIFIED_MST L1 ON L1.QUALIFIED_CD = T1.QUALIFIED_CD ";
        $query .= " LEFT JOIN ";
        $query .= "     NAME_MST L2 ON  L2.NAMECD2 = L1.CONDITION_DIV ";
        $query .= "                 AND L2.NAMECD1 = 'H311' ";
        $query .= " LEFT JOIN ";
        $query .= "     NAME_MST L3 ON  L3.NAMECD2 = T1.RANK ";
        $query .= "                 AND L3.NAMECD1 = 'H312' ";
        if ($model->cntNotPrintRank > 0) {
            $query .= " LEFT JOIN ";
            $query .= "     QUALIFIED_RANK_DAT L4 ON L4.QUALIFIED_CD = T1.QUALIFIED_CD ";
            $query .= "                          AND L4.RANK = T1.RANK ";
        }
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '{$schregno}' ";
            if ($model->Properties["useQualifiedManagementFlg"] == '1') {
            $query .= " UNION ";
            $query .= " SELECT ";
            $query .= "     T1.YEAR, ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     0 AS SEQ, ";
            $query .= "     T1.TEST_DATE AS REGDDATE, ";
            $query .= "     L1.CONDITION_DIV, ";
            $query .= "     L1.SAKI_FLG, ";
            $query .= "     L2.NAME1 AS CONDITION_DIV_NAME, ";
            $query .= "     T1.QUALIFIED_CD, ";
            $query .= "     T1.QUALIFIED_CD AS SHIKAKU_CD, ";
            $query .= "     L1.QUALIFIED_NAME, ";
            $query .= "     VALUE(L1.QUALIFIED_ABBV,'　') AS QUALIFIED_ABBV, ";
            $query .= "     '2' AS IS_OYA, ";
            $query .= "     '2' AS IS_OYA_KO, ";
            $query .= "     '2' AS IS_KO, ";
            $query .= "     L1.PROMOTER, ";
            $query .= "     L1.MANAGEMENT_FLG, ";
            $query .= "     '' AS CONTENTS, ";
            $query .= "     T1.TEST_CD, ";
            $query .= "     T1.RESULT_CD AS RANK, ";
            $query .= "     L3.RESULT_NAME AS RANK_NAME, ";
            $query .= "     '' AS REMARK ";
            if ($model->cntNotPrintResult > 0) {
                $query .= "     , L3.SCORE ";
            } else {
                $query .= "     , NULL AS SCORE ";
            }
            $query .= "     , NULL AS HOBBY_SCORE ";
            $query .= " FROM ";
            $query .= "     SCHREG_QUALIFIED_TEST_DAT T1 ";
            $query .= " LEFT JOIN ";
            $query .= "     QUALIFIED_MST L1 ON L1.QUALIFIED_CD = T1.QUALIFIED_CD ";
            $query .= " LEFT JOIN ";
            $query .= "     NAME_MST L2 ON  L2.NAMECD2 = L1.CONDITION_DIV ";
            $query .= "                 AND L2.NAMECD1 = 'H311' ";
            $query .= " LEFT JOIN ";
            $query .= "     QUALIFIED_RESULT_MST L3 ON L3.YEAR         = T1.YEAR ";
            $query .= "                            AND L3.QUALIFIED_CD = T1.QUALIFIED_CD ";
            $query .= "                            AND L3.RESULT_CD    = T1.RESULT_CD ";
            $query .= " WHERE ";
            $query .= "         T1.SCHREGNO = '{$schregno}' ";
            $query .= "     AND L3.CERT_FLG = 'T' ";//正式フラグが立っているもの
            $query .= " ORDER BY ";
            $query .= "     YEAR, ";
            $query .= "     QUALIFIED_CD, ";
            $query .= "     REGDDATE, ";
            $query .= "     SEQ ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     YEAR, ";
            $query .= "     T1.QUALIFIED_CD, ";
            $query .= "     T1.REGDDATE, ";
            $query .= "     T1.SEQ ";
        }

        return $query;
    }

    //学校校種取得
    function getSchoolKind($model) {
        $query  = "SELECT ";
        $query .= "        SCHOOL_KIND ";
        $query .= "FROM ";
        $query .= "        SCHREG_REGD_GDAT ";
        $query .= "WHERE ";
        $query .= "        GRADE = '".$model->grade."' ";
        $query .= "AND      YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //１レコード取得
    function getSchregQualifiedHobbyDat($model) {
        $query  = " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.SEQ, ";
        $query .= "     T1.REGDDATE, ";
        $query .= "     L1.CONDITION_DIV, ";
        $query .= "     T1.QUALIFIED_CD, ";
        $query .= "     L1.PROMOTER, ";
        $query .= "     T1.CONTENTS, ";
        $query .= "     T1.CERTIFICATE, ";
        $query .= "     T1.RANK, ";
        $query .= "     T1.REMARK, ";
        $query .= "     T1.UPDATED, ";
        $query .= "     T1.SCORE AS HOBBY_SCORE ";
        
        $query .= " FROM ";
        $query .= "     SCHREG_QUALIFIED_HOBBY_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     QUALIFIED_MST L1 ON L1.QUALIFIED_CD = T1.QUALIFIED_CD ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '{$model->year}' ";
        $query .= "     AND T1.SEQ      = {$model->seq} ";
        $query .= "     AND T1.SCHREGNO = '{$model->schregno}' ";

        return $query;
    }

    //１レコード取得(SCHREG_QUALIFIED_TEST_DAT)
    function getSchregQualifiedTestDat($model, $insert="") {
        $query  = " SELECT ";
        $query .= "     Q_DAT.YEAR, ";
        $query .= "     Q_DAT.SCHREGNO, ";
        $query .= "     Q_DAT.TEST_DATE AS REGDDATE, ";
        $query .= "     Q_MST.CONDITION_DIV, ";
        $query .= "     Q_DAT.QUALIFIED_CD, ";
        $query .= "     Q_MST.PROMOTER, ";
        $query .= "     Q_DAT.RESULT_CD AS RANK, ";
        $query .= "     HOBBY.SCORE AS HOBBY_SCORE ";
        $query .= " FROM ";
        $query .= "     SCHREG_QUALIFIED_TEST_DAT Q_DAT ";
        $query .= " LEFT JOIN ";
        $query .= "     QUALIFIED_MST Q_MST ON Q_MST.QUALIFIED_CD = Q_DAT.QUALIFIED_CD ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_QUALIFIED_HOBBY_DAT HOBBY ON HOBBY.QUALIFIED_CD = Q_DAT.QUALIFIED_CD ";
        $query .= " WHERE ";
        $query .= "         Q_DAT.YEAR         = '{$model->year}' ";
        $query .= "     AND Q_DAT.SCHREGNO     = '{$model->schregno}' ";
        if ($insert) {
            $query .= "     AND Q_DAT.TEST_DATE    = '{$model->field["REGDDATE"]}' ";
            $query .= "     AND Q_DAT.QUALIFIED_CD = '{$model->field["QUALIFIED_CD"]}' ";
            $query .= "     AND Q_DAT.TEST_CD      = '{$model->field["RANK"]}' ";
        } else {
            $query .= "     AND Q_DAT.TEST_DATE    = '".str_replace("/","-", $model->regddate)."' ";
            $query .= "     AND Q_DAT.QUALIFIED_CD = '{$model->shikakuCd}' ";
            $query .= "     AND Q_DAT.TEST_CD      = '{$model->testCd}' ";
        }

        return $query;
    }

    //資格期限取得
    function getLimitDate($testDate, $sikakuCd, $resultCd) {
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

    // INSERT
    function &getInsertQuery($model,$fields,$schregno) {
        $db = Query::dbCheckOut();

        //学校管理資格フラグが立っている資格CDはSCHREG_QUALIFIED_TEST_DATに追加
        if ($model->Properties["useQualifiedManagementFlg"] == '1' && $model->managementFlg == "1") {
            $limitDate = $db->getOne(knjh111bQuery::getLimitDate($fields["REGDDATE"], $fields["QUALIFIED_CD"], $fields["RANK"]));

            $data = array();
            $data["YEAR"][TEXT]                 = CTRL_YEAR;
            $data["SCHREGNO"][TEXT]             = $schregno;
            $data["TEST_DATE"]["TIMESTAMP"]       = strtr($fields["REGDDATE"],"/","-");
            $data["QUALIFIED_CD"][TEXT]         = $fields["QUALIFIED_CD"];
            $data["TEST_CD"][TEXT]              = $fields["RANK"];
            $data["RESULT_CD"][TEXT]            = $fields["RANK"];
            $data["LIMITED_DATE"]["TIMESTAMP"]    = $limitDate;
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            = "sysdate()";

            $query = Query::insertSQL($data, "SCHREG_QUALIFIED_TEST_DAT");
            $db->query($query);
            $seq = "00";
        } else {
            $query = knjh111bQuery::getMaxSeq($schregno);
            $result = $db->query($query);

            $seq = 1;
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $seq = $row["SEQ"];
            }

            $result->free();

            $data = array();
            $data["YEAR"][TEXT]           = CTRL_YEAR;
            $data["SCHREGNO"][TEXT]       = $schregno;
            $data["SEQ"][NUMBER]          = $seq;
            $data["REGDDATE"]["TIMESTAMP"]  = strtr($fields["REGDDATE"],"/","-");
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $data["CLASSCD"][TEXT]        = $fields["CLASSCD"];
                $data["SCHOOL_KIND"][TEXT]    = $model->schoolkind;
                $data["CURRICULUM_CD"][TEXT]  = '2';
                $data["SUBCLASSCD"][TEXT]     = $fields["SUBCLASSCD"];
            } else {
                $data["SUBCLASSCD"][TEXT]     = $fields["SUBCLASSCD"];
            }
            $data["QUALIFIED_CD"][TEXT]   = $fields["QUALIFIED_CD"];
            $data["RANK"][TEXT]           = $fields["RANK"];
            $data["CERTIFICATE"][TEXT]    = $fields["CERTIFICATE"];
            $data["REMARK"][TEXT]         = $fields["REMARK"];
            $data["SCORE"][NUMBER]        = $fields["HOBBY_SCORE"];
            $data["REGISTERCD"][TEXT]     = STAFFCD;
            $data["UPDATED"][NUMBER]      = "sysdate()";

            $query = Query::insertSQL($data, "SCHREG_QUALIFIED_HOBBY_DAT");
            $db->query($query);
        }
        Query::dbCheckIn($db);
        return $seq;
    }

    // UPDATE
    function &getUpdateQuery($model, $fields, $schregno, $seq) {
        $db = Query::dbCheckOut();

        //学校管理資格フラグが立っている資格CDはSCHREG_QUALIFIED_TEST_DATを更新
        if ($model->Properties["useQualifiedManagementFlg"] == '1' && $model->managementFlg == "1") {
            $limitDate = $db->getOne(knjh111bQuery::getLimitDate($fields["REGDDATE"], $fields["QUALIFIED_CD"], $fields["RANK"]));

            $data = array();
            $data["RESULT_CD"][TEXT]            = $fields["RANK"];
            $data["LIMITED_DATE"]["TIMESTAMP"]    = $limitDate;
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            = "sysdate()";

            $where  = " WHERE ";
            $where .= "         YEAR         = '".$model->year."' ";
            $where .= "     AND SCHREGNO     = '".$schregno."' ";
            $where .= "     AND TEST_DATE    = '".str_replace("/","-", $model->regddate)."' ";
            $where .= "     AND QUALIFIED_CD = '".$model->shikakuCd."' ";
            $where .= "     AND TEST_CD      = '".$model->testCd."' ";

            $query = Query::updateSQL($data, "SCHREG_QUALIFIED_TEST_DAT", $where);
            $db->query($query);
            $seq = "00";
        } else {

            $data["REGDDATE"]["TIMESTAMP"]  = strtr($fields["REGDDATE"],"/","-");
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $data["CLASSCD"][TEXT]        = $fields["CLASSCD"];
                $data["SCHOOL_KIND"][TEXT]    = $model->schoolkind;
                $data["CURRICULUM_CD"][TEXT]  = '2';
                $data["SUBCLASSCD"][TEXT]     = $fields["SUBCLASSCD"];
            } else {
                $data["SUBCLASSCD"][TEXT]     = $fields["SUBCLASSCD"];
            }
            $data["QUALIFIED_CD"][TEXT]   = $fields["QUALIFIED_CD"];
            $data["RANK"][TEXT]           = $fields["RANK"];
            $data["CERTIFICATE"][TEXT]    = $fields["CERTIFICATE"];
            $data["REMARK"][TEXT]         = $fields["REMARK"];
            $data["SCORE"][NUMBER]        = $fields["HOBBY_SCORE"];
            $data["REGISTERCD"][TEXT]     = STAFFCD;
            $data["UPDATED"][NUMBER]      = "sysdate()";

            $where  = " WHERE ";
            $where .= "         YEAR      = '".$model->year."' ";
            $where .= "     AND SEQ       = ".$seq." ";
            $where .= "     AND SCHREGNO  = '".$schregno."' ";

            $db->query(Query::updateSQL($data, "SCHREG_QUALIFIED_HOBBY_DAT", $where));
        }
        Query::dbCheckIn($db);
        return $seq;
    }

    // 一括更新1(現状はインサートと一緒の処理です)
    function getReplaceInsertQuery1($model,$schregno) {
        $db = Query::dbCheckOut();

        //学校管理資格フラグが立っている資格CDはSCHREG_QUALIFIED_TEST_DATを更新
        if ($model->Properties["useQualifiedManagementFlg"] == '1' && $model->managementFlg == "1") {
            $query  = " DELETE FROM ";
            $query .= "     SCHREG_QUALIFIED_TEST_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR         = '".CTRL_YEAR."' ";
            $query .= " AND SCHREGNO     = '".$schregno."' ";
            $query .= " AND TEST_DATE    = '".str_replace("/","-", $model->replace_data["REGDDATE"])."' ";
            $query .= " AND QUALIFIED_CD = '{$model->replace_data["QUALIFIED_CD"]}' ";
            $query .= " AND TEST_CD      = '{$model->replace_data["RANK"]}' ";
            $db->query($query);

            $limitDate = $db->getOne(knjh111bQuery::getLimitDate($model->replace_data["REGDDATE"], $model->replace_data["QUALIFIED_CD"], $model->replace_data["RANK"]));

            $data = array();
            $data["YEAR"][TEXT]                 = CTRL_YEAR;
            $data["SCHREGNO"][TEXT]             = $schregno;
            $data["TEST_DATE"]["TIMESTAMP"]       = str_replace("/","-", $model->replace_data["REGDDATE"]);
            $data["QUALIFIED_CD"][TEXT]         = $model->replace_data["QUALIFIED_CD"];
            $data["TEST_CD"][TEXT]              = $model->replace_data["RANK"];
            $data["RESULT_CD"][TEXT]            = $model->replace_data["RANK"];
            $data["LIMITED_DATE"]["TIMESTAMP"]    = $limitDate;
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            = "sysdate()";

            $query = Query::insertSQL($data, "SCHREG_QUALIFIED_TEST_DAT");
            $db->query($query);
            $seq = "00";

        } else {
            $query = knjh111bQuery::getMaxSeq($schregno);
            $result = $db->query($query);

            $seq = 1;
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $seq = $row["SEQ"];
            }

            $result->free();

            $data["YEAR"][TEXT]           = CTRL_YEAR;
            $data["SCHREGNO"][TEXT]       = $schregno;
            $data["SEQ"][NUMBER]          = $seq;
            $data["REGDDATE"]["TIMESTAMP"]  = strtr($this->replace_data["REGDDATE"],"/","-");
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $data["CLASSCD"][TEXT]        = $this->replace_data["CLASSCD"];
                $data["SCHOOL_KIND"][TEXT]    = $model->schoolkind;
                $data["CURRICULUM_CD"][TEXT]  = '2';
                $data["SUBCLASSCD"][TEXT]     = $this->replace_data["SUBCLASSCD"];
            } else {
                $data["SUBCLASSCD"][TEXT]     = $this->replace_data["SUBCLASSCD"];
            }
            $data["QUALIFIED_CD"][TEXT]   = $this->replace_data["QUALIFIED_CD"];
            $data["RANK"][TEXT]           = $this->replace_data["RANK"];
            $data["CERTIFICATE"][TEXT]    = $this->replace_data["CERTIFICATE"];
            $data["REMARK"][TEXT]         = $this->replace_data["REMARK"];
            $data["SCORE"][NUMBER]        = $this->replace_data["HOBBY_SCORE"];
            $data["REGISTERCD"][TEXT]     = STAFFCD;
            $data["UPDATED"][NUMBER]      = "sysdate()";

            $query = Query::insertSQL($data, "SCHREG_QUALIFIED_HOBBY_DAT");
            $db->query($query);
        }
        Query::dbCheckIn($db);
        return $seq;
    }

    // DELETE
    function &getDeleteQuery($regddate, $schregno, $subclasscd, $seq, $year, $model) {
        $db = Query::dbCheckOut();

        //学校管理資格フラグが立っている資格CDはSCHREG_QUALIFIED_TEST_DATを更新
        if ($model->Properties["useQualifiedManagementFlg"] == '1' && $model->managementFlg == "1") {
            $query  = " DELETE FROM ";
            $query .= "     SCHREG_QUALIFIED_TEST_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR         = '{$year}' ";
            $query .= " AND SCHREGNO     = '{$schregno}' ";
            $query .= " AND TEST_DATE    = '".str_replace("/","-", $model->regddate)."' ";
            $query .= " AND QUALIFIED_CD = '{$model->shikakuCd}' ";
            $query .= " AND TEST_CD      = '{$model->testCd}' ";
            $db->query($query);
        } else {
            $query  = " DELETE FROM ";
            $query .= "     SCHREG_QUALIFIED_HOBBY_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR     = '{$year}' ";
            $query .= " AND SEQ      = {$seq} ";
            $query .= " AND SCHREGNO = '{$schregno}' ";
            $db->query($query);
        }
        Query::dbCheckIn($db);
        return $result;
    }

    //元検定にあるか
    function getSetupCnt($model) {
        $query .= " SELECT ";
        $query .= "     QUALIFIED_CD, ";
        $query .= "     SETUP_QUALIFIED_CD ";
        $query .= " FROM ";
        $query .= "     QUALIFIED_SETUP_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SETUP_QUALIFIED_CD = '{$model->field["QUALIFIED_CD"]}' ";

        return $query;
    }

    //先検定
    function getQualifiedHdat($model, $qualifiedCd) {
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     QUALIFIED_SETUP_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND QUALIFIED_CD = '{$qualifiedCd}' ";

        return $query;
    }

    function getSchregQualifiedHobbyDatCnt($model, $qualifiedCd) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     SCHREG_QUALIFIED_HOBBY_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND T1.QUALIFIED_CD = '{$qualifiedCd}' ";

        return $query;
    }

    //対象検定取得
    function getQualifiedDat($model, $qualifiedCd, $cnt) {
        $query .= " SELECT ";
        $query .= "     SUD.SETUP_QUALIFIED_CD, ";
        $query .= "     MAX(HOBBY.YEAR) AS YEAR, ";
        $query .= "     MAX(HOBBY.REGDDATE) AS REGDDATE, ";
        $query .= "     MAX(H312.NAMESPARE1) AS SAKI_RANK, ";
        $query .= "     MAX(HOBBY.SCORE) AS HOBBY_SCORE ";
        $query .= " FROM ";
        $query .= "     QUALIFIED_SETUP_DAT SUD ";
        $query .= "     LEFT JOIN SCHREG_QUALIFIED_HOBBY_DAT HOBBY ON HOBBY.SCHREGNO = '{$model->schregno}' ";
        $query .= "          AND SUD.SETUP_QUALIFIED_CD = HOBBY.QUALIFIED_CD ";
        $query .= "     LEFT JOIN ";
        $query .= "         NAME_MST H312 ON  H312.NAMECD2 = HOBBY.RANK ";
        $query .= "                       AND H312.NAMECD1 = 'H312' ";
        $query .= " WHERE ";
        $query .= "     SUD.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SUD.QUALIFIED_CD = '{$qualifiedCd}' ";
        $query .= "     AND HOBBY.REGDDATE IS NOT NULL ";
        $query .= " GROUP BY ";
        $query .= "     SUD.SETUP_QUALIFIED_CD ";
        $query .= " ORDER BY ";
        $query .= "     REGDDATE DESC ";
        $query .= " FETCH FIRST {$cnt} ROWS ONLY ";

        return $query;
    }

    // INSERT
    function &getInsertSakiQuery($db, $model, $schregno, $qualifiedCd, $finalYear, $finalDate, $setSakiRank, $hobby_score) {

        $query = knjh111bQuery::getMaxSeq($schregno, $finalYear);
        $result = $db->query($query);

        $seq = 1;
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $seq = $row["SEQ"];
        }

        $result->free();

        $data["YEAR"][TEXT]           = $finalYear;
        $data["SCHREGNO"][TEXT]       = $schregno;
        $data["SEQ"][NUMBER]          = $seq;
        $data["REGDDATE"][DATE]       = $finalDate;
        $data["QUALIFIED_CD"][TEXT]   = $qualifiedCd;
        $data["RANK"][TEXT]           = $setSakiRank;
        $data["REGISTERCD"][TEXT]     = STAFFCD;
        $data["SCORE"][NUMBER]        = $hobby_score;
        $data["UPDATED"][NUMBER]      = "sysdate()";

        $query = Query::insertSQL($data, "SCHREG_QUALIFIED_HOBBY_DAT");
        return $query;
    }

    //対象テーブルにNOT_PRINTがあるかどうか取得
    function getSyscatColumns($tableName) {
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
    function getQualifiedMstField($qualifiedCd, $field) {
        $query  = " SELECT ";
        $query .= "     {$field} ";
        $query .= " FROM ";
        $query .= "     QUALIFIED_MST ";
        $query .= " WHERE ";
        $query .= "     QUALIFIED_CD = '{$qualifiedCd}' ";
        return $query;
    }
}
?>
