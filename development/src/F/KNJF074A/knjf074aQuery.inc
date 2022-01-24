<?php

require_once('for_php7.php');

class knjf074aQuery extends Query
{
    //年度取得
    public function getSelectYear($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "   YEAR AS LABEL, ";
        $query .= "   YEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "   MEDEXAM_DET_DAT ";
        $query .= " ORDER BY ";
        $query .= "   YEAR ";
        return $query;
    }

    //学校名称取得
    public function getSchoolName()
    {
        $query .= " SELECT SCHOOLNAME1 FROM SCHOOL_MST ";
        $query .= " WHERE YEAR        = '".CTRL_YEAR."'";
        $query .= "   AND SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "   AND SCHOOL_KIND = '".SCHOOLKIND."'";

        return $query;
    }

    //体格統計用CVS作成用のQUERY
    public function selectBodyCsvQuery()
    {
        //対象者一覧
        $query .= " WITH SCHREG_B_DIST_MST AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T2.SEX, ";
        $query .= "     YEAR('". CTRL_YEAR."-04-01' - DATE(T2.BIRTHDAY)) AS AGE, ";
        $query .= "     1 AS REC_CNT, ";
        $query .= "     T4.HEIGHT, ";
        $query .= "     T4.WEIGHT ";
        $query .= " FROM ";
        $query .= "               SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ";
        $query .= "            ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT T3 ";
        $query .= "            ON T3.YEAR     = T1.YEAR ";
        $query .= "           AND T3.GRADE    = T1.GRADE ";
        $query .= "     LEFT JOIN MEDEXAM_DET_DAT T4 ";
        $query .= "            ON T4.YEAR     = T1.YEAR ";
        $query .= "           AND T4.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "        T1.YEAR = '".CTRL_YEAR."' ";
        //身長か体重のどちらかがあれば対象
        $query .= "    AND (   T4.HEIGHT IS NOT NULL ";
        $query .= "         OR T4.WEIGHT IS NOT NULL) ";
        $query .= "    AND YEAR('". CTRL_YEAR."-04-01' - DATE(T2.BIRTHDAY)) < 18";
        $query .= " ), STD AS ( ";
        //年齢、性別毎の受検者数、身長の総和、体重の総和
        $query .= " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     AGE, ";
        $query .= "     SEX, ";
        $query .= "     NVL(SUM(REC_CNT), 0) AS CNT, ";
        $query .= "     NVL(SUM(HEIGHT),  0) AS TOTAL_HEIGHT, ";
        $query .= "     NVL(SUM(WEIGHT),  0) AS TOTAL_WEIGHT ";
        $query .= " FROM ";
        $query .= "     SCHREG_B_DIST_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " GROUP BY ";
        $query .= "     YEAR, ";
        $query .= "     SEX, ";
        $query .= "     AGE ";
        $query .= " ), MAN AS ( ";
        //男子
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     STD ";
        $query .= " WHERE ";
        $query .= "     SEX = '1' ";
        $query .= " ), WOMAN AS ( ";
        //女子
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     STD ";
        $query .= " WHERE ";
        $query .= "     SEX = '2' ";
        $query .= " ) ";
        //CSVに出力するデータ
        $query .= " SELECT ";
        $query .= "     CASE WHEN MAN.AGE IS NULL ";
        $query .= "          THEN WOMAN.AGE ";
        $query .= "          ELSE MAN.AGE ";
        $query .= "          END AGE, ";
        $query .= "     NVL(MAN.CNT, 0)            AS CNT_MAN, ";
        $query .= "     NVL(MAN.TOTAL_HEIGHT, 0)   AS TOTAL_HEI_MAN, ";
        $query .= "     NVL(MAN.TOTAL_WEIGHT, 0)   AS TOTAL_WEI_MAN, ";
        $query .= "     NVL(WOMAN.CNT, 0)          AS CNT_WOMAN, ";
        $query .= "     NVL(WOMAN.TOTAL_HEIGHT, 0) AS TOTAL_HEI_WOMAN, ";
        $query .= "     NVL(WOMAN.TOTAL_WEIGHT, 0) AS TOTAL_WEI_WOMAN ";
        $query .= " FROM ";
        $query .= "                MAN ";
        $query .= "FULL OUTER JOIN WOMAN";
        $query .= "             ON MAN.AGE = WOMAN.AGE ";

        return $query;
    }

    //疾病統計用CVS作成用のQUERY(SELECT句部分)
    public function createPrintRowShippeiCSV()
    {
        for ($sex = 1; $sex <= 2; $sex++) {
            for ($age = 2; $age <= 17; $age++) {
                $query .= "     ,NVL(MAX(CASE WHEN SEX = '".$sex."' AND AGE = '".$age."' THEN CNT END), 0) ";
                if ($sex == 1) {
                    $query .= "M". $age ." ";
                } else {
                    $query .= "F". $age ." ";
                }
            }
        }

        return $query;
    }

