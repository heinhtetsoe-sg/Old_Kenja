<?php

require_once('for_php7.php');

class knjl410query extends Query
{

    //年度一覧取得
    public function selectYearQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     YEAR AS LABEL, ";
        $query .= "     YEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "     RECRUIT_DAT ";
        $query .= " WHERE ";
        $query .= "     RECRUIT_NO = '{$model->recruitNo}' ";
        $query .= "ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //Z010のNAME1を取得
    public function getZ010()
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".(CTRL_YEAR + 1)."' ";
        $query .= "     AND NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }

    //校種
    public function getA023()
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".(CTRL_YEAR + 1)."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        $query .= "     AND NAME1 IN ('J', 'H') ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //学年取得
    public function getGrade($schoolKind)
    {
        $query  = " WITH GRADE_T (LABEL, VALUE) AS ( ";
        if ($schoolKind == "J") {
            $query .= "     VALUES('小１', '01') ";
            $query .= "     UNION ";
            $query .= "     VALUES('小２', '02') ";
            $query .= "     UNION ";
            $query .= "     VALUES('小３', '03') ";
            $query .= "     UNION ";
            $query .= "     VALUES('小４', '04') ";
            $query .= "     UNION ";
            $query .= "     VALUES('小５', '05') ";
            $query .= "     UNION ";
            $query .= "     VALUES('小６', '06') ";
        } else {
            $query .= "     VALUES('中１', '07') ";
            $query .= "     UNION ";
            $query .= "     VALUES('中２', '08') ";
            $query .= "     UNION ";
            $query .= "     VALUES('中３', '09') ";
            $query .= "     UNION ";
            $query .= "     VALUES('高１', '10') ";
            $query .= "     UNION ";
            $query .= "     VALUES('高２', '11') ";
            $query .= "     UNION ";
            $query .= "     VALUES('高３', '12') ";
        }
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     GRADE_T ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //新規番号
    public function getSinkiNo($db)
    {
        $query = " SELECT MAX(RECRUIT_NO) FROM RECRUIT_NO_DAT ";
        $maxRecruitNo = $db->getOne($query);

        if ($maxRecruitNo == '') {
            $query  = " SELECT ";
            $query .= "     VALUE(MAX(RECRUIT_NO), '".(CTRL_YEAR + 1)."0000') AS RECRUIT_NO ";
            $query .= " FROM ";
            $query .= "     RECRUIT_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".(CTRL_YEAR + 1)."' ";
            $maxRecruitNo = $db->getOne($query);
        }

        return $maxRecruitNo;
    }

    //募集データ
    public function getRecruitDat($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     RECRUIT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->recruitYear}' ";
        $query .= "     AND RECRUIT_NO = '{$model->recruitNo}' ";

        return $query;
    }

    //名称マスタ
    public function getNameMst($model, $namecd1)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '{$namecd1}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学校名
    public function getFinSchoolName($finSchoolCd)
    {
        $query  = " SELECT ";
        $query .= "     VALUE(L1.NAME1,'') || T1.FINSCHOOL_NAME AS FINSCHOOL_NAME ";
        $query .= " FROM ";
        $query .= "     FINSCHOOL_MST T1 ";
        $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'L001' ";
        $query .= "                          AND L1.NAMECD2 = T1.FINSCHOOL_DISTCD ";
        $query .= " WHERE ";
        $query .= "     T1.FINSCHOOLCD = '{$finSchoolCd}' ";

        return $query;
    }

