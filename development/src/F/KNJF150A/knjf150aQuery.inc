<?php

require_once('for_php7.php');

class knjf150aQuery extends Query
{
    //クラス名称取得
    public function getHrName($model)
    {
        $query  = " SELECT ";
        $query .= "     HR_NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER    = '".CTRL_SEMESTER."' AND ";
        $query .= "     GRADE       = '".$model->grade."' AND ";
        $query .= "     HR_CLASS    = '".$model->hr_class."' ";

        return $query;
    }

    //保健室来室記録データよりデータを取得
    public function selectQuery($model)
    {
        $query  = " WITH TYPE1 AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.*, ";
        $query .= "         N1.NAME1 AS TYPE_REASON1, ";
        $query .= "         N2.NAME1 AS TYPE_REASON2, ";
        $query .= "         N3.NAME1 AS TYPE_REASON3, ";
        $query .= "         N4.NAME1 AS TYPE_TREATMENT1, ";
        $query .= "         N5.NAME1 AS TYPE_TREATMENT2, ";
        $query .= "         N6.NAME1 AS TYPE_TREATMENT3 ";
        $query .= "     FROM ";
        $query .= "         NURSEOFF_VISITREC_DAT T1 ";
        $query .= "         LEFT JOIN NAME_MST N1 ON T1.VISIT_REASON1 = N1.NAMECD2 AND N1.NAMECD1 = 'F200' ";
        $query .= "         LEFT JOIN NAME_MST N2 ON T1.VISIT_REASON2 = N2.NAMECD2 AND N2.NAMECD1 = 'F200' ";
        $query .= "         LEFT JOIN NAME_MST N3 ON T1.VISIT_REASON3 = N3.NAMECD2 AND N3.NAMECD1 = 'F200' ";
        $query .= "         LEFT JOIN NAME_MST N4 ON T1.TREATMENT1 = N4.NAMECD2 AND N4.NAMECD1 = 'F208' ";
        $query .= "         LEFT JOIN NAME_MST N5 ON T1.TREATMENT2 = N5.NAMECD2 AND N5.NAMECD1 = 'F208' ";
        $query .= "         LEFT JOIN NAME_MST N6 ON T1.TREATMENT3 = N6.NAMECD2 AND N6.NAMECD1 = 'F208' ";
        $query .= "     WHERE ";
        $query .= "         T1.SCHREGNO = '".$model->schregno."' AND ";
        $query .= "         T1.TYPE     = '1' ";
        $query .= " ), TYPE2 AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.*, ";
        $query .= "         N1.NAME1 AS TYPE_REASON1, ";
        $query .= "         N2.NAME1 AS TYPE_REASON2, ";
        $query .= "         N3.NAME1 AS TYPE_REASON3, ";
        $query .= "         N4.NAME1 AS TYPE_TREATMENT1, ";
        $query .= "         N5.NAME1 AS TYPE_TREATMENT2, ";
        $query .= "         N6.NAME1 AS TYPE_TREATMENT3 ";
        $query .= "     FROM ";
        $query .= "         NURSEOFF_VISITREC_DAT T1 ";
        $query .= "         LEFT JOIN NAME_MST N1 ON T1.VISIT_REASON1 = N1.NAMECD2 AND N1.NAMECD1 = 'F201' ";
        $query .= "         LEFT JOIN NAME_MST N2 ON T1.VISIT_REASON2 = N2.NAMECD2 AND N2.NAMECD1 = 'F201' ";
        $query .= "         LEFT JOIN NAME_MST N3 ON T1.VISIT_REASON3 = N3.NAMECD2 AND N3.NAMECD1 = 'F201' ";
        $query .= "         LEFT JOIN NAME_MST N4 ON T1.TREATMENT1 = N4.NAMECD2 AND N4.NAMECD1 = 'F209' ";
        $query .= "         LEFT JOIN NAME_MST N5 ON T1.TREATMENT2 = N5.NAMECD2 AND N5.NAMECD1 = 'F209' ";
        $query .= "         LEFT JOIN NAME_MST N6 ON T1.TREATMENT3 = N6.NAMECD2 AND N6.NAMECD1 = 'F209' ";
        $query .= "     WHERE ";
        $query .= "         T1.SCHREGNO = '".$model->schregno."' AND ";
        $query .= "         T1.TYPE     = '2' ";
        $query .= " ), MAIN AS( ";
        $query .= "     SELECT * FROM TYPE1 ";
        $query .= "     UNION ";
        $query .= "     SELECT * FROM TYPE2 ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.VISIT_DATE, ";
        $query .= "     T1.VISIT_HOUR, ";
        $query .= "     T1.VISIT_MINUTE, ";
        $query .= "     T1.TYPE, ";
        $query .= "     CASE T1.TYPE WHEN '1' THEN '内科' WHEN '2' THEN '外科' END TYPE_NAME, ";
        $query .= "     T1.TYPE_REASON1 AS VISIT_REASON1, ";
        $query .= "     T1.TYPE_REASON2 AS VISIT_REASON2, ";
        $query .= "     T1.TYPE_REASON3 AS VISIT_REASON3, ";
        $query .= "     T1.TYPE_TREATMENT1 AS TREATMENT1, ";
        $query .= "     T1.TYPE_TREATMENT2 AS TREATMENT2, ";
        $query .= "     T1.TYPE_TREATMENT3 AS TREATMENT3, ";
        $query .= "     N1.NAME1 AS RESTTIME, ";
        $query .= "     N2.NAME1 AS CONTACT, ";
        $query .= "     T1.SPECIAL_NOTE ";
        $query .= " FROM ";
        $query .= "     MAIN T1 ";
        $query .= "     LEFT JOIN NAME_MST N1 ON T1.RESTTIME = N1.NAMECD2 AND N1.NAMECD1 = 'F212' ";
        $query .= "     LEFT JOIN NAME_MST N2 ON T1.CONTACT = N2.NAMECD2 AND N2.NAMECD1 = 'F213' ";
        $query .= " ORDER BY ";
        $query .= "     T1.VISIT_DATE DESC, ";
        $query .= "     T1.VISIT_HOUR DESC, ";
        $query .= "     T1.VISIT_MINUTE DESC, ";
        $query .= "     T1.TYPE ";

        return $query;
    }

