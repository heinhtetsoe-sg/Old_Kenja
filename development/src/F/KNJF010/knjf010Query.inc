<?php

require_once('for_php7.php');
class knjf010Query extends Query {
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
    //MEDEXAM_DET_DAT(健康診断詳細データを取得)
    function getMedexam_det_dat($model)
    {
        $db = Query::dbCheckOut();

        $query  = "SELECT * FROM MEDEXAM_DET_DAT ";
        $query .= "WHERE SCHREGNO = '".$model->schregno."' AND ";
        $query .= "      YEAR = '".$model->year."'";
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }
    //生徒名前取得（学籍基礎マスタ）
    function getSchreg_Base_Mst($model)
    {
        $query  = " SELECT * FROM SCHREG_BASE_MST ";
        $query .= " WHERE SCHREGNO = '" .$model->schregno."'";
        return $query;
    }
    //生徒学年クラス取得
    function getSchreg_Regd_Dat($model)
    {
        $query  = " SELECT t1.GRADE,t1.HR_CLASS, ";
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
    //聴力 右・左状態コンボボックス
    function getLR_EAR($model)
    {
        $query = "SELECT NAMECD2,NAME1 FROM NAME_MST WHERE NAMECD1 = 'F010'";
        return $query;
    }
    //尿状態コンボボックス
    function getUric($model)
    {
        $query = "SELECT NAMECD2,NAME1 FROM NAME_MST WHERE NAMECD1 = 'F020'";
        return $query;
    }
    //栄養状態コンボボックス
    function getNutrition($model)
    {
        $query = "SELECT NAMECD2,NAME1 FROM NAME_MST WHERE NAMECD1 = 'F030'";
        return $query;
    }
    //脊柱・胸部コンボボックス
    function getSpinerib($model)
    {
        $query = "SELECT NAMECD2,NAME1 FROM NAME_MST WHERE NAMECD1 = 'F040'";
        return $query;
    }
    //目の疾病及び異常コンボボックス
    function getEyedisease($model)
    {
        $query = "SELECT NAMECD2,NAME1 FROM NAME_MST WHERE NAMECD1 = 'F050'";
        return $query;
    }
    //耳鼻咽頭疾患コンボボックス
    function getNosedisease($model)
    {
        $query = "SELECT NAMECD2,NAME1 FROM NAME_MST WHERE NAMECD1 = 'F060'";
        return $query;
    }
    //皮膚疾患コンボボックス
    function getSkindisease($model)
    {
        $query = "SELECT NAMECD2,NAME1 FROM NAME_MST WHERE NAMECD1 = 'F070'";
        return $query;
    }
    //心臓・臨床医学的検査コンボボックス
    function getHeart_medexam($model)
    {
        $query = "SELECT NAMECD2,NAME1 FROM NAME_MST WHERE NAMECD1 = 'F080'";
        return $query;
    }
    //心臓・疾病及び異常コンボボックス
    function getHeartdisease($model)
    {
        $query = "SELECT NAMECD2,NAME1 FROM NAME_MST WHERE NAMECD1 = 'F090'";
        return $query;
    }
    //結核・所見コンボボックス
    function getTb_remark($model)
    {
        $query = "SELECT NAMECD2,NAME1 FROM NAME_MST WHERE NAMECD1 = 'F100'";
        return $query;
    }
    //結核・その他検査コンボボックス
    function getTb_othertest($model)
    {
        $query = "SELECT NAMECD2,NAME1 FROM NAME_MST WHERE NAMECD1 = 'F110'";
        return $query;
    }
    //結核・病名コンボボックス
    function getTb_Name($model)
    {
        $query = "SELECT NAMECD2,NAME1 FROM NAME_MST WHERE NAMECD1 = 'F120'";
        return $query;
    }
    //結核・指導区分コンボボックス
    function getTb_Advise($model)
    {
        $query = "SELECT NAMECD2,NAME1 FROM NAME_MST WHERE NAMECD1 = 'F130'";
        return $query;
    }
    //その他疾病及び異常コンボボックス
    function getOther_disease($model)
    {
        $query = "SELECT NAMECD2,NAME1 FROM NAME_MST WHERE NAMECD1 = 'F140'";
        return $query;
    }
    //事後処置コンボボックス
    function getTreat($model)
    {
        $query = "SELECT NAMECD2,NAME1 FROM NAME_MST WHERE NAMECD1 = 'F150'";
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

    //更新対象のレコードが存在するのかチェック
    function checkHdat($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     MEDEXAM_HDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR = '{$model->year}' ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";

        return $query;
    }

    //MEDEXAM_HDAT(健康診断ヘッダデータ)を UPDATE
    function getHdat_update($model) {
        $db = Query::dbCheckOut();

        $data["DATE"][TEXT]   = $model->field["DATE"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $where = " WHERE YEAR = '{$model->year}' AND SCHREGNO = '{$model->schregno}' ";

        $db->query(Query::updateSQL($data, "MEDEXAM_HDAT", $where));
        Query::dbCheckIn($db);
        return ;
    }
    

    //MEDEXAM_HDAT(健康診断ヘッダデータ)をINSERT
    function &getHdat_insert($model)
    {
        $db = Query::dbCheckOut();

        $data["YEAR"][TEXT]         = $model->year;
        $data["SCHREGNO"][TEXT]     = $model->schregno;
        $data["DATE"][TEXT]         = $model->field["DATE"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $db->query(Query::insertSQL($data, "MEDEXAM_HDAT"));
        Query::dbCheckIn($db);
        return ;
    }
    //MEDEXAM_DET_DAT(健康診断詳細データ)をUPDATE or INSERT
    function &getInsertUpdateQuery($model,$flag)
    {
        $db = Query::dbCheckOut();

        $data["YEAR"][TEXT]                 = $model->year;
        $data["SCHREGNO"][TEXT]             = $model->schregno;
        $data["HEIGHT"][NUMBER]             = $model->field["HEIGHT"];
        $data["WEIGHT"][NUMBER]             = $model->field["WEIGHT"];
        $data["SITHEIGHT"][NUMBER]          = $model->field["SITHEIGHT"];
        $data["R_BAREVISION"][TEXT]         = $model->field["R_BAREVISION"];
        $data["R_BAREVISION_MARK"][TEXT]    = $model->field["R_BAREVISION_MARK"];
        $data["R_VISION"][TEXT]             = $model->field["R_VISION"];
        $data["R_VISION_MARK"][TEXT]        = $model->field["R_VISION_MARK"];
        $data["L_BAREVISION"][TEXT]         = $model->field["L_BAREVISION"];
        $data["L_BAREVISION_MARK"][TEXT]    = $model->field["L_BAREVISION_MARK"];
        $data["L_VISION"][TEXT]             = $model->field["L_VISION"];
        $data["L_VISION_MARK"][TEXT]        = $model->field["L_VISION_MARK"];
        $data["EYE_TEST_RESULT"][TEXT]      = $model->field["EYE_TEST_RESULT"];
        $data["R_EAR_DB"][NUMBER]           = $model->field["R_EAR_DB"];
        $data["R_EAR"][TEXT]                = $model->field["R_EAR"];
        $data["L_EAR_DB"][NUMBER]           = $model->field["L_EAR_DB"];
        $data["L_EAR"][TEXT]                = $model->field["L_EAR"];
        $data["ALBUMINURIA1CD"][TEXT]       = $model->field["ALBUMINURIA1CD"];
        $data["URICSUGAR1CD"][TEXT]         = $model->field["URICSUGAR1CD"];
        $data["URICBLEED1CD"][TEXT]         = $model->field["URICBLEED1CD"];
        $data["ALBUMINURIA2CD"][TEXT]       = $model->field["ALBUMINURIA2CD"];
        $data["URICSUGAR2CD"][TEXT]         = $model->field["URICSUGAR2CD"];
        $data["URICBLEED2CD"][TEXT]         = $model->field["URICBLEED2CD"];
        $data["URICOTHERTEST"][TEXT]        = $model->field["URICOTHERTEST"];
        $data["NUTRITIONCD"][TEXT]          = $model->field["NUTRITIONCD"];
        $data["SPINERIBCD"][TEXT]           = $model->field["SPINERIBCD"];
        $data["EYEDISEASECD"][TEXT]         = $model->field["EYEDISEASECD"];
        $data["NOSEDISEASECD"][TEXT]        = $model->field["NOSEDISEASECD"];
        $data["SKINDISEASECD"][TEXT]        = $model->field["SKINDISEASECD"];
        $data["HEART_MEDEXAM"][TEXT]        = $model->field["HEART_MEDEXAM"];
        $data["HEART_MEDEXAM_REMARK"][TEXT] = $model->field["HEART_MEDEXAM_REMARK"];
        $data["HEARTDISEASECD"][TEXT]       = $model->field["HEARTDISEASECD"];
        $data["TB_FILMDATE"][TEXT]          = $model->field["TB_FILMDATE"];
        $data["TB_FILMNO"][TEXT]            = $model->field["TB_FILMNO"];
        $data["TB_REMARKCD"][TEXT]          = $model->field["TB_REMARKCD"];
        $data["TB_OTHERTESTCD"][TEXT]       = $model->field["TB_OTHERTESTCD"];
        $data["TB_NAMECD"][TEXT]            = $model->field["TB_NAMECD"];
        $data["TB_ADVISECD"][TEXT]          = $model->field["TB_ADVISECD"];
        $data["TB_X_RAY"][TEXT]             = $model->field["TB_X_RAY"];
        $data["ANEMIA_REMARK"][TEXT]        = $model->field["ANEMIA_REMARK"];
        $data["HEMOGLOBIN"][NUMBER]         = $model->field["HEMOGLOBIN"];
        $data["OTHERDISEASECD"][TEXT]       = $model->field["OTHERDISEASECD"];
        $data["DOC_REMARK"][TEXT]           = $model->field["DOC_REMARK"];
        $data["DOC_DATE"][TEXT]             = $model->field["DOC_DATE"];
        $data["TREATCD"][TEXT]              = $model->field["TREATCD"];
        $data["REMARK"][TEXT]               = $model->field["REMARK"];
        $data["GUIDE_DIV"][TEXT]            = $model->field["GUIDE_DIV"];
        $data["JOINING_SPORTS_CLUB"][TEXT]  = $model->field["JOINING_SPORTS_CLUB"];
        $data["MEDICAL_HISTORY"][TEXT]      = $model->field["MEDICAL_HISTORY"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        if($flag == "insert"){
            $db->query(Query::insertSQL($data, "MEDEXAM_DET_DAT"));
        }else{
            $where = " WHERE YEAR = '".$model->year."' AND SCHREGNO = '".$model->schregno."' ";
            $db->query(Query::updateSQL($data, "MEDEXAM_DET_DAT", $where));
        }
        Query::dbCheckIn($db);
        return ;
    }

    //MEDEXAM_DET_DATを DELETE
    function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM MEDEXAM_DET_DAT ";
        $query .= " WHERE YEAR = '".$model->year."' AND ";
        $query .= "       SCHREGNO = '".$model->schregno."'";

        $db->query($query);
        Query::dbCheckIn($db);
        return $result;
    }

    //MEDEXAM_TOOTH_DATが存在しない場合はMEDEXAM_HDATを削除
    function getHdat_DeleteQuery($model) {
        $query  = " DELETE FROM ";
        $query .= "     MEDEXAM_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '{$model->year}' ";
        $query .= " AND SCHREGNO = '{$model->schregno}' ";
        
        $db = Query::dbCheckOut();
        $db->query($query);
        Query::dbCheckIn($db);
    }

    //TOOTH_DATがあればDATEだけNULLにする
    function getHdatDate_DeleteQuery($model) {
        $data["DATE"][TEXT]       = "";
        $data["REGISTERCD"][TEXT] = STAFFCD;
        $data["UPDATED"][FUNC]    = "sysdate()";

        $where  = " WHERE ";
        $where .= "     YEAR     = '{$model->year}' ";
        $where .= " AND SCHREGNO = '{$model->schregno}' ";

        $db = Query::dbCheckOut();
        $query = Query::updateSQL($data, "MEDEXAM_HDAT", $where);
        $db->query($query);
        Query::dbCheckIn($db);
    }

    //生徒健康診断歯口腔データ取得
    function getMedexam_tooth_dat($model)
    {
        $db = Query::dbCheckOut();

        $query  = "SELECT * FROM MEDEXAM_TOOTH_DAT ";
        $query .= "WHERE SCHREGNO = '".$model->schregno."' AND ";
        $query .= "      YEAR = '".$model->year."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }
    //一括更新処理画面用の生徒一覧
    function GetStudent($model)
    {
        $arr = explode("-",$model->GradeClass);
        $query  = "SELECT a.NAME_SHOW";
        $query .= "      ,a.SCHREGNO";
        $query .= "      ,b.ATTENDNO";
        $query .= "  FROM SCHREG_BASE_MST a RIGHT OUTER JOIN ";
        $query .= "       SCHREG_REGD_DAT b";
        $query .= "    ON a.SCHREGNO = b.SCHREGNO";
        $query .= " WHERE b.YEAR     = '".$model->year."'";
        $query .= "   AND b.SEMESTER = '".$model->semester."'";
        $query .= "   AND b.GRADE    = '".$arr[0]."'";
        $query .= "   AND b.HR_CLASS = '".$arr[1]."'";
        $query .= " ORDER BY ATTENDNO";
        return $query;
    }
    //MEDEXAM_HDATに対する一括更新、追加
    function ReplaceUpdate_Head($model,$schregno,$flag)
    {
        $data["YEAR"][TEXT]         = $model->year;
        $data["SCHREGNO"][TEXT]     = $schregno;
        $data["DATE"][TEXT]         = $model->replace_data["head_field"]["DATE"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        if($flag == "noRecord"){
            $query = Query::insertSQL($data,"MEDEXAM_HDAT");
        }
        else{
            $where  = "WHERE SCHREGNO = '" .$schregno ."' AND ";
            $where .= "      YEAR = '".$model->year."'";
            $query = Query::updateSQL($data, "MEDEXAM_HDAT", $where);
        }
        return $query;
    }
    //MEDEXAM_DET_DATに対する一括更新、追加
    function ReplaceUpdate_Det($model,$schregno,$flag)
    {
        //一括更新1
        if($model->cmd == "replace_update1"){
            foreach ($model->replace_data["det_field"] as $key => $val) {
                if($key == "R_EAR_DB" || $key == "L_EAR_DB") {
                    $data[$key][NUMBER] = $val;
                }else{
                    $data[$key][TEXT] = $val;
                }
            }
        }
        //一括更新2
        elseif($model->cmd == "replace_update2"){
            foreach ($model->replace_data["det_field"] as $key => $val) {
                $data[$key][TEXT] = $val;
            }
        }
        //一括更新3
        elseif($model->cmd == "replace_update3"){
            foreach ($model->replace_data["det_field"] as $key => $val) {
                if($key == "HEMOGLOBIN") {
                    $data[$key][NUMBER] = $val;
                }else{
                    $data[$key][TEXT] = $val;
                }
            }
        }
        //一括更新4
        elseif($model->cmd == "replace_update4"){
            foreach ($model->replace_data["det_field"] as $key => $val) {
                $data[$key][TEXT] = $val;
            }
        }
        $data["REGISTERCD"][TEXT] = STAFFCD;
        $data["UPDATED"][FUNC]    = "sysdate()";

        if($flag == "insert"){
            $data["YEAR"][TEXT]     = $model->year;
            $data["SCHREGNO"][TEXT] = $schregno;
            $query = Query::insertSQL($data,"MEDEXAM_DET_DAT");
        }
        else{
            $where  ="WHERE SCHREGNO = '".$schregno."' AND ";
            $where .="      YEAR = '".$model->year."'";
            $query = Query::updateSQL($data, "MEDEXAM_DET_DAT", $where);
        }
        return $query;
    }

    function getVisionMark() {

        $query .= " SELECT ";
        $query .= "     NAMESPARE1, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'F011' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 DESC ";

        return $query;
    }
}
?>