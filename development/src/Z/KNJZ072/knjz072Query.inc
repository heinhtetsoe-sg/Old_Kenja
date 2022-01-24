<?php

require_once('for_php7.php');

class knjz072Query extends Query
{

    //SUBCLASS_MST データ取得
    public function getSubclassData($model)
    {
        $query  = " SELECT ";
        $query .= "      * ";
        $query .= " FROM ";
        $query .= "      SUBCLASS_MST ";
        $query .= " ORDER BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CURRICULUM_CD, ";
            $query .= "     CLASSCD, ";
        }
        $query .= "     SUBCLASSCD ";

        return $query;
    }
    
    public function getClassData($model)
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
    public function getNamecd($cd)
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
    public function getRow($model, $school_kind, $curriculum_cd, $cd, $flg = "")
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $cd);
        }
        $db = Query::dbCheckOut();

        $query  = "SELECT * FROM subclass_mst ";
        
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1' && $school_kind == "" && $curriculum_cd == "") {
            $query .= "  WHERE ";
            $query .= "         CLASSCD         = '" .$subclass_array[0]."' ";
            $query .= "     AND SCHOOL_KIND     = '" .$subclass_array[1]."' ";
            $query .= "     AND CURRICULUM_CD   = '" .$model->field["CURRICULUM_CD"]."' ";
            $query .= "     AND SUBCLASSCD      = '" .$subclass_array[2]."' ";
        //教育課程対応
        } elseif ($model->Properties["useCurriculumcd"] == '1' && $school_kind != "" && $curriculum_cd != "") {
            $query .= "  WHERE ";
            $query .= "         CLASSCD         = '" .substr($cd, 0, 2)."' ";
            $query .= "     AND SCHOOL_KIND     = '" .$school_kind."' ";
            $query .= "     AND CURRICULUM_CD   = '" .$curriculum_cd."' ";
            $query .= "     AND SUBCLASSCD      = '" .$cd."' ";
        } elseif ($model->Properties["useCurriculumcd"] != '1') {
            $query .= "  WHERE  SUBCLASSCD = '" .$cd ."'";
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

    //学校別教育委員会科目外テーブルに科目コードがあるか(INSERT)(DELETE)
    public function getRowSubclassAnotherDat($model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $db = Query::dbCheckOut();
        //学校別教育委員会科目外テーブルのデータ削除
        $query  = "SELECT * FROM EDBOARD_SUBCLASS_DAT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " WHERE  CLASSCD         = '" .$subclass_array[0]."'";
            $query .= "   AND  SCHOOL_KIND     = '" .$subclass_array[1]."'";
            $query .= "   AND  CURRICULUM_CD   = '" .$model->field["CURRICULUM_CD"]."'";
            $query .= "   AND  SUBCLASSCD      = '" .$subclass_array[2]."'";
        } else {
            $query .= " WHERE  SUBCLASSCD      = '" .$fields["SUBCLASSCD"]."'";
        }
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
            $data["SUBCLASSCD"][TEXT]       = $fields["SUBCLASSCD"];
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
        $data["ELECTDIV"][TEXT]             = ($fields["ELECTDIV"]==1) ? 1 : 0;
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        $query = Query::insertSQL($data, "subclass_mst");
        $db->query($query);

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
        if (knjz072Query::year_data_chk($fields["SUBCLASSCD"], $model)==0) {
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

            Query::dbCheckIn($db);

            $result = true;
        } else {
            $result = false;
        }
        return $result;
    }

    public function year_data_chk($cd, $model)
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

        $cnt = $db->getOne($query);
        Query::dbCheckIn($db);
        return $cnt;
    }

    //教育委員会用の学校コード取得
    public function getSchoolCd()
    {
        $query  = " SELECT ";
        $query .= "      KYOUIKU_IINKAI_SCHOOLCD ";
        $query .= " FROM ";
        $query .= "      V_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "      YEAR    = '".CTRL_YEAR."' ";
        return $query;
    }
}
