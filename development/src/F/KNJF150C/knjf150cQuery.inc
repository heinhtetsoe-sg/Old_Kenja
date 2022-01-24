<?php

require_once('for_php7.php');

class knjf150cQuery extends Query
{
    //クラス名称取得
    function getHrName($model)
    {
        $query  = " SELECT ";
        $query .= "     REGDH.HR_NAME, ";
        $query .= "     REGDH.GRADE, ";
        $query .= "     REGD.COURSECD, ";
        $query .= "     REGD.MAJORCD, ";
        $query .= "     REGD.COURSECODE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT REGDH ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT REGD ON REGDH.YEAR = REGD.YEAR ";
        $query .= "          AND REGDH.SEMESTER = REGD.SEMESTER ";
        $query .= "          AND REGD.SCHREGNO = '{$model->schregno}' ";
        $query .= " WHERE ";
        $query .= "     REGDH.YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     REGDH.SEMESTER    = '".CTRL_SEMESTER."' AND ";
        $query .= "     REGDH.GRADE       = '".$model->grade."' AND ";
        $query .= "     REGDH.HR_CLASS    = '".$model->hr_class."' ";

        return $query;
    }

    //教科名取得
    function getCreditMst($stdInfo) {
        $query  = " SELECT DISTINCT ";
        $query .= "     CRE.CLASSCD AS VALUE, ";
        $query .= "     CLS.CLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CREDIT_MST CRE ";
        $query .= "     INNER JOIN CLASS_MST CLS ";
        $query .= "         ON CRE.CLASSCD = CLS.CLASSCD ";
        $query .= "         AND CRE.SCHOOL_KIND = CLS.SCHOOL_KIND ";
        $query .= " WHERE ";
        $query .= "     CRE.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND CRE.COURSECD = '{$stdInfo["COURSECD"]}' ";
        $query .= "     AND CRE.MAJORCD = '{$stdInfo["MAJORCD"]}' ";
        $query .= "     AND CRE.GRADE = '{$stdInfo["GRADE"]}' ";
        $query .= "     AND CRE.COURSECODE = '{$stdInfo["COURSECODE"]}' ";
        $query .= " ORDER BY ";
        $query .= "     CRE.CLASSCD ";

        return $query;
    }

