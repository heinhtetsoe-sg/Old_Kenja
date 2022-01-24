<?php

require_once('for_php7.php');

class knje701Query extends Query
{
    //表示用併願データリスト
    public function getHeiganList()
    {
        $query  = "    SELECT IRHH.HEIGAN_CD ";
        $query .= "         , IRHH.HEIGAN_GROUPNAME ";
        $query .= "         , IRHD.FACULITYCD ";
        $query .= "         , IRHD.DEPARTMENTCD ";
        $query .= "         , CDM.DEPARTMENTNAME ";
        $query .= "      FROM INT_RECOMMENDATION_HEIGAN_HDAT IRHH ";
        $query .= " LEFT JOIN INT_RECOMMENDATION_HEIGAN_DAT IRHD ";
        $query .= "        ON IRHD.YEAR      = IRHH.YEAR ";
        $query .= "       AND IRHD.HEIGAN_CD = IRHH.HEIGAN_CD ";
        $query .= " LEFT JOIN COLLEGE_DEPARTMENT_MST CDM ";
        $query .= "        ON CDM.SCHOOL_CD    = '00003231' ";
        $query .= "       AND CDM.FACULTYCD    = IRHD.FACULITYCD ";
        $query .= "       AND CDM.DEPARTMENTCD = IRHD.DEPARTMENTCD ";
        $query .= "     WHERE IRHH.YEAR = '".CTRL_YEAR."' ";
        $query .= "  ORDER BY IRHH.HEIGAN_CD ";
        $query .= "         , IRHH.HEIGAN_GROUPNAME ";

        return $query;
    }

    //リンククリック用(INT_RECOMMENDATION_HEIGAN_HDAT)データ
    public function getHdat($model)
    {
        $query  = "    SELECT IRHH.HEIGAN_CD ";
        $query .= "         , IRHH.HEIGAN_GROUPNAME ";
        $query .= "      FROM INT_RECOMMENDATION_HEIGAN_HDAT IRHH ";
        $query .= "     WHERE IRHH.YEAR             = '".CTRL_YEAR."' ";
        $query .= "       AND IRHH.HEIGAN_CD        = '" .$model->heigancd. "'";
        $query .= "       AND IRHH.HEIGAN_GROUPNAME = '" .$model->heigangname. "'";

        return $query;
    }

    //リンククリック用(INT_RECOMMENDATION_HEIGAN_DAT)データ
    public function getDat($model)
    {
        $query  = "    SELECT IRHD.HEIGAN_CD ";
        $query .= "         , IRHD.FACULITYCD ";
        $query .= "         , IRHD.DEPARTMENTCD ";
        $query .= "      FROM INT_RECOMMENDATION_HEIGAN_DAT IRHD ";
        $query .= "     WHERE IRHD.YEAR = '".CTRL_YEAR."' ";
        if ($model->field["FACULITYCD"] === "" || $model->field["FACULITYCD"] === null) {
            //追加・更新・取消ボタン押下時
            $query .= "       AND IRHD.HEIGAN_CD  = '" .sprintf("%02d", $model->heigancd). "'";
            $query .= "       AND IRHD.FACULITYCD = '" .$model->facultycd. "'";
            $query .= "       AND IRHD.DEPARTMENTCD IN( '" .str_replace("-", "','", $model->departmentcd). "')";
        } else {
            //リンククリック時
            $query .= "       AND IRHD.HEIGAN_CD  = '" .sprintf("%02d", $model->field["HEIGANCD"]). "'";
            $query .= "       AND IRHD.FACULITYCD = '" .$model->field["FACULITYCD"]. "'";
            $query .= "       AND IRHD.DEPARTMENTCD IN( '" .str_replace("-", "','", $model->departmentcd). "')";
        }

        return $query;
    }

    //学部情報取得
    public function getFaculty()
    {
        $query  = "";
        $query  = "   SELECT CFM.FACULTYNAME AS LABEL ";
        $query .= "        , CFM.FACULTYCD AS VALUE ";
        $query .= "     FROM COLLEGE_FACULTY_MST CFM ";
        $query .= "    WHERE CFM.SCHOOL_CD = '00003231' ";
        $query .= " ORDER BY VALUE ";

        return $query;
    }

