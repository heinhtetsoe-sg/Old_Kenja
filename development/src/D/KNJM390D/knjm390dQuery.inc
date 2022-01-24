<?php
class knjm390dQuery extends Query {

    //学期コンボ
    function getSemecmb() {
        $query  = " SELECT ";
        $query .= "     SEMESTERNAME AS LABEL, ";
        $query .= "     SEMESTER AS VALUE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR      = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //講座リスト
    function getChrSubCd($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.CHAIRNAME AS LABEL, ";
        $query .= "     T1.CHAIRCD || '-' || T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1 ";
        //参照・更新可（制限付き）
        if (AUTHORITY != DEF_UPDATABLE) {
            $query .= "     INNER JOIN CHAIR_STF_DAT S1 ON S1.YEAR = T1.YEAR ";
            $query .= "           AND S1.SEMESTER = T1.SEMESTER ";
            $query .= "           AND S1.CHAIRCD  = T1.CHAIRCD ";
            $query .= "           AND S1.STAFFCD  = '" .STAFFCD ."' ";
        }
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".$model->semester."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //出席回数データ取得
    function getSchAttendSemesDat($model) {
        $chrsub_array = array();
        $chrsub_array = explode("-", $model->chairCd);

        $query  = " WITH CHAIR_STD AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.YEAR, ";
        $query .= "         T1.SEMESTER, ";
        $query .= "         T1.CHAIRCD, ";
        $query .= "         T1.CLASSCD, ";
        $query .= "         T1.SCHOOL_KIND, ";
        $query .= "         T1.CURRICULUM_CD, ";
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         T2.SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         CHAIR_DAT T1 ";
        $query .= "         INNER JOIN CHAIR_STD_DAT T2 ON T2.YEAR     = T1.YEAR ";
        $query .= "                                    AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "                                    AND T2.CHAIRCD  = T1.CHAIRCD ";
        $query .= "     WHERE ";
        $query .= "             T1.YEAR          = '".CTRL_YEAR."' ";
        $query .= "         AND T1.SEMESTER      = '".$model->semester."' ";
        $query .= "         AND T1.CHAIRCD       = '".$chrsub_array[0]."' ";
        $query .= "         AND T1.CLASSCD       = '".$chrsub_array[1]."' ";
        $query .= "         AND T1.SCHOOL_KIND   = '".$chrsub_array[2]."' ";
        $query .= "         AND T1.CURRICULUM_CD = '".$chrsub_array[3]."' ";
        $query .= "         AND T1.SUBCLASSCD    = '".$chrsub_array[4]."' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     MAIN.SCHREGNO, ";
        $query .= "     HDAT.HR_NAME, ";
        $query .= "     BASE.NAME_SHOW, ";
        $query .= "     SEME.SCHOOLIN_CNT ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD MAIN ";
        $query .= "     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = MAIN.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = MAIN.SCHREGNO ";
        $query .= "                                    AND REGD.YEAR     = MAIN.YEAR ";
        $query .= "                                    AND REGD.SEMESTER = MAIN.SEMESTER ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT HDAT ON HDAT.YEAR     = REGD.YEAR ";
        $query .= "                                     AND HDAT.SEMESTER = REGD.SEMESTER ";
        $query .= "                                     AND HDAT.GRADE    = REGD.GRADE ";
        $query .= "                                     AND HDAT.HR_CLASS = REGD.HR_CLASS ";
        $query .= "     LEFT JOIN SCH_ATTEND_SEMES_DAT SEME ON SEME.YEAR     = MAIN.YEAR ";
        $query .= "                                        AND SEME.SEMESTER = MAIN.SEMESTER ";
        $query .= "                                        AND SEME.SCHREGNO = MAIN.SCHREGNO ";
        $query .= "                                        AND SEME.CHAIRCD  = MAIN.CHAIRCD ";
        //クラス番号順の場合はクラスまで指定する
        if ($model->order == 2) {
            $query .= " ORDER BY ";
            $query .= "     REGD.GRADE, ";
            $query .= "     REGD.HR_CLASS, ";
            $query .= "     REGD.ATTENDNO ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     MAIN.SCHREGNO ";
        }

        return $query;
    }

    /**************/
    /** 更新処理 **/
    /**************/
    /* データ更新処理 */
    function updateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $chrsub_array = array();
        $chrsub_array = explode("-", $model->chairCd);

        $query  = " DELETE FROM SCH_ATTEND_SEMES_DAT ";
        $query .= "       WHERE YEAR     = '".CTRL_YEAR."' ";
        $query .= "         AND SEMESTER = '".$model->semester."' ";
        $query .= "         AND CHAIRCD  = '".$chrsub_array[0]."' ";

        $db->query($query);

        for ($i = 0; $i < count($model->data["SCHREGNO"]); $i++) {
            $schregNo = $model->data["SCHREGNO"][$i];

            $data = array();
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["SEMESTER"][TEXT]         = $model->semester;
            $data["SCHREGNO"][TEXT]         = $schregNo;
            $data["CHAIRCD"][TEXT]          = $chrsub_array[0];
            $data["SCHOOLIN_CNT"][NUMBER]   = $model->fields["SCHOOLIN_CNT"][$schregNo];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $query = Query::insertSQL($data, "SCH_ATTEND_SEMES_DAT");
            $db->query($query);
        }

        $db->commit();      // トランザクションをコミットする。
        Query::dbCheckIn($db);

        return ;
    }
}
?>