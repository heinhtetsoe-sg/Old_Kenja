<?php

require_once('for_php7.php');
require_once("PrimarySchoolProcess.php");

class knjc010aQuery extends Query
{
    public function getHrRemark($model)
    {
        $month = substr($model->param["syoribi"], 5, 2);
        $query  = " SELECT ";
        $query .= "     REMARK1 ";
        $query .= " FROM ";
        $query .= "     ATTEND_SEMES_REMARK_HR_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' ";
        $query .= "     AND MONTH = '{$month}' ";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND GRADE = '{$model->param["grade"]}' ";
        $query .= "     AND HR_CLASS = '{$model->param["hrclass"]}' ";

        return $query;
    }

    /* 校時取得 */
    public function getTitlePeriod()
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1, ";
        $query .= "     NAMESPARE1, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'B001' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";
        return $query;
    }

    /* 連続校時取得用マスタ */
    public function getRenzokuPeriodMst($periodCd, $div)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1, ";
        $query .= "     NAMESPARE1, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'B001' ";
        if ($div == "LOW") {
            $query .= "     AND NAMECD2 < '{$periodCd}' ";
        } else {
            $query .= "     AND NAMECD2 > '{$periodCd}' ";
        }
        $query .= " ORDER BY ";
        if ($div == "LOW") {
            $query .= "     NAMECD2 DESC ";
        } else {
            $query .= "     NAMECD2 ";
        }
        return $query;
    }

    /* 連続校時取得 */
    public function getRenzokuPeriod($model, $schregNo, $chairCd, $periodCd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     CHAIR_STF_DAT T3, ";
        $query .= "     CHAIR_STD_DAT T1, ";
        $query .= "     SCH_CHR_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T3.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T3.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND T3.STAFFCD = '".STAFFCD."' ";
        $query .= "     AND T1.CHAIRCD = T3.CHAIRCD ";
        $query .= "     AND T1.YEAR = T3.YEAR ";
        $query .= "     AND T1.SEMESTER = T3.SEMESTER ";
        $query .= "     AND T1.CHAIRCD = T3.CHAIRCD ";
        $query .= "     AND T1.SCHREGNO = '{$schregNo}' ";
        $query .= "     AND '".str_replace("/", "-", $model->param["syoribi"])."' BETWEEN T1.APPDATE AND T1.APPENDDATE ";
        $query .= "     AND T2.EXECUTEDATE = '".str_replace("/", "-", $model->param["syoribi"])."' ";
        $query .= "     AND T2.PERIODCD = '{$periodCd}' ";
        $query .= "     AND T2.CHAIRCD = T3.CHAIRCD ";
        $query .= "     AND T2.CHAIRCD = '{$chairCd}' ";

        return $query;
    }

    //名称マスタ
    public function getZ010()
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }


    //出欠コードテーブル
    public function getAttendDiCdDat($model, $isItinit)
    {
        $query  = " SELECT ";
        $query .= "     CASE WHEN ONEDAY_DI_CD IS NULL ";
        $query .= "          THEN DI_CD ";
        $query .= "          ELSE ONEDAY_DI_CD ";
        $query .= "     END AS VALUE, ";
        $query .= "     DI_NAME1 AS LABEL2, ";
        $query .= "     CASE WHEN ONEDAY_DI_CD IS NULL ";
        $query .= "          THEN '999' ";
        $query .= "          ELSE ONEDAY_DI_CD ";
        $query .= "     END AS ITINITI, ";
        $query .= "     DI_MARK AS SHOW ";
        $query .= " FROM ";
        $query .= "     ATTEND_DI_CD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->param["SEND_PRG"] == 'KNJC020A') {
            $query .= "     AND VALUE(REMARK2,'')  <> '1' ";
        }
        if (!$isItinit) {
            $query .= "     AND ONEDAY_DI_CD IS NULL ";
        }
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND RESTRICT_FLG = '1' ";
        }
        $query .= " ORDER BY ";
        $query .= "     right('0' || VALUE(ORDER, '99'), 2), ";
        $query .= "     right('0' || DI_CD, 2) ";

        return $query;
    }

    /* 実施区分取得 */
    public function getSchChrExecutediv($model)
    {
        $query  = " SELECT ";
        $query .= "     VALUE(MAX(EXECUTEDIV), '0') AS EXECUTEDIV ";
        $query .= " FROM ";
        $query .= "     SCH_CHR_DAT ";
        $query .= " WHERE ";
        $query .= "   EXECUTEDATE = '".str_replace("/", "-", $model->param["syoribi"])."' ";
        $query .= "   AND PERIODCD = '{$model->param["periodcd"]}' ";
        if ($model->param["SEND_PRG"] == "KNJC020A" || $model->param["SEND_PRG"] == "") {
            $query .= "   AND CHAIRCD = '{$model->param["chaircd"]}' ";
        } elseif ($model->param["SEND_PRG"] == "KNJC030A") {
            $query .= "   AND CHAIRCD IN (";
            $query .= "       SELECT DISTINCT ";
            $query .= "           STD.CHAIRCD ";
            $query .= "       FROM ";
            $query .= "           SCHREG_REGD_DAT REGD ";
            $query .= "           INNER JOIN CHAIR_STD_DAT STD ON STD.YEAR = REGD.YEAR ";
            $query .= "               AND STD.SEMESTER = REGD.SEMESTER ";
            $query .= "               AND STD.SCHREGNO = REGD.SCHREGNO ";
            $query .= "               AND EXECUTEDATE BETWEEN STD.APPDATE AND STD.APPENDDATE ";
            $query .= "       WHERE ";
            $query .= "           REGD.YEAR = '".CTRL_YEAR."' ";
            $query .= "           AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "           AND REGD.GRADE = '{$model->param["grade"]}' ";
            $query .= "           AND REGD.HR_CLASS = '{$model->param["hrclass"]}' ";
            $query .= "   ) ";
        }
        return $query;
    }

    //実施区分
    public function getExecutediv()
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'C009' ";
        $query .= " ORDER BY ";
        $query .= "     INT(VALUE(NAMESPARE2, '99')), ";
        $query .= "     VALUE ";
        return $query;
    }

    //一日××
    public function getItiniti($model)
    {
        $query  = " SELECT ";
        $query .= "     '1' ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'B001' ";
        $query .= "     AND NAMECD2 = '{$model->param["periodcd"]}' ";
        $query .= "     AND NAMESPARE1 IS NOT NULL ";
        return $query;
    }

    /* 生徒取得 */
    public function getSchregNo($model)
    {
        $query  = " WITH TRANSFER_T AS ( ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     SCHREG_TRANSFER_DAT ";
        $query .= " WHERE ";
        $query .= "     TRANSFERCD IN ('1', '2') ";
        $query .= "     AND '".str_replace("/", "-", $model->param["syoribi"])."' BETWEEN TRANSFER_SDATE AND VALUE(TRANSFER_EDATE, '9999-12-31') ";
        $query .= " GROUP BY ";
        $query .= "     SCHREGNO ";
        $query .= " ) ";
        if ($model->param["SEND_PRG"] == "KNJC020A" || $model->param["SEND_PRG"] == "") {
            $query .= " SELECT ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     REGD.ATTENDNO, ";
            $query .= "     SCHB.NAME, ";
            $query .= "     SCHB.NAME_KANA, ";
            $query .= "     SEX.NAME2 AS SEX, ";
            $query .= "     REGDH.HR_NAME, ";
            $query .= "     CASE WHEN '".STAFFCD."' IN (REGDH.TR_CD1, REGDH.TR_CD2, REGDH.TR_CD3, REGDH.SUBTR_CD1, REGDH.SUBTR_CD2, REGDH.SUBTR_CD3)";
            $query .= "          THEN '1' ";
            $query .= "          ELSE '' ";
            $query .= "     END AS TR_FLG, ";
            $query .= "     TRANSFER_T.CNT AS TRANSFER_CNT, ";
            if ($model->Properties["useKNJC010AGrddiv"] == "1") {
                $query .= "     CASE WHEN SCHB.GRD_DIV != '4' AND SCHB.GRD_DATE < '".str_replace("/", "-", $model->param["syoribi"])."' ";
            } else {
                $query .= "     CASE WHEN SCHB.GRD_DIV IN ('1', '2') AND SCHB.GRD_DATE < '".str_replace("/", "-", $model->param["syoribi"])."' ";
            }
            $query .= "          THEN '1' ";
            $query .= "          ELSE '' ";
            $query .= "     END AS TEN_TAIGAKU, ";
            $query .= "     REGD.GRADE, ";
            $query .= "     REGD.COURSECD, ";
            $query .= "     REGD.MAJORCD, ";
            $query .= "     REGD.COURSECODE ";
            $query .= " FROM ";
            $query .= "     CHAIR_STD_DAT T1 ";
            $query .= "     INNER JOIN SCHREG_REGD_DAT REGD ON T1.SCHREGNO = REGD.SCHREGNO ";
            $query .= "          AND T1.YEAR = REGD.YEAR ";
            $query .= "          AND T1.SEMESTER = REGD.SEMESTER ";
            $query .= "     LEFT JOIN SCHREG_BASE_MST SCHB ON REGD.SCHREGNO = SCHB.SCHREGNO ";
            $query .= "     LEFT JOIN TRANSFER_T ON REGD.SCHREGNO = TRANSFER_T.SCHREGNO ";
            $query .= "     LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGD.YEAR = REGDH.YEAR ";
            $query .= "          AND REGD.SEMESTER = REGDH.SEMESTER ";
            $query .= "          AND REGD.GRADE = REGDH.GRADE ";
            $query .= "          AND REGD.HR_CLASS = REGDH.HR_CLASS ";
            $query .= "     LEFT JOIN NAME_MST SEX ON SEX.NAMECD1 = 'Z002' ";
            $query .= "          AND SCHB.SEX = SEX.NAMECD2 ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "     AND T1.CHAIRCD = '{$model->param["chaircd"]}' ";
            $query .= "     AND '{$model->param["syoribi"]}' between T1.APPDATE AND T1.APPENDDATE ";
            $query .= " ORDER BY ";
            if ($model->Properties["defaultOrderBy"] == '2') {         //学籍番号
                $query .= "     T1.SCHREGNO ";
            } elseif ($model->Properties["defaultOrderBy"] == '3') {    //ふりがな
                $query .= "     TRANSLATE_KANA(SCHB.NAME_KANA) ";
            } else {                                                   //年組番
                $query .= "     REGD.GRADE, ";
                $query .= "     REGD.HR_CLASS, ";
                $query .= "     REGD.ATTENDNO ";
            }
        } elseif ($model->param["SEND_PRG"] == "KNJC030A") {
            $query .= " SELECT ";
            $query .= "     REGD.SCHREGNO, ";
            $query .= "     REGD.ATTENDNO, ";
            $query .= "     SCHB.NAME, ";
            $query .= "     SCHB.NAME_KANA, ";
            $query .= "     SEX.NAME2 AS SEX, ";
            $query .= "     REGDH.HR_NAME, ";
            $query .= "     CASE WHEN '".STAFFCD."' IN (REGDH.TR_CD1, REGDH.TR_CD2, REGDH.TR_CD3, REGDH.SUBTR_CD1, REGDH.SUBTR_CD2, REGDH.SUBTR_CD3)";
            $query .= "          THEN '1' ";
            $query .= "          ELSE '' ";
            $query .= "     END AS TR_FLG, ";
            $query .= "     TRANSFER_T.CNT AS TRANSFER_CNT, ";
            if ($model->Properties["useKNJC010AGrddiv"] == "1") {
                $query .= "     CASE WHEN SCHB.GRD_DIV != '4' AND SCHB.GRD_DATE < '".str_replace("/", "-", $model->param["syoribi"])."' ";
            } else {
                $query .= "     CASE WHEN SCHB.GRD_DIV IN ('1', '2') AND SCHB.GRD_DATE < '".str_replace("/", "-", $model->param["syoribi"])."' ";
            }
            $query .= "          THEN '1' ";
            $query .= "          ELSE '' ";
            $query .= "     END AS TEN_TAIGAKU, ";
            $query .= "     REGD.GRADE, ";
            $query .= "     REGD.COURSECD, ";
            $query .= "     REGD.MAJORCD, ";
            $query .= "     REGD.COURSECODE ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_DAT REGD ";
            $query .= "     LEFT JOIN SCHREG_BASE_MST SCHB ON REGD.SCHREGNO = SCHB.SCHREGNO ";
            $query .= "     LEFT JOIN TRANSFER_T ON REGD.SCHREGNO = TRANSFER_T.SCHREGNO ";
            $query .= "     LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGD.YEAR = REGDH.YEAR ";
            $query .= "          AND REGD.SEMESTER = REGDH.SEMESTER ";
            $query .= "          AND REGD.GRADE = REGDH.GRADE ";
            $query .= "          AND REGD.HR_CLASS = REGDH.HR_CLASS ";
            $query .= "     LEFT JOIN NAME_MST SEX ON SEX.NAMECD1 = 'Z002' ";
            $query .= "          AND SCHB.SEX = SEX.NAMECD2 ";
            $query .= " WHERE ";
            $query .= "     REGD.YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "     AND REGD.GRADE = '{$model->param["grade"]}' ";
            $query .= "     AND REGD.HR_CLASS = '{$model->param["hrclass"]}' ";
            $query .= " ORDER BY ";
            if ($model->Properties["defaultOrderBy"] == '2') {         //学籍番号
                $query .= "     REGD.SCHREGNO ";
            } elseif ($model->Properties["defaultOrderBy"] == '3') {    //ふりがな
                $query .= "     TRANSLATE_KANA(SCHB.NAME_KANA) ";
            } else {                                                   //年組番
                $query .= "     REGD.GRADE, ";
                $query .= "     REGD.HR_CLASS, ";
                $query .= "     REGD.ATTENDNO ";
            }
        }

        return $query;
    }

    /* 詳細情報生徒 */
    public function getSchregInfo($model)
    {
        $query  = " SELECT ";
        $query .= "     SCHB.SCHREGNO, ";
        $query .= "     REGD.ATTENDNO, ";
        $query .= "     SCHB.NAME, ";
        $query .= "     SCHB.NAME_KANA, ";
        $query .= "     REGDH.HR_NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST SCHB ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT REGD ON SCHB.SCHREGNO = REGD.SCHREGNO ";
        $query .= "          AND REGD.YEAR = '".CTRL_YEAR."' ";
        $query .= "          AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGD.YEAR = REGDH.YEAR ";
        $query .= "          AND REGD.SEMESTER = REGDH.SEMESTER ";
        $query .= "          AND REGD.GRADE = REGDH.GRADE ";
        $query .= "          AND REGD.HR_CLASS = REGDH.HR_CLASS ";
        $query .= "     LEFT JOIN NAME_MST SEX ON SEX.NAMECD1 = 'Z002' ";
        $query .= "          AND SCHB.SEX = SEX.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     SCHB.SCHREGNO = '{$model->field["SYOUSAI_SCHREGNO"]}' ";
        $query .= " ORDER BY ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.HR_CLASS, ";
        $query .= "     REGD.ATTENDNO ";

        return $query;
    }

    /* 出欠情報取得 */
    public function getAttend($model, $schregNo, $periodCd, $div = "")
    {
        $query  = " SELECT ";
        $query .= "     T1.DI_CD, ";
        $query .= "     T1.UPDATED, ";
        $query .= "     STF.STAFFNAME, ";
        if ($model->field["TITLE_DISP_SEIGYO"] == "1") {
            $query .= "     L1.DI_MARK AS LABEL ";
        } else {
            $query .= "     L1.DI_NAME1 AS LABEL ";
        }
        $query .= "   , T1.DI_REMARK_CD ";
        $query .= "   , T1.DI_REMARK ";
        $query .= " FROM ";
        $query .= "     ATTEND_DAT T1 ";
        $query .= "     LEFT JOIN ATTEND_DI_CD_DAT L1 ON T1.YEAR = L1.YEAR ";
        $query .= "          AND T1.DI_CD = L1.DI_CD ";
        $query .= "     LEFT JOIN STAFF_MST STF ON T1.REGISTERCD = STF.STAFFCD ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '{$schregNo}' ";
        $query .= "     AND T1.ATTENDDATE = '".str_replace("/", "-", $model->param["syoribi"])."' ";
        $query .= "     AND T1.PERIODCD = '{$periodCd}' ";
        if ($div == "DELCHECK") {
            $query .= "     AND (T1.DI_CD IS NOT NULL ";
            $query .= "          OR T1.DI_REMARK_CD IS NOT NULL ";
            $query .= "          OR T1.DI_REMARK IS NOT NULL) ";
        }

        return $query;
    }

    /* 備考情報取得 */
    public function getRemark($model, $schregNo, $periodCd)
    {
        $query  = " SELECT ";
        $query .= "     VALUE(L1.NAME1, '') || ";
        $query .= "     CASE WHEN VALUE(L1.NAME1, '') = '' OR VALUE(T1.DI_REMARK, '') = '' ";
        $query .= "          THEN '' ";
        $query .= "          ELSE '/' ";
        $query .= "     END ";
        $query .= "     || VALUE(T1.DI_REMARK, '') AS LABEL ";
        $query .= " FROM ";
        $query .= "     ATTEND_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'C901' ";
        $query .= "          AND T1.DI_REMARK_CD = L1.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '{$schregNo}' ";
        $query .= "     AND T1.ATTENDDATE = '".str_replace("/", "-", $model->param["syoribi"])."' ";
        $query .= "     AND T1.PERIODCD = '{$periodCd}' ";

        return $query;
    }

    /* 詳細用B001 */
    public function getB001Syousai($nameCd2)
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'B001' ";
        $query .= "     AND NAMECD2 = '{$nameCd2}' ";
        return $query;
    }

    /* 詳細用備考情報取得 */
    public function getRemarkSyousai($model, $schregNo, $periodCd)
    {
        $query  = " SELECT ";
        $query .= "     T1.DI_REMARK_CD, ";
        $query .= "     T1.DI_REMARK ";
        $query .= " FROM ";
        $query .= "     ATTEND_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '{$schregNo}' ";
        $query .= "     AND T1.ATTENDDATE = '".str_replace("/", "-", $model->param["syoribi"])."' ";
        $query .= "     AND T1.PERIODCD = '{$periodCd}' ";

        return $query;
    }

    /* 詳細用C901 */
    public function getC901Syousai()
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'C901' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    /* 科目略称情報取得 */
    public function getSubclassAbbv($model, $schregNo, $periodCd)
    {
        $query  = " SELECT ";
        $query .= "     SUBCLASSABBV AS LABEL ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_MST T1 ";
        $query .= "     INNER JOIN CHAIR_DAT T2 ON T2.YEAR = '".CTRL_YEAR."' ";
        $query .= "           AND T2.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "           AND T2.CLASSCD = T1.CLASSCD ";
        $query .= "           AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "           AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "           AND T2.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     EXISTS(SELECT ";
        $query .= "                'x' ";
        $query .= "            FROM ";
        $query .= "                ATTEND_DAT E1 ";
        $query .= "            WHERE ";
        $query .= "                E1.SCHREGNO = '{$schregNo}' ";
        $query .= "                AND E1.ATTENDDATE = '".str_replace("/", "-", $model->param["syoribi"])."' ";
        $query .= "                AND E1.PERIODCD = '{$periodCd}' ";
        $query .= "                AND T2.CHAIRCD = E1.CHAIRCD ";
        $query .= "     ) ";

        return $query;
    }

    /* 講座情報取得 */
    public function getChairInfo($model, $schregNo, $periodCd)
    {
        $query  = " SELECT ";
        $query .= "     T1.CHAIRNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND EXISTS( ";
        $query .= "            SELECT ";
        $query .= "                'x' ";
        $query .= "            FROM ";
        $query .= "                ATTEND_DAT E1 ";
        $query .= "            WHERE ";
        $query .= "                E1.SCHREGNO = '{$schregNo}' ";
        $query .= "                AND E1.ATTENDDATE = '".str_replace("/", "-", $model->param["syoribi"])."' ";
        $query .= "                AND E1.PERIODCD = '{$periodCd}' ";
        $query .= "                AND T1.CHAIRCD = E1.CHAIRCD ";
        $query .= "     ) ";

        return $query;
    }

    /* 時間割の情報 */
    public function getSchChrInfo($model, $schregNos)
    {
        $query  = " WITH T_REMARK AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO ";
        $query .= "   , T1.PERIODCD ";
        $query .= "   , VALUE(L1.NAME1, '') || ";
        $query .= "     CASE WHEN VALUE(L1.NAME1, '') = '' OR VALUE(T1.DI_REMARK, '') = '' ";
        $query .= "          THEN '' ";
        $query .= "          ELSE '/' ";
        $query .= "     END ";
        $query .= "     || VALUE(T1.DI_REMARK, '') AS REMARK ";
        $query .= " FROM ";
        $query .= "     ATTEND_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'C901' ";
        $query .= "          AND T1.DI_REMARK_CD = L1.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     T1.ATTENDDATE = '".str_replace("/", "-", $model->param["syoribi"])."' ";
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.SCHREGNO ";
        $query .= "   , T2.PERIODCD ";
        $query .= "   , T2.CHAIRCD ";
        $query .= "   , T3.CHAIRNAME ";
        $query .= "   , T4.SUBCLASSABBV ";
        if ($model->param["SEND_PRG"] == "KNJC030A") {
            $query .= "   , VALUE(T6.EXECUTED, T2.EXECUTED) AS EXECUTED ";
        } else {
            $query .= "   , T2.EXECUTED AS EXECUTED ";
        }
        $query .= "   , T5.DI_CD ";
        $query .= "   , T5.DI_REMARK ";
        $query .= "   , T5.UPDATED AS ATTEND_DAT_UPDATED ";
        $query .= "   , STF.STAFFNAME AS ATTEND_DAT_UPDATED_STAFFNAME ";
        $query .= "   , L1.DI_MARK ";
        $query .= "   , L1.DI_NAME1 ";
        $query .= "   , REM.REMARK ";
        $query .= " FROM     ";
        $query .= "     CHAIR_STD_DAT T1 ";
        $query .= "     INNER JOIN SCH_CHR_DAT T2 ON T2.EXECUTEDATE = '{$model->param["syoribi"]}' ";
        $query .= "         AND T1.CHAIRCD = T2.CHAIRCD ";
        $query .= "         AND T1.YEAR = T2.YEAR ";
        $query .= "         AND T1.SEMESTER = T2.SEMESTER ";
        $query .= "     INNER JOIN CHAIR_DAT T3 ON T1.YEAR = T3.YEAR ";
        $query .= "         AND T1.SEMESTER = T3.SEMESTER ";
        $query .= "         AND T1.CHAIRCD = T3.CHAIRCD ";
        $query .= "     INNER JOIN SUBCLASS_MST T4 ON T3.SUBCLASSCD = T4.SUBCLASSCD ";
        if ($model->useCurriculumcd = '1') {
            $query .= "         AND T3.CLASSCD = T4.CLASSCD ";
            $query .= "         AND T3.SCHOOL_KIND = T4.SCHOOL_KIND ";
            $query .= "         AND T3.CURRICULUM_CD = T4.CURRICULUM_CD ";
        }
        $query .= "     LEFT JOIN ATTEND_DAT T5 ON T1.SCHREGNO = T5.SCHREGNO ";
        $query .= "         AND T2.EXECUTEDATE = T5.ATTENDDATE ";
        $query .= "         AND T2.PERIODCD = T5.PERIODCD ";
        $query .= "     LEFT JOIN ATTEND_DI_CD_DAT L1 ON T1.YEAR = L1.YEAR ";
        $query .= "          AND T5.DI_CD = L1.DI_CD ";
        $query .= "     LEFT JOIN STAFF_MST STF ON STF.STAFFCD = T5.REGISTERCD ";
        $query .= "     LEFT JOIN T_REMARK REM ON REM.SCHREGNO = T5.SCHREGNO ";
        $query .= "          AND REM.PERIODCD = T5.PERIODCD ";
        if ($model->param["SEND_PRG"] == "KNJC030A") {
            $query .= "     LEFT JOIN SCH_CHR_HRATE_DAT T6 ON T6.EXECUTEDATE = '{$model->param["syoribi"]}' ";
            $query .= "         AND T2.PERIODCD = T6.PERIODCD ";
            $query .= "         AND T2.CHAIRCD = T6.CHAIRCD ";
            $query .= "         AND T6.GRADE = '".$model->param["grade"]."' ";
            $query .= "         AND T6.HR_CLASS = '".$model->param["hrclass"]."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND T1.SCHREGNO IN {$schregNos} ";
        $query .= "     AND T2.EXECUTEDATE BETWEEN T1.APPDATE AND T1.APPENDDATE ";
        return $query;
    }

    /* 出欠届けデータ */
    public function getPetition($model, $schregNos)
    {
        $query  = " WITH SCH_EXECUTED AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO ";
        $query .= "   , T2.EXECUTEDATE ";
        $query .= "   , T2.PERIODCD ";
        if ($model->param["SEND_PRG"] == "KNJC030A") {
            $query .= "   , VALUE( ";
            $query .= "         CASE WHEN VALUE(MAX(T6.EXECUTED), '0') = '1' THEN '1' END ";
            $query .= "       , MAX(T2.EXECUTED) ";
            $query .= "     ) AS EXECUTED ";
        } else {
            $query .= "   , MAX(T2.EXECUTED) AS EXECUTED ";
        }
        $query .= " FROM     ";
        $query .= "     CHAIR_STD_DAT T1 ";
        $query .= "     INNER JOIN SCH_CHR_DAT T2 ON T2.EXECUTEDATE BETWEEN T1.APPDATE AND T1.APPENDDATE ";
        $query .= "         AND T1.CHAIRCD = T2.CHAIRCD ";
        $query .= "         AND T1.YEAR = T2.YEAR ";
        $query .= "         AND T1.SEMESTER = T2.SEMESTER ";
        if ($model->param["SEND_PRG"] == "KNJC030A") {
            $query .= "     LEFT JOIN SCH_CHR_HRATE_DAT T6 ON T2.EXECUTEDATE = T6.EXECUTEDATE ";
            $query .= "         AND T2.PERIODCD = T6.PERIODCD ";
            $query .= "         AND T2.CHAIRCD = T6.CHAIRCD ";
            $query .= "         AND T6.GRADE = '".$model->param["grade"]."' ";
            $query .= "         AND T6.HR_CLASS = '".$model->param["hrclass"]."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND T1.SCHREGNO IN {$schregNos} ";
        $query .= "     AND T2.EXECUTEDATE = '{$model->param["syoribi"]}' ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO ";
        $query .= "   , T2.EXECUTEDATE ";
        $query .= "   , T2.PERIODCD ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO ";
        $query .= "   , T1.PERIODCD ";
        $query .= "   , T1.DI_CD ";
        $query .= "   , T1.DI_REMARK ";
        $query .= "   , ATT_DI.DI_NAME1 ";
        $query .= "   , ATT_DI.DI_MARK ";
        $query .= " FROM     ";
        $query .= "     ATTEND_PETITION_DAT T1 ";
        $query .= " LEFT JOIN ATTEND_DI_CD_DAT ATT_DI ON ATT_DI.YEAR = T1.YEAR ";
        $query .= "      AND ATT_DI.DI_CD = T1.DI_CD ";
        $query .= " LEFT JOIN ATTEND_DAT ATT ON ATT.SCHREGNO = T1.SCHREGNO ";
        $query .= "     AND ATT.ATTENDDATE = T1.ATTENDDATE ";
        $query .= "     AND ATT.PERIODCD = T1.PERIODCD ";
        $query .= " LEFT JOIN SCH_EXECUTED EXE ON EXE.SCHREGNO = T1.SCHREGNO ";
        $query .= "     AND EXE.EXECUTEDATE = T1.ATTENDDATE ";
        $query .= "     AND EXE.PERIODCD = T1.PERIODCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SCHREGNO IN {$schregNos} ";
        $query .= "     AND VALUE(ATT.DI_CD, '0') = '0' ";
        $query .= "     AND EXE.EXECUTEDATE IS NOT NULL ";
        $query .= "     AND VALUE(EXE.EXECUTED, '0') = '0' ";
        $query .= " ORDER BY ";
        $query .= "     T1.SEQNO";
        return $query;
    }

    /* 時間割の有無 */
    public function getExistSchChr($model, $schregNo, $peri)
    {
        $query .= " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM     ";
        $query .= "     CHAIR_STD_DAT T1, ";
        $query .= "     SCH_CHR_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND T1.SCHREGNO = '{$schregNo}' ";
        $query .= "     AND '{$model->param["syoribi"]}' between T1.APPDATE AND T1.APPENDDATE ";
        $query .= "     AND T2.EXECUTEDATE = '{$model->param["syoribi"]}' ";
        $query .= "     AND T2.PERIODCD = '{$peri}' ";
        $query .= "     AND T1.CHAIRCD = T2.CHAIRCD ";
        $query .= "     AND T1.YEAR = T2.YEAR ";
        $query .= "     AND T1.SEMESTER = T2.SEMESTER ";
        return $query;
    }

    /* TITLE講座情報取得 */
    public function getChairTitleDiv()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     coalesce(max(NY.NAMECD2), '1') as subclassOrChair ";
        $query .= " FROM ";
        $query .= "     NAME_YDAT as NY ";
        $query .= " WHERE ";
        $query .= "     NY.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NY.NAMECD1 = 'C000' ";
        return $query;
    }

    /* TITLE講座情報取得 */
    public function getChairTitleInfo($model, $schregNo, $periodCd, $titleDiv)
    {
        $query  = " SELECT ";
        $query .= "     T1.CHAIRCD, ";
        if ($titleDiv == "2") {
            $query .= "     left(L1.CHAIRNAME, 9) AS LABEL ";
        } else {
            $query .= "     left(L2.SUBCLASSABBV, 9) AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT T1 ";
        $query .= "     LEFT JOIN CHAIR_DAT L1 ON T1.YEAR = L1.YEAR ";
        $query .= "          AND T1.SEMESTER = L1.SEMESTER ";
        $query .= "          AND T1.CHAIRCD = L1.CHAIRCD ";
        $query .= "     LEFT JOIN SUBCLASS_MST L2 ON L1.CLASSCD = L2.CLASSCD ";
        $query .= "          AND L1.SCHOOL_KIND = L2.SCHOOL_KIND ";
        $query .= "          AND L1.CURRICULUM_CD = L2.CURRICULUM_CD ";
        $query .= "          AND L1.SUBCLASSCD = L2.SUBCLASSCD, ";
        $query .= "     SCH_CHR_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND T1.CHAIRCD = T2.CHAIRCD ";
        $query .= "     AND T1.SCHREGNO = '{$schregNo}' ";
        $query .= "     AND '".str_replace("/", "-", $model->param["syoribi"])."' BETWEEN T1.APPDATE AND T1.APPENDDATE ";
        $query .= "     AND T2.EXECUTEDATE = '".str_replace("/", "-", $model->param["syoribi"])."' ";
        $query .= "     AND T2.PERIODCD = '{$periodCd}' ";
        return $query;
    }

    /* 校時取得 */
    public function getPeriod($periodcd)
    {
        $query  = " SELECT ";
        $query .= "     NAME1 AS PERIOD_NAME ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'B001' ";
        $query .= "     AND NAMECD2 = '{$periodcd}' ";

        return $query;
    }

    /* 職員名取得 */
    public function getStaff()
    {
        $query  = " SELECT ";
        $query .= "     STAFFNAME ";
        $query .= " FROM ";
        $query .= "     STAFF_MST ";
        $query .= " WHERE ";
        $query .= "     STAFFCD = '".STAFFCD."' ";

        return $query;
    }

    /* 講座取得 */
    public function getChairName($model)
    {
        $query  = " SELECT ";
        $query .= "     CHAIRNAME ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND T1.CHAIRCD = '{$model->param["chaircd"]}' ";

        return $query;
    }

    /* 出欠判定 */
    public function getCountSchZumi($model, $chairCd, $periodCd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS COUNT_SCH_ZUMI ";
        $query .= " FROM ";
        $query .= "     SCH_CHR_DAT ";
        $query .= " WHERE ";
        $query .= "   EXECUTEDATE = '".str_replace("/", "-", $model->param["syoribi"])."' ";
        $query .= "   AND PERIODCD = '{$periodCd}' ";
        $query .= "   AND CHAIRCD = '{$chairCd}' ";
        $query .= "   AND EXECUTED = '1' ";

        return $query;
    }

    /* 出欠判定 */
    public function getCountHrZumi($model, $chairCd, $periodCd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS COUNT_HR_ZUMI ";
        $query .= " FROM ";
        $query .= "     SCH_CHR_HRATE_DAT ";
        $query .= " WHERE ";
        $query .= "   EXECUTEDATE = '".str_replace("/", "-", $model->param["syoribi"])."' ";
        $query .= "   AND PERIODCD = '{$periodCd}' ";
        $query .= "   AND CHAIRCD = '{$chairCd}' ";
        $query .= "   AND EXECUTED = '1' ";

        return $query;
    }

    /* 担当職員取得 */
    public function getChairStaff($model, $chairCd)
    {
        $query  = " SELECT ";
        $query .= "     STF.STAFFNAME ";
        $query .= " FROM ";
        $query .= "     CHAIR_STF_DAT C_STF ";
        $query .= "     INNER JOIN STAFF_MST STF ON C_STF.STAFFCD = STF.STAFFCD ";
        $query .= " WHERE ";
        $query .= "     C_STF.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND C_STF.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND C_STF.CHAIRCD = '{$chairCd}' ";
        $query .= " ORDER BY ";
        $query .= "     C_STF.CHARGEDIV DESC, ";
        $query .= "     C_STF.STAFFCD ";
        $query .= " FETCH FIRST 1 ROWS ONLY ";

        return $query;
    }

    /* 単位数取得 */
    public function getCredit($model, $coursecd, $majorcd, $grade, $coursecode, $chairCd)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T4.CREDITS ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T2 ";
        $query .= "     LEFT JOIN SUBCLASS_MST T3 ON T3.CLASSCD = T2.CLASSCD ";
        $query .= "         AND T3.SCHOOL_KIND = T2.SCHOOL_KIND ";
        $query .= "         AND T3.CURRICULUM_CD = T2.CURRICULUM_CD ";
        $query .= "         AND T3.SUBCLASSCD = T2.SUBCLASSCD ";
        $query .= "     LEFT JOIN V_CREDIT_MST T4 ON T4.YEAR = T2.YEAR ";
        $query .= "         AND T4.COURSECD = '{$coursecd}' ";
        $query .= "         AND T4.MAJORCD = '{$majorcd}' ";
        $query .= "         AND T4.GRADE = '{$grade}' ";
        $query .= "         AND T4.COURSECODE = '{$coursecode}' ";
        $query .= "         AND T4.CLASSCD = T3.CLASSCD ";
        $query .= "         AND T4.SCHOOL_KIND = T3.SCHOOL_KIND ";
        $query .= "         AND T4.CURRICULUM_CD = T3.CURRICULUM_CD ";
        $query .= "         AND T4.SUBCLASSCD = T3.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T2.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T2.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND T2.CHAIRCD = '{$chairCd}' ";

        return $query;
    }

    //出欠集計開始日付などを取得
    public function getAttendDate($z010, $semester, $month, $day)
    {
        $checkMonth = $month < "04" ? $month + 12 : $month;
        $query  = "SELECT SEMESTER ";
        $query .= "      ,MONTH ";
        $query .= "      ,MAX(APPOINTED_DAY) AS MAX_APP ";
        $query .= "FROM   ATTEND_SEMES_DAT ";
        $query .= "WHERE  YEAR = '".CTRL_YEAR."' ";
        $query .= "       AND SEMESTER <= '{$semester}' ";
        if ($z010 == 'KINDAI') {
            $query .= "       AND (CASE WHEN MONTH < '04' THEN RIGHT('00' || CAST(INT(MONTH) + 12 AS CHAR(2)), 2) ELSE MONTH END < '{$checkMonth}' ";
            $query .= "            OR (MONTH = '{$month}' AND APPOINTED_DAY <> 1 AND APPOINTED_DAY <= '{$day}') "; // 近大の締日1は翌月1日
            $query .= "           ) ";
        } else {
            $query .= "       AND (CASE WHEN MONTH < '04' THEN RIGHT('00' || CAST(INT(MONTH) + 12 AS CHAR(2)), 2) ELSE MONTH END < '{$checkMonth}' ";
            $query .= "            OR (MONTH = '{$month}' AND APPOINTED_DAY < '{$day}') ";
            $query .= "           ) ";
        }
        $query .= "GROUP BY SEMESTER, MONTH ";
        $query .= "ORDER BY ";
        $query .= "     CASE WHEN MONTH < '04' THEN '1' ELSE '0' END, ";
        $query .= "     SEMESTER, ";
        $query .= "     MONTH ";

        return $query;
    }

    /* 出欠上限判定 */
    public function getSyukketuJougen($model, $schregNo, $chairCd, $attend_seme_month, $attend_sdate, $schoolMst, $chairDat)
    {
        $semeMonth = " IN (";
        $comma = "";
        foreach ($attend_seme_month as $sm) {
            $semeMonth .= $comma . "'{$sm}'";
            $comma  =", ";
        }
        if ($comma == "") {
            $semeMonth .= "'')";
        } else {
            $semeMonth .= ")";
        }

        $query .= "  ";
        $query .= " WITH ATTEND_SUBCLASS_0 AS (SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     SEMESTER, ";
        $query .= "     CLASSCD, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     CURRICULUM_CD, ";
        $query .= "     SUBCLASSCD, ";
        $query .= "     SUM(VALUE(LATE, 0) + VALUE(EARLY, 0)) AS LATE_EARLY, ";
        $query .= "     SUM(VALUE(SICK, 0) + VALUE(NOTICE, 0) + VALUE(NONOTICE,0) + VALUE(NURSEOFF, 0)) AS SICK ";
        $query .= "  FROM ATTEND_SUBCLASS_DAT T1 ";
        $query .= "  WHERE ";
        $query .= "   T1.COPYCD = '0' ";
        $query .= "   AND T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "   AND T1.SCHREGNO = '{$schregNo}' ";
        $query .= "   AND T1.SEMESTER || '-' || T1.MONTH {$semeMonth} ";
        $query .= "   AND T1.CLASSCD = '{$chairDat["CLASSCD"]}' ";
        $query .= "   AND T1.SCHOOL_KIND= '{$chairDat["SCHOOL_KIND"]}' ";
        $query .= "   AND T1.CURRICULUM_Cd= '{$chairDat["CURRICULUM_CD"]}' ";
        $query .= "   AND T1.SUBCLASSCD = '{$chairDat["SUBCLASSCD"]}' ";
        $query .= "  GROUP BY SCHREGNO, ";
        $query .= "     SEMESTER, ";
        $query .= "     CLASSCD, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     CURRICULUM_CD, ";
        $query .= "     SUBCLASSCD ";
        $query .= "  UNION ALL ";
        $query .= "     SELECT ";
        $query .= "     T2.SCHREGNO, ";
        $query .= "     T2.SEMESTER, ";
        $query .= "     T5.CLASSCD, ";
        $query .= "     T5.SCHOOL_KIND, ";
        $query .= "     T5.CURRICULUM_CD, ";
        $query .= "     T5.SUBCLASSCD, ";
        $query .= "     SUM(CASE WHEN ATT_DI.REP_DI_CD IN ('15', '16', '23', '24') THEN 1 END) AS LATE_EARLY, ";
        $query .= "     SUM( CASE ";
        if ($schoolMst["SUB_OFFDAYS"] == '1') {
            $query .= "     WHEN T7.SCHREGNO IS NOT NULL THEN 1 ";
        }
        $query .= "         WHEN CASE WHEN ATT_DI.ATSUB_REPL_DI_CD IS NOT NULL THEN ATT_DI.ATSUB_REPL_DI_CD ELSE ATT_DI.REP_DI_CD END IN ( ";
        if ($schoolMst["SUB_ABSENT"] == '1') {
            $query .= "   '1', '8', ";
        }
        if ($schoolMst["SUB_SUSPEND"] == '1') {
            $query .= "   '2', '9', ";
        }
        if ($model->Properties["useVirus"] == "true") {
            if ($schoolMst["SUB_VIRUS"] == '1') {
                $query .= "   '19', '20', ";
            }
        }
        if ($model->Properties["useKoudome"] == "true") {
            if ($schoolMst["SUB_KOUDOME"] == '1') {
                $query .= "   '25', '26', ";
            }
        }
        if ($schoolMst["SUB_MOURNING"] == '1') {
            $query .= "   '3', '10', ";
        }
        if ($schoolMst["SUB_ABSENT"] == '1') {
            $query .= "   '1', '8', ";
        }
        $query .= "     '4', '5', '6', '14', '11', '12', '13') THEN 1 END) AS SICK ";
        $query .= "  FROM SCH_CHR_DAT T1 ";
        $query .= "  INNER JOIN CHAIR_STD_DAT T2 ON T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE ";
        $query .= "      AND T2.CHAIRCD = T1.CHAIRCD ";
        $query .= "  INNER JOIN ATTEND_DAT T3 ON T3.SCHREGNO = T2.SCHREGNO ";
        $query .= "      AND T3.ATTENDDATE = T1.EXECUTEDATE ";
        $query .= "      AND T3.PERIODCD = T1.PERIODCD ";
        $query .= "      AND T3.CHAIRCD = T1.CHAIRCD ";
        $query .= "  INNER JOIN CHAIR_DAT T5 ON T5.YEAR = T2.YEAR ";
        $query .= "      AND T5.SEMESTER = T2.SEMESTER ";
        $query .= "      AND T5.CHAIRCD = T2.CHAIRCD ";
        $query .= "  INNER JOIN SUBCLASS_MST T6 ON T6.SUBCLASSCD = T5.SUBCLASSCD ";
        $query .= "      AND T6.CLASSCD = T5.CLASSCD ";
        $query .= "      AND T6.SCHOOL_KIND = T5.SCHOOL_KIND ";
        $query .= "      AND T6.CURRICULUM_CD = T5.CURRICULUM_CD ";
        $query .= "  LEFT JOIN ATTEND_DI_CD_DAT ATT_DI ON ATT_DI.YEAR = T3.YEAR ";
        $query .= "      AND ATT_DI.DI_CD = T3.DI_CD ";
        $query .= "  LEFT JOIN SCHREG_TRANSFER_DAT T7 ON T7.SCHREGNO = T2.SCHREGNO ";
        $query .= "      AND T7.TRANSFERCD = '2'  ";
        $query .= "      AND T1.EXECUTEDATE BETWEEN T7.TRANSFER_SDATE AND T7.TRANSFER_EDATE  ";
        $query .= "  WHERE ";
        $query .= "   T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "   AND T2.SCHREGNO = '{$schregNo}' ";
        $query .= "   AND T1.EXECUTEDATE BETWEEN '{$attend_sdate}' AND '{$model->param["syoribi"]}' ";
        $query .= "   AND T3.DI_CD IS NOT NULL ";
        $query .= "   AND T5.CLASSCD = '{$chairDat["CLASSCD"]}' ";
        $query .= "   AND T5.SCHOOL_KIND= '{$chairDat["SCHOOL_KIND"]}' ";
        $query .= "   AND T5.CURRICULUM_CD= '{$chairDat["CURRICULUM_CD"]}' ";
        $query .= "   AND T5.SUBCLASSCD = '{$chairDat["SUBCLASSCD"]}' ";
        $query .= "  GROUP BY ";
        $query .= "     T2.SCHREGNO, ";
        $query .= "     T2.SEMESTER, ";
        $query .= "     T5.CLASSCD, ";
        $query .= "     T5.SCHOOL_KIND, ";
        $query .= "     T5.CURRICULUM_CD, ";
        $query .= "     T5.SUBCLASSCD ";
        $query .= " ), ATTEND_SUBCLASS AS (SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     SEMESTER, ";
        $query .= "     CLASSCD, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     CURRICULUM_CD, ";
        $query .= "     SUBCLASSCD, ";
        $query .= "     SUM(LATE_EARLY) AS LATE_EARLY, ";
        $query .= "     SUM(SICK) AS SICK ";
        $query .= "  FROM ATTEND_SUBCLASS_0 T1 ";
        $query .= "  GROUP BY SCHREGNO, ";
        $query .= "     SEMESTER, ";
        $query .= "     CLASSCD, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     CURRICULUM_CD, ";
        $query .= "     SUBCLASSCD ";
        $query .= " ), T_KEKKA AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, T2.CHAIRCD, ";
        $query .= "   T4.CREDITS, ";
        $query .= "   VALUE(T5_1.SICK, 0) + VALUE(T5_2.SICK, 0) + VALUE(T5_3.SICK, 0) AS SICK, ";
        $query .= "   '[' || RTRIM(CAST(VALUE(T5_1.LATE_EARLY,0) AS CHAR(3))) ||  ";
        $query .= "     CASE WHEN SEMESTERDIV <= '1' THEN ''  ";
        $query .= "     ELSE ',' || RTRIM(CAST(VALUE(T5_2.LATE_EARLY,0) AS CHAR(3))) || ";
        $query .= "       CASE WHEN SEMESTERDIV <= '2' THEN '' ";
        $query .= "       ELSE ',' || RTRIM(CAST(VALUE(T5_3.LATE_EARLY,0) AS CHAR(3))) ";
        $query .= "       END ";
        $query .= "     END || ']' ";
        $query .= "   AS LATE_INFO, ";
        $query .= "  VALUE(T5_1.SICK, 0) + VALUE(T5_2.SICK, 0) + VALUE(T5_3.SICK, 0) AS KETSUJI, ";
        $query .= "   VALUE(T5_1.SICK, 0) + VALUE(T5_2.SICK, 0) + VALUE(T5_3.SICK, 0) + ";
        $query .= "     CASE WHEN SCHM.ABSENT_COV IS NULL OR SCHM.ABSENT_COV_LATE IS NULL THEN 0 ";
        $query .= "          WHEN SCHM.ABSENT_COV = '1' THEN VALUE(T5_1.LATE_EARLY, 0) / INT(SCHM.ABSENT_COV_LATE) + VALUE(T5_2.LATE_EARLY, 0) / INT(SCHM.ABSENT_COV_LATE) + VALUE(T5_3.LATE_EARLY, 0) / INT(SCHM.ABSENT_COV_LATE)  ";
        $query .= "          WHEN SCHM.ABSENT_COV = '2' THEN (VALUE(T5_1.LATE_EARLY, 0) + VALUE(T5_2.LATE_EARLY, 0) + VALUE(T5_3.LATE_EARLY, 0)) / INT(SCHM.ABSENT_COV_LATE)  ";
        $query .= "          WHEN SCHM.ABSENT_COV = '3' THEN VALUE(T5_1.LATE_EARLY, 0) / DOUBLE(SCHM.ABSENT_COV_LATE) + VALUE(T5_2.LATE_EARLY, 0) / DOUBLE(SCHM.ABSENT_COV_LATE) + VALUE(T5_3.LATE_EARLY, 0) / DOUBLE(SCHM.ABSENT_COV_LATE)  ";
        $query .= "          WHEN SCHM.ABSENT_COV = '4' THEN (VALUE(T5_1.LATE_EARLY, 0) + VALUE(T5_2.LATE_EARLY, 0) + VALUE(T5_3.LATE_EARLY, 0)) / DOUBLE(SCHM.ABSENT_COV_LATE)  ";
        $query .= "          ELSE 0 ";
        $query .= "    END AS KEKKA, ";
        $query .= "     T7.NAMESPARE1 AS KAISU_OR_SHUSU,  ";
        $query .= "   CASE WHEN T7.NAMESPARE1 = '1' THEN ";
        $query .= "     CASE WHEN '1' = '".CTRL_SEMESTER."' THEN T4.ABSENCE_WARN ";
        $query .= "          WHEN '2' = '".CTRL_SEMESTER."' THEN T4.ABSENCE_WARN2 ";
        $query .= "          WHEN '3' = '".CTRL_SEMESTER."' THEN T4.ABSENCE_WARN3 ";
        $query .= "     END ";
        $query .= "     ELSE  ";
        $query .= "     CASE WHEN '1' = '".CTRL_SEMESTER."' THEN T4.ABSENCE_WARN_SHUTOKU_SEM1 ";
        $query .= "          WHEN '2' = '".CTRL_SEMESTER."' THEN T4.ABSENCE_WARN_SHUTOKU_SEM2 ";
        $query .= "          WHEN '3' = '".CTRL_SEMESTER."' THEN T4.ABSENCE_WARN_SHUTOKU_SEM3 ";
        $query .= "     END ";
        $query .= "    END AS KAISU_SHUSU, ";
        $query .= "    CASE WHEN SCHM.JUGYOU_JISU_FLG = '2' THEN T6.COMP_ABSENCE_HIGH ";
        $query .= "    ELSE  T4.ABSENCE_HIGH ";
        $query .= "   END - VALUE(CASE WHEN T7.NAMESPARE1 = '1' THEN ";
        $query .= "     CASE WHEN '1' = '".CTRL_SEMESTER."' THEN T4.ABSENCE_WARN ";
        $query .= "          WHEN '2' = '".CTRL_SEMESTER."' THEN T4.ABSENCE_WARN2 ";
        $query .= "          WHEN '3' = '".CTRL_SEMESTER."' THEN T4.ABSENCE_WARN3 ";
        $query .= "     END ";
        $query .= "     ELSE  ";
        $query .= "     CASE WHEN '1' = '".CTRL_SEMESTER."' THEN T4.ABSENCE_WARN_RISHU_SEM1 ";
        $query .= "          WHEN '2' = '".CTRL_SEMESTER."' THEN T4.ABSENCE_WARN_RISHU_SEM2 ";
        $query .= "          WHEN '3' = '".CTRL_SEMESTER."' THEN T4.ABSENCE_WARN_RISHU_SEM3 ";
        $query .= "     END ";
        $query .= "    END, 0) AS RISHU_JOGENCHI, ";
        $query .= "    CASE WHEN SCHM.JUGYOU_JISU_FLG = '2' THEN T6.GET_ABSENCE_HIGH ";
        $query .= "    ELSE T4.GET_ABSENCE_HIGH ";
        $query .= "   END - VALUE(CASE WHEN T7.NAMESPARE1 = '1' THEN ";
        $query .= "     CASE WHEN '1' = '".CTRL_SEMESTER."' THEN T4.ABSENCE_WARN ";
        $query .= "          WHEN '2' = '".CTRL_SEMESTER."' THEN T4.ABSENCE_WARN2 ";
        $query .= "          WHEN '3' = '".CTRL_SEMESTER."' THEN T4.ABSENCE_WARN3 ";
        $query .= "     END ";
        $query .= "     ELSE  ";
        $query .= "     CASE WHEN '1' = '".CTRL_SEMESTER."' THEN T4.ABSENCE_WARN_SHUTOKU_SEM1 ";
        $query .= "          WHEN '2' = '".CTRL_SEMESTER."' THEN T4.ABSENCE_WARN_SHUTOKU_SEM2 ";
        $query .= "          WHEN '3' = '".CTRL_SEMESTER."' THEN T4.ABSENCE_WARN_SHUTOKU_SEM3 ";
        $query .= "     END ";
        $query .= "    END, 0) AS SHUTOKU_JOGENCHI ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN CHAIR_STD_DAT T31 ON T31.YEAR = T1.YEAR ";
        $query .= "         AND T31.SEMESTER = T1.SEMESTER ";
        $query .= "         AND T31.SCHREGNO = T1.SCHREGNO ";
        $query .= "             AND '".str_replace("/", "-", $model->param["syoribi"])."' BETWEEN T31.APPDATE AND T31.APPENDDATE ";
        $query .= "     INNER JOIN CHAIR_DAT T2 ON T2.YEAR = T1.YEAR ";
        $query .= "         AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "         AND T2.CHAIRCD = T31.CHAIRCD ";
        $query .= "     LEFT JOIN SUBCLASS_MST T3 ON T3.CLASSCD = T2.CLASSCD ";
        $query .= "         AND T3.SCHOOL_KIND = T2.SCHOOL_KIND ";
        $query .= "         AND T3.CURRICULUM_CD = T2.CURRICULUM_CD ";
        $query .= "         AND T3.SUBCLASSCD = T2.SUBCLASSCD ";
        $query .= "     LEFT JOIN V_CREDIT_MST T4 ON T4.YEAR = T1.YEAR ";
        $query .= "         AND T4.COURSECD = T1.COURSECD ";
        $query .= "         AND T4.MAJORCD = T1.MAJORCD ";
        $query .= "         AND T4.GRADE = T1.GRADE ";
        $query .= "         AND T4.COURSECODE = T1.COURSECODE ";
        $query .= "         AND T4.CLASSCD = T3.CLASSCD ";
        $query .= "         AND T4.SCHOOL_KIND = T3.SCHOOL_KIND ";
        $query .= "         AND T4.CURRICULUM_CD = T3.CURRICULUM_CD ";
        $query .= "         AND T4.SUBCLASSCD = T3.SUBCLASSCD ";
        $query .= "     LEFT JOIN V_SCHOOL_MST SCHM ON SCHM.YEAR = T1.YEAR ";
        $query .= "     LEFT JOIN ATTEND_SUBCLASS T5_1 ON T5_1.SCHREGNO = T1.SCHREGNO ";
        $query .= "         AND T5_1.SEMESTER = '1' ";
        $query .= "         AND T5_1.CLASSCD = T3.CLASSCD ";
        $query .= "         AND T5_1.SCHOOL_KIND = T3.SCHOOL_KIND ";
        $query .= "         AND T5_1.CURRICULUM_CD = T3.CURRICULUM_CD ";
        $query .= "         AND T5_1.SUBCLASSCD = T3.SUBCLASSCD ";
        $query .= "     LEFT JOIN ATTEND_SUBCLASS T5_2 ON T5_2.SCHREGNO = T1.SCHREGNO ";
        $query .= "         AND T5_2.SEMESTER = '2' ";
        $query .= "         AND T5_2.CLASSCD = T3.CLASSCD ";
        $query .= "         AND T5_2.SCHOOL_KIND = T3.SCHOOL_KIND ";
        $query .= "         AND T5_2.CURRICULUM_CD = T3.CURRICULUM_CD ";
        $query .= "         AND T5_2.SUBCLASSCD = T3.SUBCLASSCD ";
        $query .= "     LEFT JOIN ATTEND_SUBCLASS T5_3 ON T5_3.SCHREGNO = T1.SCHREGNO ";
        $query .= "         AND T5_3.SEMESTER = '3' ";
        $query .= "         AND T5_3.CLASSCD = T3.CLASSCD ";
        $query .= "         AND T5_3.SCHOOL_KIND = T3.SCHOOL_KIND ";
        $query .= "         AND T5_3.CURRICULUM_CD = T3.CURRICULUM_CD ";
        $query .= "         AND T5_3.SUBCLASSCD = T3.SUBCLASSCD ";
        $query .= "     LEFT JOIN SCHREG_ABSENCE_HIGH_DAT T6 ON T6.YEAR = T1.YEAR ";
        $query .= "         AND T6.DIV = '1' ";
        $query .= "         AND T6.CLASSCD = T3.CLASSCD ";
        $query .= "         AND T6.SCHOOL_KIND = T3.SCHOOL_KIND ";
        $query .= "         AND T6.CURRICULUM_CD = T3.CURRICULUM_CD ";
        $query .= "         AND T6.SUBCLASSCD = T3.SUBCLASSCD ";
        $query .= "     AND T6.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN V_NAME_MST T7 ON T7.YEAR = T6.YEAR ";
        $query .= "         AND T7.NAMECD1 = 'C042' ";
        $query .= "         AND T7.NAMECD2 ='01' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND T1.SCHREGNO = '{$schregNo}' ";
        $query .= "     AND T2.CHAIRCD = '{$chairCd}' ";
        $query .= "  ";
        $query .= " ) ";
        $query .= " SELECT T1.SCHREGNO, ";
        $query .= "        T1.CHAIRCD, ";
        $query .= "        T1.CREDITS, ";
        $query .= "        T1.SICK, ";
        $query .= "        T1.LATE_INFO, ";
        $query .= "        T1.KEKKA, ";
        $query .= "        T1.RISHU_JOGENCHI, ";
        $query .= "        T1.SHUTOKU_JOGENCHI, ";
        $query .= "        T1.KAISU_SHUSU, ";
        $query .= "        T1.KAISU_OR_SHUSU, ";
        $query .= "        CASE WHEN 0 < T1.RISHU_JOGENCHI AND T1.RISHU_JOGENCHI < T1.KEKKA THEN 'RED' ";
        $query .= "             WHEN 0 < T1.SHUTOKU_JOGENCHI AND T1.SHUTOKU_JOGENCHI < T1.KEKKA THEN 'YELLOW' ";
        $query .= "        END AS COLOR ";
        $query .= " FROM T_KEKKA T1 ";

        return $query;
    }

    /* 表示制御 */
    public function getSeigyo()
    {
        $query  = " WITH SEIGYO_T(VALUE, LABEL) AS ( ";
        $query .= " VALUES('1', '勤怠マーク') ";
        $query .= " UNION ";
        $query .= " VALUES('2', '勤怠名称') ";
        $query .= " UNION ";
        $query .= " VALUES('3', '備考') ";
        $query .= " UNION ";
        $query .= " VALUES('4', '科目略称') ";
        $query .= " UNION ";
        $query .= " VALUES('5', '講座') ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SEIGYO_T ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    /* 異動情報 */
    public function getIdouInfo($model, $schregNo)
    {
        $query  = " SELECT ";
        $query .= "   T1.SCHREGNO, ";
        $query .= "   CASE WHEN GRD_DIV IS NOT NULL AND GRD_DIV NOT IN ('0', '4') THEN ";
        $query .= "         L4.NAME1 || ' @ ' || CAST(T1.GRD_DATE AS VARCHAR(10)) ";
        $query .= "        WHEN L3.SCHREGNO IS NOT NULL THEN ";
        $query .= "         L5.NAME1 || ' @ ' || CAST(L3.TRANSFER_SDATE AS VARCHAR(10)) || ' ～ ' || CAST(L3.TRANSFER_EDATE AS VARCHAR(10)) ";
        $query .= "   END AS REMARK  ";
        $query .= " FROM ";
        $query .= " SCHREG_BASE_MST T1 ";
        $query .= " LEFT JOIN (SELECT SCHREGNO, MIN(TRANSFER_SDATE) AS MIN_TRANSFER_SDATE ";
        $query .= "            FROM SCHREG_TRANSFER_DAT ";
        $query .= "            WHERE '".str_replace("/", "-", $model->param["syoribi"])."' BETWEEN TRANSFER_SDATE AND TRANSFER_EDATE ";
        $query .= "            GROUP BY SCHREGNO) L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " LEFT JOIN (SELECT SCHREGNO, TRANSFER_SDATE, MIN(TRANSFERCD) AS MIN_TRANSFERCD ";
        $query .= "            FROM SCHREG_TRANSFER_DAT ";
        $query .= "            GROUP BY SCHREGNO, TRANSFER_SDATE) L2 ON L2.SCHREGNO = L1.SCHREGNO ";
        $query .= "   AND L2.TRANSFER_SDATE = L1.MIN_TRANSFER_SDATE ";
        $query .= " LEFT JOIN SCHREG_TRANSFER_DAT L3 ON L3.SCHREGNO = L2.SCHREGNO ";
        $query .= "   AND L3.TRANSFER_SDATE = L2.TRANSFER_SDATE ";
        $query .= "   AND L3.TRANSFERCD = L2.MIN_TRANSFERCD ";
        $query .= " LEFT JOIN NAME_MST L4 ON L4.NAMECD1 = 'A003' ";
        $query .= "   AND L4.NAMECD2 = T1.GRD_DIV ";
        $query .= " LEFT JOIN NAME_MST L5 ON L5.NAMECD1 = 'A004' ";
        $query .= "   AND L5.NAMECD2 = L3.TRANSFERCD ";
        $query .= " WHERE ";
        $query .= "   T1.SCHREGNO = '{$schregNo}' ";

        return $query;
    }

    /* 講座CD取得 */
    public function getChairCd($model, $schregNo, $periodCd)
    {
        $query  = " SELECT ";
        $query .= "     T1.CHAIRCD ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT T1 ";
        $query .= "     INNER JOIN SCH_CHR_DAT T2 ON T2.EXECUTEDATE BETWEEN T1.APPDATE AND T1.APPENDDATE  ";
        $query .= "          AND T2.PERIODCD = '{$periodCd}' ";
        $query .= "          AND T2.CHAIRCD = T1.CHAIRCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND T1.SCHREGNO = '{$schregNo}' ";
        $query .= "     AND T2.EXECUTEDATE = '".str_replace("/", "-", $model->param["syoribi"])."' ";
        return $query;
    }

    /* 授業内容取得 */
    public function getSchChrRemarkDat($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.CHAIRCD ";
        $query .= "     , T2.EXECUTEDATE ";
        $query .= "     , T2.REMARK ";
        $query .= " FROM ";
        $query .= "     SCH_CHR_DAT T1 ";
        $query .= "     LEFT JOIN SCH_CHR_REMARK_DAT T2 ";
        $query .= "         ON T2.EXECUTEDATE = T1.EXECUTEDATE ";
        $query .= "         AND T2.PERIODCD = T1.PERIODCD ";
        $query .= "         AND T2.CHAIRCD = T1.CHAIRCD ";
        $query .= "         AND T2.REMARK_DIV = '01' ";
        $query .= "     INNER JOIN CHAIR_STD_DAT T3 ";
        $query .= "         ON T3.YEAR = T1.YEAR ";
        $query .= "         AND T3.SEMESTER = T1.SEMESTER ";
        $query .= "         AND T3.CHAIRCD = T1.CHAIRCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND T1.EXECUTEDATE = '$model->jugyouNaiyouSyoribi' ";
        $query .= "     AND T1.PERIODCD = '$model->jugyouNaiyouPeriodcd' ";
        $query .= "     AND T3.SCHREGNO = '$model->jugyouNaiyouSchregno' ";
        $query .= "     AND '$model->jugyouNaiyouSyoribi' BETWEEN T3.APPDATE AND T3.APPENDDATE ";

        return $query;
    }

    //表示している生徒で最新の最終更新日時を取得
    public function getLastUpdate($db, $model)
    {
        $query  = "";
        $query .= " WITH SCHREG AS ( ";
        if ($model->param["SEND_PRG"] == "KNJC020A" || $model->param["SEND_PRG"] == "") {
            $query .= " SELECT ";
            $query .= "     T1.SCHREGNO ";
            $query .= " FROM ";
            $query .= "     CHAIR_STD_DAT T1 ";
            $query .= "     INNER JOIN SCHREG_REGD_DAT REGD ON T1.SCHREGNO = REGD.SCHREGNO ";
            $query .= "          AND T1.YEAR = REGD.YEAR ";
            $query .= "          AND T1.SEMESTER = REGD.SEMESTER ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "     AND T1.CHAIRCD = '{$model->param["chaircd"]}' ";
            $query .= "     AND '{$model->param["syoribi"]}' between T1.APPDATE AND T1.APPENDDATE ";
        } elseif ($model->param["SEND_PRG"] == "KNJC030A") {
            $query .= " SELECT ";
            $query .= "     REGD.SCHREGNO ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_DAT REGD ";
            $query .= " WHERE ";
            $query .= "     REGD.YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "     AND REGD.GRADE = '{$model->param["grade"]}' ";
            $query .= "     AND REGD.HR_CLASS = '{$model->param["hrclass"]}' ";
        }
        $query .= " ) ";
        $query .= " SELECT MAX(UPDATED) AS LASTUPDATE ";
        $query .= " FROM ATTEND_DAT ";
        $query .= " WHERE ATTENDDATE = '{$model->param["syoribi"]}' ";
        $query .= "   AND SCHREGNO IN (SELECT SCHREGNO FROM SCHREG) ";

        return $query;
    }

    /* 出欠更新 */
    public function getUpdateAttend($db, $model, $schregNo, $periodCd, $diCd, $chairCd)
    {
        if ($model->Properties["useAttendSemesHrRemark"] == "1") {
            if ($model->param["SEND_PRG"] == "KNJC030A") {
                $month = substr($model->param["syoribi"], 5, 2);
                $query  = " DELETE FROM ";
                $query .= "     ATTEND_SEMES_REMARK_HR_DAT ";
                $query .= " WHERE ";
                $query .= "     YEAR = '". CTRL_YEAR ."' ";
                $query .= "     AND MONTH = '{$month}' ";
                $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
                $query .= "     AND GRADE = '{$model->param["grade"]}' ";
                $query .= "     AND HR_CLASS = '{$model->param["hrclass"]}' ";
                $db->query($query);

                $data = array();
                $data["YEAR"][TEXT]         = CTRL_YEAR;
                $data["MONTH"][TEXT]        = $month;
                $data["SEMESTER"][TEXT]     = CTRL_SEMESTER;
                $data["GRADE"][TEXT]        = $model->param["grade"];
                $data["HR_CLASS"][TEXT]     = $model->param["hrclass"];
                $data["REMARK1"][TEXT]      = $model->field["HR_REMARK"];
                $data["REGISTERCD"][TEXT]   = STAFFCD ;
                $data["UPDATED"][FUNC]      = "sysdate()";

                $query = Query::insertSQL($data, "ATTEND_SEMES_REMARK_HR_DAT");
                $db->query($query);
            }
        }

        $query = knjc010aQuery::getAttend($model, $schregNo, $periodCd);
        $attendAri = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $notDel = is_array($attendAri) && ($attendAri["DI_REMARK_CD"] != '' || $attendAri["DI_REMARK"] != '' || $attendAri["DI_CD"] != '');

        $data = array();
        if (!is_array($attendAri) && strlen($diCd) > 0) {
            $data["SCHREGNO"][TEXT]     = $schregNo;
            $data["ATTENDDATE"][DATE]   = $model->param["syoribi"];
            $data["PERIODCD"][TEXT]     = $periodCd;
            $data["CHAIRCD"][TEXT]      = $chairCd;
            $data["DI_CD"][TEXT]        = $diCd;
            $data["YEAR"][TEXT]         = CTRL_YEAR;
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $query = Query::insertSQL($data, "ATTEND_DAT");
        } elseif ($notDel || strlen($diCd) > 0) {
            $data["CHAIRCD"][TEXT]      = $chairCd;
            $data["DI_CD"][TEXT]        = $diCd;
            $data["YEAR"][TEXT]         = CTRL_YEAR;
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $where  = " WHERE ";
            $where .= "     SCHREGNO = '".$schregNo."' ";
            $where .= "     AND ATTENDDATE = '".$model->param["syoribi"]."' ";
            $where .= "     AND PERIODCD = '".$periodCd."' ";

            $query = Query::updateSQL($data, "ATTEND_DAT", $where);
        } else {
            $query  = " DELETE FROM ATTEND_DAT ";
            $query .= " WHERE ";
            $query .= "     SCHREGNO = '".$schregNo."' ";
            $query .= "     AND ATTENDDATE = '".$model->param["syoribi"]."' ";
            $query .= "     AND PERIODCD = '".$periodCd."' ";
        }

        return $query;
    }

    /* 時間割のない出欠データを削除 */
    public function getDelAttend($model)
    {
        $query  = " DELETE FROM ATTEND_DAT ATT ";
        $query .= " WHERE ";
        $query .= "     ATT.ATTENDDATE = '".str_replace("/", "-", $model->param["syoribi"])."' ";
        $query .= "     AND EXISTS ( ";
        if ($model->param["SEND_PRG"] == "KNJC020A" || $model->param["SEND_PRG"] == "") {
            $query .= "         SELECT ";
            $query .= "             'X' ";
            $query .= "         FROM ";
            $query .= "             CHAIR_STD_DAT STD ";
            $query .= "         WHERE ";
            $query .= "             STD.YEAR = '".CTRL_YEAR."' ";
            $query .= "             AND STD.SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "             AND STD.CHAIRCD = '".$model->param["chaircd"]."' ";
            $query .= "             AND STD.SCHREGNO = ATT.SCHREGNO ";
            $query .= "             AND '".str_replace("/", "-", $model->param["syoribi"])."' BETWEEN STD.APPDATE AND STD.APPENDDATE ";
        } elseif ($model->param["SEND_PRG"] == "KNJC030A") {
            $query .= "         SELECT ";
            $query .= "             'X' ";
            $query .= "         FROM ";
            $query .= "             SCHREG_REGD_DAT REGD ";
            $query .= "          WHERE ";
            $query .= "             REGD.SCHREGNO = ATT.SCHREGNO ";
            $query .= "             AND REGD.YEAR = '".CTRL_YEAR."' ";
            $query .= "             AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "             AND REGD.GRADE = '".$model->param["grade"]."' ";
            $query .= "             AND REGD.HR_CLASS = '".$model->param["hrclass"]."' ";
        }
        $query .= "     ) ";
        $query .= "     AND NOT EXISTS ( ";
        $query .= "         SELECT ";
        $query .= "           'X' ";
        $query .= "         FROM ";
        $query .= "           SCH_CHR_DAT SCH ";
        $query .= "           INNER JOIN CHAIR_STD_DAT STD ON STD.YEAR = SCH.YEAR ";
        $query .= "                AND STD.SEMESTER = SCH.SEMESTER ";
        $query .= "                AND STD.CHAIRCD = SCH.CHAIRCD ";
        $query .= "                AND SCH.EXECUTEDATE BETWEEN STD.APPDATE AND STD.APPENDDATE ";
        $query .= "         WHERE ";
        $query .= "           SCH.EXECUTEDATE = '".str_replace("/", "-", $model->param["syoribi"])."' ";
        $query .= "           AND SCH.PERIODCD = ATT.PERIODCD ";
        $query .= "           AND STD.SCHREGNO = ATT.SCHREGNO ";
        $query .= "     ) ";

        return $query;
    }

    /* SCH_CHR_HRATE_DAT削除 */
    public function getdelSchChrHrate($model, $periodCd, $chairCd)
    {
        $query  = " DELETE SCH_CHR_HRATE_DAT ";
        $query .= " WHERE ";
        $query .= "     EXECUTEDATE = '".str_replace("/", "-", $model->param["syoribi"])."' ";
        $query .= "     AND PERIODCD = '{$periodCd}' ";
        if ($model->param["SEND_PRG"] == "KNJC020A" || $model->param["SEND_PRG"] == "") {
            $query .= "     AND CHAIRCD = '{$chairCd}' ";
        }
        return $query;
    }
    /* SCH_CHR_HRATE_DAT作成 */
    public function getInsSchChrHrate($model, $periodCd, $chairCd)
    {
        $query  = " INSERT INTO SCH_CHR_HRATE_DAT ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.EXECUTEDATE, ";
        $query .= "     T1.PERIODCD, ";
        $query .= "     T1.CHAIRCD, ";
        $query .= "     T3.GRADE, ";
        $query .= "     T3.HR_CLASS, ";
        $query .= "     '0' AS EXECUTED, ";
        $query .= "     '".STAFFCD."' AS ATTESTOR, ";
        $query .= "     '".STAFFCD."' AS REGISTERCD, ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     SCH_CHR_DAT T1 ";
        $query .= "     INNER JOIN CHAIR_STD_DAT T2 ON T2.YEAR = '".CTRL_YEAR."' ";
        $query .= "           AND T2.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "           AND T2.CHAIRCD = T1.CHAIRCD ";
        $query .= "           AND T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT T3 ON T3.YEAR = T2.YEAR ";
        $query .= "           AND T3.SEMESTER = T2.SEMESTER ";
        $query .= "           AND T3.SCHREGNO = T2.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.EXECUTEDATE = '".str_replace("/", "-", $model->param["syoribi"])."' ";
        $query .= "     AND T1.PERIODCD = '{$periodCd}' ";
        if ($model->param["SEND_PRG"] == "KNJC020A" || $model->param["SEND_PRG"] == "") {
            $query .= "     AND T1.CHAIRCD = '{$chairCd}' ";
        }
        $query .= "     AND NOT EXISTS ( ";
        $query .= "             SELECT ";
        $query .= "                 'X' ";
        $query .= "             FROM ";
        $query .= "                 SCH_CHR_HRATE_DAT L1 ";
        $query .= "             WHERE ";
        $query .= "                 L1.EXECUTEDATE = T1.EXECUTEDATE ";
        $query .= "                 AND L1.PERIODCD = T1.PERIODCD ";
        $query .= "                 AND L1.CHAIRCD = T1.CHAIRCD ";
        $query .= "                 AND L1.GRADE = T3.GRADE ";
        $query .= "                 AND L1.HR_CLASS = T3.HR_CLASS ";
        $query .= "     ) ";
        return $query;
    }

    /* SCH_CHR_HRATE_DAT更新 */
    public function getUpdSchChrHrate($model, $periodCd, $chairCd)
    {
        $query  = " UPDATE SCH_CHR_HRATE_DAT ";
        $query .= " SET EXECUTED = '1', ";
        $query .= "     ATTESTOR = '".STAFFCD."', ";
        $query .= "     REGISTERCD = '".STAFFCD."', ";
        $query .= "     UPDATED = sysdate() ";
        $query .= " WHERE ";
        $query .= "   (EXECUTEDATE ";
        $query .= "   ,PERIODCD ";
        $query .= "   ,CHAIRCD ";
        $query .= "   ,GRADE ";
        $query .= "   ,HR_CLASS) IN ( ";
        $query .= "     SELECT ";
        $query .= "       T1.EXECUTEDATE, ";
        $query .= "       T1.PERIODCD, ";
        $query .= "       T1.CHAIRCD, ";
        $query .= "       T3.GRADE, ";
        $query .= "       T3.HR_CLASS ";
        $query .= "     FROM SCH_CHR_DAT T1 ";
        $query .= "     INNER JOIN CHAIR_STD_DAT T2 ON T2.YEAR = '".CTRL_YEAR."' ";
        $query .= "       AND T2.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "       AND T2.CHAIRCD = T1.CHAIRCD ";
        $query .= "       AND T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT T3 ON T3.YEAR = T2.YEAR ";
        $query .= "       AND T3.SEMESTER = T2.SEMESTER ";
        $query .= "       AND T3.SCHREGNO = T2.SCHREGNO ";
        if ($model->param["SEND_PRG"] == "KNJC030A") {
            $query .= "     AND T3.GRADE = '{$model->param["grade"]}' ";
            $query .= "     AND T3.HR_CLASS = '{$model->param["hrclass"]}' ";
        }
        $query .= "     WHERE ";
        $query .= "       T1.EXECUTEDATE = '".str_replace("/", "-", $model->param["syoribi"])."' ";
        $query .= "       AND T1.PERIODCD = '{$periodCd}' ";
        if ($model->param["SEND_PRG"] == "KNJC020A" || $model->param["SEND_PRG"] == "") {
            $query .= "       AND T1.CHAIRCD = '{$chairCd}' ";
        }
        $query .= "       ) ";
        return $query;
    }

    /* SCH_CHR_DAT更新 */
    public function getUpdSchChrExecuted($db, $model, $periodCd)
    {
        $query  = " WITH GHR_T AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     CHAIRCD, ";
        $query .= "     EXECUTEDATE, ";
        $query .= "     PERIODCD ";
        $query .= " FROM ";
        $query .= "     SCH_CHR_HRATE_DAT ";
        $query .= " WHERE ";
        $query .= "     EXECUTEDATE = '".str_replace("/", "-", $model->param["syoribi"])."' ";
        $query .= "     AND PERIODCD = '{$periodCd}' ";
        if ($model->param["SEND_PRG"] == "KNJC030A") {
            $query .= "     AND GRADE = '{$model->param["grade"]}' ";
            $query .= "     AND HR_CLASS = '{$model->param["hrclass"]}' ";
        }
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.CHAIRCD, ";
        $query .= "     MIN(T1.EXECUTED) AS EXECUTED ";
        $query .= " FROM ";
        $query .= "     SCH_CHR_HRATE_DAT T1 ";
        $query .= "     INNER JOIN GHR_T ON T1.CHAIRCD = GHR_T.CHAIRCD ";
        $query .= "           AND T1.EXECUTEDATE = GHR_T.EXECUTEDATE ";
        $query .= "           AND T1.PERIODCD = GHR_T.PERIODCD ";
        $query .= " GROUP BY ";
        $query .= "     T1.CHAIRCD ";

        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["EXECUTED"] == 1) {
                $query  = "   UPDATE SCH_CHR_DAT ";
                $query .= "   SET EXECUTED = '1' ";
                $query .= "     , REGISTERCD = '".STAFFCD."' ";
                $query .= "     , UPDATED = sysdate() ";
                $query .= "   WHERE ";
                $query .= "     EXECUTEDATE = '".str_replace("/", "-", $model->param["syoribi"])."' ";
                $query .= "     AND PERIODCD = '{$periodCd}' ";
                $query .= "     AND CHAIRCD = '{$row["CHAIRCD"]}' ";
                $db->query($query);
            }
        }
    }

    /* SCH_CHR_DAT更新 */
    public function getUpdSchChrExecutediv($db, $model, $periodCd, $chairCd)
    {
        $setExecutediv = $model->field["TITLE_EXECUTEDIV"] >= "2" ? $model->field["TITLE_EXECUTEDIV"] : "1";
        $query  = "   UPDATE SCH_CHR_DAT ";
        $query .= "   SET EXECUTEDIV = '{$setExecutediv}' ";
        $query .= "   WHERE ";
        $query .= "     EXECUTEDATE = '".str_replace("/", "-", $model->param["syoribi"])."' ";
        $query .= "     AND PERIODCD = '{$periodCd}' ";
        $query .= "     AND CHAIRCD = '{$chairCd}' ";

        return $query;
    }

    /* SCH_CHR_COUNTFLG 更新 */
    public function getUpdSchChrCountflg($db, $model, $periodCd, $chairCd)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T2.NAMESPARE1 AS COUNTFLG0_FLG, ";
        $query .= "     T3.COUNTFLG, ";
        $query .= "     T3.GRADE, ";
        $query .= "     T3.HR_CLASS ";
        $query .= " FROM ";
        $query .= "     SCH_CHR_DAT T1 ";
        $query .= "     INNER JOIN NAME_MST T2 ON T2.NAMECD1 = 'C009' AND T2.NAMECD2 = T1.EXECUTEDIV ";
        $query .= "     INNER JOIN SCH_CHR_COUNTFLG T3 ON T3.EXECUTEDATE = T1.EXECUTEDATE ";
        $query .= "         AND T3.PERIODCD = T1.PERIODCD ";
        $query .= "         AND T3.CHAIRCD = T1.CHAIRCD ";
        if ($model->param["SEND_PRG"] == "KNJC030A") {
            $query .= "     AND T3.GRADE = '{$model->param["grade"]}' ";
            $query .= "     AND T3.HR_CLASS = '{$model->param["hrclass"]}' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.EXECUTEDATE = '".str_replace("/", "-", $model->param["syoribi"])."' ";
        $query .= "     AND T1.PERIODCD = '{$periodCd}' ";
        $query .= "     AND T1.CHAIRCD = '{$chairCd}' ";

        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $setCountflg = "";
            if ("1" == $row["COUNTFLG0_FLG"] || "0" == $row["COUNTFLG"]) {
                // カウントしない
                $setCountflg = "0";
            } else {
                // カウントする
                $setCountflg = "1";
            }
            $query  = " UPDATE SCH_CHR_COUNTFLG ";
            $query .= " SET COUNTFLG = '{$setCountflg}', ";
            $query .= "     REGISTERCD = '".STAFFCD."', ";
            $query .= "     UPDATED = sysdate() ";
            $query .= " WHERE ";
            $query .= "   (EXECUTEDATE ";
            $query .= "   ,PERIODCD ";
            $query .= "   ,CHAIRCD ";
            $query .= "   ,GRADE ";
            $query .= "   ,HR_CLASS) IN ( ";
            $query .= "     SELECT ";
            $query .= "       T1.EXECUTEDATE, ";
            $query .= "       T1.PERIODCD, ";
            $query .= "       T1.CHAIRCD, ";
            $query .= "       T3.GRADE, ";
            $query .= "       T3.HR_CLASS ";
            $query .= "     FROM SCH_CHR_DAT T1 ";
            $query .= "     INNER JOIN CHAIR_STD_DAT T2 ON T2.YEAR = '".CTRL_YEAR."' ";
            $query .= "       AND T2.SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "       AND T2.CHAIRCD = T1.CHAIRCD ";
            $query .= "       AND T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE ";
            $query .= "     INNER JOIN SCHREG_REGD_DAT T3 ON T3.YEAR = T2.YEAR ";
            $query .= "       AND T3.SEMESTER = T2.SEMESTER ";
            $query .= "       AND T3.SCHREGNO = T2.SCHREGNO ";
            $query .= "       AND T3.GRADE = '{$row["GRADE"]}' ";
            $query .= "       AND T3.HR_CLASS = '{$row["HR_CLASS"]}' ";
            $query .= "     WHERE ";
            $query .= "       T1.EXECUTEDATE = '".str_replace("/", "-", $model->param["syoribi"])."' ";
            $query .= "       AND T1.PERIODCD = '{$periodCd}' ";
            $query .= "       AND T1.CHAIRCD = '{$chairCd}' ";
            $query .= "       ) ";

            $db->query($query);
        }
    }

    /* 詳細備考更新 */
    public function updSyouSai($db, $model, $updData)
    {
        $query = knjc010aQuery::getAttend($model, $updData["SCHREGNO"], $updData["PERI"]);
        $attendAri = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $query = knjc010aQuery::getAttend($model, $updData["SCHREGNO"], $updData["PERI"], "DELCHECK");
        $dellCheck = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $data = array();
        if (!is_array($attendAri) && ($updData["REMARKCD"] || $updData["REMARK"])) {
            $data["SCHREGNO"][TEXT]     = $updData["SCHREGNO"];
            $data["ATTENDDATE"][DATE]   = $model->param["syoribi"];
            $data["PERIODCD"][TEXT]     = $updData["PERI"];
            $data["YEAR"][TEXT]         = CTRL_YEAR;
            $data["DI_REMARK_CD"][TEXT] = $updData["REMARKCD"];
            $data["DI_REMARK"][TEXT]    = $updData["REMARK"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $query = Query::insertSQL($data, "ATTEND_DAT");
        } elseif (is_array($dellCheck)) {
            $data["YEAR"][TEXT]         = CTRL_YEAR;
            $data["DI_REMARK_CD"][TEXT] = $updData["REMARKCD"];
            $data["DI_REMARK"][TEXT]    = $updData["REMARK"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $where  = " WHERE ";
            $where .= "     SCHREGNO = '".$updData["SCHREGNO"]."' ";
            $where .= "     AND ATTENDDATE = '".$model->param["syoribi"]."' ";
            $where .= "     AND PERIODCD = '".$updData["PERI"]."' ";

            $query = Query::updateSQL($data, "ATTEND_DAT", $where);
        } else {
            $query  = " DELETE FROM ATTEND_DAT ";
            $query .= " WHERE ";
            $query .= "     SCHREGNO = '".$updData["SCHREGNO"]."' ";
            $query .= "     AND ATTENDDATE = '".$model->param["syoribi"]."' ";
            $query .= "     AND PERIODCD = '".$updData["PERI"]."' ";
        }
        return $query;
    }

    /* 授業内容更新 */
    public function updJugyouNaiyou($db, $model, $updRemark)
    {
        $query = knjc010aQuery::getSchChrRemarkDat($model);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $data = array();

        if ($row["EXECUTEDATE"] == "") {
            $data["EXECUTEDATE"][DATE] = $model->jugyouNaiyouSyoribi;
            $data["PERIODCD"][TEXT]    = $model->jugyouNaiyouPeriodcd;
            $data["CHAIRCD"][TEXT]     = $row["CHAIRCD"];
            $data["REMARK_DIV"][TEXT]  = "01";
            $data["REMARK"][TEXT]      = $updRemark;
            $data["REGISTERCD"][TEXT]  = STAFFCD;
            $data["UPDATED"][FUNC]     = "sysdate()";

            $query = Query::insertSQL($data, "SCH_CHR_REMARK_DAT");
        } else {
            $data["REMARK"][TEXT]      = $updRemark;
            $data["REGISTERCD"][TEXT]  = STAFFCD;
            $data["UPDATED"][FUNC]     = "sysdate()";

            $where  = " WHERE ";
            $where .= "     EXECUTEDATE = '".$model->jugyouNaiyouSyoribi."' ";
            $where .= "     AND PERIODCD = '".$model->jugyouNaiyouPeriodcd."' ";
            $where .= "     AND CHAIRCD = '".$row["CHAIRCD"]."' ";
            $where .= "     AND REMARK_DIV = '01' ";

            $query = Query::updateSQL($data, "SCH_CHR_REMARK_DAT", $where);
        }

        return $query;
    }

    // 指定日付の講座一覧取得
    public function getSchChrList($model, $executeDate)
    {
        $query = "";
        $query .= " WITH STF_CHR AS ( ";
        $query .= "   SELECT DISTINCT ";
        $query .= "     CHAIRCD ";
        $query .= "   FROM ";
        $query .= "     CHAIR_STF_DAT ";
        $query .= "   WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND STAFFCD = '".STAFFCD."' ";
        $query .= "   UNION ";
        $query .= "   SELECT DISTINCT ";
        $query .= "     CHAIRCD ";
        $query .= "   FROM ";
        $query .= "     SCH_STF_DAT ";
        $query .= "   WHERE ";
        $query .= "     EXECUTEDATE = '".$executeDate."' ";
        $query .= "     AND STAFFCD = '".STAFFCD."' ";
        $query .= " ) ";

        $query .= " , SCH_CHR_HRATE AS ( ";
        $query .= "   SELECT ";
        $query .= "     EXECUTEDATE ";
        $query .= "     , PERIODCD ";
        $query .= "     , CHAIRCD ";
        $query .= "     , COUNT(*) CNT ";
        $query .= "   FROM ";
        $query .= "     SCH_CHR_HRATE_DAT ";
        $query .= "   WHERE ";
        $query .= "     EXECUTEDATE = '".$executeDate."' ";
        $query .= "     AND EXECUTED = '1' ";
        $query .= "   GROUP BY ";
        $query .= "     EXECUTEDATE ";
        $query .= "     , PERIODCD ";
        $query .= "     , CHAIRCD ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.EXECUTEDATE ";
        $query .= "   , T1.PERIODCD ";
        $query .= "   , M1.NAME1 PERIODNAME ";
        $query .= "   , T1.CHAIRCD ";
        $query .= "   , T1.EXECUTED ";
        $query .= "   , CASE WHEN T1.EXECUTED = '1' THEN '3399ff' WHEN T3.CNT > 0 THEN 'ffff00' ELSE 'ff0099' END EXECUTED_BGCOLOR ";
        $query .= "   , CASE WHEN T1.EXECUTED = '1' THEN 'ffffff' WHEN T3.CNT > 0 THEN '000000' ELSE 'ffffff' END EXECUTED_COLOR ";
        $query .= "   , CASE WHEN T1.EXECUTED = '1' THEN '出欠済' WHEN T3.CNT > 0 THEN '出欠中' ELSE '出欠未' END EXECUTED_NAME ";
        $query .= "   , T1.CHAIRCD || ':' || T2.CHAIRNAME CHAIRNAME ";
        $query .= "   , T2.CHAIRNAME CHAIRNAME1 ";
        $query .= "   , T2.CHAIRABBV ";
        $query .= "   , T2.CLASSCD ";
        $query .= "   , T2.SCHOOL_KIND ";
        $query .= "   , T2.CURRICULUM_CD ";
        $query .= "   , T2.SUBCLASSCD ";
        $query .= "   , T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD || ':' || M2.SUBCLASSABBV SUBCLASSNAME ";
        $query .= "   , M2.SUBCLASSABBV ";
        $query .= " FROM ";
        $query .= "   SCH_CHR_DAT T1 ";
        $query .= "   INNER JOIN CHAIR_DAT T2 ";
        $query .= "     ON T2.YEAR = T1.YEAR ";
        $query .= "     AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "     AND T2.CHAIRCD = T1.CHAIRCD ";
        $query .= "   LEFT JOIN SCH_CHR_HRATE T3 ";
        $query .= "     ON T3.EXECUTEDATE = T1.EXECUTEDATE ";
        $query .= "     AND T3.PERIODCD = T1.PERIODCD ";
        $query .= "     AND T3.CHAIRCD = T1.CHAIRCD ";

        $query .= "   INNER JOIN V_NAME_MST M1 ";
        $query .= "     ON M1.YEAR = T1.YEAR ";
        $query .= "     AND M1.NAMECD1 = 'B001' ";
        $query .= "     AND M1.NAMECD2 = T1.PERIODCD ";
        $query .= "   INNER JOIN SUBCLASS_MST M2 ";
        $query .= "     ON M2.CLASSCD = T2.CLASSCD ";
        $query .= "     AND M2.SCHOOL_KIND = T2.SCHOOL_KIND ";
        $query .= "     AND M2.CURRICULUM_CD = T2.CURRICULUM_CD ";
        $query .= "     AND M2.SUBCLASSCD = T2.SUBCLASSCD ";

        $query .= " WHERE T1.EXECUTEDATE = '".$executeDate."' ";
        //制限ユーザー
        if ($model->param["SEND_AUTH"]) {
            if ($model->param["SEND_AUTH"] != DEF_UPDATABLE) {
                $query .= "   AND T1.CHAIRCD IN (SELECT CHAIRCD FROM STF_CHR) ";
            }
        } elseif (AUTHORITY != DEF_UPDATABLE) {
            $query .= "   AND T1.CHAIRCD IN (SELECT CHAIRCD FROM STF_CHR) ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.PERIODCD ";
        $query .= "   , T1.CHAIRCD ";

        return $query;
    }

    // 指定日付の講座一覧取得
    public function getChrHrClassList($model, $chairCd)
    {
        $query = "";

        $query .= " WITH CHR_CLS AS ( ";

        $query .= " SELECT ";
        $query .= "   T1.YEAR ";
        $query .= "   , T1.SEMESTER ";
        $query .= "   , T2.CHAIRCD ";
        $query .= "   , T1.GROUPCD ";
        $query .= "   , T1.TRGTGRADE GRADE ";
        $query .= "   , T1.TRGTCLASS HR_CLASS ";
        $query .= " FROM ";
        $query .= "   CHAIR_CLS_DAT T1 ";
        $query .= "   INNER JOIN CHAIR_DAT T2 ";
        $query .= "     ON T2.YEAR = T1.YEAR ";
        $query .= "     AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "     AND T2.GROUPCD = T1.GROUPCD ";
        $query .= " WHERE ";
        $query .= "       T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "   AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "   AND T1.CHAIRCD = '0000000' ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "   T1.YEAR ";
        $query .= "   , T1.SEMESTER ";
        $query .= "   , T1.CHAIRCD ";
        $query .= "   , T1.GROUPCD ";
        $query .= "   , T1.TRGTGRADE GRADE ";
        $query .= "   , T1.TRGTCLASS HR_CLASS ";
        $query .= " FROM ";
        $query .= "   CHAIR_CLS_DAT T1 ";
        $query .= "   INNER JOIN CHAIR_DAT T2 ";
        $query .= "     ON T2.YEAR = T1.YEAR ";
        $query .= "     AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "     AND T2.CHAIRCD = T1.CHAIRCD ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "   AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "   AND T1.GROUPCD = '0000' ";

        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "   T1.* ";
        $query .= "   , REGDH.HR_NAME ";
        $query .= " FROM ";
        $query .= "   CHR_CLS T1 ";
        $query .= "   INNER JOIN SCHREG_REGD_HDAT REGDH ";
        $query .= "     ON REGDH.YEAR = T1.YEAR ";
        $query .= "     AND REGDH.SEMESTER = T1.SEMESTER ";
        $query .= "     AND REGDH.GRADE = T1.GRADE ";
        $query .= "     AND REGDH.HR_CLASS = T1.HR_CLASS ";

        $query .= " WHERE T1.CHAIRCD = '{$chairCd}' ";

        $query .= " ORDER BY ";
        $query .= "   T1.CHAIRCD ";
        $query .= "   , T1.GRADE ";
        $query .= "   , T1.HR_CLASS ";

        return $query;
    }

    /** 入力講座情報 */
    public function getInputCharInfo($schregNO, $date, $periodcd)
    {
        $query  = " SELECT ";
        $query .= "   CHR.CHAIRNAME ";
        $query .= " , STF.STAFFNAME ";
        $query .= " , CRED.CREDITS ";
        $query .= " , USTF.STAFFNAME AS UPDATE_STAFFNAME ";
        $query .= " , REPLACE(CAST(ATT.UPDATED AS DATE), '-', '/') || ' ' || REPLACE(CAST(ATT.UPDATED AS TIME), '.', ':') AS UPDATE_TIME ";
        $query .= " FROM SCH_CHR_DAT T1 ";
        $query .= " INNER JOIN CHAIR_STD_DAT STD ON STD.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND STD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND STD.CHAIRCD = T1.CHAIRCD ";
        $query .= "     AND STD.SCHREGNO = '".$schregNO."' ";
        $query .= "     AND T1.EXECUTEDATE BETWEEN STD.APPDATE AND STD.APPENDDATE ";
        $query .= " INNER JOIN CHAIR_DAT CHR ON CHR.YEAR = STD.YEAR ";
        $query .= "     AND CHR.SEMESTER = STD.SEMESTER ";
        $query .= "     AND CHR.CHAIRCD = STD.CHAIRCD ";
        $query .= " INNER JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = STD.SCHREGNO ";
        $query .= "     AND REGD.YEAR = STD.YEAR ";
        $query .= "     AND REGD.SEMESTER = STD.SEMESTER ";
        $query .= " LEFT JOIN CREDIT_MST CRED ON CRED.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND CRED.COURSECD = REGD.COURSECD ";
        $query .= "     AND CRED.GRADE = REGD.GRADE ";
        $query .= "     AND CRED.MAJORCD = REGD.MAJORCD ";
        $query .= "     AND CRED.COURSECODE = REGD.COURSECODE ";
        $query .= "     AND CRED.CLASSCD = CHR.CLASSCD ";
        $query .= "     AND CRED.SCHOOL_KIND = CHR.SCHOOL_KIND ";
        $query .= "     AND CRED.CURRICULUM_CD = CHR.CURRICULUM_CD ";
        $query .= "     AND CRED.SUBCLASSCD = CHR.SUBCLASSCD ";
        $query .= " LEFT JOIN CHAIR_STF_DAT CSTF ON CSTF.YEAR = CHR.YEAR ";
        $query .= "     AND CSTF.SEMESTER = CHR.SEMESTER ";
        $query .= "     AND CSTF.CHAIRCD = CHR.CHAIRCD ";
        $query .= " LEFT JOIN STAFF_MST STF ON STF.STAFFCD = CSTF.STAFFCD ";
        $query .= " LEFT JOIN ATTEND_DAT ATT ON ATT.SCHREGNO = STD.SCHREGNO ";
        $query .= "     AND ATT.ATTENDDATE = T1.EXECUTEDATE ";
        $query .= "     AND ATT.PERIODCD = T1.PERIODCD ";
        $query .= " LEFT JOIN STAFF_MST USTF ON USTF.STAFFCD = ATT.REGISTERCD ";
        $query .= " WHERE ";
        $query .= "     T1.EXECUTEDATE = '".$date."' ";
        $query .= "     AND T1.PERIODCD = '".$periodcd."' ";
        $query .= " ORDER BY ";
        $query .= "     CHR.CHAIRCD ";
        $query .= "   , CSTF.CHARGEDIV DESC ";
        $query .= "   , CSTF.STAFFCD ";
        return $query;
    }

    /** ポップアップの表示用データ */
    public function getPopupInfo1($year, $semester, $syoribi, $chaircd, $period)
    {
        $query .= " select distinct ";
        $query .= "     t1.CHAIRCD, ";
        $query .= "     t4.CHAIRNAME, ";
        $query .= "     t6.CREDITS, ";
        $query .= "     t3.ATTESTOR, ";
        $query .= "     t7.STAFFNAME_SHOW as ATTESTOR_NAME ";
        $query .= " from ";
        $query .= "     CHAIR_STD_DAT t1 ";
        $query .= "     inner join ";
        $query .= "         (select ";
        $query .= "             SCHREGNO ";
        $query .= "         from  ";
        $query .= "             CHAIR_STD_DAT ";
        $query .= "         where ";
        $query .= "             YEAR = '{$year}' ";
        $query .= "             AND SEMESTER    = '{$semester}' ";
        $query .= "             AND CHAIRCD     = '{$chaircd}' ";
        $query .= "             AND '{$syoribi}' BETWEEN APPDATE AND APPENDDATE) t2 ";
        $query .= "         on  t1.SCHREGNO  = t2.SCHREGNO ";
        $query .= "     inner join ";
        $query .= "         SCH_CHR_DAT t3 ";
        $query .= "         on  t3.CHAIRCD      = t1.CHAIRCD ";
        $query .= "         and t3.EXECUTEDATE  = '{$syoribi}' ";
        $query .= "         and t3.PERIODCD     = '{$period}' ";
        $query .= "     inner join ";
        $query .= "         CHAIR_DAT  t4 ";
        $query .= "         on  t1.CHAIRCD      = t4.CHAIRCD ";
        $query .= "         and t1.YEAR         = t4.YEAR ";
        $query .= "         and t1.SEMESTER     = t4.SEMESTER ";
        $query .= "     inner join ";
        $query .= "         SCHREG_REGD_DAT t5 ";
        $query .= "         on  t1.SCHREGNO     = t5.SCHREGNO ";
        $query .= "         and t1.YEAR         = t5.YEAR ";
        $query .= "         and t1.SEMESTER     = t5.SEMESTER ";
        $query .= "     left join ";
        $query .= "         CREDIT_MST t6 ";
        $query .= "         on  t6.YEAR         = t5.YEAR ";
        $query .= "         and t6.GRADE        = t5.GRADE ";
        $query .= "         and t6.COURSECD     = t5.COURSECD ";
        $query .= "         and t6.MAJORCD      = t5.MAJORCD ";
        $query .= "         and t6.COURSECODE   = t5.COURSECODE ";
        $query .= "         and t6.CLASSCD      = t4.CLASSCD ";
        $query .= "         and t6.SCHOOL_KIND  = t4.SCHOOL_KIND ";
        $query .= "         and t6.CURRICULUM_CD = t4.CURRICULUM_CD ";
        $query .= "         and t6.SUBCLASSCD   = t4.SUBCLASSCD ";
        $query .= "     left join ";
        $query .= "         V_STAFF_MST t7 ";
        $query .= "         on  t3.ATTESTOR     = t7.STAFFCD ";
        $query .= "         and t7.YEAR         = t1.YEAR ";
        $query .= " where ";
        $query .= "     t1.YEAR = '{$year}' ";
        $query .= "     AND t1.SEMESTER = '{$semester}' ";
        $query .= "     AND t3.EXECUTEDATE BETWEEN t1.APPDATE AND t1.APPENDDATE ";

        return $query;
    }

    /** ポップアップの表示用データ */
    public function getPopupInfo2($year, $semester, $syoribi, $chaircd, $period)
    {
        $query .= " WITH T_CHAIRCD AS ( ";
        $query .= "     select distinct ";
        $query .= "         t1.CHAIRCD ";
        $query .= "     from ";
        $query .= "         CHAIR_STD_DAT t1 ";
        $query .= "         inner join ";
        $query .= "             (select ";
        $query .= "                 SCHREGNO ";
        $query .= "             from  ";
        $query .= "                 CHAIR_STD_DAT ";
        $query .= "             where ";
        $query .= "                 YEAR = '{$year}' ";
        $query .= "                 AND SEMESTER = '{$semester}' ";
        $query .= "                 AND CHAIRCD = '{$chaircd}' ";
        $query .= "                 AND '{$syoribi}' BETWEEN APPDATE AND APPENDDATE) t2 ";
        $query .= "             on  t1.SCHREGNO = t2.SCHREGNO ";
        $query .= "         inner join ";
        $query .= "             SCH_CHR_DAT t3 ";
        $query .= "             on  t3.CHAIRCD      = t1.CHAIRCD ";
        $query .= "             and t3.EXECUTEDATE  = '{$syoribi}' ";
        $query .= "             and t3.PERIODCD     = '{$period}' ";
        $query .= "         inner join ";
        $query .= "             CHAIR_DAT t4 ";
        $query .= "             on  t1.CHAIRCD  = t4.CHAIRCD ";
        $query .= "             and t1.YEAR     = t4.YEAR ";
        $query .= "             and t1.SEMESTER = t4.SEMESTER ";
        $query .= "     where ";
        $query .= "         t1.YEAR = '{$year}' ";
        $query .= "         AND t1.SEMESTER = '{$semester}' ";
        $query .= "         AND t3.EXECUTEDATE BETWEEN t1.APPDATE AND t1.APPENDDATE ";
        $query .= " ) ";
        $query .= "  ";
        $query .= " select ";
        $query .= "     t5.CHAIRCD, ";
        $query .= "     t6.STAFFCD, ";
        $query .= "     t7.STAFFNAME_SHOW ";
        $query .= " from ";
        $query .= "     T_CHAIRCD t5 ";
        $query .= "     inner join ";
        $query .= "         CHAIR_STF_DAT t6 ";
        $query .= "         on  t5.CHAIRCD  = t6.CHAIRCD ";
        $query .= "         and t6.YEAR     = '{$year}' ";
        $query .= "         and t6.SEMESTER = '{$semester}' ";
        $query .= "     inner join ";
        $query .= "         V_STAFF_MST t7 ";
        $query .= "         on  t6.STAFFCD  = t7.STAFFCD ";
        $query .= "         and t6.YEAR     = t7.YEAR ";
        $query .= " union ";
        $query .= " select  ";
        $query .= "     t5.CHAIRCD, ";
        $query .= "     t6.STAFFCD, ";
        $query .= "     t7.STAFFNAME_SHOW ";
        $query .= " from ";
        $query .= "     T_CHAIRCD t5 ";
        $query .= "     inner join ";
        $query .= "         SCH_STF_DAT t6 ";
        $query .= "         on  t5.CHAIRCD      = t6.CHAIRCD ";
        $query .= "         and t6.EXECUTEDATE  = '{$syoribi}' ";
        $query .= "         and t6.PERIODCD     = '{$period}' ";
        $query .= "     inner join ";
        $query .= "         V_STAFF_MST t7 ";
        $query .= "         on  t6.STAFFCD  = t7.STAFFCD ";
        $query .= "         and t7.YEAR     = '{$year}' ";

        return $query;
    }
}
