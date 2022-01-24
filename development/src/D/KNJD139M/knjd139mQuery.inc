<?php

require_once('for_php7.php');

class knjd139mQuery extends Query {

    //学期取得
    function getSemester($model) {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //校種取得
    function getSchoolKind($model) {
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR  = '{$model->exp_year}' ";
        $query .= "     AND GRADE = '{$model->grade}' ";
        return $query;
    }

    //総合、出欠の備考、総合（評価）取得
    function getHreportremarkDat($model) {
        $query  = " SELECT ";
        $query .= "     H1.TOTALSTUDYTIME, ";
        $query .= "     H1.ATTENDREC_REMARK, ";
        $query .= "     H2.REMARK4 as REMARK4_07_00, ";
        $query .= "     H1.COMMUNICATION ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT H1 ";
        $query .= "     LEFT JOIN HREPORTREMARK_DETAIL_DAT H2 ON H2.YEAR     = H1.YEAR ";
        $query .= "                                          AND H2.SEMESTER = H1.SEMESTER ";
        $query .= "                                          AND H2.SCHREGNO = H1.SCHREGNO ";
        $query .= "                                          AND H2.DIV      = '07' ";
        $query .= "                                          AND H2.CODE     = '00' ";
        $query .= " WHERE ";
        $query .= "         H1.YEAR     = '{$model->exp_year}' ";
        $query .= "     AND H1.SEMESTER = '{$model->semester}' ";
        $query .= "     AND H1.SCHREGNO = '{$model->schregno}' ";

        return $query;
    }

    //行動の記録
    function getBehaviorSemesMst($model) {
        $query  = " SELECT ";
        $query .= "     BMST.CODE, ";
        $query .= "     BMST.CODENAME, ";
        $query .= "     BDAT.RECORD ";
        $query .= " FROM ";
        $query .= "     BEHAVIOR_SEMES_MST BMST ";
        $query .= "     LEFT JOIN BEHAVIOR_SEMES_DAT BDAT ON BDAT.YEAR     = BMST.YEAR ";
        $query .= "                                      AND BDAT.SEMESTER = '{$model->semester}' ";
        $query .= "                                      AND BDAT.SCHREGNO = '{$model->schregno}' ";
        $query .= "                                      AND BDAT.CODE     = BMST.CODE ";
        $query .= " WHERE ";
        $query .= "         BMST.YEAR  = '{$model->exp_year}' ";
        $query .= "     AND BMST.GRADE = '{$model->grade}' ";
        $query .= " ORDER BY ";
        $query .= "     CODE ";

        return $query;
    }

    //名称マスタデータ取得
    function getNameMst($year, $namecd1, $namecd2="") {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE,";
        $query .= "     NAME1 || ':' || NAME2 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '{$year}' ";
        $query .= "     AND NAMECD1 = '{$namecd1}' ";
        if ($namecd2) {
            $query .= " AND NAMECD2 = '{$namecd2}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //観点名称、データ取得
    function getKantenName($model) {
        $query  = " SELECT ";
        $query .= "     D101.NAMECD2, ";
        $query .= "     D101.NAME1 AS LABEL, ";
        $query .= "     HREP.REMARK1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST D101 ";
        $query .= "     LEFT JOIN HREPORTREMARK_DETAIL_DAT HREP ON HREP.YEAR     = D101.YEAR ";
        $query .= "                                            AND HREP.SEMESTER = '{$model->semester}' ";
        $query .= "                                            AND HREP.SCHREGNO = '{$model->schregno}' ";
        $query .= "                                            AND HREP.DIV      = '07' ";
        $query .= "                                            AND HREP.CODE     = D101.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "         D101.YEAR    = '{$model->exp_year}' ";
        $query .= "     AND D101.NAMECD1 = 'D101' ";
        if($model->schoolKind == 'J'){
            $query .= "     AND D101.NAMESPARE1 IS NOT NULL ";
            $query .= " ORDER BY ";
            $query .= "     D101.NAMESPARE1 ";
        } else if($model->schoolKind == 'H'){
            $query .= "     AND D101.NAMESPARE2 IS NOT NULL ";
            $query .= " ORDER BY ";
            $query .= "     D101.NAMESPARE2 ";
        }


        return $query;
    }

    /************/
    /* 更新処理 */
    /************/
    //INSERT -- HREPORTREMARK_DAT
    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //存在チェック
        $chkHrep   = $db->getOne(knjd139mQuery::getHreportremarkDatCnt($model));

        /** HREPORTREMARK_DAT */
        $data = array();
        $data["TOTALSTUDYTIME"][TEXT]   = $model->field["TOTALSTUDYTIME"];
        $data["ATTENDREC_REMARK"][TEXT] = $model->field["ATTENDREC_REMARK"];
        $data["COMMUNICATION"][TEXT]    = $model->field["COMMUNICATION"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        if ($chkHrep < 1) {
            $data["YEAR"][TEXT]         = $model->exp_year;
            $data["SEMESTER"][TEXT]     = $model->semester;
            $data["SCHREGNO"][TEXT]     = $model->schregno;

            $query = Query::insertSQL($data, "HREPORTREMARK_DAT");
        } else {
            $where  = " WHERE ";
            $where .= "         YEAR     = '{$model->exp_year}' ";
            $where .= "     AND SEMESTER = '{$model->semester}' ";
            $where .= "     AND SCHREGNO = '{$model->schregno}' ";

            $query = Query::updateSQL($data, "HREPORTREMARK_DAT", $where);
        }
        $db->query($query);

        // 観点(9学期は評価)【HREPORTREMARK_DETAIL_DAT】
        $detailDiv = '07';
        if ($model->semester == '9') {
            $detailCode = '00';
            //存在チェック
            $chkDetail = $db->getOne(knjd139mQuery::getHreportremarkDetaelDatCnt($model, $detailDiv, $detailCode));

            $data = array();
            $data["REMARK4"][TEXT]      = $model->field["REMARK4_07_00"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            if ($chkDetail < 1) {
                $data["YEAR"][TEXT]     = $model->exp_year;
                $data["SEMESTER"][TEXT] = $model->semester;
                $data["SCHREGNO"][TEXT] = $model->schregno;
                $data["DIV"][TEXT]      = $detailDiv;
                $data["CODE"][TEXT]     = $detailCode;

                $query = Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT");
            } else {
                $where  = " WHERE ";
                $where .= "         YEAR     = '{$model->exp_year}' ";
                $where .= "     AND SEMESTER = '{$model->semester}' ";
                $where .= "     AND SCHREGNO = '{$model->schregno}' ";
                $where .= "     AND DIV      = '{$detailDiv}' ";
                $where .= "     AND CODE     = '{$detailCode}' ";

                $query = Query::updateSQL($data, "HREPORTREMARK_DETAIL_DAT", $where);
            }
            $db->query($query);
        } else {
            foreach ($model->kantenArr as $nameCd2 => $valArr) {
                //存在チェック
                $chkDetail  = $db->getOne(knjd139mQuery::getHreportremarkDetaelDatCnt($model, $detailDiv, $nameCd2));

                $data = array();
                $data["REMARK1"][TEXT]      = $model->field["REMARK1_".$nameCd2];
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";

                if ($chkDetail < 1) {
                    $data["YEAR"][TEXT]     = $model->exp_year;
                    $data["SEMESTER"][TEXT] = $model->semester;
                    $data["SCHREGNO"][TEXT] = $model->schregno;
                    $data["DIV"][TEXT]      = $detailDiv;
                    $data["CODE"][TEXT]     = $nameCd2;

                    $query = Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT");
                } else {
                    $where  = " WHERE ";
                    $where .= "         YEAR     = '{$model->exp_year}' ";
                    $where .= "     AND SEMESTER = '{$model->semester}' ";
                    $where .= "     AND SCHREGNO = '{$model->schregno}' ";
                    $where .= "     AND DIV      = '{$detailDiv}' ";
                    $where .= "     AND CODE     = '{$nameCd2}' ";

                    $query = Query::updateSQL($data, "HREPORTREMARK_DETAIL_DAT", $where);
                }
                $db->query($query);
            }
        }

        // 行動の記録【BEHAVIOR_SEMES_DAT】
        foreach ($model->recordArr as $code => $valArr) {
            //存在チェック
            $chkBehav  = $db->getOne(knjd139mQuery::getBehaviorSemesDatCnt($model, $code));

            $data = array();
            $data["RECORD"][TEXT]       = $model->field["RECORD".$code];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            if ($chkBehav < 1) {
                $data["YEAR"][TEXT]     = $model->exp_year;
                $data["SEMESTER"][TEXT] = $model->semester;
                $data["SCHREGNO"][TEXT] = $model->schregno;
                $data["CODE"][TEXT]     = $code;

                $query = Query::insertSQL($data, "BEHAVIOR_SEMES_DAT");
            } else {
                $where  = " WHERE ";
                $where .= "         YEAR     = '{$model->exp_year}' ";
                $where .= "     AND SEMESTER = '{$model->semester}' ";
                $where .= "     AND SCHREGNO = '{$model->schregno}' ";
                $where .= "     AND CODE     = '{$code}' ";

                $query = Query::updateSQL($data, "BEHAVIOR_SEMES_DAT", $where);
            }
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return ;
    }

    //カウント取得【HREPORTREMARK_DAT】
    function getHreportremarkDatCnt($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '{$model->exp_year}' ";
        $query .= "     AND SEMESTER = '{$model->semester}' ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";

        return $query;
    }

    //カウント取得【BEHAVIOR_SEMES_DAT】
    function getBehaviorSemesDatCnt($model, $code) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     BEHAVIOR_SEMES_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '{$model->exp_year}' ";
        $query .= "     AND SEMESTER = '{$model->semester}' ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND CODE     = '{$code}' ";

        return $query;
    }

    //カウント取得【HREPORTREMARK_DETAIL_DAT】
    function getHreportremarkDetaelDatCnt($model, $div, $code) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '{$model->exp_year}' ";
        $query .= "     AND SEMESTER = '{$model->semester}' ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND DIV      = '{$div}' ";
        $query .= "     AND CODE     = '{$code}' ";

        return $query;
    }

    /**************/
    /* 参照ボタン */
    /**************/
    //総合学習、出欠備考参照
    function getHreportRemrkDatSansyo($model) {
        $fieldName = ($model->cmd == 'totalStudy') ? 'TOTALSTUDYTIME': 'ATTENDREC_REMARK';

        $query  = " SELECT ";
        $query .= "     S1.SEMESTER, ";
        $query .= "     S1.SEMESTERNAME, ";
        $query .= "     H1.{$fieldName} as VALUE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST S1 ";
        $query .= "     LEFT JOIN HREPORTREMARK_DAT H1 ON H1.YEAR     = S1.YEAR ";
        $query .= "                                   AND H1.SEMESTER = S1.SEMESTER ";
        $query .= "                                   AND H1.SCHREGNO = '{$model->schregno}' ";
        $query .= " WHERE ";
        $query .= "         S1.YEAR      = '{$model->exp_year}' ";
        $query .= "     AND S1.SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }
    /************************/
}
?>
