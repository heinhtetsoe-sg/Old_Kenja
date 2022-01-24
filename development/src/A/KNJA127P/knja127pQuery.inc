<?php

require_once('for_php7.php');

class knja127pQuery extends Query {
    //所見データ取得
    function getTrainRemarkData($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     L1.TOTALREMARK, ";
        $query .= "     L1.ATTENDREC_REMARK, ";
        $query .= "     L1.FOREIGNLANGACT4, ";
        $query .= "     L2.REMARK1 AS BEHAVEREC_REMARK, ";
        $query .= "     L3.REMARK1 AS ENT_DISABILITY_REMARK ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN HTRAINREMARK_P_DAT L1 ON T1.YEAR      = L1.YEAR ";
        $query .= "                                    AND T1.SCHREGNO  = L1.SCHREGNO ";
        $query .= "     LEFT JOIN HTRAINREMARK_DETAIL2_DAT L2 ON T1.YEAR        = L2.YEAR ";
        $query .= "                                          AND T1.SCHREGNO    = L2.SCHREGNO ";
        $query .= "                                          AND L2.HTRAIN_SEQ  = '002' ";
        $query .= "     LEFT JOIN HTRAINREMARK_DETAIL2_HDAT L3 ON T1.SCHREGNO   = L3.SCHREGNO ";
        $query .= "                                           AND L3.HTRAIN_SEQ = '001' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".$model->exp_year."' AND ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //存在チェック -- HTRAINREMARK_P_DAT
    function checkHtrainremarkPDat($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_P_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' ";

        return $query;
    }

    //存在チェック -- HTRAINREMARK_DETAIL2_DAT
    function checkHtrainremarkDetail2Dat($model, $seq) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_DETAIL2_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "     HTRAIN_SEQ  = '".$seq."' ";

        return $query;
    }

    //存在チェック -- HTRAINREMARK_DETAIL2_HDAT
    function checkHtrainremarkDetail2Hdat($model, $seq) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_DETAIL2_HDAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "     HTRAIN_SEQ  = '".$seq."' ";

