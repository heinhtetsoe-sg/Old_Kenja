<?php

require_once('for_php7.php');

class knjp713Query extends Query {

    //年度
    function getYear() {
        $query  = " WITH CTRL_T (LABEL, VALUE) AS ( ";
        $query .= "     VALUES('".CTRL_YEAR."', '".CTRL_YEAR."') ";
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        $query .= "     YEAR AS LABEL, ";
        $query .= "     YEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "     COLLECT_GRP_MST ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     CTRL_T ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //前年度からコピー時のデータ存在チェック
    function selectYearQuery($year, $grade, $table)
    {
        $query = "SELECT COUNT(*) FROM $table WHERE YEAR = '{$year}' AND GRADE = '{$grade}' ";

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
        $query .= "       WHERE YEAR = '".($model->taisyouYear - 1)."' ";
        $query .= "             AND GRADE = '{$model->taisyouGrade}' ";
        $query .= "             AND COLLECT_KOJIN_FLG != '1') T1 ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     (SELECT COLLECT_L_CD || COLLECT_M_CD AS COLLECT_LM_CD ";
        $query .= "        FROM COLLECT_M_MST ";
        $query .= "       WHERE YEAR = '{$model->taisyouYear}') T2 ";
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
        $query .= "       WHERE YEAR = '".($model->taisyouYear - 1)."' ";
        $query .= "             AND GRADE = '{$model->taisyouGrade}' ";
        $query .= "             AND COLLECT_KOJIN_FLG != '1') T1 ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     (SELECT COLLECT_L_CD || COLLECT_M_CD || COLLECT_S_CD AS COLLECT_LMS_CD ";
        $query .= "        FROM COLLECT_S_MST ";
        $query .= "       WHERE YEAR = '{$model->taisyouYear}') T2 ";
        $query .= " ON ";
        $query .= "         T1.COLLECT_LMS_CD = T2.COLLECT_LMS_CD ";

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
        $query .= "     YEAR = '{$model->taisyouYear}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //課程学科
    function getMajor($model) {
        $query  = " WITH ALL_T (LABEL, VALUE) AS ( ";
        $query .= "     VALUES('0-000:全て', '0-000') ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     COURSECD || '-' || MAJORCD || ':' || MAJORNAME AS LABEL, ";
        $query .= "     COURSECD || '-' || MAJORCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_MAJOR_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->taisyouYear}' ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ALL_T ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //コース
    function getCourse($model) {
        $query  = " WITH ALL_T (LABEL, VALUE) AS ( ";
        $query .= "     VALUES('0000:全て', '0000') ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     COURSECODE || ':' || COURSECODENAME AS LABEL, ";
        $query .= "     COURSECODE AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_COURSECODE_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->taisyouYear}' ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ALL_T ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //会計グループリスト
    function SelectQuery($model)
    {
        $query .= " SELECT ";
        $query .= "     GRP_M.GRADE, ";
        $query .= "     GDAT.GRADE_NAME1, ";
        $query .= "     GRP_M.COURSECD || '-' || GRP_M.MAJORCD AS MAJORCD, ";
        $query .= "     V_MAJOR.MAJORNAME, ";
        $query .= "     GRP_M.COURSECODE, ";
        $query .= "     V_COURSE.COURSECODENAME, ";
        $query .= "     GRP_M.COLLECT_GRP_CD, ";
        $query .= "     GRP_M.COLLECT_GRP_NAME, ";
        $query .= "     GRP_D.COLLECT_L_CD, ";
        $query .= "     GRP_D.COLLECT_M_CD, ";
        $query .= "     COL_M.COLLECT_M_NAME, ";
        $query .= "     CASE WHEN GRP_D.COLLECT_S_CD = '00' ";
        $query .= "          THEN NULL ";
        $query .= "          ELSE GRP_D.COLLECT_S_CD ";
        $query .= "     END AS COLLECT_S_CD, ";
        $query .= "     COL_S.COLLECT_S_NAME ";
        $query .= " FROM ";
        $query .= "     COLLECT_GRP_MST GRP_M ";
        $query .= "     LEFT JOIN V_COLLECT_GRP_DAT GRP_D ON GRP_D.YEAR = GRP_M.YEAR ";
        $query .= "          AND GRP_D.GRADE = GRP_M.GRADE ";
        $query .= "          AND GRP_D.COURSECD = GRP_M.COURSECD ";
        $query .= "          AND GRP_D.MAJORCD = GRP_M.MAJORCD ";
        $query .= "          AND GRP_D.COURSECODE = GRP_M.COURSECODE ";
        $query .= "          AND GRP_D.COLLECT_GRP_CD = GRP_M.COLLECT_GRP_CD ";
        $query .= "     LEFT JOIN COLLECT_M_MST COL_M ON GRP_M.YEAR = COL_M.YEAR ";
        $query .= "          AND GRP_D.COLLECT_L_CD = COL_M.COLLECT_L_CD ";
        $query .= "          AND GRP_D.COLLECT_M_CD = COL_M.COLLECT_M_CD ";
        $query .= "     LEFT JOIN COLLECT_S_MST COL_S ON GRP_M.YEAR = COL_S.YEAR ";
        $query .= "          AND GRP_D.COLLECT_L_CD = COL_S.COLLECT_L_CD ";
        $query .= "          AND GRP_D.COLLECT_M_CD = COL_S.COLLECT_M_CD ";
        $query .= "          AND GRP_D.COLLECT_S_CD = COL_S.COLLECT_S_CD ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT GDAT ON GRP_M.YEAR = GDAT.YEAR ";
        $query .= "          AND GRP_D.GRADE = GDAT.GRADE ";
        $query .= "     LEFT JOIN V_MAJOR_MST V_MAJOR ON GRP_M.YEAR = V_MAJOR.YEAR ";
        $query .= "          AND GRP_D.COURSECD = V_MAJOR.COURSECD ";
        $query .= "          AND GRP_D.MAJORCD = V_MAJOR.MAJORCD ";
        $query .= "     LEFT JOIN V_COURSECODE_MST V_COURSE ON GRP_M.YEAR = V_COURSE.YEAR ";
        $query .= "          AND GRP_D.COURSECODE = V_COURSE.COURSECODE ";
        $query .= " WHERE ";
        $query .= "     GRP_M.YEAR = '{$model->taisyouYear}' ";
        $query .= "     AND GRP_M.GRADE = '{$model->taisyouGrade}' ";
        $query .= "     AND VALUE(GRP_D.COLLECT_KOJIN_FLG, '0') != '1' ";
        $query .= " ORDER BY ";
        $query .= "     GRP_M.GRADE, ";
        $query .= "     GRP_M.COURSECD || '-' || GRP_M.MAJORCD, ";
        $query .= "     GRP_M.COURSECODE, ";
        $query .= "     COLLECT_GRP_CD, ";
        $query .= "     COLLECT_L_CD, ";
        $query .= "     COLLECT_M_CD, ";
        $query .= "     COLLECT_S_CD ";
        return $query;
    }

    //１レコード取得
    function getRow($model, $flg)
    {
        $query  = " SELECT ";
        $query .= "     GRADE, ";
        $query .= "     COURSECD || '-' || MAJORCD AS MAJORCD, ";
        $query .= "     COURSECODE, ";
        $query .= "     COLLECT_GRP_CD, ";
        $query .= "     COLLECT_GRP_NAME, ";
        $query .= "     COLLECT_KOJIN_FLG ";
        $query .= " FROM ";
        $query .= "     COLLECT_GRP_MST ";
        $query .= " WHERE YEAR = '{$model->taisyouYear}'";
        $query .= "     AND GRADE = '{$model->taisyouGrade}' ";
        if ($flg == 1) {
            $query .= "     AND COURSECD || '-' || MAJORCD = '{$model->exp_majorCd}' ";
            $query .= "     AND COURSECODE = '{$model->exp_courseCode}' ";
            $query .= "     AND COLLECT_GRP_CD = '{$model->exp_grpcd}'";
        } else {
            $query .= "   AND COURSECD || '-' || MAJORCD = '{$model->field["MAJORCD"]}'";
            $query .= "   AND COURSECODE = '{$model->field["COURSECODE"]}'";
            $query .= "   AND COLLECT_GRP_CD = '".sprintf("%04d", $model->field["COLLECT_GRP_CD"])."'";
        }

        return $query;
    }

    //グループデータの存在チェック
    function getGrpData($year, $model)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     COLLECT_GRP_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND GRADE = '{$model->taisyouGrade}' ";
        $query .= "     AND COURSECD || '-' || MAJORCD = '{$model->exp_majorCd}' ";
        $query .= "     AND COURSECODE = '{$model->exp_courseCode}' ";
        $query .= "     AND COLLECT_GRP_CD = '{$model->exp_grpcd}' ";
        return $query;
    }

    //会計科目コード取得
    function getCollectLcd()
    {
        $query  = "  SELECT ";
        $query .= "      COLLECT_L_CD AS VALUE, ";
        $query .= "      COLLECT_L_CD || ':' || COLLECT_L_NAME AS LABEL ";
        $query .= "  FROM ";
        $query .= "      COLLECT_L_MST ";
        $query .= "  ORDER BY ";
        $query .= "      VALUE ";

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
        $query .= "   YEAR = '{$model->taisyouYear}' AND ";
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
        $query .= "   T1.YEAR = '{$model->taisyouYear}' AND ";
        $query .= "   T1.GRADE = '{$model->taisyouGrade}' AND ";
        $query .= "   T1.COURSECD || '-' || T1.MAJORCD = '{$model->exp_majorCd}' AND ";
        $query .= "   T1.COURSECODE = '{$model->exp_courseCode}' AND ";
        $query .= "   T1.COLLECT_GRP_CD = '{$model->exp_grpcd}' AND ";
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
        $query .= "   YEAR = '{$model->taisyouYear}' AND ";
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
        $query .= "   T1.YEAR = '{$model->taisyouYear}' AND ";
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
        $query .= "     T1.YEAR = '{$model->taisyouYear}' AND ";
        $query .= "     T1.COLLECT_KOJIN_FLG != '1' AND ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.COLLECT_L_CD = T2.COLLECT_L_CD AND ";
        $query .= "     T1.COLLECT_M_CD = T2.COLLECT_M_CD AND ";
        $query .= "     T1.COLLECT_S_CD = T2.COLLECT_S_CD AND ";
        $query .= "     T1.GRADE = '{$model->taisyouGrade}' AND ";
        $query .= "     T1.COURSECD || '-' || T1.MAJORCD = '{$model->exp_majorCd}' AND ";
        $query .= "     T1.COURSECODE = '{$model->exp_courseCode}' AND ";
        $query .= "     T1.COLLECT_GRP_CD = '{$model->exp_grpcd}' ";
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
        $query .= "   T1.YEAR = '{$model->taisyouYear}' AND ";
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
        $query .= "  WHERE YEAR = '{$model->taisyouYear}'";
        $query .= "    AND GRADE  = '{$model->taisyouGrade}'";
        $query .= "    AND COURSECD || '-' || MAJORCD  = '{$model->field["MAJORCD"]}'";
        $query .= "    AND COURSECODE  = '{$model->field["COURSECODE"]}'";
        $query .= "    AND COLLECT_GRP_CD  = '".sprintf("%04d", $model->field["COLLECT_GRP_CD"])."'";
        $db->query($query);

        //会計グループ会計項目データ
        $query  = " DELETE FROM COLLECT_GRP_DAT ";
        $query .= "  WHERE YEAR = '{$model->taisyouYear}'";
        $query .= "    AND GRADE  = '{$model->taisyouGrade}'";
        $query .= "    AND COURSECD || '-' || MAJORCD  = '{$model->field["MAJORCD"]}'";
        $query .= "    AND COURSECODE  = '{$model->field["COURSECODE"]}'";
        $query .= "    AND COLLECT_GRP_CD  = '".sprintf("%04d",$model->field["COLLECT_GRP_CD"])."'";
        $db->query($query);

        return;
    }

    //追加・更新
    function &getUpdateQuery($model,&$db)
    {
        //会計グループマスタ
        $data = array();
        $data["YEAR"][TEXT]             = $model->taisyouYear;
        $data["GRADE"][TEXT]            = $model->taisyouGrade;
        $data["COURSECD"][TEXT]         = $model->field["SET_COURSECD"];
        $data["MAJORCD"][TEXT]          = $model->field["SET_MAJORCD"];
        $data["COURSECODE"][TEXT]       = $model->field["COURSECODE"];
        $data["COLLECT_GRP_CD"][TEXT]   = sprintf("%04d", $model->field["COLLECT_GRP_CD"]);
        $data["COLLECT_GRP_NAME"][TEXT] = $model->field["COLLECT_GRP_NAME"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "COLLECT_GRP_MST");
        $db->query($query);

        //会計項目
        $collectMSexist = array();
        if ($model->field["COLLECT_M_CD"]) {
            $expmcd = explode(",", $model->field["COLLECT_M_CD"]);
            foreach ($expmcd as $val) {
                $sExists = $db->getOne("SELECT COLLECT_S_EXIST_FLG FROM COLLECT_M_MST WHERE YEAR = '{$model->taisyouYear}' AND COLLECT_L_CD || COLLECT_M_CD = '{$val}'");
                if ($sExists == "2") {
                    //会計グループ会計項目データ
                    $data = array();
                    $data["YEAR"][TEXT]             = $model->taisyouYear;
                    $data["GRADE"][TEXT]            = $model->taisyouGrade;
                    $data["COURSECD"][TEXT]         = $model->field["SET_COURSECD"];
                    $data["MAJORCD"][TEXT]          = $model->field["SET_MAJORCD"];
                    $data["COURSECODE"][TEXT]       = $model->field["COURSECODE"];
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
            }
        }

        //会計細目
        if ($model->field["COLLECT_S_CD"]) {
            $expscd = explode(",",$model->field["COLLECT_S_CD"]);
            foreach ($expscd as $val) {
                //会計グループ会計項目データ
                $data = array();
                $data["YEAR"][TEXT]             = $model->taisyouYear;
                $data["GRADE"][TEXT]            = $model->taisyouGrade;
                $data["COURSECD"][TEXT]         = $model->field["SET_COURSECD"];
                $data["MAJORCD"][TEXT]          = $model->field["SET_MAJORCD"];
                $data["COURSECODE"][TEXT]       = $model->field["COURSECODE"];
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
            }
        }
        return $collectMSexist;
    }

    //前年度からデータをコピー
    function &getCopyYearQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $setYear = $model->taisyouYear;

        //会計グループマスタ
        $query  = " DELETE FROM COLLECT_GRP_MST ";
        $query .= "  WHERE YEAR = '{$setYear}'";
        $query .= "        AND GRADE = '{$model->taisyouGrade}' ";
        $query .= "        AND VALUE(COLLECT_KOJIN_FLG, '0') != '1' ";
        $db->query($query);

        $query  = "INSERT INTO COLLECT_GRP_MST(";
        $query .= " YEAR,";
        $query .= " GRADE,";
        $query .= " COURSECD,";
        $query .= " MAJORCD,";
        $query .= " COURSECODE,";
        $query .= " COLLECT_GRP_CD,";
        $query .= " COLLECT_GRP_NAME,";
        $query .= " REGISTERCD,";
        $query .= " UPDATED)";
        $query .= " SELECT";
        $query .= " '{$setYear}',";
        $query .= " GRADE,";
        $query .= " COURSECD,";
        $query .= " MAJORCD,";
        $query .= " COURSECODE,";
        $query .= " COLLECT_GRP_CD,";
        $query .= " COLLECT_GRP_NAME,";
        $query .= " '" .STAFFCD. "', sysdate()";
        $query .= " FROM COLLECT_GRP_MST";
        $query .= " WHERE ";
        $query .= "     YEAR = '" . ($setYear - 1) . "' ";
        $query .= "     AND VALUE(COLLECT_KOJIN_FLG, '0') != '1' ";
        $db->query($query);

        //会計グループデータ
        $query  = " DELETE FROM COLLECT_GRP_DAT T1 ";
        $query .= "  WHERE T1.YEAR = '{$setYear}'";
        $query .= "        AND EXISTS(SELECT ";
        $query .= "                        'x' ";
        $query .= "                   FROM ";
        $query .= "                        COLLECT_GRP_MST T2 ";
        $query .= "                   WHERE ";
        $query .= "                        T2.YEAR = T1.YEAR ";
        $query .= "                        AND T2.GRADE = T1.GRADE ";
        $query .= "                        AND T2.COURSECD = T1.COURSECD ";
        $query .= "                        AND T2.MAJORCD = T1.MAJORCD ";
        $query .= "                        AND T2.COURSECODE = T1.COURSECODE ";
        $query .= "                        AND T2.COLLECT_GRP_CD = T1.COLLECT_GRP_CD ";
        $query .= "                        AND VALUE(T2.COLLECT_KOJIN_FLG, '0') = '0' ";
        $query .= "                  ) ";
        $db->query($query);

        $query  = "INSERT INTO COLLECT_GRP_DAT(";
        $query .= " YEAR,";
        $query .= " GRADE,";
        $query .= " COURSECD,";
        $query .= " MAJORCD,";
        $query .= " COURSECODE,";
        $query .= " COLLECT_GRP_CD,";
        $query .= " COLLECT_L_CD,";
        $query .= " COLLECT_M_CD,";
        $query .= " COLLECT_S_CD,";
        $query .= " REGISTERCD,";
        $query .= " UPDATED)";
        $query .= " SELECT";
        $query .= "     '{$setYear}',";
        $query .= "     GRADE,";
        $query .= "     COURSECD,";
        $query .= "     MAJORCD,";
        $query .= "     COURSECODE,";
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

        $db->commit();
        Query::dbCheckIn($db);

        return;
    }

}
?>
