<?php

require_once('for_php7.php');

class knjz181aQuery extends Query {
    //学年コンボ
    function getGrade() {
        $year = CTRL_YEAR;

        $query  = " SELECT ";
        $query .= "     GRADE_NAME1 AS LABEL, ";
        $query .= "     GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR = '{$year}' ";
        $query .= "     AND (SCHOOL_KIND = 'J' OR SCHOOL_KIND = 'H') ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //データ種別コンボ
    function getSemester() {
        $year = CTRL_YEAR;

        $query  = " SELECT ";
        $query .= "     NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR        = '{$year}' ";
        $query .= "     AND NAMECD1     = 'H308' ";
        $query .= "     AND NAMESPARE1  = '1' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //テスト名称コンボ
    function getTestkindcd($semester) {
        $query  = " SELECT ";
        $query .= "     MOCKNAME1 AS LABEL, ";
        $query .= "     MOCKCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     MOCK_MST ";
        $query .= " WHERE ";
        $query .= "     SUBSTR(MOCKCD, 1, 1) = '{$semester}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //クラスコンボ
    function getHrClass($model) {
        $year = CTRL_YEAR;
        $schregSemester = CTRL_SEMESTER;

        $query  = " SELECT ";
        $query .= "     HR_NAME AS LABEL, ";
        $query .= "     HR_CLASS AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '{$year}' ";
        $query .= "     AND SEMESTER = '{$schregSemester}' ";
        $query .= "     AND GRADE    = '{$model->field["GRADE"]}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //課程学科コースコンボ
    function getCourse($model) {
        $year = CTRL_YEAR;
        $schregSemester = CTRL_SEMESTER;

        $query  = " SELECT DISTINCT ";
        $query .= "     L3.COURSENAME || L3.MAJORNAME || L4.COURSECODENAME AS LABEL, ";
        $query .= "     T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN V_COURSE_MAJOR_MST L3 ";
        $query .= "         ON  L3.YEAR = T1.YEAR ";
        $query .= "         AND L3.COURSECD = T1.COURSECD ";
        $query .= "         AND L3.MAJORCD = T1.MAJORCD ";
        $query .= "     LEFT JOIN V_COURSECODE_MST L4 ";
        $query .= "         ON  L4.YEAR = T1.YEAR ";
        $query .= "         AND L4.COURSECODE = T1.COURSECODE ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '{$year}' ";
        $query .= "     AND T1.SEMESTER = '{$schregSemester}' ";
        $query .= "     AND T1.GRADE    = '{$model->field["GRADE"]}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目コンボ
    function getSubclasscd($model) {
        $query  = " SELECT ";
        $query .= "     MOCK_SUBCLASS_CD || ':' || SUBCLASS_NAME AS LABEL, ";
        $query .= "     MOCK_SUBCLASS_CD AS VALUE ";
        $query .= " FROM ";
        $query .= "     MOCK_SUBCLASS_MST ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= " WHERE ";
	        //教育課程対応
	        if ($model->Properties["useCurriculumcd"] == '1') {
	            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD IN ( ";
            } else {
	            $query .= "     CLASSCD IN ( ";
            }
            $query .= "         SELECT ";
	        //教育課程対応
	        if ($model->Properties["useCurriculumcd"] == '1') {
	            $query .= "             T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD";
            } else {
	            $query .= "             SUBSTR(T2.SUBCLASSCD, 1, 2) AS CLASSCD ";
            }
            $query .= "         FROM ";
            $query .= "             CHAIR_STF_DAT T1, ";
            $query .= "             CHAIR_DAT T2 ";
            $query .= "         WHERE ";
            $query .= "             T1.YEAR = '".CTRL_YEAR."' ";
            $query .= "             AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "             AND T1.STAFFCD = '".STAFFCD."' ";
            $query .= "             AND T2.YEAR = T1.YEAR ";
            $query .= "             AND T2.SEMESTER = T1.SEMESTER ";
            $query .= "             AND T2.CHAIRCD = T1.CHAIRCD ";
            $query .= "         GROUP BY ";
	        //教育課程対応
	        if ($model->Properties["useCurriculumcd"] == '1') {
	            $query .= "             T2.CLASSCD, ";
	            $query .= "             T2.SCHOOL_KIND, ";
	            $query .= "             T2.CURRICULUM_CD, ";
	            $query .= "             T2.SUBCLASSCD ";
            } else {
	            $query .= "             SUBSTR(T2.SUBCLASSCD, 1, 2) ";
            }
            $query .= "     ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //段階数
    function getAssessLevelCnt($model) {
        $year = CTRL_YEAR;

        $query  = " SELECT ";
        $query .= "     COUNT(T1.ASSESSLEVEL) AS ASSESSLEVELCNT ";
        $query .= " FROM ";
        $query .= "     MOCK_ASSESS_LEVEL_MST T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '{$year}' ";
        $query .= "     AND T1.MOCKCD   = '{$model->field["MOCKCD"]}' ";
        $query .= "     AND T1.DIV      = '{$model->field["DIV"]}' ";
        $query .= "     AND T1.GRADE    = '{$model->field["GRADE"]}' ";
        //表示切替(1:学年 2:クラス 3:コース)
        if ($model->field["DIV"] == '2') {
            $query .= "     AND T1.HR_CLASS = '{$model->field["HR_CLASS"]}' ";
        } else if ($model->field["DIV"] == '3') {
            $query .= "     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = '{$model->field["COURSE"]}' ";
        }
        $query .= "     AND T1.MOCK_SUBCLASS_CD = '{$model->field["MOCK_SUBCLASS_CD"]}' ";

        return $query;
    }

    //平均点・最低点・最高点を取得(計算用)・・・序列確定後
    function getRecordAverage($model) {
        $year = CTRL_YEAR;

        $query  = " SELECT ";
        $query .= "     SMALLINT(ROUND(T1.AVG_KANSAN,0)) AS AVG, ";
        $query .= "     T1.LOWSCORE_KANSAN AS LOWSCORE, ";
        $query .= "     T1.HIGHSCORE_KANSAN AS HIGHSCORE ";
        $query .= " FROM ";
        $query .= "     MOCK_AVERAGE_DAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '{$year}' ";
        $query .= "     AND T1.MOCKCD   = '{$model->field["MOCKCD"]}' ";
        $query .= "     AND T1.AVG_DIV  = '{$model->field["DIV"]}' ";
        $query .= "     AND T1.GRADE    = '{$model->field["GRADE"]}' ";
        //表示切替(1:学年 2:クラス 3:コース)
        if ($model->field["DIV"] == '2') {
            $query .= "     AND T1.HR_CLASS = '{$model->field["HR_CLASS"]}' ";
        } else if ($model->field["DIV"] == '3') {
            $query .= "     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = '{$model->field["COURSE"]}' ";
        }
        $query .= "     AND T1.MOCK_SUBCLASS_CD = '{$model->field["MOCK_SUBCLASS_CD"]}' ";

        return $query;
    }

    //リスト取得
    function getList($model) {
        $year = CTRL_YEAR;

        $query  = " SELECT ";
        $query .= "     T1.ASSESSLEVEL, ";
        $query .= "     T1.ASSESSLOW, ";
        $query .= "     T1.ASSESSHIGH ";
        $query .= " FROM ";
        $query .= "     MOCK_ASSESS_LEVEL_MST T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '{$year}' ";
        $query .= "     AND T1.MOCKCD   = '{$model->field["MOCKCD"]}' ";
        $query .= "     AND T1.DIV      = '{$model->field["DIV"]}' ";
        $query .= "     AND T1.GRADE    = '{$model->field["GRADE"]}' ";
        //表示切替(1:学年 2:クラス 3:コース)
        if ($model->field["DIV"] == '2') {
            $query .= "     AND T1.HR_CLASS = '{$model->field["HR_CLASS"]}' ";
        } else if ($model->field["DIV"] == '3') {
            $query .= "     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = '{$model->field["COURSE"]}' ";
        }
        $query .= "     AND T1.MOCK_SUBCLASS_CD = '{$model->field["MOCK_SUBCLASS_CD"]}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.ASSESSLEVEL DESC ";

        return $query;
    }

    //学年名(照会)
    function getGradeName($model) {
        $year = CTRL_YEAR;

        $query  = " SELECT ";
        $query .= "     GRADE_NAME1 AS GRADE_NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '{$year}' ";
        $query .= "     AND GRADE    = '{$model->field["GRADE"]}' ";

        return $query;
    }

    //テスト名称(照会)
    function getTestName($model) {
        $query  = " SELECT ";
        $query .= "     T2.NAME1 || '　' || T1.MOCKNAME1 AS TEST_NAME ";
        $query .= " FROM ";
        $query .= "     MOCK_MST T1 ";
        $query .= "     INNER JOIN NAME_MST T2 ";
        $query .= "         ON  T2.NAMECD1  = 'H308' ";
        $query .= "         AND T2.NAMECD2  = SUBSTR(T1.MOCKCD, 1, 1) ";
        $query .= " WHERE ";
        $query .= "     T1.MOCKCD   = '{$model->field["MOCKCD"]}' ";

        return $query;
    }

    //リスト取得(照会)
    function getListInquiry($model) {
        $year = CTRL_YEAR;
        $schregSemester = CTRL_SEMESTER;

        $query  = " WITH T_ASSESS_LEVEL AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.YEAR, ";
        $query .= "         T1.MOCKCD, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.DIV, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.COURSECD, ";
        $query .= "         T1.MAJORCD, ";
        $query .= "         T1.COURSECODE, ";
        $query .= "         T1.MOCK_SUBCLASS_CD, ";
        $query .= "         COUNT(T1.ASSESSLEVEL) AS CNT_DANKAI ";
        $query .= "     FROM ";
        $query .= "         MOCK_ASSESS_LEVEL_MST T1 ";
        $query .= "     WHERE ";
        $query .= "             T1.YEAR     = '{$year}' ";
        $query .= "         AND T1.MOCKCD   = '{$model->field["MOCKCD"]}' ";
        $query .= "         AND T1.DIV      = '{$model->field["DIV"]}' ";
        $query .= "         AND T1.GRADE    = '{$model->field["GRADE"]}' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND T1.MOCK_SUBCLASS_CD IN ( ";
            $query .= "         SELECT ";
            $query .= "             M1.VALUE ";
            $query .= "         FROM ( ";
            $query .= knjz181aQuery::getSubclasscd($model);
            $query .= "         ) M1 ";
            $query .= "     ) ";
        }
        $query .= "     GROUP BY ";
        $query .= "         T1.YEAR, ";
        $query .= "         T1.MOCKCD, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.DIV, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.COURSECD, ";
        $query .= "         T1.MAJORCD, ";
        $query .= "         T1.COURSECODE, ";
        $query .= "         T1.MOCK_SUBCLASS_CD ";
        $query .= "     ) ";

        $query .= " SELECT ";
        $query .= "     SUBSTR(T1.MOCKCD, 1, 1) AS DATA_DIV, ";
        $query .= "     S1.NAME1 AS DATA_DIV_NAME, ";
        $query .= "     T1.MOCKCD, ";
        $query .= "     S2.MOCKNAME1 AS MOCK_NAME, ";
        $query .= "     T1.GRADE, ";
        $query .= "     L1.GRADE_NAME1 AS GRADE_NAME, ";
        $query .= "     T1.DIV, ";
        $query .= "     CASE WHEN T1.DIV = '1' THEN '学年' ";
        $query .= "          WHEN T1.DIV = '2' THEN 'クラス' ";
        $query .= "          WHEN T1.DIV = '3' THEN 'コース' ";
        $query .= "          ELSE NULL END AS DIV_NAME, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     L2.HR_NAME, ";
        $query .= "     T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS COURSE, ";
        $query .= "     L3.COURSENAME || L3.MAJORNAME || L4.COURSECODENAME AS COURSE_NAME, ";
        $query .= "     T1.MOCK_SUBCLASS_CD, ";
        $query .= "     L5.SUBCLASS_NAME, ";
        $query .= "     T1.CNT_DANKAI ";
        $query .= " FROM ";
        $query .= "     T_ASSESS_LEVEL T1 ";
        $query .= "     LEFT JOIN NAME_MST S1 ";
        $query .= "         ON  S1.NAMECD1  = 'H308' ";
        $query .= "         AND S1.NAMECD2  = SUBSTR(T1.MOCKCD, 1, 1) ";
        $query .= "     LEFT JOIN MOCK_MST S2 ";
        $query .= "         ON  S2.MOCKCD = T1.MOCKCD ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT L1 ";
        $query .= "         ON  L1.YEAR = T1.YEAR ";
        $query .= "         AND L1.GRADE = T1.GRADE ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT L2 ";
        $query .= "         ON  L2.YEAR = T1.YEAR ";
        $query .= "         AND L2.SEMESTER = '{$schregSemester}' ";
        $query .= "         AND L2.GRADE = T1.GRADE ";
        $query .= "         AND L2.HR_CLASS = T1.HR_CLASS ";
        $query .= "     LEFT JOIN V_COURSE_MAJOR_MST L3 ";
        $query .= "         ON  L3.YEAR = T1.YEAR ";
        $query .= "         AND L3.COURSECD = T1.COURSECD ";
        $query .= "         AND L3.MAJORCD = T1.MAJORCD ";
        $query .= "     LEFT JOIN V_COURSECODE_MST L4 ";
        $query .= "         ON  L4.YEAR = T1.YEAR ";
        $query .= "         AND L4.COURSECODE = T1.COURSECODE ";
        $query .= "     LEFT JOIN MOCK_SUBCLASS_MST L5 ";
        $query .= "         ON  L5.MOCK_SUBCLASS_CD = T1.MOCK_SUBCLASS_CD ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.DIV, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE, ";
        $query .= "     T1.MOCK_SUBCLASS_CD ";

        return $query;
    }


    //Insert作成
    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //変数
        $year = CTRL_YEAR;

        //削除
        $query  = " DELETE FROM ";
        $query .= "     MOCK_ASSESS_LEVEL_MST T1 ";
        $query .= " WHERE  ";
        $query .= "         T1.YEAR     = '{$year}' ";
        $query .= "     AND T1.MOCKCD   = '{$model->field["MOCKCD"]}' ";
        $query .= "     AND T1.DIV      = '{$model->field["DIV"]}' ";
        $query .= "     AND T1.GRADE    = '{$model->field["GRADE"]}' ";
        //表示切替(1:学年 2:クラス 3:コース)
        if ($model->field["DIV"] == '2') {
            $query .= "     AND T1.HR_CLASS = '{$model->field["HR_CLASS"]}' ";
        } else if ($model->field["DIV"] == '3') {
            $query .= "     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = '{$model->field["COURSE"]}' ";
        }
        $query .= "     AND T1.MOCK_SUBCLASS_CD = '{$model->field["MOCK_SUBCLASS_CD"]}' ";
        $db->query($query);

        //追加
        for ($i = $this->field["ASSESSLEVELCNT"]; $i > 0; $i--) {
            $data = array();
            $data["YEAR"][TEXT]             = $year;
            $data["MOCKCD"][TEXT]           = $model->field["MOCKCD"];
            $data["MOCK_SUBCLASS_CD"][TEXT] = $model->field["MOCK_SUBCLASS_CD"];
            $data["DIV"][TEXT]              = $model->field["DIV"];
            $data["GRADE"][TEXT]            = $model->field["GRADE"];
            $data["HR_CLASS"][TEXT]         = strlen($model->field["HR_CLASS"]) ? $model->field["HR_CLASS"]           : "000";
            $data["COURSECD"][TEXT]         = strlen($model->field["COURSE"])   ? substr($model->field["COURSE"],0,1) : "0";
            $data["MAJORCD"][TEXT]          = strlen($model->field["COURSE"])   ? substr($model->field["COURSE"],1,3) : "000";
            $data["COURSECODE"][TEXT]       = strlen($model->field["COURSE"])   ? substr($model->field["COURSE"],4,4) : "0000";

            $data["ASSESSLEVEL"][NUMBER]    = $i;
            $data["ASSESSLOW"][NUMBER]      = $model->field["ASSESSLOW" .$i];
            if ($i == $this->field["ASSESSLEVELCNT"]) {
//                $data["ASSESSHIGH"][NUMBER] = 100;
                $data["ASSESSHIGH"][NUMBER] = $model->field["ASSESSHIGH".$i];
            } else {
                $assesshigh = "ASSESSLOW".($i + 1);
                $data["ASSESSHIGH"][NUMBER] = ($model->field[$assesshigh] - 1);
            }

            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            $query = Query::insertSQL($data, "MOCK_ASSESS_LEVEL_MST");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);

        return;
    }

    //コピー時の存在チェック用
    function getCntCopyQuery($model, $copycd) {
        //変数
        $year = CTRL_YEAR;
        //SQL
        $query  = " SELECT COUNT(*) FROM ";
        $query .= "     MOCK_ASSESS_LEVEL_MST T1 ";
        $query .= " WHERE  ";
        $query .= "         T1.YEAR     = '{$year}' ";
        $query .= "     AND T1.MOCKCD   = '{$copycd}' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND T1.MOCK_SUBCLASS_CD IN ( ";
            $query .= "         SELECT ";
            $query .= "             M1.VALUE ";
            $query .= "         FROM ( ";
            $query .= knjz181aQuery::getSubclasscd($model);
            $query .= "         ) M1 ";
            $query .= "     ) ";
        }
        return $query;
    }

    //コピー
    function getCopyQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //変数
        $year = CTRL_YEAR;

        //削除
        $query  = " DELETE FROM ";
        $query .= "     MOCK_ASSESS_LEVEL_MST T1 ";
        $query .= " WHERE  ";
        $query .= "         T1.YEAR     = '{$year}' ";
        $query .= "     AND T1.MOCKCD   = '{$model->field["MOCKCD"]}' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND T1.MOCK_SUBCLASS_CD IN ( ";
            $query .= "         SELECT ";
            $query .= "             M1.VALUE ";
            $query .= "         FROM ( ";
            $query .= knjz181aQuery::getSubclasscd($model);
            $query .= "         ) M1 ";
            $query .= "     ) ";
        }
        $db->query($query);

        //COPY
        $query  = " INSERT INTO MOCK_ASSESS_LEVEL_MST ";
        $query .= " SELECT ";
        $query .= "     '{$year}', ";
        $query .= "     '{$model->field["MOCKCD"]}', ";
        $query .= "     T1.MOCK_SUBCLASS_CD, ";
        $query .= "     T1.DIV, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE, ";
        $query .= "     T1.ASSESSLEVEL, ";
        $query .= "     T1.ASSESSMARK, ";
        $query .= "     T1.ASSESSLOW, ";
        $query .= "     T1.ASSESSHIGH, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     MOCK_ASSESS_LEVEL_MST T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '{$year}' ";
        $query .= "     AND T1.MOCKCD   = '{$model->field["PRE_MOCKCD"]}' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND T1.MOCK_SUBCLASS_CD IN ( ";
            $query .= "         SELECT ";
            $query .= "             M1.VALUE ";
            $query .= "         FROM ( ";
            $query .= knjz181aQuery::getSubclasscd($model);
            $query .= "         ) M1 ";
            $query .= "     ) ";
        }

        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }
}
?>
