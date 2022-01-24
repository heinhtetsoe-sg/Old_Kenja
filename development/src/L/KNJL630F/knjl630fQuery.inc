<?php
class knjl630fQuery extends Query
{
    //名称マスタより取得
    public function getNameMst($year, $namecd1, $namecd2 = "")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1, ";
        $query .= "     NAME2, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '{$year}' ";
        $query .= "     AND NAMECD1 = '{$namecd1}' ";
        if ($namecd2) {
            $query .= " AND NAMECD2 = '{$namecd2}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    public function selectSchregBaseMst($schregno)
    {
        $query  = " SELECT ";
        $query .= "     NAME, ";
        $query .= "     NAME_KANA, ";
        $query .= "     SEX, ";
        $query .= "     BIRTHDAY ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$schregno}' ";

        return $query;
    }

    // 中３コースの存在チェック
    public function selectSchregCourse($schregno, $year, $semester, $course)
    {
        $query  = " SELECT ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD, ";
        $query .= "     COURSECODE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHREGNO   = '{$schregno}' ";
        $query .= "     AND YEAR       = '{$year}' ";
        $query .= "     AND SEMESTER   = '{$semester}' ";
        $query .= "     AND COURSECODE = '{$course}' ";
    
        return $query;
    }

    //名称マスタより取得
    public function getFsCd($year, $name1)
    {
        $query  = " SELECT ";
        $query .= "     NAMESPARE1 VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '{$year}' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        $query .= " AND NAME1 = '{$name1}' ";

        return $query;
    }

    //名称マスタより和暦管理情報を取得
    public function getEra($year)
    {
        $period = $year."-03-01";

        $query  = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     SUBSTR(NAMESPARE2, 1, 4) AS STARTYEAR";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '{$year}' ";
        $query .= "     AND NAMECD1 = 'L007' ";
        $query .= "     AND '{$period}' BETWEEN NAMESPARE2 AND NAMESPARE3 ";
    
        return $query;
    }

    public function selectSchregAddr($schregno)
    {
        $query  = " SELECT ";
        $query .= "     ZIPCD, ";
        $query .= "     ADDR1, ";
        $query .= "     ADDR2, ";
        $query .= "     TELNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_ADDRESS_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     ISSUEDATE DESC ";
    
        return $query;
    }

    public function selectGuardianDat($schregno)
    {
        $query  = " SELECT ";
        $query .= "     GUARD_NAME, ";
        $query .= "     GUARD_KANA, ";
        $query .= "     GUARD_ZIPCD, ";
        $query .= "     GUARD_ADDR1, ";
        $query .= "     GUARD_ADDR2, ";
        $query .= "     GUARD_TELNO, ";
        $query .= "     RELATIONSHIP ";
        $query .= " FROM ";
        $query .= "     GUARDIAN_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$schregno}' ";
    
        return $query;
    }

    public function selectExamCourse($model, $examCourse)
    {
        $query  = " SELECT ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD, ";
        $query .= "     EXAMCOURSECD ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND TESTDIV      = '1' "; // '1' 固定
        $query .= "     AND EXAMCOURSECD = '{$examCourse}' ";
        $query .= " ORDER BY ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD, ";
        $query .= "     EXAMCOURSECD ";
    
        return $query;
    }

    // 学籍基礎情報の存在チェック
    public function checkSchreg($model, $schregno, $name = "")
    {
        $schregno = sprintf("%08d", $schregno);

        $query  = " SELECT ";
        $query .= "     COUNT(SCHREGNO) ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "         SCHREGNO = '{$schregno}' ";
        if ($name) {
            $query .= "     AND NAME = '{$name}' ";
        }
    
        return $query;
    }

    // 中３コースの存在チェック
    public function checkSchregCourse($schregno, $year, $semester, $course)
    {
        $schregno = sprintf("%08d", $schregno);

        $query  = " SELECT ";
        $query .= "     COUNT(SCHREGNO) ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHREGNO   = '{$schregno}' ";
        $query .= "     AND YEAR       = '{$year}' ";
        $query .= "     AND SEMESTER   = '{$semester}' ";
        $query .= "     AND COURSECODE = '{$course}' ";
    
        return $query;
    }

    // 受験番号の範囲チェック
    public function checkReceptNo($model, $examType, $receptno)
    {
        $receptno = sprintf("%04d", $receptno);

        $query  = " SELECT ";
        $query .= "     COUNT(RECNO) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_NO_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND TESTDIV      = '{$model->testdiv}' ";
        $query .= "     AND MEMO_KISO    = '{$examType}' ";
        $query .= "     AND '{$receptno}' BETWEEN EXAMNO_FROM AND EXAMNO_TO ";
    
        return $query;
    }

    // 志望コースの存在チェック
    public function checkExamCourse($model, $examCourse)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(EXAMCOURSECD) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND TESTDIV      = '1' "; // '1' 固定
        $query .= "     AND EXAMCOURSECD = '{$examCourse}' ";

        return $query;
    }

