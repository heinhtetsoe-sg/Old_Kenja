<?php

require_once('for_php7.php');

class knjb103eQuery extends Query
{
    //CSV出力
    public function getCsvList($model)
    {
        $query  = "";
        //（資料記述なし）出力対象者と時間割を結合する参照テーブル（SCH_STF_DAT、CHAIR_STF_DAT）
        $query .= " WITH SCH_DATA AS ( ";
        $query .= "     SELECT ";
        $query .= "         DAYOFWEEK(T1.EXECUTEDATE) AS DAYCD, ";
        $query .= "         T1.EXECUTEDATE, ";
        $query .= "         T1.PERIODCD, ";
        $query .= "         T1.CHAIRCD, ";
        $query .= "         T1.YEAR, ";
        $query .= "         T1.SEMESTER, ";
        $query .= "         T4.CHAIRNAME, ";
        $query .= "         T4.CHAIRABBV, ";
        $query .= "         T5.REMARK2 AS SMALLCLS, ";
        $query .= "         CASE WHEN T2.STAFFCD IS NOT NULL THEN T2.STAFFCD ELSE T3.STAFFCD END AS STAFFCD, ";
        $query .= "         CASE WHEN T6.FACCD IS NOT NULL THEN T6.FACCD ELSE T7.FACCD END AS FACCD, ";
        $query .= "         T8.TRGTGRADE ";
        $query .= "     FROM ";
        $query .= "         SCH_CHR_DAT T1 ";
        $query .= "         LEFT JOIN SCH_STF_DAT T2 ON T2.EXECUTEDATE = T1.EXECUTEDATE AND T2.PERIODCD = T1.PERIODCD AND T2.CHAIRCD = T1.CHAIRCD ";
        $query .= "         LEFT JOIN CHAIR_STF_DAT T3 ON T3.YEAR = T1.YEAR AND T3.SEMESTER = T1.SEMESTER AND T3.CHAIRCD = T1.CHAIRCD ";
        $query .= "         LEFT JOIN CHAIR_DAT T4 ON T4.YEAR = T1.YEAR AND T4.SEMESTER = T1.SEMESTER AND T4.CHAIRCD = T1.CHAIRCD ";
        $query .= "         LEFT JOIN CHAIR_DETAIL_DAT T5 ON T5.YEAR = T1.YEAR AND T5.SEMESTER = T1.SEMESTER AND T5.CHAIRCD = T1.CHAIRCD AND T5.SEQ = '004' ";
        $query .= "         LEFT JOIN SCH_FAC_DAT T6 ON T6.EXECUTEDATE = T1.EXECUTEDATE AND T6.PERIODCD = T1.PERIODCD AND T6.CHAIRCD = T1.CHAIRCD ";
        $query .= "         LEFT JOIN CHAIR_FAC_DAT T7 ON T7.YEAR = T1.YEAR AND T7.SEMESTER = T1.SEMESTER AND T7.CHAIRCD = T1.CHAIRCD ";
        $query .= "         LEFT JOIN CHAIR_CLS_DAT T8 ON T8.YEAR = T1.YEAR AND T8.SEMESTER = T1.SEMESTER AND T8.GROUPCD = T4.GROUPCD AND (T8.CHAIRCD = T4.CHAIRCD OR T8.CHAIRCD = '0000000') ";
        $query .= "     WHERE ";
        $query .= "         T1.EXECUTEDATE BETWEEN '".str_replace("/", "-", $model->field["FROM_DATE"])."' AND '".str_replace("/", "-", $model->field["TO_DATE"])."' ";
        $query .= "         AND T1.PERIODCD BETWEEN '2' AND '8' ";
        $query .= "         AND T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "         AND (T2.STAFFCD IS NOT NULL OR T3.STAFFCD IS NOT NULL) ";
        $query .= " ) ";
        $query .= " , SCH_MAIN AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.YEAR, ";
        $query .= "         T1.STAFFCD, ";
        $query .= "         T1.DAYCD, ";
        $query .= "         T1.EXECUTEDATE, ";
        $query .= "         T1.PERIODCD, ";
        $query .= "         T1.CHAIRCD, ";
        $query .= "         T1.CHAIRNAME, ";
        $query .= "         T1.SMALLCLS, ";
        $query .= "         MIN(T1.FACCD) AS FACCD, ";
        $query .= "         MIN(T1.TRGTGRADE) AS TRGTGRADE ";
        $query .= "     FROM ";
        $query .= "         SCH_DATA T1 ";
        $query .= "         INNER JOIN V_STAFF_MST M1 ON M1.YEAR = T1.YEAR AND M1.STAFFCD = T1.STAFFCD ";
        $query .= "     WHERE ";
        $query .= "         T1.DAYCD BETWEEN '2' AND '6' ";
        $query .= "     GROUP BY ";
        $query .= "         T1.YEAR, ";
        $query .= "         T1.STAFFCD, ";
        $query .= "         T1.DAYCD, ";
        $query .= "         T1.EXECUTEDATE, ";
        $query .= "         T1.PERIODCD, ";
        $query .= "         T1.CHAIRCD, ";
        $query .= "         T1.CHAIRNAME, ";
        $query .= "         T1.SMALLCLS ";
        $query .= " ) ";
        $query .= " , JIKANSUU AS ( ";
        $query .= "     SELECT ";
        $query .= "         STAFFCD, ";
        $query .= "         SUM(CNT) AS KOUZA_TOTAL ";
        $query .= "     FROM ";
        $query .= "         (SELECT ";
        $query .= "             STAFFCD, ";
        $query .= "             DAYCD, ";
        $query .= "             PERIODCD, ";
        $query .= "             COUNT(CHAIRCD) AS CNT ";
        $query .= "         FROM ";
        $query .= "             SCH_MAIN ";
        $query .= "         GROUP BY ";
        $query .= "             STAFFCD, ";
        $query .= "             DAYCD, ";
        $query .= "             PERIODCD) ";
        $query .= "     GROUP BY ";
        $query .= "         STAFFCD ";
        $query .= " ) ";
        $query .= " , REGD_H AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.HR_NAMEABBV, ";
        $query .= "         T1.TR_CD1 AS STAFFCD ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_HDAT T1 ";
        $query .= "         INNER JOIN V_STAFF_MST T2 ON T2.YEAR = T1.YEAR AND T2.STAFFCD = T1.TR_CD1 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "         AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " ) ";
        //クラス担任（正担任1）
        //クラス担任（正担任1）以外
        $query .= " , STAFF_LIST AS ( ";
        $query .= "     SELECT ";
        $query .= "         GRADE, ";
        $query .= "         HR_CLASS, ";
        $query .= "         HR_NAMEABBV, ";
        $query .= "         STAFFCD ";
        $query .= "     FROM ";
        $query .= "         REGD_H ";
        $query .= "     WHERE ";
        $query .= "         STAFFCD IN (SELECT STAFFCD FROM SCH_MAIN GROUP BY STAFFCD) ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT ";
        $query .= "         NULL AS GRADE, ";
        $query .= "         NULL AS HR_CLASS, ";
        $query .= "         NULL AS HR_NAMEABBV, ";
        $query .= "         STAFFCD ";
        $query .= "     FROM ";
        $query .= "         SCH_MAIN ";
        $query .= "     WHERE ";
        $query .= "         STAFFCD NOT IN (SELECT STAFFCD FROM REGD_H GROUP BY STAFFCD) ";
        $query .= "     GROUP BY ";
        $query .= "         STAFFCD ";
        $query .= " ) ";
        //部長（校長）
        $query .= " , PRINCIPAL AS ( ";
        $query .= "     SELECT ";
        $query .= "         STAFFCD ";
        $query .= "     FROM ";
        $query .= "         STAFF_PRINCIPAL_HIST_DAT ";
        $query .= "     WHERE ";
        $query .= "         '".str_replace("/", "-", $model->field["TERGET_DATE"])."' BETWEEN FROM_DATE AND VALUE(TO_DATE, '9999-03-31') ";
        $query .= "     GROUP BY ";
        $query .= "         STAFFCD ";
        $query .= " ) ";
        //メイン
        $query .= " SELECT ";
        $query .= "     T0.GRADE, ";
        $query .= "     T0.HR_CLASS, ";
        $query .= "     CASE WHEN P1.STAFFCD IS NOT NULL THEN '部長' ELSE T0.HR_NAMEABBV END AS HR_NAMEABBV, ";
        $query .= "     T0.STAFFCD, ";
        $query .= "     T2.STAFFNAME, ";
        $query .= "     S3.FIELD1 AS SECTIONCD, ";
        $query .= "     S4.SECTIONABBV, ";
        $query .= "     T1.DAYCD, ";
        $query .= "     T1.EXECUTEDATE, ";
        $query .= "     T1.PERIODCD, ";
        $query .= "     T1.CHAIRCD, ";
        $query .= "     T1.CHAIRNAME, ";
        $query .= "     T1.SMALLCLS, ";
        $query .= "     T1.FACCD, ";
        $query .= "     F1.FACILITYABBV, ";
        $query .= "     smallint(T1.TRGTGRADE) AS TRGTGRADE, ";
        //同一時限に複数講座が存在する場合は講座コード順に表示するために行番号（LINE_NO）を割り振る
        $query .= "     RANK() OVER(PARTITION BY T0.GRADE, T0.HR_CLASS, T1.STAFFCD, T1.DAYCD, T1.PERIODCD ORDER BY T1.CHAIRCD) AS LINE_NO, ";
        $query .= "     J1.KOUZA_TOTAL ";
        $query .= " FROM ";
        $query .= "     STAFF_LIST T0 ";
        $query .= "     INNER JOIN SCH_MAIN T1 ON T1.STAFFCD = T0.STAFFCD ";
        $query .= "     INNER JOIN V_STAFF_MST T2 ON T2.YEAR = T1.YEAR AND T2.STAFFCD = T1.STAFFCD ";
        $query .= "     LEFT JOIN STAFF_DETAIL_MST S3 ON S3.YEAR = T1.YEAR AND S3.STAFFCD = T1.STAFFCD AND S3.STAFF_SEQ = '002' ";
        $query .= "     LEFT JOIN SECTION_MST S4 ON S4.SECTIONCD = S3.FIELD1 ";
        $query .= "     LEFT JOIN JIKANSUU J1 ON J1.STAFFCD = T1.STAFFCD ";
        $query .= "     LEFT JOIN V_FACILITY_MST F1 ON F1.YEAR = T1.YEAR AND F1.FACCD = T1.FACCD ";
        $query .= "     LEFT JOIN PRINCIPAL P1 ON P1.STAFFCD = T1.STAFFCD ";
        //クラス担任（正担任1）はクラス順
        //クラス担任（正担任1）以外は五十音順
        //同一時限に複数講座が存在する場合は講座コード順
        $query .= " ORDER BY ";
        $query .= "     T0.GRADE, ";
        $query .= "     T0.HR_CLASS, ";
        $query .= "     T2.STAFFNAME_KANA, ";
        $query .= "     T0.STAFFCD, ";
        $query .= "     LINE_NO, ";
        $query .= "     T1.DAYCD, ";
        $query .= "     T1.PERIODCD, ";
        $query .= "     T1.CHAIRCD ";

        return $query;
    }

    //西暦・和暦の年度変換
    public function getAutoFormatYear($db, $year)
    {
        //西暦判定
        $query = " SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z012' AND NAMECD2 = '00' ";
        $seireki = $db->getOne($query);
        if ($seireki == "2") {
            return $year + "年";
        } else {
            //和暦
            return common::Calc_Wareki($year, "04", "01");
        }
    }
}