        return $query;
    }

    //年次の取得
    function getAnnual($model) {
        $query  = " SELECT ";
        $query .= "     ANNUAL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SEMESTER    = '".$model->exp_semester."' ";
        
        return $query;
    }

    //学校種別取得
    function getSchoolKind($model) {
        $query  = " SELECT ";
        $query .= "     T2.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_REGD_GDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = '$model->exp_year' AND ";
        $query .= "     T1.SEMESTER = '$model->exp_semester' AND ";
        $query .= "     T1.GRADE    = T2.GRADE AND ";
        $query .= "     T1.SCHREGNO = '$model->schregno' ";

        return $query;
    }

    //障害の状態を取得
    function getChallengedProfile($model) {
        $query  = "SELECT ";
        $query .= "    CHALLENGED_NAMES, ";
        $query .= "    CHALLENGED_STATUS ";
        $query .= "FROM ";
        $query .= "    SCHREG_CHALLENGED_PROFILE_MAIN_DAT ";
        $query .= "WHERE ";
        $query .= "    SCHREGNO = '".$model->schregno."' ";
        $query .= "AND RECORD_DATE = 'NEW' ";

        return $query;
    }

    //名称マスタ
    function getZ010() {
        $query  = "SELECT ";
        $query .= "    NAME1 ";
        $query .= "FROM ";
        $query .= "    NAME_MST ";
        $query .= "WHERE ";
        $query .= "    NAMECD1 = 'Z010' ";
        $query .= "AND NAMECD2 = '00' ";

        return $query;
    }

    //INSERT
    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();

        //年次の取得
        $annual = $db->getOne(knja127pQuery::getAnnual($model));
        //存在チェック --HTRAINREMARK_P_DAT
        $chk_pdat = $db->getOne(knja127pQuery::checkHtrainremarkPDat($model));

        $data = array();
        if ($chk_pdat > 0) {
            $data["ANNUAL"][TEXT]               = $annual;
            $data["TOTALREMARK"][TEXT]          = $model->field["TOTALREMARK"];
            $data["ATTENDREC_REMARK"][TEXT]     = $model->field["ATTENDREC_REMARK"];
            $data["FOREIGNLANGACT4"][TEXT]      = $model->field["FOREIGNLANGACT4"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR     = '".$model->exp_year."' AND ";
            $where .= "     SCHREGNO = '".$model->schregno."' ";

            $db->query(Query::updateSQL($data, "HTRAINREMARK_P_DAT", $where));
        } else {
            $data["YEAR"][TEXT]                 = $model->exp_year;
            $data["SCHREGNO"][TEXT]             = $model->schregno;
            $data["ANNUAL"][TEXT]               = $annual;
            $data["TOTALREMARK"][TEXT]          = $model->field["TOTALREMARK"];
            $data["ATTENDREC_REMARK"][TEXT]     = $model->field["ATTENDREC_REMARK"];
            $data["FOREIGNLANGACT4"][TEXT]      = $model->field["FOREIGNLANGACT4"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $db->query(Query::insertSQL($data, "HTRAINREMARK_P_DAT"));
        }

        //存在チェック --HTRAINREMARK_DETAIL2_DAT
        $chk_detailD = $db->getOne(knja127pQuery::checkHtrainremarkDetail2Dat($model, "002"));

        $data = array();
        if ($chk_detailD > 0) {
            $data["REMARK1"][TEXT]      = $model->field["BEHAVEREC_REMARK"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR        = '".$model->exp_year."' AND ";
            $where .= "     SCHREGNO    = '".$model->schregno."' AND ";
            $where .= "     HTRAIN_SEQ  = '002' ";

            $db->query(Query::updateSQL($data, "HTRAINREMARK_DETAIL2_DAT", $where));
        } else {
            $data["YEAR"][TEXT]         = $model->exp_year;
            $data["SCHREGNO"][TEXT]     = $model->schregno;
            $data["HTRAIN_SEQ"][TEXT]   = '002';
            $data["REMARK1"][TEXT]      = $model->field["BEHAVEREC_REMARK"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $db->query(Query::insertSQL($data, "HTRAINREMARK_DETAIL2_DAT"));
        }

        //存在チェック --HTRAINREMARK_DETAIL2_HDAT
        $chk_detailH = $db->getOne(knja127pQuery::checkHtrainremarkDetail2Hdat($model, "001"));

        $data = array();
        if ($chk_detailH > 0) {
            $data["REMARK1"][TEXT]      = $model->field["ENT_DISABILITY_REMARK"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $where  = " WHERE ";
            $where .= "     SCHREGNO    = '".$model->schregno."' AND ";
            $where .= "     HTRAIN_SEQ  = '001' ";

            $db->query(Query::updateSQL($data, "HTRAINREMARK_DETAIL2_HDAT", $where));
        } else {
            $data["SCHREGNO"][TEXT]     = $model->schregno;
            $data["HTRAIN_SEQ"][TEXT]   = '001';
            $data["REMARK1"][TEXT]      = $model->field["ENT_DISABILITY_REMARK"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $db->query(Query::insertSQL($data, "HTRAINREMARK_DETAIL2_HDAT"));
        }

        Query::dbCheckIn($db);
        return ;
    }

    //学年名取得
    function getGradeName($model) {
        $query  = " SELECT ";
        $query .= "     GRADE_NAME1 ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     GRADE       = '".$model->grade."' ";

        return $query;
    }

    //定型文学期マスター一覧取得
    function getHtrainRemarkTempSemesDat($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_TEMP_SEMES_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SEMESTER    = '9' AND ";
        $query .= "     GRADE       = '".$model->grade."' AND ";
        $query .= "     DATA_DIV    = '03' ";
        $query .= " ORDER BY ";
        $query .= "     PATTERN_CD ";

        return $query;
    }
}
?>
