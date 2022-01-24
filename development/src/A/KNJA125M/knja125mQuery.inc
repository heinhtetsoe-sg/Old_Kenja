<?php

require_once('for_php7.php');

class knja125mQuery extends Query {

    //名称マスタより東京仕様かどうか確認
    function getNamecd($cd, $value)
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "         NAMECD1 = '".$cd."' ";
        $query .= "     AND NAMECD2 = '".$value."' ";

        return $query;
    }

    //１レコード取得
    function getTrainRow($schregno, $exp_year, $model)
    {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        if ($model->getname === 'tokyoto') {
            $query .= "     ,T2.TOTALSTUDYACT2 ";
            $query .= "     ,T2.TOTALSTUDYVAL2 ";
            $query .= "     ,T2.CREDITREMARK ";
        } else if ($model->getname === 'naraken') {
            $query .= "     ,DET2_005.REMARK1 AS NARA_TIME ";
        } else if ($model->getname === 'sagaken') {
            $query .= "     ,DET2_006.REMARK1 AS SPECIALJISUU ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T0";
        $query .= " LEFT JOIN HTRAINREMARK_DAT T1 ON T1.YEAR = T0.YEAR ";
        $query .= "                              AND T1.SCHREGNO = T0.SCHREGNO ";
        if ($model->getname === 'tokyoto') {
            $query .= " LEFT JOIN HTRAINREMARK_HDAT T2 ON T2.SCHREGNO = T0.SCHREGNO ";
        } else if ($model->getname === 'naraken') {
            $query .= " LEFT JOIN HTRAINREMARK_DETAIL2_DAT DET2_005 ON DET2_005.YEAR = T0.YEAR ";
            $query .= "     AND DET2_005.SCHREGNO = T0.SCHREGNO ";
            $query .= "     AND DET2_005.HTRAIN_SEQ = '005' ";
        } else if ($model->getname === 'sagaken') {
            $query .= " LEFT JOIN HTRAINREMARK_DETAIL2_DAT DET2_006 ON DET2_006.YEAR = T0.YEAR ";
            $query .= "     AND DET2_006.SCHREGNO = T0.SCHREGNO ";
            $query .= "     AND DET2_006.HTRAIN_SEQ = '006' ";
        }
        $query .= " WHERE ";
        $query .= "     T0.YEAR        = '".$exp_year."' AND ";
        $query .= "     T0.SCHREGNO    = '".$schregno."' ";

        return $query;
    }

    //学校種別取得
    function getSchoolKind($model) {

        $query  = " SELECT ";
        $query .= "     T2.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_REGD_GDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = '$model->exp_year' AND ";
        $query .= "     T1.SEMESTER = '$model->exp_semester' AND ";
        $query .= "     T1.GRADE    = T2.GRADE AND ";
        $query .= "     T1.SCHREGNO = '$model->schregno' ";

        return $query;
    }

    //特別活動出席時数取得
    function getSchAttendDat($schregno, $exp_year, $model)
    {
        $query  = " SELECT ";
        $query .= "     INT(ROUND(SUM(CAST(CREDIT_TIME AS DECIMAL(5, 1))))) AS SPECIALJISUU ";
        $query .= " FROM ";
        $query .= "     SPECIALACT_ATTEND_DAT T1 ";
        $query .= " INNER JOIN V_NAME_MST M027 ";
        $query .= "      ON M027.NAME1   = T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ";
        $query .= "     AND M027.NAMECD1 = 'M027' ";
        $query .= "     AND M027.YEAR = T1.YEAR ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR        = '".$exp_year."' ";
        $query .= "     AND T1.SCHREGNO    = '".$schregno."' ";
        $query .= "     AND NOT EXISTS(SELECT 'X' FROM V_NAME_MST M026 ";
        $query .= "                    WHERE M026.NAME1      = T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ";
        $query .= "                      AND M026.YEAR       = T1.YEAR ";
        $query .= "                      AND M026.NAMECD1    = 'M026' ";
        $query .= "                      AND M026.NAMESPARE2 = '1' ) ";
        return $query;
    }

    //INSERT
    function &getInsertQuery($fields, $schregno, $exp_year, $exp_semester, $model)
    {
        $db = Query::dbCheckOut();

        //年次の取得
        $query  = "SELECT ANNUAL FROM SCHREG_REGD_DAT ";
        $query .= " WHERE SCHREGNO = '".$schregno."'";
        $query .= "   AND YEAR = '".$exp_year."'";
        $query .= "   AND SEMESTER = '".$exp_semester."'";

        $data["YEAR"][TEXT]                 = $exp_year;
        $data["SCHREGNO"][TEXT]             = $schregno;
        $data["ANNUAL"][TEXT]               = $db->getOne($query);
        $data["TOTALSTUDYACT"][TEXT]        = $fields["TOTALSTUDYACT"];
        $data["TOTALSTUDYVAL"][TEXT]        = $fields["TOTALSTUDYVAL"];
        $data["SPECIALACTREMARK"][TEXT]     = $fields["SPECIALACTREMARK"];
        $data["TOTALREMARK"][TEXT]          = $fields["TOTALREMARK"];
        $data["ATTENDREC_REMARK"][TEXT]     = $fields["ATTENDREC_REMARK"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $db->query(Query::insertSQL($data, "HTRAINREMARK_DAT"));

        if ($model->getname === 'tokyoto') {
            //奉仕の記録(HTRAINREMARK_HDAT)
            $data2["SCHREGNO"][TEXT]             = $schregno;
            $data2["TOTALSTUDYACT2"][TEXT]       = $fields["TOTALSTUDYACT2"];
            $data2["TOTALSTUDYVAL2"][TEXT]       = $fields["TOTALSTUDYVAL2"];
            $data2["CREDITREMARK"][TEXT]         = $fields["CREDITREMARK"];
            $data2["REGISTERCD"][TEXT]           = STAFFCD;
            $data2["UPDATED"][FUNC]              = "sysdate()";
            if (1 > $db->getOne("SELECT COUNT(*) FROM HTRAINREMARK_HDAT WHERE SCHREGNO = '".$schregno."'")) {
                $query = Query::insertSQL($data2, "HTRAINREMARK_HDAT");
            } else {
                $where  = "WHERE SCHREGNO = '".$schregno."' ";
                $query = Query::updateSQL($data2, "HTRAINREMARK_HDAT", $where);
            }
            $db->query($query);
        } else if ($model->getname === 'naraken') {
            //奈良Time(HTRAINREMARK_DETAIL2_DAT)
            $data2["YEAR"][TEXT]                 = $model->exp_year;
            $data2["SCHREGNO"][TEXT]             = $schregno;
            $data2["HTRAIN_SEQ"][TEXT]           = "005";
            $data2["REMARK1"][TEXT]              = $fields["NARA_TIME"];
            $data2["REGISTERCD"][TEXT]           = STAFFCD;
            $data2["UPDATED"][FUNC]              = "sysdate()";
            if (1 > $db->getOne("SELECT COUNT(*) FROM HTRAINREMARK_DETAIL2_DAT WHERE YEAR = '".$exp_year."' AND SCHREGNO = '".$schregno."' AND HTRAIN_SEQ = '005' ")) {
                $query = Query::insertSQL($data2, "HTRAINREMARK_DETAIL2_DAT");
            } else {
                $where  = "WHERE YEAR = '".$exp_year."' ";
                $where .= "  AND SCHREGNO = '".$schregno."' ";
                $where .= "  AND HTRAIN_SEQ = '005' ";
                $query = Query::updateSQL($data2, "HTRAINREMARK_DETAIL2_DAT", $where);
            }
            $db->query($query);
        } else if ($model->getname === 'sagaken') {
            //特別活動出席時数(HTRAINREMARK_DETAIL2_DAT)
            $data2["YEAR"][TEXT]                 = $model->exp_year;
            $data2["SCHREGNO"][TEXT]             = $schregno;
            $data2["HTRAIN_SEQ"][TEXT]           = "006";
            $data2["REMARK1"][TEXT]              = $fields["SPECIALJISUU"];
            $data2["REGISTERCD"][TEXT]           = STAFFCD;
            $data2["UPDATED"][FUNC]              = "sysdate()";
            if (1 > $db->getOne("SELECT COUNT(*) FROM HTRAINREMARK_DETAIL2_DAT WHERE YEAR = '".$exp_year."' AND SCHREGNO = '".$schregno."' AND HTRAIN_SEQ = '006' ")) {
                $query = Query::insertSQL($data2, "HTRAINREMARK_DETAIL2_DAT");
            } else {
                $where  = "WHERE YEAR = '".$exp_year."' ";
                $where .= "  AND SCHREGNO = '".$schregno."' ";
                $where .= "  AND HTRAIN_SEQ = '006' ";
                $query = Query::updateSQL($data2, "HTRAINREMARK_DETAIL2_DAT", $where);
            }
            $db->query($query);
        }

        Query::dbCheckIn($db);
        return ;
    }

    //DELETE
    function &getDeleteQuery($schregno, $exp_year, $model)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM HTRAINREMARK_DAT ";
        $query .= " WHERE YEAR = '".$exp_year."'";
        $query .= "   AND SCHREGNO = '".$schregno."'";

        $db->query($query);

        if ($model->getname === 'tokyoto') {
            //奉仕の記録(HTRAINREMARK_HDAT)　※他のフィールドもあるため、NULLで更新
            //データがある場合のみNULLで更新
            if ($db->getOne("SELECT COUNT(*) FROM HTRAINREMARK_HDAT WHERE SCHREGNO = '".$schregno."'") > 0) {
                $data2["TOTALSTUDYACT2"][TEXT]       = '';
                $data2["TOTALSTUDYVAL2"][TEXT]       = '';
                $data2["CREDITREMARK"][TEXT]         = '';
                $data2["REGISTERCD"][TEXT]           = STAFFCD;
                $data2["UPDATED"][FUNC]              = "sysdate()";

                $where  = "WHERE SCHREGNO = '".$schregno."' ";
                $query = Query::updateSQL($data2, "HTRAINREMARK_HDAT", $where);
                $db->query($query);
            }
        } else if ($model->getname === 'naraken') {
            //奈良Time(HTRAINREMARK_DETAIL2_DAT)　※他のフィールドもあるため、NULLで更新
            //データがある場合のみNULLで更新
            if ($db->getOne("SELECT COUNT(*) FROM HTRAINREMARK_DETAIL2_DAT WHERE YEAR = '".$exp_year."' AND SCHREGNO = '".$schregno."' AND HTRAIN_SEQ = '005' ") > 0) {
                $data2["REMARK1"][TEXT]       = '';
                $data2["REGISTERCD"][TEXT]           = STAFFCD;
                $data2["UPDATED"][FUNC]              = "sysdate()";

                $where  = "WHERE YEAR = '".$exp_year."' ";
                $where .= "  AND SCHREGNO = '".$schregno."' ";
                $where .= "  AND HTRAIN_SEQ = '005' ";
                $query = Query::updateSQL($data2, "HTRAINREMARK_DETAIL2_DAT", $where);
                $db->query($query);
            }
        } else if ($model->getname === 'sagaken') {
            //特別活動出席時数(HTRAINREMARK_DETAIL2_DAT)　※他のフィールドもあるため、NULLで更新
            //データがある場合のみNULLで更新
            if ($db->getOne("SELECT COUNT(*) FROM HTRAINREMARK_DETAIL2_DAT WHERE YEAR = '".$exp_year."' AND SCHREGNO = '".$schregno."' AND HTRAIN_SEQ = '006' ") > 0) {
                $data2["REMARK1"][TEXT]       = '';
                $data2["REGISTERCD"][TEXT]           = STAFFCD;
                $data2["UPDATED"][FUNC]              = "sysdate()";

                $where  = "WHERE YEAR = '".$exp_year."' ";
                $where .= "  AND SCHREGNO = '".$schregno."' ";
                $where .= "  AND HTRAIN_SEQ = '006' ";
                $query = Query::updateSQL($data2, "HTRAINREMARK_DETAIL2_DAT", $where);
                $db->query($query);
            }
        }

        Query::dbCheckIn($db);
        return ;
    }

    function getGradeCd($model) {
        $query  = " SELECT ";
        $query .= "   GRADE_CD ";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "   YEAR = '".$model->exp_year."' ";
        $query .= "   AND GRADE = '".$model->grade."' ";

        return $query;
    }
}
?>
