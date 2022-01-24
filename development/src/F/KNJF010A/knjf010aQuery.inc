<?php

require_once('for_php7.php');
class knjf010aQuery extends Query
{

    //未検査項目取得
    public function getMedexamDetNotExaminedDat($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MEDEXAM_DET_NOT_EXAMINED_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR  = '".$model->year."' ";
        $query .= "     AND GRADE = '".$model->getGrade."' ";

        return $query;
    }

    //生徒健康診断ヘッダデータ取得
    public function getMedexamHdat($model)
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
    public function checkMedexamHdat($year, $schregno)
    {
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
    public function getMedexamDetDat($model)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT * FROM V_MEDEXAM_DET_DAT ";
        $query .= " WHERE SCHREGNO = '".$model->schregno."' AND ";
        $query .= "      YEAR = '".$model->year."'";
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }
    //生徒名前取得（学籍基礎マスタ）
    public function getSchregBaseMst($model)
    {
        $query  = " SELECT * FROM SCHREG_BASE_MST ";
        $query .= " WHERE SCHREGNO = '" .$model->schregno."'";
        return $query;
    }
    //生徒学年クラス取得
    public function getSchregRegdDat($model)
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
        $query .= "     t1.SEMESTER = '".$model->semester."' ";
        return $query;
    }

    //身長・体重一覧取得
    public function getHeightWeightList($model)
    {
        $query  = " SELECT ";
        $query .= "     T2.SCHREGNO, ";
        $query .= "     T2.HEIGHT, ";
        $query .= "     T2.WEIGHT ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     MEDEXAM_DET_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = '".$model->year."' AND ";
        $query .= "     T1.SEMESTER = '".$model->semester."' AND ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "     T1.GRADE || '-' || T1.HR_CLASS = '".$model->GradeClass."' AND ";
        $query .= "     T1.SCHREGNO NOT IN ('".$model->schregno."') ";

        return $query;
    }

    //標準偏差値取得
    public function getSD($model)
    {
        $query  = " WITH T_SCHREG AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR     = '".$model->year."' AND ";
        $query .= "         SEMESTER = '".$model->semester."' AND ";
        $query .= "         GRADE || '-' || HR_CLASS = '".$model->GradeClass."' ";
        $query .= " ), T_HEIGHT AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.HEIGHT ";
        $query .= "     FROM ";
        $query .= "         MEDEXAM_DET_DAT T1 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '".$model->year."' AND ";
        $query .= "         T1.HEIGHT IS NOT NULL AND ";
        $query .= "         EXISTS(SELECT 'X' FROM T_SCHREG S1 WHERE S1.SCHREGNO = T1.SCHREGNO) ";
        $query .= " ), H_AVERAGE AS ( ";
        $query .= "     SELECT ";
        $query .= "         DECIMAL(ROUND(STDDEV(FLOAT(HEIGHT))*10,0)/10,5,1) AS H_STDDEV ";
        $query .= "     FROM ";
        $query .= "         T_HEIGHT ";
        $query .= " ), T_WEIGHT AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.WEIGHT ";
        $query .= "     FROM ";
        $query .= "         MEDEXAM_DET_DAT T1 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '".$model->year."' AND ";
        $query .= "         T1.WEIGHT IS NOT NULL AND ";
        $query .= "         EXISTS(SELECT 'X' FROM T_SCHREG S1 WHERE S1.SCHREGNO = T1.SCHREGNO) ";
        $query .= " ), W_AVERAGE AS ( ";
        $query .= "     SELECT ";
        $query .= "         DECIMAL(ROUND(STDDEV(FLOAT(WEIGHT))*10,0)/10,5,1) AS W_STDDEV ";
        $query .= "     FROM ";
        $query .= "         T_WEIGHT ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.H_STDDEV, ";
        $query .= "     T2.W_STDDEV ";
        $query .= " FROM ";
        $query .= "     H_AVERAGE T1, ";
        $query .= "     W_AVERAGE T2 ";

        return $query;
    }

    //測定評価平均値データ取得
    public function getHexamPhysicalAvgDat($model, $sex, $age)
    {
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HEXAM_PHYSICAL_AVG_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR       <= '".$model->year."' AND ";
        $query .= "     SEX         = '".$sex."' AND ";
        $query .= "     NENREI_YEAR = ".$age." ";
        $query .= " ORDER BY ";
        $query .= "     YEAR DESC FETCH FIRST 1 ROWS ONLY ";

        return $query;
    }

    //視力 右・左コンボボックス
    public function getLRBarevisionMark($model)
    {
        $query  = " SELECT ";
        $query .= "     NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' AND ";
        $query .= "     NAMECD1 = 'F017' ";
        return $query;
    }
    //聴力 右・左状態コンボボックス
    public function getLREar($model, $flg)
    {
        $query = "SELECT NAMECD2,NAME1 FROM V_NAME_MST WHERE YEAR = '{$model->year}' AND NAMECD1 = 'F010'";
        if ($flg === '1') {
            $query .= " AND NAMESPARE3 = '1' ORDER BY NAMECD2 ";
        }
        return $query;
    }
    //尿状態コンボボックス
    public function getUric($model, $namecd1, $flg = "")
    {
        $query = "SELECT NAMECD2,NAME1 FROM V_NAME_MST WHERE YEAR = '{$model->year}' AND NAMECD1 = '{$namecd1}'";
        if ($flg === '1') {
            $query .= " AND NAMESPARE3 = '1' ORDER BY NAMECD2 ";
        }
        if ($flg === '2') {
            $query .= " AND NAMECD2 = '98' ";
        }
        return $query;
    }
    //尿:指導区分
    public function getUriAdvisecd($model, $flg = "")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' AND ";
        $query .= "     NAMECD1 = 'F021' ";
        if ($flg === "1") {
            $query .= " AND NAMESPARE3 = '1' ";
        }
        return $query;
    }
    //尿：その他の検査
    public function getUriCothertest($model)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ' ' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' AND ";
        $query .= "     NAMECD1 = 'F022' ";
        return $query;
    }
    //栄養状態コンボボックス
    public function getNutrition($model, $flg)
    {
        $query = "SELECT NAMECD2, NAME1, ABBV1, ABBV2, NAMESPARE2 FROM V_NAME_MST WHERE YEAR = '{$model->year}' AND NAMECD1 = 'F030'";
        if ($flg === '1') {
            $query .= " AND NAMESPARE3 = '1' ORDER BY NAMECD2 ";
        }
        return $query;
    }
    //脊柱・胸部コンボボックス
    public function getSpinerib($model, $flg)
    {
        $query = "SELECT NAMECD2,NAME1 FROM V_NAME_MST WHERE YEAR = '{$model->year}' AND NAMECD1 = 'F040'";
        if ($flg === '1') {
            $query .= " AND NAMESPARE3 = '1' ORDER BY NAMECD2 ";
        }
        return $query;
    }
    //目の疾病及び異常コンボボックス
    public function getEyedisease($model, $flg)
    {
        $query = "SELECT NAMECD2,NAME1 FROM V_NAME_MST WHERE YEAR = '{$model->year}' AND NAMECD1 = 'F050'";
        if ($flg === '1') {
            $query .= " AND NAMESPARE3 = '1' ORDER BY NAMECD2 ";
        }
        return $query;
    }
    //色覚異常コンボ
    public function getEyedisease5($model, $namecd1, $flg)
    {
        $query = "SELECT NAMECD2,NAME1 FROM V_NAME_MST WHERE YEAR = '{$model->year}' AND NAMECD1 = '{$namecd1}'";
        if ($flg === '1') {
            $query .= " AND NAMESPARE3 = '1' ORDER BY NAMECD2 ";
        }
        return $query;
    }
    //耳疾患コンボ
    //鼻・副鼻腔疾患コンボ
    //口腔咽頭疾患・異常コンボ
    public function getNosedisease567($model, $namecd1, $flg)
    {
        $query = "SELECT NAMECD2,NAME1 FROM V_NAME_MST WHERE YEAR = '{$model->year}' AND NAMECD1 = '{$namecd1}'";
        if ($flg === '1') {
            $query .= " AND NAMESPARE3 = '1' ORDER BY NAMECD2 ";
        }
        return $query;
    }
    //耳鼻咽頭疾患コンボボックス
    public function getNosedisease($model, $flg)
    {
        $query = "SELECT NAMECD2,NAME1 FROM V_NAME_MST WHERE YEAR = '{$model->year}' AND NAMECD1 = 'F060'";
        if ($flg === '1') {
            $query .= " AND NAMESPARE3 = '1' ORDER BY NAMECD2 ";
        }
        return $query;
    }
    //皮膚疾患コンボボックス
    public function getSkindisease($model, $flg)
    {
        $query = "SELECT NAMECD2,NAME1 FROM V_NAME_MST WHERE YEAR = '{$model->year}' AND NAMECD1 = 'F070'";
        if ($flg === '1') {
            $query .= " AND NAMESPARE3 = '1' ORDER BY NAMECD2 ";
        }
        return $query;
    }
    //心臓・臨床医学的検査コンボボックス
    public function getHeartMedexam($model, $flg)
    {
        $query = "SELECT NAMECD2,NAME1 FROM V_NAME_MST WHERE YEAR = '{$model->year}' AND NAMECD1 = 'F080'";
        if ($flg === '1') {
            $query .= " AND NAMESPARE3 = '1' ORDER BY NAMECD2 ";
        }
        return $query;
    }
    //心臓・疾病及び異常コンボボックス
    public function getHeartdisease($model, $flg)
    {
        $query = "SELECT NAMECD2,NAME1 FROM V_NAME_MST WHERE YEAR = '{$model->year}' AND NAMECD1 = 'F090'";
        if ($flg === '1') {
            $query .= " AND NAMESPARE3 = '1' ORDER BY NAMECD2 ";
        }
        return $query;
    }
    //心臓・管理区分コンボボックス
    public function getManagementDiv($model)
    {
        $query = "SELECT NAMECD2,NAME1 FROM V_NAME_MST WHERE YEAR = '{$model->year}' AND NAMECD1 = 'F091'";
        return $query;
    }
    //学校医・所見
    public function getDocCd($model)
    {
        $query = "SELECT NAMECD2,NAME1 FROM V_NAME_MST WHERE YEAR = '{$model->year}' AND NAMECD1 = 'F144'";
        return $query;
    }
    //結核・所見コンボボックス
    public function getTbRemark($model, $flg)
    {
        $query = "SELECT NAMECD2,NAME1 FROM V_NAME_MST WHERE YEAR = '{$model->year}' AND NAMECD1 = 'F100'";
        if ($flg === '1') {
            $query .= " AND NAMESPARE3 = '1' ORDER BY NAMECD2 ";
        }
        return $query;
    }
    //結核・その他検査コンボボックス
    public function getTbOthertest($model, $flg)
    {
        $query = "SELECT NAMECD2,NAME1 FROM V_NAME_MST WHERE YEAR = '{$model->year}' AND NAMECD1 = 'F110'";
        if ($flg === '1') {
            $query .= " AND NAMESPARE3 = '1' ORDER BY NAMECD2 ";
        }
        return $query;
    }
    //結核・病名コンボボックス
    public function getTbName($model, $flg)
    {
        $query = "SELECT NAMECD2,NAME1 FROM V_NAME_MST WHERE YEAR = '{$model->year}' AND NAMECD1 = 'F120'";
        if ($flg === '1') {
            $query .= " AND NAMESPARE3 = '1' ORDER BY NAMECD2 ";
        }
        return $query;
    }
    //結核・指導区分コンボボックス
    public function getTbAdvise($model)
    {
        $query = "SELECT NAMECD2,NAME1 FROM V_NAME_MST WHERE YEAR = '{$model->year}' AND NAMECD1 = 'F130'";
        return $query;
    }

    //名称マスタ取得
    public function getNameMst($model, $namecd1)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ' ' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //その他疾病及び異常コンボボックス
    public function getOtherDisease($model, $flg)
    {
        $query = "SELECT NAMECD2,NAME1 FROM V_NAME_MST WHERE YEAR = '{$model->year}' AND NAMECD1 = 'F140'";
        if ($flg === '1') {
            $query .= " AND NAMESPARE3 = '1' ORDER BY NAMECD2 ";
        }
        return $query;
    }
    //その他疾病及び異常:指導区分
    public function getOtherAdvisecd($model)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' AND ";
        $query .= "     NAMECD1 = 'F145' ";
        return $query;
    }
    //事後処置コンボボックス
    public function getTreat($model, $flg)
    {
        $query = "SELECT NAMECD2,NAME1 FROM V_NAME_MST WHERE YEAR = '{$model->year}' AND NAMECD1 = 'F150'";
        if ($flg === '1') {
            $query .= " AND NAMESPARE3 = '1' ORDER BY NAMECD2 ";
        }
        return $query;
    }
    //事後処置2コンボ
    public function getTreat2($model)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' AND ";
        $query .= "     NAMECD1 = 'F151' ";

        return $query;
    }
    //運動/指導区分
    public function getGuideDiv($model)
    {
        $query  = " SELECT ";
        $query .= "     ABBV1 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' AND ";
        $query .= "     NAMECD1 = 'F141' ";

        return $query;
    }
    //運動/指導区分
    public function getJoiningSportsClub($model)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' AND ";
        $query .= "     NAMECD1 = 'F142' ";

        return $query;
    }

    //運動/指導区分
    public function getMedicalHist($model)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' AND ";
        $query .= "     NAMECD1 = 'F143' ";
        $query .= " ORDER BY ";
        $query .= "     smallint(NAMECD2) ";

        return $query;
    }

    //更新対象のレコードが存在するのかチェック
    public function checkHdat($model)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     MEDEXAM_HDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR = '{$model->year}' ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";

        return $query;
    }

    //学校名取得
    public function getSchoolName()
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= " AND NAMECD2 = '00' ";
        
        return $query;
    }

    //MEDEXAM_HDAT(健康診断ヘッダデータ)を UPDATE
    public function getHdatUpdate($model)
    {
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
    public function &getHdatInsert($model)
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
    public function &getInsertUpdateQuery($model, $flag)
    {
        $db = Query::dbCheckOut();
        //「学校医・日付」が設定されていないとき、「健康診断実施日付」をセットする
        //近大は「学校医・日付」が設定されていないとき、nullのまま更新する
        $query = knjf010aQuery::getSchoolName();
        $SchoolName = $db->getOne($query);
        if ($SchoolName !== 'KINDAI' && $SchoolName !== 'KINJUNIOR') {
            if (!$model->field["DOC_DATE"]) {
                $model->field["DOC_DATE"] = $model->field["DATE"];
            }
        }
        //熊本の帳票の時、更新時に初期値をセットする
        if ($model->Properties["printKenkouSindanIppan"] == "2") {
            //栄養状態コンボ
            $getNutrition_Shokichi = $db->getRow(knjf010aQuery::getNutrition($model, "1"), DB_FETCHMODE_ASSOC);
            $model->field["NUTRITIONCD"]    = $model->field["NUTRITIONCD"] ? $model->field["NUTRITIONCD"] : $getNutrition_Shokichi["NAMECD2"];
            //脊柱・胸部コンボ
            $getSpinerib_Shokichi = $db->getRow(knjf010aQuery::getSpinerib($model, "1"), DB_FETCHMODE_ASSOC);
            $model->field["SPINERIBCD"]     = $model->field["SPINERIBCD"] ? $model->field["SPINERIBCD"] : $getSpinerib_Shokichi["NAMECD2"];
            //目の疾病及び異常コンボ
            $getEyedisease_Shokichi = $db->getRow(knjf010aQuery::getEyedisease($model, "1"), DB_FETCHMODE_ASSOC);
            $model->field["EYEDISEASECD"]   = $model->field["EYEDISEASECD"] ? $model->field["EYEDISEASECD"] : $getEyedisease_Shokichi["NAMECD2"];
            //聴力・右状態コンボ
            $getLR_EAR_Shokichi = $db->getRow(knjf010aQuery::getLREar($model, "1"), DB_FETCHMODE_ASSOC);
            $model->field["R_EAR"]          = $model->field["R_EAR"] ? $model->field["R_EAR"] : $getLR_EAR_Shokichi["NAMECD2"];
            $model->field["L_EAR"]          = $model->field["L_EAR"] ? $model->field["L_EAR"] : $getLR_EAR_Shokichi["NAMECD2"];
            //耳鼻咽頭疾患コンボ
            $getNosedisease_Shokichi = $db->getRow(knjf010aQuery::getNosedisease($model, "1"), DB_FETCHMODE_ASSOC);
            $model->field["NOSEDISEASECD"]  = $model->field["NOSEDISEASECD"] ? $model->field["NOSEDISEASECD"] : $getNosedisease_Shokichi["NAMECD2"];
            //皮膚疾患コンボ
            $getSkindisease_Shokichi = $db->getRow(knjf010aQuery::getSkindisease($model, "1"), DB_FETCHMODE_ASSOC);
            $model->field["SKINDISEASECD"]  = $model->field["SKINDISEASECD"] ? $model->field["SKINDISEASECD"] : $getSkindisease_Shokichi["NAMECD2"];
            //結核・所見コンボ
            $getTb_remark_Shokichi = $db->getRow(knjf010aQuery::getTbRemark($model, "1"), DB_FETCHMODE_ASSOC);
            $model->field["TB_REMARKCD"]    = $model->field["TB_REMARKCD"] ? $model->field["TB_REMARKCD"] : $getTb_remark_Shokichi["NAMECD2"];
            //結核・その他検査コンボ
            $getTb_othertest_Shokichi = $db->getRow(knjf010aQuery::getTbOthertest($model, "1"), DB_FETCHMODE_ASSOC);
            $model->field["TB_OTHERTESTCD"] = $model->field["TB_OTHERTESTCD"] ? $model->field["TB_OTHERTESTCD"] : $getTb_othertest_Shokichi["NAMECD2"];
            //結核・病名コンボ
            $getTb_Name_Shokichi = $db->getRow(knjf010aQuery::getTbName($model, "1"), DB_FETCHMODE_ASSOC);
            $model->field["TB_NAMECD"]      = $model->field["TB_NAMECD"] ? $model->field["TB_NAMECD"] : $getTb_Name_Shokichi["NAMECD2"];
            //心臓・臨床医学的検査コンボ
            $getHeart_medexam_Shokichi = $db->getRow(knjf010aQuery::getHeartMedexam($model, "1"), DB_FETCHMODE_ASSOC);
            $model->field["HEART_MEDEXAM"]  = $model->field["HEART_MEDEXAM"] ? $model->field["HEART_MEDEXAM"] : $getHeart_medexam_Shokichi["NAMECD2"];
            //心臓・疾病及び異常コンボ
            $getHeartdisease_Shokichi = $db->getRow(knjf010aQuery::getHeartdisease($model, "1"), DB_FETCHMODE_ASSOC);
            $model->field["HEARTDISEASECD"] = $model->field["HEARTDISEASECD"] ? $model->field["HEARTDISEASECD"] : $getHeartdisease_Shokichi["NAMECD2"];
            //その他疾病及び異常コンボ
            $getOther_disease_Shokichi = $db->getRow(knjf010aQuery::getOtherDisease($model, "1"), DB_FETCHMODE_ASSOC);
            $model->field["OTHERDISEASECD"] = $model->field["OTHERDISEASECD"] ? $model->field["OTHERDISEASECD"] : $getOther_disease_Shokichi["NAMECD2"];
            //事後処置
            $getTreat_Shokichi = $db->getRow(knjf010aQuery::getTreat($model, "1"), DB_FETCHMODE_ASSOC);
            $model->field["TREATCD"]        = $model->field["TREATCD"] ? $model->field["TREATCD"] : $getTreat_Shokichi["NAMECD2"];
            //尿
            $getUric_Shokichi_F020 = $db->getRow(knjf010aQuery::getUric($model, "F020", "1"), DB_FETCHMODE_ASSOC);
            $getUric_Shokichi_F019 = $db->getRow(knjf010aQuery::getUric($model, "F019", "1"), DB_FETCHMODE_ASSOC);
            $getUric_Shokichi_F018 = $db->getRow(knjf010aQuery::getUric($model, "F018", "1"), DB_FETCHMODE_ASSOC);
            $model->field["ALBUMINURIA1CD"] = $model->field["ALBUMINURIA1CD"] ? $model->field["ALBUMINURIA1CD"] : $getUric_Shokichi_F020["NAMECD2"];
            $model->field["URICSUGAR1CD"]   = $model->field["URICSUGAR1CD"] ? $model->field["URICSUGAR1CD"] : $getUric_Shokichi_F019["NAMECD2"];
            $model->field["URICBLEED1CD"]   = $model->field["URICBLEED1CD"] ? $model->field["URICBLEED1CD"] : $getUric_Shokichi_F018["NAMECD2"];
            //対象外コードチェック
            $chkUric_Taishogai_F020 = $db->getOne(knjf010aQuery::getUric($model, "F020", "2"));
            $chkUric_Taishogai_F019 = $db->getOne(knjf010aQuery::getUric($model, "F019", "2"));
            $chkUric_Taishogai_F018 = $db->getOne(knjf010aQuery::getUric($model, "F018", "2"));
            if ($model->field["ALBUMINURIA2CD"] == "" && $model->field["URICSUGAR2CD"] == "" && $model->field["URICBLEED2CD"] == "") {
                $model->field["ALBUMINURIA2CD"] = $chkUric_Taishogai_F020;
                $model->field["URICSUGAR2CD"]   = $chkUric_Taishogai_F019;
                $model->field["URICBLEED2CD"]   = $chkUric_Taishogai_F018;
            }
            $model->field["ALBUMINURIA2CD"] = $model->field["ALBUMINURIA2CD"] ? $model->field["ALBUMINURIA2CD"] : $getUric_Shokichi_F020["NAMECD2"];
            $model->field["URICSUGAR2CD"]   = $model->field["URICSUGAR2CD"] ? $model->field["URICSUGAR2CD"] : $getUric_Shokichi_F019["NAMECD2"];
            $model->field["URICBLEED2CD"]   = $model->field["URICBLEED2CD"] ? $model->field["URICBLEED2CD"] : $getUric_Shokichi_F018["NAMECD2"];
        } elseif ($model->Properties["printKenkouSindanIppan"] == "1" && ($model->z010name1 == "KINDAI" || $model->z010name1 == "KINJUNIOR")) {
            //心臓・臨床医学的検査コンボ
            $getHeart_medexam_Shokichi = $db->getRow(knjf010aQuery::getHeartMedexam($model, "1"), DB_FETCHMODE_ASSOC);
            $model->field["HEART_MEDEXAM"]  = $model->field["HEART_MEDEXAM"] ? $model->field["HEART_MEDEXAM"] : $getHeart_medexam_Shokichi["NAMECD2"];
            $getHeart_uriAdviseCd_Shokichi = $db->getRow(knjf010aQuery::getUriAdvisecd($model, "1"), DB_FETCHMODE_ASSOC);
            $model->field["URI_ADVISECD"]  = $model->field["URI_ADVISECD"] ? $model->field["URI_ADVISECD"] : $getHeart_uriAdviseCd_Shokichi["VALUE"];
        }

        $data["YEAR"][TEXT]                  = $model->year;
        $data["SCHREGNO"][TEXT]              = $model->schregno;
        $data["HEIGHT"][NUMBER]              = $model->field["HEIGHT"];
        $data["WEIGHT"][NUMBER]              = $model->field["WEIGHT"];
        $data["SITHEIGHT"][NUMBER]           = $model->field["SITHEIGHT"];
        $data["R_BAREVISION"][TEXT]          = $model->field["R_BAREVISION"];
        $data["R_BAREVISION_MARK"][TEXT]     = $model->field["R_BAREVISION_MARK"];
        $data["R_VISION"][TEXT]              = $model->field["R_VISION"];
        $data["R_VISION_MARK"][TEXT]         = $model->field["R_VISION_MARK"];
        $data["L_BAREVISION"][TEXT]          = $model->field["L_BAREVISION"];
        $data["L_BAREVISION_MARK"][TEXT]     = $model->field["L_BAREVISION_MARK"];
        $data["L_VISION"][TEXT]              = $model->field["L_VISION"];
        $data["L_VISION_MARK"][TEXT]         = $model->field["L_VISION_MARK"];
        $data["EYE_TEST_RESULT"][TEXT]       = $model->field["EYE_TEST_RESULT"];
        $data["R_EAR_DB"][NUMBER]            = $model->field["R_EAR_DB"];
        $data["R_EAR"][TEXT]                 = $model->field["R_EAR"];
        $data["L_EAR_DB"][NUMBER]            = $model->field["L_EAR_DB"];
        $data["L_EAR"][TEXT]                 = $model->field["L_EAR"];
        $data["ALBUMINURIA1CD"][TEXT]        = $model->field["ALBUMINURIA1CD"];
        $data["URICSUGAR1CD"][TEXT]          = $model->field["URICSUGAR1CD"];
        $data["URICBLEED1CD"][TEXT]          = $model->field["URICBLEED1CD"];
        $data["ALBUMINURIA2CD"][TEXT]        = $model->field["ALBUMINURIA2CD"];
        $data["URICSUGAR2CD"][TEXT]          = $model->field["URICSUGAR2CD"];
        $data["URICBLEED2CD"][TEXT]          = $model->field["URICBLEED2CD"];
        $data["URICOTHERTEST"][TEXT]         = $model->field["URICOTHERTEST"];
        $data["URI_ADVISECD"][TEXT]          = $model->field["URI_ADVISECD"];
        $data["NUTRITIONCD"][TEXT]           = $model->field["NUTRITIONCD"];
        $data["SPINERIBCD"][TEXT]            = $model->field["SPINERIBCD"];
        $data["SPINERIBCD_REMARK"][TEXT]     = $model->field["SPINERIBCD_REMARK"];
        $data["EYEDISEASECD"][TEXT]          = $model->field["EYEDISEASECD"];
        $data["NOSEDISEASECD"][TEXT]         = $model->field["NOSEDISEASECD"];
        $data["NOSEDISEASECD_REMARK"][TEXT]  = $model->field["NOSEDISEASECD_REMARK"];
        $data["SKINDISEASECD"][TEXT]         = $model->field["SKINDISEASECD"];
        $data["HEART_MEDEXAM"][TEXT]         = $model->field["HEART_MEDEXAM"];
        $data["HEART_MEDEXAM_REMARK"][TEXT]  = $model->field["HEART_MEDEXAM_REMARK"];
        $data["HEARTDISEASECD"][TEXT]        = $model->field["HEARTDISEASECD"];
        $data["HEARTDISEASECD_REMARK"][TEXT] = $model->field["HEARTDISEASECD_REMARK"];
        $data["MANAGEMENT_DIV"][TEXT]        = $model->field["MANAGEMENT_DIV"];
        $data["MANAGEMENT_REMARK"][TEXT]     = $model->field["MANAGEMENT_REMARK"];
        $data["TB_FILMDATE"][TEXT]           = $model->field["TB_FILMDATE"];
        $data["TB_FILMNO"][TEXT]             = $model->field["TB_FILMNO"];
        $data["TB_REMARKCD"][TEXT]           = $model->field["TB_REMARKCD"];
        $data["TB_OTHERTESTCD"][TEXT]        = $model->field["TB_OTHERTESTCD"];
        $data["TB_NAMECD"][TEXT]             = $model->field["TB_NAMECD"];
        $data["TB_ADVISECD"][TEXT]           = $model->field["TB_ADVISECD"];
        $data["TB_X_RAY"][TEXT]              = $model->field["TB_X_RAY"];
        if ($model->isFukui || $model->isKoma) {
            $data["ANEMIA_REMARK"][TEXT]         = $model->field["ANEMIA_REMARK"];
            $data["HEMOGLOBIN"][NUMBER]          = $model->field["HEMOGLOBIN"];
        }
        if (($model->school_kind == "P" && $model->Properties["useParasite_P"] == "1") || ($model->school_kind == "J" && $model->Properties["useParasite_J"] == "1") || ($model->school_kind == "H" && $model->Properties["useParasite_H"] == "1")) {
            $data["PARASITE"][TEXT]              = $model->field["PARASITE"];
        }
        $data["OTHERDISEASECD"][TEXT]        = $model->field["OTHERDISEASECD"];
        $data["OTHER_ADVISECD"][TEXT]        = $model->field["OTHER_ADVISECD"];
        $data["OTHER_REMARK"][TEXT]          = $model->field["OTHER_REMARK"];
        if ($model->isMie) {
            $data["DOC_CD"][TEXT]                = $model->field["DOC_CD"];
        }
        $data["DOC_REMARK"][TEXT]            = $model->field["DOC_REMARK"];
        $data["DOC_DATE"][TEXT]              = $model->field["DOC_DATE"];
        $data["TREATCD"][TEXT]               = $model->field["TREATCD"];
        $data["REMARK"][TEXT]                = $model->field["REMARK"];
        $data["GUIDE_DIV"][TEXT]             = $model->field["GUIDE_DIV"];
        $data["JOINING_SPORTS_CLUB"][TEXT]   = $model->field["JOINING_SPORTS_CLUB"];
        $data["MEDICAL_HISTORY1"][TEXT]      = $model->field["MEDICAL_HISTORY1"];
        $data["MEDICAL_HISTORY2"][TEXT]      = $model->field["MEDICAL_HISTORY2"];
        $data["MEDICAL_HISTORY3"][TEXT]      = $model->field["MEDICAL_HISTORY3"];
        $data["DIAGNOSIS_NAME"][TEXT]        = $model->field["DIAGNOSIS_NAME"];
        $data["REGISTERCD"][TEXT]            = STAFFCD;
        $data["UPDATED"][FUNC]               = "sysdate()";

        if ($flag == "insert") {
            $db->query(Query::insertSQL($data, "MEDEXAM_DET_DAT"));
        } else {
            $where = " WHERE YEAR = '".$model->year."' AND SCHREGNO = '".$model->schregno."' ";
            $db->query(Query::updateSQL($data, "MEDEXAM_DET_DAT", $where));
        }

        if ($model->Properties["isMedexamDetNoDatWrite"] == "1") {
            knjf010aQuery::deleteMedexamDetNoDat($db, $model->year, $model->schregno);
            knjf010aQuery::insertMedexamDetNoDat($db, $model->year, $model->schregno, $model->field);
        }

        if ($model->Properties["printKenkouSindanIppan"] == "2") {
            $query  = " DELETE FROM MEDEXAM_DET_DETAIL_DAT ";
            $query .= " WHERE YEAR = '{$model->year}' AND SCHREGNO = '{$model->schregno}' ";
            $query .= "   AND DET_SEQ IN ('002', '003', '006', '007', '008', '009') ";
            $db->query($query);

            $data2 = array();
            $data2["YEAR"][TEXT]         = $model->year;
            $data2["SCHREGNO"][TEXT]     = $model->schregno;
            $data2["DET_SEQ"][TEXT]      = "002";
            $data2["DET_REMARK2"][TEXT]  = $model->field["EYE_TEST_RESULT2"];
            $data2["DET_REMARK3"][TEXT]  = $model->field["EYE_TEST_RESULT3"];
            $data2["DET_REMARK4"][TEXT]  = $model->field["EYEDISEASECD2"];
            $data2["DET_REMARK5"][TEXT]  = $model->field["EYEDISEASECD3"];
            $data2["DET_REMARK6"][TEXT]  = $model->field["EYEDISEASECD4"];
            $db->query(Query::insertSQL($data2, "MEDEXAM_DET_DETAIL_DAT"));

            $data2 = array();
            $data2["YEAR"][TEXT]         = $model->year;
            $data2["SCHREGNO"][TEXT]     = $model->schregno;
            $data2["DET_SEQ"][TEXT]      = "003";
            $data2["DET_REMARK2"][TEXT]  = $model->field["NOSEDISEASECD_REMARK2"];
            $data2["DET_REMARK3"][TEXT]  = $model->field["NOSEDISEASECD_REMARK3"];
            $data2["DET_REMARK4"][TEXT]  = $model->field["NOSEDISEASECD2"];
            $data2["DET_REMARK5"][TEXT]  = $model->field["NOSEDISEASECD3"];
            $data2["DET_REMARK6"][TEXT]  = $model->field["NOSEDISEASECD4"];
            $db->query(Query::insertSQL($data2, "MEDEXAM_DET_DETAIL_DAT"));

            $data2 = array();
            $data2["YEAR"][TEXT]         = $model->year;
            $data2["SCHREGNO"][TEXT]     = $model->schregno;
            $data2["DET_SEQ"][TEXT]      = "006";
            $data2["DET_REMARK1"][TEXT]  = $model->field["SKINDISEASECD_REMARK"];
            $db->query(Query::insertSQL($data2, "MEDEXAM_DET_DETAIL_DAT"));

            $data2 = array();
            $data2["YEAR"][TEXT]         = $model->year;
            $data2["SCHREGNO"][TEXT]     = $model->schregno;
            $data2["DET_SEQ"][TEXT]      = "007";
            $data2["DET_REMARK1"][TEXT]  = $model->field["URICOTHERTESTCD"];
            $db->query(Query::insertSQL($data2, "MEDEXAM_DET_DETAIL_DAT"));

            $data2 = array();
            $data2["YEAR"][TEXT]         = $model->year;
            $data2["SCHREGNO"][TEXT]     = $model->schregno;
            $data2["DET_SEQ"][TEXT]      = "008";
            $data2["DET_REMARK1"][TEXT]  = $model->field["MESSAGE"];
            $db->query(Query::insertSQL($data2, "MEDEXAM_DET_DETAIL_DAT"));

            $data2 = array();
            $data2["YEAR"][TEXT]         = $model->year;
            $data2["SCHREGNO"][TEXT]     = $model->schregno;
            $data2["DET_SEQ"][TEXT]      = "009";
            $data2["DET_REMARK1"][TEXT]  = $model->field["NUTRITIONCD_REMARK"];
            if ($model->Properties["useEar4000Hz"] == "1") {
                $data2["DET_REMARK2"][TEXT]  = $model->field["R_EAR_DB_4000"];
                $data2["DET_REMARK3"][TEXT]  = $model->field["L_EAR_DB_4000"];
            }
            $db->query(Query::insertSQL($data2, "MEDEXAM_DET_DETAIL_DAT"));
        } elseif ($model->Properties["printKenkouSindanIppan"] == "1") {
            $query  = " DELETE FROM MEDEXAM_DET_DETAIL_DAT ";
            $query .= " WHERE YEAR = '{$model->year}' AND SCHREGNO = '{$model->schregno}' ";
            $query .= "   AND DET_SEQ IN ('002', '003', '006', '009') ";
            $db->query($query);

            $data2 = array();
            $data2["YEAR"][TEXT]         = $model->year;
            $data2["SCHREGNO"][TEXT]     = $model->schregno;
            $data2["DET_SEQ"][TEXT]      = "002";
            $data2["DET_REMARK7"][TEXT]  = $model->field["EYEDISEASECD5"];
            $data2["DET_REMARK8"][TEXT]  = ($model->field["VISION_CANTMEASURE"] == "on" ? "1" : "");
            $data2["REGISTERCD"][TEXT]   = STAFFCD;
            $data2["UPDATED"][FUNC]      = "sysdate()";
            $db->query(Query::insertSQL($data2, "MEDEXAM_DET_DETAIL_DAT"));

            $data2 = array();
            $data2["YEAR"][TEXT]         = $model->year;
            $data2["SCHREGNO"][TEXT]     = $model->schregno;
            $data2["DET_SEQ"][TEXT]      = "003";
            $data2["DET_REMARK1"][TEXT]  = $model->field["NOSEDISEASECD_REMARK1"];
            $data2["DET_REMARK2"][TEXT]  = $model->field["NOSEDISEASECD_REMARK2"];
            $data2["DET_REMARK3"][TEXT]  = $model->field["NOSEDISEASECD_REMARK3"];
            $data2["DET_REMARK7"][TEXT]  = $model->field["NOSEDISEASECD5"];
            $data2["DET_REMARK8"][TEXT]  = $model->field["NOSEDISEASECD6"];
            $data2["DET_REMARK9"][TEXT]  = $model->field["NOSEDISEASECD7"];
            $data2["REGISTERCD"][TEXT]   = STAFFCD;
            $data2["UPDATED"][FUNC]      = "sysdate()";
            $db->query(Query::insertSQL($data2, "MEDEXAM_DET_DETAIL_DAT"));

            $data2 = array();
            $data2["YEAR"][TEXT]         = $model->year;
            $data2["SCHREGNO"][TEXT]     = $model->schregno;
            $data2["DET_SEQ"][TEXT]      = "006";
            $data2["DET_REMARK1"][TEXT]  = $model->field["SKINDISEASECD_REMARK"];
            $data2["REGISTERCD"][TEXT]   = STAFFCD;
            $data2["UPDATED"][FUNC]      = "sysdate()";
            $db->query(Query::insertSQL($data2, "MEDEXAM_DET_DETAIL_DAT"));

            $data2 = array();
            $data2["YEAR"][TEXT]         = $model->year;
            $data2["SCHREGNO"][TEXT]     = $model->schregno;
            $data2["DET_SEQ"][TEXT]      = "009";
            $data2["DET_REMARK1"][TEXT]  = $model->field["NUTRITIONCD_REMARK"];
            if ($model->Properties["useEar4000Hz"] == "1") {
                $data2["DET_REMARK2"][TEXT]  = $model->field["R_EAR_DB_4000"];
                $data2["DET_REMARK3"][TEXT]  = $model->field["L_EAR_DB_4000"];
            }
            if ($model->isKoma || ($model->Properties["printKenkouSindanIppan"] == "1" && $model->Properties["KenkouSindan_Ippan_Pattern"] == "1")) {  //★
                $data2["DET_REMARK5"][TEXT]  = $model->field["R_EAR_DB_1000"];
                $data2["DET_REMARK6"][TEXT]  = $model->field["L_EAR_DB_1000"];
                if ($model->isKoma) {
                $data2["DET_REMARK7"][TEXT]  = $model->field["HEART_GRAPH_NO"];
            }
            }
            $db->query(Query::insertSQL($data2, "MEDEXAM_DET_DETAIL_DAT"));
        } else {
            if ($model->Properties["useEar4000Hz"] == "1") {
                // $query = "DELETE FROM MEDEXAM_DET_DETAIL_DAT WHERE YEAR = '{$model->year}' AND SCHREGNO = '{$model->schregno}' AND DET_SEQ IN '009' ";
                $query  = " DELETE FROM MEDEXAM_DET_DETAIL_DAT ";
                $query .= " WHERE YEAR = '{$model->year}' AND SCHREGNO = '{$model->schregno}' ";
                $query .= "   AND DET_SEQ IN ('002', '009') ";
                $db->query($query);

                $data2 = array();
                $data2["YEAR"][TEXT]         = $model->year;
                $data2["SCHREGNO"][TEXT]     = $model->schregno;
                $data2["DET_SEQ"][TEXT]      = "009";
                $data2["DET_REMARK2"][TEXT]  = $model->field["R_EAR_DB_4000"];
                $data2["DET_REMARK3"][TEXT]  = $model->field["L_EAR_DB_4000"];
                $db->query(Query::insertSQL($data2, "MEDEXAM_DET_DETAIL_DAT"));
            } elseif ($model->Properties["printKenkouSindanIppan"] == "0") {
                $query = "DELETE FROM MEDEXAM_DET_DETAIL_DAT WHERE YEAR = '{$model->year}' AND SCHREGNO = '{$model->schregno}' AND DET_SEQ = '002' ";
                $db->query($query);
                //登録処理は上位パターン含めて下で実施。
            }

            if ($model->Properties["printKenkouSindanIppan"] == "0") {
                $data2 = array();
                $data2["YEAR"][TEXT]         = $model->year;
                $data2["SCHREGNO"][TEXT]     = $model->schregno;
                $data2["DET_SEQ"][TEXT]      = "002";
                $data2["DET_REMARK8"][TEXT]  = ($model->field["VISION_CANTMEASURE"] == "on" ? "1" : "");
                $data2["REGISTERCD"][TEXT]   = STAFFCD;
                $data2["UPDATED"][FUNC]      = "sysdate()";
                // $db->query(Query::insertSQL($data2, "MEDEXAM_DET_DETAIL_DAT"));
            }
        }

        $query = "DELETE FROM MEDEXAM_DET_DETAIL_DAT WHERE YEAR = '{$model->year}' AND SCHREGNO = '{$model->schregno}' AND DET_SEQ = '001' ";
        $db->query($query);
        $data2 = array();
        $data2["YEAR"][TEXT]         = $model->year;
        $data2["SCHREGNO"][TEXT]     = $model->schregno;
        $data2["DET_SEQ"][TEXT]      = "001";
        $data2["DET_REMARK2"][TEXT]  = $model->field["OTHER_REMARK2"];
        if ($model->Properties["printKenkouSindanIppan"] == "2") {
            $data2["DET_REMARK3"][TEXT]  = $model->field["OTHER_REMARK3"];
        }
        $db->query(Query::insertSQL($data2, "MEDEXAM_DET_DETAIL_DAT"));

        $query = "DELETE FROM MEDEXAM_DET_DETAIL_DAT WHERE YEAR = '{$model->year}' AND SCHREGNO = '{$model->schregno}' AND DET_SEQ = '005' ";
        $db->query($query);
        $data2 = array();
        $data2["YEAR"][TEXT]         = $model->year;
        $data2["SCHREGNO"][TEXT]     = $model->schregno;
        $data2["DET_SEQ"][TEXT]      = "005";
        $data2["DET_REMARK2"][TEXT]  = $model->field["TB_NAME_REMARK1"];
        if ($model->Properties["printKenkouSindanIppan"] == "2") {
            $data2["DET_REMARK1"][TEXT]  = $model->field["TB_OTHERTEST_REMARK1"];
            $data2["DET_REMARK3"][TEXT]  = $model->field["TB_ADVISE_REMARK1"];
        }
        $db->query(Query::insertSQL($data2, "MEDEXAM_DET_DETAIL_DAT"));

        $query = "DELETE FROM MEDEXAM_DET_DETAIL_DAT WHERE YEAR = '{$model->year}' AND SCHREGNO = '{$model->schregno}' AND DET_SEQ = '004' ";
        $db->query($query);
        $data2 = array();
        $data2["YEAR"][TEXT]         = $model->year;
        $data2["SCHREGNO"][TEXT]     = $model->schregno;
        $data2["DET_SEQ"][TEXT]      = "004";
        $data2["DET_REMARK1"][TEXT]  = $model->field["TREAT_REMARK1"];
        if ($model->Properties["printKenkouSindanIppan"] == "2") {
            $data2["DET_REMARK2"][TEXT]  = $model->field["TREAT_REMARK2"];
            $data2["DET_REMARK3"][TEXT]  = $model->field["TREAT_REMARK3"];
        }
        $db->query(Query::insertSQL($data2, "MEDEXAM_DET_DETAIL_DAT"));

        if ($model->Properties["printKenkouSindanIppan"] != "2") {
            $query = "DELETE FROM MEDEXAM_DET_DETAIL_DAT WHERE YEAR = '{$model->year}' AND SCHREGNO = '{$model->schregno}' AND DET_SEQ = '011' ";
            $db->query($query);
            $data2 = array();
            $data2["YEAR"][TEXT]         = $model->year;
            $data2["SCHREGNO"][TEXT]     = $model->schregno;
            $data2["DET_SEQ"][TEXT]      = "011";
            $data2["DET_REMARK1"][TEXT]  = $model->field["TREATCD2"];
            $db->query(Query::insertSQL($data2, "MEDEXAM_DET_DETAIL_DAT"));
        }

        //MEDEXAM_DET_DETAIL_DAT(012)
        $s012Cnt = $db->getOne(knjf010aQuery::getDetailCnt($model->year, $model->schregno, '012'));
        $data2 = array();
        $data2["YEAR"][TEXT]         = $model->year;
        $data2["SCHREGNO"][TEXT]     = $model->schregno;
        $data2["DET_SEQ"][TEXT]      = '012';
        $data2["DET_REMARK1"][TEXT]  = $model->field["R_VISION_CANTMEASURE"];    // 視力・測定困難（右）
        $data2["DET_REMARK2"][TEXT]  = $model->field["L_VISION_CANTMEASURE"];    // 視力・測定困難（左）
        $data2["DET_REMARK3"][TEXT]  = $model->field["R_EAR_CANTMEASURE"];       // 聴力・測定困難（右）
        $data2["DET_REMARK4"][TEXT]  = $model->field["L_EAR_CANTMEASURE"];       // 聴力・測定困難（左）
        $data2["DET_REMARK5"][TEXT]  = $model->field["NO_PRINT_OBESITY_INDEX"];  // 肥満度印字無し
        $data2["REGISTERCD"][TEXT]   = STAFFCD;
        $data2["UPDATED"][FUNC]      = "sysdate()";

        if ($s012Cnt == 0) {
            $query = Query::insertSQL($data2, "MEDEXAM_DET_DETAIL_DAT");
        } else {
            $where  = " WHERE YEAR     = '{$model->year}' ";
            $where .= "   AND SCHREGNO = '{$model->schregno}' ";
            $where .= "   AND DET_SEQ  = '012'";
            $query = Query::updateSQL($data2, "MEDEXAM_DET_DETAIL_DAT", $where);
        }
        $db->query($query);

        if ($model->isMie) {
            //三重の場合、$model->Properties["printKenkouSindanIppan"] = "1"であり、かつ、$model->field["TREAT_REMARK1"]は入力不可で空文字となるため、別項目で設定する学校医を設定する。
            $where  = " WHERE YEAR     = '{$model->year}' ";
            $where .= "   AND SCHREGNO = '{$model->schregno}' ";
            $where .= "   AND DET_SEQ  = '013'";
            $s013Cnt = $db->getOne(" SELECT COUNT(*) FROM MEDEXAM_DET_DETAIL_DAT".$where);
            $data2 = array();
            $data2["DET_REMARK1"][TEXT]  = $model->field["DOC_NAME"];
            $data2["REGISTERCD"][TEXT]   = STAFFCD;
            $data2["UPDATED"][FUNC]      = "sysdate()";
            if ($s013Cnt == 0) {
                $data2["YEAR"][TEXT]         = $model->year;
                $data2["SCHREGNO"][TEXT]     = $model->schregno;
                $data2["DET_SEQ"][TEXT]      = "013";
                $db->query(Query::insertSQL($data2, "MEDEXAM_DET_DETAIL_DAT"));
            } else {
                $db->query(Query::updateSQL($data2, "MEDEXAM_DET_DETAIL_DAT", $where));
            }
        }
        if ($model->Properties["printKenkouSindanIppan"] == "1" && $model->Properties["KenkouSindan_Ippan_Pattern"] == "1") {
            //MEDEXAM_DET_DETAIL_DAT(017)
            $s017Cnt = $db->getOne(knjf010aQuery::getDetailCnt($model->year, $model->schregno, '017'));
            $data2 = array();
            $data2["YEAR"][TEXT]         = $model->year;
            $data2["SCHREGNO"][TEXT]     = $model->schregno;
            $data2["DET_SEQ"][TEXT]      = '017';
            $data2["DET_REMARK1"][TEXT]  = $model->field["URICPH1"];  // 1次PH
            $data2["DET_REMARK2"][TEXT]  = $model->field["URICPH2"];  // 2次PH
            $data2["REGISTERCD"][TEXT]   = STAFFCD;
            $data2["UPDATED"][FUNC]      = "sysdate()";

            if ($s017Cnt == 0) {
                $query = Query::insertSQL($data2, "MEDEXAM_DET_DETAIL_DAT");
            } else {
                $where  = " WHERE YEAR     = '{$model->year}' ";
                $where .= "   AND SCHREGNO = '{$model->schregno}' ";
                $where .= "   AND DET_SEQ  = '017'";
                $query = Query::updateSQL($data2, "MEDEXAM_DET_DETAIL_DAT", $where);
            }
            $db->query($query);
        }
        //特別支援学校
        if ($model->Properties["useSpecial_Support_School"] == "1") {
            //MEDEXAM_DET_DETAIL_DAT(018)
            $s018Cnt = $db->getOne(knjf010aQuery::getDetailCnt($model->year, $model->schregno, '018'));
            $data2 = array();
            $data2["YEAR"][TEXT]         = $model->year;
            $data2["SCHREGNO"][TEXT]     = $model->schregno;
            $data2["DET_SEQ"][TEXT]      = '018';
            $data2["DET_REMARK1"][TEXT]  = $model->field["R_EAR_IN"];           // 聴力・右状態  （装用時）
            $data2["DET_REMARK2"][TEXT]  = $model->field["R_EAR_DB_IN"];        // 聴力・右DB    （装用時）
            $data2["DET_REMARK3"][TEXT]  = $model->field["R_EAR_DB_4000_IN"];   // 聴力・右4000Hz（装用時）
            $data2["DET_REMARK4"][TEXT]  = $model->field["L_EAR_IN"];           // 聴力・左状態  （装用時）
            $data2["DET_REMARK5"][TEXT]  = $model->field["L_EAR_DB_IN"];        // 聴力・左DB    （装用時）
            $data2["DET_REMARK6"][TEXT]  = $model->field["L_EAR_DB_4000_IN"];   // 聴力・左4000Hz（装用時）
            $data2["REGISTERCD"][TEXT]   = STAFFCD;
            $data2["UPDATED"][FUNC]      = "sysdate()";

            if ($s018Cnt == 0) {
                $query = Query::insertSQL($data2, "MEDEXAM_DET_DETAIL_DAT");
            } else {
                $where  = " WHERE YEAR     = '{$model->year}' ";
                $where .= "   AND SCHREGNO = '{$model->schregno}' ";
                $where .= "   AND DET_SEQ  = '018'";
                $query = Query::updateSQL($data2, "MEDEXAM_DET_DETAIL_DAT", $where);
            }
            $db->query($query);
        }

        Query::dbCheckIn($db);
        return ;
    }

    //カウント取得(MEDEXAM_DET_DETAIL_DAT)
    public function getDetailCnt($year, $schregno, $seq)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     MEDEXAM_DET_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '{$year}' ";
        $query .= "     AND SCHREGNO = '{$schregno}' ";
        $query .= "     AND DET_SEQ  = '{$seq}' ";

        return $query;
    }

    //MEDEXAM_DET_DATを DELETE
    public function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM MEDEXAM_DET_DAT ";
        $query .= " WHERE YEAR = '".$model->year."' AND ";
        $query .= "       SCHREGNO = '".$model->schregno."'";

        $db->query($query);

        if ($model->Properties["isMedexamDetNoDatWrite"] == "1") {
            knjf010aQuery::deleteMedexamDetNoDat($db, $model->year, $model->schregno);
        }

        $query  = " DELETE FROM MEDEXAM_DET_DETAIL_DAT ";
        $query .= " WHERE YEAR = '".$model->year."' AND ";
        $query .= "       SCHREGNO = '".$model->schregno."' AND ";
        $query .= "       DET_SEQ <> '010'";

        $db->query($query);
        Query::dbCheckIn($db);
        return $result;
    }

    //MEDEXAM_TOOTH_DATが存在しない場合はMEDEXAM_HDATを削除
    public function getHdatDeleteQuery($model)
    {
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
    public function getHdatDateDeleteQuery($model)
    {
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
    public function getMedexamToothDat($model)
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
    public function getStudent($model)
    {
        $arr = explode("-", $model->GradeClass);
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
    public function replaceUpdateHead($model, $schregno, $flag)
    {
        $data["YEAR"][TEXT]         = $model->year;
        $data["SCHREGNO"][TEXT]     = $schregno;
        $data["DATE"][TEXT]         = $model->replace_data["head_field"]["DATE"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        if ($flag == "noRecord") {
            $query = Query::insertSQL($data, "MEDEXAM_HDAT");
        } else {
            $where  = "WHERE SCHREGNO = '" .$schregno ."' AND ";
            $where .= "      YEAR = '".$model->year."'";
            $query = Query::updateSQL($data, "MEDEXAM_HDAT", $where);
        }
        return $query;
    }

    public function getDocDate($year, $schregno)
    {
        $query  = " SELECT ";
        $query .= "     DOC_DATE ";
        $query .= " FROM ";
        $query .= "     MEDEXAM_DET_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' AND ";
        $query .= "     SCHREGNO = '{$schregno}' ";

        return $query;
    }

    public function getCntDetailDat($year, $schregno, $seq)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MEDEXAM_DET_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '{$year}' ";
        $query .= "     AND SCHREGNO = '{$schregno}' ";
        $query .= "     AND DET_SEQ  = '{$seq}' ";

        return $query;
    }

    //MEDEXAM_DET_DATに対する一括更新、追加
    public function replaceUpdateDet($model, $schregno, $flag, $db)
    {
        //一括更新1
        if ($model->cmd == "replace_update1") {
            $cnt012 = $db->getOne(knjf010aQuery::getCntDetailDat($model->year, $schregno, '012'));
            $cnt009 = $db->getOne(knjf010aQuery::getCntDetailDat($model->year, $schregno, '009'));
            $cnt018 = $db->getOne(knjf010aQuery::getCntDetailDat($model->year, $schregno, '018'));

            //視力・聴力・測定困難
            if ($model->replace_data["det_check"][0] == "1" || $model->replace_data["det_check"][2] == "1") {
                $data2 = array();
                if ($model->replace_data["det_check"][0] == "1") {
                    $data2["DET_REMARK1"][TEXT]  = $model->replace_data["det_field"]["R_VISION_CANTMEASURE"];
                }
                if ($model->replace_data["det_check"][2] == "1") {
                    $data2["DET_REMARK2"][TEXT]  = $model->replace_data["det_field"]["L_VISION_CANTMEASURE"];
                }
                if ($cnt012 > 0) {
                    $where  = " WHERE ";
                    $where .= "     YEAR            = '{$model->year}' ";
                    $where .= "     AND SCHREGNO    = '{$schregno}' ";
                    $where .= "     AND DET_SEQ     = '012' ";
                    $db->query(Query::updateSQL($data2, "MEDEXAM_DET_DETAIL_DAT", $where));
                } else {
                    $data2["YEAR"][TEXT]         = $model->year;
                    $data2["SCHREGNO"][TEXT]     = $schregno;
                    $data2["DET_SEQ"][TEXT]      = "012";
                    $db->query(Query::insertSQL($data2, "MEDEXAM_DET_DETAIL_DAT"));
                }
            }

            if ($model->isFukui || ($model->Properties["useSpecial_Support_School"] != "1" && $model->Properties["unEar_db"] == "1")) {
                if ($model->replace_data["det_check"][4] == "1" || $model->replace_data["det_check"][5] == "1") {
                    //聴力・測定困難
                    $data2 = array();
                    if ($model->replace_data["det_check"][4] == "1") {
                        $data2["DET_REMARK3"][TEXT]  = $model->replace_data["det_field"]["R_EAR_CANTMEASURE"];
                    }
                    if ($model->replace_data["det_check"][5] == "1") {
                        $data2["DET_REMARK4"][TEXT]  = $model->replace_data["det_field"]["L_EAR_CANTMEASURE"];
                    }
                    $cnt012 = $db->getOne(knjf010aQuery::getCntDetailDat($model->year, $schregno, '012'));
                    if ($cnt012 > 0) {
                        $where  = " WHERE ";
                        $where .= "     YEAR            = '{$model->year}' ";
                        $where .= "     AND SCHREGNO    = '{$schregno}' ";
                        $where .= "     AND DET_SEQ     = '012' ";
                        $db->query(Query::updateSQL($data2, "MEDEXAM_DET_DETAIL_DAT", $where));
                    } else {
                        $data2["YEAR"][TEXT]         = $model->year;
                        $data2["SCHREGNO"][TEXT]     = $schregno;
                        $data2["DET_SEQ"][TEXT]      = "012";
                        $db->query(Query::insertSQL($data2, "MEDEXAM_DET_DETAIL_DAT"));
                    }
                    //特別支援学校
                    if ($model->Properties["useSpecial_Support_School"] == "1") {
                        //聴力(装用時)
                        $data2 = array();
                        if ($model->replace_data["det_check"][4] == "1") {
                            $data2["DET_REMARK1"][TEXT]  = $model->replace_data["det_field"]["R_EAR_IN"];
                        }
                        if ($model->replace_data["det_check"][5] == "1") {
                            $data2["DET_REMARK4"][TEXT]  = $model->replace_data["det_field"]["L_EAR_IN"];
                        }
                        if ($cnt018 > 0) {
                            $where  = " WHERE ";
                            $where .= "     YEAR            = '{$model->year}' ";
                            $where .= "     AND SCHREGNO    = '{$schregno}' ";
                            $where .= "     AND DET_SEQ     = '018' ";
                            $db->query(Query::updateSQL($data2, "MEDEXAM_DET_DETAIL_DAT", $where));
                        } else {
                            $data2["YEAR"][TEXT]         = $model->year;
                            $data2["SCHREGNO"][TEXT]     = $schregno;
                            $data2["DET_SEQ"][TEXT]      = "018";
                            $db->query(Query::insertSQL($data2, "MEDEXAM_DET_DETAIL_DAT"));
                        }
                    }
                }
            } else {
                if ($model->Properties["useEar4000Hz"] == "1") {
                    if ($model->isKoma || ($model->Properties["printKenkouSindanIppan"] == "1" && $model->Properties["KenkouSindan_Ippan_Pattern"] == "1")) {  //★
                        $r4idx = 5;
                        $l4idx = 8;
                        $r1idx = 4;
                        $l1idx = 7;
                        $rcmidx = 4;
                        $lcmidx = 7;
                    } else {
                        $r4idx = 5;
                        $l4idx = 8;
                        $rcmidx = 4;
                        $lcmidx = 7;
                    }
                    if ($model->replace_data["det_check"][$r4idx] == "1" || $model->replace_data["det_check"][$l4idx] == "1"
                        || (($model->isKoma || ($model->Properties["printKenkouSindanIppan"] == "1" && $model->Properties["KenkouSindan_Ippan_Pattern"] == "1"))  //★
                              && ($model->replace_data["det_check"][$r1idx] == "1" || $model->replace_data["det_check"][$l1idx] == "1"))
                       ) {
                        $data2 = array();
                        if ($model->replace_data["det_check"][$r4idx] == "1") {
                            $data2["DET_REMARK2"][TEXT]  = $model->replace_data["det_field"]["R_EAR_DB_4000"];
                        }
                        if ($model->replace_data["det_check"][$l4idx] == "1") {
                            $data2["DET_REMARK3"][TEXT]  = $model->replace_data["det_field"]["L_EAR_DB_4000"];
                        }
                        if (($model->isKoma || ($model->Properties["printKenkouSindanIppan"] == "1" && $model->Properties["KenkouSindan_Ippan_Pattern"] == "1"))  //★
                              && ($model->replace_data["det_check"][$r1idx] == "1" || $model->replace_data["det_check"][$l1idx] == "1")) {
                            if ($model->replace_data["det_check"][$r1idx] == "1") {
                                $data2["DET_REMARK5"][TEXT]  = $model->replace_data["det_field"]["R_EAR_DB_1000"];
                            }
                            if ($model->replace_data["det_check"][$l1idx] == "1") {
                                $data2["DET_REMARK6"][TEXT]  = $model->replace_data["det_field"]["L_EAR_DB_1000"];
                            }
                        }
                        if ($cnt009 > 0) {
                            $where  = " WHERE ";
                            $where .= "     YEAR            = '{$model->year}' ";
                            $where .= "     AND SCHREGNO    = '{$schregno}' ";
                            $where .= "     AND DET_SEQ     = '009' ";
                            $db->query(Query::updateSQL($data2, "MEDEXAM_DET_DETAIL_DAT", $where));
                        } else {
                            $data2["YEAR"][TEXT]         = $model->year;
                            $data2["SCHREGNO"][TEXT]     = $schregno;
                            $data2["DET_SEQ"][TEXT]      = "009";
                            $db->query(Query::insertSQL($data2, "MEDEXAM_DET_DETAIL_DAT"));
                        }
                    }

                    //聴力・測定困難
                    if ($model->replace_data["det_check"][$rcmidx] == "1" || $model->replace_data["det_check"][$lcmidx] == "1") {
                        $data2 = array();
                        if ($model->replace_data["det_check"][$rcmidx] == "1") {
                            $data2["DET_REMARK3"][TEXT]  = $model->replace_data["det_field"]["R_EAR_CANTMEASURE"];
                        }
                        if ($model->replace_data["det_check"][$lcmidx] == "1") {
                            $data2["DET_REMARK4"][TEXT]  = $model->replace_data["det_field"]["L_EAR_CANTMEASURE"];
                        }
                        $cnt012 = $db->getOne(knjf010aQuery::getCntDetailDat($model->year, $schregno, '012'));
                        if ($cnt012 > 0) {
                            $where  = " WHERE ";
                            $where .= "     YEAR            = '{$model->year}' ";
                            $where .= "     AND SCHREGNO    = '{$schregno}' ";
                            $where .= "     AND DET_SEQ     = '012' ";
                            $db->query(Query::updateSQL($data2, "MEDEXAM_DET_DETAIL_DAT", $where));
                        } else {
                            $data2["YEAR"][TEXT]         = $model->year;
                            $data2["SCHREGNO"][TEXT]     = $schregno;
                            $data2["DET_SEQ"][TEXT]      = "012";
                            $db->query(Query::insertSQL($data2, "MEDEXAM_DET_DETAIL_DAT"));
                        }
                    }
                    //特別支援学校
                    if ($model->Properties["useSpecial_Support_School"] == "1") {
                        //聴力(装用時)
                        if ($model->replace_data["det_check"][4] == "1" || $model->replace_data["det_check"][5] == "1" ||
                            $model->replace_data["det_check"][6] == "1" || $model->replace_data["det_check"][7] == "1" ||
                            $model->replace_data["det_check"][8] == "1" || $model->replace_data["det_check"][9] == "1") {
                            $data2 = array();
                            if ($model->replace_data["det_check"][4] == "1") {
                                $data2["DET_REMARK2"][TEXT]  = $model->replace_data["det_field"]["R_EAR_DB_IN"];
                            }
                            if ($model->replace_data["det_check"][5] == "1") {
                                $data2["DET_REMARK3"][TEXT]  = $model->replace_data["det_field"]["R_EAR_DB_4000_IN"];
                            }
                            if ($model->replace_data["det_check"][6] == "1") {
                                $data2["DET_REMARK1"][TEXT]  = $model->replace_data["det_field"]["R_EAR_IN"];
                            }
                            if ($model->replace_data["det_check"][7] == "1") {
                                $data2["DET_REMARK5"][TEXT]  = $model->replace_data["det_field"]["L_EAR_DB_IN"];
                            }
                            if ($model->replace_data["det_check"][8] == "1") {
                                $data2["DET_REMARK6"][TEXT]  = $model->replace_data["det_field"]["L_EAR_DB_4000_IN"];
                            }
                            if ($model->replace_data["det_check"][9] == "1") {
                                $data2["DET_REMARK4"][TEXT]  = $model->replace_data["det_field"]["L_EAR_IN"];
                            }
                            if ($cnt018 > 0) {
                                $where  = " WHERE ";
                                $where .= "     YEAR            = '{$model->year}' ";
                                $where .= "     AND SCHREGNO    = '{$schregno}' ";
                                $where .= "     AND DET_SEQ     = '018' ";
                                $db->query(Query::updateSQL($data2, "MEDEXAM_DET_DETAIL_DAT", $where));
                            } else {
                                $data2["YEAR"][TEXT]         = $model->year;
                                $data2["SCHREGNO"][TEXT]     = $schregno;
                                $data2["DET_SEQ"][TEXT]      = "018";
                                $db->query(Query::insertSQL($data2, "MEDEXAM_DET_DETAIL_DAT"));
                            }
                        }
                    }
                } else {
                    //聴力・測定困難
                    if ($model->replace_data["det_check"][4] == "1" || $model->replace_data["det_check"][6] == "1") {
                        $data2 = array();
                        if ($model->replace_data["det_check"][4] == "1") {
                            $data2["DET_REMARK3"][TEXT]  = $model->replace_data["det_field"]["R_EAR_CANTMEASURE"];
                        }
                        if ($model->replace_data["det_check"][6] == "1") {
                            $data2["DET_REMARK4"][TEXT]  = $model->replace_data["det_field"]["L_EAR_CANTMEASURE"];
                        }
                        $cnt012 = $db->getOne(knjf010aQuery::getCntDetailDat($model->year, $schregno, '012'));
                        if ($cnt012 > 0) {
                            $where  = " WHERE ";
                            $where .= "     YEAR            = '{$model->year}' ";
                            $where .= "     AND SCHREGNO    = '{$schregno}' ";
                            $where .= "     AND DET_SEQ     = '012' ";
                            $db->query(Query::updateSQL($data2, "MEDEXAM_DET_DETAIL_DAT", $where));
                        } else {
                            $data2["YEAR"][TEXT]         = $model->year;
                            $data2["SCHREGNO"][TEXT]     = $schregno;
                            $data2["DET_SEQ"][TEXT]      = "012";
                            $db->query(Query::insertSQL($data2, "MEDEXAM_DET_DETAIL_DAT"));
                        }
                    }
                    //特別支援学校
                    if ($model->Properties["useSpecial_Support_School"] == "1") {
                        //聴力(装用時)
                        if ($model->replace_data["det_check"][4] == "1" || $model->replace_data["det_check"][5] == "1" ||
                            $model->replace_data["det_check"][6] == "1" || $model->replace_data["det_check"][7] == "1") {
                            $data2 = array();
                            if ($model->replace_data["det_check"][4] == "1") {
                                $data2["DET_REMARK2"][TEXT]  = $model->replace_data["det_field"]["R_EAR_DB_IN"];
                            }
                            if ($model->replace_data["det_check"][5] == "1") {
                                $data2["DET_REMARK1"][TEXT]  = $model->replace_data["det_field"]["R_EAR_IN"];
                            }
                            if ($model->replace_data["det_check"][6] == "1") {
                                $data2["DET_REMARK5"][TEXT]  = $model->replace_data["det_field"]["L_EAR_DB_IN"];
                            }
                            if ($model->replace_data["det_check"][7] == "1") {
                                $data2["DET_REMARK4"][TEXT]  = $model->replace_data["det_field"]["L_EAR_IN"];
                            }
                            if ($cnt018 > 0) {
                                $where  = " WHERE ";
                                $where .= "     YEAR            = '{$model->year}' ";
                                $where .= "     AND SCHREGNO    = '{$schregno}' ";
                                $where .= "     AND DET_SEQ     = '018' ";
                                $db->query(Query::updateSQL($data2, "MEDEXAM_DET_DETAIL_DAT", $where));
                            } else {
                                $data2["YEAR"][TEXT]         = $model->year;
                                $data2["SCHREGNO"][TEXT]     = $schregno;
                                $data2["DET_SEQ"][TEXT]      = "018";
                                $db->query(Query::insertSQL($data2, "MEDEXAM_DET_DETAIL_DAT"));
                            }
                        }
                    }
                }
            }

            //熊本の帳票の時、更新時に初期値をセットする
            if ($model->Properties["printKenkouSindanIppan"] == "2") {
                //聴力・右状態コンボ
                $getLR_EAR_Shokichi = $db->getRow(knjf010aQuery::getLREar($model, "1"), DB_FETCHMODE_ASSOC);
            }

            foreach ($model->replace_data["det_field"] as $key => $val) {
                if (in_array($key, array("R_EAR_DB_1000", "L_EAR_DB_1000","R_EAR_DB_4000", "L_EAR_DB_4000",
                                         "R_VISION_CANTMEASURE", "L_VISION_CANTMEASURE",
                                         "R_EAR_CANTMEASURE", "L_EAR_CANTMEASURE",
                                         "R_EAR_DB_IN", "R_EAR_DB_4000_IN", "R_EAR_IN",
                                         "L_EAR_DB_IN", "L_EAR_DB_4000_IN", "L_EAR_IN"
                                         ))) {
                    continue;
                }
                if ($key == "R_EAR_DB" || $key == "L_EAR_DB") {
                    $data[$key][NUMBER] = $val;
                } elseif ($model->Properties["printKenkouSindanIppan"] == "2" &&
                    ($key == "R_EAR" || $key == "L_EAR")
                    ) {
                    $data[$key][TEXT] = $val ? $val : $getLR_EAR_Shokichi["NAMECD2"];
                } else {
                    $data[$key][TEXT] = $val;
                }
            }
        } elseif ($model->cmd == "replace_update2") {  //一括更新2
            if ($model->Properties["printKenkouSindanIppan"] == "1") {
                if ($model->replace_data["det_check"][0] == "1" || ($model->isKoma && $model->replace_data["det_check"][11] == "1")) {
                    $query = "SELECT COUNT(*) AS CNT FROM MEDEXAM_DET_DETAIL_DAT WHERE YEAR = '{$model->year}' AND SCHREGNO = '{$schregno}' AND DET_SEQ = '009'";
                    $cnt = $db->getOne($query);

                    $data2 = array();
                    if ($cnt > 0) {
                        if ($model->replace_data["det_check"][0] == "1") {
                            $data2["DET_REMARK1"][TEXT]  = $model->replace_data["det_field"]["NUTRITIONCD_REMARK"];
                        }
                        if ($model->isKoma && $model->replace_data["det_check"][11] == "1") {
                            $data2["DET_REMARK7"][TEXT]  = $model->replace_data["det_field"]["HEART_GRAPH_NO"];
                        }
                        $where  = " WHERE ";
                        $where .= "     YEAR = '{$model->year}' ";
                        $where .= "     AND SCHREGNO = '{$schregno}' ";
                        $where .= "     AND DET_SEQ = '009' ";
                        $db->query(Query::updateSQL($data2, "MEDEXAM_DET_DETAIL_DAT", $where));
                    } else {
                        $data2["YEAR"][TEXT]         = $model->year;
                        $data2["SCHREGNO"][TEXT]     = $schregno;
                        $data2["DET_SEQ"][TEXT]      = "009";
                        if ($model->replace_data["det_check"][0] == "1") {
                            $data2["DET_REMARK1"][TEXT]  = $model->replace_data["det_field"]["NUTRITIONCD_REMARK"];
                        }
                        if ($model->isKoma && $model->replace_data["det_check"][11] == "1") {
                            $data2["DET_REMARK7"][TEXT]  = $model->replace_data["det_field"]["HEART_GRAPH_NO"];
                        }
                        $db->query(Query::insertSQL($data2, "MEDEXAM_DET_DETAIL_DAT"));
                    }
                }
                if ($model->replace_data["det_check"][4] == "1") {
                    $query = "SELECT COUNT(*) AS CNT FROM MEDEXAM_DET_DETAIL_DAT WHERE YEAR = '{$model->year}' AND SCHREGNO = '{$schregno}' AND DET_SEQ = '006'";
                    $cnt = $db->getOne($query);

                    $data2 = array();
                    if ($cnt > 0) {
                        $data2["DET_REMARK1"][TEXT]  = $model->replace_data["det_field"]["SKINDISEASECD_REMARK"];
                        $where  = " WHERE ";
                        $where .= "     YEAR = '{$model->year}' ";
                        $where .= "     AND SCHREGNO = '{$schregno}' ";
                        $where .= "     AND DET_SEQ = '006' ";
                        $db->query(Query::updateSQL($data2, "MEDEXAM_DET_DETAIL_DAT", $where));
                    } else {
                        $data2["YEAR"][TEXT]         = $model->year;
                        $data2["SCHREGNO"][TEXT]     = $schregno;
                        $data2["DET_SEQ"][TEXT]      = "006";
                        $data2["DET_REMARK1"][TEXT]  = $model->replace_data["det_field"]["SKINDISEASECD_REMARK"];
                        $db->query(Query::insertSQL($data2, "MEDEXAM_DET_DETAIL_DAT"));
                    }
                }
                if ($model->replace_data["det_check"][9] == "1") {
                    $query = "SELECT COUNT(*) AS CNT FROM MEDEXAM_DET_DETAIL_DAT WHERE YEAR = '{$model->year}' AND SCHREGNO = '{$schregno}' AND DET_SEQ = '005'";
                    $cnt = $db->getOne($query);

                    $data2 = array();
                    if ($cnt > 0) {
                        $data2["DET_REMARK2"][TEXT]  = $model->replace_data["det_field"]["TB_NAME_REMARK1"];
                        $where  = " WHERE ";
                        $where .= "     YEAR = '{$model->year}' ";
                        $where .= "     AND SCHREGNO = '{$schregno}' ";
                        $where .= "     AND DET_SEQ = '005' ";
                        $db->query(Query::updateSQL($data2, "MEDEXAM_DET_DETAIL_DAT", $where));
                    } else {
                        $data2["YEAR"][TEXT]         = $model->year;
                        $data2["SCHREGNO"][TEXT]     = $schregno;
                        $data2["DET_SEQ"][TEXT]      = "005";
                        $data2["DET_REMARK2"][TEXT]  = $model->replace_data["det_field"]["TB_NAME_REMARK1"];
                        $db->query(Query::insertSQL($data2, "MEDEXAM_DET_DETAIL_DAT"));
                    }
                }
            }
            if ($model->Properties["printKenkouSindanIppan"] == "2") {
                if ($model->replace_data["det_check"][2] == "1") {
                    $query = "SELECT COUNT(*) AS CNT FROM MEDEXAM_DET_DETAIL_DAT WHERE YEAR = '{$model->year}' AND SCHREGNO = '{$schregno}' AND DET_SEQ = '002'";
                    $cnt = $db->getOne($query);
                    $data2 = array();
                    if ($cnt > 0) {
                        $data2["DET_REMARK2"][TEXT]  = $model->replace_data["det_field"]["EYE_TEST_RESULT2"];
                        $data2["DET_REMARK3"][TEXT]  = $model->replace_data["det_field"]["EYE_TEST_RESULT3"];
                        $data2["DET_REMARK4"][TEXT]  = $model->replace_data["det_field"]["EYEDISEASECD2"];
                        $data2["DET_REMARK5"][TEXT]  = $model->replace_data["det_field"]["EYEDISEASECD3"];
                        $data2["DET_REMARK6"][TEXT]  = $model->replace_data["det_field"]["EYEDISEASECD4"];
                        $where  = " WHERE ";
                        $where .= "     YEAR = '{$model->year}' ";
                        $where .= "     AND SCHREGNO = '{$schregno}' ";
                        $where .= "     AND DET_SEQ = '002' ";
                        $db->query(Query::updateSQL($data2, "MEDEXAM_DET_DETAIL_DAT", $where));
                    } else {
                        $data2["YEAR"][TEXT]         = $model->year;
                        $data2["SCHREGNO"][TEXT]     = $schregno;
                        $data2["DET_SEQ"][TEXT]      = "002";
                        $data2["DET_REMARK2"][TEXT]  = $model->replace_data["det_field"]["EYE_TEST_RESULT2"];
                        $data2["DET_REMARK3"][TEXT]  = $model->replace_data["det_field"]["EYE_TEST_RESULT3"];
                        $data2["DET_REMARK4"][TEXT]  = $model->replace_data["det_field"]["EYEDISEASECD2"];
                        $data2["DET_REMARK5"][TEXT]  = $model->replace_data["det_field"]["EYEDISEASECD3"];
                        $data2["DET_REMARK6"][TEXT]  = $model->replace_data["det_field"]["EYEDISEASECD4"];
                        $db->query(Query::insertSQL($data2, "MEDEXAM_DET_DETAIL_DAT"));
                    }
                }
                if ($model->replace_data["det_check"][3] == "1") {
                    $query = "SELECT COUNT(*) AS CNT FROM MEDEXAM_DET_DETAIL_DAT WHERE YEAR = '{$model->year}' AND SCHREGNO = '{$schregno}' AND DET_SEQ = '003'";
                    $cnt = $db->getOne($query);

                    $data2 = array();
                    if ($cnt > 0) {
                        $data2["DET_REMARK2"][TEXT]  = $model->replace_data["det_field"]["NOSEDISEASECD_REMARK2"];
                        $data2["DET_REMARK3"][TEXT]  = $model->replace_data["det_field"]["NOSEDISEASECD_REMARK3"];
                        $data2["DET_REMARK4"][TEXT]  = $model->replace_data["det_field"]["NOSEDISEASECD2"];
                        $data2["DET_REMARK5"][TEXT]  = $model->replace_data["det_field"]["NOSEDISEASECD3"];
                        $data2["DET_REMARK6"][TEXT]  = $model->replace_data["det_field"]["NOSEDISEASECD4"];
                        $where  = " WHERE ";
                        $where .= "     YEAR = '{$model->year}' ";
                        $where .= "     AND SCHREGNO = '{$schregno}' ";
                        $where .= "     AND DET_SEQ = '003' ";
                        $db->query(Query::updateSQL($data2, "MEDEXAM_DET_DETAIL_DAT", $where));
                    } else {
                        $data2["YEAR"][TEXT]         = $model->year;
                        $data2["SCHREGNO"][TEXT]     = $schregno;
                        $data2["DET_SEQ"][TEXT]      = "003";
                        $data2["DET_REMARK2"][TEXT]  = $model->replace_data["det_field"]["NOSEDISEASECD_REMARK2"];
                        $data2["DET_REMARK3"][TEXT]  = $model->replace_data["det_field"]["NOSEDISEASECD_REMARK3"];
                        $data2["DET_REMARK4"][TEXT]  = $model->replace_data["det_field"]["NOSEDISEASECD2"];
                        $data2["DET_REMARK5"][TEXT]  = $model->replace_data["det_field"]["NOSEDISEASECD3"];
                        $data2["DET_REMARK6"][TEXT]  = $model->replace_data["det_field"]["NOSEDISEASECD4"];
                        $db->query(Query::insertSQL($data2, "MEDEXAM_DET_DETAIL_DAT"));
                    }
                }
                
                if ($model->replace_data["det_check"][4] == "1") {
                    $query = "SELECT COUNT(*) AS CNT FROM MEDEXAM_DET_DETAIL_DAT WHERE YEAR = '{$model->year}' AND SCHREGNO = '{$schregno}' AND DET_SEQ = '006'";
                    $cnt = $db->getOne($query);

                    $data2 = array();
                    if ($cnt > 0) {
                        $data2["DET_REMARK1"][TEXT]  = $model->replace_data["det_field"]["SKINDISEASECD_REMARK"];
                        $where  = " WHERE ";
                        $where .= "     YEAR = '{$model->year}' ";
                        $where .= "     AND SCHREGNO = '{$schregno}' ";
                        $where .= "     AND DET_SEQ = '006' ";
                        $db->query(Query::updateSQL($data2, "MEDEXAM_DET_DETAIL_DAT", $where));
                    } else {
                        $data2["YEAR"][TEXT]         = $model->year;
                        $data2["SCHREGNO"][TEXT]     = $schregno;
                        $data2["DET_SEQ"][TEXT]      = "006";
                        $data2["DET_REMARK1"][TEXT]  = $model->replace_data["det_field"]["SKINDISEASECD_REMARK"];
                        $db->query(Query::insertSQL($data2, "MEDEXAM_DET_DETAIL_DAT"));
                    }
                }
                
                if ($model->replace_data["det_check"][8] == "1" ||
                    $model->replace_data["det_check"][9] == "1" ||
                    $model->replace_data["det_check"][10] == "1"
                ) {
                    $query = "SELECT COUNT(*) AS CNT FROM MEDEXAM_DET_DETAIL_DAT WHERE YEAR = '{$model->year}' AND SCHREGNO = '{$schregno}' AND DET_SEQ = '005'";
                    $cnt = $db->getOne($query);

                    $data2 = array();
                    if ($model->replace_data["det_check"][8] == "1") {
                        $data2["DET_REMARK1"][TEXT]  = $model->replace_data["det_field"]["TB_OTHERTEST_REMARK1"];
                    }
                    if ($model->replace_data["det_check"][9] == "1") {
                        $data2["DET_REMARK2"][TEXT]  = $model->replace_data["det_field"]["TB_NAME_REMARK1"];
                    }
                    if ($model->replace_data["det_check"][10] == "1") {
                        $data2["DET_REMARK3"][TEXT]  = $model->replace_data["det_field"]["TB_ADVISE_REMARK1"];
                    }
                    if ($cnt > 0) {
                        $where  = " WHERE ";
                        $where .= "     YEAR = '{$model->year}' ";
                        $where .= "     AND SCHREGNO = '{$schregno}' ";
                        $where .= "     AND DET_SEQ = '005' ";
                        $db->query(Query::updateSQL($data2, "MEDEXAM_DET_DETAIL_DAT", $where));
                    } else {
                        $data2["YEAR"][TEXT]         = $model->year;
                        $data2["SCHREGNO"][TEXT]     = $schregno;
                        $data2["DET_SEQ"][TEXT]      = "005";
                        $db->query(Query::insertSQL($data2, "MEDEXAM_DET_DETAIL_DAT"));
                    }
                }
            }
            //熊本の帳票の時、更新時に初期値をセットする
            if ($model->Properties["printKenkouSindanIppan"] == "2") {
                //栄養状態コンボ
                $getNutrition_Shokichi = $db->getRow(knjf010aQuery::getNutrition($model, "1"), DB_FETCHMODE_ASSOC);
                //脊柱・胸部コンボ
                $getSpinerib_Shokichi = $db->getRow(knjf010aQuery::getSpinerib($model, "1"), DB_FETCHMODE_ASSOC);
                //目の疾病及び異常コンボ
                $getEyedisease_Shokichi = $db->getRow(knjf010aQuery::getEyedisease($model, "1"), DB_FETCHMODE_ASSOC);
                //耳鼻咽頭疾患コンボ
                $getNosedisease_Shokichi = $db->getRow(knjf010aQuery::getNosedisease($model, "1"), DB_FETCHMODE_ASSOC);
                //皮膚疾患コンボ
                $getSkindisease_Shokichi = $db->getRow(knjf010aQuery::getSkindisease($model, "1"), DB_FETCHMODE_ASSOC);
                //結核・所見コンボ
                $getTb_remark_Shokichi = $db->getRow(knjf010aQuery::getTbRemark($model, "1"), DB_FETCHMODE_ASSOC);
                //結核・その他検査コンボ
                $getTb_othertest_Shokichi = $db->getRow(knjf010aQuery::getTbOthertest($model, "1"), DB_FETCHMODE_ASSOC);
                //結核・病名コンボ
                $getTb_Name_Shokichi = $db->getRow(knjf010aQuery::getTbName($model, "1"), DB_FETCHMODE_ASSOC);
                //心臓・臨床医学的検査コンボ
                $getHeart_medexam_Shokichi = $db->getRow(knjf010aQuery::getHeartMedexam($model, "1"), DB_FETCHMODE_ASSOC);
                //心臓・疾病及び異常コンボ
                $getHeartdisease_Shokichi = $db->getRow(knjf010aQuery::getHeartdisease($model, "1"), DB_FETCHMODE_ASSOC);
            } elseif ($model->Properties["printKenkouSindanIppan"] == "1" && ($model->z010name1 == "KINDAI" || $model->z010name1 == "KINJUNIOR")) {
                //心臓・臨床医学的検査コンボ
                $getHeart_medexam_Shokichi = $db->getRow(knjf010aQuery::getHeartMedexam($model, "1"), DB_FETCHMODE_ASSOC);
            }
            if ($model->Properties["printKenkouSindanIppan"] == "3") {
                if ($model->replace_data["det_check"][9] == "1") {
                    $query = "SELECT COUNT(*) AS CNT FROM MEDEXAM_DET_DETAIL_DAT WHERE YEAR = '{$model->year}' AND SCHREGNO = '{$schregno}' AND DET_SEQ = '005'";
                    $cnt = $db->getOne($query);

                    $data2 = array();
                    if ($cnt > 0) {
                        $data2["DET_REMARK2"][TEXT]  = $model->replace_data["det_field"]["TB_NAME_REMARK1"];
                        $where  = " WHERE ";
                        $where .= "     YEAR = '{$model->year}' ";
                        $where .= "     AND SCHREGNO = '{$schregno}' ";
                        $where .= "     AND DET_SEQ = '005' ";
                        $db->query(Query::updateSQL($data2, "MEDEXAM_DET_DETAIL_DAT", $where));
                    } else {
                        $data2["YEAR"][TEXT]         = $model->year;
                        $data2["SCHREGNO"][TEXT]     = $schregno;
                        $data2["DET_SEQ"][TEXT]      = "005";
                        $data2["DET_REMARK2"][TEXT]  = $model->replace_data["det_field"]["TB_NAME_REMARK1"];
                        $db->query(Query::insertSQL($data2, "MEDEXAM_DET_DETAIL_DAT"));
                    }
                }
            }
            foreach ($model->replace_data["det_field"] as $key => $val) {
                if (
                    (
                        $key == "EYE_TEST_RESULT2" || $key == "EYE_TEST_RESULT3" ||
                     $key == "EYEDISEASECD2" || $key == "EYEDISEASECD3" || $key == "EYEDISEASECD4" ||
                     $key == "NOSEDISEASECD_REMARK2" || $key == "NOSEDISEASECD_REMARK3" ||
                     $key == "NOSEDISEASECD2" || $key == "NOSEDISEASECD3" || $key == "NOSEDISEASECD4" ||
                     $key == "TB_OTHERTEST_REMARK1" || $key == "TB_NAME_REMARK1" || $key == "TB_ADVISE_REMARK1" ||
                     $key == "SKINDISEASECD_REMARK" || $key == "NUTRITIONCD_REMARK" || $key == "HEART_GRAPH_NO"
                    )
                    ) {
                    continue;
                } elseif ($model->Properties["printKenkouSindanIppan"] == "2" && ($key == "NUTRITIONCD")) {
                    $data[$key][TEXT] = $val ? $val : $getNutrition_Shokichi["NAMECD2"];
                } elseif ($model->Properties["printKenkouSindanIppan"] == "2" && ($key == "SPINERIBCD")) {
                    $data[$key][TEXT] = $val ? $val : $getSpinerib_Shokichi["NAMECD2"];
                } elseif ($model->Properties["printKenkouSindanIppan"] == "2" && ($key == "EYEDISEASECD")) {
                    $data[$key][TEXT] = $val ? $val : $getEyedisease_Shokichi["NAMECD2"];
                } elseif ($model->Properties["printKenkouSindanIppan"] == "2" && ($key == "NOSEDISEASECD")) {
                    $data[$key][TEXT] = $val ? $val : $getNosedisease_Shokichi["NAMECD2"];
                } elseif ($model->Properties["printKenkouSindanIppan"] == "2" && ($key == "SKINDISEASECD")) {
                    $data[$key][TEXT] = $val ? $val : $getSkindisease_Shokichi["NAMECD2"];
                } elseif ($model->Properties["printKenkouSindanIppan"] == "2" && ($key == "TB_REMARKCD")) {
                    $data[$key][TEXT] = $val ? $val : $getTb_remark_Shokichi["NAMECD2"];
                } elseif ($model->Properties["printKenkouSindanIppan"] == "2" && ($key == "TB_OTHERTESTCD")) {
                    $data[$key][TEXT] = $val ? $val : $getTb_othertest_Shokichi["NAMECD2"];
                } elseif ($model->Properties["printKenkouSindanIppan"] == "2" && ($key == "TB_NAMECD")) {
                    $data[$key][TEXT] = $val ? $val : $getTb_Name_Shokichi["NAMECD2"];
                } elseif ($model->Properties["printKenkouSindanIppan"] == "2" && ($key == "HEART_MEDEXAM")) {
                    $data[$key][TEXT] = $val ? $val : $getHeart_medexam_Shokichi["NAMECD2"];
                } elseif ($model->Properties["printKenkouSindanIppan"] == "2" && ($key == "HEARTDISEASECD")) {
                    $data[$key][TEXT] = $val ? $val : $getHeartdisease_Shokichi["NAMECD2"];
                } elseif ($model->Properties["printKenkouSindanIppan"] == "1" && ($model->z010name1 == "KINDAI" || $model->z010name1 == "KINJUNIOR") && ($key == "HEART_MEDEXAM")) {
                    $data[$key][TEXT] = $val ? $val : $getHeart_medexam_Shokichi["NAMECD2"];
                } else {
                    $data[$key][TEXT] = $val;
                }
            }
        } elseif ($model->cmd == "replace_update3") {  //一括更新3
            if ($model->Properties["printKenkouSindanIppan"] != "2") {
                if ($model->replace_data["det_check"][6]) {
                    $query = "SELECT COUNT(*) AS CNT FROM MEDEXAM_DET_DETAIL_DAT WHERE YEAR = '{$model->year}' AND SCHREGNO = '{$schregno}' AND DET_SEQ = '001'";
                    $cnt = $db->getOne($query);

                    $data2 = array();
                    if ($cnt > 0) {
                        $data2["DET_REMARK2"][TEXT]  = $model->replace_data["det_field"]["OTHER_REMARK2"];
                        $where  = " WHERE ";
                        $where .= "     YEAR = '{$model->year}' ";
                        $where .= "     AND SCHREGNO = '{$schregno}' ";
                        $where .= "     AND DET_SEQ = '001' ";
                        $db->query(Query::updateSQL($data2, "MEDEXAM_DET_DETAIL_DAT", $where));
                    } else {
                        $data2["YEAR"][TEXT]         = $model->year;
                        $data2["SCHREGNO"][TEXT]     = $schregno;
                        $data2["DET_SEQ"][TEXT]      = "001";
                        $data2["DET_REMARK2"][TEXT]  = $model->replace_data["det_field"]["OTHER_REMARK2"];
                        $db->query(Query::insertSQL($data2, "MEDEXAM_DET_DETAIL_DAT"));
                    }
                }
                if ($model->isMie && $model->replace_data["det_check"][15]) {
                    //三重の場合、$model->Properties["printKenkouSindanIppan"] = "1"。
                    //個別登録画面はdelete->insertだが、他の人の情報も一括更新なので、登録データがあればupdate、無ければinsertとする。
                    $query = " SELECT COUNT(*) FROM MEDEXAM_DET_DETAIL_DAT WHERE YEAR = '{$model->year}' AND SCHREGNO = '{$schregno}' AND DET_SEQ = '013' ";
                    $dcnt = $db->getOne($query);
                    $data2 = array();
                    $data2["DET_REMARK1"][TEXT]  = $model->replace_data["det_field"]["DOC_NAME"];
                    $data2["REGISTERCD"][TEXT]   = STAFFCD;
                    $data2["UPDATED"][FUNC]      = "sysdate()";
                    if ($dcnt == 0) {
                        $data2["YEAR"][TEXT]     = $model->year;
                        $data2["SCHREGNO"][TEXT] = $schregno;
                        $data2["DET_SEQ"][TEXT]  = "013";
                        $query = Query::insertSQL($data2, "MEDEXAM_DET_DETAIL_DAT");
                        $db->query($query);
                    } else {
                        $where = " WHERE YEAR = '{$model->year}' AND SCHREGNO = '{$schregno}' AND DET_SEQ = '013' ";
                        $query = Query::updateSQL($data2, "MEDEXAM_DET_DETAIL_DAT", $where);
                        $db->query($query);
                    }
                }
                if ($this->Properties["printKenkouSindanIppan"] == "1" && $this->Properties["KenkouSindan_Ippan_Pattern"] == "1") {
                    $where = " WHERE YEAR = '{$model->year}' AND SCHREGNO = '{$schregno}' AND DET_SEQ = '017' ";
                    $query = " SELECT COUNT(*) FROM MEDEXAM_DET_DETAIL_DAT ".$where;
                    $dcnt = $db->getOne($query);
                    $data2 = array();
                    if ($model->replace_data["det_check"][18]) {
                        $data2["DET_REMARK1"][TEXT]  = $model->replace_data["det_field"]["URICPH1"];
                    }
                    if ($model->replace_data["det_check"][19]) {
                        $data2["DET_REMARK2"][TEXT]  = $model->replace_data["det_field"]["URICPH2"];
                    }
                    $data2["REGISTERCD"][TEXT]   = STAFFCD;
                    $data2["UPDATED"][FUNC]      = "sysdate()";
                    if ($dcnt == 0) {
                        $data2["YEAR"][TEXT]     = $model->year;
                        $data2["SCHREGNO"][TEXT] = $schregno;
                        $data2["DET_SEQ"][TEXT]  = "017";
                        $query = Query::insertSQL($data2, "MEDEXAM_DET_DETAIL_DAT");
                        $db->query($query);
                    } else {
                        $query = Query::updateSQL($data2, "MEDEXAM_DET_DETAIL_DAT", $where);
                        $db->query($query);
                    }
                }
            }
            if ($model->Properties["printKenkouSindanIppan"] == "2") {
                if ($model->replace_data["det_check"][7]) {
                    $query = "SELECT COUNT(*) AS CNT FROM MEDEXAM_DET_DETAIL_DAT WHERE YEAR = '{$model->year}' AND SCHREGNO = '{$schregno}' AND DET_SEQ = '001'";
                    $cnt = $db->getOne($query);

                    $data2 = array();
                    if ($cnt > 0) {
                        $data2["DET_REMARK2"][TEXT]  = $model->replace_data["det_field"]["OTHER_REMARK2"];
                        $data2["DET_REMARK3"][TEXT]  = $model->replace_data["det_field"]["OTHER_REMARK3"];
                        $where  = " WHERE ";
                        $where .= "     YEAR = '{$model->year}' ";
                        $where .= "     AND SCHREGNO = '{$schregno}' ";
                        $where .= "     AND DET_SEQ = '001' ";
                        $db->query(Query::updateSQL($data2, "MEDEXAM_DET_DETAIL_DAT", $where));
                    } else {
                        $data2["YEAR"][TEXT]         = $model->year;
                        $data2["SCHREGNO"][TEXT]     = $schregno;
                        $data2["DET_SEQ"][TEXT]      = "001";
                        $data2["DET_REMARK2"][TEXT]  = $model->replace_data["det_field"]["OTHER_REMARK2"];
                        $data2["DET_REMARK3"][TEXT]  = $model->replace_data["det_field"]["OTHER_REMARK3"];
                        $db->query(Query::insertSQL($data2, "MEDEXAM_DET_DETAIL_DAT"));
                    }
                }
                //尿・その他の検査
                if ($model->replace_data["det_check"][6]) {
                    $query = "SELECT COUNT(*) AS CNT FROM MEDEXAM_DET_DETAIL_DAT WHERE YEAR = '{$model->year}' AND SCHREGNO = '{$schregno}' AND DET_SEQ = '007'";
                    $cnt = $db->getOne($query);

                    $data2 = array();
                    if ($cnt > 0) {
                        $data2["DET_REMARK1"][TEXT]  = $model->replace_data["det_field"]["URICOTHERTESTCD"];
                        $where  = " WHERE ";
                        $where .= "     YEAR = '{$model->year}' ";
                        $where .= "     AND SCHREGNO = '{$schregno}' ";
                        $where .= "     AND DET_SEQ = '007' ";
                        $db->query(Query::updateSQL($data2, "MEDEXAM_DET_DETAIL_DAT", $where));
                    } else {
                        $data2["YEAR"][TEXT]         = $model->year;
                        $data2["SCHREGNO"][TEXT]     = $schregno;
                        $data2["DET_SEQ"][TEXT]      = "007";
                        $data2["DET_REMARK1"][TEXT]  = $model->replace_data["det_field"]["URICOTHERTESTCD"];
                        $db->query(Query::insertSQL($data2, "MEDEXAM_DET_DETAIL_DAT"));
                    }
                }
            }
            //熊本の帳票の時、更新時に初期値をセットする
            if ($model->Properties["printKenkouSindanIppan"] == "2") {
                //その他疾病及び異常コンボ
                $getOther_disease_Shokichi = $db->getRow(knjf010aQuery::getOtherDisease($model, "1"), DB_FETCHMODE_ASSOC);
                //尿
                $getUric_Shokichi_F020 = $db->getRow(knjf010aQuery::getUric($model, "F020", "1"), DB_FETCHMODE_ASSOC);
                $getUric_Shokichi_F019 = $db->getRow(knjf010aQuery::getUric($model, "F019", "1"), DB_FETCHMODE_ASSOC);
                $getUric_Shokichi_F018 = $db->getRow(knjf010aQuery::getUric($model, "F018", "1"), DB_FETCHMODE_ASSOC);
                $chkUric_Taishogai_F020 = $db->getOne(knjf010aQuery::getUric($model, "F020", "2"));
                $chkUric_Taishogai_F019 = $db->getOne(knjf010aQuery::getUric($model, "F019", "2"));
                $chkUric_Taishogai_F018 = $db->getOne(knjf010aQuery::getUric($model, "F018", "2"));
            }
            foreach ($model->replace_data["det_field"] as $key => $val) {
                if (
                    ($key == "OTHER_REMARK2" || $key == "OTHER_REMARK3" || $key == "URICOTHERTESTCD")
                    ) {
                    continue;
                } elseif ($model->Properties["printKenkouSindanIppan"] == "2" &&
                    ($key == "OTHERDISEASECD")
                    ) {
                    $data[$key][TEXT] = $val ? $val : $getOther_disease_Shokichi["NAMECD2"];
                } elseif ($model->Properties["printKenkouSindanIppan"] == "2" &&
                    ($key == "ALBUMINURIA1CD" || $key == "URICSUGAR1CD" || $key == "URICBLEED1CD" || $key == "ALBUMINURIA2CD" || $key == "URICSUGAR2CD" || $key == "URICBLEED2CD")
                    ) {
                    if ($key == "ALBUMINURIA1CD" || $key == "ALBUMINURIA2CD") {
                        $getUric_Shokichi["NAMECD2"] = $getUric_Shokichi_F020["NAMECD2"];
                        $chkUric_Taishogai = $chkUric_Taishogai_F020;
                    }
                    if ($key == "URICSUGAR1CD" || $key == "URICSUGAR2CD") {
                        $getUric_Shokichi["NAMECD2"] = $getUric_Shokichi_F019["NAMECD2"];
                        $chkUric_Taishogai = $chkUric_Taishogai_F019;
                    }
                    if ($key == "URICBLEED1CD" || $key == "URICBLEED2CD") {
                        $getUric_Shokichi["NAMECD2"] = $getUric_Shokichi_F018["NAMECD2"];
                        $chkUric_Taishogai = $chkUric_Taishogai_F018;
                    }
                    if (($key == "ALBUMINURIA2CD" || $key == "URICSUGAR2CD" || $key == "URICBLEED2CD") && $chkUric_Taishogai && $val == "") {
                        $data[$key][TEXT] = $chkUric_Taishogai;
                    } else {
                        $data[$key][TEXT] = $val ? $val : $getUric_Shokichi["NAMECD2"];
                    }
                } elseif ($model->Properties["printKenkouSindanIppan"] == "1" && ($model->z010name1 == "KINDAI" || $model->z010name1 == "KINJUNIOR") && $key == "URI_ADVISECD") {
                    $getHeart_uriAdviseCd_Shokichi = $db->getRow(knjf010aQuery::getUriAdvisecd($model, "1"), DB_FETCHMODE_ASSOC);
                    $data[$key][TEXT] = $val ? $val : $getHeart_uriAdviseCd_Shokichi["VALUE"];
                } elseif ((!($model->school_kind == "P" && $model->Properties["useParasite_P"] == "1") && !($model->school_kind == "J" && $model->Properties["useParasite_J"] == "1") && !($model->school_kind == "H" && $model->Properties["useParasite_H"] == "1")) &&
                    ($key == "PARASITE")
                    ) {
                    continue;
                } elseif ($key == "HEMOGLOBIN") {
                    $data[$key][NUMBER] = $val;
                } elseif ($key == "DOC_NAME") {
                    //前段階で処理しているので処理しない。
                    continue;
                } elseif ($model->isMie && $key == "DOC_NAME") {
                } else {
                    $data[$key][TEXT] = $val;
                }
            }
        } elseif ($model->cmd == "replace_update4") {  //一括更新4
            foreach ($model->replace_data["det_field"] as $key => $val) {
                $data[$key][TEXT] = $val;
            }
        }
        $data["REGISTERCD"][TEXT] = STAFFCD;
        $data["UPDATED"][FUNC]    = "sysdate()";

        if ($flag == "insert") {
            $data["YEAR"][TEXT]     = $model->year;
            $data["SCHREGNO"][TEXT] = $schregno;
            $query = Query::insertSQL($data, "MEDEXAM_DET_DAT");
        } else {
            $where  ="WHERE SCHREGNO = '".$schregno."' AND ";
            $where .="      YEAR = '".$model->year."'";
            $query = Query::updateSQL($data, "MEDEXAM_DET_DAT", $where);
        }
        return $query;
    }

    public function getVisionMark($model)
    {
        $query .= " SELECT ";
        $query .= "     NAMESPARE1, ";
        $query .= "     NAMECD2, ";
        $query .= "     ABBV3, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' AND ";
        if ($model->z010name1 == "kumamoto") {
            $query .= "     NAMECD1 = 'F011' ";
        } else {
            $query .= "     NAMECD1 = 'F017' ";
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 DESC ";

        return $query;
    }

    //テーブル存在チェック
    public function checkTableExist()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSTABLES ";
        $query .= " WHERE ";
        $query .= "     NAME = 'SETTING_DAT' ";

        return $query;
    }

    //校種取得
    public function getSchoolKind($grade)
    {
        $query  = " SELECT ";
        $query .= "    SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "    SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "        YEAR  = '".CTRL_YEAR."' ";
        $query .= "    AND GRADE = '".$grade."' ";

        return $query;
    }

    //生徒項目名取得
    public function getSchName($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     REMARK1 ";
        if ($model->Properties["useSchool_KindField"] == "1") {
        $query .= "     , SCHOOLCD ";
        }
        $query .= " FROM ";
        $query .= "     SETTING_DAT ";
        $query .= " WHERE ";
        $query .= "     SEQ = '001' ";
        if ($model->Properties["useSchool_KindField"] == "1") {

        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
                $query .= " AND SCHOOL_KIND = '".$model->getSchKind."' ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        } else {
            $query .= " AND SCHOOL_KIND IN (SELECT ";
            $query .= "                         S2.SCHOOL_KIND ";
            $query .= "                     FROM ";
            $query .= "                         SCHREG_REGD_DAT S1, ";
            $query .= "                         SCHREG_REGD_GDAT S2 ";
            $query .= "                     WHERE ";
            $query .= "                         S1.YEAR     = S2.YEAR AND ";
            $query .= "                         S1.YEAR     = '".$model->year."' AND ";
            $query .= "                         S1.SEMESTER = '".$model->semester."' AND ";
            $query .= "                         S1.GRADE    = S2.GRADE AND ";
            $query .= "                         S1.SCHREGNO = '".$model->schregno."' ";
            $query .= "                     ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     SCHOOLCD ";
    }
        return $query;
    }

    //MEDEXAM_DET_NO_DATを削除
    public function deleteMedexamDetNoDat($db, $year, $schregNo)
    {
        $query  = " DELETE FROM ";
        $query .= "     MEDEXAM_DET_NO_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$year."' AND ";
        $query .= "     SCHREGNO    = '".$schregNo."' AND ";
        $query .= "     NO          = '1' ";

        $db->query($query);
        return;
    }

    //MEDEXAM_DET_NO_DAT更新
    public function insertMedexamDetNoDat($db, $year, $schregNo, $field)
    {
        $data = array();

        $data["YEAR"][TEXT]                 = $year;
        $data["SCHREGNO"][TEXT]             = $schregNo;
        $data["NO"][TEXT]                   = "1";
        $data["DATE"][TEXT]                 = $field["DATE"];
        $data["HEIGHT"][NUMBER]             = $field["HEIGHT"];
        $data["WEIGHT"][NUMBER]             = $field["WEIGHT"];
        $data["SITHEIGHT"][NUMBER]          = $field["SITHEIGHT"];
        $data["R_BAREVISION"][TEXT]         = $field["R_BAREVISION"];
        $data["R_BAREVISION_MARK"][TEXT]    = $field["R_BAREVISION_MARK"];
        $data["R_VISION"][TEXT]             = $field["R_VISION"];
        $data["R_VISION_MARK"][TEXT]        = $field["R_VISION_MARK"];
        $data["L_BAREVISION"][TEXT]         = $field["L_BAREVISION"];
        $data["L_BAREVISION_MARK"][TEXT]    = $field["L_BAREVISION_MARK"];
        $data["L_VISION"][TEXT]             = $field["L_VISION"];
        $data["L_VISION_MARK"][TEXT]        = $field["L_VISION_MARK"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $db->query(Query::insertSQL($data, "MEDEXAM_DET_NO_DAT"));
        return;
    }
}