    //塾名
    public function getPriSchoolName($priSchoolCd)
    {
        $query  = " SELECT ";
        $query .= "     PRISCHOOL_NAME ";
        $query .= " FROM ";
        $query .= "     PRISCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     PRISCHOOLCD = '{$priSchoolCd}' ";

        return $query;
    }
    //教室名
    public function getPriSchoolClassName($priSchoolCd, $priSchoolClassCd)
    {
        $query  = " SELECT ";
        $query .= "     PRISCHOOL_NAME ";
        $query .= " FROM ";
        $query .= "     PRISCHOOL_CLASS_MST ";
        $query .= " WHERE ";
        $query .= "     PRISCHOOLCD = '{$priSchoolCd}' ";
        $query .= "     AND PRISCHOOL_CLASS_CD = '{$priSchoolClassCd}' ";

        return $query;
    }
    //希望取得
    public function getHopeData($model)
    {
        $query .= " WITH MAX_DATE_T AS ( ";
        $query .= " SELECT ";
        $query .= "     MAX(TOUROKU_DATE) AS MAX_DATE ";
        $query .= " FROM ";
        $query .= "     RECRUIT_EVENT_DAT ";
        $query .= " WHERE ";
        $query .= "     RECRUIT_NO = '{$model->recruitNo}' ";
        $query .= " ), MAX_EVENT_T AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.TOUROKU_DATE AS MAX_DATE, ";
        $query .= "     MAX(EVENT_CD) AS MAX_EVENT ";
        $query .= " FROM ";
        $query .= "     RECRUIT_EVENT_DAT T1, ";
        $query .= "     MAX_DATE_T T2 ";
        $query .= " WHERE ";
        $query .= "     RECRUIT_NO = '{$model->recruitNo}' ";
        $query .= "     AND T1.TOUROKU_DATE = T2.MAX_DATE ";
        $query .= " GROUP BY ";
        $query .= "     T1.TOUROKU_DATE ";
        $query .= " ), MAX_MEDIA_T AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.TOUROKU_DATE AS MAX_DATE, ";
        $query .= "     T1.EVENT_CD AS MAX_EVENT, ";
        $query .= "     MAX(MEDIA_CD) AS MAX_MEDIA ";
        $query .= " FROM ";
        $query .= "     RECRUIT_EVENT_DAT T1, ";
        $query .= "     MAX_EVENT_T T2 ";
        $query .= " WHERE ";
        $query .= "     RECRUIT_NO = '{$model->recruitNo}' ";
        $query .= "     AND T1.TOUROKU_DATE = T2.MAX_DATE ";
        $query .= "     AND T1.EVENT_CD = T2.MAX_EVENT ";
        $query .= " GROUP BY ";
        $query .= "     T1.TOUROKU_DATE, ";
        $query .= "     T1.EVENT_CD ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.RECRUIT_NO ";
        for ($i = 1; $i <= 5; $i++) {
            $query .= "     ,T1.HOPE_COURSECD{$i} ";
            $query .= "     ,T1.HOPE_MAJORCD{$i} ";
            $query .= "     ,MAJOR{$i}.COURSENAME AS COURSENAME{$i} ";
            $query .= "     ,MAJOR{$i}.MAJORNAME AS MAJORNAME{$i} ";
            $query .= "     ,T1.HOPE_COURSECODE{$i} ";
            $query .= "     ,COURSE{$i}.COURSECODENAME AS COURSECODENAME{$i} ";
        }
        $query .= " FROM ";
        $query .= "     RECRUIT_EVENT_DAT T1 ";
        for ($i = 1; $i <= 5; $i++) {
            $query .= "     LEFT JOIN V_COURSE_MAJOR_MST MAJOR{$i} ON MAJOR{$i}.YEAR = T1.YEAR ";
            $query .= "          AND MAJOR{$i}.COURSECD = T1.HOPE_COURSECD{$i} ";
            $query .= "          AND MAJOR{$i}.MAJORCD = T1.HOPE_MAJORCD{$i} ";
            $query .= "     LEFT JOIN V_COURSECODE_MST COURSE{$i} ON COURSE{$i}.YEAR = T1.YEAR ";
            $query .= "          AND COURSE{$i}.COURSECODE = T1.HOPE_COURSECODE{$i} ";
        }
        $query .= "     ,MAX_MEDIA_T T2 ";
        $query .= " WHERE ";
        $query .= "     T1.RECRUIT_NO = '{$model->recruitNo}' ";
        $query .= "     AND T1.TOUROKU_DATE = T2.MAX_DATE ";
        $query .= "     AND T1.EVENT_CD = T2.MAX_EVENT ";
        $query .= "     AND T1.MEDIA_CD = T2.MAX_MEDIA ";

        return $query;
    }

