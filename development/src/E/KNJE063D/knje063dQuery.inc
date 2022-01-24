<?php
class knje063dQuery extends Query
{
    //教科マスタ取得
    public function getClassMst($classcd)
    {
        $where = " WHERE ";

        $query  = " SELECT ";
        $query .= "     CLASSCD || '-' || SCHOOL_KIND AS VALUE, ";
        $query .= "     CLASSCD || '-' || SCHOOL_KIND || '：' || CLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CLASS_MST ";
        if ($classcd) {
            $query .= $where;
            $query .= "     CLASSCD || '-' || SCHOOL_KIND = '".$classcd."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年度取得
    public function &selectQueryYear($model, $schoolKind)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR ";
        $query .= " FROM ";
        $query .= "     SCHREG_STUDYREC_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD = '0' ";
        $query .= " AND SCHREGNO = '".$model->schregno."' ";
        $query .= " AND SCHOOL_KIND = '".$schoolKind."' ";

        return $query;
    }

    public function getSchoolKind($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T2.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR ";
        $query .= "         AND T2.GRADE = T1.GRADE ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
        $query .= " AND T1.YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //科目マスタ取得
    public function getSubclassMst($year, $classcd)
    {
        $query  = " SELECT ";
        $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD || '　' || SUBCLASSNAME AS LABEL, ";
        $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND CLASSCD || '-' || SCHOOL_KIND = '".$classcd."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //指導要録文言評定データ取得
    public function selectQuery($model, $year)
    {
        $query  = " WITH SCH_INFO AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.* ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1, ";
        $query .= "         (SELECT ";
        $query .= "             SCHREGNO, ";
        $query .= "             YEAR, ";
        $query .= "             MAX(SEMESTER) AS SEMESTER ";
        $query .= "         FROM ";
        $query .= "             SCHREG_REGD_DAT ";
        $query .= "         GROUP BY ";
        $query .= "             SCHREGNO, ";
        $query .= "             YEAR ";
        $query .= "         ) T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "         T1.YEAR     = T2.YEAR AND ";
        $query .= "         T1.SEMESTER = T2.SEMESTER ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     L1.GRADE, ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND AS CLASSCD, ";
        $query .= "     T2.CLASSNAME, ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
        $query .= "     T1.REMARK1 ";
        $query .= " FROM ";
        $query .= "     SCHREG_STUDYREC_DETAIL_DAT T1 ";
        $query .= "     INNER JOIN CLASS_MST T2 ON T1.CLASSCD       = T2.CLASSCD ";
        $query .= "                            AND T1.SCHOOL_KIND   = T2.SCHOOL_KIND ";
        $query .= "     INNER JOIN SUBCLASS_MST T3 ON T1.SUBCLASSCD     = T3.SUBCLASSCD ";
        $query .= "                               AND T1.CLASSCD        = T3.CLASSCD ";
        $query .= "                               AND T1.SCHOOL_KIND    = T3.SCHOOL_KIND ";
        $query .= "                               AND T1.CURRICULUM_CD  = T3.CURRICULUM_CD ";
        $query .= "     LEFT JOIN SCH_INFO L1 ON T1.YEAR        = L1.YEAR ";
        $query .= "                          AND T1.SCHREGNO    = L1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '" .$model->schregno ."' AND ";
        $query .= "     T1.SEQ      = '001' AND ";
        $query .= "     T1.SCHOOLCD = '0' ";
        $query .= "     AND ";
        $query .= "     T1.YEAR     = '".$year."' ";

        return $query;
    }

    public function update($db, $model)
    {
        foreach ($model->classcds as $classcd) {
            $subclasscd = $model->field["SUBCLASS_{$classcd}"];
            $remark = $model->field["REMARK_{$subclasscd}"];

            $query = knje063dQuery::getDeleteQuery($model, $classcd, "");
            $db->query($query);
            $query = knje063dQuery::getInsertQuery($model, $subclasscd, $remark);
            $db->query($query);
        }
    }

    public function deleteYear($db, $model)
    {
        $query = knje063dQuery::getDeleteQuery($model, "", "ALL");
        $db->query($query);
    }

    // INSERT
    public function &getInsertQuery($model, $subclasscd, $remark)
    {
        list ($classcd, $school_kind, $curriculum_cd, $subclasscd) = explode('-', $subclasscd);

        $data = array();
        $data["SCHOOLCD"][TEXT]         = '0';
        $data["YEAR"][TEXT]             = $model->year;
        $data["SCHREGNO"][TEXT]         = $model->schregno;
        $data["CLASSCD"][TEXT]          = $classcd;
        $data["SCHOOL_KIND"][TEXT]      = $school_kind;
        $data["CURRICULUM_CD"][TEXT]    = $curriculum_cd;
        $data["SUBCLASSCD"][TEXT]       = $subclasscd;
        $data["SEQ"][TEXT]              = '001';
        $data["REMARK1"][TEXT]          = $remark;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $query = Query::insertSQL($data, "SCHREG_STUDYREC_DETAIL_DAT");

        return $query;
    }

    // DELETE
    public function &getDeleteQuery($model, $classcd, $subclasscd)
    {
        $query  = " DELETE FROM ";
        $query .= "     SCHREG_STUDYREC_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "   SCHOOLCD = '0' AND ";
        $query .= "   SCHREGNO = '".$model->schregno."' AND ";
        $query .= "   YEAR = '".$model->year."' AND ";
        if ($classcd != '') {
            $query .= "   CLASSCD || '-' || SCHOOL_KIND = '".$classcd."' AND ";
        } elseif ($subclasscd != "ALL") {
            $query .= "   CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$subclasscd."' AND ";
        }
        $query .= "   SEQ = '001'  ";

        return $query;
    }
}
