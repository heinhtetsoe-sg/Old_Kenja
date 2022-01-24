<?php

require_once('for_php7.php');

class knjb104cQuery extends Query
{

    //学期
    public function getSemester($model, $year)
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    //学年
    public function getGrade($model)
    {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR='{$model->year}' ";
        $query .= " ORDER BY ";
        $query .= "     GRADE ";

        return $query;
    }

    //考査
    public function getKousa($model)
    {
        $query  = " SELECT ";
        $query .= "     TESTKINDCD || TESTITEMCD || SCORE_DIV || ':' || TESTITEMNAME AS LABEL, ";
        $query .= "     TESTKINDCD || TESTITEMCD || SCORE_DIV AS VALUE ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW_SDIV ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND SEMESTER = '{$model->semester}' ";
        $query .= "     AND SCORE_DIV = '01' ";
        $query .= " ORDER BY ";
        $query .= "     TESTKINDCD, TESTITEMCD, SCORE_DIV ";

        return $query;
    }
    //校時
    public function getPeriod($model)
    {
        $query .= " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST T3 ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND NAMECD1 = 'B004' ";

        return $query;
    }

    //登録データ
    public function selectQuery1($model)
    {
        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.EXECUTEDATE, ";
        $query .= "     T2.TESTKINDCD, ";
        $query .= "     T2.TESTITEMCD, ";
        $query .= "     T2.TESTITEMNAME, ";
        $query .= "     T3.NAME1 AS PERIODNAME, ";
        $query .= "     T5.CLASSCD || '-' || T5.SCHOOL_KIND || '-' || T5.CURRICULUM_CD || '-' || T5.SUBCLASSCD || ':' || T5.SUBCLASSNAME AS SUBCLASSNAME, ";
        $query .= "     T4.CHAIRCD || ':' || T4.CHAIRNAME AS CHAIRNAME, ";
        $query .= "     LISTAGG(CAST(T7.GRADE || T7.HR_CLASS || ':' || T7.HR_NAME AS VARCHAR(10000)), '<br>') WITHIN GROUP(ORDER BY T1.EXECUTEDATE) AS HR_NAME, ";
        $query .= "     T8.FACCD, ";
        $query .= "     T4.CHAIRCD, ";
        $query .= "     T1.PERIODCD, ";
        $query .= "     T10.FACILITYNAME, ";
        $query .= "     CASE WHEN T9.YEAR IS NOT NULL THEN '1' ELSE '0' END AS FLAG ";
        $query .= " FROM ";
        $query .= "     SCH_CHR_TEST T1 ";
        $query .= " INNER JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV T2 ";
        $query .= "     ON T1.YEAR = T2.YEAR ";
        $query .= "     AND T1.SEMESTER = T2.SEMESTER ";
        $query .= "     AND T1.TESTKINDCD = T2.TESTKINDCD ";
        $query .= "     AND T1.TESTITEMCD = T2.TESTITEMCD ";
        $query .= "     AND T2.SCORE_DIV = '01' ";
        $query .= " LEFT JOIN V_NAME_MST T3 ";
        $query .= "     ON T1.YEAR = T3.YEAR ";
        $query .= "     AND T3.NAMECD1 = 'B004' ";
        $query .= "     AND T1.PERIODCD = T3.NAMECD2 ";
        $query .= " INNER JOIN CHAIR_DAT T4 ";
        $query .= "     ON T1.YEAR = T4.YEAR ";
        $query .= "     AND T1.SEMESTER = T4.SEMESTER ";
        $query .= "     AND T1.CHAIRCD = T4.CHAIRCD ";
        $query .= " LEFT JOIN SUBCLASS_MST T5 ";
        $query .= "     ON T4.CLASSCD = T5.CLASSCD ";
        $query .= "     AND T4.SCHOOL_KIND = T5.SCHOOL_KIND ";
        $query .= "     AND T4.CURRICULUM_CD = T5.CURRICULUM_CD ";
        $query .= "     AND T4.SUBCLASSCD = T5.SUBCLASSCD ";
        $query .= " LEFT JOIN CHAIR_CLS_DAT AS T6 ";
        $query .= "     ON T1.YEAR = T6.YEAR ";
        $query .= "     AND T1.SEMESTER = T6.SEMESTER ";
        $query .= "     AND T1.CHAIRCD = T6.CHAIRCD ";
        $query .= " LEFT JOIN SCHREG_REGD_HDAT T7 ";
        $query .= "     ON T1.YEAR = T7.YEAR ";
        $query .= "     AND T1.SEMESTER = T7.SEMESTER ";
        $query .= "     AND T6.TRGTGRADE = T7.GRADE ";
        $query .= "     AND T6.TRGTCLASS = T7.HR_CLASS ";
        $query .= " LEFT JOIN CHAIR_TEST_FAC_DAT T8 ";
        $query .= "     ON T1.YEAR = T8.YEAR ";
        $query .= "     AND T1.SEMESTER = T8.SEMESTER ";
        $query .= "     AND T1.CHAIRCD = T8.CHAIRCD ";
        $query .= "     AND T8.FACCD = (SELECT MIN(FACCD) FROM CHAIR_TEST_FAC_DAT WHERE YEAR = T1.YEAR AND SEMESTER = T1.SEMESTER AND CHAIRCD = T1.CHAIRCD) ";
        $query .= " LEFT JOIN CHAIR_TEST_FAC_LAYOUT_HDAT T9 ";
        $query .= "     ON T9.YEAR = T1.YEAR ";
        $query .= "     AND T9.SEMESTER = T1.SEMESTER ";
        $query .= "     AND T9.CHAIRCD = T1.CHAIRCD ";
        $query .= "     AND T9.FACCD = T8.FACCD ";
        $query .= " LEFT JOIN V_FACILITY_MST T10 ";
        $query .= "     ON T10.YEAR = T1.YEAR ";
        $query .= "     AND T10.FACCD = T8.FACCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' ";
        if ($model->semester != '') {
            $query .= "     AND T1.SEMESTER ='{$model->semester}' ";
        }
        if ($model->grade != '') {
            $query .= "     AND T7.GRADE ='{$model->grade}' ";
        }
        if ($model->kousa != '') {
            $query .= "     AND T2.TESTKINDCD ='".substr($model->kousa, 0, 2)."' ";
            $query .= "     AND T2.TESTITEMCD ='".substr($model->kousa, 2, 2)."' ";
            $query .= "     AND T2.SCORE_DIV ='".substr($model->kousa, 4, 2)."' ";
        }
        if ($model->executeDate != '') {
            $query .= "     AND T1.EXECUTEDATE = '".str_replace('/', '-', $model->executeDate)."'";
        }
        if ($model->periodCd != '') {
            $query .= "     AND T1.PERIODCD = '{$model->periodCd}'";
        }
        $query .= " GROUP BY ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.EXECUTEDATE, ";
        $query .= "     T2.TESTKINDCD, ";
        $query .= "     T2.TESTITEMCD, ";
        $query .= "     T2.TESTITEMNAME, ";
        $query .= "     T3.NAME1, ";
        $query .= "     T5.CLASSCD, ";
        $query .= "     T5.SCHOOL_KIND, ";
        $query .= "     T5.CURRICULUM_CD, ";
        $query .= "     T5.SUBCLASSCD, ";
        $query .= "     T5.SUBCLASSNAME, ";
        $query .= "     T4.CHAIRCD, ";
        $query .= "     T4.CHAIRNAME, ";
        $query .= "     T8.FACCD, ";
        $query .= "     T1.PERIODCD, ";
        $query .= "     T9.YEAR, ";
        $query .= "     T10.FACILITYNAME ";
        $query .= " ORDER BY ";
        $query .= "     T1.EXECUTEDATE, ";
        $query .= "     T1.PERIODCD, ";
        $query .= "     T4.CHAIRCD ";

        return $query;
    }
    //登録データ
    public function selectQuery2($db, $model)
    {
        $query .= " SELECT ";
        $query .= "     T4.HR_NAMEABBV || '-' || T1.ATTENDNO AS ATTENDNO, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T3.NAME, ";
        $query .= "     CASE WHEN T5.EXECUTEDATE IS NOT NULL THEN '1' ELSE '0' END AS FLAG ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " LEFT JOIN CHAIR_STD_DAT T2 ";
        $query .= "     ON T2.YEAR = T1.YEAR ";
        $query .= "     AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "     AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= " LEFT JOIN SCHREG_BASE_MST T3 ";
        $query .= "     ON T3.SCHREGNO = T1.SCHREGNO ";
        $query .= " LEFT JOIN SCHREG_REGD_HDAT T4 ";
        $query .= "     ON T4.YEAR = T1.YEAR ";
        $query .= "     AND T4.SEMESTER = T1.SEMESTER ";
        $query .= "     AND T4.GRADE = T1.GRADE ";
        $query .= "     AND T4.HR_CLASS = T1.HR_CLASS ";
        $query .= " LEFT JOIN CHAIR_TEST_FAC_LAYOUT_SCHREG_DAT T5 ";
        $query .= "     ON T5.CHAIRCD = T2.CHAIRCD ";
        $query .= "     AND T5.SCHREGNO = T1.SCHREGNO ";
        if ($model->exp_executedate != '') {
            $query .= "     AND T5.EXECUTEDATE = '{$model->exp_executedate}' ";
        }
        if ($model->exp_periodcd != '') {
            $query .= "     AND T5.PERIODCD = '{$model->exp_periodcd}' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->exp_year}' ";
        if ($model->exp_semester != '') {
            $query .= "     AND T1.SEMESTER = '{$model->exp_semester}' ";
        }
        if ($model->exp_executeDate != '') {
            $query .= "     AND '".str_replace('/', '-', $model->exp_executeDate)."' BETWEEN T2.APPDATE AND T2.APPENDDATE ";
        }
        if ($model->exp_chaircd != '') {
            $query .= "     AND T2.CHAIRCD = '{$model->exp_chaircd}' ";
        }
        if ($model->hrgradeclass != '') {
            $grade = substr($model->hrgradeclass, 0, 2);
            $hrclass = substr($model->hrgradeclass, 2, 3);
            $query .= "     AND T1.GRADE = '{$grade}' ";
            $query .= "     AND T1.HR_CLASS = '{$hrclass}' ";
        }
        $kekkaList = knjb104cQuery::getNarakenKekkaOverSchregnoSubclass($db, $model);
        if (get_count($kekkaList) != 0) {
            $query .= "     AND T1.SCHREGNO NOT IN ('". implode("','", $kekkaList) ."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO ";

        return $query;
    }

    //登録データ(教室ヘッダ)
    public function selectQuery3($model)
    {
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     CHAIR_TEST_FAC_LAYOUT_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->exp_year}' ";
        $query .= "     AND SEMESTER = '{$model->exp_semester}' ";
        $query .= "     AND CHAIRCD = '{$model->exp_chaircd}' ";
        $query .= "     AND FACCD = '{$model->exp_faccd}' ";

        return $query;
    }
    //登録データ(教室データ)
    public function selectQuery4($model)
    {
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     CHAIR_TEST_FAC_LAYOUT_NOTUSE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->exp_year}' ";
        $query .= "     AND SEMESTER = '{$model->exp_semester}' ";
        $query .= "     AND CHAIRCD = '{$model->exp_chaircd}' ";
        $query .= "     AND FACCD = '{$model->exp_faccd}' ";

        return $query;
    }
    //登録データ(生徒データ)
    public function selectQuery5($model)
    {
        $query .= " SELECT ";
        $query .= "     T4.HR_NAMEABBV || '-' || T1.ATTENDNO AS ATTENDNO, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T3.NAME, ";
        $query .= "     DAT.ROWS, ";
        $query .= "     DAT.COLUMNS, ";
        $query .= "     DAT.SEAT_NO ";
        $query .= " FROM ";
        $query .= "     CHAIR_TEST_FAC_LAYOUT_SCHREG_DAT DAT ";
        $query .= " LEFT JOIN SCHREG_REGD_DAT T1 ";
        $query .= "     ON T1.YEAR = DAT.YEAR ";
        $query .= "     AND T1.SEMESTER = DAT.SEMESTER ";
        $query .= "     AND T1.SCHREGNO = DAT.SCHREGNO ";
        $query .= " LEFT JOIN SCHREG_BASE_MST T3 ";
        $query .= "     ON T3.SCHREGNO = T1.SCHREGNO ";
        $query .= " LEFT JOIN SCHREG_REGD_HDAT T4 ";
        $query .= "     ON T4.YEAR = T1.YEAR ";
        $query .= "     AND T4.SEMESTER = T1.SEMESTER ";
        $query .= "     AND T4.GRADE = T1.GRADE  ";
        $query .= "     AND T4.HR_CLASS = T1.HR_CLASS  ";
        $query .= " WHERE ";
        $query .= "     DAT.EXECUTEDATE = '{$model->exp_executedate}' ";
        $query .= "     AND DAT.PERIODCD = '{$model->exp_periodcd}' ";
        $query .= "     AND DAT.CHAIRCD = '{$model->exp_chaircd}' ";

        return $query;
    }

