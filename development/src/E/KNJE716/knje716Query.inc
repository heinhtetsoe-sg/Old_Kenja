<?php

require_once('for_php7.php');

class knje716query extends Query
{
    //年組取得コンボ
    public function getGradeHrclassName()
    {
        $query  = "    SELECT SRH.GRADE || SRH.HR_CLASS AS VALUE ";
        $query .= "         , SRH.HR_NAME AS LABEL ";
        $query .= "      FROM SCHREG_REGD_HDAT SRH ";
        $query .= "     WHERE SRH.YEAR     = '" .CTRL_YEAR. "' ";
        $query .= "       AND SRH.SEMESTER = '" .CTRL_SEMESTER. "' ";
        $query .= "       AND SRH.GRADE    IN ('01', '02', '03') ";
        $query .= "  ORDER BY SRH.GRADE ";
        $query .= "         , SRH.HR_CLASS ";

        return $query;
    }

    //生徒のデータを取得
    public function getStudentDate($model)
    {
        $query  = "    SELECT SRD.SCHREGNO ";
        $query .= "         , SRD.ATTENDNO ";
        $query .= "         , SBM.NAME ";
        $query .= "         , IRSD.SLIPREMARK ";
        $query .= "      FROM SCHREG_REGD_DAT SRD ";
        $query .= " LEFT JOIN SCHREG_BASE_MST SBM ";
        $query .= "        ON SBM.SCHREGNO = SRD.SCHREGNO ";
        $query .= " LEFT JOIN INT_RECCOMENDATION_SLIPREMARK_DAT IRSD ";
        $query .= "        ON SRD.SCHREGNO = IRSD.SCHREGNO ";
        $query .= "     WHERE SRD.YEAR     = '" .CTRL_YEAR. "' ";
        $query .= "       AND SRD.SEMESTER = '" .CTRL_SEMESTER. "' ";
        $query .= "       AND SRD.GRADE || SRD.HR_CLASS = '" . $model->field["GRADE_HRCLASS_NAME"]. "' ";
        $query .= "  ORDER BY SRD.ATTENDNO ";

        return $query;
    }

    //生徒の所見が登録されてるかチェック
    public function recomendationSlipremarkCheck($schregno)
    {
        $query  = " SELECT COUNT(*) ";
        $query .= "   FROM INT_RECCOMENDATION_SLIPREMARK_DAT ";
        $query .= "  WHERE SCHREGNO = '{$schregno}' ";
        $query .= "    AND YEAR     = '" .CTRL_YEAR. "' ";

        return $query;
    }

    //所見更新
    public function update($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach ($model->field["SCHREGNO"] as $i => $schregno) {
            $slipremark = $model->slipremark[$i];

            //生徒の所見が登録されてるかチェック
            $query = knje716Query::recomendationSlipremarkCheck($schregno);

            if (0 < intval($db->getOne($query))) {
                //生徒の所見が存在したら更新する
                $where  = " WHERE SCHREGNO = '{$schregno}' ";
                $where .= "   AND YEAR     = '" .CTRL_YEAR. "' ";

                $data["SLIPREMARK"][TEXT] = $slipremark;
                $data["REGISTERCD"][TEXT] = STAFFCD;
                $data["UPDATED"][FUNC]    = "sysdate()";

                $query = Query::updateSQL($data, "INT_RECCOMENDATION_SLIPREMARK_DAT", $where);
            } else {
                //生徒の所見が存在しないと登録する
                $data["YEAR"][TEXT]       = CTRL_YEAR;
                $data["SCHREGNO"][TEXT]   = $schregno;
                $data["SLIPREMARK"][TEXT] = $slipremark;
                $data["REGISTERCD"][TEXT] = STAFFCD;
                $data["UPDATED"][FUNC]    = "sysdate()";

                $query = Query::insertSQL($data, "INT_RECCOMENDATION_SLIPREMARK_DAT");
            }
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }
}
