<?php

require_once('for_php7.php');

class knjh111Query extends Query {
    //資格内容取得
    function getContents() {
        $query  = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'H310' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";
        return $query;
    }

    //回数取得
    function getMaxSeq($sch) {
        $query  = " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO, ";
        $query .= "     MAX(VALUE(SEQ,0)) + 1 AS SEQ ";
        $query .= " FROM ";
        $query .= "     SCHREG_QUALIFIED_HOBBY_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' ";
        $query .= "     and SCHREGNO = '".$sch."' ";
        $query .= " GROUP BY ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO ";
        return $query;
    }

    //学籍資格データよりデータを取得
    function getAward($model, $schregno) {
        $query .="SELECT ";
        $query .="    T1.year, ";
        $query .="    T1.regddate, ";
        $query .="    T1.subclasscd, ";
        $query .="    CASE WHEN T1.CONDITION_DIV = '1' ";
        $query .="              THEN T1.CONDITION_DIV || ':資格' ";
        $query .="              ELSE T1.CONDITION_DIV || ':その他' ";
        $query .="         END AS CONDITION_DIV, ";
        $query .="    T1.SEQ, ";
        $query .="    T1.contents, ";
        $query .="    T1.remark, ";
        $query .="    T1.credits, ";
        $query .="    T5.CREDITS AS CREDIT ";
        $query .=" FROM  schreg_qualified_hobby_dat T1 ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .="       LEFT OUTER JOIN subclass_mst T2 ON T1.subclasscd     = T2.subclasscd ";
            $query .="                                      AND T1.CLASSCD        = T2.CLASSCD ";
            $query .="                                      AND T1.SCHOOL_KIND    = T2.SCHOOL_KIND ";
            $query .="                                      AND T1.CURRICULUM_CD  = T2.CURRICULUM_CD ";
        } else {
            $query .="       LEFT OUTER JOIN subclass_mst T2 ON T1.subclasscd = T2.subclasscd ";
        }
        $query .="       LEFT JOIN SCHREG_REGD_DAT T4 ON T4.SCHREGNO = T1.SCHREGNO ";
        $query .="                                   AND T4.YEAR     = T1.year ";
        $query .="                                   AND T4.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .="       LEFT JOIN CREDIT_MST T5 ON T5.YEAR       = T1.year ";
        $query .="                              AND T5.COURSECD   = T4.COURSECD ";
        $query .="                              AND T5.MAJORCD    = T4.MAJORCD ";
        $query .="                              AND T5.GRADE      = T4.GRADE ";
        $query .="                              AND T5.COURSECODE = T4.COURSECODE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .="                              AND T5.CLASSCD        = T1.CLASSCD ";
            $query .="                              AND T5.SUBCLASSCD     = T1.SUBCLASSCD ";
            $query .="                              AND T5.SCHOOL_KIND    = T1.SCHOOL_KIND ";
            $query .="                              AND T5.CURRICULUM_CD  = T1.CURRICULUM_CD ";
        } else {
            $query .="                              AND T5.CLASSCD    = SUBSTR(T1.SUBCLASSCD,1,2) ";
            $query .="                              AND T5.SUBCLASSCD = T1.SUBCLASSCD ";
        }
        $query .="WHERE T1.schregno = '{$schregno}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $QUERY .="ORDER BY T1.REGDDATE, T1.CLASSCD, T1.SUBCLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SEQ ";
        } else {
            $query .="ORDER BY T1.regddate, T1.subclasscd, T1.SEQ ";
        }

        return $query;
    }

    //学校校種取得
    function getSchoolKind($model) {
        $query  = "SELECT ";
        $query .= "        SCHOOL_KIND ";
        $query .= "FROM ";
        $query .= "        SCHREG_REGD_GDAT ";
        $query .= "WHERE ";
        $query .= "        GRADE = '".$model->grade."' ";
        $query .= "AND      YEAR = '".CTRL_YEAR."' ";
        
        return $query;
    }

    //１レコード取得
    function getRow($regddate,$schregno,$subclasscd,$condition,$seq) {
        $query  = "SELECT * FROM schreg_qualified_hobby_dat ";
        $query .= " WHERE year = '".CTRL_YEAR."'";
        if ($seq) {
            $query .= "   AND SEQ = {$seq} ";
        }
        $query .= "   AND schregno = '{$schregno}' ";

        return $query;
    }

    //単位取得 ※使っていない？
    function getCre($fields,$schregno) {
        $db = Query::dbCheckOut();

        $query  ="SELECT ";
        $query .="    CREDITS ";
        $query .=" FROM  CREDIT_MST ";
        $query .=" WHERE YEAR = '".CTRL_YEAR."' ";
        $query .="      AND GRADE = '".$fields["ANNUAL"]."' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .="      AND CLASSCD         = SUBSTR('".$fields["SUBCLASSCD"]."',1,2) ";
            $query .="      AND SUBCLASSCD      = '".$fields["SUBCLASSCD"]."' ";
            //$query .="      AND SCHOOL_KIND     = '".$fields["SCHOOL_KIND"]."' ";
            $query .="      AND CURRICULUM_CD   = '2' ";
        } else {
            $query .="      AND CLASSCD = SUBSTR('".$fields["SUBCLASSCD"]."',1,2) ";
            $query .="      AND SUBCLASSCD = '".$fields["SUBCLASSCD"]."' ";
        }
        $query .="      AND COURSECD || MAJORCD || COURSECODE = (SELECT COURSECD || MAJORCD || COURSECODE ";
        $query .="                                               FROM SCHREG_REGD_DAT ";
        $query .="                                               WHERE ";
        $query .="                                                       SCHREGNO = '".$schregno."' ";
        $query .="                                                   AND YEAR     = '".CTRL_YEAR."' ";
        $query .="                                                   AND SEMESTER = '".CTRL_SEMESTER."') ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    // 資格テキストINSERT
    function getInsertContName($fields) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     NAMECD1, ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "         NAMECD1 = 'H310' ";
        $query .= "     AND NAME1   = '{$fields["CONTENTSTEXT"]}' ";

        $checkRow = $db->getRow($query, DB_FETCHMODE_ASSOC);

        if (is_array($checkRow)) {
            $maxCd["NAMECD2"] = $checkRow["NAMECD2"];
        } else {
            $query  = " SELECT ";
            $query .= "     INT(MAX(NAMECD2))+1 AS NAMECD2 ";
            $query .= " FROM ";
            $query .= "     NAME_MST ";
            $query .= " WHERE ";
            $query .= "     NAMECD1 = 'H310' ";

            $maxCd = $db->getRow($query, DB_FETCHMODE_ASSOC);

            $dataMst["NAMECD1"][TEXT]        = "H310";
            $dataMst["NAMECD2"][TEXT]        = sprintf("%02d",$maxCd["NAMECD2"]);
            $dataMst["NAME1"][TEXT]          = $fields["CONTENTSTEXT"];
            $dataMst["REGISTERCD"][TEXT]     = STAFFCD;
            $dataMst["UPDATED"][NUMBER]      = "sysdate()";

            $query = Query::insertSQL($dataMst, "NAME_MST");
            $db->query($query);

            $dataYdat["YEAR"][TEXT]           = CTRL_YEAR;
            $dataYdat["NAMECD1"][TEXT]        = "H310";
            $dataYdat["NAMECD2"][TEXT]        = sprintf("%02d",$maxCd["NAMECD2"]);
            $dataYdat["REGISTERCD"][TEXT]     = STAFFCD;
            $dataYdat["UPDATED"][NUMBER]      = "sysdate()";

            $query = Query::insertSQL($dataYdat, "NAME_YDAT");
            $db->query($query);
        }
        Query::dbCheckIn($db);
        return $maxCd["NAMECD2"];
    }

    // INSERT
    function &getInsertQuery($model,$fields,$schregno) {
        $credits = $fields["CREDITS"];
        $db = Query::dbCheckOut();

        $query = knjh111Query::getMaxSeq($schregno);
        $result = $db->query($query);

        $seq = 1;
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $seq = $row["SEQ"];
        }

        $result->free();

        $data["YEAR"][TEXT]           = CTRL_YEAR;
        $data["REGDDATE"]["TIMESTAMP"]  = strtr($fields["REGDDATE"],"/","-");
        $data["SCHREGNO"][TEXT]       = $schregno;
        $data["SEQ"][NUMBER]          = $seq;
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["CLASSCD"][TEXT]        = $fields["CLASSCD"];
            $data["SCHOOL_KIND"][TEXT]    = $model->schoolkind;
            $data["CURRICULUM_CD"][TEXT]  = '2';
            $data["SUBCLASSCD"][TEXT]     = $fields["SUBCLASSCD"];
        } else {
            $data["SUBCLASSCD"][TEXT]     = $fields["SUBCLASSCD"];
        }
        $data["CONDITION_DIV"][TEXT]  = $fields["CONDITION_DIV"];
        if ($fields["CONDITION_DIV"]  == 1) {
            $data["CONTENTS"][TEXT]   = $fields["CONTENTSTEXT"];
        } else {
            $data["CONTENTS"][TEXT]   = $fields["CONTENTS2"];
        }
        $data["REMARK"][TEXT]         = $fields["REMARK"];
        $data["CREDITS"][NUMBER]      = $credits;
        $data["REGISTERCD"][TEXT]     = STAFFCD;
        $data["UPDATED"][NUMBER]      = "sysdate()";

        $query = Query::insertSQL($data, "schreg_qualified_hobby_dat");
        $db->query($query);
        Query::dbCheckIn($db);
        return $seq;
    }

    // UPDATE
    function &getUpdateQuery($model, $fields, $schregno, $seq) {
        $db = Query::dbCheckOut();

        $data["REGDDATE"]["TIMESTAMP"]  = strtr($fields["REGDDATE"],"/","-");
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["CLASSCD"][TEXT]        = $fields["CLASSCD"];
            $data["SCHOOL_KIND"][TEXT]    = $model->schoolkind;
            $data["CURRICULUM_CD"][TEXT]  = '2';
            $data["SUBCLASSCD"][TEXT]     = $fields["SUBCLASSCD"];
        } else {
            $data["SUBCLASSCD"][TEXT]     = $fields["SUBCLASSCD"];
        }
        $data["CONDITION_DIV"][TEXT]  = $fields["CONDITION_DIV"];
        if ($fields["CONDITION_DIV"]  == 1) {
            $data["CONTENTS"][TEXT]   = $fields["CONTENTSTEXT"];
        } else {
            $data["CONTENTS"][TEXT]   = $fields["CONTENTS2"];
        }
        $data["REMARK"][TEXT]         = $fields["REMARK"];
        $data["CREDITS"][NUMBER]      = $fields["CREDITS"];
        $data["REGISTERCD"][TEXT]     = STAFFCD;
        $data["UPDATED"][NUMBER]      = "sysdate()";

        $where  = " WHERE ";
        $where .= "     YEAR      = '".CTRL_YEAR."' AND ";
        $where .= "     SEQ       = ".$seq." AND ";
        $where .= "     SCHREGNO  = '".$schregno."' ";

        $db->query(Query::updateSQL($data, "SCHREG_QUALIFIED_HOBBY_DAT", $where));
        Query::dbCheckIn($db);
        return $seq;
    }

    // DELETE
    function &getDeleteQuery($regddate,$schregno,$subclasscd,$seq) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     SCHREG_QUALIFIED_HOBBY_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '" .CTRL_YEAR. "' ";
        $query .= " AND SEQ      = {$seq} ";
        $query .= " AND SCHREGNO = '{$schregno}' ";
        $db->query($query);
        Query::dbCheckIn($db);
        return $result;
    }
}
?>