    //更新
    public function &updateQuery($model)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     CHAIR_TEST_FAC_LAYOUT_SCHREG_DAT ";
        $query .= " WHERE ";
        $query .= "     EXECUTEDATE = '{$model->exp_executedate}' ";
        $query .= "     AND PERIODCD = '{$model->exp_periodcd}' ";
        $query .= "     AND CHAIRCD = '{$model->exp_chaircd}' ";

        $db->query($query);

        for ($i = 0; $i < get_count($model->setData); $i++) {
            list($schregno, $retu, $gyou, $seatNo) = explode(':', $model->setData[$i]);

            $data = array();
            $data['EXECUTEDATE'][TEXT] = $model->exp_executedate;
            $data['PERIODCD'][TEXT]    = $model->exp_periodcd;
            $data['CHAIRCD'][TEXT]     = $model->exp_chaircd;
            $data['ROWS'][TEXT]        = $gyou;
            $data['COLUMNS'][TEXT]     = $retu;
            $data['FACCD'][TEXT]       = $model->exp_faccd;
            $data['TESTKINDCD'][TEXT]  = $model->exp_testkindcd;
            $data['TESTITEMCD'][TEXT]  = $model->exp_testitemcd;
            $data['SCHREGNO'][TEXT]    = $schregno;
            $data['SEAT_NO'][TEXT]     = $seatNo;
            $data['YEAR'][TEXT]        = $model->exp_year;
            $data['SEMESTER'][TEXT]    = $model->exp_semester;
            ;
            $data["REGISTERCD"][TEXT]  = STAFFCD;
            $data["UPDATED"][FUNC]     = "sysdate()";

            $db->query(Query::insertSQL($data, "CHAIR_TEST_FAC_LAYOUT_SCHREG_DAT"));
        }
    }
    // 奈良県で欠課時数超過の生徒を取得する
    // 取得対象の生徒・科目の成績は生徒の合計点・平均点に含めない
    public function getNarakenKekkaOverSchregnoSubclass($db, $model)
    {
        $query = " SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '{$model->exp_year}' AND GRADE = '".substr($model->hrgradeclass, 0, 2)."' ";
        $gradeSchoolKind = $db->getOne($query);
        $categorySelected = array();
        if ($model->hrgradeclass != '') {
            $categorySelected[] = $model->hrgradeclass;
        }

        // 校種フィールドチェック
        $query = " SELECT COLNAME FROM (SELECT COLNAME FROM SYSCAT.COLUMNS WHERE TABNAME = 'V_SCHOOL_MST' AND COLNAME = 'SCHOOL_KIND') ";
        $schoolMstHasSchoolKind = $db->getOne($query);

        $query = " SELECT * FROM V_SCHOOL_MST WHERE YEAR = '{$model->exp_year}' ";
        if ($schoolMstHasSchoolKind) {
            $query .= " AND SCHOOL_KIND = '".$gradeSchoolKind."' ";
        }

        $schoolMst = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $isHoutei = $schoolMst["JUGYOU_JISU_FLG"] == "" || $schoolMst["JUGYOU_JISU_FLG"] == "1";

        $query = " SELECT MAX(SEMESTER) FROM SEMESTER_MST WHERE YEAR = '{$model->exp_year}' AND SEMESTER <> '9' ";
        $maxSemester = $db->getOne($query);

        $refusalSchregnos = array(); // 不登校生徒学籍番号 (最終学期か学年末のみ)
        $startSeme = ""; // 集計開始日付学期
        $sWeek; // 週数
        if (in_array($model->exp_semester, array($maxSemester, "9"))) {
            // 最終学期か、学年末なら年度開始日から
            $startSeme = "1";
            $sWeek = $schoolMst["JITU_SYUSU"];

            // 不登校生徒
            $query = " SELECT SCHREGNO FROM SCHREG_SCHOOL_REFUSAL_DAT ";
            $query .= " WHERE YEAR = '{$model->exp_year}' AND SCHREGNO IN (SELECT SCHREGNO FROM SCHREG_REGD_DAT WHERE YEAR = '{$model->exp_year}' AND SEMESTER = '{$model->exp_semester}' AND GRADE = '".substr($model->hrgradeclass, 0, 2)."') ";
            $refusalSchregnos = $db->getCol($query);
        } else {
            // それ以外は指定学期開始日から
            $startSeme = $model->exp_semester;
            $sWeek = $schoolMst["HOUTEI_SYUSU_SEMESTER".$model->exp_semester];
        }
        $query = " SELECT SDATE FROM SEMESTER_MST WHERE YEAR = '{$model->exp_year}' AND SEMESTER = '".$startSeme."' ";
        $sdate = $db->getOne($query);

        if (preg_match('/http/', SERVLET_URL)) {
            $url = SERVLET_URL ."/KNJZ"; // 本番用
        } else {
            $url = (empty($_SERVER["HTTPS"]) ? "http://" : "https://") . $_SERVER["HTTP_HOST"] .SERVLET_URL ."/KNJZ"; // 沖縄確認用
        }
        // 帳票のAttendAccumulateで欠課時数取得
        $data = array('CALL_DEST_JAVA_PRGID' => 'servletpack.KNJZ.detail.dao.AttendAccumulateIO',
                      'METHOD_NAME'     => 'json',
                      'DBNAME'          => DB_DATABASE,
                      'SDATE'           => str_replace('/', '-', $sdate),
                      'DATE'            => str_replace('/', '-', CTRL_DATE),
                      'ATTEND_DIV'      => 'ATTEND_SUBCLASS_DAT',
                      'CTRL_YEAR'       => $model->exp_year,
                      'CTRL_SEMESTER'   => $model->exp_semester,
                      'GRADE_HR_CLASS'  => implode(",", $categorySelected),
                      'DOCUMENTROOT'    => DOCUMENTROOT
        );

        // use key 'http' even if you send the request to https://...
        $options = array(
            'http' => array(
                'header'  => "Content-type: application/x-www-form-urlencoded\r\n",
                'method'  => 'POST',
                'content' => http_build_query($data)
            )
        );
        $target = array();
        $debug = array();
        $context  = stream_context_create($options);
        $result = file_get_contents($url, false, $context);
        if ($result != false) {
            $arr = json_decode($result, true);
            foreach ($arr as $schregno => $records) {
                foreach ($records as $record) {
                    $jisu = 0;
                    if ($isHoutei) {
                        // 単位マスタ単位と週数から授業時数から算出する
                        if ($record["CREDITS"] && $sWeek) {
                            $jisu = $record["CREDITS"] * $sWeek; // 学期・年間の授業時数
                        }
                    } else {
                        $jisu = $record["LESSON"]; // 日付までの集計時数
                    }
                    if ($jisu != 0) {
                        $jougenchi = 0;
                        if (in_array($schregno, $refusalSchregnos)) {
                            // 不登校生徒の上限値は授業時数 / 2
                            $jougenchi = $jisu / 2;
                        } else {
                            $bunshi = $schoolMst["SYUTOKU_BUNSI"];
                            $bunbo = $schoolMst["SYUTOKU_BUNBO"];
                            if ($bunshi && $bunbo > 0) {
                                // 授業時数 × 修得上限値分子 / 修得上限値分母
                                $jougenchi = $jisu * $bunshi * 1.0 / $bunbo;
                            }
                        }
                        // 上限値超過
                        if ($jougenchi && $jougenchi < $record["SICK2"]) {
                            //$target[] = "('".implode("','", explode("-", $record["SUBCLASSCD"]))."', '".$schregno."') ";
                            $target[] = $schregno;
                            $debug[] = " schregno ".$schregno." ".$record["SUBCLASSCD"].", jougen ".$jougenchi." ".$record["SICK2"];
                        }
                    }
                }
            }
        }
        //echo "[[debug]] <br>".implode("<br>", $debug);
        return $target;
    }
}