    // DELETE
    public function &getDeleteQuery($model)
    {
        $array = explode(",", implode(',', $model->checked));

        for ($i = 0; $i < get_count($array); $i++) {
            $db = Query::dbCheckOut();
            list($date, $hour, $minute, $type) = explode(":", $array[$i]);

            $query  = " DELETE FROM ";
            $query .= "     NURSEOFF_VISITREC_DAT ";
            $query .= " WHERE ";
            $query .= "     VISIT_DATE      = '".$date."' AND ";
            $query .= "     VISIT_HOUR      = '".$hour."' AND ";
            $query .= "     VISIT_MINUTE    = '".$minute."' AND ";
            $query .= "     TYPE            = '".$type."' AND ";
            $query .= "     SCHREGNO        = '".$model->schregno."' ";

            $db->query($query);
            Query::dbCheckIn($db);
        }
        return ;
    }

    //１レコード取得
    public function getRow($model)
    {
        $date = str_replace("/", "-", $model->visit_date);

        $query  = " SELECT ";
        $query .= "     N1.*, ";
        $query .= "     N_D11.REMARK1 AS SEQ11_REMARK1, ";
        $query .= "     N_D12.REMARK1 AS SEQ12_REMARK1, ";
        $query .= "     N_D13.REMARK1 AS SEQ13_REMARK1, ";
        $query .= "     N_D14.REMARK1 AS SEQ14_REMARK1, ";
        $query .= "     N_D15.REMARK1 AS SEQ15_REMARK1 ";
        $query .= " FROM ";
        $query .= "     NURSEOFF_VISITREC_DAT N1 ";
        $query .= "     LEFT JOIN NURSEOFF_VISITREC_DETAIL_DAT N_D11 ";
        $query .= "          ON N_D11.SCHREGNO     = N1.SCHREGNO ";
        $query .= "         AND N_D11.VISIT_DATE   = N1.VISIT_DATE ";
        $query .= "         AND N_D11.VISIT_HOUR   = N1.VISIT_HOUR ";
        $query .= "         AND N_D11.VISIT_MINUTE = N1.VISIT_MINUTE ";
        $query .= "         AND N_D11.TYPE         = N1.TYPE ";
        $query .= "         AND N_D11.SEQ          = '11' ";
        $query .= "     LEFT JOIN NURSEOFF_VISITREC_DETAIL_DAT N_D12 ";
        $query .= "          ON N_D12.SCHREGNO     = N1.SCHREGNO ";
        $query .= "         AND N_D12.VISIT_DATE   = N1.VISIT_DATE ";
        $query .= "         AND N_D12.VISIT_HOUR   = N1.VISIT_HOUR ";
        $query .= "         AND N_D12.VISIT_MINUTE = N1.VISIT_MINUTE ";
        $query .= "         AND N_D12.TYPE         = N1.TYPE ";
        $query .= "         AND N_D12.SEQ          = '12' ";
        $query .= "     LEFT JOIN NURSEOFF_VISITREC_DETAIL_DAT N_D13 ";
        $query .= "          ON N_D13.SCHREGNO     = N1.SCHREGNO ";
        $query .= "         AND N_D13.VISIT_DATE   = N1.VISIT_DATE ";
        $query .= "         AND N_D13.VISIT_HOUR   = N1.VISIT_HOUR ";
        $query .= "         AND N_D13.VISIT_MINUTE = N1.VISIT_MINUTE ";
        $query .= "         AND N_D13.TYPE         = N1.TYPE ";
        $query .= "         AND N_D13.SEQ          = '13' ";
        $query .= "     LEFT JOIN NURSEOFF_VISITREC_DETAIL_DAT N_D14 ";
        $query .= "          ON N_D14.SCHREGNO     = N1.SCHREGNO ";
        $query .= "         AND N_D14.VISIT_DATE   = N1.VISIT_DATE ";
        $query .= "         AND N_D14.VISIT_HOUR   = N1.VISIT_HOUR ";
        $query .= "         AND N_D14.VISIT_MINUTE = N1.VISIT_MINUTE ";
        $query .= "         AND N_D14.TYPE         = N1.TYPE ";
        $query .= "         AND N_D14.SEQ          = '14' ";
        $query .= "     LEFT JOIN NURSEOFF_VISITREC_DETAIL_DAT N_D15 ";
        $query .= "          ON N_D15.SCHREGNO     = N1.SCHREGNO ";
        $query .= "         AND N_D15.VISIT_DATE   = N1.VISIT_DATE ";
        $query .= "         AND N_D15.VISIT_HOUR   = N1.VISIT_HOUR ";
        $query .= "         AND N_D15.VISIT_MINUTE = N1.VISIT_MINUTE ";
        $query .= "         AND N_D15.TYPE         = N1.TYPE ";
        $query .= "         AND N_D15.SEQ          = '15' ";
        $query .= " WHERE ";
        $query .= "     N1.SCHREGNO     = '".$model->schregno."' AND ";
        $query .= "     N1.VISIT_DATE   = '".$date."' AND ";
        $query .= "     N1.VISIT_HOUR   = '".$model->visit_hour."' AND ";
        $query .= "     N1.VISIT_MINUTE = '".$model->visit_minute."' AND ";
        $query .= "     N1.TYPE         = '".$model->type."' ";

        return $query;
    }

