<?php

require_once('for_php7.php');
class knjz068Query extends Query
{
    //年度一覧取得
    public function getIBYear()
    {
        $query  = " WITH THIS_YEAR(LABEL, VALUE) AS (VALUES('".CTRL_YEAR."', '".CTRL_YEAR."')) ";
        $query .= " SELECT ";
        $query .= "     LABEL, ";
        $query .= "     VALUE ";
        $query .= " FROM ";
        $query .= "     THIS_YEAR ";
        $query .= " UNION ";
        $query .= " SELECT DISTINCT ";
        $query .= "     IBYEAR AS LABEL, ";
        $query .= "     IBYEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "     IBVIEW_NAME_YMST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //学年一覧取得
    public function getIBGrade($model, $flg)
    {
        $query  = " SELECT ";
        if ($flg == "check") {
            $query .= "     COUNT(*) ";
        } else {
            $query .= "     GRADE AS VALUE, ";
            $query .= "     GRADE_NAME1 AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->ibyear."' ";
        if ($flg == "list") {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //IBコース一覧取得
    public function getIBPrgCourse($model, $flg)
    {
        $query  = " SELECT ";
        if ($flg == "check") {
            $query .= "     COUNT(*) ";
        } else {
            $query .= "     NAME1 AS VALUE, ";
            $query .= "     ABBV1 AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->ibyear."' ";
        $query .= " AND NAMECD1 = 'A034' ";
        $query .= " AND '".$model->ibgrade."' BETWEEN NAME2 AND NAME3 ";
        if ($flg == "list") {
            $query .= " ORDER BY ";
            $query .= "     NAMECD2 ";
        }

        return $query;
    }

    //左フレームのリスト
    public function getList($model)
    {
        $query  = " SELECT ";
        $query .= "      T1.*, ";
        $query .= "      L2.IBSUBCLASSABBV_ENG, ";
        $query .= "      L2.IBSUBCLASSNAME_ENG, ";
        $query .= "      N1.NAME1 AS IBEVAL_DIV1_NAME, ";
        $query .= "      CASE T1.IBEVAL_DIV1 WHEN '1' THEN N3.NAME1 ";
        $query .= "                          WHEN '2' THEN N4.NAME1 ";
        $query .= "                          ELSE N2.NAME1 END AS IBEVAL_DIV2_NAME ";
        $query .= " FROM ";
        $query .= "     IBVIEW_NAME_YMST T1 ";
        $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'A034' ";
        $query .= "                          AND L1.NAME1 = T1.IBPRG_COURSE ";
        $query .= "     LEFT JOIN IBSUBCLASS_MST L2 ON L2.IBCLASSCD         = T1.IBCLASSCD ";
        $query .= "                                AND L2.IBPRG_COURSE      = T1.IBPRG_COURSE ";
        $query .= "                                AND L2.IBCURRICULUM_CD   = T1.IBCURRICULUM_CD ";
        $query .= "                                AND L2.IBSUBCLASSCD      = T1.IBSUBCLASSCD ";
        $query .= "     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'Z035' ";
        $query .= "                          AND N1.NAMECD2 = T1.IBEVAL_DIV1 ";
        $query .= "     LEFT JOIN NAME_MST N2 ON N2.NAMECD1 = 'Z036' ";
        $query .= "                          AND N2.NAMECD2 = T1.IBEVAL_DIV2 ";
        $query .= "     LEFT JOIN NAME_MST N3 ON N3.NAMECD1 = 'Z037' ";
        $query .= "                          AND N3.NAMECD2 = T1.IBEVAL_DIV2 ";
        $query .= "     LEFT JOIN NAME_MST N4 ON N4.NAMECD1 = 'Z038' ";
        $query .= "                          AND N4.NAMECD2 = T1.IBEVAL_DIV2 ";
        $query .= " WHERE ";
        $query .= "     T1.IBYEAR       = '".$model->ibyear."' AND ";
        $query .= "     T1.IBGRADE      = '".$model->ibgrade."' AND ";
        $query .= "     T1.IBPRG_COURSE = '".$model->ibprg_course."' ";
        $query .= " ORDER BY ";
        $query .= "     L1.NAMECD2, ";
        $query .= "     T1.IBPRG_COURSE, ";
        $query .= "     T1.IBCURRICULUM_CD, ";
        $query .= "     T1.IBCLASSCD, ";
        $query .= "     T1.IBSUBCLASSCD, ";
        $query .= "     T1.IBEVAL_DIV1, ";
        $query .= "     T1.IBEVAL_DIV2, ";
        $query .= "     T1.IBEVAL_MARK ";

        return $query;
    }

    //行数取得
    public function getRowDataCnt($row, $flg)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     IBVIEW_NAME_YMST ";
        $query .= " WHERE ";
        $query .= "     IBYEAR          = '".$row["IBYEAR"]."' AND ";
        $query .= "     IBGRADE         = '".$row["IBGRADE"]."' AND ";
        $query .= "     IBCLASSCD       = '".$row["IBCLASSCD"]."' AND ";
        $query .= "     IBPRG_COURSE    = '".$row["IBPRG_COURSE"]."' AND ";
        $query .= "     IBCURRICULUM_CD = '".$row["IBCURRICULUM_CD"]."' AND ";
        $query .= "     IBSUBCLASSCD    = '".$row["IBSUBCLASSCD"]."' ";
        if ($flg == "2") {
            $query .= "     AND ";
            $query .= "     IBEVAL_DIV1       = '".$row["IBEVAL_DIV1"]."' ";
        } elseif ($flg == "3") {
            $query .= "     AND ";
            $query .= "     IBEVAL_DIV1       = '".$row["IBEVAL_DIV1"]."' AND ";
            $query .= "     IBEVAL_DIV2       = '".$row["IBEVAL_DIV2"]."' ";
        }

        return $query;
    }

    //IBVIEW_NAME_YMSTから１レコード取得
    public function getIBViewNameYmst($ibyear, $ibgrade, $ibclasscd, $ibprg_course, $ibcurriculum_cd, $ibsubclasscd, $ibeval_div1, $ibeval_div2, $ibeval_mark)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     IBVIEW_NAME_YMST ";
        $query .= " WHERE ";
        $query .= "     IBYEAR          = '".$ibyear."' AND ";
        $query .= "     IBGRADE         = '".$ibgrade."' AND ";
        $query .= "     IBCLASSCD       = '".$ibclasscd."' AND ";
        $query .= "     IBPRG_COURSE    = '".$ibprg_course."' AND ";
        $query .= "     IBCURRICULUM_CD = '".$ibcurriculum_cd."' AND ";
        $query .= "     IBSUBCLASSCD    = '".$ibsubclasscd."' AND ";
        $query .= "     IBEVAL_DIV1     = '".$ibeval_div1."' AND ";
        $query .= "     IBEVAL_DIV2     = '".$ibeval_div2."' AND ";
        $query .= "     IBEVAL_MARK     = '".$ibeval_mark."' ";

        return $query;
    }

    //科目一覧取得
    public function getIBSubclasscd($model, $flg)
    {
        $query  = " SELECT ";
        if ($flg == "check") {
            $query .= "     COUNT(*) ";
        } else {
            $query .= "     IBCLASSCD || '-' || IBPRG_COURSE || '-' || IBCURRICULUM_CD || '-' || IBSUBCLASSCD AS VALUE, ";
            $query .= "     IBCLASSCD || '-' || IBPRG_COURSE || '-' || IBCURRICULUM_CD || '-' || IBSUBCLASSCD || ' ' || IBSUBCLASSNAME_ENG AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     V_IBSUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     IBYEAR          = '".$model->ibyear."' AND ";
        $query .= "     IBPRG_COURSE    = '".$model->ibprg_course."' ";
        if ($flg == "list") {
            $query .= " ORDER BY ";
            $query .= "     IBCURRICULUM_CD, ";
            $query .= "     IBCLASSCD, ";
            $query .= "     IBSUBCLASSCD ";
        }

        return $query;
    }

    //名称マスタ取得
    public function getNameMst($model, $namecd1)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->ibyear."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namecd1 == "Z035" && $model->ibprg_course == "M") {
            $query .= "     AND ";
            $query .= "     NAMECD2 = '0' ";
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    /**********/
    /* INSERT */
    /**********/
    public function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();

        //削除
        $query  = " DELETE FROM ";
        $query .= "     IBVIEW_NAME_YMST ";
        $query .= " WHERE ";
        $query .= "     IBYEAR          = '".$model->ibyear."' AND ";
        $query .= "     IBGRADE         = '".$model->ibgrade."' AND ";
        $query .= "     IBCLASSCD       = '".$model->field["IBCLASSCD"]."' AND ";
        $query .= "     IBPRG_COURSE    = '".$model->field["IBPRG_COURSE"]."' AND ";
        $query .= "     IBCURRICULUM_CD = '".$model->field["IBCURRICULUM_CD"]."' AND ";
        $query .= "     IBSUBCLASSCD    = '".$model->field["IBSUBCLASSCD"]."' AND ";
        $query .= "     IBEVAL_DIV1     = '".$model->field["IBEVAL_DIV1"]."' AND ";
        $query .= "     IBEVAL_DIV2     = '".$model->field["IBEVAL_DIV2"]."' AND ";
        $query .= "     IBEVAL_MARK     = '".$model->field["IBEVAL_MARK"]."' ";

        $db->query($query);

        //追加
        $data = array();
        $data["IBYEAR"][TEXT]           = $model->ibyear;
        $data["IBGRADE"][TEXT]          = $model->ibgrade;
        $data["IBCLASSCD"][TEXT]        = $model->field["IBCLASSCD"];
        $data["IBPRG_COURSE"][TEXT]     = $model->field["IBPRG_COURSE"];
        $data["IBCURRICULUM_CD"][TEXT]  = $model->field["IBCURRICULUM_CD"];
        $data["IBSUBCLASSCD"][TEXT]     = $model->field["IBSUBCLASSCD"];
        $data["IBEVAL_DIV1"][TEXT]      = $model->field["IBEVAL_DIV1"];
        $data["IBEVAL_DIV2"][TEXT]      = $model->field["IBEVAL_DIV2"];
        $data["IBEVAL_MARK"][TEXT]      = $model->field["IBEVAL_MARK"];
        $data["IBEVAL_NAME"][TEXT]      = $model->field["IBEVAL_NAME"];
        $data["IBEVAL_ABBV"][TEXT]      = $model->field["IBEVAL_ABBV"];
        $data["IBEVAL_NAME_ENG"][TEXT]  = $model->field["IBEVAL_NAME_ENG"];
        $data["IBEVAL_ABBV_ENG"][TEXT]  = $model->field["IBEVAL_ABBV_ENG"];
        $data["IBSORT"][NUMBER]         = $model->field["IBSORT"];
        $data["IBPERFECT"][NUMBER]      = $model->field["IBPERFECT"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $query = Query::insertSQL($data, "IBVIEW_NAME_YMST");
        $db->query($query);
        Query::dbCheckIn($db);
    }

    /**********/
    /* DELETE */
    /**********/
    public function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     IBVIEW_NAME_YMST ";
        $query .= " WHERE ";
        $query .= "     IBYEAR          = '".$model->ibyear."' AND ";
        $query .= "     IBGRADE         = '".$model->ibgrade."' AND ";
        $query .= "     IBCLASSCD       = '".$model->field["IBCLASSCD"]."' AND ";
        $query .= "     IBPRG_COURSE    = '".$model->field["IBPRG_COURSE"]."' AND ";
        $query .= "     IBCURRICULUM_CD = '".$model->field["IBCURRICULUM_CD"]."' AND ";
        $query .= "     IBSUBCLASSCD    = '".$model->field["IBSUBCLASSCD"]."' AND ";
        $query .= "     IBEVAL_DIV1     = '".$model->field["IBEVAL_DIV1"]."' AND ";
        $query .= "     IBEVAL_DIV2     = '".$model->field["IBEVAL_DIV2"]."' AND ";
        $query .= "     IBEVAL_MARK     = '".$model->field["IBEVAL_MARK"]."' ";

        $db->query($query);
        Query::dbCheckIn($db);
    }

    //IBVIEW_NAME_YMST存在チェック
    public function checkIBViewNameYmst($ibyear)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     IBVIEW_NAME_YMST ";
        $query .= " WHERE ";
        $query .= "     IBYEAR = '".$ibyear."' ";

        return $query;
    }

    //名称マスタ存在チェック
    public function checkNameMst($year, $namecd1, $namecd2)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' AND ";
        $query .= "     NAMECD2 = '".$namecd2."' ";

        return $query;
    }

    //前年度データ取得
    public function getPreYearData($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     IBVIEW_NAME_YMST T1, ";
        $query .= "     SCHREG_REGD_GDAT T2, ";
        $query .= "     V_NAME_MST T3, ";
        $query .= "     IBSUBCLASS_YDAT T4 ";
        $query .= " WHERE ";
        $query .= "     T1.IBYEAR           = '".($model->ibyear-1)."' AND ";
        $query .= "     T2.YEAR             = '".$model->ibyear."' AND ";
        $query .= "     T1.IBGRADE          = T2.GRADE AND ";
        $query .= "     T3.NAMECD1          = 'A034' AND ";
        $query .= "     T3.YEAR             = '".$model->ibyear."' AND ";
        $query .= "     T1.IBPRG_COURSE     = T3.NAME1 AND ";
        $query .= "     T4.IBYEAR           = '".$model->ibyear."' AND ";
        $query .= "     T1.IBCLASSCD        = T4.IBCLASSCD AND ";
        $query .= "     T1.IBPRG_COURSE     = T4.IBPRG_COURSE AND ";
        $query .= "     T1.IBCURRICULUM_CD  = T4.IBCURRICULUM_CD AND ";
        $query .= "     T1.IBSUBCLASSCD     = T4.IBSUBCLASSCD ";

        return $query;
    }

    /********/
    /* COPY */
    /********/
    public function &getCopyQuery($model)
    {
        $db = Query::dbCheckOut();

        //削除
        $query  = " DELETE FROM ";
        $query .= "     IBVIEW_NAME_YMST ";
        $query .= " WHERE ";
        $query .= "     IBYEAR = '".$model->ibyear."' ";
        $db->query($query);

        //前年度データ
        $query = knjz068Query::getPreYearData($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //名称マスタ存在チェック
            if ($row["IBEVAL_DIV1"] == "1") {
                $cnt = $db->getOne(knjz068Query::checkNameMst($model->ibyear, "Z037", $row["IBEVAL_DIV2"]));
            } elseif ($row["IBEVAL_DIV1"] == "2") {
                $cnt = $db->getOne(knjz068Query::checkNameMst($model->ibyear, "Z038", $row["IBEVAL_DIV2"]));
            } else {
                $cnt = $db->getOne(knjz068Query::checkNameMst($model->ibyear, "Z036", $row["IBEVAL_DIV2"]));
            }
            if ($cnt == 0) {
                continue;
            }

            //追加
            $data = array();
            $data["IBYEAR"][TEXT]           = $model->ibyear;
            $data["IBGRADE"][TEXT]          = $row["IBGRADE"];
            $data["IBCLASSCD"][TEXT]        = $row["IBCLASSCD"];
            $data["IBPRG_COURSE"][TEXT]     = $row["IBPRG_COURSE"];
            $data["IBCURRICULUM_CD"][TEXT]  = $row["IBCURRICULUM_CD"];
            $data["IBSUBCLASSCD"][TEXT]     = $row["IBSUBCLASSCD"];
            $data["IBEVAL_DIV1"][TEXT]      = $row["IBEVAL_DIV1"];
            $data["IBEVAL_DIV2"][TEXT]      = $row["IBEVAL_DIV2"];
            $data["IBEVAL_MARK"][TEXT]      = $row["IBEVAL_MARK"];
            $data["IBEVAL_NAME"][TEXT]      = $row["IBEVAL_NAME"];
            $data["IBEVAL_ABBV"][TEXT]      = $row["IBEVAL_ABBV"];
            $data["IBEVAL_NAME_ENG"][TEXT]  = $row["IBEVAL_NAME_ENG"];
            $data["IBEVAL_ABBV_ENG"][TEXT]  = $row["IBEVAL_ABBV_ENG"];
            $data["IBSORT"][NUMBER]         = $row["IBSORT"];
            $data["IBPERFECT"][NUMBER]      = $row["IBPERFECT"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            $query = Query::insertSQL($data, "IBVIEW_NAME_YMST");
            $db->query($query);
        }
        Query::dbCheckIn($db);
    }

    //左フレームのリスト
    public function getCsvQuery($model)
    {
        $query  = " SELECT ";
        $query .= "      T1.*, ";
        $query .= "      L3.GRADE_NAME1 AS GRADE_NAME, ";
        $query .= "      L2.IBSUBCLASSNAME_ENG, ";
        $query .= "      L2.IBSUBCLASSNAME, ";
        $query .= "      N1.NAME1 AS IBEVAL_DIV1_NAME, ";
        $query .= "      CASE T1.IBEVAL_DIV1 WHEN '1' THEN N3.NAME1 ";
        $query .= "                          WHEN '2' THEN N4.NAME1 ";
        $query .= "                          ELSE N2.NAME1 END AS IBEVAL_DIV2_NAME, ";
        $query .= "      '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     IBVIEW_NAME_YMST T1 ";
        $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'A034' ";
        $query .= "                          AND L1.NAME1 = T1.IBPRG_COURSE ";
        $query .= "     LEFT JOIN IBSUBCLASS_MST L2 ON L2.IBCLASSCD         = T1.IBCLASSCD ";
        $query .= "                                AND L2.IBPRG_COURSE      = T1.IBPRG_COURSE ";
        $query .= "                                AND L2.IBCURRICULUM_CD   = T1.IBCURRICULUM_CD ";
        $query .= "                                AND L2.IBSUBCLASSCD      = T1.IBSUBCLASSCD ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT L3 ON L3.YEAR    = T1.IBYEAR ";
        $query .= "                                  AND L3.GRADE   = T1.IBGRADE ";
        $query .= "     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'Z035' ";
        $query .= "                          AND N1.NAMECD2 = T1.IBEVAL_DIV1 ";
        $query .= "     LEFT JOIN NAME_MST N2 ON N2.NAMECD1 = 'Z036' ";
        $query .= "                          AND N2.NAMECD2 = T1.IBEVAL_DIV2 ";
        $query .= "     LEFT JOIN NAME_MST N3 ON N3.NAMECD1 = 'Z037' ";
        $query .= "                          AND N3.NAMECD2 = T1.IBEVAL_DIV2 ";
        $query .= "     LEFT JOIN NAME_MST N4 ON N4.NAMECD1 = 'Z038' ";
        $query .= "                          AND N4.NAMECD2 = T1.IBEVAL_DIV2 ";
        $query .= " WHERE ";
        $query .= "     T1.IBYEAR       = '".$model->ibyear."' AND ";
        $query .= "     T1.IBPRG_COURSE = '".$model->ibprg_course."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.IBGRADE, ";
        $query .= "     T1.IBPRG_COURSE, ";
        $query .= "     T1.IBCURRICULUM_CD, ";
        $query .= "     T1.IBCLASSCD, ";
        $query .= "     T1.IBSUBCLASSCD, ";
        $query .= "     T1.IBEVAL_DIV1, ";
        $query .= "     T1.IBEVAL_DIV2, ";
        $query .= "     T1.IBEVAL_MARK ";

        return $query;
    }

    //IBコース名称取得
    public function getIBPrgCourseName($model)
    {
        $query  = " SELECT ";
        $query .= "     ABBV1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->ibyear."' AND ";
        $query .= "     NAMECD1 = 'A034' AND ";
        $query .= "     NAME1   = '".$model->ibprg_course."' ";

        return $query;
    }
}