    //イベント取得
    public function getEventInfoData($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.TOUROKU_DATE, ";
        $query .= "     T1.EVENT_CD, ";
        $query .= "     EMST.EVENT_NAME, ";
        $query .= "     T1.MEDIA_CD, ";
        $query .= "     L1.NAME1 AS MEDIA_NAME, ";
        $query .= "     T1.STATE_CD, ";
        $query .= "     L2.NAME1 AS STATE_NAME, ";
        $query .= "     T1.REMARK ";
        $query .= " FROM ";
        $query .= "     RECRUIT_EVENT_DAT T1 ";
        $query .= "     LEFT JOIN RECRUIT_EVENT_YMST EMST ON EMST.YEAR = T1.YEAR ";
        $query .= "          AND EMST.EVENT_CLASS_CD = T1.EVENT_CLASS_CD ";
        $query .= "          AND EMST.EVENT_CD = T1.EVENT_CD ";
        $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'L401' ";
        $query .= "          AND L1.NAMECD2 = T1.MEDIA_CD ";
        $query .= "     LEFT JOIN NAME_MST L2 ON L2.NAMECD1 = 'L402' ";
        $query .= "          AND L2.NAMECD2 = T1.STATE_CD ";
        $query .= " WHERE ";
        $query .= "     T1.RECRUIT_NO = '{$model->recruitNo}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.TOUROKU_DATE DESC, ";
        $query .= "     T1.EVENT_CD, ";
        $query .= "     T1.MEDIA_CD ";

        return $query;
    }

    //発送物一覧取得
    public function getSendInfoData($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.RECRUIT_NO, ";
        $query .= "     T1.EVENT_CLASS_CD, ";
        $query .= "     T1.EVENT_CD, ";
        $query .= "     T1.SEND_CD, ";
        $query .= "     T1.SEND_COUNT, ";
        $query .= "     SMST.SEND_NAME, ";
        $query .= "     T1.SEND_COUNT, ";
        $query .= "     L1.NAME1 AS METHODNAME, ";
        $query .= "     SEND_HIST.SEND_DATE ";
        $query .= " FROM ";
        $query .= "     RECRUIT_SEND_HIST_STD_DAT T1 ";
        $query .= "     LEFT JOIN RECRUIT_SEND_HIST_DAT SEND_HIST ON SEND_HIST.YEAR = T1.YEAR ";
        $query .= "          AND SEND_HIST.EVENT_CLASS_CD = T1.EVENT_CLASS_CD ";
        $query .= "          AND SEND_HIST.EVENT_CD = T1.EVENT_CD ";
        $query .= "          AND SEND_HIST.SEND_CD = T1.SEND_CD ";
        $query .= "          AND SEND_HIST.SEND_COUNT = T1.SEND_COUNT ";
        $query .= "     LEFT JOIN RECRUIT_SEND_YMST SMST ON SMST.YEAR = T1.YEAR ";
        $query .= "          AND SMST.EVENT_CLASS_CD = T1.EVENT_CLASS_CD ";
        $query .= "          AND SMST.EVENT_CD = T1.EVENT_CD ";
        $query .= "          AND SMST.SEND_CD = T1.SEND_CD ";
        $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'L403' ";
        $query .= "          AND L1.NAMECD2 = SEND_HIST.SEND_METHOD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".(CTRL_YEAR + 1)."' ";
        $query .= "     AND T1.RECRUIT_NO = '{$model->recruitNo}' ";
        $query .= " ORDER BY ";
        $query .= "     SEND_HIST.SEND_DATE, ";
        $query .= "     T1.SEND_CD ";

        return $query;
    }

