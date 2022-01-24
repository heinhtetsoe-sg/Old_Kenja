<?php

require_once('for_php7.php');

class knjb060bQuery extends Query
{
    public function getSemesterEdate()
    {
        $query  = " SELECT ";
        $query .= "   EDATE ";
        $query .= " FROM ";
        $query .= "   SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "   YEAR = '".CTRL_YEAR."' ";
        $query .= "   AND SEMESTER = '".CTRL_SEMESTER."' ";

        return $query;
    }

    /** 時間割情報 **/
    public function sqlTimetable($model)
    {
        $NONE_GROUP_CODE = "0000";

        $query  = " WITH PERIOD AS ( ";
        $query .= "   SELECT NAMECD2 AS PERIODCD ";
        $query .= "     FROM V_NAME_MST ";
        $query .= "    WHERE YEAR    = '".CTRL_YEAR."' ";
        $query .= "      AND NAMECD1 = 'B001' ";
        $query .= "      AND NAMESPARE2 IS NOT NULL ";
        $query .= " ) ";
        $query .= " , CHAIR AS ( ";
        $query .= "     SELECT CHAIR_DAT.CHAIRCD ";
        $query .= "          , CHAIR_DAT.CLASSCD ";
        $query .= "          , CHAIR_DAT.SCHOOL_KIND ";
        $query .= "          , CHAIR_DAT.CURRICULUM_CD ";
        $query .= "          , CHAIR_DAT.SUBCLASSCD ";
        $query .= "          , CHAIR_DAT.CHAIRABBV";
        $query .= "          , CHAIR_CLS_DAT.TRGTGRADE ";
        $query .= "          , CHAIR_CLS_DAT.TRGTCLASS ";
        $query .= "          , CHAIR_CLS_DAT.GROUPCD ";
        $query .= "          , CHAIR_CLS_DAT.YEAR ";
        $query .= "          , CHAIR_CLS_DAT.SEMESTER ";
        $query .= "       FROM CHAIR_DAT ";
        $query .= "       JOIN CHAIR_CLS_DAT ";
        $query .= "         ON ( ";
        $query .= "                 CHAIR_DAT.YEAR        = CHAIR_CLS_DAT.YEAR ";
        $query .= "             AND CHAIR_DAT.SEMESTER    = CHAIR_CLS_DAT.SEMESTER ";
        $query .= "             AND CHAIR_DAT.GROUPCD     = CHAIR_CLS_DAT.GROUPCD ";
        $query .= "             AND CHAIR_CLS_DAT.CHAIRCD = '0000000' ";
        $query .= "             AND CHAIR_CLS_DAT.GROUPCD <> '".$NONE_GROUP_CODE."' ";
        $query .= "            ) ";
        $query .= "      UNION ";
        $query .= "     SELECT CHAIR_DAT.CHAIRCD ";
        $query .= "          , CHAIR_DAT.CLASSCD ";
        $query .= "          , CHAIR_DAT.SCHOOL_KIND ";
        $query .= "          , CHAIR_DAT.CURRICULUM_CD ";
        $query .= "          , CHAIR_DAT.SUBCLASSCD ";
        $query .= "          , CHAIR_DAT.CHAIRABBV";
        $query .= "          , CHAIR_CLS_DAT.TRGTGRADE ";
        $query .= "          , CHAIR_CLS_DAT.TRGTCLASS ";
        $query .= "          , CHAIR_CLS_DAT.GROUPCD ";
        $query .= "          , CHAIR_CLS_DAT.YEAR ";
        $query .= "          , CHAIR_CLS_DAT.SEMESTER ";
        $query .= "       FROM CHAIR_DAT ";
        $query .= "       JOIN CHAIR_CLS_DAT ";
        $query .= "         ON ( ";
        $query .= "                 CHAIR_DAT.YEAR     = CHAIR_CLS_DAT.YEAR ";
        $query .= "             AND CHAIR_DAT.SEMESTER = CHAIR_CLS_DAT.SEMESTER ";
        $query .= "             AND CHAIR_DAT.CHAIRCD  = CHAIR_CLS_DAT.CHAIRCD ";
        $query .= "             AND CHAIR_DAT.GROUPCD  = '".$NONE_GROUP_CODE."' ";
        $query .= "            ) ";
        $query .= " ) ";
        $query .= "    SELECT SCD.EXECUTEDATE ";
        $query .= "         , SCD.UPDATED ";
        $query .= "         , SCD.PERIODCD ";
        $query .= "         , SCD.CHAIRCD ";
        $query .= "         , SRH.GRADE ";
        $query .= "         , SRH.HR_CLASS ";
        $query .= "         , SRH.HR_NAMEABBV ";
        $query .= "         , CHAIR.GROUPCD ";
        $query .= "         , SM.SUBCLASSABBV ";
        $query .= "      FROM SCH_CHR_DAT SCD ";
        $query .= " LEFT JOIN CHAIR CHAIR ";
        $query .= "        ON CHAIR.CHAIRCD     = SCD.CHAIRCD ";
        $query .= "       AND CHAIR.YEAR        = SCD.YEAR ";
        $query .= "       AND CHAIR.SEMESTER    = SCD.SEMESTER ";
        $query .= " LEFT JOIN SCHREG_REGD_HDAT SRH ";
        $query .= "        ON SCD.YEAR          = SRH.YEAR ";
        $query .= "       AND SCD.SEMESTER      = SRH.SEMESTER ";
        $query .= "       AND CHAIR.TRGTGRADE   = SRH.GRADE ";
        $query .= "       AND CHAIR.TRGTCLASS   = SRH.HR_CLASS ";
        $query .= " LEFT JOIN SUBCLASS_MST SM ";
        $query .= "        ON SM.CLASSCD        = CHAIR.CLASSCD ";
        $query .= "       AND SM.SCHOOL_KIND    = CHAIR.SCHOOL_KIND ";
        $query .= "       AND SM.CURRICULUM_CD  = CHAIR.CURRICULUM_CD ";
        $query .= "       AND SM.SUBCLASSCD     = CHAIR.SUBCLASSCD ";
        $query .= "     WHERE SCD.EXECUTEDATE  BETWEEN '".$model->sDate."' AND '".$model->eDate."' ";
        $query .= "   AND SCD.PERIODCD NOT IN (SELECT PERIODCD FROM PERIOD) ";
        $query .= "   AND CHAIR.SCHOOL_KIND = 'H' ";
        $query .= "  GROUP BY SCD.EXECUTEDATE ";
        $query .= "         , SCD.UPDATED ";
        $query .= "         , SCD.PERIODCD ";
        $query .= "         , SCD.CHAIRCD ";
        $query .= "         , SRH.GRADE ";
        $query .= "         , SRH.HR_CLASS ";
        $query .= "         , SRH.HR_NAMEABBV ";
        $query .= "         , CHAIR.GROUPCD ";
        $query .= "         , CHAIR.CHAIRABBV";
        $query .= "         , SM.SUBCLASSABBV ";
        $query .= "  ORDER BY SCD.EXECUTEDATE ";
        $query .= "         , SRH.HR_CLASS ";
        $query .= "         , SRH.GRADE ";
        $query .= "         , SCD.PERIODCD ";

        return $query;
    }

