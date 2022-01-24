<?php

require_once('for_php7.php');

class knjz234Query extends Query {

    //グループ名称取得
    function GetChairMst($model)
    {
        $query  =" SELECT ";
        $query .="     CHA_GM.CHAIR_GROUP_CD, ";
        $query .="     CHA_GM.CHAIR_GROUP_NAME, ";
        $query .="     CHA_GM.SUBCLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .="     CHA_GM.CLASSCD, ";
            $query .="     CHA_GM.SCHOOL_KIND, ";
            $query .="     CHA_GM.CURRICULUM_CD, ";
        }
        $query .="     SUB_M.SUBCLASSNAME, ";
        $query .="     CHA_GD.TESTKINDCD || CHA_GD.TESTITEMCD AS TEST_CD, ";
        $query .="     TEST_M.TESTITEMNAME ";
        $query .=" FROM ";
        $query .="     CHAIR_GROUP_MST CHA_GM ";
        $query .="     LEFT JOIN SUBCLASS_MST SUB_M ON SUB_M.SUBCLASSCD = CHA_GM.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .="          AND SUB_M.CLASSCD = CHA_GM.CLASSCD ";
            $query .="          AND SUB_M.SCHOOL_KIND = CHA_GM.SCHOOL_KIND ";
            $query .="          AND SUB_M.CURRICULUM_CD = CHA_GM.CURRICULUM_CD ";
        }
        $query .="     LEFT JOIN CHAIR_GROUP_DAT CHA_GD ON CHA_GD.YEAR = CHA_GM.YEAR ";
        $query .="          AND CHA_GD.SEMESTER = CHA_GM.SEMESTER ";
        $query .="          AND CHA_GD.CHAIR_GROUP_CD = CHA_GM.CHAIR_GROUP_CD ";
        $query .="     LEFT JOIN TESTITEM_MST_COUNTFLG TEST_M ON TEST_M.YEAR = CHA_GM.YEAR ";
        $query .="          AND TEST_M.TESTKINDCD = CHA_GD.TESTKINDCD ";
        $query .="          AND TEST_M.TESTITEMCD = CHA_GD.TESTITEMCD ";
        $query .=" WHERE ";
        $query .="     CHA_GM.YEAR = '".CTRL_YEAR."' ";
        $query .="     AND CHA_GM.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .=" GROUP BY ";
        $query .="     CHA_GM.CHAIR_GROUP_CD, ";
        $query .="     CHA_GM.CHAIR_GROUP_NAME, ";
        $query .="     CHA_GM.SUBCLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .="     CHA_GM.CLASSCD, ";
            $query .="     CHA_GM.SCHOOL_KIND, ";
            $query .="     CHA_GM.CURRICULUM_CD, ";
        }
        $query .="     SUB_M.SUBCLASSNAME, ";
        $query .="     CHA_GD.TESTKINDCD, ";
        $query .="     CHA_GD.TESTITEMCD, ";
        $query .="     TEST_M.TESTITEMNAME ";
        $query .=" ORDER BY ";
        $query .="     CHA_GM.CHAIR_GROUP_CD ";

        return $query;
    }

    //グループデータ取得
    function getGroupData($gropucd, $test_cd)
    {
        $query  =" SELECT ";
        $query .="     CHA_GD.CHAIR_GROUP_CD, ";
        $query .="     CHA_GD.CHAIRCD, ";
        $query .="     CHA_D.CHAIRNAME ";
        $query .=" FROM ";
        $query .="     CHAIR_GROUP_DAT CHA_GD ";
        $query .="     LEFT JOIN CHAIR_DAT CHA_D ON CHA_D.YEAR = CHA_GD.YEAR ";
        $query .="          AND CHA_D.SEMESTER = CHA_GD.SEMESTER ";
        $query .="          AND CHA_D.CHAIRCD = CHA_GD.CHAIRCD ";
        $query .=" WHERE ";
        $query .="     CHA_GD.YEAR = '".CTRL_YEAR."' ";
        $query .="     AND CHA_GD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .="     AND CHA_GD.CHAIR_GROUP_CD = '".$gropucd."' ";
        $query .="     AND CHA_GD.TESTKINDCD || CHA_GD.TESTITEMCD = '".$test_cd."' ";
        $query .=" ORDER BY ";
        $query .="     CHA_GD.CHAIRCD ";

        return $query;
    }

    //グループ名称取得
    function getGroupName($chair_group_cd)
    {
        $query  = "SELECT ";
        $query .= "    CHAIR_GROUP_NAME ";
        $query .= "FROM ";
        $query .= "    CHAIR_GROUP_MST ";
        $query .= "WHERE ";
        $query .="     YEAR = '".CTRL_YEAR."' ";
        $query .="     AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .="     AND CHAIR_GROUP_CD = '".$chair_group_cd."' ";

        return $query;
    }

    //科目コード取得
    function getSubclass($subclasscd, $model)
    {
        $query  =" SELECT DISTINCT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .="     CHA_D.CLASSCD || '-' || CHA_D.SCHOOL_KIND || '-' || CHA_D.CURRICULUM_CD || '-' || CHA_D.SUBCLASSCD AS VALUE, ";
        } else {
            $query .="     CHA_D.SUBCLASSCD AS VALUE, ";
        }
        $query .="     SUB_M.SUBCLASSNAME AS LABEL ";
        $query .=" FROM ";
        $query .="     CHAIR_DAT CHA_D ";
        $query .="     LEFT JOIN SUBCLASS_MST SUB_M ON SUB_M.SUBCLASSCD = CHA_D.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .="                                  AND SUB_M.CLASSCD = CHA_D.CLASSCD ";
            $query .="                                  AND SUB_M.SCHOOL_KIND = CHA_D.SCHOOL_KIND ";
            $query .="                                  AND SUB_M.CURRICULUM_CD = CHA_D.CURRICULUM_CD ";
        }
        $query .=" WHERE ";
        $query .="     CHA_D.YEAR = '".CTRL_YEAR."' ";
        $query .="     AND CHA_D.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .=" ORDER BY ";
        $query .="     VALUE ";

        return $query;
    }

    //テスト名称取得
    function getTestItem()
    {
        $query  =" SELECT ";
        $query .="     TESTKINDCD || TESTITEMCD AS VALUE, ";
        $query .="     TESTITEMNAME AS LABEL ";
        $query .=" FROM ";
        $query .="     TESTITEM_MST_COUNTFLG ";
        $query .=" WHERE ";
        $query .="     YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //データ一覧取得
    function selectQuery($subclasscd, $chair_group_cd, $test_cd, $model)
    {
        //教育課程対応
        if (($model->Properties["useCurriculumcd"] == '1') && (strlen($subclasscd) > 6)) {
            $substr_classcd         = substr($subclasscd, 0, 2);
            $substr_school_kind     = substr($subclasscd, 3, 1);
            $substr_curriculum_cd   = substr($subclasscd, 5, 1);
            $substr_subclasscd      = substr($subclasscd, 7, 6);
        }
        $query  =" SELECT ";
        $query .="     CHA_D.CHAIRCD AS VALUE, ";
        $query .="     CHA_GD.CHAIRCD AS JOINCD, ";
        $query .="     CHA_D.CHAIRNAME AS LABEL ";
        $query .=" FROM ";
        $query .="     CHAIR_DAT CHA_D ";
        $query .="     LEFT JOIN CHAIR_GROUP_DAT CHA_GD ON CHA_GD.YEAR = CHA_D.YEAR ";
        $query .="          AND CHA_GD.SEMESTER = CHA_D.SEMESTER ";
        $query .="          AND CHA_GD.CHAIRCD = CHA_D.CHAIRCD ";
        $query .="          AND CHA_GD.CHAIR_GROUP_CD = '".$chair_group_cd."' ";
        $query .="          AND CHA_GD.TESTKINDCD || CHA_GD.TESTITEMCD = '".$test_cd."' ";
        $query .=" WHERE ";
        $query .="     CHA_D.YEAR = '".CTRL_YEAR."' ";
        $query .="     AND CHA_D.SEMESTER = '".CTRL_SEMESTER."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            if (strlen($subclasscd) > 6) {
                $query .="     AND CHA_D.CLASSCD        = '".$substr_classcd."' ";
                $query .="     AND CHA_D.SCHOOL_KIND    = '".$substr_school_kind."' ";
                $query .="     AND CHA_D.CURRICULUM_CD  = '".$substr_curriculum_cd."' ";
                $query .="     AND CHA_D.SUBCLASSCD     = '".$substr_subclasscd."' ";
            } else {
                $query .="     AND CHA_D.CLASSCD        = '".$model->classcd."' ";
                $query .="     AND CHA_D.SCHOOL_KIND    = '".$model->school_kind."' ";
                $query .="     AND CHA_D.CURRICULUM_CD  = '".$model->curriculum_cd."' ";
                $query .="     AND CHA_D.SUBCLASSCD     = '".$subclasscd."' ";
            }
        } else {
            $query .="     AND CHA_D.SUBCLASSCD = '".$subclasscd."' ";
        }
        $query .=" ORDER BY ";
        $query .="     VALUE ";

        return $query;
    }

    //チェック用SQL
    function checkChairGroupM($model, $year_sem, $div = "")
    {
        $query  = "SELECT ";
        $query .= "    COUNT(*) ";
        $query .= "FROM ";
        $query .= "    CHAIR_GROUP_MST ";
        $query .= "WHERE ";
        if ($div == "LAST_DATA") {
            $query .= "    YEAR || SEMESTER = '".$year_sem."' ";
        } else {
            $query .= "    YEAR = '".CTRL_YEAR."' ";
            $query .= "    AND SEMESTER = '".CTRL_SEMESTER."' ";
        }
        if ($div == "INS" || $div == "UPD") {
            $query .= "    AND CHAIR_GROUP_CD = '".$model->chair_group_cd."' ";
        }

        return $query;
    }

    //チェック用SQL
    function checkChairGroupD($model, $year_sem, $div)
    {
        $query  = "SELECT ";
        $query .= "    COUNT(*) ";
        $query .= "FROM ";
        $query .= "    CHAIR_GROUP_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR || SEMESTER = '".$year_sem."' ";
        if ($div == "COPY") {
            $query .= "    AND CHAIRCD IN (".knjz234Query::getChair().") ";
        } else {
            $query .= "    AND CHAIR_GROUP_CD = '".$model->chair_group_cd."' ";
            if ($div != "DEL") {
                $query .= "    AND TESTKINDCD || TESTITEMCD = '".$model->test_cd."' ";
            }
        }

        return $query;
    }

    //今年度、今学期講座
    function getChair()
    {
        $query  = " SELECT ";
        $query .= "     CHAIRCD ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";

        return $query;
    }

    //コピー処理
    function getCopyQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $year_sem = $db->getOne(knjz234Query::getMaxYearSem());

        //CHAIR_GROUP_MST
        $query  = " INSERT INTO CHAIR_GROUP_MST ";
        $query .= " ( ";
        $query .= " SELECT ";
        $query .= "     '".CTRL_YEAR."', ";
        $query .= "     '".CTRL_SEMESTER."', ";
        $query .= "     CHAIR_GROUP_CD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD, ";
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CURRICULUM_CD, ";
        }
        $query .= "     SUBCLASSCD, ";
        $query .= "     CHAIR_GROUP_NAME, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     SYSDATE() ";
        $query .= " FROM ";
        $query .= "     CHAIR_GROUP_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR || SEMESTER = '".$year_sem."' ";
        $query .= "     AND CHAIR_GROUP_CD IN ( SELECT DISTINCT ";
        $query .= "                                 CHAIR_GROUP_CD ";
        $query .= "                             FROM ";
        $query .= "                                 CHAIR_GROUP_DAT ";
        $query .= "                             WHERE ";
        $query .= "                                 YEAR || SEMESTER = '".$year_sem."' ";
        $query .= "                                 AND CHAIRCD IN (".knjz234Query::getChair().") ";
        $query .= "                           ) ";
        $query .= " ) ";

        $db->query($query);

        //CHAIR_GROUP_DAT
        $query  = " INSERT INTO CHAIR_GROUP_DAT ";
        $query .= " ( ";
        $query .= " SELECT ";
        $query .= "     '".CTRL_YEAR."', ";
        $query .= "     '".CTRL_SEMESTER."', ";
        $query .= "     CHAIR_GROUP_CD, ";
        $query .= "     TESTKINDCD, ";
        $query .= "     TESTITEMCD, ";
        $query .= "     CHAIRCD, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     SYSDATE() ";
        $query .= " FROM ";
        $query .= "     CHAIR_GROUP_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR || SEMESTER = '".$year_sem."' ";
        $query .= "     AND CHAIRCD IN (".knjz234Query::getChair().") ";
        $query .= " ) ";

        $db->query($query);

        $db->commit();

        Query::dbCheckIn($db);

        return;
    }

    //コピー元年学期取得
    function getMaxYearSem()
    {
        $query  =" SELECT DISTINCT ";
        $query .="     MAX(YEAR || SEMESTER) AS YEAR_SEM ";
        $query .=" FROM ";
        $query .="     CHAIR_GROUP_MST ";
        $query .=" WHERE ";
        $query .="     YEAR || SEMESTER < '".CTRL_YEAR.CTRL_SEMESTER."' ";

        return $query;
    }

    //マスタ新規SQL作成
    function makeInsChairMQuery($model)
    {
        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["SEMESTER"][TEXT]         = CTRL_SEMESTER;
        $data["CHAIR_GROUP_CD"][TEXT]   = $model->chair_group_cd;
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["CLASSCD"][TEXT]           = substr($model->subclasscd, 0, 2);
            $data["SCHOOL_KIND"][TEXT]       = substr($model->subclasscd, 3, 1);
            $data["CURRICULUM_CD"][TEXT]     = substr($model->subclasscd, 5, 1);
            $data["SUBCLASSCD"][TEXT]        = substr($model->subclasscd, 7, 6);
        } else {
            $data["SUBCLASSCD"][TEXT]        = $model->subclasscd;
        }
        $data["CHAIR_GROUP_NAME"][TEXT] = $model->chair_group_name;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $query = Query::insertSQL($data, "CHAIR_GROUP_MST");

        return $query;
    }

    //マスタ削除SQL作成
    function delChairMQuery($model)
    {
        $query  = "DELETE FROM ";
        $query .= "    CHAIR_GROUP_MST ";
        $query .= "WHERE ";
        $query .="     YEAR = '".CTRL_YEAR."' ";
        $query .="     AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .="     AND CHAIR_GROUP_CD = '".$model->chair_group_cd."' ";

        return $query;
    }

    //グループデータ追加
    function &chairGInstQuery(&$db, $select, $model)
    {
        //グループ新規
        $data = array();
        $selectdata = explode(",", $select);
        for ($i = 0; $i < get_count($selectdata); $i++) {
            if (!$selectdata[$i]) {
                continue;
            }
            $db->query(knjz234Query::makeInsChairGQuery($model, $selectdata[$i]));
        }
        return;
    }

    //データ新規SQL作成
    function makeInsChairGQuery($model, $selectdata)
    {
        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["SEMESTER"][TEXT]         = CTRL_SEMESTER;
        $data["CHAIR_GROUP_CD"][TEXT]   = $model->chair_group_cd;
        $data["TESTKINDCD"][TEXT]       = substr($model->test_cd, 0, 2);
        $data["TESTITEMCD"][TEXT]       = substr($model->test_cd, 2, 2);
        $data["CHAIRCD"][TEXT]          = $selectdata;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $query = Query::insertSQL($data, "CHAIR_GROUP_DAT");

        return $query;
    }

    //データ削除SQL作成
    function delChairGQuery($model)
    {
        $query  = "DELETE FROM ";
        $query .= "    CHAIR_GROUP_DAT ";
        $query .= "WHERE ";
        $query .="     YEAR = '".CTRL_YEAR."' ";
        $query .="     AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .="     AND CHAIR_GROUP_CD = '".$model->chair_group_cd."' ";
        $query .="     AND TESTKINDCD || TESTITEMCD = '".$model->test_cd."' ";

        return $query;
    }
}
?>
