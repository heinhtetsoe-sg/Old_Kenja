<?php

require_once('for_php7.php');

class knjz218Query extends Query {

    //類型グループリスト取得
    function getTypeGroupList($grade)
    {
        $query  =" SELECT ";
        $query .="     T1.TYPE_GROUP_CD, ";
        $query .="     T1.TYPE_GROUP_NAME, ";
        $query .="     T1.GRADE, ";
        $query .="     COUNT(T1.TYPE_GROUP_CD) AS CNT ";
        $query .=" FROM ";
        $query .="     TYPE_GROUP_COURSE_MST T1 ";
        $query .="     LEFT JOIN TYPE_GROUP_COURSE_DAT T2 ON ";
        $query .="          T2.YEAR = T1.YEAR AND ";
        $query .="          T2.TYPE_GROUP_CD = T1.TYPE_GROUP_CD AND ";
        $query .="          T2.GRADE = T1.GRADE ";
        $query .=" WHERE ";
        $query .="     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .="     T1.GRADE = '".$grade."' ";
        $query .=" GROUP BY ";
        $query .="     T1.TYPE_GROUP_CD, ";
        $query .="     T1.TYPE_GROUP_NAME, ";
        $query .="     T1.GRADE ";
        $query .=" ORDER BY ";
        $query .="     T1.TYPE_GROUP_CD ";

        return $query;
    }

    //グループデータ取得
    function getGroupData($type_group_cd, $grade)
    {
        $query  =" SELECT ";
        $query .="     T1.TYPE_GROUP_CD, ";
        $query .="     T1.COURSECD, ";
        $query .="     T1.MAJORCD, ";
        $query .="     T1.COURSECODE, ";
        $query .="     T2.COURSENAME || ' ' || T2.MAJORNAME || ' ' || T3.COURSECODENAME AS COURSENAME ";
        $query .=" FROM ";
        $query .="     TYPE_GROUP_COURSE_DAT T1 ";
        $query .="     LEFT JOIN V_COURSE_MAJOR_MST T2 ON ";
        $query .="          T2.YEAR = T1.YEAR AND ";
        $query .="          T2.COURSECD = T1.COURSECD AND ";
        $query .="          T2.MAJORCD = T1.MAJORCD ";
        $query .="     LEFT JOIN V_COURSECODE_MST T3 ON ";
        $query .="          T3.YEAR = T1.YEAR AND ";
        $query .="          T3.COURSECODE = T1.COURSECODE ";
        $query .=" WHERE ";
        $query .="     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .="     T1.TYPE_GROUP_CD = '".$type_group_cd."' AND ";
        $query .="     T1.GRADE = '".$grade."' ";
        $query .=" ORDER BY ";
        $query .="     T1.COURSECD, ";
        $query .="     T1.MAJORCD, ";
        $query .="     T1.COURSECODE ";

        return $query;
    }

    //グループ名称取得
    function getGroupName($type_group_cd, $grade)
    {
        $query  = "SELECT ";
        $query .= "    TYPE_GROUP_NAME ";
        $query .= "FROM ";
        $query .= "    TYPE_GROUP_COURSE_MST ";
        $query .= "WHERE ";
        $query .="     YEAR = '".CTRL_YEAR."' AND ";
        $query .="     TYPE_GROUP_CD = '".$type_group_cd."' AND ";
        $query .="     GRADE = '".$grade."' ";

        return $query;
    }

