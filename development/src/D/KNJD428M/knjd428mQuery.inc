<?php

require_once('for_php7.php');
class knjd428mQuery extends Query
{

    //グループ、履修情報取得
    public function getViewGradeKindSchreg($model, $flg = "")
    {
        $query  = " SELECT ";
        if ($flg == 'set') {
            $query .= " DISTINCT ";
            $query .= "     T1.GAKUBU_SCHOOL_KIND, ";
            $query .= "     T1.GHR_CD, ";
            $query .= "     T1.GRADE, ";
            $query .= "     T1.HR_CLASS, ";
            $query .= "     T1.CONDITION, ";
            $query .= "     T1.GROUPCD ";
        } else {
            $query .= "     T1.* ";
        }
        $query .= " FROM ";
        $query .= "     V_GRADE_KIND_SCHREG_UNIT_GROUP_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".$model->exp_year."' ";
        $query .= " AND T1.SEMESTER = '9' ";
        $query .= " AND T1.SCHREGNO = '".$model->schregno."' ";
        if ($flg != 'set') {
            $query .= " ORDER BY ";
            $query .= "     T1.GROUPCD,  ";
            $query .= "     T1.CLASSCD,  ";
            $query .= "     T1.SCHOOL_KIND,  ";
            $query .= "     T1.CURRICULUM_CD,  ";
            $query .= "     T1.SUBCLASSCD,  ";
            $query .= "     T1.UNITCD  ";
        }

        return $query;
    }

    //グループコード取得
    public function getGroupcd($model, $row)
    {
        $query  = " SELECT ";
        $query .= "     GROUPNAME ";
        $query .= " FROM ";
        $query .= "     GRADE_KIND_COMP_GROUP_YMST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' ";
        $query .= " AND SEMESTER    = '9' ";
        $query .= " AND GAKUBU_SCHOOL_KIND = '".$row["GAKUBU_SCHOOL_KIND"]."' ";
        $query .= " AND GHR_CD      = '".$row["GHR_CD"]."' ";
        $query .= " AND GRADE       = '".$row["GRADE"]."' ";
        $query .= " AND HR_CLASS    = '".$row["HR_CLASS"]."' ";
        $query .= " AND CONDITION   = '".$row["CONDITION"]."' ";
        $query .= " AND GROUPCD     = '".$row["GROUPCD"]."' ";

        return $query;
    }

    //状態区分名称取得
    public function getConditionName($model, $condition)
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' ";
        $query .= " AND NAMECD1     = 'A033' ";
        $query .= " AND NAMECD2     = '".$condition."' ";

