<?php

require_once('for_php7.php');

class knjx090dquery extends Query
{
    //高セキュリティー設定
    public function getSecurityHigh()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJX090D' AND ";
        $query .= "     INVALID_FLG = '0' ";

        return $query;
    }

    //学校種別取得
    public function getSchoolCd()
    {
        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";

        return $query;
    }

    //年度・学期一覧取得
    public function getYearSemeList()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.YEAR || T1.SEMESTER AS VALUE, ";
        $query .= "     T1.YEAR || '年度 ' || T2.SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT T1 ";
        $query .= "     INNER JOIN SEMESTER_MST T2 ON T1.YEAR = T2.YEAR AND T1.SEMESTER = T2.SEMESTER ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //講座一覧取得
    public function getChaircdList($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.CHAIRCD AS VALUE, ";
        $query .= "     T1.CHAIRCD || ' ' || T2.CHAIRNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT T1, ";
        $query .= "     CHAIR_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || T1.SEMESTER = '".$model->field["YEAR_SEMESTER"]."' AND ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "     T1.CHAIRCD  = T2.CHAIRCD ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学籍番号一覧取得
    public function getSchregnoList($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.SCHREGNO AS VALUE, ";
        $query .= "     T1.SCHREGNO || ' ' || T2.NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT T1, ";
        $query .= "     SCHREG_BASE_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || T1.SEMESTER = '".$model->field["YEAR_SEMESTER"]."' AND ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     T1.SCHREGNO ";

        return $query;
    }

    //存在チェック -- CHAIR_STD_DAT
    public function checkChairStdDat($data_arr, $chaircd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$data_arr["YEAR"]."' AND ";
        $query .= "     SEMESTER    = '".$data_arr["SEMESTER"]."' AND ";
        $query .= "     CHAIRCD     = '".$chaircd."' AND ";
        $query .= "     APPDATE     = '".$data_arr["APPDATE"]."' AND ";
        $query .= "     SCHREGNO    = '".$data_arr["SCHREGNO"]."' ";

        return $query;
    }

    //更新（ＣＳＶ取込）
    public function getUpdateQuery($db, $data_arr)
    {
        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            for ($j = 1; $j <= $data_arr[$i]["COL_NO"]; $j++) {
                //データ有無チェック -- CHAIR_STD_DAT
                $check_cnt = $db->getOne(knjx090dQuery::checkChairStdDat($data_arr[$i], $data_arr[$i]["CHAIRCD".$j]));
                $flg = ($check_cnt == 0) ? "insert" : "update";

                $data = array();
                if ($flg == "insert") {
                    $data["YEAR"][TEXT]            = $data_arr[$i]["YEAR"];
                    $data["SEMESTER"][TEXT]        = $data_arr[$i]["SEMESTER"];
                    $data["CHAIRCD"][TEXT]         = $data_arr[$i]["CHAIRCD".$j];
                    $data["SCHREGNO"][TEXT]        = $data_arr[$i]["SCHREGNO"];
                    $data["APPDATE"][TEXT]         = $data_arr[$i]["APPDATE"];
                }
                $data["APPENDDATE"][TEXT]      = $data_arr[$i]["APPENDDATE"];
                $data["ROW"][TEXT]             = $data_arr[$i]["ROW".$j];
                $data["COLUMN"][TEXT]          = $data_arr[$i]["COLUMN".$j];
                $data["REGISTERCD"][TEXT]      = STAFFCD;
                $data["UPDATED"][NUMBER]       = "sysdate()";

                if ($flg == "insert") {
                    $query = Query::insertSQL($data, "CHAIR_STD_DAT");
                } else {
                    $where  = " WHERE ";
                    $where .= "     YEAR        = '".$data_arr[$i]["YEAR"]."' AND ";
                    $where .= "     SEMESTER    = '".$data_arr[$i]["SEMESTER"]."' AND ";
                    $where .= "     CHAIRCD     = '".$data_arr[$i]["CHAIRCD".$j]."' AND ";
                    $where .= "     APPDATE     = '".$data_arr[$i]["APPDATE"]."' AND ";
                    $where .= "     SCHREGNO    = '".$data_arr[$i]["SCHREGNO"]."' ";

                    $query = Query::updateSQL($data, "CHAIR_STD_DAT", $where);
                }
                $db->query($query);
            }
            $cnt++;
        }
        return $cnt;
    }

    //削除（ＣＳＶ取込）
    public function getDeleteQuery($db, $data_arr)
    {
        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            for ($j = 1; $j <= $data_arr[$i]["COL_NO"]; $j++) {
                $query  = " DELETE FROM ";
                $query .= "     CHAIR_STD_DAT ";
                $query .= " WHERE ";
                $query .= "     YEAR        = '".$data_arr[$i]["YEAR"]."' AND ";
                $query .= "     SEMESTER    = '".$data_arr[$i]["SEMESTER"]."' AND ";
                $query .= "     CHAIRCD     = '".$data_arr[$i]["CHAIRCD".$j]."' AND ";
                $query .= "     APPDATE     = '".$data_arr[$i]["APPDATE"]."' AND ";
                $query .= "     SCHREGNO    = '".$data_arr[$i]["SCHREGNO"]."' ";

                $db->query($query);
            }
            $cnt++;
        }
        return $cnt;
    }

    //講座の存在チェック
    public function getChairDat($year, $semester)
    {
        $query  = " SELECT ";
        $query .= "     CHAIRCD ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$year."' AND ";
        $query .= "     SEMESTER = '".$semester."' ";

        return $query;
    }

    //日付チェック（学期内）
    public function checkSemesterMst($year, $semester, $date)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$year."' AND ";
        $query .= "     SEMESTER = '".$semester."' AND ";
        $query .= "     '".$date."' BETWEEN SDATE AND EDATE ";

        return $query;
    }

    //在籍チェック
    public function checkSchregno($year, $semester, $schregno)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$year."' AND ";
        $query .= "     SEMESTER = '".$semester."' AND ";
        $query .= "     SCHREGNO = '".$schregno."' ";

        return $query;
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1 = array();
        $data1["PROGRAMID"][TEXT]   = "KNJX090D";
        $data1["MSGROW"][NUMBER]    = $record_no;
        $data1["MSGREMARK"][TEXT]   = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $result = $db->query($query);
    }

    //学期マスタ取得（ヘッダ例出力用）
    public function getSemesterMst()
    {
        $query  = " SELECT ";
        $query .= "     SDATE, ";
        $query .= "     EDATE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".CTRL_SEMESTER."' ";

        return $query;
    }

    //ＣＳＶデータ出力
    public function getCsvQuery($model)
    {
        $query  = " WITH MAX_APPDATE AS ( ";
        $query .= "     SELECT ";
        $query .= "         CHAIRCD, ";
        $query .= "         SCHREGNO, ";
        $query .= "         MAX(APPDATE) AS APPDATE ";
        $query .= "     FROM ";
        $query .= "         CHAIR_STD_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR || SEMESTER = '".$model->field["YEAR_SEMESTER"]."' ";
        if ($model->field["CHAIRCD"] != "") {
            $query .= "     AND CHAIRCD = '".$model->field["CHAIRCD"]."' ";
        }
        if ($model->field["SCHREGNO"] != "") {
            $query .= "     AND SCHREGNO = '".$model->field["SCHREGNO"]."' ";
        }
        if ($model->field["DATE"] != "") {
            $query .= "     AND '".str_replace("/", "-", $model->field["DATE"])."' BETWEEN APPDATE AND APPENDDATE ";
        }
        $query .= "     GROUP BY ";
        $query .= "         CHAIRCD, ";
        $query .= "         SCHREGNO ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     L1.GRADE, ";
        $query .= "     L1.HR_CLASS, ";
        $query .= "     L1.ATTENDNO, ";
        $query .= "     L2.NAME, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     L4.SDATE AS APPDATE, ";
        $query .= "     L4.EDATE AS APPENDDATE, ";
        $query .= "     T1.CHAIRCD, ";
        $query .= "     L3.CHAIRNAME, ";
        $query .= "     T1.ROW, ";
        $query .= "     T1.COLUMN ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT L1 ";
        $query .= "              ON L1.YEAR     = T1.YEAR ";
        $query .= "             AND L1.SEMESTER = T1.SEMESTER ";
        $query .= "             AND L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST L2 ";
        $query .= "              ON L2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN CHAIR_DAT L3 ";
        $query .= "              ON L3.YEAR     = T1.YEAR ";
        $query .= "             AND L3.SEMESTER = T1.SEMESTER ";
        $query .= "             AND L3.CHAIRCD  = T1.CHAIRCD ";
        $query .= "     LEFT JOIN SEMESTER_MST L4 ";
        $query .= "              ON L4.YEAR     = T1.YEAR ";
        $query .= "             AND L4.SEMESTER = T1.SEMESTER ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || T1.SEMESTER = '".$model->field["YEAR_SEMESTER"]."' AND ";
        if ($model->field["CHAIRCD"] != "") {
            $query .= "     T1.CHAIRCD = '".$model->field["CHAIRCD"]."' AND ";
        }
        if ($model->field["SCHREGNO"] != "") {
            $query .= "     T1.SCHREGNO = '".$model->field["SCHREGNO"]."' AND ";
        }
        if ($model->field["DATE"] != "") {
            $query .= "     '".str_replace("/", "-", $model->field["DATE"])."' BETWEEN T1.APPDATE AND T1.APPENDDATE AND ";
        }
        $query .= "     EXISTS (SELECT ";
        $query .= "                 'X' ";
        $query .= "             FROM ";
        $query .= "                 MAX_APPDATE S1 ";
        $query .= "             WHERE ";
        $query .= "                 T1.CHAIRCD  = S1.CHAIRCD AND ";
        $query .= "                 T1.SCHREGNO = S1.SCHREGNO AND ";
        $query .= "                 T1.APPDATE  = S1.APPDATE ";
        $query .= "             ) ";
        $query .= " ORDER BY ";
        $query .= "     CHAIRCD, ";
        $query .= "     SCHREGNO, ";
        $query .= "     APPDATE ";

        return $query;
    }
}
