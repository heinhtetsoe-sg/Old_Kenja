<?php

require_once('for_php7.php');

class knjp704Query extends Query {

    //前年度からコピー時のデータ存在チェック
    function selectYearQuery($year, $table)
    {
        $db = Query::dbCheckOut();

        $query = "SELECT COUNT(*) FROM $table WHERE YEAR = '$year'";
        $row = $db->getOne($query);

        Query::dbCheckIn($db);
        return $row;
    }

    //コピー時のマスタチェック(前年度に登録されているクラスが今年度に存在するか)
    function mstClassCheck($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.GRADE || T1.HR_CLASS AS HR_CLASS, ";
        $query .= "     T2.GRADE || T2.HR_CLASS AS MST_HR_CLASS ";
        $query .= " FROM ";
        $query .= "     (SELECT GRADE,HR_CLASS ";
        $query .= "        FROM COLLECT_GRP_HR_DAT ";
        $query .= "       WHERE YEAR = '".($model->leftSetYear - 1)."') T1 ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     (SELECT GRADE,HR_CLASS ";
        $query .= "        FROM SCHREG_REGD_HDAT ";
        $query .= "       WHERE YEAR = '".$model->leftSetYear."' AND ";
        $query .= "             SEMESTER = '".CTRL_SEMESTER."' ) T2 ";
        $query .= " ON ";
        $query .= "     T1.GRADE = T2.GRADE AND ";
        $query .= "     T1.HR_CLASS = T2.HR_CLASS ";

        return $query;
    }

    //コピー時のマスタチェック(前年度に登録されている項目が今年度に存在するか)
    function mstMcdCheck($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.COLLECT_LM_CD, ";
        $query .= "     T2.COLLECT_LM_CD AS MST_EXP_MCD ";
        $query .= " FROM ";
        $query .= "     (SELECT DISTINCT COLLECT_L_CD || COLLECT_M_CD AS COLLECT_LM_CD ";
        $query .= "        FROM V_COLLECT_GRP_DAT ";
        $query .= "       WHERE YEAR = '".($model->leftSetYear - 1)."' ";
        $query .= "             AND COLLECT_KOJIN_FLG != '1') T1 ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     (SELECT COLLECT_L_CD || COLLECT_M_CD AS COLLECT_LM_CD ";
        $query .= "        FROM COLLECT_M_MST ";
        $query .= "       WHERE YEAR = '".$model->leftSetYear."') T2 ";
        $query .= " ON ";
        $query .= "         T1.COLLECT_LM_CD = T2.COLLECT_LM_CD ";

        return $query;
    }

    //コピー時のマスタチェック(前年度に登録されている細目が今年度に存在するか)
    function mstScdCheck($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.COLLECT_LMS_CD, ";
        $query .= "     T2.COLLECT_LMS_CD AS MST_EXP_SCD ";
        $query .= " FROM ";
        $query .= "     (SELECT DISTINCT COLLECT_L_CD || COLLECT_M_CD || COLLECT_S_CD AS COLLECT_LMS_CD ";
        $query .= "        FROM V_COLLECT_GRP_DAT ";
        $query .= "       WHERE YEAR = '".($model->leftSetYear - 1)."' ";
        $query .= "             AND COLLECT_KOJIN_FLG != '1') T1 ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     (SELECT COLLECT_L_CD || COLLECT_M_CD || COLLECT_S_CD AS COLLECT_LMS_CD ";
        $query .= "        FROM COLLECT_S_MST ";
        $query .= "       WHERE YEAR = '".$model->leftSetYear."') T2 ";
        $query .= " ON ";
        $query .= "         T1.COLLECT_LMS_CD = T2.COLLECT_LMS_CD ";

        return $query;
    }