    //学年取得
    function getGrade()
    {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     CAST(CAST(GRADE AS SMALLINT) AS CHAR(1)) || '学年' AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND GRADE < '11' ";
        $query .= " GROUP BY ";
        $query .= "     GRADE ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //データ一覧取得
    function selectQuery($grade, $type_group_cd)
    {
        $query  =" SELECT DISTINCT ";
        $query .="     T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS VALUE, ";
        $query .="     T2.COURSECD || T2.MAJORCD || T2.COURSECODE AS JOINCD, ";
        $query .="     T3.COURSENAME || ' ' || T3.MAJORNAME || ' ' || T4.COURSECODENAME AS LABEL ";
        $query .=" FROM ";
        $query .="     SCHREG_REGD_DAT T1 ";
        $query .="     LEFT JOIN TYPE_GROUP_COURSE_DAT T2 ON ";
        $query .="          T2.YEAR = T1.YEAR AND ";
        $query .="          T2.TYPE_GROUP_CD = '".$type_group_cd."' AND ";
        $query .="          T2.GRADE = T1.GRADE AND  ";
        $query .="          T2.COURSECD = T1.COURSECD AND  ";
        $query .="          T2.MAJORCD = T1.MAJORCD AND  ";
        $query .="          T2.COURSECODE = T1.COURSECODE  ";
        $query .="     LEFT JOIN V_COURSE_MAJOR_MST T3 ON ";
        $query .="          T3.YEAR = T1.YEAR AND ";
        $query .="          T3.COURSECD = T1.COURSECD AND ";
        $query .="          T3.MAJORCD = T1.MAJORCD ";
        $query .="     LEFT JOIN V_COURSECODE_MST T4 ON ";
        $query .="          T4.YEAR = T1.YEAR AND ";
        $query .="          T4.COURSECODE = T1.COURSECODE ";
        $query .=" WHERE ";
        $query .="     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .="     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .="     T1.GRADE = '".$grade."' ";
        $query .=" ORDER BY ";
        $query .="     VALUE ";

        return $query;
    }

    //チェック用SQL
    function checkTypeGroupM($year, $div = "")
    {
        $query  = "SELECT ";
        $query .= "    COUNT(*) ";
        $query .= "FROM ";
        $query .= "    TYPE_GROUP_COURSE_MST ";
        $query .= "WHERE ";
        if ($div == "LAST_DATA") {
            $query .= "    YEAR = '".$year."' ";
        } else {
            $query .= "    YEAR = '".CTRL_YEAR."' ";
        }

        return $query;
    }

    //チェック用SQL
    function checkTypeGroupD($model, $year, $div)
    {
        $query  = "SELECT ";
        $query .= "    COUNT(*) ";
        $query .= "FROM ";
        $query .= "    TYPE_GROUP_COURSE_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".$year."' ";
        if ($div == "COPY") {
            $query .= "    AND GRADE || COURSECD || MAJORCD || COURSECODE ";
            $query .= "         IN (".knjz218Query::getCourse().") ";
        } else {
            $query .= "    AND TYPE_GROUP_CD = '".$model->type_group_cd."' ";
            $query .= "    AND GRADE = '".$model->grade."' ";
        }

        return $query;
    }

    //今年度のコース
    function getCourse()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GRADE || T1.COURSECD || T1.MAJORCD || T1.COURSECODE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN V_COURSE_MAJOR_MST T2 ON ";
        $query .= "         T2.YEAR = T1.YEAR AND ";
        $query .= "         T2.COURSECD = T1.COURSECD AND ";
        $query .= "         T2.MAJORCD = T1.MAJORCD ";
        $query .= "     LEFT JOIN V_COURSECODE_MST T3 ON ";
        $query .= "         T3.YEAR = T1.YEAR AND ";
        $query .= "         T3.COURSECODE = T1.COURSECODE ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE < '11' ";

        return $query;
    }

    //コピー処理
    function getCopyQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $lastyear = CTRL_YEAR - 1;

