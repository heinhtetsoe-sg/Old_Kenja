<?php

require_once('for_php7.php');

class knjxhokenQuery extends Query {
    //名前
    function getName($schregno) {
        $query  = " SELECT ";
        $query .= "     NAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$schregno}' ";

        return $query;
    }

//1:保健室来室記録データよりデータを取得
    function sub1Query($model)
    {
        $query  = " WITH TYPE1 AS( ";
        $query .= " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     N1.NAME1 AS TYPE_REASON1, ";
        $query .= "     N2.NAME1 AS TYPE_REASON2, ";
        $query .= "     N3.NAME1 AS TYPE_REASON3, ";
        $query .= "     N4.NAME1 AS TYPE_TREATMENT1, ";
        $query .= "     N5.NAME1 AS TYPE_TREATMENT2, ";
        $query .= "     N6.NAME1 AS TYPE_TREATMENT3 ";
        $query .= " FROM ";
        $query .= "     NURSEOFF_VISITREC_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST N1 ON T1.VISIT_REASON1 = N1.NAMECD2 AND N1.NAMECD1 = 'F200' ";
        $query .= "     LEFT JOIN NAME_MST N2 ON T1.VISIT_REASON2 = N2.NAMECD2 AND N2.NAMECD1 = 'F200' ";
        $query .= "     LEFT JOIN NAME_MST N3 ON T1.VISIT_REASON3 = N3.NAMECD2 AND N3.NAMECD1 = 'F200' ";
        $query .= "     LEFT JOIN NAME_MST N4 ON T1.TREATMENT1 = N4.NAMECD2 AND N4.NAMECD1 = 'F208' ";
        $query .= "     LEFT JOIN NAME_MST N5 ON T1.TREATMENT2 = N5.NAMECD2 AND N5.NAMECD1 = 'F208' ";
        $query .= "     LEFT JOIN NAME_MST N6 ON T1.TREATMENT3 = N6.NAMECD2 AND N6.NAMECD1 = 'F208' ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' AND ";
        $query .= "     T1.TYPE = '1' ";
        $query .= " ), TYPE2 AS( ";
        $query .= " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     N1.NAME1 AS TYPE_REASON1, ";
        $query .= "     N2.NAME1 AS TYPE_REASON2, ";
        $query .= "     N3.NAME1 AS TYPE_REASON3, ";
        $query .= "     N4.NAME1 AS TYPE_TREATMENT1, ";
        $query .= "     N5.NAME1 AS TYPE_TREATMENT2, ";
        $query .= "     N6.NAME1 AS TYPE_TREATMENT3 ";
        $query .= " FROM ";
        $query .= "     NURSEOFF_VISITREC_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST N1 ON T1.VISIT_REASON1 = N1.NAMECD2 AND N1.NAMECD1 = 'F201' ";
        $query .= "     LEFT JOIN NAME_MST N2 ON T1.VISIT_REASON2 = N2.NAMECD2 AND N2.NAMECD1 = 'F201' ";
        $query .= "     LEFT JOIN NAME_MST N3 ON T1.VISIT_REASON3 = N3.NAMECD2 AND N3.NAMECD1 = 'F201' ";
        $query .= "     LEFT JOIN NAME_MST N4 ON T1.TREATMENT1 = N4.NAMECD2 AND N4.NAMECD1 = 'F209' ";
        $query .= "     LEFT JOIN NAME_MST N5 ON T1.TREATMENT2 = N5.NAMECD2 AND N5.NAMECD1 = 'F209' ";
        $query .= "     LEFT JOIN NAME_MST N6 ON T1.TREATMENT3 = N6.NAMECD2 AND N6.NAMECD1 = 'F209' ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' AND ";
        $query .= "     T1.TYPE = '2' ";
        $query .= " ), TYPE3 AS( ";
        $query .= " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     N1.NAME1 AS TYPE_REASON1, ";
        $query .= "     N2.NAME1 AS TYPE_REASON2, ";
        $query .= "     N3.NAME1 AS TYPE_REASON3, ";
        $query .= "     N4.NAME1 AS TYPE_TREATMENT1, ";
        $query .= "     N5.NAME1 AS TYPE_TREATMENT2, ";
        $query .= "     N6.NAME1 AS TYPE_TREATMENT3 ";
        $query .= " FROM ";
        $query .= "     NURSEOFF_VISITREC_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST N1 ON T1.VISIT_REASON1 = N1.NAMECD2 AND N1.NAMECD1 = 'F203' ";
        $query .= "     LEFT JOIN NAME_MST N2 ON T1.VISIT_REASON2 = N2.NAMECD2 AND N2.NAMECD1 = 'F203' ";
        $query .= "     LEFT JOIN NAME_MST N3 ON T1.VISIT_REASON3 = N3.NAMECD2 AND N3.NAMECD1 = 'F203' ";
        $query .= "     LEFT JOIN NAME_MST N4 ON T1.TREATMENT1 = N4.NAMECD2 AND N4.NAMECD1 = 'F210' ";
        $query .= "     LEFT JOIN NAME_MST N5 ON T1.TREATMENT2 = N5.NAMECD2 AND N5.NAMECD1 = 'F210' ";
        $query .= "     LEFT JOIN NAME_MST N6 ON T1.TREATMENT3 = N6.NAMECD2 AND N6.NAMECD1 = 'F210' ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' AND ";
        $query .= "     T1.TYPE = '3' ";
        $query .= " ), TYPE4 AS( ";
        $query .= " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     N1.NAME1 AS TYPE_REASON1, ";
        $query .= "     N2.NAME1 AS TYPE_REASON2, ";
        $query .= "     N3.NAME1 AS TYPE_REASON3, ";
        $query .= "     N4.NAME1 AS TYPE_TREATMENT1, ";
        $query .= "     N5.NAME1 AS TYPE_TREATMENT2, ";
        $query .= "     N6.NAME1 AS TYPE_TREATMENT3 ";
        $query .= " FROM ";
        $query .= "     NURSEOFF_VISITREC_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST N1 ON T1.VISIT_REASON1 = N1.NAMECD2 AND N1.NAMECD1 = 'F202' ";
        $query .= "     LEFT JOIN NAME_MST N2 ON T1.VISIT_REASON2 = N2.NAMECD2 AND N2.NAMECD1 = 'F202' ";
        $query .= "     LEFT JOIN NAME_MST N3 ON T1.VISIT_REASON3 = N3.NAMECD2 AND N3.NAMECD1 = 'F202' ";
        $query .= "     LEFT JOIN NAME_MST N4 ON T1.TREATMENT1 = N4.NAMECD2 AND N4.NAMECD1 = 'F210' ";
        $query .= "     LEFT JOIN NAME_MST N5 ON T1.TREATMENT2 = N5.NAMECD2 AND N5.NAMECD1 = 'F210' ";
        $query .= "     LEFT JOIN NAME_MST N6 ON T1.TREATMENT3 = N6.NAMECD2 AND N6.NAMECD1 = 'F210' ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' AND ";
        $query .= "     T1.TYPE = '4' ";
        $query .= " ), TYPE5 AS( ";
        $query .= " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     N1.NAME1 AS TYPE_REASON1, ";
        $query .= "     N2.NAME1 AS TYPE_REASON2, ";
        $query .= "     N3.NAME1 AS TYPE_REASON3, ";
        $query .= "     N4.NAME1 AS TYPE_TREATMENT1, ";
        $query .= "     N5.NAME1 AS TYPE_TREATMENT2, ";
        $query .= "     N6.NAME1 AS TYPE_TREATMENT3 ";
        $query .= " FROM ";
        $query .= "     NURSEOFF_VISITREC_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST N1 ON T1.VISIT_REASON1 = N1.NAMECD2 AND N1.NAMECD1 = 'F219' ";
        $query .= "     LEFT JOIN NAME_MST N2 ON T1.VISIT_REASON2 = N2.NAMECD2 AND N2.NAMECD1 = 'F219' ";
        $query .= "     LEFT JOIN NAME_MST N3 ON T1.VISIT_REASON3 = N3.NAMECD2 AND N3.NAMECD1 = 'F219' ";
        $query .= "     LEFT JOIN NAME_MST N4 ON T1.TREATMENT1 = N4.NAMECD2 AND N4.NAMECD1 = 'F220' ";
        $query .= "     LEFT JOIN NAME_MST N5 ON T1.TREATMENT2 = N5.NAMECD2 AND N5.NAMECD1 = 'F220' ";
        $query .= "     LEFT JOIN NAME_MST N6 ON T1.TREATMENT3 = N6.NAMECD2 AND N6.NAMECD1 = 'F220' ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' AND ";
        $query .= "     T1.TYPE = '5' ";
        $query .= " ), MAIN AS( ";
        $query .= " SELECT * FROM TYPE1 ";
        $query .= " UNION ";
        $query .= " SELECT * FROM TYPE2 ";
        $query .= " UNION ";
        $query .= " SELECT * FROM TYPE3 ";
        $query .= " UNION ";
        $query .= " SELECT * FROM TYPE4 ";
        $query .= " UNION ";
        $query .= " SELECT * FROM TYPE5 ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.VISIT_DATE, ";
        $query .= "     T1.VISIT_HOUR, ";
        $query .= "     T1.VISIT_MINUTE, ";
        $query .= "     T1.TYPE, ";
        $query .= "     CASE T1.TYPE WHEN '1' THEN '内科' WHEN '2' THEN '外科' WHEN '3' THEN 'その他' WHEN '4' THEN '生徒以外' ELSE '健康相談' END TYPE_NAME, ";
        $query .= "     T1.TYPE_REASON1 AS VISIT_REASON1, ";
        $query .= "     T1.TYPE_REASON2 AS VISIT_REASON2, ";
        $query .= "     T1.TYPE_REASON3 AS VISIT_REASON3, ";
        $query .= "     CASE T1.CONDITION1 WHEN '1' THEN 'はい' WHEN '2' THEN 'いいえ' WHEN '3' THEN '余り眠れない' ELSE '' END CONDITION1, ";
        $query .= "     CASE T1.CONDITION3 WHEN '1' THEN 'はい' WHEN '2' THEN 'いいえ' WHEN '3' THEN '便秘' ELSE '' END CONDITION3, ";
        $query .= "     CASE T1.CONDITION4 WHEN '1' THEN '食べた' WHEN '2' THEN '食べていない' ELSE '' END CONDITION4, ";
        $query .= "     CASE T1.CONDITION5 WHEN '1' THEN 'ある' WHEN '2' THEN 'ない' ELSE '' END CONDITION5, ";
        $query .= "     T1.CONDITION6, ";
        $query .= "     T1.TYPE_TREATMENT1 AS TREATMENT1, ";
        $query .= "     T1.TYPE_TREATMENT2 AS TREATMENT2, ";
        $query .= "     T1.TYPE_TREATMENT3 AS TREATMENT3, ";
        $query .= "     N1.NAME1 AS RESTTIME, ";
        $query .= "     N2.NAME1 AS CONTACT, ";
        $query .= "     T1.HOSPITAL, ";
        $query .= "     T1.COMPANION, ";
        $query .= "     N3.NAME1 AS COMPANION_DIV, ";
        $query .= "     T1.DIAGNOSIS, ";
        $query .= "     T1.SPECIAL_NOTE ";
        $query .= " FROM ";
        $query .= "     MAIN T1 ";
        $query .= "     LEFT JOIN NAME_MST N1 ON T1.RESTTIME = N1.NAMECD2 AND N1.NAMECD1 = 'F212' ";
        $query .= "     LEFT JOIN NAME_MST N2 ON T1.CONTACT = N2.NAMECD2 AND N2.NAMECD1 = 'F213' ";
        $query .= "     LEFT JOIN NAME_MST N3 ON T1.COMPANION_DIV = N3.NAMECD2 AND N3.NAMECD1 = 'F218' ";
        $query .= " ORDER BY ";
        $query .= "     T1.VISIT_DATE DESC, ";
        $query .= "     T1.VISIT_HOUR DESC, ";
        $query .= "     T1.VISIT_MINUTE DESC, ";
        $query .= "     T1.TYPE ";

        return $query;
    }

//2:一般、3:歯・口腔の共通
    //生徒健康診断ヘッダデータ取得
    function getMedexam_hdat($model)
    {
        $db = Query::dbCheckOut();

        $query  = "SELECT * FROM MEDEXAM_HDAT ";
        $query .= "WHERE SCHREGNO = '".$model->schregno."' AND ";
        $query .= "      YEAR = '".$model->year."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }
    //MEDEXAM_HDATにレコードがあるかチェック
    function checkMedexam_hdat($year, $schregno) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MEDEXAM_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '{$year}' AND ";
        $query .= "     SCHREGNO = '{$schregno}' ";

        return $query;
    }
    //名称マスタ
    function getNameMst($namecd1)
    {
        $query  = " SELECT ";
        $query .= "     ABBV1 AS SHOW, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || '  ' || NAME1 AS LABEL, ";
        $query .= "     CASE WHEN ABBV1 IS NULL ";
        $query .= "          THEN NAMECD2 || '  ' || NAME1 ";
        $query .= "          ELSE NAMECD2 || '  ' || ABBV1 || '  ' || NAME1 ";
        $query .= "     END AS LABEL2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '".$namecd1."'";
        return $query;
    }

//2:一般情報のSQL
    //MEDEXAM_DET_DAT(健康診断詳細データを取得)
    function getMedexam_det_dat($model)
    {
        $db = Query::dbCheckOut();

        if ($model->Properties["printKenkouSindanIppan"] == "2") {
            $query  = "SELECT * FROM V_MEDEXAM_DET_DAT ";
        } else {
            $query  = "SELECT * FROM MEDEXAM_DET_DAT ";
        }
        $query .= "WHERE SCHREGNO = '".$model->schregno."' AND ";
        $query .= "      YEAR = '".$model->year."'";
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }
    //生徒学年クラス取得
    function getSchreg_Regd_Dat($model)
    {
        $query  = " SELECT ";
        $query .= "     t1.GRADE, ";
        $query .= "     t1.HR_CLASS, ";
        $query .= "     t2.HR_NAME, ";
        $query .= "     t3.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT t1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT t2 ON t2.YEAR     = t1.YEAR ";
        $query .= "                                  AND t2.SEMESTER = t1.SEMESTER ";
        $query .= "                                  AND t2.GRADE    = t1.GRADE ";
        $query .= "                                  AND t2.HR_CLASS = t1.HR_CLASS ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT t3 ON t3.YEAR     = t1.YEAR ";
        $query .= "                                  AND t3.GRADE    = t1.GRADE ";
        $query .= " WHERE ";
        $query .= "     t1.SCHREGNO = '".$model->schregno."' AND ";
        $query .= "     t1.YEAR     = '".$model->year."' AND ";
        $query .= "     t1.SEMESTER = '".CTRL_SEMESTER."' ";
        return $query;
    }
    //聴力 右・左状態コンボボックス
    function getLR_EAR($model, $flg)
    {
        $query = "SELECT NAMECD2,NAME1 FROM NAME_MST WHERE NAMECD1 = 'F010'";
        if ($flg === '1') {
            $query .= " AND NAMESPARE3 = '1' ORDER BY NAMECD2 ";
        }
        return $query;
    }
    //尿状態コンボボックス
    function getUric($model, $namecd1, $flg)
    {
        $query = "SELECT NAMECD2,NAME1 FROM NAME_MST WHERE NAMECD1 = '{$namecd1}'";
        if ($flg === '1') {
            $query .= " AND NAMESPARE3 = '1' ORDER BY NAMECD2 ";
        }
        if ($flg === '2') {
            $query .= " AND NAMECD2 = '98' ";
        }
        return $query;
    }
    //尿:指導区分
    function getUriAdvisecd() {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'F021' ";
        return $query;
    }
    //尿：その他の検査
    function getUriCothertest() {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ' ' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'F022' ";
        return $query;
    }
    //栄養状態コンボボックス
    function getNutrition($model, $flg)
    {
        $query = "SELECT NAMECD2,NAME1 FROM NAME_MST WHERE NAMECD1 = 'F030'";
        if ($flg === '1') {
            $query .= " AND NAMESPARE3 = '1' ORDER BY NAMECD2 ";
        }
        return $query;
    }
    //脊柱・胸部コンボボックス
    function getSpinerib($model, $flg)
    {
        $query = "SELECT NAMECD2,NAME1 FROM NAME_MST WHERE NAMECD1 = 'F040'";
        if ($flg === '1') {
            $query .= " AND NAMESPARE3 = '1' ORDER BY NAMECD2 ";
        }
        return $query;
    }
    //目の疾病及び異常コンボボックス
    function getEyedisease($model, $flg)
    {
        $query = "SELECT NAMECD2,NAME1 FROM NAME_MST WHERE NAMECD1 = 'F050'";
        if ($flg === '1') {
            $query .= " AND NAMESPARE3 = '1' ORDER BY NAMECD2 ";
        }
        return $query;
    }
    //耳鼻咽頭疾患コンボボックス
    function getNosedisease($model, $flg)
    {
        $query = "SELECT NAMECD2,NAME1 FROM NAME_MST WHERE NAMECD1 = 'F060'";
        if ($flg === '1') {
            $query .= " AND NAMESPARE3 = '1' ORDER BY NAMECD2 ";
        }
        return $query;
    }
    //皮膚疾患コンボボックス
    function getSkindisease($model, $flg)
    {
        $query = "SELECT NAMECD2,NAME1 FROM NAME_MST WHERE NAMECD1 = 'F070'";
        if ($flg === '1') {
            $query .= " AND NAMESPARE3 = '1' ORDER BY NAMECD2 ";
        }
        return $query;
    }
    //心臓・臨床医学的検査コンボボックス
    function getHeart_medexam($model, $flg)
    {
        $query = "SELECT NAMECD2,NAME1 FROM NAME_MST WHERE NAMECD1 = 'F080'";
        if ($flg === '1') {
            $query .= " AND NAMESPARE3 = '1' ORDER BY NAMECD2 ";
        }
        return $query;
    }
    //心臓・疾病及び異常コンボボックス
    function getHeartdisease($model, $flg)
    {
        $query = "SELECT NAMECD2,NAME1 FROM NAME_MST WHERE NAMECD1 = 'F090'";
        if ($flg === '1') {
            $query .= " AND NAMESPARE3 = '1' ORDER BY NAMECD2 ";
        }
        return $query;
    }
    //心臓・管理区分コンボボックス
    function getManagementDiv($model)
    {
        $query = "SELECT NAMECD2,NAME1 FROM NAME_MST WHERE NAMECD1 = 'F091'";
        return $query;
    }
    //学校医・所見
    function getDoc_Cd($model)
    {
        $query = "SELECT NAMECD2,NAME1 FROM NAME_MST WHERE NAMECD1 = 'F144'";
        return $query;
    }
    //結核・所見コンボボックス
    function getTb_remark($model, $flg)
    {
        $query = "SELECT NAMECD2,NAME1 FROM NAME_MST WHERE NAMECD1 = 'F100'";
        if ($flg === '1') {
            $query .= " AND NAMESPARE3 = '1' ORDER BY NAMECD2 ";
        }
        return $query;
    }
    //結核・その他検査コンボボックス
    function getTb_othertest($model, $flg)
    {
        $query = "SELECT NAMECD2,NAME1 FROM NAME_MST WHERE NAMECD1 = 'F110'";
        if ($flg === '1') {
            $query .= " AND NAMESPARE3 = '1' ORDER BY NAMECD2 ";
        }
        return $query;
    }
    //結核・病名コンボボックス
    function getTb_Name($model, $flg)
    {
        $query = "SELECT NAMECD2,NAME1 FROM NAME_MST WHERE NAMECD1 = 'F120'";
        if ($flg === '1') {
            $query .= " AND NAMESPARE3 = '1' ORDER BY NAMECD2 ";
        }
        return $query;
    }
    //結核・指導区分コンボボックス
    function getTb_Advise($model)
    {
        $query = "SELECT NAMECD2,NAME1 FROM NAME_MST WHERE NAMECD1 = 'F130'";
        return $query;
    }