    //疾病統計用CVS作成用のQUERY
    public function selectShippeiCsvQuery($model, $block)
    {
        switch ($block) {
            case 2:
                //対象者一覧
                $query .= " WITH SCHREG_B_DIST_MST AS ( ";
                $query .= " SELECT DISTINCT ";
                $query .= "     T1.YEAR, ";
                $query .= "     T1.SCHREGNO, ";
                $query .= "     T2.SEX, ";
                $query .= "     YEAR('". CTRL_YEAR."-04-01' - DATE(T2.BIRTHDAY)) AS AGE, ";
                $query .= "     1 AS REC_CNT, ";
                $query .= "     T4.NUTRITIONCD, ";
                $query .= "     T4.SPINERIBCD ";
                $query .= " FROM ";
                $query .= "               SCHREG_REGD_DAT T1 ";
                $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ";
                $query .= "            ON T2.SCHREGNO = T1.SCHREGNO ";
                $query .= "     LEFT JOIN SCHREG_REGD_GDAT T3 ";
                $query .= "            ON T3.YEAR     = T1.YEAR ";
                $query .= "           AND T3.GRADE    = T1.GRADE ";
                $query .= "     LEFT JOIN MEDEXAM_DET_DAT T4 ";
                $query .= "            ON T4.YEAR     = T1.YEAR ";
                $query .= "           AND T4.SCHREGNO = T1.SCHREGNO ";
                $query .= " WHERE ";
                $query .= "         T1.YEAR = '".CTRL_YEAR."' ";
                                //栄養状態と脊柱・胸郭・四肢（全般）が未受験でなければ対象
                $query .= "     AND NUTRITIONCD <> '00' ";
                $query .= "     AND NUTRITIONCD IS NOT NULL ";
                $query .= "     AND SPINERIBCD  <> '00' ";
                $query .= "     AND SPINERIBCD  IS NOT NULL ";
                $query .= "     AND YEAR('". CTRL_YEAR."-04-01' - DATE(T2.BIRTHDAY)) < 18";
                $query .= " ), CNT2_1 AS ( ";
                //年齢、性別毎の受検者数
                $query .= " SELECT ";
                $query .= "     YEAR, ";
                $query .= "     AGE, ";
                $query .= "     SEX, ";
                $query .= "     NVL(SUM(REC_CNT), 0) AS CNT ";
                $query .= " FROM ";
                $query .= "     SCHREG_B_DIST_MST ";
                $query .= " GROUP BY ";
                $query .= "     YEAR, ";
                $query .= "     AGE, ";
                $query .= "     SEX ";
                $query .= " ), CNT2_2 AS ( ";
                //栄養状態
                $query .= " SELECT ";
                $query .= "     YEAR, ";
                $query .= "     AGE, ";
                $query .= "     SEX, ";
                $query .= "     NVL(SUM(REC_CNT), 0) AS CNT ";
                $query .= " FROM ";
                $query .= "     SCHREG_B_DIST_MST ";
                $query .= " WHERE ";
                $query .= "     NUTRITIONCD = '02' ";    //「02:要注意」
                $query .= " GROUP BY ";
                $query .= "     YEAR, ";
                $query .= "     AGE, ";
                $query .= "     SEX ";
                $query .= " ), CNT2_3 AS ( ";
                //脊柱・胸郭・四肢
                $query .= " SELECT ";
                $query .= "     YEAR, ";
                $query .= "     AGE, ";
                $query .= "     SEX, ";
                $query .= "     NVL(SUM(REC_CNT), 0) AS CNT ";
                $query .= " FROM ";
                $query .= "     SCHREG_B_DIST_MST ";
                $query .= " WHERE ";
                $query .= "     SPINERIBCD = '02' ";    //「02:異常あり」
                $query .= " GROUP BY ";
                $query .= "     YEAR, ";
                $query .= "     AGE, ";
                $query .= "     SEX ";
                $query .= " )";
                //CSVに出力するデータ
                $query .= " SELECT ";
                //受検者数(人)
                $query .= "     1 AS SORT, ";
                $query .= "     '受検者数(人)' AS ROWHEADER ";
                $query .= knjf074aQuery::createPrintRowShippeiCSV();
                $query .= " FROM ";
                $query .= "     CNT2_1 ";
                $query .= " UNION ";
                $query .= " SELECT ";
                //栄養状態
                $query .= "     2 AS SORT, ";
                $query .= "     '栄養状態' AS ROWHEADER ";
                $query .= knjf074aQuery::createPrintRowShippeiCSV();
                $query .= " FROM ";
                $query .= "     CNT2_2 ";
                $query .= " UNION ";
                //脊柱・胸郭・四肢
                $query .= " SELECT ";
                $query .= "     3 AS SORT, ";
                $query .= "     '脊柱・胸郭・四肢' AS ROWHEADER ";
                $query .= knjf074aQuery::createPrintRowShippeiCSV();
                $query .= " FROM ";
                $query .= "     CNT2_3 ";
                $query .= " ORDER BY ";
                $query .= "     SORT ";
                break;
            case 3:
                $query .= " WITH SCHREG_B_DIST_MST2 AS( ";
                $query .= " SELECT DISTINCT ";
                $query .= "     T1.YEAR, ";
                $query .= "     T1.SCHREGNO, ";
                $query .= "     T2.SEX, ";
                $query .= "     YEAR('". CTRL_YEAR."-04-01' - DATE(T2.BIRTHDAY)) AS AGE, ";
                $query .= "     1 AS REC_CNT, ";
                $query .= "     T4.R_BAREVISION_MARK, ";
                $query .= "     T4.L_BAREVISION_MARK, ";
                $query .= "     MAX((CASE T4.R_BAREVISION_MARK ";
                $query .= "               WHEN '01' THEN 1 ";    //A
                $query .= "               WHEN '02' THEN 2 ";    //B
                $query .= "               WHEN '03' THEN 3 ";    //C
                $query .= "               WHEN '04' THEN 4 ";    //D
                $query .= "               ELSE  0 ";
                $query .= "               END), ";
                $query .= "         (CASE T4.L_BAREVISION_MARK ";
                $query .= "               WHEN '01' THEN 1 ";    //A
                $query .= "               WHEN '02' THEN 2 ";    //B
                $query .= "               WHEN '03' THEN 3 ";    //C
                $query .= "               WHEN '04' THEN 4 ";    //D
                $query .= "               ELSE  0 ";
                $query .= "               END)) RLX ";
                $query .= " FROM ";
                $query .= "               SCHREG_REGD_DAT T1 ";
                $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ";
                $query .= "            ON T2.SCHREGNO  = T1.SCHREGNO ";
                $query .= "     LEFT JOIN SCHREG_REGD_GDAT T3 ";
                $query .= "            ON T3.YEAR      = T1.YEAR ";
                $query .= "           AND T3.GRADE     = T1.GRADE ";
                $query .= "     LEFT JOIN MEDEXAM_DET_DAT T4 ";
                $query .= "            ON T4.YEAR      = T1.YEAR ";
                $query .= "           AND T4.SCHREGNO  = T1.SCHREGNO ";
                $query .= "     LEFT JOIN NAME_MST R_CD ";
                $query .= "            ON T4.R_BAREVISION_MARK = R_CD.NAME1 ";
                $query .= "           AND R_CD.NAMECD1 = 'F017' ";
                $query .= "     LEFT JOIN NAME_MST L_CD ";
                $query .= "            ON T4.L_BAREVISION_MARK = L_CD.NAME1 ";
                $query .= "           AND L_CD.NAMECD1 = 'F017' ";
                $query .= " WHERE ";
                $query .= "         T1.YEAR = '".CTRL_YEAR."' ";
                                //裸眼視力検査の結果が登録されている
                $query .= "     AND (   T4.R_BAREVISION_MARK IS NOT NULL ";
                $query .= "          OR T4.R_BAREVISION_MARK IS NOT NULL) ";
                $query .= "     AND YEAR('".CTRL_YEAR."-04-01' - DATE(T2.BIRTHDAY)) < 18 ";
                $query .= " ), CNT3_1 AS ( ";
                //受検者数
                $query .= " SELECT ";
                $query .= "     YEAR, ";
                $query .= "     AGE, ";
                $query .= "     SEX, ";
                $query .= "     NVL(SUM(REC_CNT), 0) AS CNT ";
                $query .= " FROM ";
                $query .= "     SCHREG_B_DIST_MST2 ";
                $query .= " GROUP BY ";
                $query .= "     YEAR, ";
                $query .= "     SEX, ";
                $query .= "     AGE ";
                $query .= " ), CNT3_2 AS ( ";
                //合計
                $query .= " SELECT ";
                $query .= "     YEAR, ";
                $query .= "     AGE, ";
                $query .= "     SEX, ";
                $query .= "     NVL(SUM(REC_CNT), 0) AS CNT ";
                $query .= " FROM ";
                $query .= "     SCHREG_B_DIST_MST2 ";
                $query .= " WHERE ";
                $query .= "     RLX >= 2 ";    //裸眼視力1.0未満
                $query .= " GROUP BY ";
                $query .= "     YEAR, ";
                $query .= "     SEX, ";
                $query .= "     AGE ";
                $query .= " ), CNT3_3 AS ( ";
                //視力1.0～0.7
                $query .= " SELECT ";
                $query .= "     YEAR, ";
                $query .= "     SEX, ";
                $query .= "     AGE, ";
                $query .= "     NVL(SUM(REC_CNT), 0) AS CNT ";
                $query .= " FROM ";
                $query .= "     SCHREG_B_DIST_MST2 ";
                $query .= " WHERE ";
                $query .= "     RLX = 2 ";    //裸眼最低視力が「B」
                $query .= " GROUP BY ";
                $query .= "     YEAR, ";
                $query .= "     SEX, ";
                $query .= "     AGE ";
                $query .= " ), CNT3_4 AS ( ";
                //視力0.7～0.3
                $query .= " SELECT ";
                $query .= "     YEAR, ";
                $query .= "     SEX, ";
                $query .= "     AGE, ";
                $query .= "     NVL(SUM(REC_CNT), 0) AS CNT ";
                $query .= " FROM ";
                $query .= "     SCHREG_B_DIST_MST2 ";
                $query .= " WHERE ";
                $query .= "     RLX = 3 ";    //裸眼最低視力が「C」
                $query .= " GROUP BY ";
                $query .= "     YEAR, ";
                $query .= "     SEX, ";
                $query .= "     AGE ";
                $query .= " ), CNT3_5 AS ( ";
                //視力0.3～
                $query .= " SELECT ";
                $query .= "     YEAR, ";
                $query .= "     SEX, ";
                $query .= "     AGE, ";
                $query .= "     NVL(SUM(REC_CNT), 0) AS CNT ";
                $query .= " FROM ";
                $query .= "     SCHREG_B_DIST_MST2 ";
                $query .= " WHERE ";
                $query .= "     RLX = 4 ";    //裸眼最低視力が「D」
                $query .= " GROUP BY ";
                $query .= "     YEAR, ";
                $query .= "     SEX, ";
                $query .= "     AGE ";
                $query .= " ) ";
                //CSVに出力するデータ
                //受検者数
                $query .= " SELECT ";
                $query .= "     1 AS SORT, ";
                $query .= "     '受検者数(人)' AS ROWHEADER ";
                $query .= knjf074aQuery::createPrintRowShippeiCSV();
                $query .= " FROM ";
                $query .= "     CNT3_1 ";
                $query .= " UNION ";
                //合計
                $query .= " SELECT ";
                $query .= "     2 AS SORT, ";
                $query .= "     '合計（１．０未満）' AS ROWHEADER ";
                $query .= knjf074aQuery::createPrintRowShippeiCSV();
                $query .= " FROM ";
                $query .= "    CNT3_2 ";
                $query .= " UNION ";
                //1.0未満0.7以上
                $query .= " SELECT ";
                $query .= "     3 AS SORT, ";
                $query .= "     '１．０未満０．７以上' AS ROWHEADER ";
                $query .= knjf074aQuery::createPrintRowShippeiCSV();
                $query .= " FROM ";
                $query .= "     CNT3_3 ";
                $query .= " UNION ";
                //0.7未満0.3以上
                $query .= " SELECT ";
                $query .= "     4 AS SORT, ";
                $query .= "     '０．７未満０．３以上' AS ROWHEADER ";
                $query .= knjf074aQuery::createPrintRowShippeiCSV();
                $query .= " FROM ";
                $query .= " CNT3_4 ";
                $query .= " UNION ";
                //0.3未満
                $query .= " SELECT ";
                $query .= "     5 AS SORT, ";
                $query .= "     '０．３未満' AS ROWHEADER ";
                $query .= knjf074aQuery::createPrintRowShippeiCSV();
                $query .= " FROM ";
                $query .= "     CNT3_5 ";
                $query .= " ORDER BY";
                $query .= "     SORT ";
                break;

            case 4:
                $query .= " WITH SCHREG_B_DIST_MST4 AS ( ";
                $query .= " SELECT DISTINCT ";
                $query .= "     T1.YEAR, ";
                $query .= "     T1.SCHREGNO, ";
                $query .= "     T2.SEX, ";
                $query .= "     YEAR('".CTRL_YEAR."-04-01' - DATE(T2.BIRTHDAY)) AS AGE, ";
                $query .= "     1 AS REC_CNT, ";
                $query .= "     T4.R_EAR, ";
                $query .= "     T4.L_EAR ";
                $query .= " FROM ";
                $query .= "           SCHREG_REGD_DAT T1 ";
                $query .= " LEFT JOIN SCHREG_BASE_MST T2 ";
                $query .= "        ON T2.SCHREGNO = T1.SCHREGNO ";
                $query .= " LEFT JOIN SCHREG_REGD_GDAT T3 ";
                $query .= "        ON T3.YEAR     = T1.YEAR ";
                $query .= "       AND T3.GRADE    = T1.GRADE ";
                $query .= " LEFT JOIN MEDEXAM_DET_DAT T4 ";
                $query .= "        ON T4.YEAR     = T1.YEAR ";
                $query .= "       AND T4.SCHREGNO = T1.SCHREGNO ";
                $query .= " WHERE ";
                $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
                            //聴力検査の結果が登録されている
                $query .= " AND (   T4.R_EAR_DB IS NOT NULL ";
                $query .= "      OR T4.L_EAR_DB IS NOT NULL ";
                $query .= "      OR T4.R_EAR    IS NOT NULL ";
                $query .= "      OR T4.L_EAR    IS NOT NULL) ";
                $query .= " AND YEAR('".CTRL_YEAR."-04-01' - DATE(T2.BIRTHDAY)) < 18 ";
                $query .= " ), CNT4_1 AS ( ";
                //受検者数
                $query .= " SELECT ";
                $query .= "     YEAR, ";
                $query .= "     AGE, ";
                $query .= "     SEX, ";
                $query .= "     NVL(SUM(REC_CNT), 0) AS CNT ";
                $query .= " FROM ";
                $query .= "     SCHREG_B_DIST_MST4 ";
                $query .= " GROUP BY ";
                $query .= "     YEAR, ";
                $query .= "     SEX, ";
                $query .= "     AGE ";
                $query .= " ), CNT4_2 AS ( ";
                //難聴
                $query .= " SELECT ";
                $query .= "     YEAR, ";
                $query .= "     AGE, ";
                $query .= "     SEX, ";
                $query .= "     NVL(SUM(REC_CNT), 0) AS CNT ";
                $query .= " FROM ";
                $query .= "     SCHREG_B_DIST_MST4 ";
                $query .= " WHERE ";
                $query .= "         R_EAR = '05' ";    //「05:〇」
                $query .= "     AND L_EAR = '05' ";
                $query .= " GROUP BY ";
                $query .= "     YEAR, ";
                $query .= "     SEX, ";
                $query .= "     AGE ";
                $query .= " ) ";

                //CSVに出力するデータ
                $query .= " SELECT ";
                $query .= "     1 AS SORT, ";
                $query .= "     '受検者数(人)' AS ROWHEADER ";
                $query .= knjf074aQuery::createPrintRowShippeiCSV();
                $query .= " FROM ";
                $query .= "     CNT4_1 ";
                $query .= " UNION ";
                $query .= " SELECT ";
                $query .= "     2 AS SORT, ";
                $query .= "     '難聴' AS ROWHEADER ";
                $query .= knjf074aQuery::createPrintRowShippeiCSV();
                $query .= " FROM ";
                $query .= "     CNT4_2 ";
                $query .= " ORDER BY ";
                $query .= "     SORT ";
                break;

            case 5:
                $query .= " WITH SCHREG_B_DIST_MST5 AS ( ";
                $query .= " SELECT DISTINCT ";
                $query .= "     T1.YEAR, ";
                $query .= "     T1.SCHREGNO, ";
                $query .= "     T2.SEX, ";
                $query .= "     YEAR('".CTRL_YEAR."-04-01' - DATE(T2.BIRTHDAY)) AS AGE, ";
                $query .= "     1 AS REC_CNT, ";
                $query .= "     T4.EYEDISEASECD ";
                $query .= " FROM ";
                $query .= "           SCHREG_REGD_DAT T1 ";
                $query .= " LEFT JOIN SCHREG_BASE_MST T2 ";
                $query .= "        ON T2.SCHREGNO = T1.SCHREGNO ";
                $query .= " LEFT JOIN SCHREG_REGD_GDAT T3 ";
                $query .= "        ON T3.YEAR     = T1.YEAR ";
                $query .= "       AND T3.GRADE    = T1.GRADE ";
                $query .= " LEFT JOIN MEDEXAM_DET_DAT T4 ";
                $query .= "        ON T4.YEAR     = T1.YEAR ";
                $query .= "       AND T4.SCHREGNO = T1.SCHREGNO ";
                $query .= " WHERE ";
                $query .= "         T1.YEAR = '".CTRL_YEAR."' ";
                                //眼科検診の結果が「00:未受験」でなく、登録されている
                $query .= "     AND (   (CASE WHEN T4.EYEDISEASECD =  '00' THEN 0 ";
                $query .= "                   WHEN T4.EYEDISEASECD IS NULL THEN 0 ";
                $query .= "                   ELSE 1 END) = 1) ";
                $query .= " ), CNT5_1 AS ( ";
                //受検者数
                $query .= " SELECT ";
                $query .= "     YEAR, ";
                $query .= "     AGE, ";
                $query .= "     SEX, ";
                $query .= "     NVL(SUM(REC_CNT), 0) AS CNT ";
                $query .= " FROM ";
                $query .= "     SCHREG_B_DIST_MST5 ";
                $query .= " GROUP BY ";
                $query .= "     YEAR, ";
                $query .= "     SEX, ";
                $query .= "     AGE ";
                $query .= " ), CNT5_2 AS ( ";
                //眼・アレルギー疾患
                $query .= " SELECT ";
                $query .= "     YEAR, ";
                $query .= "     AGE, ";
                $query .= "     SEX, ";
                $query .= "     NVL(SUM(REC_CNT), 0) AS CNT ";
                $query .= " FROM ";
                $query .= "     SCHREG_B_DIST_MST5 ";
                $query .= " WHERE ";
                $query .= "     EYEDISEASECD IN ('11', '12', '13') ";    //「11:アレルギー性結膜炎」～「13:眼瞼炎」
                $query .= " GROUP BY ";
                $query .= "     YEAR, ";
                $query .= "     SEX, ";
                $query .= "     AGE ";
                $query .= " ), CNT5_3 AS ( ";
                //眼・その他の眼疾異常
                $query .= " SELECT ";
                $query .= "     YEAR, ";
                $query .= "     AGE, ";
                $query .= "     SEX, ";
                $query .= "     NVL(SUM(REC_CNT), 0) AS CNT ";
                $query .= " FROM ";
                $query .= "     SCHREG_B_DIST_MST5 ";
                $query .= " WHERE ";
                $query .= "     EYEDISEASECD IN ('21', '22', '23', '24', '25', '26', '27') ";    //「21:内反症（さかまつげ）」～「27:流行性角結膜炎症」
                $query .= " GROUP BY ";
                $query .= "     YEAR, ";
                $query .= "     SEX, ";
                $query .= "     AGE ";
                $query .= " ) ";
                //CSVに出力するデータ
                //受検者数
                $query .= " SELECT ";
                $query .= "     1 AS SORT, ";
                $query .= "     '受検者数(人)' AS ROWHEADER ";
                $query .= knjf074aQuery::createPrintRowShippeiCSV();
                $query .= " FROM ";
                $query .= " CNT5_1 ";
                $query .= " UNION ";
                //眼・アレルギー疾患
                $query .= " SELECT ";
                $query .= "     2 AS SORT, ";
                $query .= "     '眼・アレルギー性疾患' AS ROWHEADER ";
                $query .= knjf074aQuery::createPrintRowShippeiCSV();
                $query .= " FROM ";
                $query .= " CNT5_2 ";
                $query .= " UNION ";
                //眼・その他の眼疾異常
                $query .= " SELECT ";
                $query .= "     3 AS SORT, ";
                $query .= "     '眼・その他の眼疾異常' AS ROWHEADER ";
                $query .= knjf074aQuery::createPrintRowShippeiCSV();
                $query .= " FROM ";
                $query .= " CNT5_3 ";
                $query .= " ORDER BY ";
                $query .= " SORT ";
                break;

            case 6:
                $query .= " WITH SCHREG_B_DIST_MST6 AS ( ";
                $query .= " SELECT DISTINCT ";
                $query .= "     T1.YEAR, ";
                $query .= "     T1.SCHREGNO, ";
                $query .= "     T2.SEX, ";
                $query .= "     YEAR('".CTRL_YEAR."-04-01' - DATE(T2.BIRTHDAY)) AS AGE, ";
                $query .= "     1 AS REC_CNT ";
                $query .= " FROM ";
                $query .= "           SCHREG_REGD_DAT T1 ";
                $query .= " LEFT JOIN SCHREG_BASE_MST T2 ";
                $query .= "        ON T2.SCHREGNO = T1.SCHREGNO ";
                $query .= " LEFT JOIN SCHREG_REGD_GDAT T3 ";
                $query .= "        ON T3.YEAR     = T1.YEAR ";
                $query .= "       AND T3.GRADE    = T1.GRADE ";
                $query .= " LEFT JOIN MEDEXAM_DET_DAT T4 ";
                $query .= "        ON T4.YEAR     = T1.YEAR ";
                $query .= "       AND T4.SCHREGNO = T1.SCHREGNO ";
                $query .= " WHERE ";
                $query .= "         T1.YEAR = '".CTRL_YEAR."' ";
                                //全般が「00:未受験」でなく、登録されている
                $query .= "     AND (   (CASE WHEN T4.NOSEDISEASECD =  '00' THEN 0 ";
                $query .= "                   WHEN T4.NOSEDISEASECD IS NULL THEN 0 ";
                $query .= "                   ELSE 1 END) = 1) ";
                $query .= " ), SCHREG_B_DIST_MST6_2 AS ( ";
                //受験者数のカウント用とは別にDETAILを結合したテーブルを作成
                $query .= " SELECT DISTINCT ";
                $query .= "     T1.YEAR, ";
                $query .= "     T1.SCHREGNO, ";
                $query .= "     T1.SEX, ";
                $query .= "     T1.AGE, ";
                $query .= "     1 AS REC_CNT, ";
                $query .= "     T2.DET_REMARK7, ";
                $query .= "     T2.DET_REMARK8, ";
                $query .= "     T2.DET_REMARK9 ";
                $query .= " FROM ";
                $query .= "           SCHREG_B_DIST_MST6 T1 ";
                $query .= " LEFT JOIN MEDEXAM_DET_DETAIL_DAT T2 ";
                $query .= "        ON T1.YEAR     = T2.YEAR ";
                $query .= "       AND T1.SCHREGNO = T2.SCHREGNO ";
                $query .= " WHERE ";
                $query .= "         T2.DET_SEQ = '003' ";
                $query .= " ), CNT6_1 AS ( ";
                //受検者数
                $query .= " SELECT ";
                $query .= "     YEAR, ";
                $query .= "     AGE, ";
                $query .= "     SEX, ";
                $query .= "     NVL(SUM(REC_CNT), 0) AS CNT ";
                $query .= " FROM ";
                $query .= "     SCHREG_B_DIST_MST6 ";
                $query .= " GROUP BY ";
                $query .= "     YEAR, ";
                $query .= "     SEX, ";
                $query .= "     AGE ";
                $query .= " ), CNT6_2 AS ( ";
                //耳疾患
                $query .= " SELECT ";
                $query .= "     YEAR, ";
                $query .= "     AGE, ";
                $query .= "     SEX, ";
                $query .= "     NVL(SUM(REC_CNT), 0) AS CNT ";
                $query .= " FROM ";
                $query .= "     SCHREG_B_DIST_MST6_2 ";
                $query .= " WHERE ";
                $query .= "              DET_REMARK7 IN ('11', '12', '13', '14') ";    //「11:耳垢栓塞」～「14:難聴の疑い」
                $query .= "          OR  DET_REMARK8 IN ('11', '12', '13', '14') ";
                $query .= "          OR  DET_REMARK9 IN ('11', '12', '13', '14') ";
                $query .= " GROUP BY ";
                $query .= "     YEAR, ";
                $query .= "     SEX, ";
                $query .= "     AGE ";
                $query .= " ), CNT6_3 AS ( ";
                //鼻・副鼻腔疾患
                $query .= " SELECT ";
                $query .= "     YEAR, ";
                $query .= "     AGE, ";
                $query .= "     SEX, ";
                $query .= "     NVL(SUM(REC_CNT), 0) AS CNT ";
                $query .= " FROM ";
                $query .= "     SCHREG_B_DIST_MST6_2 ";
                $query .= " WHERE ";
                $query .= "             DET_REMARK7 IN ('32', '33', '34') ";    //「32:副鼻腔炎」～「34:鼻中隔わん曲症」
                $query .= "         OR  DET_REMARK8 IN ('32', '33', '34') ";
                $query .= "         OR  DET_REMARK9 IN ('32', '33', '34') ";
                $query .= " GROUP BY ";
                $query .= "     YEAR, ";
                $query .= "     SEX, ";
                $query .= "     AGE ";
                $query .= " ), CNT6_4 AS ( ";
                //耳鼻咽頭アレルギー性疾患
                $query .= " SELECT ";
                $query .= "     YEAR, ";
                $query .= "     AGE, ";
                $query .= "     SEX, ";
                $query .= "     NVL(SUM(REC_CNT), 0) AS CNT ";
                $query .= " FROM ";
                $query .= "     SCHREG_B_DIST_MST6_2 ";
                $query .= " WHERE ";
                $query .= "             DET_REMARK7 = '31' ";    //「31:アレルギー性鼻炎」
                $query .= "         OR  DET_REMARK8 = '31' ";
                $query .= "         OR  DET_REMARK9 = '31' ";
                $query .= " GROUP BY ";
                $query .= "     YEAR, ";
                $query .= "     SEX, ";
                $query .= "     AGE ";
                $query .= " ), CNT6_5 AS ( ";
                //口腔咽頭疾患・異常
                $query .= " SELECT ";
                $query .= "     YEAR, ";
                $query .= "     AGE, ";
                $query .= "     SEX, ";
                $query .= "     NVL(SUM(REC_CNT), 0) AS CNT ";
                $query .= " FROM ";
                $query .= "     SCHREG_B_DIST_MST6_2 ";
                $query .= " WHERE ";
                                                     //「51:アデノイドの疑い」～「61:咽頭炎」
                $str = "'51', '52', '53', '54', '55', '56', '57', '58', '59', '60', '61'";
                $query .= "             DET_REMARK7 IN (".$str.") ";
                $query .= "         OR  DET_REMARK8 IN (".$str.") ";
                $query .= "         OR  DET_REMARK9 IN (".$str.") ";
                $query .= " GROUP BY ";
                $query .= "     YEAR, ";
                $query .= "     SEX, ";
                $query .= "     AGE ";
                $query .= " ) ";
                //CSVに出力するデータ
                //受検者数
                $query .= " SELECT ";
                $query .= "     1 AS SORT, ";
                $query .= "     '受検者数(人)' AS ROWHEADER ";
                $query .= knjf074aQuery::createPrintRowShippeiCSV();
                $query .= " FROM ";
                $query .= " CNT6_1 ";
                $query .= " UNION ";
                //耳疾患
                $query .= " SELECT ";
                $query .= "     2 AS SORT, ";
                $query .= "     '耳疾患' AS ROWHEADER ";
                $query .= knjf074aQuery::createPrintRowShippeiCSV();
                $query .= " FROM ";
                $query .= " CNT6_2 ";
                $query .= " UNION ";
                //鼻・副鼻腔疾患
                $query .= " SELECT ";
                $query .= "     3 AS SORT, ";
                $query .= "     '鼻・副鼻腔疾患' AS ROWHEADER ";
                $query .= knjf074aQuery::createPrintRowShippeiCSV();
                $query .= " FROM ";
                $query .= " CNT6_3 ";
                $query .= " UNION ";
                //耳鼻咽頭アレルギー性疾患
                $query .= " SELECT ";
                $query .= "     4 AS SORT, ";
                $query .= "     '耳鼻咽喉アレルギー性疾患' AS ROWHEADER ";
                $query .= knjf074aQuery::createPrintRowShippeiCSV();
                $query .= " FROM ";
                $query .= " CNT6_4 ";
                $query .= " UNION ";
                //口腔咽頭疾患・異常
                $query .= " SELECT ";
                $query .= "     5 AS SORT, ";
                $query .= "     '口腔咽喉頭疾患・異常' AS ROWHEADER ";
                $query .= knjf074aQuery::createPrintRowShippeiCSV();
                $query .= " FROM ";
                $query .= " CNT6_5 ";
                $query .= " ORDER BY ";
                $query .= " SORT ";
                break;

            case 7:
                $query .= " WITH SCHREG_B_DIST_MST7 AS ( ";
                $query .= " SELECT DISTINCT ";
                $query .= "     T1.YEAR, ";
                $query .= "     T1.SCHREGNO, ";
                $query .= "     T2.SEX, ";
                $query .= "     YEAR('".CTRL_YEAR."-04-01' - DATE(T2.BIRTHDAY)) AS AGE, ";
                $query .= "     1 AS REC_CNT, ";
                $query .= "     T4.SKINDISEASECD ";
                $query .= " FROM ";
                $query .= "           SCHREG_REGD_DAT T1 ";
                $query .= " LEFT JOIN SCHREG_BASE_MST T2 ";
                $query .= "        ON T2.SCHREGNO = T1.SCHREGNO ";
                $query .= " LEFT JOIN SCHREG_REGD_GDAT T3 ";
                $query .= "        ON T3.YEAR     = T1.YEAR ";
                $query .= "       AND T3.GRADE    = T1.GRADE ";
                $query .= " LEFT JOIN MEDEXAM_DET_DAT T4 ";
                $query .= "        ON T4.YEAR     = T1.YEAR ";
                $query .= "       AND T4.SCHREGNO = T1.SCHREGNO ";
                $query .= " WHERE ";
                $query .= "         T1.YEAR = '".CTRL_YEAR."' ";
                                //皮膚疾患の診断結果が「00:未受験」でなく、登録されている
                $query .= "     AND (   (CASE WHEN T4.SKINDISEASECD =  '00' THEN 0 ";
                $query .= "                   WHEN T4.SKINDISEASECD IS NULL THEN 0 ";
                $query .= "                   ELSE 1 END) = 1) ";
                $query .= " ), CNT7_1 AS ( ";
                //受検者数
                $query .= " SELECT ";
                $query .= "     YEAR, ";
                $query .= "     AGE, ";
                $query .= "     SEX, ";
                $query .= "     NVL(SUM(REC_CNT), 0) AS CNT ";
                $query .= " FROM ";
                $query .= "     SCHREG_B_DIST_MST7 ";
                $query .= " GROUP BY ";
                $query .= "     YEAR, ";
                $query .= "     SEX, ";
                $query .= "     AGE ";
                $query .= " ), CNT7_2 AS ( ";
                //皮膚アレルギー性疾患
                $query .= " SELECT ";
                $query .= "     YEAR, ";
                $query .= "     AGE, ";
                $query .= "     SEX, ";
                $query .= "     NVL(SUM(REC_CNT), 0) AS CNT ";
                $query .= " FROM ";
                $query .= "     SCHREG_B_DIST_MST7 ";
                $query .= " WHERE ";
                $query .= "     SKINDISEASECD = '11' ";    //「11:アトピー性皮膚炎」
                $query .= " GROUP BY ";
                $query .= "     YEAR, ";
                $query .= "     SEX, ";
                $query .= "     AGE ";
                $query .= " ), CNT7_3 AS ( ";
                //その他の皮膚疾患
                $query .= " SELECT ";
                $query .= "     YEAR, ";
                $query .= "     AGE, ";
                $query .= "     SEX, ";
                $query .= "     NVL(SUM(REC_CNT), 0) AS CNT ";
                $query .= " FROM ";
                $query .= "     SCHREG_B_DIST_MST7 ";
                $query .= " WHERE ";
                                               //「12:かぶれ」～「16:円形脱毛症」「21:白癬」～「25:その他の感染性皮膚疾患」
                $query .= "     SKINDISEASECD IN ('12', '13', '14', '15', '16', '21', '22', '23', '24', '25') ";
                $query .= " GROUP BY ";
                $query .= "     YEAR, ";
                $query .= "     SEX, ";
                $query .= "     AGE ";
                $query .= " ) ";
                //CSVに出力するデータ
                //受検者数
                $query .= " SELECT ";
                $query .= "     1 AS SORT, ";
                $query .= "     '受検者数(人)' AS ROWHEADER ";
                $query .= knjf074aQuery::createPrintRowShippeiCSV();
                $query .= " FROM ";
                $query .= " CNT7_1 ";
                $query .= " UNION ";
                //皮膚アレルギー性疾患
                $query .= " SELECT ";
                $query .= "     2 AS SORT, ";
                $query .= "     '皮膚アレルギー性疾患' AS ROWHEADER ";
                $query .= knjf074aQuery::createPrintRowShippeiCSV();
                $query .= " FROM ";
                $query .= " CNT7_2 ";
                $query .= " UNION ";
                //その他の皮膚疾患
                $query .= " SELECT ";
                $query .= "     3 AS SORT, ";
                $query .= "     'その他の皮膚疾患' AS ROWHEADER ";
                $query .= knjf074aQuery::createPrintRowShippeiCSV();
                $query .= " FROM ";
                $query .= "     CNT7_3 ";
                $query .= "     ORDER BY ";
                $query .= "     SORT ";
                break;

            case 8:
                $query .= " WITH SCHREG_B_DIST_MST8 AS ( ";
                $query .= " SELECT DISTINCT ";
                $query .= "     T1.YEAR, ";
                $query .= "     T1.SCHREGNO, ";
                $query .= "     T2.SEX, ";
                $query .= "     YEAR('".CTRL_YEAR."-04-01' - DATE(T2.BIRTHDAY)) AS AGE, ";
                $query .= "     1 AS REC_CNT, ";
                $query .= "     T6.REMAINBABYTOOTH, ";
                $query .= "     T6.TREATEDBABYTOOTH, ";
                $query .= "     T6.REMAINADULTTOOTH, ";
                $query .= "     T6.TREATEDADULTTOOTH, ";
                $query .= "     T6.JAWS_JOINTCD, ";
                $query .= "     T6.JAWS_JOINTCD2, ";
                $query .= "     T6.PLAQUECD, ";
                $query .= "     T6.GUMCD, ";
                $query .= "     T6.BRACK_BABYTOOTH ";
                $query .= " FROM ";
                $query .= "           SCHREG_REGD_DAT T1 ";
                $query .= " LEFT JOIN SCHREG_BASE_MST T2 ";
                $query .= "        ON T2.SCHREGNO = T1.SCHREGNO ";
                $query .= " LEFT JOIN SCHREG_REGD_GDAT T3 ";
                $query .= "        ON T3.YEAR     = T1.YEAR ";
                $query .= "       AND T3.GRADE    = T1.GRADE ";
                $query .= " LEFT JOIN MEDEXAM_DET_DAT T4 ";
                $query .= "        ON T4.YEAR     = T1.YEAR ";
                $query .= "       AND T4.SCHREGNO = T1.SCHREGNO ";
                $query .= " LEFT JOIN MEDEXAM_HDAT T5 ";
                $query .= "        ON T5.YEAR     = T1.YEAR ";
                $query .= "       AND T5.SCHREGNO = T1.SCHREGNO ";
                $query .= " LEFT JOIN MEDEXAM_TOOTH_DAT T6 ";
                $query .= "        ON T6.YEAR     = T1.YEAR ";
                $query .= "       AND T6.SCHREGNO = T1.SCHREGNO ";
                $query .= " WHERE ";
                $query .= "         T1.YEAR       = '".CTRL_YEAR."' ";
                                //歯科検診の実施日が登録されている
                $query .= "     AND T5.TOOTH_DATE IS NOT NULL ";
                $query .= " ), SCHREG_B_DIST_MST8_2 AS ( ";
                //受験者数のカウント用とは別にDETAILを結合したテーブルを作成
                $query .= " SELECT DISTINCT ";
                $query .= "     T1.YEAR, ";
                $query .= "     T1.SCHREGNO, ";
                $query .= "     T1.SEX, ";
                $query .= "     T1.AGE, ";
                $query .= "     1 AS REC_CNT, ";
                $query .= "     T1.REMAINBABYTOOTH, ";
                $query .= "     T1.TREATEDBABYTOOTH, ";
                $query .= "     T1.REMAINADULTTOOTH, ";
                $query .= "     T1.TREATEDADULTTOOTH, ";
                $query .= "     T1.JAWS_JOINTCD, ";
                $query .= "     T1.JAWS_JOINTCD2, ";
                $query .= "     T1.PLAQUECD, ";
                $query .= "     T1.GUMCD, ";
                $query .= "     T1.BRACK_BABYTOOTH, ";
                $query .= "     T2.TOOTH_REMARK1 ";
                $query .= " FROM ";
                $query .= "           SCHREG_B_DIST_MST8 T1 ";
                $query .= " LEFT JOIN MEDEXAM_TOOTH_DETAIL_DAT T2 ";
                $query .= "        ON T1.YEAR     = T2.YEAR ";
                $query .= "       AND T1.SCHREGNO = T2.SCHREGNO ";
                $query .= " WHERE ";
                $query .= "         T2.TOOTH_SEQ = '002' ";
                $query .= " ), CNT8_1 AS ( ";
                //受検者数
                $query .= " SELECT ";
                $query .= "     YEAR, ";
                $query .= "     AGE, ";
                $query .= "     SEX, ";
                $query .= "     NVL(SUM(REC_CNT), 0) AS CNT ";
                $query .= " FROM ";
                $query .= "     SCHREG_B_DIST_MST8 ";
                $query .= " GROUP BY ";
                $query .= "     YEAR, ";
                $query .= "     SEX, ";
                $query .= "     AGE ";
                $query .= " ), CNT8_2 AS ( ";
                //う歯がない者（健全歯）
                $query .= " SELECT ";
                $query .= "     YEAR, ";
                $query .= "     AGE, ";
                $query .= "     SEX, ";
                $query .= "     NVL(SUM(REC_CNT), 0) AS CNT ";
                $query .= " FROM ";
                $query .= "     SCHREG_B_DIST_MST8 ";
                $query .= " WHERE ";
                $query .= "         REMAINBABYTOOTH   = 0 ";    //乳歯未処置数
                $query .= "     AND TREATEDBABYTOOTH  = 0 ";    //乳歯処置数
                $query .= "     AND REMAINADULTTOOTH  = 0 ";    //永久歯未処置数
                $query .= "     AND TREATEDADULTTOOTH = 0 ";    //永久歯処置数
                $query .= " GROUP BY ";
                $query .= "     YEAR, ";
                $query .= "     SEX, ";
                $query .= "     AGE ";
                $query .= " ), CNT8_4 AS ( ";
                //むし歯（う歯)／すべてのう歯／処置完了者
                $query .= " SELECT ";
                $query .= "     YEAR, ";
                $query .= "     AGE, ";
                $query .= "     SEX, ";
                $query .= "     NVL(SUM(REC_CNT), 0) AS CNT ";
                $query .= " FROM ";
                $query .= "     SCHREG_B_DIST_MST8 ";
                $query .= " WHERE ";
                $query .= "         REMAINBABYTOOTH  = 0 ";           //乳歯未処置数
                $query .= "     AND REMAINADULTTOOTH = 0 ";           //永久歯未処置数
                $query .= "     AND (   1 <= TREATEDBABYTOOTH ";      //乳歯処置数
                $query .= "          OR 1 <= TREATEDADULTTOOTH) ";    //永久歯処置数
                $query .= " GROUP BY ";
                $query .= "     YEAR, ";
                $query .= "     SEX, ";
                $query .= "     AGE ";
                $query .= " ), CNT8_5 AS ( ";
                //むし歯（う歯)／すべてのう歯／未処置歯のある者
                $query .= " SELECT ";
                $query .= "     YEAR, ";
                $query .= "     AGE, ";
                $query .= "     SEX, ";
                $query .= "     NVL(SUM(REC_CNT), 0) AS CNT ";
                $query .= " FROM ";
                $query .= "     SCHREG_B_DIST_MST8 ";
                $query .= " WHERE ";
                $query .= "         1 <= REMAINBABYTOOTH ";     //乳歯未処置数
                $query .= "      OR 1 <= REMAINADULTTOOTH ";    //永久歯未処置数
                $query .= " GROUP BY ";
                $query .= "     YEAR, ";
                $query .= "     SEX, ";
                $query .= "     AGE ";
                $query .= " ), CNT8_3 AS ( ";
                //むし歯（う歯)／すべてのう歯／合計
                $query .= " SELECT ";
                $query .= "     CASE WHEN CNT8_4.YEAR IS NOT NULL THEN CNT8_4.YEAR ";
                $query .= "          ELSE CNT8_5.YEAR ";
                $query .= "          END AS YEAR, ";
                $query .= "     CASE WHEN CNT8_4.AGE IS NOT NULL THEN CNT8_4.AGE ";
                $query .= "          ELSE CNT8_5.AGE ";
                $query .= "          END AS AGE, ";
                $query .= "     CASE WHEN CNT8_4.SEX IS NOT NULL THEN CNT8_4.SEX ";
                $query .= "          ELSE CNT8_5.SEX ";
                $query .= "          END AS SEX, ";
                                //(むし歯（う歯)／すべてのう歯／処置完了者) + （むし歯（う歯)／すべてのう歯／未処置歯のある者）
                $query .= "     NVL(SUM(CNT8_4.CNT), 0) + NVL(SUM(CNT8_5.CNT), 0) AS CNT ";
                $query .= " FROM ";
                $query .= "                CNT8_4 ";
                $query .= "FULL OUTER JOIN CNT8_5 ";
                $query .= "             ON CNT8_4.YEAR = CNT8_5.YEAR ";
                $query .= "            AND CNT8_4.AGE  = CNT8_5.AGE ";
                $query .= "            AND CNT8_4.SEX  = CNT8_5.SEX ";
                $query .= " GROUP BY ";
                $query .= "     CNT8_4.YEAR, ";
                $query .= "     CNT8_4.SEX, ";
                $query .= "     CNT8_4.AGE, ";
                $query .= "     CNT8_5.YEAR, ";
                $query .= "     CNT8_5.SEX, ";
                $query .= "     CNT8_5.AGE ";
                $query .= " ), CNT8_6 AS ( ";
                //むし歯（う歯)／永久歯／処置完了者
                $query .= " SELECT ";
                $query .= "     YEAR, ";
                $query .= "     AGE, ";
                $query .= "     SEX, ";
                $query .= "     NVL(SUM(REC_CNT), 0) AS CNT ";
                $query .= " FROM ";
                $query .= "     SCHREG_B_DIST_MST8 ";
                $query .= " WHERE ";
                $query .= "         REMAINADULTTOOTH = 0 ";      //永久歯未処置数
                $query .= "     AND 1 <= TREATEDADULTTOOTH ";    //永久歯処置数
                $query .= " GROUP BY ";
                $query .= "     YEAR, ";
                $query .= "     SEX, ";
                $query .= "     AGE ";
                $query .= " ), CNT8_7 AS ( ";
                //むし歯（う歯)／永久歯／未処置歯のある者
                $query .= " SELECT ";
                $query .= "     YEAR, ";
                $query .= "     AGE, ";
                $query .= "     SEX, ";
                $query .= "     NVL(SUM(REC_CNT), 0) AS CNT ";
                $query .= " FROM ";
                $query .= "     SCHREG_B_DIST_MST8 ";
                $query .= " WHERE ";
                $query .= "     1 <= REMAINADULTTOOTH ";    //永久歯未処置数
                $query .= " GROUP BY ";
                $query .= "     YEAR, ";
                $query .= "     SEX, ";
                $query .= "     AGE ";
                $query .= " ), CNT8_8 AS ( ";
                //歯列・咬合
                $query .= " SELECT ";
                $query .= "     YEAR, ";
                $query .= "     AGE, ";
                $query .= "     SEX, ";
                $query .= "     NVL(SUM(REC_CNT), 0) AS CNT ";
                $query .= " FROM ";
                $query .= "     SCHREG_B_DIST_MST8 ";
                $query .= " WHERE ";
                $query .= "     JAWS_JOINTCD = '03' ";    //「03:専門医（歯科医師）による診断が必要」
                $query .= " GROUP BY ";
                $query .= "     YEAR, ";
                $query .= "     SEX, ";
                $query .= "     AGE ";
                $query .= " ), CNT8_9 AS ( ";
                //顎関節
                $query .= " SELECT ";
                $query .= "     YEAR, ";
                $query .= "     AGE, ";
                $query .= "     SEX, ";
                $query .= "     NVL(SUM(REC_CNT), 0) AS CNT ";
                $query .= " FROM ";
                $query .= "     SCHREG_B_DIST_MST8 ";
                $query .= " WHERE ";
                $query .= "     JAWS_JOINTCD2 = '03' ";    //「03:専門医（歯科医師）による診断が必要」
                $query .= " GROUP BY ";
                $query .= "     YEAR, ";
                $query .= "     SEX, ";
                $query .= "     AGE ";
                $query .= " ), CNT8_10 AS ( ";
                //歯垢の状態
                $query .= " SELECT ";
                $query .= "     YEAR, ";
                $query .= "     AGE, ";
                $query .= "     SEX, ";
                $query .= "     NVL(SUM(REC_CNT), 0) AS CNT ";
                $query .= " FROM ";
                $query .= "     SCHREG_B_DIST_MST8 ";
                $query .= " WHERE ";
                $query .= "     PLAQUECD = '03' ";    //「03:相当の付着有」
                $query .= " GROUP BY ";
                $query .= "     YEAR, ";
                $query .= "     SEX, ";
                $query .= "     AGE ";
                $query .= " ), CNT8_11 AS ( ";
                //歯肉の状態
                $query .= " SELECT ";
                $query .= "     YEAR, ";
                $query .= "     AGE, ";
                $query .= "     SEX, ";
                $query .= "     NVL(SUM(REC_CNT), 0) AS CNT ";
                $query .= " FROM ";
                $query .= "     SCHREG_B_DIST_MST8 ";
                $query .= " WHERE ";
                $query .= "     GUMCD = '03' ";    //「03:専門医（歯科技師）による診断が必要」
                $query .= " GROUP BY ";
                $query .= "     YEAR, ";
                $query .= "     SEX, ";
                $query .= "     AGE ";
                $query .= " ), CNT8_12 AS ( ";
                //その他の歯疾異常
                $query .= " SELECT ";
                $query .= "     YEAR, ";
                $query .= "     AGE, ";
                $query .= "     SEX, ";
                $query .= "     NVL(SUM(REC_CNT), 0) AS CNT ";
                $query .= " FROM ";
                $query .= "     SCHREG_B_DIST_MST8_2 ";
                $query .= " WHERE ";
                $query .= "          1 <= BRACK_BABYTOOTH ";      //要注意乳歯
                $query .= "      OR ((CASE WHEN TOOTH_REMARK1 =  '00' THEN 0 ";    //「00:未受検」
                $query .= "                    WHEN TOOTH_REMARK1 =  '01' THEN 0 ";    //「01:異常なし」
                $query .= "                    WHEN TOOTH_REMARK1 IS NULL THEN 0 ";
                $query .= "                    ELSE 1 END) =1 ";
                $query .= "      ) ";
                $query .= " GROUP BY ";
                $query .= "     YEAR, ";
                $query .= "     SEX, ";
                $query .= "     AGE ";
                $query .= " ) ";
                //CSVに出力するデータ
                //受検者数
                $query .= " SELECT ";
                $query .= "     1 AS SORT, ";
                $query .= "     '受検者数(人)' AS ROWHEADER ";
                $query .= knjf074aQuery::createPrintRowShippeiCSV();
                $query .= " FROM ";
                $query .= " CNT8_1 ";
                $query .= " UNION ";
                //う歯がない者（健全歯）
                $query .= " SELECT ";
                $query .= "     2 AS SORT, ";
                $query .= "     'う歯がない者（健全歯）' AS ROWHEADER ";
                $query .= knjf074aQuery::createPrintRowShippeiCSV();
                $query .= " FROM ";
                $query .= " CNT8_2 ";
                $query .= " UNION ";
                //むし歯（う歯)／すべてのう歯／合計
                $query .= " SELECT ";
                $query .= "     3 AS SORT, ";
                $query .= "     'むし歯（う歯)／すべてのう歯／合計' AS ROWHEADER ";
                $query .= knjf074aQuery::createPrintRowShippeiCSV();
                $query .= " FROM ";
                $query .= " CNT8_3 ";
                $query .= " UNION ";
                //むし歯（う歯)／すべてのう歯／処置完了者
                $query .= " SELECT ";
                $query .= "     4 AS SORT, ";
                $query .= "     'むし歯（う歯)／すべてのう歯／処置完了者' AS ROWHEADER ";
                $query .= knjf074aQuery::createPrintRowShippeiCSV();
                $query .= " FROM ";
                $query .= " CNT8_4 ";
                $query .= " UNION ";
                //むし歯（う歯)／すべてのう歯／未処置歯のある者
                $query .= " SELECT ";
                $query .= "     5 AS SORT, ";
                $query .= "     'むし歯（う歯)／すべてのう歯／未処置歯のある者' AS ROWHEADER ";
                $query .= knjf074aQuery::createPrintRowShippeiCSV();
                $query .= " FROM ";
                $query .= " CNT8_5 ";
                $query .= " UNION ";
                //むし歯（う歯)／永久歯／処置完了者
                $query .= " SELECT ";
                $query .= "     6 AS SORT, ";
                $query .= "     'むし歯（う歯)／永久歯／処置完了者' AS ROWHEADER ";
                $query .= knjf074aQuery::createPrintRowShippeiCSV();
                $query .= " FROM ";
                $query .= " CNT8_6 ";
                $query .= " UNION ";
                //むし歯（う歯)／永久歯／未処置歯のある者
                $query .= " SELECT ";
                $query .= "     7 AS SORT, ";
                $query .= "     'むし歯（う歯)／永久歯／未処置歯のある者' AS ROWHEADER ";
                $query .= knjf074aQuery::createPrintRowShippeiCSV();
                $query .= " FROM ";
                $query .= " CNT8_7 ";
                $query .= " UNION ";
                //歯列・咬合
                $query .= " SELECT ";
                $query .= "     8 AS SORT, ";
                $query .= "     '歯列・咬合' AS ROWHEADER ";
                $query .= knjf074aQuery::createPrintRowShippeiCSV();
                $query .= " FROM ";
                $query .= " CNT8_8 ";
                $query .= " UNION ";
                //顎関節
                $query .= " SELECT ";
                $query .= "     9 AS SORT, ";
                $query .= "     '顎関節' AS ROWHEADER ";
                $query .= knjf074aQuery::createPrintRowShippeiCSV();
                $query .= " FROM ";
                $query .= " CNT8_9 ";
                $query .= " UNION ";
                //歯垢の状態
                $query .= " SELECT ";
                $query .= "     10 AS SORT, ";
                $query .= "     '歯垢の状態' AS ROWHEADER ";
                $query .= knjf074aQuery::createPrintRowShippeiCSV();
                $query .= " FROM ";
                $query .= " CNT8_10 ";
                $query .= " UNION ";
                //歯肉の状態
                $query .= " SELECT ";
                $query .= "     11 AS SORT, ";
                $query .= "     '歯肉の状態' AS ROWHEADER ";
                $query .= knjf074aQuery::createPrintRowShippeiCSV();
                $query .= " FROM ";
                $query .= " CNT8_11 ";
                $query .= " UNION ";
                //その他の歯疾異常
                $query .= " SELECT ";
                $query .= "     12 AS SORT, ";
                $query .= "     'その他の歯疾異常' AS ROWHEADER ";
                $query .= knjf074aQuery::createPrintRowShippeiCSV();
                $query .= " FROM ";
                $query .= " CNT8_12 ";
                $query .= " ORDER BY ";
                $query .= " SORT ";
                break;

            case 9:
                $query .= " WITH SCHREG_B_DIST_MST9 AS ( ";
                $query .= " SELECT DISTINCT ";
                $query .= "     T1.YEAR, ";
                $query .= "     T1.SCHREGNO, ";
                $query .= "     T2.SEX, ";
                $query .= "     YEAR('".CTRL_YEAR."-04-01' - DATE(T2.BIRTHDAY)) AS AGE, ";
                $query .= "     1 AS REC_CNT, ";
                $query .= "     T4.TB_REMARKCD, ";
                $query .= "     T4.TB_ADVISECD ";
                $query .= " FROM ";
                $query .= "           SCHREG_REGD_DAT T1 ";
                $query .= " LEFT JOIN SCHREG_BASE_MST T2 ";
                $query .= "        ON T2.SCHREGNO = T1.SCHREGNO ";
                $query .= " LEFT JOIN SCHREG_REGD_GDAT T3 ";
                $query .= "        ON T3.YEAR     = T1.YEAR ";
                $query .= "       AND T3.GRADE    = T1.GRADE ";
                $query .= " LEFT JOIN MEDEXAM_DET_DAT T4 ";
                $query .= "        ON T4.YEAR     = T1.YEAR ";
                $query .= "       AND T4.SCHREGNO = T1.SCHREGNO ";
                $query .= " WHERE ";
                $query .= "         T1.YEAR = '".CTRL_YEAR."' ";
                                //結核所見コードが「00:未受験」でなく、登録されている
                $query .= "     AND (   (CASE WHEN T4.TB_REMARKCD =  '00' THEN 0 ";
                $query .= "                   WHEN T4.TB_REMARKCD IS NULL THEN 0 ";
                $query .= "                   ELSE 1 END) = 1) ";
                $query .= " ), CNT9_1 AS ( ";
                //受検者数
                $query .= " SELECT ";
                $query .= "     YEAR, ";
                $query .= "     AGE, ";
                $query .= "     SEX, ";
                $query .= "     NVL(SUM(REC_CNT), 0) AS CNT ";
                $query .= " FROM ";
                $query .= "     SCHREG_B_DIST_MST9 ";
                $query .= " GROUP BY ";
                $query .= "     YEAR, ";
                $query .= "     SEX, ";
                $query .= "     AGE ";
                $query .= " ), CNT9_2 AS ( ";
                //結核の精密検査の対象者
                $query .= " SELECT ";
                $query .= "     YEAR, ";
                $query .= "     AGE, ";
                $query .= "     SEX, ";
                $query .= "     NVL(SUM(REC_CNT), 0) AS CNT ";
                $query .= " FROM ";
                $query .= "     SCHREG_B_DIST_MST9 ";
                $query .= " WHERE ";
                $query .= "     TB_REMARKCD = '02' ";    //「02:要精密検査」
                $query .= " GROUP BY ";
                $query .= "     YEAR, ";
                $query .= "     SEX, ";
                $query .= "     AGE ";
                $query .= " ), CNT9_3 AS ( ";
                //結核
                $query .= " SELECT ";
                $query .= "     YEAR, ";
                $query .= "     AGE, ";
                $query .= "     SEX, ";
                $query .= "     NVL(SUM(REC_CNT), 0) AS CNT ";
                $query .= " FROM ";
                $query .= "     SCHREG_B_DIST_MST9 ";
                $query .= " WHERE ";
                                             //「05:C2」「06:C1」「08:B2」「09:B1」「11:A2」「12:A1」
                $query .= "     TB_ADVISECD IN ('05', '06', '08', '09', '11', '12') ";
                $query .= " GROUP BY ";
                $query .= "     YEAR, ";
                $query .= "     SEX, ";
                $query .= "     AGE ";
                $query .= " ) ";
                //CSVに出力するデータ
                //受検者数
                $query .= " SELECT ";
                $query .= "     1 AS SORT, ";
                $query .= "     '受検者数(人)' AS ROWHEADER ";
                $query .= knjf074aQuery::createPrintRowShippeiCSV();
                $query .= " FROM ";
                $query .= " CNT9_1 ";
                $query .= " UNION ";
                //結核の精密検査の対象者
                $query .= " SELECT ";
                $query .= "     2 AS SORT, ";
                $query .= "     '結核の精密検査の対象者' AS ROWHEADER ";
                $query .= knjf074aQuery::createPrintRowShippeiCSV();
                $query .= " FROM ";
                $query .= " CNT9_2 ";
                $query .= " UNION ";
                //結核
                $query .= " SELECT ";
                $query .= "     3 AS SORT, ";
                $query .= "     '結核' AS ROWHEADER ";
                $query .= knjf074aQuery::createPrintRowShippeiCSV();
                $query .= " FROM ";
                $query .= " CNT9_3 ";
                $query .= " ORDER BY ";
                $query .= " SORT ";
                break;

            case 10:
                $query .= " WITH SCHREG_B_DIST_MST10 AS ( ";
                $query .= " SELECT DISTINCT ";
                $query .= "     T1.YEAR, ";
                $query .= "     T1.SCHREGNO, ";
                $query .= "     T2.SEX, ";
                $query .= "     YEAR('".CTRL_YEAR."-04-01' - DATE(T2.BIRTHDAY)) AS AGE, ";
                $query .= "     1 AS REC_CNT, ";
                $query .= "     T4.HEARTDISEASECD ";
                $query .= " FROM ";
                $query .= "           SCHREG_REGD_DAT T1 ";
                $query .= " LEFT JOIN SCHREG_BASE_MST T2 ";
                $query .= "        ON T2.SCHREGNO = T1.SCHREGNO ";
                $query .= " LEFT JOIN SCHREG_REGD_GDAT T3 ";
                $query .= "        ON T3.YEAR     = T1.YEAR ";
                $query .= "       AND T3.GRADE    = T1.GRADE ";
                $query .= " LEFT JOIN MEDEXAM_DET_DAT T4 ";
                $query .= "        ON T4.YEAR     = T1.YEAR ";
                $query .= "       AND T4.SCHREGNO = T1.SCHREGNO ";
                $query .= " WHERE ";
                $query .= "         T1.YEAR = '".CTRL_YEAR."' ";
                                //心臓疾患異常コードが「00:未受験」でなく、登録されている
                $query .= "     AND (   (CASE WHEN T4.HEARTDISEASECD =  '00' THEN 0 ";
                $query .= "                   WHEN T4.HEARTDISEASECD IS NULL THEN 0 ";
                $query .= "                   ELSE 1 END) = 1) ";
                $query .= " ), CNT10_1 AS ( ";
                //受検者数
                $query .= " SELECT ";
                $query .= "     YEAR, ";
                $query .= "     AGE, ";
                $query .= "     SEX, ";
                $query .= "     NVL(SUM(REC_CNT), 0) AS CNT ";
                $query .= " FROM ";
                $query .= "     SCHREG_B_DIST_MST10 ";
                $query .= " GROUP BY ";
                $query .= "     YEAR, ";
                $query .= "     SEX, ";
                $query .= "     AGE ";
                $query .= " ), CNT10_2 AS ( ";
                //心臓疾患・異常
                $query .= " SELECT ";
                $query .= "     YEAR, ";
                $query .= "     AGE, ";
                $query .= "     SEX, ";
                $query .= "     NVL(SUM(REC_CNT), 0) AS CNT ";
                $query .= " FROM ";
                $query .= "     SCHREG_B_DIST_MST10 ";
                $query .= " WHERE ";
                $query .= "     HEARTDISEASECD <> '01' ";    //「01:異常なし」
                $query .= " GROUP BY ";
                $query .= "     YEAR, ";
                $query .= "     SEX, ";
                $query .= "     AGE ";
                $query .= " ) ";
                //CSVに出力するデータ
                //受検者数
                $query .= " SELECT ";
                $query .= "     1 AS SORT, ";
                $query .= "     '受検者数(人)' AS ROWHEADER ";
                $query .= knjf074aQuery::createPrintRowShippeiCSV();
                $query .= " FROM ";
                $query .= " CNT10_1 ";
                $query .= " UNION ";
                //心臓疾患・異常
                $query .= " SELECT ";
                $query .= "     2 AS SORT, ";
                $query .= "     '心臓疾患・異常' AS ROWHEADER ";
                $query .= knjf074aQuery::createPrintRowShippeiCSV();
                $query .= " FROM ";
                $query .= " CNT10_2 ";
                $query .= " ORDER BY ";
                $query .= " SORT ";
                break;

            case 11:
                $query .= " WITH SCHREG_B_DIST_MST11 AS ( ";
                $query .= " SELECT DISTINCT ";
                $query .= "     T1.YEAR, ";
                $query .= "     T1.SCHREGNO, ";
                $query .= "     T2.SEX, ";
                $query .= "     YEAR('".CTRL_YEAR."-04-01' - DATE(T2.BIRTHDAY)) AS AGE, ";
                $query .= "     1 AS REC_CNT, ";
                $query .= "     T4.HEART_MEDEXAM ";
                $query .= " FROM ";
                $query .= "           SCHREG_REGD_DAT T1 ";
                $query .= " LEFT JOIN SCHREG_BASE_MST T2 ";
                $query .= "        ON T2.SCHREGNO = T1.SCHREGNO ";
                $query .= " LEFT JOIN SCHREG_REGD_GDAT T3 ";
                $query .= "        ON T3.YEAR     = T1.YEAR ";
                $query .= "       AND T3.GRADE    = T1.GRADE ";
                $query .= " LEFT JOIN MEDEXAM_DET_DAT T4 ";
                $query .= "        ON T4.YEAR     = T1.YEAR ";
                $query .= "       AND T4.SCHREGNO = T1.SCHREGNO ";
                $query .= " WHERE ";
                $query .= "         T1.YEAR = '".CTRL_YEAR."' ";
                                //心電図結果コードが「00:未受験」でなく、登録されている
                $query .= "     AND (   (CASE WHEN T4.HEART_MEDEXAM =  '00' THEN 0 ";
                $query .= "                   WHEN T4.HEART_MEDEXAM IS NULL THEN 0 ";
                $query .= "                   ELSE 1 END) = 1) ";
                $query .= " ), CNT11_1 AS ( ";
                //受検者数
                $query .= " SELECT ";
                $query .= "     YEAR, ";
                $query .= "     AGE, ";
                $query .= "     SEX, ";
                $query .= "     NVL(SUM(REC_CNT), 0) AS CNT ";
                $query .= " FROM ";
                $query .= "     SCHREG_B_DIST_MST11 ";
                $query .= " GROUP BY ";
                $query .= "     YEAR, ";
                $query .= "     SEX, ";
                $query .= "     AGE ";
                $query .= " ), CNT11_2 AS ( ";
                //心電図異常
                $query .= " SELECT ";
                $query .= "     YEAR, ";
                $query .= "     AGE, ";
                $query .= "     SEX, ";
                $query .= "     NVL(SUM(REC_CNT), 0) AS CNT ";
                $query .= " FROM ";
                $query .= "     SCHREG_B_DIST_MST11 ";
                $query .= " WHERE ";
                $query .= "     HEART_MEDEXAM IN ('02', '04') ";    //「02:要精査」「04:主治医管理」
                $query .= " GROUP BY ";
                $query .= "     YEAR, ";
                $query .= "     SEX, ";
                $query .= "     AGE ";
                $query .= " ) ";
                //CSVに出力するデータ
                //受検者数
                $query .= " SELECT ";
                $query .= "     1 AS SORT, ";
                $query .= "     '受検者数(人)' AS ROWHEADER ";
                $query .= knjf074aQuery::createPrintRowShippeiCSV();
                $query .= " FROM ";
                $query .= " CNT11_1 ";
                $query .= " UNION ";
                //心電図異常
                $query .= " SELECT ";
                $query .= "     2 AS SORT, ";
                $query .= "     '心電図異常' AS ROWHEADER ";
                $query .= knjf074aQuery::createPrintRowShippeiCSV();
                $query .= " FROM ";
                $query .= " CNT11_2 ";
                $query .= " ORDER BY ";
                $query .= " SORT ";
                break;

            case 12:
                $query .= " WITH SCHREG_B_DIST_MST12 AS ( ";
                $query .= " SELECT DISTINCT ";
                $query .= "     T1.YEAR, ";
                $query .= "     T1.SCHREGNO, ";
                $query .= "     T2.SEX, ";
                $query .= "     YEAR('".CTRL_YEAR."-04-01' - DATE(T2.BIRTHDAY)) AS AGE, ";
                $query .= "     1 AS REC_CNT, ";
                $query .= "     T4.ALBUMINURIA1CD, ";
                $query .= "     T4.URICSUGAR1CD ";
                $query .= " FROM ";
                $query .= "           SCHREG_REGD_DAT T1 ";
                $query .= " LEFT JOIN SCHREG_BASE_MST T2 ";
                $query .= "        ON T2.SCHREGNO = T1.SCHREGNO ";
                $query .= " LEFT JOIN SCHREG_REGD_GDAT T3 ";
                $query .= "        ON T3.YEAR     = T1.YEAR ";
                $query .= "       AND T3.GRADE    = T1.GRADE ";
                $query .= " LEFT JOIN MEDEXAM_DET_DAT T4 ";
                $query .= "        ON T4.YEAR     = T1.YEAR ";
                $query .= "       AND T4.SCHREGNO = T1.SCHREGNO ";
                $query .= " WHERE ";
                $query .= "         T1.YEAR = '".CTRL_YEAR."' ";
                                //1次尿検査の結果コードが「00:未受験」でなく、登録されている
                $query .= "     AND (   ALBUMINURIA1CD <> '00' ";
                $query .= "          OR URICSUGAR1CD   <> '00' ";
                $query .= "          OR URICBLEED1CD   <> '00') ";
                $query .= " ), CNT12_1 AS ( ";
                //受検者数
                $query .= " SELECT ";
                $query .= "     YEAR, ";
                $query .= "     AGE, ";
                $query .= "     SEX, ";
                $query .= "     NVL(SUM(REC_CNT), 0) AS CNT ";
                $query .= " FROM ";
                $query .= "     SCHREG_B_DIST_MST12 ";
                $query .= " GROUP BY ";
                $query .= "     YEAR, ";
                $query .= "     SEX, ";
                $query .= "     AGE ";
                $query .= " ), CNT12_2 AS ( ";
                //蛋白検出
                $query .= " SELECT ";
                $query .= "     YEAR, ";
                $query .= "     AGE, ";
                $query .= "     SEX, ";
                $query .= "     NVL(SUM(REC_CNT), 0) AS CNT ";
                $query .= " FROM ";
                $query .= "     SCHREG_B_DIST_MST12 ";
                $query .= " WHERE ";
                $query .= "     ALBUMINURIA1CD IN ('02', '03', '04', '05') ";    //「02:+」～「05:±」
                $query .= " GROUP BY ";
                $query .= "     YEAR, ";
                $query .= "     SEX, ";
                $query .= "     AGE ";
                $query .= " ), CNT12_3 AS ( ";
                //糖検出
                $query .= " SELECT ";
                $query .= "     YEAR, ";
                $query .= "     AGE, ";
                $query .= "     SEX, ";
                $query .= "     NVL(SUM(REC_CNT), 0) AS CNT ";
                $query .= " FROM ";
                $query .= "     SCHREG_B_DIST_MST12 ";
                $query .= " WHERE ";
                $query .= "     URICSUGAR1CD IN ('02', '03', '04', '05') ";    //「02:+」～「05:±」
                $query .= " GROUP BY ";
                $query .= "     YEAR, ";
                $query .= "     SEX, ";
                $query .= "     AGE ";
                $query .= " ) ";
                //CSVに出力するデータ
                //受検者数
                $query .= " SELECT ";
                $query .= "     1 AS SORT, ";
                $query .= "     '受検者数(人)' AS ROWHEADER ";
                $query .= knjf074aQuery::createPrintRowShippeiCSV();
                $query .= " FROM ";
                $query .= " CNT12_1 ";
                $query .= " UNION ";
                //蛋白検出
                $query .= " SELECT ";
                $query .= "     2 AS SORT, ";
                $query .= "     '蛋白検出' AS ROWHEADER ";
                $query .= knjf074aQuery::createPrintRowShippeiCSV();
                $query .= " FROM ";
                $query .= " CNT12_2 ";
                $query .= " UNION ";
                //糖検出
                $query .= " SELECT ";
                $query .= "     3 AS SORT, ";
                $query .= "     '糖検出' AS ROWHEADER ";
                $query .= knjf074aQuery::createPrintRowShippeiCSV();
                $query .= " FROM ";
                $query .= " CNT12_3 ";
                $query .= " ORDER BY ";
                $query .= " SORT ";
                break;

            case 13:
                $query .= " WITH SCHREG_B_DIST_MST13 AS ( ";
                $query .= " SELECT DISTINCT ";
                $query .= "     T1.YEAR, ";
                $query .= "     T1.SCHREGNO, ";
                $query .= "     T2.SEX, ";
                $query .= "     YEAR('".CTRL_YEAR."-04-01' - DATE(T2.BIRTHDAY)) AS AGE, ";
                $query .= "     1 AS REC_CNT, ";
                $query .= "     T4.MEDICAL_HISTORY1, ";
                $query .= "     T4.MEDICAL_HISTORY2, ";
                $query .= "     T4.MEDICAL_HISTORY3 ";
                $query .= " FROM ";
                $query .= "           SCHREG_REGD_DAT T1 ";
                $query .= " LEFT JOIN SCHREG_BASE_MST T2 ";
                $query .= "        ON T2.SCHREGNO = T1.SCHREGNO ";
                $query .= " LEFT JOIN SCHREG_REGD_GDAT T3 ";
                $query .= "        ON T3.YEAR     = T1.YEAR ";
                $query .= "       AND T3.GRADE    = T1.GRADE ";
                $query .= " LEFT JOIN MEDEXAM_DET_DAT T4 ";
                $query .= "        ON T4.YEAR     = T1.YEAR ";
                $query .= "       AND T4.SCHREGNO = T1.SCHREGNO ";
                $query .= " LEFT JOIN MEDEXAM_HDAT T5 ";
                $query .= "        ON T5.YEAR     = T1.YEAR ";
                $query .= "       AND T5.SCHREGNO = T1.SCHREGNO ";
                $query .= " WHERE ";
                $query .= "         T1.YEAR = '".CTRL_YEAR."' ";
                                //健康診断実施日に日付が登録されている
                $query .= "     AND T5.DATE IS NOT NULL ";
                $query .= " ), CNT13_1 AS ( ";
                //受検者数
                $query .= " SELECT ";
                $query .= "     YEAR, ";
                $query .= "     AGE, ";
                $query .= "     SEX, ";
                $query .= "     NVL(SUM(REC_CNT), 0) AS CNT ";
                $query .= " FROM ";
                $query .= "     SCHREG_B_DIST_MST13 ";
                $query .= " GROUP BY ";
                $query .= "     YEAR, ";
                $query .= "     SEX, ";
                $query .= "     AGE ";
                $query .= " ), CNT13_2 AS ( ";
                //食物アレルギー
                $query .= " SELECT ";
                $query .= "     YEAR, ";
                $query .= "     AGE, ";
                $query .= "     SEX, ";
                $query .= "     NVL(SUM(REC_CNT), 0) AS CNT ";
                $query .= " FROM ";
                $query .= "     SCHREG_B_DIST_MST13 ";
                $query .= " WHERE ";
                $query .= "      (   MEDICAL_HISTORY1 = '01' ";    //「01:食物アレルギー」
                $query .= "       OR MEDICAL_HISTORY2 = '01' ";
                $query .= "       OR MEDICAL_HISTORY3 = '01') ";
                $query .= " GROUP BY ";
                $query .= "     YEAR, ";
                $query .= "     SEX, ";
                $query .= "     AGE ";
                $query .= " ), CNT13_3 AS ( ";
                //ぜん息
                $query .= " SELECT ";
                $query .= "     YEAR, ";
                $query .= "     AGE, ";
                $query .= "     SEX, ";
                $query .= "     NVL(SUM(REC_CNT), 0) AS CNT ";
                $query .= " FROM ";
                $query .= "     SCHREG_B_DIST_MST13 ";
                $query .= " WHERE ";
                $query .= "      (   MEDICAL_HISTORY1 = '02' ";    //「02:ぜん息」
                $query .= "       OR MEDICAL_HISTORY2 = '02' ";
                $query .= "       OR MEDICAL_HISTORY3 = '02') ";
                $query .= " GROUP BY ";
                $query .= "     YEAR, ";
                $query .= "     SEX, ";
                $query .= "     AGE ";
                $query .= " ), CNT13_4 AS ( ";
                //腎臓疾患
                $query .= " SELECT ";
                $query .= "     YEAR, ";
                $query .= "     AGE, ";
                $query .= "     SEX, ";
                $query .= "     NVL(SUM(REC_CNT), 0) AS CNT ";
                $query .= " FROM ";
                $query .= "     SCHREG_B_DIST_MST13 ";
                $query .= " WHERE ";
                $query .= "      (   MEDICAL_HISTORY1 = '03' ";    //「03:腎臓疾患」
                $query .= "       OR MEDICAL_HISTORY2 = '03' ";
                $query .= "       OR MEDICAL_HISTORY3 = '03') ";
                $query .= " GROUP BY ";
                $query .= "     YEAR, ";
                $query .= "     SEX, ";
                $query .= "     AGE ";
                $query .= " ), CNT13_5 AS ( ";
                //言語障害
                $query .= " SELECT ";
                $query .= "    YEAR, ";
                $query .= "    AGE, ";
                $query .= "    SEX, ";
                $query .= "    NVL(SUM(REC_CNT), 0) AS CNT ";
                $query .= " FROM ";
                $query .= "    SCHREG_B_DIST_MST13 ";
                $query .= " WHERE ";
                $query .= "      (   MEDICAL_HISTORY1 = '04' ";    //「04:言語障害」
                $query .= "       OR MEDICAL_HISTORY2 = '04' ";
                $query .= "       OR MEDICAL_HISTORY3 = '04') ";
                $query .= " GROUP BY ";
                $query .= "     YEAR, ";
                $query .= "     SEX, ";
                $query .= "     AGE ";
                $query .= " ), CNT13_6 AS ( ";
                //その他の疾患・異常
                $query .= " SELECT ";
                $query .= "     YEAR, ";
                $query .= "     AGE, ";
                $query .= "     SEX, ";
                $query .= "     NVL(SUM(REC_CNT), 0) AS CNT ";
                $query .= " FROM ";
                $query .= "     SCHREG_B_DIST_MST13 ";
                $query .= " WHERE ";
                $query .= "      (   MEDICAL_HISTORY1 = '05' ";    //「05:その他の疾患・異常」
                $query .= "       OR MEDICAL_HISTORY2 = '05' ";
                $query .= "       OR MEDICAL_HISTORY3 = '05') ";
                $query .= " GROUP BY ";
                $query .= "     YEAR, ";
                $query .= "     SEX, ";
                $query .= "     AGE ";
                $query .= " ) ";
                //CSVに出力するデータ
                //受検者数
                $query .= " SELECT ";
                $query .= "     1 AS SORT, ";
                $query .= "     '受検者数(人)' AS ROWHEADER ";
                $query .= knjf074aQuery::createPrintRowShippeiCSV();
                $query .= " FROM ";
                $query .= " CNT13_1 ";
                $query .= " UNION ";
                //食物アレルギー
                $query .= " SELECT ";
                $query .= "     2 AS SORT, ";
                $query .= "     '食物アレルギー' AS ROWHEADER ";
                $query .= knjf074aQuery::createPrintRowShippeiCSV();
                $query .= " FROM ";
                $query .= " CNT13_2 ";
                $query .= " UNION ";
                //ぜん息
                $query .= " SELECT ";
                $query .= "     3 AS SORT, ";
                $query .= "     'ぜん息' AS ROWHEADER ";
                $query .= knjf074aQuery::createPrintRowShippeiCSV();
                $query .= " FROM ";
                $query .= " CNT13_3 ";
                $query .= " UNION ";
                //腎臓疾患
                $query .= " SELECT ";
                $query .= "     4 AS SORT, ";
                $query .= "     '腎臓疾患' AS ROWHEADER ";
                $query .= knjf074aQuery::createPrintRowShippeiCSV();
                $query .= " FROM ";
                $query .= " CNT13_4 ";
                $query .= " UNION ";
                //言語障害
                $query .= " SELECT ";
                $query .= "     5 AS SORT, ";
                $query .= "     '言語障害' AS ROWHEADER ";
                $query .= knjf074aQuery::createPrintRowShippeiCSV();
                $query .= " FROM ";
                $query .= " CNT13_5 ";
                $query .= " UNION ";
                //その他の疾患・異常
                $query .= " SELECT ";
                $query .= "     6 AS SORT, ";
                $query .= "     'その他の疾患・異常' AS ROWHEADER ";
                $query .= knjf074aQuery::createPrintRowShippeiCSV();
                $query .= " FROM ";
                $query .= " CNT13_6 ";
                $query .= " ORDER BY ";
                $query .= " SORT ";
                break;

            case 14:
                $query .= " WITH SCHREG_B_DIST_MST14 AS ( ";
                $query .= " SELECT DISTINCT ";
                $query .= "     T1.YEAR, ";
                $query .= "     T1.SCHREGNO, ";
                $query .= "     T2.SEX, ";
                $query .= "     YEAR('".CTRL_YEAR."-04-01' - DATE(T2.BIRTHDAY)) AS AGE, ";
                $query .= "     1 AS REC_CNT, ";
                $query .= "     T4.MEDICAL_HISTORY1, ";
                $query .= "     T4.MEDICAL_HISTORY2, ";
                $query .= "     T4.MEDICAL_HISTORY3, ";
                $query .= "     T6.LOSTADULTTOOTH, ";
                $query .= "     T6.TREATEDADULTTOOTH, ";
                $query .= "     T6.REMAINADULTTOOTH ";
                $query .= " FROM ";
                $query .= "           SCHREG_REGD_DAT T1 ";
                $query .= " LEFT JOIN SCHREG_BASE_MST T2 ";
                $query .= "        ON T2.SCHREGNO = T1.SCHREGNO ";
                $query .= " LEFT JOIN SCHREG_REGD_GDAT T3 ";
                $query .= "        ON T3.YEAR     = T1.YEAR ";
                $query .= "       AND T3.GRADE    = T1.GRADE ";
                $query .= " LEFT JOIN MEDEXAM_DET_DAT T4 ";
                $query .= "        ON T4.YEAR     = T1.YEAR ";
                $query .= "       AND T4.SCHREGNO = T1.SCHREGNO ";
                $query .= " LEFT JOIN MEDEXAM_HDAT T5 ";
                $query .= "        ON T5.YEAR     = T1.YEAR ";
                $query .= "       AND T5.SCHREGNO = T1.SCHREGNO ";
                $query .= " LEFT JOIN MEDEXAM_TOOTH_DAT T6 ";
                $query .= "        ON T6.YEAR     = T1.YEAR ";
                $query .= "       AND T6.SCHREGNO = T1.SCHREGNO ";
                $query .= " WHERE ";
                $query .= "         T1.YEAR = '".CTRL_YEAR."' ";
                                //歯科検診の実施日が登録されている
                $query .= "     AND T5.TOOTH_DATE IS NOT NULL ";
                $query .= " ), CNT14_1 AS ( ";
                //受検者数
                $query .= " SELECT ";
                $query .= "     YEAR, ";
                $query .= "     AGE, ";
                $query .= "     SEX, ";
                $query .= "     NVL(SUM(REC_CNT), 0) AS CNT ";
                $query .= " FROM ";
                $query .= "     SCHREG_B_DIST_MST14 ";
                $query .= " GROUP BY ";
                $query .= "     YEAR, ";
                $query .= "     SEX, ";
                $query .= "     AGE ";
                $query .= " ), CNT14_3 AS ( ";
                //永久歯のう歯等数／喪失歯数(本)
                $query .= " SELECT ";
                $query .= "     YEAR, ";
                $query .= "     AGE, ";
                $query .= "     SEX, ";
                $query .= "     NVL(SUM(LOSTADULTTOOTH), 0) AS CNT ";    //永久歯喪失本数
                $query .= " FROM ";
                $query .= "     SCHREG_B_DIST_MST14 ";
                $query .= " GROUP BY ";
                $query .= "     YEAR, ";
                $query .= "     SEX, ";
                $query .= "     AGE ";
                $query .= " ), CNT14_4 AS ( ";
                //永久歯のう歯等数／う歯／処置歯数(本)
                $query .= " SELECT ";
                $query .= "     YEAR, ";
                $query .= "     AGE, ";
                $query .= "     SEX, ";
                $query .= "     NVL(SUM(TREATEDADULTTOOTH), 0) AS CNT ";    //永久歯処置数
                $query .= " FROM ";
                $query .= "     SCHREG_B_DIST_MST14 ";
                $query .= " GROUP BY ";
                $query .= "     YEAR, ";
                $query .= "     SEX, ";
                $query .= "     AGE ";
                $query .= " ), CNT14_5 AS ( ";
                //永久歯のう歯等数／う歯／未処置歯数(本)
                $query .= " SELECT ";
                $query .= "     YEAR, ";
                $query .= "     AGE, ";
                $query .= "     SEX, ";
                $query .= "     NVL(SUM(REMAINADULTTOOTH), 0) AS CNT ";    //永久歯未処置数
                $query .= " FROM ";
                $query .= "     SCHREG_B_DIST_MST14 ";
                $query .= " GROUP BY ";
                $query .= "     YEAR, ";
                $query .= "     SEX, ";
                $query .= "     AGE ";
                $query .= " ), CNT14_2 AS ( ";
                //永久歯のう歯等数／計〔ＤＭＦ〕(本）
                $query .= " SELECT ";
                $query .= "     CNT14_3.YEAR, ";
                $query .= "     CNT14_3.AGE, ";
                $query .= "     CNT14_3.SEX, ";
                                //永久歯喪失本数 + 永久歯処置数 + 永久歯未処置数
                $query .= "     NVL(SUM(CNT14_3.CNT), 0) + NVL(SUM(CNT14_4.CNT), 0) + NVL(SUM(CNT14_5.CNT), 0) AS CNT ";
                $query .= " FROM ";
                $query .= "            CNT14_3 ";
                $query .= " INNER JOIN CNT14_4 ";
                $query .= "         ON CNT14_3.YEAR = CNT14_4.YEAR ";
                $query .= "        AND CNT14_3.AGE  = CNT14_4.AGE ";
                $query .= "        AND CNT14_3.SEX  = CNT14_4.SEX ";
                $query .= " INNER JOIN CNT14_5 ";
                $query .= "         ON CNT14_3.YEAR = CNT14_5.YEAR ";
                $query .= "        AND CNT14_3.AGE  = CNT14_5.AGE ";
                $query .= "        AND CNT14_3.SEX  = CNT14_5.SEX ";
                $query .= " GROUP BY ";
                $query .= "     CNT14_3.YEAR, ";
                $query .= "     CNT14_3.SEX, ";
                $query .= "     CNT14_3.AGE ";
                $query .= " ) ";
                //CSVに出力するデータ
                //受検者数
                $query .= " SELECT ";
                $query .= "     1 AS SORT, ";
                $query .= "     '受検者数(人)' AS ROWHEADER ";
                $query .= knjf074aQuery::createPrintRowShippeiCSV();
                $query .= " FROM ";
                $query .= " CNT14_1 ";
                $query .= " UNION ";
                //永久歯のう歯等数／計〔ＤＭＦ〕(本）
                $query .= " SELECT ";
                $query .= "     2 AS SORT, ";
                $query .= "     '永久歯のう歯等数／計〔ＤＭＦ〕(本）' AS ROWHEADER ";
                $query .= knjf074aQuery::createPrintRowShippeiCSV();
                $query .= " FROM ";
                $query .= " CNT14_2 ";
                $query .= " UNION ";
                //永久歯のう歯等数／喪失歯数(本)
                $query .= " SELECT ";
                $query .= "     3 AS SORT, ";
                $query .= "     '永久歯のう歯等数／喪失歯数(本)' AS ROWHEADER ";
                $query .= knjf074aQuery::createPrintRowShippeiCSV();
                $query .= " FROM ";
                $query .= " CNT14_3 ";
                $query .= " UNION ";
                //永久歯のう歯等数／う歯／処置歯数(本)
                $query .= " SELECT ";
                $query .= "     4 AS SORT, ";
                $query .= "     '永久歯のう歯等数／う歯／処置歯数(本)' AS ROWHEADER ";
                $query .= knjf074aQuery::createPrintRowShippeiCSV();
                $query .= " FROM ";
                $query .= " CNT14_4 ";
                $query .= " UNION ";
                //永久歯のう歯等数／う歯／未処置歯数(本)
                $query .= " SELECT ";
                $query .= "     5 AS SORT, ";
                $query .= "     '永久歯のう歯等数／う歯／未処置歯数(本)' AS ROWHEADER ";
                $query .= knjf074aQuery::createPrintRowShippeiCSV();
                $query .= " FROM ";
                $query .= " CNT14_5 ";
                $query .= " ORDER BY ";
                $query .= " SORT ";
                break;
        }

        return $query;
    }
}
