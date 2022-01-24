<?php

require_once('for_php7.php');


class knjd177Query extends Query
{

    //学期数を取得
    public function getCountSemester()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " AND SEMESTER <> '9' ";

        return $query;
    }

    //学期取得
    public function getSemester($getCountsemester)
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     CASE WHEN SEMESTER = '".$getCountsemester."' THEN SEMESTERNAME || '（評定なし）' ELSE SEMESTERNAME END AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得（権限チェック）
    public function getHrClass($year, $semester)
    {
        //参照・更新可
        $query  = " SELECT ";
        $query .= "     GRADE || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND SEMESTER = '".$semester."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (   TR_CD1 = '".STAFFCD."' ";
            $query .= "          OR TR_CD2 = '".STAFFCD."' ";
            $query .= "          OR TR_CD3 = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //テスト名取得(順位を印刷しない)
    public function getRecordMockOrderDat($model)
    {
        $year = CTRL_YEAR;
        $grade = substr($model->field["GRADE_HR_CLASS"], 0, 2);

        $query  = " SELECT ";
        $query .= "     CASE WHEN T1.TEST_DIV = '1' ";
        $query .= "          THEN '考査：' || L2.TESTITEMNAME ";
        $query .= "          ELSE CASE WHEN SUBSTR(T1.MOCKCD, 1, 1) = '1' ";
        $query .= "                    THEN '実力：' || L1.MOCKNAME2 ";
        $query .= "                    ELSE '模試：' || L1.MOCKNAME2 ";
        $query .= "               END ";
        $query .= "     END AS LABEL, ";
        $query .= "     T1.SEQ AS VALUE ";
        $query .= " FROM ";
        $query .= "     RECORD_MOCK_ORDER_DAT T1 ";
        $query .= " LEFT JOIN MOCK_MST L1 ON T1.MOCKCD = L1.MOCKCD ";
        $query .= " LEFT JOIN TESTITEM_MST_COUNTFLG_NEW L2 ON  L2.YEAR = T1.YEAR ";
        $query .= "                                        AND L2.SEMESTER = T1.SEMESTER ";
        $query .= "                                        AND L2.TESTKINDCD || L2.TESTITEMCD = T1.TESTKINDCD || T1.TESTITEMCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' AND ";
        $query .= "     T1.GRADE = '{$grade}' ";
        $query .= " FETCH FIRST 8 ROWS ONLY ";

        return $query;
    }

    //学期取得(順位を印刷しない)
    public function getSemesterMst()
    {
        $query  = " SELECT ";
        $query .= "     SEMESTERNAME AS LABEL, ";
        $query .= "     SEMESTER  AS VALUE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' ";

        return $query;
    }

    //対象生徒
    public function getStudent($model, $seme)
    {
        $query  = " SELECT ";
        $query .= "     T2.SCHREGNO, ";
        $query .= "     (CASE WHEN S1.SEND_ADDR1 IS NULL THEN '　' ELSE '☆' END) || T2.SCHREGNO AS SCHREGNO_SHOW, ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     T1.NAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1, ";
        $query .= "     SCHREG_REGD_DAT T2 ";
        $query .= "     LEFT JOIN ";
        $query .= "         SCHREG_SEND_ADDRESS_DAT S1 ON S1.SCHREGNO = T2.SCHREGNO AND S1.DIV = '1' ";
        $query .= " WHERE ";
        $query .= "     T2.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T2.SEMESTER = '".$seme."' ";
        $query .= "     AND T2.GRADE || T2.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= "     AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     T2.ATTENDNO ";

        return $query;
    }

    //対象外の生徒取得
    public function getSchnoIdou($model, $semester)
    {
        $date = str_replace("/", "-", $model->field["DATE"]);

        $query  = "";
        $query .= " SELECT T1.SCHREGNO, T1.ATTENDNO ";
        $query .= " FROM   SCHREG_REGD_DAT T1, SEMESTER_MST T2 ";
        $query .= " WHERE  T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "        T1.SEMESTER = '".$semester."' AND ";
        $query .= "        T1.YEAR = T2.YEAR AND ";
        $query .= "        T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "        T1.GRADE||T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ( ";
        //転学(2)・退学(3)者 但し異動日が学期終了日または異動基準日より小さい場合
        //転入(4)・編入(5)者 但し異動日が学期終了日または異動基準日より大きい場合
        $query .= "        EXISTS(SELECT 'X' FROM SCHREG_BASE_MST S1 ";
        $query .= "                WHERE S1.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "                    ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE <  ";
        $query .= "                         CASE WHEN T2.EDATE < '".$date."' ";
        $query .= "                              THEN T2.EDATE ELSE '".$date."' END) OR ";
        $query .= "                     (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE >  ";
        $query .= "                         CASE WHEN T2.EDATE < '".$date."' ";
        $query .= "                              THEN T2.EDATE ELSE '".$date."' END)) ) OR ";
        //留学(1)・休学(2)者
        $query .= "        EXISTS(SELECT 'X' FROM SCHREG_TRANSFER_DAT S1 ";
        $query .= "                WHERE S1.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "                      S1.TRANSFERCD IN ('1','2') AND  ";
        $query .= "                      CASE WHEN T2.EDATE < '".$date."' ";
        $query .= "                           THEN T2.EDATE ELSE '".$date."' END  ";
        $query .= "                      BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ) ";
        $query .= " ORDER BY T1.ATTENDNO ";

        return $query;
    }
}
