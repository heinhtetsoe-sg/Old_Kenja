<?php
class knje372mquery extends Query {

    //更新処理
    function getUpdateQuery($db, $model) {

        $torokuDate = str_replace("/", "-", $model->field["TOROKU_DATE"]);
        // 更新対象データを取得
        $query = knje372mQuery::getExecDataSql($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //存在チェック
            $query = knje372mQuery::selectAftGradCourse($model, $row["SCHREGNO"], $row["SCHOOL_CD"], $row["FACULTYCD"], $row["DEPARTMENTCD"]);
            $gradRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if ($gradRow) {
                //更新
                $data = array();
                $data["JUKEN_HOWTO"][TEXT] = "04"; //受験方法 04:指定校推薦
                $data["DECISION"][TEXT]    = "1"; //受験結果 1:合格
                //専願の場合は"1"固定
                if ($row["DEPARTMENT_CD"] < "50") {
                    $data["PLANSTAT"][TEXT]   = "1"; //進路状況 1:0決定
                }
                $data["STAT_KIND"][TEXT]   = "0"; //0固定
                $data["SENKOU_KIND"][TEXT] = "0"; //0固定
                $data["REGISTERCD"][TEXT] = STAFFCD;
                $data["UPDATED"][FUNC]    = "sysdate()";

                $where  = " WHERE YEAR = '".$row["YEAR"]."' ";
                $where .= "   AND SEQ  = ".$gradRow["SEQ"]." ";
                $query = Query::updateSQL($data, "AFT_GRAD_COURSE_DAT", $where);
                $db->query($query);
            } else {
                $query = knje372mQuery::selectAftGradCourseSeqMax($model);
                $seqMax = $db->getOne($query);
                //追加
                $data = array();
                $data["YEAR"][TEXT]         = CTRL_YEAR;  //年度
                $data["SEQ"][TEXT]          = $seqMax + 1;    //SEQ
                $data["SCHREGNO"][TEXT]     = $row["SCHREGNO"];
                $data["TOROKU_DATE"][TEXT]  = $torokuDate;

                $data["STAT_CD"][TEXT]      = $row["SCHOOL_CD"];
                $data["FACULTYCD"][TEXT]    = $row["FACULTYCD"];
                $data["DEPARTMENTCD"][TEXT] = $row["DEPARTMENTCD"];

                $data["JUKEN_HOWTO"][TEXT]  = "04";   //受験方法 04:指定校推薦
                $data["DECISION"][TEXT]     = "1";    //受験結果 1:合格
                //専願の場合は"1"固定
                if ($row["DEPARTMENT_CD"] < "50") {
                    $data["PLANSTAT"][TEXT] = "1";    //進路状況 1:0決定
                }
                $data["STAT_KIND"][TEXT]    = "0";    //0固定
                $data["SENKOU_KIND"][TEXT]  = "0";    //0固定
                $data["REGISTERCD"][TEXT] = STAFFCD;
                $data["UPDATED"][FUNC]    = "sysdate()";

                $query = Query::insertSQL($data, "AFT_GRAD_COURSE_DAT");
                $db->query($query);
            }

        }
        $result->free();
    }

    //進路情報(専願者) 取得SQL
    function getExecDataSql($model) {
        $torokuDate = str_replace("/", "-", $model->field["TOROKU_DATE"]);

        $query .= " WITH DEP_S AS ( ";
        $query .= "   SELECT ";
        $query .= "       T1.YEAR ";
        $query .= "     , T1.SCHREGNO ";
        $query .= "     , T2.DEPARTMENT_S DEPARTMENT_CD ";
        $query .= "     , T2.SCHOOL_CD ";
        $query .= "     , T2.FACULTYCD ";
        $query .= "     , T2.DEPARTMENTCD ";
        $query .= "     , T2.FACULTY_NAME ";
        $query .= "   FROM ";
        $query .= "     AFT_SCHREG_CONVERT_SCORE_DAT T1 ";
        $query .= "     INNER JOIN AFT_RECOMMENDATION_LIMIT_MST T2 ";
        $query .= "       ON T2.YEAR = T1.YEAR ";
        $query .= "       AND T2.DEPARTMENT_S = T1.RECOMMENDATION_DEPARTMENT_CD ";
        $query .= "   WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " ) ";
        $query .= " , DEP_H AS ( ";
        $query .= "   SELECT ";
        $query .= "       T1.YEAR ";
        $query .= "     , T1.SCHREGNO ";
        $query .= "     , T2.DEPARTMENT_H DEPARTMENT_CD ";
        $query .= "     , T2.SCHOOL_CD ";
        $query .= "     , T2.FACULTYCD ";
        $query .= "     , T2.DEPARTMENTCD ";
        $query .= "     , T2.FACULTY_NAME ";
        $query .= "   FROM ";
        $query .= "     AFT_SCHREG_CONVERT_SCORE_DAT T1 ";
        $query .= "     INNER JOIN AFT_RECOMMENDATION_LIMIT_MST T2 ";
        $query .= "       ON T2.YEAR = T1.YEAR ";
        $query .= "       AND T2.DEPARTMENT_H = T1.RECOMMENDATION_DEPARTMENT_CD ";
        $query .= "   WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " ) ";
        $query .= " SELECT * FROM DEP_S ";
        $query .= " UNION ";
        $query .= " SELECT * FROM DEP_H ";

        return $query;
    }

    //進路データ存在チェック
    function selectAftGradCourse($model, $schregNo, $schoolCd, $facultyCd, $departmentCd) {
        $torokuDate = str_replace("/", "-", $model->field["TOROKU_DATE"]);

        $query  = " SELECT ";
        $query .= "   YEAR ";
        $query .= "   , MAX(SEQ) SEQ ";
        $query .= "   , SCHREGNO ";
        $query .= " FROM ";
        $query .= "   AFT_GRAD_COURSE_DAT ";
        $query .= " WHERE ";
        $query .= "   YEAR = '".CTRL_YEAR."' ";
        $query .= "   AND TOROKU_DATE = '".$torokuDate."' ";
        $query .= "   AND SCHREGNO = '{$schregNo}' ";
        $query .= "   AND STAT_CD = '{$schoolCd}' ";
        $query .= "   AND FACULTYCD = '{$facultyCd}' ";
        $query .= "   AND DEPARTMENTCD = '{$departmentCd}' ";
        $query .= " GROUP BY ";
        $query .= "   YEAR ";
        $query .= "   , SCHREGNO ";

        return $query;
    }

    //最大SEQ取得
    function selectAftGradCourseSeqMax($model) {

        $query  = " SELECT ";
        $query .= "   MAX(SEQ) SEQ ";
        $query .= " FROM ";
        $query .= "   AFT_GRAD_COURSE_DAT ";
        $query .= " WHERE ";
        $query .= "   YEAR = '".CTRL_YEAR."' ";
        $query .= " GROUP BY ";
        $query .= "   YEAR ";

        return $query;
    }

}
?>
