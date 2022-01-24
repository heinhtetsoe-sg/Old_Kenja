<?php

require_once('for_php7.php');

class knjf150Query extends Query
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
        $query  = " WITH TYPE1 AS( ";
        $query .= " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     N1.NAME1 AS TYPE_REASON1, ";
        $query .= "     N2.NAME1 AS TYPE_REASON2, ";
        $query .= "     N3.NAME1 AS TYPE_REASON3, ";
        $query .= "     N4.NAME1 AS TYPE_TREATMENT1, ";
        $query .= "     N5.NAME1 AS TYPE_TREATMENT2, ";
        $query .= "     N6.NAME1 AS TYPE_TREATMENT3 ";
        $query .= " FROM ";
        $query .= "     NURSEOFF_VISITREC_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST N1 ON T1.VISIT_REASON1 = N1.NAMECD2 AND N1.NAMECD1 = 'F200' ";
        $query .= "     LEFT JOIN NAME_MST N2 ON T1.VISIT_REASON2 = N2.NAMECD2 AND N2.NAMECD1 = 'F200' ";
        $query .= "     LEFT JOIN NAME_MST N3 ON T1.VISIT_REASON3 = N3.NAMECD2 AND N3.NAMECD1 = 'F200' ";
        $query .= "     LEFT JOIN NAME_MST N4 ON T1.TREATMENT1 = N4.NAMECD2 AND N4.NAMECD1 = 'F208' ";
        $query .= "     LEFT JOIN NAME_MST N5 ON T1.TREATMENT2 = N5.NAMECD2 AND N5.NAMECD1 = 'F208' ";
        $query .= "     LEFT JOIN NAME_MST N6 ON T1.TREATMENT3 = N6.NAMECD2 AND N6.NAMECD1 = 'F208' ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' AND ";
        $query .= "     T1.TYPE = '1' ";
        $query .= " ), TYPE2 AS( ";
        $query .= " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     N1.NAME1 AS TYPE_REASON1, ";
        $query .= "     N2.NAME1 AS TYPE_REASON2, ";
        $query .= "     N3.NAME1 AS TYPE_REASON3, ";
        $query .= "     N4.NAME1 AS TYPE_TREATMENT1, ";
        $query .= "     N5.NAME1 AS TYPE_TREATMENT2, ";
        $query .= "     N6.NAME1 AS TYPE_TREATMENT3 ";
        $query .= " FROM ";
        $query .= "     NURSEOFF_VISITREC_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST N1 ON T1.VISIT_REASON1 = N1.NAMECD2 AND N1.NAMECD1 = 'F201' ";
        $query .= "     LEFT JOIN NAME_MST N2 ON T1.VISIT_REASON2 = N2.NAMECD2 AND N2.NAMECD1 = 'F201' ";
        $query .= "     LEFT JOIN NAME_MST N3 ON T1.VISIT_REASON3 = N3.NAMECD2 AND N3.NAMECD1 = 'F201' ";
        $query .= "     LEFT JOIN NAME_MST N4 ON T1.TREATMENT1 = N4.NAMECD2 AND N4.NAMECD1 = 'F209' ";
        $query .= "     LEFT JOIN NAME_MST N5 ON T1.TREATMENT2 = N5.NAMECD2 AND N5.NAMECD1 = 'F209' ";
        $query .= "     LEFT JOIN NAME_MST N6 ON T1.TREATMENT3 = N6.NAMECD2 AND N6.NAMECD1 = 'F209' ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' AND ";
        $query .= "     T1.TYPE = '2' ";
        $query .= " ), TYPE3 AS( ";
        $query .= " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     N1.NAME1 AS TYPE_REASON1, ";
        $query .= "     N2.NAME1 AS TYPE_REASON2, ";
        $query .= "     N3.NAME1 AS TYPE_REASON3, ";
        $query .= "     N4.NAME1 AS TYPE_TREATMENT1, ";
        $query .= "     N5.NAME1 AS TYPE_TREATMENT2, ";
        $query .= "     N6.NAME1 AS TYPE_TREATMENT3 ";
        $query .= " FROM ";
        $query .= "     NURSEOFF_VISITREC_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST N1 ON T1.VISIT_REASON1 = N1.NAMECD2 AND N1.NAMECD1 = 'F203' ";
        $query .= "     LEFT JOIN NAME_MST N2 ON T1.VISIT_REASON2 = N2.NAMECD2 AND N2.NAMECD1 = 'F203' ";
        $query .= "     LEFT JOIN NAME_MST N3 ON T1.VISIT_REASON3 = N3.NAMECD2 AND N3.NAMECD1 = 'F203' ";
        $query .= "     LEFT JOIN NAME_MST N4 ON T1.TREATMENT1 = N4.NAMECD2 AND N4.NAMECD1 = 'F210' ";
        $query .= "     LEFT JOIN NAME_MST N5 ON T1.TREATMENT2 = N5.NAMECD2 AND N5.NAMECD1 = 'F210' ";
        $query .= "     LEFT JOIN NAME_MST N6 ON T1.TREATMENT3 = N6.NAMECD2 AND N6.NAMECD1 = 'F210' ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' AND ";
        $query .= "     T1.TYPE = '3' ";
        $query .= " ), TYPE4 AS( ";
        $query .= " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     N1.NAME1 AS TYPE_REASON1, ";
        $query .= "     N2.NAME1 AS TYPE_REASON2, ";
        $query .= "     N3.NAME1 AS TYPE_REASON3, ";
        $query .= "     N4.NAME1 AS TYPE_TREATMENT1, ";
        $query .= "     N5.NAME1 AS TYPE_TREATMENT2, ";
        $query .= "     N6.NAME1 AS TYPE_TREATMENT3 ";
        $query .= " FROM ";
        $query .= "     NURSEOFF_VISITREC_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST N1 ON T1.VISIT_REASON1 = N1.NAMECD2 AND N1.NAMECD1 = 'F202' ";
        $query .= "     LEFT JOIN NAME_MST N2 ON T1.VISIT_REASON2 = N2.NAMECD2 AND N2.NAMECD1 = 'F202' ";
        $query .= "     LEFT JOIN NAME_MST N3 ON T1.VISIT_REASON3 = N3.NAMECD2 AND N3.NAMECD1 = 'F202' ";
        $query .= "     LEFT JOIN NAME_MST N4 ON T1.TREATMENT1 = N4.NAMECD2 AND N4.NAMECD1 = 'F210' ";
        $query .= "     LEFT JOIN NAME_MST N5 ON T1.TREATMENT2 = N5.NAMECD2 AND N5.NAMECD1 = 'F210' ";
        $query .= "     LEFT JOIN NAME_MST N6 ON T1.TREATMENT3 = N6.NAMECD2 AND N6.NAMECD1 = 'F210' ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' AND ";
        $query .= "     T1.TYPE = '4' ";
        $query .= " ), TYPE5 AS( ";
        $query .= " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     N1.NAME1 AS TYPE_REASON1, ";
        $query .= "     N2.NAME1 AS TYPE_REASON2, ";
        $query .= "     N3.NAME1 AS TYPE_REASON3, ";
        $query .= "     N4.NAME1 AS TYPE_TREATMENT1, ";
        $query .= "     N5.NAME1 AS TYPE_TREATMENT2, ";
        $query .= "     N6.NAME1 AS TYPE_TREATMENT3 ";
        $query .= " FROM ";
        $query .= "     NURSEOFF_VISITREC_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST N1 ON T1.VISIT_REASON1 = N1.NAMECD2 AND N1.NAMECD1 = 'F219' ";
        $query .= "     LEFT JOIN NAME_MST N2 ON T1.VISIT_REASON2 = N2.NAMECD2 AND N2.NAMECD1 = 'F219' ";
        $query .= "     LEFT JOIN NAME_MST N3 ON T1.VISIT_REASON3 = N3.NAMECD2 AND N3.NAMECD1 = 'F219' ";
        $query .= "     LEFT JOIN NAME_MST N4 ON T1.TREATMENT1 = N4.NAMECD2 AND N4.NAMECD1 = 'F220' ";
        $query .= "     LEFT JOIN NAME_MST N5 ON T1.TREATMENT2 = N5.NAMECD2 AND N5.NAMECD1 = 'F220' ";
        $query .= "     LEFT JOIN NAME_MST N6 ON T1.TREATMENT3 = N6.NAMECD2 AND N6.NAMECD1 = 'F220' ";
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
        $query .= "     CASE T1.TYPE WHEN '1' THEN '内科' WHEN '2' THEN '外科' WHEN '3' THEN 'その他' WHEN '4' THEN '".$model->sch_label."以外' ELSE '健康相談' END TYPE_NAME, ";
        $query .= "     T1.TYPE_REASON1 AS VISIT_REASON1, ";
        $query .= "     T1.TYPE_REASON2 AS VISIT_REASON2, ";
        $query .= "     T1.TYPE_REASON3 AS VISIT_REASON3, ";
        $query .= "     CASE T1.CONDITION1 WHEN '1' THEN 'はい' WHEN '2' THEN 'いいえ' WHEN '3' THEN '余り眠れない' ELSE '' END CONDITION1, ";
        $query .= "     CASE T1.CONDITION3 WHEN '1' THEN 'はい' WHEN '2' THEN 'いいえ' WHEN '3' THEN '便秘' ELSE '' END CONDITION3, ";
        $query .= "     CASE T1.CONDITION4 WHEN '1' THEN '食べた' WHEN '2' THEN '食べていない' ELSE '' END CONDITION4, ";
        $query .= "     CASE T1.CONDITION5 WHEN '1' THEN 'ある' WHEN '2' THEN 'ない' ELSE '' END CONDITION5, ";
        $query .= "     T1.CONDITION6, ";
        $query .= "     T1.TYPE_TREATMENT1 AS TREATMENT1, ";
        $query .= "     T1.TYPE_TREATMENT2 AS TREATMENT2, ";
        $query .= "     T1.TYPE_TREATMENT3 AS TREATMENT3, ";
        $query .= "     N1.NAME1 AS RESTTIME, ";
        $query .= "     N2.NAME1 AS CONTACT, ";
        $query .= "     T1.HOSPITAL, ";
        $query .= "     T1.COMPANION, ";
        $query .= "     N3.NAME1 AS COMPANION_DIV, ";
        $query .= "     T1.DIAGNOSIS, ";
        $query .= "     T1.SPECIAL_NOTE ";
        $query .= " FROM ";
        $query .= "     MAIN T1 ";
        $query .= "     LEFT JOIN NAME_MST N1 ON T1.RESTTIME = N1.NAMECD2 AND N1.NAMECD1 = 'F212' ";
        $query .= "     LEFT JOIN NAME_MST N2 ON T1.CONTACT = N2.NAMECD2 AND N2.NAMECD1 = 'F213' ";
        $query .= "     LEFT JOIN NAME_MST N3 ON T1.COMPANION_DIV = N3.NAMECD2 AND N3.NAMECD1 = 'F218' ";
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
    public function getRow($model)
    {
        $date = str_replace("/", "-", $model->visit_date);

        $query  = " SELECT ";
        $query .= "     T1.* ";
        if ($model->schoolName != "miyagiken") {
            $query .= "   , T2.REMARK1 AS OCCUR_PLACE_TEXT2, ";
            $query .= "     T2.REMARK2 AS RESTTIME_TEXT, ";
            $query .= "     T2.REMARK3 AS OCCUR_CAUSE_TEXT, ";
            $query .= "     T2.REMARK4 AS RESULT_RETCLS, ";
            $query .= "     T2.REMARK5 AS RELATIONSHIP_TEXT ";
        }
        $query .= " FROM ";
        $query .= "     NURSEOFF_VISITREC_DAT T1 ";
        if ($model->schoolName != "miyagiken") {
            $query .= "     LEFT JOIN NURSEOFF_VISITREC_DETAIL_DAT T2 ";
            $query .= "       ON T2.SCHREGNO = T1.SCHREGNO ";
            $query .= "      AND T2.VISIT_DATE = T1.VISIT_DATE ";
            $query .= "      AND T2.VISIT_HOUR = T1.VISIT_HOUR ";
            $query .= "      AND T2.VISIT_MINUTE = T1.VISIT_MINUTE ";
            $query .= "      AND T2.TYPE = T1.TYPE";
            $query .= "      AND T2.SEQ = '10' ";
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
    public function getNameMstPeriod()
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
    public function getSchoolName()
    {
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
    public function getNameMst($namecd1, $namecd2 = "")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE1, ";
        $query .= "     NAMESPARE2 ";
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

    //部活マスタ取得
    public function getClubMst()
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

    // UPDATE
    public function &getUpdateQuery($model, $fields, $flg)
    {
        $db = Query::dbCheckOut();

        $visit_date = str_replace("/", "-", $fields["VISIT_DATE"]);

        $data["SCHREGNO"][TEXT]             = $model->schregno;
        $data["VISIT_DATE"][DATE]           = $visit_date;
        $data["VISIT_HOUR"][TEXT]           = $fields["VISIT_HOUR"];
        $data["VISIT_MINUTE"][TEXT]         = $fields["VISIT_MINUTE"];
        $data["TYPE"][TEXT]                 = $model->type;
        $data["VISIT_PERIODCD"][TEXT]       = $fields["VISIT_PERIODCD"];
        $data["CONDITION1"][TEXT]           = ($fields["CONDITION1"] == "9") ? "" : $fields["CONDITION1"];
        $data["SLEEPTIME"][TEXT]            = ($fields["SLEEPTIME"]) ? intval($fields["SLEEPTIME"]) : "";
        $data["CONDITION3"][TEXT]           = ($fields["CONDITION3"] == "9") ? "" : $fields["CONDITION3"];
        $data["CONDITION4"][TEXT]           = ($fields["CONDITION4"] == "9") ? "" : $fields["CONDITION4"];
        $data["MEAL"][TEXT]                 = $fields["MEAL"];
        $data["CONDITION5"][TEXT]           = ($fields["CONDITION5"] == "9") ? "" : $fields["CONDITION5"];
        $data["CONDITION6"][TEXT]           = $fields["CONDITION6"];
        $data["VISIT_REASON1"][TEXT]        = $fields["VISIT_REASON1"];
        $data["VISIT_REASON1_TEXT"][TEXT]   = $fields["VISIT_REASON1_TEXT"];
        $data["VISIT_REASON2"][TEXT]        = $fields["VISIT_REASON2"];
        $data["VISIT_REASON2_TEXT"][TEXT]   = $fields["VISIT_REASON2_TEXT"];
        $data["VISIT_REASON3"][TEXT]        = $fields["VISIT_REASON3"];
        $data["VISIT_REASON3_TEXT"][TEXT]   = $fields["VISIT_REASON3_TEXT"];
        $data["OCCUR_SITUATION"][TEXT]      = $fields["OCCUR_SITUATION"];
        $data["OCCUR_CAUSE"][TEXT]          = $fields["OCCUR_CAUSE"];
        $data["OCCUR_DATE"][TEXT]           = str_replace("/", "-", $fields["OCCUR_DATE"]);
        $data["OCCUR_HOUR"][TEXT]           = ($fields["OCCUR_HOUR"]) ? sprintf("%02d", $fields["OCCUR_HOUR"]) : "";
        $data["OCCUR_MINUTE"][TEXT]         = ($fields["OCCUR_MINUTE"]) ? sprintf("%02d", $fields["OCCUR_MINUTE"]) : "";
        $data["OCCUR_ACT"][TEXT]            = $fields["OCCUR_ACT"];
        $data["OCCUR_ACT_DETAIL"][TEXT]     = $fields["OCCUR_ACT_DETAIL"];
        $data["OCCUR_PLACE"][TEXT]          = $fields["OCCUR_PLACE"];
        $data["INJURY_PART1"][TEXT]         = $fields["INJURY_PART1"];
        $data["INJURY_PART1_TEXT"][TEXT]    = $fields["INJURY_PART1_TEXT"];
        $data["INJURY_PART2"][TEXT]         = $fields["INJURY_PART2"];
        $data["INJURY_PART2_TEXT"][TEXT]    = $fields["INJURY_PART2_TEXT"];
        $data["INJURY_PART3"][TEXT]         = $fields["INJURY_PART3"];
        $data["INJURY_PART3_TEXT"][TEXT]    = $fields["INJURY_PART3_TEXT"];
        $data["TREATMENT1"][TEXT]           = $fields["TREATMENT1"];
        $data["TREATMENT1_TEXT"][TEXT]      = $fields["TREATMENT1_TEXT"];
        $data["TREATMENT2"][TEXT]           = $fields["TREATMENT2"];
        $data["TREATMENT2_TEXT"][TEXT]      = $fields["TREATMENT2_TEXT"];
        $data["TREATMENT3"][TEXT]           = $fields["TREATMENT3"];
        $data["TREATMENT3_TEXT"][TEXT]      = $fields["TREATMENT3_TEXT"];
        $data["TEMPERATURE1"][TEXT]         = $fields["TEMPERATURE1"];
        $data["MEASURE_HOUR1"][TEXT]        = ($fields["MEASURE_HOUR1"]) ? sprintf("%02d", $fields["MEASURE_HOUR1"]) : "";
        $data["MEASURE_MINUTE1"][TEXT]      = ($fields["MEASURE_MINUTE1"]) ? sprintf("%02d", $fields["MEASURE_MINUTE1"]) : "";
        $data["TEMPERATURE2"][TEXT]         = $fields["TEMPERATURE2"];
        $data["MEASURE_HOUR2"][TEXT]        = ($fields["MEASURE_HOUR2"]) ? sprintf("%02d", $fields["MEASURE_HOUR2"]) : "";
        $data["MEASURE_MINUTE2"][TEXT]      = ($fields["MEASURE_MINUTE2"]) ? sprintf("%02d", $fields["MEASURE_MINUTE2"]) : "";
        $data["TEMPERATURE3"][TEXT]         = $fields["TEMPERATURE3"];
        $data["MEASURE_HOUR3"][TEXT]        = ($fields["MEASURE_HOUR3"]) ? sprintf("%02d", $fields["MEASURE_HOUR3"]) : "";
        $data["MEASURE_MINUTE3"][TEXT]      = ($fields["MEASURE_MINUTE3"]) ? sprintf("%02d", $fields["MEASURE_MINUTE3"]) : "";
        $data["RESTTIME"][TEXT]             = $fields["RESTTIME"];
        $data["LEAVE_HOUR"][TEXT]           = ($fields["LEAVE_HOUR"]) ? sprintf("%02d", $fields["LEAVE_HOUR"]) : "";
        $data["LEAVE_MINUTE"][TEXT]         = ($fields["LEAVE_MINUTE"]) ? sprintf("%02d", $fields["LEAVE_MINUTE"]) : "";
        $data["LEAVE_PERIODCD"][TEXT]       = $fields["LEAVE_PERIODCD"];
        $data["RESULT_REST"][TEXT]          = $fields["RESULT_REST"];
        $data["RESULT_EARLY"][TEXT]         = $fields["RESULT_EARLY"];
        $data["RESULT_MEDICAL"][TEXT]       = $fields["RESULT_MEDICAL"];
        $data["CONTACT"][TEXT]              = $fields["CONTACT"];
        $data["CONTACT2"][TEXT]             = $fields["CONTACT2"];
        $data["CONTACT3"][TEXT]             = $fields["CONTACT3"];
        $data["HOSPITAL"][TEXT]             = $fields["HOSPITAL"];
        $data["COMPANION"][TEXT]            = $fields["COMPANION"];
        $data["COMPANION_DIV"][TEXT]        = $fields["COMPANION_DIV"];
        $data["DIAGNOSIS"][TEXT]            = $fields["DIAGNOSIS"];
        $data["SPECIAL_NOTE"][TEXT]         = $fields["SPECIAL_NOTE"];
        $data["CONSULTATION_NAME"][TEXT]    = $fields["CONSULTATION_NAME"];
        $data["RELATIONSHIP"][TEXT]         = $fields["RELATIONSHIP"];
        $data["CONSULTATION_METHOD"][TEXT]  = $fields["CONSULTATION_METHOD"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        if ($flg == "insert") {
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

        if ($model->schoolName != "miyagiken") {
            $data2 = array();
            $data2["SCHREGNO"][TEXT]             = $model->schregno;
            $data2["VISIT_DATE"][DATE]           = $visit_date;
            $data2["VISIT_HOUR"][TEXT]           = $fields["VISIT_HOUR"];
            $data2["VISIT_MINUTE"][TEXT]         = $fields["VISIT_MINUTE"];
            $data2["TYPE"][TEXT]                 = $model->type;
            $data2["SEQ"][TEXT]                  = '10';
            $data2["REMARK1"][TEXT]              = $fields["OCCUR_PLACE_TEXT2"];
            $data2["REMARK2"][TEXT]              = $fields["RESTTIME_TEXT"];
            $data2["REMARK3"][TEXT]              = $fields["OCCUR_CAUSE_TEXT"];
            $data2["REMARK4"][TEXT]              = $fields["RESULT_RETCLS"];
            $data2["REMARK5"][TEXT]              = $fields["RELATIONSHIP_TEXT"];
            $data2["REGISTERCD"][TEXT]           = STAFFCD;
            $data2["UPDATED"][NUMBER]            = "sysdate()";
            $query = knjf150Query::chkNOffVRecDetDat($model, $fields);
            $cnt2 = $db->getOne($query);
            if ($cnt2 == 0) {
                $db->query(Query::insertSQL($data2, "NURSEOFF_VISITREC_DETAIL_DAT"));
            } else {
                $where  = " WHERE ";
                $where .= "     SCHREGNO     = '".$model->schregno."' AND ";
                $where .= "     VISIT_DATE   = '".$visit_date."' AND ";
                $where .= "     VISIT_HOUR   = '".$fields["VISIT_HOUR"]."' AND ";
                $where .= "     VISIT_MINUTE = '".$fields["VISIT_MINUTE"]."' AND ";
                $where .= "     TYPE         = '".$model->type."' AND ";
                $where .= "     SEQ          = '10' ";
                $db->query(Query::updateSQL($data2, "NURSEOFF_VISITREC_DETAIL_DAT", $where));
            }
        }
        Query::dbCheckIn($db);
        return;
    }

    public function chkNOffVRecDetDat($model, $fields)
    {
        $visit_date = str_replace("/", "-", $fields["VISIT_DATE"]);
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     NURSEOFF_VISITREC_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO     = '".$model->schregno."' AND ";
        $query .= "     VISIT_DATE   = '".$visit_date."' AND ";
        $query .= "     VISIT_HOUR   = '".$fields["VISIT_HOUR"]."' AND ";
        $query .= "     VISIT_MINUTE = '".$fields["VISIT_MINUTE"]."' AND ";
        $query .= "     TYPE         = '".$model->type."' AND ";
        $query .= "     SEQ          = '10' ";

        return $query;
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
        $query .= "     REMARK1 ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "    , SCHOOLCD ";
        }
        $query .= " FROM ";
        $query .= "     SETTING_DAT ";
        $query .= " WHERE ";
        $query .= "     SEQ = '001' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
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
        }
        return $query;
    }
}