    //その他疾病及び異常コンボボックス
    function getOther_disease($model, $flg)
    {
        $query = "SELECT NAMECD2,NAME1 FROM NAME_MST WHERE NAMECD1 = 'F140'";
        if ($flg === '1') {
            $query .= " AND NAMESPARE3 = '1' ORDER BY NAMECD2 ";
        }
        return $query;
    }
    //その他疾病及び異常:指導区分
    function getOtherAdvisecd() {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'F145' ";
        return $query;
    }
    //事後処置コンボボックス
    function getTreat($model, $flg)
    {
        $query = "SELECT NAMECD2,NAME1 FROM NAME_MST WHERE NAMECD1 = 'F150'";
        if ($flg === '1') {
            $query .= " AND NAMESPARE3 = '1' ORDER BY NAMECD2 ";
        }
        return $query;
    }
    //運動/指導区分
    function getGuideDiv() {
        $query  = " SELECT ";
        $query .= "     ABBV1 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'F141' ";

        return $query;
    }
    //運動/指導区分
    function getJoiningSportsClub() {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'F142' ";

        return $query;
    }

    //運動/指導区分
    function getMedicalHist() {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'F143' ";
        $query .= " ORDER BY ";
        $query .= "     smallint(NAMECD2) ";

        return $query;
    }

