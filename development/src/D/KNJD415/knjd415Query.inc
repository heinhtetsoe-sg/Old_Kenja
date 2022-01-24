<?php

require_once('for_php7.php');

class knjd415Query extends Query {
    //年度取得
    function getYear($model) {
        $query  = " WITH YEAR_T(VALUE, LABEL) AS ( ";
        $query .= "     VALUES('".CTRL_YEAR."', '".CTRL_YEAR."') ";
        $query .= "     UNION ";
        $query .= "     VALUES('".(CTRL_YEAR + 1)."', '".(CTRL_YEAR + 1)."') ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     YEAR_T ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //状態区分取得
    function getCondition($condition="") {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1     = 'A033' AND ";
        $query .= "     NAMESPARE3  = '1' ";
        if ($condition) {
            $query .= "     AND NAMECD2 = '".$condition."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //リスト取得（左フレーム）
    function getList($model, $condition, $groupcd) {
        $query  = " SELECT ";
        if ($groupcd) {
            $query .= "     COUNT(*) AS CNT ";
        } else {
            $query .= "     T1.YEAR, ";
            $query .= "     T1.CONDITION, ";
            $query .= "     T1.CLASSGROUP_CD, ";
            $query .= "     T2.CLASSGROUP_NAME, ";
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '　' || T4.CLASSNAME AS SUBCLASS ";
        }
        $query .= " FROM ";
        $query .= "      HREPORT_GUIDANCE_CONTENTS_CLASS_GROUP_DAT T1 ";
        $query .= "      LEFT JOIN HREPORT_GUIDANCE_CONTENTS_CLASS_GROUP_MST T2 ";
        $query .= "          ON T1.YEAR             = T2.YEAR ";
        $query .= "         AND T1.CONDITION        = T2.CONDITION ";
        $query .= "         AND T1.CLASSGROUP_CD    = T2.CLASSGROUP_CD ";
        $query .= "      LEFT JOIN V_NAME_MST T3 ";
        $query .= "          ON T1.YEAR             = T3.YEAR ";
        $query .= "         AND T1.SCHOOL_KIND      = T3.NAME1 ";
        $query .= "         AND T3.NAMECD1 = 'A023' ";
        $query .= "      LEFT JOIN CLASS_MST T4 ";
        $query .= "          ON T1.CLASSCD          = T4.CLASSCD ";
        $query .= "         AND T1.SCHOOL_KIND      = T4.SCHOOL_KIND ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR             = '".$model->year."' ";
        if ($condition) {
            $query .= " AND T1.CONDITION        = '".$condition."' ";
        }
        if ($groupcd) {
            $query .= " AND T1.CLASSGROUP_CD        = '".$groupcd."' ";
        }
        if (!$groupcd) {
            $query .= " ORDER BY ";
            $query .= "     T1.CONDITION, ";
            $query .= "     T1.CLASSGROUP_CD, ";
            $query .= "     T3.NAMECD2, ";
            $query .= "     T1.CLASSCD ";
        }
        return $query;
    }

    //GRADE_KIND_COMP_GROUP_YMST取得
    function getCompGroupYMst($model) {
        $db = Query::dbCheckOut();
        
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_CONTENTS_CLASS_GROUP_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR          = '".$model->year."' ";
        $query .= " AND CONDITION     = '".$model->condition."' ";
        $query .= " AND CLASSGROUP_CD = '".$model->groupcd."' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //対象教科一覧取得
    function getCompGroupDat($model, $setcondition, $setgroupcd, $setName = "") {
        $query  = " SELECT ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND AS VALUE, ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '　'|| T3.CLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_CONTENTS_CLASS_GROUP_DAT T1 ";
        $query .= "      LEFT JOIN V_NAME_MST T2 ";
        $query .= "          ON T1.YEAR        = T2.YEAR ";
        $query .= "         AND T1.SCHOOL_KIND = T2.NAME1 ";
        $query .= "         AND T2.NAMECD1     = 'A023' ";
        $query .= "      LEFT JOIN CLASS_MST T3 ";
        $query .= "          ON T1.CLASSCD     = T3.CLASSCD ";
        $query .= "         AND T1.SCHOOL_KIND = T3.SCHOOL_KIND ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR             = '".$model->year."' ";
        $query .= " AND T1.CONDITION        = '".$setcondition."' ";
        $query .= " AND T1.CLASSGROUP_CD    = '".$setgroupcd."' ";
        $query .= " ORDER BY ";
        $query .= "     T2.NAMECD2, ";
        $query .= "     T1.CLASSCD ";

        return $query;
    }

    //科目マスタ取得
    function getGradeKindCompSubclassMst($model, $setcondition, $setName = "", $setQuery = "") {
        $query .= " SELECT DISTINCT  ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND AS VALUE, ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '　' || T1.CLASSNAME AS LABEL, ";
        $query .= "     T2.NAMECD2, ";
        $query .= "     T1.CLASSCD ";
        $query .= " FROM ";
        $query .= "     CLASS_MST T1 ";
        $query .= "     LEFT JOIN V_NAME_MST T2 ON T1.SCHOOL_KIND = T2.NAME1 AND";
        $query .= "     T2.NAMECD1 = 'A023'";
        $query .= " ORDER BY ";
        $query .= "     T2.NAMECD2, ";
        $query .= "     T1.CLASSCD ";

        return $query;
    }

    //追加
    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $selectdata = explode(',', $model->selectdata);

        if ($model->selectdata) {
            $data["YEAR"][TEXT]               = $model->year ;
            $data["CONDITION"][TEXT]          = $model->field["CONDITION"];
            $data["CLASSGROUP_CD"][TEXT]      = $model->field["CLASSGROUP_CD"];
            $data["CLASSGROUP_NAME"][TEXT]    = $model->field["CLASSGROUP_NAME"];
            $data["REGISTERCD"][TEXT]         = STAFFCD;
            $data["UPDATED"][FUNC]            = "sysdate()";

            $query = Query::insertSQL($data, "HREPORT_GUIDANCE_CONTENTS_CLASS_GROUP_MST");
            $db->query($query);

            for ($i=0; $i < get_count($selectdata); $i++) {
                list($classcd, $school_kind) = explode("-", $selectdata[$i]);

                $data = array();
                $data["YEAR"][TEXT]               = $model->year ;
                $data["CONDITION"][TEXT]          = $model->field["CONDITION"];
                $data["CLASSGROUP_CD"][TEXT]      = $model->field["CLASSGROUP_CD"];
                $data["CLASSCD"][TEXT]            = $classcd;
                $data["SCHOOL_KIND"][TEXT]        = $school_kind;
                $data["REGISTERCD"][TEXT]         = STAFFCD;
                $data["UPDATED"][FUNC]            = "sysdate()";

                $query = Query::insertSQL($data, "HREPORT_GUIDANCE_CONTENTS_CLASS_GROUP_DAT");
                $db->query($query);

            }
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //更新
    function &UpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        /*************************************/
        /*  HREPORT_GUIDANCE_CONTENTS_CLASS_GROUP_MST  */
        /*************************************/
        //削除
        $query  = " DELETE FROM  ";
        $query .= "     HREPORT_GUIDANCE_CONTENTS_CLASS_GROUP_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR                = '".$model->year."'  ";
        $query .= " AND CONDITION           = '".$model->field["CONDITION"]."' ";
        $query .= " AND CLASSGROUP_CD       = '".$model->field["CLASSGROUP_CD"]."' ";

        $db->query($query);

        //追加
        $data["YEAR"][TEXT]               = $model->year ;
        $data["CONDITION"][TEXT]          = $model->field["CONDITION"];
        $data["CLASSGROUP_CD"][TEXT]      = $model->field["CLASSGROUP_CD"];
        $data["CLASSGROUP_NAME"][TEXT]    = $model->field["CLASSGROUP_NAME"];
        $data["REGISTERCD"][TEXT]         = STAFFCD;
        $data["UPDATED"][FUNC]            = "sysdate()";

        $query = Query::insertSQL($data, "HREPORT_GUIDANCE_CONTENTS_CLASS_GROUP_MST");
        $db->query($query);

        /*************************************/
        /*  HREPORT_GUIDANCE_CONTENTS_CLASS_GROUP_DAT  */
        /*************************************/
        //削除
        $query  = " DELETE FROM  ";
        $query .= "     HREPORT_GUIDANCE_CONTENTS_CLASS_GROUP_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR                = '".$model->year."'  ";
        $query .= " AND CONDITION           = '".$model->field["CONDITION"]."' ";
        $query .= " AND CLASSGROUP_CD       = '".$model->field["CLASSGROUP_CD"]."' ";

        $db->query($query);

        //追加
        $selectdata = explode(',', $model->selectdata);
        for ($i=0; $i < get_count($selectdata); $i++) {
            list($classcd, $school_kind) = explode("-", $selectdata[$i]);

            $data = array();
            $data["YEAR"][TEXT]               = $model->year ;
            $data["CONDITION"][TEXT]          = $model->field["CONDITION"];
            $data["CLASSGROUP_CD"][TEXT]      = $model->field["CLASSGROUP_CD"];
            $data["CLASSCD"][TEXT]            = $classcd;
            $data["SCHOOL_KIND"][TEXT]        = $school_kind;
            $data["REGISTERCD"][TEXT]         = STAFFCD;
            $data["UPDATED"][FUNC]            = "sysdate()";

            $query = Query::insertSQL($data, "HREPORT_GUIDANCE_CONTENTS_CLASS_GROUP_DAT");
            $db->query($query);

        }

        $db->commit();      // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return;
    }

    //削除
    function &DeleteQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除 HREPORT_GUIDANCE_CONTENTS_CLASS_GROUP_MST
        $query  = " DELETE FROM  ";
        $query .= "     HREPORT_GUIDANCE_CONTENTS_CLASS_GROUP_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR                = '".$model->year."'  ";
        $query .= " AND CONDITION           = '".$model->field["CONDITION"]."' ";
        $query .= " AND CLASSGROUP_CD       = '".$model->field["CLASSGROUP_CD"]."' ";

        $db->query($query);


        //削除 HREPORT_GUIDANCE_CONTENTS_CLASS_GROUP_DAT
        $query  = " DELETE FROM  ";
        $query .= "     HREPORT_GUIDANCE_CONTENTS_CLASS_GROUP_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR                = '".$model->year."'  ";
        $query .= " AND CONDITION           = '".$model->field["CONDITION"]."' ";
        $query .= " AND CLASSGROUP_CD       = '".$model->field["CLASSGROUP_CD"]."' ";

        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //存在チェック（コピー） -- HREPORT_GUIDANCE_CONTENTS_CLASS_GROUP_MST
    function checkCourseOfStudyMst($model, $year) {
        $query  = " SELECT ";
        $query .=  "    COUNT(*) ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_CONTENTS_CLASS_GROUP_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        if($model->cmd == "add" || $model->cmd == "update"){
            $query .= " AND CONDITION           = '".$model->field["CONDITION"]."' ";
            $query .= " AND CLASSGROUP_CD       = '".$model->field["CLASSGROUP_CD"]."' ";
        }

        return $query;
    }

    //存在チェック（コピー） -- HREPORT_GUIDANCE_CONTENTS_CLASS_GROUP_MST
    function checkCourseOfStudyDat($model, $year) {
        $query  = " SELECT ";
        $query .=  "    COUNT(*) ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_CONTENTS_CLASS_GROUP_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        if($model->cmd == "add" || $model->cmd == "update"){
            $query .= " AND CONDITION           = '".$model->field["CONDITION"]."' ";
            $query .= " AND CLASSGROUP_CD       = '".$model->field["CLASSGROUP_CD"]."' ";
        }

        return $query;
    }

    //前年度からコピー
    function getCopyData($year, $flg) {
        $query  = " SELECT ";
        if ($flg == "cnt") {
            $query .=  "    COUNT(*) ";
        }
        if ($flg == "mst_list") {
            $query .=  " DISTINCT T2.* ";
        }
        if ($flg == "dat_list") {
            $query .=  " DISTINCT T1.* ";
        }
        $query .= " FROM ";
        $query .= "      HREPORT_GUIDANCE_CONTENTS_CLASS_GROUP_DAT T1 ";
        $query .= "      LEFT JOIN HREPORT_GUIDANCE_CONTENTS_CLASS_GROUP_MST T2 ";
        $query .= "          ON T1.YEAR             = T2.YEAR ";
        $query .= "         AND T1.CONDITION        = T2.CONDITION ";
        $query .= "         AND T1.CLASSGROUP_CD    = T2.CLASSGROUP_CD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR             = '".$year."' ";
        return $query;
    }

    //前年度からコピー    -- HREPORT_GUIDANCE_CONTENTS_CLASS_GROUP_MST
    function &copyInsertQueryMst($model, $pre_year) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除
        $query  =  " DELETE FROM ";
        $query .=  "    HREPORT_GUIDANCE_CONTENTS_CLASS_GROUP_MST ";
        $query .=  " WHERE ";
        $query .=  "    YEAR = '".$model->year."' ";
        $db->query($query);

        //前年度データ取得
        $query = knjd415Query::getCopyData($pre_year, "mst_list");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //追加
            $data = array();
            $data["YEAR"][TEXT]                 = $model->year;
            $data["CONDITION"][TEXT]            = $row["CONDITION"];
            $data["CLASSGROUP_CD"][TEXT]        = $row["CLASSGROUP_CD"];
            $data["CLASSGROUP_NAME"][TEXT]      = $row["CLASSGROUP_NAME"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $query = Query::insertSQL($data, "HREPORT_GUIDANCE_CONTENTS_CLASS_GROUP_MST");
            $db->query($query);
        }
        $db->commit();
        Query::dbCheckIn($db);
    }

    //前年度からコピー    -- HREPORT_GUIDANCE_CONTENTS_CLASS_GROUP_DAT
    function &copyInsertQueryDat($model, $pre_year) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除
        $query  =  " DELETE FROM ";
        $query .=  "    HREPORT_GUIDANCE_CONTENTS_CLASS_GROUP_DAT ";
        $query .=  " WHERE ";
        $query .=  "    YEAR = '".$model->year."' ";
        $db->query($query);

        //前年度データ取得
        $query = knjd415Query::getCopyData($pre_year, "dat_list");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //追加
            $data = array();
            $data["YEAR"][TEXT]                 = $model->year;
            $data["CONDITION"][TEXT]            = $row["CONDITION"];
            $data["CLASSGROUP_CD"][TEXT]        = $row["CLASSGROUP_CD"];
            $data["CLASSCD"][TEXT]              = $row["CLASSCD"];
            $data["SCHOOL_KIND"][TEXT]          = $row["SCHOOL_KIND"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $query = Query::insertSQL($data, "HREPORT_GUIDANCE_CONTENTS_CLASS_GROUP_DAT");
            $db->query($query);
        }
        $db->commit();
        Query::dbCheckIn($db);
    }
}
?>
