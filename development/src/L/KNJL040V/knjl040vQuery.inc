<?php
class knjl040vQuery extends Query
{

    //校種コンボ
    public function getSchoolKind($model)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || VALUE(ABBV1,'') AS LABEL, ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST T1 ";
        $query .= " WHERE ";
        $query .= "       YEAR    = '{$model->objYear}' ";
        $query .= "   AND NAMECD1 = 'L003' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //試験ID取得
    public function getExamId($model)
    {
        $query  = " SELECT ";
        $query .= "     EXAM_SCHOOL_KIND || APPLICANT_DIV || COURSE_DIV || FREQUENCY || ':' || EXAM_NAME AS LABEL, ";
        $query .= "     EXAM_SCHOOL_KIND || '-' || APPLICANT_DIV || '-' || COURSE_DIV || '-' || FREQUENCY AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_STD_MST ";
        $query .= " WHERE ";
        $query .= "       YEAR        = '{$model->objYear}' ";
        $query .= "   AND EXAM_SCHOOL_KIND = '{$model->examSchoolKind}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //名称マスタ取得
    public function getNameMst($namecd1, $namecd2 = "")
    {
        $query  = " SELECT ";
        if ($namecd2 != "") {
            $query .= "     NAME1 ";
        } else {
            $query .= "     NAME1 AS LABEL, ";
            $query .= "     NAMECD2 AS VALUE ";
        }
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "       NAMECD1 = '{$namecd1}' ";
        if ($namecd2 != "") {
            $query .= "   AND NAMECD2 = '{$namecd2}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //一覧表示
    public function selectQuery($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     RECEPT.RECEPTNO, ";
        $query .= "     BASE.EXAMNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     BASE.DEPOSIT AS DEPOSIT_CHECK, ";
        $query .= "     BASE.DEPOSIT_DATE, ";
        $query .= "     BASE.FEE AS FEE_CHECK, ";
        $query .= "     BASE.FEE_DATE, ";
        $query .= "     BASE.DECLINE AS DECLINE_CHECK, ";
        $query .= "     BASE.DECLINE_DATE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_STD_RECEPT_DAT RECEPT ";
        $query .= "     INNER JOIN ENTEXAM_STD_APPLICANTBASE_DAT BASE ";
        $query .= "            ON BASE.YEAR   = RECEPT.YEAR ";
        $query .= "           AND BASE.EXAMNO = RECEPT.EXAMNO ";
        $query .= " WHERE ";
        $query .= "       RECEPT.YEAR               = '{$model->objYear}' ";
        $query .= "   AND RECEPT.EXAM_SCHOOL_KIND   = '{$model->wkExamId[0]}' ";
        $query .= "   AND RECEPT.APPLICANT_DIV      = '{$model->wkExamId[1]}' ";
        $query .= "   AND RECEPT.COURSE_DIV         = '{$model->wkExamId[2]}' ";
        $query .= "   AND RECEPT.FREQUENCY          = '{$model->wkExamId[3]}' ";
        $query .= "   AND RECEPT.JUDGEMENT IN ('1','2') "; //1:合格 2:繰り上げ合格
        $query .= " ORDER BY ";
        $query .= "     RECEPT.RECEPTNO ";
        return $query;
    }

    //更新
    public function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach ($model->examnoArray as $key => $examno) {
            //更新 -- ENTEXAM_STD_APPLICANTBASE_DAT
            $data = array();
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";
            if ($model->declineCheckArray[$key] != "1") {
                //入学手続き金、入学金は辞退フラグが1以外の場合に更新
                $data["DEPOSIT"][TEXT]          = $model->depositCheckArray[$key];
                $data["DEPOSIT_DATE"][TEXT]     = str_replace("/", "-", $model->depositDateArray[$key]);
                $data["FEE"][TEXT]              = $model->feeCheckArray[$key];
                $data["FEE_DATE"][TEXT]         = str_replace("/", "-", $model->feeDateArray[$key]);
            }
            $data["DECLINE"][TEXT]          = $model->declineCheckArray[$key];
            $data["DECLINE_DATE"][TEXT]     = str_replace("/", "-", $model->declineDateArray[$key]);

            $enteringFlg = "";
            if ($model->feeCheckArray[$key] == "1") {
                //入学金フラグが1の場合、入学フラグに1をセット
                $enteringFlg = "1";
            } elseif ($model->declineCheckArray[$key] == "1") {
                //辞退フラグが1の場合、入学フラグにNULLをセット
                $enteringFlg = "";
            }
            $data["ENTERING_FLG"][TEXT] = $enteringFlg;

            //対象受験生
            $where  = " WHERE ";
            $where .= "       YEAR      = '{$model->objYear}' ";
            $where .= "   AND EXAMNO    = '{$examno}' ";
            $query = Query::updateSQL($data, "ENTEXAM_STD_APPLICANTBASE_DAT", $where);
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