    //保健室来室記録データよりデータを取得
    function selectQuery($model)
    {

        $selectDet = array();
        $selectDet["01"] = array(1, 2, 3);
        $selectDet["02"] = array(1);
        $selectDet["06"] = array(3, 4);
        $selectDet["09"] = array(1, 3, 6);
        $selectDet["98"] = array(1, 2);
        $selectDet["99"] = array("_L1");

        $query  = " WITH TYPE1 AS( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.VISIT_DATE, ";
        $query .= "     T1.VISIT_HOUR, ";
        $query .= "     T1.VISIT_MINUTE, ";
        $query .= "     T1.TYPE, ";
        foreach ($selectDet as $seq => $remarkArray) {
            foreach ($remarkArray as $remarkName) {
                $query .= "     DET{$seq}.REMARK{$remarkName} AS SEQ{$seq}_REMARK{$remarkName}, ";
            }
        }
        $query .= "     N1.NAME1 AS TYPE_REASON1 ";
        $query .= " FROM ";
        $query .= "     NURSEOFF_VISITREC_DAT T1 ";
        foreach ($selectDet as $seq => $remarkArray) {
            $query .= "     LEFT JOIN NURSEOFF_VISITREC_DETAIL_DAT DET{$seq} ON T1.SCHREGNO = DET{$seq}.SCHREGNO ";
            $query .= "          AND T1.VISIT_DATE = DET{$seq}.VISIT_DATE ";
            $query .= "          AND T1.VISIT_HOUR = DET{$seq}.VISIT_HOUR ";
            $query .= "          AND T1.VISIT_MINUTE = DET{$seq}.VISIT_MINUTE ";
            $query .= "          AND T1.TYPE = DET{$seq}.TYPE ";
            $query .= "          AND DET{$seq}.SEQ = '{$seq}' ";
        }
        $query .= "     LEFT JOIN NAME_MST N1 ON DET02.REMARK1 = N1.NAMECD2 AND N1.NAMECD1 = 'F200' ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' AND ";
        $query .= "     T1.TYPE = '1' ";
        $query .= " ), TYPE2 AS( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.VISIT_DATE, ";
        $query .= "     T1.VISIT_HOUR, ";
        $query .= "     T1.VISIT_MINUTE, ";
        $query .= "     T1.TYPE, ";
        foreach ($selectDet as $seq => $remarkArray) {
            foreach ($remarkArray as $remarkName) {
                $query .= "     DET{$seq}.REMARK{$remarkName} AS SEQ{$seq}_REMARK{$remarkName}, ";
            }
        }
        $query .= "     N1.NAME1 AS TYPE_REASON1 ";
        $query .= " FROM ";
        $query .= "     NURSEOFF_VISITREC_DAT T1 ";
        foreach ($selectDet as $seq => $remarkArray) {
            $query .= "     LEFT JOIN NURSEOFF_VISITREC_DETAIL_DAT DET{$seq} ON T1.SCHREGNO = DET{$seq}.SCHREGNO ";
            $query .= "          AND T1.VISIT_DATE = DET{$seq}.VISIT_DATE ";
            $query .= "          AND T1.VISIT_HOUR = DET{$seq}.VISIT_HOUR ";
            $query .= "          AND T1.VISIT_MINUTE = DET{$seq}.VISIT_MINUTE ";
            $query .= "          AND T1.TYPE = DET{$seq}.TYPE ";
            $query .= "          AND DET{$seq}.SEQ = '{$seq}' ";
        }
        $query .= "     LEFT JOIN NAME_MST N1 ON DET02.REMARK1 = N1.NAMECD2 AND N1.NAMECD1 = 'F201' ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' AND ";
        $query .= "     T1.TYPE = '2' ";
        $query .= " ), TYPE3 AS( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.VISIT_DATE, ";
        $query .= "     T1.VISIT_HOUR, ";
        $query .= "     T1.VISIT_MINUTE, ";
        $query .= "     T1.TYPE, ";
        foreach ($selectDet as $seq => $remarkArray) {
            foreach ($remarkArray as $remarkName) {
                $query .= "     DET{$seq}.REMARK{$remarkName} AS SEQ{$seq}_REMARK{$remarkName}, ";
            }
        }
        $query .= "     N1.NAME1 AS TYPE_REASON1 ";
        $query .= " FROM ";
        $query .= "     NURSEOFF_VISITREC_DAT T1 ";
        foreach ($selectDet as $seq => $remarkArray) {
            $query .= "     LEFT JOIN NURSEOFF_VISITREC_DETAIL_DAT DET{$seq} ON T1.SCHREGNO = DET{$seq}.SCHREGNO ";
            $query .= "          AND T1.VISIT_DATE = DET{$seq}.VISIT_DATE ";
            $query .= "          AND T1.VISIT_HOUR = DET{$seq}.VISIT_HOUR ";
            $query .= "          AND T1.VISIT_MINUTE = DET{$seq}.VISIT_MINUTE ";
            $query .= "          AND T1.TYPE = DET{$seq}.TYPE ";
            $query .= "          AND DET{$seq}.SEQ = '{$seq}' ";
        }
        $query .= "     LEFT JOIN NAME_MST N1 ON DET02.REMARK1 = N1.NAMECD2 AND N1.NAMECD1 = 'F203' ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' AND ";
        $query .= "     T1.TYPE = '3' ";
        $query .= " ), TYPE4 AS( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.VISIT_DATE, ";
        $query .= "     T1.VISIT_HOUR, ";
        $query .= "     T1.VISIT_MINUTE, ";
        $query .= "     T1.TYPE, ";
        foreach ($selectDet as $seq => $remarkArray) {
            foreach ($remarkArray as $remarkName) {
                $query .= "     DET{$seq}.REMARK{$remarkName} AS SEQ{$seq}_REMARK{$remarkName}, ";
            }
        }
        $query .= "     N1.NAME1 AS TYPE_REASON1 ";
        $query .= " FROM ";
        $query .= "     NURSEOFF_VISITREC_DAT T1 ";
        foreach ($selectDet as $seq => $remarkArray) {
            $query .= "     LEFT JOIN NURSEOFF_VISITREC_DETAIL_DAT DET{$seq} ON T1.SCHREGNO = DET{$seq}.SCHREGNO ";
            $query .= "          AND T1.VISIT_DATE = DET{$seq}.VISIT_DATE ";
            $query .= "          AND T1.VISIT_HOUR = DET{$seq}.VISIT_HOUR ";
            $query .= "          AND T1.VISIT_MINUTE = DET{$seq}.VISIT_MINUTE ";
            $query .= "          AND T1.TYPE = DET{$seq}.TYPE ";
            $query .= "          AND DET{$seq}.SEQ = '{$seq}' ";
        }
        $query .= "     LEFT JOIN NAME_MST N1 ON DET02.REMARK1 = N1.NAMECD2 AND N1.NAMECD1 = 'F202' ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' AND ";
        $query .= "     T1.TYPE = '4' ";
        $query .= " ), TYPE5 AS( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.VISIT_DATE, ";
        $query .= "     T1.VISIT_HOUR, ";
        $query .= "     T1.VISIT_MINUTE, ";
        $query .= "     T1.TYPE, ";
        foreach ($selectDet as $seq => $remarkArray) {
            foreach ($remarkArray as $remarkName) {
                $query .= "     DET{$seq}.REMARK{$remarkName} AS SEQ{$seq}_REMARK{$remarkName}, ";
            }
        }
        $query .= "     N1.NAME1 AS TYPE_REASON1 ";
        $query .= " FROM ";
        $query .= "     NURSEOFF_VISITREC_DAT T1 ";
        foreach ($selectDet as $seq => $remarkArray) {
            $query .= "     LEFT JOIN NURSEOFF_VISITREC_DETAIL_DAT DET{$seq} ON T1.SCHREGNO = DET{$seq}.SCHREGNO ";
            $query .= "          AND T1.VISIT_DATE = DET{$seq}.VISIT_DATE ";
            $query .= "          AND T1.VISIT_HOUR = DET{$seq}.VISIT_HOUR ";
            $query .= "          AND T1.VISIT_MINUTE = DET{$seq}.VISIT_MINUTE ";
            $query .= "          AND T1.TYPE = DET{$seq}.TYPE ";
            $query .= "          AND DET{$seq}.SEQ = '{$seq}' ";
        }
        $query .= "     LEFT JOIN NAME_MST N1 ON DET02.REMARK1 = N1.NAMECD2 AND N1.NAMECD1 = 'F219' ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' AND ";
        $query .= "     T1.TYPE = '5' ";
        $query .= " ), MAIN AS( ";
        $query .= " SELECT * FROM TYPE1 ";
        $query .= " UNION ";
        $query .= " SELECT * FROM TYPE2 ";
        $query .= " UNION ";
        $query .= " SELECT * FROM TYPE3 ";
        $query .= " UNION ";
        $query .= " SELECT * FROM TYPE4 ";
        $query .= " UNION ";
        $query .= " SELECT * FROM TYPE5 ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.VISIT_DATE, ";
        $query .= "     T1.VISIT_HOUR, ";
        $query .= "     T1.VISIT_MINUTE, ";
        $query .= "     T1.TYPE, ";
        $query .= "     T1.TYPE_REASON1, ";
        foreach ($selectDet as $seq => $remarkArray) {
            foreach ($remarkArray as $remarkName) {
                $query .= "     T1.SEQ{$seq}_REMARK{$remarkName}, ";
            }
        }
        $query .= "     CASE T1.TYPE WHEN '1' THEN '内科' WHEN '2' THEN '外科' WHEN '3' THEN 'その他' WHEN '4' THEN '".$model->sch_label."以外' ELSE '健康相談' END TYPE_NAME ";
        $query .= " FROM ";
        $query .= "     MAIN T1 ";
        $query .= " ORDER BY ";
        $query .= "     T1.VISIT_DATE DESC, ";
        $query .= "     T1.VISIT_HOUR DESC, ";
        $query .= "     T1.VISIT_MINUTE DESC, ";
        $query .= "     T1.TYPE ";

        return $query;
    }