        //TYPE_GROUP_COURSE_MST
        $query  = " INSERT INTO TYPE_GROUP_COURSE_MST ";
        $query .= " ( ";
        $query .= " SELECT ";
        $query .= "     '".CTRL_YEAR."', ";
        $query .= "     TYPE_GROUP_CD, ";
        $query .= "     GRADE, ";
        $query .= "     TYPE_GROUP_NAME, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     SYSDATE() ";
        $query .= " FROM ";
        $query .= "     TYPE_GROUP_COURSE_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR  = '".$lastyear."' AND ";
        $query .= "     GRADE < '11' AND ";
        $query .= "     GRADE || TYPE_GROUP_CD IN ( SELECT DISTINCT ";
        $query .= "                             GRADE || TYPE_GROUP_CD ";
        $query .= "                        FROM ";
        $query .= "                             TYPE_GROUP_COURSE_DAT ";
        $query .= "                        WHERE ";
        $query .= "                             YEAR = '".$lastyear."' AND ";
        $query .= "                             GRADE < '11' AND ";
        $query .= "                             GRADE || COURSECD || MAJORCD || COURSECODE IN (".knjz218Query::getCourse().") ";
        $query .= "                           ) ";
        $query .= " ) ";

        $db->query($query);

        //TYPE_GROUP_COURSE_DAT
        $query  = " INSERT INTO TYPE_GROUP_COURSE_DAT ";
        $query .= " ( ";
        $query .= " SELECT ";
        $query .= "     '".CTRL_YEAR."', ";
        $query .= "     TYPE_GROUP_CD, ";
        $query .= "     GRADE, ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD, ";
        $query .= "     COURSECODE, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     SYSDATE() ";
        $query .= " FROM ";
        $query .= "     TYPE_GROUP_COURSE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$lastyear."' AND ";
        $query .= "     GRADE < '11' AND ";
        $query .= "     GRADE || COURSECD || MAJORCD || COURSECODE IN (".knjz218Query::getCourse().") ";
        $query .= " ) ";

        $db->query($query);

        $db->commit();

        Query::dbCheckIn($db);

        return;
    }

    //マスタ新規SQL作成
    function makeInsTypeGrpMQuery($model)
    {
        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["TYPE_GROUP_CD"][TEXT]    = $model->type_group_cd;
        $data["GRADE"][TEXT]            = $model->grade;
        $data["TYPE_GROUP_NAME"][TEXT]  = $model->type_group_name;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $query = Query::insertSQL($data, "TYPE_GROUP_COURSE_MST");

        return $query;
    }

    //マスタ削除SQL作成
    function delTypeGroupMstQuery($model)
    {
        $query  = "DELETE FROM ";
        $query .= "    TYPE_GROUP_COURSE_MST ";
        $query .= "WHERE ";
        $query .="     YEAR = '".CTRL_YEAR."' AND ";
        $query .="     TYPE_GROUP_CD = '".$model->type_group_cd."' AND ";
        $query .="     GRADE = '".$model->grade."' ";

        return $query;
    }

    //グループデータ追加
    function &TypeGrpDInstQuery(&$db, $select, $model)
    {
        //グループ新規
        $data = array();
        $selectdata = explode(",", $select);
        for ($i = 0; $i < get_count($selectdata); $i++) {
            if (!$selectdata[$i]) {
                continue;
            }
            $db->query(knjz218Query::makeInsTypeGrpDQuery($model, $selectdata[$i]));
        }
        return;
    }

    //データ新規SQL作成
    function makeInsTypeGrpDQuery($model, $selectdata)
    {

        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["TYPE_GROUP_CD"][TEXT]    = $model->type_group_cd;
        $data["GRADE"][TEXT]            = $model->grade;
        $data["COURSECD"][TEXT]         = substr($selectdata, 0, 1);
        $data["MAJORCD"][TEXT]          = substr($selectdata, 1, 3);
        $data["COURSECODE"][TEXT]       = substr($selectdata, 4);
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $query = Query::insertSQL($data, "TYPE_GROUP_COURSE_DAT");

        return $query;
    }

    //データ削除SQL作成
    function delTypeGrpDQuery($model)
    {
        $query  = "DELETE FROM ";
        $query .= "    TYPE_GROUP_COURSE_DAT ";
        $query .= "WHERE ";
        $query .="     YEAR = '".CTRL_YEAR."' AND ";
        $query .="     TYPE_GROUP_CD = '".$model->type_group_cd."' AND ";
        $query .="     GRADE = '".$model->grade."' ";

        return $query;
    }
}
?>