    //来室校時
    public function getNameMstPeriod()
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAME1 AS LABEL2 ";
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

    //名称マスタ取得
    public function getNameMst($namecd1, $namecd2 = "")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAME1 AS LABEL2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= "     AND SUBSTR(NAMECD2,1,2) = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //存在チェック
    public function getExistCheck($model)
    {
        $db = Query::dbCheckOut();

        $date = str_replace("/", "-", $model->field["VISIT_DATE"]);

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     NURSEOFF_VISITREC_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO     = '".$model->schregno."' AND ";
        $query .= "     VISIT_DATE   = '".$date."' AND ";
        $query .= "     VISIT_HOUR   = '".$model->field["VISIT_HOUR"]."' AND ";
        $query .= "     VISIT_MINUTE = '".$model->field["VISIT_MINUTE"]."' AND ";
        $query .= "     TYPE         = '".$model->type."' ";

        $row = $db->getCol($query);
        Query::dbCheckIn($db);
        return $row;
    }

    //MAXシリアル番号取得
    public function getMaxSerialNumber($model, $visit_date)
    {
        $query  = " SELECT ";
        $query .= "     MAX(SERIAL_NUMBER) AS MAX_NUM ";
        $query .= " FROM ";
        $query .= "     NURSEOFF_VISITREC_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$model->schregno."' AND ";
        $query .= "     TYPE IN ('1', '2') AND ";
        $query .= "     FISCALYEAR(VISIT_DATE) = FISCALYEAR(DATE('".$visit_date."')) ";

        return $query;
    }

