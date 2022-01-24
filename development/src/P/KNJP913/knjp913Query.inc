<?php

require_once('for_php7.php');

class knjp913query extends Query {
    //校種取得
    function getSchoolKind($grade) {
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR  = '".CTRL_YEAR."' ";
        $query .= "     AND GRADE = '{$grade}' ";

        return $query;
    }

    //学期一覧取得
    function getSemesterList($model) {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    // 科目項目マスタ情報取得
    function getLevyLMdat($model) {
        $query  = " SELECT ";
        $query .= "     M_MST.LEVY_L_CD || M_MST.LEVY_M_CD as LM_CD, ";
        $query .= "     L_MST.LEVY_L_NAME, ";
        $query .= "     L_MST.LEVY_L_ABBV, ";
        $query .= "     M_MST.LEVY_M_NAME, ";
        $query .= "     M_MST.LEVY_M_ABBV ";
        $query .= " FROM ";
        $query .= "     LEVY_M_MST M_MST ";
        $query .= "     LEFT JOIN LEVY_L_MST L_MST ON L_MST.SCHOOLCD    = M_MST.SCHOOLCD ";
        $query .= "                               AND L_MST.SCHOOL_KIND = M_MST.SCHOOL_KIND ";
        $query .= "                               AND L_MST.YEAR        = M_MST.YEAR ";
        $query .= "                               AND L_MST.LEVY_L_CD   = M_MST.LEVY_L_CD ";
        $query .= " WHERE ";
        $query .= "         M_MST.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND M_MST.SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= "     AND M_MST.YEAR        = '".CTRL_YEAR."' ";

        return $query;
    }

    //学年取得
    function getGrade($model) {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組一覧取得
    function getGradeHrclass($model) {
        $query  = " SELECT ";
        $query .= "     T1.HR_CLASS AS VALUE, ";
        $query .= "     T1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= " LEFT JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR  = T1.YEAR";
        $query .= "                              AND T2.GRADE = T1.GRADE";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND T1.GRADE    = '".$model->field["GRADE"]."' ";
        if(AUTHORITY == DEF_UPDATE_RESTRICT || $AUTHORITY == DEF_REFER_RESTRICT) {
            $query .= "     AND (TR_CD1     = '".STAFFCD."' OR ";
            $query .= "          TR_CD2     = '".STAFFCD."' OR ";
            $query .= "          TR_CD3     = '".STAFFCD."' OR ";
            $query .= "          SUBTR_CD1  = '".STAFFCD."' OR ";
            $query .= "          SUBTR_CD2  = '".STAFFCD."' OR ";
            $query .= "          SUBTR_CD3  = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //繰越項目一覧取得
    function getCarryOverDat($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     INCOME_L_CD || INCOME_M_CD as LM_CD, ";
        $query .= "     LEVY_M_ABBV ";
        $query .= " FROM ";
        $query .= "     LEVY_CARRY_OVER_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= "     AND YEAR        = '".CTRL_YEAR."' ";
        $query .= "     AND SCHREGNO   in ( SELECT ";
        $query .= "                             SCHREGNO ";
        $query .= "                         FROM ";
        $query .= "                             SCHREG_REGD_DAT ";
        $query .= "                         WHERE ";
        $query .= "                                 YEAR     = '".CTRL_YEAR."' ";
        $query .= "                             AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "                             AND GRADE    = '".$model->field["GRADE"]."' ";
        if ($model->field["HR_CLASS"] != '') {
            $query .= "                             AND HR_CLASS = '".$model->field["HR_CLASS"]."' ";
        }
        $query .= "                        ) ";
        $query .= " ORDER BY ";
        $query .= "     LM_CD ";

        return $query;
    }

    //繰越項目データ取得
    function getCarryOverDatSch($model) {
        $query  = " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     INCOME_L_CD || INCOME_M_CD as LM_CD, ";
        $query .= "     CARRY_OVER_MONEY ";
        $query .= " FROM ";
        $query .= "     LEVY_CARRY_OVER_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD     = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND  = '{$model->schoolKind}' ";
        $query .= "     AND YEAR         = '".CTRL_YEAR."' ";
        $query .= "     AND CARRY_CANCEL is null ";
        $query .= "     AND SCHREGNO   in ( SELECT ";
        $query .= "                             SCHREGNO ";
        $query .= "                         FROM ";
        $query .= "                             SCHREG_REGD_DAT ";
        $query .= "                         WHERE ";
        $query .= "                                 YEAR     = '".CTRL_YEAR."' ";
        $query .= "                             AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "                             AND GRADE    = '".$model->field["GRADE"]."' ";
        if ($model->field["HR_CLASS"] != '') {
            $query .= "                             AND HR_CLASS = '".$model->field["HR_CLASS"]."' ";
        }
        $query .= "                        ) ";

        return $query;
    }

    //全体のデータの取得
    function selectQuery($model) {
        $query  = " WITH SCHINFO AS ( ";
        $query .= "     SELECT ";
        $query .= "         REGD.SCHREGNO, ";
        $query .= "         REGD.GRADE, ";
        $query .= "         REGD.HR_CLASS, ";
        $query .= "         REGD.ATTENDNO, ";
        $query .= "         GDAT.SCHOOL_KIND, ";
        $query .= "         BASE.NAME ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT REGD ";
        $query .= "         LEFT JOIN SCHREG_REGD_GDAT GDAT ON REGD.YEAR  = GDAT.YEAR ";
        $query .= "                                        AND REGD.GRADE = GDAT.GRADE, ";
        $query .= "         SCHREG_BASE_MST BASE ";
        $query .= "     WHERE ";
        $query .= "             REGD.YEAR     = '".CTRL_YEAR."' ";
        $query .= "         AND REGD.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "         AND REGD.GRADE    = '{$model->field["GRADE"]}' ";
        if ($model->field["HR_CLASS"] != '') {
            $query .= "         AND REGD.HR_CLASS = '{$model->field["HR_CLASS"]}' ";
        }
        $query .= "         AND REGD.SCHREGNO               = BASE.SCHREGNO ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     SCHI.SCHREGNO, ";
        $query .= "     SCHI.GRADE || SCHI.HR_CLASS || '-' || SCHI.ATTENDNO as ATTENDNO, ";
        $query .= "     SCHI.NAME ";
        $query .= " FROM ";
        $query .= "     SCHINFO SCHI ";
        $query .= " ORDER BY ";
        $query .= "     SCHI.GRADE, ";
        $query .= "     SCHI.HR_CLASS, ";
        $query .= "     SCHI.ATTENDNO ";

        return $query;
    }

    /************/
    /* 更新処理 */
    /************/
    function update($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //繰越テーブル更新
        foreach ($model->updFields as $schregno => $lmCdArr) {
            //チェック無い人は更新しない
            if ($model->field["CHECK_".$schregno] != '1') {
                continue;
            }
            foreach ($lmCdArr as $lmCd => $setMoney) {
                $setMoney = str_replace(array(" ", "　"), "", $setMoney);
                $lCd = substr($lmCd, 0, 2);
                $mCd = substr($lmCd, 2, 2);

                $data = array();
                $data["CARRY_CANCEL"][TEXT]     = ($model->field["SYORI_DIV"] == '1') ? '1': '';
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "sysdate()";

                $where  = " WHERE ";
                $where .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
                $where .= "     AND SCHOOL_KIND = '{$model->schoolKind}' ";
                $where .= "     AND YEAR        = '".CTRL_YEAR."' ";
                $where .= "     AND SCHREGNO    = '{$schregno}' ";
                $where .= "     AND INCOME_L_CD || INCOME_M_CD = '{$lmCd}' ";

                $query = Query::updateSQL($data, "LEVY_CARRY_OVER_DAT", $where);
                $db->query($query);

                //更新
                if ($model->field["SYORI_DIV"] == '1') {
                    if ($setMoney > 0) {
                        //返金伝票作成
                        knjp913Query::getUpdateHenkin($db, $model, $lCd, $mCd, $setMoney, $schregno);
                    }
                } else {
                    //返金伝票削除
                    knjp913Query::getDeleteHenkin($db, $model, $lCd, $mCd, $schregno);
                }
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
    }

    //返金伝票削除
    function getDeleteHenkin($db, $model, $incomeLcd, $incomeMcd, $schregNo) {
        // 削除する伝票番号取得
        $query = knjp913Query::getHenkinRequestNo($model, $incomeLcd, $incomeMcd, $schregNo);
        $requestNo = $db->getOne($query);

        // LEVY_REQUEST_OUTGO_DAT
        $query  = " DELETE FROM LEVY_REQUEST_OUTGO_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= "     AND YEAR        = '".CTRL_YEAR."' ";
        $query .= "     AND OUTGO_L_CD  = '99' ";
        $query .= "     AND OUTGO_M_CD  = '99' ";
        $query .= "     AND REQUEST_NO  = '{$requestNo}' ";
        $db->query($query);

        // LEVY_REQUEST_OUTGO_MEISAI_DAT
        $query  = " DELETE FROM LEVY_REQUEST_OUTGO_MEISAI_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= "     AND YEAR        = '".CTRL_YEAR."' ";
        $query .= "     AND REQUEST_NO  = '{$requestNo}' ";
        $query .= "     AND LINE_NO     = 1 ";
        $query .= "     AND OUTGO_L_CD  = '99' ";
        $query .= "     AND OUTGO_M_CD  = '99' ";
        $query .= "     AND OUTGO_S_CD  = '999' ";
        $db->query($query);

        // LEVY_REQUEST_OUTGO_SCHREG_DAT
        $query  = " DELETE FROM LEVY_REQUEST_OUTGO_SCHREG_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= "     AND YEAR        = '".CTRL_YEAR."' ";
        $query .= "     AND REQUEST_NO  = '{$requestNo}' ";
        $query .= "     AND SCHREGNO    = '{$schregNo}' ";
        $query .= "     AND LINE_NO     = 1 ";
        $query .= "     AND OUTGO_L_CD  = '99' ";
        $query .= "     AND OUTGO_M_CD  = '99' ";
        $query .= "     AND OUTGO_S_CD  = '999' ";
        $db->query($query);

        return;
    }

    //削除する伝票番号取得
    function getHenkinRequestNo($model, $incomeLcd, $incomeMcd, $schregno) {
        $query  = " SELECT ";
        $query .= "     SCHD.REQUEST_NO ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_SCHREG_DAT SCHD";
        $query .= "     LEFT JOIN LEVY_REQUEST_OUTGO_DAT OUTG ON OUTG.SCHOOLCD    = SCHD.SCHOOLCD ";
        $query .= "                                          AND OUTG.SCHOOL_KIND = SCHD.SCHOOL_KIND ";
        $query .= "                                          AND OUTG.YEAR        = SCHD.YEAR ";
        $query .= "                                          AND OUTG.OUTGO_L_CD  = SCHD.OUTGO_L_CD ";
        $query .= "                                          AND OUTG.OUTGO_M_CD  = SCHD.OUTGO_M_CD ";
        $query .= "                                          AND OUTG.REQUEST_NO  = SCHD.REQUEST_NO ";
        $query .= " WHERE ";
        $query .= "         SCHD.SCHOOLCD     = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHD.SCHOOL_KIND  = '".$model->schoolKind."'";
        $query .= "     AND SCHD.YEAR         = '".CTRL_YEAR."' ";
        $query .= "     AND SCHD.SCHREGNO     = '{$schregno}' ";
        $query .= "     AND SCHD.LINE_NO      = 1 ";
        $query .= "     AND SCHD.OUTGO_L_CD   = '99' ";
        $query .= "     AND SCHD.OUTGO_M_CD   = '99' ";
        $query .= "     AND SCHD.OUTGO_S_CD   = '999' ";
        $query .= "     AND OUTG.INCOME_L_CD  = '{$incomeLcd}' ";
        $query .= "     AND OUTG.INCOME_M_CD  = '{$incomeMcd}' ";
        $query .= "     AND OUTG.COLLECT_L_CD = '97' ";

        return $query;
    }

    //返金伝票作成
    function getUpdateHenkin($db, $model, $incomeLcd, $incomeMcd, $setMoney, $schregNo) {
        $getMaxRequestNo = $db->getOne(knjp913Query::getMaxRequestNo($model, $schoolKind));
        //対象年度の伝票番号がない場合
        if (!$getMaxRequestNo) {
            $setRequestNo = CTRL_YEAR.'B00001';
        } else {
            $setNo = sprintf("%05d", intval(substr($getMaxRequestNo, 5, 5)) + 1);
            $setRequestNo = CTRL_YEAR.'B'.$setNo;
        }

        //LEVY_REQUEST_OUTGO_DAT
        $data = array();
        $data["SCHOOLCD"][TEXT]         = (sprintf("%012d", SCHOOLCD));
        $data["SCHOOL_KIND"][TEXT]      = $model->schoolKind;
        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["OUTGO_L_CD"][TEXT]       = '99';
        $data["OUTGO_M_CD"][TEXT]       = '99';
        $data["REQUEST_NO"][TEXT]       = $setRequestNo;
        $data["REQUEST_DATE"][DATE]     = str_replace('/', '-', CTRL_DATE);
        $data["REQUEST_STAFF"][TEXT]    = STAFFCD;
        $data["INCOME_L_CD"][TEXT]      = $incomeLcd;
        $data["INCOME_M_CD"][TEXT]      = $incomeMcd;
        $data["PAY_DIV"][TEXT]          = '1'; // 固定
        $data["REQUEST_GK"][NUMBER]     = $setMoney;
        $data["OUTGO_APPROVAL"][TEXT]   = '1';
        $data["HENKIN_FLG"][TEXT]       = '1';
        $data["COLLECT_L_CD"][TEXT]     = '97';

        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $query = Query::insertSQL($data, "LEVY_REQUEST_OUTGO_DAT");
        $db->query($query);

        //LEVY_REQUEST_OUTGO_MEISAI_DAT
        $data = array();
        $data["SCHOOLCD"][TEXT]                 = (sprintf("%012d", SCHOOLCD));
        $data["SCHOOL_KIND"][TEXT]              = $model->schoolKind;
        $data["YEAR"][TEXT]                     = CTRL_YEAR;
        $data["REQUEST_NO"][TEXT]               = $setRequestNo;
        $data["LINE_NO"][NUMBER]                = 1;
        $data["OUTGO_L_CD"][TEXT]               = '99';
        $data["OUTGO_M_CD"][TEXT]               = '99';
        $data["OUTGO_S_CD"][TEXT]               = '999';
        $data["COMMODITY_PRICE"][NUMBER]        = $setMoney;
        $data["COMMODITY_CNT"][NUMBER]          = 1;
        $data["TOTAL_PRICE_ZEINUKI"][NUMBER]    = $setMoney;
        $data["TOTAL_TAX"][NUMBER]              = 0;
        $data["TOTAL_PRICE"][NUMBER]            = $setMoney;
        $data["SCH_PRICE"][NUMBER]              = $setMoney;
        $data["SCH_CNT"][NUMBER]                = 1;
        $data["HASUU"][NUMBER]                  = 0;
        $data["WARIHURI_DIV"][TEXT]             = '1';

        $data["REGISTERCD"][TEXT]               = STAFFCD;
        $data["UPDATED"][FUNC]                  = "SYSDATE()";

        $query = Query::insertSQL($data, "LEVY_REQUEST_OUTGO_MEISAI_DAT");
        $db->query($query);

        //LEVY_REQUEST_OUTGO_SCHREG_DAT
        $data = array();
        $data["SCHOOLCD"][TEXT]                 = (sprintf("%012d", SCHOOLCD));
        $data["SCHOOL_KIND"][TEXT]              = $model->schoolKind;
        $data["YEAR"][TEXT]                     = CTRL_YEAR;
        $data["REQUEST_NO"][TEXT]               = $setRequestNo;
        $data["SCHREGNO"][TEXT]                 = $schregNo;
        $data["LINE_NO"][NUMBER]                = 1;
        $data["OUTGO_L_CD"][TEXT]               = '99';
        $data["OUTGO_M_CD"][TEXT]               = '99';
        $data["OUTGO_S_CD"][TEXT]               = '999';
        $data["OUTGO_DATE"][DATE]               = str_replace('/', '-', CTRL_DATE);
        $data["OUTGO_MONEY"][NUMBER]            = $setMoney;

        $data["REGISTERCD"][TEXT]               = STAFFCD;
        $data["UPDATED"][FUNC]                  = "SYSDATE()";

        $query = Query::insertSQL($data, "LEVY_REQUEST_OUTGO_SCHREG_DAT");
        $db->query($query);

        return;
    }

    //max伝票番号を取得
    function getMaxRequestNo($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     MAX(REQUEST_NO) AS NO ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND = '".$model->schoolKind."'";
        $query .= "     AND YEAR        = '".CTRL_YEAR."' ";

        return $query;
    }
}
?>
