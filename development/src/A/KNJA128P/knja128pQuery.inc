<?php

require_once('for_php7.php');

class knja128pQuery extends Query
{
    //所見データ取得
    public function getHandicap($model)
    {
        $query  = " SELECT ";
        $query .= "     HANDICAP ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$model->schregno."' ";

        return $query;
    }
    //所見データ取得
    public function getTrainRemarkData($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     L1.FOREIGNLANGACT1, ";
        $query .= "     L1.FOREIGNLANGACT2, ";
        $query .= "     L1.FOREIGNLANGACT3, ";
        $query .= "     L1.FOREIGNLANGACT4, ";
        $query .= "     L1.TOTALREMARK, ";
        $query .= "     L1.TOTALSTUDYACT, ";
        $query .= "     L1.VIEWREMARK, ";
        $query .= "     L1.TOTALSTUDYVAL, ";
        $query .= "     L1.ATTENDREC_REMARK, ";
        $query .= "     L2.REMARK1 AS INDEPENDENT_REMARK, ";
        $query .= "     L3.REMARK1 AS ENT_DISABILITY_REMARK, ";
        $query .= "     L1.SPECIALACTREMARK ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN HTRAINREMARK_P_DAT L1 ON T1.YEAR      = L1.YEAR ";
        $query .= "                                    AND T1.SCHREGNO  = L1.SCHREGNO ";
        $query .= "     LEFT JOIN HTRAINREMARK_DETAIL2_DAT L2 ON T1.YEAR        = L2.YEAR ";
        $query .= "                                          AND T1.SCHREGNO    = L2.SCHREGNO ";
        $query .= "                                          AND L2.HTRAIN_SEQ  = '001' ";
        $query .= "     LEFT JOIN HTRAINREMARK_DETAIL2_HDAT L3 ON T1.SCHREGNO   = L3.SCHREGNO ";
        $query .= "                                           AND L3.HTRAIN_SEQ = '001' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".$model->exp_year."' AND ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //存在チェック -- HTRAINREMARK_P_DAT
    public function checkHtrainremarkPDat($model)
    {
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
    public function checkHtrainremarkDetail2Dat($model, $seq)
    {
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
    public function checkHtrainremarkDetail2Hdat($model, $seq)
    {
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
    public function getAnnual($model)
    {
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

    //学年(GRADE_CD)の取得
    public function getGradecd($model)
    {
        $query  = " SELECT ";
        $query .= "     T2.GRADE_CD ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_REGD_GDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = '$model->exp_year' AND ";
        $query .= "     T1.SEMESTER = '$model->exp_semester' AND ";
        $query .= "     T1.GRADE    = T2.GRADE AND ";
        $query .= "     T1.SCHREGNO = '$model->schregno' AND ";
        $query .= "     T2.SCHOOL_KIND = 'P' ";

        return $query;
    }

    //学校種別取得
    public function getSchoolKind($model)
    {
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

    //名称マスタ取得
    public function getNameMst($model, $namecd1, $namecd2 = "")
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->exp_year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= "     AND NAMECD2 = '".$namecd2."' ";
        }

        return $query;
    }

    //INSERT
    public function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();

        //年次の取得
        $annual = $db->getOne(knja128pQuery::getAnnual($model));
        //存在チェック --HTRAINREMARK_P_DAT
        $chk_pdat = $db->getOne(knja128pQuery::checkHtrainremarkPDat($model));

        $data = array();
        if ($chk_pdat > 0) {
            $data["ANNUAL"][TEXT]               = $annual;
            $data["TOTALSTUDYACT"][TEXT]        = $model->field["TOTALSTUDYACT"];
            $data["TOTALSTUDYVAL"][TEXT]        = $model->field["TOTALSTUDYVAL"];
            $data["TOTALREMARK"][TEXT]          = $model->field["TOTALREMARK"];
            $data["ATTENDREC_REMARK"][TEXT]     = $model->field["ATTENDREC_REMARK"];
            $data["VIEWREMARK"][TEXT]           = $model->field["VIEWREMARK"];
            if ($model->is_gaikokugo1) {
                $data["FOREIGNLANGACT1"][TEXT]      = $model->field["FOREIGNLANGACT1_2"];
            } else {
                $data["FOREIGNLANGACT1"][TEXT]      = $model->field["FOREIGNLANGACT1"];
                $data["FOREIGNLANGACT2"][TEXT]      = $model->field["FOREIGNLANGACT2"];
                $data["FOREIGNLANGACT3"][TEXT]      = $model->field["FOREIGNLANGACT3"];
            }
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
            $data["TOTALSTUDYACT"][TEXT]        = $model->field["TOTALSTUDYACT"];
            $data["TOTALSTUDYVAL"][TEXT]        = $model->field["TOTALSTUDYVAL"];
            $data["TOTALREMARK"][TEXT]          = $model->field["TOTALREMARK"];
            $data["ATTENDREC_REMARK"][TEXT]     = $model->field["ATTENDREC_REMARK"];
            $data["VIEWREMARK"][TEXT]           = $model->field["VIEWREMARK"];
            if ($model->is_gaikokugo1) {
                $data["FOREIGNLANGACT1"][TEXT]      = $model->field["FOREIGNLANGACT1_2"];
            } else {
                $data["FOREIGNLANGACT1"][TEXT]      = $model->field["FOREIGNLANGACT1"];
                $data["FOREIGNLANGACT2"][TEXT]      = $model->field["FOREIGNLANGACT2"];
                $data["FOREIGNLANGACT3"][TEXT]      = $model->field["FOREIGNLANGACT3"];
            }
            $data["FOREIGNLANGACT4"][TEXT]      = $model->field["FOREIGNLANGACT4"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $db->query(Query::insertSQL($data, "HTRAINREMARK_P_DAT"));
        }

        //存在チェック --HTRAINREMARK_DETAIL2_DAT
        $chk_detailD = $db->getOne(knja128pQuery::checkHtrainremarkDetail2Dat($model, "001"));

        $data = array();
        if ($chk_detailD > 0) {
            $data["REMARK1"][TEXT]      = $model->field["INDEPENDENT_REMARK"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR        = '".$model->exp_year."' AND ";
            $where .= "     SCHREGNO    = '".$model->schregno."' AND ";
            $where .= "     HTRAIN_SEQ  = '001' ";

            $db->query(Query::updateSQL($data, "HTRAINREMARK_DETAIL2_DAT", $where));
        } else {
            $data["YEAR"][TEXT]         = $model->exp_year;
            $data["SCHREGNO"][TEXT]     = $model->schregno;
            $data["HTRAIN_SEQ"][TEXT]   = '001';
            $data["REMARK1"][TEXT]      = $model->field["INDEPENDENT_REMARK"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $db->query(Query::insertSQL($data, "HTRAINREMARK_DETAIL2_DAT"));
        }

        //存在チェック --HTRAINREMARK_DETAIL2_HDAT
        $chk_detailH = $db->getOne(knja128pQuery::checkHtrainremarkDetail2Hdat($model, "001"));

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

    //行動記録の取得
    public function getBehavior($model)
    {
        $query  = " SELECT ";
        $query .= "     DIV, ";
        $query .= "     CODE, ";
        $query .= "     ANNUAL, ";
        $query .= "     RECORD ";
        $query .= " FROM ";
        $query .= "     BEHAVIOR_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     DIV, ";
        $query .= "     CODE ";

        return $query;
    }

    //INSERT
    public function &getInsertQuery2($model)
    {
        $db = Query::dbCheckOut();

        //BEHAVIOR_DAT削除
        $query  = " DELETE FROM ";
        $query .= "     BEHAVIOR_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "     DIV IN ('3', '4') ";

        $db->query($query);

        //年次の取得
        $annual = $db->getOne(knja128pQuery::getAnnual($model));

        //BEHAVIOR_DAT追加
        for ($j=3; $j<5; $j++) {
            $cnt = ($j == "3") ? "11" : "5";

            for ($i=1; $i<$cnt; $i++) {
                $ival = $j . sprintf("%02d", $i);
                if ($model->record["RECORD"][$ival] != "1") {
                    continue;
                }

                $data = array();
                $data["YEAR"][TEXT]         = $model->exp_year;
                $data["SCHREGNO"][TEXT]     = $model->schregno;
                $data["DIV"][TEXT]          = $j;
                $data["CODE"][TEXT]         = sprintf("%02d", $i);
                $data["ANNUAL"][TEXT]       = $annual;
                $data["RECORD"][TEXT]       = "1";
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";

                $db->query(Query::insertSQL($data, "BEHAVIOR_DAT"));
            }
        }

        //存在チェック --HTRAINREMARK_P_DAT
        $chk_pdat = $db->getOne(knja128pQuery::checkHtrainremarkPDat($model));

        $data = array();
        if ($chk_pdat > 0) {
            $data["ANNUAL"][TEXT]               = $annual;
            $data["SPECIALACTREMARK"][TEXT]     = $model->field["SPECIALACTREMARK"];
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
            $data["SPECIALACTREMARK"][TEXT]     = $model->field["SPECIALACTREMARK"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $db->query(Query::insertSQL($data, "HTRAINREMARK_P_DAT"));
        }

        Query::dbCheckIn($db);
        return ;
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

    //定型文学期マスター一覧取得
    public function getHtrainRemarkTempSemesDat($model)
    {
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