    /** 変更前時間割情報 **/
    public function sqlBeforeTimetable($model)
    {
        $NONE_GROUP_CODE = "0000";

        $query  = "  WITH PERIOD AS ( ";
        $query .= "    SELECT NAMECD2 AS PERIODCD ";
        $query .= "      FROM V_NAME_MST ";
        $query .= "    WHERE YEAR     = '".CTRL_YEAR."' ";
        $query .= "       AND NAMECD1 = 'B001' ";
        $query .= "       AND NAMESPARE2 IS NOT NULL ";
        $query .= "  ) ";
        $query .= "  , CHAIR AS ( ";
        $query .= "      SELECT CHAIR_DAT.CHAIRCD ";
        $query .= "           , CHAIR_DAT.CLASSCD ";
        $query .= "           , CHAIR_DAT.SCHOOL_KIND ";
        $query .= "           , CHAIR_DAT.CURRICULUM_CD ";
        $query .= "           , CHAIR_DAT.SUBCLASSCD ";
        $query .= "           , CHAIR_DAT.CHAIRABBV";
        $query .= "           , CHAIR_CLS_DAT.TRGTGRADE ";
        $query .= "           , CHAIR_CLS_DAT.TRGTCLASS ";
        $query .= "           , CHAIR_CLS_DAT.GROUPCD ";
        $query .= "           , CHAIR_CLS_DAT.YEAR ";
        $query .= "           , CHAIR_CLS_DAT.SEMESTER ";
        $query .= "        FROM CHAIR_DAT ";
        $query .= "        JOIN CHAIR_CLS_DAT ";
        $query .= "          ON ( ";
        $query .= "                  CHAIR_DAT.YEAR        = CHAIR_CLS_DAT.YEAR ";
        $query .= "              AND CHAIR_DAT.SEMESTER    = CHAIR_CLS_DAT.SEMESTER ";
        $query .= "              AND CHAIR_DAT.GROUPCD     = CHAIR_CLS_DAT.GROUPCD ";
        $query .= "              AND CHAIR_CLS_DAT.CHAIRCD = '0000000' ";
        $query .= "              AND CHAIR_CLS_DAT.GROUPCD <> '".$NONE_GROUP_CODE."' ";
        $query .= "             ) ";
        $query .= "       UNION ";
        $query .= "      SELECT CHAIR_DAT.CHAIRCD ";
        $query .= "           , CHAIR_DAT.CLASSCD ";
        $query .= "           , CHAIR_DAT.SCHOOL_KIND ";
        $query .= "           , CHAIR_DAT.CURRICULUM_CD ";
        $query .= "           , CHAIR_DAT.SUBCLASSCD ";
        $query .= "           , CHAIR_DAT.CHAIRABBV";
        $query .= "           , CHAIR_CLS_DAT.TRGTGRADE ";
        $query .= "           , CHAIR_CLS_DAT.TRGTCLASS ";
        $query .= "           , CHAIR_CLS_DAT.GROUPCD ";
        $query .= "           , CHAIR_CLS_DAT.YEAR ";
        $query .= "           , CHAIR_CLS_DAT.SEMESTER ";
        $query .= "        FROM CHAIR_DAT ";
        $query .= "        JOIN CHAIR_CLS_DAT ";
        $query .= "          ON ( ";
        $query .= "                  CHAIR_DAT.YEAR     = CHAIR_CLS_DAT.YEAR ";
        $query .= "              AND CHAIR_DAT.SEMESTER = CHAIR_CLS_DAT.SEMESTER ";
        $query .= "              AND CHAIR_DAT.CHAIRCD  = CHAIR_CLS_DAT.CHAIRCD ";
        $query .= "              AND CHAIR_DAT.GROUPCD  = '".$NONE_GROUP_CODE."' ";
        $query .= "             ) ";
        $query .= "  ) ";
        $query .= "     SELECT SRH.GRADE ";
        $query .= "          , SRH.HR_CLASS ";
        $query .= "          , SRH.HR_NAMEABBV ";
        $query .= "          , CHAIR.GROUPCD ";
        $query .= "          , NMD.FROM_DATE ";
        $query .= "          , NMD.FROM_PERIODCD ";
        $query .= "          , NMD.FROM_CHAIRCD  ";
        $query .= "          , SM.SUBCLASSABBV ";
        $query .= "       FROM NOTICE_MESSAGE_DAT NMD ";
        $query .= "  LEFT JOIN CHAIR CHAIR ";
        $query .= "         ON CHAIR.CHAIRCD     = NMD.FROM_CHAIRCD ";
        $query .= "        AND CHAIR.YEAR        = '".CTRL_YEAR."' ";
        $query .= "        AND CHAIR.SEMESTER    = '".CTRL_SEMESTER."' ";
        $query .= "  LEFT JOIN SCHREG_REGD_HDAT SRH ";
        $query .= "         ON CHAIR.YEAR        = SRH.YEAR ";
        $query .= "        AND CHAIR.SEMESTER    = SRH.SEMESTER ";
        $query .= "        AND CHAIR.TRGTGRADE   = SRH.GRADE ";
        $query .= "        AND CHAIR.TRGTCLASS   = SRH.HR_CLASS  ";
        $query .= "  LEFT JOIN SUBCLASS_MST SM ";
        $query .= "         ON SM.CLASSCD        = CHAIR.CLASSCD ";
        $query .= "        AND SM.SCHOOL_KIND    = CHAIR.SCHOOL_KIND ";
        $query .= "        AND SM.CURRICULUM_CD  = CHAIR.CURRICULUM_CD ";
        $query .= "        AND SM.SUBCLASSCD     = CHAIR.SUBCLASSCD ";
        $query .= "      WHERE NMD.FROM_DATE  BETWEEN '".$model->sDate."' AND '".$model->eDate."' ";
        $query .= "        AND NMD.FROM_PERIODCD NOT IN (SELECT PERIODCD FROM PERIOD) ";
        $query .= "        AND VALUE (NMD.CANCEL_FLG, '0') = '0' ";
        $query .= "        AND CHAIR.SCHOOL_KIND = 'H' ";
        $query .= "   GROUP BY SRH.GRADE ";
        $query .= "          , SRH.HR_CLASS ";
        $query .= "          , SRH.HR_NAMEABBV ";
        $query .= "          , CHAIR.GROUPCD ";
        $query .= "          , NMD.FROM_DATE ";
        $query .= "          , NMD.FROM_PERIODCD ";
        $query .= "          , NMD.FROM_CHAIRCD  ";
        $query .= "          , SM.SUBCLASSABBV ";
        $query .= "          , CHAIR.CHAIRABBV";
        $query .= "   ORDER BY SRH.HR_CLASS ";
        $query .= "          , SRH.GRADE  ";

        return $query;
    }

