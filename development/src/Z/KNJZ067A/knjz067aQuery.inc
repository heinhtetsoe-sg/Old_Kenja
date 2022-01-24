<?php

require_once('for_php7.php');
class knjz067aQuery extends Query
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
        $query .= "     IBSUBCLASS_REPLACE_DAT ";
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

    //左フレームのリスト
    public function getList($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     L1.ABBV1, ";
        $query .= "     L2.IBSUBCLASSABBV_ENG, ";
        $query .= "     L3.SUBCLASSNAME ";
        $query .= " FROM ";
        $query .= "     IBSUBCLASS_REPLACE_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'A034' ";
        $query .= "                          AND L1.NAME1   = T1.IBPRG_COURSE ";
        $query .= "     LEFT JOIN IBSUBCLASS_MST L2 ON L2.IBCLASSCD         = T1.IBCLASSCD ";
        $query .= "                                AND L2.IBPRG_COURSE      = T1.IBPRG_COURSE ";
        $query .= "                                AND L2.IBCURRICULUM_CD   = T1.IBCURRICULUM_CD ";
        $query .= "                                AND L2.IBSUBCLASSCD      = T1.IBSUBCLASSCD ";
        $query .= "     LEFT JOIN SUBCLASS_MST L3 ON L3.CLASSCD         = T1.CLASSCD ";
        $query .= "                              AND L3.SCHOOL_KIND     = T1.SCHOOL_KIND ";
        $query .= "                              AND L3.CURRICULUM_CD   = T1.CURRICULUM_CD ";
        $query .= "                              AND L3.SUBCLASSCD      = T1.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.IBYEAR   = '".$model->ibyear."' AND ";
        $query .= "     T1.IBGRADE  = '".$model->ibgrade."' ";
        $query .= " ORDER BY ";
        $query .= "     L1.NAMECD2, ";
        $query .= "     T1.IBPRG_COURSE, ";
        $query .= "     T1.IBCURRICULUM_CD, ";
        $query .= "     T1.IBCLASSCD, ";
        $query .= "     T1.IBSUBCLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SUBCLASSCD ";

        return $query;
    }

    //行数取得
    public function getRowDataCnt($row, $flg)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     IBSUBCLASS_REPLACE_DAT ";
        $query .= " WHERE ";
        $query .= "     IBYEAR          = '".$row["IBYEAR"]."' AND ";
        $query .= "     IBGRADE         = '".$row["IBGRADE"]."' AND ";
        $query .= "     IBPRG_COURSE    = '".$row["IBPRG_COURSE"]."' ";
        if ($flg == "2") {
            $query .= "     AND ";
            $query .= "     IBCLASSCD       = '".$row["IBCLASSCD"]."' AND ";
            $query .= "     IBCURRICULUM_CD = '".$row["IBCURRICULUM_CD"]."' AND ";
            $query .= "     IBSUBCLASSCD    = '".$row["IBSUBCLASSCD"]."' ";
        }

        return $query;
    }

    //IBSUBCLASS_REPLACE_DAT取得
    public function getIBSubclassReplaceDat($ibyear, $ibgrade, $ibclasscd, $ibprg_course, $ibcurriculum_cd, $ibsubclasscd)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     IBGRADE, ";
        $query .= "     IBCLASSCD, ";
        $query .= "     IBPRG_COURSE, ";
        $query .= "     IBCURRICULUM_CD, ";
        $query .= "     IBSUBCLASSCD ";
        $query .= " FROM ";
        $query .= "     IBSUBCLASS_REPLACE_DAT ";
        $query .= " WHERE ";
        $query .= "     IBYEAR          = '".$ibyear."' AND ";
        $query .= "     IBGRADE         = '".$ibgrade."' AND ";
        $query .= "     IBCLASSCD       = '".$ibclasscd."' AND ";
        $query .= "     IBPRG_COURSE    = '".$ibprg_course."' AND ";
        $query .= "     IBCURRICULUM_CD = '".$ibcurriculum_cd."' AND ";
        $query .= "     IBSUBCLASSCD    = '".$ibsubclasscd."' ";

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

    //IB科目一覧取得
    public function getIBSubclasscd($model, $ibprg_course, $flg)
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
        $query .= "     IBYEAR          = '".$model->ibyear."' ";
        if ($flg == "list") {
            $query .= "     AND ";
            $query .= "     IBPRG_COURSE    = '".$ibprg_course."' ";
            $query .= " ORDER BY ";
            $query .= "     IBCURRICULUM_CD, ";
            $query .= "     IBCLASSCD, ";
            $query .= "     IBSUBCLASSCD ";
        }

        return $query;
    }

    //科目一覧取得
    public function getSubclassMst($model, $Row, $flg)
    {
        $query  = " SELECT ";
        if ($flg == "check") {
            $query .= "     COUNT(*) ";
        } else {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS VALUE, ";
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD || ' ' || SUBCLASSNAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->ibyear."' ";
        if ($flg == "select") {
            $query .= "     AND ";
            $query .= "     EXISTS (SELECT ";
            $query .= "                 'X' ";
            $query .= "             FROM ";
            $query .= "                 IBSUBCLASS_REPLACE_DAT T2 ";
            $query .= "             WHERE ";
            $query .= "                 T2.IBYEAR           = T1.YEAR AND ";
            $query .= "                 T2.IBGRADE          = '".$model->ibgrade."' AND ";
            $query .= "                 T2.IBCLASSCD        = '".$Row["IBCLASSCD"]."' AND ";
            $query .= "                 T2.IBPRG_COURSE     = '".$Row["IBPRG_COURSE"]."' AND ";
            $query .= "                 T2.IBCURRICULUM_CD  = '".$Row["IBCURRICULUM_CD"]."' AND ";
            $query .= "                 T2.IBSUBCLASSCD     = '".$Row["IBSUBCLASSCD"]."' AND ";
            $query .= "                 T2.CLASSCD          = T1.CLASSCD AND ";
            $query .= "                 T2.SCHOOL_KIND      = T1.SCHOOL_KIND AND ";
            $query .= "                 T2.CURRICULUM_CD    = T1.CURRICULUM_CD AND ";
            $query .= "                 T2.SUBCLASSCD       = T1.SUBCLASSCD) ";
            $query .= " ORDER BY ";
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CURRICULUM_CD, ";
            $query .= "     CLASSCD, ";
            $query .= "     SUBCLASSCD ";
        }

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
        $query .= "     IBSUBCLASS_REPLACE_DAT ";
        $query .= " WHERE ";
        $query .= "     IBYEAR          = '".$model->ibyear."' AND ";
        $query .= "     IBGRADE         = '".$model->ibgrade."' AND ";
        $query .= "     IBCLASSCD       = '".$model->field["IBCLASSCD"]."' AND ";
        $query .= "     IBPRG_COURSE    = '".$model->field["IBPRG_COURSE"]."' AND ";
        $query .= "     IBCURRICULUM_CD = '".$model->field["IBCURRICULUM_CD"]."' AND ";
        $query .= "     IBSUBCLASSCD    = '".$model->field["IBSUBCLASSCD"]."' ";

        $db->query($query);

        if (implode('', $model->selectdata)) {
            foreach ($model->selectdata as $key => $val) {
                list($classcd, $school_kind, $curriculum_cd, $subclasscd) = explode('-', $val);

                //追加
                $data = array();
                $data["IBYEAR"][TEXT]           = $model->ibyear;
                $data["IBGRADE"][TEXT]          = $model->ibgrade;
                $data["IBCLASSCD"][TEXT]        = $model->field["IBCLASSCD"];
                $data["IBPRG_COURSE"][TEXT]     = $model->field["IBPRG_COURSE"];
                $data["IBCURRICULUM_CD"][TEXT]  = $model->field["IBCURRICULUM_CD"];
                $data["IBSUBCLASSCD"][TEXT]     = $model->field["IBSUBCLASSCD"];
                $data["CLASSCD"][TEXT]          = $classcd;
                $data["SCHOOL_KIND"][TEXT]      = $school_kind;
                $data["CURRICULUM_CD"][TEXT]    = $curriculum_cd;
                $data["SUBCLASSCD"][TEXT]       = $subclasscd;
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "sysdate()";

                $query = Query::insertSQL($data, "IBSUBCLASS_REPLACE_DAT");
                $db->query($query);
            }
        }
        Query::dbCheckIn($db);
    }

    //IBSUBCLASS_REPLACE_DAT存在チェック
    public function checkIBSubclassReplaceDat($ibyear)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     IBSUBCLASS_REPLACE_DAT ";
        $query .= " WHERE ";
        $query .= "     IBYEAR = '".$ibyear."' ";

        return $query;
    }

    //前年度データ取得
    public function getPreYearData($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     IBSUBCLASS_REPLACE_DAT T1, ";
        $query .= "     SCHREG_REGD_GDAT T2, ";
        $query .= "     V_NAME_MST T3, ";
        $query .= "     IBSUBCLASS_YDAT T4, ";
        $query .= "     SUBCLASS_YDAT T5 ";
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
        $query .= "     T1.IBSUBCLASSCD     = T4.IBSUBCLASSCD AND ";
        $query .= "     T1.CLASSCD          = T5.CLASSCD AND ";
        $query .= "     T1.SCHOOL_KIND      = T5.SCHOOL_KIND AND ";
        $query .= "     T1.CURRICULUM_CD    = T5.CURRICULUM_CD AND ";
        $query .= "     T1.SUBCLASSCD       = T5.SUBCLASSCD ";

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
        $query .= "     IBSUBCLASS_REPLACE_DAT ";
        $query .= " WHERE ";
        $query .= "     IBYEAR = '".$model->ibyear."' ";
        $db->query($query);

        //前年度データ
        $query = knjz067aQuery::getPreYearData($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //追加
            $data = array();
            $data["IBYEAR"][TEXT]           = $model->ibyear;
            $data["IBGRADE"][TEXT]          = $row["IBGRADE"];
            $data["IBCLASSCD"][TEXT]        = $row["IBCLASSCD"];
            $data["IBPRG_COURSE"][TEXT]     = $row["IBPRG_COURSE"];
            $data["IBCURRICULUM_CD"][TEXT]  = $row["IBCURRICULUM_CD"];
            $data["IBSUBCLASSCD"][TEXT]     = $row["IBSUBCLASSCD"];
            $data["CLASSCD"][TEXT]          = $row["CLASSCD"];
            $data["SCHOOL_KIND"][TEXT]      = $row["SCHOOL_KIND"];
            $data["CURRICULUM_CD"][TEXT]    = $row["CURRICULUM_CD"];
            $data["SUBCLASSCD"][TEXT]       = $row["SUBCLASSCD"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            $query = Query::insertSQL($data, "IBSUBCLASS_REPLACE_DAT");
            $db->query($query);
        }
        Query::dbCheckIn($db);
    }
    
    //CSV出力
    //CSV 書き出し
    public function getCsvData($model)
    {
        $query  = " SELECT DISTINCT";
        $query .= "     N1.ABBV1, ";
        $query .= "     T1.IBGRADE, ";
        $query .= "     T1.IBCLASSCD, ";
        $query .= "     T1.IBPRG_COURSE, ";
        $query .= "     L1.IBCLASSNAME_ENG, ";
        $query .= "     T1.IBCLASSCD AS SUB_IBCLASSCD, ";
        $query .= "     T1.IBPRG_COURSE AS SUB_IBPRG_COURSE, ";
        $query .= "     T1.IBCURRICULUM_CD, ";
        $query .= "     T1.IBSUBCLASSCD, ";
        $query .= "     L2.IBSUBCLASSNAME_ENG, ";
        $query .= "     L3.NEED_TIME, ";
        $query .= "     G1.CLASSCD, ";
        $query .= "     G1.SCHOOL_KIND, ";
        $query .= "     G1.CLASSNAME, ";
        $query .= "     T1.CLASSCD AS SUB_CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND AS SUB_SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     G2.SUBCLASSNAME, ";
        $query .= "     G3.CHAIRCD, ";
        $query .= "     G3.CHAIRNAME, ";
        $query .= "     G4.TIME_UNIT, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     IBSUBCLASS_REPLACE_DAT T1  ";
        $query .= " LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'A034'  ";
        $query .= "                      AND N1.NAME1 = T1.IBPRG_COURSE ";
        $query .= " LEFT JOIN IBCLASS_MST L1 ON L1.IBCLASSCD = T1.IBCLASSCD  ";
        $query .= "                         AND L1.IBPRG_COURSE = T1.IBPRG_COURSE ";
        $query .= " LEFT JOIN IBSUBCLASS_MST L2 ON L2.IBCLASSCD = T1.IBCLASSCD  ";
        $query .= "                            AND L2.IBPRG_COURSE = T1.IBPRG_COURSE ";
        $query .= "                            AND L2.IBCURRICULUM_CD = T1.IBCURRICULUM_CD  ";
        $query .= "                            AND L2.IBSUBCLASSCD = T1.IBSUBCLASSCD ";
        $query .= " LEFT JOIN IBCREDIT_MST L3 ON L3.YEAR = T1.IBYEAR ";
        $query .= "                          AND L3.GRADE = T1.IBGRADE ";
        $query .= "                          AND L3.IBCLASSCD = T1.IBCLASSCD ";
        $query .= "                          AND L3.IBPRG_COURSE = T1.IBPRG_COURSE ";
        $query .= "                          AND L3.IBCURRICULUM_CD = T1.IBCURRICULUM_CD  ";
        $query .= "                          AND L3.IBSUBCLASSCD = T1.IBSUBCLASSCD ";
        $query .= " LEFT JOIN CLASS_MST G1    ON G1.CLASSCD = T1.CLASSCD  ";
        $query .= "                          AND G1.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= " LEFT JOIN SUBCLASS_MST G2 ON G2.CLASSCD = T1.CLASSCD  ";
        $query .= "                          AND G2.SCHOOL_KIND = T1.SCHOOL_KIND  ";
        $query .= "                          AND G2.CURRICULUM_CD = T1.CURRICULUM_CD  ";
        $query .= "                          AND G2.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= " LEFT JOIN CHAIR_DAT G3    ON G3.YEAR = T1.IBYEAR ";
        $query .= "                          AND G3.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "                          AND G3.CLASSCD = T1.CLASSCD  ";
        $query .= "                          AND G3.SCHOOL_KIND = T1.SCHOOL_KIND  ";
        $query .= "                          AND G3.CURRICULUM_CD = T1.CURRICULUM_CD  ";
        $query .= "                          AND G3.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= " LEFT JOIN CREDIT_MST G4   ON G4.YEAR = T1.IBYEAR ";
        $query .= "                          AND G4.GRADE = T1.IBGRADE ";
        $query .= "                          AND G4.CLASSCD = T1.CLASSCD  ";
        $query .= "                          AND G4.SCHOOL_KIND = T1.SCHOOL_KIND  ";
        $query .= "                          AND G4.CURRICULUM_CD = T1.CURRICULUM_CD  ";
        $query .= "                          AND G4.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.IBYEAR = '{$model->ibyear}' ";
        $query .= " AND T1.IBPRG_COURSE = '{$model->field["IBPRG_COURSE"]}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.IBGRADE, ";
        $query .= "     T1.IBPRG_COURSE, ";
        $query .= "     T1.IBCURRICULUM_CD, ";
        $query .= "     T1.IBCLASSCD, ";
        $query .= "     T1.IBSUBCLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SUBCLASSCD ";

        return $query;
    }
    
    //エラーデータの削除
    public function deleteQueryErr()
    {
        $query  = " DELETE FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID='KNJZ067A'";

        return $query;
    }
    
    //エラー出力
    public function getErrorCsvData()
    {
        $query  = "SELECT MSGROW,MSGREMARK FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID='KNJZ067A'";

        return $query;
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1["PROGRAMID"][TEXT] = 'KNJZ067A';
        $data1["MSGROW"][NUMBER]  = $record_no;
        $data1["MSGREMARK"][TEXT] = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $result = $db->query($query);
    }
}
