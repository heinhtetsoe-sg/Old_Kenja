<?php

require_once('for_php7.php');

class knjp716Query extends Query {

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
    function get_Grade($model)
    {
        $query  = " SELECT ";
        $query .= "     GRADE_NAME1 AS LABEL, ";
        $query .= "     GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND GRADE IN (SELECT ";
            $query .= "                      REGD_G.GRADE ";
            $query .= "                   FROM ";
            $query .= "                      SCHREG_REGD_GDAT REGD_G ";
            $query .= "                   WHERE ";
            $query .= "                      REGD_G.YEAR = '".CTRL_YEAR."' ";
            $query .= "                      AND REGD_G.SCHOOL_KIND = '" .SCHOOLKIND ."') ";
        }
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
            $query .= "                        AND T1.COLLECT_GRP_CD = '".sprintf("%04d", $model->exp_grpcd)."'";
            $query .= "                    )";
        } else {
            $query .= "    AND SCHREGNO IN (SELECT T1.SCHREGNO ";
            $query .= "                       FROM FRESHMAN_DAT T1 ";
            $query .= "                      WHERE ";
            $query .= "                        T1.ENTERYEAR = '".$model->taisyouYear."'";
            $query .= "                    )";
        }
        $query .= "    AND COLLECT_GRP_CD = '".sprintf("%04d", $model->exp_grpcd)."' ";
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
            $query .= "                        AND T1.COLLECT_GRP_CD = '".sprintf("%04d", $model->exp_grpcd)."'";
            $query .= "                    )";
        } else {
            $query .= "    AND SCHREGNO IN (SELECT T1.SCHREGNO ";
            $query .= "                       FROM FRESHMAN_DAT T1 ";
            $query .= "                      WHERE ";
            $query .= "                        T1.ENTERYEAR = '".$model->taisyouYear."'";
            $query .= "                    )";
        }
        $query .= "    AND COLLECT_GRP_CD = '".sprintf("%04d", $model->exp_grpcd)."' ";
        $db->query($query);

        //会計グループHRクラスデータ
        $query  = " DELETE FROM COLLECT_GRP_HR_DAT ";
        $query .= "  WHERE YEAR = '".$model->taisyouYear."'";
        $query .= "    AND COLLECT_GRP_CD  = '".sprintf("%04d", $model->exp_grpcd)."'";
        $db->query($query);

        return;
    }

    //項目細目
    function getMcdScd($model)
    {
        $query  = " SELECT ";
        $query .= "     COLLECT_L_CD, ";
        $query .= "     COLLECT_M_CD, ";
        $query .= "     COLLECT_S_CD ";
        $query .= " FROM    ";
        $query .= "   COLLECT_GRP_DAT ";
        $query .= " WHERE   ";
        $query .= "   YEAR = '".$model->taisyouYear."' AND ";
        $query .= "   COLLECT_GRP_CD = '{$model->exp_grpcd}' ";
        $query .= " ORDER BY ";
        $query .= "   COLLECT_L_CD, ";
        $query .= "   COLLECT_M_CD, ";
        $query .= "   COLLECT_S_CD ";
        return $query;
    }

    //追加・更新
    function &getUpdateQuery($model,&$db)
    {
        //会計グループHRクラスデータ
        if ($model->field["CLASS"]) {
            $class = explode(",", $model->field["CLASS"]);
            foreach ($class as $val) {
                $data = array();
                $data["YEAR"][TEXT]             = $model->taisyouYear;
                $data["GRADE"][TEXT]            = $model->field["GRADE"];
                $data["HR_CLASS"][TEXT]         = $val;
                $data["COLLECT_GRP_CD"][TEXT]   = sprintf("%04d", $model->exp_grpcd);
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "sysdate()";

                $query = Query::insertSQL($data, "COLLECT_GRP_HR_DAT");
                $db->query($query);
            }
        }

        $risyuNashiArray = array();
        //会計項目
        if ($model->field["COLLECT_M_CD"]) {
            $expmcd = explode(",", $model->field["COLLECT_M_CD"]);
            foreach ($expmcd as $val) {
                //各クラス毎の生徒の学籍番号を取得
                if ($model->grade2 != "00") {
                    $query  = " SELECT ";
                    $query .= "     T4.SCHREGNO, ";
                    $query .= "     T5.SEX, ";
                    $query .= "     T3.COLLECT_M_MONEY, ";
                    $query .= "     T3.PAY_DIV, ";
                    $query .= "     T3.PAY_DATE, ";
                    $query .= "     T3.SEX AS SCD_SEX, ";
                    $query .= "     T3.GRD_YOTEI AS SCD_GRD_YOTEI ";
                    $query .= " FROM ";
                    $query .= "     COLLECT_GRP_HR_DAT T1, ";
                    $query .= "     V_COLLECT_M_MST T3, ";
                    $query .= "     SCHREG_REGD_DAT T4, ";
                    $query .= "     V_SCHREG_BASE_MST T5 ";
                    $query .= " WHERE ";
                    $query .= "     T1.YEAR = '".$model->taisyouYear."' AND ";
                    $query .= "     T1.GRADE = '".$model->field["GRADE"]."' AND ";
                    $query .= "     T1.COLLECT_GRP_CD = '".sprintf("%04d", $model->exp_grpcd)."' AND ";
                    $query .= "     T1.YEAR = T3.YEAR AND ";
                    $query .= "     T3.COLLECT_L_CD = '".substr($val, 0, 2)."' AND ";
                    $query .= "     T3.COLLECT_M_CD = '".substr($val, 2, 2)."' AND ";
                    $query .= "     T1.YEAR = T4.YEAR AND ";
                    $query .= "     T1.GRADE = T4.GRADE AND ";
                    $query .= "     T1.HR_CLASS = T4.HR_CLASS AND ";
                    $query .= "     T4.SEMESTER = '".CTRL_SEMESTER."' AND ";
                    $query .= "     T4.SCHREGNO = T5.SCHREGNO ";
                } else {
                    $query  = " SELECT ";
                    $query .= "     T5.SCHREGNO, ";
                    $query .= "     T5.SEX, ";
                    $query .= "     T3.COLLECT_M_MONEY, ";
                    $query .= "     T3.PAY_DIV, ";
                    $query .= "     T3.PAY_DATE, ";
                    $query .= "     T3.SEX AS SCD_SEX, ";
                    $query .= "     T3.GRD_YOTEI AS SCD_GRD_YOTEI ";
                    $query .= " FROM ";
                    $query .= "     COLLECT_GRP_HR_DAT T1, ";
                    $query .= "     V_COLLECT_M_MST T3, ";
                    $query .= "     FRESHMAN_DAT T5 ";
                    $query .= " WHERE ";
                    $query .= "     T1.YEAR = '".$model->taisyouYear."' AND ";
                    $query .= "     T1.GRADE = '".$model->field["GRADE"]."' AND ";
                    $query .= "     T1.COLLECT_GRP_CD = '".sprintf("%04d", $model->exp_grpcd)."' AND ";
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

                    //入金予定項目データ
                    $data = array();
                    $data["YEAR"][TEXT]             = $model->taisyouYear;
                    $data["SCHREGNO"][TEXT]         = $row["SCHREGNO"];
                    $data["COLLECT_GRP_CD"][TEXT]   = sprintf("%04d", $model->exp_grpcd);
                    $data["COLLECT_L_CD"][TEXT]     = substr($val, 0, 2);
                    $data["COLLECT_M_CD"][TEXT]     = substr($val, 2, 2);
                    $data["MONEY_DUE"][NUMBER]      = $row["COLLECT_M_MONEY"];
                    $data["COLLECT_CNT"][NUMBER]    = "1";
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

                //各クラス毎の生徒の学籍番号と性別,細目の金額と対象性別を取得
                if ($model->grade2 != "00") {
                    $query  = " SELECT ";
                    $query .= "     T4.SCHREGNO, ";
                    $query .= "     T5.SEX, ";
                    $query .= "     T3.COLLECT_S_MONEY, ";
                    $query .= "     T3.SEX AS SCD_SEX, ";
                    $query .= "     T3.GRD_YOTEI AS SCD_GRD_YOTEI ";
                    $query .= " FROM ";
                    $query .= "     COLLECT_GRP_HR_DAT T1, ";
                    $query .= "     COLLECT_GRP_DAT T2, ";
                    $query .= "     V_COLLECT_S_MST T3, ";
                    $query .= "     SCHREG_REGD_DAT T4, ";
                    $query .= "     V_SCHREG_BASE_MST T5 ";
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
                    $query .= "     T1.COLLECT_GRP_CD = '".sprintf("%04d", $model->exp_grpcd)."' AND ";
                    $query .= "     T3.COLLECT_L_CD = '".substr($val, 0, 2)."' AND ";
                    $query .= "     T3.COLLECT_M_CD = '".substr($val, 2, 2)."' AND ";
                    $query .= "     T3.COLLECT_S_CD = '".substr($val, 4, 2)."' ";
                } else {
                    $query  = " SELECT ";
                    $query .= "     T5.SCHREGNO, ";
                    $query .= "     T5.SEX, ";
                    $query .= "     T3.COLLECT_S_MONEY, ";
                    $query .= "     T3.SEX AS SCD_SEX, ";
                    $query .= "     T3.GRD_YOTEI AS SCD_GRD_YOTEI ";
                    $query .= " FROM ";
                    $query .= "     COLLECT_GRP_HR_DAT T1, ";
                    $query .= "     COLLECT_GRP_DAT T2, ";
                    $query .= "     V_COLLECT_S_MST T3, ";
                    $query .= "     FRESHMAN_DAT T5 ";
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
                    $query .= "     T1.COLLECT_GRP_CD = '".sprintf("%04d", $model->exp_grpcd)."' AND ";
                    $query .= "     T3.COLLECT_L_CD = '".substr($val, 0, 2)."' AND ";
                    $query .= "     T3.COLLECT_M_CD = '".substr($val, 2, 2)."' AND ";
                    $query .= "     T3.COLLECT_S_CD = '".substr($val, 4, 2)."' ";
                }
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    if ($row["SCD_SEX"] != "" && $row["SEX"] != $row["SCD_SEX"]) {
                        continue;
                    }

                    //入金予定細目データ
                    $data = array();
                    $data["YEAR"][TEXT]             = $model->taisyouYear;
                    $data["SCHREGNO"][TEXT]         = $row["SCHREGNO"];
                    $data["COLLECT_GRP_CD"][TEXT]   = sprintf("%04d", $model->exp_grpcd);
                    $data["COLLECT_L_CD"][TEXT]     = substr($val, 0, 2);
                    $data["COLLECT_M_CD"][TEXT]     = substr($val, 2, 2);
                    $data["COLLECT_S_CD"][TEXT]     = substr($val, 4, 2);
                    $data["MONEY_DUE"][NUMBER]      = $row["COLLECT_S_MONEY"];
                    $data["COLLECT_CNT"][NUMBER]    = "1";
                    $data["REGISTERCD"][TEXT]       = STAFFCD;
                    $data["UPDATED"][FUNC]          = "sysdate()";

                    $query = Query::insertSQL($data, "COLLECT_MONEY_DUE_S_DAT");
                    $db->query($query);
                }
            }
        }
        return;
    }

}
?>
