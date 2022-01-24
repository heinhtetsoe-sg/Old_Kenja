<?php
class knjb0030oQuery extends Query {

    /*------------------------左フレーム-----------------------*/

    //学期リスト
    function SelectComb_seme()
    {
        $query = "select year,semester,semestername from semester_mst where semester<>'9' order by year,semester ";

        return $query;
    }

    //講座一覧
    function SelectList_test($term,$group, $model, $asc_or_desc)
    {
        $query  = "SELECT ";
        $query .= "    t1.chaircd, ";
        $query .= "    t1.groupcd, ";
        $query .= "    w3.classcd, ";
        if( $group=="1" )
        {
            $query .= "    w3.classname as groupname, ";
        } else {
            $query .= "    w1.groupname, ";
        }
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    t1.classcd, ";
            $query .= "    t1.school_kind, ";
            $query .= "    t1.curriculum_cd, ";
        }
        $query .= "    t1.subclasscd, ";
        $query .= "    w2.subclassname, ";
        $query .= "    t1.chairname ";
        $query .= "FROM ";
        $query .= "    chair_dat t1 left join v_electclass_mst w1 on w1.year=t1.year and w1.groupcd=t1.groupcd ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    left join v_subclass_mst w2 on w2.year=t1.year and w2.classcd=t1.classcd and w2.school_kind=t1.school_kind and ";
            $query .= "                                   w2.curriculum_cd=t1.curriculum_cd  and w2.subclasscd=t1.subclasscd ";
            $query .= "    left join v_class_mst w3 on w3.year=t1.year and w3.classcd=t1.classcd and ";
            $query .= "                                w3.school_kind=t1.school_kind ";
        } else {
            $query .= "    left join v_subclass_mst w2 on w2.year=t1.year and w2.subclasscd=t1.subclasscd ";
            $query .= "    left join v_class_mst w3 on w3.year=t1.year and w3.classcd=substr(t1.subclasscd,1,2) ";
        }
        $query .= "WHERE ";
        $query .= "    t1.year='".SUBSTR($term,0,4)."' AND ";
        $query .= "    t1.semester='".SUBSTR($term,5)."' AND ";
        //絞込み（教科の場合、群コード：０）（群の場合、群コード：０以外）
        if( $group=="1" ) {
            $query .= "    t1.groupcd='0000' ";
            $query .= " ORDER BY ";
            if ($model->sort_name == "url_name") {
                $query .= " T1.CHAIRCD {$asc_or_desc}, ";
            }
            if ($model->sort_name == "subclass_name") {
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= " T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD {$asc_or_desc}, ";
                } else {
                    $query .= " T1.SUBCLASSCD {$asc_or_desc}, ";
                }
                $query .= " T1.CHAIRCD  ASC, ";
            }
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD ASC, ";
            } else {
                $query .= "     T1.SUBCLASSCD ASC, ";
            }
            $query .= "     T1.GROUPCD ASC ";

        } else {

            $query .= "    T1.GROUPCD<>'0000' ";
            $query .= "ORDER BY ";
            if ($model->sort_name == "url_name") {
                $query .= " T1.CHAIRCD {$asc_or_desc}, ";
            }
            if ($model->sort_name == "subclass_name") {
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= " T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD {$asc_or_desc}, ";
                } else {
                    $query .= " T1.SUBCLASSCD {$asc_or_desc}, ";
                }
                $query .= " T1.CHAIRCD  ASC, ";
            }
            if ($model->sort_name == "group_name") {
                $query .= " T1.GROUPCD {$asc_or_desc}, ";
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= " T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD ASC, ";
                } else {
                    $query .= " T1.SUBCLASSCD ASC, ";
                }
                $query .= " T1.CHAIRCD ASC, ";
            }
            $query .= "     T1.GROUPCD ASC, ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD ASC ";
            } else {
                $query .= "     T1.SUBCLASSCD ASC ";
            }

        }

        return $query;
    }

    //コピー先に講座データが存在するかをチェック----------
    function getChairDatcount($term)
    {
        $query  = "SELECT COUNT(*) FROM chair_dat";
        $query .=" WHERE year ='".SUBSTR($term,0,4)."' AND semester='".SUBSTR($term,5,1)."'";

        return $query;
    }

    //コピー元の講座データが存在するかをチェック
    function Count($term2)
    {
        $db = Query::dbCheckOut();

        $query  = "SELECT COUNT(*) FROM chair_dat";
        $query .=" WHERE year ='".SUBSTR($term2,0,4)."' AND semester='".SUBSTR($term2,5,1)."'";

        $count = $db->getOne($query);
        return $count;
        Query::dbCheckIn($db);
    }
    //対象年度の在籍ヘッダデータおよび在籍データの存在チェック---2005.02.23Add
    function countSchregRegd($term,$table)
    {
        $db = Query::dbCheckOut();

        $query  = "SELECT COUNT(*) FROM ".$table."";
        $query .=" WHERE year ='".SUBSTR($term,0,4)."' AND semester='".SUBSTR($term,5,1)."'";

        $count = $db->getOne($query);
        return $count;
        Query::dbCheckIn($db);
    }

    //参照先の講座受講生徒データが妥当なデータか確かめる(DBエラー(重複)で落とさないため)
    function checkChairStdDat($term2) {
        $db = Query::dbCheckOut();

        $query  = " WITH COUNT_CHAIR_STD_DAT AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.CHAIRCD, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     COUNT(*) AS COUNT ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT T1 ";
        $query .= " INNER JOIN ";
        $query .= "     SEMESTER_MST L1 ON ";
        $query .= "         T1.YEAR       = L1.YEAR AND ";
        $query .= "         T1.SEMESTER   = L1.SEMESTER AND ";
        $query .= "         T1.APPENDDATE = L1.EDATE ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".SUBSTR($term2,0,4)."' AND ";
        $query .= "     T1.SEMESTER = '".SUBSTR($term2,5,1)."' ";
        $query .= " GROUP BY ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.CHAIRCD, ";
        $query .= "     T1.SCHREGNO ";
        $query .= " HAVING COUNT(*) > 1 ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     COUNT_CHAIR_STD_DAT ";

        $count = $db->getOne($query);
        Query::dbCheckIn($db);
        return $count;
    }

    //「コピーボタン」押し時の処理（メイン）
    function getInsertCopyQuery($model,$term2,$term,$check,$appdate,$appenddate,$checkGrddiv)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //前処理（対象の各データを全て削除）
        $db->query(knjb0030oQuery::Delete_Copy($term, "chair_dat"));
        $db->query(knjb0030oQuery::Delete_Copy($term, "chair_cls_dat"));
        $db->query(knjb0030oQuery::Delete_Copy($term, "chair_stf_dat"));
        $db->query(knjb0030oQuery::Delete_Copy($term, "chair_textbook_dat"));
        $db->query(knjb0030oQuery::Delete_Copy($term, "chair_fac_dat"));
        $db->query(knjb0030oQuery::Delete_Copy($term, "chair_std_dat"));
        //追加処理（参照から対象へ全てインサート）
        $db->query(knjb0030oQuery::Insert_Chr_dat($model,$term2,$term));
        $db->query(knjb0030oQuery::Insert_Cls_dat($term2,$term));
        $db->query(knjb0030oQuery::Insert_Stf_dat($term2,$term));
        $db->query(knjb0030oQuery::Insert_Tex_dat($term2,$term));
        $db->query(knjb0030oQuery::Insert_Fac_dat($term2,$term));
        //生徒もコピー
        if($check==1){
            $db->query(knjb0030oQuery::Insert_Std_dat($term2,$term,$appdate,$appenddate,$checkGrddiv));
        }
        //後処理（対象年度・学期に存在しない各データを削除）
        /* -----↓----- 2005.02.23 Del by nakamoto -----↓----- 
        $db->query(knjb0030oQuery::Delete_Copy_Chrdat($term2,$term));
        $db->query(knjb0030oQuery::Delete_Copy_Clsdat($term2,$term));
        $db->query(knjb0030oQuery::Delete_Copy_Stfdat($term2,$term));
        $db->query(knjb0030oQuery::Delete_Copy_Texdat($term2,$term));
        $db->query(knjb0030oQuery::Delete_Copy_Facdat($term2,$term));
        if($check==1){
            $db->query(knjb0030oQuery::Delete_Copy_Stddat($term2,$term));
        }
         -----↑----- 2005.02.23 Del by nakamoto -----↑----- */

        $db->commit();
        Query::dbCheckIn($db);
    }

    //前処理（対象の各データを全て削除）
    function Delete_Copy($term, $table)
    {
        $query  = "DELETE FROM ".$table." ";
        $query .= " WHERE year     = '".SUBSTR($term,0,4)."'";
        $query .= "   AND semester = '".SUBSTR($term,5)."'";

        return $query;
    }

    //追加処理（参照から対象へ全てインサート）
    //講座データ
    function Insert_Chr_dat($model,$term2,$term)
    {
        $query  =" INSERT INTO chair_dat";
        $query .=" SELECT '".SUBSTR($term,0,4)."',";
        $query .="        '".SUBSTR($term,5,1)."',";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .="        chaircd, groupcd, classcd, school_kind, curriculum_cd, subclasscd, ";
            $query .="        chairname, chairabbv, takesemes, lessoncnt, framecnt,";
        } else {
            $query .="        chaircd, groupcd, subclasscd, chairname, chairabbv, takesemes, lessoncnt, framecnt,";
        }
        $query .="        countflg,";//集計フラグ---05/01/19Add
        $query .="        '".STAFFCD."', SYSDATE()";
        $query .="   FROM chair_dat";
        $query .="  WHERE year = '".SUBSTR($term2,0,4)."' AND semester = '".SUBSTR($term2,5,1)."'";

        return $query;
    }
    //講座受講クラスデータ
    function Insert_Cls_dat($term2,$term)
    {
        $query  =" INSERT INTO chair_cls_dat";
        $query .=" SELECT '".SUBSTR($term,0,4)."',";
        $query .="        '".SUBSTR($term,5,1)."',";
        $query .="        chaircd, groupcd, trgtgrade, trgtclass,";
        $query .="        '".STAFFCD."', SYSDATE()";
        $query .="   FROM chair_cls_dat";
        $query .="  WHERE year = '".SUBSTR($term2,0,4)."' AND semester = '".SUBSTR($term2,5,1)."'";

        return $query;
    }
    //講座担当職員データ
    function Insert_Stf_dat($term2,$term)
    {
        $query  =" INSERT INTO chair_stf_dat";
        $query .=" SELECT '".SUBSTR($term,0,4)."',";
        $query .="        '".SUBSTR($term,5,1)."',";
        $query .="        chaircd, staffcd, chargediv,";
        $query .="        '".STAFFCD."', SYSDATE()";
        $query .="   FROM chair_stf_dat";
        $query .="  WHERE year = '".SUBSTR($term2,0,4)."' AND semester = '".SUBSTR($term2,5,1)."'";

        return $query;
    }
    //講座教科書データ
    function Insert_Tex_dat($term2,$term)
    {
        $query  =" INSERT INTO chair_textbook_dat";
        $query .=" SELECT '".SUBSTR($term,0,4)."',";
        $query .="        '".SUBSTR($term,5,1)."',";
        $query .="        chaircd, textbookcd,";
        $query .="        '".STAFFCD."', SYSDATE()";
        $query .="   FROM chair_textbook_dat";
        $query .="  WHERE year = '".SUBSTR($term2,0,4)."' AND semester = '".SUBSTR($term2,5,1)."'";

        return $query;
    }
    //講座使用施設データ
    function Insert_Fac_dat($term2,$term)
    {
        $query  =" INSERT INTO chair_fac_dat";
        $query .=" SELECT '".SUBSTR($term,0,4)."',";
        $query .="        '".SUBSTR($term,5,1)."',";
        $query .="        chaircd, faccd,";
        $query .="        '".STAFFCD."', SYSDATE()";
        $query .="   FROM chair_fac_dat";
        $query .="  WHERE year = '".SUBSTR($term2,0,4)."' AND semester = '".SUBSTR($term2,5,1)."'";

        return $query;
    }

    //生徒もコピー
    //講座受講生徒データ
    function Insert_Std_dat($term2,$term,$appdate,$appenddate,$checkGrddiv)
    {
        $query  =" INSERT INTO chair_std_dat";
        $query .=" SELECT '".SUBSTR($term,0,4)."',";
        $query .="        '".SUBSTR($term,5,1)."',";
        $query .="        w1.chaircd, w1.schregno, '".$appdate."', '".$appenddate."', w1.row, w1.column,";
        $query .="        '".STAFFCD."', SYSDATE()";
        $query .="   FROM chair_std_dat w1, semester_mst w2 ";
        $query .="  WHERE w1.year = '".SUBSTR($term2,0,4)."' AND w1.semester = '".SUBSTR($term2,5,1)."' AND ";
        $query .="        w1.year = w2.year AND w1.semester = w2.semester AND w1.appenddate = w2.edate ";
        /* -----↓----- 2005.02.23 Add by nakamoto -----↓----- */
        if ($checkGrddiv != 1) {
            $query .="        AND W1.SCHREGNO NOT IN ( ";
            $query .="                SELECT SCHREGNO ";
            $query .="                FROM   SCHREG_BASE_MST K1,SEMESTER_MST K2 ";
            $query .="                WHERE  YEAR='".SUBSTR($term2,0,4)."' AND SEMESTER='".SUBSTR($term2,5,1)."' AND ";
            $query .="                       GRD_DIV IN ('1','2','3') AND GRD_DATE <= EDATE ) ";
        }
        /* -----↑----- 2005.02.23 Add by nakamoto -----↑----- */
        //2004/06/23 naka, 04/11/12Modify naka appenddate->appdate
        /* 04/12/15Modify naka
        $query .= "    (appdate,chaircd) in (SELECT ";
        $query .= "                                max(appdate) appdate, ";
        $query .= "                                chaircd ";
        $query .= "                            FROM ";
        $query .= "                                chair_std_dat ";
        $query .= "                            WHERE ";
        $query .= "                                year='".SUBSTR($term2,0,4)."' AND ";
        $query .= "                                semester='".SUBSTR($term2,5,1)."' ";
        $query .= "                            GROUP BY ";
        $query .= "                                chaircd ";
        $query .= "                            ) ";
        */
        return $query;
    }

    //後処理（対象年度・学期に存在しない各データを削除）
    //講座データ（群コード・科目コード）
    function Delete_Copy_Chrdat($term2, $term)
    {
        $query  = "DELETE FROM chair_dat ";
        $query .= "WHERE ";
        $query .= "    year='".SUBSTR($term,0,4)."' AND ";
        $query .= "    semester='".SUBSTR($term,5)."' AND ";
        $query .= "    (subclasscd in  (SELECT ";
        $query .= "                        w1.subclasscd ";
        $query .= "                    FROM ";
        $query .= "                        v_subclass_mst w1 ";
        $query .= "                    WHERE ";
        $query .= "                        w1.year='".SUBSTR($term2,0,4)."' AND ";
        $query .= "                        not exists(SELECT ";
        $query .= "                                        * ";
        $query .= "                                    FROM ";
        $query .= "                                        v_subclass_mst w2 ";
        $query .= "                                    WHERE ";
        $query .= "                                        w2.year='".SUBSTR($term,0,4)."' AND ";
        $query .= "                                        w2.subclasscd=w1.subclasscd ";
        $query .= "                                    ) ";
        $query .= "                    ) OR ";
        $query .= "    groupcd in (SELECT ";
        $query .= "                    w1.groupcd ";
        $query .= "                FROM ";
        $query .= "                    v_electclass_mst w1 ";
        $query .= "                WHERE ";
        $query .= "                    w1.year='".SUBSTR($term2,0,4)."' AND ";
        $query .= "                    not exists(SELECT ";
        $query .= "                                    * ";
        $query .= "                                FROM ";
        $query .= "                                    v_electclass_mst w2 ";
        $query .= "                                WHERE ";
        $query .= "                                    w2.year='".SUBSTR($term,0,4)."' AND ";
        $query .= "                                    w2.groupcd=w1.groupcd ";
        $query .= "                                ) ";
        $query .= "                ) ";
        $query .= "    ) ";

        return $query;
    }
    //講座受講クラスデータ（年組・群コード・講座コード）
    function Delete_Copy_Clsdat($term2, $term)
    {
        $query  = "DELETE FROM chair_cls_dat ";
        $query .= "WHERE ";
        $query .= "    year='".SUBSTR($term,0,4)."' AND ";
        $query .= "    semester='".SUBSTR($term,5)."' AND ";
        $query .= "    (trgtgrade || trgtclass in  (SELECT ";
        $query .= "                        w1.grade || w1.hr_class ";
        $query .= "                    FROM ";
        $query .= "                        schreg_regd_hdat w1 ";
        $query .= "                    WHERE ";
        $query .= "                        w1.year='".SUBSTR($term2,0,4)."' AND ";
        $query .= "                        w1.semester='".SUBSTR($term2,5)."' AND ";
        $query .= "                        not exists(SELECT ";
        $query .= "                                        * ";
        $query .= "                                    FROM ";
        $query .= "                                        schreg_regd_hdat w2 ";
        $query .= "                                    WHERE ";
        $query .= "                                        w2.year='".SUBSTR($term,0,4)."' AND ";
        $query .= "                                        w2.semester='".SUBSTR($term,5)."' AND ";
        $query .= "                                        w2.grade || w2.hr_class = w1.grade || w1.hr_class ";
        $query .= "                                    ) ";
        $query .= "                    ) OR ";
        $query .= "    groupcd in (SELECT ";
        $query .= "                    w1.groupcd ";
        $query .= "                FROM ";
        $query .= "                    v_electclass_mst w1 ";
        $query .= "                WHERE ";
        $query .= "                    w1.year='".SUBSTR($term2,0,4)."' AND ";
        $query .= "                    not exists(SELECT ";
        $query .= "                                    * ";
        $query .= "                                FROM ";
        $query .= "                                    v_electclass_mst w2 ";
        $query .= "                                WHERE ";
        $query .= "                                    w2.year='".SUBSTR($term,0,4)."' AND ";
        $query .= "                                    w2.groupcd=w1.groupcd ";
        $query .= "                                ) ";
        $query .= "                ) OR ";
        $query .= "    chaircd in (SELECT ";
        $query .= "                    w1.chaircd ";
        $query .= "                FROM ";
        $query .= "                    chair_dat w1 ";
        $query .= "                WHERE ";
        $query .= "                    w1.year='".SUBSTR($term2,0,4)."' AND ";
        $query .= "                    w1.semester='".SUBSTR($term2,5)."' AND ";
        $query .= "                    not exists(SELECT ";
        $query .= "                                    * ";
        $query .= "                                FROM ";
        $query .= "                                    chair_dat w2 ";
        $query .= "                                WHERE ";
        $query .= "                                    w2.year='".SUBSTR($term,0,4)."' AND ";
        $query .= "                                    w2.semester='".SUBSTR($term,5)."' AND ";
        $query .= "                                    w2.chaircd=w1.chaircd ";
        $query .= "                                ) ";
        $query .= "                ) ";
        $query .= "    ) ";

        return $query;
    }
    //講座担当職員データ（職員コード・講座コード）
    function Delete_Copy_Stfdat($term2, $term)
    {
        $query  = "DELETE FROM chair_stf_dat ";
        $query .= "WHERE ";
        $query .= "    year='".SUBSTR($term,0,4)."' AND ";
        $query .= "    semester='".SUBSTR($term,5)."' AND ";
        $query .= "    (staffcd in (SELECT ";
        $query .= "                    w1.staffcd ";
        $query .= "                FROM ";
        $query .= "                    v_staff_mst w1 ";
        $query .= "                WHERE ";
        $query .= "                    w1.year='".SUBSTR($term2,0,4)."' AND ";
        $query .= "                    not exists(SELECT ";
        $query .= "                                    * ";
        $query .= "                                FROM ";
        $query .= "                                    v_staff_mst w2 ";
        $query .= "                                WHERE ";
        $query .= "                                    w2.year='".SUBSTR($term,0,4)."' AND ";
        $query .= "                                    w2.staffcd=w1.staffcd ";
        $query .= "                                ) ";
        $query .= "                ) OR ";
        $query .= "    chaircd in (SELECT ";
        $query .= "                    w1.chaircd ";
        $query .= "                FROM ";
        $query .= "                    chair_dat w1 ";
        $query .= "                WHERE ";
        $query .= "                    w1.year='".SUBSTR($term2,0,4)."' AND ";
        $query .= "                    w1.semester='".SUBSTR($term2,5)."' AND ";
        $query .= "                    not exists(SELECT ";
        $query .= "                                    * ";
        $query .= "                                FROM ";
        $query .= "                                    chair_dat w2 ";
        $query .= "                                WHERE ";
        $query .= "                                    w2.year='".SUBSTR($term,0,4)."' AND ";
        $query .= "                                    w2.semester='".SUBSTR($term,5)."' AND ";
        $query .= "                                    w2.chaircd=w1.chaircd ";
        $query .= "                                ) ";
        $query .= "                ) ";
        $query .= "    ) ";

        return $query;
    }
    //講座使用施設データ（施設コード・講座コード）
    function Delete_Copy_Facdat($term2, $term)
    {
        $query  = "DELETE FROM chair_fac_dat ";
        $query .= "WHERE ";
        $query .= "    year='".SUBSTR($term,0,4)."' AND ";
        $query .= "    semester='".SUBSTR($term,5)."' AND ";
        $query .= "    (faccd in (SELECT ";
        $query .= "                    w1.faccd ";
        $query .= "                FROM ";
        $query .= "                    v_facility_mst w1 ";
        $query .= "                WHERE ";
        $query .= "                    w1.year='".SUBSTR($term2,0,4)."' AND ";
        $query .= "                    not exists(SELECT ";
        $query .= "                                    * ";
        $query .= "                                FROM ";
        $query .= "                                    v_facility_mst w2 ";
        $query .= "                                WHERE ";
        $query .= "                                    w2.year='".SUBSTR($term,0,4)."' AND ";
        $query .= "                                    w2.faccd=w1.faccd ";
        $query .= "                                ) ";
        $query .= "                ) OR ";
        $query .= "    chaircd in (SELECT ";
        $query .= "                    w1.chaircd ";
        $query .= "                FROM ";
        $query .= "                    chair_dat w1 ";
        $query .= "                WHERE ";
        $query .= "                    w1.year='".SUBSTR($term2,0,4)."' AND ";
        $query .= "                    w1.semester='".SUBSTR($term2,5)."' AND ";
        $query .= "                    not exists(SELECT ";
        $query .= "                                    * ";
        $query .= "                                FROM ";
        $query .= "                                    chair_dat w2 ";
        $query .= "                                WHERE ";
        $query .= "                                    w2.year='".SUBSTR($term,0,4)."' AND ";
        $query .= "                                    w2.semester='".SUBSTR($term,5)."' AND ";
        $query .= "                                    w2.chaircd=w1.chaircd ";
        $query .= "                                ) ";
        $query .= "                ) ";
        $query .= "    ) ";

        return $query;
    }
    //講座教科書データ（教科書コード・講座コード）
    function Delete_Copy_Texdat($term2, $term)
    {
        $query  = "DELETE FROM chair_textbook_dat ";
        $query .= "WHERE ";
        $query .= "    year='".SUBSTR($term,0,4)."' AND ";
        $query .= "    semester='".SUBSTR($term,5)."' AND ";
        $query .= "    (textbookcd in (SELECT ";
        $query .= "                    w1.textbookcd ";
        $query .= "                FROM ";
        $query .= "                    v_textbook_mst w1 ";
        $query .= "                WHERE ";
        $query .= "                    w1.year='".SUBSTR($term2,0,4)."' AND ";
        $query .= "                    not exists(SELECT ";
        $query .= "                                    * ";
        $query .= "                                FROM ";
        $query .= "                                    v_textbook_mst w2 ";
        $query .= "                                WHERE ";
        $query .= "                                    w2.year='".SUBSTR($term,0,4)."' AND ";
        $query .= "                                    w2.textbookcd=w1.textbookcd ";
        $query .= "                                ) ";
        $query .= "                ) OR ";
        $query .= "    chaircd in (SELECT ";
        $query .= "                    w1.chaircd ";
        $query .= "                FROM ";
        $query .= "                    chair_dat w1 ";
        $query .= "                WHERE ";
        $query .= "                    w1.year='".SUBSTR($term2,0,4)."' AND ";
        $query .= "                    w1.semester='".SUBSTR($term2,5)."' AND ";
        $query .= "                    not exists(SELECT ";
        $query .= "                                    * ";
        $query .= "                                FROM ";
        $query .= "                                    chair_dat w2 ";
        $query .= "                                WHERE ";
        $query .= "                                    w2.year='".SUBSTR($term,0,4)."' AND ";
        $query .= "                                    w2.semester='".SUBSTR($term,5)."' AND ";
        $query .= "                                    w2.chaircd=w1.chaircd ";
        $query .= "                                ) ";
        $query .= "                ) ";
        $query .= "    ) ";

        return $query;
    }
    //講座受講生徒データ（学籍番号・講座コード）
    function Delete_Copy_Stddat($term2, $term)
    {
        $query  = "DELETE FROM chair_std_dat ";
        $query .= "WHERE ";
        $query .= "    year='".SUBSTR($term,0,4)."' AND ";
        $query .= "    semester='".SUBSTR($term,5)."' AND ";
        $query .= "    (schregno in  (SELECT ";
        $query .= "                        schregno ";
        $query .= "                    FROM ";
        $query .= "                        schreg_base_mst ";
        $query .= "                    WHERE ";
        $query .= "                        grd_div in ('1','2','3') AND ";
        $query .= "                        grd_date <= '".CTRL_DATE."' ";
        $query .= "                    ) OR  ";
        $query .= "    chaircd in (SELECT ";
        $query .= "                    w1.chaircd ";
        $query .= "                FROM ";
        $query .= "                    chair_dat w1 ";
        $query .= "                WHERE ";
        $query .= "                    w1.year='".SUBSTR($term2,0,4)."' AND ";
        $query .= "                    w1.semester='".SUBSTR($term2,5)."' AND ";
        $query .= "                    not exists(SELECT ";
        $query .= "                                    * ";
        $query .= "                                FROM ";
        $query .= "                                    chair_dat w2 ";
        $query .= "                                WHERE ";
        $query .= "                                    w2.year='".SUBSTR($term,0,4)."' AND ";
        $query .= "                                    w2.semester='".SUBSTR($term,5)."' AND ";
        $query .= "                                    w2.chaircd=w1.chaircd ";
        $query .= "                                ) ";
        $query .= "                ) ";
        $query .= "    ) ";

        return $query;
    }

    /*------------------------右フレーム-----------------------*/

    //リストコンボ用取得
    //科目
    function getSubclass($model,$year)
    {
        $db  = Query::dbCheckOut();
        $opt = array();
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query  = "SELECT DISTINCT ";
            $query .= "       classcd || '-' || school_kind || '-' || curriculum_cd || '-' || subclasscd as subclasscd, ";
            $query .= "       subclassname ";
            $query .= "  FROM v_subclass_mst ";
            $query .= " WHERE year   = '".$year."' ";
            $query .= " ORDER BY subclasscd ";
        } else {
            $query  = "SELECT DISTINCT subclasscd, subclassname ";
            $query .= "  FROM v_subclass_mst ";
            $query .= " WHERE year   = '".$year."' ";
            $query .= " ORDER BY subclasscd";
        }

        $result = $db->query($query);//naka 05.12.23
