<?php

require_once('for_php7.php');

class knjl_event_searchQuery extends Query
{

    //校種
    public function getA023()
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        $query .= "     AND NAME1 IN ('J', 'H') ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //左リスト
    public function getStudents($model)
    {

        $tableName = "RECRUIT_DAT";

        $where = knjl_event_searchQuery::getStudentSearch($model);

        $query = " SELECT DISTINCT ";
        $query .= "   RECRUIT.RECRUIT_NO, ";
        $query .= "   RECRUIT.NAME, ";
        $query .= "   RECRUIT.YEAR ";
        $query .= " FROM ";
        $query .= "   RECRUIT_DAT RECRUIT ";
        $query .= "   INNER JOIN ( ";
        $query .= " SELECT ";
        $query .= "   RECRUIT.RECRUIT_NO, ";
        $query .= "   RECRUIT.NAME, ";
        $query .= "   MAX(RECRUIT.YEAR) AS YEAR ";
        $query .= " FROM ";
        $query .= "   RECRUIT_DAT RECRUIT ";
        $query .= "   LEFT JOIN RECRUIT_EVENT_DAT EVENT ON RECRUIT.YEAR = EVENT.YEAR ";
        $query .= "        AND RECRUIT.RECRUIT_NO = EVENT.RECRUIT_NO ";
        $query .= "   LEFT JOIN PRISCHOOL_CLASS_MST PRICLASS ON RECRUIT.PRISCHOOL_CLASS_CD = PRICLASS.PRISCHOOL_CLASS_CD ";
        $query .= " WHERE ".$where;
        $query .= " GROUP BY ";
        $query .= "   RECRUIT.RECRUIT_NO, ";
        $query .= "   RECRUIT.NAME ";
        $query .= " ) RECRUIT_MAX ON RECRUIT_MAX.RECRUIT_NO = RECRUIT.RECRUIT_NO ";
        $query .= "       AND RECRUIT_MAX.NAME = RECRUIT.NAME ";
        $query .= "       AND RECRUIT_MAX.YEAR = RECRUIT.YEAR ";
        $query .= " ORDER BY ";
        $query .= "   RECRUIT.YEAR DESC, ";
        $query .= "   RECRUIT.RECRUIT_NO ";

        return $query;
    }

    public function getStudentSearch($model)
    {
        //校種
        if ($model->search["SCHOOL_KIND"]) {
            $setKind = $model->search["SCHOOL_KIND"] == "1" ? "J" : "H";
            $where .= "   ".$and."RECRUIT.SCHOOL_KIND = '".$setKind."' ";
            $and = "AND ";
        }

        //管理番号
        if ($model->search["RECRUIT_NO"]) {
            $where .= "   ".$and."RECRUIT.RECRUIT_NO = '".$model->search["RECRUIT_NO"]."' ";
            $and = "AND ";
        }

        //氏名
        if ($model->search["NAME"]) {
            $where .= "   ".$and."RECRUIT.NAME LIKE '%" .str_replace("'", "''", $model->search["NAME"]). "%' ";
            $and = "AND ";
        }

        //氏名かな
        if ($model->search["NAME_KANA"]) {
            $where .= "   ".$and."RECRUIT.NAME_KANA LIKE '%" .str_replace("'", "''", $model->search["NAME_KANA"]). "%' ";
            $and = "AND ";
        }

        //分類
        if ($model->search["EVENT_CLASS_CD"]) {
            $where .= "   ".$and."EVENT.EVENT_CLASS_CD = '".$model->search["EVENT_CLASS_CD"]."' ";
            $where .= "   AND EVENT.YEAR IN ('".CTRL_YEAR."', '".(CTRL_YEAR + 1)."') ";
            $and = "AND ";
        }

        //イベント
        if ($model->search["EVENT_CD"]) {
            list ($year, $event_cd) = explode('-', $model->search["EVENT_CD"]);
            $where .= "   ".$and."EVENT.EVENT_CD = '".$event_cd."' ";
            $where .= "   AND EVENT.YEAR = '".$year."' ";
            $and = "AND ";
        }

        //媒体
        if ($model->search["MEDIA_CD"]) {
            $where .= "   ".$and."EVENT.MEDIA_CD = '".$model->search["MEDIA_CD"]."' ";
            $and = "AND ";
        }

        //出身学校
        if ($model->search["FINSCHOOLCD"]) {
            $where .= "   ".$and."RECRUIT.FINSCHOOLCD = '".$model->search["FINSCHOOLCD"]."' ";
            $and = "AND ";
        }

        //学年
        if ($model->search["GRADE"]) {
            $where .= "   ".$and."RECRUIT.GRADE = '".$model->search["GRADE"]."' ";
            $and = "AND ";
        }

        //塾
        if ($model->search["PRISCHOOLCD"]) {
            $where .= "   ".$and."RECRUIT.PRISCHOOLCD = '".$model->search["PRISCHOOLCD"]."' ";
            $and = "AND ";
        }

        //教室
        if ($model->search["PRISCHOOL_CLASS_CD"]) {
            $where .= "   ".$and."PRICLASS.PRISCHOOL_CLASS_CD = '".$model->search["PRISCHOOL_CLASS_CD"]."' ";
            $and = "AND ";
        }

        return $where;
    }