    //学年取得
    function get_Grade()
    {
        $query  = " SELECT ";
        $query .= "     GRADE_NAME1 AS LABEL, ";
        $query .= "     GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //会計グループリスト
    function SelectQuery($model)
    {
        $query .= " SELECT ";
        $query .= "     GRP_M.COLLECT_GRP_CD, ";
        $query .= "     GRP_M.COLLECT_GRP_NAME, ";
        $query .= "     GRP_D.COLLECT_L_CD, ";
        $query .= "     GRP_D.COLLECT_M_CD, ";
        $query .= "     COL_M.COLLECT_M_NAME, ";
        $query .= "     CASE WHEN GRP_D.COLLECT_S_CD = '00' ";
        $query .= "          THEN NULL ";
        $query .= "          ELSE GRP_D.COLLECT_S_CD ";
        $query .= "     END AS COLLECT_S_CD, ";
        $query .= "     COL_S.COLLECT_S_NAME, ";
        $query .= "     GRP_HR.GRADE, ";
        $query .= "     GRP_HR.HR_CLASS, ";
        $query .= "     CASE WHEN GRP_HR.GRADE = '00' ";
        $query .= "          THEN '新入生' ";
        $query .= "          ELSE REGD_H.HR_NAME ";
        $query .= "     END AS HR_NAME ";
        $query .= " FROM ";
        $query .= "     COLLECT_GRP_MST GRP_M ";
        $query .= "     LEFT JOIN V_COLLECT_GRP_DAT GRP_D ON GRP_D.YEAR = GRP_M.YEAR ";
        $query .= "          AND GRP_D.COLLECT_GRP_CD = GRP_M.COLLECT_GRP_CD ";
        $query .= "     LEFT JOIN COLLECT_M_MST COL_M ON GRP_M.YEAR = COL_M.YEAR ";
        $query .= "          AND GRP_D.COLLECT_L_CD = COL_M.COLLECT_L_CD ";
        $query .= "          AND GRP_D.COLLECT_M_CD = COL_M.COLLECT_M_CD ";
        $query .= "     LEFT JOIN COLLECT_S_MST COL_S ON GRP_M.YEAR = COL_S.YEAR ";
        $query .= "          AND GRP_D.COLLECT_L_CD = COL_S.COLLECT_L_CD ";
        $query .= "          AND GRP_D.COLLECT_M_CD = COL_S.COLLECT_M_CD ";
        $query .= "          AND GRP_D.COLLECT_S_CD = COL_S.COLLECT_S_CD ";
        $query .= "     LEFT JOIN COLLECT_GRP_HR_DAT GRP_HR ON GRP_M.YEAR = GRP_HR.YEAR ";
        $query .= "          AND GRP_M.COLLECT_GRP_CD = GRP_HR.COLLECT_GRP_CD ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT REGD_H ON GRP_M.YEAR = REGD_H.YEAR ";
        $query .= "          AND GRP_HR.GRADE = REGD_H.GRADE ";
        $query .= "          AND GRP_HR.HR_CLASS = REGD_H.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "     GRP_M.YEAR = '".$model->leftSetYear."' ";
        $query .= "     AND VALUE(GRP_D.COLLECT_KOJIN_FLG, '0') != '1' ";
        if ($model->grade != "99") {
            $query .= "     AND GRP_HR.GRADE = '{$model->grade}' ";
        } else {
            $query .= "     AND GRP_HR.GRADE != '00' ";
            $query .= " UNION ";
            $query .= " SELECT ";
            $query .= "     GRP_M.COLLECT_GRP_CD, ";
            $query .= "     GRP_M.COLLECT_GRP_NAME, ";
            $query .= "     GRP_D.COLLECT_L_CD, ";
            $query .= "     GRP_D.COLLECT_M_CD, ";
            $query .= "     COL_M.COLLECT_M_NAME, ";
            $query .= "     CASE WHEN GRP_D.COLLECT_S_CD = '00' ";
            $query .= "          THEN NULL ";
            $query .= "          ELSE GRP_D.COLLECT_S_CD ";
            $query .= "     END AS COLLECT_S_CD, ";
            $query .= "     COL_S.COLLECT_S_NAME, ";
            $query .= "     GRP_HR.GRADE, ";
            $query .= "     GRP_HR.HR_CLASS, ";
            $query .= "     CASE WHEN GRP_HR.GRADE = '00' ";
            $query .= "          THEN '新入生' ";
            $query .= "          ELSE REGD_H.HR_NAME ";
            $query .= "     END AS HR_NAME ";
            $query .= " FROM ";
            $query .= "     COLLECT_GRP_MST GRP_M ";
            $query .= "     LEFT JOIN V_COLLECT_GRP_DAT GRP_D ON GRP_D.YEAR = GRP_M.YEAR ";
            $query .= "          AND GRP_D.COLLECT_GRP_CD = GRP_M.COLLECT_GRP_CD ";
            $query .= "     LEFT JOIN COLLECT_M_MST COL_M ON GRP_M.YEAR = COL_M.YEAR ";
            $query .= "          AND GRP_D.COLLECT_L_CD = COL_M.COLLECT_L_CD ";
            $query .= "          AND GRP_D.COLLECT_M_CD = COL_M.COLLECT_M_CD ";
            $query .= "     LEFT JOIN COLLECT_S_MST COL_S ON GRP_M.YEAR = COL_S.YEAR ";
            $query .= "          AND GRP_D.COLLECT_L_CD = COL_S.COLLECT_L_CD ";
            $query .= "          AND GRP_D.COLLECT_M_CD = COL_S.COLLECT_M_CD ";
            $query .= "          AND GRP_D.COLLECT_S_CD = COL_S.COLLECT_S_CD ";
            $query .= "     LEFT JOIN COLLECT_GRP_HR_DAT GRP_HR ON GRP_M.YEAR = GRP_HR.YEAR ";
            $query .= "          AND GRP_M.COLLECT_GRP_CD = GRP_HR.COLLECT_GRP_CD ";
            $query .= "     LEFT JOIN SCHREG_REGD_HDAT REGD_H ON GRP_M.YEAR = REGD_H.YEAR ";
            $query .= "          AND GRP_HR.GRADE = REGD_H.GRADE ";
            $query .= "          AND GRP_HR.HR_CLASS = REGD_H.HR_CLASS ";
            $query .= " WHERE ";
            $query .= "     GRP_M.YEAR = '".($model->leftSetYear + 1)."' ";
            $query .= "     AND VALUE(GRP_D.COLLECT_KOJIN_FLG, '0') != '1' ";
            $query .= "     AND GRP_HR.GRADE = '00' ";
        }
        $query .= " ORDER BY ";
        $query .= "     GRADE, ";
        $query .= "     COLLECT_GRP_CD, ";
        $query .= "     HR_CLASS, ";
        $query .= "     COLLECT_L_CD, ";
        $query .= "     COLLECT_M_CD, ";
        $query .= "     COLLECT_S_CD ";
        return $query;
    }

    //１レコード取得
    function getRow($model, $flg)
    {
        $query  = "SELECT * FROM COLLECT_GRP_MST ";
        $query .= " WHERE YEAR = '".$model->taisyouYear."'";
        if ($flg == 1) {
            $query .= "   AND COLLECT_GRP_CD = '".$model->exp_grpcd."'";
        } else {
            $query .= "   AND COLLECT_GRP_CD = '".sprintf("%04d", $model->field["COLLECT_GRP_CD"])."'";
        }

        return $query;
    }

    //グループデータの存在チェック
    function getGrpData($grpcd)
    {
        $query = "SELECT COUNT(*) FROM COLLECT_GRP_MST WHERE YEAR = '".CTRL_YEAR."' AND COLLECT_GRP_CD = '$grpcd' ";
        return $query;
    }

    //入金済みデータの存在チェック
    function getPaidMoney($year, $table, $grpcd)
    {
        $query = "SELECT COUNT(*) FROM $table WHERE YEAR = '$year' AND COLLECT_GRP_CD = '$grpcd' ";
        return $query;
    }

    //割当クラス一覧(左リスト:リロード時)
    function ReloadSelectClass($model)
    {
        if ($model->grade2 != "00") {
            $query  = " SELECT ";
            $query .= "     GRADE, ";
            $query .= "     HR_CLASS, ";
            $query .= "     HR_NAME ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT ";
            $query .= " WHERE ";
            $query .= "     YEAR            = '".$model->taisyouYear."' ";
            $query .= "     AND SEMESTER    = '".CTRL_SEMESTER."' ";
            $query .= "     AND GRADE       = '".$model->grade2."' ";
            $query .= "     AND HR_CLASS IN ('".str_replace(",","','",$model->field["CLASS"])."') ";
        } else {
            $query  = " WITH FRESHMAN (GRADE, HR_CLASS, HR_NAME) AS ( ";
            $query .= "     VALUES('00', '000', '新入生') ";
            $query .= " ) ";
            $query .= " SELECT ";
            $query .= "     * ";
            $query .= " FROM ";
            $query .= "     FRESHMAN ";
            $query .= " WHERE ";
            $query .= "     HR_CLASS IN ('".str_replace(",","','",$model->field["CLASS"])."') ";
        }
        return $query;
    }

    //割当クラス一覧(左リスト)
    function GetSelectClass($model)
    {
        if ($model->grade2 != "00") {
            $query  = " SELECT ";
            $query .= "     T2.GRADE, ";
            $query .= "     T2.HR_CLASS, ";
            $query .= "     T2.HR_NAME ";
            $query .= " FROM ";
            $query .= "     COLLECT_GRP_HR_DAT T1, ";
            $query .= "     SCHREG_REGD_HDAT T2 ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR            = T2.YEAR ";
            $query .= "     AND T1.GRADE       = T2.GRADE ";
            $query .= "     AND T1.HR_CLASS    = T2.HR_CLASS ";
            $query .= "     AND T2.YEAR        = '".$model->taisyouYear."' ";
            $query .= "     AND T2.SEMESTER    = '".CTRL_SEMESTER."' ";
            $query .= "     AND T2.GRADE       = '".$model->grade2."' ";
            $query .= "     AND T1.COLLECT_GRP_CD = '".$model->exp_grpcd."' ";
        } else {
            $query  = " WITH FRESHMAN (GRADE, HR_CLASS, HR_NAME) AS ( ";
            $query .= "     VALUES('00', '000', '新入生') ";
            $query .= " ) ";
            $query .= " SELECT ";
            $query .= "     * ";
            $query .= " FROM ";
            $query .= "     COLLECT_GRP_HR_DAT T1, ";
            $query .= "     FRESHMAN T2 ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR        = '".$model->taisyouYear."' ";
            $query .= "     AND T1.COLLECT_GRP_CD = '".$model->exp_grpcd."' ";
            $query .= "     AND T1.GRADE       = '".$model->grade2."' ";
            $query .= "     AND T1.GRADE       = T2.GRADE ";
            $query .= "     AND T1.HR_CLASS    = T2.HR_CLASS ";
        }
        return $query;
    }

    //クラス一覧(右リスト)
    function GetClass($model, $classcd)
    {
        if ($model->grade2 != "00") {
            $query  = " SELECT ";
            $query .= "     GRADE, ";
            $query .= "     HR_CLASS, ";
            $query .= "     HR_NAME ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT ";
            $query .= " WHERE ";
            $query .= "     YEAR     = '".CTRL_YEAR."' ";
            $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "     AND GRADE = '".$model->grade2."' ";
            $query .= "     AND HR_CLASS NOT IN ('".implode("','", $classcd)."') ";
            $query .= "     AND HR_CLASS NOT IN (SELECT HR_CLASS ";
            $query .= "                         FROM COLLECT_GRP_HR_DAT ";
            $query .= "                        WHERE YEAR = '".CTRL_YEAR."' ";
            $query .= "                          AND GRADE = '".$model->grade2."' ";
            $query .= "                          AND COLLECT_GRP_CD = '".$model->exp_grpcd."' ";
            $query .= "                      ) ";
        } else {
            $query  = " WITH FRESHMAN (GRADE, HR_CLASS, HR_NAME) AS ( ";
            $query .= "     VALUES('00', '000', '新入生') ";
            $query .= " ) ";
            $query .= " SELECT ";
            $query .= "     * ";
            $query .= " FROM ";
            $query .= "     FRESHMAN ";
            $query .= " WHERE ";
            $query .= "     HR_CLASS NOT IN ('".implode("','", $classcd)."') ";
        }
        return $query;
    }

    //会計項目割当一覧(左リスト:リロード時)
    function ReloadSelectMcd($model)
    {
        $query  = " SELECT ";
        $query .= "   COLLECT_L_CD, ";
        $query .= "   COLLECT_M_CD, ";
        $query .= "   COLLECT_M_NAME ";
        $query .= " FROM ";
        $query .= "   COLLECT_M_MST ";
        $query .= " WHERE ";
        $query .= "   YEAR = '".$model->taisyouYear."' AND ";
        $query .= "   COLLECT_L_CD || COLLECT_M_CD IN ('".str_replace(",","','",$model->field["COLLECT_M_CD"])."') ";
        $query .= " ORDER BY ";
        $query .= "   COLLECT_L_CD, ";
        $query .= "   COLLECT_M_CD ";
        return $query;
    }

    //会計項目割当一覧(左リスト)
    function GetSelectMcd($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "   T2.COLLECT_L_CD, ";
        $query .= "   T2.COLLECT_M_CD, ";
        $query .= "   T2.COLLECT_M_NAME ";
        $query .= " FROM ";
        $query .= "   V_COLLECT_GRP_DAT T1, ";
        $query .= "   COLLECT_M_MST T2 ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '".$model->taisyouYear."' AND ";
        $query .= "   T1.COLLECT_GRP_CD = '".$model->exp_grpcd."' AND ";
        $query .= "   T1.COLLECT_KOJIN_FLG != '1' AND ";
        $query .= "   T1.YEAR = T2.YEAR AND ";
        $query .= "   T1.COLLECT_L_CD = T2.COLLECT_L_CD AND ";
        $query .= "   T1.COLLECT_M_CD = T2.COLLECT_M_CD ";
        $query .= " ORDER BY ";
        $query .= "   COLLECT_L_CD, ";
        $query .= "   T2.COLLECT_M_CD ";
        return $query;
    }

    //会計項目一覧(右リスト)
    function GetMcd($model, $exp_mcd)
    {
        $query  = " SELECT ";
        $query .= "   COLLECT_L_CD, ";
        $query .= "   COLLECT_M_CD, ";
        $query .= "   COLLECT_M_NAME ";
        $query .= " FROM ";
        $query .= "   COLLECT_M_MST ";
        $query .= " WHERE ";
        $query .= "   YEAR = '".$model->taisyouYear."' AND ";
        $query .= "   COLLECT_L_CD || COLLECT_M_CD NOT IN ('".implode("','",$exp_mcd)."') ";
        $query .= " ORDER BY ";
        $query .= "   COLLECT_L_CD, ";
        $query .= "   COLLECT_M_CD ";
        return $query;
    }

    //会計細目割当一覧(左リスト:リロード時)
    function ReloadSelectScd($model)
    {
        $query  = " SELECT ";
        $query .= "   T1.COLLECT_L_CD,T1.COLLECT_M_CD, ";
        $query .= "   T1.COLLECT_S_CD,T1.COLLECT_S_NAME ";
        $query .= " FROM ";
        $query .= "   COLLECT_S_MST T1 ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '".$model->taisyouYear."' AND ";
        $query .= "   T1.COLLECT_L_CD || T1.COLLECT_M_CD || T1.COLLECT_S_CD IN ('".str_replace(",","','",$model->field["COLLECT_S_CD"])."') ";
        return $query;
    }

    //会計細目割当一覧(左リスト)
    function GetSelectScd($model)
    {
        $query  = " SELECT ";
        $query .= "     T2.COLLECT_L_CD,T2.COLLECT_M_CD, ";
        $query .= "     T2.COLLECT_S_CD,T2.COLLECT_S_NAME ";
        $query .= " FROM ";
        $query .= "     V_COLLECT_GRP_DAT T1, ";
        $query .= "     COLLECT_S_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->taisyouYear."' AND ";
        $query .= "     T1.COLLECT_KOJIN_FLG != '1' AND ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.COLLECT_L_CD = T2.COLLECT_L_CD AND ";
        $query .= "     T1.COLLECT_M_CD = T2.COLLECT_M_CD AND ";
        $query .= "     T1.COLLECT_S_CD = T2.COLLECT_S_CD AND ";
        $query .= "     T1.COLLECT_GRP_CD = '".$model->exp_grpcd."' ";
        $query .= " ORDER BY ";
        $query .= "     COLLECT_L_CD, ";
        $query .= "     COLLECT_M_CD, ";
        $query .= "     COLLECT_S_CD ";
        return $query;
    }

    //会計細目一覧(右リスト)
    function GetScd($model, $tempcd, $exp_scd)
    {
        $query  = " SELECT ";
        $query .= "   T1.COLLECT_L_CD,T1.COLLECT_M_CD, ";
        $query .= "   T1.COLLECT_S_CD,T1.COLLECT_S_NAME ";
        $query .= " FROM    ";
        $query .= "   COLLECT_S_MST T1 ";
        $query .= " WHERE   ";
        $query .= "   T1.YEAR = '".$model->taisyouYear."' AND ";
        $query .= "   T1.COLLECT_L_CD || T1.COLLECT_M_CD IN ('".implode("','", $tempcd)."') AND ";
        $query .= "   T1.COLLECT_L_CD || T1.COLLECT_M_CD || T1.COLLECT_S_CD NOT IN ('".implode("','", $exp_scd)."') ";
        $query .= " ORDER BY ";
        $query .= "   COLLECT_L_CD, ";
        $query .= "   COLLECT_M_CD, ";
        $query .= "   T1.COLLECT_S_CD ";
        return $query;
    }

    //削除
    function &getDeleteQuery($model,&$db)
    {
        //会計グループマスタ
        $query  = " DELETE FROM COLLECT_GRP_MST ";
        $query .= "  WHERE YEAR = '".$model->taisyouYear."'";
        $query .= "    AND COLLECT_GRP_CD  = '".sprintf("%04d", $model->field["COLLECT_GRP_CD"])."'";
        $db->query($query);

        //会計グループ会計項目データ
        $query  = " DELETE FROM COLLECT_GRP_DAT ";
        $query .= "  WHERE YEAR = '".$model->taisyouYear."'";
        $query .= "    AND COLLECT_GRP_CD  = '".sprintf("%04d",$model->field["COLLECT_GRP_CD"])."'";
        $db->query($query);

        //入金予定項目データ
        $query  = " DELETE FROM COLLECT_MONEY_DUE_M_DAT ";
        $query .= "  WHERE YEAR = '".$model->taisyouYear."'";
        if ($model->grade2 != "00") {
            $query .= "    AND SCHREGNO IN (SELECT T2.SCHREGNO ";
            $query .= "                       FROM COLLECT_GRP_HR_DAT T1, ";
            $query .= "                            SCHREG_REGD_DAT T2 ";
            $query .= "                      WHERE T1.YEAR = T2.YEAR ";
            $query .= "                        AND T1.GRADE = T2.GRADE ";
            $query .= "                        AND T1.HR_CLASS = T2.HR_CLASS ";
            $query .= "                        AND T2.SEMESTER = '".CTRL_SEMESTER."'";
            $query .= "                        AND T1.YEAR = '".$model->taisyouYear."'";
            $query .= "                        AND T1.COLLECT_GRP_CD = '".sprintf("%04d", $model->field["COLLECT_GRP_CD"])."'";
            $query .= "                    )";
        } else {
            $query .= "    AND SCHREGNO IN (SELECT T1.SCHREGNO ";
            $query .= "                       FROM FRESHMAN_DAT T1 ";
            $query .= "                      WHERE ";
            $query .= "                        T1.ENTERYEAR = '".$model->taisyouYear."'";
            $query .= "                    )";
        }
        $query .= "    AND COLLECT_GRP_CD = '".sprintf("%04d", $model->field["COLLECT_GRP_CD"])."' ";
        $db->query($query);

        //入金予定細目データ
        $query  = " DELETE FROM COLLECT_MONEY_DUE_S_DAT ";
        $query .= "  WHERE YEAR = '".$model->taisyouYear."'";
        if ($model->grade2 != "00") {
            $query .= "    AND SCHREGNO IN (SELECT T2.SCHREGNO ";
            $query .= "                       FROM COLLECT_GRP_HR_DAT T1, ";
            $query .= "                            SCHREG_REGD_DAT T2 ";
            $query .= "                      WHERE T1.YEAR = T2.YEAR ";
            $query .= "                        AND T1.GRADE = T2.GRADE ";
            $query .= "                        AND T1.HR_CLASS = T2.HR_CLASS ";
            $query .= "                        AND T2.SEMESTER = '".CTRL_SEMESTER."'";
            $query .= "                        AND T1.YEAR = '".$model->taisyouYear."'";
            $query .= "                        AND T1.COLLECT_GRP_CD = '".sprintf("%04d", $model->field["COLLECT_GRP_CD"])."'";
            $query .= "                    )";
        } else {
            $query .= "    AND SCHREGNO IN (SELECT T1.SCHREGNO ";
            $query .= "                       FROM FRESHMAN_DAT T1 ";
            $query .= "                      WHERE ";
            $query .= "                        T1.ENTERYEAR = '".$model->taisyouYear."'";
            $query .= "                    )";
        }
        $query .= "    AND COLLECT_GRP_CD = '".sprintf("%04d", $model->field["COLLECT_GRP_CD"])."' ";
        $db->query($query);

        //会計グループHRクラスデータ
        $query  = " DELETE FROM COLLECT_GRP_HR_DAT ";
        $query .= "  WHERE YEAR = '".$model->taisyouYear."'";
        $query .= "    AND COLLECT_GRP_CD  = '".sprintf("%04d", $model->field["COLLECT_GRP_CD"])."'";
        $db->query($query);

        return;
    }

    //追加・更新
    function &getUpdateQuery($model,&$db)
    {
        //会計グループマスタ
        $data = array();
        $data["YEAR"][TEXT]             = $model->taisyouYear;
        $data["COLLECT_GRP_CD"][TEXT]   = sprintf("%04d", $model->field["COLLECT_GRP_CD"]);
        $data["COLLECT_GRP_NAME"][TEXT] = $model->field["COLLECT_GRP_NAME"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "COLLECT_GRP_MST");
        $db->query($query);

        //会計グループHRクラスデータ
        if ($model->field["CLASS"]) {
            $class = explode(",", $model->field["CLASS"]);
            foreach ($class as $val) {
                $data = array();
                $data["YEAR"][TEXT]             = $model->taisyouYear;
                $data["GRADE"][TEXT]            = $model->field["GRADE"];
                $data["HR_CLASS"][TEXT]         = $val;
                $data["COLLECT_GRP_CD"][TEXT]   = sprintf("%04d", $model->field["COLLECT_GRP_CD"]);
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "sysdate()";

                $query = Query::insertSQL($data, "COLLECT_GRP_HR_DAT");
                $db->query($query);
            }
        }

        $risyuNashiArray = array();
        //会計項目
        $collectMSexist = array();
        if ($model->field["COLLECT_M_CD"]) {
            $expmcd = explode(",", $model->field["COLLECT_M_CD"]);
            foreach ($expmcd as $val) {
                $sExists = $db->getOne("SELECT COLLECT_S_EXIST_FLG FROM COLLECT_M_MST WHERE YEAR = '".$model->taisyouYear."' AND COLLECT_L_CD || COLLECT_M_CD = '{$val}'");
                if ($sExists == "2") {
                    //会計グループ会計項目データ
                    $data = array();
                    $data["YEAR"][TEXT]             = $model->taisyouYear;
                    $data["COLLECT_GRP_CD"][TEXT]   = sprintf("%04d", $model->field["COLLECT_GRP_CD"]);
                    $data["COLLECT_L_CD"][TEXT]     = substr($val, 0, 2);
                    $data["COLLECT_M_CD"][TEXT]     = substr($val, 2, 2);
                    $data["COLLECT_S_CD"][TEXT]     = "00";
                    $data["REGISTERCD"][TEXT]       = STAFFCD;
                    $data["UPDATED"][FUNC]          = "sysdate()";

                    $query = Query::insertSQL($data, "COLLECT_GRP_DAT");
                    $db->query($query);
                } else {
                    $collectMSexist[$val] = 0;
                }

                //各クラス毎の生徒の学籍番号を取得
                $query  = " WITH STD_CHAIR AS ( ";
                $query .= " SELECT ";
                $query .= "     YEAR, ";
                $query .= "     SUBCLASSCD, ";
                $query .= "     SCHREGNO ";
                $query .= " FROM ";
                $query .= "     SUBCLASS_STD_SELECT_DAT ";
                $query .= " WHERE ";
                $query .= "     YEAR <= '{$model->taisyouYear}' ";
                $query .= " GROUP BY ";
                $query .= "     YEAR, ";
                $query .= "     SUBCLASSCD, ";
                $query .= "     SCHREGNO ";
                $query .= " ), STD_YEAR AS ( ";
                $query .= " SELECT ";
                $query .= "     COUNT(*) AS STDY_CNT, ";
                $query .= "     SCHREGNO, ";
                $query .= "     YEAR ";
                $query .= " FROM ";
                $query .= "     STD_CHAIR ";
                $query .= " WHERE ";
                $query .= "     YEAR <= '{$model->taisyouYear}' ";
                $query .= " GROUP BY ";
                $query .= "     SCHREGNO, ";
                $query .= "     YEAR ";
                $query .= " ), STDY_CNT AS ( ";
                $query .= " SELECT ";
                $query .= "     SCHREGNO, ";
                $query .= "     COUNT(*) AS CNT ";
                $query .= " FROM ";
                $query .= "     STD_YEAR ";
                $query .= " WHERE ";
                $query .= "     YEAR < '{$model->taisyouYear}' ";
                $query .= " GROUP BY ";
                $query .= "     SCHREGNO ";
                $query .= " ) ";
                if ($model->grade2 != "00") {
                    $query .= " SELECT ";
                    $query .= "     T4.SCHREGNO, ";
                    $query .= "     T5.SEX, ";
                    $query .= "     SC_YD.BASE_REMARK1 AS GRD_YOTEI, ";
                    $query .= "     T3.COLLECT_M_MONEY, ";
                    $query .= "     T3.PAY_DIV, ";
                    $query .= "     T3.PAY_DATE, ";
                    $query .= "     T3.SEX AS SCD_SEX, ";
                    $query .= "     T3.GRD_YOTEI AS SCD_GRD_YOTEI, ";
                    $query .= "     T3.MUSYOU, ";
                    $query .= "     VALUE(STD_YEAR.STDY_CNT, 0) AS KAMOKUSU, ";
                    $query .= "     CASE WHEN CAST(VALUE(T5.MUSYOU_KAISU, '0') AS SMALLINT) > VALUE(STDY_CNT.CNT, 0) ";
                    $query .= "          THEN CAST(VALUE(T5.MUSYOU_KAISU, '0') AS SMALLINT) - VALUE(STDY_CNT.CNT, 0) ";
                    $query .= "          ELSE 0 ";
                    $query .= "     END AS MUSYOU_KAISU ";
                    $query .= " FROM ";
                    $query .= "     COLLECT_GRP_HR_DAT T1, ";
                    $query .= "     V_COLLECT_M_MST T3, ";
                    $query .= "     SCHREG_REGD_DAT T4, ";
                    $query .= "     V_SCHREG_BASE_MST T5 ";
                    $query .= "     LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST SC_YD ON SC_YD.SCHREGNO = T5.SCHREGNO ";
                    $query .= "          AND SC_YD.YEAR = '".$model->taisyouYear."' ";
                    $query .= "          AND SC_YD.BASE_SEQ = '001' ";
                    $query .= "     INNER JOIN STD_YEAR ON STD_YEAR.SCHREGNO = T5.SCHREGNO ";
                    $query .= "           AND STD_YEAR.YEAR = '".$model->taisyouYear."' ";
                    $query .= "           AND STD_YEAR.STDY_CNT > 0 ";
                    $query .= "     LEFT JOIN STDY_CNT ON STDY_CNT.SCHREGNO = T5.SCHREGNO ";
                    $query .= " WHERE ";
                    $query .= "     T1.YEAR = '".$model->taisyouYear."' AND ";
                    $query .= "     T1.GRADE = '".$model->field["GRADE"]."' AND ";
                    $query .= "     T1.COLLECT_GRP_CD = '".sprintf("%04d", $model->field["COLLECT_GRP_CD"])."' AND ";
                    $query .= "     T1.YEAR = T3.YEAR AND ";
                    $query .= "     T3.COLLECT_L_CD = '".substr($val, 0, 2)."' AND ";
                    $query .= "     T3.COLLECT_M_CD = '".substr($val, 2, 2)."' AND ";
                    $query .= "     T1.YEAR = T4.YEAR AND ";
                    $query .= "     T1.GRADE = T4.GRADE AND ";
                    $query .= "     T1.HR_CLASS = T4.HR_CLASS AND ";
                    $query .= "     T4.SEMESTER = '".CTRL_SEMESTER."' AND ";
                    $query .= "     T4.SCHREGNO = T5.SCHREGNO ";
                } else {
                    $query .= " SELECT ";
                    $query .= "     T5.SCHREGNO, ";
                    $query .= "     T5.SEX, ";
                    $query .= "     SC_YD.BASE_REMARK1 AS GRD_YOTEI, ";
                    $query .= "     T3.COLLECT_M_MONEY, ";
                    $query .= "     T3.PAY_DIV, ";
                    $query .= "     T3.PAY_DATE, ";
                    $query .= "     T3.SEX AS SCD_SEX, ";
                    $query .= "     T3.GRD_YOTEI AS SCD_GRD_YOTEI, ";
                    $query .= "     T3.MUSYOU, ";
                    $query .= "     VALUE(STD_YEAR.STDY_CNT, 0) AS KAMOKUSU, ";
                    $query .= "     CASE WHEN CAST(VALUE(V_BASE.BASE_REMARK4, '0') AS SMALLINT) > VALUE(STDY_CNT.CNT, 0) ";
                    $query .= "          THEN CAST(VALUE(V_BASE.BASE_REMARK4, '0') AS SMALLINT) - VALUE(STDY_CNT.CNT, 0) ";
                    $query .= "          ELSE 0 ";
                    $query .= "     END AS MUSYOU_KAISU ";
                    $query .= " FROM ";
                    $query .= "     COLLECT_GRP_HR_DAT T1, ";
                    $query .= "     V_COLLECT_M_MST T3, ";
                    $query .= "     FRESHMAN_DAT T5 ";
                    $query .= "     LEFT JOIN SCHREG_BASE_DETAIL_MST V_BASE ON T5.SCHREGNO = V_BASE.SCHREGNO ";
                    $query .= "          AND V_BASE.BASE_SEQ = '004' ";
                    $query .= "     LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST SC_YD ON SC_YD.SCHREGNO = T5.SCHREGNO ";
                    $query .= "          AND SC_YD.YEAR = '".$model->taisyouYear."' ";
                    $query .= "          AND SC_YD.BASE_SEQ = '001' ";
                    $query .= "     LEFT JOIN STD_YEAR ON STD_YEAR.SCHREGNO = T5.SCHREGNO ";
                    $query .= "           AND STD_YEAR.YEAR = '".$model->taisyouYear."' ";
                    $query .= "           AND STD_YEAR.STDY_CNT > 0 ";
                    $query .= "     LEFT JOIN STDY_CNT ON STDY_CNT.SCHREGNO = T5.SCHREGNO ";
                    $query .= " WHERE ";
                    $query .= "     T1.YEAR = '".$model->taisyouYear."' AND ";
                    $query .= "     T1.GRADE = '".$model->field["GRADE"]."' AND ";
                    $query .= "     T1.COLLECT_GRP_CD = '".sprintf("%04d", $model->field["COLLECT_GRP_CD"])."' AND ";
                    $query .= "     T1.YEAR = T3.YEAR AND ";
                    $query .= "     T3.COLLECT_L_CD = '".substr($val, 0, 2)."' AND ";
                    $query .= "     T3.COLLECT_M_CD = '".substr($val, 2, 2)."' AND ";
                    $query .= "     T1.YEAR = T5.ENTERYEAR ";
                }
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    if ($row["SCD_SEX"] != "" && $row["SEX"] != $row["SCD_SEX"]) {
                        continue;
                    }
                    if ($row["SCD_GRD_YOTEI"] != "" && $row["GRD_YOTEI"] != $row["SCD_GRD_YOTEI"]) {
                        continue;
                    }
                    if ($row["MUSYOU"] != "" && $row["MUSYOU"] == "1" && $row["MUSYOU_KAISU"] == 0) {
                        continue;
                    }
                    if ($row["MUSYOU"] != "" && $row["MUSYOU"] == "2" && $row["MUSYOU_KAISU"] > 0) {
                        continue;
                    }
                    if ($row["MUSYOU"] != "") {
                        $row["COLLECT_M_MONEY"] = $row["COLLECT_M_MONEY"] * $row["KAMOKUSU"];
                    } else {
                        $row["KAMOKUSU"] = "1";
                    }

                    //入金予定項目データ
                    $data = array();
                    $data["YEAR"][TEXT]             = $model->taisyouYear;
                    $data["SCHREGNO"][TEXT]         = $row["SCHREGNO"];
                    $data["COLLECT_GRP_CD"][TEXT]   = sprintf("%04d", $model->field["COLLECT_GRP_CD"]);
                    $data["COLLECT_L_CD"][TEXT]     = substr($val, 0, 2);
                    $data["COLLECT_M_CD"][TEXT]     = substr($val, 2, 2);
                    $data["MONEY_DUE"][NUMBER]      = $row["COLLECT_M_MONEY"];
                    $data["COLLECT_CNT"][NUMBER]    = $row["KAMOKUSU"];
                    $data["PAY_DIV"][TEXT]          = $row["PAY_DIV"];
                    $data["PAY_DATE"][DATE]         = $row["PAY_DATE"];
                    $data["REGISTERCD"][TEXT]       = STAFFCD;
                    $data["UPDATED"][FUNC]          = "sysdate()";

                    $query = Query::insertSQL($data, "COLLECT_MONEY_DUE_M_DAT");
                    $db->query($query);
                }
            }
        }

        //会計細目
        if ($model->field["COLLECT_S_CD"]) {
            $expscd = explode(",",$model->field["COLLECT_S_CD"]);
            foreach ($expscd as $val) {
                //会計グループ会計項目データ
                $data = array();
                $data["YEAR"][TEXT]             = $model->taisyouYear;
                $data["COLLECT_GRP_CD"][TEXT]   = sprintf("%04d", $model->field["COLLECT_GRP_CD"]);
                $data["COLLECT_L_CD"][TEXT]     = substr($val, 0, 2);
                $data["COLLECT_M_CD"][TEXT]     = substr($val, 2, 2);
                $data["COLLECT_S_CD"][TEXT]     = substr($val, 4, 2);
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "sysdate()";

                $query = Query::insertSQL($data, "COLLECT_GRP_DAT");
                $db->query($query);

                //チェック用
                $checkLM = substr($val, 0, 4);
                if (array_key_exists($checkLM, $collectMSexist)) {
                    $collectMSexist[$checkLM] = 1;
                }

                //各クラス毎の生徒の学籍番号と性別,細目の金額と対象性別を取得
                $query  = " WITH STD_CHAIR AS ( ";
                $query .= " SELECT ";
                $query .= "     YEAR, ";
                $query .= "     SUBCLASSCD, ";
                $query .= "     SCHREGNO ";
                $query .= " FROM ";
                $query .= "     SUBCLASS_STD_SELECT_DAT ";
                $query .= " WHERE ";
                $query .= "     YEAR <= '{$model->taisyouYear}' ";
                $query .= " GROUP BY ";
                $query .= "     YEAR, ";
                $query .= "     SUBCLASSCD, ";
                $query .= "     SCHREGNO ";
                $query .= " ), STD_YEAR AS ( ";
                $query .= " SELECT ";
                $query .= "     COUNT(*) AS STDY_CNT, ";
                $query .= "     SCHREGNO, ";
                $query .= "     YEAR ";
                $query .= " FROM ";
                $query .= "     STD_CHAIR ";
                $query .= " WHERE ";
                $query .= "     YEAR <= '{$model->taisyouYear}' ";
                $query .= " GROUP BY ";
                $query .= "     SCHREGNO, ";
                $query .= "     YEAR ";
                $query .= " ), STDY_CNT AS ( ";
                $query .= " SELECT ";
                $query .= "     SCHREGNO, ";
                $query .= "     COUNT(*) AS CNT ";
                $query .= " FROM ";
                $query .= "     STD_YEAR ";
                $query .= " WHERE ";
                $query .= "     YEAR < '{$model->taisyouYear}' ";
                $query .= " GROUP BY ";
                $query .= "     SCHREGNO ";
                $query .= " ) ";
                if ($model->grade2 != "00") {
                    $query .= " SELECT ";
                    $query .= "     T4.SCHREGNO, ";
                    $query .= "     T5.SEX, ";
                    $query .= "     SC_YD.BASE_REMARK1 AS GRD_YOTEI, ";
                    $query .= "     T3.COLLECT_S_MONEY, ";
                    $query .= "     T3.SEX AS SCD_SEX, ";
                    $query .= "     T3.GRD_YOTEI AS SCD_GRD_YOTEI, ";
                    $query .= "     T3.MUSYOU, ";
                    $query .= "     VALUE(STD_YEAR.STDY_CNT, 0) AS KAMOKUSU, ";
                    $query .= "     CASE WHEN CAST(VALUE(T5.MUSYOU_KAISU, '0') AS SMALLINT) > VALUE(STDY_CNT.CNT, 0) ";
                    $query .= "          THEN CAST(VALUE(T5.MUSYOU_KAISU, '0') AS SMALLINT) - VALUE(STDY_CNT.CNT, 0) ";
                    $query .= "          ELSE 0 ";
                    $query .= "     END AS MUSYOU_KAISU ";
                    $query .= " FROM ";
                    $query .= "     COLLECT_GRP_HR_DAT T1, ";
                    $query .= "     COLLECT_GRP_DAT T2, ";
                    $query .= "     V_COLLECT_S_MST T3, ";
                    $query .= "     SCHREG_REGD_DAT T4, ";
                    $query .= "     V_SCHREG_BASE_MST T5 ";
                    $query .= "     LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST SC_YD ON SC_YD.SCHREGNO = T5.SCHREGNO ";
                    $query .= "          AND SC_YD.YEAR = '".$model->taisyouYear."' ";
                    $query .= "          AND SC_YD.BASE_SEQ = '001' ";
                    $query .= "     INNER JOIN STD_YEAR ON STD_YEAR.SCHREGNO = T5.SCHREGNO ";
                    $query .= "           AND STD_YEAR.YEAR = '".$model->taisyouYear."' ";
                    $query .= "           AND STD_YEAR.STDY_CNT > 0 ";
                    $query .= "     LEFT JOIN STDY_CNT ON STDY_CNT.SCHREGNO = T5.SCHREGNO ";
                    $query .= " WHERE ";
                    $query .= "     T1.YEAR = '".$model->taisyouYear."' AND ";
                    $query .= "     T1.YEAR = T2.YEAR AND ";
                    $query .= "     T1.COLLECT_GRP_CD = T2.COLLECT_GRP_CD AND ";
                    $query .= "     T1.YEAR = T3.YEAR AND ";
                    $query .= "     T2.COLLECT_L_CD = T3.COLLECT_L_CD AND ";
                    $query .= "     T2.COLLECT_M_CD = T3.COLLECT_M_CD AND ";
                    $query .= "     T2.COLLECT_S_CD = T3.COLLECT_S_CD AND ";
                    $query .= "     T1.YEAR = T4.YEAR AND ";
                    $query .= "     T1.GRADE = T4.GRADE AND ";
                    $query .= "     T1.HR_CLASS = T4.HR_CLASS AND ";
                    $query .= "     T4.SCHREGNO = T5.SCHREGNO AND ";
                    $query .= "     T4.SEMESTER = '".CTRL_SEMESTER."' AND ";
                    $query .= "     T1.GRADE = '".$model->field["GRADE"]."' AND ";
                    $query .= "     T1.COLLECT_GRP_CD = '".sprintf("%04d", $model->field["COLLECT_GRP_CD"])."' AND ";
                    $query .= "     T3.COLLECT_L_CD = '".substr($val, 0, 2)."' AND ";
                    $query .= "     T3.COLLECT_M_CD = '".substr($val, 2, 2)."' AND ";
                    $query .= "     T3.COLLECT_S_CD = '".substr($val, 4, 2)."' ";
                } else {
                    $query .= " SELECT ";
                    $query .= "     T5.SCHREGNO, ";
                    $query .= "     T5.SEX, ";
                    $query .= "     SC_YD.BASE_REMARK1 AS GRD_YOTEI, ";
                    $query .= "     T3.COLLECT_S_MONEY, ";
                    $query .= "     T3.SEX AS SCD_SEX, ";
                    $query .= "     T3.GRD_YOTEI AS SCD_GRD_YOTEI, ";
                    $query .= "     T3.MUSYOU, ";
                    $query .= "     VALUE(STD_YEAR.STDY_CNT, 0) AS KAMOKUSU, ";
                    $query .= "     CASE WHEN CAST(VALUE(V_BASE.BASE_REMARK4, '0') AS SMALLINT) > VALUE(STDY_CNT.CNT, 0) ";
                    $query .= "          THEN CAST(VALUE(V_BASE.BASE_REMARK4, '0') AS SMALLINT) - VALUE(STDY_CNT.CNT, 0) ";
                    $query .= "          ELSE 0 ";
                    $query .= "     END AS MUSYOU_KAISU ";
                    $query .= " FROM ";
                    $query .= "     COLLECT_GRP_HR_DAT T1, ";
                    $query .= "     COLLECT_GRP_DAT T2, ";
                    $query .= "     V_COLLECT_S_MST T3, ";
                    $query .= "     FRESHMAN_DAT T5 ";
                    $query .= "     LEFT JOIN SCHREG_BASE_DETAIL_MST V_BASE ON T5.SCHREGNO = V_BASE.SCHREGNO ";
                    $query .= "          AND V_BASE.BASE_SEQ = '004' ";
                    $query .= "     LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST SC_YD ON SC_YD.SCHREGNO = T5.SCHREGNO ";
                    $query .= "          AND SC_YD.YEAR = '".$model->taisyouYear."' ";
                    $query .= "          AND SC_YD.BASE_SEQ = '001' ";
                    $query .= "     LEFT JOIN STD_YEAR ON STD_YEAR.SCHREGNO = T5.SCHREGNO ";
                    $query .= "           AND STD_YEAR.YEAR = '".$model->taisyouYear."' ";
                    $query .= "           AND STD_YEAR.STDY_CNT > 0 ";
                    $query .= "     LEFT JOIN STDY_CNT ON STDY_CNT.SCHREGNO = T5.SCHREGNO ";
                    $query .= " WHERE ";
                    $query .= "     T1.YEAR = T2.YEAR AND ";
                    $query .= "     T1.COLLECT_GRP_CD = T2.COLLECT_GRP_CD AND ";
                    $query .= "     T1.YEAR = T3.YEAR AND ";
                    $query .= "     T2.COLLECT_L_CD = T3.COLLECT_L_CD AND ";
                    $query .= "     T2.COLLECT_M_CD = T3.COLLECT_M_CD AND ";
                    $query .= "     T2.COLLECT_S_CD = T3.COLLECT_S_CD AND ";
                    $query .= "     T1.YEAR = T5.ENTERYEAR AND ";
                    $query .= "     T1.YEAR = '".$model->taisyouYear."' AND ";
                    $query .= "     T5.ENTERYEAR = '".$model->taisyouYear."' AND ";
                    $query .= "     T1.GRADE = '".$model->field["GRADE"]."' AND ";
                    $query .= "     T1.COLLECT_GRP_CD = '".sprintf("%04d", $model->field["COLLECT_GRP_CD"])."' AND ";
                    $query .= "     T3.COLLECT_L_CD = '".substr($val, 0, 2)."' AND ";
                    $query .= "     T3.COLLECT_M_CD = '".substr($val, 2, 2)."' AND ";
                    $query .= "     T3.COLLECT_S_CD = '".substr($val, 4, 2)."' ";
                }
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    if ($row["SCD_SEX"] != "" && $row["SEX"] != $row["SCD_SEX"]) {
                        continue;
                    }
                    if ($row["SCD_GRD_YOTEI"] != "" && $row["GRD_YOTEI"] != $row["SCD_GRD_YOTEI"]) {
                        continue;
                    }
                    if ($row["MUSYOU"] != "" && $row["MUSYOU"] == "1" && $row["MUSYOU_KAISU"] == 0) {
                        continue;
                    }
                    if ($row["MUSYOU"] != "" && $row["MUSYOU"] == "2" && $row["MUSYOU_KAISU"] > 0) {
                        continue;
                    }
                    if ($row["MUSYOU"] != "") {
                        $row["COLLECT_S_MONEY"] = $row["COLLECT_S_MONEY"] * $row["KAMOKUSU"];
                    } else {
                        $row["KAMOKUSU"] = "1";
                    }

                    //入金予定細目データ
                    $data = array();
                    $data["YEAR"][TEXT]             = $model->taisyouYear;
                    $data["SCHREGNO"][TEXT]         = $row["SCHREGNO"];
                    $data["COLLECT_GRP_CD"][TEXT]   = sprintf("%04d", $model->field["COLLECT_GRP_CD"]);
                    $data["COLLECT_L_CD"][TEXT]     = substr($val, 0, 2);
                    $data["COLLECT_M_CD"][TEXT]     = substr($val, 2, 2);
                    $data["COLLECT_S_CD"][TEXT]     = substr($val, 4, 2);
                    $data["MONEY_DUE"][NUMBER]      = $row["COLLECT_S_MONEY"];
                    $data["COLLECT_CNT"][NUMBER]    = $row["KAMOKUSU"];
                    $data["REGISTERCD"][TEXT]       = STAFFCD;
                    $data["UPDATED"][FUNC]          = "sysdate()";

                    $query = Query::insertSQL($data, "COLLECT_MONEY_DUE_S_DAT");
                    $db->query($query);
                }
            }
        }
        return $collectMSexist;
    }

    //前年度からデータをコピー
    function &getCopyYearQuery($model, $div)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $setYear = $div != "SINNYU" ? $model->leftSetYear : $model->leftSetYear + 1;

        //会計グループマスタ
        $query  = " DELETE FROM COLLECT_GRP_MST ";
        $query .= "  WHERE YEAR = '".$setYear."'";
        $query .= "        AND VALUE(COLLECT_KOJIN_FLG, '0') != '1' ";
        $db->query($query);

        $query  = "INSERT INTO COLLECT_GRP_MST(";
        $query .= " YEAR,";
        $query .= " COLLECT_GRP_CD,";
        $query .= " COLLECT_GRP_NAME,";
        $query .= " REGISTERCD,";
        $query .= " UPDATED)";
        $query .= " SELECT";
        $query .= " '$setYear',";
        $query .= " COLLECT_GRP_CD,";
        $query .= " COLLECT_GRP_NAME,";
        $query .= " '" .STAFFCD. "', sysdate()";
        $query .= " FROM COLLECT_GRP_MST";
        $query .= " WHERE ";
        $query .= "     YEAR = '" . ($setYear - 1) . "' ";
        $query .= "     AND VALUE(COLLECT_KOJIN_FLG, '0') != '1' ";
        $db->query($query);

        //会計グループHRクラスデータ
        $query  = " DELETE FROM COLLECT_GRP_HR_DAT T1 ";
        $query .= "  WHERE T1.YEAR = '".$setYear."'";
        $query .= "        AND EXISTS(SELECT ";
        $query .= "                        'x' ";
        $query .= "                   FROM ";
        $query .= "                        COLLECT_GRP_MST T2 ";
        $query .= "                   WHERE ";
        $query .= "                        T2.YEAR = T1.YEAR ";
        $query .= "                        AND T2.COLLECT_GRP_CD = T1.COLLECT_GRP_CD ";
        $query .= "                        AND VALUE(T2.COLLECT_KOJIN_FLG, '0') = '0' ";
        $query .= "                  ) ";
        if ($div != "SINNYU") {
            $query .= "     AND GRADE != '00' ";
        } else {
            $query .= "     AND GRADE = '00' ";
        }

        $db->query($query);

        if ($div != "SINNYU") {
            $query  = "INSERT INTO COLLECT_GRP_HR_DAT(";
            $query .= " YEAR,";
            $query .= " GRADE,";
            $query .= " HR_CLASS,";
            $query .= " COLLECT_GRP_CD,";
            $query .= " REGISTERCD,";
            $query .= " UPDATED)";
            $query .= " SELECT";
            $query .= "     '$setYear',";
            $query .= "     T1.GRADE,";
            $query .= "     T1.HR_CLASS,";
            $query .= "     T1.COLLECT_GRP_CD,";
            $query .= "     '" .STAFFCD. "', sysdate()";
            $query .= " FROM";
            $query .= "     (SELECT T1.GRADE,T1.HR_CLASS,T1.COLLECT_GRP_CD ";
            $query .= "        FROM COLLECT_GRP_HR_DAT T1 ";
            $query .= "       WHERE YEAR = '".($setYear - 1)."' ";
            $query .= "             AND EXISTS(SELECT ";
            $query .= "                             'x' ";
            $query .= "                        FROM ";
            $query .= "                             COLLECT_GRP_MST T2 ";
            $query .= "                        WHERE ";
            $query .= "                             T2.YEAR = T1.YEAR ";
            $query .= "                             AND T2.COLLECT_GRP_CD = T1.COLLECT_GRP_CD ";
            $query .= "                             AND VALUE(T2.COLLECT_KOJIN_FLG, '0') = '0' ";
            $query .= "                       ) ";
            $query .= "     ) T1,";
            $query .= "     (SELECT GRADE,HR_CLASS";
            $query .= "        FROM SCHREG_REGD_HDAT";
            $query .= "       WHERE YEAR = '".$setYear."'";
            $query .= "         AND SEMESTER = '".CTRL_SEMESTER."'";
            $query .= "     ) T2";
            $query .= " WHERE ";
            $query .= "     T1.GRADE = T2.GRADE AND ";
            $query .= "     T1.GRADE != '00' AND ";
            $query .= "     T1.HR_CLASS = T2.HR_CLASS ";
        } else {
            $query  = "INSERT INTO COLLECT_GRP_HR_DAT(";
            $query .= " YEAR,";
            $query .= " GRADE,";
            $query .= " HR_CLASS,";
            $query .= " COLLECT_GRP_CD,";
            $query .= " REGISTERCD,";
            $query .= " UPDATED)";
            $query .= " SELECT";
            $query .= "     '$setYear',";
            $query .= "     GRADE,";
            $query .= "     HR_CLASS,";
            $query .= "     COLLECT_GRP_CD,";
            $query .= "     '" .STAFFCD. "', sysdate()";
            $query .= " FROM";
            $query .= "     COLLECT_GRP_HR_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".($setYear - 1)."'";
            $query .= "     AND GRADE = '00' ";
        }
        $db->query($query);

        //会計グループデータ
        $query  = " DELETE FROM COLLECT_GRP_DAT T1 ";
        $query .= "  WHERE T1.YEAR = '".$setYear."'";
        $query .= "        AND EXISTS(SELECT ";
        $query .= "                        'x' ";
        $query .= "                   FROM ";
        $query .= "                        COLLECT_GRP_MST T2 ";
        $query .= "                   WHERE ";
        $query .= "                        T2.YEAR = T1.YEAR ";
        $query .= "                        AND T2.COLLECT_GRP_CD = T1.COLLECT_GRP_CD ";
        $query .= "                        AND VALUE(T2.COLLECT_KOJIN_FLG, '0') = '0' ";
        $query .= "                  ) ";
        $db->query($query);

        $query  = "INSERT INTO COLLECT_GRP_DAT(";
        $query .= " YEAR,";
        $query .= " COLLECT_GRP_CD,";
        $query .= " COLLECT_L_CD,";
        $query .= " COLLECT_M_CD,";
        $query .= " COLLECT_S_CD,";
        $query .= " REGISTERCD,";
        $query .= " UPDATED)";
        $query .= " SELECT";
        $query .= "     '$setYear',";
        $query .= "     COLLECT_GRP_CD,";
        $query .= "     COLLECT_L_CD,";
        $query .= "     COLLECT_M_CD,";
        $query .= "     COLLECT_S_CD,";
        $query .= "     '" .STAFFCD. "', SYSDATE()";
        $query .= " FROM";
        $query .= "     V_COLLECT_GRP_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".($setYear - 1)."' ";
        $query .= "     AND COLLECT_KOJIN_FLG != '1' ";
        $db->query($query);

        //入金予定項目データ
        $query  = " DELETE FROM COLLECT_MONEY_DUE_M_DAT ";
        $query .= "  WHERE YEAR = '".$setYear."'";
        $db->query($query);

        //各クラス毎の生徒の学籍番号を取得
        $query  = " WITH STD_CHAIR AS ( ";
        $query .= " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     SUBCLASSCD, ";
        $query .= "     SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_STD_SELECT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR <= '{$setYear}' ";
        $query .= " GROUP BY ";
        $query .= "     YEAR, ";
        $query .= "     SUBCLASSCD, ";
        $query .= "     SCHREGNO ";
        $query .= " ), STD_YEAR AS ( ";
        $query .= " SELECT ";
        $query .= "     COUNT(*) AS STDY_CNT, ";
        $query .= "     SCHREGNO, ";
        $query .= "     YEAR ";
        $query .= " FROM ";
        $query .= "     STD_CHAIR ";
        $query .= " WHERE ";
        $query .= "     YEAR <= '{$setYear}' ";
        $query .= " GROUP BY ";
        $query .= "     SCHREGNO, ";
        $query .= "     YEAR ";
        $query .= " ), STDY_CNT AS ( ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     STD_YEAR ";
        $query .= " WHERE ";
        $query .= "     YEAR < '{$setYear}' ";
        $query .= " GROUP BY ";
        $query .= "     SCHREGNO ";
        $query .= " ) ";
        if ($div != "SINNYU") {
            $query .= " SELECT DISTINCT ";
            $query .= "     T4.SCHREGNO, ";
            $query .= "     T2.COLLECT_GRP_CD, ";
            $query .= "     T3.COLLECT_L_CD, ";
            $query .= "     T3.COLLECT_M_CD, ";
            $query .= "     T3.PAY_DIV, ";
            $query .= "     T3.PAY_DATE, ";
            $query .= "     T5.SEX, ";
            $query .= "     SC_YD.BASE_REMARK1 AS GRD_YOTEI, ";
            $query .= "     T3.COLLECT_M_MONEY, ";
            $query .= "     T3.SEX AS SCD_SEX, ";
            $query .= "     T3.GRD_YOTEI AS SCD_GRD_YOTEI, ";
            $query .= "     T3.MUSYOU, ";
            $query .= "     VALUE(STD_YEAR.STDY_CNT, 0) AS KAMOKUSU, ";
            $query .= "     CASE WHEN CAST(VALUE(T5.MUSYOU_KAISU, '0') AS SMALLINT) > VALUE(STDY_CNT.CNT, 0) ";
            $query .= "          THEN CAST(VALUE(T5.MUSYOU_KAISU, '0') AS SMALLINT) - VALUE(STDY_CNT.CNT, 0) ";
            $query .= "          ELSE 0 ";
            $query .= "     END AS MUSYOU_KAISU ";
            $query .= " FROM ";
            $query .= "     COLLECT_GRP_HR_DAT T1, ";
            $query .= "     V_COLLECT_GRP_DAT T2, ";
            $query .= "     V_COLLECT_M_MST T3, ";
            $query .= "     SCHREG_REGD_DAT T4, ";
            $query .= "     V_SCHREG_BASE_MST T5 ";
            $query .= "     LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST SC_YD ON SC_YD.SCHREGNO = T5.SCHREGNO ";
            $query .= "          AND SC_YD.YEAR = '".$setYear."' ";
            $query .= "          AND SC_YD.BASE_SEQ = '001' ";
            $query .= "     INNER JOIN STD_YEAR ON STD_YEAR.SCHREGNO = T5.SCHREGNO ";
            $query .= "           AND STD_YEAR.YEAR = '".$setYear."' ";
            $query .= "           AND STD_YEAR.STDY_CNT > 0 ";
            $query .= "     LEFT JOIN STDY_CNT ON STDY_CNT.SCHREGNO = T5.SCHREGNO ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR = '".$setYear."' AND ";
            $query .= "     T1.YEAR = T2.YEAR AND ";
            $query .= "     T1.COLLECT_GRP_CD = T2.COLLECT_GRP_CD AND ";
            $query .= "     T2.YEAR = T3.YEAR AND ";
            $query .= "     T2.COLLECT_KOJIN_FLG != '1' AND ";
            $query .= "     T2.COLLECT_L_CD = T3.COLLECT_L_CD AND ";
            $query .= "     T2.COLLECT_M_CD = T3.COLLECT_M_CD AND ";
            $query .= "     T1.YEAR = T4.YEAR AND ";
            $query .= "     T1.GRADE = T4.GRADE AND ";
            $query .= "     T1.HR_CLASS = T4.HR_CLASS AND ";
            $query .= "     T4.SEMESTER = '".CTRL_SEMESTER."' AND ";
            $query .= "     T4.SCHREGNO = T5.SCHREGNO ";
        } else {
            $query .= " SELECT DISTINCT ";
            $query .= "     T5.SCHREGNO, ";
            $query .= "     T2.COLLECT_GRP_CD, ";
            $query .= "     T3.COLLECT_L_CD, ";
            $query .= "     T3.COLLECT_M_CD, ";
            $query .= "     T3.PAY_DIV, ";
            $query .= "     T3.PAY_DATE, ";
            $query .= "     T5.SEX, ";
            $query .= "     SC_YD.BASE_REMARK1 AS GRD_YOTEI, ";
            $query .= "     T3.COLLECT_M_MONEY, ";
            $query .= "     T3.SEX AS SCD_SEX, ";
            $query .= "     T3.GRD_YOTEI AS SCD_GRD_YOTEI, ";
            $query .= "     T3.MUSYOU, ";
            $query .= "     VALUE(STD_YEAR.STDY_CNT, 0) AS KAMOKUSU, ";
            $query .= "     CASE WHEN CAST(VALUE(V_BASE.BASE_REMARK4, '0') AS SMALLINT) > VALUE(STDY_CNT.CNT, 0) ";
            $query .= "          THEN CAST(VALUE(V_BASE.BASE_REMARK4, '0') AS SMALLINT) - VALUE(STDY_CNT.CNT, 0) ";
            $query .= "          ELSE 0 ";
            $query .= "     END AS MUSYOU_KAISU ";
            $query .= " FROM ";
            $query .= "     COLLECT_GRP_HR_DAT T1, ";
            $query .= "     V_COLLECT_GRP_DAT T2, ";
            $query .= "     V_COLLECT_M_MST T3, ";
            $query .= "     FRESHMAN_DAT T5 ";
            $query .= "     LEFT JOIN SCHREG_BASE_DETAIL_MST V_BASE ON T5.SCHREGNO = V_BASE.SCHREGNO ";
            $query .= "          AND V_BASE.BASE_SEQ = '004' ";
            $query .= "     LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST SC_YD ON SC_YD.SCHREGNO = T5.SCHREGNO ";
            $query .= "          AND SC_YD.YEAR = '".$setYear."' ";
            $query .= "          AND SC_YD.BASE_SEQ = '001' ";
            $query .= "     LEFT JOIN STD_YEAR ON STD_YEAR.SCHREGNO = T5.SCHREGNO ";
            $query .= "           AND STD_YEAR.YEAR = '".$setYear."' ";
            $query .= "           AND STD_YEAR.STDY_CNT > 0 ";
            $query .= "     LEFT JOIN STDY_CNT ON STDY_CNT.SCHREGNO = T5.SCHREGNO ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR = '".$setYear."' AND ";
            $query .= "     T1.YEAR = T2.YEAR AND ";
            $query .= "     T1.COLLECT_GRP_CD = T2.COLLECT_GRP_CD AND ";
            $query .= "     T2.YEAR = T3.YEAR AND ";
            $query .= "     T2.COLLECT_KOJIN_FLG != '1' AND ";
            $query .= "     T2.COLLECT_L_CD = T3.COLLECT_L_CD AND ";
            $query .= "     T2.COLLECT_M_CD = T3.COLLECT_M_CD AND ";
            $query .= "     T1.COLLECT_GRP_CD = T2.COLLECT_GRP_CD AND ";
            $query .= "     T1.YEAR = T3.YEAR AND ";
            $query .= "     T1.YEAR = T5.ENTERYEAR ";
        }
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["SCD_SEX"] != "" && $row["SEX"] != $row["SCD_SEX"]) {
                continue;
            }
            if ($row["SCD_GRD_YOTEI"] != "" && $row["GRD_YOTEI"] != $row["SCD_GRD_YOTEI"]) {
                continue;
            }
            if ($row["MUSYOU"] != "" && $row["MUSYOU"] == "1" && $row["MUSYOU_KAISU"] == 0) {
                continue;
            }
            if ($row["MUSYOU"] != "" && $row["MUSYOU"] == "2" && $row["MUSYOU_KAISU"] > 0) {
                continue;
            }
            if ($row["MUSYOU"] != "") {
                $row["COLLECT_M_MONEY"] = $row["COLLECT_M_MONEY"] * $row["KAMOKUSU"];
            } else {
                $row["KAMOKUSU"] = "1";
            }

            //入金予定項目データ
            $data = array();
            $data["YEAR"][TEXT]             = $setYear;
            $data["SCHREGNO"][TEXT]         = $row["SCHREGNO"];
            $data["COLLECT_GRP_CD"][TEXT]   = $row["COLLECT_GRP_CD"];
            $data["COLLECT_L_CD"][TEXT]     = $row["COLLECT_L_CD"];
            $data["COLLECT_M_CD"][TEXT]     = $row["COLLECT_M_CD"];
            $data["MONEY_DUE"][NUMBER]      = $row["COLLECT_M_MONEY"];
            $data["COLLECT_CNT"][NUMBER]    = $row["KAMOKUSU"];
            $data["PAY_DIV"][TEXT]          = $row["PAY_DIV"];
            $data["PAY_DATE"][DATE]         = $row["PAY_DATE"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $query = Query::insertSQL($data, "COLLECT_MONEY_DUE_M_DAT");
            $db->query($query);
        }

        //入金予定細目データ
        $query  = " DELETE FROM COLLECT_MONEY_DUE_S_DAT ";
        $query .= "  WHERE YEAR = '".$setYear."'";
        $db->query($query);

        //各クラス毎の生徒の学籍番号と性別,細目の金額と対象性別を取得
        $query  = " WITH STD_CHAIR AS ( ";
        $query .= " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     SUBCLASSCD, ";
        $query .= "     SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_STD_SELECT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR <= '{$setYear}' ";
        $query .= " GROUP BY ";
        $query .= "     YEAR, ";
        $query .= "     SUBCLASSCD, ";
        $query .= "     SCHREGNO ";
        $query .= " ), STD_YEAR AS ( ";
        $query .= " SELECT ";
        $query .= "     COUNT(*) AS STDY_CNT, ";
        $query .= "     SCHREGNO, ";
        $query .= "     YEAR ";
        $query .= " FROM ";
        $query .= "     STD_CHAIR ";
        $query .= " WHERE ";
        $query .= "     YEAR <= '{$setYear}' ";
        $query .= " GROUP BY ";
        $query .= "     SCHREGNO, ";
        $query .= "     YEAR ";
        $query .= " ), STDY_CNT AS ( ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     STD_YEAR ";
        $query .= " WHERE ";
        $query .= "     YEAR < '{$setYear}' ";
        $query .= " GROUP BY ";
        $query .= "     SCHREGNO ";
        $query .= " ) ";
        if ($div != "SINNYU") {
            $query .= " SELECT ";
            $query .= "     T4.SCHREGNO, ";
            $query .= "     T2.COLLECT_GRP_CD, ";
            $query .= "     T3.COLLECT_L_CD, ";
            $query .= "     T3.COLLECT_M_CD, ";
            $query .= "     T3.COLLECT_S_CD, ";
            $query .= "     T5.SEX, ";
            $query .= "     SC_YD.BASE_REMARK1 AS GRD_YOTEI, ";
            $query .= "     T3.COLLECT_S_MONEY, ";
            $query .= "     T3.SEX AS SCD_SEX, ";
            $query .= "     T3.GRD_YOTEI AS SCD_GRD_YOTEI, ";
            $query .= "     T3.MUSYOU, ";
            $query .= "     VALUE(STD_YEAR.STDY_CNT, 0) AS KAMOKUSU, ";
            $query .= "     CASE WHEN CAST(VALUE(T5.MUSYOU_KAISU, '0') AS SMALLINT) > VALUE(STDY_CNT.CNT, 0) ";
            $query .= "          THEN CAST(VALUE(T5.MUSYOU_KAISU, '0') AS SMALLINT) - VALUE(STDY_CNT.CNT, 0) ";
            $query .= "          ELSE 0 ";
            $query .= "     END AS MUSYOU_KAISU ";
            $query .= " FROM ";
            $query .= "     COLLECT_GRP_HR_DAT T1, ";
            $query .= "     V_COLLECT_GRP_DAT T2, ";
            $query .= "     V_COLLECT_S_MST T3, ";
            $query .= "     SCHREG_REGD_DAT T4, ";
            $query .= "     V_SCHREG_BASE_MST T5 ";
            $query .= "     LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST SC_YD ON SC_YD.SCHREGNO = T5.SCHREGNO ";
            $query .= "          AND SC_YD.YEAR = '".$setYear."' ";
            $query .= "          AND SC_YD.BASE_SEQ = '001' ";
            $query .= "     INNER JOIN STD_YEAR ON STD_YEAR.SCHREGNO = T5.SCHREGNO ";
            $query .= "           AND STD_YEAR.YEAR = '".$setYear."' ";
            $query .= "           AND STD_YEAR.STDY_CNT > 0 ";
            $query .= "     LEFT JOIN STDY_CNT ON STDY_CNT.SCHREGNO = T5.SCHREGNO ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR = '".$setYear."' AND ";
            $query .= "     T1.YEAR = T2.YEAR AND ";
            $query .= "     T1.COLLECT_GRP_CD = T2.COLLECT_GRP_CD AND ";
            $query .= "     T2.YEAR = T3.YEAR AND ";
            $query .= "     T2.COLLECT_KOJIN_FLG != '1' AND ";
            $query .= "     T2.COLLECT_L_CD = T3.COLLECT_L_CD AND ";
            $query .= "     T2.COLLECT_M_CD = T3.COLLECT_M_CD AND ";
            $query .= "     T2.COLLECT_S_CD = T3.COLLECT_S_CD AND ";
            $query .= "     T2.YEAR = T3.YEAR AND ";
            $query .= "     T2.COLLECT_L_CD = T3.COLLECT_L_CD AND ";
            $query .= "     T2.COLLECT_M_CD = T3.COLLECT_M_CD AND ";
            $query .= "     T2.COLLECT_S_CD = T3.COLLECT_S_CD AND ";
            $query .= "     T1.YEAR = T4.YEAR AND ";
            $query .= "     T4.SEMESTER = '".CTRL_SEMESTER."' AND ";
            $query .= "     T1.GRADE = T4.GRADE AND ";
            $query .= "     T1.HR_CLASS = T4.HR_CLASS AND ";
            $query .= "     T4.SCHREGNO = T5.SCHREGNO ";
        } else {
            $query .= " SELECT ";
            $query .= "     T5.SCHREGNO, ";
            $query .= "     T2.COLLECT_GRP_CD, ";
            $query .= "     T3.COLLECT_L_CD, ";
            $query .= "     T3.COLLECT_M_CD, ";
            $query .= "     T3.COLLECT_S_CD, ";
            $query .= "     T5.SEX, ";
            $query .= "     SC_YD.BASE_REMARK1 AS GRD_YOTEI, ";
            $query .= "     T3.COLLECT_S_MONEY, ";
            $query .= "     T3.SEX AS SCD_SEX, ";
            $query .= "     T3.GRD_YOTEI AS SCD_GRD_YOTEI, ";
            $query .= "     T3.MUSYOU, ";
            $query .= "     VALUE(STD_YEAR.STDY_CNT, 0) AS KAMOKUSU, ";
            $query .= "     CASE WHEN CAST(VALUE(V_BASE.BASE_REMARK4, '0') AS SMALLINT) > VALUE(STDY_CNT.CNT, 0) ";
            $query .= "          THEN CAST(VALUE(V_BASE.BASE_REMARK4, '0') AS SMALLINT) - VALUE(STDY_CNT.CNT, 0) ";
            $query .= "          ELSE 0 ";
            $query .= "     END AS MUSYOU_KAISU ";
            $query .= " FROM ";
            $query .= "     COLLECT_GRP_HR_DAT T1, ";
            $query .= "     V_COLLECT_GRP_DAT T2, ";
            $query .= "     V_COLLECT_S_MST T3, ";
            $query .= "     FRESHMAN_DAT T5 ";
            $query .= "     LEFT JOIN SCHREG_BASE_DETAIL_MST V_BASE ON T5.SCHREGNO = V_BASE.SCHREGNO ";
            $query .= "          AND V_BASE.BASE_SEQ = '004' ";
            $query .= "     LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST SC_YD ON SC_YD.SCHREGNO = T5.SCHREGNO ";
            $query .= "          AND SC_YD.YEAR = '".$setYear."' ";
            $query .= "          AND SC_YD.BASE_SEQ = '001' ";
            $query .= "     LEFT JOIN STD_YEAR ON STD_YEAR.SCHREGNO = T5.SCHREGNO ";
            $query .= "           AND STD_YEAR.YEAR = '".$setYear."' ";
            $query .= "           AND STD_YEAR.STDY_CNT > 0 ";
            $query .= "     LEFT JOIN STDY_CNT ON STDY_CNT.SCHREGNO = T5.SCHREGNO ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR = '".$setYear."' AND ";
            $query .= "     T1.YEAR = T2.YEAR AND ";
            $query .= "     T1.COLLECT_GRP_CD = T2.COLLECT_GRP_CD AND ";
            $query .= "     T2.YEAR = T3.YEAR AND ";
            $query .= "     T2.COLLECT_KOJIN_FLG != '1' AND ";
            $query .= "     T2.COLLECT_L_CD = T3.COLLECT_L_CD AND ";
            $query .= "     T2.COLLECT_M_CD = T3.COLLECT_M_CD AND ";
            $query .= "     T2.COLLECT_S_CD = T3.COLLECT_S_CD AND ";
            $query .= "     T2.YEAR = T3.YEAR AND ";
            $query .= "     T2.COLLECT_L_CD = T3.COLLECT_L_CD AND ";
            $query .= "     T2.COLLECT_M_CD = T3.COLLECT_M_CD AND ";
            $query .= "     T2.COLLECT_S_CD = T3.COLLECT_S_CD AND ";
            $query .= "     T1.YEAR = T5.ENTERYEAR ";
        }
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["SCD_SEX"] != "" && $row["SEX"] != $row["SCD_SEX"]) {
                continue;
            }
            if ($row["SCD_GRD_YOTEI"] != "" && $row["GRD_YOTEI"] != $row["SCD_GRD_YOTEI"]) {
                continue;
            }
            if ($row["MUSYOU"] != "" && $row["MUSYOU"] == "1" && $row["MUSYOU_KAISU"] == 0) {
                continue;
            }
            if ($row["MUSYOU"] != "" && $row["MUSYOU"] == "2" && $row["MUSYOU_KAISU"] > 0) {
                continue;
            }
            if ($row["MUSYOU"] != "") {
                $row["COLLECT_S_MONEY"] = $row["COLLECT_S_MONEY"] * $row["KAMOKUSU"];
            } else {
                $row["KAMOKUSU"] = "1";
            }

            //入金予定細目データ
            $data = array();
            $data["YEAR"][TEXT]             = $setYear;
            $data["SCHREGNO"][TEXT]         = $row["SCHREGNO"];
            $data["COLLECT_GRP_CD"][TEXT]   = $row["COLLECT_GRP_CD"];
            $data["COLLECT_L_CD"][TEXT]     = $row["COLLECT_L_CD"];
            $data["COLLECT_M_CD"][TEXT]     = $row["COLLECT_M_CD"];
            $data["COLLECT_S_CD"][TEXT]     = $row["COLLECT_S_CD"];
            $data["MONEY_DUE"][NUMBER]      = $row["COLLECT_S_MONEY"];
            $data["COLLECT_CNT"][NUMBER]    = $row["KAMOKUSU"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $query = Query::insertSQL($data, "COLLECT_MONEY_DUE_S_DAT");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);

        return;
    }

}
?>