        return $query;
    }

    //学期取得
    public function getSemester($model)
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '{$model->exp_year}' ";
        $query .= "     AND SEMESTER < '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    // GRADE_KIND_SCHREG_GROUP_DAT 取得
    public function getGradeKindSchregGroupDat($model)
    {
        if ($model->Properties["useGradeKindCompGroupSemester"] == '1') {
            $semester = $model->semester;
        } else {
            $semester = "9";
        }

        $query  = " SELECT ";
        $query .= "     GAKUBU_SCHOOL_KIND, ";
        $query .= "     GHR_CD, ";
        $query .= "     GRADE, ";
        $query .= "     HR_CLASS, ";
        $query .= "     CONDITION, ";
        $query .= "     GROUPCD ";
        $query .= " FROM ";
        $query .= "     GRADE_KIND_SCHREG_GROUP_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '{$model->exp_year}' ";
        $query .= "     AND SEMESTER = '{$semester}' ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";

        return $query;
    }

    //帳票パターン取得
    public function getPrintPattern($model, $row)
    {
        if ($model->Properties["useGradeKindCompGroupSemester"] == '1') {
            $semester = $model->semester;
        } else {
            $semester = "9";
        }

        $query  = " SELECT ";
        $query .= "     A035.NAMESPARE1 ";
        $query .= " FROM ";
        $query .= "     GRADE_KIND_COMP_GROUP_YMST PATTERN ";
        $query .= "     LEFT JOIN NAME_MST A035 ON A035.NAMECD1 = 'A035' ";
        $query .= "          AND PATTERN.GUIDANCE_PATTERN = A035.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "         PATTERN.YEAR               = '{$model->exp_year}' ";
        $query .= "     AND PATTERN.SEMESTER           = '{$semester}' ";
        $query .= "     AND PATTERN.GAKUBU_SCHOOL_KIND = '{$row["GAKUBU_SCHOOL_KIND"]}' ";
        $query .= "     AND PATTERN.GHR_CD             = '{$row["GHR_CD"]}' ";
        $query .= "     AND PATTERN.GRADE              = '{$row["GRADE"]}' ";
        $query .= "     AND PATTERN.HR_CLASS           = '{$row["HR_CLASS"]}' ";
        $query .= "     AND PATTERN.CONDITION          = '{$row["CONDITION"]}' ";
        $query .= "     AND PATTERN.GROUPCD            = '{$row["GROUPCD"]}' ";

        return $query;
    }

    //所見取得
    public function getHreportremarkDat($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.COMMUNICATION, ";
        $query .= "     T1.ATTENDREC_REMARK, ";
        $query .= "     D02_01.REMARK1 AS D02_01_REMARK1, ";
        $query .= "     D02_01.REMARK2 AS D02_01_REMARK2, ";
        $query .= "     D02_01.REMARK3 AS D02_01_REMARK3, ";
        $query .= "     D02_02.REMARK1 AS D02_02_REMARK1, ";
        $query .= "     D02_02.REMARK2 AS D02_02_REMARK2, ";
        $query .= "     D02_02.REMARK3 AS D02_02_REMARK3, ";
        $query .= "     D01_01.REMARK1 AS D01_01_REMARK1, ";
        $query .= "     D01_02.REMARK1 AS D01_02_REMARK1, ";
        $query .= "     D01_03.REMARK1 AS D01_03_REMARK1 ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT T1 ";
        $query .= "     LEFT JOIN HREPORTREMARK_DETAIL_DAT D02_01 ";
        $query .= "          ON D02_01.YEAR     = T1.YEAR ";
        $query .= "         AND D02_01.SEMESTER = T1.SEMESTER ";
        $query .= "         AND D02_01.SCHREGNO = T1.SCHREGNO ";
        $query .= "         AND D02_01.DIV      = '02' ";
        $query .= "         AND D02_01.CODE     = '01' ";
        $query .= "     LEFT JOIN HREPORTREMARK_DETAIL_DAT D02_02 ";
        $query .= "          ON D02_02.YEAR     = T1.YEAR ";
        $query .= "         AND D02_02.SEMESTER = T1.SEMESTER ";
        $query .= "         AND D02_02.SCHREGNO = T1.SCHREGNO ";
        $query .= "         AND D02_02.DIV      = '02' ";
        $query .= "         AND D02_02.CODE     = '02' ";
        $query .= "     LEFT JOIN HREPORTREMARK_DETAIL_DAT D01_01 ";
        $query .= "          ON D01_01.YEAR     = T1.YEAR ";
        $query .= "         AND D01_01.SEMESTER = T1.SEMESTER ";
        $query .= "         AND D01_01.SCHREGNO = T1.SCHREGNO ";
        $query .= "         AND D01_01.DIV      = '01' ";
        $query .= "         AND D01_01.CODE     = '01' ";
        $query .= "     LEFT JOIN HREPORTREMARK_DETAIL_DAT D01_02 ";
        $query .= "          ON D01_02.YEAR     = T1.YEAR ";
        $query .= "         AND D01_02.SEMESTER = T1.SEMESTER ";
        $query .= "         AND D01_02.SCHREGNO = T1.SCHREGNO ";
        $query .= "         AND D01_02.DIV      = '01' ";
        $query .= "         AND D01_02.CODE     = '02' ";
        $query .= "     LEFT JOIN HREPORTREMARK_DETAIL_DAT D01_03 ";
        $query .= "          ON D01_03.YEAR     = T1.YEAR ";
        $query .= "         AND D01_03.SEMESTER = T1.SEMESTER ";
        $query .= "         AND D01_03.SCHREGNO = T1.SCHREGNO ";
        $query .= "         AND D01_03.DIV      = '01' ";
        $query .= "         AND D01_03.CODE     = '03' ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '{$model->exp_year}' ";
        $query .= "     AND T1.SEMESTER = '{$model->semester}' ";
        $query .= "     AND T1.SCHREGNO = '{$model->schregno}' ";

        return $query;
    }

    //校種 取得
    public function getSchoolKind($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     GDAT.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT GDAT ";
        $query .= "       ON GDAT.YEAR     = REGD.YEAR ";
        $query .= "      AND GDAT.GRADE    = REGD.GRADE ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT HDAT ";
        $query .= "       ON HDAT.YEAR     = REGD.YEAR ";
        $query .= "      AND HDAT.SEMESTER = REGD.SEMESTER ";
        $query .= "      AND HDAT.GRADE    = REGD.GRADE ";
        $query .= "      AND HDAT.HR_CLASS = REGD.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "     REGD.YEAR     = '{$model->exp_year}' ";
        $query .= " AND REGD.SEMESTER = '{$model->semester}' ";
        $query .= " AND REGD.SCHREGNO = '{$model->schregno}' ";
        $query .= " AND HDAT.GRADE    = '{$model->grade}' ";
        $query .= " AND HDAT.HR_CLASS = '{$model->hrClass}' ";

        return $query;
    }

    //HREPORT_CONDITION_DAT 取得
    public function getReportCondition($model, $schoolKind, $seq)
    {
        $query  = " SELECT ";
        $query .= "   T1.REMARK1 ";
        $query .= " FROM ";
        $query .= "   HREPORT_CONDITION_DAT T1 ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '{$model->exp_year}' ";
        $query .= "   AND T1.SCHOOLCD = '".SCHOOLCD."' ";
        $query .= "   AND T1.SCHOOL_KIND = '".$schoolKind."' ";
        $query .= "   AND T1.GRADE = '00' ";
        $query .= "   AND T1.COURSECD = '0' ";
        $query .= "   AND T1.MAJORCD = '000' ";
        $query .= "   AND T1.COURSECODE = '0000' ";
        $query .= "   AND T1.SEQ = '".$seq."' ";

        return $query;
    }

    //INSERT -- HREPORTREMARK_DAT
    public function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();

        //存在チェック
        $checkH = $db->getCol(knjd428mQuery::getHreportremarkDat($model));

        // HREPORTREMARK_DAT
        $data = array();
        $data["ATTENDREC_REMARK"][TEXT] = $model->field["ATTENDREC_REMARK"];
        $data["COMMUNICATION"][TEXT]    = $model->field["COMMUNICATION"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        if (get_count($checkH) == 0) {
            $data["YEAR"][TEXT]         = $model->exp_year;
            $data["SEMESTER"][TEXT]     = $model->semester;
            $data["SCHREGNO"][TEXT]     = $model->schregno;

            $db->query(Query::insertSQL($data, "HREPORTREMARK_DAT"));
        } else {
            $where  = " WHERE ";
            $where .= "         YEAR     = '{$model->exp_year}' ";
            $where .= "     AND SEMESTER = '{$model->semester}' ";
            $where .= "     AND SCHREGNO = '{$model->schregno}' ";

            $db->query(Query::updateSQL($data, "HREPORTREMARK_DAT", $where));
        }

        //印刷デフォルト値設定「主に使用する様式」が「文言評価(3枠)B」の場合
        if ($model->useJisshu) {
            //HREPORTREMARK_DETAIL_DAT
            $data = array();
            $data["REMARK1"][TEXT] = $model->field["D02_01_REMARK1"];
            $data["REMARK2"][TEXT] = $model->field["D02_01_REMARK2"];
            $data["REMARK3"][TEXT] = $model->field["D02_01_REMARK3"];
            $data["REGISTERCD"][TEXT]     = STAFFCD;
            $data["UPDATED"][FUNC]        = "sysdate()";
            knjd428mQuery::updateHreportRemarkDetail($db, $model, "02", "01", $data);

            $data = array();
            $data["REMARK1"][TEXT] = $model->field["D02_02_REMARK1"];
            $data["REMARK2"][TEXT] = $model->field["D02_02_REMARK2"];
            $data["REMARK3"][TEXT] = $model->field["D02_02_REMARK3"];
            $data["REGISTERCD"][TEXT]     = STAFFCD;
            $data["UPDATED"][FUNC]        = "sysdate()";
            knjd428mQuery::updateHreportRemarkDetail($db, $model, "02", "02", $data);

            $data = array();
            $data["REMARK1"][TEXT] = $model->field["D01_01_REMARK1"];
            $data["REGISTERCD"][TEXT]     = STAFFCD;
            $data["UPDATED"][FUNC]        = "sysdate()";
            knjd428mQuery::updateHreportRemarkDetail($db, $model, "01", "01", $data);

            $data = array();
            $data["REMARK1"][TEXT] = $model->field["D01_02_REMARK1"];
            $data["REGISTERCD"][TEXT]     = STAFFCD;
            $data["UPDATED"][FUNC]        = "sysdate()";
            knjd428mQuery::updateHreportRemarkDetail($db, $model, "01", "02", $data);

            $data = array();
            $data["REMARK1"][TEXT] = $model->field["D01_03_REMARK1"];
            $data["REGISTERCD"][TEXT]     = STAFFCD;
            $data["UPDATED"][FUNC]        = "sysdate()";
            knjd428mQuery::updateHreportRemarkDetail($db, $model, "01", "03", $data);
        }

        Query::dbCheckIn($db);
        return ;
    }

    //データチェック
    public function getHreportRemarkCnt($model, $tblname, $div = "", $code = "")
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     {$tblname} ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$model->exp_year."' ";
        $query .= " AND SEMESTER = '".$model->semester."' ";
        $query .= " AND SCHREGNO = '".$model->schregno."' ";
        if ($div != "") {
            $query .= " AND DIV = '".$div."' ";
        }
        if ($code != "") {
            $query .= " AND CODE = '".$code."' ";
        }

        return $query;
    }

    //UPDATE -- HREPORTREMARK_DETAIL_DAT
    public function updateHreportRemarkDetail(&$db, $model, $setDiv, $setCode, $data)
    {
        $cnt = $db->getOne(knjd428mQuery::getHreportRemarkCnt($model, "HREPORTREMARK_DETAIL_DAT", $setDiv, $setCode));
        if ($cnt > 0) {
            $where  = " WHERE ";
            $where .= "     YEAR     = '".$model->exp_year."' ";
            $where .= " AND SEMESTER = '".$model->semester."' ";
            $where .= " AND SCHREGNO = '".$model->schregno."' ";
            $where .= " AND DIV      = '".$setDiv."' ";
            $where .= " AND CODE     = '".$setCode."' ";

            $query = Query::updateSQL($data, "HREPORTREMARK_DETAIL_DAT", $where);
        } else {
            $data["YEAR"][TEXT]     = $model->exp_year;
            $data["SEMESTER"][TEXT] = $model->semester;
            $data["SCHREGNO"][TEXT] = $model->schregno;
            $data["DIV"][TEXT]      = $setDiv;
            $data["CODE"][TEXT]     = $setCode;

            $query = Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT");
        }
        $db->query($query);
    }

    //年間目標タイトル名取得
    public function getTitleName($model)
    {
        $query  = " SELECT ";
        $query .= "   KIND_NAME ";
        $query .= " FROM ";
        $query .= "   HREPORT_GUIDANCE_KIND_NAME_HDAT T1 ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '{$model->exp_year}' ";
        $query .= "   AND T1.KIND_NO = '02' ";

        return $query;
    }

    //年間目標数取得
    public function getCntRemark($model)
    {
        $query  = " SELECT ";
        $query .= "   COUNT(*) ";
        $query .= " FROM ";
        $query .= "   HREPORT_GUIDANCE_KIND_NAME_DAT T1 ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '{$model->exp_year}' ";
        $query .= "   AND T1.KIND_NO = '02' ";

        return $query;
    }

    //年間目標項目名取得
    public function getRemarkName($model, $seq)
    {
        $query  = " SELECT ";
        $query .= "   KIND_REMARK ";
        $query .= " FROM ";
        $query .= "   HREPORT_GUIDANCE_KIND_NAME_DAT T1 ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '{$model->exp_year}' ";
        $query .= "   AND T1.KIND_NO = '02' ";
        $query .= "   AND T1.KIND_SEQ = '{$seq}' ";

        return $query;
    }

    //年間目標内容取得
    public function getRemark($model, $i, $flg = "")
    {
        $query  = " SELECT ";
        if ($flg == "CNT") {
            $query .= "   COUNT(*) AS CNT ";
        } else {
            $query .= "   GOALS ";
        }
        $query .= " FROM ";
        $query .= "   HREPORT_SCHREG_REMARK_DAT T1 ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '{$model->exp_year}' ";
        $query .= "   AND T1.SEMESTER = '{$model->semester9}' ";
        $query .= "   AND T1.SCHREGNO = '{$model->schregno}' ";
        $query .= "   AND T1.DIV = '$i' ";

        return $query;
    }

    //年間目標更新処理
    public function &getRemarkInsertQuery($model)
    {
        $db = Query::dbCheckOut();

        for ($i = 1; $i <= $model->field["REMARK_CNT"]; $i++) {
            //存在チェック
            $check = $db->getOne(knjd428mQuery::getRemark($model, $i, "CNT"));

            $data = array();
            $data["GOALS"][TEXT]            = $model->field["REMARK".$i];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";
            if ($check == 0) {
                $data["YEAR"][TEXT]         = $model->exp_year;
                $data["SEMESTER"][TEXT]     = $model->semester9;
                $data["SCHREGNO"][TEXT]     = $model->schregno;
                $data["DIV"][TEXT]          = $i;

                $db->query(Query::insertSQL($data, "HREPORT_SCHREG_REMARK_DAT"));
            } else {
                $where  = " WHERE ";
                $where .= "         YEAR     = '{$model->exp_year}' ";
                $where .= "     AND SEMESTER = '{$model->semester9}' ";
                $where .= "     AND SCHREGNO = '{$model->schregno}' ";
                $where .= "     AND DIV      = '{$i}' ";

                $db->query(Query::updateSQL($data, "HREPORT_SCHREG_REMARK_DAT", $where));
            }
        }

        Query::dbCheckIn($db);
        return ;
    }

    //年間目標 指導計画から取込
    public function getinputData($model, $i)
    {
        $query  =  " SELECT ";
        $query .=  "    REMARK ";
        $query .=  " FROM ";
        $query .=  "    HREPORT_GUIDANCE_SCHREG_REMARK_DAT ";
        $query .=  " WHERE ";
        $query .=  "       YEAR     = '{$model->exp_year}' ";
        $query .=  "   AND SEMESTER = '{$model->semester9}' ";
        $query .=  "   AND SCHREGNO = '{$model->schregno}' ";
        $query .=  "   AND DIV      = '02' ";
        $query .=  "   AND SEQ      = '{$i}' ";

        return $query;
    }
}
