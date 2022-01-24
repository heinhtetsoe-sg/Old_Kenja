<?php

require_once('for_php7.php');

class knja071Query extends Query
{
    //学期を取得
    public function getSemester()
    {
        $query  = " SELECT ";
        $query .= "     YEAR || '-' || SEMESTER AS VALUE, ";
        $query .= "     YEAR || '年度 ' || SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR IN ('".CTRL_YEAR."', '".(CTRL_YEAR - 1)."') ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    //課程学科コンボ用取得
    public function getCourseMajor($year)
    {
        $db  = Query::dbCheckOut();
        $opt = array();

        $query  = "SELECT COURSECD, MAJORCD, COURSENAME, MAJORNAME";
        $query .= "  FROM V_COURSE_MAJOR_MST ";
        $query .= " WHERE ";
        $query .= " year = '".$year."'";
        $query .= " ORDER BY COURSECD, MAJORCD";

        $result = $db->query($query);
        $opt[] = array("label" => "", "value" => "");
        //$opt = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["COURSECD"].$row["MAJORCD"].":".$row["COURSENAME"].$row["MAJORNAME"],
                           "value" => $row["COURSECD"]."-".$row["MAJORCD"]);
        }
        Query::dbCheckIn($db);

        return $opt;
    }

    //コースコンボ用取得
    public function getCourseCode($year)
    {
        $db  = Query::dbCheckOut();
        $opt = array();
        $query  = "SELECT COURSECODE, COURSECODENAME FROM V_COURSECODE_MST";
        $query .= " WHERE year = '".$year."'";
        $query .= " ORDER BY COURSECODE";

        $result = $db->query($query);

        $opt[] = array("label" => "", "value" => "");
        //$opt = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["COURSECODE"].":".$row["COURSECODENAME"],
                           "value" => $row["COURSECODE"]);
        }
        Query::dbCheckIn($db);
        return $opt;
    }

    //１レコード取得
    public function getHrName($term, $grade, $hr_class)
    {
        $db  = Query::dbCheckOut();

        $query  = "SELECT HR_NAME FROM schreg_regd_hdat T1";
        $query .= " WHERE year   = '".SUBSTR($term, 0, 4)."'";
        $query .= " AND semester = '".SUBSTR($term, 5)."'";
        $query .= " AND grade    = '".$grade."'";
        $query .= " AND hr_class = '".$hr_class."'";

        $hr_name = $db->getOne($query);

        Query::dbCheckIn($db);
        return $hr_name;
    }

    //１レコード取得
    public function getRow($term, $grade, $hr_class)
    {
        $db  = Query::dbCheckOut();

        $query  = "SELECT * FROM schreg_regd_diteil_hdat ";
        $query .= " WHERE year   = '".SUBSTR($term, 0, 4)."'";
        $query .= " AND semester = '".SUBSTR($term, 5)."'";
        $query .= " AND grade    = '".$grade."'";
        $query .= " AND hr_class = '".$hr_class."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        Query::dbCheckIn($db);
        return $row;
    }

    //職員一覧
    public function selectList($term)
    {
        $query  = "SELECT x.grade, x.hr_class, x.hr_name, x.hr_nameabbv, ";
        $query .= "       x.tr_cd1, a.staffname_show name1, ";
        $query .= "       RD.COURSECD, RD.MAJORCD, RD.COURSECODE, ";
        $query .= "       CM.COURSENAME || CM.MAJORNAME as COURSEMAJORNAME, CC.COURSECODENAME ";
        $query .= " FROM ";
        $query .= "       schreg_regd_hdat x ";
        $query .= " LEFT OUTER JOIN v_staff_mst a ON x.tr_cd1 = a.staffcd AND x.year = a.year";
        $query .= " LEFT OUTER JOIN schreg_regd_diteil_hdat RD ON x.year = RD.year AND x.semester = RD.semester AND x.grade = RD.grade AND x.hr_class = RD.hr_class";
        $query .= " LEFT OUTER JOIN V_COURSE_MAJOR_MST CM ON RD.COURSECD = CM.COURSECD AND RD.MAJORCD = CM.MAJORCD AND x.year = CM.year";
        $query .= " LEFT OUTER JOIN V_COURSECODE_MST CC ON RD.COURSECODE = CC.COURSECODE AND x.year = CC.year";
        $query .= " WHERE x.year     = '".SUBSTR($term, 0, 4)."'";
        $query .= "   AND x.semester = '".SUBSTR($term, 5)."'";
        $query .= " ORDER BY x.grade, x.hr_class ";

        return $query;
    }

    //--- INSERT
    public function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();

        list($COUUSECD, $MAJORCD) = preg_split("/-/", $model->fields["COURSEMAJOR"]);
        $COURSECODE = $model->fields["COURSECODE"];


        $data["YEAR"][TEXT]         = SUBSTR($model->term, 0, 4);
        $data["SEMESTER"][TEXT]     = SUBSTR($model->term, 5);
        $data["GRADE"][TEXT]        = $model->grade;
        $data["HR_CLASS"][TEXT]     = $model->hr_class;
        $data["COURSECD"][TEXT]     = $COUUSECD;
        $data["MAJORCD"][TEXT]      = $MAJORCD;
        $data["COURSECODE"][TEXT]   = $COURSECODE;

        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $query = Query::insertSQL($data, "schreg_regd_diteil_hdat");
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //--- UPDATE
    public function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();

        list($COUUSECD, $MAJORCD) = preg_split("/-/", $model->fields["COURSEMAJOR"]);
        $COURSECODE = $model->fields["COURSECODE"];

        $data["COURSECD"][TEXT]     = $COUUSECD;
        $data["MAJORCD"][TEXT]      = $MAJORCD;
        $data["COURSECODE"][TEXT]   = $COURSECODE;

        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $where  = " WHERE year   = '" .SUBSTR($model->term, 0, 4)."'";
        $where .= " AND semester = '".SUBSTR($model->term, 5)."'";
        $where .= " AND grade    = '".$model->grade."'";
        $where .= " AND hr_class = '".$model->hr_class."'";

        $query = Query::updateSQL($data, "schreg_regd_diteil_hdat", $where);
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    /*クラス編成データの設定学期に参照学期のデータをインサート。
      (設定学期の出席番号は参照学期の出席番号でインサート)*/
    public function insertCfd($term2, $term)
    {
        $query  ="INSERT INTO class_formation_dat";
        $query .="       (schregno,year,semester,grade,hr_class,attendno,coursecd,majorcd,coursecode,registercd,updated) ";
        $query .="SELECT schregno,";
        $query .="       '".SUBSTR($term, 0, 4)."',";
        $query .="       '".SUBSTR($term, 5, 1)."',";
        $query .="       grade,";
        $query .="       hr_class,";
        $query .="       attendno,";
        $query .="       coursecd,";       //課程
        $query .="       majorcd,";        //学科
        $query .="       coursecode,";     //コース
        $query .="       REGISTERCD,";
        $query .="       SYSDATE()";
        $query .="  FROM schreg_regd_dat";
        $query .=" WHERE year ='".SUBSTR($term2, 0, 4)."' AND semester='".SUBSTR($term2, 5, 1)."'";

        return $query;
    }
    //「コピーボタン」押し時の処理
    public function getInsertCopyQuery($model, $term, $term2)
    {
        $db = Query::dbCheckOut();

        $query  ="INSERT INTO SCHREG_REGD_DITEIL_HDAT";
        $query .="       (YEAR,SEMESTER,GRADE,HR_CLASS,COURSECD,MAJORCD,COURSECODE,REGISTERCD,UPDATED) ";
        $query .= " SELECT ";
        $query .="      '".SUBSTR($term, 0, 4)."',";
        $query .="      '".SUBSTR($term, 5, 1)."',";
        $query .= "     GRADE, ";
        $query .= "     HR_CLASS, ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD, ";
        $query .= "     COURSECODE, ";
        $query .="      REGISTERCD,";
        $query .="      SYSDATE()";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DITEIL_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".SUBSTR($term2, 0, 4)."' ";
        $query .= "     AND SEMESTER = '".SUBSTR($term2, 5, 1)."' ";

        $db->query($query);

        Query::dbCheckIn($db);
    }

    //コピー先のデータの件数
    public function diteilHdatCount($model, $term)
    {
        $query .= " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DITEIL_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".SUBSTR($term, 0, 4)."' ";
        $query .= "     AND SEMESTER = '".SUBSTR($term, 5, 1)."' ";

        return $query;
    }
}
