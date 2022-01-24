<?php

require_once('for_php7.php');


//通信制は基本的に通年制なので、前期のみ在籍した生徒を後期にも表示できるようにとの考えで、
//データを取得するとき学期を見ていない。
//必然的にDISTINCTが多用されますがあしからず。

class knjm430mQuery extends Query {

    //RECORD_DATに存在しない生徒をinsert(通信制の場合、履修学期は「０：通年」で固定）
    function insertEx_Std_RecQuery($model) {

        $query  = " INSERT INTO RECORD_DAT ";
        $query .= " (YEAR, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "  CLASSCD,";
            $query .= "  SCHOOL_KIND,";
            $query .= "  CURRICULUM_CD,";
        }
        $query .= " SUBCLASSCD,TAKESEMES,SCHREGNO,CHAIRCD,REGISTERCD,UPDATED) ";
        $query .= " SELECT DISTINCT ";
        $query .= "    t1.YEAR, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "    t2.CLASSCD, ";
            $query .= "    t2.SCHOOL_KIND, ";
            $query .= "    t2.CURRICULUM_CD, ";
        }
        $query .= "    t2.SUBCLASSCD, ";
        $query .= "    '0', ";
        $query .= "    t1.SCHREGNO, ";
        $query .= "    t1.CHAIRCD, ";
        $query .= "    '".STAFFCD."', ";
        $query .= "    sysdate() ";
        $query .= " FROM ";
        $query .= "    CHAIR_STD_DAT t1, ";
        $query .= "    CHAIR_DAT t2 ";
        $query .= " WHERE ";
        $query .= "     t1.YEAR       = t2.YEAR AND ";
        $query .= "     t1.CHAIRCD    = t2.CHAIRCD AND ";
        $query .= "     T1.semester   = T2.semester AND ";
        $query .= "     t1.YEAR       = '".CTRL_YEAR."' AND ";
        $query .= "     t1.CHAIRCD    = '".substr($model->sub,0,7)."' AND ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     t2.CLASSCD || t2.SCHOOL_KIND || t2.CURRICULUM_CD || t2.SUBCLASSCD = '".substr($model->sub,7)."' AND ";
        } else {
            $query .= "     t2.SUBCLASSCD = '".substr($model->sub,7)."' AND ";
        }
        $query .= "     t1.SCHREGNO NOT IN (SELECT ";
        $query .= "                             SCHREGNO ";
        $query .= "                         FROM ";
        $query .= "                             RECORD_DAT ";
        $query .= "                         WHERE ";
        $query .= "                             YEAR='".CTRL_YEAR."' AND ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "                             CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD = '".substr($model->sub,7)."') ";
        } else {
            $query .= "                             SUBCLASSCD = '".substr($model->sub,7)."') ";
        }

        return $query;
    }


    //科目（講座）リスト
    function ReadQuery($model) {
        //参照・更新可
        if (AUTHORITY == DEF_UPDATABLE) {
            $query  = "SELECT DISTINCT CHAIRCD,CHAIRNAME, ";
            if ($model->Properties["useCurriculumcd"] == "1") {
                $query .= " CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD AS SUBCLASSCD ";
            } else {
                $query .= " SUBCLASSCD ";
            }
            $query .= "FROM CHAIR_DAT ";
            $query .= "WHERE YEAR ='" .CTRL_YEAR ."'";
            $query .= "AND SUBSTR(SUBCLASSCD,1,2) < '92' ";
            if (CTRL_SEMESTER == '1') {
                if ($model->Properties["useCurriculumcd"] == "1") {
                    $query .= " AND CLASSCD || CURRICULUM_CD || SUBCLASSCD NOT IN (SELECT NAME1 || NAME2 || NAME3 FROM NAME_MST WHERE NAMECD1 = 'M016') ";
                } else {
                    $query .= " AND SUBCLASSCD NOT IN (SELECT NAME3 FROM NAME_MST WHERE NAMECD1 = 'M016') ";
                }
            } else if (CTRL_SEMESTER == '2') {
                if ($model->Properties["useCurriculumcd"] == "1") {
                    $query .= " AND CLASSCD || CURRICULUM_CD || SUBCLASSCD NOT IN (SELECT NAME1 || NAME2 || NAME3 FROM NAME_MST WHERE NAMECD1 = 'M015') ";
                } else {
                    $query .= " AND SUBCLASSCD NOT IN (SELECT NAME3 FROM NAME_MST WHERE NAMECD1 = 'M015') ";
                }
            }
            $query .= " ORDER BY CHAIRCD ";
        //参照・更新可（制限付き）
        } else {
            $query  = "SELECT DISTINCT CHAIRCD,CHAIRNAME, ";
            if ($model->Properties["useCurriculumcd"] == "1") {
                $query .= " CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD AS SUBCLASSCD ";
            } else {
                $query .= " SUBCLASSCD ";
            }
            $query .= "FROM CHAIR_DAT ";
            $query .= "WHERE YEAR='" .CTRL_YEAR ."'";
            $query .= "AND SUBSTR(SUBCLASSCD,1,2) < '92' ";
            if (CTRL_SEMESTER == '1') {
                if ($model->Properties["useCurriculumcd"] == "1") {
                    $query .= " AND CLASSCD || CURRICULUM_CD || SUBCLASSCD NOT IN (SELECT NAME1 || NAME2 || NAME3 FROM NAME_MST WHERE NAMECD1 = 'M016') ";
                } else {
                    $query .= " AND SUBCLASSCD NOT IN (SELECT NAME3 FROM NAME_MST WHERE NAMECD1 = 'M016') ";
                }
            } else if (CTRL_SEMESTER == '2') {
                if ($model->Properties["useCurriculumcd"] == "1") {
                    $query .= " AND CLASSCD || CURRICULUM_CD || SUBCLASSCD NOT IN (SELECT NAME1 || NAME2 || NAME3 FROM NAME_MST WHERE NAMECD1 = 'M015') ";
                } else {
                    $query .= " AND SUBCLASSCD NOT IN (SELECT NAME3 FROM NAME_MST WHERE NAMECD1 = 'M015') ";
                }
            }
            $query .= "AND CHAIRCD in ";
            $query .= " (SELECT CHAIRCD ";
            $query .= "  FROM CHAIR_STF_DAT ";
            $query .= "WHERE YEAR='" .CTRL_YEAR ."'";
            $query .= "AND STAFFCD ='" .STAFFCD ."')";
            $query .= " ORDER BY CHAIRCD ";
        }

        return $query;
    }


    //講座受講クラスリスト
    function ChairClassQuery($model) {
        $query  = "SELECT DISTINCT C.TRGTGRADE AS CGRADE,C.TRGTCLASS AS CCLASS,S.HR_NAME AS CLASSNAME ";
        $query .= " FROM (SELECT * FROM CHAIR_CLS_DAT ";
        $query .= "WHERE YEAR ='" .CTRL_YEAR ."'";
        $query .= " AND CHAIRCD = '".substr($model->sub,0,7)."' ";
        $query .= " ) C INNER JOIN ";
        $query .= " SCHREG_REGD_HDAT S ON ";
        $query .= " C.YEAR = S.YEAR AND C.SEMESTER = S.SEMESTER AND C.TRGTGRADE = S.GRADE AND C.TRGTCLASS = S.HR_CLASS";
        $query .= " ORDER BY C.TRGTGRADE,C.TRGTCLASS ";

        return $query;
    }



    //成績データ取得
    function GetRecordDatdata($model) {
        $query  = " SELECT t1.SCHREGNO,t3.GRADE,t3.HR_CLASS,t3.ATTENDNO,t2.NAME_SHOW,t3.HR_NAME,";
        $query .= "     t1.SEM1_INTR_VALUE,t1.SEM1_TERM_VALUE,t1.SEM2_INTR_VALUE,t1.SEM2_TERM_VALUE,t1.GRAD_VALUE2,t1.GRAD_VALUE,";
        $query .= "     t1.get_credit,t1.comp_credit,t4.REMARK, ";
        $query .= "     PASS1.SEM_PASS_FLG AS SEM_PASS_FLG1, ";
        $query .= "     PASS2.SEM_PASS_FLG AS SEM_PASS_FLG2, ";
        $query .= "     PASS3.SEM_PASS_FLG AS SEM_PASS_FLG3, ";
        $query .= "     CASE WHEN PASS1.SCHREGNO IS NOT NULL THEN '1' END AS SEM_PASS_FLG_RECORD1, ";
        $query .= "     CASE WHEN PASS2.SCHREGNO IS NOT NULL THEN '1' END AS SEM_PASS_FLG_RECORD2, ";
        $query .= "     CASE WHEN PASS3.SCHREGNO IS NOT NULL THEN '1' END AS SEM_PASS_FLG_RECORD3 ";
        $query .= " FROM (RECORD_DAT t1 INNER JOIN SCHREG_BASE_MST t2 ON t1.SCHREGNO = t2.SCHREGNO) ";

        $query .= " INNER JOIN (";

        $query .= " SELECT DISTINCT w1.SCHREGNO,w1.GRADE,w1.HR_CLASS,w1.ATTENDNO,w2.HR_NAME ";
        $query .= " FROM SCHREG_REGD_DAT w1 ";
        $query .= " LEFT JOIN SCHREG_REGD_HDAT w2 ";
        $query .= "     ON w1.YEAR = w2.YEAR AND w1.SEMESTER = w2.SEMESTER AND w1.GRADE = w2.GRADE AND w1.HR_CLASS = w2.HR_CLASS ";
        $query .= " LEFT JOIN SCHREG_BASE_MST w3 ";
        $query .= "     ON w3.SCHREGNO = w1.SCHREGNO ";
        $query .= " WHERE w1.YEAR = '".CTRL_YEAR."' ";
        $query .= " AND w1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " AND VALUE(w3.INOUTCD, '') <> '8' "; // 聴講生（内外区分:8）は表示しない
        $query .= " ) t3";
        $query .= " ON t1.SCHREGNO = t3.SCHREGNO";

        //備考
        $query .= " LEFT JOIN RECORD_REMARK_DAT t4 ";
        $query .= "     ON t1.YEAR = t4.YEAR AND t1.SUBCLASSCD = t4.SUBCLASSCD AND t1.SCHREGNO = t4.SCHREGNO ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= " AND t1.CLASSCD = t4.CLASSCD ";
            $query .= " AND t1.SCHOOL_KIND = t4.SCHOOL_KIND ";
            $query .= " AND t1.CURRICULUM_CD = t4.CURRICULUM_CD ";
        }

        //受験資格設定
        $query .= " LEFT JOIN SUBCLASS_STD_PASS_DAT PASS1 ON PASS1.YEAR = '".CTRL_YEAR."' ";
        $query .= "      AND PASS1.SEMESTER = '1' ";
        $query .= "      AND t1.SUBCLASSCD = PASS1.SUBCLASSCD ";
        $query .= "      AND t1.SCHREGNO = PASS1.SCHREGNO ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= " AND t1.CLASSCD = PASS1.CLASSCD ";
            $query .= " AND t1.SCHOOL_KIND = PASS1.SCHOOL_KIND ";
            $query .= " AND t1.CURRICULUM_CD = PASS1.CURRICULUM_CD ";
        }
        $query .= " LEFT JOIN SUBCLASS_STD_PASS_DAT PASS2 ON PASS2.YEAR = '".CTRL_YEAR."' ";
        $query .= "      AND PASS2.SEMESTER = '2' ";
        $query .= "      AND t1.SUBCLASSCD = PASS2.SUBCLASSCD ";
        $query .= "      AND t1.SCHREGNO = PASS2.SCHREGNO ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= " AND t1.CLASSCD = PASS2.CLASSCD ";
            $query .= " AND t1.SCHOOL_KIND = PASS2.SCHOOL_KIND ";
            $query .= " AND t1.CURRICULUM_CD = PASS2.CURRICULUM_CD ";
        }
        $query .= " LEFT JOIN SUBCLASS_STD_PASS_DAT PASS3 ON PASS3.YEAR = '".CTRL_YEAR."' ";
        $query .= "      AND PASS3.SEMESTER = '3' ";
        $query .= "      AND t1.SUBCLASSCD = PASS3.SUBCLASSCD ";
        $query .= "      AND t1.SCHREGNO = PASS3.SCHREGNO ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= " AND t1.CLASSCD = PASS3.CLASSCD ";
            $query .= " AND t1.SCHOOL_KIND = PASS3.SCHOOL_KIND ";
            $query .= " AND t1.CURRICULUM_CD = PASS3.CURRICULUM_CD ";
        }

        $query .= " WHERE t1.YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= " AND t1.CLASSCD || t1.SCHOOL_KIND || t1.CURRICULUM_CD || t1.SUBCLASSCD = '".substr($model->sub,7)."' ";
        } else {
            $query .= " AND t1.SUBCLASSCD = '".substr($model->sub,7)."' ";
        }
        //クラス番号順の場合はクラスまで指定する
        if ($model->order == 2) {
            $query .= " ORDER BY t3.grade,t3.hr_class,t3.attendno";
        } else {
            $query .= " ORDER BY SUBSTR(t1.SCHREGNO, 1, 4) DESC, SUBSTR(t1.SCHREGNO, 5, 4) ";
        }

        return $query;
    }


    //半期認定科目・補完科目【前期(M015)・後期(M016)のみの成績】
    //  NAME1:教科
    //  ABBV1:校種（未設定の場合はＨ）
    //  NAME2:教育課程
    //  NAME3:科目
    //  NAMESPARE1:予備1に'1'が立っている科目を補完科目
    function getHankiSub($model, $namecd1) {
        $query  = " SELECT COUNT(*) FROM NAME_MST ";
        $query .= " WHERE NAMECD1 = '{$namecd1}' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= " AND NAME1 || value(ABBV1,'H') || NAME2 || NAME3 = '".substr($model->sub,7)."' ";
        } else {
            $query .= " AND NAME3 = '".substr($model->sub,7)."' ";
        }
        return $query;
    }

    /* データ更新処理 */
    function getUpdateQuery($model) {
        $db = Query::dbCheckOut();

        //半期認定科目・補完科目かどうか
        $isSem1Only = $db->getOne(knjm430mQuery::getHankiSub($model, "M015"));
        $isSem2Only = $db->getOne(knjm430mQuery::getHankiSub($model, "M016"));

        for ($i = 1; $i <= get_count($model->setdata["SCHREGNO"]); $i++) {

            $remark_key = ""; //学年成績・備考で使用

            $data = array();

            $setSchreg = $model->setdata["SCHREGNO"][$i];
            //前期
            if (in_array("0112",$model->adm_array)) {
                if ($model->schregInfo[$setSchreg]["SEM_PASS_FLG"][1] == "1") {
                    $data["SEM1_INTR_VALUE"][NUMBER]  = $model->setdata["SEM1_INTR_VALUE"][$i];
                    $remark_key = "1";
                } else if ($model->schregInfo[$setSchreg]["SEM_PASS_FLG_RECORD"][1] == "1") {
                    $remark_key = "1";
                }
            }
            if (in_array("0122",$model->adm_array)) {
                if ($model->schregInfo[$setSchreg]["SEM_PASS_FLG"][1] == "1") {
                    $data["SEM1_TERM_VALUE"][NUMBER]  = $model->setdata["SEM1_TERM_VALUE"][$i];
                    $remark_key = "1";
                } else if ($model->schregInfo[$setSchreg]["SEM_PASS_FLG_RECORD"][1] == "1") {
                    $remark_key = "1";
                }
            }

            //後期
            if (in_array("0212",$model->adm_array)) {
                if ($model->schregInfo[$setSchreg]["SEM_PASS_FLG"][2] == "1") {
                    $data["SEM2_INTR_VALUE"][NUMBER]  = $model->setdata["SEM2_INTR_VALUE"][$i];
                    $remark_key = "1";
                } else if ($model->schregInfo[$setSchreg]["SEM_PASS_FLG_RECORD"][2] == "1") {
                    $remark_key = "1";
                }
            }
            if (in_array("0222",$model->adm_array)) {
                if ($model->schregInfo[$setSchreg]["SEM_PASS_FLG"][2] == "1") {
                    $data["SEM2_TERM_VALUE"][NUMBER]  = $model->setdata["SEM2_TERM_VALUE"][$i];
                    $remark_key = "1";
                } else if ($model->schregInfo[$setSchreg]["SEM_PASS_FLG_RECORD"][2] == "1") {
                    $remark_key = "1";
                }
            }

            //学年成績
            if (in_array("0882",$model->adm_array)) {
                $data["GRAD_VALUE2"][NUMBER]      = $model->setdata["GRAD_VALUE2"][$i]; //手入力
                $remark_key = "1";
            } else if ($remark_key != "") { //自動計算
                $grad_value2 = "";
                //半期認定科目・補完科目【前期(M015)】【前期のみの成績から学年成績を算出する】
                if ($isSem1Only) {
                    //補充指導点がある場合は、補充指導点を使用する
                    $grad_value2 = strlen($model->setdata["SEM1_TERM_VALUE"][$i]) ? $model->setdata["SEM1_TERM_VALUE"][$i] : $model->setdata["SEM1_INTR_VALUE"][$i];
                    //試験40未満で補充指導点がない場合は、学年成績は付かない
                    if (strlen($model->setdata["SEM1_INTR_VALUE"][$i]) && $model->setdata["SEM1_INTR_VALUE"][$i] < 40 && !strlen($model->setdata["SEM1_TERM_VALUE"][$i])) {
                        $grad_value2 = "";
                    }
                //半期認定科目・補完科目【後期(M016)】【後期のみの成績から学年成績を算出する】
                } else if ($isSem2Only) {
                    //補充指導点がある場合は、補充指導点を使用する
                    $grad_value2 = strlen($model->setdata["SEM2_TERM_VALUE"][$i]) ? $model->setdata["SEM2_TERM_VALUE"][$i] : $model->setdata["SEM2_INTR_VALUE"][$i];
                    //試験40未満で補充指導点がない場合は、学年成績は付かない
                    if (strlen($model->setdata["SEM2_INTR_VALUE"][$i]) && $model->setdata["SEM2_INTR_VALUE"][$i] < 40 && !strlen($model->setdata["SEM2_TERM_VALUE"][$i])) {
                        $grad_value2 = "";
                    }
                //通常科目
                } else {
                    //補充指導点がある場合は、補充指導点を使用する
                    $sem1 = strlen($model->setdata["SEM1_TERM_VALUE"][$i]) ? $model->setdata["SEM1_TERM_VALUE"][$i] : $model->setdata["SEM1_INTR_VALUE"][$i];
                    $sem2 = strlen($model->setdata["SEM2_TERM_VALUE"][$i]) ? $model->setdata["SEM2_TERM_VALUE"][$i] : $model->setdata["SEM2_INTR_VALUE"][$i];
                    if (CTRL_SEMESTER == "1") {
                        $grad_value2 = $sem1;
                        //試験40未満で補充指導点がない場合は、学年成績は付かない
                        if (strlen($model->setdata["SEM1_INTR_VALUE"][$i]) && $model->setdata["SEM1_INTR_VALUE"][$i] < 40 && !strlen($model->setdata["SEM1_TERM_VALUE"][$i])) {
                            $grad_value2 = "";
                        }
                    } else {
                        //学年成績は前期成績＋後期成績の平均で小数点以下四捨五入する（後期）
                        if (strlen($sem1) && strlen($sem2)) {
                            $grad_value2 = round(($sem1 + $sem2) / 2);
                        }
                        //試験40未満で補充指導点がない場合は、学年成績は付かない
                        if (strlen($model->setdata["SEM1_INTR_VALUE"][$i]) && $model->setdata["SEM1_INTR_VALUE"][$i] < 40 && !strlen($model->setdata["SEM1_TERM_VALUE"][$i])) {
                            $grad_value2 = "";
                        }
                        if (strlen($model->setdata["SEM2_INTR_VALUE"][$i]) && $model->setdata["SEM2_INTR_VALUE"][$i] < 40 && !strlen($model->setdata["SEM2_TERM_VALUE"][$i])) {
                            $grad_value2 = "";
                        }
                    }
                }
                $data["GRAD_VALUE2"][NUMBER]      = $grad_value2;
            }

            $data["REGISTERCD"][TEXT]        = STAFFCD ;
            $data["UPDATED"][FUNC]           = "sysdate()";

            //条件
            $where  = " WHERE YEAR           = '".CTRL_YEAR."' AND ";
            if ($model->Properties["useCurriculumcd"] == "1") {
                $where .= "   CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD = '".substr($model->sub,7)."' AND ";
            } else {
                $where .= "   SUBCLASSCD     = '".substr($model->sub,7)."' AND ";
            }
            $where .= "       SCHREGNO       = '".$model->setdata["SCHREGNO"][$i]."' AND ";
            $where .= "       TAKESEMES      = '0' ";

            //更新
            $query = Query::updateSQL($data, "RECORD_DAT",$where);
            $db->query($query);
//------------------------------------------------
            //備考
            if ($remark_key != "") {
                $year = CTRL_YEAR;
                $subcd = substr($model->sub,7);
                $schno = $model->setdata["SCHREGNO"][$i];
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $substr_classcd          = substr($subcd, 0, 2);
                    $substr_school_kind      = substr($subcd, 2, 1);
                    $substr_curriculum_cd    = substr($subcd, 3, 1);
                    $substr_subclasscd       = substr($subcd, 4, 6);
                }
                //削除
                $query  = "DELETE FROM RECORD_REMARK_DAT";
                $query .= " WHERE YEAR = '{$year}'";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "   AND CLASSCD          = '".$substr_classcd."' ";
                    $query .= "   AND SCHOOL_KIND      = '".$substr_school_kind."' ";
                    $query .= "   AND CURRICULUM_CD    = '".$substr_curriculum_cd."' ";
                    $query .= "   AND SUBCLASSCD       = '".$substr_subclasscd."' ";
                } else {
                    $query .= "   AND SUBCLASSCD = '{$subcd}'";
                }
                $query .= "   AND SCHREGNO = '{$schno}'";
                $db->query($query);
                //追加
                $data = array();
                $data["YEAR"][TEXT] = $year;
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $data["CLASSCD"][TEXT]       = $substr_classcd;
                    $data["SCHOOL_KIND"][TEXT]   = $substr_school_kind;
                    $data["CURRICULUM_CD"][TEXT] = $substr_curriculum_cd;
                    $data["SUBCLASSCD"][TEXT]    = $substr_subclasscd;
                } else {
                    $data["SUBCLASSCD"][TEXT] = $subcd;
                }
                $data["SCHREGNO"][TEXT] = $schno;
                $data["REMARK"][TEXT] = $model->setdata["REMARK"][$i];
                $data["REGISTERCD"][TEXT] = STAFFCD ;
                $data["UPDATED"][FUNC] = "sysdate()";
                $query = Query::insertSQL($data, "RECORD_REMARK_DAT");
                $db->query($query);
            }
//------------------------------------------------
        }

        Query::dbCheckIn($db);
    }

    //編集可能学期の判別
    function selectContolCodeQuery($model) {
        $query  = " SELECT ";
        $query .= "    CONTROL_CODE ";
        $query .= " FROM ";
        $query .= "     ADMIN_CONTROL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "                     SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        }
        $query .= "     CONTROL_FLG = '1' ";
        return $query;
    }

}
?>