    //分類取得
    public function getRecruitClass()
    {
        $query  = " SELECT ";
        $query .= "     EVENT_CLASS_CD || ':' || EVENT_CLASS_NAME LABEL, ";
        $query .= "     EVENT_CLASS_CD VALUE ";
        $query .= " FROM ";
        $query .= "     RECRUIT_CLASS_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //イベント
    public function getEventMst($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR || '-' || EVENT_CD || ':' || EVENT_NAME AS LABEL, ";
        $query .= "     YEAR || '-' || EVENT_CD AS VALUE, ";
        $query .= "     YEAR, ";
        $query .= "     EVENT_CD ";
        $query .= " FROM ";
        $query .= "     RECRUIT_EVENT_YMST ";
        $query .= " WHERE ";
        $query .= "     YEAR IN ('".CTRL_YEAR."', '".(CTRL_YEAR + 1)."') ";
        $query .= "     AND EVENT_CLASS_CD = '{$model->search["EVENT_CLASS_CD"]}' ";
        if ($model->search["SCHOOL_KIND"] == "1") {
            $query .= "     AND INT(EVENT_CD) < 500 ";
        } else {
            $query .= "     AND INT(EVENT_CD) >= 500 ";
        }
        $query .= " ORDER BY ";
        $query .= "     YEAR DESC, ";
        $query .= "     EVENT_CD ";

        return $query;
    }

    //名称マスタ
    public function getNameMst($namecd1)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".(CTRL_YEAR + 1)."' AND ";
        $query .= "     NAMECD1 = '{$namecd1}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //出身学校
    public function getFinschoolCd($model)
    {
        $setKind = $model->search["SCHOOL_KIND"] == "1" ? "J" : "H";

        $query  = " SELECT DISTINCT ";
        $query .= "     T1.FINSCHOOLCD || ':' || VALUE(L2.NAME1,'') || L1.FINSCHOOL_NAME AS LABEL, ";
        $query .= "     T1.FINSCHOOLCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     RECRUIT_DAT T1 ";
        $query .= "     INNER JOIN FINSCHOOL_MST L1 ON T1.FINSCHOOLCD = L1.FINSCHOOLCD ";
        $query .= "     LEFT JOIN NAME_MST L2 ON L2.NAMECD1 = 'L001' AND L1.FINSCHOOL_DISTCD = L2.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOL_KIND = '".$setKind."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年取得
    public function getGrade($model)
    {
        $query  = " WITH GRADE_T (LABEL, VALUE) AS ( ";
        if ($model->search["SCHOOL_KIND"] == "1") {
            $query .= "     VALUES('小１', '01') ";
            $query .= "     UNION ";
            $query .= "     VALUES('小２', '02') ";
            $query .= "     UNION ";
            $query .= "     VALUES('小３', '03') ";
            $query .= "     UNION ";
            $query .= "     VALUES('小４', '04') ";
            $query .= "     UNION ";
            $query .= "     VALUES('小５', '05') ";
            $query .= "     UNION ";
            $query .= "     VALUES('小６', '06') ";
        } else {
            $query .= "     VALUES('中１', '07') ";
            $query .= "     UNION ";
            $query .= "     VALUES('中２', '08') ";
            $query .= "     UNION ";
            $query .= "     VALUES('中３', '09') ";
            $query .= "     UNION ";
            $query .= "     VALUES('高１', '10') ";
            $query .= "     UNION ";
            $query .= "     VALUES('高２', '11') ";
            $query .= "     UNION ";
            $query .= "     VALUES('高３', '12') ";
        }
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     GRADE_T ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //塾
    public function getPrischoolCd($model)
    {
        $setKind = $model->search["SCHOOL_KIND"] == "1" ? "J" : "H";
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.PRISCHOOLCD || ':' || L1.PRISCHOOL_NAME AS LABEL, ";
        $query .= "     T1.PRISCHOOLCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     RECRUIT_DAT T1 ";
        $query .= "     INNER JOIN PRISCHOOL_MST L1 ON T1.PRISCHOOLCD = L1.PRISCHOOLCD ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOL_KIND = '{$setKind}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //教室
    public function getPrischoolClassMst($model)
    {
        $setKind = $model->search["SCHOOL_KIND"] == "1" ? "J" : "H";
        $query  = " SELECT DISTINCT ";
        $query .= "     RECRUIT.PRISCHOOLCD, ";
        $query .= "     PRICLASS.PRISCHOOL_CLASS_CD, ";
        $query .= "     PRI.PRISCHOOL_NAME || '：' || PRICLASS.PRISCHOOL_NAME AS LABEL, ";
        $query .= "     PRICLASS.PRISCHOOL_CLASS_CD AS VALUE ";
        $query .= " FROM ";
        $query .= "     RECRUIT_DAT RECRUIT ";
        $query .= "     INNER JOIN PRISCHOOL_MST PRI ON RECRUIT.PRISCHOOLCD = PRI.PRISCHOOLCD ";
        $query .= "     INNER JOIN PRISCHOOL_CLASS_MST PRICLASS ON PRI.PRISCHOOLCD = PRICLASS.PRISCHOOLCD ";
        $query .= "           AND RECRUIT.PRISCHOOL_CLASS_CD = PRICLASS.PRISCHOOL_CLASS_CD ";
        $query .= " WHERE ";
        $query .= "     RECRUIT.SCHOOL_KIND = '{$setKind}' ";
        $query .= "     AND RECRUIT.PRISCHOOLCD = '{$model->search["PRISCHOOLCD"]}' ";
        $query .= " ORDER BY ";
        $query .= "     RECRUIT.PRISCHOOLCD, ";
        $query .= "     PRICLASS.PRISCHOOL_CLASS_CD ";

        return $query;
    }
}
?>
