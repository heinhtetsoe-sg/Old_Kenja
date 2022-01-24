<?php

require_once('for_php7.php');
class knjz068bQuery extends Query
{
    //IB年度一覧取得
    public function getIBYear()
    {
        $query  = " WITH THIS_YEAR(LABEL, VALUE) AS (VALUES('".CTRL_YEAR."', '".CTRL_YEAR."')) ";
        $query .= " SELECT ";
        $query .= "     LABEL, ";
        $query .= "     VALUE ";
        $query .= " FROM ";
        $query .= "     THIS_YEAR ";
        $query .= " UNION ";
        $query .= " SELECT DISTINCT ";
        $query .= "     IBYEAR AS LABEL, ";
        $query .= "     IBYEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "     IBSUBCLASS_UNIT_DAT ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //IB学年一覧取得
    public function getIBGrade($model, $flg)
    {
        $query  = " SELECT ";
        if ($flg == "check") {
            $query .= "     COUNT(*) ";
        } else {
            $query .= "     GRADE AS VALUE, ";
            $query .= "     GRADE_NAME1 AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->ibyear."' ";
        if ($flg == "list") {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //IBコース一覧取得
    public function getIBPrgCourse($model, $flg)
    {
        $query  = " SELECT ";
        if ($flg == "check") {
            $query .= "     COUNT(*) ";
        } else {
            $query .= "     NAME1 AS VALUE, ";
            $query .= "     ABBV1 AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->ibyear."' ";
        $query .= " AND NAMECD1 = 'A034' ";
        $query .= " AND '".$model->ibgrade."' BETWEEN NAME2 AND NAME3 ";
        if ($flg == "list") {
            $query .= " ORDER BY ";
            $query .= "     NAMECD2 ";
        }

        return $query;
    }

    //左フレームのリスト
    public function getList($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.IBCLASSCD, ";
        $query .= "     T1.IBPRG_COURSE, ";
        $query .= "     T1.IBCURRICULUM_CD, ";
        $query .= "     T1.IBSUBCLASSCD, ";
        $query .= "     L2.IBSUBCLASSABBV_ENG, ";
        $query .= "     L2.IBSUBCLASSNAME_ENG, ";
        $query .= "     L1.NAMECD2, ";
        $query .= "     COUNT(*) AS IBSEQ_CNT ";
        $query .= " FROM ";
        $query .= "     IBSUBCLASS_UNIT_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'A034' ";
        $query .= "                          AND L1.NAME1   = T1.IBPRG_COURSE ";
        $query .= "     LEFT JOIN IBSUBCLASS_MST L2 ON L2.IBCLASSCD         = T1.IBCLASSCD ";
        $query .= "                                AND L2.IBPRG_COURSE      = T1.IBPRG_COURSE ";
        $query .= "                                AND L2.IBCURRICULUM_CD   = T1.IBCURRICULUM_CD ";
        $query .= "                                AND L2.IBSUBCLASSCD      = T1.IBSUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.IBYEAR       = '".$model->ibyear."' AND ";
        $query .= "     T1.IBGRADE      = '".$model->ibgrade."' AND ";
        $query .= "     T1.IBPRG_COURSE = '".$model->ibprg_course."' ";
        $query .= " GROUP BY ";
        $query .= "     T1.IBCLASSCD, ";
        $query .= "     T1.IBPRG_COURSE, ";
        $query .= "     T1.IBCURRICULUM_CD, ";
        $query .= "     T1.IBSUBCLASSCD, ";
        $query .= "     L2.IBSUBCLASSABBV_ENG, ";
        $query .= "     L2.IBSUBCLASSNAME_ENG, ";
        $query .= "     L1.NAMECD2 ";
        $query .= " ORDER BY ";
        $query .= "     L1.NAMECD2, ";
        $query .= "     T1.IBPRG_COURSE, ";
        $query .= "     T1.IBCURRICULUM_CD, ";
        $query .= "     T1.IBCLASSCD, ";
        $query .= "     T1.IBSUBCLASSCD ";

        return $query;
    }

    //IBSUBCLASS_UNIT_DATからリンク番号を取得
    public function getIBSubclassUnitDatLinkNo($ibyear, $ibgrade, $ibclasscd, $ibprg_course, $ibcurriculum_cd, $ibsubclasscd)
    {
        $query  = " SELECT ";
        $query .= "     MIN(LINK_NO) AS LINK_NO ";
        $query .= " FROM ";
        $query .= "     IBSUBCLASS_UNIT_DAT ";
        $query .= " WHERE ";
        $query .= "     IBYEAR          = '".$ibyear."' AND ";
        $query .= "     IBGRADE         = '".$ibgrade."' AND ";
        $query .= "     IBCLASSCD       = '".$ibclasscd."' AND ";
        $query .= "     IBPRG_COURSE    = '".$ibprg_course."' AND ";
        $query .= "     IBCURRICULUM_CD = '".$ibcurriculum_cd."' AND ";
        $query .= "     IBSUBCLASSCD    = '".$ibsubclasscd."' ";

        return $query;
    }

    //IBSUBCLASS_UNIT_DATからデータ件数を取得
    public function getIBSubclassUnitDatCnt($link_no)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     IBSUBCLASS_UNIT_DAT ";
        $query .= " WHERE ";
        $query .= "     LINK_NO = '".$link_no."' ";

        return $query;
    }

    //IBSUBCLASS_UNIT_DATからデータ件数(年度毎)を取得
    public function getIBSubclassUnitDatCnt2($link_no)
    {
        $query  = " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     COUNT(*) AS IBSEQ_CNT ";
        $query .= " FROM ";
        $query .= "     IBSUBCLASS_UNIT_DAT ";
        $query .= " WHERE ";
        $query .= "     LINK_NO = '".$link_no."' ";
        $query .= " GROUP BY ";
        $query .= "     YEAR ";
        $query .= " ORDER BY ";
        $query .= "     YEAR ";

        return $query;
    }

    //IBSUBCLASS_UNIT_DATからデータ1件を取得
    public function getIBSubclassUnitDatList($link_no, $ibseq)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     IBSUBCLASS_UNIT_DAT ";
        $query .= " WHERE ";
        $query .= "     LINK_NO = '".$link_no."' AND ";
        $query .= "     IBSEQ = ".$ibseq." ";

        return $query;
    }

    //IBSUBCLASS_UNIT_DATからデータ取得
    public function getIBSubclassUnitDat($ibyear, $ibgrade, $ibclasscd, $ibprg_course, $ibcurriculum_cd, $ibsubclasscd, $ibseq, $flg)
    {
        $query  = " SELECT DISTINCT ";
        if ($flg == "row") {
            $query .= "     IBCLASSCD, ";
            $query .= "     IBPRG_COURSE, ";
            $query .= "     IBCURRICULUM_CD, ";
            $query .= "     IBSUBCLASSCD ";
        } elseif ($flg == "cnt") {
            $query .= "     COUNT(*) ";
        } else {
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     IBSUBCLASS_UNIT_DAT ";
        $query .= " WHERE ";
        $query .= "     IBYEAR          = '".$ibyear."' AND ";
        $query .= "     IBGRADE         = '".$ibgrade."' AND ";
        $query .= "     IBCLASSCD       = '".$ibclasscd."' AND ";
        $query .= "     IBPRG_COURSE    = '".$ibprg_course."' AND ";
        $query .= "     IBCURRICULUM_CD = '".$ibcurriculum_cd."' AND ";
        $query .= "     IBSUBCLASSCD    = '".$ibsubclasscd."' ";
        if ($flg == "list") {
            $query .= "     AND ";
            $query .= "     IBSEQ           = ".$ibseq." ";
        }

        return $query;
    }

    //IB科目一覧取得
    public function getIBSubclasscd($model, $flg)
    {
        $query  = " SELECT DISTINCT ";
        if ($flg == "check") {
            $query .= "     COUNT(*) ";
        } else {
            $query .= "     T1.IBCLASSCD || '-' || T1.IBPRG_COURSE || '-' || T1.IBCURRICULUM_CD || '-' || T1.IBSUBCLASSCD AS VALUE, ";
            $query .= "     T1.IBCLASSCD || '-' || T1.IBPRG_COURSE || '-' || T1.IBCURRICULUM_CD || '-' || T1.IBSUBCLASSCD || ' ' || T1.IBSUBCLASSNAME_ENG AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     V_IBSUBCLASS_MST T1";
        $query .= "     LEFT JOIN IBSUBCLASS_REPLACE_DAT L1 ON L1.IBYEAR = T1.IBYEAR ";
        $query .= "                                        AND L1.IBCLASSCD = T1.IBCLASSCD ";
        $query .= "                                        AND L1.IBPRG_COURSE = T1.IBPRG_COURSE ";
        $query .= "                                        AND L1.IBCURRICULUM_CD = T1.IBCURRICULUM_CD ";
        $query .= "                                        AND L1.IBSUBCLASSCD = T1.IBSUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.IBYEAR          = '".$model->ibyear."' ";
        $query .= " AND T1.IBPRG_COURSE    = '".$model->ibprg_course."' ";
        $query .= " AND L1.IBGRADE = '".$model->ibgrade."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= " AND T1.IBCLASSCD || T1.IBPRG_COURSE IN ";
            $query .= "                             (SELECT  ";
            $query .= "                                  ST.FIELD2 || ST.FIELD3 ";
            $query .= "                              FROM  ";
            $query .= "                                  STAFF_DETAIL_MST ST  ";
            $query .= "                              WHERE  ";
            $query .= "                                  T1.IBYEAR = ST.YEAR  ";
            $query .= "                                  AND ST.STAFFCD = '".STAFFCD."'  ";
            $query .= "                                  AND ST.STAFF_SEQ IN ('005', '006', '007')  ";
            $query .= "                                  AND ST.FIELD1 = '1050')  ";
        }
        if ($flg == "list") {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }
        return $query;
    }

    //科目一覧取得
    public function getSubclasscd($model, $flg, $year, $grade)
    {
        if ($model->cmd === 'edit') {
            $set_ibclasscd = $model->ibclasscd;
            $set_ibprg_course = $model->ibprg_course;
            $set_ibcurriculum_cd = $model->ibcurriculum_cd;
            $set_ibsubclasscd = $model->ibsubclasscd;
        } else {
            $set_ibclasscd = $model->field["IBCLASSCD"];
            $set_ibprg_course = $model->field["IBPRG_COURSE"];
            $set_ibcurriculum_cd = $model->field["IBCURRICULUM_CD"];
            $set_ibsubclasscd = $model->field["IBSUBCLASSCD"];
        }

        $query  = "SELECT ";
        if ($flg == "check") {
            $query .= "     COUNT(*) ";
        } else {
            $query .= "    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE, ";
            $query .= "    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || ' ' || T1.SUBCLASSNAME AS LABEL ";
        }
        $query .= "FROM ";
        $query .= "    V_SUBCLASS_MST T1 ";
        $query .= "    INNER JOIN IBSUBCLASS_REPLACE_DAT L1 ON L1.CLASSCD = T1.CLASSCD ";
        $query .= "                                        AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "                                        AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "                                        AND L1.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "                                        AND L1.IBYEAR = T1.YEAR ";
        $query .= "WHERE ";
        $query .= "    L1.IBYEAR = '{$year}' ";
        $query .= "AND L1.IBGRADE = '{$grade}' ";
        $query .= "AND L1.IBCLASSCD = '{$set_ibclasscd}' ";
        $query .= "AND L1.IBPRG_COURSE = '{$set_ibprg_course}' ";
        $query .= "AND L1.IBCURRICULUM_CD = '{$set_ibcurriculum_cd}' ";
        $query .= "AND L1.IBSUBCLASSCD = '{$set_ibsubclasscd}' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= " AND L1.IBCLASSCD || L1.IBPRG_COURSE IN ";
            $query .= "                             (SELECT  ";
            $query .= "                                  ST.FIELD2 || ST.FIELD3 ";
            $query .= "                              FROM  ";
            $query .= "                                  STAFF_DETAIL_MST ST  ";
            $query .= "                              WHERE  ";
            $query .= "                                  T1.YEAR = ST.YEAR  ";
            $query .= "                                  AND ST.STAFFCD = '".STAFFCD."'  ";
            $query .= "                                  AND ST.STAFF_SEQ IN ('005', '006', '007')  ";
            $query .= "                                  AND ST.FIELD1 = '1050')  ";
        }
        if ($flg == "list") {
            $query .= "ORDER BY ";
            $query .= "    T1.SCHOOL_KIND, ";
            $query .= "    T1.CURRICULUM_CD, ";
            $query .= "    T1.CLASSCD, ";
            $query .= "    T1.SUBCLASSCD ";
        }

        return $query;
    }

    //学期一覧取得
    public function getSemesterMst($model, $flg, $year)
    {
        $query  = " SELECT ";
        if ($flg == "check") {
            $query .= "     COUNT(*) ";
        } else {
            $query .= "     SEMESTER AS VALUE, ";
            $query .= "     SEMESTERNAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR      = '".$year."' AND ";
        $query .= "     SEMESTER <> '9'  ";
        if ($flg == "list") {
            $query .= " ORDER BY ";
            $query .= "     SEMESTER ";
        }

        return $query;
    }

    //年度一覧取得
    public function getYear($model)
    {
        $query  = " WITH THIS_YEAR(LABEL, VALUE) AS ( ";
        $query .= "     VALUES('".($model->ibyear-1)."', '".($model->ibyear-1)."') ";
        $query .= "     UNION ";
        $query .= "     VALUES('".$model->ibyear."', '".$model->ibyear."') ";
        $query .= "     UNION ";
        $query .= "     VALUES('".($model->ibyear+1)."', '".($model->ibyear+1)."') ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     LABEL, ";
        $query .= "     VALUE ";
        $query .= " FROM ";
        $query .= "     THIS_YEAR ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //学年一覧取得
    public function getGrade($model, $year)
    {
        $query  = " SELECT ";
        $query .= "     T1.GRADE AS VALUE, ";
        $query .= "     T1.GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST T2, ";
        $query .= "     SCHREG_REGD_GDAT T1 ";
        $query .= " WHERE ";
        $query .= "     T2.YEAR     = '".$year."' AND ";
        $query .= "     T2.NAMECD1  = 'A034' AND ";
        $query .= "     T2.NAME1    = '".$model->ibprg_course."' AND ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.GRADE BETWEEN T2.NAME2 AND T2.NAME3 ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    /**********/
    /* INSERT */
    /**********/
    public function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //リンク番号
        $tmpYearGrade = "";
        $minYearGrade = "9999-99";
        for ($i = 1; $i <= $model->field["IBSEQ_CNT"]; $i++) {
            $tmpYearGrade = $model->field2["YEAR_".$i].'-'.$model->field2["GRADE_".$i];
            if ($model->field2["YEAR_".$i] != "" && $model->field2["GRADE_".$i] != "") {
                if ($tmpYearGrade < $minYearGrade) {
                    $minYearGrade = $tmpYearGrade;
                }
            }
        }
        $link_no = $minYearGrade.'-'.$model->field["IBCLASSCD"].'-'.$model->field["IBPRG_COURSE"].'-'.$model->field["IBCURRICULUM_CD"].'-'.$model->field["IBSUBCLASSCD"];

        //削除
        $query  = " DELETE FROM ";
        $query .= "     IBSUBCLASS_UNIT_DAT ";
        $query .= " WHERE ";
        $query .= "     LINK_NO = '".$link_no."' ";

        $db->query($query);
        
        list($year, $grade) = explode('-', $minYearGrade);
        //追加
        for ($i = 1; $i <= $model->field["IBSEQ_CNT"]; $i++) {
            $data = array();
            if ($model->field2["YEAR_".$i] == "" || $model->field2["GRADE_".$i] == "") {
                $data["IBYEAR"][TEXT]           = $year;
                $data["IBGRADE"][TEXT]          = $grade;
            } else {
                $data["IBYEAR"][TEXT]           = $model->field2["YEAR_".$i];
                $data["IBGRADE"][TEXT]          = $model->field2["GRADE_".$i];
            }
            $data["IBCLASSCD"][TEXT]        = $model->field["IBCLASSCD"];
            $data["IBPRG_COURSE"][TEXT]     = $model->field["IBPRG_COURSE"];
            $data["IBCURRICULUM_CD"][TEXT]  = $model->field["IBCURRICULUM_CD"];
            $data["IBSUBCLASSCD"][TEXT]     = $model->field["IBSUBCLASSCD"];
            $data["IBSEQ"][NUMBER]          = $i;
            $data["YEAR"][TEXT]             = $model->field2["YEAR_".$i];
            $data["SEMESTER"][TEXT]         = $model->field2["SEMESTER_".$i];
            $data["GRADE"][TEXT]            = $model->field2["GRADE_".$i];
            $data["CLASSCD"][TEXT]          = $model->field2["CLASSCD_".$i];
            $data["SCHOOL_KIND"][TEXT]      = $model->field2["SCHOOL_KIND_".$i];
            $data["CURRICULUM_CD"][TEXT]    = $model->field2["CURRICULUM_CD_".$i];
            $data["SUBCLASSCD"][TEXT]       = $model->field2["SUBCLASSCD_".$i];
            $data["LINK_NO"][TEXT]          = $link_no;
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            $query = Query::insertSQL($data, "IBSUBCLASS_UNIT_DAT");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
    }

    /**********/
    /* DELETE */
    /**********/
    public function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();

        $query = knjz068bQuery::getIBSubclassUnitDatLinkNo($model->ibyear, $model->ibgrade, $model->field["IBCLASSCD"], $model->field["IBPRG_COURSE"], $model->field["IBCURRICULUM_CD"], $model->field["IBSUBCLASSCD"]);
        $link_no = $db->getOne($query);

        $query  = " DELETE FROM ";
        $query .= "     IBSUBCLASS_UNIT_DAT ";
        $query .= " WHERE ";
        $query .= "     LINK_NO = '".$link_no."' ";

        $db->query($query);
        Query::dbCheckIn($db);
    }

    //IBSUBCLASS_UNIT_DAT存在チェック(コピー)
    public function checkIBSubclassUnitDat($ibyear)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     IBSUBCLASS_UNIT_DAT ";
        $query .= " WHERE ";
        $query .= "     LINK_NO LIKE '".$ibyear."%' ";

        return $query;
    }

    //前年度データ取得
    public function getPreYearData($model)
    {
        $pre_year = $model->ibyear - 1;

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     IBSUBCLASS_UNIT_DAT ";
        $query .= " WHERE ";
        $query .= "     LINK_NO LIKE '".$pre_year."%' ";

        return $query;
    }

    //IBSUBCLASS_UNIT_DATの重複チェック用
    public function checkIBSubclassUnitDatChouFuku($model, $ibyear, $ibgrade, $ibclasscd, $ibprg_course, $ibcurriculum_cd, $ibsubclasscd, $ibseq)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     IBSUBCLASS_UNIT_DAT ";
        $query .= " WHERE ";
        $query .= "     IBYEAR          = '".$ibyear."' AND ";
        $query .= "     IBGRADE         = '".$ibgrade."' AND ";
        $query .= "     IBCLASSCD       = '".$ibclasscd."' AND ";
        $query .= "     IBPRG_COURSE    = '".$ibprg_course."' AND ";
        $query .= "     IBCURRICULUM_CD = '".$ibcurriculum_cd."' AND ";
        $query .= "     IBSUBCLASSCD    = '".$ibsubclasscd."' AND ";
        $query .= "     IBSEQ           = ".$ibseq." AND ";
        $query .= "     LINK_NO NOT LIKE '".$model->ibyear."%' ";

        return $query;
    }

    //IB学年の存在チェック
    public function getIBGradeCheck($year)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";

        return $query;
    }

    //IBコースの存在チェック
    public function getIBPrgCourseCheck($year)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = 'A034' ";

        return $query;
    }

    //IB科目の存在チェック
    public function getIBSubclasscdCheck($ibyear, $ibclasscd, $ibprg_course, $ibcurriculum_cd, $ibsubclasscd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     V_IBSUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     IBYEAR          = '".$ibyear."' AND ";
        $query .= "     IBCLASSCD       = '".$ibclasscd."' AND ";
        $query .= "     IBPRG_COURSE    = '".$ibprg_course."' AND ";
        $query .= "     IBCURRICULUM_CD = '".$ibcurriculum_cd."' AND ";
        $query .= "     IBSUBCLASSCD    = '".$ibsubclasscd."' ";

        return $query;
    }

    //科目の存在チェック
    public function getSubclasscdCheck($year, $classcd, $school_kind, $curriculum_cd, $subclasscd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR          = '".$year."' AND ";
        $query .= "     CLASSCD       = '".$classcd."' AND ";
        $query .= "     SCHOOL_KIND   = '".$school_kind."' AND ";
        $query .= "     CURRICULUM_CD = '".$curriculum_cd."' AND ";
        $query .= "     SUBCLASSCD    = '".$subclasscd."' ";

        return $query;
    }

    //学期の存在チェック
    public function getSemesterMstCheck($year)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR      = '".$year."' AND ";
        $query .= "     SEMESTER <> '9'  ";

        return $query;
    }

