<?php

require_once('for_php7.php');


class knja228aQuery extends Query
{
    //クラス一覧取得
    public function getHrClassList($model)
    {
        $query  = "";

        $query .= " SELECT GRADE || HR_CLASS AS VALUE ";
        $query .= "      , HR_NAME           AS LABEL ";
        $query .= "   FROM SCHREG_REGD_HDAT T1 ";
        $query .= "  WHERE YEAR     = '".CTRL_YEAR."' ";
        $query .= "    AND SEMESTER = '".CTRL_SEMESTER."' ";
        //参照・更新可(制限付き)
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= " AND (   TR_CD1    = '".STAFFCD."' ";
            $query .= "      OR TR_CD2    = '".STAFFCD."' ";
            $query .= "      OR TR_CD3    = '".STAFFCD."' ";
            $query .= "      OR SUBTR_CD1 = '".STAFFCD."' ";
            $query .= "      OR SUBTR_CD2 = '".STAFFCD."' ";
            $query .= "      OR SUBTR_CD3 = '".STAFFCD."') ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T1.GRADE IN (";
                $query .= "         SELECT G1.GRADE ";
                $query .= "           FROM SCHREG_REGD_GDAT G1 ";
                $query .= "          WHERE G1.YEAR        = T1.YEAR ";
                $query .= "            AND G1.SCHOOL_KIND IN ('". implode(explode(':', $model->selectSchoolKind), "','"). "') ";
                $query .= "     ) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.GRADE IN ( ";
            $query .= "         SELECT G1.GRADE ";
            $query .= "           FROM SCHREG_REGD_GDAT G1 ";
            $query .= "          WHERE G1.YEAR        = T1.YEAR ";
            $query .= "            AND G1.SCHOOL_KIND = '".SCHOOLKIND."' ";
            $query .= "     ) ";
        }
        $query .= "  ORDER BY VALUE ";