//        $result = $db->query($query, DB_FETCHMODE_ASSOC);

        $opt[] = array("label" => "", "value" => "");
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["SUBCLASSCD"]."　".$row["SUBCLASSNAME"],
                           "value" => $row["SUBCLASSCD"]);
        }
        Query::dbCheckIn($db);
        return $opt;
    }
    //履修期間区分
    function getTakesemes($year)
    {
        $db  = Query::dbCheckOut();
        $opt = array();
        $query  = "SELECT DISTINCT semester, semestername ";
        $query .= "  FROM semester_mst ";
        $query .= " WHERE year   = '".$year."' and semester<>'9' ";
        $query .= " ORDER BY semester";

        $result = $db->query($query);//naka 05.12.23
//        $result = $db->query($query, DB_FETCHMODE_ASSOC);

        $opt[] = array("label" => "0　通年", "value" => "0");
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["SEMESTER"]."　".$row["SEMESTERNAME"],
                           "value" => $row["SEMESTER"]);
        }
        Query::dbCheckIn($db);
        return $opt;
    }
    //群
    function getGroup($year)
    {
        $db  = Query::dbCheckOut();
        $opt = array();
        $query  = "SELECT DISTINCT groupcd, groupname ";
        $query .= "  FROM v_electclass_mst ";
        $query .= " WHERE year   = '".$year."' ";
        $query .= " ORDER BY groupcd";

        $result = $db->query($query);//naka 05.12.23
//        $result = $db->query($query, DB_FETCHMODE_ASSOC);

        $opt[] = array("label" => "0000　なし（ＨＲ）", "value" => "0000");
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["GROUPCD"]."　".$row["GROUPNAME"],
                           "value" => $row["GROUPCD"]);
        }
        Query::dbCheckIn($db);
        return $opt;
    }

    //選択ダイアログ
    //受講クラス
    function selectQuerySubForm1($term)
    {
        $query  = "SELECT ";
        $query .= "    grade || hr_class as grade_class, ";
        $query .= "    hr_name, ";
        $query .= "    hr_nameabbv ";
        $query .= "FROM ";
        $query .= "    schreg_regd_hdat ";
        $query .= "WHERE ";
        $query .= "    year='".SUBSTR($term,0,4)."' AND ";
        $query .= "    semester='".SUBSTR($term,5)."' ";
        $query .= " ORDER BY 1 ";

        return $query;
    }
    //科目担任
    function selectQuerySubForm2($term, $staffcd, $check)
    {
        $query  = "SELECT ";
        $query .= "    t1.sectioncd, ";
        $query .= "    t2.sectionabbv, ";
        $query .= "    t1.staffcd, ";
        $query .= "    t1.staffname_show ";
        $query .= "FROM ";
        $query .= "    v_staff_mst t1 ";
        $query .= "    left join v_section_mst t2 on t2.year =t1.year and t2.sectioncd=t1.sectioncd ";
        $query .= "WHERE ";
        $query .= "    t1.year='".SUBSTR($term,0,4)."' ";
        if($check){
            $query .= "    AND t1.staffcd in ('".implode("','",$staffcd)."') ";
        }
        $query .= " ORDER BY 1 ";

        return $query;
    }

    //資格科目
    function getStaffClass($model, $subclass, $flg="")
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     STAFFCD ";
        $query .= " FROM ";
        $query .= "     STAFF_CLASS_MST ";
        if($flg){
            $query .= " WHERE ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                list($classcd, $school_kind, $curriculum_cd, $subclasscd) = explode("-", $subclass);
                $query .= "     CLASSCD         = '".$classcd."' AND ";
                $query .= "     SCHOOL_KIND     = '".$school_kind."' ";
            } else {
                $query .= "     CLASSCD = SUBSTR('".$subclass."',1,2) ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     STAFFCD ";

        return $query;
    }

    //使用施設
    function selectQuerySubForm3($term)
    {
        $query  = "SELECT ";
        $query .= "    faccd, ";
        $query .= "    facilityname, ";
        $query .= "    facilityabbv ";
        $query .= "FROM ";
        $query .= "    v_facility_mst ";
        $query .= "WHERE ";
        $query .= "    year='".SUBSTR($term,0,4)."' ";
        $query .= " ORDER BY 1 ";

        return $query;
    }
    //教科書
    function selectQuerySubForm4($term)
    {
        $query  = "SELECT ";
        $query .= "    textbookcd, ";
        $query .= "    textbookname, ";
        $query .= "    textbookabbv ";
        $query .= "FROM ";
        $query .= "    v_textbook_mst ";
        $query .= "WHERE ";
        $query .= "    year='".SUBSTR($term,0,4)."' ";
        $query .= " ORDER BY 1 ";

        return $query;
    }

    //１レコード取得
    //（講座）
    function getRow_test($term, $chaircd)
    {
        $db  = Query::dbCheckOut();

        $query  = "SELECT * FROM chair_dat ";
        $query .= "WHERE year   = '".SUBSTR($term,0,4)."' ";
        $query .= "AND semester = '".SUBSTR($term,5)."' ";
        $query .= "AND chaircd  = '".$chaircd."' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }
    //（授業回数）
    function getRow_lesson($term, $groupcd)
    {
        $db  = Query::dbCheckOut();

        $query  = "SELECT distinct lessoncnt,framecnt FROM chair_dat ";
        $query .= "WHERE year   = '".SUBSTR($term,0,4)."' ";
        $query .= "AND semester = '".SUBSTR($term,5)."' ";
        $query .= "AND groupcd  = '".$groupcd."' ";
        $query .= "order by lessoncnt desc ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //受講クラス取得
    function getGradeClass($term, $chaircd, $groupcd)
    {
        $query  = "SELECT ";
        $query .= "    t1.trgtgrade || t1.trgtclass as grade_class, ";
        $query .= "    t2.hr_nameabbv ";
        $query .= "FROM ";
        $query .= "    chair_cls_dat t1, ";
        $query .= "    schreg_regd_hdat t2 ";
        $query .= "WHERE ";
        $query .= "    t1.year='".SUBSTR($term,0,4)."' AND ";
        $query .= "    t1.semester='".SUBSTR($term,5)."' AND ";
        if ($groupcd > "0000") {
            $query .= "    t1.groupcd='".$groupcd."' AND ";
        } else {
            $query .= "    t1.chaircd='".$chaircd."' AND ";
        }
        $query .= "    t1.year=t2.year AND ";
        $query .= "    t1.semester=t2.semester AND ";
        $query .= "    t1.trgtgrade=t2.grade AND ";
        $query .= "    t1.trgtclass=t2.hr_class ";
        $query .= " ORDER BY 1 ";

        return $query;
    }
    //科目担任取得
    function getStaff($term, $chaircd)
    {
        $query  = "SELECT ";
        $query .= "    t1.staffcd, ";
        $query .= "    value(t1.chargediv,0) as chargediv, ";
        $query .= "    t2.staffname_show ";
        $query .= "FROM ";
        $query .= "    chair_stf_dat t1, ";
        $query .= "    v_staff_mst t2 ";
        $query .= "WHERE ";
        $query .= "    t1.year='".SUBSTR($term,0,4)."' AND ";
        $query .= "    t1.semester='".SUBSTR($term,5)."' AND ";
        $query .= "    t1.chaircd='".$chaircd."' AND ";
        $query .= "    t1.year=t2.year AND ";
        $query .= "    t1.staffcd=t2.staffcd ";
        $query .= " ORDER BY t1.staffcd ";

        return $query;
    }
    //使用施設取得
    function getFac($term, $chaircd)
    {
        $query  = "SELECT ";
        $query .= "    t1.faccd, ";
        $query .= "    t2.facilityabbv ";
        $query .= "FROM ";
        $query .= "    chair_fac_dat t1, ";
        $query .= "    v_facility_mst t2 ";
        $query .= "WHERE ";
        $query .= "    t1.year='".SUBSTR($term,0,4)."' AND ";
        $query .= "    t1.semester='".SUBSTR($term,5)."' AND ";
        $query .= "    t1.chaircd='".$chaircd."' AND ";
        $query .= "    t1.year=t2.year AND ";
        $query .= "    t1.faccd=t2.faccd ";
        $query .= " ORDER BY t1.faccd";

        return $query;
    }
    //教科書取得
    function getTextbook($term, $chaircd)
    {
        $query  = "SELECT ";
        $query .= "    t1.textbookcd, ";
        $query .= "    t2.textbookabbv ";
        $query .= "FROM ";
        $query .= "    chair_textbook_dat t1, ";
        $query .= "    v_textbook_mst t2 ";
        $query .= "WHERE ";
        $query .= "    t1.year='".SUBSTR($term,0,4)."' AND ";
        $query .= "    t1.semester='".SUBSTR($term,5)."' AND ";
        $query .= "    t1.chaircd='".$chaircd."' AND ";
        $query .= "    t1.year=t2.year AND ";
        $query .= "    t1.textbookcd=t2.textbookcd ";
        $query .= " ORDER BY t1.textbookcd ";

        return $query;
    }
    //--- INSERT（講座データ）
    function Insert_Chair($model)
    {
        $data["YEAR"][TEXT]         = substr($model->term,0,4);
        $data["SEMESTER"][TEXT]     = substr($model->term,5);
        $data["CHAIRCD"][TEXT]      = $model->fields["CHAIRCD"];
        $data["GROUPCD"][TEXT]      = $model->fields["GROUPCD"];
        if ($model->Properties["useCurriculumcd"] == '1') {
            list($classcd, $school_kind, $curriculum_cd, $subclasscd) = explode("-", $model->fields["SUBCLASSCD"]);
            $data["CLASSCD"][TEXT]      = $classcd;
            $data["SCHOOL_KIND"][TEXT]  = $school_kind;
            $data["CURRICULUM_CD"][TEXT]= $curriculum_cd;
            $data["SUBCLASSCD"][TEXT]   = $subclasscd;
        } else {
            $data["SUBCLASSCD"][TEXT]   = $model->fields["SUBCLASSCD"];
        }
        $data["CHAIRNAME"][TEXT]    = $model->fields["CHAIRNAME"];
        $data["CHAIRABBV"][TEXT]    = $model->fields["CHAIRABBV"];
        $data["TAKESEMES"][TEXT]    = $model->fields["TAKESEMES"];
        $data["LESSONCNT"][NUMBER]  = $model->fields["LESSONCNT"];
        $data["FRAMECNT"][NUMBER]   = $model->fields["FRAMECNT"];
        //集計フラグ---05/01/19Add 1:集計する 0:集計しない
        $data["COUNTFLG"][TEXT]     = ($model->fields["COUNTFLG"] == "1") ? $model->fields["COUNTFLG"] : "0";
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "SYSDATE()";

        $query = Query::insertSQL($data, "chair_dat");

        return $query;
    }
    //--- UPDATE（講座データ）
    function Update_Chair($model)
    {
        $data["YEAR"][TEXT]         = substr($model->term,0,4);
        $data["SEMESTER"][TEXT]     = substr($model->term,5);
        $data["CHAIRCD"][TEXT]      = $model->fields["CHAIRCD"];
        $data["GROUPCD"][TEXT]      = $model->fields["GROUPCD"];
        if ($model->Properties["useCurriculumcd"] == '1') {
            list($classcd, $school_kind, $curriculum_cd, $subclasscd) = explode("-", $model->fields["SUBCLASSCD"]);
            $data["CLASSCD"][TEXT]      = $classcd;
            $data["SCHOOL_KIND"][TEXT]  = $school_kind;
            $data["CURRICULUM_CD"][TEXT]= $curriculum_cd;
            $data["SUBCLASSCD"][TEXT]   = $subclasscd;
        } else {
            $data["SUBCLASSCD"][TEXT]   = $model->fields["SUBCLASSCD"];
        }
        $data["CHAIRNAME"][TEXT]    = $model->fields["CHAIRNAME"];
        $data["CHAIRABBV"][TEXT]    = $model->fields["CHAIRABBV"];
        $data["TAKESEMES"][TEXT]    = $model->fields["TAKESEMES"];
        $data["LESSONCNT"][NUMBER]  = $model->fields["LESSONCNT"];
        $data["FRAMECNT"][NUMBER]   = $model->fields["FRAMECNT"];
        //集計フラグ---05/01/19Add 1:集計する 0:集計しない
        $data["COUNTFLG"][TEXT]     = ($model->fields["COUNTFLG"] == "1") ? $model->fields["COUNTFLG"] : "0";
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";
        $where  = " WHERE year   = '".SUBSTR($model->term,0,4)."'";
        $where .= " AND semester = '".SUBSTR($model->term,5)."'";
        $where .= " AND chaircd  = '".$model->fields["CHAIRCD"]."'";

        $query = Query::updateSQL($data, "chair_dat", $where);

        return $query;
    }
    //--- CHEACK（講座データ）
    function Count_Chair($model)
    {
        $query  = "SELECT COUNT(*) FROM chair_dat ";
        $query .= "WHERE year   = '".SUBSTR($model->term,0,4)."' ";
        $query .= "AND semester = '".SUBSTR($model->term,5)."' ";
        $query .= "AND chaircd  = '".$model->fields["CHAIRCD"]."' ";

        return $query;
    }
    //--- CHEACK（講座データ：群コード）---2004.04.25
    function ChecktoGroupcd($model)
    {
        $query  = "SELECT COUNT(*) FROM chair_dat ";
        $query .= "WHERE year   = '".SUBSTR($model->term,0,4)."' ";
        $query .= "AND semester = '".SUBSTR($model->term,5)."' ";
        $query .= "AND chaircd  = '".$model->fields["CHAIRCD"]."' ";
        $query .= "AND groupcd  = '0000' ";

        return $query;
    }
    //--- INSERT（講座受講クラスデータ）
    function Insert_ChairCls($model,$grade_class)
    {
        $data["YEAR"][TEXT]         = substr($model->term,0,4);
        $data["SEMESTER"][TEXT]     = substr($model->term,5);
        $data["CHAIRCD"][TEXT]      = $model->fields["CHAIRCD"];
        $data["GROUPCD"][TEXT]      = $model->fields["GROUPCD"];
        $data["TRGTGRADE"][TEXT]    = SUBSTR($grade_class,0,2);
        $data["TRGTCLASS"][TEXT]    = SUBSTR($grade_class,2);
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "SYSDATE()";

        $query = Query::insertSQL($data, "chair_cls_dat");

        return $query;
    }
    //--- DELETE（講座受講クラスデータ）
    function Delete_ChairCls($model)
    {
        $query  = "DELETE FROM chair_cls_dat ";
        $query .= " WHERE year     = '".SUBSTR($model->term,0,4)."'";
        $query .= "   AND semester = '".SUBSTR($model->term,5)."'";
        $query .= "   AND chaircd  = '".$model->fields["CHAIRCD"]."'";

        return $query;
    }
    //--- INSERT（講座担当職員データ）
    function Insert_ChairStf($model,$staffcd,$chargediv)
    {
        $data["YEAR"][TEXT]         = substr($model->term,0,4);
        $data["SEMESTER"][TEXT]     = substr($model->term,5);
        $data["CHAIRCD"][TEXT]      = $model->fields["CHAIRCD"];
        $data["STAFFCD"][TEXT]      = $staffcd;
        $data["CHARGEDIV"][NUMBER]  = $chargediv;
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "SYSDATE()";

        $query = Query::insertSQL($data, "chair_stf_dat");

        return $query;
    }
    //--- DELETE（講座担当職員データ）
    function Delete_ChairStf($model)
    {
        $query  = "DELETE FROM chair_stf_dat ";
        $query .= " WHERE year     = '".SUBSTR($model->term,0,4)."'";
        $query .= "   AND semester = '".SUBSTR($model->term,5)."'";
        $query .= "   AND chaircd  = '".$model->fields["CHAIRCD"]."'";

        return $query;
    }
    //--- INSERT（講座使用施設データ）
    function Insert_ChairFac($model,$faccd)
    {
        $data["YEAR"][TEXT]         = substr($model->term,0,4);
        $data["SEMESTER"][TEXT]     = substr($model->term,5);
        $data["CHAIRCD"][TEXT]      = $model->fields["CHAIRCD"];
        $data["FACCD"][TEXT]        = $faccd;
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "SYSDATE()";

        $query = Query::insertSQL($data, "chair_fac_dat");

        return $query;
    }
    //--- DELETE（講座使用施設データ）
    function Delete_ChairFac($model)
    {
        $query  = "DELETE FROM chair_fac_dat ";
        $query .= " WHERE year     = '".SUBSTR($model->term,0,4)."'";
        $query .= "   AND semester = '".SUBSTR($model->term,5)."'";
        $query .= "   AND chaircd  = '".$model->fields["CHAIRCD"]."'";

        return $query;
    }
    //--- DELETE（講座受講生徒データ）
    function Delete_ChairStd($model)
    {
        $query  = "DELETE FROM chair_std_dat ";
        $query .= " WHERE year     = '".SUBSTR($model->term,0,4)."'";
        $query .= "   AND semester = '".SUBSTR($model->term,5)."'";
        $query .= "   AND chaircd  = '".$model->fields["CHAIRCD"]."'";

        return $query;
    }
    //--- INSERT（講座教科書データ）
    function Insert_ChairTextbook($model,$textbookcd)
    {
        $data["YEAR"][TEXT]         = substr($model->term,0,4);
        $data["SEMESTER"][TEXT]     = substr($model->term,5);
        $data["CHAIRCD"][TEXT]      = $model->fields["CHAIRCD"];
        $data["TEXTBOOKCD"][TEXT]   = $textbookcd;
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "SYSDATE()";

        $query = Query::insertSQL($data, "chair_textbook_dat");

        return $query;
    }
    //--- DELETE（講座教科書データ）
    function Delete_ChairTextbook($model)
    {
        $query  = "DELETE FROM chair_textbook_dat ";
        $query .= " WHERE year     = '".SUBSTR($model->term,0,4)."'";
        $query .= "   AND semester = '".SUBSTR($model->term,5)."'";
        $query .= "   AND chaircd  = '".$model->fields["CHAIRCD"]."'";

        return $query;
    }

    //「更新ボタン」押し時の処理（メイン）・・・データがない場合、追加。ある場合、更新。
    function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //配列データ
        $grade_class = $staffcd = $chargediv = $textbookcd = $faccd = array();
        $grade_class    = explode(",",$model->fields["GRADE_CLASS"]);
        $staffcd        = explode(",",$model->fields["STAFFCD"]);
        $chargediv      = explode(",",$model->fields["CHARGEDIV"]);
        $textbookcd     = explode(",",$model->fields["TEXTBOOKCD"]);
        $faccd          = explode(",",$model->fields["FACCD"]);

        //講座データ
        $query = knjb0030oQuery::Count_Chair($model);
        if ($db->getOne($query) > 0) {
            $db->query(knjb0030oQuery::Update_Chair($model));
            $ret_val = "Update";
        } else {
            $db->query(knjb0030oQuery::Insert_Chair($model));
            $ret_val = "Insert";
        }
        //講座受講クラスデータ
        if ($model->fields["GROUPCD"]=="0000") {
            $db->query(knjb0030oQuery::Delete_ChairCls($model));
            if ($model->fields["GRADE_CLASS"] != "")
                for ($i=0;$i<count($grade_class);$i++)
                    $db->query(knjb0030oQuery::Insert_ChairCls($model,$grade_class[$i]));
        }
        //講座担当職員データ
        $db->query(knjb0030oQuery::Delete_ChairStf($model));
        if ($model->fields["STAFFCD"] != "")
            for ($i=0;$i<count($staffcd);$i++)
                $db->query(knjb0030oQuery::Insert_ChairStf($model,$staffcd[$i],$chargediv[$i]));
        //講座教科書データ
        $db->query(knjb0030oQuery::Delete_ChairTextbook($model));
        if ($model->fields["TEXTBOOKCD"] != "")
            for ($i=0;$i<count($textbookcd);$i++)
                $db->query(knjb0030oQuery::Insert_ChairTextbook($model,$textbookcd[$i]));
        //講座使用施設データ
        $db->query(knjb0030oQuery::Delete_ChairFac($model));
        if ($model->fields["FACCD"] != "")
            for ($i=0;$i<count($faccd);$i++)
                $db->query(knjb0030oQuery::Insert_ChairFac($model,$faccd[$i]));

        $db->commit();
        Query::dbCheckIn($db);

        return $ret_val;
    }

    //「削除ボタン」押し時の処理（メイン）
    function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = "DELETE FROM chair_dat ";
        $query .= " WHERE year     = '".SUBSTR($model->term,0,4)."'";
        $query .= "   AND semester = '".SUBSTR($model->term,5)."'";
        $query .= "   AND chaircd  = '".$model->fields["CHAIRCD"]."'";
        $db->query($query);

        if ($model->fields["GROUPCD"]=="0000") $db->query(knjb0030oQuery::Delete_ChairCls($model));
        $db->query(knjb0030oQuery::Delete_ChairStf($model));
        $db->query(knjb0030oQuery::Delete_ChairTextbook($model));
        $db->query(knjb0030oQuery::Delete_ChairFac($model));
        $db->query(knjb0030oQuery::Delete_ChairStd($model));

        $db->commit();
        Query::dbCheckIn($db);
        return "succeeded";
    }

}
?>
