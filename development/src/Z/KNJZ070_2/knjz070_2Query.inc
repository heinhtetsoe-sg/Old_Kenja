<?php

require_once('for_php7.php');

class knjz070_2Query extends Query
{

    //校種取得
    public function getSchkind($model)
    {
        $query  = "";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " WITH T_ALL (LABEL, VALUE, SORT) AS ( ";
            $query .= "      VALUES('--全て--', '99', '00') ";
            $query .= "      ) ";
            $query .= " SELECT ";
            $query .= "      LABEL, ";
            $query .= "      VALUE, ";
            $query .= "      SORT ";
            $query .= " FROM ";
            $query .= "      T_ALL ";
            $query .= " UNION ALL  ";
        }
        $query .= " SELECT ";
        $query .= "     NAME1 || '：' || ABBV1 AS LABEL, ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     NAME1 AS SORT ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND NAME1 = '".SCHOOLKIND."' ";
        }

        $query .= " ORDER BY ";
        $query .= "     SORT ";

        return $query;
    }

    //学校名称取得
    public function getSchoolName($model)
    {
        $query  = " SELECT ";
        $query .= "      NAME1 ";
        $query .= " FROM ";
        $query .= "      NAME_MST ";
        $query .= " WHERE ";
        $query .= "      NAMECD1 = 'Z010' ";
        $query .= " AND  NAMECD2 = '00' ";

        return $query;
    }

    //教科取得（左画面）
    public function getClassCd($model)
    {
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     CLASSCD, ";
        $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CLASSCD AS VALUE, ";
        $query .= "     CLASSCD || '-' || SCHOOL_KIND || '：' || CLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CLASS_MST ";
        if ($model->schkind != "" && $model->schkind != "99") {
            $query .= " WHERE SCHOOL_KIND = '".$model->schkind."' ";
        } elseif ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " WHERE SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     CLASSCD ";

        return $query;
    }

    //SUBCLASS_MST データ取得
    public function getSubclassData($model)
    {
        if ($model->Properties["useSpecial_Support_Hrclass"] == '1') {
            $query  = " SELECT ";
            $query .= "      T1.*, ";
            $query .= "      T2.SUBCLASS_REMARK1 AS DETAIL001, ";
            $query .= "      T3.NAME1 AS DETAIL001_NAME ";
            $query .= " FROM ";
            $query .= "      SUBCLASS_MST T1 ";
            $query .= "      LEFT JOIN SUBCLASS_DETAIL_MST T2 ";
            $query .= "                     ON T2.CLASSCD       = T1.CLASSCD ";
            $query .= "                    AND T2.SCHOOL_KIND   = T1.SCHOOL_KIND ";
            $query .= "                    AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ";
            $query .= "                    AND T2.SUBCLASSCD    = T1.SUBCLASSCD ";
            $query .= "                    AND T2.SUBCLASS_SEQ  = '001' ";
            $query .= "      LEFT JOIN NAME_MST T3 ";
            $query .= "                     ON T3.NAMECD1   = 'A033' ";
            $query .= "                    AND T3.NAMECD2   = T2.SUBCLASS_REMARK1 ";
            $andWhere = "";
            if ($model->schkind != "" && $model->schkind != "99") {
                $query .= " WHERE T1.SCHOOL_KIND = '".$model->schkind."' ";
                $andWhere = " AND ";
            } elseif ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " WHERE T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                    $andWhere = " AND ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " WHERE T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
                $andWhere = " AND ";
            }
            if ($model->list_curriculum_cd != "" && $model->list_curriculum_cd != "99") {
                $andWhere = ($andWhere == "") ? " WHERE " : " AND ";
                $query .= "     {$andWhere} T1.CURRICULUM_CD = '{$model->list_curriculum_cd}' ";
            }
            if ($model->list_classcd != "" && $model->list_classcd != "99-99-99") {
                $andWhere = ($andWhere == "") ? " WHERE " : " AND ";
                $query .= "     {$andWhere} T1.CLASSCD = '".substr($model->list_classcd, 0, 2)."' ";
                $andWhere = ($andWhere == "") ? " WHERE " : " AND ";
                $query .= "     {$andWhere} T1.SCHOOL_KIND = '".substr($model->list_classcd, 3, 1)."' ";
            }
            $query .= " ORDER BY ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD DESC, ";
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SUBCLASSCD ";
        } else {
            $query  = " SELECT ";
            $query .= "      * ";
            $query .= " FROM ";
            $query .= "      SUBCLASS_MST ";
            $andWhere = "";
            if ($model->schkind != "" && $model->schkind != "99") {
                $query .= " WHERE SCHOOL_KIND = '".$model->schkind."' ";
                $andWhere = " AND ";
            } elseif ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " WHERE SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                    $andWhere = " AND ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " WHERE SCHOOL_KIND = '".SCHOOLKIND."' ";
                $andWhere = " AND ";
            }
            if ($model->list_curriculum_cd != "" && $model->list_curriculum_cd != "99") {
                $andWhere = ($andWhere == "") ? " WHERE " : " AND ";
                $query .= "     {$andWhere} CURRICULUM_CD = '{$model->list_curriculum_cd}' ";
            }
            if ($model->list_classcd != "" && $model->list_classcd != "99-99-99") {
                $andWhere = ($andWhere == "") ? " WHERE " : " AND ";
                $query .= "     {$andWhere} CLASSCD = '".substr($model->list_classcd, 0, 2)."' ";
                $andWhere = ($andWhere == "") ? " WHERE " : " AND ";
                $query .= "     {$andWhere} SCHOOL_KIND = '".substr($model->list_classcd, 3, 1)."' ";
            }
            $query .= " ORDER BY ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     SCHOOL_KIND, ";
                $query .= "     CURRICULUM_CD DESC, ";
                $query .= "     CLASSCD, ";
            }
            $query .= "     SUBCLASSCD ";
        }

        return $query;
    }

    public function getClassData($model)
    {
        $query  = " SELECT ";
        $query .= "      * ";
        $query .= " FROM ";
        $query .= "      CLASS_MST ";
        if ($model->schkind != "" && $model->schkind != "99") {
            $query .= " WHERE SCHOOL_KIND = '".$model->schkind."' ";
        } elseif ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " WHERE SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
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

    //教育課程コード取得
    public function getNamecd($cd)
    {
        $query  = " SELECT ";
        $query .= "      NAMECD2 AS VALUE, ";
        $query .= "      NAMECD2 || '：' || NAME1 AS LABEL ";
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
    public function getRow($model, $school_kind, $curriculum_cd, $cd)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $cd);
        }
        $db = Query::dbCheckOut();

        //特別支援
        if ($model->Properties["useSpecial_Support_Hrclass"] == '1') {
            $query  = " SELECT ";
            $query .= "     T1.*, ";
            $query .= "     T2.SUBCLASS_REMARK1 AS DETAIL001 ";
            $query .= " FROM ";
            $query .= "     SUBCLASS_MST T1 ";
            $query .= "     LEFT JOIN SUBCLASS_DETAIL_MST T2 ";
            $query .= "                 ON T2.CLASSCD       = T1.CLASSCD ";
            $query .= "                AND T2.SCHOOL_KIND   = T1.SCHOOL_KIND ";
            $query .= "                AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ";
            $query .= "                AND T2.SUBCLASSCD    = T1.SUBCLASSCD ";
            $query .= "                AND T2.SUBCLASS_SEQ  = '001'";
        } else {
            $query  = " SELECT * FROM SUBCLASS_MST T1 ";
        }

        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1' && $school_kind == "" && $curriculum_cd == "") {
            $query .= " WHERE ";
            $query .= "         T1.CLASSCD         = '" .$subclass_array[0]."' ";
            $query .= "     AND T1.SCHOOL_KIND     = '" .$subclass_array[1]."' ";
            $query .= "     AND T1.CURRICULUM_CD   = '" .$model->field["CURRICULUM_CD"]."' ";
            $query .= "     AND T1.SUBCLASSCD      = '" .$subclass_array[2]."' ";
        //教育課程対応
        } elseif ($model->Properties["useCurriculumcd"] == '1' && $school_kind != "" && $curriculum_cd != "") {
            $query .= " WHERE ";
            $query .= "         T1.CLASSCD         = '" .substr($cd, 0, 2)."' ";
            $query .= "     AND T1.SCHOOL_KIND     = '" .$school_kind."' ";
            $query .= "     AND T1.CURRICULUM_CD   = '" .$curriculum_cd."' ";
            $query .= "     AND T1.SUBCLASSCD      = '" .$cd."' ";
        } elseif ($model->Properties["useCurriculumcd"] != '1') {
            $query .= " WHERE ";
            $query .= "     T1.SUBCLASSCD = '" .$cd ."'";
        }
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //教育課程_チェック用１レコード取得
    public function getRowCheck($model, $cd, $checkcd)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $cd);
        }
        $db = Query::dbCheckOut();

        $query  = "SELECT * FROM subclass_mst ";
        $query .= "  WHERE ";
        $query .= "         CLASSCD         = '" .$subclass_array[0]."' ";
        $query .= "     AND SCHOOL_KIND     = '" .$subclass_array[1]."' ";
        $query .= "     AND CURRICULUM_CD   = '" .$model->field["CURRICULUM_CD"]."' ";
        $query .= "     AND SUBCLASSCD      = '" .$checkcd."' ";
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
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
    public function checkSubclass($model, $tableName, $subclasscd)
    {
        $query  = " SELECT ";
        $query .= "   COUNT(*) ";
        $query .= " FROM ";
        $query .= "   ( ";
        $query .= "     SELECT ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "       CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS SUBCLASSCD ";
        } else {
            $query .= "       SUBCLASSCD AS SUBCLASSCD ";
        }
        $query .= "     FROM ";
        $query .= "       {$tableName} ";
        $query .= "     GROUP BY ";
        $query .= "       CLASSCD, ";
        $query .= "       SCHOOL_KIND, ";
        $query .= "       CURRICULUM_CD, ";
        $query .= "       SUBCLASSCD ";
        $query .= "   ) ";
        $query .= " WHERE ";
        $query .= "   SUBCLASSCD = '{$subclasscd}' ";

        return $query;
    }

    //INSERT
    public function &getInsertQuery($fields, $model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $fields["SUBCLASSCD"]);
        }
        $db = Query::dbCheckOut();
        $data = array();

        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["CLASSCD"][TEXT]          = $subclass_array[0];
            $data["SCHOOL_KIND"][TEXT]      = $subclass_array[1];
            $data["CURRICULUM_CD"][TEXT]    = $model->field["CURRICULUM_CD"];
            $data["SUBCLASSCD"][TEXT]       = $subclass_array[2];
        } else {
            $data["SUBCLASSCD"][TEXT]           = $fields["SUBCLASSCD"];
        }
        $data["SUBCLASSNAME"][TEXT]         = $fields["SUBCLASSNAME"];
        $data["SUBCLASSABBV"][TEXT]         = $fields["SUBCLASSABBV"];
        $data["SUBCLASSNAME_ENG"][TEXT]     = $fields["SUBCLASSNAME_ENG"];
        $data["SUBCLASSABBV_ENG"][TEXT]     = $fields["SUBCLASSABBV_ENG"];
        $data["SUBCLASSORDERNAME1"][TEXT]   = $fields["SUBCLASSORDERNAME1"];
        $data["SUBCLASSORDERNAME2"][TEXT]   = $fields["SUBCLASSORDERNAME2"];
        $data["SUBCLASSORDERNAME3"][TEXT]   = $fields["SUBCLASSORDERNAME3"];
        $data["SHOWORDER"][NUMBER]          = $fields["SHOWORDER"];
        $data["SHOWORDER2"][NUMBER]         = $fields["SHOWORDER2"];
        $data["SHOWORDER3"][NUMBER]         = $fields["SHOWORDER3"];
        $data["SUBCLASSCD2"][TEXT]          = $fields["SUBCLASSCD2"];
        $data["SUBCLASSCD3"][TEXT]          = $fields["SUBCLASSCD3"];
        $data["ELECTDIV"][TEXT]             = ($fields["ELECTDIV"]==1) ? 1 : 0;
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        $query = Query::insertSQL($data, "subclass_mst");
        $db->query($query);

        //特別支援
        if ($model->Properties["useSpecial_Support_Hrclass"] == '1') {
            knjz070_2Query::getDetailDeleteQuery($db, $model, $subclass_array);
            knjz070_2Query::getDetailInsertQuery($db, $model, $subclass_array, $fields);
        }

        Query::dbCheckIn($db);

        return;
    }
    //UPDATE
    public function &getUpdateQuery($fields, $model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $fields["SUBCLASSCD"]);
        }
        $db = Query::dbCheckOut();
        $data = array();

        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["CLASSCD"][TEXT]          = $subclass_array[0];
            $data["SCHOOL_KIND"][TEXT]      = $subclass_array[1];
            $data["CURRICULUM_CD"][TEXT]    = $model->field["CURRICULUM_CD"];
            ;
            $data["SUBCLASSCD"][TEXT]       = $subclass_array[2];
        } else {
            $data["SUBCLASSCD"][TEXT]           = $fields["SUBCLASSCD"];
        }
        $data["SUBCLASSNAME"][TEXT]         = $fields["SUBCLASSNAME"];
        $data["SUBCLASSABBV"][TEXT]         = $fields["SUBCLASSABBV"];
        $data["SUBCLASSNAME_ENG"][TEXT]     = $fields["SUBCLASSNAME_ENG"];
        $data["SUBCLASSABBV_ENG"][TEXT]     = $fields["SUBCLASSABBV_ENG"];
        $data["SUBCLASSORDERNAME1"][TEXT]   = $fields["SUBCLASSORDERNAME1"];
        $data["SUBCLASSORDERNAME2"][TEXT]   = $fields["SUBCLASSORDERNAME2"];
        $data["SUBCLASSORDERNAME3"][TEXT]   = $fields["SUBCLASSORDERNAME3"];
        $data["SHOWORDER"][NUMBER]          = $fields["SHOWORDER"];
        $data["SHOWORDER2"][NUMBER]         = $fields["SHOWORDER2"];
        $data["SHOWORDER3"][NUMBER]         = $fields["SHOWORDER3"];
        $data["SUBCLASSCD2"][TEXT]          = $fields["SUBCLASSCD2"];
        $data["SUBCLASSCD3"][TEXT]          = $fields["SUBCLASSCD3"];
        $data["ELECTDIV"][TEXT]             = ($fields["ELECTDIV"]==1) ? 1 : 0;
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $where  = " WHERE  CLASSCD         = '" .$subclass_array[0]."'";
            $where .= "   AND  SCHOOL_KIND     = '" .$subclass_array[1]."'";
            $where .= "   AND  CURRICULUM_CD   = '" .$model->field["CURRICULUM_CD"]."'";
            $where .= "   AND  SUBCLASSCD      = '" .$subclass_array[2]."'";
        } else {
            $where .= " WHERE SUBCLASSCD         = '" .$fields["SUBCLASSCD"]."'";
        }
        $query = Query::updateSQL($data, "subclass_mst", $where);
        $db->query($query);

        //特別支援
        if ($model->Properties["useSpecial_Support_Hrclass"] == '1') {
            knjz070_2Query::getDetailDeleteQuery($db, $model, $subclass_array);
            knjz070_2Query::getDetailInsertQuery($db, $model, $subclass_array, $fields);
        }

        Query::dbCheckIn($db);

        return ;
    }
    //DELETE
    public function &getDeleteQuery($fields, $model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $fields["SUBCLASSCD"]);
        }
        if (knjz070_2Query::yearDataChk($fields["SUBCLASSCD"], $model)==0) {
            $db = Query::dbCheckOut();

            $query  = "DELETE FROM subclass_mst ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= " WHERE  CLASSCD         = '" .$subclass_array[0]."'";
                $query .= "   AND  SCHOOL_KIND     = '" .$subclass_array[1]."'";
                $query .= "   AND  CURRICULUM_CD   = '" .$model->field["CURRICULUM_CD"]."'";
                $query .= "   AND  SUBCLASSCD      = '" .$subclass_array[2]."'";
            } else {
                $query .= " WHERE  SUBCLASSCD      = '" .$fields["SUBCLASSCD"]."'";
            }
            $db->query($query);

            //特別支援
            if ($model->Properties["useSpecial_Support_Hrclass"] == '1') {
                knjz070_2Query::getDetailDeleteQuery($db, $model, $subclass_array);
            }

            Query::dbCheckIn($db);

            $result = true;
        } else {
            $result = false;
        }
        return $result;
    }

    public function yearDataChk($cd, $model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $cd);
        }
        $db = Query::dbCheckOut();
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS cnt";
        $query .= " FROM ";
        $query .= "     SUBCLASS_YDAT ";
        $query .= " WHERE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         CLASSCD         = '" .$subclass_array[0]."'";
            $query .= "     AND SCHOOL_KIND     = '" .$subclass_array[1]."' ";
            $query .= "     AND CURRICULUM_CD   = '" .$model->field["CURRICULUM_CD"]."' ";
            $query .= "     AND SUBCLASSCD      = '" .$subclass_array[2]."' ";
        } else {
            $query .= "     SUBCLASSCD = '" .$cd ."'";
        }
        $db->query($query);

        $cnt = $db->getone($query);
        Query::dbCheckIn($db);
        return $cnt;
    }

    //INSERT -- SUBCLASS_DETAIL_MST
    public function &getDetailInsertQuery($db, $model, $subclass_array, $fields)
    {
        //追加
        $data = array();
        $data["CLASSCD"][TEXT]              = $subclass_array[0];
        $data["SCHOOL_KIND"][TEXT]          = $subclass_array[1];
        $data["CURRICULUM_CD"][TEXT]        = $model->field["CURRICULUM_CD"];
        $data["SUBCLASSCD"][TEXT]           = $subclass_array[2];
        $data["SUBCLASS_SEQ"][TEXT]         = '001';
        $data["SUBCLASS_REMARK1"][TEXT]     = $fields["DETAIL001"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        $query = Query::insertSQL($data, "SUBCLASS_DETAIL_MST");
        $db->query($query);
    }

    //DELETE -- SUBCLASS_DETAIL_MST
    public function &getDetailDeleteQuery($db, $model, $subclass_array)
    {
        //削除
        $query  = " DELETE FROM ";
        $query .= "     SUBCLASS_DETAIL_MST ";
        $query .= " WHERE ";
        $query .= "     CLASSCD         = '".$subclass_array[0]."' AND ";
        $query .= "     SCHOOL_KIND     = '".$subclass_array[1]."' AND ";
        $query .= "     CURRICULUM_CD   = '".$model->field["CURRICULUM_CD"]."' AND ";
        $query .= "     SUBCLASSCD      = '".$subclass_array[2]."' AND ";
        $query .= "     SUBCLASS_SEQ    = '001' ";

        $db->query($query);
    }
}