    /** 変更後時間割情報 **/
    public function sqlAfterTimetable($model)
    {
        $NONE_GROUP_CODE = "0000";

        $query  = " WITH PERIOD AS ( ";
        $query .= "   SELECT NAMECD2 AS PERIODCD ";
        $query .= "     FROM V_NAME_MST ";
        $query .= "    WHERE YEAR    = '".CTRL_YEAR."' ";
        $query .= "      AND NAMECD1 = 'B001' ";
        $query .= "      AND NAMESPARE2 IS NOT NULL ";
        $query .= " ) ";
        $query .= " , CHAIR AS ( ";
        $query .= "     SELECT CHAIR_DAT.CHAIRCD ";
        $query .= "          , CHAIR_DAT.CLASSCD ";
        $query .= "          , CHAIR_DAT.SCHOOL_KIND ";
        $query .= "          , CHAIR_DAT.CURRICULUM_CD ";
        $query .= "          , CHAIR_DAT.SUBCLASSCD ";
        $query .= "          , CHAIR_DAT.CHAIRABBV";
        $query .= "          , CHAIR_CLS_DAT.TRGTGRADE ";
        $query .= "          , CHAIR_CLS_DAT.TRGTCLASS ";
        $query .= "          , CHAIR_CLS_DAT.GROUPCD ";
        $query .= "          , CHAIR_CLS_DAT.YEAR ";
        $query .= "          , CHAIR_CLS_DAT.SEMESTER ";
        $query .= "       FROM CHAIR_DAT ";
        $query .= "       JOIN CHAIR_CLS_DAT ";
        $query .= "         ON ( ";
        $query .= "                 CHAIR_DAT.YEAR        = CHAIR_CLS_DAT.YEAR ";
        $query .= "             AND CHAIR_DAT.SEMESTER    = CHAIR_CLS_DAT.SEMESTER ";
        $query .= "             AND CHAIR_DAT.GROUPCD     = CHAIR_CLS_DAT.GROUPCD ";
        $query .= "             AND CHAIR_CLS_DAT.CHAIRCD = '0000000' ";
        $query .= "             AND CHAIR_CLS_DAT.GROUPCD <> '".$NONE_GROUP_CODE."' ";
        $query .= "            ) ";
        $query .= "      UNION ";
        $query .= "     SELECT CHAIR_DAT.CHAIRCD ";
        $query .= "          , CHAIR_DAT.CLASSCD ";
        $query .= "          , CHAIR_DAT.SCHOOL_KIND ";
        $query .= "          , CHAIR_DAT.CURRICULUM_CD ";
        $query .= "          , CHAIR_DAT.SUBCLASSCD ";
        $query .= "          , CHAIR_DAT.CHAIRABBV";
        $query .= "          , CHAIR_CLS_DAT.TRGTGRADE ";
        $query .= "          , CHAIR_CLS_DAT.TRGTCLASS ";
        $query .= "          , CHAIR_CLS_DAT.GROUPCD ";
        $query .= "          , CHAIR_CLS_DAT.YEAR ";
        $query .= "          , CHAIR_CLS_DAT.SEMESTER ";
        $query .= "       FROM CHAIR_DAT ";
        $query .= "       JOIN CHAIR_CLS_DAT ";
        $query .= "         ON ( ";
        $query .= "                 CHAIR_DAT.YEAR     = CHAIR_CLS_DAT.YEAR ";
        $query .= "             AND CHAIR_DAT.SEMESTER = CHAIR_CLS_DAT.SEMESTER ";
        $query .= "             AND CHAIR_DAT.CHAIRCD  = CHAIR_CLS_DAT.CHAIRCD ";
        $query .= "             AND CHAIR_DAT.GROUPCD  = '".$NONE_GROUP_CODE."' ";
        $query .= "            ) ";
        $query .= " ) ";
        $query .= "    SELECT SRH.GRADE ";
        $query .= "         , SRH.HR_CLASS ";
        $query .= "         , SRH.HR_NAMEABBV ";
        $query .= "         , CHAIR.GROUPCD ";
        $query .= "         , NMD.TO_PERIODCD ";
        $query .= "         , NMD.FROM_DATE ";
        $query .= "         , NMD.TO_DATE ";
        $query .= "         , NMD.TO_CHAIRCD ";
        $query .= "         , SM.SUBCLASSABBV AS TOSUBCLASSNAME ";
        $query .= "      FROM NOTICE_MESSAGE_DAT NMD ";
        $query .= " LEFT JOIN CHAIR CHAIR ";
        $query .= "        ON CHAIR.CHAIRCD     = NMD.TO_CHAIRCD ";
        $query .= "       AND CHAIR.YEAR        = '".CTRL_YEAR."' ";
        $query .= "       AND CHAIR.SEMESTER    = '".CTRL_SEMESTER."' ";
        $query .= " LEFT JOIN SCHREG_REGD_HDAT SRH ";
        $query .= "        ON CHAIR.YEAR        = SRH.YEAR ";
        $query .= "       AND CHAIR.SEMESTER    = SRH.SEMESTER ";
        $query .= "       AND CHAIR.TRGTGRADE   = SRH.GRADE ";
        $query .= "       AND CHAIR.TRGTCLASS   = SRH.HR_CLASS  ";
        $query .= " LEFT JOIN SUBCLASS_MST SM ";
        $query .= "        ON SM.CLASSCD        = CHAIR.CLASSCD ";
        $query .= "       AND SM.SCHOOL_KIND    = CHAIR.SCHOOL_KIND ";
        $query .= "       AND SM.CURRICULUM_CD  = CHAIR.CURRICULUM_CD ";
        $query .= "       AND SM.SUBCLASSCD     = CHAIR.SUBCLASSCD ";
        $query .= "     WHERE NMD.TO_DATE  BETWEEN '".$model->sDate."' AND '".$model->eDate."' ";
        $query .= "       AND NMD.TO_CHAIRCD  NOT IN (SELECT PERIODCD FROM PERIOD) ";
        $query .= "       AND VALUE (NMD.CANCEL_FLG, '0') = '0' ";
        $query .= "       AND CHAIR.SCHOOL_KIND = 'H' ";
        $query .= "  GROUP BY SRH.GRADE ";
        $query .= "         , SRH.HR_CLASS ";
        $query .= "         , SRH.HR_NAMEABBV ";
        $query .= "         , CHAIR.GROUPCD ";
        $query .= "         , NMD.TO_PERIODCD ";
        $query .= "         , NMD.FROM_DATE ";
        $query .= "         , NMD.TO_DATE ";
        $query .= "         , NMD.TO_CHAIRCD ";
        $query .= "         , SM.SUBCLASSABBV ";
        $query .= "         , CHAIR.CHAIRABBV";
        $query .= "  ORDER BY SRH.HR_CLASS ";
        $query .= "         , SRH.GRADE  ";

        return $query;
    }
}
