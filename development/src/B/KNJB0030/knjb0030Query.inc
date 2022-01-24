<?php

class knjb0030Query extends Query
{

    /*------------------------左フレーム-----------------------*/

    //学期リスト
    public function selectCombSeme()
    {
        $query = "select year,semester,semestername from semester_mst where semester<>'9' order by year,semester ";

        return $query;
    }

    //教科一覧
    public function getClassMst($model, $term)
    {
        $query  = " SELECT ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND AS VALUE, ";
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CLASSNAME AS LABEL ";
        } else {
            $query .= "     CLASSCD AS VALUE, ";
            $query .= "     CLASSCD || '-' || CLASSNAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     V_CLASS_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '" . SUBSTR($term, 0, 4) . "' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useCurriculumcd"] == '1' && $model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //講座一覧
    public function selectListTest($term, $group, $model, $asc_or_desc)
    {
        $query  = "SELECT ";
        $query .= "    t1.chaircd, ";
        $query .= "    t1.groupcd, ";
        $query .= "    w3.classcd, ";
        if ($group=="1") {
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
            $query .= "    left join v_class_mst w3 on w3.year=t1.year and w3.classcd=substr(t1.subclasscd,1, 2) ";
        }
        $query .= "WHERE ";
        $query .= "    t1.year='" . SUBSTR($term, 0, 4) . "' AND ";
        $query .= "    t1.semester='" . SUBSTR($term, 5) . "' AND ";
        if ($model->selectClass != "999") {
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "    T1.CLASSCD || '-' || T1.SCHOOL_KIND ='{$model->selectClass}' AND ";
            } else {
                $query .= "    T1.CLASSCD = '{$model->selectClass}' AND ";
            }
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     t1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') AND ";
            }
        } elseif ($model->Properties["useCurriculumcd"] == '1' && $model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     t1.SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        }
        //絞込み（教科の場合、群コード：０）（群の場合、群コード：０以外）
        if ($group=="1") {
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
    public function getChairDatcount($model, $term)
    {
        $query  = "SELECT COUNT(*) FROM chair_dat";
        $query .=" WHERE year ='" . SUBSTR($term, 0, 4)."' AND semester='" . SUBSTR($term, 5, 1)."'";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useCurriculumcd"] == '1' && $model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    //コピー元の講座データが存在するかをチェック
    public function count($term2)
    {
        $db = Query::dbCheckOut();

        $query  = "SELECT COUNT(*) FROM chair_dat";
        $query .=" WHERE year ='" . SUBSTR($term2, 0, 4)."' AND semester='" . SUBSTR($term2, 5, 1)."'";

        $count = $db->getOne($query);
        return $count;
        Query::dbCheckIn($db);
    }
    //対象年度の在籍ヘッダデータおよび在籍データの存在チェック---2005.02.23Add
    public function countSchregRegd($term, $table)
    {
        $db = Query::dbCheckOut();

        $query  = "SELECT COUNT(*) FROM ".$table."";
        $query .=" WHERE year ='" . SUBSTR($term, 0, 4)."' AND semester='" . SUBSTR($term, 5, 1)."'";

        $count = $db->getOne($query);
        return $count;
        Query::dbCheckIn($db);
    }

    //参照先の講座受講生徒データが妥当なデータか確かめる(DBエラー(重複)で落とさないため)
    public function checkChairStdDat($term2)
    {
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
        $query .= "     T1.YEAR     = '" . SUBSTR($term2, 0, 4)."' AND ";
        $query .= "     T1.SEMESTER = '" . SUBSTR($term2, 5, 1)."' ";
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
    public function getInsertCopyQuery($model, $term2, $term, $check, $appdate, $appenddate, $checkGrddiv)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //前処理（対象の各データを全て削除）
        $db->query(knjb0030Query::deleteCopy($term, "chair_dat"));
        $db->query(knjb0030Query::deleteCopy($term, "chair_cls_dat"));
        $db->query(knjb0030Query::deleteCopy($term, "chair_stf_dat"));
        $db->query(knjb0030Query::deleteCopy($term, "chair_textbook_dat"));
        $db->query(knjb0030Query::deleteCopy($term, "chair_fac_dat"));
        $db->query(knjb0030Query::deleteCopy($term, "chair_detail_dat"));
        $db->query(knjb0030Query::deleteCopy($term, "chair_std_dat"));
        //追加処理（参照から対象へ全てインサート）
        $db->query(knjb0030Query::insertChrDat($model, $term2, $term));
        $db->query(knjb0030Query::insertClsDat($term2, $term));
        $db->query(knjb0030Query::insertStfDat($term2, $term));
        $db->query(knjb0030Query::insertTexDat($term2, $term));
        $db->query(knjb0030Query::insertFacDat($term2, $term));
        $db->query(knjb0030Query::insertDetailDat($term2, $term));
        //生徒もコピー
        if ($check==1) {
            $db->query(knjb0030Query::insertStdDat($term2, $term, $appdate, $appenddate, $checkGrddiv));
        }
        //後処理（対象年度・学期に存在しない各データを削除）
        /*-----↓----- 2005.02.23 Del by nakamoto -----↓-----
        $db->query(knjb0030Query::deleteCopyChrDat($term2,$term));
        $db->query(knjb0030Query::deleteCopyClsDat($term2,$term));
        $db->query(knjb0030Query::deleteCopyStfDat($term2,$term));
        $db->query(knjb0030Query::deleteCopyTexDat($term2,$term));
        $db->query(knjb0030Query::deleteCopyFacDat($term2,$term));
        if($check==1){
            $db->query(knjb0030Query::deleteCopyStdDat($term2,$term));
        }
         -----↑----- 2005.02.23 Del by nakamoto -----↑----- */

        $db->commit();
        Query::dbCheckIn($db);
    }

    //前処理（対象の各データを全て削除）
    public function deleteCopy($term, $table)
    {
        $query  = "DELETE FROM ".$table." ";
        $query .= " WHERE year     = '" . SUBSTR($term, 0, 4)."'";
        $query .= "   AND semester = '" . SUBSTR($term, 5)."'";

        return $query;
    }

    //追加処理（参照から対象へ全てインサート）
    //講座データ
    public function insertChrDat($model, $term2, $term)
    {
        $query  =" INSERT INTO chair_dat";
        $query .=" SELECT '" . SUBSTR($term, 0, 4)."',";
        $query .="        '" . SUBSTR($term, 5, 1)."',";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .="        chaircd, groupcd, classcd, school_kind, curriculum_cd, subclasscd, ";
            $query .="        chairname, chairabbv, takesemes, lessoncnt, framecnt,";
        } else {
            $query .="        chaircd, groupcd, subclasscd, chairname, chairabbv, takesemes, lessoncnt, framecnt,";
        }
        $query .="        countflg,";//集計フラグ---05/01/19Add
        $query .="        '".STAFFCD."', SYSDATE()";
        $query .="   FROM chair_dat";
        $query .="  WHERE year = '" . SUBSTR($term2, 0, 4)."' AND semester = '" . SUBSTR($term2, 5, 1)."'";

        return $query;
    }
    //講座受講クラスデータ
    public function insertClsDat($term2, $term)
    {
        $query  =" INSERT INTO chair_cls_dat";
        $query .=" SELECT '" . SUBSTR($term, 0, 4)."',";
        $query .="        '" . SUBSTR($term, 5, 1)."',";
        $query .="        chaircd, groupcd, trgtgrade, trgtclass,";
        $query .="        '".STAFFCD."', SYSDATE()";
        $query .="   FROM chair_cls_dat";
        $query .="  WHERE year = '" . SUBSTR($term2, 0, 4)."' AND semester = '" . SUBSTR($term2, 5, 1)."'";

        return $query;
    }
    //講座担当職員データ
    public function insertStfDat($term2, $term)
    {
        $query  =" INSERT INTO chair_stf_dat";
        $query .=" SELECT '" . SUBSTR($term, 0, 4)."',";
        $query .="        '" . SUBSTR($term, 5, 1)."',";
        $query .="        chaircd, staffcd, chargediv,";
        $query .="        '".STAFFCD."', SYSDATE()";
        $query .="   FROM chair_stf_dat";
        $query .="  WHERE year = '" . SUBSTR($term2, 0, 4)."' AND semester = '" . SUBSTR($term2, 5, 1)."'";

        return $query;
    }
    //講座教科書データ
    public function insertTexDat($term2, $term)
    {
        $query  =" INSERT INTO chair_textbook_dat";
        $query .=" SELECT '" . SUBSTR($term, 0, 4)."',";
        $query .="        '" . SUBSTR($term, 5, 1)."',";
        $query .="        chaircd, textbookcd,";
        $query .="        '".STAFFCD."', SYSDATE()";
        $query .="   FROM chair_textbook_dat";
        $query .="  WHERE year = '" . SUBSTR($term2, 0, 4)."' AND semester = '" . SUBSTR($term2, 5, 1)."'";

        return $query;
    }
    //講座使用施設データ
    public function insertFacDat($term2, $term)
    {
        $query  =" INSERT INTO chair_fac_dat";
        $query .=" SELECT '" . SUBSTR($term, 0, 4)."',";
        $query .="        '" . SUBSTR($term, 5, 1)."',";
        $query .="        chaircd, faccd,";
        $query .="        '".STAFFCD."', SYSDATE()";
        $query .="   FROM chair_fac_dat";
        $query .="  WHERE year = '" . SUBSTR($term2, 0, 4)."' AND semester = '" . SUBSTR($term2, 5, 1)."'";

        return $query;
    }

    //講座使用施設データ
    public function insertDetailDat($term2, $term)
    {
        $query  = " INSERT INTO chair_detail_dat (year,semester,chaircd,seq,remark1,remark2,registercd,updated) ";
        $query .= " SELECT '" . SUBSTR($term, 0, 4)."', ";
        $query .= "        '" . SUBSTR($term, 5, 1)."', ";
        $query .= "        chaircd, seq, remark1, remark2, ";
        $query .= "        '".STAFFCD."', SYSDATE() ";
        $query .= " FROM chair_detail_dat ";
        $query .= " WHERE year = '" . SUBSTR($term2, 0, 4)."' AND semester = '" . SUBSTR($term2, 5, 1)."' ";

        return $query;
    }

    //生徒もコピー
    //講座受講生徒データ
    public function insertStdDat($term2, $term, $appdate, $appenddate, $checkGrddiv)
    {
        $query  =" INSERT INTO chair_std_dat";
        $query .=" SELECT '" . SUBSTR($term, 0, 4)."',";
        $query .="        '" . SUBSTR($term, 5, 1)."',";
        $query .="        w1.chaircd, w1.schregno, '".$appdate."', '".$appenddate."', w1.row, w1.column,";
        $query .="        '".STAFFCD."', SYSDATE()";
        $query .="   FROM chair_std_dat w1, semester_mst w2 ";
        $query .="  WHERE w1.year = '" . SUBSTR($term2, 0, 4)."' AND w1.semester = '" . SUBSTR($term2, 5, 1)."' AND ";
        $query .="        w1.year = w2.year AND w1.semester = w2.semester AND w1.appenddate = w2.edate ";
        /* -----↓----- 2005.02.23 Add by nakamoto -----↓----- */
        if ($checkGrddiv != 1) {
            $query .="        AND W1.SCHREGNO NOT IN ( ";
            $query .="                SELECT SCHREGNO ";
            $query .="                FROM   SCHREG_BASE_MST K1,SEMESTER_MST K2 ";
            $query .="                WHERE  YEAR='" . SUBSTR($term2, 0, 4)."' AND SEMESTER='" . SUBSTR($term2, 5, 1)."' AND ";
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
        $query .= "                                year='" . SUBSTR($term2, 0, 4)."' AND ";
        $query .= "                                semester='" . SUBSTR($term2, 5, 1)."' ";
        $query .= "                            GROUP BY ";
        $query .= "                                chaircd ";
        $query .= "                            ) ";
        */
        return $query;
    }

    //後処理（対象年度・学期に存在しない各データを削除）
    //講座データ（群コード・科目コード）
    public function deleteCopyChrDat($term2, $term)
    {
        $query  = "DELETE FROM chair_dat ";
        $query .= "WHERE ";
        $query .= "    year='" . SUBSTR($term, 0, 4)."' AND ";
        $query .= "    semester='" . SUBSTR($term, 5)."' AND ";
        $query .= "    (subclasscd in  (SELECT ";
        $query .= "                        w1.subclasscd ";
        $query .= "                    FROM ";
        $query .= "                        v_subclass_mst w1 ";
        $query .= "                    WHERE ";
        $query .= "                        w1.year='" . SUBSTR($term2, 0, 4)."' AND ";
        $query .= "                        not exists(SELECT ";
        $query .= "                                        * ";
        $query .= "                                    FROM ";
        $query .= "                                        v_subclass_mst w2 ";
        $query .= "                                    WHERE ";
        $query .= "                                        w2.year='" . SUBSTR($term, 0, 4)."' AND ";
        $query .= "                                        w2.subclasscd=w1.subclasscd ";
        $query .= "                                    ) ";
        $query .= "                    ) OR ";
        $query .= "    groupcd in (SELECT ";
        $query .= "                    w1.groupcd ";
        $query .= "                FROM ";
        $query .= "                    v_electclass_mst w1 ";
        $query .= "                WHERE ";
        $query .= "                    w1.year='" . SUBSTR($term2, 0, 4)."' AND ";
        $query .= "                    not exists(SELECT ";
        $query .= "                                    * ";
        $query .= "                                FROM ";
        $query .= "                                    v_electclass_mst w2 ";
        $query .= "                                WHERE ";
        $query .= "                                    w2.year='" . SUBSTR($term, 0, 4)."' AND ";
        $query .= "                                    w2.groupcd=w1.groupcd ";
        $query .= "                                ) ";
        $query .= "                ) ";
        $query .= "    ) ";

        return $query;
    }
    //講座受講クラスデータ（年組・群コード・講座コード）
    public function deleteCopyClsDat($term2, $term)
    {
        $query  = "DELETE FROM chair_cls_dat ";
        $query .= "WHERE ";
        $query .= "    year='" . SUBSTR($term, 0, 4)."' AND ";
        $query .= "    semester='" . SUBSTR($term, 5)."' AND ";
        $query .= "    (trgtgrade || trgtclass in  (SELECT ";
        $query .= "                        w1.grade || w1.hr_class ";
        $query .= "                    FROM ";
        $query .= "                        schreg_regd_hdat w1 ";
        $query .= "                    WHERE ";
        $query .= "                        w1.year='" . SUBSTR($term2, 0, 4)."' AND ";
        $query .= "                        w1.semester='" . SUBSTR($term2, 5)."' AND ";
        $query .= "                        not exists(SELECT ";
        $query .= "                                        * ";
        $query .= "                                    FROM ";
        $query .= "                                        schreg_regd_hdat w2 ";
        $query .= "                                    WHERE ";
        $query .= "                                        w2.year='" . SUBSTR($term, 0, 4)."' AND ";
        $query .= "                                        w2.semester='" . SUBSTR($term, 5)."' AND ";
        $query .= "                                        w2.grade || w2.hr_class = w1.grade || w1.hr_class ";
        $query .= "                                    ) ";
        $query .= "                    ) OR ";
        $query .= "    groupcd in (SELECT ";
        $query .= "                    w1.groupcd ";
        $query .= "                FROM ";
        $query .= "                    v_electclass_mst w1 ";
        $query .= "                WHERE ";
        $query .= "                    w1.year='" . SUBSTR($term2, 0, 4)."' AND ";
        $query .= "                    not exists(SELECT ";
        $query .= "                                    * ";
        $query .= "                                FROM ";
        $query .= "                                    v_electclass_mst w2 ";
        $query .= "                                WHERE ";
        $query .= "                                    w2.year='" . SUBSTR($term, 0, 4)."' AND ";
        $query .= "                                    w2.groupcd=w1.groupcd ";
        $query .= "                                ) ";
        $query .= "                ) OR ";
        $query .= "    chaircd in (SELECT ";
        $query .= "                    w1.chaircd ";
        $query .= "                FROM ";
        $query .= "                    chair_dat w1 ";
        $query .= "                WHERE ";
        $query .= "                    w1.year='" . SUBSTR($term2, 0, 4)."' AND ";
        $query .= "                    w1.semester='" . SUBSTR($term2, 5)."' AND ";
        $query .= "                    not exists(SELECT ";
        $query .= "                                    * ";
        $query .= "                                FROM ";
        $query .= "                                    chair_dat w2 ";
        $query .= "                                WHERE ";
        $query .= "                                    w2.year='" . SUBSTR($term, 0, 4)."' AND ";
        $query .= "                                    w2.semester='" . SUBSTR($term, 5)."' AND ";
        $query .= "                                    w2.chaircd=w1.chaircd ";
        $query .= "                                ) ";
        $query .= "                ) ";
        $query .= "    ) ";

        return $query;
    }
    //講座担当職員データ（職員コード・講座コード）
    public function deleteCopyStfDat($term2, $term)
    {
        $query  = "DELETE FROM chair_stf_dat ";
        $query .= "WHERE ";
        $query .= "    year='" . SUBSTR($term, 0, 4)."' AND ";
        $query .= "    semester='" . SUBSTR($term, 5)."' AND ";
        $query .= "    (staffcd in (SELECT ";
        $query .= "                    w1.staffcd ";
        $query .= "                FROM ";
        $query .= "                    v_staff_mst w1 ";
        $query .= "                WHERE ";
        $query .= "                    w1.year='" . SUBSTR($term2, 0, 4)."' AND ";
        $query .= "                    not exists(SELECT ";
        $query .= "                                    * ";
        $query .= "                                FROM ";
        $query .= "                                    v_staff_mst w2 ";
        $query .= "                                WHERE ";
        $query .= "                                    w2.year='" . SUBSTR($term, 0, 4)."' AND ";
        $query .= "                                    w2.staffcd=w1.staffcd ";
        $query .= "                                ) ";
        $query .= "                ) OR ";
        $query .= "    chaircd in (SELECT ";
        $query .= "                    w1.chaircd ";
        $query .= "                FROM ";
        $query .= "                    chair_dat w1 ";
        $query .= "                WHERE ";
        $query .= "                    w1.year='" . SUBSTR($term2, 0, 4)."' AND ";
        $query .= "                    w1.semester='" . SUBSTR($term2, 5)."' AND ";
        $query .= "                    not exists(SELECT ";
        $query .= "                                    * ";
        $query .= "                                FROM ";
        $query .= "                                    chair_dat w2 ";
        $query .= "                                WHERE ";
        $query .= "                                    w2.year='" . SUBSTR($term, 0, 4)."' AND ";
        $query .= "                                    w2.semester='" . SUBSTR($term, 5)."' AND ";
        $query .= "                                    w2.chaircd=w1.chaircd ";
        $query .= "                                ) ";
        $query .= "                ) ";
        $query .= "    ) ";

        return $query;
    }
    //講座使用施設データ（施設コード・講座コード）
    public function deleteCopyFacDat($term2, $term)
    {
        $query  = "DELETE FROM chair_fac_dat ";
        $query .= "WHERE ";
        $query .= "    year='" . SUBSTR($term, 0, 4)."' AND ";
        $query .= "    semester='" . SUBSTR($term, 5)."' AND ";
        $query .= "    (faccd in (SELECT ";
        $query .= "                    w1.faccd ";
        $query .= "                FROM ";
        $query .= "                    v_facility_mst w1 ";
        $query .= "                WHERE ";
        $query .= "                    w1.year='" . SUBSTR($term2, 0, 4)."' AND ";
        $query .= "                    not exists(SELECT ";
        $query .= "                                    * ";
        $query .= "                                FROM ";
        $query .= "                                    v_facility_mst w2 ";
        $query .= "                                WHERE ";
        $query .= "                                    w2.year='" . SUBSTR($term, 0, 4)."' AND ";
        $query .= "                                    w2.faccd=w1.faccd ";
        $query .= "                                ) ";
        $query .= "                ) OR ";
        $query .= "    chaircd in (SELECT ";
        $query .= "                    w1.chaircd ";
        $query .= "                FROM ";
        $query .= "                    chair_dat w1 ";
        $query .= "                WHERE ";
        $query .= "                    w1.year='" . SUBSTR($term2, 0, 4)."' AND ";
        $query .= "                    w1.semester='" . SUBSTR($term2, 5)."' AND ";
        $query .= "                    not exists(SELECT ";
        $query .= "                                    * ";
        $query .= "                                FROM ";
        $query .= "                                    chair_dat w2 ";
        $query .= "                                WHERE ";
        $query .= "                                    w2.year='" . SUBSTR($term, 0, 4)."' AND ";
        $query .= "                                    w2.semester='" . SUBSTR($term, 5)."' AND ";
        $query .= "                                    w2.chaircd=w1.chaircd ";
        $query .= "                                ) ";
        $query .= "                ) ";
        $query .= "    ) ";

        return $query;
    }
    //講座教科書データ（教科書コード・講座コード）
    public function deleteCopyTexDat($term2, $term)
    {
        $query  = "DELETE FROM chair_textbook_dat ";
        $query .= "WHERE ";
        $query .= "    year='" . SUBSTR($term, 0, 4)."' AND ";
        $query .= "    semester='" . SUBSTR($term, 5)."' AND ";
        $query .= "    (textbookcd in (SELECT ";
        $query .= "                    w1.textbookcd ";
        $query .= "                FROM ";
        $query .= "                    v_textbook_mst w1 ";
        $query .= "                WHERE ";
        $query .= "                    w1.year='" . SUBSTR($term2, 0, 4)."' AND ";
        $query .= "                    not exists(SELECT ";
        $query .= "                                    * ";
        $query .= "                                FROM ";
        $query .= "                                    v_textbook_mst w2 ";
        $query .= "                                WHERE ";
        $query .= "                                    w2.year='" . SUBSTR($term, 0, 4)."' AND ";
        $query .= "                                    w2.textbookcd=w1.textbookcd ";
        $query .= "                                ) ";
        $query .= "                ) OR ";
        $query .= "    chaircd in (SELECT ";
        $query .= "                    w1.chaircd ";
        $query .= "                FROM ";
        $query .= "                    chair_dat w1 ";
        $query .= "                WHERE ";
        $query .= "                    w1.year='" . SUBSTR($term2, 0, 4)."' AND ";
        $query .= "                    w1.semester='" . SUBSTR($term2, 5)."' AND ";
        $query .= "                    not exists(SELECT ";
        $query .= "                                    * ";
        $query .= "                                FROM ";
        $query .= "                                    chair_dat w2 ";
        $query .= "                                WHERE ";
        $query .= "                                    w2.year='" . SUBSTR($term, 0, 4)."' AND ";
        $query .= "                                    w2.semester='" . SUBSTR($term, 5)."' AND ";
        $query .= "                                    w2.chaircd=w1.chaircd ";
        $query .= "                                ) ";
        $query .= "                ) ";
        $query .= "    ) ";

        return $query;
    }
    //講座受講生徒データ（学籍番号・講座コード）
    public function deleteCopyStdDat($term2, $term)
    {
        $query  = "DELETE FROM chair_std_dat ";
        $query .= "WHERE ";
        $query .= "    year='" . SUBSTR($term, 0, 4)."' AND ";
        $query .= "    semester='" . SUBSTR($term, 5)."' AND ";
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
        $query .= "                    w1.year='" . SUBSTR($term2, 0, 4)."' AND ";
        $query .= "                    w1.semester='" . SUBSTR($term2, 5)."' AND ";
        $query .= "                    not exists(SELECT ";
        $query .= "                                    * ";
        $query .= "                                FROM ";
        $query .= "                                    chair_dat w2 ";
        $query .= "                                WHERE ";
        $query .= "                                    w2.year='" . SUBSTR($term, 0, 4)."' AND ";
        $query .= "                                    w2.semester='" . SUBSTR($term, 5)."' AND ";
        $query .= "                                    w2.chaircd=w1.chaircd ";
        $query .= "                                ) ";
        $query .= "                ) ";
        $query .= "    ) ";

        return $query;
    }

    /*------------------------右フレーム-----------------------*/

    //リストコンボ用取得
    //科目
    public function getSubclass($model, $year)
    {
        $db  = Query::dbCheckOut();
        $opt = array();
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query  = "SELECT DISTINCT ";
            $query .= "       classcd || '-' || school_kind || '-' || curriculum_cd || '-' || subclasscd as subclasscd, ";
            $query .= "       subclassname ";
            $query .= "  FROM v_subclass_mst ";
            $query .= " WHERE year   = '".$year."' ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
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
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["SUBCLASSCD"]."　".$row["SUBCLASSNAME"],
                           "value" => $row["SUBCLASSCD"]);
        }
        Query::dbCheckIn($db);
        return $opt;
    }
    //履修期間区分
    public function getTakesemes($year)
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
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["SEMESTER"]."　".$row["SEMESTERNAME"],
                           "value" => $row["SEMESTER"]);
        }
        Query::dbCheckIn($db);
        return $opt;
    }
    //履修コース
    public function getRisyuuCourse($year)
    {
        $db  = Query::dbCheckOut();
        $opt = array();
        $query  = " SELECT DISTINCT namecd2, name1 ";
        $query .= " FROM v_name_mst ";
        $query .= " WHERE year = '".$year."' and namecd1 = 'B022' ";
        $query .= " ORDER BY namecd2";

        $result = $db->query($query);

        $opt[] = array("label" => "", "value" => "");
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["NAMECD2"]."　".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }
        Query::dbCheckIn($db);
        return $opt;
    }
    //群
    public function getGroup($year)
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
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["GROUPCD"]."　".$row["GROUPNAME"],
                           "value" => $row["GROUPCD"]);
        }
        Query::dbCheckIn($db);
        return $opt;
    }

    //選択ダイアログ
    //受講クラス
    public function selectQuerySubForm1($model, $term)
    {
        $query  = "SELECT ";
        $query .= "    grade || hr_class as grade_class, ";
        $query .= "    hr_name, ";
        $query .= "    hr_nameabbv ";
        $query .= "FROM ";
        $query .= "    schreg_regd_hdat T1 ";
        $query .= "WHERE ";
        $query .= "    year='" . SUBSTR($term, 0, 4)."' AND ";
        $query .= "    semester='" . SUBSTR($term, 5)."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND GRADE IN (  SELECT ";
                $query .= "                     G1.GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT G1 ";
                $query .= "                 WHERE ";
                $query .= "                     G1.YEAR         = T1.YEAR AND ";
                $query .= "                     G1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                $query .= "                 ) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND GRADE IN (  SELECT ";
            $query .= "                     G1.GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT G1 ";
            $query .= "                 WHERE ";
            $query .= "                     G1.YEAR         = T1.YEAR AND ";
            $query .= "                     G1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
            $query .= "                 ) ";
        }
        $query .= " ORDER BY 1 ";

        return $query;
    }
    //科目担任
    public function selectQuerySubForm2($term, $staffcd, $subclass)
    {
        $query  = "SELECT ";
        $query .= "    T1.SECTIONCD, ";
        $query .= "    T2.SECTIONABBV, ";
        $query .= "    T1.STAFFCD, ";
        $query .= "    T1.STAFFNAME_SHOW ";
        $query .= "FROM ";
        $query .= "    V_STAFF_MST T1 ";
        $query .= "    LEFT JOIN V_SECTION_MST T2 ON T2.YEAR =T1.YEAR AND T2.SECTIONCD = T1.SECTIONCD ";
        $query .= "    LEFT JOIN STAFF_CLASS_MST T3 ON T3.STAFFCD = T1.STAFFCD ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            list($classcd, $school_kind, $curriculum_cd, $subclasscd) = explode("-", $subclass);
            $query .= "     AND T3.CLASSCD         = '".$classcd."' ";
            $query .= "     AND T3.SCHOOL_KIND     = '".$school_kind."' ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " AND T3.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND T3.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
        } else {
            $query .= "     AND T3.CLASSCD = SUBSTR('".$subclass."', 1, 2) ";
        }
        $query .= "WHERE ";
        $query .= "    T1.YEAR='" . SUBSTR($term, 0, 4)."' ";
        $query .= " ORDER BY ";
        $query .= "    CASE WHEN T3.STAFFCD IS NOT NULL ";
        $query .= "         THEN 0 ";
        $query .= "         ELSE 1 ";
        $query .= "    END, ";
        $query .= "    T1.STAFFCD, ";
        $query .= "    T1.SECTIONCD ";
        return $query;
    }

    //資格科目
    public function getStaffClass($model, $subclass, $flg = "")
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     STAFFCD ";
        $query .= " FROM ";
        $query .= "     STAFF_CLASS_MST ";
        if ($flg) {
            $query .= " WHERE ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                list($classcd, $school_kind, $curriculum_cd, $subclasscd) = explode("-", $subclass);
                $query .= "     CLASSCD         = '".$classcd."' AND ";
                $query .= "     SCHOOL_KIND     = '".$school_kind."' ";
                if ($model->Properties["use_prg_schoolkind"] == "1") {
                    if ($model->selectSchoolKind) {
                        $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                    }
                } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                    $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
                }
            } else {
                $query .= "     CLASSCD = SUBSTR('".$subclass."',1, 2) ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     STAFFCD ";

        return $query;
    }

    //使用施設
    public function selectQuerySubForm3($term)
    {
        $query  = "SELECT ";
        $query .= "    faccd, ";
        $query .= "    facilityname, ";
        $query .= "    facilityabbv ";
        $query .= "FROM ";
        $query .= "    v_facility_mst ";
        $query .= "WHERE ";
        $query .= "    year='" . SUBSTR($term, 0, 4)."' ";
        $query .= " ORDER BY 1 ";

        return $query;
    }
    //教科書
    public function selectQuerySubForm4($term)
    {
        $query  = "SELECT ";
        $query .= "    textbookcd, ";
        $query .= "    textbookname, ";
        $query .= "    textbookabbv ";
        $query .= "FROM ";
        $query .= "    v_textbook_mst ";
        $query .= "WHERE ";
        $query .= "    year='" . SUBSTR($term, 0, 4)."' ";
        $query .= " ORDER BY 1 ";

        return $query;
    }

    //１レコード取得
    //（講座）
    public function getRowTest($model, $term, $chaircd)
    {
        $db  = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     L1.REMARK1 AS STD_CNT, ";
        $query .= "     L1.REMARK2 AS NOT_DAY_ATTEND, ";
        $query .= "     L2.REMARK1 AS RISYUU_COURSE ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1 ";
        $query .= "     LEFT JOIN CHAIR_DETAIL_DAT L1 ";
        $query .= "          ON T1.YEAR     = L1.YEAR ";
        $query .= "         AND T1.SEMESTER = L1.SEMESTER ";
        $query .= "         AND T1.CHAIRCD  = L1.CHAIRCD ";
        $query .= "         AND L1.SEQ      = '001' ";
        $query .= "     LEFT JOIN CHAIR_DETAIL_DAT L2 ";
        $query .= "          ON T1.YEAR     = L2.YEAR ";
        $query .= "         AND T1.SEMESTER = L2.SEMESTER ";
        $query .= "         AND T1.CHAIRCD  = L2.CHAIRCD ";
        $query .= "         AND L2.SEQ      = '002' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '" . SUBSTR($term, 0, 4)."' ";
        $query .= " AND T1.SEMESTER = '" . SUBSTR($term, 5)."' ";
        $query .= " AND T1.CHAIRCD  = '".$chaircd."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }
    //（授業回数）
    public function getRowLesson($term, $groupcd)
    {
        $db  = Query::dbCheckOut();

        $query  = "SELECT distinct lessoncnt,framecnt FROM chair_dat ";
        $query .= "WHERE year   = '" . SUBSTR($term, 0, 4)."' ";
        $query .= "AND semester = '" . SUBSTR($term, 5)."' ";
        $query .= "AND groupcd  = '".$groupcd."' ";
        $query .= "order by lessoncnt desc ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //受講クラス取得
    public function getGradeClass($model, $term, $chaircd, $groupcd)
    {
        $query  = "SELECT ";
        $query .= "    t1.trgtgrade || t1.trgtclass as grade_class, ";
        $query .= "    t2.hr_nameabbv ";
        $query .= "FROM ";
        $query .= "    chair_cls_dat t1, ";
        $query .= "    schreg_regd_hdat t2 ";
        $query .= "WHERE ";
        $query .= "    t1.year='" . SUBSTR($term, 0, 4)."' AND ";
        $query .= "    t1.semester='" . SUBSTR($term, 5)."' AND ";
        if ($groupcd > "0000") {
            $query .= "    t1.groupcd='".$groupcd."' AND ";
        } else {
            $query .= "    t1.chaircd='".$chaircd."' AND ";
        }
        $query .= "    t1.year=t2.year AND ";
        $query .= "    t1.semester=t2.semester AND ";
        $query .= "    t1.trgtgrade=t2.grade AND ";
        $query .= "    t1.trgtclass=t2.hr_class ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND t1.trgtgrade IN (SELECT ";
                $query .= "                         G1.GRADE ";
                $query .= "                     FROM ";
                $query .= "                         SCHREG_REGD_GDAT G1 ";
                $query .= "                     WHERE ";
                $query .= "                         G1.YEAR         = T1.YEAR AND ";
                $query .= "                         G1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                $query .= "                     ) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND t1.trgtgrade IN (SELECT ";
            $query .= "                         G1.GRADE ";
            $query .= "                     FROM ";
            $query .= "                         SCHREG_REGD_GDAT G1 ";
            $query .= "                     WHERE ";
            $query .= "                         G1.YEAR         = T1.YEAR AND ";
            $query .= "                         G1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
            $query .= "                     ) ";
        }
        $query .= " ORDER BY 1 ";

        return $query;
    }
    //科目担任取得
    public function getStaff($term, $chaircd)
    {
        $query  = "SELECT ";
        $query .= "    t1.staffcd, ";
        $query .= "    value(t1.chargediv,0) as chargediv, ";
        $query .= "    t2.staffname_show ";
        $query .= "FROM ";
        $query .= "    chair_stf_dat t1, ";
        $query .= "    v_staff_mst t2 ";
        $query .= "WHERE ";
        $query .= "    t1.year='" . SUBSTR($term, 0, 4)."' AND ";
        $query .= "    t1.semester='" . SUBSTR($term, 5)."' AND ";
        $query .= "    t1.chaircd='".$chaircd."' AND ";
        $query .= "    t1.year=t2.year AND ";
        $query .= "    t1.staffcd=t2.staffcd ";
        $query .= " ORDER BY t1.staffcd ";

        return $query;
    }
    //使用施設取得
    public function getFac($term, $chaircd)
    {
        $query  = "SELECT ";
        $query .= "    t1.faccd, ";
        $query .= "    t2.facilityabbv ";
        $query .= "FROM ";
        $query .= "    chair_fac_dat t1, ";
        $query .= "    v_facility_mst t2 ";
        $query .= "WHERE ";
        $query .= "    t1.year='" . SUBSTR($term, 0, 4)."' AND ";
        $query .= "    t1.semester='" . SUBSTR($term, 5)."' AND ";
        $query .= "    t1.chaircd='".$chaircd."' AND ";
        $query .= "    t1.year=t2.year AND ";
        $query .= "    t1.faccd=t2.faccd ";
        $query .= " ORDER BY t1.faccd";

        return $query;
    }
    //教科書取得
    public function getTextbook($term, $chaircd)
    {
        $query  = "SELECT ";
        $query .= "    t1.textbookcd, ";
        $query .= "    t2.textbookabbv ";
        $query .= "FROM ";
        $query .= "    chair_textbook_dat t1, ";
        $query .= "    v_textbook_mst t2 ";
        $query .= "WHERE ";
        $query .= "    t1.year='" . SUBSTR($term, 0, 4)."' AND ";
        $query .= "    t1.semester='" . SUBSTR($term, 5)."' AND ";
        $query .= "    t1.chaircd='".$chaircd."' AND ";
        $query .= "    t1.year=t2.year AND ";
        $query .= "    t1.textbookcd=t2.textbookcd ";
        $query .= " ORDER BY t1.textbookcd ";

        return $query;
    }
    //--- INSERT（講座データ）
    public function insertChair($model)
    {
        $data["YEAR"][TEXT]         = substr($model->term, 0, 4);
        $data["SEMESTER"][TEXT]     = substr($model->term, 5);
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
    public function updateChair($model)
    {
        $data["YEAR"][TEXT]         = substr($model->term, 0, 4);
        $data["SEMESTER"][TEXT]     = substr($model->term, 5);
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
        $where  = " WHERE year   = '" . SUBSTR($model->term, 0, 4)."'";
        $where .= " AND semester = '" . SUBSTR($model->term, 5)."'";
        $where .= " AND chaircd  = '".$model->fields["CHAIRCD"]."'";

        $query = Query::updateSQL($data, "chair_dat", $where);

        return $query;
    }
    //--- CHEACK（講座データ）
    public function countChair($model)
    {
        $query  = "SELECT COUNT(*) FROM chair_dat ";
        $query .= "WHERE year   = '" . SUBSTR($model->term, 0, 4)."' ";
        $query .= "AND semester = '" . SUBSTR($model->term, 5)."' ";
        $query .= "AND chaircd  = '".$model->fields["CHAIRCD"]."' ";

        return $query;
    }
    //--- CHEACK（講座データ：群コード）---2004.04.25
    public function checktoGroupcd($model)
    {
        $query  = "SELECT COUNT(*) FROM chair_dat ";
        $query .= "WHERE year   = '" . SUBSTR($model->term, 0, 4)."' ";
        $query .= "AND semester = '" . SUBSTR($model->term, 5)."' ";
        $query .= "AND chaircd  = '".$model->fields["CHAIRCD"]."' ";
        $query .= "AND groupcd  = '0000' ";

        return $query;
    }
    //--- INSERT（講座受講クラスデータ）
    public function insertChairCls($model, $grade_class)
    {
        $data["YEAR"][TEXT]         = substr($model->term, 0, 4);
        $data["SEMESTER"][TEXT]     = substr($model->term, 5);
        $data["CHAIRCD"][TEXT]      = $model->fields["CHAIRCD"];
        $data["GROUPCD"][TEXT]      = $model->fields["GROUPCD"];
        $data["TRGTGRADE"][TEXT]    = SUBSTR($grade_class, 0, 2);
        $data["TRGTCLASS"][TEXT]    = SUBSTR($grade_class, 2);
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "SYSDATE()";

        $query = Query::insertSQL($data, "chair_cls_dat");

        return $query;
    }
    //--- DELETE（講座受講クラスデータ）
    public function deleteChairCls($model)
    {
        $query  = "DELETE FROM chair_cls_dat ";
        $query .= " WHERE year     = '" . SUBSTR($model->term, 0, 4)."'";
        $query .= "   AND semester = '" . SUBSTR($model->term, 5)."'";
        $query .= "   AND chaircd  = '".$model->fields["CHAIRCD"]."'";

        return $query;
    }
    //--- INSERT（講座担当職員データ）
    public function insertChairStf($model, $staffcd, $chargediv)
    {
        $data["YEAR"][TEXT]         = substr($model->term, 0, 4);
        $data["SEMESTER"][TEXT]     = substr($model->term, 5);
        $data["CHAIRCD"][TEXT]      = $model->fields["CHAIRCD"];
        $data["STAFFCD"][TEXT]      = $staffcd;
        $data["CHARGEDIV"][NUMBER]  = $chargediv;
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "SYSDATE()";

        $query = Query::insertSQL($data, "chair_stf_dat");

        return $query;
    }
    //--- DELETE（講座担当職員データ）
    public function deleteChairStf($model)
    {
        $query  = "DELETE FROM chair_stf_dat ";
        $query .= " WHERE year     = '" . SUBSTR($model->term, 0, 4)."'";
        $query .= "   AND semester = '" . SUBSTR($model->term, 5)."'";
        $query .= "   AND chaircd  = '".$model->fields["CHAIRCD"]."'";

        return $query;
    }
    //--- INSERT（講座使用施設データ）
    public function insertChairFac($model, $faccd)
    {
        $data["YEAR"][TEXT]         = substr($model->term, 0, 4);
        $data["SEMESTER"][TEXT]     = substr($model->term, 5);
        $data["CHAIRCD"][TEXT]      = $model->fields["CHAIRCD"];
        $data["FACCD"][TEXT]        = $faccd;
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "SYSDATE()";

        $query = Query::insertSQL($data, "chair_fac_dat");

        return $query;
    }
    //--- DELETE（講座使用施設データ）
    public function deleteChairFac($model)
    {
        $query  = "DELETE FROM chair_fac_dat ";
        $query .= " WHERE year     = '" . SUBSTR($model->term, 0, 4)."'";
        $query .= "   AND semester = '" . SUBSTR($model->term, 5)."'";
        $query .= "   AND chaircd  = '".$model->fields["CHAIRCD"]."'";

        return $query;
    }
    //--- DELETE（講座受講生徒データ）
    public function deleteChairStd($model)
    {
        $query  = "DELETE FROM chair_std_dat ";
        $query .= " WHERE year     = '" . SUBSTR($model->term, 0, 4)."'";
        $query .= "   AND semester = '" . SUBSTR($model->term, 5)."'";
        $query .= "   AND chaircd  = '".$model->fields["CHAIRCD"]."'";

        return $query;
    }

    //--- INSERT（講座詳細データ）
    public function insertChairDetail($model, $seq, $array)
    {
        $data = array();
        $data["YEAR"][TEXT]         = substr($model->term, 0, 4);
        $data["SEMESTER"][TEXT]     = substr($model->term, 5);
        $data["CHAIRCD"][TEXT]      = $model->fields["CHAIRCD"];
        $data["SEQ"][TEXT]          = $seq;
        foreach ($array as $key => $val) {
            $data[$key][TEXT]           = $model->fields[$val];
        }
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "SYSDATE()";

        $query = Query::insertSQL($data, "chair_detail_dat");

        return $query;
    }

    //--- DELETE（講座詳細データ）
    public function deleteChairDetail($model, $seq = "")
    {
        $query  = " DELETE FROM chair_detail_dat ";
        $query .= " WHERE year      = '" . SUBSTR($model->term, 0, 4)."' ";
        $query .= "   AND semester  = '" . SUBSTR($model->term, 5)."' ";
        $query .= "   AND chaircd   = '".$model->fields["CHAIRCD"]."' ";
        if ($seq) {
            $query .= "   AND seq       = '".$seq."' ";
        }

        return $query;
    }
    //--- INSERT（講座教科書データ）
    public function insertChairTextbook($model, $textbookcd)
    {
        $data["YEAR"][TEXT]         = substr($model->term, 0, 4);
        $data["SEMESTER"][TEXT]     = substr($model->term, 5);
        $data["CHAIRCD"][TEXT]      = $model->fields["CHAIRCD"];
        $data["TEXTBOOKCD"][TEXT]   = $textbookcd;
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "SYSDATE()";

        $query = Query::insertSQL($data, "chair_textbook_dat");

        return $query;
    }
    //--- DELETE（講座教科書データ）
    public function deleteChairTextbook($model)
    {
        $query  = "DELETE FROM chair_textbook_dat ";
        $query .= " WHERE year     = '" . SUBSTR($model->term, 0, 4)."'";
        $query .= "   AND semester = '" . SUBSTR($model->term, 5)."'";
        $query .= "   AND chaircd  = '".$model->fields["CHAIRCD"]."'";

        return $query;
    }

    //「更新ボタン」押し時の処理（メイン）・・・データがない場合、追加。ある場合、更新。
    public function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //配列データ
        $grade_class = $staffcd = $chargediv = $textbookcd = $faccd = array();
        $grade_class    = explode(",", $model->fields["GRADE_CLASS"]);
        $staffcd        = explode(",", $model->fields["STAFFCD"]);
        $chargediv      = explode(",", $model->fields["CHARGEDIV"]);
        $textbookcd     = explode(",", $model->fields["TEXTBOOKCD"]);
        $faccd          = explode(",", $model->fields["FACCD"]);

        //講座データ
        $query = knjb0030Query::countChair($model);
        if ($db->getOne($query) > 0) {
            $db->query(knjb0030Query::updateChair($model));
            $ret_val = "Update";
        } else {
            $db->query(knjb0030Query::insertChair($model));
            $ret_val = "Insert";
        }
        //講座受講クラスデータ
        $db->query(knjb0030Query::deleteChairCls($model));
        if ($model->fields["GROUPCD"]=="0000") {
            if ($model->fields["GRADE_CLASS"] != "") {
                for ($i = 0; $i < count($grade_class); $i++) {
                    $db->query(knjb0030Query::insertChairCls($model, $grade_class[$i]));
                }
            }
        }
        //講座担当職員データ
        $db->query(knjb0030Query::deleteChairStf($model));
        if ($model->fields["STAFFCD"] != "") {
            for ($i = 0; $i < count($staffcd); $i++) {
                $db->query(knjb0030Query::insertChairStf($model, $staffcd[$i], $chargediv[$i]));
            }
        }
        //講座教科書データ
        $db->query(knjb0030Query::deleteChairTextbook($model));
        if ($model->fields["TEXTBOOKCD"] != "") {
            for ($i = 0; $i < count($textbookcd); $i++) {
                $db->query(knjb0030Query::insertChairTextbook($model, $textbookcd[$i]));
            }
        }
        //講座使用施設データ
        $db->query(knjb0030Query::deleteChairFac($model));
        if ($model->fields["FACCD"] != "") {
            for ($i = 0; $i < count($faccd); $i++) {
                $db->query(knjb0030Query::insertChairFac($model, $faccd[$i]));
            }
        }
        //講座詳細データ＋１日出欠対象外
        $db->query(knjb0030Query::deleteChairDetail($model, '001'));
        if ($model->fields["STD_CNT"] != "" || $model->fields["NOT_DAY_ATTEND"] == "1") {
            $array = array("REMARK1" => "STD_CNT", "REMARK2" => "NOT_DAY_ATTEND");
            $db->query(knjb0030Query::insertChairDetail($model, '001', $array));
        }
        //講座詳細データ＋履修コース
        $db->query(knjb0030Query::deleteChairDetail($model, '002'));
        if ($model->Properties["useChairDatRisyuuCourse"] == '1') {
            if ($model->fields["RISYUU_COURSE"] != "") {
                $array = array("REMARK1" => "RISYUU_COURSE");
                $db->query(knjb0030Query::insertChairDetail($model, '002', $array));
            }
        }

        $db->commit();
        Query::dbCheckIn($db);

        return $ret_val;
    }

    //「削除ボタン」押し時の処理（メイン）
    public function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = "DELETE FROM chair_dat ";
        $query .= " WHERE year     = '" . SUBSTR($model->term, 0, 4)."'";
        $query .= "   AND semester = '" . SUBSTR($model->term, 5)."'";
        $query .= "   AND chaircd  = '".$model->fields["CHAIRCD"]."'";
        $db->query($query);

        if ($model->fields["GROUPCD"] == "0000") {
            $db->query(knjb0030Query::deleteChairCls($model));
        }
        $db->query(knjb0030Query::deleteChairStf($model));
        $db->query(knjb0030Query::deleteChairTextbook($model));
        $db->query(knjb0030Query::deleteChairFac($model));
        $db->query(knjb0030Query::deleteChairStd($model));
        $db->query(knjb0030Query::deleteChairDetail($model));

        $db->commit();
        Query::dbCheckIn($db);
        return "succeeded";
    }
}
?>
