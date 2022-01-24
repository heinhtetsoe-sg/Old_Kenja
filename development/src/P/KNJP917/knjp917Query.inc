<?php

require_once('for_php7.php');

class knjp917Query extends Query {
    /************/
    /* 検索画面 */
    /************/
    //左リスト
    function GetStudents($model) {

        $where = knjp917Query::getStudentSearch($model);
        $and = $where == "" ? "" : "AND ";

        $query  = " WITH SCH_R AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         REGD.YEAR, ";
        $query .= "         REGD.SCHREGNO, ";
        $query .= "         REGD.GRADE, ";
        $query .= "         REGD.HR_CLASS, ";
        $query .= "         REGD.ATTENDNO, ";
        $query .= "         HDAT.HR_NAME ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT REGD ";
        $query .= "         LEFT JOIN SCHREG_REGD_HDAT HDAT ";
        $query .= "              ON REGD.YEAR     = HDAT.YEAR ";
        $query .= "             AND REGD.SEMESTER = HDAT.SEMESTER ";
        $query .= "             AND REGD.GRADE    = HDAT.GRADE ";
        $query .= "             AND REGD.HR_CLASS = HDAT.HR_CLASS ";
        $query .= "     WHERE ";
        $query .= "         REGD.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query .= "     AND ('".STAFFCD."' IN (HDAT.TR_CD1, HDAT.TR_CD2, HDAT.TR_CD3, HDAT.SUBTR_CD1, HDAT.SUBTR_CD2, HDAT.SUBTR_CD3) ";
            $query .= "          OR (REGD.GRADE IN (SELECT ";
            $query .= "                                 FIELD2 ";
            $query .= "                             FROM ";
            $query .= "                                 STAFF_DETAIL_MST ST ";
            $query .= "                             WHERE ";
            $query .= "                                 REGD.YEAR   = ST.YEAR ";
            $query .= "                             AND ST.STAFFCD  = '".STAFFCD."' ";
            $query .= "                             AND ST.STAFF_SEQ IN ('005', '006', '007') ";
            $query .= "                             AND ST.FIELD1   = '0200')) ";
            $query .= "         ) ";
        }
        $query .= " ) ";
        //メイン
        $query .= " SELECT DISTINCT ";
        $query .= "   SCH_R.GRADE || SCH_R.HR_CLASS || VALUE(SCH_R.ATTENDNO,'') AS HR_CLASS_ATTEND_ORDER, ";
        $query .= "   SCH_R.GRADE, ";
        $query .= "   SCH_R.HR_CLASS, ";
        $query .= "   SCH_R.ATTENDNO, ";
        $query .= "   GDAT.SCHOOL_KIND, ";
        $query .= "   MAIN.SCHREGNO, ";
        $query .= "   MAIN.NAME, ";
        $query .= "   MAIN.SEX, ";
        $query .= "   VALUE(SCH_R.HR_NAME,'') || ' - ' || VALUE(SCH_R.ATTENDNO,'') AS HR_ATTEND ";
        $query .= " FROM ";
        $query .= "   SCHREG_BASE_MST MAIN ";
        $query .= "   LEFT JOIN SCH_R ON MAIN.SCHREGNO = SCH_R.SCHREGNO ";
        $query .= "   LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR  = SCH_R.YEAR ";
        $query .= "                                  AND GDAT.GRADE = SCH_R.GRADE ";
        //対象者は給付金テーブルに存在する人のみ（P906で登録）
        $query .= "  INNER JOIN LEVY_REQUEST_BENEFIT_SCHREG_DAT BENE ON BENE.SCHOOLCD    = '{$model->schoolCd}' ";
        $query .= "                                                 AND BENE.SCHOOL_KIND = GDAT.SCHOOL_KIND ";
        $query .= "                                                 AND BENE.YEAR        = GDAT.YEAR ";
        $query .= "                                                 AND BENE.SCHREGNO    = MAIN.SCHREGNO ";
        if ($where != "") {
            $query .= " WHERE ".$where;
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                if ($where == "") $query .= " WHERE ";
                $query .= "  {$and} GDAT.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            if ($where == "") $query .= " WHERE ";
            $query .= "  {$and} GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        $query .= " ORDER BY ";
        if ($model->search["HR_CLASS_HYOUJI_FLG"] == "1") {
            $query .= "   HR_CLASS_ATTEND_ORDER ";
        } else {
            $query .= "   MAIN.SCHREGNO ";
        }

        return $query;
   }

    //検索条件（WHERE）
    function getStudentSearch($model) {
        $and = "";
        $where = "";

        //入学年度
        if ($model->search["ENT_YEAR"]) {
            $where .= "   ".$and."FISCALYEAR(ENT_DATE) = '".$model->search["ENT_YEAR"]."' ";
            $and = "AND ";
        }
        //卒業予定年度
        if ($model->search["GRD_YEAR"]) {
             $where .= "   ".$and."FISCALYEAR(GRD_DATE) = '".$model->search["GRD_YEAR"]."' ";
             $and = "AND ";
        }
        //学年
        if ($model->search["GRADE"]) {
            $where .= "   ".$and."SCH_R.GRADE = '".$model->search["GRADE"]."' ";
            $and = "AND ";
        }
        //年組
        if ($model->search["HR_CLASS"]) {
            $where .= "   ".$and."SCH_R.GRADE || SCH_R.HR_CLASS = '".$model->search["HR_CLASS"]."' ";
            $and = "AND ";
        }
        //学籍番号
        if ($model->search["SCHREGNO"]) {
            $where .= "   ".$and."MAIN.SCHREGNO = '".$model->search["SCHREGNO"]."' ";
            $and = "AND ";
        }
        //氏名
        if ($model->search["NAME"]) {
            $where .= "   ".$and."MAIN.NAME LIKE '%" .str_replace("'","''",$model->search["NAME"]). "%' ";
            $and = "AND ";
        }
        //氏名かな
        if ($model->search["NAME_KANA"]) {
            $where .= "   ".$and."MAIN.NAME_KANA LIKE '%" .str_replace("'","''",$model->search["NAME_KANA"]). "%' ";
            $and = "AND ";
        }

        return $where;
    }

    //入学年度
    function getEntYear() {
        $query  = " SELECT ";
        $query .= "     YEAR AS LABEL, ";
        $query .= "     YEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " GROUP BY ";
        $query .= "     YEAR ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //卒業予定年度
    function getGrdYear() {
        $query  = " WITH MIRAI_3YEAR (LABEL, VALUE) AS ( ";
        $query .= "     VALUES ";
        $query .= "         ('".(CTRL_YEAR + 1)."', '".(CTRL_YEAR + 1)."'), ";
        $query .= "         ('".(CTRL_YEAR + 2)."', '".(CTRL_YEAR + 2)."'), ";
        $query .= "         ('".(CTRL_YEAR + 3)."', '".(CTRL_YEAR + 3)."') ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     YEAR AS LABEL, ";
        $query .= "     YEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     LABEL, ";
        $query .= "     VALUE ";
        $query .= " FROM ";
        $query .= "     MIRAI_3YEAR ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //学年
    function getGrade($model) {
        $query  = " SELECT ";
        $query .= "     GRADE_NAME1 AS LABEL, ";
        $query .= "     GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "  AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "  AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        //給付対象で絞る
        $query .= "    AND GRADE in (SELECT DISTINCT ";
        $query .= "                      REGD.GRADE ";
        $query .= "                  FROM ";
        $query .= "                      LEVY_REQUEST_BENEFIT_SCHREG_DAT BENE ";
        $query .= "                      LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = BENE.SCHREGNO ";
        $query .= "                                                    AND REGD.YEAR     = '".CTRL_YEAR."' ";
        $query .= "                                                    AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "                  ) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組
    function getHrClass($model) {
        $query  = " SELECT ";
        $query .= "     HDAT.GRADE || HDAT.HR_CLASS AS VALUE, ";
        $query .= "     HDAT.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT HDAT ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR  = HDAT.YEAR ";
        $query .= "                                     AND GDAT.GRADE = HDAT.GRADE ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "       AND GDAT.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "           AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "         HDAT.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND HDAT.SEMESTER = '".CTRL_SEMESTER."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query .= " AND (   '".STAFFCD."' IN (HDAT.TR_CD1, HDAT.TR_CD2, HDAT.TR_CD3, HDAT.SUBTR_CD1, HDAT.SUBTR_CD2, HDAT.SUBTR_CD3) ";
            $query .= "      OR (HDAT.GRADE IN (SELECT ";
            $query .= "                             FIELD2 ";
            $query .= "                         FROM ";
            $query .= "                             STAFF_DETAIL_MST ST ";
            $query .= "                         WHERE ";
            $query .= "                                 HDAT.YEAR   = ST.YEAR ";
            $query .= "                             AND ST.STAFFCD  = '".STAFFCD."' ";
            $query .= "                             AND ST.STAFF_SEQ IN ('005', '006', '007') ";
            $query .= "                             AND ST.FIELD1   = '0200')) ";
            $query .= "     ) ";
        }
        //給付対象で絞る
        $query .= "    AND HDAT.GRADE || HDAT.HR_CLASS in (SELECT DISTINCT ";
        $query .= "                                            REGD.GRADE || REGD.HR_CLASS ";
        $query .= "                                        FROM ";
        $query .= "                                            LEVY_REQUEST_BENEFIT_SCHREG_DAT BENE ";
        $query .= "                                            LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = BENE.SCHREGNO ";
        $query .= "                                                                          AND REGD.YEAR     = '".CTRL_YEAR."' ";
        $query .= "                                                                          AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "                                        ) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    /**********/
    /* 右画面 */
    /**********/
    //生徒名
    function getStudentName($schregno) {
        $query  = " SELECT ";
        $query .= "     NAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$schregno}' ";

        return $query;
    }

    //返金（給付）伝票確認
    function getHenkinApproval($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_SCHREG_DAT OSCH ";
        $query .= "     LEFT JOIN LEVY_REQUEST_OUTGO_DAT OUTG ON OUTG.SCHOOLCD    = OSCH.SCHOOLCD ";
        $query .= "                                          AND OUTG.SCHOOL_KIND = OSCH.SCHOOL_KIND ";
        $query .= "                                          AND OUTG.YEAR        = OSCH.YEAR ";
        $query .= "                                          AND OUTG.OUTGO_L_CD  = OSCH.OUTGO_L_CD ";
        $query .= "                                          AND OUTG.OUTGO_M_CD  = OSCH.OUTGO_M_CD ";
        $query .= "                                          AND OUTG.REQUEST_NO  = OSCH.REQUEST_NO ";
        $query .= " WHERE ";
        $query .= "         OSCH.SCHOOLCD       = '{$model->schoolCd}' ";
        $query .= "     AND OSCH.SCHOOL_KIND    = '{$model->schoolKind}' ";
        $query .= "     AND OSCH.YEAR           = '{$model->year}' ";
        $query .= "     AND OSCH.SCHREGNO       = '{$model->schregno}' ";
        $query .= "     AND OUTG.INCOME_L_CD    = '98' ";//給付伝票
        $query .= "     AND VALUE(OUTG.OUTGO_APPROVAL, '0')  = '1' ";
        $query .= "     AND VALUE(OUTG.OUTGO_CANCEL, '0')    = '0' ";
        $query .= "     AND VALUE(OUTG.HENKIN_APPROVAL, '0') = '1' ";//CSV取込完了済み

        return $query;
    }

    //給付上限取得
    function getMaxBenefitMoney($model) {
        $query  = " SELECT ";
        $query .= "     BENEFIT_MONEY ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_BENEFIT_SCHREG_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '{$model->schoolCd}' ";
        $query .= "     AND SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= "     AND YEAR        = '{$model->year}' ";
        $query .= "     AND SCHREGNO    = '{$model->schregno}' ";

        return $query;
    }

    //名称マスタ
    function getNameMst($namecd1) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($namecd1 == 'P004') {
            $query .= "     AND NAMECD2 <> '1' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //生徒情報取得
    function getStudentInfoData($model, $lmCd="", $flg="") {
        $query  = " WITH GASSAN_T AS ( ";
        $query .= "     SELECT ";
        $query .= "         OUTS.SCHOOLCD, ";
        $query .= "         OUTS.SCHOOL_KIND, ";
        $query .= "         OUTS.YEAR, ";
        $query .= "         OUTS.OUTGO_L_CD, ";
        $query .= "         OUTS.OUTGO_M_CD, ";
        $query .= "         OUTS.OUTGO_S_CD, ";
        $query .= "         max(OUTG.OUTGO_DATE) as OUTGO_DATE, ";
        $query .= "         sum(OUTS.OUTGO_MONEY) as OUTGO_MONEY ";
        $query .= "     FROM ";
        $query .= "         LEVY_REQUEST_OUTGO_SCHREG_DAT OUTS ";
        $query .= "         INNER JOIN LEVY_REQUEST_BENEFIT_SCHREG_DAT BENE ON BENE.SCHOOLCD    = OUTS.SCHOOLCD ";
        $query .= "                                                        AND BENE.SCHOOL_KIND = OUTS.SCHOOL_KIND ";
        $query .= "                                                        AND BENE.YEAR        = OUTS.YEAR ";
        $query .= "                                                        AND BENE.SCHREGNO    = OUTS.SCHREGNO ";
        $query .= "         LEFT JOIN LEVY_REQUEST_OUTGO_DAT OUTG ON OUTG.SCHOOLCD    = OUTS.SCHOOLCD ";
        $query .= "                                              AND OUTG.SCHOOL_KIND = OUTS.SCHOOL_KIND ";
        $query .= "                                              AND OUTG.YEAR        = OUTS.YEAR ";
        $query .= "                                              AND OUTG.OUTGO_L_CD  = OUTS.OUTGO_L_CD ";
        $query .= "                                              AND OUTG.OUTGO_M_CD  = OUTS.OUTGO_M_CD ";
        $query .= "                                              AND OUTG.REQUEST_NO  = OUTS.REQUEST_NO ";
        $query .= "         LEFT JOIN LEVY_S_MST SMST ON SMST.SCHOOLCD    = OUTS.SCHOOLCD ";
        $query .= "                                  AND SMST.SCHOOL_KIND = OUTS.SCHOOL_KIND ";
        $query .= "                                  AND SMST.YEAR        = OUTS.YEAR ";
        $query .= "                                  AND SMST.LEVY_L_CD   = OUTS.OUTGO_L_CD ";
        $query .= "                                  AND SMST.LEVY_M_CD   = OUTS.OUTGO_M_CD ";
        $query .= "                                  AND SMST.LEVY_S_CD   = OUTS.OUTGO_S_CD ";
        $query .= "     WHERE ";
        $query .= "             OUTS.SCHOOLCD    = '{$model->schoolCd}' ";
        $query .= "         AND OUTS.SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= "         AND OUTS.YEAR        = '{$model->year}' ";
        $query .= "         AND OUTS.SCHREGNO    = '{$model->schregno}' ";
        $query .= "         AND OUTG.OUTGO_L_CD  <> '99' ";
        $query .= "         AND OUTG.INCOME_L_CD <> '98' ";
        $query .= "         AND OUTG.INCOME_M_CD <> '98' ";
        $query .= "         AND VALUE(OUTG.OUTGO_APPROVAL, '0')  = '1' ";
        $query .= "         AND VALUE(OUTG.OUTGO_CANCEL, '0')    = '0' ";
        $query .= "         AND VALUE(SMST.BENEFIT, '0')         = '1' ";
        $query .= "     GROUP BY ";
        $query .= "         OUTS.SCHOOLCD, ";
        $query .= "         OUTS.SCHOOL_KIND, ";
        $query .= "         OUTS.YEAR, ";
        $query .= "         OUTS.OUTGO_L_CD, ";
        $query .= "         OUTS.OUTGO_M_CD, ";
        $query .= "         OUTS.OUTGO_S_CD ";
        $query .= " ), KYUFU_T AS ( ";
        $query .= "     SELECT ";
        $query .= "         OUTS.OUTGO_L_CD, ";
        $query .= "         OUTS.OUTGO_M_CD, ";
        $query .= "         OUTS.OUTGO_S_CD, ";
        $query .= "         sum(OUTS.OUTGO_MONEY) as KYUFU_MONEY ";
        $query .= "     FROM ";
        $query .= "         LEVY_REQUEST_OUTGO_SCHREG_DAT OUTS ";
        $query .= "         INNER JOIN LEVY_REQUEST_BENEFIT_SCHREG_DAT BENE ON BENE.SCHOOLCD    = OUTS.SCHOOLCD ";
        $query .= "                                                        AND BENE.SCHOOL_KIND = OUTS.SCHOOL_KIND ";
        $query .= "                                                        AND BENE.YEAR        = OUTS.YEAR ";
        $query .= "                                                        AND BENE.SCHREGNO    = OUTS.SCHREGNO ";
        $query .= "         LEFT JOIN LEVY_REQUEST_OUTGO_DAT OUTG ON OUTG.SCHOOLCD    = OUTS.SCHOOLCD ";
        $query .= "                                              AND OUTG.SCHOOL_KIND = OUTS.SCHOOL_KIND ";
        $query .= "                                              AND OUTG.YEAR        = OUTS.YEAR ";
        $query .= "                                              AND OUTG.OUTGO_L_CD  = OUTS.OUTGO_L_CD ";
        $query .= "                                              AND OUTG.OUTGO_M_CD  = OUTS.OUTGO_M_CD ";
        $query .= "                                              AND OUTG.REQUEST_NO  = OUTS.REQUEST_NO ";
        $query .= "         LEFT JOIN LEVY_S_MST SMST ON SMST.SCHOOLCD    = OUTS.SCHOOLCD ";
        $query .= "                                  AND SMST.SCHOOL_KIND = OUTS.SCHOOL_KIND ";
        $query .= "                                  AND SMST.YEAR        = OUTS.YEAR ";
        $query .= "                                  AND SMST.LEVY_L_CD   = OUTS.OUTGO_L_CD ";
        $query .= "                                  AND SMST.LEVY_M_CD   = OUTS.OUTGO_M_CD ";
        $query .= "                                  AND SMST.LEVY_S_CD   = OUTS.OUTGO_S_CD ";
        $query .= "     WHERE ";
        $query .= "             OUTS.SCHOOLCD    = '{$model->schoolCd}' ";
        $query .= "         AND OUTS.SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= "         AND OUTS.YEAR        = '{$model->year}' ";
        $query .= "         AND OUTS.SCHREGNO    = '{$model->schregno}' ";
        $query .= "         AND OUTG.INCOME_L_CD = '98' ";
        $query .= "         AND OUTG.INCOME_M_CD = '98' ";
        $query .= "         AND VALUE(OUTG.OUTGO_APPROVAL, '0')  = '1' ";
        $query .= "         AND VALUE(OUTG.OUTGO_CANCEL, '0')    = '0' ";
        $query .= "         AND VALUE(SMST.BENEFIT, '0')         = '1' ";
        $query .= "     GROUP BY ";
        $query .= "         OUTS.SCHOOLCD, ";
        $query .= "         OUTS.SCHOOL_KIND, ";
        $query .= "         OUTS.YEAR, ";
        $query .= "         OUTS.OUTGO_L_CD, ";
        $query .= "         OUTS.OUTGO_M_CD, ";
        $query .= "         OUTS.OUTGO_S_CD ";
        $query .= " ) ";
        //メイン
        $query .= " SELECT ";
        if ($lmCd != "") {
            if ($flg == "OUT_GO") {
                $query .= "     sum( ";
                $query .= "         case ";
                $query .= "             when SMST.MAX_BENEFIT < MAIN.OUTGO_MONEY then SMST.MAX_BENEFIT ";
                $query .= "             else MAIN.OUTGO_MONEY ";
                $query .= "         end ";
                $query .= "         ) as OUTGO_TOTAL ";
            } else {
                $query .= "     sum(KYUF.KYUFU_MONEY) ";
            }
        } else {
            $query .= "     MAIN.OUTGO_DATE, ";
            $query .= "     MAIN.OUTGO_L_CD, ";
            $query .= "     MAIN.OUTGO_M_CD, ";
            $query .= "     MAIN.OUTGO_S_CD, ";
            $query .= "     LMST.LEVY_L_NAME, ";
            $query .= "     MMST.LEVY_M_NAME, ";
            $query .= "     SMST.LEVY_S_NAME, ";
            $query .= "     SMST.MAX_BENEFIT, ";
            $query .= "     case ";
            $query .= "         when SMST.MAX_BENEFIT < MAIN.OUTGO_MONEY then SMST.MAX_BENEFIT ";
            $query .= "         else MAIN.OUTGO_MONEY ";
            $query .= "     end as OUTGO_MONEY, ";
            $query .= "     KYUF.KYUFU_MONEY ";
        }
        $query .= " FROM ";
        $query .= "     GASSAN_T MAIN ";
        $query .= "     LEFT JOIN KYUFU_T KYUF ON KYUF.OUTGO_L_CD = MAIN.OUTGO_L_CD ";
        $query .= "                           AND KYUF.OUTGO_M_CD = MAIN.OUTGO_M_CD ";
        $query .= "                           AND KYUF.OUTGO_S_CD = MAIN.OUTGO_S_CD ";
        $query .= "     LEFT JOIN LEVY_L_MST LMST ON LMST.SCHOOLCD    = MAIN.SCHOOLCD ";
        $query .= "                              AND LMST.SCHOOL_KIND = MAIN.SCHOOL_KIND ";
        $query .= "                              AND LMST.YEAR        = MAIN.YEAR ";
        $query .= "                              AND LMST.LEVY_L_CD   = MAIN.OUTGO_L_CD ";
        $query .= "     LEFT JOIN LEVY_M_MST MMST ON MMST.SCHOOLCD    = MAIN.SCHOOLCD ";
        $query .= "                              AND MMST.SCHOOL_KIND = MAIN.SCHOOL_KIND ";
        $query .= "                              AND MMST.YEAR        = MAIN.YEAR ";
        $query .= "                              AND MMST.LEVY_L_CD   = MAIN.OUTGO_L_CD ";
        $query .= "                              AND MMST.LEVY_M_CD   = MAIN.OUTGO_M_CD ";
        $query .= "     LEFT JOIN LEVY_S_MST SMST ON SMST.SCHOOLCD    = MAIN.SCHOOLCD ";
        $query .= "                              AND SMST.SCHOOL_KIND = MAIN.SCHOOL_KIND ";
        $query .= "                              AND SMST.YEAR        = MAIN.YEAR ";
        $query .= "                              AND SMST.LEVY_L_CD   = MAIN.OUTGO_L_CD ";
        $query .= "                              AND SMST.LEVY_M_CD   = MAIN.OUTGO_M_CD ";
        $query .= "                              AND SMST.LEVY_S_CD   = MAIN.OUTGO_S_CD ";
        if ($lmCd != "") {
            $query .= " WHERE ";
            $query .= "     MAIN.OUTGO_L_CD || MAIN.OUTGO_M_CD = '{$lmCd}' ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     MAIN.OUTGO_L_CD, ";
            $query .= "     MAIN.OUTGO_M_CD, ";
            $query .= "     MAIN.OUTGO_DATE, ";
            $query .= "     MAIN.OUTGO_S_CD ";
        }

        return $query;
    }

    /************/
    /* 更新処理 */
    /************/
    //max伝票番号を取得
    function getMaxRequestNo($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     MAX(REQUEST_NO) AS NO ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '{$model->schoolCd}' ";
        $query .= "     AND SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= "     AND YEAR        = '{$model->year}' ";

        return $query;
    }

    //UPDATE
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //入金作成
        foreach ($model->updLMScd as $lmsCd => $outGoMoney) {
            $kyufuMoney = $model->field["KYUFU:".$lmsCd];

            //チェックあるものを更新
            if ($model->field["CHECK_BOX:".$lmsCd] == "1") {
                list($lCd, $mCd, $sCd) = explode("-", $lmsCd);

                //伝票番号セット
                $getMaxRequestNo = $db->getOne(knjp917Query::getMaxRequestNo($model));
                $setNo = sprintf("%05d", intval(substr($getMaxRequestNo, 5, 5)) + 1);
                $setRequestNo = $model->year.'B'.$setNo;

                if ($kyufuMoney != "") {

                    //LEVY_REQUEST_OUTGO_SCHREG_DAT
                    $data = array();
                    $data["SCHOOLCD"][TEXT]                 = $model->schoolCd;
                    $data["SCHOOL_KIND"][TEXT]              = $model->schoolKind;
                    $data["YEAR"][TEXT]                     = $model->year;
                    $data["REQUEST_NO"][TEXT]               = $setRequestNo;
                    $data["SCHREGNO"][TEXT]                 = $model->schregno;
                    $data["LINE_NO"][NUMBER]                = 98;
                    $data["OUTGO_L_CD"][TEXT]               = $lCd;
                    $data["OUTGO_M_CD"][TEXT]               = $mCd;
                    $data["OUTGO_S_CD"][TEXT]               = $sCd;
                    $data["OUTGO_DATE"][DATE]               = str_replace('/', '-', $model->field["OUTGO_DATE"]);
                    $data["OUTGO_MONEY"][NUMBER]            = $kyufuMoney;

                    $data["REGISTERCD"][TEXT]               = STAFFCD;
                    $data["UPDATED"][FUNC]                  = "SYSDATE()";

                    $query = Query::insertSQL($data, "LEVY_REQUEST_OUTGO_SCHREG_DAT");
                    $db->query($query);

                    //LEVY_REQUEST_OUTGO_MEISAI_DAT
                    $data = array();
                    $data["SCHOOLCD"][TEXT]                 = $model->schoolCd;
                    $data["SCHOOL_KIND"][TEXT]              = $model->schoolKind;
                    $data["YEAR"][TEXT]                     = $model->year;
                    $data["REQUEST_NO"][TEXT]               = $setRequestNo;
                    $data["LINE_NO"][NUMBER]                = 98;
                    $data["OUTGO_L_CD"][TEXT]               = $lCd;
                    $data["OUTGO_M_CD"][TEXT]               = $mCd;
                    $data["OUTGO_S_CD"][TEXT]               = $sCd;
                    $data["COMMODITY_PRICE"][NUMBER]        = $kyufuMoney;
                    $data["COMMODITY_CNT"][NUMBER]          = 1;
                    $data["TOTAL_PRICE_ZEINUKI"][NUMBER]    = $kyufuMoney;
                    $data["TOTAL_TAX"][NUMBER]              = 0;
                    $data["TOTAL_PRICE"][NUMBER]            = $kyufuMoney;
                    $data["SCH_PRICE"][NUMBER]              = $kyufuMoney;
                    $data["SCH_CNT"][NUMBER]                = 1;
                    $data["HASUU"][NUMBER]                  = 0;
                    $data["WARIHURI_DIV"][TEXT]             = '1';

                    $data["REGISTERCD"][TEXT]               = STAFFCD;
                    $data["UPDATED"][FUNC]                  = "SYSDATE()";

                    $query = Query::insertSQL($data, "LEVY_REQUEST_OUTGO_MEISAI_DAT");
                    $db->query($query);

                    //LEVY_REQUEST_OUTGO_DAT
                    $data = array();
                    $data["SCHOOLCD"][TEXT]         = $model->schoolCd;
                    $data["SCHOOL_KIND"][TEXT]      = $model->schoolKind;
                    $data["YEAR"][TEXT]             = $model->year;
                    $data["OUTGO_L_CD"][TEXT]       = $lCd;
                    $data["OUTGO_M_CD"][TEXT]       = $mCd;
                    $data["REQUEST_NO"][TEXT]       = $setRequestNo;
                    $data["REQUEST_DATE"][DATE]     = str_replace('/', '-', $model->field["OUTGO_DATE"]);
                    $data["REQUEST_STAFF"][TEXT]    = STAFFCD;
                    $data["INCOME_L_CD"][TEXT]      = '98';
                    $data["INCOME_M_CD"][TEXT]      = '98';
                    $data["PAY_DIV"][TEXT]          = '1'; // 固定
                    $data["REQUEST_GK"][NUMBER]     = $kyufuMoney;
                    $data["OUTGO_APPROVAL"][TEXT]   = '1';
                    $data["HENKIN_FLG"][TEXT]       = '1';

                    $data["REGISTERCD"][TEXT]       = STAFFCD;
                    $data["UPDATED"][FUNC]          = "SYSDATE()";

                    $query = Query::insertSQL($data, "LEVY_REQUEST_OUTGO_DAT");
                    $db->query($query);
                }
            }
        }

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }

    //DELETE
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query = knjp917Query::getDelRequestNo($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            //LEVY_REQUEST_OUTGO_SCHREG_DAT
            $query  = " DELETE FROM LEVY_REQUEST_OUTGO_SCHREG_DAT ";
            $query .= " WHERE ";
            $query .= "     SCHOOLCD    = '{$model->schoolCd}' ";
            $query .= " AND SCHOOL_KIND = '{$model->schoolKind}' ";
            $query .= " AND YEAR        = '{$model->year}' ";
            $query .= " AND REQUEST_NO  = '{$row["REQUEST_NO"]}' ";
            $db->query($query);

            //LEVY_REQUEST_OUTGO_MEISAI_DAT
            $query  = " DELETE FROM LEVY_REQUEST_OUTGO_MEISAI_DAT ";
            $query .= " WHERE ";
            $query .= "     SCHOOLCD    = '{$model->schoolCd}' ";
            $query .= " AND SCHOOL_KIND = '{$model->schoolKind}' ";
            $query .= " AND YEAR        = '{$model->year}' ";
            $query .= " AND REQUEST_NO  = '{$row["REQUEST_NO"]}' ";
            $db->query($query);

            //LEVY_REQUEST_OUTGO_DAT
            $query  = " DELETE FROM LEVY_REQUEST_OUTGO_DAT ";
            $query .= " WHERE ";
            $query .= "     SCHOOLCD    = '{$model->schoolCd}' ";
            $query .= " AND SCHOOL_KIND = '{$model->schoolKind}' ";
            $query .= " AND YEAR        = '{$model->year}' ";
            $query .= " AND REQUEST_NO  = '{$row["REQUEST_NO"]}' ";
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);

        return;
    }

    //削除する伝票番号取得
    function getDelRequestNo($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     O_SCH.REQUEST_NO ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_SCHREG_DAT O_SCH ";
        $query .= "     LEFT JOIN LEVY_REQUEST_OUTGO_DAT OUTGO ON OUTGO.SCHOOLCD    = O_SCH.SCHOOLCD ";
        $query .= "                                           AND OUTGO.SCHOOL_KIND = O_SCH.SCHOOL_KIND ";
        $query .= "                                           AND OUTGO.YEAR        = O_SCH.YEAR ";
        $query .= "                                           AND OUTGO.OUTGO_L_CD  = O_SCH.OUTGO_L_CD ";
        $query .= "                                           AND OUTGO.OUTGO_M_CD  = O_SCH.OUTGO_M_CD ";
        $query .= "                                           AND OUTGO.REQUEST_NO  = O_SCH.REQUEST_NO ";
        $query .= " WHERE ";
        $query .= "         O_SCH.SCHOOLCD    = '{$model->schoolCd}' ";
        $query .= "     AND O_SCH.SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= "     AND O_SCH.YEAR        = '{$model->year}' ";
        $query .= "     AND O_SCH.SCHREGNO    = '{$model->schregno}' ";
        $query .= "     AND OUTGO.INCOME_L_CD = '98' ";
        $query .= "     AND OUTGO.INCOME_M_CD = '98' ";

        return $query;
    }
}
?>
