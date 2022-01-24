<?php

require_once('for_php7.php');

class knja123psQuery extends Query {

    function getNameMst($namecd1) {

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '".$namecd1."' AND ";
        $query .= "     NAMECD2 = '00' ";

        return $query;
    }

    //１レコード取得
    function getTrainRow($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_P_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->left_year."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' ";

        return $query;
    }

    //行動記録の取得
    function getBehavior($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     BEHAVIOR_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->left_year."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "     DIV IN ('3', '4') ";
        $query .= " ORDER BY ";
        $query .= "     DIV, ";
        $query .= "     CODE ";

        return $query;
    }

    function getInkanUmu() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     ATTEST_INKAN_DAT ";
        $query .= " WHERE ";
        $query .= "     '".str_replace("/", "-", CTRL_DATE)."' BETWEEN START_DATE AND VALUE(STOP_DATE, '9999-12-31') ";
        $query .= "     AND STAFFCD = '".STAFFCD."' ";

        return $query;
    }

    function getIneiFlg($model) {
        $query  = " WITH INKAN_T AS ( ";
        $query .= " SELECT ";
        $query .= "     MAX(STAMP_NO) AS STAMP_NO, ";
        $query .= "     STAFFCD ";
        $query .= " FROM ";
        $query .= "     ATTEST_INKAN_DAT ";
        $query .= " WHERE ";
        $query .= "     '".str_replace("/", "-", CTRL_DATE)."' BETWEEN START_DATE AND VALUE(STOP_DATE, '9999-12-31') ";
        $query .= " GROUP BY ";
        $query .= "     STAFFCD ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.CHAGE_OPI_SEQ, ";
        $query .= "     IN1.STAMP_NO AS CHAGE_STAMP, ";
        $query .= "     T1.LAST_OPI_SEQ, ";
        $query .= "     IN2.STAMP_NO AS LAST_STAMP ";
        $query .= " FROM ";
        $query .= "     ATTEST_OPINIONS_WK T1 ";
        $query .= "     LEFT JOIN INKAN_T IN1 ON T1.CHAGE_STAFFCD = IN1.STAFFCD ";
        $query .= "     LEFT JOIN INKAN_T IN2 ON T1.LAST_STAFFCD = IN2.STAFFCD ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '{$model->left_year}' AND ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";

        return $query;
    }

    //担任データ
    function getOpinionsSignature($model) {
        $query  = " SELECT ";
        $query .= "     L1.OPINION AS CHAGE_OPI, ";
        $query .= "     L2.OPINION AS LAST_OPI ";
        $query .= " FROM ";
        $query .= "     ATTEST_OPINIONS_WK T1 ";
        $query .= "     LEFT JOIN ATTEST_OPINIONS_DAT L1 ON T1.YEAR = L1.YEAR ";
        $query .= "          AND T1.CHAGE_OPI_SEQ = L1.SEQ ";
        $query .= "          AND T1.SCHREGNO = L1.SCHREGNO ";
        $query .= "     LEFT JOIN ATTEST_OPINIONS_DAT L2 ON T1.YEAR = L2.YEAR ";
        $query .= "          AND T1.LAST_OPI_SEQ = L2.SEQ ";
        $query .= "          AND T1.SCHREGNO = L2.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->left_year."' ";
        $query .= "     AND T1.SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    function getDeleteWk($model) {
        $query = "DELETE FROM ATTEST_OPINIONS_WK WHERE YEAR = '".$model->left_year."' AND SCHREGNO = '".$model->schregno."'";

        return $query;
    }

    function getSeqNo($model) {
        $query = "SELECT MAX(SEQ) FROM ATTEST_OPINIONS_DAT WHERE YEAR = '{$model->left_year}'";

        return $query;
    }

    //更新処理
    function &getInsUpdSql($db, $model, $seq, $opinion, $signature) {

        //署名クリア
        $query = knja123psQuery::getDeleteWk($model);
        $db->query($query);

        $data = array();
        $data["YEAR"][TEXT]        = $model->left_year;
        $data["SEQ"][NUMBER]       = $seq;
        $data["STAFFCD"][TEXT]     = STAFFCD;
        $data["CERT_NO"][NUMBER]   = 123456;
        $data["OPINION"][TEXT]     = $opinion;   //ハッシュ値
        $data["SIGNATURE"][TEXT]   = $signature; //署名
        $data["RESULT"][NUMBER]    = 0;
        $data["SCHREGNO"][TEXT]    = $model->schregno;
        $data["ACTION"][NUMBER]    = 1;
        $data["STATUS"][NUMBER]    = 3;
        $data["REGISTERCD"][TEXT]  = STAFFCD;
        $data["UPDATED"][FUNC]     = "sysdate()";

        $query = Query::insertSQL($data, "ATTEST_OPINIONS_DAT");
        $db->query($query);

        $query = knja123psQuery::getTrainRow($model);
        $resultTrain = $db->query($query);
        while ($rowTrainP = $resultTrain->fetchRow(DB_FETCHMODE_ASSOC)) {

            $data = array();
            $data["CANCEL_YEAR"][TEXT]        = $model->left_year;
            $data["CANCEL_SEQ"][NUMBER]       = $seq;
            $data["CANCEL_STAFFCD"][TEXT]     = STAFFCD;
            $data["YEAR"][TEXT]               = $rowTrainP["YEAR"];
            $data["SCHREGNO"][TEXT]           = $rowTrainP["SCHREGNO"];
            $data["ANNUAL"][TEXT]             = $rowTrainP["ANNUAL"];
            $data["TOTALSTUDYACT"][TEXT]      = $rowTrainP["TOTALSTUDYACT"];
            $data["TOTALSTUDYVAL"][TEXT]      = $rowTrainP["TOTALSTUDYVAL"];
            $data["SPECIALACTREMARK"][TEXT]   = $rowTrainP["SPECIALACTREMARK"];
            $data["TOTALREMARK"][TEXT]        = $rowTrainP["TOTALREMARK"];
            $data["ATTENDREC_REMARK"][TEXT]   = $rowTrainP["ATTENDREC_REMARK"];
            $data["VIEWREMARK"][TEXT]         = $rowTrainP["VIEWREMARK"];
            $data["BEHAVEREC_REMARK"][TEXT]   = $rowTrainP["BEHAVEREC_REMARK"];
            $data["CLASSACT"][TEXT]           = $rowTrainP["CLASSACT"];
            $data["STUDENTACT"][TEXT]         = $rowTrainP["STUDENTACT"];
            $data["CLUBACT"][TEXT]            = $rowTrainP["CLUBACT"];
            $data["SCHOOLEVENT"][TEXT]        = $rowTrainP["SCHOOLEVENT"];
            $data["FOREIGNLANGACT1"][TEXT]    = $rowTrainP["FOREIGNLANGACT1"];
            $data["FOREIGNLANGACT2"][TEXT]    = $rowTrainP["FOREIGNLANGACT2"];
            $data["FOREIGNLANGACT3"][TEXT]    = $rowTrainP["FOREIGNLANGACT3"];
            $data["FOREIGNLANGACT4"][TEXT]    = $rowTrainP["FOREIGNLANGACT4"];
            $data["REGISTERCD"][TEXT]         = $rowTrainP["REGISTERCD"];
            $data["UPDATED"][FUNC]            = "sysdate()";

            $query = Query::insertSQL($data, "ATTEST_CANCEL_HTRAINREMARK_P_DAT");
            $db->query($query);
        }
        $resultTrain->free();

        $query = knja123psQuery::getBehavior($model);
        $resultBehav = $db->query($query);
        while ($rowBehavior = $resultBehav->fetchRow(DB_FETCHMODE_ASSOC)) {

            $data = array();
            $data["CANCEL_YEAR"][TEXT]        = $model->left_year;
            $data["CANCEL_SEQ"][NUMBER]       = $seq;
            $data["CANCEL_STAFFCD"][TEXT]     = STAFFCD;
            $data["YEAR"][TEXT]               = $rowBehavior["YEAR"];
            $data["SCHREGNO"][TEXT]           = $rowBehavior["SCHREGNO"];
            $data["DIV"][TEXT]                = $rowBehavior["DIV"];
            $data["CODE"][TEXT]               = $rowBehavior["CODE"];
            $data["ANNUAL"][TEXT]             = $rowBehavior["ANNUAL"];
            $data["RECORD"][TEXT]             = $rowBehavior["RECORD"];
            $data["REGISTERCD"][TEXT]         = $rowBehavior["REGISTERCD"];
            $data["UPDATED"][FUNC]            = "sysdate()";

            $query = Query::insertSQL($data, "ATTEST_CANCEL_BEHAVIOR_DAT");
            $db->query($query);
        }
        $resultBehav->free();

        $query = knja123psQuery::getStudyRec($model);
        $resultStudy = $db->query($query);
        while ($rowStudyRec = $resultStudy->fetchRow(DB_FETCHMODE_ASSOC)) {

            $data = array();
            $data["CANCEL_YEAR"][TEXT]        = $model->left_year;
            $data["CANCEL_SEQ"][NUMBER]       = $seq;
            $data["CANCEL_STAFFCD"][TEXT]     = STAFFCD;
            $data["SCHOOLCD"][TEXT]           = $rowStudyRec["SCHOOLCD"];
            $data["YEAR"][TEXT]               = $rowStudyRec["YEAR"];
            $data["SCHREGNO"][TEXT]           = $rowStudyRec["SCHREGNO"];
            $data["ANNUAL"][TEXT]             = $rowStudyRec["ANNUAL"];
            $data["CLASSCD"][TEXT]            = $rowStudyRec["CLASSCD"];
            if ($model->Properties["useCurriculumcd"] == '1') {
                $data["SCHOOL_KIND"][TEXT]        = $rowStudyRec["SCHOOL_KIND"];
                $data["CURRICULUM_CD"][TEXT]      = $rowStudyRec["CURRICULUM_CD"];
            }
            $data["SUBCLASSCD"][TEXT]         = $rowStudyRec["SUBCLASSCD"];
            $data["CLASSNAME"][TEXT]          = $rowStudyRec["CLASSNAME"];
            $data["CLASSABBV"][TEXT]          = $rowStudyRec["CLASSABBV"];
            $data["CLASSNAME_ENG"][TEXT]      = $rowStudyRec["CLASSNAME_ENG"];
            $data["CLASSABBV_ENG"][TEXT]      = $rowStudyRec["CLASSABBV_ENG"];
            $data["SUBCLASSES"][NUMBER]       = $rowStudyRec["SUBCLASSES"];
            $data["SUBCLASSNAME"][TEXT]       = $rowStudyRec["SUBCLASSNAME"];
            $data["SUBCLASSABBV"][TEXT]       = $rowStudyRec["SUBCLASSABBV"];
            $data["SUBCLASSNAME_ENG"][TEXT]   = $rowStudyRec["SUBCLASSNAME_ENG"];
            $data["SUBCLASSABBV_ENG"][TEXT]   = $rowStudyRec["SUBCLASSABBV_ENG"];
            $data["VALUATION"][NUMBER]        = $rowStudyRec["VALUATION"];
            $data["GET_CREDIT"][NUMBER]       = $rowStudyRec["GET_CREDIT"];
            $data["ADD_CREDIT"][NUMBER]       = $rowStudyRec["ADD_CREDIT"];
            $data["COMP_CREDIT"][NUMBER]      = $rowStudyRec["COMP_CREDIT"];
            $data["PRINT_FLG"][TEXT]          = $rowStudyRec["PRINT_FLG"];
            $data["REGISTERCD"][TEXT]         = $rowStudyRec["REGISTERCD"];
            $data["UPDATED"][FUNC]            = "sysdate()";

            $query = Query::insertSQL($data, "ATTEST_CANCEL_SCHREG_STUDYREC_DAT");
            $db->query($query);
        }
        $resultStudy->free();

        $query = knja123psQuery::getAttendRec($model);
        $resultAttend = $db->query($query);
        while ($rowAttendRec = $resultAttend->fetchRow(DB_FETCHMODE_ASSOC)) {

            $data = array();
            $data["CANCEL_YEAR"][TEXT]        = $model->left_year;
            $data["CANCEL_SEQ"][NUMBER]       = $seq;
            $data["CANCEL_STAFFCD"][TEXT]     = STAFFCD;
            $data["SCHOOLCD"][TEXT]           = $rowAttendRec["SCHOOLCD"];
            $data["YEAR"][TEXT]               = $rowAttendRec["YEAR"];
            $data["SCHREGNO"][TEXT]           = $rowAttendRec["SCHREGNO"];
            $data["ANNUAL"][TEXT]             = $rowAttendRec["ANNUAL"];
            $data["SUMDATE"][DATE]            = $rowAttendRec["SUMDATE"];
            $data["CLASSDAYS"][NUMBER]        = $rowAttendRec["CLASSDAYS"];
            $data["OFFDAYS"][NUMBER]          = $rowAttendRec["OFFDAYS"];
            $data["ABSENT"][NUMBER]           = $rowAttendRec["ABSENT"];
            $data["SUSPEND"][NUMBER]          = $rowAttendRec["SUSPEND"];
            $data["MOURNING"][NUMBER]         = $rowAttendRec["MOURNING"];
            $data["ABROAD"][NUMBER]           = $rowAttendRec["ABROAD"];
            $data["REQUIREPRESENT"][NUMBER]   = $rowAttendRec["REQUIREPRESENT"];
            $data["SICK"][NUMBER]             = $rowAttendRec["SICK"];
            $data["ACCIDENTNOTICE"][NUMBER]   = $rowAttendRec["ACCIDENTNOTICE"];
            $data["NOACCIDENTNOTICE"][NUMBER] = $rowAttendRec["NOACCIDENTNOTICE"];
            $data["PRESENT"][NUMBER]            = $rowAttendRec["PRESENT"];
            $data["REGISTERCD"][TEXT]         = $rowAttendRec["REGISTERCD"];
            $data["UPDATED"][FUNC]            = "sysdate()";

            $query = Query::insertSQL($data, "ATTEST_CANCEL_SCHREG_ATTENDREC_DAT");
            $db->query($query);
        }
        $resultAttend->free();
    }

    //学習記録データ取得
    function getStudyRec($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_STUDYREC_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$model->left_year."' AND ";
        $query .= "     SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //出欠記録データ取得
    function getAttendRec($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_ATTENDREC_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$model->left_year."' AND ";
        $query .= "     SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

}
?>