    //学科情報取得
    public function getDepartment($model, $mode)
    {
        $query  = "";
        $query  = "   SELECT CDM.DEPARTMENTNAME AS LABEL ";
        $query .= "        , CDM.DEPARTMENTCD AS VALUE ";
        $query .= "     FROM COLLEGE_DEPARTMENT_MST CDM ";
        $query .= "    WHERE CDM.SCHOOL_CD = '00003231' ";
        if ($model->checkFlg == false) {
            $query .= "      AND CDM.FACULTYCD = '" .$model->field["FACULITYCD"]. "'";
            if ($mode == "left") {
                //エラー発生時の左側
                $query .= "      AND CDM.DEPARTMENTCD IN('" .str_replace(",", "','", $model->field["selectleft"]). "')";
            } elseif ($mode == "right") {
                //エラー発生時の右側
                $query .= "      AND CDM.DEPARTMENTCD NOT IN('" .str_replace(",", "','", $model->field["selectleft"]). "')";
            }
        } else {
            if ($model->field["FACULITYCD"] === null) {
                $query .= "      AND CDM.FACULTYCD = '" .$model->facultycd. "'";
                if ($mode == "left") {
                    //リンククリック時の左側
                    $query .= "      AND CDM.DEPARTMENTCD IN('" .str_replace("-", "','", $model->departmentcd). "')";
                } elseif ($mode == "right") {
                    //リンククリック時の右側
                    $query .= "      AND CDM.DEPARTMENTCD NOT IN('" .str_replace("-", "','", $model->departmentcd). "')";
                }
            } else {
                //学部コンボ変更時
                $query .= "      AND CDM.FACULTYCD = '" .$model->field["FACULITYCD"]. "'";
            }
        }
        $query .= " ORDER BY VALUE ";

        return $query;
    }

    //更新時の重複チェック
    public function duplicateCheckHdat($model, $mode = "")
    {
        $query  = "";
        $query  = " SELECT COUNT(*) AS CNT ";
        $query .= "   FROM INT_RECOMMENDATION_HEIGAN_HDAT ";
        $query .= "  WHERE YEAR = '".CTRL_YEAR."' ";
        if ($mode == "") {
            //併願コードでの重複チェック
            $query .= "      AND HEIGAN_CD = '" .sprintf("%02d", $model->field["HEIGANCD"]). "'";
        } elseif ($mode == "1") {
            //併願名称での重複チェック
            $query .= "      AND HEIGAN_GROUPNAME = '" .$model->field["HEIGANGNAME"]. "'";
        }

        return $query;
    }

    //更新時の重複チェック
    public function duplicateCheckDat($model)
    {
        $query  = "";
        $query  = " SELECT COUNT(*) AS CNT ";
        $query .= "   FROM INT_RECOMMENDATION_HEIGAN_DAT ";
        $query .= "  WHERE YEAR         = '" .CTRL_YEAR."' ";
        $query .= "    AND FACULITYCD   = '" .$model->field["FACULITYCD"]. "'";
        $query .= "    AND DEPARTMENTCD IN('" .str_replace(",", "','", $model->field["selectleft"]). "')";

        return $query;
    }

    //前年度コピーの存在チェック
    public function existenceCheck($tableName, $year)
    {
        $query  = "";
        $query  = " SELECT COUNT(*) AS CNT ";
        $query .= "   FROM ".$tableName ;
        $query .= "  WHERE YEAR = '" .$year. "' ";

        return $query;
    }

    //前年度コピー用(INT_RECOMMENDATION_HEIGAN_HDAT)データ取得
    public function getHdatCopyDate($year)
    {
        $query  = "    SELECT IRHH.HEIGAN_CD ";
        $query .= "         , IRHH.HEIGAN_GROUPNAME ";
        $query .= "      FROM INT_RECOMMENDATION_HEIGAN_HDAT IRHH ";
        $query .= "     WHERE IRHH.YEAR = '".$year."' ";

        return $query;
    }