    // 受験科目数の存在チェック
    public function checkExamType($model, $namecd2)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(NAMECD2) ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '{$model->year}' ";
        $query .= "     AND NAMECD1 = 'L005' ";
        $query .= "     AND NAMECD2 = '{$namecd2}' ";
    
        return $query;
    }

    // 英語見なし得点の存在チェック
    public function checkEiken($model, $namecd2)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(NAMECD2) ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR       = '{$model->year}' ";
        $query .= "     AND NAMECD1     = 'L055' ";
        $query .= "     AND NAMECD2     = '{$namecd2}' ";
        $query .= "     AND NAMESPARE1 = '{$model->applicantdiv}' ";

        return $query;
    }

    // 奨学区分の存在チェック
    public function checkShougaku($model, $namecd2)
    {
        $namecd2 = sprintf("%02d", $namecd2);

        $query  = "";
        $query .= " SELECT ";
        $query .= "     COUNT(NAMECD2) ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '{$model->year}' ";
        $query .= "     AND NAMECD1 = 'L025' ";
        $query .= "     AND NAMECD2 = '{$namecd2}' ";
        $query .= "     AND NAMESPARE1 = '{$model->applicantdiv}' ";

        return $query;
    }

    //学籍番号よりすでに登録済みにの受験者番号を取得する。
    public function getExamnoQuery($model, $schregno)
    {
        $query  = " SELECT ";
        $query .= "     EXAMNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND SEQ = '026' ";
        $query .= "     AND REMARK1 = '{$schregno}' ";

        return $query;
    }

    //MAX管理番号＋１を取得
    public function getMaxExamnoQuery($db, $model)
    {
        $query  = " SELECT ";
        $query .= "     MAX(EXAMNO) AS EXAMNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_KANRI_NO_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $maxExam = $db->getOne($query);

        if (!strlen($maxExam)) {
            $query  = " SELECT ";
            $query .= "     MAX(EXAMNO) AS EXAMNO ";
            $query .= " FROM ";
            $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
            $query .= " WHERE ";
            $query .= "     ENTEXAMYEAR = '{$model->year}' ";
            $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
            $maxExam = $db->getOne($query);
        }

        if (!strlen($maxExam)) {
            $maxExam = "00001";
        } else {
            $maxExam++;
        }

        return sprintf("%05d", $maxExam);
    }

    /**************/
    /* ＣＳＶ取込 */
    /**************/
    //データの追加（ＣＳＶデータより読込）
    public function updateQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < count($data_arr); $i++) {
            $schregno = sprintf("%08d", $data_arr[$i]["SCHREGNO"]);
            $receptno = sprintf("%04d", $data_arr[$i]["RECEPTNO"]);
            $shougaku = sprintf("%02d", $data_arr[$i]["SHOUGAKU"]);

            $query = knjl630fQuery::getExamnoQuery($model, $schregno);
            $examno = $db->getOne($query);
            if (!$examno) {
                $examno = knjl630fQuery::getMaxExamnoQuery($db, $model);
                $examno = $model->applicantdiv == "1" ? $examno : $receptno;
            }

            //１件取得
            $query = knjl630fQuery::selectSchregBaseMst($schregno);
            $rowSchregBaseMst = $db->getrow($query, DB_FETCHMODE_ASSOC);

            $query = knjl630fQuery::selectSchregCourse($schregno, CTRL_YEAR, CTRL_SEMESTER, $data_arr[$i]["COURSE_J"]);
            $rowSchregCourse = $db->getrow($query, DB_FETCHMODE_ASSOC);

            $name1 = $model->applicantdiv == "1" ? "P" : "J";
            $query = knjl630fQuery::getFsCd($model->year, $name1);
            $rowFsmst = $db->getrow($query, DB_FETCHMODE_ASSOC);

            $namecd2 = $model->applicantdiv == "1" ? "J" : "H";
            $query = knjl630fQuery::getEra($model->year);
            $rowEramst = $db->getrow($query, DB_FETCHMODE_ASSOC);

            $query = knjl630fQuery::selectSchregAddr($schregno);
            $rowSchregAddr = $db->getrow($query, DB_FETCHMODE_ASSOC);

            $query = knjl630fQuery::selectGuardianDat($schregno);
            $rowGuardianDat = $db->getrow($query, DB_FETCHMODE_ASSOC);

            $query = knjl630fQuery::selectExamCourse($model, $data_arr[$i]["EXAMCOURSE"]);
            $rowExamCourse = $db->getrow($query, DB_FETCHMODE_ASSOC);

            $query = knjl630fQuery::selectExamCourse($model, $data_arr[$i]["EXAMCOURSE2"]);
            $rowExamCourse2 = $db->getrow($query, DB_FETCHMODE_ASSOC);

            //ENTEXAM_APPLICANTBASE_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]   = $model->year;
            $data["APPLICANTDIV"][TEXT]  = $model->applicantdiv;
            $data["EXAMNO"][TEXT]        = $examno;
            $data["TESTDIV"][TEXT]       = $model->testdiv;
            $data["SHDIV"][TEXT]         = "1";
            $data["DESIREDIV"][TEXT]     = "1";
            if ($model->applicantdiv == "2") {
                $data["TESTDIV1"][TEXT]      = $data_arr[$i]["EXAM_TYPE"];
            }
            $data["RECEPTDATE"][TEXT]    = str_replace("/", "-", $model->field["RECEPTDATE"]);
            $data["NAME"][TEXT]          = $rowSchregBaseMst["NAME"];
            $data["NAME_KANA"][TEXT]     = $rowSchregBaseMst["NAME_KANA"];
            $data["SEX"][TEXT]           = $rowSchregBaseMst["SEX"]; // 性別
            $data["BIRTHDAY"][TEXT]      = $rowSchregBaseMst["BIRTHDAY"];
            $data["FS_CD"][TEXT]         = $rowFsmst["VALUE"];
            $data["FS_GRDYEAR"][TEXT]    = $model->year;
            $data["FS_ERACD"][TEXT]      = $rowEramst["NAMECD2"];
            $data["FS_Y"][TEXT]          = sprintf("%02d", ($model->year - $rowEramst["STARTYEAR"] + 1));
            $data["FS_M"][TEXT]          = "03"; // 03:3月 固定
            $data["FS_DAY"][TEXT]        = $model->year."-03-01";
            $data["FS_GRDDIV"][TEXT]     = "2"; // 2:卒業 固定
            $data["REGISTERCD"][TEXT]    = STAFFCD;
            $data["UPDATED"][FUNC]       = "sysdate()";
            $where  = " WHERE ENTEXAMYEAR = '{$model->year}' ";
            $where .= "       AND APPLICANTDIV = '{$model->applicantdiv}' ";
            $where .= "       AND EXAMNO = '{$examno}'";
            $dataCnt = $db->getOne(" SELECT COUNT(*) FROM ENTEXAM_APPLICANTBASE_DAT ".$where);
            if ($dataCnt == 0) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_APPLICANTADDR_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]   = $model->year;
            $data["APPLICANTDIV"][TEXT]  = $model->applicantdiv;
            $data["EXAMNO"][TEXT]        = $examno;
            $data["ZIPCD"][TEXT]         = $rowSchregAddr["ZIPCD"];
            $data["ADDRESS1"][TEXT]      = $rowSchregAddr["ADDR1"];
            $data["ADDRESS2"][TEXT]      = $rowSchregAddr["ADDR2"];
            $data["TELNO"][TEXT]         = $rowSchregAddr["TELNO"];
            $data["GNAME"][TEXT]         = $rowGuardianDat["GUARD_NAME"];
            $data["GKANA"][TEXT]         = $rowGuardianDat["GUARD_KANA"];
            $data["GZIPCD"][TEXT]        = $rowGuardianDat["GUARD_ZIPCD"];
            $data["GADDRESS1"][TEXT]     = $rowGuardianDat["GUARD_ADDR1"];
            $data["GADDRESS2"][TEXT]     = $rowGuardianDat["GUARD_ADDR2"];
            $data["GTELNO"][TEXT]        = $rowGuardianDat["GUARD_TELNO"];
            $data["RELATIONSHIP"][TEXT]  = $rowGuardianDat["RELATIONSHIP"];
            $data["REGISTERCD"][TEXT]    = STAFFCD;
            $data["UPDATED"][FUNC]       = "sysdate()";
            $where  = " WHERE ENTEXAMYEAR = '{$model->year}' ";
            $where .= "       AND APPLICANTDIV = '{$model->applicantdiv}' ";
            $where .= "       AND EXAMNO = '{$examno}'";
            $dataCnt = $db->getOne(" SELECT COUNT(*) FROM ENTEXAM_APPLICANTADDR_DAT ".$where);
            if ($dataCnt == 0) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTADDR_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTADDR_DAT", $where);
            }
            $db->query($query);

            //第一志望コース
            //ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ=001
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]  = $model->year;
            $data["APPLICANTDIV"][TEXT] = $model->applicantdiv;
            $data["EXAMNO"][TEXT]       = $examno;
            $data["SEQ"][TEXT]          = "001";
            $data["REMARK8"][TEXT]      = $rowExamCourse["COURSECD"];
            $data["REMARK9"][TEXT]      = $rowExamCourse["MAJORCD"];
            $data["REMARK10"][TEXT]     = $rowExamCourse["EXAMCOURSECD"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";
            $where  = " WHERE ENTEXAMYEAR = '{$model->year}' ";
            $where .= "       AND APPLICANTDIV = '{$model->applicantdiv}' ";
            $where .= "       AND EXAMNO = '{$examno}'";
            $where .= "       AND SEQ = '001'";
            $dataCnt = $db->getOne(" SELECT COUNT(*) FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ".$where);
            if ($dataCnt == 0) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            }
            $db->query($query);

            //第二志望コース
            //ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ=017
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]  = $model->year;
            $data["APPLICANTDIV"][TEXT] = $model->applicantdiv;
            $data["EXAMNO"][TEXT]       = $examno;
            $data["SEQ"][TEXT]          = "017";
            $data["REMARK1"][TEXT]      = $rowExamCourse2["COURSECD"];
            $data["REMARK2"][TEXT]      = $rowExamCourse2["MAJORCD"];
            $data["REMARK3"][TEXT]      = $rowExamCourse2["EXAMCOURSECD"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";
            $where  = " WHERE ENTEXAMYEAR = '{$model->year}' ";
            $where .= "       AND APPLICANTDIV = '{$model->applicantdiv}' ";
            $where .= "       AND EXAMNO = '{$examno}'";
            $where .= "       AND SEQ = '017'";
            $dataCnt = $db->getOne(" SELECT COUNT(*) FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ".$where);
            if ($dataCnt == 0) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            }
            $db->query($query);

            if ($model->applicantdiv == "1") {
                //ENTEXAM_KANRI_NO_DAT
                $data = array();
                $data["ENTEXAMYEAR"][TEXT]  = $model->year;
                $data["APPLICANTDIV"][TEXT] = $model->applicantdiv;
                $data["EXAMNO"][TEXT]       = $examno;
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";
                $where  = " WHERE ENTEXAMYEAR = '{$model->year}' ";
                $where .= "       AND APPLICANTDIV = '{$model->applicantdiv}' ";
                $dataCnt = $db->getOne(" SELECT COUNT(*) FROM ENTEXAM_KANRI_NO_DAT ".$where);
                if ($dataCnt == 0) {
                    $query = Query::insertSQL($data, "ENTEXAM_KANRI_NO_DAT");
                } else {
                    $query = Query::updateSQL($data, "ENTEXAM_KANRI_NO_DAT", $where);
                }
                $db->query($query);

                //入試区分
                //ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ=010
                $data = array();
                $data["ENTEXAMYEAR"][TEXT]  = $model->year;
                $data["APPLICANTDIV"][TEXT] = $model->applicantdiv;
                $data["EXAMNO"][TEXT]       = $examno;
                $data["SEQ"][TEXT]          = "010";
                $data["REMARK2"][TEXT]      = '2'; // 2 固定
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";
                $where  = " WHERE ENTEXAMYEAR = '{$model->year}' ";
                $where .= "       AND APPLICANTDIV = '{$model->applicantdiv}' ";
                $where .= "       AND EXAMNO = '{$examno}'";
                $where .= "       AND SEQ = '010'";
                $dataCnt = $db->getOne(" SELECT COUNT(*) FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ".$where);
                if ($dataCnt == 0) {
                    $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
                } else {
                    $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
                }
                $db->query($query);

                //受験科目数
                //ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ=011
                $data = array();
                $data["ENTEXAMYEAR"][TEXT]  = $model->year;
                $data["APPLICANTDIV"][TEXT] = $model->applicantdiv;
                $data["EXAMNO"][TEXT]       = $examno;
                $data["SEQ"][TEXT]          = "011";
                $data["REMARK2"][TEXT]      = $data_arr[$i]["EXAM_TYPE"];
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";
                $where  = " WHERE ENTEXAMYEAR = '{$model->year}' ";
                $where .= "       AND APPLICANTDIV = '{$model->applicantdiv}' ";
                $where .= "       AND EXAMNO = '{$examno}'";
                $where .= "       AND SEQ = '011'";
                $dataCnt = $db->getOne(" SELECT COUNT(*) FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ".$where);
                if ($dataCnt == 0) {
                    $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
                } else {
                    $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
                }
                $db->query($query);

                //受験番号
                //ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ=012
                $data = array();
                $data["ENTEXAMYEAR"][TEXT]  = $model->year;
                $data["APPLICANTDIV"][TEXT] = $model->applicantdiv;
                $data["EXAMNO"][TEXT]       = $examno;
                $data["SEQ"][TEXT]          = "012";
                $data["REMARK2"][TEXT]      = $receptno;
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";
                $where  = " WHERE ENTEXAMYEAR = '{$model->year}' ";
                $where .= "       AND APPLICANTDIV = '{$model->applicantdiv}' ";
                $where .= "       AND EXAMNO = '{$examno}'";
                $where .= "       AND SEQ = '012'";
                $dataCnt = $db->getOne(" SELECT COUNT(*) FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ".$where);
                if ($dataCnt == 0) {
                    $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
                } else {
                    $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
                }
                $db->query($query);
            } else {
                //中３コース
                //ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ=041
                $data = array();
                $data["ENTEXAMYEAR"][TEXT]  = $model->year;
                $data["APPLICANTDIV"][TEXT] = $model->applicantdiv;
                $data["EXAMNO"][TEXT]       = $examno;
                $data["SEQ"][TEXT]          = "041";
                $data["REMARK1"][TEXT]      = $rowSchregCourse["COURSECD"];
                $data["REMARK2"][TEXT]      = $rowSchregCourse["MAJORCD"];
                $data["REMARK3"][TEXT]      = $rowSchregCourse["COURSECODE"];
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";
                $where  = " WHERE ENTEXAMYEAR = '{$model->year}' ";
                $where .= "       AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
                $where .= "       AND EXAMNO = '{$examno}'";
                $where .= "       AND SEQ = '041'";
                $dataCnt = $db->getOne(" SELECT COUNT(*) FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ".$where);
                if ($dataCnt == 0) {
                    $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
                } else {
                    $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
                }
                $db->query($query);
            }

            //英語見なし得点、奨学区分
            //ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ=005
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]  = $model->year;
            $data["APPLICANTDIV"][TEXT] = $model->applicantdiv;
            $data["EXAMNO"][TEXT]       = $examno;
            $data["SEQ"][TEXT]          = "005";
            $data["REMARK1"][TEXT]      = $data_arr[$i]["EIKEN_SHUTOKU_KYUU"];
            $data["REMARK2"][TEXT]      = $shougaku;
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";
            $where  = " WHERE ENTEXAMYEAR = '{$model->year}' ";
            $where .= "       AND APPLICANTDIV = '{$model->applicantdiv}' ";
            $where .= "       AND EXAMNO = '{$examno}'";
            $where .= "       AND SEQ = '005'";
            $dataCnt = $db->getOne(" SELECT COUNT(*) FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ".$where);
            if ($dataCnt == 0) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            }
            $db->query($query);

            //学籍番号
            //ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ=026
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]  = $model->year;
            $data["APPLICANTDIV"][TEXT] = $model->applicantdiv;
            $data["EXAMNO"][TEXT]       = $examno;
            $data["SEQ"][TEXT]          = "026";
            $data["REMARK1"][TEXT]      = $schregno;
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";
            $where  = " WHERE ENTEXAMYEAR = '{$model->year}' ";
            $where .= "       AND APPLICANTDIV = '{$model->applicantdiv}' ";
            $where .= "       AND EXAMNO = '{$examno}'";
            $where .= "       AND SEQ = '026'";
            $dataCnt = $db->getOne(" SELECT COUNT(*) FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ".$where);
            if ($dataCnt == 0) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            }
            $db->query($query);

            $cnt++;
        }

        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //エラーデータの削除
    public function deleteQueryErr()
    {
        $query  = "DELETE FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID = 'KNJL630F' ";
        return $query;
    }

    //エラーＤＢへの追加
    public function insertQueryErr($db, $record_no, $errMessage)
    {
        $data1["PROGRAMID"][TEXT]  = "KNJL630F";
        $data1["MSGROW"][NUMBER]   = $record_no;
        $data1["MSGREMARK"][TEXT]  = $errMessage;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $result = $db->query($query);
    }

    //CVS作成用のQUERY
    public function selectCsvQuery()
    {
        $query  = "SELECT MSGROW, MSGREMARK FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID = 'KNJL630F'";

        return $query;
    }
}
