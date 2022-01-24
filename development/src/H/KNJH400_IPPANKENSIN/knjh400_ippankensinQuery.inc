<?php

require_once('for_php7.php');
class knjh400_ippankensinQuery extends Query
{

    //生徒名取得
    public function getName($model)
    {
        $query .= " SELECT ";
        $query .= "     NAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";

        return $query;
    }
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
    public function getMedexamHdat($model, $year = '')
    {
        $year = ($year == '') ? $model->year : $year;
        $db = Query::dbCheckOut();

        $query  = "SELECT * FROM MEDEXAM_HDAT ";
        $query .= "WHERE SCHREGNO = '".$model->schregno."' AND ";
        $query .= "      YEAR = '".$year."'";

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
    public function getMedexamDetDat($model, $year = '')
    {
        $year = ($year == '') ? $model->year : $year;
        $db = Query::dbCheckOut();

        $query  = " SELECT * FROM V_MEDEXAM_DET_DAT ";
        $query .= " WHERE SCHREGNO = '".$model->schregno."' AND ";
        $query .= "      YEAR = '".$year."'";
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
        $query .= "     REMARK1, ";
        $query .= "     SCHOOLCD ";
        $query .= " FROM ";
        $query .= "     SETTING_DAT ";
        $query .= " WHERE ";
        $query .= "     SEQ = '001' ";
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

        return $query;
    }
}