    // UPDATE
    public function &getUpdateQuery($model, $fields, $flg)
    {
        $db = Query::dbCheckOut();
        $visit_date = str_replace("/", "-", $fields["VISIT_DATE"]);

        $serial_number = $db->getOne(knjf150aQuery::getMaxSerialNumber($model, $visit_date));

        $data = array();
        $data["SCHREGNO"][TEXT]             = $model->schregno;
        $data["VISIT_DATE"][DATE]           = $visit_date;
        $data["VISIT_HOUR"][TEXT]           = $fields["VISIT_HOUR"];
        $data["VISIT_MINUTE"][TEXT]         = $fields["VISIT_MINUTE"];
        $data["TYPE"][TEXT]                 = $model->type;
        $data["VISIT_PERIODCD"][TEXT]       = ($fields["VISIT_PERIODCD"] == "BLANK") ? "" : $fields["VISIT_PERIODCD"];
        $data["LESSON_CLASS"][TEXT]         = $fields["LESSON_CLASS"];
        $data["VISIT_REASON1"][TEXT]        = $fields["VISIT_REASON1"];
        $data["VISIT_REASON1_TEXT"][TEXT]   = $fields["VISIT_REASON1_TEXT"];
        $data["VISIT_REASON2"][TEXT]        = $fields["VISIT_REASON2"];
        $data["VISIT_REASON2_TEXT"][TEXT]   = $fields["VISIT_REASON2_TEXT"];
        $data["VISIT_REASON3"][TEXT]        = $fields["VISIT_REASON3"];
        $data["VISIT_REASON3_TEXT"][TEXT]   = $fields["VISIT_REASON3_TEXT"];
        if ($model->type == "1") {
            $data["SINCE_WHEN"][TEXT]           = ($fields["SINCE_WHEN"] == "BLANK") ? "" : $fields["SINCE_WHEN"];
            $data["SINCE_WHEN_TEXT"][TEXT]      = $fields["SINCE_WHEN_TEXT"];
            $data["CONDITION1"][TEXT]           = ($fields["CONDITION1"] == "BLANK") ? "" : $fields["CONDITION1"];
            $data["SLEEPTIME"][TEXT]            = ($fields["SLEEPTIME"]) ? intval($fields["SLEEPTIME"]) : "";
            $data["SLEEPTIME_M"][TEXT]          = ($fields["SLEEPTIME_M"]) ? intval($fields["SLEEPTIME_M"]) : "";
            $data["BEDTIME_H"][TEXT]            = ($fields["BEDTIME_H"]) ? intval($fields["BEDTIME_H"]) : "";
            $data["BEDTIME_M"][TEXT]            = ($fields["BEDTIME_M"]) ? intval($fields["BEDTIME_M"]) : "";
            $data["CONDITION3"][TEXT]           = ($fields["CONDITION3"] == "BLANK") ? "" : $fields["CONDITION3"];
            $data["CONDITION4"][TEXT]           = ($fields["CONDITION4"] == "BLANK") ? "" : $fields["CONDITION4"];
            $data["CONDITION7"][TEXT]           = ($fields["CONDITION7"] == "BLANK") ? "" : $fields["CONDITION7"];
            $data["CONDITION7_TEXT"][TEXT]      = $fields["CONDITION7_TEXT"];
            $data["CONDITION8"][TEXT]           = ($fields["CONDITION8"] == "BLANK") ? "" : $fields["CONDITION8"];
            $data["CONDITION8_TEXT"][TEXT]      = $fields["CONDITION8_TEXT"];
            $data["CONDITION9"][TEXT]           = ($fields["CONDITION9"] == "BLANK") ? "" : $fields["CONDITION9"];
            $data["CONDITION10"][TEXT]          = ($fields["CONDITION10"] == "BLANK") ? "" : $fields["CONDITION10"];
            $data["CONDITION11"][TEXT]          = ($fields["CONDITION11"] == "BLANK") ? "" : $fields["CONDITION11"];
            $data["CONDITION12"][TEXT]          = ($fields["CONDITION12"] == "BLANK") ? "" : $fields["CONDITION12"];
            $data["CONDITION12_TEXT"][TEXT]     = $fields["CONDITION12_TEXT"];
            $data["TEMPERATURE1"][TEXT]         = $fields["TEMPERATURE1"];
            $data["MEASURE_HOUR1"][TEXT]        = ($fields["MEASURE_HOUR1"]) ? sprintf("%02d", $fields["MEASURE_HOUR1"]) : "";
            $data["MEASURE_MINUTE1"][TEXT]      = ($fields["MEASURE_MINUTE1"]) ? sprintf("%02d", $fields["MEASURE_MINUTE1"]) : "";
            $data["PULSE"][TEXT]                = $fields["PULSE"];
            $data["BLOOD_PRESSURE_L"][TEXT]     = $fields["BLOOD_PRESSURE_L"];
            $data["BLOOD_PRESSURE_H"][TEXT]     = $fields["BLOOD_PRESSURE_H"];
        }
        if ($model->type == "2") {
            $data["INJURY_PART1"][TEXT]         = $fields["INJURY_PART1"];
            $data["INJURY_PART1_TEXT"][TEXT]    = $fields["INJURY_PART1_TEXT"];
            $data["INJURY_PART2"][TEXT]         = $fields["INJURY_PART2"];
            $data["INJURY_PART2_TEXT"][TEXT]    = $fields["INJURY_PART2_TEXT"];
            $data["INJURY_PART3"][TEXT]         = $fields["INJURY_PART3"];
            $data["INJURY_PART3_TEXT"][TEXT]    = $fields["INJURY_PART3_TEXT"];
            $data["OCCUR_DATE"][TEXT]           = str_replace("/", "-", $fields["OCCUR_DATE"]);
            $data["OCCUR_HOUR"][TEXT]           = ($fields["OCCUR_HOUR"]) ? sprintf("%02d", $fields["OCCUR_HOUR"]) : "";
            $data["OCCUR_MINUTE"][TEXT]         = ($fields["OCCUR_MINUTE"]) ? sprintf("%02d", $fields["OCCUR_MINUTE"]) : "";
            $data["OCCUR_ACT"][TEXT]            = ($fields["OCCUR_ACT"] == "BLANK") ? "" : $fields["OCCUR_ACT"];
            $data["OCCUR_SITUATION"][TEXT]      = $fields["OCCUR_SITUATION"];
            $data["OCCUR_PLACE"][TEXT]          = ($fields["OCCUR_PLACE"] == "BLANK") ? "" : $fields["OCCUR_PLACE"];
            $data["OCCUR_PLACE_TEXT"][TEXT]     = $fields["OCCUR_PLACE_TEXT"];
        }
        $data["TREATMENT1"][TEXT]           = ($fields["TREATMENT1"] == "BLANK") ? "" : $fields["TREATMENT1"];
        $data["TREATMENT1_TEXT"][TEXT]      = $fields["TREATMENT1_TEXT"];
        $data["TREATMENT2"][TEXT]           = ($fields["TREATMENT2"] == "BLANK") ? "" : $fields["TREATMENT2"];
        $data["TREATMENT2_TEXT"][TEXT]      = $fields["TREATMENT2_TEXT"];
        $data["TREATMENT3"][TEXT]           = ($fields["TREATMENT3"] == "BLANK") ? "" : $fields["TREATMENT3"];
        $data["TREATMENT3_TEXT"][TEXT]      = $fields["TREATMENT3_TEXT"];
        $data["RESTTIME"][TEXT]             = ($fields["RESTTIME"] == "BLANK") ? "" : $fields["RESTTIME"];
        $data["CONTACT"][TEXT]              = ($fields["CONTACT"] == "BLANK") ? "" : $fields["CONTACT"];
        $data["CONTACT_TEXT"][TEXT]         = $fields["CONTACT_TEXT"];
        $data["SPECIAL_NOTE"][TEXT]         = $fields["SPECIAL_NOTE"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        if ($flg == "insert") {
            $data["SERIAL_NUMBER"][TEXT]         = $serial_number + 1;
            $db->query(Query::insertSQL($data, "NURSEOFF_VISITREC_DAT"));
        } else {
            $where  = " WHERE ";
            $where .= "     SCHREGNO     = '".$model->schregno."' AND ";
            $where .= "     VISIT_DATE   = '".$visit_date."' AND ";
            $where .= "     VISIT_HOUR   = '".$fields["VISIT_HOUR"]."' AND ";
            $where .= "     VISIT_MINUTE = '".$fields["VISIT_MINUTE"]."' AND ";
            $where .= "     TYPE         = '".$model->type."' ";

            $db->query(Query::updateSQL($data, "NURSEOFF_VISITREC_DAT", $where));
        }

        foreach ($model->selectDetailSeq as $seq => $val) {
            if (in_array($model->type, $val["types"])) {
                $query  = " DELETE FROM ";
                $query .= "     NURSEOFF_VISITREC_DETAIL_DAT ";
                $query .= " WHERE ";
                $query .= "     SCHREGNO     = '".$model->schregno."' AND ";
                $query .= "     VISIT_DATE   = '".$visit_date."' AND ";
                $query .= "     VISIT_HOUR   = '".$fields["VISIT_HOUR"]."' AND ";
                $query .= "     VISIT_MINUTE = '".$fields["VISIT_MINUTE"]."' AND ";
                $query .= "     TYPE         = '".$model->type."' AND ";
                $query .= "     SEQ          = '".$seq."' ";
                $db->query($query);

                $data = array();
                $data["SCHREGNO"][TEXT]     = $model->schregno;
                $data["VISIT_DATE"][DATE]   = $visit_date;
                $data["VISIT_HOUR"][TEXT]   = $fields["VISIT_HOUR"];
                $data["VISIT_MINUTE"][TEXT] = $fields["VISIT_MINUTE"];
                $data["TYPE"][TEXT]         = $model->type;
                $data["SEQ"][TEXT]          = $seq;
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][NUMBER]    = "sysdate()";
                $data["REMARK1"][TEXT]      = $fields["SEQ{$seq}_REMARK1"];
                $query = Query::insertSQL($data, "NURSEOFF_VISITREC_DETAIL_DAT");

                $db->query($query);
            }
        }

        Query::dbCheckIn($db);
        return;
    }

    //テーブル存在チェック
    public function checkTableExist()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSTABLES ";
        $query .= " WHERE ";
        $query .= "     NAME = 'SETTING_DAT' ";

        return $query;
    }

    //生徒項目名取得
    public function getSchName($model)
    {
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
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
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