    // DELETE
    function &getDeleteQuery($model)
    {
        $array = explode (",", implode(',',$model->checked));

        for ($i = 0; $i < get_count($array); $i++){
        $db = Query::dbCheckOut();
            list($date, $hour, $minute, $type) = explode(":", $array[$i]);
            $query  = " DELETE FROM ";
            $query .= "     NURSEOFF_VISITREC_DAT ";
            $query .= " WHERE ";
            $query .= "     VISIT_DATE = '".$date."' AND ";
            $query .= "     VISIT_HOUR = '".$hour."' AND ";
            $query .= "     VISIT_MINUTE = '".$minute."' AND ";
            $query .= "     TYPE = '".$type."' AND ";
            $query .= "     SCHREGNO = '".$model->schregno."' ";

        $db->query($query);
        Query::dbCheckIn($db);
        }
        return ;
    }

    //１レコード取得
    function getRow($model)
    {
        $date = str_replace("/", "-", $model->visit_date);

        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.VISIT_DATE, ";
        $query .= "     T1.VISIT_HOUR, ";
        $query .= "     T1.VISIT_MINUTE ";
        foreach ($model->selectDetailSeq as $seq => $remarkArray) {
            foreach ($remarkArray as $remarkName) {
                $query .= "     , DET{$seq}.REMARK{$remarkName} AS SEQ{$seq}_REMARK{$remarkName} ";
            }
        }
        $query .= " FROM ";
        $query .= "     NURSEOFF_VISITREC_DAT T1 ";
        foreach ($model->selectDetailSeq as $seq => $remarkArray) {
            $query .= "     LEFT JOIN NURSEOFF_VISITREC_DETAIL_DAT DET{$seq} ON T1.SCHREGNO = DET{$seq}.SCHREGNO ";
            $query .= "          AND T1.VISIT_DATE = DET{$seq}.VISIT_DATE ";
            $query .= "          AND T1.VISIT_HOUR = DET{$seq}.VISIT_HOUR ";
            $query .= "          AND T1.VISIT_MINUTE = DET{$seq}.VISIT_MINUTE ";
            $query .= "          AND T1.TYPE = DET{$seq}.TYPE ";
            $query .= "          AND DET{$seq}.SEQ = '{$seq}' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO     = '".$model->schregno."' AND ";
        $query .= "     T1.VISIT_DATE   = '".$date."' AND ";
        $query .= "     T1.VISIT_HOUR   = '".$model->visit_hour."' AND ";
        $query .= "     T1.VISIT_MINUTE = '".$model->visit_minute."' AND ";
        $query .= "     T1.TYPE         = '".$model->type."' ";

        return $query;
    }

    //来室校時
    function getNameMstPeriod()
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'F700' ";
        $query .= " ORDER BY ";
        $query .= "     INT(NAMESPARE1), ";
        $query .= "     INT(VALUE) ";

        return $query;
    }