    //学校名取得
    function getSchoolName() {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= " AND NAMECD2 = '00' ";
        
        return $query;
    }
    
//3:歯・口腔情報のSQL
    //生徒健康診断歯口腔データ取得
    function getMedexam_tooth_dat($model)
    {
        $db = Query::dbCheckOut();

        if ($model->Properties["printKenkouSindanIppan"] == "2") {
            $query  = "SELECT * FROM V_MEDEXAM_TOOTH_DAT ";
        } else {
            $query  = "SELECT * FROM MEDEXAM_TOOTH_DAT ";
        }
        $query .= "WHERE SCHREGNO = '".$model->schregno."' AND ";
        $query .= "      YEAR = '".$model->year."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }
    //生徒学年クラス取得
    function getSchregRegdDat_data($model)
    {
        $query  = " SELECT t1.GRADE,t1.HR_CLASS,t1.ATTENDNO, ";
        $query .= "        t2.HR_NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT t1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT t2 ON t2.YEAR = t1.YEAR ";
        $query .= "     AND t2.SEMESTER = t1.SEMESTER ";
        $query .= "     AND t2.GRADE = t1.GRADE ";
        $query .= "     AND t2.HR_CLASS = t1.HR_CLASS ";
        $query .= " WHERE t1.SCHREGNO = '" .$model->schregno."' AND ";
        $query .= "       t1.YEAR = '".$model->year."' AND ";
        $query .= "       t1.SEMESTER = '".$model->semester."' ";

        return $query;
    }

    //生徒健康診断歯口腔データ取得
    function getToothData($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MEDEXAM_TOOTH_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$model->schregno."' ";
        $query .= "     AND YEAR = '".$model->year."'";

        return $query;
    }

//4:相談記録データ取得
    function sub4Query($model) {
        $query  = " SELECT ";
        $query .= "     T1.VISIT_DATE, ";
        $query .= "     T1.VISIT_HOUR, ";
        $query .= "     T1.VISIT_MINUTE, ";
        $query .= "     T1.RELATIONSHIP, ";
        $query .= "     L1.NAME1 AS RELATIONSHIP_SHOW, ";
        $query .= "     T1.CONSULTATION_METHOD, ";
        $query .= "     L2.NAME1 AS CONSULTATION_METHOD_SHOW ";
        $query .= " FROM ";
        $query .= "     NURSEOFF_VISITREC_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'F221' AND T1.RELATIONSHIP = L1.NAMECD2 ";
        $query .= "     LEFT JOIN NAME_MST L2 ON L2.NAMECD1 = 'F215' AND T1.CONSULTATION_METHOD = L2.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "     T1.TYPE        = '6' ";
        $query .= " ORDER BY ";
        $query .= "     T1.VISIT_DATE, ";
        $query .= "     T1.VISIT_HOUR, ";
        $query .= "     T1.VISIT_MINUTE ";

        return $query;
    }
}
?>
