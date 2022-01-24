<?php

require_once('for_php7.php');

class knjz062_2aQuery extends Query {

    //CLASS_MST データ取得
    function getData($model)
    {
        $query  = " SELECT ";
        $query .= "      * ";
        $query .= " FROM ";
        $query .= "      CLASS_MST ";
        $query .= " ORDER BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CLASSCD ";
        } else {
            $query .= "     CLASSCD ";
        }

        return $query;
    }

    //学校校種、教育課程コード取得
    function getNamecd($cd)
    {
        $query  = " SELECT ";
        if ($cd === 'A023') {
            $query .= "      NAME1 AS VALUE, ";
            $query .= "      NAME1 || '：' || ABBV1 AS LABEL ";
        } else {
            $query .= "      NAMECD2 AS VALUE, ";
            $query .= "      NAMECD2 || '：' || NAME1 AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "      V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "      YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "      NAMECD1 = '".$cd."' ";
        $query .= " ORDER BY ";
        $query .= "      VALUE ";

        return $query;
    }

    //１レコード取得
    function getRow($cd, $model, $school_kind, $flg)
    {
        $db = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();
        
        $query  = " SELECT ";
        $query .= "      * ";
        $query .= " FROM ";
        $query .= "      CLASS_MST ";
        $query .= " WHERE ";
        $query .= "      CLASSCD = '" .$cd ."'";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND SCHOOL_KIND     = '" .$school_kind."' ";
        }
        if (!$flg) {
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $row = $db2->getRow($query, DB_FETCHMODE_ASSOC);
        }
        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);
        
        return $row;
    }

    //年度データの確認
    function IsExisting($classcd, $model, $school_kind)
    {
        $db    = Query::dbCheckOut();
        
        $query  = " SELECT ";
        $query .= "      * ";
        $query .= " FROM ";
        $query .= "      CLASS_YDAT ";
        $query .= " WHERE ";
        $query .= "      CLASSCD = '" .$classcd ."'";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND SCHOOL_KIND     = '" .$school_kind."' ";
        }
        $row   = $db->getRow($query);
        Query::dbCheckIn($db);

        if (is_array($row)){
            return true;
        } else {
            return false;
        }        
    }

    //科目マスタの確認
    function IsExistingSubclass($classcd, $model, $school_kind)
    {
        $db    = Query::dbCheckOut();
        
        $query  = " SELECT ";
        $query .= "      * ";
        $query .= " FROM ";
        $query .= "      SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "      CLASSCD = '" .$classcd ."'";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND SCHOOL_KIND     = '" .$school_kind."' ";
        }
        $row   = $db->getRow($query);
        Query::dbCheckIn($db);

        if (is_array($row)){
            return true;
        } else {
            return false;
        }        
    }

    //教育委員会用の学校コード取得
    function getSchoolCd()
    {
        $query  = " SELECT ";
        $query .= "      KYOUIKU_IINKAI_SCHOOLCD ";
        $query .= " FROM ";
        $query .= "      V_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "      YEAR    = '".CTRL_YEAR."' ";

        return $query;
    }

    //チェック対象のテーブル名を取得
    function getTargetTable($allTables) {
        $query  = " SELECT DISTINCT ";
        $query .= "   TABLE_NAME ";
        $query .= " FROM ";
        $query .= "   SYSIBM.COLUMNS ";
        $query .= " WHERE ";
        $query .= "   TABLE_NAME IN ('".implode("','", $allTables)."') ";

        return $query;
    }

    //対象科目が特定のテーブルに参照されているかをチェック
    function checkClsCd($model, $tableName, $classcd) {
        $query  = " SELECT ";
        $query .= "   COUNT(*) ";
        $query .= " FROM ";
        $query .= "   ( ";
        $query .= "     SELECT ";
        $query .= "       CLASSCD, SCHOOL_KIND ";
        $query .= "     FROM ";
        $query .= "       {$tableName} ";
        $query .= "     GROUP BY ";
        $query .= "       CLASSCD, ";
        $query .= "       SCHOOL_KIND ";
        $query .= "   ) ";
        $query .= " WHERE ";
        $query .= "   CLASSCD || '-' || SCHOOL_KIND = '{$classcd}' ";

        return $query;
    }

    //--- INSERT
    function &getInsertQuery($fields, $model, $school_kind)
    {
        $db = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();
        
        $data["CLASSCD"][TEXT]              = $fields["CLASSCD"];
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["SCHOOL_KIND"][TEXT]          = $fields["SCHOOL_KIND"];
        } 
        $data["CLASSNAME"][TEXT]            = $fields["CLASSNAME"];
        $data["CLASSABBV"][TEXT]            = $fields["CLASSABBV"];
        $data["CLASSNAME_ENG"][TEXT]        = $fields["CLASSNAME_ENG"];
        $data["CLASSABBV_ENG"][TEXT]        = $fields["CLASSABBV_ENG"];
        $data["CLASSORDERNAME1"][TEXT]      = $fields["CLASSORDERNAME1"];
        $data["CLASSORDERNAME2"][TEXT]      = $fields["CLASSORDERNAME2"];
        $data["CLASSORDERNAME3"][TEXT]      = $fields["CLASSORDERNAME3"];
        $data["SUBCLASSES"][NUMBER]         = $fields["SUBCLASSES"];
        $data["SHOWORDER"][NUMBER]          = $fields["SHOWORDER"];
        $data["SHOWORDER2"][NUMBER]         = $fields["SHOWORDER2"];
        $data["SHOWORDER3"][NUMBER]         = $fields["SHOWORDER3"];
        $data["SHOWORDER4"][NUMBER]         = $fields["SHOWORDER4"];
        $data["ELECTDIV"][TEXT]             = ($fields["ELECTDIV"]==1) ? 1 : 0;
        $data["SPECIALDIV"][TEXT]           = $fields["SPECIALDIV"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        $query = Query::insertSQL($data, "CLASS_MST");
        $db->query($query);
        
        //学校別教育委員会教科外テーブルへのデータ追加        
        $data2["EDBOARD_SCHOOLCD"][TEXT]     = $model->schoolcd;
        $data2["CLASSCD"][TEXT]              = $fields["CLASSCD"];
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data2["SCHOOL_KIND"][TEXT]          = $fields["SCHOOL_KIND"];
        } 
        $data2["EDBOARD_FLG"][TEXT]          = "2"; //2:学校科目
        $data2["REGISTERCD"][TEXT]           = STAFFCD;
        $data2["UPDATED"][NUMBER]            = "sysdate()";

        $query = Query::insertSQL($data2, "EDBOARD_CLASS_DAT");
        $db2->query($query);
        
        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);
        return;
    }

    //--- UPDATE 
    function &getUpdateQuery($fields, $model, $school_kind)
    {
        $db = Query::dbCheckOut();

        $data["CLASSCD"][TEXT]              = $fields["CLASSCD"];
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["SCHOOL_KIND"][TEXT]          = $fields["SCHOOL_KIND"];
        } 
        $data["CLASSNAME"][TEXT]            = $fields["CLASSNAME"];
        $data["CLASSABBV"][TEXT]            = $fields["CLASSABBV"];
        $data["CLASSNAME_ENG"][TEXT]        = $fields["CLASSNAME_ENG"];
        $data["CLASSABBV_ENG"][TEXT]        = $fields["CLASSABBV_ENG"];
        $data["CLASSORDERNAME1"][TEXT]      = $fields["CLASSORDERNAME1"];
        $data["CLASSORDERNAME2"][TEXT]      = $fields["CLASSORDERNAME2"];
        $data["CLASSORDERNAME3"][TEXT]      = $fields["CLASSORDERNAME3"];
        $data["SUBCLASSES"][NUMBER]         = $fields["SUBCLASSES"];
        $data["SHOWORDER"][NUMBER]          = $fields["SHOWORDER"];
        $data["SHOWORDER2"][NUMBER]         = $fields["SHOWORDER2"];
        $data["SHOWORDER3"][NUMBER]         = $fields["SHOWORDER3"];
        $data["SHOWORDER4"][NUMBER]         = $fields["SHOWORDER4"];
        $data["ELECTDIV"][TEXT]             = ($fields["ELECTDIV"]==1) ? 1 : 0;
        $data["SPECIALDIV"][TEXT]           = $fields["SPECIALDIV"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        $where = " WHERE CLASSCD = '" .$fields["CLASSCD"]."'";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $where .= "       AND  SCHOOL_KIND   = '".$fields["SCHOOL_KIND"]."' ";
        }         
        $query = Query::updateSQL($data, "class_mst", $where);
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //--- DELETE
    function &getDeleteQuery($fields, $model, $school_kind)
    {
        $db = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();
        
        $query  = " DELETE ";
        $query .= " FROM ";
        $query .= "      CLASS_MST ";
        $query .= " WHERE ";
        $query .= "      CLASSCD = '" .$fields["CLASSCD"]."'";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND SCHOOL_KIND     = '" .$fields["SCHOOL_KIND"]."' ";
        }
        $db->query($query);
        
        $query  = " DELETE ";
        $query .= " FROM ";
        $query .= "      EDBOARD_CLASS_DAT ";
        $query .= " WHERE ";
        $query .= "      EDBOARD_SCHOOLCD = '" .$model->schoolcd."' ";
        $query .= "  AND CLASSCD  = '" .$fields["CLASSCD"]."'";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND SCHOOL_KIND     = '" .$fields["SCHOOL_KIND"]."' ";
        }
        $db2->query($query);
        
        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);
        
        return $result;
    }
}
?>
