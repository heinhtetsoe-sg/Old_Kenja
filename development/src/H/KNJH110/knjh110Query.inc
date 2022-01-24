<?php

require_once('for_php7.php');

class knjh110Query extends Query {

    //科目名称取得
    function getName($model) {
        $query  = "SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "        CLASSCD, ";
            $query .= "        SCHOOL_KIND, ";
            $query .= "        CURRICULUM_CD, ";
        }
        $query .= "        SUBCLASSCD, ";
        $query .= "        SUBCLASSNAME ";
        $query .= "FROM ";
        $query .= "        SUBCLASS_MST ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "WHERE";
            $query .= "        SCHOOL_KIND = '".$model->schoolkind."' ";
        }
        $query .= "ORDER BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "        CLASSCD, ";
            $query .= "        SCHOOL_KIND, ";
            $query .= "        CURRICULUM_CD, ";
        }
        $query .= "        SUBCLASSCD";
        
        return $query;
    }
    
    //学校校種取得
    function getSchoolKind($model) {
        $query  = "SELECT ";
        $query .= "        SCHOOL_KIND ";
        $query .= "FROM ";
        $query .= "        SCHREG_REGD_GDAT ";
        $query .= "WHERE ";
        $query .= "        GRADE = '".$model->field["ANNUAL"]."' ";
        $query .= "AND      YEAR = '".CTRL_YEAR."' ";
        
        return $query;
    }
    
    //資格内容取得 NO001
    function getContents($namecd)
    {
        $query  = "SELECT namecd2,name1 FROM v_name_mst ";
        $query .= " WHERE year = '".CTRL_YEAR."' and namecd1 = '" .$namecd. "' ";
        $query .= " ORDER BY namecd2";
        return $query;
    }

    //資格テキスト
    function getContName($content,$namecd)
    {
        $query  = "SELECT name1 FROM v_name_mst ";
        $query .= " WHERE year = '".CTRL_YEAR."' and namecd1 = '" .$namecd. "' AND namecd2 = '" .$content. "'";
        return $query;
    }

    //回数取得 NO001
    function getMaxSeq($sch)
    {
        $query  = "SELECT YEAR,SCHREGNO,MAX(VALUE(SEQ,0)) + 1 AS SEQ FROM schreg_qualified_dat ";
        $query .= " WHERE YEAR = '".CTRL_YEAR."' and SCHREGNO = '".$sch."' ";
        $query .= " GROUP BY YEAR,SCHREGNO ";
        return $query;
    }

    //学籍資格データよりデータを取得
    function getAward($model, $schregno)
    {   
        $query  ="SELECT ";
        $query .="    T1.year, ";
        $query .="    T1.regddate, ";
        $query .="    T1.subclasscd, T2.subclassname, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .="    T1.CLASSCD, ";
            $query .="    T1.SCHOOL_KIND, ";
            $query .="    T1.CURRICULUM_CD, ";
        }
        $query .="    CASE WHEN T1.CONDITION_DIV = '1' THEN T1.CONDITION_DIV || ':増加' ";
        $query .="         WHEN T1.CONDITION_DIV = '2' THEN T1.CONDITION_DIV || ':学外増加' ";
        $query .="         ELSE T1.CONDITION_DIV || ':学外認定' END AS CONDITION_DIV, ";    //NO001
        $query .="    T1.SEQ, ";    //NO001
        $query .="    CASE WHEN T1.CONDITION_DIV = '1' THEN T1.contents || ':' || VALUE(H305.NAME1,'') ";
        $query .="         WHEN T1.CONDITION_DIV = '2' THEN T1.contents || ':' || VALUE(H306.NAME1,'') ";
        $query .="         ELSE T1.contents END AS contents, ";
        $query .="    T1.remark, ";
        $query .="    T1.credits, ";
        $query .="    T5.CREDITS AS CREDIT ";   //NO002
        $query .=" FROM  schreg_qualified_dat T1 ";
        $query .="       LEFT OUTER JOIN subclass_mst T2 ON T1.SUBCLASSCD    = T2.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .="                                      AND T1.CLASSCD       = T2.CLASSCD ";
            $query .="                                      AND T1.SCHOOL_KIND   = T2.SCHOOL_KIND ";
            $query .="                                      AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ";
        }
        $query .="       LEFT JOIN name_mst H305 ON H305.namecd1 = 'H305' and T1.contents = H305.namecd2 ";
        $query .="       LEFT JOIN name_mst H306 ON H306.namecd1 = 'H306' and T1.contents = H306.namecd2 ";
        //NO001-->
        $query .="       LEFT JOIN SCHREG_REGD_DAT T4 ON T4.SCHREGNO = T1.SCHREGNO ";
        $query .="       AND T4.YEAR = T1.year ";
        $query .="       AND T4.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .="       LEFT JOIN CREDIT_MST T5 ON T5.YEAR = T1.year ";
        $query .="       AND T5.COURSECD = T4.COURSECD ";
        $query .="       AND T5.MAJORCD = T4.MAJORCD ";
        $query .="       AND T5.GRADE = T4.GRADE ";
        $query .="       AND T5.COURSECODE = T4.COURSECODE ";
        $query .="       AND T5.CLASSCD = SUBSTR(T1.SUBCLASSCD,1,2) ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .="   AND T5.SCHOOL_KIND   = T1.SCHOOL_KIND ";
            $query .="   AND T5.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .="       AND T5.SUBCLASSCD = T1.SUBCLASSCD ";
        
        //NO001<--
        $query .="WHERE T1.schregno = '".$schregno."'";
        $query .="ORDER BY T1.regddate, T1.subclasscd, T1.SEQ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .=",T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD";
        }
        return $query;
    }

    //１レコード取得 NO001
    function getRow($regddate,$schregno,$subclasscd,$condition,$seq)
    {
        $db = Query::dbCheckOut();

        $query  = "SELECT * FROM schreg_qualified_dat ";
        $query .= " WHERE year     = '".CTRL_YEAR."'";
        //NO001
        if ($seq){
            $query .= "   AND SEQ = ".$seq."";
        }
        $query .= "   AND schregno = '".$schregno. "'";
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //単位取得 NO001 ※使っていない
    function getCre($fields,$schregno)
    {
        $db = Query::dbCheckOut();

        $query  ="SELECT ";
        $query .="    CREDITS ";
        $query .=" FROM  CREDIT_MST ";
        $query .="WHERE YEAR = '".CTRL_YEAR."' ";
        $query .="      AND GRADE = '".$fields["ANNUAL"]."' ";
        $query .="      AND CLASSCD = SUBSTR('".$fields["SUBCLASSCD"]."',1,2) ";
        $query .="      AND SUBCLASSCD = '".$fields["SUBCLASSCD"]."' ";
        $query .="      AND COURSECD || MAJORCD || COURSECODE = (SELECT COURSECD || MAJORCD || COURSECODE ";
        $query .="                                               FROM SCHREG_REGD_DAT ";
        $query .="                                               WHERE SCHREGNO = '".$schregno."' ";
        $query .="                                                     AND YEAR = '".CTRL_YEAR."' ";
        $query .="                                                     AND SEMESTER = '".CTRL_SEMESTER."') ";
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    // 資格テキストINSERT
    function getInsertContName($fields, $namecd)
    {
        $db = Query::dbCheckOut();

        $query  = "SELECT namecd1,namecd2,name1 FROM name_mst ";
        $query .= " WHERE namecd1 = '" .$namecd. "' AND name1 = '" .$fields["CONTENTSTEXT"]. "'";

        $checkRow = $db->getRow($query, DB_FETCHMODE_ASSOC);

        if (is_array($checkRow)) {
            $maxCd["NAMECD2"] = $checkRow["NAMECD2"];
        } else {
            $query  = "SELECT MAX(int(namecd2))+1 AS NAMECD2 FROM name_mst ";
            $query .= " WHERE namecd1 = '" .$namecd. "' ";

            $maxCd = $db->getRow($query, DB_FETCHMODE_ASSOC);

            $dataMst["NAMECD1"][TEXT]        = $namecd;
            $dataMst["NAMECD2"][TEXT]        = sprintf("%02d",$maxCd["NAMECD2"]);
            $dataMst["NAME1"][TEXT]          = $fields["CONTENTSTEXT"];
            $dataMst["REGISTERCD"][TEXT]     = STAFFCD;
            $dataMst["UPDATED"][NUMBER]      = "sysdate()";

            $query = Query::insertSQL($dataMst, "NAME_MST");
            $db->query($query);

            $dataYdat["YEAR"][TEXT]           = CTRL_YEAR;
            $dataYdat["NAMECD1"][TEXT]        = $namecd;
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
    function &getInsertQuery($model,$fields,$schregno)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        //NO001-->
        $credits = $fields["CREDITS"];
        //NO001<--
        $db = Query::dbCheckOut();

        $query = knjh110Query::getMaxSeq($schregno);
        $result = $db->query($query);

        $seq = 1;
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $seq = $row["SEQ"];
        }

        $result->free();

        $data["YEAR"][TEXT]           = CTRL_YEAR;
        $data["REGDDATE"]["TIMESTAMP"]  = strtr($fields["REGDDATE"],"/","-");
        $data["SCHREGNO"][TEXT]       = $schregno;
        $data["SEQ"][NUMBER]          = $seq;   //NO001
        
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["CLASSCD"][TEXT]        = $subclass_array[0];
            $data["SCHOOL_KIND"][TEXT]    = $subclass_array[1];
            $data["CURRICULUM_CD"][TEXT]  = $subclass_array[2];
            $data["SUBCLASSCD"][TEXT]     = $subclass_array[3];
        } else {
            $data["SUBCLASSCD"][TEXT]     = $fields["SUBCLASSCD"];
        }
        $data["CONDITION_DIV"][TEXT]  = $fields["CONDITION_DIV"];   //NO001
        //NO001
        if ($fields["CONDITION_DIV"] != 3){
            $data["CONTENTS"][TEXT]       = sprintf("%02d",$fields["CONTENTS1"]);
        }else {
            $data["CONTENTS"][TEXT]       = $fields["CONTENTS2"];
        }
        $data["REMARK"][TEXT]         = $fields["REMARK"];
        $data["CREDITS"][NUMBER]      = $credits;               //NO001
        $data["REGISTERCD"][TEXT]     = STAFFCD;
        $data["UPDATED"][NUMBER]      = "sysdate()";
        $query = Query::insertSQL($data, "schreg_qualified_dat");
        $db->query($query);
        Query::dbCheckIn($db);

        return $seq;
    }

    // DELETE
    function &getDeleteQuery($regddate,$schregno,$subclasscd,$seq)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM schreg_qualified_dat ";
        $query .= "   WHERE ";
        $query .= "   YEAR     = '" .CTRL_YEAR. "'";
        $query .= "   AND SEQ = ".$seq."";  //NO001
        $query .= "   AND SCHREGNO = '" .$schregno. "'";
        $db->query($query);
        Query::dbCheckIn($db);
        return $result;
    }
}
?>
