<?php

require_once('for_php7.php');

class knjz060_2Query extends Query
{

    //CLASS_MST データ取得
    public function getData($model)
    {
        $query  = " SELECT ";
        $query .= "      * ";
        $query .= " FROM ";
        $query .= "      CLASS_MST ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->schkind != "99") {
                $query .= " WHERE SCHOOL_KIND = '".$model->schkind."' ";
            } else {
                if ($model->selectSchoolKind) {
                    $query .= " WHERE SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                }
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " WHERE SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
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
    public function getNamecd($model, $cd)
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
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND NAME1 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "      VALUE ";

        return $query;
    }

    //名称マスタコード取得
    public function getNamecdTwo($model, $cd)
    {
        $query  = " SELECT ";
        $query .= "      NAMECD2 AS VALUE, ";
        $query .= "      NAMECD2 || '：' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "      NAME_MST ";
        $query .= " WHERE ";
        $query .= "      NAMECD1 = '".$cd."' ";
        $query .= " ORDER BY ";
        $query .= "      VALUE ";

        return $query;
    }


    //１レコード取得
    public function getRow($cd, $model, $school_kind)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "      T1.* ";
        if ($model->Properties["useSpecial_Support_Hrclass"] == '1') {
            $query .= "      ,L1.CLASS_REMARK1 AS SEQ001 ";
            $query .= "      ,L1.CLASS_REMARK2 AS SEQ001_REMARK2 ";
        }
        $query .= " FROM ";
        $query .= "      CLASS_MST T1 ";
        if ($model->Properties["useSpecial_Support_Hrclass"] == '1') {
            $query .= "      LEFT JOIN CLASS_DETAIL_MST L1 ON T1.CLASSCD = L1.CLASSCD ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "           AND T1.SCHOOL_KIND = L1.SCHOOL_KIND ";
            }
            $query .= "           AND L1.CLASS_SEQ = '001' ";
        }
        $query .= " WHERE ";
        $query .= "      T1.CLASSCD = '" .$cd ."'";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1' || $model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND T1.SCHOOL_KIND     = '" .$school_kind."' ";
        }
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //年度データの確認
    public function IsExisting($classcd, $model, $school_kind)
    {
        $db    = Query::dbCheckOut();
        
        $query  = " SELECT ";
        $query .= "      * ";
        $query .= " FROM ";
        $query .= "      CLASS_YDAT ";
        $query .= " WHERE ";
        $query .= "      CLASSCD = '" .$classcd ."'";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1' || $model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND SCHOOL_KIND     = '" .$school_kind."' ";
        }
        $row   = $db->getRow($query);
        Query::dbCheckIn($db);

        if (is_array($row)) {
            return true;
        } else {
            return false;
        }
    }

    //科目マスタの確認
    public function IsExistingSubclass($classcd, $model, $school_kind)
    {
        $db    = Query::dbCheckOut();
        
        $query  = " SELECT ";
        $query .= "      * ";
        $query .= " FROM ";
        $query .= "      SUBCLASS_MST ";
        $query .= " WHERE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1' || $model->Properties["useSchool_KindField"] == "1") {
            $query .= " CLASSCD = '" .$classcd ."'";
            $query .= " AND SCHOOL_KIND     = '" .$school_kind."' ";
        } else {
            $query .= " SUBSTR(SUBCLASSCD, 1, 2) = '" .$classcd ."'";
        }
        $row   = $db->getRow($query);
        Query::dbCheckIn($db);

        if (is_array($row)) {
            return true;
        } else {
            return false;
        }
    }

    //チェック対象のテーブル名を取得
    public function getTargetTable($allTables)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "   TABLE_NAME ";
        $query .= " FROM ";
        $query .= "   SYSIBM.COLUMNS ";
        $query .= " WHERE ";
        $query .= "   TABLE_NAME IN ('".implode("','", $allTables)."') ";

        return $query;
    }

    //対象科目が特定のテーブルに参照されているかをチェック
    public function checkClsCd($model, $tableName, $classcd)
    {
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
    public function &getInsertQuery($fields, $model, $school_kind)
    {
        $db = Query::dbCheckOut();
        
        $data["CLASSCD"][TEXT]              = $fields["CLASSCD"];
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1' || $model->Properties["useSchool_KindField"] == "1") {
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

        $query = Query::insertSQL($data, "class_mst");
        $db->query($query);
        
        Query::dbCheckIn($db);
        return;
    }

    //--- UPDATE
    public function &getUpdateQuery($fields, $model, $school_kind)
    {
        $db = Query::dbCheckOut();

        $data["CLASSCD"][TEXT]              = $fields["CLASSCD"];
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1' || $model->Properties["useSchool_KindField"] == "1") {
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
        if ($model->Properties["useCurriculumcd"] == '1' || $model->Properties["useSchool_KindField"] == "1") {
            $where .= "       AND  SCHOOL_KIND   = '".$fields["SCHOOL_KIND"]."' ";
        }
        $query = Query::updateSQL($data, "class_mst", $where);
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //--- DELETE
    public function &getDeleteQuery($fields, $model, $school_kind)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE ";
        $query .= " FROM ";
        $query .= "      CLASS_MST ";
        $query .= " WHERE ";
        $query .= "      CLASSCD = '" .$fields["CLASSCD"]."'";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1' || $model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND SCHOOL_KIND     = '" .$fields["SCHOOL_KIND"]."' ";
        }
        $db->query($query);
        Query::dbCheckIn($db);

        return $result;
    }

    //--- DELETE DETAIL_MST
    public function &detailDeleteQuery($fields, $model, $school_kind, $classSeq)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE ";
        $query .= " FROM ";
        $query .= "      CLASS_DETAIL_MST ";
        $query .= " WHERE ";
        $query .= "      CLASSCD = '".$fields["CLASSCD"]."'";
        $query .= "      AND SCHOOL_KIND = '".$fields["SCHOOL_KIND"]."' ";
        $query .= "      AND CLASS_SEQ = '".$classSeq."'";
        $db->query($query);
        Query::dbCheckIn($db);

        return $result;
    }

    //--- INSERT DETAIL_MST
    public function &detailInsertQuery($fields, $model, $school_kind, $classSeq)
    {
        $db = Query::dbCheckOut();

        $data["CLASSCD"][TEXT]          = $fields["CLASSCD"];
        $data["SCHOOL_KIND"][TEXT]      = $fields["SCHOOL_KIND"];
        $data["CLASS_SEQ"][TEXT]        = $classSeq;
        $data["CLASS_REMARK1"][TEXT]    = $fields["SEQ001"];
        $data["CLASS_REMARK2"][TEXT]    = $fields["SEQ001_REMARK2"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $query = Query::insertSQL($data, "CLASS_DETAIL_MST");
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }
}