    //学校名取得
    function getSchoolName() {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= " AND NAMECD2 = '00' ";
        
        return $query;
    }

    //名称マスタ取得
    function getNameMst($namecd1, $namecd2="")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        if($namecd1 == "F224"){
            $query .= "     NAME1 AS LABEL, ";
        } else {
            $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        }
        $query .= "     NAMESPARE1, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if($namecd2){
            $query .= "     AND SUBSTR(NAMECD2,1,2) = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //部活マスタ取得
    function getClubMst()
    {
        $query  = " SELECT ";
        $query .= "     T1.CLUBCD AS VALUE, ";
        $query .= "     T1.CLUBCD || ':' || T2.CLUBNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CLUB_YDAT T1 ";
        $query .= "     LEFT JOIN CLUB_MST T2 ON T1.CLUBCD = T2.CLUBCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //存在チェック
    function getExistCheck($model)
    {
        $db = Query::dbCheckOut();

        $date = str_replace("/", "-", $model->field["SEQ01_REMARK1"]);

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     NURSEOFF_VISITREC_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO     = '".$model->schregno."' AND ";
        $query .= "     VISIT_DATE   = '".$date."' AND ";
        $query .= "     VISIT_HOUR   = '".$model->field["SEQ01_REMARK2"]."' AND ";
        $query .= "     VISIT_MINUTE = '".$model->field["SEQ01_REMARK3"]."' AND ";
        $query .= "     TYPE         = '".$model->type."' ";

        $row = $db->getCol($query);
        Query::dbCheckIn($db);
        return $row;
    }

    // UPDATE
    function &getUpdateQuery($model, $fields, $flg)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $visit_date = str_replace("/", "-", $fields["SEQ01_REMARK1"]);

        $data = array();
        $data["SCHREGNO"][TEXT]             = $model->schregno;
        $data["VISIT_DATE"][DATE]           = $visit_date;
        $data["VISIT_HOUR"][TEXT]           = $fields["SEQ01_REMARK2"];
        $data["VISIT_MINUTE"][TEXT]         = $fields["SEQ01_REMARK3"];
        $data["TYPE"][TEXT]                 = $model->type;
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        if($flg == "insert"){
            $query = Query::insertSQL($data, "NURSEOFF_VISITREC_DAT");
            $db->query($query);
        } else {
            $where  = " WHERE ";
            $where .= "     SCHREGNO     = '".$model->schregno."' AND ";
            $where .= "     VISIT_DATE   = '".$visit_date."' AND ";
            $where .= "     VISIT_HOUR   = '".$fields["SEQ01_REMARK2"]."' AND ";
            $where .= "     VISIT_MINUTE = '".$fields["SEQ01_REMARK3"]."' AND ";
            $where .= "     TYPE         = '".$model->type."' ";
            $query = Query::updateSQL($data, "NURSEOFF_VISITREC_DAT", $where);
            $db->query($query);
        }

        $query  = " DELETE FROM ";
        $query .= "     NURSEOFF_VISITREC_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     VISIT_DATE = '{$visit_date}' AND ";
        $query .= "     VISIT_HOUR = '{$fields["SEQ01_REMARK2"]}' AND ";
        $query .= "     VISIT_MINUTE = '{$fields["SEQ01_REMARK3"]}' AND ";
        $query .= "     TYPE = '{$model->type}' AND ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";
        $db->query($query);

        foreach ($model->selectDetailSeq as $seq => $remarkArray) {
            $data = array();
            $data["SCHREGNO"][TEXT]     = $model->schregno;
            $data["VISIT_DATE"][DATE]   = $visit_date;
            $data["VISIT_HOUR"][TEXT]   = $fields["SEQ01_REMARK2"];
            $data["VISIT_MINUTE"][TEXT] = $fields["SEQ01_REMARK3"];
            $data["TYPE"][TEXT]         = $model->type;
            $data["SEQ"][TEXT]          = $seq;
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][NUMBER]    = "sysdate()";
            $data["REMARK1"][TEXT]      = $fields["SEQ{$seq}_REMARK1"];
            $data["REMARK2"][TEXT]      = $fields["SEQ{$seq}_REMARK2"];
            $data["REMARK3"][TEXT]      = $fields["SEQ{$seq}_REMARK3"];
            $data["REMARK4"][TEXT]      = $fields["SEQ{$seq}_REMARK4"];
            $data["REMARK5"][TEXT]      = $fields["SEQ{$seq}_REMARK5"];
            $data["REMARK6"][TEXT]      = $fields["SEQ{$seq}_REMARK6"];
            $data["REMARK7"][TEXT]      = $fields["SEQ{$seq}_REMARK7"];
            $data["REMARK8"][TEXT]      = $fields["SEQ{$seq}_REMARK8"];
            $data["REMARK9"][TEXT]      = $fields["SEQ{$seq}_REMARK9"];
            $data["REMARK10"][TEXT]     = $fields["SEQ{$seq}_REMARK10"];
            $data["REMARK_L1"][TEXT]    = $fields["SEQ{$seq}_REMARK_L1"];

            $query = Query::insertSQL($data, "NURSEOFF_VISITREC_DETAIL_DAT");
            $db->query($query);
        }

