<?php

require_once('for_php7.php');


class knje372fquery extends Query {

    // 名称マスタ取得
    function getNameMst($nameCd1, $nameCd2) {
        $query  = " SELECT * ";
        $query .= " FROM NAME_MST ";
        $query .= " WHERE NAMECD1 = '{$nameCd1}' ";
        if ($nameCd2) {
            $query .= "   AND NAMECD2 = '{$nameCd2}' ";
        }
        return $query;
    }

    //年組取得
    function getHrClass($model, $year, $semester) {
        //参照・更新可
        $query  = " SELECT ";
        $query .= "     T1.GRADE || '-' || T1.HR_CLASS AS VALUE, ";
        $query .= "     T1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= " LEFT JOIN SCHREG_REGD_GDAT T2 ";
        $query .= "      ON T2.YEAR  = T1.YEAR ";
        $query .= "     AND T2.GRADE = T1.GRADE ";
        //卒業学年
        $query .= " LEFT JOIN V_NAME_MST N1 ";
        $query .= "      ON N1.YEAR  = T1.YEAR ";
        $query .= "     AND N1.NAMECD1 = 'A023' ";
        $query .= "     AND N1.NAME1 = T2.SCHOOL_KIND ";

        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$year."' ";
        $query .= "     AND T1.SEMESTER = '".$semester."' ";
        $query .= " AND T2.SCHOOL_KIND = 'H' ";
        $query .= " AND T1.GRADE BETWEEN N1.NAMESPARE2 AND N1.NAMESPARE3 ";

        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    // 実力科目マスタ
    function getProficiencySubclassMst($subclassCd) {
        $query  = " SELECT ";
        $query .= "     PROFICIENCY_SUBCLASS_CD VALUE ";
        $query .= "   , SUBCLASS_ABBV LABEL ";
        $query .= " FROM PROFICIENCY_SUBCLASS_MST ";
        $query .= " WHERE PROFICIENCY_SUBCLASS_CD = '{$subclassCd}' ";

        return $query;
    }

    //順位の重複を取得
    function selectDuplicatRank() {

        $query  = " SELECT CONVERT_RANK ";
        $query .= " FROM AFT_SCHREG_CONVERT_SCORE_DAT ";
        $query .= " WHERE YEAR = '".CTRL_YEAR."' ";
        $query .= " GROUP BY CONVERT_RANK ";
        $query .= " HAVING COUNT(CONVERT_RANK) > 1 ";

        return $query;
    }

    //データの取得
    function selectQuery($model) {

        $query  = "";
        $query .= " SELECT ";
        $query .= "   T1.YEAR ";
        $query .= "   , T1.SCHREGNO ";
        $query .= "   , REGD.GRADE ";
        $query .= "   , REGD.HR_CLASS ";
        $query .= "   , REGDH.HR_NAME ";
        $query .= "   , REGDH.HR_NAMEABBV ";
        $query .= "   , REGD.ATTENDNO ";
        $query .= "   , REGDM.NAME ";

        $query .= "   , T1.CONVERT_DEVIATION ";
        $query .= "   , T1.CONVERT_SCORE ";
        $query .= "   , (VALUE(T1.CONVERT_SCORE, 0) + VALUE(T1.ATTEND_ADJUSTMENT_SCORE, 0) + VALUE(T1.ADJUSTMENT_SCORE, 0)) CONVERT_TOTAL ";

        $query .= "   , T1.CONVERT_RANK ";

        $query .= "   , T1.ATTEND_ADJUSTMENT_SCORE ";
        $query .= "   , T1.ADJUSTMENT_SCORE ";
        $query .= "   , (VALUE(T1.ATTEND_ADJUSTMENT_SCORE, 0) + VALUE(T1.ADJUSTMENT_SCORE, 0)) ADJUST_TOTAL_SCORE ";

        $query .= "   , T1.PROFICIENCY1_SCORE1 ";
        $query .= "   , T1.PROFICIENCY1_SCORE2 ";
        $query .= "   , T1.PROFICIENCY1_SCORE3 ";
        $query .= "   , T1.PROFICIENCY1_AVG ";
        $query .= "   , T1.PROFICIENCY2_SCORE1 ";
        $query .= "   , T1.PROFICIENCY2_SCORE2 ";
        $query .= "   , T1.PROFICIENCY2_SCORE3 ";
        $query .= "   , T1.PROFICIENCY2_AVG ";
        $query .= "   , T1.GRADE1_TOTAL_SCORE ";
        $query .= "   , T1.GRADE2_TOTAL_SCORE ";
        $query .= "   , T1.GRADE3_TOTAL_SCORE ";
        $query .= "   , T1.TOTAL_AVG ";
        $query .= "   , T1.RECOMMENDATION_DEPARTMENT_CD ";
        $query .= "   , T2.ACTIVITY_CD ";
        $query .= "   , T2.ACTIVITY_CD || ':' || VALUE(N1.NAME1, '') ACTIVITY_NAME ";
        $query .= "   , T2.ACTIVITY_CONTENT ";
        $query .= "   , T2.DECLINE_FLG ";
        $query .= " FROM ";
        $query .= "   AFT_SCHREG_CONVERT_SCORE_DAT T1 ";
        $query .= "   LEFT JOIN AFT_SCHREG_RECOMMENDATION_INFO_DAT T2 ";
        $query .= "     ON T2.YEAR = T1.YEAR ";
        $query .= "     AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "   INNER JOIN SCHREG_REGD_DAT REGD ";
        $query .= "     ON REGD.SCHREGNO = T1.SCHREGNO ";
        $query .= "     AND REGD.YEAR = T1.YEAR ";
        $query .= "     AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "   INNER JOIN SCHREG_BASE_MST REGDM ";
        $query .= "     ON REGDM.SCHREGNO = T1.SCHREGNO ";
        $query .= "   LEFT JOIN SCHREG_REGD_HDAT REGDH ";
        $query .= "     ON REGDH.YEAR = REGD.YEAR ";
        $query .= "     AND REGDH.SEMESTER = REGD.SEMESTER ";
        $query .= "     AND REGDH.GRADE = REGD.GRADE ";
        $query .= "     AND REGDH.HR_CLASS = REGD.HR_CLASS ";
        $query .= "   LEFT JOIN NAME_MST N1 ";
        $query .= "     ON N1.NAMECD1 = 'E071' ";
        $query .= "     AND N1.NAMECD2 = T2.ACTIVITY_CD ";

        $query .= " WHERE ";
        $query .= "       T1.YEAR = '".CTRL_YEAR."' ";
        // $query .= "   AND REGD.GRADE = '' ";

        if ($model->field["HR_CLASS"] != "") {
            list($grade, $hrClass) = explode("-", $model->field["HR_CLASS"]);
            $query .= "   AND (REGD.GRADE, REGD.HR_CLASS) = ({$grade}, {$hrClass}) ";
        }

        //校内試験を2回受験
        $query .= "   AND (T1.PROFICIENCY1_SCORE1 IS NOT NULL OR T1.PROFICIENCY1_SCORE2 IS NOT NULL OR T1.PROFICIENCY1_SCORE3 IS NOT NULL ) ";
        $query .= "   AND (T1.PROFICIENCY2_SCORE1 IS NOT NULL OR T1.PROFICIENCY2_SCORE2 IS NOT NULL OR T1.PROFICIENCY2_SCORE3 IS NOT NULL ) ";
        //辞退区分
        $query .= "   AND VALUE(T2.DECLINE_FLG, '') <> '1' ";

        $query .= " ORDER BY ";
        if ($model->field["SORT_ITEM"] == "2") {
            $query .= "     REGD.GRADE ";
            $query .= "   , REGD.HR_CLASS ";
            $query .= "   , REGD.ATTENDNO ";
        } else {
            $query .= "     CONVERT_TOTAL DESC ";
            $query .= "   , T1.CONVERT_SCORE DESC ";
            $query .= "   , T1.CONVERT_RANK ";
            $query .= "   , T1.SCHREGNO ";
        }

        return $query;
    }

    /* データ更新処理 */
    function update($model) {
        $db = Query::dbCheckOut();

        foreach ($model->convertRank as $schregNo => $convertRank) {
            /*** AFT_SCHREG_CONVERT_SCORE_DAT ***/
            $data = array();

            $data["CONVERT_RANK"][NUMBER] = $convertRank;

            $data["REGISTERCD"][TEXT]     = STAFFCD ;
            $data["UPDATED"][FUNC]        = "sysdate()";

            //更新
            $where  = " WHERE YEAR = '".CTRL_YEAR."'";
            $where .= "   AND SCHREGNO = '{$schregNo}'";
            $query = Query::updateSQL($data, "AFT_SCHREG_CONVERT_SCORE_DAT", $where);
            $db->query($query);
        }

        Query::dbCheckIn($db);
    }

/*================================== CSV処理 ==================================*/

    function selectConvertScoreDat($schregNo) {
        $query  = "";
        $query .= " SELECT SCHREGNO ";
        $query .= " FROM AFT_SCHREG_CONVERT_SCORE_DAT ";
        $query .= " WHERE YEAR = '".CTRL_YEAR."' ";
        $query .= "   AND SCHREGNO = '".$schregNo."' ";

        return $query;
    }

    //データの取得
    function selectSmpleQuery($model) {

        $query  = "";
        $query .= " WITH SAMPLE_DAT( ";
        $query .= "     SCHREGNO ";
        $query .= "   , GRADE ";
        $query .= "   , HR_CLASS ";
        $query .= "   , ATTENDNO ";
        $query .= "   , NAME ";
        $query .= "   , ATTEND_ADJUSTMENT_SCORE ";
        $query .= "   , ADJUSTMENT_SCORE ";
        $query .= "   , ADJUST_TOTAL_SCORE ";
        $query .= "   , CONVERT_SCORE ";
        $query .= "   , CONVERT_TOTAL ";
        $query .= "   , CONVERT_RANK ";
        $query .= " ) AS ( ";
        $query .= "   VALUES ('".CTRL_YEAR."0001', '03', '001', '001', '氏名', 0, 0, 0, 100, 100, 1) ";
        $query .= " ) ";
        $query .= " SELECT * FROM SAMPLE_DAT ";

        return $query;
    }

    //エラーＤＢへの追加
    function insertCsvMsgPrgDat(&$db, $line, $check_error) {
        $data1 = array();
        $data1["PROGRAMID"][TEXT] = PROGRAMID;
        $data1["MSGROW"][NUMBER]  = $line;
        $data1["MSGREMARK"][TEXT] = (strlen($check_error) > 119) ? substr($check_error, 0, 119)." " : $check_error." ";

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $db->query($query);

        return;
    }
}
?>
