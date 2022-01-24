<?php

require_once('for_php7.php');

class knjd132kQuery extends Query
{
    //校種取得
    public function getSchoolKind($schregno)
    {
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "    SCHREG_REGD_DAT T1, ";
        $query .= "    SCHREG_REGD_GDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.SCHREGNO = '".$schregno."' AND ";
        $query .= "     T1.GRADE    = T2.GRADE ";

        $db = Query::dbCheckOut();
        $retVal = $db->getOne($query);
        Query::dbCheckIn($db);

        return $retVal;
    }

    //学期取得
    public function getSemesterQuery()
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER <> '9' ";
        $query .= " ORDER BY  ";
        $query .= "     SEMESTER ";

        return $query;
    }

    //データ取得
    public function getHreportremarkDat($schregno, $semester, $div, $code)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR            = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER    = '{$semester}' ";
        $query .= "     AND SCHREGNO    = '{$schregno}' ";
        $query .= "     AND DIV         = '{$div}' ";
        $query .= "     AND CODE        = '{$code}' ";

        return $query;
    }

    //更新処理
    public function &getUpdateQuery($model, $schregno)
    {
        $db = Query::dbCheckOut();

        //更新対象項目
        $array = array();
        foreach ($model->textList as $field => $val) {
            list($divCode, $fieldName) = explode('_', $field);
            $array[$divCode][$fieldName] = $model->field[$field];
        }

        //学期
        $semester = $model->gakki;

        foreach ($array as $divCode => $fields) {
            $div = substr($divCode, 0, 2);
            $code = substr($divCode, 2);

            //削除
            $query  = " DELETE FROM ";
            $query .= "     HREPORTREMARK_DETAIL_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR            = '".CTRL_YEAR."' ";
            $query .= "     AND SEMESTER    = '".$semester."' ";
            $query .= "     AND SCHREGNO    = '".$schregno."' ";
            $query .= "     AND DIV         = '{$div}' ";
            $query .= "     AND CODE        = '{$code}' ";
            $db->query($query);

            //更新
            $data = array();
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["SEMESTER"][TEXT]         = $semester;
            $data["SCHREGNO"][TEXT]         = $schregno;
            $data["DIV"][TEXT]              = $div;
            $data["CODE"][TEXT]             = $code;

            foreach ($array[$divCode] as $field => $val) {
                $data[$field][TEXT]             = $val;
            }

            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            $query = Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT");
            $db->query($query);
        }

        Query::dbCheckIn($db);
        return;
    }

    //観点マスタ取得
    public function getJviewnameGradeMst($model, $subclasscd)
    {
        $query  = " SELECT ";
        $query .= "     T1.VIEWCD, ";
        $query .= "     T1.VIEWNAME ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_GRADE_MST T1 ";
        $query .= "     INNER JOIN JVIEWNAME_GRADE_YDAT T2 ";
        $query .= "          ON T2.GRADE         = T1.GRADE ";
        $query .= "         AND T2.CLASSCD       = T1.CLASSCD ";
        $query .= "         AND T2.SCHOOL_KIND   = T1.SCHOOL_KIND ";
        $query .= "         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "         AND T2.SUBCLASSCD    = T1.SUBCLASSCD ";
        $query .= "         AND T2.VIEWCD        = T1.VIEWCD ";
        $query .= " WHERE ";
        $query .= "     T2.YEAR          = '".CTRL_YEAR."' ";
        $query .= "     AND T1.GRADE        = '".$model->grade."' ";
        $query .= "     AND T1.CLASSCD      = '90' ";
        $query .= "     AND T1.SCHOOL_KIND  = '".$model->school_kind."' ";
        $query .= "     AND T1.SUBCLASSCD   = '".$subclasscd."' ";
        $query .= " ORDER BY  ";
        $query .= "     T1.CURRICULUM_CD DESC, ";
        $query .= "     T1.VIEWCD ASC ";
        $query .= " FETCH FIRST 3 ROWS ONLY ";

        return $query;
    }
}