    ///前年度コピー用(INT_RECOMMENDATION_HEIGAN_HDAT)データ取得
    public function getDatCopyDate($year)
    {
        $query  = "    SELECT IRHD.HEIGAN_CD ";
        $query .= "         , IRHD.FACULITYCD ";
        $query .= "         , IRHD.DEPARTMENTCD ";
        $query .= "      FROM INT_RECOMMENDATION_HEIGAN_DAT IRHD ";
        $query .= "     WHERE IRHD.YEAR = '".$year."' ";

        return $query;
    }

    //前年度からのデータを今年度にCOPY(INT_RECOMMENDATION_HEIGAN_HDAT)
    public function copyInsertHdat($row)
    {
        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["HEIGAN_CD"][TEXT]        = $row["HEIGAN_CD"];
        $data["HEIGAN_GROUPNAME"][TEXT] = $row["HEIGAN_GROUPNAME"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $query = Query::insertSQL($data, "INT_RECOMMENDATION_HEIGAN_HDAT");
        return $query;
    }

    //前年度からのデータを今年度にCOPY(INT_RECOMMENDATION_HEIGAN_DAT)
    public function copyInsertDat($row)
    {
        $data["YEAR"][TEXT]         = CTRL_YEAR;
        $data["HEIGAN_CD"][TEXT]    = $row["HEIGAN_CD"];
        $data["FACULITYCD"][TEXT]   = $row["FACULITYCD"];
        $data["DEPARTMENTCD"][TEXT] = $row["DEPARTMENTCD"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $query = Query::insertSQL($data, "INT_RECOMMENDATION_HEIGAN_DAT");
        return $query;
    }

    //INSERT
    public function getInsertQueryHdat($model)
    {
        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["HEIGAN_CD"][TEXT]        = sprintf("%02d", $model->field["HEIGANCD"]);
        $data["HEIGAN_GROUPNAME"][TEXT] = $model->field["HEIGANGNAME"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $query = Query::insertSQL($data, "INT_RECOMMENDATION_HEIGAN_HDAT");
        return $query;
    }

    //INSERT
    public function getInsertQueryDat($model, $gakka)
    {
        $data["YEAR"][TEXT]         = CTRL_YEAR;
        $data["HEIGAN_CD"][TEXT]    = sprintf("%02d", $model->field["HEIGANCD"]);
        $data["FACULITYCD"][TEXT]   = $model->field["FACULITYCD"];
        $data["DEPARTMENTCD"][TEXT] = $gakka;
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $query = Query::insertSQL($data, "INT_RECOMMENDATION_HEIGAN_DAT");
        return $query;
    }

    //削除
    public function getDeleteQueryHdat($model, $mode)
    {
        $query  = " DELETE FROM INT_RECOMMENDATION_HEIGAN_HDAT";
        $query .= " WHERE YEAR = '" .CTRL_YEAR."' ";
        if ($mode == "1") {
            //更新処理時
            $query .= "   AND HEIGAN_CD        = '" .sprintf("%02d", $model->field["HEIGANCD"]). "' ";
            $query .= "   AND HEIGAN_GROUPNAME = '" .$model->field["HEIGANGNAME"]. "'";
        } else {
            //削除処理時
            $query .= "   AND HEIGAN_CD        = '" .sprintf("%02d", $model->heigancd). "' ";
            $query .= "   AND HEIGAN_GROUPNAME = '" .$model->heigangname. "'";
        }

        return $query;
    }

    //削除
    public function getDeleteQueryDat($model, $mode)
    {
        $query  = " DELETE FROM INT_RECOMMENDATION_HEIGAN_DAT";
        $query .= " WHERE YEAR = '" .CTRL_YEAR."' ";
        if ($mode == "1") {
            //更新処理時
            $query .= "   AND HEIGAN_CD  = '" .sprintf("%02d", $model->field["HEIGANCD"]). "' ";
            $query .= "   AND FACULITYCD = '" .$model->field["FACULITYCD"]. "'";
        } else {
            //削除処理時
            $query .= "   AND HEIGAN_CD  = '" .sprintf("%02d", $model->heigancd). "' ";
            $query .= "   AND FACULITYCD = '" .$model->facultycd. "'";
        }

        return $query;
    }
}
