<?php

require_once('for_php7.php');

class knjd133tquery extends Query
{

    //学期取得
    public function getSemesterCmb($target = "")
    {
        $query  = " SELECT ";
        if ($target) {
            $query .= "     SEMESTERNAME ";
        } else {
            $query .= "     SEMESTER AS VALUE, ";
            $query .= "     SEMESTERNAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."' ";
        if ($target) {
            $query .= "     AND SEMESTER = '{$target}' ";
        } else {
            $query .= "     AND SEMESTER <> '9' ";
        }

        return $query;
    }

    public function getNameMstche($model)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     COUNT(*) AS COUNT";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = '$model->che_school_kind' ";

        return $query;
    }

    //科目データの取得
    public function selectSubclassQuery($model)
    {
        $namecd1 = ($model->count > 0) ? $model->che_school_kind : "D008";

        $query  = "    SELECT DISTINCT ";
        $query .= "      VSUB.CLASSCD || '-' || VSUB.SCHOOL_KIND || '-' || VSUB.CURRICULUM_CD || '-' || VSUB.SUBCLASSCD AS VALUE, ";
        $query .= "      VSUB.CLASSCD || '-' || VSUB.SCHOOL_KIND || '-' || VSUB.CURRICULUM_CD || '-' || VSUB.SUBCLASSCD || ':' || VALUE (VSUB.SUBCLASSNAME, '') AS LABEL ";
        $query .= "    FROM ";
        $query .= "      CHAIR_DAT CHAIR ";
        $query .= "    INNER JOIN CHAIR_STF_DAT STF ";
        $query .= "       ON STF.YEAR             = CHAIR.YEAR ";
        $query .= "      AND STF.SEMESTER         = CHAIR.SEMESTER ";
        $query .= "      AND STF.CHAIRCD          = CHAIR.CHAIRCD ";
        $query .= "    LEFT JOIN V_SUBCLASS_MST VSUB ";
        $query .= "       ON VSUB.YEAR            = CHAIR.YEAR ";
        $query .= "      AND VSUB.CLASSCD         = CHAIR.CLASSCD ";
        $query .= "      AND VSUB.SCHOOL_KIND     = CHAIR.SCHOOL_KIND ";
        $query .= "      AND VSUB.CURRICULUM_CD   = CHAIR.CURRICULUM_CD ";
        $query .= "      AND VSUB.SUBCLASSCD      = CHAIR.SUBCLASSCD ";
        $query .= "    WHERE ";
        $query .= "          CHAIR.SEMESTER       = {$model->semester} ";
        $query .= "      AND SUBSTR(CHAIR.SUBCLASSCD, 1, 2) IN (SELECT N1.NAMECD2 FROM V_NAME_MST N1 WHERE N1.YEAR = '".CTRL_YEAR."' AND N1.NAMECD1 = '{$namecd1}') ";
        //権限(制限付)
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "        AND STF.STAFFCD = '".STAFFCD."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //講座データの取得
    public function selectChairQuery($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     CHAIR.CHAIRCD AS VALUE, ";
        $query .= "     CHAIR.CHAIRCD || ' ' || VALUE(CHAIR.CHAIRNAME,'') AS LABEL ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT CHAIR ";
        $query .= " INNER JOIN CHAIR_STF_DAT STF ";
        $query .= "      ON STF.YEAR       = CHAIR.YEAR ";
        $query .= "     AND STF.SEMESTER   = CHAIR.SEMESTER ";
        $query .= "     AND STF.CHAIRCD    = CHAIR.CHAIRCD ";
        $query .= " WHERE ";
        $query .= "         CHAIR.SEMESTER = {$model->semester} ";
        $query .= "     AND CHAIR.CLASSCD || '-' || CHAIR.SCHOOL_KIND || '-' || CHAIR.CURRICULUM_CD || '-' || CHAIR.SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        //権限(制限付)
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "        AND STF.STAFFCD = '".STAFFCD."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学期取得
    public function getSemester($model)
    {
        $query  = " SELECT ";
        $query .= "     SDATE, ";
        $query .= "     EDATE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '{$model->semester}' ";

        return $query;
    }

    //全体のデータの取得
    public function selectQuery($model, $execute_date)
    {
        $query  = "    SELECT ";
        $query .= "      REGD.SCHREGNO, ";
        $query .= "      HDAT.HR_NAME, ";
        $query .= "      REGD.ATTENDNO, ";
        $query .= "      BASE.NAME AS NAME_SHOW, ";
        $query .= "      CMNT.COMMENT1, ";
        $query .= "      VSUB.SUBCLASSNAME "; //CSV用
        $query .= "    FROM ";
        $query .= "      CHAIR_STD_DAT CHR_STD ";
        $query .= "    INNER JOIN SCHREG_REGD_DAT REGD ";
        $query .= "       ON CHR_STD.YEAR         = REGD.YEAR ";
        $query .= "      AND CHR_STD.SEMESTER     = REGD.SEMESTER ";
        $query .= "      AND CHR_STD.SCHREGNO     = REGD.SCHREGNO ";
        $query .= "    INNER JOIN CHAIR_DAT CHAIR ";
        $query .= "       ON CHAIR.YEAR           = CHR_STD.YEAR ";
        $query .= "      AND CHAIR.SEMESTER       = CHR_STD.SEMESTER ";
        $query .= "      AND CHAIR.CHAIRCD        = CHR_STD.CHAIRCD ";
        $query .= "    LEFT JOIN PYP_COMMENT_DAT CMNT ";
        $query .= "       ON CMNT.YEAR            = CHAIR.YEAR ";
        $query .= "      AND CMNT.SEMESTER        = CHAIR.SEMESTER ";
        $query .= "      AND CMNT.CLASSCD         = CHAIR.CLASSCD ";
        $query .= "      AND CMNT.SCHOOL_KIND     = CHAIR.SCHOOL_KIND ";
        $query .= "      AND CMNT.CURRICULUM_CD   = CHAIR.CURRICULUM_CD ";
        $query .= "      AND CMNT.SUBCLASSCD      = CHAIR.SUBCLASSCD ";
        $query .= "      AND CMNT.SCHREGNO        = CHR_STD.SCHREGNO ";
        $query .= "    LEFT JOIN SCHREG_BASE_MST BASE ";
        $query .= "      ON BASE.SCHREGNO         = REGD.SCHREGNO ";
        $query .= "    LEFT JOIN SCHREG_REGD_HDAT HDAT ";
        $query .= "       ON HDAT.YEAR            = REGD.YEAR ";
        $query .= "      AND HDAT.SEMESTER        = REGD.SEMESTER ";
        $query .= "      AND HDAT.GRADE           = REGD.GRADE ";
        $query .= "      AND HDAT.HR_CLASS        = REGD.HR_CLASS ";
        $query .= "    LEFT JOIN V_SUBCLASS_MST VSUB ";
        $query .= "       ON VSUB.YEAR            = CHAIR.YEAR ";
        $query .= "      AND VSUB.CLASSCD         = CHAIR.CLASSCD ";
        $query .= "      AND VSUB.SCHOOL_KIND     = CHAIR.SCHOOL_KIND ";
        $query .= "      AND VSUB.CURRICULUM_CD   = CHAIR.CURRICULUM_CD ";
        $query .= "      AND VSUB.SUBCLASSCD      = CHAIR.SUBCLASSCD ";
        $query .= "    WHERE ";
        $query .= "          CHR_STD.YEAR         = '".CTRL_YEAR."' ";
        $query .= "      AND CHR_STD.SEMESTER     = '{$model->semester}' ";
        $query .= "      AND CHR_STD.CHAIRCD      = '{$model->field["CHAIRCD"]}' ";
        $query .= "      AND '".$execute_date."'  BETWEEN CHR_STD.APPDATE AND CHR_STD.APPENDDATE ";
        $query .= "    ORDER BY ";
        $query .= "      REGD.GRADE, ";
        $query .= "      REGD.HR_CLASS, ";
        $query .= "      REGD.ATTENDNO ";

        return $query;
    }

    /* データ更新処理 */
    public function update($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $year = CTRL_YEAR;
        list($classcd, $school_kind, $curriculum_cd, $subclasscd) = preg_split("/-/", $model->field["SUBCLASSCD"]);

        foreach ($model->data["SCHREGNO"] as $schregno) {
            //項目
            $data = array();
            $data["YEAR"][TEXT]             = $year;
            $data["SEMESTER"][TEXT]         = $model->semester;
            $data["CLASSCD"][TEXT]          = $classcd;
            $data["SCHOOL_KIND"][TEXT]      = $school_kind;
            $data["CURRICULUM_CD"][TEXT]    = $curriculum_cd;
            $data["SUBCLASSCD"][TEXT]       = $subclasscd;
            $data["SCHREGNO"][TEXT]         = $schregno;
            $data["COMMENT1"][TEXT]         = $model->fields["COMMENT_".$schregno];
            $data["REGISTERCD"][TEXT]       = STAFFCD ;
            $data["UPDATED"][FUNC]          = "sysdate()";
            //存在チェック
            $query  = "SELECT COUNT(*) FROM PYP_COMMENT_DAT";
            $query .= " WHERE YEAR          = '{$year}'";
            $query .= "   AND SEMESTER      = '{$model->semester}'";
            $query .= "   AND CLASSCD       = '{$classcd}' ";
            $query .= "   AND SCHOOL_KIND   = '{$school_kind}' ";
            $query .= "   AND CURRICULUM_CD = '{$curriculum_cd}' ";
            $query .= "   AND SUBCLASSCD    = '{$subclasscd}' ";
            $query .= "   AND SCHREGNO      = '{$schregno}'";
            if (0 < $db->getOne($query)) {
                //更新
                $where  = " WHERE YEAR          = '{$year}'";
                $where .= "   AND SEMESTER      = '{$model->semester}'";
                $where .= "   AND CLASSCD       = '{$classcd}' ";
                $where .= "   AND SCHOOL_KIND   = '{$school_kind}' ";
                $where .= "   AND CURRICULUM_CD = '{$curriculum_cd}' ";
                $where .= "   AND SUBCLASSCD    = '{$subclasscd}' ";
                $where .= "   AND SCHREGNO      = '{$schregno}'";
                $query = Query::updateSQL($data, "PYP_COMMENT_DAT", $where);
            } else {
                //追加
                $query = Query::insertSQL($data, "PYP_COMMENT_DAT");
            }
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
    }
}
