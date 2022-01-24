<?php

require_once('for_php7.php');

class knjwfin_searchQuery extends Query
{
    //校種コンボ
    public function getFinschoolType()
    {
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.FINSCHOOL_TYPE || ' ' || NAME1 AS LABEL, ";
        $query .= "     T1.FINSCHOOL_TYPE AS VALUE ";
        $query .= " FROM ";
        $query .= "     FINSCHOOL_MST T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     NAME_MST L1 ON  L1.NAMECD1 = 'L019' ";
        $query .= "                 AND L1.NAMECD2 = T1.FINSCHOOL_TYPE ";
        $query .= " WHERE ";
        $query .= "     T1.FINSCHOOL_TYPE IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     T1.FINSCHOOL_TYPE ";

        return $query;
    }

    //学校種別コンボ
    public function getPref()
    {
        $query .= " SELECT DISTINCT ";
        $query .= "     PREF_CD || ' ' || PREF_NAME AS LABEL, ";
        $query .= "     PREF_CD AS VALUE ";
        $query .= " FROM ";
        $query .= "     PREF_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学校種別コンボ
    public function getNameMst($namecd1)
    {
        $query .= " SELECT DISTINCT ";
        $query .= "     NAMECD2 || ' ' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //検索結果
    public function getSchoolList($model)
    {
        $query .= " SELECT ";
        $query .= "     FINSCHOOLCD || ' | ' || ";
        $query .= "     VALUE(L1.NAME1, '　') || ' | ' || ";
        $query .= "     VALUE(FINSCHOOL_NAME, '　') || ' | ' || ";
        $query .= "     VALUE(FINSCHOOL_ADDR1, '　') || ' | ' || ";
        $query .= "     VALUE(FINSCHOOL_ADDR2, '　') || ' | ' || ";
        $query .= "     VALUE(L2.NAME1, '　') || ' | ' || ";
        $query .= "     VALUE(L3.NAME1, '　') || ' | ' || ";
        $query .= "     VALUE(T1.FINSCHOOL_ZIPCD, '　') || ' | ' || ";
        $query .= "     VALUE(T1.FINSCHOOL_TELNO, '　') || ' | ' || ";
        $query .= "     VALUE(L015.NAME1, '　') AS LABEL ";
        $query .= " FROM ";
        $query .= "     FINSCHOOL_MST T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     NAME_MST L1 ON  L1.NAMECD1 = 'L019' ";
        $query .= "                 AND L1.NAMECD2 = T1.FINSCHOOL_TYPE ";
        $query .= " LEFT JOIN ";
        $query .= "     NAME_MST L2 ON  L2.NAMECD1 = 'Z003' ";
        $query .= "                 AND L2.NAMECD2 = T1.DISTRICTCD ";
        $query .= " LEFT JOIN ";
        $query .= "     NAME_MST L3 ON  L3.NAMECD1 = 'L001' ";
        $query .= "                 AND L3.NAMECD2 = T1.FINSCHOOL_DISTCD ";
        $query .= " LEFT JOIN ";
        $query .= "     NAME_MST L015 ON  L015.NAMECD1 = 'L015' ";
        $query .= "                 AND L015.NAMECD2 = T1.FINSCHOOL_DIV ";
        $query .= " WHERE ";
        $query .= "         FINSCHOOLCD IS NOT NULL ";
        if ($model->field["FINSCHOOL_TYPE"] != '') {
            $query .= "     AND FINSCHOOL_TYPE = '{$model->field["FINSCHOOL_TYPE"]}' ";
        }
        if ($model->field["FINSCHOOL_NAME"] != '') {
            $query .= "     AND FINSCHOOL_NAME LIKE '%{$model->field["FINSCHOOL_NAME"]}%' ";
        }
        if ($model->field["FINSCHOOL_KANA"] != '') {
            $query .= "     AND FINSCHOOL_KANA LIKE '%{$model->field["FINSCHOOL_KANA"]}%' ";
        }
        if ($model->field["PREF_CD"] != '') {
            $query .= "     AND FINSCHOOL_PREF_CD = '{$model->field["PREF_CD"]}' ";
        }
        if ($model->field["FINSCHOOL_DISTCD"] != '') {
            $query .= "     AND FINSCHOOL_DISTCD = '{$model->field["FINSCHOOL_DISTCD"]}' ";
        }
        if ($model->field["FINSCHOOL_DIV"] != '') {
            $query .= "     AND FINSCHOOL_DIV = '{$model->field["FINSCHOOL_DIV"]}' ";
        }

        $query .= " ORDER BY ";
        $query .= "     FINSCHOOL_TYPE, ";
        $query .= "     FINSCHOOLCD ";
        
        return $query;
    }
}