    /********/
    /* COPY */
    /********/
    public function &getCopyQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //前年度データを配列にセット
        $insertData = array();
        $query = knjz068bQuery::getPreYearData($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //各年度＋１
            $row["IBYEAR"]  = $row["IBYEAR"] + 1;
            $row["YEAR"]    = $row["YEAR"] + 1;
            $row["LINK_NO"] = (substr($row["LINK_NO"], 0, 4) + 1) . substr($row["LINK_NO"], 4);
            $insertData[] = $row;
        }

        //チェック
        $errMsg = "";
        $checkarray = array();
        foreach ($insertData as $key => $row) {
            $checkarray[$row["LINK_NO"]] = "OK";
            //存在チェック
            //件数取得（学年）
            //$cnt_grade = $db->getOne(knjz068bQuery::getIBGradeCheck($row["YEAR"]));
            //件数取得（IBコース）
            //$cnt_course = $db->getOne(knjz068bQuery::getIBPrgCourseCheck($row["YEAR"]));
            //件数取得（IB科目）
            $cnt_ibsubclass = $db->getOne(knjz068bQuery::getIBSubclasscdCheck($row["IBYEAR"], $row["IBCLASSCD"], $row["IBPRG_COURSE"], $row["IBCURRICULUM_CD"], $row["IBSUBCLASSCD"]));
            //件数取得（科目）
            $cnt_subclass = $db->getOne(knjz068bQuery::getSubclasscdCheck($row["YEAR"], $row["CLASSCD"], $row["SCHOOL_KIND"], $row["CURRICULUM_CD"], $row["SUBCLASSCD"]));
            //件数取得（学期）
            //$cnt_semester = $db->getOne(knjz068bQuery::getSemesterMstCheck($row["YEAR"]));
            //事前処理チェック
            if ($cnt_ibsubclass == 0 || $cnt_subclass == 0) {
                $errMsg = "sonzai";
                $checkarray[$row["LINK_NO"]] = "NG";
            }
            //重複チェック(DBエラー回避用)
            $cnt = $db->getOne(knjz068bQuery::checkIBSubclassUnitDatChouFuku($model, $row["IBYEAR"], $row["IBGRADE"], $row["IBCLASSCD"], $row["IBPRG_COURSE"], $row["IBCURRICULUM_CD"], $row["IBSUBCLASSCD"], $row["IBSEQ"]));
            if ($cnt > 0) {
                $errMsg = "choufuku";
            }
        }
        if ($errMsg == "choufuku") {
            return $errMsg;
        }