        return $query;
    }

    //生徒データ
    public function getSchno($model)
    {
        $query  = "";
        $query .= "   SELECT T2.SCHREGNO ";
        $query .= "        , T2.ATTENDNO ";
        $query .= "        , T1.NAME_SHOW ";
        $query .= "        , T2.GRADE ";
        $query .= "        , T2.HR_CLASS ";
        $query .= "        , T3.HR_NAME ";
        $query .= "     FROM SCHREG_BASE_MST  T1 ";
        $query .= "        , SCHREG_REGD_DAT  T2 ";
        $query .= "        , SCHREG_REGD_HDAT T3 ";
        $query .= "    WHERE T2.YEAR                 = '".CTRL_YEAR."' ";
        $query .= "      AND T2.SEMESTER             = '".CTRL_SEMESTER."' ";
        $query .= "      AND T2.GRADE || T2.HR_CLASS = '{$model->field["GRADE_HR_CLASS"]}'";
        $query .= "      AND T2.SCHREGNO             = T1.SCHREGNO ";
        $query .= "      AND T2.YEAR                 = T3.YEAR ";
        $query .= "      AND T2.SEMESTER             = T3.SEMESTER ";
        $query .= "      AND T2.GRADE                = T3.GRADE ";
        $query .= "      AND T2.HR_CLASS             = T3.HR_CLASS ";
        $query .= " ORDER BY T2.ATTENDNO ";

        return $query;
    }

    //名称マスタ
    public function getSchoolName()
    {
        $query  = "";
        $query .= "SELECT NAME1 ";
        $query .= "  FROM NAME_MST ";
        $query .= " WHERE NAMECD1 = 'Z010'";
        $query .= "   AND NAMECD2 = '00' ";

        return $query;
    }

    //(CSVデータ取得用)選択されたクラスの文字列から学籍番号を取得するSQL
    public function getCsvTargetRecRegNoInClass($model, $classCds)
    {
        //IN句用文字列作成
        $condWhereIn = '';
        foreach ($classCds as $v) {
            if ($condWhereIn != '') {
                $condWhereIn = $condWhereIn . ', ';
            }
            $condWhereIn .= "'{$v}'";
        }

        //さらに外側をカッコで囲む
        $condWhereIn = "(" . $condWhereIn . ")";

        $query  = "";
        $query .= "    SELECT T1.SCHREGNO";
        $query .= "      FROM SCHREG_REGD_DAT T1";
        $query .= " LEFT JOIN SCHREG_BASE_MST T2";
        $query .= "        ON (T2.SCHREGNO = T1.SCHREGNO)";
        $query .= "     WHERE T1.YEAR     = '".CTRL_YEAR."'";
        $query .= "       AND T1.SEMESTER = '".CTRL_SEMESTER."'";
        $query .= "       AND T1.GRADE || T1.HR_CLASS IN {$condWhereIn}";

        return $query;
    }

    //(CSVデータ取得用)基本データ
    public function getCsvBasicData($model, $recRegNo)
    {
        $query  = "";
        $query .= " WITH SRD AS ( ";
        $query .= "     SELECT SCHREGNO ";
        $query .= "          , YEAR ";
        $query .= "          , SEMESTER ";
        $query .= "          , GRADE ";
        $query .= "          , HR_CLASS ";
        $query .= "          , ATTENDNO ";
        $query .= "          , COURSECD ";
        $query .= "          , MAJORCD ";
        $query .= "       FROM SCHREG_REGD_DAT ";
        $query .= "      WHERE SCHREGNO = '{$recRegNo}' ";
        $query .= " ) ";
        $query .= "    SELECT SRD.SCHREGNO ";      //学籍番号
        $query .= "         , SRD.YEAR ";          //年度(西暦)
        $query .= "         , SRD.SEMESTER ";      //学期
        $query .= "         , CASE WHEN SRD.GRADE = '01' THEN '1年' ";
        $query .= "                WHEN SRD.GRADE = '02' THEN '2年' ";
        $query .= "                WHEN SRD.GRADE = '03' THEN '3年' ";
        $query .= "                ELSE NULL ";
        $query .= "           END AS GRADE_NAME "; //学年
        $query .= "         , SRH.HR_CLASS_NAME2 ";//組
        $query .= "         , SRD.ATTENDNO ";      //番号
        $query .= "         , MAJOR_M.MAJORNAME "; //学科
        $query .= "         , STAFF_M.STAFFNAME "; //担任
        $query .= "      FROM SRD ";
        $query .= " LEFT JOIN SCHREG_REGD_HDAT SRH ";
        $query .= "        ON (SRH.YEAR     = SRD.YEAR ";
        $query .= "       AND  SRH.SEMESTER = SRD.SEMESTER ";
        $query .= "       AND  SRH.GRADE    = SRD.GRADE ";
        $query .= "       AND  SRH.HR_CLASS = SRD.HR_CLASS) ";
        $query .= " LEFT JOIN MAJOR_MST MAJOR_M ";
        $query .= "        ON (MAJOR_M.COURSECD = SRD.COURSECD ";
        $query .= "       AND  MAJOR_M.MAJORCD  = SRD.MAJORCD) ";
        $query .= " LEFT JOIN STAFF_CLASS_HIST_DAT SCHD ";
        $query .= "        ON (SCHD.YEAR     = SRD.YEAR ";
        $query .= "       AND  SCHD.SEMESTER = SRD.SEMESTER ";
        $query .= "       AND  SCHD.GRADE    = SRD.GRADE ";
        $query .= "       AND  SCHD.HR_CLASS = SRD.HR_CLASS ";
        $query .= "       AND  SCHD.TR_DIV   = '1') ";
        $query .= " LEFT JOIN STAFF_MST STAFF_M ";
        $query .= "        ON (STAFF_M.STAFFCD = SCHD.STAFFCD) ";
        $query .= "  GROUP BY SRD.SCHREGNO ";
        $query .= "         , SRD.YEAR ";
        $query .= "         , SRD.SEMESTER ";
        $query .= "         , SRD.GRADE ";
        $query .= "         , SRH.HR_CLASS_NAME2 ";
        $query .= "         , SRD.ATTENDNO ";
        $query .= "         , MAJOR_M.MAJORNAME ";
        $query .= "         , STAFF_M.STAFFNAME ";
        $query .= "  ORDER BY SRD.SCHREGNO ";
        $query .= "         , SRD.YEAR ";
        $query .= "         , SRD.SEMESTER ";

        return $query;
    }

    //(CSVデータ取得用)基本データ
    public function getCsvStudentBasicData($model, $recRegNo)
    {
        $query  = "";
        $query .= " WITH SCHREG_ADDRESS_LAST AS ( ";
        $query .= "        SELECT SAD.SCHREGNO ";
        $query .= "             , MAX(SAD.ISSUEDATE) AS ISSUEDATE ";
        $query .= "          FROM SCHREG_ADDRESS_DAT SAD ";
        $query .= "         WHERE SAD.SCHREGNO = '{$recRegNo}' ";
        $query .= "      GROUP BY SAD.SCHREGNO ";
        $query .= " ) ";
        $query .= "    SELECT SB_M.NAME_KANA ";     //氏名かな
        $query .= "         , SB_M.NAME ";          //氏名
        $query .= "         , SB_M.SCHREGNO ";      //学籍番号
        $query .= "         , CASE WHEN SB_M.SEX = '1' THEN '男' ";
        $query .= "                WHEN SB_M.SEX = '2' THEN '女' ";
        $query .= "                ELSE NULL ";
        $query .= "           END AS SEX_NAME ";    //性別
        $query .= "         , SB_M.BIRTHDAY ";      //生年月日
        $query .= "         , SAD.ADDR1 ";          //住所1
        $query .= "         , SAD.ADDR2 ";          //住所2
        $query .= "         , SED.RESIDENTCD ";
        $query .= "         , NM.NAME1 AS RESIDENT_NAME ";//住居区分
        $query .= "         , NM2.NAME1 AS AREA_NAME ";   //居住区域
        $query .= "         , SAD.TELNO ";          //電話番号
        $query .= "         , SAD.FAXNO ";          //FAX番号
        $query .= "         , SAD.TELNO2 ";         //携帯番号
        $query .= "         , SAD.EMAIL ";          //メールアドレス
        $query .= "         , CASE WHEN SED.COMMUTE_HOURS IS NULL        THEN '' ";
        $query .= "                WHEN TO_NUMBER(SED.COMMUTE_HOURS) > 0 THEN TO_NUMBER(SED.COMMUTE_HOURS) || '時間' ";
        $query .= "                ELSE '' ";
        $query .= "           END ";
        $query .= "           || ";
        $query .= "           CASE WHEN SED.COMMUTE_MINUTES IS NULL THEN '' ";
        $query .= "                ELSE TO_NUMBER(SED.COMMUTE_MINUTES) || '分' ";
        $query .= "           END AS COMMUTE_TIME ";                //通学時間 ";
        $query .= "         , NM3.NAME1 AS HOWTOCOMMUTE_NAME ";     //通学方法
        $query .= "         , FM.FINSCHOOL_NAME AS FINSCHOOL_NAME ";//出身校
        $query .= "         , NM4.NAME1 AS ENT_DIV_NAME ";          //入学区分
        $query .= "         , SED.PLANUNIV ";       //進路希望(進学)
        $query .= "         , SED.PLANJOB ";        //進路希望(就職)
        $query .= "      FROM SCHREG_ADDRESS_LAST SAL ";
        $query .= "      JOIN SCHREG_BASE_MST SB_M ";
        $query .= "        ON (SB_M.SCHREGNO  = SAL.SCHREGNO) ";
        $query .= " LEFT JOIN SCHREG_ADDRESS_DAT SAD ";
        $query .= "        ON (SAD.SCHREGNO  = SAL.SCHREGNO ";
        $query .= "       AND  SAD.ISSUEDATE = SAL.ISSUEDATE) ";
        $query .= " LEFT JOIN SCHREG_ENVIR_DAT SED ";
        $query .= "        ON (SED.SCHREGNO  = SB_M.SCHREGNO) ";
        $query .= " LEFT JOIN NAME_MST NM ";
        $query .= "        ON (NM.NAMECD1 = 'H108' ";
        $query .= "       AND  NM.NAMECD2 = SED.RESIDENTCD) ";
        $query .= " LEFT JOIN NAME_MST NM2 ";
        $query .= "        ON (NM2.NAMECD1 = 'A020' ";
        $query .= "       AND  NM2.NAMECD2 = SAD.AREACD) ";
        $query .= " LEFT JOIN NAME_MST NM3 ";
        $query .= "        ON (NM3.NAMECD1 = 'H100' ";
        $query .= "       AND  NM3.NAMECD2 = SED.HOWTOCOMMUTECD) ";
        $query .= " LEFT JOIN NAME_MST NM4 ";
        $query .= "        ON (NM4.NAMECD1 = 'A002' ";
        $query .= "       AND  NM4.NAMECD2 = SB_M.ENT_DIV) ";
        $query .= " LEFT JOIN FINSCHOOL_MST FM ";
        $query .= "        ON (FM.FINSCHOOLCD = SB_M.FINSCHOOLCD) ";

        return $query;
    }

    //(CSVデータ取得用)留学情報
    public function getCsvTransferData($model, $recRegNo)
    {
        $query  = "";
        $query .= "   SELECT STD.SCHREGNO ";      //学籍番号
        $query .= "        , STD.TRANSFER_SDATE ";//留学開始日
        $query .= "        , STD.TRANSFER_EDATE ";//留学終了日
        $query .= "        , STD.TRANSFERPLACE "; //留学場所
        $query .= "     FROM SCHREG_TRANSFER_DAT STD ";
        $query .= "    WHERE STD.TRANSFERCD = '1' ";
        $query .= "      AND STD.SCHREGNO = '{$recRegNo}' ";
        $query .= " ORDER BY STD.TRANSFER_SDATE DESC ";

        return $query;
    }

    //(CSVデータ取得用)部活動情報
    public function getCsvClubData($model, $recRegNo)
    {
        $query  = "";
        $query .= " WITH ";
        $query .= " SRD AS ( ";
        $query .= "       SELECT SCHREGNO ";
        $query .= "            , YEAR ";
        $query .= "            , GRADE ";
        $query .= "         FROM SCHREG_REGD_DAT ";
        $query .= "        WHERE SCHREGNO = '{$recRegNo}' ";
        $query .= "     GROUP BY SCHREGNO ";
        $query .= "            , YEAR ";
        $query .= "            , GRADE ";
        $query .= " ) ";
        $query .= " , ";
        $query .= " SCHD AS ( ";
        $query .= "     SELECT CASE WHEN MONTH(SCHREG_CLUB_HIST_DAT.SDATE) < 4 THEN YEAR(SCHREG_CLUB_HIST_DAT.SDATE)-1 ";
        $query .= "                 ELSE YEAR (SCHREG_CLUB_HIST_DAT.SDATE) ";
        $query .= "            END AS SDATE_NENDO ";
        $query .= "          , CASE WHEN MONTH(SCHREG_CLUB_HIST_DAT.EDATE) < 4 THEN YEAR(SCHREG_CLUB_HIST_DAT.EDATE)-1 ";
        $query .= "                 ELSE YEAR (SCHREG_CLUB_HIST_DAT.EDATE) ";
        $query .= "            END AS EDATE_NENDO ";
        $query .= "          , SCHOOLCD ";
        $query .= "          , SCHOOL_KIND ";
        $query .= "          , SCHREGNO ";
        $query .= "          , CLUBCD ";
        $query .= "          , SDATE ";
        $query .= "          , EDATE ";
        $query .= "          , EXECUTIVECD ";
        $query .= "       FROM SCHREG_CLUB_HIST_DAT ";
        $query .= " ) ";//END WITH
        $query .= "    SELECT SRD.SCHREGNO ";     //学籍番号
        $query .= "         , SRD.YEAR ";         //年度(西暦)
        $query .= "         , SRD.GRADE ";        //学年
        $query .= "         , CASE WHEN SRD.GRADE = '01' THEN '1年' ";
        $query .= "                WHEN SRD.GRADE = '02' THEN '2年' ";
        $query .= "                WHEN SRD.GRADE = '03' THEN '3年' ";
        $query .= "                ELSE NULL ";
        $query .= "           END AS GRADE_NAME ";         //学年
        $query .= "         , CM.CLUBNAME ";               //部活動(名称)
        $query .= "         , NM.NAME1 AS EXECUTIVE_NAME ";//役職
        $query .= "         , SCHD.SDATE ";                //入部日
        $query .= "         , CASE WHEN SRD.YEAR = SCHD.EDATE_NENDO THEN SCHD.EDATE ";
        $query .= "                ELSE NULL ";
        $query .= "           END AS EDATE ";     //退部日付
        $query .= "      FROM SRD ";
        $query .= " LEFT JOIN SCHD ";
        $query .= "        ON (SRD.SCHREGNO = SCHD.SCHREGNO ";
        $query .= "       AND  SCHD.SDATE_NENDO <= SRD.YEAR ";
        $query .= "           ) ";
        $query .= " LEFT JOIN CLUB_MST CM ";
        $query .= "        ON CM.CLUBCD = SCHD.CLUBCD ";
        $query .= " LEFT JOIN NAME_MST NM ";
        $query .= "        ON (NM.NAMECD1 = 'J001' ";
        $query .= "       AND  NM.NAMECD2 = SCHD.EXECUTIVECD) ";
        $query .= "     WHERE SCHD.CLUBCD IS NOT NULL ";
        $query .= "  ORDER BY SRD.SCHREGNO ";
        $query .= "         , SRD.YEAR ";
        $query .= "         , SRD.GRADE ";

        return $query;
    }

    //(CSVデータ取得用)委員会・生徒会情報
    public function getCsvCommitteeData($model, $recRegNo)
    {
        $query  = "";
        $query .= " WITH ";
        $query .= " SRD AS ( ";
        $query .= "       SELECT SCHREGNO ";
        $query .= "            , YEAR ";
        $query .= "            , SEMESTER ";
        $query .= "            , GRADE ";
        $query .= "         FROM SCHREG_REGD_DAT ";
        $query .= "        WHERE SCHREGNO = '{$recRegNo}' ";
        $query .= " ) ";//END WITH
        $query .= "    SELECT SCHD.SCHREGNO ";      //学籍番号
        $query .= "         , SCHD.YEAR ";          //年度
        $query .= "         , CMT_M.COMMITTEENAME ";//委員会・生徒会(名称)
        $query .= "         , NM.NAME1 AS EXECUTIVE_NAME ";//役職
        $query .= "         , SCHD.GRADE ";         //学年
        $query .= "      FROM SRD ";
        $query .= " LEFT JOIN SCHREG_REGD_GDAT SRG ";
        $query .= "        ON (SRG.YEAR    = SRD.YEAR ";
        $query .= "       AND  SRG.GRADE   = SRD.GRADE) ";
        $query .= "      JOIN SCHREG_COMMITTEE_HIST_DAT SCHD ";
        $query .= "        ON (SCHD.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "       AND  SCHD.SCHOOL_KIND = SRG.SCHOOL_KIND ";
        $query .= "       AND  SCHD.YEAR        = SRD.YEAR ";
        $query .= "       AND  SCHD.SCHREGNO    = SRD.SCHREGNO) ";
        $query .= " LEFT JOIN COMMITTEE_MST CMT_M ";
        $query .= "        ON (CMT_M.SCHOOLCD    = SCHD.SCHOOLCD ";
        $query .= "       AND  CMT_M.SCHOOL_KIND = SCHD.SCHOOL_KIND ";
        $query .= "       AND  CMT_M.COMMITTEECD = SCHD.COMMITTEECD) ";
        $query .= " LEFT JOIN NAME_MST NM ";
        $query .= "        ON (NM.NAMECD1 = 'J002' ";
        $query .= "       AND  NM.NAMECD2 = SCHD.EXECUTIVECD) ";
        $query .= "  GROUP BY SCHD.SCHREGNO ";
        $query .= "         , SCHD.YEAR ";
        $query .= "         , SCHD.GRADE ";
        $query .= "         , CMT_M.COMMITTEENAME ";
        $query .= "         , SCHD.EXECUTIVECD ";
        $query .= "         , NM.NAME1 ";
        $query .= "  ORDER BY SCHD.SCHREGNO ";
        $query .= "         , SCHD.YEAR ";
        $query .= "         , CMT_M.COMMITTEENAME ";
        $query .= "         , SCHD.EXECUTIVECD ";

        return $query;
    }

    //(CSVデータ取得用)保護者情報
    public function getCsvGuardian1Data($model, $recRegNo)
    {
        $query  = "";
        $query .= "    SELECT GD.SCHREGNO ";   //学籍番号
        $query .= "         , GD.GUARD_KANA "; //保護者1(氏名かな)
        $query .= "         , GD.GUARD_NAME "; //保護者1(氏名)
        $query .= "         , NM.NAME1 AS RELATION_NAME ";//保護者1(続柄)
        $query .= "         , GD.GUARD_ADDR1 ";//保護者1(住所1)
        $query .= "         , GD.GUARD_ADDR2 ";//保護者1(住所2)
        $query .= "         , GD.GUARD_TELNO ";//保護者1(電話)
        $query .= "         , GD.GUARD_E_MAIL ";//保護者1(メール)
        $query .= "      FROM GUARDIAN_DAT GD ";
        $query .= " LEFT JOIN NAME_MST NM ";
        $query .= "        ON (NM.NAMECD1 = 'H201' ";
        $query .= "       AND  NM.NAMECD2 = GD.RELATIONSHIP) ";
        $query .= "  WHERE GD.SCHREGNO = '{$recRegNo}' ";

        return $query;
    }

    //(CSVデータ取得用)保護者情報2
    public function getCsvGuardian2Data($model, $recRegNo)
    {
        $query  = "";
        $query .= "    SELECT GD2.SCHREGNO ";   //学籍番号
        $query .= "         , GD2.GUARD_KANA "; //保護者1(氏名かな)
        $query .= "         , GD2.GUARD_NAME "; //保護者1(氏名)
        $query .= "         , NM.NAME1 AS RELATION_NAME ";//保護者1(続柄)
        $query .= "         , GD2.GUARD_ADDR1 ";//保護者1(住所1)
        $query .= "         , GD2.GUARD_ADDR2 ";//保護者1(住所2)
        $query .= "         , GD2.GUARD_TELNO ";//保護者1(電話)
        $query .= "         , GD2.GUARD_E_MAIL ";//保護者1(メール)
        $query .= "      FROM GUARDIAN2_DAT GD2 ";
        $query .= " LEFT JOIN NAME_MST NM ";
        $query .= "        ON (NM.NAMECD1 = 'H201' ";
        $query .= "       AND  NM.NAMECD2 = GD2.RELATIONSHIP) ";
        $query .= "  WHERE GD2.SCHREGNO = '{$recRegNo}' ";

        return $query;
    }

    //(CSVデータ取得用)身元引受人情報
    public function getCsvGuaranterData($model, $recRegNo)
    {
        $query  = "";
        $query .=  " WITH ";
        $query .=  " GUARANTOR_HIST_LAST AS ( ";
        $query .=  "        SELECT GHD.SCHREGNO ";
        $query .=  "             , MAX(GHD.ISSUEDATE) AS ISSUEDATE ";
        $query .=  "          FROM GUARANTOR_HIST_DAT GHD ";
        $query .=  "         WHERE GHD.SCHREGNO = '{$recRegNo}' ";
        $query .=  "      GROUP BY GHD.SCHREGNO ";
        $query .=  " ) ";//END WITH
        $query .=  "    SELECT GHD.SCHREGNO ";
        $query .=  "         , GHD.GUARANTOR_KANA "; //身元引受人(氏名かな)
        $query .=  "         , GHD.GUARANTOR_NAME "; //身元引受人(氏名)
        $query .=  "         , NM.NAME1 AS RELATION_NAME ";//身元引受人(続柄)
        $query .=  "         , GAD.GUARANTOR_ADDR1 ";//身元引受人(住所1)
        $query .=  "         , GAD.GUARANTOR_ADDR2 ";//身元引受人(住所2)
        $query .=  "         , GAD.GUARANTOR_TELNO ";//身元引受人(電話)
        $query .=  "      FROM GUARANTOR_HIST_DAT GHD ";
        $query .=  "      JOIN GUARANTOR_HIST_LAST GHL ";
        $query .=  "        ON (GHL.SCHREGNO  = GHD.SCHREGNO ";
        $query .=  "       AND  GHL.ISSUEDATE = GHD.ISSUEDATE) ";
        $query .=  " LEFT JOIN GUARANTOR_ADDRESS_DAT GAD ";
        $query .=  "        ON (GAD.SCHREGNO  = GHD.SCHREGNO) ";
        $query .= "  LEFT JOIN NAME_MST NM ";
        $query .= "         ON (NM.NAMECD1 = 'H201' ";
        $query .= "        AND  NM.NAMECD2 = GHD.GUARANTOR_RELATIONSHIP) ";

        return $query;
    }

    //(CSVデータ取得用)その他の情報
    public function getCsvSendData($model, $recRegNo)
    {
        $query  = "";
        $query .=  "   SELECT SSA.SCHREGNO ";  //学籍番号
        $query .=  "        , SSA.SEND_KANA "; //その他(氏名かな)
        $query .=  "        , SSA.SEND_NAME "; //その他(氏名)
        $query .=  "        , NM.NAME1 AS RELATION_NAME "; //その他(続柄)
        $query .=  "        , SSA.SEND_ADDR1 ";//その他(住所1)
        $query .=  "        , SSA.SEND_ADDR2 ";//その他(住所2)
        $query .=  "        , SSA.SEND_TELNO ";//その他(電話)
        $query .=  "     FROM SCHREG_SEND_ADDRESS_DAT SSA ";
        $query .= " LEFT JOIN NAME_MST NM ";
        $query .= "        ON (NM.NAMECD1 = 'H201' ";
        $query .= "       AND  NM.NAMECD2 = SSA.SEND_RELATIONSHIP) ";
        $query .=  "  WHERE SSA.SCHREGNO = '{$recRegNo}' ";
        $query .=  " ORDER BY SSA.DIV ASC ";

        return $query;
    }

    //(CSVデータ取得用)緊急連絡先情報
    public function getCsvEmergencyData($model, $recRegNo)
    {
        $query  = "";
        $query .=  " SELECT SBM.SCHREGNO ";      //学籍番号
        $query .=  "      , SBM.EMERGENCYNAME "; //緊急連絡先(緊急1：氏名)
        $query .=  "      , SBM.EMERGENCYTELNO "; //緊急連絡先(緊急1：連絡先)
        $query .=  "      , SBM.EMERGENCYNAME2 ";//緊急連絡先(緊急2：氏名)
        $query .=  "      , SBM.EMERGENCYTELNO2 ";//緊急連絡先(緊急2：連絡先)
        $query .=  "   FROM SCHREG_BASE_MST SBM ";
        $query .=  "  WHERE SBM.SCHREGNO = '{$recRegNo}' ";

        return $query;
    }

    //(CSVデータ取得用)兄弟姉妹情報
    public function getCsvSiblingsData($model, $recRegNo)
    {
        $query  = "";
        $query .=  " WITH ";
        $query .=  " RELA AS ( ";
        $query .=  "     SELECT SCHREGNO ";
        $query .=  "          , RELAKANA ";
        $query .=  "          , RELANAME ";
        $query .=  "          , RELATIONSHIP ";
        $query .=  "          , RELA_SCHREGNO ";
        $query .=  "       FROM SCHREG_RELA_DAT ";
        $query .=  "      WHERE RELA_SCHREGNO IS NOT NULL ";
        $query .=  "        AND SCHREGNO = '{$recRegNo}' ";
        $query .=  " ) ";//END WITH
        $query .=  "    SELECT RELA.SCHREGNO ";//学籍番号(※本人)
        $query .=  "         , RELA.RELAKANA ";//兄弟姉妹(かな)
        $query .=  "         , RELA.RELANAME ";//兄弟姉妹(氏名)
        $query .=  "         , NM.NAME1 AS RELATION_NAME ";//兄弟姉妹(続柄)
        $query .=  "         , CASE WHEN SRD.GRADE = '01' THEN '1年' ";
        $query .=  "                WHEN SRD.GRADE = '02' THEN '2年' ";
        $query .=  "                WHEN SRD.GRADE = '03' THEN '3年' ";
        $query .=  "                ELSE NULL ";
        $query .=  "           END || SRH.HR_CLASS_NAME1 AS GRADE_NAME ";//兄弟姉妹(年組)
        $query .=  "      FROM RELA ";
        $query .=  " LEFT JOIN SCHREG_REGD_DAT SRD ";
        $query .=  "        ON (SRD.YEAR     = '".CTRL_YEAR."' ";
        $query .=  "       AND  SRD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .=  "       AND  SRD.SCHREGNO = RELA.RELA_SCHREGNO) ";
        $query .=  " LEFT JOIN SCHREG_REGD_HDAT SRH ";
        $query .=  "        ON (SRH.YEAR     = SRD.YEAR ";
        $query .=  "       AND  SRH.SEMESTER = SRD.SEMESTER ";
        $query .=  "       AND  SRH.GRADE    = SRD.GRADE ";
        $query .=  "       AND  SRH.HR_CLASS = SRD.HR_CLASS) ";
        $query .= "  LEFT JOIN NAME_MST NM ";
        $query .= "         ON (NM.NAMECD1 = 'H201' ";
        $query .= "        AND  NM.NAMECD2 = RELA.RELATIONSHIP) ";

        return $query;
    }

    //(CSVデータ取得用)成績情報
    public function getCsvScoreData($model, $recRegNo)
    {
        $query  = "";
        $query .=  " WITH ";
        $query .=  " SRD AS ( ";//生徒情報、年度
        $query .=  "        SELECT SRD.SCHREGNO ";     //学籍番号
        $query .=  "             , SRD.YEAR ";         //年度(西暦)
        $query .=  "             , CASE WHEN SRD.GRADE = '01' THEN '1年' ";
        $query .=  "                    WHEN SRD.GRADE = '02' THEN '2年' ";
        $query .=  "                    WHEN SRD.GRADE = '03' THEN '3年' ";
        $query .=  "                    ELSE NULL ";
        $query .=  "               END AS GRADE_NAME ";//学年
        $query .=  "          FROM SCHREG_REGD_DAT  SRD ";
        $query .=  "     LEFT JOIN SCHREG_REGD_HDAT SRH ";
        $query .=  "            ON (SRH.YEAR     = SRD.YEAR ";
        $query .=  "           AND  SRH.SEMESTER = SRD.SEMESTER ";
        $query .=  "           AND  SRH.GRADE    = SRD.GRADE ";
        $query .=  "           AND  SRH.HR_CLASS = SRD.HR_CLASS) ";
        $query .=  "          JOIN SCHREG_BASE_MST SB_M ";
        $query .=  "            ON (SB_M.SCHREGNO = SRD.SCHREGNO) ";
        $query .=  "         WHERE SRD.SCHREGNO = '{$recRegNo}' ";
        $query .=  "      GROUP BY SRD.SCHREGNO ";
        $query .=  "             , SRD.YEAR ";
        $query .=  "             , SRD.GRADE ";
        $query .=  "             , SRH.HR_CLASS_NAME2 ";
        $query .=  "             , SRD.ATTENDNO ";
        $query .=  "             , SB_M.NAME ";
        $query .=  "      ORDER BY SRD.SCHREGNO ";
        $query .=  "             , SRD.YEAR ";
        $query .=  " )";
        $query .=  " ,";
        $query .=  " RSD AS ( ";//特定生徒の成績で絞る
        $query .=  "        SELECT SCHREGNO ";
        $query .=  "             , RECORD_SCORE_DAT.YEAR ";
        $query .=  "             , RECORD_SCORE_DAT.SEMESTER ";
        $query .=  "             , RECORD_SCORE_DAT.TESTKINDCD ";
        $query .=  "             , RECORD_SCORE_DAT.TESTITEMCD ";
        $query .=  "             , RECORD_SCORE_DAT.SCORE_DIV ";
        $query .=  "             , RECORD_SCORE_DAT.CLASSCD ";
        $query .=  "             , RECORD_SCORE_DAT.SCHOOL_KIND ";
        $query .=  "             , RECORD_SCORE_DAT.CURRICULUM_CD ";
        $query .=  "             , RECORD_SCORE_DAT.SUBCLASSCD ";
        $query .=  "             , SUBCLASS_MST.SUBCLASSABBV ";
        $query .=  "             , RECORD_SCORE_DAT.SCORE ";
        $query .=  "          FROM RECORD_SCORE_DAT ";
        $query .=  "     LEFT JOIN SUBCLASS_MST ";
        $query .=  "            ON (SUBCLASS_MST.CLASSCD       = RECORD_SCORE_DAT.CLASSCD ";
        $query .=  "           AND  SUBCLASS_MST.SCHOOL_KIND   = RECORD_SCORE_DAT.SCHOOL_KIND ";
        $query .=  "           AND  SUBCLASS_MST.CURRICULUM_CD = RECORD_SCORE_DAT.CURRICULUM_CD ";
        $query .=  "           AND  SUBCLASS_MST.SUBCLASSCD    = RECORD_SCORE_DAT.SUBCLASSCD) ";
        $query .=  "         WHERE SCHREGNO = '{$recRegNo}' ";
        $query .=  "           AND ( ";
        $query .=  "                   (RECORD_SCORE_DAT.SEMESTER IN ('1', '2', '3') AND RECORD_SCORE_DAT.SCORE_DIV = '01') ";
        $query .=  "                OR (RECORD_SCORE_DAT.SEMESTER =  '9'             AND RECORD_SCORE_DAT.SCORE_DIV = '08') ";
        $query .=  "               ) ";
        $query .=  " ) ";
        $query .=  " , ";
        $query .=  " TMCNS AS ( ";//対象の考査で絞る
        $query .=  "     SELECT * ";
        $query .=  "       FROM TESTITEM_MST_COUNTFLG_NEW_SDIV ";
        $query .=  "      WHERE SEMESTER   IN ('1', '2', '3', '9') ";
        $query .=  "        AND TESTKINDCD IN ('01', '02', '99') ";
        $query .=  "        AND TESTITEMCD IN ('00', '01', '02') ";
        $query .=  "        AND SCORE_DIV  = '01' ";
        $query .=  " ) ";//END WITH
        $query .=  "    SELECT SRD.SCHREGNO ";
        $query .=  "         , SRD.YEAR ";          //年度(西暦)
        $query .=  "         , SRD.GRADE_NAME ";    //学年
        $query .=  "         , TMCNS.SEMESTER||TMCNS.TESTITEMCD||TMCNS.TESTKINDCD AS TESTCD ";//(学期の識別に使用)
        $query .=  "         , TMCNS.TESTITEMNAME ";//学期
        $query .=  "         , RSD.SUBCLASSCD ";    //(科目の識別に使用)
        $query .=  "         , RSD.SUBCLASSABBV ";  //科目名
        $query .=  "         , RSD.SCORE ";         //点数
        $query .=  "      FROM SRD ";
        $query .=  "      JOIN RSD ";
        $query .=  "        ON (RSD.SCHREGNO = SRD.SCHREGNO ";
        $query .=  "       AND  RSD.YEAR     = SRD.YEAR) ";
        $query .=  "      JOIN TMCNS ";
        $query .=  "        ON (TMCNS.YEAR       = RSD.YEAR ";
        $query .=  "       AND  TMCNS.SEMESTER   = RSD.SEMESTER ";
        $query .=  "       AND  TMCNS.TESTKINDCD = RSD.TESTKINDCD ";
        $query .=  "       AND  TMCNS.TESTITEMCD = RSD.TESTITEMCD ";
        $query .=  "       AND  TMCNS.SCORE_DIV  = RSD.SCORE_DIV) ";
        $query .=  "  ORDER BY SRD.SCHREGNO ";
        $query .=  "         , SRD.YEAR ";
        $query .=  "         , TMCNS.SEMESTER ";
        $query .=  "         , TMCNS.TESTITEMCD ";
        $query .=  "         , TMCNS.TESTKINDCD ";
        $query .=  "         , RSD.SUBCLASSCD ";

        return $query;
    }

    //(CSVデータ取得用)出欠情報
    public function getCsvAttendanceData($model, $recRegNo)
    {
        $query  = "";
        $query .=  " WITH SRD AS ( ";//生徒情報、年度
        $query .=  "        SELECT SRD.SCHREGNO ";     //学籍番号
        $query .=  "             , SRD.YEAR ";         //年度(西暦)
        $query .=  "             , CASE WHEN SRD.GRADE = '01' THEN '1年' ";
        $query .=  "                    WHEN SRD.GRADE = '02' THEN '2年' ";
        $query .=  "                    WHEN SRD.GRADE = '03' THEN '3年' ";
        $query .=  "                    ELSE NULL ";
        $query .=  "               END AS GRADE_NAME ";//学年
        $query .=  "          FROM SCHREG_REGD_DAT  SRD ";
        $query .=  "     LEFT JOIN SCHREG_REGD_HDAT SRH ";
        $query .=  "            ON (SRH.YEAR     = SRD.YEAR ";
        $query .=  "           AND  SRH.SEMESTER = SRD.SEMESTER ";
        $query .=  "           AND  SRH.GRADE    = SRD.GRADE ";
        $query .=  "           AND  SRH.HR_CLASS = SRD.HR_CLASS) ";
        $query .=  "          JOIN SCHREG_BASE_MST SB_M ";
        $query .=  "            ON (SB_M.SCHREGNO = SRD.SCHREGNO) ";
        $query .=  "         WHERE SRD.SCHREGNO = '{$recRegNo}' ";
        $query .=  "      GROUP BY SRD.SCHREGNO ";
        $query .=  "             , SRD.YEAR ";
        $query .=  "             , SRD.GRADE ";
        $query .=  "             , SRH.HR_CLASS_NAME2 ";
        $query .=  "             , SRD.ATTENDNO ";
        $query .=  "             , SB_M.NAME ";
        $query .=  "      ORDER BY SRD.SCHREGNO ";
        $query .=  "             , SRD.YEAR ";
        $query .=  " ) ";//END WITH
        $query .=  "    SELECT SRD.SCHREGNO ";
        $query .=  "         , SRD.YEAR ";               //年度(西暦)
        $query .=  "         , SRD.GRADE_NAME ";         //学年
        $query .=  "         , SAD.CLASSDAYS ";          //授業日数
        $query .=  "         , SAD.MOURNING ";           //出停忌引
        $query .=  "         , SAD.ABROAD ";             //留学日数
        $query .=  "         , (NVL(SAD.SICK, 0)+NVL(SAD.ACCIDENTNOTICE, 0)+NVL(SAD.NOACCIDENTNOTICE, 0)) AS SICK ";//欠席日数
        $query .=  "         , SAD.PRESENT ";            //出席日数
        $query .=  "         , SUM(ASD.LATE)  AS LATE "; //遅刻日数
        $query .=  "         , SUM(ASD.EARLY) AS EARLY ";//早退日数
        $query .=  "         , HD.ATTENDREC_REMARK ";    //備考
        $query .=  "      FROM SRD ";
        $query .=  " LEFT JOIN SCHREG_ATTENDREC_DAT SAD ";
        $query .=  "        ON (SAD.YEAR     = SRD.YEAR ";
        $query .=  "       AND  SAD.SCHREGNO = SRD.SCHREGNO) ";
        $query .=  " LEFT JOIN ATTEND_SEMES_DAT ASD ";
        $query .=  "        ON (ASD.YEAR     = SRD.YEAR ";
        $query .=  "       AND  ASD.SCHREGNO = SRD.SCHREGNO) ";
        $query .=  " LEFT JOIN HTRAINREMARK_DAT HD ";
        $query .=  "        ON (HD.YEAR     = SRD.YEAR ";
        $query .=  "       AND  HD.SCHREGNO = SRD.SCHREGNO) ";
        $query .=  "  GROUP BY SRD.SCHREGNO ";
        $query .=  "         , SRD.YEAR ";
        $query .=  "         , SRD.GRADE_NAME ";
        $query .=  "         , SAD.CLASSDAYS ";
        $query .=  "         , SAD.MOURNING ";
        $query .=  "         , SAD.ABROAD ";
        $query .=  "         , (NVL(SAD.SICK, 0)+NVL(SAD.ACCIDENTNOTICE, 0)+NVL(SAD.NOACCIDENTNOTICE, 0)) ";//
        $query .=  "         , SAD.PRESENT ";
        $query .=  "         , HD.ATTENDREC_REMARK ";
        $query .=  " ORDER BY SRD.SCHREGNO ";
        $query .=  "        , SRD.YEAR ";

        return $query;
    }

    //(CSVデータ取得用)資格情報
    public function getCsvQualifiedData($model, $recRegNo)
    {
        $query  = "";
        $query .=  " WITH SQHD AS ( ";
        $query .=  "     SELECT SCHREGNO ";
        $query .=  "          , REGDDATE ";
        $query .=  "          , YEAR ";
        $query .=  "          , QUALIFIED_CD ";
        $query .=  "          , RANK ";
        $query .=  "       FROM SCHREG_QUALIFIED_HOBBY_DAT ";
        $query .=  "      WHERE SCHREGNO = '{$recRegNo}' ";
        $query .=  " ) ";//END WITH
        $query .=  "    SELECT SQHD.SCHREGNO ";                            //学籍番号
        $query .=  "         , CASE WHEN SRD.GRADE = '01' THEN '1' ";
        $query .=  "                WHEN SRD.GRADE = '02' THEN '2' ";
        $query .=  "                WHEN SRD.GRADE = '03' THEN '3' ";
        $query .=  "                ELSE NULL ";
        $query .=  "           END AS GRADE ";                             //学年
        $query .=  "         , SQHD.REGDDATE ";                            //取得日
        $query .=  "         , QF_M.QUALIFIED_NAME||NM_M.NAME1 AS SIKAKU ";//資格名
        $query .=  "      FROM SQHD ";
        $query .=  " LEFT JOIN SCHREG_REGD_DAT SRD ";
        $query .=  "        ON (SRD.YEAR     = SQHD.YEAR ";
        $query .=  "       AND  SRD.SCHREGNO = SQHD.SCHREGNO) ";
        $query .=  " LEFT JOIN QUALIFIED_MST QF_M ";
        $query .=  "        ON (QF_M.QUALIFIED_CD = SQHD.QUALIFIED_CD) ";
        $query .=  " LEFT JOIN NAME_MST NM_M ";
        $query .=  "        ON (NM_M.NAMECD1 = 'H312' ";
        $query .=  "       AND  NM_M.NAMECD2 = SQHD.RANK) ";
        $query .=  "  GROUP BY SQHD.SCHREGNO ";
        $query .=  "         , GRADE ";
        $query .=  "         , SQHD.REGDDATE ";
        $query .=  "         , QF_M.QUALIFIED_NAME ";
        $query .=  "         , NM_M.NAME1 ";
        $query .=  "  ORDER BY SQHD.SCHREGNO ";
        $query .=  "         , SQHD.REGDDATE DESC";
        $query .=  "         , GRADE         DESC";

        return $query;
    }
}