        $db->commit();

        Query::dbCheckIn($db);
        return;
    }

    //テーブル存在チェック
    function checkTableExist() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSTABLES ";
        $query .= " WHERE ";
        $query .= "     NAME = 'SETTING_DAT' ";

        return $query;
    }

    //生徒項目名取得
    function getSchName($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     REMARK1, ";
        $query .= "     SCHOOLCD ";
        $query .= " FROM ";
        $query .= "     SETTING_DAT ";
        $query .= " WHERE ";
        $query .= "     SEQ = '001' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN (SELECT ";
                $query .= "                         S2.SCHOOL_KIND ";
                $query .= "                     FROM ";
                $query .= "                         SCHREG_REGD_DAT S1, ";
                $query .= "                         SCHREG_REGD_GDAT S2 ";
                $query .= "                     WHERE ";
                $query .= "                         S1.YEAR     = S2.YEAR AND ";
                $query .= "                         S1.YEAR     = '".CTRL_YEAR."' AND ";
                $query .= "                         S1.SEMESTER = '".CTRL_SEMESTER."' AND ";
                $query .= "                         S1.GRADE    = S2.GRADE AND ";
                $query .= "                         S1.SCHREGNO = '".$model->schregno."' ";
                $query .= "                     ) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        } else {
            $query .= " AND SCHOOL_KIND IN (SELECT ";
            $query .= "                         S2.SCHOOL_KIND ";
            $query .= "                     FROM ";
            $query .= "                         SCHREG_REGD_DAT S1, ";
            $query .= "                         SCHREG_REGD_GDAT S2 ";
            $query .= "                     WHERE ";
            $query .= "                         S1.YEAR     = S2.YEAR AND ";
            $query .= "                         S1.YEAR     = '".CTRL_YEAR."' AND ";
            $query .= "                         S1.SEMESTER = '".CTRL_SEMESTER."' AND ";
            $query .= "                         S1.GRADE    = S2.GRADE AND ";
            $query .= "                         S1.SCHREGNO = '".$model->schregno."' ";
            $query .= "                     ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     SCHOOLCD ";

        return $query;
    }
}
?>