        //削除
        $query  = " DELETE FROM ";
        $query .= "     IBSUBCLASS_UNIT_DAT ";
        $query .= " WHERE ";
        $query .= "     LINK_NO LIKE '".$model->ibyear."%' ";
        $db->query($query);

        //前年度データ(追加)
        foreach ($insertData as $key => $row) {
            if ($checkarray[$row["LINK_NO"]] == "OK") {
                //追加
                $data = array();
                $data["IBYEAR"][TEXT]           = $row["IBYEAR"];
                $data["IBGRADE"][TEXT]          = $row["IBGRADE"];
                $data["IBCLASSCD"][TEXT]        = $row["IBCLASSCD"];
                $data["IBPRG_COURSE"][TEXT]     = $row["IBPRG_COURSE"];
                $data["IBCURRICULUM_CD"][TEXT]  = $row["IBCURRICULUM_CD"];
                $data["IBSUBCLASSCD"][TEXT]     = $row["IBSUBCLASSCD"];
                $data["IBSEQ"][NUMBER]          = $row["IBSEQ"];
                $data["YEAR"][TEXT]             = $row["YEAR"];
                $data["SEMESTER"][TEXT]         = $row["SEMESTER"];
                $data["GRADE"][TEXT]            = $row["GRADE"];
                $data["CLASSCD"][TEXT]          = $row["CLASSCD"];
                $data["SCHOOL_KIND"][TEXT]      = $row["SCHOOL_KIND"];
                $data["CURRICULUM_CD"][TEXT]    = $row["CURRICULUM_CD"];
                $data["SUBCLASSCD"][TEXT]       = $row["SUBCLASSCD"];
                $data["LINK_NO"][TEXT]          = $row["LINK_NO"];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "sysdate()";

                $query = Query::insertSQL($data, "IBSUBCLASS_UNIT_DAT");
                $db->query($query);
            } else {
                $setmesage = $row["LINK_NO"];
            }
        }

