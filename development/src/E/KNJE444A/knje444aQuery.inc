<?php

require_once('for_php7.php');

class knje444aQuery extends Query
{
    //教育委員会チェック
    public function getNameMst()
    {
        $query  = " SELECT ";
        $query .= "     ABBV1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= " AND NAMECD2 = '00' ";

        return $query;
    }

    //出力対象年度
    public function getYear()
    {
        $query  = " SELECT ";
        $query .= "     '".CTRL_YEAR."' AS VALUE, ";
        $query .= "     '".CTRL_YEAR."' || '年度' AS LABEL ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSDUMMY1 ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     YEAR AS VALUE, ";
        $query .= "     YEAR || '年度' AS LABEL ";
        $query .= " FROM ";
        $query .= "     AFT_STAT_COURSE_STAT_SS_DAT ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //学校一覧
    public function getSchoolList($model)
    {
        $query  = " WITH DATA_CNT AS ( ";
        $query .= "     SELECT ";
        $query .= "         EDBOARD_SCHOOLCD, ";
        $query .= "         COUNT(*) AS DATA_CNT ";
        $query .= "     FROM ";
        $query .= "         AFT_STAT_COURSE_STAT_SS_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$model->year}' ";
        $query .= "     GROUP BY ";
        $query .= "         EDBOARD_SCHOOLCD ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.EDBOARD_SCHOOLCD AS VALUE, ";
        $query .= "     T1.EDBOARD_SCHOOLNAME AS LABEL, ";
        $query .= "     VALUE(L1.DATA_CNT, 0) AS DATA_CNT ";
        $query .= " FROM ";
        $query .= "     EDBOARD_SCHOOL_MST T1 ";
        $query .= "     LEFT JOIN DATA_CNT L1 ON L1.EDBOARD_SCHOOLCD = T1.EDBOARD_SCHOOLCD ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //CSV出力
    public function getCsvQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     EDBOARD_SCHOOLCD, ";
        $query .= "     SCHOOLNAME, ";
        $query .= "     YEAR, ";
        $query .= "     TOROKU_DATE, ";
        $query .= "     SEQ, ";
        $query .= "     SCHREGNO, ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD, ";
        $query .= "     MAJORNAME, ";
        $query .= "     GRADE, ";
        $query .= "     HR_CLASS, ";
        $query .= "     HR_CLASS_NAME, ";
        $query .= "     ATTENDNO, ";
        $query .= "     NAME, ";
        $query .= "     NAME_KANA, ";
        $query .= "     BIRTHDAY, ";
        $query .= "     SEX, ";
        $query .= "     FINSCHOOLCD, ";
        $query .= "     FINSCHOOLNAME, ";
        $query .= "     PREF_CD, ";
        $query .= "     PREF_NAME, ";
        $query .= "     SENKOU_KIND, ";
        $query .= "     SENKOU_NAME, ";
        $query .= "     STAT_CD, ";
        $query .= "     STAT_NAME, ";
        $query .= "     SCHOOL_GROUP, ";
        $query .= "     SCHOOL_GROUP_NAME, ";
        $query .= "     FACULTYCD, ";
        $query .= "     FACULTYNAME, ";
        $query .= "     DEPARTMENTCD, ";
        $query .= "     DEPARTMENTNAME, ";
        $query .= "     JOBTYPE_LCD, ";
        $query .= "     JOBTYPE_LNAME, ";
        $query .= "     JOBTYPE_MCD, ";
        $query .= "     JOBTYPE_MNAME, ";
        $query .= "     JOBTYPE_SCD, ";
        $query .= "     JOBTYPE_SSCD, ";
        $query .= "     JOBTYPE_SNAME, ";
        $query .= "     AFT_GRAD_PREF_CD, ";
        $query .= "     AFT_GRAD_PREF_NAME, ";
        $query .= "     HOWTOEXAM, ";
        $query .= "     HOWTOEXAM_NAME, ";
        $query .= "     DECISION, ";
        $query .= "     DECISION_NAME, ";
        $query .= "     PLANSTAT, ";
        $query .= "     PLANSTAT_NAME, ";
        $query .= "     AVG1, ";
        $query .= "     AVG2, ";
        $query .= "     AVG3, ";
        $query .= "     AVG4, ";
        $query .= "     SUBCLASSCD_1, ";
        $query .= "     SUBCLASSNAME_1, ";
        $query .= "     SUBCLASS1_VALUE1, ";
        $query .= "     SUBCLASS1_VALUE2, ";
        $query .= "     SUBCLASS1_VALUE3, ";
        $query .= "     SUBCLASS1_VALUE4, ";
        $query .= "     SUBCLASSCD_2, ";
        $query .= "     SUBCLASSNAME_2, ";
        $query .= "     SUBCLASS2_VALUE1, ";
        $query .= "     SUBCLASS2_VALUE2, ";
        $query .= "     SUBCLASS2_VALUE3, ";
        $query .= "     SUBCLASS2_VALUE4, ";
        $query .= "     SUBCLASSCD_3, ";
        $query .= "     SUBCLASSNAME_3, ";
        $query .= "     SUBCLASS3_VALUE1, ";
        $query .= "     SUBCLASS3_VALUE2, ";
        $query .= "     SUBCLASS3_VALUE3, ";
        $query .= "     SUBCLASS3_VALUE4, ";
        $query .= "     SUBCLASSCD_4, ";
        $query .= "     SUBCLASSNAME_4, ";
        $query .= "     SUBCLASS4_VALUE1, ";
        $query .= "     SUBCLASS4_VALUE2, ";
        $query .= "     SUBCLASS4_VALUE3, ";
        $query .= "     SUBCLASS4_VALUE4, ";
        $query .= "     SUBCLASSCD_5, ";
        $query .= "     SUBCLASSNAME_5, ";
        $query .= "     SUBCLASS5_VALUE1, ";
        $query .= "     SUBCLASS5_VALUE2, ";
        $query .= "     SUBCLASS5_VALUE3, ";
        $query .= "     SUBCLASS5_VALUE4, ";
        $query .= "     SUBCLASSCD_6, ";
        $query .= "     SUBCLASSNAME_6, ";
        $query .= "     SUBCLASS6_VALUE1, ";
        $query .= "     SUBCLASS6_VALUE2, ";
        $query .= "     SUBCLASS6_VALUE3, ";
        $query .= "     SUBCLASS6_VALUE4, ";
        $query .= "     SUBCLASSCD_7, ";
        $query .= "     SUBCLASSNAME_7, ";
        $query .= "     SUBCLASS7_VALUE1, ";
        $query .= "     SUBCLASS7_VALUE2, ";
        $query .= "     SUBCLASS7_VALUE3, ";
        $query .= "     SUBCLASS7_VALUE4, ";
        $query .= "     SUBCLASSCD_8, ";
        $query .= "     SUBCLASSNAME_8, ";
        $query .= "     SUBCLASS8_VALUE1, ";
        $query .= "     SUBCLASS8_VALUE2, ";
        $query .= "     SUBCLASS8_VALUE3, ";
        $query .= "     SUBCLASS8_VALUE4, ";
        $query .= "     SUBCLASSCD_9, ";
        $query .= "     SUBCLASSNAME_9, ";
        $query .= "     SUBCLASS9_VALUE1, ";
        $query .= "     SUBCLASS9_VALUE2, ";
        $query .= "     SUBCLASS9_VALUE3, ";
        $query .= "     SUBCLASS9_VALUE4, ";
        $query .= "     SUBCLASSCD_10, ";
        $query .= "     SUBCLASSNAME_10, ";
        $query .= "     SUBCLASS10_VALUE1, ";
        $query .= "     SUBCLASS10_VALUE2, ";
        $query .= "     SUBCLASS10_VALUE3, ";
        $query .= "     SUBCLASS10_VALUE4, ";
        $query .= "     SUBCLASSCD_11, ";
        $query .= "     SUBCLASSNAME_11, ";
        $query .= "     SUBCLASS11_VALUE1, ";
        $query .= "     SUBCLASS11_VALUE2, ";
        $query .= "     SUBCLASS11_VALUE3, ";
        $query .= "     SUBCLASS11_VALUE4, ";
        $query .= "     SUBCLASSCD_12, ";
        $query .= "     SUBCLASSNAME_12, ";
        $query .= "     SUBCLASS12_VALUE1, ";
        $query .= "     SUBCLASS12_VALUE2, ";
        $query .= "     SUBCLASS12_VALUE3, ";
        $query .= "     SUBCLASS12_VALUE4, ";
        $query .= "     SUBCLASSCD_13, ";
        $query .= "     SUBCLASSNAME_13, ";
        $query .= "     SUBCLASS13_VALUE1, ";
        $query .= "     SUBCLASS13_VALUE2, ";
        $query .= "     SUBCLASS13_VALUE3, ";
        $query .= "     SUBCLASS13_VALUE4, ";
        $query .= "     SUBCLASSCD_14, ";
        $query .= "     SUBCLASSNAME_14, ";
        $query .= "     SUBCLASS14_VALUE1, ";
        $query .= "     SUBCLASS14_VALUE2, ";
        $query .= "     SUBCLASS14_VALUE3, ";
        $query .= "     SUBCLASS14_VALUE4, ";
        $query .= "     SUBCLASSCD_15, ";
        $query .= "     SUBCLASSNAME_15, ";
        $query .= "     SUBCLASS15_VALUE1, ";
        $query .= "     SUBCLASS15_VALUE2, ";
        $query .= "     SUBCLASS15_VALUE3, ";
        $query .= "     SUBCLASS15_VALUE4, ";
        $query .= "     SUBCLASSCD_16, ";
        $query .= "     SUBCLASSNAME_16, ";
        $query .= "     SUBCLASS16_VALUE1, ";
        $query .= "     SUBCLASS16_VALUE2, ";
        $query .= "     SUBCLASS16_VALUE3, ";
        $query .= "     SUBCLASS16_VALUE4, ";
        $query .= "     SUBCLASSCD_17, ";
        $query .= "     SUBCLASSNAME_17, ";
        $query .= "     SUBCLASS17_VALUE1, ";
        $query .= "     SUBCLASS17_VALUE2, ";
        $query .= "     SUBCLASS17_VALUE3, ";
        $query .= "     SUBCLASS17_VALUE4, ";
        $query .= "     SUBCLASSCD_18, ";
        $query .= "     SUBCLASSNAME_18, ";
        $query .= "     SUBCLASS18_VALUE1, ";
        $query .= "     SUBCLASS18_VALUE2, ";
        $query .= "     SUBCLASS18_VALUE3, ";
        $query .= "     SUBCLASS18_VALUE4, ";
        $query .= "     SUBCLASSCD_19, ";
        $query .= "     SUBCLASSNAME_19, ";
        $query .= "     SUBCLASS19_VALUE1, ";
        $query .= "     SUBCLASS19_VALUE2, ";
        $query .= "     SUBCLASS19_VALUE3, ";
        $query .= "     SUBCLASS19_VALUE4, ";
        $query .= "     SUBCLASSCD_20, ";
        $query .= "     SUBCLASSNAME_20, ";
        $query .= "     SUBCLASS20_VALUE1, ";
        $query .= "     SUBCLASS20_VALUE2, ";
        $query .= "     SUBCLASS20_VALUE3, ";
        $query .= "     SUBCLASS20_VALUE4, ";
        $query .= "     SUBCLASSCD_21, ";
        $query .= "     SUBCLASSNAME_21, ";
        $query .= "     SUBCLASS21_VALUE1, ";
        $query .= "     SUBCLASS21_VALUE2, ";
        $query .= "     SUBCLASS21_VALUE3, ";
        $query .= "     SUBCLASS21_VALUE4, ";
        $query .= "     SUBCLASSCD_22, ";
        $query .= "     SUBCLASSNAME_22, ";
        $query .= "     SUBCLASS22_VALUE1, ";
        $query .= "     SUBCLASS22_VALUE2, ";
        $query .= "     SUBCLASS22_VALUE3, ";
        $query .= "     SUBCLASS22_VALUE4, ";
        $query .= "     SUBCLASSCD_23, ";
        $query .= "     SUBCLASSNAME_23, ";
        $query .= "     SUBCLASS23_VALUE1, ";
        $query .= "     SUBCLASS23_VALUE2, ";
        $query .= "     SUBCLASS23_VALUE3, ";
        $query .= "     SUBCLASS23_VALUE4, ";
        $query .= "     SUBCLASSCD_24, ";
        $query .= "     SUBCLASSNAME_24, ";
        $query .= "     SUBCLASS24_VALUE1, ";
        $query .= "     SUBCLASS24_VALUE2, ";
        $query .= "     SUBCLASS24_VALUE3, ";
        $query .= "     SUBCLASS24_VALUE4, ";
        $query .= "     SUBCLASSCD_25, ";
        $query .= "     SUBCLASSNAME_25, ";
        $query .= "     SUBCLASS25_VALUE1, ";
        $query .= "     SUBCLASS25_VALUE2, ";
        $query .= "     SUBCLASS25_VALUE3, ";
        $query .= "     SUBCLASS25_VALUE4, ";
        $query .= "     SUBCLASSCD_26, ";
        $query .= "     SUBCLASSNAME_26, ";
        $query .= "     SUBCLASS26_VALUE1, ";
        $query .= "     SUBCLASS26_VALUE2, ";
        $query .= "     SUBCLASS26_VALUE3, ";
        $query .= "     SUBCLASS26_VALUE4, ";
        $query .= "     SUBCLASSCD_27, ";
        $query .= "     SUBCLASSNAME_27, ";
        $query .= "     SUBCLASS27_VALUE1, ";
        $query .= "     SUBCLASS27_VALUE2, ";
        $query .= "     SUBCLASS27_VALUE3, ";
        $query .= "     SUBCLASS27_VALUE4, ";
        $query .= "     SUBCLASSCD_28, ";
        $query .= "     SUBCLASSNAME_28, ";
        $query .= "     SUBCLASS28_VALUE1, ";
        $query .= "     SUBCLASS28_VALUE2, ";
        $query .= "     SUBCLASS28_VALUE3, ";
        $query .= "     SUBCLASS28_VALUE4, ";
        $query .= "     SUBCLASSCD_29, ";
        $query .= "     SUBCLASSNAME_29, ";
        $query .= "     SUBCLASS29_VALUE1, ";
        $query .= "     SUBCLASS29_VALUE2, ";
        $query .= "     SUBCLASS29_VALUE3, ";
        $query .= "     SUBCLASS29_VALUE4, ";
        $query .= "     SUBCLASSCD_30, ";
        $query .= "     SUBCLASSNAME_30, ";
        $query .= "     SUBCLASS30_VALUE1, ";
        $query .= "     SUBCLASS30_VALUE2, ";
        $query .= "     SUBCLASS30_VALUE3, ";
        $query .= "     SUBCLASS30_VALUE4, ";
        $query .= "     SUBCLASSCD_31, ";
        $query .= "     SUBCLASSNAME_31, ";
        $query .= "     SUBCLASS31_VALUE1, ";
        $query .= "     SUBCLASS31_VALUE2, ";
        $query .= "     SUBCLASS31_VALUE3, ";
        $query .= "     SUBCLASS31_VALUE4, ";
        $query .= "     SUBCLASSCD_32, ";
        $query .= "     SUBCLASSNAME_32, ";
        $query .= "     SUBCLASS32_VALUE1, ";
        $query .= "     SUBCLASS32_VALUE2, ";
        $query .= "     SUBCLASS32_VALUE3, ";
        $query .= "     SUBCLASS32_VALUE4, ";
        $query .= "     SUBCLASSCD_33, ";
        $query .= "     SUBCLASSNAME_33, ";
        $query .= "     SUBCLASS33_VALUE1, ";
        $query .= "     SUBCLASS33_VALUE2, ";
        $query .= "     SUBCLASS33_VALUE3, ";
        $query .= "     SUBCLASS33_VALUE4, ";
        $query .= "     SUBCLASSCD_34, ";
        $query .= "     SUBCLASSNAME_34, ";
        $query .= "     SUBCLASS34_VALUE1, ";
        $query .= "     SUBCLASS34_VALUE2, ";
        $query .= "     SUBCLASS34_VALUE3, ";
        $query .= "     SUBCLASS34_VALUE4, ";
        $query .= "     SUBCLASSCD_35, ";
        $query .= "     SUBCLASSNAME_35, ";
        $query .= "     SUBCLASS35_VALUE1, ";
        $query .= "     SUBCLASS35_VALUE2, ";
        $query .= "     SUBCLASS35_VALUE3, ";
        $query .= "     SUBCLASS35_VALUE4, ";
        $query .= "     SUBCLASSCD_36, ";
        $query .= "     SUBCLASSNAME_36, ";
        $query .= "     SUBCLASS36_VALUE1, ";
        $query .= "     SUBCLASS36_VALUE2, ";
        $query .= "     SUBCLASS36_VALUE3, ";
        $query .= "     SUBCLASS36_VALUE4, ";
        $query .= "     SUBCLASSCD_37, ";
        $query .= "     SUBCLASSNAME_37, ";
        $query .= "     SUBCLASS37_VALUE1, ";
        $query .= "     SUBCLASS37_VALUE2, ";
        $query .= "     SUBCLASS37_VALUE3, ";
        $query .= "     SUBCLASS37_VALUE4, ";
        $query .= "     SUBCLASSCD_38, ";
        $query .= "     SUBCLASSNAME_38, ";
        $query .= "     SUBCLASS38_VALUE1, ";
        $query .= "     SUBCLASS38_VALUE2, ";
        $query .= "     SUBCLASS38_VALUE3, ";
        $query .= "     SUBCLASS38_VALUE4, ";
        $query .= "     SUBCLASSCD_39, ";
        $query .= "     SUBCLASSNAME_39, ";
        $query .= "     SUBCLASS39_VALUE1, ";
        $query .= "     SUBCLASS39_VALUE2, ";
        $query .= "     SUBCLASS39_VALUE3, ";
        $query .= "     SUBCLASS39_VALUE4, ";
        $query .= "     SUBCLASSCD_40, ";
        $query .= "     SUBCLASSNAME_40, ";
        $query .= "     SUBCLASS40_VALUE1, ";
        $query .= "     SUBCLASS40_VALUE2, ";
        $query .= "     SUBCLASS40_VALUE3, ";
        $query .= "     SUBCLASS40_VALUE4, ";
        $query .= "     SUBCLASS_OVERFLG ";
        $query .= " FROM ";
        $query .= "     AFT_STAT_COURSE_STAT_SS_DAT ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD IN ('" .str_replace(",", "','", $model->selectdata_l) ."') ";
        $query .= "     AND YEAR = '{$model->year}' ";
        $query .= " ORDER BY ";
        $query .= "     EDBOARD_SCHOOLCD, ";
        $query .= "     GRADE, ";
        $query .= "     HR_CLASS, ";
        $query .= "     ATTENDNO, ";
        $query .= "     SEQ ";

        return $query;
    }

    //選択フィールド取得
    public function getFieldSql($model, $dataDiv)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     KNJE444A_KAKIDASHI_LIST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND DATA_DIV = '{$dataDiv}' ";
        $query .= " ORDER BY ";
        $query .= "     SEQ ";

        return $query;
    }

    //選択フィールド削除
    public function getDelFieldSql($model, $dataDiv)
    {
        $query  = " DELETE FROM ";
        $query .= "     KNJE444A_KAKIDASHI_LIST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND DATA_DIV = '{$dataDiv}' ";

        return $query;
    }

    //選択フィールド保存
    public function getInsFieldSql($model, $dataDiv, $setSeq, $field)
    {
        $data = array();
        $data["YEAR"][TEXT]            = $model->year;
        $data["DATA_DIV"][TEXT]        = $dataDiv;
        $data["SEQ"][NUMBER]           = $setSeq;
        $data["FIELD_NAME"][TEXT]      = $field;
        $data["REGISTERCD"][TEXT]      = STAFFCD;
        $data["UPDATED"][FUNC]         = "sysdate()";

        $query = Query::insertSQL($data, "KNJE444A_KAKIDASHI_LIST");
        return $query;
    }
}
