<?php

require_once('for_php7.php');


//通信制は基本的に通年制なので、前期のみ在籍した生徒を後期にも表示できるようにとの考えで、
//データを取得するとき学期を見ていない。
//必然的にDISTINCTが多用されますがあしからず。

class knjm435Query extends Query {

    //RECORD_DATに存在しない生徒をinsert(通信制の場合、履修学期は「０：通年」で固定）
    function insertEx_Std_RecQuery($model){

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
        //クラス番号順の場合はクラスまで指定する
        if ($model->order == 2){
            $query .= "     AND t1.SCHREGNO IN (SELECT ";
            $query .= "                             DISTINCT SCHREGNO ";
            $query .= "                         FROM ";
            $query .= "                             (select distinct schregno,grade,hr_class from schreg_regd_dat ";
            $query .= "                             where year='".CTRL_YEAR."' and grade='".substr($model->selcla,0,2)."'";
            $query .= "                             and hr_class='".substr($model->selcla,2)."' ) t3 )";
        }

      return $query;
    }


    //科目（講座）リスト
    function ReadQuery($model)
    {
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
            $query .= "AND CHAIRCD not like '92%' ";
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
            $query .= "AND CHAIRCD not like '92%' ";
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
    function ChairClassQuery($model)
    {
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
    function GetRecordDatdata($model)
    {
        //講座
        $query  = " WITH T_CHAIR AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "    t1.YEAR, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "    t2.CLASSCD, ";
            $query .= "    t2.SCHOOL_KIND, ";
            $query .= "    t2.CURRICULUM_CD, ";
        }
        $query .= "    t2.SUBCLASSCD, ";
        $query .= "    t1.SCHREGNO, ";
        $query .= "    t1.CHAIRCD ";
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
            $query .= "     t2.CLASSCD || t2.SCHOOL_KIND || t2.CURRICULUM_CD || t2.SUBCLASSCD = '".substr($model->sub,7)."' ";
        } else {
            $query .= "     t2.SUBCLASSCD = '".substr($model->sub,7)."' ";
        }
        //クラス番号順の場合はクラスまで指定する
        if ($model->order == 2){
            $query .= "     AND t1.SCHREGNO IN (SELECT ";
            $query .= "                             DISTINCT SCHREGNO ";
            $query .= "                         FROM ";
            $query .= "                             (select distinct schregno,grade,hr_class from schreg_regd_dat ";
            $query .= "                             where year='".CTRL_YEAR."' and grade='".substr($model->selcla,0,2)."'";
            $query .= "                             and hr_class='".substr($model->selcla,2)."' ) t3 )";
        }
        $query .= " ) ";

        //メイン
        $query .= " SELECT t0.SCHREGNO,t3.GRADE,t3.HR_CLASS,t3.ATTENDNO,t2.NAME_SHOW,";
        $query .= " t1.SEM1_TERM_SCORE_DI, t1.SEM1_TERM_SCORE, ";
        $query .= " t1.SEM2_TERM_SCORE_DI, t1.SEM2_TERM_SCORE, ";
        $query .= " t1.SEM1_VALUE,t1.SEM2_VALUE,t1.GRAD_VALUE,";
        $query .= "t1.get_credit,t1.comp_credit,t4.credits,t4.AUTHORIZE_FLG ";
        $query .= " FROM T_CHAIR t0 ";
        $query .= " LEFT JOIN RECORD_DAT t1 ON t1.YEAR = t0.YEAR ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= " AND t1.CLASSCD = t0.CLASSCD ";
            $query .= " AND t1.SCHOOL_KIND = t0.SCHOOL_KIND ";
            $query .= " AND t1.CURRICULUM_CD = t0.CURRICULUM_CD ";
            $query .= " AND t1.SUBCLASSCD = t0.SUBCLASSCD ";
        } else {
            $query .= " AND t1.SUBCLASSCD = t0.SUBCLASSCD ";
        }
        $query .= "     AND t1.SCHREGNO = t0.SCHREGNO ";
        $query .= " INNER JOIN SCHREG_BASE_MST t2 ON t2.SCHREGNO = t0.SCHREGNO ";
        $query .= " INNER JOIN SCHREG_REGD_DAT t3 ON t3.SCHREGNO = t0.SCHREGNO ";
        $query .= "     AND t3.YEAR = t0.YEAR ";
        $query .= "     AND t3.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " LEFT JOIN CREDIT_MST t4 ON t4.YEAR = t3.YEAR ";
        $query .= "     AND t4.COURSECD = t3.COURSECD ";
        $query .= "     AND t4.MAJORCD = t3.MAJORCD ";
        $query .= "     AND t4.COURSECODE = t3.COURSECODE ";
        $query .= "     AND t4.GRADE = t3.GRADE ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= " AND t4.CLASSCD = t0.CLASSCD ";
            $query .= " AND t4.SCHOOL_KIND = t0.SCHOOL_KIND ";
            $query .= " AND t4.CURRICULUM_CD = t0.CURRICULUM_CD ";
            $query .= " AND t4.SUBCLASSCD = t0.SUBCLASSCD ";
        } else {
            $query .= " AND t4.SUBCLASSCD = t0.SUBCLASSCD ";
        }
        //クラス番号順の場合はクラスまで指定する
        if ($model->order == 2){
            $query .= " WHERE t3.grade = '".substr($model->selcla,0,2)."' ";
            $query .= " AND t3.hr_class = '".substr($model->selcla,2)."' ";
            $query .= " ORDER BY t3.grade,t3.hr_class,t3.attendno";
        } else {
            $query .= " ORDER BY t0.SCHREGNO";
        }

        return $query;
    }



    /* データ更新処理 */
    function getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();

        for($i=1;$i<=get_count($model->setdata["SCHREGNO"]);$i++){

            $data = array();

            //前期
            if(in_array("0111",$model->adm_array) == true) {
                if ($model->setdata["SEM1_TERM_SCORE"][$i] == "*") {
                    $data["SEM1_TERM_SCORE_DI"][TEXT]  = $model->setdata["SEM1_TERM_SCORE"][$i];
                } else {
                    $data["SEM1_TERM_SCORE"][NUMBER]  = $model->setdata["SEM1_TERM_SCORE"][$i];
                    $data["SEM1_TERM_SCORE_DI"][TEXT]  = "";
                }
            }
            if(in_array("0112",$model->adm_array) == true) {
                $data["SEM1_VALUE"][NUMBER]       = $model->setdata["SEM1_VALUE"][$i];     
            }

            //単位
            if(in_array("0211",$model->adm_array) == true) {
                if ($model->setdata["SEM2_TERM_SCORE"][$i] == "*") {
                    $data["SEM2_TERM_SCORE_DI"][TEXT]  = $model->setdata["SEM2_TERM_SCORE"][$i];
                } else {
                    $data["SEM2_TERM_SCORE"][NUMBER]  = $model->setdata["SEM2_TERM_SCORE"][$i];
                    $data["SEM2_TERM_SCORE_DI"][TEXT]  = "";
                }
            }
            if(in_array("0212",$model->adm_array) == true) {
                $data["SEM2_VALUE"][NUMBER]       = $model->setdata["SEM2_VALUE"][$i];     
            }

            //評定 と 単位
            if((in_array("0882",$model->adm_array) == true) ||
               (in_array("0882",$model->adm_array) == false)) {

                //評定が入力可の時(但し評定に値がない時)、または、
                //評定が入力不可で単位評価が入力可の時、評定を自動算出する
                if((in_array("0882",$model->adm_array) == true  && !strlen($model->setdata["GRAD_VALUE"][$i])) ||
                   (in_array("0882",$model->adm_array) == false)) {
                    //評定は、通年タイプの科目は、単位評価で算出する
                    $sem1 = $model->setdata["SEM1_VALUE"][$i];
                    $sem2 = $model->setdata["SEM2_VALUE"][$i];
                    if ($model->setdata["AUTHORIZE_FLG"][$i] != "1") {
                        if (strlen($sem2)) {
                            $model->setdata["GRAD_VALUE"][$i] = $sem2;
                        } else {
                            $model->setdata["GRAD_VALUE"][$i] = "";
                        }
                    } else {
                        if (strlen($sem2)) {
                            $model->setdata["GRAD_VALUE"][$i] = $sem2;
                        } else if (strlen($sem1)) {
                            $model->setdata["GRAD_VALUE"][$i] = $sem1;
                        } else {
                            $model->setdata["GRAD_VALUE"][$i] = "";
                        }
                    }

                    //評定読替
                    if ($model->setdata["GRAD_VALUE"][$i] != "" && CTRL_YEAR >= '2013') {
                        $model->setdata["GRAD_VALUE"][$i] = $db->getOne(knjm435Query::getAssessMst("data", $model->setdata["GRAD_VALUE"][$i]));
                    }
                }

                $data["GRAD_VALUE"][NUMBER]       = $model->setdata["GRAD_VALUE"][$i];
                
                //評定が２以上の場合・・・履修単位／修得単位共に登録
                if($model->setdata["GRAD_VALUE"][$i] > "1") {
                    $data["COMP_CREDIT"][NUMBER]  = $model->setdata["CREDITS"][$i];
                    $data["GET_CREDIT"][NUMBER]  = $model->setdata["CREDITS"][$i];
                } else if($model->setdata["GRAD_VALUE"][$i] == "1") {
                //湧心館通信制では、「評定＝１」の場合は「未履修」とする。
                    $data["COMP_CREDIT"][NUMBER]  = 0;
                    $data["GET_CREDIT"][NUMBER]   = 0;
                } else if($model->setdata["GRAD_VALUE"][$i] == "0") {
                    $data["COMP_CREDIT"][NUMBER]  = 0;
                    $data["GET_CREDIT"][NUMBER]   = 0;
                } else {
                    $data["COMP_CREDIT"][NUMBER]  = NULL;
                    $data["GET_CREDIT"][NUMBER]   = NULL;
                }

            }

            $data["REGISTERCD"][TEXT]        = STAFFCD ;
            $data["UPDATED"][FUNC]           = "sysdate()";

            $where  = " WHERE YEAR           = '".CTRL_YEAR."' AND ";
            if ($model->Properties["useCurriculumcd"] == "1") {
                $where .= "       CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD     = '".substr($model->sub,7)."' AND ";
            } else {
                $where .= "       SUBCLASSCD     = '".substr($model->sub,7)."' AND ";
            }
            $where .= "       SCHREGNO       = '".$model->setdata["SCHREGNO"][$i]."' AND ";
            $where .= "       TAKESEMES      = '0' ";

            $cnt = $db->getOne("SELECT COUNT(*) FROM RECORD_DAT".$where);
            if (0 < $cnt) {
                $query = Query::updateSQL($data, "RECORD_DAT",$where);
            } else {
                $data["YEAR"][TEXT]         = CTRL_YEAR ;
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $data["CLASSCD"][TEXT]          = substr($model->sub,7,2);
                    $data["SCHOOL_KIND"][TEXT]      = substr($model->sub,9,1);
                    $data["CURRICULUM_CD"][TEXT]    = substr($model->sub,10,1);
                    $data["SUBCLASSCD"][TEXT]       = substr($model->sub,11,6);
                } else {
                    $data["SUBCLASSCD"][TEXT]       = substr($model->sub,7);
                }
                $data["SCHREGNO"][TEXT]     = $model->setdata["SCHREGNO"][$i];
                $data["TAKESEMES"][TEXT]    = '0';
                $data["CHAIRCD"][TEXT]      = substr($model->sub,0,7);
                $query = Query::insertSQL($data, "RECORD_DAT");
            }

            $db->query($query);
        }

        Query::dbCheckIn($db);

    }

    //編集可能学期の判別
    function selectContolCodeQuery($model){

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

    //評価マスタ取得
    function getAssessMst($div, $value="") {

        $query  = " SELECT ";
        if ($div == "max") {
            $query .= "    MAX(ASSESSLEVEL) ";
        } else if ($div == "min") {
            $query .= "    MIN(ASSESSLEVEL) ";
        } else {
            $query .= "    ASSESSLEVEL ";
        }
        $query .= " FROM ";
        $query .= "     ASSESS_MST ";
        $query .= " WHERE ";
        $query .= "     ASSESSCD = '3' ";
        if ($div == "data" && $value != "") {
            $query .= "     AND ".$value." <= ASSESSHIGH ";
            $query .= "     AND ".$value." >= ASSESSLOW ";
        }

        return $query;
    }
}
?>