        $db->commit();
        Query::dbCheckIn($db);

        return $errMsg;
    }
    
    //CSV出力
    //CSV 書き出し
    public function getCsvData($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.IBYEAR, ";
        $query .= "     T1.IBGRADE, ";
        $query .= "     L1.ABBV1, ";
        $query .= "     L2.IBSUBCLASSNAME_ENG, ";
        $query .= "     T1.IBCLASSCD, ";
        $query .= "     T1.IBPRG_COURSE, ";
        $query .= "     T1.IBCURRICULUM_CD, ";
        $query .= "     T1.IBSUBCLASSCD, ";
        $query .= "     CASE WHEN T1.IBPRG_COURSE = 'M' THEN 'Unit' || CHAR(T1.IBSEQ) ELSE 'Task' || CHAR(T1.IBSEQ) END AS UNIT_TASK, ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.GRADE, ";
        $query .= "     L3.SUBCLASSNAME, ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     IBSUBCLASS_UNIT_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'A034' ";
        $query .= "                          AND L1.NAME1 = T1.IBPRG_COURSE ";
        $query .= "     LEFT JOIN IBSUBCLASS_MST L2 ON L2.IBCLASSCD = T1.IBCLASSCD ";
        $query .= "                                AND L2.IBPRG_COURSE = T1.IBPRG_COURSE ";
        $query .= "                                AND L2.IBCURRICULUM_CD = T1.IBCURRICULUM_CD ";
        $query .= "                                AND L2.IBSUBCLASSCD = T1.IBSUBCLASSCD ";
        $query .= "     LEFT JOIN SUBCLASS_MST L3   ON L3.CLASSCD = T1.CLASSCD ";
        $query .= "                                AND L3.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "                                AND L3.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "                                AND L3.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     IBYEAR = '".$model->ibyear."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.IBYEAR, ";
        $query .= "     T1.IBGRADE, ";
        $query .= "     T1.IBPRG_COURSE, ";
        $query .= "     T1.IBCURRICULUM_CD, ";
        $query .= "     T1.IBCLASSCD, ";
        $query .= "     T1.IBSUBCLASSCD, ";
        $query .= "     T1.IBSEQ ";

        return $query;
    }
}
