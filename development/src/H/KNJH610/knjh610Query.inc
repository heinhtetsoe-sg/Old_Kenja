<?php

require_once('for_php7.php');

class knjh610Query extends Query
{

    //左画面リスト
    public function getList($model)
    {
        $query  = " SELECT ";
        $query .= "     CENTER_CLASS_CD, ";
        $query .= "     CENTER_SUBCLASS_CD, ";
        $query .= "     SUBCLASS_NAME, ";
        $query .= "     SUBCLASS_ABBV, ";
        $query .= "     CLASSCD, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CURRICULUM_CD, ";
        }
        $query .= "     SUBCLASSCD, ";
        $query .= "     SUBCLASS_DIV ";
        $query .= " FROM ";
        $query .= "     MOCK_CENTER_SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     CENTER_SUBCLASS_CD ";

        return $query;
    }

    //１レコード取得
    public function getRow($cd, $db, $model, $centerClasscd)
    {
        $query  = " SELECT ";
        $query .= "     CENTER_CLASS_CD, ";
        $query .= "     CENTER_SUBCLASS_CD, ";
        $query .= "     BUNRIDIV, ";
        $query .= "     SUBCLASS_NAME, ";
        $query .= "     SUBCLASS_ABBV, ";
        $query .= "     SUBCLASS_DIV, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND AS CLASSCD, ";
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS SUBCLASSCD, ";
        } else {
            $query .= "     CLASSCD, ";
            $query .= "     SUBCLASSCD, ";
        }
        $query .= "     PERFECT, ";
        $query .= "     ALLOT_POINT ";
        $query .= " FROM ";
        $query .= "     MOCK_CENTER_SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND CENTER_CLASS_CD    = '" .$centerClasscd ."' ";
        $query .= "     AND CENTER_SUBCLASS_CD = '" .$cd ."' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        return $row;
    }

    //共通テスト教科コードコンボ
    public function getCenterclasscd()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     CENTER_CLASS_CD AS VALUE, ";
        $query .= "     CENTER_CLASS_CD || '：' || CLASS_ABBV AS LABEL  ";
        $query .= " FROM ";
        $query .= "     MOCK_CENTER_CLASS_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //文理区分
    public function getBunri()
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'H316' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //教科コンボ
    public function getClasscd($model)
    {
        $query  = " SELECT ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND AS VALUE, ";
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '　' || CLASSNAME AS LABEL ";
        } else {
            $query .= "     CLASSCD AS VALUE, ";
            $query .= "     CLASSCD || '　' || CLASSNAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     CLASS_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目コンボ
    public function getSubclasscd($classCd, $model)
    {
        $query  = " SELECT ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS VALUE, ";
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD || '　' || SUBCLASSNAME AS LABEL ";
        } else {
            $query .= "     SUBCLASSCD AS VALUE, ";
            $query .= "     SUBCLASSCD || '　' || SUBCLASSNAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     SUBCLASS_MST ";
        $query .= " WHERE ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND  = '{$classCd}' ";
        } else {
            $query .= "     SUBCLASSCD LIKE '{$classCd}%' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //前年度からのコピーの件数カウント
    public function getCopyCountQuery()
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     MOCK_CENTER_SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".(CTRL_YEAR-1)."' ";

        $cnt = $db->getOne($query);
        Query::dbCheckIn($db);
        return $cnt;
    }

    //今年度の件数カウント
    public function getCopyCount2()
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     MOCK_CENTER_SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";

        $cnt2 = $db->getOne($query);
        Query::dbCheckIn($db);
        return $cnt2;
    }

    //今年度のMOCK_CENTER_CLASS_MSTデータを消去
    public function &getCopyDelClassQuery()
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     MOCK_CENTER_CLASS_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //前年度からのコピー(MOCK_CENTER_CLASS_MST)
    public function &getCopyClassQuery()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " INSERT INTO MOCK_CENTER_CLASS_MST ";
        $query .= " SELECT ";
        $query .= "     '".CTRL_YEAR."' AS YEAR, ";
        $query .= "     CENTER_CLASS_CD, ";
        $query .= "     CLASS_NAME, ";
        $query .= "     CLASS_ABBV, ";
        $query .= "     CLASSCD, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     CLASS_DIV, ";
        $query .= "     PERFECT, ";
        $query .= "     ALLOT_POINT, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     MOCK_CENTER_CLASS_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".(CTRL_YEAR-1)."' ";

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }

    //今年度のMOCK_CENTER_SUBCLASS_MSTデータを消去
    public function &getCopyDelSubQuery()
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     MOCK_CENTER_SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //前年度からのコピー(MOCK_CENTER_SUBCLASS_MST)
    public function &getCopySubQuery()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " INSERT INTO MOCK_CENTER_SUBCLASS_MST ";
        $query .= " SELECT ";
        $query .= "     '".CTRL_YEAR."' AS YEAR, ";
        $query .= "     CENTER_CLASS_CD, ";
        $query .= "     CENTER_SUBCLASS_CD, ";
        $query .= "     BUNRIDIV, ";
        $query .= "     SUBCLASS_NAME, ";
        $query .= "     SUBCLASS_ABBV, ";
        $query .= "     CLASSCD, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     CURRICULUM_CD, ";
        $query .= "     SUBCLASSCD, ";
        $query .= "     SUBCLASS_DIV, ";
        $query .= "     PERFECT, ";
        $query .= "     ALLOT_POINT, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     MOCK_CENTER_SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".(CTRL_YEAR-1)."' ";

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }

    //--- INSERT
    public function &getInsertQuery($fields, $model)
    {
        $db = Query::dbCheckOut();

        $data["YEAR"][TEXT]               = CTRL_YEAR;
        $data["CENTER_CLASS_CD"][TEXT]    = $fields["CENTER_CLASS_CD"];
        $data["CENTER_SUBCLASS_CD"][TEXT] = $fields["CENTER_SUBCLASS_CD"];
        $data["BUNRIDIV"][TEXT]           = $fields["BUNRIDIV"];
        $data["SUBCLASS_NAME"][TEXT]      = $fields["SUBCLASS_NAME"];
        $data["SUBCLASS_ABBV"][TEXT]      = $fields["SUBCLASS_ABBV"];
        if ($model->Properties["useCurriculumcd"] == '1') {
            list($classcd, $school_kind)  = explode('-', $fields["CLASSCD"]);
            $subclasscd = explode('-', $fields["SUBCLASSCD"]);
            $data["CLASSCD"][TEXT]            = $classcd;
            $data["SCHOOL_KIND"][TEXT]        = $school_kind;
            $data["CURRICULUM_CD"][TEXT]      = $subclasscd[2];
            $data["SUBCLASSCD"][TEXT]         = $subclasscd[3];
        } else {
            $data["CLASSCD"][TEXT]            = $fields["CLASSCD"];
            $data["SUBCLASSCD"][TEXT]         = $fields["SUBCLASSCD"];
        }
        $data["SUBCLASS_DIV"][TEXT]       = $fields["SUBCLASS_DIV"];
        $data["PERFECT"][NUMBER]          = $fields["PERFECT"];
        $data["ALLOT_POINT"][NUMBER]      = $fields["ALLOT_POINT"];
        $data["REGISTERCD"][TEXT]         = STAFFCD;
        $data["UPDATED"][NUMBER]          = "sysdate()";

        $query = Query::insertSQL($data, "MOCK_CENTER_SUBCLASS_MST");
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //--- UPDATE
    public function &getUpdateQuery($fields, $model)
    {
        $db = Query::dbCheckOut();

        $data["YEAR"][TEXT]               = CTRL_YEAR;
        $data["CENTER_CLASS_CD"][TEXT]    = $fields["CENTER_CLASS_CD"];
        $data["CENTER_SUBCLASS_CD"][TEXT] = $fields["CENTER_SUBCLASS_CD"];
        $data["BUNRIDIV"][TEXT]           = $fields["BUNRIDIV"];
        $data["SUBCLASS_NAME"][TEXT]      = $fields["SUBCLASS_NAME"];
        $data["SUBCLASS_ABBV"][TEXT]      = $fields["SUBCLASS_ABBV"];
        if ($model->Properties["useCurriculumcd"] == '1') {
            list($classcd, $school_kind) = explode('-', $fields["CLASSCD"]);
            $subclasscd = explode('-', $fields["SUBCLASSCD"]);
            $data["CLASSCD"][TEXT]            = $classcd;
            $data["SCHOOL_KIND"][TEXT]        = $school_kind;
            $data["CURRICULUM_CD"][TEXT]      = $subclasscd[2];
            $data["SUBCLASSCD"][TEXT]         = $subclasscd[3];
        } else {
            $data["CLASSCD"][TEXT]            = $fields["CLASSCD"];
            $data["SUBCLASSCD"][TEXT]         = $fields["SUBCLASSCD"];
        }
        $data["SUBCLASS_DIV"][TEXT]       = $fields["SUBCLASS_DIV"];
        $data["PERFECT"][NUMBER]          = $fields["PERFECT"];
        $data["ALLOT_POINT"][NUMBER]      = $fields["ALLOT_POINT"];
        $data["REGISTERCD"][TEXT]         = STAFFCD;
        $data["UPDATED"][NUMBER]          = "sysdate()";

        $where  = " WHERE ";
        $where .= "         YEAR               = '".CTRL_YEAR."' ";
        $where .= "     AND CENTER_CLASS_CD    = '" .$fields["CENTER_CLASS_CD"]."' ";
        $where .= "     AND CENTER_SUBCLASS_CD = '" .$fields["CENTER_SUBCLASS_CD"]."' ";


        $query = Query::updateSQL($data, "MOCK_CENTER_SUBCLASS_MST", $where);
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    public function &getDeleteQuery($fields)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     MOCK_CENTER_SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR               = '".CTRL_YEAR."' ";
        $query .= "     AND CENTER_CLASS_CD    = '" .$fields["CENTER_CLASS_CD"]."' ";
        $query .= "     AND CENTER_SUBCLASS_CD = '" .$fields["CENTER_SUBCLASS_CD"]."' ";

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }
}