    //募集データ有無
    public function getRecruitDatCnt($model, $year)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     RECRUIT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND RECRUIT_NO = '{$model->field["RECRUIT_NO"]}' ";

        return $query;
    }

    //RECRUIT_NOのMAX更新
    public function insRecruitMaxRecuitNoDat($db, $recruitNo)
    {
        $query = " SELECT RECRUIT_NO FROM RECRUIT_NO_DAT ";
        $tabRecruitNo = $db->getOne($query);
        if ($tabRecruitNo != '') {
            $query = " UPDATE RECRUIT_NO_DAT SET (RECRUIT_NO, REGISTERCD, UPDATED) = ('".$recruitNo."', '".STAFFCD."', current timestamp)";
        } else {
            $query = " INSERT INTO RECRUIT_NO_DAT VALUES('".$recruitNo."', '".STAFFCD."', current timestamp)";
        }
        $db->query($query);
    }

    /**
     * 削除 RECRUIT_DAT
     */
    public function &delRecruitDat($model, $table)
    {

        $query  = "DELETE FROM ";
        $query .= "    ".$table." ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".$model->recruitYear."' ";
        $query .= "    AND RECRUIT_NO = '".$model->field["RECRUIT_NO"]."' ";

        return $query;
    }

    /**
     * 新規 RECRUIT_DAT
     */
    public function &insRecruitDat($model)
    {

        $data["YEAR"][TEXT]                 = $model->recruitYear;
        $data["RECRUIT_NO"][TEXT]           = $model->field["RECRUIT_NO"];
        $data["NAME"][TEXT]                 = $model->field["NAME"];
        $data["NAME_KANA"][TEXT]            = $model->field["NAME_KANA"];
        $data["SCHOOL_KIND"][TEXT]          = $model->schoolKindHenkan[$model->field["SCHOOL_KIND"]];
        $data["SEX"][TEXT]                  = $model->field["SEX"];
        $data["BIRTHDAY"][DATE]             = $model->field["BIRTHDAY"];
        $data["FINSCHOOLCD"][TEXT]          = $model->field["FINSCHOOLCD"];
        $data["GRADE"][TEXT]                = $model->field["GRADE"];
        $data["SCHOOL_TEACHER"][TEXT]       = $model->field["SCHOOL_TEACHER"];
        $data["PRISCHOOLCD"][TEXT]          = $model->field["PRISCHOOLCD"];
        $data["PRISCHOOL_CLASS_CD"][TEXT]   = $model->field["PRISCHOOL_CLASS_CD"];
        $data["PRISCHOOL_TEACHER"][TEXT]    = $model->field["PRISCHOOL_TEACHER"];
        $data["GUARD_NAME"][TEXT]           = $model->field["GUARD_NAME"];
        $data["GUARD_KANA"][TEXT]           = $model->field["GUARD_KANA"];
        $data["ZIPCD"][TEXT]                = $model->field["ZIPCD"];
        $data["ADDR1"][TEXT]                = $model->field["ADDR1"];
        $data["ADDR2"][TEXT]                = $model->field["ADDR2"];
        $data["TELNO"][TEXT]                = $model->field["TELNO"];
        $data["TELNO2"][TEXT]               = $model->field["TELNO2"];
        $data["FAXNO"][TEXT]                = $model->field["FAXNO"];
        $data["EMAIL"][TEXT]                = $model->field["EMAIL"];
        $data["REMARK"][TEXT]               = $model->field["REMARK"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "RECRUIT_DAT");

        return $query;
    }

    /**
     * 削除 RECRUIT_DAT
     */
    public function &delSendHistStdDat($model, $key)
    {

        $query  = " DELETE FROM ";
        $query .= "    RECRUIT_SEND_HIST_STD_DAT ";
        $query .= " WHERE ";
        $query .= "    YEAR || '-' || EVENT_CLASS_CD || '-' || EVENT_CD || '-' || SEND_CD || '-' || SEND_COUNT || '-' || RECRUIT_NO = '{$key}' ";
        return $query;
    }
}
?>
